/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestSparseArray.java,v 1.1 2009/03/05 14:55:56 hburger Exp $
 * Description: Test SparseArray 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/05 14:55:56 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2009, OMEX AG, Switzerland
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
package test.w3c.cci2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.junit.Test;
import org.w3c.cci2.SortedMaps;
import org.w3c.cci2.SparseArray;

/**
 * Test SparseArray
 */
public class TestSparseArray {
	
    private static final String THE   = "The";
    private static final String QUICK = "quick";
    private static final String BROWN = "brown";
    private static final String FOX   = "Fox";
	
    private static final String FOX_2 = "Fox2";
	
    private static SparseArray<String> newStringSparseArray(){
        return SortedMaps.asSparseArray(
            new TreeMap<Integer, String>()
        );        
    }

    private static SparseArray<String> newStringSparseArray(
        List<String> source
    ){
        SparseArray<String> array = newStringSparseArray();
        array.asList().addAll(source);
        return array;
    }

    private static SparseArray<String> newStringSparseArray(
        SparseArray<String> source
    ){
        SparseArray<String> array = newStringSparseArray();
        array.putAll(source);
        return array;
    }

    @Test
    public void testEmptySparseArray() {
        SparseArray<String> array  = newStringSparseArray();
        try {
            array.firstKey();
            fail("Start()");
        } catch (NoSuchElementException expected) {
        }
        try {
            array.lastKey();
            fail("End()");
        } catch (NoSuchElementException expected) {
        }
        assertEquals("Count",      0,    array.size());
        assertEquals("ToString()", "{}", array.toString());

		List<String> list = new ArrayList<String>();
		list.add(THE);
		list.add(QUICK);
		list.add(BROWN);
		list.add(FOX);
        array  = newStringSparseArray(list);
        array.clear();
        try {
            array.firstKey();
            fail("Start()");
        } catch (NoSuchElementException expected) {
        }
        try {
            array.lastKey();
            fail("End()");
        } catch (NoSuchElementException expected) {
        }
        assertEquals("Count",      0,    array.size());
        assertEquals("ToString()", "{}", array.toString());
    }

    @Test
    public void testPopulatedByCollection() {
		List<String> list = new ArrayList<String>();
		list.add(THE);
		list.add(QUICK);
		list.add(BROWN);
		list.add(FOX);
        SparseArray<String> array  = newStringSparseArray(list);
        verifySparseArray(array, 0, 4);

        assertEquals("toString()", "{0=The, 1=quick, 2=brown, 3=Fox}", array.toString());
    }

    @Test
    public void testPopulatedBySparseArray() {
		List<String> list = new ArrayList<String>();
		list.add(THE);
		list.add(QUICK);
		list.add(BROWN);
		list.add(FOX);
        SparseArray<String> array = newStringSparseArray( newStringSparseArray(list) );
        verifySparseArray(array, 0, 4);

        assertEquals("toString()", "{0=The, 1=quick, 2=brown, 3=Fox}", array.toString());
    }
    
    @Test
	public void testPopulatedAscending() {
		SparseArray<String> array = newStringSparseArray();
        array.put(100, THE);
		array.put(200, QUICK);
		array.put(400, BROWN);
        array.put(600, FOX);
        verifySparseArray(array, 100, 601);

        assertEquals("toString()", "{100=The, 200=quick, 400=brown, 600=Fox}", array.toString());
    }

    @Test
	public void testPopulatedDescending(){
        SparseArray<String> array = newStringSparseArray();
        array.put(106, FOX);
        array.put(84, BROWN);
        array.put(62, QUICK);
        array.put(20, THE);
		verifySparseArray(array, 20, 107);

        assertEquals("toString()", "{20=The, 62=quick, 84=brown, 106=Fox}", array.toString());
    }

    @Test
	public void testPopulatedFromCenter() {
        SparseArray<String> array = newStringSparseArray();
        array.put(22, QUICK);
        array.put(44, BROWN);
        array.put(11, THE);
        array.put(66, FOX);
		verifySparseArray(array, 11, 67);

        assertEquals("toString()", "{11=The, 22=quick, 44=brown, 66=Fox}", array.toString());
    }

    @Test
	public void testPopulatedWithAdd() {	
        SparseArray<String> array = newStringSparseArray();
        array.asList().add(THE);
        array.asList().add(QUICK);
        array.asList().add(BROWN);
        array.asList().add(FOX);
        verifySparseArray(array, 0, 4);

        assertEquals("toString()", "{0=The, 1=quick, 2=brown, 3=Fox}", array.toString());
    }

