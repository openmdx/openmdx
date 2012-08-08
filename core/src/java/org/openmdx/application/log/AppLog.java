/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AppLog.java,v 1.17 2007/10/10 17:16:04 hburger Exp $
 * Description: Logging
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 17:16:04 $
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
package org.openmdx.application.log;

import java.util.Properties;

import org.openmdx.application.Dependencies;
import org.openmdx.kernel.exception.VersionMismatchException;
import org.openmdx.kernel.log.Config;
import org.openmdx.kernel.log.impl.Log;
import org.openmdx.kernel.log.LogLevel;
import org.openmdx.kernel.log.SysLog;




/**
 * <code>AppLog</code> wraps the Log class to do application-general
 * logging. It provides a singleton accessor, along with lots of
 * handy class method {static} accessors. <p>
 *
 */
public class AppLog extends Log {

    // This class must NEVER be instantiated. Actually RawLog only extends    
    // the class Log to prevent problems with multiple class loaders. 
    // RawLog uses a Log instance singleton by delegation!

    private AppLog() {
        super();
    }



    ///////////////////////////////////////////////////////////////////////////
    //
    // LOG configuration
    //
    ///////////////////////////////////////////////////////////////////////////


	/**
	 * Set the config name that the logs pertain to. Must be called
	 * before doing any logging.
	 *
	 * @param cfgName  a config name
	 */
	public static void setConfigName(String cfgName)
	{
		// Setting a config name is only possible when the config
		// object does not exist and nothing has been logged yet.
		// The application name must be kept in sync with the Config object.
		if (!initialized) {
			if (cfgName == null) return;  // reject null strings
			String configName = cfgName.trim();
			if (configName.length()==0) return; // reject empty strings

			AppLog.configName = configName;
		}else{
			SysLog.error("A config name must be set before any logging " +
			             "takes place", new Exception());
		}
	}


	/**
	 * Set a log source object. The logging framework uses the toString() method
	 * to determine the log source for each log event.
	 * The object may be set at any time.
	 *
	 * <p>
	 * Dynamic log source example:
	 * <code>
	 *
	 * class LogSource
	 * {
	 *   public String toString()
	 *   {
	 * 		return "LogSource-" + System.currentTimeMillis();
	 * 	 }
	 * }
	 *
	 * LogSource logSource = new LogSource();
	 * AppLog.setLogSource(logSource)
	 *
	 * </code>
	 *
	 * <p>
	 * Static log source example:
	 * <code>
	 *
	 * AppLog.setLogSource("LogSource")
	 *
	 * </code>
	 *
	 * @param logSource a log source object
	 */
	public static void setLogSource(Object logSource)
	{
		if (logSource == null) return;

		AppLog.logSource = logSource;
	}


	/**
	 * Sets the log properties [Config-Level-3]. setLogProperties() must be
	 * called before doing any logging.
	 *
	 * @param properties  the log properties
	 */
	public static void setLogProperties(Properties props)
	{
		// Setting log properties is only possible when the config
		// object does not exist and nothing has been logged yet.
		if (!initialized) {
			AppLog.logProperties = props;
		}else{
			SysLog.error("Log properties must be set before any logging " +
			             "takes place", new Exception());
		}
	}


	/**
     * Returns the logger's configuration object
	 *
     * @return  the configuration object
     */
    public static Config getLogConfig()
    {
		if (!initialized) init();

		// return a proxy
        return new Config(singleton.getConfig());
    }


	/**
     * Checks if trace logging is active
     *
     * <p>
     * Applications can use this method before they call AppLog.trace(...) if
     * the log string creation is very time consuming.
     *
     * <pre>
     *      AppLog.trace("Customer created", "First=Mark, Last=Smith");
	 *
     *      if (AppLog.isTraceOn()) {
     *	        summary = expensive_creation();
     *	        detail  = expensive_creation();
     *          AppLog.trace(summary, detail);
     *    	}
     * </pre>
	 *
     * @return  true if trace is active
     */
    public static boolean isTraceOn()
    {
		if (!initialized) init();

        return (singleton.getLoggingLevel() >= LogLevel.LOG_LEVEL_TRACE);
    }


