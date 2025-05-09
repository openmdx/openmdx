/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Immutable Primitive Type Parser test
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
package org.openmdx.base.text.parsing;

import java.text.ParseException;
import java.util.Date;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.text.spi.Parser;
import org.w3c.spi.DatatypeFactories;
import org.w3c.spi2.Datatypes;

/**
 * Immutable Primitive Type Parser Test
 */
public class ChronoTypeParserTest {

	/**
	 * Duration of 6 Months
	 */
	@Test
	public void whenHalfAYearThenParseAsDuration(){
		// Arrange
		final Parser testee = ChronoTypeParser.getInstance();
		// Act
		final Object value = testee.parse(Datatypes.DURATION_CLASS, "P6M");
		// Assert
		Assertions.assertEquals(DatatypeFactories.immutableDatatypeFactory().newDuration("P6M"), value);
	}

	/**
	 * Timepoint 01.04.2000 05:06:07.890 UTC
	 */
	@Test
	public void whenDateTimeThenParseAsDate(){
		// Arrange
		final Parser testee = ChronoTypeParser.getInstance();
		// Act
		final Object value = testee.parse(Datatypes.DATE_TIME_CLASS, "2000-04-01T05:06:07.890Z");
		// Assert
		Assertions.assertEquals(DatatypeFactories.immutableDatatypeFactory().newDateTime("20000401T050607.890Z"), value);
	}

	@Test
	public void whenRemovedAtInTheFutureThenParseAsDate(){
		// Arrange
		final Parser testee = ChronoTypeParser.getInstance();
		// Act
		final Object value = testee.parse(Datatypes.DATE_TIME_CLASS, "+10000-01-01T00:00:00Z");
		// Assert
		Assertions.assertEquals(DatatypeFactories.immutableDatatypeFactory().newDateTime("100000101T000000Z"), value);
	}

	/**
	 * Date 01.04.2000
	 */
	public void whenDateThenParseAsXMLGregorianCalendar(){
		// Arrange
		final Parser testee = ChronoTypeParser.getInstance();
		// Act
		final Object value = testee.parse(Datatypes.DATE_CLASS, "2000-04-01");
		// Assert
		Assertions.assertEquals(DatatypeFactories.immutableDatatypeFactory().newDate("20000401"), value);
	}

	@Test
	public void whenValueClassIsObjectThenThrowParseException(){
		// Arrange
		final Parser testee = ChronoTypeParser.getInstance();
		try {
			// Act
			testee.parse(Object.class, "null");
			Assertions.fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException expected) {
			// Assert
			final BasicException cause = Throwables.getCause(expected, null);
			Assertions.assertEquals(ParseException.class.getName(), cause.getExceptionClass());
		}
	}

	@Test
	public void whenValueClassIsNullThenKeepString(){
		// Arrange
		final Parser testee = ChronoTypeParser.getInstance();
		// Act
		final Object value = testee.parse(null, "null");
		// Assert
		Assertions.assertEquals("null", value);
	}

	@Test
	public void whenValueIsNullThenReturnNull(){
		// Arrange
		final Parser testee = ChronoTypeParser.getInstance();
		// Act
		final Object value = testee.parse(Integer.class, null);
		// Assert
		Assertions.assertNull(value);
	}

	@Test
	public void whenValueAndValueClassAreNullThenReturnNull(){
		// Arrange
		final Parser testee = ChronoTypeParser.getInstance();
		// Act
		final Object value = testee.parse(null, null);
		// Assert
		Assertions.assertNull(value);
	}

}
