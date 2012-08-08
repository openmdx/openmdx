/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SessionBeanDecorator.java,v 1.3 2010/06/04 22:45:00 hburger Exp $
 * Description: lab client
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/04 22:45:00 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.kernel.application.deploy.lightweight;

import org.openmdx.kernel.application.deploy.spi.Report;
import org.openmdx.kernel.application.deploy.spi.Deployment.SessionBean;


public class SessionBeanDecorator
  extends BeanDecorator<SessionBean>
  implements SessionBean
{

  public SessionBeanDecorator(
    SessionBean delegate
  ) {
    super(delegate);
  }

  public String getHome() {
    return super.delegate.getHome();
  }

  public String getRemote() {
    return super.delegate.getRemote();
  }

  public String getLocalHome() {
    return super.delegate.getLocalHome();
  }

  public String getLocal() {
    return super.delegate.getLocal();
  }

  public String getSessionType() {
    return super.delegate.getSessionType();
  }

  public String getTransactionType() {
    return super.delegate.getTransactionType();
  }

  public String getHomeClass(
  ) {
    // do validation before accessing this property's value to ensure that
    // either the configured value or the default value is returned
    this.validate();
    
    return this.homeClass;
  }

  public String getLocalHomeClass(
  ) {
    // do validation before accessing this property's value to ensure that
    // either the configured value or the default value is returned
    this.validate();
    
    return this.localHomeClass;
  }

  @Override
public Report validate(
  ) {
    if (!this.isValidated())
    {
      Report report = super.validate();
      if ("Stateful".equals(super.delegate.getSessionType()))
      {
        report.addError("stateful session beans are not supported at the moment");
      }

      this.homeClass = super.delegate.getHomeClass();
      if (this.homeClass == null && this.getHome() != null)
      {
        // derive home class from home if possible
        this.homeClass = this.mapHomeToHomeClass(this.getHome());
        if (this.homeClass != null)
        {
          report.addInfo("unset attribute 'HomeClass' was overriden with default value " + this.homeClass);
        }
        else
        {
          report.addWarning("no default home class defined for home " + this.getHome());
        }
      }

      this.localHomeClass = super.delegate.getLocalHomeClass();
      if (this.localHomeClass != null) report.addInfo("Deprecated attribute 'LocalHomeClass' ignored");

      return report;
    }
    else
    {
      return super.validate();
    }
  }

  /**
   * Applies default mapping of home interface to home class. If there is no 
   * default mapping, the null value will be returned.
   * 
   * @param home home interface
   * @return home class (if a default mapping is available, otherwise the null value is returned)
   */
  private String mapHomeToHomeClass(
    String home
  ) {
  	return "org.openmdx.application.dataprovider.transport.ejb.cci.Dataprovider_1Home".equals(home) ?
  		"org.openmdx.application.dataprovider.transport.ejb.lightweight.Dataprovider_1HomeImpl" :
        null;
  }

  private String homeClass = null;
  private String localHomeClass = null;
  
}
