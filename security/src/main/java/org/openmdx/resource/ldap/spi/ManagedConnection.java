/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Managed LDAP Connection 
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.resource.ldap.spi;

import java.io.IOException;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.SecurityException;
import javax.resource.spi.security.PasswordCredential;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.EISSystemException;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.SecurityException;
import jakarta.resource.spi.security.PasswordCredential;
#endif
import javax.security.auth.Subject;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.openmdx.resource.spi.AbstractManagedConnection;
import org.openmdx.resource.spi.PasswordCredentials;

/**
 * Managed LDAP Connection
 */
public class ManagedConnection extends AbstractManagedConnection<ManagedConnectionFactory> {

    /**
     * Constructor 
     */
    public ManagedConnection(
        ManagedConnectionFactory factory,
    	PasswordCredential credential,
        LdapConnection physicalConnection
    ) throws ResourceException {
    	super(factory,"LDAP", String.valueOf(LdapConnectionConfig.LDAP_V3), credential, null);
    	this.physicalConnection = physicalConnection;
    }

    /**
     * The physical connection
     */
    private LdapConnection physicalConnection;

    @Override
    public void destroy(
    ) throws ResourceException {
    	try {
			this.physicalConnection.close();
		} catch (IOException exception) {
			throw this.log(
				new EISSystemException(
					"LDAP disconnection failure",
					exception
				),
				false
			);
		} finally {
    		super.destroy();
	        this.physicalConnection = null;
		}
    }

    public boolean isInvalid() {
        if(this.physicalConnection == null) {
            log("Prune closed connection");
            return true;
        }
        if(this.physicalConnection.isConnected()) {
            return false;
        }
        try {
            this.physicalConnection.connect();
            return false;
        } catch (LdapException e) {
            log("Prune managed connection upon connect failure");
            return true;
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.resource.spi.AbstractManagedConnection#newConnection()
     */
    @Override
    protected Connection newConnection(
        Subject subject, 
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        final Connection connection = new Connection(this.physicalConnection);
        try {
            bind(getCredential(subject, connectionRequestInfo));
            return connection;
        } catch (ResourceException exception) {
            try {
                connection.close();
            } catch (IOException closeException) {
                log(
                    new EISSystemException(
                        "Unable to close connection after authenication failure", 
                        closeException
                    ), 
                    false
               );
            }
            throw exception;
        }
    }

    private void bind(
        final PasswordCredential passwordCredential
    ) throws ResourceException {
        try {
            if(passwordCredential == null) {
                this.physicalConnection.anonymousBind();
            } else if (PasswordCredentials.isPasswordEmpty(passwordCredential)){
                this.physicalConnection.bind(
                    passwordCredential.getUserName()
                );
            } else {
                this.physicalConnection.bind(
                    passwordCredential.getUserName(), 
                    new String(passwordCredential.getPassword())
                );
            }
        } catch (LdapException authenticationException) {
            throw new SecurityException("Authentication failure", authenticationException);
        }
    }

    /**
     * The credentials are not applied to the managed connection itself 
     * but upon its association with a connection handle.
     */
    @Override
    protected boolean matches(
        Object credential,
        ConnectionRequestInfo connectionRequestInfo
    ) {
        return true;
    }

}
