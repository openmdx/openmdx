/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LogPropertiesLoader.java,v 1.12 2008/02/05 13:41:39 hburger Exp $
 * Description: Logging
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/05 13:41:39 $
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Properties;

import org.openmdx.kernel.log.SysLog;

/**
 * A loader for log properties
 */
public class LogPropertiesLoader {


	/**
	 * Loads the log properties following loading priorities.
	 * 
	 * <p>The log property resolving strategy follows this priority 
	 * list:
     *
     * <pre>
	 * -------------------------------------------------------------------------
	 * Priority  Resolve
	 * -------------------------------------------------------------------------
	 *    1      APPLICATION SPECIFIC PROPERTIES
	 *           If application defined properties are available use them.
	 *           An application may set log properties using the static
	 *           method setLogProperties() on the AppLog and SysLog class.
	 *           If no application specific properties are available proceed
	 *           with the next priority level else proceed with priority 6
	 *
	 *    2a     VM PROPERTY 
	 *           If the logger is SysLog and the VM property 
	 *           "org.openmdx.log.property.filename.syslog" exist, read the 
	 *           properties from that file. If the file denoted by this property 
	 *           does not exist proceed with the next priority level (2b) else 
	 *           proceed with priority 6
	 *
	 *    2b     VM PROPERTY 
	 *           {"org.openmdx.log.property.filename"}
	 *           If this VM system property exists, read the properties from
	 *           that file. If the file denoted by this property does not exist
	 *           proceed with the next priority level  else proceed with 
	 *           priority 6
	 *
	 *    3      VM PROPERTY
	 *           {"org.openmdx.log.property.filedir"}
	 *           If this VM system property exists, read the properties from a 
	 *           file "{configname}.log.properties" in the directory denoted by
	 *           this system property. If the log property file does not exist
	 *           in this directory proceed with the next priority level else 
	 *           proceed with priority 6
	 *
	 *    4      CURRENT WORKING DIRECTORY
	 *           If a log property file exists in the current working directory,
	 *           read the properties from there otherwise proceed with the
	 *           next priority level else proceed with priority 6
	 *           The file name is "{configname}.log.properties"
	 *
	 *    5a     CLASSPATH
	 *           If a log property file "{configname}.log.properties" exists in 
	 *           the classpath read the properties from there otherwise proceed
	 *           with the next priority level else proceed with priority 6
	 *           The property loader uses the classloader that loaded the logger
	 *           class (AppLog, SysLog)
	 *
	 *    5b     CLASSPATH
	 *           If a generic log property file "Log.properties" exists in 
	 *           the classpath read the properties from there otherwise proceed
	 *           with the next priority level else proceed with priority 6 
	 *           The property loader uses the classloader that loaded the logger
	 *           class (AppLog, SysLog)
	 *
	 *    6      INHERIT SYSLOG 
	 *           If the log properties read so far contain the property
	 *           "InheritSysLog=true" add the SysLog properties. This is
	 *           only available for non SysLog loggers. Proceed with priority 7
	 *
	 *    7      DEFAULTS
	 *           As a last resort the default properties are loaded. Any 
	 *           property that has not been loaded so far is set to its default
	 *           value.
	 * 
     *    8      VM PROPERTY 
     *           If a log file path VM property "org.openmdx.log.path" exists, 
     *           it overides any otherwise configured log file path for storing
     *           the log files.
     *
	 * 
	 * Remarks:
	 * 
	 * i)   The {configname} can be set by AppLog.setConfigName() and
	 *      SysLog.setConfigName(). The default is "AppLog" or "SysLog"
	 *      respectively.
	 * 
	 * ii)  Each logger (AppLog, SysLog, ..) loads its properties following
	 *      this strategy.
	 * 
     * <pre>
	 * 
	 * @param log	A logger
	 * @param configName  The configuration name
	 * @param classLoader  A classloader
	 * @param applicationProperties Applicationproperties may be a null object
	 */
	public LogPropertiesLoader(
		Log				log,
		String			configName,
		ClassLoader		classLoader,
		Properties		applicationProperties)
	{
		this.log = log;
		this.configName = configName;
		this.classLoader = classLoader;
		this.applicationProperties = applicationProperties;
				
		setPropertySource("unknown");
	}

	
	/**
	 * Load the properties following the load priority list. 
	 * 
	 * <p>The default properties get merged with the properties from any other 
	 * properties obtained for a specific priority level.
	 * 
	 * @return properties (is never null)
	 */
	public Properties load()
	{
		Properties   properties = null;

		this.log.getBootstrapLogger().logDetail(
			this, "load", 
			getDebugLogID("0") 
			+ "Config name is: " + this.configName);		


		// Priority [7] DEFAULT
		Properties   defaultProperties = loadDefaults();
 
		
		do {
			// Priority [1] APPLICATION SPECIFIC PROPERTIES
			properties = loadFromApplicationProperties();
			if (properties != null) break;
		
			// Priority [2a] VM PROPERTY SYS_PROPERTY_SYSLOG_FILE
			if ("SysLog".equals(this.log.getName())) {
				properties = loadFromVmPropertyFile(SYS_PROPERTY_SYSLOG_FILE);
				if (properties != null) break;
			}

			// Priority [2b] VM PROPERTY SYS_PROPERTY_LOG_FILE
			properties = loadFromVmPropertyFile(SYS_PROPERTY_LOG_FILE);
			if (properties != null) break;
			
			// Priority [3] VM PROPERTY SYS_PROPERTY_LOG_FILE_DIR
			properties = loadFromVmPropertyDirectory(SYS_PROPERTY_LOG_FILE_DIR);
			if (properties != null) break;

			// Priority [4] CURRENT WORKING DIRECTORY
			properties = loadFromCurrentWorkingDirectory();
			if (properties != null) break;

			// Priority [5a] CLASSPATH
			properties = loadFromClasspath();
			if (properties != null) break;

			// Priority [5b] CLASSPATH "Log.properties"
			properties = loadFromClasspathDefault();
			if (properties != null) break;

			this.log.getBootstrapLogger().logDetail(
				this, "load", 
				getDebugLogID("-") 
				+ "Nothing left than the default properties");		

			properties = new Properties();
		} while(false);


		// Check if SysLog configuration inheritence is active
		String inherit = properties.getProperty("InheritSysLog", "false");
		if ("true".equals(inherit)) {
			if ("SysLog".equals(this.log.getName())) {
				// Ooops, this is not allowed for SysLog loggers!
				this.log.getBootstrapLogger().logError(
					this, "load", 
					getDebugLogID("6") 
					+ "The property 'InheritSysLog' is not allowed for a"
					+ " SysLog logger. Discarded the inheritence.");		
					
			}else{
				this.log.getBootstrapLogger().logDetail(
					this, "load", 
					getDebugLogID("6") 
					+ "Active SysLog inheritence flag. Merging with SysLog"
					+" properties");		

				Properties  sysLog = SysLog.getLogger().getLogProperties().getAllProperties();
				
				properties = mergeProperties(properties, sysLog);
			}
		}else{
			this.log.getBootstrapLogger().logDetail(
				this, "load", 
				getDebugLogID("6") 
				+ "SysLog inheritence is disabled");		
		}
		

		if (properties != null) {
			this.log.getBootstrapLogger().logDetail(
				this, "load", 
				getDebugLogID("7") 
				+ "Merging with the loaded default properties");		
		}
		
		properties = mergeProperties(properties, defaultProperties);

        // Systemwide override for log file path
        String logDir = System.getProperty(SYS_LOG_DIR);
        if (logDir != null) {
            this.log.getBootstrapLogger().logDetail(
                this, 
                "load", 
                getDebugLogID("8") + "Checking VM property " + SYS_LOG_DIR 
                + " => log dir <" + logDir + ">");      

            properties.put("LogFilePath", logDir);
        }
		
		return properties;
	}
	
	
	/**
	 * Returns a short description from where the properties have been loaded.
	 * 
	 * @return A property source
	 */
	public String getPropertySource()
	{
		return this.propertySource;
	}
	
	
	/**
	 * Returns the logging related VM system properties
	 * 
	 * @return the properties
	 */
	public static Properties getLoggingRelatedVmSystemProperties()
	{
		Properties props = new Properties();
		
		String[] names = new String[] {
			SYS_PROPERTY_LOG_FILE,
			SYS_PROPERTY_LOG_FILE_DIR,
			SYS_PROPERTY_LOG_DEBUG
		};
		
		for(int ii=0; ii<names.length; ii++) {
			String value = System.getProperty(names[ii]);
			
			if (value != null) {
				props.put(names[ii], value);
			}
		}
		
		return props;
	}
	
	
	/** 
	 * Merges the properties given by <code>properties2</code> to the
	 * properties given by <code>properties1</code> 
	 * 
	 * @param properties1
	 * @param properties2
	 * @return the merged properties
	 */
	@SuppressWarnings("unchecked")
    private Properties mergeProperties(
		Properties  properties1,
		Properties  properties2)
	{
		if (properties1 == null) {
			return properties2;
		}

		Properties merged = new Properties(properties1);
		String     key2, val1;
				
		for(
		    Enumeration<String> e = (Enumeration<String>) properties2.propertyNames(); 
		    e.hasMoreElements();
		) {
			key2 = e.nextElement();
			val1 = properties1.getProperty(key2);
			
			if (val1 == null) {		
				merged.put(key2, properties2.getProperty(key2));
			}
		}
		
		return merged;
	}


