/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractLoggingMechanism.java,v 1.1 2008/03/21 18:21:58 hburger Exp $
 * Description: Abstract Logging Mechanism
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:21:58 $
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
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.openmdx.compatibility.kernel.log.LogEntity;
import org.openmdx.compatibility.kernel.log.LogEntityReader;
import org.openmdx.compatibility.kernel.log.LogEvent;
import org.openmdx.compatibility.kernel.log.LogFormatter;
import org.openmdx.compatibility.kernel.log.LogLevel;
import org.openmdx.compatibility.kernel.log.LogUtil;
import org.openmdx.compatibility.kernel.log.ManageableMechanism;
import org.openmdx.compatibility.kernel.log.SysLog;


/**
 * AbstractLoggingMechanism is an abstract class which serves to define a
 * hierarchy of different logging mechanisms.  Subclasses of
 * this class will implement the different mechanisms defined herein.
 *
 * The main responsibility of this class hierarchy is to offload the
 * implementation work for differences in logging streams for different
 * mechanisms.  For example, the standard error stream is a very different
 * implementation than a shared local output file.
 */

public abstract class AbstractLoggingMechanism 
	implements ManageableMechanism
{
	/**
	 * Loggers call this method to register themselves with the mechanism.
	 * It will automatically open the mechanism if needed and log the fact
	 * that a logger started using this mechanism.
	 * 
	 * @param log            A logger
	 * @param loggingLevel   An initial logging level
	 */
	synchronized 
	void openForLog(
		Log log, 
		int loggingLevel) 
	{
		// If no loggers are registered yet, this must be the first one and
		// the mechanism must be openend.
		if (this.loggers.size() <= 0) {

			// A shared mechanism must be configured using SysLog properties!
			LogProperties logProperties = 
				isSharedLog() 
					? SysLog.getLogger().getLogProperties()
					: log.getLogProperties();

			// Take care for lazy logging configuration and mechanism loading. 
			// If using shared mechanisms the getLogProperties() may trigger 
			// nested openForLog() calls.
			if (!isOpen()) {
				this.formatter = createFormatter(log.getName(), logProperties);
				open(log);
			}
		}

		this.loggers.addElement(log);
		notifyLogOpened(log, loggingLevel);
	}


	/**
	 * Loggers call this method to deregister themselves with the mechanism.
	 * It will automatically close the mechanism if needed and log the fact
	 * that a logger stopped using this mechanism.
	 * 
	 * @param log  A logger
	 */
	synchronized 
	void closeForLog(
		Log log)
	{
		// If the log is in the set of logs, remove it and put out
		// a notification message.  
		if (this.loggers.contains(log)) {
			this.loggers.removeElement(log);
			notifyLogClosed(log);

			// If this was the last logger that asked for closing, close the
			// mechanism (we don't the mechanism anymore).
			if (this.loggers.size() <= 0) close();
		}
	}


	/**
	 * This method opens the particular logging mechanism so that messages
	 * can be output.
	 */
	protected void open(Log log)
	{
		this.open = true;
		this.dateOpened = new Date();
	}


	/**
	 * This method closes the particular logging mechanism so that messages
	 * no longer get logged to the mechanism.
	 */
	protected void close()
	{
		this.open = false;
	}


	/**
	 * Log a message that a logger was openend on this mechanism
	 * 
	 * @param log    A logger
	 * @param state  An initial log level
	 */
	protected void notifyLogOpened(
		Log log, 
		int loggingLevel)
	{
		SimpleDateFormat dateFormatter = new SimpleDateFormat(
								AbstractLoggingMechanism.OPEN_DATE_FORMAT);

		Date   date = getDateOpend();
		String sOpenDate = (date != null) ? dateFormatter.format(date) : "???";
		

		logNotification(
			log, 
            "Log opened for " + log.getName() 
            + ". Implementation Version=" 
	   	    + org.openmdx.kernel.Version.getImplementationVersion()
			+ ", opened=" + sOpenDate, 
	   	    null);

		if (acceptsStandardLogs()) {
			notifyLoggingLevelChange(log, loggingLevel);
		}

		if (acceptsPerformanceLogs()) {
			notifyLoggingPerformanceChange(log, log.isLoggingPerformance());
		}

		if (acceptsStatisticLogs()) {
			notifyLoggingStatisticsChange(log, log.isLoggingStatistics());
		}
	}


	/**
	 * Log a message that a logger was closed on this mechanism
	 * 
	 * @param log    A logger
	 */
	protected void notifyLogClosed(Log log)
	{
		logNotification(log, "Log closed", null);
	}


	/**
	 * Log a log level change
	 * 
	 * @param log    A logger
	 * @param state  A new log level
	 */
	protected void notifyLoggingLevelChange(
		Log log, 
		int loggingLevel)
	{
		logNotification(
			log, 
			"Log level change to level " +
	   		LogUtil.logLevelToStringShort(loggingLevel),
	   		null);
	}


	/**
	 * Log a performance change
	 * 
	 * @param log    A logger
	 * @param state  A new performance logging state
	 */
	protected void notifyLoggingPerformanceChange(
		Log      log, 
		boolean state)
	{
		logNotification(
			log, 
			"Log performance " + (state ? "enabled" : "disabled"),
			null);
	}

	/**
	 * Put out a nice message that a log changed its statistics state
	 * 
	 * @param log  A logger
	 * @param state A new state to be logged as notification
	 */
	protected void notifyLoggingStatisticsChange(
		Log      log, 
		boolean state)
	{
		logNotification(
			log, 
			"Log statistics " + (state ? "enabled" : "disabled"),
			null);
	}


	/**
	 * Logs a string to printStream, complete with a descriptive prefix so 
	 * viewers of the log can interpret who logged which strings and how 
	 * important they were.
	 * 
	 * @param log    A logger
	 * @param event  A log event
	 */
	abstract 
	protected void logEvent(
		Log log, 
		LogEvent  event);
	

    /**
     * Mechanisms can be shared or unique. Shared mechanisms require the log 
     * name to be in every log entry (so you can distinguish between the log 
     * entries). Unique mechanisms name themselves uniquely (for example, have
     * the log name in their file name) so they do not have to put the name of 
     * the log in their log entry. 
     * <p>The default is shared (true).
     * 
     * @return true if the mechanism is shared
     */
    protected boolean isSharedLog()
    {
        return true;
    }
    
    
    /**
     * Sets a new notification mode
     * 
     * @param newMode   A new notification mode for the mechanism
     */
    protected void setNotification(
    	boolean newMode)
    {
        this.notification = newMode;
    }


	/**
	 * Returns true if the mechanism is open
	 * 
	 * @return true if the mechanism is open
	 */
	protected boolean isOpen()
	{
		return this.open;
	}


	/** 
	 * The loggers that use this mechanism
	 * 
	 * @return a vector of loggers
	 */
	protected Vector<Log> getLoggers()
	{
		return this.loggers;
	}


	/** 
	 * The date this mechanism was openend.
	 * 
	 * @return a Date or null if the mechanism is not open.
	 */
	protected Date getDateOpend()
	{
		return this.dateOpened;
	}


	/**
	 * Create a new log event formatter.
	 * 
	 * @param logName the logger's name
	 * @param logProperties the log properties
	 */
	protected LogFormatter createFormatter(
		String         logName,
		LogProperties  logProperties)
	{
		// Read the standard log format from the log properties
		String format = logProperties.getProperty(logName, "LogFormat", null);
	
		return new LogFormatterStandard(format);
	}


	/**
	 * Returns the log event formatter.
	 * 
	 * @return a log event formatter
	 */
	protected LogFormatter getFormatter()
	{
		return this.formatter;
	}


	/**
	 * Sets a new log event formatter.
	 * 
	 * @param a log event formatter
	 */
	protected void setFormatter(LogFormatter  formatter)
	{
		if (formatter != null) {
			this.formatter = formatter;
		}
	}


    /**
     * Checks if the mechanisms accepts standard logs.
     * CRITICAL_ERROR ... TRACE level
     * The default is to accept standard logs (true).
     *
     * @return  true if log accepts standard records
     */
    public boolean acceptsStandardLogs() { return true; }


    /**
     * Checks if the mechanisms accepts statistics logs.
     * The default is to reject statistics (false).
     *
     * @return  true if log accepts statistic records
     */
    public boolean acceptsStatisticLogs() { return false; }


	/**
	 * Checks if the mechanisms accepts performance logs.
	 * The default is to accept perfromance logs (true).
	 *
	 * @return  true if log accepts performance records
	 */
	public boolean acceptsPerformanceLogs() { return true; }


	/**
	 * Checks if the mechanisms accepts notification logs.
	 * The default is the configured notification mode from the log properties.
	 *
	 * @return  true if log accepts notification records
	 */
	public boolean acceptsNotificationLogs()  {  return this.notification; }


	/**
	 * Return the name of the mechanism.
	 * 
	 * @return The mechanism name
	 */
	public String getName() 
	{ 
		return "AbstractLoggingMechanism"; 
	}


	/**
	 * Return a nice debug string.
	 * 
	 * @return A mechanism description
	 */
	public String toString() 
	{
	    return getName();
	}


	/**
	 * Returns a list of all active (in use) log entities that are available
	 * by the file logging mechanisms controlled by this logger
	 *
	 * @return a list of {@link LogEntity} objects
	 */
	public List<LogEntity> getActiveEntities()
	{
		// This mechanism is not manageable
		return new ArrayList<LogEntity>();
	}


	/**
	 * Returns a list of all readable log entities that are available
	 * by the file logging mechanisms controlled by this logger
	 *
	 * @return a list of log entity names
	 */
	public List<LogEntity> getReadableEntities()
	{
		// This mechanism is not manageable
		return new ArrayList<LogEntity>();
	}


	/**
	 * Returns a list of all removeable log entities that are available
	 * by the file logging mechanisms controlled by this logger
	 *
	 * @return a list of log entitiy names
	 */
	public List<LogEntity> getRemoveableEntities()
	{
		// This mechanism is not manageable
		return new ArrayList<LogEntity>();
	}


	/**
	 * Remove a log entity
	 *
	 * @param String an entity name
	 */
	public void removeEntity(
		LogEntity entity)
	{
		// This mechanism is not manageable
	}


	/**
	 * Returns an entity reader for given log entity
	 *
	 * @param String an entity name
	 */
	public LogEntityReader getReader(
		LogEntity entity)
	{
		// This mechanism is not manageable
		return null;
	}


	/**
	 * Creates a notification log event.
	 * 
	 * @param log  A logger
	 * @param summary A notification summary (E.g. "Log closed")
	 * @param detail A notification detail
	 * @return a notification log event
	 */
	final public LogEvent createNotificationEvent(
		Log      log,
		String   summary,
		String   detail)
	{
		return new LogEvent(
						log.getName(),
						new Date(),
						LogLevel.LOG_NOTIFICATION,
						log.getConfig().getConfigName(),
						log.getConfig().getApplicationId(),
						"Log-Framework",
						log.getHostName(),
						null,  // process ID not available
						Thread.currentThread().getName(),
						getName(),
						"logNotification",
						-1,
						summary,
						detail);
	}
	

	/**
	 * Logs a notification log event.
	 * 
	 * @param log  A logger
	 * @param summary A notification summary (E.g. "Log closed")
	 * @param detail A notification detail
	 */
	private void logNotification(
		Log      log,
		String   summary,
		String   detail)
	{
		if (acceptsNotificationLogs()) {
			logEvent(
				log, createNotificationEvent(log, summary, detail));
		}
	}


	public static final String OPEN_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";


	/** The log event formatter */
	private LogFormatter 	formatter = new LogFormatterStandard(null);

	/** The date the mechanism was first opened. */
	private Date dateOpened;

	/** Reflects the mechanism's notification state */
	private boolean notification = false;

	// Keep a list of logs that use the logging mechanism, so we know when
	// to open/close the log properly.
	private Vector<Log> loggers = new Vector<Log>(10);
	
	/** Reflects the open state of the mechanism */
	private boolean open = false;

}

