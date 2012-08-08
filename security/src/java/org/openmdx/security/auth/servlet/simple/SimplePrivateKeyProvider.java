/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: SimplePrivateKeyProvider.java,v 1.6 2006/08/22 16:41:36 hburger Exp $
 * Description: Simple Key Provider
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/08/22 16:41:36 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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

package org.openmdx.security.auth.servlet.simple;

import java.io.FileInputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyException;
import java.security.KeyStore;
import java.security.cert.Certificate;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.security.pki.KeyProvider;
import org.openmdx.security.auth.servlet.spi.AbstractHandler;

/**
 * Simple <code>KeyPovider</code>
 */
public class SimplePrivateKeyProvider 
    extends AbstractHandler 
    implements KeyProvider 
{
    
    //------------------------------------------------------------------------
    // Extends AbstractProvider
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.spi.AbstractHandler#init()
     */
    protected void init(
    ) throws ServletException {
        String keyStoreType = getInitParameter(
            "keystore-type",
            "JKS"
        );
        String keyStoreFileName = getInitParameter(
            "keystore-filename",
            "C:\\opt\\BEA\\weblogic92\\server\\lib\\DemoIdentity.jks"
        ); 
        String keyStorePassPhrase  = getInitParameter(
            "keystore-passphrase",
            "DemoIdentityKeyStorePassPhrase"
        );
        this.privateKeyAlias = getInitParameter(
            "private-key-alias",
            "DemoIdentity"
        );
        String privateKeyPassPhrase = getInitParameter(
            "private-key-passphrase",
            "DemoIdentityPassPhrase"
        );
        try {
            this.keyStore = KeyStore.getInstance(keyStoreType);   
            this.keyStore.load(
                new FileInputStream(keyStoreFileName),
                keyStorePassPhrase.toCharArray()
            );
            this.key = this.keyStore.getKey(
                this.privateKeyAlias,
                privateKeyPassPhrase.toCharArray()
            );
            if(isDebug()) {
                log("$Id: SimplePrivateKeyProvider.java,v 1.6 2006/08/22 16:41:36 hburger Exp $");
                log("keystore-type: " + keyStoreType);
                log("keystore-filename: " + keyStoreFileName);
                log("private-key-alias: " + this.privateKeyAlias);
                log("keystore-passphrase: n/a");
                log("private-key-passphrase: n/a");
            }
        } catch (Exception exception) {
            throw (UnavailableException) Throwables.initCause(
                new UnavailableException(
                    "Private key provider acquisition failed"
                ),
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("info", "$Id: SimplePrivateKeyProvider.java,v 1.6 2006/08/22 16:41:36 hburger Exp $"),
                    new BasicException.Parameter("name", getServletName()),
                    new BasicException.Parameter("keystore-type", keyStoreType),
                    new BasicException.Parameter("keystore-passphrase", "n/a"),
                    new BasicException.Parameter("private-key-alias", getAlias()),
                    new BasicException.Parameter("private-key-passphrase", "n/a")
                }, null
            );
        }
    }

    
    //------------------------------------------------------------------------
    // Implements KeyProvider
    //------------------------------------------------------------------------
    
    /**
     * The <code>Key</code>'s alias
     */
    private String privateKeyAlias;

    /**
     * This provider's <code>KeyStore</code>
     */
    private KeyStore keyStore;

    /**
     * This provider's <code>Key</code>'
     */
    private Key key;

    /* (non-Javadoc)
     * @see org.openmdx.kernel.security.pki.KeyProvider#getAlias()
     */
    public String getAlias() {
        return this.privateKeyAlias;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.security.pki.KeyProvider#getKey()
     */
    public Key getKey() throws KeyException {
        return this.key;
    }

	public Certificate getCertificate() throws GeneralSecurityException {
		return this.keyStore.getCertificate(getAlias());
	}

	
	//------------------------------------------------------------------------
	// Implements Connection
	//------------------------------------------------------------------------

	public void close() {
		this.key = null;
		this.keyStore = null;
	}


}
