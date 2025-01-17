/*
 * ====================================================================
 * Description: Abstract PersistenceManager
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.jdo.Extent;
import javax.jdo.FetchGroup;
import javax.jdo.JDOException;
import javax.jdo.JDOUserException;
import javax.jdo.listener.InstanceLifecycleListener;

import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.collection.Maps;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.JDOPersistenceManagerFactory;
import org.openmdx.kernel.resource.spi.CloseCallback;

/**
 * Abstract PersistenceManager
 *
 * @since openMDX 2.0
 */
@SuppressWarnings({"rawtypes","unchecked"})
public abstract class AbstractPersistenceManager implements PersistenceManager_1_0 {

    /**
     * Constructor 
     *
     * @param factory the factory which creates this persistence manager
     * @param instanceLifecycleListener the instance life cycle listener has to be provided by the subclass
     */
    protected AbstractPersistenceManager(
        final JDOPersistenceManagerFactory factory, 
        final MarshallingInstanceLifecycleListener instanceLifecycleListener
    ){
        final boolean multithreaded = factory.getMultithreaded();
        this.persistenceManagerFactory = factory;
        this.instanceLifecycleListener = instanceLifecycleListener;
        this.userObjects = Maps.newMap(multithreaded);
        this.setIgnoreCache(factory.getIgnoreCache());
        this.setMultithreaded(multithreaded);
        this.setDetachAllOnCommit(factory.getDetachAllOnCommit());
        this.setCopyOnAttach(factory.getCopyOnAttach());
        this.setDatastoreReadTimeoutMillis(factory.getDatastoreReadTimeoutMillis());
        this.setDatastoreWriteTimeoutMillis(factory.getDatastoreWriteTimeoutMillis());
    }

    /**
     * The non-null key for unqualified user objects
     */
    private static final Object USER_OBJECT_KEY = new Object();
    
    /**
     * The connection factory
     */
    private JDOPersistenceManagerFactory persistenceManagerFactory;

    /**
     * 
     */
    private MarshallingInstanceLifecycleListener instanceLifecycleListener;

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
     * The number of milliseconds allowed for read operations to complete
     */
    private Integer datastoreReadTimeoutMillis;

    /**
     * The number of milliseconds allowed for write operations to complete
     */
    private Integer datastoreWriteTimeoutMillis;
    
    /**
     * Tells, whether the persistence manager has been closed
     */
    private volatile boolean closed = false;
    
