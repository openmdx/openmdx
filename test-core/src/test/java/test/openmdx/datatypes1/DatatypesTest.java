/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Datatypes Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2021, OMEX AG, Switzerland
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
package test.openmdx.datatypes1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.naming.NamingException;
import javax.resource.cci.MappedRecord;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openmdx.application.xml.Exporter;
import org.openmdx.application.xml.Importer;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.rest.spi.RestParser;
import org.openmdx.base.rest.spi.RestSource;
import org.openmdx.junit5.OpenmdxTestCoreStandardExtension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.state2.spi.Order;
import org.w3c.cci2.ImmutableDatatype;
import org.w3c.format.DateTimeFormat;
import org.w3c.spi.DatatypeFactories;
import org.w3c.spi2.Datatypes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import test.openmdx.datatypes1.cci2.NonStatedQuery;
import test.openmdx.datatypes1.dto.CountryCode;
import test.openmdx.datatypes1.jmi1.Data;
import test.openmdx.datatypes1.jmi1.Datatypes1Package;
import test.openmdx.datatypes1.jmi1.NonStated;
import test.openmdx.datatypes1.jmi1.Segment;

/**
 * Datatypes Test
 */
@ExtendWith(OpenmdxTestCoreStandardExtension.class)
public class DatatypesTest  {

    @BeforeAll
    public static void createPersistenceManagerFactory(
    ) throws NamingException{
        entityManagerFactory = JDOHelper.getPersistenceManagerFactory(
            "test-Datatypes-EntityManagerFactory"
        );
    }

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
    protected static final int COUNTRY = VALUE_COUNT++;
    protected static final int SLICE_COUNT = 3;
    private static final int FETCH_SIZE_HUGE = 10000;

    protected static PersistenceManagerFactory entityManagerFactory;
    private Object[][] values;
    
    /**
     * Set up the test fixture
     * @throws IOException 
     * @throws URISyntaxException 
     */
    @BeforeEach
    public void setUp(
    ) throws ParseException, URISyntaxException{  
        DatatypeFactory datatypeFactory = DatatypeFactories.xmlDatatypeFactory();
        DateTimeFormat dateFormat = DateTimeFormat.BASIC_UTC_FORMAT;
        this.values = new Object[SLICE_COUNT][];
        for(
            int i = 0;
            i < this.values.length;
            i++
        ) this.values[i] = new Object[VALUE_COUNT];
        this.values[0][VALUE1] = Boolean.TRUE;
        this.values[0][VALUE2] = Short.valueOf(Short.MAX_VALUE);
        this.values[0][VALUE3] = new int[]{-12,12};
        this.values[0][VALUE4] = Long.valueOf(8000000000L);
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
        this.values[0][COUNTRY] = CountryCode.valueOf("ch");
        
        this.values[1][VALUE1] = Boolean.FALSE;
        this.values[1][VALUE2] = Short.valueOf(Short.MIN_VALUE);
        this.values[1][VALUE3] = new int[]{};
        this.values[1][VALUE4] = Long.valueOf(8000000000L);
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
        this.values[1][COUNTRY] = CountryCode.valueOf("de");
        
        this.values[2][VALUE1] = Boolean.FALSE;
        this.values[2][VALUE2] = Short.valueOf(Short.MIN_VALUE);
        this.values[2][VALUE3] = new int[]{};
        this.values[2][VALUE4] = Long.valueOf(8000000000L);
        this.values[2][VALUE5] = BigDecimal.valueOf(-1234, 2);
        this.values[2][VALUE6] = "an_undersocre";
        this.values[2][VALUE7] = dateFormat.parse("20061201T120000.123Z");
        this.values[2][VALUE8] = datatypeFactory.newXMLGregorianCalendarDate(2006,DatatypeConstants.DECEMBER,01,DatatypeConstants.FIELD_UNDEFINED);
        this.values[2][VALUE9] = new URI("mailto:info@openmdx.org");
        this.values[2][VALUE10] = new byte[]{};
        this.values[2][VALUE11a] = datatypeFactory.newDurationYearMonth("-P12Y6M");
        this.values[2][VALUE11b] = datatypeFactory.newDurationDayTime("-P5DT4H3M2.010S");
        this.values[2][VALUE11A] = datatypeFactory.newDurationYearMonth("-P150M");
        this.values[2][VALUE11B] = datatypeFactory.newDurationDayTime("-PT446582.010S");
        this.values[2][COUNTRY] = CountryCode.valueOf("li");
        
        System.out.println("Acquiring persistence manager factory...");
    }

