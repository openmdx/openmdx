/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Manager_1.java,v 1.15 2009/02/11 19:05:25 hburger Exp $
 * Description: SPICE Object Layer: Manager implementation
 * Revision:    $Revision: 1.15 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/11 19:05:25 $
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.accessor.view;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jdo.Extent;
import javax.jdo.FetchGroup;
import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.JDOUserException;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.datastore.DataStoreCache;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.resource.cci.InteractionSpec;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.PersistenceManagerFactory_1_0;
import org.openmdx.base.accessor.cci.PersistenceManager_1_0;
import org.openmdx.base.accessor.cci.Structure_1_0;
import org.openmdx.base.accessor.spi.Delegating_1_0;
import org.openmdx.base.accessor.spi.ListStructure_1;
import org.openmdx.base.accessor.spi.MarshallingStructure_1;
import org.openmdx.base.event.InstanceCallbackEvent;
import org.openmdx.base.event.InstanceCallbackListener;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.CachingMarshaller;
import org.openmdx.base.mof.cci.Model_1_6;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.compatibility.state1.spi.StateCapables;
import org.openmdx.kernel.exception.BasicException;

/**
 * Manager_1 implementation.
 * <p>
 * The manager returns the same object for a given object id as long as it is not 
 * garbage collected.
 */
