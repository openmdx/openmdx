/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SessionBeanDeploymentDescriptor.java,v 1.5 2005/04/05 11:23:23 hburger Exp $
 * Description: lab client
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/04/05 11:23:23 $
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
import java.util.List;

import org.openmdx.kernel.application.configuration.Report;
import org.openmdx.kernel.application.deploy.spi.Deployment.SessionBean;
import org.w3c.dom.Element;

public class SessionBeanDeploymentDescriptor
  extends BeanDeploymentDescriptor
  implements SessionBean
{
  public SessionBeanDeploymentDescriptor(
    ModuleDeploymentDescriptor owningModule,
	URL url, 
    List containerTransaction
  ) {
    super(owningModule, url, containerTransaction);
  }

  public void parseXml(
    Element element,
    Report report
  ) {
    super.parseXml(element, report);

    this.home = getElementContent(getOptionalChild(element, "home", report));
    this.remote = getElementContent(getOptionalChild(element, "remote", report));
    this.localHome = getElementContent(getOptionalChild(element, "local-home", report));
    this.local = getElementContent(getOptionalChild(element, "local", report));
    this.sessionType = getElementContent(getUniqueChild(element, "session-type", report));
    this.transactionType = getElementContent(getUniqueChild(element, "transaction-type", report));
    if(!"Container".equals(this.transactionType)) {
        if(super.containerTransaction != null && !super.containerTransaction.isEmpty()) report.addWarning(
            "Container transactions ignored for transaction type '" + this.transactionType + "'"
        );
        super.containerTransaction = null;
    }
  }

  public void parseOpenMdxXml(
    Element element,
    Report report
  ) {
    super.parseOpenMdxXml(element, report);

    this.homeClass = getElementContent(getOptionalChild(element, "home-class", report));
    this.localHomeClass = getElementContent(getOptionalChild(element, "local-home-class", report));
  }
  
  public void verify(
    Report report
  ) {
    super.verify(report);

    if ( 
      (this.getRemote() == null || this.getRemote().length() == 0) &&
      (this.getLocal() == null || this.getLocal().length() == 0)
    )
    {
      report.addError("either local or remote interfaces (or both) must be defined for session bean " + this.getName());
    }

    if ( 
      (this.getHome() == null || this.getHome().length() == 0) &&
      (this.getLocalHome() == null || this.getLocalHome().length() == 0)
    )
    {
      report.addError("either local or remote home interfaces (or both) must be defined for session bean " + this.getName());
    }

    if (this.getSessionType() == null || this.getSessionType().length() == 0)
    {
      report.addError("no 'session-type' defined for session bean " + this.getName());
    }
    else if (
      !SESSION_TYPE_STATELESS.equals(this.getSessionType()) &&
      !SESSION_TYPE_STATEFUL.equals(this.getSessionType())
    )
    {
      report.addError("no valid value for 'session-type' defined for session bean " + this.getName());
    }

    if (this.getTransactionType() == null || this.getTransactionType().length() == 0)
    {
      report.addError("no 'transaction-type' defined for session bean " + this.getName());
    }
    else if (
      !TRANSACTION_TYPE_CONTAINER.equals(this.getTransactionType()) &&
      !TRANSACTION_TYPE_BEAN.equals(this.getTransactionType())
    )
    {
      report.addError("no valid value for 'transaction-type' defined for session bean " + this.getName());
    }
  }

  public String getHome(
  ) {
    return this.home;
  }

  public String getRemote(
  ) {
    return this.remote;
  }

  public String getLocal(
  ) {
    return this.local;
  }

  public String getLocalHome(
  ) {
    return this.localHome;
  }

  public String getLocalHomeClass(
  ) {
    return this.localHomeClass;
  }

  public String getHomeClass(
  ) {
    return this.homeClass;
  }

  public String getSessionType(
  ) {
    return this.sessionType;
  }
  
  public String getTransactionType(
  ) {
    return this.transactionType;
  }
  
  private String home = null;
  private String remote = null;
  private String localHome = null;
  private String local = null;
  private String sessionType = null;
  private String transactionType = null;
  private String homeClass = null;
  private String localHomeClass = null;
  private final static String SESSION_TYPE_STATELESS = "Stateless";
  private final static String SESSION_TYPE_STATEFUL = "Stateful";
  private final static String TRANSACTION_TYPE_CONTAINER = "Container";
  private final static String TRANSACTION_TYPE_BEAN = "Bean";
}
