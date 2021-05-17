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

import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.transaction.Synchronization;

import org.openmdx.base.persistence.spi.UnitOfWork;
import org.openmdx.base.transaction.Status;

/**
 * Transaction Adapter
 */
class TransactionAdapter implements Transaction, javax.transaction.Synchronization {

    /**
     * Constructor
     *
     * @param unitOfWork
     */
    TransactionAdapter(
        UnitOfWork unitOfWork
    ) {
        this.unitOfWork = unitOfWork;
    }

    private final UnitOfWork unitOfWork;

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#begin()
     */
    @Override
    public void begin() {
        this.unitOfWork.begin();
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#commit()
     */
    @Override
    public void commit() {
        this.unitOfWork.commit();
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#rollback()
     */
    @Override
    public void rollback() {
        this.unitOfWork.rollback();
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#isActive()
     */
    @Override
    public boolean isActive() {
        return this.unitOfWork.isActive();
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#getRollbackOnly()
     */
    @Override
    public boolean getRollbackOnly() {
        return this.unitOfWork.getRollbackOnly();
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#setRollbackOnly()
     */
    @Override
    public void setRollbackOnly() {
        this.unitOfWork.setRollbackOnly();
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#setNontransactionalRead(boolean)
     */
    @Override
    public void setNontransactionalRead(boolean nontransactionalRead) {
        this.unitOfWork.setNontransactionalRead(nontransactionalRead);
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#getNontransactionalRead()
     */
    @Override
    public boolean getNontransactionalRead() {
        return this.unitOfWork.getNontransactionalRead();
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#setNontransactionalWrite(boolean)
     */
    @Override
    public void setNontransactionalWrite(boolean nontransactionalWrite) {
        this.unitOfWork.setNontransactionalWrite(nontransactionalWrite);
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#getNontransactionalWrite()
     */
    @Override
    public boolean getNontransactionalWrite() {
        return this.unitOfWork.getNontransactionalWrite();
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#setRetainValues(boolean)
     */
    @Override
    public void setRetainValues(boolean retainValues) {
        this.unitOfWork.setRetainValues(retainValues);
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#getRetainValues()
     */
    @Override
    public boolean getRetainValues() {
        return this.unitOfWork.getRetainValues();
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#setRestoreValues(boolean)
     */
    @Override
    public void setRestoreValues(boolean restoreValues) {
        this.unitOfWork.setRestoreValues(restoreValues);
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#getRestoreValues()
     */
    @Override
    public boolean getRestoreValues() {
        return this.unitOfWork.getRestoreValues();
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#setOptimistic(boolean)
     */
    @Override
    public void setOptimistic(boolean optimistic) {
        this.unitOfWork.setOptimistic(optimistic);
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#getOptimistic()
     */
    @Override
    public boolean getOptimistic() {
        return this.unitOfWork.getOptimistic();
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#getIsolationLevel()
     */
    @Override
    public String getIsolationLevel() {
        return this.unitOfWork.getIsolationLevel();
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#setIsolationLevel(String)
     */
    @Override
    public void setIsolationLevel(String level) {
        this.unitOfWork.setIsolationLevel(level);
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#getPersistenceManager()
     */
    @Override
    public PersistenceManager getPersistenceManager() {
        return this.unitOfWork.getPersistenceManager();
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#getSynchronization(Synchronization)
     */
    @Override
    public void setSynchronization(Synchronization sync) {
        this.unitOfWork.setSynchronization(
            new UnitOfWorkSynchronizationAdapter(sync)
        );
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#getSynchronization()
     */
    @Override
    public Synchronization getSynchronization() {
        org.openmdx.base.persistence.cci.Synchronization synchronization = this.unitOfWork.getSynchronization();
        if (synchronization == null) {
            return null;
        } else if (synchronization instanceof UnitOfWorkSynchronizationAdapter) {
            return ((UnitOfWorkSynchronizationAdapter) synchronization).getDelegate();
        } else {
            throw new JDOUserException(
                "A synchronization object set to org.openmdx.kernel.jdo.LocalTransaction " +
                    "can't be retrieved through javax.jdo.Transaction"
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#beforeCompletion()
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

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setSerializeRead(java.lang.Boolean)
     */
    @Override
    public void setSerializeRead(Boolean serialize) {
        if(Boolean.TRUE.equals(serialize)) {
            throw new JDOFatalDataStoreException("openMDX does not support read serialization");
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getSerializeRead()
     */
    @Override
    public Boolean getSerializeRead() {
        return null;
    }

}
