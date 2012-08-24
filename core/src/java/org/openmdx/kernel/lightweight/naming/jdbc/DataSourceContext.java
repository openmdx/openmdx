/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: DataSource Context 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
 * All rights reserved.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.lightweight.naming.jdbc;

import java.util.Collections;

import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.transaction.TransactionManager;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.lightweight.naming.spi.ResourceContext;
import org.openmdx.kernel.lightweight.resource.LightweightConnectionManager;
import org.openmdx.kernel.lightweight.sql.DatabaseConnection;
import org.openmdx.kernel.lightweight.sql.DatabaseConnectionRequestInfo;
import org.openmdx.kernel.lightweight.sql.LightweightXADataSource;
import org.openmdx.kernel.lightweight.sql.ManagedDatabaseConnectionFactory;


/**
 * DataSource Context
 */
class DataSourceContext extends ResourceContext {

    /**
     * Constructor 
     *
     * @param transactionManager
     */
    DataSourceContext(
        TransactionManager transactionManager
    ){
        this.connectionManager = new LightweightConnectionManager(
            Collections.emptySet(), // credentials 
            DatabaseConnection.class,
            transactionManager, 
            null, // maximumCapacity 
            null // maximumWait
        ); 
    }

    /**
     * The Connection Manager
     */
    private final ConnectionManager connectionManager;
    
    /* (non-Javadoc)
     * @see javax.naming.Context#lookup(java.lang.String)
     */
    public Object lookup(
        String name
    ) throws NamingException {
        Object dataSource = lookupLink(name);
        if(dataSource == null) try {
            bind(
                name,
                dataSource = new ManagedDatabaseConnectionFactory(
                    new LightweightXADataSource(name, environment),
                    new DatabaseConnectionRequestInfo(
                        null, // transactionIsolation
                        null, // validationStatement
                        null // loginTimeout
                    )
                ).createConnectionFactory(
                    this.connectionManager
                )
            );
        } catch (ResourceException exception) {
            throw Throwables.initCause(
                new NamingException("Lazy datasource set-up failed"),
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE,
                new BasicException.Parameter("connectionURL", name)
            );
        }
        return dataSource;
    }

}
