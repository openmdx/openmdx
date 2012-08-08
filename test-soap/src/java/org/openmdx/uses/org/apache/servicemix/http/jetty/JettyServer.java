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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;
import org.mortbay.management.MBeanContainer;
import org.mortbay.thread.BoundedThreadPool;
import org.mortbay.thread.ThreadPool;
import org.mortbay.util.ByteArrayISO8859Writer;
import org.mortbay.util.LazyList;
import org.mortbay.util.StringUtil;

public class JettyServer {

    //-----------------------------------------------------------------------
    /**
     * @return the mbeanServer
     */
    public MBeanServer getMBeanServer() {
        return mbeanServer;
    }

    //-----------------------------------------------------------------------
    /**
     * @param mbeanServer the mbeanServer to set
     */
    public void setMBeanServer(MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
    }

    //-----------------------------------------------------------------------
    public void init() throws Exception {
        if (this.configuration == null) {
            this.configuration = new HttpConfiguration();
        }
        if (this.mbeanServer != null && !configuration.isManaged() && configuration.isJettyManagement()) {
            this.mbeanContainer = new MBeanContainer(mbeanServer);
        }
        this.servers = new HashMap();
        this.sslParams = new HashMap();
        BoundedThreadPool btp = new BoundedThreadPool();
        btp.setMaxThreads(this.configuration.getJettyThreadPoolSize());
        this.threadPool = btp;
    }

    //-----------------------------------------------------------------------
    public void shutDown() throws Exception {
        stop();
    }

    //-----------------------------------------------------------------------
    public void start(
    ) throws Exception {
        threadPool.start();
        for (Iterator it = servers.values().iterator(); it.hasNext();) {
            Server server = (Server) it.next();
            server.start();
        }
    }

    //-----------------------------------------------------------------------
    public void stop(
    ) throws Exception {
        for (Iterator it = servers.values().iterator(); it.hasNext();) {
            Server server = (Server) it.next();
            server.stop();
        }
        for (Iterator it = servers.values().iterator(); it.hasNext();) {
            Server server = (Server) it.next();
            server.join();
            Connector[] connectors = server.getConnectors();
            for (int i = 0; i < connectors.length; i++) {
                if (connectors[i] instanceof AbstractConnector) {
                    ((AbstractConnector) connectors[i]).join();
                }
            }
        }
        threadPool.stop();
    }
    
    //-----------------------------------------------------------------------
    public synchronized Object createContext(
        String strUrl,
        String servletClassName,
        SslParameters sslParameters,
        String authMethod,
        UserRealm userRealm
    ) throws Exception {
        URL url = new URL(strUrl);
        Server server = getServer(url);
        if (server == null) {
            server = createServer(url, sslParameters);
        } else {
            // Check ssl params
            SslParameters ssl = (SslParameters) sslParams.get(getKey(url));
            if (ssl != null && !ssl.equals(sslParameters)) {
                throw new Exception("An https server is already created on port " + url.getPort() + " but SSL parameters do not match");
            }
        }
        String path = url.getPath();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String pathSlash = path + "/";
        // Check that context does not exist yet
        HandlerCollection handlerCollection = (HandlerCollection) server.getHandler();
        ContextHandlerCollection contexts = (ContextHandlerCollection) handlerCollection.getHandlers()[0];
        Handler[] handlers = contexts.getHandlers();
        if (handlers != null) {
            for (int i = 0; i < handlers.length; i++) {
                if (handlers[i] instanceof ContextHandler) {
                    ContextHandler h = (ContextHandler) handlers[i];
                    String handlerPath = h.getContextPath() + "/";
                    if (handlerPath.startsWith(pathSlash) ||
                        pathSlash.startsWith(handlerPath)) {
                        throw new Exception("The requested context for path '" + path + "' overlaps with an existing context for path: '" + h.getContextPath() + "'");
                    }
                }
            }
        }
        // Create context
        ContextHandler context = new ContextHandler();
        context.setContextPath(path);
        ServletHolder holder = new ServletHolder();
        holder.setName("jbiServlet");
        holder.setClassName(
            servletClassName == null
                ? HttpBridgeServlet.class.getName()
                : servletClassName
        );
        ServletHandler handler = new ServletHandler();
        handler.setServlets(new ServletHolder[] { holder });
        ServletMapping mapping = new ServletMapping();
        mapping.setServletName("jbiServlet");
        mapping.setPathSpec("/*");
        handler.setServletMappings(new ServletMapping[] { mapping });
        if (authMethod != null) {
            SecurityHandler secHandler = new SecurityHandler();
            ConstraintMapping constraintMapping = new ConstraintMapping();
            Constraint constraint = new Constraint();
            constraint.setAuthenticate(true);
            constraint.setRoles(new String[] { "*" });
            constraintMapping.setConstraint(constraint);
            constraintMapping.setPathSpec("/");
            secHandler.setConstraintMappings(new ConstraintMapping[] { constraintMapping });
            secHandler.setHandler(handler);
            secHandler.setAuthMethod(authMethod);
            secHandler.setUserRealm(userRealm);
            context.setHandler(secHandler);
        } else {
            context.setHandler(handler);
        }
        // add context
        contexts.addHandler(context);
        handler.initialize();
        context.start();
        return context;
    }
    
