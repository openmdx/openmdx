/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: LoginServlet.java,v 1.14 2006/08/11 09:45:20 hburger Exp $
 * Description: Login Servlet
 * Revision:    $Revision: 1.14 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/08/11 09:45:20 $
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
package org.openmdx.security.auth.servlet;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.text.StringBuilders;
import org.openmdx.security.auth.context.spi.AuthenticationContext;
import org.openmdx.security.auth.servlet.cci.HttpAuthenticationHandler;
import org.openmdx.security.auth.servlet.cci.HttpCallbackHandler;
import org.openmdx.security.auth.servlet.cci.HttpExceptionHandler;
import org.openmdx.security.auth.servlet.cci.HttpSubjectHandler;
import org.openmdx.security.auth.servlet.cci.TimeoutException;
import org.openmdx.security.auth.servlet.simple.InsecureExceptionHandler;
import org.openmdx.security.auth.servlet.simple.SimpleAuthenticationHandler;
import org.openmdx.security.auth.servlet.simple.SimpleCallbackHandler;
import org.openmdx.security.auth.servlet.simple.SimpleSubjectHandler;

/**
 * Login Servlet
 */
public class LoginServlet extends HttpServlet {

    /**
     * Implements <code>Serializable</code>.
     */
    private static final long serialVersionUID = 3257001060131616054L;

    /**
     * Tells whether debugging is enabled or not
     */
    private boolean debug;

    /**
     * The <code>LoginServlet</code>'s <code>HttpAuthenticationHandler</code>.
     */
    protected HttpAuthenticationHandler authenticationHandler;

    /**
     * The <code>LoginServlet</code>'s <code>HttpCallbackHandler</code>.
     */
    protected HttpCallbackHandler callbackHandler;
    
    /**
     * The <code>LoginServlet</code>'s <code>HttpSubjectHandler</code>.
     */
    protected HttpSubjectHandler subjectHandler;
	
    /**
     * The <code>LoginServlet</code>'s <code>HttpExceptionHandler</code>.
     */
    protected HttpExceptionHandler exceptionHandler;

    /* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init(
	) throws ServletException {
        this.debug = getInitParameter(
            "debug",
            "false"
        ).equalsIgnoreCase(
            "true"
        );
        String authenticationHandler = getInitParameter(
            "authentication-handler",
            SimpleAuthenticationHandler.class.getName()
        );
        String callbackHandler = getInitParameter(
            "callback-handler",
            SimpleCallbackHandler.class.getName()
        );
        String subjectHandler = getInitParameter(
            "subject-handler",
            SimpleSubjectHandler.class.getName()
        );
        String exceptionHandler = getInitParameter(
            "exception-handler",
            InsecureExceptionHandler.class.getName()
        );
		try {
            this.authenticationHandler = (HttpAuthenticationHandler) Classes.getApplicationClass(
                authenticationHandler
            ).newInstance();
            this.callbackHandler = (HttpCallbackHandler) Classes.getApplicationClass(
                callbackHandler
            ).newInstance();
            this.subjectHandler = (HttpSubjectHandler) Classes.getApplicationClass(
                subjectHandler
            ).newInstance();
            this.exceptionHandler = (HttpExceptionHandler) Classes.getApplicationClass(
                exceptionHandler
            ).newInstance();
            if(isDebug()) {
                log("$Id: LoginServlet.java,v 1.14 2006/08/11 09:45:20 hburger Exp $");
                log("authentication-handler: " + authenticationHandler);
                log("callback-handler: " + callbackHandler);
                log("subject-handler: " + subjectHandler);
                log("exception-handler: " + exceptionHandler);
            }
		} catch (Exception exception) {
            throw (UnavailableException) Throwables.initCause(
                new UnavailableException(
                    "Login servlet could not be initialized"
                ),
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("id", "$Id: LoginServlet.java,v 1.14 2006/08/11 09:45:20 hburger Exp $"),
                    new BasicException.Parameter("name", getServletName()),
                    new BasicException.Parameter("authentication-handler", authenticationHandler),
                    new BasicException.Parameter("callback-handler", callbackHandler),
                    new BasicException.Parameter("subject-handler", subjectHandler),
                    new BasicException.Parameter("exception-handler", exceptionHandler),
                    new BasicException.Parameter("debug", isDebug())
                }, null
            );
        }
        this.authenticationHandler.init(getServletConfig());
        this.callbackHandler.init(getServletConfig());
        this.subjectHandler.init(getServletConfig());
        this.exceptionHandler.init(getServletConfig());
	}

    /* (non-Javadoc)
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    public void init(
        javax.servlet.ServletConfig configuration
    ) throws ServletException {
        try {
            super.init(configuration);
        } catch (ServletException exception) {
            CharSequence message = StringBuilders.newStringBuilder("Servlet ");
            if(exception instanceof UnavailableException){
                StringBuilders.asStringBuilder(
                    message
                ).append(
                    ((UnavailableException)exception).isPermanent() ? "permanently" : "temporarely"
                ).append(
                    " unavailable"
                );
            } else {
                StringBuilders.asStringBuilder(
                    message
                ).append(
                    "initialization failed"
                );
            }
            if(exception instanceof BasicException.Wrapper) {
                log(
                    StringBuilders.asStringBuilder(
                        message
                    ).append(
                        ": "
                    ).append(
                        exception).toString()
                    );
            } else {
                log(message.toString(), exception);
            }
            throw  exception;
        }
    }

    /**
     * Retrieve an init parameter or its default value
     * 
     * @param name the parameter name
     * @param defaultValue the parameter's default value
     * 
     * @return the init parameter or ist default value if it where 
     * <code>null</code>
     */
    protected final String getInitParameter(
        String name,
        String defaultValue
    ){
        String value = this.getInitParameter(name);
        return value == null ? defaultValue : value;
    }
    
