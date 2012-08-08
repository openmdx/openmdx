/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestDeployment.java,v 1.32 2008/01/25 01:00:44 hburger Exp $
 * Description: Container Test
 * Revision:    $Revision: 1.32 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/25 01:00:44 $
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
package org.openmdx.test.kernel.application.deploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.Deflater;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.application.configuration.Report;
import org.openmdx.kernel.application.container.lightweight.LightweightContainer;
import org.openmdx.kernel.application.container.lightweight.LightweightContainerTransaction;
import org.openmdx.kernel.application.container.spi.ejb.ContainerTransaction;
import org.openmdx.kernel.application.container.spi.ejb.TransactionAttribute;
import org.openmdx.kernel.application.deploy.enterprise.EjbLocalReferenceDeploymentDescriptor;
import org.openmdx.kernel.application.deploy.enterprise.EjbRemoteReferenceDeploymentDescriptor;
import org.openmdx.kernel.application.deploy.enterprise.EnvEntryDeploymentDescriptor;
import org.openmdx.kernel.application.deploy.enterprise.SessionBeanDeploymentDescriptor;
import org.openmdx.kernel.application.deploy.enterprise.VerifyingDeploymentManager;
import org.openmdx.kernel.application.deploy.lightweight.ValidatingDeploymentManager;
import org.openmdx.kernel.application.deploy.spi.Deployment;
import org.openmdx.kernel.application.deploy.spi.Deployment.Application;
import org.openmdx.kernel.application.deploy.spi.Deployment.ApplicationClient;
import org.openmdx.kernel.application.deploy.spi.Deployment.AuthenticationMechanism;
import org.openmdx.kernel.application.deploy.spi.Deployment.Component;
import org.openmdx.kernel.application.deploy.spi.Deployment.Connector;
import org.openmdx.kernel.application.deploy.spi.Deployment.MessageDrivenBean;
import org.openmdx.kernel.application.deploy.spi.Deployment.Method;
import org.openmdx.kernel.application.deploy.spi.Deployment.Module;
import org.openmdx.kernel.application.deploy.spi.Deployment.ResourceAdapter;
import org.openmdx.kernel.application.deploy.spi.Deployment.SessionBean;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.naming.Contexts;
import org.openxri.XRI;

/**
 * Container Test
 */
public class TestDeployment extends TestCase {

  /**
   * Constructs a test case with the given name.
   */ 
  public TestDeployment(String name) {
    super(name);
  }
    
  /**
   * The batch TestRunner can be given a class to run directly.
   * To start the batch runner from your main you can write: 
   */
  public static void main (String[] args) {
    junit.textui.TestRunner.run (suite());
  }
    
  /**
   * A test runner either expects a static method suite as the
   * entry point to get a test to run or it will extract the 
   * suite automatically. 
   */
  public static Test suite() {
    return new TestSuite(TestDeployment.class);
  }

  /**
   * Sets up the fixture, for example, open a network connection.
   * This method is called before a test is executed.
   */
  protected void setUp(
  ) throws Exception {
    LightweightContainer.getInstance(
        LightweightContainer.Mode.ENTERPRISE_APPLICATION_CONTAINER
    );
    applicationClientEnvironment.put("ShortEnvVar","8099");
    this.deployment = new ValidatingDeploymentManager(
        new VerifyingDeploymentManager()
     );
    this.context = new InitialContext().createSubcontext("test"); 
    this.directory = this.createTemporaryTestFiles();
  }

  protected void tearDown(
  ) throws Exception {
    InitialContext initialContext = new InitialContext();
    traceContext("", initialContext, "test");
    initialContext.destroySubcontext("test");
    this.requestRemoval(this.directory);
  }

  public void testApplicationClientDeployment() throws Exception{
      LightweightContainer.getInstance(
          LightweightContainer.Mode.ENTERPRISE_APPLICATION_CONTAINER
      ).deployApplicationClient(
          new File(FULLY_EXPLODED_EAR_LOCATION).toURL().toString(), 
          Collections.EMPTY_MAP,
          new String[]{}
      );      
  }

  public void testFullyExplodedEAR(
  ) throws Exception {
    this.runEARDeploymentTests(	
      new File(FULLY_EXPLODED_EAR_LOCATION).toURL()
    );
  }

  public void testFullyExplodedConnector(
  ) throws Exception {
    this.runConnectorDeploymentTests(
      new File(FULLY_EXPLODED_EAR_LOCATION + "/eis1.rar").toURL()
    );
  }

  public void testConnector(
  ) throws Exception {
    this.runConnectorDeploymentTests(
      new File(this.directory.getAbsoluteFile() + "/test1.ear.semi-exploded/eis1.rar").toURL()
    );
  }

  public void testSemiExplodedEAR(
  ) throws Exception {
    this.runEARDeploymentTests(
      new File(this.directory.getAbsoluteFile() + "/test1.ear.semi-exploded").toURL()
    );
  }

  public void testEAR(
  ) throws Exception {
    this.runEARDeploymentTests(
      new File(this.directory.getAbsoluteFile() + "/test1.ear").toURL()
    );
  }  
  
