/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LogEvent.java,v 1.9 2006/08/11 09:24:13 hburger Exp $
 * Description: Logging
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/08/11 09:24:13 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import org.openmdx.kernel.text.StringBuilders;

/**
 * This class holds represents the log event parameters
 */

public class LogEvent
    implements Serializable {

        /**
	 * Create a new LogEvent with all elements set to their defaults.
	 * <code>String</code> and <code>Date</code> members are set to
	 * <code>null</code>, int member are set to <code>0</code>
	 */
    public LogEvent()
    {
        reset();
    }

        /**
	 * Create a new LogEvent
	 *
	 *
     * @param logName      The name of the logger
     * @param timestamp    The timestamp of the log event
     * @param logLevel     The error level
     * @param cfgName      The log config name
     * @param appId        The log application id
     * @param logSource    The log event source
     * @param hostName     The host name that caused the log
     * @param processID    The process ID from the process that caused the log
     * @param threadName   The thread name that caused the log
     * @param className    The class that caused the log
     * @param methodName   The method that caused the log
     * @param lineNr       The linenr that caused the log
     * @param summary      A summary log string
     * @param detail       A detail log string
	 **/
    public LogEvent(
                        String logName,
                        Date timestamp,
                        int logLevel,
                        String cfgName,
                        String appId,
                        String logSource,
                        String hostName,
                        String processID,
                        String threadName,
                        String className,
                        String methodName,
                        int lineNr,
                String summary,
                        String detail)
        {
                this.logName = logName;
                this.time = timestamp;
                this.loggingLevel = logLevel;
                this.cfgName = cfgName;
                this.appId = appId;
                this.source = logSource;
                this.hostname = hostName;
                this.processid = processID;
                this.threadname = threadName;
                this.className = className;
                this.methodName = methodName;
                this.lineNr = lineNr;
                this.logStringSummary = fixSummary(summary);
                this.logStringDetail = detail;
    }

        /**
 	 * Resets the log event.
	 * <code>String</code> and <code>Date</code> members are set to
	 * <code>null</code>, int member are set to <code>0</code>
 	 */
        public void reset()
        {
                this.logName = null;
                this.time = null;
                this.loggingLevel = 0;
                this.hostname = null;
                this.cfgName = null;
                this.appId = null;
                this.source = null;
                this.processid = null;
                this.threadname = null;
                this.className = null;
                this.methodName = null;
                this.lineNr = 0;
                this.logStringSummary = null;
                this.logStringDetail = null;
        }

        /**
 	 * Returns the log name
 	 *
 	 * @return A log name
 	 */
    public String getLogName() { return this.logName; }

        /**
 	 * Returns the hostname
 	 *
 	 * @return A hostname
 	 */
    public String getHostname() { return this.hostname; }

        /**
 	 * Returns the threadname
 	 *
 	 * @return A threadname
 	 */
    public String getThreadname() { return this.threadname; }

        /**
 	 * Returns the processid
 	 *
 	 * @return A process id
 	 */
    public String getProcessId() { return this.processid; }

        /**
 	 * Returns the config name
 	 *
 	 * @return An config name
 	 */
    public String getCfgName() { return this.cfgName; }

        /**
 	 * Returns the application name view
 	 *
 	 * @return An application name view
 	 */
    public String getAppId() { return this.appId; }

        /**
 	 * Returns the application name
 	 *
 	 * @return An application name
     * @deprecated use {@link LogEvent#getAppId()} instead
 	 */
    public String getAppName() { return this.appId; }

        /**
 	 * Returns the source
 	 *
 	 * @return A source
 	 */
    public String getLogSource() { return this.source; }

        /**
 	 * Returns the time
 	 *
 	 * @return A date
 	 */
    public Date getTime() { return this.time; }

        /**
 	 * Returns the log string summary
 	 *
 	 * @return A summary log string
 	 */
    public String getLogStringSummary() { return this.logStringSummary; }

        /**
 	 * Returns the log string detail. If a single detail string has been set, it
 	 * is preferably returned otherwise the set ArrayList is returned as a
 	 * single string. 
 	 *
 	 * @return A detail log string
 	 */
    public String getLogStringDetail()
    {
        if (this.logStringDetail != null) {
                return this.logStringDetail;
        }else{
                if (this.logStringDetailList != null) {
                CharSequence sb = StringBuilders.newStringBuilder();
                        int len = this.logStringDetailList.size();
                        for(int ii=0; ii<len; ii++) {
                            StringBuilders.asStringBuilder(sb).append(this.logStringDetailList.get(ii));
                            if (ii<(len-1)) StringBuilders.asStringBuilder(sb).append(lineSeparator);
                        }
                        return sb.toString();
                } else {
                        return "";
                }
        }
    }

        /**
 	 * Returns the log string detail. If an ArrayList of detail string has been 
 	 * set, it is preferably returned otherwise the set string is broken up
 	 * into lines and returned as an ArrayList of Strings. 
 	 *
 	 * @return A detail log string
 	 */
    public ArrayList getLogStringDetailAsList()
    {
                if (this.logStringDetailList != null) {
                        return this.logStringDetailList;
        }else{
                ArrayList al = new ArrayList();

                if (this.logStringDetail != null) {
                        // Check for LineFeed characters
                        if (this.logStringDetail.indexOf('\n') < 0) {
                                al.add(this.logStringDetail);
                                return al;
                        }

                        // Break the string up into single lines    				
                                char strArr[] = this.logStringDetail.toCharArray();
                                char c = '.', last;
                                int startPos = 0;

                                int ii;
                                for(ii=0; ii<strArr.length; ii++) {
                                        last = c;
                                        c = strArr[ii];
                                        if (c == '\r' || c == '\n') {
                                                if (last != '\r') {
                                                        if (ii-startPos > 0) {
                                                        al.add(new String(strArr, startPos, ii-startPos));
                                                        }else{
                                                                al.add(new String());
                                                        }
                                                }

                                                startPos = ii+1;
                                        }
                                }

                                // Flush last line
                                if (startPos < ii) {
                                        al.add(new String(strArr, startPos, ii-startPos));
                                }
                }
                return al;
                }
    }

        /**
 	 * Returns the class name
 	 *
 	 * @return A class name
 	 */
    public String getClassName() { return this.className; }

        /**
 	 * Returns the method name
 	 *
 	 * @return A method name
 	 */
    public String getMethodName() { return this.methodName; }

        /**
 	 * Returns the logging level
 	 *
 	 * @return A logging level
 	 */
    public int getLoggingLevel() { return this.loggingLevel; }

        /**
 	 * Returns the line number
 	 *
 	 * @return A line number
 	 */
    public int getLineNr() { return this.lineNr; }

        /**
 	 * Returns true if the log event is a performance log event
 	 *
 	 * @return A boolean
 	 */
    public boolean isPerformanceEvent()
    { return (this.loggingLevel == LogLevel.LOG_PERFORMANCE); }

        /**
 	 * Returns true if the log event is a statistics log event
 	 *
 	 * @return A boolean
 	 */
    public boolean isStatisticsEvent()
    { return (this.loggingLevel == LogLevel.LOG_STATISTICS); }

        /**
 	 * Returns true if the log event is a notification log event
 	 *
 	 * @return A boolean
 	 */
    public boolean isNotificationEvent()
    { return (this.loggingLevel == LogLevel.LOG_NOTIFICATION); }

        /**
 	 * Sets the log name
 	 *
 	 * @param value A log name
 	 */
    public void setLogName(String value) { this.logName = value; }

        /**
 	 * Sets the hostname
 	 *
 	 * @param value A hostname
 	 */
    public void setHostname(String value) { this.hostname = value; }

        /**
 	 * Sets the threadname
 	 *
 	 * @param value A threadname
 	 */
    public void setThreadname(String value) { this.threadname = value; }

        /**
 	 * Sets the processid
 	 *
 	 * @param value A process id
 	 */
    public void setProcessId(String value) { this.processid = value; }

        /**
 	 * Sets the config name
 	 *
 	 * @param value An config name
 	 */
    public void setCfgName(String value) { this.cfgName = value; }

        /**
 	 * Sets the application id
 	 *
 	 * @param value An application id
 	 */
    public void setAppId(String value) { this.appId = value; }

        /**
 	 * Sets the application name
 	 *
 	 * @param value An application name
     * @deprecated use {@link LogEvent#setAppId(String)} instead
 	 */
    public void setAppName(String value) { this.appId = value; }

        /**
 	 * Sets the source
 	 *
 	 * @param value A source
 	 */
    public void setLogSource(String value) { this.source = value; }

        /**
 	 * Sets the time
 	 *
 	 * @param date A date
 	 */
    public void setTime(Date date) { this.time = date; }


        /**
 	 * Sets the log string summary
 	 *
 	 * @param value A summary log string
 	 */
    public void setLogStringSummary(String value) { this.logStringSummary = fixSummary(value); }

        /**
 	 * Sets the log string detail. You may either use <code>setLogStringDetail(String)</code>
 	 * or <code>setLogStringDetail(ArrayList)</code>. The passed string may 
 	 * contain CR, LF characters 
 	 * 
 	 * @param value A detail log string. The st
 	 */
    public void setLogStringDetail(String value) { this.logStringDetail = value; }

        /**
 	 * Sets the log string detail. You may either use <code>setLogStringDetail(String)</code>
 	 * or <code>setLogStringDetail(ArrayList)</code>.
 	 *
 	 * @param list   A list of detail log string. The strings must not contain
 	 *                CR or LF characters
 	 */
    public void setLogStringDetail(ArrayList list)
    {
        this.logStringDetailList = list;
    }

        /**
 	 * Sets the class name
 	 *
 	 * @param vale A class name
 	 */
    public void setClassName(String value) { this.className = value; }

        /**
 	 * Sets the method name
 	 *
 	 * @param value A method name
 	 */
    public void setMethodName(String value) { this.methodName = value; }

        /**
 	 * Sets the logging level
 	 *
 	 * @param value A logging level
 	 */
    public void setLoggingLevel(int value) { this.loggingLevel = value; }

        /**
 	 * Sets the line number
 	 *
 	 * @param value A line number
 	 */
    public void setLineNr(int value) { this.lineNr = value; }


        /**
	 * Fixes log summary strings. Summary strings may not contain
	 * '\r' and '\n' characters. These characters are silently removed.
	 *
	 * @param str  A log summary string
	 * @return     A fixed string without '\r' and '\n' characters
	 */
        private String fixSummary(String str)
        {
                if ((str == null) || (str.length() == 0)) {
                        return "";
                }
                else if (str.indexOf('\n') < 0) {
                        return str;
                }
                else{
                        char strArr[] = str.toCharArray();
            CharSequence sBuf = StringBuilders.newStringBuilder(strArr.length);
                        char c = '.';
                        int startPos = 0;

                        for(int ii=0; ii<strArr.length; ii++) {
                                c = strArr[ii];
                                if (c == '\r' || c == '\n') {
                                        if (ii>startPos) StringBuilders.asStringBuilder(sBuf).append(strArr, startPos, ii-startPos);
                                        startPos = ii+1;
                                }
                        }

                        return sBuf.toString();
                }
        }

        //-----------------------------------------------------------------------
        // Members
        //-----------------------------------------------------------------------
    static final long serialVersionUID = -436415147593542117L;

    private String logStringSummary;
        private String logStringDetail;
        private int loggingLevel;
        private String className;
        private String methodName;
        private int lineNr;

        private String logName;
        private String cfgName;
        private String appId;
        private String source;
    private String hostname;
    private String processid;
    private String threadname;
    private Date time;

    private ArrayList logStringDetailList;

        private static final String lineSeparator = System.getProperty("line.separator");
}

//--- End of File -----------------------------------------------------------
