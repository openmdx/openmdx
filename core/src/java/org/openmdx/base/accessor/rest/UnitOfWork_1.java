/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Unit Of Work
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2017, OMEX AG, Switzerland
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
import java.util.concurrent.ConcurrentHashMap;

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
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.Interaction;

import org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0;
import org.openmdx.base.accessor.rest.spi.LocalUserTransactionAdapters;
import org.openmdx.base.aop0.PlugIn_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.Synchronization;
import org.openmdx.base.persistence.cci.UserObjects;
import org.openmdx.base.persistence.spi.AbstractPersistenceManagerFactory;
import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.base.persistence.spi.UnitOfWork;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.base.transaction.LocalUserTransaction;
import org.openmdx.base.transaction.Status;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.loading.Factory;
import org.openmdx.kernel.log.SysLog;

/**
 * Unit Of Work
 */
public class UnitOfWork_1 implements Serializable, UnitOfWork {

    /**
     * Constructor 
     *
     * @param persistenceManager
     * @param connection
     * @param aspectSpecificContexts
     * @param threadSafe tells, whether the unit of work instance shall be thread safe
     * (e.g. when not providing a separate unit of work per thread)
     * 
     * @throws JDODataStoreException  
     */
    UnitOfWork_1(
        DataObjectManager_1 persistenceManager,
        Connection connection,
        DataObjectManager_1.AspectObjectDispatcher aspectSpecificContexts, 
        boolean threadSafe
    ){
        this.dataObjectManager = persistenceManager;
        this.connection = connection;
        this.aspectSpecificContexts = aspectSpecificContexts;
        PersistenceManagerFactory persistenceManagerFactory = persistenceManager.getPersistenceManagerFactory();
        this.optimistic = persistenceManagerFactory.getOptimistic();
        this.containerManaged = AbstractPersistenceManagerFactory.isTransactionContainerManaged(
            persistenceManagerFactory
        );
        this.resourceLocalTransaction = Constants.RESOURCE_LOCAL.equals(
            persistenceManagerFactory.getTransactionType()
        );
        this.setIsolationLevel(
            persistenceManagerFactory.getTransactionIsolationLevel()
        );
        if(threadSafe) {
            this.members = Collections.synchronizedList(new ArrayList<DataObject_1>());
            this.states = new ConcurrentHashMap<DataObject_1,TransactionalState_1>();
        } else {
            this.members = new ArrayList<DataObject_1>();
            this.states = new IdentityHashMap<DataObject_1,TransactionalState_1>();
        }
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
     * 
     */
    private final boolean containerManaged;
    
    /**
     * Tells whether we use a resource local transaction or not
     */
    private final boolean resourceLocalTransaction;
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3256437001975641907L;

    /**
     * The members of this unit of work
     */
    protected final List<DataObject_1> members ;

    /**
     * The thread local information of an object
     */
    protected final Map<DataObject_1,TransactionalState_1> states;
        
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
     * Remembers whether the unit of work has beeen flushed
     */
    private boolean flushed = false;
    
    /**
     * 
     */
    private final static int PREPARE_CYCLE_LIMIT = 16;

    /**
     * The user supplied synchronization object
     */
    private Synchronization entityManagerSynchronization = null;
    
    /**
     * Defines when the unit of work did start.
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
     * The user transaction object
     */
    private LocalUserTransaction  userTransaction;

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
     * Determines whether the unit of work has been flushed.
     *
     * @return Returns the forgettable.
     */
    final boolean isFlushed() {
        return this.flushed;
    }

    /**
     * Use its path to find the failed object
     * 
     * @param initialCause
     * 
     * @return the failed object, or <code>null</code> if its acquisition fails
     */
    private Object getFailedObject(BasicException initialCause) {
        try {
            return this.getPersistenceManager().getObjectById(new Path(initialCause.getParameter("path")), false);
        } catch (RuntimeException ignored) {
            return null; // Must no prevent us from creating a fatal data store exception
        }
    } 

    /**
     * Create a fatal data store exception
     * 
     * @param cause the cause for the before completion or commit failure 
     * 
     * @return a generic fatal data store exception or a specific optimistic verification exception
     */
    private JDOFatalDataStoreException toFatalDataStoreException(
        BasicException cause
    ) {
        BasicException initialCause = cause.getCause(null);
        return initialCause.getExceptionCode() == BasicException.Code.CONCURRENT_ACCESS_FAILURE ? new JDOOptimisticVerificationException(
            cause.getDescription(),
            new JDOOptimisticVerificationException[]{
                BasicException.initHolder(
                    new JDOOptimisticVerificationException(
                        initialCause.getDescription(),
                        BasicException.newEmbeddedExceptionStack(cause),
                        getFailedObject(initialCause) 
                    )
                )
            }
        ) : BasicException.initHolder(
            new JDOFatalDataStoreException(
                cause.getDescription(),
                BasicException.newEmbeddedExceptionStack(cause)
            )
        );
    }
    
    /*
     * Determines whether the unit of work is forgettable.
     *
     * @return Returns the forgettable.
     */
    private final boolean isForgettable() {
        return this.optimistic && !this.flushed;
    }
    
    private LocalUserTransaction getUserTransaction(
    ) throws ResourceException {
        if(this.userTransaction == null) {
            if(this.resourceLocalTransaction) {
                this.userTransaction = LocalUserTransactionAdapters.getResourceLocalUserTransactionAdapter(this.getPersistenceManager());
            } else if (this.containerManaged) { 
                this.userTransaction = LocalUserTransactionAdapters.getContainerManagedUserTransactionAdapter();
            } else {
                this.userTransaction = LocalUserTransactionAdapters.getJTAUserTransactionAdapter();
            }
        }
        return this.userTransaction;
    }

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
           this.flushed = true;
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
    @Override
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
        } else try {
            return getUserTransaction().isRollbackOnly();
        } catch (ResourceException exception) {
            throw new JDODataStoreException(
                "Rollback only query failure",
                exception
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
            try {
                getUserTransaction().setRollbackOnly();
            } catch (ResourceException exception) {
                throw new JDODataStoreException(
                    "Unable to set rollback only",
                    exception
                );
            }
        }
    }

