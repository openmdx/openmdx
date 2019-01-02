/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Managed LDAP Connection 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2018, OMEX AG, Switzerland
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
package org.openmdx.resource.ldap.spi;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.openmdx.resource.spi.AbstractManagedConnection;
import org.openmdx.resource.spi.PasswordCredentials;

import netscape.ldap.LDAPConnection;
import netscape.ldap.LDAPException;
import netscape.ldap.LDAPv3;


/**
 * Managed LDAP Connection
 */
public class ManagedConnection extends AbstractManagedConnection<AbstractManagedConnectionFactory> {

    /**
     * Constructor 
     */
    public ManagedConnection(
        AbstractManagedConnectionFactory factory,
    	PasswordCredential credential,
        ConnectionRequestInfo connectionRequestInfo, 
        LDAPv3 physicalConnection
    ) throws ResourceException {
    	super(factory,"LDAP", String.valueOf(factory.getProtocolVersion()), credential, connectionRequestInfo);
    	this.physicalConnection = physicalConnection;
    }

    /**
     * The physical connection
     */
    private LDAPv3 physicalConnection;

    @Override
    public void destroy(
    ) throws ResourceException {
    	try {
			this.physicalConnection.disconnect();
		} catch (LDAPException exception) {
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

    /* (non-Javadoc)
     * @see org.openmdx.resource.spi.AbstractManagedConnection#newConnection()
     */
    @Override
    protected Connection newConnection(
        Subject subject, 
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        final LDAPv3 delegate;
        if(this.physicalConnection instanceof Cloneable) {
            // LDAP v3
            delegate = (LDAPv3) ((LDAPConnection)this.physicalConnection).clone();
            final PasswordCredential credential = subject == null ?
                getCredential() :
                PasswordCredentials.getPasswordCredential(getManagedConnectionFactory(), subject);
            if(credential != null) {
                bind(delegate, credential);
            }
        } else {
            // LDIF
            delegate = this.physicalConnection;
        }
	    return new Connection(delegate);
    }

    /**
     * Authenticate 
     * 
     * @param physicalConnection
     *            the physical connection
     * @param credential
     *            the credential
     * 
     * @throws ResourceException
     */
    private void bind(
        final LDAPv3 physicalConnection,
        final PasswordCredential credential
    ) throws ResourceException {
        final String distinguishedName = credential.getUserName();
        final String password = new String(credential.getPassword());
        try {
            physicalConnection.authenticate(getManagedConnectionFactory().getProtocolVersion(), distinguishedName, password);
        } catch (LDAPException exception) {
            switch (exception.getLDAPResultCode()) {
                case LDAPException.NO_SUCH_OBJECT:
                    log(
                        "LDAP authentication failed, user '{0}' does not exist",
                        credential.getUserName()
                    );
                    throw new EISSystemException(
                        "LDAP authentication failed, user does not exist",
                        exception
                    );
                case LDAPException.INVALID_CREDENTIALS:
                    log(
                        "LDAP authentication failed, invalid password for user '{0}'",
                        credential.getUserName()
                    );
                    throw new EISSystemException(
                        "LDAP authentication failed, invalid password",
                        exception
                    );
                default:
                    log(
                        "LDAP authentication failed, invalid password for user '{0}'",
                        credential.getUserName()
                    );
                    throw log(
                        new EISSystemException(
                            "LDAP authentication failed, invalid password",
                            exception
                        ),
                        true
                    );
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.resource.spi.AbstractManagedConnection#matches(javax.resource.spi.ManagedConnectionFactory, java.lang.Object, javax.resource.spi.ConnectionRequestInfo)
     */
    @Override
    protected boolean matches(
        ManagedConnectionFactory factory,
        Object credential,
        ConnectionRequestInfo connectionRequestInfo
    ) {
        return factory == getManagedConnectionFactory();
    }

}
