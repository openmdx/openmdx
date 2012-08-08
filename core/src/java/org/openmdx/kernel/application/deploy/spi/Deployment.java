/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Deployment.java,v 1.18 2008/01/13 21:37:34 hburger Exp $
 * Description: Deployment 
 * Revision:    $Revision: 1.18 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/13 21:37:34 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.kernel.application.deploy.spi;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;

import org.openmdx.kernel.application.configuration.Configuration;


/**
 * Deployment
 */
public interface Deployment {

    /**
     * Get an Application instance
     * 
     * @param applicationURL
     *        an enterprise application archive or directory
     * 
     * @return an Application instance
     */
    Application getApplication(
        URL appplicationURL
    ) throws Exception;
        
    /**
     * Get a connector instance
     * 
     * @param connectorURL
     *        a connector archive or directory
     * 
     * @return a connector instance
     */
    Connector getConnector(
        URL connectorURL
    ) throws Exception;

    /**
     * A Java 2 Configuration
     */
    interface Descriptor extends Configuration {
        
        /**
         * Tells whether the configuration is an archive or expanded
         * 
         * @return <code>true</code> if the application is expanded 
         */
        boolean isExpanded(
        );
                
    }
    
    /**
     * A Java 2 Enterprise Application
     */
    interface Application extends Descriptor {
        
        /**
         * Retrieve an application's display name
         * 
         * @return an application's display name
         */
        String getDisplayName();
        
        /**
         * Retrieve an application's modules
         * <p>
         * The application deployment descriptor must have one
         * module element for each J2EE module in the
         * application package. A module element is defined
         * by moduleType definition.
         * 
         * The modules are returned in the order they are declared in the
         * application.xml.
         * 
         * @return all modules of this application
         */
        List<Module> getModules();

    }
    
    /**
     * A Java 2 Enterprise Module
     */
    interface Module extends Descriptor {
        
        /**
         * Retrieve a module's URI
         * 
         * @return the module URI
         */
        public String getModuleURI(
        );

        /**
         * Retrieve a module's display name
         * 
         * @return a module's display name
         */
        String getDisplayName();
  
        /**
         * Retrieve a module's components
         * 
         * @return a module's components
         */
        Collection<? extends Component> getComponents();
                
        /**
         * Retrieve the module's class path, e.g. a collection containing<ul>
         * <li>the JAR's URL in case of an enterprise java bean
         * <li>the WAR's META-INF/classes directory and its META-INF/lib JARs 
         *     in case of a web application
         * </ul
         * 
         * @return the module's local class path
         */
        URL[] getModuleClassPath();

        /**
         * Retrieve the class path for classes referenced by the module,
         * which should be loaded by the application's class loader, e.g.
         * URLs based on the Classpath: entry in the META-INF/Manifest.mf file.
         * 
         * @return the module's application class path
         */
        URL[] getApplicationClassPath();
        
    }

    /**
     * A Java 2 Enterprise Component
     */
    interface Component extends Configuration {
        
      /**
       * Get the component's name 
       * 
       * @return the component's name
       */
      String getName();
        
      /**
       * This method populates the contexts.
       * 
       * @param componentContext
       */
      void populate (
        Context componentContext
      ) throws NamingException;
      
    }

    /**
     * A Java 2 Enterprise Application Client
     */
    interface ApplicationClient extends Module {
        
      /**
       * Retrieve the callback handler class name
       * 
       * @return callback handler class
       */
      String getCallbackHandler();

      /**
       * Retrieve the main class defined in the META-INF/Manifest.mf file.
       * 
       * @return main class
       */
      String getMainClass();
      
      /**
       * This method populates the contexts.
       * 
       * @param applicationClientContext
       * @param applicationClientEnvironment 
       */
      void populate (
        Context applicationClientContext, 
        Map<String,String> applicationClientEnvironment
      ) throws NamingException;
      
