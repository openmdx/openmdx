/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Lightweight Transaction Manager
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.lightweight.transaction;

import java.util.logging.Level;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.openmdx.kernel.log.SysLog;

/**
 * JTA Transaction manager implementation.
 * <p>
 * Implementation notes :
 * <ul>
 * <li>Does not support nested transactions</li>
 * <li>No security</li>
 * </ul>
 * 
 * @deprecated in favour of Atomikos' transaction manager
 */
@Deprecated
public final class LightweightTransactionManager implements TransactionManager {

    /**
     * Constructor 
     */
    private LightweightTransactionManager() {
        super();
    }

    /**
     * The transactions bound to a given thread
     */
    private final ThreadLocal<LightweightTransaction> currentTransactions = new ThreadLocal<LightweightTransaction>();

    /**
     * The transaction id factory singleton
     */
    private final TransactionIdFactory transactionIdFactory = new TransactionIdFactory();

    private final ThreadLocal<Integer> timeouts = new ThreadLocal<Integer>(){

		/* (non-Javadoc)
		 * @see java.lang.ThreadLocal#initialValue()
		 */
		@Override
		protected Integer initialValue() {
			return Integer.valueOf(0);
		}
    	
    };
    
    /**
     * The transaction manager must be a singleton
     */
    private static final LightweightTransactionManager instance = new LightweightTransactionManager();
    
    /**
     * The transaction manager must be a singleton
     * 
     * @return the transaction manager singleton
     */
    public static synchronized LightweightTransactionManager getInstance(
    ){
        return instance;
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
    public void begin(
    ) throws NotSupportedException, SystemException {
        if(currentTransactions.get() != null) throw new NotSupportedException(
            "There is already an active transaction, nested transactions are not supported"
        );        
        LightweightTransaction currentTransaction = new LightweightTransaction(
            this.transactionIdFactory
        );
        SysLog.log(Level.FINEST,"Begin {0}", currentTransaction);
        currentTransaction.setTimeout(timeouts.get().intValue());
        currentTransactions.set(currentTransaction);
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
        final Transaction currentTransaction = getTransaction(true); // assert that a transaction is active
        try {
            SysLog.log(Level.FINEST,"Commit {0}", currentTransaction);
            currentTransaction.commit();
        } finally {
            currentTransactions.remove();
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
    public void rollback(
    ) throws SecurityException, IllegalStateException, SystemException {
        final Transaction currentTransaction = getTransaction(true); // assert that a transaction is active
        try {
            SysLog.log(Level.FINEST,"Rollback {0}", currentTransaction);
            currentTransaction.rollback();
        } finally {
            currentTransactions.remove();
        }
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
    public void setRollbackOnly(
    ) throws IllegalStateException, SystemException {
        final Transaction currentTransaction = getTransaction(true); // assert that a transaction is active
        SysLog.log(Level.INFO,"Set {0} to rollback-only", currentTransaction);
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
    public int getStatus(
    ) throws SystemException {
        final Transaction currentTransaction = currentTransactions.get();
        return currentTransaction == null ? Status.STATUS_NO_TRANSACTION : currentTransaction.getStatus();
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
    public LightweightTransaction getTransaction(
    ){
        return currentTransactions.get();
    }

    /**
     * Get the transaction object that represents the transaction context of
     * the calling thread.
     * 
     * @param exists tells whether the transaction must exit or not
     * 
     * @return the transaction object
     * 
     * @exception IllegalStateException if there is no active transaction when one is expected
     * @exception NotSupportedException in case of a nested transaction attempt
     */
    LightweightTransaction getTransaction(
        boolean exists
    ){
        final LightweightTransaction transaction = currentTransactions.get();
        if(exists != (transaction != null)) {
            throw new IllegalStateException(
                exists ? "There is no active transaction" : "The current transaction is still active"
            );
        }
        return transaction;
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
    public void resume(
        Transaction tobj
    ) throws InvalidTransactionException, IllegalStateException, SystemException {
        getTransaction(false); // assert that no transaction is active
        if (!(tobj instanceof LightweightTransaction)) {
            throw new InvalidTransactionException();
        }
        currentTransactions.set((LightweightTransaction) tobj);
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
    public Transaction suspend(
    ) throws SystemException {
        final LightweightTransaction currentTransaction = currentTransactions.get();
        currentTransactions.remove();
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
    public void setTransactionTimeout(
        int seconds
    ) throws SystemException {
    	timeouts.set(Integer.valueOf(seconds));
    }

}
