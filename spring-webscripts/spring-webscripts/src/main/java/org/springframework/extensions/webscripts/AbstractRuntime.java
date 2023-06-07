/**
 * Copyright (C) 2005-2022 Alfresco Software Limited.
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

package org.springframework.extensions.webscripts;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;


/**
 * Encapsulates the execution of a single Web Script.
 *
 * Sub-classes of WebScriptRuntime maintain the execution environment e.g. servlet
 * request & response.
 * 
 * A new instance of WebScriptRuntime is required for each invocation.
 * 
 * @author davidc
 */
public abstract class AbstractRuntime implements Runtime
{
    // Logger
    protected static final Log logger = LogFactory.getLog(AbstractRuntime.class);
    protected static final Log exceptionLogger = LogFactory.getLog(AbstractRuntime.class.getName() + ".exception");

    /** Component Dependencies */
    protected RuntimeContainer container;
    protected WebScriptSession session;

    /**
     * Construct
     * 
     * @param container  web script context
     */
    public AbstractRuntime(RuntimeContainer container)
    {
        this.container = container;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Runtime#getContainer()
     */
    public Container getContainer()
    {
        return container;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.Runtime#getSession()
     */
    public WebScriptSession getSession()
    {
        if (session == null)
        {
            session = new RuntimeSession(createSessionFactory());
        }
        return session;
    }
    
    private URLModelFactory urlModelFactory = null;
    
    public void setURLModelFactory(URLModelFactory urlModelFactory)
    {
        this.urlModelFactory = urlModelFactory;
    }
    
    private URLModel createURLModel(WebScriptRequest request)
    {
        URLModel urlModel = null;
        if (this.urlModelFactory == null)
        {
            urlModel = new DefaultURLModel(request);
        }
        else
        {
            urlModel = this.urlModelFactory.createURLModel(request);
        }
        return urlModel;
    }
    /**
     * Execute the Web Script encapsulated by this Web Script Runtime
     */
    final public void executeScript()
    {
        final boolean debug = logger.isDebugEnabled();
        long startRuntime = 0L;
        if (debug) startRuntime = System.nanoTime();

        final String method = getScriptMethod();
        String scriptUrl = null;
        Match match = null;

        try
        {
            // extract script url
            scriptUrl = getScriptUrl();
            if (scriptUrl == null || scriptUrl.length() == 0)
            {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Script URL not specified");
            }

            if (debug)
                logger.debug("(Runtime=" + getName() + ", Container=" + container.getName() + ") Processing script url ("  + method + ") " + scriptUrl);

            WebScriptRequest scriptReq = null;
            WebScriptResponse scriptRes = null;
            Authenticator auth = null;
            
            RequiredAuthentication containerRequiredAuth = container.getRequiredAuthentication();
            
            if (!containerRequiredAuth.equals(RequiredAuthentication.none))
            {
                // Create initial request & response
                scriptReq = createRequest(null);
                scriptRes = createResponse();
                auth = createAuthenticator();
                
                if (debug)
                    logger.debug("(Runtime=" + getName() + ", Container=" + container.getName() + ") Container requires pre-auth: "+containerRequiredAuth);
                
                boolean preAuth = true;
                
                if (auth != null && auth.emptyCredentials())
                {
                    // check default (unauthenticated) domain
                    match = container.getRegistry().findWebScript(method, scriptUrl);
                    if ((match != null) && (match.getWebScript().getDescription().getRequiredAuthentication().equals(RequiredAuthentication.none)))
                    {
                        preAuth = false;
                    }
                }
                
                if (preAuth && (!container.authenticate(auth, containerRequiredAuth)))
                {
                    return; // return response (eg. prompt for un/pw if status is 401 or redirect)
                }
            }
            
            if (match == null)
            {
                match = container.getRegistry().findWebScript(method, scriptUrl);
            }
            
            if (match == null || match.getKind() == Match.Kind.URI)
            {
                if (match == null)
                {
                    String msg = "Script url " + scriptUrl + " does not map to a Web Script.";
                    if (debug) logger.debug(msg);
                    throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, msg);
                }
                else
                {
                    String msg = "Script url " + scriptUrl + " does not support the method " + method;
                    if (debug) logger.debug(msg);
                    throw new WebScriptException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg);
                }
            }

            // create web script request & response
            scriptReq = createRequest(match);
            scriptRes = createResponse();
            
            if (auth == null)
            {
                // not pre-authenticated
                auth = createAuthenticator();
            }
            
            if (debug) logger.debug("Agent: " + scriptReq.getAgent());

            long startScript = System.nanoTime();
            final WebScript script = match.getWebScript();
            final Description description = script.getDescription();
            
            try
            {
                if (debug)
                {
                    String reqFormat = scriptReq.getFormat();
                    String format = (reqFormat == null || reqFormat.length() == 0) ? "[undefined]" : reqFormat;
                    Description desc = scriptReq.getServiceMatch().getWebScript().getDescription();
                    logger.debug("Invoking Web Script " + description.getId() + " (format " + format + ", style: " + desc.getFormatStyle() + ", default: " + desc.getDefaultFormat() + ")");
                }

                executeScript(scriptReq, scriptRes, auth);
            }
            finally
            {
                if (debug)
                {
                    long endScript = System.nanoTime();
                    logger.debug("Web Script " + description.getId() + " executed in " + (endScript - startScript)/1000000f + "ms");
                }
            }
        }
        catch (Throwable e)
        {
            if (beforeProcessError(match, e))
            {
                handleExecuteScriptsExceptions(debug, e);

                // setup context
                WebScriptRequest req = createRequest(match);
                WebScriptResponse res = createResponse();

                Cache cache = new Cache();
                cache.setNeverCache(true);
                res.setCache(cache);

                renderErrorResponse(match, e, req, res);

            }
        }
        finally
        {
            if (debug)
            {
                long endRuntime = System.nanoTime();
                logger.debug("Processed script url ("  + method + ") " + scriptUrl + " in " + (endRuntime - startRuntime)/1000000f + "ms");
            }
        }
    }