    @Test
    public void testCR20019941() throws IOException, ClassNotFoundException{
        XMLGregorianCalendar original = Datatypes.create(XMLGregorianCalendar.class, "20000401");
        Assertions.assertTrue(original instanceof ImmutableDatatype<?>, "Date is immutable");
        XMLGregorianCalendar copy = copy(original);
        Assertions.assertNotSame(original.getClass(), copy.getClass(), "Immutable date gets mutable");
        Assertions.assertFalse(copy instanceof ImmutableDatatype<?>, "A Date's copy is mutable");
        Assertions.assertEquals("2000-04-01", copy.toXMLFormat());
        original = (XMLGregorianCalendar) original.clone();
        Assertions.assertFalse(original instanceof ImmutableDatatype<?>, "A Date's clone is mutable");
        copy = copy(original);
        Assertions.assertEquals("2000-04-01", copy.toXMLFormat());
        Assertions.assertSame(original.getClass(),  copy.getClass(), "Mutable date remains the mutable");
    }
    
    @Test
    public void testDate(){
        XMLGregorianCalendar firstOfApril = DatatypeFactories.immutableDatatypeFactory().toDate(
                DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(
                2009, 
                4, 
                1, 
                DatatypeConstants.FIELD_UNDEFINED
            )
        );
        Assertions.assertEquals(firstOfApril,  Datatypes.create(XMLGregorianCalendar.class, "2009-04-01"), "2009-04-01");
        Assertions.assertEquals(firstOfApril,  Datatypes.create(XMLGregorianCalendar.class, "20090401"), "20090401");
        Assertions.assertEquals(firstOfApril,  Datatypes.create(XMLGregorianCalendar.class, "090401"), "090401");
    }
    
    @Test
    public void testCountryCode(){
        CountryCode actual = Datatypes.create(CountryCode.class, "ch");
        CountryCode expected = CountryCode.valueOf("ch");
        Assertions.assertEquals(expected,  actual, "Datatypes");
    }
    
    @Test
    public void testCalendar(){
        Calendar calendar = Calendar.getInstance(UTC);
        calendar.clear();
        calendar.set(
          1995, // year
          00, // zero based month
          01, // day
          11, // hour
          55, // minute
          00 // second
        );
        Date fiveToTwelve = new Date(calendar.getTimeInMillis()); 
        Assertions.assertEquals("19950101T115500.000Z",  DateTimeFormat.BASIC_UTC_FORMAT.format(fiveToTwelve), "Basic Format");
        Assertions.assertEquals("1995-01-01T11:55:00.000Z",  DateTimeFormat.EXTENDED_UTC_FORMAT.format(fiveToTwelve), "Extended Format");
        Assertions.assertEquals(fiveToTwelve,  Datatypes.create(Date.class, "19950101T115500.000Z"), "Basic parser");
        Assertions.assertEquals(fiveToTwelve,  Datatypes.create(Date.class, "1995-01-01T11:55:00.000Z"), "Extended parser");
        Assertions.assertEquals(fiveToTwelve,  Datatypes.create(Date.class, "19950101T1155-00"), "Basic parser accepting reduced accuracy and alternative UTC identifier");
        Assertions.assertEquals(fiveToTwelve,  Datatypes.create(Date.class, "1995-01-01T11:55:00,000000Z"), "Extended parser accepting comma and extended accuracy");
        Assertions.assertEquals(fiveToTwelve,  Datatypes.create(Date.class, "95-01-01T11:55-00"), "Extended parser accepting two digit year");
        Assertions.assertEquals(fiveToTwelve,  Datatypes.create(Date.class, "950101T1155-00"), "Basic parser accepting two digit year");
    }
    
    @Test
    public void testDefault(
    ) throws Exception{
        try {
            storeDatatypes("Persistent", "Default");
            retrieveDatatypes("Persistent", "Default");
            exportDatatypes("Persistent", "Default");
            importDatatypes("Persistent", "Default");
            removeOptionalData("Persistent", "Default");
            doNotTouchWhenApplyingCurrentValue(retrieveData("Persistent", "Default"));
        } catch(Exception exception) {
            SysLog.error("Exception", exception);
            throw exception;
        }
    }

