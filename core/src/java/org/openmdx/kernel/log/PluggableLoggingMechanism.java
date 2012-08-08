/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PluggableLoggingMechanism.java,v 1.6 2008/02/05 13:41:39 hburger Exp $
 * Description: Pluggable Logging Mechanism
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/05 13:41:39 $
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
package org.openmdx.kernel.log;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;

import org.openmdx.kernel.log.impl.Log;
import org.openmdx.kernel.log.impl.LogLog;
import org.openmdx.kernel.log.impl.MechanismManager;
import org.openmdx.kernel.log.impl.PluggableLoggingMechanismProxy;


/**
 * This <code>PluggableLoggingMechanism</code> mechanism implements a base class
 * for pluggable mechanisms.
 *
 * <p>Pluggable mechanism implementations:
 * <ul>
 * <li>are completely decoupled from the logging framework.
 * <li>are dynamically plugged into the logging framework.
 * <li>are instantiated and controlled by application itself.
 * 
 * <p>The logging framework is designed not to rely on dynamic classloading to
 * prevent any problems with the wide range of application architecture
 * frameworks (Corba, J2EE, ...). Therefore the applications must
 * itself instantiate their pluggable mechanisms and <code>attach</code> /
 * <code>detach</code> them with the log framework.
 * 
 * 
 * <p><b>A sample Pluggable Logging Mechanism</b>
 *
 * <pre>
 *
 *		public class ObserverMechanism extends PluggableLoggingMechanism
 *		{
 *			public ObserverMechanism() {}
 *
 *			public String getMechanismName() { return "ObserverMechanism"; }
 *
 *			public void log(LogEvent  event)
 *			{
 *				if (event.getLoggingLevel() == LogLevel.LOG_LEVEL_CRITICAL_ERROR) {
 *					System.out.println("*************************************");
 *					System.out.println("* OBSERVER: CRITICAL ERROR");
 *					System.out.println("* Class   : " + event.getClassName());
 *					System.out.println("* Method  : " + event.getMethodName());
 *					System.out.println("* Summary : " + event.getLogStringSummary());
 *					System.out.println("* Detail  : " + event.getLogStringDetail());
 *					System.out.println("*************************************");
 *		   		} //if
 *		   	}
 *		};
 *
 *		public class Sample
 *		{
 *			public static void main (String[] args)
 *			{
 *				// initialize the logging subsystem
 *				AppLog.setConfigName("Sample");
 *				AppLog.setLogSource("MySample");
 *
 *              // Read the observer's log property
 *              String observerMyProperty = SysLog.getLogConfig().getLogProperty(
 *                                           "ObserverMechanism.myProperty", "")
 *  
 *				// instantiate your pluggable logging mechanism
 *				ObserverMechanism observer = new ObserverMechanism();
 *
 *				// Make it available to the log subsystem
 *				observer.attach(AppLog.getLogConfig());
 *				observer.activate();
 *
 *				...  YOUR IMPLEMENTATION ...
 *
 *				// Release it from the log subsystem (optional)
 *				observer.deactivate();
 *				observer.detach();
 *			}
 *		}
 * </pre>
 *
 * <p>The log property file <b>Sample.log.properties</b> could look as follows:
 * <pre>
 *		LogFileExtension        = log
 *		LogFilePath             =
 *		LogLevel                = 3
 *		java.LoggingMechanism   = StandardErrorLoggingMechanism
 *		java.LoggingMechanism2  = ObserverMechanism
 *		LogPerformance          = true
 *		LogNotification         = false
 *      SysLog.ObserverMechanism.myProperty = demo
 * </pre>
 * 
 */



public abstract class  PluggableLoggingMechanism
{
	protected PluggableLoggingMechanism() {
	    super();
	}


	/**
	 * Logs an event.
	 *
	 * <p>IMPORTANT: The pluggable logging mechanism only gets log events that
	 * conform the actual logging level.
     *
     * @param event A log event
     */
	public void log(LogEvent  event)
	{
		// Default implementation does nothing
	}



	/**
	 * Return the name of the mechanism.
	 *
	 * @return   a string
	 */
	public String getMechanismName()
	{
		return "PluggableLoggingMechanism";
	}


	/**
	 * This method is called, to inform the pluggable logging mechanism that
	 * the log level has changed.
	 *
	 * IMPORTANT:
	 * Actually the pluggable logging mechanism only gets log events that
	 * conform the actual logging level.
	 *
	 * @param loggingLevel A logging level
	 */
	public void notifyLoggingLevelChange(int newLoggingLevel)
	{
		// Default implementation does nothing
	}


	/**
	 * This method is called, to inform the pluggable logging mechanism that
	 * the log performance has changed.
	 *
	 *
	 * @param newState A new performance state
	 */
	public void notifyLoggingPerformanceChange(boolean newState)
	{
		// Default implementation does nothing
	}

	/**
	 * This method is called, to inform the pluggable logging mechanism that
	 * the log statistics has changed.
	 *
	 *
	 * @param newState A new performance state
	 */
	public void notifyLoggingStatisticsChange(boolean newState)
	{
		// Default implementation does nothing
	}

    /**
     * Checks if the mechanisms accepts standard logs.
     * CRITICAL_ERROR ... TRACE level
     * The default is to accept standard logs (true).
     *
     * @return  true if log accepts standard logs
     */
    public boolean acceptsStandard() { return true; }

    /**
     * Checks if the mechanisms accepts statistics logs.
     * The default is to reject statistics (false).
     *
     * @return  true if log accepts statistic logs
     */
    public boolean acceptsStatistics() { return false; }

