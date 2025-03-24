/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Duration Masrhaller Test
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

import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.datatypes.DurationMarshaller;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.MutableDatatypeFactory;

public class DurationMarshallerTest {
    
    protected DatatypeFactory datatypeFactory;
    protected DurationMarshaller intervalDurationMarshaller;
    protected DurationMarshaller numericDurationMarshaller;
    protected DurationMarshaller characterDurationMarshaller;
    
    @BeforeEach
    public void setUp(
    ) throws Exception {
        this.datatypeFactory = MutableDatatypeFactory.xmlDatatypeFactory();
        this.intervalDurationMarshaller = DurationMarshaller.newInstance(
            LayerConfigurationEntries.DURATION_TYPE_INTERVAL
        );
        this.numericDurationMarshaller = DurationMarshaller.newInstance(
            LayerConfigurationEntries.DURATION_TYPE_NUMERIC
        );
        this.characterDurationMarshaller = DurationMarshaller.newInstance(
            LayerConfigurationEntries.DURATION_TYPE_CHARACTER
        );
    }
    
    @Disabled("§")
    @Test
    public void testExternalizeInterval(
    ) throws Throwable {
        Object internalized = this.characterDurationMarshaller.unmarshal("P0D");
        Object externalized = this.intervalDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("0 0:0:0.000", externalized, "P0D");
        internalized = this.characterDurationMarshaller.unmarshal("P0M");
        externalized = this.intervalDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("0-0", externalized, "P0M");
        internalized = this.characterDurationMarshaller.unmarshal("P7Y6M");
        externalized = this.intervalDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("7-6", externalized, "P7Y6M");
        internalized = this.characterDurationMarshaller.unmarshal("PT4H3M2.010S");
        externalized = this.intervalDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("0 4:3:2.010", externalized, "PT4H3M2.010S");
        internalized = this.characterDurationMarshaller.unmarshal("-P5DT4H3M2.010S");
        externalized = this.intervalDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("-5 4:3:2.010", externalized, "-P5DT4H3M2.010S");
        internalized = this.characterDurationMarshaller.unmarshal("-P7Y6M");
        externalized = this.intervalDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("-7-6", externalized, "-P7Y6M");
        internalized = this.characterDurationMarshaller.unmarshal("-PT4H3M2.010S");
        externalized = this.intervalDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("-0 4:3:2.010", externalized, "-PT4H3M2.010S");
        internalized = this.characterDurationMarshaller.unmarshal("P7Y6M5DT4H3M2.010S");
        try {
            externalized = this.intervalDurationMarshaller.marshal(internalized,"anyDB");
            fail("year-month or day-time");
        } catch (ServiceException exception) {
            Assertions.assertEquals( BasicException.Code.TRANSFORMATION_FAILURE,  exception.getExceptionCode(), "TRANSFORMATION_FAILURE");                
        }
        internalized = this.characterDurationMarshaller.unmarshal("P7Y6M5D");
        try {
            externalized = this.intervalDurationMarshaller.marshal(internalized,"anyDB");
            fail("year-month or day-time");
        } catch (ServiceException exception) {
            Assertions.assertEquals( BasicException.Code.TRANSFORMATION_FAILURE,  exception.getExceptionCode(), "TRANSFORMATION_FAILURE");                
        }
        internalized = this.characterDurationMarshaller.unmarshal("-P7Y6M5D");
        try {
            externalized = this.intervalDurationMarshaller.marshal(internalized,"anyDB");
            fail("year-month or day-time");
        } catch (ServiceException exception) {
            Assertions.assertEquals( BasicException.Code.TRANSFORMATION_FAILURE,  exception.getExceptionCode(), "TRANSFORMATION_FAILURE");                
        }
        internalized = this.characterDurationMarshaller.unmarshal("-P7Y6M5DT4H3M2.010S");
        try {
            externalized = this.intervalDurationMarshaller.marshal(internalized,"anyDB");
            fail("year-month or day-time");
        } catch (ServiceException exception) {
            Assertions.assertEquals( BasicException.Code.TRANSFORMATION_FAILURE,  exception.getExceptionCode(), "TRANSFORMATION_FAILURE");                
        }
    }  

