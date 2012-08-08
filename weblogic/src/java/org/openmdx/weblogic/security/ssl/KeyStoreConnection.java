/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: KeyStoreConnection.java,v 1.8 2009/07/06 11:14:48 hburger Exp $
 * Description: Connection 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/07/06 11:14:48 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.security.auth.Subject;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.security.ExecutionContext;

import weblogic.jndi.Environment;
import weblogic.jndi.WLContext;
import weblogic.security.Security;
import weblogic.security.auth.Authenticate;

/**
 * Key Store Connection
 */
public class KeyStoreConnection
    implements ExecutionContext, Resettable, InitialContextFactory
{

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
     * Token
     */
    private transient long token = 0L;
    
    /**
     * The subject should not be serialized
     */
    private transient Subject subject = null;

    /**
     * Set certificateChain.
     * 
     * @param certificateChain The certificateChain to set.
     */
    void setCertificateChain(Certificate[] certificateChain) {
        this.certificateChain = certificateChain;
    }


    /**
     * Set environment.
     * 
     * @param environment The environment to set.
     */
    void setEnvironment(Hashtable<Object, Object> environment) {
        this.environment = environment;
    }

    /**
     * Set key.
     * 
     * @param key The key to set.
     */
    void setKey(Key key) {
        this.key = key;
    }

    /**
     * Tells whether a client certificate should be provided
     * 
     * @return <code>treu</code> if  a client certificate should be provided
     */
    private boolean hasSSLClientCertificate(){
        return this.key != null;
    }
    
    
    //------------------------------------------------------------------------
    // Implements ExcecutionContext
    //------------------------------------------------------------------------

    /**
     * Get the SSL Client Certificate Information
     * 
     * @return an InputStream[] consisting of the key an the certificate chain
     * 
     * @throws CertificateEncodingException
     */
    protected InputStream[] getSSLClientCertificate(
    ) throws CertificateEncodingException{
        InputStream[] streams = new InputStream[certificateChain.length + 1];
        streams[0] = new ByteArrayInputStream(this.key.getEncoded());
        for(
            int i = 0;
            i < this.certificateChain.length;
            i++
        ) streams[i+1] = new ByteArrayInputStream(this.certificateChain[i].getEncoded());
        return streams;
    }

    /**
     * Return the subject after lazy initialization
     * 
     * @return the - maybe lazy retrieved - Subject
     * 
     * @throws SecurityException
     */
    protected synchronized Subject getSubject(
    ) throws SecurityException {
        if(this.subject == null) try {
            Subject subject = new Subject();
            Environment environment = new Environment(this.environment);
            if(hasSSLClientCertificate()) environment.setSSLClientCertificate(
                getSSLClientCertificate()
            );
            Authenticate.authenticate(environment, subject);
            this.subject = subject;
            this.token = System.currentTimeMillis();
        } catch (Exception exception) {
            throw BasicException.initHolder(
                new SecurityException(
                    "Lazy connection establishment failed",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.COMMUNICATION_FAILURE,
                        new BasicException.Parameter(
                            WLContext.INITIAL_CONTEXT_FACTORY, 
                            this.environment.get(WLContext.INITIAL_CONTEXT_FACTORY)
                        ),
                        new BasicException.Parameter(
                            WLContext.PROVIDER_URL,
                            this.environment.get(WLContext.PROVIDER_URL)
                        ),
                        new BasicException.Parameter(
                            WLContext.SECURITY_PRINCIPAL,
                            this.environment.containsKey(WLContext.SECURITY_PRINCIPAL)
                        ),
                        new BasicException.Parameter(
                            WLContext.SECURITY_CREDENTIALS,
                            this.environment.containsKey(WLContext.SECURITY_CREDENTIALS)
                        ),
                        new BasicException.Parameter(
                            WLContext.SSL_CLIENT_CERTIFICATE,
                            hasSSLClientCertificate()
                        )                       
                   )
               )
            );
        }
        return this.subject;
    }
    
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.security.ExecutionContext#execute(java.security.PrivilegedAction)
     */
    @SuppressWarnings("unchecked")
    public Object execute(
        PrivilegedAction action
    ) {
        return Security.runAs(getSubject(), action);
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.security.ExecutionContext#execute(java.security.PrivilegedExceptionAction)
     */
    @SuppressWarnings("unchecked")
    public Object execute(
        PrivilegedExceptionAction action
    ) throws PrivilegedActionException {
        return Security.runAs(getSubject(), action);
    }

    
    //------------------------------------------------------------------------
    // Implements Resettable
    //------------------------------------------------------------------------

    /**
     * Reset 
     */
    public synchronized void reset(
    ){
        this.subject = null;
        this.token = 0L;
    }
        
    /* (non-Javadoc)
     * @see org.openmdx.kernel.io.Resettable#getResetToken()
     */
    public synchronized long getResetToken() {
        return this.token;
    }


    //------------------------------------------------------------------------
    // Implements Resettable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString(
    ) {
        return "Execution context for " + (
            hasSSLClientCertificate() ? "SSL Client Certificate" : "Principal/Credentials"
        ) + " authenticated connection to " + this.environment.get(WLContext.PROVIDER_URL);
    }


    //------------------------------------------------------------------------
    // Implements InitialContextFactory
    //------------------------------------------------------------------------

    /**
     * Combine environments
     */
    private Hashtable<Object, Object> getEnvironment(
        Hashtable<?, ?> environment
    ){
        if (environment == null || environment.isEmpty()) {
            return this.environment;
        } else {
            Hashtable<Object, Object> combined = new Hashtable<Object, Object>(this.environment);
            combined.putAll(environment);
            return combined;
        }
    }
       
    /* (non-Javadoc)
     * @see javax.naming.spi.InitialContextFactory#getInitialContext(java.util.Hashtable)
     */
    public Context getInitialContext(
        Hashtable<?, ?> environment
    ) throws NamingException {
        return new InitialContext(getEnvironment(environment));
    }


    //------------------------------------------------------------------------
    // Implements Connection
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.kernel.security.resource.Connection#close()
     */
    public void close() {
        this.certificateChain = null;
        this.environment = null;
        this.key = null;
        this.token = -1;
    }

}
