/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DataObjectManager_1.java,v 1.5 2009/06/09 12:45:18 hburger Exp $
 * Description: Data Object Manager
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 12:45:18 $
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
 *   * Redistributions in binary form must reproduce the above copyright
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
package org.openmdx.base.accessor.rest;

import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jdo.Extent;
import javax.jdo.FetchGroup;
import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalInternalException;
import javax.jdo.JDOUserException;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;

import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.Structure_1_0;
import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.CachingMarshaller;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.AspectObjectAcessor;
import org.openmdx.base.persistence.spi.StandardFetchGroup;
import org.openmdx.base.persistence.spi.StandardFetchPlan;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.spi.TransactionManager;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;
import org.openmdx.compatibility.state1.spi.StateCapables;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;

/**
 * A Data Object Manager 1.x implementation
 */
public class DataObjectManager_1
    extends CachingMarshaller
    implements DataObjectManager_1_0
{

    /**
     * Constructor 
     *
     * @param principalChain
     * @param connection
     * @param optimisticTransaction
     * 
     * @throws ResourceException 
     */
    DataObjectManager_1(
        PersistenceManagerFactory factory,
        List<String> principalChain,
        Connection connection,
        TransactionManager optimisticTransaction
    ) {
        this.factory = factory;
        this.interactionSpecs = InteractionSpecs.newRestInteractionSpecs(
            principalChain, 
            false // retainValues
        );
        this.connection = connection;
        this.optimisticTransaction = optimisticTransaction;
        if(MULTITHREADED) {
            this.unitOfWork = null;
            this.unitsOfWork = new ConcurrentHashMap<Thread,UnitOfWork_1>();
        } else {
            this.unitOfWork = newUnitOfWork();
            this.unitsOfWork = null;
        }
    }

    /**
     * The persistence manager's factory
     */
    private final PersistenceManagerFactory factory;
    
    /**
     * Tells whether the connection is multi-threaded.
     */
    private static final boolean MULTITHREADED = true;  

    /**
     * 
     */
    private static final boolean DETACH_ALL_ON_COMMIT = false;
    
    /**
     * 
     */
    private static final boolean IGNORE_CACHE = false;
    
    /**
     * 
     */
    final InteractionSpecs interactionSpecs;

    /**
     * The underlying REST connection
     */
    private final JDOConnection jdoConnection = new JDOConnection(){

        public void close() {
        }

        public Object getNativeConnection(
        ){
            return connection;
        }

    };

    /**
     * Retrieve a non-transactional interaction
     * 
     * @return a non-transactional interaction
     * @throws ResourceException 
     */
    public Interaction getInteraction2(
    ) throws ResourceException{
        return this.interaction2 == null ? 
            this.interaction2 = this.connection.createInteraction() : 
            this.interaction2;
    }

    /**
     * Retrieve the interaction specifications
     * 
     * @return the interaction specifications
     */
    public InteractionSpecs getInteractionSpecs(){
        return this.interactionSpecs;
    }

    /**
     * Unit of work factory method
     * 
     * @return a new unit of work
     */
    protected UnitOfWork_1 newUnitOfWork(
    ){
        return new UnitOfWork_1(
            this,
            this.connection,
            this.optimisticTransaction, 
            this.aspectSpecificContexts
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_5#isMultithreaded()
     */
    public boolean getMultithreaded() {
        return MULTITHREADED;
    }

    /**
     * Retrieve model.
     *
     * @return Returns the model.
     */
    final Model_1_0 getModel() {
        if(this.model == null) {
            this.model = Model_1Factory.getModel();
        }
        return this.model;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.StructureFactory_1_0#createStructure(java.lang.String, java.util.List, java.util.List)
     */
    public Structure_1_0 createStructure(
        String type,
        List<String> fieldNames,
        List<?> fieldValues
    ) throws ServiceException {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }    

    //------------------------------------------------------------------------
    // Implements ObjectFactory_1_1
    //------------------------------------------------------------------------

    public void evictAll(
    ){
        for(
            Iterator<?> i = this.mapping.values().iterator();
            i.hasNext();
        ){
            Object o = i.next();
            if(o instanceof Evictable) {
                ((Evictable)o).evict();
            }
        }
    }

    public void clear(
    ){
        if(MULTITHREADED) {
            this.unitsOfWork.clear();
        }
        super.clear();
    }

    //------------------------------------------------------------------------
    // Implements PersistenceManager
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#checkConsistency()
     */
    public void checkConsistency() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#currentTransaction()
     */
    public Transaction currentTransaction(
    ) {
        try {
            return this.getUnitOfWork();
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to get transaction",
                e,
                this
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistent(java.lang.Object)
     */
    public void deletePersistent(
        Object pc
    ) {
        try {
            ((DataObject_1)pc).objRemove();
        }
        catch(Exception e) {
            throw new JDOUserException(
                "unable to delete object",
                e,
                this
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void deletePersistentAll(Collection pcs) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void evictAll(Collection pcs) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#flush()
     */
    public void flush() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDataStoreConnection()
     */
    public JDOConnection getDataStoreConnection() {
        return this.jdoConnection;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDetachAllOnCommit()
     */
    public boolean getDetachAllOnCommit() {
        return this.getPersistenceManagerFactory().getDetachAllOnCommit();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getFetchPlan()
     */
    public FetchPlan getFetchPlan() {
        return this.fetchPlan;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getIgnoreCache()
     */
    public boolean getIgnoreCache() {
        return IGNORE_CACHE;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object, boolean)
     */
    public Object getObjectById(Object oid, boolean validate) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectId(java.lang.Object)
     */
    public Object getObjectId(Object pc) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectIdClass(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Class getObjectIdClass(Class cls) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public Collection getObjectsById(Collection oids) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopy(java.lang.Object)
     */
    public <T> T detachCopy(T arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopyAll(java.util.Collection)
     */
    public <T> Collection<T> detachCopyAll(Collection<T> arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopyAll(T[])
     */
    public <T> T[] detachCopyAll(T... arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(boolean, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public void evictAll(boolean arg0, Class arg1) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getCopyOnAttach()
     */
    public boolean getCopyOnAttach() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class)
     */
    public <T> Extent<T> getExtent(Class<T> arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class, boolean)
     */
    public <T> Extent<T> getExtent(Class<T> arg0, boolean arg1) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getFetchGroup(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public FetchGroup getFetchGroup(Class type, String name) {
        FetchGroup fetchGroup;
        synchronized(this.fetchGroups) {
            Map<Class<?>,FetchGroup> fetchGroups = this.fetchGroups.get(name);
            if(fetchGroups == null) {
                this.fetchGroups.put(
                    name,
                    fetchGroups = new IdentityHashMap<Class<?>,FetchGroup>()
                );
            }            
            fetchGroup = fetchGroups.get(type);
            if(fetchGroup == null) {
                fetchGroups.put(
                    type,
                    fetchGroup = new StandardFetchGroup(type, name)
                );
            }
        }
        return fetchGroup;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects()
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet)
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects(EnumSet<ObjectState> arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.lang.Class[])
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects(Class... arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet, java.lang.Class[])
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects(EnumSet<ObjectState> arg0, Class... arg1) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Class, java.lang.Object)
     */
    public <T> T getObjectById(Class<T> arg0, Object arg1) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(boolean, java.lang.Object[])
     */
    public Object[] getObjectsById(boolean arg0, Object... arg1) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getServerDate()
     */
    public Date getServerDate() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.lang.Object[])
     */
    public void evictAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[])
     */
    public Object[] getObjectsById(Object... oids) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.lang.Object[])
     */
    public void makeTransactionalAll(Object... pcs) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistent(java.lang.Object)
     */
    public <T> T makePersistent(T arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistentAll(T[])
     */
    public <T> T[] makePersistentAll(T... arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistentAll(java.util.Collection)
     */
    public <T> Collection<T> makePersistentAll(Collection<T> arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#addInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener, java.lang.Class[])
     */
    @SuppressWarnings("unchecked")
    public void addInstanceLifecycleListener(
        InstanceLifecycleListener listener,
        Class... classes) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(boolean, java.lang.Object[])
     */
    public void makeTransientAll(boolean arg0, Object... arg1) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newInstance(java.lang.Class)
     */
    public <T> T newInstance(Class<T> arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(boolean, java.lang.Object[])
     */
    public void retrieveAll(boolean arg0, Object... arg1) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setCopyOnAttach(boolean)
     */
    public void setCopyOnAttach(boolean arg0) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public Collection getObjectsById(Collection oids, boolean validate) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[], boolean)
     */
    public Object[] getObjectsById(Object[] oids, boolean validate) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evict(java.lang.Object)
     */
    public void evict(Object pc) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.lang.Object[])
     */
    public void deletePersistentAll(Object... pcs) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.lang.Object[])
     */
    public void makeNontransactionalAll(Object... pcs) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[])
     */
    public void makeTransientAll(Object... pcs) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.lang.Object[])
     */
    public void refreshAll(Object... pcs) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[])
     */
    public void retrieveAll(Object... pcs) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getPersistenceManagerFactory()
     */
    public PersistenceManagerFactory getPersistenceManagerFactory() {
        return this.factory;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getSequence(java.lang.String)
     */
    public Sequence getSequence(String name) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getTransactionalObjectId(java.lang.Object)
     */
    public Object getTransactionalObjectId(Object pc) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getUserObject()
     */
    public Object getUserObject() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getUserObject(java.lang.Object)
     */
    public Object getUserObject(Object key) {
        return 
            AspectObjectAcessor.class == key ? this.aspectSpecificContexts :
            null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactional(java.lang.Object)
     */
    public void makeNontransactional(
        Object pc
    ) {
        try {
            if(pc instanceof DataObject_1) {
                ((DataObject_1)pc).objMakeNontransactional();
            }
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to make object non-transactional",
                e,
                this
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void makeNontransactionalAll(Collection pcs) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactional(java.lang.Object)
     */
    public void makeTransactional(
        Object pc
    ) {
        try {
            if(pc instanceof DataObject_1) {
                ((DataObject_1)pc).objMakeTransactional();
            }
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to make object non-transactional",
                e,
                this
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void makeTransactionalAll(Collection pcs) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object)
     */
    public void makeTransient(Object pc) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object, boolean)
     */
    public void makeTransient(Object pc, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void makeTransientAll(Collection pcs) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[], boolean)
     */
    public void makeTransientAll(Object[] pcs, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public void makeTransientAll(Collection pcs, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newNamedQuery(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newNamedQuery(Class cls, String queryName) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newObjectIdInstance(java.lang.Class, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public Object newObjectIdInstance(Class pcClass, Object key) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery()
     */
    public Query newQuery() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Object)
     */
    public Query newQuery(Object compiled) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String)
     */
    public Query newQuery(String query) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Extent cln) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String, java.lang.Object)
     */
    public Query newQuery(String language, Object query) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, Collection cln) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, String filter) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Extent cln, String filter) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, Collection cln, String filter) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#putUserObject(java.lang.Object, java.lang.Object)
     */
    public Object putUserObject(Object key, Object val) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.PersistenceManager_1_0#getPersistenceManager(javax.resource.cci.InteractionSpec)
     */
    public PersistenceManager_1_0 getPersistenceManager(
        InteractionSpec interactionSpec
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refresh(java.lang.Object)
     */
    public void refresh(
        Object pc
    ) {
        try {
            if(pc instanceof DataObject_1) {
                ((DataObject_1)pc).objRefresh();
            }
        }
        catch(ServiceException e) {
            throw new JDOUserException(
                "Unable to refresh object",
                e,
                this
            );                
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll()
     */
    public void refreshAll() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void refreshAll(Collection pcs) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(javax.jdo.JDOException)
     */
    public void refreshAll(JDOException jdoe) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#removeInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener)
     */
    public void removeInstanceLifecycleListener(
        InstanceLifecycleListener listener) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#removeUserObject(java.lang.Object)
     */
    public Object removeUserObject(Object key) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object)
     */
    public void retrieve(Object pc) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object, boolean)
     */
    public void retrieve(Object pc, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void retrieveAll(Collection pcs) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public void retrieveAll(Collection pcs, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[], boolean)
     */
    public void retrieveAll(Object[] pcs, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setDetachAllOnCommit(boolean)
     */
    public void setDetachAllOnCommit(boolean flag) {
        if(flag != DETACH_ALL_ON_COMMIT) throw new UnsupportedOperationException(
            "The current implementation restricts detachAllOnCommit to " + DETACH_ALL_ON_COMMIT
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setIgnoreCache(boolean)
     */
    public void setIgnoreCache(boolean flag) {
        if(flag != IGNORE_CACHE) throw new UnsupportedOperationException(
            "The current implementation restricts ignoreCache to " + IGNORE_CACHE
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setMultithreaded(boolean)
     */
    public void setMultithreaded(boolean flag) {
        if(flag != MULTITHREADED) throw new UnsupportedOperationException(
            "The current implementation restricts multithreaded to " + MULTITHREADED
        );
        
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setUserObject(java.lang.Object)
     */
    public void setUserObject(Object o) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.PersistenceManager_1_0#getFeatureReplacingObjectById(java.lang.Object, java.lang.String)
     */
    public Object getFeatureReplacingObjectById(
        Object objectId,
        String featureName
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    
    //------------------------------------------------------------------------
    // Implements ObjectFactory_1_0
    //------------------------------------------------------------------------

    /**
     * Close the basic accessor.
     * <p>
     * After the close method completes, all methods on the ObjectFactory_1_0
     * instance except isClosed throw an ILLEGAL_STATE ServiceException.
     */
    public void close(
    ) {
        if(!isClosed()) {
            // TODO ?   this.connection.close();
            this.connection = null;
            clear();      
        }
    }

    /**
     * 
     */
    public boolean isClosed(
    ){  
        return this.connection == null;
    }

    private final void validateState(
    ) throws ServiceException{
        if(isClosed()) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            "Connection is closed"
        );  
    }

    /**
     * Return the unit of work associated with the current basic accessor.
     *
     * @return  the unit of work
     * @throws ServiceException 
     * @throws ServiceException 
     */
    UnitOfWork_1 getUnitOfWork(
    ) throws ServiceException{
        this.validateState();
        if(MULTITHREADED) {
            Thread thread = Thread.currentThread();
            UnitOfWork_1 unitOfWork = this.unitsOfWork.get(thread);
            if(unitOfWork == null) {
                this.unitsOfWork.put(
                    thread,
                    unitOfWork = newUnitOfWork()
                );
            }
            return unitOfWork;
        } 
        else {
            return this.unitOfWork;
        }
    }

    /**
     * Get an object from the object factory.
     * <p>
     * If an object with the given access path is already in the cache it is
     * returned, otherwise a new object is returned.
     *
     * @param       accessPath
     *              Access path of object to be retrieved.
     *
     * @return      A persistent object
     *
     * @exception   ServiceException    ILLEGAL_STATE
     *              if the object factory is closed
     */
    public Object getObjectById(
        Object accessPath
    ) {
        try {
            validateState();
            return accessPath == null ?
                null :
                    accessPath instanceof Path ?
                        ((DataObject_1)this.marshal(accessPath)) :
                            ((DataObject_1)this.marshal(new Path(accessPath.toString())));
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to get object",
                e,
                this
            );
        }
    }

    /**
     * Create an object
     *
     * @param       objectClass
     *              The model class of the object to be created
     *
     * @return      an object
     */
    public DataObject_1_0 newInstance(
        String objectClass
    ) throws ServiceException{
        validateState();
        if(getModel().isSubtypeOf(objectClass, "org:openmdx:base:ExtentCapable")){
            Path transientObjectId = StateCapables.newTransientObjectId();
            DataObject_1 object = new DataObject_1(objectClass, this, transientObjectId);
            super.cacheObject(transientObjectId, object);  
            return object;
        } else {
            return new DataObject_1(objectClass, this, null);
        }
    }

    
    //------------------------------------------------------------------------
    // Implements ReplyListener
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.ReplyListener#onReply(javax.resource.cci.MappedRecord)
     */
    public DataObject_1_0 receive(MappedRecord record) {
        try {
            ObjectHolder_2Facade facade = ObjectHolder_2Facade.newInstance(record);
            DataObject_1 dataObject = (DataObject_1) getObjectById(facade.getPath());
            dataObject.postLoad(record);
            return dataObject;
        } catch (Exception exception) {
            //
            // TODO add exception to resource warnings
            //
            Throwables.log(exception); 
            return null;
        }
    }

    
    //------------------------------------------------------------------------
    // Implements Manager_1_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Manager_1_0#invalidate(org.openmdx.compatibility.base.naming.Path, boolean)
     */
    void invalidate(
        Path accessPath, 
        boolean makeInaccessable
    ) throws ServiceException {
        if(isClosed()) return;
        ((DataObject_1)marshal(accessPath.getPrefix(accessPath.size() - 2))).setExistence(
            accessPath, 
            false
        );
        for (
            Iterator<Entry<Object, Object>> i = super.mapping.entrySet().iterator();
            i.hasNext();
        ){
            Map.Entry<?,?> e = i.next();
            if (((Path)e.getKey()).startsWith(accessPath)){
                ((DataObject_1)e.getValue()).invalidate(makeInaccessable);
                i.remove();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Manager_1_0#move(org.openmdx.compatibility.base.naming.Path, org.openmdx.compatibility.base.naming.Path)
     */
    void move(
        Path path,
        Path newValue
    ){
        if(isClosed()){
            path.setTo(newValue);   
        } else {
            Object object = super.mapping.remove(path);
            path.setTo(newValue);
            if(object != null) cacheObject(path, object);
        }
    }

    boolean containsKey(
        Path path
    ){
        return mapping.containsKey(path);
    }

    //------------------------------------------------------------------------
    // Extends CachingMarshaller
    //------------------------------------------------------------------------

    /**
     * Marshals path objects to Object_1_0 objects.
     *
     * @param  source    The object to be marshalled
     * 
     * @return           The marshalled object
     * 
     * @exception        ServiceException
     *                   Object can't be marshalled
     */
    protected Object createMarshalledObject (
        Object source
    ) throws ServiceException {
        Path objectId = (Path) source;
        Path id = new Path(objectId);
        Parents: for(
            int i = id.size();
            i > 6;
        ){
            id.remove(--i);
            id.remove(--i);
            DataObject_1_0 p = (DataObject_1_0) this.mapping.get(id);
            if(p == null) {
                break Parents;
            }
            if(p.jdoIsNew()) {
                return null;
            }
        }
        return new DataObject_1(
            objectId,
            this
        );
    }

    /**
     * Marshals an object
     *
     * @param  source    The object to be marshalled
     * 
     * @return           The marshalled object
     * 
     * @exception        ServiceException
     *                   DATA_CONVERSION: Object can't be marshalled
     */
    public Object marshal (
        Object source
    ) throws ServiceException{
        validateState();
        return source instanceof Path ? super.marshal(source) : source;
    }

    /**
     * Unmarshals an object
     *
     * @param  source   The marshalled object
     * 
     * @return          The unmarshalled object
     * 
     * @exception       ServiceException
     *                  Object can't be unmarshalled
     */
    public Object unmarshal (
        Object source
    ) throws ServiceException{
        validateState();
        if(source instanceof DataObject_1_0) {
            DataObject_1_0 object = (DataObject_1_0)source;
            // Unmarshal of aspects is aspect-type specific. Only handle when
            // object is in state new.
            if(object.jdoIsNew()) {
                if(getModel().isInstanceof(object, "org:openmdx:state2:BasicState")) {
                    DataObject_1_0 core = (DataObject_1_0) object.objGetValue("core");
                    if(core != null) {
                        Path corePath = core.jdoGetObjectId(); 
                        if(!StateCapables.isCoreObject(corePath)) {
                            return corePath;
                        }
                    }
                }
            }
            return object.jdoGetObjectId();
        } else {
            return source;
        }
    }


    //------------------------------------------------------------------------
    // Class members
    //------------------------------------------------------------------------

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3977865059621680436L;

    /**
     *  
     */
    private Model_1_0 model = null;

    /**
     *
     */ 
    private UnitOfWork_1 unitOfWork;

    /**
     *
     */ 
    private ConcurrentMap<Thread,UnitOfWork_1> unitsOfWork; 

    /**
     *
     */
    Connection connection;

    /**
     * Non-transactional interaction
     */
    private Interaction interaction2;

    /**
     * 
     */
    private final TransactionManager optimisticTransaction;

    /**
     * The persistence manager's fetch groups
     */
    private final Map<String,Map<Class<?>,FetchGroup>> fetchGroups = new HashMap<String,Map<Class<?>,FetchGroup>>();

    /**
     * The persistence manager's fetch plan
     */
    private final FetchPlan fetchPlan = new StandardFetchPlan();

    /**
     * The Apsect Specific Context instance 
     */
    private final AspectObjectDispatcher aspectSpecificContexts = new AspectObjectDispatcher();

    //------------------------------------------------------------------------
    // Class AspectObjectDispatcher
    //------------------------------------------------------------------------

    /**
     * Aspect Object Dispatcher
     */
    class AspectObjectDispatcher implements AspectObjectAcessor {

        /**
         * Retrueve the object's state
         * 
         * @param objectId
         * @param optional
         * 
         * @return the object's state
         */
        private TransactionalState_1 getState(
            Object objectId,
            boolean optional
        ){
            DataObject_1 dataObject = (DataObject_1) getObjectById(objectId);
            if(dataObject == null) throw BasicException.initHolder(
                new JDOFatalInternalException(
                    AspectObjectAcessor.class.getSimpleName() + " access failure: Data object not found",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND,
                        new BasicException.Parameter("objectId", objectId)
                    )
                )
            );
            try {
                return dataObject.getState(optional);
            } 
            catch (ServiceException exception) {
                throw BasicException.initHolder(
                    new JDOFatalInternalException(
                        AspectObjectAcessor.class.getSimpleName() + " access failure: Transactional state inaccessable",
                        BasicException.newEmbeddedExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ILLEGAL_STATE,
                            new BasicException.Parameter("objectId", objectId)
                        )
                    )
                );
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.AspectSpecificContexts#get(java.lang.Object, java.lang.Class)
         */
        public Object get(
            Object objectId, 
            Class<?> aspect
        ) {
            try {
                UnitOfWork_1 unitOfWork = DataObjectManager_1.this.getUnitOfWork();
                if(unitOfWork.isActive()) {
                    TransactionalState_1 state = this.getState(objectId, true);
                    return state == null ? null : state.getContext(aspect);
                }
                else {
                    Map<Class<?>,Object> contexts = this.sharedContexts.get(objectId);
                    return contexts == null ?
                        null :
                        contexts.get(aspect);
                }
            }
            catch(Exception e) {
                throw new RuntimeServiceException(e);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.AspectSpecificContexts#put(java.lang.Object, java.lang.Class, java.lang.Object)
         */
        public void put(
            Object objectId, 
            Class<?> aspect, 
            Object context
        ) {
            try {
                UnitOfWork_1 unitOfWork = DataObjectManager_1.this.getUnitOfWork();
                if(unitOfWork.isActive()) {
                    this.getState(objectId, false).setContext(aspect, context);
                }
                else {
                    Map<Class<?>,Object> contexts = this.sharedContexts.get(objectId);
                    if(contexts == null) {
                        this.sharedContexts.putIfAbsent(
                            objectId,
                            contexts = new IdentityHashMap<Class<?>,Object>()
                        );
                    }
                    contexts.put(
                        aspect,
                        context
                    );
                }
            }
            catch(Exception e) {
                throw new RuntimeServiceException(e);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.AspectSpecificContexts#remove(java.lang.Object, java.lang.Class)
         */
        public void remove(
            Object objectId, 
            Class<?> aspect
        ) {
            try {
                UnitOfWork_1 unitOfWork = DataObjectManager_1.this.getUnitOfWork();
                if(unitOfWork.isActive()) {
                    TransactionalState_1 state = getState(objectId, true);
                    if(state != null) {
                        state.removeContext(aspect);
                    }
                }
                else {
                    Map<Class<?>,Object> contexts = this.sharedContexts.get(objectId);
                    if(contexts != null) {
                        contexts.remove(aspect);
                    }
                }
            }
            catch(Exception e) {
                throw new RuntimeServiceException(e);
            }
        }

        //-------------------------------------------------------------------
        public void clear(
        ) {
            this.sharedContexts.clear();
        }
        
        //-------------------------------------------------------------------
        // Members
        //-------------------------------------------------------------------
        private ConcurrentMap<Object,Map<Class<?>,Object>> sharedContexts = new ConcurrentHashMap<Object,Map<Class<?>,Object>>();
    }

}
