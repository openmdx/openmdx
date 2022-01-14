/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Resource Local User Transaction Adapter 
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
package org.openmdx.base.accessor.rest.spi;

import javax.jdo.PersistenceManager;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;

import org.openmdx.base.accessor.rest.DataObjectManager_1;
import org.openmdx.base.resource.spi.LocalTransactions;
import org.openmdx.base.transaction.LocalUserTransaction;

/**
 * Resource Local User Transaction Adapter
 */
class ResourceLocaUserTransactionAdapter implements LocalUserTransaction {

    /**
     * Constructor 
     *
     * @param persistenceManager
     * 
     * @throws ResourceException
     */
    ResourceLocaUserTransactionAdapter(
        PersistenceManager persistenceManager
    ) throws ResourceException{
        this.localTransaction = LocalTransactions.getLocalTransaction(
            DataObjectManager_1.getLocalTransaction(persistenceManager)
        ); 
    }

    /**
     * The delegate in case of an optimistic transaction
     */
    private final LocalTransaction localTransaction;
    
    /**
     * @throws ResourceException
     * @see javax.resource.spi.LocalTransaction#begin()
     */
    public void begin(
    ) throws ResourceException {
        this.localTransaction.begin();
    }

    /**
     * @throws ResourceException
     * @see javax.resource.spi.LocalTransaction#commit()
     */
    public void commit(
    ) throws ResourceException {
        this.localTransaction.commit();
    }

    /**
     * @throws ResourceException
     * @see javax.resource.spi.LocalTransaction#rollback()
     */
    public void rollback(
    ) throws ResourceException {
        this.localTransaction.rollback();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.UserTransaction_2_0#setRollbackOnly()
     */
//  @Override
    public void setRollbackOnly(
    ) throws ResourceException {
        throw new NotSupportedException(
            "Rollback only methods are not supported for resource local transactions"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.UserTransaction_2_0#isRollbackOnly()
     */
//  @Override
    public boolean isRollbackOnly(
    ) throws ResourceException {
        throw new NotSupportedException(
            "Rollback only methods are not supported for resource local transactions"
        );
    }

} 
