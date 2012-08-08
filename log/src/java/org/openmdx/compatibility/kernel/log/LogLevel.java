/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LogLevel.java,v 1.1 2008/03/21 18:21:56 hburger Exp $
 * Description: Logging Levels
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:21:56 $
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
package org.openmdx.compatibility.kernel.log;


/**
 * <p>
 * <b> System Event Categories </b>
 * <p>
 * The logging system provides six system event categories.
 *
 * <ul>
 * 		<li><b>CRITICAL_ERROR_LEVEL</b> (Level 1)
 * <p>
 * 		The system encountered a critical error at this logging level, 
 * 		which affects the accuracy, integrity, reliability, or 
 * 		capability of the system.  Someone should be paged to address
 * 		the error as soon as possible.
 * <p>
 * 		Critical errors must explicitely be logged by the application.
 * <p>
 * 		<li><b>ERROR_LEVEL</b> (Level 2)
 * <p>
 * 		The system encountered an unexpected error at this logging level, 
 * 		which probably means the code intercepted an error it cannot handle.
 * 		This error is not of a critical nature and can be recovered from
 * 		automatically.
 * <p>
 * 		Someone should probably be emailed to resolve the error in the near
 * 		future to increase the reliability of the product.
 * <p>
 * 		Errors must not explicitely be logged by the application. Only
 * 		the SvcError class implicitely logs at this level, when its
 * 		log flag is set to true.
 * <p>
 * 		<li><b>WARNING_LEVEL</b> (Level 3)
 * <p>
 * 		The system encountered an expected error situation. The system
 * 		recovered from it but the fact that it happened should be recorded
 * 		to see how frequent it happens.
 * <p>
 * 		Warnings must not explicitely be logged by the application. Only
 * 		the SvcException class implicitely logs at this level, when its
 * 		log flag is set to true. 
 * <p>
 * 		<li><b>INFO_LEVEL</b> (Level 4)
 * <p>
 * 		Normal logging level.  All interesting periodic events should 
 * 		be logged at this level so someone looking through the log can
 * 		see the amount and kind of processing happening in the system.
 * <p>
 * 		The application may log at this level. E.g. the application 
 * 		framework logs server start, stop and command line options at 
 * 		this level.
 * <p>
 * 		<li><b>DETAIL_LEVEL</b> (Level 5)
 * <p>
 * 		Moderately detailed logging level to be used to help debug
 * 		typical problems in the system.  Not so detailed that the
 * 		big picture gets lost.
 * <p>
 * 		<li><b>TRACE_LEVEL</b> (Level 6)
 * <p>
 * 		Most detailed logging level. Everything sent to the log will
 * 		be logged. Use this level to trace system execution for really
 * 		nasty problems.
 * <p>
 * 		The application may log at this level. E.g. SQL traces.
 * <p>
 * 		DO NOT "method-level" trace (that is, logging the entrance and
 * 		exit to each method). Actually the SvcException provides
 * 		enough stack trace information in case of errors.
 * <p>
 * 		<li><b>LOG_PERFORMANCE</b> (independet Level)
 * <p>
 * 		Performance logging level to be used to help finding performance
 * 		problems in the system. This level is handled independently from 
 * 		all other logging levels, meaning that it can be turned on and
 * 		off without affecting the other levels.
 * <p>
 * 		The application may log at this level.
 * <p>
 * 		<li><b>LOG_STATISTICS</b> (independet Level)
 * <p>
 * 		Statistics logging level is used to gather statistics information.
 * 		This level is handled independently from all other logging levels, 
 *      meaning that it can be turned on and off without affecting the other 
 *      levels.
 * <p>
 * 		The application may log at this level.
 * <p>
 * 		<li><b>LOG_NOTIFICATION</b> (independet Level)
 * <p>
 *      Notification logging level to be used to protocol log framework internal
 *      state changes. E.g. Openening and closing of logging mechanisms, 
 *      changeing logging level, ... 
 * <p>
 *
 *      The application MUST NOT log at this level.
 * </ul>
 * <p>
 * <b> Logging configuration </b>
 * <p>
 * To set up your own logging configuration that differs from the default
 * behaviour, create in the application's start directory a log property file
 * named "XXX.log.properties".
 * <p>
 * This "XXX.log.properties" sample activates all logging levels up to 
 * level 6 (TRACE), enables performance traces and logs to a file: 
 *
 * <pre>
 *	LogFileExtension = log
 *	LogFilePath =
 *	LogLevel = trace
 *	java.LoggingMechanism = SharedFileLoggingMechanism
 *	LogFieldSeparator = |
 *	LogPerformance = true
 *	LogStatistics = true
 *	LogNotification = false 
 * </pre>
 * 
 */

