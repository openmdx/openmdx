/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LogFormatterStandard.java,v 1.1 2008/03/21 18:22:00 hburger Exp $
 * Description: Standard Log Formatter
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:22:00 $
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
import java.util.ArrayList;

import org.openmdx.compatibility.kernel.log.LogEvent;
import org.openmdx.compatibility.kernel.log.LogFormatter;
import org.openmdx.compatibility.kernel.log.LogLevel;

/**
 * This class provides a simple formatter for log events
 */
public class LogFormatterStandard
        implements LogFormatter
{
        /** The log event format for standard log events */
        public static final String DEFAULT_FORMAT =
                "${logger}|${time}|${level}|${logsource}|${host}|${thread}|" +
                "${class}|${method}|${line}|${summary}|${detail}";

        /** The log event format for statistics log events */
        public static final String DEFAULT_FORMAT_STATISTICS =
                "${logger}|${time}|LS|${logsource}|${host}|${group}|${record}";


        /** The field positions for a standard log entry */
        private final LogFormatField format[];

        private final String formatString;

        /** Date formatter */
        private SimpleDateFormat dateFormatter;


//	private static final String lineSeparator      = System.getProperty("line.separator");
        private static final String multiLineSeparator = System.getProperty("line.separator") + " ";



        /**
	 * Constructor.
	 */
        public LogFormatterStandard(
                String aFormat)
        {
                this.formatString = (aFormat != null) ? aFormat : DEFAULT_FORMAT;

                this.dateFormatter = new SimpleDateFormat(
                                                                LogFormatField.DEFAULT_DATE_FORMAT);

                this.format = LogFormatField.createFormat(this.formatString);
        }


    /**
     * Returns a String representation for the LogEvent object. Uses the
     * standard format.
	 *
     * <p>
     * This operation is <bold>not<bold> threadsafe.
     *
     * @return  a String representation
     */
    public String format(LogEvent event)
    {
        return formatter(event, this.format);
        }



    /**
     * Returns a String representation for the LogEvent object.
	 *
     * @return  a String representation
     */
    private String formatter(LogEvent event, LogFormatField[] fields)
    {
    	StringBuilder sb = new StringBuilder(512);
                int loggingLevel = event.getLoggingLevel();
                LogFormatField formatField;
                String value;


                for(int ii=0; ii<fields.length; ii++) {
                        formatField = fields[ii];

                        switch(formatField.field) {
                                case LogFormatField.FIELD_LOGGER:
                                    sb.append(event.getLogName());
                                break;
                                case LogFormatField.FIELD_TIME:
                                        // on demand creation of date formatter
                                        if (this.dateFormatter == null) {
                                                try {
                                                        this.dateFormatter = new SimpleDateFormat(
                                                                                                                formatField.format);
                                                }
                                                catch(IllegalArgumentException iae){
                                                        LogLog.error(
                                                                this.getClass(),
                                                                "htmlFormatter",
                                                                "Bad date format '" + formatField.format + "'",
                                                                null);
                                                        this.dateFormatter = new SimpleDateFormat(
                                                                                                        LogFormatField.DEFAULT_DATE_FORMAT);
                                                }
                                        }
                                        sb.append(this.dateFormatter.format(event.getTime()));
                                break;
                                case LogFormatField.FIELD_LEVEL:
                                    // for performance & memory reasons do not use
                                    // LogEvent.logLevelToStringShort(level) here
                                        switch(loggingLevel) {
                                                case LogLevel.LOG_PERFORMANCE:
                                                    sb.append("LP");
                                                        break;
                                                case LogLevel.LOG_STATISTICS:
                                                    sb.append("LS");
                                                        break;
                                                case LogLevel.LOG_NOTIFICATION:
                                                    sb.append("LN");
                                                        break;
                                                default:
                                                    sb.append("L").append(loggingLevel);
                                                        break;
                                        }
                                        break;
                                case LogFormatField.FIELD_LOGSOURCE:
                                        value = event.getLogSource();
                                if (value != null) sb.append(value);
                                break;
                                case LogFormatField.FIELD_CFGNAME:
                                    sb.append(event.getCfgName());
                                break;
                                case LogFormatField.FIELD_APPID:
                                    sb.append(event.getAppId());
                                break;
                                case LogFormatField.FIELD_HOST:
                                    sb.append(event.getHostname());
                                break;
                                case LogFormatField.FIELD_PID:
                                    sb.append(event.getProcessId());
                                break;
                                case LogFormatField.FIELD_THREAD:
                                    sb.append(event.getThreadname());
                                break;
                                case LogFormatField.FIELD_CLASS:
                                        value = event.getClassName();
                                if (value != null) sb.append(value);
                                break;
                                case LogFormatField.FIELD_METHOD:
                                        value = event.getMethodName();
                                if (value != null) sb.append(value);
                                break;
                                case LogFormatField.FIELD_LINE:
                                        int lineNr = event.getLineNr();
                                if (lineNr > 0) sb.append(lineNr);
                                break;
                                case LogFormatField.FIELD_SUMMARY:
                                        value = event.getLogStringSummary();
                                if (value != null) {
                                    // remove CR/LF
                        value.replace('\r', ' '); // CR
                        value.replace('\n', ' '); // LF
                        sb.append(value);
                                }
                                break;
                                case LogFormatField.FIELD_DETAIL:
                                        if (formatField.format != null && formatField.format.equals("p") ) {
                                                // plain, no multiline formatting, its faster but the
                                                // log entries are not properly parsable!
                                                value = event.getLogStringDetail();
                                        if (value != null) sb.append(value);
                                        }else{
                                                ArrayList<String> al = event.getLogStringDetailAsList();
                                                String line;
                                                int len = al.size();
                                                if (len == 0) break;
                                                if (len == 1) {
                                                    sb.append(al.get(0));
                                                }else{
                                                    sb.append(LogFormatter.multiLineMarker);
                                                        for(int jj=0; jj<len; jj++) {
                                                                line = (String)al.get(jj);
                                                                if ((jj == 0) && (line.length() == 0)) continue;
                                                                sb.append(
                                                                    LogFormatterStandard.multiLineSeparator
                                                                ).append(
                                                                    line
                                                                );
                                                        }
                                                }
                                        }
                                break;
                                case LogFormatField.FIELD_TEXT:
                                    sb.append(formatField.value);
                                break;
                        }
                }

        return sb.toString();
        }


    /**
     * Returns the currently set format string.
	 *
     * @return  A log format string
     */
    public String getFormat()
    {
        return this.formatString;
        }


        /**
	 * Returns log file format specifier.
	 * This specifier is written to the first line of each log file.
	 *
	 * @return  A log format specifier string
	 */
    public String getLogFileSpecifier()
    {
        return LogFormatter.formatSpecifierPrefix + getFormat();
        }

}
