/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractExtensionTest_1.java,v 1.8 2009/02/19 16:39:12 hburger Exp $
 * Description: Extension Unit Test
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/19 16:39:12 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2008, OMEX AG, Switzerland
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
package org.openmdx.test.datatypes1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.openmdx.application.log.AppLog;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.persistence.cci.EntityManagerFactory;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.test.datatypes1.cci2.NonStatedQuery;
import org.openmdx.test.datatypes1.jmi1.Data;
import org.openmdx.test.datatypes1.jmi1.Datatypes1Package;
import org.openmdx.test.datatypes1.jmi1.NonStated;
import org.openmdx.test.datatypes1.jmi1.Segment;
import org.w3c.spi.DatatypeFactories;

/**
 * Extension Unit Test
 */
public abstract class AbstractExtensionTest_1 {

    protected final static String MODEL = "org:openmdx:test:datatypes1";
    protected static final String GATEWAY_JNDI_NAME = "test/openmdx/datatypes1/Gateway";
    protected static final String ENTITY_MANAGER_FACTORY_JNDI_NAME = "test/openmdx/datatypes1/EntityManagerFactory";
    protected static final boolean IN_PROCESS = true;
    protected static final boolean LOG_DEPLOYMENT_DETAIL = true;
    protected static final String APPLICATION_URI = "file:../test-core/src/ear/test-extension.ear";

    protected final static TimeZone UTC = TimeZone.getTimeZone("UTC");
    
    protected static int VALUE_COUNT = 0;
    protected static final int VALUE1 = VALUE_COUNT++;
    protected static final int VALUE2 = VALUE_COUNT++;
    protected static final int VALUE3 = VALUE_COUNT++;
    protected static final int VALUE4 = VALUE_COUNT++;
    protected static final int VALUE5 = VALUE_COUNT++;
    protected static final int VALUE6 = VALUE_COUNT++;
    protected static final int VALUE7 = VALUE_COUNT++;
    protected static final int VALUE8 = VALUE_COUNT++;
    protected static final int VALUE9 = VALUE_COUNT++;
    protected static final int VALUE10 = VALUE_COUNT++;
    protected static final int VALUE11A = VALUE_COUNT++;
    protected static final int VALUE11B = VALUE_COUNT++;
    protected static final int VALUE11a = VALUE_COUNT++;
    protected static final int VALUE11b = VALUE_COUNT++;
    protected static final int SLICE_COUNT = 3;

    protected static EntityManagerFactory managerFactory;
    private Object[][] values;
    
    /**
     * Constructor
     * 
     * @param name
     */
    protected AbstractExtensionTest_1(
    ){
    }  
  
