/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: PasscodeCallbackHandler.java,v 1.2 2009/03/08 18:52:20 wfro Exp $
 * Description: Passcode Callback Handler
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
import java.io.Serializable;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Passcode Callback Handler
 * <p>
 * A call-back handler to return Username, Password, Passcode and its provider 
 * context.
 */
public class PasscodeCallbackHandler 
	extends AbstractCallbackHandler
	implements Serializable {

	/**
     * Implements <code>Serializable</code>
     */
	private static final long serialVersionUID = 3401353174461784041L;

    /**
     * Constructor
     * 
     * @param username the user name
     * @param password the user's password
     * @param passcode the user's passcode
     */
    public PasscodeCallbackHandler(
        String username,
        String password,
        String passcode
    ){
        this(
        	username, 
        	password == null ? null : password.toCharArray(), 
        	passcode, 
        	null // context
        );
    }

	/**
     * Constructor
     * 
     * @param username the user name
     * @param password the user's password
     * @param passcode the user's passcode
     * @param context the passcode provider's context
     * @param client TODO
     */
    public PasscodeCallbackHandler(
        String username,
        char[] password,
        String passcode,
        String context
    ){
    	this(
    		username,
    		password,
    		passcode,
    		context,
    		null // tokencodeLength
    	);
    }

	/**
     * Constructor
     * 
     * @param username the user name
     * @param password the user's password
     * @param passcode the user's passcode
     * @param context the passcode provider's context
     * @param tokencodeLength
     */
    public PasscodeCallbackHandler(
        String username,
        char[] password,
        String passcode,
        String context,
        Integer tokencodeLength
    ){
        this.username = username;
        this.password = password;
        this.passcode = passcode;
        this.context = context;
        this.tokencodeLength = tokencodeLength;
    }

    
    /**
     * Constructor
     * 
     * @param username
     * @param password
     * @param pin
     * @param tokencode
     * @param context
     */
    public PasscodeCallbackHandler(
        String username,
        char[] password,
        char[] pin,
        String tokencode,
        String context
    ){
    	this(
    		username,
    		password,
    		PasscodeCallbackHandler.toPasscode(pin, tokencode),
    		context,
    		tokencode == null ? 
    			ZERO : 
    			new Integer(tokencode.length())
    	);
    }
    
    /**
     * @serial
     */
    private final String username;
    
    /**
     * @serial
     */
    private final char[] password;
    
    /**
     * @serial
     */
    private final String passcode;

    /**
     * @serial
     */
    private final String context;

    /**
     * @serial
     */
    private final Integer tokencodeLength;
    
    private static final Integer ZERO = new Integer(0);
    
    /**
     * Calculates the passcode
     * 
     * @param pin
     * @param tokencode
     * @return
     */
    private static String toPasscode(
        char[] pin,
        String tokencode
    ){
    	return pin == null ? (
    		tokencode == null ? null : tokencode
    	) : new String(pin) + (
    		tokencode == null ? "" : tokencode
    	);
    }
       
    /**
     * Tells whether a prompt starts with a given token
     * 
     * @param prompt
     * @param token
     * @return
     */
    private static boolean matches(
    	String prompt,
    	String token
    ){
    	return 
	    	prompt != null &&
	    	prompt.startsWith(token);
    }
    
	protected void handle(
		NameCallback callback
	) throws IOException, UnsupportedCallbackException {
    	if(PasscodeCallbackHandler.matches(callback.getPrompt(),StandardCallbackPrompts.USERNAME)) {
	        callback.setName(this.username);
    	} 
    	else {
    		this.unsupported(callback, callback.getPrompt());
    	}
	}

	protected void handle(
		PasswordCallback callback
	) throws IOException, UnsupportedCallbackException {
    	if(PasscodeCallbackHandler.matches(callback.getPrompt(),StandardCallbackPrompts.PASSWORD)) {
	        callback.setPassword(this.password);
    	} 
    	else if(
    		this.tokencodeLength!= null &&
    		PasscodeCallbackHandler.matches(callback.getPrompt(),StandardCallbackPrompts.PIN)
    	) {
	        callback.setPassword(
	        	this.passcode == null || this.passcode.length() == this.tokencodeLength.intValue() ?
	        		null :
        			this.passcode.substring(0, this.passcode.length() - this.tokencodeLength.intValue()).toCharArray()
	        );
		} 
    	else {
    		this.unsupported(callback, callback.getPrompt());
		}
	}

	protected void handle(
		TextInputCallback callback
	) throws IOException, UnsupportedCallbackException {
        if(StandardCallbackPrompts.PASSCODE.equals(callback.getPrompt())){
            callback.setText(this.passcode);
        } 
        else if(
        	this.tokencodeLength != null &&
        	StandardCallbackPrompts.TOKENCODE.equals(callback.getPrompt())
        ){
            callback.setText(
            	this.passcode == null || this.passcode.length() < this.tokencodeLength.intValue() ?
            		null :
            		this.passcode.substring(this.passcode.length() - this.tokencodeLength.intValue())
            );
        } 
        else if (StandardCallbackPrompts.CONTEXT.equals(callback.getPrompt())){
            callback.setText(this.context);
		} 
        else {
        	this.unsupported(callback, callback.getPrompt());
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
