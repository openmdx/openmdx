/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Unit Of Work
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2012, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.Constants;
import javax.jdo.JDODataStoreException;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.JDOOptimisticVerificationException;
import javax.jdo.JDOUnsupportedOptionException;
import javax.jdo.JDOUserCallbackException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.Interaction;
import javax.resource.spi.LocalTransaction;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;

import org.openmdx.base.accessor.rest.DataObjectManager_1;
import org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0;
import org.openmdx.base.accessor.rest.spi.Synchronization_2_0;
import org.openmdx.base.aop0.PlugIn_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.UnitOfWork;
import org.openmdx.base.persistence.cci.UserObjects;
import org.openmdx.base.persistence.spi.AbstractPersistenceManagerFactory;
import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.base.resource.spi.LocalTransactions;
import org.openmdx.base.resource.spi.UserTransactions;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.loading.Factory;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.naming.ComponentEnvironment;

/**
 * Unit Of Work
 */
public class UnitOfWork_1 implements Serializable, Transaction, UnitOfWork, Synchronization_2_0 {

    /**
     * Constructor 
     *
     * @param persistenceManager
     * @param connection
     * @param aspectSpecificContexts
     * 
     * @throws JDODataStoreException  
     */
    UnitOfWork_1(
        DataObjectManager_1 persistenceManager,
        Connection connection,
        DataObjectManager_1.AspectObjectDispatcher aspectSpecificContexts
    ){
        this.dataObjectManager = persistenceManager;
        this.connection = connection;
        this.aspectSpecificContexts = aspectSpecificContexts;
        PersistenceManagerFactory persistenceManagerFactory = persistenceManager.getPersistenceManagerFactory();
		this.optimistic = persistenceManagerFactory.getOptimistic();
        try {
            String transactionType = persistenceManagerFactory.getTransactionType();
            if(Constants.RESOURCE_LOCAL.equals(transactionType)) {
                this.userTransaction = null;
                this.localTransaction = LocalTransactions.getLocalTransaction( 
                    this.dataObjectManager.connection.getLocalTransaction()
                ); 
            } else if (this.isContainerManaged()) {
                this.userTransaction = null;
                this.localTransaction = null; 
            } else {
                this.userTransaction = UserTransactions.getUserTransaction();
                this.localTransaction = LocalTransactions.getLocalTransaction(this.userTransaction); 
            }
        } catch (ResourceException exception) {
            throw new JDODataStoreException(
                "Unit of work could not be acquired",
                BasicException.newEmbeddedExceptionStack(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.TRANSACTION_FAILURE
                )
            );
        }
        this.setIsolationLevel(
        	persistenceManagerFactory.getTransactionIsolationLevel()
        );
    }

    /**
     * The associated persistence manager
     */
    final DataObjectManager_1 dataObjectManager;
    
    /**
     * 
     */
    private final Connection connection;
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3256437001975641907L;

    /**
     * The members of this unit of work
     */
    protected final List<DataObject_1> members = new ArrayList<DataObject_1>();

    /**
     * The thread local information of an object
     */
    protected final Map<DataObject_1,TransactionalState_1> states = 
        new IdentityHashMap<DataObject_1,TransactionalState_1>();

    /**
     * The transactional interaction
     */
    private Interaction interaction = null;
    
    /**
     * Rollback-only covers the whole transaction
     */
    private boolean rollbackOnly = false;
    
    /**
     * Forget-only is restricted to the unit of work
     */
    private boolean forgetOnly = false;

    /**
     * 
     */
    private boolean forgettable = false;
    
    /**
     * 
     */
    private final static int PREPARE_CYCLE_LIMIT = 16;

    /**
     * The user supplied synchronization object
     */
    private Synchronization entityManagerSynchronization = null;
    
    /**
     * The transaction time represents the date and time when the non-
     * optimistic part of a unit of work began.
     */
    private Date transactionTime = null;

