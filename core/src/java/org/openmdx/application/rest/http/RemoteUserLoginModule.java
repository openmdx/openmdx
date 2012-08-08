/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: RemoteUserLoginModule.java,v 1.2 2011/04/27 06:20:08 hburger Exp $
 * Description: Remote User Login Module 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/04/27 06:20:08 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010-2011, OMEX AG, Switzerland
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
package org.openmdx.application.rest.http;

import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import org.openmdx.application.rest.spi.AbstractLoginModule;
import org.openmdx.base.rest.cci.RestConnectionSpec;

/**
 * Remote User Login Module
 */
public class RemoteUserLoginModule extends AbstractLoginModule {

    /**
     * The default value for the <code>RefInitializeOnCreate</code> property
     */
    private static Boolean REF_INITIALIZE_ON_CREATE_DEFAULT = Boolean.TRUE;
    
    //	@Override
	public boolean login() throws LoginException {
		NameCallback nameCallback = new NameCallback(CallbackPrompts.REMOTE_USER);
		PasswordCallback passwordCallback = new PasswordCallback(CallbackPrompts.SESSION_ID, false);
		BooleanCallback refInitializeOnCreate = new BooleanCallback(CallbackPrompts.REF_INITIALIZE_ON_CREATE, REF_INITIALIZE_ON_CREATE_DEFAULT);
		this.handle(nameCallback, passwordCallback, refInitializeOnCreate);
		char[] password = passwordCallback.getPassword();
		this.setPublicCredential(
			new RestConnectionSpec(
				nameCallback.getName(),
				password == null ? null : new String(password),
				null,
				Boolean.TRUE.equals(refInitializeOnCreate.getValue())
			)
		);
		return true;
	}

}
