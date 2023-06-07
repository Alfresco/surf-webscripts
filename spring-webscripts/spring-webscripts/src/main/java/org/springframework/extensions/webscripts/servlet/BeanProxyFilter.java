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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * An adapter from the servlet filter world into the Spring dependency injected world. Simply looks up a
 * {@link DependencyInjectedFilter} with a configured bean name and delegates the
 * {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} call to that. This allows us to swap in and out
 * different implementations for different 'hook points' in web.xml.
 * 
 * @author dward
 */
public class BeanProxyFilter implements Filter
{
    /**
     * Name of the init parameter that carries the proxied bean name 
     */
    private static final String INIT_PARAM_BEAN_NAME = "beanName";
    
    private DependencyInjectedFilter filter;
    private ServletContext context;    
    
    /**
     * Initialize the filter.
     * 
     * @param args  FilterConfig
     * @throws ServletException the servlet exception
     * @exception ServletException
     */
    public void init(FilterConfig args) throws ServletException
    {
        WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(args.getServletContext());
        this.filter = (DependencyInjectedFilter)ctx.getBean(args.getInitParameter(INIT_PARAM_BEAN_NAME));
        this.context = args.getServletContext();
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.Filter#destroy()
     */
    public void destroy()
    {
        this.filter = null;
    }

    /* (non-Javadoc)
     * @see jakarta.servlet.Filter#doFilter(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse, jakarta.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException
    {
        this.filter.doFilter(this.context, request, response, chain);
    }
}