  public void testCR0003173(
  ) throws Exception {
      Deployment.Application application = this.deployment.getApplication(
          new URL("file:src/ear/test-CR0003173.ear")
      );
      traceContents((Application)application);
      
      assertNotNull("Application", application);
      Report report = application.verify();
      assertNotNull("Application Verification Report", report);
      System.out.println(report);
      assertTrue("Application Verification", report.isSuccess());
      System.out.println("Application '" + application.getDisplayName() + "' verified\n---\n");

      Context containerContext = Contexts.getSubcontext(this.context, "_container");
      Context applicationContext = Contexts.getSubcontext(this.context, "_application");

      assertEquals("Application Display Name", "openMDX/Test.Core CR0003173", application.getDisplayName());
      List modules = application.getModules();
      
      // check whether module order is ok
      assertEquals("Modules", 1, modules.size());
      assertNull("modules[0]", ((Module)modules.get(0)).getDisplayName());
      
      // module one.jar
      Module moduleGateway = this.getModuleById(modules, "gateway.jar");
      assertNotNull("Module with id 'gateway.jar'", moduleGateway);
      
      // checking application class path for module gateway.jar
      URL[] applicationClassPath = moduleGateway.getApplicationClassPath();
      assertNotNull("Application class path for module 'gateway'", applicationClassPath);
      assertEquals("applicationClassPath.length", 0, applicationClassPath.length);

      // checking module class path for module one.jar
      URL[] moduleClassPath = moduleGateway.getModuleClassPath();
      assertNotNull("Module class path for module 'gateway'", moduleClassPath);
      assertEquals("moduleClassPath.length", 1, moduleClassPath.length);
      assertEndsWith("moduleClassPath[0]", moduleClassPath[0],"gateway.jar");

      report = moduleGateway.verify();
      assertNotNull("Module Verification Report", report);
      System.out.println(report);
      assertTrue("Module Verification", report.isSuccess());
      System.out.println("Module '" + moduleGateway.getDisplayName() + "' verified\n---\n");

      // component gateway.jar/explorer_Dataprovider_1ManagingTransaction
      Component componentManaging = this.getComponentByName(moduleGateway.getComponents(), "explorer_Dataprovider_1ManagingTransaction");
      assertNotNull("Component with name 'explorer_Dataprovider_1ManagingTransaction'", componentManaging);
      report = componentManaging.verify();
      assertNotNull("Component Verification Report", report);
      System.out.println(report);
      assertFalse("Component Verification fails because openMDX specific descriptor is missing", report.isSuccess());
      System.out.println("Component '" + componentManaging.getName() + "' verified\n---\n");

      Context contextManaging = Contexts.getSubcontext(this.context, componentManaging.getName());
      componentManaging.populate(contextManaging);

      assertTrue("Deployment.SessionBean expected", componentManaging instanceof Deployment.SessionBean);
      Deployment.SessionBean sessionBeanManaging = (Deployment.SessionBean) componentManaging;
      assertEquals("Deployment.SessionBean.Home", "org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1Home", sessionBeanManaging.getHome());
      assertEquals("Deployment.SessionBean.Remote", "org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1_0Remote", sessionBeanManaging.getRemote());
      assertEquals("Deployment.SessionBean.LocalHome", "org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1LocalHome", sessionBeanManaging.getLocalHome());
      assertEquals("Deployment.SessionBean.Local", "org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1_0Local", sessionBeanManaging.getLocal());
      assertEquals("Deployment.SessionBean.EjbClass", "org.openmdx.compatibility.application.dataprovider.transport.ejb.transaction.Hub_1Bean", sessionBeanManaging.getEjbClass());

      sessionBeanManaging.deploy(
          containerContext,
          applicationContext,
          new LinkRef(LOCAL_LINK_REF),
          new LinkRef(REMOTE_LINK_REF)
      );

      assertEquals("Deployment.SessionBean.SessionType", "Stateless", sessionBeanManaging.getSessionType());
      assertEquals("Deployment.SessionBean.TransactionType", "Bean", sessionBeanManaging.getTransactionType());

      // check container transaction
      List containerTransactionsManaging = sessionBeanManaging.getContainerTransaction();
      assertNull("No container transaction for bean-managed EJB", containerTransactionsManaging);

      // check deployed references
      this.assertReference(
          applicationContext, 
          SF1181033_RESOLVED ? "(gateway.jar)/explorer_Dataprovider_1ManagingTransaction/local" : "(gateway.jar)/*explorer_Dataprovider_1ManagingTransaction/*local", 
          LOCAL_LINK_REF
      );
      this.assertReference(
          applicationContext, 
          SF1181033_RESOLVED ? "(gateway.jar)/explorer_Dataprovider_1ManagingTransaction/remote" : "(gateway.jar)/*explorer_Dataprovider_1ManagingTransaction/*remote", 
          REMOTE_LINK_REF
      );
  }
  
  private void assertApplicationClient(
      Deployment.ApplicationClient applicationClient,
      Context containerContext,
      Context applicationContext
  ) throws Exception {
    assertNotNull("Application Client", applicationClient);
    Report report = applicationClient.validate();
    assertNotNull("Application Client Verification Report", report);
    System.out.println(report);
    assertTrue("Application Client Verification", report.isSuccess());
    System.out.println("Application Client '" + applicationClient.getDisplayName() + "' verified\n---");
    assertEquals("Application Client Display Name", "openMDX test application client", applicationClient.getDisplayName());
    
    // checking main class entry
    assertEquals("Application Client Main-Class", "org.openmdx.kernel.Version", applicationClient.getMainClass());

    assertEquals(
        "JAAS Callback Handler", 
        "org.jboss.test.client.test.SystemPropertyCallbackHandler", 
        applicationClient.getCallbackHandler()
    );
    
    // checking client class path
    URL[] clientClassPath = applicationClient.getModuleClassPath();
    assertNotNull("Application Client Class-Path", clientClassPath);
    assertEquals("clientClassPath.length", 2, clientClassPath.length);
    assertEndsWith("clientClassPath[0]", clientClassPath[0],"openmdx-application.jar");
    assertEndsWith("clientClassPath[1]", clientClassPath[1],"openmdx-base.jar");

    Context clientContext = Contexts.getSubcontext(this.context, "_client");
    applicationClient.populate(
        clientContext, applicationClientEnvironment
    );

    // environment variables
    assertEquals("env/ByteEnvVar", new java.lang.Byte((byte)1), clientContext.lookup("env/ByteEnvVar"));
    assertEquals("env/BooleanEnvVar", new java.lang.Boolean(true), clientContext.lookup("env/BooleanEnvVar"));
    assertEquals("env/StringEnvVar", "abcdefghijklmnopqrstuvwxyz", clientContext.lookup("env/StringEnvVar"));
    assertEquals("env/CharacterEnvVar", new java.lang.Character('A'), clientContext.lookup("env/CharacterEnvVar"));
    assertEquals("env/IntegerEnvVar", new java.lang.Integer(1234567890), clientContext.lookup("env/IntegerEnvVar"));
    assertEquals("env/ShortEnvVar", new java.lang.Short((short)8099), clientContext.lookup("env/ShortEnvVar"));
    assertEquals("env/LongEnvVar", new java.lang.Long(112233445566778899L), clientContext.lookup("env/LongEnvVar"));
    assertEquals("env/FloatEnvVar", new java.lang.Float(12.34), clientContext.lookup("env/FloatEnvVar"));
    assertEquals("env/DoubleEnvVar", new java.lang.Double(1234567890.123456789), clientContext.lookup("env/DoubleEnvVar"));

    // check remote references
    this.assertReference(
        clientContext, 
        "env/ejb/Roger", 
        "openmdx:container/org/openmdx/test/Roger"
    );
    this.assertReference(
        clientContext, 
        "env/ejb/Harald", 
        SF1181033_RESOLVED ? "openmdx:application/(one.jar)/Harald/remote" : "openmdx:application/(one.jar)/*Harald/*remote"
    );
    
    // check resource reference
    this.assertReference(
        clientContext, 
        "env/resource/foo", 
        "openmdx:container/org/openmdx/test/foo"
    );
    this.assertReference(
        clientContext, 
        "env/resource/bar", 
        "openmdx:container/org/openmdx/test/bar"
    );

  }
  
