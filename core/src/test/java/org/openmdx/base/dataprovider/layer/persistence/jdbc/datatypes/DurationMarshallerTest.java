/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Duration Marshaller Test 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2021, OMEX AG, Switzerland
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
package org.openmdx.base.dataprovider.layer.persistence.jdbc.datatypes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;


public class DurationMarshallerTest {

	@Test
	public void postgresZero() throws ServiceException {
		// Arrange
		DurationMarshaller testee = DurationMarshaller.newInstance("INTERVAL");
		// Act
		Object duration = testee.unmarshal("0 years 0 mons 0 days 0 hours 0 mins 0.0 secs");
		// Assert
		assertEquals("P0D", duration);
	}
	
	@Test
	public void postgresPositiveDays() throws ServiceException {
		// Arrange
		DurationMarshaller testee = DurationMarshaller.newInstance("INTERVAL");
		// Act
		Object duration = testee.unmarshal("0 years 0 mons 30 days 0 hours 0 mins 0.0 secs");
		// Assert
		assertEquals("P30D", duration);
	}

	@Test
	public void postgresPositiveMonths() throws ServiceException {
		// Arrange
		DurationMarshaller testee = DurationMarshaller.newInstance("INTERVAL");
		// Act
		Object duration = testee.unmarshal("0 years 14 mons 0 days 0 hours 0 mins 0.0 secs");
		// Assert
		assertEquals("P1Y2M", duration);
	}

	@Test
	public void postgresPositiveTime() throws ServiceException {
		// Arrange
		DurationMarshaller testee = DurationMarshaller.newInstance("INTERVAL");
		// Act
		Object duration = testee.unmarshal("0 years 0 mons 0 days 0 hours 1 mins 2.5 secs");
		// Assert
		assertEquals("PT1M2.5S", duration);
	}

	@Test
	public void postgresNegativeTime() throws ServiceException {
		// Arrange
		DurationMarshaller testee = DurationMarshaller.newInstance("INTERVAL");
		// Act
		Object duration = testee.unmarshal("0 years 0 mons 0 days 0 hours -1 mins -2.5 secs");
		// Assert
		assertEquals("-PT1M2.5S", duration);
	}

	@Test
	public void postgresPositiveMonthTime() throws ServiceException {
		// Arrange
		DurationMarshaller testee = DurationMarshaller.newInstance("INTERVAL");
		// Act
		Object duration = testee.unmarshal("0 years 1 mons 0 days 0 hours 2 mins 3.5 secs");
		// Assert
		assertEquals("P1MT2M3.5S", duration);
	}

	@Test
	public void postgresNegativeMonthTime() throws ServiceException {
		// Arrange
		DurationMarshaller testee = DurationMarshaller.newInstance("INTERVAL");
		// Act
		Object duration = testee.unmarshal("0 years -1 mons 0 days 0 hours -2 mins -3.5 secs");
		// Assert
		assertEquals("-P1MT2M3.5S", duration);
	}

	@Disabled("PostgreSQL marshalling must be fixed first")
	@Test
	public void postgresPositiveYearNegativeTime() throws ServiceException {
		// Arrange
		DurationMarshaller testee = DurationMarshaller.newInstance("INTERVAL");
		// Act
		try {
			testee.unmarshal("1 years 0 mons 0 days 0 hours -2 mins -3.5 secs");
			Assertions.fail();
		} catch (ServiceException expected) {
			Assertions.assertEquals(BasicException.Code.TRANSFORMATION_FAILURE, expected.getExceptionCode());
		}
	}
	
	private void assertEquals(
		String expected, Object actual	
    ) throws ServiceException {
		DurationMarshaller formatter = DurationMarshaller.newInstance("CHARACTER");
		Assertions.assertEquals(expected, formatter.unmarshal(actual).toString());
	}
	
}
