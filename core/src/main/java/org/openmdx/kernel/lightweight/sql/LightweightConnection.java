/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Lightweight Connection
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2016, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other
 * organization as listed in the NOTICE file.
 */
package org.openmdx.kernel.lightweight.sql;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.openmdx.kernel.log.SysLog;

/**
 * Lightweight Connection
 */
class LightweightConnection implements Connection {

	/**
	 * Constructor
	 * 
	 * @param pooledConnection
	 *            the pooled connection
	 * @param connectionEventListener
	 *            the connection event listener
	 */
	LightweightConnection(
		LightweightPooledConnection pooledConnection
	) {
		this.pooledConnection = pooledConnection;
	}
	
	/**
	 * The pooled connection
	 */
	private LightweightPooledConnection pooledConnection;
	
	/**
	 * Provide the physical connection
	 */
	private Connection getDelegate() throws SQLException{
		if(isClosed()) {
			throw new SQLException("Connection already closed");
		}
		return this.pooledConnection.getManagedConnection();
	}


	// ------------------------------------------------------------------------
	// Implements Connection
	// ------------------------------------------------------------------------

	/**
	 * Clears all warnings reported for this Connection object.
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs
	 */
	@Override
	public void clearWarnings() throws SQLException {
		getDelegate().clearWarnings();
	}

	/**
	 * Releases this Connection object's database and JDBC resources immediately
	 * instead of waiting for them to be automatically released.
	 * 
	 * @throws SQLException 
	 */
	@Override
	public void close(
	){
		if(!isClosed()) {
			try {
				pooledConnection.connectionClosed(this);
			} finally {
				this.pooledConnection = null;
			}
		}			
	}

	/**
	 * Makes all changes made since the previous commit/rollback permanent and
	 * releases any database locks currently held by this Connection object.
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs or this Connection object
	 *             is in auto-commit mode
	 */
	@Override
	public void commit() throws SQLException {
		throw new SQLException(getClass().getName() + " does not support explicit transaction control");
	}

	/**
	 * Creates a Statement object for sending SQL statements to the database.
	 * SQL statements without parameters are normally executed using Statement
	 * objects. If the same SQL statement is executed many times, it may be more
	 * efficient to use a PreparedStatement object.
	 * <p>
	 * Result sets created using the returned Statement object will by default
	 * be type TYPE_FORWARD_ONLY and have a concurrency level of
	 * CONCUR_READ_ONLY.
	 * 
	 * @return new default Statement object
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs
	 */
	@Override
	public Statement createStatement() throws SQLException {
		return getDelegate().createStatement();
	}

