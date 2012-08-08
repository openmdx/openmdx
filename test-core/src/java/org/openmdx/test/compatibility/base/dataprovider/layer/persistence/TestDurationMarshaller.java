/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestDurationMarshaller.java,v 1.4 2007/01/22 15:41:04 hburger Exp $
 * Description: class TestRecord
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/01/22 15:41:04 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.test.compatibility.base.dataprovider.layer.persistence;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc.DurationMarshaller;
import org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc.LayerConfigurationEntries;
import org.openmdx.kernel.exception.BasicException;

public class TestDurationMarshaller extends TestCase {
    
    /**
     * Constructs a test case with the given name.
     */
    public TestDurationMarshaller(
    String name
    ) {
        super(name);
    }
    
    /**
     * The batch TestRunner can be given a class to run directly.
     * To start the batch runner from your main you can write: 
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * A test runner either expects a static method suite as the
     * entry point to get a test to run or it will extract the 
     * suite automatically. 
     */
    public static Test suite(
    ) {
        return new TestSuite(TestDurationMarshaller.class);
    }

    protected DatatypeFactory datatypeFactory;
    protected DurationMarshaller intervalDurationMarshaller;
    protected DurationMarshaller numericDurationMarshaller;
    protected DurationMarshaller characterDurationMarshaller;
    
    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp(
    ) throws Exception {
        this.datatypeFactory = DatatypeFactory.newInstance();
        this.intervalDurationMarshaller = DurationMarshaller.newInstance(
            LayerConfigurationEntries.DURATION_TYPE_INTERVAL, true
        );
        this.numericDurationMarshaller = DurationMarshaller.newInstance(
            LayerConfigurationEntries.DURATION_TYPE_NUMERIC, true
        );
        this.characterDurationMarshaller = DurationMarshaller.newInstance(
            LayerConfigurationEntries.DURATION_TYPE_CHARACTER, true
        );
    }
    
    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void tearDown(
    ) throws Exception {
    }
    
    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testExternalizeInterval(
    ) throws Throwable {
        Object internalized = this.characterDurationMarshaller.unmarshal("P0D");
        Object externalized = this.intervalDurationMarshaller.marshal(internalized);
        assertEquals("P0D", "0 0:0:0.000", externalized);
        internalized = this.characterDurationMarshaller.unmarshal("P0M");
        externalized = this.intervalDurationMarshaller.marshal(internalized);
        assertEquals("P0M", "0-0", externalized);
        internalized = this.characterDurationMarshaller.unmarshal("P7Y6M");
        externalized = this.intervalDurationMarshaller.marshal(internalized);
        assertEquals("P7Y6M", "7-6", externalized);
        internalized = this.characterDurationMarshaller.unmarshal("PT4H3M2.010S");
        externalized = this.intervalDurationMarshaller.marshal(internalized);
        assertEquals("PT4H3M2.010S", "0 4:3:2.010", externalized);
        internalized = this.characterDurationMarshaller.unmarshal("-P5DT4H3M2.010S");
        externalized = this.intervalDurationMarshaller.marshal(internalized);
        assertEquals("-P5DT4H3M2.010S", "-5 4:3:2.010", externalized);
        internalized = this.characterDurationMarshaller.unmarshal("-P7Y6M");
        externalized = this.intervalDurationMarshaller.marshal(internalized);
        assertEquals("-P7Y6M", "-7-6", externalized);
        internalized = this.characterDurationMarshaller.unmarshal("-PT4H3M2.010S");
        externalized = this.intervalDurationMarshaller.marshal(internalized);
        assertEquals("-PT4H3M2.010S", "-0 4:3:2.010", externalized);
        internalized = this.characterDurationMarshaller.unmarshal("P7Y6M5DT4H3M2.010S");
        try {
            externalized = this.intervalDurationMarshaller.marshal(internalized);
            fail("year-month or day-time");
        } catch (ServiceException exception) {
            assertEquals(
                "TRANSFORMATION_FAILURE", 
                BasicException.Code.TRANSFORMATION_FAILURE,
                exception.getExceptionCode()
            );                
        }
        internalized = this.characterDurationMarshaller.unmarshal("P7Y6M5D");
        try {
            externalized = this.intervalDurationMarshaller.marshal(internalized);
            fail("year-month or day-time");
        } catch (ServiceException exception) {
            assertEquals(
                "TRANSFORMATION_FAILURE", 
                BasicException.Code.TRANSFORMATION_FAILURE,
                exception.getExceptionCode()
            );                
        }
        internalized = this.characterDurationMarshaller.unmarshal("-P7Y6M5D");
        try {
            externalized = this.intervalDurationMarshaller.marshal(internalized);
            fail("year-month or day-time");
        } catch (ServiceException exception) {
            assertEquals(
                "TRANSFORMATION_FAILURE", 
                BasicException.Code.TRANSFORMATION_FAILURE,
                exception.getExceptionCode()
            );                
        }
        internalized = this.characterDurationMarshaller.unmarshal("-P7Y6M5DT4H3M2.010S");
        try {
            externalized = this.intervalDurationMarshaller.marshal(internalized);
            fail("year-month or day-time");
        } catch (ServiceException exception) {
            assertEquals(
                "TRANSFORMATION_FAILURE", 
                BasicException.Code.TRANSFORMATION_FAILURE,
                exception.getExceptionCode()
            );                
        }
    }  

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testInternalizeInterval(
    ) throws Throwable {      
        Object internalized = this.intervalDurationMarshaller.unmarshal("0 0:0:0.000");
        Object externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("P0DT0H0M0.000S", "P0DT0H0M0.000S", externalized);
        internalized = this.intervalDurationMarshaller.unmarshal("0-0");
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("P0Y0M", "P0Y0M", externalized);
        internalized = this.intervalDurationMarshaller.unmarshal("7-6");
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("P7Y6M", "P7Y6M", externalized);
        internalized = this.intervalDurationMarshaller.unmarshal("0 4:3:2.010");
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("P0DT4H3M2.010S", "P0DT4H3M2.010S", externalized);
        internalized = this.intervalDurationMarshaller.unmarshal("-5 4:3:2.010");
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("-P5DT4H3M2.010S", "-P5DT4H3M2.010S", externalized);
        internalized = this.intervalDurationMarshaller.unmarshal("-7-6");
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("-P7Y6M", "-P7Y6M", externalized);
        internalized = this.intervalDurationMarshaller.unmarshal("-0 4:3:2.010");
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("-P0DT4H3M2.010S", "-P0DT4H3M2.010S", externalized);
    }  

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testExternalizeNumeric(
    ) throws Throwable {        
        Object internalized = this.characterDurationMarshaller.unmarshal("P0D");
        Object externalized = this.numericDurationMarshaller.marshal(internalized);
        assertEquals("P0D", BigDecimal.valueOf(0, 3), externalized);
        internalized = this.characterDurationMarshaller.unmarshal("P0M");
        externalized = this.numericDurationMarshaller.marshal(internalized);
        assertEquals("P0M", BigInteger.ZERO, externalized);
        internalized = this.characterDurationMarshaller.unmarshal("P7Y6M");
        externalized = this.numericDurationMarshaller.marshal(internalized);
        assertEquals("P7Y6M", BigInteger.valueOf(90), externalized);
        internalized = this.characterDurationMarshaller.unmarshal("PT4H3M2.010S");
        externalized = this.numericDurationMarshaller.marshal(internalized);
        assertEquals("PT4H3M2.010S", new BigDecimal("14582.010"), externalized);
        internalized = this.characterDurationMarshaller.unmarshal("-P5DT4H3M2.010S");
        externalized = this.numericDurationMarshaller.marshal(internalized);
        assertEquals("-P5DT4H3M2.010S", new BigDecimal("-446582.010"), externalized);
        internalized = this.characterDurationMarshaller.unmarshal("-P7Y6M");
        externalized = this.numericDurationMarshaller.marshal(internalized);
        assertEquals("-P7Y6M", BigInteger.valueOf(-90), externalized);
        internalized = this.characterDurationMarshaller.unmarshal("-PT4H3M2.010S");
        externalized = this.numericDurationMarshaller.marshal(internalized);
        assertEquals("-PT4H3M2.010S", new BigDecimal("-14582.010"), externalized);
        internalized = this.characterDurationMarshaller.unmarshal("P7Y6M5DT4H3M2.010S");
        try {
            externalized = this.numericDurationMarshaller.marshal(internalized);
            fail("year-month or day-time");
        } catch (ServiceException exception) {
            assertEquals(
                "TRANSFORMATION_FAILURE", 
                BasicException.Code.TRANSFORMATION_FAILURE,
                exception.getExceptionCode()
            );                
        }
        internalized = this.characterDurationMarshaller.unmarshal("P7Y6M5D");
        try {
            externalized = this.numericDurationMarshaller.marshal(internalized);
            fail("year-month or day-time");
        } catch (ServiceException exception) {
            assertEquals(
                "TRANSFORMATION_FAILURE", 
                BasicException.Code.TRANSFORMATION_FAILURE,
                exception.getExceptionCode()
            );                
        }
        internalized = this.characterDurationMarshaller.unmarshal("-P7Y6M5D");
        try {
            externalized = this.numericDurationMarshaller.marshal(internalized);
            fail("year-month or day-time");
        } catch (ServiceException exception) {
            assertEquals(
                "TRANSFORMATION_FAILURE", 
                BasicException.Code.TRANSFORMATION_FAILURE,
                exception.getExceptionCode()
            );                
        }
        internalized = this.characterDurationMarshaller.unmarshal("-P7Y6M5DT4H3M2.010S");
        try {
            externalized = this.numericDurationMarshaller.marshal(internalized);
            fail("year-month or day-time");
        } catch (ServiceException exception) {
            assertEquals(
                "TRANSFORMATION_FAILURE", 
                BasicException.Code.TRANSFORMATION_FAILURE,
                exception.getExceptionCode()
            );                
        }
    }  

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testInternalizeNumeric(
    ) throws Throwable {        
        Object internalized = this.numericDurationMarshaller.unmarshal(BigDecimal.valueOf(0, 3));
        Object externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("P0D", "PT0.000S", externalized);
        internalized = this.numericDurationMarshaller.unmarshal(BigInteger.ZERO);
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("P0M", "P0M", externalized);
        internalized = this.numericDurationMarshaller.unmarshal(BigInteger.valueOf(90));
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("P7Y6M", "P90M", externalized);
        internalized = this.numericDurationMarshaller.unmarshal(new BigDecimal("14582.010"));
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("PT4H3M2.010S", "PT14582.010S", externalized);
        internalized = this.numericDurationMarshaller.unmarshal(new BigDecimal("-446582.010"));
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("-P5DT4H3M2.010S", "-PT446582.010S", externalized);
        internalized = this.numericDurationMarshaller.unmarshal(BigInteger.valueOf(-90));
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("-P7Y6M", "-P90M", externalized);
        internalized = this.numericDurationMarshaller.unmarshal(new BigDecimal("-14582.010"));
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("-PT4H3M2.010S", "-PT14582.010S", externalized);
    }  

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testExternalizeCharacter(
    ) throws Throwable {
        Duration internalized = this.datatypeFactory.newDuration(true, null, null, BigInteger.valueOf(5), BigInteger.valueOf(4), BigInteger.valueOf(3), new BigDecimal("2.010"));
        assertEquals("SIGN", +1, internalized.getSign());
        assertEquals("DURATION_DAYTIME", DatatypeConstants.DURATION_DAYTIME, internalized.getXMLSchemaType());
        Object externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("P5DT4H3M2.010S", "P5DT4H3M2.010S", externalized);
        internalized = this.datatypeFactory.newDuration(true, null, null, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, new BigDecimal("0.000"));
        assertEquals("SIGN", 0, internalized.getSign());
        assertEquals("DURATION_YEARMONTH", DatatypeConstants.DURATION_DAYTIME, internalized.getXMLSchemaType());
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("P0DT0H0M0.000S", "P0DT0H0M0.000S", externalized);
        internalized = this.datatypeFactory.newDuration(true, null, null, BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO, new BigDecimal("2.010"));
        assertEquals("SIGN", +1, internalized.getSign());
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("P0DT0H0M2.010S", "P0DT0H0M2.010S", externalized);
        internalized = this.datatypeFactory.newDurationYearMonth(true, 7, 6);
        assertEquals("SIGN", +1, internalized.getSign());
        assertEquals("DURATION_YEARMONTH", DatatypeConstants.DURATION_YEARMONTH, internalized.getXMLSchemaType());
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("P7Y6M", "P7Y6M", externalized);
        internalized = this.datatypeFactory.newDurationYearMonth(false, 7, 6);
        assertEquals("SIGN", -1, internalized.getSign());
        assertEquals("DURATION_YEARMONTH", DatatypeConstants.DURATION_YEARMONTH, internalized.getXMLSchemaType());
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("-P7Y6M", "-P7Y6M", externalized);
        internalized = this.datatypeFactory.newDurationDayTime(true, 5, 4, 3, 2);
        assertEquals("SIGN", +1, internalized.getSign());
        assertEquals("DURATION_DAYTIME", DatatypeConstants.DURATION_DAYTIME, internalized.getXMLSchemaType());
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("P5DT4H3M2S", "P5DT4H3M2S", externalized);
        internalized = this.datatypeFactory.newDurationDayTime(false, 5, 4, 3, 2);
        assertEquals("SIGN", -1, internalized.getSign());
        assertEquals("DURATION_DAYTIME", DatatypeConstants.DURATION_DAYTIME, internalized.getXMLSchemaType());
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("-P5DT4H3M2S", "-P5DT4H3M2S", externalized);
        internalized = this.datatypeFactory.newDuration(true, BigInteger.valueOf(7), BigInteger.valueOf(6), BigInteger.valueOf(5), BigInteger.valueOf(4), BigInteger.valueOf(3), new BigDecimal("2.010"));
        assertEquals("SIGN",+1, internalized.getSign());
        assertEquals("DURATION", DatatypeConstants.DURATION, internalized.getXMLSchemaType());
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("P7Y6M5DT4H3M2.010S", "P7Y6M5DT4H3M2.010S", externalized);
        internalized = this.datatypeFactory.newDuration(false, BigInteger.valueOf(7), BigInteger.valueOf(6), BigInteger.valueOf(5), BigInteger.valueOf(4), BigInteger.valueOf(3), new BigDecimal("2.010"));
        assertEquals("SIGN", -1, internalized.getSign());
        assertEquals("DURATION", DatatypeConstants.DURATION, internalized.getXMLSchemaType());
        externalized = this.characterDurationMarshaller.marshal(internalized);
        assertEquals("-P7Y6M5DT4H3M2.010S", "-P7Y6M5DT4H3M2.010S", externalized);
    }  

}
