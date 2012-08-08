/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: EjbReferenceDeploymentDescriptor.java,v 1.3 2010/06/04 22:44:59 hburger Exp $
 * Description: EjbReferenceDeploymentDescriptor
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/04 22:44:59 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
import java.io.IOException;

import javax.naming.Context;
import javax.naming.NamingException;

import org.openmdx.kernel.application.deploy.spi.Report;
import org.w3c.dom.Element;

/**
 * EjbReferenceDeploymentDescriptor
 */
public abstract class EjbReferenceDeploymentDescriptor
  extends AbstractConfiguration {

  public EjbReferenceDeploymentDescriptor(
    ModuleDeploymentDescriptor module
  ) {
    this.module = module;
  }
  
  @Override
public void parseXml(
    Element element,
    Report report
  ) {
    name = getElementContent(getUniqueChild(element, "ejb-ref-name", report));
    type = getElementContent(getUniqueChild(element, "ejb-ref-type", report));
    link = getElementContent(getOptionalChild(element, "ejb-link", report));
  }

  @Override
public void parseOpenMdxXml(
    Element element,
    Report report
  ) {
      //
  }
  
  public abstract void bindEjbReference(
      Context context, 
      Appendable webApplicationContext, 
      Report report
  ) throws NamingException;
  
  public String getName(
  ) {
    return this.name;
  }

  public String getType(
  ) {
    return this.type;
  }

  public String getLink(
  ) {
    return this.link;
  }

  @Override
public void verify(
    Report report
  ) {
    super.verify(report);
    
    if (this.getName() == null || this.getName().length() == 0)
    {
      report.addError("No value for 'ejb-ref-name' defined for ejb reference");
    }
    else if (!this.getName().startsWith(EJB_REF_NAME_PREFIX))
    {
      report.addWarning("By J2EE convention the 'ejb-ref-name' of a reference should start with '" + EJB_REF_NAME_PREFIX + "' (" + this.getName() + ")");   
    }

    if (this.getType() == null || this.getType().length() == 0)
    {
      report.addError("No value for 'ejb-ref-type' defined for ejb reference " + this.getName());
    }
    else if (this.getType().equals(EJB_REF_TYPE_ENTITY))
    {
      report.addError("value 'Entity' for 'ejb-ref-type' is not supported");
    }
    else if (!this.getType().equals(EJB_REF_TYPE_SESSION))
    {
      report.addError("invalid value for 'ejb-ref-type'");
    }
  }

  protected ModuleDeploymentDescriptor getModule(
  ) {
    return this.module;
  }

  protected void bind(
      Appendable webApplicationContext, 
      String name,
      String type,
      CharSequence link
  ) throws NamingException{
      try {
          webApplicationContext.append(
              "\t<Resource\n\t\tname=\""
          ).append(
              name
          ).append(
              "\"\n\t\tauth=\"Container\"\n\t\ttype=\""
          ).append(
              type
          ).append(
              "\"\n\t\tfactory=\"org.openmdx.kernel.naming.tomcat.LinkingObjectFactory\"\n\t\tlinkName=\""
          ).append(
              link
          ).append(
              "\"\n\t/>"
          );
      } catch (IOException exception) {
          throw (NamingException) new NamingException(
              "Web Application Context Amendmend Failure"
          ).initCause(
              exception
          );
      }
  }
  
  private String name;
  private String type;
  private String link;
  private final ModuleDeploymentDescriptor module;
  private static final String EJB_REF_NAME_PREFIX = "ejb/";
  public static final String EJB_REF_TYPE_SESSION = "Session";
  private static final String EJB_REF_TYPE_ENTITY = "Entity";
}
