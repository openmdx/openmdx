/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ConnectorDeploymentDescriptor.java,v 1.3 2010/06/04 22:44:59 hburger Exp $
 * Description: lab client
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/04 22:44:59 $
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

import org.openmdx.kernel.application.deploy.spi.Report;
import org.openmdx.kernel.application.deploy.spi.Deployment.Connector;
import org.openmdx.kernel.application.deploy.spi.Deployment.ResourceAdapter;
import org.w3c.dom.Element;

public class ConnectorDeploymentDescriptor
  extends ModuleDeploymentDescriptor
  implements Connector
{

  public ConnectorDeploymentDescriptor(
    String moduleId,
    ApplicationDeploymentDescriptor owner,
	URL url
  ) {
    super(
        moduleId, 
        owner, 
        url,
        new Report(
            REPORT_CONNECTOR_NAME, 
            REPORT_CONNECTOR_VERSION, 
            url.toString()
        )
    );
  }

  @Override
public void parseXml(
    Element element
  ) {
    if ("connector".equals(element.getTagName()))
    {
      this.parseXml(element, this.report);
    }
    else
    {
      this.report.addError("unexpected root tag found '" + element.getTagName() + "' (expected 'connector')");
    }
  }

  @Override
public void parseXml(
    Element element,
    Report report
  ) {
    this.displayName = this.getElementContent(getOptionalChild(element, "display-name", report));
    this.vendorName = this.getElementContent(getUniqueChild(element, "vendor-name", report));
    this.eisType = this.getElementContent(getUniqueChild(element, "eis-type", report));
    this.resourceAdapterVersion = this.getElementContent(getUniqueChild(element, "version", report));
    this.licenseRequired = (
      this.getElementContent(getOptionalChild(element, "license", report)) == null ?
        null :
        new Boolean(this.getElementContent(getOptionalChild(element, "license", report)))
    );
    Element resourceAdapter = getUniqueChild(element, "resourceadapter", report);
    this.resourceAdapterDeploymentDescriptor = new ResourceAdapterDeploymentDescriptor();
    this.resourceAdapterDeploymentDescriptor.parseXml(resourceAdapter, report);
  }
  
  @Override
public void parseOpenMdxXml(
    Element element
  ) {
    if ("openmdx-connector".equals(element.getTagName()))
    {
      this.parseOpenMdxXml(element, report);
    }
    else
    {
      this.report.addError("unexpected root tag found '" + element.getTagName() + "' (expected 'openmdx-connector')");
    }
  }
  
  @Override
public void parseOpenMdxXml(
    Element element,
    Report report
  ) {
    // resource adapter
    this.resourceAdapterDeploymentDescriptor.parseOpenMdxXml(element, report);
  }
  
  @Override
public Report verify() {
    this.verify(this.report);    
    return this.report;
  }
  
  @Override
public void verify(
    Report report
  ) {
    super.verify(report);
    
    // the resource adapter is verified here because a connector without a
    // correct resource adapter does not make any sense
    if (this.getResourceAdapter() == null)
    {
      report.addError("no value for 'resourceadapter' defined for connector " + this.getModuleURI());
    }
    else
    {
      ((ResourceAdapterDeploymentDescriptor)this.getResourceAdapter()).verify(report);
    }
  }
  
  public String getVendorName(
  ) {
    return this.vendorName;
  }
        
  public String getEisType(
  ) {
    return this.eisType;
  }
        
  public String getResourceadapterVersion(
  ) {
    return this.resourceAdapterVersion;
  }
        
  public Boolean getLicenseRequired(
  ) {
    return this.licenseRequired;
  }
  
  public ResourceAdapter getResourceAdapter(
  ) {
    return this.resourceAdapterDeploymentDescriptor;
  }

  private String vendorName = null;
  private String eisType = null;
  private String resourceAdapterVersion = null;
  private Boolean licenseRequired = null;
  private ResourceAdapterDeploymentDescriptor resourceAdapterDeploymentDescriptor = null;
}
