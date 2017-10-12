/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Configuration
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2016, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.application;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.openmdx.kernel.log.SysLog;

/**
 * Configuration
 */
public class Configuration {

    /**
     * Constructor 
     */
    private Configuration() {
        // Avoid instantiation
    }

    /**
     * The lazily loaded configuration
     */
    private static Properties configuration;
    
    /**
     * The configuration file name
     */
    private static final String CONFIGURATION = "configuration.properties";

    /**
     * The property value has the following sources<ol>
     * <li>System Property
     * <li>Configuration Value
     * </ol>
     * 
     * @param key the property name or configuration entry
     * 
     * @return the platform specific value
     */
    public static String getProperty(
        String key
    ){
        String value = System.getProperty(key);
        if(value == null) value = getConfiguration().getProperty(key);
        return value == null ? null : value.trim();
    }

    /**
     * The property value has the following sources<ol>
     * <li>System Property
     * <li>Configuration Value
     * <li>Default Value
     * </ol>
     * 
     * @param key the property name or configuration entry
     * @param defaultValue the default value
     * 
     * @return the platform specific value
     */
    public static String getProperty(
        String key,
        String defaultValue
    ){
        String value = Configuration.getProperty(key);
        return value == null ?  defaultValue : value;
    }
 
    private static Properties getConfiguration(
    ){
        if(Configuration.configuration == null) {
            Configuration.configuration = loadConfiguration();
        }
        return Configuration.configuration;
    }
    
    private static Properties loadConfiguration(){
        final Properties configuration = new Properties();
        final InputStream stream = Configuration.class.getResourceAsStream(CONFIGURATION);
        if(stream == null) {
            SysLog.detail(CONFIGURATION + " missing");
        } else {
	        try {
				configuration.load(stream);
	        } catch (IOException exception) {
	            SysLog.error(
            		CONFIGURATION + " processing failure",
	                exception
	            );
	        }
        }
        return configuration;
    }
    
}
