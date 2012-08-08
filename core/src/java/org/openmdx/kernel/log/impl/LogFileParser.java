/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LogFileParser.java,v 1.5 2007/10/10 17:16:07 hburger Exp $
 * Description: Log file parser
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 17:16:07 $
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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmdx.kernel.log.LogEntityReader;
import org.openmdx.kernel.log.LogEvent;
import org.openmdx.kernel.log.LogEventFilter;
import org.openmdx.kernel.log.LogFormatter;
import org.openmdx.kernel.log.LogLevel;


/**
 * This class parses log files into LogEvents
 */
public class LogFileParser implements LogEntityReader {

	/**
	 * Create a new LogFileParser.
	 */
    public LogFileParser(File  aFile)
    {
    	this.file = aFile;
    }

    //-----------------------------------------------------------------------
    private static abstract class EventWriter {
        public abstract void write(
            LogEvent event
        ) throws IOException;
        
    }

    //-----------------------------------------------------------------------
    private static class ObjectOutputStreamEventWriter 
        extends EventWriter {
        
        public ObjectOutputStreamEventWriter(
            ObjectOutputStream os
        ) {
            this.os = os;
        }

        public void write(
            LogEvent event
        ) throws IOException {
            os.writeObject(event);
            if(count % 20 == 0) {
                os.reset();
            }
            count++;
        }
        
        private final ObjectOutputStream os;
        private int count = 0;
    }
    
    //-----------------------------------------------------------------------
    private static class ListEventWriter 
        extends EventWriter {
        
        public ListEventWriter(
            List l
        ) {
            this.l = l;
        }
        public void write(
            LogEvent event
        ) throws IOException {
            l.add(event);
        }
        
        private final List l;
    }

    //-----------------------------------------------------------------------
    /**
	 * Returns the size of the log entity in bytes
	 *
	 * @return    The log entity size
	 * @exception IOException Thrown if file does not exist or cannot be read
	 * @exception UnsupportedOperationException if the entity size is not 
	 *              supported on the currently used log entity
	 */
	public long size()
		throws IOException
	{
    	if (!this.file.exists()) {
    		throw new IOException("File " + this.file.getCanonicalPath() + " does not exist");
    	}

    	if (!this.file.canRead()) {
    		throw new IOException("File " + this.file.getCanonicalPath() + " cannot be read");
    	}
    	
    	return this.file.length();
	}
	
