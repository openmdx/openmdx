/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: LDAP Connector Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package test.openmdx.resource.ldap;

import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import netscape.ldap.LDAPException;
import netscape.ldap.LDAPSearchResults;
import netscape.ldap.LDAPv3;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openmdx.kernel.lightweight.naming.NonManagedInitialContextFactoryBuilder;
import org.openmdx.resource.cci.ConnectionFactory;

/**
 * LDAP Test
 */
public class TestLDAPConnector {

	/**
	 * Test class set-up
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static synchronized void setUp(
	) throws Exception {  
		if (!NamingManager.hasInitialContextFactoryBuilder()) {
			Map<String, String> resources = new HashMap<String, String>();
			resources.put(
				"org.openmdx.comp.env.ldap.bund",
				"eis:org.openmdx.resource.ldap.v3.ManagedConnectionFactory?ProtocolVersion=(java.lang.Integer)3&ConnectionURL=x500.bund.de:389"
			);
			resources.put(
				"org.openmdx.comp.env.ldap.fake",
				"eis:org.openmdx.resource.ldap.ldif.ManagedConnectionFactory?" +
				"ProtocolVersion=(java.lang.Integer)3&" +
				"ConnectionURL=xri:\\/\\/+resource\\/test\\/openmdx\\/resource\\/ldap\\/bund.ldif"
			);
			NonManagedInitialContextFactoryBuilder.install(resources);
		}
    }

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void fakeTest(
	) throws Exception {
		run("fake");
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void serverTest(
	) throws Exception {
		run("bund");
	}

	private void run(
		String name
	) throws NamingException, LDAPException {
		System.out.println("Test '" + name + "' started");
		@SuppressWarnings("unchecked")
		ConnectionFactory<LDAPv3, LDAPException> ldapConnectionFactory = (ConnectionFactory<LDAPv3, LDAPException>) new InitialContext(
		).lookup(
			"java:comp/env/ldap/" + name
		); 
		for (
			int i = 0;
			i < 3;
			i++
		) {
			this.run(ldapConnectionFactory, i);
		}
		System.out.println("Test '" + name + "' completed");
	}
	
	private void run(
		ConnectionFactory<LDAPv3, LDAPException> ldapConnectionFactory,
		int iteration
	) throws LDAPException {
		LDAPv3 ldap = ldapConnectionFactory.getConnection();
		try {
			LDAPSearchResults result = ldap.search(
				"o=Bund,c=DE", // base
				LDAPv3.SCOPE_SUB,
				"(sn=B*)", // filter
				new String[]{
					"cn", "sn", "givenName", "mail"	
				}, 
				false // attrsOnly 
			);
			for (
				int i = 0;
				result.hasMoreElements();
				i++
			) {
				System.out.println("" + iteration + "." + i + ": " + result.next());
			}
		} finally {
			ldap.disconnect();
		}
	}
	
}
