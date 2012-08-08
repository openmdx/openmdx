/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UniqueFileLoggingMechanism.java,v 1.1 2008/03/21 18:22:03 hburger Exp $
 * Description: Logging
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:22:03 $
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
 * This logging mechanism is the simplest implementation of a file logging
 * mechanism.  It is designed to have one log file per log, with the name
 * of the log as part of the file name.  It doesn't do anything fancy like
 * rolling over to a new file when a date changes, it appends all messages
 * to the same file name if already present.
 */
public class UniqueFileLoggingMechanism 
	extends AbstractFileLoggingMechanism 
{


	protected UniqueFileLoggingMechanism() {
	    super();
	}

	
	/**
	 * Returns the mechanism object. The mechanism is not shared so it returns 
	 * each time a new object. 
	 * 
	 * @return A mechanism
	 */
	public static AbstractLoggingMechanism getInstance()
	{
		return new UniqueFileLoggingMechanism();
	}


	/** 
	 * This file mechanism is not shared
	 * 
	 * @return false to indicate that the mechanism is not shared
	 */
    protected boolean isSharedLog() { return false; }


	/** 
	 * This file mechanism is not dated
	 * 
	 * @return false to indicate that the mechanism is not dated
	 */
    protected boolean isDatedLog() { return false; }


	/** 
	 * Returns the name of the mechanism. 
	 * 
	 * @return The mechanism name
	 */
	public String getName() { return "UniqueFileLoggingMechanism"; }

}