	/**
	 * Parses the specified log file for log events. Returns at most <code>
	 * events</code> log events. If the start position is >0 the parser seeks
	 * to next starting log event.
	 * <p>
	 * The first line in the log file holds the log event format!
	 * <p>
	 * If the file position is a positive number the reading starts from the
	 * given file position. If the given file position is a negative number
	 * it is used as offset from the end of the file.
	 * <p>
	 * Be prepared that the requested number of events can be limited.
	 * <p>
	 * The parser returns if either the end of the file is reached, the max
	 * number of events is parsed or the max processing time is exceeded.
	 *
	 * @param aFile                A log file to be parsed
	 * @param startPos             The start position where the parsing starts
	 * @param maxEvents            The max number of events to be processed
	 * @param maxProcessingTime    The max processing time in milliseconds. 
	 *                              A value of 0 means no time limit.
	 * @param filter               A log event filter 
	 * @param eventList            The event list to be filled with the parsed 
	 *                              {@link LogEvent} objects
	 * @return    The file position
	 * @exception IOException Thrown if file does not exist or cannot be read
	 */
	private long readLogEvents(
        long _startPos,
        int _maxEvents,
        long maxProcessingTime,
        LogEventFilter filter,
        EventWriter eventWriter
    ) throws IOException {
    	if (!this.file.exists()) {
    		throw new IOException("File " + this.file.getCanonicalPath() + " does not exist");
    	}

    	if (!this.file.canRead()) {
    		throw new IOException("File " + this.file.getCanonicalPath() + " cannot be read");
    	}

		String logEventFormat = readLogFileSpecifier(this.file);
		if (logEventFormat == null) {
    		throw new IOException("File " + this.file.getCanonicalPath() +
    		                      " cannot read log file event format specifier at line 1");
		}

		LogFormatField formatFields[] = LogFormatField.createFormat(logEventFormat);

 		int maxEvents = Math.min(_maxEvents, MAX_EVENTS);  // limit

		long size = this.file.length();  // current file size
		long startPos = _startPos >= 0 ?
			Math.min(_startPos, size) : // position relative from file start
		    Math.max(size + _startPos, 0); // position relative from file end

		long             currPos = startPos;
		long             pos = 0;
		int              eventCount = 0;
		String           line = null;
		ArrayList        lineList;
		RandomAccessFile raf = new RandomAccessFile(this.file, "r");
		long             startTime = System.currentTimeMillis();
		LogEvent         event = null;
		boolean		  isMultiLine = false;
		LogEventFilter   logEventFilter = (filter != null) ? filter : new LogEventFilter();

		if (startPos == 0) {
	    	raf.seek(startPos);
			line = raf.readLine();  // skip
		}else{
			// synchronize to the beginning of a new line
	    	raf.seek(startPos-1);
			char c = raf.readChar();
			if (c != '\n') { raf.readLine(); }
		}

		// synchronize to the beginning of new log event (a line that does
		// not start with a SPACE character
		pos = raf.getFilePointer();  // remember pos
    	line = raf.readLine();
    	if (line == null) return currPos; //EOF
		while(line.startsWith(" ")) {
			pos = raf.getFilePointer();
	    	line = raf.readLine();
	    	if (line == null) return currPos; //EOF
		}

		currPos = pos;    // The file pos of that log event

		// read log events one event per loop
		while(true) {
			long  currTime = System.currentTimeMillis();
			lineList = new ArrayList();
			
			// stop if the max number of events is read
			if (eventCount >= maxEvents) break;
			
			// stop if the processing exceeds the specified duration
			if ((maxProcessingTime > 0) && ((currTime - startTime) > maxProcessingTime)) {
				break;
			}

	
			// The first line of the log event
			if (line == null) {
				pos = raf.getFilePointer();
				line = raf.readLine();
				if (line == null) break; // EOF 
				currPos = pos; // The file pos of a log event
			}
			
			lineList.add(line);
			
			// A log event with a multi line detail string?
			isMultiLine = line.endsWith(LogFormatter.multiLineMarker);

			line = null;  // this line is processed 

			if (isMultiLine) {
				// check for multiline detail log strings
				pos  = raf.getFilePointer();
		    	line = raf.readLine();
		    	
		    	if (line != null) {
					while((line != null) && line.startsWith(" ")) {
						pos = raf.getFilePointer();
		    			lineList.add(line);
				    	line = raf.readLine();
					}
					if (line != null) currPos = pos; // pos at next log event
		    	}
			}
					
			try {
				event = parseLogEvent(logEventFilter, lineList, formatFields);
				if (event != null) {
					eventCount += 1;
					eventWriter.write(event);
				}
			}catch(Exception ex) {
			    // ignore
			}
		}

    	return currPos;
	}

	//-----------------------------------------------------------------------
	public long readLogEvents(
        long startPos,
        int maxEvents,
        long maxProcessingTime,
        LogEventFilter filter,
        ArrayList events
    ) throws IOException {
	    events.clear();
        return this.readLogEvents(
            startPos,
            maxEvents,
            maxProcessingTime,
            filter,
            new ListEventWriter(events)
        );
	}

	//-----------------------------------------------------------------------
	public long readLogEvents(
        long startPos,
        int maxEvents,
        long maxProcessingTime,
        LogEventFilter filter,
        ObjectOutputStream target
    ) throws IOException {
        return this.readLogEvents(
            startPos,
            maxEvents,
            maxProcessingTime,
            filter,
            new ObjectOutputStreamEventWriter(target)
        );
	}

