/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CompareWithWebsphereOnSun.java,v 1.1 2005/11/01 18:30:23 hburger Exp $
 * Description: Test Transport
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/11/01 18:30:23 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.test.compatibility.base.dataprovider.cci;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.naming.Path;

/**
 * Test Transport
 */
public class CompareWithWebsphereOnSun extends TestCase {

    /**
     * Constructs a test case with the given name.
     */
    public CompareWithWebsphereOnSun(String name) {
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
        return new TestSuite(CompareWithWebsphereOnSun.class);
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     * @throws Exception 
     */
    public void testDataproviderObject(
    ) throws Exception {
        long total = 0;
        for(int k = 0; k < TESTS; k++) {
            long start = System.currentTimeMillis();
            for(int j = 0; j < UNITS_OF_WORK; j++) {
                Map manager = new HashMap();
                for(int i = 0; i < OBJECTS; i++) {
                    DataproviderObject o = new DataproviderObject(
                        PARENT.getChild(String.valueOf(i))
                    );
                    o.values("attribute0").set(0, "String0");
                    o.values("attribute1").set(0, "String1");
                    o.values("attribute2").set(0, "String2");
                    o.values("attribute3").set(0, "String3");
                    o.values("attribute4").set(0, "String4");
                    o.values("attribute5").set(0, "String5");
                    o.values("attribute6").set(0, "String6");
                    o.values("attribute7").set(0, "String7");
                    o.values("attribute8").set(0, "String8");
                    o.values("attribute9").set(0, "String9");
                    o.values("attribute10").set(0, new Integer(i));
                    o.values("attribute11").set(0, new Integer(i+1));
                    o.values("attribute12").set(0, new Integer(i+2));
                    o.values("attribute13").set(0, new Integer(i+3));
                    o.values("attribute14").set(0, new Integer(i+4));
                    o.values("attribute15").set(0, "20051012T122933.000Z");
                    o.values("attribute16").set(0, "20051013T122933.000Z");
                    o.values("attribute17").set(0, "20051014T122933.000Z");
                    o.values("attribute18").set(0, "20051015T122933.000Z");
                    o.values("attribute19").set(0, "20051016T122933.000Z");
                    manager.put(o.path(), o);
                }
            }
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("Test " + k + " (" + UNITS_OF_WORK + "*" + OBJECTS + " Dataprovider Objects): " + elapsed + " ms");
            total += elapsed;
        }
        System.out.println("DataproviderO bject average: " + (total / TESTS) + " ms");
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     * @throws Exception 
     */
    public void testTypedObject(
    ) throws Exception {
        DateFormat dateFormat = DateFormat.getInstance();
        long value15 = dateFormat.parse("20051012T122933.000Z").getTime();
        long value16 = dateFormat.parse("20051013T122933.000Z").getTime();
        Date value17 = dateFormat.parse("20051014T122933.000Z");
        Date value18 = dateFormat.parse("20051015T122933.000Z");
        Date value19 = dateFormat.parse("20051016T122933.000Z");
        Field attribute0 = TypedObject.class.getDeclaredField("attribute0");
        Field attribute1 = TypedObject.class.getDeclaredField("attribute1");
        Field attribute2 = TypedObject.class.getDeclaredField("attribute2");
        Field attribute3 = TypedObject.class.getDeclaredField("attribute3");
        Field attribute4 = TypedObject.class.getDeclaredField("attribute4");
        Field attribute5 = TypedObject.class.getDeclaredField("attribute5");
        Field attribute6 = TypedObject.class.getDeclaredField("attribute6");
        Field attribute7 = TypedObject.class.getDeclaredField("attribute7");
        Field attribute8 = TypedObject.class.getDeclaredField("attribute8");
        Field attribute9 = TypedObject.class.getDeclaredField("attribute9");
        Field attribute10 = TypedObject.class.getDeclaredField("attribute10");
        Field attribute11 = TypedObject.class.getDeclaredField("attribute11");
        Field attribute12 = TypedObject.class.getDeclaredField("attribute12");
        Field attribute13 = TypedObject.class.getDeclaredField("attribute13");
        Field attribute14 = TypedObject.class.getDeclaredField("attribute14");
        Field attribute15 = TypedObject.class.getDeclaredField("attribute15");
        Field attribute16 = TypedObject.class.getDeclaredField("attribute16");
        Field attribute17 = TypedObject.class.getDeclaredField("attribute17");
        Field attribute18 = TypedObject.class.getDeclaredField("attribute18");
        Field attribute19 = TypedObject.class.getDeclaredField("attribute19");
        long total = 0;
        for(int k = 0; k < TESTS; k++) {
            long start = System.currentTimeMillis();
            for(int j = 0; j < UNITS_OF_WORK; j++) {
                Map manager = new HashMap();
                for(int i = 0; i < OBJECTS; i++) {
                    TypedObject o = new TypedObject(
                        PARENT.getChild(String.valueOf(i))
                    );
                    attribute0.set(o, "String0");
                    attribute1.set(o, "String1");
                    attribute2.set(o, "String2");
                    attribute3.set(o, "String3");
                    attribute4.set(o, "String4");
                    attribute5.set(o, "String5");
                    attribute6.set(o, "String6");
                    attribute7.set(o, "String7");
                    attribute8.set(o, "String8");
                    attribute9.set(o, "String9");
                    attribute10.setInt(o, i);
                    attribute11.setInt(o, i+1);
                    attribute12.set(o, new Integer(i+2));
                    attribute13.set(o, new Integer(i+3));
                    attribute14.set(o, new Integer(i+4));
                    attribute15.setLong(o, value15);
                    attribute16.setLong(o, value16);
                    attribute17.set(o, value17);
                    attribute18.set(o, value18);
                    attribute19.set(o, value19);
                    manager.put(o.path, o);
                }
            }
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("Test " + k + " (" + UNITS_OF_WORK + "*" + OBJECTS + " Typed Objects): " + elapsed + " ms");
            total += elapsed;
        }
        System.out.println("Typed Object average: " + (total / TESTS) + " ms");
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     * @throws Exception 
     */
    public void testGenericObject(
    ) throws Exception {
        DateFormat dateFormat = DateFormat.getInstance();
        Date value15 = dateFormat.parse("20051012T122933.000Z");
        Date value16 = dateFormat.parse("20051013T122933.000Z");
        Date value17 = dateFormat.parse("20051014T122933.000Z");
        Date value18 = dateFormat.parse("20051015T122933.000Z");
        Date value19 = dateFormat.parse("20051016T122933.000Z");
        long total = 0;
        for(int k = 0; k < TESTS; k++) {
            long start = System.currentTimeMillis();
            for(int j = 0; j < UNITS_OF_WORK; j++) {
                Map manager = new HashMap();
                for(int i = 0; i < OBJECTS; i++) {
                    GenericObject o = new GenericObject(
                        PARENT.getChild(String.valueOf(i))
                    );
                    o.setValue("attribute0", "String0");
                    o.setValue("attribute1", "String1");
                    o.setValue("attribute2", "String2");
                    o.setValue("attribute3", "String3");
                    o.setValue("attribute4", "String4");
                    o.setValue("attribute5", "String5");
                    o.setValue("attribute6", "String6");
                    o.setValue("attribute7", "String7");
                    o.setValue("attribute8", "String8");
                    o.setValue("attribute9", "String9");
                    o.setValue("attribute10", new Integer(i));
                    o.setValue("attribute11", new Integer(i+1));
                    o.setValue("attribute12", new Integer(i+2));
                    o.setValue("attribute13", new Integer(i+3));
                    o.setValue("attribute14", new Integer(i+4));
                    o.setValue("attribute15", value15);
                    o.setValue("attribute16", value16);
                    o.setValue("attribute17", value17);
                    o.setValue("attribute18", value18);
                    o.setValue("attribute19", value19);
                    manager.put(o.path(), o);
                }
            }
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("Test " + k + " (" + UNITS_OF_WORK + "*" + OBJECTS + " Generic Objects): " + elapsed + " ms");
            total += elapsed;
        }
        System.out.println("Generic Object average: " + (total / TESTS) + " ms");
    }

    protected static int TESTS = 10;
    protected static int UNITS_OF_WORK = 100;
    protected static int OBJECTS = 1000;
    Path PARENT = new Path("ch::css::vertrag1::base/provider/VTR/segment/TEST_MOROFF/police/1000000KV/vertrag/VERTRAG_OKP1/versichertesObjekt");
    
    
}
