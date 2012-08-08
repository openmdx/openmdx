/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: AddressCallbackHandler.java,v 1.2 2009/03/08 18:52:20 wfro Exp $
 * Description: Address Callback Handler
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/08 18:52:20 $
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
package org.openmdx.security.auth.callback;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.openmdx.kernel.security.authentication.callback.AddressCallback;

/**
 * Address Callback Handler
 * <p>
 * The Address Callback Handler is able to handle address callback requests 
 * and delegate the remaining requests to a delegate callback handler. 
 */
public abstract class AddressCallbackHandler 
	implements CallbackHandler {

    /**
     * Constructor
     * 
     * @param request
     * @param delegate
     * 
     * @throws IOException
     */
    protected AddressCallbackHandler(
        CallbackHandler delegate
    ) throws IOException {
    	this.delegate = delegate;
    }
    
    /**
     * 
     */
    private final CallbackHandler delegate;
    
    /**
     * Handle a given callback class
     * 
     * @param callback the callback object to be handled
     * 
     * @throws UnsupportedCallbackException if the given callback class is not supported
     * @throws IOException if callback handling fails
     */
    protected abstract void handle(
    	AddressCallback callback
    ) throws IOException, UnsupportedCallbackException;
    
    /**
     * Tells whether callbacks should be handled or delegated
     * 
     * @return return <code>true</code> if callbacks should be handled
     */
    protected abstract boolean accept(
    );
    
    /**
     * Tells whether a given callback should be handled or delegated
     * 
     * @param callback
     * 
     * @return return <code>true</code> if t he callback should be handled
     */
    protected abstract boolean accept(
    	AddressCallback callback
    );
    
    /**
     * Tells whether a given callback should be handled or delegated
     * 
     * @param callback
     * 
     * @return return <code>true</code> if t he callback should be handled
     */
    private boolean accept(
    	Callback callback
    ){
    	return 
    		callback instanceof AddressCallback &&
    		this.accept((AddressCallback) callback);
    }
    
    /**
     * Handle address callbacks and delegate the remaining ones
     * 
     * @param callbacks
     * 
     * @throws UnsupportedCallbackException if the given callback class is not supported
     * @throws IOException if callback handling fails
     */
	public void handle(
        Callback[] callbacks
    ) throws IOException, UnsupportedCallbackException {
		if(this.accept()) {
			boolean[] handled = new boolean[callbacks.length];
			int remaining = callbacks.length;
			for(
				int i = 0;
				i < callbacks.length;
				i++
			){
				if(handled[i] = this.accept(callbacks[i])) {
					this.handle((AddressCallback)callbacks[i]);
	                remaining--;
				}
			}
	        if(remaining != 0) {
	        	Callback[] remainder;
	        	if(remaining == callbacks.length) {
	        		remainder = callbacks;
	        	} 
	        	else {
	        		remainder = new Callback[remaining];
	        		int delegated = 0;
	        		for(
	    				int i = 0;
	    				i < callbacks.length;
	    				i++
	    			){
	                	if (!handled[i]) {
	                		remainder[delegated++] = callbacks[i];
	        	        }
	                }
	        	}
	        	this.delegate.handle(remainder);
	        }
		} 
		else {
			this.delegate.handle(callbacks);
		}
    }

	/**
     * Reject a given callback
     * 
     * @param callback callback
     * @param prompt unsupported prompt
     * 
     * @throws UnsupportedCallbackException
     */
    protected void unsupported(
        Callback callback,
        String prompt
    ) throws UnsupportedCallbackException {
        throw new UnsupportedCallbackException(
    		callback,
        	"Unsupported prompt for class " + callback.getClass().getName() + ": " + prompt
        );
    }
    
}
