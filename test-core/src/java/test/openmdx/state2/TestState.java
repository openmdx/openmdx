/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestState.java,v 1.2 2008/11/25 17:49:18 hburger Exp $
 * Description: TestState 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/25 17:49:18 $
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.openmdx.base.persistence.spi.AbstractManagerFactory.toSubject;

import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.resource.ResourceException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.persistence.spi.ManagerFactory_2_0;
import org.openmdx.compatibility.base.application.cci.Deployment_1;
import org.openmdx.state2.cci.DateStateViews;
import org.w3c.spi.DatatypeFactories;

import test.openmdx.state2.jmi1.CoreA;
import test.openmdx.state2.jmi1.Segment;
import test.openmdx.state2.jmi1.State2Package;
import test.openmdx.state2.jmi1.StateA;

/**
 * TestState
 */
public class TestState {

    private static final String SEGMENT_NAME = "Standard";
    
    private static final String PROVIDER_NAME = "Standard";

    protected static Provider provider;
        
    protected static final ManagerFactory_2_0 managerFactory = new Deployment_1(
        true, // IN_PROCESS
        "file:../test-core/src/connector/openmdx-2/oracle-10g.rar", // CONNECTOR_URL
        "file:../test-core/src/ear/test-state.ear", // APPLICATION_URL
        false, /// LOG_DEPLOYMENT_DETAIL
        "test/openmdx/state2/EntityProviderFactory", // ENTITY_MANAGER_FACTORY_JNDI_NAME
        "test/openmdx/state2/Gateway", // GATEWAY_JNDI_NAME
        "test:openmdx:state2" // MODEL
    );
    
    @BeforeClass
    public static void reset(
    ) throws ResourceException{
        PersistenceManager persistenceManager = managerFactory.createManager(
            toSubject("JUnit", null, null)
        );
        Transaction transaction = persistenceManager.currentTransaction();
        Authority authority = (Authority) persistenceManager.getObjectById(
            Authority.class, 
            State2Package.AUTHORITY_XRI
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
    public void testStateA(){
        getTransaction().begin();
        Segment segment = (Segment) provider.getSegment(false, SEGMENT_NAME);
        CoreA coreA = (CoreA) getPersistenceManager().newInstance(CoreA.class);
        StateA stateA0_0 = (StateA) getPersistenceManager().newInstance(StateA.class);
        stateA0_0.setCore(coreA);
        stateA0_0.setStringValue("State A");
        stateA0_0.setStateValidFrom(
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-01-01")
        );
        stateA0_0.setStateValidTo(
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-03-31")
        );
        StateA stateA0_1 = (StateA) getPersistenceManager().newInstance(StateA.class);
        stateA0_1.setCore(coreA);
        stateA0_1.getStringList().add("State A");
        stateA0_1.setStateValidFrom(
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-04-01")
        );
        stateA0_1.setStateValidTo(
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-04-30")
        );
        segment.addA(false, "a0", coreA);
        assertEquals("a0!0#stringValue", "State A", stateA0_0.getStringValue());
        assertNull("a0!1#stringValue", stateA0_1.getStringValue());
        Set<StateA> states = coreA.getState();
        for(StateA s : states) {
            System.out.println(s.toString());
        }
        getTransaction().commit();
        for(StateA s : states) {
            System.out.println(s.toString());
        }
        assertEquals("a0!0#stringValue", "State A", stateA0_0.getStringValue());
        assertNull("a0!1#stringValue", stateA0_1.getStringValue());
        StateA stateA0_2 = (StateA) DateStateViews.getViewForTimeRange(
            coreA, 
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-03-20"), 
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-04-10")
        );
        getTransaction().begin();
        stateA0_2.setStringValue("State A+");
        getTransaction().commit();
        StateA stateA0_a = (StateA) DateStateViews.getViewForTimePoint(
            coreA, 
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-03-19")
        );
        StateA stateA0_b = (StateA) DateStateViews.getViewForTimePoint(
            coreA, 
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-03-20")
        );
        StateA stateA0_c = (StateA) DateStateViews.getViewForTimePoint(
            coreA, 
            DatatypeFactories.xmlDatatypeFactory().newXMLGregorianCalendar("2000-04-10")
        );
        StateA stateA0_d = (StateA) DateStateViews.getViewForTimePoint(
            coreA, 
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
    }

}
