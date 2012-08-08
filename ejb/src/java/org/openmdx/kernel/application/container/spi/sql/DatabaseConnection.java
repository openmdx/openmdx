/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DatabaseConnection.java,v 1.1 2009/01/12 12:49:24 wfro Exp $
 * Description: DatabaseConnection
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/12 12:49:24 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
package org.openmdx.kernel.application.container.spi.sql;


import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.Savepoint;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import java.util.Map;

import org.openmdx.kernel.callback.CloseCallback;
/**
 * DatabaseConnection
 */
@SuppressWarnings("unchecked")
public class DatabaseConnection implements Connection {

    /**
     * The underlaying SQL connection
     */
    private Connection connection;

    /**
     * The underlaying JCA managed connection
     */
    private CloseCallback managedConnection;

    /**
     * Auto-commits on <code>close()</code> when <code>true</code>
     */
    private boolean autoCommit = false;
    
    /**
     * Set the delegate
     * 
     * @param managedConnnection the JCA managed connection 
     * @param connnection the SQL connection
     */
    protected void setDelegate(
        CloseCallback managedConnection,
        Connection connection
    ){
        this.managedConnection = managedConnection;
        this.connection = connection;
    }


    //------------------------------------------------------------------------
    // Implements Connection
    //------------------------------------------------------------------------

    /**
     * Clears all warnings reported for this Connection object.
     * 
     * @throws java.sql.SQLException if a database access error occurs
     */
    public void clearWarnings() throws SQLException {
        this.connection.clearWarnings();
    }

    /**
     * Releases this Connection object's database and JDBC resources 
     * immediately instead of waiting for them to be automatically released.
     * 
     * @throws java.sql.SQLException if a database access error occurs
     */
    public void close() throws SQLException {
        if(this.autoCommit) {
            this.connection.commit();
        }
        this.managedConnection.postClose(this);
    }

    /**
     * Makes all changes made since the previous commit/rollback permanent 
     * and releases any database locks currently held by this Connection 
     * object.
     * 
     * @throws java.sql.SQLException
     *  if a database access error occurs or this Connection object is in 
     *  auto-commit mode
     */
    public void commit() throws SQLException {
        throw new SQLException(
            getClass().getName() +
            " does not support explicit transaction control"
        );
    }

