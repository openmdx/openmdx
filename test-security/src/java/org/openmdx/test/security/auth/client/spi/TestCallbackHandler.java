/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: TestCallbackHandler.java,v 1.1 2009/03/11 16:32:33 hburger Exp $
 * Description: Test Callback Handler
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/11 16:32:33 $
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

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.openmdx.security.auth.callback.AmendingCallbackHandler;
import org.openmdx.security.auth.client.spi.AbstractLoginClient;
import org.openmdx.security.auth.client.spi.TerminalCallbackHandler;


/**
 * Test Callback Handler
 */
public class TestCallbackHandler extends AbstractLoginClient {

	/**
	 * Tells whether the console or swing should be uses as user interface.
	 */
	private static boolean SWING = true;

	/**
	 * 
	 */
	private AmendingCallbackHandler callbackHandler = new AmendingCallbackHandler(
		TestCallbackHandler.SWING ? this.newCallbackHandler() : new TerminalCallbackHandler()
	);
	
	/**
	 * Main
	 * 
	 * @param arguments
	 */
	public static void main(
		String... arguments
	){
		new TestCallbackHandler().run();
	}

	
	/* (non-Javadoc)
	 * @see org.openmdx.security.auth.client.spi.AbstractLoginClient#newCallbackHandler()
	 */
	@Override
	protected CallbackHandler getCallbackHandler() {
		return this.callbackHandler;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.security.auth.client.spi.AbstractLoginClient#newLoginConfiguration()
	 */
	@Override
	protected Configuration getLoginConfiguration() {
		return new EchoingLoginConfiguration();
	}

	/* (non-Javadoc)
	 * @see org.openmdx.security.auth.client.spi.AbstractLoginClient#run(javax.security.auth.Subject)
	 */
	@Override
	protected void run(
		Subject subject
	) {
		System.out.println("Success");
	}


	/* (non-Javadoc)
	 * @see org.openmdx.security.auth.client.spi.AbstractLoginClient#handle(javax.security.auth.login.LoginException)
	 */
	@Override
	protected void handle(LoginException exception) {
		if(exception instanceof FailedLoginException) {
			this.callbackHandler.setEpilog(
				new Callback[]{
					new TextOutputCallback(
						TextOutputCallback.INFORMATION, 
						"PASSWORD_HINT"
					)	
				}
			);
		} 
		else {
			this.callbackHandler.setEpilog(null);
			super.handle(exception);
		}
	}
	
}
