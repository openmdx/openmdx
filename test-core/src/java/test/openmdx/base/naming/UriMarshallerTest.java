/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UriMarshallerTest.java,v 1.1 2009/03/06 10:38:42 hburger Exp $
 * Description: TestUriMarshaller
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/06 10:38:42 $
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
package test.openmdx.base.naming;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

public class UriMarshallerTest extends TestCase {

    /**
     * Constructs a test case with the given name.
     */
    public UriMarshallerTest(String name) {
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
        return new TestSuite(UriMarshallerTest.class);
    }

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected String[][] components;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected Path[] paths;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected String[] uris;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() {
        components = new String[][]{
            new String[]{},
            new String[]{"A"},
            new String[]{"A","B:B0/B1","C"},
            new String[]{"A","B/B0::B1","C"},
            new String[]{"Fran\u00e7ois"}, // "François"
            new String[]{"A","provider","P","segment","S","object","RR_1:0:"},
            new String[]{"A","provider","P","segment","S","object",":012345"}
        };
        paths = new Path[]{
            new Path(components[0]),
            new Path(components[1]),
            new Path(components[2]),
            new Path(components[3]),
            new Path(components[4]),
            new Path(components[5]),
            new Path(components[6])
        };
        uris = new String[]{
            "spice:/",
            "spice://A",
            "spice://A/B:B0%2fB1/C",
            "spice://A/B%2fB0%3a%3aB1/C",
            "spice://Fran%c3%a7ois",
            "spice://A/provider/P/segment/S/object/RR_1:0:",
            "spice://A/provider/P/segment/S/object/:012345"
        };
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testEncode(
    ) throws ServiceException {
        for (
            int index = 0;
            index < components.length;
            index++
        ) assertEquals(
            Arrays.asList(components[index]).toString(),
            uris[index], 
            paths[index].toUri()
        );
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testDecode() {
        for (
            int index = 0;
            index < components.length;
            index++
        ) assertEquals(
            Arrays.asList(components[index]).toString(),
            paths[index], 
            new Path(uris[index])
        );
    }
    
}
