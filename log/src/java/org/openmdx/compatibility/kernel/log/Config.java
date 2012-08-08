/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Config.java,v 1.1 2008/03/21 18:21:53 hburger Exp $
 * Description: Log Configuration Proxy
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:21:53 $
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
package org.openmdx.compatibility.kernel.log;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.MissingResourceException;

import org.openmdx.kernel.log.impl.ConfigDelegate;
import org.openmdx.kernel.log.impl.Log;



/**
 * The configuration for a specific logger
 */
public final class Config {

	/**
	 * Creates a new confiuration object
	 *
	 * @param delegate  A delegate
	 */
    public Config(
		ConfigDelegate delegation)
    {
		this.delegateRef = new WeakReference<ConfigDelegate>(delegation);
    }


	/**
	 * Checks if the configuration is still active.
	 */
	public boolean isActiv()
	{
		return (this.delegateRef.get() != null);
	}

	/**
	 * Return the instantion timestamp of the Config object.
	 * P.S. the logger has the same instantiation time.
	 *
	 * @return a Date
	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
	 */
	public java.util.Date getInstantiationTime()
		throws MissingResourceException
	{
		ConfigDelegate delegation = (ConfigDelegate)this.delegateRef.get();
		if (delegation != null) {
			return delegation.getInstantiationTime();
		}else{
			throw new MissingResourceException(
				"The config object delegate does not exist anymore."
				+ " Most probably the logger has been undeployed and the"
				+ " reference to the config's delegate has been released.",
				"", "");
		}
	}


	/**
	 * Return the application name string that the logs pertain to.
	 *
	 * @return a string
	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
	 */
	public String getConfigName()
		throws MissingResourceException
	{
    	return getDelegate().getConfigName();
	}



	/**
	 * Return the application name view string that the logs pertain to.
	 *
	 * @return a string
	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
	 */
	public String getApplicationId()
		throws MissingResourceException
	{
    	return getDelegate().getApplicationId();
	}



	/**
	 * Return the log name ("AppLog", "SysLog").
	 *
	 * @return a string
	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
	 */
	public String getLogName()
		throws MissingResourceException
	{
    	return getDelegate().getLogName();
	}


	/**
	 * Dump the log properties read from the log property file to
	 * a string
	 *
	 * @return  The stringified log properties (multiline)
	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
	 */
	public String dumpLogProperties()
		throws MissingResourceException
	{
		return getDelegate().dumpLogProperties();
	}


    /**
     * Set logging level.
     *
     * <p>
     * This method should not be used by applications. The logging level is set
     * in the log property file.
     *
     * @param level  a new logging level
	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
     */
	public void setLogLevel(int logLevel)
		throws MissingResourceException
	{
		getDelegate().setLogLevel(logLevel);
	}


	/**
     * Get logging level.
     *
     * <p>
     * This method should not be used by applications.
     *
     * @return the current logging level
	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
     */
	public int getLogLevel()
		throws MissingResourceException
	{
		return getDelegate().getLogLevel();
	}


    /**
     * Enable/Disable performance logging.
     *
     * <p>
     * This method should not be used by applications. The performance log state
     * is set in the log property file.
     *
     * @param enable   enable if true
 	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
    */
	public void enablePerformanceLog(boolean  enable)
		throws MissingResourceException
	{
		getDelegate().enablePerformanceLog(enable);
	}


    /**
     * Checks if performance logging is active.
     *
     * @return true if performance logging is active
	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
     */
	public boolean isPerformanceLog()
		throws MissingResourceException
	{
		return getDelegate().isPerformanceLog();
	}


    /**
     * Enable/Disable statistics logging.
     *
     * <p>
     * This method should not be used by applications. The statistics log state
     * is set in the log property file.
     *
     * @param enable   enable if true
	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
     */
	public void enableStatisticsLog(boolean  enable)
		throws MissingResourceException
	{
		getDelegate().enableStatisticsLog(enable);
	}


    /**
     * Checks if statistics logging is active.
     *
     * @return true if statistics logging is active
	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
     */
	public boolean isStatisticsLog()
		throws MissingResourceException
	{
		return getDelegate().isStatisticsLog();
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
	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
	 */
	public void setApplicationControlledTrace(
			boolean enable,
			Thread  thread)
		throws MissingResourceException
	{
		getDelegate().setApplicationControlledTrace(enable, thread);
	}


	/**
	 * Returns the specified log property from the log configuration.
	 *
	 * @param name a property name
	 * @param defaultValue a default value
	 * @return a log log property
	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
	 */
	public String getLogProperty(
			String name,
			String defaultValue)
		throws MissingResourceException
	{
       	return getDelegate().getLogProperty(name, defaultValue);
	}


	/**
	 * Checks whether some other object is "equal to" this one.
	 *
	 * @param obj The reference object with which to compare.
	 * @return true if this object is the same as the obj argument; false
	 * otherwise.
	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
	 */
	public boolean equals(Object obj)
		throws MissingResourceException
	{
		return getDelegate().equals(obj);
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
	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
     */
    public List<LogEntity> getActiveEntities()
		throws MissingResourceException
    {
   		return getDelegate().getActiveEntities();
    }

    /**
     * Returns a list of all readable log entities that are available
     * by the file logging mechanisms controlled by this logger.
     *
     * @return a list of {@link LogEntity} objects
     */
    public List<LogEntity> getReadableEntities()
		throws MissingResourceException
    {
   		return getDelegate().getReadableEntities();
    }

    /**
     * Returns a list of all removeable log entities that are available
     * by the file logging mechanisms controlled by this logger.
     *
     * @return a list of {@link LogEntity} objects
	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
     */
    public List<LogEntity> getRemoveableEntities()
		throws MissingResourceException
    {
   		return getDelegate().getRemoveableEntities();
    }

    /**
     * Remove a log entity.
     *
     * @param entity a log entity
	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
     */
    public void removeEntity(LogEntity entity)
		throws MissingResourceException
    {
		getDelegate().removeEntity(entity);
    }

    /**
     * Returns an entity reader for given log entity.
     *
     * @param entity a log entity
	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
     */
    public LogEntityReader getReader(LogEntity entity)
		throws MissingResourceException
    {
    	return getDelegate().getReader(entity);
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
	 * @throws MissingResourceException if the delegate config object does
	 * not exist anymore. Most probably the logger has been undeployed and the
	 * reference to the config's delegate has been released.
	 */
    Log getLogger()
		throws MissingResourceException
    {
        return getDelegate().getLogger();
    }


	private ConfigDelegate getDelegate()
		throws MissingResourceException
	{
		ConfigDelegate delegation = (ConfigDelegate)this.delegateRef.get();
		if (delegation != null) {
			return delegation;
		}else{
			throw new MissingResourceException(
				"The log config object reference does not exist anymore."
				+ " Most probably the logger has been undeployed.",
				"", "");
		}
	}

	private final WeakReference<ConfigDelegate> delegateRef;
}

