/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestClientCertificate.java,v 1.6 2009/01/22 18:36:05 wfro Exp $
 * Description: Test Key Store Resources
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/22 18:36:05 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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
package org.openmdx.test.weblogic.security.ssl;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.application.cci.Model_1Deployment;
import org.openmdx.application.dataprovider.accessor.Jmi1AccessorFactory_1;
import org.openmdx.application.dataprovider.deployment.Dataprovider_1Deployment;
import org.openmdx.application.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;
import org.openmdx.base.application.deploy.InProcessDeployment;
import org.openmdx.base.cci.Authority;
import org.openmdx.base.cci.Provider;
import org.openmdx.base.deploy.Deployment;
import org.openmdx.base.object.jdo.ConfigurableProperties_2_0;
import org.openmdx.preferences1.cci.Preferences;
import org.openmdx.preferences1.cci.Segment;
import org.openmdx.preferences1.jmi.Preferences1Package;

/**
 * Test Key Store Resources
 */
public class TestClientCertificate extends TestCase {
    
    /**
     * Constructs a test case with the given name.
     */
    public TestClientCertificate(
    String name
    ) {
        super(name);
    }
    
    /**
     * The batch TestRunner can be given a class to run directly.
     * To start the batch runner from your main you can write: 
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * A test runner either expects a static method suite as the
     * entry point to get a test to run or it will extract the 
     * suite automatically. 
     */
    public static Test suite(
    ) {
        return new TestSuite(TestClientCertificate.class);
    }
    
    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp(
    ) throws Exception {
        Map<String, Object> configuration = new HashMap<String, Object>();
        configuration.put(
            Dataprovider_1ConnectionFactory.class.getName(),
            inProcessDeployment
        );
        configuration.put(
            ConfigurableProperties_2_0.FACTORY_CLASS,
            Jmi1AccessorFactory_1.class.getName()
        );
        configuration.put(
            ConfigurableProperties_2_0.OPTIMISTIC,
            Boolean.TRUE.toString()
        );
        this.persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory(
            configuration
        );
    }
    
    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void tearDown(
    ) throws Exception {
        this.persistenceManagerFactory.close();
    }
    
    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testKeyStoreResources(
    ) throws Throwable {
        PersistenceManager persistenceManager = this.persistenceManagerFactory.getPersistenceManager();
        Authority authority = (Authority) persistenceManager.getObjectById(
            Authority.class,
            Preferences1Package.AUTHORITY_XRI
        );
        Provider provider = authority.getProvider("JKS");
        Segment segment = (Segment) provider.getSegment("client-certificate");
        Preferences preferences = segment.getPreferences("context");
        assertEquals(
            "Execution context for SSL Client Certificate authenticated connection to t3s://localhost:7102", 
            preferences.getDescription()
        );
        if (VERBOSE) System.out.println(preferences.getAbsolutePath() + ": " + preferences.getDescription());
        segment = (Segment) provider.getSegment("principal-credentials");
        preferences = segment.getPreferences("context");
        assertEquals(
            "Execution context for Principal/Credentials authenticated connection to t3s://localhost:7102", 
            preferences.getDescription()
        );
        if (VERBOSE) System.out.println(preferences.getAbsolutePath() + ": " + preferences.getDescription());
    }

    private final static boolean VERBOSE = false;
    
    /**
     * The <code>PersistenceManagerFactory</code> belonging to the current fixture
     */
    private PersistenceManagerFactory persistenceManagerFactory;

    /**
     * Define whether deployment details should logged to the console
     */
    private final static boolean LOG_DEPLOYMENT_DETAIL = true;

    /**
     * The example1 enterprise application's URI
     */
    private final static String[] APPLICATION_URI = new String[]{
        "file:src/ear/test-pki.ear"
    };
    
    /**
     * The example1 enterprise application's URI
     */
    private final static String[] CONNECTOR_URI = new String[]{ 
        "file:src/connector/openmdx-2/client-certificate-connection.rar",
        "file:src/connector/openmdx-2/principal-credentials-connection.rar"
    };

    /**
     * Model Deployment
     */
    protected final static Deployment models = new Model_1Deployment(
        new String[]{
            "org:oasis-open",
            "org:openmdx:base",
            "org:openmdx:compatibility:state1",
            "org:openmdx:compatibility:view1",
            "org:openmdx:generic1",
            "org:openmdx:preferences1",
            "org:w3c"
        }
    );
    
    private static final String JNDI_NAME = "org/openmdx/test/resource/security/pki/preferences1";
    
    /**
     * In-Process deployment with lazy initialization
     */
    protected final static Dataprovider_1ConnectionFactory inProcessDeployment = new Dataprovider_1Deployment(
        new InProcessDeployment(
            CONNECTOR_URI,
            APPLICATION_URI,
            LOG_DEPLOYMENT_DETAIL ? System.out : null,
            System.err
        ),
        models,
        JNDI_NAME
    );

}
