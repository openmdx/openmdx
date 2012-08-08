/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: TerminalCallbackHandler.java,v 1.2 2009/03/08 18:52:20 wfro Exp $
 * Description: Terminal Callback Handler
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/08 18:52:20 $
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.openmdx.security.auth.callback.AbstractCallbackHandler;



/**
 * Call-back Handler for Console I/O
 */
public class TerminalCallbackHandler extends AbstractCallbackHandler {

    /**
     * Constructor
     */
    public TerminalCallbackHandler(
    ) {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }

    /**
     * To accept the console input
     */
    protected final BufferedReader reader;
    
    /**
     * Accept an single value
     * 
     * @param prompt
     * @param defaultValue
     * 
     * @return the given value; or null
     * @throws IOException
     */
    protected String accept(
        String prompt,
        String defaultValue
    ) throws IOException{
        System.out.print(
            '\t' + prompt +
            (defaultValue == null ? "" : " [" + defaultValue + "]") + 
            ": "
        );
        String reply = this.reader.readLine();
        return "".equals(reply) ? null : reply;
    }
    
    @Override
	protected void handle(
		NameCallback callback
	) throws UnsupportedCallbackException, IOException {
        String value = this.accept(callback.getPrompt(), callback.getDefaultName());
        if(value != null){
            callback.setName(value);
        }
	}

	@Override
	protected void handle(
		PasswordCallback callback
	) throws UnsupportedCallbackException, IOException {
        String value = this.accept(callback.getPrompt(), null);
        if(value != null){
            callback.setPassword(value.toCharArray());
        }
	}

	@Override
	protected void handle(
		TextInputCallback callback
    ) throws IOException, UnsupportedCallbackException {
        String value = this.accept(callback.getPrompt(), callback.getDefaultText());
        if(value != null){
            callback.setText(value);
        }
	}

	@Override
	protected void handle(
		TextOutputCallback callback
    ) throws IOException, UnsupportedCallbackException {
		switch(callback.getMessageType()) {
			case TextOutputCallback.ERROR:
				System.out.print("\tERROR: ");
			case TextOutputCallback.INFORMATION:
				System.out.print("\tINFORMATION: ");
			case TextOutputCallback.WARNING:
				System.out.print("\tWARNING: ");
		}
		System.out.println(callback.getMessage());
	}

	@Override
	public void handle(
		Callback[] callbacks
	) throws IOException, UnsupportedCallbackException {
		System.out.println("LOGIN");
		super.handle(callbacks);
	}

}