  private void runConnectorDeploymentTests(
    URL connectorURL
  ) throws Exception {
    Context containerContext = Contexts.getSubcontext(this.context, "_container");
    Context applicationContext = Contexts.getSubcontext(this.context, "_application");

    Deployment.Connector connector = this.deployment.getConnector(connectorURL);
    this.assertConnector(connector, containerContext, applicationContext);
  }

  private void runEARDeploymentTests(
    URL ear
  ) throws Exception {
    Context containerContext = Contexts.getSubcontext(this.context, "_container");
    Context applicationContext = Contexts.getSubcontext(this.context, "_application");

    Deployment.Application application = this.deployment.getApplication(ear);
    traceContents((Application)application);
    
    assertNotNull("Application", application);
    Report report = application.verify();
    assertNotNull("Application Verification Report", report);
    System.out.println(report);
    assertTrue("Application Verification", report.isSuccess());
    System.out.println("Application '" + application.getDisplayName() + "' verified\n---\n");
    assertEquals("Application Display Name", "openMDX test1 EAR", application.getDisplayName());
    List modules = application.getModules();
    
    // check whether module order is ok
    assertEquals("Modules", 4, modules.size());
    assertEquals("modules[0]", "openMDX test application client", ((Module)modules.get(0)).getDisplayName());
    assertEquals("modules[1]", "openMDX HTTP Connector", ((Module)modules.get(1)).getDisplayName());
    assertEquals("modules[2]", "module one", ((Module)modules.get(2)).getDisplayName());
    assertEquals("modules[3]", "module two", ((Module)modules.get(3)).getDisplayName());
    
    // module one.jar
    Module moduleOne = this.getModuleById(modules, "one.jar");
    assertNotNull("Module with id 'one.jar'", moduleOne);
    
    // checking application class path for module one.jar
    URL[] applicationClassPath = moduleOne.getApplicationClassPath();
    assertNotNull("Application class path for module 'module one'", applicationClassPath);
    assertEquals("applicationClassPath.length", 2, applicationClassPath.length);
    assertEndsWith("applicationClassPath[0]", applicationClassPath[0],"openmdx-application.jar");
    assertEndsWith("applicationClassPath[1]", applicationClassPath[1],"openmdx-base.jar");

    // checking module class path for module one.jar
    URL[] moduleClassPath = moduleOne.getModuleClassPath();
    assertNotNull("Module class path for module 'module one'", moduleClassPath);
    assertEquals("moduleClassPath.length", 1, moduleClassPath.length);
    assertEndsWith("moduleClassPath[0]", moduleClassPath[0],"one.jar");

    report = moduleOne.verify();
    assertNotNull("Module Verification Report", report);
    System.out.println(report);
    assertTrue("Module Verification", report.isSuccess());
    System.out.println("Module '" + moduleOne.getDisplayName() + "' verified\n---\n");

    // component one.jar/Werner
    Component componentWerner = this.assertComponent(moduleOne, "Werner");
            
    Context contextWerner = Contexts.getSubcontext(this.context, componentWerner.getName());
    componentWerner.populate(contextWerner);

    // environment variables
    assertEquals("env/ByteEnvVar", new java.lang.Byte((byte)1), contextWerner.lookup("env/ByteEnvVar"));
    assertEquals("env/BooleanEnvVar", java.lang.Boolean.TRUE, contextWerner.lookup("env/BooleanEnvVar"));
    assertEquals("env/StringEnvVar", "abcdefghijklmnopqrstuvwxyz", contextWerner.lookup("env/StringEnvVar"));
    assertEquals("env/CharacterEnvVar", new java.lang.Character('A'), contextWerner.lookup("env/CharacterEnvVar"));
    assertEquals("env/IntegerEnvVar", new java.lang.Integer(1234567890), contextWerner.lookup("env/IntegerEnvVar"));
    assertEquals("env/ShortEnvVar", new java.lang.Short((short)8311), contextWerner.lookup("env/ShortEnvVar"));
    assertEquals("env/LongEnvVar", new java.lang.Long(1234567890123456789l), contextWerner.lookup("env/LongEnvVar"));
    assertEquals("env/FloatEnvVar", new java.lang.Float(12.34), contextWerner.lookup("env/FloatEnvVar"));
    assertEquals("env/DoubleEnvVar", new java.lang.Double(1234567890.123456789), contextWerner.lookup("env/DoubleEnvVar"));
    
    // check local/remote ejb reference to bean in the same module
    this.assertReference(
        contextWerner, 
        "env/ejb/Harald", 
        SF1181033_RESOLVED ? "openmdx:application/(one.jar)/Harald/local" : "openmdx:application/(one.jar)/*Harald/*local"
    );
    this.assertReference(
        contextWerner, 
        "env/ejb/Roger", 
        SF1181033_RESOLVED ? "openmdx:application/(one.jar)/Roger/remote" : "openmdx:application/(one.jar)/*Roger/*remote" 
    );

    Deployment.SessionBean sessionBeanWerner = this.assertSessionBean(componentWerner);
    sessionBeanWerner.deploy(
        containerContext,
        applicationContext,
        new LinkRef(LOCAL_LINK_REF),
        new LinkRef(REMOTE_LINK_REF)
    );

    assertEquals("Deployment.SessionBean.SessionType", "Stateless", sessionBeanWerner.getSessionType());
    assertEquals("Deployment.SessionBean.TransactionType", "Bean", sessionBeanWerner.getTransactionType());

    // check container transaction
    List containerTransactionsWerner = sessionBeanWerner.getContainerTransaction();
    assertNull("No container transaction for bean-managed EJB", containerTransactionsWerner);

    // check deployed references
    this.assertReference(
        applicationContext, 
        SF1181033_RESOLVED ? "(one.jar)/Werner/local" : "(one.jar)/*Werner/*local", 
        LOCAL_LINK_REF
    );
    this.assertReference(
        applicationContext, 
        SF1181033_RESOLVED ? "(one.jar)/Werner/remote" : "(one.jar)/*Werner/*remote", 
        REMOTE_LINK_REF
    );

    // component one.jar/Harald
    Component componentHarald = this.assertComponent(moduleOne, "Harald");
      
    Context contextHarald = Contexts.getSubcontext(this.context, componentHarald.getName());
    componentHarald.populate(contextHarald);

    // check local/remote ejb reference to bean in another module in the same J2EE application
    this.assertReference(
        contextHarald, 
        "env/ejb/MartinWithModulePrefix", 
        SF1181033_RESOLVED ? "openmdx:application/(two.jar)/Martin/local" : "openmdx:application/(two.jar)/*Martin/*local"
    );
    this.assertReference(
        contextHarald, 
        "env/ejb/JuergWithModulePrefix", 
        SF1181033_RESOLVED ? "openmdx:application/(two.jar)/Juerg/remote" : "openmdx:application/(two.jar)/*Juerg/*remote"
    );
    this.assertReference(
        contextHarald, 
        "env/ejb/MartinWithoutModulePrefix", 
        SF1181033_RESOLVED ? "openmdx:application/(two.jar)/Martin/local" : "openmdx:application/(two.jar)/*Martin/*local"
    );
    this.assertReference(
        contextHarald, 
        "env/ejb/JuergWithoutModulePrefix", 
        SF1181033_RESOLVED ? "openmdx:application/(two.jar)/Juerg/remote" : "openmdx:application/(two.jar)/*Juerg/*remote"
    );

    Deployment.SessionBean sessionBeanHarald = this.assertSessionBean(componentHarald);
    sessionBeanHarald.deploy(
        containerContext,
        applicationContext,
        new LinkRef(LOCAL_LINK_REF),
        new LinkRef(REMOTE_LINK_REF)
    );

    assertEquals("Deployment.SessionBean.SessionType", "Stateful", sessionBeanHarald.getSessionType());
    assertEquals("Deployment.SessionBean.TransactionType", "Container", sessionBeanHarald.getTransactionType());

    // check container transaction
    List containerTransactionsHarald = sessionBeanHarald.getContainerTransaction();
    assertEquals("Default container transactions size", 0, containerTransactionsHarald.size());

    // check deployed references
    this.assertReference(
        applicationContext, 
        SF1181033_RESOLVED ? "(one.jar)/Harald/local" : "(one.jar)/*Harald/*local", 
        LOCAL_LINK_REF
    );
    this.assertReference(
        applicationContext, 
        SF1181033_RESOLVED ? "(one.jar)/Harald/remote" : "(one.jar)/*Harald/*remote", 
        REMOTE_LINK_REF
    );

    // component one.jar/Roger
    Component componentRoger = this.assertComponent(moduleOne, "Roger");
      
    Context contextRoger = Contexts.getSubcontext(this.context, componentRoger.getName());
    componentRoger.populate(contextRoger);

    // check local/remote ejb reference to bean in another module in the same J2EE application
    this.assertReference(contextRoger, "env/ejb/Daniel", "openmdx:container/org/openmdx/test/Daniel");
    this.assertReference(contextRoger, "env/ejb/Markus", "openmdx:container/org/openmdx/test/Markus");
    
    // check local/remote ejb reference to bean in the same module (ejb names also exist in sibling module)
    this.assertReference(
        contextRoger, 
        "env/ejb/Harald", 
        SF1181033_RESOLVED ? "openmdx:application/(one.jar)/Harald/remote" : "openmdx:application/(one.jar)/*Harald/*remote"
    );
    this.assertReference(
        contextRoger, 
        "env/ejb/Werner", 
        SF1181033_RESOLVED ? "openmdx:application/(one.jar)/Werner/local" : "openmdx:application/(one.jar)/*Werner/*local" 
    );

    // check resource reference
    this.assertReference(contextRoger, "env/resource/TestResourceRef", "openmdx:container/org/openmdx/test/resource/TestResourceRef");

    // check resource-env reference
    this.assertReference(contextRoger, "env/resource-env/TestResourceEnvRef", "openmdx:container/org/openmdx/test/resource-env/TestResourceEnvRef");

    assertTrue("component must be instance of Deployment.Pool", componentRoger instanceof Deployment.Pool);
    Deployment.Pool poolRoger = (Deployment.Pool) componentRoger;
    assertEquals("MaximumCapacity", new Integer(100), poolRoger.getMaximumCapacity());
    assertEquals("InitialCapacity", new Integer(1), poolRoger.getInitialCapacity());
    assertEquals("MaximumWait", new Long(1000000), poolRoger.getMaximumWait());

    Deployment.SessionBean sessionBeanRoger = this.assertSessionBean(componentRoger);
    sessionBeanRoger.deploy(
        containerContext,
        applicationContext,
        new LinkRef(LOCAL_LINK_REF),
        new LinkRef(REMOTE_LINK_REF)
    );
    // check container transaction
    List containerTransactionsRoger = sessionBeanRoger.getContainerTransaction();
    assertEquals("Container transactions size", 1, containerTransactionsRoger.size());
    Deployment.ContainerTransaction containerTransactionRoger = (Deployment.ContainerTransaction) containerTransactionsRoger.get(0);
    assertEquals("Transaction attribute", "Required", containerTransactionRoger.getTransAttribute());
    List methodsRoger = containerTransactionRoger.getMethod();
    assertEquals("Container transaction method size", 1, methodsRoger.size());
    Deployment.Method methodRoger = (Method) methodsRoger.get(0);
    assertEquals("Container transaction method name", "*", methodRoger.getMethodName());
    assertEquals("Method interface", "Remote", methodRoger.getMethodIntf());
    assertNull("Method arguments", methodRoger.getMethodParams());
    ContainerTransaction containerTransactionLightweight = new LightweightContainerTransaction(
        containerTransactionsRoger
    );
    assertEquals(
        "Remote method transaction attribute", 
        TransactionAttribute.REQUIRED, 
        containerTransactionLightweight.getTransactionAttribute("Remote", "anyMethod", new String[]{})
    );
    assertEquals(
        "Remote method transaction attribute", 
        TransactionAttribute.SUPPORTS, 
        containerTransactionLightweight.getTransactionAttribute("Local", "anyMethod", new String[]{})
    );
    // check deployed references
    this.assertReference(
        applicationContext, 
        SF1181033_RESOLVED ? "(one.jar)/Roger/local" : "(one.jar)/*Roger/*local", 
        LOCAL_LINK_REF
    );
    this.assertReference(
        applicationContext, 
        SF1181033_RESOLVED ? "(one.jar)/Roger/remote" : "(one.jar)/*Roger/*remote", 
        REMOTE_LINK_REF
   );
    
    
    // module two.jar
    Module moduleTwo = this.getModuleById(modules, "two.jar");
    assertNotNull("Module with id 'two.jar'", moduleTwo);
    report = moduleTwo.verify();
    assertNotNull("Module Verification Report", report);
    System.out.println(report);
    assertTrue("Module Verification", report.isSuccess());
    System.out.println("Module '" + moduleTwo.getDisplayName() + "' verified\n---\n");

    // component two.jar/Martin
    Component componentMartin = this.assertComponent(moduleTwo, "Martin");
              
    Context contextMartin = Contexts.getSubcontext(this.context, componentMartin.getName());
    componentMartin.populate(contextMartin);

    Deployment.SessionBean sessionBeanMartin = this.assertSessionBean(componentMartin);
    sessionBeanMartin.deploy(
        containerContext,
        applicationContext,
        new LinkRef(LOCAL_LINK_REF),
        new LinkRef(REMOTE_LINK_REF)
    );

    assertEquals("Deployment.SessionBean.HomeClass", "TestHomeClass", sessionBeanMartin.getHomeClass());
    assertEquals("Deployment.SessionBean.LocalHomeClass", "TestLocalHomeClass", sessionBeanMartin.getLocalHomeClass());

    // check deployed references
    this.assertReference(
        applicationContext, 
        SF1181033_RESOLVED ? "(two.jar)/Martin/local" : "(two.jar)/*Martin/*local", 
        LOCAL_LINK_REF
    );
    this.assertReference(
        applicationContext, 
        SF1181033_RESOLVED ? "(two.jar)/Martin/remote" : "(two.jar)/*Martin/*remote", 
        REMOTE_LINK_REF
    );
    this.assertReference(
        containerContext, 
        "org/openmdx/test/Martin", 
        REMOTE_LINK_REF
    );

    // component two.jar/Juerg
    Component componentJuerg = this.assertComponent(moduleTwo, "Juerg");
              
    Context contextJuerg = Contexts.getSubcontext(this.context, componentJuerg.getName());
    componentJuerg.populate(contextJuerg);

    Deployment.SessionBean sessionBeanJuerg = this.assertSessionBean(componentJuerg);
    sessionBeanJuerg.deploy(
        containerContext,
        applicationContext,
        new LinkRef(LOCAL_LINK_REF),
        new LinkRef(REMOTE_LINK_REF)
    );

    // check deployed references
    this.assertReference(
        applicationContext, 
        SF1181033_RESOLVED ? "(two.jar)/Juerg/local" : "(two.jar)/*Juerg/*local", 
        LOCAL_LINK_REF
    );
    this.assertReference(
        applicationContext, 
        SF1181033_RESOLVED ? "(two.jar)/Juerg/remote" : "(two.jar)/*Juerg/*remote", 
        REMOTE_LINK_REF
    );
    this.assertReference(
        containerContext, 
        "org/openmdx/test/Juerg", 
        LOCAL_LINK_REF
    );

    // component two.jar/Andy
    Component componentAndy = this.assertComponent(moduleTwo, "Andy");
              
    Context contextAndy = Contexts.getSubcontext(this.context, componentAndy.getName());
    componentAndy.populate(contextAndy);

    Deployment.MessageDrivenBean messageDrivenBeanAndy = this.assertMessageDrivenBean(componentAndy);
    messageDrivenBeanAndy.deploy(
        containerContext,
        applicationContext,
        new LinkRef(LOCAL_LINK_REF),
        new LinkRef(REMOTE_LINK_REF)
    );

    // check deployed references
    this.assertReference(
        applicationContext, 
        SF1181033_RESOLVED ? "(two.jar)/Andy/local" : "(two.jar)/*Andy/*local", 
        LOCAL_LINK_REF
    );
    this.assertReference(
        applicationContext, 
        SF1181033_RESOLVED ? "(two.jar)/Andy/remote" : "(two.jar)/*Andy/*remote", 
        REMOTE_LINK_REF
     );
    this.assertReference(
        containerContext, 
        "org/openmdx/test/Andy", 
        REMOTE_LINK_REF
    );
    this.assertReference(
        containerContext, 
        "org/openmdx/local/test/Andy", 
        LOCAL_LINK_REF
    );

    // module eis1.rar
    Connector connectorEis1 = (Connector)this.getModuleById(modules, "eis1.rar");
    this.assertConnector(connectorEis1, containerContext, applicationContext);
    
    // module client.jar
    ApplicationClient applicationClient1 = (ApplicationClient)this.getModuleById(modules, "client.jar");
    this.assertApplicationClient(applicationClient1, containerContext, applicationContext);
  }

