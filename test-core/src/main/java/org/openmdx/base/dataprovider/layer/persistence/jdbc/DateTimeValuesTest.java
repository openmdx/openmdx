/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Date Time Values Test 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2018, OMEX AG, Switzerland
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

package org.openmdx.base.dataprovider.layer.persistence.jdbc;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.state2.cci.DateStateViews;
import org.w3c.spi.DatatypeFactories;

/**
 * Date Time Values Test
 */
public class DateTimeValuesTest {

    private ObjectRecord object;
    
    @Before
    public void setUp() throws ResourceException{
        object = Records.getRecordFactory().createMappedRecord(ObjectRecord.class);
        object.setValue(Records.getRecordFactory().createMappedRecord("org:openmdx:base:dataprovider:layer:persistence:jdbc:TestData"));
    }
    
    @Test
    public void whenDateThenLeaveXMLGregorianCalendar() throws ResourceException{
        // Arrange
        final XMLGregorianCalendar today = DateStateViews.today();
        object.getValue().put("today", today);
        // Act
        DateTimeValues.normalizeDateTimeValues(object);
        // Assert
        Assert.assertSame(today, object.getValue().get("today"));
    }

    @Test
    public void whenNowThenConvertToDate() throws ResourceException{
        // Arrange
        final XMLGregorianCalendar now = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar((GregorianCalendar) GregorianCalendar.getInstance());
        object.getValue().put("now", now);
        // Act
        DateTimeValues.normalizeDateTimeValues(object);
        // Assert
        Assert.assertTrue(object.getValue().get("now") instanceof Date);
    }

    @Test
    public void convertInListWhereNecessary() throws ResourceException{
        // Arrange
        final XMLGregorianCalendar today = DateStateViews.today();
        final XMLGregorianCalendar now = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar((GregorianCalendar) GregorianCalendar.getInstance());
        final IndexedRecord list = Records.getRecordFactory().createIndexedRecord(Multiplicity.LIST.code());
        list.add(today);
        list.add(now);
        object.getValue().put("list", list);
        // Act
        DateTimeValues.normalizeDateTimeValues(object);
        // Assert
        Assert.assertTrue(list.get(0) instanceof XMLGregorianCalendar);
        Assert.assertTrue(list.get(1) instanceof Date);
    }

    @Test
    public void convertInMapWhereNecessary() throws ResourceException{
        // Arrange
        final XMLGregorianCalendar today = DateStateViews.today();
        final XMLGregorianCalendar now = DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar((GregorianCalendar) GregorianCalendar.getInstance());
        final MappedRecord map = Records.getRecordFactory().createMappedRecord(Multiplicity.SPARSEARRAY.code());
        map.put(Integer.valueOf(4), now);
        map.put(Integer.valueOf(2), today);
        object.getValue().put("map", map);
        // Act
        DateTimeValues.normalizeDateTimeValues(object);
        // Assert
        Assert.assertTrue(map.get(2) instanceof XMLGregorianCalendar);
        Assert.assertTrue(map.get(4) instanceof Date);
    }
        
}