	/**
	 * Sets a short description from where the properties have been loaded.
	 * 
	 * @param A property source
	 */
	private void setPropertySource(
		String source)
	{
		this.propertySource = source;
	}


	
	/**
	 * Load the properties from the application defined properties 
	 * 
	 * <p> Priority level [1]
	 * 
	 * @return properties
	 */
	private Properties loadFromApplicationProperties()
	{
		if (this.applicationProperties == null) {
			this.log.getBootstrapLogger().logDetail(
				this, 
				"load", 
				getDebugLogID("1") + "Checking application properties => not defined");		

			return null;
		
		}else if (this.applicationProperties.size() == 0) {
			this.log.getBootstrapLogger().logDetail(
				this, 
				"load", 
				getDebugLogID("1") + "Checking application properties => empty");		

			return null;
		
		}else{
			setPropertySource("Loaded properties from application properties");

			this.log.getBootstrapLogger().logDetail(
				this, 
				"load", 
				getDebugLogID("1") + "Checking application properties");
						
			this.log.getBootstrapLogger().logDetail(
				this, "load", getDebugLogID("1") + getPropertySource());		

			return this.applicationProperties;
		}
	}
	

	/**
	 * Load the properties from a file specified by the given VM property.
	 * 
	 * <p> Priority level [2]
	 * 
	 * @param propertyName The VM property name
	 * @return properties
	 */
	private Properties loadFromVmPropertyFile(
		String propertyName)
	{
		String fileName = System.getProperty(propertyName);
		
		if (fileName == null) {
			this.log.getBootstrapLogger().logDetail(
				this, 
				"load", 
				getDebugLogID("2") + "Checking VM property " + propertyName		
				+ " => not defined");		

			return null;
		}else{
			this.log.getBootstrapLogger().logDetail(
				this, 
				"load", 
				getDebugLogID("2") + "Checking VM property " + propertyName 
				+ " => property file <" + fileName + ">");		
		}
			
		File file = new File(fileName);
		
		Properties properties = loadFromFile(file);
		
		if (properties != null) {
			setPropertySource(
				"Loaded properties from VM property " + propertyName 
				+ " => property file <" + file.getAbsolutePath() + ">");

			this.log.getBootstrapLogger().logDetail(
				this, "load", getDebugLogID("2") + getPropertySource());		
		}
		
		return properties;
	}


