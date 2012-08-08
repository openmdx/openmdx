/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Connection_1.java,v 1.20 2009/03/09 17:11:21 hburger Exp $
 * Description: Dataprovider connection implementation
 * Revision:    $Revision: 1.20 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/09 17:11:21 $
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
package org.openmdx.application.dataprovider.accessor;

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
import javax.jdo.JDOUserException;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;
import javax.transaction.UserTransaction;

import org.openmdx.application.dataprovider.cci.OptimisticTransaction_2_0;
import org.openmdx.application.dataprovider.cci.RequestCollection;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.PersistenceManager_1_0;
import org.openmdx.base.accessor.cci.Structure_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.CachingMarshaller;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Model_1_6;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.StandardFetchGroup;
import org.openmdx.base.persistence.spi.StandardFetchPlan;
import org.openmdx.compatibility.state1.spi.StateCapables;
import org.openmdx.kernel.exception.BasicException;

/**
 * Object factory implementation which allows to access a dataprovider.
 * <p>
 * A connection can be constructed with the following transaction management policies:
 * <p>
 * <pre>
 *                             transaction    containerManaged    optimistic
 * 
 *  a) optimistic server-side Tx   null           false              true
 *     (e.g. EJB managed) 
 *  b) optimistic client-side Tx  !null           false              true
 *  c) non-optimistic             !null           false              false
 * 
 *  d) external control*           null           true               false 
 * 
 * </pre>
 * <p> 
 * *external control. Transaction management is NOT under the control of the connection. 
 * As a consequence begin() and commit() must not be called on the unit of work, i.e. 
 * are not supported. The synchronization points of externally controlled transactions 
 * are afterBegin() and beforeCompletion().
 * <p>  
 * A non-optimistic connection coordinates user transactions. As a consequence it requires 
 * a provider with an transactionPolicyIsNew=false interaction policy, i.e. a Provider which
 * is not itself transaction coordinator.
 */
