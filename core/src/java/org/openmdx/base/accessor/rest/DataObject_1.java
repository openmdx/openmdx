/*
 * ====================================================================
 * Description: Object_1 class
 * Revision:    $Revision: 1.88 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/23 09:11:35 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
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

import javax.jdo.JDOException;
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

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.rest.spi.VirtualObjects_2_0;
import org.openmdx.base.accessor.spi.AbstractDataObject_1;
import org.openmdx.base.accessor.spi.DateMarshaller;
import org.openmdx.base.accessor.spi.DateTimeMarshaller;
import org.openmdx.base.accessor.spi.DurationMarshaller;
import org.openmdx.base.accessor.spi.ExceptionHelper;
import org.openmdx.base.accessor.spi.IntegerMarshaller;
import org.openmdx.base.accessor.spi.LongMarshaller;
import org.openmdx.base.accessor.spi.ShortMarshaller;
import org.openmdx.base.aop0.PlugIn_1_0;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.collection.PopulationMap;
import org.openmdx.base.collection.TreeSparseArray;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.spi.ModelUtils;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.PathComponent;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.base.query.AttributeSpecifier;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.MethodInvocationSpec;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.base.text.conversion.JavaBeans;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.CharacterLargeObjects;
import org.w3c.cci2.LargeObject;
import org.w3c.cci2.SparseArray;

/**
 * DataObject_1_0 implementation
 */
