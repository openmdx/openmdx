/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: IdentityAssertionLoginModule.java,v 1.5 2007/08/20 09:37:19 hburger Exp $
 * Description: Perimeter Login Module
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/08/20 09:37:19 $
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */

package org.openmdx.weblogic.security.authentication;

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.openmdx.kernel.security.authentication.callback.PrincipalCallback;
import org.openmdx.kernel.security.authentication.spi.GenericPrincipal;
import org.openmdx.kernel.security.authentication.spi.GenericPrincipals;
import org.openmdx.weblogic.security.realm.WebLogicGroup;
import org.openmdx.weblogic.security.realm.WebLogicPrincipal;
import org.openmdx.weblogic.security.realm.WebLogicUser;

/**
 * Perimeter <code>LoginModule</code>
 */
public class IdentityAssertionLoginModule implements LoginModule {

    /**
     * 
     */
    private Subject subject;

    /**
     * 
     */
    private CallbackHandler callbackHandler;

    /**
     * 
     */
    private Set<Principal> genericPrincipals;

    /**
     * 
     */
    private Set<WebLogicPrincipal> weblogicPrincipals = new HashSet<WebLogicPrincipal>();

    /**
     * 
     */
    private boolean active;

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#abort()
     */
    public boolean abort() throws LoginException {
        if(this.active) {
            this.weblogicPrincipals.clear();
            this.genericPrincipals = Collections.emptySet();
        }
        return this.active;
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#commit()
     */
    public boolean commit() throws LoginException {
        if(this.active) {
            for(
                Iterator<Principal> i = this.genericPrincipals.iterator();
                i.hasNext();
            ) {
                Principal p = (Principal) i.next();
                if(GenericPrincipals.isGenericUser(p)) {
                    GenericPrincipal g = (GenericPrincipal) p;
                    this.weblogicPrincipals.add(
                        new WebLogicUser(
                            g.getIdentity(),
                            g.getName()
                        )
                    );
                } else if(GenericPrincipals.isGenericGroup(p)) {
                    GenericPrincipal g = (GenericPrincipal) p;
                    this.weblogicPrincipals.add(
                        new WebLogicGroup(
                            g.getIdentity(),
                            g.getName()
                        )
                    );
                }
            }
            Set<Principal> target = this.subject.getPrincipals();
            this.weblogicPrincipals.removeAll(target);
            target.addAll(this.weblogicPrincipals);
        }
        return this.active;
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
        this.active = true;
    }

    /* (non-Javadoc)
     * @see javax.security.auth.spi.LoginModule#login()
     */
    @SuppressWarnings("unchecked")
    public boolean login() throws LoginException {
        try {
            PrincipalCallback[] callbacks = new PrincipalCallback[]{
                new PrincipalCallback(GenericPrincipals.TOKEN)
            };
            this.callbackHandler.handle(callbacks);
            this.genericPrincipals = callbacks[0].getPrincipals();
            this.active = true;
        } catch (Exception exception) {
            throw (LoginException) new LoginException(
                "Could not retrieve '" + GenericPrincipals.TOKEN + "' token"
            ).initCause(
                exception
            );
        }
        return this.active;
    }

    /**
     * Logout.  This should never be called.
     *
     * @return A boolean indicating whether or not the logout succeeded.
     */
    public boolean logout() throws LoginException {
        return true;
    }

}
