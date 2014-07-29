/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Test XMLGregorianCalendar Marshaller 
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
 * OR TORT (INCLUDING NEGLIGENCE OR OTHJavaUpperERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package test.openmdx.application.dataprovider.layer.persistence.jdbc;

import java.sql.Connection;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmdx.application.dataprovider.layer.persistence.jdbc.DataTypes;
import org.openmdx.application.dataprovider.layer.persistence.jdbc.LayerConfigurationEntries;
import org.openmdx.application.dataprovider.layer.persistence.jdbc.XMLGregorianCalendarMarshaller;
import org.openmdx.base.exception.ServiceException;
import org.w3c.spi.DatatypeFactories;

public class TestXMLGregorianCalendarMarshaller {

	private static XMLGregorianCalendarMarshaller marshaller;
	
	@BeforeClass
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
      final XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(lexicalRepresentation);
      // Act
      final Object sqlValue = marshaller.marshal(xmlGregorianCalendar, null);
      // Assert
      Assert.assertEquals("dateTime", "2013-10-26 02:30:00.000000 Europe/Zurich CEST", sqlValue);
   }

   @Test
   public void whenInLastHourOfCentralEuropeanSummerTimeThenInDaylightSavingsTime() throws ParseException, ServiceException {
      // Arrange
      final String lexicalRepresentation = "2013-10-27T00:30:00.000Z";
      final XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(lexicalRepresentation);
      // Act
      final Object sqlValue = marshaller.marshal(xmlGregorianCalendar, null);
      // Assert
      Assert.assertEquals("dateTime", "2013-10-27 02:30:00.000000 Europe/Zurich CEST", sqlValue);
   }

   @Test
   public void whenInFirstHourOfCentralEuropeanTimeThenNotInDaylightSavingsTime() throws ParseException, ServiceException {
      // Arrange
      final String lexicalRepresentation = "2013-10-27T01:30:00.000Z";
      final XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(lexicalRepresentation);
      // Act
      final Object sqlValue = marshaller.marshal(xmlGregorianCalendar, null);
      // Assert
      Assert.assertEquals("dateTime", "2013-10-27 02:30:00.000000 Europe/Zurich", sqlValue);
   }

   @Test
   public void whenInCentralEuropeanTimeThenNotInDaylightSavingsTime() throws ParseException, ServiceException {
      // Arrange
      final String lexicalRepresentation = "2013-10-28T01:30:00.000Z";
      final XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar(lexicalRepresentation);
      // Act
      final Object sqlValue = marshaller.marshal(xmlGregorianCalendar, null);
      // Assert
      Assert.assertEquals("dateTime", "2013-10-28 02:30:00.000000 Europe/Zurich", sqlValue);
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
		
	}
	
}