    @Test
    public void testAddRemove() {	
        SparseArray<String> array = newStringSparseArray();
        array.asList().add(THE);
        array.asList().add(QUICK);
        array.asList().add(BROWN);
        array.asList().add(FOX);
        verifySparseArray(array, 0, 4);

        assertTrue("Contains '" + FOX + "'", array.containsValue(FOX));
        assertTrue("Contains '" + FOX_2 + "'", !array.containsValue(FOX_2));

        assertEquals("Value at position 2", BROWN, array.get(2));
        array.put(2, "red");
        assertEquals("Value at position 2", "red", array.get(2));
        array.put(2, BROWN);
        verifySparseArray(array, 0, 4);

        array.put(100, FOX_2);
        assertEquals("Remove at position 100", FOX_2, array.remove(100));
        verifySparseArray(array, 0, 4);

        array.put(50, FOX_2);
        assertTrue("Remove '" + FOX_2 + "'", array.values().remove(FOX_2));
        verifySparseArray(array, 0, 4);

        array.put(75, FOX_2);
        assertEquals("Remove IndexOf()", FOX_2, array.remove(array.asList().indexOf(FOX_2)));
        verifySparseArray(array, 0, 4);

        for(int i=1; i<10; i++) {
            array.asList().add(FOX_2);
        }
        while( array.values().remove(FOX_2)){}
        verifySparseArray(array, 0, 4);
    }

    @Test
    public void testGetSet() {
        SparseArray<String> array = newStringSparseArray();
        array.put(100, THE);
        array.put(200, QUICK);
        array.put(400, BROWN);
        array.put(600, FOX);
        verifySparseArray(array, 100, 601);

        assertNotNull("SortedMap[100]", array.get(100));
        assertNotNull("SortedMap[200]", array.get(200));
        assertNotNull("SortedMap[400]", array.get(400));
        assertNotNull("SortedMap[600]", array.get(600));
        assertNull("SortedMap[17]",    array.get(17));
        assertNull("SortedMap[99]",    array.get(99));
        assertNull("SortedMap[101]",   array.get(101));
        assertNull("SortedMap[199]",   array.get(199));
        assertNull("SortedMap[601]",   array.get(601));
        assertNull("SortedMap[77777]", array.get(77777));

        assertEquals("IndexOf(the)",   100, array.asList().indexOf(THE));
        assertEquals("IndexOf(quick)", 200, array.asList().indexOf(QUICK));
        assertEquals("IndexOf(brown)", 400, array.asList().indexOf(BROWN));
        assertEquals("IndexOf(fox)",   600, array.asList().indexOf(FOX));
        assertEquals("IndexOf(fox2)",  -1,  array.asList().indexOf(FOX_2));
    
        for(int i=150; i<153; i++) {
            array.put(i, FOX_2);
        }
        for(int i=250; i<253; i++) {
            array.put(i, FOX_2);
        }
        assertEquals("Count", 10, array.size());
        assertEquals("toString()",
                     "{100=The, 150=Fox2, 151=Fox2, 152=Fox2, 200=quick, 250=Fox2, 251=Fox2, 252=Fox2, 400=brown, 600=Fox}",
                     array.toString());

        while( array.values().remove(FOX_2)){}
        verifySparseArray(array, 100, 601);

        array.put(300, FOX_2);
        array.put(300, null);      //e.g. Remove(300)
        verifySparseArray(array, 100, 601);
        array.put(199, null);
        array.put(999, null);
        verifySparseArray(array, 100, 601);
    }

