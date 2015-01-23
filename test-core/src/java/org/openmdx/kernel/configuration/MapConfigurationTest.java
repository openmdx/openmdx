/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Map Backed Configuration Test
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openmdx.kernel.configuration.MapConfiguration;
import org.w3c.cci2.SparseArray;
import org.w3c.spi.PrimitiveTypeParsers;

/**
 * Map Backed Configuration Test
 */
public class MapConfigurationTest {

	@Test
	public void whenMapIsEmptyThenEntryNamesIsEmptyToo(){
		// Arrange
		final Map<String,?> map = Collections.emptyMap();
		// Act
		final MapConfiguration testee = new MapConfiguration(map, PrimitiveTypeParsers.getExtendedParser());
		// Assert
		Assert.assertTrue(testee.singleValuedEntryNames().isEmpty());
		Assert.assertTrue(testee.multiValuedEntryNames().isEmpty());
	}
	
	@Test
	public void whenEntryIsMissingThenReturnEmptySparseArray(){
		// Arrange
		final Map<String,?> map = Collections.emptyMap();
		final MapConfiguration testee = new MapConfiguration(map, PrimitiveTypeParsers.getExtendedParser());
		// Act
		final SparseArray<Integer> values = testee.getValues("feature", Integer.class);
		// Assert
		Assert.assertTrue(values.isEmpty());
	}

	@Test
	public void whenEntryIsMissingThenReturnDefaultValue(){
		// Arrange
		final String entryName = "feature";
		final Map<String,?> map = Collections.emptyMap();
		final MapConfiguration testee = new MapConfiguration(map, PrimitiveTypeParsers.getExtendedParser());
		final Integer defaultValue = Integer.valueOf(4711);
		// Act
		final Integer value = testee.getValue(entryName, defaultValue);
		// Assert
		Assert.assertEquals(defaultValue, value);
	}

	@Test
	public void whenEntryIsNullThenReturnDefaultValue(){
		// Arrange
		final String entryName = "feature";
		final Map<String,?> map = Collections.singletonMap(entryName, null);
		final MapConfiguration testee = new MapConfiguration(map, PrimitiveTypeParsers.getExtendedParser());
		final Integer defaultValue = Integer.valueOf(4711);
		// Act
		final Integer value = testee.getValue(entryName, defaultValue);
		// Assert
		Assert.assertEquals(defaultValue, value);
	}

	@Test
	public void whenEntryIsNullThenReturnNull(){
		// Arrange
		final String entryName = "feature";
		final Map<String,?> map = Collections.singletonMap(entryName, null);
		final MapConfiguration testee = new MapConfiguration(map, PrimitiveTypeParsers.getExtendedParser());
		// Act
		final Integer value = testee.getOptionalValue(entryName, Integer.class);
		// Assert
		Assert.assertNull(value);
	}
		
	@Test
	public void whenEntryIsMissingThenReturnNull(){
		// Arrange
		final String entryName = "feature";
		final Map<String,?> map = Collections.emptyMap();
		final MapConfiguration testee = new MapConfiguration(map, PrimitiveTypeParsers.getExtendedParser());
		// Act
		final Integer value = testee.getOptionalValue(entryName, Integer.class);
		// Assert
		Assert.assertNull(value);
	}
	
	@Test
	public void whenParserIsMissingThenKeepLong(){
		// Arrange
		final String entryName = "feature";
		final MapConfiguration testee = new MapConfiguration(null);
		testee.singleValued.put(entryName, Long.valueOf(-1));
		// Act
		final Long value = testee.getOptionalValue(entryName, Long.class);
		// Assert
		Assert.assertEquals(Long.valueOf(-1), value);
	}

	@Test
	public void whenParserIsMissingThenKeepString(){
		// Arrange
		final String entryName = "feature";
		final MapConfiguration testee = new MapConfiguration(null);
		testee.singleValued.put(entryName, "aValue");
		// Act
		final String value = testee.getOptionalValue(entryName, String.class);
		// Assert
		Assert.assertEquals("aValue", value);
	}

	@Test(expected = ClassCastException.class)
	public void whenParserIsMissingThenThrowException(){
		// Arrange
		final String entryName = "feature";
		final MapConfiguration testee = new MapConfiguration(null);
		testee.singleValued.put(entryName, "-1");
		// Act
		testee.getOptionalValue(entryName, Integer.class);
	}

	@Test
	public void whenParserIsGivenThenConvertStringToInteger(){
		// Arrange
		final String entryName = "feature";
		final MapConfiguration testee = new MapConfiguration(PrimitiveTypeParsers.getExtendedParser());
		testee.singleValued.put(entryName, "-1");
		// Act
		final Integer value = testee.getOptionalValue(entryName, Integer.class);
		// Assert
		Assert.assertEquals(Integer.valueOf(-1), value);
	}
	
	@Test
	public void whenTypeIsMissingThenCastToString(){
		// Arrange
		final String entryName = "feature";
		final Map<String,?> map = Collections.singletonMap(entryName, "(java.lang.String)aValue");
		final MapConfiguration testee = new MapConfiguration(map, PrimitiveTypeParsers.getExtendedParser());
		// Act
		final Object value = testee.getOptionalValue(entryName, (Class<?>)null);
		// Assert
		Assert.assertEquals("aValue", value);
	}

	@Test
	public void whenTypeIsMissingThenKeepString(){
		// Arrange
		final String entryName = "feature";
		final Map<String,?> map = Collections.singletonMap(entryName, "aValue");
		final MapConfiguration testee = new MapConfiguration(map, PrimitiveTypeParsers.getExtendedParser());
		// Act
		final Object value = testee.getOptionalValue(entryName, (Class<?>)null);
		// Assert
		Assert.assertEquals("aValue", value);
	}
	
	@Test
	public void whenValueClassIsMissingThenParseToSpecifiedTypes(){
		// Arrange
		final String entryName = "feature";
		final Map<String,Object> map = new HashMap<String, Object>();
		map.put(entryName + "[0]", "(java.lang.String)0");
		map.put(entryName + "[1]", "(java.lang.Integer)1");
		final MapConfiguration testee = new MapConfiguration(
			map, 
			PrimitiveTypeParsers.getExtendedParser()
		);
		// Act
		final SparseArray<?> values = testee.getValues(entryName, (Class<?>)null);
		// Assert
		Assert.assertEquals(2, values.size());
		Assert.assertEquals("0", values.get(Integer.valueOf(0)));
		Assert.assertEquals(Integer.valueOf(1), values.get(Integer.valueOf(1)));
	}

	@Test
	public void whenValueClassIsMissingThenKeepOriginalTypes(){
		// Arrange
		final String entryName = "feature";
		final Map<String,Object> map = new HashMap<String, Object>();
		map.put(entryName + "[0]", "0");
		map.put(entryName + "[1]", "(java.lang.Integer)1");
		final MapConfiguration testee = new MapConfiguration(
			map, 
			PrimitiveTypeParsers.getExtendedParser()
		);
		// Act
		final SparseArray<?> values = testee.getValues(entryName, (Class<?>)null);
		// Assert
		Assert.assertEquals(2, values.size());
		Assert.assertEquals("0", values.get(Integer.valueOf(0)));
		Assert.assertEquals(Integer.valueOf(1), values.get(Integer.valueOf(1)));
	}

}
