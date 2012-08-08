/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: FakeLoginModule.java,v 1.2 2010/03/05 13:21:20 hburger Exp $
 * Description: FakeLoginModule
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/05 13:21:20 $
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

package org.openmdx.test.security.auth;

import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import test.openmdx.security.auth.GenericPrincipal;

/**
 * 
 */
public class FakeLoginModule implements LoginModule {

    /**
     * Constructor
     */
    public FakeLoginModule() {
        super();
    }
    
    protected Subject subject;
    protected CallbackHandler callbackHandler;
    protected Map<String,?> sharedState; 
    protected Map<String,?> options;
    private Set<Principal> principals;

    /**
     * A generic principal
     */
    public static final String PRINCIPAL_TYPE = "org:openmdx:security:identity1:Principal";
    
    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#abort()
     */
    public boolean abort() throws LoginException {
        this.principals.clear();
        return true;
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#commit()
     */
    public boolean commit() throws LoginException {
        this.subject.getPrincipals().addAll(this.principals);
        return true;
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#login()
     */
    public boolean login() throws LoginException {
        NameCallback nameCallback = new NameCallback("Name");
        try {
            this.callbackHandler.handle(
                new Callback[]{nameCallback}
            );
        } 
        catch (IOException exception) {
            throw (LoginException) new FailedLoginException(
                 "Name retrieval failed"
            ).initCause(
                 exception
            );
        } 
        catch (UnsupportedCallbackException exception) {
            throw (LoginException) new FailedLoginException(
                    "Name retrieval not supported"
               ).initCause(
                    exception
               );
        }
        PasswordCallback passwordCallback = this.createPasswordCallback(
            nameCallback.getName()
        );
        try {
            this.callbackHandler.handle(
                new Callback[]{passwordCallback}
            );
        } 
        catch (IOException exception) {
            throw (LoginException) new FailedLoginException(
                "Password retrieval failed"
            ).initCause(
                exception
            );
        } 
        catch (UnsupportedCallbackException exception) {
            throw (LoginException) new FailedLoginException(
                "Password retrieval not supported"
            ).initCause(
                exception
            );
        }
        this.checkPassword(
            nameCallback.getName(),
            new String(passwordCallback.getPassword())
        );      
        this.principals.add(
            new GenericPrincipal(
                FakeLoginModule.PRINCIPAL_TYPE,
                "ch::omex::mdx::compatibility::security1/provider/ch::omex/segment/ch::omex::mdx::test/subject/"+ nameCallback.getName() + "/role/SingleSignOn", 
                nameCallback.getName()
            )
        );
        return true;
    }

    /**
     * 
     */
    private  PasswordCallback createPasswordCallback(
        String name
    ) throws LoginException {
        int tag = name.length();
        switch(tag){
            case 3:
                try {
                    String pin = String.valueOf(
                        1000000 + Integer.parseInt(name, Character.MAX_RADIX)
                    );
                    return new PasswordCallback(
                        "Challenge: " + pin.substring(0, 1) + ' ' + pin.substring(1,3) + ' ' + pin.substring(3) + " Response? ",
                        true
                    );
                } 
                catch (NumberFormatException exception) {
                    throw new FailedLoginException(
                        "User '" + name + "' with name length " + tag + " does not belong to realm"
                    ); 
                }
            case 4: 
                return new PasswordCallback(
                    "Password for user '" + name + "'",
                    false
                );
            default:
                throw new FailedLoginException(
                    "User '" + name + "' with name length " + tag + " does not belong to realm"
                ); 
        }
    }

    private  void checkPassword(
        String name,
        String password
    ) throws LoginException {
        int tag = name.length();
        switch(tag){
            case 3: 
                if(
                    password.equals(
                        String.valueOf(1000000 + Integer.parseInt(name, Character.MAX_RADIX))
                    )
                 ) return;
                 break;
            case 4: 
                if(password.equals(name.toUpperCase()))return;
                break;
        }
        throw new FailedLoginException(
            "User '" + name + "' can't be authenticated with password '" + password + "'"
        ); 
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#logout()
     */
    public boolean logout() throws LoginException {
        this.subject.getPrincipals().removeAll(this.principals);
        this.principals.clear();
        return true;
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
     */
    public void initialize(
        Subject subject, 
        CallbackHandler callbackHandler,
        Map<String,?> sharedState, 
        Map<String,?> options
    ) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
        this.principals = new HashSet<Principal>();
    }

}
