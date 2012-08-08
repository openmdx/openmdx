/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: BootstrapLogger.java,v 1.2 2004/04/02 16:59:04 wfro Exp $
 * Description: A simple bootstrap logger
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/04/02 16:59:04 $
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


import java.util.ArrayList;
import java.util.Date;

import org.openmdx.kernel.log.LogEvent;
import org.openmdx.kernel.log.LogLevel;


/**
 * The Bootstrap logger collects log messages while bootstrapping the logging
 * subsystem.
 * 
 * <p>The controlling system must take the responibilty that this logger is
 * closed after bootstrapping.
 */
public class BootstrapLogger 
{

	private static class Event
	{
		public Event(
			Object obj,
			String method,
			String message,
			int    logLevel)	
		{
			this.obj      = obj;
			this.method   = method;
			this.message  = message;
			this.lineNr   = 0;
			this.date     = new Date();
			this.logLevel = logLevel;
		}
		
		public final Object obj;
		public final String method;
		public final int    lineNr;
		public final Date   date;
		public final String message;
		public final int    logLevel;
	}
	
	
	
	/**
	 * Creates a new bootstrap logger to an existing main logger 
	 * (AppLog, SysLog, ...)
	 * 
	 * @param logger  The associated main logger
	 */
	BootstrapLogger(
		Log logger)
	{
		this.logger = logger;
		this.open = true;
	}


	/**
	 * Logs a critical error message.
	 *
	 * @param  obj  The object that logs the message
	 * @param  method  The method that logs the message
	 * @param  message  a log message
	 */
	public void logCriticalError(
		Object obj,
		String method,
		String message)
	{
		log(obj, method, message, LogLevel.LOG_LEVEL_CRITICAL_ERROR);
	}


	/**
	 * Logs an error message.
	 *
	 * @param  obj  The object that logs the message
	 * @param  method  The method that logs the message
	 * @param  message  a log message
	 */
	public void logError(
		Object obj,
		String method,
		String message)
	{
		log(obj, method, message, LogLevel.LOG_LEVEL_ERROR);
	}


	/**
	 * Logs a warning message.
	 *
	 * @param  obj  The object that logs the message
	 * @param  method  The method that logs the message
	 * @param  message  a log message
	 */
	public void logWarning(
		Object obj,
		String method,
		String message)
	{
		log(obj, method, message, LogLevel.LOG_LEVEL_WARNING);
	}


	/**
	 * Logs an info message.
	 *
	 * @param  obj  The object that logs the message
	 * @param  method  The method that logs the message
	 * @param  message  a log message
	 */
	public void logInfo(
		Object obj,
		String method,
		String message)
	{
		log(obj, method, message, LogLevel.LOG_LEVEL_INFO);
	}


	/**
	 * Logs a detail message.
	 *
	 * @param  obj  The object that logs the message
	 * @param  method  The method that logs the message
	 * @param  message  a log message
	 */
	public void logDetail(
		Object obj,
		String method,
		String message)
	{
		log(obj, method, message, LogLevel.LOG_LEVEL_DETAIL);
	}


	/**
	 * Logs a trace message.
	 *
	 * @param  obj  The object that logs the message
	 * @param  method  The method that logs the message
	 * @param  message  a log message
	 */
	public void logTrace(
		Object obj,
		String method,
		String message)
	{
		log(obj, method, message, LogLevel.LOG_LEVEL_TRACE);
	}


	/**
	 * Logs a notification message.
	 *
	 * @param  obj  The object that logs the message
	 * @param  method  The method that logs the message
	 * @param  message  a log message
	 */
	public void logNotification(
		Object obj,
		String method,
		String message)
	{
		log(obj, method, message, LogLevel.LOG_NOTIFICATION);
	}


	/**
	 * Logs a trace message.
	 *
	 * @param  obj  The object that logs the message
	 * @param  method  The method that logs the message
	 * @param  message  a log message
	 */
	private void log(
		Object obj,
		String method,
		String message,
		int    logLevel)
	{
		if (isOpen()) {
			synchronized(this.events) {
				this.events.add(
					new Event(obj, method, message, logLevel));
			}
		}

		// For debugging purposes send a copy to the debug logger
		if (LogProperties.isDebuggingEnabled()) {
			LogLog.log(
				logLevel,
				obj.getClass(),
				method,
				"",
				message);
		}
	}


	/**
	 * Flushes the collected log events to the log system
	 */
	synchronized
	public void flush()
	{
		String logSource = "Log-Framework";
		
		synchronized(this.events) {			
			for(int ii=0; ii<events.size(); ii++) {				
				Event ev = (Event)events.get(ii);
							
				// create a log event ...
				LogEvent logEvent= new LogEvent(
										this.logger.getName(),
										ev.date,
										ev.logLevel,
										this.logger.getConfig().getConfigName(),
										this.logger.getConfig().getApplicationId(),
										logSource,
										this.logger.getHostName(),
										null,  // process ID not available
										Thread.currentThread().getName(),
										ev.obj.getClass().getName(),
										ev.method,
										ev.lineNr,
										"",
										ev.message);
	
				// ... and log it
				switch (logEvent.getLoggingLevel()) {
					case LogLevel.LOG_LEVEL_CRITICAL_ERROR:
					case LogLevel.LOG_LEVEL_ERROR:
					case LogLevel.LOG_LEVEL_WARNING:
						// log allways even if the log level is not active
						this.logger.logEvent(logEvent, true);
						break;
						
					case LogLevel.LOG_LEVEL_INFO:
					case LogLevel.LOG_LEVEL_DETAIL:
					case LogLevel.LOG_LEVEL_TRACE:
					case LogLevel.LOG_NOTIFICATION:
					case LogLevel.LOG_PERFORMANCE:
					case LogLevel.LOG_STATISTICS:
						// log only if log level is active
						this.logger.logEvent(logEvent, false);
						break;
				}
			}

			events.clear();
		}
	}
	
	

	/**
	 * Closes the bootlogger. 
	 */
	public void close()
	{
		this.open = false;
	}
	

	/**
	 * Checks if the boot logger is open. 
	 * 
	 * @return true if open
	 */
	public boolean isOpen()
	{
		return this.open;
	}




	/** The associated logger */
	private final Log  logger;

	/** Reflects the open state */
	private boolean open;

	/** Collected bootstrap log events */
	private final ArrayList  events = new ArrayList();
}
