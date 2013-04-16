/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Transaction Adapter 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2012, OMEX AG, Switzerland
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
package org.openmdx.application.persistence.adapter;

import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.transaction.Synchronization;

import org.openmdx.base.accessor.rest.spi.Synchronization_2_0;
import org.openmdx.base.persistence.spi.UnitOfWork;
import org.openmdx.base.transaction.Status;

/**
 * Transaction Adapter
 */
@SuppressWarnings("deprecation")
class TransactionAdapter implements Transaction, javax.transaction.Synchronization, Synchronization_2_0 {

    /**
     * Constructor 
     *
     * @param unitOfWork
     */
    TransactionAdapter (
        UnitOfWork unitOfWork
    ) {
        this.unitOfWork = unitOfWork;
    }

    private final UnitOfWork unitOfWork;

    /**
     * 
     * @see org.openmdx.kernel.jdo.LocalTransaction#begin()
     */
    public void begin() {
        this.unitOfWork.begin();
    }

    /**
     * 
     * @see org.openmdx.kernel.jdo.LocalTransaction#commit()
     */
    public void commit() {
        this.unitOfWork.commit();
    }

    /**
     * 
     * @see org.openmdx.kernel.jdo.LocalTransaction#rollback()
     */
    public void rollback() {
        this.unitOfWork.rollback();
    }

    /**
     * @return
     * @see org.openmdx.kernel.jdo.LocalTransaction#isActive()
     */
    public boolean isActive() {
        return this.unitOfWork.isActive();
    }

    /**
     * @return
     * @see org.openmdx.kernel.jdo.LocalTransaction#getRollbackOnly()
     */
    public boolean getRollbackOnly() {
        return this.unitOfWork.getRollbackOnly();
    }

    /**
     * 
     * @see org.openmdx.kernel.jdo.LocalTransaction#setRollbackOnly()
     */
    public void setRollbackOnly() {
        this.unitOfWork.setRollbackOnly();
    }

    /**
     * @param nontransactionalRead
     * @see org.openmdx.kernel.jdo.LocalTransaction#setNontransactionalRead(boolean)
     */
    public void setNontransactionalRead(boolean nontransactionalRead) {
        this.unitOfWork.setNontransactionalRead(nontransactionalRead);
    }

    /**
     * @return
     * @see org.openmdx.kernel.jdo.LocalTransaction#getNontransactionalRead()
     */
    public boolean getNontransactionalRead() {
        return this.unitOfWork.getNontransactionalRead();
    }

    /**
     * @param nontransactionalWrite
     * @see org.openmdx.kernel.jdo.LocalTransaction#setNontransactionalWrite(boolean)
     */
    public void setNontransactionalWrite(boolean nontransactionalWrite) {
        this.unitOfWork.setNontransactionalWrite(nontransactionalWrite);
    }

    /**
     * @return
     * @see org.openmdx.kernel.jdo.LocalTransaction#getNontransactionalWrite()
     */
    public boolean getNontransactionalWrite() {
        return this.unitOfWork.getNontransactionalWrite();
    }

    /**
     * @param retainValues
     * @see org.openmdx.kernel.jdo.LocalTransaction#setRetainValues(boolean)
     */
    public void setRetainValues(boolean retainValues) {
        this.unitOfWork.setRetainValues(retainValues);
    }

    /**
     * @return
     * @see org.openmdx.kernel.jdo.LocalTransaction#getRetainValues()
     */
    public boolean getRetainValues() {
        return this.unitOfWork.getRetainValues();
    }

    /**
     * @param restoreValues
     * @see org.openmdx.kernel.jdo.LocalTransaction#setRestoreValues(boolean)
     */
    public void setRestoreValues(boolean restoreValues) {
        this.unitOfWork.setRestoreValues(restoreValues);
    }

    /**
     * @return
     * @see org.openmdx.kernel.jdo.LocalTransaction#getRestoreValues()
     */
    public boolean getRestoreValues() {
        return this.unitOfWork.getRestoreValues();
    }

