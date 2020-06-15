/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: TestCallbacks 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland
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

package test.openmdx.model1;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.kernel.lightweight.naming.NonManagedInitialContextFactoryBuilder;

import test.openmdx.model1.jmi1.C4;
import test.openmdx.model1.jmi1.C41;
import test.openmdx.model1.jmi1.Model1Package;
import test.openmdx.model1.jmi1.Segment;

/**
 * TestCallbacks
 */
public class TestCallbacks {

    protected static final String ENTITY_MANAGER_FACTORY_NAME = "test-Main-EntityManagerFactory";
    protected static PersistenceManagerFactory entityManagerFactory;
    protected PersistenceManager entityManager;
    

    @BeforeClass
    public static void createPersistenceManagerFactory(
    ) throws NamingException{
        entityManagerFactory = JDOHelper.getPersistenceManagerFactory(
            ENTITY_MANAGER_FACTORY_NAME
        );
        if(!NamingManager.hasInitialContextFactoryBuilder()) {
            NonManagedInitialContextFactoryBuilder.install(null);
        }
    }
    
    @Before
    public void setUp(){
        this.entityManager = entityManagerFactory.getPersistenceManager();
    }

    @Test
    public void testAbstractCallbackOrder(){
        this.entityManager.currentTransaction().begin();
        Segment segment = getModelTestSegment();
        this.entityManager.currentTransaction().commit();
        this.entityManager.currentTransaction().begin();
        C4 c4 = entityManager.newInstance(C4.class);
        segment.addC("c4",c4);
        this.entityManager.currentTransaction().commit();
        System.out.println(c4.refClass().refMofId() + " Callback Order: " + c4.getCallback());
    }
    
    @Test
    public void testConcreteCallbackOrder(){
        this.entityManager.currentTransaction().begin();
        Segment segment = getModelTestSegment();
        this.entityManager.currentTransaction().commit();
        this.entityManager.currentTransaction().begin();
        C4 c4 = entityManager.newInstance(C41.class);
        segment.addC("c41",c4);
        this.entityManager.currentTransaction().commit();
        System.out.println(c4.refClass().refMofId() + " Callback Order: " + c4.getCallback());
    }
    
    
    /**
     * Retrieve the Test segment
     * 
     * @return the Test segment
     */
    protected Segment getModelTestSegment(
    ){
        Provider provider = getModelTestProvider();
        Segment segment = (Segment) provider.getSegment("Test");
        if(segment == null) {
            PersistenceManager persistenceManager = ReducedJDOHelper.getPersistenceManager(provider);
            segment = persistenceManager.newInstance(Segment.class);
            provider.addSegment("Test", segment);
        }
        return segment;
    }

    /**
     * Retrieve the Transient provider
     *
     * @return the Transient provider
     */
    protected Provider getModelTestProvider(
    ) {
        Authority authority = entityManager.getObjectById(Authority.class, Model1Package.AUTHORITY_XRI);
        Provider provider = authority.getProvider("Transient");
        return provider;
    }

}
