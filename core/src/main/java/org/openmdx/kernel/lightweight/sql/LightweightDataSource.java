/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Lightweight DataSource
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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

/**
 * Lightweight Data Source
 * 
 * @deprecated in favour of Atomikos' JDBC support
 */
@Deprecated
public class LightweightDataSource implements DataSource {
	
	private TransactionManager transactionManager;
    private String driverUrl;
	private LightweightXADataSource xaDataSource;
	private Integer loginTimeout;

	private synchronized LightweightXADataSource getXaDataSource() throws SQLException {
		if(this.xaDataSource == null) {
			this.xaDataSource = new LightweightXADataSource(this.transactionManager, this.driverUrl);
			if(this.loginTimeout != null) {
				this.xaDataSource.setLoginTimeout(this.loginTimeout.intValue());
				this.loginTimeout = null;
			}
		}
		return this.xaDataSource;
	}
	
	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public String getDriverUrl() {
		return driverUrl;
	}

	public void setDriverUrl(String url) {
		this.driverUrl = url;
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return getXaDataSource().getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		getXaDataSource().setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		if(this.xaDataSource == null) {
			this.loginTimeout = Integer.valueOf(seconds);
		} else {
			this.xaDataSource.setLoginTimeout(seconds);
		}
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		if(this.xaDataSource == null) {
			return this.loginTimeout == null ? 0 : this.loginTimeout.intValue();
		} else {
			return this.xaDataSource.getLoginTimeout();
		}
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if(iface.isAssignableFrom(DataSource.class)){
			return iface.cast(this);	
		}
		if(iface.isAssignableFrom(XADataSource.class)){
			return iface.cast(this.getXaDataSource());	
		}
		throw new SQLException(getClass().getName() + " is not a wrapper for " + iface.getName());
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return 
			iface.isAssignableFrom(DataSource.class) ||
			iface.isAssignableFrom(XADataSource.class);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return this.getXaDataSource().getPooledConnection().getConnection();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return this.getXaDataSource().getXAConnection(username, password).getConnection();
	}
	
	/* (non-Javadoc)
     * @see javax.sql.CommonDataSource#getParentLogger()
     */
    @Override
    public Logger getParentLogger(
    ) throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException("JRE 7 features not yet supported");
    }

}
