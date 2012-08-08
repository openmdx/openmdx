/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LightweightContainer.java,v 1.24 2011/06/21 22:54:40 hburger Exp $
 * Description: Lightweight Container
 * Revision:    $Revision: 1.24 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/06/21 22:54:40 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.application.container.lightweight;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.InitialContextFactory;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.sql.XADataSource;
import javax.transaction.UserTransaction;
import javax.xml.parsers.ParserConfigurationException;

import org.openmdx.kernel.application.client.spi.Main;
import org.openmdx.kernel.application.container.spi.ejb.BeanInstanceFactory;
import org.openmdx.kernel.application.container.spi.ejb.ContainerTransaction;
import org.openmdx.kernel.application.container.spi.ejb.ContextSwitcher;
import org.openmdx.kernel.application.container.spi.ejb.ProxyReference;
import org.openmdx.kernel.application.container.spi.http.ManagedDataproviderConnectionFactory;
import org.openmdx.kernel.application.container.spi.resource.ManagedCallbackHandler;
import org.openmdx.kernel.application.deploy.cci.DeploymentProperties;
import org.openmdx.kernel.application.deploy.enterprise.VerifyingDeploymentManager;
import org.openmdx.kernel.application.deploy.lightweight.ValidatingDeploymentManager;
import org.openmdx.kernel.application.deploy.spi.Deployment;
import org.openmdx.kernel.application.deploy.spi.LightweightClassLoader;
import org.openmdx.kernel.application.deploy.spi.Report;
import org.openmdx.kernel.application.deploy.spi.Deployment.Application;
import org.openmdx.kernel.application.deploy.spi.Deployment.AuthenticationMechanism;
import org.openmdx.kernel.application.deploy.spi.Deployment.Component;
import org.openmdx.kernel.application.deploy.spi.Deployment.SessionBean;
import org.openmdx.kernel.application.process.Subprocess;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.lightweight.resource.LightweightConnectionManager;
import org.openmdx.kernel.lightweight.sql.DatabaseConnectionFactory;
import org.openmdx.kernel.lightweight.sql.DatabaseConnectionRequestInfo;
import org.openmdx.kernel.lightweight.sql.LightweightXADataSource;
import org.openmdx.kernel.lightweight.sql.ManagedDatabaseConnectionFactory;
import org.openmdx.kernel.lightweight.transaction.LightweightTransactionManager;
import org.openmdx.kernel.lightweight.transaction.LightweightTransactionSynchronizationRegistry;
import org.openmdx.kernel.lightweight.transaction.LightweightUserTransaction;
import org.openmdx.kernel.loading.BeanFactory;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.loading.Factory;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.naming.Contexts;
import org.openmdx.kernel.naming.component.java.ComponentContextFactory;
import org.openmdx.kernel.naming.container.openmdx.ContainerContextFactory;
import org.openmdx.kernel.naming.container.openmdx.openmdxURLContextFactory;
import org.openmdx.kernel.naming.spi.ClassLoadertContextFactory;
import org.openmdx.kernel.naming.spi.rmi.Context_1;
import org.openmdx.kernel.naming.tomcat.LinkReference;
import org.openmdx.kernel.resource.spi.ShareableConnectionManager;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.openmdx.uses.org.apache.commons.pool.ObjectPool;
import org.openmdx.uses.org.apache.commons.pool.impl.GenericObjectPool;

/**
 * Lightweight Container
 * <p>
 * Note:<ul>
 * <li>The lightweight container is designed as a singleton.
 * </ul>
 * 
 * @see org.openmdx.kernel.application.deploy.cci.DeploymentProperties#CONNECTOR_URLS
 * @see org.openmdx.kernel.appl-xmlication.deploy.cci.DeploymentProperties#APPLICATION_URLS
 */
public class LightweightContainer {

    /**
     * Constructor
     * 
     * @param mode the mode in which the lightweight container shall be launched
     * @param applicationContextFactory
     * @param componentContextFactory 
     * @param contextSwitcher 
     * @throws NamingException 
     */
    private LightweightContainer(
        Mode mode, 
        InitialContextFactory applicationContextFactory, 
        InitialContextFactory componentContextFactory, 
        ContextSwitcher contextSwitcher
    ) throws NamingException {
        SysLog.log(Level.INFO,"Starting {0}", mode);
        LightweightContainer.instance = this;
        this.mode = mode;        
        System.setProperty(
           LightweightContainer.class.getName() + ".Mode",
           mode.toString()
        );
        //
        // URL setup
        //
        prependSystemPropertyValue(
            "java.protocol.handler.pkgs",
            "org.openmdx.kernel.url.protocol",
            '|'
        );
        //
        // JNDI Settings
        //
        prependSystemPropertyValue(
            Context.URL_PKG_PREFIXES,
            "org.openmdx.kernel.naming.container",
            ':'
        );
        if(mode != Mode.ENTERPRISE_JAVA_BEAN_CONTAINER) {
            System.setProperty(
                Context.INITIAL_CONTEXT_FACTORY,
                "org.openmdx.kernel.naming.initial.ContextFactory"
            );
            prependSystemPropertyValue(
                Context.URL_PKG_PREFIXES,
                "org.openmdx.kernel.naming.component",
                ':'
            ); 
        }
        this.providerURL = mode == Mode.ENTERPRISE_JAVA_BEAN_SERVER ? new StringBuilder(
            "//"
        ).append(
            getLocalHost()
        ).append(
            ':'
        ).append(
            Contexts.getRegistryPort()
        ).append(
            '/'
        ).append(
            Contexts.getNamingService()
        ).toString(
        ) : null;
        //
        // Context Switcher
        //
        this.contextSwitcher = contextSwitcher;
        //
        // Class Loader
        //
        this.containerClassLoader = new LightweightClassLoader(
            "Lightweight Container",
            mode.toString(),
            LightweightContainer.class.getClassLoader()
        );
        //
        // Context Factories
        //
        this.containerContextFactory = new ContainerContextFactory(
            openmdxURLContextFactory.CONTAINER_CONTEXT
        );
        this.privateContextFactory = new ContainerContextFactory(
            openmdxURLContextFactory.PRIVATE_CONTEXT
        );
        this.applicationContextFactory = applicationContextFactory;
        this.componentContextFactory = componentContextFactory;
        //
        // Initial Contexts
        //
        Hashtable<String, Object> environment = ClassLoadertContextFactory.getEnvironment(
            getClass().getClassLoader(),
            null // no uri
        );
        this.containerContext = this.containerContextFactory.getInitialContext(environment);
        this.privateContext = this.privateContextFactory.getInitialContext(environment);
        //
        // Set up the deployment managers
        //
        this.deploymentManager = newDeploymentManager();
        //
        // System-Property based deployment
        //
        if(mode != Mode.ENTERPRISE_JAVA_BEAN_CONTAINER) {
            //
            // Auto-Deploy Connectors 
            //
            for(String url : getUrls(DeploymentProperties.CONNECTOR_URLS)){
                SysLog.log(Level.INFO,"Auto-deploying connector {0}", url);
                try {
                    Report report = deployConnector(new URL(url));
                    log("connector", url, report);
                } catch (Exception exception) {
                    log("Deployment of connector", url, exception);
                }
            }
            //
            // Auto-Deploy Applications
            //
            for(String url : getUrls(DeploymentProperties.APPLICATION_URLS)){
                SysLog.log(Level.INFO,"Auto-deploying application {0}", url);
                try {
                    Report[] reports = deployApplication(new URL(url));
                    log("application", url, reports);
                } catch (Exception exception) {
                    log("Deployment of application", url, exception);
                }
            }
        }
    }

