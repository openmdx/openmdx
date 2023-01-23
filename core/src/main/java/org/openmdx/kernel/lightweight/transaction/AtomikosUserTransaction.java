/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Atomikos User Transaction
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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * User Transaction Implementation
 * <p>
 * Note:<br>
 * This is just a workaround as Atomikos does not provide a native
 * {@code TransactionSynchronizationRegistr}. Especially the interposed 
 * synchronization is somehow supported but not properly integrated.   
 */
public class AtomikosUserTransaction implements UserTransaction {

	/**
     * Constructor
     * 
	 * @param transactionManager Atomikos' UserTransaction
	 * @param transactionSynchronizationRegistry openMDX' TransactionSynchronizationRegistry workaround
	 */
	public AtomikosUserTransaction(
        UserTransaction delegate,
        AtomikosTransactionSynchronizationRegistry transactionSynchronizationRegistry
    ){
        this.delegate = delegate;
        this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
	}
    
    /**
     * The user transaction to delegate to.
     */
    private final UserTransaction delegate;    

    /**
     * The user transaction to delegate to.
     */
    private final AtomikosTransactionSynchronizationRegistry transactionSynchronizationRegistry;    
    
    /* (non-Javadoc)
	 * @see javax.transaction.UserTransaction#begin()
	 */
	public void begin() throws NotSupportedException, SystemException {
        this.delegate.begin();
		transactionSynchronizationRegistry.enlist();
	}

	/* (non-Javadoc)
	 * @see javax.transaction.UserTransaction#commit()
	 */
	public void commit(
	) throws RollbackException, HeuristicMixedException,
			HeuristicRollbackException, SecurityException,
			IllegalStateException, SystemException 
	{
        this.delegate.commit();
	}

	/* (non-Javadoc)
	 * @see javax.transaction.UserTransaction#rollback()
	 */
	public void rollback(
	) throws IllegalStateException, SecurityException, SystemException {
        this.delegate.rollback();
	}

	/* (non-Javadoc)
	 * @see javax.transaction.UserTransaction#setRollbackOnly()
	 */
	public void setRollbackOnly() throws IllegalStateException, SystemException {
        this.delegate.setRollbackOnly();
	}

	/* (non-Javadoc)
	 * @see javax.transaction.UserTransaction#getStatus()
	 */
	public int getStatus() throws SystemException {
        return this.delegate.getStatus();
	}

	/* (non-Javadoc)
	 * @see javax.transaction.UserTransaction#setTransactionTimeout(int)
	 */
	public void setTransactionTimeout(int timeout) throws SystemException {
        this.delegate.setTransactionTimeout(timeout);
	}

}
