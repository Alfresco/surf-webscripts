/**
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This file is part of the Spring Surf Extension project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.extensions.webscripts.servlet;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * A bean-like equivalent of a servlet filter, designed to be managed by a Spring container.
 * 
 * @see BeanProxyFilter
 * @author dward
 */
public interface DependencyInjectedFilter
{
    /**
     * The <code>doFilter</code> method of the Filter is called by the container each time a request/response pair is
     * passed through the chain due to a client request for a resource at the end of the chain. The FilterChain passed
     * in to this method allows the Filter to pass on the request and response to the next entity in the chain.
     * <p>
     * A typical implementation of this method would follow the following pattern:- <br>
     * 1. Examine the request<br>
     * 2. Optionally wrap the request object with a custom implementation to filter content or headers for input
     * filtering <br>
     * 3. Optionally wrap the response object with a custom implementation to filter content or headers for output
     * filtering <br>
     * 4. a) <strong>Either</strong> invoke the next entity in the chain using the FilterChain object (
     * <code>chain.doFilter()</code>), <br>
     * 4. b) <strong>or</strong> not pass on the request/response pair to the next entity in the filter chain to block
     * the request processing<br>
     * 5. Directly set headers on the response after invocation of the next entity in the filter chain.
     **/
    public void doFilter(ServletContext context, ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException;
}
