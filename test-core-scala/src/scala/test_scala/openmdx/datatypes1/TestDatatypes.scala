/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestDatatypes.scala,v 1.1 2010/11/26 14:05:38 wfro Exp $
 * Description: Test Oracle
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/11/26 14:05:38 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2010, OMEX AG, Switzerland
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
package test_scala.openmdx.datatypes1;

import javax.jdo._
import javax.naming._
import org.junit._
import Assert._

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openmdx.application.xml.Exporter;
import org.openmdx.application.xml.Importer;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.kernel.lightweight.naming.NonManagedInitialContextFactoryBuilder;
import org.openmdx.kernel.log.SysLog;
import org.w3c.format.DateTimeFormat;
import org.w3c.spi.DatatypeFactories;
import org.w3c.spi2.Datatypes;

import test.openmdx.datatypes1.cci2.NonStatedQuery;
import test.openmdx.datatypes1.jmi1.Data;
import test.openmdx.datatypes1.jmi1.Datatypes1Package;
import test.openmdx.datatypes1.jmi1.NonStated;
import test.openmdx.datatypes1.jmi1.Segment;

/**
 * Test Database Extension
 */
class TestDatatypes  {

    val UTC: TimeZone = TimeZone.getTimeZone("UTC");
    
    val VALUE_COUNT: Int = 15;
    val VALUE1: Int = 1;
    val VALUE2: Int = 2; 
    val VALUE3: Int = 3;
    val VALUE4: Int = 4;
    val VALUE5: Int = 5;
    val VALUE6: Int = 6;
    val VALUE7: Int = 7;
    val VALUE8: Int = 8;
    val VALUE9: Int = 9;
    val VALUE10: Int = 10;
    val VALUE11A: Int = 11;
    val VALUE11B: Int = 12;
    val VALUE11a: Int = 13;
    val VALUE11b: Int = 14;
    val SLICE_COUNT: Int = 3;

    var values: Array[Array[Object]] = null;
    
