/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: User Transaction For Container Managed Units Of Work 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.application.transaction;

#if JAVA_8
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.LocalTransactionException;
import javax.transaction.TransactionSynchronizationRegistry;
#else
import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.LocalTransactionException;
import jakarta.transaction.TransactionSynchronizationRegistry;
#endif

import org.openmdx.base.transaction.LocalUserTransaction;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.naming.ComponentEnvironment;

/**
 * User Transaction For Container Managed Units Of Work
 * <p>
 * Instantiated reflecively by org.openmdx.base.accessor.rest.spi.LocalUserTransactionAdapters
 * 
 * @see org.openmdx.base.accessor.rest.spi.LocalUserTransactionAdapters
 */
public class ContainerManagedLocalUserTransactionAdapter implements LocalUserTransaction {

    /**
     * Constructor 
     *
     * Instantiated reflectively by org.openmdx.base.accessor.rest.spi.LocalUserTransactionAdapters
     * 
     * @see org.openmdx.base.accessor.rest.spi.LocalUserTransactionAdapters.getContainerManagedUserTransactionAdapter()
     */
    public ContainerManagedLocalUserTransactionAdapter() {
        super();
    }

    /**
     * The transaction synchronization registry
     */
    private static TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    private static TransactionSynchronizationRegistry getTransactionSynchronizationRegistry(
    ) throws ResourceException {
        if(transactionSynchronizationRegistry == null) {
            try {
                transactionSynchronizationRegistry = ComponentEnvironment.lookup(
                    TransactionSynchronizationRegistry.class
                    );
            } catch (BasicException exception) {
                throw new LocalTransactionException(
                    "TransactionSynchronizationRegistry acquisition failure",
                    exception
                    );
            }
        }
        return transactionSynchronizationRegistry;
    }

    /**
     * @throws ResourceException
     * @see javax.resource.spi.LocalTransaction#begin()
     */
    @Override
    public void begin(
    ) throws ResourceException {
        throw new NotSupportedException(
            "Transaction start and stop methods are not supported for container managed units of work"
            );
    }

    /**
     * @throws ResourceException
     * @see javax.resource.spi.LocalTransaction#commit()
     */
    @Override
    public void commit(
    ) throws ResourceException {
        throw new NotSupportedException(
            "Transaction start and stop methods are not supported for container managed units of work"
            );
    }

    /**
     * @throws ResourInstantiatedceException
     * @see javax.resource.spi.LocalTransaction#rollback()
     */
    @Override
    public void rollback(
    ) throws ResourceException {
        throw new NotSupportedException(
            "Transaction start and stop methods are not supported for container managed units of work"
            );
    }
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.UserTransaction_2_0#setRollbackOnly()
     */
    @Override
    public void setRollbackOnly(
    ) throws ResourceException {
        getTransactionSynchronizationRegistry().setRollbackOnly();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.UserTransaction_2_0#isRollbackOnly()
     */
    @Override
    public boolean isRollbackOnly(
    ) throws ResourceException {
        return getTransactionSynchronizationRegistry().getRollbackOnly();
    }

} 
