/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SynchronizedPersistenceManager_2.java,v 1.5 2007/02/16 14:26:05 hburger Exp $
 * Description: SynchronizedPersistenceManager_2 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/02/16 14:26:05 $
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

import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.listener.InstanceLifecycleListener;

/**
 * SynchronizedPersistenceManager_2
 * 
 * @since openMDX 2.0
 */
public class SynchronizedPersistenceManager_2
    extends AbstractPersistenceManager
{

    /**
     * Constructor 
     *
     * @param factory
     * @param notifier
     * @param connectionUsername
     * @param connectionPassword
     */
    public SynchronizedPersistenceManager_2(
        PersistenceManagerFactory factory,
        InstanceLifecycleNotifier notifier,
        String connectionUsername,
        String connectionPassword
     ) {
        super(factory, notifier, connectionUsername, connectionPassword);
    }

    
    //------------------------------------------------------------------------
    // Implements PersistenceManager
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getMultithreaded()
     */
    public final boolean getMultithreaded() {
        return true;
    }


    //------------------------------------------------------------------------
    // Extends PersistenceManager_2
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#addInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener, java.lang.Class[])
     */
    public synchronized void addInstanceLifecycleListener(InstanceLifecycleListener listener, Class[] classes) {
        super.addInstanceLifecycleListener(listener, classes);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#attachCopy(java.lang.Object, boolean)
     */
    public synchronized Object attachCopy(Object pc, boolean makeTransactional) {
        
        return super.attachCopy(pc, makeTransactional);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#attachCopyAll(java.util.Collection, boolean)
     */
    public synchronized Collection attachCopyAll(Collection pcs, boolean makeTransactional) {
        
        return super.attachCopyAll(pcs, makeTransactional);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#attachCopyAll(java.lang.Object[], boolean)
     */
    public synchronized Object[] attachCopyAll(Object[] pcs, boolean makeTransactional) {
        
        return super.attachCopyAll(pcs, makeTransactional);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#checkConsistency()
     */
    public synchronized void checkConsistency() {
        
        super.checkConsistency();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#close()
     */
    public synchronized void close() {
        
        super.close();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#currentTransaction()
     */
    public synchronized Transaction currentTransaction() {
        
        return super.currentTransaction();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#deletePersistent(java.lang.Object)
     */
    public synchronized void deletePersistent(Object pc) {
        
        super.deletePersistent(pc);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#deletePersistentAll(java.util.Collection)
     */
    public synchronized void deletePersistentAll(Collection pcs) {
        
        super.deletePersistentAll(pcs);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#deletePersistentAll(java.lang.Object[])
     */
    public synchronized void deletePersistentAll(Object[] pcs) {
        
        super.deletePersistentAll(pcs);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#detachCopy(java.lang.Object)
     */
    public synchronized Object detachCopy(Object pc) {
        
        return super.detachCopy(pc);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#detachCopyAll(java.util.Collection)
     */
    public synchronized Collection detachCopyAll(Collection pcs) {
        
        return super.detachCopyAll(pcs);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#detachCopyAll(java.lang.Object[])
     */
    public synchronized Object[] detachCopyAll(Object[] pcs) {
        
        return super.detachCopyAll(pcs);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#evict(java.lang.Object)
     */
    public synchronized void evict(Object pc) {
        
        super.evict(pc);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#evictAll()
     */
    public synchronized void evictAll() {
        
        super.evictAll();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#evictAll(java.util.Collection)
     */
    public synchronized void evictAll(Collection pcs) {
        
        super.evictAll(pcs);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#evictAll(java.lang.Object[])
     */
    public synchronized void evictAll(Object[] pcs) {
        
        super.evictAll(pcs);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#flush()
     */
    public synchronized void flush() {
        
        super.flush();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getDataStoreConnection()
     */
    public synchronized JDOConnection getDataStoreConnection() {
        
        return super.getDataStoreConnection();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getExtent(java.lang.Class, boolean)
     */
    public synchronized Extent getExtent(Class persistenceCapableClass, boolean subclasses) {
        
        return super.getExtent(persistenceCapableClass, subclasses);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getExtent(java.lang.Class)
     */
    public synchronized Extent getExtent(Class persistenceCapableClass) {
        
        return super.getExtent(persistenceCapableClass);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getFetchPlan()
     */
    public synchronized FetchPlan getFetchPlan() {
        
        return super.getFetchPlan();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getIgnoreCache()
     */
    public synchronized boolean getIgnoreCache() {
        
        return super.getIgnoreCache();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getObjectById(java.lang.Class, java.lang.Object)
     */
    public synchronized Object getObjectById(Class cls, Object key) {
        
        return super.getObjectById(cls, key);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getObjectById(java.lang.Object, boolean)
     */
    public synchronized Object getObjectById(Object oid, boolean validate) {
        
        return super.getObjectById(oid, validate);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getObjectById(java.lang.Object)
     */
    public synchronized Object getObjectById(Object oid) {
        
        return super.getObjectById(oid);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getObjectId(java.lang.Object)
     */
    public synchronized Object getObjectId(Object pc) {
        
        return super.getObjectId(pc);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getObjectIdClass(java.lang.Class)
     */
    public synchronized Class getObjectIdClass(Class cls) {
        
        return super.getObjectIdClass(cls);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getObjectsById(java.util.Collection, boolean)
     */
    public synchronized Collection getObjectsById(Collection oids, boolean validate) {
        
        return super.getObjectsById(oids, validate);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getObjectsById(java.util.Collection)
     */
    public synchronized Collection getObjectsById(Collection oids) {
        
        return super.getObjectsById(oids);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getObjectsById(java.lang.Object[], boolean)
     */
    public synchronized Object[] getObjectsById(Object[] oids, boolean validate) {
        
        return super.getObjectsById(oids, validate);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getObjectsById(java.lang.Object[])
     */
    public synchronized Object[] getObjectsById(Object[] oids) {
        
        return super.getObjectsById(oids);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getPersistenceManagerFactory()
     */
    public synchronized PersistenceManagerFactory getPersistenceManagerFactory() {
        
        return super.getPersistenceManagerFactory();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getSequence(java.lang.String)
     */
    public synchronized Sequence getSequence(String name) {
        
        return super.getSequence(name);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getTransactionalObjectId(java.lang.Object)
     */
    public synchronized Object getTransactionalObjectId(Object pc) {
        
        return super.getTransactionalObjectId(pc);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getUserObject()
     */
    public synchronized Object getUserObject() {
        
        return super.getUserObject();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#getUserObject(java.lang.Object)
     */
    public synchronized Object getUserObject(Object key) {
        
        return super.getUserObject(key);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#isClosed()
     */
    public synchronized boolean isClosed() {
        
        return super.isClosed();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#makeNontransactional(java.lang.Object)
     */
    public synchronized void makeNontransactional(Object pc) {
        
        super.makeNontransactional(pc);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#makeNontransactionalAll(java.util.Collection)
     */
    public synchronized void makeNontransactionalAll(Collection pcs) {
        
        super.makeNontransactionalAll(pcs);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#makeNontransactionalAll(java.lang.Object[])
     */
    public synchronized void makeNontransactionalAll(Object[] pcs) {
        
        super.makeNontransactionalAll(pcs);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#makePersistent(java.lang.Object)
     */
    public synchronized Object makePersistent(Object pc) {
        return super.makePersistent(pc);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#makePersistentAll(java.util.Collection)
     */
    public synchronized Collection makePersistentAll(Collection pcs) {
        return super.makePersistentAll(pcs);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#makePersistentAll(java.lang.Object[])
     */
    public synchronized Object[] makePersistentAll(Object[] pcs) {
        return super.makePersistentAll(pcs);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#makeTransactional(java.lang.Object)
     */
    public synchronized void makeTransactional(Object pc) {
        
        super.makeTransactional(pc);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#makeTransactionalAll(java.util.Collection)
     */
    public synchronized void makeTransactionalAll(Collection pcs) {
        
        super.makeTransactionalAll(pcs);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#makeTransactionalAll(java.lang.Object[])
     */
    public synchronized void makeTransactionalAll(Object[] pcs) {
        
        super.makeTransactionalAll(pcs);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#makeTransient(java.lang.Object)
     */
    public synchronized void makeTransient(Object pc) {
        
        super.makeTransient(pc);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#makeTransientAll(java.util.Collection)
     */
    public synchronized void makeTransientAll(Collection pcs) {
        
        super.makeTransientAll(pcs);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#makeTransientAll(java.lang.Object[])
     */
    public synchronized void makeTransientAll(Object[] pcs) {
        
        super.makeTransientAll(pcs);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#newInstance(java.lang.Class)
     */
    public synchronized Object newInstance(Class pcClass) {
        
        return super.newInstance(pcClass);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#newNamedQuery(java.lang.Class, java.lang.String)
     */
    public synchronized Query newNamedQuery(Class cls, String queryName) {
        
        return super.newNamedQuery(cls, queryName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#newObjectIdInstance(java.lang.Class, java.lang.Object)
     */
    public synchronized Object newObjectIdInstance(Class pcClass, Object key) {
        
        return super.newObjectIdInstance(pcClass, key);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#newQuery()
     */
    public synchronized Query newQuery() {
        
        return super.newQuery();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#newQuery(java.lang.Class, java.util.Collection, java.lang.String)
     */
    public synchronized Query newQuery(Class cls, Collection cln, String filter) {
        
        return super.newQuery(cls, cln, filter);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#newQuery(java.lang.Class, java.util.Collection)
     */
    public synchronized Query newQuery(Class cls, Collection cln) {
        
        return super.newQuery(cls, cln);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#newQuery(java.lang.Class, java.lang.String)
     */
    public synchronized Query newQuery(Class cls, String filter) {
        
        return super.newQuery(cls, filter);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#newQuery(java.lang.Class)
     */
    public synchronized Query newQuery(Class cls) {
        
        return super.newQuery(cls);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#newQuery(javax.jdo.Extent, java.lang.String)
     */
    public synchronized Query newQuery(Extent cln, String filter) {
        
        return super.newQuery(cln, filter);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#newQuery(javax.jdo.Extent)
     */
    public synchronized Query newQuery(Extent cln) {
        
        return super.newQuery(cln);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#newQuery(java.lang.Object)
     */
    public synchronized Query newQuery(Object compiled) {
        
        return super.newQuery(compiled);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#newQuery(java.lang.String, java.lang.Object)
     */
    public synchronized Query newQuery(String language, Object query) {
        
        return super.newQuery(language, query);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#newQuery(java.lang.String)
     */
    public synchronized Query newQuery(String query) {
        
        return super.newQuery(query);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#putUserObject(java.lang.Object, java.lang.Object)
     */
    public synchronized Object putUserObject(Object key, Object val) {
        
        return super.putUserObject(key, val);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#refresh(java.lang.Object)
     */
    public synchronized void refresh(Object pc) {
        
        super.refresh(pc);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#refreshAll()
     */
    public synchronized void refreshAll() {
        
        super.refreshAll();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#refreshAll(java.util.Collection)
     */
    public synchronized void refreshAll(Collection pcs) {
        
        super.refreshAll(pcs);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#refreshAll(javax.jdo.JDOException)
     */
    public synchronized void refreshAll(JDOException jdoe) {
        
        super.refreshAll(jdoe);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#refreshAll(java.lang.Object[])
     */
    public synchronized void refreshAll(Object[] pcs) {
        
        super.refreshAll(pcs);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#removeInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener)
     */
    public synchronized void removeInstanceLifecycleListener(InstanceLifecycleListener listener) {
        
        super.removeInstanceLifecycleListener(listener);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#removeUserObject(java.lang.Object)
     */
    public synchronized Object removeUserObject(Object key) {
        
        return super.removeUserObject(key);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#retrieve(java.lang.Object)
     */
    public synchronized void retrieve(Object pc) {
        
        super.retrieve(pc);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#retrieveAll(java.util.Collection, boolean)
     */
    public synchronized void retrieveAll(Collection pcs, boolean DFGOnly) {

        super.retrieveAll(pcs, DFGOnly);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#retrieveAll(java.util.Collection)
     */
    public synchronized void retrieveAll(Collection pcs) {
        super.retrieveAll(pcs);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#retrieveAll(java.lang.Object[], boolean)
     */
    public synchronized void retrieveAll(Object[] pcs, boolean DFGOnly) {
        super.retrieveAll(pcs, DFGOnly);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#retrieveAll(java.lang.Object[])
     */
    public synchronized void retrieveAll(Object[] pcs) {
        super.retrieveAll(pcs);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#setIgnoreCache(boolean)
     */
    public synchronized void setIgnoreCache(boolean flag) {
        super.setIgnoreCache(flag);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManager_2#setUserObject(java.lang.Object)
     */
    public synchronized void setUserObject(Object o) {
        super.setUserObject(o);
    }

}
