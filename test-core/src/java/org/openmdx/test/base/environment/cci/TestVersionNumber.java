/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestVersionNumber.java,v 1.3 2004/04/02 16:59:04 wfro Exp $
 * Description: openMDX Version Test
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/04/02 16:59:04 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.test.base.environment.cci;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.kernel.environment.cci.VersionNumber;

/**
 * @author hburger
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TestVersionNumber extends TestCase {

    /**
     * Constructs a test case with the given name.
     */
    public TestVersionNumber(String name) {
        super(name);
    }
    
    /**
     * The batch TestRunner can be given a class to run directly.
     * To start the batch runner from your main you can write: 
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    
    /**
     * A test runner either expects a static method suite as the
     * entry point to get a test to run or it will extract the 
     * suite automatically. 
     */
    public static Test suite() {
        return new TestSuite(TestVersionNumber.class);
    }

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

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() {
        this.v = new VersionNumber("");
        this.v2_1 = new VersionNumber("2.1");
        this.v3 = new VersionNumber("3");
        this.v3_1 = new VersionNumber("3.1");
        this.v3_3 = new VersionNumber("3.3");
        this.v3_3_1 = new VersionNumber("3.3.1");
        this.v3_3_3 = new VersionNumber("3.3.3");
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
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
    
    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
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

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testPrefix(
    ){
        assertEquals("Prefix 0 of v3.3.1", v, v3_3_1.getPrefix(0));
        assertEquals("Prefix 1 of v3.3.1", v3, v3_3_1.getPrefix(1));
        assertEquals("Prefix 2 of v3.3.1", v3_3, v3_3_1.getPrefix(2));
    }
    
    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testCompare(
    ) throws CloneNotSupportedException{
        assertEquals("v3.3 == v3.3", 0, v3_3.compareTo(v3_3.clone()));
        assertTrue("v3.3 < v3.3.1", v3_3.compareTo(v3_3_1) < 0);
        assertTrue("v3.3.3 > v3.3.1", v3_3_3.compareTo(v3_3_1) > 0);
    }

}
