/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestSparseArray.java,v 1.3 2008/02/18 14:15:20 hburger Exp $
 * Description: class TestSparseArray 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/18 14:15:20 $
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
package org.openmdx.test.base.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.compatibility.base.collection.SparseArray;
import org.openmdx.compatibility.base.collection.TreeSparseArray;


public class TestSparseArray extends TestCase {
	
    private static final String THE   = "The";
    private static final String QUICK = "quick";
    private static final String BROWN = "brown";
    private static final String FOX   = "Fox";
	
    private static final String FOX_2 = "Fox2";
	

	/**
	 * Constructs a test case with the given name.
	 */
	public TestSparseArray(String name) {
		super(name);
	}
	
	/**
	 * The batch TestRunner can be given a class to run directly.
	 * To start the batch runner from your main you can write: 
	 */
	public static void main (String[] args) {
		junit.textui.TestRunner.run (suite());
		//junit.swingui.TestRunner.run(TestSparseArray.class);
	}
	
	/**
	 * A test runner either expects a static method suite as the
	 * entry point to get a test to run or it will extract the 
	 * suite automatically. 
	 */
	public static Test suite() {
		return new TestSuite(TestSparseArray.class);
	}

    /// <summary>
    /// Write the test case method in the fixture class.
    /// Be sure to make it public, or it can't be invoked through reflection. 
    /// </summary>
    public void testEmptySparseArray() {
        SparseArray array  = new TreeSparseArray();
        assertEquals("Start()",    -1,   array.start());
        assertEquals("End()",      0,    array.end());
        assertEquals("Count",      0,    array.size());
        assertEquals("ToString()", "[]", array.toString());

		List list = new ArrayList();
		list.add(THE);
		list.add(QUICK);
		list.add(BROWN);
		list.add(FOX);
        array  = new TreeSparseArray( list );
        array.clear();
        assertEquals("Start()",    -1,   array.start());
        assertEquals("End()",      0,    array.end());
        assertEquals("Count",      0,    array.size());
        assertEquals("ToString()", "[]", array.toString());
    }

    /// <summary>
    /// Write the test case method in the fixture class.
    /// Be sure to make it public, or it can't be invoked through reflection. 
    /// </summary>
    public void testPopulatedByCollection() {
		List list = new ArrayList();
		list.add(THE);
		list.add(QUICK);
		list.add(BROWN);
		list.add(FOX);
        SparseArray array  = new TreeSparseArray( list );
        verifySparseArray(array, 0, 4);

        assertEquals("toString()", "[0:The, 1:quick, 2:brown, 3:Fox]", array.toString());
    }

    /// <summary>
    /// Write the test case method in the fixture class.
    /// Be sure to make it public, or it can't be invoked through reflection. 
    /// </summary>
    public void testPopulatedBySparseArray() {
		List list = new ArrayList();
		list.add(THE);
		list.add(QUICK);
		list.add(BROWN);
		list.add(FOX);
        SparseArray array = new TreeSparseArray( new TreeSparseArray( list ) );
        verifySparseArray(array, 0, 4);

        assertEquals("toString()", "[0:The, 1:quick, 2:brown, 3:Fox]", array.toString());
    }
    
    /// <summary>
	/// Write the test case method in the fixture class.
	/// Be sure to make it public, or it can't be invoked through reflection. 
	/// </summary>
	public void testPopulatedAscending() {
		SparseArray array = new TreeSparseArray();
        array.set(100, THE);
		array.set(200, QUICK);
		array.set(400, BROWN);
        array.set(600, FOX);
        verifySparseArray(array, 100, 601);

        assertEquals("toString()", "[100:The, 200:quick, 400:brown, 600:Fox]", array.toString());
    }

