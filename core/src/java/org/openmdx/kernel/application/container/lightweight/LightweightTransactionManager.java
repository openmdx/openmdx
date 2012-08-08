/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LightweightTransactionManager.java,v 1.7 2008/06/28 00:21:36 hburger Exp $
 * Description: Lightweight Transaction Manager
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/28 00:21:36 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */

package org.openmdx.kernel.application.container.lightweight;

import java.util.Hashtable;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.openmdx.kernel.application.container.transaction.TransactionIdFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JTA Transaction manager implementation.
 * <p>
 * Implementation notes :
 * <ul>
 * <li>Does not support nested transactions</li>
 * <li>No security</li>
 * </ul>
 */
final class LightweightTransactionManager implements TransactionManager {


    // -------------------------------------------------------------- Constants

    public static final int DEFAULT_TRANSACTION_TIMEOUT = 30;


    // ------------------------------------------------------------ Constructor


    // ----------------------------------------------------- Instance Variables

    /**
     * The logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(
        LightweightTransactionManager.class
    );
    
    /**
     * Transaction bindings thread id <-> transaction object.
     */
    final Hashtable<Thread, LightweightTransaction> bindings = 
        new Hashtable<Thread, LightweightTransaction>();


    /**
     * Transaction bindings thread id <-> transaction timeout.
     */
    private final Hashtable<Thread,Integer> timeouts = new Hashtable<Thread,Integer>();

    /**
     * 
     */
    private final TransactionIdFactory transactionIdFactory = new TransactionIdFactory();

    /**
     * The transaction synchronization registry
     */
    private final TransactionSynchronizationRegistry transactionSynchronizationRegistry =
        new LightweightTransactionSynchronizationRegistry();
    
    // ------------------------------------------------------------- Properties


    // --------------------------------------------------------- Public Methods

    public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry(
    ){
        return this.transactionSynchronizationRegistry;
    }
    
    // --------------------------------------------- TransactionManager Methods

    /**
     * Create a new transaction and associate it with the current thread.
     *
     * @exception NotSupportedException Thrown if the thread is already
     * associated with a transaction and the Transaction Manager
     * implementation does not support nested transactions.
     * @exception SystemException Thrown if the transaction manager encounters
     * an unexpected error condition.
     */
    public void begin()
        throws NotSupportedException, SystemException {

        if (getTransaction() != null) throw new NotSupportedException(
            "Nested transactions are not supported"
         );

        LightweightTransaction currentTransaction = new LightweightTransaction(
            this.transactionIdFactory
        );
        bindings.put(Thread.currentThread(), currentTransaction);

        this.logger.trace("Begin {}", currentTransaction);

    }


    /**
     * Complete the transaction associated with the current thread. When this
     * method completes, the thread becomes associated with no transaction.
     * If the commit is terminated with an exception, the rollback should be
     * called, to do a proper clean-up.
     *
     * @exception RollbackException Thrown to indicate that the transaction
     * has been rolled back rather than committed.
     * @exception HeuristicMixedException Thrown to indicate that a heuristic
     * decision was made and that some relevant updates have been committed
     * while others have been rolled back.
     * @exception HeuristicRollbackException Thrown to indicate that a
     * heuristic decision was made and that some relevant updates have been
     * rolled back.
     * @exception SecurityException Thrown to indicate that the thread is not
     * allowed to commit the transaction.
     * @exception IllegalStateException Thrown if the current thread is not
     * associated with a transaction.
     * @exception SystemException Thrown if the transaction manager encounters
     * an unexpected error condition.
     */
    public void commit()
        throws RollbackException, HeuristicMixedException,
        HeuristicRollbackException, SecurityException, IllegalStateException,
        SystemException {

        Thread currentThread = Thread.currentThread();
        Transaction currentTransaction = bindings.get(currentThread);
        if (currentTransaction == null)
            throw new IllegalStateException();


        this.logger.trace("Commit {}", currentTransaction);
        try {
            currentTransaction.commit();
        } finally {
            timeouts.remove(currentThread);
            bindings.remove(currentThread);
        }

    }


    /**
     * Roll back the transaction associated with the current thread. When
     * this method completes, the thread becomes associated with no
     * transaction.
     *
     * @exception SecurityException Thrown to indicate that the thread is not
     * allowed to commit the transaction.
     * @exception IllegalStateException Thrown if the current thread is not
     * associated with a transaction.
     * @exception SystemException Thrown if the transaction manager encounters
     * an unexpected error condition.
     */
    public void rollback()
        throws SecurityException, IllegalStateException, SystemException {

        Thread currentThread = Thread.currentThread();
        Transaction currentTransaction = bindings.remove(currentThread);
        if (currentTransaction == null)
            throw new IllegalStateException();

        timeouts.remove(currentThread);

        this.logger.trace("Rollback {}", currentTransaction);
        currentTransaction.rollback();

    }