    private void handleExecuteScriptsExceptions(boolean debug, Throwable throwable)
    {
        final Map<Integer, String> handledExecuteScriptErrorCodes = Map.of(
                HttpServletResponse.SC_NOT_FOUND, "NOT FOUND",
                HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED",
                HttpServletResponse.SC_PRECONDITION_FAILED, "PRECONDITION FAILED");
        if (throwable instanceof WebScriptException && (handledExecuteScriptErrorCodes.containsKey(((WebScriptException) throwable).getStatus())))
        {
            String errorCode = handledExecuteScriptErrorCodes.get(((WebScriptException) throwable).getStatus());
            if (((WebScriptException) throwable).getStatus() != HttpServletResponse.SC_PRECONDITION_FAILED) // debug level output for "missing" WebScripts and API URLs entered incorrectly
            {
                if (debug)
                {
                    logger.debug("Webscript did not execute. (" + errorCode + "): " + throwable.getMessage());
                }
            }
            else // handle 412 webscript error code (ArchivedIOException) to lower log pollution
            {
                if (debug) // log with stack trace at debug level
                {
                    logger.debug("Precondition failed when executing webscript ", throwable);
                }
                else if (logger.isInfoEnabled()) // log without stack trace at info level
                {
                    logger.info("Precondition failed when executing webscript. Message: " + throwable.getMessage());
                }
            }
        }
        // log error on server so its not swallowed and lost
        else if (logger.isErrorEnabled())
        {
            logger.error("Exception from executeScript: " + throwable.getMessage(), throwable);
        }
    }

