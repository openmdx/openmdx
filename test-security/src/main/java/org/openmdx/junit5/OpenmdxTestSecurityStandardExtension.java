/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: JUnit 5 Extension for openMDX/Test Securtiy
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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openmdx.kernel.lightweight.naming.NonManagedInitialContextFactoryBuilder;

/**
 * JUnit 5 Standard Extension for openMDX/Test Security
 */
public class OpenmdxTestSecurityStandardExtension implements BeforeAllCallback {

	public void beforeAll(ExtensionContext context) throws Exception {
		if (!NamingManager.hasInitialContextFactoryBuilder()) {
			final Properties buildProperties = BuildProperties.getBuildProperties();
			configureURLs();
			configureTimezone(buildProperties);
			configureJNDI(buildProperties);
		}
	}

	private void configureJNDI(final Properties buildProperties) throws NamingException {
		final Map<String, String> environment = new HashMap<>();
		environment.put(
			"org.openmdx.comp.env.ldap.bund", 
			"eis:org.openmdx.resource.ldap.v3.ManagedConnectionFactory?" +
			"ProtocolVersion=(java.lang.Integer)3&" + 
			"ConnectionURL=ldap:\\/\\/x500.bund.de");
		environment.put(
			"org.openmdx.comp.env.ldap.fake",
			"eis:org.openmdx.resource.ldap.ldif.ManagedConnectionFactory?" + 
			"ProtocolVersion=(java.lang.Integer)3&" + 
			"ConnectionURL=xri:\\/\\/+resource\\/test\\/openmdx\\/resource\\/ldap\\/bund.ldif"
		);
		environment.put(
			"org.openmdx.comp.env.ldap.local", 
			"eis:org.openmdx.resource.ldap.v3.ManagedConnectionFactory?" + 
			"ProtocolVersion=(java.lang.Integer)3&" + 
			"ConnectionURL=ldaps:\\/\\/localhost"
		);
		NonManagedInitialContextFactoryBuilder.install(environment);
	}

	private void configureTimezone(final Properties buildProperties) {
		System.setProperty("user.timezone", buildProperties.getProperty(BuildProperties.TIMEZONE_KEY));
	}

	private void configureURLs() {
		System.setProperty("java.protocol.handler.pkgs", "org.openmdx.kernel.url.protocol");
	}

}
