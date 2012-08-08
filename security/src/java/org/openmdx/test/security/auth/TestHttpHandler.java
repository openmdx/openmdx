/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestHttpHandler.java,v 1.12 2008/04/04 17:55:31 hburger Exp $
 * Description: Test Callback Handler
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/04 17:55:31 $
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
package org.openmdx.test.security.auth;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.ChoiceCallback;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.login.AccountExpiredException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.security.auth.servlet.LoginServlet;
import org.openmdx.security.auth.servlet.cci.HttpCallbackHandler;
import org.openmdx.security.auth.servlet.cci.HttpExceptionHandler;
import org.openmdx.security.auth.servlet.simple.InsecureExceptionHandler;
import org.openmdx.security.auth.servlet.simple.SimpleCallbackHandler;

/**
 * Test Simple Callback Handler
 */
public class TestHttpHandler extends ServletTestCase {

    /**
     * Constructor
     */
    public TestHttpHandler() {
        super();
    }

    /**
     * Constructor
     *
     * @param name
     */
    public TestHttpHandler(String name) {
        super(name);
    }

    protected ServletConfig newConfig(){
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("debug", "true");
        map.put("login-configuration", "xri:+resource/org/openmdx/test/security/auth/login.configuration");
        return new TestConfig(map);
    }
    
    /**
     * 
     */
    protected static final DateFormat dateFormat = new SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm"
    );
    
    /**
     * The batch TestRunner can be given a class to run directly.
     * To start the batch runner from your main you can write: 
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * A test runner either expects a static method suite as the
     * entry point to get a test to run or it will extract the 
     * suite automatically. 
     */
    public static Test suite(
    ) {
        return new TestSuite(TestHttpHandler.class);
    }

    /**
     * Method to be detected by the <code>TestSuite</code>
     */
    public void testSimpleCallbackHandler(
    ) throws Exception {
        HttpCallbackHandler callbackHandler = new SimpleCallbackHandler(); 
        callbackHandler.init(newConfig());
        assertTrue(
            "These calllbacks generate output",
            callbackHandler.handle(
                new TestRequest(), 
                new TestResponse(), 
                new Callback[]{
                    new ChoiceCallback(
                        "Colour",
                        new String[]{"red", "green", "blue"},
                        1,
                        true
                    ),
                    new ConfirmationCallback(
                        ConfirmationCallback.WARNING,
                        ConfirmationCallback.YES_NO_CANCEL_OPTION,
                        ConfirmationCallback.NO
                    ),
                    new ConfirmationCallback(
                        "Did exceptions occur?",
                        ConfirmationCallback.ERROR,
                        new String[]{"maybe", "warnings only"},
                        0
                    ),
                    new NameCallback(
                        "UserName",
                        "guest"
                    ),
                    new PasswordCallback(
                        "Password",
                        false
                    ),
                    new TextOutputCallback(
                        TextOutputCallback.INFORMATION,
                        "Here & Now (" + dateFormat.format(new Date()) + ")"
                    ),
                    new TextInputCallback(
                        "Respond to 3729884732904"
                    )
               }
            )
        );
    }

    /**
     * Method to be detected by the <code>TestSuite</code>
     */
    public void testInsecureExceptionHandler(
    ) throws Exception {
        HttpExceptionHandler exceptionHandler = new InsecureExceptionHandler(); 
        exceptionHandler.init(newConfig());
        assertTrue(
            "This exception is not retriable",
            exceptionHandler.handle(
                new TestRequest(), 
                new TestResponse(), 
                new AccountExpiredException(
                    "We are sorry: You forgot to pay this year's membership fee!"
                ),
                1
            )
        );
    }
    
    /**
     * Method to be detected by the <code>TestSuite</code>
     */
    public void testLoginServlet(
    ) throws Exception {
        try {
            Servlet loginServlet = new LoginServlet();
            loginServlet.init(newConfig());    
            HttpSession session = new TestSession();
            HttpServletRequest request = new TestRequest(
                session, 
                "GET", 
                Collections.EMPTY_MAP
            );
            HttpServletResponse response = new TestResponse();
            loginServlet.service(request, response);
            request = new TestRequest(
                session, 
                "POST", 
                Collections.singletonMap(
                    "callback-0", 
                    new String[]{"user"}
                )
            );
            response = new TestResponse();
            loginServlet.service(request, response);
            request = new TestRequest(
                session, 
                "POST", 
                Collections.singletonMap(
                    "callback-0", 
                    new String[]{"USER"}
                )
            );
            response = new TestResponse();
            loginServlet.service(request, response);
        } catch (ServletException exception) {
            new ServiceException(exception).printStackTrace();
        }
    }
       
}
