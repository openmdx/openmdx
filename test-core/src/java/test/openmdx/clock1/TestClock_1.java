/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Class Loading Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package test.openmdx.clock1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import javax.jdo.Constants;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.resource.ResourceException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openmdx.base.Version;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.kernel.lightweight.naming.NonManagedInitialContextFactoryBuilder;
import org.w3c.format.DateTimeFormat;

import test.openmdx.clock1.jmi1.Clock1Package;
import test.openmdx.clock1.jmi1.Segment;

/**
 * Class Loading Test
 */
public class TestClock_1 {

    /**
     * Retrieve and validate the entity manager factory
     * 
     * @return the entity manager factory
     * 
     * @throws Exception
     */
    protected PersistenceManagerFactory getEntityManagerFactory(
    ){
        PersistenceManagerFactory entityManagerFactory = JDOHelper.getPersistenceManagerFactory("test-Clock-EntityManagerFactory");
        assertNotNull("Persistence Manager Factory", entityManagerFactory);
        Properties factoryProperties = entityManagerFactory.getProperties();
        assertEquals(
            Constants.NONCONFIGURABLE_PROPERTY_VENDOR_NAME, 
            "openMDX", 
            factoryProperties.getProperty(Constants.NONCONFIGURABLE_PROPERTY_VENDOR_NAME)
        );
        assertEquals(
            Constants.NONCONFIGURABLE_PROPERTY_VERSION_NUMBER, 
            Version.getSpecificationVersion(), 
            factoryProperties.getProperty(Constants.NONCONFIGURABLE_PROPERTY_VERSION_NUMBER)
        );
        return entityManagerFactory;
    }
    
    /**
     * Retrieve a segment
     */
    protected Segment getSegment(
        String name
    ) throws ResourceException{
        PersistenceManager persistenceManager = getEntityManagerFactory().getPersistenceManager();
        Authority authority = persistenceManager.getObjectById(Authority.class, Clock1Package.AUTHORITY_XRI);
        assertNotNull("Authority '" + Clock1Package.AUTHORITY_XRI + "'", authority);
        Provider provider = authority.getProvider("Java");
        assertNotNull("Provider 'Java'", provider);
        Segment segment = (Segment) provider.getSegment(name);
        assertNotNull("Segment '" + name + "'", segment);
        return segment;
    }
    
    /**
     * Try to access the application resource via EJB
     */
    @Test
    public void testSegment(
    ) throws ResourceException{
        Segment segment = getSegment("In-Process");
        System.out.println(
            DateTimeFormat.BASIC_UTC_FORMAT.format(
                segment.currentDateAndTime().getUtc()
            )
        );
        assertEquals("description", "clock1 segment",segment.getDescription());
    }
        
    @BeforeClass
    public static void deploy() throws NamingException{
        if(!NamingManager.hasInitialContextFactoryBuilder()) {
            NonManagedInitialContextFactoryBuilder.install(null);
        }
    }

}
