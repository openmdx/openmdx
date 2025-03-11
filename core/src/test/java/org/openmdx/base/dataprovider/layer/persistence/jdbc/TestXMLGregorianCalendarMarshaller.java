/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: XMLGregorianCalendar Marshaller Test
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
 * OR TORT (INCLUDING NEGLIGENCE OR OTHJavaUpperERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.dataprovider.layer.persistence.jdbc;

import java.sql.Connection;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;

import #if CLASSIC_CHRONO_TYPES javax.xml.datatype #else java.time #endif.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.datatypes.XMLGregorianCalendarMarshaller;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.DataTypes;
import org.openmdx.base.exception.ServiceException;

public class TestXMLGregorianCalendarMarshaller {

	private static XMLGregorianCalendarMarshaller marshaller;
	
	@BeforeAll
	public static void setUp() throws ServiceException{
		marshaller = XMLGregorianCalendarMarshaller.newInstance(
			LayerConfigurationEntries.TIME_TYPE_STANDARD,
			LayerConfigurationEntries.DATE_TYPE_STANDARD,
			LayerConfigurationEntries.DATETIME_TYPE_STANDARD,
			"Europe/Zurich",
			"Europe/Zurich CEST",
			TimeUnit.MICROSECONDS.name(),
			new CharacterDataTypes()
        );
	}	

   @Test
   public void whenInCentralEuropeanSummerTimeThenInDaylightSavingsTime() throws ParseException, ServiceException {
      // Arrange
      final String lexicalRepresentation = "2013-10-26T00:30:00.000Z";
      final #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif xmlGregorianCalendar = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(lexicalRepresentation);
      // Act
      final Object sqlValue = marshaller.marshal(xmlGregorianCalendar, null);
      // Assert
      Assertions.assertEquals("2013-10-26 02:30:00.000000 Europe/Zurich CEST", sqlValue, "dateTime");
   }

   @Test
   public void whenInLastHourOfCentralEuropeanSummerTimeThenInDaylightSavingsTime() throws ParseException, ServiceException {
      // Arrange
      final String lexicalRepresentation = "2013-10-27T00:30:00.000Z";
      final #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif xmlGregorianCalendar = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(lexicalRepresentation);
      // Act
      final Object sqlValue = marshaller.marshal(xmlGregorianCalendar, null);
      // Assert
      Assertions.assertEquals("2013-10-27 02:30:00.000000 Europe/Zurich CEST", sqlValue, "dateTime");
   }

   @Test
   public void whenInFirstHourOfCentralEuropeanTimeThenNotInDaylightSavingsTime() throws ParseException, ServiceException {
      // Arrange
      final String lexicalRepresentation = "2013-10-27T01:30:00.000Z";
      final #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif xmlGregorianCalendar = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(lexicalRepresentation);
      // Act
      final Object sqlValue = marshaller.marshal(xmlGregorianCalendar, null);
      // Assert
      Assertions.assertEquals("2013-10-27 02:30:00.000000 Europe/Zurich", sqlValue, "dateTime");
   }

   @Test
   public void whenInCentralEuropeanTimeThenNotInDaylightSavingsTime() throws ParseException, ServiceException {
      // Arrange
      final String lexicalRepresentation = "2013-10-28T01:30:00.000Z";
      final #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif xmlGregorianCalendar = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(lexicalRepresentation);
      // Act
      final Object sqlValue = marshaller.marshal(xmlGregorianCalendar, null);
      // Assert
      Assertions.assertEquals("2013-10-28 02:30:00.000000 Europe/Zurich", sqlValue, "dateTime");
   }
   
	static class CharacterDataTypes implements DataTypes {

		@Override
		public String getDateTimeType(
			Connection connection
		) throws ServiceException {
			return LayerConfigurationEntries.DATETIME_TYPE_TIMESTAMP_WITH_TIMEZONE;
		}

		@Override
		public String getDateType(
			Connection connection
		) throws ServiceException {
			return LayerConfigurationEntries.DATE_TYPE_CHARACTER;
		}

		@Override
		public String getTimeType(
			Connection connection
		) throws ServiceException {
			return LayerConfigurationEntries.TIME_TYPE_CHARACTER;
		}

		@Override
		public String getBooleanType(
			Connection connection
		) throws ServiceException {
			return LayerConfigurationEntries.BOOLEAN_TYPE_CHARACTER;
		}

		@Override
		public String getDurationType(Connection connection) throws ServiceException {
			return LayerConfigurationEntries.DURATION_TYPE_CHARACTER;
		}
		
	}
	
}
