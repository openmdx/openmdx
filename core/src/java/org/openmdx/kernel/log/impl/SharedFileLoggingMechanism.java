/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SharedFileLoggingMechanism.java,v 1.4 2007/10/10 16:06:08 hburger Exp $
 * Description: Logging
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:08 $
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
package org.openmdx.kernel.log.impl;



/**
 * This logging mechanism is shared among all of the different logs
 * which attempt to use it, instead of creating a new log file for every
 * log (which the "Unique" logging mechanisms do).
 */
public class SharedFileLoggingMechanism 
	extends AbstractFileLoggingMechanism 
{

	protected static SharedFileLoggingMechanism singleton =
		new SharedFileLoggingMechanism();


	protected SharedFileLoggingMechanism() {
	    super();
	}
	
	
	/**
	 * Returns the mechanism object. The mechanism is shared so it returns a 
	 * singleton
	 * 
	 * @return the mechanism singleton
	 */
	public static AbstractLoggingMechanism getInstance()
	{
		return SharedFileLoggingMechanism.singleton;
	}


	/** 
	 * This file mechanism is not dated
	 * 
	 * @return false to indicate that the mechanism is not dated
	 */
    protected boolean isDatedLog() { return false; }


	/**
	 * Shared logging needs an additional qualifier on each message
	 * logged, prefix the message with the log name.
	 */
	String loggingPrefix(
		Log log)
	{
		return ( log.getName() );
	}


	/** 
	 * Returns the name of the mechanism. 
	 * 
	 * @return The mechanism name
	 */
	public String getName() { return "SharedFileLoggingMechanism"; }

}