	//-----------------------------------------------------------------------
	/**
	 * Reads from a start position a given number of bytes from the log
	 * file.
	 *
	 * @param startPos             The start position where the parsing starts
	 * @param buffer               The allocated buffer to receive the file data
	 * @return    The number of bytes read. 0 indicates EOF
	 * @exception IOException Thrown if file does not exist or cannot be read
	 */
	public int readBinary(
			long       startPos,
			byte[]     buffer)
		throws IOException
	{
    	if (!this.file.exists()) {
    		throw new IOException("File " + this.file.getCanonicalPath() + " does not exist");
    	}

    	if (!this.file.canRead()) {
    		throw new IOException("File " + this.file.getCanonicalPath() + " cannot be read");
    	}


		RandomAccessFile raf = new RandomAccessFile(this.file, "r");

		
	   	long fileLen = raf.length();
		if (startPos >= fileLen) {
			return 0;
		}

		// limit the number of bytes read
		long len = fileLen - startPos;
		if (len > buffer.length) len = buffer.length;
		
		int bytesRead = 0;
				
		try {
			raf.seek(startPos);
			bytesRead = raf.read(buffer, 0, (int)len);
		}catch(EOFException  ex) {
			// should not happen unless someone shortened the file
			return 0;
		}
		
		return bytesRead;
	}



	private LogEvent parseLogEvent(
			LogEventFilter   filter,
			ArrayList 		 lines,
			LogFormatField[] formatFields)
		throws Exception
	{
		int            currPos = 0;
		int            pos = 0;
		String         line = (String)lines.get(0);
		String         field = null;
		LogEvent       event = new LogEvent();
		LogFormatField formatField = null;
		LogFormatField lastFormatField = null;


		// parse the Fields
		for(int ii=0; ii<formatFields.length; ii++) {
			formatField = formatFields[ii];
			
			if (formatField.field == LogFormatField.FIELD_TEXT) {
				// this is the delimiter and as that the sync point
				pos = line.indexOf(formatField.value, currPos);
				if (pos == currPos) {
					field = new String();
				}else if (pos > currPos) {
					field = line.substring(currPos, pos);
				}else{
					field = line.substring(currPos);
				}
		
				// parse the field		
				parseField(field, lastFormatField, event);
				 
				// adjust position
				currPos = pos + formatField.value.length();
			}else{
				lastFormatField = formatField;
			}
		}
	

		// process last field (read upto EOL)		
		field = line.substring(currPos);
		parseField(field, lastFormatField, event);
	
		// if the last field is a FIELD_DETAIL process multine detail info
		if (lastFormatField.field == LogFormatField.FIELD_DETAIL) {
			// only for multiline detail events, single line detail events
			// are processed above
			if (lines.size() > 1) {
				lines.remove(0);
				event.setLogStringDetail(lines);
			}
		}

		// Filter the event
		switch(event.getLoggingLevel()) {
			case LogLevel.LOG_PERFORMANCE: 
				if (!filter.getPerformance()) return null;
				break;
			case LogLevel.LOG_STATISTICS:
				if (!filter.getStatistics()) return null;
				break;
			case LogLevel.LOG_NOTIFICATION:
				if (!filter.getNotification()) return null;
				break;
			default:
				if (event.getLoggingLevel() > filter.getLoggingLevel()) return null;
				break;
		}

		return event;
	}




