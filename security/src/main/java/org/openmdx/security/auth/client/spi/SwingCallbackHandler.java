/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: Swing Based Callback Handler
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.security.auth.client.spi;

import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Swing Based Callback Handler
 */
public class SwingCallbackHandler 
	implements Runnable, CallbackHandler, SwingCallbackContext {
	
	/**
	 * Constructor 
	 */
	public SwingCallbackHandler(
		ResourceBundle resourceBundle
	) {
		this.resourceBundle = resourceBundle;
		this.handler = new ReentrantLock();
		this.handled = this.handler.newCondition(); 
	}

	private final ResourceBundle resourceBundle;
	private final Lock handler;
	private final Condition handled; 
	
	private JFrame frame = null;
	private Callback[] callbacks = null;
	private Exception exception = null;
			
	//------------------------------------------------------------------------
	// Implements CallbackHandler
	//------------------------------------------------------------------------

	public void handle(
		Callback[] callbacks
	) throws IOException, UnsupportedCallbackException {
    	this.handler.lock();
    	try {
			this.callbacks = callbacks;
			this.exception = null;
			SwingUtilities.invokeLater(this);
			this.handled.awaitUninterruptibly();
			if(this.exception instanceof UnsupportedCallbackException) {
				throw (UnsupportedCallbackException)this.exception;
			} 
			else if (this.exception instanceof IOException) {
				throw (IOException)this.exception;
			}
		} 
    	finally {
			this.callbacks = null;
        	this.handler.unlock();
		}
	}

	
	//------------------------------------------------------------------------
	// Implements Runnable
	//------------------------------------------------------------------------
	
	public void run() {
    	this.handler.lock();
        try {
			this.frame = new JFrame(this.toLocalizedString("LOGIN_WINDOW"));
			new SwingCallbackPanel(this);
		} 
        catch (UnsupportedCallbackException exception) {
        	this.signalFailure(exception);
        } 
        finally {
        	this.handler.unlock();
        }
	}

	
	//------------------------------------------------------------------------
	// Implements SwingCallbackContext
	//------------------------------------------------------------------------
	
	/**
	 * 
	 * @param exception
	 */
	private void signalHandled(
		Exception exception
	){
    	this.handler.lock();
		try {
			this.exception = exception;
			this.frame.dispatchEvent(new WindowEvent(this.frame, WindowEvent.WINDOW_CLOSING));
            this.handled.signal();
		} 
		finally {
        	this.handler.unlock();
		}
	}

	/**
	 * Signals that the callback handler's <code>handle()</code> method should return
	 */
	public void signalReturn(
	){
		this.signalHandled(null);
	}

	/**
	 * Signals that callback handler's <code>handle()</code> method should fail
	 *
	 * @param exception the exception 
	 */
	public void signalFailure(
		UnsupportedCallbackException exception
	){
		this.signalHandled(exception);
	}
	
	/**
	 * Signals that callback handler's <code>handle()</code> method should fail
	 *
	 * @param exception the exception 
	 */
	public void signalFailure(
		IOException exception
	){
		this.signalHandled(exception);
	}
	
	/**
	 * Retrieve the controlling frame
	 * 
	 * @return the controlling frame
	 */
	public JFrame getFrame(
	){
		return this.frame;
	}
		
	/**
	 * Retrieve the callbacks to be handled
	 * 
	 * @return the callbacks to be handled
	 */
	public Callback[] getCallbacks(
	){
		return this.callbacks;
	}
	
	/**
	 * Internalization support
	 * 
	 * @param string the string to be localized
	 * 
	 * @return the localized String; or the original one
	 * if no localization exists.
	 */
	public String toLocalizedString(
		String string
	){
		if(this.resourceBundle == null) {
			return string;
		} 
		else try {
			return this.resourceBundle.getString(string);
		} 
		catch (MissingResourceException exception) {
			return string;
		}
	}

}
