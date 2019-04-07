/**
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.springframework.extensions.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.dom4j.Element;
import org.springframework.extensions.config.element.ConfigElementAdapter;
import org.springframework.util.StringUtils;

/**
 * Describes the connection, authentication and endpoint properties stored
 * within the <remote> block of the current configuration.  This block
 * provides settings for creating and working with remote services.
 * 
 * @author muzquiano
 */
public class RemoteConfigElement extends ConfigElementAdapter implements RemoteConfigProperties
{
    private static final Log logger = LogFactory.getLog(RemoteConfigElement.class);

    private static final String REMOTE_SSL_CONFIG = "ssl-config";
    private static final String REMOTE_ENDPOINT = "endpoint";
    private static final String REMOTE_AUTHENTICATOR = "authenticator";
    private static final String REMOTE_CONNECTOR = "connector";
    private static final String CONFIG_ELEMENT_ID = "remote";

    protected SSLConfigDescriptor sslConfigDescriptor;
    protected HashMap<String, ConnectorDescriptor> connectors = null;
    protected HashMap<String, AuthenticatorDescriptor> authenticators = null;
    protected HashMap<String, EndpointDescriptor> endpoints = null;

    protected String defaultEndpointId;
    protected String defaultCredentialVaultProviderId;

    /**
     * Constructs a new Remote Config Element
     */
    public RemoteConfigElement()
    {
        super(CONFIG_ELEMENT_ID);

        connectors = new HashMap<String, ConnectorDescriptor>(10);
        authenticators = new HashMap<String, AuthenticatorDescriptor>(10);
        endpoints = new HashMap<String, EndpointDescriptor>(10);
    }
    
    /* (non-Javadoc)
     * @see org.springframework.extensions.surf.config.element.ConfigElementAdapter#combine(org.springframework.extensions.surf.config.ConfigElement)
     */
    public ConfigElement combine(ConfigElement element)
    {
        RemoteConfigElement configElement = (RemoteConfigElement) element;

        // new combined element
        RemoteConfigElement combinedElement = new RemoteConfigElement();

        // copy in our things
        combinedElement.connectors.putAll(this.connectors);
        combinedElement.authenticators.putAll(this.authenticators);
        combinedElement.endpoints.putAll(this.endpoints);

        // override with things from the merging object
        combinedElement.connectors.putAll(configElement.connectors);
        combinedElement.authenticators.putAll(configElement.authenticators);
        combinedElement.endpoints.putAll(configElement.endpoints);
        
        // default endpoint id
        combinedElement.defaultEndpointId = this.defaultEndpointId;
        if(configElement.defaultEndpointId != null)
        {
            combinedElement.defaultEndpointId = configElement.defaultEndpointId;
        }

        // default credential vault provider id
        combinedElement.defaultCredentialVaultProviderId = this.defaultCredentialVaultProviderId;
        if(configElement.defaultCredentialVaultProviderId != null)
        {
            combinedElement.defaultCredentialVaultProviderId = configElement.defaultCredentialVaultProviderId;
        }

        // SSL KeyStore configuration
        combinedElement.sslConfigDescriptor = this.sslConfigDescriptor;
        if(configElement.sslConfigDescriptor != null)
        {
           combinedElement.sslConfigDescriptor = configElement.sslConfigDescriptor;
        }

        // return the combined element
        return combinedElement;
    }

    // remote connectors
    public String[] getConnectorIds()
    {
        return this.connectors.keySet().toArray(new String[this.connectors.size()]);
    }

    public ConnectorDescriptor getConnectorDescriptor(String id)
    {
        return (ConnectorDescriptor) this.connectors.get(id);
    }

    // remote authenticators
    public String[] getAuthenticatorIds()
    {
        return this.authenticators.keySet().toArray(new String[this.authenticators.size()]);
    }

    public AuthenticatorDescriptor getAuthenticatorDescriptor(String id)
    {
        return (AuthenticatorDescriptor) this.authenticators.get(id);
    }

    // remote endpoints
    public String[] getEndpointIds()
    {
        return this.endpoints.keySet().toArray(new String[this.endpoints.size()]);
    }

