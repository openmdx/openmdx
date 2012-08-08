/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SLF4JLoggingMechanism.java,v 1.6 2007/10/10 16:06:08 hburger Exp $
 * Description: SLF4J Logging Mechanism
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:08 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.kernel.log.slf4j;

import org.openmdx.kernel.log.LogEvent;
import org.openmdx.kernel.log.LogLevel;
import org.openmdx.kernel.log.impl.AbstractLoggingMechanism;
import org.openmdx.kernel.log.impl.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.spi.LocationAwareLogger;

/**
 * This logging mechanism delegates to SLF4J. 
 */
public class SLF4JLoggingMechanism 
	extends AbstractLoggingMechanism
{
	
	/**
	 * The logging mechanism instance
	 */
	private static final SLF4JLoggingMechanism singleton = new SLF4JLoggingMechanism();

	/**
	 * Constructor
	 */
	protected SLF4JLoggingMechanism(
	){		
	    super();
	}

	/**
	 * Returns the mechanism object. The mechanism is shared so it returns a 
	 * singleton
	 * 
	 * @return the mechanism singleton
	 */
	public static AbstractLoggingMechanism getInstance(
	){
		return SLF4JLoggingMechanism.singleton;
	}

	/** 
	 * Returns the name of the mechanism. 
	 * 
	 * @return The mechanism name
	 */
	public String getName(
	){
		return "SLF4JLoggingMechanism"; 
	}

	/** 
	 * This mechanism accepts statistics logs 
	 * 
	 * @return true to indicate that the mechanism accepts statistics
	 */
	public boolean acceptsStatisticLogs() {
		return true; 
	}

	/**
	 * Logs a log event.
	 * 
	 * @param log A logger
	 * @param event A log event
	 */
	protected void logEvent(
		Log       log, 
		LogEvent  event
	){
        //
        // Logger
        //
        String logClass = event.getClassName();
        Logger logger = LoggerFactory.getLogger(
            logClass == null ? Logger.ROOT_LOGGER_NAME : logClass
        );
        //
        // Marker
        //
        String appId = event.getAppId();
        Marker marker = appId == null ? null : MarkerFactory.getMarker(appId);
        //
        // Level
        //
        int logLevel = getLogLevel(
            logger,
            marker,
            event.getLoggingLevel()
        );
        if(logLevel != NULL) {
            //
            // Log
            //
            String msg = getFormatter().format(event);
            if(logger instanceof LocationAwareLogger) {
                ((LocationAwareLogger)logger).log(
                    marker,
                    log.getLogClassName(), 
                    logLevel,
                    msg,
                    null // Throwables are incorporated in message
                );
            } else {
                switch(logLevel) {
                    case LocationAwareLogger.ERROR_INT:
                        logger.error(marker, msg);
                        break;
                    case LocationAwareLogger.WARN_INT:
                        logger.warn(marker, msg);
                        break;
                    case LocationAwareLogger.INFO_INT:
                        logger.info(marker, msg);
                        break;
                    case LocationAwareLogger.DEBUG_INT:
                        logger.debug(marker, msg);
                        break;
                    case LocationAwareLogger.TRACE_INT:
                        logger.trace(marker, msg);
                        break;
                }
            }
        }
	}

	/**
	 * Maps an openMDX level to a <code>LocationAwareLogger</code> Level<ol>
	 * <li>ERROR &larr; CRITICAL_ERROR
	 * <li>ERROR &larr; ERROR
	 * <li>WARN &larr; WARNING
	 * <li>INFO &larr; INFO
	 * <li>DEBUG &lrarr; DETAIL
   * <li>TRACE &lrarr; TRACE
   * </ol><ul>
   * <li>INFO &larr; PERFORMANCE
   * <li>INFO &larr; STATISTIC
   * <li>INFO &larr; NOTIFICATION
	 * </ul>
   * @param level the openMDX log level
   * 
	 * @return the openMDX log level corresponding to a given log4j level.
	 */
	protected int getLogLevel(
        Logger logger,
        Marker marker,
		int level
	){
		switch(level) {
			case LogLevel.LOG_LEVEL_CRITICAL_ERROR:
			case LogLevel.LOG_LEVEL_ERROR:
          return logger.isErrorEnabled(marker) ? LocationAwareLogger.ERROR_INT : NULL;
			case LogLevel.LOG_LEVEL_WARNING:
          return logger.isWarnEnabled(marker) ? LocationAwareLogger.WARN_INT : NULL;
			case LogLevel.LOG_LEVEL_INFO:
          return logger.isInfoEnabled(marker) ? LocationAwareLogger.INFO_INT : NULL;
      case LogLevel.LOG_LEVEL_DETAIL:
          return logger.isDebugEnabled(marker) ? LocationAwareLogger.DEBUG_INT : NULL;
      case LogLevel.LOG_LEVEL_TRACE:
          return logger.isTraceEnabled(marker) ? LocationAwareLogger.TRACE_INT : NULL;
			default:
          return logger.isInfoEnabled(marker) ? LocationAwareLogger.INFO_INT : NULL;
		}
	}

    /**
     * 
     */
    protected static int NULL = -1;
    
}
