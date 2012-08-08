/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: SysLog.java,v 1.14 2008/05/05 17:51:52 hburger Exp $
 * Description: Former Log API
 * Revision:    $Revision: 1.14 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/05 17:51:52 $
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
package org.openmdx.kernel.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;

/**
 * <code>SysLog</code> wraps the Log class to do application-general
 * logging. It provides a singleton accessor, along with lots of
 * handy class method {static} accessors. <p>
 */
public class SysLog {

    /**
     * Constructor 
     */
	private SysLog(
	) {
	    // This class must NEVER be instantiated. Actually SysLog only extends
	    // the class Log to prevent problems with multiple class
	    // loaders. SysLog uses a Log instance singleton by delegation!
	}
	
    /** Constants */
	final public static String LOGSOURCE_SUMMARY_DETAIL_FORMAT = "{}|{}|{}";
    final public static String LOGSOURCE_SUMMARY_FORMAT = "{}|{}";
    final public static String LOGNAME = "SysLog";
    final private static String LOGSOURCE = "Sys";
	
    private static final Logger logger = LoggerFactory.getLogger(LOGNAME);
    private static final boolean locationAware = logger instanceof LocationAwareLogger;
    private static final LocationAwareLogger locationAwareLogger = (LocationAwareLogger) (locationAware ? logger : null);
    private static Object logSource = LOGSOURCE;
    private static final String THIS = SysLog.class.getName();

	///////////////////////////////////////////////////////////////////////////
    //
    // LOG configuration
    //
    ///////////////////////////////////////////////////////////////////////////

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
	 * SysLog.setLogSource(logSource)
	 *
	 * </code>
	 *
	 * <p>
	 * Static log source example:
	 * <code>
	 *
	 * SysLog.setLogSource("LogSource")
	 *
	 * </code>
	 *
	 * @param logSource a log source object
	 */
	public static void setLogSource(
	    Object logSource
	){
	    if(logSource != null) {
    	    SysLog.logSource = logSource;
	    }
	}

	/**
     * Checks if trace logging is active
     *
     * <p>
     * Applications can use this method before they call SysLog.trace(...) if
     * the log string creation is very time consuming.
     *
     * <pre>
     *      SysLog.trace("Customer created", "First=Mark, Last=Smith");
	 *
     *      if (SysLog.isTraceOn()) {
     *	        summary = expensive_creation();
     *	        detail  = expensive_creation();
     *          SysLog.trace(summary, detail);
     *    	}
     * </pre>
	 *
     * @return  true if trace is active
     */
    public static boolean isTraceOn()
    {
        return logger.isTraceEnabled();
    }


