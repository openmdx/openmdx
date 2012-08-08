/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LenientHostnameVerifier.java,v 1.4 2005/03/06 19:02:01 hburger Exp $
 * Description: Lenient Hostname Verifier
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/03/06 19:02:01 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.base.net.ssl;

import javax.net.ssl.HostnameVerifier;

/**
 * Lenient Hostname Verifier
 * <p>
 * Accepts for example a certificate with the common name "*.openmdx.org"
 * to access "any.level.of.sub-domains.openmdx.org".
 */
public class LenientHostnameVerifier
    extends AbstractHostnameVerifier 
{

    /**
     * Constructor
     */
    protected LenientHostnameVerifier() {
        super();
    }

    /**
     * Return an instance of WildcardCertificateAwareHostnameVerifier
     * 
     * @return the pre-allocated instance
     */
    public static HostnameVerifier getInstance(
    ){
        return LenientHostnameVerifier.instance;
    }

    /**
     * The pre-allocated instance
     */
    private static HostnameVerifier instance = new LenientHostnameVerifier();

    /* (non-Javadoc)
     * @see com.lgt.uses.org.openmdx.base.net.WildcardCertificateAwareHostnameVerifier#matches(java.lang.String, java.lang.String)
     */
    protected boolean matches(String hostName, String commonName) {
//      System.out.println("verifying host name " + hostName + " against common name " + commonName);
        return 
            commonName.startsWith("*.") &&
            hostName.endsWith(commonName.substring(1));
    }

}
