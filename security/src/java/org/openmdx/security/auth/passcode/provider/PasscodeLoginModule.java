/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: PasscodeLoginModule.java,v 1.7 2008/09/11 10:47:30 hburger Exp $
 * Description: Passcode Login Module
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/11 10:47:30 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
package org.openmdx.security.auth.passcode.provider;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.ProviderException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.security.auth.callback.StandardCallbackPrompts;
import org.openmdx.security.auth.passcode.exception.NewPINRequiredException;
import org.openmdx.security.auth.passcode.exception.WaitForNextTokenException;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusAttribute;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusAttributeValues;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusClient;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusPacket;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException;
import org.openmdx.uses.org.apache.commons.pool.ObjectPool;

/**
 * Passcode Login Module
 */
public class PasscodeLoginModule implements LoginModule {

	/**
	 * where to get user names, passwords, ... for this login
	 */
	private CallbackHandler callbackHandler;

	/**
	 * 
	 */
	private ObjectPool radiusClientPool;

	/**
	 * Defines whether a Passcode is required or not
	 */
	private boolean verified;


	/**
	 * The Token Code Length
	 */
	private Integer tokenCodeLength;

	/**
	 * The debug flag
	 */
	private boolean debug;

	/**
	 * The logger instance
	 */
	private Logger logger;

	/**
	 * The Radius Client Pool Configuration Entry Name
	 */
	public static final String RADIUS_CLIENT_POOL = "radiusClientPool";

	/**
	 * The Token Code Length Configuration Entry Name
	 */
	public static final String TOKEN_CODE_LENGTH = "tokenCodeLength";

	/**
	 * The Debug Configuration Entry Name
	 */
	public static final String DEBUG = "debug";

	/**
	 * Tell whether login attempts should be logged
	 * @return
	 */
	protected final boolean isDebug(){
		return this.debug;
	}

	/**
	 * The Logger to be used
	 * 
	 * @return a logger instance
	 */
	protected Logger newLogger(
	){
		return Logger.getLogger(PasscodeLoginModule.class.getName());
	}

	/**
	 * The Logger to be used
	 * 
	 * @return a logger instance
	 */
	protected final Logger getLogger(
	){
		return this.logger == null ?
				this.logger = newLogger() :
					this.logger;
	}

	/**
	 * Initialize a login attempt.
	 *
	 * @param subject the Subject this login attempt will populate.
	 *
	 * @param callbackhandler the CallbackHandler that can be used to
	 * get the user name, and in authentication mode, the user's password
	 *
	 * @param sharedState A Map containing data shared between login
	 * modules when there are multiple authenticators configured.  This
	 * simple sample does not use this parameter.
	 *
	 * @param options A Map containing options that the authenticator's
	 * authentication provider impl wants to pass to its login module impl.
	 * For example, it can be used to pass in configuration data (where
	 * is the database holding user and group info) and to pass in whether
	 * the login module is used for authentication or to complete identity
	 * assertion.
	 * The RemoteAuthenticationProvider adds an option named "RealmFactory".
	 * The value is a RealmFactory object.  It gives the login module access 
	 * to the remote realm.
	 */
	public void initialize (
			Subject subject, 
			CallbackHandler handler,
			Map<java.lang.String, ?> sharedState,
			Map<java.lang.String, ?> options
	){
		this.callbackHandler = handler;
		this.radiusClientPool = (ObjectPool) options.get(RADIUS_CLIENT_POOL);
		this.tokenCodeLength = (Integer)options.get(TOKEN_CODE_LENGTH);
		this.debug = ((Boolean) options.get(DEBUG)).booleanValue();
	}

	/**
	 * Handle callbacks
	 * 
	 * @param the callbacks to be handled
	 */
	protected void handle (
			Callback[] callbacks
	) throws LoginException{
		try {
			this.callbackHandler.handle(
					callbacks
			);
		} catch (IOException exception) {
			throw toLoginException(
					"Callback Handling Failure",
					exception
			);
		} catch (UnsupportedCallbackException exception) {
			throw toLoginException(
					"Inappropriate Callback Handler",
					exception
			);
		}
	}

