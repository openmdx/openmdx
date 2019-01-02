/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Simple Connection Manager
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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

import java.util.HashSet;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;


/**
 * Simple Connection Manager
 */
public class SimpleConnectionManager 
    extends AbstractConnectionManager 
    implements ConnectionEventListener
{

    /**
     * Constructor
     * 
     * @param credentials
     */
    public SimpleConnectionManager(
        Set<?> credentials
    ) {
        super(credentials);
    }

    /**
     * Implements <code>Serializable</code>.
     */
    private static final long serialVersionUID = -5805446410031847773L;

    /**
     * The managed connections to be shared
     */
    private final Set<ManagedConnection> idleConnections = new HashSet<ManagedConnection>();

    /* (non-Javadoc)
     * @see org.openmdx.kernel.resource.spi.AbstractConnectionManager#allocateManagedConnection(javax.security.auth.Subject, javax.resource.spi.ManagedConnectionFactory, javax.resource.spi.ConnectionRequestInfo)
     */
    @Override
    protected ManagedConnection allocateManagedConnection(
    	Subject subject, 
    	ManagedConnectionFactory managedConnectionFactory, 
    	ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
    	ManagedConnection managedConnection =  super.allocateManagedConnection(
    		subject, 
    		managedConnectionFactory, 
    		connectionRequestInfo
    	);
    	managedConnection.addConnectionEventListener(this);
    	return managedConnection;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ConnectionEventListener#connectionClosed(javax.resource.spi.ConnectionEvent)
     */
    @Override
    public void connectionClosed(ConnectionEvent closeEvent) {
        Object source = closeEvent.getSource();
        if(source instanceof AbstractManagedConnection){
        	AbstractManagedConnection<?> managedConnection = (AbstractManagedConnection<?>) source;
        	if(managedConnection.isIdle()) {
	            this.idleConnections.add(managedConnection);
        	}
        }
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ConnectionEventListener#connectionErrorOccurred(javax.resource.spi.ConnectionEvent)
     */
    @Override
    public void connectionErrorOccurred(ConnectionEvent arg0) {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ConnectionEventListener#localTransactionCommitted(javax.resource.spi.ConnectionEvent)
     */
    @Override
    public void localTransactionCommitted(ConnectionEvent arg0) {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ConnectionEventListener#localTransactionRolledback(javax.resource.spi.ConnectionEvent)
     */
    @Override
    public void localTransactionRolledback(ConnectionEvent arg0) {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ConnectionEventListener#localTransactionStarted(javax.resource.spi.ConnectionEvent)
     */
    @Override
    public void localTransactionStarted(ConnectionEvent arg0) {
        // nothing to do
    }

}