      /**
       * This method deploys the resource adapter
       * 
       * @param containerContext
       *        components are registered in the containerContext
       *        in order to be referenced by other components.
       *        It's URL for LinkRef's is "openmdx:container".
       * @param reference
       *        Reference to the application clients provileged action
       *        wrapping its main method invocation
       * 
       * @throws NamingException 
       */
      void deploy (
          Context containerContext,
          Reference reference
      ) throws NamingException;
      
    }

    /**
     * A Web Application
     */
    interface WebApplication extends Module {
        
        /**
         * Retrieve the contextRoot.
         *
         * @return Returns the contextRoot.
         */
        public String getContextRoot();
        
        /**
         * This method populates the contexts.
         * 
         * @param webApplicationContext
         */
        void populate (
          Appendable webApplicationContext
        ) throws NamingException;

    }

        /**
     * An openMDX Pool 
     */
    interface Pool {
        
        /**
         * The initial-capacity element identifies the initial number 
         * of instance which the openMDX Container will 
         * attempt to obtain during deployment.
         * <p>
         * The default initial capacity is 1.
         * 
         * @return the initial number of pool instances
         */
        Integer getInitialCapacity();
        
        /**
         * The maximum-capacity element identifies the maximum number of 
         * managed connections which the openMDX Container will allow. 
         * Requests beyond this limit will result in an Exception being 
         * returned to the caller.
         * <p>
         * The default maximum capacity is java.lang.Integer.MAX_VALUE, 
         * i.e.2<sup>31</sup>-1.
         * 
         * @return the maximum number of pool instances that are allowed.
         */
        Integer getMaximumCapacity(
        );
        
        /**
         * The maximum-wait element defines the time in milliseconds to wait 
         * for an instance to be returned to the pool when there are 
         * maximum-capacity active instances.
         * <p>
         * A value of 0 will mean not to wait at all. When a request times out 
         * waiting for an instance an Exception is generated and the call aborted.
         * <p>
         * The default timeout value is java.lang.Long.MAX_VALUE, 
         * i.e. 2<sup>63</sup>-1.
         * 
         * @return time in milliseconds to wait for an instance to be returned 
         * to the pool when there are maximum-capacity active instances
         * 
         * @see #getMaximumCapacity()
         */
        Long getMaximumWait(
        ); 
        
    }

    /**
     * A Java 2 Enterprise Bean
     */
    interface Bean extends Component, Pool, AssemblyDescriptor {

        /**
         * The ejb-classType contains the fully-qualified name of the
         * enterprise bean's class. It is used by ejb-class elements.
         * <p>
         * Example:<ul>
         * <li>com.wombat.empl.EmployeeServiceBean
         * </ul>
         * 
         * @return the EJB class name
         */
        String getEjbClass();
                        
        /**
         * Retrieves the local JNDI name that has been configured for this bean
         * in the deployment descriptor.
         * 
         * @return the local JNDI name
         */
        String getLocalJndiName();
        
        /**
         * Retrieves the JNDI name that has been configured for this bean
         * in the deployment descriptor.
         * 
         * @return the JNDI name
         */
        String getJndiName();
        
        /**
         * This method deploys the Bean
         * 
         * @param containerContext
         *        components are registered in the containerContext
         *        in order to be referenced by other components.
         *        It's URL for LinkRef's is "openmdx:container".
         * @param applicationContext
         *        components may be registered in the applicationContext
         *        for application internal link resolutions.
         *        It's URL for LinkRef's is "openmdx:application".
         * @param localReference
         *        Reference to the EJB's local home
         * @param remoteReference
         *        Reference to the EJB's home
         * @throws NamingException 
         */
        void deploy (
            Context containerContext,
            Context applicationContext,
            Reference localReference,
            Reference remoteReference
        ) throws NamingException;

    }
    
