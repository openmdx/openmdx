/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LogUtil.java,v 1.4 2004/04/02 16:59:03 wfro Exp $
 * Description: Logging
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/04/02 16:59:03 $
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



import java.util.HashMap;


public class LogUtil
{
	private static HashMap logLevelConvertMap;


	static {
		LogUtil.logLevelConvertMap = new HashMap();

		// short form
		LogUtil.logLevelConvertMap.put("LP", new Integer(LogLevel.LOG_PERFORMANCE));
		LogUtil.logLevelConvertMap.put("LS", new Integer(LogLevel.LOG_STATISTICS));
		LogUtil.logLevelConvertMap.put("LN", new Integer(LogLevel.LOG_NOTIFICATION));
		LogUtil.logLevelConvertMap.put("L0", new Integer(LogLevel.LOG_LEVEL_DEACTIVATE));
		LogUtil.logLevelConvertMap.put("L1", new Integer(LogLevel.LOG_LEVEL_CRITICAL_ERROR));
		LogUtil.logLevelConvertMap.put("L2", new Integer(LogLevel.LOG_LEVEL_ERROR));
		LogUtil.logLevelConvertMap.put("L3", new Integer(LogLevel.LOG_LEVEL_WARNING));
		LogUtil.logLevelConvertMap.put("L4", new Integer(LogLevel.LOG_LEVEL_INFO));
		LogUtil.logLevelConvertMap.put("L5", new Integer(LogLevel.LOG_LEVEL_DETAIL));
		LogUtil.logLevelConvertMap.put("L6", new Integer(LogLevel.LOG_LEVEL_TRACE));

		// long form
		LogUtil.logLevelConvertMap.put("PERFORMANCE",  new Integer(LogLevel.LOG_PERFORMANCE));
		LogUtil.logLevelConvertMap.put("STATISTICS",   new Integer(LogLevel.LOG_STATISTICS));
		LogUtil.logLevelConvertMap.put("NOTIFICATION", new Integer(LogLevel.LOG_NOTIFICATION));
		LogUtil.logLevelConvertMap.put("DEACTIVATE",   new Integer(LogLevel.LOG_LEVEL_DEACTIVATE));
		LogUtil.logLevelConvertMap.put("CRITICAL",     new Integer(LogLevel.LOG_LEVEL_CRITICAL_ERROR));
		LogUtil.logLevelConvertMap.put("ERROR",        new Integer(LogLevel.LOG_LEVEL_ERROR));
		LogUtil.logLevelConvertMap.put("WARNING",      new Integer(LogLevel.LOG_LEVEL_WARNING));
		LogUtil.logLevelConvertMap.put("INFO",         new Integer(LogLevel.LOG_LEVEL_INFO));
		LogUtil.logLevelConvertMap.put("DETAIL",       new Integer(LogLevel.LOG_LEVEL_DETAIL));
		LogUtil.logLevelConvertMap.put("TRACE",        new Integer(LogLevel.LOG_LEVEL_TRACE));
	}



	/**
	 * Returns a string representation of the log level in a short format
	 * ("L1", "L2", ... , "LP", "LS")
	 *
	 * @param  logLevel  A log level
	 * @return String A String
	 */
	public static String logLevelToStringShort(int logLevel)
	{
		switch(logLevel) {
			case LogLevel.LOG_PERFORMANCE:
				return("LP");
			case LogLevel.LOG_STATISTICS:
				return("LS");
			case LogLevel.LOG_NOTIFICATION:
				return("LN");
			default:
				return("L" + String.valueOf(logLevel));
		}
	}


	/**
	 * Returns a string representation of the log level in a long format
	 * ("CRITICAL", "ERRROR", ... , "PERFORMANCE", "STATISTICS")
	 *
	 * @param  logLevel  A log level
	 * @return A stringified log level
	 */
	public static String logLevelToStringLong(int logLevel)
	{
		switch(logLevel) {
			case LogLevel.LOG_PERFORMANCE:
				return("PERFORMANCE");
			case LogLevel.LOG_STATISTICS:
				return("STATISTICS");
			case LogLevel.LOG_NOTIFICATION:
				return("NOTIFICATION");
			case LogLevel.LOG_LEVEL_DEACTIVATE:
				return("DEACTIVATE");
			case LogLevel.LOG_LEVEL_CRITICAL_ERROR:
				return("CRITICAL");
			case LogLevel.LOG_LEVEL_ERROR:
				return("ERROR");
			case LogLevel.LOG_LEVEL_WARNING:
				return("WARNING");
			case LogLevel.LOG_LEVEL_INFO:
				return("INFO");
			case LogLevel.LOG_LEVEL_DETAIL:
				return("DETAIL");
			case LogLevel.LOG_LEVEL_TRACE:
				return("TRACE");
			default:
				return("L?");
		}
	}


	/**
	 * Parses a log level in a short or a long format to integer log level
	 *
	 * @param  logLevel  A stringified log level
	 * @return int A log level
	 */
	public static int logLevelFromString(String logLevel)
	{
		Integer  level = null;

		if (logLevel != null) {
			level = (Integer)LogUtil.logLevelConvertMap.get(logLevel);
		}

		if (level == null) {
			return LogLevel.LOG_LEVEL_DEACTIVATE;
		}else{
			return level.intValue();
		}
	}

}


