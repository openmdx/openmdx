/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: Test Against Forum Systems
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
import java.util.concurrent.TimeUnit;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
#endif

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapAuthenticationException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openmdx.resource.cci.AuthenticationInfo;
import org.openmdx.resource.cci.ConnectionFactory;
import org.openmdx.resource.ldap.v3.ManagedConnectionFactory;
import org.openmdx.resource.spi.DefaultConnectionManager;

/**
 * Use Forum System's public
 * <a href="https://www.forumsys.com/tutorials/integration-how-to/ldap/online-ldap-test-server/">Online LDAP Test Server</a>
 */
class ForumSystemsTest {

	private static ConnectionManager connectionManager;
	
	@BeforeAll
	static void setUp() {
		connectionManager = new DefaultConnectionManager();
	}

	@AfterAll
	static void tearDown() {
		connectionManager = null;
	}
	
    @Test
    void withClassicAuthenticationLookupForExistingUserSucceeds() throws LdapException, ResourceException, IOException {
        // Arrange
        final ManagedConnectionFactory testee = createTestee();
        testee.setUserName("cn=read-only-admin,dc=example,dc=com");
        testee.setPassword("password");
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory = testee.createConnectionFactory(connectionManager);
        // Act
        final String commonName = readCommonNameOfGaussWithContainerAuthentication(connectionFactory);
        // Assert
        Assertions.assertEquals("Carl Friedrich Gauss", commonName);
    }

    @Test
    void withClassicAuthenticationDistinguishedNameMustBeValid() throws ResourceException, LdapException {
    	// Arrange
        final ManagedConnectionFactory testee = createTestee();
        testee.setUserName("cn=doe,dc=example,dc=com");
        testee.setPassword("password");
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory = testee.createConnectionFactory(connectionManager);
        // Act & Assert
        Assertions.assertThrowsExactly(LdapAuthenticationException.class,()-> connectionFactory.getConnection());
    }

    @Test
    void withClassicAuthenticationPasswordMustBeValid() throws ResourceException, LdapException {
    	// Arrange
        final ManagedConnectionFactory testee = createTestee();
        testee.setUserName("cn=read-only-admin,dc=example,dc=com");
        testee.setPassword("Passwort");
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory = testee.createConnectionFactory(connectionManager);
        // Act & Assert
        Assertions.assertThrowsExactly(LdapAuthenticationException.class,()-> connectionFactory.getConnection());
    }
    
    @Test
    void withoutAuthenticationLookupForExistingUserSucceeds() throws ResourceException, LdapException, IOException {
    	// Arrange
        final ManagedConnectionFactory testee = createTestee();
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory = testee.createConnectionFactory(connectionManager);
        // Act
        final String commonName = readCommonNameOfGaussWithContainerAuthentication(connectionFactory);
        // Assert
        Assertions.assertEquals("Carl Friedrich Gauss", commonName);
    }

    @Test
    void withApplicationAuthenticationLookupForExistingUserSucceeds() throws ResourceException, LdapException, IOException {
    	// Arrange
        final ManagedConnectionFactory testee = createTestee();
        final AuthenticationInfo authenticationInfo = new AuthenticationInfo(
            "cn=read-only-admin,dc=example,dc=com", 
            new char[] {'p','a','s','s','w','o','r','d'}
        );
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory = testee.createConnectionFactory(connectionManager);
        // Act
        final String commonName = readCommonNameOfGaussWithApplicationAuthentication(connectionFactory, authenticationInfo);
        // Assert
        Assertions.assertEquals("Carl Friedrich Gauss", commonName);
    }

    @Test
    void withApplicationAuthenticationClassicCredentialsAreIgnored() throws ResourceException, LdapException, IOException {
    	// Arrange
        final ManagedConnectionFactory testee = createTestee();
        testee.setUserName("cn=ignored,dc=example,dc=com");
        testee.setPassword("Passwort");
        final AuthenticationInfo authenticationInfo = new AuthenticationInfo(
            "cn=read-only-admin,dc=example,dc=com", 
            new char[] {'p','a','s','s','w','o','r','d'}
        );
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory = testee.createConnectionFactory(connectionManager);
        // Act
        final String commonName = readCommonNameOfGaussWithApplicationAuthentication(connectionFactory, authenticationInfo);
        // Assert
        Assertions.assertEquals("Carl Friedrich Gauss", commonName);
    }
    
    @Test
    void withApplicationAuthenticationPasswordMustBeValid() throws ResourceException, LdapException {
    	// Arrange
        final ManagedConnectionFactory testee = createTestee();
        final AuthenticationInfo authenticationInfo = new AuthenticationInfo(
            "cn=read-only-admin,dc=example,dc=com", 
            new char[] {'P','a','s','s','w','o','r','t'}
        );
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory = testee.createConnectionFactory(connectionManager);
        // Act & Assert
        Assertions.assertThrowsExactly(LdapAuthenticationException.class,()-> connectionFactory.getConnection(authenticationInfo));
    }

