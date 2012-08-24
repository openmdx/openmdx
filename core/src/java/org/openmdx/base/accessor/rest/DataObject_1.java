/*
 * ====================================================================
 * Description: Object_1 class
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2012, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.rest;

import static org.openmdx.base.mof.cci.PrimitiveTypes.DATE;
import static org.openmdx.base.mof.cci.PrimitiveTypes.DATETIME;
import static org.openmdx.base.mof.cci.PrimitiveTypes.DURATION;
import static org.openmdx.base.mof.cci.PrimitiveTypes.INTEGER;
import static org.openmdx.base.mof.cci.PrimitiveTypes.LONG;
import static org.openmdx.base.mof.cci.PrimitiveTypes.SHORT;

import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalInternalException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;
import javax.jdo.listener.AttachLifecycleListener;
import javax.jdo.listener.ClearLifecycleListener;
import javax.jdo.listener.CreateLifecycleListener;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.DetachLifecycleListener;
import javax.jdo.listener.DirtyLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.jdo.listener.LoadLifecycleListener;
import javax.jdo.listener.StoreLifecycleListener;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.application.dataprovider.layer.persistence.jdbc.LockAssertions;
import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.rest.spi.ManagedConnectionCache_2_0;
import org.openmdx.base.accessor.rest.spi.ManagedConnectionCache_2_0.Mode;
import org.openmdx.base.accessor.spi.AbstractDataObject_1;
import org.openmdx.base.accessor.spi.DateMarshaller;
import org.openmdx.base.accessor.spi.DateTimeMarshaller;
import org.openmdx.base.accessor.spi.DurationMarshaller;
import org.openmdx.base.accessor.spi.ExceptionHelper;
import org.openmdx.base.accessor.spi.IntegerMarshaller;
import org.openmdx.base.accessor.spi.LongMarshaller;
import org.openmdx.base.accessor.spi.ShortMarshaller;
import org.openmdx.base.aop0.PlugIn_1_0;
import org.openmdx.base.collection.Maps;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.collection.PopulationMap;
import org.openmdx.base.collection.Sets;
import org.openmdx.base.collection.TreeSparseArray;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.cci.Persistency;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.PathComponent;
import org.openmdx.base.persistence.spi.PersistenceCapableCollection;
import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.IsInCondition;
import org.openmdx.base.query.IsInstanceOfCondition;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.MethodInvocationSpec;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.base.text.conversion.JavaBeans;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.CharacterLargeObjects;
import org.w3c.cci2.LargeObject;
import org.w3c.cci2.SortedMaps;
import org.w3c.cci2.SparseArray;

/**
 * DataObject_1_0 implementation
 */
