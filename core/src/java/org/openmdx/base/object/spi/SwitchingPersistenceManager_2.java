/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: SwitchingPersistenceManager_2.java,v 1.2 2008/02/19 14:18:31 hburger Exp $
 * Description: Abstract PersistenceManager
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/19 14:18:31 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
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
import javax.jdo.JDOFatalException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOUnsupportedOptionException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.identity.StringIdentity;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.transaction.Synchronization;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract PersistenceManager
 *
 * @since openMDX 2.0
 */
public class SwitchingPersistenceManager_2
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
    protected SwitchingPersistenceManager_2(
        PersistenceManagerFactory factory,
        PersistenceManagerSwitch_2_0 selector
    ){
        this.persistenceManagerFactory = factory;
        setIgnoreCache(factory.getIgnoreCache());
        setMultithreaded(factory.getMultithreaded());
        setDetachAllOnCommit(factory.getDetachAllOnCommit());
        this.delegates = selector;
    }

    /**
     * 
     */
    private boolean closed = false;
    
    /**
     * 
     */
    private PersistenceManagerFactory persistenceManagerFactory;
    
    /**
     * 
     */
    private final Map<Object,Object> userObjects = new HashMap<Object,Object>();
    
    /**
     * 
     */
    private final PersistenceManagerSwitch_2_0 delegates;
    
    /**
     * 
     */
    private final ProxyFactory_2_0 marshaller = new StandardProxyFactory_2(this);
    
    /**
     * 
     */
    private boolean ignoreCache;

    /**
     * 
     */
    private boolean detachAllOnCommit;

    /**
     * Methods to be implemented for full openMDX 2 support
     */
    private final static String OPENMDX_1_RESTRICTION = "This method is unsupported in openMDX 1 compatibility mode";

    /**
     * Handle <code>JDOException</code>s for multi-valued calls
     * 
     * @throws JDOException unless the nested exception collection is <code>null</code> 
     */
    protected void handle(
        String message,
        Collection<JDOException> nested
    ){
        if(nested != null) throw new JDOException(
            message, 
            nested.toArray(new JDOException[nested.size()])
        );
    }
           
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#isClosed()
     */
    public boolean isClosed() {
        return this.closed;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#close()
     */
    public void close() {
        if(!isClosed()) {
            if(currentTransaction().isActive()) throw new JDOUserException(
                "Persistence manager with an active unit of work can't be closed"
            );
            for(PersistenceManager pm : this.delegates) {
                pm.close();
            }
            this.closed = true;
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#currentTransaction()
     */
    public Transaction currentTransaction() {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evict(java.lang.Object)
     */
    public void evict(Object pc) {
        this.delegates.getDelegateManager(pc).evict(
            PersistenceCapableProxyHandler_2.getDelegate(pc)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.lang.Object[])
     */
    public void evictAll(Object[] pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            evict (pc);
        } catch (JDOException exception) {
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle(
            "Eviction failure",
            exceptions
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
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Eviction failure", exceptions);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll()
     */
    public void evictAll() {
        for(PersistenceManager pm : this.delegates) {
            pm.evictAll();
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refresh(java.lang.Object)
     */
    public void refresh(Object pc) {
        this.delegates.getDelegateManager(pc).refresh(
            PersistenceCapableProxyHandler_2.getDelegate(pc)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.lang.Object[])
     */
    public void refreshAll(Object[] pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            refresh(pc);
        } catch (JDOException exception) {
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Refresh failure", exceptions);
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
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Refresh failure", exceptions);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll()
     */
    public void refreshAll() {
        for(PersistenceManager pm : this.delegates) {
            pm.refreshAll();
        }
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
        throw new UnsupportedOperationException(OPENMDX_1_RESTRICTION); // TODO
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Object)
     */
    public Query newQuery(Object compiled) {
        throw new UnsupportedOperationException(OPENMDX_1_RESTRICTION); // TODO
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String)
     */
    public Query newQuery(String query) {
        throw new UnsupportedOperationException(OPENMDX_1_RESTRICTION); // TODO
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String, java.lang.Object)
     */
    public Query newQuery(String language, Object query) {
        throw new UnsupportedOperationException(OPENMDX_1_RESTRICTION); // TODO
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls) {
        throw new UnsupportedOperationException(OPENMDX_1_RESTRICTION); // TODO
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent)
     */
    public Query newQuery(Extent cln) {
        throw new UnsupportedOperationException(OPENMDX_1_RESTRICTION); // TODO
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, Collection cln) {
        throw new UnsupportedOperationException(OPENMDX_1_RESTRICTION); // TODO
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, String filter) {
        throw new UnsupportedOperationException(OPENMDX_1_RESTRICTION); // TODO
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, Collection cln, String filter) {
        throw new UnsupportedOperationException(OPENMDX_1_RESTRICTION); // TODO
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent, java.lang.String)
     */
    public Query newQuery(Extent cln, String filter) {
        throw new UnsupportedOperationException(OPENMDX_1_RESTRICTION); // TODO
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newNamedQuery(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newNamedQuery(Class cls, String queryName) {
        throw new JDOFatalException(OPENMDX_1_RESTRICTION);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class, boolean)
     */
    @SuppressWarnings("unchecked")
    public Extent getExtent(Class persistenceCapableClass, boolean subclasses) {
        throw new JDOFatalException(OPENMDX_1_RESTRICTION);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Extent getExtent(Class persistenceCapableClass) {
        throw new JDOFatalException(OPENMDX_1_RESTRICTION);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object, boolean)
     */
    public Object getObjectById(Object oid, boolean validate) {
        return PersistenceCapableProxyHandler_2.newProxy(
            this.marshaller, 
            oid, 
            this.delegates.getDelegateInstance(
                this.marshaller.unmarshalObjectId(oid), validate
            )
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Class, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public Object getObjectById(
        Class cls, 
        Object key
    ) {
        return getObjectById(
            newObjectIdInstance(cls, key)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object)
     */
    public Object getObjectById(Object oid) {
        return getObjectById(oid, true);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectId(java.lang.Object)
     */
    public Object getObjectId(Object pc) {
        return JDOHelper.getObjectId(
            PersistenceCapableProxyHandler_2.getDelegate(pc)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getTransactionalObjectId(java.lang.Object)
     */
    public Object getTransactionalObjectId(Object pc) {
        return JDOHelper.getTransactionalObjectId(
            PersistenceCapableProxyHandler_2.getDelegate(pc)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newObjectIdInstance(java.lang.Class, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public Object newObjectIdInstance(Class pcClass, Object key) {
        return new StringIdentity(
            pcClass,
            key.toString()
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public Collection getObjectsById(Collection oids, boolean validate) {
        List<JDOException> exceptions = null;
        Collection<Object> objects = new ArrayList<Object>(oids.size());
        for(Object oid : oids) {
            try {
                objects.add(getObjectById(oid, validate));
            } catch (JDOException exception) {
                if(exceptions == null) exceptions = new ArrayList<JDOException>();
                exceptions.add(exception);
            }
        }
        handle("Object Retrieval failure", exceptions);
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
        List<JDOException> exceptions = null;
        Object[] objects = new Object[oids.length];
        for(
            int i = 0;
            i < oids.length;
            i++
        ) try {
            objects[i] = getObjectById(oids[i], validate);
        } catch (JDOException exception) {
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Retrieval Failure", exceptions);
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
        Object delegate = this.marshaller.unmarshal(pc);
        return this.marshaller.marshal(
            JDOHelper.getPersistenceManager(delegate).makePersistent(delegate)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistentAll(java.lang.Object[])
     */
    public Object[] makePersistentAll(Object[] pcs) {
        List<JDOException> exceptions = null;
        Object[] objects = new Object[pcs.length];
        for(
            int i = 0, iLimit = pcs.length;
            i < iLimit;
            i++
        ) try {
            objects[i] = makePersistent (pcs[i]);
        } catch (JDOException exception) {
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Make persistent failure", exceptions);
        return objects;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistentAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public Collection makePersistentAll(Collection pcs) {
        List<JDOException> exceptions = null;
        List<Object> objects = new ArrayList<Object>(pcs.size());
        for(Object pc : pcs) try {
            makePersistent (pc);
        } catch (JDOException exception) {
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Make persistent failure", exceptions);
        return objects;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistent(java.lang.Object)
     */
    public void deletePersistent(Object pc) {
        this.delegates.getDelegateManager(pc).deletePersistent(
            PersistenceCapableProxyHandler_2.getDelegate(pc)
        );

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.lang.Object[])
     */
    public void deletePersistentAll(Object[] pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            deletePersistent (pc);
        } catch (JDOException exception) {
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Delete persistent failure", exceptions);
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
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Delete persistent failure", exceptions);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object)
     */
    public void makeTransient(Object pc) {
        this.delegates.getDelegateManager(pc).makeTransient(
            PersistenceCapableProxyHandler_2.getDelegate(pc)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[])
     */
    public void makeTransientAll(Object[] pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makeTransient (pc);
        } catch (JDOException exception) {
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Make transient failure", exceptions);
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object, boolean)
     */
    public void makeTransient(Object pc, boolean useFetchPlan) {
        this.delegates.getDelegateManager(pc).makeTransient(
            PersistenceCapableProxyHandler_2.getDelegate(pc),
            useFetchPlan
        );
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
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Make transistent failure", exceptions);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[], boolean)
     */
    public void makeTransientAll(Object[] pcs, boolean useFetchPlan) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makeTransient (pc, useFetchPlan);
        } catch (JDOException exception) {
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Make transient failure", exceptions);
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
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Make transient failure", exceptions);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactional(java.lang.Object)
     */
    public void makeTransactional(Object pc) {
        this.delegates.getDelegateManager(pc).makeTransactional(
            PersistenceCapableProxyHandler_2.getDelegate(pc)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.lang.Object[])
     */
    public void makeTransactionalAll(Object[] pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makeTransactional (pc);
        } catch (JDOException exception) {
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Make transactional failure", exceptions);
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
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Make transactional failure", exceptions);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactional(java.lang.Object)
     */
    public void makeNontransactional(Object pc) {
        this.delegates.getDelegateManager(pc).makeNontransactional(
            PersistenceCapableProxyHandler_2.getDelegate(pc)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.lang.Object[])
     */
    public void makeNontransactionalAll(Object[] pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makeNontransactional (pc);
        } catch (JDOException exception) {
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Make non-transactional failure", exceptions);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void makeNontransactionalAll(Collection pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            makeNontransactional (pc);
        } catch (JDOException exception) {
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Make non-transactional failure", exceptions);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object)
     */
    public void retrieve(Object pc) {
        this.delegates.getDelegateManager(pc).retrieve(
            PersistenceCapableProxyHandler_2.getDelegate(pc)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void retrieveAll(Collection pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            retrieve (pc);
        } catch (JDOException exception) {
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Retrieval failure", exceptions);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public void retrieveAll(Collection pcs, boolean useFetchPlan) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            retrieve (pc, useFetchPlan);
        } catch (JDOException exception) {
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Retrieval failure", exceptions);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[])
     */
    public void retrieveAll(Object[] pcs) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            retrieve (pc);
        } catch (JDOException exception) {
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Retrieval failure", exceptions);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[], boolean)
     */
    public void retrieveAll(Object[] pcs, boolean useFetchPlan) {
        List<JDOException> exceptions = null;
        for(Object pc : pcs) try {
            retrieve (pc, useFetchPlan);
        } catch (JDOException exception) {
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Retrieval failure", exceptions);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object, boolean)
     */
    public void retrieve(Object pc, boolean useFetchPlan) {
        this.delegates.getDelegateManager(pc).retrieve(
            PersistenceCapableProxyHandler_2.getDelegate(pc),
            useFetchPlan
        );
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
        return StringIdentity.class;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getMultithreaded()
     */
    public boolean getMultithreaded() {
        return false;
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
        for(PersistenceManager pm : this.delegates) {
            pm.setIgnoreCache(flag);
        }
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
        for(PersistenceManager pm : this.delegates) {
            pm.setDetachAllOnCommit(detachAllOnCommit);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopy(java.lang.Object)
     */
    public Object detachCopy(Object pc) {
        return this.delegates.getDelegateManager(pc).detachCopy(
            PersistenceCapableProxyHandler_2.getDelegate(pc)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopyAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public Collection detachCopyAll(Collection pcs) {
        List<JDOException> exceptions = null;
        List<Object> objects = new ArrayList<Object>(pcs.size());
        for(Object pc : pcs) try {
            objects.add(detachCopy (pc));
        } catch (JDOException exception) {
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Detach copy failure", exceptions);
        return objects;
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
            if(exceptions == null) exceptions = new ArrayList<JDOException>();
            exceptions.add(exception);
        }
        handle("Detach copy failure", exceptions);
        return objects;
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
        for(PersistenceManager pm : this.delegates) {
            pm.flush();
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#checkConsistency()
     */
    public void checkConsistency() {
        for(PersistenceManager pm : this.delegates) {
            pm.checkConsistency();
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getFetchPlan()
     */
    public FetchPlan getFetchPlan() {
        throw new UnsupportedOperationException(OPENMDX_1_RESTRICTION); // TODO
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newInstance(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Object newInstance(Class pcClass) {
        return this.marshaller.marshal(
            this.delegates.newDelegateInstance(pcClass)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getSequence(java.lang.String)
     */
    public Sequence getSequence(String name) {
        return this.delegates.getSequence(name);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDataStoreConnection()
     */
    public JDOConnection getDataStoreConnection() {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#addInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener, java.lang.Class[])
     */
    @SuppressWarnings("unchecked")
    public void addInstanceLifecycleListener(
        InstanceLifecycleListener listener,
        Class[] classes
    ) {
        throw new UnsupportedOperationException(OPENMDX_1_RESTRICTION); // TODO
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#removeInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener)
     */
    public void removeInstanceLifecycleListener(
        InstanceLifecycleListener listener
    ) {
        throw new UnsupportedOperationException(OPENMDX_1_RESTRICTION); // TODO
    }


    //------------------------------------------------------------------------
    // Class UnitOfWork
    //------------------------------------------------------------------------
    
    /**
     * UnitOfWork_2
     * 
     * @since openMDX 2.0
     */
    public class UnitOfWork implements Transaction {

        /**
         * Constructor 
         */
        public UnitOfWork(
            PersistenceManager persistenceManager,
            UserTransaction userTransaction
        ) {
            this.persistenceManager = persistenceManager;
            PersistenceManagerFactory persistenceManagerFactory = persistenceManager.getPersistenceManagerFactory();
            this.restoreValues = persistenceManagerFactory.getRestoreValues();
            this.retainValues = persistenceManagerFactory.getRetainValues();
            this.nontransactionalWrite = persistenceManagerFactory.getNontransactionalWrite();
            this.nontransactionalRead = persistenceManagerFactory.getNontransactionalRead();
            this.optimistic = persistenceManagerFactory.getOptimistic();
            this.userTransaction = userTransaction;
        }

        /**
         * 
         */
        private final Logger logger = LoggerFactory.getLogger(UnitOfWork.class);
        
        /**
         * 
         */
        private PersistenceManager persistenceManager;

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
        
        /**
         * Tells whether there is an active unit of work
         */
        private boolean active = false;

        /**
         * Tells whether the active unit of work is set to rollback only
         */
        private boolean rollbackOnly = false;
        
        /**
         * 
         */
        private final UserTransaction userTransaction;
        
        
        //------------------------------------------------------------------------
        // Implements Transaction
        //------------------------------------------------------------------------

        /**
         * Asserts that the unit of work is bean managed
         * 
         * @throws JDOUserException if the unit of work is container managed.
         */
        private final void assertBeanManaged(
        ){
            if(this.userTransaction == null) throw new JDOUserException(
                "The unit of work is container managed"
            );
        }

        /**
         * Asserts that the unit of work is bean managed
         * 
         * @throws JDOUserException if the unit of work is container managed.
         */
        private final void assertActive(
        ){
            if(!this.active) throw new JDOUserException(
                "Unit of work is active"
            );
        }
        
        /* (non-Javadoc)
         * @see javax.jdo.Transaction#begin()
         */
        public void begin() {
            assertBeanManaged();
            if(this.active) throw new JDOUserException(
                "Unit of work is active"
            );
            this.rollbackOnly = false;
            List<Transaction> begun = new ArrayList<Transaction>();
            try {
                for (PersistenceManager pm : delegates){
                    Transaction t = pm.currentTransaction();
                    t.begin();
                    begun.add(t);
                }
                this.active = true;
            } catch (RuntimeException exception) {
                for(Transaction t : begun) {
                    try {
                        t.rollback();
                    } catch (RuntimeException ignore) {
                        this.logger.error("Could not undo begin()", ignore);
                    }
                }
                throw exception;
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#commit()
         */
        public void commit() {
            assertBeanManaged();
            assertActive();
            // TODO Auto-generated method stub
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#rollback()
         */
        public void rollback() {
            assertBeanManaged();
            assertActive();
            // TODO Auto-generated method stub
        }

        /* (non-Javadoc)
         * @see javax.jdo.Transaction#isActive()
         */
        public boolean isActive(
        ){
            return this.active;
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
        public void setOptimistic(boolean optimistic) {
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
        public void setSynchronization(Synchronization sync) {
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
            return this.persistenceManager;
        }

    }

}
