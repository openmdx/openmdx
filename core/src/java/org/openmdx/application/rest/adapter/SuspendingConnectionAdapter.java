/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: REST Connection Adapter
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2013, OMEX AG, Switzerland
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
package org.openmdx.application.rest.adapter;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.Record;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.LocalTransactionException;
import javax.resource.spi.ResourceAllocationException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.openmdx.application.transaction.TransactionManagerFactory;
import org.openmdx.application.transaction.UserTransactions;
import org.openmdx.base.resource.spi.AbstractInteraction;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.base.rest.spi.ConnectionAdapter;
import org.openmdx.base.rest.spi.RestConnectionFactory;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.naming.ComponentEnvironment;

/**
 * Wraps a REST Port into a JCA Connection
 * <p>
 * This implementation is restricted to the transaction attribute REQUIRES_NEW.
 */
class SuspendingConnectionAdapter extends ConnectionAdapter {

	/**
	 * Constructor 
	 */
    SuspendingConnectionAdapter(
        RestConnectionFactory connectionFactory, 
        RestConnectionSpec connectionSpec
    ){
        super(
            connectionFactory, 
            connectionSpec
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.ConnectionAdapter#newTransactionProxy(boolean)
     */
    @Override
    protected LocalTransaction newTransactionProxy(
    ) {
        return new LocalTransactionManagerAdapter();
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Connection#createInteraction()
     */
    @Override
    public Interaction createInteraction(
    ) throws ResourceException {
        this.assertOpen();
        return new SuspendingInteractionAdapter(this.getDelegate());
    }
   
    protected LocalTransactionManager getLocalTransactionManager(
    ) throws ResourceException {
        return (LocalTransactionManager) super.getLocalTransaction();
    }

    static {
        ComponentEnvironment.register(new TransactionManagerFactory());
    }

    
    //------------------------------------------------------------------------
    // Interface Transaction Manager
    //------------------------------------------------------------------------

    /**
     * Local Transaction Manager
     */
    static interface LocalTransactionManager extends LocalTransaction {

        /**
         * Suspend the current transaction and return it
         * 
         * @return the suspended transaction
         * 
         * @throws ResourceException 
         */
        Transaction suspendCurrentTransaction(
        ) throws ResourceException;
        
        /**
         * Resume the suspended transaction
         * 
         * @param transaction
         * 
         * @throws ResourceException
         */
        void resumeSuspendedTransaction(
            Transaction transaction
        ) throws ResourceException;    
    }

    
    //------------------------------------------------------------------------
    // Class LocalTransactionManagerAdapter
    //------------------------------------------------------------------------
    
    /**
     * Local Transaction Manager Adapter
     */
    static class LocalTransactionManagerAdapter implements LocalTransactionManager {

        /**
         * The lazily acquired transaction manager
         */
        private TransactionManager transactionManager;

        /**
         * The user transaction is lazily acquired when no transaction manager has been acquired before
         */
        private UserTransaction userTransaction;

        /**
         * Acquire the <code>TransactionManager</code>
         * 
         * @return the <code>TransactionManager</code>
         * 
         * @exception ResourceException
         */
        private TransactionManager getTransactionManager(
        ) throws ResourceException {
            if(this.transactionManager == null) {
                try {
                    this.transactionManager = ComponentEnvironment.lookup(TransactionManager.class);
                } catch (BasicException exception) {
                	throw ResourceExceptions.initHolder(
	                    new ResourceAllocationException(
	                        "REQUIRES_NEW requires a TransactionManager which could not be acquired",
	                        BasicException.newEmbeddedExceptionStack(exception)
	                    )
	                );
                }
            }
            return this.transactionManager;
        }

        /**
         * Tells whether the transaction manager or a user transaction shall be used
         * 
         * @return <code>true</code> if the transaction manager shall be used
         */
        private boolean hasTransactionManager(
        ){
            return this.transactionManager != null;
        }

        private UserTransaction getUserTransaction(
        ) throws ResourceException{
            if(this.userTransaction == null) {
                this.userTransaction = UserTransactions.getUserTransaction();
            }
            return this.userTransaction;
        }

        /* (non-Javadoc)
         * @see javax.resource.spi.LocalTransaction#begin()
         */
        @Override
        public void begin(
        ) throws ResourceException {
            try {
                if(hasTransactionManager()) {
                    getTransactionManager().begin();
                } else {
                    getUserTransaction().begin();
                }
            } catch (SystemException exception) {
            	throw ResourceExceptions.initHolder(
                    new javax.resource.spi.EISSystemException(
	                    "Unable to start the current transaction",
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            } catch (javax.transaction.NotSupportedException exception) {
            	throw ResourceExceptions.initHolder(
                    new javax.resource.NotSupportedException(
	                    "Unable to start the current transaction",
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            }
        }

        /* (non-Javadoc)
         * @see javax.resource.spi.LocalTransaction#commit()
         */
        @Override
        public void commit(
        ) throws ResourceException {
            try {
                if(hasTransactionManager()) {
                    getTransactionManager().commit();
                } else {
                    getUserTransaction().commit();
                }
            } catch (SystemException exception) {
            	throw ResourceExceptions.initHolder(
                    new javax.resource.spi.EISSystemException(
	                    "Unable to commit the current transaction",
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            } catch (SecurityException exception) {
            	throw ResourceExceptions.initHolder(
                    new javax.resource.spi.SecurityException(
	                    "Unable to commit the current transaction",
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            } catch (IllegalStateException exception) {
            	throw ResourceExceptions.initHolder(
                    new javax.resource.spi.IllegalStateException(
	                    "Unable to commit the current transaction",
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            } catch (RollbackException exception) {
            	throw ResourceExceptions.initHolder(
                    new LocalTransactionException(
	                    "Unable to commit the current transaction",
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            } catch (HeuristicMixedException exception) {
            	throw ResourceExceptions.initHolder(
                    new LocalTransactionException(
	                    "Unable to commit the current transaction",
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            } catch (HeuristicRollbackException exception) {
            	throw ResourceExceptions.initHolder(
                    new LocalTransactionException(
	                    "Unable to commit the current transaction",
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            }
        }

        /* (non-Javadoc)
         * @see javax.resource.spi.LocalTransaction#rollback()
         */
        @Override
        public void rollback(
        ) throws ResourceException {
            try {
                if(hasTransactionManager()) {
                    getTransactionManager().rollback();
                } else {
                    getUserTransaction().rollback();
                }
            } catch (SystemException exception) {
            	throw ResourceExceptions.initHolder(
                    new EISSystemException(
                        "Unable to roll back the current transaction",
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.adapter.TransactionManager_2_0#suspendCurrentTransaction()
         */
        @Override
        public Transaction suspendCurrentTransaction(
        ) throws ResourceException {
            try {
                return getTransactionManager().suspend();
            } catch (SystemException exception) {
            	throw ResourceExceptions.initHolder(
                    new EISSystemException(
                        "Unable to suspend the current transaction",
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.adapter.TransactionManager_2_0#resumeSuspendedTransaction(javax.transaction.Transaction)
         */
        @Override
        public void resumeSuspendedTransaction(
        	Transaction transaction
        ) throws ResourceException {
            if(transaction != null) {
                try {
                    getTransactionManager().resume(transaction);
                } catch (SystemException exception) {
                	throw ResourceExceptions.initHolder(
	                    new EISSystemException(
	                        "Unable to resume the suspended transaction",
	                        BasicException.newEmbeddedExceptionStack(exception)
	                    )
	                );
                } catch (InvalidTransactionException exception) {
                	throw ResourceExceptions.initHolder(
	                    new LocalTransactionException(
	                        "Unable to resume the suspended transaction",
	                        BasicException.newEmbeddedExceptionStack(exception)
	                    )
	                );
                } catch (IllegalStateException exception) {
                	throw ResourceExceptions.initHolder(
	                    new javax.resource.spi.IllegalStateException(
                        "Unable to resume the given transaction",
	                        BasicException.newEmbeddedExceptionStack(exception)
	                    )
	                );
                }
            }
        }
    }    
    
    
    //------------------------------------------------------------------------
    // Class InteractionAdapter
    //------------------------------------------------------------------------
    
    /**
     * Interaction Adapter
     */
    class SuspendingInteractionAdapter extends AbstractInteraction<Connection> {

        /**
         * Constructor 
         *
         * @param restInteraction
         */
        SuspendingInteractionAdapter(
            Interaction restInteraction
        ){
            super(SuspendingConnectionAdapter.this);
            this.delegate = restInteraction;
        }
        
        /**
         * The interaction's connection
         */
        private final Interaction delegate;
        
        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
         */
        private <T> T execute(
            Class<T> returnType,
            InteractionSpec ispec, 
            Record input,
            Record output
        ) throws ResourceException {
            this.assertOpened();
            Transaction suspendedTransaction = getLocalTransactionManager().suspendCurrentTransaction();
            try {
                RuntimeException error = null;
                ResourceException failure = null;
                T reply = null;
                getLocalTransactionManager().begin();
                try {
                    reply = returnType.cast(
                        Boolean.class == returnType ? Boolean.valueOf(
                            this.delegate.execute(ispec, input, output)
                        ) : this.delegate.execute(ispec, input)
                    );
                } catch (ResourceException exception){
                    failure = exception;
                } catch (RuntimeException exception) {
                    error = exception;
                }
                if(error == null) {
                    getLocalTransactionManager().commit();
                } else {
                    try {
                        getLocalTransactionManager().rollback();
                    } catch (ResourceException exception) {
                        SysLog.warning(
                            "Rollback caused by RuntimeException failed, only rollback exception is propagated", 
                            error
                        );
                        throw exception;
                    }
                    throw ResourceExceptions.initHolder(
                        new LocalTransactionException(
                        	"Runtime exception lead to rollback: " + error.getMessage(),
	                        BasicException.newEmbeddedExceptionStack(
		                        error, 
		                        BasicException.Code.DEFAULT_DOMAIN,
		                        BasicException.Code.ROLLBACK
		                     )
		                 )
                     );
                }
                if(failure == null) {
                    return reply;
                } else {
                    throw failure;
                }
            } finally {
                getLocalTransactionManager().resumeSuspendedTransaction(suspendedTransaction);
            }
        }
        
        /* (non-Javadoc)
         * @see javax.resource.cci.Interaction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
         */
        @Override
        public Record execute(
            InteractionSpec ispec, 
            Record input
        ) throws ResourceException {
            return execute(
                Record.class,
                ispec,
                input,
                null
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.resource.spi.AbstractInteraction#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
         */
        @Override
        public boolean execute(
            InteractionSpec ispec,
            Record input,
            Record output
        ) throws ResourceException {
            return execute(
                Boolean.class,
                ispec,
                input,
                output
            ).booleanValue();
        }

    }

}