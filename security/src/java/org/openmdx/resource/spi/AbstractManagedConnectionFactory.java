/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: AbstractManagedConnectionFactory.java,v 1.6 2011/08/20 19:55:06 hburger Exp $
 * Description: Abstract Managed URL Connection Factory
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/08/20 19:55:06 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2010, OMEX AG, Switzerland
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
package org.openmdx.resource.spi;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.openmdx.kernel.resource.spi.ShareableConnectionManager;

/**
 * Abstract Managed URL Connection Factory
 */
public abstract class AbstractManagedConnectionFactory implements ManagedConnectionFactory {

	/**
	 * Constructor
	 */
	protected AbstractManagedConnectionFactory() {
		super();
	}

	/**
	 * Implements <code>Serializable</code>
	 */
	private static final long serialVersionUID = 8606343080939877576L;

	/**
	 * Lazily calculated hash code
	 */
	private volatile int hash = 0;

	/**
	 * 
	 */
	private PrintWriter logWriter;

	/**
	 * The 'ConnectionURL' property
	 */
	private String connectionURL;

	/**
	 * The connection user name
	 */
	private String userName;

	/**
	 * The connection password
	 */
	private String password;

    /**
     * The resource adapter's internal connection manager
     */
    private ConnectionManager connectionManager;
    
    /**
     * An empty password
     */
    private static final char[] EMPTY_PASSWORD = {};
    
    /**
     * An empty user name
     */
    private static final String EMPTY_USER_NAME = "";    

    /**
     * Tells whether this factory creates shareable connections
     * 
     * @return <code>true</code> if this factory creates shareable connections
     */
    protected abstract boolean isManagedConnectionShareable(
    );
    
    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#matchManagedConnections(java.util.Set, javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    
//  @Override
	@SuppressWarnings("rawtypes")
    public final ManagedConnection matchManagedConnections(
        Set managedConnections,
        Subject subject,
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        PasswordCredential credential = this.getCredential(subject);
        for(Object managedConnection : managedConnections) {
            if(managedConnection instanceof AbstractManagedConnection) {
            	AbstractManagedConnection candidate = (AbstractManagedConnection) managedConnection;
            	if(candidate.matches(credential)) {
            		return candidate;
            	}
            }
        }
        return null;
    }
    
    /**
     * Retrieve the resource adapter's internal connection manager
     * 
     * @return the resource adapter's internal connection manager
     */
	ConnectionManager getConnectionManager(
    ){
        if(this.connectionManager == null) {
            String password = this.getPassword();
            String userName = this.getUserName();
            PasswordCredential credential = new PasswordCredential(
                userName == null ? EMPTY_USER_NAME : userName,
                password == null ? EMPTY_PASSWORD : password.toCharArray()
            );
            credential.setManagedConnectionFactory(this);
            Set<PasswordCredential> credentials = Collections.singleton(credential);
            this.connectionManager = this.isManagedConnectionShareable() ? new ShareableConnectionManager(
                credentials
            ) : new SimpleConnectionManager(
                credentials
            );
        }
        return this.connectionManager;
    }
    
    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory()
     */
    public Object createConnectionFactory(
    ) throws ResourceException {
        return createConnectionFactory(null);
    }

	/* (non-Javadoc)
	 * @see javax.resource.spi.ManagedConnectionFactory#getLogWriter()
	 */
	public final PrintWriter getLogWriter(
	){
		return this.logWriter;
	}

	/**
	 * Retrieve the password credential from the subject
	 * 
	 * @param subject
	 * 
	 * @return the credential
	 * 
	 * @throws ResourceException
	 */
	protected PasswordCredential getCredential(
		Subject subject
	) throws ResourceException{
		if(subject != null) {
			for(PasswordCredential credential : subject.getPrivateCredentials(PasswordCredential.class)) {
				if(credential.getManagedConnectionFactory() == this) {
					return credential;
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.resource.spi.ManagedConnectionFactory#setLogWriter(java.io.PrintWriter)
	 */
	public void setLogWriter(
		PrintWriter logWriter
	) throws ResourceException {
		this.logWriter = logWriter;
	}

	/**
	 * Retrieve connectionURL.
	 *
	 * @return Returns the connectionURL.
	 */
	public String getConnectionURL() {
		return this.connectionURL;
	}

	/**
	 * Set connectionURL.
	 * 
	 * @param connectionURL The connectionURL to set.
	 */
	public void setConnectionURL(
		String connectionURL
	) {
		this.connectionURL = connectionURL;
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
			PrintWriter logWriter = this.getLogWriter();
			if(logWriter != null) {
				exception.printStackTrace(logWriter);
			}
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
	public final String getPassword() {
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
	 * Overriding equals() is required.
	 */
	@Override
	public boolean equals(
		Object other
	) {
		if(this == other) {
			return true;
		} else if(
			other == null || 
			this.connectionURL == null || 
			this.getClass() != other.getClass()
		){
			return false;
		} else {
			AbstractManagedConnectionFactory that = (AbstractManagedConnectionFactory) other;
			return 
				this.connectionURL.equals(that.connectionURL) && (
					this.userName == null ? that.userName == null : this.userName.equals(that.userName)
				) && (
					this.password == null ? that.password == null : this.password.equals(that.password)
				);
		}
	}

	/**
	 * Overriding hashCode() is required.
	 */
	@Override
	public int hashCode(
	) {
		if(this.hash == 0) synchronized(this) {
			if(this.hash == 0) {
				if(this.connectionURL == null) {
					this.hash = System.identityHashCode(this);
				} else {
					int hash = this.connectionURL.hashCode();
					if(this.userName != null) {
						hash += this.userName.hashCode();
					}
					if(this.password != null) {
						hash += this.password.hashCode();
					}
					this.hash = hash;
				}
			}
		}
		return this.hash;
	}

}
