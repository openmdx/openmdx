/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: LocalUserTransactionAdapters Test with user defined
 *              classes
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.resource.ResourceException;

import org.openmdx.base.transaction.LocalUserTransaction;

public class LocalUserTransactionAdaptersTestWithUserDefinedClasses extends AbstractLocalUserTransactionAdaptersTest {

	@Override
	protected String jtaUserTransactionClassName() {
		return MyJTALocalUserTransactionAdapter.class.getName();
	}

	@Override
	protected String containerManagedUserTransactionClassName() {
		return MyContainerManagedLocalUserTransactionAdapter.class.getName();
	}

	@Override
	protected Void testGetJTAUserTransactionAdapterClass() throws ResourceException {
		// act
		final LocalUserTransaction localUserTransaction = LocalUserTransactionAdapters.getJTAUserTransactionAdapter();
		// assert
		assertTrue(localUserTransaction instanceof MyJTALocalUserTransactionAdapter);
		// implements Callable
		return null;
	}

	@Override
	protected Void testGetContainerManagedUserTransactionAdapter() throws ResourceException {
		// act
		final LocalUserTransaction localUserTransaction = LocalUserTransactionAdapters
				.getContainerManagedUserTransactionAdapter();
		// assert
		assertTrue(localUserTransaction instanceof MyContainerManagedLocalUserTransactionAdapter);
		// implements Callable
		return null;
	}

	public static void main(String... arguments) throws Exception {
		new LocalUserTransactionAdaptersTestWithUserDefinedClasses().testAll();
	}

	static class MyJTALocalUserTransactionAdapter implements LocalUserTransaction {

		@Override
		public void setRollbackOnly() throws ResourceException {
			// non-functional
		}

		@Override
		public boolean isRollbackOnly() throws ResourceException {
			return false;
		}

		@Override
		public void begin() throws ResourceException {
			// non-functional
		}

		@Override
		public void commit() throws ResourceException {
			// non-functional
		}

		@Override
		public void rollback() throws ResourceException {
			// non-functional
		}
	}

	static class MyContainerManagedLocalUserTransactionAdapter implements LocalUserTransaction {

		@Override
		public void setRollbackOnly() throws ResourceException {
			// non-functional
		}

		@Override
		public boolean isRollbackOnly() throws ResourceException {
			return false;
		}

		@Override
		public void begin() throws ResourceException {
			// non-functional
		}

		@Override
		public void commit() throws ResourceException {
			// non-functional
		}

		@Override
		public void rollback() throws ResourceException {
			// non-functional
		}
	}

}