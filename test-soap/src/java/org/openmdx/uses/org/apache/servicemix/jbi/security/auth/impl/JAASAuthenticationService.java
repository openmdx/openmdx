/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openmdx.uses.org.apache.servicemix.jbi.security.auth.impl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmdx.uses.org.apache.servicemix.jbi.security.auth.AuthenticationService;
import org.openmdx.uses.org.apache.servicemix.jbi.security.login.CertificateCallback;

/**
 * Implementation of the authentication service using JAAS. 
 *  
 * @org.apache.xbean.XBean element="authenticationService"
 */
public class JAASAuthenticationService implements AuthenticationService {

    private static final Log log = LogFactory.getLog(JAASAuthenticationService.class);
    
    public void authenticate(Subject subject,
                             String domain,
                             final String user, 
                             final Object credentials) throws GeneralSecurityException {
        if (log.isDebugEnabled()) {
            log.debug("Authenticating '" + user + "' with '" + credentials + "'");
        }
        LoginContext loginContext = new LoginContext(domain, subject, new CallbackHandler() {
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                for (int i = 0; i < callbacks.length; i++) {
                    if (callbacks[i] instanceof NameCallback) {
                        ((NameCallback) callbacks[i]).setName(user);
                    } else if (callbacks[i] instanceof PasswordCallback && credentials instanceof String) {
                        ((PasswordCallback) callbacks[i]).setPassword(((String) credentials).toCharArray());
                    } else if (callbacks[i] instanceof CertificateCallback && credentials instanceof X509Certificate) {
                        ((CertificateCallback) callbacks[i]).setCertificate((X509Certificate) credentials);
                    } else {
                        throw new UnsupportedCallbackException(callbacks[i]);
                    }
                }
            }
        });
        loginContext.login();
    }

}
