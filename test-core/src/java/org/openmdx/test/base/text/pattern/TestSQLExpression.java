/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestSQLExpression.java,v 1.6 2006/07/21 16:59:48 hburger Exp $
 * Description: openMDX SQL Expression Test
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/07/21 16:59:48 $
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
package org.openmdx.test.base.text.pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.base.text.pattern.SQLExpression;
import org.openmdx.base.text.pattern.cci.Pattern_1_0;

/**
 * Test SQL Expression
 */
public class TestSQLExpression extends TestCase {

    /**
     * Constructs a test case with the given name.
     */
    public TestSQLExpression(String name) {
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
        return new TestSuite(TestSQLExpression.class);
    }

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected Pattern_1_0[] validPattern;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() {
        this.validPattern = new Pattern_1_0[]{
            SQLExpression.compile("ABC%"),
            SQLExpression.compile("A\\%B_C"),
            SQLExpression.compile("%B\u20AC\\\\C["), // "%B€\\C["
            SQLExpression.compile("A_B%C\\_"),
            SQLExpression.compile("(?i)aBc")
        };
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testMatches(
    ){
        //
        // ABC% -> ^ABC
        //
        String candidate = "ABC";
        assertTrue(validPattern[0] + " ~ " + candidate, this.validPattern[0].matches(candidate));
        candidate = "ABCD";
        assertTrue(validPattern[0] + " ~ " + candidate, this.validPattern[0].matches(candidate));
        candidate = "ABCDE";
        assertTrue(validPattern[0] + " ~ " + candidate, this.validPattern[0].matches(candidate));
        candidate = "AABCDE";
        assertFalse(validPattern[0] + " ~ " + candidate, this.validPattern[0].matches(candidate));
        //
        // A\%B_C -> ^A%B.C$
        //
        candidate = "A%BXC";
        assertTrue(validPattern[1] + " ~ " + candidate, this.validPattern[1].matches(candidate));
        candidate = "A%BX\u20ACC";
        assertFalse(validPattern[1] + " ~ " + candidate, this.validPattern[1].matches(candidate));
        candidate = "A%BC";
        assertFalse(validPattern[1] + " ~ " + candidate, this.validPattern[1].matches(candidate));
        //
        // %B€\\C[ -> B€\\C\u005b$
        //
        candidate = "B\u20AC\\C[";
        assertTrue(validPattern[2] + " ~ " + candidate, this.validPattern[2].matches(candidate));
        candidate = "AB\u20AC\\C.";
        assertFalse(validPattern[2] + " ~ " + candidate, this.validPattern[2].matches(candidate));
        candidate = "AAB\u20AC\\C[";
        assertTrue(validPattern[2] + " ~ " + candidate, this.validPattern[2].matches(candidate));
        //
        // A_B%C\_ -> ^A.B.*C_$
        //
        candidate = "ABC";
        assertFalse(validPattern[3] + " ~ " + candidate, this.validPattern[3].matches(candidate));
        candidate = "AXBC_";
        assertTrue(validPattern[3] + " ~ " + candidate, this.validPattern[3].matches(candidate));
        candidate = "AXBCZ";
        assertFalse(validPattern[3] + " ~ " + candidate, this.validPattern[3].matches(candidate));
        candidate = "AXBYYC_";
        assertTrue(validPattern[3] + " ~ " + candidate, this.validPattern[3].matches(candidate));
        //
        // Case insensitive match
        //
        candidate = "abc";
        assertTrue(validPattern[4] + " ~ " + candidate, this.validPattern[4].matches(candidate));
        candidate = "(?i)aBc";
        assertFalse(validPattern[4] + " ~ " + candidate, this.validPattern[4].matches(candidate));
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testExpression(
    ){
        try {
            SQLExpression.compile(null);
            fail("Illegal argument exception expected");
        } catch (IllegalArgumentException exception) {
            assertTrue(
                "BadParameterException expected", 
                exception.getMessage().startsWith("DefaultDomain.BAD_PARAMETER")
            );
        }
    }
    
}