    /**
     * Modify the transaction associated with the current thread such that
     * the only possible outcome of the transaction is to roll back the
     * transaction.
     *
     * @exception IllegalStateException Thrown if the current thread is not
     * associated with a transaction.
     * @exception SystemException Thrown if the transaction manager encounters
     * an unexpected error condition.
     */
    public void setRollbackOnly()
        throws IllegalStateException, SystemException {

        Transaction currentTransaction = getTransaction();
        if (currentTransaction == null)
            throw new IllegalStateException();
        this.logger.info("Set {} to rollback-only", currentTransaction);
        currentTransaction.setRollbackOnly();
    }


    /**
     * Obtain the status of the transaction associated with the current thread.
     *
     * @exception SystemException Thrown if the transaction manager encounters
     * an unexpected error condition.
     * @return The transaction status. If no transaction is associated with
     * the current thread, this method returns the Status.NoTransaction value.
     */
    public int getStatus()
        throws SystemException {

        Transaction currentTransaction = getTransaction();
        return currentTransaction == null ? 
            Status.STATUS_NO_TRANSACTION : 
            currentTransaction.getStatus();
    }


    /**
     * Get the transaction object that represents the transaction context of
     * the calling thread.
     *
     * @return the Transaction object representing the transaction associated
     * with the calling thread.
     * @exception SystemException Thrown if the transaction manager encounters
     * an unexpected error condition.
     */
    public Transaction getTransaction(
    ) throws SystemException {
        return bindings.get(Thread.currentThread());
    }


    /**
     * Resume the transaction context association of the calling thread with
     * the transaction represented by the supplied Transaction object. When
     * this method returns, the calling thread is associated with the
     * transaction context specified.
     *
     * @param tobj The Transaction object that represents the transaction to
     * be resumed.
     * @exception InvalidTransactionException Thrown if the parameter
     * transaction object contains an invalid transaction.
     * @exception IllegalStateException Thrown if the thread is already
     * associated with another transaction.
     * @exception SystemException Thrown if the transaction manager encounters
     * an unexpected error condition.
     */
    public void resume(Transaction tobj)
        throws InvalidTransactionException, IllegalStateException,
        SystemException {

        if (getTransaction() != null)
            throw new IllegalStateException();

        if (tobj instanceof LightweightTransaction) {
            bindings.put(Thread.currentThread(), (LightweightTransaction) tobj);
        } else  throw new InvalidTransactionException();


    }


    /**
     * Suspend the transaction currently associated with the calling thread
     * and return a Transaction object that represents the transaction
     * context being suspended. If the calling thread is not associated with
     * a transaction, the method returns a null object reference. When this
     * method returns, the calling thread is associated with no transaction.
     *
     * @return Transaction object representing the suspended transaction.
     * @exception SystemException Thrown if the transaction manager encounters
     * an unexpected error condition.
     */
    public Transaction suspend()
        throws SystemException {

        Transaction currentTransaction = getTransaction();

        if (currentTransaction != null) {
            Thread currentThread = Thread.currentThread();
            bindings.remove(currentThread);
            timeouts.remove(currentThread);
        }

        return currentTransaction;

    }


    /**
     * Modify the value of the timeout value that is associated with the
     * transactions started by the current thread with the begin method.
     * <p>
     * If an application has not called this method, the transaction service
     * uses some default value for the transaction timeout.
     *
     * @param seconds The value of the timeout in seconds. If the value is
     * zero, the transaction service restores the default value.
     * @exception SystemException Thrown if the transaction manager encounters
     * an unexpected error condition.
     */
    public void setTransactionTimeout(int seconds)
        throws SystemException {

        timeouts.put(Thread.currentThread(), new Integer(seconds));

    }


    //------------------------------------------------------------------------
    // Class LightweightTransactionSynchronizationRegistry
    //------------------------------------------------------------------------
    
    /**
     * Lightweight Transaction Synchronization Registry
     */
    final class LightweightTransactionSynchronizationRegistry 
        implements TransactionSynchronizationRegistry
    {

        private LightweightTransaction getTransaction(){
            LightweightTransaction transaction = bindings.get(Thread.currentThread());
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

}
