/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ApplicationClientDeploymentDescriptor.java,v 1.2 2009/09/07 13:03:03 hburger Exp $
 * Description: lab client
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/07 13:03:03 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.application.deploy.enterprise;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;

import org.openmdx.kernel.application.deploy.spi.Report;
import org.openmdx.kernel.application.deploy.spi.Deployment.ApplicationClient;
import org.openmdx.kernel.naming.Contexts;
import org.w3c.dom.Element;

@SuppressWarnings("unchecked")
public class ApplicationClientDeploymentDescriptor
  extends ModuleDeploymentDescriptor
  implements ApplicationClient
{

  public ApplicationClientDeploymentDescriptor(
      String moduleId,
      ApplicationDeploymentDescriptor owner,
   	  URL url
  ) {
    super(
        moduleId,
        owner,
        url,
        new Report(
            REPORT_EJB_CLIENT_NAME, 
            REPORT_APPLICATION_VERSION, 
            url.toString()
        )
    );
  }

  public void parseXml(
    Element element
  ) {
    if ("application-client".equals(element.getTagName()))
    {
      this.parseXml(element, this.report);
    }
    else
    {
      this.report.addError("unexpected root tag found '" + element.getTagName() + "' (expected 'application-client')");
    }
  }
    
  public void parseXml(
    Element element, 
    Report report
  ) {
	this.displayName = getElementContent(getUniqueChild(element, "display-name", report));

    for(
      Iterator<Element> it = getChildrenByTagName(element, "env-entry");
      it.hasNext();
    ) {
      EnvEntryDeploymentDescriptor envEntryDD = new EnvEntryDeploymentDescriptor();
      envEntryDD.parseXml(it.next(), report);
      environmentEntries.add(envEntryDD);
    }

    for(
      Iterator<Element> it = getChildrenByTagName(element, "ejb-ref");
      it.hasNext();
    ) {
        EjbRemoteReferenceDeploymentDescriptor ejbRemoteRefDD = new EjbRemoteReferenceDeploymentDescriptor(this);
      ejbRemoteRefDD.parseXml(it.next(), report);
      ejbRemoteReferences.put(
        ejbRemoteRefDD.getName(),
        ejbRemoteRefDD
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
	this.callbackHandler = getElementContent(getOptionalChild(element, "callback-handler", report));

  }

  public void parseOpenMdxXml(
    Element element
  ) {
    if ("openmdx-application-client".equals(element.getTagName()))
    {
      this.parseOpenMdxXml(element, this.report);
    }
    else
    {
      this.report.addError("unexpected root tag found '" + element.getTagName() + "' (expected 'openmdx-application-client')");
    }
  }
    
	public void parseOpenMdxXml(
    Element element, 
    Report report
  ) {
	for(
      Iterator it = getChildrenByTagName(element, "ejb-ref");
      it.hasNext();
    )
    {
      Element ejbRef = (Element)it.next();
      String ejbRefName = getElementContent(getUniqueChild(ejbRef, "ejb-ref-name", report));
      EjbRemoteReferenceDeploymentDescriptor ejbRemoteRefDD = this.getEjbRemoteReferenceByName(ejbRefName);
      if (ejbRemoteRefDD == null)
      {
        report.addWarning("ejb-ref '" + ejbRefName + "' found in openmdx-application-client.xml but not in application-client.xml");
      }
      else
      {
        ejbRemoteRefDD.parseOpenMdxXml(ejbRef, report);
      }
    }

    for(
      Iterator it = getChildrenByTagName(element, "resource-ref");
      it.hasNext();
    )
    {
      Element resRef = (Element)it.next();
      String resRefName = getElementContent(getUniqueChild(resRef, "res-ref-name", report));
      ResourceReferenceDeploymentDescriptor resRefDD = this.getResourceReferenceByName(resRefName);
      if (resRefDD == null)
      {
        report.addWarning("resource-ref '" + resRefName + "' found in openmdx-application-client.xml but not in application-client.xml");
      }
      else
      {
        resRefDD.parseOpenMdxXml(resRef, report);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.openmdx.kernel.application.deploy.spi.Deployment.ApplicationClient#populate(javax.naming.Context, java.util.Map)
   */
  public void populate(
    Context applicationClientContext,
    Map applicationClientEnvironment
  ) throws NamingException{
    Context envContext = Contexts.getSubcontext(applicationClientContext, "env");

    // bind env entries
    for(
      Iterator<EnvEntryDeploymentDescriptor> it = this.getEnvironmentEntries().iterator();
      it.hasNext();
    ) {
      it.next().bindEnvEntry(envContext, this.report, applicationClientEnvironment);
    }

    // bind remote ejb references
    for(
      Iterator<EjbRemoteReferenceDeploymentDescriptor> it = this.getEjbRemoteReferences().iterator();
      it.hasNext();
    ) {
        EjbRemoteReferenceDeploymentDescriptor ejbRef = it.next();
      ejbRef.bindEjbReference(
        envContext,
        null, this.report
      );
    }

    // bind resource references
    for(
      Iterator<ResourceReferenceDeploymentDescriptor> it = this.getResourceReferences().iterator();
      it.hasNext();
    ) {
      ResourceReferenceDeploymentDescriptor resRef = it.next();
      resRef.bindResourceReference(
        envContext,
        this.report
      );
    }
    
  }

  /* (non-Javadoc)
   * @see org.openmdx.kernel.application.deploy.spi.Deployment.ApplicationClient#deploy(javax.naming.Context, javax.naming.Reference)
   */
  public void deploy(
      Context containerContext, 
      Reference reference
  ) throws NamingException {
      Contexts.bind(containerContext, "main", reference);
  }

  public String getDisplayName(
  ) {
    return this.displayName;
  }

  public String getCallbackHandler(
  ) {
    return this.callbackHandler;
  }

  public URL[] getModuleClassPath(
  ) {
    return this.clientClassPath;
  }

  public void setClientClassPath(
    URL[] clientClassPath
  ) {
    this.clientClassPath = clientClassPath;
  }
  
  public String getMainClass(
  ) {
    return this.mainClass;
  }
  
  public void setMainClass(
    String mainClass
  ) {
    this.mainClass = mainClass;
  }
  
  public Report verify() {
    this.verify(this.report);
    return this.report;
  }

  public Report validate() {
    return verify();
  }

  public Collection<EnvEntryDeploymentDescriptor> getEnvironmentEntries(
  ) {
     return Collections.unmodifiableCollection(this.environmentEntries);
  }

  public Collection<EjbRemoteReferenceDeploymentDescriptor> getEjbRemoteReferences(
  ) {
     return Collections.unmodifiableCollection(this.ejbRemoteReferences.values());
  }

  public Collection<ResourceReferenceDeploymentDescriptor> getResourceReferences(
  ) {
     return Collections.unmodifiableCollection(this.resourceReferences.values());
  }

  public EjbRemoteReferenceDeploymentDescriptor getEjbRemoteReferenceByName(
    String name
  ) {
     return ejbRemoteReferences.get(name);
  }

  public ResourceReferenceDeploymentDescriptor getResourceReferenceByName(
    String name
  ) {
     return resourceReferences.get(name);
  }

  private String displayName = null;
  private String callbackHandler = null;
  private String mainClass = null;
  private URL[] clientClassPath = null;
  private List<EnvEntryDeploymentDescriptor> environmentEntries = new ArrayList<EnvEntryDeploymentDescriptor>();
  private Map<String,EjbRemoteReferenceDeploymentDescriptor> ejbRemoteReferences = new HashMap<String,EjbRemoteReferenceDeploymentDescriptor>();
  private Map<String,ResourceReferenceDeploymentDescriptor> resourceReferences = new HashMap<String,ResourceReferenceDeploymentDescriptor>();
  
}
