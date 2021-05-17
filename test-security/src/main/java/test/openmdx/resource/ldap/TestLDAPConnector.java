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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;
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
				"eis:org.openmdx.resource.ldap.v3.ManagedConnectionFactory?"
				+ "ProtocolVersion=(java.lang.Integer)3&"
				+ "ConnectionURL=ldap:\\/\\/x500.bund.de"
			);
			resources.put(
				"org.openmdx.comp.env.ldap.fake",
				"eis:org.openmdx.resource.ldap.ldif.ManagedConnectionFactory?" +
				"ProtocolVersion=(java.lang.Integer)3&" +
				"ConnectionURL=xri:\\/\\/+resource\\/test\\/openmdx\\/resource\\/ldap\\/bund.ldif"
			);
			resources.put(
				"org.openmdx.comp.env.ldap.local",
				"eis:org.openmdx.resource.ldap.v3.ManagedConnectionFactory?"
				+ "ProtocolVersion=(java.lang.Integer)3&"
				+ "ConnectionURL=ldaps:\\/\\/localhost"
			);
			NonManagedInitialContextFactoryBuilder.install(resources);
		}
    }

	@Test
	public void fakeTest(
	) throws Exception {
		run("fake");
	}

	@Test
	public void serverTest(
	) throws Exception {
		run("bund");
	}

	/**
	 * Test against a LDAP server
	 */
	public static void main(
		String... arguments
	) throws Exception {
		setUp();
		final TestLDAPConnector instance = new TestLDAPConnector();
		instance.run("local");
	}
	
	
	private void run(
		String name
	) throws NamingException, LdapException, IOException {
		System.out.println("Test '" + name + "' started");
		@SuppressWarnings("unchecked")
		ConnectionFactory<LdapConnection, LdapException> ldapConnectionFactory = (ConnectionFactory<LdapConnection, LdapException>) new InitialContext(
		).lookup(
			"java:comp/env/ldap/" + name
		); 
		for (
			int i = 0;
			i < 3;
			i++
		) {
			if("local".equals(name)) {
				this.chSearch(ldapConnectionFactory, i);
			} else {
				this.deSearch(ldapConnectionFactory, i);
			}
		}
		System.out.println("Test '" + name + "' completed");
	}
	
	private void deSearch(
		ConnectionFactory<LdapConnection, LdapException> ldapConnectionFactory,
		int iteration
	) throws LdapException, IOException {
		try (LdapConnection ldap = ldapConnectionFactory.getConnection()){
			try(EntryCursor result = ldap.search(
				"o=Bund,c=DE", // base
				"(sn=B*)", // filter
				SearchScope.SUBTREE,
				"cn", "sn", "givenName", "mail"
			)) {
				int i = 0;
				for (Entry e : result) {
					System.out.println("" + iteration + "." + i++ + ": " + e);
				}
			}
		}
	}

	private void chSearch(
		ConnectionFactory<LdapConnection, LdapException> ldapConnectionFactory,
		int iteration
	) throws LdapException, IOException {
		try (LdapConnection ldap = ldapConnectionFactory.getConnection()){
			try(EntryCursor result = ldap.search(
				"o=omex,c=ch", // base
				"(ou=IT)", // filter
				SearchScope.SUBTREE,
				"cn", "sn", "description" 
			)){
				int i = 0;
				for (Entry e : result) {
					System.out.println("" + iteration + "." + i++ + ": " + e);
				}
			}
		}
	}
	
}
