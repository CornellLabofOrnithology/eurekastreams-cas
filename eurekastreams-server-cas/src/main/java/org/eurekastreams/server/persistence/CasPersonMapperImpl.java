/*
 * Copyright (c) 2009-2010 Lockheed Martin Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eurekastreams.server.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eurekastreams.commons.hibernate.QueryOptimizer;
import org.eurekastreams.server.domain.CasPerson;
import javax.persistence.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class provides the mapper functionality for Person entities.
 */
public class CasPersonMapperImpl extends DomainEntityMapper<CasPerson> implements CasPersonMapper
{
    /**
     * Local log instance.
     */
    private static Log logger = LogFactory.getLog(CasPersonMapper.class);
    

    /**
     * Constructor.
     * 
     * @param inQueryOptimizer
     *            the QueryOptimizer to use for specialized functions.
     */
    public CasPersonMapperImpl(final QueryOptimizer inQueryOptimizer)
    {
        super(inQueryOptimizer);
    }

    /**
     * Retrieve the name of the DomainEntity. This is to allow for the super class to identify the table within
     * hibernate.
     * 
     * @return The name of the domain entity.
     */
    @Override
    protected String getDomainEntityName()
    {
        return "CasPerson";
    }

  
    /**
     * Creates a follower/following relationship between two Person objects.
     * 
     * @param followerId
     *            The id of the follower Person
     * @param followingId
     *            The id of the person being Followed.
     */
    @Transactional
    public void updateUserId(final String accountId, final String userId)
    {
    	//protect ourselves from ever changing input
    	String lowerCaseAccountId = accountId.toLowerCase();
    	
    	//for testing only check for this userid existing
        /*Query q = getEntityManager().createQuery(
                "FROM Person where accountId=:accountId").setParameter("accountId",lowerCaseAccountId);

        if (q.getResultList().size() > 0) {
            logger.info("found user record to update with userid");
        } else {
        	logger.info("did not find user record to update with userid");
        }*/

    	// TODO dont understand comments below...
        // now update the counts for persons subtracting 1 for themselves.
    	// version update just makes sure to update the person version column, since we're updating that record, standard hql
        int result = getEntityManager().createQuery(
                "update versioned CasPerson set userId = :userId where accountId = :accountId").setParameter(
                "userId", userId).setParameter("accountId", lowerCaseAccountId).executeUpdate();

        if(result < 1){
        	logger.error("Eurekastreams server cas person mapper failed to update a person record with the cas user id. This is a critical error!!");
        }
       
        getEntityManager().flush();
        getEntityManager().clear();

    }

  
}