    /**
     * The lazily allocated user objects
     */
    private Map<Object,Object> userObjects;

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getPersistenceManagerFactory()
     */
    @Override
    public JDOPersistenceManagerFactory getPersistenceManagerFactory() {
        if(this.closed) {
            throw BasicException.initHolder(
                new JDOUserException(
                    "The persistence manager is closed",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE
                    )
                )
            );
        } else {
	        return this.persistenceManagerFactory;
        }
    }

    /**
     * Tells whether the transactions are container managed
     * 
     * @return {@code true} if the transactions are container managed
     */ 
    protected boolean isTransactionContainerManaged(){
    	return false;
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#close()
     */
    @Override
    public synchronized void close() {
    	if(!closed) {
	        if(!this.isTransactionContainerManaged() && this.currentUnitOfWork().isActive()) {
	        	throw new JDOUserException(
		            "Persistence manager with an active unit of work can't be closed unless they are container managed"
		        );
	        }
	        this.instanceLifecycleListener.close();
	        this.instanceLifecycleListener = null;
	        if(this.userObjects != null) {
    	        this.userObjects.clear();
    	        this.userObjects = null;
	        }
	        if(this.persistenceManagerFactory instanceof CloseCallback) {
	            ((CloseCallback)this.persistenceManagerFactory).postClose(this);
	        }
	        this.persistenceManagerFactory = null;
	        this.closed = true;
    	}
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#isClosed()
     */
    @Override
    public boolean isClosed() {
        return this.closed;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize(
    ) throws Throwable {
        close();
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.util.Collection)
     */
    @Override
    public void evictAll(Collection pcs) {
        PersistenceManagers.evictAll(this, pcs);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.util.Collection)
     */
    @Override
    public void refreshAll(Collection pcs) {
        PersistenceManagers.refreshAll(this, pcs);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(javax.jdo.JDOException)
     */
    @Override
    public void refreshAll(JDOException jdoe) {
        PersistenceManagers.refreshAll(this, jdoe);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Class, java.lang.Object)
     */
    @Override
    public <T> T getObjectById(Class<T> cls, Object key) {
        return (T) this.getObjectById(
            this.newObjectIdInstance(cls, key)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection, boolean)
     */
    @Override
    public Collection getObjectsById(Collection oids, boolean validate) {
        return PersistenceManagers.getObjectsById(this, validate, oids);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection)
     */
    @Override
    public Collection getObjectsById(Collection oids) {
        return this.getObjectsById(oids, true);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(boolean, java.lang.Object[])
     */
    @Override
    public Object[] getObjectsById(boolean validate, Object... oids) {
        return PersistenceManagers.getObjectsById(this, validate, oids);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistentAll(T[])
     */
    @Override
    public <T> T[] makePersistentAll(T... pcs) {
        return PersistenceManagers.makePersistentAll(this, pcs);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistentAll(java.util.Collection)
     */
    @Override
    public <T> Collection<T> makePersistentAll(Collection<T> pcs) {
        return PersistenceManagers.makePersistentAll(this, pcs);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.util.Collection)
     */
    @Override
    public void deletePersistentAll(Collection pcs) {
        PersistenceManagers.deletePersistentAll(this, pcs);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object)
     */
    @Override
    public void makeTransient(Object pc) {
        this.makeTransient(pc, false);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection, boolean)
     */
    @Override
    public void makeTransientAll(Collection pcs, boolean useFetchPlan) {
        PersistenceManagers.makeTransientAll(this, pcs, useFetchPlan);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(boolean, java.lang.Object[])
     */
    @Override
    public void makeTransientAll(boolean useFetchPlan, Object... pcs) {
        PersistenceManagers.makeTransientAll(this, useFetchPlan, pcs);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection)
     */
    @Override
    public void makeTransientAll(Collection pcs) {
        this.makeTransientAll(pcs, false);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.util.Collection)
     */
    @Override
    public void makeTransactionalAll(Collection pcs) {
        PersistenceManagers.makeTransactionalAll(this, pcs);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.util.Collection)
     */
    @Override
    public void makeNontransactionalAll(Collection pcs) {
        PersistenceManagers.makeNontransactionalAll(this, pcs);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object)
     */
    @Override
    public void retrieve(Object pc) {
        this.retrieve(pc, false);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection)
     */
    @Override
    public void retrieveAll(Collection pcs) {
        this.retrieveAll(pcs, false);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection, boolean)
     */
    @Override
    public void retrieveAll(Collection pcs, boolean useFetchPlan) {
        PersistenceManagers.retrieveAll(this, useFetchPlan, pcs);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(boolean, java.lang.Object[])
     */
    @Override
    public void retrieveAll(boolean useFetchPlan, Object... pcs) {
        PersistenceManagers.retrieveAll(this, useFetchPlan, pcs);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setUserObject(java.lang.Object)
     */
    @Override
    public void setUserObject(Object o) {
        if(o == null) {
            removeUserObject(USER_OBJECT_KEY);
        } else {
            putUserObject(USER_OBJECT_KEY, o);
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getUserObject()
     */
    @Override
    public Object getUserObject() {
        return getUserObject(USER_OBJECT_KEY);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setIgnoreCache(boolean)
     */
    @Override
    public void setIgnoreCache(boolean flag) {
        this.ignoreCache = flag;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getIgnoreCache()
     */
    @Override
    public boolean getIgnoreCache() {
        return this.ignoreCache;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDetachAllOnCommit()
     */
    @Override
    public boolean getDetachAllOnCommit() {
        return this.detachAllOnCommit;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setDetachAllOnCommit(boolean)
     */
    @Override
    public void setDetachAllOnCommit(boolean detachAllOnCommit) {
        this.detachAllOnCommit = detachAllOnCommit;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getCopyOnAttach()
     */
    @Override
    public boolean getCopyOnAttach() {
        return this.copyOnAttach;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setCopyOnAttach(boolean)
     */
    @Override
    public void setCopyOnAttach(boolean flag) {
        this.copyOnAttach = flag;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopyAll(java.util.Collection)
     */
    @Override
    public <T> Collection<T> detachCopyAll(Collection<T> pcs) {
        return PersistenceManagers.detachCopyAll(this, pcs);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopyAll(java.lang.Object[])
     */
    @Override
    public <T> T[] detachCopyAll(T... pcs) {
        return PersistenceManagers.detachCopyAll(this, pcs);
    }

    /**
     * Allocate the user objects repository lazily
     * 
     * @return the user objects repository
     */
    private Map<Object,Object> getUserObjects(){
        if(this.userObjects == null) {
            if(isClosed()) {
                throw new IllegalStateException("The persistence manager is closed");
            }
            this.userObjects = Maps.newMap(this.getMultithreaded());
        }
        return this.userObjects;
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#putUserObject(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object putUserObject(Object key, Object val) {
        return getUserObjects().put(key, val);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getUserObject(java.lang.Object)
     */
    @Override
    public Object getUserObject(Object key) {
        return getUserObjects().get(key);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#removeUserObject(java.lang.Object)
     */
    @Override
    public Object removeUserObject(Object key) {
        return getUserObjects().remove(key);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#addInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener, java.lang.Class[])
     */
    @Override
    public void addInstanceLifecycleListener(
        InstanceLifecycleListener listener,
        Class... classes
    ) {
        this.instanceLifecycleListener.addInstanceLifecycleListener(listener, classes);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#removeInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener)
     */
    @Override
    public void removeInstanceLifecycleListener(
        InstanceLifecycleListener listener
    ) {
        this.instanceLifecycleListener.removeInstanceLifecycleListener(listener);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class)
     */
    @Override
    public <T> Extent<T> getExtent(Class<T> persistenceCapableClass) {
        return this.getExtent(persistenceCapableClass, true);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(boolean, java.lang.Class)
     */
    @Override
    public void evictAll(boolean subclasses, Class pcClass) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getFetchGroup(java.lang.Class, java.lang.String)
     */
    @Override
    public FetchGroup getFetchGroup(Class arg0, String arg1) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.lang.Object[])
     */
    @Override
    public void deletePersistentAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.lang.Object[])
     */
    @Override
    public void evictAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[])
     */
    @Override
    public Object[] getObjectsById(Object... oids) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[], boolean)
     *
     * @deprecated with JDO 2.1
     * removed with JDO 3.2
     */
    public Object[] getObjectsById(Object[] oids, boolean validate) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.lang.Object[])
     */
    @Override
    public void makeNontransactionalAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.lang.Object[])
     */
    @Override
    public void makeTransactionalAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[])
     */
    @Override
    public void makeTransientAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.lang.Object[])
     */
    @Override
    public void refreshAll(Object... pcs) {
        PersistenceManagers.refreshAll(this, pcs);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[])
     */
    @Override
    public void retrieveAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[], boolean)
     *
     * @deprecated with JDO 2.1
     * removed with JDO 3.2
     */
    public void retrieveAll(Object[] pcs, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Operation not supported by AbstractPersistenceManager");
    }
        
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setDatastoreReadTimeoutMillis(java.lang.Integer)
     */
    @Override
    public void setDatastoreReadTimeoutMillis(Integer interval) {
        this.datastoreReadTimeoutMillis = interval;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDatastoreReadTimeoutMillis()
     */
    @Override
    public Integer getDatastoreReadTimeoutMillis() {
        return this.datastoreReadTimeoutMillis;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setDatastoreWriteTimeoutMillis(java.lang.Integer)
     */
    @Override
    public void setDatastoreWriteTimeoutMillis(Integer interval) {
        this.datastoreWriteTimeoutMillis = interval;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDatastoreWriteTimeoutMillis()
     */
    @Override
    public Integer getDatastoreWriteTimeoutMillis() {
        return this.datastoreWriteTimeoutMillis;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty(
        String propertyName,
        Object value
    ) {
        PersistenceManagers.setProperty(this, propertyName, value);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getProperties()
     */
    @Override
    public Map<String, Object> getProperties() {
        return PersistenceManagers.getProperties(this);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getSupportedProperties()
     */
    @Override
    public Set<String> getSupportedProperties() {
        return PersistenceManagers.getSupportedProperties(); 
    }

}
