/*
 * ====================================================================
 * Project:     openMDX/OpenEJB, http://www.openmdx.org/
 * Name:        $Id: OpenEJBDeploymentManager.java,v 1.12 2009/03/31 18:07:45 wfro Exp $
 * Description: OpenEJBDeploymentManager
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/31 18:07:45 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
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
package org.openmdx.openejb.deployment;

/**
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.parsers.ParserConfigurationException;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.url.URLInputStream;
import org.openmdx.kernel.url.protocol.XRI_2Protocols;
import org.openmdx.kernel.xml.ValidatingDocumentBuilder;
import org.openmdx.openejb.logging.JdkLogStreamFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class OpenEJBDeploymentManager implements DeploymentManager {

    /**
     * Constructor
     * 
     * @param mode 
     */
    OpenEJBDeploymentManager(
        OpenEJBDeploymentFactory.Mode mode
    ) {
        try {
            this.locale = null;
            this.mode = mode;
            this.targets = new Target[]{new Type()};
            this.documentBuilder = ValidatingDocumentBuilder.newInstance();
        }
        catch(Exception e) {
            throw new RuntimeServiceException(e);
        }
    }

    //-----------------------------------------------------------------------
    private static URL getNestedArchiveUrl(
        URL archiveURL,
        String fileName
    ) throws MalformedURLException {
        URL containerURL = archiveURL;
        if("file".equalsIgnoreCase(containerURL.getProtocol())){
            File archiveFile = new File(containerURL.getFile());
            if(archiveFile.isDirectory()){
                File nestedFile = new File(archiveFile, fileName);
                if(! nestedFile.exists() && fileName.length() > 4){
                    String ending = fileName.substring(fileName.length()-4).toLowerCase();
                    if(".jar".equals(ending)||".war".equals(ending)||".rar".equals(ending)){
                        File alternateFile = new File(archiveFile, fileName.substring(0, fileName.length()-4));
                        if(alternateFile.isDirectory()) nestedFile = alternateFile;
                    }
                }
                return nestedFile.toURI().toURL();
            } else {
                containerURL = archiveFile.toURI().toURL();
            }
        }
        return new URL(XRI_2Protocols.ZIP_PREFIX + containerURL + XRI_2Protocols.ZIP_SEPARATOR + fileName);
    }
    
    //-----------------------------------------------------------------------
    private ConnectorDeploymentDescriptor getConnector(
        URL connectorURL
    ) throws IOException, SAXException, ParserConfigurationException {
        // skip trailing '/'
        String connectorURLExternalForm = connectorURL.toExternalForm();
        while (connectorURLExternalForm.endsWith(NAME_SEPARATOR)) {
            connectorURLExternalForm = connectorURLExternalForm.substring(0, connectorURLExternalForm.length()-1);
        }
        String moduleID = connectorURLExternalForm.substring(connectorURLExternalForm.lastIndexOf(NAME_SEPARATOR)+1);
        return this.parseConnectorModule(
            connectorURL,
            moduleID
        );
    }

    //-----------------------------------------------------------------------
    private ConnectorDeploymentDescriptor parseConnectorModule(
        URL moduleURL,
        String moduleID
    ) throws IOException, SAXException, ParserConfigurationException {
        // Read and parse rar.xml (mandatory)
        URL raXmlUrl = getNestedArchiveUrl(moduleURL, RA_XML);
        logger.log(Level.FINEST, "Accessing file '{0}|{1}", new Object[]{RA_XML, raXmlUrl});
        Document rarXmlDocument = this.documentBuilder.parse(raXmlUrl);
        ConnectorDeploymentDescriptor connectorDD = new ConnectorDeploymentDescriptor(moduleID, raXmlUrl);
        connectorDD.parseXml(rarXmlDocument.getDocumentElement());
        // Parse OPENEJB_CONNECTOR_XML
        URL openEjbXmlUrl = getNestedArchiveUrl(moduleURL, OPENEJB_CONNECTOR_XML);
        logger.log(Level.FINEST, "Accessing file '{0}|{1}", new Object[]{OPENMDX_CONNECTOR_XML, openEjbXmlUrl});
        try {
            Document openMdxXmlDocument = this.documentBuilder.parse(openEjbXmlUrl);
            connectorDD.parseOpenMdxXml(openMdxXmlDocument.getDocumentElement());
        }
        catch (FileNotFoundException e0) {
            logger.log(Level.FINEST, "Optional file '{0}' does not exist for module|{1}", new Object[]{OPENEJB_CONNECTOR_XML, moduleURL.toExternalForm()});
            // Fallback to OPENMDX_CONNECTOR_XML
            URL openMdxXmlUrl = getNestedArchiveUrl(moduleURL, OPENMDX_CONNECTOR_XML);
            logger.log(Level.FINEST, "Accessing file '{0}|{1}", new Object[]{OPENMDX_CONNECTOR_XML, openMdxXmlUrl});
            try {
                Document openMdxXmlDocument = this.documentBuilder.parse(openMdxXmlUrl);
                connectorDD.parseOpenMdxXml(openMdxXmlDocument.getDocumentElement());
            }
            catch (FileNotFoundException e1) {
                logger.log(Level.FINEST, "Optional file '{0}' does not exist for module|{1}", new Object[]{OPENMDX_CONNECTOR_XML, moduleURL.toExternalForm()});
            }
        }
        return connectorDD;
    }
    
    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#createConfiguration(javax.enterprise.deploy.model.DeployableObject)
     */
    public DeploymentConfiguration createConfiguration(
        DeployableObject obj
    ) throws InvalidModuleException {
        /// Nothing done yet
        return null;
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#distribute(javax.enterprise.deploy.spi.Target[], java.io.File, java.io.File)
     */
    public ProgressObject distribute(
        Target[] targetList, 
        File moduleArchive,
        File deploymentPlan
    ) throws IllegalStateException {
        try {
            URL moduleURL = moduleArchive.toURL();
            ModuleType type = getType(moduleArchive);
            return distribute(
                targetList,
                type,
                moduleURL,
                null // deploymentPlan not yet supported
            );
        } catch (MalformedURLException exception) {
            throw new UnsupportedOperationException(
                "The module archive can't be reference as URL",
                exception
            );
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Derives the module type from the archive file's ending
     * 
     * @param archiveFile
     * 
     * @return the archive's module type
     */
    protected ModuleType getType(
        File archiveFile
    ){
        String archiveName = archiveFile.getName().toLowerCase();
        for(int i = 0; i < 5; i++) {
            ModuleType candidate = ModuleType.getModuleType(i);
            if(archiveName.endsWith(candidate.getModuleExtension())) {
                return candidate;
            }
        }
        return null;
    }
    
    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#distribute(javax.enterprise.deploy.spi.Target[], java.io.InputStream, java.io.InputStream)
     */
    public ProgressObject distribute(
        Target[] targetList,
        InputStream moduleArchive,
        InputStream deploymentPlan
    ) throws IllegalStateException {
        throw new UnsupportedOperationException(
            "This deprecated methiod is not supported"
        );
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#distribute(javax.enterprise.deploy.spi.Target[], javax.enterprise.deploy.shared.ModuleType, java.io.InputStream, java.io.InputStream)
     */
    public ProgressObject distribute(
        final Target[] targetList, 
        final ModuleType type,
        final InputStream moduleArchive,
        final InputStream deploymentPlan
    ) throws IllegalStateException {
        if(moduleArchive instanceof URLInputStream) {
            URL moduleURL = ((URLInputStream)moduleArchive).getURL(); 
            return distribute(
                targetList,
                type,
                moduleURL,
                null // deploymentPlan not yet supported
            );
        } 
        else {
            throw new UnsupportedOperationException(
                "Upload not yet supported by, the module archive should be of type " + 
                URLInputStream.class.getName() + " instead of "+ 
                moduleArchive.getClass().getName()
            );
        }
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#distribute(javax.enterprise.deploy.spi.Target[], javax.enterprise.deploy.shared.ModuleType, java.io.InputStream, java.io.InputStream)
     */
    protected ProgressObject distribute(
        final Target[] targetList, 
        final ModuleType type,
        final URL moduleArchive,
        final URL deploymentPlan
    ) throws IllegalStateException {
        TargetModuleID[] targetModuleIDs = new TargetModuleID[targetList.length];
        for(int i = 0; i < targetModuleIDs.length; i++){
            targetModuleIDs[i] = new Unit(
                targetList[i],
                type,
                moduleArchive
            );
        }
        return new Progress(
            ActionType.EXECUTE,
            CommandType.DISTRIBUTE,
            StateType.COMPLETED,
            targetModuleIDs
        );
    }
    
    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#getAvailableModules(javax.enterprise.deploy.shared.ModuleType, javax.enterprise.deploy.spi.Target[])
     */
    public TargetModuleID[] getAvailableModules(
        ModuleType moduleType,
        Target[] targetList
    ) throws TargetException, IllegalStateException {
        /// Nothing done yet
        return null;
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#getCurrentLocale()
     */
    public Locale getCurrentLocale() {
        return this.locale == null ? getDefaultLocale() : this.locale;
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#getDConfigBeanVersion()
     */
    public DConfigBeanVersionType getDConfigBeanVersion(
    ) {
        /// Nothing done yet
        return null;
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#getDefaultLocale()
     */
    public Locale getDefaultLocale() {
        return Locale.getDefault();
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#getNonRunningModules(javax.enterprise.deploy.shared.ModuleType, javax.enterprise.deploy.spi.Target[])
     */
    public TargetModuleID[] getNonRunningModules(
        ModuleType moduleType,
        Target[] targetList
    ) throws TargetException, IllegalStateException {
        /// Nothing done yet
        return null;
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#getRunningModules(javax.enterprise.deploy.shared.ModuleType, javax.enterprise.deploy.spi.Target[])
     */
    public TargetModuleID[] getRunningModules(
        ModuleType moduleType,
        Target[] targetList
    ) throws TargetException, IllegalStateException {
        /// Nothing done yet
        return null;
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#getSupportedLocales()
     */
    public Locale[] getSupportedLocales() {
        return Locale.getAvailableLocales();
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#getTargets()
     */
    public Target[] getTargets() throws IllegalStateException {
        return this.targets;
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#isDConfigBeanVersionSupported(javax.enterprise.deploy.shared.DConfigBeanVersionType)
     */
    public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType version) {
        return false;
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#isLocaleSupported(java.util.Locale)
     */
    public boolean isLocaleSupported(Locale locale) {
        for(Locale candidate : Locale.getAvailableLocales()) {
            if(candidate.equals(locale)) {
                return true;
            }
        }
        return false;
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#isRedeploySupported()
     */
    public boolean isRedeploySupported() {
        return false;
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#redeploy(javax.enterprise.deploy.spi.TargetModuleID[], java.io.File, java.io.File)
     */
    public ProgressObject redeploy(
        TargetModuleID[] moduleIDList,
        File moduleArchive, 
        File deploymentPlan
    ) throws UnsupportedOperationException, IllegalStateException {
        /// Nothing done yet
        return null;
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#redeploy(javax.enterprise.deploy.spi.TargetModuleID[], java.io.InputStream, java.io.InputStream)
     */
    public ProgressObject redeploy(
        TargetModuleID[] moduleIDList,
        InputStream moduleArchive, 
        InputStream deploymentPlan
    ) throws UnsupportedOperationException, IllegalStateException {
        /// Nothing done yet
        return null;
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#release()
     */
    public void release() {
        /// Nothing done yet
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#setDConfigBeanVersion(javax.enterprise.deploy.shared.DConfigBeanVersionType)
     */
    public void setDConfigBeanVersion(
        DConfigBeanVersionType version
    ) throws DConfigBeanVersionUnsupportedException {
        /// Nothing done yet
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#setLocale(java.util.Locale)
     */
    public void setLocale(
        Locale locale
    ) throws UnsupportedOperationException {
        this.locale = locale;
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#start(javax.enterprise.deploy.spi.TargetModuleID[])
     */
    public ProgressObject start(
        TargetModuleID[] moduleIDList
    ) throws IllegalStateException {
        Type target = null;
        SortedMap<Integer,URL> applications = new TreeMap<Integer,URL>();
        SortedMap<Integer,URL> resourceAdapters = new TreeMap<Integer,URL>();
        logger.log(Level.FINE, "Processing modules {0}", Arrays.asList(moduleIDList));
        for(int i = 0; i < moduleIDList.length; i++) {
            if( moduleIDList[i] instanceof Unit) {
                Unit targetModuleID = (Unit)moduleIDList[i];
                if(target == null) {
                    Target candidate = targetModuleID.getTarget();
                    if(candidate instanceof Type) {
                        target = (Type) candidate;
                    } else throw new IllegalArgumentException(
                        "Unsupported target: " + target    
                    );
                } else if(!target.equals(targetModuleID.getTarget())) throw new IllegalArgumentException(
                    "Deployment to different targets at once is not supported"
                );
                ModuleType type = targetModuleID.getType(); 
                URL url = targetModuleID.getURL();
                if(ModuleType.EAR == type) {
                    applications.put(i,url);
                } 
                else if (ModuleType.RAR == type) {
                    resourceAdapters.put(i,url);
                } 
                else throw new IllegalArgumentException(
                    "Unsupported target module id type:" + type
                );
            } 
            else throw new IllegalArgumentException(
                "Unsupported target module id: " + moduleIDList[i]
            );
        }
        if(target == null) {
            return new Progress(
                ActionType.EXECUTE,
                CommandType.START,
                StateType.COMPLETED,
                new TargetModuleID[]{}
            );
        }
        switch(target.getMode()) {
            case ENTERPRISE_APPLICATION_CONTAINER: try {
                
                logger.log(Level.FINE, "Starting OpenEJB in mode {0}", target.getMode());
                List<TargetModuleID> completed = new ArrayList<TargetModuleID>();
                Map<String,String> deployments = new HashMap<String,String>();
                for(Map.Entry<Integer,URL> resourceAdapter: resourceAdapters.entrySet()){
                    logger.log(Level.FINE, "Processing {0}", resourceAdapter.getValue());
                    // TODO Do it this way because OpenEJB 3.1 can not deploy RARs. Maybe
                    // in a future version this step can be simplified, i.e. ra.xml does
                    // not have to be parsed here
                    try {
                        ConnectorDeploymentDescriptor connectorDD = this.getConnector(resourceAdapter.getValue());
                        // For each RAR create a property of the form
                        // properties.setProperty("jdbc_helloworld", "new://Resource?type=DataSource&JdbcDriver=org.postgresql.Driver&JdbcUrl=jdbc:postgresql://localhost/helloworld&UserName=postgres&Password=changeit&JtaManaged=true");
                        String moduleId = connectorDD.getResourceAdapter().getConnectionFactoryJndiName();
                        String moduleUrl =
                            "new://Resource?" +
                            "type=DataSource&" +
                            "JdbcDriver=" + connectorDD.getResourceAdapter().getConfigProperties().get("Driver") + "&" +
                            "JdbcUrl=" + connectorDD.getResourceAdapter().getConfigProperties().get("ConnectionURL") + "&" +
                            "UserName=" + connectorDD.getResourceAdapter().getConfigProperties().get("UserName") + "&" +
                            "Password=" + connectorDD.getResourceAdapter().getConfigProperties().get("Password") + "&" +
                            "JtaManaged=true";
                        logger.log(Level.FINE, "Adding resource id={0}, url={1}", new Object[]{moduleId, moduleUrl});
                        completed.add(moduleIDList[resourceAdapter.getKey()]);                    
                        deployments.put(moduleId, moduleUrl);
                    }
                    catch(Exception e) {
                        logger.log(
                        	Level.WARNING,
                            "Deployment of resource adapter {0} failed: {1}",
                            new Object[]{resourceAdapter.getValue(), e}
                        );                        
                    }
                }
                for(Map.Entry<Integer,URL> application : applications.entrySet()){
                    logger.log(Level.FINE, "Processing {0}", application.getValue());
                    // For each RAR create a property of the form
                    // properties.setProperty("helloworld.ear", "new://Deployments?jar=/home/openejb/helloworld/src/ear/helloworld.ear");
                    String moduleId = new File(application.getValue().getFile()).getName();
                    String moduleUrl =
                        "new://Deployments?" +
                        "jar=" + (application.getValue().getProtocol().equals("file") ? URLEncoder.encode(new File(application.getValue().getFile()).getAbsolutePath(), "UTF-8") : application.getValue().toString());
                    logger.log(Level.FINE, "Adding module id={0}, url={1}", new Object[]{moduleId, moduleUrl});
                    completed.add(moduleIDList[application.getKey()]);                    
                    deployments.put(moduleId, moduleUrl);
                }
                // Launch OpenEJB
                System.getProperties().setProperty(
                    Context.INITIAL_CONTEXT_FACTORY, 
                    "org.apache.openejb.client.LocalInitialContextFactory"
                );
                System.getProperties().setProperty(
                    "openejb.log.factory", 
                    JdkLogStreamFactory.class.getName()
                );
                Properties properties = new Properties();
                properties.putAll(deployments);
                try {
                    logger.info("Starting OpenEJB");
                    new InitialContext(properties);
                }
                catch(Exception e) {
                    new ServiceException(e).log();
                }
                return new Progress(
                    ActionType.EXECUTE,
                    CommandType.START,
                    StateType.COMPLETED,
                    completed.toArray(new TargetModuleID[completed.size()])                         
                );
            } 
            catch (Exception exception) {
                return new Progress(
                    ActionType.EXECUTE,
                    CommandType.START,
                    StateType.FAILED,
                    new TargetModuleID[]{}
                );
            }
            default: throw new IllegalArgumentException(
                "Unsupported target mode: " + target.getMode()
            );                
        }
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#stop(javax.enterprise.deploy.spi.TargetModuleID[])
     */
    public ProgressObject stop(
        TargetModuleID[] moduleIDList
    ) throws IllegalStateException {
        /// Nothing done yet
        return null;
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see javax.enterprise.deploy.spi.DeploymentManager#undeploy(javax.enterprise.deploy.spi.TargetModuleID[])
     */
    public ProgressObject undeploy(
        TargetModuleID[] moduleIDList
    ) throws IllegalStateException {
        /// Nothing done yet
        return null;
    }
        
    //------------------------------------------------------------------------
    // Class Type
    //------------------------------------------------------------------------
    
    /**
     * Lightweight Target
     */
    class Type implements Target {

        OpenEJBDeploymentFactory.Mode getMode(){
            return OpenEJBDeploymentManager.this.mode;
        }
        
        public String getDescription() {
            switch(getMode()) {
                case ENTERPRISE_APPLICATION_CONTAINER:
                    return "In-process OpenEJB Container";
                case ENTERPRISE_JAVA_BEAN_SERVER:
                    return "Remote OpenEJB Container";
                default:
                    return toString();
            }
        }

        public String getName() {
            return getMode().name();
        }

    }

    //------------------------------------------------------------------------
    // Class Status
    //------------------------------------------------------------------------
    
    /**
     * Lightweight Deployment Status
     */
    static class Status implements DeploymentStatus {

        Status(
            ActionType action,
            CommandType command,
            StateType initialState
        ){
            this.action = action;
            this.command = command;
            this.state = initialState;
        }

        private final ActionType action;
        
        private final CommandType command;
        
        private StateType state;
        
        public ActionType getAction() {
            return this.action;
        }

        public CommandType getCommand() {
            return this.command;
        }

        public String getMessage() {
            return this.action + " " + this.command + " (" + this.state + ")";
        }

        public StateType getState() {
            return this.state;
        }

        void setState(
            StateType state
        ){
            this.state = state;
        }
        
        public boolean isCompleted() {
            return StateType.COMPLETED == this.state;
        }

        public boolean isFailed() {
            return StateType.FAILED == this.state;
        }

        public boolean isRunning() {
            return StateType.RUNNING == this.state;
        }

    }

    //------------------------------------------------------------------------
    // Class Progress
    //------------------------------------------------------------------------
    
    /**
     * Lightweight Progress Object
     */
    static class Progress implements ProgressObject {
        
        /**
         * Constructor
         * 
         * @param action
         * @param command
         * @param initialState
         */
        Progress(
            ActionType action,
            CommandType command,
            StateType initialState,
            TargetModuleID[] targetModuleIDs
        ){
            this.status = new Status(
                action,
                command,
                initialState
            );
            this.targetModuleIDs = targetModuleIDs;
        }

        final private DeploymentStatus status;

        final private TargetModuleID[] targetModuleIDs;
        
        /* (non-Javadoc)
         * @see javax.enterprise.deploy.spi.status.ProgressObject#addProgressListener(javax.enterprise.deploy.spi.status.ProgressListener)
         */
        public void addProgressListener(ProgressListener pol) {
            // Nothing done yet
        }

        /* (non-Javadoc)
         * @see javax.enterprise.deploy.spi.status.ProgressObject#cancel()
         */
        public void cancel() throws OperationUnsupportedException {
            // Nothing done yet
        }

        /* (non-Javadoc)
         * @see javax.enterprise.deploy.spi.status.ProgressObject#getClientConfiguration(javax.enterprise.deploy.spi.TargetModuleID)
         */
        public ClientConfiguration getClientConfiguration(TargetModuleID id) {
            // Nothing done yet
            return null;
        }

        /* (non-Javadoc)
         * @see javax.enterprise.deploy.spi.status.ProgressObject#getDeploymentStatus()
         */
        public DeploymentStatus getDeploymentStatus() {
            return this.status;
        }

        /* (non-Javadoc)
         * @see javax.enterprise.deploy.spi.status.ProgressObject#getResultTargetModuleIDs()
         */
        public TargetModuleID[] getResultTargetModuleIDs() {
            return this.targetModuleIDs;
        }

        /* (non-Javadoc)
         * @see javax.enterprise.deploy.spi.status.ProgressObject#isCancelSupported()
         */
        public boolean isCancelSupported() {
            return true;
        }

        /* (non-Javadoc)
         * @see javax.enterprise.deploy.spi.status.ProgressObject#isStopSupported()
         */
        public boolean isStopSupported() {
            return false;
        }

        /* (non-Javadoc)
         * @see javax.enterprise.deploy.spi.status.ProgressObject#removeProgressListener(javax.enterprise.deploy.spi.status.ProgressListener)
         */
        public void removeProgressListener(ProgressListener pol) {
            // Nothing done yet
        }

        /* (non-Javadoc)
         * @see javax.enterprise.deploy.spi.status.ProgressObject#stop()
         */
        public void stop() throws OperationUnsupportedException {
            // Nothing done yet
        }

    }
    
    //------------------------------------------------------------------------
    // Class Unit
    //------------------------------------------------------------------------    
    static class Unit implements TargetModuleID {

        /**
         * Constructor
         * 
         * @param target
         * @param url
         */
        Unit(
            Target target,
            ModuleType type,
            URL url
        ){
            this.target = target;
            this.type = type;
            this.url = url;
        }
        
        private final Target target;
        
        private final ModuleType type;
        
        private final URL url;
        
        
        public TargetModuleID[] getChildTargetModuleID(
        ) {
            // Nothing done yet
            return null;
        }

        public String getModuleID() {
            return this.url.toString();
        }

        public TargetModuleID getParentTargetModuleID(
        ) {
            // Nothing done yet
            return null;
        }

        public Target getTarget(
        ) {
            return this.target;
        }

        public String getWebURL(
        ) {
            return null;
        }

        URL getURL(){
            return this.url;
        }
        
        ModuleType getType(){
            return this.type;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return this.url + " (" + this.type + ")";
        }
        
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final String OPENMDX_CONNECTOR_XML = "META-INF/openmdx-connector.xml";
    private static final String OPENEJB_CONNECTOR_XML = "META-INF/openejb-connector.xml";
    private static final String NAME_SEPARATOR = "/";
    private static final String RA_XML = "META-INF/ra.xml";

    private final ValidatingDocumentBuilder documentBuilder;
    private Locale locale;
    private static Logger logger = Logger.getLogger(OpenEJBDeploymentFactory.class.getName());

    final OpenEJBDeploymentFactory.Mode mode;
    private final Target[] targets; 
    
}
