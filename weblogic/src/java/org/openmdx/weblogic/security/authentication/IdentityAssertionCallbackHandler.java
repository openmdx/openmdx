/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: IdentityAssertionCallbackHandler.java,v 1.3 2006/08/20 21:56:10 hburger Exp $
 * Description: PrincipalCallbackHandler
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/08/20 21:56:10 $
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

package org.openmdx.weblogic.security.authentication;

import java.io.IOException;
import java.security.Principal;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.openmdx.kernel.security.authentication.callback.PrincipalCallback;
import org.openmdx.kernel.security.authentication.spi.GenericPrincipals;

/**
 * <code>CallbackHandler</code> to pass the token's authenticated
 * <code>Principal</code>s to the <code>LoginModule</code>.
 */
public class IdentityAssertionCallbackHandler implements CallbackHandler {

    /**
     * Constructor
     *
     * @param type
     * @param principals
     */
    public IdentityAssertionCallbackHandler(
        String type,    
        Principal[] principals
    ) {
        this.type = type;
        this.principals = principals;
    }

    /**
     * The excpected prompt; or <code>null</code> to ignore the prompt. 
     */
    private final String type;
    
    /**
     * The token's principals
     */
    private final Principal[] principals;

    /* (non-Javadoc)
     * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
     */
    public void handle(
        Callback[] callbacks
    ) throws IOException, UnsupportedCallbackException {
        for(
            int i = 0;
            i < callbacks.length;
            i++
        ){
            Callback callback = callbacks[i];
            if(callback instanceof PrincipalCallback){
                handle((PrincipalCallback)callback);
            } else if(callback instanceof NameCallback){
                handle((NameCallback)callback);
            } else throw new UnsupportedCallbackException(
                callback,
                "Unsupported callback class '" + callback.getClass() + "'"
            );
        }
    }

    /**
     * Handle a single <code>PrincipalCallback</code>
     * 
     * @param callback
     * @throws UnsupportedCallbackException  
     */
    private void handle (
        PrincipalCallback callback
    ) throws UnsupportedCallbackException {
        if(this.type == null || this.type.equals(callback.getPrompt())) {
            callback.setPrincipals(this.principals);
        } else throw new UnsupportedCallbackException(
            callback,
            "Unsupported prompt '" + callback.getPrompt() + "'"
        );
    }

    /**
     * Handle a single <code>PrincipalCallback</code>
     * 
     * @param callback
     * @throws UnsupportedCallbackException  
     */
    private void handle (
        NameCallback callback
    ) throws UnsupportedCallbackException {
        for(
            int i = 0;
            i < this.principals.length;
            i++
        ){
            Principal principal = principals[i];
            if (GenericPrincipals.isGenericUser(principal)) {
                callback.setName(principal.getName());
                return;
            }
        }
    }

}
