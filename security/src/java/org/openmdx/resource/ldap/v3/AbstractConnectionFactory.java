/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractConnectionFactory.java,v 1.9 2010/03/05 13:24:13 hburger Exp $
 * Description: Managed LDAP Connection Factory
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/05 13:24:13 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2009, OMEX AG, Switzerland
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
package org.openmdx.resource.ldap.v3;

import java.util.Collections;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.openmdx.kernel.resource.spi.ShareableConnectionManager;
import org.openmdx.resource.ldap.spi.AbstractManagedConnectionFactory;
import org.openmdx.resource.ldap.spi.ConnectionFactory;
import org.openmdx.resource.ldap.spi.ManagedConnection;

/**
 * Managed LDAP Connection Factory
 */
public abstract class AbstractConnectionFactory extends AbstractManagedConnectionFactory {

	/**
     * Constructor
     */
    protected AbstractConnectionFactory() {
	    super();
    }

	/**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 4843002483599509129L;

	/**
	 * Default LDAP protocol version
	 */
	protected static int DEFAULT_PROTOCOL_VERSION = 2;
	
    /**
     * The LDAP protocol version
     */
    private int protocolVersion = DEFAULT_PROTOCOL_VERSION;
    
    /**
     * The resource adapter's internal connection manager
     */
    private ConnectionManager connectionManager = null;

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory()
     */
    public final Object createConnectionFactory(
    ) throws ResourceException {
        if(this.connectionManager == null) {
            String password = super.getPassword();
            this.connectionManager = new ShareableConnectionManager(
                Collections.singleton(
                    new PasswordCredential(
                        super.getUserName(),
                        (password == null ? "" : password).toCharArray()
                    )
                )
            );
        }
        return createConnectionFactory(this.connectionManager);
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory(javax.resource.spi.ConnectionManager)
     */
    public final Object createConnectionFactory(
        ConnectionManager connectionManager
    ) throws ResourceException {
        return connectionManager == null ?
        	this.createConnectionFactory() :
            new ConnectionFactory(this, connectionManager);
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#matchManagedConnections(java.util.Set, javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    @SuppressWarnings("unchecked")
	public final ManagedConnection matchManagedConnections(
        Set managedConnections,
        Subject subject,
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        PasswordCredential credential = this.getCredential(subject);
        for(Object managedConnection : managedConnections) {
            if(managedConnection instanceof ManagedConnection) {
            	ManagedConnection candidate = (ManagedConnection) managedConnection;
            	if(candidate.matches(credential)) {
            		return candidate;
            	}
            }
        }
        return null;
    }
    
    public Integer getProtocolVersion() {
		return this.protocolVersion;
	}


	public void setProtocolVersion(Integer protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

}
