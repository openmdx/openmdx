/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: VerifyingDeploymentManager.java,v 1.22 2008/09/08 12:50:30 hburger Exp $
 * Description: DeploymentManager
 * Revision:    $Revision: 1.22 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/08 12:50:30 $
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
package org.openmdx.kernel.application.deploy.enterprise;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.openmdx.kernel.application.deploy.spi.Deployment;
import org.openmdx.kernel.application.deploy.spi.LightweightClassLoader;
import org.openmdx.kernel.url.protocol.XRI_2Protocols;
import org.openmdx.kernel.xml.EntityMapper;
import org.openmdx.kernel.xml.ValidatingDocumentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * DeploymentManager
 */
public class VerifyingDeploymentManager implements Deployment {

    static {
        final String location = XRI_2Protocols.RESOURCE_PREFIX + "org/openmdx/kernel/application/deploy/";
        //
        // DTDs
        //
        EntityMapper.registerPublicId(
            "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN",
            location + "ejb-jar_1_1.dtd"
        );
        EntityMapper.registerPublicId(
            "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN",
            location + "ejb-jar_2_0.dtd"
        );
        EntityMapper.registerPublicId(
            "-//Sun Microsystems, Inc.//DTD J2EE Application 1.2//EN",
            location + "application_1_2.dtd"
        );
        EntityMapper.registerPublicId(
            "-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN",
            location + "application_1_3.dtd"
        );
        EntityMapper.registerPublicId(
            "-//Sun Microsystems, Inc.//DTD Connector 1.0//EN",
            location + "connector_1_0.dtd"
        );
        EntityMapper.registerPublicId(
            "-//openMDX//DTD Enterprise JavaBeans Extension 1.0//EN",
            location + "openmdx-ejb-jar_1_0.dtd"
        );
        EntityMapper.registerPublicId(
            "-//openMDX//DTD Connector Extension 1.0//EN",
            location + "openmdx-connector_1_0.dtd"
        );
        EntityMapper.registerPublicId(
            "-//Sun Microsystems, Inc.//DTD J2EE Application Client 1.2//EN",
            location + "application-client_1_2.dtd"
        );
        EntityMapper.registerPublicId(
            "-//Sun Microsystems, Inc.//DTD J2EE Application Client 1.3//EN",
            location + "application-client_1_3.dtd"
        );
        EntityMapper.registerPublicId(
            "-//openMDX//DTD J2EE Application Client Extension 1.0//EN",
            location + "openmdx-application-client_1_0.dtd"
        );
        EntityMapper.registerPublicId(
            "-//openMDX//DTD J2EE Application Client Extension 1.1//EN",
            location + "openmdx-application-client_1_1.dtd"
        );
        EntityMapper.registerPublicId(
            "-//openMDX//DTD J2EE Application Client Extension 1.1//EN",
            location + "openmdx-application-client_1_1.dtd"
        );
        EntityMapper.registerSystemId(
            "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN",
            location + "web-app_2_2.dtd"
        );
        EntityMapper.registerSystemId(
            "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN",
            location + "web-app_2_3.dtd"
        );
        //
        // XSDs
        //
        EntityMapper.registerSystemId(
            "http://www.w3.org/2001/xml.xsd",
            location + "xml.xsd"
        );
        EntityMapper.registerSystemId(
            "http://java.sun.com/xml/ns/application_1_4.xsd",
            location + "application_1_4.xsd"
        );
        EntityMapper.registerSystemId(
            "http://java.sun.com/xml/ns/application-client_1_4.xsd",
            location + "application_1_4-client.xsd"
        );
        EntityMapper.registerSystemId(
            "http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd",
            location + "ejb-jar_2_1.xsd"
        );
        EntityMapper.registerSystemId(
            "http://java.sun.com/xml/ns/j2ee_1_4.xsd",
            location + "j2ee_1_4.xsd"
        );
        EntityMapper.registerSystemId(
            "http://www.ibm.com/webservices/xsd/j2ee_web_services_client_1_1.xsd",
            location + "j2ee_web_services_client_1_1.xsd"
        );
        EntityMapper.registerSystemId(
            "http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd",
            location + "web-app_2_4.xsd"
        );                
        EntityMapper.registerSystemId(
            "http://java.sun.com/xml/ns/javaee/javaee_5.xsd",
            location + "javaee_5.xsd"
        );                
        EntityMapper.registerSystemId(
            "http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd",
            location + "web-app_2_5.xsd"
        );                
        EntityMapper.registerSystemId(
            "http://java.sun.com/xml/ns/javaee/application_5.xsd",
            location + "application_5.xsd"
        );                
        EntityMapper.registerSystemId(
            "http://java.sun.com/xml/ns/javaee/application-client_5.xsd",
            location + "application-client_5.xsd"
        );                
        EntityMapper.registerSystemId(
            "http://java.sun.com/xml/ns/javaee/ejb-jar_3_0.xsd",
            location + "ejb-jar_3_0.xsd"
        );                
        EntityMapper.registerSystemId(
            "http://java.sun.com/xml/ns/javaee/javaee_web_services_1_2.xsd",
            location + "javaee_web_services_1_2.xsd"
        );                
        EntityMapper.registerSystemId(
            "http://java.sun.com/xml/ns/javaee/javaee_web_services_client_1_2.xsd",
            location + "javaee_web_services_client_1_2.xsd"
        );                
        EntityMapper.registerSystemId(
            "http://java.sun.com/xml/ns/javaee/jsp_2_1.xsd",
            location + "jsp_2_1.xsd"
        );                
        EntityMapper.registerSystemId(
            "http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd",
            location + "web-app_2_5.xsd"
        );                
        EntityMapper.registerSystemId(
            "http://java.sun.com/xml/ns/javaee/web-facesconfig_1_2.xsd",
            location + "web-facesconfig_1_2.xsd"
        );                
        EntityMapper.registerSystemId(
            "http://java.sun.com/xml/ns/javaee/web-jsptaglibrary_2_1.xsd",
            location + "web-jsptaglibrary_2_1.xsd"
        );                
        EntityMapper.registerSystemId(
            "http://java.sun.com/xml/ns/javaee/javaee_web_services_metadata_handler_2_0.xsd",
            location + "javaee_web_services_metadata_handler_2_0.xsd"
        );                
    }

