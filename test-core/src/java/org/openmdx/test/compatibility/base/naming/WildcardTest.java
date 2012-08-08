/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: WildcardTest.java,v 1.3 2008/08/22 09:08:03 hburger Exp $
 * Description: class TestPath 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/08/22 09:08:03 $
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
package org.openmdx.test.compatibility.base.naming;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.naming.PathComponent;
import org.openxri.XRI;

public class WildcardTest extends TestCase {

    /**
     * Constructs a test case with the given name.
     */
    public WildcardTest(String name) {
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
        return new TestSuite(WildcardTest.class);
    }

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected Path[] paths;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected String[] xris;

    private static final PathComponent AUTHORITY = new PathComponent(
        new String[]{"test","openmdx","naming"}
    );

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() {
        paths = new Path[]{
            new Path(new String[]{AUTHORITY.toString(), "provider", "test*openmdx*naming!wildcard"}),
            new Path(new String[]{AUTHORITY.toString(), "provider", "test*($.*open)*($.)*($.!wild)"}),
            new Path(new String[]{AUTHORITY.toString(), "provider", "test*openmdx*($.)*($.)"}),
            new Path(new String[]{AUTHORITY.toString(), "provider", ":test*"}),
            new Path(new String[]{AUTHORITY.toString(), "provider", ":test*openmdx*naming*"}),
            new Path(new String[]{AUTHORITY.toString(), "provider", "test*($..)"}),
            new Path(new String[]{AUTHORITY.toString(), "provider", "test*openmdx*naming!wildcard*($..)"}),
            toPath("", "*"),
            new Path(new String[]{AUTHORITY.toString(), "%"}),
            new Path(new String[]{AUTHORITY.toString(), "prov%"}),
            toPath("%")
        };
        xris = new String[]{
            "xri://@openmdx*test.openmdx.naming/provider/test*openmdx*naming!wildcard",
            "xri://@openmdx*test.openmdx.naming/provider/test*($.*open)*($.)*($.!wild)",
            "xri://@openmdx*test.openmdx.naming/provider/test*openmdx*($.)*($.)",
            "xri://@openmdx*test.openmdx.naming/provider/($.*test)*($..)",
            "xri://@openmdx*test.openmdx.naming/provider/($.*test*openmdx*naming)*($..)",
            "xri://@openmdx*test.openmdx.naming/provider/test*($..)",
            "xri://@openmdx*test.openmdx.naming/provider/test*openmdx*naming!wildcard*($..)",
            "xri://@openmdx*test.openmdx.naming/provider/($..)",
            "xri://@openmdx*test.openmdx.naming/($...)",
            "xri://@openmdx*test.openmdx.naming/($.*prov)*($..)/($...)",
            "xri://@openmdx*test.openmdx.naming/provider/($...)"
        };
    }

    protected static Path toPath(
        String... suffix
    ){
        return new Path(
            new String[]{
                AUTHORITY.toString(),
                "provider",
                new PathComponent(suffix).toString()
            }
        );
    }
    
    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testPathToXRI() {
        for(
            int i = 0;
            i < xris.length;
            i++
        ){
            XRI xri = paths[i].toXRI();
            assertEquals("path>xri[" + i + "]", xris[i], xri.toString());
        }
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testXRIToPath() {
        for(
            int i = 0;
            i < xris.length;
            i++
        ){
            Path path = new Path(xris[i]);
            assertEquals("xri>path[" + i + "]", paths[i], path);
        }
    }
    
    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testMatch(){
        Path matchAll = new Path("xri://@openmdx*test.openmdx.naming/provider/test*openmdx*naming!wildcard");
        for(Path pattern : this.paths) {
            assertTrue(pattern.toString(), matchAll.isLike(pattern));
        }
        Path matchMoreSubSegments = new Path("xri://@openmdx*test.openmdx.naming/provider/test*openmdx*naming!wildcard*there*is*more");
        for(
            int i = 0;
            i < this.paths.length;
            i++
        ){
            assertEquals(
                i + ": " + this.paths[i], 
                i > 2,
                matchMoreSubSegments.isLike(paths[i])
            );
        }
        Path matchLessSubSegments = new Path("xri://@openmdx*test.openmdx.naming/provider/test*openmdx*naming");
        for(
            int i = 0;
            i < this.paths.length;
            i++
        ){
            assertEquals(
                i + ": " + this.paths[i], 
                i > 2 && i != 6,
                matchLessSubSegments.isLike(paths[i])
            );
        }
        Path matchMoreSegments = new Path("xri://@openmdx*test.openmdx.naming/provider/test*openmdx*naming!wildcard/there*is*more");
        for(
            int i = 0;
            i < this.paths.length;
            i++
        ){
            assertEquals(
                i + ": " + this.paths[i], 
                i > 7,
                matchMoreSegments.isLike(paths[i])
            );
        }
        Path matchLessSegments = new Path("xri://@openmdx*test.openmdx.naming/provider");
        for(
            int i = 0;
            i < this.paths.length;
            i++
        ){
            assertEquals(
                i + ": " + this.paths[i], 
                i > 7,
                matchLessSegments.isLike(paths[i])
            );
        }
    }

}