    //-----------------------------------------------------------------------
    public synchronized void remove(
        Object context
    ) throws Exception {
        ((ContextHandler) context).stop();
        for (Iterator it = servers.values().iterator(); it.hasNext();) {
            Server server = (Server) it.next();
            HandlerCollection handlerCollection = (HandlerCollection) server.getHandler();
            ContextHandlerCollection contexts = (ContextHandlerCollection) handlerCollection.getHandlers()[0];
            Handler[] handlers = contexts.getHandlers();
            if (handlers != null && handlers.length > 0) {
                contexts.setHandlers((Handler[])LazyList.removeFromArray(handlers, context));
            }
        }
    }

    //-----------------------------------------------------------------------
    protected Server getServer(
        URL url
    ) {
        Server server = (Server) servers.get(getKey(url));
        return server;
    }
    
    //-----------------------------------------------------------------------
    protected String getKey(
        URL url
    ) {
        String host = url.getHost();
        try {
            InetAddress addr = InetAddress.getByName(host);
            if (addr.isAnyLocalAddress()) {
                host = InetAddress.getLocalHost().getHostName();
            }
        } catch (UnknownHostException e) {
        }
        String key = url.getProtocol() + "://" + host + ":" + url.getPort();
        return key;
    }
    
    //-----------------------------------------------------------------------
    protected Server createServer(
        URL url, 
        SslParameters ssl
    ) throws Exception {
        boolean isSsl = false;
        if (url.getProtocol().equals("https")) {
            // TODO: put ssl default information on HttpConfiguration
            if (ssl == null) {
                throw new IllegalArgumentException("https protocol required but no ssl parameters found");
            }
            isSsl = true;
        } else if (!url.getProtocol().equals("http")) {
            throw new UnsupportedOperationException("Protocol " + url.getProtocol() + " is not supported");
        }
        // Create a new server
        Connector connector;
        if (isSsl && ssl.isManaged()) {
            String keyStore = ssl.getKeyStore();
            if (keyStore == null) {
                throw new IllegalArgumentException("keyStore must be set");
            }
            JettySslSocketConnector sslConnector = new JettySslSocketConnector();
            sslConnector.setSslKeyManagerFactoryAlgorithm(ssl.getKeyManagerFactoryAlgorithm());
            sslConnector.setSslTrustManagerFactoryAlgorithm(ssl.getTrustManagerFactoryAlgorithm());
            sslConnector.setProtocol(ssl.getProtocol());
            sslConnector.setConfidentialPort(url.getPort());
            sslConnector.setKeystore(keyStore);
            sslConnector.setKeyAlias(ssl.getKeyAlias());
            sslConnector.setNeedClientAuth(ssl.isNeedClientAuth());
            sslConnector.setWantClientAuth(ssl.isWantClientAuth());
            sslConnector.setKeystoreManager(getConfiguration().getKeystoreManager());
            // important to set this values for selfsigned keys 
            // otherwise the standard truststore of the jre is used
            sslConnector.setTruststore(ssl.getTrustStore());
            if (ssl.getTrustStorePassword() != null) {
                // check is necessary because if a null password is set
                // jetty would ask for a password on the comandline
                sslConnector.setTrustPassword(ssl.getTrustStorePassword());
            }
            sslConnector.setTruststoreType(ssl.getTrustStoreType());
            connector = sslConnector;
        } else if (isSsl) {
            String keyStore = ssl.getKeyStore();
            if (keyStore == null) {
                keyStore = System.getProperty("javax.net.ssl.keyStore", "");
                if (keyStore == null) {
                    throw new IllegalArgumentException("keyStore or system property javax.net.ssl.keyStore must be set");
                }
            }
//            if (keyStore.startsWith("classpath:")) {
//                try {
//                    String res = keyStore.substring(10);
//                    URL resurl = new ClassPathResource(res).getURL();
//                    keyStore = resurl.toString();
//                } catch (IOException e) {
//                    throw new JBIException("Unable to find keystore " + keyStore, e);
//                }
//            }
            String keyStorePassword = ssl.getKeyStorePassword();
            if (keyStorePassword == null) {
                keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
                if (keyStorePassword == null) {
                    throw new IllegalArgumentException("keyStorePassword or system property javax.net.ssl.keyStorePassword must be set");
                }
            }
            SslSocketConnector sslConnector = new SslSocketConnector();
            sslConnector.setSslKeyManagerFactoryAlgorithm(ssl.getKeyManagerFactoryAlgorithm());
            sslConnector.setSslTrustManagerFactoryAlgorithm(ssl.getTrustManagerFactoryAlgorithm());
            sslConnector.setProtocol(ssl.getProtocol());
            sslConnector.setConfidentialPort(url.getPort());
            sslConnector.setPassword(ssl.getKeyStorePassword());
            sslConnector.setKeyPassword(ssl.getKeyPassword() != null ? ssl.getKeyPassword() : keyStorePassword);
            sslConnector.setKeystore(keyStore);
            sslConnector.setKeystoreType(ssl.getKeyStoreType());
            sslConnector.setNeedClientAuth(ssl.isNeedClientAuth());
            sslConnector.setWantClientAuth(ssl.isWantClientAuth());
            connector = sslConnector;
        } else {
            String connectorClassName = configuration.getJettyConnectorClassName();
            try {
                connector = (Connector) Class.forName(connectorClassName).newInstance();
            } catch (Exception e) {
                logger.warn("Could not create a jetty connector of class '" + connectorClassName + "'. Defaulting to " + HttpConfiguration.DEFAULT_JETTY_CONNECTOR_CLASS_NAME);
                if (logger.isDebugEnabled()) {
                    logger.debug("Reason: " + e.getMessage(), e);
                }
                connector = (Connector) Class.forName(HttpConfiguration.DEFAULT_JETTY_CONNECTOR_CLASS_NAME).newInstance();
            }
        }
        connector.setHost(url.getHost());
        connector.setPort(url.getPort());
        connector.setMaxIdleTime(this.configuration.getConnectorMaxIdleTime());
        Server server = new Server();
        server.setThreadPool(new ThreadPoolWrapper());
        server.setConnectors(new Connector[] { connector });
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] { contexts, new DisplayServiceHandler() });
        server.setHandler(handlers);
        server.start();
        servers.put(getKey(url), server);
        sslParams.put(getKey(url), isSsl ? ssl : null);
        if (mbeanContainer != null) {
            server.getContainer().addEventListener(mbeanContainer);
        }
        return server;
    }

    //-----------------------------------------------------------------------
    public HttpConfiguration getConfiguration(
    ) {
        return configuration;
    }

    //-----------------------------------------------------------------------
    public void setConfiguration(
        HttpConfiguration configuration
    ) {
        this.configuration = configuration;
    }

    public ThreadPool getThreadPool() {
        return threadPool;
    }
    
    protected class DisplayServiceHandler extends AbstractHandler {

        public void handle(
            String target, 
            HttpServletRequest request, 
            HttpServletResponse response, 
            int dispatch
        ) throws IOException, ServletException {
            if (response.isCommitted() || HttpConnection.getCurrentConnection().getRequest().isHandled())
                return;
            
            String method = request.getMethod();
            
            if (!method.equals(HttpMethods.GET) || !request.getRequestURI().equals("/")) {
                response.sendError(404);
                return;   
            }

            response.setStatus(404);
            response.setContentType(MimeTypes.TEXT_HTML);
            
            ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer(1500);

            String uri = request.getRequestURI();
            uri = StringUtil.replace(uri, "<", "&lt;");
            uri = StringUtil.replace(uri, ">", "&gt;");
            
            writer.write("<HTML>\n<HEAD>\n<TITLE>Error 404 - Not Found");
            writer.write("</TITLE>\n<BODY>\n<H2>Error 404 - Not Found.</H2>\n");
            writer.write("No service matched or handled this request.<BR>");
            writer.write("Known services are: <ul>");

            Set servers = JettyServer.this.servers.keySet();
            for (Iterator iter = servers.iterator(); iter.hasNext();) {
                String serverUri = (String) iter.next();
                Server server = (Server) JettyServer.this.servers.get(serverUri);
                Handler[] handlers = server.getChildHandlersByClass(ContextHandler.class);
                for (int i = 0; handlers != null && i < handlers.length; i++) {
                    if (!(handlers[i] instanceof ContextHandler)) {
                        continue;
                    }
                    ContextHandler context = (ContextHandler) handlers[i];
                    if (context.isStarted()) {
                        writer.write("<li><a href=\"");
                        writer.write(serverUri);
                        if (!context.getContextPath().startsWith("/")) {
                            writer.write("/");
                        }
                        writer.write(context.getContextPath());
                        if (!context.getContextPath().endsWith("/")) {
                            writer.write("/");
                        }
                        writer.write("?wsdl\">");
                        writer.write(serverUri);
                        writer.write(context.getContextPath());
                        writer.write("</a></li>\n");
                    } else {
                        writer.write("<li>");
                        writer.write(serverUri);
                        writer.write(context.getContextPath());
                        writer.write(" [Stopped]</li>\n");
                    }
                }
            }
            
            for (int i=0; i < 10; i++) {
                writer.write("\n<!-- Padding for IE                  -->");
            }
            
            writer.write("\n</BODY>\n</HTML>\n");
            writer.flush();
            response.setContentLength(writer.size());
            OutputStream out = response.getOutputStream();
            writer.writeTo(out);
            out.close();
        }
        
    }
    
    //-----------------------------------------------------------------------
    protected class ThreadPoolWrapper 
        extends AbstractLifeCycle 
        implements ThreadPool {

        public boolean dispatch(Runnable job) {
            if (logger.isDebugEnabled()) {
                logger.debug("Dispatching job: " + job);
            }
            return threadPool.dispatch(job);
        }

        public int getIdleThreads() {
            return threadPool.getIdleThreads();
        }

        public int getThreads() {
            return threadPool.getThreads();
        }

        public void join() throws InterruptedException {
        }

        public boolean isLowOnThreads() {
            return threadPool.isLowOnThreads();
        }
    }
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final Log logger = LogFactory.getLog(JettyServer.class);
    
    private Map servers;
    private HttpConfiguration configuration;
    private BoundedThreadPool threadPool;
    private Map sslParams;
    private MBeanServer mbeanServer;
    private MBeanContainer mbeanContainer;
    
}