public class Manager_1 
    extends CachingMarshaller
    implements InstanceCallbackListener, Serializable, PersistenceManager_1_0, PersistenceManagerFactory_1_0  {

    /**
     * Constructs a Manager.
     *
     * @param   connection
     *          the interaction object to be used by this manager
     */
    public Manager_1(
        PersistenceManager_1_0 connection
    ) throws ServiceException{
        this.connection = connection;
        this.objectFactories = new ConcurrentHashMap<InteractionSpec, Manager_1>();
        this.objectFactories.put(InteractionSpecs.NULL, this);
        this.interactionSpec = null;
    }

    /**
     * Constructor 
     *
     * @param connection
     * @param objectFactories
     * @param interactionSpec
     */
    private Manager_1(
        PersistenceManager_1_0 connection,
        ConcurrentMap<InteractionSpec, Manager_1> objectFactories,
        InteractionSpec interactionSpec
    ){
        this.connection = connection;
        this.objectFactories = objectFactories;
        this.interactionSpec = interactionSpec;
    }
        
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 4121130329538180151L;

    /**
     *  
     */
    private PersistenceManager_1_0 connection;

    /**
     *  
     */
    private final InteractionSpec interactionSpec;

    /**
     * 
     */
    private ConcurrentMap<InteractionSpec, Manager_1> objectFactories;
    
    /**
     * The model repository
     */
    private Model_1_6 model;
    
    /**
     * Return connection assigned to this manager.
     */
    public PersistenceManager_1_0 getConnection(
    ) {
        return this.connection;
    }

    /**
     * Retrieve the interaction spec associated with this object factory.
     * 
     * @return the interaction spec associated with this object factory
     */
    InteractionSpec getInteractionSpec(){
        return this.interactionSpec;
    }

    protected Model_1_6 getModel(){
        if(this.model == null) {
            this.model = Model_1Factory.getModel();
        }
        return this.model;
    }
    
    
    //-----------------------------------------------------------------------
    // PersistenceManagerFactory_1_0
    //-----------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_4#getObjectFactory(javax.resource.cci.InteractionSpec)
     */
    public Manager_1 getPersistenceManager(
        InteractionSpec interactionSpec
    ) {
        InteractionSpec key = interactionSpec == null ? InteractionSpecs.NULL : interactionSpec;
        Manager_1 objectFactory = this.objectFactories.get(key);
        if(objectFactory == null) {
            Manager_1 concurrent = this.objectFactories.putIfAbsent(
                key,
                objectFactory = new Manager_1(
                    this.connection,
                    this.objectFactories,
                    interactionSpec
                )
            );
            if(concurrent != null) {
                objectFactory = concurrent;
            }
        }
        return objectFactory;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getPersistenceManager()
     */
    public PersistenceManager getPersistenceManager(
    ) {
        return this.getPersistenceManager(null);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getPersistenceManager(java.lang.String, java.lang.String)
     */
    public PersistenceManager getPersistenceManager(
        String userid,
        String password
    ) {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.PersistenceManagerFactory_1_0#getBindingPackageSuffix()
     */
    public String getBindingPackageSuffix() {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.PersistenceManagerFactory_1_0#setBindingPackageSuffix(java.lang.String)
     */
    public void setBindingPackageSuffix(String bindingPackageSuffix) {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionDriverName()
     */
    public String getConnectionDriverName() {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory()
     */
    public Object getConnectionFactory() {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory2()
     */
    public Object getConnectionFactory2() {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactory2Name()
     */
    public String getConnectionFactory2Name() {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionFactoryName()
     */
    public String getConnectionFactoryName() {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionURL()
     */
    public String getConnectionURL() {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getConnectionUserName()
     */
    public String getConnectionUserName() {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getDataStoreCache()
     */
    public DataStoreCache getDataStoreCache() {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getMapping()
     */
    public String getMapping() {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getNontransactionalRead()
     */
    public boolean getNontransactionalRead() {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getNontransactionalWrite()
     */
    public boolean getNontransactionalWrite() {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getOptimistic()
     */
    public boolean getOptimistic() {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getProperties()
     */
    public Properties getProperties() {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getRestoreValues()
     */
    public boolean getRestoreValues() {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getRetainValues()
     */
    public boolean getRetainValues() {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionDriverName(java.lang.String)
     */
    public void setConnectionDriverName(String driverName) {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory(java.lang.Object)
     */
    public void setConnectionFactory(Object connectionFactory) {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory2(java.lang.Object)
     */
    public void setConnectionFactory2(Object connectionFactory) {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactory2Name(java.lang.String)
     */
    public void setConnectionFactory2Name(String connectionFactoryName) {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionFactoryName(java.lang.String)
     */
    public void setConnectionFactoryName(String connectionFactoryName) {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionPassword(java.lang.String)
     */
    public void setConnectionPassword(String password) {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionURL(java.lang.String)
     */
    public void setConnectionURL(String url) {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setConnectionUserName(java.lang.String)
     */
    public void setConnectionUserName(String userName) {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setMapping(java.lang.String)
     */
    public void setMapping(String mapping) {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setNontransactionalRead(boolean)
     */
    public void setNontransactionalRead(boolean flag) {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setNontransactionalWrite(boolean)
     */
    public void setNontransactionalWrite(boolean flag) {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setOptimistic(boolean)
     */
    public void setOptimistic(boolean flag) {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setRestoreValues(boolean)
     */
    public void setRestoreValues(boolean restoreValues) {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setRetainValues(boolean)
     */
    public void setRetainValues(boolean flag) {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#supportedOptions()
     */
    @SuppressWarnings("unchecked")
    public Collection supportedOptions() {
        throw new UnsupportedOperationException("Operation not supported by Manager_1");
    }
    
    //------------------------------------------------------------------------
    // Implements PersistenceManager_1_0
    //------------------------------------------------------------------------

    /**
     * Close the basic accessor.
     * <p>
     * After the close method completes, all methods on the ObjectFactory_1_0
     * instance except isClosed throw a ILLEGAL_STATE RuntimeServiceException.
     */
    public void close(
    ) {
        if (isClosed()) return;
        this.connection.close();
        this.connection = null;
        super.clear();
    }

    /**
     * Tells whether the object factory has been closed.
     * 
     * @return <code>true</code> if the object factory has been closed
     */
    public boolean isClosed(
    ){
        return this.connection == null;
    }

    /**
     * 
     * @throws ServiceException
     */
    private void validateState(
    ) throws ServiceException{
        if(isClosed()) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            "The manager is closed"
        ); 
    }

    /**
     * Get an object from the object factory.
     * <p>
     * If an object with the given id is already in the cache it is returned,
     * otherwise a new object is returned.
     *
     * @param       objectId
     *              Access path of object to be retrieved.
     *
     * @return      A persistent object
     */
    private DataObject_1_0 getObjectById(
        Path objectId
    ) throws ServiceException{
        validateState();
        DataObject_1_0 dataObject = (DataObject_1_0) this.connection.getObjectById(
            objectId
        );
        if(
            StateCapables.isCoreObject(objectId) &&
            !(Boolean)dataObject.objGetValue("validTimeUnique")
         ){
            dataObject = (DataObject_1_0) this.connection.getObjectById(
                new Path(objectId.getBase())
            );
        }
        return (DataObject_1_0) marshal(dataObject);
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
            return accessPath == null ? null : getObjectById(
                accessPath instanceof Path ? (Path) accessPath : new Path(accessPath.toString())
            );
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to get object",
                e,
                this
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#addInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener, java.lang.Class[])
     */
    @SuppressWarnings("unchecked")
    public void addInstanceLifecycleListener(
        InstanceLifecycleListener listener,
        Class... classes
    ) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#checkConsistency()
     */
    public void checkConsistency() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#currentTransaction()
     */
    public Transaction currentTransaction(
    ) {
        try {
            this.validateState();
            return this.connection.currentTransaction();
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
            ((ObjectView_1_0)pc).objDelete();
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to delete object",
                e,
                this
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.lang.Object[])
     */
    public void deletePersistentAll(Object... pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void deletePersistentAll(Collection pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopy(java.lang.Object)
     */
    public <T> T detachCopy(T pc) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopyAll(java.util.Collection)
     */
    public <T> Collection<T> detachCopyAll(Collection<T> pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopyAll(java.lang.Object[])
     */
    public <T> T[] detachCopyAll(T... pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evict(java.lang.Object)
     */
    public void evict(Object pc) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.lang.Object[])
     */
    public void evictAll(Object... pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void evictAll(Collection pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#flush()
     */
    public void flush() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(boolean, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public void evictAll(boolean subclasses, Class pcClass) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getCopyOnAttach()
     */
    public boolean getCopyOnAttach() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getFetchGroup(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public FetchGroup getFetchGroup(Class type, String name) {
        return this.connection.getFetchGroup(type, name);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects()
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet)
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects(EnumSet<ObjectState> states) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.lang.Class[])
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects(Class... classes) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet, java.lang.Class[])
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects(EnumSet<ObjectState> states, Class... classes) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(boolean, java.lang.Object[])
     */
    public Object[] getObjectsById(boolean validate, Object... oids) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getServerDate()
     */
    public Date getServerDate() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(boolean, java.lang.Object[])
     */
    public void makeTransientAll(boolean useFetchPlan, Object... pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(boolean, java.lang.Object[])
     */
    public void retrieveAll(boolean useFetchPlan, Object... pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setCopyOnAttach(boolean)
     */
    public void setCopyOnAttach(boolean flag) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#addFetchGroups(javax.jdo.FetchGroup[])
     */
    public void addFetchGroups(FetchGroup... arg0) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getFetchGroups()
     */
    @SuppressWarnings("unchecked")
    public Set getFetchGroups() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getName()
     */
    public String getName() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getPersistenceManagerProxy()
     */
    public PersistenceManager getPersistenceManagerProxy() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getPersistenceUnitName()
     */
    public String getPersistenceUnitName() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getReadOnly()
     */
    public boolean getReadOnly() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getServerTimeZoneID()
     */
    public String getServerTimeZoneID() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getTransactionIsolationLevel()
     */
    public String getTransactionIsolationLevel() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#getTransactionType()
     */
    public String getTransactionType() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#removeAllFetchGroups()
     */
    public void removeAllFetchGroups() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#removeFetchGroups(javax.jdo.FetchGroup[])
     */
    public void removeFetchGroups(FetchGroup... arg0) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setName(java.lang.String)
     */
    public void setName(String name) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setPersistenceUnitName(java.lang.String)
     */
    public void setPersistenceUnitName(String name) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setReadOnly(boolean)
     */
    public void setReadOnly(boolean arg0) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setServerTimeZoneID(java.lang.String)
     */
    public void setServerTimeZoneID(String timezoneid) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setTransactionIsolationLevel(java.lang.String)
     */
    public void setTransactionIsolationLevel(String arg0) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManagerFactory#setTransactionType(java.lang.String)
     */
    public void setTransactionType(String name) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDataStoreConnection()
     */
    public JDOConnection getDataStoreConnection() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDetachAllOnCommit()
     */
    public boolean getDetachAllOnCommit() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class)
     */
    public <T> Extent<T> getExtent(Class<T> persistenceCapableClass) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class, boolean)
     */
    public <T> Extent<T> getExtent(
        Class<T> persistenceCapableClass,
        boolean subclasses
    ) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getFetchPlan()
     */
    public FetchPlan getFetchPlan() {
        return this.connection.getFetchPlan();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getIgnoreCache()
     */
    public boolean getIgnoreCache() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object, boolean)
     */
    public Object getObjectById(Object oid, boolean validate) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Class, java.lang.Object)
     */
    public <T> T getObjectById(Class<T> cls, Object key) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectId(java.lang.Object)
     */
    public Object getObjectId(Object pc) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectIdClass(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Class getObjectIdClass(Class cls) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public Collection getObjectsById(Collection oids) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[])
     */
    public Object[] getObjectsById(Object... oids) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public Collection getObjectsById(Collection oids, boolean validate) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[], boolean)
     */
    public Object[] getObjectsById(Object[] oids, boolean validate) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getPersistenceManagerFactory()
     */
    public PersistenceManagerFactory getPersistenceManagerFactory(
    ) {
        return this;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getSequence(java.lang.String)
     */
    public Sequence getSequence(String name) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getTransactionalObjectId(java.lang.Object)
     */
    public Object getTransactionalObjectId(Object pc) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getUserObject()
     */
    public Object getUserObject() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getUserObject(java.lang.Object)
     */
    public Object getUserObject(Object key) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactional(java.lang.Object)
     */
    public void makeNontransactional(Object pc) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.lang.Object[])
     */
    public void makeNontransactionalAll(Object... pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void makeNontransactionalAll(Collection pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistent(java.lang.Object)
     */
    public <T> T makePersistent(T pc) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistentAll(java.lang.Object[])
     */
    public <T> T[] makePersistentAll(T... pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistentAll(java.util.Collection)
     */
    public <T> Collection<T> makePersistentAll(Collection<T> pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactional(java.lang.Object)
     */
    public void makeTransactional(Object pc) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.lang.Object[])
     */
    public void makeTransactionalAll(Object... pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void makeTransactionalAll(Collection pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object)
     */
    public void makeTransient(Object pc) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object, boolean)
     */
    public void makeTransient(Object pc, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[])
     */
    public void makeTransientAll(Object... pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void makeTransientAll(Collection pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[], boolean)
     */
    public void makeTransientAll(Object[] pcs, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public void makeTransientAll(Collection pcs, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newInstance(java.lang.Class)
     */
    public <T> T newInstance(Class<T> pcClass) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newNamedQuery(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newNamedQuery(Class cls, String queryName) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newObjectIdInstance(java.lang.Class, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public Object newObjectIdInstance(Class pcClass, Object key) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery()
     */
    public Query newQuery() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Object)
     */
    public Query newQuery(Object compiled) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String)
     */
    public Query newQuery(String query) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Extent cln) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String, java.lang.Object)
     */
    public Query newQuery(String language, Object query) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, Collection cln) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, String filter) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Extent cln, String filter) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Query newQuery(Class cls, Collection cln, String filter) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#putUserObject(java.lang.Object, java.lang.Object)
     */
    public Object putUserObject(Object key, Object val) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refresh(java.lang.Object)
     */
    public void refresh(
        Object pc
    ) {
        try {
            if(pc instanceof ObjectView_1_0) {
                ((ObjectView_1_0)pc).objRefresh();
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
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.lang.Object[])
     */
    public void refreshAll(Object... pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void refreshAll(Collection pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(javax.jdo.JDOException)
     */
    public void refreshAll(JDOException jdoe) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#removeInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener)
     */
    public void removeInstanceLifecycleListener(
        InstanceLifecycleListener listener) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#removeUserObject(java.lang.Object)
     */
    public Object removeUserObject(Object key) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object)
     */
    public void retrieve(Object pc) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object, boolean)
     */
    public void retrieve(Object pc, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void retrieveAll(Collection pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[])
     */
    public void retrieveAll(Object... pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public void retrieveAll(Collection pcs, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[], boolean)
     */
    public void retrieveAll(Object[] pcs, boolean useFetchPlan) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setDetachAllOnCommit(boolean)
     */
    public void setDetachAllOnCommit(boolean flag) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setIgnoreCache(boolean)
     */
    public void setIgnoreCache(boolean flag) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setMultithreaded(boolean)
     */
    public void setMultithreaded(boolean flag) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setUserObject(java.lang.Object)
     */
    public void setUserObject(Object o) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
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
        return (DataObject_1_0)marshal(
            this.connection.newInstance(objectClass)
        );
    }

    /**
     * Tells whether the persistence manager represented by this connection is multithreaded or not
     * 
     * @return <code> true</code> if the the persistence manager is multithreaded 
     */
    public boolean getMultithreaded(
    ) {
        return this.connection.getMultithreaded();
    }
    
    /**
     * Create a structure
     *
     * @param       type
     *              The type of the structure to be created
     * @param       fieldNames
     *              The names of the structure's fields
     * @param       fieldValues
     *              The structure's field values
     *
     * @return      a structure
     */
    public Structure_1_0 createStructure(
        String type,
        List<String> fieldNames,
        List<?> fieldValues
    ) throws ServiceException {
        validateState();
        return new MarshallingStructure_1(
            new ListStructure_1(type,fieldNames,fieldValues),
            this
        );
    }

    /**
     * Test whether there is no layer mismatch.
     * 
     * @param initialValues
     * 
     * @return the initialValues' delegate
     */
    DataObject_1_0 getDelegate(
        DataObject_1_0 initialValues
    ) throws ServiceException {
        try {
            return ((ObjectView_1) initialValues).objGetDelegate();
        } catch (ClassCastException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED, 
                "object extension requires an object instanceof '" +
                ObjectView_1.class.getName() + 
                "'. This problem is likely to occur in JMI plugins when using extend<X>(refObject).",
                new BasicException.Parameter(
                    "class", 
                    initialValues == null ? null : initialValues.getClass().getName()
                )
            );
        }
    }

    /**
     * Clears the cache 
     */
    public void clear(
    ){
        super.clear();
        this.connection.clear();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_3#evict()
     */
    public void evictAll(
    ) {
        this.connection.evictAll();
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
     * @exception        RuntimeServiceException
     *                   DATA_CONVERSION: Object can't be marshalled
     */
    protected Object createMarshalledObject (
        Object source
    ) throws ServiceException{
        validateState();
        return new ObjectView_1(
            this,
            (DataObject_1_0) source
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
     *                   Object can't be marshalled
     */
    public Object marshal (
        Object source
    ) throws ServiceException{
        validateState();
        return  
            source instanceof ObjectView_1_0 ? source :
            source instanceof DataObject_1_0 ? super.marshal(source) : 
            source;
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
    ) throws ServiceException {
        validateState();
        return source instanceof ObjectView_1 ? ((Delegating_1_0)source).objGetDelegate() : source;
    }
    
    
    //------------------------------------------------------------------------
    // Implements InstanceCallbackListener
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.event.InstanceCallbackListener#postCreate(org.openmdx.compatibility.base.event.InstanceCallbackEvent)
     */
    public void postCreate(
        InstanceCallbackEvent event
    ) throws ServiceException {
        // Can't be propagated to instance level
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.event.InstanceCallbackListener#postLoad(org.openmdx.base.event.InstanceCallbackEvent)
     */
    public void postLoad(
        InstanceCallbackEvent event
    ) throws ServiceException {
        ObjectView_1 value = (ObjectView_1) super.mapping.get(event.getSource());
        if(value != null) try {
            value.jdoPostLoad();
        } catch (RuntimeException exception) {
            throw new ServiceException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.event.InstanceCallbackListener#preClear(org.openmdx.base.event.InstanceCallbackEvent)
     */
    public void preClear(
        InstanceCallbackEvent event
    ) throws ServiceException {
        Object key = event.getSource();
        if(key instanceof DataObject_1_0) {
            DataObject_1_0 delegate = (DataObject_1_0) key;
            if(delegate.objIsInaccessible()) {
                ObjectView_1 value = (ObjectView_1) super.mapping.remove(key);
                if(value != null) {
                    value.setInaccessibilityReason(
                        delegate.getInaccessibilityReason()
                    );
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.event.InstanceCallbackListener#preDelete(org.openmdx.base.event.InstanceCallbackEvent)
     */
    public void preDelete(
        InstanceCallbackEvent event
    )throws ServiceException {
        ObjectView_1 value = (ObjectView_1) super.mapping.get(event.getSource());
        if(value != null) try {
            value.jdoPreDelete();
        } catch (RuntimeException exception) {
            throw new ServiceException(exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.event.InstanceCallbackListener#preStore(org.openmdx.base.event.InstanceCallbackEvent)
     */
    public void preStore(
        InstanceCallbackEvent event
    ) throws ServiceException {
        ObjectView_1 value = (ObjectView_1) super.mapping.get(event.getSource());
        if(value != null) try {
            value.jdoPreStore();
        } catch (RuntimeException exception) {
            throw new ServiceException(exception);
        }
    }

}