  private void assertConnector(
    Connector connector,
    Context containerContext,
    Context applicationContext
  ) throws ServiceException, NamingException {
    assertNotNull("Connector", connector);
    Report report = connector.verify();
    assertNotNull("Connector Verification Report", report);
    System.out.println(report);
    assertTrue("Connector Verification", report.isSuccess());
    System.out.println("Connector '" + connector.getDisplayName() + "' verified\n---\n");

    assertEquals("Connector.VendorName", "OMEX AG", connector.getVendorName());
    assertEquals("Connector.EisType", "openMDX", connector.getEisType());
    assertEquals("Connector.ResourceAdapterVersion", "1", connector.getResourceadapterVersion());
    assertEquals("Connector.LicenseRequired", null, connector.getLicenseRequired());
    
    ResourceAdapter resAdapter = connector.getResourceAdapter();
    assertEquals("Connector.ResourceAdapter.ConnectionURL", "http://demo.opencrx.org/opencrx-core-CRX/gateway/", (String)resAdapter.getConfigProperties().get("ConnectionURL"));
    assertEquals("Connector.ResourceAdapter.UserName", "system", (String)resAdapter.getConfigProperties().get("UserName"));
    assertEquals("Connector.ResourceAdapter.Password", "manager", (String)resAdapter.getConfigProperties().get("Password"));

    assertEquals("Connector.ResourceAdapter.InitialCapacity", new java.lang.Integer(1), resAdapter.getInitialCapacity());
    assertEquals("Connector.ResourceAdapter.MaximumCapacity", new java.lang.Integer(100), resAdapter.getMaximumCapacity());
    assertEquals("Connector.ResourceAdapter.MaximumWait", new java.lang.Long(1000000), resAdapter.getMaximumWait());

    assertEquals("Connector.ResourceAdapter.ManagedConnectionFactoryClass", "org.openmdx.kernel.application.container.spi.http.ManagedDataproviderConnectionFactory", resAdapter.getManagedConnectionFactoryClass());
    assertEquals("Connector.ResourceAdapter.ConnectionFactoryInterface", "org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory", resAdapter.getConnectionFactoryInterface());
    assertEquals("Connector.ResourceAdapter.ConnectionFactoryImplClass", "org.openmdx.compatibility.base.dataprovider.transport.http.Dataprovider_1HttpConnectionFactory", resAdapter.getConnectionFactoryImplClass());
    assertEquals("Connector.ResourceAdapter.ConnectionInterface", "org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection", resAdapter.getConnectionInterface());
    assertEquals("Connector.ResourceAdapter.ConnectionImplClass", "org.openmdx.compatibility.base.dataprovider.transport.http.Dataprovider_1HttpConnection", resAdapter.getConnectionImplClass());
    assertEquals("Connector.ResourceAdapter.ReauthenticationSupport", false, resAdapter.getReauthenticationSupport());
    
    List authenticationMechanisms = resAdapter.getAuthenticationMechanism();
    assertEquals("Connector.ResourceAdapter.getAuthenticationMechanism.size", 1, authenticationMechanisms.size());
    AuthenticationMechanism authenticationMechanismType = (AuthenticationMechanism)authenticationMechanisms.get(0);
    assertEquals("Connector.ResourceAdapter.getAuthenticationMechanism[0].AuthenticationMechanismType", "BasicPassword", authenticationMechanismType.getAuthenticationMechanismType());
    assertEquals("Connector.ResourceAdapter.getAuthenticationMechanism[0].CredentialInterface", "javax.resource.spi.security.PasswordCredential", authenticationMechanismType.getCredentialInterface());
    
    resAdapter.deploy(
      containerContext,
      applicationContext,
      new LinkRef(RESOURCE_ADAPTER_LINK_REF)
    );

    // check deployed reference
    this.assertReference(containerContext, "org/openmdx/test/eis1/connectionfactory", RESOURCE_ADAPTER_LINK_REF);
  }
  
