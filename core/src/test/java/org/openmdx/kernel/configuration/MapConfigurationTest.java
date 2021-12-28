/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Map Backed Configuration Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2014-2021, OMEX AG, Switzerland
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.kernel.configuration.cci.Configuration;
import org.w3c.cci2.SparseArray;

/**
 * Map Backed Configuration Test
 */
public class MapConfigurationTest {

	@Test
	public void whenMapIsEmptyThenEntryNamesIsEmptyToo(){
		// Arrange
		final Map<String,?> map = Collections.emptyMap();
		// Act
        final Configuration testee = Configurations.getBeanConfiguration(
            map
        );
		// Assert
		Assertions.assertTrue(testee.singleValuedEntryNames().isEmpty());
		Assertions.assertTrue(testee.multiValuedEntryNames().isEmpty());
	}
	
	@Test
	public void whenEntryIsMissingThenReturnEmptySparseArray(){
		// Arrange
        final Configuration testee = Configurations.getBeanConfiguration(
            Collections.emptyMap()
        );
		// Act
		final SparseArray<Integer> values = testee.getSparseArray("feature", Integer.class);
		// Assert
		Assertions.assertTrue(values.isEmpty());
	}

	@Test
	public void whenEntryIsMissingThenReturnDefaultValue(){
		// Arrange
		final String entryName = "feature";
        final Configuration testee = Configurations.getBeanConfiguration(
            Collections.emptyMap()
        );
		final Integer defaultValue = Integer.valueOf(4711);
		// Act
		final Integer value = testee.getOptionalValue(entryName, Integer.class).orElse(defaultValue);
		// Assert
		Assertions.assertEquals(defaultValue, value);
	}

	@Test
	public void whenEntryIsNullThenReturnDefaultValue(){
		// Arrange
		final String entryName = "feature";
        final Configuration testee = Configurations.getBeanConfiguration(
            Collections.singletonMap(entryName, null)
        );
		final Integer defaultValue = Integer.valueOf(4711);
		// Act
		final Integer value = testee.getOptionalValue(entryName, Integer.class).orElse(defaultValue);
		// Assert
		Assertions.assertEquals(defaultValue, value);
	}

	@Test
	public void whenEntryIsNullThenReturnAbsent(){
		// Arrange
		final String entryName = "feature";
        final Configuration testee = Configurations.getBeanConfiguration(
            Collections.singletonMap(entryName, null)
        );
		// Act
		final Optional<Integer> value = testee.getOptionalValue(entryName, Integer.class);
		// Assert
		Assertions.assertFalse(value.isPresent());
	}
		
	@Test
	public void whenEntryIsMissingThenReturnAbsent(){
		// Arrange
		final String entryName = "feature";
        final Configuration testee = Configurations.getBeanConfiguration(
            Collections.emptyMap()
        );
		// ActCollections.emptyMap()
		final Optional<Integer> value = testee.getOptionalValue(entryName, Integer.class);
		// Assert
        Assertions.assertFalse(value.isPresent());
	}
	
	@Test
	public void whenParserIsMissingThenKeepLong(){
		// Arrange
		final String entryName = "feature";
        final Configuration testee = Configurations.getBeanConfiguration(
            Collections.singletonMap(entryName, Long.valueOf(-1))
        );
		// Act
		final Optional<Long> value = testee.getOptionalValue(entryName, Long.class);
		// Assert
		Assertions.assertEquals(Long.valueOf(-1), value.get());
	}

	@Test
	public void whenParserIsMissingThenKeepString(){
		// Arrange
		final String entryName = "feature";
        final Configuration testee = Configurations.getBeanConfiguration(
            Collections.singletonMap(entryName, "aValue")
        );
		// Act
		final Optional<String> value = testee.getOptionalValue(entryName, String.class);
		// Assert
		Assertions.assertEquals("aValue", value.get());
	}

	@Test
	public void whenParserIsGivenThenConvertStringToInteger(){
		// Arrange
		final String entryName = "feature";
        final Configuration testee = Configurations.getBeanConfiguration(
            Collections.singletonMap(entryName, "-1")
        );
		// Act
		final Optional<Integer> value = testee.getOptionalValue(entryName, Integer.class);
		// Assert
		Assertions.assertEquals(Integer.valueOf(-1), value.get());
	}
	
	@Test
	public void whenTypeIsMissingThenCastToString(){
		// Arrange
		final String entryName = "feature";
		final Map<String,?> map = Collections.singletonMap(entryName, "(java.lang.String)aValue");
        final Configuration testee = Configurations.getBeanConfiguration(map);
		// Act
		final Optional<?> value = testee.getOptionalValue(entryName, (Class<?>)null);
		// Assert
		Assertions.assertEquals("aValue", value.get());
	}

	@Test
	public void whenTypeIsMissingThenKeepString(){
		// Arrange
		final String entryName = "feature";
		final Map<String,?> map = Collections.singletonMap(entryName, "aValue");
        final Configuration testee = Configurations.getBeanConfiguration(map);
		// Act
		final Optional<?> value = testee.getOptionalValue(entryName, (Class<?>)null);
		// Assert
		Assertions.assertEquals("aValue", value.get());
	}
	
	@Test
	public void whenValueClassIsMissingThenParseToSpecifiedTypes(){
		// Arrange
		final String entryName = "feature";
		final Map<String,Object> map = new HashMap<String, Object>();
		map.put(entryName + "[0]", "(java.lang.String)0");
		map.put(entryName + "[1]", "(java.lang.Integer)1");
        final Configuration testee = Configurations.getBeanConfiguration(map);
		// Act
		final SparseArray<?> values = testee.getSparseArray(entryName, (Class<?>)null);
		// Assert
		Assertions.assertEquals(2, values.size());
		Assertions.assertEquals("0", values.get(Integer.valueOf(0)));
		Assertions.assertEquals(Integer.valueOf(1), values.get(Integer.valueOf(1)));
	}

	@Test
	public void whenValueClassIsMissingThenKeepOriginalTypes(){
		// Arrange
		final String entryName = "feature";
		final Map<String,Object> map = new HashMap<String, Object>();
		map.put(entryName + "[0]", "0");
		map.put(entryName + "[1]", "(java.lang.Integer)1");
        final Configuration testee = Configurations.getBeanConfiguration(map);
		// Act
		final SparseArray<?> values = testee.getSparseArray(entryName, (Class<?>)null);
		// Assert
		Assertions.assertEquals(2, values.size());
		Assertions.assertEquals("0", values.get(Integer.valueOf(0)));
		Assertions.assertEquals(Integer.valueOf(1), values.get(Integer.valueOf(1)));
	}

}
