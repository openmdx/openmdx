/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AuthenticationMechanismDeploymentDescriptor.java,v 1.2 2009/09/07 13:03:03 hburger Exp $
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

import org.openmdx.kernel.application.deploy.spi.Report;
import org.openmdx.kernel.application.deploy.spi.Deployment.AuthenticationMechanism;
import org.w3c.dom.Element;

public class AuthenticationMechanismDeploymentDescriptor
  extends AbstractConfiguration
  implements AuthenticationMechanism {

  public AuthenticationMechanismDeploymentDescriptor() {
    super();
  }

  public void parseXml(
    Element element,
    Report report
  ) {
    this.authenticationMechanismType = getElementContent(getUniqueChild(element, "authentication-mechanism-type", report));
    this.credentialInterface = getElementContent(getUniqueChild(element, "credential-interface", report));
  }

  public void parseOpenMdxXml(
    Element element,
    Report report
  ) {
      //
  }

  public void verify(
    Report report
  ) {
    super.verify(report);
        
    if (
      !CREDENTIAL_INTERFACE_PASSWORD_CREDENTIAL.equals(this.getCredentialInterface()) &&
      !CREDENTIAL_INTERFACE_GENERIC_CREDENTIAL.equals(this.getCredentialInterface()) &&
      !CREDENITAL_INTERFACE_GSS_CREDENTIAL.equals(this.getCredentialInterface())
    ) {
      report.addError("illegal value '" + this.getCredentialInterface() + "' specified for credential interface");
    }
  }

  public String getAuthenticationMechanismType() {
    return this.authenticationMechanismType;
  }

  public String getCredentialInterface() {
    return this.credentialInterface;
  }

  private String authenticationMechanismType = null;
  private String credentialInterface = null;
  private final static String CREDENTIAL_INTERFACE_PASSWORD_CREDENTIAL = "javax.resource.spi.security.PasswordCredential";
  private final static String CREDENTIAL_INTERFACE_GENERIC_CREDENTIAL = "javax.resource.spi.security.GenericCredential";
  private final static String CREDENITAL_INTERFACE_GSS_CREDENTIAL = "org.ietf.jgss.GSSCredential";

}