  private Component assertComponent(
    Module module, 
    String name
  ) {
    Component component = this.getComponentByName(module.getComponents(), name);
    assertNotNull("Component with name '" + name + "'", component);
    Report report = component.verify();
    assertNotNull("Component Verification Report", report);
    System.out.println(report);
    assertTrue("Component Verification", report.isSuccess());
    System.out.println("Component '" + component.getName() + "' verified\n---\n");
    return component;
  }
  
  private SessionBean assertSessionBean(
    Component component 
  ) {
    assertTrue("Deployment.SessionBean expected", component instanceof Deployment.SessionBean);
    Deployment.SessionBean bean = (Deployment.SessionBean) component;
    assertEquals("Deployment.SessionBean.Home", "testPackage.TestHome", bean.getHome());
    assertEquals("Deployment.SessionBean.Remote", "testPackage.TestRemote", bean.getRemote());
    assertEquals("Deployment.SessionBean.LocalHome", "testPackage.TestLocalHome", bean.getLocalHome());
    assertEquals("Deployment.SessionBean.Local", "testPackage.TestLocal", bean.getLocal());
    assertEquals("Deployment.SessionBean.EjbClass", "testPackage.TestClass", bean.getEjbClass());
    return bean;
  }
  
  private MessageDrivenBean assertMessageDrivenBean(
    Component component 
  ) {
    assertTrue("Deployment.MessageDrivenBean expected", component instanceof Deployment.MessageDrivenBean);
    Deployment.MessageDrivenBean bean = (Deployment.MessageDrivenBean) component;
    assertEquals("Deployment.MessageDrivenBean.MessageSelector", "TestMessageSelector", bean.getMessageSelector());
    assertEquals("Deployment.MessageDrivenBean.AcknowledgeMode", "Auto-acknowledge", bean.getAcknowledgeMode());
    assertEquals("Deployment.MessageDrivenBean.MessageDrivenDestinationType", "javax.jms.Topic", bean.getMessageDrivenDestinationType());
    assertEquals("Deployment.MessageDrivenBean.MessageDrivenDestinationSubscriptionDurability", "NonDurable", bean.getMessageDrivenDestinationSubscriptionDurability());
    assertEquals("Deployment.MessageDrivenBean.EjbClass", "testPackage.TestClass", bean.getEjbClass());
    return bean;
  }
  
