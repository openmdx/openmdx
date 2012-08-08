/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestJdbc_1.java,v 1.20 2009/02/04 11:06:38 hburger Exp $
 * Description: junit for jdbc persistence
 * Revision:    $Revision: 1.20 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/04 11:06:38 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.AttributeSpecifier;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.Directions;
import org.openmdx.application.dataprovider.cci.Orders;
import org.openmdx.application.dataprovider.cci.RequestCollection;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;

//---------------------------------------------------------------------------  
public class TestJdbc_1
  extends TestCase {

  //---------------------------------------------------------------------------  
  public TestJdbc_1(String name) {
      super(name);
  }  
  
  //--------------------------------------------------------------------------- 
  public static void main(
    String[] args
  ){
      TestRunner.run(suite());
  }
     
  //---------------------------------------------------------------------------    
  public static Test suite(
  ) {
    TestSuite suite = new TestSuite();
    for(
      Iterator<String> i = TestJdbc_1.providerNames.iterator();
      i.hasNext();
    ) {
      suite.addTest(new TestJdbc_1(i.next()));
    }
    return suite;
  }
  
  //---------------------------------------------------------------------------  
  protected void setUp(
  ) throws Exception {  
    System.out.println(">>>> **** Start Test: " + this.getName());
    providerPath = new Path("xri:@openmdx:org.openmdx.test/provider/" + this.getName());
    provider = null;
//  dataproviderConnectionfactory.createConnection();
  }

  //---------------------------------------------------------------------------  
  protected void tearDown(
  ) {
    try {
      String testName = "<<<< **** End Test: " + this.getName();        
      System.out.println(testName);
      AppLog.info(testName);
    }
    catch(Exception e) {
      System.out.println("error in deactivating");
    }
  }
  
  //---------------------------------------------------------------------------
  public void runTest(
  ) throws Throwable {
    testEverything();
  }

  //---------------------------------------------------------------------------  
  public void testEverything(
  ) throws Exception {
    doTestCreate();
    doTestExtent();
    doTestGet1();
    doTestFind();
    doTestModify();
    doTestGet2();
    doTestReplace();
    doTestGet3();
    doTestRemove();
  }

  //---------------------------------------------------------------------------  
  public void doTestMultiValuedModify() {
  
    AppLog.warning("*** testMultiValuedModify ***");
  
    if(_testMultiValuedModify) {
   
      try {
  
        RequestCollection requests = new RequestCollection(
          new ServiceHeader(), 
          provider
        );
  
        // test modify for multi-valued attributes
        Path p = new Path(providerPath.toXri() + "/segment/" + segmentName + "/container/99");
        DataproviderObject o = new DataproviderObject(p);
  
        // create object
        o.values("multiValued").add(".");
        o.values("multiValued").add(".");
        o.values("multiValued").add(".");
        o.values("multiValued").add(".");
        o.values("multiValued").add(".");
        o.values("multiValued").add(".");
        o.values("multiValued").add(".");
        o.values("multiValued").add(".");
        o.values("multiValued").add(".");
        o.values("multiValued").add(".");
        o.values("multiValued").add(".");
        o.values("multiValued").add(".");
        o.values("multiValued").add(".");
        o.values("multiValued").add(".");
        o.values("multiValued").add(".");
        o.values("multiValued").add(".");
        o.values("multiValued").add(".");
        o.values("object_class").add("org:openmdx:container1:Container");
  
        requests.addCreateRequest(o);
  
        // modify & verify object
        for(int i = 9; i >= 0; i--) {
          o.clearValues("multiValued");
          for(int j = i; j < 10; j++) {
            o.values("multiValued").set(j, Integer.toString(j));
          }
  
          // modify object
          DataproviderObject_1_0 modifyReply = requests.addReplaceRequest(
            o,
            AttributeSelectors.ALL_ATTRIBUTES,
            new AttributeSpecifier[]{}
          );
  
          for(int j = 0; j < 10; j++) {
            if(j >= i) {
              assertEquals("multiValued[" + j + "]", String.valueOf(j), modifyReply.values("multiValued").get(j));
            }
            else {
              assertEquals("multiValued[" + j + "]", ".", modifyReply.values("multiValued").get(j));
            }
          }
        }
  
        // remove object
        requests.addRemoveRequest(p);
      }
      catch(ServiceException ex) {
        ex.log();
        fail("Caught exception: ServiceException\n" + ex.toString());
      }
    }
  }
  //---------------------------------------------------------------------------  
  public void doTestCreate() {
  
    AppLog.warning("*** testCreate ***");
  
    if(_testCreate) {
  
      try {
  
        RequestCollection requests = new RequestCollection(
          new ServiceHeader(), 
          provider
        );
  
        for(int i = 0; i < _nRuns; i++) {
          Path p0 = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + i);
          DataproviderObject o0 = new DataproviderObject(p0);
          o0.values("object_class").add("org:openmdx:booking1:CompoundBooking");
          o0.values("cbType").add("13");
          o0.values("cancelsCB").add(new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + (i + 111)));
          o0.values("adviceText").add("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
          o0.values("adviceText").add("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");  
          requests.addCreateRequest(o0);
  
          // SingleLegBooking  
          for(int j = 0; j < 10; j++) {
            Path p1 = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + i + "/slb/" + j);
            DataproviderObject o1 = new DataproviderObject(p1);
            o1.values("object_class").add("org:openmdx:booking1:SingleLegBooking");
            o1.values("slbType").add("11");
            o1.values("pos").add(new Path(providerPath.toXri() + "/segment/" + segmentName + "/account/123456789012345678901234567890/bookedPosition/FI:" + j));
            o1.values("price").add(new BigDecimal(1000 * i + j));
            o1.values("isDebit").add(new BigDecimal(1));
            o1.values("isLong").add(new BigDecimal(0));
            o1.values("priceCurrency").add("CHF");
            o1.values("valueDate").add("20010101T120000X000Z");
            o1.values("bookingDate").add("20010101T120000X000Z");
            o1.values("quantity").add(new BigDecimal(i + j));
            o1.values("quantityAbsolute").add(new BigDecimal(0));
            o1.values("visibility").add("private");
            o1.values("administrativeDescription").add("test booking");
            o1.values("description").set(0, "<booking>Ihr Zahlungsauftrag vom 1.1.2000</booking>");
            o1.values("description").set(2, "additional description 2");
            o1.values("description").set(4, "additional description 4");
            o1.values("description").set(6, "additional description 6");
            o1.values("description").set(8, "additional description 8");
            if(this.getName().endsWith("Sliced")) {
              o1.values("credValue").add(new byte[]{0x55, 0x56, 0x57});
            }  
            requests.addCreateRequest(o1);
          }

          // provoke a DUPLICATE
          try {
            Path p1 = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + "0" + "/slb/" + "0");            
            DataproviderObject o1 = new DataproviderObject(p1);
            o1.values("object_class").add("org:openmdx:booking1:SingleLegBooking");
            o1.values("slbType").add("11");
            o1.values("pos").add(new Path(providerPath.toXri() + "/segment/" + segmentName + "/account/123456789012345678901234567890/bookedPosition/FI:" + "0"));
            o1.values("price").add(new BigDecimal(1000 * 0 + 0));
            o1.values("isDebit").add(new BigDecimal(1));
            o1.values("isLong").add(new BigDecimal(0));
            o1.values("priceCurrency").add("CHF");
            o1.values("valueDate").add("20010101T120000X000Z");
            o1.values("bookingDate").add("20010101T120000X000Z");
            o1.values("quantity").add(new BigDecimal(0 + 0));
            o1.values("quantityAbsolute").add(new BigDecimal(0));
            o1.values("visibility").add("private");
            o1.values("administrativeDescription").add("test booking");
            o1.values("description").set(0, "<booking>Ihr Zahlungsauftrag vom 1.1.2000</booking>");
            o1.values("description").set(2, "additional description 2");
            o1.values("description").set(4, "additional description 4");
            o1.values("description").set(6, "additional description 6");
            o1.values("description").set(8, "additional description 8");
            if(this.getName().endsWith("Sliced")) {
              o1.values("credValue").add(new byte[]{0x55, 0x56, 0x57});
            }  
            requests.addCreateRequest(o1);
            fail("expected DUPLICATE exception when creating " + p1.toUri());
          }
          catch(ServiceException e) {
            if(e.getExceptionCode() != BasicException.Code.DUPLICATE) {
              throw e;
            }
          }
        }

        // provoke a DUPLICATE
        try {
          Path p0 = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + "0");
          DataproviderObject o0 = new DataproviderObject(p0);
          o0.values("object_class").add("org:openmdx:booking1:CompoundBooking");
          o0.values("cbType").add("13");
          o0.values("cancelsCB").add(new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + ("0" + 111)));
          o0.values("adviceText").add("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
          o0.values("adviceText").add("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");  
          requests.addCreateRequest(o0);
          fail("expected DUPLICATE exception when creating " + p0.toUri());
        }
        catch(ServiceException e) {
          if(e.getExceptionCode() != BasicException.Code.DUPLICATE) {
            throw e;
          }
        }
      }
      catch(ServiceException ex) {
        ex.log();
        fail("Caught exception: ServiceException\n" + ex.toString());
      }      
    }
  }

  //---------------------------------------------------------------------------  
  public void doTestExtent() {
  
    AppLog.warning("*** testExtent ***");
  
    if(_testExtent) {
      try {
        RequestCollection requests = new RequestCollection(
          new ServiceHeader(), 
          provider
        );
        
        // get all SingleLegBooking from extent
        List<?> slbFindReply = requests.addFindRequest(
          providerPath.getDescendant(
            new String[]{
              "segment",
              segmentName,
              "extent"
            }
          ),
          new FilterProperty[]{
            new FilterProperty(
              Quantors.THERE_EXISTS,
              "identity",
              FilterOperators.IS_LIKE,
                providerPath.toUri() + "/segment/" + segmentName + "/cb/:*/slb/:*"
            )
          },
          AttributeSelectors.ALL_ATTRIBUTES,
          null,
          0,
          Integer.MAX_VALUE,
          Directions.ASCENDING  
        );
        for(Iterator<?> slbs = slbFindReply.iterator(); slbs.hasNext(); ) {
          slbs.next();
        }
        assertEquals("slbFindReply.size()", _nRuns * 10, slbFindReply.size());
      }
      catch(ServiceException ex) {
        ex.log();
        fail("Caught exception: ServiceException\n" + ex.toString());
      }
    }
  }
    
  //---------------------------------------------------------------------------    
  public void doTestGet1() {
  
    AppLog.warning("*** testGet1 ***");
  
    if(_testGet) {
      try {
  
        RequestCollection requests = new RequestCollection(
          new ServiceHeader(), 
          provider
        );
  
        for(int i = 0; i < _nRuns; i++) {
          Path p0 = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + i);
  
          DataproviderObject_1_0 o0 = requests.addGetRequest(
            p0,
            AttributeSelectors.ALL_ATTRIBUTES,
            new AttributeSpecifier[]{}
          );
    
          assertEquals("cb.object_class", o0.values("object_class").get(0), "org:openmdx:booking1:CompoundBooking");
          assertEquals("cb.cbType", o0.values("cbType").get(0), "13");
          assertEquals("cb.cancelsCB", o0.values("cancelsCB").get(0), new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + (i + 111)));
          assertEquals("cb.adviceText", o0.values("adviceText").get(0), "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
          assertEquals("cb.adviceText", o0.values("adviceText").get(1), "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");

          for(int j = 0; j < 10; j++) {
            Path p1 = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + i + "/slb/" + j);
  
            DataproviderObject_1_0 o1 = requests.addGetRequest(
              p1,
              AttributeSelectors.ALL_ATTRIBUTES,
              new AttributeSpecifier[]{}
            );
    
            assertEquals("slb.object_class", o1.values("object_class").get(0), "org:openmdx:booking1:SingleLegBooking");
            assertEquals("slb.slbType", o1.values("slbType").get(0), "11");
            assertEquals("slb.pos", o1.values("pos").get(0), new Path(providerPath.toXri() + "/segment/" + segmentName + "/account/123456789012345678901234567890/bookedPosition/FI:" + j));
            assertTrue("slb.price", new BigDecimal(1000 * i + j).compareTo((BigDecimal)o1.values("price").get(0)) == 0);
            assertEquals("slb.isDebit", ((BigDecimal)o1.values("isDebit").get(0)).intValue(), 1);
            assertEquals("slb.isLong", ((BigDecimal)o1.values("isLong").get(0)).intValue(), 0);
            assertEquals("slb.priceCurrency", o1.values("priceCurrency").get(0), "CHF");
            assertEquals("slb.valueDate", o1.values("valueDate").get(0), "20010101T120000X000Z");
            assertEquals("slb.bookingDate", o1.values("bookingDate").get(0), "20010101T120000X000Z");
            assertTrue("slb.quantity", new BigDecimal(i + j).compareTo((BigDecimal)o1.values("quantity").get(0)) == 0);
            assertTrue("slb.quantityAbsolute", new BigDecimal(0).compareTo((BigDecimal)o1.values("quantityAbsolute").get(0)) == 0);
            assertEquals("slb.visibility", o1.values("visibility").get(0), "private");
            assertEquals("slb.administrativeDescription",  "test booking", o1.values("administrativeDescription").get(0));
            assertEquals("slb.description", "<booking>Ihr Zahlungsauftrag vom 1.1.2000</booking>", o1.values("description").get(0));
            assertEquals("slb.description", null, o1.values("description").get(1));
            assertEquals("slb.description", "additional description 2", o1.values("description").get(2));
            assertEquals("slb.description", null, o1.values("description").get(3));
            assertEquals("slb.description", "additional description 4", o1.values("description").get(4));
            assertEquals("slb.description", null, o1.values("description").get(5));
            assertEquals("slb.description", "additional description 6", o1.values("description").get(6));
            assertEquals("slb.description", null, o1.values("description").get(7));
            assertEquals("slb.description", "additional description 8", o1.values("description").get(8));
            assertEquals("slb.description", null, o1.values("description").get(9));
            if(this.getName().endsWith("Sliced")) {
                byte[] credValue = (byte[])o1.values("credValue").get(0);
                assertEquals("credential.credValue[0]", credValue[0], 0x55);
                assertEquals("credential.credValue[1]", credValue[1], 0x56);
                assertEquals("credential.credValue[2]", credValue[2], 0x57);
            }
          }
        }
      }
      catch(ServiceException ex) {
        ex.log();
        fail("Caught exception: ServiceException\n" + ex.toString());
      }
    }
  }

  //---------------------------------------------------------------------------  
  public void doTestFind() {
  
    AppLog.warning("*** testFind ***");
  
    if(_testFind) {
  
      RequestCollection requests = new RequestCollection(
        new ServiceHeader(), 
        provider
      );
  
      try {
      
        FilterProperty p0, p1 = null;
    
        //
        // find CBs and SLBs
        //
        Path referenceFilterCB = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb");
  
        p0 = new FilterProperty(Quantors.THERE_EXISTS, "cbType", FilterOperators.IS_IN, "13");
        FilterProperty[] attributeFilterCB = new FilterProperty[]{p0};
  
        List<?> cbFindReply = requests.addFindRequest(
          referenceFilterCB,
          attributeFilterCB,
          AttributeSelectors.ALL_ATTRIBUTES,
          new AttributeSpecifier[]{
            new AttributeSpecifier("cbType", 0, Orders.ASCENDING)
          },
          0,
          Integer.MAX_VALUE,
          Directions.ASCENDING  
        );
  
        for(Iterator<?> cbs = cbFindReply.iterator(); cbs.hasNext(); ) {
          DataproviderObject o0 = (DataproviderObject)cbs.next();
  
          // find SLBs by cbType
          Path referenceFilterSLB = new Path(o0.path().toString() + "/slb");
  
          p0 = new FilterProperty(Quantors.THERE_EXISTS, "pos", FilterOperators.IS_LIKE,
              new Path(providerPath.toXri() + "/segment/" + segmentName + "/account/123456789012345678901234567890/bookedPosition/FI:%")
          );
          p1 = new FilterProperty(Quantors.THERE_EXISTS, "bookingDate", FilterOperators.IS_IN,"20010101T120000X000Z");
          FilterProperty[] attributeFilterSLB = new FilterProperty[]{p0, p1};
  
          List<?> slbFindReply = requests.addFindRequest(
            referenceFilterSLB,
            attributeFilterSLB,
              AttributeSelectors.ALL_ATTRIBUTES,
              new AttributeSpecifier[]{
              new AttributeSpecifier(SystemAttributes.CREATED_AT),
                new AttributeSpecifier("bookingDate")
              },
              0,
              Integer.MAX_VALUE,
              Directions.ASCENDING  
          );
  
          assertEquals("slbFindReply.size()", 10, slbFindReply.size());
          for(Iterator<?> slbs = slbFindReply.iterator(); slbs.hasNext(); ) {
            /*DataproviderObject o1 = (DataproviderObject)*/slbs.next();
          }
          
          // find SLBs by description
          p0 = new FilterProperty(Quantors.THERE_EXISTS, "description", FilterOperators.IS_IN, 
            "additional description 4"
          );
          p1 = new FilterProperty(Quantors.THERE_EXISTS, "bookingDate", FilterOperators.IS_IN, 
            "20010101T120000X000Z"
          );
          attributeFilterSLB = new FilterProperty[]{p0,p1};
  
          slbFindReply = requests.addFindRequest(
            referenceFilterSLB,
            attributeFilterSLB,
              AttributeSelectors.ALL_ATTRIBUTES,
              null,
              0,
              Integer.MAX_VALUE,
              Directions.ASCENDING  
          );
  
          assertEquals("slbFindReply.size()", 10, slbFindReply.size());
          for(Iterator<?> slbs = slbFindReply.iterator(); slbs.hasNext(); ) {
            /*DataproviderObject o1 = (DataproviderObject)*/slbs.next();
          }
        }

        /**
         * find2 - attribute existence
         */
        // tests for non-existence of attribute 'nonExistentAttribute'. Should return all objects
        // matching referenceFilter
        p0 = new FilterProperty(Quantors.FOR_ALL, "nonExistentAttribute", FilterOperators.IS_IN);
        FilterProperty[] attributeFilter = new FilterProperty[]{p0};
        Path referenceFilter = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb");
        try {
          List<?> findReply = requests.addFindRequest( 
            referenceFilter,
            attributeFilter
          );
          assertEquals("findReply.size()", 10, findReply.size());
        }
        catch(ServiceException e) {
            assertEquals(
            "find2.exception.code", 
            BasicException.Code.MEDIA_ACCESS_FAILURE, 
            e.getExceptionCode()
          );
        }
        
        // tests for non-existence of attribute 'cbType'. Should return no objects
        p0 = new FilterProperty(Quantors.FOR_ALL, "cbType", FilterOperators.IS_IN);
        attributeFilter = new FilterProperty[]{p0};
        referenceFilter = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb");
        List<?> findReply = requests.addFindRequest( 
          referenceFilter,
          attributeFilter
        );
        assertEquals("findReply.size()", 0, findReply.size());
          
        /**
         * find3 - complex filters
         */
    
        // THERE_EXISTS   
        p0 = new FilterProperty(Quantors.THERE_EXISTS, "cbType", FilterOperators.IS_LESS,"AAA");
      
        p1 = new FilterProperty(Quantors.THERE_EXISTS, "cbType", FilterOperators.IS_LESS_OR_EQUAL,"AAA");
      
        FilterProperty p2 = new FilterProperty(Quantors.THERE_EXISTS, "cbType", FilterOperators.IS_IN, "AAA");
      
        FilterProperty p3 = new FilterProperty(Quantors.THERE_EXISTS, "cbType", FilterOperators.IS_GREATER_OR_EQUAL, "AAA");
      
        FilterProperty p4 = new FilterProperty(Quantors.THERE_EXISTS, "cbType", FilterOperators.IS_GREATER, "AAA");
      
        FilterProperty p5 = new FilterProperty(Quantors.THERE_EXISTS, "cbType", FilterOperators.IS_BETWEEN, "AAA","BBB");
      
        FilterProperty p6 = new FilterProperty(Quantors.THERE_EXISTS, "cbType", FilterOperators.IS_OUTSIDE, "AAA","BBB");
      
        FilterProperty p7 = new FilterProperty(Quantors.THERE_EXISTS, "cbType", FilterOperators.IS_LIKE, "AAA%");
      
        FilterProperty p8 = new FilterProperty(Quantors.THERE_EXISTS, "cbType", FilterOperators.IS_UNLIKE, "AAA%");
      
        // FOR_ALL
        FilterProperty p9 = new FilterProperty(Quantors.FOR_ALL, "cbType", FilterOperators.IS_LESS, "AAA");
      
        FilterProperty p10 = new FilterProperty(Quantors.FOR_ALL, "cbType", FilterOperators.IS_LESS_OR_EQUAL, "AAA");
      
        FilterProperty p11 = new FilterProperty(Quantors.FOR_ALL, "cbType", FilterOperators.IS_IN, "AAA");
      
        FilterProperty p12 = new FilterProperty(Quantors.FOR_ALL, "cbType", FilterOperators.IS_GREATER_OR_EQUAL, "AAA");
      
        FilterProperty p13 = new FilterProperty(Quantors.FOR_ALL, "cbType", FilterOperators.IS_GREATER, "AAA");
      
        FilterProperty p14 = new FilterProperty(Quantors.FOR_ALL, "cbType", FilterOperators.IS_BETWEEN, "AAA","BBB");
      
        FilterProperty p15 = new FilterProperty(Quantors.FOR_ALL, "cbType", FilterOperators.IS_OUTSIDE, "AAA","BBB");
      
        FilterProperty p16 = new FilterProperty(Quantors.FOR_ALL, "cbType", FilterOperators.IS_LIKE, "AAA%");
      
        FilterProperty p17 = new FilterProperty(Quantors.FOR_ALL, "cbType", FilterOperators.IS_UNLIKE, "AAA%");
      
        // find_1
        attributeFilter = new FilterProperty[]{
          p0,  p1,
          p2,  p3,  p4,  p5,  p6,  p7,  p8,  p9
        };
        referenceFilter = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb");
        requests.addFindRequest( 
          referenceFilter,
          attributeFilter
        );

        // find_2
        attributeFilter = new FilterProperty[]{
          p10, p11, p12, p13, p14, p15, p16, p17
        };
        referenceFilter = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb");
        requests.addFindRequest( 
          referenceFilter,
          attributeFilter
        );
  
      }
      catch(ServiceException ex) {
        fail("Caught exception: ServiceException\n" + ex.toString());
      }
    }
  }

  //---------------------------------------------------------------------------  
  public void doTestModify() {
  
    AppLog.warning("*** testModify ***");
  
    if(_testReplace) {
  
      try {
  
        RequestCollection requests = new RequestCollection(
          new ServiceHeader(), 
          provider
        );
  
        for(int i = 0; i < _nRuns; i++) {
          Path p0 = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + i);
          DataproviderObject o0 = new DataproviderObject(p0);
          o0.values("object_class").add("org:openmdx:booking1:CompoundBooking");
          o0.values("cbType").add("99");
          o0.values("cancelsCB").add(new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + (i + 1)));
          o0.values("adviceText").add("replace XML string");  
          requests.addReplaceRequest(o0);
  
          for(int j = 0; j < 10; j++) {
            Path p1 = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + i + "/slb/" + j);
            DataproviderObject o1 = new DataproviderObject(p1);
            o1.values("object_class").add("org:openmdx:booking1:SingleLegBooking");
            o1.values("slbType").add("33");
            o1.values("pos").add(new Path(providerPath.toXri() + "/segment/" + segmentName + "/account/123456789012345678901234567890/bookedPosition/FI:" + j));
            o1.values("pos").add(new Path(providerPath.toXri() + "/segment/" + segmentName + "/account/123456789012345678901234567890/bookedPosition/FI:" + j));
            o1.values("pos").add(new Path(providerPath.toXri() + "/segment/" + segmentName + "/account/123456789012345678901234567890/bookedPosition/FI:" + j));
            o1.values("price").add(new BigDecimal(9999 * i + j));
            o1.values("priceCurrency").add("USD");
            o1.values("isDebit").add(new BigDecimal(1));
            o1.values("isLong").add(new BigDecimal(0));
            o1.values("valueDate").add("99990101T120000X000Z");
            o1.values("bookingDate").add("99990101T120000X000Z");
            o1.values("quantity").add(new BigDecimal(9999 + i + j));
            o1.values("quantityAbsolute").add(new BigDecimal(0));
            o1.values("visibility").add("public");
            o1.values("administrativeDescription").add("replaced test booking");
            o1.values("description").add("<booking>replaced: Ihr Zahlungsauftrag vom 1.1.2000</booking>");
            if(this.getName().endsWith("Sliced")) {
              o1.values("credValue").add(new byte[]{0x75, 0x76, 0x77});
            }
            requests.addReplaceRequest(o1);
          }
        }
      }
      catch(ServiceException ex) {
        ex.log();
        fail("Caught exception: ServiceException\n" + ex.toString());
      }
    }
  }
  //---------------------------------------------------------------------------  
  public void doTestGet2() {
  
    AppLog.warning("*** testGet2 ***");
  
    if(_testReplace) {
  
      try {
  
        RequestCollection requests = new RequestCollection(
          new ServiceHeader(), 
          provider
        );
  
        for(int i = 0; i < _nRuns; i++) {
          Path p0 = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + i);
  
          DataproviderObject_1_0 o0 = requests.addGetRequest(
            p0,
            AttributeSelectors.ALL_ATTRIBUTES,
            new AttributeSpecifier[]{}
          );
    
          assertEquals("object_class", o0.values("object_class").get(0), "org:openmdx:booking1:CompoundBooking");
          assertEquals("cbType", o0.values("cbType").get(0), "99");
          assertEquals("cancelsCB", o0.values("cancelsCB").get(0), new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + (i + 1)));
          assertEquals("adviceText", o0.values("adviceText").get(0), "replace XML string");
                    
          for(int j = 0; j < 10; j++) {
            Path p1 = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + i + "/slb/" + j);
  
            DataproviderObject_1_0 o1 = requests.addGetRequest(
              p1,
              AttributeSelectors.ALL_ATTRIBUTES,
              new AttributeSpecifier[]{}
            );
  
            assertEquals("object_class", o1.values("object_class").get(0), "org:openmdx:booking1:SingleLegBooking");
            assertEquals("slbType", o1.values("slbType").get(0), "33");
            assertEquals("pos", o1.values("pos").get(0), new Path(providerPath.toXri() + "/segment/" + segmentName + "/account/123456789012345678901234567890/bookedPosition/FI:" + j));
            assertTrue("price",  new BigDecimal(9999 * i + j).compareTo((BigDecimal)o1.values("price").get(0)) == 0);
            assertEquals("isDebit", ((BigDecimal)o1.values("isDebit").get(0)).intValue(), 1);
            assertEquals("isLong", ((BigDecimal)o1.values("isLong").get(0)).intValue(), 0);
            assertEquals("priceCurrency", o1.values("priceCurrency").get(0), "USD");
            assertEquals("valueDate", o1.values("valueDate").get(0), "99990101T120000X000Z");
            assertEquals("bookingDate", o1.values("bookingDate").get(0), "99990101T120000X000Z");
            assertTrue("quantity", new BigDecimal(9999 + i + j).compareTo((BigDecimal)o1.values("quantity").get(0)) == 0);
            assertTrue("quantityAbsolute", new BigDecimal(0).compareTo((BigDecimal)o1.values("quantityAbsolute").get(0)) == 0);
            assertEquals("visibility", o1.values("visibility").get(0), "public");
            assertEquals("administrativeDescription", o1.values("administrativeDescription").get(0), "replaced test booking");
            assertEquals("description", o1.values("description").get(0), "<booking>replaced: Ihr Zahlungsauftrag vom 1.1.2000</booking>");
            assertEquals("description", o1.values("description").get(1), null);
            assertEquals("description", o1.values("description").get(2), "additional description 2");
            assertEquals("description", o1.values("description").get(3), null);
            assertEquals("description", o1.values("description").get(4), "additional description 4");
            assertEquals("description", o1.values("description").get(5), null);
            assertEquals("description", o1.values("description").get(6), "additional description 6");
            assertEquals("description", o1.values("description").get(7), null);
            assertEquals("description", o1.values("description").get(8), "additional description 8");
            assertEquals("description", o1.values("description").get(9), null);
            if(this.getName().endsWith("Sliced")) {
                byte[] credValue = (byte[])o1.values("credValue").get(0);
                assertEquals("credValue[0]", credValue[0], 0x75);
                assertEquals("credValue[1]", credValue[1], 0x76);
                assertEquals("credValue[2]", credValue[2], 0x77);
            }    
          }
        }
      }
      catch(ServiceException ex) {
        ex.log();
        fail("Caught exception: ServiceException\n" + ex.toString());
      }
    }
  }
  
  //---------------------------------------------------------------------------  
  public void doTestReplace() {
  
    AppLog.warning("*** testReplace ***");
  
    if(_testModify) {
      try {
  
        RequestCollection requests = new RequestCollection(
          new ServiceHeader(), 
          provider
        );
  
        for(int i = 0; i < _nRuns; i++) {
          Path p0 = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + i);
          DataproviderObject o0 = new DataproviderObject(p0);
          o0.values("cbType").add("11");
  
          requests.addReplaceRequest(o0);
  
          for(int j = 0; j < 10; j++) {
            Path p1 = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + i + "/slb/" + j);
            DataproviderObject o1 = new DataproviderObject(p1);
            o1.values("slbType").add("11");
            o1.values("price").add(new BigDecimal(i + j));
            o1.values("priceCurrency").add("CHF");
            o1.values("valueDate").add("20010101T120000X000Z");
            o1.values("bookingDate").add("20010101T120000X000Z");
            o1.values("quantity").add(new BigDecimal(i + j));
            o1.values("quantityAbsolute").add(new BigDecimal(0));
            o1.values("visibility").add("private");
  
            requests.addReplaceRequest(o1);
          }
        }
      }
      catch(ServiceException ex) {
        ex.log();
        fail("Caught exception: ServiceException\n" + ex.toString());
      }
    }
  }
  
  //---------------------------------------------------------------------------  
  public void doTestGet3() {
  
    AppLog.warning("*** testGet3 ***");
  
    if(_testModify) {
      try {
  
        RequestCollection requests = new RequestCollection(
          new ServiceHeader(), 
          provider
        );

        for(int i = 0; i < _nRuns; i++) {
          Path p0 = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + i);
          
          DataproviderObject_1_0 o0 = requests.addGetRequest(
            p0,
            AttributeSelectors.ALL_ATTRIBUTES,
            new AttributeSpecifier[]{}
          );
    
          assertEquals("cbType", o0.values("cbType").get(0), "11");
    
          for(int j = 0; j < 10; j++) {
            Path p1 = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + i + "/slb/" + j);
  
            DataproviderObject_1_0 o1 = requests.addGetRequest( 
              p1,
              AttributeSelectors.ALL_ATTRIBUTES,
              new AttributeSpecifier[]{}
            );
    
            assertEquals("object_class",  "org:openmdx:booking1:SingleLegBooking", o1.values("object_class").get(0));
            assertEquals("slbType", o1.values("slbType").get(0), "11");
            assertEquals("pos", o1.values("pos").get(0), new Path(providerPath.toXri() + "/segment/" + segmentName + "/account/123456789012345678901234567890/bookedPosition/FI:" + j));
            assertTrue("price", new BigDecimal(i + j).compareTo((BigDecimal)o1.values("price").get(0)) == 0);
            assertEquals("isDebit", ((BigDecimal)o1.values("isDebit").get(0)).intValue(), 1);
            assertEquals("isLong", ((BigDecimal)o1.values("isLong").get(0)).intValue(), 0);
            assertEquals("priceCurrency", o1.values("priceCurrency").get(0), "CHF");
            assertEquals("valueDate", o1.values("valueDate").get(0), "20010101T120000X000Z");
            assertEquals("bookingDate", o1.values("bookingDate").get(0), "20010101T120000X000Z");
            assertTrue("quantity",  new BigDecimal(i + j).compareTo((BigDecimal)o1.values("quantity").get(0)) == 0);
            assertTrue("quantityAbsolute", new BigDecimal(0).compareTo((BigDecimal)o1.values("quantityAbsolute").get(0)) == 0);
            assertEquals("visibility", o1.values("visibility").get(0), "private");
            assertEquals("administrativeDescription", o1.values("administrativeDescription").get(0), "replaced test booking");
            assertEquals("description", o1.values("description").get(0), "<booking>replaced: Ihr Zahlungsauftrag vom 1.1.2000</booking>");
            assertEquals("description", o1.values("description").get(1), null);
            assertEquals("description", o1.values("description").get(2), "additional description 2");
            assertEquals("description", o1.values("description").get(3), null);
            assertEquals("description", o1.values("description").get(4), "additional description 4");
            assertEquals("description", o1.values("description").get(5), null);
            assertEquals("description", o1.values("description").get(6), "additional description 6");
            assertEquals("description", o1.values("description").get(7), null);
            assertEquals("description", o1.values("description").get(8), "additional description 8");
            assertEquals("description", o1.values("description").get(9), null);
            if(this.getName().endsWith("Sliced")) {
              byte[] credValue = (byte[])o1.values("credValue").get(0);
              assertEquals("credValue[0]", credValue[0], 0x75);
              assertEquals("credValue[1]", credValue[1], 0x76);
              assertEquals("credValue[2]", credValue[2], 0x77);
            }
          }
        }
      }
      catch(ServiceException ex) {
        ex.log();
        fail("Caught exception: ServiceException\n" + ex.toString());
      }
    }
  }

  //---------------------------------------------------------------------------  
  public void doTestRemove() {
  
    AppLog.warning("*** testRemove ***");
  
    if(_testRemove) {
      try {
  
        RequestCollection requests = new RequestCollection(
          new ServiceHeader(), 
          provider
        );
  
        for(int i = 0; i < _nRuns; i++) {
          Path p = new Path(providerPath.toXri() + "/segment/" + segmentName + "/cb/" + i);
          requests.addRemoveRequest(p);
        }
      }
      catch(ServiceException ex) {
        ex.log();
        fail("Caught exception: ServiceException\n" + ex.toString());
      }
    }
  }
  
  //---------------------------------------------------------------------------  
  
  static private Path providerPath = null;
  static private Dataprovider_1_0 provider = null;
  static private int _nRuns = 10;
  static private String segmentName = "s0";
  static private List<String> providerNames = Arrays.asList(
      "Sliced"
  ); 
  static final boolean _testMultiValuedModify = true;
  static final boolean _testExtent = false;
  static final boolean _testCreate = true;
  static final boolean _testGet = true;
  static final boolean _testFind = true;
  static final boolean _testReplace = true;
  static final boolean _testModify = true;
  static final boolean _testRemove = true;

  static private final String PROVIDER_URL = "file:src/ear/test-persistence.ear";
//static private final String CONNECTOR_URL = "file:src/connector/openmdx-2/postgresql-7.rar";
//static private final String CONNECTOR_URL = "file:src/connector/openmdx-2/mysql-4.rar";
//static private final String CONNECTOR_URL = "file:src/connector/openmdx-2/sql-server-2000.rar";
//static private final String CONNECTOR_URL = "file:src/connector/openmdx-2/sql-server-2005.rar";
//static private final String CONNECTOR_URL = "file:src/connector/openmdx-2/firebird-1.rar";
  static private final String CONNECTOR_URL = "file:src/connector/openmdx-2/oracle-10g.rar";
  
  /**
   * Define whether deployment details should logged to the console
   */
  final private static boolean LOG_DEPLOYMENT_DETAIL = false;

//  /**
//   * 
//   */
//  protected final static Dataprovider_1ConnectionFactory dataproviderConnectionfactory = new Dataprovider_1Deployment(
//      new LazyDeployment(
//          CONNECTOR_URL,
//          PROVIDER_URL,
//          LOG_DEPLOYMENT_DETAIL ? System.out : null,
//          System.err
//      ),
//      null, "org/openmdx/test/gateway1/NoOrNew"  
//  );

}

//--- End of File -----------------------------------------------------------