	/**
	 * Load the properties from the file "{configname}.log.properties" from 
	 * a directory specified by the given VM property.
	 * 
	 * <p> Priority level [3]
	 * 
	 * @param propertyName The VM property name
	 * @return properties
	 */
	private Properties loadFromVmPropertyDirectory(
		String propertyName)
	{
		String fileName = this.configName + LOG_PROPERTY_FILE_SUFFIX;

		String prop = System.getProperty(propertyName);
		
		if (prop == null) {
			this.log.getBootstrapLogger().logDetail(
				this, 
				"load", 
				getDebugLogID("3") + "Checking VM property " + propertyName 
				+ " => not defined");		
			return null;
		}else{
			this.log.getBootstrapLogger().logDetail(
				this, 
				"load", 
				getDebugLogID("3") + "Checking VM property " + propertyName 
				+ " => property file <" + fileName + ">");		
		}

		File dir = new File(prop);
		if (!dir.exists()) {
			return null;
		}

		File file = new File(dir, fileName);
		
		Properties properties = loadFromFile(file);
		
		if (properties != null) {
			setPropertySource(
				"Loaded properties from VM property " + propertyName 
				+ " => property file <" + file.getAbsolutePath() + ">");

			this.log.getBootstrapLogger().logDetail(
				this, "load", getDebugLogID("3") + getPropertySource());		
		}
		
		return properties;
	}