  private void assertReference(
    Context context,
    String referenceName,
    String linkName
  ) throws NamingException {
    assertNotNull("Context lookup '" + referenceName + "' is null", context.lookupLink(referenceName));
    assertEquals(referenceName, linkName, ((LinkRef)context.lookupLink(referenceName)).getLinkName());
  }

  private Module getModuleById(
    Collection modules,
    String moduleId
  ) {
    for(
      Iterator it = modules.iterator();
      it.hasNext();
    ) {
      Module module = (Module)it.next();
      if (module.getModuleURI().equals(moduleId))
      {
        return module;
      }
    }

    // not found
    return null;
  }
  
  private Component getComponentByName(
    Collection components,
    String name
  ) {
    for(
      Iterator it = components.iterator();
      it.hasNext();
    ) {
      Component component = (Component)it.next();
      if (component.getName().equals(name))
      {
        return component;
      }
    }
  
    // not found
    return null;
  }

  private File createTemporaryTestFiles(
  ) throws IOException {
      
    File dir = createTemporaryDirectory();
      
    // create semi-exploded EAR based on fully-exploded EAR
    new File(dir.getAbsolutePath() + "/test1.ear.semi-exploded/META-INF").mkdirs();
    copyFile(
      FULLY_EXPLODED_EAR_LOCATION + "/META-INF/MANIFEST.MF",
      dir.getAbsolutePath() + "/test1.ear.semi-exploded/META-INF/MANIFEST.MF"
    );
    copyFile(
      FULLY_EXPLODED_EAR_LOCATION + "/META-INF/application.xml",
      dir.getAbsolutePath() + "/test1.ear.semi-exploded/META-INF/application.xml"
    );
    createJar(
      dir.getAbsolutePath() + "/test1.ear.semi-exploded/one.jar",
      new String[] {
        FULLY_EXPLODED_EAR_LOCATION + "/one.jar/META-INF/MANIFEST.MF",
        FULLY_EXPLODED_EAR_LOCATION + "/one.jar/META-INF/ejb-jar.xml",
        FULLY_EXPLODED_EAR_LOCATION + "/one.jar/META-INF/openmdx-ejb-jar.xml"
      },
      new String[] {
        "META-INF/MANIFEST.MF",
        "META-INF/ejb-jar.xml",
        "META-INF/openmdx-ejb-jar.xml"
      }
    );
    createJar(
      dir.getAbsolutePath() + "/test1.ear.semi-exploded/two.jar",
      new String[] {
        FULLY_EXPLODED_EAR_LOCATION + "/two.jar/META-INF/MANIFEST.MF",
        FULLY_EXPLODED_EAR_LOCATION + "/two.jar/META-INF/ejb-jar.xml",
        FULLY_EXPLODED_EAR_LOCATION + "/two.jar/META-INF/openmdx-ejb-jar.xml"
      },
      new String[] {
        "META-INF/MANIFEST.MF",
        "META-INF/ejb-jar.xml",
        "META-INF/openmdx-ejb-jar.xml"
      }
    );
    createJar(
      dir.getAbsolutePath() + "/test1.ear.semi-exploded/eis1.rar",
      new String[] {
        FULLY_EXPLODED_EAR_LOCATION + "/eis1.rar/META-INF/MANIFEST.MF",
        FULLY_EXPLODED_EAR_LOCATION + "/eis1.rar/META-INF/ra.xml",
        FULLY_EXPLODED_EAR_LOCATION + "/eis1.rar/META-INF/openmdx-connector.xml"
      },
      new String[] {
        "META-INF/MANIFEST.MF",
        "META-INF/ra.xml",
        "META-INF/openmdx-connector.xml"
      }
    );
    createJar(
        dir.getAbsolutePath() + "/test1.ear.semi-exploded/client.jar",
        new String[] {
          FULLY_EXPLODED_EAR_LOCATION + "/client.jar/META-INF/MANIFEST.MF",
          FULLY_EXPLODED_EAR_LOCATION + "/client.jar/META-INF/application-client.xml",
          FULLY_EXPLODED_EAR_LOCATION + "/client.jar/META-INF/openmdx-application-client.xml"
        },
        new String[] {
          "META-INF/MANIFEST.MF",
          "META-INF/application-client.xml",
          "META-INF/openmdx-application-client.xml"
        }
      );
      

    // create EAR based on semi-exploded EAR
    createJar(
      dir.getAbsolutePath() + "/test1.ear",
      new String[] {
        dir.getAbsolutePath() + "/test1.ear.semi-exploded/one.jar",
        dir.getAbsolutePath() + "/test1.ear.semi-exploded/two.jar",
        dir.getAbsolutePath() + "/test1.ear.semi-exploded/eis1.rar",
        dir.getAbsolutePath() + "/test1.ear.semi-exploded/client.jar",
        dir.getAbsolutePath() + "/test1.ear.semi-exploded/META-INF/MANIFEST.MF",
        dir.getAbsolutePath() + "/test1.ear.semi-exploded/META-INF/application.xml",
      },
      new String[] {
        "one.jar",
        "two.jar",
        "eis1.rar",
        "client.jar",
        "META-INF/MANIFEST.MF",
        "META-INF/application.xml"
      }
    );
      
    return dir;
  }
      
