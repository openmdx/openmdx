/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CmdLineArgs.java,v 1.6 2007/10/10 16:05:52 hburger Exp $
 * Description: Application Framework 
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:05:52 $
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
package org.openmdx.base.application.control;


import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.openmdx.kernel.text.StringBuilders;



/**
 * CmdLineArgs holds the parsed command line arguments.
 */
	
public class CmdLineArgs 
{
	public CmdLineArgs()
	{
		m_argsRaw     = null;
		m_args        = null;
		m_freeArgs    = null;
	}
	

	/**
	 * Checks wether the argument given by 'id' has been passed in the
	 * command line
	 * The option id has no leading option marker "-", "--"
	 *
	 * E.g.    args.hasArg("k")        // short option -k
	 *         args.hasArg("host")     // long option --host
	 * 
	 * @return	true if arg has been set
	 */
	public boolean  hasArg(String  id)
	{
		if (m_args == null) return false;
		
		for (int ii = 0; ii<m_args.size(); ii++) {
			if ( ((CmdLineArg)m_args.get(ii)).getName().equals(id) ) {
				return true;
			}
		}

		return false;
	}

	
	/**
	 * Returns the first command line attribute value of a possible multi-value 
	 * option id. 
	 * The attribute may be an empty string if the option is defined
	 * with 'optional_argument' and no arguments have been passed.
	 *
	 * The option id has no leading option marker "-", "--"
	 *
	 * E.g.    args.getFirstValue("k")        // short option -k
	 *         args.getFirstValue("host")     // long option --host
	 * 
	 * @return	A string or null
	 */
	public String  getFirstValue(String  id)
	{
		if (m_args == null) return null;

		CmdLineArg  arg;
		
		for (int ii = 0; ii<m_args.size(); ii++) {
			arg = (CmdLineArg)m_args.get(ii);
	
			if ( arg.getName().equals(id) ) {
				return arg.getValue();
			}
		}

		return null;
	}
	

	/**
	 * Returns all command line attribute values for a given option id.
	 * Only arguments with non zero strings are passed back.
	 *
	 * The option id has no leading option marker "-", "--"
	 *
	 * E.g.    args.getValues("k")        // short option -k
	 *         args.getValues("host")     // long option --host
	 * 
	 * @return	A list of Strings
	 */
	public List  getValues(String  id)
	{
		if (m_args == null) return new ArrayList();

		ArrayList   args = new ArrayList();
		CmdLineArg  arg;
		
		for (int ii = 0; ii<m_args.size(); ii++) {
			arg = (CmdLineArg)m_args.get(ii);
	
			if ( arg.getName().equals(id) ) {
				args.add(arg.getValue());
			}
		}
		
		return args;
	}
	
	
	/**
	 * Returns all command line attribute as Properties
	 * 
	 * <p>Switches:  property-name=name; property-value=(null)
	 * <p>Single value arguments:  property-name=name; property-value=value
	 * <p>Multiple value arguments: property-name=name[ii]; property-value=value
	 * <p>Free arguments: property-name=free-arg[ii]; property-value=value
	 * 
	 * @return	A list of Strings
	 */
	public Properties getAsProperties()
	{
		Properties props = new Properties();
		String      argName;
		CmdLineArg  arg;
		List        argValues;		
		
		HashMap  map = new HashMap();
		
		
		// The switches
		Iterator iter = m_args.iterator();
		while(iter.hasNext()) {
			arg = (CmdLineArg)iter.next();
			if (arg.isSwitch()) {
				props.put(arg.getName(), null);			
			}else{
				map.put(arg.getName(), null);
			}
		}
		
		// The arguments
		iter = map.keySet().iterator();
		while(iter.hasNext()) {
			argName = (String)iter.next();
			argValues = getValues(argName);
			
			if (argValues.size() == 1) {
				// add it in both forms
				props.put(argName, argValues.get(0));
				props.put(argName + "[0]", argValues.get(0));
			}else{
				for(int ii=0; ii<argValues.size(); ii++) {
					props.put(argName + "[" + ii + "]", argValues.get(ii));
				}
			}
		}
		
		// The free arguments
		for(int ii=0; ii<m_freeArgs.size(); ii++) {
			props.put("free-arg[" + ii + "]", m_freeArgs.get(ii));
		}
		
		return props;
	}


	/**
	 * Get the non option (free) arguments
	 * 
	 * @return	A list of Strings
	 */
	public List  getFreeArgs()
	{
		if (m_freeArgs == null) return new ArrayList();

		return (ArrayList)m_freeArgs.clone(); // shallow copy
	}
	


