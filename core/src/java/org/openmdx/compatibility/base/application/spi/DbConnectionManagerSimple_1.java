/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DbConnectionManagerSimple_1.java,v 1.5 2008/09/10 08:55:24 hburger Exp $
 * Description: Non-pooling DB connection manager
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:24 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.compatibility.base.application.spi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.cci.DbConnectionManager_1_0;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.text.format.DatabaseDriverFormatter;
import org.openmdx.kernel.text.format.IndentingFormatter;

/**
 * Manages a single DB connection using various JDBC drivers.
 */
public class DbConnectionManagerSimple_1 
implements DbConnectionManager_1_0 {

    //---------------------------------------------------------------------------  
    /**
     * Creates a new connection manager.
     *
     * @param jdbcDriver name of JDBC driver class, e.g. for SQLServer2000=
     *        weblogic.jdbc.mssqlserver4.Driver, Oracle=oracle.jdbc.driver.OracleDriver.
     * 
     * @param connectionStr a database connection string, e.g. for SQLServer2000=
     *        spice@wfro:1433, Oracle=@OMEX.
     *
     * @param id a user id
     *
     * @param pw a password
     */
    public DbConnectionManagerSimple_1(      
        String jdbcDriver, 
        String connectionUrl, 
        String userId, 
        String pw,
        String exDomain
    ) throws ServiceException {

//      String prefix = null;
        Connection conn = null;

        this.jdbcDriver = jdbcDriver;
        this.userId = userId;

        try {
            if(this.jdbcDriver.equalsIgnoreCase("com.microsoft.jdbc.sqlserver.SQLServerDriver")) {
                this.connectionUrl = connectionUrl.startsWith("jdbc") 
                ? connectionUrl 
                    : "jdbc:microsoft:sqlserver://" + connectionUrl;
                Classes.getKernelClass(this.jdbcDriver).newInstance();
            }
            else if(this.jdbcDriver.equalsIgnoreCase("weblogic.jdbc.mssqlserver4.Driver")) {
                this.connectionUrl = connectionUrl.startsWith("jdbc") 
                ? connectionUrl 
                    : "jdbc:weblogic:mssqlserver4:" + connectionUrl;
                Classes.getKernelClass(this.jdbcDriver).newInstance();
            }
            else if(this.jdbcDriver.equalsIgnoreCase("oracle.jdbc.driver.OracleDriver")) {
                this.connectionUrl = connectionUrl.startsWith("jdbc") 
                ? connectionUrl 
                    : "jdbc:oracle:thin:" + connectionUrl;
                Classes.getKernelClass(this.jdbcDriver).newInstance();
            }
            else {
                this.connectionUrl = connectionUrl;
                Classes.getKernelClass(this.jdbcDriver).newInstance();
            }
        }
        catch(IllegalAccessException ex) {
            throw new ServiceException(
                ex,
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.BAD_PARAMETER,
                "illegal access to class",
                new BasicException.Parameter("jdbcDriver", this.jdbcDriver)
            ).log();
        }
        catch(InstantiationException ex) {
            throw new ServiceException(
                ex,
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.BAD_PARAMETER,
                "can not instantiate class",
                new BasicException.Parameter("jdbcDriver", this.jdbcDriver)
            ).log();
        }
        catch(ClassNotFoundException ex) {
            throw new ServiceException(
                ex,
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.BAD_PARAMETER,
                "Class not found",
                new BasicException.Parameter("jdbcDriver", this.jdbcDriver)
            ).log();
        }

        try {
            SysLog.detail(
                "Matching Driver",
                new DatabaseDriverFormatter(
                    DriverManager.getDriver(this.connectionUrl),
                    this.connectionUrl,
                    System.getProperties()
                )
            );
            conn = DriverManager.getConnection(
                this.connectionUrl, 
                this.userId, 
                pw
            );
            SysLog.detail(
                "DB connection created",
                new IndentingFormatter(
                    ArraysExtension.asMap(
                        new String[]{"jdbcDriver","connectionStr","userId", "url"},
                        new Object[]{this.jdbcDriver,connectionUrl,this.userId,this.connectionUrl}
                    )
                )
            );
        } catch(SQLException ex) {
            throw new ServiceException(
                ex,
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.MEDIA_ACCESS_FAILURE,
                "SQL Exception encountered",
                new BasicException.Parameter("jdbcDriver", this.jdbcDriver),
                new BasicException.Parameter("connectionStr", this.connectionUrl),
                new BasicException.Parameter("userId", this.userId),
                new BasicException.Parameter("url", this.connectionUrl),
                new BasicException.Parameter("sqlErrorCode", ex.getErrorCode())
            ).log();
        }
        this.conn = conn;
    }

    //---------------------------------------------------------------------------   
    public java.sql.Connection getConnection() 
    throws ServiceException {
        return this.conn;
    }

    //---------------------------------------------------------------------------   
    /**
     * connection is not closed. The same connection is returned
     * on next call to getConnection()
     */
    public void closeConnection(
        java.sql.Connection conn)
    throws ServiceException {
        //
    }

    //---------------------------------------------------------------------------  
    public void activate()
    throws java.lang.Exception, ServiceException {
        //
    }

    //---------------------------------------------------------------------------  
    /**
     * Closes the connection manager
     *
     * The manager closes the connection, if it is still open.
     */
    public void deactivate() 
    throws java.lang.Exception, ServiceException {

        if(this.conn != null) {
            try {
                if(!this.conn.isClosed()) {
                    this.conn.close();
                }
                SysLog.detail("DB connection closed", "url=\u00ab" + this.connectionUrl + "\u00bb, uid=\u00ab" + this.userId + "\u00bb");
            }
            catch(SQLException ex) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.MEDIA_ACCESS_FAILURE,
                    "SQL Exception encountered",
                    new BasicException.Parameter("jdbcDriver", this.jdbcDriver),
                    new BasicException.Parameter("connectionStr", this.connectionUrl),
                    new BasicException.Parameter("userId", this.userId),
                    new BasicException.Parameter("sqlErrorCode", ex.getErrorCode())
                ).log();
            }
        }  
        this.conn = null;
    }
    //---------------------------------------------------------------------------  

    private String jdbcDriver; 
    private String connectionUrl; 
    private String userId; 
    private java.sql.Connection conn;

}

//--- End of File -----------------------------------------------------------
