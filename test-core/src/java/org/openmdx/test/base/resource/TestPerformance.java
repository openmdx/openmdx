/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestPerformance.java,v 1.6 2006/08/07 09:43:01 hburger Exp $
 * Description: class TestPerformance
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/08/07 09:43:01 $
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
package org.openmdx.test.base.resource;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestPerformance 
    extends TestCase
{

    /**
     * Constructs a test case with the given name.
     */
    public TestPerformance(String name) {
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
        return new TestSuite(TestPerformance.class);
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void test250000timesReusing25bytes(
    ){
        performTest(RUNS, 250000, false, 25);
    }
            
    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void test250000timesReusing250bytes(
    ){
        performTest(RUNS, 250000, false, 250);
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void test2500000timesReusing250000bytes(
    ){
        performTest(RUNS, 250000, false, 250000);
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void test100000timesReallocating25bytes(
    ){
        performTest(RUNS, 100000, true, 25);
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void test100000timesReallocating250bytes(
    ){
        performTest(RUNS, 100000, true, 250);
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void test10000timesReallocating2500bytes(
    ){
        performTest(RUNS, 10000, true, 2500);
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void test1000timesReallocating25000bytes(
    ){
        performTest(RUNS, 1000, true, 25000);
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void test10timesReallocating2500000bytes(
    ){
        performTest(RUNS, 10, true, 2500000);
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void test10timesReallocating250000bytes(
    ){
        performTest(RUNS, 100, true, 250000);
    }

    /**
     * 
     * @param runs
     * @param iterations
     * @param reallocate
     * @param size
     */
    protected static void performTest(
        int runs,
        int iterations,
        boolean reallocate,
        int size
    ){  

        long[] duration = new long[VARIANTS];
        Arrays.fill(duration, 0L);
                
        System.out.println();       
        System.out.println(runs + " runs with " + iterations + " iterations " + (
            reallocate ? "reallocating" : "reusing"
        ) + " int[" + size + "]");    
                
        // Pack an unpack source
        try {

            int[] source = new int[size];
            Arrays.fill(source, 1);
                        
            for(
                int run = 0;
                run < runs * VARIANTS;
                run++
            ){
                PerformanceTest performanceTest = new PerformanceTest(run);
                long begin = System.currentTimeMillis();
                for (
                    int i = 0;
                    i < iterations;
                    i++
                ){
                    if(reallocate) {
                        source = new int[size];
                        Arrays.fill(source, 1);
                    }
                    int[] target = performanceTest.unmarshal(
                        performanceTest.marshal(source)
                    );
                    if(target[0] == -1) System.out.println("strange");
                }
                duration[
                    performanceTest.flag
                ]+= System.currentTimeMillis() - begin;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        System.out.println("With JCA Record:        " + duration[0] + " ms");
        System.out.println("Without JCA Record:     " + duration[1] + " ms"); 
        System.out.println("  JCA Record Overhead:  " + (
            (duration[0]-duration[1]) * 1000000 / (runs * iterations) 
        ) + " ns"); 
        System.out.println("With PerformanceRecord: " + duration[2] + " ms"); 
        System.out.println("  JCA Record Overhead:  " + (
            (duration[0]-duration[2]) * 1000000 / (runs * iterations) 
        ) + " ns"); 
    }

    /**
     * 
     */
    final static private int VARIANTS = 3;
    
    /*
     * 
     */
    final static private int RUNS = 4;

}
