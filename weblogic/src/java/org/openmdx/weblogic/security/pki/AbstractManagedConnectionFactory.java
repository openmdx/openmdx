/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractManagedConnectionFactory.java,v 1.1 2009/01/04 18:10:46 wfro Exp $
 * Description: Abstract Managed Connection Factory
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/04 18:10:46 $
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

package org.openmdx.weblogic.security.pki;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.SecurityException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.openmdx.kernel.application.container.lightweight.ShareableConnectionManager;

/**
 * Abstract Managed Connection Factory
 */
@SuppressWarnings("unchecked")
public abstract class AbstractManagedConnectionFactory
    implements ManagedConnectionFactory
{

    /**
     * 
     */
    private Object identity = null;
    
    /**
     * 
     */
    private PrintWriter logWriter;
    
    /**
     * 'KeyStore' property
     */
    private String keyStoreType;
    
    /**
     * 'ConnectionURL' property
     */
    private String connectionURL;
    
    /**
     * 'PassPhraseSeparator' property
     */
    private String passPhraseSeparator;

    /**
     * 
     */
    private String userName;
    
    /**
     * 
     */
    private String password;
    
    /**
     * The resource adapter's internal connection manager
     */
    private ConnectionManager connectionManager = null;
    
    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory()
     */
    public final Object createConnectionFactory(
    ) throws ResourceException {
        if(this.connectionManager == null) {
            if(this.userName == null || "".equals(this.userName)) throw log(
                new SecurityException("Missing UserName to be used as certificate alias")
            );
            this.connectionManager = new ShareableConnectionManager(
                Collections.singleton(
                    new PasswordCredential(
                        this.userName,
                        (this.password == null ? "" : this.password).toCharArray()
                    )
                )
            );
        }
        return createConnectionFactory(this.connectionManager);
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory(javax.resource.spi.ConnectionManager)
     */
    public final Object createConnectionFactory(
        ConnectionManager connectionManager
    ) throws ResourceException {
        return connectionManager == null ?
            createConnectionFactory() :
            new KeyStoreConnectionFactory(this, connectionManager);
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#getLogWriter()
     */
    public final PrintWriter getLogWriter(
    ) throws ResourceException {
        return this.logWriter;
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#matchManagedConnections(java.util.Set, javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    public final javax.resource.spi.ManagedConnection matchManagedConnections(
        Set managedConnections,
        Subject subject,
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        PasswordCredential credential = getCredential(subject, true);
        for(
            Iterator i = managedConnections.iterator();
            i.hasNext();
        ){
            Object managedConnection = i.next();
            if(
                managedConnection instanceof ManagedKeyStoreConnection &&
                ((ManagedKeyStoreConnection)managedConnection).matches(credential)
            ) return (javax.resource.spi.ManagedConnection)managedConnection;
        }
        return null;
    }
    
    /**
     * 
     * @param subject
     * @return
     * @throws ResourceException
     */
    protected PasswordCredential getCredential(
        Subject subject,
        boolean mandatory
    ) throws ResourceException{
        Set credentials = subject == null ? Collections.EMPTY_SET : subject.getPrivateCredentials(
            PasswordCredential.class
        );
        switch(credentials.size()) {
            case 0: 
                if(mandatory) {
                    throw log(
                        new SecurityException(
                            "There is no " +   
                            PasswordCredential.class.getName() +
                            " instance among the subject's private credentials"
                        )
                    );
                } else {
                    return null;
                }
            case 1:
                PasswordCredential credential = (PasswordCredential) credentials.iterator().next();
                if(credential.getUserName() == null) throw log(
                    new SecurityException(
                        "Missing user name to be used as certificate alias"
                    )
                );
                return credential;
           default:
               throw log(
                   new SecurityException(
                       "There is more than one " +   
                       PasswordCredential.class.getName() +
                       " instance among the subject's private credentials"
                   )
               );
        }
    }

    /**
     * Retrieve the first pass phrase encoded in the credential's password field
     * 
     * @param credential
     * 
     * @return the first pass phrase encoded in the credential's password field
     * 
     * @throws ResourceException
     */
    protected final char[][] getPassPhrases(
        PasswordCredential credential
    ) throws ResourceException {
        char[] password = credential.getPassword();
        if(passPhraseSeparator == null) {
            return new char[][]{password};
        } else switch (getPassPhraseSeparator().length()){
            case 0: 
                return new char[][]{password};
            case 1:
                char passPhraseSeparator = getPassPhraseSeparator().charAt(0);
                List passPhrases = new ArrayList();
                for(
                    int i = 0, begin = 0;
                    i <= password.length;
                    i++
                ) if (
                    i == password.length ||
                    password[i] == passPhraseSeparator
                ) {                    
                    char[] passPhrase = new char[i - begin];
                    System.arraycopy(password, begin, passPhrase, 0, passPhrase.length);
                    passPhrases.add(passPhrase);
                    begin = i + 1;
                }
                return (char[][]) passPhrases.toArray(new char[passPhrases.size()][]);
            default: 
                throw log(
                    new InvalidPropertyException(
                        "A pass phrase separator must be one character long: '" + getPassPhraseSeparator() + "'"
                    )
                );
        }
    }
    
    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#setLogWriter(java.io.PrintWriter)
     */
    public final void setLogWriter(
        PrintWriter logWriter
    ) throws ResourceException {
        this.logWriter = logWriter;
    }

    /**
     * Retrieve connectionURL.
     *
     * @return Returns the connectionURL.
     */
    public final String getConnectionURL() {
        return this.connectionURL;
    }
    
    /**
     * Set connectionURL.
     * 
     * @param connectionURL The connectionURL to set.
     */
    public final void setConnectionURL(
        String connectionURL
    ) {
        this.connectionURL = connectionURL;
    }
    
    /**
     * Retrieve keyStoreType.
     *
     * @return Returns the keyStoreType.
     */
    public final String getKeyStoreType() {
        return this.keyStoreType;
    }
    
    /**
     * Set keyStoreType.
     * 
     * @param keyStoreType The keyStoreType to set.
     */
    public final void setKeyStoreType(
        String keyStoreType
    ) {
        this.keyStoreType = keyStoreType;
    }
    
    /**
     * Retrieve passPhraseSeparator.
     *
     * @return Returns the passPhraseSeparator.
     */
    public final String getPassPhraseSeparator() {
        return this.passPhraseSeparator;
    }

    /**
     * Set passPhraseSeparator.
     * 
     * @param passPhraseSeparator The passPhraseSeparator to set.
     */
    public final void setPassPhraseSeparator(
        String passPhraseSeparator
    ) {
        this.passPhraseSeparator = passPhraseSeparator;
    }

    /**
     * Log and return an exception
     * 
     * @param an exception
     * 
     * @return the exception
     */
    protected final ResourceException log(
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
     * Set password.
     * 
     * @param password The password to set.
     */
    public final void setPassword(
        String password
    ) {
        this.password = password;
    }
    
    /**
     * Retrieve password.
     *
     * @return Returns the password.
     */
    protected final String getPassword() {
        return this.password;
    }    

    /**
     * Retrieve userName.
     *
     * @return Returns the userName.
     */
    public final String getUserName() {
        return this.userName;
    }
    
    /**
     * Set userName.
     * 
     * @param userName The userName to set.
     */
    public final void setUserName(
        String userName 
    ) {
        this.userName = userName;
    }

    
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /**
     * 
     * @return
     */
    private Object getIdentity(){
        if(this.identity == null) this.identity = this.connectionURL == null ?
            new Object() :
        this.userName == null && this.password == null ?
            this.connectionURL :
            new StringBuilder(
                this.connectionURL
            ).append(
                '|'
            ).append(
                this.userName == null ? "" : this.userName
            ).append(
                '|'
            ).append(
                this.password == null ? "" : this.password
            ).toString();
        return this.identity;
    }

    /**
     * Overriding equals() is required.
     */
    public boolean equals(Object that) {
        return 
            that != null && 
            this.getClass().equals(that.getClass()) &&
            this.getIdentity().equals(((AbstractManagedConnectionFactory)that).getIdentity());
    }

    /**
     * Overriding hashCode() is required.
     */
    public int hashCode(
    ) {
        return getIdentity().hashCode();
    }
    
}
