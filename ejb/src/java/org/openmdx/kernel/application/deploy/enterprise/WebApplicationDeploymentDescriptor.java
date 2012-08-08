/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: WebApplicationDeploymentDescriptor.java,v 1.2 2009/09/07 13:03:03 hburger Exp $
 * Description: Web Application Deployment Descriptor
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/07 13:03:03 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.NamingException;

import org.openmdx.kernel.application.deploy.spi.Report;
import org.openmdx.kernel.application.deploy.spi.Deployment.WebApplication;
import org.w3c.dom.Element;

public class WebApplicationDeploymentDescriptor
  extends ModuleDeploymentDescriptor
  implements WebApplication
{

  public WebApplicationDeploymentDescriptor(
    String moduleId,
    ApplicationDeploymentDescriptor owner,
	URL url, 
	String contextRoot
  ) {
    super(
        moduleId, 
        owner, 
        url,
        new Report(
            REPORT_WEB_APPLICATION_NAME, 
            REPORT_WEB_APPLICATION_VERSION, 
            url.toString()
        )
    );
    this.contextRoot = contextRoot;
  }

  public void parseXml(
    Element element
  ) {
    if ("web-app".equals(element.getTagName()))
    {
      this.parseXml(element, this.report);
    }
    else
    {
      this.report.addError("unexpected root tag found '" + element.getTagName() + "' (expected 'web-app')");
    }
  }

  public void parseXml(
    Element element,
    Report report
  ) {
      this.displayName = this.getElementContent(
          getOptionalChild(element, "display-name", report)
      );
      for(
          Iterator<Element> it = getChildrenByTagName(element, "ejb-ref");
          it.hasNext();
      ) {
          EjbRemoteReferenceDeploymentDescriptor ejbRemoteRefDD = new EjbRemoteReferenceDeploymentDescriptor(this);
          ejbRemoteRefDD.parseXml(it.next(), report);
          this.ejbRemoteReferences.put(
              ejbRemoteRefDD.getName(),
              ejbRemoteRefDD
          );
      }
      for(
          Iterator<Element> it = getChildrenByTagName(element, "ejb-local-ref");
          it.hasNext();
      ) {
          EjbLocalReferenceDeploymentDescriptor ejbLocalRefDD = new EjbLocalReferenceDeploymentDescriptor(this);
          ejbLocalRefDD.parseXml(it.next(), report);
          this.ejbLocalReferences.put(
              ejbLocalRefDD.getName(),
              ejbLocalRefDD
          );
      }
  }
  
  public Report verify() {
    this.verify(this.report);    
    return this.report;
  }
  
  /* (non-Javadoc)
   * @see org.openmdx.kernel.application.deploy.spi.Deployment.WebApplication#populate(java.util.Map)
   */
  public void populate(
      Appendable webApplicationContext
  ) throws NamingException {
      //
      // bind remote EJB references
      //
      for(
        Iterator<EjbRemoteReferenceDeploymentDescriptor> i = this.getEjbRemoteReferences().iterator();
        i.hasNext();
      ){
            i.next().bindEjbReference(
                null, 
                webApplicationContext, 
                this.report
            );
      }
      //
      // bind local EJB references
      //
      for(
        Iterator<EjbLocalReferenceDeploymentDescriptor> i = this.getEjbLocalReferences().iterator();
        i.hasNext();
      ){
            i.next().bindEjbReference(
                null, 
                webApplicationContext, 
                this.report
             );
      }
  }

  /**
   * Retrieve contextRoot.
   *
   * @return Returns the contextRoot.
   */
  public final String getContextRoot() {
      return this.contextRoot;
  }

  public Collection<EjbRemoteReferenceDeploymentDescriptor> getEjbRemoteReferences(
  ) {
      return Collections.unmodifiableCollection(this.ejbRemoteReferences.values());
  }

  public Collection<EjbLocalReferenceDeploymentDescriptor> getEjbLocalReferences(
  ) {
      return Collections.unmodifiableCollection(this.ejbLocalReferences.values());
  }

  public EjbRemoteReferenceDeploymentDescriptor getEjbRemoteReferenceByName(
      String name
  ) {
      return ejbRemoteReferences.get(name);
  }

  public EjbLocalReferenceDeploymentDescriptor getEjbLocalReferenceByName(
      String name
  ) {
      return ejbLocalReferences.get(name);
  }
  
  final private String contextRoot;
  final private Map<String,EjbRemoteReferenceDeploymentDescriptor> ejbRemoteReferences = new HashMap<String,EjbRemoteReferenceDeploymentDescriptor>();
  final private Map<String,EjbLocalReferenceDeploymentDescriptor> ejbLocalReferences = new HashMap<String,EjbLocalReferenceDeploymentDescriptor>();

}