    /**
     * Acquire a deployment manager instance
     * 
     * @return a new deployment manager instance
     */
    private static Deployment newDeploymentManager(
    ){
        try {
            return new ValidatingDeploymentManager(
                new VerifyingDeploymentManager()
            );
        } catch (ParserConfigurationException exception) {            
            System.err.println("Deplyoment manager acquisition failed: " + exception.getMessage());
            SysLog.log(Level.SEVERE,"Deplyoment manager acquisition failed", exception);
            return null;
        }
    }

    /**
     * Assert that a specific value is among the values in a system propery's 
     * value list.
     * 
     * @param name the system property's name
     * @param value the required value
     * @param separator the value separator
     */
    private static void prependSystemPropertyValue (
        String name,
        String value,
        char separator
    ){
        String values = System.getProperty(name);
        if(values == null || values.length() == 0){
            SysLog.log(
                Level.INFO,
                "Set system property {0} to \"{1}\"",
                new Object[]{name,value}
            );
            System.setProperty(
                name, 
                value
            );
        } else if ((separator + values + separator).indexOf(separator + value + separator) < 0) {
            String newValue = value + separator + values; 
            SysLog.log(
                Level.INFO,
                "Change system property {0} from \"{1}\" to \"{2}\"",
                new Object[]{name, values, newValue}
            );
            System.setProperty(
                name,
                newValue
            );
        }
    }

    /**
     * Log completion
     * 
     * @param type
     * @param url
     * @param report
     */
    private static void log(
        String type,
        String url,
        Report report
    ){
        String message = "Deployment of " + type + " '" + url + "' ";
        Object[] logParameters = new Object[]{message, report}; 
        if(report.isSuccess()){
            message += "completed";
            System.out.println(message + " (see log for details)");
            SysLog.log(Level.INFO,"{0}|{1}", logParameters);
        } else {
            System.err.println(message + " (see log for reason)");
            SysLog.log(Level.WARNING,"{0}|{1}", logParameters);
        }
    }

    /**
     * Log completion
     * 
     * @param type
     * @param url
     * @param report
     */
    private static void log(
        String type,
        String url,
        Report[] reports
    ){
        StringBuilder message = new StringBuilder(
            "Deployment of "
        ).append(
            type
        ).append(
            " '"
        ).append(
            url
        ).append(
            "' "
        );
        MultiLineStringRepresentation detail = new IndentingFormatter(reports);
        Object[] logParameters = new Object[]{message, detail};
        if(reports[0].isSuccess()){
            if(reports[0].hasWarning()) {
                message.append("completed, but some modules failed");
                SysLog.log(Level.WARNING,"{0}|{1}", logParameters);
                System.err.println(message.append(" (see log for reason)"));
            } else {
                message.append("successfully completed");
                SysLog.log(Level.INFO,"{0}|{1}", logParameters);
                System.out.println(message.append(" (see log for details)"));
            }
        } else {
            message.append("failed");
            SysLog.log(Level.WARNING,"{}|{}", logParameters);
            System.err.println(message.append(" (see log for reason)"));
        }
    }

    /**
     * Log abort 
     * 
     * @param type
     * @param url
     * @param report
     */
    private static void log(
        String type,
        String url,
        Throwable exception
    ){
        String message = type + " '" + url + "' aborted";
        SysLog.log(Level.WARNING,message, exception);
        System.err.println(message + ": " + exception);
    }

    /**
     * Get URLs specified by system properties
     * 
     * @param   systemProperty
     *          the system property name
     * 
     * @return  the URLs specified by the system property, a list which may
     *          be empty but never null.
     */
    private static List<String> getUrls(
        String systemProperty
    ){
        String urlString = System.getProperty(systemProperty);
        List<String> urlList = new ArrayList<String>();
        if(urlString != null) for (
                StringTokenizer urlTokens = new StringTokenizer(urlString);
                urlTokens.hasMoreTokens();
        ){
            String url = urlTokens.nextToken();
            if(url.length() > 0) urlList.add(url);
        }
        return urlList;
    }

    /**
     * The lightweight container singleton
     */
    private static LightweightContainer instance = null;

    /**
     * Create a new lightweight container instance
     * 
     * @param mode the lightweight container's mode
     * @param applicationContextFactory
     * @param componentContextFactory
     * @param contextSwitcher 
     * @return a lightweight container in the given mode
     */
    private static LightweightContainer newInstance(
        Mode mode,
        InitialContextFactory applicationContextFactory, 
        InitialContextFactory componentContextFactory, 
        ContextSwitcher contextSwitcher
    ){
        try {
            return new LightweightContainer(
                mode, 
                applicationContextFactory, 
                componentContextFactory, 
                contextSwitcher
            );
        } catch (NamingException exception) {
            SysLog.log(Level.SEVERE,"Lightweight Container Activation Failure", exception);
            return null;
        }
    }

    /**
     * Gets the Lightweight Container Singleton
     *  
     * @return the lightweight container singleton
     * 
     * @see javax.naming.spi.InitialContextFactoryBuilder
     */
    public static LightweightContainer getInstance(
    ){
        if(hasInstance()) {
            return LightweightContainer.instance;
        } else {
            SysLog.log(
                Level.WARNING,
                "LightweightContainer.getInstance() in order to acquire an instance is deprecated. " +
                "Use getInstance(Mode.ENTERPRISE_APPLICATION_CONTAINER) instead"
            );
            return getInstance(Mode.ENTERPRISE_APPLICATION_CONTAINER);
        }
    }

    /**
     * Get a lightweight container instance
     * 
     * @param mode the lightweight container's mode
     * 
     * @return a lightweight container instance
     */
    public static LightweightContainer getInstance(
        Mode mode
    ){
        if(LightweightContainer.instance == null) {
            return newInstance(
                mode, 
                new ContainerContextFactory(), 
                new ComponentContextFactory(), 
                null // contextSwitcher
            );
        } else if (LightweightContainer.instance.mode == mode) {
            return LightweightContainer.instance;
        } else throw new IllegalStateException(
            "Can't get a " + mode + " instance, " +
            "the lightweight container is already started as " + LightweightContainer.instance.mode
        );
    }

    /**
     * Create a new ENTERPRISE_JAVA_BEAN_CONTAINER instance
     * 
     * @param applicationContextFactory
     * @param componentContextFactory
     * @param contextSwitcher 
     * 
     * @return a new ENTERPRISE_JAVA_BEAN_CONTAINER instance
     */
    public static LightweightContainer getInstance(
        InitialContextFactory applicationContextFactory, 
        InitialContextFactory componentContextFactory, 
        ContextSwitcher contextSwitcher
    ){
        if(hasInstance()) {
            throw new IllegalStateException(
                "Can't get a " + Mode.ENTERPRISE_JAVA_BEAN_CONTAINER + " instance, " +
                "the lightweight container is already started as " + LightweightContainer.instance.mode
            );
        } else {
            return newInstance(
                Mode.ENTERPRISE_JAVA_BEAN_CONTAINER, 
                applicationContextFactory, 
                componentContextFactory, 
                contextSwitcher
            );
        }
    }

    /**
     * Tests whether there is a Lightweight Container Singleton.
     * 
     * @return <code>true</code> if there exists a lightweight container in this VM.
     */
    public static boolean hasInstance(){
        return LightweightContainer.instance != null;
    }

    /**
     * Deploy an enterprise application without application clients or web 
     * applications.
     * 
     * @param applicationURL
     *        an enterprise application archive or directory to be deployed
     * 
     * @return the configuration validation reports.
     *         The first element's success flag is true if and only if
     *         the whole deployment succeeded. 
     * 
     * @throws Exception
     */
    public Report[] deployApplication(
        URL applicationURL
    ) throws Exception{
        return deployApplication(
            applicationURL, 
            null // no stagingDirectory
        );
    }

