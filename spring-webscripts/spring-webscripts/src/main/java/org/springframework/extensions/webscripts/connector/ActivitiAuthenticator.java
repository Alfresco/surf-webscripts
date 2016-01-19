/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.springframework.extensions.webscripts.connector;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.exception.AuthenticationException;
import org.springframework.extensions.surf.util.URLEncoder;

/**
 * Activiti API endpoint authenticator.
 * <p>
 * Used to connect to <pre>https://activiti.alfresco.com/activiti-app/app</pre> endpoint.
 * <p>
 * This connector will post a URL encoded form with the login credentials to:
 * <pre>https://activiti.alfresco.com/activiti-app/app/authentication</pre>
 * content type: application/x-www-form-urlencoded
 * <p>
 * Response should be a 200 OK with no body content and ACTIVITI_REMEMBER_ME Cookie header set.
 * 
 * @author Kevin Roast
 */
public class ActivitiAuthenticator extends AbstractAuthenticator
{
    private static Log logger = LogFactory.getLog(ActivitiAuthenticator.class);
    
    private static final String API_LOGIN = "/authentication";
    
    private static final String FORM_LOGIN = "j_username={0}&j_password={1}&_spring_security_remember_me=true&submit=Login";
    
    private static final String MIMETYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
    
    
    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.connector.Authenticator#authenticate(java.lang.String, org.springframework.extensions.webscripts.connector.Credentials, org.springframework.extensions.webscripts.connector.ConnectorSession)
     */
    @Override
    public ConnectorSession authenticate(String endpoint, Credentials credentials, ConnectorSession connectorSession)
            throws AuthenticationException
    {
        ConnectorSession cs = null;
        
        String user, pass;
        if (credentials != null &&
            (user = (String)credentials.getProperty(Credentials.CREDENTIAL_USERNAME)) != null &&
            (pass = (String)credentials.getProperty(Credentials.CREDENTIAL_PASSWORD)) != null)
        {
            // build a new remote client
            final RemoteClient remoteClient = buildRemoteClient(endpoint);
            
            if (logger.isDebugEnabled())
                logger.debug("Authenticating Activiti user: " + user);
            
            // POST to the login API
            remoteClient.setRequestContentType(MIMETYPE_FORM_URLENCODED);
            final String body = MessageFormat.format(FORM_LOGIN, URLEncoder.encode(user), URLEncoder.encode(pass));
            final Response response = remoteClient.call(API_LOGIN, body);
            
            // read back the response
            if (response.getStatus().getCode() == 200)
            {
                if (logger.isDebugEnabled())
                    logger.debug("200 status received - storing auth cookie...");
                
                // The login creates an empty response, with cookies in the response headers.
                processResponse(response, connectorSession);
                cs = connectorSession;
            }
            else
            {
                if (logger.isDebugEnabled())
                    logger.debug("Authentication failed, received response code: " + response.getStatus().getCode());            
            }
        }
        else if (logger.isDebugEnabled())
        {
            logger.debug("No user credentials available - cannot authenticate.");
        }
        
        return cs;
    }

    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.connector.Authenticator#isAuthenticated(java.lang.String, org.springframework.extensions.webscripts.connector.ConnectorSession)
     */
    @Override
    public boolean isAuthenticated(String endpoint, ConnectorSession connectorSession)
    {
        return (connectorSession.getCookieNames().length != 0);
    }
}
