/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestDateState.java,v 1.2 2009/09/18 12:44:37 hburger Exp $
 * Description: TestState 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/18 12:44:37 $
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

package test.openmdx.state2;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import javax.resource.ResourceException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.naming.Path;
import org.openmdx.state2.cci.DateStateViews;
import org.w3c.cci2.Container;
import org.w3c.spi2.Datatypes;

import test.openmdx.state2.jmi1.Segment;
import test.openmdx.state2.jmi1.State2Package;
import test.openmdx.state2.jmi1.StateA;
import test.openmdx.state2.jmi1.StateD;

/**
 * TestState
 */
public class TestDateState {

    protected static int M_COUNT = 3;
    
    protected PersistenceManager entityManager;
    
    protected static final PersistenceManagerFactory managerFactory = null;
//    new Deployment_1(
//        "xri://@openmdx*(+lightweight)*ENTERPRISE_APPLICATION_CONTAINER",
////      "xri://@openmdx*(+openejb)*ENTERPRISE_APPLICATION_CONTAINER",
//        new String[]{"file:../test-core/src/connector/openmdx-2/oracle-10g.rar"}, // CONNECTOR_URL
////      new String[]{"file:../test-core/src/connector/openmdx-2/postgresql-7.rar"}, // CONNECTOR_URL
//        new String[]{"file:../test-core/src/ear/test-datestate.ear"}, // APPLICATION_URL
//        false, /// LOG_DEPLOYMENT_DETAIL
//        "test/openmdx/datestate/EntityProviderFactory", // ENTITY_MANAGER_FACTORY_JNDI_NAME
//        null, // GATEWAY_JNDI_NAME
//        new String[]{"test:openmdx:compatibility:state1"} // MODEL
//    );
    
    @Before
    public void open(
    ) throws ResourceException{
        this.entityManager = managerFactory.getPersistenceManager(
            "JUnit",
            null
        );
    }

    @After
    public void close(
    ){
        if(this.entityManager != null) {
            this.entityManager.close();
            this.entityManager = null;
        }
    }
    
    protected Provider getProvider(){
        Authority authority = (Authority) entityManager.getObjectById(
            Authority.class, 
            State2Package.AUTHORITY_XRI
        );
        return authority.getProvider(false, "JUnit");
    }

    @Test
    public void testCompatibility(){
        testSegment(resetSegment("Compatibility"));
    }

    /**
     * (Re-)Create a segment
     * 
     * @param segmentName
     * 
     * @return an empty segment
     */
    private Segment resetSegment(
        String segmentName
    ){
        Transaction transaction = this.entityManager.currentTransaction();
        Provider provider = getProvider();
        //
        // Reset
        //
        Segment segment = (Segment) provider.getSegment(false, segmentName);
        if(segment != null) {
            transaction.begin();
            segment.refDelete();
            transaction.commit();
        }
        {
            transaction.begin();
            segment = (Segment) entityManager.newInstance(Segment.class);
            segment.setDescription(segmentName + " Segment");
            provider.addSegment(false, segmentName, segment);
            transaction.commit();
        }
        return segment;
    }
    
    /**
     * Test a single segment
     * 
     * @param segmentName
     */
    protected void testSegment(
       Segment segment
    ){
        {
            //
            // Validate Segment
            //
            assertNotNull("The segment must not be null", segment);
            Path objectId = (Path) JDOHelper.getObjectId(segment);
            assertNotNull("The segment's objectId must not be null", objectId);
            String description = segment.getDescription();
            assertNotNull("The segment's description must not be null", description);
            String name = objectId.getBase();
            assertTrue(
                "The description '" + description + "' must start with '" + name + "'",
                description.startsWith(name)
            );
            System.out.println("Test " + objectId.toXRI() + ": " + description);
        }
        //
        // Test Orders
        //
        testOrder(segment, 1, 2, 3);
        testOrder(segment, 1, 3, 2);
        testOrder(segment, 2, 1, 3);
        testOrder(segment, 3, 1, 2);
        testOrder(segment, 2, 3, 1);
        testOrder(segment, 3, 2, 1);
    }
    
