/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: class TestPath 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.naming;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.xri.XRIAuthorities;
import org.openmdx.kernel.xri.XRI_2Protocols;

public class PathTest {

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected Path    path1;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected Path    path2;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected Path    path3;
    
    /**
     * Add an instance variable for each part of the fixture 
     */
    protected Path    path4;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    @BeforeEach
    public void setUp() {
        path1 = new Path( new String[0] );
        path2 = new Path( new String[]{ "a" } );
        path3 = new Path( new String[]{ "a", "b0/b1", "c" } );
        path4 = new Path( new String[]{ "a", "b;x=0;y=1,2,3,z=false", "c" } );
    }

    @Test
    public void testLength() {
        Assertions.assertEquals(0, path1.size(), "Path('').length()");
        Assertions.assertEquals(1, path2.size(), "Path('a').length()");
        Assertions.assertEquals(3, path3.size(), "Path('a/b0//b1/c').length()");
        Assertions.assertEquals(3, path3.size(), "Path('a/b;x=0;y=1,2,3,z=false/c').length()");
    }

    @Test
    public void testBase (
    ) throws Throwable {
        
        try {
            path1.getSegment(path1.size()-1).toClassicRepresentation();
            fail ("Path('').getBase()");
        } catch (ArrayIndexOutOfBoundsException exception) {
        }
                 
        String   base2 = path2.getSegment(path2.size()-1).toClassicRepresentation();
        String   base3 = path3.getSegment(path3.size()-1).toClassicRepresentation();
        
        Assertions.assertEquals("a", base2, "Path('a').getBase()");
        Assertions.assertEquals("c", base3, "Path('a/b0//b1/c').getBase()");
    }

    @Test
    public void testParent(
    ) throws Throwable {
        
        try {
            path1.getPrefix(path1.size()-1);
            fail ("Path('').getParent()");
        } catch (ArrayIndexOutOfBoundsException exception) {
        }
         
        Path   parent2 = (Path) path2.getPrefix(path2.size()-1);
        Path   parent3 = (Path) path3.getPrefix(path3.size()-1);
        Path   parent4 = (Path) path4.getPrefix(path4.size()-1);

        Path   result2 = new Path( new String[0] );
        Path   result3 = new Path( new String[]{ "a", "b0/b1" } );
        Path   result4 = new Path( new String[]{ "a", "b;x=0;y=1,2,3,z=false" } );
        
        Assertions.assertEquals(result2, parent2, "Path('a').getParent()");
        Assertions.assertEquals(result3, parent3, "Path('a/b0//b1/c').getParent()");
        Assertions.assertEquals(result4, parent4, "Path('a/b;x=0;y=1,2,3,z=false/c').getParent()");
    }
    
    @Test
    @SuppressWarnings("deprecation")
	public void testClone(
    ) throws Throwable {
        Path   path1_c = (Path)path1.clone();
        Path   path2_c = (Path)path2.clone();
        Path   path3_c = (Path)path3.clone();
        Path   path4_c = (Path)path4.clone();

        assertNotNull(path1_c);
        assertNotNull(path2_c);
        assertNotNull(path3_c);
        assertNotNull(path4_c);

        Assertions.assertEquals(path1, path1_c, "Path('').clone()");
        Assertions.assertEquals(path2, path2_c, "Path('a').clone()");
        Assertions.assertEquals(path3, path3_c, "Path('a/b0//b1/c').clone()");
        Assertions.assertEquals(path4, path4_c, "Path('a/b;x=0;y=1,2,3,z=false/c').clone()");
    }

    @Test
	public void testDescendant(
    ) throws Throwable {
        Path   path1_c = path1.getChild("1234"); 
        Path   path2_c = path2.getChild("1234");
        Path   path3_c = path2.getChild("1234").getChild("5678");
        Path   path4_c = path3.getDescendant("1234", "5678");

        Path   result1 = new Path( new String[]{"1234"} );
        Path   result2 = new Path( new String[]{"a","1234"} );
        Path   result3 = new Path( new String[]{"a","1234","5678"} );
        Path   result4 = new Path( new String[]{"a","b0/b1","c","1234","5678"} );

        assertNotNull(path1_c);
        assertNotNull(path2_c);
        assertNotNull(path3_c);
        assertNotNull(path4_c);
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertNotNull(result4);
        
        Assertions.assertEquals(result1, path1_c, "Path('').getDescendant({'1234'})");
        Assertions.assertEquals(result2, path2_c, "Path('a').getDescendant({'1234'})");
        Assertions.assertEquals(result3, path3_c, "Path('a').getDescendant({'1234'}).append({'5678'})");
        Assertions.assertEquals(result4, path4_c, "Path('a/b0//b1/c').getDescendant({'1234','5678'})");
    }