    /**
     * Renders an error message to the response based on the Throwable exception passed in.
     * @param match
     * @param exception
     * @param request
     * @param response
     */
    protected void renderErrorResponse(Match match, Throwable exception, WebScriptRequest request, WebScriptResponse response) {

        // extract status code, if specified
        int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        final boolean debug = logger.isDebugEnabled();
        StatusTemplate statusTemplate = null;
        Map<String, Object> statusModel = null;
        String format = request.getFormat();

        if (exception instanceof WebScriptException)
        {
            WebScriptException we = (WebScriptException) exception;
            statusCode = we.getStatus();
            statusTemplate = we.getStatusTemplate();
            statusModel = we.getStatusModel();
        }

        // retrieve status template for response rendering
        if (statusTemplate == null)
        {
            // locate status template
            // NOTE: search order...
            //   1) root located <status>.ftl
            //   2) root located <format>.status.ftl
            //   3) root located status.ftl
            statusTemplate = getStatusCodeTemplate(statusCode);

            String validTemplatePath = container.getTemplateProcessorRegistry().findValidTemplatePath(statusTemplate.getPath());
            if (validTemplatePath == null)
            {
                if (format != null && format.length() > 0)
                {
                    // if we have a format try and get the format specific status template
                    statusTemplate = getFormatStatusTemplate(format);
                    validTemplatePath = container.getTemplateProcessorRegistry().findValidTemplatePath(statusTemplate.getPath());
                }

                // if we don't have a valid template path get the default status template
                if (validTemplatePath == null)
                {
                    statusTemplate = getStatusTemplate();
                    validTemplatePath = container.getTemplateProcessorRegistry().findValidTemplatePath(statusTemplate.getPath());
                }

                // throw error if a status template could not be found
                if (validTemplatePath == null)
                {
                    throw new WebScriptException("Failed to find status template " + statusTemplate.getPath() + " (format: " + statusTemplate.getFormat() + ")");
                }
            }
        }

        // create basic model for all information known at this point, if one hasn't been pre-provided
        if (statusModel == null || statusModel.equals(Collections.EMPTY_MAP))
        {
            statusModel = new HashMap<String, Object>(8, 1.0f);
            statusModel.putAll(container.getTemplateParameters());
            statusModel.put("url", createURLModel(request));
            if (match != null && match.getWebScript() != null)
            {
                statusModel.put("webscript", match.getWebScript().getDescription());
            }
        }

        // add status to model
        Status status = new Status();
        status.setCode(statusCode);
        status.setMessage(exception.getMessage() != null ? exception.getMessage() : exception.toString());
        if (exceptionLogger.isDebugEnabled())
        {
            status.setException(exception);
        }
        statusModel.put("status", status);

        // render output
        String mimetype = container.getFormatRegistry().getMimeType(request.getAgent(), statusTemplate.getFormat());
        if (mimetype == null)
        {
            throw new WebScriptException("Web Script format '" + statusTemplate.getFormat() + "' is not registered");
        }

        if (debug)
        {
            logger.debug("Force success status header in response: " + request.forceSuccessStatus());
            logger.debug("Sending status " + statusCode + " (Template: " + statusTemplate.getPath() + ")");
            logger.debug("Rendering response: content type=" + mimetype);
        }

        response.setStatus(request.forceSuccessStatus() ? HttpServletResponse.SC_OK : statusCode);
        response.setContentType(mimetype + ";charset=UTF-8");

        try
        {
            String validTemplatePath = container.getTemplateProcessorRegistry().findValidTemplatePath(statusTemplate.getPath());
            TemplateProcessor statusProcessor = container.getTemplateProcessorRegistry().getTemplateProcessor(validTemplatePath);
            statusProcessor.process(validTemplatePath, statusModel, response.getWriter());
        }
        catch (Exception e1)
        {
            logger.error("Internal error", e1);
            throw new WebScriptException("Internal error", e1);
        }
    }

    /**
     * Before processing an error exception - hook point to allow additional processing
     * of the exception based on the runtime. This allows runtime to handle errors themselves
     * if required - for example silently ignoring missing webscripts. 
     * 
     * @param match WebScript that was processed and caused the error
     * @param e Exception that occured during webscript processing
     * 
     * @return true to continue default error processing, false to assume handling is complete
     */
    protected boolean beforeProcessError(Match match, Throwable e)
    {
        // default implementation simply continues default processing
        return true;
    }

    /**
     * Execute script given the specified context
     * 
     * @param scriptReq WebScriptRequest
     * @param scriptRes WebScriptResponse
     * @param auth Authenticator
     * 
     * @throws IOException
     */
    protected void executeScript(WebScriptRequest scriptReq, WebScriptResponse scriptRes, Authenticator auth)
        throws IOException
    {
        container.executeScript(scriptReq, scriptRes, auth);
    }

    /**
     * Get code specific Status Template path
     * 
     * @param statusCode int
     * @return  path
     */
    protected StatusTemplate getStatusCodeTemplate(int statusCode)
    {
        return new StatusTemplate("/" + statusCode + ".ftl", WebScriptResponse.HTML_FORMAT);
    }
    