	/**
	 * Load the properties from the file "{configname}.log.properties" from 
	 * from the current working directory
	 * 
	 * <p> Priority level [4]
	 * 
	 * @return properties
	 */
	private Properties loadFromCurrentWorkingDirectory()
	{
		String fileName = this.configName + LOG_PROPERTY_FILE_SUFFIX;

		this.log.getBootstrapLogger().logDetail(
			this, 
			"load", 
			getDebugLogID("4") + "Checking current working directory for property file <"
			+ fileName + ">");	
		
		this.log.getBootstrapLogger().logDetail(
			this, 
			"load", 
			getDebugLogID("4") + "The current working directory is <"
			+ System.getProperty("user.dir") + ">");	

		File file = new File(".", fileName);
		Properties properties = loadFromFile(file);
		
		if (properties != null) {
			setPropertySource(
				"Loaded properties from current working directory" 
				+ " => property file <" + file.getAbsolutePath() + ">");

			this.log.getBootstrapLogger().logDetail(
				this, "load", getDebugLogID("4") + getPropertySource());		
		}
		
		return properties;
	}


	/**
	 * Load the properties from the file "{configname}.log.properties" from 
	 * the classpath
	 * 
	 * <p> Priority level [5a]
	 * 
	 * @return properties
	 */
	private Properties loadFromClasspath()
	{
		String resourceName = this.configName + LOG_PROPERTY_FILE_SUFFIX;
		
		this.log.getBootstrapLogger().logDetail(
			this, 
			"load", 
			getDebugLogID("5") + "Checking classpath for property resource <"
			+ resourceName + ">");		

		if (this.classLoader == null) {
			return null;
		}

		Properties properties = loadFromResource(resourceName);

		if (properties != null) {
			setPropertySource(
				"Loaded properties as resource from classpath" 
				+ " => property resource <" + resourceName + ">");

			this.log.getBootstrapLogger().logDetail(
				this, "load", getDebugLogID("5") + getPropertySource());		
		}
		
		return properties;
	}