    ///////////////////////////////////////////////////////////////////////////
    //
    // LOG methods
    //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Logs a text string at CRITICAL_ERROR_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @param logObj  
     *         a log object providing detail information. The log object is
     *         stringified using its <code>toString</code> method before getting
     *         logged. The log object may be a null object. If the log object
     *         is a <code>Throwable</code> it's message and stack trace is 
     *         logged.
     * @see #criticalError(String)
     * @see #criticalError(String, Object, int)
     */
    public static void criticalError(
    			String logString,
    			Object logObj)
    {
		if (!initialized) init();

		if (LogLevel.LOG_LEVEL_CRITICAL_ERROR <= singleton.getLoggingLevel()) {
        	singleton.logString(
        	    AppLog.logSource,
        		logString,
        		logObj,
        		LogLevel.LOG_LEVEL_CRITICAL_ERROR,
        		0);
		}
    }

    /**
     * Logs a text string at CRITICAL_ERROR_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @see #criticalError(String, Object)
     * @see #criticalError(String, Object, int)
     */
    public static void criticalError(String logString)
    {
		if (!initialized) init();

		if (LogLevel.LOG_LEVEL_CRITICAL_ERROR <= singleton.getLoggingLevel()) {
        	singleton.logString(
        	    AppLog.logSource,
        		logString,
        		null,
        		LogLevel.LOG_LEVEL_CRITICAL_ERROR,
        		0);
		}
    }

    /**
     * Logs a text string at CRITICAL_ERROR_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @param logObj  
     *         a log object providing detail information. The log object is
     *         stringified using its <code>toString</code> method before getting
     *         logged. The log object may be a null object. If the log object
     *         is a <code>Throwable</code> it's message and stack trace is 
     *         logged.
     * @param callStackOff  
     *         a call stack correction offset. The offset must be a positive 
     *         number: 0, 1, 2, 3, ...
     * @see #criticalError(String)
     * @see #criticalError(String, Object)
     */
    public static void criticalError(
    			String logString,
    			Object logObj,
    			int    callStackOff)
    {
		if (!initialized) init();

		if (LogLevel.LOG_LEVEL_CRITICAL_ERROR <= singleton.getLoggingLevel()) {
        	singleton.logString(
        	    AppLog.logSource,
        		logString,
        		logObj,
        		LogLevel.LOG_LEVEL_CRITICAL_ERROR,
        		callStackOff);
		}
    }

	/**
     * Logs a text string at ERROR_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @param logObj  
     *         a log object providing detail information. The log object is
     *         stringified using its <code>toString</code> method before getting
     *         logged. The log object may be a null object. If the log object
     *         is a <code>Throwable</code> it's message and stack trace is 
     *         logged.
     * @see #error(String)
     * @see #error(String, Object, int)
     */
    public static void error(
    			String logString,
    			Object logObj)
    {
		if (!initialized) init();

		if (LogLevel.LOG_LEVEL_ERROR <= singleton.getLoggingLevel()) {
        	singleton.logString(
        	    AppLog.logSource,
        		logString,
        		logObj,
        		LogLevel.LOG_LEVEL_ERROR,
        		0);
		}
    }

	/**
     * Logs a text string at ERROR_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @see #error(String, Object)
     * @see #error(String, Object, int)
     */
    public static void error(String logString)
    {
		if (!initialized) init();

		if (LogLevel.LOG_LEVEL_ERROR <= singleton.getLoggingLevel()) {
        	singleton.logString(
        	    AppLog.logSource,
        		logString,
        		null,
        		LogLevel.LOG_LEVEL_ERROR,
        		0);
		}
    }

