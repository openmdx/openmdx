/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestWildcard.java,v 1.2 2006/04/25 13:29:40 hburger Exp $
 * Description: Test Wildcard
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/04/25 13:29:40 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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
package org.openmdx.test.base.text.conversion;

import org.openmdx.base.text.conversion.SQLWildcards;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestWildcard extends TestCase {

    /**
     * Constructs a test case with the given name.
     */
    public TestWildcard(String name) {
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
        return new TestSuite(TestWildcard.class);
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testFromJDO(
    ){
        assertEquals("Convert .* Wildcard", "%X%Y%", sqlWildcards.fromJDO(".*X.*Y.*"));
        assertEquals("Convert . Wildcard", "_X_Y_", sqlWildcards.fromJDO(".X.Y."));
        assertEquals("Escape %", "\\%X\\%Y\\%", sqlWildcards.fromJDO("%X%Y%"));
        assertEquals("Escape _", "\\_X\\_Y\\_", sqlWildcards.fromJDO("_X_Y_"));
        assertEquals("Convert .* Escape", ".*X.*Y.*", sqlWildcards.fromJDO("\\.\\*X\\.\\*Y\\.\\*"));
        assertEquals("Convert . Escape", ".X.Y.", sqlWildcards.fromJDO("\\.X\\.Y\\."));
        assertEquals(
            "All",
            "%X%Y%" + "_X_Y_" + "\\%X\\%Y\\%" + "\\_X\\_Y\\_" + ".*X.*Y.*" + ".X.Y.",
            sqlWildcards.fromJDO(
                ".*X.*Y.*" + ".X.Y." + "%X%Y%" + "_X_Y_" + "\\.\\*X\\.\\*Y\\.\\*" + "\\.X\\.Y\\."
            )
        );
    }
    
    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testToJDO(
    ){
        assertEquals("Convert % Wildcard", ".*X.*Y.*", sqlWildcards.toJDO("%X%Y%"));
        assertEquals("Convert _ Wildcard", ".X.Y.", sqlWildcards.toJDO("_X_Y_"));
        assertEquals("Escape %", "%X%Y%", sqlWildcards.toJDO("\\%X\\%Y\\%"));
        assertEquals("Escape _", "_X_Y_", sqlWildcards.toJDO("\\_X\\_Y\\_"));
        assertEquals("Convert .* Escape", "\\.\\*X\\.\\*Y\\.\\*", sqlWildcards.toJDO(".*X.*Y.*"));
        assertEquals("Convert . Escape", "\\.X\\.Y\\.", sqlWildcards.toJDO(".X.Y."));
        assertEquals(
            "All",
            ".*X.*Y.*" + ".X.Y." + "%X%Y%" + "_X_Y_" + "\\.\\*X\\.\\*Y\\.\\*" + "\\.X\\.Y\\.",
            sqlWildcards.toJDO(
                "%X%Y%" + "_X_Y_" + "\\%X\\%Y\\%" + "\\_X\\_Y\\_" + ".*X.*Y.*" + ".X.Y."
            )
        );
    }

    /**
     * 
     */
    protected final static SQLWildcards sqlWildcards = new SQLWildcards('\\');
    
}