    /**
     * A Java 2 Session Bean
     * <p>
     * The declaration consists of:<ul>
     * <li>an optional description
     * <li>an optional display name
     * <li>an optional icon element that contains a small and a large icon
     *     file name
     * <li>a name assigned to the enterprise bean in the deployment 
     *     description
     * <li>the names of the session bean's remote home and remote interfaces, 
     *     if any
     * <li>the names of the session bean's local home and local interfaces, 
     *     if any
     * <li>the name of the session bean's web service endpoint interface, if
     *     any
     * <li>the session bean's implementation class
     * <li>the session bean's state management type
     * <li>the session bean's transaction management type
     * <li>an optional declaration of the bean's environment entries
     * <li>an optional declaration of the bean's EJB references
     * <li>an optional declaration of the bean's local EJB references 
     * <li>an optional declaration of the bean's web service references
     * <li>an optional declaration of the security role references
     * <li>an optional declaration of the security identity to be used for the 
     *     execution of the bean's methods
     * <li>an optional declaration of the bean's resource manager connection 
     *     factory references
     * <li>an optional declaration of the bean's resource environment references.
     * <li>an optional declaration of the bean's message destination references 
     * <ul>
     * The elements that are optional are "optional" in the sense that they are 
     * omitted when if lists represented by them are empty. Either both the 
     * local-home and the local elements or both the home and the remote elements 
     * must be specified for the session bean. The service-endpoint element may 
     * only be specified if the bean is a stateless session bean.
     */
    interface SessionBean extends Bean, SessionBeanExtension {

        /**
         * The homeType defines the fully-qualified name of
         * an enterprise bean's home interface.
         * <p>
         * Example<ul>
         * <li>com.aardvark.payroll.PayrollHome
         * </ul>
         * 
         * @return the EJB's remote home interface class name
         */
        String getHome();
          
        /**
         * The remote element contains the fully-qualified name
         * of the enterprise bean's remote interface.
         * <p>
         * Example:<ul>
         * <li>com.wombat.empl.EmployeeService
         * </ul>
         * 
         * @return the EJB's remote interface class name
         */
        String getRemote();
          
        /**
         * The local-homeType defines the fully-qualified
         * name of an enterprise bean's local home interface.
         * 
         * @return the EJB's local home interface class name
         */
        String getLocalHome();
          
        /**
         * The localType defines the fully-qualified name of an
         * enterprise bean's local interface.
         * 
         * @return the EJB's local interface class name
         */
        String getLocal();
        
        /**
         * The session-typeType describes whether the session bean is a stateful 
         * session or stateless session. It is used by session-type elements.
         * <p> 
         * The value must be one of the two following<ul>
         * <li>Stateful
         * <li>Stateless
         * </ul>
         * @return the EJB's session type
         */
        String getSessionType();
        
        /**
         * The transaction-typeType specifies an enterprise bean's transaction 
         * management type.
         * <p>
         * The transaction-type must be one of the two following:<ul>
         * <li>Bean
         * <li>Container
         * </ul>
         * 
         * @return the EJB's transaction type
         */
        String getTransactionType();
        
    }

    interface SessionBeanExtension {
    	
      String getHomeClass();

      String getLocalHomeClass();

    }
    
    interface MessageDrivenBean extends Bean {

      String getMessageSelector();
  
      String getAcknowledgeMode();
  
      String getTransactionType();
  
      String getMessageDrivenDestinationType();
  
      String getMessageDrivenDestinationSubscriptionDurability();
      
    }

    /**
     * A Java 2 Connector
     * <p>
     * This element includes general information - vendor name, resource 
     * adapter version, icon - about the resource adapter module. It also 
     * includes information specific to the implementation of the resource 
     * adapter library as specified through the element resourceadapter.
     */
    interface Connector extends Module {

        /**
         * The element vendor-name specifies the name of resource adapter 
         * provider vendor.
         * 
         * @return the vendor name
         */
        String getVendorName(
        );
        
        /**
         * The element eis-type contains information about the type of the 
         * EIS. For example, the type of an EIS can be product name of EIS 
         * independent of any version info. This helps in identifying EIS 
         * instances that can be used with this resource adapter.
         * 
         * @return the EIS type
         */
        String getEisType(
        );
        
