/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: AuthenticationContextThread.java,v 1.18 2009/03/31 17:30:55 hburger Exp $
 * Description: Authentication Context Thread
 * Revision:    $Revision: 1.18 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/31 17:30:55 $
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
import java.util.EnumSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.openmdx.base.concurrent.StateTransitions;
import org.openmdx.kernel.log.LoggerFactory;
import org.openmdx.security.auth.context.spi.AuthenticationContext;
import org.openmdx.security.auth.context.spi.Invalidator;
import org.openmdx.uses.org.apache.commons.pool.ObjectPool;
import org.openmdx.uses.org.apache.commons.pool.impl.GenericObjectPool;

/**
 * Authenticator
 */
public class AuthenticationContextThread
	extends Thread
	implements AuthenticationContext {

	/**
	 * Constructor 
	 * 
	 * @param configuration
	 * @param suffix
	 */
	AuthenticationContextThread(
		AuthenticationContextFactory configuration,
		int suffix
	){
		super(configuration, configuration.getName() + suffix);
		this.callbackHandler = new SuspendingCallbackHandler(
			configuration.getSuspendTimeout(),
			configuration.getMaximumWait(),
			this.getName(),
			configuration.isDebug()
		);
		this.status = new StateTransitions<Status>(
			null,
			"Authenticator " + this.getName(),
			configuration.isDebug()
		);
		this.lock = new ReentrantLock();
		this.activate = this.lock.newCondition();
		this.iterate = this.lock.newCondition();
		this.setDaemon(true);
	}

	/**
	 * 
	 */
	private final SuspendingCallbackHandler callbackHandler;

	/**
	 * The <code>Authenticator</code>'s status
	 */
	final StateTransitions<Status> status;

	/**
	 * The pending login exception
	 */
	private Subject subject;

	/**
	 * The <code>Autenticator</code>'s <code>Lock</code> object
	 */
	final Lock lock;

	/**
	 * Event activate
	 */
	private final Condition activate;

	/**
	 * Event iterate
	 */
	final Condition iterate;

	/**
	 * The correlation id
	 */
	private String id = null;

	/**
	 * Attempts counter
	 */
	private int attempts = -1;

	/**
	 * The logger instance
	 */
	private final static Logger logger = LoggerFactory.getLogger();
	
	/**
	 * Retrieve the id.
	 * 
	 * @return the <code>id</code>'s value
	 */
	public synchronized String getCorrelationId() {
		return this.id;
	}

	/**
	 * Set the id
	 *
	 * @param id The <code>id</code>'s value
	 */
	public synchronized void setCorrelationId(String id) {
		this.id = id;
		if(
				id == null &&
				this.status.stateMatches(Status.COMPLETED)
		) {
			this.lock.lock();
			try {
				this.status.transition(Status.COMPLETED, this.iterate, Status.PASSIVE);
			} 
			finally {
				this.lock.unlock();
			}
		}
	}

	/**
	 * Set the loginException
	 *
	 * @param loginException The <code>loginException</code>'s value
	 */
	final synchronized void setException(LoginException exception) {
		this.callbackHandler.complete(exception);
	}

	/**
	 * Set the subject
	 *
	 * @param subject The <code>subject</code>'s value
	 */
	synchronized final void setSubject(Subject subject) {
		this.subject = subject;
	}

	/**
	 * Create an <code>Authenticator</code> pool
	 * 
	 * @param loginConfiguration the login <code>Configuration</code> URL
	 * @param applicationName
	 * @param initialCapacity
	 * @param maximumCapacity
	 * @param maximumWait
	 * @param idleTimeout
	 * @param callbackTimeout
	 * @param debug 
	 * @param invalidator 
	 * @return a new <code>Authenticator</code> pool
	 * 
	 * @throws IOException if the login <code>Configuration</code> can not be
	 * acquired
	 */
	public static ObjectPool newPool(
		Configuration loginConfiguration,
		String applicationName,
		int initialCapacity,
		int maximumCapacity,
		long maximumWait,
		long idleTimeout,
		long callbackTimeout,
		boolean debug,
		Invalidator invalidator
	) throws IOException{
		ObjectPool pool = new GenericObjectPool(
			new AuthenticationContextFactory(
				applicationName,
				loginConfiguration,
				idleTimeout,
				callbackTimeout,
				maximumWait,
				debug,
				invalidator
			),
			maximumCapacity, // maxActive, 
			maximumWait < 0 ? // whenExhaustedAction
				GenericObjectPool.WHEN_EXHAUSTED_GROW :
					GenericObjectPool.WHEN_EXHAUSTED_BLOCK,
					maximumWait, // maxWait, 
					true, // testOnBorrow, 
					true // testOnReturn
		);
		try {
			for(
				int i = 0;
				i < initialCapacity;
				i++
			) {
				pool.addObject();
			}
		} 
		catch (Exception exception) {
			AuthenticationContextThread.logger.log(Level.WARNING,"Authenticator pool population failed", exception);
		}
		return pool;
	}

	//------------------------------------------------------------------------
	// Implements Runnable
	//------------------------------------------------------------------------

	/**
	 * 
	 */
	final static private EnumSet<Status> ACTIVE = EnumSet.of(Status.ACTIVE);
	
	/**
	 * 
	 */
	final static private EnumSet<Status> ACTIVE_OR_PASSIVE = EnumSet.of(Status.ACTIVE, Status.PASSIVE);

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		AuthenticationContextFactory configuration = (AuthenticationContextFactory)this.getThreadGroup();
		this.lock.lock();
		try {
			this.status.transition(null, this.iterate, Status.PASSIVE);
			reUse: while(!configuration.isDaemon()) {
				this.status.assertState(Status.PASSIVE);
				this.attempts = 0;
				try {
					if(
							!this.status.awaitState(AuthenticationContextThread.ACTIVE, this.activate, configuration.getIdleTimeout())
					) break reUse;
				} 
				catch (InterruptedException exception) {
					break reUse;
				}
				//
				// Re-Associate
				//
				for(
					LoginContext loginContext = new LoginContext(
							configuration.getApplicationName(),
							this.getSubject(),
							this.callbackHandler,
							configuration.getLoginConfiguration()
					);
					this.status.stateMatches(Status.ACTIVE);
				) {
					try {
						this.attempts++;
						loginContext.login();
						this.setSubject(loginContext.getSubject());
						this.setException(null);
					} 
					catch (LoginException exception) {
						this.setException(exception);
					} 
					finally {
						this.status.transition(Status.ACTIVE, null, Status.COMPLETED);
					}
					try {
						if(
							!this.status.awaitState(AuthenticationContextThread.ACTIVE_OR_PASSIVE, this.iterate, configuration.getSuspendTimeout())
						) {
							AuthenticationContextThread.logger.log(
								Level.FINER,
								"Authentication context thread {0} has timed out and will be terminated", 
								this.getName()
							);
							break reUse;
						}
					} 
					catch (InterruptedException exception) {
						AuthenticationContextThread.logger.log(
							Level.FINER,
							"Authentication context thread {0} has been interrupted and will be terminated", 
							this.getName()
						);
						break reUse;
					}
				}
			}
		} 
		catch (LoginException exception){
			AuthenticationContextThread.logger.log(
				Level.WARNING,
				"LoginContext acquisition failed", 
				exception
			);
		} 
		finally {
			this.status.setState(Status.FINALIZABLE);
			this.lock.unlock();
		}
		AuthenticationContextThread.logger.log(
			Level.FINER,
			"Terminating authentication context thread {0}", 
			this.getName()
		);
		try {
			configuration.invalidateObject(this);
		} 
		catch (Exception exception) {
			AuthenticationContextThread.logger.log(
				Level.WARNING,
				"Authentication context invalidation failed", 
				exception
			);
		}
	}


	//------------------------------------------------------------------------
	// Implements Authentication
	//------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see org.openmdx.security.auth.context.spi.AuthenticationContext#begin()
	 */
	public boolean startLogin(
			Subject subject
	) throws LoginException, IOException {
		if(subject != null || this.status.stateMatches(Status.PASSIVE)) {
			this.lock.lock();
			try {
				this.status.assertState(Status.PASSIVE);
				this.setSubject(subject);
				this.status.transition(Status.PASSIVE, this.activate, Status.ACTIVE);
			} 
			finally {
				this.lock.unlock();
			}
		}
		return this.callbackHandler.isCompleted();
	}

	/* (non-Javadoc)
	 * @see org.openmdx.security.auth.context.spi.AuthenticationContext#continueLogin()
	 */
	public boolean continueLogin(
	) throws LoginException, IOException {
		return this.callbackHandler.isCompleted();
	}

	/* (non-Javadoc)
	 * @see org.openmdx.security.auth.context.spi.AuthenticationContext#restartLogin()
	 */
	public boolean restartLogin() throws LoginException, IOException {
		this.lock.lock();
		try {
			this.status.transition(Status.COMPLETED, this.iterate, Status.ACTIVE);
		} 
		finally {
			this.lock.unlock();
		}
		return this.continueLogin();
	}

	/* (non-Javadoc)
	 * @see org.openmdx.security.auth.context.spi.AuthenticationContext#callbacksHandled()
	 */
	public boolean resumeLogin() throws LoginException, IOException {
		this.callbackHandler.resume(null);
		return this.continueLogin();
	}

	/* (non-Javadoc)
	 * @see org.openmdx.security.auth.context.spi.AuthenticationContext#callbacksHandled(java.io.IOException)
	 */
	public boolean resumeLogin(IOException exception) throws LoginException, IOException {
		this.callbackHandler.resume(exception);
		return this.continueLogin();
	}

	/* (non-Javadoc)
	 * @see org.openmdx.security.auth.context.spi.AuthenticationContext#callbacksHandled(javax.security.auth.callback.UnsupportedCallbackException)
	 */
	public boolean resumeLogin(UnsupportedCallbackException exception) throws LoginException, IOException {
		this.callbackHandler.resume(exception);
		return this.continueLogin();
	}

	/* (non-Javadoc)
	 * @see org.openmdx.security.auth.context.spi.AuthenticationContext#getSubject()
	 */
	public Subject getSubject(
	) {
		return this.subject;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.security.auth.context.spi.AuthenticationContext#getAttempt()
	 */
	public synchronized int getAttempt() {
		return this.attempts;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.security.auth.context.spi.AuthenticationContext#getCallbacks()
	 */
	public Callback[] getCallbacks(
	) throws LoginException {
		return this.callbackHandler.getCallbacks();
	}

}
