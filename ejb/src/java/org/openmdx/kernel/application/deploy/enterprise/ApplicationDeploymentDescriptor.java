/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ApplicationDeploymentDescriptor.java,v 1.3 2010/04/09 09:33:38 hburger Exp $
 * Description: Application Deployment Descriptor
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/09 09:33:38 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.kernel.application.deploy.enterprise;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.openmdx.kernel.application.deploy.spi.Report;
import org.openmdx.kernel.application.deploy.spi.Deployment.Application;
import org.openmdx.kernel.application.deploy.spi.Deployment.Bean;
import org.openmdx.kernel.application.deploy.spi.Deployment.Component;
import org.openmdx.kernel.application.deploy.spi.Deployment.SessionBean;
import org.w3c.dom.Element;

/**
 * Application Deployment Descriptor
 */
@SuppressWarnings("unchecked")
public class ApplicationDeploymentDescriptor
    extends AbstractDeploymentDescriptor
    implements Application
{

    public ApplicationDeploymentDescriptor(
        URL earURL, 
        URL applicationURL
    ) {
        super(applicationURL);
        this.earURL = earURL;
        this.expanded = "file".equalsIgnoreCase(applicationURL.getProtocol());
        this.report = new Report(
            REPORT_APPLICATION_NAME, 
            REPORT_APPLICATION_VERSION, 
            applicationURL.toString()
        );
    }

    public void parseXml(
        Element element,
        Report report
    ) {
        this.displayName = getElementContent(getUniqueChild(element, "display-name", report));
        for (
                Iterator it = getChildrenByTagName(element, "module");
                it.hasNext ();
        ) {
            Element module = (Element)it.next();
            Element ejbChild = getOptionalChild(module, "ejb", report);
            if (ejbChild!=null)
            {
                this.ejbModuleURIs.add(this.getElementContent(ejbChild));
            }

            Element connectorChild = getOptionalChild(module, "connector", report);
            if (connectorChild!=null)
            {
                this.connectorModuleURIs.add(this.getElementContent(connectorChild));
            }

            Element javaChild = getOptionalChild(module, "java", report);
            if (javaChild!=null)
            {
                this.applicationClientURIs.add(this.getElementContent(javaChild));
            }

            Element webChild = getOptionalChild(module, "web", report);
            if (webChild != null) {
                this.contextRoots.add(
                    this.getElementContent(
                        getUniqueChild(webChild, "context-root", report)
                    )
                );
                this.webApplicationURIs.add(
                    this.getElementContent(
                        getUniqueChild(webChild, "web-uri", report)
                    )
                );
            }
        }
        // security-role
        for (
                Iterator it = getChildrenByTagName(element, "security-role");
                it.hasNext ();
        ) {
            Element securityRole = (Element)it.next();
            report.addInfo("security roles are not supported; security role declaration '" + securityRole + "' is ignored");
        }    
        this.libraryDirectory = getElementContent(getOptionalChild(element, "library-directory", report));
        if(this.libraryDirectory == null) {
            this.libraryDirectory = "lib";
        }
        if("".equals(libraryDirectory)) {
            setApplicationClassPath(new URL[]{});
            report.addInfo("library-directory search disabled");
        } else try {
            URL libraryURL = VerifyingDeploymentManager.getNestedArchiveUrl(earURL, this.libraryDirectory);
            report.addInfo("library-directory " + libraryDirectory + ": " + libraryURL);
            if(isExpanded()) {
                File libraryDirectory = new File(libraryURL.toURI());
                File[] files = libraryDirectory.listFiles(jarFilter);
                if(files != null) {
                    URL[] urls = new URL[files.length];
                    for(
                        int i = 0;
                        i < files.length;
                        i++
                    ){
                        urls[i] = files[i].toURI().toURL();
                    }
                    setApplicationClassPath(urls);
                } else {
                    setApplicationClassPath(new URL[]{});
                }
            } else {
                File libraryDirectory = new File(this.libraryDirectory);
                ZipInputStream stream = new ZipInputStream(earURL.openStream());
                List<URL> jars = new ArrayList();
                for(
                   ZipEntry entry = stream.getNextEntry();
                   entry != null;
                   entry = stream.getNextEntry()
                ){
                    File candidate = new File(entry.getName());
                    if(
                        libraryDirectory.equals(candidate.getParentFile()) &&
                        candidate.getName().endsWith(".jar")
                    ){
                        jars.add(
                            VerifyingDeploymentManager.getNestedArchiveUrl(earURL, entry.getName())
                        );
                    }
                }
                setApplicationClassPath(
                    jars.toArray(new URL[jars.size()])
                );
            }
        } catch (IOException exception) {
            report.addError("library-directory evaluation failure", exception);
        } catch (URISyntaxException exception) {
            report.addError("library-directory evaluation failure", exception);
        }
    }

    public void parseXml(
        Element element
    ) {
        if ("application".equals(element.getTagName()))
        {
            this.parseXml(element, this.report);
        }
        else
        {
            this.report.addError("unexpected root tag found '" + element.getTagName() + "' (expected 'application')");
        }
    }

    public void parseOpenMdxXml(
        Element element,
        Report report
    ) {
        //
    }  

    public Report verify(
    ) {
        this.verify(this.report);
        return this.report;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.configuration.Configuration#validate()
     */
    public Report validate() {
        return verify();
    }

    public void verify(
        Report report
    ) {
        super.verify(report);

        // check whether the component names inside this module are unique
        Set moduleNames = new HashSet();
        for(
                Iterator it = this.getAllModuleURIs().iterator();
                it.hasNext();
        ) {
            String moduleId = (String)it.next();
            if (moduleNames.contains(moduleId))
            {
                report.addError("module " + moduleId + " must be unique inside application");
            }
            else
            {
                moduleNames.add(moduleId);
            }
        }

        // check referential integrity for internal links (ejb-link)
        // external links (based on JNDI entries) can only be validated
        // at runtime
        for(
                Iterator it = this.getModules().iterator();
                it.hasNext();
        ) {
            ModuleDeploymentDescriptor moduleDD = (ModuleDeploymentDescriptor)it.next();
            for(
                    Iterator it2 = moduleDD.getComponents().iterator();
                    it2.hasNext();
            ) {
                ComponentDeploymentDescriptor componentDD = (ComponentDeploymentDescriptor)it2.next();

                if (componentDD instanceof BeanDeploymentDescriptor)
                {
                    BeanDeploymentDescriptor beanDD = (BeanDeploymentDescriptor)componentDD;

                    // check local ejb references
                    for(
                            Iterator it3 = beanDD.getEjbLocalReferences().iterator();
                            it3.hasNext();
                    ) {
                        EjbReferenceDeploymentDescriptor reference = (EjbReferenceDeploymentDescriptor)it3.next();
                        Bean targetBean = this.checkLinkIntegrity(
                            reference,
                            moduleDD,
                            beanDD,
                            report
                        );

                        if (targetBean != null && targetBean instanceof SessionBean)
                        {
                            SessionBean sessionBean = (SessionBean)targetBean;
                            if(sessionBean.getLocal() == null || sessionBean.getLocal().length() == 0)
                            {
                                report.addError(
                                    "no value 'local' defined for the bean referred by 'ejb-link' " + reference.getLink() + 
                                " (local ejb reference)");
                            }
                            if(sessionBean.getLocalHome() == null || sessionBean.getLocalHome().length() == 0)
                            {
                                report.addError(
                                    "no value 'local-home' defined for the bean referred by 'ejb-link' " + reference.getLink() + 
                                " (local ejb reference)");
                            }
                        }
                    }

                    // check remote ejb references
                    for(
                            Iterator it3 = beanDD.getEjbRemoteReferences().iterator();
                            it3.hasNext();
                    ) {
                        EjbReferenceDeploymentDescriptor reference = (EjbReferenceDeploymentDescriptor)it3.next();
                        Bean targetBean = this.checkLinkIntegrity(
                            reference,
                            moduleDD,
                            beanDD,
                            report
                        );

                        if (targetBean != null && targetBean instanceof SessionBean)
                        { 
                            SessionBean sessionBean = (SessionBean)targetBean;
                            if(sessionBean.getRemote() == null || sessionBean.getRemote().length() == 0)
                            {
                                report.addError(
                                    "no value 'remote' defined for the bean referred by 'ejb-link' " + reference.getLink() + 
                                " (remote ejb reference)");
                            }
                            if(sessionBean.getHome() == null || sessionBean.getHome().length() == 0)
                            {
                                report.addError(
                                    "no value 'home' defined for the bean referred by 'ejb-link' " + reference.getLink() + 
                                " (remote ejb reference)");
                            }
                        }
                    }
                }

            }
        }
    }

    public String getDisplayName(
    ) {
        return this.displayName;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Application#getLibraryDirectory()
     */
    public String getLibraryDirectory() {
        return this.libraryDirectory;
    }

    public URL[] getApplicationClassPath(
    ) {
      return this.applicationClassPath;
    }

    public void setApplicationClassPath(
      URL[] applicationClassPath
    ) {
      this.applicationClassPath = applicationClassPath;
    }

    public List<String> getEjbModuleURIs(
    ) {
        return this.ejbModuleURIs;
    }

    public List<String> getConnectorModuleURIs(
    ) {
        return this.connectorModuleURIs;
    }

    public List<String> getApplicationClientModuleURIs(
    ) {
        return this.applicationClientURIs;
    }

    public List getWebApplicationURIs(
    ) {
        return this.webApplicationURIs;
    }

    public List getContextRoots(
    ) {
        return this.contextRoots;
    }

    public List getAllModuleURIs(
    ) {
        List allModuleIDs = new ArrayList(this.getEjbModuleURIs());
        allModuleIDs.addAll(this.getConnectorModuleURIs());
        allModuleIDs.addAll(this.getApplicationClientModuleURIs());
        allModuleIDs.addAll(this.getWebApplicationURIs());
        return allModuleIDs;
    }

    public List getModules(
    ) {
        return Collections.unmodifiableList(this.modules);
    }

    public void addModule(
        ModuleDeploymentDescriptor moduleDD
    ) {
        this.modules.add(moduleDD);

        for(
                Iterator it = moduleDD.getComponents().iterator();
                it.hasNext();
        ) {
            Component component = (Component)it.next();
            this.addEjb(component.getName(), moduleDD.getModuleURI());
        }
    }

    private ModuleDeploymentDescriptor getModuleById(
        String moduleId
    ) {
        if (moduleId == null) { return null; }
        for (
                Iterator it = this.modules.iterator();
                it.hasNext();
        ) {
            ModuleDeploymentDescriptor module = (ModuleDeploymentDescriptor)it.next();
            if (moduleId.equals(module.getModuleURI()))
            {
                return module;
            }
        }
        // return null, if the module cannot be found
        return null;
    }

    // at the moment only links to session beans are supported
    private BeanDeploymentDescriptor checkLinkIntegrity(
        EjbReferenceDeploymentDescriptor reference,
        ModuleDeploymentDescriptor currentModule,
        BeanDeploymentDescriptor currentBean,
        Report report
    ) {
        BeanDeploymentDescriptor targetBeanDD = null;
        if (reference.getLink() != null)
        {
            if (currentModule.getOwner() == null)
            {
                // no application, links must be unique inside module and cannot be 
                // qualified with module (e.g. myModule.jar#myBean)
                if (reference.getLink().indexOf('#') != -1)
                {
                    report.addError(
                        "value '" + reference.getLink() + "' for 'ejb-link' in ejb reference '" 
                        + reference.getName() + "' cannot be qualified because the scope is" +
                        " limited to the module (defined in bean " + 
                        currentModule.getModuleURI() + "#" + currentBean.getName() + ")"
                    );
                }
                else if (!this.getModulesForEjb(reference.getLink()).contains(currentModule.getModuleURI()))
                {
                    report.addError(
                        "value '" + reference.getLink() + "' for 'ejb-link' in ejb reference '" 
                        + reference.getName() + "' does not link to an ejb in the same module" +
                        " (defined in bean " + 
                        currentModule.getModuleURI() + "#" + currentBean.getName() + ")"
                    );
                }
                else
                {
                    targetBeanDD = currentModule.getComponentByName(reference.getLink());
                }
            }
            else
            {
                // there is an application
                int indexOfHash = reference.getLink().indexOf('#');
                if (indexOfHash == -1)
                {
                    // if link does not point to ejb in local module, then the link target
                    // bean must be unique
                    Set modulesForEjb = this.getModulesForEjb(reference.getLink());
                    if (modulesForEjb == null){
                        report.addWarning(
                            "value '" + reference.getLink() + "' for 'ejb-link' in ejb reference '" 
                            + reference.getName() + "' refers to an EJB missing in the enterprise application"
                        );
                    } else if (
                            !modulesForEjb.contains(currentModule.getModuleURI()) &&
                            modulesForEjb.size() != 1
                    ) {
                        report.addError(
                            "value '" + reference.getLink() + "' for 'ejb-link' in ejb reference '" 
                            + reference.getName() + "' is ambiguous (defined in bean " + 
                            currentModule.getModuleURI() + "#" + currentBean.getName() + ")"
                        );
                    }
                }
                else
                {
                    // ejb-link points to a bean in another module
                    String linkModuleName = reference.getLink().substring(0, indexOfHash);
                    String linkBeanName = reference.getLink().substring(indexOfHash+1);

                    ModuleDeploymentDescriptor linkModuleDD = this.getModuleById(linkModuleName);
                    if (linkModuleDD == null || linkModuleDD.getComponentByName(linkBeanName) == null)
                    {
                        report.addError(
                            "value '" + reference.getLink() + "' for 'ejb-link' in ejb reference '" 
                            + reference.getName() + "' is invalid (defined in bean " + 
                            currentModule.getModuleURI() + "/" + currentBean.getName() + ")"
                        );
                    }
                    else
                    {
                        targetBeanDD = linkModuleDD.getComponentByName(linkBeanName);
                        if (
                                EjbReferenceDeploymentDescriptor.EJB_REF_TYPE_SESSION.equals(reference.getType()) &&
                                !(targetBeanDD instanceof SessionBeanDeploymentDescriptor)
                        )
                        {
                            report.addError("ejb reference '" + reference.getName() + "' is not linked to a session bean");
                        }
                    }
                }
            }
        }
        return targetBeanDD;
    }

    private void addEjb(
        String ejbName,
        String moduleId
    ) {
        Set modules = (Set)this.owningModulesForEjb.get(ejbName);
        if (modules == null)
        {
            modules = new HashSet();
            this.owningModulesForEjb.put(ejbName, modules);
        }
        modules.add(moduleId);
    }

    public Set getModulesForEjb(
        String ejbName
    ) {
        return (Set)this.owningModulesForEjb.get(ejbName);
    }

    /**
     * Retrieve expanded.
     *
     * @return Returns the expanded.
     */
    public final boolean isExpanded() {
        return this.expanded;
    }

    private Report report = null;
    private List<String> ejbModuleURIs = new ArrayList<String>();
    private List webApplicationURIs = new ArrayList();
    private List contextRoots = new ArrayList();
    private List<String> connectorModuleURIs = new ArrayList<String>();
    private List<String> applicationClientURIs = new ArrayList<String>();
    private List modules = new ArrayList();
    private String displayName = null;
    private String libraryDirectory;
    private URL[] applicationClassPath;
    private final boolean expanded;
    private final URL earURL;
    private Map owningModulesForEjb = new HashMap();

    private final static FilenameFilter jarFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar");
        }
    };
    
}