    @Test
    public void testSubArray() {
        SparseArray<String> orgArray = newStringSparseArray();
        orgArray.put(100, THE);
        orgArray.put(101, QUICK);
        orgArray.put(102, BROWN);
        orgArray.put(103, FOX);
        orgArray.put(110, "AAAA");
        orgArray.put(111, "BBBB");
        orgArray.put(112, "CCCC");
        orgArray.put(113, "DDDD");

		SparseArray<String> array    = newStringSparseArray(orgArray);
 
        SparseArray<String> subArray = array.subMap(102, 112);
        assertEquals("subArray.start()", 102, subArray.firstKey().intValue());
        assertEquals("subArray.end()",   112, subArray.lastKey().intValue() + 1);
        assertEquals("subArray.size()",    4, subArray.size());

        assertEquals("subArray.get(103)", FOX,  subArray.get(103));
        assertNull("subArray.get(105)", subArray.get(105));
        array.put(105, "NewString");
        assertEquals("subArray.get(105)", "NewString",  subArray.get(105));
        assertEquals("subArray.toString()",
                     "{102=brown, 103=Fox, 105=NewString, 110=AAAA, 111=BBBB}",
                     subArray.toString());

        // Test GetEnumerator
        StringBuffer sb = new StringBuffer();
        for (Iterator<String> iter = subArray.values().iterator(); iter.hasNext();) {
			Object element = iter.next();
			sb.append(element);
		}
        assertEquals("All Values of SubArray", "brownFoxNewStringAAAABBBB", sb.toString());
        
        // Test Clear()
        subArray.clear();
        assertEquals("array.toString()", "{100=The, 101=quick, 112=CCCC, 113=DDDD}", array.toString());
        assertEquals("array.size()", 4, array.size());
        assertEquals("subArray.toString()", "{}", subArray.toString());
        assertEquals("subArray.size()", 0, subArray.size());
        
	    // Test Add()/Set
	    array.asList().add("ZZZZ");
	    subArray.asList().add("ZZZZ");
	    subArray.asList().add("XXXX");
	    subArray.put(104, "YYYY");
	    assertTrue(subArray.values().remove("ZZZZ"));

        assertEquals("array.toString()", "{100=The, 101=quick, 103=XXXX, 104=YYYY, 112=CCCC, 113=DDDD, 114=ZZZZ}", array.toString());
        assertEquals("subArray.toString()", "{103=XXXX, 104=YYYY}", subArray.toString());

        assertEquals("subArray.indexOf('XXXX')", 1, subArray.asList().indexOf("XXXX"));
        assertEquals("subArray.indexOf('YYYY')", 2, subArray.asList().indexOf("YYYY"));
        assertEquals("subArray.indexOf('ZZZZ')",  -1, subArray.asList().indexOf("ZZZZ"));
        assertEquals("array.indexOf('XXXX')", 103, array.asList().indexOf("XXXX"));
        assertEquals("array.indexOf('YYYY')", 104, array.asList().indexOf("YYYY"));
        assertEquals("array.indexOf('ZZZZ')", 114, array.asList().indexOf("ZZZZ"));

        // Test asList()
        List<String> list = subArray.asList();
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
        SparseArray<String> cloneArray = newStringSparseArray(subArray);
        assertEquals("cloneArray.toString()", "{103=XXXX, 104=YYYY}", cloneArray.toString());
        cloneArray.clear();
        assertEquals("cloneArray.toString()", "{}", cloneArray.toString());
        assertEquals("subArray.toString()", "{103=XXXX, 104=YYYY}", subArray.toString());
        
        // Test SetAll()
        SparseArray<String> saveArray = newStringSparseArray(array);
        SparseArray<String> newArray = newStringSparseArray();
        newArray.put(102, "0000");
        newArray.put(103, "1111");
        newArray.put(104, "2222");
        newArray.put(105, "4444");
        subArray.clear();
        subArray.putAll(newArray);
        assertEquals("subArray.toString()", "{102=0000, 103=1111, 104=2222, 105=4444}", subArray.toString());
        array.clear();
        array.putAll(saveArray);
        assertEquals("array.toString()", "{100=The, 101=quick, 103=XXXX, 104=YYYY, 112=CCCC, 113=DDDD, 114=ZZZZ}", array.toString());
            
        // Test Contains()
        assertTrue(array.asList().contains("XXXX"));
        assertTrue(array.asList().contains("YYYY"));
        assertTrue(array.asList().contains("ZZZZ"));
        assertTrue(subArray.asList().contains("XXXX"));
        assertTrue(subArray.asList().contains("YYYY"));
        assertTrue(!subArray.asList().contains("ZZZZ"));

        assertEquals("subArray.remove(103)", "XXXX", subArray.remove(103));
        assertTrue("subArray.remove('YYYY')", subArray.values().remove("YYYY"));
        assertFalse("subArray.remove('ZZZZ')", subArray.values().remove("ZZZZ"));
        assertTrue("array.remove('ZZZZ')", array.values().remove("ZZZZ"));

        assertEquals("array.toString()", "{100=The, 101=quick, 112=CCCC, 113=DDDD}", array.toString());
        assertEquals("subArray.toString()", "{}", subArray.toString());
    }

