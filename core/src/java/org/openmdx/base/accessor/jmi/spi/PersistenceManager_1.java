/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PersistenceManager_1.java,v 1.11 2008/02/19 13:42:54 hburger Exp $
 * Description: PersistenceManager_1 
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/19 13:42:54 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006-2008, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.spi;

import java.util.Collection;

import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.JDOOptimisticVerificationException;
import javax.jdo.JDOUnsupportedOptionException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.RefObject;
import javax.transaction.Synchronization;

import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_3;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.object.spi.AbstractPersistenceManager;
import org.openmdx.base.object.spi.InstanceLifecycleNotifier;
import org.openmdx.base.transaction.Synchronization_1_0;
import org.openmdx.base.transaction.UnitOfWork_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;

/**
 * PersistenceManager_1
 */
class PersistenceManager_1
    extends AbstractPersistenceManager
{

    /**
     * Constructor 
     * 
     * @param factory 
     * @param notifier 
     * @param marshaller
     * @param unitOfWork
     */
    PersistenceManager_1(
        PersistenceManagerFactory factory, 
        InstanceLifecycleNotifier notifier,
        RefPackage_1_0 delegate
    ) {
        super(
            factory,
            notifier,
            null, // connectionUsername
            null // connectionPassword
        );
        this.delegate = delegate;
        this.transaction = new Transaction_1(delegate.refUnitOfWork());
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#isClosed()
     */
    public boolean isClosed(
    ) {
        return this.delegate == null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#close()
     */
    public void close() {
        this.delegate = null;
        this.transaction = null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#currentTransaction()
     */
    public Transaction currentTransaction() {
        return this.transaction; 
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evict(java.lang.Object)
     */
    public void evict(Object pc) {
        // The hint is ignored at the moment...
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll()
     */
    public void evictAll() {
        if(this.delegate instanceof RefPackage_1_3) {
            ((RefPackage_1_3)this.delegate).clear();
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refresh(java.lang.Object)
     */
    public void refresh(Object pc) {
        if(pc instanceof RefObject_1_0) try {
            ((RefObject_1_0)pc).refRefresh();
        } catch (JmiServiceException exception) {
            throw new JDOException(
                "Refresh failure",
                exception,
                pc
            );
        }
    } 

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll()
     */
    public void refreshAll() {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery()
     */
    public Query newQuery() {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Object)
     */
    public Query newQuery(Object compiled) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String)
     */
    public Query newQuery(String query) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String, java.lang.Object)
     */
    public Query newQuery(String language, Object query) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent)
     */
    public Query newQuery(Extent cln) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, Collection cln) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, String filter) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, Collection cln, String filter) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent, java.lang.String)
     */
    public Query newQuery(Extent cln, String filter) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newNamedQuery(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newNamedQuery(Class cls, String queryName) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class, boolean)
     */
    @SuppressWarnings("unchecked")
    public Extent getExtent(Class persistenceCapableClass, boolean subclasses) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Extent getExtent(Class persistenceCapableClass) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object, boolean)
     */
    public Object getObjectById(Object oid, boolean validate) {
        return this.delegate.refObject(oid.toString());
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object)
     */
    public Object getObjectById(Object oid) {
        return this.getObjectById(oid, true);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectId(java.lang.Object)
     */
    public Object getObjectId(Object pc) {
        return pc instanceof PersistenceCapable ?
            ((PersistenceCapable)pc).jdoGetObjectId() :
            null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getTransactionalObjectId(java.lang.Object)
     */
    public Object getTransactionalObjectId(Object pc) {
        return pc instanceof PersistenceCapable ?
            ((PersistenceCapable)pc).jdoGetTransactionalObjectId() :
            null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newObjectIdInstance(java.lang.Class, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public Object newObjectIdInstance(Class pcClass, Object key) {
        return new Path(key.toString());
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistent(java.lang.Object)
     */
    public Object makePersistent(Object pc) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistent(java.lang.Object)
     */
    public void deletePersistent(Object pc) {
        if(pc instanceof PersistenceCapable && pc instanceof RefObject) {
            PersistenceCapable jdoObject = (PersistenceCapable) pc;
            if(this != jdoObject.jdoGetPersistenceManager()) throw new JDOUserException(
                "The object is managed b a different PersistnceManager",
                jdoObject
            );
            RefObject jmiObject = (RefObject) pc;
            try {
                jmiObject.refDelete();
            } catch (JmiServiceException exception) {
                throw new JDOUserException(
                    "Deletion failure",
                    exception,
                    jdoObject
                );
            }
        } else throw new JDOUserException(
            "The object to be deleted is not an instance of PersistenceCapable and RefObject"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object)
     */
    public void makeTransient(Object pc) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object, boolean)
     */
    public void makeTransient(Object pc, boolean useFetchPlan) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public void makeTransientAll(Collection pcs, boolean useFetchPlan) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[], boolean)
     */
    public void makeTransientAll(Object[] pcs, boolean useFetchPlan) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactional(java.lang.Object)
     */
    public void makeTransactional(Object pc) {
        if(pc instanceof PersistenceCapable && pc instanceof RefObject) {
            PersistenceCapable jdoObject = (PersistenceCapable) pc;
            if(this != jdoObject.jdoGetPersistenceManager()) throw new JDOUserException(
                "The object is managed b a different PersistnceManager",
                jdoObject
            );
            RefObject_1_0 jmiObject = (RefObject_1_0) pc;
            try {
                jmiObject.refAddToUnitOfWork();
            } catch (JmiServiceException exception) {
                throw new JDOUserException(
                    "Make transactional failure",
                    exception,
                    jdoObject
                );
            }
        } else throw new JDOUserException(
            "The object to be made transactional is not an instance of PersistenceCapable and RefObject"
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactional(java.lang.Object)
     */
    public void makeNontransactional(Object pc) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object)
     */
    public void retrieve(Object pc) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void retrieveAll(Collection pcs) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public void retrieveAll(Collection pcs, boolean DFGOnly) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[])
     */
    public void retrieveAll(Object[] pcs) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[], boolean)
     */
    public void retrieveAll(Object[] pcs, boolean DFGOnly) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectIdClass(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Class getObjectIdClass(Class cls) {
        return RefObject_1_0.class.isAssignableFrom(cls) ? Path.class : null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopy(java.lang.Object)
     */
    public Object detachCopy(Object pc) {
        // TODO Auto-generated method stub
        return null;
    }
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#attachCopy(java.lang.Object, boolean)
     */
    public Object attachCopy(Object pc, boolean makeTransactional) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#flush()
     */
    public void flush() {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#checkConsistency()
     */
    public void checkConsistency() {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getFetchPlan()
     */
    public FetchPlan getFetchPlan() {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newInstance(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Object newInstance(Class pcClass) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getSequence(java.lang.String)
     */
    public Sequence getSequence(String name) {
        throw new UnsupportedOperationException(OPENMDX_1_JDO);            
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getMultithreaded()
     */
    public boolean getMultithreaded(
    ) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.AbstractPersistenceManager#getDataStoreConnection()
     */
    public JDOConnection getDataStoreConnection() {
        Object candidate = getPersistenceManagerFactory();
        return candidate instanceof JDOConnection ? (JDOConnection) candidate : null;
    }

    
    //------------------------------------------------------------------------
    // Class Transaction_1
    //------------------------------------------------------------------------

    /**
     * Transaction_1
     */
    class Transaction_1
        implements Transaction, Synchronization_1_0
    {

        /**
         * Constructor 
         */
        public Transaction_1(
            UnitOfWork_1_0 delegate
        ) {
            this.delegate = delegate;
        }

        /**
         * 
         */
        private final UnitOfWork_1_0 delegate;

        /**
         * 
         */
        private boolean rollbackOnly = false;
        
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#begin()
         */
        public void begin(
        ) {
            try {
                this.delegate.begin();
            } catch (ServiceException exception) {
                throw new JDOUserException(
                    "Transaction begin failed",
                    exception
                );
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#commit()
         */
        public void commit() {
            if(this.rollbackOnly) {
                try {
                    this.delegate.rollback();
                    throw new JDOUserException(
                        "Unit of work is rollback-only"
                    );
                } catch (ServiceException rollbackException) {
                    throw new JDOUserException(
                        "Unit of work is rollback-only, but rollback failed",
                        rollbackException
                    );
                }
            } else {
                try {
                    this.delegate.commit();
                } catch (ServiceException exception) {                
                    BasicException initialCause = exception.getCause(null);   
                    throw initialCause.getExceptionCode() == BasicException.Code.CONCURRENT_ACCESS_FAILURE ?
                        new JDOOptimisticVerificationException(
                            exception.getMessage(),
                            new Throwable[]{
                                new JDOOptimisticVerificationException(
                                    initialCause.getDescription() + ": " + initialCause.getParameter("path"),
                                    new Throwable[]{initialCause}
                                )
                            }
                        ) : new JDOFatalDataStoreException(
                            "Transaction commit failed",
                            exception
                    );
                }
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#rollback()
         */
        public void rollback() {
            try {
                this.delegate.rollback();
            } catch (ServiceException exception) {
                throw new JDOFatalDataStoreException(
                    "Transaction rollback failed",
                    exception
                );
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#isActive()
         */
        public boolean isActive() {
           return this.delegate.isActive();
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getRollbackOnly()
         */
        public boolean getRollbackOnly() {
            return this.rollbackOnly;
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setRollbackOnly()
         */
        public void setRollbackOnly() {
            this.rollbackOnly = true;
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setNontransactionalRead(boolean)
         */
        public void setNontransactionalRead(boolean nontransactionalRead) {
            if(nontransactionalRead != getNontransactionalRead()) throw new JDOUnsupportedOptionException(
                "Non-transactional reads are enabled for optimistic or non-transactional units of work"
            );
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getNontransactionalRead()
         */
        public boolean getNontransactionalRead() {
            return this.delegate.isOptimistic() || !this.delegate.isTransactional();
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setNontransactionalWrite(boolean)
         */
        public void setNontransactionalWrite(boolean nontransactionalWrite) {
            if(nontransactionalWrite != getNontransactionalWrite()) throw new JDOUnsupportedOptionException(
                "Non-transactional writes are enabled for non-transactional units of work"
            );
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getNontransactionalWrite()
         */
        public boolean getNontransactionalWrite() {
            return !this.delegate.isTransactional();
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setRetainValues(boolean)
         */
        public void setRetainValues(boolean retainValues) {
            if(retainValues != getRetainValues()) throw new JDOUnsupportedOptionException(
                "The retain values flag can't be modified"
            );
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getRetainValues()
         */
        public boolean getRetainValues() {
            return false;
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setRestoreValues(boolean)
         */
        public void setRestoreValues(boolean restoreValues) {
            if(restoreValues != getRestoreValues()) throw new JDOUnsupportedOptionException(
                "The restore values flag can't be modified"
            );
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getRestoreValues()
         */
        public boolean getRestoreValues() {
            return false;
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setOptimistic(boolean)
         */
        public void setOptimistic(boolean optimistic) {
            if(optimistic != getOptimistic()) throw new JDOUnsupportedOptionException(
                "The optimistic flag can't be modified"
            );
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getOptimistic()
         */
        public boolean getOptimistic() {
            return this.delegate.isOptimistic();
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setSynchronization(javax.transaction.Synchronization)
         */
        public void setSynchronization(Synchronization sync) {
            throw new UnsupportedOperationException(OPENMDX_1_JDO);            
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getSynchronization()
         */
        public Synchronization getSynchronization() {
            throw new UnsupportedOperationException(OPENMDX_1_JDO);            
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getPersistenceManager()
         */
        public PersistenceManager getPersistenceManager() {
            return PersistenceManager_1.this;
        }

        /**
         * @throws ServiceException
         * @see org.openmdx.base.transaction.Synchronization_1_0#afterBegin()
         */
        public void afterBegin()
            throws ServiceException {
            this.delegate.afterBegin();
        }

        /**
         * @param committed
         * @throws ServiceException
         * @see org.openmdx.base.transaction.Synchronization_1_0#afterCompletion(boolean)
         */
        public void afterCompletion(boolean committed)
            throws ServiceException {
            this.delegate.afterCompletion(committed);
        }

        /**
         * @throws ServiceException
         * @see org.openmdx.base.transaction.Synchronization_1_0#beforeCompletion()
         */
        public void beforeCompletion()
            throws ServiceException {
            this.delegate.beforeCompletion();
        }

    }
    
    
    //------------------------------------------------------------------------
    // Instance Members
    //------------------------------------------------------------------------

    /**
     * 
     */
    private Transaction transaction;
    
    /**
     * 
     */
    private RefPackage_1_0 delegate;
    
    
    //------------------------------------------------------------------------
    // Class Members
    //------------------------------------------------------------------------
    
    /**
     * Only a subset of the JDO methods are implemented in openMDX 1
     */
    protected static final String OPENMDX_1_JDO = 
        "This JDO operation is not supported in openMDX 1 compatibility mode";

}