/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractConnectionFactory.java,v 1.1 2007/11/26 14:04:34 hburger Exp $
 * Description: Managed LDAP Connection Factory
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/11/26 14:04:34 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.SecurityException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.openmdx.kernel.application.container.lightweight.ShareableConnectionManager;
import org.openmdx.kernel.text.StringBuilders;
import org.openmdx.resource.ldap.spi.ConnectionFactory;
import org.openmdx.resource.ldap.spi.ManagedConnection;

/**
 * Managed LDAP Connection Factory
 */
public abstract class AbstractConnectionFactory
    implements ManagedConnectionFactory
{

	/**
	 * Default LDAP protocol version
	 */
	protected static int DEFAULT_PROTOCOL_VERSION = 2;
	
	/**
     * 
     */
    private Object identity = null;
    
    /**
     * 
     */
    private PrintWriter logWriter;
    
    /**
     * 'ConnectionURL' property, e.g.<ul>
     * <li>"directory.knowledge.com"
     * <li>"199.254.1.2"
     * <li>"directory.knowledge.com:1050 people.catalog.com 199.254.1.2"
     * </ul>
     */
    private String connectionURL;
    
    /**
     * The distinguished name
     */
    private String userName;
    
    /**
     * The LDAP password
     */
    private String password;

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
            this.connectionManager = new ShareableConnectionManager(
                Collections.singleton(
                    new PasswordCredential(
                        this.userName,
                        (this.password == null ? "" : this.password).toCharArray()
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
            createConnectionFactory() :
            new ConnectionFactory(this, connectionManager);
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#getLogWriter()
     */
    public final PrintWriter getLogWriter(
    ) throws ResourceException {
        return this.logWriter;
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
        PasswordCredential credential = getCredential(subject);
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
    
	/**
     * 
     * @param subject
     * @return
     * @throws ResourceException
     */
    protected PasswordCredential getCredential(
        Subject subject
    ) throws ResourceException{
        Set<?> credentials = subject == null ? Collections.EMPTY_SET : subject.getPrivateCredentials(
            PasswordCredential.class
        );
        switch(credentials.size()) {
            case 0: 
                return null;
            case 1:
            	return (PasswordCredential) credentials.iterator().next();
           default:
               throw log(
                   new SecurityException(
                       "There is more than one " +   
                       PasswordCredential.class.getName() +
                       " instance among the subject's private credentials"
                   )
               );
        }
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#setLogWriter(java.io.PrintWriter)
     */
    public final void setLogWriter(
        PrintWriter logWriter
    ) throws ResourceException {
        this.logWriter = logWriter;
    }

    /**
     * Retrieve connectionURL.
     *
     * @return Returns the connectionURL.
     */
    public final String getConnectionURL() {
        return this.connectionURL;
    }
    
    /**
     * Set connectionURL.
     * 
     * @param connectionURL The connectionURL to set.
     */
    public final void setConnectionURL(
        String connectionURL
    ) {
        this.connectionURL = connectionURL;
    }
    
    public Integer getProtocolVersion() {
		return this.protocolVersion;
	}


	public void setProtocolVersion(Integer protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	/**
     * Log and return an exception
     * 
     * @param an exception
     * 
     * @return the exception
     */
    protected final ResourceException log(
        ResourceException exception
    ){
        try {
            PrintWriter logWriter = getLogWriter();
            if(logWriter != null) exception.printStackTrace(logWriter);
        } catch (Exception ignore) {
            // Ensure that the original exception will be available
        }
        return exception;
    }

    /**
     * Set password.
     * 
     * @param password The password to set.
     */
    public final void setPassword(
        String password
    ) {
        this.password = password;
    }
    
    /**
     * Retrieve password.
     *
     * @return Returns the password.
     */
    protected final String getPassword() {
        return this.password;
    }    

    /**
     * Retrieve userName.
     *
     * @return Returns the userName.
     */
    public final String getUserName() {
        return this.userName;
    }
    
    /**
     * Set userName.
     * 
     * @param userName The userName to set.
     */
    public final void setUserName(
        String userName 
    ) {
        this.userName = userName;
    }

    
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /**
     * 
     * @return
     */
    private Object getIdentity(){
        if(this.identity == null) this.identity = this.connectionURL == null ?
            new Object() :
        this.userName == null && this.password == null ?
            this.connectionURL :
            StringBuilders.newStringBuilder(
                this.connectionURL
            ).append(
                '|'
            ).append(
                this.userName == null ? "" : this.userName
            ).append(
                '|'
            ).append(
                this.password == null ? "" : this.password
            ).toString();
        return this.identity;
    }

    //------------------------------------------------------------------------
    // Extends AbstractManagedConnectionFactory
    //------------------------------------------------------------------------
    
    /**
     * Overriding equals() is required.
     */
    public boolean equals(Object that) {
        return 
            that != null && 
            this.getClass().equals(that.getClass()) &&
            this.getIdentity().equals(((AbstractConnectionFactory)that).getIdentity());
    }

    /**
     * Overriding hashCode() is required.
     */
    public int hashCode(
    ) {
        return getIdentity().hashCode();
    }

}
