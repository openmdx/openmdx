/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Dummy Login Module
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2012, OMEX AG, Switzerland
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
package test.openmdx.security.jaas.behaviour;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/**
 * Dummy Login Module
 */
public class DummyLoginModule implements LoginModule {

	private CallbackHandler callbackHandler;
	
	private boolean success;
	
	private Object correlationId;

	public final static String CORRELATION_ID = "CorrelationId";
	
	@Override
	public void initialize(
		Subject subject, 
		CallbackHandler callbackHandler,
		Map<String, ?> sharedState, 
		Map<String, ?> options
	) {
		this.correlationId = options.get("CorrelationId");
		this.callbackHandler = callbackHandler;
		this.success = false;
	}

	@Override
	public boolean login() throws LoginException {
		OutcomeCallback outcomeCallback = new OutcomeCallback(
			this.correlationId
		);
		try {
			this.callbackHandler.handle(
				new Callback[]{outcomeCallback}
			);
		} catch (IOException e) {
			throw new LoginException(e.toString());
		} catch (UnsupportedCallbackException e) {
			throw new LoginException(e.toString());
		}
		switch(outcomeCallback.getOutcome()) {
			case IGNORE:
				return this.success = false;
			case SUCCESS:
				return this.success = true;
		}
		throw new FailedLoginException("This attempt was doomed to fail");
	}

	@Override
	public boolean commit() throws LoginException {
		return this.success;
	}

	@Override
	public boolean abort() throws LoginException {
		boolean success = this.success;
		this.success = false;
		return success;
	}
	
	@Override
	public boolean logout() throws LoginException {
		this.success = false;
		return true;
	}
	
}
