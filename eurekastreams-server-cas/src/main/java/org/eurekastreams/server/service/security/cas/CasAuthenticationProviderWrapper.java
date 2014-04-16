package org.eurekastreams.server.service.security.cas;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eurekastreams.commons.actions.context.service.ServiceActionContext;
import org.eurekastreams.commons.actions.service.TaskHandlerServiceAction;
import org.eurekastreams.commons.server.service.ActionController;
import org.eurekastreams.server.domain.Person;
import org.eurekastreams.server.persistence.CasPersonMapper;
import org.eurekastreams.server.persistence.PersonMapper;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.security.providers.cas.CasAuthenticationProvider;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;



public class CasAuthenticationProviderWrapper extends CasAuthenticationProvider {

	/**
     * Logger.
     */
    private static Log log = LogFactory.getLog(CasAuthenticationProviderWrapper.class);

	 /**
     * Mapper for Person information.
     */
    private final PersonMapper personMapper;
    
    /**
     * Action to create user from LDAP.
     */
    private final TaskHandlerServiceAction createUserfromCasAction;
    

    /**
     * {@link ActionController}.
     */
    private final ActionController serviceActionController;
    
    /**
     * Mapper for Person information.
     */
    private final CasPersonMapper casPersonMapper;
    
    public static final Pattern FORBIDDEN_MEMCACHED_CHARACTERS = Pattern.compile("\\s|\\n|\\r|\\t");
    
    /**
     * 
     * @param inPersonMapper
     * @param inCreateUserfromCasAction
     * @param inServiceActionController
     * @param inCasPersonMapper
     */
	public CasAuthenticationProviderWrapper(final PersonMapper inPersonMapper,
			final TaskHandlerServiceAction inCreateUserfromCasAction, 
				final ActionController inServiceActionController, 
					final CasPersonMapper inCasPersonMapper) {
		super();
		this.personMapper = inPersonMapper;
		this.createUserfromCasAction = inCreateUserfromCasAction;
		this.serviceActionController = inServiceActionController;
		this.casPersonMapper = inCasPersonMapper;
	}
    
	/**
	 * This is the way we're extending the login process, if the cas account isnt found, we make it
	 * 
	 * Note: see UserDetailsServiceImpl#loadUserByUsername for the basics of what we're trying to copy here
	 * 
	 * TODO should write tests for any of these custom classes in server-cas extensions project
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	protected UserDetails loadUserByAssertion(final Assertion assertion){
		//check if user exists already
		String username = assertion.getPrincipal().getName();
		Map<String,String> casAssertions = assertion.getPrincipal().getAttributes();  
		
		//regardless, now theres a possibility we need an alternate preferred name, so store the original name and use that for preferred name
		casAssertions.put("preferredName", username); 
				
		//check that username contains no invalid memcache characters (spaces, newlines, etc), since memcache keys are created with this value
		if(FORBIDDEN_MEMCACHED_CHARACTERS.matcher(username).find()){
			username = FORBIDDEN_MEMCACHED_CHARACTERS.matcher(username).replaceAll("_");
		    casAssertions.put("username", username); 
		}
		
		log.info("Cas authentication extension is checking first whether" + username + " is in the db already");
		
		Person person = personMapper.findByAccountId(username);
		
		/**
		 * es is setup to try and create users from ldap, but we need to create a user from the cas assertion response
		 */
		if(person == null){
			log.info("The person wasn't found in db, so lets execute custom create user from cas action");
			
			//add person to db
			 person = (Person) serviceActionController.execute(new ServiceActionContext((Serializable) casAssertions, null),
                     												createUserfromCasAction);
			 
			 //all es accountids are lowercase, who knows what cas is going to through at us, so always go to lowercase
			 log.info("update person with our own custom person mapper that can update the cas_user_id from the user_id in the cas response");
			 casPersonMapper.updateUserId(username, (String)assertion.getPrincipal().getAttributes().get("user_id"));
		}
		
		return super.getUserDetailsService().loadUserByUsername(username);
	}
}
