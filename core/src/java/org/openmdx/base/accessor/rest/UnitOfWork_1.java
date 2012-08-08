/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: UnitOfWork_1.java,v 1.4 2009/06/08 17:09:03 hburger Exp $
 * Description: Unit Of Work Implementation
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/08 17:09:03 $
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

import javax.jdo.JDOException;
import javax.jdo.JDOFatalInternalException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOOptimisticVerificationException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.resource.cci.Connection;
import javax.resource.cci.Interaction;
import javax.resource.spi.LocalTransactionException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.openmdx.base.accessor.rest.DataObjectManager_1.AspectObjectDispatcher;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.TransactionManager;
import org.openmdx.base.resource.spi.UserTransactionAdapter;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.naming.ComponentEnvironment;

/**
 * Unit Of Work implementation.
 */
class UnitOfWork_1 implements Serializable, Transaction, Synchronization {

    /**
     * Constructor 
     *
     * @param persistenceManager
     * @param connection
     * @param transactionManager
     * @param aspectSpecificContexts
     */
    UnitOfWork_1(
        PersistenceManager persistenceManager,
        Connection connection,
        TransactionManager transactionManager, 
        AspectObjectDispatcher aspectSpecificContexts
    ){
        this.persistenceManager = persistenceManager;
        this.connection = connection;
        this.transactionManager = transactionManager;
        this.aspectSpecificContexts = aspectSpecificContexts;
    }

    /**
     * The associated persistence manager
     */
    private final PersistenceManager persistenceManager;
    
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
    private final TransactionManager transactionManager;

    /**
     * 
     */
    private UserTransaction userTransaction = null;
    
    /**
     *
     */
    private boolean active = false;

    /**
    *
    */
   private boolean optimistic = true;
    
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
    private Synchronization synchronization = null;
    
    /**
     * The transaction time, i.e.<ul>
     * <li>The time point of the transaction's commit() invocation in case of an optimistic transaction
     * <li>The time point of the transaction's begin() invocation in case of a non-optimistic transaction
     * </ul>
     */
    private Date transactionTime = null;
    
    /**
     * 
     */
    private final AspectObjectDispatcher aspectSpecificContexts;
    
    
    //------------------------------------------------------------------------
    // Membership management
    //------------------------------------------------------------------------

