/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Properties Configuration Provider
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.kernel.collection.TreeSparseArray;
import org.openmdx.kernel.configuration.cci.Configuration;
import org.openmdx.kernel.configuration.cci.ConfigurationProvider;
import org.w3c.cci2.SparseArray;

public class PropertiesConfigurationProviderTest {

	@Test
	public void whenSectionIsEmptyThenReturnTopLevel() throws IOException{
		// Arrange
		final String sectionName = "";
		final String entryName = "layerPlugIn";
		final Properties source = new Properties();
		final String value = "nothing/to/do";
		source.put(entryName + "[0]", value);
		final ConfigurationProvider testee = Configurations.getDataproviderConfigurationProvider(
			source
		);
		// Act
		final Configuration configuration = testee.getSection(sectionName);
		final SparseArray<String> values = configuration.getSparseArray(entryName, String.class);
		// Assert
		Assertions.assertEquals(1, values.size());
		Assertions.assertEquals(value, values.get(0));
	}
	
	@Test
	public void whenEntryIsMissingThenReturnDefaultValue() throws IOException{
		// Arrange
		final String sectionName = "my/plug-in";
		final String entryName = "number";
		final Properties source = new Properties();
        final ConfigurationProvider testee = Configurations.getDataproviderConfigurationProvider(
            source
        );
		// Act
		final Configuration configuration = testee.getSection(sectionName);
		final Integer value = configuration.getOptionalValue(entryName, Integer.class).orElse(Integer.valueOf(7));
		// Assert
		Assertions.assertEquals(Integer.valueOf(7), value);
	}

	@Test
	public void whenEntryExistsThenReturnParsedValue() throws IOException{
		// Arrange
		final String sectionName = "my/plug-in";
		final String entryName = "number";
		final Properties source = new Properties();
		source.put(sectionName + '/' + entryName, "-5");
        final ConfigurationProvider testee = Configurations.getDataproviderConfigurationProvider(
            source
        );
		// Act
		final Configuration configuration = testee.getSection(sectionName);
		final Integer value = configuration.getOptionalValue(entryName, Integer.class).orElse(Integer.valueOf(7));
		// Assert
		Assertions.assertEquals(Integer.valueOf(-5), value);
	}
	
	@Test
	public void whenEntryIsOverriddenThenReturnOverriddenValue() throws IOException{
		// Arrange
		final String sectionName = "my/plug-in";
		final String entryName = "number";
		final Properties source = new Properties();
		source.put(sectionName + '/' + entryName, "-5");
        final ConfigurationProvider testee = Configurations.getDataproviderConfigurationProvider(
            source
        );
		final Map<String,Object> override = new HashMap<String,Object>();
		override.put(entryName, "8");
		// Act
		final Configuration configuration = testee.getSection(override, sectionName, Collections.emptyMap());
		final Integer value = configuration.getOptionalValue(entryName, Integer.class).orElse(Integer.valueOf(7));
		// Assert
		Assertions.assertEquals(Integer.valueOf(8), value);
	}

	@Test
	public void whenEntryIsMissingThenReturnEmptyValues() throws IOException{
		// Arrange
		final String sectionName = "my/plug-in";
		final String entryName = "number";
		final Properties source = new Properties();
        final ConfigurationProvider testee = Configurations.getDataproviderConfigurationProvider(
            source
        );
		// Act
		final Configuration configuration = testee.getSection(sectionName);
		final SparseArray<Long> values = configuration.getSparseArray(entryName, Long.class);
		// Assert
		Assertions.assertTrue(values.isEmpty());
	}
		
	@Test
	public void whenEntryExistsThenReturnParsedValues() throws IOException{
		// Arrange
		final String sectionName = "my/plug-in";
		final String entryName = "number";
		final Properties source = new Properties();
		source.put(sectionName + '/' + entryName + "[2]", "-5");
        final ConfigurationProvider testee = Configurations.getDataproviderConfigurationProvider(
            source
        );
		// Act
		final Configuration configuration = testee.getSection(sectionName);
		final SparseArray<Long> values = configuration.getSparseArray(entryName, Long.class);
		// Assert
		Assertions.assertEquals(1, values.size());
		Assertions.assertEquals(Long.valueOf(-5l), values.get(2));
	}
	
	@Test
	public void whenEntryIsAmendedThenReturnCombinedValues() throws IOException{
		// Arrange
		final String sectionName = "my/plug-in";
		final String entryName = "number";
		final Properties source = new Properties();
		source.put(sectionName + '/' + entryName + "[2]", "-5");
        final ConfigurationProvider testee = Configurations.getDataproviderConfigurationProvider(
            source
        );
		final Map<String,Object> override = new HashMap<String,Object>();
		override.put(entryName + "[1]", "8");
		// Act
		final Configuration configuration = testee.getSection(override, sectionName, Collections.emptyMap());
		final SparseArray<Long> values = configuration.getSparseArray(entryName, Long.class);
		// Assert
		Assertions.assertEquals(2, values.size());
		Assertions.assertEquals(Long.valueOf(8l), values.get(1));
		Assertions.assertEquals(Long.valueOf(-5l), values.get(2));
	}

    @Test
    public void whenEntryIsOverriddenThenReturnOverriddenValues() throws IOException{
        // Arrange
        final String sectionName = "my/plug-in";
        final String entryName = "number";
        final Properties source = new Properties();
        source.put(sectionName + '/' + entryName + "[2]", "-5");
        final ConfigurationProvider testee = Configurations.getDataproviderConfigurationProvider(
            source
        );
        final Map<String,Object> override = new HashMap<String,Object>();
        final SparseArray<Object> entryValue = new TreeSparseArray<>();
        entryValue.put(Integer.valueOf(1),"8");
        override.put(entryName, entryValue);
        // Act
        final Configuration configuration = testee.getSection(override, sectionName, Collections.emptyMap());
        final SparseArray<Long> values = configuration.getSparseArray(entryName, Long.class);
        // Assert
        Assertions.assertEquals(1, values.size());
        Assertions.assertEquals(Long.valueOf(8l), values.get(1));
        Assertions.assertNull(values.get(2));
    }
	
	@Test
	public void whenEntryInDefaultPropertiesIsNotOverriddenThenItIsVisible(
	) throws IOException{
		// Arrange
		final String sectionName = "my";
		final Properties defaultProperties = new Properties();
		defaultProperties.put("my/valueA", "A");
		defaultProperties.put("my/valueB", "bbb");
		final Properties source = new Properties(defaultProperties);
		defaultProperties.put("my/valueB", "B");
		defaultProperties.put("my/valueC", "C");
        final ConfigurationProvider testee = Configurations.getDataproviderConfigurationProvider(
            source
        );
		// Act
		final Configuration configuration = testee.getSection(sectionName);
		final String valueA = configuration.getOptionalValue("valueA", String.class).orElse("X");
		final String valueB = configuration.getOptionalValue("valueB", String.class).orElse("Y");
		final String valueC = configuration.getOptionalValue("valueC", String.class).orElse("Z");
		// Assert
		Assertions.assertEquals("A", valueA);
		Assertions.assertEquals("B", valueB);
		Assertions.assertEquals("C", valueC);
	}
}
