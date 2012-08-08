/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LogProperties.java,v 1.1 2008/03/21 18:22:01 hburger Exp $
 * Description: Logging
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:22:01 $
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.openmdx.compatibility.kernel.log.LogLevel;
import org.openmdx.compatibility.kernel.log.SysLog;

/**
 * LogProperties is a concrete class which manages all of the default
 * start-up properties for logs.  There is only one singleton
 * instance of this class.  The first time the application uses this
 * class, it will attempt to read its properties from the file named
 * [App].log.properties in the current directory, where [App] is the
 * application name taken from the associated log class. If the file
 * cannot be read the default property file LogProperties.properties from
 * the log package will be read instead. This allows an easy "out-of-the-box"
 * usage of the log package as well as sophisticated configuration capabilites.
 *
 * The following property tags and defaults will be used when
 * reading the log property file:
 *
 * <pre>
 * ApplicationId = XXXX
 * LogFileExtension = log
 * LogFilePath = c:/temp
 * LogLevel = 2
 * java.LoggingMechanism = StandardErrLoggingMechanism
 * LogPerformance = {false|true}
 * LogStatistics = {false|true}
 * LogNotification = {false|true}
 * ApplicationControlledTracing = {false|true}
 * LogVmPropertiesSuppress = item1,item2,item3,...
 * </pre>
 *
 * <p>If the property 'LogFilePath' is empty the file logs go to the current
 * working directory. If the property 'LogFilePath' is omitted the file
 * logs go to the platforms temp directory (Usually /tmp for UNIX,
 * and "C:\Documents and Settings\%USER%\Local Settings\Temp" on
 * Windows 2000 (according to the environment variable TEMP).
 *
 * <p>Any of the above property tags (except 'LogFileExtension' and 'LogFilePath'
 * can be overridden for a particular log, by prefixing the property key by 
 * the log name. 
 *  
 * <p>If 'LogVmPropertiesSuppress' is missing only some standard VM properties
 * get logged. If 'LogVmPropertiesSuppress' contains a comma separated list
 * of suppressed property names all VM properties except the ones that match the
 * suppress list are logged. A property matches the suppress list if it its name
 * contains one of the suppressed items.
 * 
 * <p>Also note that because you may have more than one logging mechanism open for 
 * the same log, append a consecutive integer to the property starting at 2.  
 * 
 * 
 * <p>For example:
 *
 * <pre>
 * LogFilePath = /tmp
 * AppLog.LogLevel = 3
 * AppLog.java.LoggingMechanism  = StandardErrorLoggingMechanism
 * AppLog.java.LoggingMechanism2 = SharedFileLoggingMechanism
 * AppLog.java.LoggingMechanism3 = CorbaLoggingMechanism
 * </pre>
 */
public class LogProperties {

	// Keys and default values
	public static final String STANDARD_ERROR_MECHANISM = "StandardErrorLoggingMechanism"; 

	static final String LOG_FILE_EXTENSION_PROPERTY = "LogFileExtension";
	static final String DEFAULT_LOG_FILE_EXTENSION = "log";

	static final String LOGGING_MECHANISM_PROPERTY = "java.LoggingMechanism";

	static final String LOGGING_LEVEL_PROPERTY = "LogLevel";
	static final String DEFAULT_LOGGING_LEVEL = "warning";

	static final String LOG_PERFORMANCE_PROPERTY = "LogPerformance";
	static final boolean DEFAULT_PERFORMANCE = false;

	static final String LOG_STATISTICS_PROPERTY = "LogStatistics";
	static final boolean DEFAULT_STATISTICS = false;

	static final String LOG_NOTIFICATION_PROPERTY = "LogNotification";
	static final boolean DEFAULT_NOTIFICATION = false;

	static final String LOG_APPCONTROLLEDTRACE_PROPERTY = "ApplicationControlledTracing";
	static final boolean DEFAULT_APPCONTROLLEDTRACE = false;

	private final String loaderLogName;
	private final String configName;
	private final String propertySource;
	
	private final Properties properties;

