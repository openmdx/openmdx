/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestRegularExpression.java,v 1.2 2004/11/09 19:07:19 hburger Exp $
 * Description: Relocatable Enumeration
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/11/09 19:07:19 $
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

import org.openmdx.base.text.pattern.RegularExpression;
import org.openmdx.base.text.pattern.cci.Matcher_1_0;
import org.openmdx.base.text.pattern.cci.Pattern_1_0;


/**
 * 
 */
public class TestRegularExpression extends TestCase {

    /**
     * 
     */
    public TestRegularExpression() {
        super();
    }    

    public void testRegExp() {
        Pattern_1_0 pattern = RegularExpression.compile("xri:@openmdx:([^/]+)(.*)");
        Matcher_1_0 matcher = pattern.matcher("xri:@openmdx:org.openmdx.lock1/provider");
        assertTrue("Matches", matcher.matches());
        assertEquals("Group Count", 2, matcher.groupCount());
        assertEquals("Group 0", "xri:@openmdx:org.openmdx.lock1/provider", matcher.group(0));
        assertEquals("Group 1", "org.openmdx.lock1", matcher.group(1));
        assertEquals("Group 2", "/provider", matcher.group(2));
    }

    public void test0002720(){
        String objectId = "K=SZ=Spital:1:2:";
        Pattern_1_0 pattern;
        Matcher_1_0 matcher;
        //
        // Original version (matches with JRE 1.3 only)
        //
        pattern = RegularExpression.compile("([:print:]*)\\:([:print:]*)");
        matcher = pattern.matcher(objectId);
        if(matcher.matches()) {
	        assertEquals("Group Count", 2, matcher.groupCount());
	        assertEquals("Group 0", objectId, matcher.group(0));
	        assertEquals("Group 1", "K=SZ=Spital:1:2", matcher.group(1));
	        assertEquals("Group 2", "", matcher.group(2));
        } else {
            System.out.println("'" + objectId + "' does not match '" + pattern);
        }
        //
        // Alternate Version (matches with JRE 1.3 only)
        //
        pattern = RegularExpression.compile("([:print:]*?)\\:([:print:]*)");
        matcher = pattern.matcher(objectId);
        if(matcher.matches()) {
	        assertEquals("Group Count", 2, matcher.groupCount());
	        assertEquals("Group 0", objectId, matcher.group(0));
	        assertEquals("Group 1", "K=SZ=Spital", matcher.group(1));
	        assertEquals("Group 2", "1:2:", matcher.group(2));
        } else {
            System.out.println("'" + objectId + "' does not match '" + pattern);
        }
        //
        // Possible Version 
        //
        pattern = RegularExpression.compile("([^:]+):(.*)");
        matcher = pattern.matcher(objectId);
        assertTrue("Matches", matcher.matches());
        assertEquals("Group Count", 2, matcher.groupCount());
        assertEquals("Group 0", objectId, matcher.group(0));
        assertEquals("Group 1", "K=SZ=Spital", matcher.group(1));
        assertEquals("Group 2", "1:2:", matcher.group(2));
        //
        // Recommended Version
        //
        pattern = RegularExpression.compile("^([^:]+):(.*)$");
        matcher = pattern.matcher(objectId);
        assertTrue("Matches", matcher.matches());
        assertEquals("Group Count", 2, matcher.groupCount());
        assertEquals("Group 0", objectId, matcher.group(0));
        assertEquals("Group 1", "K=SZ=Spital", matcher.group(1));
        assertEquals("Group 2", "1:2:", matcher.group(2));
    }

}