    protected void testOrder(
        Segment segment,
        int... order
    ){
        Transaction transaction = this.entityManager.currentTransaction();
        StringBuilder b = new StringBuilder();
        for(int o : order) {
            b.append(o);
        }
        String oPrefix = b.toString();
        transaction.begin();
        Container<StateA> aC = segment.getA();
        System.out.println("aC: " + aC.size());
//        assertTrue("aC", aC.isEmpty());
        for(int aI = 0; aI < 3; aI++) {
            Segment sV = DateStateViews.getViewForTimeRange(
                segment,
                aI < 1 ? null : Datatypes.create(XMLGregorianCalendar.class, "20000101"),
                aI > 1 ? null : Datatypes.create(XMLGregorianCalendar.class, "20001231")
            );
            PersistenceManager sM = JDOHelper.getPersistenceManager(sV);
            String aId = oPrefix + "." + aI;
            StateA a = sM.newInstance(StateA.class);
            a.setStringValue("Order" + oPrefix + " A" + aI);
            List<String> m = a.getStringList();
            for(int mI = 0; mI < M_COUNT; mI++) {
                m.add("Order" + oPrefix + " A" + aI + "[" + mI + "]");
            }
            Container<StateD> dC = a.getD();
//            assertTrue("dC", dC.isEmpty());
            System.out.println("dC: " + dC.size());
            StateD[] dA = new StateD[3];
            for(int dI = 0; dI < 3; dI++){
                StateA aV = DateStateViews.getViewForTimeRange(
                    a,
                    Datatypes.create(XMLGregorianCalendar.class, dI < 2 ? "20000101" : "20000701"), 
                    Datatypes.create(XMLGregorianCalendar.class, dI > 0 ? "20001231" : "20000630")
                );
                PersistenceManager aM = JDOHelper.getPersistenceManager(aV);
                StateD d = dA[dI] = aM.newInstance(StateD.class);
                d.setIntegerValue(dI);
                d.setDateTimeValue(Datatypes.create(Date.class, "20000229T12000" + dI + ".000Z"));                
            }
            for(int o : order) {
                switch(o) {
                    case 1: 
                        sV.addA(false, aId, a);
//                        assertEquals("aC", 2 * (aI + 1), aC.size());
                        System.out.println("aC: " + aC.size());
                        break;
                    case 2:
                        int dI = 0;
                        for(StateD d : dA) {
                            a.addD(false, String.valueOf(dI++), d);
//                            assertEquals("dC", 2 * (dI + 1), dC.size());
                            System.out.println("dC: " + dC.size());
                        }
                        break;
                    case 3:
                        break;
                }
            }
        }
        transaction.commit();
    }