public class DataObject_1 
    implements DataObject_1_0, Serializable, Evictable 
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
        this.digest = version;
    }
    
    /**
     * Constructor 
     *
     * @param manager
     * @param identity
     * @param transientObjectId 
     * @throws ServiceException  
     */
    private DataObject_1(
        DataObjectManager_1 dataObjectManager,
        Path identity, 
        UUID transientObjectId
    ) throws ServiceException {
        this.dataObjectManager = dataObjectManager;
        this.identity = identity;
        this.transientObjectId = transientObjectId == null ? UUIDs.newUUID() : transientObjectId;
        this.transientValues = identity == null ? new HashMap<String,Object>() : null;
        this.detached = false;
        dataObjectManager.putIfAbsent(
            this.transientObjectId,  
            this
        );
    }
    
    /**
     * Constructor 
     * 
     * @param manager
     * @param identity 
     * @param transientObjectId 
     * @param objectClass
     * 
     * @throws ServiceException
     */
    DataObject_1(
        DataObjectManager_1 manager,
        Path identity, 
        UUID transientObjectId, 
        String objectClass
    ) throws ServiceException{
        this(
            manager,
            identity, 
            transientObjectId
        );
        if(objectClass == null && identity == null) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "The identity is required for persisten objects, the object class for transient ones"
        );
        this.transactionalValuesRecordName = objectClass;
        if(identity != null) {
            manager.putIfAbsent(identity, this);
        }
    }

    /**
     * Constructor
     * 
     * @param that
     * @param beforeImage <code>true</code> if the before-image shall be cloned
     * @param identity 

     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    private DataObject_1(
        DataObject_1 that, 
        boolean beforeImage, 
        Path identity
    ) throws ServiceException{
        this(
            that.dataObjectManager,
            identity,
            null, // transientObjectId 
            getRecordName(that)
        );
        this.digest = null;
        // 
        // Persistent Values
        //
        try {
            this.persistentValues = Records.getRecordFactory().createMappedRecord(this.transactionalValuesRecordName);
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
        }
        if(!that.objIsHollow()) {
            this.persistentValues.putAll(that.persistentValues);
        }
        TransactionalState_1 thisState = this.getState(false);
        Set<String> thisDirty;
        if(beforeImage){
            //
            // Before Image Only
            //
            if(identity == null) {
                thisDirty = thisState.dirtyFeatures(false);
                thisDirty.addAll(that.persistentValues.keySet());
            } else {
                thisDirty = null;
            }
        } else {
            thisDirty = thisState.dirtyFeatures(false);
            //
            // Transactional Values
            //
            TransactionalState_1 thatState = that.getState(true);
            if(thatState != null) {
                for(Map.Entry<String,Object> e : thatState.values(true).entrySet()){
                    String feature = e.getKey();
                    Object candidate = e.getValue();
                    if(candidate instanceof Set) {
                        Set<Object> target = this.objGetSet(feature);
                        target.clear();
                        target.addAll((Set)candidate);
                    } else if (candidate instanceof List) {
                        List<Object> target = this.objGetList(feature);
                        target.clear();
                        target.addAll((List)candidate);
                    } else if (candidate instanceof SortedMap) {
                        SortedMap<Integer,Object> target = this.objGetSparseArray(feature);
                        target.clear();
                        target.putAll((SortedMap)candidate);
                    } else {
                        this.objSetValue(feature, candidate);
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
                this.transientValues.putAll(that.transientValues);
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
     *
     */
    protected transient MappedRecord persistentValues = null;

    /**
    *
    */
    protected Map<String,Object> transientValues;
    
    /**
     * @serial
     */
    protected Object digest = null;

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
     * The object's classifier
     */
    private transient ModelElement_1_0 classifier;

    /**
     * 
     */
    static final Map<String, DataObject_1_0> NO_ASPECT = Collections.emptyMap();
    
    /**
     * 
     */
    private static final FilterProperty[] NO_FILTER = {};

    /**
     * Map a data types's qualified name to its marshaller
     */
    static final Map<String,Marshaller> dataTypeMarshaller = new HashMap<String,Marshaller>();
    
    /**
     * Valdidate the object's state and retrieve its class
     * 
     * @param that
     * 
     * @return the object's class
     * 
     * @throws ServiceException
     */
    private static String getRecordName(
        DataObject_1 that
    ) throws ServiceException {
        if(that.transactionalValuesRecordName == null && that.objIsHollow()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "The source object is hollow"
            );
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
        return left == null ? right == null : left.equals(right);
    }
    
    /**
     * Add a new value unless it has already been set
     * 
     * @param key the feature name
     * @param value the new value to be used if no previous value is set
     * @return the previous value if one exists, the new value otherwise
     */
    @SuppressWarnings("unchecked")
    private final <T extends Flushable> T putFlushable(
        String key,
        T value
    ){
        T concurrent = (T) this.flushableValues.putIfAbsent(
            key,
            value
        );
        return concurrent == null ? value : concurrent;
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
            return getUnitOfWork().getState(this,optional);
        }        
    }

    @SuppressWarnings("unchecked")
    void setExistence(
        Path identity,
        boolean existence
    ) throws ServiceException{
        UnitOfWork_1 unitOfWork = getUnitOfWork();
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
                assertObjectIsAccessible(false);
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
        TransactionalState_1 state = getState(true);
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
    DataObject_1 postLoad(
        MappedRecord objectHolder
    ) throws ServiceException {
        try {
            Object_2Facade facade = Object_2Facade.newInstance(objectHolder);
            if(this.identity == null || this.identity.equals(facade.getPath())) {
                //
                // Composite & Transient
                //
                if(this.persistentValues == null) {
                    this.persistentValues = facade.getValue();
                    this.digest = facade.getVersion();
                } else {
                    MappedRecord source = facade.getValue();
                    MappedRecord persistentValues = Records.getRecordFactory().createMappedRecord(source.getRecordName());
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
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
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
            getUnitOfWork().remove(this);
        } catch (Exception ignored) {
            // Just ignore it
        }
        clear();
        this.digest = null;
        this.persistentValues = null;
        this.dataObjectManager = null;
        this.created = false;
        this.identity = null;
    }

    /**
     * Ask the persistence framework for the object's content
     */
    DataObject_1 unconditionalLoad(
    ) throws ServiceException {
        try {
            IndexedRecord reply = (IndexedRecord) this.dataObjectManager.getInteraction().execute(
                this.dataObjectManager.getInteractionSpecs().GET,
                Records.getRecordFactory().singletonIndexedRecord(
                    Multiplicities.LIST, 
                    null, 
                    this.jdoIsPersistent() ? this.jdoGetObjectId() : this.jdoGetTransactionalObjectId()
                )
            );
            DataObject_1 composite = postLoad((MappedRecord) reply.get(0));
            this.inaccessabilityReason = null;
            return composite;
        } catch (ResourceException exception) {
            throw setInaccessibilityReason(new ServiceException(exception));
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
    
    void unconditionalEvict(){
        TransactionalState_1 state = getState(true);
        if(state != null) {
            state.clear();
        }
        this.digest = null;
        this.persistentValues = null;
    }
    
    /**
     * Retrieve this object's container
     *
     * @param forEviction 
     * 
     * @return the container
     * @throws ServiceException 
     * 
     * @throws ServiceException 
     */
    Container_1 getContainer(
        boolean forEviction
    ) throws ServiceException{
        if(this.container == null && this.jdoIsPersistent()) {
            Path identity = jdoGetObjectId();
            int size = identity.size() - 2;
            if(size > 0) {
                Path parentId = identity.getPrefix(size);
                if(!forEviction || this.dataObjectManager.containsKey(parentId)) {
                    DataObject_1_0 parent = this.dataObjectManager.getObjectById(parentId, false); 
                    if(!parent.objIsInaccessible()) {
                        this.container = (Container_1) parent.objGetContainer(
                            identity.get(size)
                        );
                    }
                }
            }
        }
        return this.container;
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
        if(this.aspects == null) {
            Container_1 container = getContainer(false);
            if(container != null) {
                this.aspects = container.subMap(
                    new FilterProperty[]{
                        new FilterProperty(
                            Quantors.THERE_EXISTS,
                            "core",
                            FilterOperators.IS_IN,
                            this.jdoIsPersistent() ? this.jdoGetObjectId() : this.jdoGetTransactionalObjectId()
                        )
                    }
                );
            }
        }
        return this.aspects;
    }

    String getQualifier(
    ){
        return jdoIsPersistent() ? jdoGetObjectId().getBase() : this.qualifier;
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
        boolean transactional = makeDirty != null && jdoIsPersistent();
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
        addTo(getUnitOfWork()); 
    }

    /**
     * Add to unit of work
     * 
     * @param unitOfWork
     * @throws ServiceException 
     */
    private final void addTo(
        UnitOfWork_1 unitOfWork
    ) throws ServiceException{
        if(unitOfWork.add(this)) {
            if(this.transientValues == null){
                assertObjectIsAccessible(false);
            } else {
                unitOfWork.getState(this,false).setValues(this.transientValues);
                this.transientValues = null;
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
        if(jdoIsDirty()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "A dirty object can't be removed from the unit of work"
            );
        }
        getUnitOfWork().remove(this);
    }

    /**
     * This method clones an object
     * @param original
     * @return the clone
     * 
     * @throws ServiceException
     */
    public DataObject_1_0 openmdxjdoClone(
    ) {
        try {
            return new DataObject_1(
                this, 
                false, // beforeImage
                null // identity
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
    ) throws ServiceException{
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
        if (jdoIsPersistent()) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            "Attempt to move a persistent object",
            ExceptionHelper.newObjectIdParameter("id", this),
            new BasicException.Parameter("old",PersistenceHelper.getTransientContainerId(this.getContainer(true))),
            new BasicException.Parameter("new",PersistenceHelper.getTransientContainerId(there.container()))
        );
        this.container = (Container_1) there.container();
        //
        // Set the container
        //
        if(this.container.openmdxjdoIsPersistent()){
            String qualifier = criteria;
            for(PlugIn_1_0 plugIn : this.dataObjectManager.getPlugIns()) {
                qualifier = plugIn.getQualifier(this, qualifier);
            }
            if(qualifier == null) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "No plug-in did provide the object id's last XRI segment",
                ExceptionHelper.newObjectIdParameter("id", this),
                new BasicException.Parameter("criteria", criteria)
            );
            Path identity = container.openmdxjdoGetContainerId().getChild(qualifier);
            if(this.dataObjectManager.containsKey(identity)) {
                DataObject_1_0 collision = this.dataObjectManager.getObjectById(identity);
                if(collision != this) {
                    try {
                        collision.objGetClass();
                    } catch (Exception exception) {
                        // The candidate could be evicted now
                    }
                    if(this.dataObjectManager.containsKey(identity)) {
                        getState(false).setLifeCycleEventPending(false);
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
            makePersistent(identity, true);
            for(Flushable flushable : this.flushableValues.values()){
                if(flushable instanceof Container_1){
                    makePersistent(
                        (Container_1)flushable,
                        false // aspect
                    );
                    makePersistent(
                        (Container_1)flushable,
                        true // aspect
                    );
                } else if (flushable instanceof ManagedAspect) {
                    ManagedAspect managedAspect = (ManagedAspect) flushable;
                    if(this.created){
                        managedAspect.move();
                    } else {
                        managedAspect.evict();
                    }
                }
            }
            this.container.addToCache(qualifier, this);
        } else {
            //
            // Add to a transient container
            //
            this.container.addToCache(criteria, this);
            this.qualifier = criteria;
            for(Flushable flushable : this.flushableValues.values()){
                if(flushable instanceof ManagedAspect) {
                    ((ManagedAspect) flushable).move();
                }
            }
        }
    }

    /**
     * Make an object persistent ignoring its container and callbacks
     * 
     * @param identity the object id
     * @param callback 
     * 
     * @throws ServiceException  
     */
    public void makePersistent(
        Path identity, 
        boolean callback
    ) throws ServiceException {
        TransactionalState_1 state = getState(false);
        state.setLifeCycleEventPending(true);
        this.created = true;
        this.identity = identity;
        if(this.transientObjectId == null) {
            this.dataObjectManager.putIfAbsent(identity, this);
        }  else {
            this.dataObjectManager.move(this.transientObjectId,identity);
        }
        objMakeTransactional();
        if(callback) {
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
     * Removes an object.
     * <p>
     * Neither <code>getValue()</code> nor <code>setValue()</code>
     * calls are allowed after an <code>remove()</code> invocation and
     * <code>isDeleted()</code> will return <code>true</code> unless the
     * object has been transient.
     * 
     * @param updateCache 
     *
     * @exception   ServiceException NOT_SUPPORTED
     *              If the object refuses to be removed.
     * @exception   ServiceException
     *              if the object can't be removed
     */
    void objRemove(
        boolean updateCache
    ) throws ServiceException {
        if(!jdoIsPersistent()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "Attempt to remove a transient object"
            );
        }
        this.objMakeTransactional();
        this.dataObjectManager.getObjectById(
            this.identity.getPrefix(this.identity.size()-2)
        ).objMakeTransactional();
        this.dataObjectManager.fireInstanceCallback(this, InstanceLifecycleEvent.DELETE, false);
        this.deleted = true;
        this.getState(false).setLifeCycleEventPending(true);
        if(updateCache) {
            getContainer(false).removeFromChache(this.identity.getBase());
        }
    }

    /**
     * Prepare the object for flushing
     * @param transactionTime
     * 
     * @throws ServiceException
     */
    void prepare() throws ServiceException {
        if(jdoIsDirty() && !jdoIsDeleted()) {
            this.dataObjectManager.fireInstanceCallback(this, InstanceLifecycleEvent.STORE, false);
        }
        getState(false).setPrepared(true);
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
        TransactionalState_1 state = getState(false);
        if(state.isLifeCycleEventPending()){
            Object_2Facade input = Object_2Facade.newInstance(
                this.identity,
                this.transactionalValuesRecordName
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
    @SuppressWarnings("unchecked")
    void flush(
        Interaction interaction, 
        boolean beforeCompletion
    ) throws ServiceException {
        TransactionalState_1 state = getState(false);
        try {
            if(!jdoIsPersistent() && state.isLifeCycleEventPending()) {
                Map<String,Object> transactionalValues = state.values(false);
                if(this.persistentValues == null) {
                    this.persistentValues = Records.getRecordFactory().createMappedRecord(this.transactionalValuesRecordName);
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
                            source == null ? null : getMarshaller(feature).unmarshal(source)
                        );                    
                    }
                }
                Object_2Facade input = Object_2Facade.newInstance();
                input.setPath(
                    this.jdoIsPersistent() ? (Path)this.jdoGetObjectId() : new Path(this.jdoGetTransactionalObjectId())
                );
                input.setVersion(this.jdoGetVersion());
                input.setValue(this.persistentValues);                
                interaction.execute(
                    this.dataObjectManager.getInteractionSpecs().CREATE,
                    input.getDelegate()
                );
                state.setLifeCycleEventPending(false);
                state.setFlushed(true);
            } else if(jdoIsDeleted()){
                if(state.isLifeCycleEventPending()){
                    if(!jdoIsNew() || state.isFlushed()) {
                        Object_2Facade input = Object_2Facade.newInstance(
                            jdoGetObjectId(),
                            getRecordName(this)
                        );
                        input.setVersion(this.jdoGetVersion());
                        interaction.execute(
                            this.dataObjectManager.getInteractionSpecs().DELETE,
                            input.getDelegate()
                        );
                        state.setFlushed(false);
                    }
                    state.setLifeCycleEventPending(false);
                }
            } else if(jdoIsNew() && state.isLifeCycleEventPending()) {
                Map<String,Object> transactionalValues = state.values(false);
                if(this.persistentValues == null) {
                    this.persistentValues = Records.getRecordFactory().createMappedRecord(this.transactionalValuesRecordName);
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
                            source == null ? null : getMarshaller(feature).unmarshal(source)
                        );                    
                    }
                }
                Object_2Facade input = Object_2Facade.newInstance();
                input.setPath(this.identity);
                input.setVersion(this.jdoGetVersion());
                input.setValue(this.persistentValues);
                if(isProxy()) {
                    interaction.execute(
                        this.dataObjectManager.getInteractionSpecs().PUT,
                        input.getDelegate()
                    );
                } else if(state.isFlushed()) {
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
                this.transientOnRollback = true;
                state.setLifeCycleEventPending(false);
            } else if(
                (jdoIsDirty() || (jdoIsPersistent() && this.digest != null)) &&
                jdoGetObjectId().size() > 4 // exclude Authorities and Providers
            ){
                MappedRecord beforeImage = this.persistentValues;
                this.persistentValues = Records.getRecordFactory().createMappedRecord(objGetClass());
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
                                getMarshaller(feature).unmarshal(source)
                            );                    
                        }
                    } else if (beforeImage != null) {
                        this.persistentValues.put(
                            feature,
                            beforeImage.get(feature)
                        );
                    }
                }
                Object_2Facade input = Object_2Facade.newInstance();
                input.setPath(this.identity);
                input.setVersion(this.jdoGetVersion());
                input.setValue(this.persistentValues);
                this.persistentValues = beforeImage;
                interaction.execute(
                    this.dataObjectManager.getInteractionSpecs().PUT,
                    input.getDelegate()
                );
            }
            Queue<Operation> operations = state.operations(true);
            for(
                Operation operation = operations.poll();
                operation != null;
                operation = operations.poll()
            ){
                operation.invoke(interaction);
            }
            state.setDirtyFeaturesFlushed();
            if(
                !beforeCompletion &&
                !this.dataObjectManager.isRetainValues()
            ) {
                evict();
                
            }
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
        }
    }

    @SuppressWarnings("unchecked")
    public void addModifiedFeaturesTo(
        Set<Object> to
    ) throws ServiceException {
        TransactionalState_1 state = getState(false);
        ModelElement_1_0 classDef = this.dataObjectManager.getModel().findElement(
            this.transactionalValuesRecordName
        );
        Map<String,ModelElement_1_0> attributes = (Map<String, ModelElement_1_0>) classDef.objGetValue("attribute");
        for(
            Iterator<String> i = state.dirtyFeatures(true).iterator();
            i.hasNext();
        ){
            ModelElement_1_0 attribute = attributes.get(i.next());
            if(isFeatureModified(attribute)) {
                if(!Boolean.TRUE.equals(attribute.objGetValue("isDerived"))){
                    to.add(attribute.objGetValue("qualifiedName"));
                }
            } else {
                i.remove();
            }
        }
    }

    /**
     * 
     * 
     * @param feature
     * @param beforeImage
     * 
     * @return <code>true</code> if the feature had been modified
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
        String multiplicity = ModelUtils.getMultiplicity(feature);
        String featureName = (String) feature.objGetValue("name");
        if(SystemAttributes.CONTEXT_CAPABLE_CONTEXT.equals(featureName)) {
            return false;
        }
        Object left;
        Object right;
        if(
            Multiplicities.SINGLE_VALUE.equals(multiplicity) ||
            Multiplicities.OPTIONAL_VALUE.equals(multiplicity)
        ) {
            left = this.beforeImage.objGetValue(featureName);
            right = this.objGetValue(featureName);
        } else if (
            Multiplicities.LIST.equals(multiplicity) 
        ){
            left = this.beforeImage.objGetList(featureName);
            right = this.objGetList(featureName);
        } else if (
            Multiplicities.SET.equals(multiplicity)
        ){
            left = this.beforeImage.objGetSet(featureName);
            right = this.objGetSet(featureName);
        } else if (
            Multiplicities.SPARSEARRAY.equals(multiplicity)
        ){
            left = this.beforeImage.objGetSparseArray(featureName);
            right = this.objGetSparseArray(featureName);
        } else {
            return true; // to be on the secure side
        }
        return left == null ? right != null : !left.equals(right);
    }
    
    /**
     * Provide the before image
     * 
     * @param beforeImageId the before image's object id
     * 
     * @return the before image
     * 
     * @throws ServiceException
     */
    public DataObject_1 getBeforeImage(
        Path beforeImageId
    ) throws ServiceException {
        if((this.beforeImage == null || this.beforeImage.objIsInaccessible()) && !jdoIsNew()) {
            this.beforeImage = new DataObject_1(
                this, 
                true, // beforeImage
                beforeImageId
            );
        }
        return this.beforeImage;
    }
    
    void afterCompletion(
        int status
    ) throws ServiceException {
        switch(status) {
            case javax.transaction.Status.STATUS_COMMITTED:
                if(jdoIsDeleted()){
                    this.dataObjectManager.invalidate(
                        this.identity, 
                        false // makeNonTransactional
                    );
                    this.deleted = false;
                    this.identity = null;
                } else if(!this.dataObjectManager.isRetainValues()) {
                    evict(); 
                }
                break;
            case javax.transaction.Status.STATUS_ROLLEDBACK:
                getState(false).setLifeCycleEventPending(false);
                if(jdoIsDeleted()){
                    this.deleted = false;
                } else if(!this.dataObjectManager.isRetainValues()) {
                    evict(); //... depending on configuration
                }
                if(this.transientOnRollback || jdoIsNew()){
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
        Container_1 container = getContainer(true);
        if(container != null) {
            container.evict();
        }
        this.transientOnRollback = false;
        this.created = false;
        this.beforeImage = null;
    }

    /**
     * 
     */
    public void evict(
    ){
        if(jdoIsPersistent()) {
            try {
                this.dataObjectManager.fireInstanceCallback(this, InstanceLifecycleEvent.CLEAR, true);
            } catch (ServiceException unreachable) {
                // we are unexpectedly here 
            }
            clear();
            for(Map.Entry<String, Flushable> entry : this.flushableValues.entrySet()) {
                Flushable value = entry.getValue();
                if(value instanceof ManagedAspect || value instanceof Container_1) {
                    ((Evictable)value).evict();
                }
            }
        }
        this.digest = null;
        this.persistentValues = null;
    }

    private void clear(){
        try {
            TransactionalState_1 state = getState(true);
            if(state != null){ 
                Map<String,Object> values = state.values(true);
                if(!values.isEmpty()) {
                    values.clear();
                }
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
        TransactionalState_1 state = getState(true);
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
        if(jdoIsNew() || jdoIsDeleted()) {
            return true;
        } else if (jdoIsPersistent()) {
            TransactionalState_1 state = getState(true);
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

    public boolean objIsContained(){
        return jdoIsPersistent() || this.container != null;
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
    private boolean isProxy(
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
            return SharedObjects.getPlugInObject(this.dataObjectManager, VirtualObjects_2_0.class).isVirtual(this.identity);
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
            return getUnitOfWork().getMembers().contains(this);
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
     *
     * @exception   ServiceException    ILLEGAL_STATE
     *                  if the object is deleted
     */
    DataObject_1 assertObjectIsAccessible(
        boolean reload
    ) throws ServiceException {
        if(objIsInaccessible()){
            throw new ServiceException(
                getInaccessibilityReason()
            );
        } else if(reload || objIsHollow()) {
            if(isProxy() && !isVirtual()) {
                UnitOfWork_1 unitOfWork = this.dataObjectManager.currentTransaction();
                if(unitOfWork.isOutOfSync()) {
                    unitOfWork.flush(false);
                }
                return unconditionalLoad();
            } else if (jdoIsPersistent() && !jdoIsNew()) {
                return unconditionalLoad();
            } else {
                return this;
            }
        } else {
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
        if(jdoIsPersistent() && !jdoIsNew()) {
            assertObjectIsAccessible(false);
            return true;
        } else if (isProxy() && Boolean.TRUE.equals(this.getFeature(name).objGetValue("isDerived"))) {
            objMakeTransactional();
            TransactionalState_1 state = getState(false);
            if(!state.isFlushed()) {
                state.setLifeCycleEventPending(true);
            }
            assertObjectIsAccessible(true);
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
        String multiplicity, 
        boolean clear
    ) throws ServiceException {
        Object persistentValue;
        if(!clear && attributeMustBeLoaded(name)) try {
            persistentValue = this.persistentValues.get(name);
            if(
                persistentValue == null &&
                !this.persistentValues.containsKey(name)
            ) {
                MappedRecord persistentValues = Records.getRecordFactory().createMappedRecord(this.persistentValues.getRecordName());
                persistentValues.putAll(this.persistentValues);
                Filter attributeSpecifier = new Filter(
                    NO_FILTER,
                    new AttributeSpecifier[]{
                        new AttributeSpecifier(name)
                    }
                );
                Query_2Facade input = Query_2Facade.newInstance();
                input.setPath(this.jdoIsPersistent() ? this.jdoGetObjectId() : new Path(this.jdoGetTransactionalObjectId()));
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
                Object_2Facade output = Object_2Facade.newInstance(
                    (MappedRecord)indexedRecord.get(0)
                );                
                persistentValues.putAll(
                    output.getValue()
                );
                persistentValue = stream ? persistentValues.remove(name) : persistentValues.get(name);
                this.persistentValues = persistentValues;
            }
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
        }
        else if (this.persistentValues == null) {
            persistentValue = null;
        } 
        else {    
            persistentValue = this.persistentValues.get(name);
        }
        if(
            Multiplicities.SINGLE_VALUE.equals(multiplicity) || 
            Multiplicities.OPTIONAL_VALUE.equals(multiplicity)
        ) {
            if(persistentValue instanceof List) {
                persistentValue = ((List<?>)persistentValue).get(0);
                if(this.persistentValues != null) {
                    this.persistentValues.put(
                        name,
                        persistentValue
                    );
                }
            }
            // Do not cache streams
            if(stream && this.persistentValues != null) {
                this.persistentValues.remove(name);
            }
        } 
        else if (persistentValue instanceof List) {
            if(clear) {
                ((List<?>)persistentValue).clear();
            }
        } 
        else if (persistentValue instanceof SparseArray) {
            if(clear) {
                ((SparseArray<?>)persistentValue).clear();
            }
        } 
        else if(persistentValue instanceof IndexedRecord) {
            persistentValue = clear ? new ArrayList() : new ArrayList((IndexedRecord)persistentValue);
            if(this.persistentValues != null) {
                this.persistentValues.put(
                    name,
                    persistentValue
                );
            }
        }
        else if(persistentValue instanceof MappedRecord) {
            if(Multiplicities.SPARSEARRAY.equals(multiplicity)) {
                SparseArray target = new TreeSparseArray(
                    (MappedRecord) persistentValue
                );
                persistentValue = target;
            }
            else {
                List target = new ArrayList(); 
                if(!clear) {
                    Map<?,?> source = (MappedRecord) persistentValue;
                    for(Map.Entry<?, ?> e : source.entrySet()) {
                        target.set((Integer)e.getKey(), e.getValue());
                    }
                }
                persistentValue = target;
            }
            if(this.persistentValues != null) {
                this.persistentValues.put(
                    name,
                    persistentValue
                );
            }
        }
        else {
            if(Multiplicities.SPARSEARRAY.equals(multiplicity)) {
                SparseArray target = new TreeSparseArray();
                if(!clear) {
                    target.put(0, persistentValue);
                }
                persistentValue = target;
            }
            else {
                persistentValue = new ArrayList(
                    clear || persistentValue == null ? 
                        Collections.emptyList() : 
                            Collections.singletonList(persistentValue)
                );
            }
            if(this.persistentValues != null) {
                this.persistentValues.put(
                    name,
                    persistentValue
                );
            }
        }
        return persistentValue;
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
        assertSingleValued(to);
        UnitOfWork_1 unitOfWork = getUnitOfWorkIfTransactional(feature);
        Object value;
        try {
            value = 
                to instanceof BinaryLargeObject ? ((BinaryLargeObject)to).getContent() :
                to instanceof CharacterLargeObject ? ((CharacterLargeObject)to).getContent() :
                to;
        } catch (IOException exception) {
            throw new ServiceException(exception);
        }
        (
            unitOfWork == null ? this.transientValues : unitOfWork.getState(this,false).values(false)
        ).put(
            feature, 
            value
        );
        if("core".equals(feature) && to instanceof DataObject_1) {
            for(PlugIn_1_0 plugIn : this.dataObjectManager.getPlugIns()) {
                plugIn.postSetCore(this, (DataObject_1) to);
            }
        }
    }

    /**
     * Retrieve the object's classifier
     * 
     * @return  the object's classifier
     * 
     * @throws ServiceException
     */
    protected ModelElement_1_0 getClassifier(
    ) throws ServiceException{
        return this.classifier == null ?
            this.classifier = this.dataObjectManager.getModel().getElement(objGetClass()) :
            this.classifier;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetIterable(java.lang.String)
     */
    Iterable<?> objGetIterable(
        String featureName
    ) throws ServiceException {
        ModelElement_1_0 featureDef = this.dataObjectManager.getModel().getFeatureDef(this.getClassifier(), featureName, false);
        String multiplicity;
        if(featureDef == null) {
            multiplicity = null;
            new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Feature definition not found",
                new BasicException.Parameter("class.name", this.objGetClass()),
                new BasicException.Parameter("feature.name", featureName)                
            ).log();
        } else {
            multiplicity = ModelUtils.getMultiplicity(featureDef);
        }
        if(Multiplicities.LIST.equals(multiplicity)) {
            return objGetList(featureName);
        }
        if(Multiplicities.SET.equals(multiplicity)) {
            return objGetSet(featureName);
        }
        if(Multiplicities.SPARSEARRAY.equals(multiplicity)){
            return objGetSparseArray(featureName).values();
        }
        Object value = objGetValue(featureName);
        return value == null ? Collections.emptySet() : Collections.singleton(value);
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
        Object persistentValue = getPersistentAttribute(
            featureName, 
            true, // stream 
            Multiplicities.SINGLE_VALUE, 
            false // clear
        );
        if(PrimitiveTypes.BINARY.equals(type)) {
            return persistentValue instanceof BinaryLargeObject ? (BinaryLargeObject) persistentValue : new BinaryLargeObjects.StreamLargeObject(
                (InputStream) persistentValue
            ) {
                
                @Override
                protected InputStream newContent(
                ) throws IOException {
                    try {
                        return (InputStream) getPersistentAttribute(
                            featureName, 
                            true, // stream 
                            Multiplicities.SINGLE_VALUE, 
                            false // clear
                        );
                    } catch (ServiceException exception) {
                        throw new IOException(
                            exception.getCause().getDescription(),
                            exception.getCause()
                        );
                    }
                }
                
            }; 
        } else if (PrimitiveTypes.STRING.equals(type)) {
            return persistentValue instanceof CharacterLargeObject ? (CharacterLargeObject) persistentValue : new CharacterLargeObjects.StreamLargeObject(
                (Reader) persistentValue
            ) {
                
                @Override
                protected Reader newContent(
                ) throws IOException {
                    try {
                        return (Reader) getPersistentAttribute(
                            featureName, 
                            true, // stream 
                            Multiplicities.SINGLE_VALUE, 
                            false // clear
                        );
                    } catch (ServiceException exception) {
                        throw new IOException(
                            exception.getCause().getDescription(),
                            exception.getCause()
                        );
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
            UnitOfWork_1 unitOfWork = getUnitOfWork();
            Map<String,Object> transactionalValues;
            if(jdoIsTransactional()) { 
                transactionalValues = unitOfWork.getState(DataObject_1.this, false).values(false);
                Object transactionalValue = transactionalValues.get(feature);
                if(transactionalValue != null || transactionalValues.containsKey(feature)) {
                    return transactionalValue;
                }
            } else {
                transactionalValues = null; 
            }
            ModelElement_1_0 featureDef = getFeature(feature);
            Object transactionalValue;
            if(Multiplicities.STREAM.equals(ModelUtils.getMultiplicity(featureDef))){
                transactionalValue = getLargeObjectValue(feature, featureDef);
            } else {
                Object nonTransactionalValue = getPersistentAttribute(
                    feature, 
                    false, 
                    Multiplicities.SINGLE_VALUE, 
                    false
                );
                transactionalValue = 
                    nonTransactionalValue == null ? null :  
                    featureDef == null ? nonTransactionalValue : // an embedded object's class
                    getMarshaller(featureDef).marshal(nonTransactionalValue);
            }
            if(transactionalValues != null) {
                transactionalValues.put(feature, transactionalValue);
            }
            return transactionalValue;
        } else {
            if(
                (objIsHollow() && !isProxy()) ||
                this.transientValues.containsKey(feature)
            ) {
                return this.transientValues.get(feature);
            } else {
                ModelElement_1_0 featureDef = getFeature(feature);
                Object transientValue;
                if(Multiplicities.STREAM.equals(ModelUtils.getMultiplicity(featureDef))){
                    transientValue = getLargeObjectValue(feature, featureDef);
                } else {
                    Object clonedValue = getPersistentAttribute(
                        feature, 
                        false, // stream
                        Multiplicities.SINGLE_VALUE, // single-valued
                        false // clear
                    );
                    transientValue = 
                        clonedValue == null ? null :  
                        featureDef == null ? clonedValue : // an embedded object's class
                        getMarshaller(featureDef).marshal(clonedValue);
                }
                (jdoIsTransactional() ? getState(false).values(false) : this.transientValues).put(
                    feature, 
                    transientValue
                );
                return transientValue;
            }
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
        List<Object> flushable = (List<Object>) this.flushableValues.get(feature);
         return flushable == null ?putFlushable(
            feature,
            new ManagedList(
                feature,
                getMarshaller(feature)
            )
        ) : flushable;
    }

    /**
     * Get a Set attribute.
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
     *              if the feature's value is not a set
     */
    @SuppressWarnings("unchecked")
    public Set<Object> objGetSet(
        String feature
    ) throws ServiceException {
        Set<Object> flushable = (Set<Object>) this.flushableValues.get(feature);
        return flushable == null ? putFlushable(
            feature,
            new ManagedSet(
                feature,
                getMarshaller(feature)
            )
        ) :  flushable;
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
        SortedMap<Integer,Object> flushable = (SortedMap<Integer, Object>) this.flushableValues.get(feature);
        return flushable == null ? putFlushable(
            feature,
            new ManagedSortedMap(
                feature,
                getMarshaller(feature)
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
    ) throws ServiceException{
        Container_1_0 flushable = (Container_1_0) this.flushableValues.get(feature);
        return flushable == null ? putFlushable(
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
                    objMakeTransactional();
                    getState(false).operations(false).offer(entry);
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
    @SuppressWarnings("unchecked")
    public String toString(
    ){
        if(this.objIsInaccessible()) {
            return AbstractDataObject_1.toString(this, null);
        } else {
            try {
                Map content = new HashMap();
                TransactionalState_1 state = getState(true);
                String description;
                if(state == null) {
                    description = null;
                } else {
                    for(Map.Entry<String,Object> e : state.values(false).entrySet()) {
                        Object v = e.getValue();
                        if(v instanceof Collection) {
                            if (v instanceof List) {
                                List t = new ArrayList();
                                for(
                                        Iterator j = ((List)v).iterator();
                                        j.hasNext();
                                ) {
                                    t.add(
                                        noContent(j.next())
                                    );
                                }
                            } else if (v instanceof Set) {
                                Set t = new HashSet();
                                for(
                                        Iterator j = ((Set)v).iterator();
                                        j.hasNext();
                                ) {
                                    t.add(
                                        noContent(j.next())
                                    );
                                }
                            } // else ignore
                        } else if (v instanceof SortedMap) {
                            SortedMap t = new TreeMap();
                            for(
                                    Iterator j = ((SortedMap)v).entrySet().iterator();
                                    j.hasNext();
                            ){
                                Map.Entry k = (Map.Entry)j.next();
                                t.put(
                                    k.getKey(),
                                    noContent(k.getValue())
                                );
                            }
                        } else {
                            content.put(e.getKey(), noContent(v));
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
            TransactionalState_1 state = getState(true);
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
                state = getState(false);
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
        this.dataObjectManager.putIfAbsent(this.identity, this);
    }

    @SuppressWarnings("unchecked")
    private final ModelElement_1_0 getFeature(
        String featured,
        String kind, 
        String feature
    ) throws ServiceException{
        Map<String,ModelElement_1_0> features = (Map<String, ModelElement_1_0>)this.dataObjectManager.getModel().getElement(
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
        Marshaller marshaller = feature == null ? null : dataTypeMarshaller.get(
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
        return getMarshaller(getFeature(featured, kind, feature));
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
            return getFeature(
                objGetClass(), 
                "attribute", 
                feature
            );
        } else {
            this.assertObjectIsAccessible(false);
            Object raw = this.persistentValues.get(
                feature.substring(0, ++i) + SystemAttributes.OBJECT_CLASS
            );
            String embeddedClass = raw instanceof List<?> ?
                (String) ((List<?>)raw).get(0) :
                (String)raw;
            String embeddedFeature = feature.substring(i);
            return SystemAttributes.OBJECT_CLASS.equals(embeddedFeature) ? null : getFeature(
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
        return getMarshaller(getFeature(feature));
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getAspect(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public Map<String, DataObject_1_0> getAspect(
        String aspectType
    ){
        Map<String, DataObject_1_0> flushable = (Map<String, DataObject_1_0>) this.flushableValues.get(aspectType);
        return flushable == null ? putFlushable(
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

    
    //--------------------------------------------------------------------------
    // Class ManagedAspect
    //--------------------------------------------------------------------------
    
    /**
     * Managed Aspect
     */
    final class ManagedAspect
        extends AbstractMap<String,DataObject_1_0> 
        implements Flushable, Evictable
    {

        ManagedAspect(
            String aspectClass
        ){
            this.aspectClass = aspectClass;
        }

        private boolean evicted = true;        
        private final String aspectClass;        
        private transient Container_1_0 aspect = null;
        private transient Container_1_0 stored = null;
        private transient Collection<DataObject_1_0> values = null;
        
        /**
         * Retrieve either the transactional or persistent entry set
         * 
         * @return the delegate entry set
         */
        private Container_1_0 getStored(
        ){
            if(this.stored == null) try {
                this.stored = this.getAspect();
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
            return this.stored;
        }
        
        Map<String,DataObject_1_0> getCache(
        ){
            boolean evicted = this.evicted;
            this.evicted = false;
            TransactionalState_1 state = getState(false); 
            return evicted ? state.transientAspects(
                this.aspectClass, 
                this.getStored()
            ) : state.transientAspects(
                this.aspectClass
            );
        }

        void move(){
            if(!this.evicted && this.aspect == null) {
                Map<String,DataObject_1_0> source = new HashMap<String,DataObject_1_0>(
                    getCache()
                );
                evict();
                putAll(source);
            }
        }
        
        /**
         * Aspect accessor
         * 
         * @return an accessor for a given aspect class
         * 
         * @throws ServiceException
         */
        private Container_1_0 getAspect(
        ) throws ServiceException {
            if(this.aspect == null) {
                Container_1_0 aspects = DataObject_1.this.getAspects();
                if(aspects != null) {
                    this.aspect = aspects.subMap(
                        new FilterProperty[]{
                            new FilterProperty(
                                Quantors.THERE_EXISTS,
                                SystemAttributes.OBJECT_INSTANCE_OF,
                                FilterOperators.IS_IN,
                                this.aspectClass
                            )
                        }
                    );
                    this.aspect.retrieveAll(null);
                }
            }
            return this.aspect;
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractMap#clear()
         */
        public void clear() {
            this.evicted = true;
            this.getStored().clear();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#containsKey(java.lang.Object)
         */
        public boolean containsKey(
            Object key
        ) {
            return this.getCache().containsKey(key);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#containsValue(java.lang.Object)
         */
        public boolean containsValue(
            Object value
        ) {
            return getCache().containsValue(value);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#entrySet()
         */
        public Set<java.util.Map.Entry<String, DataObject_1_0>> entrySet(
        ) {
            return this.getCache().entrySet();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#equals(java.lang.Object)
         */
        public boolean equals(
            Object o
        ) {
            return this.getCache().equals(o);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#get(java.lang.Object)
         */
        public DataObject_1_0 get(
            Object key
        ) {
            return this.getCache().get(key);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#hashCode()
         */
        public int hashCode(
        ) {
            return this.getCache().hashCode();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#isEmpty()
         */
        public boolean isEmpty(
        ) {
            return this.getCache().isEmpty();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#keySet()
         */
        public Set<String> keySet(
        ) {
            return this.getCache().keySet();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
         */
        public DataObject_1_0 put(
            String key, 
            DataObject_1_0 value
        ) {
            try {
                if(jdoIsPersistent()) {
                    objMakeTransactional();
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
                return getCache().put(
                    DataObject_1.this.getAspects() == null ? key : this.toObjectId(getQualifier(), key), 
                    value
                );
            } catch (ServiceException exception) {
                this.evicted = true;
                throw new RuntimeServiceException(exception);
            } catch (RuntimeServiceException exception) {
                this.evicted = true;
                throw exception;
            }
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#remove(java.lang.Object)
         */
        public DataObject_1_0 remove(
            Object key
        ) {
            this.evicted = true;
            return this.getStored().remove(key);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#size()
         */
        public int size(
        ) {
            return this.getCache().size();
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
                aspectId.startsWith(":") ? ":" + coreId + aspectId :
                aspectId.startsWith("!") ? coreId + aspectId :
                coreId + '*' + aspectId;    
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.transport.delegation.Evictable#evict()
         */
        public void evict() {
            this.evicted = true;
            this.stored = null;
            this.aspect = null;
        }

        
        //--------------------------------------------------------------------
        // Class Values
        //--------------------------------------------------------------------
        
        /**
         * Values
         */
        class Values implements Collection<DataObject_1_0> {

            private final Collection<DataObject_1_0> getDelegate(){
                return getCache().values();
            }

            /**
             * <em>Note:<br>
             * Do not call this method if the object is already member of the collection!
             * </em>
             * @param o
             * @return <code>true</code>
             * @see java.util.Collection#add(java.lang.Object)
             */
            public boolean add(
                DataObject_1_0 o
            ) {
                if(containsValue(o)) throw new RuntimeServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "Do not add a member twice!",
                    ExceptionHelper.newObjectIdParameter("id", o)
                );
                put(
                    PathComponent.createPlaceHolder().toString(),
                    o
                );
                return true;
            }

            /**
             * @param c
             * @return
             * @see java.util.Collection#addAll(java.util.Collection)
             */
            public boolean addAll(Collection<? extends DataObject_1_0> c) {
                boolean modified = false;
                for(DataObject_1_0 o : c) {
                    modified |= add(o);
                }
                return modified;
            }

            /**
             * 
             * @see java.util.Collection#clear()
             */
            public void clear() {
                getCache().clear();
            }

            /**
             * @param o
             * @return
             * @see java.util.Collection#contains(java.lang.Object)
             */
            public boolean contains(Object o) {
                return getCache().containsValue(o);
            }

            /**
             * @param c
             * @return
             * @see java.util.Collection#containsAll(java.util.Collection)
             */
            public boolean containsAll(Collection<?> c) {
                return getDelegate().containsAll(c);
            }

            /**
             * @return
             * @see java.util.Collection#isEmpty()
             */
            public boolean isEmpty() {
                return getCache().isEmpty();
            }

            /**
             * @return
             * @see java.util.Collection#iterator()
             */
            public Iterator<DataObject_1_0> iterator() {
                return getDelegate().iterator();
            }

            /**
             * @param o
             * @return
             * @see java.util.Collection#remove(java.lang.Object)
             */
            public boolean remove(Object o) {
                return getDelegate().remove(o);
            }

            /**
             * @param c
             * @return
             * @see java.util.Collection#removeAll(java.util.Collection)
             */
            public boolean removeAll(Collection<?> c) {
                return getDelegate().removeAll(c);
            }

            /**
             * @param c
             * @return
             * @see java.util.Collection#retainAll(java.util.Collection)
             */
            public boolean retainAll(Collection<?> c) {
                return getDelegate().retainAll(c);
            }

            /**
             * @return
             * @see java.util.Collection#size()
             */
            public int size() {
                return getCache().size();
            }

            /**
             * @return
             * @see java.util.Collection#toArray()
             */
            public Object[] toArray() {
                return getDelegate().toArray();
            }

            /**
             * @param <T>
             * @param a
             * @return
             * @see java.util.Collection#toArray(T[])
             */
            public <T> T[] toArray(T[] a) {
                return getDelegate().toArray(a);
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
                UnitOfWork_1 unitOfWork = getUnitOfWorkIfTransactional(makeDirty ? this.feature : null);
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
                            transactionalValue = clear ? new ArrayList() : new ArrayList<Object>(this.nonTransactional)
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

        public Object get(
            int index
        ) {
            return getDelegate(false, false).get(index);
        }

        public int size(
        ){
            return getDelegate(false, false).size();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return getDelegate(false, false).isEmpty();
        }

        public Object set(
            int index,
            Object element
        ){
            return getDelegate(true, false).set(index, element);
        }

        public void add(
            int index,
            Object element
        ){
            getDelegate(true, false).add(index, element);
        }

        public Object remove(
            int index
        ){
            return getDelegate(true, false).remove(index);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractList#clear()
         */
        @Override
        public void clear() {
            getDelegate(true, true);
        }

        /* (non-Javadoc)
         * @see java.io.Flushable#flush()
         */
        public void flush() throws IOException {
            List<Object> source = getDelegate(false, false);
            if(source != this.nonTransactional) {
                this.nonTransactional.clear();
                this.nonTransactional.addAll(source);
            }
        }

        /**
         * Persistent Values Accessor
         */
        @SuppressWarnings("unchecked")
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
            protected List getDelegate(
            ) {
                try {
                    return (List)DataObject_1.this.getPersistentAttribute(
                        ManagedList.this.feature, 
                        false,
                        Multiplicities.LIST,
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
                        Multiplicities.LIST,
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
                UnitOfWork_1 unitOfWork = getUnitOfWorkIfTransactional(makeDirty ? this.feature : null);
                if(unitOfWork == null) {
                    if(transientValues != null) {
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
                            transactionalValue = clear ? new HashSet() : new HashSet<Object>(this.nonTransactional)
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
            Set<Object> delegate = getDelegate(false, false);
            return new SetIterator(
                delegate.iterator(),
                delegate != this.nonTransactional
            );
        }

        @Override
        public void clear(
        ) {
            getDelegate(true, true);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size(
        ) {
            return getDelegate(false, false).size();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return getDelegate(false, false).isEmpty();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#add(java.lang.Object)
         */
        @Override
        public boolean add(Object o) {
            return getDelegate(true, false).add(o);
        }

        /* (non-Javadoc)
         * @see java.io.Flushable#flush()
         */
        public void flush() throws IOException {
            Set<Object> source = getDelegate(false, false);
            if(source != this.nonTransactional) {
                this.nonTransactional.clear();
                this.nonTransactional.addAll(source);
            }
        }

        /**
         * Persistent Values Accessor
         */
        @SuppressWarnings("unchecked")
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
            protected Collection getDelegate(
            ) {
                try {
                    return (Collection)DataObject_1.this.getPersistentAttribute(
                        ManagedSet.this.feature, 
                        false,
                        Multiplicities.LIST,
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
                        Multiplicities.LIST,
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
                    getDelegate(true, false).remove(this.current);
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
                                Multiplicities.SPARSEARRAY,
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
                                Multiplicities.SPARSEARRAY,
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
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = -5406002308998595406L;

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
                UnitOfWork_1 unitOfWork = getUnitOfWorkIfTransactional(makeDirty ? this.feature : null);
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
            SortedMap<Integer,Object> source = getDelegate(false, false);
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
            return getDelegate(false,false).firstKey();
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
            return getDelegate(false,false).lastKey();
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
            getDelegate(true,true);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#containsKey(java.lang.Object)
         */
        public boolean containsKey(Object key) {
            return getDelegate(false,false).containsKey(key);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#containsValue(java.lang.Object)
         */
        public boolean containsValue(Object value) {
            return getDelegate(false,false).containsValue(value);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#get(java.lang.Object)
         */
        public Object get(Object key) {
            return getDelegate(false,false).get(key);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#isEmpty()
         */
        public boolean isEmpty() {
            return getDelegate(false,false).isEmpty();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#keySet()
         */
        public Set<Integer> keySet() {
            return getDelegate(false,false).keySet(); // TOD make it modifiable
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
         */
        public Object put(Integer key, Object value) {
            return getDelegate(true,false).put(key, value);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#putAll(java.util.Map)
         */
        public void putAll(Map<? extends Integer, ? extends Object> t) {
            getDelegate(true,false).putAll(t);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#remove(java.lang.Object)
         */
        public Object remove(Object key) {
            return getDelegate(true,false).remove(key);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#size()
         */
        public int size() {
            return getDelegate(false,false).size();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#values()
         */
        public Collection<Object> values() {
            return getDelegate(false,false).values();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            return getDelegate(false,false).equals(obj);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return getDelegate(false,false).hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return getDelegate(false,false).toString();
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
                SortedMap<Integer,Object> map = getDelegate(false,false);
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
                return getSubMap().size();
            }

            /* (non-Javadoc)
             * @see java.util.AbstractMap#entrySet()
             */
            @Override
            public Set<java.util.Map.Entry<Integer, Object>> entrySet() {
                return getSubMap().entrySet();
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
                return getSubMap().firstKey();
            }

            /* (non-Javadoc)
             * @see java.util.SortedMap#headMap(java.lang.Object)
             */
            public SortedMap<Integer, Object> headMap(Integer toKey) {
                validateKey(toKey);
                return new SubMap(this.from, toKey);
            }

            /* (non-Javadoc)
             * @see java.util.SortedMap#lastKey()
             */
            public Integer lastKey() {
                return getSubMap().lastKey();
            }

            /* (non-Javadoc)
             * @see java.util.SortedMap#subMap(java.lang.Object, java.lang.Object)
             */
            public SortedMap<Integer, Object> subMap(
                Integer fromKey,
                Integer toKey
            ) {
                validateKey(fromKey);
                validateKey(toKey);
                return new SubMap(fromKey, toKey);
            }

            /* (non-Javadoc)
             * @see java.util.SortedMap#tailMap(java.lang.Object)
             */
            public SortedMap<Integer, Object> tailMap(Integer fromKey) {
                validateKey(fromKey);
                return new SubMap(fromKey, null);
            }
            
        };
        
        /**
         * Entry Iterator
         */
        class EntryIterator implements Iterator<Map.Entry<Integer, Object>> {
            
            private SortedMap<Integer,Object> readOnly = getDelegate(false,false);
            
            private Iterator<Map.Entry<Integer, Object>> delegate = readOnly.entrySet().iterator();

            protected Map.Entry<Integer, Object> current = null;
            
            protected void makeDirty(){
                if(this.readOnly != null) {
                    SortedMap<Integer,Object> forUpdate = getDelegate(true,false);
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
                        makeDirty();
                        return EntryIterator.this.current.setValue(value);
                    }
                    
                };
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#remove()
             */
            public void remove() {
                makeDirty();
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
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = -6603383640304625799L;

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
                MessageRecord input = (MessageRecord) Records.getRecordFactory().createMappedRecord(MessageRecord.NAME);
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
        dataTypeMarshaller.put(DATE, DateMarshaller.NORMALIZE);
        dataTypeMarshaller.put(DATETIME, DateTimeMarshaller.NORMALIZE);
        dataTypeMarshaller.put(DURATION, DurationMarshaller.NORMALIZE);
        dataTypeMarshaller.put(SHORT, ShortMarshaller.NORMALIZE);
        dataTypeMarshaller.put(INTEGER, IntegerMarshaller.NORMALIZE);
        dataTypeMarshaller.put(LONG, LongMarshaller.NORMALIZE);
        //
        // lifecycleListenerClass initalization
        //
        lifecycleListenerClass[InstanceLifecycleEvent.CREATE] = CreateLifecycleListener.class;
        lifecycleListenerClass[InstanceLifecycleEvent.LOAD] = LoadLifecycleListener.class;
        lifecycleListenerClass[InstanceLifecycleEvent.STORE] = StoreLifecycleListener.class;
        lifecycleListenerClass[InstanceLifecycleEvent.CLEAR] = ClearLifecycleListener.class;
        lifecycleListenerClass[InstanceLifecycleEvent.DELETE] = DeleteLifecycleListener.class;
        lifecycleListenerClass[InstanceLifecycleEvent.DIRTY] = DirtyLifecycleListener.class;
        lifecycleListenerClass[InstanceLifecycleEvent.DETACH] = DetachLifecycleListener.class;
        lifecycleListenerClass[InstanceLifecycleEvent.ATTACH] = AttachLifecycleListener.class;

    }

}
