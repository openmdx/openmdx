/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ManagedKeyStoreConnectionFactory.java,v 1.4 2006/08/21 09:50:05 hburger Exp $
 * Description: KeyStoreEntryManagedConnectionFactory 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/08/21 09:50:05 $
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.resource.ResourceException;
import javax.resource.spi.CommException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

/**
 * KeyStoreEntryManagedConnectionFactory
 */
public class ManagedKeyStoreConnectionFactory
    extends AbstractManagedConnectionFactory
{

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -572981008975600439L;

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createManagedConnection(javax.security.auth.Subject, javax.resource.spi.ConnectionRequestInfo)
     */
    public javax.resource.spi.ManagedConnection createManagedConnection(
        Subject subject,
        ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
        PasswordCredential credential = getCredential(subject, true);
        String alias = credential.getUserName();
        char[][] passPhrases = getPassPhrases(credential);
        String keyStoreType = getKeyStoreType();
        String connectionURL = getConnectionURL();
        try {
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);   
            keyStore.load(
                new URL(connectionURL).openStream(),
                passPhrases[0].length == 0 ? null : passPhrases[0]
            );
            return new ManagedKeyStoreConnection(
                credential,
                keyStore.getCertificate(alias),
                passPhrases.length <= 1 ? null : keyStore.getKey(alias, passPhrases[1])
             );
        } catch (GeneralSecurityException exception) {
            throw log(
                (ResourceException) new EISSystemException(
                    "Unable to load " + keyStoreType + " key store from " + connectionURL
                ).initCause(
                    exception
                )
            );
        } catch (MalformedURLException exception) {
            throw log(
                (ResourceException) new InvalidPropertyException(
                    "Invalid key store URL: " + connectionURL
                ).initCause(
                    exception
                )
            );
        } catch (IOException exception) {
            throw log(
                (ResourceException) new CommException(
                    "Unable to load key store from " + connectionURL
                ).initCause(
                    exception
                )
            );
        }
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
