/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: AuthenticationContextFactory.java,v 1.10 2009/03/08 18:52:19 wfro Exp $
 * Description: Authentication Context Factory
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/08 18:52:19 $
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

import java.util.EnumSet;

import javax.security.auth.login.Configuration;

import org.openmdx.kernel.log.SysLog;
import org.openmdx.security.auth.context.spi.Invalidator;
import org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory;

/**
 * Authentication Context Factory
 */
class AuthenticationContextFactory
	extends ThreadGroup
	implements PoolableObjectFactory, Invalidator {

	/**
	 * Constructor
	 * 
	 * @param applicationName
	 * @param loginConfiguration
	 * @param idleTimeout
	 * @param callbackTimeout
	 * @param maximumWait 
	 * @param debug 
	 * @param logger
	 * @param invalidator 
	 */
	AuthenticationContextFactory(
		String applicationName,
		Configuration loginConfiguration,
		long idleTimeout,
		long callbackTimeout,
		long maximumWait,
		boolean debug,
		Invalidator invalidator
	){
		super(applicationName);
		this.applicationName = applicationName;
		this.loginConfiguration = loginConfiguration;
		this.idleTimeout = idleTimeout;
		this.callbackTimeout = callbackTimeout;
		this.maximumWait = maximumWait;
		this.debug = debug;
		this.invalidator = invalidator;
	}

	/**
	 * Used to derive authentication context names
	 */
	private volatile int suffix = 0;

	/**
	 * This <code>AuthenticationContext.Factory</code>'s login <code>Configuration</code>
	 */
	final Configuration loginConfiguration;

	/**
	 * This <code>AuthenticationContext.Factory</code>'s application name
	 */
	final String applicationName;

	/**
	 * Defines how long an <code>AuthenticationContext</code> may remain in
	 * <code>PASSIVE</code> state.
	 */
	final long idleTimeout;

	/**
	 * Defines how long an <code>AuthenticationContext</code> may remain in
	 * <code>SUSPENDED</code> state.
	 */
	private final long callbackTimeout;

	/**
	 * Defines whether debg log entries should be written or not.
	 */
	private final boolean debug;

	/**
	 * To invalidate <code>AuthenticationContexThread</code>s
	 */
	private final Invalidator invalidator;

	/**
	 * Defines how long the client is ready to wait.
	 */
	final long maximumWait;

	private final static EnumSet<Status> PASSIVE = EnumSet.of(Status.PASSIVE);

	/* (non-Javadoc)
	 * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#activateObject(java.lang.Object)
	 */
	public void activateObject(
		Object obj
	) throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#destroyObject(java.lang.Object)
	 */
	public void destroyObject(Object obj) throws Exception {
		AuthenticationContextThread authenticator = (AuthenticationContextThread) obj;
		if(!authenticator.status.stateMatches(Status.FINALIZABLE)) {
			authenticator.setCorrelationId(null);
			if(authenticator.status.stateMatches(Status.PASSIVE)) authenticator.interrupt();
		}
		SysLog.detail("Destroying authentication context thread", authenticator.getName());
	}

	/* (non-Javadoc)
	 * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#makeObject()
	 */
	public Object makeObject() throws Exception {
		AuthenticationContextThread authenticator = new AuthenticationContextThread(this, --this.suffix);
		authenticator.lock.lock();
		try {
			authenticator.start();
			authenticator.status.awaitState(
				AuthenticationContextFactory.PASSIVE,
				authenticator.iterate,
				this.getMaximumWait()
			);
		} 
		finally {
			authenticator.lock.unlock();
		}
		return authenticator;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#passivateObject(java.lang.Object)
	 */
	public void passivateObject(Object obj) throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.openmdx.uses.org.apache.commons.pool.PoolableObjectFactory#validateObject(java.lang.Object)
	 */
	public boolean validateObject(Object obj) {
		AuthenticationContextThread authenticator = (AuthenticationContextThread) obj;
		return authenticator.status.stateMatches(Status.PASSIVE);
	}

	/**
	 * @return Returns the loginConfiguration.
	 */
	Configuration getLoginConfiguration() {
		return this.loginConfiguration;
	}


	/**
	 * Retrieve the idleTimeout.
	 * 
	 * @return the <code>idleTimeout</code>'s value
	 */
	long getIdleTimeout() {
		return this.idleTimeout;
	}

	/**
	 * Retrieve the suspendTimeout.
	 * 
	 * @return the <code>suspendTimeout</code>'s value
	 */
	long getSuspendTimeout() {
		return this.callbackTimeout;
	}

	/**
	 * Retrieve the maximumWait.
	 * 
	 * @return the <code>maximumWait</code>'s value
	 */
	long getMaximumWait() {
		return this.maximumWait;
	}

	/**
	 * Retrieve the applicationName.
	 * 
	 * @return the <code>applicationName</code>'s value
	 */
	String getApplicationName() {
		return this.applicationName;
	}

	/**
	 * Retrieve debug.
	 *
	 * @return Returns the debug.
	 */
	public boolean isDebug() {
		return this.debug;
	}

	/**
	 * Terminate this <code>AuthenticationContext.Factory</code>
	 * and all their <code>AuthenticationContext</code>'s. 
	 */
	void terminate(
	){
		this.setDaemon(true);
	}

	/* (non-Javadoc)
	 * @see org.openmdx.security.auth.context.spi.Invalidator#invalidateObject(java.lang.Object)
	 */
	public void invalidateObject(
		Object obj
	) throws Exception {
		this.invalidator.invalidateObject(obj);
	}

}
