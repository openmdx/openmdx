/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ResourceEnvReferenceDeploymentDescriptor.java,v 1.4 2010/06/04 22:44:59 hburger Exp $
 * Description: lab client
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/04 22:44:59 $
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

public class ResourceEnvReferenceDeploymentDescriptor
  extends AbstractConfiguration
{

  @Override
public void parseXml(
    Element element,
    Report report
  ) {
    name = getElementContent(getUniqueChild(element, "resource-env-ref-name", report));
    type = getElementContent(getUniqueChild(element, "resource-env-ref-type", report));
    type = type.trim();
  }

  @Override
public void parseOpenMdxXml(
    Element element,
    Report report
  ) {
    this.jndiName = getElementContent(getOptionalChild(element, "jndi-name", report));
  }

  public void bindResourceEnvReference(
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
      report.addInfo("Binding resource environment reference: " + this.getName() + " to LinkRef: " + sb.toString());
      Contexts.bind(context, this.getName(), new LinkRef(sb.toString()));
    }
  }

  @Override
public void verify(
    Report report
  ) {
    super.verify(report);

    if (this.getName() == null || this.getName().length() == 0)
    {
      report.addError("no value for 'resource-env-ref-name' defined for resource reference");
    }
    if (this.getType() == null || this.getType().length() == 0)
    {
      report.addError("no value for 'resource-env-ref-type' defined for resource reference " + this.getName());
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

  public String getJndiName(
  ) {
    return this.jndiName;
  }

  private String name = null;
  private String type = null;
  private String jndiName = null;
}
