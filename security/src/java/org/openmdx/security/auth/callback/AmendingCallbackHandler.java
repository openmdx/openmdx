/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: AmendingCallbackHandler.java,v 1.1 2007/11/26 14:04:34 hburger Exp $
 * Description: Amending Callback Handler
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/11/26 14:04:34 $
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

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Amending Callback Handler
 */
public final class AmendingCallbackHandler implements CallbackHandler {

	/**
	 * Constructor
	 * 
	 * @param delegate
	 */
	public AmendingCallbackHandler (
		CallbackHandler delegate
	){
		this.delegate = delegate;
	}

	/**
	 * Constructor
	 * 
	 * @param delegate
	 */
	public AmendingCallbackHandler (
		Callback[] prolog,
		CallbackHandler delegate,
		Callback[] epilog
	){
		this(delegate);
		this.setProlog(prolog);
		this.setEpilog(epilog);
	}
	
	/**
	 * The delegate
	 */
	private final CallbackHandler delegate;
	
	/**
	 * 
	 */
	private Callback[] prolog = NONE;
	
	/**
	 * 
	 */
	private Callback[] epilog = NONE;
	
	/**
	 * In case no prolog or epilog is required
	 */	
	private final static Callback[] NONE = new Callback[0]; 
		
	/**
	 * @param prolog the prolog to set
	 */
	public void setProlog(Callback[] prolog) {
		this.prolog = prolog == null ? NONE : prolog;
	}

	/**
	 * @param epilog the epilog to set
	 */
	public void setEpilog(Callback[] epilog) {
		this.epilog = epilog == null ? NONE : epilog;
	}

	/**
	 * Amend the callback array before passing them to the delegate.
	 * 
	 * @param callbacks the original callback array
	 */
	public void handle(
		Callback[] callbacks
	) throws IOException, UnsupportedCallbackException {
		Callback[] amendedCallbacks = new Callback[
	         this.prolog.length + callbacks.length + this.epilog.length
		];
		System.arraycopy(
			this.prolog, 0, 
			amendedCallbacks, 0, 
			this.prolog.length
		);
		System.arraycopy(
			callbacks, 0, 
			amendedCallbacks, this.prolog.length, 
			callbacks.length
		);
		System.arraycopy(
			this.epilog, 0, 
			amendedCallbacks, this.prolog.length + callbacks.length, 
			this.epilog.length
		);
		this.delegate.handle(amendedCallbacks);
	}

}
