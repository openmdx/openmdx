/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Atomikos Transaction Synchronization Registry
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

import java.util.Map;
import java.util.Objects;
import java.util.HashMap;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.openmdx.base.exception.RuntimeServiceException;

/**
 * Atomikos Transaction Synchronization Registry
 * <p>
 * Note:<br>
 * This is just a workaround as Atomikos does not provide a native
 * {@code TransactionSynchronizationRegistr}. Especially the interposed 
 * synchronization is somehow supported but not properly integrated.   
 */
public final class AtomikosTransactionSynchronizationRegistry 
    implements TransactionSynchronizationRegistry
{

    public AtomikosTransactionSynchronizationRegistry(TransactionManager transactionManger) {
		this.transactionManager = transactionManger;
	}

	/**
     * The Atomikos Transaction Manager
     */
    private final TransactionManager transactionManager;
    
    /**
     * The resource registry
     */
    private final Map<Transaction,Map<Object,Object>> resources = new HashMap<>();
    
    /* (non-Javadoc)
     * @see javax.transaction.TransactionSynchronizationRegistry#getResource(java.lang.Object)
     */
    public Object getResource(Object key) {
        return getResources().get(Objects.requireNonNull(key, "Missing object key"));
    }

	private Transaction getTransaction(){
    	if(!isTransactionActive()) {
    		throw new IllegalStateException("No active transaction");
    	}
		try {
			return this.transactionManager.getTransaction();
		} catch (SystemException e) {
			throw new RuntimeException(e);
		}
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
    public String getTransactionKey() {
		return isTransactionActive() ? getTransaction().toString() : null;
    }

    /* (non-Javadoc)
     * @see javax.transaction.TransactionSynchronizationRegistry#getTransactionStatus()
     */
    public int getTransactionStatus() {
    	try {
			return transactionManager.getStatus();
		} catch (SystemException e) {
			throw new RuntimeServiceException(e);
		}
    }
    
    private boolean isTransactionActive(){
    	return getTransactionStatus()!= Status.STATUS_NO_TRANSACTION;
    }

    /* (non-Javadoc)
     * @see javax.transaction.TransactionSynchronizationRegistry#putResource(java.lang.Object, java.lang.Object)
     */
    public void putResource(
        Object key, 
        Object value
    ) {
        getResources().put(Objects.requireNonNull(key, "Missing object key"), value);
    }

	private Map<Object, Object> getResources() {
		return getResources(getTransaction());
	}

	protected Map<Object, Object> getResources(final Transaction transaction) {
		return this.resources.computeIfAbsent(transaction, t -> new HashMap<>());
	}

    /* (non-Javadoc)
     * @see javax.transaction.TransactionSynchronizationRegistry#registerInterposedSynchronization(javax.transaction.Synchronization)
     */
    public void registerInterposedSynchronization(Synchronization sync) {
    	AtomikosSynchronization atomikosSynchronization = (AtomikosSynchronization) getResources().get(AtomikosSynchronization.class);
    	atomikosSynchronization.setInterposedSynchronizatio(sync);
    }

    /* (non-Javadoc)
     * @see javax.transaction.TransactionSynchronizationRegistry#setRollbackOnly()
     */
    public void setRollbackOnly(
    ) {
    	try {
			this.transactionManager.setRollbackOnly();
		} catch (IllegalStateException | SystemException e) {
			throw new RuntimeException();
		}
    }
    
    public void enlist() throws SystemException {
    	try {
			new AtomikosSynchronization(getTransaction());
		} catch (IllegalStateException | RollbackException e) {
			throw new SystemException("TransactionSynchronizationRegistry workaround failure");
		}
    }

    /**
     * Try to enlist first in order to be called back last
     */
    private class AtomikosSynchronization implements Synchronization {
    	
		AtomikosSynchronization(
			Transaction transaction
		) throws IllegalStateException, RollbackException, SystemException {
			this.transaction = transaction;
			enlist();
		}
		
    	private final Transaction transaction;
    	
    	/**
    	 * As interposeed synchronization is not yet supported by Atomikos
    	 * we do our best to be the first to registered and the last to be 
    	 * called
    	 */
    	private Synchronization interposedSynchronization;
		
    	void setInterposedSynchronizatio(Synchronization synchronization) {
    		this.interposedSynchronization = synchronization;
    	}
    	
		private void enlist() throws RollbackException, SystemException {
			this.transaction.registerSynchronization(this);
			getResources(this.transaction).put(AtomikosSynchronization.class, this);
		}
    	
		@Override
		public void beforeCompletion() {
			if(this.interposedSynchronization != null) {
				this.interposedSynchronization.beforeCompletion();
			}
		}

		@Override
		public void afterCompletion(int status) {
			if(this.interposedSynchronization != null) {
				this.interposedSynchronization.afterCompletion(status);
			}
			resources.remove(this.transaction).clear();
		}

    }
    
}