	/// <summary>
	/// Write the test case method in the fixture class.
	/// Be sure to make it public, or it can't be invoked through reflection. 
	/// </summary>
	public void testPopulatedDescending(){
        SparseArray array = new TreeSparseArray();
        array.set(106, FOX);
        array.set(84, BROWN);
        array.set(62, QUICK);
        array.set(20, THE);
		verifySparseArray(array, 20, 107);

        assertEquals("toString()", "[20:The, 62:quick, 84:brown, 106:Fox]", array.toString());
    }

	/// <summary>
	/// Write the test case method in the fixture class.
	/// Be sure to make it public, or it can't be invoked through reflection. 
	/// </summary>
	public void testPopulatedFromCenter() {
        SparseArray array = new TreeSparseArray();
        array.set(22, QUICK);
        array.set(44, BROWN);
        array.set(11, THE);
        array.set(66, FOX);
		verifySparseArray(array, 11, 67);

        assertEquals("toString()", "[11:The, 22:quick, 44:brown, 66:Fox]", array.toString());
    }

	/// <summary>
	/// Write the test case method in the fixture class.
	/// Be sure to make it public, or it can't be invoked through reflection. 
	/// </summary>
	public void testPopulatedWithAdd() {	
        SparseArray array = new TreeSparseArray();
        array.add(THE);
        array.add(QUICK);
        array.add(BROWN);
        array.add(FOX);
        verifySparseArray(array, 0, 4);

        assertEquals("toString()", "[0:The, 1:quick, 2:brown, 3:Fox]", array.toString());
    }

    /// <summary>
    /// Write the test case method in the fixture class.
    /// Be sure to make it public, or it can't be invoked through reflection. 
    /// </summary>
    public void testAddRemove() {	
        SparseArray array = new TreeSparseArray();
        array.add(THE);
        array.add(QUICK);
        array.add(BROWN);
        array.add(FOX);
        verifySparseArray(array, 0, 4);

        assertTrue("Contains '" + FOX + "'", array.contains(FOX));
        assertTrue("Contains '" + FOX_2 + "'", !array.contains(FOX_2));

        assertEquals("Value at position 2", BROWN, array.get(2));
        array.set(2, "red");
        assertEquals("Value at position 2", "red", array.get(2));
        array.set(2, BROWN);
        verifySparseArray(array, 0, 4);

        array.set(100, FOX_2);
        assertEquals("Remove at position 100", FOX_2, array.remove(100));
        verifySparseArray(array, 0, 4);

        array.set(50, FOX_2);
        assertTrue("Remove '" + FOX_2 + "'", array.remove(FOX_2));
        verifySparseArray(array, 0, 4);

        array.set(75, FOX_2);
        assertEquals("Remove IndexOf()", FOX_2, array.remove(array.indexOf(FOX_2)));
        verifySparseArray(array, 0, 4);

        for(int i=1; i<10; i++) {
            array.add(FOX_2);
        }
        while( array.remove(FOX_2) ){}
        verifySparseArray(array, 0, 4);
    }

    /// <summary>
    /// Write the test case method in the fixture class.
    /// Be sure to make it public, or it can't be invoked through reflection. 
    /// </summary>
    public void testGetSet() {
        SparseArray array = new TreeSparseArray();
        array.set(100, THE);
        array.set(200, QUICK);
        array.set(400, BROWN);
        array.set(600, FOX);
        verifySparseArray(array, 100, 601);

        assertNotNull("SparseArray[100]", array.get(100));
        assertNotNull("SparseArray[200]", array.get(200));
        assertNotNull("SparseArray[400]", array.get(400));
        assertNotNull("SparseArray[600]", array.get(600));
        assertNull("SparseArray[17]",    array.get(17));
        assertNull("SparseArray[99]",    array.get(99));
        assertNull("SparseArray[101]",   array.get(101));
        assertNull("SparseArray[199]",   array.get(199));
        assertNull("SparseArray[601]",   array.get(601));
        assertNull("SparseArray[77777]", array.get(77777));

        assertEquals("IndexOf(the)",   100, array.indexOf(THE));
        assertEquals("IndexOf(quick)", 200, array.indexOf(QUICK));
        assertEquals("IndexOf(brown)", 400, array.indexOf(BROWN));
        assertEquals("IndexOf(fox)",   600, array.indexOf(FOX));
        assertEquals("IndexOf(fox2)",  -1,  array.indexOf(FOX_2));
    
        for(int i=150; i<153; i++) {
            array.set(i, FOX_2);
        }
        for(int i=250; i<253; i++) {
            array.set(i, FOX_2);
        }
        assertEquals("Count", 10, array.size());
        assertEquals("toString()",
                     "[100:The, 150:Fox2, 151:Fox2, 152:Fox2, 200:quick, 250:Fox2, 251:Fox2, 252:Fox2, 400:brown, 600:Fox]",
                     array.toString());

        while( array.remove(FOX_2) ){}
        verifySparseArray(array, 100, 601);

        array.set(300, FOX_2);
        array.set(300, null);      //e.g. Remove(300)
        verifySparseArray(array, 100, 601);
        array.set(199, null);
        array.set(999, null);
        verifySparseArray(array, 100, 601);
    }