    /**
     * Set up the test fixture
     * @throws IOException 
     * @throws URISyntaxException 
     */
    @Before
    public void setUp(
    ) throws ParseException, URISyntaxException{  
        DatatypeFactory datatypeFactory = DatatypeFactories.xmlDatatypeFactory();
        DateFormat dateFormat = DateFormat.getInstance();
        this.values = new Object[SLICE_COUNT][];
        for(
            int i = 0;
            i < this.values.length;
            i++
        ) this.values[i] = new Object[VALUE_COUNT];
        this.values[0][VALUE1] = Boolean.TRUE;
        this.values[0][VALUE2] = new Short(Short.MAX_VALUE);
        this.values[0][VALUE3] = new int[]{-12,12};
        this.values[0][VALUE4] = new Long(8000000000L);
        this.values[0][VALUE5] = BigDecimal.valueOf(1234, 2);
        this.values[0][VALUE6] = "ABC";
        this.values[0][VALUE7] = dateFormat.parse("20060601T120000.123Z");
        this.values[0][VALUE8] = datatypeFactory.newXMLGregorianCalendarDate(2006,DatatypeConstants.APRIL,01,DatatypeConstants.FIELD_UNDEFINED);
        this.values[0][VALUE9] = new URI("http://www.openmdx.org");
        this.values[0][VALUE10] = new byte[]{Byte.MIN_VALUE, 0, Byte.MAX_VALUE};
        this.values[0][VALUE11A] = datatypeFactory.newDurationYearMonth("P150M");
        this.values[0][VALUE11B] = datatypeFactory.newDurationDayTime("PT446582.010S");
        this.values[0][VALUE11a] = datatypeFactory.newDurationYearMonth("P12Y6M");
        this.values[0][VALUE11b] = datatypeFactory.newDurationDayTime("P5DT4H3M2.010S");
        this.values[1][VALUE1] = Boolean.FALSE;
        this.values[1][VALUE2] = new Short(Short.MIN_VALUE);
        this.values[1][VALUE3] = new int[]{};
        this.values[1][VALUE4] = new Long(8000000000L);
        this.values[1][VALUE5] = BigDecimal.valueOf(-1234, 2);
        this.values[1][VALUE6] = "qwerty";
        this.values[1][VALUE7] = dateFormat.parse("29900101T000000.000Z");
        this.values[1][VALUE8] = datatypeFactory.newXMLGregorianCalendarDate(2990,DatatypeConstants.JANUARY,01,DatatypeConstants.FIELD_UNDEFINED);
        this.values[1][VALUE9] = new URI("mailto:info@openmdx.org");
        this.values[1][VALUE10] = new byte[]{};
        this.values[1][VALUE11A] = datatypeFactory.newDurationYearMonth("-P150M");
        this.values[1][VALUE11B] = datatypeFactory.newDurationDayTime("-PT446582.010S");
        this.values[1][VALUE11a] = datatypeFactory.newDurationYearMonth("-P12Y6M");
        this.values[1][VALUE11b] = datatypeFactory.newDurationDayTime("-P5DT4H3M2.010S");
        this.values[2][VALUE1] = Boolean.FALSE;
        this.values[2][VALUE2] = new Short(Short.MIN_VALUE);
        this.values[2][VALUE3] = new int[]{};
        this.values[2][VALUE4] = new Long(8000000000L);
        this.values[2][VALUE5] = BigDecimal.valueOf(-1234, 2);
        this.values[2][VALUE6] = "an_undersocre";
        this.values[2][VALUE7] = dateFormat.parse("20061201T120000.123Z");
        this.values[2][VALUE8] = datatypeFactory.newXMLGregorianCalendarDate(2006,DatatypeConstants.DECEMBER,01,DatatypeConstants.FIELD_UNDEFINED);
        this.values[2][VALUE9] = new URI("mailto:info@openmdx.org");
        this.values[2][VALUE10] = new byte[]{};
        this.values[2][VALUE11A] = datatypeFactory.newDurationYearMonth("-P12Y6M");
        this.values[2][VALUE11B] = datatypeFactory.newDurationDayTime("-P5DT4H3M2.010S");
        this.values[2][VALUE11a] = datatypeFactory.newDurationYearMonth("-P150M");
        this.values[2][VALUE11b] = datatypeFactory.newDurationDayTime("-PT446582.010S");
        System.out.println("Acquiring persistence manager factory...");
    }

    @Test
    public void testCalendar(){
        Calendar calendar = Calendar.getInstance(UTC);
        calendar.clear();
        calendar.set(
          2990, // year
          00, // zero based month
          01, // day
          0, // hour
          0, // minute
          0 // second
        );
        assertEquals("29900101T000000.000Z", DateFormat.getInstance().format(new Date(calendar.getTimeInMillis())));
    }
    
    @Test
    public void testDefault(
    ) throws Exception{
        try {
            storeDatatypes("Persistent", "Default");
            retrieveDatatypes("Persistent", "Default");
        } catch(Exception exception) {
            AppLog.error("Exception", exception);
            throw exception;
        }
    }

    @Test
    public void testNumeric(
    ) throws Exception{
        try {
            storeDatatypes("Persistent", "Numeric");
            retrieveDatatypes("Persistent", "Numeric");
        } catch(Exception exception) {
            AppLog.error("Exception", exception);
            throw exception;
        }
    }

    @Test
    public void testNative(
    ) throws Exception{
        try {
            storeDatatypes("Persistent", "Native");
            retrieveDatatypes("Persistent", "Native");
        } catch(Exception exception) {
            AppLog.error("Exception", exception);
            throw exception;
        }
    }

    @Test
    public void testVolatile(
    ) throws Exception{
        try {
            storeDatatypes("Volatile", "Native");
            retrieveDatatypes("Volatile", "Native");
        } catch(Exception exception) {
            AppLog.error("Exception", exception);
            throw exception;
        }
    }

    /**
     * Clear and Populate
     * 
     * @param providerName
     * @param segmentName
     * 
     * @throws Exception
     */
    protected void storeDatatypes(
        String providerName, 
        String segmentName
    ) throws Exception {
        System.out.println("Acquire persistence manager...");
        PersistenceManager persistenceManager = managerFactory.getEntityManager();
        Transaction unitOfWork = persistenceManager.currentTransaction();
        Authority authority = (Authority) persistenceManager.getObjectById(
            Authority.class,
            Datatypes1Package.AUTHORITY_XRI
        );
        Datatypes1Package datatypes1Package = (Datatypes1Package) 
        authority.refOutermostPackage().refPackage("org:openmdx:test:datatypes1");
        //
        // Reset workshop1 segment
        //
        Provider provider = authority.getProvider(providerName);
        System.out.println("Creating datatypes1 Segment...");
        Segment segment = (Segment) provider.getSegment(segmentName);
        if(segment != null) {
            unitOfWork.begin();
            segment.refDelete();
            unitOfWork.commit();
        }
        unitOfWork.begin();
        segment = datatypes1Package.getSegment().createSegment();
        provider.addSegment(false, segmentName, segment);
        unitOfWork.commit();
        //
        // Populate state1 segment
        //
        unitOfWork.begin();
        for(
            int i = 0;
            i < this.values.length;
            i++
        ){
            NonStated nonStated = datatypes1Package.getNonStated().createNonStated();
            setData(nonStated, this.values[i]);
            segment.addNonStated(false, String.valueOf(i), nonStated);
        }
        unitOfWork.commit();
    }