    /**
     * Set up the test fixture
     * @throws IOException 
     * @throws URISyntaxException 
     */
    @Before
    def setUp(
    ) =  {  
        val datatypeFactory: DatatypeFactory = DatatypeFactories.xmlDatatypeFactory();
        val dateFormat: DateTimeFormat = DateTimeFormat.BASIC_UTC_FORMAT;
        this.values = new Array(SLICE_COUNT);
        var i: Int = 0;
        while(i < this.values.length) {
        	this.values(i) = new Array[Object](VALUE_COUNT);
        	i += 1;
        }
        this.values(0)(VALUE1) = java.lang.Boolean.TRUE;
        this.values(0)(VALUE2) = new java.lang.Short(java.lang.Short.MAX_VALUE);
        this.values(0)(VALUE3) = java.util.Arrays.asList(-12, 12);
        this.values(0)(VALUE4) = new java.lang.Long(8000000000L);
        this.values(0)(VALUE5) = BigDecimal.valueOf(1234, 2);
        this.values(0)(VALUE6) = "ABC";
        this.values(0)(VALUE7) = dateFormat.parse("20060601T120000.123Z");
        this.values(0)(VALUE8) = datatypeFactory.newXMLGregorianCalendarDate(2006,DatatypeConstants.APRIL,01,DatatypeConstants.FIELD_UNDEFINED);
        this.values(0)(VALUE9) = new URI("http://www.openmdx.org");
        val bytes: Array[Byte] = new Array[Byte](3);
        bytes(0) = java.lang.Byte.MIN_VALUE; 
        bytes(1) = 0; 
        bytes(2) = java.lang.Byte.MAX_VALUE;
        this.values(0)(VALUE10) = bytes;
        this.values(0)(VALUE11A) = datatypeFactory.newDurationYearMonth("P150M");
        this.values(0)(VALUE11B) = datatypeFactory.newDurationDayTime("PT446582.010S");
        this.values(0)(VALUE11a) = datatypeFactory.newDurationYearMonth("P12Y6M");
        this.values(0)(VALUE11b) = datatypeFactory.newDurationDayTime("P5DT4H3M2.010S");
        this.values(1)(VALUE1) = java.lang.Boolean.FALSE;
        this.values(1)(VALUE2) = new java.lang.Short(java.lang.Short.MIN_VALUE);
        this.values(1)(VALUE3) = java.util.Arrays.asList();
        this.values(1)(VALUE4) = new java.lang.Long(8000000000L);
        this.values(1)(VALUE5) = BigDecimal.valueOf(-1234, 2);
        this.values(1)(VALUE6) = "qwerty";
        this.values(1)(VALUE7) = dateFormat.parse("29900101T000000.000Z");
        this.values(1)(VALUE8) = datatypeFactory.newXMLGregorianCalendarDate(2990,DatatypeConstants.JANUARY,01,DatatypeConstants.FIELD_UNDEFINED);
        this.values(1)(VALUE9) = new URI("mailto:info@openmdx.org");
        this.values(1)(VALUE10) = new Array[Byte](0);
        this.values(1)(VALUE11A) = datatypeFactory.newDurationYearMonth("-P150M");
        this.values(1)(VALUE11B) = datatypeFactory.newDurationDayTime("-PT446582.010S");
        this.values(1)(VALUE11a) = datatypeFactory.newDurationYearMonth("-P12Y6M");
        this.values(1)(VALUE11b) = datatypeFactory.newDurationDayTime("-P5DT4H3M2.010S");
        this.values(2)(VALUE1) = java.lang.Boolean.FALSE;
        this.values(2)(VALUE2) = new java.lang.Short(java.lang.Short.MIN_VALUE);
        this.values(2)(VALUE3) = java.util.Arrays.asList();
        this.values(2)(VALUE4) = new java.lang.Long(8000000000L);
        this.values(2)(VALUE5) = BigDecimal.valueOf(-1234, 2);
        this.values(2)(VALUE6) = "an_undersocre";
        this.values(2)(VALUE7) = dateFormat.parse("20061201T120000.123Z");
        this.values(2)(VALUE8) = datatypeFactory.newXMLGregorianCalendarDate(2006,DatatypeConstants.DECEMBER,01,DatatypeConstants.FIELD_UNDEFINED);
        this.values(2)(VALUE9) = new URI("mailto:info@openmdx.org");
        this.values(2)(VALUE10) = new Array[Byte](0);
        this.values(2)(VALUE11A) = datatypeFactory.newDurationYearMonth("-P12Y6M");
        this.values(2)(VALUE11B) = datatypeFactory.newDurationDayTime("-P5DT4H3M2.010S");
        this.values(2)(VALUE11a) = datatypeFactory.newDurationYearMonth("-P150M");
        this.values(2)(VALUE11b) = datatypeFactory.newDurationDayTime("-PT446582.010S");
        System.out.println("Acquiring persistence manager factory...");
    }

    @Test
    def testDate() = {
        val firstOfApril: XMLGregorianCalendar = DatatypeFactories.immutableDatatypeFactory().toDate(
                DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendarDate(
                2009, 
                4, 
                1, 
                DatatypeConstants.FIELD_UNDEFINED
            )
        );
        assertEquals(
            "2009-04-01",
            firstOfApril,
            Datatypes.create(classOf[XMLGregorianCalendar], "2009-04-01")
        );
        assertEquals(
            "20090401",
            firstOfApril,
            Datatypes.create(classOf[XMLGregorianCalendar], "20090401")
        );
        assertEquals(
            "090401",
            firstOfApril,
            Datatypes.create(classOf[XMLGregorianCalendar], "090401")
        );
    }
    
    @Test
    def testCalendar() = {
        val calendar: Calendar = Calendar.getInstance(UTC);
        calendar.clear();
        calendar.set(
          1995, // year
          00, // zero based month
          01, // day
          11, // hour
          55, // minute
          00 // second
        );
        val fiveToTwelve: Date = new Date(calendar.getTimeInMillis()); 
        assertEquals(
            "Basic Format",
            "19950101T115500.000Z", 
            DateTimeFormat.BASIC_UTC_FORMAT.format(fiveToTwelve)
        );
        assertEquals(
            "Extended Format",
            "1995-01-01T11:55:00.000Z", 
            DateTimeFormat.EXTENDED_UTC_FORMAT.format(fiveToTwelve)
        );
        assertEquals(
            "Basic parser",
            fiveToTwelve,
            Datatypes.create(classOf[Date], "19950101T115500.000Z")
        );
        assertEquals(
            "Extended parser",
            fiveToTwelve,
            Datatypes.create(classOf[Date], "1995-01-01T11:55:00.000Z")
        );
        assertEquals(
            "Basic parser accepting reduced accuracy and alternative UTC identifier",
            fiveToTwelve,
            Datatypes.create(classOf[Date], "19950101T1155-00")
        );
        assertEquals(
            "Extended parser accepting comma and extended accuracy",
            fiveToTwelve,
            Datatypes.create(classOf[Date], "1995-01-01T11:55:00,000000Z")
        );
        assertEquals(
            "Extended parser accepting two digit year",
            fiveToTwelve,
            Datatypes.create(classOf[Date], "95-01-01T11:55-00")
        );
        assertEquals(
            "Basic parser accepting two digit year",
            fiveToTwelve,
            Datatypes.create(classOf[Date], "950101T1155-00")
        );
    }
    
