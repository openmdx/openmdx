/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestPersistenceManagerFactory.java,v 1.1 2006/05/12 20:11:22 hburger Exp $
 * Description: Test Persistence Manager Factory
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/05/12 20:11:22 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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
package org.openmdx.test.base.accessor.jmi.spi;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.base.accessor.jmi.spi.PersistenceManagerFactory_1;

public class TestPersistenceManagerFactory extends TestCase {

	/**
	 * Constructs a test case with the given name.
	 */
	public TestPersistenceManagerFactory(String name)
	{
		super(name);
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
	 * A test runner either expects a static method suite as the
	 * entry point to get a test to run or it will extract the
	 * suite automatically.
	 */
	public static Test suite()
	{
		return new TestSuite(TestPersistenceManagerFactory.class);
	}

    /**
     * 
     */
    public void testPrincipalChain(
    ) {
        assertEquals(
            "null",
            new String[]{}, 
            PersistenceManagerFactory_1.getPrincipalChain(null)
                
        );
        assertEquals(
            "empty", 
            new String[]{}, 
            PersistenceManagerFactory_1.getPrincipalChain("")
        );
        assertEquals(
            "principal", 
            new String[]{"principal"}, 
            PersistenceManagerFactory_1.getPrincipalChain("principal")
        );
        String[] principals = new String[]{"principal0", "principal1", "principal2"}; 
        assertEquals(
            "principals", 
            principals, 
            PersistenceManagerFactory_1.getPrincipalChain(
                Arrays.asList(principals).toString()
            )
        );
    }

    /**
     * Asserts that two objects are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void assertEquals(
        String message, 
        Object[] expected, 
        Object[] actual
    ) {
        if(Arrays.equals(expected, actual)) return;
        fail(
            message + " expected:<" + Arrays.asList(expected) + "> but was:<" + Arrays.asList(actual) + ">"
        );
    }
        
}
