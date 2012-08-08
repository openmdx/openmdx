/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ManagedDatabaseConnectionFactory.java,v 1.8 2008/03/21 18:38:41 hburger Exp $
 * Description: Managed Database Connection Factory
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:38:41 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.sql.XADataSource;


/**
 * Managed Database Connection Factory
 */
@SuppressWarnings("unchecked")
public class ManagedDatabaseConnectionFactory implements ManagedConnectionFactory {

    public ManagedDatabaseConnectionFactory(
        XADataSource xaDataSource,
        DatabaseConnectionRequestInfo connectionRequestInfo
    ){
        this.xaDataSource = xaDataSource;
        this.connectionRequestInfo = connectionRequestInfo;
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3257565113841104953L;

    /**
     * @serial Database Connection Arguments
     */
    private final XADataSource xaDataSource;
    
    /**
     * @serial Database Connection Arguments
     */
    private final DatabaseConnectionRequestInfo connectionRequestInfo;

    /**
     * 
     */
    private PrintWriter logWriter = null;

    
    //------------------------------------------------------------------------
    // Implements ManagedConnectionFactory
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory(javax.resource.spi.ConnectionManager)
     */
    public Object createConnectionFactory(
        ConnectionManager connectionManager
    ) throws ResourceException {
        Object connectionFactory = new DatabaseConnectionFactory(
            connectionManager,
            this,
            this.connectionRequestInfo       
        );
        return connectionFactory;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory()
     */
    public synchronized Object createConnectionFactory(
    ) throws ResourceException {
        return createConnectionFactory(null);
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createManagedConnection(javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    public ManagedConnection createManagedConnection(
        Subject subject, 
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        try {
            Set credentials = subject.getPrivateCredentials(PasswordCredential.class);
            if(credentials.isEmpty()) {
                return new ManagedDatabaseConnection(
                    this,
                    this.xaDataSource.getXAConnection()
                );
            } else {
                PasswordCredential credential = (PasswordCredential) credentials.iterator().next();
                return new ManagedDatabaseConnection(
                    this,
                    this.xaDataSource.getXAConnection(
                        credential.getUserName(),
                        new String(credential.getPassword())
                    )
                );
            }
        } catch (SQLException exception) {
            throw propagate("Could not create managed connection", exception);
        }
    }

    /**
     * Create and log a resource exxception if a logWriter is set.
     * 
     * @param message
     * @param cause
     * 
     * @return the resource exception
     */
    ResourceException propagate(
        String message,
        Exception cause
    ){
        ResourceException exception = new ResourceAdapterInternalException(message, cause);
        if(this.logWriter != null) exception.printStackTrace(logWriter);
        return exception;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#matchManagedConnections(java.util.Set, javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    public ManagedConnection matchManagedConnections(
        Set connectionSet, 
        Subject subject, 
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        for(
            Iterator i = connectionSet.iterator();
            i.hasNext();
        ){
            ManagedConnection candidate = (ManagedConnection) i.next();
            if(
                candidate instanceof ManagedDatabaseConnection &&
                ((ManagedDatabaseConnection)candidate).managedConnectionFactory == this
            ) return candidate;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#setLogWriter(java.io.PrintWriter)
     */
    public void setLogWriter(PrintWriter logWriter){
        this.logWriter = logWriter;
        try {
            this.xaDataSource.setLogWriter(logWriter);
        } catch (SQLException exception) {
            // ignore
        }
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#getLogWriter()
     */
    public PrintWriter getLogWriter(){
        return this.logWriter;
    }
    
}