    /// <summary>
    /// Write the test case method in the fixture class.
    /// Be sure to make it public, or it can't be invoked through reflection. 
    /// </summary>
    public void testClone() {
        TreeSparseArray array = new TreeSparseArray();
        array.set(100, THE);
        array.set(200, QUICK);
        array.set(400, BROWN);
        array.set(600, FOX);
        verifySparseArray(array, 100, 601);

        TreeSparseArray cloneArray = (TreeSparseArray)array.clone();
        verifySparseArray(cloneArray, 100, 601);
        
        assertEquals("toString()", array.toString(), cloneArray.toString());

        cloneArray.add(FOX_2);
        cloneArray.set(700, FOX_2);

        assertEquals("toString()",
                     "[100:The, 200:quick, 400:brown, 600:Fox, 601:Fox2, 700:Fox2]",
                     cloneArray.toString());

        verifySparseArray(array, 100, 601);
    }

    /// <summary>
    /// Write the test case method in the fixture class.
    /// Be sure to make it public, or it can't be invoked through reflection. 
    /// </summary>
    public void testSubArray() {
        SparseArray orgArray = new TreeSparseArray();
        orgArray.set(100, THE);
        orgArray.set(101, QUICK);
        orgArray.set(102, BROWN);
        orgArray.set(103, FOX);
        orgArray.set(110, "AAAA");
        orgArray.set(111, "BBBB");
        orgArray.set(112, "CCCC");
        orgArray.set(113, "DDDD");

		SparseArray array    = new TreeSparseArray(orgArray);
 
        SparseArray subArray = array.subArray(102, 112);
        assertEquals("subArray.start()", 102, subArray.start());
        assertEquals("subArray.end()",   112, subArray.end());
        assertEquals("subArray.size()",    4, subArray.size());

        assertEquals("subArray.get(103)", FOX,  subArray.get(103));
        assertNull("subArray.get(105)", subArray.get(105));
        array.set(105, "NewString");
        assertEquals("subArray.get(105)", "NewString",  subArray.get(105));
        assertEquals("subArray.toString()",
                     "[102:brown, 103:Fox, 105:NewString, 110:AAAA, 111:BBBB]",
                     subArray.toString());

        // Test GetEnumerator
        StringBuffer sb = new StringBuffer();
        for (Iterator iter = subArray.iterator(); iter.hasNext();) {
			Object element = iter.next();
			sb.append(element);
		}
        assertEquals("All Values of SubArray", "brownFoxNewStringAAAABBBB", sb.toString());
        
        // Test Clear()
        subArray.clear();
        assertEquals("array.toString()", "[100:The, 101:quick, 112:CCCC, 113:DDDD]", array.toString());
        assertEquals("array.size()", 4, array.size());
        assertEquals("subArray.toString()", "[]", subArray.toString());
        assertEquals("subArray.size()", 0, subArray.size());
        
	    // Test Add()/Set
	    array.add("ZZZZ");
	    subArray.add("ZZZZ");
	    subArray.add("XXXX");
	    subArray.set(104, "YYYY");
	    subArray.remove("ZZZZ");

        assertEquals("array.toString()", "[100:The, 101:quick, 103:XXXX, 104:YYYY, 112:CCCC, 113:DDDD, 114:ZZZZ]", array.toString());
        assertEquals("subArray.toString()", "[103:XXXX, 104:YYYY]", subArray.toString());

        assertEquals("subArray.indexOf('XXXX')", 103, subArray.indexOf("XXXX"));
        assertEquals("subArray.indexOf('YYYY')", 104, subArray.indexOf("YYYY"));
        assertEquals("subArray.indexOf('ZZZZ')",  -1, subArray.indexOf("ZZZZ"));
        assertEquals("array.indexOf('XXXX')", 103, array.indexOf("XXXX"));
        assertEquals("array.indexOf('YYYY')", 104, array.indexOf("YYYY"));
        assertEquals("array.indexOf('ZZZZ')", 114, array.indexOf("ZZZZ"));

        // Test asList()
        List list = subArray.asList();
        assertEquals("subArray.asList().size()", 3, list.size());
        for(int i=0; i<list.size(); i++) {
            if ( i == 1 || i == 2 ) {
                assertNotNull("subArray.asList().get(" + i + ")", list.get(i));
            }
            else {
                assertNull("subArray.asList().get(" + i + ")", list.get(i));
            }
        }

        // Test Clone()
        TreeSparseArray cloneArray = new TreeSparseArray(subArray);
        assertEquals("cloneArray.toString()", "[103:XXXX, 104:YYYY]", cloneArray.toString());
        cloneArray.clear();
        assertEquals("cloneArray.toString()", "[]", cloneArray.toString());
        assertEquals("subArray.toString()", "[103:XXXX, 104:YYYY]", subArray.toString());
        
        // Test SetAll()
        SparseArray saveArray = new TreeSparseArray(array);
        SparseArray newArray = new TreeSparseArray();
        newArray.set(102, "0000");
        newArray.set(103, "1111");
        newArray.set(104, "2222");
        newArray.set(105, "4444");
        subArray.clear();
        subArray.setAll(newArray);
        assertEquals("subArray.toString()", "[102:0000, 103:1111, 104:2222, 105:4444]", subArray.toString());
        array.clear();
        array.setAll(saveArray);
        assertEquals("array.toString()", "[100:The, 101:quick, 103:XXXX, 104:YYYY, 112:CCCC, 113:DDDD, 114:ZZZZ]", array.toString());
            
        // Test Contains()
        assertTrue(array.contains("XXXX"));
        assertTrue(array.contains("YYYY"));
        assertTrue(array.contains("ZZZZ"));
        assertTrue(subArray.contains("XXXX"));
        assertTrue(subArray.contains("YYYY"));
        assertTrue(!subArray.contains("ZZZZ"));

        assertEquals("subArray.remove(103)", "XXXX", subArray.remove(103));
        assertTrue("subArray.remove('YYYY')", subArray.remove("YYYY"));
        assertTrue("subArray.remove('ZZZZ')", !subArray.remove("ZZZZ"));
        assertTrue("array.remove('ZZZZ')", array.remove("ZZZZ"));

        assertEquals("array.toString()", "[100:The, 101:quick, 112:CCCC, 113:DDDD]", array.toString());
        assertEquals("subArray.toString()", "[]", subArray.toString());
    }

