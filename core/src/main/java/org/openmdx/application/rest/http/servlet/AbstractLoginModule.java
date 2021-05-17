/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Abstract Login Module 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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
package org.openmdx.application.rest.http.servlet;

import java.io.IOException;
import java.util.Map;

import javax.resource.cci.ConnectionSpec;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.CredentialNotFoundException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.openmdx.application.rest.http.CallbackPrompts;

/**
 * Abstract Login Module
 */
public abstract class AbstractLoginModule implements LoginModule {

	private Subject subject;
	private CallbackHandler callbackHandler;
	private ConnectionSpec connectionSpec;
	private Map<String, ?> sharedState;

	/**
	 * Retrieve the shared state
	 * 
	 * @return the shared state
	 */
	protected Map<String, ?> getSharedState(){
		return this.sharedState;
	}

	/**
	 * Set the connection spec
	 * 
	 * @param connectionSpec
	 */
	protected void setPublicCredential(
		ConnectionSpec connectionSpec
	){
		this.connectionSpec = connectionSpec;
	}
	
//	@Override
	public boolean abort() throws LoginException {
		this.connectionSpec = null;
		return true;
	}

//	@Override
	public boolean commit() throws LoginException {
		if(this.subject.isReadOnly()) {
			return false;
		} else  {
			this.subject.getPublicCredentials().add(this.connectionSpec);
			return true;
		}
	}

//	@Override
	public void initialize(
		Subject subject, 
		CallbackHandler callbackHandler,
		Map<String, ?> sharedState, 
		Map<String, ?> options
	) {
		this.subject = subject == null ? new Subject() : subject;
		this.callbackHandler = callbackHandler;
		this.sharedState = sharedState;
	}

	/**
	 * Handle the callbacks
	 * 
	 * @param callbacks
	 * 
	 * @throws LoginException
	 */
	protected void handle(
		Callback... callbacks 
	) throws LoginException {
		try {
			this.callbackHandler.handle(callbacks);
		} catch (IOException exception) {
			throw new CredentialException(
				"Unable to retrieve the credential: " + exception.getMessage());
		} catch (UnsupportedCallbackException exception) {
			throw new CredentialNotFoundException(
				exception.getCallback() instanceof NameCallback ? CallbackPrompts.CONNECTION_USER : CallbackPrompts.CONNECTION_PASSWORD
			);
		}
	}

//	@Override
	public boolean logout() throws LoginException {
		return false; // can not close the JCA connection
	}

}
