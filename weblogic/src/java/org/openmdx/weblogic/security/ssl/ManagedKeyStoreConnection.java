/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ManagedKeyStoreConnection.java,v 1.3 2007/08/13 17:33:08 hburger Exp $
 * Description: Managed Connection 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/08/13 17:33:08 $
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
package org.openmdx.weblogic.security.ssl;

import java.io.PrintWriter;
import java.security.Key;
import java.security.cert.Certificate;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

/**
 * Managed Connection
 */
public class ManagedKeyStoreConnection 
    implements javax.resource.spi.ManagedConnection
{

    /**
     * Constructor 
     *
     * @param credential
     * @param certificate
     * @param key
     * @param environment TODO
     */
    protected ManagedKeyStoreConnection(
        PasswordCredential credential,
        Certificate[] certificate,
        Key key, 
        Map<String, String> environment
    ) {
        this.credential = credential;
        this.certificateChain = certificate;
        this.key = key;
        this.environment = new Hashtable<Object, Object>(environment);
        if(key == null && credential != null) {
            this.environment.put(Context.SECURITY_PRINCIPAL, credential.getUserName());
            this.environment.put(Context.SECURITY_CREDENTIALS, new String(credential.getPassword()));
        }
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
    private Certificate[] certificateChain;

    /**
     * 
     */
    private Key key;

    /**
     * 
     */
    private Hashtable<Object, Object> environment;
    
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
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#associateConnection(java.lang.Object)
     */
    public void associateConnection(
        Object connection
    ) throws ResourceException {
        try {
            KeyStoreConnection executionContextConnection = (KeyStoreConnection) connection;
            executionContextConnection.setKey(key);
            executionContextConnection.setCertificateChain(this.certificateChain);
            executionContextConnection.setEnvironment(this.environment);
        } catch (ClassCastException exception) {
            throw (ResourceException) new ResourceAdapterInternalException(
                "Managed connection class and connection class do not match"
            ).initCause(
                exception
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#cleanup()
     */
    public void cleanup(
    )throws ResourceException {
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#destroy()
     */
    public void destroy(
    ) throws ResourceException {
        this.credential = null;
        this.certificateChain = null;
        this.key = null;
        this.environment = null;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#getConnection(javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    public Object getConnection(
        Subject subject, 
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        Object connection = new KeyStoreConnection();
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
        return this.credential == null ? 
            credential == null :
            this.credential.equals(credential);
    }

    /**
     * Retrieve the alias
     * 
     * @return the alias
     */
    String getUserName(){
        return this.credential == null ? null : this.credential.getUserName();
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
            return ManagedKeyStoreConnection.this.getUserName();
        }

    }
    
}
