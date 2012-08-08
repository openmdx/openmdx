/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestClock_1.scala,v 1.2 2010/11/26 14:05:38 wfro Exp $
 * Description: TestClock_1
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/11/26 14:05:38 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
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
package test_scala.mock.clock1;

import javax.jdo._
import javax.naming._
import org.junit._
import Assert._

import org.junit.runner.RunWith
import org.openmdx.kernel.lightweight.naming.NonManagedInitialContextFactory;
import org.w3c.format._

/**
 * Class Loading Test
 */
class TestClock_1 {
    
    /**
     * Retrieve a segment
     */
    private def getSegment(
        name: String,
        overrides: java.util.Map[String,String]
    ) : test.openmdx.clock1.jmi1.Segment = {
        val entityManagerFactory = JDOHelper.getPersistenceManagerFactory(overrides, "test-Clock-EntityManagerFactory");
        val persistenceManager = entityManagerFactory.getPersistenceManager();
        val authority = persistenceManager.getObjectById(
    		classOf[org.openmdx.base.jmi1.Authority], 
    		test.openmdx.clock1.jmi1.Clock1Package.AUTHORITY_XRI
    	);
        val provider = authority.getProvider("Java");
        provider.getSegment(name).asInstanceOf[test.openmdx.clock1.jmi1.Segment]
    }
    
    /**
     * Standard
     */
    @Test 
    def normal(
    ) {
        val segment = getSegment("Normal", null);
        val description = segment.getDescription();
        assertEquals(
            "description", 
            "clock1 segment",
            segment.getDescription()
        );
        val utc = segment.currentDateAndTime().getUtc();
        val now = System.currentTimeMillis();
        Assert.assertTrue("Time window < 1 s", Math.abs(now - utc.getTime()) < 1000);
        println(description + ": " + DateTimeFormat.BASIC_UTC_FORMAT.format(utc));
        val provider = segment.getProvider();
        assertEquals("Normal provider", "xri://@openmdx*test.openmdx.clock1/provider/Java", provider.refMofId());
    }

    /**
     * Standard
     * @throws ParseException 
     */
    @Test 
    def mocked(
    ) {
        val overrides = new java.util.HashMap[String, String]();
        overrides.put("org.openmdx.jdo.EntityManager.plugIn[0]","mockPlugIn");
        overrides.put("mockPlugIn.modelPackage[0]","test:openmdx:clock1");
        overrides.put("mockPlugIn.packageImpl[0]","test.mock.clock1.aop2");
        val segment = getSegment("Mocked", overrides);
        try {
            segment.getDescription();
            Assert.fail("IndexOutOfBoundsException expected");
        } catch {
        	case exception: IndexOutOfBoundsException =>
            // Expected behaviour
        }
        val utc = segment.currentDateAndTime().getUtc();
        assertEquals("High Noon", DateTimeFormat.BASIC_UTC_FORMAT.parse("20000401T120000.000Z"), segment.currentDateAndTime().getUtc());
        println("n/a: " + DateTimeFormat.BASIC_UTC_FORMAT.format(utc)); // xri://@openmdx*test.openmdx.clock1/provider/Mocked
        val provider = segment.getProvider();
        assertEquals("Normal provider", "xri://@openmdx*test.openmdx.clock1/provider/Mocked", provider.refMofId());
        assertSame("Persistence Managers", JDOHelper.getPersistenceManager(segment), JDOHelper.getPersistenceManager(provider));
    }
       
}

object TestClock_1 {
	
    @BeforeClass 
    def setUp() {
        System.setProperty(
        	Context.INITIAL_CONTEXT_FACTORY, 
        	classOf[NonManagedInitialContextFactory].getName()
        );
    }

}
