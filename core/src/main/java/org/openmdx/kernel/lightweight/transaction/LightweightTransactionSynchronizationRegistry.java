/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Lightweight Transaction Synchronization Registry
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.kernel.lightweight.transaction;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionSynchronizationRegistry;

/**
 * Lightweight Transaction Synchronization Registry
 * <p>
 * The transaction synchronization registry is a thread-safe proxy.
 * 
 * @deprecated in favour of Atomikos' transaction manager
 */
@Deprecated
public final class LightweightTransactionSynchronizationRegistry 
    implements TransactionSynchronizationRegistry
{

    /**
     * The transaction synchronization registry instance may be shared
     */
    private static volatile TransactionSynchronizationRegistry instance;
    
    /**
     * A transaction synchronization registry instance may be shared
     * 
     * @return a transaction synchronization registry instance
     */
    public static TransactionSynchronizationRegistry getInstance(){
        if(instance == null) {
            instance = new LightweightTransactionSynchronizationRegistry();
        }
        return instance;
    }
    
    /**
     * Retrieve the active transaction
     * 
     * @return the active transaction
     */
    private LightweightTransaction getTransaction(boolean required){
        final LightweightTransactionManager transactionManager = LightweightTransactionManager.getInstance();
        return required ? transactionManager.getTransaction(true) : transactionManager.getTransaction();
    }
    
    /* (non-Javadoc)
     * @see javax.transaction.TransactionSynchronizationRegistry#getResource(java.lang.Object)
     */
    public Object getResource(Object key) {
        return getTransaction(true).managedResources.get(key);
    }

    /* (non-Javadoc)
     * @see javax.transaction.TransactionSynchronizationRegistry#getRollbackOnly()
     */
    public boolean getRollbackOnly() {
        return getTransactionStatus() == Status.STATUS_MARKED_ROLLBACK;
    }

    /* (non-Javadoc)
     * @see javax.transaction.TransactionSynchronizationRegistry#getTransactionKey()
     */
    public Object getTransactionKey() {
        final LightweightTransaction transaction = getTransaction(false);
        return transaction == null ? null : transaction.xid;
    }

    /* (non-Javadoc)
     * @see javax.transaction.TransactionSynchronizationRegistry#getTransactionStatus()
     */
    public int getTransactionStatus() {
        final LightweightTransaction transaction = getTransaction(false);
        return transaction == null ? Status.STATUS_NO_TRANSACTION : transaction.status;
    }

    /* (non-Javadoc)
     * @see javax.transaction.TransactionSynchronizationRegistry#putResource(java.lang.Object, java.lang.Object)
     */
    public void putResource(
        Object key, 
        Object value
    ) {
        getTransaction(true).managedResources.put(key, value);
    }

    /* (non-Javadoc)
     * @see javax.transaction.TransactionSynchronizationRegistry#registerInterposedSynchronization(javax.transaction.Synchronization)
     */
    public void registerInterposedSynchronization(Synchronization sync) {
        getTransaction(true).interposedSynchronizations.add(sync);
    }

    /* (non-Javadoc)
     * @see javax.transaction.TransactionSynchronizationRegistry#setRollbackOnly()
     */
    public void setRollbackOnly(
    ) {
        try {
            getTransaction(true).setRollbackOnly();
        } catch (SystemException exception) {
            throw new RuntimeException(exception);
        }
    }
}