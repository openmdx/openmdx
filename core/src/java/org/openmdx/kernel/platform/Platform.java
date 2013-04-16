/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Platform 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland
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
package org.openmdx.kernel.platform;

import java.io.IOException;
import java.util.Properties;

import org.openmdx.kernel.log.SysLog;

/**
 * Platform
 */
public class Platform {

    /**
     * Constructor 
     */
    private Platform() {
        // Avoid instantiation
    }

    private static Properties configuration;

    private static Properties loadConfiguration(){
        Properties configuration = new Properties();
        try {
            configuration.load(
                Platform.class.getResourceAsStream("platform.properties")
            );
        } catch (IOException exception) {
            SysLog.error(
                "Platform configuration acquisition failure",
                exception
            );
        }
        return configuration;
    }
    
    private static Properties getConfiguration(
    ){
        if(Platform.configuration == null) {
            Platform.configuration = loadConfiguration();
        }
        return Platform.configuration;
    }
    
    /**
     * The property value has the following sources<ol>
     * <li>System Property
     * <li>Platform Configuration
     * <li>Default Value
     * </ol>
     * 
     * @param key
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
     * <li>Platform Configuration
     * </ol>
     * 
     * @param key
     * 
     * @return the platform specific value
     */
    public static String getProperty(
        String key,
        String defaultValue
    ){
        String value = Platform.getProperty(key);
        return value == null ?  defaultValue : value;
    }
 
    /**
     * Tells whether we are in a Java- or Dalvik-VM:<ul>
     * <li><code>Java-VM</code>
     * <li><code>Dalvik-VM</code>
     * </ul>
     *
     * @return the platform id
     */
    public static String getPlatform() {
        return getProperty("platform");
    }

}
