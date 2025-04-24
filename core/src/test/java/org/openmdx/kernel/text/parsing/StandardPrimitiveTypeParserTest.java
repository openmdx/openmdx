/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Standard Primitive Type Parser Test 
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
package org.openmdx.kernel.text.parsing;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.base.naming.Path;
import org.openmdx.base.text.parsing.ChronoTypeParser;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.text.spi.Parser;
import org.w3c.spi2.Datatypes;

/**
 * Standard Primitive Type Parser Test 
 */
public class StandardPrimitiveTypeParserTest {

	/**
	 * org::openmdx::state2
	 */
	@Test
	public void whenStateAuthorityThenParseAsPath(){
		// Arrange
		final Parser testee = StandardPrimitiveTypeParser.getInstance();
		// Act
		final Path value = testee.parse(Path.class, "xri://@openmdx*org.openmdx.state2");
		// Assert
		Assertions.assertEquals(new Path("org::openmdx::state2"), value);
	}

	@Test
	public void whenTrueThenParseAsBoolean(){
		// Arrange
		final Parser testee = StandardPrimitiveTypeParser.getInstance(); 
		// Act
		final Boolean value = testee.parse(Boolean.class, "True");
		// Assert
		Assertions.assertEquals(Boolean.TRUE, value);
	}
	
	@Test
	public void when5ThenParseAsInteger(){
		// Arrange
		final Parser testee = StandardPrimitiveTypeParser.getInstance(); 
		// Act
		final Integer value = testee.parse(Integer.class, "5");
		// Assert
		Assertions.assertEquals(Integer.valueOf(5), value);
	}

	@Test
	public void whenMinus128ThenParseAsByte(){
		// Arrange
		final Parser testee = StandardPrimitiveTypeParser.getInstance(); 
		// Act
		final Byte value = testee.parse(Byte.class, "-128");
		// Assert
		Assertions.assertEquals(Byte.valueOf((byte)-128), value);
	}

	@Test
	public void whenBarThenParseAsString(){
		// Arrange
		final Parser testee = StandardPrimitiveTypeParser.getInstance(); 
		// Act
		final String value = testee.parse(String.class, "Bar");
		// Assert
		Assertions.assertEquals("Bar", value);
	}
	
	@Test
	public void when32767ThenParseAsShort(){
		// Arrange
		final Parser testee = StandardPrimitiveTypeParser.getInstance(); 
		// Act
		final Short value = testee.parse(Short.class, "32767");
		// Assert
		Assertions.assertEquals(Short.valueOf((short)32767), value);
	}
	
	@Test
	public void whenMinus0ThenParseAsLong(){
		// Arrange
		final Parser testee = StandardPrimitiveTypeParser.getInstance(); 
		// Act
		final Long value = testee.parse(Long.class, "-0");
		// Assert
		Assertions.assertEquals(Long.valueOf(0), value);
	}

	@Test
	public void whenAHalfThenParseAsBigDecimal(){
		// Arrange
		final Parser testee = StandardPrimitiveTypeParser.getInstance(); 
		// Act
		final BigDecimal value = testee.parse(BigDecimal.class, "0.5");
		// Assert
		Assertions.assertEquals(new BigDecimal("0.5"), value);
	}

	/**
	 * 1'000'000'000'000
	 */
	@Test
	public void whenATrillionThenParseAsBigInteger(){
		// Arrange
		final Parser testee = StandardPrimitiveTypeParser.getInstance(); 
		// Act
		final BigInteger value = testee.parse(BigInteger.class, "1000000000000");
		// Assert
		Assertions.assertEquals(new BigInteger("1000000000000"), value);
	}
	
	@Test
	public void whenNullThenParseAsOid(){
		// Arrange
		final Parser testee = StandardPrimitiveTypeParser.getInstance(); 
		// Act
		final Oid value = testee.parse(Oid.class, null);
		// Assert
		Assertions.assertNull(value);
	}

	/**
	 * UUID d7f859e9-a493-4d50-8ec1-b3d090e623e1
	 */
	public void whenUUIDThenParseAsOid() throws GSSException{
		// Arrange
		final Parser testee = StandardPrimitiveTypeParser.getInstance(); 
		// Act
		final Oid value = testee.parse(Oid.class, "2.25.287073532360223759562881821002273530849");
		// Assert
		Assertions.assertEquals(new Oid("2.25.287073532360223759562881821002273530849"), value);
	}

	/**
	 * Timepoint 2000-04-01T00:00:00.000Z
	 */
	@Test
	public void whenValueClassIsDateThenThrowParseException(){
		// Arrange
		final Parser testee = StandardPrimitiveTypeParser.getInstance(); 
		try {
			// Act
			testee.parse(Datatypes.DATE_TIME_CLASS, "20000401T000000.000Z");
			Assertions.fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException expected) {
			// Assert
			final BasicException cause = Throwables.getCause(expected, null);
			Assertions.assertEquals(ParseException.class.getName(), cause.getExceptionClass());
		}
	}
	
}
