/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: EjbRemoteReferenceDeploymentDescriptor.java,v 1.2 2009/09/07 13:03:03 hburger Exp $
 * Description: lab client
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/07 13:03:03 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */

package org.openmdx.kernel.application.deploy.enterprise;
import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.NamingException;

import org.openmdx.kernel.application.deploy.spi.Report;
import org.openmdx.kernel.naming.Contexts;
import org.openmdx.kernel.naming.container.openmdx.openmdxURLContextFactory;
import org.w3c.dom.Element;

public class EjbRemoteReferenceDeploymentDescriptor
  extends EjbReferenceDeploymentDescriptor
{

  public EjbRemoteReferenceDeploymentDescriptor(
    ModuleDeploymentDescriptor module
  ) {
    super(module);
  }

  public String getRemote(
  ) {
    return this.remote;
  }

  public String getHome(
  ) {
    return this.home;
  }

  public String getJndiName(
  ) {
    return this.jndiName;
  }

  public void parseXml(
    Element element,
    Report report
  ) {
    super.parseXml(element, report);
    this.home = getElementContent(getUniqueChild(element, "home", report));
    this.remote = getElementContent(getUniqueChild(element, "remote", report));
  }

  public void parseOpenMdxXml(
    Element element,
    Report report
  ) {
    super.parseOpenMdxXml(element, report);
    this.jndiName = getElementContent(getOptionalChild(element, "jndi-name", report));
  }

  public void bindEjbReference(
      Context context,
      Appendable webApplicationContext, 
      Report report
  ) throws NamingException {
      StringBuilder link = new StringBuilder(
      openmdxURLContextFactory.URL_PREFIX
    );  
    // distinguish between external and internal links
    if (this.getLink() != null && this.getLink().length() != 0) {
      // internal link
      link.append(
          openmdxURLContextFactory.APPLICATION_CONTEXT
      ).append(
          '/'
      );

      // distinguish between links within the same module and links
      // that reference other modules within the same J2EE application
      int indexOfHash = this.getLink().indexOf('#');
      if (indexOfHash == -1) {
        ApplicationDeploymentDescriptor appDD = this.getModule().getOwner();
        // check whether the target bean is defined locally (i.e. in the same module)
        if (appDD.getModulesForEjb(this.getLink()).contains(this.getModule().getModuleURI())) {
            link.append(
            this.createUniqueRemoteApplicationContextLink(
              this.getModule().getModuleURI(),
              this.getLink()
            )
          );
        } else {
          // bean is not defined in local module
          // precondition: link is unique (has been checked by verify operation)
            link.append(
            this.createUniqueRemoteApplicationContextLink(
              (String)appDD.getModulesForEjb(this.getLink()).iterator().next(),
              this.getLink()
            )
          );
        }
      } else {
        // link that references another module within the same J2EE application
        // e.g. MyModule#MyBean
          link.append(
          this.createUniqueRemoteApplicationContextLink(
            this.getLink().substring(0, indexOfHash),
            this.getLink().substring(indexOfHash+1)
          )
        );
      }
    } else if (this.getJndiName() != null && this.getJndiName().length() != 0) {
      // external link
        link.append(
          openmdxURLContextFactory.CONTAINER_CONTEXT
      ).append(
          '/'
      ).append(
          this.getJndiName()
      );
    } else {
      return;  
    }
    report.addInfo("Link remote reference " + this.getName() + " to " + link);
    if(context != null) {
        Contexts.bind(context, this.getName(), new LinkRef(link.toString()));
    }
    if(webApplicationContext != null) bind(
        webApplicationContext,
        getName(),
        getHome(),
        link
    );
  }

  public void verify(
    Report report
  ) {
    super.verify(report);

    if (this.getRemote() == null || this.getRemote().length() == 0)
    {
      report.addError("No value for 'remote' defined for remote ejb reference " + this.getName());
    }
    if (this.getHome() == null || this.getHome().length() == 0)
    {
      report.addError("No value for 'home' defined for remote ejb reference " + this.getName());
    }
    if (
      (this.getLink() == null || this.getLink().length() == 0) &&
      (this.getJndiName() == null || this.getJndiName().length() == 0)
    ) {
      report.addError("Either 'ejb-link' or 'jndi-name' must be defined for remote ejb reference " + this.getName());
    }
    if (this.getLink() != null && this.getLink().length() != 0 && this.getJndiName() != null && this.getJndiName().length() != 0)
    {
      report.addWarning("Remote ejb reference " + this.getName() + " has values defined for 'ejb-link' and 'jndi-name' => jndi entry is ignored");
    }
  }

  private String home;
  private String remote;
  private String jndiName;

}