    @Test
    public void testSubSubArray() {

        SparseArray<String> array = newStringSparseArray();
        array.put(100, THE);
        array.put(101, QUICK);
        array.put(102, BROWN);
        array.put(103, FOX);
        array.put(110, "AAAA");
        array.put(111, "BBBB");
        array.put(112, "CCCC");
        array.put(113, "DDDD");
        array.put(114, "EEEE");
        array.put(115, "FFFF");
        array.put(116, "GGGG");
        array.put(117, "HHHH");

        SparseArray<String> subArray = array.subMap(102, 117);
        SparseArray<String> subSubArray = subArray.subMap(112, 115);
        assertEquals("subArray.start()", 102, subArray.firstKey().intValue());
        assertEquals("subArray.end()",   117, subArray.lastKey().intValue() + 1);
        assertEquals("subSubArray.start()", 112, subSubArray.firstKey().intValue());
        assertEquals("subSubArray.end()",   115, subSubArray.lastKey().intValue() + 1);
        assertEquals("array.toString()",
                     "{100=The, 101=quick, 102=brown, 103=Fox, 110=AAAA, 111=BBBB, 112=CCCC, 113=DDDD, 114=EEEE, 115=FFFF, 116=GGGG, 117=HHHH}",
                     array.toString());
        assertEquals("SubArray.toString()",
                     "{102=brown, 103=Fox, 110=AAAA, 111=BBBB, 112=CCCC, 113=DDDD, 114=EEEE, 115=FFFF, 116=GGGG}",
                     subArray.toString());
        assertEquals("subSubArray.toString()",
                     "{112=CCCC, 113=DDDD, 114=EEEE}",
                     subSubArray.toString());

        subSubArray.clear();
        subSubArray.put(113, "Hallo");
        subSubArray.asList().add("Joe");
        assertEquals("array.toString()",
                     "{100=The, 101=quick, 102=brown, 103=Fox, 110=AAAA, 111=BBBB, 113=Hallo, 114=Joe, 115=FFFF, 116=GGGG, 117=HHHH}",
                     array.toString());
        assertEquals("SubArray.toString()",
                     "{102=brown, 103=Fox, 110=AAAA, 111=BBBB, 113=Hallo, 114=Joe, 115=FFFF, 116=GGGG}",
                     subArray.toString());
        assertEquals("subSubArray.toString()",
                     "{113=Hallo, 114=Joe}",
                     subSubArray.toString());
                     
        SparseArray<String> cloneSubSubArray = newStringSparseArray(subSubArray);

        assertEquals("subArray.indexOf('Hallo')", 11, subArray.asList().indexOf("Hallo"));
        assertEquals("subSubArray.indexOf('Hallo')", 1, subSubArray.asList().indexOf("Hallo"));
        subSubArray.values().remove("Hallo");
        assertEquals("array.toString()",
                     "{100=The, 101=quick, 102=brown, 103=Fox, 110=AAAA, 111=BBBB, 114=Joe, 115=FFFF, 116=GGGG, 117=HHHH}",
                     array.toString());
        assertEquals("SubArray.toString()",
                     "{102=brown, 103=Fox, 110=AAAA, 111=BBBB, 114=Joe, 115=FFFF, 116=GGGG}",
                     subArray.toString());
        assertEquals("subSubArray.toString()",
                     "{114=Joe}",
                     subSubArray.toString());
		assertEquals("cloneSubSubArray.toString()",
		             "{113=Hallo, 114=Joe}",
		             cloneSubSubArray.toString());
		
		int[] indices = new int[]{102,103,110,111,114,115,116};
		String[] values = new String[]{"brown","Fox","AAAA","BBBB","Joe","FFFF","GGGG"};
        int i = 0;
        for(String s : subArray){
            assertEquals("iterator", values[i++], s); 
        }
        i = 0;
		for(
		    ListIterator<String> l = subArray.populationIterator();
		    l.hasNext();
		    i++
		){
		    int j = l.nextIndex();
            assertEquals("populationIterator.index", indices[i], l.nextIndex()); 
            assertEquals("populationIterator.value", values[i], l.next()); 
            if(j == 111){
                l.set("BBB-");
                l.add("BBB+");
            } else if (j == 115) {
                l.remove();
            }
		}
		
		assertEquals("subArray.get", "Fox", subArray.get(103));
		
        indices = new int[]{102,103,110,111,112,114,116};
        values = new String[]{"brown","Fox","AAAA","BBB-","BBB+","Joe","GGGG"};
        i = 0;
        for(
            ListIterator<String> l = subArray.populationIterator();
            l.hasNext();
            i++
        ){
            int j = l.nextIndex();
            assertEquals("populationIterator.index", indices[i], l.nextIndex()); 
            assertEquals("populationIterator.value[" + j + "]", values[i], l.next()); 
        }
    }
    
    //------------------------------------------------------------------------
	// Verify method
	//------------------------------------------------------------------------

	public void verifySparseArray(SparseArray<String> array,
                                  int expectedStartValue,
                                  int expectedEndValue) {
        // Test GetEnumerator
        StringBuffer sb = new StringBuffer();
        for (Iterator<String> iter = array.values().iterator(); iter.hasNext();) {
			Object element = iter.next();
			sb.append(element);
		}
        assertEquals("All values of SortedMap", "ThequickbrownFox", sb.toString());

        assertEquals("SparseArray.size",   4,                   array.size());
        if(expectedStartValue >= 0) {
            assertEquals("SparseArray.start()", expectedStartValue, array.firstKey().intValue());
        }
        if(expectedEndValue > 0) {
            assertEquals("SparseArray.end()",   expectedEndValue,   array.lastKey().intValue() + 1);
        }
        assertEquals( "SortedMap.AsList().Count", expectedEndValue, array.asList().size() );
    }
	
}
