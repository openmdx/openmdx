/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: LDAP Connector Test
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openmdx.junit5.OpenmdxTestSecurityStandardExtension;
import org.openmdx.resource.cci.ConnectionFactory;

/**
 * LDAP Test
 */
@ExtendWith(OpenmdxTestSecurityStandardExtension.class)
public class LDAPConnectorTest {

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

	@Disabled("Usually there is no local LDAP server available")
	@Test
	public void localTest(
	) throws Exception {
		run("local");
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
