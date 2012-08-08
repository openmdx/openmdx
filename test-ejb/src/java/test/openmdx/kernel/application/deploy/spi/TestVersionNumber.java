/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestVersionNumber.java,v 1.1 2010/03/05 16:44:04 hburger Exp $
 * Description: Test Version Number
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/05 16:44:04 $
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
Test Version Number */
package test.openmdx.kernel.application.deploy.spi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.openmdx.kernel.application.deploy.spi.VersionNumber;


/**
 * Test Version Number
 */
public class TestVersionNumber {

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected VersionNumber v;
    protected VersionNumber v2_1;
    protected VersionNumber v3;
    protected VersionNumber v3_1;
    protected VersionNumber v3_3;
    protected VersionNumber v3_3_1;
    protected VersionNumber v3_3_3;

    @Before
    public void setUp() {
        this.v = new VersionNumber("");
        this.v2_1 = new VersionNumber("2.1");
        this.v3 = new VersionNumber("3");
        this.v3_1 = new VersionNumber("3.1");
        this.v3_3 = new VersionNumber("3.3");
        this.v3_3_1 = new VersionNumber("3.3.1");
        this.v3_3_3 = new VersionNumber("3.3.3");
    }

    @Test
    public void testCompliance(
    ){
        //
        // Positive examples:
        //
        assertTrue("3.1 is compliant with 3", v3_1.isCompliantWith(v3));
        assertTrue("3.3 is compliant with 3.1", v3_3.isCompliantWith(v3_1));
        assertTrue("3.3.1 is compliant with 3.3.3", v3_3_1.isCompliantWith(v3_3_3));
        assertTrue("3.3.3 is compliant with 3.3.1", v3_3_3.isCompliantWith(v3_3_1));
        //
        // Negative examples:
        //
        assertFalse("3 is not compliant with 3.1", v3.isCompliantWith(v3_1));
        assertFalse("3.1 is not compliant with 3.3", v3_1.isCompliantWith(v3_3));
        assertFalse("3.1 is not compliant with 2.1", v3_1.isCompliantWith(v2_1));       
    }
    
    @Test
    public void testParsing(
    ){
        assertEquals(
            "Version 3.1.20031203154826500",
            "3.1.20031203154826500", 
            VersionNumber.toVersionNumber("3.1.20031203154826500").toString()
        );
        //
        // A version string must not start with a dot
        // 
        assertNull("A version string must not start with a dot", VersionNumber.toVersionNumber(".3"));
        try{
            new VersionNumber(".3");
            fail("A version string must not start with a dot");
        } catch (IllegalArgumentException exception){
            // A version string must not start with a dot
        }
        //
        // A version string must not end with a dot
        // 
        assertNull("A version string must not end with a dot", VersionNumber.toVersionNumber("3.4."));
        try{
            new VersionNumber("3.4.");
            fail("A version string must not end with a dot");
        } catch (IllegalArgumentException exception){
            // A version string must not end with a dot
        }
        //
        // A version string must not contain any character except digits and dots
        // 
        assertNull(
            "A version string must not contain any character except digits and dots", 
            VersionNumber.toVersionNumber("3.4alpha")
        );
        try{
            new VersionNumber("3.4alpha");
            fail("A version string must not contain any character except digits and dots");
        } catch (IllegalArgumentException exception){
            // A version string must not contain any character except digits and dots
        }
    }

    @Test
    public void testPrefix(
    ){
        assertEquals("Prefix 0 of v3.3.1", v, v3_3_1.getPrefix(0));
        assertEquals("Prefix 1 of v3.3.1", v3, v3_3_1.getPrefix(1));
        assertEquals("Prefix 2 of v3.3.1", v3_3, v3_3_1.getPrefix(2));
    }
    
    @Test
    public void testCompare(
    ) throws CloneNotSupportedException{
        assertEquals("v3.3 == v3.3", 0, v3_3.compareTo(v3_3.clone()));
        assertTrue("v3.3 < v3.3.1", v3_3.compareTo(v3_3_1) < 0);
        assertTrue("v3.3.3 > v3.3.1", v3_3_3.compareTo(v3_3_1) > 0);
    }

}
