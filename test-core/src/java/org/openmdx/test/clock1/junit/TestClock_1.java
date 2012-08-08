/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestClock_1.java,v 1.16 2009/03/06 16:59:45 hburger Exp $
 * Description: Class Loading Test
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/06 16:59:45 $
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
package org.openmdx.test.clock1.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.URL;

import javax.jdo.PersistenceManager;
import javax.resource.ResourceException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmdx.application.dataprovider.deployment.Deployment_1;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.test.clock1.jmi1.Clock1Package;
import org.openmdx.test.clock1.jmi1.Segment;

/**
 * Class Loading Test
 */
public class TestClock_1 {

    /**
     * An enterprise application resource
     */
    protected static final String APPLICATION_RESOURCE_URI = "org/openmdx/test/clock1/segment.txt";
    
    /**
     * Entity Manager Factory
     */
    protected static Deployment_1 entityManagerFactory;
    
    @Test
    /**
     * Try to access the application resource directly
     */
	public void testDirectAccess(
	) throws IOException{
        URL url = Classes.getApplicationResource(APPLICATION_RESOURCE_URI);
        assertNull("The applucation resource should be unvisible", url);
	}

    /**
     * Retrieve a segment
     */
    protected Segment getSegment(
        String name
    ) throws ResourceException{
        PersistenceManager persistenceManager = entityManagerFactory.getEntityManager();
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
    public void testBeanAccess(
    ) throws ResourceException{
        Segment segment = getSegment("In-Process");
        System.out.println(
            DateFormat.getInstance().format(
                segment.currentDateAndTime().getUtc()
            )
        );
        assertEquals("description", "clock1 segment",segment.getDescription());
    }
        
    @BeforeClass
    public static void deploy(){
        entityManagerFactory = new Deployment_1(
            true, // IN_PROCESS_LIGHTWEIGHT_CONTAINER
            null, // CONNECTOR_URL
            "file:../test-core/src/ear/test-classloading.ear", // APPLICATION_URL
            false, // LOG_DEPLOYMENT_DETAILS
            "test/openmdx/clock1/EntityProviderFactory", // ENTITY_MANAGER_FACTORY_JNDI_NAME
            null, // GATEWAY_JNDI_NAME
            "org:openmdx:test:clock1" // MODEL
        );
    }

    @AfterClass
    public static void close(
    ) throws IOException{
        entityManagerFactory.close();
    }

}