        /**
         * The element resourceadapter-version specifies a string-based 
         * version of the resource adapter from the resource adapter provider.
         * 
         * @return the resource adapter version
         */
        String getResourceadapterVersion(
        );
        
        /**
         * The licenseType specifies licensing requirements for the resource 
         * adapter module. This type specifies whether a license is required 
         * to deploy and use this resource adapter, and an optional 
         * description of the licensing terms (examples: duration of license, 
         * number of connection restrictions). It is used by the license 
         * element.
         * <p>
         * The element license-required specifies whether a license is 
         * required to deploy and use the resource adapter. 
         * 
         * @return true if a license is required
         */
        Boolean getLicenseRequired(
        );
        
        /**
         * Retrieve the connector's resource adapter
         * 
         * @return the connector's resource adapter
         */
        ResourceAdapter getResourceAdapter(
        );
        
    }
    
    /**
     * A Java 2 Resource Adapter
     * <p>
     * The resourceadapterType specifies information about the resource 
     * adapter. The information includes fully qualified resource adapter 
     * Java class name, configuration properties, information specific to the 
     * implementation of the resource adapter library as specified through 
     * the outbound-resourceadapter and inbound-resourceadapter elements, and 
     * an optional set of administered objects.
     */
    interface ResourceAdapter extends Pool {
       
        /**
         * The connector architecture defines a set of well-defined
         * properties all of type java.lang.String. These are as
         * follows:<ul>
         * <li>ServerName
         * <li>PortNumber
         * <li>UserName
         * <li>Password
         * <li>ConnectionURL
         * </ul>
         * <p>
         * A resource adapter provider can extend this property set to
         * include properties specific to the resource adapter and its
         * underlying EIS.
         * 
         * Possible values include<ul>
         * <li>ServerName
         * <li>PortNumber
         * <li>UserName
         * <li>Password
         * <li>ConnectionURL
         * </ul>
         * 
         * Property values may be instances of<ul>
         * <li>java.lang.Boolean
         * <li>java.lang.String
         * <li>java.lang.Integer
         * <li>java.lang.Double
         * <li>java.lang.Byte
         * <li>java.lang.Short
         * <li>java.lang.Long
         * <li>java.lang.Float
         * <li>java.lang.Character
         * </ul>
         * 
         * @return an instance mapping proerties to values.
         */
        Map<String,Object> getConfigProperties(
        );
       
        //--------------------------------------------------------------------
        // A Java 2 Outbound Resource Adapter
        //--------------------------------------------------------------------
        // The outbound-resourceadapterType specifies information about an 
        // outbound resource adapter. The information includes fully qualified 
        // names of classes/interfaces required as part of the connector 
        // architecture specified contracts for connection management, level of 
        // transaction support provided, one or more authentication mechanisms 
        // supported and additional required security permissions. If there is 
        // no authentication-mechanism specified as part of resource adapter 
        // element then the resource adapter does not support any standard 
        // security authentication mechanisms as part of security contract. 
        // The application server ignores the security part of the system 
        // contracts in this case.
      
        /**
         * The element managedconnectionfactory-class specifies
         * the fully qualified name of the Java class that
         * implements the
         * javax.resource.spi.ManagedConnectionFactory interface.
         * This Java class is provided as part of resource
         * adapter's implementation of connector architecture
         * specified contracts. The implementation of this
         * class is required to be a JavaBean.
         * <p>
         * Example:<ul>
         * <li>com.wombat.ManagedConnectionFactoryImpl
         * </ul>
         * 
         * @return the managed connection factory class name
         */
        String getManagedConnectionFactoryClass(
        );
      
        /**
         * The element connectionfactory-interface specifies
         * the fully qualified name of the ConnectionFactory
         * interface supported by the resource adapter
         * <p>
         * Example:<ul>
         * <li>com.wombat.ConnectionFactory
         * <li>javax.resource.cci.ConnectionFactory
         * </ul>
         * 
         * @return the managed connection factory interface name
         */
        String getConnectionFactoryInterface(
        );