    /**
     * Creates a Statement object for sending SQL statements to the database. 
     * SQL statements without parameters are normally executed using Statement 
     * objects. If the same SQL statement is executed many times, it may be 
     * more efficient to use a PreparedStatement object. 
     * <p>
     * Result sets created using the returned Statement object will by default 
     * be type TYPE_FORWARD_ONLY and have a concurrency level of 
     * CONCUR_READ_ONLY.
     * 
     * @return  new default Statement object 
     * 
     * @throws java.sql.SQLException if a database access error occurs
     */
    public Statement createStatement() throws SQLException {
        return this.connection.createStatement();
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
     *  pre-compiled SQL statement 
     * 
     * @throws java.sql.SQLException if a database access error occurs
     */
    public Statement createStatement(
        int resultSetType,
        int resultSetConcurrency
    ) throws SQLException {
        return this.connection.createStatement(
            resultSetType,
            resultSetConcurrency
        );
    }

    /**
     * Retrieves the current auto-commit mode for this Connection object.
     * 
     * @return the current state of this Connection object's auto-commit mode 
     * 
     * @throws java.sql.SQLException if a database access error occurs
     */
    public boolean getAutoCommit() throws SQLException {
        return this.connection.getAutoCommit();
    }

    /**
     * Retrieves this Connection object's current catalog name.
     * 
     * @return the current catalog name or null if there is none  
     * 
     * @throws java.sql.SQLException if a database access error occurs
     */
    public String getCatalog() throws SQLException {
        return this.connection.getCatalog();
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
     * @throws java.sql.SQLException if a database access error occurs
     */
    public DatabaseMetaData getMetaData() throws SQLException {
        return this.connection.getMetaData();
    }

    /**
     * Retrieves this Connection object's current transaction isolation level
     * 
     * @return the current transaction isolation level, which will be one of 
     * the following constants: Connection.TRANSACTION_READ_UNCOMMITTED, 
     * Connection.TRANSACTION_READ_COMMITTED, 
     * Connection.TRANSACTION_REPEATABLE_READ, 
     * Connection.TRANSACTION_SERIALIZABLE, or Connection.TRANSACTION_NONE. 
     * 
     * @throws java.sql.SQLException if a database access error occurs
     */
    public int getTransactionIsolation() throws SQLException {
        return this.connection.getTransactionIsolation();
    }

    /**
     * @return
     * @throws java.sql.SQLException
     */
    public Map getTypeMap() throws SQLException {
        return this.connection.getTypeMap();
    }

    /**
     * @return
     * @throws java.sql.SQLException
     */
    public SQLWarning getWarnings() throws SQLException {
        return this.connection.getWarnings();
    }

    /**
     * Retrieves whether this Connection object has been closed. A 
     * connection is closed if the method close has been called on it or if 
     * certain fatal errors have occurred. This method is guaranteed to 
     * return true only when it is called after the method Connection.close 
     * has been called.
     * <p>
     * This method generally cannot be called to determine whether a 
     * connection to a database is valid or invalid. A typical client can 
     * determine that a connection is invalid by catching any exceptions that 
     * might be thrown when an operation is attempted. 

     * @return true if this Connection object is closed; false if it is still 
     * open 
     * 
     * @throws java.sql.SQLException if a database access error occurs
     */
    public boolean isClosed() throws SQLException {
        return this.connection == null;
    }

    /**
     * Retrieves whether this Connection object is in read-only mode. 
     * 
     * @return true if this Connection object is read-only; false otherwise 
     * 
     * @throws java.sql.SQLException if a database access error occurs
     */
    public boolean isReadOnly() throws SQLException {
        return this.connection.isReadOnly();
    }

    /**
     * Converts the given SQL statement into the system's native SQL grammar. 
     * A driver may convert the JDBC SQL grammar into its system's native SQL 
     * grammar prior to sending it. This method returns the native form of the 
     * statement that the driver would have sent. 
     * 
     * @param sql an SQL statement that may contain one or more '?' parameter 
     * placeholders 
     * 
     * @return the native form of this statement 
     * @throws java.sql.SQLException if a database access error occurs
     */
    public String nativeSQL(
        String sql
    ) throws SQLException {
        return this.connection.nativeSQL(sql);
    }

    /**
     * Creates a CallableStatement object for calling database stored 
     * procedures. 
     * The CallableStatement object provides methods for setting up its IN and 
     * OUT parameters, and methods for executing the call to a stored 
     * procedure.
     * 
     * @param sql an SQL statement that may contain one or more '?' parameter 
     * placeholders. Typically this statement is a JDBC function call escape s
     * tring.
     *  
     * @return a new default CallableStatement object containing the pre-compiled 
     * SQL statement
     *  
     * @throws java.sql.SQLException if a database access error occurs
     */
    public CallableStatement prepareCall(String sql) throws SQLException {
        return this.connection.prepareCall(sql);
    }

    /**
     * Creates a CallableStatement object that will generate ResultSet objects with 
     * the given type and concurrency. This method is the same as the prepareCall 
     * method above, but it allows the default result set type and concurrency to be 
     * overridden.
     *  
     * @param sql
     * @param resultSetType
     * @param resultSetConcurrency
     * 
     * @return a new CallableStatement object containing the pre-compiled SQL 
     * statement that will produce ResultSet objects with the given type and 
     * concurrency
     * 
     * @throws java.sql.SQLException if a database access error occurs
     */
    public CallableStatement prepareCall(
        String sql,
        int resultSetType,
        int resultSetConcurrency
    ) throws SQLException {
        return this.connection.prepareCall(
            sql,
            resultSetType,
            resultSetConcurrency
        );
    }

    /**
     * Creates a PreparedStatement object for sending parameterized SQL 
     * statements to the database.
     * <p>
     * A SQL statement with or without IN parameters can be pre-compiled 
     * and stored in a PreparedStatement object. This object can then be 
     * used to efficiently execute this statement multiple times. 
     * 
     * @param sql an SQL statement that may contain one or more '?' IN 
     * parameter placeholders
     * 
     * @return  new default PreparedStatement object containing the 
     * pre-compiled SQL statement
     *  
     * @throws java.sql.SQLException if a database access error occurs
     */
    public PreparedStatement prepareStatement(
        String sql
    ) throws SQLException {
        return this.connection.prepareStatement(sql);
    }

    /**
     * Creates a PreparedStatement object that will generate ResultSet objects 
     * with the given type and concurrency. This method is the same as the 
     * prepareStatement method above, but it allows the default result set 
     * type and concurrency to be overridden.
     * 
     * @param sql
     * @param resultSetType
     * @param resultSetConcurrency
     * 
     * @return a new PreparedStatement object containing the pre-compiled SQL 
     * statement that will produce ResultSet objects with the given type and 
     * concurrency
     *  
     * @throws java.sql.SQLException if a database access error occurs
     */
    public PreparedStatement prepareStatement(
        String sql,
        int resultSetType,
        int resultSetConcurrency
    ) throws SQLException {
        return this.connection.prepareStatement(
            sql,
            resultSetType,
            resultSetConcurrency
        );
    }

    /**
     * Undoes all changes made in the current transaction and releases any 
     * database locks currently held by this Connection object. This method 
     * should be used only when auto-commit mode has been disabled. 
     * 
     * @throws java.sql.SQLException if a database access error occurs or this 
     * Connection object is in auto-commit mode
     */
    public void rollback() throws SQLException {
        throw new SQLException(
            getClass().getName() +
            " does not support explicit transaction control"
        );
    }

    /**
     * Sets this connection's auto-commit mode to the given state. If a 
     * connection is in auto-commit mode, then all its SQL statements will be 
     * executed and committed as individual transactions. Otherwise, its SQL 
     * statements are grouped into transactions that are terminated by a call 
     * to either the method commit or the method rollback. By default, new 
     * connections are in auto-commit mode. 
     * <p>
     * The commit occurs when the statement completes or the next execute 
     * occurs, whichever comes first. In the case of statements returning a 
     * ResultSet object, the statement completes when the last row of the 
     * ResultSet object has been retrieved or the ResultSet object has been 
     * closed. In advanced cases, a single statement may return multiple 
     * results as well as output parameter values. In these cases, the commit 
     * occurs when all results and output parameter values have been retrieved. 
     * 
     * @param autoCommit
     * 
     * @throws java.sql.SQLException if a database access error occurs
     */
    public void setAutoCommit(
        boolean autoCommit
    ) throws SQLException {
        this.autoCommit = autoCommit;
    }

    /**
     * Sets the given catalog name in order to select a subspace of this 
     * Connection object's database in which to work. 
     * <p>
     * If the driver does not support catalogs, it will silently ignore 
     * this request.
     * 
     * @param catalog
     * 
     * @throws java.sql.SQLException if a database access error occurs
     */
    public void setCatalog(String catalog) throws SQLException {
        this.connection.setCatalog(catalog);
    }

    /**
     * Puts this connection in read-only mode as a hint to the driver to 
     * enable database optimizations.
     *  
     * @param readOnly
     * 
     * @throws java.sql.SQLException if a database access error occurs
     */
    public void setReadOnly(boolean readOnly) throws SQLException {
        throw new SQLException(
            getClass().getName() +
            " does not support explicit connection control"
        );
    }

    /**
     * Attempts to change the transaction isolation level for this 
     * Connection object to the one given. The constants defined in the 
     * interface Connection are the possible transaction isolation levels. 
     * 
     * @param level
     * 
     * @throws java.sql.SQLException if a database access error occurs
     */
    public void setTransactionIsolation(int level) throws SQLException {
        throw new SQLException(
            getClass().getName() +
            " does not support explicit connection control"
        );
    }

    /**
     * Installs the given TypeMap object as the type map for this Connection 
     * object. The type map will be used for the custom mapping of SQL s
     * tructured types and distinct types.
     * 
     * @param map
     * 
     * @throws java.sql.SQLException if a database access error occurs
     */
    public void setTypeMap(Map map) throws SQLException {
        throw new SQLException(
            getClass().getName() +
            " does not support explicit connection control"
        );
    }


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
        public String toString() {
                return getClass().getName() + " (" + this.connection + ')';
        }



    //------------------------------------------------------------------------
    // Since JRE 1.4
    //------------------------------------------------------------------------

        /**
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @param resultSetHoldability
	 * @return
	 * @throws java.sql.SQLException
	 */
        public Statement createStatement(
        int resultSetType,
                int resultSetConcurrency,
                int resultSetHoldability
    ) throws SQLException {
                return this.connection.createStatement(
                    resultSetType,
                        resultSetConcurrency,
                        resultSetHoldability
            );
        }

        /**
	 * @return
	 * @throws java.sql.SQLException
	 */
        public int getHoldability() throws SQLException {
                return this.connection.getHoldability();
        }

        /**
	 * @param sql
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @param resultSetHoldability
	 * @return
	 * @throws java.sql.SQLException
	 */
        public CallableStatement prepareCall(
           String sql,
           int resultSetType,
           int resultSetConcurrency,
           int resultSetHoldability
        ) throws SQLException {
                return this.connection.prepareCall(
                    sql,
                    resultSetType,
                        resultSetConcurrency,
                        resultSetHoldability
            );
        }

        /**
	 * @param sql
	 * @param autoGeneratedKeys
	 * @return
	 * @throws java.sql.SQLException
	 */
        public PreparedStatement prepareStatement(
                String sql,
                int autoGeneratedKeys
        ) throws SQLException {
                return this.connection.prepareStatement(
                        sql,
                        autoGeneratedKeys
                );
        }

        /**
	 * @param sql
	 * @param resultSetType
	 * @param resultSetConcurrency
	 * @param resultSetHoldability
	 * @return
	 * @throws java.sql.SQLException
	 */
        public PreparedStatement prepareStatement(
            String sql,
            int resultSetType,
                int resultSetConcurrency,
                int resultSetHoldability
        ) throws SQLException {
                return this.connection.prepareStatement(
                        sql,
                        resultSetType,
                        resultSetConcurrency,
                        resultSetHoldability
                );
        }

        /**
	 * @param sql
	 * @param columnIndexes
	 * @return
	 * @throws java.sql.SQLException
	 */
        public PreparedStatement prepareStatement(
                String sql,
                int[] columnIndexes
    ) throws SQLException {
                return this.connection.prepareStatement(
                    sql,
                    columnIndexes
                );
        }

        /**
	 * @param sql
	 * @param columnNames
	 * @return
	 * @throws java.sql.SQLException
	 */
        public PreparedStatement prepareStatement(
                String sql,
                String[] columnNames
        ) throws SQLException {
                return this.connection.prepareStatement(
                        sql,
                        columnNames
                );
        }

        /**
	 * @param savepoint
	 * @throws java.sql.SQLException
	 */
        public void releaseSavepoint(
                Savepoint savepoint
        ) throws SQLException {
                this.connection.releaseSavepoint(savepoint);
        }

        /**
	 * @param savepoint
	 * @throws java.sql.SQLException
	 */
        public void rollback(
                Savepoint savepoint
        ) throws SQLException {
                this.connection.rollback(savepoint);
        }

        /**
	 * @param holdability
	 * @throws java.sql.SQLException
	 */
        public void setHoldability(
                int holdability
        ) throws SQLException {
                this.connection.setHoldability(holdability);
        }

        /**
	 * @return
	 * @throws java.sql.SQLException
	 */
        public Savepoint setSavepoint() throws SQLException {
                return this.connection.setSavepoint();
        }

        /**
	 * @param name
	 * @return
	 * @throws java.sql.SQLException
	 */
        public Savepoint setSavepoint(String name) throws SQLException {
                return this.connection.setSavepoint(name);
        }
}
