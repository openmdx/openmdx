/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: UnitOfWork_1.java,v 1.30 2008/12/15 03:15:29 hburger Exp $
 * Description: Unit Of Work Implementation
 * Revision:    $Revision: 1.30 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:29 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.transport.delegation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.persistence.spi.OptimisticTransaction_2_0;
import org.openmdx.base.transaction.Synchronization_1_0;
import org.openmdx.base.transaction.TransactionManager_1;
import org.openmdx.base.transaction.UnitOfWork_1_2;
import org.openmdx.compatibility.base.collection.FilteringList;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.Selector;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.slf4j.LoggerFactory;

/**
 * Unit Of Work implementation.
 */
class UnitOfWork_1 implements Serializable, UnitOfWork_1_2 {

    /**
     * Constructor
     * 
     * @param synchronization
     * @param transactional
     * @param containerManaged
     * @param optimistic
     * @param userTransaction
     * @param optimisticTransaction 
     * @throws ServiceException BAD_PARAMETER 
     *         if the userTransaction is missing for a transactional 
     *         non-optimistic unit of work
     */
    UnitOfWork_1(
        Synchronization_1_0 synchronization,
        boolean transactional,
        boolean containerManaged,
        boolean optimistic,
        UserTransaction userTransaction, 
        OptimisticTransaction_2_0 optimisticTransaction
    ){
        this.synchronization = synchronization;
        this.transactional = transactional;
        this.containerManaged = containerManaged;
        this.optimistic = optimistic;
        this.userTransaction = userTransaction;
        this.optimisticTransaction = optimisticTransaction;
        this.opimisticTransactionCallback = optimisticTransaction == null ? 
            null :
            new OptimisticTransactionCallback();
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3256437001975641907L;

    /**
     * The members of this unit of work
     */
    protected final List<Object_1> members = new ArrayList<Object_1>();

    /**
     * The thread local information of an object
     */
    protected final Map<Object_1,TransactionalState_1> states = 
        new IdentityHashMap<Object_1,TransactionalState_1>();

    /**
     * The filtered containers created by this unit of work.
     */
    private final Collection<Evictable> containers = new ArrayList<Evictable>();

    /**
     *
     */
    private final Synchronization_1_0 synchronization;

    /**
     *
     */
    private final boolean containerManaged;

    /**
     *
     */
    private final boolean transactional;

    /**
     * 
     */
    private boolean optimistic;

    /**
     * 
     */
    private UserTransaction userTransaction;

    /**
     * 
     */
    private OptimisticTransaction_2_0 optimisticTransaction;

    /**
     * 
     */
    private Synchronization opimisticTransactionCallback;
    
    /**
     *
     */
    private boolean active = false;

    /**
     * 
     */
    private boolean rollbackOnly = false;

    /**
     * 
     */
    private final static int PREPARE_CYCLE_LIMIT = 8;

    /**
     * The transaction time, i.e.<ul>
     * <li>The time point of the transaction's commit() invocation in case of an optimistic transaction
     * <li>The time point of the transaction's begin() invocation in case of a non-optimistic transaction
     * </ul>
     */
    private Date transactionTime = null;
    
    
    //------------------------------------------------------------------------
    // Membership management
    //------------------------------------------------------------------------

    TransactionalState_1 getState(
        Object_1 member,
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
        Object_1 member
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
        Object_1 member
    ) throws ServiceException {
        this.members.remove(member);
        this.states.remove(member);
    }

    boolean contains(
        Object candidate
    ){
        return this.members.contains(candidate);
    }

    List<?> include (
        Path referenceFilter,
        Selector attributeFilter
    ){
        return new Include(
            this.members, 
            referenceFilter, attributeFilter
        );
    }

    List<?> exclude (
        Path referenceFilter,
        Selector attributeFilter
    ){
        return new Exclude(
            this.members, 
            referenceFilter, attributeFilter
        );
    }

    void register(
        Evictable container
    ){
        this.containers.add(container);
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
    public boolean getRollbackOnly() {
        return this.active && this.rollbackOnly;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.UnitOfWork_1_2#setRollbackOnly()
     */
    public void setRollbackOnly() {
        this.rollbackOnly = true;
    }

    //------------------------------------------------------------------------
    // Implements UnitOfWork_1_0
    //------------------------------------------------------------------------

    /** 
     * Begin a unit of work.  The type of unit of work is determined by the
     * setting of the Optimistic flag.
     * @see #setOptimistic
     * @see #getOptimistic
     * @throws ServiceException if units of work are managed by a container
     * in the managed environment, or if the unit of work is already active.
     */
    public void begin(
    ) throws ServiceException {
        if(isActive())throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            "Unit of work is active"
        );
        if(isContainerManaged())throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "A container managed transaction can't be started"
        );
        if(isTransactional() && !isOptimistic()) try {
            this.transactionTime = new Date();
            this.userTransaction.begin();
        } catch (NotSupportedException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Transaction could not be started"
            );
        } catch (SystemException exception) {
            throw new ServiceException(exception);
        }
        reset();
        this.active = true;
    }

