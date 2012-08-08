/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ViewManager_1.java,v 1.16 2009/06/09 15:39:58 hburger Exp $
 * Description: View Manager
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 15:39:58 $
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jdo.Extent;
import javax.jdo.FetchGroup;
import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOUserException;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.resource.cci.InteractionSpec;

import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.Structure_1_0;
import org.openmdx.base.accessor.spi.AbstractTransaction_1;
import org.openmdx.base.accessor.spi.ListStructure_1;
import org.openmdx.base.accessor.spi.MarshallingStructure_1;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingMap;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSparseArray;
import org.openmdx.base.collection.Unmarshalling;
import org.openmdx.base.event.InstanceCallbackEvent;
import org.openmdx.base.event.InstanceCallbackListener;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.CachingMarshaller;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.mof.spi.ModelUtils;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.compatibility.state1.spi.StateCapables;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.SortedMaps;

/**
 * Manager_1 implementation.
 * <p>
 * The manager returns the same object for a given object id as long as it is not 
 * garbage collected.
 */
public class ViewManager_1 
    extends CachingMarshaller
    implements InstanceCallbackListener, Serializable, DataObjectManager_1_0  {

    /**
     * Constructs a Manager.
     *
     * @param   connection
     *          the interaction object to be used by this manager
     */
    public ViewManager_1(
        DataObjectManager_1_0 connection
    ){
        this(null, connection);
    }

    /**
     * Constructs a Manager.
     *
     * @param   connection
     *          the interaction object to be used by this manager
     * @param factory 
     */
    public ViewManager_1(
        DataObjectManager_1_0 connection, 
        PersistenceManagerFactory factory
    ){
        this(factory, connection);
    }

    /**
     * Constructs a Manager.
     * @param factory 
     * @param   connection
     *          the interaction object to be used by this manager
     */
    public ViewManager_1(
        PersistenceManagerFactory factory, 
        DataObjectManager_1_0 connection
    ){
        this.connection = connection;
        this.objectFactories = new ConcurrentHashMap<InteractionSpec, ViewManager_1>();
        this.objectFactories.put(InteractionSpecs.NULL, this);
        this.interactionSpec = null;
        this.factory = factory;
        this.transaction = new AbstractTransaction_1(){

            @Override
            protected Transaction getDelegate() {
                return ViewManager_1.this.connection.currentTransaction();
            }

            public PersistenceManager getPersistenceManager() {
                return ViewManager_1.this;
            }
        };
    }

    /**
     * Constructor 
     *
     * @param connection
     * @param objectFactories
     * @param interactionSpec
     */
    private ViewManager_1(
        DataObjectManager_1_0 connection,
        ConcurrentMap<InteractionSpec, ViewManager_1> objectFactories,
        InteractionSpec interactionSpec,
        Transaction transaction
    ){
        this.connection = connection;
        this.objectFactories = objectFactories;
        this.interactionSpec = interactionSpec;
        this.transaction = transaction;
    }
        
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 4121130329538180151L;

    /**
     *  
     */
    DataObjectManager_1_0 connection;

    /**
     *  
     */
    private final InteractionSpec interactionSpec;

    /**
     * 
     */
    private ConcurrentMap<InteractionSpec, ViewManager_1> objectFactories;
    
    /**
     * The model repository
     */
    private Model_1_0 model;
    
    /**
     * The transaction view
     */
    private final Transaction transaction;
    
    /**
     * 
     */
    private PersistenceManagerFactory factory;
    
    /**
     * Return connection assigned to this manager.
     */
    public DataObjectManager_1_0 getConnection(
    ) {
    	try {
	        validateState();
        } catch (ServiceException exception) {
        	throw BasicException.initHolder(
	        	new JDOFatalUserException(
	        		"Unable to retrieve a closed persistence manager's connection",
	        		BasicException.newEmbeddedExceptionStack(exception)
	        	)
	        );
        }
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

    protected Model_1_0 getModel(){
        if(this.model == null) {
            this.model = Model_1Factory.getModel();
        }
        return this.model;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_4#getObjectFactory(javax.resource.cci.InteractionSpec)
     */
    public ViewManager_1 getPersistenceManager(
        InteractionSpec interactionSpec
    ) {
        InteractionSpec key = interactionSpec == null ? InteractionSpecs.NULL : interactionSpec;
        ViewManager_1 objectFactory = this.objectFactories.get(key);
        if(objectFactory == null) {
            ViewManager_1 concurrent = this.objectFactories.putIfAbsent(
                key,
                objectFactory = new ViewManager_1(
                    this.connection,
                    this.objectFactories,
                    interactionSpec,
                    this.transaction
                )
            );
            if(concurrent != null) {
                objectFactory = concurrent;
            }
        }
        return objectFactory;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.PersistenceManager_1_0#getFeatureReplacingObjectById(java.lang.Object, java.lang.String)
     */
    public Object getFeatureReplacingObjectById(
        Object objectId,
        String featureName
    ) {
        DataObject_1_0 source = (DataObject_1_0) getObjectById(objectId);
        try {
            if(source == null) { 
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "Object not found",
                    new BasicException.Parameter("xri", objectId),
                    new BasicException.Parameter("feature", featureName)
                ); 
            }
            Model_1_0 model = getModel();
            ModelElement_1_0 featureDef;
            if (featureName.indexOf(':') >= 0) {
                //
                // Fully qualified feature name. Lookup in model
                //
                featureDef = model.getElement(featureName);
            } else {
                //
                // Get all features of class and find feature with featureName
                //
                ModelElement_1_0 classifierDef = model.getElement(source.objGetClass());
                featureDef = classifierDef== null ? null  : model.getFeatureDef(
                    classifierDef,
                    featureName,
                    false
                );
            }
            if(featureDef == null) { 
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_MEMBER_NAME,
                    "feature not found",
                    new BasicException.Parameter("xri", objectId),
                    new BasicException.Parameter("class", source.objGetClass()),
                    new BasicException.Parameter("feature", featureName)
                ); 
            }
            String cciFeatureName = (String) featureDef.objGetValue("name"); 
            if (!model.isReferenceType(featureDef)) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_MEMBER_NAME,
                "model element not of type " + ModelAttributes.REFERENCE,
                new BasicException.Parameter("model element", featureDef)
            ); 
            if(model.referenceIsStoredAsAttribute(featureDef)) {
                //
                // Reference Stored As Attribute
                //
                String multiplicity = ModelUtils.getMultiplicity(featureDef);
                if(
                    Multiplicities.SINGLE_VALUE.equals(multiplicity) || 
                    Multiplicities.OPTIONAL_VALUE.equals(multiplicity) 
                ){
                    return PersistenceHelper.getCurrentObjectId(
                        source.objGetValue(cciFeatureName)
                    ); 
                } else if (Multiplicities.LIST.equals(multiplicity)) { 
                    return new MarshallingList<Object>(
                        ObjectIdMarshaller.INSTANCE,
                        source.objGetList(cciFeatureName),
                        Unmarshalling.RELUCTANT
                    );
                } else if (Multiplicities.SET.equals(multiplicity)) { 
                    return new MarshallingSet<Object>(
                        ObjectIdMarshaller.INSTANCE,
                        source.objGetSet(cciFeatureName),
                        Unmarshalling.RELUCTANT
                    );
                } else if (Multiplicities.SPARSEARRAY.equals(multiplicity)) { 
                    return new MarshallingSparseArray(
                        ObjectIdMarshaller.INSTANCE,
                        SortedMaps.asSparseArray(source.objGetSparseArray(cciFeatureName)),
                        Unmarshalling.RELUCTANT
                    );
                } else throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "Unsupported multiplicity",
                    new BasicException.Parameter("xri", PersistenceHelper.getCurrentObjectId(this)),
                    new BasicException.Parameter("feature",cciFeatureName),
                    new BasicException.Parameter("multiplicity",multiplicity)
                 );
            } else {
                //
                // Aggregation
                //
                ModelElement_1_0 exposedEnd = model.getElement(
                    featureDef.objGetValue("exposedEnd")
                );
                // navigation to parent object is performed locally by removing
                // the last to object path components
                if(
                    AggregationKind.SHARED.equals(exposedEnd.objGetValue("aggregation")) || 
                    AggregationKind.COMPOSITE.equals(exposedEnd.objGetValue("aggregation"))
                ) {
                    return source.jdoGetObjectId().getPrefix(
                        source.jdoGetObjectId().size() - 2
                    );
                }  else {
                    return new MarshallingMap<String, DataObject_1_0>(
                        ObjectIdMarshaller.INSTANCE,
                        source.objGetContainer(cciFeatureName),
                        Unmarshalling.RELUCTANT
                    );
                }
            }
        }  catch (ServiceException exception) {
            throw BasicException.initHolder(
                new JDOUserException(
                    "Could not retrieve a feature while replacing objects by their id",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        exception.getExceptionDomain(),
                        exception.getExceptionCode(),
                        new BasicException.Parameter("xri", objectId),
                        new BasicException.Parameter("feature", featureName)
                    ),
                    source
               )
           );
        }  catch (RuntimeServiceException exception) {
            throw BasicException.initHolder(
                new JDOUserException(
                    "Could not retrieve a feature while replacing objects by their id",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        exception.getExceptionDomain(),
                        exception.getExceptionCode(),
                        new BasicException.Parameter("xri", objectId),
                        new BasicException.Parameter("feature", featureName)
                    ),
                    source
               )
           );
        }
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
        if(accessPath == null) {
            return null;
        } else try {
            Path objectId = accessPath instanceof Path ? (Path) accessPath : new Path(accessPath.toString()); 
            return objectId.size() % 2 == 1 ?
                getObjectById(objectId) :
                getObjectById(objectId.getParent()).objGetContainer(objectId.getBase());
        } catch(Exception e) {
            throw new JDOUserException(
                "Unable to get object",
                e
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
        return this.transaction;
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
                pc
            );
        }
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.lang.Object[])
     */
    public void deletePersistentAll(Object... pcs) {
    	for(Object pc : pcs){
    		deletePersistent(pc);
    	}
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void deletePersistentAll(Collection pcs) {
    	for(Object pc : pcs){
    		deletePersistent(pc);
    	}
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
    	getConnection().evict(toDataObject(pc));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.lang.Object[])
     */
    public void evictAll(Object... pcs) {
    	getConnection().evictAll(toDataObjects(pcs));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void evictAll(Collection pcs) {
    	getConnection().evictAll(toDataObjects(pcs));
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
        return getConnection().getFetchGroup(type, name);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects()
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects() {
        return new MarshallingSet(
            this,
            getConnection().getManagedObjects()
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet)
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects(EnumSet<ObjectState> states) {
        return new MarshallingSet(
            this,
            getConnection().getManagedObjects(states)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.lang.Class[])
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects(Class... classes) {
        return new MarshallingSet(
            this,
            getConnection().getManagedObjects(classes)
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet, java.lang.Class[])
     */
    @SuppressWarnings("unchecked")
    public Set getManagedObjects(EnumSet<ObjectState> states, Class... classes) {
        return new MarshallingSet(
            this,
            getConnection().getManagedObjects(states, classes)
        );
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
    	for(Object pc : pcs){
    		makeTransient(pc, useFetchPlan);
    	}
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
        return this.connection.getDetachAllOnCommit();
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
        return connection.getIgnoreCache();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object, boolean)
     */
    public Object getObjectById(Object oid, boolean validate) {
        return getObjectById(oid);
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
        if(this.interactionSpec == null) { 
            if(this.factory == null) {
                this.factory = new ViewManagerFactory_1(this.connection.getPersistenceManagerFactory());
            }
            return this.factory;
        } else {
            return getPersistenceManager(null).getPersistenceManagerFactory();
        }
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
        throw null;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getUserObject(java.lang.Object)
     */
    public Object getUserObject(Object key) {
        return null;
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
    	getConnection().makeTransactional(toDataObject(pc));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.lang.Object[])
     */
    public void makeTransactionalAll(Object... pcs) {
    	getConnection().makeTransactionalAll(toDataObjects(pcs));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void makeTransactionalAll(Collection pcs) {
    	getConnection().makeTransactionalAll(toDataObjects(pcs));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object)
     */
    public void makeTransient(Object pc) {
    	getConnection().makeTransient(toDataObject(pc));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object, boolean)
     */
    public void makeTransient(Object pc, boolean useFetchPlan) {
    	getConnection().makeTransient(toDataObject(pc),useFetchPlan);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[])
     */
    public void makeTransientAll(Object... pcs) {
    	getConnection().makeTransientAll(toDataObjects(pcs));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void makeTransientAll(Collection pcs) {
    	getConnection().makeTransientAll(toDataObjects(pcs));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[], boolean)
     */
    public void makeTransientAll(Object[] pcs, boolean useFetchPlan) {
    	getConnection().makeTransientAll(useFetchPlan, toDataObjects(pcs));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public void makeTransientAll(Collection pcs, boolean useFetchPlan) {
    	getConnection().makeTransientAll(toDataObjects(pcs), useFetchPlan);
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
                pc
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
    	for(Object pc : pcs) {
    		refresh(pc);
    	}
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void refreshAll(Collection pcs) {
    	for(Object pc : pcs) {
    		refresh(pc);
    	}
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
        retrieve(pc, false);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object, boolean)
     */
    public void retrieve(Object pc, boolean useFetchPlan) {
        getConnection().retrieve(toDataObject(pc), useFetchPlan);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void retrieveAll(Collection pcs) {
        retrieveAll(pcs, false);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[])
     */
    public void retrieveAll(Object... pcs) {
    	retrieveAll(false,pcs);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public void retrieveAll(Collection pcs, boolean useFetchPlan) {
         getConnection().retrieveAll(toDataObjects(pcs), useFetchPlan);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[], boolean)
     */
    public void retrieveAll(Object[] pcs, boolean useFetchPlan) {
    	getConnection().retrieveAll(useFetchPlan, toDataObjects(pcs));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setDetachAllOnCommit(boolean)
     */
    public void setDetachAllOnCommit(boolean flag) {
        this.connection.setDetachAllOnCommit(flag);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setIgnoreCache(boolean)
     */
    public void setIgnoreCache(boolean flag) {
        this.connection.setIgnoreCache(flag);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setMultithreaded(boolean)
     */
    public void setMultithreaded(boolean flag) {
        this.connection.setMultithreaded(flag);
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
        return source instanceof ObjectView_1 ? ((ObjectView_1)source).objGetDelegate() : source;
    }
    
    /**
     * Retrieve an ObjectView_1's DataObject_1_0 delegate
     * 
     * @param pc the ObjectView_1 instance
     * 
     * @return its DataObject_1_0 delegate
     */
    private static Object toDataObject(
    	Object pc
    ){
        if(pc instanceof ObjectView_1_0) try {
    		return ((ObjectView_1_0)pc).objGetDelegate();
    	} catch (ServiceException exception) {
    		throw BasicException.initHolder(
	        	new JDOFatalUserException(
	        		"Unable to retrieve the persistence capable's DataObject_1_0 delegate",
	        		BasicException.newEmbeddedExceptionStack(exception)
	        	)
        	);    		
    	} else {
    		throw BasicException.initHolder(
	        	new JDOFatalUserException(
	        		"Unable to retrieve the persistence capable's DataObject_1_0 delegate",
	        		BasicException.newEmbeddedExceptionStack(
	            		BasicException.Code.DEFAULT_DOMAIN,
	            		BasicException.Code.BAD_PARAMETER,
	            		new BasicException.Parameter("class", pc == null ? null : pc.getClass().getName()),
                        new BasicException.Parameter("acceptable", ObjectView_1_0.class.getName())
	        		)
	        	)
        	);    		
    	}
    }
    
    private static Collection<Object> toDataObjects(Collection<?> pcs) {
    	Collection<Object> dos = new ArrayList<Object>();
    	for(Object pc : pcs) {
    		dos.add(toDataObject(pc));
    	}
    	return dos;
    }

    private static Object[] toDataObjects(Object[] pcs) {
    	Object[] dos = new Object[pcs.length];
    	int i = 0;
    	for(Object pc : pcs) {
    		dos[i++] = toDataObject(pc);
    	}
    	return dos;
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
        // Do nothing
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


    // -----------------------------------------------------------------------
    // Class ObjectIdMarshaller
    // -----------------------------------------------------------------------

    /**
     * Object Id Marshaller
     */
    static class ObjectIdMarshaller implements Marshaller {

        /**
         * Constructor 
         */
        private ObjectIdMarshaller(
        ){
            // Avoid external instantiation
        }
            
        /**
         * The singleton
         */
        static final Marshaller INSTANCE = new ObjectIdMarshaller();

        /* (non-Javadoc)
         * @see org.openmdx.base.marshalling.Marshaller#marshal(java.lang.Object)
         */
        public Object marshal(
            Object source
        ) throws ServiceException {
            return PersistenceHelper.getCurrentObjectId(source);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.marshalling.Marshaller#unmarshal(java.lang.Object)
         */
        public Object unmarshal(
            Object source
        ) throws ServiceException {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Oobject id collections are unmodifiable"
            );
        }
        
    }

    
}
