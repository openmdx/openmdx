/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Date Time Values Test 
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

package org.openmdx.base.dataprovider.layer.persistence.jdbc;

import java.util.GregorianCalendar;

import #if JAVA_8 javax.resource.ResourceException #else jakarta.resource.ResourceException #endif;
import #if JAVA_8 javax.resource.cci.IndexedRecord #else jakarta.resource.cci.IndexedRecord #endif;
import #if JAVA_8 javax.resource.cci.MappedRecord #else jakarta.resource.cci.MappedRecord #endif;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.state2.cci.DateStateViews;
#if CLASSIC_CHRONO_TYPES import org.w3c.spi.ImmutableDatatypeFactory;#endif
import org.w3c.spi2.Datatypes;

/**
 * Date Time Values Test
 */
public class DateTimeValuesTest {

    private ObjectRecord object;
    
    @BeforeEach
    public void setUp() throws ResourceException{
        object = Records.getRecordFactory().createMappedRecord(ObjectRecord.class);
        object.setValue(Records.getRecordFactory().createMappedRecord("org:openmdx:base:dataprovider:layer:persistence:jdbc:TestData"));
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void whenDateThenLeaveXMLGregorianCalendar() throws ResourceException{
        // Arrange
        final XMLGregorianCalendar today = DateStateViews.today();
        object.getValue().put("today", today);
        // Act
        DateTimeValues.normalizeDateTimeValues(object);
        // Assert
        Assertions.assertSame(today, object.getValue().get("today"));
    }

    @SuppressWarnings("unchecked")
	@Test
    public void whenNowThenConvertToDate() throws ResourceException{
        // Arrange
        final #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif now = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar((GregorianCalendar) GregorianCalendar.getInstance());
        object.getValue().put("now", now);
        // Act
        DateTimeValues.normalizeDateTimeValues(object);
        // Assert
        Assertions.assertTrue(Datatypes.DATE_TIME_CLASS.isInstance(object.getValue().get("now")));
    }

    @SuppressWarnings("unchecked")
	@Test
    public void convertInListWhereNecessary() throws ResourceException{
        // Arrange
        final #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif today = DateStateViews.today();
        final #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif now = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar((GregorianCalendar) GregorianCalendar.getInstance());
        final IndexedRecord list = Records.getRecordFactory().createIndexedRecord(Multiplicity.LIST.code());
        list.add(today);
        list.add(now);
        object.getValue().put("list", list);
        // Act
        DateTimeValues.normalizeDateTimeValues(object);
        // Assert
        Assertions.assertTrue(Datatypes.DATE_CLASS.isInstance(list.get(0)));
        Assertions.assertTrue(Datatypes.DATE_TIME_CLASS.isInstance(list.get(1)));
    }

    @SuppressWarnings("unchecked")
	@Test
    public void convertInMapWhereNecessary() throws ResourceException{
        // Arrange
        final #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif today = DateStateViews.today();
        final #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif now = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar((GregorianCalendar) GregorianCalendar.getInstance());
        final MappedRecord map = Records.getRecordFactory().createMappedRecord(Multiplicity.SPARSEARRAY.code());
        map.put(Integer.valueOf(4), now);
        map.put(Integer.valueOf(2), today);
        object.getValue().put("map", map);
        // Act
        DateTimeValues.normalizeDateTimeValues(object);
        // Assert
        Assertions.assertTrue(Datatypes.DATE_CLASS.isInstance(map.get(2)));
        Assertions.assertTrue(Datatypes.DATE_TIME_CLASS.isInstance(map.get(4)));
    }
        
}