public class DataObject_1 
    implements DataObject_1_0, Serializable
{

    /**
     * Constructor for a detached-clean instance
     */
    public DataObject_1(
        Path identity, 
        Object version
    ){
        this.identity = identity;
        this.transientObjectId = null;
        this.detached = true;
        this.untouchable = false;
        this.digest = version;
    }
    
    /**
     * Constructor 
     * 
     * @param manager
     * @param identity 
     * @param transientObjectId 
     * @param objectClass
     * @param frozen 
     * 
     * @throws ServiceException
     */
    DataObject_1(
        DataObjectManager_1 manager,
        Path identity, 
        UUID transientObjectId, 
        String objectClass, 
        boolean frozen
    ) throws ServiceException{
        this.dataObjectManager = manager;
		this.identity = identity;
		this.transientObjectId = transientObjectId == null ? UUIDs.newUUID() : transientObjectId;
		this.transientValues = identity == null ? new HashMap<String,Object>() : null;
		this.detached = false;
		this.untouchable = frozen;
		manager.putUnlessPresent(
		    this.transientObjectId,  
		    this
		);
        if(objectClass == null && identity == null) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "The identity is required for persisten objects, the object class for transient ones"
        );
        this.transactionalValuesRecordName = objectClass;
        if(identity != null) {
            manager.putUnlessPresent(identity, this);
        }
    }

	/**
     * Constructor
     * 
     * @param that
	 * @param beforeImage <code>true</code> if the before-image shall be cloned
	 * @param identity 
     * @param exclude the features not to be cloned
     * 
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    private DataObject_1(
        DataObject_1 that, 
        boolean beforeImage, 
        String... exclude
    ) throws ServiceException{
        this(
            that.dataObjectManager,
            null, // identity
            null, // transientObjectId 
            DataObject_1.getRecordName(that, false), 
            beforeImage
        );
        this.digest = null;
        Set<String> notToBeCloned = Sets.asSet(exclude);
        if(that.jdoIsPersistent() && !that.jdoIsNew()) {
		    that.objRetrieve(false, null, null, beforeImage);
		}
		this.persistentValues = newRecord(this.transactionalValuesRecordName);
        if(!that.objIsHollow()) {
            //
            // Persistent Values
            //
        	for(Map.Entry<?,?> e: ((Map<?,?>)that.persistentValues).entrySet()) {
                if(!notToBeCloned.contains(e.getKey())){
	    			this.persistentValues.put(e.getKey(), e.getValue());
                }
        	}
        }
        TransactionalState_1 thisState = this.getState(false);
        Set<String> thisDirty = thisState.dirtyFeatures(false);
        if(beforeImage){
            //
            // Persistent Values
            //
			thisDirty.addAll(that.persistentValues.keySet());
        } else {
            //
            // Transactional Values
            //
            TransactionalState_1 thatState = that.getState(true);
            if(thatState != null) {
                for(Map.Entry<String,Object> e : thatState.values(true).entrySet()){
                    String feature = e.getKey();
                    if(!notToBeCloned.contains(feature)){
	                    Object candidate = e.getValue();
	                    if(candidate instanceof Set) {
	                        Set<Object> target = this.objGetSet(feature);
	                        target.clear();
	                        target.addAll((Set<?>)candidate);
	                    } else if (candidate instanceof List) {
	                        List<Object> target = this.objGetList(feature);
	                        target.clear();
	                        target.addAll((List<?>)candidate);
	                    } else if (candidate instanceof SortedMap) {
	                        SortedMap<Integer,Object> target = this.objGetSparseArray(feature);
	                        target.clear();
	                        target.putAll((SortedMap<Integer,Object>)candidate);
	                    } else {
	                        this.objSetValue(feature, candidate);
	                    }
                    }
                }
            }
            //
            // Transient Values
            //
            if (
                that.transientValues != null &&
                this.transientValues != null
             ) {
                for(Map.Entry<String,Object> e : that.transientValues.entrySet()){
                    String feature = e.getKey();
                    if(!notToBeCloned.contains(feature)) {
	                    Object candidate = e.getValue();
	                    if(candidate instanceof Set) {
	                        this.transientValues.put(feature, new HashSet<Object>((Set<?>)candidate));
	                        this.objGetSet(feature);
	                    } else if (candidate instanceof List) {
	                        this.transientValues.put(feature, new ArrayList<Object>((List<?>)candidate));
	                        this.objGetList(feature);
	                    } else if (candidate instanceof SortedMap) {
	                        this.transientValues.put(feature, new TreeMap<Integer,Object>((SortedMap<Integer,Object>)candidate));
	                        this.objGetSparseArray(feature);
	                    } else {
	                        this.transientValues.put(feature, candidate);
	                    }
                    }
                }
            } 
            //
            // Dirty Features
            //
            thisDirty.addAll(thisState.values(false).keySet());
            if(that.persistentValues != null) {
                thisDirty.addAll(that.persistentValues.keySet());
            }
            if(this.transientValues != null) {
                thisDirty.addAll(this.transientValues.keySet());
            }
            //
            // Streams
            //
            for(Map.Entry<String, ModelElement_1_0> attribute : getAttributes().entrySet()) {
                ModelElement_1_0 featureDef = attribute.getValue();
                if(isStreamed(featureDef) && isPersistent(featureDef)){
                    String featureName = attribute.getKey();
                    this.objSetValue(
                        featureName,
                        that.objGetValue(featureName)
                    );
                }
            }
        }
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 6246865175754052117L;

    /**
     * Map instance life cycle event types to the corresponding listener
     */
    @SuppressWarnings("unchecked")
    final static Class<? extends InstanceLifecycleListener>[] lifecycleListenerClass = new Class[8];
    
    /**
     * The object's identity
     *
     * @serial
     */
    protected Path identity;

    /**
     * @serial
     */
    private DataObjectManager_1 dataObjectManager;

    /**
     * @serial
     */
    private final UUID transientObjectId;
    
    /**
     * @serial
     */
    private String transactionalValuesRecordName;

    /**
     * Tells whether this object shall become transient on rollback 
     */
    private boolean transientOnRollback = false;
    
    /**
     * 
     */
    private String qualifier = null;
    
    /**
     * @serial 
     * 
     * @see #jdoIsDeleted()
     */
    private boolean deleted = false;

    /**
     * @serial
     * 
     * @see #jdoIsNew()
     */
    private boolean created = false;

    /**
     * @serial
     * 
     * @see #jdoIsDetached()
     */
    private final boolean detached;
    
    /**
     * The read-only flag is true in case of before images
     */
    private final boolean untouchable;

    /**
     *
     */
    protected transient MappedRecord persistentValues = null;

    /**
    *
    */
    protected Map<String,Object> transientValues;
    
    /**
     * @serial the write lock
     */
    protected Object digest = null;

    /**
     * @serial the read lock
     */
    protected Object lock = null;
    
    /**
     * Such an object cant't leave its hollow state
     */
    private transient ServiceException inaccessabilityReason = null;

    /**
     * Cache the before image
     */
    private transient DataObject_1 beforeImage = null;
    
    /**
     * The flushable value registry
     */
    transient ConcurrentMap<String,Flushable> flushableValues = new ConcurrentHashMap<String,Flushable>();

    /**
     * The persistent aspects
     */
    private transient Container_1 container = null;

    /**
     * The persistent aspects
     */
    private transient Container_1_0 aspects = null;
            
    /**
     * 
     */
    static final Map<String, DataObject_1_0> NO_ASPECT = Collections.emptyMap();

    /**
     * 
     */
    private boolean loadLock = false;
    
    /**
     * Map a data types's qualified name to its marshaller
     */
    static final Map<String,Marshaller> dataTypeMarshaller = new HashMap<String,Marshaller>();
    
    /**
     * Valdidate the object's state and retrieve its class
     * 
     * @param that the object to be inspected
     * @param lenient an exception is thrown for hollow objects unless <code>lenient</code> is <code>true</code> 
     * 
     * @return the object's class
     * 
     * @throws ServiceException
     */
    static String getRecordName(
        DataObject_1 that, 
        boolean lenient
    ) throws ServiceException {
        if(that.transactionalValuesRecordName == null && that.objIsHollow()) {
        	if(lenient){
        		return null;
        	} else {
	            throw new ServiceException(
	                BasicException.Code.DEFAULT_DOMAIN,
	                BasicException.Code.ILLEGAL_STATE,
	                "The source object is hollow"
	            );
        	}
        } else {
            return that.transactionalValuesRecordName == null ? that.persistentValues.getRecordName() : that.transactionalValuesRecordName;
        }
    }
    
    /**
     * Test whether two objects are either both <code>null</code> or equal.
     * 
     * @param left
     * @param right
     * 
     * @return <code>true</code> if either both objects are <code>null</code> or equal.
     */
    static boolean equal(
        Object left,
        Object right
    ){
        return left == right || (
        	left != null && right != null && left.equals(right)
        );
    }
    
    /**
     * Tells whether the object represents an aspect
     * 
     * @return <code>true</code> if the object represents an aspect
     * 
     * @throws ServiceException
     */
    public boolean isAspect(
    ) throws ServiceException {
        return getModel().isInstanceof(this, "org:openmdx:base:Aspect");
    }
    
    /**
     * Tells whether the object is an instance of Modifiable
     * 
     * @return <code>true</code> if the object is an instance of Modifiable
     * 
     * @throws ServiceException
     */
    public boolean isModifiable(
    ) throws ServiceException {
        return getModel().isInstanceof(this, "org:openmdx:base:Modifiable");
    }
    
    /**
     * Tells whether the object is an instance of Removable
     * 
     * @return <code>true</code> if the object is an instance of Removable
     * 
     * @throws ServiceException
     */
    public boolean isRemovable(
    ) throws ServiceException {
        return getModel().isInstanceof(this, "org:openmdx:base:Removable");
    }

    /**
     * @return the model repository
     */
    public Model_1_0 getModel() {
        return this.dataObjectManager.getModel();
    }
    
    /**
     * Tells whether the given feature name refers to an aspect's core reference
     * 
     * @param feature the feature name
     * 
     * @return <code>true</code> if the given feature name refers to an aspect's core reference
     * 
     * @throws ServiceException
     */
    private boolean isAspectHasCore(
        String feature
    ) throws ServiceException {
        return "core".equals(feature) && isAspect();
    }
    
    /**
     * Retrieve the object's transactional state
     * 
     * @param optional
     * 
     * @return the object's transactional state
     */
    final TransactionalState_1 getState(
        boolean optional
    ){
        if(
            optional && 
            (this.dataObjectManager == null || this.dataObjectManager.isClosed())
        ) {
            return null;
        } else {
            return this.getUnitOfWork().getState(this,optional);
        }        
    }

    @SuppressWarnings("unchecked")
    void setExistence(
        Path identity,
        boolean existence
    ) throws ServiceException{
        UnitOfWork_1 unitOfWork = this.getUnitOfWork();
        if(unitOfWork.isActive()) {
            if(!identity.startsWith(this.identity) || identity.size() - this.identity.size() != 2) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "The given path does not refer to a potential child",
                    ExceptionHelper.newObjectIdParameter("object", this),
                    new BasicException.Parameter("child", identity),
                    new BasicException.Parameter("existence", existence)
                );
            }
            Set<String> notFound = null;
            TransactionalState_1 state = unitOfWork.getState(DataObject_1.this, existence);
            if(state != null) {
                String feature = identity.get(identity.size() - 2);
                Map<String,Object> values = state.values(existence);
                notFound = (Set<String>) values.get(feature);
                if(notFound == null && !existence) {
                    values.put(
                        feature,
                        notFound = new HashSet<String>()
                    );
                }
            }
            if(notFound != null) {
                String qualifier = identity.get(identity.size() - 1);
                if(existence) {
                    notFound.remove(qualifier);
                } else {
                    notFound.add(qualifier);
                }
            }
        }
    }

    /**
     * Returns the object's model class.
     *
     * @return  the object's model class
     *
     * @exception   ServiceException
     *              if the information is unavailable
     */
    public String objGetClass(
    ) throws ServiceException {
        if(this.transactionalValuesRecordName == null) {
            try {
                this.objRetrieve(
                    false, // reload
                    this.dataObjectManager.getFetchPlan(), 
                    null, // features
                    false // beforeImage
                );
                this.transactionalValuesRecordName = this.persistentValues.getRecordName();
            } catch (NullPointerException exception){
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_AVAILABLE,
                    "Object class can't be determined"
                );
            }
        }
        return this.transactionalValuesRecordName;
    }

    /**
     * Returns the object's identity.
     *
     * @return    the object's identity;
     *            or null for transient objects
     */
    public Path jdoGetObjectId(
    ){
        return this.identity;
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetTransactionalObjectId()
     */
    public UUID jdoGetTransactionalObjectId(
    ) {
        return this.transientObjectId;
    }

    /**
     * Returns a new set containing the names of the features in the default
     * fetch group.
     * <p>
     * The returned set is a copy of the original set, i.e. interceptors are
     * free to modify it before passing it on.
     *
     * @return  the names of the features in the default fetch group
     * @throws ServiceException 
     */
    @SuppressWarnings("unchecked")
    public Set<String> objDefaultFetchGroup(
    ) throws ServiceException{
        Set<String> result = new HashSet<String>();
        TransactionalState_1 state = this.getState(true);
        if(state != null) {
            result.addAll(state.values(true).keySet());
        } else if (this.transientValues != null) {
            result.addAll(this.transientValues.keySet());
        } 
        if(!objIsHollow()) {
            result.addAll(this.persistentValues.keySet());
        }            
        return result;
    }

    /**
     * Set the default fetch group
     * 
     * @param objectHolder
     *
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    synchronized DataObject_1 postLoad(
        MappedRecord objectHolder
    ) throws ServiceException {
        if(this.loadLock) {
        	return null;
        } else try {
        	this.loadLock = true;
            Object_2Facade facade = Facades.asObject(objectHolder);
            if(this.identity == null || this.identity.equals(facade.getPath())) {
                //
                // Composite & Transient
                //
                if(this.persistentValues == null) {
                    this.persistentValues = facade.getValue();
                    this.digest = facade.getVersion();
                } else {
                    MappedRecord source = facade.getValue();
                    MappedRecord persistentValues = newRecord(source.getRecordName());
                    persistentValues.putAll(source);
                    persistentValues.putAll(this.persistentValues);
                    this.persistentValues = persistentValues;
                }
                this.dataObjectManager.fireInstanceCallback(this, InstanceLifecycleEvent.LOAD, false);
                return this;
            } else {
                //
                // Shared
                //
                DataObject_1 composite = this.dataObjectManager.getObjectById(facade.getPath()); 
                setInaccessibilityReason(
                    new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE,
                        "Shared object proxy had to be replaced by composite object proxy",
                        ExceptionHelper.newObjectIdParameter("shared", this),
                        new BasicException.Parameter("composite", facade.getPath())
                        
                    )
                );
                return composite.postLoad(objectHolder);
            }
        } finally {
        	this.loadLock = false;
        }
    }

    /**
     * Make the object inaccessible
     * 
     * @param makeNonTransactional 
     */
    void invalidate(
        boolean makeNonTransactional
    ){
        if(makeNonTransactional) try {
            this.getUnitOfWork().remove(this);
        } catch (Exception ignored) {
            // Just ignore it
        }
        this.clear();
        this.digest = null;
        this.persistentValues = null;
        this.created = false;
        this.identity = null;
    }

    /**
     * Ask the persistence framework for the object's content
     * 
     * @param fetchPlan 
     * @param features 
     * @param refresh tells whether the (remote) object shall be 
     * refreshed before answering the query
     */
    @SuppressWarnings("unchecked")
    DataObject_1 unconditionalLoad(
        FetchPlan fetchPlan, 
        Set<String> features, 
        boolean refresh
    ) throws ServiceException {
        try {
            Query_2Facade query = Facades.newQuery(this);
            if(fetchPlan != null) {
                query.setGroups(fetchPlan.getGroups());
            }
            if(features != null) {
                query.setFeatures(features);
            }
            query.setRefresh(refresh);
            IndexedRecord reply = (IndexedRecord) this.dataObjectManager.getInteraction().execute(
                this.dataObjectManager.getInteractionSpecs().GET,
                query.getDelegate()
            );
            DataObject_1 composite = postLoad((MappedRecord) reply.get(0));
            this.inaccessabilityReason = null;
            return composite;
        } catch (ResourceException exception) {
            throw this.setInaccessibilityReason(new ServiceException(exception));
        } catch (ServiceException exception){      
            throw setInaccessibilityReason(exception);
        }
    }

    /**
     * Set the inaccessibility reason
     * 
     * @param inaccessabilityReason
     * 
     * @return the inaccessibility reason
     */
    public ServiceException setInaccessibilityReason(
        ServiceException inaccessabilityReason
    ){
        this.inaccessabilityReason = inaccessabilityReason;
        if(this.dataObjectManager == null) {
            this.invalidate(
                false // makeNonTransactional
            );
        } else if(inaccessabilityReason.getExceptionCode() != BasicException.Code.NOT_FOUND) {
            this.invalidate(
                true // makeNonTransactional
            );
        } else try {
            this.dataObjectManager.invalidate(
                this.identity, 
                true // makeNonTransactional
            );
        } catch (ServiceException exception) {
            exception.log();
        }
        return inaccessabilityReason;
    }

    /**
     * Evict the data object
     */
    public void evictUnconditionally(
    ){
        clear();
        this.digest = null;
        this.persistentValues = null;
    }
    
    /**
     * Refresh the data object 
     * 
     * @throws ServiceException
     */
    void refreshUnconditionally(
    ) throws ServiceException {
        this.evictUnconditionally();
        Container_1 container = this.getContainer(true);
        if(container != null) {
            container.openmdxjdoEvict(false, true);
        }
        this.unconditionalLoad(
    		this.dataObjectManager.getFetchPlan(), 
    		null, // features
    		true // refresh
        );
        evictReadLock();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#getContainer(boolean)
     */
//  @Override
    public Container_1 getContainer(
        boolean lazily
    ){
        if(this.container == null && this.jdoIsPersistent()) {
            Path identity = this.jdoGetObjectId();
            int size = identity.size() - 2;
            if(size > 0) {
                Path parentId = identity.getPrefix(size);
                if(!lazily || this.dataObjectManager.containsKey(parentId)) {
                    DataObject_1_0 parent = this.dataObjectManager.getObjectById(parentId, false); 
                    if(!parent.objIsInaccessible()) try {
                        this.container = (Container_1) parent.objGetContainer(
                            identity.get(size)
                        );
                    } catch (ServiceException exception) {
                        return null; // As if the parent were inaccessible
                    }
                }
            }
        }
        return this.container;
    }
    
    /**
     * Let a Container_1 register itself to speed-up "lazy" retrieval
     * 
     * @param container
     */
    void setContainer(
        Container_1 container
    ){
        this.container = container;
    }
    
    /**
     * Retrieve this object's persistent aspects
     * 
     * @return the persistent aspects
     * 
     * @throws ServiceException 
     */
    Container_1_0 getAspects(
    ) throws ServiceException{
    	if(isAspect()) {
    		DataObject_1 core = (DataObject_1) this.objGetValue("core");
    		if(core != null) {
    			this.aspects = core.getAspects();
    		}
    	} else {
	        Container_1 container = this.getContainer(false);
	        if(container != null && (this.aspects == null || container != this.aspects.container())) {
	            this.aspects = container.subMap(
	                new Filter(
	                    new IsInCondition(
	                        Quantifier.THERE_EXISTS,
	                        "core",
	                        true, // IS_IN,
	                        this.jdoIsPersistent() ? this.jdoGetObjectId() : this.jdoGetTransactionalObjectId()
	                    )
	                )
	            );
	        }
    	}
        return this.aspects;
    }

    boolean isQualified(){
        return this.jdoIsPersistent() || this.qualifier != null;
    }
    
    String getQualifier(
    ){
        return this.jdoIsPersistent() ? this.jdoGetObjectId().getBase() : this.qualifier;
    }

    /**
     * The transient qualifier is used when an object is added without 
     * qualifier to a transient container
     * 
     * @return the object's transient qualifier
     */
    String getPlaceHolder(){
        return ":" + this.transientObjectId;
    }
    
    /**
     * Retrieve the unit of work
     * 
     * @return the layer specific unit of work
     * 
     * @throws JDOUserException in case of an invalid object 
     */
    public final UnitOfWork_1 getUnitOfWork(
    ){
        if (this.dataObjectManager == null) {
            throw BasicException.initHolder(
                new JDOUserException(
                    "Invalid object. Can not get unit of work.",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ILLEGAL_STATE,
                        ExceptionHelper.newObjectIdParameter("id", this)
                    )
                )
            );  
        }
        return this.dataObjectManager.currentTransaction();
    }

    /**
     * Retrieve the unit of work
     * 
     * @param a feature to be marked dirty, or <code>null</code>
     * 
     * @return the unit of work or <code>null</code> for non-transactional access
     * 
     * @throws ServiceException 
     */
    final UnitOfWork_1 getUnitOfWorkIfTransactional(
        String makeDirty
    ) throws ServiceException {
        UnitOfWork_1 unitOfWork = this.getUnitOfWork(); 
        boolean transactional = makeDirty != null && this.jdoIsPersistent();
        if(transactional) {
            this.addTo(unitOfWork);
        } else {
            transactional = unitOfWork.getMembers().contains(this);
        }
        if(transactional) {
            if(makeDirty != null) {
                TransactionalState_1 state = unitOfWork.getState(this,false);
                state.setPrepared(false);
                state.dirtyFeatures(false).add(makeDirty);
            }
            return unitOfWork;
        } else {
            return null;
        }
    }

    /**
     * After this call the object observes unit of work boundaries.
     * <p>
     * This method is idempotent.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if no unit of work is in progress
     * @exception   ServiceException
     *              if the object can't be added to the unit of work for
     *                another reason.
     */
    void objMakeTransactional(
    ) throws ServiceException {
        this.addTo(getUnitOfWork()); 
    }

    /**
     * An object is touched when a non query operation is invoked
     * @throws ServiceException 
     */
    public void touch(
    ) throws ServiceException{
        objMakeTransactional();
        this.getUnitOfWork().getState(this,false).touch();
    }
    
    /**
     * Add to unit of work
     * 
     * @param unitOfWork
     * 
     * @throws ServiceException 
     */
    private final void addTo(
        UnitOfWork_1 unitOfWork
    ) throws ServiceException {
        if(unitOfWork.add(this)) {
            if(this.transientValues == null){
                this.objRetrieve(
                    false, // reload
                    this.dataObjectManager.getFetchPlan(), 
                    null, // features
                    false // beforeImage
                );
            } else {
                unitOfWork.getState(this,false).setValues(this.transientValues);
                this.transientValues = null;
            }
            if(
            	unitOfWork.isReadLockRequired() &&
            	this.jdoIsPersistent() && !this.jdoIsNew() && 
            	isModifiable() && !isRemovable()
            ) {
            	this.lock = LockAssertions.newReadLockAssertion(unitOfWork.getTransactionTime());
            }
        }
    }
    
    /**
     * After this call the object ignores unit of work boundaries.
     * <p>
     * This method is idempotent.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is dirty.
     * @exception   ServiceException
     *              if the object can't be removed from its unit of work for
     *                another reason
     */
    void objMakeNontransactional(
    ) throws ServiceException {
        if(this.jdoIsDirty()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "A dirty object can't be removed from the unit of work"
            );
        }
        getUnitOfWork().remove(this);
    }
    
    /**
     * This method clones this data object
     *
     * @parame exclude the features not to be cloned
     *
     * @return the clone
     */
    public DataObject_1_0 openmdxjdoClone(
    	String... exclude
    ) {
        try {
            return new DataObject_1(
                this, 
                false, // beforeImage
                exclude
            );
        } catch(Exception e) {
            throw new JDOUserException(
                "Unable to clone object",
                e,
                this
            );
        }
    }
    
    /**
     * The move operation moves the object to the scope of the container
     * passed as the first parameter. The object remains valid after move has
     * successfully executed.
     *
     * @param     there
     *            the object's new container.
     * @param     criteria
     *            The criteria is used to move the object to the container or
     *            <code>null</null>, in which case it is up to the
     *            implementation to define the criteria.
     *
     * @exception ServiceException  ILLEGAL_STATE
     *            if the object is persistent.
     * @exception ServiceException BAD_PARAMETER
     *            if <code>there</code> is not an instance of <code>DelegatingContainer</code>.
     * @exception ServiceException
     *            if the move operation fails.
     */
    public void objMove(
        Container_1_0 there,
        String criteria
    ) throws ServiceException {
        //
        // Validate the arguments
        //
        if(there == null){
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The container is null"
            );
        } 
        if (this.jdoIsPersistent()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "Attempt to move a persistent object",
                ExceptionHelper.newObjectIdParameter("id", this),
                new BasicException.Parameter("old", JDOHelper.getTransactionalObjectId(this.getContainer(true))),
                new BasicException.Parameter("new", JDOHelper.getTransactionalObjectId(there.container()))
            );
        }
        this.container = (Container_1) there.container();
        //
        // Set the container
        //
        if(this.container.jdoIsPersistent()){
            String qualifier = criteria;
            for(PlugIn_1_0 plugIn : this.dataObjectManager.getPlugIns()) {
                qualifier = plugIn.getQualifier(this, qualifier);
            }
            if(qualifier == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "No plug-in did provide the object id's last XRI segment",
                    ExceptionHelper.newObjectIdParameter("id", this),
                    new BasicException.Parameter("criteria", criteria)
                );
            }
            Path identity = this.container.jdoGetObjectId().getChild(qualifier);
            boolean flushed = false;
            if(PathComponent.isPlaceHolder(qualifier)) {
                if(isProxy()) {
                    this.objMakeTransactional();
                    TransactionalState_1 state = this.getState(false);
                    state.setLifeCycleEventPending(true);
                    this.identity = identity;
                    this.created = true;
                    this.dataObjectManager.currentTransaction().flush(false);
                    flushed = true;
                    qualifier = identity.getBase();
                } else {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        "The qualifier must be neither null nor a place holder unless the container is a proxy",
                        new BasicException.Parameter("container",container.jdoGetObjectId()),
                        new BasicException.Parameter("qualifier",qualifier),
                        new BasicException.Parameter("proxy",Boolean.FALSE)
                    );
                }
            } else if(this.dataObjectManager.containsKey(identity)) {
                DataObject_1_0 collision = this.dataObjectManager.getObjectById(identity, false);
                if(collision != this) {
                    try {
                        collision.objGetClass();
                    } catch (Exception exception) {
                        // The candidate could be evicted now
                    }
                    if(this.dataObjectManager.containsKey(identity)) {
                        this.getState(false).setLifeCycleEventPending(false);
                        this.container = null;
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.DUPLICATE,
                            "There is already an object with the same qualifier in the container",
                            new BasicException.Parameter("resourceIdentifier",identity.toXRI())
                        );
                    }                    
                }
            }
            DataObject_1 parent = this.dataObjectManager.getObjectById(
                identity.getPrefix(identity.size() - 2),
                false
            ); 
            parent.objMakeTransactional();
            parent.setExistence(identity, true);
            this.makePersistent(
                identity, 
                flushed // already flushed
            );
            for(Flushable flushable : this.flushableValues.values()){
                if(flushable instanceof Container_1){
                    this.makePersistent(
                        (Container_1)flushable,
                        false // aspect
                    );
                    this.makePersistent(
                        (Container_1)flushable,
                        true // aspect
                    );
                } else if (flushable instanceof ManagedAspect) {
                    ManagedAspect managedAspect = (ManagedAspect) flushable;
                    managedAspect.evict();
                    if(this.created){
                        managedAspect.move();
                    }
                }
            }
            if (container.isRetrieved()) {
                String key = getCacheKey();
                if (key == null) {
                   this.container.addToCache(qualifier, this);
                } else if (!key.equals(qualifier)) {
                   this.container.removeFromChache(key);
                   this.container.addToCache(qualifier, this);
                }
            }            
        } else {
            //
            // Add to a transient container
            //
            if(criteria == null) {
                this.container.addToCache(
                    this.getPlaceHolder(),
                    this
                );
            } else {
                this.container.addToCache(
                    this.qualifier = criteria, 
                    this
                );
                for(Flushable flushable : this.flushableValues.values()){
                    if(flushable instanceof ManagedAspect) {
                        ((ManagedAspect) flushable).move();
                    }
                }
            }
        }
    }

    /**
     * Search the object in the cache and return its registration key
     * 
     * @return the object's registration key, or <code>null<code> if it is not cached
     */
    private String getCacheKey(
    ){
        for (Map.Entry<String, DataObject_1_0> e : this.container.getCache().entrySet()) {
           if (e.getValue() == this) {
              return e.getKey();
           }
        }
        return null;
    }
    
    /**
     * Make an object persistent ignoring its container and callbacks
     * 
     * @param identity the object id
     * @param flushed <code>true</code> if the object is alread flushed
     * @throws ServiceException  
     */
    public void makePersistent(
        Path identity, 
        boolean flushed
    ) throws ServiceException {
        if(!flushed){
            TransactionalState_1 state = this.getState(false);
            state.setLifeCycleEventPending(true);
            this.created = true;
            this.identity = identity;
        }
        if(this.transientObjectId == null) {
            this.dataObjectManager.putUnlessPresent(identity, this);
        }  else {
            this.dataObjectManager.move(this.transientObjectId,identity);
        }
        if(!flushed){
            this.objMakeTransactional();
        }
        if(!this.untouchable) {
            this.dataObjectManager.fireInstanceCallback(this, InstanceLifecycleEvent.CREATE, false);
        }
    }
    
    /**
     * Make either the aspects or other descendants persistent 
     * 
     * @param descendants
     * @param aspect
     * 
     * @throws ServiceException
     */
    private void makePersistent(
        Container_1 descendants,
        boolean aspect
    ) throws ServiceException{
        Map<String,DataObject_1_0> children = new LinkedHashMap<String,DataObject_1_0>();
        for(Map.Entry<String,DataObject_1_0> child : descendants.entrySet()) {
            if(!child.getValue().jdoIsPersistent()) {
                PathComponent key = new PathComponent(child.getKey());
                if(aspect == (key.isPlaceHolder() && key.size() == 3)){    
                    children.put(
                        child.getKey(),
                        child.getValue()
                    );
                }
            }
        }
        for(Map.Entry<String,DataObject_1_0> child : children.entrySet()) {
            child.getValue().objMove(
                descendants,
                child.getKey()
            );
        }
    }
    
    /**
     * Retrieve the meta object
     * 
     * @return the meta-object
     * 
     * @throws ServiceException
     */
    ModelElement_1_0 getClassifier() throws ServiceException{
        return getModel().getElement(this.objGetClass());
    }
    
    /**
     * Removes an object.
     * <p>
     * Neither <code>getValue()</code> nor <code>setValue()</code>
     * calls are allowed after an <code>remove()</code> invocation and
     * <code>isDeleted()</code> will return <code>true</code> unless the
     * object has been transient.
     * 
     * @param updateCache 
     * @exception   ServiceException NOT_SUPPORTED
     *              If the object refuses to be removed.
     * @exception   ServiceException
     *              if the object can't be removed
     */
    @SuppressWarnings("unchecked")
    void objRemove(
        boolean updateCache
    ) throws ServiceException {
        Model_1_0 model = getModel();
        //
        // Cascade Removal To Children
        //
        if(!this.jdoIsPersistent()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "Attempt to remove a transient object"
            );
        } else if(this.jdoIsNew()) {
            for(Flushable flushable : this.flushableValues.values()) {
                if(flushable instanceof Container_1) {
                    this.dataObjectManager.deletePersistentAll(
                        ((Container_1)flushable).values()
                    );
                }
            }
        } else {
            CascadeDelete: for(PlugIn_1_0 plugIn : this.dataObjectManager.getPlugIns()) {
                if(plugIn.requiresCallbackOnCascadedDelete(this)) {
                    for(Map.Entry<String, ModelElement_1_0> e : ((Map<String, ModelElement_1_0>)getClassifier().objGetValue("allFeature")).entrySet()){
                        ModelElement_1_0 featureDef = e.getValue();
                        if(model.isReferenceType(featureDef)) {
                            if(AggregationKind.COMPOSITE.equals(model.getElement(featureDef.objGetValue("referencedEnd")).objGetValue("aggregation"))){
                                this.dataObjectManager.deletePersistentAll(
                                    this.objGetContainer(e.getKey()).values()
                                );
//                                this.objGetContainer(e.getKey()).clear();
                            }
                        }
                    }
                    break CascadeDelete;
                }
            }
        }
        //
        // Remove Object Itself
        //
        this.objMakeTransactional();
        this.dataObjectManager.fireInstanceCallback(this, InstanceLifecycleEvent.DELETE, false);
        this.deleted = true;
        this.getState(false).setLifeCycleEventPending(true);
        //
        // Cascade Removal To Aspects
        //
        if(
            model.isInstanceof(this, "org:openmdx:base:AspectCapable") &&
            !model.isInstanceof(this, "org:openmdx:base:Aspect")
        ){
            this.getAspects().clear();
        }
        //
        // Cache management
        //
        if(updateCache) {
            Container_1 container = this.getContainer(true);
            if(container != null) {
                container.removeFromChache(this.identity.getBase());
            }
        }
    }

    /**
     * Prepare the object for flushing
     * @param transactionTime
     * 
     * @throws ServiceException
     */
    void prepare() throws ServiceException {
        if(!this.untouchable && this.jdoIsDirty() && !this.jdoIsDeleted()) {
            this.dataObjectManager.fireInstanceCallback(this, InstanceLifecycleEvent.STORE, false);
        }
        this.getState(false).setPrepared(true);
    }

    /**
     * Propagate new instances if necessary
     * 
     * @exception   ServiceException
     *              if the object can't be flushed
     * @throws ResourceException 
     */
    void propagate(
        Interaction interaction
    ) throws ServiceException, ResourceException {
        TransactionalState_1 state = this.getState(false);
        if(state.isLifeCycleEventPending()){
            Object_2Facade input;
            if(!state.isFlushed() && this.identity.getLastComponent().isPlaceHolder()) {
                input = Object_2Facade.newInstance(
                    new Path(this.jdoGetTransactionalObjectId()),
                    this.transactionalValuesRecordName
                );
                interaction.execute(
                    this.dataObjectManager.getInteractionSpecs().CREATE,
                    input.getDelegate()
                );
                state.setFlushed(true);
            }
            input = Object_2Facade.newInstance(
                this.identity,
                this.transactionalValuesRecordName
            );
            if(state.isFlushed()) {
                Record reply = interaction.execute(
                    this.dataObjectManager.getInteractionSpecs().MOVE,
                    Records.getRecordFactory().singletonMappedRecord(
                        "map", 
                        null, // recordShortDescription
                        this.transientObjectId, 
                        input.getDelegate()
                    )
                );
                Object_2Facade persistent = Object_2Facade.newInstance(
                    (MappedRecord)((IndexedRecord)reply).get(0)
                );
                this.identity.setTo(persistent.getPath());
            } else {
                interaction.execute(
                    InteractionSpecs.getRestInteractionSpecs(false).CREATE,
                    input.getDelegate()
                );
            }
        }        
    }

    /**
     * Flush the state of the instance to its provider.
     * 
     * @param interaction
     * @param beforeCompletion 
     * 
     * @exception   ServiceException
     *              if the object can't be flushed
     */
    void flush(
        Interaction interaction, 
        boolean beforeCompletion
    ) throws ServiceException {
        TransactionalState_1 state = this.getState(false);
        if(beforeCompletion && !this.untouchable && jdoIsPersistent() && !isProxy()){
            validate(state);   
        }
        flushStructuralFeatures(interaction, state);
        flushBehaviouralFeatures(interaction, state);
        state.setDirtyFeaturesFlushed();
        if(!beforeCompletion) {
    		evictReadLock();
			evictWriteLock();
        }
    }

    /**
     * Tells whether the feature's cardinality is invalid
     * 
     * @param featureDef 
     * 
     * @return <code>true</code> if a mandatory feature is missing
     * 
     * @throws ServiceException
     */
    private boolean cardinalityIsInvalid(
    	ModelElement_1_0 featureDef
    ) throws ServiceException{
    	return 
    		isMandatory(featureDef) &&
    		!dataObjectManager.isExemptFromValidation(this, featureDef) &&
    		isPersistent(featureDef) &&
    		this.objGetValue((String) featureDef.objGetValue("name")) == null;
    }
    
    /**
     * Validate the object before it is flushed to the data store
     * 
     * @throws ServiceException
     */
    private void validate(
        TransactionalState_1 state
    ) throws ServiceException {
        Collection<String> missing = null;
        if(jdoIsDeleted()) {
            // nothing to do
        } else if(jdoIsNew()) {
            for(Map.Entry<String, ModelElement_1_0> attribute : getAttributes().entrySet()){
                if(cardinalityIsInvalid(attribute.getValue())) {
                    if(missing == null) {
                        missing = new ArrayList<String>();
                    }
                    missing.add(attribute.getKey());
                }
            }
        } else if (jdoIsDirty()){
            Map<String, ModelElement_1_0> attributes = getAttributes();
            for(String feature : state.dirtyFeatures(true)) {
                ModelElement_1_0 attribute = attributes.get(feature);
                if(cardinalityIsInvalid(attribute)) {
                    if(missing == null) {
                        missing = new ArrayList<String>();
                    }
                    missing.add(feature);
                }
            }
        }
        if(missing != null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.VALIDATION_FAILURE,
                "Mandatory attribute(s) missing",
                ExceptionHelper.newObjectIdParameter("object", this),
                new BasicException.Parameter("class", objGetClass()),
                new BasicException.Parameter("missing", missing)
            );
        }
    }

    /**
     * Initialize a newly created object facade
     * 
     * @param input
     * @param values
     * 
     * @return the initialize facade
     * @throws ResourceException 
     */
    private Object_2Facade newInput(
    	boolean transactionalId,
    	MappedRecord values
    ) throws ResourceException{
    	Object_2Facade input = transactionalId ? 
    		Object_2Facade.newInstance(this.jdoGetTransactionalObjectId()) :
			Object_2Facade.newInstance(this.jdoGetObjectId());
        input.setVersion(this.jdoGetVersion());
        input.setLock(this.lock);
        if(values != null) {
	        input.setValue(values);
        }
    	return input;
    }
    	
    /**
     * Flush structural features
     * 
     * @param interaction
     * @param state
     * 
     * @throws ServiceException
     * @throws ResourceException
     */
    @SuppressWarnings("unchecked")
    private void flushStructuralFeatures(
        Interaction interaction,
        TransactionalState_1 state
    ) throws ServiceException {
        try {
            if(!jdoIsPersistent() && state.isLifeCycleEventPending()) {
                Map<String,Object> transactionalValues = state.values(false);
                if(this.persistentValues == null) {
                    this.persistentValues = newRecord(this.transactionalValuesRecordName);
                }
                for(Map.Entry<String,Object> e : transactionalValues.entrySet()) {
                    String feature = e.getKey();
                    Object source = e.getValue();
                    Flushable flushable = this.flushableValues.get(feature);
                    if(flushable != null) {
                        try {
                            flushable.flush();
                        } catch (IOException exception) {
                            throw new ServiceException(exception);
                        }
                    } else {
                        this.persistentValues.put(
                            feature,
                            source == null ? null : this.getMarshaller(feature).unmarshal(source)
                        );                    
                    }
                }
                Object_2Facade input = newInput(
                	true,
                	this.persistentValues
                );
                interaction.execute(
                    this.dataObjectManager.getInteractionSpecs().CREATE,
                    input.getDelegate()
                );
                state.setLifeCycleEventPending(false);
                state.setFlushed(true);
            } else if(this.jdoIsDeleted()){
                if(state.isLifeCycleEventPending()){
                    if(!this.jdoIsNew() || state.isFlushed()) {
                        String objectClass = DataObject_1.getRecordName(this, false);
						Object_2Facade input = newInput(
                        	false,
                        	objectClass == null ? null : Records.getRecordFactory().createMappedRecord(objectClass)
                        );
                        interaction.execute(
                            this.dataObjectManager.getInteractionSpecs().DELETE,
                            input.getDelegate()
                        );
                        state.setFlushed(false);
                    }
                    state.setLifeCycleEventPending(false);
                }
            } else if(this.jdoIsNew() && state.isLifeCycleEventPending()) {
                Map<String,Object> transactionalValues = state.values(false);
                if(this.persistentValues == null) {
                    this.persistentValues = newRecord(this.transactionalValuesRecordName);
                }
                for(Map.Entry<String,Object> e : transactionalValues.entrySet()) {
                    String feature = e.getKey();
                    Object source = e.getValue();
                    Flushable flushable = this.flushableValues.get(feature);
                    if(flushable == null) {
                        this.persistentValues.put(
                            feature,
                            source == null ? null : this.getMarshaller(feature).unmarshal(source)
                        );                    
                    } else {
                        try {
                            flushable.flush();
                        } catch (IOException exception) {
                            throw new ServiceException(exception);
                        }
                    }
                }
                Object_2Facade input;
                if(this.isProxy()) {
                    if(PathComponent.isPlaceHolder(this.identity.getBase())) {
                    	input = newInput(
                    		true,
                    		this.persistentValues
                    	);
                        interaction.execute(
                            this.dataObjectManager.getInteractionSpecs().CREATE,
                            input.getDelegate()
                        );
                        input.setPath(this.identity);
                        Record reply = interaction.execute(
                            this.dataObjectManager.getInteractionSpecs().MOVE,
                            Records.getRecordFactory().singletonMappedRecord(
                                "map", 
                                null, // recordShortDescription
                                this.transientObjectId, 
                                input.getDelegate()
                            )
                        );
                        Object_2Facade persistent = Object_2Facade.newInstance(
                            (MappedRecord)((IndexedRecord)reply).get(0)
                        );
                        this.identity.setTo(persistent.getPath());
                    } else {
                    	input = newInput(
                    		false,
                    		this.persistentValues
                    	);
                        interaction.execute(
                            this.dataObjectManager.getInteractionSpecs().PUT,
                            input.getDelegate()
                        );
                    }
                    state.setFlushed(true);
                } else { 
                	input = newInput(
                		false,
                		this.persistentValues
                	);
                    if(state.isFlushed()) {
                        interaction.execute(
                            this.dataObjectManager.getInteractionSpecs().PUT,
                            Records.getRecordFactory().singletonMappedRecord(
                                "map", 
                                null, // recordShortDescription
                                this.transientObjectId, 
                                input.getDelegate()
                            )
                        );
                    } else {
                        interaction.execute(
                            this.dataObjectManager.getInteractionSpecs().CREATE,
                            input.getDelegate()
                        );
                    }
                }
                this.transientOnRollback = true;
                state.setLifeCycleEventPending(false);
            } else if(
                (this.jdoIsDirty() || (this.jdoIsPersistent() && this.digest != null)) &&
                this.jdoGetObjectId().size() > 4 // exclude Authorities and Providers
            ){
                MappedRecord beforeImage = this.persistentValues;
                this.persistentValues = newRecord(this.objGetClass());
                Map<String,Object> transactionalValues = state.values(false);
                for(String feature : state.dirtyFeatures(true)){
                    Flushable flushable = this.flushableValues.get(feature);
                    if(flushable != null) {
                        try {
                            flushable.flush();
                        } catch (IOException exception) {
                            throw new ServiceException(exception);
                        }
                    } else if(transactionalValues.containsKey(feature)) {
                        Object source = transactionalValues.get(feature);
                        if(source instanceof Flushable) {
                            try {
                                ((Flushable)source).flush();
                            } catch (IOException exception) {
                                throw new ServiceException(exception);
                            }
                        } else {
                            this.persistentValues.put(
                                feature,
                                this.getMarshaller(feature).unmarshal(source)
                            );                    
                        }
                    } else if (beforeImage != null) {
                        this.persistentValues.put(
                            feature,
                            beforeImage.get(feature)
                        );
                    }
                }
                Object_2Facade input = newInput(
                	false,
                	this.persistentValues
                );
                this.persistentValues = beforeImage;
                interaction.execute(
                    this.dataObjectManager.getInteractionSpecs().PUT,
                    input.getDelegate()
                );
            }
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
        }
    }

    /**
     * Flush behavioural features
     * 
     * @param interaction
     * @param state
     * 
     * @throws ServiceException
     */
    private void flushBehaviouralFeatures(
        Interaction interaction,
        TransactionalState_1 state)
        throws ServiceException {
        Queue<Operation> operations = state.operations(true);
        for(
            Operation operation = operations.poll();
            operation != null;
            operation = operations.poll()
        ){
            operation.invoke(interaction);
        }
    }


    /**
     * Tells whether the given feature has been modified
     * 
     * @param feature
     * 
     * @return <code>true</code> if the feature has been modified
     * 
     * @throws ServiceException
     */
    private boolean isFeatureModified(
        ModelElement_1_0 feature
    ) throws ServiceException {
        if(this.beforeImage == null) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE,
            "Before image should have been set"
        );
        Multiplicity multiplicity = ModelHelper.getMultiplicity(feature);
        String featureName = (String) feature.objGetValue("name");
        switch(multiplicity) {
        	case SINGLE_VALUE: case OPTIONAL: 
        		return !equal(this.beforeImage.objGetValue(featureName), this.objGetValue(featureName));
        	case LIST: 
        		return !equal(this.beforeImage.objGetList(featureName), this.objGetList(featureName));
        	case SET: 
        		return !equal(this.beforeImage.objGetSet(featureName), this.objGetSet(featureName));
        	case SPARSEARRAY: 
        		return !equal(this.beforeImage.objGetSparseArray(featureName), this.objGetSparseArray(featureName));
        	case STREAM:
                SysLog.log(
                    Level.FINER, 
                    "{0} features are not compared with their before image, " +
                    "the feature {1} in the object {2} as therefore treated as modified", 
                    multiplicity, 
                    featureName, 
                    this.jdoIsPersistent() ? this.jdoGetObjectId().toXRI() : this.jdoGetTransactionalObjectId()
                );
                return true;
        	    
            default:
                SysLog.log(
                    Level.WARNING, 
                    "Unsupported Multiplicity {0}, treat the feature {1} in the object {2} as modified", 
                    multiplicity, 
                    featureName, 
                    this.jdoIsPersistent() ? this.jdoGetObjectId().toXRI() : this.jdoGetTransactionalObjectId()
                );
                return true;
        }
    }
    
    /**
     * Tests whether some non-derived features have been modified
     * 
     * @return <code>true</code> if some non-derived features have been modified
     * 
     * @throws ServiceException
     */
    public boolean objIsModified() throws ServiceException{
        TransactionalState_1 state = this.getState(false);
        Map<String,ModelElement_1_0> attributes = getAttributes();
        boolean modified = state.isTouched();
        Set<String> dirtyFeatures = state.dirtyFeatures(true); 
        for(
            Iterator<String> i = dirtyFeatures.iterator(); 
            i.hasNext();
        ){
            ModelElement_1_0 attribute = attributes.get(i.next());
            if(!Boolean.TRUE.equals(attribute.objGetValue("isDerived"))){
                if(this.isFeatureModified(attribute)) {
                    modified = true;
                } else {
                    i.remove();
                }
            }
        }
        return modified;
    }

    /**
     * Retrieve the object's structural features
     * 
     * @return the object's structural features
     * 
     * @throws ServiceException
     */
    private Map<String, ModelElement_1_0> getAttributes(
    ) throws ServiceException {
        ModelElement_1_0 classifier = getClassifier();
        return classifier.getModel().getAttributeDefs(
            classifier,
            false, // sub-types
            true // includeDerived
        );
    }
    
    /**
     * Tells whether an attribute is persistent
     * 
     * @param attribute the attribute to be tested
     * 
     * @return <code>true</code> if the attribute is persistent
     * 
     * @throws ServiceException
     */
    private static boolean isPersistent(
        ModelElement_1_0 attribute
    ) throws ServiceException {
        return Persistency.getInstance().isPersistentAttribute(attribute); 
    }

    /**
     * Tells whether an attribute is streamed
     * 
     * @param attribute the attribute to be tested
     * 
     * @return <code>true</code> if the attribute is streamed
     * 
     * @throws ServiceException
     */
    private static boolean isStreamed(
        ModelElement_1_0 attribute
    ) throws ServiceException {
        return  ModelHelper.getMultiplicity(attribute).isStreamValued();
    }
    
    /**
     * Tells whether an attribute is mandatory
     * 
     * @param attribute the attribute to be tested
     * 
     * @return <code>true</code> if the attribute is mandatory
     * 
     * @throws ServiceException
     */
    private static boolean isMandatory(
        ModelElement_1_0 attribute
    ) throws ServiceException {
        return ModelHelper.getMultiplicity(attribute) == Multiplicity.SINGLE_VALUE;
    }
    
    /**
     * Provide the before image
     * 
     * @return the before image
     * 
     * @throws ServiceException
     */
    public DataObject_1 getBeforeImage(
    ) throws ServiceException {
    	if(this.jdoIsNew()) return null;
    	if(this.beforeImage == null || this.beforeImage.objIsInaccessible()) {
            this.beforeImage = new DataObject_1(
                this, 
                true
            );
        }
        return this.beforeImage;
    }
    
    public void makePersistentCleanWhenUnmodified(
    ) throws ServiceException{
        getBeforeImage(); // prerequisite for objIsModified()
        objIsModified(); // this invocation removes idempotent modifications
        evictBeforeImage(); // free memory
    }
    
    void afterCompletion(
        int status
    ) throws ServiceException {
        switch(status) {
            case javax.transaction.Status.STATUS_COMMITTED:
                if(this.jdoIsDeleted()){
                    if(this.jdoIsPersistent()) {
                        this.dataObjectManager.invalidate(
                            this.identity, 
                            false // makeNonTransactional
                        );
                    }
                    this.deleted = false;
                    this.identity = null;
                }
                break;
            case javax.transaction.Status.STATUS_ROLLEDBACK:
                this.getState(false).setLifeCycleEventPending(false);
                if(this.jdoIsDeleted()){
                    this.deleted = false;
                }
                if(this.transientOnRollback || this.jdoIsNew()){
                    if(this.dataObjectManager != null) {
                        this.dataObjectManager.makeTransient(this);
                    }
                    if(this.container != null) {
                        this.container.removeFromChache(this.identity.getBase());
                        this.container = null;
                        this.aspects = null;
                    }
                    this.identity = null;
                }
                break;
        }
        Container_1 container = this.getContainer(true);
        if(container != null) {
            container.openmdxjdoEvict(false, true);
        }
        this.transientOnRollback = false;
        this.created = false;
        this.beforeImage = null;
    }

    /**
     * Reset the read lock
     */
    private void evictReadLock(){
        this.lock = null;
    }
    
    /**
     * Reset the write lock
     */
    private void evictWriteLock(){
        this.digest = null;
    }
    
    /**
     * Evict the data object
     */
    void evict(
    ){
        boolean clear;
        if(this.jdoIsDirty()) {
            clear = false; // to not to lose changes
        } else if (this.jdoIsPersistent()){
            try {
                this.dataObjectManager.fireInstanceCallback(this, InstanceLifecycleEvent.CLEAR, true);
            } catch (ServiceException exception) {
                throw new JDOFatalInternalException(
                    "Lenient instance callback exception should have been logged and not propagated",
                    exception.getCause()
                );
            }
            clear = true;
        } else if(this.isProxy()) {
            clear = true;
        } else {
            clear = false;
        }
        if(clear){   
            this.clear();
            for(Map.Entry<String, Flushable> entry : this.flushableValues.entrySet()) {
                Flushable value = entry.getValue();
                if(value instanceof ManagedAspect) {
                    ((ManagedAspect)value).evict();
                } else if (value instanceof PersistenceCapableCollection) {
                    ((PersistenceCapableCollection)value).openmdxjdoEvict(
                        false, // allMembers
                        true // allSubSets
                     );
                }
            }
        }
        evictReadLock();
		evictWriteLock();
        this.persistentValues = null;
        evictBeforeImage();
    }

    /**
     * The before image is evicted unless it is persistent.
     */
    private void evictBeforeImage() {
        if(this.beforeImage != null && !this.beforeImage.jdoIsPersistent()) {
            this.beforeImage.evictUnconditionally();
            this.beforeImage = null;
        }
    }

    private void clear(
    ){
        try {
            TransactionalState_1 state = getState(true);
            if(state != null){ 
                state.clear();
            }
        } catch (JDOException ignore) {
            // Eviction should nevertheless be successful
        }
    }
    
    /**
     * Tells whether the object is out-of-sync with its "remote" counterpart
     * 
     * @return <code>true</code> if the object is out-of-sync with its 
     * "remote" counterpart
     */
    final boolean isOutOfSync(){
        TransactionalState_1 state = this.getState(true);
        return state != null && state.isOutOfSync();
    }
    
    /**
     * Tests whether this object is dirty. Instances that have been modified,
     * deleted, or newly made persistent in the current unit of work return
     * true.
     * <p>
     * Transient instances return false.
     *
     * @return true if this instance has been modified in the current unit
     *         of work.
     * @throws ServiceException 
     */
    public boolean jdoIsDirty(
    ) {
        if(this.jdoIsNew() || this.jdoIsDeleted()) {
            return true;
        } else if (this.jdoIsPersistent()) {
            TransactionalState_1 state = this.getState(true);
            return state != null && state.isDirty();
        } else {
            return false;
        }
    }

    /**
     * Tests whether this object is persistent. Instances that represent
     * persistent objects in the data store return true.
     *
     * @return true if this instance is persistent.
     */
    public boolean jdoIsPersistent(
    ){
        return this.identity != null;
    }
    
    /**
     * Tells whether the object is contained in a container
     * 
     * @return <code>true</code> if the object is either persistent or has 
     * already been moved to a container
     */
    public boolean objIsContained(){
        return this.jdoIsPersistent() || this.container != null;
    }
    
    /**
     * Tests whether this object has been newly made persistent. Instances
     * that have been made persistent in the current unit of work return true.
     * <p>
     * Transient instances return false.
     *
     * @return  true if this instance was made persistent in the current unit
     *          of work.
     */
    public boolean jdoIsNew(
    ){
        return this.created;
    }

    /**
     * Tests whether this object is hollow
     * 
     * @return <code>true</code> if the object is hollow
     */
    protected boolean objIsHollow(
    ){
        return this.persistentValues == null;
    }

    /**
     * Tests whether this object is a proxy
     * 
     * @return <code>true</code> if this object is a proxy
     */
    public boolean isProxy(
    ){
        return this.dataObjectManager.isProxy();
    }
    
    /**
     * Tests whether this object is a virtual object
     * 
     * @return <code>true</code> if this object is a virtual object
     */
    private boolean isVirtual(
    ){
        try {
            return 
                this.jdoIsPersistent() && 
                SharedObjects.getPlugInObject(this.dataObjectManager, ManagedConnectionCache_2_0.class).isAvailable(Mode.BASIC,this.identity);
        } catch (Exception ignore) {
            return false;
        }        
    }
    
    /**
     * Tests whether this object becomes transient on rollback.
     *
     * @return  true if this instance becomes transient on rollback
     */
    public boolean isTransientOnRollback(
    ){
        return jdoIsNew();
    }

    /**
     * Tests whether this object has been deleted. Instances that have been
     * deleted in the current unit of work return true.
     * Transient instances return false.
     *
     * @return  true if this instance was deleted in the current unit of work.
     */
    public boolean jdoIsDeleted(
    ){
        return this.deleted;
    }

    /**
     * Tests whether this object belongs to the current unit of work.
     *
     * @return  true if this instance belongs to the current unit of work.
     */
    public boolean jdoIsTransactional(
    ) {
        try {
            return this.getUnitOfWork().getMembers().contains(this);
        } catch(Exception e) {
            throw new JDOUserException(
                "Unable to get object state",
                e,
                this
            );            
        }
    }

    /**
     * Tests whether this object can't leave its hollow state
     *
     * @return  true if this instance is inaccessible
     */
    public boolean objIsInaccessible(
    ){
        return this.inaccessabilityReason != null;
    }

    /**
     * Retrieve the reason for the objects inaccessibility
     *
     * @return Returns the inaccessibility reason
     */
    public ServiceException getInaccessibilityReason() {
        return this.inaccessabilityReason;
    }
    
    /**
     * Ensure that the object is read enabled
     * 
     * @param reload 
     * @param fetchPlan
     * @param features 
     * @param beforeImage load the complete before image
     * @exception   ServiceException    ILLEGAL_STATE
     *                  if the object is deleted
     */
    @SuppressWarnings("unchecked")
    DataObject_1 objRetrieve(
        boolean reload, 
        FetchPlan fetchPlan, 
        Set<String> features, 
        boolean beforeImage
    ) throws ServiceException {
        if(this.objIsInaccessible()){
            throw new ServiceException(
                this.getInaccessibilityReason()
            );
        } else if(reload || this.objIsHollow()) {
            if(this.isProxy() && !isVirtual()) {
                UnitOfWork_1 unitOfWork = this.dataObjectManager.currentTransaction();
                unitOfWork.synchronize();
                return this.unconditionalLoad(
                	fetchPlan, 
                	features, 
                	false // refresh
                );
            } else if (jdoIsPersistent() && !jdoIsNew()) {
                return this.unconditionalLoad(
                	fetchPlan, 
                	features, 
                	false // refresh
                );
            } else {
                return this;
            }
        } else if (fetchPlan == null){
            Set<String> fetched = beforeImage ? (
        		objIsHollow() ? Collections.emptySet() : persistentValues.keySet()
            ) : objDefaultFetchGroup();
            Set<String> missing = null;
            for(Map.Entry<String, ModelElement_1_0> attribute : getAttributes().entrySet()) {
                String attributeName = attribute.getKey();
                if(
                    !fetched.contains(attributeName) && 
                    isPersistent(attribute.getValue()) && 
                    !isStreamed(attribute.getValue())
                ) {
                    if(missing == null) {
                        missing = new HashSet<String>();
                    }
                    missing.add(attributeName);
                }
            }            
            if(missing != null) try {
                MappedRecord persistentValues = newRecord(
                    this.persistentValues.getRecordName()
                );
                persistentValues.putAll(this.persistentValues);
                List<OrderSpecifier> orders = new ArrayList<OrderSpecifier>();
                for(String feature : missing) {
                    orders.add(
                        new OrderSpecifier(feature, SortOrder.UNSORTED)
                    );
                }
                Query_2Facade input = Facades.newQuery(this);
                input.setQuery(
                    JavaBeans.toXML(
                        new Filter(
                            null, // condition
                            orders,
                            null // extension
                        )
                    )
                );
                IndexedRecord indexedRecord = (IndexedRecord) this.dataObjectManager.getInteraction().execute(
                    this.dataObjectManager.getInteractionSpecs().GET,
                    input.getDelegate()
                );
                if(indexedRecord.isEmpty()) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "Could not fetch missing attributes",
                    ExceptionHelper.newObjectIdParameter("id", this),
                    new BasicException.Parameter("feature", missing)
                );
                Object_2Facade output = Facades.asObject(
                    (MappedRecord)indexedRecord.get(0)
                );                
                persistentValues.putAll(
                    output.getValue()
                );
                this.persistentValues = persistentValues;
            } catch (ResourceException exception) {
                throw new ServiceException(exception);
            }
            return this;
        } else {
        	// TODO Retrieve according to fetch plan
            return this;
        }
    }

    /**
     * Test whether the given attribute has to be loaded
     * 
     * @param name the attribute's name
     * 
     * @return <code>true</code> if the given attribute has to be loaded
     * 
     * @throws ServiceException 
     */
    private boolean attributeMustBeLoaded(
        String name
    ) throws ServiceException{
        if(this.jdoIsPersistent() && !this.jdoIsNew()) {
            this. objRetrieve(
                false, // reload
                this.dataObjectManager.getFetchPlan(), 
                Collections.singleton(name), false
            );
            return true;
        } else if (this.isProxy() && Boolean.TRUE.equals(this.getFeature(name).objGetValue("isDerived"))) {
            this.objMakeTransactional();
            TransactionalState_1 state = this.getState(false);
            if(!state.isFlushed()) {
                state.setLifeCycleEventPending(true);
            }
            this.objRetrieve(
                true, // reload
                this.dataObjectManager.getFetchPlan(), 
                Collections.singleton(name), false
            );
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Ensure that the attribute is fetched.
     *
     * @param     name
     *            attribute name
     * @param     stream
     *            defines whether the value is a stream or not
     * @param clear 
     * 
     * @exception   ServiceException    ILLEGAL_STATE
     *                  if the object is deleted
     */
    @SuppressWarnings("unchecked")
    final Object getPersistentAttribute(
        String name,
        boolean stream,
        Multiplicity multiplicity, 
        boolean clear
    ) throws ServiceException {
        Object rawValue;
        //
        // Retrieve
        //
        if(!clear && this.attributeMustBeLoaded(name)) {
            try {
                rawValue = this.persistentValues.get(name);
                if(
                    rawValue == null &&
                    !this.persistentValues.containsKey(name)
                ) {
                    MappedRecord persistentValues = newRecord(this.persistentValues.getRecordName());
                    persistentValues.putAll(this.persistentValues);
                    Filter attributeSpecifier = new Filter(
                        null, // condition
                        Collections.singletonList(
                            new OrderSpecifier(name, SortOrder.UNSORTED)
                        ),
                        null // extension
                    );
                    Query_2Facade input = Facades.newQuery(this);
                    input.setQuery(JavaBeans.toXML(attributeSpecifier));
                    IndexedRecord indexedRecord = (IndexedRecord) this.dataObjectManager.getInteraction().execute(
                        this.dataObjectManager.getInteractionSpecs().GET,
                        input.getDelegate()
                    );
                    if(indexedRecord.isEmpty()) throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND,
                        "Could not fetch attribute",
                        ExceptionHelper.newObjectIdParameter("id", this),
                        new BasicException.Parameter("feature", name)
                    );
                    Object_2Facade output = Facades.asObject(
                        (MappedRecord)indexedRecord.get(0)
                    );                
                    persistentValues.putAll(
                        output.getValue()
                    );
                    rawValue = stream ? persistentValues.remove(name) : persistentValues.get(name);
                    this.persistentValues = persistentValues;
                }
            } catch (ResourceException exception) {
                throw new ServiceException(exception);
            }
        } else if (this.persistentValues == null) {
            rawValue = null;
        } else {    
            rawValue = this.persistentValues.get(name);
        }
        //
        // Normalize 
        //
        Object normalizedValue = rawValue;
        switch(multiplicity) {
	        case SINGLE_VALUE: case OPTIONAL: {
	            if(rawValue instanceof List) {
	                List<?> source = (List<?>)rawValue;
	                normalizedValue = source.isEmpty() ? null : source.get(0);
	            }
	        } break;
	        case SET: case LIST: {
	            if(rawValue instanceof List<?>) {
	                if(clear){
	                    ((Collection<?>)rawValue).clear();
	                }
	            } else {
	                List<Object> target = new ArrayList<Object>();
	                if(!clear){
	                    if (rawValue instanceof Collection<?>) {
	                        target.addAll((Collection<?>)rawValue);
	                    } else if (rawValue instanceof Map<?,?>) {
	                        target.addAll(
	                            SortedMaps.asSparseArray(
	                                rawValue instanceof SortedMap<?,?> ? (SortedMap<Integer,?>) rawValue : new TreeMap<Integer,Object>((Map<Integer,?>)rawValue)
	                            ).asList()
	                        );
	                    } else if(rawValue != null) {
	                        target.add(rawValue);
	                    }
	                }
	                normalizedValue = target;
	            } 
	        } break;
	        case SPARSEARRAY: {
	            if(rawValue instanceof SparseArray<?>) {
	                if(clear) {
	                    ((SparseArray<?>)rawValue).clear();
	                }
	            } else {
	                SparseArray<Object> target = new TreeSparseArray<Object>();
	                if(!clear){
	                    if(rawValue instanceof Map<?,?>) {
	                        target.putAll((Map<Integer,?>) rawValue);
	                    } else if(rawValue instanceof Collection<?>) {
	                        int i = 0;
	                        for(Object value : (Collection<?>)rawValue) {
	                            target.put(i++, value);
	                        }
	                    } else if (rawValue != null) {
	                        target.put(0, rawValue);
	                    }
	                }
	                normalizedValue = target;
	            }
	        } break;
        }
        //
        // Cache
        //
        if(this.persistentValues != null) {
            if(stream) {
                this.persistentValues.remove(name);
            } else if(rawValue != normalizedValue){
                this.persistentValues.put(
                    name,
                    normalizedValue
                );
            }
        }
        return normalizedValue;
    }

    /**
     * Ensure that the argument is single valued
     *
     * @param argument
     *        the argument to be checked
     *
     * @exception   ServiceException    BAD_PARAMETER
     *                  if the argument is multi-valued
     */
    protected void assertSingleValued(
        Object argument
    ) throws ServiceException {
        if(argument instanceof Collection<?>) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.BAD_PARAMETER,
                "Single valued argument expected",
                new BasicException.Parameter("class", argument.getClass().getName())
            );
        }
    }

    /**
     * Set an attribute's value.
     * <p>
     * This method returns a <code>BAD_PARAMETER</code> exception unless the
     * feature is single valued or a stream.
     *
     * @param       feature
     *              the attribute's name
     * @param       to
     *              the object.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is write protected.
     * @exception   ServiceException BAD_PARAMETER
     *              if the feature is multi-valued
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ServiceException
     *              if the object is not accessible
     */
    public void objSetValue(
        String feature,
        Object to
    ) throws ServiceException {
        this.assertSingleValued(to);
        UnitOfWork_1 unitOfWork = this.getUnitOfWorkIfTransactional(feature);
        (
            unitOfWork == null ? this.transientValues : unitOfWork.getState(this,false).values(false)
        ).put(
            feature, 
            to
        );
        if(to != null && this.isAspectHasCore(feature)) {
            for(PlugIn_1_0 plugIn : this.dataObjectManager.getPlugIns()) {
                plugIn.postSetCore(this, (DataObject_1) to);
            }
        }
    }

    /**
     * Retrieve a large object
     * 
     * @param featureName
     * @param featureDef
     * @return the requested value
     * @throws ServiceException
     */
    private LargeObject getLargeObjectValue(
        final String featureName,
        final ModelElement_1_0 featureDef
    ) throws ServiceException{
        String type = (String) featureDef.getModel().getDereferencedType(featureDef.objGetValue("type")).objGetValue("qualifiedName");
        Object persistentValue = this.getPersistentAttribute(
            featureName, 
            true, // stream 
            Multiplicity.SINGLE_VALUE, 
            false // clear
        );
        if(PrimitiveTypes.BINARY.equals(type)) {
            InputStream value;
            Long length;
            if(persistentValue instanceof InputStream) {
                //
                // Compatibility Mode
                //
                value = (InputStream) persistentValue;
                length = null;
            } else try {
                //
                // Native Mode
                //
                BinaryLargeObject object = (BinaryLargeObject) persistentValue;
                value = object.getContent();
                length = object.getLength();
            } catch (IOException exception) {
               throw new ServiceException(exception); 
            }
            return new BinaryLargeObjects.StreamLargeObject(value, length) {
                
                private static final long serialVersionUID = 6010313705463030648L;

                @Override
                protected InputStream newContent(
                ) throws IOException {
                    Object persistentValue;
                    try {
                        persistentValue = DataObject_1.this.getPersistentAttribute(
                            featureName, 
                            true, // stream 
                            Multiplicity.SINGLE_VALUE, 
                            false // clear
                        );
                    } catch (ServiceException exception) {
                        BasicException cause = exception.getCause();
                        throw (IOException) new IOException(cause.getDescription()).initCause(cause);
                    }
                    if(persistentValue instanceof InputStream) {
                        //
                        // Compatibility Mode
                        //
                        return (InputStream) persistentValue;
                    } else {
                        //
                        // Native Mode
                        //
                        return ((BinaryLargeObject)persistentValue).getContent();
                    }
                }
                
            }; 
        } else if (PrimitiveTypes.STRING.equals(type)) {
            Reader value;
            Long length;
            if(persistentValue instanceof Reader) {
                //
                // Compatibility Mode
                //
                value = (Reader) persistentValue;
                length = null;
            } else try {
                //
                // Native Mode
                //
                CharacterLargeObject object = (CharacterLargeObject) persistentValue;
                value = object.getContent();
                length = object.getLength();
            } catch (IOException exception) {
               throw new ServiceException(exception); 
            }
            return new CharacterLargeObjects.StreamLargeObject(value,length){
                
                private static final long serialVersionUID = -6402214529785948658L;

                @Override
                protected Reader newContent(
                ) throws IOException {
                    Object persistentValue;
                    try {
                        persistentValue = DataObject_1.this.getPersistentAttribute(
                            featureName, 
                            true, // stream 
                            Multiplicity.SINGLE_VALUE, 
                            false // clear
                        );
                    } catch (ServiceException exception) {
                    	BasicException cause = exception.getCause();
                    	throw (IOException) new IOException(cause.getDescription()).initCause(cause);
                    }
                    if(persistentValue instanceof Reader) {
                        //
                        // Compatibility Mode
                        //
                        return (Reader) persistentValue;
                    } else {
                        //
                        // Native Mode
                        //
                        return ((CharacterLargeObject)persistentValue).getContent();
                    }
                }
                
            }; 
        } else throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "Unsupported stream type",
            new BasicException.Parameter("expected", PrimitiveTypes.BINARY, PrimitiveTypes.STRING),
            new BasicException.Parameter("actual", type)
        );
        
    }
        
    /**
     * Get an attribute.
     * <p>
     * Note: This specific implementation may allow to return multivalued
     * attributes as well!
     *
     * @param       feature
     *              the feature's name
     *
     * @return      the object representing the feature;
     *              or null if the feature's value hasn't been set yet.
     *
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ServiceException
     *              if the object is not accessible
     */
    public Object objGetValue(
        final String feature
    ) throws ServiceException {
        if (this.transientValues == null) {
            UnitOfWork_1 unitOfWork = this.getUnitOfWork();
            Map<String,Object> transactionalValues;
            if(this.jdoIsTransactional()) { 
                transactionalValues = unitOfWork.getState(DataObject_1.this, false).values(false);
                Object transactionalValue = transactionalValues.get(feature);
                if(transactionalValue != null || transactionalValues.containsKey(feature)) {
                    return transactionalValue;
                }
            } else {
                transactionalValues = null; 
            }
            ModelElement_1_0 featureDef = this.getFeature(feature);
            Object transactionalValue;
            if(ModelHelper.getMultiplicity(featureDef).isStreamValued()){
                transactionalValue = this.getLargeObjectValue(feature, featureDef);
            } else {
                Object nonTransactionalValue = this.getPersistentAttribute(
                    feature, 
                    false, 
                    Multiplicity.SINGLE_VALUE, 
                    false
                );
                transactionalValue = 
                    nonTransactionalValue == null ? null :  
                    featureDef == null ? nonTransactionalValue : // an embedded object's class
                        this.getMarshaller(featureDef).marshal(nonTransactionalValue);
            }
            if(transactionalValues != null) {
                transactionalValues.put(feature, transactionalValue);
            }
            return transactionalValue;
        } else {
            if(
                (this.objIsHollow() && !this.isProxy()) ||
                this.transientValues.containsKey(feature)
            ) {
                return this.transientValues.get(feature);
            } else {
                ModelElement_1_0 featureDef = this.getFeature(feature);
                Object transientValue;
                if(ModelHelper.getMultiplicity(featureDef).isStreamValued()){
                    transientValue = this.getLargeObjectValue(feature, featureDef);
                } else {
                    Object clonedValue = this.getPersistentAttribute(
                        feature, 
                        false, // stream
                        Multiplicity.SINGLE_VALUE,
                        false // clear
                    );
                    transientValue = 
                        clonedValue == null ? null :  
                        featureDef == null ? clonedValue : // an embedded object's class
                        this.getMarshaller(featureDef).marshal(clonedValue);
                }
                (this.jdoIsTransactional() ? this.getState(false).values(false) : this.transientValues).put(
                    feature, 
                    transientValue
                );
                return transientValue;
            }
        }
    }

    /**
     * Cast the value of a given feature
     * 
     * @param feature
     * @param type
     * @param value
     * 
     * @return the value
     * @throws ServiceException if the value is not of the given type
     */
    private <T> T getFlushable(
        String feature,
        Class<T> type
    ) throws ServiceException{
        Object value = this.flushableValues.get(feature);
        if(value == null || type.isInstance(value)) {
            return type.cast(value);
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "The feature's cache contains already a value of another type",
                ExceptionHelper.newObjectIdParameter("object", this),
                new BasicException.Parameter("feature", feature),
                new BasicException.Parameter("expected", type.getName()),
                new BasicException.Parameter("actual", value.getClass().getName())
            );
        }
    }
    
    /**
     * Get a List attribute.
     * <p>
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ClassCastException
     *              if the feature's value is not a list
     */
    @SuppressWarnings("unchecked")
    public List<Object> objGetList(
        String feature
    ) throws ServiceException {
    	List<Object> flushable = getFlushable(feature, List.class);
        return flushable == null ? (List<Object>) Maps.putUnlessPresent(
            this.flushableValues,
            feature,
            new ManagedList(
                feature,
                this.getMarshaller(feature)
            )
        ) : flushable;
    }

    /**
     * Get a Set attribute.
     * <p>
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature     List<Object> flushable = getFlushable(feature, List.class);

     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ClassCastException
     *              if the feature's value is not a set
     */
    @SuppressWarnings("unchecked")
    public Set<Object> objGetSet(
        String feature
    ) throws ServiceException {
    	Set<Object> flushable = getFlushable(feature, Set.class);
        return flushable == null ? (Set<Object>) Maps.putUnlessPresent(
            this.flushableValues,
            feature,
            new ManagedSet(
                feature,
                this.getMarshaller(feature)
            )
        ) : flushable;
    }

    /**
     * Get a SparseArray attribute.
     * <p>
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature's value is not a sparse array
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     */
    @SuppressWarnings("unchecked")
    public SortedMap<Integer,Object> objGetSparseArray(
        String feature
    ) throws ServiceException {
        SortedMap<Integer,Object> flushable = getFlushable(feature, SortedMap.class);
        return flushable == null ? (SortedMap<Integer,Object>) Maps.putUnlessPresent(
            this.flushableValues,
            feature,
            new ManagedSortedMap(
                feature,
                this.getMarshaller(feature)
            )
        ) : flushable;
    }

    /**
     * Get a reference feature.
     * <p>
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature is not a reference
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     */
    public Container_1_0 objGetContainer(
        String feature
    )  throws ServiceException {
        Container_1_0 flushable = getFlushable(feature, Container_1_0.class);
        return flushable == null ? (Container_1_0) Maps.putUnlessPresent(
            this.flushableValues,
            feature,
            new Container_1(this,feature)
        ) : flushable;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
     */
    public boolean execute(
        InteractionSpec ispec, 
        Record input, 
        Record output
    ) throws ResourceException {
        if(ispec instanceof MethodInvocationSpec) {
            try {
                MethodInvocationSpec methodInvocationSpec = (MethodInvocationSpec) ispec;
                Operation entry = new Operation(
                    methodInvocationSpec.getFunctionName(),
                    (MappedRecord)input,
                    (MappedRecord)output
                );
                boolean query = methodInvocationSpec.getInteractionVerb() == InteractionSpec.SYNC_SEND_RECEIVE;            
                if(query) {
                    entry.invoke(this.dataObjectManager.getInteraction());
                } else {
                    this.objMakeTransactional();
                    this.getState(false).operations(false).offer(entry);
                }
                return query;
            } catch (ServiceException exception) {
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        exception.getCause().getDescription(),
                        BasicException.newEmbeddedExceptionStack(exception)
                    )
                );
            }
        } else {
            throw ResourceExceptions.initHolder(
                new ResourceException(
                    "Bad interaction spec for method invocations",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("expected", MethodInvocationSpec.class.getName()),
                        new BasicException.Parameter("actual", ispec == null ? null : ispec.getClass().getName())
                    )
                )
            );
        }
    }

    Object noContent (
        Object source
    ){
        if(source instanceof DataObject_1){
            DataObject_1 object = (DataObject_1) source;
            return AbstractDataObject_1.toString(
                object, 
                object.transactionalValuesRecordName, 
                null
            );
        } else {
            return source;
        }
    }

    /**
     *
     */
    @Override
    public String toString(
    ){
        if(this.objIsInaccessible()) {
            return AbstractDataObject_1.toString(this, null);
        } else {
            try {
                Map<Object,Object> content = new HashMap<Object,Object>();
                TransactionalState_1 state = this.getState(true);
                String description;
                if(state == null) {
                    description = null;
                } else {
                    for(Map.Entry<String,Object> e : state.values(false).entrySet()) {
                        Object v = e.getValue();
                        if(v instanceof Collection) {
                            if (v instanceof List) {
                                List<Object> t = new ArrayList<Object>();
                                for(
                                        Iterator<?> j = ((List<?>)v).iterator();
                                        j.hasNext();
                                ) {
                                    t.add(
                                        this.noContent(j.next())
                                    );
                                }
                            } else if (v instanceof Set) {
                                Set<Object> t = new HashSet<Object>();
                                for(
                                        Iterator<?> j = ((Set<?>)v).iterator();
                                        j.hasNext();
                                ) {
                                    t.add(
                                        this.noContent(j.next())
                                    );
                                }
                            } // else ignore
                        } else if (v instanceof SortedMap) {
                            SortedMap<Object,Object> t = new TreeMap<Object,Object>();
                            for(
                                    Iterator<?> j = ((SortedMap<?,?>)v).entrySet().iterator();
                                    j.hasNext();
                            ){
                                Map.Entry<?,?> k = (Map.Entry<?,?>)j.next();
                                t.put(
                                    k.getKey(),
                                    this.noContent(k.getValue())
                                );
                            }
                        } else {
                            content.put(e.getKey(), this.noContent(v));
                        }
                    }
                    description = state.isPrepared() ? "prepared" : "not prepared";
                }
                return AbstractDataObject_1.toString(
                    this, 
                    this.transactionalValuesRecordName, 
                    description
                ) + ", attributes=" + IndentingFormatter.toString(
                    content
                );
            } catch (Exception exception) {
                return AbstractDataObject_1.toString(
                    this, 
                    this.transactionalValuesRecordName, 
                    exception.getMessage()
                );
            }
        }
    }

    /**
     * Save the data of the <tt>Object_1_0</tt> instance to a stream (that
     * is, serialize it).
     *
     * @serialData The objects data
     */
    private synchronized void writeObject(
        java.io.ObjectOutputStream stream
    ) throws java.io.IOException {
        try {
            TransactionalState_1 state = this.getState(true);
            stream.defaultWriteObject();
            if(state == null) {
                stream.writeInt(0);
            } else {
                Set<String> features = state.dirtyFeatures(true);
                Map<String,Object> source = state.values(false);
                stream.writeInt(features.size());
                for(String feature : features) {
                    stream.writeObject(feature);
                    stream.writeObject(source.get(feature));
                }
            }
        } catch (JDOException exception) {
            throw (IOException) new IOException().initCause(exception);
        }
    }

    /**
     * Reconstitute the <tt>Object_1_0</tt> instance from a stream (that is,
     * deserialize it).
     */
    private synchronized void readObject(
        java.io.ObjectInputStream stream
    ) throws java.io.IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.flushableValues = new ConcurrentHashMap<String,Flushable>();
        int count = stream.readInt();
        if(count > 0) {
            TransactionalState_1 state;
            try {
                state = this.getState(false);
            } catch (JDOException exception) {
                throw (IOException) new IOException().initCause(exception);
            }
            Set<String> features = state.dirtyFeatures(false);
            Map<String,Object> target = state.values(false);
            for(int i = 0; i < count; i++) {
                String feature = (String) stream.readObject();
                features.add(feature);
                target.put(feature, stream.readObject());
            }
        }
        this.dataObjectManager.putUnlessPresent(this.identity, this);
    }

    @SuppressWarnings("unchecked")
    private final ModelElement_1_0 getFeature(
        String featured,
        String kind, 
        String feature
    ) throws ServiceException{
        Map<String,ModelElement_1_0> features = (Map<String, ModelElement_1_0>)getModel().getElement(
            featured
        ).objGetValue(kind);
        return features == null ? null : features.get(feature);
    }

    /**
     * Determine the marshaller to be used
     * 
     * @param feature
     * 
     * @return an object or datatype marshaller
     * 
     * @throws ServiceException 
     */
    private final Marshaller getMarshaller(
        ModelElement_1_0 feature
    ) throws ServiceException{
        Marshaller marshaller = feature == null ? null : DataObject_1.dataTypeMarshaller.get(
            feature.getModel().getDereferencedType(feature.objGetValue("type")).objGetValue("qualifiedName")
        );
        return marshaller == null ? this.dataObjectManager : marshaller;
    }

    /**
     * Determine the marshaller to be used
     * 
     * @param kind 
     * @param feature
     * @param feature
     * 
     * @return an object or datatype marshaller
     * @throws ServiceException 
     */
    final Marshaller getMarshaller(
        String featured,
        String kind, 
        String feature
    ) throws ServiceException{
        return this.getMarshaller(getFeature(featured, kind, feature));
    }

    /**
     * Retrieve the model element for a given object feature
     * 
     * @param feature
     * 
     * @return the model element for the given feature
     * 
     * @throws ServiceException
     */
    private final ModelElement_1_0 getFeature(
        String feature
    ) throws ServiceException {
        int i = feature.lastIndexOf(':');
        if(i < 0) {
            return this.getFeature(
                objGetClass(), 
                "attribute", 
                feature
            );
        } else {
            this.objRetrieve(
                false, // reload
                this.dataObjectManager.getFetchPlan(), 
                Collections.singleton(feature), false
            );
            Object raw = this.persistentValues.get(
                feature.substring(0, ++i) + SystemAttributes.OBJECT_CLASS
            );
            String embeddedClass = raw instanceof List<?> ?
                (String) ((List<?>)raw).get(0) :
                (String)raw;
            String embeddedFeature = feature.substring(i);
            return SystemAttributes.OBJECT_CLASS.equals(embeddedFeature) ? null : this.getFeature(
                embeddedClass, 
                "attribute",
                embeddedFeature
            );
        }
    }

    /**
     * Determine the marshaller to be used
     * 
     * @param feature
     *  
     * @return an object or datatype marshaller
     * 
     * @throws ServiceException 
     */
    final Marshaller getMarshaller(
        String feature
    ) throws ServiceException{
        return this.getMarshaller(this.getFeature(feature));
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getAspect(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Map<String, DataObject_1_0> getAspect(
        String aspectType
    ){
        Map<String, DataObject_1_0> flushable = (Map<String, DataObject_1_0>) this.flushableValues.get(aspectType);
        return flushable == null ? (Map<String, DataObject_1_0>) Maps.putUnlessPresent(
            this.flushableValues,
            aspectType,
            new ManagedAspect(aspectType)
        ) : flushable;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getFactory()
     */
    public DataObjectManager_1 jdoGetPersistenceManager(
    ) {
        return this.dataObjectManager;
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyFields(java.lang.Object, int[])
     */
    public void jdoCopyFields(Object other, int[] fieldNumbers) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsFromObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldConsumer, java.lang.Object)
     */
    public void jdoCopyKeyFieldsFromObjectId(
        ObjectIdFieldConsumer fm,
        Object oid
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(java.lang.Object)
     */
    public void jdoCopyKeyFieldsToObjectId(Object oid) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldSupplier, java.lang.Object)
     */
    public void jdoCopyKeyFieldsToObjectId(ObjectIdFieldSupplier fm, Object oid) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetVersion()
     */
    public Object jdoGetVersion() {
        return this.digest;
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDetached()
     */
    public boolean jdoIsDetached() {
        return this.detached;
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoMakeDirty(java.lang.String)
     */
    public void jdoMakeDirty(String fieldName) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager)
     */
    public PersistenceCapable jdoNewInstance(StateManager sm) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager, java.lang.Object)
     */
    public PersistenceCapable jdoNewInstance(StateManager sm, Object oid) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance()
     */
    public Object jdoNewObjectIdInstance() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance(java.lang.Object)
     */
    public Object jdoNewObjectIdInstance(Object o) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideField(int)
     */
    public void jdoProvideField(int fieldNumber) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideFields(int[])
     */
    public void jdoProvideFields(int[] fieldNumbers) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceField(int)
     */
    public void jdoReplaceField(int fieldNumber) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFields(int[])
     */
    public void jdoReplaceFields(int[] fieldNumbers) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFlags()
     */
    public void jdoReplaceFlags() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceStateManager(javax.jdo.spi.StateManager)
     */
    public void jdoReplaceStateManager(StateManager sm)
        throws SecurityException {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");        
    }

    /**
     * Create a record
     * 
     * @param recordName the MOF class name
     * 
     * @return a new record
     * 
     * @throws ServiceException 
     */
    @SuppressWarnings("unchecked")
	static <T extends Record> T newRecord(
    	String recordName
    ) throws ServiceException{
    	try {
			return (T) Records.getRecordFactory().createMappedRecord(recordName);
		} catch (ResourceException exception) {
			throw new ServiceException(exception);
		}
    }
    
    protected <T extends Collection<?>> void assertNonNullValue(
    	Object value,
    	Class<T> target
    ) {
    	if(value == null) {
	        throw new JDOUserException(
	            "Null values can't be inserted into such a JMI feature collection",
	            new NullPointerException(target.getSimpleName() + " does not accept null values"),
	            this
	        );
    	}
    }
    
    //--------------------------------------------------------------------------
    // Class ManagedAspect
    //--------------------------------------------------------------------------
    
    /**
     * Managed Aspect
     */
    final class ManagedAspect
        implements Map<String,DataObject_1_0>, Flushable
    {

        ManagedAspect(
            String aspectClass
        ){
            this.aspectClass = aspectClass;
        }

        private final String aspectClass;        
        private transient Container_1_0 standardAspect;
        private Map<String,DataObject_1_0> transientAspect;
        
        private transient Collection<DataObject_1_0> values;
        
        int c = 0;
        
        /**
         * Retrieve the transient or standard delegate.
         * 
         * @return the transient or standard delegate
         */
        Map<String,DataObject_1_0> getDelegate(
        ){
        	try {
				Container_1_0 aspects = DataObject_1.this.objIsContained() ? DataObject_1.this.getAspects() : null;
				if(aspects != null) {
					if(this.standardAspect != null && aspects.container() != this.standardAspect.container()) {
						this.standardAspect = null;
					}
					if(this.standardAspect == null) {
						this.standardAspect = aspects.subMap(
					        new Filter(
    				            new IsInstanceOfCondition(this.aspectClass)
    				        )
    				    );
					}
					if(this.transientAspect != null) {
	        			move(); // just for security
					}
        			return this.standardAspect;
				} else {
		        	if(this.transientAspect == null) {
		    			this.transientAspect = new HashMap<String, DataObject_1_0>();
		        	}				
		        	return this.transientAspect;		        	
				}
			} catch (ServiceException e) {
				throw new RuntimeServiceException(e);
			}
        }

        void move(){
            Map<String, DataObject_1_0> transientAspect = this.transientAspect;
			if(transientAspect != null) {
				this.transientAspect = null;
				ManagedAspect.this.putAll(transientAspect);
            }
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractMap#clear()
         */
        public void clear() {
            this.getDelegate().clear();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#containsKey(java.lang.Object)
         */
        public boolean containsKey(
            Object key
        ) {
            return this.getDelegate().containsKey(key);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#containsValue(java.lang.Object)
         */
        public boolean containsValue(
            Object value
        ) {
            return this.getDelegate().containsValue(value);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#entrySet()
         */
        public Set<java.util.Map.Entry<String, DataObject_1_0>> entrySet(
        ) {
            return this.getDelegate().entrySet();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#equals(java.lang.Object)
         */
        @Override
        public boolean equals(
            Object that
        ) {
            return this == that;
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#get(java.lang.Object)
         */
        public DataObject_1_0 get(
            Object key
        ) {
            return this.getDelegate().get(key);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#hashCode()
         */
        @Override
        public int hashCode(
        ) {
            return this.aspectClass.hashCode();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#isEmpty()
         */
        public boolean isEmpty(
        ) {
            return this.getDelegate().isEmpty();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#keySet()
         */
        public Set<String> keySet(
        ) {
            return this.getDelegate().keySet();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
         */
        public DataObject_1_0 put(
            String key, 
            DataObject_1_0 value
        ) {
            try {
                if(DataObject_1.this.jdoIsPersistent()) {
                    DataObject_1.this.objMakeTransactional();
                }
                //
                // Move the aspect's containers to the core object
                //
                for(
                    Iterator<Map.Entry<String,Flushable>> i = ((DataObject_1)value).flushableValues.entrySet().iterator();
                    i.hasNext();
                ){
                    Map.Entry<String,Flushable> flushableEntry = i.next();
                    Flushable flushable = flushableEntry.getValue();
                    if(flushable instanceof Container_1_0) {
                        Container_1_0 target = DataObject_1.this.objGetContainer(flushableEntry.getKey());
                        Container_1_0 source = (Container_1_0) flushable;
                        for(Map.Entry<String, DataObject_1_0> objectEntry : source.entrySet()) {
                            objectEntry.getValue().objMove(target, objectEntry.getKey());
                        }
                        i.remove();
                    }
                }
                //
                // Save the aspect
                //
                return this.getDelegate().put(
                    DataObject_1.this.getAspects() == null || !DataObject_1.this.isQualified() ? key : this.toObjectId(DataObject_1.this.getQualifier(), key), 
                    value
                );
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            } catch (RuntimeServiceException exception) {
                throw exception;
            }
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#remove(java.lang.Object)
         */
        public DataObject_1_0 remove(
            Object key
        ) {
            return this.getDelegate().remove(key);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#size()
         */
        public int size(
        ) {
            return this.getDelegate().size();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#values()
         */
        public Collection<DataObject_1_0> values(
        ) {
            if(this.values == null) {
                this.values = new Values();
            }
            return this.values;
        }

        /* (non-Javadoc)
         * @see java.io.Flushable#flush()
         */
        public void flush(
        ) throws IOException {
            // Nothing to do at the moment
        }
        
        private String toObjectId(
            String coreId,
            String aspectId
        ){
            return
                aspectId.startsWith(":") ? (":" + coreId + aspectId) :
                aspectId.startsWith("!") ? (coreId + aspectId) :
                (coreId + '*' + aspectId);    
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.transport.delegation.Evictable#evict()
         */
        public void evict() {
        	this.standardAspect = null;
        }

        /* (non-Javadoc)
		 * @see java.util.Map#putAll(java.util.Map)
		 */
		public void putAll(Map<? extends String, ? extends DataObject_1_0> t) {
			for(java.util.Map.Entry<? extends String, ? extends DataObject_1_0> e : t.entrySet()) {
				put(e.getKey(), e.getValue());
			}
		}

        
        //--------------------------------------------------------------------
        // Class Values
        //--------------------------------------------------------------------
        

		/**
         * Values
         */
        class Values implements Collection<DataObject_1_0> {

            private final Collection<DataObject_1_0> getDelegate(){
                return ManagedAspect.this.getDelegate().values();
            }

            /**
             * @param o
             * 
             * @return <code>true</code> if the collection had to be modified
             * 
             * @see java.util.Collection#add(java.lang.Object)
             */
            public boolean add(
                DataObject_1_0 o
            ) {
                boolean modify = !ManagedAspect.this.containsValue(o);
                if(modify) {
                    ManagedAspect.this.put(
                        PathComponent.createPlaceHolder().toString(),
                        o
                    );
                }
                return modify;
            }

            /**
             * @param c
             * @return
             * @see java.util.Collection#addAll(java.util.Collection)
             */
            public boolean addAll(Collection<? extends DataObject_1_0> c) {
                boolean modified = false;
                for(DataObject_1_0 o : c) {
                    modified |= this.add(o);
                }
                return modified;
            }

            /**
             * 
             * @see java.util.Collection#clear()
             */
            public void clear() {
                ManagedAspect.this.getDelegate().clear();
            }

            /**
             * @param o
             * @return
             * @see java.util.Collection#contains(java.lang.Object)
             */
            public boolean contains(Object o) {
                return ManagedAspect.this.getDelegate().containsValue(o);
            }

            /**
             * @param c
             * @return
             * @see java.util.Collection#containsAll(java.util.Collection)
             */
            public boolean containsAll(Collection<?> c) {
                return this.getDelegate().containsAll(c);
            }

            /**
             * @return
             * @see java.util.Collection#isEmpty()
             */
            public boolean isEmpty() {
                return ManagedAspect.this.getDelegate().isEmpty();
            }

            /**
             * @return
             * @see java.util.Collection#iterator()
             */
            public Iterator<DataObject_1_0> iterator() {
                return this.getDelegate().iterator();
            }

            /**
             * @param o
             * @return
             * @see java.util.Collection#remove(java.lang.Object)
             */
            public boolean remove(Object o) {
                return this.getDelegate().remove(o);
            }

            /**
             * @param c
             * @return
             * @see java.util.Collection#removeAll(java.util.Collection)
             */
            public boolean removeAll(Collection<?> c) {
                return this.getDelegate().removeAll(c);
            }

            /**
             * @param c
             * @return
             * @see java.util.Collection#retainAll(java.util.Collection)
             */
            public boolean retainAll(Collection<?> c) {
                return this.getDelegate().retainAll(c);
            }

            /**
             * @return
             * @see java.util.Collection#size()
             */
            public int size() {
                return ManagedAspect.this.getDelegate().size();
            }

            /**
             * @return
             * @see java.util.Collection#toArray()
             */
            public Object[] toArray() {
                return this.getDelegate().toArray();
            }

            /**
             * @param <T>
             * @param a
             * @return
             * @see java.util.Collection#toArray(T[])
             */
            public <T> T[] toArray(T[] a) {
                return this.getDelegate().toArray(a);
            }
                        
        }
                
    }

        
    //--------------------------------------------------------------------------
    // Class ManagedList
    //--------------------------------------------------------------------------

    /**
     * The managed list delegates<ul>
     * <li>to a unit of work local copy if the object is transactional
     * <li>to the persistent values if the object is non-transactional
     * </ul>
     */
    final class ManagedList
        extends AbstractList<Object>
        implements Flushable
    {

        /**
         * Constructor 
         *
         * @param feature
         * @param marshaller 
         * 
         * @throws ServiceException
         */
        ManagedList(
            String feature,
            Marshaller marshaller
        ) throws ServiceException {
            this.feature = feature;
            this.nonTransactional = new NonTransactional(marshaller);
        }

        /**
         * 
         */
        final String feature;

        /**
         * 
         */
        private final List<Object> nonTransactional;

        @SuppressWarnings("unchecked")
        private List<Object> getDelegate(
            boolean makeDirty, 
            boolean clear
        ){
            try {
                UnitOfWork_1 unitOfWork = DataObject_1.this.getUnitOfWorkIfTransactional(makeDirty ? this.feature : null);
                if(unitOfWork == null) {
                    if(DataObject_1.this.transientValues != null) {
                        List<Object> transientValue = (List<Object>) DataObject_1.this.transientValues.get(this.feature);
                        if(makeDirty && transientValue == null) {
                            transientValues.put(
                                this.feature,
                                transientValue = new ArrayList<Object>()
                            );
                            return transientValue;
                        }
                        if(transientValue != null) {
                            if(clear) {
                                transientValue.clear();
                            }
                            return transientValue;
                        }
                    }
                    if(clear) {
                        this.nonTransactional.clear();
                    }
                    return this.nonTransactional;
                } else {
                    Map<String,Object> transactionalValues = unitOfWork.getState(DataObject_1.this, false).values(false); 
                    List<Object> transactionalValue = (List<Object>) transactionalValues.get(this.feature);
                    if(transactionalValue == null) {
                        transactionalValues.put(
                            this.feature,
                            transactionalValue = clear ? new ArrayList<Object>() : new ArrayList<Object>(this.nonTransactional)
                        );
                    } else if (clear) {
                        transactionalValue.clear();
                    }
                    return transactionalValue;
                }
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        @Override
        public Object get(
            int index
        ) {
            return this.getDelegate(false, false).get(index);
        }

        @Override
        public int size(
        ){
            return this.getDelegate(false, false).size();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return this.getDelegate(false, false).isEmpty();
        }

        @Override
        public Object set(
            int index,
            Object element
        ){
        	assertNonNullValue(element, List.class);
            return this.getDelegate(true, false).set(index, element);
        }

        @Override
        public void add(
            int index,
            Object element
        ){
        	assertNonNullValue(element, List.class);
            this.getDelegate(true, false).add(index, element);
        }

        @Override
        public Object remove(
            int index
        ){
            return this.getDelegate(true, false).remove(index);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractList#clear()
         */
        @Override
        public void clear() {
            this.getDelegate(true, true);
        }

        /* (non-Javadoc)
         * @see java.io.Flushable#flush()
         */
        public void flush() throws IOException {
            List<Object> source = this.getDelegate(false, false);
            if(source != this.nonTransactional) {
                this.nonTransactional.clear();
                this.nonTransactional.addAll(source);
            }
        }

        /**
         * Persistent Values Accessor
         */
        class NonTransactional extends MarshallingList<Object> {

            /**
             * Constructor 
             * 
             * @param marshaller 
             *
             * @throws ServiceException 
             */
            NonTransactional(
                Marshaller marshaller
            ) throws ServiceException {
                super(
                    marshaller, 
                    null // delegate provided by getDelegate() method
                );
            }

            /**
             * Implements <code>Serializable</code>
             */
            private static final long serialVersionUID = -5790864813300543661L;

            /* (non-Javadoc)
             * @see org.openmdx.base.collection.MarshallingList#getDelegate()
             */
            @Override
            protected List<?> getDelegate(
            ) {
                try {
                    return (List<?>)DataObject_1.this.getPersistentAttribute(
                        ManagedList.this.feature, 
                        false,
                        Multiplicity.LIST,
                        false
                    );
                } catch(ServiceException e) {
                    throw new RuntimeServiceException(e);
                }
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.collection.MarshallingList#clear()
             */
            @Override
            public void clear(
            ) {
                try {
                    DataObject_1.this.getPersistentAttribute(
                        ManagedList.this.feature, 
                        false,
                        Multiplicity.LIST,
                        true
                    );
                } catch(ServiceException e) {
                    throw new RuntimeServiceException(e);
                }
            }

        }

    }


    //------------------------------------------------------------------------
    // Class ManagedSet
    //------------------------------------------------------------------------

    /**
     * The managed set delegates<ul>
     * <li>to a unit of work local copy if the object is transactional
     * <li>to the persistent values if the object is non-transactional
     * </ul>
     */
    final class ManagedSet
        extends AbstractSet<Object>
        implements Flushable
    {

        /**
         * Constructor 
         *
         * @param feature
         * @param transactional
         * @param marshaller 
         * @throws ServiceException
         */
        ManagedSet(
            String feature,
            Marshaller marshaller
        ) throws ServiceException {
            this.feature = feature;
            this.nonTransactional = new NonTransactional(marshaller);
        }

        /**
         * 
         */
        final String feature;

        /**
         * 
         */
        private final Set<Object> nonTransactional;

        @SuppressWarnings("unchecked")
        protected Set<Object> getDelegate(
            boolean makeDirty, 
            boolean clear
        ){
            try {
                UnitOfWork_1 unitOfWork = DataObject_1.this.getUnitOfWorkIfTransactional(makeDirty ? this.feature : null);
                if(unitOfWork == null) {
                    if(DataObject_1.this.transientValues != null) {
                        Set<Object> transientValue = (Set<Object>) DataObject_1.this.transientValues.get(this.feature);
                        if(makeDirty && transientValue == null) {
                            transientValues.put(
                                this.feature,
                                transientValue = new HashSet<Object>()
                            );
                            return transientValue;
                        }
                        if(transientValue != null) {
                            if(clear) {
                                transientValue.clear();
                            }
                            return transientValue;
                        }
                    }
                    if(clear) {
                        this.nonTransactional.clear();
                    }
                    return this.nonTransactional;
                } 
                else {
                    Map<String,Object> transactionalValues = unitOfWork.getState(DataObject_1.this, false).values(false); 
                    Set<Object> transactionalValue = (Set<Object>)transactionalValues.get(this.feature);
                    if(transactionalValue == null) {
                        transactionalValues.put(
                            this.feature,
                            transactionalValue = clear ? new HashSet<Object>() : new HashSet<Object>(this.nonTransactional)
                        );
                    } else if (clear) {
                        transactionalValue.clear();
                    }
                    return transactionalValue;
                }
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<Object> iterator() {
            Set<Object> delegate = this.getDelegate(false, false);
            return new SetIterator(
                delegate.iterator(),
                delegate != this.nonTransactional
            );
        }

        @Override
        public void clear(
        ) {
            this.getDelegate(true, true);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size(
        ) {
            return this.getDelegate(false, false).size();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return this.getDelegate(false, false).isEmpty();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#add(java.lang.Object)
         */
        @Override
        public boolean add(Object o) {
        	assertNonNullValue(o, Set.class);
            return this.getDelegate(true, false).add(o);
        }

        /* (non-Javadoc)
         * @see java.io.Flushable#flush()
         */
        public void flush() throws IOException {
            Set<Object> source = this.getDelegate(false, false);
            if(source != this.nonTransactional) {
                this.nonTransactional.clear();
                this.nonTransactional.addAll(source);
            }
        }

        /**
         * Persistent Values Accessor
         */
        class NonTransactional extends MarshallingSet<Object> {

            /**
             * Constructor 
             * 
             * @param marshaller 
             *
             * @throws ServiceException 
             */
            NonTransactional(
                Marshaller marshaller
            ) throws ServiceException {
                super(
                    marshaller, 
                    null // delegate provided by getDelegate() method
                );
            }

            /**
             * Implements <code>Serializable</code>
             */
            private static final long serialVersionUID = -5790864813300543661L;

            /* (non-Javadoc)
             * @see org.openmdx.base.collection.MarshallingCollection#getDelegate()
             */
            @Override
            protected Collection<?> getDelegate(
            ) {
                try {
                    return (Collection<?>)DataObject_1.this.getPersistentAttribute(
                        ManagedSet.this.feature, 
                        false,
                        Multiplicity.LIST,
                        false
                    );
                } catch(ServiceException e) {
                    throw new RuntimeServiceException(e);
                }
            }

            /* (non-Javadoc)
             * @see org.openmdx.base.collection.MarshallingCollection#clear()
             */
            @Override
            public void clear(
            ) {
                try {
                    DataObject_1.this.getPersistentAttribute(
                        ManagedSet.this.feature, 
                        false,
                        Multiplicity.LIST,
                        true
                    );
                } catch(ServiceException e) {
                    throw new RuntimeServiceException(e);
                }
            }

        }

        /**
         * 
         */
        class SetIterator implements Iterator<Object> {

            SetIterator(
                Iterator<?> delegate,
                boolean transactional
            ){
                this.delegate = delegate;
                this.transactional = transactional;
            }

            /**
             * 
             */
            private final Iterator<?> delegate;

            /**
             * 
             */
            private final boolean transactional;

            /**
             * 
             */
            private Object current = null;

            /* (non-Javadoc)
             * @see java.util.Iterator#hasNext()
             */
            public boolean hasNext() {
                return this.delegate.hasNext();
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#next()
             */
            public Object next() {
                return this.current = this.delegate.next();
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#remove()
             */
            public void remove(
            ) {
                if(this.transactional) {
                    this.delegate.remove();
                } 
                else if(this.current == null){
                    throw new IllegalStateException(
                        "There is no current element to be removed"
                    );
                } 
                else {
                    ManagedSet.this.getDelegate(true, false).remove(this.current);
                    this.current = null;
                }
            }

        }

    }
    

    //------------------------------------------------------------------------
    // Class ManagedSortedMap
    //------------------------------------------------------------------------

    /**
     * The managed sparse array delegates<ul>
     * <li>to a unit of work local copy if the object is transactional
     * <li>to the persistent values if the object is non-transactional
     * </ul>
     */
    final class ManagedSortedMap
        implements SortedMap<Integer,Object>, Flushable
    {

        /**
         * Constructor 
         *
         * @param feature
         * @param transactional
         * @param marshaller 
         * 
         * @throws ServiceException
         */
        ManagedSortedMap(
            final String feature,
            Marshaller marshaller
        ) throws ServiceException {
            this.feature = feature;
            this.nonTransactional = new MarshallingSortedMap(
                marshaller,
                new PopulationMap<Object>(){

                    /* (non-Javadoc)
                     * @see org.openmdx.compatibility.base.collection.PopulationMap#getDelegate()
                     */
                    @SuppressWarnings("unchecked")
                    @Override
                    protected SparseArray<Object> getDelegate(
                    ) {
                        try {
                            return (SparseArray<Object>)DataObject_1.this.getPersistentAttribute(
                                ManagedSortedMap.this.feature,
                                false,
                                Multiplicity.SPARSEARRAY,
                                false
                            );
                        } catch(ServiceException e) {
                            throw new RuntimeServiceException(e);
                        }
                    }

                    /* (non-Javadoc)
                     * @see java.util.AbstractMap#clear()
                     */
                    @Override
                    public void clear(
                    ) {
                        try {
                            DataObject_1.this.getPersistentAttribute(
                                ManagedSortedMap.this.feature,
                                false,
                                Multiplicity.SPARSEARRAY,
                                true
                            );
                        } catch(ServiceException e) {
                            throw new RuntimeServiceException(e);
                        }
                    }

                }
            );
        }        
        
        /**
         * 
         */
        final String feature;

        /**
         * 
         */
        private final SortedMap<Integer,Object> nonTransactional;

        private final Set<Map.Entry<Integer,Object>> entries = new AbstractSet<Map.Entry<Integer,Object>>(){

            @Override
            public Iterator<Map.Entry<Integer, Object>> iterator() {
                return new EntryIterator();
            }

            @Override
            public int size() {
                return getDelegate(false,false).size();
            }
            
        };
        
        @SuppressWarnings("unchecked")
        protected SortedMap<Integer,Object> getDelegate(
            boolean makeDirty, 
            boolean clear
        ){
            try {
                UnitOfWork_1 unitOfWork = DataObject_1.this.getUnitOfWorkIfTransactional(makeDirty ? this.feature : null);
                if(unitOfWork == null) {
                    if(DataObject_1.this.transientValues != null) {
                        SortedMap<Integer,Object> transientValue = (SortedMap<Integer,Object>) transientValues.get(this.feature);
                        if(makeDirty && transientValue == null) {
                            DataObject_1.this.transientValues.put(
                                this.feature,
                                transientValue = new TreeMap<Integer,Object>()
                            );
                            return transientValue;
                        }
                        if(transientValue != null) {
                            if(clear) {
                                transientValue.clear();
                            }
                            return transientValue;
                        }
                    }
                    if(clear) {
                        this.nonTransactional.clear();
                    }
                    return this.nonTransactional;
                } else {
                    Map<String,Object> transactionalValues = unitOfWork.getState(DataObject_1.this, false).values(false); 
                    SortedMap<Integer,Object> transactionalValue = (SortedMap<Integer, Object>)transactionalValues.get(this.feature);
                    if(transactionalValue == null) {
                        transactionalValues.put(
                            this.feature,
                            transactionalValue = clear ? new TreeMap<Integer,Object>() : new TreeMap<Integer,Object>(this.nonTransactional)
                        );
                    } else if (clear) {
                        transactionalValue.clear();
                    }
                    return transactionalValue;
                }
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }
        
        /* (non-Javadoc)
         * @see java.io.Flushable#flush()
         */
        public void flush() throws IOException {
            SortedMap<Integer,Object> source = ManagedSortedMap.this.getDelegate(false, false);
            if(source != this.nonTransactional) {
                this.nonTransactional.clear();
                this.nonTransactional.putAll(source);
            }
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#entrySet()
         */
        public Set<java.util.Map.Entry<Integer, Object>> entrySet() {
            return this.entries;
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#comparator()
         */
        public Comparator<? super Integer> comparator() {
            return null;
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#firstKey()
         */
        public Integer firstKey() {
            return ManagedSortedMap.this.getDelegate(false,false).firstKey();
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#headMap(java.lang.Object)
         */
        public SortedMap<Integer, Object> headMap(Integer toKey) {
            return new SubMap(null, toKey);
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#lastKey()
         */
        public Integer lastKey() {
            return ManagedSortedMap.this.getDelegate(false,false).lastKey();
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#subMap(java.lang.Object, java.lang.Object)
         */
        public SortedMap<Integer, Object> subMap(Integer fromKey, Integer toKey) {
            return new SubMap(fromKey, toKey);
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#tailMap(java.lang.Object)
         */
        public SortedMap<Integer, Object> tailMap(Integer fromKey) {
            return new SubMap(fromKey, null);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#clear()
         */
        public void clear() {
            ManagedSortedMap.this.getDelegate(true,true);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#containsKey(java.lang.Object)
         */
        public boolean containsKey(Object key) {
            return ManagedSortedMap.this.getDelegate(false,false).containsKey(key);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#containsValue(java.lang.Object)
         */
        public boolean containsValue(Object value) {
            return ManagedSortedMap.this.getDelegate(false,false).containsValue(value);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#get(java.lang.Object)
         */
        public Object get(Object key) {
            return ManagedSortedMap.this.getDelegate(false,false).get(key);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#isEmpty()
         */
        public boolean isEmpty() {
            return ManagedSortedMap.this.getDelegate(false,false).isEmpty();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#keySet()
         */
        public Set<Integer> keySet() {
            return ManagedSortedMap.this.getDelegate(false,false).keySet(); // TOD make it modifiable
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
         */
        public Object put(Integer key, Object value) {
            return ManagedSortedMap.this.getDelegate(true,false).put(key, value);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#putAll(java.util.Map)
         */
        public void putAll(Map<? extends Integer, ? extends Object> t) {
            ManagedSortedMap.this.getDelegate(true,false).putAll(t);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#remove(java.lang.Object)
         */
        public Object remove(Object key) {
            return ManagedSortedMap.this.getDelegate(true,false).remove(key);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#size()
         */
        public int size() {
            return ManagedSortedMap.this.getDelegate(false,false).size();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#values()
         */
        public Collection<Object> values() {
            return ManagedSortedMap.this.getDelegate(false,false).values();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            return ManagedSortedMap.this.getDelegate(false,false).equals(obj);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return ManagedSortedMap.this.getDelegate(false,false).hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return ManagedSortedMap.this.getDelegate(false,false).toString();
        }

        /**
         * Head, tail and sub-mapes
         */
        class SubMap 
            extends AbstractMap<Integer,Object>
            implements SortedMap<Integer,Object>
        {
            
            /**
             * Constructor 
             *
             * @param from
             * @param to
             */
            SubMap(
                Integer from,
                Integer to
            ){
                this.from = from;
                this.to = to;
            }

            /**
             * The from-key, or <code>null</code> in case of a head-map
             */
            private final Integer from;
            
            /**                anotherPersistenceManager.currentTransaction().begin();
                for(Map.Entry<Integer,String> e : anotherInfo.entrySet()) {
                    e.setValue(e.getKey().toString());
                }
                anotherPersistenceManager.currentTransaction().commit();

             * The to-key, or <code>null</code> in case of a tail-map
             */
            private final Integer to;

            /**
             * Assert that the key is inside the sub-map's range
             * 
             * @param key the key to be tested
             */
            private void validateKey(
                int key
            ){
                if(
                    (this.from != null && this.from > key) ||
                    (this.to != null && this.to < key)
                ){
                    throw BasicException.initHolder(
                        new IllegalArgumentException(
                            "Key outside the sub-map's range",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_PARAMETER,
                                new BasicException.Parameter("lower-bound", this.from),
                                new BasicException.Parameter("key", key),
                                new BasicException.Parameter("upper-bound", this.to)
                            )
                        )
                    );
                }
            }
            
            /**
             * Retrieve the delegate sub-map
             * 
             * @return the delegate sub-map
             */
            protected SortedMap<Integer,Object> getSubMap(){
                SortedMap<Integer,Object> map = ManagedSortedMap.this.getDelegate(false,false);
                if(from == null) {
                    if(to == null) {
                        return map;
                    } else {
                        return map.headMap(to);
                    }
                } else {
                    if(to == null) {
                        return map.tailMap(from);
                    } else {
                        return map.subMap(from, to);
                    }
                }
            }
            
            @Override
            public int size() {
                return this.getSubMap().size();
            }

            /* (non-Javadoc)
             * @see java.util.AbstractMap#entrySet()
             */
            @Override
            public Set<java.util.Map.Entry<Integer, Object>> entrySet() {
                return this.getSubMap().entrySet();
            }

            /* (non-Javadoc)
             * @see java.util.SortedMap#comparator()
             */
            public Comparator<? super Integer> comparator() {
                return null;
            }

            /* (non-Javadoc)
             * @see java.util.SortedMap#firstKey()
             */
            public Integer firstKey() {
                return this.getSubMap().firstKey();
            }

            /* (non-Javadoc)
             * @see java.util.SortedMap#headMap(java.lang.Object)
             */
            public SortedMap<Integer, Object> headMap(Integer toKey) {
                this.validateKey(toKey);
                return new SubMap(this.from, toKey);
            }

            /* (non-Javadoc)
             * @see java.util.SortedMap#lastKey()
             */
            public Integer lastKey() {
                return this.getSubMap().lastKey();
            }

            /* (non-Javadoc)
             * @see java.util.SortedMap#subMap(java.lang.Object, java.lang.Object)
             */
            public SortedMap<Integer, Object> subMap(
                Integer fromKey,
                Integer toKey
            ) {
                this.validateKey(fromKey);
                this.validateKey(toKey);
                return new SubMap(fromKey, toKey);
            }

            /* (non-Javadoc)
             * @see java.util.SortedMap#tailMap(java.lang.Object)
             */
            public SortedMap<Integer, Object> tailMap(Integer fromKey) {
                this.validateKey(fromKey);
                return new SubMap(fromKey, null);
            }
            
        };
        
        /**
         * Entry Iterator
         */
        class EntryIterator implements Iterator<Map.Entry<Integer, Object>> {
            
            private SortedMap<Integer,Object> readOnly = ManagedSortedMap.this.getDelegate(false,false);
            
            private Iterator<Map.Entry<Integer, Object>> delegate = readOnly.entrySet().iterator();

            protected Map.Entry<Integer, Object> current = null;
            
            protected void makeDirty(){
                if(this.readOnly != null) {
                    SortedMap<Integer,Object> forUpdate = ManagedSortedMap.this.getDelegate(true,false);
                    SortedMap<Integer,Object> readOnly = this.readOnly;
                    this.readOnly = null;
                    if(forUpdate != readOnly) {
                        this.delegate = forUpdate.entrySet().iterator();
                        while(this.delegate.hasNext()){
                            Map.Entry<Integer, Object> candidate = this.delegate.next();
                            if(this.current.getKey().equals(candidate.getKey())) {
                                this.current = candidate;
                                return;
                            }
                        }
                        throw new ConcurrentModificationException(
                            "The read-only iterators key can't be reached by the for-update iterator: " + this.current.getKey()
                        );
                    }
                }
            }
            
            /* (non-Javadoc)
             * @see java.util.Iterator#hasNext()
             */
            public boolean hasNext() {
                return this.delegate.hasNext();
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#next()
             */
            public Map.Entry<Integer, Object> next() {
                this.current = this.delegate.next();
                return new Map.Entry<Integer, Object>() {

                    public Integer getKey() {
                        return EntryIterator.this.current.getKey();
                    }

                    public Object getValue() {
                        return EntryIterator.this.current.getValue();
                    }

                    public Object setValue(Object value) {
                        EntryIterator.this.makeDirty();
                        return EntryIterator.this.current.setValue(value);
                    }
                    
                };
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#remove()
             */
            public void remove() {
                EntryIterator.this.makeDirty();
                this.delegate.remove();
            }
        
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class Operation
    //------------------------------------------------------------------------

    /**
     * Operation
     */
    final class Operation {

        /**
         * Constructor 
         *
         * @param functionName
         * @param input
         * @param output
         */
        Operation(
            String functionName,
            MappedRecord input,
            MappedRecord output
        ){
            this.operation = functionName;
            this.input = input;
            this.output = output;
        }

        /**
         * The operation name
         */
        private final String operation;

        /**
         * The operation request
         */
        private final MappedRecord input;

        /**
         * The operation response
         */
        private final MappedRecord output;

        /**
         * Invoke the operation
         * 
         * @throws ServiceException
         */
        @SuppressWarnings("unchecked")
        void invoke(
            Interaction interaction
        ) throws ServiceException {
            try {
                MessageRecord input = DataObject_1.newRecord(MessageRecord.NAME);
                input.setPath(identity.getDescendant(this.operation, UUIDs.newUUID().toString()));
                input.setBody(this.input);
                MessageRecord replies = (MessageRecord) interaction.execute(
                    jdoGetPersistenceManager().getInteractionSpecs().INVOKE,
                    input
                );
                this.output.putAll(replies.getBody());
            } catch (ResourceException exception) {
                throw new ServiceException(exception);
            }
        }

    }
    

    static {
        //
        // dataTypeMarshaller initalization
        //
        DataObject_1.dataTypeMarshaller.put(DATE, DateMarshaller.NORMALIZE);
        DataObject_1.dataTypeMarshaller.put(DATETIME, DateTimeMarshaller.NORMALIZE);
        DataObject_1.dataTypeMarshaller.put(DURATION, DurationMarshaller.NORMALIZE);
        DataObject_1.dataTypeMarshaller.put(SHORT, ShortMarshaller.NORMALIZE);
        DataObject_1.dataTypeMarshaller.put(INTEGER, IntegerMarshaller.NORMALIZE);
        DataObject_1.dataTypeMarshaller.put(LONG, LongMarshaller.NORMALIZE);
        //
        // lifecycleListenerClass initalization
        //
        DataObject_1.lifecycleListenerClass[InstanceLifecycleEvent.CREATE] = CreateLifecycleListener.class;
        DataObject_1.lifecycleListenerClass[InstanceLifecycleEvent.LOAD] = LoadLifecycleListener.class;
        DataObject_1.lifecycleListenerClass[InstanceLifecycleEvent.STORE] = StoreLifecycleListener.class;
        DataObject_1.lifecycleListenerClass[InstanceLifecycleEvent.CLEAR] = ClearLifecycleListener.class;
        DataObject_1.lifecycleListenerClass[InstanceLifecycleEvent.DELETE] = DeleteLifecycleListener.class;
        DataObject_1.lifecycleListenerClass[InstanceLifecycleEvent.DIRTY] = DirtyLifecycleListener.class;
        DataObject_1.lifecycleListenerClass[InstanceLifecycleEvent.DETACH] = DetachLifecycleListener.class;
        DataObject_1.lifecycleListenerClass[InstanceLifecycleEvent.ATTACH] = AttachLifecycleListener.class;

    }

}