    /**
     * Populate Data objects
     * 
     * @param data
     * @param source
     */
    protected void setData(
        Data data,
        Object[] source
    ){
        data.setValue1(((Boolean)source[VALUE1]).booleanValue());
        data.setValue2(((Number)source[VALUE2]).shortValue());
        data.setValue3((int[]) source[VALUE3]);
        data.setValue4((Long)source[VALUE4]);
        data.setValue5((BigDecimal) source[VALUE5]);
        data.setValue6((String)source[VALUE6]);
        data.setValue7((Date)source[VALUE7]);
        data.setValue8((XMLGregorianCalendar)source[VALUE8]);
        data.setValue9((URI)source[VALUE9]);
        data.setValue10((byte[])source[VALUE10]);
        data.setValue11a((Duration)source[VALUE11A]);
        data.setValue11b((Duration)source[VALUE11B]);
    }
    
    /**
     * Clear and Populate
     * 
     * @param providerName
     * @param segmentName
     * @throws Exception
     */
    protected void retrieveDatatypes(
        String providerName, 
        String segmentName
    ) throws Exception {
        System.out.println("Acquire persistence manager...");
        PersistenceManager persistenceManager = managerFactory.getEntityManager();
        Authority authority = (Authority) persistenceManager.getObjectById(
            Authority.class,
            Datatypes1Package.AUTHORITY_XRI
        );
        //
        // Reset workshop1 segment
        //
        Provider provider = authority.getProvider(providerName);
        Datatypes1Package datatypes1Package = (Datatypes1Package) authority.refOutermostPackage().refPackage("org:openmdx:test:datatypes1");
        System.out.println("Validating state1 Segment...");
        Segment segment = (Segment) provider.getSegment(segmentName);
        for(
            int i = 0;
            i < this.values.length;
            i++
        ){
            NonStated nonStated = segment.getNonStated(String.valueOf(i));
            validateData(nonStated, this.values[i]);
            NonStatedQuery query = datatypes1Package.createNonStatedQuery();
            query.value6().like((String)this.values[i][VALUE6]);
            List<NonStated> list = segment.getNonStated(query);
            assertEquals("Query result size", 1, list.size());
            nonStated = (NonStated) list.get(0);
            validateData(nonStated, this.values[i]);
        }
    }

    /**
     * Populate Data objects
     * 
     * @param data
     * @param source
     */
    protected void validateData(
        Data data,
        Object[] source
    ){
        assertEquals("value1", (Boolean)source[VALUE1], Boolean.valueOf(data.isValue1()));
        assertEquals("value2", ((Number)source[VALUE2]).shortValue(), data.getValue2());
        int[] expected3 = (int[]) source[VALUE3];        
        assertEquals("value3.size()", expected3.length, data.getValue3().size());            
        for(
            int i = 0;
            i < expected3.length;
            i++
        ) {
            assertEquals("value3[" + i + "]", expected3[i], data.getValue3().get(i).intValue());
        }
        assertEquals("value4", (Long)source[VALUE4], data.getValue4());
        assertEquals("value5", (BigDecimal) source[VALUE5], data.getValue5());
        assertEquals("value6", (String)source[VALUE6], data.getValue6());
        assertEquals("value7", (Date)source[VALUE7], data.getValue7());
        assertEquals("value8", (XMLGregorianCalendar)source[VALUE8], data.getValue8());
        assertEquals(
            "value9", 
            (URI)
            source[VALUE9], 
            data.getValue9()
        );
        byte[] expected10 = (byte[]) source[VALUE10];
        byte[] actual10 = data.getValue10();
        if(expected10.length == 0) {
            assertTrue("value10", actual10 == null || actual10.length == 0);
        } else {
            assertEquals("value10.length", expected10.length, actual10.length);            
            for(
                int i = 0;
                i < expected10.length;
                i++
            ) assertEquals("value10[" + i + "]", expected10[i], data.getValue10()[i]);
        }
        assertEquals("value11a", (Duration)source[VALUE11a], data.getValue11a());
        assertEquals("value11b", (Duration)source[VALUE11b], data.getValue11b());
    }
    
}
