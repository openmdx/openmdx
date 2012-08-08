/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RemoteAuthenticationHandler.java,v 1.3 2008/09/11 10:47:30 hburger Exp $
 * Description: Remote Authentication Handler
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/11 10:47:30 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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

package org.openmdx.security.auth.login.http;

import java.net.MalformedURLException;
import java.net.URL;

import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;
import org.openmdx.compatibility.base.dataprovider.transport.http.Dataprovider_1HttpConnectionFactory;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.security.auth.servlet.spi.AbstractRemoteAuthenticationHandler;

/**
 * Remote Authentication Handler
 */
public class RemoteAuthenticationHandler 
extends AbstractRemoteAuthenticationHandler 
{

	/**
	 * This handler's Dataprovider Connection Factory Instance
	 */
	private Dataprovider_1HttpConnectionFactory connectionFactory;

	/* (non-Javadoc)
	 * @see org.openmdx.security.auth.servlet.spi.AbstractAuthenticationHandler#init()
	 */
	public void init() throws ServletException {
		// 
		// URL
		//
		String providerURL = this.getInitParameter(
				"realm-provider-url", 
				realmProviderURLDefault()
		); 
		// 
		// Security context
		//
		String username = this.getInitParameter(
				"realm-access-username", 
				realmAccessUsernameDefault()
		); 
		String password = this.getInitParameter(
				"realm-access-password", 
				realmAccessPasswordDefault()
		); 
		Subject subject = new Subject();
		subject.getPrivateCredentials().add(
				new PasswordCredential(
						username,
						password.toCharArray()
				)
		);
		// 
		// Connection Factory
		//
		this.connectionFactory = new Dataprovider_1HttpConnectionFactory();
		try {
			connectionFactory.initialize(
					subject,
					new URL(providerURL)
			);
		} catch (MalformedURLException exception) {
			throw (UnavailableException) Throwables.initCause(
					new UnavailableException(
							"Establishment of the realm's connection factory failed"
					),
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.ACTIVATION_FAILURE,
					new BasicException.Parameter("info", "$Id: RemoteAuthenticationHandler.java,v 1.3 2008/09/11 10:47:30 hburger Exp $"),
					new BasicException.Parameter("name", getServletName()),
					new BasicException.Parameter("realm-provider-url", providerURL),
					new BasicException.Parameter("realm-access-username", username),
					new BasicException.Parameter("realm-access-password", "n/a"),
					new BasicException.Parameter("debug", isDebug())
			);
		}
		// 
		// Log in case of debug
		//
		if(isDebug()) {
			log("$Id: RemoteAuthenticationHandler.java,v 1.3 2008/09/11 10:47:30 hburger Exp $");
			log("realm-provider-url: " + providerURL);
			log("realm-access-username: " + username);
			log("realm-access-password: n/a");
		}
		//
		// Complete initialization
		//
		super.init();
	}

	/* (non-Javadoc)
	 * @see org.openmdx.security.auth.servlet.spi.AbstractRemoteAuthenticationHandler#getConnectionFactory()
	 */
	protected Dataprovider_1ConnectionFactory getConnectionFactory(
	) throws ServiceException {
		return this.connectionFactory;
	}

	/**
	 * Provide the "realm-access-username" default value.
	 * 
	 * @return the "realm-access-username" default value
	 */
	protected String realmAccessUsernameDefault(
	){
		return null; // was "b2e"
	}

	/**
	 * Provide the "realm-access-password" default value.
	 * 
	 * @return the "realm-access-password" default value
	 */
	protected String realmAccessPasswordDefault(
	){
		return null; // was "b2ePassword
	}

}
