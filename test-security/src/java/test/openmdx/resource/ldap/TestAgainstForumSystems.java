/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: Test Against Forum Systems
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2018, OMEX AG, Switzerland
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
import java.util.concurrent.TimeUnit;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapAuthenticationException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmdx.resource.cci.AuthenticationInfo;
import org.openmdx.resource.cci.ConnectionFactory;
import org.openmdx.resource.ldap.v3.ManagedConnectionFactory;
import org.openmdx.resource.spi.DefaultConnectionManager;

/**
 * Use Forum System's public
 * <a href="https://www.forumsys.com/tutorials/integration-how-to/ldap/online-ldap-test-server/">Online LDAP Test Server</a>
 */
public class TestAgainstForumSystems {

	private static ConnectionManager connectionManager;
	
	@BeforeClass
	public static void setUp() {
		connectionManager = new DefaultConnectionManager();
	}

	@AfterClass
	public static void tearDown() {
		connectionManager = null;
	}
	
    @Test
    public void withClassicAuthenticationLookupSucceeds() throws LdapException, ResourceException, IOException {
        // Arrange
        final ManagedConnectionFactory testee = createTestee();
        testee.setUserName("cn=read-only-admin,dc=example,dc=com");
        testee.setPassword("password");
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory = testee.createConnectionFactory(connectionManager);
        // Act
        final String commonName = readWithContainerAuthentication(connectionFactory);
        // Assert
        Assert.assertEquals("Carl Friedrich Gauss", commonName);
    }

    @Test(expected=LdapAuthenticationException.class)
    public void withClassicAuthenticationDistinguishedNameMustBeValid() throws ResourceException, LdapException {
        final ManagedConnectionFactory testee = createTestee();
        testee.setUserName("cn=doe,dc=example,dc=com");
        testee.setPassword("password");
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory = testee.createConnectionFactory(connectionManager);
        // Act
        connectionFactory.getConnection();
    }

    @Test(expected=LdapAuthenticationException.class)
    public void withClassicAuthenticationPasswordMustBeValid() throws ResourceException, LdapException {
        final ManagedConnectionFactory testee = createTestee();
        testee.setUserName("cn=read-only-admin,dc=example,dc=com");
        testee.setPassword("Passwort");
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory = testee.createConnectionFactory(connectionManager);
        // Act
        connectionFactory.getConnection();
    }
    
    @Test
    public void withoutAuthenticationLookupSucceeds() throws ResourceException, LdapException, IOException {
        final ManagedConnectionFactory testee = createTestee();
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory = testee.createConnectionFactory(connectionManager);
        // Act
        final String commonName = readWithContainerAuthentication(connectionFactory);
        // Assert
        Assert.assertEquals("Carl Friedrich Gauss", commonName);
    }

    @Test
    public void withApplicationAuthenticationLookupSucceeds() throws ResourceException, LdapException, IOException {
        final ManagedConnectionFactory testee = createTestee();
        final AuthenticationInfo authenticationInfo = new AuthenticationInfo(
            "cn=read-only-admin,dc=example,dc=com", 
            new char[] {'p','a','s','s','w','o','r','d'}
        );
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory = testee.createConnectionFactory(connectionManager);
        // Act
        final String commonName = readWithApplicationAuthentication(connectionFactory, authenticationInfo);
        // Assert
        Assert.assertEquals("Carl Friedrich Gauss", commonName);
    }

    @Test
    public void withApplicationAuthenticaationClassicCredentialsAreIgnored() throws ResourceException, LdapException, IOException {
        final ManagedConnectionFactory testee = createTestee();
        testee.setUserName("cn=ignored,dc=example,dc=com");
        testee.setPassword("Passwort");
        final AuthenticationInfo authenticationInfo = new AuthenticationInfo(
            "cn=read-only-admin,dc=example,dc=com", 
            new char[] {'p','a','s','s','w','o','r','d'}
        );
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory = testee.createConnectionFactory(connectionManager);
        // Act
        final String commonName = readWithApplicationAuthentication(connectionFactory, authenticationInfo);
        // Assert
        Assert.assertEquals("Carl Friedrich Gauss", commonName);
    }
    
