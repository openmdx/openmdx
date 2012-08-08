/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestState.java,v 1.8 2009/03/05 17:51:36 hburger Exp $
 * Description: TestState 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/05 17:51:36 $
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

package test.openmdx.compatibility.state1;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.resource.ResourceException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmdx.application.dataprovider.deployment.Deployment_1;
import org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.EntityManagerFactory;
import org.openmdx.compatibility.state1.jmi1.DateState;
import org.openmdx.compatibility.state1.jmi1.StateCapable;
import org.openmdx.compatibility.state1.spi.StateCapables;
import org.openmdx.compatibility.state1.view.DateStateViews;
import org.w3c.spi.DatatypeFactories;

import test.openmdx.compatibility.state1.cci2.SegmentContainsStateNoState;
import test.openmdx.compatibility.state1.cci2.StateAHasStateD;
import test.openmdx.compatibility.state1.jmi1.E;
import test.openmdx.compatibility.state1.jmi1.Segment;
import test.openmdx.compatibility.state1.jmi1.State1Package;
import test.openmdx.compatibility.state1.jmi1.StateA;
import test.openmdx.compatibility.state1.jmi1.StateD;

/**
 * TestState
 */
public class TestState {

    private static final String SEGMENT_NAME = "Compatibility";
    
    private static final String PROVIDER_NAME = "Standard";

    private static final Path TRANSACTIONAL_PATTERN = StateCapables.CORE_SEGMENT.getDescendant(
        "extent",
        ":*"
    );
    
    protected static Provider provider;
        
    protected static final EntityManagerFactory managerFactory = new Deployment_1(
        true, // IN_PROCESS
        "file:../test-core/src/connector/openmdx-2/oracle-10g.rar", // CONNECTOR_URL
        "file:../test-core/src/ear/test-state.ear", // APPLICATION_URL
        false, /// LOG_DEPLOYMENT_DETAIL
        "test/openmdx/state2/EntityProviderFactory", // ENTITY_MANAGER_FACTORY_JNDI_NAME
        "test/openmdx/state2/Gateway", // GATEWAY_JNDI_NAME
        "test:openmdx:compatibility:state1" // MODEL
    );
    
    @BeforeClass
    public static void reset(
    ) throws ResourceException{
        PersistenceManager persistenceManager = managerFactory.getEntityManager(
            AbstractPersistenceManagerFactory_1.toSubject("JUnit", null, null)
        );
        Transaction transaction = persistenceManager.currentTransaction();
        Authority authority = (Authority) persistenceManager.getObjectById(
            Authority.class, 
            State1Package.AUTHORITY_XRI
        );
        provider = authority.getProvider(false, PROVIDER_NAME);
        Segment segment = (Segment) provider.getSegment(false, SEGMENT_NAME);
        if(segment != null) {
            transaction.begin();
            segment.refDelete();
            transaction.commit();
        }
        {
            transaction.begin();
            segment = (Segment) persistenceManager.newInstance(Segment.class);
            segment.setDescription("Standard Segment");
            provider.addSegment(false, SEGMENT_NAME, segment);
            transaction.commit();
        }
    }

    @AfterClass
    public static void close(
    ){
        if(provider != null) {
            getPersistenceManager().close();
            provider = null;
        }
    }
    
    protected static PersistenceManager getPersistenceManager(){
        return JDOHelper.getPersistenceManager(provider);
    }
    
    protected static Transaction getTransaction(){
        return getPersistenceManager().currentTransaction();
    }

    @Test
    public void testStateCapable1(
    ){
        getTransaction().begin();
        Authority authority = (Authority) getPersistenceManager().getObjectById(
            Authority.class, 
            org.openmdx.compatibility.state1.jmi1.State1Package.AUTHORITY_XRI
        );
        Provider provider = authority.getProvider("-");
        org.openmdx.compatibility.state1.jmi1.Segment segment = (org.openmdx.compatibility.state1.jmi1.Segment) provider.getSegment("-");
        StateCapable c0 = (StateCapable) getPersistenceManager().newInstance(StateCapable.class);
        String transactionalObjectId = c0.getIdentity();
        assertNotNull("Transactional Object Id", transactionalObjectId);
        assertTrue("Transactional Object Id", new Path(transactionalObjectId).isLike(TRANSACTIONAL_PATTERN));
        Path path = TestState.provider.refGetPath().getDescendant(
            "segment",
            SEGMENT_NAME,
            "a",
            "c0"
        );
        segment.addState1Core(path.toString(), c0);
        getTransaction().commit();
    }

    @Test
    public void testStateCapable2(
    ){
        testStateCapable2(false);
        testStateCapable2(true);
    }

    private void testStateCapable2(
        boolean direct
    ){
        StateCapable c0 = getStateCapable2(direct);
        String suffix = direct ? " (direct)" : " (indirect)";
        assertEquals(
            "identitiy" + suffix, 
            "xri://@openmdx*test.openmdx.compatibility.state1/provider/Standard/segment/Compatibility/a/c0", 
            c0.getIdentity()
        );
        Path resourceIdentifier = c0.refGetPath();
        assertEquals(
            "resourceIdentifier" + suffix, 
            "xri://@openmdx*org.openmdx.compatibility.state1/provider/-/segment/-/state1Core/(@openmdx*test.openmdx.compatibility.state1/provider/Standard/segment/Compatibility/a/c0)", 
            resourceIdentifier.toResourceIdentifier()
        );
    }
    
