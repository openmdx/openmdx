/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LogLog.java,v 1.1 2008/03/21 18:22:01 hburger Exp $
 * Description: Technical Logger for the logging subsystem
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:22:01 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
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
package org.openmdx.kernel.log.impl;



import java.text.SimpleDateFormat;
import java.util.Date;

import org.openmdx.compatibility.kernel.log.LogLevel;


/**
 * The LogLog classes provides low level debugging and logging functionality
 * for the logging framework. This debugging facility can be enabled/disbaled. 
 * 
 * <p>The class may be used by the logging framework only. Especially mechanism
 * implementations must use this log facility to avoid the recursion trap for
 * unrecoverable errors.
 *
 * <p>The class is self contained and does not use any other resources from the
 * logging framework (except the logging levels).
 *
 * <p>The log output is written to standard error.
 */
public class LogLog {

    /**
     * Logs a text string at CRITICAL_ERROR_LEVEL.
     *
     * @param  clazz      a class
     * @param  method     a method name
     * @param  logString  a log string (summary)
     * @param  logObj     a log object (detail, to be stringified)
     */
    static 
    public void criticalError(
    			Class<?>  clazz,
    			String method,
    			String logString,
    			Object logObj)
    {
		LogLog.log(
			LogLevel.LOG_LEVEL_CRITICAL_ERROR, clazz, method, logString, logObj);
    }

	/**
     * Logs a text string at ERROR_LEVEL.
     *
     * @param  clazz      a class
     * @param  method     a method name
     * @param  logString  a log string (summary)
     * @param  logObj     a log object (detail, to be stringified)
     */
    static 
    public void error(
    			Class<?>  clazz,
    			String method,
    			String logString,
    			Object logObj)
    {
		LogLog.log(
			LogLevel.LOG_LEVEL_ERROR, clazz, method, logString, logObj);
    }


    /**
     * Logs a text string at WARNING_LEVEL.
     *
     * @param  clazz      a class
     * @param  method     a method name
     * @param  logString  a log string (summary)
     * @param  logObj     a log object (detail, to be stringified)
     */
    static 
    public void warning(
    			Class<?>  clazz,
    			String method,
    			String logString,
    			Object logObj)
    {
		LogLog.log(
			LogLevel.LOG_LEVEL_WARNING, clazz, method, logString, logObj);
    }


    /**
     * Logs a text string at INFO_LEVEL.
     *
     * @param  clazz      a class
     * @param  method     a method name
     * @param  logString  a log string (summary)
     * @param  logObj     a log object (detail, to be stringified)
     */
    static 
    public void info(
    			Class<?>  clazz,
    			String method,
    			String logString,
    			Object logObj)
    {
		LogLog.log(
			LogLevel.LOG_LEVEL_INFO, clazz, method, logString, logObj);
    }


    /**
     * Logs a text string at DETAIL_LEVEL.
     *
     * @param  clazz      a class
     * @param  method     a method name
     * @param  logString  a log string (summary)
     * @param  logObj     a log object (detail, to be stringified)
     */
    static 
    public void detail(
				Class<?>  clazz,
    			String method,
    			String logString,
    			Object logObj)
    {
		LogLog.log(
			LogLevel.LOG_LEVEL_DETAIL, clazz, method, logString, logObj);
    }


    /**
     * Logs a text string at TRACE_LEVEL.
     *
     * @param  clazz      a class
     * @param  method     a method name
     * @param  logString  a log string (summary)
     * @param  logObj     a log object (detail, to be stringified)
     */
    static 
    public void trace(
    			Class<?>  clazz,
    			String method,
    			String logString,
    			Object logObj)
    {
		LogLog.log(
			LogLevel.LOG_LEVEL_TRACE, clazz, method, logString, logObj);
    }


    /**
     * Enable/Disable technical framework logging.
     *
     * @param  on     enable on true
     */
    static 
    public void enable(boolean on)
    {
		LogLog.enabled = on;
    }


	/**
	 * Returns true if the logging is enabled.
	 *
	 * @return true if enabled
	 */
	static 
    public boolean isEnabled()
	{
		return LogLog.enabled;
	}


    /**
     * Log a message.
     *
     * @param  clazz      a class
     * @param  method     a method name
     * @param  logString  a log string (summary)
     * @param  logObj     a log object (detail, to be stringified)
     */
    static 
    public void log(
    			int    _level,
    			Class<?>  clazz,
    			String method,
    			String _logString,
    			Object logObj)
    {
        int level = _level;
        String logString = _logString;
    	// Errors and warnings override the enabled flag!!!
    	switch(level) {
    		case LogLevel.LOG_LEVEL_CRITICAL_ERROR:
    		case LogLevel.LOG_LEVEL_ERROR:
    		case LogLevel.LOG_LEVEL_WARNING:
    			break;
    			
			case LogLevel.LOG_LEVEL_INFO:
			case LogLevel.LOG_LEVEL_DETAIL:
			case LogLevel.LOG_LEVEL_TRACE:
				if (!LogLog.enabled) return;
				break;
				
    		default:
    			if (!LogLog.enabled) return;
    			
    			// correct the bad log level!
				level = LogLevel.LOG_LEVEL_TRACE;
				logString = "(Bad LogLevel '" + level + "' in method"
				            + " LogLevel.log => corrected to LOG_LEVEL_TRACE) " 
				            + logString;
    			break;
    	}

		synchronized(System.err) {
			System.err.println(
						"LogLog|" +
						LogLog.dateFormat.format(new Date()) + "|" +
						"L" + level + "|" +
						Thread.currentThread().getName() + "|" +
						clazz.getName() + "|" +
						method + "|" +
						(logString==null ? "" : logString) + "|" +
						(logObj==null ? "" : logObj.toString()));
		}
    }


    /** Enable/disable state */
	private static boolean enabled = true;


    /** Date formatter */
	private static SimpleDateFormat dateFormat =
	                           new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss.SSS");
}
