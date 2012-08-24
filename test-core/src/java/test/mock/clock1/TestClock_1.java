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
package test.mock.clock1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.resource.ResourceException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.kernel.lightweight.naming.NonManagedInitialContextFactory;
import org.w3c.format.DateTimeFormat;

import test.openmdx.clock1.jmi1.Clock1Package;
import test.openmdx.clock1.jmi1.Segment;

/**
 * Class Loading Test
 */
public class TestClock_1 {
    
    /**
     * Retrieve a segment
     */
    protected Segment getSegment(
        String name,
        Map<?,?> overrides
    ) throws ResourceException{
        PersistenceManagerFactory entityManagerFactory = JDOHelper.getPersistenceManagerFactory(overrides, "test-Clock-EntityManagerFactory");
        PersistenceManager persistenceManager = entityManagerFactory.getPersistenceManager();
        Authority authority = persistenceManager.getObjectById(Authority.class, Clock1Package.AUTHORITY_XRI);
        Provider provider = authority.getProvider("Java");
        return (Segment) provider.getSegment(name);
    }
    
    /**
     * Standard
     */
    @Test
    public void normal(
    ) throws ResourceException{
        Segment segment = getSegment("Normal", null);
        String description = segment.getDescription();
        assertEquals(
            "description", 
            "clock1 segment",
            segment.getDescription()
        );
        Date utc = segment.currentDateAndTime().getUtc();
        long now = System.currentTimeMillis();
        Assert.assertTrue("Time window < 1 s", Math.abs(now - utc.getTime()) < 1000);
        System.out.println(description + ": " + DateTimeFormat.BASIC_UTC_FORMAT.format(utc));
        Provider provider = segment.getProvider();
        assertEquals("Normal provider", "xri://@openmdx*test.openmdx.clock1/provider/Java", provider.refMofId());
    }

    /**
     * Standard
     * @throws ParseException 
     */
    @Test
    public void mocked(
    ) throws ResourceException, ParseException {
        Map<String,String> overrides = new HashMap<String, String>();
        overrides.put("org.openmdx.jdo.EntityManager.plugIn[0]","mockPlugIn");
        overrides.put("mockPlugIn.modelPackage[0]","test:openmdx:clock1");
        overrides.put("mockPlugIn.packageImpl[0]","test.mock.clock1.aop2");
        Segment segment = getSegment("Mocked", overrides);
        try {
            segment.getDescription();
            Assert.fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException exception) {
            // Excpected behaviour
        }
        Date utc = segment.currentDateAndTime().getUtc();
        assertEquals("High Noon", DateTimeFormat.BASIC_UTC_FORMAT.parse("20000401T120000.000Z"), segment.currentDateAndTime().getUtc());
        System.out.println("n/a: " + DateTimeFormat.BASIC_UTC_FORMAT.format(utc)); // xri://@openmdx*test.openmdx.clock1/provider/Mocked
        Provider provider = segment.getProvider();
        assertEquals("Normal provider", "xri://@openmdx*test.openmdx.clock1/provider/Mocked", provider.refMofId());
        assertSame("Persistence Managers", JDOHelper.getPersistenceManager(segment), JDOHelper.getPersistenceManager(provider));
    }
        
    
    @BeforeClass
    public static void setUp() throws NamingException{
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, NonManagedInitialContextFactory.class.getName());
    }

}
