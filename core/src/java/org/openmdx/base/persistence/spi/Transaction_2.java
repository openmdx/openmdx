/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Transaction_2.java,v 1.5 2008/05/30 18:21:05 hburger Exp $
 * Description: Unit Of Work
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/30 18:21:05 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.persistence.spi;

import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.JDOFatalInternalException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOOptimisticVerificationException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.transaction.Synchronization_1_0;
import org.openmdx.kernel.exception.BasicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit Of Work
 * 
 * @since openMDX 2.0
 */
class Transaction_2 
    implements Transaction, Synchronization 
{

    /**
     * Constructor 
     */
    Transaction_2(
        PersistenceManager persistenceManager,
        OptimisticTransaction_2_0 optimisticTransaction
    ) {
        this.persistenceManager = persistenceManager;
        this.optimisticTransaction = optimisticTransaction;
    }

    /**
     * 
     */
    private final Logger logger = LoggerFactory.getLogger(Transaction_2.class);
    
    /**
     * 
     */
    private final PersistenceManager persistenceManager;
    
    /**
     * 
     */
    private final OptimisticTransaction_2_0 optimisticTransaction;
    
    /**
     * 
     */
    private Synchronization synchronization = null;
    
    /**
     * 
     */
    private UserTransaction userTransaction = null;
    
    /**
     * Retrieve the user transaction 
     * 
     * @return the user transaction 
     */
    private UserTransaction getUserTransaction(){
        if(this.userTransaction == null) this.userTransaction = ComponentEnvironment.lookup(
            UserTransaction.class
        );
        return this.userTransaction;
    }
    
    
    //--------------------------------------------------------------------
    // Implements Transaction
    //--------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#begin()
     */
    public void begin() {
        if(isActive()) throw new JDOUserException(
            "The transaction is already active"
        );
        if(!getOptimistic()) try {
            getUserTransaction().begin();
        } catch (NotSupportedException exception) {
            throw new JDOUserException(
                "The transaction could not be started",
                exception
            );
        } catch (SystemException exception) {
            throw new JDOFatalInternalException(
                "The transaction could not be started",
                exception
            );
        }
        Transaction delegate = this.persistenceManager.currentTransaction();
        if(persistenceManager.getMultithreaded()) {
            delegate.begin();
        } else if(delegate instanceof Synchronization_1_0) try {
            ((Synchronization_1_0)delegate).afterBegin();
        } catch (ServiceException exception) {
            throw new JDOFatalInternalException(
                "Could not enlist the persistence manager",
                exception.getCause()
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#commit()
     */
    public void commit() {
        if(!isActive()) throw new JDOUserException(
            "The transaction is not active"
        );
        int status = Status.STATUS_UNKNOWN;
        try {
            if(getRollbackOnly()) {
                status = Status.STATUS_ROLLEDBACK;
                throw new JDOFatalDataStoreException(
                    "Transaction is marked for rollback only"
                );
            }
            if(getOptimistic()) {
                try {
                    status = Status.STATUS_PREPARING;
                    this.optimisticTransaction.commit(this);
                    status = Status.STATUS_COMMITTED;
                } catch (ServiceException exception) {
                    status = exception.getExceptionCode() == BasicException.Code.ROLLBACK ?
                        Status.STATUS_ROLLEDBACK :
                        Status.STATUS_NO_TRANSACTION;
                    throw exception.getExceptionStack().getCause(null).getExceptionCode() == BasicException.Code.CONCURRENT_ACCESS_FAILURE ? new JDOOptimisticVerificationException(
                        "Commit failed",
                        new Throwable[]{exception.getCause()}
                    ) : new JDOFatalDataStoreException(
                        "Commit failed",
                        exception.getCause()
                    );
                }
            } else {
                beforeCompletion();
                try {
                    getUserTransaction().commit();
                    status = Status.STATUS_COMMITTED;
                } catch (SecurityException exception) {
                    throw new JDOFatalInternalException(
                        "Commit failed",
                        exception
                    );
                } catch (IllegalStateException exception) {
                    status = Status.STATUS_NO_TRANSACTION;
                    throw new JDOFatalUserException(
                        "Commit failed",
                        exception
                    );
                } catch (RollbackException exception) {
                    status = Status.STATUS_ROLLEDBACK;
                    throw new JDOFatalDataStoreException(
                        "Commit failed",
                        exception
                    );
                } catch (HeuristicMixedException exception) {
                    status = Status.STATUS_NO_TRANSACTION;
                    throw new JDOFatalDataStoreException(
                        "Commit failed",
                        exception
                    );
                } catch (HeuristicRollbackException exception) {
                    status = Status.STATUS_ROLLEDBACK;
                    throw new JDOFatalDataStoreException(
                        "Commit failed",
                        exception
                    );
                } catch (SystemException exception) {
                    throw new JDOFatalInternalException(
                        "Commit failed",
                        exception
                    );
                }
            }
        } finally {
            afterCompletion(status);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#rollback()
     */
    public void rollback() {
        if(!isActive()) throw new JDOUserException(
            "The transaction is not active"
        );
        int status = Status.STATUS_UNKNOWN;
        try {
            if(getOptimistic()) {
                status = Status.STATUS_ROLLEDBACK;
            } else {
                getUserTransaction().rollback();
                status = Status.STATUS_ROLLEDBACK;
            }
        } catch (IllegalStateException exception) {
            status = Status.STATUS_NO_TRANSACTION;
            throw new JDOFatalDataStoreException(
                "Rollback failed",
                exception
            );
        } catch (SecurityException exception) {
            throw new JDOFatalInternalException(
                "Rollback failed",
                exception
            );
        } catch (SystemException exception) {
            throw new JDOFatalInternalException(
                "Rollback failed",
                exception
            );
        } finally {
            afterCompletion(status);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#isActive()
     */
    public boolean isActive(
    ){
        return this.persistenceManager.currentTransaction().isActive();
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getRollbackOnly()
     */
    public boolean getRollbackOnly() {
        return isActive() && this.persistenceManager.currentTransaction().getRollbackOnly();
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setRollbackOnly()
     */
    public void setRollbackOnly() {
        if(isActive()) {
            this.persistenceManager.currentTransaction().setRollbackOnly();
        } else {
            throw new JDOUserException(
                "The transaction is not active"
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setNontransactionalRead(boolean)
     */
    public void setNontransactionalRead(
        boolean nontransactionalRead
    ) {
        this.persistenceManager.currentTransaction().setNontransactionalRead(nontransactionalRead);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getNontransactionalRead()
     */
    public boolean getNontransactionalRead() {
        return this.persistenceManager.currentTransaction().getNontransactionalRead();
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setNontransactionalWrite(boolean)
     */
    public void setNontransactionalWrite(
        boolean nontransactionalWrite
    ) {
        this.persistenceManager.currentTransaction().setNontransactionalWrite(nontransactionalWrite);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getNontransactionalWrite()
     */
    public boolean getNontransactionalWrite() {
        return this.persistenceManager.currentTransaction().getNontransactionalWrite();
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setRetainValues(boolean)
     */
    public void setRetainValues(
        boolean retainValues
    ) {
        this.persistenceManager.currentTransaction().setRetainValues(retainValues);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getRetainValues()
     */
    public boolean getRetainValues() {
        return this.persistenceManager.currentTransaction().getRetainValues();
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setRestoreValues(boolean)
     */
    public void setRestoreValues(
        boolean restoreValues
    ) {
        this.persistenceManager.currentTransaction().setRestoreValues(restoreValues);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getRestoreValues()
     */
    public boolean getRestoreValues() {
        return this.persistenceManager.currentTransaction().getRestoreValues();
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setOptimistic(boolean)
     */
    public void setOptimistic(boolean optimistic) {
        this.persistenceManager.currentTransaction().setOptimistic(optimistic);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getOptimistic()
     */
    public boolean getOptimistic() {
        return this.persistenceManager.currentTransaction().getOptimistic();
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setSynchronization(javax.transaction.Synchronization)
     */
    public void setSynchronization(Synchronization sync) {
        this.synchronization = sync;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getSynchronization()
     */
    public Synchronization getSynchronization(
    ){
        return this.synchronization;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getPersistenceManager()
     */
    public PersistenceManager getPersistenceManager(
    ) {
        return this.persistenceManager;
    }

    
    //--------------------------------------------------------------------
    // Implements Synchronization
    //--------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#afterCompletion(int)
     */
    public void afterCompletion(
        int status
    ) {
        if(this.synchronization != null) try {
            this.synchronization.afterCompletion(status);
        } catch (RuntimeException exception) {
            logger.error("afterCompletion() failure", exception);
        }
        if(this.persistenceManager instanceof Synchronization) try {
            ((Synchronization)this.persistenceManager).afterCompletion(status);
        } catch (RuntimeException exception) {
            logger.error("afterCompletion() failure", exception);
        }
        Transaction delegate = this.persistenceManager.currentTransaction();
        if(delegate instanceof Synchronization) try {
            ((Synchronization)delegate).afterCompletion(status);
        } catch (RuntimeException exception) {
            logger.error("afterCompletion() failure", exception);
        }
    }

    /* (non-Javadoc)
     * @see javax.transaction.Synchronization#beforeCompletion()
     */
    public void beforeCompletion() {
        if(getRollbackOnly()) return;
        if(this.persistenceManager instanceof Synchronization) {
            ((Synchronization)this.persistenceManager).beforeCompletion();
        }
        if(this.synchronization != null) {
            this.synchronization.beforeCompletion();
        }
        Transaction delegate = this.persistenceManager.currentTransaction();
        if(delegate instanceof Synchronization) {
            ((Synchronization)delegate).beforeCompletion();
        }
    }
    
}