  private void createJar(
    String destJarFileName,
    String[] srcFilesToJar,
    String[] destFilesToJar
  ) throws IOException {
    byte[] buffer = new byte[18024];
    JarOutputStream out = new JarOutputStream(new FileOutputStream(destJarFileName));

    // Set the compression ratio
    out.setLevel(Deflater.DEFAULT_COMPRESSION);

    // iterate through the array of files, adding each to the jar file
    for (int i = 0; i < srcFilesToJar.length; i++) {
      // Associate a file input stream for the current file
      FileInputStream in = new FileInputStream(srcFilesToJar[i]);

      // Add Jar entry to output stream.
      out.putNextEntry(new JarEntry(destFilesToJar[i]));

      // Transfer bytes from the current file to the ZIP file
      int len;
      while ((len = in.read(buffer)) > 0)
      {
        out.write(buffer, 0, len);
      }

      // Close the current entry
      out.closeEntry();

      // Close the current file input stream
      in.close();
    }
    // Close the ZipOutPutStream
    out.close();
  }

  private void copyFile(
    String srcFile,
    String destFile
  ) throws IOException {
    InputStream in = new FileInputStream(new File(srcFile));
    OutputStream out = new FileOutputStream(new File(destFile));
   
    byte[] buf = new byte[18024];
    int len;
    while ((len = in.read(buf)) > 0) {
      out.write(buf, 0, len);
    }
    in.close();
    out.close();
  }
  
