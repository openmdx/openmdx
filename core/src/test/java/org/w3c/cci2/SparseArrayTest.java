/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: SparseArray Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2021, OMEX AG, Switzerland
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
package org.w3c.cci2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.cci2.SortedMaps;
import org.w3c.cci2.SparseArray;

/**
 * SparseArray Test
 */
public class SparseArrayTest {
	
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
            Assertions.fail("Start()");
        } catch (NoSuchElementException expected) {
        }
        try {
            array.lastKey();
            Assertions.fail("End()");
        } catch (NoSuchElementException expected) {
        }
        Assertions.assertEquals(0, array.size(), "Count");
        Assertions.assertEquals("{}", array.toString(), "ToString()");

		List<String> list = new ArrayList<String>();
		list.add(THE);
		list.add(QUICK);
		list.add(BROWN);
		list.add(FOX);
        array  = newStringSparseArray(list);
        array.clear();
        try {
            array.firstKey();
            Assertions.fail("Start()");
        } catch (NoSuchElementException expected) {
        }
        try {
            array.lastKey();
            Assertions.fail("End()");
        } catch (NoSuchElementException expected) {
        }
        Assertions.assertEquals(0, array.size(), "Count");
        Assertions.assertEquals("{}", array.toString(), "ToString()");
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

        Assertions.assertEquals("{0=The, 1=quick, 2=brown, 3=Fox}", array.toString(), "toString()");
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

        Assertions.assertEquals("{0=The, 1=quick, 2=brown, 3=Fox}", array.toString(), "toString()");
    }
    
    @Test
	public void testPopulatedAscending() {
		SparseArray<String> array = newStringSparseArray();
        array.put(100, THE);
		array.put(200, QUICK);
		array.put(400, BROWN);
        array.put(600, FOX);
        verifySparseArray(array, 100, 601);

        Assertions.assertEquals("{100=The, 200=quick, 400=brown, 600=Fox}", array.toString(), "toString()");
    }

    @Test
	public void testPopulatedDescending(){
        SparseArray<String> array = newStringSparseArray();
        array.put(106, FOX);
        array.put(84, BROWN);
        array.put(62, QUICK);
        array.put(20, THE);
		verifySparseArray(array, 20, 107);

        Assertions.assertEquals("{20=The, 62=quick, 84=brown, 106=Fox}", array.toString(), "toString()");
    }

    @Test
	public void testPopulatedFromCenter() {
        SparseArray<String> array = newStringSparseArray();
        array.put(22, QUICK);
        array.put(44, BROWN);
        array.put(11, THE);
        array.put(66, FOX);
		verifySparseArray(array, 11, 67);

        Assertions.assertEquals("{11=The, 22=quick, 44=brown, 66=Fox}", array.toString(), "toString()");
    }

    @Test
	public void testPopulatedWithAdd() {	
        SparseArray<String> array = newStringSparseArray();
        array.asList().add(THE);
        array.asList().add(QUICK);
        array.asList().add(BROWN);
        array.asList().add(FOX);
        verifySparseArray(array, 0, 4);

        Assertions.assertEquals("{0=The, 1=quick, 2=brown, 3=Fox}", array.toString(), "toString()");
    }

    @Test
    public void testAddRemove() {	
        SparseArray<String> array = newStringSparseArray();
        array.asList().add(THE);
        array.asList().add(QUICK);
        array.asList().add(BROWN);
        array.asList().add(FOX);
        verifySparseArray(array, 0, 4);

        Assertions.assertTrue(array.containsValue(FOX), "Contains '" + FOX + "'");
        Assertions.assertTrue(!array.containsValue(FOX_2), "Contains '" + FOX_2 + "'");

        Assertions.assertEquals(BROWN, array.get(2), "Value at position 2");
        array.put(2, "red");
        Assertions.assertEquals("red", array.get(2), "Value at position 2");
        array.put(2, BROWN);
        verifySparseArray(array, 0, 4);

        array.put(100, FOX_2);
        Assertions.assertEquals(FOX_2, array.remove(100), "Remove at position 100");
        verifySparseArray(array, 0, 4);

        array.put(50, FOX_2);
        Assertions.assertTrue(array.values().remove(FOX_2), "Remove '" + FOX_2 + "'");
        verifySparseArray(array, 0, 4);

        array.put(75, FOX_2);
        Assertions.assertEquals(FOX_2, array.remove(array.asList().indexOf(FOX_2)), "Remove IndexOf()");
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

        Assertions.assertNotNull(array.get(100), "SortedMap[100]");
        Assertions.assertNotNull(array.get(200), "SortedMap[200]");
        Assertions.assertNotNull(array.get(400), "SortedMap[400]");
        Assertions.assertNotNull(array.get(600), "SortedMap[600]");
        Assertions.assertNull(array.get(17), "SortedMap[17]");
        Assertions.assertNull(array.get(99), "SortedMap[99]");
        Assertions.assertNull(array.get(101), "SortedMap[101]");
        Assertions.assertNull(array.get(199), "SortedMap[199]");
        Assertions.assertNull(array.get(601), "SortedMap[601]");
        Assertions.assertNull(array.get(77777), "SortedMap[77777]");

        Assertions.assertEquals(100, array.asList().indexOf(THE), "IndexOf(the)");
        Assertions.assertEquals(200, array.asList().indexOf(QUICK), "IndexOf(quick)");
        Assertions.assertEquals(400, array.asList().indexOf(BROWN), "IndexOf(brown)");
        Assertions.assertEquals(600, array.asList().indexOf(FOX), "IndexOf(fox)");
        Assertions.assertEquals(-1, array.asList().indexOf(FOX_2), "IndexOf(fox2)");
    
        for(int i=150; i<153; i++) {
            array.put(i, FOX_2);
        }
        for(int i=250; i<253; i++) {
            array.put(i, FOX_2);
        }
        Assertions.assertEquals(10, array.size(), "Count");
        Assertions.assertEquals("{100=The, 150=Fox2, 151=Fox2, 152=Fox2, 200=quick, 250=Fox2, 251=Fox2, 252=Fox2, 400=brown, 600=Fox}", array.toString(), "toString()");

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
        Assertions.assertEquals(102, subArray.firstKey().intValue(), "subArray.start()");
        Assertions.assertEquals(112, subArray.lastKey().intValue() + 1, "subArray.end()");
        Assertions.assertEquals(4, subArray.size(), "subArray.size()");

        Assertions.assertEquals(FOX, subArray.get(103), "subArray.get(103)");
        Assertions.assertNull(subArray.get(105), "subArray.get(105)");
        array.put(105, "NewString");
        Assertions.assertEquals("NewString", subArray.get(105), "subArray.get(105)");
        Assertions.assertEquals("{102=brown, 103=Fox, 105=NewString, 110=AAAA, 111=BBBB}", subArray.toString(), "subArray.toString()");

        // Test GetEnumerator
        StringBuffer sb = new StringBuffer();
        for (Iterator<String> iter = subArray.values().iterator(); iter.hasNext();) {
			Object element = iter.next();
			sb.append(element);
		}
        Assertions.assertEquals("brownFoxNewStringAAAABBBB", sb.toString(), "All Values of SubArray");
        
        // Test Clear()
        subArray.clear();
        Assertions.assertEquals("{100=The, 101=quick, 112=CCCC, 113=DDDD}", array.toString(), "array.toString()");
        Assertions.assertEquals(4, array.size(), "array.size()");
        Assertions.assertEquals("{}", subArray.toString(), "subArray.toString()");
        Assertions.assertEquals(0, subArray.size(), "subArray.size()");
        
	    // Test Add()/Set
	    array.asList().add("ZZZZ");
	    try {
            subArray.asList().add("ZZZZ");
            Assertions.fail("IndexOutOfBoundsException expected");
	    } catch (IndexOutOfBoundsException expected) {
	        subArray.asList().set(102, "ZZZZ");
	    }
        subArray.asList().add("XXXX");
	    
	    subArray.put(104, "YYYY");
	    Assertions.assertTrue(subArray.values().remove("ZZZZ"));

        Assertions.assertEquals("{100=The, 101=quick, 103=XXXX, 104=YYYY, 112=CCCC, 113=DDDD, 114=ZZZZ}", array.toString(), "array.toString()");
        Assertions.assertEquals("{103=XXXX, 104=YYYY}", subArray.toString(), "subArray.toString()");

        Assertions.assertEquals(103, subArray.asList().indexOf("XXXX"), "subArray.indexOf('XXXX')");
        Assertions.assertEquals(104, subArray.asList().indexOf("YYYY"), "subArray.indexOf('YYYY')");
        Assertions.assertEquals(-1, subArray.asList().indexOf("ZZZZ"), "subArray.indexOf('ZZZZ')");
        Assertions.assertEquals(103, array.asList().indexOf("XXXX"), "array.indexOf('XXXX')");
        Assertions.assertEquals(104, array.asList().indexOf("YYYY"), "array.indexOf('YYYY')");
        Assertions.assertEquals(114, array.asList().indexOf("ZZZZ"), "array.indexOf('ZZZZ')");

        // Test asList()
        List<String> list = subArray.asList();
        Assertions.assertEquals(105, list.size(), "subArray.asList().size() " + subArray);
        for(int i=0; i<list.size(); i++) {
            if ( i == 103 || i == 104 ) {
                Assertions.assertNotNull(list.get(i), "subArray.asList().get(" + i + ")");
            }
            else {
                Assertions.assertNull(list.get(i), "subArray.asList().get(" + i + ")");
            }
        }

        // Test Clone()
        SparseArray<String> cloneArray = newStringSparseArray(subArray);
        Assertions.assertEquals("{103=XXXX, 104=YYYY}", cloneArray.toString(), "cloneArray.toString()");
        cloneArray.clear();
        Assertions.assertEquals("{}", cloneArray.toString(), "cloneArray.toString()");
        Assertions.assertEquals("{103=XXXX, 104=YYYY}", subArray.toString(), "subArray.toString()");
        
        // Test SetAll()
        SparseArray<String> saveArray = newStringSparseArray(array);
        SparseArray<String> newArray = newStringSparseArray();
        newArray.put(102, "0000");
        newArray.put(103, "1111");
        newArray.put(104, "2222");
        newArray.put(105, "4444");
        subArray.clear();
        subArray.putAll(newArray);
        Assertions.assertEquals("{102=0000, 103=1111, 104=2222, 105=4444}", subArray.toString(), "subArray.toString()");
        array.clear();
        array.putAll(saveArray);
        Assertions.assertEquals("{100=The, 101=quick, 103=XXXX, 104=YYYY, 112=CCCC, 113=DDDD, 114=ZZZZ}", array.toString(), "array.toString()");
            
        // Test Contains()
        Assertions.assertTrue(array.asList().contains("XXXX"));
        Assertions.assertTrue(array.asList().contains("YYYY"));
        Assertions.assertTrue(array.asList().contains("ZZZZ"));
        Assertions.assertTrue(subArray.asList().contains("XXXX"));
        Assertions.assertTrue(subArray.asList().contains("YYYY"));
        Assertions.assertTrue(!subArray.asList().contains("ZZZZ"));

        Assertions.assertEquals("XXXX", subArray.remove(103), "subArray.remove(103)");
        Assertions.assertTrue(subArray.values().remove("YYYY"), "subArray.remove('YYYY')");
        Assertions.assertFalse(subArray.values().remove("ZZZZ"), "subArray.remove('ZZZZ')");
        Assertions.assertTrue(array.values().remove("ZZZZ"), "array.remove('ZZZZ')");

        Assertions.assertEquals("{100=The, 101=quick, 112=CCCC, 113=DDDD}", array.toString(), "array.toString()");
        Assertions.assertEquals("{}", subArray.toString(), "subArray.toString()");
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
        Assertions.assertEquals(102, subArray.firstKey().intValue(), "subArray.start()");
        Assertions.assertEquals(117, subArray.lastKey().intValue() + 1, "subArray.end()");
        Assertions.assertEquals(112, subSubArray.firstKey().intValue(), "subSubArray.start()");
        Assertions.assertEquals(115, subSubArray.lastKey().intValue() + 1, "subSubArray.end()");
        Assertions.assertEquals("{100=The, 101=quick, 102=brown, 103=Fox, 110=AAAA, 111=BBBB, 112=CCCC, 113=DDDD, 114=EEEE, 115=FFFF, 116=GGGG, 117=HHHH}", array.toString(), "array.toString()");
        Assertions.assertEquals("{102=brown, 103=Fox, 110=AAAA, 111=BBBB, 112=CCCC, 113=DDDD, 114=EEEE, 115=FFFF, 116=GGGG}", subArray.toString(), "SubArray.toString()");
        Assertions.assertEquals("{112=CCCC, 113=DDDD, 114=EEEE}", subSubArray.toString(), "subSubArray.toString()");

        subSubArray.clear();
        subSubArray.put(113, "Hallo");
        subSubArray.asList().add("Joe");
        Assertions.assertEquals("{100=The, 101=quick, 102=brown, 103=Fox, 110=AAAA, 111=BBBB, 113=Hallo, 114=Joe, 115=FFFF, 116=GGGG, 117=HHHH}", array.toString(), "array.toString()");
        Assertions.assertEquals("{102=brown, 103=Fox, 110=AAAA, 111=BBBB, 113=Hallo, 114=Joe, 115=FFFF, 116=GGGG}", subArray.toString(), "SubArray.toString()");
        Assertions.assertEquals("{113=Hallo, 114=Joe}", subSubArray.toString(), "subSubArray.toString()");
                     
        SparseArray<String> cloneSubSubArray = newStringSparseArray(subSubArray);

        Assertions.assertEquals(113, subArray.asList().indexOf("Hallo"), "subArray.indexOf('Hallo')");
        Assertions.assertEquals(113, subSubArray.asList().indexOf("Hallo"), "subSubArray.indexOf('Hallo')");
        subSubArray.values().remove("Hallo");
        Assertions.assertEquals("{100=The, 101=quick, 102=brown, 103=Fox, 110=AAAA, 111=BBBB, 114=Joe, 115=FFFF, 116=GGGG, 117=HHHH}", array.toString(), "array.toString()");
        Assertions.assertEquals("{102=brown, 103=Fox, 110=AAAA, 111=BBBB, 114=Joe, 115=FFFF, 116=GGGG}", subArray.toString(), "SubArray.toString()");
        Assertions.assertEquals("{114=Joe}", subSubArray.toString(), "subSubArray.toString()");
		Assertions.assertEquals("{113=Hallo, 114=Joe}", cloneSubSubArray.toString(), "cloneSubSubArray.toString()");
		
		int[] indices = new int[]{102,103,110,111,114,115,116};
		String[] values = new String[]{"brown","Fox","AAAA","BBBB","Joe","FFFF","GGGG"};
        int i = 0;
        for(String s : subArray){
            Assertions.assertEquals(values[i++], s, "iterator"); 
        }
        i = 0;
		for(
		    ListIterator<String> l = subArray.populationIterator();
		    l.hasNext();
		    i++
		){
		    int j = l.nextIndex();
            Assertions.assertEquals(indices[i], l.nextIndex(), "populationIterator.index"); 
            Assertions.assertEquals(values[i], l.next(), "populationIterator.value"); 
            if(j == 111){
                l.set("BBB-");
                l.add("BBB+");
            } else if (j == 115) {
                l.remove();
            }
		}
		
		Assertions.assertEquals("Fox", subArray.get(103), "subArray.get");
		
        indices = new int[]{102,103,110,111,112,114,116};
        values = new String[]{"brown","Fox","AAAA","BBB-","BBB+","Joe","GGGG"};
        i = 0;
        for(
            ListIterator<String> l = subArray.populationIterator();
            l.hasNext();
            i++
        ){
            int j = l.nextIndex();
            Assertions.assertEquals(indices[i], l.nextIndex(), "populationIterator.index"); 
            Assertions.assertEquals(values[i], l.next(), "populationIterator.value[" + j + "]"); 
        }
    }
    
    //------------------------------------------------------------------------
	// Verify method
	//------------------------------------------------------------------------

    public void verifySparseArray(SparseArray<String> array,
        int expectedStartValue,
        int expectedListSize) {
        verifySparseArray(array, expectedStartValue, expectedListSize - 1, expectedListSize);
    }
    
	public void verifySparseArray(SparseArray<String> array,
                                  int expectedStartValue,
                                  int expectedEndValue,
                                  int expectedListSize) {
        // Test GetEnumerator
        StringBuffer sb = new StringBuffer();
        for (Iterator<String> iter = array.values().iterator(); iter.hasNext();) {
			Object element = iter.next();
			sb.append(element);
		}
        Assertions.assertEquals("ThequickbrownFox", sb.toString(), "All values of SortedMap");

        Assertions.assertEquals(4, array.size(), "SparseArray.size");
        if(expectedStartValue >= 0) {
            Assertions.assertEquals(expectedStartValue, array.firstKey().intValue(), "SparseArray.start()");
        }
        if(expectedEndValue > 0) {
            Assertions.assertEquals(expectedEndValue, array.lastKey().intValue(), "SparseArray.end()");
        }
        Assertions.assertEquals(expectedListSize, array.asList().size(), "SortedMap.AsList().Count " + array);
    }
	
}