	/**
     * Logs a text string at ERROR_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @param logObj  
     *         a log object providing detail information. The log object is
     *         stringified using its <code>toString</code> method before getting
     *         logged. The log object may be a null object. If the log object
     *         is a <code>Throwable</code> it's message and stack trace is 
     *         logged.
     * @param callStackOff  
     *         a call stack correction offset. The offset must be a positive 
     *         number: 0, 1, 2, 3, ...
     * @see #error(String)
     * @see #error(String, Object)
     */
    public static void error(
    			String logString,
    			Object logObj,
    			int    callStackOff)
    {
		if (!initialized) init();

		if (LogLevel.LOG_LEVEL_ERROR <= singleton.getLoggingLevel()) {
        	singleton.logString(
        	    AppLog.logSource,
        		logString,
        		logObj,
        		LogLevel.LOG_LEVEL_ERROR,
        		callStackOff);
		}
    }

    /**
     * Logs a text string at WARNING_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @param logObj  
     *         a log object providing detail information. The log object is
     *         stringified using its <code>toString</code> method before getting
     *         logged. The log object may be a null object. If the log object
     *         is a <code>Throwable</code> it's message and stack trace is 
     *         logged.
     * @see #warning(String)
     * @see #warning(String, Object, int)
     */
    public static void warning(
    			String logString,
    			Object logObj)
    {
		if (!initialized) init();

		if (LogLevel.LOG_LEVEL_WARNING <= singleton.getLoggingLevel()) {
        	singleton.logString(
        	    AppLog.logSource,
        		logString,
        		logObj,
        		LogLevel.LOG_LEVEL_WARNING,
        		0);
		}
    }

    /**
     * Logs a text string at WARNING_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @see #warning(String, Object)
     * @see #warning(String, Object, int)
     */
    public static void warning(String logString)
    {
		if (!initialized) init();

		if (LogLevel.LOG_LEVEL_WARNING <= singleton.getLoggingLevel()) {
        	singleton.logString(
        	    AppLog.logSource,
        		logString,
        		null,
        		LogLevel.LOG_LEVEL_WARNING,
        		0);
		}
    }

    /**
     * Logs a text string at WARNING_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @param logObj  
     *         a log object providing detail information. The log object is
     *         stringified using its <code>toString</code> method before getting
     *         logged. The log object may be a null object. If the log object
     *         is a <code>Throwable</code> it's message and stack trace is 
     *         logged.
     * @param callStackOff  
     *         a call stack correction offset. The offset must be a positive 
     *         number: 0, 1, 2, 3, ...
     * @see #warning(String)
     * @see #warning(String, Object)
     */
    public static void warning(
    			String logString,
    			Object logObj,
    			int    callStackOff)
    {
		if (!initialized) init();

		if (LogLevel.LOG_LEVEL_WARNING <= singleton.getLoggingLevel()) {
        	singleton.logString(
        	    AppLog.logSource,
        		logString,
        		logObj,
        		LogLevel.LOG_LEVEL_WARNING,
        		callStackOff);
		}
    }


    /**
     * Logs a text string at INFO_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @param logObj  
     *         a log object providing detail information. The log object is
     *         stringified using its <code>toString</code> method before getting
     *         logged. The log object may be a null object. If the log object
     *         is a <code>Throwable</code> it's message and stack trace is 
     *         logged.
     * @see #info(String)
     * @see #info(String, Object, int)
     */
    public static void info(
    			String logString,
    			Object logObj)
    {
		if (!initialized) init();

		if (LogLevel.LOG_LEVEL_INFO <= singleton.getLoggingLevel()) {
        	singleton.logString(
        	    AppLog.logSource,
        		logString,
        		logObj,
        		LogLevel.LOG_LEVEL_INFO,
        		0);
		}
    }

    /**
     * Logs a text string at INFO_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @see #info(String, Object)
     * @see #info(String, Object, int)
     */
    public static void info(String logString)
    {
		if (!initialized) init();

		if (LogLevel.LOG_LEVEL_INFO <= singleton.getLoggingLevel()) {
        	singleton.logString(
        	    AppLog.logSource,
        		logString,
        		null,
        		LogLevel.LOG_LEVEL_INFO,
        		0);
		}
	}

