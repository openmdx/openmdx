/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ConfigDelegate.java,v 1.1 2008/03/21 18:21:59 hburger Exp $
 * Description: Log Configuration
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:21:59 $
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


import java.util.List;

import org.openmdx.compatibility.kernel.log.LogEntity;
import org.openmdx.compatibility.kernel.log.LogEntityReader;



/**
 * The configuration for a specific logger.
 */
public final class ConfigDelegate {

	/**
	 * Creates a new confiuration object.
	 *
	 * @param logger          The associated logger
	 * @param cfgName         The configuration name
	 * @param logProperties   The log properties
	 */
	public ConfigDelegate(
        Log            logger,
        String         cfgName,
        LogProperties  logProperties)
    {
        this.logger        = logger;
        this.loggerName    = logger.getName();
        this.cfgName       = cfgName;
	this.appId         = logProperties.getApplicationId(logger.getName());
//	this.logProperties = logProperties;
    }


	/**
	 * Return the instantion timestamp of the Config object.
	 * P.S. the logger has the same instantiation time.
	 *
	 * @return a Date
	 */
	public java.util.Date getInstantiationTime()
	{
        return this.instantiationTime;
	}



	/**
	 * Return the application name string that the logs pertain to.
	 * 
	 * @return a string
	 */
	public String getConfigName()
	{
	    return this.cfgName;
	}



	/**
	 * Return the application name view string that the logs pertain to.
	 * 
	 * @return a string
	 */
	public String getApplicationId()
	{
	    return this.appId;
	}



	/**
	 * Return the log name ("AppLog", "SysLog").
	 *
	 * @return a string
	 */
	public String getLogName()
	{
	    return this.loggerName;
	}


	/**
	 * Dump the log properties read from the log property file to.
	 * a string
	 *
	 * @return  A string
	 */
	public String dumpLogProperties()
	{
		return this.getLogger().getLogProperties().toString();
	}


    /**
     * Set logging level.
     *
     * <p>
     * This method should not be used by applications. The logging level is set
     * in the log property file.
     *
     * @param level - a new logging level
     */
	public void setLogLevel(int logLevel)
	{
		this.logger.setLoggingLevel(logLevel);
	}


	/**
     * Get logging level.
     *
     * <p>
     * This method should not be used by applications.
     *
     * @returns the current logging level
     */
	public int getLogLevel()
	{
		return this.logger.getLoggingLevel();
	}


    /**
     * Enable/Disable performance logging.
     *
     * <p>
     * This method should not be used by applications. The performance log state
     * is set in the log property file.
     *
     * @param enable   enable if true
     */
	public void enablePerformanceLog(boolean  enable)
	{
		this.logger.setLoggingPerformance(enable);
	}


    /**
     * Checks if performance logging is active.
     *
     * @return true if performance logging is active
     */
	public boolean isPerformanceLog()
	{
		return this.logger.isLoggingPerformance();
	}


    /**
     * Enable/Disable statistics logging.
     *
     * <p>
     * This method should not be used by applications. The statistics log state
     * is set in the log property file.
     *
     * @param enable   enable if true
     */
	public void enableStatisticsLog(boolean  enable)
	{
		this.logger.setLoggingStatistics(enable);
	}


    /**
     * Checks if statistics logging is active.
     *
     * @return true if statistics logging is active
     */
	public boolean isStatisticsLog()
	{
		return this.logger.isLoggingStatistics();
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
		// delegate
		this.logger.setApplicationControlledTrace(enable, thread);
	}


	/**
	 * Returns the specified log property from the log configuration.
	 *
	 * @param   name           a property name
	 * @param   defaultValue   a default value
	 *
	 * @return   a string
	 */
	public String getLogProperty(String name, String defaultValue)
	{
        return getLogger().getLogProperties().getProperty(
                    getLogger().getName(),
                    name,
                    defaultValue);
	}


	/**
	 * Checks whether some other object is "equal to" this one.
	 * 
	 * @param obj The reference object with which to compare.
	 * @return true if this object is the same as the obj argument; false 
	 *          otherwise.
	 */
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}
		
		if (obj != null && getClass() == obj.getClass()) {
			return (cfgName.equals(((ConfigDelegate)obj).cfgName) &&
			         appId.equals(((ConfigDelegate)obj).appId) &&
			         loggerName.equals(((ConfigDelegate)obj).loggerName));
		}
		
		return false;
	}


    ///////////////////////////////////////////////////////////////////////////
    //
    // LOG entity management
    //
    ///////////////////////////////////////////////////////////////////////////


    /**
     * Returns a list of all active (in use) log entities that are available
     * by the file logging mechanisms controlled by this logger.
     *
     * @return a list of {@link LogEntity} objects
     */
    public List<LogEntity> getActiveEntities()
    {
    	// delegate to the associated logger
    	return this.logger.getActiveEntities();
    }

    /**
     * Returns a list of all readable log entities that are available
     * by the file logging mechanisms controlled by this logger.
     *
     * @return a list of {@link LogEntity} objects
     */
    public List<LogEntity> getReadableEntities()
    {
    	// delegate to the associated logger
    	return this.logger.getReadableEntities();
    }

    /**
     * Returns a list of all removeable log entities that are available
     * by the file logging mechanisms controlled by this logger.
     *
     * @return a list of {@link LogEntity} objects
     */
    public List<LogEntity> getRemoveableEntities()
    {
    	// delegate to the associated logger
    	return this.logger.getRemoveableEntities();
    }

    /**
     * Remove a log entity.
     *
     * @param String an entity name
     */
    public void removeEntity(LogEntity entity)
    {
    	// delegate to the associated logger
    	this.logger.removeEntity(entity);
    }

    /**
     * Returns an entity reader for given log entity.
     *
     * @param String an entity name
     */
    public LogEntityReader getReader(LogEntity entity)
    {
    	// delegate to the associated logger
    	return this.logger.getReader(entity);
    }




    ///////////////////////////////////////////////////////////////////////////
    //
    // LOG framework specific functionality
    //
    ///////////////////////////////////////////////////////////////////////////


	/**
	 * Returns the associated logger.
	 *
	 * @return a logger
	 */
    public Log getLogger()
    {
        return this.logger;
    }



    private java.util.Date   instantiationTime = new java.util.Date();


    /** An config name */
    private final String  cfgName;

    /** An application id */
    private final String  appId;

    /** A config object is always associated with a logger */
    private final Log  logger;
        
    /** A config object is always associated with a logger */
//  private final LogProperties logProperties;

    /** A logger name the config is associated */
    private String  loggerName;


}

