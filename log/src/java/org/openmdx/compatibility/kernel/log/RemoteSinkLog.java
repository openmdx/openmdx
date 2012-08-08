/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RemoteSinkLog.java,v 1.1 2008/03/21 18:21:56 hburger Exp $
 * Description: Raw Logger
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:21:56 $
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.kernel.log;


import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.log.impl.Log;




/**
 * The RemoteSinkLog class is used to log events received by the remote
 * sink servlet. It must not be used by applications.
 */
public class RemoteSinkLog extends Log {

	// This class must NEVER be instantiated. Actually RemoteSinkLog only extends
	// the class Log to prevent problems with multiple class
	// loaders. RemoteSinkLog uses a Log instance singleton by
	// delegation!
	private RemoteSinkLog() {
	    super();
	}




    ///////////////////////////////////////////////////////////////////////////
    //
    // LOG configuration
    //
    ///////////////////////////////////////////////////////////////////////////


	/**
	 * Set the config name that the logs pertain to. Must be called
	 * before doing any logging.
	 *
	 * @param cfgName - the config name
	 */
	public static void setConfigName(String cfgName)
	{
		// Setting a config name is only possible when the config
		// object does not exist and nothing has been logged yet.
		// The application name must be kept in sync with the Config object.
		if (!initialized) {
			if (cfgName == null) return;  // reject null strings
			String configName = cfgName.trim();
			if (configName.length()==0) return;  // reject empty strings

			RemoteSinkLog.configName = configName;
		}else{
			SysLog.error("A config name must be set before any logging " +
			             "takes place", new Exception());
		}
	}



	/**
	 * Sets the log properties [Config-Level-3]. setLogProperties() must be
	 * called before doing any logging.
	 *
	 * @param properties - the properties
	 */
	public static void setLogProperties(java.util.Properties props)
	{
		// Setting log properties is only possible when the config
		// object does not exist and nothing has been logged yet.
		if (!initialized) {
			RemoteSinkLog.logProperties = props;
		}else{
			SysLog.error("Log properties must be set before any logging " +
			             "takes place", new Exception());
		}
	}


	/**
     * Returns the logger's configuration object
	 *
     * @return  the configuration object
     */
    public static Config getLogConfig()
    {
		if (!initialized) init();

		// return a proxy
        return new Config(singleton.getConfig());
    }



    ///////////////////////////////////////////////////////////////////////////
    //
    // LOG methods
    //
    ///////////////////////////////////////////////////////////////////////////



	/**
     * Logs a remote log event
     *
	 * @param event  A log event
     */
    public static void log(LogEvent event)
    {
		if (!initialized) init();

		// Log the event?
		if (event.getLoggingLevel() <= singleton.getLoggingLevel()) {
		    singleton.logEvent(event, false);
		}
    }


    ///////////////////////////////////////////////////////////////////////////
    //
    // Init
    //
    ///////////////////////////////////////////////////////////////////////////

    private static synchronized void init()
    {
		// For Double-Checked-Locking behaviour used here, see comment for
		// SysLog.init()
		if (!initialized) {
			initialized = true;
			singleton.loadConfig(RemoteSinkLog.configName, RemoteSinkLog.logProperties);
			singleton.loadMechanisms();
			Log.register(RemoteSinkLog.getLogConfig());
			SysLog.trace(
                "RemoteSinkLog initialized with the given configuration", 
				RemoteSinkLog.configName 
			);
		}
    }

    /** Constants */
    final private static String LOGNAME   = "RemoteSinkLog";
    final static String         LOGSOURCE = "RemoteSink";


    /**
     * Provides the class variable for the Singleton pattern,
     * to keep track of the one and only instance of this class.
     */
    private static volatile Log singleton = newLog("RemoteSinkLog", RemoteSinkLog.class);

	private static volatile boolean initialized = false;


	/** The application properties. These properties are optional */
	private static java.util.Properties  logProperties   = null;

	/** The application name. Default is the log name */
    private static String configName = LOGNAME;

//	/** The log source */
//    private static Object logSource = LOGSOURCE;
//
//	/** The platform end-of-line delimiter */
//    private static final String EOL = System.getProperty("line.separator");
 }
