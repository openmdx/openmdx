/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LightweightXADataSource.java,v 1.2 2006/08/09 14:37:29 hburger Exp $
 * Description: Lightweight XADataSource
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/08/09 14:37:29 $
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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import javax.sql.XAConnection;
import javax.sql.XADataSource;


/**
 * Lightweight XADataSource
 */
public class LightweightXADataSource
    implements XADataSource 
{

    /**
     * Constructor
     * 
     * @param url a database url of the form 
     * <code>jdbc:subprotocol:subname</code>
     * @parame info a list of arbitrary string tag/value pairs as connection 
     * arguments
     */
    public LightweightXADataSource(
        String url,
        Map info
    ) {
        this.url = url;
        this.info = info;
    }
    
    /**
     * A database url of the form <code>jdbc:subprotocol:subname</code>
     */
    private final String url;
    
    /**
     * A list of arbitrary string tag/value pairs as connection arguments
     */
    private final Map info;
    
    /* (non-Javadoc)
     * @see javax.sql.XADataSource#getLogWriter()
     */
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    /* (non-Javadoc)
     * @see javax.sql.XADataSource#getLoginTimeout()
     */
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    /* (non-Javadoc)
     * @see javax.sql.XADataSource#getXAConnection()
     */
    public XAConnection getXAConnection() throws SQLException {
        return getXAConnection(null, null);
    }

    /* (non-Javadoc)
     * @see javax.sql.XADataSource#getXAConnection(java.lang.String, java.lang.String)
     */
    public XAConnection getXAConnection(
        String user, 
        String password
    ) throws SQLException {
        Connection connection;
        if(this.info == null || this.info.isEmpty()) {
             connection = DriverManager.getConnection(
                 this.url,
                 user,
                 password
             );
        } else {
            Properties properties = new Properties();
            if(this.info != null) properties.putAll(this.info);
            if(user != null) properties.put("user", user);
            if(password != null) properties.put("password", password);
            connection = DriverManager.getConnection(this.url, properties);
        }
        return new LightweightXAConnection(connection);
    }

    /* (non-Javadoc)
     * @see javax.sql.XADataSource#setLogWriter(java.io.PrintWriter)
     */
    public void setLogWriter(PrintWriter out) throws SQLException {
        DriverManager.setLogWriter(out);        
    }

    /* (non-Javadoc)
     * @see javax.sql.XADataSource#setLoginTimeout(int)
     */
    public void setLoginTimeout(int seconds) throws SQLException {
        DriverManager.setLoginTimeout(seconds);
    }

}