    /**
     * Deploy an enterprise application without application clients or web 
     * applications.
     * 
     * @param applicationURL
     *        an enterprise application archive or directory to be deployed
     * @param stagingDirectory 
     * @return the configuration validation reports.
     *         The first element's success flag is true if and only if
     *         the whole deployment succeeded. 
     * 
     * @throws Exception
     */
    public Report[] deployApplication(
        URL applicationURL, 
        File stagingDirectory
    ) throws Exception{
        return deploy (
            applicationURL,
            null, // no applicationClientEnvironment
            null, // no applicationClientArguments
            null, // no applicationClientHolder
            stagingDirectory
        );
    }

    /**
     * Deploy a connector
     * 
     * @param connectorURL
     *        a resource adapter archive or directory to be deployed
     * 
     * @return the configuration validation report
     *         Its success flag is true if and only if the deployment 
     *         succeeded. 
     */
    synchronized public Report deployConnector(
        URL connectorURL
    ) throws Exception {
        List<Report> reports = new ArrayList<Report>();
        Deployment.Connector connector = this.deploymentManager.getConnector(
            connectorURL
        );
        Report connectorReport = connector.validate();
        if(isSuccess(reports, connectorReport)){
            LightweightClassLoader anonymousClassLoader = new LightweightClassLoader(
                connectorReport.getName(),
                connectorURL.toString(),
                this.containerClassLoader
            );
            deploy(
                null,
                anonymousClassLoader,
                connector,
                connectorReport
            );
        }
        if(connectorReport.isSuccess()){
            connectorReport.addInfo(
                "Resource adapter sucessfully deployed in " + toString()
            );
        } else {
            connectorReport.addWarning(
                "Deplyoment of resource adapter in " + toString() + " failed"
            );
        }
        return reports.get(0);
    }

    /**
     * Deploy an enterprise application including one application client but  
     * no web application.
     * @param applicationURL
     *        an enterprise application archive or directory to be deployed
     * @param applicationClientEnvironment 
     *        used to override the application clients environment entries
     * 
     * @return the configuration validation reports.
     *         The first element's success flag is true if and only if
     *         the whole deployment succeeded. 
     * 
     * @throws Exception
     */
    synchronized public Runnable deployApplicationClient(
        String applicationURL,
        Map<String,String> applicationClientEnvironment,
        String[] applicationClientArguments
    ) throws Exception {
        List<Runnable> applicationClientHolder = new ArrayList<Runnable>();
        Report[] reports = deploy(
            new URL(applicationURL),
            applicationClientEnvironment,
            applicationClientArguments,
            applicationClientHolder, 
            null // no stagingDirectory
        );
        Runnable applicationClient = null;
        if(reports[0].isSuccess()) {
            if(applicationClientHolder.size() == 1) {
                applicationClient = applicationClientHolder.get(0);
            } else {
                reports[0].addError(
                    "Exactly one application client module expected, found " +
                    applicationClientHolder.size()
                );
            }
        }
        log("Application Client", applicationURL, reports);
        return applicationClient;
    }

    /**
     * Deploy an Enterprise Application 
     * 
     * @param applicationURL
     *        an enterprise application archive or directory to be deployed
     * @param applicationClientEnvironment 
     *        used to override the application clients environment entries
     * @param applicationClientArguments
     * @param applicationClientHolder 
     * @param stagingDirectory 
     *        the staging directory for webApplications
     * @return the configuration validation reports.
     *         The first element's success flag is true if and only if
     *         the whole deployment succeeded. 
     * 
     * @throws Exception
     */
    synchronized private Report[] deploy(
        URL applicationURL,
        Map<String, String> applicationClientEnvironment,
        String[] applicationClientArguments,
        List<Runnable> applicationClientHolder, 
        File stagingDirectory
    ) throws Exception {
        List<Report> reports = new ArrayList<Report> ();
        Deployment.Application application = this.deploymentManager.getApplication(
            applicationURL
        );
        Report applicationReport = application.validate();
        if(isSuccess(reports, applicationReport)){
            LightweightClassLoader applicationClassLoader = LightweightClassLoader.newInstance(
                applicationReport.getName(),
                applicationURL.toString(),
                this.containerClassLoader,
                application.getApplicationClassPath(),
                applicationReport
            );
            String uri;
            if(stagingDirectory == null) {
                uri = new File(applicationURL.getPath()).getName();
            } else {
                uri = stagingDirectory.getName();
                if(!stagingDirectory.isDirectory()) {
                    stagingDirectory.mkdirs();
                }
            }
            Context applicationContext = this.applicationContextFactory.getInitialContext(
                ClassLoadertContextFactory.getEnvironment(applicationClassLoader, uri)
            );
            for (Deployment.Module module : application.getModules()) {
                deploy(
                    uri,
                    application,
                    applicationClassLoader,
                    applicationContext,
                    module,
                    reports,
                    applicationClientEnvironment, 
                    applicationClientArguments, 
                    applicationClientHolder, stagingDirectory
                );
            }
        }
        int successCount=0,failureCount=0;
        for(
                Iterator<Report> i = reports.listIterator(1);
                i.hasNext();
        ){
            if(i.next().isSuccess()){
                successCount++;
            } else {
                failureCount++;
            }
        }
        if(failureCount==0){
            reports.get(0).addInfo(
                String.valueOf(successCount + failureCount) + " modules or components sucessfully deployed in " + toString()
            );
        } else {
            reports.get(0).addWarning(
                String.valueOf(successCount + failureCount) + " modules or components deployed in " + toString() +
                ": " + successCount + " succedded, " + failureCount + " failed"
            );
        }
        return reports.toArray(
            new Report[reports.size()]
        );
    }

    /**
     * Deploy a module
     * @param applicationURI 
     * @param application 
     * @param applicationClassLoader the module's parent classloader
     * @param module
     * @param reports 
     * @param applicationClientEnvironment 
     * @param applicationClientArguments
     * @param applicationClientHolder
     * @param stagingDirectory 
     * 
     * @return <code>true</code> if the module matches the application client filter.
     */
    private void deploy (
        String applicationURI,
        Application application,
        LightweightClassLoader applicationClassLoader,
        Context applicationContext,
        Deployment.Module module,
        List<Report> reports,
        Map<String, String> applicationClientEnvironment, 
        String[] applicationClientArguments, List<Runnable> applicationClientHolder, File stagingDirectory
    ){
        Report report = module.validate();
        if(isSuccess(reports, report)) try {
            String id = getId(module);
            String uri = applicationURI + '#' + module.getModuleURI();
            applicationClassLoader.addURLs(
                module.getApplicationClassPath(), 
                report
            );
            if(module instanceof Deployment.WebApplication){
                if(stagingDirectory == null) {
                    report.addWarning(
                        "Web application " + id + " is ignored by the lightweight container"
                    );
                } else {
                    deploy(
                        application,
                        (Deployment.WebApplication)module, 
                        report, 
                        stagingDirectory
                    );
                }
            } else {
                URL[] moduleClassPath = module.getModuleClassPath();
                LightweightClassLoader moduleClassLoader = LightweightClassLoader.newInstance(
                    report.getName(),
                    moduleClassPath.length == 0 ? "" : moduleClassPath[0].toString(),
                        applicationClassLoader,
                        moduleClassPath,
                        report
                );
                if (module instanceof Deployment.Connector) {
                    deploy(
                        applicationContext,
                        moduleClassLoader,
                        (Deployment.Connector) module,
                        report
                    );
                } else if (module instanceof Deployment.ApplicationClient) {
                    Deployment.ApplicationClient applicationClient = (Deployment.ApplicationClient) module;
                    if(applicationClientHolder != null) {
                        applicationClientHolder.add(
                            deploy(
                                applicationContext,
                                moduleClassLoader,
                                applicationClient,
                                report,
                                applicationClientEnvironment,
                                applicationClientArguments
                            )
                        );
                        if(report.isSuccess()){
                            report.addInfo(
                                id + " deployed"
                            );
                            String callbackHandler = applicationClient.getCallbackHandler();
                            if(callbackHandler != null) this.callbackHandler = new ManagedCallbackHandler(
                                moduleClassLoader,
                                callbackHandler
                            );
                        } else {
                            reports.get(0).addError(
                                "Deployment of " + id + " failed"
                            );
                        }
                    } else {
                        report.addWarning(
                            "Server side " + id + " is ignored by the lightweight container"
                        );
                    }
                } else {
                    for(Component component : module.getComponents()) {
                        deploy(
                            applicationClassLoader,
                            applicationContext,
                            moduleClassLoader,
                            uri,
                            component, 
                            reports
                        );
                    }
                }
            }
        } catch (NamingException exception) {
            report.addError("Unable to bind the module to the JNDI context", exception);
        } catch (IOException exception) {
            report.addError("Resolving the manifests' Class-Path attribute failed", exception);
        }
    }

