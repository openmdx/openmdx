/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openmdx.uses.org.apache.servicemix.http.jetty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.mortbay.jetty.nio.SelectChannelConnector;
import org.openmdx.uses.org.apache.servicemix.jbi.security.auth.AuthenticationService;
import org.openmdx.uses.org.apache.servicemix.jbi.security.keystore.KeystoreManager;

/**
 * 
 * @author gnodet
 * @org.apache.xbean.XBean element="configuration"
 */
public class HttpConfiguration {

    public static final String DEFAULT_JETTY_CONNECTOR_CLASS_NAME = SelectChannelConnector.class.getName();
    public static final String MAPPING_DEFAULT = "/jbi";
    public final static String CONFIG_FILE = "component.properties"; 
    
    private String rootDir;
    private String componentName = "servicemix-http";
    private Properties properties = new Properties();
    private boolean streamingEnabled = false;
    private String jettyConnectorClassName = DEFAULT_JETTY_CONNECTOR_CLASS_NAME;
    private transient KeystoreManager keystoreManager;
    private transient AuthenticationService authenticationService;
    
    /**
     * The JNDI name of the AuthenticationService object
     */
    private String authenticationServiceName = "java:comp/env/smx/AuthenticationService";
    
    /**
     * The JNDI name of the KeystoreManager object
     */
    private String keystoreManagerName = "java:comp/env/smx/KeystoreManager";

    /**
     * The maximum number of threads for the Jetty thread pool. It's set 
     * to 255 by default to match the default value in Jetty. 
     */
    private int jettyThreadPoolSize = 255;
    
    /**
     * Maximum number of concurrent requests to the same host.
     */
    private int maxConnectionsPerHost = 32;
    
    /**
     * Maximum number of concurrent requests.
     */
    private int maxTotalConnections = 256;
    
    /**
     * If true, use register jetty mbeans
     */
    private boolean jettyManagement;
    
    /**
     * If the component is deployed in a web container and uses
     * a servlet instead of starting its own web server.
     */
    private boolean managed = false;
    
    /**
     * When managed is true, this is the servlet mapping used
     * to access the component.
     */
    private transient String mapping = MAPPING_DEFAULT;

    /**
     * Jetty connector max idle time 
     * (default value in jetty is 30000msec)
     **/
    private int connectorMaxIdleTime = 30000;

    /**
     * HttpConsumerProcessor continuation suspend time
     * (default value in servicemix is 60000msec)
     */
    private int consumerProcessorSuspendTime = 60000;

    /**
     * Number of times a given HTTP request will be tried
     * until successful.  If streaming is enabled, the value
     * will always be 0.
     */
    private int retryCount = 3;
    
    /**
     * Proxy hostname. Component wide configuration, used either for http or https connections. Can be overriden on a endpoint basis.
     */
    private String proxyHost;
    
    /**
     * Proxy listening port. Component wide configuration, used either for http or https connections. Can be overriden on a endpoint basis. 
     */
    private int proxyPort;

    /**
     * @return Returns the rootDir.
     * @org.apache.xbean.Property hidden="true"
     */
    public String getRootDir() {
        return rootDir;
    }

    /**
     * @param rootDir The rootDir to set.
     */
    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    /**
     * @return Returns the componentName.
     * @org.apache.xbean.Property hidden="true"
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * @param componentName The componentName to set.
     */
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    /**
     * @return the mapping
     */
    public String getMapping() {
        return mapping;
    }

    /**
     * @param mapping the mapping to set
     */
    public void setMapping(String mapping) {
        if (!mapping.startsWith("/")) {
            mapping = "/" + mapping;
        }
        if (mapping.endsWith("/")) {
            mapping = mapping.substring(0, mapping.length() - 1);
        }
        this.mapping = mapping;
    }

    /**
     * @return the managed
     */
    public boolean isManaged() {
        return managed;
    }

    /**
     * @param managed the managed to set
     */
    public void setManaged(boolean managed) {
        this.managed = managed;
    }

    /**
     * @return the jettyManagement
     */
    public boolean isJettyManagement() {
        return jettyManagement;
    }

