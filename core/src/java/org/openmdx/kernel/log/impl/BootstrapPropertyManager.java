/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: BootstrapPropertyManager.java,v 1.4 2007/10/10 16:06:06 hburger Exp $
 * Description: Logging
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:06 $
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


import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;


public class BootstrapPropertyManager

{
	public BootstrapPropertyManager(
		Log  logger)
	{
		this.logger = logger;
	}


	/**
	 * Add the log configuration and VM properties as log messages to the
	 * bootstrap log.
	 */
	public void addLogAndVmProperties()
	{
		addLogProperties();
		addJvmProperties();


		BootstrapLogger bootLogger = this.logger.getBootstrapLogger();

		bootLogger.logDetail(
			this, 
			"addLogAndVmProperties", 		
			"Running Microsoft VM: " + Log.isMicrosoftVM);
	}


	/**
	 * Add the log configuration properties to the bootstrap log event list.
	 */
	private void addLogProperties()
	{
		BootstrapLogger bootLogger = this.logger.getBootstrapLogger();


		bootLogger.logDetail(
			this,
			"addLogProperties",
			this.logger.getName() + "->Log Cfg Name: "
			+ this.logger.getConfig().getConfigName());

		bootLogger.logDetail(
			this,
			"addLogProperties",
			this.logger.getName() + "->Log App Id  : "
			+ this.logger.getConfig().getApplicationId());


		bootLogger.logDetail(
			this,
			"addLogProperties",
			this.logger.getName() + "->Logger      : "
			+ this.logger.getName());

		try {
			InetAddress ip = InetAddress.getLocalHost();
			bootLogger.logDetail(
				this,
				"addLogProperties",
				this.logger.getName() + "->IP Address  : " + ip.toString());
		}catch(java.net.UnknownHostException ex) {
		    // ignore
		}

		// Add the logging related Java VM properties
		Properties logVmProperties = 
						LogPropertiesLoader.getLoggingRelatedVmSystemProperties();
		
		for(Enumeration i = logVmProperties.propertyNames(); i.hasMoreElements();) {
			String name = (String)i.nextElement();

			bootLogger.logDetail(
				this,
				"addLogProperties",
				this.logger.getName() + "->VM Property : "
				+ name + " = " + logVmProperties.getProperty(name));
		}

		// Add the log configuration properties
		addProperties(
			"addLogProperties",
			this.logger.getName() + "->LOG Property: ",
			this.logger.getLogProperties().getAllProperties(),
			new String[] { "password", "passwd", "pwd" });
	}


	/**
	 * Add the configuration properties to the bootstrap log event list.
	 *
	 * @param all Add all properties
	 */
	private void addJvmProperties()
	{
		Properties props;

		String suppressProperty = this.logger.getLogProperties().getProperty(
										this.logger.getName(),
										"LogVmPropertiesSuppress",
										"");

		String[] suppressList = parseSuppressList(suppressProperty);


		if (suppressList.length != 0) {
			props = new Properties(System.getProperties());
		}else{
			// Selected VM properties
			String vmProperties[] = new String[] {
				"java.home",
				"java.version",
				"java.vendor",
				"java.vm.version",
				"java.vm.vendor",
				"java.vm.name",
				"java.vm.specification.version",
				"java.vm.specification.vendor",
				"java.vm.specification.name",
				"java.runtime.version",
				"java.runtime.name",
				"java.specification.version",
				"java.specification.vendor",
				"java.specification.name",
				"user.dir",
				"os.name",
				"os.arch",
				"os.version"
			};
			props = new Properties();

			for(int ii=0; ii<vmProperties.length; ii++) {
                if(
                    !"Microsoft Corp.".equals(System.getProperty("java.vendor")) ||
                    (!vmProperties[ii].startsWith("java.vm") && !vmProperties[ii].startsWith("java.specification"))
                ) {
                    String value = System.getProperty(vmProperties[ii]);
                    props.put(vmProperties[ii], value==null?"":value);
                }
			}
		}


		// Classpath property is handled especially
		props.remove("java.class.path");
		props.remove("sun.boot.class.path");
		addClasspath("java.class.path");
		addClasspath("sun.boot.class.path");
		
		
		addProperties(
			"addJvmProperties",
			this.logger.getName() + "->VM Property : ",
			props,
			suppressList);
	}



	/**
	 * Adds a bootstrap log message for each property
	 *
	 * @param props
	 */
	private void addClasspath(
		String  classpathProperty)
	{
		StringTokenizer  stClasspath;
		
		stClasspath = new StringTokenizer(
								System.getProperty(classpathProperty, ""),
								System.getProperty("path.separator"));
								
		while(stClasspath.hasMoreTokens()) {
			this.logger.getBootstrapLogger().logDetail(
				this,
				"addJvmProperties",
				this.logger.getName() + "->VM Property : "
				+ classpathProperty + " = " + stClasspath.nextToken());
		}
	}


	/**
	 * Adds a bootstrap log message for each property
	 *
	 * @param props
	 */
	private void addProperties(
		String     method,
		String     msgPrefix,
		Properties props,
		String[]   suppressList)
	{
		// Add the sorted property keys to bootstrap log events
		//       ArrayList keys = new ArrayList(this.logProperties.keySet());
		// The J# compliant way
		ArrayList keys = new ArrayList();
		for(Enumeration i = props.propertyNames(); i.hasMoreElements();) {
			keys.add(i.nextElement());
		}
		Collections.sort(keys);

		Iterator iterator = keys.iterator();
		String  key, value;
		while(iterator.hasNext()) {
			key   = (String)iterator.next();
			value = suppress(key, suppressList) ? "*******" : props.getProperty(key);

			this.logger.getBootstrapLogger().logDetail(
				this,
				method,
				msgPrefix + key +  " = '" + value + "'");
		}
	}


	/**
	 * Checks if a property (given by its name) must be supressed. A property
	 * is suppressed if its name contains the at least one of strings defined
	 * by the suppress list.
	 *
	 * @param key A property name
	 * @param supressList A suppress list
	 * @return true if the property must be suppressed
	 */
	private boolean suppress(
		String    name,
		String[]  suppressList)
	{
		if (suppressList != null) {
			for(int ii=0; ii<suppressList.length; ii++) {
				if (name.indexOf(suppressList[ii]) >= 0) {
					return true;
				}
			}
		}

		return false;
	}


	/**
	 * Parses a comma separated suppress list
	 *
	 * @param suppress a comma separated list
	 * @return The parsed strings
	 */
	private String[] parseSuppressList(
		String    suppress)
	{
		ArrayList  suppressList = new ArrayList();

		StringTokenizer  st = new StringTokenizer(suppress, ",");

		while(st.hasMoreTokens()) {
			suppressList.add(st.nextToken().trim());
		}

		return (String[])suppressList.toArray(new String[0]);
	}



	/** The logger these events belong to */
	private final Log logger;

}

