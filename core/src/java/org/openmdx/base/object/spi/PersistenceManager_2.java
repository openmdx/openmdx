/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: PersistenceManager_2.java,v 1.10 2008/02/08 16:51:40 hburger Exp $
 * Description: PersistenceManager_2 
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/08 16:51:40 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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

package org.openmdx.base.object.spi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.JDOUnsupportedOptionException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.transaction.Synchronization;
import org.openmdx.kernel.callback.CloseCallback;

/**
 * PersistenceManager_2
 *
 * @since openMDX 2.0
 */
public class PersistenceManager_2
    implements PersistenceManager
{

    /**
     * Constructor 
     * <p>
     * The parameters may be kept by the instance.
     *
     * @param factory
     * @param notifier
     */
    public PersistenceManager_2(
        PersistenceManagerFactory factory,
        InstanceLifecycleNotifier notifier
    ){
        this.persistenceManagerFactory = factory;
        this.instanceLifecycleNotifier = notifier;
        setIgnoreCache(factory.getIgnoreCache());
        setMultithreaded(factory.getMultithreaded());
        setDetachAllOnCommit(factory.getDetachAllOnCommit());
        this.unitOfWork = new UnitOfWork(factory);
    }

    /**
     * Constructor 
     * <p>
     * The parameters may be kept by the instance.
     *
     * @param factory
     * @param notifier
     * @param connectionUsername
     * @param connectionPassword
     */
    public PersistenceManager_2(
        PersistenceManagerFactory factory,
        InstanceLifecycleNotifier notifier,
        String connectionUsername,
        String connectionPassword
    ){
        this(
            factory,
            notifier
        );
    }

    /**
     * 
     */
    private PersistenceManagerFactory persistenceManagerFactory;
    
    /**
     * 
     */
    protected final InstanceLifecycleNotifier instanceLifecycleNotifier;

    /**
     * 
     */
    private final Map<Object,Object> userObjects = new HashMap<Object,Object>();
    
    /**
     * 
     */
    private final Transaction unitOfWork;

    /**
     * 
     */
    private boolean ignoreCache;

    /**
     * 
     */
    private boolean detachAllOnCommit;
    
    /**
     * The lock object for multithread support
     */
    private Object lock = null;
    
    
    //------------------------------------------------------------------------
    // Implements PersistenceManager
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#isClosed()
     */
    public boolean isClosed() {
        return this.persistenceManagerFactory == null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#close()
     */
    public void close() {
        if(!isClosed()) {
            if(currentTransaction().isActive()) throw new JDOUserException(
                "Persistence manager with an active unit of work can't be closed"
            );
            this.instanceLifecycleNotifier.close();
            if(persistenceManagerFactory instanceof CloseCallback) {
                ((CloseCallback)this.persistenceManagerFactory).postClose(this);
            }
            this.persistenceManagerFactory = null;
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#currentTransaction()
     */
    public Transaction currentTransaction() {
        return this.unitOfWork;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evict(java.lang.Object)
     */
    public void evict(Object pc) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.lang.Object[])
     */
    public void evictAll(Object[] pcs) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.util.Collection)
     */
    public void evictAll(Collection pcs) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll()
     */
    public void evictAll() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refresh(java.lang.Object)
     */
    public void refresh(Object pc) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.lang.Object[])
     */
    public void refreshAll(Object[] pcs) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.util.Collection)
     */
    public void refreshAll(Collection pcs) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll()
     */
    public void refreshAll() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(javax.jdo.JDOException)
     */
    public void refreshAll(JDOException jdoe) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery()
     */
    public Query newQuery() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Object)
     */
    public Query newQuery(Object compiled) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String)
     */
    public Query newQuery(String query) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String, java.lang.Object)
     */
    public Query newQuery(String language, Object query) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class)
     */
    public Query newQuery(Class cls) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent)
     */
    public Query newQuery(Extent cln) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection)
     */
    public Query newQuery(Class cls, Collection cln) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.lang.String)
     */
    public Query newQuery(Class cls, String filter) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection, java.lang.String)
     */
    public Query newQuery(Class cls, Collection cln, String filter) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent, java.lang.String)
     */
    public Query newQuery(Extent cln, String filter) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newNamedQuery(java.lang.Class, java.lang.String)
     */
    public Query newNamedQuery(Class cls, String queryName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class, boolean)
     */
    public Extent getExtent(Class persistenceCapableClass, boolean subclasses) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class)
     */
    public Extent getExtent(Class persistenceCapableClass) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object, boolean)
     */
    public Object getObjectById(Object oid, boolean validate) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Class, java.lang.Object)
     */
    public Object getObjectById(Class cls, Object key) {
        return getObjectById (newObjectIdInstance (cls, key), true);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object)
     */
    public Object getObjectById(Object oid) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectId(java.lang.Object)
     */
    public Object getObjectId(Object pc) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getTransactionalObjectId(java.lang.Object)
     */
    public Object getTransactionalObjectId(Object pc) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newObjectIdInstance(java.lang.Class, java.lang.Object)
     */
    public Object newObjectIdInstance(Class pcClass, Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection, boolean)
     */
    public Collection getObjectsById(Collection oids, boolean validate) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection)
     */
    public Collection getObjectsById(Collection oids) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[], boolean)
     */
    public Object[] getObjectsById(Object[] oids, boolean validate) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[])
     */
    public Object[] getObjectsById(Object[] oids) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistent(java.lang.Object)
     */
    public Object makePersistent(Object pc) {
        // TODO Auto-generated method stub
        return pc;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistentAll(java.lang.Object[])
     */
    public Object[] makePersistentAll(Object[] pcs) {
        // TODO Auto-generated method stub
        return pcs;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistentAll(java.util.Collection)
     */
    public Collection makePersistentAll(Collection pcs) {
        // TODO Auto-generated method stub
        return pcs;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistent(java.lang.Object)
     */
    public void deletePersistent(Object pc) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.lang.Object[])
     */
    public void deletePersistentAll(Object[] pcs) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.util.Collection)
     */
    public void deletePersistentAll(Collection pcs) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object)
     */
    public void makeTransient(Object pc) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[])
     */
    public void makeTransientAll(Object[] pcs) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection)
     */
    public void makeTransientAll(Collection pcs) {
        // TODO Auto-generated method stub

    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object, boolean)
     */
    public void makeTransient(Object pc, boolean useFetchPlan) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection, boolean)
     */
    public void makeTransientAll(Collection pcs, boolean useFetchPlan) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[], boolean)
     */
    public void makeTransientAll(Object[] pcs, boolean useFetchPlan) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactional(java.lang.Object)
     */
    public void makeTransactional(Object pc) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.lang.Object[])
     */
    public void makeTransactionalAll(Object[] pcs) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.util.Collection)
     */
    public void makeTransactionalAll(Collection pcs) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactional(java.lang.Object)
     */
    public void makeNontransactional(Object pc) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.lang.Object[])
     */
    public void makeNontransactionalAll(Object[] pcs) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.util.Collection)
     */
    public void makeNontransactionalAll(Collection pcs) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object)
     */
    public void retrieve(Object pc) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object, boolean)
     */
    public void retrieve(Object pc, boolean useFetchPlan) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection)
     */
    public void retrieveAll(Collection pcs) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection, boolean)
     */
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
     * @see javax.jdo.PersistenceManager#setUserObject(java.lang.Object)
     */
    public void setUserObject(Object o) {
        putUserObject(null, o);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getUserObject()
     */
    public Object getUserObject() {
        return getUserObject(null);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getPersistenceManagerFactory()
     */
    public PersistenceManagerFactory getPersistenceManagerFactory() {
        return this.persistenceManagerFactory;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectIdClass(java.lang.Class)
     */
    public Class getObjectIdClass(Class cls) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setMultithreaded(boolean)
     */
    public void setMultithreaded(boolean flag) {
        if(flag) throw new JDOUnsupportedOptionException(
            "javax.jdo.option.Multithreaded not supported yet"
        ); //... TODO
    }

    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getMultithreaded()
     */
    public final boolean getMultithreaded() {
        return this.lock != null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setIgnoreCache(boolean)
     */
    public void setIgnoreCache(boolean flag) {
        this.ignoreCache = flag;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getIgnoreCache()
     */
    public boolean getIgnoreCache() {
        return this.ignoreCache;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDetachAllOnCommit()
     */
    public boolean getDetachAllOnCommit() {
        return this.detachAllOnCommit;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setDetachAllOnCommit(boolean)
     */
    public void setDetachAllOnCommit(boolean flag) {
        this.detachAllOnCommit = flag;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopy(java.lang.Object)
     */
    public Object detachCopy(Object pc) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopyAll(java.util.Collection)
     */
    public Collection detachCopyAll(Collection pcs) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopyAll(java.lang.Object[])
     */
    public Object[] detachCopyAll(Object[] pcs) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#attachCopy(java.lang.Object, boolean)
     */
    public Object attachCopy(Object pc, boolean makeTransactional) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#attachCopyAll(java.util.Collection, boolean)
     */
    public Collection attachCopyAll(Collection pcs, boolean makeTransactional) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#attachCopyAll(java.lang.Object[], boolean)
     */
    public Object[] attachCopyAll(Object[] pcs, boolean makeTransactional) {
        // TODO Auto-generated method stub
        return null;
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#putUserObject(java.lang.Object, java.lang.Object)
     */
    public Object putUserObject(Object key, Object val) {
        return this.userObjects.put(key, val);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getUserObject(java.lang.Object)
     */
    public Object getUserObject(Object key) {
        return this.userObjects.get(key);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#removeUserObject(java.lang.Object)
     */
    public Object removeUserObject(Object key) {
        return this.userObjects.remove(key);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#flush()
     */
    public void flush() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#checkConsistency()
     */
    public void checkConsistency() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getFetchPlan()
     */
    public FetchPlan getFetchPlan() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newInstance(java.lang.Class)
     */
    public Object newInstance(Class pcClass) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getSequence(java.lang.String)
     */
    public Sequence getSequence(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDataStoreConnection()
     */
    public JDOConnection getDataStoreConnection() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#addInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener, java.lang.Class[])
     */
    public void addInstanceLifecycleListener(
        InstanceLifecycleListener listener,
        Class[] classes
    ) {
        this.instanceLifecycleNotifier.addInstanceLifecycleListener(listener, classes);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#removeInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener)
     */
    public void removeInstanceLifecycleListener(
        InstanceLifecycleListener listener
    ) {
        this.instanceLifecycleNotifier.removeInstanceLifecycleListener(listener);
    }

    //------------------------------------------------------------------------
    // Class UnitOfWork
    //------------------------------------------------------------------------
    
    /**
     * 
     */
    class UnitOfWork
        implements Transaction
    {
    
        /**
         * Constructor 
         */
        UnitOfWork(
            PersistenceManagerFactory persistenceManagerFactory
        ) {
            this.restoreValues = persistenceManagerFactory.getRestoreValues();
            this.retainValues = persistenceManagerFactory.getRetainValues();
            this.nontransactionalWrite = persistenceManagerFactory.getNontransactionalWrite();
            this.nontransactionalRead = persistenceManagerFactory.getNontransactionalRead();
            this.optimistic = persistenceManagerFactory.getOptimistic();
        }
        
        /**
         * 
         */
        private boolean restoreValues;
    
        /**
         * 
         */
        private boolean retainValues;
    
        /**
         * 
         */
        private boolean nontransactionalWrite;
    
        /**
         * 
         */
        private boolean nontransactionalRead;
    
        /**
         * 
         */
        private boolean optimistic;
    
        /**
         * 
         */
        private Synchronization synchronization;
        
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#begin()
         */
        public void begin() {
            // TODO Auto-generated method stub
        }
    
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#commit()
         */
        public void commit() {
            // TODO Auto-generated method stub
        }
    
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#rollback()
         */
        public void rollback() {
            // TODO Auto-generated method stub
        }
    
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#isActive()
         */
        public boolean isActive(
        ){
            // TODO Auto-generated method stub
            return false;
        }
    
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getRollbackOnly()
         */
        public boolean getRollbackOnly() {
            // TODO Auto-generated method stub
            return false;
        }
    
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setRollbackOnly()
         */
        public void setRollbackOnly() {
            // TODO Auto-generated method stub
        }
    
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setNontransactionalRead(boolean)
         */
        public void setNontransactionalRead(
            boolean nontransactionalRead
        ) {
            this.nontransactionalRead = nontransactionalRead;
        }
    
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getNontransactionalRead()
         */
        public boolean getNontransactionalRead() {
            return this.nontransactionalRead;
        }
    
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setNontransactionalWrite(boolean)
         */
        public void setNontransactionalWrite(
            boolean nontransactionalWrite
        ) {
            this.nontransactionalWrite = nontransactionalWrite;
        }
    
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getNontransactionalWrite()
         */
        public boolean getNontransactionalWrite() {
            return this.nontransactionalWrite;
        }
    
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setRetainValues(boolean)
         */
        public void setRetainValues(
            boolean retainValues
        ) {
            this.retainValues = retainValues;
        }
    
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getRetainValues()
         */
        public boolean getRetainValues() {
            return this.retainValues;
        }
    
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setRestoreValues(boolean)
         */
        public void setRestoreValues(
            boolean restoreValues
        ) {
            this.restoreValues = restoreValues;
        }
    
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getRestoreValues()
         */
        public boolean getRestoreValues() {
            return this.restoreValues;
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
         * @see javax.jdo.Transaction#getOptimistic()
         */
        public boolean getOptimistic() {
            return this.optimistic;
        }
    
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#setSynchronization(javax.transaction.Synchronization)
         */
        public void setSynchronization(
            Synchronization sync
        ) {
            this.synchronization = sync;
        }
    
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getSynchronization()
         */
        public Synchronization getSynchronization(
        ){
            return this.synchronization;
        }
    
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#getPersistenceManager()
         */
        public PersistenceManager getPersistenceManager(
        ) {
            return PersistenceManager_2.this;
        }
    
    }
    
}
