/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: SuspendingCallbackHandler.java,v 1.11 2008/03/17 16:29:18 hburger Exp $
 * Description: Suspending Callback Handler
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/17 16:29:18 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
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
package org.openmdx.security.auth.context.pool;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.EnumSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import org.openmdx.base.concurrent.locks.StateTransitions;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * Suspending <code>CallbackHandler</code>
 */
final class SuspendingCallbackHandler implements CallbackHandler {

	/**
	 * Constructor
	 * 
	 * @param callbackTimeout Defines how long the callback handler waits for a reply
	 * @param maximumWait Defines how log the client is ready to wait
	 * @param owner
	 * @param debug 
	 */
	SuspendingCallbackHandler(
		long callbackTimeout,
		long maximumWait,
		String owner,
		boolean debug
	){
		this.callbackTimeout = callbackTimeout;
		this.maximumWait = maximumWait;
		this.status = new StateTransitions<Status>(
				Status.PASSIVE,
				"Authenticator " + owner + ": CallbackHandler",
				debug
		);
	}

	/**
	 * The <code>Authenticator</code>'s status
	 */
	private final StateTransitions<Status> status;

	/**
	 * The <code>Autenticator</code>'s <code>Lock</code> object
	 */
	private final Lock lock = new ReentrantLock();

	/**
	 * Event suspend
	 */
	private final Condition suspend = this.lock.newCondition();

	/**
	 * Event resume
	 */
	final Condition resume = this.lock.newCondition();

	/**
	 * Defines how log the callback handler waits for a reply
	 */
	final long callbackTimeout;

	/**
	 * Defines how log the client is ready to wait
	 */
	final long maximumWait;

	/**
	 * The pending <code>Exception</code>
	 */
	private volatile Exception exception;

	/**
	 * The callbacks to be handled.
	 */
	volatile Callback[] callbacks;

	/**
	 * 
	 */
	private static final EnumSet<Status> PASSIVE = EnumSet.of(Status.PASSIVE);
	
	/**
	 * 
	 */
	private final static EnumSet<Status> ACTIVE_OR_COMPLETED = EnumSet.of(Status.ACTIVE,Status.COMPLETED);

	/* (non-Javadoc)
	 * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
	 */
	public void handle(
			Callback[] callbacks
	) throws IOException, UnsupportedCallbackException {
		this.lock.lock();
		try {
			this.callbacks = callbacks;
			this.exception = null;
			this.status.transition(Status.PASSIVE, this.suspend, Status.ACTIVE);
			if(
					!this.status.awaitState(PASSIVE, this.resume, this.callbackTimeout)
			) setCallbackException(
					new InterruptedIOException(
							"Callback handling timed out after " + this.callbackTimeout + " ms"
					)
			);
		} catch (InterruptedException exception) {
			setCallbackException(
					(IOException) new InterruptedIOException(
							"A thread suspended for callback handling has been interrupted"
					).initCause(
							exception
					)
			);
		} finally {
			this.lock.unlock();
		}
		propagateCallbackException();
	}

	/**
	 * Tells whether login is completed or in progress
	 * 
	 * @return <code>true</code> if login is committed or aborted
	 * 
	 * @throws LoginException
	 * @throws IOException
	 */
	boolean isCompleted(
	) throws LoginException, IOException {
		this.lock.lock();
		try{
			this.status.awaitState(
					ACTIVE_OR_COMPLETED,
					this.suspend,
					this.maximumWait
			);
			boolean completed = this.status.stateMatches(Status.COMPLETED);
			if(completed) this.status.setState(Status.PASSIVE);
			propagateLoginException();
			return completed;
		} catch (InterruptedException exception) {
			throw (InterruptedIOException) new InterruptedIOException(
					"Maximum wait of " + this.maximumWait + " ms exceeded"
			).initCause(
					exception
			);
		} finally {
			this.lock.unlock();
		}
	}

	/**
	 * Retrieve the callbacks to be handled.
	 * 
	 * @return the callbacks to be handled
	 * 
	 * @throws LoginException if the callback handler is not ACTIVE
	 * and a LoginException is pending 
	 */
	Callback[] getCallbacks(
	) throws LoginException{
		this.lock.lock();
		try {
			return this.status.stateMatches(Status.ACTIVE) ? this.callbacks : null;
		} finally {
			this.lock.unlock();
		}
	}

	/**
	 * Callback handling timed out or was interrupted
	 * 
	 * @param ioException
	 */
	private void setCallbackException(
			IOException ioException
	){
		this.exception = ioException;
		this.callbacks = null;
		this.status.transition(Status.ACTIVE, null, Status.PASSIVE);
	}

	/**
	 * Propagate the callback exception unless it is <code>null</code.
	 * 
	 * @throws IOException 
	 * @throws UnsupportedCallbackException 
	 */
	private void propagateCallbackException(
	) throws IOException, UnsupportedCallbackException{
		if(this.exception instanceof IOException) {
			throw (IOException) this.exception;
		} else if(this.exception instanceof UnsupportedCallbackException) {
			throw (UnsupportedCallbackException) this.exception;
		} else if(this.exception != null) {
			throw new UndeclaredThrowableException(
					this.exception,
					"Assertion failure: " + this.exception.getClass().getName() +
					" is neither an IOException nor an UnsupportedCallbackException"
			);
		}
	}

	/**
	 * Propagate the <code>LoginException</code> unless it is <code>null</code.
	 * 
	 * @throws LoginException
	 */
	private void propagateLoginException(
	) throws LoginException{
		if(this.exception instanceof LoginException) {
			throw (LoginException) this.exception;
		} else if(this.exception != null) {
			throw new UndeclaredThrowableException(
					this.exception,
					"Assertion failure: " + this.exception.getClass().getName() +
					" is not a LoginException"
			);
		}
	}

	/**
	 * Resume login
	 *
	 * @param callbackException The <code>exception</code>'s value
	 */
	void resume(
			Exception callbackException
	) {
		this.lock.lock();
		try {
			this.exception = callbackException;
			this.status.transition(Status.ACTIVE, this.resume, Status.PASSIVE);
		} finally {
			this.lock.unlock();
		}
	}

	/**
	 * Login has commited or aborted
	 * 
	 * @param loginException <code>null</code> in case of commit
	 */
	void complete(
			LoginException loginException
	){
		this.lock.lock();
		try {
			this.exception = loginException;
			this.status.transition(
					Status.PASSIVE,
					this.suspend,
					Status.COMPLETED
			);
		} catch (IllegalStateException stateException) {
			ServiceException exception = new ServiceException(
					stateException,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.ILLEGAL_STATE,
					new BasicException.Parameter[]{
							new BasicException.Parameter("status", this.status),
					},
					"Unexpected state during completion, force to COMPLETED"
			);
			(
					loginException == null ? exception : exception.appendCause(loginException)
			).log();
			this.status.setState(this.suspend, Status.COMPLETED);
		} finally {
			this.lock.unlock();
		}
	}

}
