/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ResourceReferenceDeploymentDescriptor.java,v 1.1 2009/01/12 12:49:22 wfro Exp $
 * Description: lab client
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/12 12:49:22 $
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

import org.openmdx.kernel.application.configuration.Report;
import org.openmdx.kernel.naming.Contexts;
import org.openmdx.kernel.naming.container.openmdx.openmdxURLContextFactory;
import org.w3c.dom.Element;

public class ResourceReferenceDeploymentDescriptor
  extends AbstractConfiguration
{

  public void parseXml(
    Element element,
    Report report
  ) {
    name = getElementContent(getUniqueChild(element, "res-ref-name", report));
    type = getElementContent(getUniqueChild(element, "res-type", report));
    type = type.trim();
    auth = getElementContent(getUniqueChild(element, "res-auth", report));
    sharingScope = getElementContent(getOptionalChild(element, "res-sharing-scope", report));
  }

  public void parseOpenMdxXml(
    Element element,
    Report report
  ) {
    this.jndiName = getElementContent(getOptionalChild(element, "jndi-name", report));
  }

  public void bindResourceReference(
    Context context,
        Report report
  ) throws NamingException {
    if (this.getJndiName() != null && this.getJndiName().length() != 0)
    {
      // external link
        StringBuilder sb = new StringBuilder(
          openmdxURLContextFactory.URL_PREFIX
      ).append(
          openmdxURLContextFactory.CONTAINER_CONTEXT
      ).append(
          '/'
      ).append(
          this.getJndiName()
      );
      report.addInfo("Binding resource reference: " + this.getName() + " to LinkRef: " + sb.toString());
      Contexts.bind(context, this.getName(), new LinkRef(sb.toString()));
    }
  }

  public void verify(
    Report report
  ) {
    super.verify(report);

    if (this.getName() == null || this.getName().length() == 0)
    {
      report.addError("no value for 'res-ref-name' defined for resource reference");
    }
    if (this.getType() == null || this.getType().length() == 0)
    {
      report.addError("no value for 'res-type' defined for resource reference " + this.getName());
    }

    if (
      this.getSharingScope() != null &&
      !(SHARING_SCOPE_SHAREABLE.equals(this.getSharingScope()) || SHARING_SCOPE_UNSHAREABLE.equals(this.getSharingScope())))
    {
      report.addError("invalid value " + this.getSharingScope() + " for 'res-sharing-scope' defined for resource reference " + this.getName());
    }

    if (
      !AUTH_APPLICATION.equals(this.getAuth()) &&
      !AUTH_CONTAINER.equals(this.getAuth())
    ) {
      report.addError("invalid value " + this.getAuth() + " for 'res-auth' defined for resource reference " + this.getName());
    }
  }

  public String getName(
  ) {
    return this.name;
  }

  public String getType(
  ) {
    return this.type;
  }

  public String getSharingScope(
  ) {
    return this.sharingScope;
  }

  public String getAuth(
  ) {
    return this.auth;
  }

  public String getJndiName(
  ) {
    return this.jndiName;
  }

  private String name = null;
  private String type = null;
  private String auth = null;
  private String sharingScope = null;
  private String jndiName = null;
  private static final String SHARING_SCOPE_SHAREABLE = "Shareable";
  private static final String SHARING_SCOPE_UNSHAREABLE = "Unshareable";
  private static final String AUTH_APPLICATION = "Application";
  private static final String AUTH_CONTAINER = "Container";
}