    public VerifyingDeploymentManager(
    ) throws ParserConfigurationException {
        this.documentBuilder = ValidatingDocumentBuilder.newInstance();
        this.logger = LoggerFactory.getLogger(VerifyingDeploymentManager.class);
    }

    public Application getApplication(
        URL appplicationURL
    ) throws Exception {
        return this.parseEAR(appplicationURL);
    }

    public Connector getConnector(
        URL connectorURL
    ) throws Exception {
        // skip trailing '/'
        String connectorURLExternalForm = connectorURL.toExternalForm();
        while (connectorURLExternalForm.endsWith(NAME_SEPARATOR))
        {
            connectorURLExternalForm = connectorURLExternalForm.substring(0, connectorURLExternalForm.length()-1);
        }

        String moduleID = connectorURLExternalForm.substring(connectorURLExternalForm.lastIndexOf(NAME_SEPARATOR)+1);

        return this.parseConnectorModule(
            connectorURL,
            moduleID,
            null // direct call for module (module is not in an application)
        );
    }

    private ApplicationDeploymentDescriptor parseEAR(
        URL earURL
    ) throws IOException, SAXException, ParserConfigurationException {

        URL applicationXmlUrl = VerifyingDeploymentManager.getNestedArchiveUrl(earURL, APPLICATION_XML);
        this.logger.trace("accessing file '{}'|{}", APPLICATION_XML, applicationXmlUrl);
        Document document = this.documentBuilder.parse(applicationXmlUrl);
        ApplicationDeploymentDescriptor appDD = new ApplicationDeploymentDescriptor(applicationXmlUrl);
        appDD.parseXml(document.getDocumentElement());
        this.logger.trace("EAR detected|{}", appDD.getDisplayName());

        this.logger.trace("The EAR's application client modules|{}", appDD.getApplicationClientModuleURIs());
        for(
            Iterator<String> it = appDD.getApplicationClientModuleURIs().iterator();
            it.hasNext();
        ) {
            String applicationClientModule = it.next();
            if (applicationClientModule.length() > 0)
            {
                this.logger.trace("Parsing application client module|{}", applicationClientModule);
                appDD.addModule(
                    this.parseApplicationClientModule(
                        VerifyingDeploymentManager.getNestedArchiveUrl(earURL, applicationClientModule),
                        applicationClientModule,
                        appDD
                    )
                );
            }
        }

        this.logger.trace("The EAR's connector modules|{}", appDD.getConnectorModuleURIs());
        for(
                Iterator<String> it = appDD.getConnectorModuleURIs().iterator();
                it.hasNext();
        ) {
            String connectorModule = it.next();
            if (connectorModule.length() > 0)
            {
                this.logger.trace("Parsing connector module|{}", connectorModule);
                appDD.addModule(
                    this.parseConnectorModule(
                        VerifyingDeploymentManager.getNestedArchiveUrl(earURL, connectorModule),
                        connectorModule,
                        appDD
                    )
                );
            }
        }

        this.logger.trace("The EAR's EJB modules|{}", appDD.getEjbModuleURIs());
        for(
                Iterator<String> it = appDD.getEjbModuleURIs().iterator();
                it.hasNext();
        ) {
            String ejbModule = it.next();
            if (ejbModule.length() > 0)
            {
                this.logger.trace("Parsing EJB module|{}", ejbModule);
                appDD.addModule(
                    this.parseEjbModule(
                        VerifyingDeploymentManager.getNestedArchiveUrl(earURL, ejbModule),
                        ejbModule,
                        appDD
                    )
                );
            }
        }

        this.logger.trace("The EAR's Web modules|{}", appDD.getWebApplicationURIs());
        for(
            int i = 0, iLimit = appDD.getWebApplicationURIs().size();
            i < iLimit;
            i++
        ){
            String webApplication = (String) appDD.getWebApplicationURIs().get(i);
            if (webApplication.length() > 0) {
                this.logger.trace("Parsing Web module|{}", webApplication);
                String contextRoot = (String) appDD.getContextRoots().get(i);
                try {
                    appDD.addModule(
                        this.parseWebApplication(
                            VerifyingDeploymentManager.getNestedArchiveUrl(earURL, webApplication),
                            webApplication,
                            appDD, 
                            contextRoot
                        )
                    );
                } catch (IOException exception) {
                    this.logger.warn("Could not parse Web module|{}", webApplication);
                }
            }
        }
        
        return appDD;
    }