	private final Hashtable<String, Integer> logLevelMappingTable;


	/**
	 * Constructor
	 */
	public LogProperties(
		Log			log,
		String		configName,
		ClassLoader	propertyFileClassLoader,
		Properties	applicationProperties)
	{
		LogPropertiesLoader  loader = null;
		
		this.logLevelMappingTable = new Hashtable<String, Integer>();
		
		loader = new LogPropertiesLoader(
						log,
						configName,
						propertyFileClassLoader,
						applicationProperties);

	    this.loaderLogName  = log.getName();
	    this.configName     = configName;		
		
		this.properties = new Properties(loader.load());

		this.propertySource = loader.getPropertySource();
		
		presetLogLevelTable(this.logLevelMappingTable);
	}


	/**
	 * Returns all properties.
	 * 
	 * @return the properties
	 */
    public Properties getAllProperties()
	{
		return this.properties;
	}


	/**
	 * Returns the log property for a given key.
	 * 
	 * <p>Actually tries to read properties using the following name strategy and 
	 * returns the default value if no value could be found for any of the
	 * names.
	 * <ul>
	 * <li>name[1]: {logName}.{appId}.{key}
	 * <li>name[2]: {logName}.{key}
	 * <li>name[3]: {key}
	 * </ul>
	 * 
	 * @param logName a logger's name {AppLog, SysLog, ...}
	 * @param key the propertie's name
	 * @param defaultValue the propertie's default value
	 * @return the propertie's value
	 */
    public String getProperty(
		String logName, 
		String key, 
		String defaultValue) 
	{
		String value;
		String appId = getApplicationId(logName);
		String[] keys = new String[] { null, null, null };

		keys[2] = key;
		if (logName != null) {
			if ((appId != null) && appId.equals(logName)) {
				keys[0] = logName + "." + appId + "." + key;
			}
			keys[1] = logName + "." + key;
		}
		
		for(int ii=0; ii<keys.length; ii++) {
			if (keys[ii] == null) continue;
			
			value = this.properties.getProperty(keys[ii], "");

			if (value.length() > 0) return value;
		}
		
		return defaultValue;
	}


	/**
	 * Returns application. The default application id is the configuration
	 * name.
	 * 
	 * @param logName a logger's name {AppLog, SysLog, ...}
	 * @return  an application id
	 */
    public String getApplicationId(String logName)
	{
		String appId;
		
		if (logName != null) {
			appId = this.properties.getProperty(logName + ".ApplicationId", "");
			
			if (appId.length() > 0) return appId;
		}

		appId = this.properties.getProperty("ApplicationId", this.configName);
		
		return appId;
	}


	/**
	 * Returns the file path the log file should be written out under.
	 * 
	 * @param logName a logger's name {AppLog, SysLog, ...}
	 * @return  a file path
	 */
    public String getLoggingPath(String logName)
	{
        // <logName> is not used actually, because the logging path is a
        // global property that must not be overriden by specific loggers
		String path = getProperty(null, "LogFilePath", ".");

		// Reference to SysLog.LogFilePath?
		if (path.equals("SysLog.LogFilePath")) {
			// Prevent a resolve reference loop from SysLog itself
			if ("SysLog".equals(this.loaderLogName)) {
				LogLog.error(
					this.getClass(),
					"getLoggingPath",
					"The SysLog log property file must not define a logfile path"
					+ " reference like \"LogFilePath=SysLog.LogFilePath\". Using"
					+ " current working directory instead."
					, "");

				path = ".";
			}else{
				path = SysLog.getLogger().getLogProperties().getProperty(
						null, "LogFilePath", ".");
			}
		}

		if (path.length() == 0) path = ".";

		// ensure path ends with a path delimiter
		if (!(path.endsWith("/") || path.endsWith("\\"))) {
			path = path + "/";
		}

		// convert '\' to '/' so one can easily print them
		// works on SUN and IBM Java VMs on Windows
		return path.replace('\\', '/');
	}