    @Test
    void withApplicationAuthenticationDistunguishedNameMustBeValid() throws ResourceException, LdapException {
    	// Arrange
        final ManagedConnectionFactory testee = createTestee();
        final AuthenticationInfo authenticationInfo = new AuthenticationInfo(
            "cn=doe,dc=example,dc=com", 
            new char[] {'p','a','s','s','w','o','r','d'}
        );
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory = testee.createConnectionFactory(connectionManager);
        // Act & Assert
        Assertions.assertThrowsExactly(LdapAuthenticationException.class,()-> connectionFactory.getConnection(authenticationInfo));
    }
    
    @Test
    void withoutAuthenticationLookupForExistingGroupSucceeds() throws ResourceException, LdapException, IOException {
    	// Arrange
        final ManagedConnectionFactory testee = createTestee();
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory = testee.createConnectionFactory(connectionManager);
        // Act
        final String commonName = readCommonNameOfChemistsWithContainerAuthentication(connectionFactory);
        final String objectClass = readObjectClassOfChemistsWithContainerAuthentication(connectionFactory);
        // Assert
        Assertions.assertEquals("Chemists", commonName);
        Assertions.assertEquals("groupOfUniqueNames", objectClass);
    }

    @Test
    void  withoutAuthenticationLookupForNonExistingGroupReturnsNull() throws ResourceException, LdapException, IOException {
    	// Arrange
        final ManagedConnectionFactory testee = createTestee();
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory = testee.createConnectionFactory(connectionManager);
        // Act
        final Entry entry = readObjectClassOfAlchemistsWithContainerAuthentication(connectionFactory);
        // Assert
        Assertions.assertNull(entry);
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
            final String commonName = readCommonNameOfGaussWithApplicationAuthentication(connectionFactory, authenticationInfo);
            Assertions.assertEquals("Carl Friedrich Gauss", commonName);
        } catch (LdapException exception) {
            exception.printStackTrace();
            return;
        }
        waitForOperator("Disconnect from Internet, please");
        try {
            readCommonNameOfGaussWithApplicationAuthentication(connectionFactory, authenticationInfo);
            Assertions.fail("You forgot to disconnect!");
        } catch (LdapException expected) {
            waitForOperator("Reconnect to the Internet, please");
        }
        try {
            final String commonName = readCommonNameOfGaussWithApplicationAuthentication(connectionFactory, authenticationInfo);
            Assertions.assertEquals("Carl Friedrich Gauss", commonName);
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

    private static String readCommonNameOfGaussWithContainerAuthentication(
    	final ConnectionFactory<LdapConnection, LdapException> connectionFactory
    ) throws LdapException, IOException {
        try (LdapConnection ldap = connectionFactory.getConnection()) {
            return getCommonNameOfGauss(ldap);
        }
    }

    private static String readCommonNameOfChemistsWithContainerAuthentication(
    	final ConnectionFactory<LdapConnection, LdapException> connectionFactory
    ) throws LdapException, IOException {
        try (LdapConnection ldap = connectionFactory.getConnection()) {
            return getCommonNameOfChemists(ldap);
        }
    }

    private static String readObjectClassOfChemistsWithContainerAuthentication(
    	final ConnectionFactory<LdapConnection, LdapException> connectionFactory
    ) throws LdapException, IOException {
        try (LdapConnection ldap = connectionFactory.getConnection()) {
            return getObjectClassOfChemists(ldap);
        }
    }

    private static Entry readObjectClassOfAlchemistsWithContainerAuthentication(
    	final ConnectionFactory<LdapConnection, LdapException> connectionFactory
    ) throws LdapException, IOException {
        try (LdapConnection ldap = connectionFactory.getConnection()) {
            return getObjectClassOfAlchemists(ldap);
        }
    }
    
    private static String readCommonNameOfGaussWithApplicationAuthentication(
        final ConnectionFactory<LdapConnection, LdapException> connectionFactory,
        final AuthenticationInfo authenticationInfo
    ) throws LdapException, IOException {
        try (LdapConnection ldap = connectionFactory.getConnection(authenticationInfo)){
            return getCommonNameOfGauss(ldap);
        }
    }

    private static String getCommonNameOfChemists(LdapConnection ldap) throws LdapException {
        final Entry entry = ldap.lookup("ou=chemists,dc=example,dc=com","cn");
        final Attribute cn = entry.get("cn");
        final Value value = cn.get(); // the first value
        return value.getString();
    }

    private static String getObjectClassOfChemists(LdapConnection ldap) throws LdapException {
        final Entry entry = ldap.lookup("ou=chemists,dc=example,dc=com","objectClass");
        final Attribute objectClass = entry.get("objectClass");
        final Value value = objectClass.get(); // the first value
        return value.getString();
    }

    private static Entry getObjectClassOfAlchemists(LdapConnection ldap) throws LdapException {
        return ldap.lookup("ou=alchemists,dc=example,dc=com","objectClass");
    }
    
    private static String getCommonNameOfGauss(LdapConnection ldap) throws LdapException {
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