    /**
     * Deploy a Java 2 Enterprise Component
     * 
     * @param applicationClassLoader
     * @param applicationContext
     * @param moduleClassLoader the module's ClassLoader
     * @param moduleURI 
     * @param component the Java 2 Enterprise component to be deployed
     * @param reports 
     * @throws NamingException
     */
    private void deploy (
        LightweightClassLoader applicationClassLoader,
        Context applicationContext,
        LightweightClassLoader moduleClassLoader,
        String moduleURI,
        Deployment.Component component, 
        List<Report> reports
    ) throws NamingException {
        Report report = component.validate();
        if(isSuccess(reports, report)) {
            String componentURI = moduleURI + '#' + component.getName();
            LightweightClassLoader componentClassLoader = new LightweightClassLoader(
                report.getName(),
                component.getName(),
                moduleClassLoader
            );
            Context componentContext = this.componentContextFactory.getInitialContext(
                ClassLoadertContextFactory.getEnvironment(
                    componentClassLoader,
                    componentURI
                )
            );
            if(component instanceof Deployment.Bean){
                deploy(
                    applicationClassLoader, 
                    applicationContext,
                    componentClassLoader, 
                    componentContext,
                    (Deployment.Bean)component, 
                    report
                );
            } else {
                report.addError("Unsupported component type '" + component.getClass().getName() + "'");
                return;
            }
        }
    }

    /**
     * Deploy a Java 2 Web Application
     * 
     * @param enterpriseApplication 
     * @param report the report to be amended
     * @param stagingDirectory 
     * @param applicationClassLoader
     * @param applicationContext
     * @param componentClassLoader
     * @param componentContext
     * @param bean the Enterprise JavaBean to be deployed
     * 
     * @return the web application context file
     * 
     * @throws IOException
     */
    private void deploy (
        Application enterpriseApplication,
        Deployment.WebApplication webApplication, 
        Report report, 
        File stagingDirectory
    ) throws IOException{
        String id = getId(webApplication);
        report.addInfo("Deploying Web Application " + id);
        //
        // Context Naming
        //
        String contextRoot = webApplication.getContextRoot();
        if(!contextRoot.startsWith("/")) contextRoot = "/" + contextRoot;
        String contextId = contextRoot.substring(1).replace('/','#');
        File contextFile = new File(
            stagingDirectory,
            (contextId.length() == 0 ? "ROOT" : contextId) + ".xml" 
        );
        //
        // Web Application Naming
        //
        File webApplicationFile;
        URI webApplicationURI;
        try {
            String webXML = report.getContext();
            webApplicationURI = new URI(
                webXML.substring(
                    webXML.indexOf('(') + 1,
                    webXML.lastIndexOf(')')
                )
            );
        } catch (URISyntaxException exception) {
            report.addError(
                "WebApplicatoion URI could not be determined",
                exception
            );
            return;
        }
        URL webApplicationURL = webApplicationURI.toURL();
        if(enterpriseApplication.isExpanded()) {
            webApplicationFile = new File(webApplicationURI);
        } else {
            //
            // Web Application Staging
            // 

            String moduleURI = webApplication.getModuleURI();
            webApplicationFile = new File(
                stagingDirectory,
                moduleURI.substring(
                    moduleURI.startsWith("/") ? 1 : 0
                ).replace(
                    '/',
                    '#'
                )
            );
            URLConnection connection = webApplicationURL.openConnection();
            long sourceModification = connection.getLastModified();
            if(
                    sourceModification == 0L || // source time stamp unknown  
                    webApplicationFile.lastModified() < sourceModification // target is newer
            ) {
                report.addInfo("Staging " + webApplicationURI + ": " + webApplicationFile.getAbsolutePath());
                InputStream input = connection.getInputStream();
                OutputStream output = new FileOutputStream(webApplicationFile);
                byte[] buffer = new byte[0x10000];
                for(
                        int i = input.read(buffer);
                        i >= 0;
                        i = input.read(buffer)
                ){
                    output.write(buffer, 0, i);
                }
                input.close();
                output.flush();
                output.close();
                if(sourceModification != 0L) {
                    webApplicationFile.setLastModified(sourceModification);
                }
            } else {
                report.addInfo("Keeping " + webApplicationURI + ": " + webApplicationFile.getAbsolutePath());

            }
        }
        {
            //
            // Context Staging
            // 
            URL contextFragmentURL = VerifyingDeploymentManager.getNestedArchiveUrl(
                webApplicationURL, 
                VerifyingDeploymentManager.TOMCAT_WEB_XML
            );
            long sourceModification = new URL(report.getContext()).openConnection().getLastModified();
            URLConnection fragmentConnection = contextFragmentURL.openConnection();
            long fragmentModification = fragmentConnection.getLastModified();
            if(fragmentModification > sourceModification) {
                sourceModification = fragmentModification;
            }
            if(
                    sourceModification == 0L || // source time stamp unknown  
                    contextFile.lastModified() < sourceModification // target is newer
            ) {
                report.addInfo("Staging " + webApplicationFile + " context: " + contextFile.getAbsolutePath());
                OutputStream output = new FileOutputStream(contextFile);
                Writer contextWriter = new OutputStreamWriter(output, "UTF-8");
                contextWriter.write("<Context\n\tclassName=\"org.openmdx.tomcat.application.container.ExtendedContext\"\n\tpath=\"");
                contextWriter.write(contextRoot);
                contextWriter.write("\"\n\tenterpriseApplication=\"");
                contextWriter.write(stagingDirectory.getName());
                contextWriter.write("\"\n\tdocBase=\"");
                contextWriter.write(webApplicationFile.getCanonicalPath());
                contextWriter.write("\"\n>\n");
                try {
                    webApplication.populate(contextWriter);
                } catch (NamingException exception) {
                    report.addWarning(
                        "Resource link declaration failure",
                        exception
                    );
                }
                InputStream input;
                try {
                    input = contextFragmentURL.openStream();
                    report.addInfo("Embedding context fragment " + contextFragmentURL + " into " + contextFile.getAbsolutePath());
                } catch (IOException exception) {
                    input = null;
                    report.addInfo("No context fragment " + contextFragmentURL + " for " + contextFile.getAbsolutePath());
                }
                if(input != null) {
                    byte[] buffer = new byte[0x1000];
                    contextWriter.write("<!--source=\"" + VerifyingDeploymentManager.TOMCAT_WEB_XML + "\"-->\n");
                    contextWriter.flush();
                    for(
                            int i = input.read(buffer);
                            i >= 0;
                            i = input.read(buffer)
                    ){
                        output.write(buffer, 0, i);
                    }
                    input.close();
                }
                contextWriter.write("\n</Context>\n");
                contextWriter.flush();
                output.close();
                if(sourceModification > 0) {
                    contextFile.setLastModified(sourceModification);
                }
            } else {
                report.addInfo("Keeping " + webApplicationFile + " context: " + contextFile.getAbsolutePath());
            }
        }
    }

