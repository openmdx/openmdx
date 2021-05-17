/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Former Log API
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2013, OMEX AG, Switzerland
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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

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
	    // Avoid instantiation
	}

    /**
     * To tell the foreign record about the loggger class
     */
    private static final String THIS = SysLog.class.getName();

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
        return LoggerFactory.getLogger().isLoggable(Level.FINEST);
    }

    /**
     * Log using the given pattern and arguments
     * 
     * @param level
     * @param pattern 
     * @param arguments
     */
    public static void log(
        Level level,
        String pattern, 
        Object... arguments
    ){
        Logger logger = LoggerFactory.getLogger();
        if(logger.isLoggable(level)) {
            LogRecord record = new ForeignLogRecord(
                logger.getName(), 
                SysLog.THIS, 
                level, 
                pattern
            );
            // If we used resource bundles we would them set here
            record.setParameters(arguments);
            logger.log(record);
        }
    }

    /**
     * Log an exception
     * 
     * @param level
     * @param message
     * @param throwable
     */
    public static void log(
        Level level,
        String message,
        Throwable throwable
    ){
        Logger logger = LoggerFactory.getLogger();
        if(logger.isLoggable(level)) {
            LogRecord record = new ForeignLogRecord(
                logger.getName(), 
                SysLog.THIS, 
                level, 
                message
            );
            // If we used resource bundles we would them set here
            record.setThrown(throwable);
            logger.log(record);
        }
        
    }
    
    /**
     * Log an exception
     * 
     * @param level
     * @param throwable
     * @param pattern
     * @param arguments
     */
    private static void log(
        Level level,
        Throwable throwable,
        String pattern, 
        Object... arguments
    ){
        Logger logger = LoggerFactory.getLogger();
        if(logger.isLoggable(level)) {
            LogRecord record = new ForeignLogRecord(
                logger.getName(), 
                SysLog.THIS, 
                level, 
                pattern
            );
            // If we used resource bundles we would them set here
            record.setParameters(arguments);
            record.setThrown(throwable);
            logger.log(record);
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
     * @see #criticalError(String)
     * @see #criticalError(String, Object, int)
     */
    public static void criticalError(
		String logString,
		Object logObj
    ){
        log(Level.SEVERE, "Sys|{0}|{1}", logString, logObj);
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
        Throwable logObj
    ){
        log(Level.SEVERE, logObj, "Sys|{0}", logString);
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
        log(Level.SEVERE, "Sys|{0}", logString);
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
        log(Level.SEVERE, "Sys|{0}|{1}", logString, logObj);
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
        Throwable logObj
    ){
        log(Level.SEVERE, logObj, "Sys|{0}", logString);
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
        log(Level.SEVERE, "Sys|{0}", logString);
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
        log(Level.WARNING, "Sys|{0}|{1}", logString, logObj);
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
        Throwable logObj
    ){
        log(Level.WARNING, logObj, "Sys|{0}", logString);
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
        log(Level.WARNING, "Sys|{0}", logString);
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
        log(Level.INFO, "Sys|{0}|{1}", logString, logObj);
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
        Throwable logObj
    ){
        log(Level.INFO, logObj, "Sys|{0}", logString);
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
        log(Level.INFO, "Sys|{0}", logString);
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
        log(Level.FINE, "Sys|{0}|{1}", logString, logObj);
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
        Throwable logObj
    ){
        log(Level.FINE, logObj, "Sys|{0}", logString);
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
        log(Level.FINE, "Sys|{0}", logString);
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
        log(Level.FINEST, "Sys|{0}|{1}", logString, logObj);
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
        Throwable logObj
    ){
        log(Level.FINEST, logObj, "Sys|{0}", logString);
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
        log(Level.FINEST, "Sys|{0}", logString);
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
        log(Level.INFO, "Sys Performance|{0}|Elapsed time: {1} ms", logString, Long.valueOf(elapsedTime));
    }

    /**
     * Logs a text string at STATISTICS_LEVEL. Statistics messages are grouped.
     * The groups used are defined by the application and do not have any
     * meaning for the logging framework.
     *
     * @param group  a statistics group.
     * @param record a statistics record string
     * @see #statistics(String, String, int)
     */
    public static void statistics(
        String 	group, 
        String 	record
    ) {
        log(Level.INFO, "Sys Statistics|Group {0}|{1}", group, record);
   	}

}
