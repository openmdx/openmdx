/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractCallbackHandler.java,v 1.5 2005/11/15 13:20:41 hburger Exp $
 * Description: Abstract Callback Handler
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/11/15 13:20:41 $
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

package org.openmdx.security.auth.servlet.spi;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.ChoiceCallback;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.LanguageCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmdx.kernel.security.authentication.callback.AddressCallback;
import org.openmdx.security.auth.servlet.cci.HttpCallbackHandler;

/**
 * Abstract Callback Handler
 */
public class AbstractCallbackHandler 
    extends AbstractHandler 
    implements HttpCallbackHandler 
{
    
    /**
     * Constructor
     */
    public AbstractCallbackHandler() {
        super();
    }

    //------------------------------------------------------------------------
    // Handle Callback Requests
    //------------------------------------------------------------------------

    /**
     * Prompt to retrieve the value of the CGI variable REMOTE_ADDR.
     */
    public static final String REMOTE_ADDR = "REMOTE_ADDR";
    
    /**
     * Prompt to retrieve the Internet Protocol (IP) address of the interface 
     * on which the request was received. 
     */
    public static final String LOCAL_ADDR = "LOCAL_ADDR";

    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.cci.HttpCallbackHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.security.auth.callback.Callback[])
     */
    public boolean handle(
        HttpServletRequest request,
        HttpServletResponse response,
        Callback[] callbacks
    ) throws IOException, UnsupportedCallbackException {
        boolean respond = false;
        StringWriter form = new StringWriter();
        startForm(request, form);
        for(
            int i = 0;
            i < callbacks.length;
            i++
        ) respond |= handle(request, form, i, callbacks[i]);
        if(respond) {
            TokenCallback tokenCallback = new TokenCallback();
            tokenCallback.setToken(getToken(callbacks));
            handle(request, form, callbacks.length, tokenCallback);
            endForm(request, form);
            Writer html = startDocument(response);
            startHead(html);
            endHead(html);
            startBody(html);
            html.write(form.getBuffer().toString());
            endBody(html);
            endDocument(html);
        }
        return respond;
    }

    /**
     * Write the FORM's start tag.
     * 
     * @param request
     * @param html
     * 
     * @throws IOException
     */
    protected void startForm(
        HttpServletRequest request,
        Writer html
    ) throws IOException{
        html.write("<FORM method=\"post\" action=\"");
        html.write(request.getContextPath() + request.getServletPath());
        html.write("\">");
    }

    /**
     * Terminate the FORM including its end tag.
     * 
     * @param request 
     * @param html
     * 
     * @throws IOException
     */
    protected void endForm(
        HttpServletRequest request, 
        Writer html
    ) throws IOException{
        html.write("<P><INPUT type=\"submit\" value=\"Login\">&nbsp;<input type=\"reset\"></P></FORM>");
    }
    
    /**
     * Handle Callbacks
     * 
     * @param request
     * @param html write the to the HTML form
     * @param index 
     * @param callback
     * 
     * @return <code>true</code> if a response has been added
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected boolean handle(
        HttpServletRequest request,
        Writer html, 
        int index, 
        Callback callback
    ) throws IOException, UnsupportedCallbackException {
        synchronized(callback){
            if (callback instanceof TokenCallback) {
                return handle(request, html, index, (TokenCallback) callback);            
            } else if (callback instanceof AddressCallback) {
                return handle(request, html, index, (AddressCallback) callback);            
            } else if (callback instanceof ChoiceCallback) {
                return handle(request, html, index, (ChoiceCallback) callback);                        
            } else if (callback instanceof ConfirmationCallback) {
                return handle(request, html, index, (ConfirmationCallback) callback);
            } else if (callback instanceof LanguageCallback) {
                return handle(request, html, index, (LanguageCallback) callback);
            } else if (callback instanceof NameCallback) {
                return handle(request, html, index, (NameCallback) callback);
            } else if (callback instanceof PasswordCallback) {
                return handle(request, html, index, (PasswordCallback) callback);
            } else if (callback instanceof TextInputCallback) {
                return handle(request, html, index, (TextInputCallback) callback);
            } else if (callback instanceof TextOutputCallback) {
                return handle(request, html, index, (TextOutputCallback) callback);
            } else throw new UnsupportedCallbackException(
                callback
            );
        }
    }
    
    /**
     * Handle Address Callbacks
     * 
     * @param request
     * @param html write the to the HTML form
     * @param index 
     * @param callback
     * @return <code>true</code> if a response has been added
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected boolean handle(
        HttpServletRequest request,
        Writer html, 
        int index, 
        AddressCallback callback
    ) throws IOException, UnsupportedCallbackException {
        String prompt = callback.getPrompt();
        String address = null;
        if(REMOTE_ADDR.equals(prompt)) {
            address = request.getRemoteAddr();
        } else if (LOCAL_ADDR.equals(prompt)) {
            address = request.getLocalAddr();
        } else throw new UnsupportedCallbackException(
            callback,
            AbstractCallbackHandler.class.getName() + 
                " supports the propmpt values {" +
                REMOTE_ADDR + ", " + LOCAL_ADDR + "} only"
        );
        callback.setAddress(
            address == null ? null : InetAddress.getByName(address)
        );
        return false;
    }

    /**
     * Handle Choice Callbacks
     * 
     * @param request
     * @param html write the to the HTML form
     * @param index 
     * @param callback
     * 
     * @return <code>true</code> if a response has been added
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected boolean handle(
        HttpServletRequest request,
        Writer html, 
        int index, 
        ChoiceCallback callback
    ) throws IOException, UnsupportedCallbackException {
        throw new UnsupportedCallbackException(callback);
    }

    /**
     * Handle Confirmation Callbacks
     * 
     * @param request
     * @param html write the to the HTML form
     * @param index 
     * @param callback
     * 
     * @return <code>true</code> if a response has been added
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected boolean handle(
        HttpServletRequest request,
        Writer html, 
        int index, 
        ConfirmationCallback callback
    ) throws IOException, UnsupportedCallbackException {
        throw new UnsupportedCallbackException(callback);
    }

    /**
     * Handle Language Callbacks
     * 
     * @param request
     * @param html write the to the HTML form
     * @param index 
     * @param callback
     * 
     * @return <code>true</code> if a response has been added
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected boolean handle(
        HttpServletRequest request,
        Writer html, 
        int index, 
        LanguageCallback callback
    ) throws IOException, UnsupportedCallbackException {
        callback.setLocale(request.getLocale());
        return true;
    }

    /**
     * Handle Name Callbacks
     * 
     * @param request
     * @param html write the to the HTML form
     * @param index 
     * @param callback
     * 
     * @return <code>true</code> if a response has been added
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected boolean handle(
        HttpServletRequest request,
        Writer html, 
        int index, 
        NameCallback callback
    ) throws IOException, UnsupportedCallbackException {
        throw new UnsupportedCallbackException(callback);
    }
    
    /**
     * Handle Password Callbacks
     * 
     * @param request
     * @param html write the to the HTML form
     * @param index 
     * @param callback
     * 
     * @return <code>true</code> if a response has been added
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected boolean handle(
        HttpServletRequest request,
        Writer html, 
        int index, 
        PasswordCallback callback
    ) throws IOException, UnsupportedCallbackException {
        throw new UnsupportedCallbackException(callback);
    }

    /**
     * Handle Text Input Callbacks
     * 
     * @param request
     * @param html write the to the HTML form
     * @param index 
     * @param callback
     * 
     * @return <code>true</code> if a response has been added
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected boolean handle(
        HttpServletRequest request,
        Writer html, 
        int index, 
        TextInputCallback callback
    ) throws IOException, UnsupportedCallbackException {
        throw new UnsupportedCallbackException(callback);
    }

    /**
     * Handle Text output Callbacks
     * 
     * @param request
     * @param html write the to the HTML form
     * @param index 
     * @param callback
     * 
     * @return <code>true</code> if a response has been added
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected boolean handle(
        HttpServletRequest request,
        Writer html, 
        int index, 
        TextOutputCallback callback
    ) throws IOException, UnsupportedCallbackException {
        throw new UnsupportedCallbackException(callback);
    }

    /**
     * Handle Token Callbacks
     * 
     * @param request
     * @param html write the to the HTML form
     * @param index 
     * @param callback
     * 
     * @return <code>true</code> if a response has been added
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected boolean handle(
        HttpServletRequest request,
        Writer html, 
        int index, 
        TokenCallback callback
    ) throws IOException, UnsupportedCallbackException {
        throw new UnsupportedCallbackException(callback);
    }
    
    //------------------------------------------------------------------------
    // Handle Callback Replies
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.application.security.auth.servlet.cci.HttpCallbackHandler#handle(javax.servlet.http.HttpServletRequest, javax.security.auth.callback.Callback[])
     */
    public boolean handle(
        HttpServletRequest request, 
        Callback[] callbacks
    ) throws IOException, UnsupportedCallbackException {
        TokenCallback tokenCallback = new TokenCallback();
        handle(request, callbacks.length, tokenCallback);
        if(getToken(callbacks).equals(tokenCallback.getToken())) {
            for(
                int i = 0;
                i < callbacks.length;
                i++
            ) handle(request, i, callbacks[i]);
            return true;
        } else {
            return false;
        }
    }

    /**
     * The token should be a valua which changes from callback request
     * to callback request.
     * 
     * @param callbacks
     * 
     * @return the token value
     */
    protected String getToken(
        Callback[] callbacks
    ){
        return String.valueOf(
            System.identityHashCode(callbacks)
        );
    }
    
    /**
     * Handle Callbacks
     * @param request
     * @param index 
     * @param callback
     * 
     * @return <code>true</code> if a response has been added
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected void handle(
        HttpServletRequest request,
        int index, 
        Callback callback
    ) throws IOException, UnsupportedCallbackException {
        synchronized(callback){
            if (callback instanceof TokenCallback) {
                handle(request, index, (TokenCallback) callback);            
            } else if (callback instanceof AddressCallback) {
                handle(request, index, (AddressCallback) callback);            
            } else if (callback instanceof ChoiceCallback) {
                handle(request, index, (ChoiceCallback) callback);                        
            } else if (callback instanceof ConfirmationCallback) {
                handle(request, index, (ConfirmationCallback) callback);
            } else if (callback instanceof LanguageCallback) {
                handle(request, index, (LanguageCallback) callback);
            } else if (callback instanceof NameCallback) {
                handle(request, index, (NameCallback) callback);
            } else if (callback instanceof PasswordCallback) {
                handle(request, index, (PasswordCallback) callback);
            } else if (callback instanceof TextInputCallback) {
                handle(request, index, (TextInputCallback) callback);
            } else if (callback instanceof TextOutputCallback) {
                handle(request, index, (TextOutputCallback) callback);
            } else throw new UnsupportedCallbackException(
                callback
            );
        }
    }
    
    /**
     * Handle Address Callbacks
     * @param request
     * @param index 
     * @param callback
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected void handle(
        HttpServletRequest request,
        int index, AddressCallback callback
    ) throws IOException, UnsupportedCallbackException {
    }

    /**
     * Handle Choice Callbacks
     * @param request
     * @param index 
     * @param callback
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected void handle(
        HttpServletRequest request,
        int index, ChoiceCallback callback
    ) throws IOException, UnsupportedCallbackException {
    }

    /**
     * Handle Confirmation Callbacks
     * @param request
     * @param index 
     * @param callback
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected void handle(
        HttpServletRequest request,
        int index, ConfirmationCallback callback
    ) throws IOException, UnsupportedCallbackException {
    }

    /**
     * Handle Language Callbacks
     * @param request
     * @param index 
     * @param callback
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected void handle(
        HttpServletRequest request,
        int index, LanguageCallback callback
    ) throws IOException, UnsupportedCallbackException {
    }

    /**
     * Handle Name Callbacks
     * @param request
     * @param index 
     * @param callback
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected void handle(
        HttpServletRequest request,
        int index, NameCallback callback
    ) throws IOException, UnsupportedCallbackException {
    }

    /**
     * Handle Password Callbacks
     * @param request
     * @param index 
     * @param callback
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected void handle(
        HttpServletRequest request,
        int index, PasswordCallback callback
    ) throws IOException, UnsupportedCallbackException {
    }

    /**
     * Handle Text Input Callbacks
     * @param request
     * @param index 
     * @param callback
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected void handle(
        HttpServletRequest request,
        int index, TextInputCallback callback
    ) throws IOException, UnsupportedCallbackException {
    }

    /**
     * Handle Text output Callbacks
     * @param request
     * @param index 
     * @param callback
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected void handle(
        HttpServletRequest request,
        int index, TextOutputCallback callback
    ) throws IOException, UnsupportedCallbackException {
    }

    /**
     * Handle Token Callbacks
     * 
     * @param request
     * @param index 
     * @param callback
     * 
     * @throws IOException
     * @throws UnsupportedCallbackException
     */
    protected void handle(
        HttpServletRequest request,
        int index, 
        TokenCallback callback
    ) throws IOException, UnsupportedCallbackException {
    }

}