	/**
	 * Attempt to login.
	 *
	 * @return A boolean indicating whether or not the login for
	 * this login module succeeded.
	 */
	public boolean login(
	) throws LoginException {
		this.verified = false;
		try {
			//
			// Get Name
			//
			NameCallback nameCallback = new NameCallback(
					StandardCallbackPrompts.USERNAME
			);
			handle(
					new Callback[]{
							nameCallback,
					}
			);
			//
			// Test whether the module should be ignored or not
			//
			if(isPasscodeExempt(nameCallback.getName())) {
				return false; 
			}
			//
			// Validate passcode
			//
			TextInputCallback contextCallback = new TextInputCallback(
					StandardCallbackPrompts.CONTEXT
			);              
			TextInputCallback passCodeCallback;
			PasswordCallback pinCallback;            
			TextInputCallback tokenCodeCallback;
			if(this.tokenCodeLength == null) {
				pinCallback = new PasswordCallback(
						StandardCallbackPrompts.PIN,
						false
				);
				tokenCodeCallback = new TextInputCallback(
						StandardCallbackPrompts.TOKENCODE
				);
				passCodeCallback = null;
				handle(
						new Callback[]{
								pinCallback,
								tokenCodeCallback,
								contextCallback
						}
				);
			} else {
				tokenCodeCallback = null;
				pinCallback = null;
				passCodeCallback = new TextInputCallback(
						StandardCallbackPrompts.PASSCODE
				);
				handle(
						new Callback[]{
								passCodeCallback,
								contextCallback
						}
				);
			}
			RadiusClient radiusClient = null;
			try {
				radiusClient = (RadiusClient) this.radiusClientPool.borrowObject();
			} catch (Exception exception) {
				throw toLoginException(
						"Radius Client Acquistion Failure",
						exception
				);
			}         
			try {
				String context = contextCallback.getText();
				RadiusPacket reply = radiusClient.authenticate(
						this.tokenCodeLength == null ? new AceAccessRequest(
								nameCallback.getName(),
								pinCallback.getPassword(),
								tokenCodeCallback.getText(),
								context
						) : new AceAccessRequest(
								nameCallback.getName(),
								passCodeCallback.getText(),
								context,
								this.tokenCodeLength
						)
				);
				switch(reply.getPacketType()){
				case RadiusPacket.ACCESS_ACCEPT: {
					if(isDebug()) getLogger().fine(
							"User " + nameCallback.getName() + " provided a valid passcode"
					);
					return this.verified = true;
				}
				case RadiusPacket.ACCESS_REJECT: 
					throw new FailedLoginException(getMessage(reply));
				case RadiusPacket.ACCESS_CHALLENGE: {
					String message = getMessage(reply);
					context = SecurIDState.getContext(reply); 
					if(isDebug()) getLogger().fine(
							"[" + context + "] User " + nameCallback.getName() + " is challenged: " + message
					);
					switch(SecurIDState.getTag(context)){
					case SecurIDState.SECURID_WAIT: 
						reply = radiusClient.authenticate(
								this.tokenCodeLength == null ? new AceAccessRequest(
										nameCallback.getName(),
										pinCallback.getPassword(),
										tokenCodeCallback.getText(),
										context
								) : new AceAccessRequest(
										nameCallback.getName(),
										passCodeCallback.getText(),
										context,
										this.tokenCodeLength
								)
						);
						switch(reply.getPacketType()){
						case RadiusPacket.ACCESS_ACCEPT: 
							if(isDebug()) getLogger().fine(
									"User " + nameCallback.getName() + " provided a valid token code"
							);
							return this.verified = true;
						case RadiusPacket.ACCESS_REJECT: 
							throw new FailedLoginException(
									getMessage(reply)
							);            	
						case RadiusPacket.ACCESS_CHALLENGE:
							message = getMessage(reply);
							context = SecurIDState.getContext(reply);
							switch(SecurIDState.getTag(context)){
							case SecurIDState.SECURID_NEXT: 
								throw new WaitForNextTokenException(
										message,
										context
								);
							default:
								throw new LoginException(
										message
								);
							}
						}
					case SecurIDState.SECURID_NEXT: 
						throw new WaitForNextTokenException(
								message,
								context
						);
					case SecurIDState.SECURID_NPIN: 
						throw new NewPINRequiredException(
								message,
								context
						);
					default: 
						throw new LoginException(
								message
						);
					}
				}
				default: 
					throw new RadiusException(
							null,
							"Unexpected Radius Reply Packet Type",
							new BasicException.Parameter("packetType", reply.getPacketType())
					);            	
				}
			} catch (RadiusException exception) {
				throw toLoginException(
						"General Passcode Authentication Failure",
						exception
				);
			} catch (InvalidParameterException exception) {
				throw toLoginException(
						"General Passcode Authentication Failure",
						exception
				);
			} finally {
				try {
					this.radiusClientPool.returnObject(radiusClient);
				} catch (Exception exception) {
					getLogger().log(
							Level.WARNING,
							"Could not return RadiusClient to its pool", 
							exception
					);
				}
			}
		} catch (FailedLoginException exception) {
			if(isDebug()) getLogger().log(
					Level.FINE,
					"Unsuccessful authentication attempt", 
					exception
			);		  
			throw exception;
		} catch (CredentialException exception) {
			if(isDebug()) getLogger().log(
					Level.FINE,
					"Authentication attempt with an invalid or expired credential", 
					exception
			);		    
			throw exception;
		} catch (LoginException exception) {
			getLogger().log(
					Level.WARNING,
					"Unexpected authentication failure", 
					exception
			);
			throw exception;
		}
	}

