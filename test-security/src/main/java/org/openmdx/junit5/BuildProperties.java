/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Build Properties for openMDX/Test Security
 * Owner:       Datura Informatik + Organisation AG, Switzerland, 
 *              https://www.datura.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.junit5;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.openmdx.kernel.log.SysLog;

/**
 * Build Properties for openMDX/Test Security
 */
public class BuildProperties {

	public static final String TIMEZONE_KEY = "org.openmdx.test-core.timezone";
	public static final String DATASOURCE_KEY = "org.openmdx.test-core.datasource";

	private static final String TIMEZONE_DEFAULT = "Europe/Zurich";
	
	/**
	 * The (legacy) format for non-XA drivers would look like:
	 * {@code "jdbc:postgresql:\\/\\/localhost\\/openmdx-test?user=openmdx-test&password=secret&driverClassName=org.postgresql.Driver"}
	 */
	private static final String DATASOURCE_DEFAULT = "jdbc:xa:org.postgresql.xa.PGXADataSource?user=openmdx-test&password=secret&databaseName=openmdx-test";
	
	private static final String FILE_NAME = "build.properties";

	public static Properties getBuildProperties() throws IOException {
		return getUserBuildProperties();
	}
	
	private static Properties getUserBuildProperties() throws IOException {
		final Properties userBuildProperties = new Properties(getProjectBuildProperties());
		final File userBuildPropertiesFile = new File(System.getProperty("user.home"), FILE_NAME);
		if (userBuildPropertiesFile.canRead()) {
			SysLog.info("Optional User Build Properties found", userBuildPropertiesFile.getAbsolutePath());
			userBuildProperties.load(new FileInputStream(userBuildPropertiesFile));
		} else {
			SysLog.info("No opptional User Build Properties found", userBuildPropertiesFile.getAbsolutePath());
		}
		return userBuildProperties;
	}

	private static Properties getProjectBuildProperties() throws IOException {
		final Properties projectBuildProperties = new Properties(getStandardBuildProperties());
		final File projectBuildPropertiesFile = new File(FILE_NAME);
		if (projectBuildPropertiesFile.canRead()) {
			SysLog.info("Optional Project Build Properties found", projectBuildPropertiesFile.getAbsolutePath());
			projectBuildProperties.load(new FileInputStream(projectBuildPropertiesFile));
		} else {
			SysLog.info("No optional Project Build Properties found", projectBuildPropertiesFile.getAbsolutePath());
		}
		return projectBuildProperties;
	}

	private static Properties getStandardBuildProperties() {
		final Properties standardBuildProperties = new Properties();
		standardBuildProperties.setProperty(TIMEZONE_KEY, TIMEZONE_DEFAULT);
		standardBuildProperties.setProperty(DATASOURCE_KEY, DATASOURCE_DEFAULT);
		return standardBuildProperties;
	}
	
}
