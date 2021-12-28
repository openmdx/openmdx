/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Embedded Flags Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013-2021, OMEX AG, Switzerland
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
package test.openmdx.application.dataprovider.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.openmdx.base.query.spi.EmbeddedFlags;
import org.w3c.cci2.RegularExpressionFlag;

public class TestEmbeddedFlags {

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\\\.[A-Za-z0-9]+)*(\\\\.[A-Za-z]{2,})$";
	
	@Test
	public void whenNoFlagThenNoFLag(){
		// Arrange
		final String value = EMAIL_PATTERN; 
		EmbeddedFlags testee = EmbeddedFlags.getInstance();
		// Act
		final String amendedValue = testee.embedFlags(EnumSet.noneOf(RegularExpressionFlag.class), value);
		// Assert
		assertEquals(value, amendedValue, "No embedded flags");
	}

	@Test
	public void whenNoEmbeddedFlagAndIgnoreCaseFlagThenPrependEmbeddedIgnoreCaseFlag(){
		// Arrange
		final String value = EMAIL_PATTERN; 
		EmbeddedFlags testee = EmbeddedFlags.getInstance();
		// Act
		final String amendedValue = testee.embedFlags(EnumSet.of(RegularExpressionFlag.CASE_INSENSITIVE), value);
		// Assert
		assertEquals("(?i)" + EMAIL_PATTERN, amendedValue, "Embedded ignore case flag inserted");
	}

	@Test
	public void whenEmbeddedIgnoreCaseFlagAndUnicodeCaseFlagThenIncludeEmbeddedUnicodeCaseFlag(){
		// Arrange
		final String value = "(?i)" + EMAIL_PATTERN; 
		EmbeddedFlags testee = EmbeddedFlags.getInstance();
		// Act
		final String amendedValue = testee.embedFlags(EnumSet.of(RegularExpressionFlag.UNICODE_CASE), value);
		// Assert
		assertEquals("(?iu)" + EMAIL_PATTERN, amendedValue, "Embedded unicode case flag inserted");
	}
	
	@Test
	public void whenLiteralFlagThenQuoted(){
		// Arrange
		final String value = EMAIL_PATTERN; 
		EmbeddedFlags testee = EmbeddedFlags.getInstance();
		// Act
		final String amendedValue = testee.embedFlags(EnumSet.of(RegularExpressionFlag.LITERAL), value);
		// Assert
		assertEquals(
				"\\Q" + EMAIL_PATTERN + "\\E", 
				amendedValue,
				"Meta characters escaped" 
		);
	}

	@Test
	public void whenLiteralFlagThenMatch(){
		// Arrange
		final String value = EMAIL_PATTERN; 
		EmbeddedFlags testee = EmbeddedFlags.getInstance();
		// Act
		final String amendedValue = testee.embedFlags(EnumSet.of(RegularExpressionFlag.LITERAL), value);
		final Pattern literalPattern = Pattern.compile(amendedValue);
		// Assert
		assertTrue(literalPattern.matcher(value).matches(), "Literal match");
	}
	
	
	@Test
	public void whenEmbeddedUniceCaseFlagAndCaseInsensitiveFlagThenUnicodeCaseFlagAndCaseInsensitiveFlag(){
		// Arrange
		final String valueWithEmbeddedFlags = "(?iu)" + EMAIL_PATTERN; 
		EmbeddedFlags testee = EmbeddedFlags.getInstance();
		// Act
		final EmbeddedFlags.FlagsAndValue flagsAndValue = testee.parse(valueWithEmbeddedFlags);
		// Assert
		assertEquals(EnumSet.of(RegularExpressionFlag.UNICODE_CASE, RegularExpressionFlag.CASE_INSENSITIVE), flagsAndValue.getFlagSet());
		assertEquals(EMAIL_PATTERN, flagsAndValue.getValue());
	}
	
	@Test
	public void whenLiteralAndPosixThenQuoteAndEmbeddedPosix(){
		// Arrange
		final String value = EMAIL_PATTERN; 
		EmbeddedFlags testee = EmbeddedFlags.getInstance();
		// Act
		final String amendedValue = testee.embedFlags(EnumSet.of(RegularExpressionFlag.LITERAL, RegularExpressionFlag.POSIX_EXPRESSION), value);
		// Assert
		assertEquals(
				"(?P)\\Q" + EMAIL_PATTERN + "\\E", 
				amendedValue,
				"Meta characters escaped" 
		);
		
	}

	@Test
	public void whenNoEmbeddedFlagThenNoFlag(){
		// Arrange
		final String valueWithEmbeddedFlags = EMAIL_PATTERN; 
		EmbeddedFlags testee = EmbeddedFlags.getInstance();
		// Act
		final EmbeddedFlags.FlagsAndValue flagsAndValue = testee.parse(valueWithEmbeddedFlags);
		// Assert
		assertEquals(EnumSet.noneOf(RegularExpressionFlag.class), flagsAndValue.getFlagSet());
		assertEquals(EMAIL_PATTERN, flagsAndValue.getValue());
	}

}