        /**
         * The element connectionfactory-impl-class specifies
         * the fully qualified name of the ConnectionFactory
         * class that implements resource adapter
         * specific ConnectionFactory interface.
         * <p>
         * Example:<ul>
         * <li>com.wombat.ConnectionFactoryImpl
         * </ul>
         * 
         * @return the connection factory class name
         */
        String getConnectionFactoryImplClass(
        );

        /**
         * The connection-interface element specifies the fully
         * qualified name of the Connection interface supported
         * by the resource adapter.
         * <p>
         * Example:<ul>
         * <li>javax.resource.cci.Connection
         * </ul>
         * 
         * @return the connection factory interface name
         */
        String getConnectionInterface(
        );

        /**
         * The connection-impl-classType specifies the fully
         * qualified name of the Connection class that
         * implements resource adapter specific Connection
         * interface.  It is used by the connection-impl-class
         * elements.
         * <p>
         * Example:<ul>
         * <li>com.wombat.ConnectionImpl
         * </ul>
         * 
         * @return the connection class name
         */
        String getConnectionImplClass(
        );
      
        /**
         * The transaction-supportType specifies the level of transaction 
         * support provided by the resource adapter. It is used by 
         * transaction-support elements. 
         * <p>
         * The value must be one of the following:<ul>
         * <li>NoTransaction
         * <li>LocalTransaction
         * <li>XATransaction
         * </ul>
         * 
         * @return the kind of transaction support
         */
        String getTransactionSupport(
        );

        /**
         * Get the supported authentication mechanisms.
         * <p>
         * Note that this support is for the resource adapter and not for the 
         * underlying EIS instance. The optional description specifies any 
         * resource adapter specific requirement for the support of security 
         * contract and authentication mechanism.          
         * 
         * @return the supported authentication mechanisms
         */
        List<AuthenticationMechanism> getAuthenticationMechanism(
        );
      
        /**
         * The element reauthentication-support specifies whether the resource 
         * adapter implementation supports re-authentication of existing 
         * Managed-Connection instance. 
         * <p>
         * Note that this information is for the resource adapter implementation 
         * and not for the underlying EIS instance.
         *  
         * @return true if reauthentication is supported
         */
        boolean getReauthenticationSupport(
        );
      
        /**
         * This method deploys the resource adapter
         * 
         * @param containerContext
         *        components are registered in the containerContext
         *        in order to be referenced by other components.
         *        It's URL for LinkRef's is "openmdx:container".
         * @param applicationContext
         *        components may be registered in the applicationContext
         *        for application internal link resolutions.
         *        It's URL for LinkRef's is "openmdx:application".
         * @param reference
         *        Reference to the connection factory
         * 
         * @throws NamingException
         */
        void deploy (
            Context containerContext,
            Context applicationContext,
            Reference reference
        ) throws NamingException;

        //--------------------------------------------------------------------
        // A Java 2 Inbound Resource Adapter
        //--------------------------------------------------------------------
        // The inbound-resourceadapterType specifies information
        // about an inbound resource adapter. This contains information
        // specific to the implementation of the resource adapter
        // library as specified through the messageadapter element.

        //...
        
    }

    /**
     * The authentication-mechanism specifies an authentication 
     * mechanism supported by the resource adapter. Note that this 
     * support is for the resource adapter and not for the underlying 
     * EIS instance. The optional description specifies any resource 
     * adapter specific requirement for the support of security contract 
     * and authentication mechanism. 
     * <p>
     * Note that BasicPassword mechanism type should support the 
     * javax.resource.spi.security.PasswordCredential interface. 
     * The Kerbv5 mechanism type should support the 
     * org.ietf.jgss.GSSCredential interface or the deprecated 
     * javax.resource.spi.security.GenericCredential interface.
     */
    interface AuthenticationMechanism
    {
      /**
       * The element authentication-mechanism-type specifies
       * type of an authentication mechanism.
       * 
       * The example values are:
       * 
       * <authentication-mechanism-type>BasicPassword
       * </authentication-mechanism-type>
       * 
       * <authentication-mechanism-type>Kerbv5
       * </authentication-mechanism-type>
       * 
       * Any additional security mechanisms are outside the
       * scope of the Connector architecture specification.
       */
      String getAuthenticationMechanismType(
      );
      
