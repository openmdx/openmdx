/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Immutable Datatypes Test
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
package org.w3c.spi;

import static javax.xml.datatype.DatatypeConstants.FIELD_UNDEFINED;
import static org.w3c.spi.DatatypeFactories.immutableDatatypeFactory;
import static org.w3c.spi.DatatypeFactories.xmlDatatypeFactory;

import java.text.ParseException;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.cci2.ImmutableDatatype;
import org.w3c.format.DateTimeFormat;

/**
 * Immutable Datatypes Test
 */
public class ImmutableDatatypesTest {

    static XMLGregorianCalendar immutableDate02; // 2000-02-29 
    static XMLGregorianCalendar immutableDate03; // 2000-03-01
    static Date immutableDateTime02; // 2000-02-29T12:00Z
    static Date immutableDateTime03; // 2000-03-01T12:00Z
    static Duration oneDay;
    static Duration oneAndHalfAYear;
    static Duration oneHour;
    XMLGregorianCalendar mutableDate02;
    XMLGregorianCalendar mutableDate03;
    #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif mutableDateTime02;
    #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif mutableDateTime03;

    @BeforeAll
    public static void immutableValues(){
        immutableDate02 = immutableDatatypeFactory().newDate("20000229"); 
        immutableDate03 = immutableDatatypeFactory().newDate("20000301");
        oneDay = immutableDatatypeFactory().newDuration("P1D");
        oneAndHalfAYear = immutableDatatypeFactory().newDuration("P18M");
        oneHour = immutableDatatypeFactory().newDuration("PT3600S");
        immutableDateTime02 = immutableDatatypeFactory().newDateTime("20000229T120000.000Z");
        immutableDateTime03 = immutableDatatypeFactory().newDateTime("20000301T120000.000Z");
    }
    
