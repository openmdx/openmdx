/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: HTTP Request Callback Handler
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

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
#if JAVA_8
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
#else
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
#endif

import org.openmdx.application.rest.http.CallbackPrompts;

/**
 * HTTP Request Callback Handler
 */
public class RequestCallbackHandler implements CallbackHandler {

	/**
	 * Constructor
	 * 
	 * @param request the HTTP servlet request
	 */
	public RequestCallbackHandler(
		HttpServletRequest request 
	) {
		this.request  = request;
	}

	/**
	 * The HTTP request's user principal
	 */
	private final HttpServletRequest request;
	
	/**
	 * Handle the callbacks
	 * 
	 * @param callbacks
	 */
	public final void handle(
		Callback[] callbacks
	) throws IOException, UnsupportedCallbackException {
		for (Callback callback : callbacks) {
			if (!handle(callback)) {
				throw new UnsupportedCallbackException(callback);
			}
		}
	}

	/**
	 * Handle a single callback
	 * 
	 * @param callback
	 * 
	 * @return {@code true} if the callback could be handled
	 */
	protected boolean handle(Callback callback) {
		if (callback instanceof NameCallback) {
			NameCallback nameCallback = (NameCallback) callback;
			String prompt = nameCallback.getPrompt();
			if (CallbackPrompts.REMOTE_USER.equals(prompt)) {
				nameCallback.setName(this.request.getRemoteUser());
				return true;
			}
			if (CallbackPrompts.CONNECTION_USER.equals(prompt)) {
				nameCallback.setName(this.request.getParameter("UserName"));
				return true;
			}
		} else if (callback instanceof PasswordCallback) {
			PasswordCallback passwordCallback = (PasswordCallback) callback;
			String prompt = passwordCallback.getPrompt();
			if (CallbackPrompts.CONNECTION_PASSWORD.equals(prompt)) {
				String password = this.request.getParameter("Password");
				passwordCallback.setPassword(password == null ? null : password.toCharArray());
				return true;
			}
			if (CallbackPrompts.SESSION_ID.equals(prompt)) {
				HttpSession session = this.request.getSession();
				String sessionId = session == null ? null : session.getId();
				passwordCallback.setPassword(sessionId == null ? null : sessionId.toCharArray());
				return true;
			}
		} else if (callback instanceof BooleanCallback) {
            BooleanCallback booleanCallback = (BooleanCallback) callback;
            String prompt = booleanCallback.getPrompt();
            if (CallbackPrompts.BULK_LOAD.equals(prompt)) {
                String bulkLoad = this.request.getParameter(prompt);
                if(bulkLoad != null) {
                    booleanCallback.setValue(Boolean.valueOf(bulkLoad));
                }
                return true;
            }
        }
		return false;
	}

}