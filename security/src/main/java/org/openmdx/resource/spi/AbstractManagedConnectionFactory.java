/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: Abstract Managed URL Connection Factory
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
package org.openmdx.resource.spi;

import java.io.PrintWriter;
import java.util.Objects;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

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
     * Implements {@code Serializable}
	 */
	private static final long serialVersionUID = 8606343080939877576L;

	/**
	 * Lazily calculated hash code
	 */
	private int hash = 0;

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
    private ConnectionManager defaultConnectionManager;
    
    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#matchManagedConnections(java.util.Set, javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    @Override
	@SuppressWarnings("rawtypes")
    public final ManagedConnection matchManagedConnections(
        Set managedConnections,
        Subject subject,
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        final PasswordCredential credential = PasswordCredentials.getPasswordCredential(this, subject);
        for(Object managedConnection : managedConnections) {
            if(managedConnection instanceof AbstractManagedConnection) {
            	AbstractManagedConnection candidate = (AbstractManagedConnection) managedConnection;
            	if(candidate.getManagedConnectionFactory() == this  && candidate.matches(credential, connectionRequestInfo)) {
            		return candidate;
            	}
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory()
     */
    public Object createConnectionFactory(
    ) throws ResourceException {
        if(this.defaultConnectionManager == null) {
            this.defaultConnectionManager = new DefaultConnectionManager();
        }
        return createConnectionFactory(defaultConnectionManager);
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
	protected PasswordCredential getPasswordCredential(
		Subject subject
	) throws ResourceException{
		if(subject == null) {
			final String userName = getUserName();
			final String password = getPassword();
			if(userName != null && password != null) {
				final PasswordCredential credential = new PasswordCredential(userName, password.toCharArray());
				credential.setManagedConnectionFactory(this);
				return credential;
			}
			return null;
		} else {
		    return PasswordCredentials.getPasswordCredential(this, subject);
		} 
	}

	/* (non-Javadoc)
	 * @see javax.resource.spi.ManagedConnectionFactory#setLogWriter(java.io.PrintWriter)
	 */
	public void setLogWriter(
		PrintWriter logWriter
	){
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
     * @param an exception t
     * 
     * @return the exception
     */
    protected ResourceException log(
        ResourceException exception,
        boolean printStackTrace
    ){
        LogWriter.log(getLogWriter(), exception, printStackTrace);
        return exception;
    }

    /**
     * Logs a message by replacing the placeholders {0}, {1} etc. by the 
     * arguments' string values
     * 
     * @param target the optional target
     * @param pattern the pattern
     * @param arguments the (optional) arguments
     */
    protected void log(
        String pattern,
        Object... arguments 
    ){
        LogWriter.log(getLogWriter(), pattern, arguments);
    }
    
	/**
	 * Set password.
	 * 
	 * @param password The password to set.
	 */
	public void setPassword(
		String password
	) {
		this.password = password;
	}

	/**
	 * Retrieve password.
	 *
	 * @return Returns the password.
	 */
	public String getPassword() {
		return this.password;
	}    

	/**
	 * Retrieve userName.
	 *
	 * @return Returns the userName.
	 */
	public String getUserName() {
		return this.userName;
	}

	/**
	 * Set userName.
	 * 
	 * @param userName The userName to set.
	 */
	public void setUserName(
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
			final AbstractManagedConnectionFactory that = (AbstractManagedConnectionFactory) other;
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
		if(this.hash == 0) {
		    this.hash = this.connectionURL == null ? System.identityHashCode(
		        this
		    ) : Objects.hash(
	            this.connectionURL,
	            this.userName,
	            this.password
	        );
		}
		return this.hash;
	}

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createManagedConnection(javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    @Override
    public final ManagedConnection createManagedConnection(
        Subject subject,
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        final ManagedConnection managedConnection = newManagedConnection(subject, connectionRequestInfo);
        managedConnection.setLogWriter(getLogWriter());
        return managedConnection;
    }

    /**
     * Callback method for to create a managed connection
     * 
     * @param subject the subject with its credentials
     * @param connectionRequestInfo the connection request info
     * 
     * @return a newly created managed connection
     * 
     * @throws ResourceException
     */
    protected abstract ManagedConnection newManagedConnection(
        Subject subject,
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException;
    
}
