/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: SysLogLoggerAdapter.java,v 1.2 2008/05/05 17:50:04 hburger Exp $
 * Description: Object Relational Mapping 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/05 17:50:04 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2008, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.slf4j.impl;

import org.openmdx.compatibility.kernel.log.LogLevel;
import org.openmdx.compatibility.kernel.log.SysLog;
import org.openmdx.kernel.log.impl.Log;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;

/**
 * A wrapper over {@link org.openmdx.kernel.SysLog org.openmdx.kernel.SysLog} in
 * conformity with the {@link Logger} interface. 
 */
public final class SysLogLoggerAdapter 
    extends MarkerIgnoringBase 
    implements LocationAwareLogger 
{

    // WARN: SysLogLoggerAdapter constructor should have only package access so
    // that only SysLogLoggerFactory be able to create one.
    SysLogLoggerAdapter(
        String logName
    ) {
        this.logName = logName;
        this.singleton = SysLog.getLogger();
    }

    /**
     * 
     */
    private final String logName;

    /**
     * The initialized logger
     */
    private final Log singleton;  

    /**
     * 
     */
    private static final int NATIVE_CALL_STACK_OFFSET = 0;
    
    /**
     * 
     */
    private static final int LOCATION_AWARE_CALL_STACK_OFFSET = 1;

    /**
     * 
     */
    public String getName(
    ) {
        return this.logName;
    }

    public void log(
        Marker marker, 
        String callerFQCN, 
        int level, 
        String message, 
        Throwable t
    ) {
        int logLevel;
        switch(level) {
            case LocationAwareLogger.TRACE_INT: 
                logLevel = LogLevel.LOG_LEVEL_TRACE;
                break;
            case LocationAwareLogger.DEBUG_INT: 
                logLevel = LogLevel.LOG_LEVEL_DETAIL;
                break;
            case LocationAwareLogger.INFO_INT: 
                logLevel = LogLevel.LOG_LEVEL_INFO;
                break;
            case LocationAwareLogger.WARN_INT: 
                logLevel = LogLevel.LOG_LEVEL_WARNING;
                break;
            case LocationAwareLogger.ERROR_INT: 
                logLevel = LogLevel.LOG_LEVEL_ERROR;
                break;
            default:
                throw new IllegalStateException("Level number "+level+" is not recognized.");
        }
        if (logLevel <= singleton.getLoggingLevel()) {
    		int bar;
        	if(
        	    (this.logName == org.openmdx.kernel.log.SysLog.LOGNAME || this.logName == org.openmdx.application.log.AppLog.LOGNAME) && 
	            message != null && (bar = message.indexOf('|')) > 0
            ){
                this.singleton.logString(
                	message.substring(0, bar), // log-source
                	message.substring(bar + 1), // log-message
                    t,
                    logLevel,
                    LOCATION_AWARE_CALL_STACK_OFFSET
                );
        	} else {
                this.singleton.logString(
            		this.logName,
            		message,
                    t,
                    logLevel,
                    LOCATION_AWARE_CALL_STACK_OFFSET
                );
        	}
        }
    }

    
    //------------------------------------------------------------------------
    // Trace
    //------------------------------------------------------------------------
        
    /* (non-Javadoc)
     * @see org.slf4j.Logger#isTraceEnabled()
     */
    public final boolean isTraceEnabled() {
        return LogLevel.LOG_LEVEL_TRACE <= singleton.getLoggingLevel();
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String)
     */
    public void trace(String msg) {
        if (isTraceEnabled()) this.singleton.logString(
            this.logName,
            msg,
            null,
            LogLevel.LOG_LEVEL_TRACE,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object)
     */
    public void trace(String format, Object arg) {
        if (isTraceEnabled()) this.singleton.logString(
            this.logName,
            MessageFormatter.format(format, arg),
            null,
            LogLevel.LOG_LEVEL_TRACE,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object, java.lang.Object)
     */
    public void trace(String format, Object arg1, Object arg2) {
        if (isTraceEnabled()) {
            this.singleton.logString(
                this.logName,
                MessageFormatter.format(format, arg1, arg2),
                null,
                LogLevel.LOG_LEVEL_TRACE,
                NATIVE_CALL_STACK_OFFSET
            );
        }
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object[])
     */
    public void trace(String format, Object... argArray) {
        if (isTraceEnabled()) this.singleton.logString(
            this.logName,
            MessageFormatter.arrayFormat(format, argArray),
            null,
            LogLevel.LOG_LEVEL_TRACE,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Throwable)
     */
    public void trace(String msg, Throwable t) {
        if (isTraceEnabled()) this.singleton.logString(
            this.logName,
            msg,
            t,
            LogLevel.LOG_LEVEL_TRACE,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    
    //------------------------------------------------------------------------
    // Debug
    //------------------------------------------------------------------------
        
    /**
     * Is this logger instance enabled for the LOG_LEVEL_DETAIL level?
     * 
     * @return True if this Logger is enabled for level LOG_LEVEL_DETAIL, false otherwise.
     */
    public final boolean isDebugEnabled() {
        return LogLevel.LOG_LEVEL_DETAIL <= singleton.getLoggingLevel();
    }

    /**
     * Log a message object at level LOG_LEVEL_DETAIL.
     * 
     * @param msg -
     *          the message object to be logged
     */
    public void debug(String msg) {
        if (isDebugEnabled()) this.singleton.logString(
            this.logName,
            msg,
            null,
            LogLevel.LOG_LEVEL_DETAIL,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    /**
     * Log a message at level LOG_LEVEL_DETAIL according to the specified format and argument.
     * 
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for level LOG_LEVEL_DETAIL.
     * </p>
     * 
     * @param format
     *          the format string
     * @param arg
     *          the argument
     */
    public void debug(String format, Object arg) {
        if (isDebugEnabled()) this.singleton.logString(
            this.logName,
            MessageFormatter.format(format, arg),
            null,
            LogLevel.LOG_LEVEL_DETAIL,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    /**
     * Log a message at level LOG_LEVEL_DETAIL according to the specified format and
     * arguments.
     * 
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the LOG_LEVEL_DETAIL level.
     * </p>
     * 
     * @param format
     *          the format string
     * @param arg1
     *          the first argument
     * @param arg2
     *          the second argument
     */
    public void debug(
        String format, 
        Object arg1, 
        Object arg2
    ) {
        if (isDebugEnabled()) {
            this.singleton.logString(
                this.logName,
                MessageFormatter.format(format, arg1, arg2),
                null,
                LogLevel.LOG_LEVEL_DETAIL,
                NATIVE_CALL_STACK_OFFSET
            );
        }
    }

    /**
     * Log a message at level LOG_LEVEL_DETAIL according to the specified format and
     * arguments.
     * 
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the LOG_LEVEL_DETAIL level.
     * </p>
     * 
     * @param format
     *          the format string
     * @param argArray
     *          an array of arguments
     */
    public void debug(String format, Object... argArray) {
        if (isDebugEnabled()) this.singleton.logString(
            this.logName,
            MessageFormatter.arrayFormat(format, argArray),
            null,
            LogLevel.LOG_LEVEL_DETAIL,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    /**
     * Log an exception (throwable) at level LOG_LEVEL_DETAIL with an accompanying message.
     * 
     * @param msg
     *          the message accompanying the exception
     * @param t
     *          the exception (throwable) to log
     */
    public void debug(String msg, Throwable t) {
        if (isDebugEnabled()) this.singleton.logString(
            this.logName,
            msg,
            t,
            LogLevel.LOG_LEVEL_DETAIL,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    
    //------------------------------------------------------------------------
    // Info
    //------------------------------------------------------------------------
        
    /**
     * Is this logger instance enabled for the INFO level?
     * 
     * @return True if this Logger is enabled for the INFO level, false otherwise.
     */
    public final boolean isInfoEnabled() {
        return LogLevel.LOG_LEVEL_INFO <= singleton.getLoggingLevel();
    }

    /**
     * Log a message object at the INFO level.
     * 
     * @param msg -
     *          the message object to be logged
     */
    public void info(String msg) {
        if (isInfoEnabled()) this.singleton.logString(
            this.logName,
            msg,
            null,
            LogLevel.LOG_LEVEL_INFO,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    /**
     * Log a message at level INFO according to the specified format and argument.
     * 
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the INFO level.
     * </p>
     * 
     * @param format
     *          the format string
     * @param arg
     *          the argument
     */
    public void info(String format, Object arg) {
        if (isInfoEnabled()) this.singleton.logString(
            this.logName,
            MessageFormatter.format(format, arg),
            null,
            LogLevel.LOG_LEVEL_INFO,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    /**
     * Log a message at the INFO level according to the specified format and
     * arguments.
     * 
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the INFO level.
     * </p>
     * 
     * @param format
     *          the format string
     * @param arg1
     *          the first argument
     * @param arg2
     *          the second argument
     */
    public void info(String format, Object arg1, Object arg2) {
        if (isInfoEnabled()) {
            this.singleton.logString(
                this.logName,
                MessageFormatter.format(format, arg1, arg2),
                null,
                LogLevel.LOG_LEVEL_INFO,
                NATIVE_CALL_STACK_OFFSET
            );
        }
    }

    /**
     * Log a message at level INFO according to the specified format and
     * arguments.
     * 
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the INFO level.
     * </p>
     * 
     * @param format
     *          the format string
     * @param argArray
     *          an array of arguments
     */
    public void info(String format, Object... argArray) {
        if (isInfoEnabled()) this.singleton.logString(
            this.logName,
            MessageFormatter.arrayFormat(format, argArray),
            null,
            LogLevel.LOG_LEVEL_INFO,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    /**
     * Log an exception (throwable) at the INFO level with an accompanying
     * message.
     * 
     * @param msg
     *          the message accompanying the exception
     * @param t
     *          the exception (throwable) to log
     */
    public void info(String msg, Throwable t) {
        if (isInfoEnabled()) this.singleton.logString(
            this.logName,
            msg,
            t,
            LogLevel.LOG_LEVEL_INFO,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    
    //------------------------------------------------------------------------
    // Warn
    //------------------------------------------------------------------------
        
    /**
     * Is this logger instance enabled for the WARNING level?
     * 
     * @return True if this Logger is enabled for the WARNING level, false
     *         otherwise.
     */
    public final boolean isWarnEnabled() {
        return LogLevel.LOG_LEVEL_WARNING <= singleton.getLoggingLevel();
    }

    /**
     * Log a message object at the WARNING level.
     * 
     * @param msg -
     *          the message object to be logged
     */
    public void warn(String msg) {
        if (isWarnEnabled()) this.singleton.logString(
            this.logName,
            msg,
            null,
            LogLevel.LOG_LEVEL_WARNING,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    /**
     * Log a message at the WARNING level according to the specified format and
     * argument.
     * 
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the WARNING level.
     * </p>
     * 
     * @param format
     *          the format string
     * @param arg
     *          the argument
     */
    public void warn(String format, Object arg) {
        if (isWarnEnabled()) this.singleton.logString(
            this.logName,
            MessageFormatter.format(format, arg),
            null,
            LogLevel.LOG_LEVEL_WARNING,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    /**
     * Log a message at the WARNING level according to the specified format and
     * arguments.
     * 
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the WARNING level.
     * </p>
     * 
     * @param format
     *          the format string
     * @param arg1
     *          the first argument
     * @param arg2
     *          the second argument
     */
    public void warn(String format, Object arg1, Object arg2) {
        if (isWarnEnabled()) {
            this.singleton.logString(
                this.logName,
                MessageFormatter.format(format, arg1, arg2),
                null,
                LogLevel.LOG_LEVEL_WARNING,
                NATIVE_CALL_STACK_OFFSET
            );
        }
    }

    /**
     * Log a message at level WARNING according to the specified format and
     * arguments.
     * 
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the WARNING level.
     * </p>
     * 
     * @param format
     *          the format string
     * @param argArray
     *          an array of arguments
     */
    public void warn(String format, Object... argArray) {
        if (isWarnEnabled()) this.singleton.logString(
            this.logName,
            MessageFormatter.arrayFormat(format, argArray),
            null,
            LogLevel.LOG_LEVEL_WARNING,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    /**
     * Log an exception (throwable) at the WARNING level with an accompanying
     * message.
     * 
     * @param msg
     *          the message accompanying the exception
     * @param t
     *          the exception (throwable) to log
     */
    public void warn(String msg, Throwable t) {
        if (isWarnEnabled()) this.singleton.logString(
            this.logName,
            msg,
            t,
            LogLevel.LOG_LEVEL_WARNING,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    
    //------------------------------------------------------------------------
    // Error
    //------------------------------------------------------------------------
        
    /**
     * Is this logger instance enabled for level LOG_LEVEL_ERROR?
     * 
     * @return True if this Logger is enabled for level LOG_LEVEL_ERROR, false otherwise.
     */
    public final boolean isErrorEnabled() {
        return LogLevel.LOG_LEVEL_ERROR <= singleton.getLoggingLevel();
    }

    /**
     * Log a message object at the SEVERE level.
     * 
     * @param msg -
     *          the message object to be logged
     */
    public void error(String msg) {
        if (isErrorEnabled()) this.singleton.logString(
            this.logName,
            msg,
            null,
            LogLevel.LOG_LEVEL_ERROR,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    /**
     * Log a message at the LOG_LEVEL_ERROR level according to the specified format and
     * argument.
     * 
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the LOG_LEVEL_ERROR level.
     * </p>
     * 
     * @param format
     *          the format string
     * @param arg
     *          the argument
     */
    public void error(String format, Object arg) {
        if (isErrorEnabled()) this.singleton.logString(
            this.logName,
            MessageFormatter.format(format, arg),
            null,
            LogLevel.LOG_LEVEL_ERROR,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    /**
     * Log a message at the LOG_LEVEL_ERROR level according to the specified format and
     * arguments.
     * 
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the LOG_LEVEL_ERROR level.
     * </p>
     * 
     * @param format
     *          the format string
     * @param arg1
     *          the first argument
     * @param arg2
     *          the second argument
     */
    public void error(String format, Object arg1, Object arg2) {
        if (isErrorEnabled()) {
            this.singleton.logString(
                this.logName,
                MessageFormatter.format(format, arg1, arg2),
                null,
                LogLevel.LOG_LEVEL_ERROR,
                NATIVE_CALL_STACK_OFFSET
            );
        }
    }

    /**
     * Log a message at level INFO according to the specified format and
     * arguments.
     * 
     * <p>
     * This form avoids superfluous object creation when the logger is disabled
     * for the INFO level.
     * </p>
     * 
     * @param format
     *          the format string
     * @param argArray
     *          an array of arguments
     */
    public void error(String format, Object... argArray) {
        if (isErrorEnabled()) this.singleton.logString(
            this.logName,
            MessageFormatter.arrayFormat(format, argArray),
            null,
            LogLevel.LOG_LEVEL_ERROR,
            NATIVE_CALL_STACK_OFFSET
        );
    }

    /**
     * Log an exception (throwable) at the SEVERE level with an accompanying
     * message.
     * 
     * @param msg
     *          the message accompanying the exception
     * @param t
     *          the exception (throwable) to log
     */
    public void error(String msg, Throwable t) {
        if (isErrorEnabled()) this.singleton.logString(
            this.logName,
            msg,
            t,
            LogLevel.LOG_LEVEL_ERROR,
            NATIVE_CALL_STACK_OFFSET
        );
    }

}
