/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractAuthenticationHandler.java,v 1.24 2009/03/08 18:52:18 wfro Exp $
 * Description: Abstract HTTP Authentication Handler
 * Revision:    $Revision: 1.24 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/08 18:52:18 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2006, OMEX AG, Switzerland
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
package org.openmdx.security.auth.servlet.spi;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.security.auth.context.pool.AuthenticationContextThread;
import org.openmdx.security.auth.context.spi.AuthenticationContext;
import org.openmdx.security.auth.context.spi.Invalidator;
import org.openmdx.security.auth.context.spi.URLConfiguration;
import org.openmdx.security.auth.servlet.cci.HttpAuthenticationHandler;
import org.openmdx.security.auth.servlet.cci.TimeoutException;
import org.openmdx.uses.org.apache.commons.pool.ObjectPool;

/**
 * Abstract HTTP Authentication Handler
 */
public abstract class AbstractAuthenticationHandler 
extends AbstractHandler 
implements HttpAuthenticationHandler, Invalidator {

	/**
	 * Constructor 
	 */
	protected AbstractAuthenticationHandler(
	) {
	}

	/**
	 * The <code>StandardAuthenticationHandler</code>'s 
	 * <code>Authentication</code> pool.
	 */
	private ObjectPool pool;

	/**
	 * Maps correlation IDs to <code>Authentication</code> objects.
	 */
	private Map<String,AuthenticationContext> associations;

	/**
	 * Tells whether the <code>HttpAuthenticationHandler</code> is lenient.
	 */
	private boolean lenient;

	/**
	 * Retrieve the lenient.
	 * 
	 * @return the <code>lenient</code>'s value
	 */
	protected final boolean isLenient() {
		return this.lenient;
	}

	/**
	 * Retrieve the shared options
	 * 
	 * @return a Map containing the default options shared by all 
	 * <code>LoginModule</code>s
	 * 
	 * @throws ServletException
	 */
	protected abstract Map<String,?> getSharedOptions(
	) throws ServletException;


	//------------------------------------------------------------------------
	// Implements HttpHandler
	//------------------------------------------------------------------------

	/**
	 * A minute in milliseconds
	 */
	private static final long MINUTE = 60 * 1000L;

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init(
	) throws ServletException {
		String loginConfiguration = this.getInitParameter(
				"login-configuration",
				this.loginConfigurationDefault()            
		); 
		String  applicationName = this.getInitParameter(
				"application-name",
				this.applicationNameDefault()
		);
		int initialCapacity = this.getInitParameter(
				"initial-capacity",
				this.initialCapacityDefault()
		);
		int maximumCapacity = this.getInitParameter(
				"maximum-capacity",
				this.maximumCapacityDefault()
		);
		long maximumWait = this.getInitParameter(
				"maximum-wait",
				this.maximumWaitDefault()
		);
		long idleTimeout = this.getInitParameter(
				"idle-timeout",
				this.idleTimeoutDefault()
		);
		long callbackTimeout =  this.getInitParameter(
				"callback-timeout",
				this.callbackTimeoutDefault()
		);
		this.lenient = this.getInitParameter(
				"lenient",
				this.lenientDefault()
		);
		try {
			this.pool = AuthenticationContextThread.newPool(
					new URLConfiguration(
							new URL(loginConfiguration), 
							this.getSharedOptions()
					), 
					applicationName,
					initialCapacity,
					maximumCapacity,
					maximumWait,
					idleTimeout,
					callbackTimeout, 
					this.isDebug(), 
					this
			);
			this.associations = Collections.synchronizedMap(new HashMap<String,AuthenticationContext>());
			if(this.isDebug()) {
				this.log("$Id: AbstractAuthenticationHandler.java,v 1.24 2009/03/08 18:52:18 wfro Exp $");
				this.log("login-configuration: " + loginConfiguration);
				this.log("application-name: " + applicationName);
				this.log("initial-capacity: " + initialCapacity);
				this.log("maximum-capacity: " + maximumCapacity);
				this.log("maximum-wait: " + maximumWait);
				this.log("idle-timeout: " + idleTimeout);
				this.log("callback-timeout: " + callbackTimeout);
				this.log("lenient: " + this.lenient);
			}
		} 
		catch (UnavailableException exception) {
			throw exception;
		} 
		catch (Exception exception) {
			throw Throwables.initCause(
					new UnavailableException(
						"Acquisition of the servlet's authentication provider failed"                     
					),
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.ACTIVATION_FAILURE,
					new BasicException.Parameter("info", "$Id: AbstractAuthenticationHandler.java,v 1.24 2009/03/08 18:52:18 wfro Exp $"),
					new BasicException.Parameter("name", this.getServletName()),
					new BasicException.Parameter("login-configuration", loginConfiguration),
					new BasicException.Parameter("application-name", applicationName),
					new BasicException.Parameter("initial-capacity", initialCapacity),
					new BasicException.Parameter("maximum-capacity", maximumCapacity),
					new BasicException.Parameter("maximum-wait", maximumWait),
					new BasicException.Parameter("idle-timeout", idleTimeout),
					new BasicException.Parameter("callback-timeout", callbackTimeout),
					new BasicException.Parameter("lenient", this.isLenient()),
					new BasicException.Parameter("debug", this.isDebug())
			);
		}
	}

	/* (non-Javadoc)
	 * @see org.openmdx.application.security.auth.servlet.spi.AbstractHandler#destroy()
	 */
	public void destroy(
	){
		try {
			this.associations.clear();
			this.pool.close();
		} 
		catch (Exception exception) {
			this.log("Authentication Object Pool Destruction Failure", exception);
		} 
		finally {
			this.associations = null;
			this.pool = null;
			super.destroy();
		}
	}


	//------------------------------------------------------------------------
	// Implements HttpAuthenticationHandler
	//------------------------------------------------------------------------

	/**
	 * Get the correlation id for a given HTTP request.
	 * 
	 * @return the correlation id to be used to associate a HTTP request with
	 * an <code>Authentication</code> object.
	 */
	protected String getCorrelationId(
			HttpServletRequest request
	){
		return request.getSession().getId();
	}

	/* (non-Javadoc)
	 * @see org.openmdx.application.security.auth.servlet.cci.HttpAuthenticationHandler#getAuthentication(javax.servlet.http.HttpServletRequest, boolean)
	 */
	public AuthenticationContext getAuthentication(
			HttpServletRequest request, 
			boolean create
	) throws ServletException, TimeoutException {
		String correlationId = this.getCorrelationId(request);
		AuthenticationContext reply = (AuthenticationContext) this.associations.get(correlationId);
		if(create){
			if(reply == null || !correlationId.equals(reply.getCorrelationId())) try {
				reply = (AuthenticationContext) this.pool.borrowObject();
				reply.setCorrelationId(correlationId);
				this.associations.put(
						correlationId, 
						reply
				);
			} 
			catch (Exception exception) {
				throw new ServletException(
						"No Authentication Context available",
						exception
				);
			}
			if(this.isDebug()) this.log(
					"Thread " + Thread.currentThread().getName() + 
					" borrowed Authenticator " + reply.getName() +
					" from the pool"
			);
		} 
		else {
			if(reply != null && !correlationId.equals(reply.getCorrelationId())) {                    
				this.associations.remove(correlationId);
				reply = null;
			}
			if(reply == null) throw new TimeoutException(
					"Authentication Context no longer available"
			);
			if(this.isDebug()) this.log(
					"Thread " + Thread.currentThread().getName() + 
					" re-associated with Authenticator " + reply.getName()
			);
		}
		return reply;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.application.security.auth.servlet.cci.HttpAuthenticationHandler#returnAuthentication(javax.servlet.http.HttpServletRequest)
	 */
	public void returnAuthentication(
			HttpServletRequest request
	) throws ServletException, TimeoutException {
		String correlationId = this.getCorrelationId(request);
		AuthenticationContext authenticationContext = (AuthenticationContext) this.associations.remove(correlationId);
		if(authenticationContext == null) {
			if(this.isDebug() || !this.isLenient()) this.log(
					"Thread " + Thread.currentThread().getName() + 
					" tries to return its authentication context" +
					" while none is assoicated with it" 
			);
		} 
		else if(!correlationId.equals(authenticationContext.getCorrelationId())) {
			if(this.isDebug() || !this.isLenient()) this.log(
					"Thread " + Thread.currentThread().getName() + 
					" tries to return the authentication context " + authenticationContext.getName() +
					" already owned by another login context now"
			);
		} 
		else try {
			authenticationContext.setCorrelationId(null);
			this.pool.returnObject(authenticationContext);
			if(this.isDebug()) this.log(
					"Thread " + Thread.currentThread().getName() + 
					" returned authentication context " + authenticationContext.getName() +
					" to the pool"
			);
		} 
		catch (Exception exception) {
			if(!this.isLenient()) throw (ServletException) new ServletException(
					"Failed to return authentication context" + authenticationContext.getName()
			).initCause(
					exception
			);
		}
	}

	/* (non-Javadoc)
	 * @see org.openmdx.uses.org.apache.commons.pool.ObjectPool#invalidateObject(java.lang.Object)
	 */
	public void invalidateObject(Object obj)
	throws Exception {
		this.pool.invalidateObject(obj);
	}

	/**
	 * Provide the "login-configuration" default value.
	 * 
	 * @return the "login-configuration" default value
	 */
	protected String loginConfigurationDefault(
	){
		return "xri://+resource/org/openmdx/security/auth/servlet/login.configuration";
	}

	/**
	 * Provide the "initital-capacity" default value.
	 * 
	 * @return the "initital-capacity" default value
	 */
	protected int initialCapacityDefault(
	){
		return 0;
	}

	/**
	 * Provide the "maximum-capacity" default value.
	 * 
	 * @return the "maximum-capacity" default value
	 */
	protected int maximumCapacityDefault(
	){
		return 10;
	}

	/**
	 * Provide the "maximum-wait" default value.
	 * 
	 * @return the "maximum-wait" default value
	 */
	protected long maximumWaitDefault(
	){
		return 1 * AbstractAuthenticationHandler.MINUTE;

	}

	/**
	 * Provide the "idle-timeout" default value.
	 * 
	 * @return the "idle-timeout" default value
	 */
	protected long idleTimeoutDefault(
	){
		return 30 * AbstractAuthenticationHandler.MINUTE;
	}

	/**
	 * Provide the "callback-timeout" default value.
	 * 
	 * @return the "callback-timeout" default value
	 */
	protected long callbackTimeoutDefault(
	){
		return 1 * AbstractAuthenticationHandler.MINUTE;
	}

	/**
	 * Provide the "lenient" default value.
	 * 
	 * @return the "lenient" default value
	 */
	protected boolean lenientDefault(
	){
		return false;
	}

	/**
	 * Provide the "application-name" default value.
	 * 
	 * @return the "application-name" default value
	 */
	protected String applicationNameDefault(
	){
		return "RemoteAuthentication"; // was getServletName()
	}

}