    @Test
    def testDefault(
    ) = {
        try {
            storeDatatypes("Persistent", "Default");
            retrieveDatatypes("Persistent", "Default");
            exportDatatypes("Persistent", "Default");
            importDatatypes("Persistent", "Default");
            removeOptionalData("Persistent", "Default");
            retrieveOptionalData("Persistent", "Default");
        } catch {
        	case exception: Exception =>
	            SysLog.error("Exception", exception);
	            throw exception;
        }
    }

    @Test
    def testNumeric(
    ) = {
        try {
            storeDatatypes("Persistent", "Numeric");
            retrieveDatatypes("Persistent", "Numeric");
            removeOptionalData("Persistent", "Numeric");
            retrieveOptionalData("Persistent", "Numeric");
        } catch {
        	case exception: Exception =>
	            SysLog.error("Exception", exception);
	            throw exception;
        }
    }

    @Ignore // TODO re-activate  
    @Test
    def testNative(
    ) = {
        try {
            storeDatatypes("Persistent", "Native");
            retrieveDatatypes("Persistent", "Native");
            removeOptionalData("Persistent", "Native");
            retrieveOptionalData("Persistent", "Native");
        } catch {
        	case exception: Exception => 
	            SysLog.error("Exception", exception);
	            throw exception;
        }
    }

//    @Test
//    public void testVolatile(
//    ) throws Exception{
//        try {
//            storeDatatypes("Volatile", "Native");
//            retrieveDatatypes("Volatile", "Native");
//        } catch(Exception exception) {
//            SysLog.error("Exception", exception);
//            throw exception;
//        }
//    }

    /**
     * Clear and Populate
     * 
     * @param providerName
     * @param segmentName
     * 
     * @throws Exception
     */
    def storeDatatypes(
        providerName: String, 
        segmentName: String
    ) = {
        System.out.println("Acquire persistence manager...");
        val persistenceManager: PersistenceManager = TestDatatypes.entityManagerFactory.getPersistenceManager();
        val unitOfWork: Transaction = persistenceManager.currentTransaction();
        val authority: Authority = persistenceManager.getObjectById(
            classOf[Authority],
            Datatypes1Package.AUTHORITY_XRI
        );
        val datatypes1Package: Datatypes1Package =  
        	authority.refOutermostPackage().refPackage("test:openmdx:datatypes1").asInstanceOf[Datatypes1Package]
        //
        // Reset workshop1 segment
        //
        val provider: Provider = authority.getProvider(providerName);
        System.out.println("Creating datatypes1 Segment...");
        var segment: Segment = provider.getSegment(segmentName).asInstanceOf[Segment]
        if(segment != null) {
            unitOfWork.begin();
            segment.refDelete();
            unitOfWork.commit();
        }
        unitOfWork.begin();
        segment = datatypes1Package.getSegment().createSegment()
        provider.addSegment(false, segmentName, segment);
        unitOfWork.commit();
        //
        // Populate state1 segment
        //
        unitOfWork.begin();
        var i: Int = 0;
        while(i < this.values.length) {
            val nonStated: NonStated = datatypes1Package.getNonStated().createNonStated();
            setData(nonStated, this.values(i));
            segment.addNonStated(false, String.valueOf(i), nonStated);
            i += 1;
        }
        unitOfWork.commit();
    }

