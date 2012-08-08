/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestRemoteExceptions.java,v 1.1 2005/06/09 19:31:32 hburger Exp $
 * Description: Test RemoteExceptions
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/06/09 19:31:32 $
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
package org.openmdx.test.base.exception;

import java.net.MalformedURLException;
import java.rmi.RemoteException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.base.exception.RemoteExceptions;

/**
 * Test RemoteExceptions
 */
public class TestRemoteExceptions extends TestCase {

    /**
     * Constructs a test case with the given name.
     */
    public TestRemoteExceptions(String name) {
        super(name);
    }
    
    /**
     * A test runner either expects a static method suite as the
     * entry point to get a test to run or it will extract the 
     * suite automatically. 
     */
    public static Test suite() {
        return new TestSuite(TestRemoteExceptions.class);
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
    public void testRetriability(
    ){
        assertRetriability(
            true,
            new java.rmi.ConnectException("Test 1")
        );
        assertRetriability(
           false, 
           new java.rmi.RemoteException("Test 2")
        );
        assertRetriability(
           true,
           setDetail(
               new java.rmi.RemoteException("Test 3"),
               new java.net.ConnectException("Test 3")
           )
        );
        assertRetriability(
            false, 
            setDetail(
                new java.rmi.RemoteException("Test 4"),
                new MalformedURLException("Test 4")
            )
        );
        assertRetriability(
            true, 
            new java.rmi.ConnectIOException(
                "Test 5",
                new java.net.ConnectException("Test 5")
             )
        );
        assertRetriability(
             false, 
             new java.rmi.MarshalException("Test 6")
         );
    }

    /**
     * Assert a <code>RemoteExceptions</code>'s expected retriability.
     * 
     * @param expected
     * @param exception
     */
    protected static void assertRetriability(
        boolean expected,
        RemoteException exception
    ){
        assertEquals(
            getClassName(exception) + '*' + getClassName(exception.detail) + " (" + exception.getMessage() + ")",
            expected,
            RemoteExceptions.isRetriable(exception)
        );
    }
            
    /**
     * Retrieve the detail exception's class name
     * 
     * @param throwable
     * 
     * @return the throwable's class name; or <code>"null"</code> in case of 
     * <code>null</code>
     */
    private static String getClassName(
        Throwable throwable    
    ){
        return throwable == null ? "null" : throwable.getClass().getName();
    }

    /**
     * Set a <code>RemoteException</code>'s detail
     * 
     * @param exception
     * @param detail
     * 
     * @return the given exception
     */
    private static RemoteException setDetail (
        RemoteException exception,
        Throwable detail
    ){
        exception.detail = detail;
        return exception;
    }

}