    private void start(
    ){
        this.rollbackOnly = false;
        this.forgetOnly = false;
        this.flushed = false;
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
                    this.enlist(!containerManaged);
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
            getUserTransaction().begin();
        }
        this.enlisted = true;
        SysLog.detail(
            "Unit of work enlisted with datastore transaction", 
            this.unitOfWorkId
        );
    }
    
    /**
     * Close the interaction
     */
    private void closeInteraction(
        ) {
        if(this.interaction != null) {
            try {
                this.interaction.close();
            } catch (ResourceException exception) {
                Throwables.log(exception);
            } finally {
                this.interaction = null;
            } 
        }
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
                this.afterCompletion(Status.STATUS_ROLLEDBACK);
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
                        getUserTransaction().commit();
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
                        getUserTransaction().rollback();
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
                closeInteraction();
                this.enlisted = false;
                if(commitException == null) {
                    this.afterCompletion(Status.STATUS_COMMITTED);
                } else {
                    this.afterCompletion(Status.STATUS_ROLLEDBACK);
                    throw toFatalDataStoreException(commitException);
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
                getUserTransaction().rollback();
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
                closeInteraction();
                this.enlisted = false;
            }
            this.afterCompletion(Status.STATUS_ROLLEDBACK);
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
    @Override
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
    @Override
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
    @Override
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
        if(!isForgettable()){
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
    @Override
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
            throw toFatalDataStoreException(
                BasicException.newStandAloneExceptionStack(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ROLLBACK,
                    "Unable to flush unit of work"
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
    @Override
    synchronized public void afterCompletion(
        Status status
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
            if(status == Status.STATUS_COMMITTED) {
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
                    "status", status
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
