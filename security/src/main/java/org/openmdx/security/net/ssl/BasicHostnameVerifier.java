/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: Basic Host Name Verifier
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.security.net.ssl;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLSession;
import javax.net.ssl.HostnameVerifier;

/**
 * The basic host name verifier does not support wildcard certificates.
 */
public class BasicHostnameVerifier implements HostnameVerifier {

	/**
	 * String form according to RFC 4514
	 */
    private static final Pattern commonNamePattern = Pattern.compile(
        "(?:^|,)\\s*[Cc][Nn]\\s*=\\s*((?:\\\\,|[^,])+)\\s*(?:,|$)"
    );
    
    /**
     * Escape sequences according to RFC 4515
     */
    private static final Pattern escapePattern =  Pattern.compile(
        "([^\\\\]*)(\\\\[\"+,;<>\\\\])?((?:\\\\[0-9A-Fa-f][0-9A-Fa-f])+)?"
    );
    
	@Override
	public boolean verify(
		String hostname,
		SSLSession session
	) {
		try {
			Certificate[] certs = session.getPeerCertificates();
			if (certs.length == 0) {
				return false;
			}
			X509Certificate x509 = (X509Certificate) certs[0];
			String subjectDN = x509.getSubjectX500Principal().getName();
			return verify(hostname, subjectDN);
		} catch (Exception ignored) {
			return false;
		}
	}

	/**
	 * Verify a hostname against a distinguished name
	 * 
	 * @param hostName the host name to be verified
	 * @param distinguishedName the distinguished name
	 * 
	 * @return {@code true} in case of success
	 */
	protected boolean verify(
		String hostName,
		String distinguishedName
	){
        for(
        	Matcher commonNameMatcher = commonNamePattern.matcher(distinguishedName);
        	commonNameMatcher.find();
         ) {
        	String commonName = commonNameMatcher.group(1).trim(); 
        	if(commonName.indexOf('\\') >= 0) {
        		StringBuilder commonNameBuilder = new StringBuilder();
        		for(
        			Matcher escapeMatcher = escapePattern.matcher(commonName);
        			escapeMatcher.find();
        		){
        			commonNameBuilder.append(escapeMatcher.group(1));
        			String characterEscape = escapeMatcher.group(2);
        			if(characterEscape != null) {
        				commonNameBuilder.append(characterEscape.charAt(1));
        			}
        			String utf8Escape = escapeMatcher.group(3);
        			if(utf8Escape != null) {
        				byte[] utf8 = new byte[utf8Escape.length() / 3];
        				for(
        					int i = 0;
        					i < utf8.length;
        					i++
        				){
        					utf8[i] = (byte) Integer.parseInt(utf8Escape.substring(i * 3 + 1, (i + 1) * 3), 0x10);
        				}
                        commonNameBuilder.append(new String(utf8, StandardCharsets.UTF_8));
                    }
        		}
        		commonName = commonNameBuilder.toString();
        	}
        	if(matches(hostName, commonName)) return true;
        }
        return false;
	}
	
	/**
	 * Tests whether the host name matches the comment name
	 * 
	 * @param hostName the host name to be compared
	 * @param commonName the common name to be compared
	 * 
	 * @return {@code true} in case of success
	 */
	protected boolean matches(
        String hostName,
        String commonName
    ){
		return hostName.equalsIgnoreCase(commonName);
	}
    
}