    /**
     * A unit-of-work id is assigned when the unit of work is activated. 
     */
    private String unitOfWorkId = null;

    /**
     * A task id is assigned when the unit of work is activated.
     */
    private String taskId = null;
    
    /**
     * A flag to disable the before image comparison
     */
    private boolean updateAvoidanceDisabled = false;
    
    /**
     * 
     */
    private final DataObjectManager_1.AspectObjectDispatcher aspectSpecificContexts;

    /**
     * The delegate in case of an optimistic transaction
     */
    private final LocalTransaction localTransaction;
    
    /**
     * The optional user transaction object
     */
    private final UserTransaction userTransaction;

    /**
     * The optional transaction synchronization registry
     */
    private static TransactionSynchronizationRegistry transactionSynchronizationRegistry;
    
    /**
     * Tells whether the transaction is optimistic or pessimistic
     */
    private boolean optimistic;

    /**
     * Tells whether an optimistic transaction has been enlisted with a remote
     * or data store transaction.
     */
    private boolean enlisted;
    
    /**
     * Tells whether the unit of work is flushing
     */
    private boolean flushing = false;
    
    /**
     * Shared object accessor
     */
    private final SystemObjects systemObjects = new SystemObjects();

    /**
     * The requested transaction isolation level
     */
    private String isolationLevel;