	/**
	 * Load the properties from the file "Log.properties" from the classpath
	 * 
	 * <p> Priority level [5b]
	 * 
	 * @return properties
	 */
	private Properties loadFromClasspathDefault()
	{
		String resourceName = LOG_PROPERTY_FILE_DEFAULT;

		this.log.getBootstrapLogger().logDetail(
			this, 
			"load", 
			getDebugLogID("5") + "Checking classpath for property resource <"
			 + resourceName + ">");		

		if (this.classLoader == null) {
			return null;
		}

		Properties properties = loadFromResource(resourceName);

		if (properties != null) {
			setPropertySource(
				"Loaded properties as resource from classpath" 
				+ " => property resource <" + resourceName + ">");

			this.log.getBootstrapLogger().logDetail(
				this, "load", getDebugLogID("5") + getPropertySource());		
		}
		
		return properties;
	}
	
	
	/**
	 * Load the default properties.
	 * 
	 * <p> Priority level [6]
	 * 
	 * @return properties (never returns a null object)
	 */
	private Properties loadDefaults()
	{
		Properties props = new Properties();
		boolean slf4j = slf4jIsAvailable();
		// Set the default properties (see priority level 6)
		// setProperty(..) may not be used for J# compliance
		props.put(
            "LogLevel",
            slf4j ? "detail": "warning"
        );
		props.put("LogStatistics","false");
		props.put("LogPerformance","false");
		props.put("LogNotification","false");
        if(!slf4j) {
    		props.put("LogFilePath",".");
    		props.put("LogFileExtension","log");
        }
		props.put(
            "java.LoggingMechanism", 
            slf4j ? "SLF4JLoggingMechanism": "StandardErrLoggingMechanism"
        );
		props.put("ApplicationId", this.configName);
		props.put("ApplicationControlledTracing","false");
		props.put("LogVmPropertiesSuppress","");
		props.put(
            "LogFormat",
            slf4j ? 
                "${logsource}|${summary}|${detail}" : 
                "${logger}|${time}|${level}|${logsource}|" +
			      "${host}|${thread}|${class}|${method}|${line}|" +
			      "${summary}|${detail}");
		props.put(
            "LogFormatStatistics",
            slf4j ? 
                "${logsource}|${host}|${group}|${record}" :
                "${logger}|${time}|LS|${logsource}|" +
			      "${host}|${group}|${record}");

		setPropertySource("Loading default properties");
		this.log.getBootstrapLogger().logDetail(
			this, 
            "load", 
            getDebugLogID("0") + 
            getPropertySource()
        );		
		return props;
	}
	
    /**
     * Tells whether a foreign SLF4J logger is bound
     * 
     * @return
     */
	private boolean slf4jIsAvailable(){
        try {
            Class.forName(
                MARKER_BINDER_CLASS_NAME,
                true,
                ClassLoader.getSystemClassLoader()
            );
            Class<? extends Object> loggerBinderClass = Class.forName(
                LOGGER_BINDER_CLASS_NAME,
                true,
                ClassLoader.getSystemClassLoader()
            );
            Object loggerBinder = loggerBinderClass.getField(
                LOGGER_BINDER_SINGLETON_FIELD
            ).get(
                null // LOGGER_BINDER_SINGLETON_FIELD is static
            );
            Method loggerFactoryClassNameAccessor = loggerBinderClass.getMethod(
                LOGGER_FACTORY_CLASS_NAME_ACCESSOR,
                new Class[]{}
            );
            Object loggerFactoryClassName = loggerFactoryClassNameAccessor.invoke(
                loggerBinder,
                (Object[])null
            );
            boolean foreignLoggerFactory = !SysLogLoggerFactory.class.getName().equals(loggerFactoryClassName);
            this.log.getBootstrapLogger().logDetail(
                this, 
                "load", 
                getDebugLogID("0") + "Select " + (
                    foreignLoggerFactory ? "SLF4J" : "openMDX"
                ) + " as default logging system because the SLF4J logger factory class " + (
                    foreignLoggerFactory ? "is foreign" : "delegates back to openMDX' Log"
                ) + ": " + loggerFactoryClassName
            );        
            return foreignLoggerFactory;
        } catch (NoClassDefFoundError error) {
            this.log.getBootstrapLogger().logDetail(
                this, 
                "load", 
                getDebugLogID("0") + 
                    "Select openMDX Log as default logging system " +
                    "because SLF4J lookup ended with " + error.getClass().getName()
            );        
            return false;
        } catch (Exception exception) {
            this.log.getBootstrapLogger().logDetail(
                this, 
                "load", 
                getDebugLogID("0") + 
                    "Select openMDX Log as default logging system " +
                    "because SLF4J lookup ended with " + exception.getClass().getName()
            );        
            return false;
        }        
    }
	
