/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Managed LDAP Connection Factory
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
package org.openmdx.resource.ldap.ldif;

import java.net.MalformedURLException;
import java.net.URL;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

import org.apache.directory.api.ldap.model.exception.LdapConfigurationException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.template.exception.LdapRuntimeException;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.resource.ldap.spi.ConnectionFactory;
import org.openmdx.resource.ldap.spi.ManagedConnection;
import org.openmdx.resource.spi.AbstractManagedConnectionFactory;


/**
 * Managed LDIF Connection Factory
 */
public class ManagedConnectionFactory extends AbstractManagedConnectionFactory {

    /**
     * Constructor
     */
    public ManagedConnectionFactory() {
	    super();
    }

    /**
     * The lazily initialized repository
     */
    private Repository repository;
    
    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = 1393224964772754034L;
    
    /**
     * Set the LDAP protocol version
     * 
     * @param protocolVersion the LDAP protocol version
     */
    public void setProtocolVersion(
        int protocolVersion
    ) {
        if(protocolVersion != LdapConnectionConfig.LDAP_V3) {
            throw new LdapRuntimeException(
                new LdapConfigurationException("Only LDAP version " + LdapConnectionConfig.LDAP_V3 + " is supported")
            );
        }
    }

    /**
     * Retrieve the LDAP protocol version
     * 
     * @return the LDAP protocol version
     */
    public int getProtocolVersion() {
        return LdapConnectionConfig.LDAP_V3;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory(javax.resource.spi.ConnectionManager)
     */
    @Override
    public org.openmdx.resource.cci.ConnectionFactory<LdapConnection,LdapException> createConnectionFactory(
        ConnectionManager connectionManager
    ) throws ResourceException {
        return new ConnectionFactory(
            this, 
            connectionManager
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.resource.spi.AbstractManagedConnectionFactory#createConnectionFactory()
     */
    @SuppressWarnings("unchecked")
    @Override
    public org.openmdx.resource.cci.ConnectionFactory<LdapConnection,LdapException> createConnectionFactory()
        throws ResourceException {
        return (org.openmdx.resource.cci.ConnectionFactory<LdapConnection, LdapException>) super.createConnectionFactory();
    }

    private Repository getRepository() throws ResourceException {
        if(this.repository == null) {
            this.repository = new Repository(toURL(getConnectionURL()));
        }
        return this.repository;
    }

    private URL toURL(
        final String uri
    ) throws ResourceException {
        try {
            return new URL(uri);
        } catch (MalformedURLException e) {
            throw ResourceExceptions.toResourceException(e);
        }
    }
    @Override
    protected javax.resource.spi.ManagedConnection newManagedConnection(
        Subject subject,
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        return new ManagedConnection(this, null, getRepository());
    }

    //------------------------------------------------------------------------
    // Extends AbstractManagedConnectionFactory
    //------------------------------------------------------------------------
    
    /**
     * Overriding required for Oracle WebLogic
     * 
     * @see org.openmdx.resource.spi.AbstractManagedConnectionFactory#equals(java.lang.Object)
     */
    @Override
    public boolean equals(
    	Object that
    ) {
        return super.equals(that);
    }

	/**
     * Overriding required for Oracle WebLogic
     * 
     * @see org.openmdx.resource.spi.AbstractManagedConnectionFactory#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
}
