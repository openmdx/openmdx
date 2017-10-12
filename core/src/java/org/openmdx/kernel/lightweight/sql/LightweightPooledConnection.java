/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Lightweight Pooled Connection
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2016, OMEX AG, Switzerland
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
package org.openmdx.kernel.lightweight.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashSet;
import java.util.Set;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import javax.sql.StatementEventListener;

/**
 * Lightweight Pooled Connection
 */
abstract class LightweightPooledConnection implements ValidatablePooledConnection {

	/**
	 * Constructor
	 */
	protected LightweightPooledConnection(
		Connection managedConnection
	) {
		this.managedConnection = managedConnection;
	}

	/**
	 * The connection to be re-used.
	 */
	private Connection managedConnection;

	/**
	 * The registered <code>ConnectionEvent</code> listeners
	 */
	private final Set<ConnectionEventListener> connectionEventListeners = new HashSet<ConnectionEventListener>();
	
	/**
	 * Validation timeout in seconds
	 */
	private static final int VALIDATION_TIMEOUT = 1;

	/**
	 * @return the managedConnection
	 */
	protected Connection getManagedConnection() {
		return managedConnection;
	}

	/**
	 * Tells whether the XA connection is closed
	 * 
	 * @return <code>true</code> if the connection is closed
	 */
	protected boolean isClosed() {
		return this.managedConnection == null;
	}

	protected abstract void connectionClosed(
		Connection handle
	);

	/**
	 * Lets the pool validate the connection
	 * 
	 * @return <code>true</code> if the managed connection can further be used
	 */
	@Override
	public boolean isValid() {
		try {
			return !isClosed() && getManagedConnection().isValid(VALIDATION_TIMEOUT);
		} catch (SQLException e) {
			return false;
		}
	}

	/**
	 * Closes the physical connection that this <code>PooledConnection</code>
	 * object represents. An application never calls this method directly; it is
	 * called by the connection pool module, or manager.
	 * <P>
	 * See the {@link PooledConnection interface description} for more
	 * information.
	 *
	 * @exception SQLException
	 *                if a database access error occurs
	 * @exception SQLFeatureNotSupportedException
	 *                if the JDBC driver does not support this method
	 */
	public void close() throws SQLException {
		if (!isClosed()) {
			try {
				this.managedConnection.close();				
			} finally {
				this.managedConnection = null;
				this.connectionEventListeners.clear();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getName() + ": " + (isClosed() ? "CLOSED" : "OPEN");
	}

	/**
	 * Creates and returns a <code>Connection</code> object that is a handle for
	 * the physical connection that this <code>PooledConnection</code> object
	 * represents. The connection pool manager calls this method when an
	 * application has called the method <code>DataSource.getConnection</code>
	 * and there are no <code>PooledConnection</code> objects available. See the
	 * {@link PooledConnection interface description} for more information.
	 *
	 * @return a <code>Connection</code> object that is a handle to this
	 *         <code>PooledConnection</code> object
	 * @exception SQLException
	 *                if a database access error occurs
	 * @exception SQLFeatureNotSupportedException
	 *                if the JDBC driver does not support this method
	 */
	@Override
	public Connection getConnection() throws SQLException {
		if (isClosed()) {
			throw new SQLException("Managed Connection already closed");
		}
		return new LightweightConnection(this);
	}
	
	/**
	 * Returns this managed connection to the pool
	 * 
	 * @throws SQLException in case of failure
	 */
	protected void fireCloseEvent(
	){
		final ConnectionEvent event = new ConnectionEvent(this);
		for(ConnectionEventListener listener : this.connectionEventListeners) {
			listener.connectionClosed(event);
		}
	}

	/**
	 * Returns this managed connection to the pool
	 * 
	 * @throws SQLException in case of failure
	 */
	protected void fireErrorEvent(
		SQLException cause
	){
		final ConnectionEvent event = new ConnectionEvent(this, cause);
		for(ConnectionEventListener listener : this.connectionEventListeners) {
			listener.connectionErrorOccurred(event);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.PooledConnection#addConnectionEventListener(javax.sql.
	 * ConnectionEventListener)
	 */
	@Override
	public void addConnectionEventListener(ConnectionEventListener listener) {
		this.connectionEventListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.PooledConnection#removeConnectionEventListener(javax.sql.
	 * ConnectionEventListener)
	 */
	@Override
	public void removeConnectionEventListener(ConnectionEventListener listener) {
		this.connectionEventListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.openmdx.kernel.lightweight.sql.ValidatablePooledConnection#activate()
	 */
	@Override
	public void activate() throws SQLException {
		getManagedConnection().clearWarnings();
	}
		
	
	// -----------------------------------------------------------------------
	// Since JRE 6
	// -----------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.PooledConnection#addStatementEventListener(javax.sql.
	 * StatementEventListener)
	 */
	public void addStatementEventListener(StatementEventListener listener) {
		throw new UnsupportedOperationException(
			new SQLFeatureNotSupportedException("StatementEventListener not yet supported")
		);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.PooledConnection#removeStatementEventListener(javax.sql.
	 * StatementEventListener)
	 */
	public void removeStatementEventListener(StatementEventListener listener) {
		throw new UnsupportedOperationException(
			new SQLFeatureNotSupportedException("StatementEventListener not yet supported")
		);
	}

}