    @Test
    public void testNumeric(
    ) throws Exception{
        try {
            storeDatatypes("Persistent", "Numeric");
            retrieveDatatypes("Persistent", "Numeric");
            removeOptionalData("Persistent", "Numeric");
            doNotTouchWhenApplyingCurrentValue(retrieveData("Persistent", "Numeric"));
        } catch(Exception exception) {
            SysLog.error("Exception", exception);
            throw exception;
        }
    }

    @Test
    public void testNative(
    ) throws Exception{
        try {
            storeDatatypes("Persistent", "Native");
            retrieveDatatypes("Persistent", "Native");
            removeOptionalData("Persistent", "Native");
            doNotTouchWhenApplyingCurrentValue(retrieveData("Persistent", "Native"));
        } catch(Exception exception) {
            SysLog.error("Exception", exception);
            throw exception;
        }
    }

    @Test
    public void testCR10009964() throws Exception{
        storeBulk(
            "Persistent", 
            "Bulk", 
            DateTimeFormat.EXTENDED_UTC_FORMAT.parse("2009-09-08T20:00:09.000Z"), 
            60000L, // 60 s
            DateTimeFormat.EXTENDED_UTC_FORMAT.parse("2009-09-09T23:59:59.999Z"),
            Datatypes.create(XMLGregorianCalendar.class, "2012-09-08"), // Tue Sep 08 2009
            Datatypes.create(XMLGregorianCalendar.class, "2012-09-09"), // Wed Sep 09 2009
            Datatypes.create(XMLGregorianCalendar.class, "2012-09-10") // Thu Sep 10 2009
        );
        validateBulk(
            "Persistent", 
            "Bulk", 
            DateTimeFormat.EXTENDED_UTC_FORMAT.parse("2009-09-08T22:00:00.000Z"), // Wed Sep 09 00:00:00 CEST 2009 
            DateTimeFormat.EXTENDED_UTC_FORMAT.parse("2009-09-09T21:59:59.000Z"), // Wed Sep 09 23:59:59 CEST 2009
            Datatypes.create(XMLGregorianCalendar.class, "2012-09-09"), // Wed Sep 09 2009
            Datatypes.create(XMLGregorianCalendar.class, "2012-09-09"), // Wed Sep 09 2009
            24 * 60 / 3,
            Integer.valueOf(FetchPlan.FETCH_SIZE_OPTIMAL)
        );
        validateBulk(
            "Persistent", 
            "Bulk", 
            DateTimeFormat.EXTENDED_UTC_FORMAT.parse("2009-09-08T22:00:00.000Z"), // Wed Sep 09 00:00:00 CEST 2009 
            DateTimeFormat.EXTENDED_UTC_FORMAT.parse("2009-09-09T21:59:59.000Z"), // Wed Sep 09 23:59:59 CEST 2009
            Datatypes.create(XMLGregorianCalendar.class, "2012-09-09"), // Wed Sep 09 2009
            Datatypes.create(XMLGregorianCalendar.class, "2012-09-09"), // Wed Sep 09 2009
            24 * 60 / 3,
            Integer.valueOf(FETCH_SIZE_HUGE)
        );
        validateBulk(
            "Persistent", 
            "Bulk", 
            DateTimeFormat.EXTENDED_UTC_FORMAT.parse("2009-09-08T22:00:00.000Z"), // Wed Sep 09 00:00:00 CEST 2009 
            DateTimeFormat.EXTENDED_UTC_FORMAT.parse("2009-09-09T21:59:59.000Z"), // Wed Sep 09 23:59:59 CEST 2009
            Datatypes.create(XMLGregorianCalendar.class, "2012-09-09"), // Wed Sep 09 2009
            Datatypes.create(XMLGregorianCalendar.class, "2012-09-09"), // Wed Sep 09 2009
            24 * 60 / 3,
            Integer.valueOf(13)
        );
        validateBulk(
            "Persistent", 
            "Bulk", 
            DateTimeFormat.EXTENDED_UTC_FORMAT.parse("2009-09-08T22:00:00.000Z"), // Wed Sep 09 00:00:00 CEST 2009 
            DateTimeFormat.EXTENDED_UTC_FORMAT.parse("2009-09-09T21:59:59.000Z"), // Wed Sep 09 23:59:59 CEST 2009
            Datatypes.create(XMLGregorianCalendar.class, "2012-09-09"), // Wed Sep 09 2009
            Datatypes.create(XMLGregorianCalendar.class, "2012-09-09"), // Wed Sep 09 2009
            24 * 60 / 3,
            Integer.valueOf(FetchPlan.FETCH_SIZE_GREEDY)
        );
        validateBulk(
            "Persistent", 
            "Bulk", 
            DateTimeFormat.EXTENDED_UTC_FORMAT.parse("2009-09-08T22:00:00.000Z"), // Wed Sep 09 00:00:00 CEST 2009 
            DateTimeFormat.EXTENDED_UTC_FORMAT.parse("2009-09-09T21:59:59.000Z"), // Wed Sep 09 23:59:59 CEST 2009
            Datatypes.create(XMLGregorianCalendar.class, "2012-09-09"), // Wed Sep 09 2009
            Datatypes.create(XMLGregorianCalendar.class, "2012-09-09"), // Wed Sep 09 2009
            24 * 60 / 3,
            null
        );
    }