public interface LogLevel {

	/**
	 * Turns the log completely off, no messages are logged
	 */
	public static final int LOG_LEVEL_DEACTIVATE = 0;

	/**
	 * The system encountered a critical error at this logging level, 
	 * which affects the accuracy, integrity, reliability, or 
	 * capability of the system.  Someone should be paged to address
	 * the error as soon as possible.<p>
	 *
	 * Critical errors must explicitely be logged by the application.
	 */
	public static final int LOG_LEVEL_CRITICAL_ERROR = 1;

	/**
	 * The system encountered an unexpected error at this logging level, 
	 * which probably means the code intercepted an error it cannot handle.
	 * This error is not of a critical nature and can be recovered from
	 * automatically.<p>
	 * Someone should probably be emailed to resolve the error in the near
	 * future to increase the reliability of the product.<p>
	 *
	 * Errors must explicitely be logged by the application.
	 */
	public static final int LOG_LEVEL_ERROR = 2;

	/**
	 * The system encountered an expected error situation.  The system
	 * recovered from it but the fact that it happened should be recorded
	 * to see how frequent it happens.<p>
	 *
	 * Warnings must explicitely be logged by the application.
	 */
	public static final int LOG_LEVEL_WARNING = 3;

	/**
	 * Normal logging level.  All interesting periodic events should 
	 * be logged at this level so someone looking through the log can
	 * see the amount and kind of processing happening in the system.<p>
	 *
	 * The application may log at this level. E.g. the application 
	 * framework logs start, stop and command line options at this
	 * level.
	 */
	public static final int LOG_LEVEL_INFO = 4;

	/**
	 * Moderately detailed logging level to be used to help debug
	 * typical problems in the system.  Not so detailed that the
	 * big picture gets lost.
	 */
	public static final int LOG_LEVEL_DETAIL = 5;

	/**
	 * Most detailed logging level. Everything sent to the log will
	 * be logged. Use this level to trace system execution for really
	 * nasty problems.<p>
	 *
	 * The application may log at this level. E.g. SQL traces.<p>
	 *
	 * Avoid method-level tracing (that is, logging the entrance and
	 * exit to each method). Actually the BasicException provides
	 * enough stack trace information in case of errors.
	 */
	public static final int LOG_LEVEL_TRACE = 6;

	/**
	 * Performance logging level to be used to help finding performance
	 * problems in the system. This level is handled independently from 
	 * all other logging levels, meaning that it can be turned on and
	 * off without affecting the other levels.<p>
	 *
	 * The application may log at this level.
	 */
	public static final int LOG_PERFORMANCE = -1;
	
	
	/**
	 * Statistics logging level to be used to protocol application statistics
	 * This level is handled independently from all other logging levels, 
	 * meaning that it can be turned on and off without affecting the other 
	 * levels.<p>
	 *
	 * The application may log at this level.
	 */
	public static final int LOG_STATISTICS = -2;


	/**
	 * Notification logging level to be used to protocol log framework internal
	 * state changes. E.g. Openening and closing of logging mechanisms, 
	 * changeing logging level, ... 
	 * <p>
	 *
	 * The application MUST NOT log at this level.
	 */
	public static final int LOG_NOTIFICATION = -3;


	/**
	 * Minimal standard log level
	 */
	public static final int LOG_LEVEL_MIN = LOG_LEVEL_DEACTIVATE;

	/**
	 * Maximal standard log level
	 */
	public static final int LOG_LEVEL_MAX = LOG_LEVEL_TRACE;

}


