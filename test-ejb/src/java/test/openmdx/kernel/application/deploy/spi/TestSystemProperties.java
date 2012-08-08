/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestSystemProperties.java,v 1.1 2009/09/07 12:53:45 hburger Exp $
 * Description: TestSystemProperties 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/07 12:53:45 $
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */

package test.openmdx.kernel.application.deploy.spi;

import org.openmdx.kernel.application.deploy.spi.SystemProperties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * TestSystemProperties
 */
public class TestSystemProperties extends TestCase {

    /**
     * Constructs a test case with the given name.
     */
    public TestSystemProperties(String name) {
        super(name);
    }
    
    /**
     * A test runner either expects a static method suite as the
     * entry point to get a test to run or it will extract the 
     * suite automatically. 
     */
    public static Test suite() {
        return new TestSuite(TestSystemProperties.class);
    }

    /**
     * The batch TestRunner can be given a class to run directly.
     * To start the batch runner from your main you can write:
     */
    public static void main (String[] args)
    {
        junit.textui.TestRunner.run (suite());
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testSystemProperties() {
        assertEquals(
            "ticketCache",
            "ticketCache='" + System.getProperty("user.home") + System.getProperty("file.separator") + "tickets'",
            SystemProperties.expand("ticketCache='${user.home}${/}tickets'")
        );
        assertEquals(
            "ticketCache",
            "ticketCache='" + System.getProperty("user.home") + System.getProperty("file.separator") + "tickets'",
            SystemProperties.expand(true, "ticketCache='${user.home}${/}tickets'")
        );
        assertEquals(
            "ticketCache",
            "ticketCache='${user.home}${/}tickets'",
            SystemProperties.expand(false, "ticketCache='${user.home}${/}tickets'")
        );
    }

}