    private StateCapable getStateCapable2(
        boolean direct
    ){
        Path identity = TestState.provider.refGetPath().getDescendant(
            "segment",
            SEGMENT_NAME,
            "a",
            "c0"
        );        
        if(direct) {
            Path path = new Path(
                org.openmdx.compatibility.state1.jmi1.State1Package.AUTHORITY_XRI
            ).getDescendant(
                "provider",
                "-",
                "segment",
                "-",
                "state1Core",
                identity.toString()
            );
            return (StateCapable) getPersistenceManager().getObjectById(path);
        } else {
            Authority authority = (Authority) getPersistenceManager().getObjectById(
                Authority.class, 
                org.openmdx.compatibility.state1.jmi1.State1Package.AUTHORITY_XRI
            );
            Provider provider = authority.getProvider("-");
            org.openmdx.compatibility.state1.jmi1.Segment segment = (org.openmdx.compatibility.state1.jmi1.Segment) provider.getSegment("-");
            return segment.getState1Core(identity.toString());
        }
    }
    
    @Test
    public void testStateA_a(
    ){
        getTransaction().begin();
        Segment segment = DateStateViews.getViewForTimeRange(
            (Segment)provider.getSegment(false, SEGMENT_NAME),
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-01-01"),
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-03-31")
        );            
        StateA stateA0_0 = (StateA) JDOHelper.getPersistenceManager(segment).newInstance(StateA.class);
        E e0 = (E) JDOHelper.getPersistenceManager(segment).newInstance(E.class);
        e0.setStringValue("Object E");
        e0.setStateA(stateA0_0);
        segment.addE("e0", e0);
        stateA0_0.setStringValue("State A");
        segment.addA(false, "a0", stateA0_0);
        segment = (Segment) DateStateViews.getViewForTimeRange(
            segment,
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-04-01"),
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-04-30")
        );
        StateA stateA0_1 = (StateA) JDOHelper.getPersistenceManager(segment).newInstance(StateA.class);
        stateA0_1.getStringList().add("State A");
        segment.addA(false, "a0", stateA0_1);
        assertEquals("a0!0#stringValue", "State A", stateA0_0.getStringValue());
        assertNull("a0!1#stringValue", stateA0_1.getStringValue());
        System.out.println("Before commit");
        List<? extends DateState> states = DateStateViews.getStates(stateA0_0);
        for(DateState s : states) {
            System.out.println(s.toString());
        }
        getTransaction().commit();
        System.out.println("After commit");
        states = DateStateViews.getStates(stateA0_0);
        for(DateState s : states) {
            System.out.println(s.toString());
        }
        assertEquals("a0!0#stringValue", "State A", stateA0_0.getStringValue());
        assertNull("a0!1#stringValue", stateA0_1.getStringValue());
        Object objectA0_2 = DateStateViews.getViewForTimeRange(
            stateA0_0, 
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-03-20"), 
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-04-10")
        );
        StateA stateA0_2 = (StateA) objectA0_2;
        getTransaction().begin();
        stateA0_2.setStringValue("State A+");
        getTransaction().commit();
        StateA stateA0_a = (StateA) DateStateViews.getView(
            stateA0_0, 
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-03-19")
        );
        StateA stateA0_b = (StateA) DateStateViews.getView(
            stateA0_0, 
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-03-20")
        );
        StateA stateA0_c = (StateA) DateStateViews.getView(
            stateA0_0, 
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-04-10")
        );
        StateA stateA0_d = (StateA) DateStateViews.getView(
            stateA0_0, 
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-04-11")
        );
        assertEquals("State A", stateA0_a.getStringValue());   
        assertTrue(stateA0_a.getStringList().isEmpty());   
        assertEquals("State A+", stateA0_b.getStringValue());   
        assertTrue(stateA0_b.getStringList().isEmpty());   
        assertEquals("State A+", stateA0_c.getStringValue());   
        assertArrayEquals(new Object[]{"State A"}, stateA0_c.getStringList().toArray());   
        assertNull(stateA0_d.getStringValue());   
        assertArrayEquals(new Object[]{"State A"}, stateA0_d.getStringList().toArray());   
        assertSame("getCore", stateA0_a, stateA0_a.getCore());
        
        SegmentContainsStateNoState.E<E> e = segment.getE();
        assertEquals("E", 1, e.size());
        
        StateAHasStateD.D<StateD> d = stateA0_a.getD();
//        d = DateStateViews.getNoView(d);
    }

    @Test
    public void testStateA_b(
    ){
        Segment segment = DateStateViews.getView(
            (Segment)provider.getSegment(false, SEGMENT_NAME),
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-03-31")
        );            
        StateA stateA0_b = segment.getA("a0");
        assertNotNull(stateA0_b);
        assertEquals("State A+", stateA0_b.getStringValue());   
    }
    
}