    /**
     * Logs a text string at INFO_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @param logObj  
     *         a log object providing detail information. The log object is
     *         stringified using its <code>toString</code> method before getting
     *         logged. The log object may be a null object. If the log object
     *         is a <code>Throwable</code> it's message and stack trace is 
     *         logged.
     * @param callStackOff  
     *         a call stack correction offset. The offset must be a positive 
     *         number: 0, 1, 2, 3, ...
     * @see #info(String)
     * @see #info(String, Object)
     */
    public static void info(
    			String logString,
    			Object logObj,
    			int    callStackOff)
    {
		if (!initialized) init();

		if (LogLevel.LOG_LEVEL_INFO <= singleton.getLoggingLevel()) {
        	singleton.logString(
        	    AppLog.logSource,
        		logString,
        		logObj,
        		LogLevel.LOG_LEVEL_INFO,
        		callStackOff);
		}
    }

    /**
     * Logs a text string at DETAIL_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @param logObj  
     *         a log object providing detail information. The log object is
     *         stringified using its <code>toString</code> method before getting
     *         logged. The log object may be a null object. If the log object
     *         is a <code>Throwable</code> it's message and stack trace is 
     *         logged.
     * @see #detail(String)
     * @see #detail(String, Object, int)
     */
    public static void detail(
    			String logString,
    			Object logObj)
    {
		if (!initialized) init();

		if (LogLevel.LOG_LEVEL_DETAIL <= singleton.getLoggingLevel()) {
        	singleton.logString(
        	    AppLog.logSource,
        		logString,
        		logObj,
        		LogLevel.LOG_LEVEL_DETAIL,
        		0);
		}
    }

    /**
     * Logs a text string at DETAIL_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @see #detail(String, Object)
     * @see #detail(String, Object, int)
     */
    public static void detail(String logString)
    {
		if (!initialized) init();

		if (LogLevel.LOG_LEVEL_DETAIL <= singleton.getLoggingLevel()) {
        	singleton.logString(
        	    AppLog.logSource,
        		logString,
        		null,
        		LogLevel.LOG_LEVEL_DETAIL,
        		0);
		}
    }

    /**
     * Logs a text string at DETAIL_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @param logObj  
     *         a log object providing detail information. The log object is
     *         stringified using its <code>toString</code> method before getting
     *         logged. The log object may be a null object. If the log object
     *         is a <code>Throwable</code> it's message and stack trace is 
     *         logged.
     * @param callStackOff  
     *         a call stack correction offset. The offset must be a positive 
     *         number: 0, 1, 2, 3, ...
     * @see #detail(String)
     * @see #detail(String, Object)
     */
    public static void detail(
    			String logString,
    			Object logObj,
    			int    callStackOff)
    {
		if (!initialized) init();

		if (LogLevel.LOG_LEVEL_DETAIL <= singleton.getLoggingLevel()) {
        	singleton.logString(
        	    AppLog.logSource,
        		logString,
        		logObj,
        		LogLevel.LOG_LEVEL_DETAIL,
        		callStackOff);
		}
    }

    /**
     * Logs a text string at TRACE_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @param logObj  
     *         a log object providing detail information. The log object is
     *         stringified using its <code>toString</code> method before getting
     *         logged. The log object may be a null object. If the log object
     *         is a <code>Throwable</code> it's message and stack trace is 
     *         logged.
     * @see #trace(String)
     * @see #trace(String, Object, int)
     */
    public static void trace(
    			String logString,
    			Object logObj)
    {
		if (!initialized) init();

		if (LogLevel.LOG_LEVEL_TRACE <= singleton.getLoggingLevel()) {
        	singleton.logString(
    	    	AppLog.logSource,
	        	logString,
        		logObj,
        		LogLevel.LOG_LEVEL_TRACE,
        		0);
		}
    }

