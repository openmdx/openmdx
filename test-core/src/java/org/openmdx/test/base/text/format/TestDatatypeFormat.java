/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestDatatypeFormat.java,v 1.1 2006/06/20 17:05:38 hburger Exp $
 * Description: JUnit testing the DatatypeFormat class
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/06/20 17:05:38 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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
package org.openmdx.test.base.text.format;

import java.math.BigDecimal;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DatatypeFormat;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * JUnit testing the <code>DatatypeFormat</code> class
 */
public class TestDatatypeFormat extends TestCase {
    
    /**
     * Constructor 
     *
     * @param name
     */
    public TestDatatypeFormat(
        String name
    ) {
        super(name);
    }  

    /**
     * Main
     * 
     * @param args
     */
    public static void main(
        String[] args
    ){
        TestRunner.run(TestDatatypeFormat.class);
    }

    
    protected DatatypeFormat datatypeFormat;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp(
    ) throws Exception {
        this.datatypeFormat = DatatypeFormat.newInstance("testBasic".equals(getName()));
    }

    public void testBasic(
    ) throws ServiceException{
        date("20060401");
        dateTime("20060401T120000.000Z");
        yearMonthDuration("P99Y11M");
        timeDuration("-P1DT0H0M0.000S");
    }

    public void testExtended(
    ) throws ServiceException{
        date("2006-04-01");
        dateTime("2006-04-01T12:00:00.000Z");
        yearMonthDuration("P99Y11M");
        timeDuration("-P1DT0H0M0.000S");
    }

    /**
     * To be found by reflection...
     * @param value TODO
     * @throws ServiceException 
     */
    public void date(String value) throws ServiceException{
        XMLGregorianCalendar beAwareOfJokes = this.datatypeFormat.parseDate(value);
        assertEquals("year", 2006, beAwareOfJokes.getYear());
        assertEquals("month", 4, beAwareOfJokes.getMonth());
        assertEquals("day", 1, beAwareOfJokes.getDay());
        assertEquals("hour", DatatypeConstants.FIELD_UNDEFINED, beAwareOfJokes.getHour());
        assertEquals("minute", DatatypeConstants.FIELD_UNDEFINED, beAwareOfJokes.getMinute());
        assertNull("fractionalSecond", beAwareOfJokes.getFractionalSecond());
        assertEquals("timezone", DatatypeConstants.FIELD_UNDEFINED, beAwareOfJokes.getTimezone());
        assertEquals("marshal", beAwareOfJokes, this.datatypeFormat.marshal(value));
        assertEquals("unmarshal", value, this.datatypeFormat.unmarshal(beAwareOfJokes));
    }

    /**
     * To be found by reflection...
     * @param value TODO
     * @throws ServiceException 
     */
    public void dateTime(String value) throws ServiceException{
        XMLGregorianCalendar highNoon = this.datatypeFormat.parseDateTime(value);
        assertEquals("year", 2006, highNoon.getYear());
        assertEquals("month", 4, highNoon.getMonth());
        assertEquals("day", 1, highNoon.getDay());
        assertEquals("hour", 12, highNoon.getHour());
        assertEquals("minute", 00, highNoon.getMinute());
        assertEquals("fractionalSecond", BigDecimal.valueOf(0, 3), highNoon.getFractionalSecond());
        assertEquals("timezone", 0, highNoon.getTimezone());
        assertEquals("marshal", highNoon, this.datatypeFormat.marshal(value));
        assertEquals("unmarshal", value, this.datatypeFormat.unmarshal(highNoon));
    }

    public void yearMonthDuration(String value) throws ServiceException{
        Duration nearlyACentury = this.datatypeFormat.parseDuration(value);
        assertEquals("sign", 1, nearlyACentury.getSign());
        assertEquals("years", 99, nearlyACentury.getYears());
        assertEquals("months", 11, nearlyACentury.getMonths());
        assertEquals("days", 0, nearlyACentury.getDays());
        assertEquals("hours", 0, nearlyACentury.getHours());
        assertEquals("minutes", 0, nearlyACentury.getMinutes());
        assertNull("seconds", nearlyACentury.getField(DatatypeConstants.SECONDS));
        assertEquals("type", DatatypeConstants.DURATION_YEARMONTH, nearlyACentury.getXMLSchemaType());
        assertEquals("marshal", nearlyACentury, this.datatypeFormat.marshal(value));
        assertEquals("unmarshal", value, this.datatypeFormat.unmarshal(nearlyACentury));
    }

    public void timeDuration(String value) throws ServiceException{
        Duration yesterday = this.datatypeFormat.parseDuration(value);
        assertEquals("sign", -1, yesterday.getSign());
        assertEquals("years", 0, yesterday.getYears());
        assertEquals("months", 0, yesterday.getMonths());
        assertEquals("days", 1, yesterday.getDays());
        assertEquals("hours", 0, yesterday.getHours());
        assertEquals("minutes", 0, yesterday.getMinutes());
        assertEquals("seconds", BigDecimal.valueOf(0, 3), yesterday.getField(DatatypeConstants.SECONDS));
        assertEquals("type", DatatypeConstants.DURATION_DAYTIME, yesterday.getXMLSchemaType());
        assertEquals("marshal", yesterday, this.datatypeFormat.marshal(value));
        assertEquals("unmarshal", value, this.datatypeFormat.unmarshal(yesterday));
    }

}