	protected LoginException toLoginException(
			String message,
			Throwable cause
	){
		LoginException exception = new LoginException(message);
		exception.initCause(
				BasicException.toStackedException(cause, exception)
		);
		return exception;
	}

	/**
	 * Tells whether the login module should be ignored or not
	 * 
	 * @param username
	 * 
	 * @return <code>true</code> if the login module should be ignored.
	 */
	protected boolean isPasscodeExempt(
			String username
	) throws LoginException {
		return false;
	}

	/**
	 * 
	 * @param packet
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected String getMessage(
			RadiusPacket packet
	){
		for(
				Iterator i = packet.getAttributes().iterator();
				i.hasNext();
		) {
			RadiusAttribute attribute = (RadiusAttribute) i.next();
			if(
					attribute.getType() == RadiusAttributeValues.REPLY_MESSAGE
			) try {
				return new String(attribute.getValue(), RadiusClient.ENCODING);
			} catch (UnsupportedEncodingException exception) {
				throw new ProviderException(
						"The RadiusClient's \"" + RadiusClient.ENCODING + "\" encoding is not supported",
						exception
				);
			}
		}
		return null;
	}

	/**
	 * Completes the login by adding the user and the user's groups
	 * to the subject.
	 *
	 * @return A boolean indicating whether or not the commit succeeded.
	 */
	public boolean commit(
	) throws LoginException {
		return this.verified;
	}

	/**
	 * Reset the state
	 * 
	 * @return <code>true</code>
	 */
	private boolean reset(){
		this.verified = false;
		return true;
	}

	/**
	 * Aborts the login attempt.  Remove any principals we put
	 * into the subject during the commit method from the subject.
	 *
	 * @return A boolean indicating whether or not the abort succeeded.
	 */
	public boolean abort(
	) throws LoginException {
		return reset();
	}

	/**
	 * Logout.  This should never be called.
	 *
	 * @return A boolean indicating whether or not the logout succeeded.
	 */
	public boolean logout(
	) throws LoginException {
		return reset();
	}

}