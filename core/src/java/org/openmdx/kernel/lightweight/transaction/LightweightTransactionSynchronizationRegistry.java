/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LightweightTransactionSynchronizationRegistry.java,v 1.1 2009/09/07 15:00:30 hburger Exp $
 * Description: Lightweight Transaction Manager Registry
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/07 15:00:30 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
 * All rights reserved.
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
 */
public final class LightweightTransactionSynchronizationRegistry 
    implements TransactionSynchronizationRegistry
{

    /**
     * Constructor 
     *
     * @param lightweightTransactionManager
     */
    public LightweightTransactionSynchronizationRegistry(
        LightweightTransactionManager lightweightTransactionManager
    ) {
        this.lightweightTransactionManager = lightweightTransactionManager;
    }

    /**
     * 
     */
    private final LightweightTransactionManager lightweightTransactionManager;

    /**
     * Retrieve the active transaction
     * 
     * @return the active transaction
     */
    private LightweightTransaction getTransaction(){
        LightweightTransaction transaction = this.lightweightTransactionManager.bindings.get(Thread.currentThread());
        if (transaction == null) throw new IllegalStateException(
            "No transaction is active"
        );
        return transaction;
    }
    
    /* (non-Javadoc)
     * @see javax.transaction.TransactionSynchronizationRegistry#getResource(java.lang.Object)
     */
    public Object getResource(Object key) {
        return getTransaction().managedResources.get(key);
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
        return getTransaction().xid;
    }

    /* (non-Javadoc)
     * @see javax.transaction.TransactionSynchronizationRegistry#getTransactionStatus()
     */
    public int getTransactionStatus() {
        return getTransaction().status;
    }

    /* (non-Javadoc)
     * @see javax.transaction.TransactionSynchronizationRegistry#putResource(java.lang.Object, java.lang.Object)
     */
    public void putResource(
        Object key, 
        Object value
    ) {
        getTransaction().managedResources.put(key, value);
    }

    /* (non-Javadoc)
     * @see javax.transaction.TransactionSynchronizationRegistry#registerInterposedSynchronization(javax.transaction.Synchronization)
     */
    public void registerInterposedSynchronization(Synchronization sync) {
        getTransaction().interposedSynchronization = sync;
    }

    /* (non-Javadoc)
     * @see javax.transaction.TransactionSynchronizationRegistry#setRollbackOnly()
     */
    public void setRollbackOnly(
    ) {
        try {
            getTransaction().setRollbackOnly();
        } catch (SystemException exception) {
            throw new RuntimeException(exception);
        }
    }
}