    @Test
    public void testToXRI (
    ) throws Throwable {
        Assertions.assertEquals("xri://@openmdx", path1.toXRI(), "Path('').toXRI()");
        Assertions.assertEquals("xri://@openmdx*a", path2.toXRI(), "Path('a').toXRI()");
        Assertions.assertEquals("xri://@openmdx*a/(@openmdx*b0/b1)/c", path3.toXRI(), "Path('a/b0//b1/c').toXRI()");
        Assertions.assertEquals("xri://@openmdx*a/($t*ces*b%3Bx%3D0%3By%3D1%2C2%2C3%2Cz%3Dfalse)/c", path4.toXRI(), "Path('a/b;x=0;y=1,2,3,z=false/c').toXRI()");
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testToUri (
    ) throws Throwable {
        Assertions.assertEquals("spice:/", path1.toUri(), "Path('').toUri()");
        Assertions.assertEquals("spice://a", path2.toUri(), "Path('a').toUri()");
        Assertions.assertEquals("spice://a/b0%2fb1/c", path3.toUri(), "Path('a/b0//b1/c').toUri()");
        Assertions.assertEquals("spice://a/b;x=0;y=1,2,3,z=false/c", path4.toUri(), "Path('a/b;x=0;y=1,2,3,z=false/c').toUri()");
    }

    @Test
    public void testFromString (
    ) throws Throwable {
        Assertions.assertEquals(path1, new Path (""), "Path('')");
        Assertions.assertEquals(path2, new Path ("a"), "Path('a')");
        Assertions.assertEquals(path3, new Path ("a/b0//b1/c"), "Path('a/b0//b1/c')");
        Assertions.assertEquals(path4, new Path ("a/b;x=0;y=1,2,3,z=false/c"), "Path('a/b;x=0;y=1,2,3,z=false/c')");
    }

    @Test
    public void testFromUri (
    ) throws Throwable {
        try {
            Assertions.assertEquals(path1, new Path ("spice:/"), "Path('')");
            Assertions.assertEquals(path2, new Path ("spice://a"), "Path('a')");
            Assertions.assertEquals(path3, new Path ("spice://a/b0%2fb1/c"), "Path('a/b0//b1/c')");
            Assertions.assertEquals(path4, new Path ("spice://a/b;x=0;y=1,2,3,z=false/c"), "Path('a/b;x=0;y=1,2,3,z=false/c')");
        } catch (RuntimeException exception) {
            Throwables.log(exception);
            throw exception;
        }
    }

    @Test
    @SuppressWarnings("deprecation")
	public void testNull (
    ) throws Throwable {
        
        String      null1   = null;
        String []   null2   = null;
        Path        null3   = null;
        
        try {
            new Path (null1);
            fail ("Null String");
        } catch (NullPointerException exception) {
        }
        try {
            new Path (null2);
            fail ("Null String []");
        } catch (NullPointerException exception) {
        }
        try {
            new Path (null3);
            fail ("Null List");
        } catch (NullPointerException exception) {
        }
        
    }
    
    @Test
    public void testIsLike (
    ) throws Throwable {
        Path pattern0 = new Path( new String[]{ "a0", ":b*", "%" } );
        Path pattern1 = new Path("xri:@openmdx:a1:a11/provider/:*/segment/:*/parameter1D");
        Path pattern2 = new Path("xri:@openmdx:a1:a11/provider/:*/segment/:*/parameter1D/%");
        Path pattern3 = new Path("xri:@openmdx:a1:a11/provider/:*/segment/:*/parameter1D/:*");
        
        Path path1 = new Path( new String[]{ "a0", "b:1", "c3" } );    
        Path path2 = new Path( new String[]{ "a0", "b:18c3" } );       
        Path path3 = new Path( new String[]{ "a1", "b:1", "c3" } );    
        Path path4 = new Path( new String[]{ "a0", "c:3b1", "c3" } );  
        Path path5 = new Path( new String[]{ "a0" } );                
        Path path6 = new Path( "a1::a11/provider/PARAMETER/segment/UNIT_TEST/parameter1D");
        
        Assertions.assertTrue(path1.isLike(pattern0), path1 + " is like " + pattern0);
        Assertions.assertTrue(path2.isLike(pattern0), path2 + " is like " + pattern0);
        Assertions.assertFalse(path3.isLike(pattern0), path3 + " is not like " + pattern0);
        Assertions.assertFalse(path4.isLike(pattern0), path4 + " is not like " + pattern0);
        Assertions.assertFalse(path5.isLike(pattern0), path5 + " is not like " + pattern0);
        Assertions.assertTrue(path6.isLike(pattern1), path6 + " is like " + pattern1);
        Assertions.assertTrue(path6.isLike(pattern2), path6 + " is like " + pattern2);
        Assertions.assertFalse(path6.isLike(pattern3), path6 + " is like " + pattern3);

        pattern0 = pattern0.getPrefix(2);
        Assertions.assertFalse(path1.isLike(pattern0), path1 + " is not like " + pattern0);
        Assertions.assertTrue(path2.isLike(pattern0), path2 + " is like " + pattern0);
        Assertions.assertFalse(path3.isLike(pattern0), path3 + " is not like " + pattern0);
        Assertions.assertFalse(path4.isLike(pattern0), path4 + " is not like " + pattern0);
        Assertions.assertFalse(path5.isLike(pattern0), path5 + " is not like " + pattern0);

        pattern0 = pattern0.getPrefix(1);
        Assertions.assertFalse(path1.isLike(pattern0), path1 + " is not like " + pattern0);
        Assertions.assertFalse(path2.isLike(pattern0), path2 + " is not like " + pattern0);
        Assertions.assertFalse(path3.isLike(pattern0), path3 + " is not like " + pattern0);
        Assertions.assertFalse(path4.isLike(pattern0), path4 + " is not like " + pattern0);
        Assertions.assertTrue(path5.isLike(pattern0), path5 + " is like " + pattern0);

        pattern0 = pattern0.getChild("b:%");
        Assertions.assertTrue(path1.isLike(pattern0), path1 + " is like " + pattern0);
        Assertions.assertTrue(path2.isLike(pattern0), path2 + " is like " + pattern0);
        Assertions.assertFalse(path3.isLike(pattern0), path3 + " is not like " + pattern0);
        Assertions.assertFalse(path4.isLike(pattern0), path4 + " is not like " + pattern0);
        Assertions.assertFalse(path5.isLike(pattern0), path5 + " is not like " + pattern0);
        
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testCrossReferences (
    ) throws Throwable {
    	// Arrange
    	final Path expected = new Path(new String[]{"a","b","c", "1/2/3"});
        Path p1 = new Path("xri://@openmdx*a/b/c");
        Path p2 = new Path("xri://@openmdx*1/2/3");
        // Act
        Path recommended = p1.getChild(p2);
        Path alternative = new Path(p1.toXRI() + "/(" + p2.toXRI().substring(6) + ")");
        Path legacy = p1.getChild(p2.toClassicRepresentation());
        // Assert
        Assertions.assertEquals(new Path(new String[]{"a","b","c"}), p1, "p1");
        Assertions.assertEquals(new Path(new String[]{"1","2","3"}), p2, "p2");
        Assertions.assertEquals("xri://@openmdx*a/b/c", p1.toXRI(), "p1");
        Assertions.assertEquals("xri://@openmdx*1/2/3", p2.toXRI(), "p2");
        Assertions.assertEquals("xri:@openmdx:a/b/c", p1.toXri(), "p1");
        Assertions.assertEquals("xri:@openmdx:1/2/3", p2.toXri(), "p2");
		Assertions.assertEquals(expected, recommended, "recommended");
        Assertions.assertEquals(expected, alternative, "alternative");
        Assertions.assertEquals(expected, legacy, "legacy");
        Assertions.assertEquals("xri://@openmdx*a/b/c/(@openmdx*1/2/3)", expected.toXRI(), "expected");
        Assertions.assertEquals(p2, new Path(expected.getLastSegment().toClassicRepresentation()), "child");
    }
    
    @Test
    public void testURI(
    ){
        Assertions.assertEquals("xri://@openmdx*A/($t*ces*B%20B)/($t*ces*C%2BC)/D&D/E-E/F:F/($t*ces*G%3DG)", new Path(new String[]{"A","B B","C+C","D&D","E-E","F:F","G=G"}).toXRI(), "A/B B/C+C/D&D/E-E/F:F/G=G");
        Assertions.assertEquals("@openmdx*A/($t*ces*B%2520B)/($t*ces*C%252BC)/D&D/E-E/F:F/($t*ces*G%253DG)", new Path(new String[]{"A","B B","C+C","D&D","E-E","F:F","G=G"}).toURI(), "A/B B/C+C/D&D/E-E/F:F/G=G");
        Assertions.assertEquals(new Path(new String[]{"A","B B","C+C","D&D","E-E","F:F","G=G"}), new Path("@openmdx*A/($t*ces*B%2520B)/($t*ces*C%252BC)/D&D/E-E/F:F/($t*ces*G%253DG)"), "A/B B/C+C/D&D/E-E/F:F/G=G");
        Assertions.assertEquals("@openmdx!($t*uuid*cf730615-beb8-11e9-a5fb-afa7e42b2e0b)", new Path("xri://@openmdx!($t*uuid*cf730615-beb8-11e9-a5fb-afa7e42b2e0b)").toURI());
    }

    @Test
    public void testTransient(
    ){
        Assertions.assertEquals("xri://@openmdx!($t*uuid*cf730615-beb8-11e9-a5fb-afa7e42b2e0b)", new Path("xri://@openmdx!($t*uuid*cf730615-beb8-11e9-a5fb-afa7e42b2e0b)").toXRI());
        Assertions.assertEquals("@openmdx!($t*uuid*cf730615-beb8-11e9-a5fb-afa7e42b2e0b)", new Path("xri://@openmdx!($t*uuid*cf730615-beb8-11e9-a5fb-afa7e42b2e0b)").toURI());
    }

    @Test
    public void testEmpty(
    ){
        Assertions.assertEquals(XRI_2Protocols.OPENMDX_PREFIX, path1.toXRI());
        Assertions.assertEquals(XRIAuthorities.OPENMDX_AUTHORITY, path1.toURI());
    }
    
}