    @Test(expected=LdapAuthenticationException.class)
    public void withApplicationAuthenticationPasswordMustBeValid() throws ResourceException, LdapException {
        final ManagedConnectionFactory testee = createTestee();
        final AuthenticationInfo authenticationInfo = new AuthenticationInfo(
            "cn=read-only-admin,dc=example,dc=com", 
            new char[] {'P','a','s','s','w','o','r','t'}
        );
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory = testee.createConnectionFactory(connectionManager);
        // Act
        connectionFactory.getConnection(authenticationInfo);
    }

    @Test(expected=LdapAuthenticationException.class)
    public void withApplicationAuthenticationDistunguishedNameMustBeValid() throws ResourceException, LdapException {
        final ManagedConnectionFactory testee = createTestee();
        final AuthenticationInfo authenticationInfo = new AuthenticationInfo(
            "cn=doe,dc=example,dc=com", 
            new char[] {'p','a','s','s','w','o','r','d'}
        );
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory = testee.createConnectionFactory(connectionManager);
        // Act
        connectionFactory.getConnection(authenticationInfo);
    }

    public static void main(String[] arguments) throws IOException {
        final ManagedConnectionFactory testee = createTestee();
        final AuthenticationInfo authenticationInfo = new AuthenticationInfo(
            "cn=read-only-admin,dc=example,dc=com", 
            new char[] {'p','a','s','s','w','o','r','d'}
        );
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory;
        try {
            connectionFactory = testee.createConnectionFactory();
        } catch (ResourceException exception) {
            exception.printStackTrace();
            return;
        }
        // Act
        try {
            final String commonName = readWithApplicationAuthentication(connectionFactory, authenticationInfo);
            Assert.assertEquals("Carl Friedrich Gauss", commonName);
        } catch (LdapException exception) {
            exception.printStackTrace();
            return;
        }
        waitForOperator("Disconnect from Internet, please");
        try {
            readWithApplicationAuthentication(connectionFactory, authenticationInfo);
            Assert.fail("You forgot to disconnect!");
        } catch (LdapException expected) {
            waitForOperator("Reconnect to the Internet, please");
        }
        try {
            final String commonName = readWithApplicationAuthentication(connectionFactory, authenticationInfo);
            Assert.assertEquals("Carl Friedrich Gauss", commonName);
        } catch (LdapException exception) {
            exception.printStackTrace();
            return;
        }
        System.out.println("Reconnection successful");
    }

    /**
     * @param message
     */
    private static void waitForOperator(final String message) {
        System.out.println(message);
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            System.out.println("We have been interrupted");
        }
    }

    private static String readWithContainerAuthentication(final ConnectionFactory<LdapConnection, LdapException> connectionFactory)
        throws LdapException, IOException {
        try (LdapConnection ldap = connectionFactory.getConnection()) {
            return getCommonNameOfGauss(ldap);
        }
    }

    private static String readWithApplicationAuthentication(
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory,
        final AuthenticationInfo authenticationInfo
    )
        throws LdapException, IOException {
        try (LdapConnection ldap = connectionFactory.getConnection(authenticationInfo)){
            return getCommonNameOfGauss(ldap);
        }
    }

    private static String getCommonNameOfGauss(
        LdapConnection ldap
    ) throws LdapException {
        final Entry entry = ldap.lookup("uid=gauss,dc=example,dc=com");
        final Attribute cn = entry.get("cn");
        final Value value = cn.get(); // the first value
        return value.getString();
    }

	private static ManagedConnectionFactory createTestee() {
		final ManagedConnectionFactory testee = new ManagedConnectionFactory();
		testee.setServerName("ldap.forumsys.com");
		testee.setPortNumber(389);
		return testee;
	}

}