    /**
     * Deploy a Java 2 Enterprise Bean
     * 
     * @param applicationClassLoader
     * @param applicationContext
     * @param componentClassLoader
     * @param componentContext
     * @param bean the Enterprise JavaBean to be deployed
     * @param report the report to be amended
     * @throws NamingException
     */
	private void deploy (
        LightweightClassLoader applicationClassLoader,
        Context applicationContext,
        LightweightClassLoader componentClassLoader,
        Context componentContext,
        Deployment.Bean bean,
        Report report
    ) throws NamingException{
        report.addInfo("Deploying EJB '" + bean.getName() + "'");
        //
        // Validate Constraints
        // 
        if(!(bean instanceof Deployment.SessionBean)) {
            report.addError("Unsupported bean type '" + bean.getClass().getName() + "'");
            return;
        }
        Deployment.SessionBean sessionBean = (SessionBean) bean;
        //
        // CR0003112 Stateful Session Bean Support in LightweightContainer
        //
        if(!"Stateless".equals(sessionBean.getSessionType())) {
            report.addError("Unsupported session type '" + sessionBean.getSessionType() + "'");
            return;
        }
        //
        // Transaction Handling
        //
        UserTransaction userTransaction;
        LightweightSessionContext sessionContext;
        {
            String value = sessionBean.getTransactionType();
            if("Bean".equals(value)) {
                sessionContext = new LightweightSessionContext(
                    null,
                    userTransaction = LightweightUserTransaction.getInstance()
                );
            } else if ("Container".equals(value)) {
                sessionContext = new LightweightSessionContext(
                    LightweightTransactionManager.getInstance(),
                    userTransaction = null
                );
            } else {
                report.addError("Unsupported transaction type '" + value + "'");
                return;
            }
        }
        //
        //
        // Populate Component Context
        // 
        sessionBean.populate(componentContext);
        if(userTransaction != null) {
            componentContext.bind("UserTransaction", userTransaction);
            report.addInfo("'UserTransaction' bound to 'java:comp'");
        }
        componentContext.bind(
        	"TransactionManager",
        	LightweightTransactionManager.getInstance()
        );
        report.addInfo("'TransactionManager' bound to 'java:comp'");
        componentContext.bind(
            "TransactionSynchronizationRegistry",
            LightweightTransactionSynchronizationRegistry.getInstance()
        );
        report.addInfo("'TransactionSynchronizationRegistry' bound to 'java:comp'");
        //
        // Create Instance Pool
        //
        BeanInstanceFactory instanceFactory;
        try {
            instanceFactory = new BeanInstanceFactory(
                componentClassLoader,
                sessionBean,
                sessionContext, 
                contextSwitcher
            );
        } catch (NoSuchMethodException exception) {
            report.addError(
                "Bean instance factory creation failed",
                BasicException.newStandAloneExceptionStack(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_IMPLEMENTED,
                    "Bean class lacks ejbCreate method",
                    new BasicException.Parameter("class",sessionBean.getEjbClass()),
                    new BasicException.Parameter("method","ejbCreate")
                )
            );
            return;
        } catch (Exception exception) {
            report.addError("Bean instance factory creation failed", exception);
            return;
        }
        ObjectPool instancePool = sessionBean.getMaximumWait().longValue() == 0L ? new GenericObjectPool(
            instanceFactory,
            sessionBean.getMaximumCapacity().intValue(),
            GenericObjectPool.WHEN_EXHAUSTED_FAIL,
            GenericObjectPool.DEFAULT_MAX_WAIT,
            sessionBean.getMaximumCapacity().intValue()
        ) : new GenericObjectPool(
            instanceFactory,
            sessionBean.getMaximumCapacity().intValue(),
            GenericObjectPool.WHEN_EXHAUSTED_BLOCK,
            sessionBean.getMaximumWait().longValue(),
            sessionBean.getMaximumCapacity().intValue()
        );
            Object callerContext = instanceFactory.setBeanContext();
            //
            // Create Home
            //
            try {
                ContainerTransaction containerTransaction = new LightweightContainerTransaction(
                    sessionBean.getContainerTransaction()
                );
                LightweightHomeFactory homeFactory = new LightweightHomeFactory(
                    sessionBean.getHome(),
                    sessionBean.getRemote(),
                    sessionBean.getLocalHome(),
                    sessionBean.getLocal(), 
                    sessionBean.getHomeClass()
                );
                homeFactory.initialize(
                    sessionBean.getName(),
                    sessionBean.getJndiName(),
                    instanceFactory,
                    instanceFactory.getInstanceClass(),
                    instancePool,
                    containerTransaction,
                    LightweightTransactionManager.getInstance()
                );
                if(getMode() == Mode.ENTERPRISE_JAVA_BEAN_CONTAINER){
                    sessionContext.remoteReference = createProxyReference (
                        homeFactory.getHomeClass(),
                        homeFactory.getHomeHandler()
                    );
                    sessionContext.localReference = createExplicitReference(
                        homeFactory.getLocalHome()
                    );
                } else {
                    sessionContext.remoteReference = createStandardReference(
                        homeFactory.getHome()
                    );
                    sessionContext.localReference = createStandardReference(
                        homeFactory.getLocalHome()
                    );
                }
                for(
                        int i = sessionBean.getInitialCapacity().intValue();
                        i > 0;
                        i--
                ) try {
                    instancePool.addObject();
                } catch (Exception exception) {
                    report.addWarning("Instance creation failed", exception);
                    break;
                }
            } catch (Exception exception) {
                report.addError("EJB deployment failed", exception);
            } finally {
                instanceFactory.setCallerContext(callerContext);
            }
            //
            // Deploy
            // 
            sessionBean.deploy(
                containerContext, applicationContext,
                sessionContext.localReference, sessionContext.remoteReference
            );
    }

    /**
     * Deploy a Java 2 Application Client
     * 
     * @param applicationContext
     * @param moduleClassLoader
     * @param applicationClient
     * @param report
     * @param applicationClientEnvironment
     * @param applicationClientArguments 
     * @param applicationClientFilter 
     * 
     * @throws NamingException
     */
    private Runnable deploy (
        Context applicationContext,
        ClassLoader moduleClassLoader,
        Deployment.ApplicationClient applicationClient,
        Report report,
        Map<String, String> applicationClientEnvironment,
        String[] applicationClientArguments
    ) throws NamingException {
        Hashtable<String,Object> environment = new Hashtable<String,Object>();
        environment.put(
            ClassLoader.class.getName(),
            moduleClassLoader
        );
        Context componentContext = this.componentContextFactory.getInitialContext(
            environment
        );
        applicationClient.populate(
            componentContext,
            applicationClientEnvironment
        );
        componentContext.bind(
            "UserTransaction",
            LightweightUserTransaction.getInstance()
        );
        componentContext.bind(
            "TransactionSynchronizationRegistry",
            LightweightTransactionSynchronizationRegistry.getInstance()
        );
        try {
            return new Main(
                Classes.getApplicationClass(
                    applicationClient.getMainClass()
                ).getDeclaredMethod(
                    "main",
                    String[].class
                ),
                applicationClientArguments,
                moduleClassLoader
            );
        } catch (SecurityException exception) {
            report.addError(
                "Wrapping '" + applicationClient.getMainClass() +
                "' into '" + Main.class.getName() + " failed",
                exception
            );
            return null;
        } catch (NoSuchMethodException exception) {
            report.addError(
                "Missig 'main(String[])' method in '" + applicationClient.getMainClass() + "'",
                exception
            );
            return null;
        } catch (ClassNotFoundException exception) {
            report.addError(
                "Missig main class '" + applicationClient.getMainClass() + "'",
                exception
            );
            return null;
        }
    }