      /**
       * The credential-interfaceType specifies the interface that the 
       * resource adapter implementation supports for the representation of 
       * the credentials. This element(s) that use this type, i.e.
       *  credential-interface, should be used by application server to 
       * find out the Credential interface it should use as part of the 
       * security contract.
       * <p>
       * The possible values are<ul>
       * <li>javax.resource.spi.security.PasswordCredential
       * <li>org.ietf.jgss.GSSCredential
       * <li>javax.resource.spi.security.GenericCredential
       * </ul>
       * @return the credential interface type
       */
      String getCredentialInterface(
      );
      
    }

    /**
     * AssemblyDescriptor
     * <p>
     * The assembly-descriptorType defines
     * application-assembly information.
     * <p>
     * The application-assembly information consists of the
     * following parts: the definition of security roles, the
     * definition of method permissions, the definition of
     * transaction attributes for enterprise beans with
     * container-managed transaction demarcation and a list of
     * methods to be excluded from being invoked.
     * <p>
     * All the parts are optional in the sense that they are
     * omitted if the lists represented by them are empty.
     * <p>
     * Providing an assembly-descriptor in the deployment
     * descriptor is optional for the ejb-jar file producer.
     */
    interface AssemblyDescriptor {
        
        /**
         * The container-transaction element specifies how the container must
         * manage transaction scopes for the enterprise bean's method invocations.
         * The element consists of an optional description, a list of
         * method elements, and a transaction attribute. The transaction
         * attribute is to be applied to all the specified methods.
         * 
         * @return the EJB's container transactions
         */
        List<ContainerTransaction> getContainerTransaction();
        
    }

    /**
     * ContainerTransaction
     * <p>
     * The container-transactionType specifies how the container
     * must manage transaction scopes for the enterprise bean's
     * method invocations. It defines an optional description, a
     * list of method elements, and a transaction attribute. The
     * transaction attribute is to be applied to all the specified
     * methods.
     */
    interface ContainerTransaction {
        
        /**
         * Retrieve the collection of methods to which the given transaction
         * attribute should be applied. 
         * 
         * @return the list of methods
         */
        List<String> getMethod();
        
        /**
         * The trans-attributeType specifies how the container must manage the 
         * transaction boundaries when delegating a method invocation to an 
         * enterprise bean's business method. 
         * <p>
         * The value must be one of the following<ul>
         * <li>NotSupported
         * <li>Supports
         * <li>Required
         * <li>RequiresNew
         * <li>Mandatory
         * <li>Never
         * </ul>
         * 
         * @return the EJB's transaction attribute
         */
        String getTransAttribute();        
        
    }
    
