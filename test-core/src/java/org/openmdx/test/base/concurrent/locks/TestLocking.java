/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestLocking.java,v 1.10 2005/05/20 15:52:11 hburger Exp $
 * Description: BoundedBuffer
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/05/20 15:52:11 $
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.test.base.concurrent.locks;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test Locking
 */
public class TestLocking extends TestCase {

    public TestLocking() {
        super();
    }

    public TestLocking(String name) {
        super(name);
    }

    protected Thread producer;
    protected Thread consumer;
    protected BoundedBuffer buffer;
    
    private static final int COUNT = 1000;
    protected static boolean debug = false;    
    
    /**
     * The batch TestRunner can be given a class to run directly.
     * To start the batch runner from your main you can write: 
     */
    public static void main (String[] args) {
        if(args != null) for (
            int i = 0;
            i < args.length;
            i++
        ) if ("--debug=true".equals(args[i])) {
            TestLocking.debug = true;
        } else if ("--debug=false".equals(args[i])) {
            TestLocking.debug = false;
        }
        if(TestLocking.debug) System.out.println(
            "$Id: TestLocking.java,v 1.10 2005/05/20 15:52:11 hburger Exp $"
        );
        junit.textui.TestRunner.run (suite());
    }
    
    /**
     * A test runner either expects a static method suite as the
     * entry point to get a test to run or it will extract the 
     * suite automatically. 
     */
    public static Test suite() {
        return new TestSuite(TestLocking.class);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.buffer = new BoundedBuffer(
            COUNT / 10, 
            TestLocking.debug
        );
        this.producer = new Producer();
        this.consumer = new Consumer();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        if(
            this.producer.isAlive() &&
            !this.producer.isInterrupted()
        ) this.producer.interrupt();
        super.tearDown();
    }

    /**
     * Log a message at debug level
     * @param message
     */
    static void log(
        String message
     ){
         if(TestLocking.debug) System.out.println(
              Thread.currentThread().getName() + 
              " [Priority " + Thread.currentThread().getPriority() + "]: " + 
              message
         );
     }
         
    class Producer extends Thread {

        public Producer() {
            super("Producer");
        }

        /* (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        public void run() {
            try {
                log("started");
                for(
                    int i = COUNT; 
                    i >= 0; 
                    i--
                ) {
                    log("processing " + i);
                    buffer.put(new Integer(i));
                }
                log("terminated");
            } catch (InterruptedException e) {
                log("interrupted");
            } catch (Throwable throwable) {
                log("aborted");
                throwable.printStackTrace();
            }
        }
        
    }

    class Consumer extends Thread {

        public Consumer() {
            super("Consumer");
        }

        /* (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        public void run() {
            try {
                log("started");
                for(
                    int i = COUNT; 
                    i >= 0; 
                    i--
                ) {
                    log("processing " + i);
                    assertEquals("Element", i, ((Integer)buffer.take()).intValue());
                }
                log("terminated");
            } catch (InterruptedException e) {
                log("interrupted");
            } catch (Throwable throwable) {
                log("aborted");
                throwable.printStackTrace();
            }
        }
                      
    }

    
    public void testLocking() throws Throwable {
        log("started");
        this.consumer.start();
        this.producer.start();
        log("joining");
        this.producer.join();
        this.consumer.join();
        log("terminated");
    }

}