    /**
     * Deploy a Java 2 Connector
     * 
     * @param applicationContext
     * @param moduleClassLoader
     * @param connector
     * @param report
     * 
     * @throws NamingException
     */
    private void deploy (
        Context applicationContext,
        ClassLoader moduleClassLoader,
        Deployment.Connector connector,
        Report report
    ) throws NamingException {
        Deployment.ResourceAdapter resourceAdapter = connector.getResourceAdapter();
        String id = UUIDs.newUUID().toString();
        Object connectionFactory;
        Map<String, Object> configuration = new HashMap<String, Object>(resourceAdapter.getConfigProperties());
        Set<Object> credentials;
        List<?> authenticationMechanisms = resourceAdapter.getAuthenticationMechanism();
        switch(authenticationMechanisms.size()) {
            case 0:
                credentials = Collections.emptySet();
                break;
            case 1:
                Deployment.AuthenticationMechanism authenticationMechanism = (AuthenticationMechanism) authenticationMechanisms.get(0);
                if(
                        "BasicPassword".equals(authenticationMechanism.getAuthenticationMechanismType()) &&
                        PasswordCredential.class.getName().equals(authenticationMechanism.getCredentialInterface())
                ){
                    String userName = (String) configuration.remove("UserName");
                    String password = (String) configuration.remove("Password");
                    boolean realmInformationCallback = Boolean.TRUE.equals(
                        configuration.remove("RealmInformationCallback")
                    );
                    PasswordCredential credential;
                    if(userName == null || password == null) {
                        if(this.callbackHandler == null) {
                            report.addWarning(
                                "PasswordCredential credential could not be provided " +
                                "as there is no callback handler configured " +
                                "to add the lacking UserName or Password"
                            );
                            credential = null;
                        } else {
                            List<Serializable> callbacks = new ArrayList<Serializable>();
                            if (realmInformationCallback) callbacks.add(
                                new TextOutputCallback(
                                    TextOutputCallback.INFORMATION,
                                    "Realm: " + connector.getDisplayName()
                                )
                            );
                            NameCallback nameCallback = null;
                            if(userName == null) callbacks.add (
                                nameCallback = new NameCallback("UserName")
                            );
                            PasswordCallback passwordCallback = null;
                            if(password == null) callbacks.add (
                                passwordCallback = new PasswordCallback("Password", false)
                            );
                            try {
                                this.callbackHandler.handle(
                                    callbacks.toArray(new Callback[callbacks.size()])
                                );
                            } catch (Exception exception) {
                                report.addError(
                                    "Authentication callback failed",
                                    exception
                                );
                                return;
                            }
                            credential = new PasswordCredential (
                                userName == null ? nameCallback.getName() : userName,
                                    password == null ? passwordCallback.getPassword() : password.toCharArray()
                            );
                        }
                    } else {
                        credential = new PasswordCredential (
                            userName,
                            password.toCharArray()
                        );
                    }
                    credentials = credential == null ? 
                        Collections.emptySet() :
                            Collections.singleton((Object)credential);
                        report.addInfo(
                            "Connection will be established " + (credentials.isEmpty() ?
                                "without credentials" :
                                    ("as '" + credential.getUserName() + "'")
                            )
                        );
                        break;
                } else if(
                        "TokenCookie".equals(authenticationMechanism.getAuthenticationMechanismType()) &&
                        PasswordCredential.class.getName().equals(authenticationMechanism.getCredentialInterface())
                ){
                    String cookieName = (String) configuration.remove("CookieName");
                    if(cookieName == null) {
                        report.addWarning(
                            "Cookie credential could not be provided " +
                            "as there is no cookie name configured"
                        );
                    } else if(this.callbackHandler == null) {
                        report.addWarning(
                            "Cookie credential could not be provided " +
                            "as there is no callback handler configured " +
                            "to add the lacking UserName or cookie value"
                        );
                    } else {
                        PasswordCallback passwordCallback = new PasswordCallback(cookieName, false);
                        try {
                            this.callbackHandler.handle(
                                new Callback[] {passwordCallback}
                            );
                        } catch (Exception exception) {
                            report.addError(
                                "Authentication callback failed",
                                exception
                            );
                            return;
                        }
                        char[] tokenValue = passwordCallback.getPassword();
                        credentials = tokenValue == null ?
                            Collections.emptySet() :
                                Collections.singleton(
                                    (Object)new PasswordCredential(
                                        "Cookie:" + cookieName,
                                        tokenValue
                                    )
                                );
                            report.addInfo(
                                "Connection will be established " + (credentials.isEmpty() ?
                                    "without credentials" :
                                        ("with '" + cookieName + "' cookie")
                                )
                            );
                            break;
                    }
                }
            default:
                report.addError(
                    "The lightweight container supports only BasicPassword and TokenCookie authentication " +
                    "mechanism with javax.resource.spi.security.PasswordCredential interface at the moment"
                );
            return;
        }
        if(
            ManagedDatabaseConnectionFactory.class.getName().equals(
                resourceAdapter.getManagedConnectionFactoryClass()
            )
        ){
            if(resourceAdapter.getReauthenticationSupport()) report.addWarning(
                "Re-authentication not supported by " +
                DatabaseConnectionFactory.class.getName()
            );
	            String driver = (String) configuration.remove("Driver");
	            TransactionIsolation transactionIsolation = toTransactionIsolation(
	            	(String)configuration.remove("TransactionIsolation")
	            );
	            String validationStatement = (String) configuration.remove("ValidationStatement");
	            Long loginTimeout = (Long) configuration.remove("LoginTimeout");
	            TransactionSupport transactionSupport = TransactionSupport.valueOf(
	           		resourceAdapter.getTransactionSupport()
	            );	
	            String connectionURL = transactionSupport == TransactionSupport.XATransaction ? 
	            	null :
	        		(String) configuration.remove("ConnectionURL");
	            String connectionImplClass = resourceAdapter.getConnectionImplClass();
	            //
	            // Connection Manager Acquisition
	            // 
	            ConnectionManager connectionManager;
	            try {
	                connectionManager = transactionSupport == TransactionSupport.NoTransaction ? new ShareableConnectionManager(
	                    credentials,
	                    connectionImplClass
	                ) : new LightweightConnectionManager(
	                    credentials,
	                    connectionImplClass,
	                    LightweightTransactionManager.getInstance(),
	                    resourceAdapter.getMaximumCapacity(),
	                    resourceAdapter.getMaximumWait(), 
	                    resourceAdapter.getMaximumIdle(), 
	                    resourceAdapter.getMaximumIdle(), 
	                    resourceAdapter.getTestOnBorrow(), 
	                    resourceAdapter.getTestOnReturn(), 
	                    resourceAdapter.getTimeBetweenEvictionRuns(), 
	                    resourceAdapter.getNumberOfTestsPerEvictionRun(), 
	                    resourceAdapter.getMinimumEvictableIdleTime(), 
	                    resourceAdapter.getTestWhileIdle() 
	                );
	            } catch (ResourceException exception) {
	            	report.addError(
	            		"Unable to acquire connection manager",
	                    exception
	                );
	            	return;
	            }
	            //
	            // Data Source Acquisition
	            // 
	            XADataSource xaDataSource;
	            try {
	                if(transactionSupport == TransactionSupport.XATransaction) {
	                    Factory<XADataSource> xaDataSourceFactory = BeanFactory.newInstance(
	                        driver,
	                        configuration
	                    );
	                    xaDataSource = xaDataSourceFactory.instantiate();
	                } else {
	                    if(connectionURL == null) {
	    	            	report.addError("ConnectionURL missing");
	    	            	return;
	                    }
	                    if(driver != null) {
	                        Classes.getKernelClass(driver);
	                    }
	                    DriverManager.getDriver(connectionURL);
	                    xaDataSource = new LightweightXADataSource(
	                        connectionURL, 
	                        configuration
	                    );
	                }
	            } catch (Exception exception) {
	            	report.addError(
	                    "Unable to acquire DataSource",
	                    exception
	                );
	            	return;
	            }
	            //
	            // Connection Factory Acquisition
	            // 
	            DatabaseConnectionRequestInfo connectionRequestInfo = new DatabaseConnectionRequestInfo(
	                transactionIsolation == null ? null : Integer.valueOf(transactionIsolation.value()),
	                validationStatement, 
	                loginTimeout
	            );
	            ManagedConnectionFactory managedConnectionFactory = new ManagedDatabaseConnectionFactory(
	                xaDataSource,
	                connectionRequestInfo
	            );
	            assignCredentials(credentials, managedConnectionFactory);
	            try {
					connectionFactory = managedConnectionFactory.createConnectionFactory(
					    connectionManager
					);
				} catch (ResourceException exception) {
					report.addError(
						"The connection factory, a DataSource, could not be created",
						exception
					);
					return;
				}
	            if(connectionManager instanceof LightweightConnectionManager) try {
	                ((LightweightConnectionManager)connectionManager).preAllocateManagedConnection(
	                    resourceAdapter.getInitialCapacity(),
	                    managedConnectionFactory,
	                    connectionRequestInfo
	                );
	            } catch (ResourceException exception) {
	            	report.addWarning("Unable to pre-allocate connections", exception);
	            }
        } else if(
                ManagedDataproviderConnectionFactory.class.getName().equals(
                    resourceAdapter.getManagedConnectionFactoryClass()
                )
        ){
            String connectionURL = (String) configuration.get("ConnectionURL");
            if(! connectionURL.endsWith("/")) {
                report.addError(
                    "Connection URL '" + connectionURL + "' does not end with a slash",
                    new MalformedURLException("Dataprovider Servlet URL should end with a slash ('/')")
                );
            }
            PasswordCredential passwordCredential = credentials.isEmpty() ?
                null :
                    (PasswordCredential)credentials.iterator().next();
            report.addInfo(
                "Connection will be established " + passwordCredential == null ?
                    "without authentication" :
                        ("as '" + passwordCredential.getUserName() + "'")
            );
            try {
                ManagedConnectionFactory managedConnectionFactory = new ManagedDataproviderConnectionFactory(
                    resourceAdapter.getConnectionFactoryImplClass(),
                    new URL (connectionURL),
                    passwordCredential
                );
                assignCredentials(credentials, managedConnectionFactory);
                connectionFactory = managedConnectionFactory.createConnectionFactory();
            } catch (MalformedURLException exception){
                report.addError(
                    "Connection URL '" + connectionURL + "' could not be parsed",
                    exception
                );
                return;
            } catch (ResourceException exception) {
                report.addError(
                    "Connection factory could not be created",
                    exception
                );
                return;
            }
        } else {
            try {
                Factory<ManagedConnectionFactory> managedConnectionFactoryBuilder = BeanFactory.newInstance(
            		resourceAdapter.getManagedConnectionFactoryClass(),
                    configuration
                );
                ManagedConnectionFactory managedConnectionFactory = managedConnectionFactoryBuilder.instantiate();
                assignCredentials(credentials, managedConnectionFactory);
                connectionFactory = credentials.isEmpty() ? managedConnectionFactory.createConnectionFactory(
                ) : managedConnectionFactory.createConnectionFactory(
                    new ShareableConnectionManager(credentials)
                );
            } catch (ClassCastException exception) {
                report.addError(
                    "Managed connection factory class '" +
                    resourceAdapter.getManagedConnectionFactoryClass() +
                    "' must be an instance of '" + ManagedConnectionFactory.class.getName() + "'",
                    exception
                );
                return;
            } catch (ResourceException exception) {
                report.addError(
                    "Connection factory acquisition for '" +
                    resourceAdapter.getManagedConnectionFactoryClass() +
                    "' failed",
                    exception
                );
                return;
            } catch (Exception exception) {
                report.addError(
                    "Managed connection factory class '" +
                    resourceAdapter.getManagedConnectionFactoryClass() +
                    "' can't be instantiated",
                    exception
                );
                return;
            }
        }
        privateContext.bind(id, connectionFactory);
        Reference reference = new LinkRef(PRIVATE_PREFIX + id);
        if(connectionFactory instanceof Referenceable) {
            ((Referenceable)connectionFactory).setReference(reference);
        }
        resourceAdapter.deploy(containerContext, applicationContext, reference);
    }
    