    /// <summary>
    /// Write the test case method in the fixture class.
    /// Be sure to make it public, or it can't be invoked through reflection. 
    /// </summary>
    public void testSubSubArray() {

        SparseArray array = new TreeSparseArray();
        array.set(100, THE);
        array.set(101, QUICK);
        array.set(102, BROWN);
        array.set(103, FOX);
        array.set(110, "AAAA");
        array.set(111, "BBBB");
        array.set(112, "CCCC");
        array.set(113, "DDDD");
        array.set(114, "EEEE");
        array.set(115, "FFFF");
        array.set(116, "GGGG");
        array.set(117, "HHHH");

        SparseArray subArray = array.subArray(102, 117);
        SparseArray subSubArray = subArray.subArray(112, 115);
        assertEquals("subArray.start()", 102, subArray.start());
        assertEquals("subArray.end()",   117, subArray.end());
        assertEquals("subSubArray.start()", 112, subSubArray.start());
        assertEquals("subSubArray.end()",   115, subSubArray.end());
        assertEquals("array.toString()",
                     "[100:The, 101:quick, 102:brown, 103:Fox, 110:AAAA, 111:BBBB, 112:CCCC, 113:DDDD, 114:EEEE, 115:FFFF, 116:GGGG, 117:HHHH]",
                     array.toString());
        assertEquals("SubArray.toString()",
                     "[102:brown, 103:Fox, 110:AAAA, 111:BBBB, 112:CCCC, 113:DDDD, 114:EEEE, 115:FFFF, 116:GGGG]",
                     subArray.toString());
        assertEquals("subSubArray.toString()",
                     "[112:CCCC, 113:DDDD, 114:EEEE]",
                     subSubArray.toString());

        subSubArray.clear();
        subSubArray.set(113, "Hallo");
        subSubArray.add("Joe");
        assertEquals("array.toString()",
                     "[100:The, 101:quick, 102:brown, 103:Fox, 110:AAAA, 111:BBBB, 113:Hallo, 114:Joe, 115:FFFF, 116:GGGG, 117:HHHH]",
                     array.toString());
        assertEquals("SubArray.toString()",
                     "[102:brown, 103:Fox, 110:AAAA, 111:BBBB, 113:Hallo, 114:Joe, 115:FFFF, 116:GGGG]",
                     subArray.toString());
        assertEquals("subSubArray.toString()",
                     "[113:Hallo, 114:Joe]",
                     subSubArray.toString());
                     
        SparseArray cloneSubSubArray = new TreeSparseArray(subSubArray);

        assertEquals("subArray.indexOf('Hallo')", 113, subArray.indexOf("Hallo"));
        assertEquals("subsubArray.indexOf('Hallo')", 113, subSubArray.indexOf("Hallo"));
        subSubArray.remove("Hallo");
        assertEquals("array.toString()",
                     "[100:The, 101:quick, 102:brown, 103:Fox, 110:AAAA, 111:BBBB, 114:Joe, 115:FFFF, 116:GGGG, 117:HHHH]",
                     array.toString());
        assertEquals("SubArray.toString()",
                     "[102:brown, 103:Fox, 110:AAAA, 111:BBBB, 114:Joe, 115:FFFF, 116:GGGG]",
                     subArray.toString());
        assertEquals("subSubArray.toString()",
                     "[114:Joe]",
                     subSubArray.toString());
		assertEquals("cloneSubSubArray.toString()",
		             "[113:Hallo, 114:Joe]",
		             cloneSubSubArray.toString());
    }
    
    //------------------------------------------------------------------------
	// Verify method
	//------------------------------------------------------------------------

	public void verifySparseArray(SparseArray array,
                                  int expectedStartValue,
                                  int expectedEndValue) {
        // Test GetEnumerator
        StringBuffer sb = new StringBuffer();
        for (Iterator iter = array.iterator(); iter.hasNext();) {
			Object element = iter.next();
			sb.append(element);
		}
        assertEquals("All values of SparseArray", "ThequickbrownFox", sb.toString());

        assertEquals("SparsArray.size",   4,                   array.size());
        assertEquals("SparsArray.start()", expectedStartValue, array.start());
        assertEquals("SparsArray.end()",   expectedEndValue,   array.end());

        assertEquals( "SparseArray.AsList().Count", expectedEndValue, array.asList().size() );
    }
	
}
