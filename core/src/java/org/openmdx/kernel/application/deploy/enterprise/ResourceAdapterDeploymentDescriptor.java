/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ResourceAdapterDeploymentDescriptor.java,v 1.3 2008/01/13 21:37:34 hburger Exp $
 * Description: lab client
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/13 21:37:34 $
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;

import org.openmdx.kernel.application.configuration.Report;
import org.openmdx.kernel.application.deploy.spi.Deployment.ResourceAdapter;
import org.openmdx.kernel.naming.Contexts;
import org.w3c.dom.Element;

public class ResourceAdapterDeploymentDescriptor
  extends AbstractConfiguration
  implements ResourceAdapter
{
  public ResourceAdapterDeploymentDescriptor() {
    super();
  }

  public void parseXml(
    Element element,
    Report report
  ) {
    // implementation is based on J2EE Connector Architecture 1.0 (not 1.5)!!!
    // i.e. the following properties are defined directly within the resource
    // adapter tag and not within the subtag <outbound-resourceadapter> as 
    // proposed in the 1.5 specification
    this.managedConnectionFactoryClass = getElementContent(getUniqueChild(element, "managedconnectionfactory-class", report));
    this.connectionFactoryInterface = getElementContent(getUniqueChild(element, "connectionfactory-interface", report));
    this.connectionFactoryImplClass = getElementContent(getUniqueChild(element, "connectionfactory-impl-class", report));
    this.connectionInterface = getElementContent(getUniqueChild(element, "connection-interface", report));
    this.connectionImplClass = getElementContent(getUniqueChild(element, "connection-impl-class", report));
    this.transactionSupport = getElementContent(getUniqueChild(element, "transaction-support", report));
    this.reauthenticationSupport = Boolean.valueOf(getElementContent(getUniqueChild(element, "reauthentication-support", report))).booleanValue();

    // config properties
    for (
      Iterator it = getChildrenByTagName(element, "config-property");
      it.hasNext ();
    ) {
      Element configProperty = (Element)it.next();
      String configPropertyName = getElementContent(getUniqueChild(configProperty, "config-property-name", report));
      String configPropertyValue = getElementContent(getUniqueChild(configProperty, "config-property-value", report));
      String configPropertyType = getElementContent(getUniqueChild(configProperty, "config-property-type", report)).trim();
      if ("java.lang.String".equals(configPropertyType))
      {
        this.configProperties.put(configPropertyName, configPropertyValue);
      }
      else if("java.lang.Integer".equals(configPropertyType))
      {
        this.configProperties.put(configPropertyName, new Integer(configPropertyValue));
      }
      else if("java.lang.Long".equals(configPropertyType))
      {
        this.configProperties.put(configPropertyName, new Long(configPropertyValue));
      }
      else if("java.lang.Double".equals(configPropertyType))
      {
        this.configProperties.put(configPropertyName, new Double(configPropertyValue));
      }
      else if("java.lang.Float".equals(configPropertyType))
      {
        this.configProperties.put(configPropertyName, new Float(configPropertyValue));
      }
      else if("java.lang.Byte".equals(configPropertyType))
      {
        this.configProperties.put(configPropertyName, new Byte(configPropertyValue));
      }
      else if("java.lang.Short".equals(configPropertyType))
      {
        this.configProperties.put(configPropertyName, new Short(configPropertyValue));
      }
      else if("java.lang.Boolean".equals(configPropertyType))
      {
        this.configProperties.put(configPropertyName, new Boolean(configPropertyValue));
      }
      else if("java.lang.Character".equals(configPropertyType))
      {
        if (configPropertyValue.length() > 0)
        {
          this.configProperties.put(configPropertyName, new Character(configPropertyValue.charAt(0)));
        }
      }
      else
      {
        // default use java.lang.String
        this.configProperties.put(configPropertyName, configPropertyValue);
      }
    }

    // authentication mechanisms
    for (
      Iterator it = getChildrenByTagName(element, "authentication-mechanism");
      it.hasNext ();
    ) {
      Element authenticationMechanism = (Element)it.next();
      AuthenticationMechanismDeploymentDescriptor authenticationMechanismDD = new AuthenticationMechanismDeploymentDescriptor();
      authenticationMechanismDD.parseXml(authenticationMechanism, report);
      this.authenticationMechanism.add(authenticationMechanismDD);
    }
  }

  public void parseOpenMdxXml(
    Element element,
    Report report
  ) {
    Element resourceAdapterElement = getUniqueChild(element, "resourceadapter", report);
    if (resourceAdapterElement != null)
    {
      this.connectionFactoryJndiName = getElementContent(getUniqueChild(resourceAdapterElement, "connectionfactory-jndi-name", report));

      // Pool Configuration
      Element poolElement = getOptionalChild(resourceAdapterElement, "pool", report);
      if (poolElement != null)
      {
        String maximumCapacityText = getElementContent(getOptionalChild(poolElement, "maximum-capacity", report));
        try
        {
           this.maximumCapacity = new Integer(maximumCapacityText);
        }
        catch (NumberFormatException e)
        {
          report.addError("Invalid maximum-capacity '" + maximumCapacityText + "' value for pool configuration", e);
        }

        String initialCapacityText = getElementContent(getOptionalChild(poolElement, "initial-capacity", report));
        try
        {
           this.initialCapacity = new Integer(initialCapacityText);
        }
        catch (NumberFormatException e)
        {
          report.addError("Invalid initial-capacity '" + initialCapacityText + "' value for pool configuration", e);
        }

        String maximumWaitText = getElementContent(getOptionalChild(poolElement, "maximum-wait", report));
        try
        {
           this.maximumWait = new Long(maximumWaitText);
        }
        catch (NumberFormatException e)
        {
          report.addError("Invalid maximum-wait '" + maximumWaitText + "' value for pool configuration");
        }
      }
    }
  }

  public Map getConfigProperties(
  ) {
    return this.configProperties;
  }

  public void verify(
    Report report
  ) {
    super.verify(report);
        
    if (this.getConnectionFactoryJndiName() == null || this.getConnectionFactoryJndiName().length() == 0)
    {
      report.addError("no value for 'connectionfactory-jndi-name' defined for resource adapter");
    }

    if (this.getTransactionSupport() == null || this.getTransactionSupport().length() == 0)
    {
      report.addError("no value for 'transaction-support' defined for resource adapter");
    }
    else if (
      !TRANSACTION_SUPPORT_NO_TRANSACTION.equals(this.getTransactionSupport()) &&
      !TRANSACTION_SUPPORT_LOCAL_TRANSACTION.equals(this.getTransactionSupport()) &&
      !TRANSACTION_SUPPORT_XA_TRANSACTION.equals(this.getTransactionSupport())
    ) {
      report.addError("illegal value '" + this.getTransactionSupport() + "' specified for transaction support");
    }

    if (this.getManagedConnectionFactoryClass() == null || this.getManagedConnectionFactoryClass().length() == 0)
    {
      report.addError("no value for 'managedconnectionfactory-class' defined for resource adapter");
    }

    if (this.getConnectionFactoryInterface() == null || this.getConnectionFactoryInterface().length() == 0)
    {
      report.addError("no value for 'connectionfactory-interface' defined for resource adapter");
    }

    if (this.getConnectionFactoryImplClass() == null || this.getConnectionFactoryImplClass().length() == 0)
    {
      report.addError("no value for 'connectionfactory-impl-class' defined for resource adapter");
    }

    if (this.getConnectionInterface() == null || this.getConnectionInterface().length() == 0)
    {
      report.addError("no value for 'connection-interface' defined for resource adapter");
    }

    if (this.getConnectionImplClass() == null || this.getConnectionImplClass().length() == 0)
    {
      report.addError("no value for 'connection-impl-class' defined for resource adapter");
    }

    for(
      Iterator it = this.authenticationMechanism.iterator();
      it.hasNext();
    ) {
      ((AuthenticationMechanismDeploymentDescriptor)it.next()).verify(report);
    }
  }

  public String getManagedConnectionFactoryClass(
  ) {
    return this.managedConnectionFactoryClass;
  }
      
  public String getConnectionFactoryInterface(
  ) {
    return this.connectionFactoryInterface;
  }

  public String getConnectionFactoryImplClass(
  ) {
    return this.connectionFactoryImplClass;
  }

  public String getConnectionInterface(
  ) {
    return this.connectionInterface;
  }

  public String getConnectionImplClass(
  ) {
    return this.connectionImplClass;
  }
      
  public String getTransactionSupport(
  ) {
    return this.transactionSupport;
  }

  public String getCredentialInterface(
  ) {
    return this.credentialInterface;
  }
      
  public boolean getReauthenticationSupport(
  ) {
    return this.reauthenticationSupport;
  }
      
  public List getAuthenticationMechanism(
  ) {
    return this.authenticationMechanism;
  }
  
  public String getConnectionFactoryJndiName(
  ) {
    return this.connectionFactoryJndiName;
  }

  public Integer getInitialCapacity(
  ) {
    return this.initialCapacity;
  }
        
  public Integer getMaximumCapacity(
  ) {
    return this.maximumCapacity;
  }
        
  public Long getMaximumWait(
  ) {
    return this.maximumWait;
  }

  public void deploy(
      Context containerContext,
      Context applicationContext,
      Reference reference
  ) throws NamingException {
    // create JNDI entry to link to the connection factory
    // the entry is only created if the jndi name is set
    if (this.getConnectionFactoryJndiName() != null)
    {
        Contexts.bind(containerContext, this.getConnectionFactoryJndiName(), reference);
    }
  }

  private static final String TRANSACTION_SUPPORT_NO_TRANSACTION = "NoTransaction";
  private static final String TRANSACTION_SUPPORT_LOCAL_TRANSACTION = "LocalTransaction";
  private static final String TRANSACTION_SUPPORT_XA_TRANSACTION = "XATransaction";
  private Map configProperties = new HashMap();
  private String connectionFactoryInterface = null;
  private String connectionFactoryImplClass = null;
  private String connectionInterface = null;
  private String connectionImplClass = null;
  private String credentialInterface = null;
  private String transactionSupport = null;
  private String managedConnectionFactoryClass = null;
  private boolean reauthenticationSupport;
  private List authenticationMechanism = new ArrayList();
  private String connectionFactoryJndiName = null;
  private Integer maximumCapacity = null;
  private Integer initialCapacity = null;
  private Long maximumWait = null;
}