    /**
     * Assign the credentials to a managed connection factory
     * 
     * @param credentials
     * @param to the managed connection factory to which the credentials shall be assigned
     */
    private static void assignCredentials(
        Set<?> credentials,
        ManagedConnectionFactory to
    ){
        for(Object credential : credentials){
            if(credential instanceof PasswordCredential) {
                PasswordCredential passwordCredential = (PasswordCredential) credential; 
                passwordCredential.setManagedConnectionFactory(to);
            }
        }
    }
    
    /**
     * Convert the transaction isolation's string representation
     * 
     * @param transactionIsolation the transaction isolation's string representation
     * @return the transaction isolation object
     * 
     * @throws ResourceException
     */
    private TransactionIsolation toTransactionIsolation(
    	String transactionIsolation
    ){
    	return transactionIsolation == null ?
    		null :
    		TransactionIsolation.valueOf(transactionIsolation);
    }
    
    /**
     * 
     * Create a link reference for a given home or local home object
     * 
     * @param object home or local home object
     * 
     * @return the Reference or <code>null</code> if the object is <code>null</code>
     * 
     * @throws NamingException
     */
    private Reference createStandardReference(
        Object object
    ) throws NamingException{
        if(object == null) {
            return null;
        } else {
            String id = UUIDs.newUUID().toString();
            this.privateContext.bind(id, object);
            return new LinkRef(PRIVATE_PREFIX + id);
        }
    }

    /**
     * Create a proxy reference for a given handler
     * 
     * @param homeInterface
     * @param homeHandler
     * 
     * @return the Reference or <code>null</code> if the object is <code>null</code>
     * 
     * @throws NamingException
     */
    private Reference createProxyReference(
        String homeInterface,
        Object homeHandler
    ) throws NamingException{
        if(homeHandler == null) return null;
        String id = UUIDs.newUUID().toString();
        this.privateContext.bind(id, homeHandler);
        return new ProxyReference(
            homeInterface,
            PRIVATE_PREFIX + id
        );
    }

    /**
     * Create a link reference for a given local home object
     * 
     * @param object local home object
     * 
     * @return the Reference or <code>null</code> if the object is <code>null</code>
     * 
     * @throws NamingException
     */
    private Reference createExplicitReference(
        Object object
    ) throws NamingException{
        if(object == null) {
            return null;
        } else {
            String id = UUIDs.newUUID().toString();
            this.privateContext.bind(id, object);
            return new LinkReference(PRIVATE_PREFIX + id);
        }
    }


    /**
     * Create a module id to be included in a report.
     * 
     * @param module
     * 
     * @return module type, URI and display name if any
     */
    private static String getId(
        Deployment.Module module
    ){
        StringBuilder id = new StringBuilder(
            module instanceof Deployment.ApplicationClient ? "Application Client" : "Module"
        ).append(
            ' '
        ).append(
            module.getModuleURI()
        );
        String displayName = module.getDisplayName();
        return (
                displayName == null ? id : id.append(
                    '('
                ).append(
                    displayName
                ).append(
                    ')'
                )
        ).toString();
    }

    /**
     * Retrieve the lightweight container's mode
     * 
     * @return the lightweight container's mode.
     */
    public static Mode getMode(){
        return LightweightContainer.instance == null ? 
            null :
                LightweightContainer.instance.mode;
    }

    //------------------------------------------------------------------------
    // RMI Server
    //------------------------------------------------------------------------

