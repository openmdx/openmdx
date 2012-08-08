/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LogEventFilter.java,v 1.4 2007/10/10 16:06:06 hburger Exp $
 * Description: Log Event Filter
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:06 $
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
package org.openmdx.kernel.log;



public class LogEventFilter
{
	/**
	 * The default log event filter accepts all log events
	 */
	public LogEventFilter()
	{
	    super();
	}


	/**
	 * Create a log event filter that accepts specific log events.
     *
     * @param logLevel      Accept all log events with a log level lower
     *                       than the specified level
     * @param performance   Accept/Reject performance log events
     * @param statistics    Accept/Reject statistics log events
     * @param notification  Accept/Reject notification log events
	 */
	public LogEventFilter(
		int     logLevel,
		boolean performance,
		boolean statistics,
		boolean notification)
	{
		this.logLevel     = logLevel;
		this.performance  = performance;
		this.statistics   = statistics;
		this.notification = notification;
	}


	public void setLoggingLevel(int level) { this.logLevel = level; }
	public void setPerformance(boolean val) { this.performance = val; }
	public void setStatistics(boolean val) { this.statistics = val; }
	public void setNotification(boolean val) { this.notification = val; }

	public int     getLoggingLevel() { return this.logLevel; }
	public boolean getPerformance() { return this.performance; }
	public boolean getStatistics() { return this.statistics; }
	public boolean getNotification() { return this.notification; }


	private int     logLevel     = LogLevel.LOG_LEVEL_MAX;
	private boolean performance  = true;
	private boolean statistics   = true;
	private boolean notification = true;
}
