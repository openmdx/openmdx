/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ManagedDatabaseConnection.java,v 1.7 2007/10/10 16:06:04 hburger Exp $
 * Description: Managed Database Connection
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:04 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.application.container.spi.sql;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.security.auth.Subject;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import org.openmdx.kernel.callback.CloseCallback;

/**
 * Managed Database Connection
 */
public class ManagedDatabaseConnection
    implements ManagedConnection, CloseCallback 
{
 
    /**
     * A managed database connection
     */
    XAConnection xaConnection;
    
    /**
     * A database connection
     */
    private Connection connection = null;

    /**
     * Application level connections assoicated with this managed connection
     */
    private final Collection connections = new ArrayList();
    
    /**
     * The connection event listeners.
     */
    private Set listeners = null;
       
    /**
     * The managed coonnection's log writer.
     */
    private PrintWriter logWriter;
    
    /**
     * LocalTransaction cache
     */
    private final LocalTransaction localTransaction = new DatabaseTransaction();
    
    /**
     * ManagedConnectionMetaData cache
     */
    private final ManagedConnectionMetaData metaData = new MetaData();

    /**
     * Keep the factory reference for matching 
     */
    ManagedConnectionFactory managedConnectionFactory;
    
    /**
     * Constructor
     */
    public ManagedDatabaseConnection (
        ManagedConnectionFactory managedConnectionFactory,
        XAConnection xaConnection
    ) throws ResourceException {
        this.managedConnectionFactory = managedConnectionFactory;
        this.xaConnection = xaConnection;
    }
    
    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#getConnection(javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    public Object getConnection(
         Subject subject, 
         ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        throw propagate(
            new NotSupportedException("Re-authentication not supported")
        );
    }
    
    /**
     * @return a connection handle
     * 
     * @throws SQLException
     */
    synchronized Connection getConnection(
    ) throws SQLException{
        if(this.connection == null) this.connection = this.xaConnection.getConnection();
        return this.connection;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#destroy()
     */
    public synchronized void destroy() throws ResourceException {
        try {
            cleanup();
        } catch (ResourceException exception){           
            // ignore
        } finally {
            if(this.xaConnection != null) try {
                this.xaConnection.close();
            } catch (SQLException exception) {
                throw propagate(
                    "Pooled connection destruction failed",
                    exception
                );
            } finally {
                this.xaConnection = null;
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#cleanup()
     */
    public synchronized void cleanup() throws ResourceException {
        try {
            if(this.connection != null) this.connection.close();
        } catch (SQLException exception) {
            propagate("Could not close managed connection handle", exception);
        } finally {
            this.connection = null;
            if(!this.connections.isEmpty()) {
                if(this.logWriter != null) logWriter.println(
                    "Cleanup found " + this.connections.size() + 
                    " open handles for database connection " + this.managedConnectionFactory
                );
                for(
                    Iterator i = this.connections.iterator();
                    i.hasNext();
                ){
                    postClose(i.next());
                    i.remove();
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#associateConnection(java.lang.Object)
     */
    public synchronized void associateConnection(
        Object connection
    ) throws ResourceException {
        try {
            ((DatabaseConnection)connection).setDelegate(this, getConnection());
        } catch (SQLException exception) {
            throw propagate("Could not initialize connection handle", exception);
        }
        this.connections.add(connection);
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.callback.CloseCallback#postClose(java.lang.Object)
     */
    public void postClose(
        Object connection
    ){
        ((DatabaseConnection)connection).setDelegate(null, null);
        this.connections.remove(connection);
        if(this.listeners != null) {
            ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
            event.setConnectionHandle(connection);
            fire(event);
        }
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#addConnectionEventListener(javax.resource.spi.ConnectionEventListener)
     */
    public synchronized void addConnectionEventListener(
        ConnectionEventListener connectionEventListener
    ) {
        if(this.listeners == null) this.listeners = new HashSet();
        this.listeners.add(connectionEventListener);
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#removeConnectionEventListener(javax.resource.spi.ConnectionEventListener)
     */
    public synchronized void removeConnectionEventListener(
        ConnectionEventListener connectionEventListener
    ) {
        if(this.listeners != null) this.listeners.remove(connectionEventListener);
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#getXAResource()
     */
    public XAResource getXAResource(
    ) throws ResourceException {
        try {
            return this.xaConnection.getXAResource();
        } catch (SQLException exception) {
            throw propagate("XAResource retrieval failed", exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#getLocalTransaction()
     */
    public LocalTransaction getLocalTransaction() throws ResourceException {
        return this.localTransaction;
    }
    
    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#getMetaData()
     */
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return this.metaData;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#setLogWriter(java.io.PrintWriter)
     */
    public void setLogWriter(PrintWriter logWriter) throws ResourceException {
        this.logWriter = logWriter;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#getLogWriter()
     */
    public PrintWriter getLogWriter() throws ResourceException {
        return this.logWriter;
    }

    /**
     * Create and log a resource exxception if a logWriter is set.
     * 
     * @param message
     * @param cause
     * 
     * @return the resource exception
     */
    ResourceException propagate(
        String message,
        Exception cause
    ){
        ResourceException exception = new ResourceAdapterInternalException(message, cause);
        return propagate(exception);
    }

    /**
     * Log a resource exxception if a logWriter is set.
     * 
     * @param exception
     * 
     * @return the resource exception
     */
    ResourceException propagate(
        ResourceException exception
    ){
        if(this.logWriter != null) exception.printStackTrace(logWriter); 
        fire(exception);
        return exception;        
    }

    void fire(
        int eventKind
    ){
        if(this.listeners != null) fire(
            new ConnectionEvent(this, eventKind)
        );
    }
    
    void fire(
        ResourceException exception
    ){
        if(this.listeners != null) fire(
            new ConnectionEvent(
                this, 
                ConnectionEvent.CONNECTION_ERROR_OCCURRED, 
                exception
            )
        );
    }

    void fire(
        ConnectionEvent event
    ){
        if(this.listeners != null) for(
            Iterator i = this.listeners.iterator();
            i.hasNext();
        ){
            ConnectionEventListener listener = (ConnectionEventListener) i.next();
            switch (event.getId()) {
                case ConnectionEvent.CONNECTION_CLOSED:
                    listener.connectionClosed(event);
                    break;
                case ConnectionEvent.CONNECTION_ERROR_OCCURRED:
                    listener.connectionErrorOccurred(event);
                    break;
                case ConnectionEvent.LOCAL_TRANSACTION_COMMITTED:
                    listener.localTransactionCommitted(event);
                    break;
                case ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK:
                    listener.localTransactionRolledback(event);                    
                    break;
                case ConnectionEvent.LOCAL_TRANSACTION_STARTED:
                    listener.localTransactionStarted(event);
                    break;
            }
        }        
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getClass().getName() + ": " + this.managedConnectionFactory;
    }

    /**
     * Local Database Transaction
     */
    class DatabaseTransaction implements LocalTransaction {

        public void begin() throws ResourceException {
            try {
//              managedConnection.setReadOnly(false);
                getConnection().setAutoCommit(false);
                fire(ConnectionEvent.LOCAL_TRANSACTION_STARTED);
            } catch (SQLException exception) {
                throw propagate(
                    "Transaction initialization failed",
                    exception
                );
            }            
        }
    
        public void commit() throws ResourceException {
            try {
                getConnection().commit();
                fire(ConnectionEvent.LOCAL_TRANSACTION_COMMITTED);
            } catch (SQLException exception) {
                throw propagate(
                    "Transaction commit failed",
                    exception
                );
            }
        }
    
        public void rollback() throws ResourceException {
            try {
                getConnection().rollback();
                fire(ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK);
            } catch (SQLException exception) {
                throw propagate(
                    "Transaction rollback failed",
                    exception
                );
            }
        }
        
    }
    
    /**
     * Class MetaData
     */
    class MetaData implements ManagedConnectionMetaData {

        private DatabaseMetaData getDelegate() throws SQLException{
            return getConnection().getMetaData();
        }

        public String getEISProductName() throws ResourceException {
            try {
                return getDelegate().getDatabaseProductName();
            } catch (SQLException exception) {
                throw propagate(exception);
            }
        }

        public String getEISProductVersion() throws ResourceException {
            try {
                return getDelegate().getDatabaseProductVersion();
            } catch (SQLException exception) {
                throw propagate(exception);
            }
        }

        public int getMaxConnections() throws ResourceException {
            try {
                return getDelegate().getMaxConnections();
            } catch (SQLException exception) {
                throw propagate(exception);
            }
        }

        public String getUserName() throws ResourceException {
            try {
                return getDelegate().getUserName();
            } catch (SQLException exception) {
                throw propagate(exception);
            }
        }
        
        private ResourceException propagate(
            Exception cause
        ){
            return ManagedDatabaseConnection.this.propagate(
                "Database meta data retrieval failed",
                cause
            );
        }

    }
    
}