    /**
     * @param jettyManagement the jettyManagement to set
     */
    public void setJettyManagement(boolean jettyManagement) {
        this.jettyManagement = jettyManagement;
    }

    /**
     * @return the authenticationService
     */
    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    /**
     * @param authenticationService the authenticationService to set
     */
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * @return the authenticationServiceName
     */
    public String getAuthenticationServiceName() {
        return authenticationServiceName;
    }

    /**
     * @param authenticationServiceName the authenticationServiceName to set
     */
    public void setAuthenticationServiceName(String authenticationServiceName) {
        this.authenticationServiceName = authenticationServiceName;
        save();
    }

    /**
     * @return the keystoreManager
     */
    public KeystoreManager getKeystoreManager() {
        return keystoreManager;
    }

    /**
     * @param keystoreManager the keystoreManager to set
     */
    public void setKeystoreManager(KeystoreManager keystoreManager) {
        this.keystoreManager = keystoreManager;
    }

    /**
     * @return the keystoreManagerName
     */
    public String getKeystoreManagerName() {
        return keystoreManagerName;
    }

    /**
     * @param keystoreManagerName the keystoreManagerName to set
     */
    public void setKeystoreManagerName(String keystoreManagerName) {
        this.keystoreManagerName = keystoreManagerName;
        save();
    }

    public boolean isStreamingEnabled() {
        return streamingEnabled;
    }

    public void setStreamingEnabled(boolean streamingEnabled) {
        this.streamingEnabled = streamingEnabled;
        save();
    }

    public String getJettyConnectorClassName() {
        return jettyConnectorClassName;
    }

    public void setJettyConnectorClassName(String jettyConnectorClassName) {
        this.jettyConnectorClassName = jettyConnectorClassName;
        save();
    }

    public int getJettyThreadPoolSize() {
        return jettyThreadPoolSize;
    }

    public void setJettyThreadPoolSize(int jettyThreadPoolSize) {
        this.jettyThreadPoolSize = jettyThreadPoolSize;
        save();
    }
    
    public int getMaxConnectionsPerHost() {
        return maxConnectionsPerHost;
    }

