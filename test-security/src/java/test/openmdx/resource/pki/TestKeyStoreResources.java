/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestKeyStoreResources.java,v 1.2 2009/03/31 17:10:26 hburger Exp $
 * Description: Test Key Store Resources
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/31 17:10:26 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2009, OMEX AG, Switzerland
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
package test.openmdx.resource.pki;

import static org.junit.Assert.assertEquals;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;

import org.junit.Test;
import org.openmdx.application.dataprovider.deployment.Deployment_1;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.persistence.cci.EntityManagerFactory;
import org.openmdx.preferences1.jmi1.Preferences;
import org.openmdx.preferences1.jmi1.Preferences1Package;
import org.openmdx.preferences1.jmi1.Segment;

/**
 * Test Key Store Resources
 */
public class TestKeyStoreResources {

    /**
     * The SLF4 Logger
     */
    private static final Logger logger = Logger.getLogger(TestKeyStoreResources.class.getName());
    
    private static EntityManagerFactory deployment = new Deployment_1(
        true, // inProcess
        new String[]{
            "file:src/connector/openmdx-2/certificate-provider.rar",
            "file:src/connector/openmdx-2/key-provider.rar"
        }, //connectorURI
        new String[]{
            "file:src/ear/test-pki.ear"
        }, // applicationURI, 
        true, // logDeploymentDetails
        "test/openmdx/pki/EntityProviderFactory", // entityManagerFactoryURI 
        null, // gatewayURI,
        new String[]{
            "org:openmdx:preferences1" 
        } // model
    );
    
    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    @Test
    public void testKeyStoreResources(
    ) throws Throwable {
        PersistenceManager persistenceManager = deployment.getEntityManager();
        Authority authority = (Authority) persistenceManager.getObjectById(
            Authority.class,
            Preferences1Package.AUTHORITY_XRI
        );
        Provider provider = authority.getProvider("JKS");
        Segment segment = (Segment) provider.getSegment("certificate");
        Preferences preferences = segment.getPreferences("certificate");
        assertEquals("certgenca/certificate", preferences.getAbsolutePath());
        logger.log(Level.INFO,"{0}: {1}", new Object[]{preferences.getAbsolutePath(), preferences.getDescription()});
        segment = (Segment) provider.getSegment("key");
        preferences = segment.getPreferences("certificate");
        assertEquals("DemoIdentity/certificate", preferences.getAbsolutePath());
        logger.log(Level.INFO,"{0}: {1}", new Object[]{preferences.getAbsolutePath(), preferences.getDescription()});
        preferences = segment.getPreferences("key");
        assertEquals("DemoIdentity/key", preferences.getAbsolutePath());
        logger.log(Level.INFO,"{0}: {1}", new Object[]{preferences.getAbsolutePath(), preferences.getDescription()});
    }

}
