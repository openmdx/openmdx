/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Abstract LocalUserTransactionAdapters Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 *
 * Copyright (c) 2022, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.rest.spi;

import java.util.concurrent.Callable;

import org.openmdx.junit5.OpenmdxCoreStandardExtension;

abstract class AbstractLocalUserTransactionAdaptersTest {

    private static final String JTA_KEY = "org.openmdx.base.transaction.LocalUserTransaction.jta";
    private static final String CONTAINER_MANAGED_KEY = "org.openmdx.base.transaction.LocalUserTransaction.containerManaged";

    private final OpenmdxCoreStandardExtension openmdxCoreStandardExtension = new OpenmdxCoreStandardExtension();

    protected void beforeAll() throws Exception {
    	openmdxCoreStandardExtension.beforeAll(null);
        System.setProperty(JTA_KEY, jtaUserTransactionClassName());
        System.setProperty(CONTAINER_MANAGED_KEY, containerManagedUserTransactionClassName());
    }

    protected void beforeEach() throws Exception {
    	// Nothing to do yet
    }
    
    protected void afterEach() throws Exception {
    	// Nothing to do yet
    }
    
    protected void afterAll() {
        System.clearProperty(JTA_KEY);
        System.clearProperty(CONTAINER_MANAGED_KEY);
    }

    protected abstract String jtaUserTransactionClassName();
    protected abstract String containerManagedUserTransactionClassName();
    
    protected abstract Void testGetJTAUserTransactionAdapterClass() throws Exception;

    protected abstract Void testGetContainerManagedUserTransactionAdapter() throws Exception;

	public void testAll() throws Exception {
		beforeAll();
		try {
			test(this::testGetJTAUserTransactionAdapterClass);
			test(this::testGetContainerManagedUserTransactionAdapter);
		} catch (Exception exception) {
	    	afterAll();
	    	throw exception;
		}
	}

    private void test(Callable<Void> testee) throws Exception {
    	beforeEach();
    	try {
	    	testee.call();
    	} catch (Exception exception) {
	    	afterEach();
	    	throw exception;
    	}
    }

}