    @BeforeEach
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
        Assertions.assertEquals(immutableDate02,  mutableDate02, "immutableDate02.equals(mutableDate02)");
        Assertions.assertEquals(mutableDate02,  immutableDate02.clone(), "mutableDate02.equals(immutableDate02)");
        Assertions.assertEquals(immutableDate03,  mutableDate03, "immutableDate03.equals(mutableDate03)");
        Assertions.assertEquals(mutableDate03,  immutableDate03.clone(), "mutableDate03.equals(immutableDate03)");
        Assertions.assertEquals(-1,  mutableDate02.compare(mutableDate03), "mutableDate02.compare(mutableDate03)");
        Assertions.assertEquals(+1,  mutableDate03.compare(mutableDate02), "mutableDate03.compare(mutableDate02)");
        Assertions.assertEquals(-1,  mutableDate02.compare((XMLGregorianCalendar)immutableDate03.clone()), "mutableDate02.compare(immutableDate03)");
        Assertions.assertEquals(0,  mutableDate02.compare((XMLGregorianCalendar)immutableDate02.clone()), "mutableDate02.compare(immutableDate02)");
        Assertions.assertEquals(+1,  mutableDate03.compare((XMLGregorianCalendar)immutableDate02.clone()), "mutableDate02.compare(immutableDate02)");
        Assertions.assertEquals(-1,  immutableDate02.compare(mutableDate03), "immutableDate02.compare(mutableDate03)");
        Assertions.assertEquals(0,  immutableDate02.compare(mutableDate02), "immutableDate02.compare(mutableDate02)");
        Assertions.assertEquals(+1,  immutableDate03.compare(mutableDate02), "immutableDate03.compare(mutableDate02)");
        Assertions.assertEquals(0,  immutableDate02.compare(immutableDate02), "immutableDate02.compare(immutableDate02)");
        Assertions.assertEquals(0,  ((Comparable<XMLGregorianCalendar>)immutableDate02).compareTo(immutableDate02), "immutableDate02.compareTo(immutableDate02)");
        Assertions.assertEquals(-1,  immutableDate02.compare(immutableDate03), "immutableDate02.compare(immutableDate03)");
        Assertions.assertEquals(-1,  ((Comparable<XMLGregorianCalendar>)immutableDate02).compareTo(immutableDate03), "immutableDate02.compareTo(immutableDate03)");
        Assertions.assertEquals(+1,  immutableDate03.compare(immutableDate02), "immutableDate03.compare(immutableDate02)");
        Assertions.assertEquals(+1,  ((Comparable<XMLGregorianCalendar>)immutableDate03).compareTo(immutableDate02), "immutableDate03.compareTo(immutableDate02)");
        Assertions.assertEquals("2000-02-29",  mutableDate02.toXMLFormat(), "mutableDate02.toXMLFormat()");
        Assertions.assertEquals("2000-02-29",  immutableDate02.toXMLFormat(), "immutableDate02.toXMLFormat()");
        Assertions.assertEquals("20000229",  ((ImmutableDatatype<?>)immutableDate02).toBasicFormat(), "immutableDate02.toBasicFormat()");
        mutableDate02.add(oneDay);
        Assertions.assertEquals(mutableDate02,  mutableDate03, "mutableDate02 += P1D");
        try {
            immutableDate02.reset();
            Assertions.fail("immutableDate02");
        } catch (UnsupportedOperationException expected) {
        	// Unable to reset an immutable object
        }
    }

    private boolean isDurationNormalized(){
        return 
             !"1.5".equals(System.getProperty("java.specification.version")) &&
             !"IBM Corporation".equals(System.getProperty("java.vendor"));
    }
    
    @Test
    public void durationValues(){
        Assertions.assertEquals("P1Y6M",  immutableDatatypeFactory().toNormalizedDuration(oneAndHalfAYear).toString(), "Normalize year/month duration");
        Assertions.assertEquals("PT1H0M0S",  immutableDatatypeFactory().toNormalizedDuration(oneHour).toString(), "Normalize day/time duration");
        Duration d = oneAndHalfAYear.add(oneHour);
        if(isDurationNormalized()) {
            Assertions.assertEquals("P1Y6MT1H0M0S",  d.toString(), "Standard duration is normalized");
        } else {
            Assertions.assertEquals("P18MT3600S",  d.toString(), "Standard duration is non-normalized");
        }
        Assertions.assertEquals("P1Y6MT1H0M0S",  immutableDatatypeFactory().toNormalizedDuration(d).toString(), "Normalized duration");
    }
    
    @Test
    public void dateTimeValues(){
        Assertions.assertEquals(immutableDateTime02,  mutableDateTime02, "immutableDateTime02.equals(mutableDateTime02)");
        Assertions.assertEquals(mutableDateTime02,  immutableDateTime02, "mutableDateTime02.equals(immutableDateTime02)");
        Assertions.assertEquals(immutableDateTime03,  mutableDateTime03, "immutableDateTime03.equals(mutableDateTime03)");
        Assertions.assertEquals(mutableDateTime03,  immutableDateTime03, "mutableDateTime03.equals(immutableDateTime03)");
        Assertions.assertEquals(-1,  immutableDateTime02.compareTo(immutableDateTime03), "immutableDateTime02.compareTo(immutableDateTime03)");
        Assertions.assertEquals(0,  immutableDateTime02.compareTo(immutableDateTime02), "immutableDateTime02.compareTo(immutableDateTime02)");
        Assertions.assertEquals(-1,  immutableDateTime02.compareTo(immutableDateTime03), "immutableDateTime02.compareTo(immutableDateTime03)");
        Assertions.assertEquals(-1,  immutableDateTime02.compareTo(mutableDateTime03), "immutableDateTime02.compareTo(mutableDateTime03)");
        Assertions.assertEquals(0,  immutableDateTime02.compareTo(mutableDateTime02), "immutableDateTime02.compareTo(mutableDateTime02)");
        Assertions.assertEquals(-1,  immutableDateTime02.compareTo(mutableDateTime03), "immutableDateTime02.compareTo(mutableDateTime03)");
        Assertions.assertEquals(-1,  mutableDateTime02.compareTo(immutableDateTime03), "mutableDateTime02.compareTo(immutableDateTime03)");
        Assertions.assertEquals(0,  mutableDateTime02.compareTo(immutableDateTime02), "mutableDateTime02.compareTo(immutableDateTime02)");
        Assertions.assertEquals(-1,  mutableDateTime02.compareTo(immutableDateTime03), "mutableDateTime02.compareTo(immutableDateTime03)");
        Assertions.assertEquals("2000-02-29T12:00:00.000Z",  DateTimeFormat.EXTENDED_UTC_FORMAT.format(immutableDateTime02), "immutableDate02.toExtendedFormat()");
        Assertions.assertEquals("2000-02-29T12:00:00.000Z",  DateTimeFormat.EXTENDED_UTC_FORMAT.format(mutableDateTime02), "mutableDate02.toExtendedFormat()");
        Assertions.assertEquals("20000229T120000.000Z",  DateTimeFormat.BASIC_UTC_FORMAT.format(immutableDateTime02), "immutableDate02.toBasicFormat()");
        Assertions.assertEquals("20000229T120000.000Z",  DateTimeFormat.BASIC_UTC_FORMAT.format(mutableDateTime02), "mutableDate02.toBasicFormat()");
        mutableDateTime03 = new Date(mutableDateTime02.getTime() + 1000L * 60 * 60 * 24);
        Assertions.assertEquals(immutableDateTime03,  mutableDateTime03, "mutableDateTime02 += P1D");
        try {
            immutableDateTime02.setTime(System.currentTimeMillis());
            Assertions.fail("immutableDateTime02");
        } catch (UnsupportedOperationException expected) {
        	// Unable to modify an immutable object
        }
    }
    
}
