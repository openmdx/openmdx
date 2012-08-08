/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CertificateConnection.java,v 1.1 2009/03/11 16:04:22 hburger Exp $
 * Description: CertificateConnection 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/11 16:04:22 $
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

package org.openmdx.resource.pki.keystore;

import java.security.GeneralSecurityException;
import java.security.cert.Certificate;

import org.openmdx.resource.pki.cci.CertificateProvider;

/**
 * CertificateConnection
 */
public class CertificateConnection
    implements CertificateProvider
{

    /**
     * 
     */
    private String alias;
    
    /**
     * 
     */
    private Certificate certificate;
    
    
    //------------------------------------------------------------------------
    // Implements CertificateProvider
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.kernel.security.pki.CertificateProvider#getAlias()
     */
    public String getAlias() {
        return this.alias;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.security.pki.CertificateProvider#getCertificate()
     */
    public Certificate getCertificate(
    ) throws GeneralSecurityException {
        return this.certificate;
    }

    /**
     * Set alias.
     * 
     * @param alias The alias to set.
     */
    void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Set certificate.
     * 
     * @param certificate The certificate to set.
     */
    void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    
    //------------------------------------------------------------------------
    // Implements Connection
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.security.resource.Connection#close()
     */
    public void close(
    ) {
        this.certificate = null;
    }

}