    /** 
     * Commit the current unit of work.
     * @throws ServiceException if units of work are managed by a container
     * in the managed environment, or if the unit of work is already active.
     */
    public void commit(
    ) throws ServiceException {
        if(isContainerManaged())throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "A container managed transaction can't be commited"
        );
        if(! isActive())throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            "No unit of work is active"
        );
        if(this.rollbackOnly) {
            try {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ROLLBACK,
                    "The unit of work has been marked for rollback only"
                );
            } finally {
                afterCompletion(false);
            }
        } else {
            if(isTransactional() && this.userTransaction != null) {
                TransactionManager_1.execute(
                    isOptimistic() ? 
                        this.userTransaction : 
                            new TransactionTerminator(this.userTransaction),
                            this
                );
            } else if (isOptimistic() && this.optimisticTransaction != null) {
                boolean committed = false;
                try {
                    this.optimisticTransaction.commit(this.opimisticTransactionCallback);
                    committed = true;
                } finally {
                    afterCompletion(committed);
                }
            } else  {
                boolean committed = false;
                try {
                    beforeCompletion();
                    committed = true;
                } finally {
                    afterCompletion(committed);
                }
            }
        }
    } 

    /**
     * Roll back the current unit of work.
     * @throws ServiceException if units of work are managed by a container
     * in the managed environment, or if the unit of work is not active.
     */
    public void rollback(
    ) throws ServiceException {
        if(isContainerManaged())throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "A container managed transaction can't be rolled back"
        );
        if(! isActive())throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            "No unit of work is active"
        );
        afterCompletion(false);
    }       

    /**
     * Verify the content of the current unit of work.
     * <p>
     * The state of the objects remains unchanged.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if no unit of work is in progress
     * @exception   ServiceException 
     *              if verification fails
     */    
    public void verify(
    ) throws ServiceException{
        throw new UnsupportedOperationException(
            "Verification only mode not supported yet"
        );
    }

    /**
     * The reset method notifies a provider or plug-in that a new
     * unit of work has started, and that the subsequent business methods on
     * the instance will be invoked in the context of the unit of work. 
     */
    synchronized private void reset(
    ) throws ServiceException {
        SysLog.detail("Unit Of Work","reset");
        this.rollbackOnly = false;
        this.members.clear();
        this.synchronization.afterBegin();
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
    public final boolean isOptimistic(
    ){
        return this.optimistic;
    }

    /**
     * Transactional units of work do not hold data store locks until commit time.
     *
     * @return the value of the Transactional property.
     */
    public final boolean isTransactional(
    ){
        return this.transactional;
    }

    /**
     * Container managed units of work are either non transactional or part of
     * a bigger unit of work.
     *
     * @return the value of the ContainerManaged property.
     */
    protected final boolean isContainerManaged(
    ){
        return this.containerManaged;
    }

    //------------------------------------------------------------------------
    // Implements Synchronization_1_0       
    //------------------------------------------------------------------------

    /**
     * The afterBegin method notifies the unit or work that a transaction
     * has been started 
     */
    public void afterBegin(
    ) throws ServiceException {
        this.active = true;
        if(isContainerManaged()) {
            reset();
        }
    }

    /**
     * The beforeCompletion method notifies a provider or plug-in that a
     * unit of work is about to be committed.
     */
    public void beforeCompletion(
    ) throws ServiceException {
        SysLog.detail("Unit Of Work","flushing");
        if(isOptimistic()) {
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
            ){
                Object_1 member = this.members.get(i);
                TransactionalState_1 memberState = this.getState(member, true);
                if(memberState != null && !memberState.isPrepared()){
                    preparing = true;
                    member.prepare();
                }
            }
        }
        if(preparing) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.QUOTA_EXCEEDED,
            "Maximal number of prepare cycles exceeded",
            new BasicException.Parameter("maximum",PREPARE_CYCLE_LIMIT)
        );
        try {
            this.synchronization.beforeCompletion();
            for(Object_1 member : this.members) {
                member.flush();
            }
            this.synchronization.afterCompletion(true);
        } catch (ServiceException exception) {
            this.synchronization.afterCompletion(false);
            throw exception;
        } catch (RuntimeException exception) {
            this.synchronization.afterCompletion(false);
            throw new ServiceException(exception).log();
        }
    } 

    /**
     * The afterCompletion method notifies a provider or plug-in that a
     * unit of work commit protocol has completed, and tells the instance
     * whether the unit of work has been committed or rolled back. 
     */
    synchronized public void afterCompletion(
        boolean committed
    ) throws ServiceException {
        SysLog.detail(
            "Unit Of Work",
            committed ? 
                "committed" : 
                    this.transactional || this.containerManaged ? "rolled back" : "aborted"
        );
        this.active = false;
        notifyAll();
        for(Object_1 member : this.members) {
            member.afterCompletion(committed);
        }
        if(isContainerManaged()) {
            reset(); // includes this.members.clear();
        } else {
            this.members.clear();
        }
        this.states.clear();
        this.transactionTime = null;
    }

    /**
     * 
     * @param referenceFilter
     * @param candidate
     */
    static final boolean isMemberOfContainer(
        Path referenceFilter,
        Path candidate
    ){
        return 
        candidate != null &&
        candidate.getParent().equals(referenceFilter) &&
        candidate.getBase().indexOf(';') == -1; 
    }


    //------------------------------------------------------------------------
    // Class Include
    //------------------------------------------------------------------------

    /**
     * Include
     */
    static final class Include extends FilteringList implements Selector {

        /**
         * Constructor
         * 
         * @param members 
         * @param referenceFilter
         * @param attributeFilter
         */
        Include(
            List<?> members,
            Path referenceFilter, 
            Selector attributeFilter
        ){
            super(members);
            if(referenceFilter == null) throw new NullPointerException(
                "The referenceFilter must not be null"
            );
            this.referenceFilter = referenceFilter;
            this.attributeFilter = attributeFilter;
        }

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.query.Selector#accept(java.lang.Object)
         */
        public boolean accept(Object candidate) {
            if(!(candidate instanceof Object_1)) {
                return false;
            } else try {
                Object_1 object = (Object_1) candidate;
                return 
                ! object.objIsDeleted() &&
                isMemberOfContainer(this.referenceFilter, object.objGetPath()) && 
                (this.attributeFilter == null ? 
                    object.objIsNew() : 
                        object.objIsDirty() && this.attributeFilter.accept(candidate)
                );
            } catch (ServiceException exception) {
                return false;
            }
        }

        /**
         * 
         */
        private final Path referenceFilter;

        /**
         * 
         */
        private final Selector attributeFilter;

    }


    //------------------------------------------------------------------------
    // Class Exclude
    //------------------------------------------------------------------------

    /**
     * Exclude
     */
    static final class Exclude extends FilteringList implements Selector {

        /**
         * Constructor
         * 
         * @param members 
         * @param referenceFilter
         * @param attributeFilter
         */
        Exclude(
            List<?> members,
            Path referenceFilter, 
            Selector attributeFilter
        ){
            super(members);
            if(referenceFilter == null) throw new NullPointerException(
                "The referenceFilter must not be null"
            );
            this.referenceFilter = referenceFilter;
            this.attributeFilter = attributeFilter != null;
        }

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.query.Selector#accept(java.lang.Object)
         */
        public boolean accept(Object candidate) {
            if(!(candidate instanceof Object_1)) {
                return false;
            } else try {
                Object_1 object = (Object_1) candidate;
                return 
                !object.objIsNew() &&
                (attributeFilter ? object.objIsDirty() : object.objIsDeleted()) &&
                isMemberOfContainer(this.referenceFilter, object.objGetPath());
            } catch (ServiceException exception) {
                return false;
            }
        }

        /**
         * 
         */
        private final Path referenceFilter;

        /**
         * 
         */
        private final boolean attributeFilter;

    }


    //------------------------------------------------------------------------
    // Class OptimisticTransactionCallback
    //------------------------------------------------------------------------
    
    /**
     * Optimistic Transaction Callback
     */
    final class OptimisticTransactionCallback implements Synchronization {

        /* (non-Javadoc)
         * @see javax.transaction.Synchronization#afterCompletion(int)
         */
        public void afterCompletion(int status) {
            try {
                UnitOfWork_1.this.afterCompletion(status == Status.STATUS_COMMITTED);
            } catch (ServiceException exception) {
                LoggerFactory.getLogger(OptimisticTransactionCallback.class).warn(
                    "After completion failure ignored", 
                    exception
                );
            }
        }

        /* (non-Javadoc)
         * @see javax.transaction.Synchronization#beforeCompletion()
         */
        public void beforeCompletion() {
            try {
                UnitOfWork_1.this.beforeCompletion();
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ROLLBACK,
                    "Before completion failure leads to rollback"
                );
            }
        }
        
    }
    
    //------------------------------------------------------------------------
    // Class NonOptimisticTransaction
    //------------------------------------------------------------------------

    /**
     * Non-optimistic UserTransaction Wrapper
     */
    static final class TransactionTerminator implements UserTransaction {

        TransactionTerminator(
            UserTransaction delegate
        ){
            this.delegate = delegate;
        }

        private final UserTransaction delegate;

        /* (non-Javadoc)
         * @see javax.transaction.UserTransaction#begin()
         */
        public void begin() throws NotSupportedException, SystemException {
            SysLog.trace(
                "phase",
                "Please ignore the previous 'begin' and the next 'afterBegin' entry"
            );
        }

        /* (non-Javadoc)
         * @see javax.transaction.UserTransaction#commit()
         */
        public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
            this.delegate.commit();
        }

        /* (non-Javadoc)
         * @see javax.transaction.UserTransaction#getStatus()
         */
        public int getStatus() throws SystemException {
            return this.delegate.getStatus();
        }

        /* (non-Javadoc)
         * @see javax.transaction.UserTransaction#rollback()
         */
        public void rollback() throws IllegalStateException, SecurityException, SystemException {
            this.delegate.rollback();
        }

        /* (non-Javadoc)
         * @see javax.transaction.UserTransaction#setRollbackOnly()
         */
        public void setRollbackOnly() throws IllegalStateException, SystemException {
            this.delegate.setRollbackOnly();
        }

        /* (non-Javadoc)
         * @see javax.transaction.UserTransaction#setTransactionTimeout(int)
         */
        public void setTransactionTimeout(int seconds) throws SystemException {
            this.delegate.setTransactionTimeout(seconds);
        }

    }

}