    TransactionalState_1 getState(
        DataObject_1 member,
        boolean optional
    ) throws ServiceException {
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
     * objFlush requires this information
     * 
     * @return the transaction time
     */
    protected Date getTransactionTime(){
        return this.transactionTime;
    }
    
    
    //------------------------------------------------------------------------
    // Implements UnitOfWork_1_2       
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.UnitOfWork_1_2#getRollbckOnly()
     */
    public boolean getRollbackOnly(
    ) {
        if(this.optimistic) {
            return this.active && this.rollbackOnly;
        } else try {
            return getUserTransaction().getStatus() == Status.STATUS_MARKED_ROLLBACK;
        } catch (SystemException exception) {
            throw BasicException.initHolder(
                new JDOFatalInternalException(
                    "Could not retrieve transaction status",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.TRANSACTION_FAILURE
                    )
                )
             );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.UnitOfWork_1_2#setRollbackOnly()
     */
    public void setRollbackOnly() {
        if(this.optimistic) {
            this.rollbackOnly = true;
        } else {
            try {
                getUserTransaction().setRollbackOnly();
            } catch (IllegalStateException exception) {
                throw BasicException.initHolder(
                    new JDOUserException(
                        "No active transaction",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ILLEGAL_STATE
                        )
                    )
                 );
            } catch (SystemException exception) {
                throw BasicException.initHolder(
                    new JDOFatalInternalException(
                        "Could not set to rollback-only",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.TRANSACTION_FAILURE
                        )
                    )
                 );
            }
        }
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
        if(this.userTransaction == null) try {
            this.userTransaction = ComponentEnvironment.lookup(UserTransaction.class);
        } catch (BasicException exception) {
            throw new JDOFatalInternalException(
                "User transaction acquisition failure",
                exception
            );
        }
        return this.userTransaction;
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
        if(this.active) {
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
            this.rollbackOnly = false;
            this.members.clear();
            this.active = true;
            if(!this.optimistic) try {
                getUserTransaction().begin();
            } catch (Exception exception) {
                throw new JDOFatalInternalException(
                    "Non-optimistic transaction start failure",
                    exception
                );
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
        if(this.active) {
            if(this.rollbackOnly) {
                if(this.optimistic) {
                    this.afterCompletion(javax.transaction.Status.STATUS_ROLLEDBACK);
                } else try {
                    getUserTransaction().rollback();
                    this.afterCompletion(javax.transaction.Status.STATUS_ROLLEDBACK);
                } catch (SystemException exception) {
                    throw BasicException.initHolder(
                        new JDOFatalInternalException(
                            "Rollback failure",
                            BasicException.newEmbeddedExceptionStack(
                                exception,
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.TRANSACTION_FAILURE 
                            )
                        )
                    );
                }
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
                if(this.optimistic) {
                    try {
                        this.transactionManager.commit(this);
                        afterCompletion(Status.STATUS_COMMITTED);
                    } catch(LocalTransactionException exception) {
                        this.afterCompletion(Status.STATUS_ROLLEDBACK);
                        throw toCommitException(exception);
                    }
                }  else  {
                    BasicException commitException = null;
                    try {
                        this.beforeCompletion();
                    } catch (Exception exception) {
                        commitException = BasicException.newStandAloneExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ROLLBACK,
                            "Prepare failure"
                        );
                    }
                    if(commitException == null) try {
                        getUserTransaction().commit();
                    } catch (RollbackException exception) {
                        commitException = BasicException.newStandAloneExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ROLLBACK,
                            "Rollback during commit"
                        );
                    } catch (HeuristicMixedException exception) {
                        commitException = BasicException.newStandAloneExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.HEURISTIC,
                            "Heuristic commit"
                        );
                    } catch (HeuristicRollbackException exception) {
                        commitException = BasicException.newStandAloneExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.HEURISTIC,
                            "Heuristic rollback"
                        );
                    } catch (SystemException exception) {
                        commitException = BasicException.newStandAloneExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.TRANSACTION_FAILURE,
                            "Commit failure"
                        );
                    } else try {
                        getUserTransaction().rollback();
                    } catch (Exception exception) {
                        commitException = BasicException.newStandAloneExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.HEURISTIC,
                            "Rollback failure"
                        ).getCause(
                            null
                        ).initCause(
                            commitException
                        );
                    }
                    if(commitException == null) {
                        this.afterCompletion(javax.transaction.Status.STATUS_COMMITTED);
                    } else {
                        this.afterCompletion(javax.transaction.Status.STATUS_ROLLEDBACK);
                        throw toCommitException(commitException);
                    }
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
     * Handle concurrent modification exceptions properly
     * 
     * @param exception
     * 
     * @return a JDO commit exception
     */
    private JDOFatalUserException toCommitException(
        Exception exception
    ){
        BasicException exceptionStack = BasicException.toExceptionStack(exception);
        BasicException initialCause = exceptionStack.getCause(null);
        if(initialCause.getExceptionCode() == BasicException.Code.CONCURRENT_ACCESS_FAILURE){
            throw new JDOOptimisticVerificationException(
                exceptionStack.getDescription(),
                new JDOOptimisticVerificationException[]{
                    BasicException.initHolder(
                        new JDOOptimisticVerificationException(
                            initialCause.getDescription(),
                            BasicException.newEmbeddedExceptionStack(exceptionStack),
                            getPersistenceManager().getObjectById(new Path(initialCause.getParameter("path"))) 
                        )
                    )
                }
            );
        } else {
            throw BasicException.initHolder(
                new JDOFatalUserException(
                    exceptionStack.getDescription(),
                    exceptionStack
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
        if(this.active) {
            if(this.optimistic) {
                this.afterCompletion(javax.transaction.Status.STATUS_ROLLEDBACK);
            } else try {
                getUserTransaction().rollback();
            } catch (SystemException exception) {
                throw BasicException.initHolder(
                    new JDOFatalInternalException(
                        "Rollback failure",
                        BasicException.newEmbeddedExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.HEURISTIC
                        )
                    )
                );
            } finally {
                this.afterCompletion(javax.transaction.Status.STATUS_ROLLEDBACK);
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
        return this.persistenceManager;
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
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
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
        this.synchronization = sync;
    }
    
    /** 
     * Tells whether there is a unit of work currently active.
     * @return <code>true</code> if the unit of work is active.
     */
    public final boolean isActive(
    ){
        return this.active;
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
        return this.synchronization;
    }
    
    //------------------------------------------------------------------------
    // Implements Synchronization_1_0       
    //------------------------------------------------------------------------

    /**
     * The beforeCompletion method notifies a provider or plug-in that a
     * unit of work is about to be committed.
     */
    public void beforeCompletion(
    ) {
        SysLog.detail("Unit Of Work","flushing");
        if(this.synchronization != null) {
            this.synchronization.beforeCompletion();
        }
        if(this.optimistic) {
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
                try {
                    TransactionalState_1 memberState = this.getState(member, true);
                    if(memberState != null && !memberState.isPrepared()){
                        preparing = true;
                        member.prepare();
                    }
                } catch(ServiceException exception) {
                    throw BasicException.initHolder(
                        new JDOUserException(
                            "Unable to perform beforeCompletion",
                            BasicException.newEmbeddedExceptionStack(exception),
                            member
                        )
                    );                        
                }
            }
        }
        if(preparing) {
            throw BasicException.initHolder(
                new JDOUserException(
                    "Maximal number of prepare cycles exceeded",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.QUOTA_EXCEEDED,
                        new BasicException.Parameter("maximum", PREPARE_CYCLE_LIMIT)
                    )
                )
            );
        }
        try {
            Interaction interaction = this.connection.createInteraction();
            for(DataObject_1 member : this.members) {
                member.flush(interaction);
            }
        } catch (JDOException exception) {
        	throw exception;
        } catch (Exception exception) {
            throw BasicException.initHolder(
                new JDOUserException(
                    "Unable to perform beforeCompletion",
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
            this.active = false;
            notifyAll();
            for(DataObject_1 member : this.members) {
                member.afterCompletion(status);
            }
            this.members.clear();
            this.states.clear();
            this.transactionTime = null;
            this.aspectSpecificContexts.clear();
            if(this.synchronization != null) {
                this.synchronization.afterCompletion(status);
            }
        } catch (Exception exception) {
            new RuntimeServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.SYSTEM_EXCEPTION,
                "afterCompletion() failure",
                new BasicException.Parameter(
                    "status", UserTransactionAdapter.getStatus(status)
                )
            ).log();
        }
    }

}
