package test.openmdx/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Persistence Manager Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2014-2021, OMEX AG, Switzerland
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
.generic1;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmdx.generic1.jmi1.Property;
import org.openmdx.generic1.jmi1.StringProperty;
import org.openmdx.kernel.lightweight.naming.NonManagedInitialContextFactoryBuilder;
import org.w3c.cci2.Container;

import test.openmdx.app1.cci2.GenericAddress;

/**
 * Persistence Manager Test
 */
public class PersistenceManagerTest {

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
    public void whenOneChildIsAddedToTransientContainerThenItsSizeIs1(){
    	// Arrange
    	final GenericAddress parent = this.entityManager.newInstance(GenericAddress.class);
    	final Container<Property> testee = parent.<Property>getProperty();
    	final StringProperty child = this.entityManager.newInstance(StringProperty.class);
    	// Act
    	testee.add(child);
    	// Assert
    	Assert.assertEquals(1, testee.size());
    }

    @Test
    public void whenOneChildIsAddedToTransientContainerThenItIsReturnedByIterator(){
    	// Arrange
    	final GenericAddress parent = this.entityManager.newInstance(GenericAddress.class);
    	final Container<Property> testee = parent.<Property>getProperty();
    	final StringProperty child = this.entityManager.newInstance(StringProperty.class);
    	testee.add(child);
    	// Act
    	Property found = null;
    	for(Property property : testee) {
    		found = property;
    	}
    	// Assert
    	Assert.assertSame(child,found);
    }
    
}