    //------------------------------------------------------------------------
    // Former state1 test code
    //------------------------------------------------------------------------
    
//  private static final String SEGMENT_NAME = "Compatibility";
//  
//  private static final String PROVIDER_NAME = "Standard";
//
//  private static final Path TRANSACTIONAL_PATTERN = new Path("xri:@openmdx:!($t*uuid*($.))");
//  
//  protected static Provider provider;
//      
//  protected static final EntityManagerFactory managerFactory = new Deployment_1(
//      "xri://@openmdx*(+leightweight)*ENTERPRISE_APPLICATION_CONTAINER",
////    "xri://@openmdx*(+openejb)*ENTERPRISE_APPLICATION_CONTAINER",        
////      new String[]{"file:../test-core/src/connector/openmdx-2/postgresql-7.rar"}, // CONNECTOR_URL
//      new String[]{"file:../test-core/src/connector/openmdx-2/oracle-10g.rar"}, // CONNECTOR_URL
//      new String[]{"file:../test-core/src/ear/test-state.ear"}, // APPLICATION_URL
//      false, /// LOG_DEPLOYMENT_DETAIL
//      "test/openmdx/state2/EntityProviderFactory", // ENTITY_MANAGER_FACTORY_JNDI_NAME
//      "test/openmdx/state2/Gateway", // GATEWAY_JNDI_NAME
//      new String[]{"test:openmdx:compatibility:state1"} // MODEL
//  );
//  
//  @BeforeClass
//  public static void reset(
//  ) throws ResourceException{
//      PersistenceManager persistenceManager = managerFactory.getEntityManager(
//          Collections.singletonList("JUnit")
//      );
//      Transaction transaction = persistenceManager.currentTransaction();
//      Authority authority = (Authority) persistenceManager.getObjectById(
//          Authority.class, 
//          State1Package.AUTHORITY_XRI
//      );
//      provider = authority.getProvider(false, PROVIDER_NAME);
//      Segment segment = (Segment) provider.getSegment(false, SEGMENT_NAME);
//      if(segment != null) {
//          transaction.begin();
//          segment.refDelete();
//          transaction.commit();
//      }
//      {
//          transaction.begin();
//          segment = (Segment) persistenceManager.newInstance(Segment.class);
//          segment.setDescription("Standard Segment");
//          provider.addSegment(false, SEGMENT_NAME, segment);
//          transaction.commit();
//      }
//  }
//
//  @AfterClass
//  public static void close(
//  ){
//      if(provider != null) {
//          getPersistenceManager().close();
//          provider = null;
//      }
//  }
//  
//  protected static PersistenceManager getPersistenceManager(){
//      return JDOHelper.getPersistenceManager(provider);
//  }
//  
//  protected static Transaction getTransaction(){
//      return getPersistenceManager().currentTransaction();
//  }
//
//  @Test
//  public void testStateCapable1(
//  ){
//      getTransaction().begin();
//      Authority authority = (Authority) getPersistenceManager().getObjectById(
//          Authority.class, 
//          org.openmdx.compatibility.state1.jmi1.State1Package.AUTHORITY_XRI
//      );
//      Provider provider = authority.getProvider("-");
//      org.openmdx.compatibility.state1.jmi1.Segment segment = (org.openmdx.compatibility.state1.jmi1.Segment) provider.getSegment("-");
//      StateCapable c0 = (StateCapable) getPersistenceManager().newInstance(StateCapable.class);
//      String transactionalObjectId = c0.getIdentity();
//      assertNotNull("Transactional Object Id", transactionalObjectId);
//      assertTrue("Transactional Object Id", new Path(transactionalObjectId).isLike(TRANSACTIONAL_PATTERN));
//      Path path = TestState.provider.refGetPath().getDescendant(
//          "segment",
//          SEGMENT_NAME,
//          "a",
//          "c0"
//      );
//      segment.addState1Core(path.toString(), c0);
//      getTransaction().commit();
//  }
//
//  @Test
//  public void testStateCapable2(
//  ){
//      testStateCapable2(false);
//      testStateCapable2(true);
//  }
//
//  private void testStateCapable2(
//      boolean direct
//  ){
//      StateCapable c0 = getStateCapable2(direct);
//      String suffix = direct ? " (direct)" : " (indirect)";
//      assertEquals(
//          "identitiy" + suffix, 
//          "xri://@openmdx*test.openmdx.compatibility.state1/provider/Standard/segment/Compatibility/a/c0", 
//          c0.getIdentity()
//      );
//      Path resourceIdentifier = c0.refGetPath();
//      assertEquals(
//          "resourceIdentifier" + suffix, 
//          "xri://@openmdx*org.openmdx.compatibility.state1/provider/-/segment/-/state1Core/(@openmdx*test.openmdx.compatibility.state1/provider/Standard/segment/Compatibility/a/c0)", 
//          resourceIdentifier.toXRI()
//      );
//  }
//  
//  private StateCapable getStateCapable2(
//      boolean direct
//  ){
//      Path identity = TestState.provider.refGetPath().getDescendant(
//          "segment",
//          SEGMENT_NAME,
//          "a",
//          "c0"
//      );        
//      if(direct) {
//          Path path = new Path(
//              org.openmdx.compatibility.state1.jmi1.State1Package.AUTHORITY_XRI
//          ).getDescendant(
//              "provider",
//              "-",
//              "segment",
//              "-",
//              "state1Core",
//              identity.toString()
//          );
//          return (StateCapable) getPersistenceManager().getObjectById(path);
//      } else {
//          Authority authority = (Authority) getPersistenceManager().getObjectById(
//              Authority.class, 
//              org.openmdx.compatibility.state1.jmi1.State1Package.AUTHORITY_XRI
//          );
//          Provider provider = authority.getProvider("-");
//          org.openmdx.compatibility.state1.jmi1.Segment segment = (org.openmdx.compatibility.state1.jmi1.Segment) provider.getSegment("-");
//          return segment.getState1Core(identity.toString());
//      }
//  }
//  
//  @Test
//  public void testStateA_a(
//  ){
//      getTransaction().begin();
//      Segment segment = DateStateViews.getViewForTimeRange(
//          (Segment)provider.getSegment(false, SEGMENT_NAME),
//          DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-01-01"),
//          DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-03-31")
//      );            
//      StateA stateA0_0 = (StateA) JDOHelper.getPersistenceManager(segment).newInstance(StateA.class);
//      E e0 = (E) JDOHelper.getPersistenceManager(segment).newInstance(E.class);
//      e0.setStringValue("Object E");
//      e0.setAReference(stateA0_0);
//      segment.addE("e0", e0);
//      stateA0_0.setStringValue("State A");
//      segment.addA(false, "a0", stateA0_0);
//      segment = (Segment) DateStateViews.getViewForTimeRange(
//          segment,
//          DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-04-01"),
//          DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-04-30")
//      );
//      StateA stateA0_1 = (StateA) JDOHelper.getPersistenceManager(segment).newInstance(StateA.class);
//      stateA0_1.getStringList().add("State A");
//      segment.addA(false, "a0", stateA0_1);
//      assertEquals("a0!0#stringValue", "State A", stateA0_0.getStringValue());
//      assertNull("a0!1#stringValue", stateA0_1.getStringValue());
//      System.out.println("Before commit");
//      List<? extends DateState> states = DateStateViews.getStates(stateA0_0);
//      for(DateState s : states) {
//          System.out.println(s.toString());
//      }
//      getTransaction().commit();
//      System.out.println("After commit");
//      states = DateStateViews.getStates(stateA0_0);
//      for(DateState s : states) {
//          System.out.println(s.toString());
//      }
//      assertEquals("a0!0#stringValue", "State A", stateA0_0.getStringValue());
//      assertNull("a0!1#stringValue", stateA0_1.getStringValue());
//      Object objectA0_2 = DateStateViews.getViewForTimeRange(
//          stateA0_0, 
//          DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-03-20"), 
//          DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-04-10")
//      );
//      StateA stateA0_2 = (StateA) objectA0_2;
//      getTransaction().begin();
//      stateA0_2.setStringValue("State A+");
//      getTransaction().commit();
//      StateA stateA0_a = (StateA) DateStateViews.getView(
//          stateA0_0, 
//          DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-03-19")
//      );
//      StateA stateA0_b = (StateA) DateStateViews.getView(
//          stateA0_0, 
//          DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-03-20")
//      );
//      StateA stateA0_c = (StateA) DateStateViews.getView(
//          stateA0_0, 
//          DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-04-10")
//      );
//      StateA stateA0_d = (StateA) DateStateViews.getView(
//          stateA0_0, 
//          DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-04-11")
//      );
//      assertEquals("State A", stateA0_a.getStringValue());   
//      assertTrue(stateA0_a.getStringList().isEmpty());   
//      assertEquals("State A+", stateA0_b.getStringValue());   
//      assertTrue(stateA0_b.getStringList().isEmpty());   
//      assertEquals("State A+", stateA0_c.getStringValue());   
//      assertArrayEquals(new Object[]{"State A"}, stateA0_c.getStringList().toArray());   
//      assertNull(stateA0_d.getStringValue());   
//      assertArrayEquals(new Object[]{"State A"}, stateA0_d.getStringList().toArray());   
//      assertSame("getCore", stateA0_a, stateA0_a.getCore());
//      
//      SegmentContainsE.E<E> e = segment.getE();
//      assertEquals("E", 1, e.size());
//      
//      @SuppressWarnings("unused")
//      StateAContainsStateD.D<StateD> d = stateA0_a.getD();
////      d = DateStateViews.getNoView(d);
//  }
//
//  @Test
//  public void testStateA_b(
//  ){
//      Segment segment = DateStateViews.getView(
//          (Segment)provider.getSegment(false, SEGMENT_NAME),
//          DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-03-31")
//      );            
//      StateA stateA0_b = segment.getA("a0");
//      assertNotNull(stateA0_b);
//      assertEquals("State A+", stateA0_b.getStringValue());   
//  }

}
