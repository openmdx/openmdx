/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ManagedDataproviderConnectionFactory.java,v 1.1 2009/01/12 12:49:24 wfro Exp $
 * Description: ManagedDataproviderConnectionFactory
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/12 12:49:24 $
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
package org.openmdx.kernel.application.container.spi.http;

import java.io.PrintWriter;
import java.net.URL;
import java.util.Set;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.openmdx.compatibility.kernel.application.cci.Classes;


/**
 * ManagedDataproviderConnectionFactory
 */
@SuppressWarnings("unchecked")
public class ManagedDataproviderConnectionFactory 
    implements ManagedConnectionFactory 
{

    /**
     * serialVersionUID to implement Serializable
     */
    private static final long serialVersionUID = 3544672867129176625L;

    /**
     * Constructor to implement Serializable
     */
    protected ManagedDataproviderConnectionFactory() {
        super();
    }

    /**
     * Constructor 
     */
    public ManagedDataproviderConnectionFactory(
        String connectionFactoryClass,
        URL url,
        PasswordCredential passwordCredential
    ) {
        this.connectionFactoryClass = connectionFactoryClass;
        this.url = url;
        this.credential = passwordCredential;
    }

    /**
     * @serial
     */
    private String connectionFactoryClass;

    /**
     * @serial
     */
    private URL url;
    
    /**
     * @serial
     */
    private PasswordCredential credential;
    
    /**
     * The ManagedDataproviderConnectionFactory's log writer
     */
    private transient PrintWriter logWriter = null;
    
    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory(javax.resource.spi.ConnectionManager)
     */
    public Object createConnectionFactory(
        ConnectionManager connectionManager
    ) throws ResourceException {
        throw propagate(
            new NotSupportedException(
                "Only resource adapter provided connection managers are supported at the moment"
            )
        );
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory()
     */
    public Object createConnectionFactory(
    ) throws ResourceException {        
        try {
            DataproviderConnectionFactory connectionFactory = (DataproviderConnectionFactory) Classes.getApplicationClass(
                this.connectionFactoryClass
            ).newInstance();
            Subject subject = new Subject();
            subject.getPrivateCredentials().add(this.credential);
            connectionFactory.initialize(subject, this.url);
            return connectionFactory;
        } catch (Exception exception) {
            throw propagate(
                new ResourceAdapterInternalException(
                    "Could not initialize dataprovider http connection factory",
                    exception
                )
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createManagedConnection(javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    public ManagedConnection createManagedConnection(
       Subject subject,
       ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        throw propagate(
            new NotSupportedException(
               "Managed connections are not supported yet"
            )
        );
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#matchManagedConnections(java.util.Set, javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    public ManagedConnection matchManagedConnections(
        Set managedConnections,
        Subject subject,
        ConnectionRequestInfo connectionRequestInfo
   ) throws ResourceException {
        throw propagate(
            new NotSupportedException(
                "Managed connections are not supported yet"
            )
        );
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#setLogWriter(java.io.PrintWriter)
     */
    public void setLogWriter(
        PrintWriter logWriter
    ) throws ResourceException {
        this.logWriter = logWriter;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#getLogWriter()
     */
    public PrintWriter getLogWriter(
   ) throws ResourceException {
        return this.logWriter;
    }

    /**
     * Log a resource exception
     * 
     * @param exception the exception to be logged
     * 
     * @return the exception itself
     */
    private ResourceException propagate(
       ResourceException exception
    ){
        if(this.logWriter != null) exception.printStackTrace(this.logWriter);
        return exception;
    }

}