  private File createTemporaryDirectory(
  ) throws IOException {
    String junitTestTmp = System.getProperty("junit.test.tmp");
    File f = File.createTempFile(
        "JUnit-TestContainer", 
        "", 
        junitTestTmp == null ? null : new File(junitTestTmp)
    );
    String fileName = f.getAbsolutePath();
    f.delete();
    File dir = new File(fileName);
    dir.mkdir();
    return dir;
  }
    
  private void requestRemoval(
    File file
  ){
      if (file.isDirectory()) {
          String[] children = file.list();
          for (int i=0; i<children.length; i++) {
              requestRemoval(new File(file, children[i]));
          }
      }
      if(!file.delete()){
          System.err.println("File '" + file + "' could not be removed, ask for 'delete on exit'");
          file.deleteOnExit();
      }
  }
   
  private void traceContext(
    String indent,
    Context ctx,
    String name
  ) {
    SysLog.trace(indent + name + " [Context]");
    try {
      NamingEnumeration bindings = ctx.list(name);

      while (bindings.hasMore()) {
          Binding bd = (Binding)bindings.next();
          if (bd.getObject() instanceof Context)
          {
            traceContext(indent + "   ", (Context)ctx.lookup(name), bd.getName());
          }
          else
          {
            SysLog.trace(indent + "   " + bd.getName());
          }
      }
    }
    catch(Exception ex) {
      SysLog.trace("exception in showContext " + ex);
    }
  }

  private void traceContents(
    Application application
  ) {
    SysLog.trace("application: display name=" + application.getDisplayName());
    for(
      Iterator it = application.getModules().iterator();
      it.hasNext();
    ) {
      this.traceContents((Module)it.next());
    }
  }
  
  private void traceContents(
    Module module
  ) {
    SysLog.trace("   module: display name=" + module.getDisplayName());
    SysLog.trace("      module class path:");
    for(int i = 0; i < module.getModuleClassPath().length; i++)
    {
      SysLog.trace("         " + module.getModuleClassPath()[i]);        
    }
    SysLog.trace("      application class path:");
    for(int i = 0; i < module.getApplicationClassPath().length; i++)
    {
      SysLog.trace("         " + module.getApplicationClassPath()[i]);        
    }
    for(
      Iterator it = module.getComponents().iterator();
      it.hasNext();
    ) {
      Component component = (Component)it.next();
      if (component instanceof SessionBeanDeploymentDescriptor)
      {
        this.traceContents((SessionBeanDeploymentDescriptor)component);
      }
    }
  }

  private void traceContents(
    SessionBeanDeploymentDescriptor sbDD
  ) {
    SysLog.trace("      Session Bean:");
    SysLog.trace("         ejbName=" + sbDD.getName());
    SysLog.trace("         home=" + sbDD.getHome());
    SysLog.trace("         remote=" + sbDD.getRemote());
    SysLog.trace("         localHome=" + sbDD.getLocalHome());
    SysLog.trace("         localHome=" + sbDD.getLocalHome());
    SysLog.trace("         ejbClass=" + sbDD.getEjbClass());
    SysLog.trace("         jndiName=" + sbDD.getJndiName());
    SysLog.trace("         localJndiName=" + sbDD.getLocalJndiName());
    SysLog.trace("         sessionType=" + sbDD.getSessionType());
    SysLog.trace("         transactionType=" + sbDD.getTransactionType());
    SysLog.trace("         maximumCapacity=" + sbDD.getMaximumCapacity());
    SysLog.trace("         initialCapacity=" + sbDD.getInitialCapacity());
    SysLog.trace("         timeout=" + sbDD.getMaximumWait());
    for(
      Iterator it = sbDD.getEnvironmentEntries().iterator();
      it.hasNext();
    ) {
      EnvEntryDeploymentDescriptor eeDD = (EnvEntryDeploymentDescriptor) it.next();
      SysLog.trace("         Environment Entry:");
      SysLog.trace("            name=" + eeDD.getName());
      SysLog.trace("            type=" + eeDD.getType());
      SysLog.trace("            value=" + eeDD.getValue());
    }
    for(
      Iterator it = sbDD.getEjbLocalReferences().iterator();
      it.hasNext();
    ) {
      EjbLocalReferenceDeploymentDescriptor elrDD = (EjbLocalReferenceDeploymentDescriptor) it.next();
      SysLog.trace("         EJB Local References:");
      SysLog.trace("            name=" + elrDD.getName());
      SysLog.trace("            local-jndi-name=" + elrDD.getLocalJndiName());
      SysLog.trace("            type=" + elrDD.getType());
      SysLog.trace("            link=" + elrDD.getLink());
      SysLog.trace("            local=" + elrDD.getLocal());
      SysLog.trace("            localHome=" + elrDD.getLocalHome());
    }
    for(
      Iterator it = sbDD.getEjbRemoteReferences().iterator();
      it.hasNext();
    ) {
      EjbRemoteReferenceDeploymentDescriptor errDD = (EjbRemoteReferenceDeploymentDescriptor) it.next();
      SysLog.trace("         EJB Remote References:");
      SysLog.trace("            name=" + errDD.getName());
      SysLog.trace("            jndi-name=" + errDD.getJndiName());
      SysLog.trace("            type=" + errDD.getType());
      SysLog.trace("            link=" + errDD.getLink());
      SysLog.trace("            remote=" + errDD.getRemote());
      SysLog.trace("            home=" + errDD.getHome());
    }
  }
  
  /**
   * Test whether an URL ends with an expected file or directory.
   * 
   * @param message
   * @param url
   * @param suffix
   */
  private void assertEndsWith(
  		String message,
		URL url,
		String suffix
  ){
  		String uri = url.toExternalForm();
  		assertTrue(message, uri.endsWith(suffix) || uri.endsWith(suffix+'/'));
  }
  
  private Deployment deployment;
  private Context context;
  private File directory;
  protected Map applicationClientEnvironment = new HashMap();
  
  private static final String LOCAL_LINK_REF = "./local";
  private static final String REMOTE_LINK_REF = "./remote";
  private static final String RESOURCE_ADAPTER_LINK_REF = "./resourceadapter";
  private static final String FULLY_EXPLODED_EAR_LOCATION = "etc/org.openmdx.test.base.application.deploy/test1.ear.fully-exploded";

  private static final boolean SF1181033_RESOLVED = "xri://@example".equals(
      new XRI("xri://@example").toIRINormalForm()
  );
  
}
