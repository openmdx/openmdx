/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: LocalUserTransactionAdapters Test with wrong
 *              classes
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * * Redistribution and use in source and binary forms, with or
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

import static org.junit.jupiter.api.Assertions.assertThrows;

#if JAVA_8 
	import javax.resource.ResourceException;
	import javax.resource.cci.LocalTransaction;
	import javax.resource.spi.LocalTransactionException;
#else 
	import jakarta.resource.ResourceException;
	import jakarta.resource.cci.LocalTransaction;
	import jakarta.resource.spi.LocalTransactionException;
#endif;


public class LocalUserTransactionAdaptersTestWithWrongClasses extends AbstractLocalUserTransactionAdaptersTest {

	@Override
	protected String jtaUserTransactionClassName() {
		return JTAImplementingWrongInterface.class.getName();
	}

	@Override
	protected String containerManagedUserTransactionClassName() {
		return ContainerManagedImplementingWrongInterface.class.getName();
	}

	@Override
	protected Void testGetJTAUserTransactionAdapterClass() throws ResourceException {
		assertThrows(LocalTransactionException.class, LocalUserTransactionAdapters::getJTAUserTransactionAdapter);
		// implements Callable
		return null;
	}

	@Override
	protected Void testGetContainerManagedUserTransactionAdapter() throws ResourceException {
		assertThrows(LocalTransactionException.class, LocalUserTransactionAdapters::getContainerManagedUserTransactionAdapter);
		// implements Callable
		return null;
	}

	public static void main(String... arguments) throws Exception {
		new LocalUserTransactionAdaptersTestWithWrongClasses().testAll();
	}

	/**
	 * Implements javax.resource.cci.LocalTransaction instead of org.openmdx.base.transaction.LocalUserTransaction
	 */
	static class JTAImplementingWrongInterface implements LocalTransaction {

		@Override
		public void begin() throws ResourceException {
			// Non-functional
		}

		@Override
		public void commit() throws ResourceException {
			// Non-functional
		}

		@Override
		public void rollback() throws ResourceException {
			// Non-functional
		}

	}

	/**
	 * Implements javax.resource.cci.LocalTransaction instead of org.openmdx.base.transaction.LocalUserTransaction
	 */
	static class ContainerManagedImplementingWrongInterface implements LocalTransaction {

		@Override
		public void begin() throws ResourceException {
			// Non-functional
		}

		@Override
		public void commit() throws ResourceException {
			// Non-functional
		}

		@Override
		public void rollback() throws ResourceException {
			// Non-functional
		}

	}

}