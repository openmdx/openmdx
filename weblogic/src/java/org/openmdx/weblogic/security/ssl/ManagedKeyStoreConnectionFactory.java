/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ManagedKeyStoreConnectionFactory.java,v 1.5 2007/08/13 17:33:08 hburger Exp $
 * Description: KeyStoreEntryManagedConnectionFactory 
 * Revision:    $Revision: 1.5 $
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.resource.ResourceException;
import javax.resource.spi.CommException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.openmdx.base.security.pki.AbstractManagedConnectionFactory;

/**
 * KeyStoreEntryManagedConnectionFactory
 *
 */
public class ManagedKeyStoreConnectionFactory
    extends AbstractManagedConnectionFactory
{

	/**
     * Implements <code>Serializable</code>
     */
	private static final long serialVersionUID = -4108519491921055739L;

    /**
     * 'ConnectionURL2' property
     */
    private String connectionURL2;

    /**
     * 'ProviderURL' property
     */
    private String providerURL;

    /**
     * 'InitialContextFactory' property
     */
    private String initialContextFactory;
    
    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createManagedConnection(javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    public javax.resource.spi.ManagedConnection createManagedConnection(
        Subject subject,
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        PasswordCredential credential = getCredential(subject, true);
        Map<String, String> environment = new HashMap<String, String>();
        environment.put(Context.PROVIDER_URL, getProviderURL());
        environment.put(Context.INITIAL_CONTEXT_FACTORY, this.getInitialContextFactory());
        if(this.getConnectionURL() == null) {
            return new ManagedKeyStoreConnection(
                credential,
                null, 
                null,  
                environment
             );
        } else try {
            String alias = credential.getUserName();
            char[][] passPhrases = getPassPhrases(credential);
            KeyStore keyStore = getKeyStore(
                this.getConnectionURL(),
                passPhrases[0]
            );
            X509Certificate[] certificateChain = getCertificateChain(
                keyStore.getCertificateChain(alias),
                keyStore, 
                true
            );
            Key key = keyStore.getKey(alias, passPhrases[1]);
            if(!isSelfSigned(certificateChain[certificateChain.length-1])) {
                keyStore = getKeyStore(
                    getConnectionURL2(),
                    passPhrases[2]
                );
                certificateChain = getCertificateChain(
                    certificateChain,
                    keyStore, 
                    false
                );
            }                
            return new ManagedKeyStoreConnection(
                credential,
                certificateChain,
                key, 
                environment
             );
        } catch (GeneralSecurityException exception) {
            throw (ResourceException) new ResourceException(
                "Client certificate based execution context set-up failed"
            ).initCause(
                exception
            );
        }
    }
    
    /**
     * Retrieve connectionURL2.
     *
     * @return Returns the connectionURL2.
     */
    public final String getConnectionURL2() {
        return this.connectionURL2;
    }
    
    /**
     * Set connectionURL2.
     * 
     * @param connectionURL2 The connectionURL2 to set.
     */
    public final void setConnectionURL2(
        String connectionURL2
    ) {
        this.connectionURL2 = connectionURL2;
    }
        
    /**
     * Retrieve initialContextFactory.
     *
     * @return Returns the initialContextFactory.
     */
    public String getInitialContextFactory() {
        return this.initialContextFactory;
    }

    /**
     * Set initialContextFactory.
     * 
     * @param initialContextFactory The initialContextFactory to set.
     */
    public void setInitialContextFactory(String initialContextFactory) {
        this.initialContextFactory = initialContextFactory;
    }

    /**
     * Retrieve providerURL.
     *
     * @return Returns the providerURL.
     */
    public String getProviderURL() {
        return this.providerURL;
    }

    /**
     * Set providerURL.
     * 
     * @param providerURL The providerURL to set.
     */
    public void setProviderURL(String providerURL) {
        this.providerURL = providerURL;
    }

    /**
     * Load a key store
     * 
     * @param connectionURL
     * @param passPhrase
     * 
     * @return the reqiested key store
     * 
     * @throws ResourceException
     */
    protected KeyStore getKeyStore(
        String connectionURL,
        char[] passPhrase
    ) throws ResourceException {
        String keyStoreType = getKeyStoreType();
        try {
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);   
            keyStore.load(
                new URL(connectionURL).openStream(),
                passPhrase.length == 0 ? null : passPhrase
            );
            return keyStore;
        } catch (MalformedURLException exception) {
            throw log(
                (ResourceException) new ResourceAdapterInternalException(
                    "Invalid KeyStore URL: " + connectionURL
                ).initCause(
                    exception
                )
            );
        } catch (IOException exception) {
            throw log(
                (ResourceException) new CommException(
                    "Unable to load KeyStore from " + connectionURL
                ).initCause(
                    exception
                )
            );
        } catch (GeneralSecurityException exception) {
            throw log(
                (ResourceException) new EISSystemException(
                    "Unable to load " + keyStoreType + " KeyStore from " + connectionURL
                ).initCause(
                    exception
                )
            );
        }
    }
    
    /**
     * Get an X509 Certificate Chain
     * 
     * @param source
     * @param keyStore the Key Store to be used to complete the Certificate 
     *        Chain
     * @param lenient tells whether an exception should be thrown if the
     *        resulting certificate chain is still incomplete.
     * 
     * @return the (complete or incomplete) certificate chain
     * 
     * @throws KeyStoreException 
     * @throws ArrayOutOfBoundsException if the source is empty
     * @throws NullPointerExceotion if source or keyStore are null
     */
    protected static X509Certificate[] getCertificateChain (
        Certificate[] source,
        KeyStore keyStore, 
        boolean lenient
    ) throws KeyStoreException{
        List<Certificate> chain = new ArrayList<Certificate>(Arrays.asList(source));
        Map<Principal, X509Certificate> keyStoreEntries = null;
        for(
            X509Certificate current = (X509Certificate) source[source.length-1], issuer = null;
            !isSelfSigned(current);
            current = issuer
         ){
            if(keyStoreEntries == null) {
                keyStoreEntries = new HashMap<Principal, X509Certificate>();
                for(
                    Enumeration<String> e = keyStore.aliases();
                    e.hasMoreElements();
                ){
                    String alias = (String) e.nextElement();
                    if(keyStore.isCertificateEntry(alias)) {
                        Certificate certificate = keyStore.getCertificate(alias);
                        if(certificate instanceof X509Certificate){
                            X509Certificate x509Certificate = (X509Certificate) certificate;
                            keyStoreEntries.put(
                                x509Certificate.getSubjectDN(),
                                x509Certificate
                            );
                        }
                    }
                }
            }
            issuer = (X509Certificate) keyStoreEntries.get(current.getIssuerDN());
            if(issuer == null) {
                if(lenient) break;
                throw new KeyStoreException(
                    "Certificate chain could not be completed, certificate " +
                    current.getIssuerDN() +
                    " not found in the configured key store(s)"
                );          
            }
            chain.add(issuer);
        }
        return (X509Certificate[]) chain.toArray(new X509Certificate[chain.size()]);
    }

    /**
     * Test whether an X509 Certificate Chain is self signed 
     * 
     * @param certificate the certificated to be tested
     * 
     * @return true if the prvoided certificate is self signed
     */
    protected static boolean isSelfSigned(
        X509Certificate certificate
    ){
        return certificate.getSubjectDN().equals(certificate.getIssuerDN());
    }

    
    //------------------------------------------------------------------------
    // Extends AbstractManagedConnectionFactory
    //------------------------------------------------------------------------
    
    /**
     * Overrdiding required for BEA WebLogic 9
     * 
     * @see org.openmdx.base.security.pki.AbstractManagedConnectionFactory#equals(java.lang.Object)
     */
    public boolean equals(Object that) {
        return super.equals(that);
    }

    /**
     * Overrdiding required for BEA WebLogic 9
     * 
     * @see org.openmdx.base.security.pki.AbstractManagedConnectionFactory#hashCode()
     */
    public int hashCode() {
        return super.hashCode();
    }
    
}