    /**
     * @param optimistic
     * @see org.openmdx.kernel.jdo.LocalTransaction#setOptimistic(boolean)
     */
    public void setOptimistic(boolean optimistic) {
        this.unitOfWork.setOptimistic(optimistic);
    }

    /**
     * @return
     * @see org.openmdx.kernel.jdo.LocalTransaction#getOptimistic()
     */
    public boolean getOptimistic() {
        return this.unitOfWork.getOptimistic();
    }

    /**
     * @return
     * @see org.openmdx.kernel.jdo.LocalTransaction#getIsolationLevel()
     */
    public String getIsolationLevel() {
        return this.unitOfWork.getIsolationLevel();
    }

    /**
     * @param level
     * @see org.openmdx.kernel.jdo.LocalTransaction#setIsolationLevel(java.lang.String)
     */
    public void setIsolationLevel(String level) {
        this.unitOfWork.setIsolationLevel(level);
    }

    /**
     * @param sync
     * @see org.openmdx.kernel.jdo.LocalTransaction#setSynchronization(org.openmdx.base.persistence.cci.Synchronization)
     */
    public void setSynchronization(
        org.openmdx.base.persistence.cci.Synchronization sync) {
        this.unitOfWork.setSynchronization(sync);
    }

    /**
     * @return
     * @see org.openmdx.kernel.jdo.LocalTransaction#getPersistenceManager()
     */
    public PersistenceManager getPersistenceManager() {
        return this.unitOfWork.getPersistenceManager();
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setSynchronization(javax.transaction.Synchronization)
     */
    @Override
    public void setSynchronization(Synchronization sync) {
        this.unitOfWork.setSynchronization(
            new SynchronizationAdapter(sync)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getSynchronization()
     */
    @Override
    public Synchronization getSynchronization() {
        org.openmdx.base.persistence.cci.Synchronization synchronization =
            this.unitOfWork.getSynchronization();
        if(synchronization == null) {
            return null;
        } else if (synchronization instanceof SynchronizationAdapter) {
            return ((SynchronizationAdapter)synchronization).getDelegate();
        } else {
            throw new JDOUserException(
                "A synchronization object set to org.openmdx.kernel.jdo.LocalTransaction " +
                "can't be retrieved through javax.jdo.Transaction"
            );
        }
    }

    /**
     * 
     * @see org.openmdx.base.persistence.cci.Synchronization#beforeCompletion()
     */
    @Override
    public void beforeCompletion() {
        this.unitOfWork.beforeCompletion();
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#afterCompletion(int)
     */
    @Override
    public void afterCompletion(int status) {
        this.unitOfWork.afterCompletion(Status.valueOf(status));
    }

    /**
     * 
     * @see javax.transaction.Synchronization#afterBegin()
     */
    @Override
    public void afterBegin() {
        this.unitOfWork.afterBegin();
    }

    /**
     * 
     * @see org.openmdx.base.persistence.cci.UnitOfWork#clear()
     */
    @Override
    public void clear() {
        this.unitOfWork.clear();
    }

    
    //------------------------------------------------------------------------
    // Class Synchronization Adapter
    //------------------------------------------------------------------------
    
    /**
     * Synchronization Adapter
     */
    static class SynchronizationAdapter implements org.openmdx.base.persistence.cci.Synchronization {

        /**
         * Constructor 
         *
         * @param delegate
         */
        SynchronizationAdapter(
            Synchronization delegate
        ) {
            this.delegate = delegate;
        }

        private final Synchronization delegate;
        
        /* (non-Javadoc)
         * @see javax.transaction.Synchronization#afterCompletion(int)
         */
        @Override
        public void afterCompletion(Status status) {
            this.delegate.afterCompletion(status.ordinal());
        }

        /* (non-Javadoc)
         * @see javax.transaction.Synchronization#beforeCompletion()
         */
        @Override
        public void beforeCompletion() {
            this.delegate.beforeCompletion();
        }
        
        /**
         * Retrieve delegate.
         *
         * @return Returns the delegate.
         */
        Synchronization getDelegate() {
            return this.delegate;
        }

    }

}