    /**
     * Clear and Populate
     * 
     * @param providerName
     * @param segmentName
     * 
     * @throws Exception
     */
    def removeOptionalData(
        providerName: String, 
        segmentName: String
    ) = {
        System.out.println("Acquire persistence manager...");
        val persistenceManager: PersistenceManager = TestDatatypes.entityManagerFactory.getPersistenceManager();
        val unitOfWork: Transaction = persistenceManager.currentTransaction();
        val authority: Authority = persistenceManager.getObjectById(
            classOf[Authority],
            Datatypes1Package.AUTHORITY_XRI
        );
        //
        // Reset workshop1 segment
        //
        val provider: Provider = authority.getProvider(providerName);
        System.out.println("Creating datatypes1 Segment...");
        val segment: Segment = provider.getSegment(segmentName).asInstanceOf[Segment]
        val nonStated: NonStated = segment.getNonStated("0");
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
    def retrieveOptionalData(
        providerName: String, 
        segmentName: String
    ) = {
        System.out.println("Acquire persistence manager...");
        val persistenceManager: PersistenceManager = TestDatatypes.entityManagerFactory.getPersistenceManager();
        val authority: Authority = persistenceManager.getObjectById(
        	classOf[Authority],
            Datatypes1Package.AUTHORITY_XRI
        );
        //
        // Reset workshop1 segment
        //
        val provider: Provider = authority.getProvider(providerName);
        System.out.println("Creating datatypes1 Segment...");
        val segment: Segment = provider.getSegment(segmentName).asInstanceOf[Segment];
        val nonStated: NonStated = segment.getNonStated("0");
        this.validateOptionalData(nonStated, true);
    }
    
    
    /**
     * Populate Data objects
     * 
     * @param data
     * @param source
     */
    def setData(
        data: Data,
        source: Array[Object] 
    ) = {
        data.setValue1((source(VALUE1).asInstanceOf[Boolean]).booleanValue());
        data.setValue2((source(VALUE2).asInstanceOf[Number]).shortValue());
        data.setValue3(source(VALUE3).asInstanceOf[java.util.List[Integer]]);
        data.setValue4(source(VALUE4).asInstanceOf[Long]);
        data.setValue5(source(VALUE5).asInstanceOf[BigDecimal]);
        data.setValue6(source(VALUE6).asInstanceOf[String]);
        data.setValue7(source(VALUE7).asInstanceOf[Date]);
        data.setValue8(source(VALUE8).asInstanceOf[XMLGregorianCalendar]);
        data.setValue9(source(VALUE9).asInstanceOf[URI]);
        data.setValue10(source(VALUE10).asInstanceOf[Array[Byte]]);
        data.setValue11a(source(VALUE11A).asInstanceOf[Duration]);
        data.setValue11b(source(VALUE11B).asInstanceOf[Duration]);
    }

    /**
     * Clear the optional values
     * 
     * @param data
     */
    def clearOptionalValues(
        data: Data 
    ) = {
        data.setValue4(null);
        data.setValue9(null);
        data.setValue11a(null);
    }
    
    def retrieveDatatypes(
        providerName: String, 
        segmentName: String
    ) = {
        System.out.println("Acquire persistence manager...");
        val persistenceManager: PersistenceManager = TestDatatypes.entityManagerFactory.getPersistenceManager();
        val authority: Authority = persistenceManager.getObjectById(
            classOf[Authority],
            Datatypes1Package.AUTHORITY_XRI
        );
        //
        // Reset workshop1 segment
        //
        val provider: Provider = authority.getProvider(providerName).asInstanceOf[Provider]
        val datatypes1Package: Datatypes1Package = authority.refOutermostPackage().refPackage("test:openmdx:datatypes1").asInstanceOf[Datatypes1Package]
        System.out.println("Validating state1 Segment...");
        val segment: Segment = provider.getSegment(segmentName).asInstanceOf[Segment]
        var i = 0;
        while(i < this.values.length) {
            var nonStated: NonStated = segment.getNonStated(String.valueOf(i)).asInstanceOf[NonStated]
            validateData(nonStated, this.values(i));
            val query: NonStatedQuery = datatypes1Package.createNonStatedQuery();
            query.value6().like(this.values(i)(VALUE6).asInstanceOf[String]);
            val list: List[NonStated] = segment.getNonStated(query);
            assertEquals("Query result size", 1, list.size());
            nonStated = list.get(0);
            validateData(nonStated, this.values(i));
            i += 1;
        }
    }

    def exportDatatypes(
        providerName: String, 
        segmentName: String
    ) = {
        System.out.println("Acquire persistence manager...");
        val persistenceManager: PersistenceManager = TestDatatypes.entityManagerFactory.getPersistenceManager();
        val authority: Authority = persistenceManager.getObjectById(
        	classOf[Authority],
            Datatypes1Package.AUTHORITY_XRI
        );
        val provider: Provider = authority.getProvider(providerName);
        val segment: Segment = provider.getSegment(segmentName).asInstanceOf[Segment]
        val file: File = File.createTempFile("data", ".zip");
        Exporter.export(
            Exporter.asTarget(file, Exporter.MIME_TYPE_XML),
            persistenceManager,
            null,
            segment.refGetPath()
        );
        System.out.println(segment.refGetPath().toXRI() + " exported to " + file);
    }

    def importDatatypes(
        providerName: String, 
        segmentName: String
    ) = {
        System.out.println("Acquire persistence manager...");
        val persistenceManager: PersistenceManager = TestDatatypes.entityManagerFactory.getPersistenceManager();
        persistenceManager.currentTransaction().begin();
        Importer.importObjects(
            Importer.asTarget(persistenceManager),
            Importer.asSource(
                new URL("xri://+resource/test/openmdx/datatypes1/data.xml")
            )
        );
        persistenceManager.currentTransaction().rollback();
    }
    
    
    def validateData(
        data: Data ,
        source: Array[Object]
    ){
        assertEquals("value1", source(VALUE1), java.lang.Boolean.valueOf(data.isValue1()));
        assertEquals("value2", (source(VALUE2).asInstanceOf[Number]).shortValue(), data.getValue2());
        val expected3: java.util.List[Int] = source(VALUE3).asInstanceOf[java.util.List[Int]]    
        assertEquals("value3.size()", expected3.size(), data.getValue3().size());        
        var i = 0;
        while(i < expected3.size()) {
            assertEquals("value3[" + i + "]", expected3.get(i), data.getValue3().get(i).intValue());
            i += 1;
        }
        assertEquals("value4", source(VALUE4), data.getValue4());
        assertEquals("value5", (source(VALUE5).asInstanceOf[BigDecimal]).doubleValue(), data.getValue5().doubleValue(), 0.0);
        assertEquals("value6", source(VALUE6), data.getValue6());
        assertEquals("value7", source(VALUE7), data.getValue7());
        assertEquals("value8", source(VALUE8), data.getValue8());
        assertEquals(
            "value9", 
            source(VALUE9), 
            data.getValue9()
        );
        val expected10: Array[Byte] = source(VALUE10).asInstanceOf[Array[Byte]]
        val actual10: Array[Byte] = data.getValue10();
        if(expected10.length == 0) {
            assertTrue("value10", actual10 == null || actual10.length == 0);
        } else {
            assertEquals("value10.length", expected10.length, actual10.length);
            var i: Int = 0;
            while(i < expected10.length) {
            	assertEquals("value10[" + i + "]", expected10(i), data.getValue10()(i));
            	i += 1;
            }
        }
        assertEquals("value11a", source(VALUE11a), data.getValue11a());
        assertEquals("value11b", source(VALUE11b), data.getValue11b());
    }

   def validateOptionalData(
        data: Data,
        empty: Boolean
    ){
        assertEquals("value4", empty, data.getValue4() == null);
        assertEquals("value9", empty, data.getValue4() == null);
        assertEquals("value11a", empty, data.getValue11a() == null);
    }
    
    @Test
    def testDuration() = {
        val t: Duration = DatatypeFactories.xmlDatatypeFactory().newDuration(
            true, 
            0, // years 
            0, // months
            0, // days
            0, // hours
            70, // minutes
            0 // seconds
        );
        System.out.println("70 min: " + t);
    }

}

object TestDatatypes  {

    var entityManagerFactory: PersistenceManagerFactory = null;

    @BeforeClass
    def createPersistenceManagerFactory(
    ) = {
        entityManagerFactory = JDOHelper.getPersistenceManagerFactory(
            "test-Datatypes-EntityManagerFactory"
        );
        if(!NamingManager.hasInitialContextFactoryBuilder()) {
            NonManagedInitialContextFactoryBuilder.install(null);
        }
    }

    @AfterClass
    def closePersistenceManagerFactory(
    ) = {
        if(entityManagerFactory.isInstanceOf[Closeable]) {
            (entityManagerFactory.asInstanceOf[Closeable]).close();
        }
    }
    
}
