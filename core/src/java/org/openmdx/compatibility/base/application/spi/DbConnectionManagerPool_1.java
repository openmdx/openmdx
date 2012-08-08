/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DbConnectionManagerPool_1.java,v 1.6 2008/09/10 08:55:24 hburger Exp $
 * Description: Pooling DB connection manager
 * Revision:    $Revision: 1.6 $
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
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.cci.DbConnectionManager_1_0;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * Manages a connection pool. The constructor takes a DataSource as parameter
 * assuming that it is a pooled DataSource. In the case of pooled DataSources
 * getConnection() and closeConnection() get and return connections from/to
 * the pool. The real connections are managed by the DataSource pool manager.
 * <p>
 * How to configure pools for BEA/WLE
 * <ul>
 *   <li> define a pool in ubbconfig
 *   <pre>
 *     *JDBCCONNPOOLS                              
 *     <poolName>
 *       SRVGRP          = <group name of server> (ex. Dataprovider_Directory)
 *       SRVID           = <id of server> (ex. 1000)
 *       DRIVER          = "oracle.jdbc.driver.OracleDriver"  
 *       URL             = "jdbc:oracle:oci8:system@OMEX"    
 *       PROPS           = "user=system;password=manager" 
 *       ENABLEXA        = N                 
 *       INITCAPACITY    = 1
 *       MAXCAPACITY     = 10
 *       CAPACITYINCR    = 1                
 *       CREATEONSTARTUP = Y
 *       ALLOWSHRINKING  = Y
 *       SHRINKPERIOD    = 30
 *   </pre>
 *   <li> reference the the pool for each dataprovider with the option --connectionUrl=jdbc/<poolName>
 * </ul>
 * <p>
 * How to configure pools for BEA/WLS
 * <ul>
 *   <li> define a connection pool
 *   <pre>
 *     <JDBCConnectionPool 
 *       CapacityIncrement="1"
 *       ConnLeakProfilingEnabled="false" DeploymentOrder="1000"
 *       DriverName="oracle.jdbc.driver.OracleDriver"
 *       EnableResourceHealthMonitoring="true" InitialCapacity="1"
 *       JDBCXADebugLevel="0" KeepLogicalConnOpenOnRelease="false"
 *       KeepXAConnTillTxComplete="false" LoginDelaySeconds="0"
 *       MaxCapacity="10" Name="Dataprovider_Pool"
 *       NeedTxCtxOnClose="false" NewXAConnForCommit="false"
 *       PrepStmtCacheProfilingEnabled="false"
 *       PrepStmtCacheProfilingThreshold="10"
 *       PreparedStatementCacheSize="10"
 *       Properties="user=system;password=manager;dll=ocijdbc8;protocol=oci8"
 *       RecoverOnlyOnce="false" RefreshMinutes="1"
 *       ShrinkPeriodMinutes="15" ShrinkingEnabled="true"
 *       SqlStmtMaxParamLength="10" SqlStmtParamLoggingEnabled="false"
 *       SqlStmtProfilingEnabled="false" SupportsLocalTransaction="false"
 *       Targets="myserver" TestConnectionsOnRelease="false"
 *       TestConnectionsOnReserve="false" TestTableName="TEST_ATTRS"
 *       URL="jdbc:oracle:oci8:system@OMEX" XAEndOnlyOnce="false"/> 
 *   </pre>
 *   <p>
 *   <li> define a data source
 *   <pre>
 *     <JDBCDataSource 
 *       JNDIName="jdbc.Dataprovider_Pool"
 *       Name="Dataprovider_DataSource" 
 *       PoolName="Dataprovider_Pool"
 *       RowPrefetchEnabled="true" 
 *       Targets="myserver"/>
 *   </pre>
 *   <p>
 *   <li> define weblogic-ejb-jar.xml
 *   <pre>
 *     <resource-description>
 *       <res-ref-name>jdbc/Dataprovider_Pool</res-ref-name>
 *       <jndi-name>jdbc.Dataprovider_Pool</jndi-name>
 *     </resource-description>
 *   </reference-descriptor>
 *   </pre>
 *   <p>
 *   <li> define ejb-jar.xml
 *   <pre>
 *     <env-entry>
 *       <env-entry-name>connectionUrl</env-entry-name> 
 *       <env-entry-type>java.lang.String</env-entry-type> 
 *       <env-entry-value>jdbc/Dataprovider_Pool</env-entry-value> 
 *     </env-entry>
 *   </pre>
 * </ul>
 * 
 * How to configure Oracle-XA for BEA/WLS:
 * <ul>
 *
 * <li>Pool und DataSource aufsetzen in WLS config.xml:</li>
 * <pre>
 *  <JDBCConnectionPool
 *    DriverName="oracle.jdbc.xa.client.OracleXADataSource"
 *    InitialCapacity="1" MaxCapacity="10"
 *    Name="jdbcXADataprovidersPool"
 *    Properties="
 *      user=LEXPERT;
 *      url=jdbc:oracle:thin:@unx90007:1521:t7;
 *      password=LEXPERT;
 *      dataSourceName=jdbcXADataprovidersPool"
 *    Targets="SampleServer"
 *    URL="jdbc:oracle:thin:@unx90007:1521:t7"/>
 *  <JDBCTxDataSource 
 *    JNDIName="jdbc.dataproviders"
 *    Name="jdbc.xa.dataproviders" 
 *    PoolName="jdbcXADataprovidersPool"
 *    Targets="SampleServer"/>
 *
 * </ul>
 * 
 */
