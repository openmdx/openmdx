/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Wildcard Test 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2021, OMEX AG, Switzerland
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
package org.openmdx.base.naming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation")
public class WildcardTest {

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected Path[] paths;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected String[] xris;

    private static final String AUTHORITY = "test:openmdx:naming";

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    @BeforeEach
    public void setUp() {
        paths = new Path[]{
            new Path(new String[]{AUTHORITY, "provider", "test*openmdx*naming!wildcard"}),
            new Path(new String[]{AUTHORITY, "provider", "test*($.*open)*($.)*($.!wild)"}),
            new Path(new String[]{AUTHORITY, "provider", "test*openmdx*($.)*($.)"}),
            new Path(new String[]{AUTHORITY, "provider", ":*"}),
            new Path(new String[]{AUTHORITY, "provider", ":test*"}),
            new Path(new String[]{AUTHORITY, "provider", ":test*openmdx*naming*"}),
            new Path(new String[]{AUTHORITY, "provider", "test*($..)"}),
            new Path(new String[]{AUTHORITY, "provider", "test*openmdx*naming!wildcard*($..)"}),
            toPath(":*"),
            new Path(new String[]{AUTHORITY, "%"}),
            new Path(new String[]{AUTHORITY, "prov%"}),
            toPath("%"),
            new Path(new String[]{":*", "provider", "%"})
        };
        xris = new String[]{
            "xri://@openmdx*test.openmdx.naming/provider/test*openmdx*naming!wildcard",
            "xri://@openmdx*test.openmdx.naming/provider/test*($.*open)*($.)*($.!wild)",
            "xri://@openmdx*test.openmdx.naming/provider/test*openmdx*($.)*($.)",
            "xri://@openmdx*test.openmdx.naming/provider/($..)",
            "xri://@openmdx*test.openmdx.naming/provider/($.*test)*($..)",
            "xri://@openmdx*test.openmdx.naming/provider/($.*test*openmdx*naming)*($..)",
            "xri://@openmdx*test.openmdx.naming/provider/test*($..)",
            "xri://@openmdx*test.openmdx.naming/provider/test*openmdx*naming!wildcard*($..)",
            "xri://@openmdx*test.openmdx.naming/provider/($..)",
            "xri://@openmdx*test.openmdx.naming/($...)",
            "xri://@openmdx*test.openmdx.naming/($.*prov)*($..)/($...)",
            "xri://@openmdx*test.openmdx.naming/provider/($...)",
            "xri://@openmdx*($..)/provider/($...)"
        };
    }

    private static Path toPath(
        String provider
    ){
        return new Path(
            new String[]{
                AUTHORITY,
                "provider",
                provider
            }
        );
    }

    @Test
    public void testPathToXRI() {
        for(
            int i = 0;
            i < xris.length;
            i++
        ){
            String xri = paths[i].toXRI();
            assertEquals(xris[i], xri, "path>xri[" + i + "]");
        }
    }

    @Test
    public void testXRIToPath() {
        for(
            int i = 0;
            i < xris.length;
            i++
        ){
            Path path = new Path(xris[i]);
            Assertions.assertEquals(paths[i], path, "xri>path[" + i + "]");
        }
    }
    
    @Test
    public void testMatch(){
        Path matchAll = new Path("xri://@openmdx*test.openmdx.naming/provider/test*openmdx*naming!wildcard");
        for(Path pattern : this.paths) {
            assertTrue(matchAll.isLike(pattern), pattern.toString());
        }
        Path matchMoreSubSegments = new Path("xri://@openmdx*test.openmdx.naming/provider/test*openmdx*naming!wildcard*there*is*more");
        for(
            int i = 0;
            i < this.paths.length;
            i++
        ){
            Assertions.assertEquals(
                i > 2,
                matchMoreSubSegments.isLike(paths[i]),
                i + ": " + this.paths[i]
            );
        }
        Path matchLessSubSegments = new Path("xri://@openmdx*test.openmdx.naming/provider/test*openmdx*naming");
        for(
            int i = 0;
            i < this.paths.length;
            i++
        ){
            assertEquals(
                i > 2 && i != 7,
                matchLessSubSegments.isLike(paths[i]),
                i + ": " + this.paths[i]
            );
        }
        Path matchMoreSegments = new Path("xri://@openmdx*test.openmdx.naming/provider/test*openmdx*naming!wildcard/there*is*more");
        for(
            int i = 0;
            i < this.paths.length;
            i++
        ){
            Assertions.assertEquals(
                i > 8,
                matchMoreSegments.isLike(paths[i]),
                i + ": " + this.paths[i]
            );
        }
        Path matchLessSegments = new Path("xri://@openmdx*test.openmdx.naming/provider");
        for(
            int i = 0;
            i < this.paths.length;
            i++
        ){
            Assertions.assertEquals(
                i > 8,
                matchLessSegments.isLike(paths[i]),
                i + ": " + this.paths[i]
            );
        }
    }

    @Test
    public void testAuthority(){
        Path authorityPath = new Path(new String[]{AUTHORITY});
        Path[] authorityPattern = {
            new Path(new String[]{":*"}),
            new Path("::*"),
            new Path("xri://@openmdx*($..)")
        };
        for(Path pattern : authorityPattern) {
            Assertions.assertTrue(authorityPath.isLike(pattern), authorityPath.toXri() + " matches " + pattern.toXri());
        }
    }
    
}
