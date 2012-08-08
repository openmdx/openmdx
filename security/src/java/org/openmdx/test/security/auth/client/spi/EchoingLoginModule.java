/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: EchoingLoginModule.java,v 1.2 2009/03/08 18:52:19 wfro Exp $
 * Description: Echoing Login Module
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/08 18:52:19 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package org.openmdx.test.security.auth.client.spi;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.openmdx.security.auth.callback.StandardCallbackPrompts;
import org.openmdx.security.auth.client.spi.CallbackActions;
import org.openmdx.security.auth.client.spi.CancelledCallbackException;

/**
 * Echoing Login Module
 */
public class EchoingLoginModule implements LoginModule {

	private CallbackHandler callbackHandler;
	
	private boolean accepted;
	
	Callback[] callbacks = new Callback[] {
	    new NameCallback(StandardCallbackPrompts.USERNAME, "Benutzer"),
	    new PasswordCallback(StandardCallbackPrompts.PASSWORD, false),
		new PasswordCallback(StandardCallbackPrompts.PIN, false),
		new TextInputCallback(StandardCallbackPrompts.TOKENCODE)
	};
		
	public void initialize(
		Subject subject, 
		CallbackHandler callbackHandler,
		Map<String, ?> sharedState, 
		Map<String, ?> options
	) {
		this.callbackHandler = callbackHandler;
		this.accepted = false;
	}

	public boolean abort() throws LoginException {
		System.out.println("\nAborting...");
		this.accepted = false;
		return true;
	}

	public boolean commit() throws LoginException {
		System.out.println("\nCommitting...");
		for(
			Callback callback : this.callbacks
		){
			if(callback instanceof NameCallback) {
				NameCallback usernameCallback = (NameCallback) callback;
				System.out.println(
					'\t' + usernameCallback.getPrompt() + ": " + (
						usernameCallback.getName() == null ? usernameCallback.getDefaultName() : usernameCallback.getName()
					)
				);
			} 
			else if (callback instanceof PasswordCallback) {
				PasswordCallback passwordCallback = (PasswordCallback) callback;
				System.out.println('\t' + passwordCallback.getPrompt() + ": " + new String(passwordCallback.getPassword()));
			} 
			else if (callback instanceof TextInputCallback) {
				TextInputCallback passcodeCallback = (TextInputCallback) callback;
				System.out.println('\t' + passcodeCallback.getPrompt() + ": " + passcodeCallback.getText());
			} 
			else {
				System.out.println('\t' + callback.getClass().getName() + " not supported");
			}
		}
		return this.accepted;
	}

	public boolean login() throws LoginException {
		System.out.println("\nLoggging in...");
		try {
			this.callbackHandler.handle(this.callbacks);
		} 
		catch (IOException exception) {
			throw (LoginException) (
				CallbackActions.CANCEL.equals(exception.getMessage()) ?
					new CancelledCallbackException("Authentication Cancelled").initCause(exception) :
					new LoginException("Callback Failure").initCause(exception)
			);
		} 
		catch (UnsupportedCallbackException exception) {
			throw (LoginException )new LoginException("Inappropriate Callback Handler").initCause(exception);
		}
		this.accepted = Arrays.equals(
			PASSWORD, 
			((PasswordCallback)this.callbacks[1]).getPassword()
		);
		if(this.accepted) {
			return true; // Login module is relevant
		} 
		else {
			throw new FailedLoginException(
				"Invalid Password"
			);
		}
	}

	public boolean logout() throws LoginException {
		System.out.println("\nLogging out...");
		this.accepted = false;
		return true; // Logout was succeeds
	}

	private static final char[] PASSWORD = new char[]{
		'g', 'e', 'h', 'e', 'i', 'm'
	};

}