	/**
	 * Creates a PreparedStatement object for sending parameterized SQL
	 * statements to the database.
	 * <p>
	 * A SQL statement with or without IN parameters can be pre-compiled and
	 * stored in a PreparedStatement object. This object can then be used to
	 * efficiently execute this statement multiple times.
	 * 
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * 
	 * @return a new default PreparedStatement object containing the
	 *         pre-compiled SQL statement
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs
	 */
	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return getDelegate().createStatement(resultSetType, resultSetConcurrency);
	}

	/**
	 * Retrieves the current auto-commit mode for this Connection object.
	 * 
	 * @return the current state of this Connection object's auto-commit mode
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs
	 */
	@Override
	public boolean getAutoCommit() throws SQLException {
		return getDelegate().getAutoCommit();
	}

	/**
	 * Retrieves this Connection object's current catalog name.
	 * 
	 * @return the current catalog name or null if there is none
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs
	 */
	@Override
	public String getCatalog() throws SQLException {
		return getDelegate().getCatalog();
	}

	/**
	 * Retrieves a DatabaseMetaData object that contains metadata about the
	 * database to which this Connection object represents a connection. The
	 * metadata includes information about the database's tables, its supported
	 * SQL grammar, its stored procedures, the capabilities of this connection,
	 * and so on.
	 * 
	 * @return a DatabaseMetaData object for this Connection object
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs
	 */
	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return getDelegate().getMetaData();
	}

	/**
	 * Retrieves this Connection object's current transaction isolation level
	 * 
	 * @return the current transaction isolation level, which will be one of the
	 *         following constants: Connection.TRANSACTION_READ_UNCOMMITTED,
	 *         Connection.TRANSACTION_READ_COMMITTED,
	 *         Connection.TRANSACTION_REPEATABLE_READ,
	 *         Connection.TRANSACTION_SERIALIZABLE, or
	 *         Connection.TRANSACTION_NONE.
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs
	 */
	@Override
	public int getTransactionIsolation() throws SQLException {
		return getDelegate().getTransactionIsolation();
	}

	/* (non-Javadoc)
	 * @see java.sql.Connection#getTypeMap()
	 */
	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return getDelegate().getTypeMap();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public SQLWarning getWarnings() throws SQLException {
		return getDelegate().getWarnings();
	}

	/**
	 * Retrieves whether this Connection object has been closed. A connection is
	 * closed if the method close has been called on it or if certain fatal
	 * errors have occurred. This method is guaranteed to return true only when
	 * it is called after the method Connection.close has been called.
	 * <p>
	 * This method generally cannot be called to determine whether a connection
	 * to a database is valid or invalid. A typical client can determine that a
	 * connection is invalid by catching any exceptions that might be thrown
	 * when an operation is attempted.
	 * 
	 * @return true if this Connection object is closed; false if it is still
	 *         open
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs
	 */
	@Override
	public boolean isClosed() {
		return this.pooledConnection == null;
	}

	/**
	 * Retrieves whether this Connection object is in read-only mode.
	 * 
	 * @return true if this Connection object is read-only; false otherwise
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs
	 */
	@Override
	public boolean isReadOnly() throws SQLException {
		return getDelegate().isReadOnly();
	}

	/**
	 * Converts the given SQL statement into the system's native SQL grammar. A
	 * driver may convert the JDBC SQL grammar into its system's native SQL
	 * grammar prior to sending it. This method returns the native form of the
	 * statement that the driver would have sent.
	 * 
	 * @param sql
	 *            an SQL statement that may contain one or more '?' parameter
	 *            placeholders
	 * 
	 * @return the native form of this statement
	 * @throws java.sql.SQLException
	 *             if a database access error occurs
	 */
	@Override
	public String nativeSQL(String sql) throws SQLException {
		return getDelegate().nativeSQL(sql);
	}

	/**
	 * Creates a CallableStatement object for calling database stored
	 * procedures. The CallableStatement object provides methods for setting up
	 * its IN and OUT parameters, and methods for executing the call to a stored
	 * procedure.
	 * 
	 * @param sql
	 *            an SQL statement that may contain one or more '?' parameter
	 *            placeholders. Typically this statement is a JDBC function call
	 *            escape string.
	 * 
	 * @return a new default CallableStatement object containing the
	 *         pre-compiled SQL statement
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs
	 */
	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		return getDelegate().prepareCall(sql);
	}

	/**
	 * Creates a CallableStatement object that will generate ResultSet objects
	 * with the given type and concurrency. This method is the same as the
	 * prepareCall method above, but it allows the default result set type and
	 * concurrency to be overridden.
	 * 
	 * @param sql
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * 
	 * @return a new CallableStatement object containing the pre-compiled SQL
	 *         statement that will produce ResultSet objects with the given type
	 *         and concurrency
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs
	 */
	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return getDelegate().prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	/**
	 * Creates a PreparedStatement object for sending parameterized SQL
	 * statements to the database.
	 * <p>
	 * A SQL statement with or without IN parameters can be pre-compiled and
	 * stored in a PreparedStatement object. This object can then be used to
	 * efficiently execute this statement multiple times.
	 * 
	 * @param sql
	 *            an SQL statement that may contain one or more '?' IN parameter
	 *            placeholders
	 * 
	 * @return new default PreparedStatement object containing the pre-compiled
	 *         SQL statement
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs
	 */
	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return getDelegate().prepareStatement(sql);
	}

	/**
	 * Creates a PreparedStatement object that will generate ResultSet objects
	 * with the given type and concurrency. This method is the same as the
	 * prepareStatement method above, but it allows the default result set type
	 * and concurrency to be overridden.
	 * 
	 * @param sql
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * 
	 * @return a new PreparedStatement object containing the pre-compiled SQL
	 *         statement that will produce ResultSet objects with the given type
	 *         and concurrency
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs
	 */
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return getDelegate().prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	/**
	 * Undoes all changes made in the current transaction and releases any
	 * database locks currently held by this Connection object. This method
	 * should be used only when auto-commit mode has been disabled.
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs or this Connection object
	 *             is in auto-commit mode
	 */
	@Override
	public void rollback() throws SQLException {
		throw new SQLException(getClass().getName() + " does not support explicit transaction control");
	}

	/**
	 * Sets this connection's auto-commit mode to the given state. If a
	 * connection is in auto-commit mode, then all its SQL statements will be
	 * executed and committed as individual transactions. Otherwise, its SQL
	 * statements are grouped into transactions that are terminated by a call to
	 * either the method commit or the method rollback. By default, new
	 * connections are in auto-commit mode.
	 * <p>
	 * The commit occurs when the statement completes or the next execute
	 * occurs, whichever comes first. In the case of statements returning a
	 * ResultSet object, the statement completes when the last row of the
	 * ResultSet object has been retrieved or the ResultSet object has been
	 * closed. In advanced cases, a single statement may return multiple results
	 * as well as output parameter values. In these cases, the commit occurs
	 * when all results and output parameter values have been retrieved.
	 * 
	 * @param autoCommit
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs
	 */
	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		if(getAutoCommit() != autoCommit) {
			throw new SQLException("Auto-commit mode can't be changed from " + !autoCommit + " to " + autoCommit);
		}
	}

	/**
	 * Sets the given catalog name in order to select a subspace of this
	 * Connection object's database in which to work.
	 * <p>
	 * If the driver does not support catalogs, it will silently ignore this
	 * request.
	 * 
	 * @param catalog
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs
	 */
	@Override
	public void setCatalog(String catalog) throws SQLException {
		getDelegate().setCatalog(catalog);
	}

	/**
	 * Puts this connection in read-only mode as a hint to the driver to enable
	 * database optimizations.
	 * 
	 * @param readOnly
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs
	 */
	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		throw newSQLException("Explicit connection control not supported");
	}

	/**
	 * Attempts to change the transaction isolation level for this Connection
	 * object to the one given. The constants defined in the interface
	 * Connection are the possible transaction isolation levels.
	 * 
	 * @param level
	 * 
	 * @throws java.sql.SQLException
	 *             if a database access error occurs
	 */
	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		throw newSQLException("Explicit connection control not supported");
	}

	private SQLException newSQLException(String message) {
		return new SQLException(getClass().getName() + ": " + message);
	}

	
	// ------------------------------------------------------------------------
	// Extends Object
	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getName() + " (closed=" + isClosed() + ')';
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		if(!isClosed()) {
			SysLog.info("Connection leak", "Connection closed by finalizer");
			close();
		}
	}
		
	
	// ------------------------------------------------------------------------
	// Since JRE 1.4
	// ------------------------------------------------------------------------

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return getDelegate().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public int getHoldability() throws SQLException {
		return getDelegate().getHoldability();
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		return getDelegate().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return getDelegate().prepareStatement(sql, autoGeneratedKeys);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		return getDelegate().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		return getDelegate().prepareStatement(sql, columnIndexes);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		return getDelegate().prepareStatement(sql, columnNames);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		getDelegate().releaseSavepoint(savepoint);
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		getDelegate().rollback(savepoint);
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		getDelegate().setHoldability(holdability);
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return getDelegate().setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return getDelegate().setSavepoint(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setTypeMap(java.util.Map)
	 */
	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		getDelegate().setTypeMap(map);
	}

	
	// -----------------------------------------------------------------------
	// Since JRE 6
	// -----------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createArrayOf(java.lang.String,
	 * java.lang.Object[])
	 */
	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return getDelegate().createArrayOf(typeName, elements);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createBlob()
	 */
	@Override
	public Blob createBlob() throws SQLException {
		return getDelegate().createBlob();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createClob()
	 */
	@Override
	public Clob createClob() throws SQLException {
		return getDelegate().createClob();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createNClob()
	 */
	@Override
	public NClob createNClob() throws SQLException {
		return getDelegate().createNClob();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createSQLXML()
	 */
	@Override
	public SQLXML createSQLXML() throws SQLException {
		return getDelegate().createSQLXML();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#createStruct(java.lang.String,
	 * java.lang.Object[])
	 */
	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return getDelegate().createStruct(typeName, attributes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getClientInfo()
	 */
	@Override
	public Properties getClientInfo() throws SQLException {
		return getDelegate().getClientInfo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#getClientInfo(java.lang.String)
	 */
	@Override
	public String getClientInfo(String name) throws SQLException {
		return getDelegate().getClientInfo(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#isValid(int)
	 */
	@Override
	public boolean isValid(int timeout) throws SQLException {
		return getDelegate().isValid(timeout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setClientInfo(java.util.Properties)
	 */
	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		try {
			getDelegate().setClientInfo(properties);
		} catch (SQLException e) {
			throw toSQLClientInfoException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Connection#setClientInfo(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		try {
			getDelegate().setClientInfo(name, value);
		} catch (SQLException e) {
			throw toSQLClientInfoException(e);
		}
	}

	private SQLClientInfoException toSQLClientInfoException(SQLException cause) {
		final SQLClientInfoException exception = new SQLClientInfoException();
		exception.initCause(cause);
		return exception;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return getDelegate().isWrapperFor(iface);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return getDelegate().unwrap(iface);
	}

	
    //-----------------------------------------------------------------------
    // Since JRE 7
    //-----------------------------------------------------------------------

	public int getNetworkTimeout(
	) throws SQLException {
		throw newSQLFeatureNotSupportedException("JRE 7 Connection features not yet supported");
	}
	       
	public void setNetworkTimeout(
		Executor executor,
		int milliseconds
	) throws SQLException {
		throw newSQLFeatureNotSupportedException("JRE 7 Connection features not yet supported");
	}
		
	public void abort(
		Executor executor
	) throws SQLException {
		throw newSQLFeatureNotSupportedException("JRE 7 Connection features not yet supported");
	}
	
	public String getSchema(
	) throws SQLException {
		throw newSQLFeatureNotSupportedException("JRE 7 Connection features not yet supported");
	}
			     
	public void setSchema(
		String schema
	) throws SQLException {
		throw newSQLFeatureNotSupportedException("JRE 7 Connection features not yet supported");
	}

	private SQLException newSQLFeatureNotSupportedException(String message) {
		return new SQLException(getClass().getName() + ": " + message);
	}

}
