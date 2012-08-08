/*
 * ====================================================================
 * Project:     OMEX/Security, http://www.omex.ch/
 * Name:        $Id: IdentityAssertionProvider.java,v 1.17 2007/08/13 17:33:08 hburger Exp $
 * Description: Perimeter Authentication Provider
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/08/13 17:33:08 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2006, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.weblogic.security.authentication;

import java.net.URL;
import java.security.KeyStore;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.security.authentication.spi.GenericPrincipals;
import org.openmdx.kernel.security.token.TokenException;
import org.openmdx.kernel.security.token.TokenValidator;

import weblogic.management.security.ProviderMBean;
import weblogic.security.provider.PrincipalValidatorImpl;
import weblogic.security.spi.AuthenticationProvider;
import weblogic.security.spi.IdentityAsserter;
import weblogic.security.spi.IdentityAssertionException;
import weblogic.security.spi.PrincipalValidator;
import weblogic.security.spi.SecurityProvider;
import weblogic.security.spi.SecurityServices;

/**
 * The Identity Asserter relies on openMDX/Security Tokens.
 * <p>
 * It is itself an <code>IdentityAsserter</code>.
 */
public final class IdentityAssertionProvider 
	implements AuthenticationProvider, IdentityAsserter 
{
    
    /**
     * A description of this provider
     */
	private String description;
	
    /**
     * The token validator
     */ 
    private TokenValidator tokenValidator;
    
    /**
     * Set in case of initialization failures
     */
    private BasicException status;
    
    //------------------------------------------------------------------------
    // Implements AuthenticationProvider
    //------------------------------------------------------------------------

    /**
     * 
     */
    private final static Map<String, Object> ASSERTION_MODULE_OPTIONS = Collections.emptyMap();
    
    /**
     * 
     */
    private final static AppConfigurationEntry ASSERTION_MODULE_CONFIGURATION = new AppConfigurationEntry(
        IdentityAssertionLoginModule.class.getName(),
        LoginModuleControlFlag.SUFFICIENT,
        ASSERTION_MODULE_OPTIONS
    );
    
    /**
     * Initialize the <code>AuthenticationProvider</code>.
     *
     * @param mbean A ProviderMBean that holds the 
     * <code>IdentityAssertionProvider</code>'s configuration data. 
     * This mbean must be an instance of 
     * <code>AirLockIdentityAsserterMBean</code>.
     *
     * @param services The <code>SecurityServices</code> gives access to the 
     * auditor so that the provider can post audit events.
     * The Identity Asserter doesn't use this parameter.
     *
     * @see SecurityProvider
     */
    public void initialize(
        ProviderMBean mbean, 
        SecurityServices services
    ) {
        //
        // Cast the mbean from a generic ProviderMBean to a AirLockIdentityAsserterMBean.
        //
        AirLockIdentityAsserterMBean configuration = (AirLockIdentityAsserterMBean)mbean;
        //
        // Set the description to the Identity Asserter's mbean's description and version
        //
        this.description = configuration.getDescription() + "\n" + configuration.getVersion();
        //
        // Acquire the <code>TokenValidator</code>
        //
        String tokenKeyStoreURL = configuration.getTokenKeyStoreURL();
        String tokenKeyStorePassPhrase = configuration.getTokenKeyStorePassPhrase();
        boolean hasTokenKeyStorePassPhrase = tokenKeyStorePassPhrase != null && tokenKeyStorePassPhrase.length() > 0; 
        String tokenKeyAlias = configuration.getTokenKeyAlias();
        try {
            KeyStore keyStore = KeyStore.getInstance(configuration.getTokenKeyStoreType());   
            keyStore.load(
                new URL(tokenKeyStoreURL).openStream(),
                hasTokenKeyStorePassPhrase ? tokenKeyStorePassPhrase.toCharArray() : null
            );
            this.tokenValidator = new TokenValidator(
                keyStore.getCertificate(tokenKeyAlias).getPublicKey()
            );
        } catch (Exception exception) {
            this.tokenValidator = null;
            new BasicException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("description", this.description),
                    new BasicException.Parameter("tokenKeyStoreURL", tokenKeyStoreURL),
                    new BasicException.Parameter("tokenKeyAlias", tokenKeyAlias),
                    new BasicException.Parameter("tokenKeyStorePassPhrase", hasTokenKeyStorePassPhrase)
                },
                "Token validator acquisition faild"
            ).printStackTrace(
                System.err
            );
        }
    }

    /**
     * Get the Identity Asserter's description.
     *
     * @return A String containing a brief description of the Identity Asserter.
     *
     * @see SecurityProvider
     */
    public String getDescription(
    ) {
        return this.description;
    }

    /**
     * Shutdown the <code>IdentityAssertionProvider</code>.
     *
     * @see SecurityProvider
     */
    public void shutdown(
    ) {
        this.tokenValidator = null;
    }

    /**
     * Create a JAAS AppConfigurationEntry (which tells JAAS
     * how to create the login module and how to use it) when
     * the Identity Asserter is used to authenticate (vs. to
     * complete identity assertion).
     *
     * @return An AppConfigurationEntry that tells JAAS how to use the 
     * Identity Asserter's login module for authentication.
     */
    public AppConfigurationEntry getLoginModuleConfiguration(
    ){
        return null;
    }

    /**
     * Create a JAAS AppConfigurationEntry (which tells JAAS
     * how to create the login module and how to use it) when
     * the Identity Asserter is used to complete identity
     * assertion (vs. to authenticate).
     *
     * @return An AppConfigurationEntry that tells JAAS how to use the 
     * Identity Asserter's login module for identity assertion.
     */
    public AppConfigurationEntry getAssertionModuleConfiguration(
    ) {
        return ASSERTION_MODULE_CONFIGURATION;
    }
    
    /**
     * Return the <code>PrincipalValidator</code> that can validate the
     * <code>Principal</code>s that the authenticator's 
     * <code>LoginModule</code> puts into the <code>Subject</code>.
     *
     * @return A <code>null</code> <code>PrincipalValidator</code> since the 
     * Identity Asserter is not an authenticator (thus doesn't put 
     * principals into the subject).
     */
    public PrincipalValidator getPrincipalValidator(
    ){
        return new PrincipalValidatorImpl();
    }

    /**
     * Returns this providers identity asserter object.
     *
     * @return a <code>TokenIdentityAsserter</code>
     */
    public IdentityAsserter getIdentityAsserter(
    ) {
        return this;
    }
    
    
    //------------------------------------------------------------------------
    // Implements TokenIdentityAsserter
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see weblogic.security.spi.IdentityAsserter#assertIdentity(java.lang.String, java.lang.Object)
     */
    public CallbackHandler assertIdentity(
        String type, 
        Object token
    ) throws IdentityAssertionException {
        if(this.tokenValidator == null) {
            throw (IdentityAssertionException) new IdentityAssertionException(
                "Configuration failure, token key validator missing"
            ).initCause(
                this.status
            );
        } else if(type == null) {
            throw new IdentityAssertionException(
                "Type is null"
            );  
        } else if (!GenericPrincipals.TOKEN.equals(type)) {
            throw new IdentityAssertionException(
                "Token must be of type " + GenericPrincipals.TOKEN + ": " + type
            );  
        } else if(token == null) {
            throw new IdentityAssertionException(
                "Token is null"
            );  
        } else if (!(token instanceof byte[])) {
            throw new IdentityAssertionException(
                "Token must be an instance of byte[]: " + token.getClass().getName()
            ); 
        } try {
            return new IdentityAssertionCallbackHandler(
                type,
                (Principal[]) this.tokenValidator.getValue((byte[])token)
            );
        } catch (TokenException exception) {
            throw (IdentityAssertionException) new IdentityAssertionException(
                "Token validation failed: " + exception.getMessage()
            ).initCause(
                exception
            );
        }
    }

}
