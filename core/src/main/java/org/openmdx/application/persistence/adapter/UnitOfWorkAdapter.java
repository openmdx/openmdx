/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: UnitOfWorkAdapter 
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
package org.openmdx.application.persistence.adapter;

import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOUnsupportedOptionException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import org.openmdx.base.persistence.cci.Synchronization;
import org.openmdx.base.persistence.spi.UnitOfWork;
import org.openmdx.base.transaction.Status;


/**
 * Unit Of Work Adapter
 */
class UnitOfWorkAdapter implements UnitOfWork {

    /**
     * Constructor 
     *
     * @param transaction the delegate
     */
    UnitOfWorkAdapter(
        Transaction transaction
    ){
        this.transaction = transaction;
    }
    
    /**
     * The delegate
     */
    private final Transaction transaction;

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#begin()
     */
    @Override
    public void begin() {
        this.transaction.begin();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#commit()
     */
    @Override
    public void commit() {
        this.transaction.commit();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#rollback()
     */
    @Override
    public void rollback() {
        this.transaction.rollback();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#isActive()
     */
    @Override
    public boolean isActive() {
        return this.transaction.isActive();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#getRollbackOnly()
     */
    @Override
    public boolean getRollbackOnly() {
        return this.transaction.getRollbackOnly();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#setRollbackOnly()
     */
    @Override
    public void setRollbackOnly() {
        this.transaction.setRollbackOnly();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#setNontransactionalRead(boolean)
     */
    @Override
    public void setNontransactionalRead(boolean nontransactionalRead) {
        this.transaction.setNontransactionalRead(nontransactionalRead);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#getNontransactionalRead()
     */
    @Override
    public boolean getNontransactionalRead() {
        return this.transaction.getNontransactionalRead();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#setNontransactionalWrite(boolean)
     */
    @Override
    public void setNontransactionalWrite(boolean nontransactionalWrite) {
        this.transaction.setNontransactionalWrite(nontransactionalWrite);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#getNontransactionalWrite()
     */
    @Override
    public boolean getNontransactionalWrite() {
        return this.transaction.getNontransactionalWrite();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#setRetainValues(boolean)
     */
    @Override
    public void setRetainValues(boolean retainValues) {
        this.transaction.setRetainValues(retainValues);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#getRetainValues()
     */
    @Override
    public boolean getRetainValues() {
        return this.transaction.getRestoreValues();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#setRestoreValues(boolean)
     */
    @Override
    public void setRestoreValues(boolean restoreValues) {
        this.transaction.setRestoreValues(restoreValues);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#getRestoreValues()
     */
    @Override
    public boolean getRestoreValues() {
        return this.transaction.getRestoreValues();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#setOptimistic(boolean)
     */
    @Override
    public void setOptimistic(boolean optimistic) {
        this.transaction.setOptimistic(optimistic);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#getOptimistic()
     */
    @Override
    public boolean getOptimistic() {
        return this.transaction.getOptimistic();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#getIsolationLevel()
     */
    @Override
    public String getIsolationLevel() {
        return this.transaction.getIsolationLevel();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#setIsolationLevel(java.lang.String)
     */
    @Override
    public void setIsolationLevel(String level) {
        this.transaction.setIsolationLevel(level);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#setSynchronization(org.openmdx.base.persistence.cci.Synchronization)
     */
    @Override
    public void setSynchronization(Synchronization sync) {
        this.transaction.setSynchronization(
            new TransactionSynchronizationAdapter(sync)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#getSynchronization()
     */
    @Override
    public Synchronization getSynchronization() {
        javax.transaction.Synchronization synchronization = this.transaction.getSynchronization();
        if(synchronization == null) {
            return null;
        } else if (synchronization instanceof TransactionSynchronizationAdapter) {
            return ((TransactionSynchronizationAdapter)synchronization).getDelegate();
        } else {
            throw new JDOUserException(
                "A synchronization object set to javax.jdo.Transaction " +
                "can't be retrieved through org.openmdx.base.persistence.cci.UnitOfWork"
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#getPersistenceManager()
     */
    @Override
    public PersistenceManager getPersistenceManager() {
        return this.transaction.getPersistenceManager();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#setForgetOnly()
     */
    @Override
    public void setForgetOnly() {
        throw new JDOUnsupportedOptionException(
            "The associated persistence manager does not support forget-only"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.UnitOfWork#isForgetOnly()
     */
    @Override
    public boolean isForgetOnly() {
        throw new JDOUnsupportedOptionException(
            "The associated persistence manager does not support forget-only"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.Synchronization#beforeCompletion()
     */
    @Override
    public void beforeCompletion() {
        ((javax.transaction.Synchronization) this.transaction).beforeCompletion();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.Synchronization#afterCompletion(org.openmdx.base.transaction.Status)
     */
    @Override
    public void afterCompletion(Status status) {
        ((javax.transaction.Synchronization) this.transaction).afterCompletion(status.ordinal());
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.UnitOfWork#afterBegin()
     */
    @Override
    public void afterBegin() {
        throw new JDOFatalUserException(
            "The associated persistence manager does not support afterBegin()"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.UnitOfWork#clear()
     */
    @Override
    public void clear() {
        throw new JDOFatalUserException(
            "The associated persistence manager does not support clear()"
        );
    }

}
