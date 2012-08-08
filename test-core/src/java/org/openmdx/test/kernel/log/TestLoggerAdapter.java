/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestLoggerAdapter.java,v 1.4 2007/03/29 10:37:35 hburger Exp $
 * Description: TestLoggerAdapter 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/03/29 10:37:35 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package org.openmdx.test.kernel.log;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmdx.application.log.AppLog;
import org.openmdx.kernel.log.SysLog;
import org.slf4j.InvocationTest;

/**
 * TestLoggerAdapter
 */
public class TestLoggerAdapter
    extends InvocationTest
{

    /**
     * Constructor 
     *
     * @param name
     */
    public TestLoggerAdapter(String name) {
        super(name);
    }

    /**
     * The batch TestRunner can be given a class to run directly.
     * To start the batch runner from your main you can write: 
     */
    public static void main (
        String[] args
    ){
        TestRunner.run (suite());
    }


    /* (non-Javadoc)
     * @see org.slf4j.InvocationTest#setUp()
     */
    protected void setUp(
    )throws Exception {
        super.setUp();
        this.log = "testFromJCL".equals(getName()) ?
            LogFactory.getLog(TestLoggerAdapter.class) :
            null;
    }

    /**
     * A test runner either expects a static method suite as the
     * entry point to get a test to run or it will extract the 
     * suite automatically. 
     */
    public static Test suite(
    ){
//        return new TestSuite(TestLoggerAdapter.class);
        TestSuite suite = new TestSuite();
        suite.addTest(new TestLoggerAdapter("testSysLog"));
        return suite;
    }

    /**
     * Test Jakarta Commons Logging 1.0.4
     */
    public void testFromJCL() {
        Exception e = new Exception("This is a test exception");
        log.trace("A trace message");
        log.debug("A debug message");
        log.info("An informational message");
        log.warn("A warning", e);
        log.error("An error", e);
        log.fatal("A fatal exception", e);
    }

    /**
     * Test Logging via SysLog
     */
    public void testSysLog() {
        Exception e = new Exception("This is a test exception");
        SysLog.trace("A trace message");
        SysLog.detail("A debug message");
        SysLog.info("An informational message");
        SysLog.warning("A warning", e);
        SysLog.error("An error", e);
        SysLog.criticalError("A fatal exception", e);
    }
    
    /**
     * Test Logging via AppLog
     */
    public void testAppLog() {
        Exception e = new Exception("This is a test exception");
        AppLog.trace("A trace message");
        AppLog.detail("A debug message");
        AppLog.info("An informational message");
        AppLog.warning("A warning", e);
        AppLog.error("An error", e);
        AppLog.criticalError("A fatal exception", e);
    }

    /**
     * Jakarta Commons Logging 1.0.4 Logger
     */
    private Log log;
    
}