	/**
	 * Returns the log filename extension for a specific logger.
	 * 
	 * @param logName a logger's name {AppLog, SysLog, ...}
	 * @return  a filename extension (e.g.: ".log")
	 */
    public String getLoggingExtension(String logName)
	{
        // 'logName' is not used actually, because the log file extension is a
        // global property that must not be overriden by specific loggers
		return this.properties.getProperty(
					LOG_FILE_EXTENSION_PROPERTY, 
					DEFAULT_LOG_FILE_EXTENSION);
	}


	/**
	 * Return the logging mechanisms for a specific logger.
	 * 
	 * @param logName a logger's name {AppLog, SysLog, ...}
	 * @return  an array of mechanism names
	 */
    public String[] getLoggingMechanismNames(String logName) 
	{
        ArrayList<String>  loggingMechanismNames = new ArrayList<String>();
		String  mechName;
		String  keyName;


		// java.LoggingMechanism = StandardErrLoggingMechanism
		keyName  = logName + "." + LOGGING_MECHANISM_PROPERTY;
		mechName = this.properties.getProperty(keyName, "");
		if (mechName.equals("")) {
			keyName  = LOGGING_MECHANISM_PROPERTY;
			mechName = this.properties.getProperty(keyName, "");
		}

		// No mechanism defined?
		if (mechName.equals("")) {
			// Add the default mechanism, so we have at least one mechanism.
			if (loggingMechanismNames.isEmpty()) {
				loggingMechanismNames.add(STANDARD_ERROR_MECHANISM);
			}

			return loggingMechanismNames.toArray(new String[loggingMechanismNames.size()]);
		}

		// Add the first configured mechanisms
		StringTokenizer  st = new StringTokenizer(mechName, ",");
		while(st.hasMoreTokens()) {
			mechName = st.nextToken().trim();
			loggingMechanismNames.add(mechName);
		}
		
		
		// Look for other logging mechanisms starting at 2
		int mechNumber = 2;
		String nextMechName = this.properties.getProperty(keyName + mechNumber, "");

		while(!nextMechName.equals("")) {
		    loggingMechanismNames.add(nextMechName);

		    mechNumber++;
		    nextMechName = this.properties.getProperty(keyName + mechNumber, "");
		}
		
		return loggingMechanismNames.toArray(new String[loggingMechanismNames.size()]);
	}


	/**
	 * Returns the logging level for a specific logger. The default level is
	 * "warning".
	 * 
	 * @param logName a logger's name {AppLog, SysLog, ...}
	 * @return a log level (according to class LogLevel)
	 */
    public int getLoggingLevel(String logName) 
	{
		String logLevel = getProperty(
					    	logName, 
					    	LOGGING_LEVEL_PROPERTY,
					        DEFAULT_LOGGING_LEVEL);

		Integer level = this.logLevelMappingTable.get(
									logLevel.toLowerCase());
		
		if (level != null) {
			return level.intValue();
		}else{
			return LogLevel.LOG_LEVEL_WARNING;
		}
	}


	/**
	 * Checks if performance logs are enabled for a specific logger.
	 * 
	 * @param logName a logger's name {AppLog, SysLog, ...}
	 * @return true if performance logs are enabled
	 */
    public boolean isLoggingPerformance(String logName)
	{
		return Boolean.valueOf(
		    getProperty(
		    	logName, 
		    	LOG_PERFORMANCE_PROPERTY,
		        new Boolean(DEFAULT_PERFORMANCE).toString())).booleanValue();
	}


	/**
	 * Checks if statistics logs are enabled for a specific logger.
	 * 
	 * @param logName a logger's name {AppLog, SysLog, ...}
	 * @return true if statistics logs are enabled
	 */
    public boolean isLoggingStatistics(String logName)
	{
		return Boolean.valueOf(
		    getProperty(
		    	logName, 
		    	LOG_STATISTICS_PROPERTY,
		        new Boolean(DEFAULT_STATISTICS).toString())).booleanValue();
	}


