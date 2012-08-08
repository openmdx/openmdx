/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: IdentityAssertionValve.java,v 1.4 2008/03/17 15:52:51 hburger Exp $
 * Description: Tomcat Identity Assertion Valve
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/17 15:52:51 $
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
package org.openmdx.tomcat.security.authentication;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.openmdx.kernel.security.authentication.spi.GenericPrincipals;

/**
 * An <b>Authenticator</b> and <b>Valve</b> implementation of authentication
 * that utilizes openMDX tokens to identify client users.
 */
public class IdentityAssertionValve
	extends AuthenticatorBase
{

	/**
	 * The Tomcat Version this valve is made for
	 */
	private final static String TOMCAT_VERSION = "6";

	/**
	 * Descriptive information about this implementation.
	 */
	protected static final String info = IdentityAssertionValve.class.getName() + '/' + TOMCAT_VERSION;

	/**
	 * The logger instance
	 */
    private static Log log = LogFactory.getLog(AuthenticatorBase.class);
	
	/**
	 * Return descriptive information about this Valve implementation.
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * Authenticate the user by checking for the existence of a certificate
	 * chain, and optionally asking a trust manager to validate that we trust
	 * this user.
	 *
	 * @param request Request we are processing
	 * @param response Response we are creating
	 * @param config    Login configuration describing how authentication
	 *              should be performed
	 *
	 * @exception IOException if an input/output error occurs
	 */
	public boolean authenticate(
			Request request,
			Response response,
			LoginConfig config
	) throws IOException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request.getRequest();
		HttpServletResponse httpServletResponse = (HttpServletResponse) response.getResponse();
		// Have we already authenticated someone?
		Principal principal = request.getUserPrincipal();
		if (principal != null) {
			if(log.isTraceEnabled()) {
				log.trace("Already authenticated: " + principal.getName());
			}
			//
			// Associate the session with any existing SSO session in order
			// to get coordinated session invalidation at logout
			//
			String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
			if (ssoId != null) associate(
					ssoId,
					request.getSessionInternal(true)
			);
			return true;
		}
		//
		// Retrieve the certificate chain for this client
		//
		log.trace("Looking up token");
		String token = null;
		Cookie[] cookies = httpServletRequest.getCookies();
		if(cookies != null) for(
				int i = 0;
				token == null && i < cookies.length;
				i++
		) if (
				GenericPrincipals.TOKEN.equals(cookies[i].getName())
		) token = cookies[i].getValue();
		if(token == null) {
			log.trace("No token included with this request");
			httpServletResponse.sendError(
					HttpServletResponse.SC_BAD_REQUEST,
					sm.getString("authenticator.notAuthenticated")
			);
			return (false);
		}
		//
		// Authenticate the specified certificate chain
		//
		principal = this.context.getRealm().authenticate(
				null,
				token
		);
		if (principal == null) {
			log.trace("Realm.authenticate() returned false");
			httpServletResponse.sendError(
					HttpServletResponse.SC_UNAUTHORIZED,
					sm.getString("authenticator.unauthorized")
			);
			return (false);
		}
		//
		// Cache the principal (if requested) and record this authentication
		//
		register(
				request,
				response,
				principal,
				Constants.CERT_METHOD,
				null,
				null
		);
		return true;

	}

}
