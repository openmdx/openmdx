/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestStringExpression.java,v 1.2 2004/12/16 17:41:29 hburger Exp $
 * Description: Relocatable Enumeration
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/12/16 17:41:29 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.test.base.text.pattern;

import junit.framework.TestCase;

import org.openmdx.base.text.pattern.StringExpression;
import org.openmdx.base.text.pattern.cci.Matcher_1_0;
import org.openmdx.base.text.pattern.cci.Pattern_1_0;


/**
 * 
 */
public class TestStringExpression extends TestCase {

    /**
     * 
     */
    public TestStringExpression() {
        super();
    }    

    private final static String[] elements = new String[]{
        "'a",
        "b",
        "c'"
    };
    
    private final static String input = "'a', 'b', 'c'";
    
    private static final String delimiter = "', '";
    
    public void testMatch() {
        Pattern_1_0 pattern = StringExpression.compile(delimiter);
        Matcher_1_0 matcher = pattern.matcher(input);
        assertFalse("Matches", matcher.matches());
        assertEquals("Group Count", 0, matcher.groupCount());
        assertEquals("Group 0", input, matcher.group(0));
    }

    public void testFind() {
        Pattern_1_0 pattern = StringExpression.compile(delimiter);
        Matcher_1_0 matcher = pattern.matcher(input);
        int i, position = 0;        
        for(
             i = 0;
             i < elements.length - 1;
             i++
        ){
            assertTrue("Find " + i, matcher.find());
            assertEquals("Element " + i, elements[i], input.substring(position, matcher.start()));
            position = matcher.end();
        }
        assertFalse("Find " + i, matcher.find());
        assertEquals("Element " + i, elements[i], input.substring(position));
    }

    public void testReplacement() {
        Pattern_1_0 pattern = StringExpression.compile(delimiter);
        Matcher_1_0 matcher = pattern.matcher(input);
        assertEquals("Replace all ", "'a/b/c'", matcher.replaceAll("/"));
        assertEquals("Replace first ", "'a/b', 'c'", matcher.replaceFirst("/"));
    }

}