	/**
	 * Returns the argument list as passed in by Java main()
	 * 
	 * @return	The args
	 */
	public List  getRawArgs()
	{
		if (m_argsRaw == null) return new ArrayList();

		return (ArrayList)m_argsRaw.clone();  // shallow copy
	}
	
	
	/**
	 * Returns a string representation of the parsed arguments
	 * 
	 * @return	A string
	 */
	public String  toString()
	{
		int           ii;
		String        endl = System.getProperty("line.separator");		
		CmdLineArg    arg; 
        CharSequence  buf  = StringBuilders.newStringBuilder(
            2048
		// Long options
		).append(
            "Long options: "
        ).append(
            endl
        );
		if (m_args != null) {
			for (ii = 0; ii<m_args.size(); ii++) {
				arg = (CmdLineArg)m_args.get(ii);
				if (arg.getName().length() > 1) {
					StringBuilders.asStringBuilder(
                        buf
                    ).append(
                        "     --"
                    ).append(
                        arg.toString()
                    ).append(
                        endl
                    );
				}
			}
		}
		
		// Short options
        StringBuilders.asStringBuilder(
            buf
        ).append(
            "Short options: "
        ).append(
            endl
        );
		if (m_args != null) {
			for (ii = 0; ii<m_args.size(); ii++) {
				arg = (CmdLineArg)m_args.get(ii);
				if (arg.getName().length() == 1) {
                    StringBuilders.asStringBuilder(
                        buf
                    ).append(
                        "     -"
                    ).append(
                        arg.toString()
                    ).append(
                        endl
                    );
				}
			}
		}
					
		// Free args
        StringBuilders.asStringBuilder(
            buf
        ).append(
            "Non option args: "
        ).append(
            endl
        );
		if (m_freeArgs != null) {
			for(ii=0; ii<m_freeArgs.size(); ii++) {
                StringBuilders.asStringBuilder(
                    buf
                ).append(
                    "     "
                ).append(
                    m_freeArgs.get(ii)
                ).append(
                    endl
                );
			}
		}
	
		return buf.toString();
	}
	
	
	/**
	 * Set the raw command line arguments
	 * 
	 * @param	args			The arguments from main()
	 */
	public void setRawArgs(String args[])
	{
		m_argsRaw = new ArrayList();
		
		for(int ii=0; ii<args.length; ii++) m_argsRaw.add(args[ii]);
	}
	

	/**
	 * Set the parsed command line arguments
	 * 
	 * @param	parsedArgs		The parsed arguments a list of objects of class CmdLineArg
	 * @param	parsedFreeArgs	The parsed free arguments a list of objects of class String
	 */
	public void setParsedArgs(
		ArrayList	parsedArgs,
		ArrayList	parsedFreeArgs)
	{
		m_args     = (ArrayList)parsedArgs.clone();      // shallow copy
		m_freeArgs = (ArrayList)parsedFreeArgs.clone();  // shallow copy
		
	}
	

	/**
	 * Reads additional arguments from an external argument file, and
	 * mergers it with current raw args
	 * 
	 * @param   fileName   an argument file
	 * @return  true on successful reading the file
	 */
	public boolean  readArgumentFile(String  fileName)
	{
		if ( new File(fileName).exists() ) {
			try {
				final FileReader file = new FileReader (fileName);
				final BufferedReader buffer = new BufferedReader (file);
				final StreamTokenizer tokenizer = new StreamTokenizer (buffer);
				tokenizer.resetSyntax ();
				tokenizer.wordChars ('\u0021', '\u00ff');
				tokenizer.quoteChar ('\''); tokenizer.quoteChar ('"');
				tokenizer.commentChar ('#');
				while (tokenizer.nextToken () != StreamTokenizer.TT_EOF) {
					if (tokenizer.sval != null) m_argsRaw.add (tokenizer.sval);
				}
				buffer.close(); 
				file.close(); 
				
				return true;
			} catch (EOFException e) {
			    // ignore
			} catch (IOException e) {
				return false;
			}
		}
		
		return false;
	}
	
	
	/** Raw arguments (objects of class String) */
	private ArrayList		m_argsRaw;

	/** The parsed free command line arguments (objects of class String) */
	private ArrayList		m_freeArgs;
	
	/** The parsed command line options with arguments (objects of class CmdLineArg) */
	private ArrayList		m_args;
}