	/**
	 * Load properties from a specified file
	 * 
	 * @return properties
	 */
	private Properties loadFromFile(File  file)
	{
		if (!file.exists()) {
			return null;
		}

		if (!file.canRead()) {
			this.log.getBootstrapLogger().logError(
				this, 
				"loadFromFile", 
				"Load error: The property file <" + file.getAbsolutePath() 
				+ "> has no read permission!");		

			return null;
		}
		
		FileInputStream stream = null;
		try {
			Properties props = new Properties();
				
			stream = new FileInputStream(file);
			props.load(stream);

			return props;
		}
		catch (IOException e) {
			this.log.getBootstrapLogger().logError(
				this, 
				"loadFromFile", 
				"Load error while reading the property file <" 
				+ file.getAbsolutePath() + ">!. " + e.toString());		
		}
		finally{
			if (stream != null) {
				try {stream.close();}catch(Exception ex) {
				    // ignore
				}
			}
		}

		return null;
	}
	

	/**
	 * Load properties from a resource file in the classpath
	 * 
	 * @return properties
	 */
	private Properties loadFromResource(String  name)
	{
		if (this.classLoader == null) {
			this.log.getBootstrapLogger().logError(
				this, 
				"loadFromFile", 
				"Load error: No classloader available to load property file"
				+ "resource <"+ name +">!");		
			return null;
		} 

		InputStream inputStream = null;		
		try {
			Properties props = new Properties();
			inputStream = this.classLoader.getResourceAsStream(name);

			if (inputStream == null) {
				// The resource could not be found
				return null;
			} 

			props.load(inputStream);

			return props;
		}
		catch (IOException e) {
			this.log.getBootstrapLogger().logError(
				this, 
				"loadFromResource", 
				"Load error while loading the property resource <" 
				+ name + ">!. " + e.toString());		
		}
		finally{
			if (inputStream != null) {
				try {inputStream.close();}catch(Exception ex) {
				    // ignore
				}
			}
		}

		return null;
	}
	

	private String getDebugLogID(String phase)
	{
        return new StringBuilder(
        ).append(
            "["
        ).append(
            phase
        ).append(
            "]"
        ).append(
            " "
        ).append(
            "["
        ).append(
            this.log.getName()
        ).append(
            "]"
        ).append(
            " "
        ).toString();
	}

    /**
     * The SLF4J Static Logger Binder Class Name
     */
    private static final String LOGGER_BINDER_CLASS_NAME = "org.slf4j.impl.StaticLoggerBinder";
    
    /**
     * The SLF4J Static Logger Binder Singleton Field
     */
    private static final String LOGGER_BINDER_SINGLETON_FIELD = "SINGLETON";

    /**
     * The SLF4J Static Logger Factor Class Name Accessor
     */
    private static final String LOGGER_FACTORY_CLASS_NAME_ACCESSOR = "getLoggerFactoryClassStr";
    
    /**
     * The SLF4J Static Marker Binder Class Name
     */
    private static final String MARKER_BINDER_CLASS_NAME = "org.slf4j.impl.StaticMarkerBinder";


    /** A VM System property denoting a directory for the log files */
    public static final String SYS_LOG_DIR = "org.openmdx.log.path";

    /** A VM System property denoting a log property file */
    public static final String SYS_PROPERTY_LOG_FILE = "org.openmdx.log.config.filename";

	/** A VM System property denoting a log property file */
	public static final String SYS_PROPERTY_SYSLOG_FILE = "org.openmdx.log.config.filename.syslog";

	/** A VM System property denoting a directory for log property files */
	public static final String SYS_PROPERTY_LOG_FILE_DIR = "org.openmdx.log.config.filedir";

	/** A VM System property to enable/disable the log debugging */
	public static final String SYS_PROPERTY_LOG_DEBUG = "org.openmdx.log.config.debug";

	/** The log property file suffix */
	public static final String LOG_PROPERTY_FILE_SUFFIX = ".log.properties";

	/** The default log property file */
	public static final String LOG_PROPERTY_FILE_DEFAULT = "Log.properties";
	
	private final Log  log;
	private final String  configName;
	private final ClassLoader  classLoader;
	private final java.util.Properties  applicationProperties;
	
	private String propertySource;
    
}


