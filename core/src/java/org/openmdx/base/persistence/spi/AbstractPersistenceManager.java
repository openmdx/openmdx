/*
 * ====================================================================
 * Name:        $Id: AbstractPersistenceManager.java,v 1.3 2009/03/03 17:23:08 hburger Exp $
 * Description: Abstract PersistenceManager
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/03 17:23:08 $
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
package org.openmdx.base.persistence.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jdo.Extent;
import javax.jdo.FetchGroup;
import javax.jdo.JDOException;
import javax.jdo.JDOUserException;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.listener.InstanceLifecycleListener;

import org.openmdx.kernel.persistence.resource.Connection_2;

/**
 * Abstract PersistenceManager
 *
 * @since openMDX 2.0
 */
@SuppressWarnings({
    "unchecked"
})
public abstract class AbstractPersistenceManager
    extends Connection_2
{

    /**
     * Constructor 
     * <p>
     * The parameters may be kept by the instance.
     *
     * @param factory
     */
    protected AbstractPersistenceManager(
    ){
        super();
        this.instanceLifecycleNotifier = new InstanceLifecycleNotifier();
    }

    /**
     * Constructor 
     * <p>
     * The parameters may be kept by the instance.
     *
     * @param factory
     * @param notifier
     */
    protected AbstractPersistenceManager(
        PersistenceManagerFactory factory,
        InstanceLifecycleNotifier notifier
    ){
        super(factory);
        this.instanceLifecycleNotifier = notifier;
    }

    /**
     * 
     */
    private final InstanceLifecycleNotifier instanceLifecycleNotifier;

    /**
     * 
     */
    private boolean ignoreCache;

    /**
     * 
     */
    private boolean detachAllOnCommit;

    /**
     * 
     */
    private boolean copyOnAttach;

    /**
     * 
     */
    private final ConcurrentMap<Object,Object> userObjects = new ConcurrentHashMap<Object,Object>();

    /**
     * Provide the instance life-cycle notifier
     * 
     * @return the instance life-cycle notifier
     */
    protected final InstanceLifecycleNotifier getInstanceLifecycleNotifier(){
        return this.instanceLifecycleNotifier;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.persistence.resource.Connection_2#setPersistenceManagerFactory(javax.jdo.PersistenceManagerFactory)
     */
    @Override
    public void setPersistenceManagerFactory(
        PersistenceManagerFactory persistenceManagerFactory
    ) {
        super.setPersistenceManagerFactory(persistenceManagerFactory);
        setIgnoreCache(persistenceManagerFactory.getIgnoreCache());
        setMultithreaded(persistenceManagerFactory.getMultithreaded());
        setDetachAllOnCommit(persistenceManagerFactory.getDetachAllOnCommit());



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
            super.close();
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.util.Collection)
     */
    public void evictAll(Collection pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            evict (pc);
        } catch (JDOException exception) {
            (
                exceptions == null ? exceptions = new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Eviction failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.util.Collection)
     */
    public void refreshAll(Collection pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            refresh(pc);
        } catch (JDOException exception) {
            (
                exceptions == null ? exceptions = new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Refresh failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
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
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Class, java.lang.Object)
     */
    public <T> T getObjectById(Class<T> cls, Object key) {
        return (T) this.getObjectById(
            this.newObjectIdInstance(cls, key)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection, boolean)
     */
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
    public Collection getObjectsById(Collection oids) {
        return getObjectsById(oids, true);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(boolean, java.lang.Object[])
     */
    public Object[] getObjectsById(boolean validate, Object... oids) {
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
     * @see javax.jdo.PersistenceManager#makePersistentAll(T[])
     */

    public <T> T[] makePersistentAll(T... pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makePersistent (pc);
        } catch (JDOException exception) {
            (
                exceptions == null ? exceptions = new ArrayList<JDOException>() : exceptions
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
    public <T> Collection<T> makePersistentAll(Collection<T> pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makePersistent (pc);
        } catch (JDOException exception) {
            (
                exceptions == null ? exceptions = new ArrayList<JDOException>() : exceptions
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
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.util.Collection)
     */
    public void deletePersistentAll(Collection pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            deletePersistent (pc);
        } catch (JDOException exception) {
            (
                exceptions == null ? exceptions = new ArrayList<JDOException>() : exceptions
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
        makeTransient(pc, false);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection, boolean)
     */
    public void makeTransientAll(Collection pcs, boolean useFetchPlan) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makeTransient (pc, useFetchPlan);
        } catch (JDOException exception) {
            (
                exceptions == null ? exceptions = new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Make transient failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(boolean, java.lang.Object[])
     */
    public void makeTransientAll(boolean useFetchPlan, Object... pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makeTransient (pc, useFetchPlan);
        } catch (JDOException exception) {
            (
                exceptions == null ? exceptions = new ArrayList<JDOException>() : exceptions
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
    public void makeTransientAll(Collection pcs) {
        makeTransientAll(pcs, false);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.util.Collection)
     */
    public void makeTransactionalAll(Collection pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makeTransactional (pc);
        } catch (JDOException exception) {
            (
                exceptions == null ? exceptions = new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Make transactional failure",
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
                exceptions == null ? exceptions = new ArrayList<JDOException>() : exceptions
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
        retrieve(pc, false);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection)
     */
    public void retrieveAll(Collection pcs) {
        retrieveAll(false, pcs);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection, boolean)
     */
    public void retrieveAll(Collection pcs, boolean useFetchPlan) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            retrieve(pc, useFetchPlan);
        } catch (JDOException exception) {
            (
                exceptions == null ? exceptions = new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Retrieve failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(boolean, java.lang.Object[])
     */
    public void retrieveAll(boolean useFetchPlan, Object... pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            retrieve(pc, useFetchPlan);
        } catch (JDOException exception) {
            (
                exceptions == null ? exceptions = new ArrayList<JDOException>() : exceptions
            ).add(exception);
        }
        if(exceptions != null) throw new JDOException(
            "Retrieve failure",
            exceptions.toArray(new JDOException[exceptions.size()])
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setUserObject(java.lang.Object)
     */
    public void setUserObject(Object o) {
        this.userObjects.put(null, o);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getUserObject()
     */
    public Object getUserObject() {
        return this.userObjects.get(null);
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
     * @see javax.jdo.PersistenceManager#getCopyOnAttach()
     */
    public boolean getCopyOnAttach() {
        return this.copyOnAttach;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setCopyOnAttach(boolean)
     */
    public void setCopyOnAttach(boolean flag) {
        this.copyOnAttach = flag;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopyAll(java.util.Collection)
     */
    public <T> Collection<T> detachCopyAll(Collection<T> pcs) {
        List<JDOException> exceptions = null;
        List objects = new ArrayList(pcs.size());
        for(Object pc : pcs) try {
            objects.add(detachCopy (pc));
        } catch (JDOException exception) {
            (
                exceptions == null ? exceptions = new ArrayList<JDOException>() : exceptions
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
    public <T> T[] detachCopyAll(T... pcs) {
        List<JDOException> exceptions = null;
        T[] objects = pcs.clone();
        int i = 0;
        for(T pc : pcs) try {
            objects[i++] = detachCopy (pc);
        } catch (JDOException exception) {
            (
                exceptions == null ? exceptions = new ArrayList<JDOException>() : exceptions
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
     * @see javax.jdo.PersistenceManager#addInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener, java.lang.Class[])
     */
    public void addInstanceLifecycleListener(
        InstanceLifecycleListener listener,
        Class... classes
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

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class)
     */
    public <T> Extent<T> getExtent(Class<T> persistenceCapableClass) {
        return getExtent(persistenceCapableClass, true);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(boolean, java.lang.Class)
     */
    public void evictAll(boolean subclasses, Class pcClass) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getFetchGroup(java.lang.Class, java.lang.String)
     */
    public FetchGroup getFetchGroup(Class arg0, String arg1) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects()
     */
    public Set getManagedObjects() {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet)
     */
    public Set getManagedObjects(EnumSet<ObjectState> states) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.lang.Class[])
     */
    public Set getManagedObjects(Class... classes) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet, java.lang.Class[])
     */
    public Set getManagedObjects(EnumSet<ObjectState> states, Class... classes) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.lang.Object[])
     */
    public void deletePersistentAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.lang.Object[])
     */
    public void evictAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[])
     */
    public Object[] getObjectsById(Object... oids) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[], boolean)
     */
    public Object[] getObjectsById(Object[] oids, boolean validate) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.lang.Object[])
     */
    public void makeNontransactionalAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.lang.Object[])
     */
    public void makeTransactionalAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[])
     */
    public void makeTransientAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.lang.Object[])
     */
    public void refreshAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[])
     */
    public void retrieveAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[], boolean)
     */
    public void retrieveAll(Object[] pcs, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }
    
}