    @Test
    public void testInternalizeInterval(
    ) throws Throwable {      
        Object internalized = this.intervalDurationMarshaller.unmarshal("0 0:0:0.000");
        Object externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("P0DT0H0M0.000S", externalized, "P0DT0H0M0.000S");
        internalized = this.intervalDurationMarshaller.unmarshal("0-0");
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("P0Y0M", externalized, "P0Y0M");
        internalized = this.intervalDurationMarshaller.unmarshal("7-6");
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("P7Y6M", externalized, "P7Y6M");
        internalized = this.intervalDurationMarshaller.unmarshal("0 4:3:2.010");
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("P0DT4H3M2.010S", externalized, "P0DT4H3M2.010S");
        internalized = this.intervalDurationMarshaller.unmarshal("-5 4:3:2.010");
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("-P5DT4H3M2.010S", externalized, "-P5DT4H3M2.010S");
        internalized = this.intervalDurationMarshaller.unmarshal("-7-6");
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("-P7Y6M", externalized, "-P7Y6M");
        internalized = this.intervalDurationMarshaller.unmarshal("-0 4:3:2.010");
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("-P0DT4H3M2.010S", externalized, "-P0DT4H3M2.010S");
    }  

    @Test
    public void testExternalizeNumeric(
    ) throws Throwable {        
        Object internalized = this.characterDurationMarshaller.unmarshal("P0D");
        Object externalized = this.numericDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals(BigDecimal.valueOf(0, 3), externalized, "P0D");
        internalized = this.characterDurationMarshaller.unmarshal("P0M");
        externalized = this.numericDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals(BigInteger.ZERO, externalized, "P0M");
        internalized = this.characterDurationMarshaller.unmarshal("P7Y6M");
        externalized = this.numericDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals(BigInteger.valueOf(90), externalized, "P7Y6M");
        internalized = this.characterDurationMarshaller.unmarshal("PT4H3M2.010S");
        externalized = this.numericDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals(new BigDecimal("14582.010"), externalized, "PT4H3M2.010S");
        internalized = this.characterDurationMarshaller.unmarshal("-P5DT4H3M2.010S");
        externalized = this.numericDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals(new BigDecimal("-446582.010"), externalized, "-P5DT4H3M2.010S");
        internalized = this.characterDurationMarshaller.unmarshal("-P7Y6M");
        externalized = this.numericDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals(BigInteger.valueOf(-90), externalized, "-P7Y6M");
        internalized = this.characterDurationMarshaller.unmarshal("-PT4H3M2.010S");
        externalized = this.numericDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals(new BigDecimal("-14582.010"), externalized, "-PT4H3M2.010S");
        internalized = this.characterDurationMarshaller.unmarshal("P7Y6M5DT4H3M2.010S");
        try {
            externalized = this.numericDurationMarshaller.marshal(internalized,"anyDB");
            fail("year-month or day-time");
        } catch (ServiceException exception) {
            Assertions.assertEquals( BasicException.Code.TRANSFORMATION_FAILURE,  exception.getExceptionCode(), "TRANSFORMATION_FAILURE");                
        }
        internalized = this.characterDurationMarshaller.unmarshal("P7Y6M5D");
        try {
            externalized = this.numericDurationMarshaller.marshal(internalized,"anyDB");
            fail("year-month or day-time");
        } catch (ServiceException exception) {
            Assertions.assertEquals( BasicException.Code.TRANSFORMATION_FAILURE,  exception.getExceptionCode(), "TRANSFORMATION_FAILURE");                
        }
        internalized = this.characterDurationMarshaller.unmarshal("-P7Y6M5D");
        try {
            externalized = this.numericDurationMarshaller.marshal(internalized,"anyDB");
            fail("year-month or day-time");
        } catch (ServiceException exception) {
            Assertions.assertEquals( BasicException.Code.TRANSFORMATION_FAILURE,  exception.getExceptionCode(), "TRANSFORMATION_FAILURE");                
        }
        internalized = this.characterDurationMarshaller.unmarshal("-P7Y6M5DT4H3M2.010S");
        try {
            externalized = this.numericDurationMarshaller.marshal(internalized,"anyDB");
            fail("year-month or day-time");
        } catch (ServiceException exception) {
            Assertions.assertEquals( BasicException.Code.TRANSFORMATION_FAILURE,  exception.getExceptionCode(), "TRANSFORMATION_FAILURE");                
        }
    }  

    @Test
    public void testInternalizeNumeric(
    ) throws Throwable {        
        Object internalized = this.numericDurationMarshaller.unmarshal(BigDecimal.valueOf(0, 3));
        Object externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("PT0.000S", externalized, "P0D");
        internalized = this.numericDurationMarshaller.unmarshal(BigInteger.ZERO);
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("P0M", externalized, "P0M");
        internalized = this.numericDurationMarshaller.unmarshal(BigInteger.valueOf(90));
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("P7Y6M", externalized, "P90M");
        internalized = this.numericDurationMarshaller.unmarshal(new BigDecimal("14582.010"));
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("PT4H3M2.010S", externalized, "PT14582.010S");
        internalized = this.numericDurationMarshaller.unmarshal(new BigDecimal("-446582.010"));
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("-P5DT4H3M2.010S", externalized, "-PT446582.010S");
        internalized = this.numericDurationMarshaller.unmarshal(BigInteger.valueOf(-90));
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("-P7Y6M", externalized, "-P90M");
        internalized = this.numericDurationMarshaller.unmarshal(new BigDecimal("-14582.010"));
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("-PT4H3M2.010S", externalized, "-PT14582.010S");
    }  

