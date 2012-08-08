/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractRemoteAuthenticationHandler.java,v 1.11 2008/09/11 10:47:30 hburger Exp $
 * Description: AbstractRemoteAuthenticationHandler 
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/11 10:47:30 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2008, OMEX AG, Switzerland
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

import org.openmdx.base.accessor.jmi.spi.PersistenceManagerFactory_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.persistence.cci.ConfigurableProperty;
import org.openmdx.model1.accessor.basic.spi.Model_1;
import org.openmdx.security.realm1.cci.Realm;

/**
 * AbstractRemoteAuthenticationHandler
 */
public abstract class AbstractRemoteAuthenticationHandler
extends AbstractAuthenticationHandler
{

	/**
	 * Constructor 
	 */
	protected AbstractRemoteAuthenticationHandler() {
	}

	/* (non-Javadoc)
	 * @see org.openmdx.security.auth.servlet.spi.AbstractAuthenticationHandler#init()
	 */
	public void init(
	) throws ServletException {
		//
		// Load models
		//
		try {
			new Model_1().addModels(
					getModels(
							this.getInitParameter(
									"models",
									modelsDefault()
							)
					)
			);
		} catch (ServiceException exception) {
			throw new ServletException(
					"Model loading failed",
					exception
			);
		}
		//
		// Complete initialization
		//
		super.init();
	}

	/* (non-Javadoc)
	 * @see org.openmdx.security.auth.servlet.simple.SimpleAuthenticationHandler#getSharedOptions()
	 */
	final protected synchronized Map<String,?> getSharedOptions(
	) throws ServletException {
		String realmIdentifier = this.getInitParameter(
				"realm-xri",
				realmXRIDefault()
		);
		String nameCallbackPrompt = this.getInitParameter(
				"name-prompt",
				namePromptDefault()
		);
		String passwordEcho = this.getInitParameter(
				"password-echo",
				passwordEchoDefault()
		);
		String passwordPrompt = this.getInitParameter(
				"password-prompt",
				passwordPromptDefault()
		);
		String realmInformation = this.getInitParameter(
				"realm-information",
				realmInformationDefault()
		);
		long unavailabilityExpectation = this.getInitParameter(
				"realm-unavailability-expectation",
				realmUnavailabilityExpectationDefault()
		);
		try {
			Object passwordCallbackEchoConfiguration = getEchoConfiguration(
					passwordEcho
			);
			// 
			// Log in case of debug
			//
			if(isDebug()) {
				log("$Id: AbstractRemoteAuthenticationHandler.java,v 1.11 2008/09/11 10:47:30 hburger Exp $");
				log("realm-xri: " + realmIdentifier);
				log("name-prompt: " + nameCallbackPrompt);
				log("password-echo: " + passwordEcho);
				log("password-prompt: " + passwordPrompt);
				log("realm-information: " + realmInformation);
				log("realm-unavailability-expectation: " + unavailabilityExpectation + " ms");
			}
			Map<String,Object> properties = new HashMap<String,Object>();
			properties.put(
					ConfigurableProperty.PersistenceManagerFactoryClass.qualifiedName(),
					PersistenceManagerFactory_1.class.getName()
			);
			properties.put(
					Dataprovider_1ConnectionFactory.class.getName(),
					getConnectionFactory()
			);
			properties.put(
					ConfigurableProperty.Optimistic.qualifiedName(),
					Boolean.TRUE.toString()
			);
			PersistenceManagerFactory persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory(properties);
			// 
			// Return
			//
			Map<String,Object> options = new HashMap<String,Object>();
			options.put(Realm.class.getName(), realmIdentifier);
			options.put(PersistenceManagerFactory.class.getName(), persistenceManagerFactory);
			options.put(PasswordCallback.class.getName() + ".echoOn", passwordCallbackEchoConfiguration);
			options.put(PasswordCallback.class.getName() + ".prompt", passwordPrompt);
			options.put(TextOutputCallback.class.getName() + ".realm", realmInformation);
			options.put(NameCallback.class.getName() + ".prompt", nameCallbackPrompt);
			return options;
		} catch (Exception exception) {
			throw (UnavailableException) Throwables.initCause(
					new UnavailableException(
							"Acquisition of the servlet's realm accessor failed",
							(int) TimeUnit.MILLISECONDS.toSeconds(unavailabilityExpectation)
					),
					exception,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.ACTIVATION_FAILURE,
					null,
					new BasicException.Parameter("info", "$Id: AbstractRemoteAuthenticationHandler.java,v 1.11 2008/09/11 10:47:30 hburger Exp $"),
					new BasicException.Parameter("name", getServletName()),
					new BasicException.Parameter("realm-xri", realmIdentifier),
					new BasicException.Parameter("name-prompt", nameCallbackPrompt),
					new BasicException.Parameter("password-echo", passwordEcho),
					new BasicException.Parameter("password-prompt", passwordPrompt),
					new BasicException.Parameter("realm-information", realmInformation),
					new BasicException.Parameter("realm-unavailability-expectation", unavailabilityExpectation),
					new BasicException.Parameter("debug", isDebug())
			);
		}
	}

	/**
	 * Connection factory callback method
	 * 
	 * @return the <code>Dataprovider_1ConnectionFactory</code> to be sused by the <code>LoginModule</code>.
	 * 
	 * @exception ServiceException if the connection factory can't be retrieved.
	 */
	protected abstract Dataprovider_1ConnectionFactory getConnectionFactory(
	) throws ServiceException;

	/**
	 * Convert the echo configuration to an object
	 * 
	 * @param echoConfiguration the echo configuration, e.g.<ul>
	 * <li><code>"false"</code> (Echo off for all calllbacks)
	 * <li><code>"true"</code>  (Echo on for all calllbacks)
	 * <li><code>"[2]"</code> (Echo on in case of request code <code>2</code>)
	 * <li><code>"[2, 3]"</code> (Echo on in case of request codes <code>2</code> or <code>3</code>)
	 * </ul>
	 * 
	 * @return the echo configuration object, e.g.<ul>
	 * <li><code>Boolean.FALSE</code> (Echo off for all calllbacks)
	 * <li><code>Boolean.TRUE</code>  (Echo on for all calllbacks)
	 * <li><code>Collections.singleton(new Short(2))</code> (Echo on in case of request code <code>2</code>)
	 * <li><code>new HashSet(Arrays.asList(new Short[]{new Short(2), new Short(3)}))</code> (Echo on in case of request codes <code>2</code> or <code>3</code>)
	 * </ul>
	 */
	private static Object getEchoConfiguration(
			String echoConfiguration
	){
		if(
				echoConfiguration == null ||
				"".equals(echoConfiguration) ||
				"false".equalsIgnoreCase(echoConfiguration)
		) {
			return Boolean.FALSE;
		} else if (
				"true".equalsIgnoreCase(echoConfiguration)
		) {
			return Boolean.TRUE;
		} else if(
				echoConfiguration.startsWith("[") &&
				echoConfiguration.endsWith("]")
		) {
			Set<Short> codes = new HashSet<Short>();
			for(
					int j = 0, i = 1, iLimit = echoConfiguration.length() - 1;
					i < iLimit;
					i = j + 2
			){
				j = echoConfiguration.indexOf(", ", i);
				if(j < 0) j = iLimit;
				codes.add(
						Short.valueOf(echoConfiguration.substring(i, j))
				);
			}
			return codes;
		} else {
			return Boolean.FALSE;
		}
	}

	/**
	 * Convert the model configuration to a Set
	 * 
	 * @param modelConfiguration
	 * 
	 * @return a Set of models to be loaded
	 */
	private Set<String> getModels(
			String modelConfiguration
	){
		if(
				modelConfiguration == null ||
				modelConfiguration.length() == 0
		) {
			return Collections.emptySet();
		} else if(
				modelConfiguration.startsWith("[") &&
				modelConfiguration.endsWith("]")
		) {
			Set<String> models = new HashSet<String>();
			for(
					int j = 0, i = 1, iLimit = modelConfiguration.length() - 1;
					i < iLimit;
					i = j + 2
			){
				j = modelConfiguration.indexOf(", ", i);
				if(j < 0) j = iLimit;
				models.add(
						modelConfiguration.substring(i, j)
				);
			}
			return models;
		} else {
			Set<String> models = new HashSet<String>(getModels(modelsDefault()));
			models.add(modelConfiguration);
			return models;
		}
	}

	/**
	 * Provide the "unavailability-expectation" default value.
	 * 
	 * @return the "unavailability-expectation" default value
	 */
	protected long realmUnavailabilityExpectationDefault(
	){
		return 0L;
	}

	/**
	 * Provide the "realm-xri" default value of the form
	 * "xri:@openmdx:org.openmdx.security.realm1/provider/'Provider'/segment/'Segment'/realm/'Realm'";
	 * 
	 * @return the "realm-xri" default value.
	 */
	protected String realmXRIDefault(
	){
		return null;
	}

	/**
	 * Provide the "password-echo" default configuration.
	 * 
	 * @return the "password-echo" default configuration, e.g.<ul>
	 * <li><code>"false"</code> (Echo off for all calllbacks)
	 * <li><code>"true"</code>  (Echo on for all calllbacks)
	 * <li><code>"[2]"</code> (Echo on in case of request code <code>2</code>)
	 * <li><code>"[2, 3]"</code> (Echo on in case of request codes <code>2</code> or <code>3</code>)
	 * </ul>
	 */
	protected String passwordEchoDefault(
	){
		return "false";
	}

	/**
	 * Provide the "name-prompt" default value.
	 * 
	 * @return the "name-prompt" default value
	 */
	protected String namePromptDefault(
	){
		return "Username";
	}

	/**
	 * Provide the "realm-information" default value.
	 * <p>
	 * The following placeholders are supported<ul>
	 * <li><code>${realm.id}</code> The realm path's base name
	 * <li><code>${realm.xri}</code> The realm path's XRI
	 * </ul>
	 * 
	 * @return the "realm-information" default value
	 */
	protected String realmInformationDefault(
	){
		return "Realm ${realm.xri}";
	}

	/**
	 * Provide the "password-prompt" default value.
	 * <p>
	 * The following placeholders are supported<ul>
	 *     <li><code>${name}</code> The <code>NameCallback</code>'s name
	 *     <li><code>${challenge}</code> The credential's challenge
	 * </ul>
	 * 
	 * @return the "password-prompt" default value
	 */
	protected String passwordPromptDefault(
	){
		return "${challenge}";
	}

	/**
	 * Provide the "realm-provider-url" default value.
	 * 
	 * @return the "realm-provider-url" default value
	 */
	protected String realmProviderURLDefault(
	){
		return null;
	}

	/**
	 * Provide the "realm-initial-context-factory" default value.
	 * 
	 * @return the "realm-initial-context-factory" default value
	 */
	protected String realmInitialContextFactoryDefault(
	){
		return null;
	}

	/**
	 * Provide the "realm-jndi-name" default value.
	 * 
	 * @return the "realm-jndi-name" default value
	 */
	protected String realmJNDINameDefault(
	){
		return null;
	}

	/**
	 * Provide the "models" default value.
	 * 
	 * @return the "models" default value
	 */
	protected String modelsDefault(
	){
		return
		"org:openmdx:base," +
		"org:w3c," +
		"org:oasis-open," +
		"org:openmdx:compatibility:role1," +
		"org:openmdx:compatibility:state1," +
		"org:openmdx:compatibility:view1," +
		"org:openmdx:security:realm1";
	}

}