public class DbConnectionManagerPool_1 
implements DbConnectionManager_1_0 
{

    /**
     * Creates a new connection manager. 
     * Using a pool of connections to the database.
     *
     * @param poolName JDBC Connection Pool name
     *
     * @param exDomain the exception domain name
     */
    public DbConnectionManagerPool_1(
        String jndiName
    ) throws ServiceException {
        this.pool = null; 
        this.jndiName = jndiName;
    }

    /**
     * Get a data source
     * 
     * @return the DataSource corresponding to the JNDI name
     * @throws ServiceException
     * 
     * @throws ServiceException
     */
    protected DataSource getDataSource(
    ) throws ServiceException{
        try {
            return (DataSource)new InitialContext().lookup(this.jndiName);
        } catch (NamingException exception) {
            throw new ServiceException(
                exception, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE, 
                "Could not lookup connection pool '" + this.jndiName + "'", 
                new BasicException.Parameter("jndiName",this.jndiName)
            ).log();
        }
    }

    /**
     * Get a database connection pool
     * 
     * @return the DataSource corresponding to the JNDI name
     * 
     * @throws ServiceException
     */
    private synchronized DataSource getPool(
    ) throws ServiceException{
        if (this.pool == null){
            SysLog.detail(
                "Acquire Connection Pool",
                this.jndiName
            );
            this.pool = getDataSource();
        }
        return this.pool;	    
    }

    /**
     * Returns a connection. 
     * <p>
     * The connection must be closed when it is not needed any more, so it is 
     * available again. Otherwise the connection pool runs out of available 
     * connections!
     */
    public java.sql.Connection getConnection(
    ) throws ServiceException {	
        try {
            Connection con = getPool().getConnection();    
            if(con == null) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE, 
                "No connection available from '" + this.jndiName + "'", 
                new BasicException.Parameter("jndiName",this.jndiName)
            ).log();
            return con;
        } catch(SQLException ex) {
            throw new ServiceException(
                ex, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE, 
                "Failure when getting a connection from '" + this.jndiName + "'", 
                new BasicException.Parameter("jndiName",this.jndiName)
            ).log();
        }    
    }

    /**
     * The connection is returned to the free connection pool
     * 
     * @param the connection to be returned 
     */
    public void closeConnection(
        java.sql.Connection conn
    ) throws ServiceException {
        if(conn != null) try {
            conn.close();
        } catch(java.sql.SQLException ex) {
            throw new ServiceException(ex);
        }
    }

    /**
     * 
     */
    public void activate(
    ) throws java.lang.Exception {
        //
    }

    /**
     * Closes the connection manager. 
     *
     * Does not close any open connections that have been obtained from the manager.
     */
    public void deactivate(
    ) throws java.lang.Exception {
        this.pool = null;
        SysLog.detail(
            "Connection pool discarded",
            this.jndiName
        );
    }

    /**
     * Get the connection pool's JNDI name
     * 
     * @return the connection pool's JNDI name
     */
    protected final String getName(){
        return this.jndiName;
    }

    /**
     * 
     */
    private DataSource pool;

    /**
     * 
     */
    private final String jndiName;

}