	private void parseField(
			String           field,
			LogFormatField   formatField,
			LogEvent         event)
		throws Exception
	{
		switch(formatField.field) {
			case LogFormatField.FIELD_LOGGER:
				event.setLogName(field);
		        break;
		        
			case LogFormatField.FIELD_TIME:
				// on demand creation of date formatter
				if (this.formatDate == null) {
					this.formatDate = new SimpleDateFormat(formatField.format);
				}
				// parse date
				Date date = this.formatDate.parse(field, new ParsePosition(0));
				if (date == null) {
					throw new Exception("LogEvent parser: Bad timestamp '" + field + "'");
				}
				event.setTime(date);
		        break;
		        
			case LogFormatField.FIELD_LEVEL:
				if ((field.length() == 2) && (field.charAt(0) == 'L')) {
					char level = field.charAt(1);
					switch(level) {
						case 'P': event.setLoggingLevel(LogLevel.LOG_PERFORMANCE); break;
						case 'S': event.setLoggingLevel(LogLevel.LOG_STATISTICS); break;
						case 'N': event.setLoggingLevel(LogLevel.LOG_NOTIFICATION); break;
						case '0': event.setLoggingLevel(LogLevel.LOG_LEVEL_DEACTIVATE); break;
						case '1': event.setLoggingLevel(LogLevel.LOG_LEVEL_CRITICAL_ERROR); break;
						case '2': event.setLoggingLevel(LogLevel.LOG_LEVEL_ERROR); break;
						case '3': event.setLoggingLevel(LogLevel.LOG_LEVEL_WARNING); break;
						case '4': event.setLoggingLevel(LogLevel.LOG_LEVEL_INFO); break;
						case '5': event.setLoggingLevel(LogLevel.LOG_LEVEL_DETAIL); break;
						case '6': event.setLoggingLevel(LogLevel.LOG_LEVEL_TRACE);  break;
						default:
							throw new Exception("LogEvent parser: Bad log level '" + field + "'");
					}
				}
				break;
				
			case LogFormatField.FIELD_LOGSOURCE:
				event.setLogSource(field);
		        break;
		        
			case LogFormatField.FIELD_CFGNAME:
				event.setCfgName(field);
		        break;
		        
			case LogFormatField.FIELD_APPID:
				event.setAppId(field);
		        break;
		        
			case LogFormatField.FIELD_HOST:
				event.setHostname(field);
		        break;
		        
			case LogFormatField.FIELD_PID:
				event.setProcessId(field);
		        break;
		        
			case LogFormatField.FIELD_THREAD:
				event.setThreadname(field);
		        break;
		        
			case LogFormatField.FIELD_CLASS:
				event.setClassName(field);
		        break;
		        
			case LogFormatField.FIELD_METHOD:
				event.setMethodName(field);
		        break;
		        
			case LogFormatField.FIELD_LINE:
				if (field.length() == 0) {
					event.setLineNr(-1);
				}else{
					try {
						event.setLineNr(new Integer(field).intValue());
					}catch(NumberFormatException ex) {
						throw new Exception("LoggEvent parser: Bad lineNr '" + field + "'");
					}
				}
		        break;
		        
			case LogFormatField.FIELD_SUMMARY:
				event.setLogStringSummary(field);
		        break;
		        
			case LogFormatField.FIELD_DETAIL:
				event.setLogStringDetail(field);
		        break;
		        
			case LogFormatField.FIELD_TEXT:
		        break;
		}
	}
	

	/**
	 * Reads the log file format specifier from the first line of a log file
	 *
	 * @param aFile     A log file to be parsed
	 * @return          The log file format specifier
	 * @exception IOException Thrown if file does not exist or cannot be read
	 */
	private String readLogFileSpecifier(File aFile)
	{
		BufferedReader reader    = null;
		String         specifier = null;

		try {
			 reader = new BufferedReader(new FileReader(aFile));

			// read first line
			if (reader.ready()) {
				String line = reader.readLine();
				if (line.startsWith(LogFormatter.formatSpecifierPrefix)) {
					specifier = line.substring(LogFormatter.formatSpecifierPrefix.length());
				}
			}
		}catch(IOException ex) {
		    // ignore
		}finally{
			if (reader != null) {
				try { reader.close(); }catch(IOException ex) {
				    // ignore
				}
			}
		}

		return specifier;
	}


    /** Limited events read */
	private final int  MAX_EVENTS = 1000;

    /** Date formatter */
	private SimpleDateFormat formatDate;
	
	/** The processed file */
	private File  file = null;
	}



