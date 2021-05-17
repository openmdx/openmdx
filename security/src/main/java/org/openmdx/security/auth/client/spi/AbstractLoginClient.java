/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: Abstract Login Client
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.security.auth.client.spi;

import java.util.ResourceBundle;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * Abstract Login Client
 */
public abstract class AbstractLoginClient 
	implements Runnable {
	
	/**
	 * Constructor
	 */
	protected AbstractLoginClient(
	) {
	}
		
	/**
	 * Terminate the launcher with an exception stack on the terminal window
	 * 
	 * @param phase the phase during which the error occurred
	 * @param cause the reason for the abort
	 * 
	 * @return <code>null</code>
	 */
	protected <T> T abort(
		String phase,
		Exception cause
	){
		System.err.print(phase);
		System.err.print(" failure:\n\t");
		cause.printStackTrace();
		System.exit(FAILED);
		return null; // Necessary although unreachable
	}
	
	/**
	 * Retrieve the callback handler
	 * 
	 * @return the callback handler to be used to log-in
	 */
	protected abstract CallbackHandler getCallbackHandler(
	);

	/**
	 * Create a callback handler, which may be amended by a sub-class.
	 * 
	 * @return the new callback handler 
	 */
	protected CallbackHandler newCallbackHandler(
	){
		return new SwingCallbackHandler(
			ResourceBundle.getBundle(
				this.getClass().getName()
			)				
		);
	}
	
	/**
	 * Create a new login configuration
	 * 
	 * @return the configuration to be used to log-in
	 */
	protected Configuration getLoginConfiguration(
	){
		return null; // i.e. use the system property java.security.auth.login.config
	}
	
	/**
	 * Create a new login context
	 * 
	 * @return the context to be used to log-in
	 */
	protected LoginContext getLoginContext(
	){
		try {
			return new LoginContext(
				this.getName(),
				null, // subject 
				this.getCallbackHandler(),
				this.getLoginConfiguration()
			);
		} 
		catch (LoginException exception) {
			return this.abort("Login context acquisition", exception);
		}
	}
	
	/**
	 * Provider the client name
	 * 
	 * @return the client name
	 */
	protected String getName(){
		String qualifiedName = this.getClass().getName();
		return qualifiedName.substring(
			qualifiedName.lastIndexOf('.') + 1
		);
	}
	
	/**
	 * Define the maximal number of retries
	 * 
	 * @return the maximal number of login attempts
	 */
	protected int getLoginLimit(){
		return 10;
	}
	
	/**
	 * Log-in 
	 * 
	 * @param loginContext the context to be used to log-in
	 * 
	 * @return <code>true</code> if log-in succeeded,
	 * <code>false</code> if log-in has been canceled.
	 */
	protected boolean login(
		LoginContext loginContext
	){
		for(
			int attempts = this.getLoginLimit();
			attempts > 0;
			attempts--
		){
			try {
				loginContext.login();
				return true;
			} 
			catch (CancelledCallbackException exception) {
				return false;
			} 
			catch (LoginException exception) {
				this.handle(exception);
			}
		}
		return false;
	}

	/**
	 * Log the exception
	 * 
	 * @param exception
	 */
	protected void handle(
		LoginException exception
	){
		exception.printStackTrace();
	}
	
	/**
	 * This method is executed while the user is logged in.
	 * 
	 * @param subject the authenticated subject
	 */
	abstract protected void run(
		Subject subject
	);

	/**
	 * Implements <code>Runnable</code>
	 */
	public void run(
	) {
		LoginContext loginContext = this.getLoginContext();
		if(loginContext == null) {
			System.exit(FAILED);
		} 
		else if(this.login(loginContext)) {
			this.run(loginContext.getSubject());
			try {
				loginContext.logout();
			} 
			catch (LoginException logoutException) {
				// ignore logoutException
			}
			System.exit(SUCCESS);
		} 
		else {
			System.exit(CANCELLED);
		}
	}
	
	/**
	 * Retrieve a single value out of a set
	 * 
	 * @param elementClass
	 * @param set
	 * 
	 * @return the single value out of the set
	 * 
	 * @throws IllegalArgumentException if the set's size is not equal to one
	 */
	protected static <T> T getSingleton(
		Class<T> elementClass,
		Set<T> set
	) {
		int size = set.size();
		if(size == 1) {
			return set.iterator().next();
		} 
		else throw new IllegalArgumentException(
			"Expected exactly one element of type " + elementClass.getName() +
			" in the subject, but found " + size
		);
	}

	/**
	 * Authentication succeeded
	 */
	protected static final int SUCCESS = 0;
	
	/**
	 * Authentication failed
	 */
	protected static final int CANCELLED = 1;
	
	/**
	 * Authentication failed
	 */
	protected static final int FAILED = 2;

}
