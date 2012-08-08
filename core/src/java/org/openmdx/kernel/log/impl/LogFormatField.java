/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LogFormatField.java,v 1.2 2004/04/02 16:59:04 wfro Exp $
 * Description: Logging
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/04/02 16:59:04 $
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
package org.openmdx.kernel.log.impl;


import java.util.ArrayList;
import java.util.StringTokenizer;



/**
 * This class holds represents the log event parameters
 */

public class LogFormatField implements Cloneable {

	public LogFormatField(int field)
	{
		this.field  = field;
		this.value  = null;
		this.format = null;
	}


	public LogFormatField(int field, String value)
	{
		this.field  = field;
		this.value  = value;
		this.format = null;
	}

	public LogFormatField(int field, String value, String format)
	{
		this.field  = field;
		this.value  = value;
		this.format = format;
	}
	

	public int getField() { return this.field; }
	
	public String getValue() { return this.value; }

	public String getFormat() { return this.format; }
	
	
	public Object clone()
	{
		try {
			return super.clone();
		}catch(CloneNotSupportedException ex) { return null; }
	}
	
    /**
     * Factory method to create the format information array
     *
     * The <code>logFormat</code> string follows the format
     *
     * <p>
     * E.g.:  "${logger}|${time}|${level}|${class}|${method}|${line}|${summary}"
	 *
	 * <p>
	 * Possible values are:
	 *
	 * <pre>
	 *     ${logger}
	 *     ${time}
	 *     ${level}
	 *     ${cfgname}
	 *     ${appid}
	 *     ${logsource}
	 *     ${host}
	 *     ${pid}
	 *     ${thread}
	 *     ${class}
	 *     ${method}
	 *     ${line}
	 *     ${summary}
	 *     ${detail}
	 *     ${group}
	 *     ${record}
	 * </pre>
     *
     * @param logFormat A log format string
     */
	public  static LogFormatField[] createFormat(String logFormat)
	{
		String      token, field, format;
		ArrayList	arr = new ArrayList();


		StringTokenizer  stk = new StringTokenizer(logFormat, "${}", true);

		while(stk.hasMoreElements()) {
			token = stk.nextToken();

			if (!token.equals("$")) {
				arr.add(new LogFormatField(FIELD_TEXT, token));
				continue;
			}

			if (!stk.hasMoreElements()) {
				arr.add(new LogFormatField(FIELD_TEXT, token)); // only a "$"
				break;
			}

			token = stk.nextToken(); // '{' expected

			if (!token.equals("{")) {
				arr.add(new LogFormatField(FIELD_TEXT, "$" + token)); // only a "$xxx"
				continue;
			}

			if (!stk.hasMoreElements()) {
				arr.add(new LogFormatField(FIELD_TEXT, "${")); // only a "${"
				break;
			}

			field  = stk.nextToken(); // a field name
			format = null;

			// check if a format specifier is available
			int pos = field.indexOf(":");
			if (pos >= 0) {
				format = field.substring(pos+1);			
				field  = field.substring(0, pos);			
			}

			if (stk.hasMoreElements()) {
				token = stk.nextToken(); // '}' expected
				if (token.equals("}")) {
					LogFormatField obj = lookup(field);
					if (obj != null) {
						// override default format if a fromat has been specified
						if (format != null) {
							obj.format = format;
						}
						arr.add(obj);
					}else{
						// unknown field
						arr.add(new LogFormatField(FIELD_TEXT, "${" + field + "}"));
					}
				}else{
					// missing closing bracket return a "${xxxyyy"
					arr.add(new LogFormatField(FIELD_TEXT, "${" + field + token));
				}
			}else{
				arr.add(new LogFormatField(FIELD_TEXT, "${" + token)); // only a "${xxx"
			}
		}

		return (LogFormatField[])arr.toArray(new LogFormatField[arr.size()]);
	}



	private static LogFormatField lookup(String name) 
	{
		for(int ii=0; ii<FIELDS.length; ii++) {
			if (name.equals(FIELDS[ii].value)) {
				return (LogFormatField)FIELDS[ii].clone();
			}
		}
		
		return null;
	}

	/** The default date format */
	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";


	// Constants describing the possible fields in a log entry
	public final static int FIELD_LOGGER      =  0;  // ${logger}
	public final static int FIELD_TIME        =  1;  // ${time}
	public final static int FIELD_LEVEL       =  2;  // ${level}
	public final static int FIELD_CFGNAME     =  3;  // ${cfgname}
	public final static int FIELD_APPID       =  4;  // ${appid}
	public final static int FIELD_LOGSOURCE   =  5;  // ${logsource}
	public final static int FIELD_HOST        =  6;  // ${host}
	public final static int FIELD_PID         =  7;  // ${pid}
	public final static int FIELD_THREAD      =  8;  // ${thread}
	public final static int FIELD_CLASS       =  9;  // ${class}
	public final static int FIELD_METHOD      = 10;  // ${method}
	public final static int FIELD_LINE        = 11;  // ${line}
	public final static int FIELD_SUMMARY     = 12;  // ${summary}
	public final static int FIELD_DETAIL      = 13;  // ${detail}
	public final static int FIELD_TEXT        = 14;  // text field
	public final static int FIELD_GROUP       = FIELD_SUMMARY;  // ${group}
	public final static int FIELD_RECORD      = FIELD_DETAIL;   // ${record}


	// field - name - format association
	private final static LogFormatField FIELDS[] = {
		 new LogFormatField(FIELD_LOGGER,    "logger",    null),
         new LogFormatField(FIELD_TIME,      "time" ,     DEFAULT_DATE_FORMAT),
         new LogFormatField(FIELD_LEVEL,     "level",     null),
         new LogFormatField(FIELD_CFGNAME,   "cfgname",   null),
         new LogFormatField(FIELD_APPID,     "appid",     null),
         new LogFormatField(FIELD_LOGSOURCE, "logsource", null),
         new LogFormatField(FIELD_HOST,      "host",      null),
         new LogFormatField(FIELD_PID,       "pid",       null),
         new LogFormatField(FIELD_THREAD,    "thread",    null),
         new LogFormatField(FIELD_CLASS,     "class",     null),
         new LogFormatField(FIELD_METHOD,    "method",    null),
         new LogFormatField(FIELD_LINE,      "line",      null),
         new LogFormatField(FIELD_SUMMARY,   "summary",   null),
         new LogFormatField(FIELD_DETAIL,    "detail",    null),
         new LogFormatField(FIELD_GROUP,     "group",     null),
         new LogFormatField(FIELD_RECORD,    "record",    null)
    };

	public int     field;
	public String  value;
	public String  format;
}



