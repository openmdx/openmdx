/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestMergingList.java,v 1.2 2009/03/05 14:57:40 hburger Exp $
 * Description: class TestMergingList 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/05 14:57:40 $
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
package org.openmdx.application.dataprovider.accessor;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestMergingList 
    extends TestCase 
{

    /**
     * Constructs a test case with the given name.
     */
    public TestMergingList(String name) {
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
        return new TestSuite(TestMergingList.class);
    }

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected List<String> valuesX;
    /**
     * Add an instance variable for each part of the fixture 
     */
    protected List<String> valuesY;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected List<String> referenceList;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected List<String> mergingList;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() {
        this.valuesX = new ArrayList<String>(
            Arrays.asList(new String[]{"a","C","c1","z"})
        );
        this.valuesY = new ArrayList<String>(
            Arrays.asList(new String[]{"b","C","c2","Y"})
        ); 
        this.referenceList = new ArrayList<String>();
        this.referenceList.addAll(valuesX);
        this.referenceList.addAll(valuesY);
        Collections.sort(referenceList, Collator.getInstance());
//      System.out.println("referenceList = " + this.referenceList);
        this.mergingList = new PersistentContainer_1.MergingList(
            this.valuesX,
            this.valuesY,
            Collator.getInstance()
        );
//      System.out.println("mergingList = " + this.mergingList);
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testLists() {
        assertEquals(
            "Filtered list",
            this.referenceList,
            this.mergingList
        );
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testSize() {
        assertEquals(
            "List size",
            this.referenceList.size(),
            this.mergingList.size()
        );
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testJoJo() {
        ListIterator<String> referenceIterator = this.referenceList.listIterator();     
        ListIterator<String> mergingIterator = this.mergingList.listIterator();
        boolean available;      
        for(
            int i = 0;
            i < 2;
            i++
        ){
            for(
                int j = 0;
                j <= this.referenceList.size();
                j++
            ){
//              System.out.println("[" + i + ", " + j + ", ++]");
                assertEquals(
                    "hasNext() [" + i + ", " + j + ", ++]",
                    available = referenceIterator.hasNext(),
                    mergingIterator.hasNext()
                );
                assertEquals(
                    "hasPrevious() [" + i + ", " + j + ", ++]",
                    referenceIterator.hasPrevious(),
                    mergingIterator.hasPrevious()
                );
                assertEquals(
                    "previousIndex() [" + i + ", " + j + ", ++]",
                    referenceIterator.previousIndex(),
                    mergingIterator.previousIndex()
                );
                assertEquals(
                    "nextIndex() [" + i + ", " + j + ", ++]",
                    referenceIterator.nextIndex(),
                    mergingIterator.nextIndex()
                );
                if(available) assertEquals(
                    "next() [" + i + ", " + j + ", ++]",
                    referenceIterator.next(),
                    mergingIterator.next()
                );
            }
            for(
                int j = this.referenceList.size();
                j >= 0;
                j--
            ){
//              System.out.println("[" + i + ", " + j + ", --]");
                assertEquals(
                    "hasNext() [" + i + ", " + j + ", --]",
                    referenceIterator.hasNext(),
                    mergingIterator.hasNext()
                );
                assertEquals(
                    "hasPrevious() [" + i + ", " + j + ", --]",
                    available = referenceIterator.hasPrevious(),
                    mergingIterator.hasPrevious()
                );
                assertEquals(
                    "previousIndex() [" + i + ", " + j + ", --]",
                    referenceIterator.previousIndex(),
                    mergingIterator.previousIndex()
                );
                assertEquals(
                    "nextIndex() [" + i + ", " + j + ", --]",
                    referenceIterator.nextIndex(),
                    mergingIterator.nextIndex()
                );
                if(available) assertEquals(
                    "previous() [" + i + ", " + j + ", --]",
                    referenceIterator.previous(),
                    mergingIterator.previous()
                );
            }
        }
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testClear(
    ){
        this.mergingList.clear();
        assertTrue("Merging list empty", this.mergingList.isEmpty());
        assertTrue("First list empty", this.valuesX.isEmpty());
        assertTrue("Second list empty", this.valuesY.isEmpty());
    }


}
