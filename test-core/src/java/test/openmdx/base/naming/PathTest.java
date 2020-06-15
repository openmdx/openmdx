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
package test.openmdx.base.naming;

import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.xri.XRIAuthorities;
import org.openmdx.kernel.xri.XRI_2Protocols;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PathTest extends TestCase {

    /**
     * Constructs a test case with the given name.
     */
    public PathTest(String name) {
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
        return new TestSuite(PathTest.class);
    }

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
    @Override
    protected void setUp() {
        path1 = new Path( new String[0] );
        path2 = new Path( new String[]{ "a" } );
        path3 = new Path( new String[]{ "a", "b0/b1", "c" } );
        path4 = new Path( new String[]{ "a", "b;x=0;y=1,2,3,z=false", "c" } );
    }
    
    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testLength() {
        assertEquals("Path('').length()", 0, path1.size() );
        assertEquals("Path('a').length()", 1, path2.size() );
        assertEquals("Path('a/b0//b1/c').length()", 3, path3.size() );
        assertEquals("Path('a/b;x=0;y=1,2,3,z=false/c').length()", 3, path3.size() );
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testBase (
    ) throws Throwable {
        
        try {
            path1.getSegment(path1.size()-1).toClassicRepresentation();
            fail ("Path('').getBase()");
        } catch (ArrayIndexOutOfBoundsException exception) {
        }
                 
        String   base2 = path2.getSegment(path2.size()-1).toClassicRepresentation();
        String   base3 = path3.getSegment(path3.size()-1).toClassicRepresentation();
        
        assertEquals("Path('a').getBase()"      , "a", base2 );
        assertEquals("Path('a/b0//b1/c').getBase()" , "c", base3 );
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
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
        
        assertEquals("Path('a').getParent()", result2, parent2 );
        assertEquals("Path('a/b0//b1/c').getParent()", result3, parent3 );
        assertEquals("Path('a/b;x=0;y=1,2,3,z=false/c').getParent()", result4, parent4 );
    }
    
    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
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

        assertEquals("Path('').clone()",        path1, path1_c );
        assertEquals("Path('a').clone()",       path2, path2_c );
        assertEquals("Path('a/b0//b1/c').clone()",  path3, path3_c );
        assertEquals("Path('a/b;x=0;y=1,2,3,z=false/c').clone()",   path4, path4_c );
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */    
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
        
        assertEquals("Path('').getDescendant({'1234'})", result1, path1_c);
        assertEquals("Path('a').getDescendant({'1234'})", result2, path2_c);
        assertEquals("Path('a').getDescendant({'1234'}).append({'5678'})", result3, path3_c);
        assertEquals("Path('a/b0//b1/c').getDescendant({'1234','5678'})", result4, path4_c);
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testToXRI (
    ) throws Throwable {
        assertEquals(
            "Path('').toXRI()",
            "xri://@openmdx",
            path1.toXRI()
        );
        assertEquals(
            "Path('a').toXRI()",
            "xri://@openmdx*a",
            path2.toXRI()
        );
        assertEquals(
            "Path('a/b0//b1/c').toXRI()",
            "xri://@openmdx*a/(@openmdx*b0/b1)/c",
            path3.toXRI()
        );
        assertEquals(
            "Path('a/b;x=0;y=1,2,3,z=false/c').toXRI()",
            "xri://@openmdx*a/($t*ces*b%3Bx%3D0%3By%3D1%2C2%2C3%2Cz%3Dfalse)/c",
            path4.toXRI()
        );
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    @SuppressWarnings("deprecation")
    public void testToUri (
    ) throws Throwable {
        assertEquals(
            "Path('').toUri()",
            "spice:/",
            path1.toUri()
        );
        assertEquals(
            "Path('a').toUri()",
            "spice://a",
            path2.toUri()
        );
        assertEquals(
            "Path('a/b0//b1/c').toUri()",
            "spice://a/b0%2fb1/c",
            path3.toUri()
        );
        assertEquals(
            "Path('a/b;x=0;y=1,2,3,z=false/c').toUri()",
            "spice://a/b;x=0;y=1,2,3,z=false/c",
            path4.toUri()
        );
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testFromString (
    ) throws Throwable {
        assertEquals("Path('')",        path1, new Path (""));
        assertEquals("Path('a')",       path2, new Path ("a"));
        assertEquals("Path('a/b0//b1/c')",  path3, new Path ("a/b0//b1/c"));
        assertEquals("Path('a/b;x=0;y=1,2,3,z=false/c')",   path4, new Path ("a/b;x=0;y=1,2,3,z=false/c"));
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testFromUri (
    ) throws Throwable {
        try {
            assertEquals("Path('')",        path1, new Path ("spice:/"));
            assertEquals("Path('a')",       path2, new Path ("spice://a"));
            assertEquals("Path('a/b0//b1/c')",  path3, new Path ("spice://a/b0%2fb1/c"));
            assertEquals("Path('a/b;x=0;y=1,2,3,z=false/c')",   path4, new Path ("spice://a/b;x=0;y=1,2,3,z=false/c"));
        } catch (RuntimeException exception) {
            Throwables.log(exception);
            throw exception;
        }
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
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
    
    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
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
        
        assertTrue(path1 + " is like " + pattern0, path1.isLike(pattern0));
        assertTrue(path2 + " is like " + pattern0, path2.isLike(pattern0));
        assertFalse(path3 + " is not like " + pattern0, path3.isLike(pattern0));
        assertFalse(path4 + " is not like " + pattern0, path4.isLike(pattern0));
        assertFalse(path5 + " is not like " + pattern0, path5.isLike(pattern0));
        assertTrue(path6 + " is like " + pattern1, path6.isLike(pattern1));
        assertTrue(path6 + " is like " + pattern2, path6.isLike(pattern2));
        assertFalse(path6 + " is like " + pattern3, path6.isLike(pattern3));

        pattern0 = pattern0.getPrefix(2);
        assertFalse(path1 + " is not like " + pattern0, path1.isLike(pattern0));
        assertTrue(path2 + " is like " + pattern0, path2.isLike(pattern0));
        assertFalse(path3 + " is not like " + pattern0, path3.isLike(pattern0));
        assertFalse(path4 + " is not like " + pattern0, path4.isLike(pattern0));
        assertFalse(path5 + " is not like " + pattern0, path5.isLike(pattern0));

        pattern0 = pattern0.getPrefix(1);
        assertFalse(path1 + " is not like " + pattern0, path1.isLike(pattern0));
        assertFalse(path2 + " is not like " + pattern0, path2.isLike(pattern0));
        assertFalse(path3 + " is not like " + pattern0, path3.isLike(pattern0));
        assertFalse(path4 + " is not like " + pattern0, path4.isLike(pattern0));
        assertTrue(path5 + " is like " + pattern0, path5.isLike(pattern0));

        pattern0 = pattern0.getChild("b:%");
        assertTrue(path1 + " is like " + pattern0, path1.isLike(pattern0));
        assertTrue(path2 + " is like " + pattern0, path2.isLike(pattern0));
        assertFalse(path3 + " is not like " + pattern0, path3.isLike(pattern0));
        assertFalse(path4 + " is not like " + pattern0, path4.isLike(pattern0));
        assertFalse(path5 + " is not like " + pattern0, path5.isLike(pattern0));
        
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
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
        assertEquals("p1", new Path(new String[]{"a","b","c"}), p1);
        assertEquals("p2", new Path(new String[]{"1","2","3"}), p2);
        assertEquals("p1", "xri://@openmdx*a/b/c", p1.toXRI());
        assertEquals("p2", "xri://@openmdx*1/2/3", p2.toXRI());
        assertEquals("p1", "xri:@openmdx:a/b/c", p1.toXri());
        assertEquals("p2", "xri:@openmdx:1/2/3", p2.toXri());
		assertEquals("recommended", expected, recommended);
        assertEquals("alternative", expected, alternative);
        assertEquals("legacy", expected, legacy);
        assertEquals("expected", "xri://@openmdx*a/b/c/(@openmdx*1/2/3)", expected.toXRI());
        assertEquals("child", p2, new Path(expected.getLastSegment().toClassicRepresentation()));
    }
    
    public void testURI(
    ){
        assertEquals("A/B B/C+C/D&D/E-E/F:F/G=G", "xri://@openmdx*A/($t*ces*B%20B)/($t*ces*C%2BC)/D&D/E-E/F:F/($t*ces*G%3DG)", new Path(new String[]{"A","B B","C+C","D&D","E-E","F:F","G=G"}).toXRI());
        assertEquals("A/B B/C+C/D&D/E-E/F:F/G=G", "@openmdx*A/($t*ces*B%2520B)/($t*ces*C%252BC)/D&D/E-E/F:F/($t*ces*G%253DG)", new Path(new String[]{"A","B B","C+C","D&D","E-E","F:F","G=G"}).toURI());
        assertEquals("A/B B/C+C/D&D/E-E/F:F/G=G", new Path(new String[]{"A","B B","C+C","D&D","E-E","F:F","G=G"}),new Path("@openmdx*A/($t*ces*B%2520B)/($t*ces*C%252BC)/D&D/E-E/F:F/($t*ces*G%253DG)"));
        assertEquals("@openmdx!($t*uuid*cf730615-beb8-11e9-a5fb-afa7e42b2e0b)", new Path("xri://@openmdx!($t*uuid*cf730615-beb8-11e9-a5fb-afa7e42b2e0b)").toURI());
    }

    public void testTransient(
    ){
        assertEquals("xri://@openmdx!($t*uuid*cf730615-beb8-11e9-a5fb-afa7e42b2e0b)", new Path("xri://@openmdx!($t*uuid*cf730615-beb8-11e9-a5fb-afa7e42b2e0b)").toXRI());
        assertEquals("@openmdx!($t*uuid*cf730615-beb8-11e9-a5fb-afa7e42b2e0b)", new Path("xri://@openmdx!($t*uuid*cf730615-beb8-11e9-a5fb-afa7e42b2e0b)").toURI());
    }

    public void testEmpty(
    ){
        assertEquals(XRI_2Protocols.OPENMDX_PREFIX, path1.toXRI());
        assertEquals(XRIAuthorities.OPENMDX_AUTHORITY, path1.toURI());
    }
    
}