    @Test
    public void testExternalizeCharacter(
    ) throws Throwable {
        Duration internalized = this.datatypeFactory.newDuration(true, null, null, BigInteger.valueOf(5), BigInteger.valueOf(4), BigInteger.valueOf(3), new BigDecimal("2.010"));
        Assertions.assertEquals(+1,  internalized.getSign(), "SIGN");
        Assertions.assertEquals(DatatypeConstants.DURATION_DAYTIME, internalized.getXMLSchemaType(), "DURATION_DAYTIME");
        Object externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("P5DT4H3M2.010S", externalized, "P5DT4H3M2.010S");
        internalized = this.datatypeFactory.newDuration(true, null, null, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, new BigDecimal("0.000"));
        Assertions.assertEquals(0,  internalized.getSign(), "SIGN");
        Assertions.assertEquals(DatatypeConstants.DURATION_DAYTIME, internalized.getXMLSchemaType(), "DURATION_YEARMONTH");
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("P0DT0H0M0.000S", externalized, "P0DT0H0M0.000S");
        internalized = this.datatypeFactory.newDuration(true, null, null, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, new BigDecimal("2.010"));
        Assertions.assertEquals(+1,  internalized.getSign(), "SIGN");
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("P0DT0H0M2.010S", externalized, "P0DT0H0M2.010S");
        internalized = this.datatypeFactory.newDurationYearMonth(true, 7, 6);
        Assertions.assertEquals(+1,  internalized.getSign(), "SIGN");
        Assertions.assertEquals(DatatypeConstants.DURATION_YEARMONTH, internalized.getXMLSchemaType(), "DURATION_YEARMONTH");
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("P7Y6M", externalized, "P7Y6M");
        internalized = this.datatypeFactory.newDurationYearMonth(false, 7, 6);
        Assertions.assertEquals(-1,  internalized.getSign(), "SIGN");
        Assertions.assertEquals(DatatypeConstants.DURATION_YEARMONTH, internalized.getXMLSchemaType(), "DURATION_YEARMONTH");
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("-P7Y6M", externalized, "-P7Y6M");
        internalized = this.datatypeFactory.newDurationDayTime(true, 5, 4, 3, 2);
        Assertions.assertEquals(+1,  internalized.getSign(), "SIGN");
        Assertions.assertEquals(DatatypeConstants.DURATION_DAYTIME, internalized.getXMLSchemaType(), "DURATION_DAYTIME");
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("P5DT4H3M2S", externalized, "P5DT4H3M2S");
        internalized = this.datatypeFactory.newDurationDayTime(false, 5, 4, 3, 2);
        Assertions.assertEquals(-1,  internalized.getSign(), "SIGN");
        Assertions.assertEquals(DatatypeConstants.DURATION_DAYTIME, internalized.getXMLSchemaType(), "DURATION_DAYTIME");
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("-P5DT4H3M2S", externalized, "-P5DT4H3M2S");
        internalized = this.datatypeFactory.newDuration(true, BigInteger.valueOf(7), BigInteger.valueOf(6), BigInteger.valueOf(5), BigInteger.valueOf(4), BigInteger.valueOf(3), new BigDecimal("2.010"));
        Assertions.assertEquals(+1,  internalized.getSign(), "SIGN");
        Assertions.assertEquals(DatatypeConstants.DURATION, internalized.getXMLSchemaType(), "DURATION");
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("P7Y6M5DT4H3M2.010S", externalized, "P7Y6M5DT4H3M2.010S");
        internalized = this.datatypeFactory.newDuration(false, BigInteger.valueOf(7), BigInteger.valueOf(6), BigInteger.valueOf(5), BigInteger.valueOf(4), BigInteger.valueOf(3), new BigDecimal("2.010"));
        Assertions.assertEquals(-1,  internalized.getSign(), "SIGN");
        Assertions.assertEquals(DatatypeConstants.DURATION, internalized.getXMLSchemaType(), "DURATION");
        externalized = this.characterDurationMarshaller.marshal(internalized,"anyDB");
        Assertions.assertEquals("-P7Y6M5DT4H3M2.010S", externalized, "-P7Y6M5DT4H3M2.010S");
    }  

}
