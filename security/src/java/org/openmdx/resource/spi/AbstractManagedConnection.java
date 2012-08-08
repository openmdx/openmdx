/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractManagedConnection.java,v 1.4 2010/09/01 15:07:59 hburger Exp $
 * Description: Abstract managed connection
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/09/01 15:07:59 $
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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.openmdx.base.collection.Sets;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.kernel.exception.BasicException;


/**
 * Abstract managed connection
 */
public abstract class AbstractManagedConnection implements ManagedConnection {

	/**
	 * Constructor
	 * 
	 * @param eisProductName
	 * @param eisProductVersion
	 * @param credential
	 */
    protected AbstractManagedConnection(
    	final String eisProductName,
    	final String eisProductVersion,
        final PasswordCredential credential
    ) {
        this.credential = credential;
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
				return credential.getUserName();
            }
        	
        };
	}

	/**
	 * Eagerly initialized meta data
	 */
	private final ManagedConnectionMetaData metaData;

	/**
     * The managed connection's credentials
     */
	private PasswordCredential credential;

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
    
    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#addConnectionEventListener(javax.resource.spi.ConnectionEventListener)
     */
//  @Override
	public void addConnectionEventListener(
        ConnectionEventListener connectionEventListener
    ) {
		synchronized(this.listeners){
	    	this.listeners.add(connectionEventListener);
		}
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#getLocalTransaction()
     */
//  @Override
	public LocalTransaction getLocalTransaction(
    ) throws ResourceException {
        throw this.log(
        	new NotSupportedException(
        		getClass().getName() + " is non-transactional"
        	)
        );
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#getLogWriter()
     */
//  @Override
    public PrintWriter getLogWriter(
    ) throws ResourceException {
        return this.logWriter;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#getMetaData()
     */
//	@Override
	public ManagedConnectionMetaData getMetaData(
    ) throws ResourceException {
        return this.metaData;
    }

     /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#getXAResource()
     */
//	@Override
    public XAResource getXAResource(
    ) throws ResourceException {
        throw this.log(
        	new NotSupportedException(
        		getClass().getName() + " is non-transactional"
        	)
        );
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#removeConnectionEventListener(javax.resource.spi.ConnectionEventListener)
     */
//	@Override
    public void removeConnectionEventListener(
        ConnectionEventListener connectionEventListener
    ) {
		synchronized(this.listeners){
	    	this.listeners.remove(connectionEventListener);
		}
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#setLogWriter(java.io.PrintWriter)
     */
//  @Override
    public void setLogWriter(
        PrintWriter logWriter
    ) throws ResourceException {
        this.logWriter = logWriter;        
    }
    
    /**
     * Log and return an exception
     * 
     * @param an exception
     * 
     * @return the exception
     */
    protected ResourceException log(
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
     * Tests whether connection's credential matches the expected credential
     * 
     * @param credential the expected credential
     */
    boolean matches(
    	Object credential
    ){
    	return this.credential == null ? credential == null : this.credential.equals(credential);
    }

	/* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#destroy()
     */
//  @Override
    public void destroy(
    ) throws ResourceException {
        this.credential = null;
    }

//  @Override
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
     * @return <code>true</code> if the managed connection is idle
     */
    boolean isIdle(){
    	return this.connections.isEmpty();
    }
    
//  @Override
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
     * 
     * @return a new connection handle
     */
    protected abstract Object newConnection(
    ) throws ResourceException;

	/* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#getConnection(javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    public Object getConnection(
        Subject subject, 
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
    	Object connection = newConnection();
        this.associateConnection(connection);
        return connection;
    }
    
}
