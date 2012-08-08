/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: UnitOfWork_1.java,v 1.25 2010/04/17 13:02:26 hburger Exp $
 * Description: Unit Of Work
 * Revision:    $Revision: 1.25 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/17 13:02:26 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDODataStoreException;
import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.JDOFatalInternalException;
import javax.jdo.JDOOptimisticVerificationException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.Interaction;
import javax.resource.spi.LocalTransaction;
import javax.transaction.Synchronization;
import javax.transaction.UserTransaction;

import org.openmdx.base.accessor.rest.DataObjectManager_1.AspectObjectDispatcher;
import org.openmdx.base.accessor.rest.spi.Synchronization_2_0;
import org.openmdx.base.aop0.PlugIn_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.UserObjects;
import org.openmdx.base.resource.spi.LocalTransactions;
import org.openmdx.base.resource.spi.UserTransactions;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.log.SysLog;

/**
 * Unit Of Work
 */
public class UnitOfWork_1 implements Serializable, Transaction, Synchronization_2_0 {

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
        AspectObjectDispatcher aspectSpecificContexts
    ){
        this.dataObjectManager = persistenceManager;
        this.connection = connection;
        this.aspectSpecificContexts = aspectSpecificContexts;
        this.optimistic = persistenceManager.getPersistenceManagerFactory().getOptimistic();
        try {
            this.delegate = PersistenceUnitTransactionType.RESOURCE_LOCAL.name().equals(persistenceManager.getPersistenceManagerFactory().getTransactionType()) ? LocalTransactions.getLocalTransaction( 
                this.dataObjectManager.connection.getLocalTransaction()
            ) : LocalTransactions.getLocalTransaction( 
                getUserTransaction()
            );
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
    }

    /**
     * The associated persistence manager
     */
    private final DataObjectManager_1 dataObjectManager;
    
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
     * 
     */
    private UserTransaction userTransaction = null;
    
    /**
     * The transactional interaction
     */
    private Interaction interaction = null;
    
    /**
     * 
     */
    private boolean rollbackOnly = false;

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
     * 
     */
    private final AspectObjectDispatcher aspectSpecificContexts;

    /**
     * 
     */
    private final LocalTransaction delegate;
    
    /**
     * 
     */
    private boolean optimistic;

    /**
     * Tells whether an optimistic transaction has been enlisted with the datastore transaction
     */
    private boolean enlisted;
    
    private boolean flushing = false;
    
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
       if(isActive() && !this.flushing) try {
           this.flushing = true;
           if(!this.enlisted) {
               enlist();
           }
           if(beforeCompletion && this.entityManagerSynchronization != null) {
               this.entityManagerSynchronization.beforeCompletion();
           }
           if(this.transactionTime == null) {
               this.transactionTime = new Date();
           }
           boolean preparing = true;
           for(
               int cycle = 0;
               preparing && cycle < PREPARE_CYCLE_LIMIT;
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
                   new BasicException.Parameter("maximum", PREPARE_CYCLE_LIMIT)
               );
           }
           if(beforeCompletion) {
               for(PlugIn_1_0 plugIn : this.dataObjectManager.getPlugIns()) {
                   plugIn.beforeCompletion(this);
               }
           }
           for(
               int index1 = 0, memberCount = this.members.size();
               index1 < memberCount;
               index1++
           ){
               DataObject_1 member1 = this.members.get(index1);
               if(member1.jdoIsNew() && !member1.jdoIsDeleted()){
                   Path xri1 = member1.jdoGetObjectId();
                   for(
                       int index2 = index1 + 1;
                       index2 < memberCount;
                       index2++
                   ){
                       DataObject_1 member2 = this.members.get(index2);
                       if(member2.jdoIsNew() && !member2.jdoIsDeleted()){
                           Path xri2 = member2.jdoGetObjectId();
                           if(xri1.startsWith(xri2)){
                               this.members.add(index1, this.members.remove(index2));
                               member1 = member2;
                               xri1 = xri2;
                               index2 = index1 + 2;
                           }
                       }
                   }
               }
           }
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
        if(isActive()){
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

    void remove(
        DataObject_1 member
    ) throws ServiceException {
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
     * Tests whether any member is out-of-sync
     * 
     * @return <code>true</code> if any member is out-of-sync
     */
    boolean isOutOfSync(){
        if(!this.members.isEmpty()) {
            for(DataObject_1 member : this.members) {
                if(member.isOutOfSync()) {
                    return true;
                }
            }
        }
        return false;
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
        return isActive() && this.rollbackOnly;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setRollbackOnly()
     */
    public void setRollbackOnly(
    ) {
        this.rollbackOnly = true;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.Synchronization_2_0#afterBegin()
     */
    public void afterBegin(
    ) {
        start();
        this.enlisted = this.optimistic;
    }

    private void start(
    ){
        this.rollbackOnly = false;
        this.members.clear();
        this.unitOfWorkId = UUIDConversion.toUID(UUIDs.newUUID());
        Object taskIdentifier = UserObjects.getTaskIdentifier(this.dataObjectManager);
        this.taskId = taskIdentifier == null ? null : taskIdentifier.toString();
    }
    
    //------------------------------------------------------------------------
    // Implements UnitOfWork_1_0
    //------------------------------------------------------------------------

    /**
     * Retrieve the user transaction on demand
     * 
     * @return the user transaction
     */
    private UserTransaction getUserTransaction(
    ){
        if(this.userTransaction == null) { 
            try {
                this.userTransaction = UserTransactions.getUserTransaction();
            } catch (ResourceException exception) {
                throw new JDOFatalInternalException(
                    "User transaction acquisition failure",
                    exception
                );
            }
        }
        return this.userTransaction;
    }
    
    /**
     * Provide the interaction used for commit
     * 
     * @return the interaction used for commit
     */
    Interaction getInteraction(){
        return this.interaction;
    }
    
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
        if(isActive()) {
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
            start();
            if(!this.optimistic) {
                try {
                    enlist();
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
    ) throws ResourceException {
        this.interaction = this.connection.createInteraction();
        this.delegate.begin();
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
        if(isActive()) {
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
                if(commitException == null) { 
                    try {
                        flush(true);
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
                        this.delegate.commit();
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
                        this.delegate.rollback();
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
                                    getPersistenceManager().getObjectById(new Path(initialCause.getParameter("path"))) 
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
            if(this.enlisted) {
                try {
                    this.delegate.rollback();
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
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getNontransactionalWrite()
     */
    public boolean getNontransactionalWrite(
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
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
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getRetainValues()
     */
    public boolean getRetainValues(
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#getIsolationLevel()
     */
    public String getIsolationLevel() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setIsolationLevel(java.lang.String)
     */
    public void setIsolationLevel(String arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setNontransactionalRead(boolean)
     */
    public void setNontransactionalRead(
        boolean nontransactionalRead
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setNontransactionalWrite(boolean)
     */
    public void setNontransactionalWrite(
        boolean nontransactionalWrite
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
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
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.Transaction#setRetainValues(boolean)
     */
    public void setRetainValues(
        boolean retainValues
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
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
     * Tells whether the persistence manager is enlisted in either<ul>
     * <li>a remote unit of work
     * <li>or a datastore transaction
     * </ul>
     * @return <code>true</code> when a proxy is flushing
     */
    boolean isEnlisted(){
        return this.enlisted;
    }
    
    /**
     * Optimistic units of work do not hold data store locks until commit time.
     *
     * @return the value of the Optimistic property.
     */
    public final boolean getOptimistic(
    ){
        return true;
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

    /**
     * The beforeCompletion method notifies a provider or plug-in that a
     * unit of work is about to be committed.
     */
    public void beforeCompletion(
    ) {
        SysLog.detail("Unit Of Work","flushing");
        try {
            flush(true);
        } catch (ServiceException exception) {
            throw BasicException.initHolder(
                new JDOUserException(
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
    synchronized public void afterCompletion(
        int status
    ) {
        try {
            this.unitOfWorkId = null;
            notifyAll();
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

}
