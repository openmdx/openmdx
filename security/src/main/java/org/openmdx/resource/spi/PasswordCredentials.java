/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: Password Credentials
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.resource.spi;

import java.util.Arrays;

#if JAVA_8
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
#else
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.security.PasswordCredential;
#endif
import javax.security.auth.Subject;

import org.openmdx.resource.cci.AuthenticationInfo;

public class PasswordCredentials {

    private PasswordCredentials() {
        // Avoid instantiation
    }

    /**
     * An empty password
     */
    private static final char[] EMPTY_PASSWORD = {};
    
    /**
     * An empty user name
     */
    private static final String EMPTY_USER_NAME = "";    
    
    /**
     * Extract the password credentials for a given managed connection factory
     * 
     * @param target the target of the credentials
     * @param subject the subject holding the credentials
     * 
     * @return a matching credential, or {@code null}
     */
    public static PasswordCredential getPasswordCredential(
        final ManagedConnectionFactory target,
        final Subject subject
    ) {
        if(subject != null) {
            for(PasswordCredential credential : subject.getPrivateCredentials(PasswordCredential.class)) {
                if(credential.getManagedConnectionFactory() == target) {
                    return credential;
                }
            }
        }
        return null;
    }
    
    /**
     * Create a password credential
     * 
     * @param target the managed connection factory for which it is meant
     * @param userName the EIS username
     * @param password the EIS password
     * 
     * @return a new password credential
     */
    public static PasswordCredential newPasswordCredential(
        final ManagedConnectionFactory target,
        final String userName,
        final char[] password
    ) {
        final PasswordCredential credential = new PasswordCredential(
            userName == null ? EMPTY_USER_NAME : userName,
            password == null ? EMPTY_PASSWORD : password
        );
        credential.setManagedConnectionFactory(target);
        return credential;
    }

    /**
     * Create a password credential
     * 
     * @param target the managed connection factory for which it is meant
     * @param userName the EIS username
     * @param password the EIS password
     * 
     * @return a new password credential
     */
    public static PasswordCredential newPasswordCredential(
        final ManagedConnectionFactory target,
        final String userName,
        final String password
    ) {
        return newPasswordCredential(
            target,
            userName,
            password == null ? null : password.toCharArray()
        );
    }
    
    /**
     * Create a password credential
     * 
     * @param target the managed connection factory for which it is meant
     * @param authenticationInfo the authentication information
     * @return a new password credential
     */
    public static PasswordCredential newPasswordCredential(
        final ManagedConnectionFactory target,
        final AuthenticationInfo authenticationInfo
    ) {
        return newPasswordCredential(
            target,
            authenticationInfo.getUserName(),
            authenticationInfo.getPassword()
        );
    }

    public static void destroy(PasswordCredential passwordCredential) {
        final char[] password = passwordCredential.getPassword();
        for(int i = 0; i < password.length; i++) {
            password[i] = Character.MIN_VALUE;
        }
    }

    /**
     * Tells whether the password is empty
     * 
     * @param passwordCredential the credential
     * 
     * @return {@code true} if the password is empty
     */
    public static boolean isPasswordEmpty(
        PasswordCredential passwordCredential
    ) {
        return Arrays.equals(EMPTY_PASSWORD, passwordCredential.getPassword());
    }

}