    private ModuleDeploymentDescriptor parseEjbModule(
        URL moduleURL,
        String moduleID,
        ApplicationDeploymentDescriptor owningApplication
    ) throws IOException, SAXException, ParserConfigurationException {

        // read and parse ejb-jar.xml (mandatory)
        URL ejbJarXmlUrl = VerifyingDeploymentManager.getNestedArchiveUrl(moduleURL, EJB_JAR_XML);
        this.logger.trace("Accessing file '{}'|{}", EJB_JAR_XML, ejbJarXmlUrl);
        Document ejbJarXmlDocument = this.documentBuilder.parse(ejbJarXmlUrl);
        ModuleDeploymentDescriptor moduleDD = new ModuleDeploymentDescriptor(
            moduleID,
            owningApplication,
            ejbJarXmlUrl
        );
        moduleDD.parseXml(ejbJarXmlDocument.getDocumentElement());

        // read and parse openmdx-ejb-jar.xml (optional)
        URL openMdxXmlUrl = VerifyingDeploymentManager.getNestedArchiveUrl(moduleURL, OPENMDX_EJB_JAR_XML);
        this.logger.trace("Accessing file '{}|{}", OPENMDX_EJB_JAR_XML, openMdxXmlUrl);
        try {
            Document openMdxXmlDocument = this.documentBuilder.parse(openMdxXmlUrl);
            moduleDD.parseOpenMdxXml(openMdxXmlDocument.getDocumentElement());
        }
        catch (FileNotFoundException ex) {
            this.logger.debug("Optional file '{}' does not exist for module|{}", OPENMDX_EJB_JAR_XML, moduleURL);
        }

        // set module class path (mandatory)
        moduleDD.setModuleClassPath(new URL[]{moduleURL});

        // read and access manifest file to set application class path (optional)
        moduleDD.setApplicationClassPath(LightweightClassLoader.getManifestClassPath(moduleURL, this.logger));

        return moduleDD;
    }

    private WebApplicationDeploymentDescriptor parseWebApplication(
        URL moduleURL,
        String moduleID,
        ApplicationDeploymentDescriptor owningApplication, 
        String contextRoot
    ) throws IOException, SAXException, ParserConfigurationException {
        // read and parse web.xml (mandatory)
        URL webXmlUrl = VerifyingDeploymentManager.getNestedArchiveUrl(moduleURL, WEB_XML);
        this.logger.trace("Accessing file '{}'|{}", WEB_XML, webXmlUrl);
        Document webXmlDocument = this.documentBuilder.parse(webXmlUrl);
        WebApplicationDeploymentDescriptor moduleDD = new WebApplicationDeploymentDescriptor(
            moduleID,
            owningApplication,
            webXmlUrl, 
            contextRoot
        );
        moduleDD.parseXml(webXmlDocument.getDocumentElement());
        // read and access manifest file to set application class path (optional)
        moduleDD.setApplicationClassPath(LightweightClassLoader.getManifestClassPath(moduleURL, this.logger));
        return moduleDD;
    }

    
    private ConnectorDeploymentDescriptor parseConnectorModule(
        URL moduleURL,
        String moduleID,
        ApplicationDeploymentDescriptor owningApplication
    ) throws IOException, SAXException, ParserConfigurationException {

        // read and parse rar.xml (mandatory)
        URL raXmlUrl = VerifyingDeploymentManager.getNestedArchiveUrl(moduleURL, RA_XML);
        this.logger.trace("Accessing file '{}|{}", RA_XML, raXmlUrl);
        Document rarXmlDocument = this.documentBuilder.parse(raXmlUrl);
        ConnectorDeploymentDescriptor connectorDD = new ConnectorDeploymentDescriptor(moduleID, owningApplication, raXmlUrl);
        connectorDD.parseXml(rarXmlDocument.getDocumentElement());

        // read and parse openmdx-connector.xml (optional)
        URL openMdxXmlUrl = VerifyingDeploymentManager.getNestedArchiveUrl(moduleURL, OPENMDX_CONNECTOR_XML);
        this.logger.trace("Accessing file '{}|{}", OPENMDX_CONNECTOR_XML, openMdxXmlUrl);
        try {
            Document openMdxXmlDocument = this.documentBuilder.parse(openMdxXmlUrl);
            connectorDD.parseOpenMdxXml(openMdxXmlDocument.getDocumentElement());
        }
        catch (FileNotFoundException ex) {
            this.logger.debug("Optional file '{}' does not exist for module|{}", OPENMDX_CONNECTOR_XML, moduleURL.toExternalForm());
        }

        // set module class path (mandatory)
        connectorDD.setModuleClassPath(new URL[]{moduleURL});

        // read and access manifest file to set application class path (optional)
        connectorDD.setApplicationClassPath(LightweightClassLoader.getManifestClassPath(moduleURL, this.logger));

        return connectorDD;
    }

