/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Launch
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2013, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.tools.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Launch
 */
public class Launch {

	/**
	 * Expand argument files and launch the specified class
	 * 
	 * @param arguments
	 * 
	 * @throws Exception 
	 */
	public static void main(
		String[] arguments
	) throws Throwable {
		int classNameIndex = getClassNameIndex (arguments);
		Method main = Class.forName(
			arguments[classNameIndex],
			true,
			Thread.currentThread().getContextClassLoader()
		).getMethod(
			"main", 
			new Class[]{String[].class}
		);
		try {
			main.invoke(
				null, 
				new Object[]{
					expandArgumentFiles(arguments, classNameIndex)
				}
			);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	/**
	 * Find the class name index in the argument list
	 * 
	 * @param arguments
	 * 
	 * @return the class name index
	 */
	private static int getClassNameIndex (
		String[] arguments
	){
		if(arguments != null) {
			for(
				int i = 0;
				i < arguments.length;
		    ){
				if(
						CLASSNAME_OPTION.equals(arguments[i++]) &&
					arguments.length > i
				){
					return i;
				}
			}
		}
		throw new IllegalArgumentException(
	    	Launch.class.getName() + " requires a " + CLASSNAME_OPTION + " <classname> argument sequence"
	    );
	}
	
    /**
     * Replace argument file values by their content
     *  
     * @param arguments raw arguments
     * 
     * @return resolved arguments
     * 
     * @throws IOException 
     */
    private static String[] expandArgumentFiles(
    	String[] source,
    	int classNameIndex
    ) throws IOException{
    	List<String> target = new ArrayList<String>();
    	for(
             int i = 0;
             i < source.length;
             i++
        ) {
    		if(i < classNameIndex - 1 || i > classNameIndex) {
	    		String value = source[i];
	    		if(value.startsWith("@")) {
	    	        File script = new File(value.substring(1));
	    	        try (
	    	            BufferedReader in = new BufferedReader(
	    	                new FileReader(script)
	    	            )
	    	        ){
	    	        	for(
	    	        		String argument = in.readLine();
	    	        		argument != null;
	    	        		argument = in.readLine()
	    	        	) {
	    	        		target.add(argument);
	    	        	}
	    	        }
	    		} else {
	    			target.add(value);
	    		}
    		}
    	}
    	return (String[]) target.toArray(new String[target.size()]);
    }

    /**
     * Class name option
     */
    public static final String CLASSNAME_OPTION = "-classname";
    
}