    /**
     * Method
     * <p>
     * The methodType is used to denote a method of an enterprise
     * bean's home, component, and/or web service endpoint
     * interface, or, in the case of a message-driven bean, the
     * bean's message listener method, or a set of such
     * methods. The ejb-name element must be the name of one of the
     * enterprise beans declared in the deployment descriptor; the
     * optional method-intf element allows to distinguish between a
     * method with the same signature that is multiply defined
     * across the home, component, and/or web service endpoint
     * interfaces; the method-name element specifies the method
     * name; and the optional method-params elements identify a
     * single method among multiple methods with an overloaded
     * method name.
     * <p>
     * There are three possible styles of using methodType element
     * within a method element<ol>
     * <li><pre>
     * 
     * &lt;method>
     *   &lt;ejb-name>EJBNAME&lt;/ejb-name>
     *   &lt;method-name>*&lt;/method-name>
     * &lt;/method>
     * </pre>
     * This style is used to refer to all the methods of the
     * specified enterprise bean's home, component, and/or web
     * service endpoint interfaces.
     * <li><pre>
     * 
     * &lt;method>
     *   &lt;ejb-name>EJBNAME&lt;/ejb-name>
     *   &lt;method-name>METHOD&lt;/method-name>
     * &lt;/method>
     * </pre> 
     * This style is used to refer to the specified method of
     * the specified enterprise bean. If there are multiple
     * methods with the same overloaded name, the element of
     * this style refers to all the methods with the overloaded
     * name.
     * <li><pre>
     * 
     * &lt;method&gt;
     *   &lt;ejb-name&gt;EJBNAME&lt;/ejb-name&gt;
     *   &lt;method-name&gt;METHOD&lt;/method-name&gt;
     *   &lt;method-params&gt;
     *     &lt;method-param&gt;PARAM-1&lt;/method-param&gt;
     *     &lt;method-param&gt;PARAM-2&lt;/method-param&gt;
     *     ...
     *     &lt;method-param&gt;PARAM-n&lt;/method-param&gt;
     *   &lt;/method-params&gt;
     * &lt;/method&gt;
     * </pre&gt;
     * This style is used to refer to a single method within a
     * set of methods with an overloaded name. PARAM-1 through
     * PARAM-n are the fully-qualified Java types of the
     * method's input parameters (if the method has no input
     * arguments, the method-params element contains no
     * method-param elements). Arrays are specified by the
     * array element's type, followed by one or more pair of
     * square brackets (e.g. int[][]). If there are multiple
     * methods with the same overloaded name, this style refers
     * to all of the overloaded methods.
     * </ol&gt;
     * Examples:<ul&gt;
     * <li&gt;
     * Style 1: The following method element refers to all the
     * methods of the EmployeeService bean's home, component,
     * and/or web service endpoint interfaces:
     * <pre&gt;
     * &lt;method&gt;
     *   &lt;ejb-name&gt;EmployeeService&lt;/ejb-name&gt;
     *   &lt;method-name&gt;*&lt;/method-name&gt;
     * &lt;/method&gt;
     * </pre&gt;
     * <li&gt;
     * Style 2: The following method element refers to all the
     * create methods of the EmployeeService bean's home
     * interface(s).
     * <pre&gt;
     * &lt;method&gt;
     *   &lt;ejb-name&gt;EmployeeService&lt;/ejb-name&gt;
     *   &lt;method-name&gt;create&lt;/method-name&gt;
     * &lt;/method&gt;
     * </pre&gt;
     * <li&gt;
     * Style 3: The following method element refers to the
     * create(String firstName, String LastName) method of the
     * EmployeeService bean's home interface(s).
     * <pre&gt;
     * &lt;method&gt;
     *   &lt;ejb-name&gt;EmployeeService&lt;/ejb-name&gt;
     *   &lt;method-name&gt;create&lt;/method-name&gt;
     *   &lt;method-params&gt;
     *     &lt;method-param&gt;java.lang.String&lt;/method-param&gt;
     *     &lt;method-param&gt;java.lang.String&lt;/method-param&gt;
     *   &lt;/method-params&gt;
     * &lt;/method&gt;
     * </pre&gt;
     * <li&gt;
     * The following example illustrates a Style 3 element with
     * more complex parameter types. The method
     * foobar(char s, int i, int[] iar, mypackage.MyClass mycl,
     * mypackage.MyClass[][] myclaar) would be specified as:
     * <pre&gt;
     * &lt;method&gt;
     *   &lt;ejb-name&gt;EmployeeService&lt;/ejb-name&gt;
     *   &lt;method-name&gt;foobar&lt;/method-name&gt;
     *   &lt;method-params&gt;
     *     &lt;method-param&gt;char&lt;/method-param&gt;
     *     &lt;method-param&gt;int&lt;/method-param&gt;
     *     &lt;method-param&gt;int[]&lt;/method-param&gt;
     *     &lt;method-param&gt;mypackage.MyClass&lt;/method-param&gt;
     *     &lt;method-param&gt;mypackage.MyClass[][]&lt;/method-param&gt;
     *   &lt;/method-params&gt;
     * &lt;/method&gt;
     * </pre&gt;
     * <li&gt;
     * The optional method-intf element can be used when it becomes
     * necessary to differentiate between a method that is multiply
     * defined across the enterprise bean's home, component, and/or
     * web service endpoint interfaces with the same name and
     * signature.
     * <br&gt;
     * For example, the method element
     * <pre&gt;
     * &lt;method&gt;
     *   &lt;ejb-name&gt;EmployeeService&lt;/ejb-name&gt;
     *   &lt;method-intf&gt;Remote&lt;/method-intf&gt;
     *   &lt;method-name&gt;create&lt;/method-name&gt;
     *   &lt;method-params&gt;
     *     &lt;method-param&gt;java.lang.String&lt;/method-param&gt;
     *     &lt;method-param&gt;java.lang.String&lt;/method-param&gt;
     *   &lt;/method-params&gt;
     * &lt;/method&gt;
     * </pre&gt;
     * can be used to differentiate the create(String, String)
     * method defined in the remote interface from the
     * create(String, String) method defined in the remote home
     * interface, which would be defined as
     * <pre&gt;
     * &lt;method&gt;
     *   &lt;ejb-name&gt;EmployeeService&lt;/ejb-name&gt;
     *   &lt;method-intf&gt;Home&lt;/method-intf&gt;
     *   &lt;method-name&gt;create&lt;/method-name&gt;
     *   &lt;method-params&gt;
     *     &lt;method-param&gt;java.lang.String&lt;/method-param&gt;
     *     &lt;method-param&gt;java.lang.String&lt;/method-param&gt;
     *   &lt;/method-params&gt;
     * &lt;/method&gt;
     * </pre&gt;
     * and the create method that is defined in the local home
     * interface which would be defined as
     * <pre&gt;
     * &lt;method&gt;
     *   &lt;ejb-name&gt;EmployeeService&lt;/ejb-name&gt;
     *   &lt;method-intf&gt;LocalHome&lt;/method-intf&gt;
     *   &lt;method-name&gt;create&lt;/method-name&gt;
     *   &lt;method-params&gt;
     *     &lt;method-param&gt;java.lang.String&lt;/method-param&gt;
     *     &lt;method-param&gt;java.lang.String&lt;/method-param&gt;
     *   &lt;/method-params&gt;
     * &lt;/method&gt;
     * </pre&gt;
     * The method-intf element can be used with all three Styles
     * of the method element usage. For example, the following
     * method element example could be used to refer to all the
     * methods of the EmployeeService bean's remote home interface.
     * <pre&gt;
     * &lt;method&gt;
     *   &lt;ejb-name&gt;EmployeeService&lt;/ejb-name&gt;
     *   &lt;method-intf&gt;Home&lt;/method-intf&gt;
     *   &lt;method-name&gt;*&lt;/method-name&gt;
     * &lt;/method&gt;
     * </pre&gt;
     */
    interface Method {
        
        /**
         * The method-intf element allows a method element to differentiate
         * between the methods with the same name and signature that are multiply
         * defined across the home and component interfaces (e.g., in both an
         * enterprise bean's remote and local interfaces, or in both an enterprise
         * bean's home and remote interfaces, etc.)
         * <p>
         * The method-intf element must be one of the following:<ul>
         * <li>Home
         * <li>Remote
         * <li>LocalHome
         * <li>Local
         * </ul>
         * 
         * @return the method's interface
         */
        String getMethodIntf();
        
        /**
         * The method-name element contains a name of an enterprise bean method,
         * or the asterisk (*) character. The asterisk is used when the element^
         * denotes all the methods of an enterprise bean's component and home
         * interfaces.
         * 
         * @return the method's name
         */
        String getMethodName();
        
        /**
         * The method-params element contains a list of the fully-qualified Java
         * type names of the method parameters.^
         * 
         * @return the moethod's paramete types
         */
        List<String> getMethodParams();

    }

}
