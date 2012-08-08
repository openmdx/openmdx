/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractPersistenceManager.java,v 1.4 2008/02/08 16:51:40 hburger Exp $
 * Description: Abstract PersistenceManager
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/08 16:51:40 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2006, OMEX AG, Switzerland
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

import org.openmdx.kernel.callback.CloseCallback;

/**
 * Abstract PersistenceManager
 *
 * @since openMDX 2.0
 */
@SuppressWarnings({
    "unchecked"
})
public abstract class AbstractPersistenceManager
    implements PersistenceManager
{

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
    protected AbstractPersistenceManager(
        PersistenceManagerFactory factory,
        InstanceLifecycleNotifier notifier,
        String connectionUsername,
        String connectionPassword
    ){
        this.persistenceManagerFactory = factory;
        this.instanceLifecycleNotifier = notifier;
//      this.connectionUsername = connectionUsername;
//      this.connectionPassword = connectionPassword;        
        setIgnoreCache(factory.getIgnoreCache());
        setMultithreaded(factory.getMultithreaded());
        this.setDetachAllOnCommit(factory.getDetachAllOnCommit());
    }

    /**
     * 
     */
    private PersistenceManagerFactory persistenceManagerFactory;
    
    /**
     * 
     */
    private final InstanceLifecycleNotifier instanceLifecycleNotifier;

    /**
     * 
     */
    private final Map<Object,Object> userObjects = new HashMap<Object,Object>();
    
//  /**
//   * 
//   */
//    private final String connectionUsername;
//    
//  /**
//   * 
//   */
//  private final String connectionPassword;

    /**
     * 
     */
    private boolean ignoreCache;

    /**
     * 
     */
    private boolean detachAllOnCommit;
    
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
        // TODO Auto-generated method stub
        return null;
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
        List<JDOException> exceptions = new ArrayList<JDOException>();
        for(Object pc : pcs) try {
            evict (pc);
        } catch (JDOException exception) {
            exceptions.add(exception);
        }
        if(!exceptions.isEmpty()) throw new JDOException(
            "Eviction failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void evictAll(Collection pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            evict (pc);
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Eviction failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
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
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            refresh(pc);
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Refresh failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void refreshAll(Collection pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            refresh(pc);
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Refresh failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
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
        Throwable[] throwables = jdoe.getNestedExceptions();
        if(throwables != null) {
            List<Object> objects = new ArrayList<Object>();
            for(Throwable throwable : throwables) {
                if(throwable instanceof JDOException) {
                    Object object = ((JDOException)throwable).getFailedObject();
                    if(object != null) objects.add(object);
                }
            }
            refreshAll(objects);
        }
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
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, Collection cln) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, String filter) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection, java.lang.String)
     */
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
    public Query newNamedQuery(Class cls, String queryName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class, boolean)
     */
    @SuppressWarnings("unchecked")
    public Extent getExtent(Class persistenceCapableClass, boolean subclasses) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
    public Object getObjectById(
        Class cls, 
        Object key
    ) {
        return this.getObjectById(
            this.newObjectIdInstance(cls, key)
        );
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
    @SuppressWarnings("unchecked")
    public Object newObjectIdInstance(Class pcClass, Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public Collection getObjectsById(Collection oids, boolean validate) {
        Collection<Object> objects = new ArrayList<Object>(oids.size());
        for(Object oid : oids) {
            objects.add(getObjectById(oid, validate));
        }
        return objects;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public Collection getObjectsById(Collection oids) {
        return getObjectsById(oids, true);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[], boolean)
     */
    public Object[] getObjectsById(Object[] oids, boolean validate) {
        Object[] objects = new Object[oids.length];
        for(
            int i = 0;
            i < oids.length;
            i++
        ) {
            objects[i] = getObjectById(oids[i], validate);
        }
        return objects;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[])
     */
    public Object[] getObjectsById(Object[] oids) {
        return getObjectsById(oids, true);
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
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makePersistent (pc);
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions == null) {
            return pcs;
        } else {
            throw new JDOException(
                "Make persistent failure",
                exceptions.toArray(new JDOException[exceptions.size()])
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistentAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public Collection makePersistentAll(Collection pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makePersistent (pc);
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions == null) {
            return pcs;
        } else {
            throw new JDOException(
                "Make persistent failure",
                exceptions.toArray(new JDOException[exceptions.size()])
            );
        }
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
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            deletePersistent (pc);
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Delete persistent failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void deletePersistentAll(Collection pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            deletePersistent (pc);
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Delete persistent failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
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
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makeTransient (pc);
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Make transient failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
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
    @SuppressWarnings("unchecked")
    public void makeTransientAll(Collection pcs, boolean useFetchPlan) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makeTransient (pc, useFetchPlan);
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Make transient failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[], boolean)
     */
    public void makeTransientAll(Object[] pcs, boolean useFetchPlan) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makeTransient (pc, useFetchPlan);
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Make transient failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void makeTransientAll(Collection pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makeTransient (pc);
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Make transient failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
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
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makeTransactional (pc);
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Make transactional failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void makeTransactionalAll(Collection pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makeTransactional (pc);
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Make transactional failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
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
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makeNontransactional (pc);
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Make non-transactional failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.util.Collection)
     */
    public void makeNontransactionalAll(Collection pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makeNontransactional (pc);
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Make non-transactional failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
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
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object, boolean)
     */
    public void retrieve(Object pc, boolean useFetchPlan) {
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
    @SuppressWarnings("unchecked")
    public Class getObjectIdClass(Class cls) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setMultithreaded(boolean)
     */
    public void setMultithreaded(boolean flag) {
        if(flag != getMultithreaded()) throw new JDOUnsupportedOptionException(
            "javax.jdo.option.Multithreaded can be set at factory level only"
        );
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
    public void setDetachAllOnCommit(boolean detachAllOnCommit) {
        this.detachAllOnCommit = detachAllOnCommit;
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
        List<JDOException> exceptions = null;
        List<Object> objects = new ArrayList<Object>(pcs.size());
        for(Object pc : pcs) try {
            objects.add(detachCopy (pc));
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions == null){
            return objects;
        } else throw new JDOException(
            "Detach copy failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopyAll(java.lang.Object[])
     */
    public Object[] detachCopyAll(Object[] pcs) {
        List<JDOException> exceptions = null;
        Object[] objects = new Object[pcs.length];
        for(
            int i = 0;
            i < pcs.length;
            i++
        ) try{
            objects[i] = detachCopy (pcs[i]);
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions == null) {
            return objects;
        } else throw new JDOException(
            "Detach copy failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
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
    public Collection attachCopyAll(
        Collection pcs, 
        boolean makeTransactional
    ) {
        List<JDOException> exceptions = null;
        List<Object> objects = new ArrayList<Object>(pcs.size());
        for(Object pc : pcs) try {
            objects.add(attachCopy (pc, makeTransactional));
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions == null){
            return objects;
        } else throw new JDOException(
            "Attach copy failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#attachCopyAll(java.lang.Object[], boolean)
     */
    public Object[] attachCopyAll(
        Object[] pcs, 
        boolean makeTransactional
    ) {
        List<JDOException> exceptions = null;
        Object[] objects = new Object[pcs.length];
        for(
            int i = 0;
            i < pcs.length;
            i++
        ) try{
            objects[i] = attachCopy (pcs[i], makeTransactional);
        } catch (JDOException exception) {
            (
                exceptions == null ?  exceptions =  new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions == null) {
            return objects;
        } else throw new JDOException(
            "Attach copy failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#putUserObject(java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
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

}