    /**
     * Get format Status Template path
     * 
     * @param format String
     * @return  path
     */
    protected StatusTemplate getFormatStatusTemplate(String format)
    {
        return new StatusTemplate("/" + format + ".status.ftl", format);
    }
    
    /**
     * Get Status Template path
     * 
     * @return  path
     */
    protected StatusTemplate getStatusTemplate()
    {
        return new StatusTemplate("/status.ftl", WebScriptResponse.HTML_FORMAT);
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Runtime#getScriptParameters()
     */
    public Map<String, Object> getScriptParameters()
    {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("session", getSession());
        
        ScriptParameterFactoryRegistry registry = container.getScriptParameterFactoryRegistry();
        if (registry != null)
        {
            Collection<ScriptParameterFactory> factories = registry.getScriptParameterFactories();
            if (factories != null)
            {
                for (ScriptParameterFactory factory : factories)
                {
                    parameters.putAll(factory.getParameters(this));
                }
            }
        }
        
        return parameters;
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.Runtime#getTemplateParameters()
     */
    public Map<String, Object> getTemplateParameters()
    {
        return Collections.emptyMap();
    }
    
    /**
     * Get the Web Script Method  e.g. get, post
     * 
     * @return  web script method
     */
    protected abstract String getScriptMethod();

    /**
     * Get the Web Script Url
     * 
     * @return  web script url
     */
    protected abstract String getScriptUrl();
    
    /**
     * Create a Web Script Request
     * 
     * @param match  web script matching the script method and url
     * @return  web script request
     */
    protected abstract WebScriptRequest createRequest(Match match);
    
    /**
     * Create a Web Script Response
     * 
     * @return  web script response
     */
    protected abstract WebScriptResponse createResponse();
    
    /**
     * Create a Web Script Authenticator
     * 
     * @return  web script authenticator
     */
    protected abstract Authenticator createAuthenticator();
    
    /**
     * Create a Web Script Session
     */
    protected abstract WebScriptSessionFactory createSessionFactory();

    /**
     * Helper to retrieve real (last) Web Script Request in a stack of wrapped Web Script requests
     * 
     * @param request WebScriptRequest
     * @return WebScriptRequest
     */
    protected static WebScriptRequest getRealWebScriptRequest(WebScriptRequest request)
    {
        WebScriptRequest real = request;
        while(real instanceof WrappingWebScriptRequest)
        {
            real = ((WrappingWebScriptRequest)real).getNext();
        }
        return real;
    }

    /**
     * Helper to retrieve real (last) Web Script Response in a stack of wrapped Web Script responses
     * 
     * @param response WebScriptResponse
     * @return WebScriptResponse
     */
    protected static WebScriptResponse getRealWebScriptResponse(WebScriptResponse response)
    {
        WebScriptResponse real = response;
        while(real instanceof WrappingWebScriptResponse)
        {
            real = ((WrappingWebScriptResponse)real).getNext();
        }
        return real;
    }

    /**
     * Session whose values are namespaced
     */
    private static class RuntimeSession implements WebScriptSession
    {
        private static final WebScriptSession NOOP_WEBSCRIPTSESSION = new NOOPWebScriptSession();
        private WebScriptSessionFactory sessionFactory;
        private WebScriptSession session;
        
        public RuntimeSession(WebScriptSessionFactory sessionFactory)
        {
            this.sessionFactory = sessionFactory;
        }

        public String getId()
        {
            return getSession().getId();
        }
        
        public Object getValue(String name)
        {
            return getSession().getValue(name);
        }

        public void removeValue(String name)
        {
            getSession().removeValue(name);
        }

        public void setValue(String name, Object value)
        {
            getSession().setValue(name, value);
        }
        
        private WebScriptSession getSession()
        {
            if (session == null && sessionFactory != null)
            {
                session = sessionFactory.createSession();
            }
            if (session == null)
            {
                session = NOOP_WEBSCRIPTSESSION;
            }
            return session;
        }
        
        /**
         * No-op implementation of WebScriptSession
         */
        private static class NOOPWebScriptSession implements WebScriptSession
        {
            public String getId()
            {
                return null;
            }
            
            public Object getValue(String name)
            {
                return null;
            }

            public void removeValue(String name)
            {
            }

            public void setValue(String name, Object value)
            {
            }
        }
    }
    
}
