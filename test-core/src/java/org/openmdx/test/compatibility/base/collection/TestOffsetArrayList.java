/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestOffsetArrayList.java,v 1.4 2004/04/02 16:59:05 wfro Exp $
 * Description: class TestOffsetArrayList 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/04/02 16:59:05 $
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
package org.openmdx.test.compatibility.base.collection;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.compatibility.base.collection.OffsetArrayList;
import org.openmdx.compatibility.base.collection.SparseList;

public class TestOffsetArrayList extends TestCase {

    /**
     * Constructs a test case with the given name.
     */
    public TestOffsetArrayList(String name) {
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
        return new TestSuite(TestOffsetArrayList.class);
    }

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected OffsetArrayList populatedBySparseList;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected OffsetArrayList populatedByCollection;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected OffsetArrayList populatedAscending;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected OffsetArrayList populatedDescending;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected OffsetArrayList populatedFromCenter;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected OffsetArrayList addAllTarget;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected List addAllSource;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() {
        populatedByCollection = new OffsetArrayList (
            Arrays.asList(
                new Object[]{null,"A",null,"B",null,"C",null}
            )
        );
        populatedBySparseList = new OffsetArrayList (
            populatedByCollection
        );
        populatedAscending = new OffsetArrayList();
        populatedDescending = new OffsetArrayList();
        populatedFromCenter = new OffsetArrayList();
        addAllTarget = new OffsetArrayList (
            Arrays.asList(
                new Object[]{null,"A","C",null}
            )
        ); 
        addAllSource = Arrays.asList(
            new Object[]{null,"B",null}
        ); 
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testPopulatedAscending() {
        populatedAscending.set(1, "A");
        populatedAscending.set(3, "B");
        populatedAscending.set(5, "C");
        verifySparseList(populatedAscending);
        verifyPopulation(populatedAscending.population());
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testPopulatedDescending() {
        populatedDescending.add(3, "C");
        populatedDescending.add(2, "B");
        populatedDescending.add(1, "A");
        verifySparseList(populatedDescending);
        verifyPopulation(populatedDescending.population());
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testPopulatedByCollection() {
        verifySparseList(populatedBySparseList);
        verifyPopulation(populatedBySparseList.population());
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testPopulatedBySparseList() {
        verifySparseList(populatedBySparseList);
        verifyPopulation(populatedBySparseList.population());
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testAddAll() {
        addAllTarget.addAll(2, addAllSource);
        verifySparseList(addAllTarget);
        verifyPopulation(addAllTarget.population());
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testPopulatedFromCenter() {
        populatedFromCenter.set(2, "B");
        populatedFromCenter.add(4, "C");
        populatedFromCenter.add(1, "A");
        verifySparseList(populatedFromCenter);
        verifyPopulation(populatedFromCenter.population());
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testEquality(
    ) {
        assertEquals(
            "populatedFromCollection v/s populatedBySparseList",
            this.populatedByCollection,
            this.populatedBySparseList
        );
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void verifySparseList(
        SparseList list
    ) {
        String detail = "{offset=" + list.firstIndex() + 
            ", values=" + list.subList(list.firstIndex(), list.size()) + '}';
        assertEquals(
            detail + ".firstIndex()",
            1, list.firstIndex()
        );
        assertEquals(
            detail + ".lastIndex()",
            5, list.lastIndex()
        );
        assertEquals(
            detail + ".size()",
            6, list.size()
        );
        assertEquals(
            detail + ".get(0)",
            null, list.get(0)
        );
        assertEquals(
            detail + ".get(1)",
            "A", list.get(1)
        );
        assertEquals(
            detail + ".get(2)",
            null, list.get(2)
        );
        assertEquals(
            detail + ".get(3)",
            "B", list.get(3)
        );
        assertEquals(
            detail + ".get(4)",
            null, list.get(4)
        );
        assertEquals(
            detail + ".get(5)",
            "C", list.get(5)
        );
        assertEquals(
            detail + ".get(6)",
            null, list.get(6)
        );
        ListIterator iterator = list.populationIterator();
        detail += ": populationIterator";
        assertTrue(
            detail + ".hasNext():0",
            iterator.hasNext()
        );
        assertEquals(
            detail + ".nextIndex():0",
            1, iterator.nextIndex()
        );
        assertEquals(
            detail + ".next():0",
            "A", iterator.next()
        );
        assertTrue(
            detail + ".hasNext():1",
            iterator.hasNext()
        );
        assertEquals(
            detail + ".nextIndex():1",
            3, iterator.nextIndex()
        );
        assertEquals(
            detail + ".next():1",
            "B", iterator.next()
        );
        assertTrue(
            detail + ".hasNext():2",
            iterator.hasNext()
        );
        assertEquals(
            detail + ".nextIndex():2",
            5, iterator.nextIndex()
        );
        assertEquals(
            detail + ".next():2",
            "C", iterator.next()
        );
        assertTrue(
            detail + ".hasNext():3",
            ! iterator.hasNext()
        );
        assertEquals(
            detail + ".toString()",
            "[1:A, 3:B, 5:C]", list.toString()
        );
            
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void verifyPopulation(
        List list
    ) {
        String detail = list.toString();
        assertEquals(
            detail + ".size()",
            3, list.size()
        );
        assertEquals(
            detail + ".get(0)", 
            "A", list.get(0)
        );
        assertEquals(
            detail + ".get(1)",
            "B", list.get(1)
        );
        assertEquals(
            detail + ".get(2)",
            "C", list.get(2)
        );
        ListIterator iterator = list.listIterator();
        detail = "iterator";
        assertTrue(
            detail + ".hasNext():0",
            iterator.hasNext()
        );
        assertEquals(
            detail + ".nextIndex():0",
            0, iterator.nextIndex()
        );
        assertEquals(
            detail + ".next():0",
            "A", iterator.next()
        );
        assertTrue(
            detail + ".hasNext():1",
            iterator.hasNext()
        );
        assertEquals(
            detail + ".nextIndex():1",
            1, iterator.nextIndex()
        );
        assertEquals(
            detail + ".next():1",
            "B", iterator.next()
        );
        assertTrue(
            detail + ".hasNext():2",
            iterator.hasNext()
        );
        assertEquals(
            detail + ".nextIndex():2",
            2, iterator.nextIndex()
        );
        assertEquals(
            detail + ".next():2",
            "C", iterator.next()
        );
        assertTrue(
            detail + ".hasNext():3",
            ! iterator.hasNext()
        );
    }

    
}
