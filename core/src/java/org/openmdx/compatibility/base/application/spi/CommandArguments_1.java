/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CommandArguments_1.java,v 1.3 2008/03/21 18:45:21 hburger Exp $
 * Description: Application Framework 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:45:21 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.compatibility.base.application.spi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.cci.CommandArguments_1_0;
import org.openmdx.compatibility.base.application.cci.Manageable_1_0;

/**
 * The application class
 * 
 * @deprecated
 */
public class CommandArguments_1 
	implements CommandArguments_1_0, Manageable_1_0
{ 

	/**
	 * The startup arguments
	 */
	private final String [] rawArguments;

	/**
	 * The startup arguments
	 */
	private String [] arguments;

	/**
	 * Constructor
	 * 
	 * @param	arguments	Command arguments
	 */
	public CommandArguments_1 (
		String [] arguments
	){
		rawArguments = arguments;
	}

	
	//--------------------------------------------------------------------------
	// Implement the Command_1_0 interface
	//--------------------------------------------------------------------------

	/**
	 * Get raw arguments
	 * 
	 * @return        Raw arguments
	 */
	public String [] getRawValues () {
		return rawArguments.clone ();
	}
	
	/**
	 * Get all arguments passed either as command argument or in an 
	 * argument file.
	 * 
	 * @return        All arguments
	 */
	public String [] getValues () {
		return arguments.clone ();
	}

	
	//--------------------------------------------------------------------------
	// Implement the Mangeable_1_0 interface
	//--------------------------------------------------------------------------

	/**
	 * The argument file prefix
	 */
	final static String ARGUMENT_FILE_PREFIX = "--" + ARGUMENT_FILE + '=';
		
    /**
	 * The activate method initializes a layer or component.
	 */
	public void activate(
	) throws Exception, ServiceException {
		ArrayList<String> arguments = new ArrayList<String> (rawArguments.length);
		boolean modified = false;
		for (int index = 0; index < rawArguments.length; index++) {
			final String argument = rawArguments [index];
			if (argument.startsWith (ARGUMENT_FILE_PREFIX)) {
				modified = true;
				final String name = argument.substring (ARGUMENT_FILE_PREFIX.length());
				final FileReader file = new FileReader (name);
				final BufferedReader buffer = new BufferedReader (file);
				final StreamTokenizer tokenizer = new StreamTokenizer (buffer);
				tokenizer.resetSyntax ();
				tokenizer.wordChars ('\u0021', '\u00ff');
				tokenizer.quoteChar ('\''); tokenizer.quoteChar ('"');
				tokenizer.commentChar ('#');
				while (tokenizer.nextToken () != StreamTokenizer.TT_EOF) {
					if (tokenizer.sval != null) arguments.add (tokenizer.sval);
				}
				buffer.close(); 
				file.close(); 
			} else {
				arguments.add (argument);
			}
		}
		this.arguments = modified ? 
			(String []) arguments.toArray(new String [arguments.size()]) : 
			rawArguments;
	}

	/**
 	 * The deactivate method releases a layer or component.
	 */
	public void deactivate() {
	    //
	}
	
}
