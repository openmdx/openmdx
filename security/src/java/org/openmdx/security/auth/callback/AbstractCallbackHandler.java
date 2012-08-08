/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: AbstractCallbackHandler.java,v 1.3 2010/03/05 13:26:09 hburger Exp $
 * Description: Abstract Callback Handler
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/05 13:26:09 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package org.openmdx.security.auth.callback;

import java.io.IOException;
import java.util.Locale;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.ChoiceCallback;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.LanguageCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;


/**
 * Abstract Callback Handler
 */
public abstract class AbstractCallbackHandler implements CallbackHandler {

    /**
     * Constructor
     */
    protected AbstractCallbackHandler() {
    }

    /**
     * Handle a given callback class
     * 
     * @param callback the callback object to be handled
     * 
     * @throws UnsupportedCallbackException if the given callback class is not supported
     * @throws IOException if callback handling fails
     */
    protected void handle(
    	ChoiceCallback callback
    ) throws IOException, UnsupportedCallbackException {
    	this.unsupported(callback);
    }
    
    /**
     * Handle a given callback class
     * 
     * @param callback the callback object to be handled
     * 
     * @throws UnsupportedCallbackException if the given callback class is not supported
     * @throws IOException if callback handling fails
     */
    protected void handle(
    	ConfirmationCallback callback
    ) throws IOException, UnsupportedCallbackException {
    	this.unsupported(callback);
    }
    
    /**
     * Handle a given callback class
     * 
     * @param callback the callback object to be handled
     * 
     * @throws UnsupportedCallbackException if the given callback class is not supported
     * @throws IOException if callback handling fails
     */
    protected void handle(
    	LanguageCallback callback
    ) throws IOException, UnsupportedCallbackException {
    	callback.setLocale(Locale.getDefault());
    }
    
    /**
     * Handle a given callback class
     * 
     * @param callback the callback object to be handled
     * 
     * @throws UnsupportedCallbackException if the given callback class is not supported
     * @throws IOException if callback handling fails
     */
    protected void handle(
    	NameCallback callback
    ) throws IOException, UnsupportedCallbackException {
    	this.unsupported(callback);
    }
    
    /**
     * Handle a given callback class
     * 
     * @param callback the callback object to be handled
     * 
     * @throws UnsupportedCallbackException if the given callback class is not supported
     * @throws IOException if callback handling fails
     */
    protected void handle(
    	PasswordCallback callback
    ) throws IOException, UnsupportedCallbackException {
    	this.unsupported(callback);
    }
    
    /**
     * Handle a given callback class
     * 
     * @param callback the callback object to be handled
     * 
     * @throws UnsupportedCallbackException if the given callback class is not supported
     * @throws IOException if callback handling fails
     */
    protected void handle(
    	TextInputCallback callback
    ) throws IOException, UnsupportedCallbackException {
    	this.unsupported(callback);
    }
    
    /**
     * Handle a given callback class
     * 
     * @param callback the callback object to be handled
     * 
     * @throws UnsupportedCallbackException if the given callback class is not supported
     * @throws IOException if callback handling fails
     */
    protected void handle(
    	TextOutputCallback callback
    ) throws IOException, UnsupportedCallbackException {
    	this.unsupported(callback);
    }

    /**
     * Handle a given callback class
     * 
     * @param callback the callback object to be handled
     * 
     * @throws UnsupportedCallbackException if the given callback class is not supported
     * @throws IOException if callback handling fails
     */
    protected void handle(
    	AddressCallback callback
    ) throws IOException, UnsupportedCallbackException {
    	this.unsupported(callback);
    }
    
    /**
     * Handle a single calllback
     * <p>
     * This method may be overridden to support additional callback classes 
     * 
     * @param callback the callback to be handled
     */
    protected void handle(
        Callback callback
    ) throws IOException, UnsupportedCallbackException {
    	if(callback instanceof ChoiceCallback){
    		this.handle((ChoiceCallback)callback);
        } 
    	else if (callback instanceof ConfirmationCallback) {
    		this.handle((ConfirmationCallback)callback);
        } 
    	else if (callback instanceof LanguageCallback) {
    		this.handle((LanguageCallback)callback);
        } 
    	else if (callback instanceof NameCallback) {
    		this.handle((NameCallback)callback);
        } 
    	else if (callback instanceof PasswordCallback) {
    		this.handle((PasswordCallback)callback);
        } 
    	else if (callback instanceof TextInputCallback) {
    		this.handle((TextInputCallback)callback);
        } 
    	else if (callback instanceof TextOutputCallback) {
    		this.handle((TextOutputCallback)callback);
        } 
    	else if (callback instanceof AddressCallback) {
    		this.handle((AddressCallback)callback);
        } 
    	else {
    		this.unsupported(callback);
        }
    }
    
    /**
     * Handle all callbacks
     * 
     * @param callbacks the callbacks to be handled
     */
    public void handle(
        Callback[] callbacks
    ) throws IOException, UnsupportedCallbackException {
        for(
            int i = 0;
            i < callbacks.length;
            i++
        ){
        	this.handle(callbacks[i]);
        }
    }
    
    /**
     * Reject a given callback
     * 
     * @param callback unsupported callback
     * 
     * @throws UnsupportedCallbackException
     */
    protected void unsupported(
        Callback callback
    ) throws UnsupportedCallbackException {
        throw new UnsupportedCallbackException(
    		callback,
        	"Unsupported callback class " + callback.getClass().getName()
        );
    }

}
