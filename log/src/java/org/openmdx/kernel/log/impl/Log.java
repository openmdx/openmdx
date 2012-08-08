/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Log.java,v 1.2 2008/05/05 17:47:13 hburger Exp $
 * Description: Log
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/05 17:47:13 $
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


import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.openmdx.compatibility.kernel.log.Config;
import org.openmdx.compatibility.kernel.log.LogEntity;
import org.openmdx.compatibility.kernel.log.LogEntityReader;
import org.openmdx.compatibility.kernel.log.LogEvent;
import org.openmdx.compatibility.kernel.log.LogLevel;
import org.openmdx.compatibility.kernel.log.ManageableMechanism;
import org.openmdx.kernel.text.MultiLineStringRepresentation;


/**
 * <code>Log</code> is the base class which defines
 * all of the logging constants and implements any
 * general logging responsibilities. It dispatches the actual
 * logging information over to a concrete subclass of
 * <code>AbstractLoggingMechanism</code> to do the work of writing
 * out the log information. <p>
 */
@SuppressWarnings("unchecked")
public class Log
	implements ManageableMechanism, MechanismManagerListener
{

    /**
     * Prevent instantiation of subclasses
     */
    protected Log(
    ){
        throw new UnsupportedOperationException(
            this.getClass().getName() + " must not be instantiated"
        );
    }
    
    /**
     * Public constructor to register a new log with a particular name.
     * The name of the log is very important. It is used as a key into
     * the log properties file to find settings for how the log is to
     * be opened.
     *
     * @param name        A log name. E.g.: "AppLog" (mandatory)
     * @param logClass    A logger class (mandatory)
     * @param logSource   The optional log source
     */
    private Log(
        String       name,
        Class        logClass, 
        Object       logSource
    ){
        this.name             = name;
        this.logClass         = logClass;
        this.logSource        = logSource;
        this.mechanismManager = new MechanismManager(this);
        this.bootstrapLogger  = new BootstrapLogger(this);

        this.mechanismManager.register(this);

        try {
            this.hostname = InetAddress.getLocalHost().getHostName();
        }
        catch(java.net.UnknownHostException ex) {
            this.hostname = DEFAULT_HOSTNAME;
        }
    }

    /**
     * Public constructor to register a new log with a particular name.
     * The name of the log is very important. It is used as a key into
     * the log properties file to find settings for how the log is to
     * be opened.
     *
     * @param name        A log name. E.g.: "AppLog" (mandatory)
     * @param logClass    A logger class (mandatory)
     */
    protected static Log newLog(
        String       name,
        Class        logClass,
        Object       logSource
    ){
        return new Log(name,logClass, logSource);
    }

    /**
     * Public constructor to register a new log with a particular name.
     * The name of the log is very important. It is used as a key into
     * the log properties file to find settings for how the log is to
     * be opened.
     *
     * @param name        A log name. E.g.: "AppLog" (mandatory)
     * @param logClass    A logger class (mandatory)
     */
    protected static Log newLog(
        String       name,
        Class        logClass)
    {
        return new Log(name,logClass, null);
    }
    
    /**
     * Load the configuration
     *
     * @param cfgName        A config name (may be <null>)
     * @param logProperties  A list of log properties (may be <null>)
     */
    synchronized
    public void loadConfig(
        String        cfgName,
        Properties    logProperties)
    {
    	if (this.configured) return;
    
    	// load the properties
    	this.logProperties = new LogProperties(
    								this,
    								cfgName,
    								this.logClass.getClassLoader(),
    								logProperties);
    
    
    	// create the logger's configuration object
    	this.config = new ConfigDelegate(
    						this, 
    						cfgName, 
    						this.logProperties);
    
        // get logging levels from the log properties
        this.currentLoggingLevel       = this.logProperties.getLoggingLevel(name);
        this.currentLoggingPerformance = this.logProperties.isLoggingPerformance(name);
        this.currentLoggingStatistics  = this.logProperties.isLoggingStatistics(name);
    
        // The logger is configured now
        this.configured = true;
    }
    
    
    /**
     * Load the mechanisms
     */
    synchronized
    public void loadMechanisms()
    {
    	if (!this.configured) return;
    	if (this.mechanismsLoaded) return;
    
    	// Enable/Disable technical framework logging/debugging
        LogLog.enable(
        	LogProperties.isDebuggingEnabled());
    
        // You'll only see this message with enabled LogLog :-)
        LogLog.trace(
    		this.getClass(),
    		"Log",
            "LogLog " + (LogLog.isEnabled() ? "enabled" : "disabled")
            + " from logger " + this.name
            , "");
    
    	// Get a list of all of the mechanism names and open them
    	this.mechanismManager.addMechanisms(
    		this.logProperties.getLoggingMechanismNames(this.name));
    
    
    	// Add the log and VM properties to the bootstrap log
    	BootstrapPropertyManager  propMgr = new BootstrapPropertyManager(this);
    	propMgr.addLogAndVmProperties();
    	
    
    	// Now we'are able to log the collected bootstrap log events.
    	// Close first the prevent any new events.
    	this.getBootstrapLogger().close();
    	this.getBootstrapLogger().flush();
    
    	this.mechanismsLoaded = true;
    	this.booted = true;
    }
    
    
    /**
     * Logs a log event to all mechanisms. It's up to the mechanisam to reject
     * specific log event types (standard logs, statistics logs or performance
     * logs).
     *
     * @param event         A log event
     * @param forceLogging  log an event even if the currently active log
     *                       level does not permit logging
     */
    public void logEvent(
    	LogEvent  event,
    	boolean   forceLogging)
    {
    	final int     loggingLevel = event.getLoggingLevel();
    	final boolean statistics   = (loggingLevel == LogLevel.LOG_STATISTICS);
    	final boolean performance  = (loggingLevel == LogLevel.LOG_PERFORMANCE);
    	final boolean logIt;
    
    
    	// Log the message if higher or equal to current logging level
    	logIt =   forceLogging
    		   || ((loggingLevel > 0) && (loggingLevel <= this.currentLoggingLevel))
    		   || (this.currentLoggingPerformance && performance)
    		   || (this.currentLoggingStatistics && statistics);
    
    	if (logIt) {
    		AbstractLoggingMechanism currLogMech = null;
    		AbstractLoggingMechanism[] mechArr = this.mechanismManager.getMechanisms();
    
    		// Iterate through the logging mechanisms
    		for(int ii=0; ii<mechArr.length; ii++) {
    			currLogMech = mechArr[ii];
    
    			if (statistics) {
    				if (currLogMech.acceptsStatisticLogs()) {
    					currLogMech.logEvent(this, event);
    				}
    				else continue;
    			}
    			else if (performance) {
    				if (currLogMech.acceptsPerformanceLogs()) {
    					currLogMech.logEvent(this, event);
    				}
    				else continue;
    			}
    			else if (currLogMech.acceptsStandardLogs()) {
    				currLogMech.logEvent(this, event);
    			}
    		}
    	}
    }
    
    /**
     * Adds a message string to the log if the log's level is
     * currently set at or above the loggingLevel passed in.
     * Prepends the current thread name before the log entry.
     *
     * @param logSource         a log source object
     * @param logStringSummary  The log summary info (single line)
     * @param logObj            A log object (detail, to be stringified, a null
     *                          object my be passed)
     * @param loggingLevel      The level at which to log the string.
     * @param callStackOff      a call stack correction offset. The offset must
     *                          be a positive number 0, 1, 2, 3
     */
    public void logAtLevel(
        String logStringSummary,
        Object logObj,
        int    loggingLevel,
        int    callStackOff
    ) {
        if(loggingLevel <= this.currentLoggingLevel) logString(
            this.logSource,
            logStringSummary,
            logObj,
            loggingLevel,
            callStackOff
        );
    }
    
    /**
     * Adds a message string to the log if the log's level is
     * currently set at or above the loggingLevel passed in.
     * Prepends the current thread name before the log entry.
     *
     * @param logSource         a log source object
     * @param logStringSummary  The log summary info (single line)
     * @param logObj            A log object (detail, to be stringified, a null
     *                          object my be passed)
     * @param loggingLevel      The level at which to log the string.
     * @param callStackOff      a call stack correction offset. The offset must
     *                          be a positive number 0, 1, 2, 3
     */
    public void logString(
    	Object logSource,
    	String logStringSummary,
    	Object logObj,
    	int    loggingLevel,
    	int    callStackOff)
    {
    	LogEvent  event  = null;
        String    detail;
    
        if (logObj == null) {
        	detail = null;
        } else if (
			logObj instanceof Throwable &&
			!(logObj instanceof MultiLineStringRepresentation)
		){
			// BUG: Microsoft J#
			// There is a bug in Microsoft J# that handles the stack 
			// trace of exceptions not correctly. Actually the 
			// excpetion's stack trace should be filled in the 
			// exception's constructor  but Microsoft J# does this on  
			// demand when calling printStackTrace() or when explicitly 
			// calling fillInStackTrace(). The stack trace logged below
			// is therefore from a wrong calling context.
			StringWriter sw = new StringWriter();
			((Throwable)logObj).printStackTrace(new PrintWriter(sw));

			if (Log.isMicrosoftVM) {
				detail = "The exception logged below might not be"
				         + " correct on this Microsoft Java VM.\n" 
				         + sw.toString();
			}
			else {
				detail = sw.toString();
			}
        } else{
            detail = logObj.toString();
        }
    
    	String classname, methodname;
    	int linenr;
    
       	if (loggingLevel != LogLevel.LOG_STATISTICS) {
    		LogLocation stack = stackTraceElementFactory.getStackTraceElement(
    			// Offset is 2 because two logging framework methods have been
    			// called so far.
    		    2 + callStackOff
    		);
    		classname  = stack.getClassname();
    		methodname = stack.getMethod();
    		linenr     = stack.getLineNumber();
    	}else{
    		classname  = "";
    		methodname = "";
    		linenr     = -1;
    	}
    
    	event = new LogEvent(
    					getName(),
    					new Date(),
    					loggingLevel,
    					this.config.getConfigName(),
    					this.config.getApplicationId(),
    					logSource.toString(),
    					getHostName(),
    					null,  // process ID not available
    					Thread.currentThread().getName(),
    					classname,
    					methodname,
    					linenr,
    					logStringSummary,
    					detail);
    
        logEvent(event, false);
    
        event.reset();
    }
    
    
    /**
     * Returns true if the logger has booted..
     *
     * <p>The logger has booted if it is properly configured and the
     * mechanims have been loaded.
     *
     * @return true if booted
     */
    public boolean isBooted()
    {
    	return this.booted;
    }
    
    
    
    /**
     * Gets the logging level of the current log.
     */
    public int getLoggingLevel()
    {
    	return this.currentLoggingLevel;
    }
    
    
    /**
     * Gets the logging performance state of the current log.
     */
    public boolean isLoggingPerformance()
    {
    	return this.currentLoggingPerformance;
    }
    
    
    /**
     * Changes the logging performance state of the current log.
     */
    public void setLoggingPerformance(
    	boolean newState)
    {
    	if (this.currentLoggingLevel != LogLevel.LOG_LEVEL_DEACTIVATE) {
    		this.currentLoggingPerformance = newState;
    	}
    }
    
    
    /**
     * Gets the logging statistics state of the current log.
     */
    public boolean isLoggingStatistics()
    {
    	return this.currentLoggingStatistics;
    }
    
    
    /**
     * Changes the logging statistics state of the current log.
     */
    public void setLoggingStatistics(
    	boolean newState)
    {
    	if (this.currentLoggingLevel != LogLevel.LOG_LEVEL_DEACTIVATE) {
    		this.currentLoggingStatistics = newState;
    	}
    }
    
    
    /**
     * Changes the logging level of the current log.
     *
     * Use this method to activate, deactivate, or change the logging
     * detail level of an active log.
     *
     * @param level a new log level
     */
    public void setLoggingLevel(
    	int level)
    {
        int newLevel = 
            level < LogLevel.LOG_LEVEL_MIN ? LogLevel.LOG_LEVEL_MIN :
            level > LogLevel.LOG_LEVEL_MAX ? LogLevel.LOG_LEVEL_MAX :
            level; 
    	if (newLevel == LogLevel.LOG_LEVEL_DEACTIVATE) {
    		this.currentLoggingPerformance = false;
    		this.currentLoggingStatistics  = false;
    	}
    
    
    	// Notify the log change
        AbstractLoggingMechanism currLogMech = null;
    	AbstractLoggingMechanism[] mechArr = this.mechanismManager.getMechanisms();
    	for(int ii=0; ii<mechArr.length; ii++) {
    		currLogMech = mechArr[ii];
    
    		if (currLogMech.acceptsStandardLogs()) {
    			currLogMech.notifyLoggingLevelChange(this, newLevel);
    		}
        }
    
    	// Set the new logging level
    	this.currentLoggingLevel = newLevel;
    }
    
    
    /**
     * Returns the logger's mechanism manager.
     *
     * @return a mechanism manager
     */
    public MechanismManager getMechanismManager()
    {
    	return this.mechanismManager;
    }
    
    
    /**
     * Returns the logger's configuration object
     *
     * @return  the configuration object
     */
    public ConfigDelegate getConfig()
    {
    	return this.config;
    }
    
    
    /**
     * Called when a new mechanism has been added.
     *
     * <p>From <code>MechanismManagerListener</code> interface
     *
     * @param mech
     */
    public void mechanismAddedEvent(
    	AbstractLoggingMechanism mech)
    {
    	mech.setNotification(this.logProperties.isLoggingNotification(getName()));
    
    	mech.openForLog(this, this.currentLoggingLevel);
    }
    
    
    /**
     * Called when a new mechanism has been removed.
     *
     * <p>From <code>MechanismManagerListener</code> interface
     *
     * @param mech
     */
    public void mechanismRemovedEvent(
    	AbstractLoggingMechanism mech)
    {
    	mech.closeForLog(this);
    }
    
    
    /**
     * Returns a list of all active (in use) log entities that are available
     * by the file logging mechanisms controlled by this logger.
     *
     * <p>From <code>ManageableMechanism</code> interface
     *
     * @return a list of {@link LogEntity} objects
     */
    public List<LogEntity> getActiveEntities()
    {
    	if (!this.mechanismsLoaded) loadMechanisms();  // lazy loading
    
    	ArrayList                   entities = new ArrayList();
    	AbstractLoggingMechanism[]  mechArr = this.mechanismManager.getMechanisms();
    
    	for(int ii=0; ii<mechArr.length; ii++) {
    		entities.addAll(mechArr[ii].getActiveEntities());
    	}
    
    	return entities;
    }
    
    
    /**
     * Returns a list of all readable log entities that are available
     * by the file logging mechanisms controlled by this logger.
     *
     * <p>From <code>ManageableMechanism</code> interface
     *
     * @return a list of log entity names
     */
    public List<LogEntity> getReadableEntities()
    {
    	if (!this.mechanismsLoaded) loadMechanisms();  // lazy loading
    
    
    	ArrayList                   entities = new ArrayList();
    	AbstractLoggingMechanism[]  mechArr = this.mechanismManager.getMechanisms();
    
    	for(int ii=0; ii<mechArr.length; ii++) {
    		entities.addAll(mechArr[ii].getReadableEntities());
    	}
    
    	return entities;
    }
    
    
    /**
     * Returns a list of all removeable log entities that are available
     * by the file logging mechanisms controlled by this logger.
     *
     * <p>From <code>ManageableMechanism</code> interface
     *
     * @return a list of log entitiy names
     */
    public List<LogEntity> getRemoveableEntities()
    {
    	if (!this.mechanismsLoaded) loadMechanisms();  // lazy loading
    
    	ArrayList                   entities = new ArrayList();
    	AbstractLoggingMechanism[]  mechArr = this.mechanismManager.getMechanisms();
    
    	for(int ii=0; ii<mechArr.length; ii++) {
    		entities.addAll(mechArr[ii].getRemoveableEntities());
    	}
    
    	return entities;
    }
    
    
    /**
     * Remove a log entity.
     *
     * <p>From <code>ManageableMechanism</code> interface
     *
     * @param String an entity name
     */
    public void removeEntity(
    	LogEntity entity)
    {
    	if (!this.mechanismsLoaded) loadMechanisms();  // lazy loading
    
    	// dispatch to associated mechanism
    	AbstractLoggingMechanism[]  mechArr = this.mechanismManager.getMechanisms();
    
    	for(int ii=0; ii<mechArr.length; ii++) {
    		if (mechArr[ii].getName().equals(entity.getMechanism())) {
    			mechArr[ii].removeEntity(entity);
    			return;
    		}
    	}
    }
    
    
    /**
     * Returns an entity reader for given log entity.
     *
     * <p>From <code>ManageableMechanism</code> interface
     *
     * @param String an entity name
     * @return A log entity reader for the entity
     */
    public LogEntityReader getReader(
    	LogEntity entity)
    {
    	if (!this.mechanismsLoaded) loadMechanisms();  // lazy loading
    
    	AbstractLoggingMechanism[]  mechArr = this.mechanismManager.getMechanisms();
    
    	for(int ii=0; ii<mechArr.length; ii++) {
    		if (mechArr[ii].getName().equals(entity.getMechanism())) {
    			return mechArr[ii].getReader(entity);
    		}
    	}
    
    	return null;
    }
    
    
    /**
     * Returns a string representation of log properties read
     *
     * @return  a String
     */
    public String toString()
    {
    	return getName();
    }
    
    
    
    /**
     * Enables/Disables tracing for the specified thread.
     *
     * If application controlled tracing has been enabled through configuration
     * setApplicationControlledTrace(true, thread) allows applications to force
     * logs at level TRACE_LEVEL regardless the actual configuration state of
     * the TRACE_LEVEL. A setApplicationControlledTrace(false, thread) lets
     * the log level TRACE_LEVEL control the trace output of the specified
     * thread.
     *
     * @param enable   enables/disables trace
     * @param thread   the thread that is affected
     */
    public void setApplicationControlledTrace(
    	boolean enable,
    	Thread  thread)
    {
    	new IllegalStateException(
    		"The 'Application-Controlled-Trace' feature is not yet available.");
    }
    
    /**
     * Get the log properties.
     *
     * @param properties - the properties
     */
    public LogProperties getLogProperties()
    {
    	return this.logProperties;
    }
    
    
    /**
     * Returns the bootrap logger
     *
     * @param properties - the properties
     */
    public BootstrapLogger getBootstrapLogger()
    {
    	return this.bootstrapLogger;
    }
    
    
    /**
     * Return the name of the log, usually passed in by the constructor.
     *
     * @return The logger's name
     */
    public String getName()
    {
        return this.name;
    }
    
    
    /**
     * Return the date the log was opened, only valid if the log is
     * currently open.
     *
     * @return The logger's open date
     */
    public Date getDateOpened()
    {
    	return this.dateOpened;
    }
    
    
    /**
     * Return the host
     *
     * @return The hostname
     */
    public String getHostName()
    {
    	return this.hostname;
    }
    
    
    /**
     * Must close the log for the application if the application did not or
     * could not.
     *
     * @exception Throwable Default finalizer exception
     */
    protected void finalize()
    	throws Throwable
    {
    	this.mechanismManager.removeAllMechanisms();
    
    	super.finalize();
    }

    /**
     * Register a Logger by its configuration object. All AppLog classes
     * and the SysLog class must register their configuration object.
     *
     * This list of configurations may be used to track the available
     * loggers and to implement dynamic logging configurators.
     *
     * @param config
     *         a configuration object that belongs to a static logger class
     */
    protected static void register(Config config)
    {
        configMgr.register(config);
    }


    /**
     * Unregister a logger.
     *
     * @param config
     *         a configuration object that belongs to a static logger class
     */
    public  static void unregister(Config config)
    {
        configMgr.unregister(config);
    }
    
    /**
     * <code>LocationAwareLogger</code> requires the log class name
     * 
     * @return the log class name
     */
    public final String getLogClassName(){
        return this.logClass.getName();
    }    
    
    /** Reflects if the currently used VM is a Microsoft VM (J#) */
    public static final boolean isMicrosoftVM = 
    	"Microsoft Corp.".equals(System.getProperty("java.vendor"));
        
    /** The default host name (unknown) */
    private static final String DEFAULT_HOSTNAME = "N/A";
    
    
    /** Keeps the name of the Log instance (AppLog, SysLog, ..) */
    private String name = "Log";
    
    /** Keep the logging level the log is currently operating at */
    private int currentLoggingLevel = LogLevel.LOG_LEVEL_DEACTIVATE;
    
    /** Keep the performance logging status the log is currently operating at */
    private boolean currentLoggingPerformance = false;
    
    /** Keep the statistics logging status the log is currently operating at */
    private boolean currentLoggingStatistics = false;
    
    /** Keeps the date the log opened */
    private final Date dateOpened = new Date();

    /** The hostname */
    private String hostname = DEFAULT_HOSTNAME;
    
    /** The bootstrap logger */
    private final BootstrapLogger  bootstrapLogger;
    
    /** The Log configuration object */
    private ConfigDelegate config = null;
    
    /** The Log properties object */
    private LogProperties logProperties = null;
    
    /** Keeps the class of the Log instance (AppLog, SysLog, ..) */
    private final Class logClass;

    /** Keeps the class of the Log instance (AppLog, SysLog, ..) */
    private final Object logSource;
    
    /** A mechanims manager */
    private final MechanismManager   mechanismManager;
    
//  /** Application control tracing enable state */
//  private Boolean applicationControlledTracing = null;
    
    /** Reflects whether the mechanisms have been loaded already */
    private boolean mechanismsLoaded = false;
    
    /** Reflects whether logger is already configured */
    private boolean configured = false;
    
    /**
     * Reflects whether logger has booted.
     *
     * <p>The logger has booted if it is properly configured and the
     * mechanims have been loaded.
     */
    private boolean booted = false;

    /**
     * The stack trace analysis is platform dependend.
     */
    private static final StackTraceElementFactory_1_0 stackTraceElementFactory =
        new org.openmdx.kernel.log.impl.StackTraceElementFactory_1();
    
    /** The config manager that holds all logger's config objects */
    protected static final ConfigManager configMgr = new ConfigManager();

}

