/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: PluggableLoggingMechanismProxy.java,v 1.3 2007/10/10 16:06:08 hburger Exp $
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


import org.openmdx.kernel.log.LogEvent;
import org.openmdx.kernel.log.ManageableMechanism;
import org.openmdx.kernel.log.PluggableLoggingMechanism;




/**
 * This is the proxy mechanism for pluggable logging mechanisms. It
 * decouples PluggableLoggingMechanism from the logging framework.
 *
 * @author Juerg Lang
 */

public class PluggableLoggingMechanismProxy 
	extends AbstractLoggingMechanism 
	implements ManageableMechanism
{

	public PluggableLoggingMechanismProxy(
		String						logName,
		PluggableLoggingMechanism 	mechanism)
	{
		this.mechanismImpl = mechanism;

		if (mechanism != null) {
			this.acceptsStatisticLogs   = mechanism.acceptsStatistics();
			this.acceptsPerformanceLogs = mechanism.acceptsPerformance();
			this.acceptsStandardLogs    = mechanism.acceptsStandard();
		}else{
			this.acceptsStatisticLogs   = false;
			this.acceptsPerformanceLogs = false;
			this.acceptsStandardLogs    = false;
		}

	}


	/**
	 * Logs a log event
     *
     * @param log the log object
     * @param event the log event
     */
	protected void logEvent(
		Log       log, 
		LogEvent  event)
	{
		if (this.mechanismImpl == null)
			return;

		this.mechanismImpl.log(event);
	}


	/**
	 * Return the name of the mechanism.
	 *
	 * @return   a string
	 */
	public String getName()
	{
		if (this.mechanismImpl != null) {
			return this.mechanismImpl.getMechanismName();
		}

		return "N/A";
	}


	/**
	 * Puts out a message that a log was opened on this mechanism
	 *
	 * @param log a log object
	 * @param loggingLevel a logging level
	 */
	protected void notifyLogOpened(
		Log log, 
		int loggingLevel)
	{
	    //
	}


	/**
	 * Puts out a message that a log was closed on this mechanism
	 *
	 * @param  log a log object
	 */
	protected void notifyLogClosed(
		Log log)
	{
	    //
	}


	/**
	 * Puts out a message that a log changed its logging level
	 *
	 * @param  log - a log object
	 * @param loggingLevel - a logging level
	 */
	protected void notifyLoggingLevelChange(
		Log log, 
		int loggingLevel)
	{
		if (acceptsNotificationLogs()) {
			if (this.mechanismImpl != null) {
				this.mechanismImpl.notifyLoggingLevelChange(loggingLevel);
			}
		}
	}


	/**
	 * Puts out a message that a log changed its performance level
	 *
	 * @param  log - a log object
	 * @param state - the new state
	 */
	protected void notifyLoggingPerformanceChange(
		Log     log, 
		boolean state)
	{
		if (acceptsNotificationLogs()) {
			if (this.mechanismImpl != null) {
				this.mechanismImpl.notifyLoggingPerformanceChange(state);
			}
		}
	}

	/**
	 * Puts out a message that a log changed its statistics level
	 *
	 * @param  log - a log object
	 * @param state - the new state
	 */
	protected void notifyLoggingStatisticsChange(
		Log     log, 
		boolean state)
	{
		if (acceptsNotificationLogs()) {
			if (this.mechanismImpl != null) {
				this.mechanismImpl.notifyLoggingStatisticsChange(state);
			}
		}
	}

    /**
     * Checks if the mechanisms accepts standard logs.
     * CRITICAL_ERROR ... TRACE level
     * The default is to accept standard logs (true).
     *
     * @return  true if log accepts standard records
     */
    public boolean acceptsStandardLogs()
	{
		// buffered for performance reasons (see constructor)
		return this.acceptsStandardLogs;
	}

    /**
     * Checks if the mechanisms accepts statistics logs.
     * The default is to reject statistics (false).
     *
     * @return  true if log accepts statistic records
     */
    public boolean acceptsStatisticLogs()
	{
		// buffered for performance reasons (see constructor)
		return this.acceptsStatisticLogs;
	}

    /**
     * Checks if the mechanisms accepts performance logs.
     * The default is to accept perfromance logs (true).
     *
     * @return  true if log accepts performance records
     */
    public boolean acceptsPerformanceLogs()
	{
		// buffered for performance reasons (see constructor)
		return this.acceptsPerformanceLogs;
	}




	/** The real implementation of the mechanism */
	private PluggableLoggingMechanism	mechanismImpl;

	/** Reflects whether the mechanism accepts statistics logs */
	private boolean acceptsStatisticLogs;

	/** Reflects whether the mechanism accepts perfromance logs */
	private boolean acceptsPerformanceLogs;

	/** Reflects whether the mechanism accepts standard logs */
	private boolean acceptsStandardLogs;
}

