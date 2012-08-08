/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractHostnameVerifier.java,v 1.4 2007/10/10 16:05:53 hburger Exp $
 * Description: Abstract Hostname Verifier
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:05:53 $
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
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;

import org.openmdx.base.text.pattern.RegularExpression;
import org.openmdx.base.text.pattern.cci.Matcher_1_0;
import org.openmdx.base.text.pattern.cci.Pattern_1_0;


/**
 * Abstract Hostname Verifier
 * <p>
 * Accepts for example a certificate with the common name "*.openmdx.org"
 * to access "any-sub-domain.openmdx.org".
 */
public class AbstractHostnameVerifier
    implements HostnameVerifier 
{

    /**
     * Constructor
     */
    protected AbstractHostnameVerifier() {
        super();
    }

    
    //------------------------------------------------------------------------
    // Implements HostnameVerifier
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String, javax.net.ssl.SSLSession)
     */
    public boolean verify(
        String hostName, 
        SSLSession sslSession
    ){
        try {
            X509Certificate certificate = sslSession.getPeerCertificateChain()[0];
            String distinguishedName = certificate.getSubjectDN().getName();            
            String commonName = getCommonName(distinguishedName);
            return matches(hostName, commonName);
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * Tests whether the host name matches a given wildcard common name
     * <p>
     * This method is usually overridden by a subclass as JSSE does not 
     * call the HostnameVerifier interceptor if the given expression
     * evaluates to true.
     *  
     * @param hostName
     * @param commonName
     * 
     * @return true if the host name matches the given common name
     */
    protected boolean matches(
        String hostName,
        String commonName
    ){
        int i;
        return
            hostName.equals(commonName) ||
            (i = hostName.indexOf('.')) > 0 &&
            commonName.equals('*' + hostName.substring(i));
    }
    
    /**
     * Retrieves the common name from an X.500 principal
     * 
     * @param principal
     * 
     * @return the principal's common name
     */
    protected static String getCommonName(
        String distinguishedName
    ){
        Matcher_1_0 commonNameMatcher = commonNamePattern.matcher(distinguishedName);
        return commonNameMatcher.matches() ? commonNameMatcher.group(2) : null;
    }
    
    private static final Pattern_1_0 commonNamePattern = RegularExpression.compile(
        "(.* )?CN=([^,]*)(,.*)?"
    );
    
}
