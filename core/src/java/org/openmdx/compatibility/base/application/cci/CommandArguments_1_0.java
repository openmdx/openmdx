/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CommandArguments_1_0.java,v 1.1 2004/07/15 15:56:35 hburger Exp $
 * Description: CommandArguments_1_0 interface
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/07/15 15:56:35 $
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
package org.openmdx.compatibility.base.application.cci;

/**
 * The command interface.
 * <p>
 *  Options may either be passed as command argument or in argument files.
 *  <ul>
 *   <li>
 *    The command argument syntax is as following:
 *    <pre>
 *		Arguments			= {ArgumentFileOption | Argument}
 *		ArgumentFileOption	= ArgumentFilePrefix ArgumentFileName 
 *		ArgumentFilePrefix	= "--argument-file=" 
 *    </pre>
 *   </li>
 *   <li>
 *    The file argument syntax is as following:
 *    <pre>
 *		ArgumentFile	= {ArgumentLine EndOfLine} EndOfFile
 *		ArgumentLine	= {Argument} "#" Comment 
 *    </pre>
 *   </li>
 *  </ul>
 * </p>
 */
public interface CommandArguments_1_0 
{ 

	/**
	 * The argument file option
	 */
	final static String ARGUMENT_FILE = "argument-file";
	
	/**
	 * Get raw arguments
	 * 
	 * @return        Raw arguments
	 */
	public String [] getRawValues ();
	
	/**
	 * Get all arguments passed either as command argument or in an 
	 * argument file.
	 * 
	 * @return        All arguments
	 */
	public String [] getValues ();
	
}
