package org.eurekastreams.server.cas;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.mvc.WebContentInterceptor;

/**
 * To use CAS properly with html pages (or spring security for that mattter)
 * we need to make sure plain html pages aren't cached if we end to 
 * secure them.
 * @author cm325
 * @see applicationContext-security-cas-jass.xml#responseCacheHeadersFilter for configuration of cache-headers
 *
 */
public class ResponseCacheHeadersFilter extends WebContentInterceptor implements Filter {

	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		 try {
             this.preHandle((HttpServletRequest) request, (HttpServletResponse) response, chain);
         } catch (Exception e) {
             throw new ServletException(e);
         }
         chain.doFilter(request, response);
	}

	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}


}