    /**
     * Flush the unit of work to the data store
     * 
     * @param beforeCompletion <code>true</code> if the before completion
     * callbacks shall be included
     * 
     */
    synchronized void flush(
        boolean beforeCompletion
    ) throws ServiceException {
        if(this.forgetOnly) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "The unit of work has been marked for rollback only"
           );
       }
       if(this.isActive() && !this.flushing) try {
           this.forgettable = false;
           this.flushing = true;
           if(!this.enlisted) {
               this.enlist(true);
           }
           if(beforeCompletion && this.entityManagerSynchronization != null) try {
               this.entityManagerSynchronization.beforeCompletion();
           } catch (RuntimeException exception ){
               throw new JDOUserCallbackException(
                   "Synchronization failure",
                   exception
               );
           }
           boolean preparing = true;
           for(
               int cycle = 0;
               preparing && cycle < UnitOfWork_1.PREPARE_CYCLE_LIMIT;
               cycle++
           ){
               preparing = false;
               for(
                   int i=0;
                   i < this.members.size(); // this.members.size() may grow
                   i++
               ) {
                   DataObject_1 member = this.members.get(i);
                   TransactionalState_1 memberState = this.getState(member, true);
                   if(memberState != null && !memberState.isPrepared()){
                       preparing = true;
                       member.prepare();
                   }
               }
           }
           if(preparing) {
               throw new ServiceException(
                   BasicException.Code.DEFAULT_DOMAIN,
                   BasicException.Code.QUOTA_EXCEEDED,
                   "Maximal number of prepare cycles exceeded",
                   new BasicException.Parameter("maximum", UnitOfWork_1.PREPARE_CYCLE_LIMIT)
               );
           }
           for(PlugIn_1_0 plugIn : this.dataObjectManager.getPlugIns()) {
               plugIn.flush(this, beforeCompletion);
           }
           Collections.sort(this.members, FlushOrder.getInstance());
           if(this.dataObjectManager.isProxy()) {
               for(DataObject_1 member : this.members){
                   if(member.jdoIsNew() && !member.jdoIsDeleted()){
                       member.propagate(this.interaction);
                   }
               }
           }
           for(DataObject_1 member : this.members) {
               member.flush(this.interaction, beforeCompletion);
           }
       } catch (ResourceException exception) {
           throw new ServiceException(exception);
       } catch (JDOException exception) {
           throw new ServiceException(exception);
       } finally {
           this.flushing = false;
       }
    }
    
    /**
     * Retrieve a data object's state
     * 
     * @param member the data object
     * @param optional
     * 
     * @return the  data object's state
     */
    public TransactionalState_1 getState(
        DataObject_1 member,
        boolean optional
    ){
        TransactionalState_1 transactional = this.states.get(member);
        if(transactional == null && !optional) {
            this.states.put(
                member,
                transactional = new TransactionalState_1()
            );
        }
        return transactional;
    }

    boolean add(
        DataObject_1 member
    ) throws ServiceException {
        if(this.isActive()){
            boolean transition = !this.members.contains(member);
            if(transition){
                this.members.add(member);
            }
            return transition;
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "No active unit of work"
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.Synchronization_2_0#clear()
     */
//  @Override
    public void clear() {
        this.members.clear();
        this.states.clear();
    }

    void remove(
        DataObject_1 member
    ){
        this.members.remove(member);
        this.states.remove(member);
    }

    /**
     * Retrieve the members of the given unit of work
     * 
     * @return the members of the given unit of work
     */
    final List<DataObject_1> getMembers(){
    	return this.members;
    }

    /**
     * Flush if any member is out-of-sync
     * 
     * @throws ServiceException 
     */
    boolean synchronize() throws ServiceException{
    	boolean outOfSync = false;
        Members: for(DataObject_1 member : this.members) {
            if(outOfSync |= member.isOutOfSync()) {
                break Members;
            }
        }
    	if(outOfSync) {
        	this.flush(false);
    	}
    	return outOfSync;
    }

    /**
     * objFlush requires this information
     * 
     * @return the transaction time
     */
    public Date getTransactionTime(
    ){
        return this.transactionTime;
    }
        
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.UnitOfWorkContext#getTaskIdentifier()
     */
    public String getTaskIdentifier(
    ) {
        return this.taskId;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.UnitOfWorkContext#getUnitOfWorkIdentifier()
     */
    public String getUnitOfWorkIdentifier(
    ) {
        return this.unitOfWorkId;
    }

    
    //------------------------------------------------------------------------
    // Implements Synchronization_2_0      
    //------------------------------------------------------------------------

    /**
     * Retrieve the <code>TransactionSynchronizationRegistry</code> lazily
     * 
     * @return the <code>TransactionSynchronizationRegistry</code>
     */
    private TransactionSynchronizationRegistry getTransactionSynchronizationRegistry(
    ){
        if(UnitOfWork_1.transactionSynchronizationRegistry == null) try {
            UnitOfWork_1.transactionSynchronizationRegistry = ComponentEnvironment.lookup(TransactionSynchronizationRegistry.class);
        } catch (BasicException exception) {                        
            throw new JDOFatalDataStoreException(
                "Transaction synchronization registry acquisition failure",
                BasicException.newEmbeddedExceptionStack(exception)
            );
        }
        return UnitOfWork_1.transactionSynchronizationRegistry;
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getRollbackOnly()
     */
    public boolean getRollbackOnly(
    ) {
        if(!this.isActive()) {
            return false;
        } else if(this.rollbackOnly) {
            return true;
        } else if(this.optimistic) {
            return false;
        } else if (this.userTransaction == null){
            return getTransactionSynchronizationRegistry().getRollbackOnly();
        } else try {
            return this.userTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK;
        } catch (SystemException exception) {
            throw new JDODataStoreException(
                "Rollback-only query failed",
                BasicException.newEmbeddedExceptionStack(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.TRANSACTION_FAILURE
                )
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setRollbackOnly()
     */
    public void setRollbackOnly(
    ) {
        if(this.isActive()){
            this.rollbackOnly = true;
        } else {
            throw new JDODataStoreException(
                "There is no active transaction to be set into rollback-only mode",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE
                )
            );
        }
        if(!this.optimistic) {
            if(this.userTransaction == null) {
                getTransactionSynchronizationRegistry().setRollbackOnly();
            } else try {
                this.userTransaction.setRollbackOnly();
            } catch (IllegalStateException exception) {
                throw new JDODataStoreException(
                    "Could not set the transaction into rollback-only mode",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE
                    )
                );
            } catch (SystemException exception) {
                throw new JDODataStoreException(
                    "Could not set the transaction into rollback-only mode",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.TRANSACTION_FAILURE
                    )
                );
            }
        }
    }

    private void start(
    ){
        this.rollbackOnly = false;
        this.forgetOnly = false;
        this.forgettable = this.optimistic;
        this.members.clear();
        this.unitOfWorkId = UUIDConversion.toUID(UUIDs.newUUID());
        this.taskId = this.systemObjects.newTaskIdentifier();
        this.transactionTime = this.systemObjects.newTransactionTime();
    }
    
    /**
     * Provide the interaction used for commit
     * 
     * @return the interaction used for commit
     */
    Interaction getInteraction(){
        return this.interaction;
    }

    
    //------------------------------------------------------------------------
    // Implements Transaction
    //------------------------------------------------------------------------his.

    /** 
     * Begin a unit of work.  The type of unit of work is determined by the
     * setting of the Optimistic flag.
     * @see #setOptimistic
     * @see #getOptimistic
     * @throws ServiceException if units of work are managed by a container
     * in the managed environment, or if the unit of work is already active.
     */
    public void begin(
    ) {
        if(this.isActive()) {
        	throw BasicException.initHolder(
        		new JDOUserException(
    				"Unit of work is active",
    				BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE
                    )
    			)
            );
        } else {
            this.start();
            if(!this.optimistic) {
                try {
                    this.enlist(this.localTransaction != null);
                } catch (ResourceException exception) {
                    throw BasicException.initHolder(
                        new JDODataStoreException(
                            "Unit of work could not be started",
                            BasicException.newEmbeddedExceptionStack(
                                exception,
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.TRANSACTION_FAILURE
                            )
                        )
                    );
                }
            }
            SysLog.detail(
                "Unit of work started", 
                this.unitOfWorkId
            );
        }
    }

    /**
     * Enlist the unit of work in the data store transaction
     */
    private void enlist(
        boolean beginTransaction
    ) throws ResourceException {
        this.interaction = this.dataObjectManager.newInteraction(this.connection);
        if(beginTransaction) {
            this.localTransaction.begin();
        }
        this.enlisted = true;
        SysLog.detail(
            "Unit of work enlisted with datastore transaction", 
            this.unitOfWorkId
        );
    }
    
    /** 
     * Commit the current unit of work.
     * @throws ServiceException if units of work are managed by a container
     * in the managed environment, or if the unit of work is already active.
     */
    public void commit(
    ) {
        if(this.isActive()) {
            if(this.rollbackOnly) {
                this.afterCompletion(javax.transaction.Status.STATUS_ROLLEDBACK);
                throw BasicException.initHolder(
                    new JDOUserException(
                        "The unit of work has been marked for rollback only",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ROLLBACK
                        )
                    )
                );
            }  else {
                BasicException commitException = null;
                if(!this.forgetOnly) { 
                    try {
                        this.flush(true);
                    } catch (ServiceException exception) {
                        commitException = BasicException.newStandAloneExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ROLLBACK,
                            "Prepare failure"
                        );
                    }
                }
                if(commitException == null) {
                    try {
                        this.localTransaction.commit();
                    } catch (ResourceException exception) {
                        commitException = BasicException.newStandAloneExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ROLLBACK,
                            "Commit failure"
                        );
                    }
                } else if (this.enlisted) {
                    try {
                        this.localTransaction.rollback();
                    } catch (ResourceException exception) {
                        BasicException rollbackException = BasicException.newStandAloneExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.HEURISTIC,
                            "Rollback failure"
                        );
                        rollbackException.getCause(null).initCause(commitException);
                        commitException = rollbackException;
                    }
                }
                if(this.interaction != null) {
                    try {
                        this.interaction.close();
                    } catch (ResourceException exception) {
                        Throwables.log(exception);
                    } finally {
                        this.interaction = null;
                    } 
                }
                this.enlisted = false;
                if(commitException == null) {
                    this.afterCompletion(javax.transaction.Status.STATUS_COMMITTED);
                } else {
                    this.afterCompletion(javax.transaction.Status.STATUS_ROLLEDBACK);
                    BasicException initialCause = commitException.getCause(null);
                    throw initialCause.getExceptionCode() == BasicException.Code.CONCURRENT_ACCESS_FAILURE ? new JDOOptimisticVerificationException(
                        commitException.getDescription(),
                        new JDOOptimisticVerificationException[]{
                            BasicException.initHolder(
                                new JDOOptimisticVerificationException(
                                    initialCause.getDescription(),
                                    BasicException.newEmbeddedExceptionStack(commitException),
                                    this.getPersistenceManager().getObjectById(new Path(initialCause.getParameter("path")), false) 
                                )
                            )
                        }
                    ) : BasicException.initHolder(
                        new JDOFatalDataStoreException(
                            commitException.getDescription(),
                            BasicException.newEmbeddedExceptionStack(commitException)
                        )
                    );
                }
            }
        } else {
            throw BasicException.initHolder(
                new JDOUserException(
                    "No unit of work is active",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE
                    )
                )
            );
        }
    } 

    /**
     * Roll back the current unit of work.
     * @throws ServiceException if units of work are managed by a container
     * in the managed environment, or if the unit of work is not active.
     */
    public void rollback(
    ) {
        if(isActive()) {
            if(this.enlisted) try {
                this.localTransaction.rollback();
            } catch (ResourceException exception) {
                throw BasicException.initHolder(
                    new JDOUserException(
                        "Rollback failed",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.HEURISTIC
                        )
                    )
                );
            } finally {
                this.enlisted = false;
            }
            this.afterCompletion(javax.transaction.Status.STATUS_ROLLEDBACK);
        } else {
            throw BasicException.initHolder(
                new JDOUserException(
                    "No unit of work is active",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE
                    )
                )
            );
        }
    }       

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getNontransactionalRead()
     */
    public boolean getNontransactionalRead(
    ) {
        return this.dataObjectManager.getPersistenceManagerFactory().getNontransactionalRead();
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getNontransactionalWrite()
     */
    public boolean getNontransactionalWrite(
    ) {
        return this.dataObjectManager.getPersistenceManagerFactory().getNontransactionalWrite();
    }

    /* (non-Javadoc)restore
     * @see javax.jdo.Transaction#getPersistenceManager()
     */
    public PersistenceManager getPersistenceManager(
    ) {
        return this.dataObjectManager;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getRestoreValues()
     */
    public boolean getRestoreValues(
    ) {
        return this.dataObjectManager.getPersistenceManagerFactory().getRestoreValues();
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getRetainValues()
     */
    public boolean getRetainValues(
    ) {
        return this.dataObjectManager.getPersistenceManagerFactory().getRetainValues();
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getIsolationLevel()
     */
    public String getIsolationLevel() {
        return this.isolationLevel;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setIsolationLevel(java.lang.String)
     */
    public void setIsolationLevel(String isolationLevel) {
    	this.isolationLevel = isolationLevel;
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setNontransactionalRead(boolean)
     */
    public void setNontransactionalRead(
        boolean nontransactionalRead
    ) {
        if(nontransactionalRead != this.getNontransactionalRead()) {
            throw new JDOUnsupportedOptionException(
                "The nontransactional-read option can't be changed at unit-of-work level"
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setNontransactionalWrite(boolean)
     */
    public void setNontransactionalWrite(
        boolean nontransactionalWrite
    ) {
        if(nontransactionalWrite != this.getNontransactionalWrite()) {
            throw new JDOUnsupportedOptionException(
                "The nontransactional-write option can't be changed at unit-of-work level"
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setOptimistic(boolean)
     */
    public void setOptimistic(
        boolean optimistic
    ) {
        this.optimistic = optimistic;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setRestoreValues(boolean)
     */
    public void setRestoreValues(
        boolean restoreValues
    ) {
        if(restoreValues != this.getRestoreValues()) {
            throw new JDOUnsupportedOptionException(
                "The restore-values option can't be changed at unit-of-work level"
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setRetainValues(boolean)
     */
    public void setRetainValues(
        boolean retainValues
    ) {
        if(retainValues != this.getRetainValues()) {
            throw new JDOUnsupportedOptionException(
                "The retain-values option can't be changed at unit-of-work level"
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setSynchronization(javax.transaction.Synchronization)
     */
    public void setSynchronization(
        Synchronization sync
    ) {
        this.entityManagerSynchronization = sync;
    }
    
    /** 
     * Tells whether there is a unit of work currently active.
     * 
     * @return <code>true</code> if the unit of work is active.
     */
    public final boolean isActive(
    ){
        return this.unitOfWorkId != null;
    }

    /**
     * Optimistic units of work do not hold data store locks until commit time.
     *
     * @return the value of the Optimistic property.
     */
    public final boolean getOptimistic(
    ){
        return this.optimistic;
    }

    /**
     * Container managed unit's of work don't handle commit or rollback
     *
     * @return the value of the ContainerManaged property.
     */
    private final boolean isContainerManaged(
    ){
        return AbstractPersistenceManagerFactory.isTransactionContainerManaged(
            this.dataObjectManager.getPersistenceManagerFactory()
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.UnitOfWork_1_0#getSynchronization()
     */
    public Synchronization getSynchronization(
    ) {
        return this.entityManagerSynchronization;
    }
    
    
    //------------------------------------------------------------------------
    // Implements Synchronization_2_0       
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.Synchronization_2_0#afterBegin()
     */
//  @Override
    public void afterBegin(
    ) {
        this.start();
        try {
            this.enlist(false);
        } catch (ResourceException exception) {
            throw new JDOFatalDataStoreException(
                "Unable to enlist the unit of work in the current transaction",
                exception
            );
        }
    }

    
    //------------------------------------------------------------------------
    // Implements UnitOfWork    
    //------------------------------------------------------------------------

    /* (non-Javadoc)
	 * @see org.openmdx.base.persistence.cci.UnitOfWork#setForgetOnly()
	 */
//  @Override
	public void setForgetOnly() {
	    if(!this.optimistic) {
            throw new JDODataStoreException(
                "Only an optimistic unit of work may be set into forget-only mode",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED
                )
            );
	    }
        if(!this.isActive()){
            throw new JDODataStoreException(
                "There is no active unit of work to be set into forget-only mode",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE
                )
            );
        }
        if(!this.forgettable){
            throw new JDODataStoreException(
                "An optimistic unit of work becomes non-optimistic after flushing",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE
                )
            );
        }
	    this.forgetOnly = true;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.persistence.cci.UnitOfWork#isForgetOnly()
	 */
	public boolean isForgetOnly() {
	    return this.forgetOnly;
	}
	
    
    //------------------------------------------------------------------------
    // Implements Synchronization    
    //------------------------------------------------------------------------

    /**
     * The beforeCompletion method notifies a provider or plug-in that a
     * unit of work is about to be committed.
     */
//  @Override
    public void beforeCompletion(
    ) {
        try {
        	if(this.rollbackOnly) {
	            SysLog.detail(
	                "Unit of work is set to rollback only, no flushing takes place", 
	                this.unitOfWorkId
	            );
        	} else if(this.forgetOnly) {
                SysLog.detail(
                    "Unit of work is set to forget only, no flushing takes place", 
                    this.unitOfWorkId
                );
        	} else {
	            SysLog.detail(
	                "Unit of work flushing", 
	                this.unitOfWorkId
	            );
	            this.flush(true);
        	}
        } catch (ServiceException exception) {
            this.setRollbackOnly();
            throw BasicException.initHolder(
                new JDOFatalDataStoreException(
                    "Unable to flush unit of work",
                    BasicException.newEmbeddedExceptionStack(exception)
                )
            );                        
        }
    } 

    /**
     * The afterCompletion method notifies a provider or plug-in that a
     * unit of work commit protocol has completed, and tells the instance
     * whether the unit of work has been committed or rolled back. 
     * <p>
     * This method must not throw an exception.
     */
//  @Override
    synchronized public void afterCompletion(
        int status
    ) {
        try {
        	this.updateAvoidanceDisabled = false; // reset the flag
            this.unitOfWorkId = null;
            this.notifyAll();
            while(!this.members.isEmpty()) {
                DataObject_1 member = this.members.remove(0); 
                if(!member.objIsInaccessible()) {
                    member.afterCompletion(status);
                }
            }
            this.states.clear();
            this.transactionTime = null;
            this.taskId = null;
            this.aspectSpecificContexts.clear();
            if(status == javax.transaction.Status.STATUS_COMMITTED) {
                SharedObjects.getPlugInObject(this.dataObjectManager, DataStoreCache_2_0.class).evictAll();
            }
            if(!this.dataObjectManager.isRetainValues()) {
                this.dataObjectManager.evictAll();
            }
            if(this.entityManagerSynchronization != null) {
                this.entityManagerSynchronization.afterCompletion(status);
            }
        } catch (Exception exception) {
            new RuntimeServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.SYSTEM_EXCEPTION,
                "afterCompletion() failure",
                new BasicException.Parameter(
                    "status", UserTransactions.getStatus(status)
                )
            ).log();
        }
    }

    /**
     * Disables the update avoidance for the current unit of work
     */
	public void disableUpdateAvoidance() {
		this.updateAvoidanceDisabled = true;
	}
	
	/**
	 * @return the updateAvoidanceDisabled
	 */
	public boolean isUpdateAvoidanceEnabled() {
		return !this.updateAvoidanceDisabled;
	}
	
	/**
     * If read lock is required then a concurrent modification exception shall
     * be thrown if the object is<ul>
	 *     <li>either updated by both this unit of work and another unit of
	 *         work concurrently
	 *     <li>or made transactional in this unit of work and
	 *         updated by another unit of work concurrently
	 *     </ul>
	 * </ul>
	 * @return <code>true</code>
	 */
	public boolean isReadLockRequired(){
		return Constants.TX_REPEATABLE_READ.equals(this.isolationLevel);
	}

	
    //------------------------------------------------------------------------
    // Class SystemObjects
    //------------------------------------------------------------------------

	/**
     * System Objects
     */
    class SystemObjects extends SharedObjects {

        /**
         * Constructor 
         */
        protected SystemObjects(
        ){
            super();
        }
        
        /**
         * Retrieve the shared objects accessor
         * 
         * @return the shared objects accessor
         */
        private Accessor getAccessor(){
            return sharedObjects(UnitOfWork_1.this.dataObjectManager);
        }
        
        /**
         * Retrieve the current task identifier 
         * 
         * @return the current task identifier 
         * 
         * @see UserObjects#setTaskIdentifier(PersistenceManager, Object)
         */
        String newTaskIdentifier(){
            Object taskIdentifier = getAccessor().getTaskIdentifier();
            return taskIdentifier == null ? null : taskIdentifier.toString();
        }

        /**
         * Retrieve the current transaction time
         * 
         * @return the current transaction time
         * 
         * @see UserObjects#setTransactionTime(PersistenceManager, Factory<Date>)
         */
        Date newTransactionTime(){
            Factory<Date> transactionTime = getAccessor().getTransactionTime();
            return transactionTime == null ? new Date() : transactionTime.instantiate();
        }
        
    }

}
