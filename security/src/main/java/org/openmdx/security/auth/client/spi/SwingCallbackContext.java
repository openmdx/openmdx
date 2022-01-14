/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: Swing Callback Context
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.JFrame;

/**
 * Swing Callback Context
 */
public interface SwingCallbackContext {

	/**
	 * Signals that the callback handler's <code>handle()</code> method should return
	 */
	void signalReturn(
	);

	/**
	 * Signals that callback handler's <code>handle()</code> method should fail
	 *
	 * @param exception the exception 
	 */
	void signalFailure(
		UnsupportedCallbackException exception
	);
	
	/**
	 * Signals that callback handler's <code>handle()</code> method should fail
	 *
	 * @param exception the exception 
	 */
	void signalFailure(
		IOException exception
	);
	
	/**
	 * Retrieve the controlling frame
	 * 
	 * @return the controlling frame
	 */
	JFrame getFrame(
	);

	/**
	 * Retrieve the callbacks to be handled
	 * 
	 * @return the callbacks to be handled
	 */
	Callback[] getCallbacks(
	);
	
	/**
	 * Internationalization support
	 * 
	 * @param key a key
	 * 
	 * @return the String for the corresponding key
	 */
	String toLocalizedString(
		String key
	);
	
}
