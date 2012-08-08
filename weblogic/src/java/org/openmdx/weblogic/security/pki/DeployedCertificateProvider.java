/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DeployedCertificateProvider.java,v 1.2 2009/07/06 11:14:48 hburger Exp $
 * Description: DeployedKeyProvider 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/07/06 11:14:48 $
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */

package org.openmdx.weblogic.security.pki;

import java.security.GeneralSecurityException;
import java.security.cert.Certificate;

import javax.naming.NamingException;

import org.openmdx.resource.pki.cci.CertificateProvider;

/**
 * Deployed Key Provider
 */
public class DeployedCertificateProvider
    extends AbstractProvider
    implements CertificateProvider
{

    /**
     * Constructor
     *  
     * @throws NamingException 
     */
    public DeployedCertificateProvider() throws NamingException {
        super();
    }

    /**
     * Constructor
     *  
     * @throws NamingException 
     */
    public DeployedCertificateProvider(
        String jndiName
    ) throws NamingException {
        super(jndiName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.security.pki.CertificateProvider#getAlias()
     */
    public String getAlias(
    ) throws GeneralSecurityException {
        return ((CertificateProvider)super.getDelegate()).getAlias();
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.security.pki.CertificateProvider#getCertificate()
     */
    public Certificate getCertificate(
    ) throws GeneralSecurityException {
        return ((CertificateProvider)super.getDelegate()).getCertificate();
    }

}