    /**
     * Logs a text string at TRACE_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @see #trace(String, Object)
     * @see #trace(String, Object, int)
     */
    public static void trace(String logString)
    {
		if (!initialized) init();

		if (LogLevel.LOG_LEVEL_TRACE <= singleton.getLoggingLevel()) {
        	singleton.logString(
        	    AppLog.logSource,
        		logString,
        		null,
        		LogLevel.LOG_LEVEL_TRACE,
        		0);
		}
    }

    /**
     * Logs a text string at TRACE_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @param logObj  
     *         a log object providing detail information. The log object is
     *         stringified using its <code>toString</code> method before getting
     *         logged. The log object may be a null object. If the log object
     *         is a <code>Throwable</code> it's message and stack trace is 
     *         logged.
     * @param callStackOff  
     *         a call stack correction offset. The offset must be a positive 
     *         number: 0, 1, 2, 3, ...
     * @see #trace(String)
     * @see #trace(String, Object)
     */
    public static void trace(
    			String logString,
    			Object logObj,
    			int    callStackOff)

    {
		if (!initialized) init();

		if (LogLevel.LOG_LEVEL_TRACE <= singleton.getLoggingLevel()) {
        	singleton.logString(
        	    AppLog.logSource,
        		logString,
        		logObj,
        		LogLevel.LOG_LEVEL_TRACE,
        		callStackOff);
		}
    }

    /**
     * Logs a text string and an elapsed time at PERFORMANCE_LEVEL.
     *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
     * @param elapsedTime	
     *         the elapsed time in milli seconds
     * @see #performance(String, long, int)
     */
    public static void performance(String logString, long elapsedTime)
    {
		if (!initialized) init();

		if (singleton.isLoggingPerformance()) {
        	singleton.logString(
    	    	AppLog.logSource,
        		elapsedTime + " ms",
        		logString,
        		LogLevel.LOG_PERFORMANCE,
        		0);
		}
    }

	/**
	 * Logs a text string and an elapsed time at PERFORMANCE_LEVEL.
	 *
     * @param logString   
     *         a concise summary message. The message must be single line and
     *         must therefore not contain any '\r' or '\n' characters. The '\r' 
     *         and '\n' characters are removed silently from the message.
	 * @param elapsedTime	
	 *         the elapsed time in milli seconds
     * @param callStackOff  
     *         a call stack correction offset. The offset must be a positive 
     *         number: 0, 1, 2, 3, ...
     * @see #performance(String, long)
	 */
	public static void performance(
				String logString, 
				long elapsedTime,
				int    callStackOff)
	{
		if (!initialized) init();

		if (singleton.isLoggingPerformance()) {
			singleton.logString(
				AppLog.logSource,
				elapsedTime + " ms",
				logString,
				LogLevel.LOG_PERFORMANCE,
				callStackOff);
		}
	}

    /**
     * Logs a text string at STATISTICS_LEVEL. Statistics messages are grouped.
     * The groups used are defined by the application and do not have any
     * meaning for the logging framework.
     *
     * @param group  a statistics group.
     * @param info   a statistics record string
     * @see #statistics(String, String, int)
     */
    public static void statistics(String 	group, String 	record)
    {
		if (!initialized) init();

		if (singleton.isLoggingStatistics()) {
        	singleton.logString(
    	    	AppLog.logSource,
        		group,
        		record,
        		LogLevel.LOG_STATISTICS,
        		0);
		}
   	}

	/**
	 * Logs a text string at STATISTICS_LEVEL. Statistics messages are grouped.
	 * The groups used are defined by the application and do not have any
	 * meaning for the logging framework.
	 *
	 * @param group  
	 *         a statistics group.
	 * @param info   
	 *         a statistics record string
     * @param callStackOff  
     *         a call stack correction offset. The offset must be a positive 
     *         number: 0, 1, 2, 3, ...
     * @see #statistics(String, String)
	 */
	public static void statistics(
		String 	group, 
		String 	record,
		int    callStackOff)
	{
		if (!initialized) init();

		if (singleton.isLoggingStatistics()) {
			singleton.logString(
				AppLog.logSource,
				group,
				record,
				LogLevel.LOG_STATISTICS,
				callStackOff);
		}
	}


