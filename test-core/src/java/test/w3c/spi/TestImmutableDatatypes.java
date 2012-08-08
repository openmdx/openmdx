/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestImmutableDatatypes.java,v 1.7 2011/04/05 18:06:28 hburger Exp $
 * Description: TestImmutableDatatypes 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/04/05 18:06:28 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package test.w3c.spi;

import static javax.xml.datatype.DatatypeConstants.FIELD_UNDEFINED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.w3c.spi.DatatypeFactories.immutableDatatypeFactory;
import static org.w3c.spi.DatatypeFactories.xmlDatatypeFactory;

import java.text.ParseException;
import java.util.Date;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.cci2.ImmutableDatatype;
import org.w3c.format.DateTimeFormat;

/**
 * TestImmutableDatatypes
 */
public class TestImmutableDatatypes {

    static XMLGregorianCalendar immutableDate02; // 2000-02-29 
    static XMLGregorianCalendar immutableDate03; // 2000-03-01
    static Date immutableDateTime02; // 2000-02-29T12:00Z
    static Date immutableDateTime03; // 2000-03-01T12:00Z
    static Duration oneDay;
    static Duration oneAndHalfAYear;
    static Duration oneHour;
    XMLGregorianCalendar mutableDate02;
    XMLGregorianCalendar mutableDate03;
    Date mutableDateTime02;
    Date mutableDateTime03;

    @BeforeClass
    public static void immutableValues(){
        immutableDate02 = immutableDatatypeFactory().newDate("20000229"); 
        immutableDate03 = immutableDatatypeFactory().newDate("20000301");
        oneDay = immutableDatatypeFactory().newDuration("P1D");
        oneAndHalfAYear = immutableDatatypeFactory().newDuration("P18M");
        oneHour = immutableDatatypeFactory().newDuration("PT3600S");
        immutableDateTime02 = immutableDatatypeFactory().newDateTime("20000229T120000.000Z");
        immutableDateTime03 = immutableDatatypeFactory().newDateTime("20000301T120000.000Z");
    }
    
