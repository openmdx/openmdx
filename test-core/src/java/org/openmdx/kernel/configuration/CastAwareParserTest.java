/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Cast-Aware Parser Test
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

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;
import org.openmdx.kernel.configuration.CastAwareParser;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.text.spi.Parser;
import org.w3c.spi.PrimitiveTypeParsers;

/**
 * Cast-Aware Parser Test
 */
public class CastAwareParserTest {

	@Test
	public void whenNoCastIsSpecifiedThenParseAsValueClass(){
		// Arrange
		final String expression = "1.5";
		final Parser testee = newCastAwareParser();
		// Act
		final BigDecimal value = testee.parse(BigDecimal.class, expression);
		// Assert
		Assert.assertEquals(new BigDecimal(expression), value);
	}	

	@Test
	public void whenExactCastIsSpecifiedThenParseAsValueClass(){
		// Arrange
		final String expression = "1.5";
		final Parser testee = newCastAwareParser();
		// Act
		final BigDecimal value = testee.parse(
			BigDecimal.class, 
			"(" + BigDecimal.class.getName() + ")" + expression
		);
		// Assert
		Assert.assertEquals(new BigDecimal(expression), value);
	}	

	@Test
	public void whenNoValueClassIsSpecifiedThenHandlesIsTrue(){
		// Arrange
		final Parser testee = newCastAwareParser();
		// Act
		final boolean handles = testee.handles(null);
		// Assert
		Assert.assertTrue(handles);
	}	

	@Test
	public void whenNeitherValueClassNorCastIsSpecifiedThenReturnRawValue(){
		// Arrange
		final Parser testee = newCastAwareParser();
		// Act
		final Object value = testee.parse(null, "1.5");
		// Assert
		Assert.assertEquals("1.5", value);
	}	

	@Test
	public void whenNoValueClassIsSpecifiedThanParseAsCastType(){
		// Arrange
		final Parser testee = newCastAwareParser();
		// Act
		final Object value = testee.parse(null, "(java.math.BigDecimal)1.5");
		// Assert
		Assert.assertEquals(new BigDecimal("1.5"), value);
	}	

	@Test
	public void whenValueClassAndCastThanParseAsCastType(){
		// Arrange
		final Parser testee = newCastAwareParser();
		// Act
		final Object value = testee.parse(java.lang.Number.class, "(java.math.BigDecimal)1.5");
		// Assert
		Assert.assertEquals(new BigDecimal("1.5"), value);
	}	

	@Test
	public void whenValueClassIsIncompatibleToCastThanThrowException(){
		// Arrange
		final Parser testee = newCastAwareParser();
		try {
			// Act
			testee.parse(java.lang.Integer.class, "(java.math.BigDecimal)1.5");
			Assert.fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException expected) {
			// Assert
			final BasicException cause = Throwables.getCause(expected, null);
			Assert.assertEquals(ClassCastException.class.getName(), cause.getExceptionClass());
		}
	}	

	@Test
	public void whenValueIsNotParsableThanThrowException(){
		// Arrange
		final Parser testee = newCastAwareParser();
		try {
			// Act
			testee.parse(java.lang.Integer.class, "(java.math.BigDecimal)1;5");
			Assert.fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException expected) {
			// Assert
			final BasicException cause = Throwables.getCause(expected, null);
			Assert.assertEquals(NumberFormatException.class.getName(), cause.getExceptionClass());
		}
	}	
	
	@Test
	public void whenStartsWithCastThenCastedIsTrue(){
		// Arrange
		final String expression = "(java.lang.Integer)5";
		// Act
		final boolean casted = isCasted(expression);
		// Assert
		Assert.assertTrue(casted);
	}	

	@Test
	public void whenParenthesisDoNotStartAtTheVeryBeginningThenCastedIsFalse(){
		// Arrange
		final String expression = "type=(java.lang.Integer)";
		// Act
		final boolean casted = isCasted(expression);
		// Assert
		Assert.assertFalse(casted);
	}	

	@Test
	public void whenIsEmptyThenCastedIsFalse(){
		// Arrange
		final String expression = "()aString";
		// Act
		final boolean casted = isCasted(expression);
		// Assert
		Assert.assertFalse(casted);
	}	
	
	boolean isCasted(String expression) {
		return CastAwareParser.CASTED_VALUE_PATTERN.matcher(expression).matches();
	}

	private static CastAwareParser newCastAwareParser(){
		return new CastAwareParser(PrimitiveTypeParsers.getExtendedParser());
	}
	
}
