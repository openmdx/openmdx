/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: StandardErrLoggingMechanism.java,v 1.3 2007/10/10 16:06:08 hburger Exp $
 * Description: Logging
 * Revision:    $Revision: 1.3 $
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

import org.openmdx.kernel.log.*;

/**
 * This logging mechanism logs errors to the <code>System.err</code> output 
 * stream. The mechanism synchronizes on the <code>System.err</code> object
 * to synchronize output with other systems (as J2EE containers, ...)
 */
public class StandardErrLoggingMechanism 
	extends AbstractLoggingMechanism
{
	private static StandardErrLoggingMechanism singleton =
		new StandardErrLoggingMechanism();

	protected StandardErrLoggingMechanism() {
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
		return StandardErrLoggingMechanism.singleton;
	}


	/** 
	 * Returns the name of the mechanism. 
	 * 
	 * @return The mechanism name
	 */
	public String getName() { return "StandardErrLoggingMechanism"; }


	/** 
	 * This mechanism accepts statistics logs 
	 * 
	 * @return true to indicate that the mechanism accepts statistics
	 */
	public boolean acceptsStatisticLogs() { return true; }


	/**
	 * Logs a log event.
	 * 
	 * @param log A logger
	 * @param event A log event
	 */
	protected void logEvent(
		Log       log, 
		LogEvent  event) 
	{
		synchronized (System.err) {
			System.err.println(getFormatter().format(event));
			System.err.flush();
		}
	}
}

