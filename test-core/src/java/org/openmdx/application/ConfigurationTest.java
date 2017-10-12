/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Configuration Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2016, OMEX AG, Switzerland
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
package org.openmdx.application;

import org.junit.Test;

import org.junit.Assert;

/**
 * Configuration Test
 */
public class ConfigurationTest {

	private final String MISSING_KEY = "test.openmdx.jdo.option.Missing";	
	private final String EXISTING_KEY = "test.openmdx.jdo.option.Existing";	
	
	@Test
	public void missingProperyWithoutDefaultValue() {
		// Act
		final String value = Configuration.getProperty(MISSING_KEY);
		// Assert
		Assert.assertNull(value);
	}
	
	@Test
	public void missingProperyWithDefaultValue() {
		// Act
		final String value = Configuration.getProperty(MISSING_KEY, "default");
		// Assert
		Assert.assertEquals("default", value);
	}

	@Test
	public void missingProperyWithSystemProperty() {
		// Arrange
		System.setProperty(MISSING_KEY,"property");
		try {
			// Act
			final String value = Configuration.getProperty(MISSING_KEY);
			// Assert
			Assert.assertEquals("property", value);
		} finally {
			// Clean-Up
			System.clearProperty(MISSING_KEY);
		}
	}

	@Test
	public void existingProperyWithoutDefaultValue() {
		// Act
		final String value = Configuration.getProperty(EXISTING_KEY);
		// Assert
		Assert.assertEquals("value", value);
	}
	
	@Test
	public void existingProperyWithDefaultValue() {
		// Act
		final String value = Configuration.getProperty(EXISTING_KEY, "default");
		// Assert
		Assert.assertEquals("value", value);
	}

	@Test
	public void existingProperyWithSystemProperty() {
		// Arrange
		System.setProperty(EXISTING_KEY,"property");
		try {
			// Act
			final String value = Configuration.getProperty(EXISTING_KEY);
			// Assert
			Assert.assertEquals("property", value);
		} finally {
			// Clean-Up
			System.clearProperty(EXISTING_KEY);
		}
	}
	
	
}
