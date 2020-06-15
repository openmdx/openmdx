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
package org.openmdx.resource.ldap.v3;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ValidatingManagedConnectionFactory;
import javax.security.auth.Subject;

import org.apache.directory.api.ldap.model.exception.LdapConfigurationException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.DefaultLdapConnectionFactory;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapConnectionFactory;
import org.apache.directory.ldap.client.template.exception.LdapRuntimeException;
import org.openmdx.resource.ldap.spi.ConnectionFactory;
import org.openmdx.resource.ldap.spi.ManagedConnection;
import org.openmdx.resource.spi.AbstractManagedConnectionFactory;

/**
 * Managed LDAP Connection Factory
 */
public class ManagedConnectionFactory 
    extends AbstractManagedConnectionFactory 
    implements ValidatingManagedConnectionFactory
{

    /**
     * Constructor
     */
    public ManagedConnectionFactory() {
        this.ldapConnectionConfig = new LdapConnectionConfig();
        this.ldapConnectionFactory = new DefaultLdapConnectionFactory(ldapConnectionConfig);
    }

	/**
	 * Implements {@code Serializable}
	 */
    private static final long serialVersionUID = -8107927475529647385L;

    /**
     * The LDAP connection configuration is initialized by the managed connection factory's setters.
     */
    private final LdapConnectionConfig ldapConnectionConfig;
    
    /**
     * The LDAP connection factory's configuration is used by reference. 
     */
    private final LdapConnectionFactory ldapConnectionFactory;
    
    /**
     * @deprecated use {@link #setServerName(String)},
     * {@link #setPortNumber(int)} and {@link #setUseSsl(boolean)}
     */
    @Deprecated
    public void setConnectionURL(
        String connectionURL
    ){
        final URI connectionURI = URI.create(connectionURL);
        final String ldapScheme = connectionURI.getScheme();
        if(ldapScheme != null) {
            this.ldapConnectionConfig.setUseSsl("ldaps".equalsIgnoreCase(ldapScheme));
        }
        final int ldapPort = connectionURI.getPort();
        if(ldapPort >= 0) {
            this.ldapConnectionConfig.setLdapPort(ldapPort);
        } else if (isUseSsl()) {
            this.ldapConnectionConfig.setLdapPort(ldapConnectionConfig.getDefaultLdapsPort());
        } else {
            this.ldapConnectionConfig.setLdapPort(ldapConnectionConfig.getDefaultLdapPort());
        }
        final String ldapHost = connectionURI.getHost();
        if(ldapHost != null) {
            this.ldapConnectionConfig.setLdapHost(ldapHost);
        }
        super.setConnectionURL(connectionURL);
    }
    
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

    /**
     * @param useSsl
     * @see org.apache.directory.ldap.client.api.LdapConnectionConfig#setUseSsl(boolean)
     */
    public void setUseSsl(
        boolean useSsl
    ) {
        ldapConnectionConfig.setUseSsl(useSsl);
    }

    /**
     * @return
     * @see org.apache.directory.ldap.client.api.LdapConnectionConfig#isUseSsl()
     */
    public boolean isUseSsl(
        ) {
        return ldapConnectionConfig.isUseSsl();
    }
    
    /**
     * @param ldapPort
     * @see org.apache.directory.ldap.client.api.LdapConnectionConfig#setLdapPort(int)
     */
    public void setPortNumber(
        int ldapPort
    ) {
        ldapConnectionConfig.setLdapPort(ldapPort);
    }

    /**
     * @return
     * @see org.apache.directory.ldap.client.api.LdapConnectionConfig#getLdapPort()
     */
    public int getPortNumber(
        ) {
        return ldapConnectionConfig.getLdapPort();
    }
    
    /**
     * @param ldapHost
     * @see org.apache.directory.ldap.client.api.LdapConnectionConfig#setLdapHost(java.lang.String)
     */
    public void setServerName(
        String ldapHost
    ) {
        ldapConnectionConfig.setLdapHost(ldapHost);
    }

    /**
     * @return
     * @see org.apache.directory.ldap.client.api.LdapConnectionConfig#getLdapHost()
     */
    public String getServerName(
        ) {
        return ldapConnectionConfig.getLdapHost();
    }
    
    /**
     * @param timeout
     * @see org.apache.directory.ldap.client.api.LdapConnectionConfig#setTimeout(long)
     */
    public void setTimeout(
        long timeout
    ) {
        ldapConnectionConfig.setTimeout(timeout);
    }

    /**
     * @return
     * @see org.apache.directory.ldap.client.api.LdapConnectionConfig#getTimeout()
     */
    public long getTimeout(
    ) {
        return ldapConnectionConfig.getTimeout();
    }

    /**
     * @param sslProtocol
     * @see org.apache.directory.ldap.client.api.LdapConnectionConfig#setSslProtocol(java.lang.String)
     */
    public void setSslProtocol(
        String sslProtocol
    ) {
        ldapConnectionConfig.setSslProtocol(sslProtocol);
    }

    /**
     * @return
     * @see org.apache.directory.ldap.client.api.LdapConnectionConfig#getSslProtocol()
     */
    public String getSslProtocol(
    ) {
        return ldapConnectionConfig.getSslProtocol();
    }

    /**
     * @param useTls
     * @see org.apache.directory.ldap.client.api.LdapConnectionConfig#setUseTls(boolean)
     */
    public void setUseTls(
        boolean useTls
    ) {
        ldapConnectionConfig.setUseTls(useTls);
    }

    /**
     * @return
     * @see org.apache.directory.ldap.client.api.LdapConnectionConfig#isUseTls()
     */
    public boolean isUseTls(
        ) {
        return ldapConnectionConfig.isUseTls();
    }

    /**
     * @param name
     * @see org.apache.directory.ldap.client.api.LdapConnectionConfig#setName(java.lang.String)
     */
    public void setName(
        String name
    ) {
        ldapConnectionConfig.setName(name);
    }

    /**
     * @return
     * @see org.apache.directory.ldap.client.api.LdapConnectionConfig#getName()
     */
    public String getName(
        ) {
        return ldapConnectionConfig.getName();
    }
    
    /**
     * @param credentials
     * @see org.apache.directory.ldap.client.api.LdapConnectionConfig#setCredentials(java.lang.String)
     */
    public void setCredentials(
        String credentials
    ) {
        ldapConnectionConfig.setCredentials(credentials);
    }

    /**
     * @return
     * @see org.apache.directory.ldap.client.api.LdapConnectionConfig#getCredentials()
     */
    public String getCredentials(
    ) {
        return ldapConnectionConfig.getCredentials();
    }

    @Override
    protected javax.resource.spi.ManagedConnection newManagedConnection(
        Subject subject,
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
		return new ManagedConnection(
            this,
            getPasswordCredential(subject),
            ldapConnectionFactory.newUnboundLdapConnection()
         );
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory(javax.resource.spi.ConnectionManager)
     */
    @Override
    public org.openmdx.resource.cci.ConnectionFactory<LdapConnection,LdapException> createConnectionFactory(
        ConnectionManager cxManager
    ) throws ResourceException {
        return new ConnectionFactory(this, cxManager);
    }
    
    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory(javax.resource.spi.ConnectionManager)
     */
    @Override
    public org.openmdx.resource.cci.ConnectionFactory<LdapConnection,LdapException> createConnectionFactory(
    ) throws ResourceException {
        return (ConnectionFactory) super.createConnectionFactory();
    }
    
    //------------------------------------------------------------------------
    // Implements ValidatingManagedConnectionFactory
    //------------------------------------------------------------------------
    
    @SuppressWarnings("rawtypes")
    @Override
    public Set getInvalidConnections(
        Set connectionSet
    ) throws ResourceException {
        if(connectionSet.size() == 1) {
            for(Object connection : connectionSet) {
                if(connection instanceof ManagedConnection) {
                    final ManagedConnection candidate = (ManagedConnection) connection;
                    return candidate.isInvalid() ? Collections.singleton(candidate) : Collections.emptySet();
                }
            }
        }
        final Set<ManagedConnection> invalidConnections = new HashSet<>();
        for(Object connection : connectionSet) {
            if(connection instanceof ManagedConnection) {
                final ManagedConnection candidate = (ManagedConnection) connection;
                if(candidate.isInvalid()) {
                    invalidConnections.add(candidate);
                }
            }
        }
        return invalidConnections;
    }

    
    //------------------------------------------------------------------------
    // Extends AbstractManagedConnectionFactory
    //------------------------------------------------------------------------
    
    /**
     * Overriding required for Oracle WebLogic
     */
    @Override
    public boolean equals(
    	Object that
    ) {
        return this == that;
    }

	/**
     * Overriding required for Oracle WebLogic
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
