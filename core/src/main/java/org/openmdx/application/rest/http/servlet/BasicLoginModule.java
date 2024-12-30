/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Basic Login Module 
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
package org.openmdx.application.rest.http.servlet;

import #if JAVA_8 javax.resource.cci.ConnectionSpec #else jakarta.resource.cci.ConnectionSpec #endif;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import org.openmdx.application.rest.http.CallbackPrompts;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.kernel.log.SysLog;

/**
 * Basic Login Module
 */
public abstract class BasicLoginModule extends AbstractLoginModule {

    /**
     * Constructor 
     *
     * @param nameCallback
     * @param passwordCallback
     */
    protected BasicLoginModule(
        NameCallback nameCallback,
        PasswordCallback passwordCallback
    ) {
        this.nameCallback = nameCallback;
        this.passwordCallback = passwordCallback;
    }

    private final NameCallback nameCallback;
    private final PasswordCallback passwordCallback;
    
//	@Override
	public boolean login(
	) throws LoginException {
        BooleanCallback bulkLoad = new BooleanCallback(CallbackPrompts.BULK_LOAD, Boolean.FALSE);
        this.handle(nameCallback, passwordCallback, bulkLoad);
		char[] password = passwordCallback.getPassword();
		ConnectionSpec connectionSpec = new RestConnectionSpec(
        	nameCallback.getName(),
        	password == null ? null : new String(password),
        	null,
            Boolean.TRUE.equals(bulkLoad.getValue())
        );
        this.setPublicCredential(connectionSpec);
        SysLog.detail("Login provides ConnectionSpec", connectionSpec);
		return true;
	}

}
