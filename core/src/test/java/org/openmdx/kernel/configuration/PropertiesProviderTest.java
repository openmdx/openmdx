/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Properties Provider Test
 * Owner:       the original authors.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.configuration;

import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.base.collection.Sets;

class PropertiesProviderTest {

	@Test
	void when_complex_then_applyPriorities() throws IOException{
		// Arrange
		final String uri = "xri://+resource/org/openmdx/kernel/configuration/P.properties";
		final Set<String> expectedNames = Sets.asSet("A","B","C","I","R","U","V","W","X","Y","Z");
		// Act
		final Properties properties = PropertiesProvider.getProperties(uri);
		final Set<String> names = properties.stringPropertyNames();
		// Assert
		Assertions.assertEquals(expectedNames, names);
		Assertions.assertEquals("A.P",properties.getProperty("A"));
		Assertions.assertEquals("B.P10",properties.getProperty("B"));
		Assertions.assertEquals("C.P20",properties.getProperty("C"));
		Assertions.assertEquals("I.I10",properties.getProperty("I"));
		Assertions.assertEquals("U.P10",properties.getProperty("U"));
		Assertions.assertEquals("V.E",properties.getProperty("V"));
		Assertions.assertEquals("W.E",properties.getProperty("W", "W.-"));
		Assertions.assertEquals("X.P",properties.getProperty("X"));
		Assertions.assertEquals("Y.D10",properties.getProperty("Y"));
		Assertions.assertEquals("Z.P20",properties.getProperty("Z"));
		Assertions.assertEquals("", properties.getProperty("R"));
		Assertions.assertNull(properties.getProperty("S"));
		Assertions.assertEquals("T.-", properties.getProperty("T","T.-"));
	}

	@Test
	void when_conflictInInclude_then_exception() throws IOException{
		// Arrange
		final String uri = "xri://+resource/org/openmdx/kernel/configuration/C.properties";
		// Act
		Assertions.assertThrows(InvalidPropertiesFormatException.class, () -> PropertiesProvider.getProperties(uri));
	}
	
	@Test
	void when_conflictInFile_then_override() throws IOException{
		// Arrange
		final String uri = "xri://+resource/org/openmdx/kernel/configuration/F.properties";
		final Set<String> expectedNames = Sets.asSet("Q","R");
		// Act
		final Properties properties = PropertiesProvider.getProperties(uri);
		final Set<String> names = properties.stringPropertyNames();
		// Assert
		Assertions.assertEquals(expectedNames, names);
		Assertions.assertEquals("S.F",properties.getProperty("Q"));
		Assertions.assertEquals("R.F",properties.getProperty("R"));
	}

}
