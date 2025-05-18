/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Abstract managed connection
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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

#if JAVA_8
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.security.PasswordCredential;
#else
import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.LocalTransaction;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ManagedConnectionMetaData;
import jakarta.resource.spi.security.PasswordCredential;
#endif
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.openmdx.base.collection.Sets;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.resource.cci.AuthenticationInfo;


/**
 * Abstract managed connection
 */
public abstract class AbstractManagedConnection<F extends ManagedConnectionFactory> implements ManagedConnection {

	/**
	 * Constructor
	 */
    protected AbstractManagedConnection(
    	F factory,
    	final String eisProductName,
        final String eisProductVersion, 
        final PasswordCredential credential,
        ConnectionRequestInfo connectionRequestInfo
    ) {
        this.factory = factory;
        this.credential = credential;
        this.connectionRequestInfo = connectionRequestInfo;
        this.metaData = new ManagedConnectionMetaData(){

			@Override
            public String getEISProductName() throws ResourceException {
	            return eisProductName;
            }

			@Override
            public String getEISProductVersion() throws ResourceException {
	            return eisProductVersion;
            }

			@Override
            public int getMaxConnections() throws ResourceException {
	            return 0; // no limit
            }

			@Override
            public String getUserName() throws ResourceException {
				return credential == null ? null : credential.getUserName();
            }
        	
        };
	}

    /**
     * The managed connection factory
     */
    private final F factory;
    
	/**
	 * Eagerly initialized meta data
	 */
	private final ManagedConnectionMetaData metaData;

	/**
     * The managed connection's credentials
     */
	private final PasswordCredential credential;

	/**
	 * The managed connection's request info
	 */
	private final ConnectionRequestInfo connectionRequestInfo;
	
	/**
     * The managed connection's log writer
     */
    private PrintWriter logWriter;

    /**
     * The connection event listener registry
     */
    private final Set<ConnectionEventListener> listeners = Sets.asSet(new IdentityHashMap<ConnectionEventListener,Object>());

    /**
     * The connection registry
     */
    private final Set<AbstractConnection> connections = Sets.asSet(new IdentityHashMap<AbstractConnection,Object>());
    
    
    
    /**
     * Retrieve this instance's factory
     * 
     * @return the factory
     */
    protected F getManagedConnectionFactory() {
        return factory;
    }

    @Override
	public void addConnectionEventListener(
        ConnectionEventListener connectionEventListener
    ) {
		synchronized(this.listeners){
	    	this.listeners.add(connectionEventListener);
		}
    }

    @Override
	public LocalTransaction getLocalTransaction(
    ) throws ResourceException {
        throw log(
        	new NotSupportedException(
        		getClass().getName() + " is non-transactional"
        	),
        	true
        );
    }

    @Override
    public PrintWriter getLogWriter(
    ){
        return this.logWriter;
    }

    @Override
	public ManagedConnectionMetaData getMetaData(
    ) throws ResourceException {
        return this.metaData;
    }

    @Override
    public XAResource getXAResource(
    ) throws ResourceException {
        throw this.log(
        	new NotSupportedException(
        		getClass().getName() + " is non-transactional"
        	),
        	true
        );
    }

    @Override
    public void removeConnectionEventListener(
        ConnectionEventListener connectionEventListener
    ) {
		synchronized(this.listeners){
	    	this.listeners.remove(connectionEventListener);
		}
    }

    @Override
    public void setLogWriter(
        PrintWriter logWriter
    ) throws ResourceException {
        this.logWriter = logWriter;        
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
     * Tests whether the managed connection matches
     * 
     * @param credential the expected credential
     * @param connectionRquestInfo additional connection request info
     * 
     * @return {@code true} if the managed connection matches
     */
    protected abstract boolean matches(
    	Object credential,
    	ConnectionRequestInfo connectionRequestInfo
    );

    /**
     * Tests whether the credentials match
     * 
     * @param credential the expected credential
     * 
     * @return {@code true} if the credentials match
     */
    protected boolean credentialsMatch(
        Object credential
    ) {
        return Objects.equals(this.credential, credential);
    }

    /**
     * Tests whether connection request information matches
     * 
     * @param connectionRquestInfo additional connection request info
     * 
     * @return {@code true} if the connection request information matches
     */
    protected boolean connectionRequestInformationMatches(
        ConnectionRequestInfo connectionRequestInfo
    ) {
        return Objects.equals(this.connectionRequestInfo, connectionRequestInfo);
    }

    @Override
    public void destroy(
    ) throws ResourceException {
        PasswordCredentials.destroy(credential);
    }

    @Override
    public void cleanup(
    )throws ResourceException {
    	synchronized(this.connections) {
    		for(
    			Iterator<AbstractConnection> i = this.connections.iterator();
    			i.hasNext();
    		){
    			i.next().associateManagedConnection(null);
    			i.remove();
    		}
    	}
    }

	/**
     * Dissociate the connection from this managed connection
     * 
     * @param connection the connection to dissociate
     * @param signal 
     */
    void dissociateConnection(
        Object connection, 
        boolean signal
    ) {
    	synchronized (this.connections) {
        	this.connections.remove(connection);
        }
    	if(signal) synchronized (this.listeners) {
	        ConnectionEvent event = new ConnectionEvent(
	        	this,
	        	ConnectionEvent.CONNECTION_CLOSED
	        );
	        event.setConnectionHandle(connection);
        	for(ConnectionEventListener listener : this.listeners) {
        		listener.connectionClosed(event);
        	}
        }
    }
    
    /**
     * Tells whether the managed connection is idle
     * 
     * @return {@code true} if the managed connection is idle
     */
    boolean isIdle(){
    	return this.connections.isEmpty();
    }
    
    @Override
    public void associateConnection(
        Object connection
    ) throws ResourceException {
    	if(connection instanceof AbstractConnection) synchronized(this.connections) {
        	AbstractConnection connectionProxy = (AbstractConnection) connection;
    		connectionProxy.associateManagedConnection(this);
    		this.connections.add(connectionProxy);
    	} else {
            throw ResourceExceptions.initHolder(
            	new ResourceException(
	                "Managed connection class and connection class do not match",
	                BasicException.newEmbeddedExceptionStack(
	                	BasicException.Code.DEFAULT_DOMAIN,
	                	BasicException.Code.BAD_PARAMETER,
	                	new BasicException.Parameter("managedConnectionFactory", this.getClass().getName()),
	                	new BasicException.Parameter("connection", connection == null ? null : connection.getClass().getName())
	                )
	            )
            );
    	}
    }
    
    /**
     * Create a new connection handle
     * @param subject TODO
     * @param connectionRequestInfo TODO
     * 
     * @return a new connection handle
     */
    protected abstract Object newConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException;

    @Override
    public Object getConnection(
        Subject subject, 
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
    	Object connection = newConnection(subject, connectionRequestInfo);
        this.associateConnection(connection);
        return connection;
    }

    
    /**
     * 
     * 
     * @return the credential
     */
    protected PasswordCredential getCredential(
        Subject subject, 
        ConnectionRequestInfo connectionRequestInfo
    ) {
        if(connectionRequestInfo instanceof AuthenticationInfo) {
            return PasswordCredentials.newPasswordCredential(getManagedConnectionFactory(), (AuthenticationInfo)connectionRequestInfo);
        }
        if(subject != null) {
            PasswordCredentials.getPasswordCredential(getManagedConnectionFactory(), subject);
        }
        return credential;
    }
    
}
