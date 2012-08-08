/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ComponentDeploymentDescriptor.java,v 1.4 2008/01/13 21:37:33 hburger Exp $
 * Description: lab client
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/13 21:37:33 $
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

import javax.naming.Context;
import javax.naming.NamingException;

import org.openmdx.kernel.application.configuration.Configuration;
import org.openmdx.kernel.application.configuration.Report;
import org.w3c.dom.Element;

public abstract class ComponentDeploymentDescriptor
  extends AbstractConfiguration
  implements Configuration
{

  public ComponentDeploymentDescriptor(
    URL url
  ) {
    super();
    this.report = new Report(
      REPORT_EJB_COMPONENT_NAME, 
      REPORT_EJB_VERSION, 
	  url.toString()
    );
  }

  public void parseXml(
    Element element
  ) {
    this.parseXml(element, this.report);
  }
  
  public void parseOpenMdxXml(
    Element element
  ) {
    this.parseOpenMdxXml(element, this.report);
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
  }
  
  public abstract void populate(
  	Context componentContext
  ) throws NamingException;
  
  public String getName(
  ) {
    return this.name;
  }

  protected String name = null;
  protected Report report = null;
}