    /**
     * Tells whether debugging is enabled or not.
     * 
     * @return <code>true</code> if debugging is enabled
     */
    protected final boolean isDebug(){
        return this.debug;
    }
    
    /* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	public void destroy() {
        this.authenticationHandler.destroy();
        this.callbackHandler.destroy();
        this.subjectHandler.destroy();
        this.exceptionHandler.destroy();
        if(isDebug()) log("Handlers destructed");
		super.destroy();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(
		HttpServletRequest request, 
		HttpServletResponse response
	) throws ServletException, IOException {
        doLogin(request, response, true);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(
		HttpServletRequest request, 
		HttpServletResponse response
	) throws ServletException, IOException {
        doLogin(request, response, false);
    }	
    
    /**
     * Start or resume the login process
     * 
     * @param request
     * @param response
     * @param resume start the login process if <code>false</code>, resume it 
     * otherwise 
     * 
     * @throws ServletException
     * @throws IOException
     */
    protected void doLogin(
        HttpServletRequest request, 
        HttpServletResponse response,
        boolean start
    ) throws ServletException, IOException {
        boolean complete = false;
        //
        // HandleRequest
        // 
        for(
            AuthenticationContext authentication = null;
            !complete;
        ) try {
            if(authentication == null) {
                authentication = this.authenticationHandler.getAuthentication(
                    request,
                    start
                );
                if(start) {
                    complete =
                        authentication.getAttempt() == 0 &&
                        authentication.startLogin(null);
                } else try {
                    Callback[] callbacks = authentication.getCallbacks();
                    complete = callbacks == null ?
                        authentication.continueLogin() :
                        this.callbackHandler.handle(
                            request, 
                            callbacks
                        ) && authentication.resumeLogin();
                } catch (UnsupportedCallbackException exception) {
                    complete = authentication.resumeLogin(exception);
                } catch (IOException exception) {
                    complete = authentication.resumeLogin(exception);
                }
            } else {
                complete = authentication.restartLogin();
            }
            //
            // ProvideReply
            //
            while(!complete) try {
                Callback[] callbacks = authentication.getCallbacks();
                if(callbacks == null) {
                    complete = authentication.continueLogin();
                } else {
                    boolean respond = this.callbackHandler.handle(
                        request, 
                        response, 
                        callbacks
                    );
                    if(respond) return;
                    complete = authentication.resumeLogin();
                }
            } catch (UnsupportedCallbackException exception) {
                complete = authentication.resumeLogin(exception);
            } catch (IOException exception) {
                complete = authentication.resumeLogin(exception);
            }
            this.subjectHandler.handle(
                request, 
                response, 
                authentication.getSubject()
            );
        } catch (LoginException exception) {
            complete = this.exceptionHandler.handle(
                request,
                response, 
                exception, 
                authentication == null ? -1 : authentication.getAttempt()
            );
        } catch (IllegalStateException exception) {
            throw new ServletException(
                "Verify the usage of the Authentication object",
                exception
            );
        }
        try {
            this.authenticationHandler.returnAuthentication(
                request
            );
        } catch (TimeoutException exception) {
            log(
                "Authentication object unavailable for return",
                exception
            );
        } catch (ServletException exception) {
            log(
                "Returning Authentication object failed",
                exception
            );
        }
    }
    
}