    /**
     * Enrich NullPointerException when accessing unset primitive values
     */
    @Test
    public void testCR20022958(
    ){
        PersistenceManager persistenceManager = entityManagerFactory.getPersistenceManager();
        Authority authority = (Authority) persistenceManager.getObjectById(
            Authority.class,
            Datatypes1Package.AUTHORITY_XRI
        );
        Datatypes1Package datatypes1Package = (Datatypes1Package) 
        	authority.refOutermostPackage().refPackage("test:openmdx:datatypes1");
        NonStated nonStated = datatypes1Package.getNonStated().createNonStated();
        try {
        	nonStated.isValue1();
        	Assertions.fail("NullPointerException expected");
        } catch (NullPointerException exception) {
        	final BasicException cause = Throwables.getCause(exception, null);
        	Assertions.assertEquals(BasicException.Code.ILLEGAL_STATE,  cause.getExceptionCode(), "exception-code");
        	Assertions.assertEquals("value1",  cause.getParameter("feature-name"), "feature-name");
        	Assertions.assertEquals(PrimitiveTypes.BOOLEAN,  cause.getParameter("feature-type"), "feature-type");
        	Assertions.assertEquals("test:openmdx:datatypes1:NonStated",  cause.getParameter("object-class"), "object-class");
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
        PersistenceManager persistenceManager = entityManagerFactory.getPersistenceManager();
        Transaction unitOfWork = persistenceManager.currentTransaction();
        Authority authority = (Authority) persistenceManager.getObjectById(
            Authority.class,
            Datatypes1Package.AUTHORITY_XRI
        );
        Datatypes1Package datatypes1Package = (Datatypes1Package) 
        authority.refOutermostPackage().refPackage("test:openmdx:datatypes1");
        //
        // Reset datatypes1 segment
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
        // Populate datatypes1 segment
        //
        unitOfWork.begin();
        for(
            int i = 0;
            i < this.values.length;
            i++
        ){
            NonStated nonStated = datatypes1Package.getNonStated().createNonStated();
            nonStated.getCountry();
            setData(nonStated, this.values[i]);
            nonStated.setSegment(segment);
            segment.addNonStated(false, String.valueOf(i), nonStated);
        }
        unitOfWork.commit();
    }

    /**
     * Clear and Populate
     * 
     * @param providerName
     * @param segmentName
     * @param begin the begin of the period to be filled with entries
     * @param step the "distance" between entries
     * @param end the end of the period to be filled with entries
     * @throws Exception
     */
    protected void storeBulk(
        String providerName, 
        String segmentName, 
        Date begin, 
        long step, 
        Date end,
        XMLGregorianCalendar... dates
    ) throws Exception {
        System.out.println("Acquire persistence manager...");
        PersistenceManager persistenceManager = entityManagerFactory.getPersistenceManager();
        Transaction unitOfWork = persistenceManager.currentTransaction();
        Authority authority = (Authority) persistenceManager.getObjectById(
            Authority.class,
            Datatypes1Package.AUTHORITY_XRI
        );
        Datatypes1Package datatypes1Package = (Datatypes1Package) 
        authority.refOutermostPackage().refPackage("test:openmdx:datatypes1");
        //
        // Reset datatypes1 segment
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
        // Populate datatypes1 segment
        //
        unitOfWork.begin();
        int i = 0;
        for(
            Date d =  begin;
            d.compareTo(end) <= 0;
            d = new Date(d.getTime() + step)
        ){
            NonStated nonStated = datatypes1Package.getNonStated().createNonStated();
            setData(nonStated, this.values[0]);
            nonStated.setValue7(d);
            if(dates != null & dates.length > 0) {
                nonStated.setValue8(dates[i++ % dates.length]);
            }
            nonStated.setSegment(segment);
            segment.addNonStated(true, UUID.randomUUID().toString(), nonStated);
        }
        unitOfWork.commit();
    }

    /**
     * Count
     * 
     * @param providerName
     * @param segmentName
     * @param dateTimeLowerBound the lower bound of the date/time selection
     * @param dateTimeUpperBound the upper bound of the date/time selection
     * @param dateLowerBound the lower bound of the date selection
     * @param dateUpperBound the upper bound of the date selection
     * @param expectedCount the expected number of entries in the selection
     * @param fetchSize the fetch size to be used, full segment scan if <code>null</code>
     * 
     * @throws Exception
     */
    protected void validateBulk(
        String providerName, 
        String segmentName,
        Date dateTimeLowerBound,
        Date dateTimeUpperBound,
        XMLGregorianCalendar dateLowerBound,
        XMLGregorianCalendar dateUpperBound,
        int expectedCount,
        Integer fetchSize
    ) throws Exception {
        System.out.println("Acquire persistence manager...");
        PersistenceManager persistenceManager = entityManagerFactory.getPersistenceManager();
        Authority authority = (Authority) persistenceManager.getObjectById(
            Authority.class,
            Datatypes1Package.AUTHORITY_XRI
        );
        Provider provider = authority.getProvider(providerName);
        System.out.println("Creating datatypes1 Segment...");
        Segment segment = (Segment) provider.getSegment(segmentName);
        int actualCount;
        if(fetchSize == null) {
            actualCount = 0;
            for(NonStated nonStated : segment.<NonStated>getNonStated()) {
                Date value7 = nonStated.getValue7();
                XMLGregorianCalendar value8 = nonStated.getValue8();
                if(
                    Order.compareValidFrom(value7, dateTimeLowerBound) >= 0 && Order.compareInvalidFrom(value7, dateTimeUpperBound) <= 0 &&
                    Order.compareValidFrom(value8, dateLowerBound) >= 0 && Order.compareValidTo(value8, dateUpperBound) <= 0
                ) {
                    actualCount++;
                }
            }
        } else {
            NonStatedQuery query = (NonStatedQuery) persistenceManager.newQuery(NonStated.class);
            query.value7().between(dateTimeLowerBound, dateTimeUpperBound);
            query.value8().between(dateLowerBound, dateUpperBound);
            ((Query)query).getFetchPlan().setFetchSize(fetchSize.intValue());
            actualCount = segment.getNonStated(query).size();
        }
        Assertions.assertEquals(expectedCount,  actualCount, "Number of entries in the range [" + DateTimeFormat.BASIC_UTC_FORMAT.format(dateTimeLowerBound) + ".." + DateTimeFormat.BASIC_UTC_FORMAT.format(dateTimeUpperBound) + "]");
    } 
    
    /**
     * Clear and Populate
     * 
     * @param providerName
     * @param segmentName
     * 
     * @throws Exception
     */
    protected void removeOptionalData(
        String providerName, 
        String segmentName
    ) throws Exception {
        System.out.println("Acquire persistence manager...");
        PersistenceManager persistenceManager = entityManagerFactory.getPersistenceManager();
        Transaction unitOfWork = persistenceManager.currentTransaction();
        Authority authority = (Authority) persistenceManager.getObjectById(
            Authority.class,
            Datatypes1Package.AUTHORITY_XRI
        );
        //
        // Reset datatypes1 segment
        //
        Provider provider = authority.getProvider(providerName);
        System.out.println("Creating datatypes1 Segment...");
        Segment segment = (Segment) provider.getSegment(segmentName);
        NonStated nonStated = segment.getNonStated("0");
        this.validateOptionalData(nonStated, false);
        unitOfWork.begin();
        this.clearOptionalValues(nonStated);
        this.validateOptionalData(nonStated, true);
        unitOfWork.commit();
        this.validateOptionalData(nonStated, true);
        
    }

    /**
     * Clear and Populate
     * 
     * @param providerName
     * @param segmentName
     * 
     * @throws Exception
     */
    protected NonStated retrieveData(
        String providerName, 
        String segmentName
    ) throws Exception {
        System.out.println("Acquire persistence manager...");
        PersistenceManager persistenceManager = entityManagerFactory.getPersistenceManager();
        Authority authority = (Authority) persistenceManager.getObjectById(
            Authority.class,
            Datatypes1Package.AUTHORITY_XRI
        );
        //
        // Reset datatypes1 segment
        //
        Provider provider = authority.getProvider(providerName);
        System.out.println("Creating datatypes1 Segment...");
        Segment segment = (Segment) provider.getSegment(segmentName);
        NonStated nonStated = segment.getNonStated("0");
        this.validateOptionalData(nonStated, true);
        return nonStated;
    }
    
    private void doNotTouchWhenApplyingCurrentValue(
        NonStated nonStated        
    ) throws Exception {
        final Transaction currentTransaction = JDOHelper.getPersistenceManager(nonStated).currentTransaction();
        Assertions.assertFalse(JDOHelper.isDirty(nonStated));
        System.out.println("Touch with same value");
        currentTransaction.begin();
        touchEvenWhenApplyingCurrentRequiredValue(nonStated);
        currentTransaction.rollback();
        Assertions.assertFalse(JDOHelper.isDirty(nonStated));
        currentTransaction.begin();
        doNotTouchWhenApplyingCurrentListValue(nonStated);
        currentTransaction.rollback();
        Assertions.assertFalse(JDOHelper.isDirty(nonStated));
    }

    /**
     * @param nonStated
     */
    private void touchEvenWhenApplyingCurrentRequiredValue(
        NonStated nonStated
    ) {
        final boolean oldValue = nonStated.isValue1();
        nonStated.setValue1(oldValue);
        Assertions.assertTrue(JDOHelper.isDirty(nonStated));
    }

    /**
     * @param nonStated
     */
    private void doNotTouchWhenApplyingCurrentListValue(
        NonStated nonStated
    ) {
        final List<Integer> value = nonStated.getValue3();
        value.remove(Integer.valueOf(47));
        Assertions.assertFalse(JDOHelper.isDirty(nonStated));
        value.remove(Integer.valueOf(12));
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
     * Populate Data objects
     * 
     * @param data
     * @param source
     */
    protected void setData(
        NonStated data,
        Object[] source
    ){
        data.setCountry((CountryCode) source[COUNTRY]);
        setData((Data)data, source);
    }
    
    /**
     * Clear the optional values
     * 
     * @param data
     */
    protected void clearOptionalValues(
        Data data
    ){
        data.setValue4(null);
        data.setValue9(null);
        data.setValue11a(null);
    }
    
    protected void retrieveDatatypes(
        String providerName, 
        String segmentName
    ) throws Exception {
        System.out.println("Acquire persistence manager...");
        PersistenceManager persistenceManager = entityManagerFactory.getPersistenceManager();
        Authority authority = (Authority) persistenceManager.getObjectById(
            Authority.class,
            Datatypes1Package.AUTHORITY_XRI
        );
        //
        // Reset datatypes1 segment
        //
        Provider provider = authority.getProvider(providerName);
        Datatypes1Package datatypes1Package = (Datatypes1Package) authority.refOutermostPackage().refPackage("test:openmdx:datatypes1");
        System.out.println("Validating datatypes1 Segment...");
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
            Assertions.assertEquals(1,  list.size(), "Query result size");
            nonStated = (NonStated) list.get(0);
            validateData(nonStated, this.values[i]);
        }
    }

    protected void exportDatatypes(
        String providerName, 
        String segmentName
    ) throws Exception {
        System.out.println("Acquire persistence manager...");
        PersistenceManager persistenceManager = entityManagerFactory.getPersistenceManager();
        Authority authority = (Authority) persistenceManager.getObjectById(
            Authority.class,
            Datatypes1Package.AUTHORITY_XRI
        );
        Provider provider = authority.getProvider(providerName);
        Segment segment = (Segment) provider.getSegment(segmentName);
        File file = File.createTempFile("data", ".zip");
        Exporter.export(
            Exporter.asTarget(file, Exporter.MIME_TYPE_XML),
            persistenceManager,
            null,
            segment.refGetPath()
        );
        System.out.println(segment.refGetPath() + " exported to " + file);
    }

    protected void importDatatypes(
        String providerName, 
        String segmentName
    ) throws Exception {
        System.out.println("Acquire persistence manager...");
        PersistenceManager persistenceManager = entityManagerFactory.getPersistenceManager();
        persistenceManager.currentTransaction().begin();
        Importer.importObjects(
            Importer.asTarget(persistenceManager),
            Importer.asSource(
                new URL("xri://+resource/test/openmdx/datatypes1/data.xml")
            )
        );
        persistenceManager.currentTransaction().rollback();
    }
    
    
    protected void validateData(
        Data data,
        Object[] source
    ){
        Assertions.assertEquals((Boolean)source[VALUE1],  Boolean.valueOf(data.isValue1()), "value1");
        Assertions.assertEquals((int) ((Number)source[VALUE2]).shortValue(),  (int) data.getValue2(), "value2");
        int[] expected3 = (int[]) source[VALUE3];        
        Assertions.assertEquals(expected3.length,  data.getValue3().size(), "value3.size()");            
        for(
            int i = 0;
            i < expected3.length;
            i++
        ) {
            Assertions.assertEquals(expected3[i],  data.getValue3().get(i).intValue(), "value3[" + i + "]");
        }
        Assertions.assertEquals((Long)source[VALUE4],   data.getValue4(), "value4");
        Assertions.assertEquals(((BigDecimal) source[VALUE5]).doubleValue(),  data.getValue5().doubleValue(), 0.0, "value5");
        Assertions.assertEquals((String)source[VALUE6],  data.getValue6(), "value6");
        Assertions.assertEquals((Date)source[VALUE7],  data.getValue7(), "value7");
        Assertions.assertEquals((XMLGregorianCalendar)source[VALUE8],  data.getValue8(), "value8");
        Assertions.assertEquals((URI)
		source[VALUE9],  data.getValue9(), "value9");
        byte[] expected10 = (byte[]) source[VALUE10];
        byte[] actual10 = data.getValue10();
        if(expected10.length == 0) {
            Assertions.assertTrue(actual10 == null || actual10.length == 0, "value10");
        } else {
            Assertions.assertEquals(expected10.length,  actual10.length, "value10.length");            
            for(
                int i = 0;
                i < expected10.length;
                i++
            )
				Assertions.assertEquals((int) expected10[i],  (int) data.getValue10()[i], "value10[" + i + "]");
        }
        Assertions.assertEquals((Duration)source[VALUE11a],  data.getValue11a(), "value11a");
        Assertions.assertEquals((Duration)source[VALUE11b],  data.getValue11b(), "value11b");
    }

    protected void validateOptionalData(
        Data data,
        boolean empty
    ){
        Assertions.assertEquals( empty,   (data.getValue4() == null), "value4");
        Assertions.assertEquals( empty,   (data.getValue4() == null), "value9");
        Assertions.assertEquals( empty,   (data.getValue11a() == null), "value11a");
    }
    
    @AfterAll
    public static void closePersistenceManagerFactory(
    ) throws IOException{
        if(entityManagerFactory instanceof Closeable) {
            ((Closeable)entityManagerFactory).close();
        }
    }

    @Test
    public void testDuration(){
        Duration t = DatatypeFactories.xmlDatatypeFactory().newDuration(
            true, 
            0, // years 
            0, // months
            0, // days
            0, // hours
            70, // minutes
            0 // seconds
        );
        Assertions.assertEquals("P0Y0M0DT0H70M0S",  t.toString(), "70 min");
    }

    @Test
    public void testStruct(
    ) throws SAXException {
        RestSource restSource = new RestSource(
            "./",           
            new InputSource("xri://+resource/test/openmdx/datatypes1/GetConfigResultT.xml"),
            "application/xml",
            null
        );
        MappedRecord getConfigResultT = RestParser.parseRequest(
            restSource,
            null // no XRI struct
        );
        Assertions.assertNotNull(getConfigResultT, "GetConfigResultT");
    }

    @SuppressWarnings("unchecked")
	private <T> T copy(T t) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutput target = new ObjectOutputStream(buffer);
        target.writeObject(t);
        ObjectInput source = new ObjectInputStream(
            new ByteArrayInputStream(buffer.toByteArray())
        );
        return (T) source.readObject();
    }
    
}
