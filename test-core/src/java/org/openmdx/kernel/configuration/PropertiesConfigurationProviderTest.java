/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Properties Configuration Provider
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2014, OMEX AG, Switzerland
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
package org.openmdx.kernel.configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.cci2.SparseArray;
import org.w3c.spi.PrimitiveTypeParsers;

public class PropertiesConfigurationProviderTest {

	private static final char DELIMITER = '/';
	
	@Test
	public void whenSectionIsEmptyThenReturnTopLevel() throws IOException{
		// Arrange
		final String sectionName = "";
		final String entryName = "layerPlugIn";
		final Properties source = new Properties();
		final String value = "nothing" + DELIMITER + "to" + DELIMITER + "do";
		source.put(entryName + "[0]", value);
		final PropertiesConfigurationProvider testee = new PropertiesConfigurationProvider(
			PrimitiveTypeParsers.getExtendedParser(),
			DELIMITER,
			source
		);
		// Act
		final Configuration configuration = testee.getConfiguration(sectionName);
		final SparseArray<String> values = configuration.getValues(entryName, String.class);
		// Assert
		Assert.assertEquals(1, values.size());
		Assert.assertEquals(value, values.get(0));
	}
	
	@Test
	public void whenEntryIsMissingThenReturnDefaultValue() throws IOException{
		// Arrange
		final String sectionName = "my" + DELIMITER + "plug-in";
		final String entryName = "number";
		final Properties source = new Properties();
		final PropertiesConfigurationProvider testee = new PropertiesConfigurationProvider(
			PrimitiveTypeParsers.getExtendedParser(),
			DELIMITER,
			source
		);
		// Act
		final Configuration configuration = testee.getConfiguration(sectionName);
		final Integer value = configuration.getValue(entryName, Integer.valueOf(7));
		// Assert
		Assert.assertEquals(Integer.valueOf(7), value);
	}

	@Test
	public void whenEntryExistsThenReturnParsedValue() throws IOException{
		// Arrange
		final String sectionName = "my" + DELIMITER + "plug-in";
		final String entryName = "number";
		final Properties source = new Properties();
		source.put(sectionName + DELIMITER + entryName, "-5");
		final PropertiesConfigurationProvider testee = new PropertiesConfigurationProvider(
			PrimitiveTypeParsers.getExtendedParser(),
			DELIMITER,
			source
		);
		// Act
		final Configuration configuration = testee.getConfiguration(sectionName);
		final Integer value = configuration.getValue(entryName, Integer.valueOf(7));
		// Assert
		Assert.assertEquals(Integer.valueOf(-5), value);
	}
	
	@Test
	public void whenEntryIsOverriddenThenReturnOverriddenValue() throws IOException{
		// Arrange
		final String sectionName = "my" + DELIMITER + "plug-in";
		final String entryName = "number";
		final Properties source = new Properties();
		source.put(sectionName + DELIMITER + entryName, "-5");
		final PropertiesConfigurationProvider testee = new PropertiesConfigurationProvider(
			PrimitiveTypeParsers.getExtendedParser(),
			DELIMITER,
			source
		);
		final Map<String,Object> override = new HashMap<String,Object>();
		override.put(entryName, "8");
		// Act
		final Configuration configuration = testee.getConfiguration(sectionName, override);
		final Integer value = configuration.getValue(entryName, Integer.valueOf(7));
		// Assert
		Assert.assertEquals(Integer.valueOf(8), value);
	}

	@Test
	public void whenEntryIsMissingThenReturnEmptyValues() throws IOException{
		// Arrange
		final String sectionName = "my" + DELIMITER + "plug-in";
		final String entryName = "number";
		final Properties source = new Properties();
		final PropertiesConfigurationProvider testee = new PropertiesConfigurationProvider(
			PrimitiveTypeParsers.getExtendedParser(),
			DELIMITER,
			source
		);
		// Act
		final Configuration configuration = testee.getConfiguration(sectionName);
		final SparseArray<Long> values = configuration.getValues(entryName, Long.class);
		// Assert
		Assert.assertTrue(values.isEmpty());
	}
		
	@Test
	public void whenEntryExistsThenReturnParsedValues() throws IOException{
		// Arrange
		final String sectionName = "my" + DELIMITER + "plug-in";
		final String entryName = "number";
		final Properties source = new Properties();
		source.put(sectionName + DELIMITER + entryName + "[2]", "-5");
		final PropertiesConfigurationProvider testee = new PropertiesConfigurationProvider(
			PrimitiveTypeParsers.getExtendedParser(),
			DELIMITER,
			source
		);
		// Act
		final Configuration configuration = testee.getConfiguration(sectionName);
		final SparseArray<Long> values = configuration.getValues(entryName, Long.class);
		// Assert
		Assert.assertEquals(1, values.size());
		Assert.assertEquals(Long.valueOf(-5l), values.get(2));
	}
	
	@Test
	public void whenEntryIsOverriddenThenReturnOverriddenValues() throws IOException{
		// Arrange
		final String sectionName = "my" + DELIMITER + "plug-in";
		final String entryName = "number";
		final Properties source = new Properties();
		source.put(sectionName + DELIMITER + entryName + "[2]", "-5");
		final PropertiesConfigurationProvider testee = new PropertiesConfigurationProvider(
			PrimitiveTypeParsers.getExtendedParser(),
			DELIMITER,
			source
		);
		final Map<String,Object> override = new HashMap<String,Object>();
		override.put(entryName + "[1]", "8");
		// Act
		final Configuration configuration = testee.getConfiguration(sectionName, override);
		final SparseArray<Long> values = configuration.getValues(entryName, Long.class);
		// Assert
		Assert.assertEquals(1, values.size());
		Assert.assertEquals(Long.valueOf(8l), values.get(1));
		Assert.assertNull(values.get(2));
	}
	
	@Test
	public void whenEntryInDefaultPropertiesIsNotOverriddenThenItIsVisible(
	) throws IOException{
		// Arrange
		final String sectionName = "my";
		final Properties defaultProperties = new Properties();
		defaultProperties.put("my" + DELIMITER + "valueA", "A");
		defaultProperties.put("my" + DELIMITER + "valueB", "bbb");
		final Properties source = new Properties(defaultProperties);
		defaultProperties.put("my" + DELIMITER + "valueB", "B");
		defaultProperties.put("my" + DELIMITER + "valueC", "C");
		final PropertiesConfigurationProvider testee = new PropertiesConfigurationProvider(
			PrimitiveTypeParsers.getExtendedParser(),
			DELIMITER,
			source
		);
		// Act
		final Configuration configuration = testee.getConfiguration(sectionName);
		final String valueA = configuration.getValue("valueA", "X");
		final String valueB = configuration.getValue("valueB", "Y");
		final String valueC = configuration.getValue("valueC", "Z");
		// Assert
		Assert.assertEquals("A", valueA);
		Assert.assertEquals("B", valueB);
		Assert.assertEquals("C", valueC);
	}
}
