/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: BeanDeploymentDescriptor.java,v 1.1 2009/01/12 12:49:22 wfro Exp $
 * Description: Bean Deplyment Descriptor
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/12 12:49:22 $
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;

import org.openmdx.kernel.application.configuration.Report;
import org.openmdx.kernel.application.deploy.spi.Deployment.Bean;
import org.openmdx.kernel.naming.Contexts;
import org.w3c.dom.Element;

/**
 * BeanDeploymentDescriptor
 */
@SuppressWarnings("unchecked")
public class BeanDeploymentDescriptor 
extends ComponentDeploymentDescriptor
implements Bean
{

    protected BeanDeploymentDescriptor (
        ModuleDeploymentDescriptor owner,
        URL url, 
        List containerTransaction
    ) {
        super(url);
        this.containerTransaction = containerTransaction;
        this.owner = owner;
    }

    public void parseXml(
        Element element,
        Report report
    ) {
        this.name = getElementContent(getUniqueChild(element, "ejb-name", report));
        report.addInfo("ejb-name=" + this.name);
        this.ejbClass = getElementContent(getUniqueChild(element, "ejb-class", report));

        for(
                Iterator it = getChildrenByTagName(element, "env-entry");
                it.hasNext();
        ) {
            EnvEntryDeploymentDescriptor envEntryDD = new EnvEntryDeploymentDescriptor();
            envEntryDD.parseXml((Element)it.next(), report);
            environmentEntries.add(envEntryDD);
        }

        for(
                Iterator it = getChildrenByTagName(element, "ejb-ref");
                it.hasNext();
        ) {
            EjbRemoteReferenceDeploymentDescriptor ejbRemoteRefDD = new EjbRemoteReferenceDeploymentDescriptor(this.getOwner());
            ejbRemoteRefDD.parseXml((Element)it.next(), report);
            ejbRemoteReferences.put(
                ejbRemoteRefDD.getName(),
                ejbRemoteRefDD
            );
        }

        for(
                Iterator it = getChildrenByTagName(element, "ejb-local-ref");
                it.hasNext();
        ) {
            EjbLocalReferenceDeploymentDescriptor ejbLocalRefDD = new EjbLocalReferenceDeploymentDescriptor(this.getOwner());
            ejbLocalRefDD.parseXml((Element)it.next(), report);
            ejbLocalReferences.put(
                ejbLocalRefDD.getName(),
                ejbLocalRefDD
            );
        }

        for(
                Iterator it = getChildrenByTagName(element, "resource-ref");
                it.hasNext();
        ) {
            ResourceReferenceDeploymentDescriptor resourceRefDD = new ResourceReferenceDeploymentDescriptor();
            resourceRefDD.parseXml((Element)it.next(), report);
            resourceReferences.put(
                resourceRefDD.getName(),
                resourceRefDD
            );
        }

        for(
                Iterator it = getChildrenByTagName(element, "resource-env-ref");
                it.hasNext();
        ) {
            ResourceEnvReferenceDeploymentDescriptor resourceEnvRefDD = new ResourceEnvReferenceDeploymentDescriptor();
            resourceEnvRefDD.parseXml((Element)it.next(), report);
            resourceEnvReferences.put(
                resourceEnvRefDD.getName(),
                resourceEnvRefDD
            );
        }

        if (getOptionalChild(element, "security-role-ref", report) != null)
        {
            report.addInfo("settings for 'security-role-ref' are ignored");
        }

        if (getOptionalChild(element, "security-identity", report) != null)
        {
            report.addInfo("value for 'security-identity' is ignored");
        }
    }

    public void parseOpenMdxXml(
        Element element,
        Report report
    ) {
        this.jndiName = getElementContent(getOptionalChild(element, "jndi-name", report));
        if (this.jndiName != null) { report.addInfo("jndi-name=" + this.jndiName); }
        this.localJndiName = getElementContent(getOptionalChild(element, "local-jndi-name", report));
        if (this.localJndiName != null) { report.addInfo("local-jndi-name=" + this.localJndiName); }

        for(
                Iterator it = getChildrenByTagName(element, "ejb-ref");
                it.hasNext();
        ){
            Element ejbRef = (Element)it.next();
            String ejbRefName = getElementContent(getUniqueChild(ejbRef, "ejb-ref-name", report));
            EjbRemoteReferenceDeploymentDescriptor ejbRemoteRefDD = this.getEjbRemoteReferenceByName(ejbRefName);
            if (ejbRemoteRefDD == null) {
                report.addWarning("ejb-ref '" + ejbRefName + "' found in openmdx-ejb-jar.xml but not in ejb-jar.xml");
            } else {
                ejbRemoteRefDD.parseOpenMdxXml(ejbRef, report);
            }
        }

        for(
                Iterator it = getChildrenByTagName(element, "ejb-local-ref");
                it.hasNext();
        ) {
            Element ejbRef = (Element)it.next();
            String ejbRefName = getElementContent(getUniqueChild(ejbRef, "ejb-ref-name", report));
            EjbLocalReferenceDeploymentDescriptor ejbLocalRefDD = this.getEjbLocalReferenceByName(ejbRefName);
            if (ejbLocalRefDD == null) {
                report.addWarning("ejb-local-ref '" + ejbRefName + "' found in openmdx-ejb-jar.xml but not in ejb-jar.xml");
            } else {
                ejbLocalRefDD.parseOpenMdxXml(ejbRef, report);
            }
        }

        for(
                Iterator it = getChildrenByTagName(element, "resource-ref");
                it.hasNext();
        ) {
            Element resRef = (Element)it.next();
            String resRefName = getElementContent(getUniqueChild(resRef, "res-ref-name", report));
            ResourceReferenceDeploymentDescriptor resRefDD = this.getResourceReferenceByName(resRefName);
            if (resRefDD == null) {
                report.addWarning("resource-ref '" + resRefName + "' found in openmdx-ejb-jar.xml but not in ejb-jar.xml");
            } else {
                resRefDD.parseOpenMdxXml(resRef, report);
            }
        }

        for(
                Iterator it = getChildrenByTagName(element, "resource-env-ref");
                it.hasNext();
        ) {
            Element resEnvRef = (Element)it.next();
            String resEnvRefName = getElementContent(getUniqueChild(resEnvRef, "resource-env-ref-name", report));
            ResourceEnvReferenceDeploymentDescriptor resEnvRefDD = this.getResourceEnvReferenceByName(resEnvRefName);
            if (resEnvRefDD == null) {
                report.addWarning("resource-env-ref '" + resEnvRefName + "' found in openmdx-ejb-jar.xml but not in ejb-jar.xml");
            } else {
                resEnvRefDD.parseOpenMdxXml(resEnvRef, report);
            }
        }

        // Pool Configuration
        Element poolElement = getOptionalChild(element, "pool", report);
        if (poolElement != null)
        {
            String maximumCapacityText = getElementContent(getOptionalChild(poolElement, "maximum-capacity", report));
            try
            {
                this.maximumCapacity = new Integer(maximumCapacityText);
            }
            catch (NumberFormatException e)
            {
                report.addError("Invalid maximum-capacity '" + maximumCapacityText + "' value for pool configuration", e);
            }

            String initialCapacityText = getElementContent(getOptionalChild(poolElement, "initial-capacity", report));
            try
            {
                this.initialCapacity = new Integer(initialCapacityText);
            }
            catch (NumberFormatException e)
            {
                report.addError("Invalid initial-capacity '" + initialCapacityText + "' value for pool configuration", e);
            }

            String maximumWaitText = getElementContent(getOptionalChild(poolElement, "maximum-wait", report));
            try
            {
                this.maximumWait = new Long(maximumWaitText);
            }
            catch (NumberFormatException e)
            {
                report.addError("Invalid maximum-wait '" + maximumWaitText + "' value for pool configuration", e);
            }
        }
    }

    public void verify(
        Report report
    ) {
        super.verify(report);

        if (this.getName() == null || this.getName().length() == 0)
        {
            report.addError("no value for 'ejb-name' defined");
        }
        if (this.getEjbClass() == null || this.getEjbClass().length() == 0)
        {
            report.addError("no value for 'ejb-class' defined for ejb " + this.getName());
        }

        for(
                Iterator it = this.getEjbLocalReferences().iterator();
                it.hasNext();
        ) {
            ((EjbLocalReferenceDeploymentDescriptor)it.next()).verify(report);
        }

        for(
                Iterator it = this.getEjbRemoteReferences().iterator();
                it.hasNext();
        ) {
            ((EjbRemoteReferenceDeploymentDescriptor)it.next()).verify(report);
        }

        for(
                Iterator it = this.getEnvironmentEntries().iterator();
                it.hasNext();
        ) {
            ((EnvEntryDeploymentDescriptor)it.next()).verify(report);
        }

        for(
                Iterator it = this.getResourceReferences().iterator();
                it.hasNext();
        ) {
            ((ResourceReferenceDeploymentDescriptor)it.next()).verify(report);
        }

        for(
                Iterator it = this.getResourceEnvReferences().iterator();
                it.hasNext();
        ) {
            ((ResourceEnvReferenceDeploymentDescriptor)it.next()).verify(report);
        }
    }

    public void populate(
        Context componentContext
    ) throws NamingException{
        Context envContext = Contexts.getSubcontext(componentContext, "env");

        // bind env entries
        for(
                Iterator it = this.getEnvironmentEntries().iterator();
                it.hasNext();
        ) {
            ((EnvEntryDeploymentDescriptor)it.next()).bindEnvEntry(envContext, this.report, null);
        }

        // bind remote ejb references
        for(
                Iterator it = this.getEjbRemoteReferences().iterator();
                it.hasNext();
        ) {
            ((EjbRemoteReferenceDeploymentDescriptor)it.next()).bindEjbReference(envContext, null, this.report);
        }

        // bind local ejb references
        for(
                Iterator it = this.getEjbLocalReferences().iterator();
                it.hasNext();
        ) {
            ((EjbLocalReferenceDeploymentDescriptor)it.next()).bindEjbReference(envContext, null, this.report);
        }

        // bind resource references
        for(
                Iterator it = this.getResourceReferences().iterator();
                it.hasNext();
        ) {
            ((ResourceReferenceDeploymentDescriptor)it.next()).bindResourceReference(envContext, this.report);
        }

        // bind resource environment references
        for(
                Iterator it = this.getResourceEnvReferences().iterator();
                it.hasNext();
        ) {
            ((ResourceEnvReferenceDeploymentDescriptor)it.next()).bindResourceEnvReference(envContext, this.report);
        }
    }

    public void deploy(
        Context containerContext, 
        Context applicationContext, 
        Reference localReference,
        Reference remoteReference
    ) throws NamingException{
        // create entries for external links (links to beans in different J2EE 
        // applications/EARs)
        // these entries are only created if the jndi name/local jndi name is set
        // and the references are not null
        if (this.getJndiName() != null && remoteReference != null)
        {
            Contexts.bind(containerContext, this.getJndiName(), remoteReference);
        }
        if (this.getLocalJndiName() != null && localReference != null)
        {
            Contexts.bind(containerContext, this.getLocalJndiName(), localReference);
        }

        // create entries for internal links (links to beans within the same J2EE
        // application/EAR)
        // these entries are always created, if applicationContext is not null
        // and the references are not null
        if (applicationContext != null)
        {
            if (remoteReference != null)
            {
                String remoteLinkName = this.createUniqueRemoteApplicationContextLink(
                    this.getOwner().getModuleURI(),
                    this.name
                );
                Contexts.bind(applicationContext, remoteLinkName.toString(), remoteReference);
            }

            if (localReference != null)
            {
                String localLinkName = this.createUniqueLocalApplicationContextLink(
                    this.getOwner().getModuleURI(),
                    this.name
                );
                Contexts.bind(applicationContext, localLinkName.toString(), localReference);
            }
        }
    }


    public ModuleDeploymentDescriptor getOwner(
    ) {
        return this.owner;
    }

    public String getEjbClass(
    ) {
        return this.ejbClass;
    }

    public String getJndiName(
    ) {
        return this.jndiName;
    }

    public String getLocalJndiName(
    ) {
        return this.localJndiName;
    }

    public Collection getEnvironmentEntries(
    ) {
        return Collections.unmodifiableCollection(this.environmentEntries);
    }

    public Collection getEjbRemoteReferences(
    ) {
        return Collections.unmodifiableCollection(this.ejbRemoteReferences.values());
    }

    public Collection getEjbLocalReferences(
    ) {
        return Collections.unmodifiableCollection(this.ejbLocalReferences.values());
    }

    public Collection getResourceReferences(
    ) {
        return Collections.unmodifiableCollection(this.resourceReferences.values());
    }

    public Collection getResourceEnvReferences(
    ) {
        return Collections.unmodifiableCollection(this.resourceEnvReferences.values());
    }

    public EjbRemoteReferenceDeploymentDescriptor getEjbRemoteReferenceByName(
        String name
    ) {
        return (EjbRemoteReferenceDeploymentDescriptor)ejbRemoteReferences.get(name);
    }

    public EjbLocalReferenceDeploymentDescriptor getEjbLocalReferenceByName(
        String name
    ) {
        return (EjbLocalReferenceDeploymentDescriptor)ejbLocalReferences.get(name);
    }

    public ResourceReferenceDeploymentDescriptor getResourceReferenceByName(
        String name
    ) {
        return (ResourceReferenceDeploymentDescriptor)resourceReferences.get(name);
    }

    public ResourceEnvReferenceDeploymentDescriptor getResourceEnvReferenceByName(
        String name
    ) {
        return (ResourceEnvReferenceDeploymentDescriptor)resourceEnvReferences.get(name);
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.AssemblyDescriptor#getContainerTransaction()
     */
    public List getContainerTransaction() {
        return this.containerTransaction == null ? 
            null :
                Collections.unmodifiableList(this.containerTransaction);
    }

    public Integer getInitialCapacity(
    ) {
        return this.initialCapacity;
    }

    public Integer getMaximumCapacity(
    ) {
        return this.maximumCapacity;
    }

    public Long getMaximumWait(
    ) {
        return this.maximumWait;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Pool#getMaximumIdle()
     */
    public Integer getMaximumIdle() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Pool#getMinimumEvictableIdleTime()
     */
    public Long getMinimumEvictableIdleTime() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Pool#getMinimumIdle()
     */
    public Integer getMinimumIdle() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Pool#getNumberOfTestsPerEvictionRun()
     */
    public Integer getNumberOfTestsPerEvictionRun() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Pool#getTestOnBorrow()
     */
    public Boolean getTestOnBorrow() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Pool#getTestOnReturn()
     */
    public Boolean getTestOnReturn() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Pool#getTestWhileIdle()
     */
    public Boolean getTestWhileIdle() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.deploy.spi.Deployment.Pool#getTimeBetweenEvictionRuns()
     */
    public Long getTimeBetweenEvictionRuns() {
        // TODO Auto-generated method stub
        return null;
    }

    private String ejbClass = null;
    private String jndiName;
    private String localJndiName;
    private List environmentEntries = new ArrayList();
    private HashMap ejbRemoteReferences = new HashMap();
    private HashMap ejbLocalReferences = new HashMap();
    private HashMap resourceReferences = new HashMap();
    private HashMap resourceEnvReferences = new HashMap();
    private final ModuleDeploymentDescriptor owner;
    protected List containerTransaction;
    private Integer maximumCapacity = null;
    private Integer initialCapacity = null;
    private Long maximumWait = null;
}