    /**
     * Retrieve the local host name
     * 
     * @return the local host name
     */
    private static String getLocalHost(
    ){
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    /**
     * Analyze report and throw exception in case of failure
     * 
     * @param reports target report list
     * @param report source report
     * 
     * @return true if the newly appended report is in <code>success</code> state.
     */
    private boolean isSuccess (
        List<Report> reports,
        Report report
    ){
        reports.add(report);
        if(report.isSuccess()){
            SysLog.log(Level.INFO,"Successfully validated|{0}", report);
        } else {
            SysLog.log(Level.WARNING,"Validation failed|{0}", report);
        }
        return report.isSuccess();
    }

    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString(){
        return this.mode.toString();
    }


    //------------------------------------------------------------------------
    // RMI Server
    //------------------------------------------------------------------------

    /**
     * Fork a lightweight container.
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    public static Subprocess fork(
    ) throws IOException, InterruptedException{
        return fork(
            null, // jre
            null, // classpath
            null, // properties 
            System.out, // outputStream
            System.err // exceptionStream
        );
    }

    /**
     * Fork a lightweight container.
     * @param jre the JRE directory. 
     *        Optional, defaults to the "java.home" system property. 
     * @param classpath the class-path. 
     *        Optional, defaults to the "java.class.path" system property. 
     * @param properties the system properties to be set.
     *        Optional, defaults to "org.openmdx.rmi.naming.service" and 
     *        "org.openmdx.rmi.registry.port" retrieved from 
     *        {@link org.openmdx.kernel.naming.Contexts Contexts}.
     * @param outputStream 
     *        The stream obtains data piped from the standard output stream 
     *        of the forked <code>Subprocess</code>; 
     *        or <code>null</code> to discard the data. 
     * @param exceptionStream 
     *        The stream obtains data piped from the error output stream of 
     *        the forked <code>Subprocess</code>; 
     *        or <code>null</code> to discard the data.
     * 
     * @return the LightweightContainer's Process
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    public static Subprocess fork(
        String jre,
        String classpath,
        Map<String, Object> properties,
        OutputStream outputStream,
        OutputStream exceptionStream
    ) throws IOException, InterruptedException {
        Map<String, Object> systemProperties = properties; 
        if(systemProperties == null) {
            systemProperties = new HashMap<String, Object>();
            systemProperties.put(Contexts.REGISTRY_PORT,new Integer(Contexts.getRegistryPort()));
            systemProperties.put(Contexts.NAMING_SERVICE,Contexts.getNamingService());
        }
        return Subprocess.fork(
            jre,
            classpath,
            null,
            systemProperties,
            LightweightContainer.class.getName(),
            null,
            SYNCHRONIZATION,
            outputStream,
            exceptionStream
        );
    }

    public static String newProviderURL(){
    	return new StringBuilder(
            "//"
        ).append(
            getLocalHost()
        ).append(
            ':'
        ).append(
            Contexts.getRegistryPort()
        ).append(
            '/'
        ).append(
            Contexts.getNamingService()
        ).toString();
    }
    
    /**
     * The lightweight container's main method
     * 
     * @param arguments
     */
    public static void main(
        String[] arguments
    ){
        String providerURL = newProviderURL();
        try {
            getInstance(Mode.ENTERPRISE_JAVA_BEAN_SERVER);
            LocateRegistry.createRegistry(
                Contexts.getRegistryPort()
            ).bind(
                Contexts.getNamingService(),
                new Context_1()
            );
            System.out.println(
                SYNCHRONIZATION +
                "Lightweight Container is listening at " +
                providerURL
            );
            System.out.flush();
            SysLog.log(
                Level.INFO,
                "ENTERPRISE_JAVA_BEAN_SERVER is listening at {0}",
                providerURL
            );
        } catch (Exception e) {
            log(
                "Lightweight Container Activation",
                providerURL,
                e
            );
            System.err.print(SYNCHRONIZATION);
            System.err.flush();
            System.exit(ACTIVATION_FAILURE);
        }
    }

    /**
     * Retrieve the Provider URL
     * 
     * @return the provider URL in case of ENTERPRISE_JAVA_BEAN_SERVER;
     * <code>null</code> otherwise
     */
    public String getProviderURL(){
        return this.providerURL;
    }

    /**
     * Retrieve the <code>openmdx:container</code> context
     * 
     * @return the container context
     */
    public Context getContainerContext(){
        return this.containerContext;
    }

    /**
     * java:comp context factory 
     */
    private final InitialContextFactory componentContextFactory;

    /**
     * openmdx:container context factory 
     */
    private final InitialContextFactory containerContextFactory;

    /**
     * openmdx:private context factory 
     */
    private  final InitialContextFactory privateContextFactory;

    /**
     * openmdx:application context factory 
     */
    private final InitialContextFactory applicationContextFactory;

    /**
     * 
     */
    private final Deployment deploymentManager;

    /**
     * 
     */
    private static final String PRIVATE_PREFIX = openmdxURLContextFactory.URL_PREFIX + openmdxURLContextFactory.PRIVATE_CONTEXT + '/';

    /**
     * Context Switcher
     */
    private final ContextSwitcher contextSwitcher;

    /**
     * Container Class Loader
     */
    private final LightweightClassLoader containerClassLoader;

    /**
     * Private Context
     */
    private final Context privateContext;

    /**
     * Container Context
     */
    private final Context containerContext;

    /**
     * The callback handler in case of an application client
     */
    private CallbackHandler callbackHandler = null;

    /**
     * Fork waits until the subprocess has terminated or the synchronization 
     * character has been sent either to the subprocess' error or output 
     * stream.
     */
    private final static Character SYNCHRONIZATION = new Character('\f');

    /**
     * The exit status to be used when the lightweight container can't be 
     * started.
     */
    private static final int ACTIVATION_FAILURE = -1;

    /**
     * Will be set to one of<ol>
     * <li>{@link Mode.ENTERPRISE_APPLICATION_CONTAINER}
     * <li>{@link Mode.ENTERPRISE_APPLICATION_CLIENT}
     * <li>{@link Mode.ENTERPRISE_JAVA_BEAN_CONTAINER}
     * <li>{@link Mode.ENTERPRISE_JAVA_BEAN_SERVER}
     * </ol>
     */
    private final Mode mode;

    /**
     * The provider URL in case of ENTERPRISE_JAVA_BEAN_SERVER
     */
    private final String providerURL;
    
    
    //------------------------------------------------------------------------
    // Class Mode
    //------------------------------------------------------------------------

    /**
     * The Lightweight Container's Mode
     */
    public static enum Mode {

        /**
         * Provides Initial Context Factory for local JNDI access
         */
        ENTERPRISE_APPLICATION_CONTAINER,

        /**
         * Provides Initial Context Factory for java:comp access
         */
        ENTERPRISE_APPLICATION_CLIENT,

        /**
         * Uses Apache's Initial Context Factory
         */
        ENTERPRISE_JAVA_BEAN_CONTAINER,

        /**
         * Provides Initial Context Factory for remote JNDI access
         */
        ENTERPRISE_JAVA_BEAN_SERVER

    }

    //------------------------------------------------------------------------
    // Enum TransactionSupport
    //------------------------------------------------------------------------
    
    /**
     * Transaction Support
     */
    static enum TransactionSupport {

        NoTransaction,
        LocalTransaction,
        XATransaction
        
    }

    
    //------------------------------------------------------------------------
    // Enum TransactionIsolation
    //------------------------------------------------------------------------

    /**
     * Transaction Isolation
     */
    static enum TransactionIsolation {
        
        ReadCommitted(Connection.TRANSACTION_READ_COMMITTED),
        ReadUncommitted(Connection.TRANSACTION_READ_UNCOMMITTED),
        RepeatableRead(Connection.TRANSACTION_REPEATABLE_READ),
        Serializable(Connection.TRANSACTION_SERIALIZABLE);

        TransactionIsolation(
            int value
        ){
            this.value = value;
        }

        /**
         * The transaction isolation value
         */
        private final int value;
        
        /**
         * Retrieve the connection's transaction isolation value
         * 
         * @return the connection's transaction isolation value
         */
        public int value(){
            return this.value;
        }
        
    }

}