public class Connection_1
    extends CachingMarshaller
    implements PersistenceManager_1_0
{

    /**
     * Constructor 
     * 
     * @param channel 
     * @param userTransaction 
     * @param optimisticTransaction 
     * @param multithreaded 
     * @param defaultQualifierType UID, UUID, URN, SEQUENCE
     * @param containerManagedUnitOfWork
     * @param transactional 
     * @param optimistic 
     * @throws ServiceException
     */
    private Connection_1(
        RequestCollection delegation,
        boolean transactionPolicyIsNew,
        boolean persistentNewObjectBecomeTransientUponRollback,
        UserTransaction userTransaction, 
        OptimisticTransaction_2_0 optimisticTransaction, 
        boolean multithreaded, 
        String defaultQualifierType, 
        boolean containerManagedUnitOfWork, 
        boolean transactional, 
        boolean optimistic
    ) throws ServiceException{
        this.multithreaded = multithreaded;
        this.provider = new Channel(
            delegation, 
            transactionPolicyIsNew,
            persistentNewObjectBecomeTransientUponRollback
        );
        this.defaultQualifierType = defaultQualifierType;
        this.containerManagedUnitOfWork = containerManagedUnitOfWork;
        this.transactional = transactional;
        this.optimistic = optimistic;
        this.userTransaction = userTransaction;
        this.optimisticTransaction = optimisticTransaction;
        if(!optimistic && transactional && userTransaction == null) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "Transactional non-optimistic units of work require a user transaction",
            new BasicException.Parameter("transactional", transactional),
            new BasicException.Parameter("containerManaged", containerManagedUnitOfWork),
            new BasicException.Parameter("optimistic", optimistic)
        );
        if(multithreaded) {
            this.unitOfWork = null;
            this.unitsOfWork = new ConcurrentHashMap<Thread,UnitOfWork_1>();
        } else {
            this.unitOfWork = newUnitOfWork();
            this.unitsOfWork = null;
        }
    }

    /**
     * Constructor
     *
     * @param   providers
     *           the provider connected to this router
     * @param    containerManagedUnitOfWork
     *               defines whether the unit of work is container managed
     * @param defaultQualifierType UID, UUID, URN, SEQUENCE
     * 
     * @throws ServiceException
     */
    public Connection_1(
        RequestCollection delegation,
        boolean transactionPolicyIsNew,
        boolean containerManagedUnitOfWork,
        String defaultQualifierType
    ) throws ServiceException{
        this(
            delegation,
            transactionPolicyIsNew,
            true, // persistentNewObjectBecomeTransientUponRollback
            null, // userTransaction
            null, // optimisticTransaction
            true, // multithreaded
            defaultQualifierType, 
            containerManagedUnitOfWork, 
            transactionPolicyIsNew, // transactional
            !containerManagedUnitOfWork // optimistic
        );
    }

    /**
     * Constructor
     *
     * @param   providers
     *           the provider connected to this router
     * @param    containerManagedUnitOfWork
     *               defines whether the unit of work is container managed
     * @param defaultQualifierType UID, UUID, URN, SEQUENCE
     * 
     * @throws ServiceException
     */
    public Connection_1(
        RequestCollection delegation,
        boolean transactionPolicyIsNew,
        OptimisticTransaction_2_0 optimisticTransaction,
        String defaultQualifierType
    ) throws ServiceException{
        this(
            delegation,
            transactionPolicyIsNew,
            true, // persistentNewObjectBecomeTransientUponRollback
            null, // userTransaction
            optimisticTransaction,
            true, // multithreaded 
            defaultQualifierType,  
            false, // containerManagedUnitOfWork,
            false, // transactional (false at provider level)
            true // optimistic
        );
    }

    
    /**
     * Constructor 
     *
     * @param channel
     * @param userTransaction
     * @param containerManaged 
     * @param optimistic
     * @param defaultQualifierType UID, UUID, URN, SEQUENCE
     * @throws ServiceException
     */
    public Connection_1(
        RequestCollection delegation,
        boolean transactionPolicyIsNew,
        UserTransaction userTransaction,
        boolean containerManaged, 
        boolean optimistic, 
        String defaultQualifierType
    ) throws ServiceException{
        this(
            delegation,
            transactionPolicyIsNew,
            true, // persistentNewObjectBecomeTransientUponRollback
            userTransaction, 
            null, // optimisticTransaction
            true, // multithreaded
            defaultQualifierType, 
            containerManaged, 
            transactionPolicyIsNew, 
            optimistic
        );
    }


    /**
     * Constructs a Manager.
     *
     * @param   providers
     *           the provider connected to this router
     * @param    containerManagedUnitOfWork
     *               defines whether the unit of work is container managed
     * 
     * @throws ServiceException
     */
    public Connection_1(
        RequestCollection delegation,
        boolean transactionPolicyIsNew,
        boolean containerManagedUnitOfWork
    ) throws ServiceException{
        this(
            delegation,
            transactionPolicyIsNew,
            true, // persistentNewObjectBecomeTransientUponRollback
            null, // userTransaction
            null, // optimisticTransaction
            true, // multithreaded
            "UUID", // defaultQualifierType 
            containerManagedUnitOfWork, // transactional
            transactionPolicyIsNew,
            !containerManagedUnitOfWork // optimistic
        );
    }

    /**
     * Tells whether the connection is multi-threaded.
     */
    private final boolean multithreaded;
    
    /**
     * Retrieves the defaultQualifierType.
     * 
     * @return the defaultQualifierType
     */
    public String getDefaultQualifierType() {
        return this.defaultQualifierType;
    }

    /**
     * Unit of work factory method
     * 
     * @return a new unit of work
     */
    protected UnitOfWork_1 newUnitOfWork(
    ){
        return new UnitOfWork_1(
            this.provider, 
            this.transactional, 
            this.containerManagedUnitOfWork, 
            this.optimistic, 
            this.userTransaction, 
            this.optimisticTransaction
        );
    }

    
    //------------------------------------------------------------------------
    // Implements DataAccessService_1_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_5#isMultithreaded()
     */
    public boolean getMultithreaded() {
        return this.multithreaded;
    }

    /**
     * Retrieve model.
     *
     * @return Returns the model.
     */
    public final Model_1_6 getModel() {
        if(this.model == null) {
            this.model = Model_1Factory.getModel();
        }
        return this.model;
    }

    /**
     * Set model.
     * 
     * @param model The model to set.
     * 
     * @deprecated setting the model is no longer required
     */
    public final void setModel(
        Model_1_0 model
    ) {
        this.model = (Model_1_6) model;
    }    

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataAccessService_1_0#getObjectFactory(javax.resource.cci.InteractionSpec)
     */
    public PersistenceManager_1_0 getPersistenceManager(
        InteractionSpec interactionSpec
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
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
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_3#getPrincipalChain()
     */
    public List<String> getPrincipalChain(
    ) {
        return this.provider.getPrincipalChain();
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
        if(this.multithreaded) {
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
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDetachAllOnCommit()
     */
    public boolean getDetachAllOnCommit() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
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
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
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
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
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
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
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
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setIgnoreCache(boolean)
     */
    public void setIgnoreCache(boolean flag) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setMultithreaded(boolean)
     */
    public void setMultithreaded(boolean flag) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setUserObject(java.lang.Object)
     */
    public void setUserObject(Object o) {
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
            this.provider.close();
            this.provider = null;
            clear();      
        }
    }

    /**
     * 
     */
    public boolean isClosed(
    ){  
        return this.provider == null;
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
     */
    UnitOfWork_1 getUnitOfWork(
    ) throws ServiceException{
        this.validateState();
        if(this.multithreaded) {
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
     * Get an object from the basic accessor.
     * <p>
     * If an object with the given id is already in the cache it is returned,
     * otherwise a new object is returned.
     *
     * @param       objectId
     *              Identity of object to be retrieved.
     *
     * @return      A managed object
     */
    private DataObject_1 getObjectById(
        Path objectId
    ) throws ServiceException{
        validateState();
        return (DataObject_1)marshal(objectId);
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
            return accessPath == null ?
                null :
                accessPath instanceof Path ?
                    this.getObjectById((Path)accessPath) :
                    this.getObjectById(new Path(accessPath.toString()));
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
    // Implements Manager_1_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Manager_1_0#fetched(org.openmdx.compatibility.base.naming.Path, javax.resource.cci.MappedRecord)
     */
    public void fetched(
        Path accessPath, 
        MappedRecord attributes
    ) throws ServiceException {
        if(isClosed()) return;
        ((DataObject_1)this.marshal(accessPath)).fetched(attributes);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Manager_1_0#updated(org.openmdx.compatibility.base.naming.Path, javax.resource.cci.MappedRecord)
     */
    public void updated(
        Path accessPath, 
        MappedRecord attributes
    ) throws ServiceException {
        if(isClosed()) return;
        if(this.containsKey(accessPath))((DataObject_1)this.marshal(accessPath)).updated(attributes);
    } 

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Manager_1_0#invalidate(org.openmdx.compatibility.base.naming.Path, boolean)
     */
    public void invalidate(
        Path accessPath, 
        boolean makeInaccessable
    ) throws ServiceException {
        if(isClosed()) return;
        getObjectById(
            accessPath.getPrefix(accessPath.size() - 2)
        ).setExistence(
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
    public void move(
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

    public boolean containsKey(
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
            this,
            this.provider
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
                        Path corePath = (Path) core.jdoGetObjectId(); 
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
    private Model_1_6 model = null;

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
    private Channel provider;

    /**
     * 
     */
    private final boolean transactional;

    /**
     * 
     */
    private final boolean optimistic;

    /**
     * 
     */
    private final UserTransaction userTransaction;

    /**
     * 
     */
    private final OptimisticTransaction_2_0 optimisticTransaction;

    
    /**
     * UID, UUID, URN, SEQUENCE
     */
    private String defaultQualifierType;

    /**
     * Container managed units of work are either non transactional or part of
     * a bigger unit of work.
     */
    private final boolean containerManagedUnitOfWork;

    /**
     * The persistence manager's fetch groups
     */
    private final Map<String,Map<Class<?>,FetchGroup>> fetchGroups = new HashMap<String,Map<Class<?>,FetchGroup>>();
    
    /**
     * The persistence manager's fetch plan
     */
    private final FetchPlan fetchPlan = new StandardFetchPlan();

}