    /**
     * Checks if the mechanisms accepts performance logs.
     * The default is to accept perfromance logs (true).
     *
     * @return  true if log accepts performance logs
     */
    public boolean acceptsPerformance() { return true; }


	/**
	 * Attach the logging mechanism to the logging framework. This method can
	 * be called multiple times to attach the mechanism to multiple loggers.
	 * Do not forget to call {@link #activate()} to activate the mechanism.
	 *
	 * @param loggerConfigs  An array of loggers (AppLog, SysLog, ...) to 
	 * attach the mechanism to.
	 * @return  true if successful
	 */
	public final boolean attach(
		Config[]  loggerConfigs)
	{
		if (loggerConfigs == null) return true;
		
		for(int ii=0; ii<loggerConfigs.length; ii++) {
			attach(loggerConfigs[ii]);
		}
		
		return true; // attaching is always succesful
	}

	/**
	 * Attach the logging mechanism to the logging framework. This method can
	 * be called multiple times to attach the mechanism to multiple loggers.
	 * Do not forget to call {@link #activate()} to activate the mechanism.
	 *
	 * @param loggerConfig A logger (AppLog, SysLog, ...) to 
	 * attach the mechanism to.
	 * @return  true if successful
	 */
	public final boolean attach(
		Config  loggerConfig)
	{
		if (loggerConfig == null) return true;
		
		if (!this.configs.contains(loggerConfig)) {
			this.configs.add(loggerConfig);
		}
		
		this.attached = true;

		return true; // attaching is always succesful
	}


	/**
	 * Detach the logging mechanism from the logging framework
	 *
	 * @return   true if successfull
	 */
	synchronized
	public final boolean detach()
	{
		checkMechanismName();		

		if ( this.attached ) {
			if (this.active) {
				deactivate();  // deactivate first
			}
			this.attached = false;
		}else{
    	    LogLog.warning(
    	    	this.getClass(),
    	        "detach",
    	        "The puggable mechanism '" + getMechanismName() 
    	        + "' is not attached.",
    	        null);
	    }

		return true;
	}


	/**
	 * Activates the logging mechanism. The framework can now send log
	 * messages to the mechanism. The mechanism must have been attached
	 * to the logging framework prior to activate it.
	 *
	 * @return   true if successfull
	 */
	synchronized
	public final boolean activate()
	{
		checkMechanismName();		

		if (!this.attached) {
			LogLog.warning(
				this.getClass(),
				"activate",
				"The pluggable mechanism '" + getMechanismName() 
				+ "' is not attached. Please attach it before activating it.",
				null);     

			return false;
		}
		

		MechanismManager  mechMgr;
		Log               logger;
		
		Iterator<Config> iter = this.configs.iterator();
		while(iter.hasNext()) {
			Config config =iter.next();
			
			try {
				logger  = config.getLogger();
				mechMgr = config.getLogger().getMechanismManager();
				
				// Enforce mechanism loading. At this point it's guaranteed
				// that the logger is configured otherwise we could not have
				// got its configuration object.
				if (!logger.isBooted()) {
					logger.loadMechanisms();
				}
				
				// If the mechanism is configured and not yet active add it
				if (mechMgr.isMechanismConfigured(this.getMechanismName())
				    && !mechMgr.isMechanismActive(this.getMechanismName()))
				{
					mechMgr.addMechanism(
						new PluggableLoggingMechanismProxy(
							logger.getName(), this));
				}
			}
			catch(MissingResourceException  me) {
				continue; // This logger must have been undeployed
			}
		}
		
		this.active = true;

		return true;
	}


	/**
	 * Deactivates the logging mechanism. The framework can't now send anymore
	 * log messages to the mechanism.
	 *
	 * @return   true if successfull
	 */
	synchronized
	public final boolean deactivate()
	{
		checkMechanismName();		

		if (this.active) {
			Iterator<Config> iter = this.configs.iterator();
			while(iter.hasNext()) try {
			    iter.next().getLogger().getMechanismManager().removeMechanism(
					this.getMechanismName());
			} catch(MissingResourceException  me) {
				continue; // This logger must have been undeployed
    		}
			this.active = false;
		}else{
    	    LogLog.trace(
    	    	this.getClass(),
    	        "deactivate",
    	        "The pluggable mechanism '" + getMechanismName() 
    	        + "' is not active",
    	        null);
	    }

		return true;
	}


	/**
	 * Checks if the mechanism is attached to the logging framework
	 *
	 * @return   true if attached
	 */
	public final boolean isAttached()
	{
		return this.attached;
	}
	

	/**
	 * Checks if the mechanism is open
	 *
	 * @return   true if attached
	 */
	public final boolean isActive()
	{
		return this.active;
	}


	/**
	 * Return the name of the mechanism.
	 * 
	 * @return the mechanism name
	 */
	String getName() 
	{ 
		return "PluggableLoggingMechanism"; 
	}


	/** 
	 * Check the mechanism name for validity. A mechanism name must not be 
	 * null or empty.
	 *
	 * throws IllegalStateException on illegal mechanism names.
	 */
	private void checkMechanismName() 
		throws IllegalStateException
	{
		if ((getMechanismName() == null) || (getMechanismName().length() == 0)) {
			throw new IllegalStateException(
				"The mechanism name of the mechanism <" 
				+ this.getClass().getName() + "> is either null or empty");
		}
	}
	
	

	/** Reflects the mechanisms activation state */
	private boolean active = false;

	/** Reflects the mechanisms attached state */
	private boolean  attached = false;


	/** The logger's config to which the mechanism has been attached */
    private List<Config> configs = new ArrayList<Config>();
}



