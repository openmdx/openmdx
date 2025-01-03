/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: JUnit 5 Extension for openMDX/Test Core
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

import java.util.Collections;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;

import javax.naming.NamingException;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openmdx.kernel.lightweight.naming.LightweightInitialContextFactoryBuilder;
import org.openmdx.kernel.log.SysLog;

/**
 * JUnit 5 Standard Extension for openMDX/Test Core
 */
public class OpenmdxTestCoreStandardExtension implements BeforeAllCallback {

	public void beforeAll(ExtensionContext context) throws Exception {
		if (!LightweightInitialContextFactoryBuilder.isInstalled()) {
			final Properties buildProperties = BuildProperties.getBuildProperties();
		    configureTimezone(buildProperties);
			configureLightweightContainer(buildProperties);
		}
	}

	private void configureLightweightContainer(
		final Properties buildProperties
	) throws NamingException {
		String dataSourceURL = buildProperties.getProperty(BuildProperties.DATASOURCE_KEY);
		SysLog.log(Level.INFO, "Sys|Build Property {0}|{1}", BuildProperties.DATASOURCE_KEY, dataSourceURL);
		LightweightInitialContextFactoryBuilder.install(
			Collections.singletonMap(
				"org.openmdx.comp.env.jdbc.DataSource", 
				dataSourceURL
			)
		);
	}

	private void configureTimezone(final Properties buildProperties) {
		String configuredTimeZone = buildProperties.getProperty(BuildProperties.TIMEZONE_KEY);
		System.setProperty("user.timezone", configuredTimeZone);
		TimeZone.setDefault(TimeZone.getTimeZone(configuredTimeZone));
	}

}
