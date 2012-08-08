/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestKeyStoreResources.java,v 1.5 2010/01/04 17:35:01 hburger Exp $
 * Description: Test Key Store Resources
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/01/04 17:35:01 $
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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.junit.Before;
import org.junit.Test;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.kernel.lightweight.naming.NonManagedInitialContextFactoryBuilder;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.preferences1.jmi1.Preferences;
import org.openmdx.preferences1.jmi1.Preferences1Package;
import org.openmdx.preferences1.jmi1.Segment;

/**
 * Test Key Store Resources
 */
public class TestKeyStoreResources {

	@Before
	public void setUp() throws NamingException{
		if(!NamingManager.hasInitialContextFactoryBuilder()) {
			Map<String,String> pkiProviders = new HashMap<String,String>();
			String userHome = System.getProperty("user.home").replaceAll("/", "\\\\/");
			pkiProviders.put(
				"org.openmdx.comp.env.pki.certificate",
				"eis:org.openmdx.resource.pki.keystore.ManagedKeyStoreConnectionFactory?" +
				"KeyStoreType=jks&" +
				"ConnectionURL=file:" + userHome + "\\/opt\\/bea\\/weblogic92\\/server\\/lib\\/DemoTrust.jks&" +
				"UserName=certgenca&" +
				"Password=DemoTrustKeyStorePassPhrase"
			);
			pkiProviders.put(
				"org.openmdx.comp.env.pki.key",
				"eis:org.openmdx.resource.pki.keystore.ManagedKeyStoreConnectionFactory?" +
				"KeyStoreType=jks&" +
				"ConnectionURL=file:" + userHome + "\\/opt\\/bea\\/weblogic92\\/server\\/lib\\/DemoIdentity.jks&" +
				"PassPhraseSeparator=|&" +
				"UserName=DemoIdentity&" +
				"Password=DemoIdentityKeyStorePassPhrase|DemoIdentityPassPhrase"
			);
            NonManagedInitialContextFactoryBuilder.install(pkiProviders);
		}
	}

	/**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    @Test
    public void testKeyStoreResources(
    ) throws Throwable {
    	PersistenceManagerFactory persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory(
			"test-PKI-EntityManagerFactory"
    	);
        PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
        Authority authority = (Authority) persistenceManager.getObjectById(
            Authority.class,
            Preferences1Package.AUTHORITY_XRI
        );
        Provider provider = authority.getProvider("PKI");
        Segment segment = (Segment) provider.getSegment("certificate");
        Preferences preferences = segment.getPreferences("certificate");
        assertEquals("certgenca/certificate", preferences.getAbsolutePath());
        SysLog.log(Level.INFO,"{0}: {1}", preferences.getAbsolutePath(), preferences.getDescription());
        segment = (Segment) provider.getSegment("key");
        preferences = segment.getPreferences("certificate");
        assertEquals("DemoIdentity/certificate", preferences.getAbsolutePath());
        SysLog.log(Level.INFO,"{0}: {1}", preferences.getAbsolutePath(), preferences.getDescription());
        preferences = segment.getPreferences("key");
        assertEquals("DemoIdentity/key", preferences.getAbsolutePath());
        SysLog.log(Level.INFO,"{0}: {1}", preferences.getAbsolutePath(), preferences.getDescription());
    }

}