    public EndpointDescriptor getEndpointDescriptor(String id)
    {
        return (EndpointDescriptor) this.endpoints.get(id);
    }

    // defaults
    public String getDefaultEndpointId()
    {
        if(defaultEndpointId == null)
        {
            return "alfresco";
        }
        return defaultEndpointId;
    }

    public String getDefaultCredentialVaultProviderId()
    {
        if(defaultCredentialVaultProviderId == null)
        {
            return "credential.vault.provider";
        }
        return defaultCredentialVaultProviderId;
    }

    @Override
    public SSLConfigDescriptor getSSLConfigDescriptor()
    {
        return this.sslConfigDescriptor;
    }

    /**
     * EndPoint Descriptor class
     */
    public static class Descriptor implements Serializable
    {
        private static final String ID = "id";

        protected HashMap<String, Object> map = new HashMap<String, Object>();

        Descriptor(Element el)
        {
            List elements = el.elements();
            for(int i = 0; i < elements.size(); i++)
            {
                Element element = (Element) elements.get(i);
                put(element);
            }
        }

        public void put(Element el)
        {
            String key = el.getName();
            Object value = (Object) el.getTextTrim();
            if(value != null)
            {
                this.map.put(key, value);
            }
        }

        public Object get(String key)
        {
            return (Object) this.map.get(key);
        }

        public String getId() 
        {
            return (String) get(ID);
        }

        public Object getProperty(String key)
        {
            return get(key);
        }

        public String getStringProperty(String key)
        {
            return (String) get(key);
        }

        @Override
        public String toString()
        {
            // TODO Auto-generated method stub
            return map.toString();
        }
    }

    public static class SSLConfigDescriptor extends Descriptor
    {
        // Keystore elements
        private static final String KEYSTORE_PATH = "keystore-path";
        private static final String KEYSTORE_TYPE = "keystore-type";
        private static final String KEYSTORE_PASSWORD = "keystore-password";
        // Truststore elements
        private static final String TRUSTSTORE_PATH = "truststore-path";
        private static final String TRUSTSTORE_TYPE = "truststore-type";
        private static final String TRUSTSTORE_PASSWORD = "truststore-password";
        // verify host name element
        private static final String VERIFY_HOSTNAME = "verify-hostname";

        private Registry<ConnectionSocketFactory> socketFactoryRegistry;

        /**
         * Initializes SSL client keystore and truststore configuration, if appropriate.
         * 
         * @param elem the element
         */
        SSLConfigDescriptor(Element el)
        {
            super(el);

            KeyStore keyStore = loadKeyStore(KEYSTORE_PATH, KEYSTORE_TYPE, KEYSTORE_PASSWORD);
            KeyStore trustStore = loadKeyStore(TRUSTSTORE_PATH, TRUSTSTORE_TYPE, TRUSTSTORE_PASSWORD);

            if (keyStore == null && trustStore == null)
            {
                logger.warn("Custom SSL socket factory was not configured, as there was no Keystore or Truststore.");
                return;
            }

            try
            {
                String verifyHostStr = getStringProperty(VERIFY_HOSTNAME);
                // default is 'true', if the verify-hostname element hasn't been defined.
                boolean verifyHostname = StringUtils.isEmpty(verifyHostStr) ? true : Boolean.valueOf(verifyHostStr);
                if (verifyHostname)
                {
                    logger.info("Creating custom SSL socket factory with hostname verification enabled.");
                }
                else
                {
                    logger.warn("Creating custom SSL socket factory with hostname verification disabled.");
                }

                HostnameVerifier hostnameVerifier = verifyHostname
                            ? SSLConnectionSocketFactory.getDefaultHostnameVerifier() : NoopHostnameVerifier.INSTANCE;

                SSLContextBuilder sslContextBuilder = SSLContexts.custom();
                if (keyStore != null)
                {
                    sslContextBuilder.loadKeyMaterial(keyStore, getPassword(KEYSTORE_PASSWORD));
                }
                if (trustStore != null)
                {
                    sslContextBuilder.loadTrustMaterial(trustStore, null);
                }

                SSLContext sslContext = sslContextBuilder.useProtocol("TLS").build();
                SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);

                this.socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
                            .register("https", socketFactory)
                            .register("http", PlainConnectionSocketFactory.INSTANCE)
                            .build();

            }
            catch (Exception ex)
            {
                logger.error(ex);
            }
        }

