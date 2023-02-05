/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Late Binding Data Source
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.application.dataprovider.kernel;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;

import java.util.logging.Logger;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Late Binding {@code DataSource}
 */
public class LateBindingDataSource implements DataSource {

    /**
     * Constructor
     *
     * @param jndiName the {@code DataSource}'s JNDI name
     */
    public LateBindingDataSource(
        String jndiName
    ){
        this.jndiName = jndiName;
    }

    /**
     * The {@code DataSource} is retrieved lazily
     */
    private DataSource delegate;
    
    /**
     * The JNDI name refers usually to an entry in the {@code java:comp/env/jdbc} context 
     */
    private final String jndiName;
    
    /**
     * Look up the {@code DataSource}
     * 
     * @return the DataSource corresponding to the JNDI name
     * 
     * @throws ServiceException
     */
    private synchronized DataSource getDelegate(
    ) throws SQLException {
        if (this.delegate == null){ 
        	try {
	            SysLog.detail(
	                "Acquire Connection Pool",
	                this.jndiName
	            );
	            this.delegate = (DataSource)new InitialContext().lookup(this.jndiName);
	        } catch(Exception exception) {
	            throw Throwables.initCause(
	                new SQLException("Could not lookup data source"),
	                exception,
	                BasicException.Code.DEFAULT_DOMAIN,
	                BasicException.Code.MEDIA_ACCESS_FAILURE,
	                new BasicException.Parameter("jndiName", this.jndiName)
	            );
	        }
        }
        return this.delegate;
    }

    /**
     * Returns a connection. 
     * <p>
     * <em>The connection must be closed when it is not needed any more.</em>
     */
    public java.sql.Connection getConnection(
    ) throws SQLException {	
        try {
            return this.getDelegate().getConnection();    
        } catch(Exception ex) {
            // Mix-in the JNDI name even in case of SQL exceptions
            throw Throwables.initCause(
            	new SQLException("Connection acquisition failed"),
            	ex,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE,
                new BasicException.Parameter("jndiName", this.jndiName)
            ); 
        }    
    }

    public Connection getConnection(
        String username, 
        String password
    ) throws SQLException {
        return this.getDelegate().getConnection(username, password);
    }
    
    public int getLoginTimeout(
    ) throws SQLException {
        return this.getDelegate().getLoginTimeout();
    }
    
    public PrintWriter getLogWriter(
    ) throws SQLException {
        return this.getDelegate().getLogWriter();
    }
    
    public void setLoginTimeout(
        int seconds
    ) throws SQLException {
        this.getDelegate().setLoginTimeout(seconds);
    }
    
    public void setLogWriter(
        PrintWriter out
    ) throws SQLException {
        this.getDelegate().setLogWriter(out);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getName() + ": " + this.jndiName;
    }


    /* (non-Javadoc)
     * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
     */
    public boolean isWrapperFor(Class<?> iface)
        throws SQLException {
        return this.getDelegate().isWrapperFor(iface);
    }

    /* (non-Javadoc)
     * @see java.sql.Wrapper#unwrap(java.lang.Class)
     */
    public <T> T unwrap(Class<T> arg0)
        throws SQLException {
        return this.getDelegate().unwrap(arg0);
    }



    //-----------------------------------------------------------------------
    // Since JRE 7
    //-----------------------------------------------------------------------
    
	public Logger getParentLogger(
	) throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}
	

}
