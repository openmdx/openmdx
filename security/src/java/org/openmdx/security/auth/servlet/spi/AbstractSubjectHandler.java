/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractSubjectHandler.java,v 1.11 2008/09/11 10:47:30 hburger Exp $
 * Description: Signed Token
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/11 10:47:30 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
package org.openmdx.security.auth.servlet.spi;

import java.security.Principal;
import java.security.PrivateKey;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.Cookie;

import org.openmdx.base.text.conversion.Base64;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.security.authentication.spi.GenericPrincipals;
import org.openmdx.kernel.security.pki.KeyProvider;
import org.openmdx.kernel.security.token.TokenException;
import org.openmdx.kernel.security.token.TokenFactory;
import org.openmdx.security.auth.servlet.cci.HttpSubjectHandler;

public abstract class AbstractSubjectHandler 
extends AbstractHandler
implements HttpSubjectHandler 
{	

	/**
	 * 
	 */
	private TokenFactory tokenFactory;

	/**
	 * The token's timeout in milliseconds; -1L for no expiration
	 */
	private long tokenTimeout;

	/**
	 * Signatur algorithm to be used
	 */
	private String signatureAlgorithm;

	/**
	 * The <code>Cookie</code>'s name
	 */
	private String cookieName;

	/**
	 * The <code>Cookie</code>'s comment
	 */
	private String cookieComment;

	/**
	 * The <code>Cookie</code>'s domain
	 */
	private String cookieDomain;

	/**
	 * The <code>Cookie</code>'s path
	 */
	private String cookiePath;

	/**
	 * Tells the browser whether this cookie should only be passed over a 
	 * secure connection like SSL.
	 */
	private boolean cookieSecure;

	/**
	 * Retrieve the cookieDomain.
	 * 
	 * @return the <code>cookieDomain</code>'s value
	 */
	protected String getCookieDomain() {
		return this.cookieDomain;
	}

	/**
	 * Retrieve the cookieName.
	 * 
	 * @return the <code>cookieName</code>'s value
	 */
	protected String getCookieName() {
		return this.cookieName;
	}

	/**
	 * Retrieve the cookieComment.
	 * 
	 * @return the <code>cookieComment</code>'s value
	 */
	protected String getCookieComment() {
		return this.cookieComment;
	}

	/**
	 * Retrieve the cookiePath.
	 * 
	 * @return the <code>cookiePath</code>'s value
	 */
	protected String getCookiePath() {
		return this.cookiePath;
	}

	/**
	 * Retrieve the cookieSecure.
	 * 
	 * @return the <code>cookieSecure</code>'s value
	 */
	protected boolean isCookieSecure() {
		return this.cookieSecure;
	}

	/**
	 * Retrieve the signatureAlgorithm.
	 * 
	 * @return the <code>signatureAlgorithm</code>'s value
	 */
	protected String getSignatureAlgorithm() {
		return this.signatureAlgorithm;
	}

	/**
	 * Retrieve the tokenFactory.
	 * 
	 * @return the <code>tokenFactory</code>'s value
	 */
	protected TokenFactory getTokenFactory() {
		return this.tokenFactory;
	}

	/**
	 * Retrieve the tokenTimeout.
	 * 
	 * @return the <code>tokenTimeout</code>'s value
	 */
	protected long getTokenTimeout() {
		return this.tokenTimeout;
	}

	/**
	 * Retrieve this handler's <code>KeyPrpvider</code>
	 * 
	 * @return this handler's <code>KeyProvider</code>
	 */
	protected abstract KeyProvider getKeyProvider();


	//------------------------------------------------------------------------
	// Extends AbstractHandler
	//------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see org.openmdx.application.security.auth.servlet.spi.AbstractHandler#init(javax.servlet.Servlet)
	 */
	public void init(ServletConfig configuration) throws ServletException {
		super.init(configuration);
		this.tokenTimeout = getInitParameter(
				"token-timeout",
				-1L // No expiration by default
		);
		this.signatureAlgorithm = getInitParameter(
				"signature-algorithm",
				"SHA1withRSA"
		);
		this.cookieName = getInitParameter(
				"cookie-name",
				GenericPrincipals.TOKEN
		);
		this.cookieComment = getInitParameter(
				"cookie-comment",
				"openMDX Authentication Token"
		);
		this.cookieDomain = getInitParameter(
				"cookie-domain",
				null
		);
		this.cookiePath = getInitParameter(
				"cookie-path",
				null
		);
		this.cookieSecure = getInitParameter(
				"cookie-secure",
				true
		);
		try {
			this.tokenFactory = new TokenFactory(
					getSignatureAlgorithm(), 
					(PrivateKey) getKeyProvider().getKey()
			);
			if(isDebug()) {
				log("$Id: AbstractSubjectHandler.java,v 1.11 2008/09/11 10:47:30 hburger Exp $");
				log("token-timeout:" + this.tokenTimeout);
				log("signature-algorithm:" + this.signatureAlgorithm);
				log("cookie-name:" + this.cookieName);
				log("cookie-comment:" + this.cookieComment);
				log("cookie-domain:" + this.cookieDomain);
				log("cookie-path:" + this.cookiePath);
				log("cookie-secure:" + this.cookieSecure);
			}
		} catch (Exception exception) {
			throw (UnavailableException) Throwables.initCause(
					new UnavailableException(
							"Token factory acquisition failed"
					),
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.ACTIVATION_FAILURE,
					null,
					new BasicException.Parameter("info", "$Id: AbstractSubjectHandler.java,v 1.11 2008/09/11 10:47:30 hburger Exp $"),
					new BasicException.Parameter("name", getServletName()),
					new BasicException.Parameter("token-timeout", this.tokenTimeout),
					new BasicException.Parameter("signature-algorithm", this.signatureAlgorithm),
					new BasicException.Parameter("cookie-name", this.cookieName),
					new BasicException.Parameter("cookie-comment", this.cookieComment),
					new BasicException.Parameter("cookie-domain", this.cookieDomain),
					new BasicException.Parameter("cookie-path", this.cookiePath),
					new BasicException.Parameter("cookie-secure", this.cookieSecure),
					new BasicException.Parameter("debug", isDebug())
			);
		}
	}

	/**
	 * Create a base 64 encoded token based on the subject's principals
	 * 
	 * @param subject
	 * 
	 * @return a token based on the given subject
	 * 
	 * @throws LoginException
	 */
	protected byte[] getToken(
			Subject subject
	) throws LoginException {
		Set<Principal> principalSet = subject.getPrincipals();
		try {
			return getTokenFactory(
			).create(
					getTokenTimeout(), 
					principalSet.toArray(
							new Principal[principalSet.size()]
					)
			);
		} catch (TokenException exception) {
			throw (LoginException) new LoginException(
					"Could not create the token for a given subject"
			).initCause(
					exception
			);
		}
	}

	/**
	 * Create <code>Cookie</code> based on the given subject's token
	 * 
	 * @param subject
	 * 
	 * @return a <code>Cookie</code> based on the subject's token
	 * 
	 * @throws LoginException
	 */
	protected Cookie getCookie(
			Subject subject
	) throws LoginException {
		Cookie cookie = new Cookie(
				getCookieName(),
				Base64.encode(getToken(subject))
		);
		String value;
		if((value = getCookieComment()) != null) cookie.setComment(value);
		if((value = getCookieDomain()) != null) cookie.setDomain(value);
		if((value = getCookiePath()) != null) cookie.setPath(value); 
		cookie.setSecure(isCookieSecure());
		return cookie;
	}

}