	/**
	 * Checks if log notification is enabled for a specific logger
	 *
	 * @param logName a logger's name {AppLog, SysLog, ...}
	 * @return true if notification logs are enabled
	 */
    public boolean  isLoggingNotification(String logName)
	{
		return Boolean.valueOf(
		    getProperty(
		    	logName, 
		    	LOG_NOTIFICATION_PROPERTY,
		        new Boolean(DEFAULT_NOTIFICATION).toString())).booleanValue();
	}


	/**
	 * Checks if log framework debugging is enabled.
	 * 
	 * @return true if log framework debugging is enabled
	 */
	static	
    public boolean isDebuggingEnabled()
	{
		return "true".equals(
					System.getProperty(
						LogPropertiesLoader.SYS_PROPERTY_LOG_DEBUG, "false"));
	}


	/**
	 * Returns a string representation of log properties read
	 *
	 * @return  a String
	 */
	@SuppressWarnings("unchecked")
    public String toString()
	{
		String   	tmp    = new String();

        StringWriter  sWriter = new StringWriter();
        PrintWriter   pWriter = new PrintWriter(sWriter);


        pWriter.println("# Property Source : " + this.propertySource);
        pWriter.println("# Log Config Name : " + this.configName);
        pWriter.println("# Logger          : " + this.loaderLogName
                                               + " " + 
                                               org.openmdx.kernel.Version.getImplementationVersion());

		Collection<String> keys = new TreeSet<String>();
		for(
		    Enumeration<String> i = (Enumeration<String>) this.properties.propertyNames(); 
		    i.hasMoreElements();
		) {
			keys.add(i.nextElement());
		}
		for(
	        Iterator<String> iterator = keys.iterator();    
	        iterator.hasNext();
	     ) {
			String key = iterator.next();
			if (isSecureProperty(key)) {
				pWriter.println(key + "= '*******'");
			}else{
				pWriter.println(key + "=" + this.properties.getProperty(key));
			}
		}

		tmp = sWriter.toString();

		return ( tmp );
	}


	/**
	 * Preset the log level conversion table.
	 * 
	 * @param table
	 */
	private void presetLogLevelTable(Hashtable<String, Integer> table)
	{
		// old format
		table.put("1", Integer.valueOf(LogLevel.LOG_LEVEL_CRITICAL_ERROR));
		table.put("2", Integer.valueOf(LogLevel.LOG_LEVEL_ERROR));
		table.put("3", Integer.valueOf(LogLevel.LOG_LEVEL_WARNING));
		table.put("4", Integer.valueOf(LogLevel.LOG_LEVEL_INFO));
		table.put("5", Integer.valueOf(LogLevel.LOG_LEVEL_DETAIL));
		table.put("6", Integer.valueOf(LogLevel.LOG_LEVEL_TRACE));

		// Some customers use level 7 or 8 to enable all logging levels.
		// Don't let them down :-)
		table.put("7", Integer.valueOf(LogLevel.LOG_LEVEL_TRACE));
		table.put("8", Integer.valueOf(LogLevel.LOG_LEVEL_TRACE));

		// new format version 2.2 and higher
		table.put("critical", Integer.valueOf(LogLevel.LOG_LEVEL_CRITICAL_ERROR));
		table.put("error",    Integer.valueOf(LogLevel.LOG_LEVEL_ERROR));
		table.put("warning",  Integer.valueOf(LogLevel.LOG_LEVEL_WARNING));
		table.put("info",     Integer.valueOf(LogLevel.LOG_LEVEL_INFO));
		table.put("detail",   Integer.valueOf(LogLevel.LOG_LEVEL_DETAIL));
		table.put("trace",    Integer.valueOf(LogLevel.LOG_LEVEL_TRACE));
	}


	/** 
	 * Checks if a property contains 'secret' data.
	 * 
	 * @param name A property name
	 * @return true if its a secret
	 */
	private boolean isSecureProperty(String name)
	{
		final String[] secrets = new String[] {"password", "passwd", "pwd"};
		
		for(int ii=0; ii<secrets.length; ii++) {
			if (name.indexOf(secrets[ii]) >= 0) {
				return true;
			}
		}
		
		return false;
	}
}