    public void setMaxConnectionsPerHost(int maxConnectionsPerHost) {
        this.maxConnectionsPerHost = maxConnectionsPerHost;
        save();
    }

    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }

    public void setMaxTotalConnections(int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
        save();
    }

    public int getConnectorMaxIdleTime() {
        return connectorMaxIdleTime;
    }

    public void setConnectorMaxIdleTime(int connectorMaxIdleTime) {
        this.connectorMaxIdleTime = connectorMaxIdleTime;
        save();
    }

    public int getConsumerProcessorSuspendTime() {
        return consumerProcessorSuspendTime;
    }

    public void setConsumerProcessorSuspendTime(int consumerProcessorSuspendTime) {
        this.consumerProcessorSuspendTime = consumerProcessorSuspendTime;
        save();
    }

    /**
     * @return the retryCount
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * @param retryCount the retryCount to set
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        save();
    }

    /**
     * @return Returns the proxyHost.
     */
    public String getProxyHost() {
        return this.proxyHost;
    }

    /**
     * @param proxyHost The proxyHost to set.
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
        save();
    }

    /**
     * @return Returns the proxyPort.
     */
    public int getProxyPort() {
        return this.proxyPort;
    }

    /**
     * @param proxyPort The proxyPort to set.
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
        save();
    }
    
    public void save() {
        setProperty(componentName + ".jettyThreadPoolSize", Integer.toString(jettyThreadPoolSize));
        setProperty(componentName + ".jettyConnectorClassName", jettyConnectorClassName);
        setProperty(componentName + ".streamingEnabled", Boolean.toString(streamingEnabled));
        setProperty(componentName + ".maxConnectionsPerHost", Integer.toString(maxConnectionsPerHost));
        setProperty(componentName + ".maxTotalConnections", Integer.toString(maxTotalConnections));
        setProperty(componentName + ".keystoreManagerName", keystoreManagerName);
        setProperty(componentName + ".authenticationServiceName", authenticationServiceName);
        setProperty(componentName + ".jettyManagement", Boolean.toString(jettyManagement));
        setProperty(componentName + ".connectorMaxIdleTime", Integer.toString(connectorMaxIdleTime));
        setProperty(componentName + ".consumerProcessorSuspendTime", Integer.toString(consumerProcessorSuspendTime));
        setProperty(componentName + ".retryCount", Integer.toString(retryCount));
        setProperty(componentName + ".proxyHost", proxyHost);
        setProperty(componentName + ".proxyPort", Integer.toString(proxyPort));
        if (rootDir != null) {
            File f = new File(rootDir, CONFIG_FILE);
            try {
                this.properties.store(new FileOutputStream(f), null);
            } catch (Exception e) {
                throw new RuntimeException("Could not store component configuration", e);
            }
        }
    }
    
    protected void setProperty(String name, String value) {
        if (value == null) {
            properties.remove(name);
        } else {
            properties.setProperty(name, value);
        }
    }
    
    public boolean load() {
        File f = null;
        InputStream in = null;
        if (rootDir != null) {
            // try to find the property file in the workspace folder
            f = new File(rootDir, CONFIG_FILE);
            if (!f.exists()) {
                f = null;
            }
        }
        if (f == null) {
            // find property file in classpath if it is not available in workspace 
            in = this.getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (in == null) {
                return false;
            }
        }

        try {
            if (f != null) {
                properties.load(new FileInputStream(f));
            } else {
                properties.load(in);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load component configuration", e);
        }
        if (properties.getProperty(componentName + ".jettyThreadPoolSize") != null) {
            jettyThreadPoolSize = Integer.parseInt(properties.getProperty(componentName + ".jettyThreadPoolSize"));
        }
        if (properties.getProperty(componentName + ".jettyConnectorClassName") != null) {
            jettyConnectorClassName = properties.getProperty(componentName + ".jettyConnectorClassName");
        }
        if (properties.getProperty(componentName + ".streamingEnabled") != null) {
            streamingEnabled = Boolean.valueOf(properties.getProperty(componentName + ".streamingEnabled")).booleanValue();
        }
        if (properties.getProperty(componentName + ".maxConnectionsPerHost") != null) {
            maxConnectionsPerHost = Integer.parseInt(properties.getProperty(componentName + ".maxConnectionsPerHost"));
        }
        if (properties.getProperty(componentName + ".maxTotalConnections") != null) {
            maxTotalConnections = Integer.parseInt(properties.getProperty(componentName + ".maxTotalConnections"));
        }
        if (properties.getProperty(componentName + ".keystoreManagerName") != null) {
            keystoreManagerName = properties.getProperty(componentName + ".keystoreManagerName");
        }
        if (properties.getProperty(componentName + ".authenticationServiceName") != null) {
            authenticationServiceName = properties.getProperty(componentName + ".authenticationServiceName");
        }
        if (properties.getProperty(componentName + ".jettyManagement") != null) {
            jettyManagement = Boolean.valueOf(properties.getProperty(componentName + ".jettyManagement")).booleanValue();
        }
        if (properties.getProperty(componentName + ".connectorMaxIdleTime") != null) {
            connectorMaxIdleTime = Integer.parseInt(properties.getProperty(componentName + ".connectorMaxIdleTime"));
        }
        if (properties.getProperty(componentName + ".consumerProcessorSuspendTime") != null) {
            consumerProcessorSuspendTime = Integer.parseInt(properties.getProperty(componentName + ".consumerProcessorSuspendTime"));
        }
        if (properties.getProperty(componentName + ".retryCount") != null) {
            retryCount = Integer.parseInt(properties.getProperty(componentName + ".retryCount"));
        }
        if (properties.getProperty(componentName + ".proxyHost") != null) {
            proxyHost = properties.getProperty(componentName + ".proxyHost");
        }
        if (properties.getProperty(componentName + ".proxyPort") != null) {
            proxyPort = Integer.parseInt(properties.getProperty(componentName + ".proxyPort"));
        }
        return true;
    }

}
