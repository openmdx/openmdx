/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Lightweight XAConnection
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
import java.util.ArrayDeque;
import java.util.Deque;

import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;

/**
 * Lightweight XAConnection
 */
public class LightweightXAConnection extends LightweightPooledConnection implements XAConnection {

	/**
	 * Constructor
	 */
	public LightweightXAConnection(Connection managedConnection) {
		super(managedConnection);
	}

	/**
	 * The associated connection handles
	 */
	private final Deque<Connection> handles = new ArrayDeque<Connection>();

	/**
	 * The lightweight resource supports one-phase commit only.
	 */
	private final XAResource lightweightResource = new LightweightResource();

	@Override
	protected void connectionClosed(Connection handle) {
		this.handles.remove(handle);
	}

	
	// ------------------------------------------------------------------------
	// Implements XAConnection
	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.XAConnection#getXAResource()
	 */
	@Override
	public XAResource getXAResource() throws SQLException {
		return this.lightweightResource;
	}

	// ------------------------------------------------------------------------
	// Implements PooledConnection
	// ------------------------------------------------------------------------

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
			throw new SQLException("XAConnection already closed");
		}
		Connection handle = new LightweightConnection(this);
		this.handles.push(handle);
		return handle;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.kernel.lightweight.sql.LightweightPooledConnection#passivate()
	 */
	@Override
	public
	void passivate() throws SQLException {
		try {
			while (isAssociated()) {
				handles.pop().close();
			}
		} catch (SQLException exception) {
			close();
			throw exception;
		}
	}

	private boolean isAssociated() {
		return !handles.isEmpty();
	}

		
	// ------------------------------------------------------------------------
	// Class LightweightResource
	// ------------------------------------------------------------------------
	
	/**
	 * The Lightweight Resource class accepts onePhase commits only because it
	 * delegates to the managed connection's LocalTransaction interface.
	 */
	class LightweightResource implements XAResource {

		@Override
		public void commit(Xid xid, boolean onePhase) throws XAException {
			if (onePhase)
				try {
					getManagedConnection().commit();
					fireCloseEvent();
				} catch (SQLException exception) {
					fireErrorEvent(exception);
					throw Throwables.initCause(new XAException(XAException.XAER_RMERR), exception,
							BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.TRANSACTION_FAILURE,
							"Local transaction commit failed", new BasicException.Parameter("xid", xid),
							new BasicException.Parameter("onePhase", onePhase));
				}
			else {
				fireErrorEvent(null);
				throw Throwables.log(Throwables.initCause(new XAException(XAException.XAER_PROTO), (Exception) null, 
						BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.TRANSACTION_FAILURE,
						"Two-phase commit not supported", new BasicException.Parameter("xid", xid),
						new BasicException.Parameter("onePhase", onePhase)));
			}
		}

		@Override
		public void end(Xid xid, int flag) throws XAException {
			// Nothing to do
		}

		@Override
		public void forget(Xid xid) throws XAException {
			// Nothing to do
		}

		@Override
		public int getTransactionTimeout() throws XAException {
			return 0;
		}

		@Override
		public boolean isSameRM(XAResource that) throws XAException {
			return this == that;
		}

		@Override
		public int prepare(Xid xid) throws XAException {
			throw Throwables.log(Throwables.initCause(new XAException(XAException.XAER_PROTO), null,
					BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.TRANSACTION_FAILURE,
					"Two-phase commit not supported", new BasicException.Parameter("xid", xid)));
		}

		@Override
		public Xid[] recover(int arg0) throws XAException {
			return new Xid[] {};
		}

		@Override
		public void rollback(Xid xid) throws XAException {
			try {
				getManagedConnection().rollback();
				fireCloseEvent();
			} catch (SQLException exception) {
				fireErrorEvent(exception);
				throw Throwables.log(Throwables.initCause(new XAException(XAException.XAER_RMERR), exception,
						BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.TRANSACTION_FAILURE,
						"Local transaction rollback failed", new BasicException.Parameter("xid", xid)));
			}
		}

		@Override
		public boolean setTransactionTimeout(int timeout) throws XAException {
			return false;
		}

		@Override
		public void start(Xid xid, int flags) throws XAException {
			// nothing to do
		}

	}

}
