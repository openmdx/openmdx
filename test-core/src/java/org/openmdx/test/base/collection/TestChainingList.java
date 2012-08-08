/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestChainingList.java,v 1.6 2008/11/04 10:19:09 hburger Exp $
 * Description: class TestChainingList 
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/04 10:19:09 $
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.compatibility.base.dataprovider.transport.delegation.ChainingList;

public class TestChainingList extends TestCase {

    /**
     * Constructs a test case with the given name.
     */
    public TestChainingList(String name) {
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
        return new TestSuite(TestChainingList.class);
    }

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected List[] pieces;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected List referenceList;

    /**
     * Add an instance variable for each part of the fixture 
     */
    protected List chainedList;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() {
        this.pieces = new List[]{
            Collections.EMPTY_LIST,
            new ArrayList(Arrays.asList(new String[]{"A",null,"C"})),
            Collections.EMPTY_LIST,
            new ArrayList(Arrays.asList(new String[]{"D"})),
        };
        this.referenceList = new ArrayList();
        for(
            int i = 0;
            i < this.pieces.length;
            i++
        ) this.referenceList.addAll(this.pieces[i]);
        this.chainedList = new ChainingList(this.pieces);
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testLists() {
        assertEquals(
            "Concatenated list",
            this.referenceList,
            this.chainedList
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
            this.chainedList.size()
        );
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testJoJo() {
        ListIterator referenceIterator = this.referenceList.listIterator();     
        ListIterator chainedIterator = this.chainedList.listIterator();
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
                    chainedIterator.hasNext()
                );
                assertEquals(
                    "hasPrevious() [" + i + ", " + j + ", ++]",
                    referenceIterator.hasPrevious(),
                    chainedIterator.hasPrevious()
                );
                assertEquals(
                    "previousIndex() [" + i + ", " + j + ", ++]",
                    referenceIterator.previousIndex(),
                    chainedIterator.previousIndex()
                );
                assertEquals(
                    "nextIndex() [" + i + ", " + j + ", ++]",
                    referenceIterator.nextIndex(),
                    chainedIterator.nextIndex()
                );
                if(available) assertEquals(
                    "next() [" + i + ", " + j + ", ++]",
                    referenceIterator.next(),
                    chainedIterator.next()
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
                    chainedIterator.hasNext()
                );
                assertEquals(
                    "hasPrevious() [" + i + ", " + j + ", --]",
                    available = referenceIterator.hasPrevious(),
                    chainedIterator.hasPrevious()
                );
                assertEquals(
                    "previousIndex() [" + i + ", " + j + ", --]",
                    referenceIterator.previousIndex(),
                    chainedIterator.previousIndex()
                );
                assertEquals(
                    "nextIndex() [" + i + ", " + j + ", --]",
                    referenceIterator.nextIndex(),
                    chainedIterator.nextIndex()
                );
                if(available) assertEquals(
                    "previous() [" + i + ", " + j + ", --]",
                    referenceIterator.previous(),
                    chainedIterator.previous()
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
        this.chainedList.clear();
        assertTrue("Merging list empty", this.chainedList.isEmpty());
        for(
            int i = 0;
            i < this.pieces.length;
            i++
        ) assertTrue("List " + i + " empty", this.pieces[i].isEmpty());
    }

}