    ///////////////////////////////////////////////////////////////////////////
    //
    // LOG methods
    //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Format the log message
     */
    private static final String format(
        String logString,
        Object logObj
    ){
        return logObj == null || logObj instanceof Throwable ? MessageFormatter.arrayFormat(
            LOGSOURCE_SUMMARY_FORMAT, 
            logSource, 
            logString
        ) : MessageFormatter.arrayFormat(
            LOGSOURCE_SUMMARY_DETAIL_FORMAT, 
            logSource, 
            logString,
            logObj
        );
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
     * @see #criticalError(String)
     * @see #criticalError(String, Object, int)
     */
    public static void criticalError(
		String logString,
		Object logObj
    ){
        if (logger.isErrorEnabled()) {
            String message = format(logString, logObj);
            Throwable throwable = (Throwable) (logObj instanceof Throwable ? logObj : null);
            if(locationAware) {
                locationAwareLogger.log(
                    null, // marker
                    THIS, 
                    LocationAwareLogger.ERROR_INT, 
                    message, 
                    throwable
                 );
            } else if(throwable == null) {
                logger.error(message);
            } else {
                logger.error(message, throwable);
            }
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
    public static void criticalError(String logString) {
        if (logger.isErrorEnabled()) {
            String message = format(logString, null);
            if(locationAware) {
                locationAwareLogger.log(
                    null, // marker
                    THIS, 
                    LocationAwareLogger.ERROR_INT, 
                    message, 
                    null // throwable
                 );
            } else {
                logger.error(message);
            }
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
		Object logObj
	){
        if (logger.isErrorEnabled()) {
            String message = format(logString, logObj);
            Throwable throwable = (Throwable) (logObj instanceof Throwable ? logObj : null);
            if(locationAware) {
                locationAwareLogger.log(
                    null, // marker
                    THIS, 
                    LocationAwareLogger.ERROR_INT, 
                    message, 
                    throwable
                 );
            } else if(throwable == null) {
                logger.error(message);
            } else {
                logger.error(message, throwable);
            }
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
    public static void error(
        String logString
    ){
        if (logger.isErrorEnabled()) {
            String message = format(logString, null);
            if(locationAware) {
                locationAwareLogger.log(
                    null, // marker
                    THIS, 
                    LocationAwareLogger.ERROR_INT, 
                    message, 
                    null // throwable
                 );
            } else {
                logger.error(message);
            }
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
		Object logObj
    ){
        if (logger.isWarnEnabled()) {
            String message = format(logString, logObj);
            Throwable throwable = (Throwable) (logObj instanceof Throwable ? logObj : null);
            if(locationAware) {
                locationAwareLogger.log(
                    null, // marker
                    THIS, 
                    LocationAwareLogger.WARN_INT, 
                    message, 
                    throwable
                 );
            } else if(throwable == null) {
                logger.warn(message);
            } else {
                logger.warn(message, throwable);
            }
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
    public static void warning(
        String logString
    ){
        if (logger.isWarnEnabled()) {
            String message = format(logString, null);
            if(locationAware) {
                locationAwareLogger.log(
                    null, // marker
                    THIS, 
                    LocationAwareLogger.WARN_INT, 
                    message, 
                    null // throwable
                 );
            } else {
                logger.warn(message);
            }
        }
    }

    /**
     * Log an exception
     * 
     * @param exception
     */
    public static void warning(
        Exception exception
    ){
        if (logger.isWarnEnabled()) {
            String message = format(exception.getMessage(), null);
            Throwable throwable = exception.getCause();
            if(locationAware) {
                locationAwareLogger.log(
                    null, // marker
                    throwable.getClass().getName(), 
                    LocationAwareLogger.WARN_INT, 
                    message, 
                    throwable
                 );
            } else {
                logger.warn(message, throwable);
            }
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
		Object logObj
    ){
        if (logger.isInfoEnabled()) {
            String message = format(logString, logObj);
            Throwable throwable = (Throwable) (logObj instanceof Throwable ? logObj : null);
            if(locationAware) {
                locationAwareLogger.log(
                    null, // marker
                    THIS, 
                    LocationAwareLogger.INFO_INT, 
                    message, 
                    throwable
                 );
            } else if(throwable == null) {
                logger.info(message);
            } else {
                logger.info(message, throwable);
            }
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
    public static void info(
        String logString
    ){
        if (logger.isInfoEnabled()) {
            String message = format(logString, null);
            if(locationAware) {
                locationAwareLogger.log(
                    null, // marker
                    THIS, 
                    LocationAwareLogger.INFO_INT, 
                    message, 
                    null // throwable
                 );
            } else {
                logger.info(message);
            }
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
		Object logObj
    ){
        if (logger.isDebugEnabled()) {
            String message = format(logString, logObj);
            Throwable throwable = (Throwable) (logObj instanceof Throwable ? logObj : null);
            if(locationAware) {
                locationAwareLogger.log(
                    null, // marker
                    THIS, 
                    LocationAwareLogger.DEBUG_INT, 
                    message, 
                    throwable
                 );
            } else if(throwable == null) {
                logger.debug(message);
            } else {
                logger.debug(message, throwable);
            }
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
    public static void detail(
        String logString
    ){
        if (logger.isDebugEnabled()) {
            String message = format(logString, null);
            if(locationAware) {
                locationAwareLogger.log(
                    null, // marker
                    THIS, 
                    LocationAwareLogger.DEBUG_INT, 
                    message, 
                    null // throwable
                 );
            } else {
                logger.debug(message);
            }
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
		Object logObj
	){
        if (logger.isTraceEnabled()) {
            String message = format(logString, logObj);
            Throwable throwable = (Throwable) (logObj instanceof Throwable ? logObj : null);
            if(locationAware) {
                locationAwareLogger.log(
                    null, // marker
                    THIS, 
                    LocationAwareLogger.TRACE_INT, 
                    message, 
                    throwable
                 );
            } else if(throwable == null) {
                logger.trace(message);
            } else {
                logger.trace(message, throwable);
            }
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
    public static void trace(
        String logString
    ){
        if (logger.isTraceEnabled()) {
            String message = format(logString, null);
            if(locationAware) {
                locationAwareLogger.log(
                    null, // marker
                    THIS, 
                    LocationAwareLogger.TRACE_INT, 
                    message, 
                    null // throwable
                 );
            } else {
                logger.trace(message);
            }
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
    public static void performance(
        String logString, 
        long elapsedTime
    ){
        if (logger.isInfoEnabled()) {
            String message = MessageFormatter.arrayFormat(
                "{} Performance|{}|Elapsed time: {} ms", 
                logSource, 
                logString,
                elapsedTime
            );
            if(locationAware) {
                locationAwareLogger.log(
                    null, // marker
                    THIS, 
                    LocationAwareLogger.INFO_INT, 
                    message, 
                    null // throwable
                 );
            } else {
                logger.info(message);
            }
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
        if (logger.isInfoEnabled()) {
            String message = MessageFormatter.arrayFormat(
                "{} Statistics|Group {}|{}", 
                logSource, 
                group,
                record
            );
            if(locationAware) {
                locationAwareLogger.log(
                    null, // marker
                    THIS, 
                    LocationAwareLogger.INFO_INT, 
                    message, 
                    null // throwable
                 );
            } else {
                logger.info(message);
            }
        }
   	}

}
