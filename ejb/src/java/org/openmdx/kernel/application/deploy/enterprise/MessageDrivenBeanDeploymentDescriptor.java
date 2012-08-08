/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: MessageDrivenBeanDeploymentDescriptor.java,v 1.1 2009/01/12 12:49:22 wfro Exp $
 * Description: lab client
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/12 12:49:22 $
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
import org.openmdx.kernel.application.deploy.spi.Deployment.MessageDrivenBean;
import org.w3c.dom.Element;

@SuppressWarnings("unchecked")
public class MessageDrivenBeanDeploymentDescriptor
  extends BeanDeploymentDescriptor
  implements MessageDrivenBean
{
  public MessageDrivenBeanDeploymentDescriptor(
    ModuleDeploymentDescriptor owningModule,
	URL url, List containerTransaction
  ) {
    super(owningModule, url, containerTransaction);
  }

  public void parseXml(
    Element element,
    Report report
  ) {
    super.parseXml(element, report);

    this.transactionType = getElementContent(getUniqueChild(element, "transaction-type", report));
    this.messageSelector = getElementContent(getOptionalChild(element, "message-selector", report));
    this.acknowledgeMode = getElementContent(getOptionalChild(element, "acknowledge-mode", report));
    Element messageDrivenDestination = getOptionalChild(element, "message-driven-destination", report);
    if (messageDrivenDestination != null)
    {
      this.messageDrivenDestinationType = getElementContent(getUniqueChild(messageDrivenDestination, "destination-type", report));
      this.messageDrivenDestinationSubscriptionDurability = getElementContent(getOptionalChild(messageDrivenDestination, "subscription-durability", report));
    }
  }

  public void verify(
    Report report
  ) {
    super.verify(report);

    if (this.getTransactionType() == null || this.getTransactionType().length() == 0)
    {
      report.addError("no 'transaction-type' defined for message driven bean " + this.getName());
    }
    else if (
      !TRANSACTION_TYPE_CONTAINER.equals(this.getTransactionType()) &&
      !TRANSACTION_TYPE_BEAN.equals(this.getTransactionType())
    )
    {
      report.addError("no valid value '" + this.transactionType + "' for 'transaction-type' defined for message driven bean " + this.getName());
    }
    
    if (
      this.acknowledgeMode != null &&
      !this.acknowledgeMode.equals(ACKNOWLEDGE_MODE_AUTO_ACKNOWLEDGE) && 
      !this.acknowledgeMode.equals(ACKNOWLEDGE_MODE_DUPS_OK_ACKNOWLEDGE)
    ) {
      report.addError("no valid value '" + this.acknowledgeMode + "' for 'acknowledge-mode' defined for message driven bean " + this.getName());
    }

    if (
      this.messageDrivenDestinationType != null &&
      !this.messageDrivenDestinationType.equals(MESSAGE_DRIVEN_DESTINATION_TYPE_QUEUE) && 
      !this.messageDrivenDestinationType.equals(MESSAGE_DRIVEN_DESTINATION_TYPE_TOPIC)
    ) {
      report.addError("no valid value '" + this.messageDrivenDestinationType + "' for 'destination-type' defined for message driven bean " + this.getName());
    }

    if (
      this.messageDrivenDestinationSubscriptionDurability != null &&
      !this.messageDrivenDestinationSubscriptionDurability.equals(MESSAGE_DRIVEN_DESTINATION_SUBSCRIPTION_DURABILITY_DURABLE) && 
      !this.messageDrivenDestinationSubscriptionDurability.equals(MESSAGE_DRIVEN_DESTINATION_SUBSCRIPTION_DURABILITY_NON_DURABLE)
    ) {
      report.addError("no valid value '" + this.messageDrivenDestinationSubscriptionDurability + "' for 'subscription-durability' defined for message driven bean " + this.getName());
    }
  }

  public String getMessageSelector(
  ) {
    return this.messageSelector;
  }
  
  public String getAcknowledgeMode(
  ) {
    return this.acknowledgeMode;
  }
  
  public String getTransactionType(
  ) {
    return this.transactionType;
  }
  
  public String getMessageDrivenDestinationType(
  ) {
    return this.messageDrivenDestinationType;
  }
  
  public String getMessageDrivenDestinationSubscriptionDurability(
  ) {
    return this.messageDrivenDestinationSubscriptionDurability;
  }
  
  private String messageSelector = null;
  private String transactionType = null;
  private String acknowledgeMode = null;
  private String messageDrivenDestinationType = null;
  private String messageDrivenDestinationSubscriptionDurability = null;
  private final static String TRANSACTION_TYPE_CONTAINER = "Container";
  private final static String TRANSACTION_TYPE_BEAN = "Bean";
  private final static String ACKNOWLEDGE_MODE_AUTO_ACKNOWLEDGE = "Auto-acknowledge";
  private final static String ACKNOWLEDGE_MODE_DUPS_OK_ACKNOWLEDGE = "Dups-ok-acknowledge";
  private final static String MESSAGE_DRIVEN_DESTINATION_TYPE_QUEUE = "javax.jms.Queue";
  private final static String MESSAGE_DRIVEN_DESTINATION_TYPE_TOPIC = "javax.jms.Topic";
  private final static String MESSAGE_DRIVEN_DESTINATION_SUBSCRIPTION_DURABILITY_DURABLE = "Durable";
  private final static String MESSAGE_DRIVEN_DESTINATION_SUBSCRIPTION_DURABILITY_NON_DURABLE = "NonDurable";
}
