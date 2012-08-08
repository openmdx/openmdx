/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ManagedKeyStoreConnection.java,v 1.2 2007/10/10 16:05:53 hburger Exp $
 * Description: Managed Connection 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:05:53 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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
package org.openmdx.base.security.pki;

import java.io.PrintWriter;
import java.security.Key;
import java.security.cert.Certificate;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

/**
 * Managed Connection
 */
class ManagedKeyStoreConnection 
    implements ManagedConnection
{

    /**
     * Constructor 
     *
     * @param credential
     * @param certificate
     * @param key
     */
    ManagedKeyStoreConnection(
        PasswordCredential credential,
        Certificate certificate,
        Key key
    ) {
        this.credential = credential;
        this.certificate = certificate;
        this.key = key;
    }

    /**
     * Lazy initialized meta data
     */
    private ManagedConnectionMetaData metaData = null;

    /**
     * 
     */
    private PasswordCredential credential;

    /**
     * 
     */
    private Certificate certificate;

    /**
     * 
     */
    private Key key;

    /**
     * 
     */
    private PrintWriter logWriter = null;

    /**
     * 
     */
    private final static String NON_TRANSACTIONAL = "KeyStore resources are non-transactional";
        
    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#addConnectionEventListener(javax.resource.spi.ConnectionEventListener)
     */
    public void addConnectionEventListener(
        ConnectionEventListener connectionEventListener
    ) {
        //
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#associateConnection(java.lang.Object)
     */
    public void associateConnection(
        Object connection
    ) throws ResourceException {
        try {            
            CertificateConnection certificateConnection = (CertificateConnection) connection;
            certificateConnection.setAlias(getAlias());
            certificateConnection.setCertificate(this.certificate);
            if(connection instanceof KeyConnection) {
                KeyConnection keyConnection = (KeyConnection) connection;
                keyConnection.setKey(this.key);
            }
        } catch (ClassCastException exception){
            throw new ResourceException(
                "Managed connection class and connection class do not match",
                exception
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#cleanup()
     */
    public void cleanup(
    )throws ResourceException {
        //
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#destroy()
     */
    public void destroy(
    ) throws ResourceException {
        this.credential = null;
        this.certificate = null;
        this.key = null;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#getConnection(javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    public Object getConnection(
        Subject subject, 
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        Object connection = this.key == null ? new CertificateConnection() : new KeyConnection();
        this.associateConnection(connection);
        return connection;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#getLocalTransaction()
     */
    public LocalTransaction getLocalTransaction(
    ) throws ResourceException {
        throw log(new NotSupportedException(NON_TRANSACTIONAL));
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#getLogWriter()
     */
    public PrintWriter getLogWriter(
    ) throws ResourceException {
        return this.logWriter;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#getMetaData()
     */
    public ManagedConnectionMetaData getMetaData(
    ) throws ResourceException {
        return this.metaData == null ?
            this.metaData = new MetaData() :
            this.metaData;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#getXAResource()
     */
    public XAResource getXAResource(
    ) throws ResourceException {
        throw log(new NotSupportedException(NON_TRANSACTIONAL));
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#removeConnectionEventListener(javax.resource.spi.ConnectionEventListener)
     */
    public void removeConnectionEventListener(
        ConnectionEventListener connectionEventListener
    ) {
        //
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#setLogWriter(java.io.PrintWriter)
     */
    public void setLogWriter(
        PrintWriter logWriter
    ) throws ResourceException {
        this.logWriter = logWriter;        
    }
    
    /**
     * Log and return an exception
     * 
     * @param an exception
     * 
     * @return the exception
     */
    private ResourceException log(
        ResourceException exception
    ){
        try {
            PrintWriter logWriter = getLogWriter();
            if(logWriter != null) exception.printStackTrace(logWriter);
        } catch (Exception ignore) {
            // Ensure that thr original exception will be available
        }
        return exception;
    }

    /**
     * Test whether the managed connection was created with the same credentials.
     * 
     * @param credential
     * 
     * @return <code>true</code> if the managed connection was created with the same credentials
     */
    boolean matches(
        PasswordCredential credential
    ){
        return this.credential.equals(credential);
    }

    /**
     * Retrieve the alias
     * 
     * @return the alias
     */
    private String getAlias(){
        return this.credential.getUserName();
    }
    
    
    //------------------------------------------------------------------------
    // Class MetaData
    //------------------------------------------------------------------------
    
    /**
     * ManagedConnectionMetaData implementation
     */
    class MetaData
        implements ManagedConnectionMetaData
    {

        /**
         * Constructor 
         *
         * @param alias the certificate's alias
         */
        public MetaData(
        ) {
            super();
        }

        /* (non-Javadoc)
         * @see javax.resource.spi.ManagedConnectionMetaData#getEISProductName()
         */
        public String getEISProductName(
        ) throws ResourceException {
            return "KeyStore";
        }

        /* (non-Javadoc)
         * @see javax.resource.spi.ManagedConnectionMetaData#getEISProductVersion()
         */
        public String getEISProductVersion(
        ) throws ResourceException {
            return "1.0";
        }

        /* (non-Javadoc)
         * @see javax.resource.spi.ManagedConnectionMetaData#getMaxConnections()
         */
        public int getMaxConnections(
        )throws ResourceException {
            return 0; // no limit
        }

        /* (non-Javadoc)
         * @see javax.resource.spi.ManagedConnectionMetaData#getUserName()
         */
        public String getUserName(
        ) throws ResourceException {
            return getAlias();
        }

    }
    
}