    ///////////////////////////////////////////////////////////////////////////
    //
    // Backward compatibility (all deprecated)
    //
    ///////////////////////////////////////////////////////////////////////////

	/**
	 * Set the application name string that the logs pertain to. Must be called
	 * before doing any logging.
	 *
	 * @param appName the application name
     * @deprecated use {@link AppLog#setConfigName(String)} instead
	 */
	public static void setApplicationName(String appName)
	{
		setConfigName(appName);
	}

    /**
     * Set logging level
     *
     * <p>
     * This method should not be used by applications. The logging level is set
     * in the log property file.
     *
     * @param level  a new logging level
     * @deprecated use {@link Config#setLogLevel(int)} from the
     *                 {@link AppLog#getLogConfig()} class
     */
	public static void setLogLevel(int logLevel)
	{
		if (!initialized) init();

		singleton.setLoggingLevel(logLevel);
	}


    /**
     * Enable/Disable performance logging
     *
     * <p>
     * This method should not be used by applications. The performance log state
     * is set in the log property file.
     *
     * @param enable   enable if true
     * @deprecated use {@link Config#enablePerformanceLog(boolean)} from the
     *                 {@link AppLog#getLogConfig()} class
     */
	public static void enablePerformanceLog(boolean  enable)
	{
		if (!initialized) init();

		singleton.setLoggingPerformance(enable);
	}


    /**
     * Enable/Disable statistics logging
     *
     * <p>
     * This method should not be used by applications. The statistics log state
     * is set in the log property file.
     *
     * @param enable  enable if true
     * @deprecated use {@link Config#enableStatisticsLog(boolean)} from the
     *                 {@link AppLog#getLogConfig()} class
     */
	public static void enableStatisticsLog(boolean  enable)
	{
		if (!initialized) init();

		singleton.setLoggingStatistics(enable);
	}



    ///////////////////////////////////////////////////////////////////////////
    //
    // Init
    //
    ///////////////////////////////////////////////////////////////////////////

    private static void  init()
    {
    	// For Double-Checked-Locking behaviour used here see the comment in
    	// SysLog.init(). Usually Double-Checked-Locking does not work except
        // for some rare conditions. This saves us from polluting this class
        // with synchronized methods.
       	synchronized(singleton) {
            if (!initialized) {
        		initialized = true;
        
        		singleton.loadConfig(AppLog.configName, AppLog.logProperties);
        		singleton.loadMechanisms();
          
        		SysLog.register(AppLog.getLogConfig());
                SysLog.trace("AppLog initialized with the given configuration", AppLog.configName);
        
                // openmdx jar version logging
                AppLog.info("openMDX kernel version", org.openmdx.kernel.Version.getImplementationVersion());                            
                AppLog.info("openMDX base version", org.openmdx.base.Version.getImplementationVersion());                
                AppLog.info("openMDX application version", org.openmdx.application.Version.getImplementationVersion());
 
                // openmdx jar version dependeny check
                try {
                    Dependencies.checkDependencies();
                } 
                catch (VersionMismatchException exception) {
                    AppLog.error("Dependency check failed", exception); 
                    throw exception;       
                }
            }
        }
    }



    /** Constants */
    final private static String LOGNAME = "AppLog";
    final static String LOGSOURCE = "App";


    /**
     * Provides the class variable for the Singleton pattern,
     * to keep track of the one and only instance of this class.
     */
    private static volatile Log singleton = newLog(LOGNAME, AppLog.class);

    private static volatile boolean initialized = false;


	/** The application properties. These properties are optional */
	private static Properties  logProperties = null;

	/** The application name. Default is the log name */
    private static String configName = LOGNAME;

	/** The log source */
    private static Object logSource = LOGSOURCE;
}