    @Before
    public void initialValues() throws ParseException{
        mutableDate02 = xmlDatatypeFactory().newXMLGregorianCalendarDate(
            2000, // year 
            2, // month
            29, // day
            FIELD_UNDEFINED // time zone
        );
        mutableDate03 = xmlDatatypeFactory().newXMLGregorianCalendarDate(
            2000, // year 
            3, // month
            01, // day
            FIELD_UNDEFINED // time zone
        );
        mutableDateTime02 = DateTimeFormat.BASIC_UTC_FORMAT.parse("20000229T120000.000Z");
        mutableDateTime03 = DateTimeFormat.BASIC_UTC_FORMAT.parse("20000301T120000.000Z");
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void dateValues(){
        assertEquals("immutableDate02.equals(mutableDate02)", immutableDate02, mutableDate02);
        assertEquals("mutableDate02.equals(immutableDate02)", mutableDate02, immutableDate02.clone());
        assertEquals("immutableDate03.equals(mutableDate03)", immutableDate03, mutableDate03);
        assertEquals("mutableDate03.equals(immutableDate03)", mutableDate03, immutableDate03.clone());
        assertEquals("mutableDate02.compare(mutableDate03)", -1, mutableDate02.compare(mutableDate03));
        assertEquals("mutableDate03.compare(mutableDate02)", +1, mutableDate03.compare(mutableDate02));
        assertEquals("mutableDate02.compare(immutableDate03)", -1, mutableDate02.compare((XMLGregorianCalendar)immutableDate03.clone()));
        assertEquals("mutableDate02.compare(immutableDate02)", 0, mutableDate02.compare((XMLGregorianCalendar)immutableDate02.clone()));
        assertEquals("mutableDate02.compare(immutableDate02)", +1, mutableDate03.compare((XMLGregorianCalendar)immutableDate02.clone()));
        assertEquals("immutableDate02.compare(mutableDate03)", -1, immutableDate02.compare(mutableDate03));
        assertEquals("immutableDate02.compare(mutableDate02)", 0, immutableDate02.compare(mutableDate02));
        assertEquals("immutableDate03.compare(mutableDate02)", +1, immutableDate03.compare(mutableDate02));
        assertEquals("immutableDate02.compare(immutableDate02)", 0, immutableDate02.compare(immutableDate02));
        assertEquals("immutableDate02.compareTo(immutableDate02)", 0, ((Comparable<XMLGregorianCalendar>)immutableDate02).compareTo(immutableDate02));
        assertEquals("immutableDate02.compare(immutableDate03)", -1, immutableDate02.compare(immutableDate03));
        assertEquals("immutableDate02.compareTo(immutableDate03)", -1, ((Comparable<XMLGregorianCalendar>)immutableDate02).compareTo(immutableDate03));
        assertEquals("immutableDate03.compare(immutableDate02)", +1, immutableDate03.compare(immutableDate02));
        assertEquals("immutableDate03.compareTo(immutableDate02)", +1, ((Comparable<XMLGregorianCalendar>)immutableDate03).compareTo(immutableDate02));
        assertEquals("mutableDate02.toXMLFormat()", "2000-02-29", mutableDate02.toXMLFormat());
        assertEquals("immutableDate02.toXMLFormat()", "2000-02-29", immutableDate02.toXMLFormat());
        assertEquals("immutableDate02.toBasicFormat()", "20000229", ((ImmutableDatatype<?>)immutableDate02).toBasicFormat());
        mutableDate02.add(oneDay);
        assertEquals("mutableDate02 += P1D", mutableDate02, mutableDate03);
        try {
            immutableDate02.reset();
            fail("immutableDate02");
        } catch (UnsupportedOperationException expected) {
        }
    }

    @Test
    public void durationValues(){
        assertEquals("Normalize year/month duration", "P1Y6M", immutableDatatypeFactory().toNormalizedDuration(oneAndHalfAYear).toString());
        assertEquals("Normalize day/time duration", "PT1H0M0S", immutableDatatypeFactory().toNormalizedDuration(oneHour).toString());
        Duration d = oneAndHalfAYear.add(oneHour);
        if("1.5".equals(System.getProperty("java.specification.version"))){
            assertEquals("Standard duration is non-normalized", "P18MT3600S", d.toString());
        } else {
            assertEquals("Standard duration is normalized", "P1Y6MT1H0M0S", d.toString());
        }
        assertEquals("Normalized duration", "P1Y6MT1H0M0S", immutableDatatypeFactory().toNormalizedDuration(d).toString());
    }
    
    @Test
    public void dateTimeValues(){
        assertEquals("immutableDateTime02.equals(mutableDateTime02)", immutableDateTime02, mutableDateTime02);
        assertEquals("mutableDateTime02.equals(immutableDateTime02)", mutableDateTime02, immutableDateTime02);
        assertEquals("immutableDateTime03.equals(mutableDateTime03)", immutableDateTime03, mutableDateTime03);
        assertEquals("mutableDateTime03.equals(immutableDateTime03)", mutableDateTime03, immutableDateTime03);
        assertEquals("immutableDateTime02.compareTo(immutableDateTime03)", -1, immutableDateTime02.compareTo(immutableDateTime03));
        assertEquals("immutableDateTime02.compareTo(immutableDateTime02)", 0, immutableDateTime02.compareTo(immutableDateTime02));
        assertEquals("immutableDateTime02.compareTo(immutableDateTime03)", -1, immutableDateTime02.compareTo(immutableDateTime03));
        assertEquals("immutableDateTime02.compareTo(mutableDateTime03)", -1, immutableDateTime02.compareTo(mutableDateTime03));
        assertEquals("immutableDateTime02.compareTo(mutableDateTime02)", 0, immutableDateTime02.compareTo(mutableDateTime02));
        assertEquals("immutableDateTime02.compareTo(mutableDateTime03)", -1, immutableDateTime02.compareTo(mutableDateTime03));
        assertEquals("mutableDateTime02.compareTo(immutableDateTime03)", -1, mutableDateTime02.compareTo(immutableDateTime03));
        assertEquals("mutableDateTime02.compareTo(immutableDateTime02)", 0, mutableDateTime02.compareTo(immutableDateTime02));
        assertEquals("mutableDateTime02.compareTo(immutableDateTime03)", -1, mutableDateTime02.compareTo(immutableDateTime03));
        assertEquals("immutableDate02.toExtendedFormat()", "2000-02-29T12:00:00.000Z", DateTimeFormat.EXTENDED_UTC_FORMAT.format(immutableDateTime02));
        assertEquals("mutableDate02.toExtendedFormat()", "2000-02-29T12:00:00.000Z",  DateTimeFormat.EXTENDED_UTC_FORMAT.format(mutableDateTime02));
        assertEquals("immutableDate02.toBasicFormat()", "20000229T120000.000Z",  DateTimeFormat.BASIC_UTC_FORMAT.format(immutableDateTime02));
        assertEquals("mutableDate02.toBasicFormat()", "20000229T120000.000Z",  DateTimeFormat.BASIC_UTC_FORMAT.format(mutableDateTime02));
        mutableDateTime03 = new Date(mutableDateTime02.getTime() + 1000L * 60 * 60 * 24);
        assertEquals("mutableDateTime02 += P1D", immutableDateTime03, mutableDateTime03);
        try {
            immutableDateTime02.setTime(System.currentTimeMillis());
            fail("immutableDateTime02");
        } catch (UnsupportedOperationException expected) {
        }
    }
    
}
