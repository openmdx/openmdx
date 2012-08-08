/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Configuration.java,v 1.1 2007/12/02 11:32:00 hburger Exp $
 * Description: Log Configuration 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/02 11:32:00 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package de.corag.openmdx.log;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.openmdx.kernel.log.SysLog;

/**
 * Configuration
 */
public class Configuration {

    public static Properties getLogPropertiesFromConfigFile(
    ) throws IOException {
        String logConfigFileName = System.getProperty("org.openmdx.log.config.filename");
        if(logConfigFileName == null) {
            return new Properties();
        } else {
            Properties configFileProperties = new Properties();
            configFileProperties.load(new FileInputStream(logConfigFileName));
            return configFileProperties;
        }
    }

    public static void setLogFilePathToTmpDir(){
        try {
            Properties logProperties = new Properties(getLogPropertiesFromConfigFile());
            String logFilePath = System.getProperty("java.io.tmpdir",".");
            logProperties.setProperty("LogFilePath", logFilePath);
            SysLog.setLogProperties(logProperties);
            SysLog.info("Log file path forced to tmp directory", logFilePath);
        } catch (IOException exception) {
            SysLog.error("Loading properties from config file failed", exception);
        }
    }
    
    /**
     * Just to test, whether the log configuration was successfull
     * 
     * @param args
     */
    public static void main(String[] args) {
        setLogFilePathToTmpDir();
        SysLog.info("Hello World");
    }

}
