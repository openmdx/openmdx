/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Lightweight XADataSource
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2011-2016, OMEX AG, Switzerland
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
package org.openmdx.kernel.lightweight.sql;

import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.openmdx.uses.org.apache.commons.pool2.ObjectPool;
import org.openmdx.uses.org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * Lightweight XADataSource
 */
final public class LightweightXADataSource
    implements XADataSource 
{

	/**
     * Constructor
     * 
     * @param transactionManager the transaction manager to enlist the data source
     * @param url a database url of the form <code>jdbc:subprotocol:subname</code>
     */
	LightweightXADataSource(
		TransactionManager transactionManager,
        String url
    ){
    	this.transactionManager = transactionManager;
    	this.transactionalPool = createManagedConnectionPool(url, false);
    	this.nonTransactionalPool = createManagedConnectionPool(url, true);
    }

	private final TransactionManager transactionManager;
    private final ObjectPool<ValidatablePooledConnection> transactionalPool;
    private final ObjectPool<ValidatablePooledConnection> nonTransactionalPool;
    private final Map<Transaction,XAConnection> transactionalConnections = new WeakHashMap<Transaction, XAConnection>();
    
    private static ObjectPool<ValidatablePooledConnection> createManagedConnectionPool(
    	String url, 
    	boolean autoCommit
    ) {
    	final String connectionURL;
    	final Properties connectionProperties = new Properties();
    	final int q = url.indexOf('?');
    	if(q < 0) {
    		connectionURL = url;
    	} else {
    		connectionURL = url.substring(0, q);
    		String[] entries = url.substring(q+1).split("&");
    		for(String entry : entries) {
    			int e = entry.indexOf('=');
    			if(e > 0) {
    				connectionProperties.put(
						entry.substring(0, e), 
						entry.substring(e+1)
					);
    			}
    		}
    	}
    	final PooledConnectionFactory factory = new PooledConnectionFactory(connectionURL, connectionProperties, autoCommit);
    	final ObjectPool<ValidatablePooledConnection> pool = new GenericObjectPool<ValidatablePooledConnection>(factory);
    	factory.setConnectionEventListener(new PoolableConnectionEventListener(pool));
    	return pool;
    }
    
    /* (non-Javadoc)
     * @see javax.sql.XADataSource#getLogWriter(
     */
	@Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    /* (non-Javadoc)
     * @see javax.sql.XADataSource#getLoginTimeout()
     */
	@Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    /* (non-Javadoc)
     * @see javax.sql.XADataSource#getXAConnection()
     */
    @Override
    public  synchronized XAConnection getXAConnection() throws SQLException {
    	return getXAConnection(getTransaction());
    }
    
    private synchronized XAConnection getXAConnection(
		Transaction transaction
    ) throws SQLException {
    	XAConnection xaConnection = this.transactionalConnections.get(transaction);
    	if(xaConnection == null) {
    		xaConnection = (XAConnection) getPooledConnection(this.transactionalPool);
    		enlistResource(transaction, xaConnection);
    	}
    	return xaConnection;
    }

    PooledConnection getPooledConnection(
    ) throws SQLException{
    	final Transaction transaction = getTransaction();	
    	return transaction == null ?
    		getPooledConnection(this.nonTransactionalPool) :
			getXAConnection(transaction);
    }
    
	private ValidatablePooledConnection getPooledConnection (
		final ObjectPool<ValidatablePooledConnection> objectPool
	) throws SQLException {
		try {
			return objectPool.borrowObject();
		} catch (Exception e) {
			throw new SQLException("XA connection acquisition failure", e);
		}
	}
	
	private void enlistResource(
		final Transaction transaction, 
		final XAConnection xaConnection
	) throws SQLException {
		try {
			transaction.enlistResource(xaConnection.getXAResource());
		} catch (Exception e) {
			throw new SQLException("XA connection enlisting failure", e);
		}
		this.transactionalConnections.put(transaction, xaConnection);
	}

	
	private Transaction getTransaction() throws SQLException {
		try {
			return this.transactionManager.getTransaction();
		} catch (SystemException e) {
			throw new SQLException("Transaction acquisition failure", e);
		}
	}

    /* (non-Javadoc)
     * @see javax.sql.XADataSource#getXAConnection(java.lang.String, java.lang.String)
     */
    @Override
    public XAConnection getXAConnection(
        String user, 
        String password
    ) throws SQLException {
    	throw new SQLFeatureNotSupportedException("Re-authentication not yet supported");
    }

    /* (non-Javadoc)
     * @see javax.sql.XADataSource#setLogWriter(java.io.PrintWriter)
     */
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        DriverManager.setLogWriter(out);        
    }

    /* (non-Javadoc)
     * @see javax.sql.XADataSource#setLoginTimeout(int)
     */
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        DriverManager.setLoginTimeout(seconds);
    }

    
    //-----------------------------------------------------------------------
    // Since JRE 7
    //-----------------------------------------------------------------------
    
	public java.util.logging.Logger getParentLogger(
	) throws java.sql.SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException("JRE 7 features not yet supported");
	}

}