    private ApplicationClientDeploymentDescriptor parseApplicationClientModule(
        URL applicationClientURL,
        String moduleID,
        ApplicationDeploymentDescriptor owningApplication
    ) throws IOException, SAXException, ParserConfigurationException {

        // read and parse rar.xml (mandatory)
        URL appClientXmlUrl = VerifyingDeploymentManager.getNestedArchiveUrl(applicationClientURL, APPLICATION_CLIENT_XML);
        this.logger.trace("Accessing file '{}'|{}", APPLICATION_CLIENT_XML, appClientXmlUrl);
        Document appClientXmlDocument = this.documentBuilder.parse(appClientXmlUrl);
        ApplicationClientDeploymentDescriptor appClientDD = new ApplicationClientDeploymentDescriptor(
            moduleID,
            owningApplication,
            appClientXmlUrl
        );
        appClientDD.parseXml(appClientXmlDocument.getDocumentElement());

        // read and parse openmdx-connector.xml (optional)
        URL openMdxAppClientXmlUrl = VerifyingDeploymentManager.getNestedArchiveUrl(applicationClientURL, OPENMDX_APPLICATION_CLIENT_XML);
        this.logger.trace("Accessing file '{}'|{}", OPENMDX_APPLICATION_CLIENT_XML, openMdxAppClientXmlUrl);
        try {
            Document openMdxAppClientXmlDocument = this.documentBuilder.parse(openMdxAppClientXmlUrl);
            appClientDD.parseOpenMdxXml(openMdxAppClientXmlDocument.getDocumentElement());
        }
        catch (FileNotFoundException ex) {
            this.logger.debug("Optional file '{}' does not exist", OPENMDX_APPLICATION_CLIENT_XML);
        }

        // read and access manifest file to set main class and client class path
        appClientDD.setMainClass(LightweightClassLoader.getManifest(applicationClientURL).getMainAttributes().getValue("Main-Class"));
        appClientDD.setClientClassPath(LightweightClassLoader.getManifestClassPath(applicationClientURL, this.logger));

        // read and access manifest file to set application class path (optional)
        appClientDD.setApplicationClassPath(LightweightClassLoader.getManifestClassPath(applicationClientURL, this.logger));

        return appClientDD;
    }

    /**
     * The component is mandatory.
     * <p>
     * Invalid URLs may therefore be returned in order to lead to an I/O Exception.
     * 
     * @param archiveURL
     * @param fileName
     * 
     * @return the nested archive's URL
     * 
     * @throws MalformedURLException
     */
    public static URL getNestedArchiveUrl(
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

    private final Logger logger;
    private final ValidatingDocumentBuilder documentBuilder;
    private static final String APPLICATION_XML = "META-INF/application.xml";
    private static final String APPLICATION_CLIENT_XML = "META-INF/application-client.xml";
    private static final String EJB_JAR_XML = "META-INF/ejb-jar.xml";
    private static final String WEB_XML = "WEB-INF/web.xml";
    public static final String TOMCAT_WEB_XML = "WEB-INF/tomcat-6-web.xml";
    private static final String RA_XML = "META-INF/ra.xml";
    private static final String OPENMDX_EJB_JAR_XML = "META-INF/openmdx-ejb-jar.xml";
    private static final String OPENMDX_CONNECTOR_XML = "META-INF/openmdx-connector.xml";
    private static final String OPENMDX_APPLICATION_CLIENT_XML = "META-INF/openmdx-application-client.xml";
    private static final String NAME_SEPARATOR = "/";

}
