/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: AuthenticationContext.java,v 1.5 2006/08/09 21:35:15 hburger Exp $
 * Description: Authentication
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/08/09 21:35:15 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.security.auth.context.spi;

import java.io.IOException;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

/**
 * Authentication
 * <p>
 * Callbacks can be handled in either of two ways:<ul>
 * <li>By using <code>Applet</code>'s.
 * <li>By using a sequence of service requests:<ol>
 * <li>Acquire an AuthenticationContext and start login
 * <li>Complete the authentication sequence if there are no more callbacks to be handled
 * <li>Present the callbacks as HTTP form 
 * <li>Re-acquire the AuthenticationContext, apply the <code>POST</code>ed replies to the calback objects and resume login
 * <li>Go to step 2
 * </ol>
 * </ul>
 * <p>
 * The <code>AuthenticationContext</code> pattern helps to follow the service 
 * request sequence strategy. The invocation sequence would look like that, if 
 * it wasn't distributed over different HTTP requests:
 * <pre>
 * for(
 *      boolean handleCallbacks = authentication.startLogin();
 *      handleCallbacks;
 * ) try {
 *     Callback[] callbacks = authentication.getCallbacks();
 *     // Handle Callbacks
 *     handleCallbacks = authentication.resumeLogin();
 * } catch (IOException exception) {
 *     handleCallbacks = authentication.resumeLogin(exception);
 * } catch (UnsupportedCallbackException exception) {
 *     handleCallbacks = authentication.resumeLogin(exception);
 * }
 * Subject subject = authentication.getSubject();
 * </pre>
 */
public interface AuthenticationContext {

	/**
	 * Starts the authentication sequence
	 * @param subject the <code>Subject</code> to authenticate,
     *     or <code>null</code>
	 * @return <code>true</code> if there are no more callbacks to be handled
     * and login has commpleted successfully.
	 * 
	 * @throws LoginException if the login attempt failed
	 * @throws IllegalStateException if the authenticator is in an 
     * inappropriate state
	 */
	boolean startLogin(
         Subject subject
	) throws LoginException, IOException;

    /**
     * Re-starts the authentication sequence
     * 
     * @return <code>true</code> if there are no more callbacks to be handled
     * and login has commpleted successfully.
     * 
     * @throws LoginException if the login attempt failed
     * @throws IllegalStateException if the authenticator is in an 
     * inappropriate state
     */
    boolean restartLogin(
    ) throws LoginException, IOException;

    /**
     * Resumes execution
     * 
     * @return <code>true</code> if there are no more callbacks to be handled
     * and login has commpleted successfully.
     * 
     * @throws IllegalStateException if the authenticator is in an 
     * inappropriate state
     * @throws LoginException if the login process failed
     */
    boolean resumeLogin(
    ) throws LoginException, IOException;
    
    /**
     * Continue execution
     * 
     * @return <code>true</code> if there are no more callbacks to be handled
     * and login has commpleted successfully.
     * 
     * @throws IllegalStateException if the authenticator is in an 
     * inappropriate state
     * @throws LoginException if the login process failed
     */
    boolean continueLogin(
    ) throws LoginException, IOException;

    /**
     * Resumes execution
     * 
     * @param exception The <code>IOException</code> which occured during 
     * callback handling.
     * 
     * @return <code>true</code> if there are no more callbacks to be handled
     * and the login process has commpleted successfully.
     * 
     * @throws IllegalStateException if the authenticator is in an 
     * inappropriate state
     * @throws NullPointerException if <code>exception</code> is 
     * <code>null</code>.
     * @throws LoginException if the login process failed
     */
    boolean resumeLogin(
        IOException exception
    ) throws LoginException, IOException;

    /**
     * Resumes execution
     * 
     * @param exception The <code>UnsupportedCallbackException</code> which 
     * occured during callback handling.
     * 
     * @return <code>true</code> if there are no more callbacks to be handled
     * and the login process has commpleted successfully.
     * 
     * @throws IllegalStateException if the authenticator is in an 
     * inappropriate state
     * @throws NullPointerException if <code>exception</code> is 
     * <code>null</code>.
     * @throws LoginException if the login process failed
     */
    boolean resumeLogin(
        UnsupportedCallbackException exception
    ) throws LoginException, IOException;

    /**
     * Tells how often the login sequence has been started for the associated
     * <code>LoginContext</code>.
     * 
     * @return the number of starts or re-starts.
     */
    int getAttempt();
    
    /**
     * Return the authenticated Subject.
     *
     * @return the authenticated Subject.  
     * If the caller specified a Subject to this Authentication's 
     * login(Subject) method, this method returns the caller-specified 
     * Subject.
     * If a Subject was not specified and authentication succeeds, this 
     * method returns the Subject instantiated and used for authentication by 
     * this Authentication.
     * If a Subject was not specified, and authentication fails, this method 
     * returns <code>null</code>.
     */
    Subject getSubject();
    
	/**
     * Retrieve the callbacks to be handled.
     * 
     * @return the callbacks to be handled
     * 
     * @throws LoginException if the callback handler is not ACTIVE
     * and a LoginException is pending 
	 */
	Callback[] getCallbacks(
	) throws LoginException;

    /**
     * Retrieve the correlation id.
     * 
     * @return the <code>id</code>'s value
     */
    String getCorrelationId();

    /**
     * Set the correlation id.
     *
     * @param id The <code>id</code>'s value
     */
    void setCorrelationId(
         String id
    );
    
    /**
     * Returns the <code>Authentication</code> object's name
     * <p>
     * This method is meant for logging and debugging purposes only.
     * @return the <code>Authentication</code> object's name
     */
    String getName();

}