        private KeyStore loadKeyStore(String pathElmName, String typeElmName, String passwordElmName)
        {
            String path = getStringProperty(pathElmName);
            if (StringUtils.isEmpty(path))
            {
                String storeKind = (TRUSTSTORE_PATH.equals(pathElmName)) ? "Truststore" : "Keystore";
                logger.warn("No SSL " + storeKind + " was configured.");
                return null;
            }

            char[] password = getPassword(passwordElmName);
            if (password == null)
            {
                logger.warn("No SSL key store password was provided. Attempt to load a key store without a password.");
            }

            try (InputStream keyStoreIn = new FileInputStream(new File(path)))
            {
                KeyStore keyStore = KeyStore.getInstance(getStringProperty(typeElmName));
                keyStore.load(keyStoreIn, password);

                return keyStore;
            }
            catch (Exception error)
            {
                logger.error(error);
                return null;
            }
        }

        private char[] getPassword(String passwordElmName)
        {
            String passwordStr = getStringProperty(passwordElmName);
            return (StringUtils.isEmpty(passwordStr)) ? null : passwordStr.toCharArray();
        }

        public Registry<ConnectionSocketFactory> getSocketFactoryRegistry()
        {
            return this.socketFactoryRegistry;
        }
    }
    /**
     * The Class ConnectorDescriptor.
     */
    public static class ConnectorDescriptor extends Descriptor
    {
        private static final String CLAZZ = "class";
        private static final String DESCRIPTION = "description";
        private static final String NAME = "name";
        private static final String AUTHENTICATOR_ID = "authenticator-id";
        private static final String UNAUTHENTICATED_MODE = "unauthenticated-mode";
        private static final String RECONNECT_TIMEOUT = "reconnect-timeout";

        /**
         * Instantiates a new remote connector descriptor.
         * 
         * @param el the elem
         */
        ConnectorDescriptor(Element el)
        {
            super(el);
        }

        public String getImplementationClass() 
        {
            return getStringProperty(CLAZZ);
        }
        
        public String getDescription() 
        {
            return getStringProperty(DESCRIPTION);
        }
        
        public String getName() 
        {
            return getStringProperty(NAME);
        } 
        
        public String getAuthenticatorId()
        {
            return getStringProperty(AUTHENTICATOR_ID);
        }
        
        public String getUnauthenticatedMode()
        {
            return getStringProperty(UNAUTHENTICATED_MODE);
        }
        
        public String getReconnectTimeout()
        {
            return getStringProperty(RECONNECT_TIMEOUT);
        }
    }

    /**
     * The Class AuthenticatorDescriptor.
     */
    public static class AuthenticatorDescriptor extends Descriptor
    {
        private static final String CLAZZ = "class";
        private static final String DESCRIPTION = "description";
        private static final String NAME = "name";

        /**
         * Instantiates a new remote authenticator descriptor.
         * 
         * @param el the elem
         */
        AuthenticatorDescriptor(Element el)
        {
            super(el);
        }

        public String getImplementationClass() 
        {
            return getStringProperty(CLAZZ);
        }
        public String getDescription() 
        {
            return getStringProperty(DESCRIPTION);
        }
        public String getName() 
        {
            return getStringProperty(NAME);
        }
    }

    /**
     * The Class EndpointDescriptor.
     */
    public static class EndpointDescriptor extends Descriptor
    {
        private static final String PASSWORD = "password";
        private static final String USERNAME = "username";
        private static final String IDENTITY = "identity";
        private static final String ENDPOINT_URL = "endpoint-url";
        private static final String AUTH_ID = "auth-id";
        private static final String CONNECTOR_ID = "connector-id";
        private static final String DESCRIPTION = "description";
        private static final String NAME = "name";
        private static final String UNSECURE = "unsecure";
        private static final String BASIC_AUTH = "basic-auth";
        private static final String EXTERNAL_AUTH = "external-auth";
        private static final String PARENT_ID = "parent-id";

        /**
         * Instantiates a new remote endpoint descriptor.
         * 
         * @param el the elem
         */
        EndpointDescriptor(Element el)
        {
            super(el);
        }

        public String getDescription() 
        {
            return getStringProperty(DESCRIPTION);
        }

        public String getName() 
        {
            return getStringProperty(NAME);
        }    

        public String getConnectorId() 
        {
            return getStringProperty(CONNECTOR_ID);
        }

        public String getAuthId()
        {
            return getStringProperty(AUTH_ID);
        }

        public String getEndpointUrl()
        {
            return getStringProperty(ENDPOINT_URL);
        }

        public IdentityType getIdentity()
        {
            IdentityType identityType = IdentityType.NONE;
            String identity = getStringProperty(IDENTITY);
            if (identity != null)
            {
                identityType = IdentityType.valueOf(identity.toUpperCase());
            }
            return identityType;
        }

        public String getUsername()
        {
            return getStringProperty(USERNAME);
        }

        public String getPassword()
        {
            return getStringProperty(PASSWORD);
        }
        
        public boolean getUnsecure()
        {
            return Boolean.parseBoolean(getStringProperty(UNSECURE));
        }
        
        public boolean getBasicAuth()
        {
            return Boolean.parseBoolean(getStringProperty(BASIC_AUTH));
        }
        
        public boolean getExternalAuth()
        {
            return Boolean.parseBoolean(getStringProperty(EXTERNAL_AUTH));
        }
        
        public String getParentId()
        {
            return getStringProperty(PARENT_ID);
        }
    }

    /**
     * New instance.
     * 
     * @param elem the elem
     * 
     * @return the remote config element
     */
    protected static RemoteConfigElement newInstance(Element elem)
    {
        RemoteConfigElement configElement = new RemoteConfigElement();

        // connectors
        List connectors = elem.elements(REMOTE_CONNECTOR);
        for(int i = 0; i < connectors.size(); i++)
        {
            Element el = (Element) connectors.get(i);
            ConnectorDescriptor descriptor = new ConnectorDescriptor(el);
            configElement.connectors.put(descriptor.getId(), descriptor);
        }

        // authenticators
        List authenticators = elem.elements(REMOTE_AUTHENTICATOR);
        for(int i = 0; i < authenticators.size(); i++)
        {
            Element el = (Element) authenticators.get(i);
            AuthenticatorDescriptor descriptor = new AuthenticatorDescriptor(el);
            configElement.authenticators.put(descriptor.getId(), descriptor);
        }

        // endpoints
        List endpoints = elem.elements(REMOTE_ENDPOINT);
        for(int i = 0; i < endpoints.size(); i++)
        {
            Element el = (Element) endpoints.get(i);
            EndpointDescriptor descriptor = new EndpointDescriptor(el);
            configElement.endpoints.put(descriptor.getId(), descriptor);
        }

        String _defaultEndpointId = elem.elementTextTrim("default-endpoint-id");
        if(_defaultEndpointId != null && _defaultEndpointId.length() > 0)
        {
            configElement.defaultEndpointId = _defaultEndpointId;
        }

        String _defaultCredentialVaultProviderId = elem.elementTextTrim("default-credential-vault-provider-id");
        if(_defaultCredentialVaultProviderId != null && _defaultCredentialVaultProviderId.length() > 0)
        {
            configElement.defaultCredentialVaultProviderId = _defaultCredentialVaultProviderId;
        }

        Element remoteSSLConfig = elem.element(REMOTE_SSL_CONFIG);
        if (remoteSSLConfig != null)
        {
            configElement.sslConfigDescriptor = new SSLConfigDescriptor(remoteSSLConfig);
        }

        return configElement;
    }
    
    
    /**
     * Enum describing the Identity Type for an Endpoint
     */
    public enum IdentityType
    {
        DECLARED, USER, NONE;
    }
}
