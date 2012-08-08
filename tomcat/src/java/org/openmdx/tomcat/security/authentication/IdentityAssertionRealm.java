/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: IdentityAssertionRealm.java,v 1.4 2008/01/12 11:28:30 hburger Exp $
 * Description: Tomcat Identity Assertion Realm
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/12 11:28:30 $
 * ====================================================================
 *
 * Copyright (c) 2005, OMEX AG, Switzerland
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openmdx.tomcat.security.authentication;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.realm.RealmBase;
import org.apache.catalina.util.StringManager;
import org.openmdx.kernel.security.authentication.spi.GenericPrincipals;
import org.openmdx.kernel.security.token.TokenException;
import org.openmdx.kernel.security.token.TokenValidator;

/**
 * This Tomcat <code>Realm</code> is used for perimeter authentication.
 */
public class IdentityAssertionRealm extends RealmBase {

    /**
     * Descriptive information about this <code>Realm</code> implementation.
     */
    protected static final String info = IdentityAssertionRealm.class.getName() + "/1.0";

    /**
     * Descriptive information about this <code>Realm</code> implementation.
     */
    protected static final String name = "TokenRealm";

    /**
     * The <code>ToekValidator</code> is acquired during start().
     */
    protected TokenValidator tokenValidator;

    /**
     * The <code>KeyStore</code>'s type
     */
    private String keyStoreType = "JKS";
    
    /**
     * The <code>KeyStore</code>'s file name
     */
    private String keyStoreFileName = "C:\\opt\\BEA\\weblogic92\\server\\lib\\DemoIdentity.jks";
    
    /**
     * The <code>KeyStore</code>'s pass phrase
     */
    private String keyStorePassPhrase = "DemoIdentityKeyStorePassPhrase";
    
    /**
     * The public key's alias
     */
    private String alias = "DemoIdentity";
    
    /**
     * The string manager for this package.
     */
    protected static StringManager sm = StringManager.getManager(Constants.Package);

    /**
     * Retrieve the alias.
     * 
     * @return the <code>alias</code>'s value
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * Set the alias
     *
     * @param alias The <code>alias</code>'s value
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Retrieve the keyStoreFileName.
     * 
     * @return the <code>keyStoreFileName</code>'s value
     */
    public String getKeyStoreFileName() {
        return this.keyStoreFileName;
    }

    /**
     * Set the keyStoreFileName
     *
     * @param keyStoreFileName The <code>keyStoreFileName</code>'s value
     */
    public void setKeyStoreFileName(String keyStoreFileName) {
        this.keyStoreFileName = keyStoreFileName;
    }

    /**
     * Retrieve the keyStorePassPhrase.
     * 
     * @return the <code>keyStorePassPhrase</code>'s value
     */
    public String getKeyStorePassPhrase() {
        return this.keyStorePassPhrase;
    }

    /**
     * Set the keyStorePassPhrase
     *
     * @param keyStorePassPhrase The <code>keyStorePassPhrase</code>'s value
     */
    public void setKeyStorePassPhrase(String keyStorePassPhrase) {
        this.keyStorePassPhrase = keyStorePassPhrase;
    }

    /**
     * Retrieve the keyStoreType.
     * 
     * @return the <code>keyStoreType</code>'s value
     */
    public String getKeyStoreType() {
        return this.keyStoreType;
    }

    /**
     * Set the keyStoreType
     *
     * @param keyStoreType The <code>keyStoreType</code>'s value
     */
    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    
    //------------------------------------------------------------------------
    // Extends RealmBase
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.apache.catalina.realm.RealmBase#getName()
     */
    protected String getName() {
        return IdentityAssertionRealm.name;
    }

    /* (non-Javadoc)
     * @see org.apache.catalina.realm.RealmBase#getPassword(java.lang.String)
     */
    protected String getPassword(String username) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.catalina.realm.RealmBase#getPrincipal(java.lang.String)
     */
    protected Principal getPrincipal(String username) {
        return null;
    }

    
    //------------------------------------------------------------------------
    // Implements Realm
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.apache.catalina.realm.RealmBase#authenticate(java.lang.String, byte[])
     */
    public Principal authenticate(
         String username, 
         byte[] credentials
    ){
        try {
            Principal[] principals = (Principal[]) this.tokenValidator.getValue(credentials);
            List<String> roles = new ArrayList<String>();
            String name = null;
            for(
                int i = 0;
                i < principals.length;
                i++
            ) if (GenericPrincipals.isGenericUser(principals[i])) {
                name = principals[i].getName();
            } else if (GenericPrincipals.isGenericGroup(principals[i])) {
                roles.add(principals[i].getName());
            }
            return new org.apache.catalina.realm.GenericPrincipal(
            	 this,
                 name,
                 null, // password
                 roles
            );
        } catch (TokenException e) {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.catalina.realm.RealmBase#authenticate(java.lang.String, java.lang.String)
     */
    public Principal authenticate(
        String username, 
        String credentials
    ){
        return authenticate(username, Base64.decode(credentials));
    }

    /* (non-Javadoc)
     * @see org.apache.catalina.realm.RealmBase#hasRole(java.security.Principal, java.lang.String)
     */
    public boolean hasRole(Principal principal, String role) {
        return ((org.apache.catalina.realm.GenericPrincipal)principal).hasRole(role);
    }
    
    
    //------------------------------------------------------------------------
    // Extends Lifecycle
    //------------------------------------------------------------------------
    
    /**
     * Get a specific Key Store
     * 
     * @param keyStoreType
     * @param keyStoreFileName
     * @param keyStorePassPhrase
     * 
     * @return the initialized key store
     * 
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     */
    protected static KeyStore getKeyStore(
        String keyStoreType,
        String keyStoreFileName,
        String keyStorePassPhrase
    ) throws LifecycleException{
        try {
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);   
            keyStore.load(
                new FileInputStream(keyStoreFileName),
                keyStorePassPhrase.toCharArray()
            );
            return keyStore;
        } catch (Exception exception) {
            throw new LifecycleException(
                sm.getString("authenticator.keystore"),
                exception
            );
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.catalina.realm.RealmBase#start()
     */
    public void start() throws LifecycleException {
        super.start();
        KeyStore keyStore = getKeyStore(
            getKeyStoreType(),
            getKeyStoreFileName(),
            getKeyStorePassPhrase()             
        );
        try {
            this.tokenValidator = new TokenValidator(
                keyStore.getCertificate(
                    getAlias()
                ).getPublicKey()
            );
        } catch (KeyStoreException exception) { 
            throw new LifecycleException(
                "Public key acquisition failed",
                exception
            );
        }
    }

    /* (non-Javadoc)
     * @see org.apache.catalina.realm.RealmBase#stop()
     */
    public void stop() throws LifecycleException {
        super.stop();
        this.tokenValidator = null;
    }

}
