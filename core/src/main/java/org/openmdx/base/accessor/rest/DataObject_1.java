/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Object_1 class
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2016, OMEX AG, Switzerland
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
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Date;
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
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalInternalException;
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
import org.openmdx.base.accessor.rest.spi.LockAssertions;
import org.openmdx.base.accessor.rest.spi.ObjectRecords;
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
import org.openmdx.base.collection.MarshallingMap;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.collection.PopulationMap;
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
import org.openmdx.base.naming.ClassicSegments;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.PersistenceCapableCollection;
import org.openmdx.base.persistence.spi.StandardFetchPlan;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.IsInCondition;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.MethodInvocationSpec;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.base.transaction.Status;
import org.openmdx.kernel.collection.TreeSparseArray;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.CharacterLargeObjects;
import org.w3c.cci2.LargeObject;
import org.w3c.cci2.SortedMaps;
import org.w3c.cci2.SparseArray;
import org.w3c.format.DateTimeFormat;

/**
 * DataObject_1_0 implementation
 */
public class DataObject_1
    implements DataObject_1_0, Serializable {

    /**
     * Constructor for a detached-clean instance
     */
    public DataObject_1(
        Path identity,
        byte[] version,
        boolean multithreaded
    ) {
        this.identity = identity;
        this.transientObjectId = null;
        this.detached = true;
        this.untouchable = false;
        this.version = version;
        this.flushableValues = Maps.newMap(multithreaded);
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
        final DataObjectManager_1 manager,
        final Path identity,
        final UUID transientObjectId,
        final String objectClass,
        final boolean frozen 
    )
        throws ServiceException {
        if (objectClass == null && identity == null)
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The identity is required for persisten objects, the object class for transient ones",
                new BasicException.Parameter("identity", identity),
                new BasicException.Parameter("model-class", objectClass)
            );
        final boolean threadSafetyRequired = manager.isThreadSafetyRequired();
        this.dataObjectManager = manager;
        this.identity = identity;
        this.transientObjectId = transientObjectId == null ? UUIDs.newUUID() : transientObjectId;
        this.flushableValues = Maps.newMap(threadSafetyRequired);
        this.detached = false;
        this.untouchable = frozen;
        this.transactionalValuesRecordName = objectClass;
        if (identity == null) {
            this.transientValues = Maps.newMapSupportingNullValues(threadSafetyRequired);
        } else {
            manager.putUnlessPresent(identity, this);
        }
        manager.putUnlessPresent(
            this.transientObjectId,
            this
        );
    }

    /**
     * Constructor
     * 
     * @param that
     * @param beforeImage
     *            <code>true</code> if the before-image shall be cloned
     * @param identity
     * @param exclude
     *            the features not to be cloned
     * 
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    private DataObject_1(
        DataObject_1 that,
        boolean beforeImage,
        String... exclude
    )
        throws ServiceException {
        this(
            that.dataObjectManager,
            null, // identity
            null, // transientObjectId 
            DataObject_1.getRecordName(that, false),
            beforeImage
        );
        this.version = null;
        if (that.jdoIsPersistent() && !that.jdoIsNew()) {
            that.objRetrieve(
                false,
                null,
                null,
                beforeImage,
                true // throwNotFoundException
            );
        }
        final Set<String> notToBeCloned = new HashSet<>(Arrays.asList(exclude));
        if (!beforeImage && isExtentCapable()) {
            notToBeCloned.add(SystemAttributes.OBJECT_IDENTITY);
        }
        this.persistentValues = newRecord(this.transactionalValuesRecordName);
        if (!that.objIsHollow()) {
            //
            // Persistent Values
            //
            for (Map.Entry<?, ?> e : ((Map<?, ?>) that.persistentValues).entrySet()) {
                final Object featureName = e.getKey();
                if (!notToBeCloned.contains(featureName)) {
                    this.persistentValues.put(featureName, isolate(e.getValue()));
                }
            }
        }
        final TransactionalState_1 thisState = this.getState(false);
        final Set<String> thisDirty = thisState.dirtyFeatures(false);
        if (beforeImage) {
            //
            // Persistent Values
            //
            thisDirty.addAll(that.persistentValues.keySet());
        } else {
            //
            // Transactional Values
            //
            TransactionalState_1 thatState = that.getState(true);
            if (thatState != null) {
                for (Map.Entry<String, Object> e : thatState.values(true).entrySet()) {
                    String feature = e.getKey();
                    if (!notToBeCloned.contains(feature)) {
                        Object candidate = e.getValue();
                        if (candidate instanceof Set) {
                            Set<Object> target = this.objGetSet(feature);
                            target.clear();
                            target.addAll((Set<?>) candidate);
                        } else if (candidate instanceof List) {
                            List<Object> target = this.objGetList(feature);
                            target.clear();
                            target.addAll((List<?>) candidate);
                        } else if (candidate instanceof SortedMap) {
                            SortedMap<Integer, Object> target = this.objGetSparseArray(feature);
                            target.clear();
                            target.putAll((SortedMap<Integer, Object>) candidate);
                        } else {
                            this.objSetValue(feature, candidate);
                        }
                    }
                }
            }
            //
            // Transient Values
            //
            if (that.transientValues != null &&
                this.transientValues != null) {
                for (Map.Entry<String, Object> e : that.transientValues.entrySet()) {
                    String feature = e.getKey();
                    if (!notToBeCloned.contains(feature)) {
                        Object candidate = e.getValue();
                        if (candidate instanceof Set) {
                            this.transientValues.put(feature, new HashSet<Object>((Set<?>) candidate));
                            this.objGetSet(feature);
                        } else if (candidate instanceof List) {
                            this.transientValues.put(feature, new ArrayList<Object>((List<?>) candidate));
                            this.objGetList(feature);
                        } else if (candidate instanceof SortedMap) {
                            this.transientValues.put(feature, new TreeMap<Integer, Object>((SortedMap<Integer, Object>) candidate));
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
            if (that.persistentValues != null) {
                thisDirty.addAll(that.persistentValues.keySet());
            }
            if (this.transientValues != null) {
                thisDirty.addAll(this.transientValues.keySet());
            }
            //
            // Streams
            //
            for (Map.Entry<String, ModelElement_1_0> attribute : getAttributes().entrySet()) {
                ModelElement_1_0 featureDef = attribute.getValue();
                if (isStreamed(featureDef) && isPersistent(featureDef)) {
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
    final static Class<? extends InstanceLifecycleListener>[] LIFECYCLE_LISTENER_CLASS = new Class[8];

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
    protected Map<String, Object> transientValues;

    /**
     * @serial the write lock
     */
    protected byte[] version = null;

    /**
     * @serial the read lock
     */
    protected Object lock = null;

    /**
     * The id of the non-existing object
     */
    private transient String notFound = null;

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
    transient Map<String, Flushable> flushableValues;

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
    static final Map<String, Marshaller> DATA_TYPE_MARSHALLER = new HashMap<String, Marshaller>();

    private static final String ANONYMOUS_XRI = "";

    private static final Pattern WRITE_LOCK_PATTERN = Pattern.compile(
        "(" + SystemAttributes.CREATED_AT + "|" + SystemAttributes.MODIFIED_AT + ")=" +
            "([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}[.][0-9]{3})000Z"
    );

    /**
     * The clone fetch plan retrieves all attributes
     */
    private static final FetchPlan PROXY_CLONE_FETCH_PLAN = StandardFetchPlan.newInstance(null);
    
    /**
     * Isolate {@link Record} values
     * 
     * @param value the source value
     * @return the cloned value in case of a {@link Record}, the value itself in all other cases
     * @throws ServiceException 
     */
    @SuppressWarnings("unchecked")
    private Object isolate(Object value) throws ServiceException {
        if(value instanceof Record) {
            try {
                return ((Record)value).clone();
            } catch (CloneNotSupportedException exception) {
                throw new ServiceException(exception);
            }
        } else if (value instanceof List<?>){
            try {
                final IndexedRecord clone = Records.getRecordFactory().createIndexedRecord(Multiplicity.LIST.code());
                clone.addAll((List<?>)value);
                return clone;
            } catch (ResourceException exception) {
                throw new ServiceException(exception);
            }
        } else if (value instanceof Set<?>){
            try {
                final IndexedRecord clone = Records.getRecordFactory().createIndexedRecord(Multiplicity.SET.code());
                clone.addAll((Set<?>)value);
                return clone;
            } catch (ResourceException exception) {
                throw new ServiceException(exception);
            }
        } else {
            return value;
        }
    }

    /**
     * Valdidate the object's state and retrieve its class
     * 
     * @param that
     *            the object to be inspected
     * @param lenient
     *            an exception is thrown for hollow objects unless <code>lenient</code> is <code>true</code>
     * 
     * @return the object's class
     * 
     * @throws ServiceException
     */
    static String getRecordName(
        DataObject_1 that,
        boolean lenient
    )
        throws ServiceException {
        if (that.transactionalValuesRecordName == null && that.objIsHollow()) {
            if (lenient) {
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
    ) {
        return left == right || (left != null && right != null && left.equals(right));
    }

    /**
     * Tells whether the object represents an aspect
     * 
     * @return <code>true</code> if the object represents an aspect
     * 
     * @throws ServiceException
     */
    public boolean isAspect()
        throws ServiceException {
        return getModel().isInstanceof(this, "org:openmdx:base:Aspect");
    }

    /**
     * Tells whether the object is an instance of <code>org::openmdx::base::Modifiable</code>
     * 
     * @return <code>true</code> if the object is an instance of <code>org::openmdx::base::Modifiable</code>
     * 
     * @throws ServiceException
     */
    public boolean isModifiable()
        throws ServiceException {
        return getModel().isInstanceof(this, "org:openmdx:base:Modifiable");
    }

    /**
     * Tells whether the object is an instance of <code>org::openmdx::base::Creatable</code>
     * 
     * @return <code>true</code> if the object is an instance of <code>org::openmdx::base::Creatable</code>
     * 
     * @throws ServiceException
     */
    public boolean isCreatable()
        throws ServiceException {
        return getModel().isInstanceof(this, "org:openmdx:base:Creatable");
    }

    /**
     * Tells whether the object is an instance of <code>org::openmdx::base::ExtentCapable</code>
     * 
     * @return <code>true</code> if the object is an instance of <code>org::openmdx::base::ExtentCapable</code>
     * 
     * @throws ServiceException
     */
    private boolean isExtentCapable()
        throws ServiceException {
        return getModel().isInstanceof(this, "org:openmdx:base:ExtentCapable");
    }

    /**
     * Tells whether the object is an instance of Removable
     * 
     * @return <code>true</code> if the object is an instance of Removable
     * 
     * @throws ServiceException
     */
    public boolean isRemovable()
        throws ServiceException {
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
     * @param feature
     *            the feature name
     * 
     * @return <code>true</code> if the given feature name refers to an aspect's core reference
     * 
     * @throws ServiceException
     */
    private boolean isAspectHasCore(
        String feature
    )
        throws ServiceException {
        return SystemAttributes.CORE.equals(feature) && isAspect();
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
    ) {
        if (optional &&
            (this.dataObjectManager == null || this.dataObjectManager.isClosed())) {
            return null;
        } else {
            return this.getUnitOfWork().getState(this, optional);
        }
    }

    void setExistence(
        Path identity,
        boolean existence
    )
        throws ServiceException {
        UnitOfWork_1 unitOfWork = this.getUnitOfWork();
        if (unitOfWork.isActive()) {
            if (!identity.startsWith(this.identity) || identity.size() - this.identity.size() != 2) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "The given path does not refer to a potential child",
                    ExceptionHelper.newObjectIdParameter("object", this),
                    new BasicException.Parameter("child", identity),
                    new BasicException.Parameter("existence", existence)
                );
            }
            Set<String> notFound;
            TransactionalState_1 state = unitOfWork.getState(DataObject_1.this, existence);
            if (state == null) {
                notFound = null;
            } else {
                String feature = identity.getSegment(identity.size() - 2).toClassicRepresentation();
                Map<String, Set<String>> values = state.unavailability(existence);
                notFound = values.get(feature);
                if (notFound == null && !existence) {
                    values.put(
                        feature,
                        notFound = new HashSet<String>()
                    );
                }
            }
            if (notFound != null) {
                String qualifier = identity.getSegment(identity.size() - 1).toClassicRepresentation();
                if (existence) {
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
     * @return the object's model class
     *
     * @exception ServiceException
     *                if the information is unavailable
     */
    @Override
    public String objGetClass()
        throws ServiceException {
        if (this.transactionalValuesRecordName == null) {
            try {
                this.objRetrieve(
                    false, // reload
                    this.dataObjectManager.getFetchPlan(),
                    null, // features
                    false, // beforeImage
                    true // throwNotFoundException
                );
                this.transactionalValuesRecordName = this.persistentValues.getRecordName();
            } catch (NullPointerException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_AVAILABLE,
                    "Object class can't be determined",
                    new BasicException.Parameter("identity", this.identity)                
                );
            }
        }
        return this.transactionalValuesRecordName;
    }

    /**
     * Returns the object's identity.
     *
     * @return the object's identity;
     *         or null for transient objects
     */
    @Override
    public Path jdoGetObjectId() {
        return this.identity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoGetTransactionalObjectId()
     */
    @Override
    public UUID jdoGetTransactionalObjectId() {
        return this.transientObjectId;
    }

    /**
     * Returns a new set containing the names of the features in the default
     * fetch group.
     * <p>
     * The returned set is a copy of the original set, i.e. interceptors are
     * free to modify it before passing it on.
     *
     * @return the names of the features in the default fetch group
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<String> objDefaultFetchGroup()
        throws ServiceException {
        Set<String> result = new HashSet<String>();
        TransactionalState_1 state = this.getState(true);
        if (state != null) {
            result.addAll(state.values(true).keySet());
        } else if (this.transientValues != null) {
            result.addAll(this.transientValues.keySet());
        }
        if (!objIsHollow()) {
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
        ObjectRecord objectHolder
    )
        throws ServiceException {
        if (this.loadLock) {
            return null;
        } else
            try {
                this.loadLock = true;
                if (this.identity == null || this.identity.equals(objectHolder.getResourceIdentifier())) {
                    //
                    // Composite & Transient
                    //
                    if (this.persistentValues == null) {
                        this.persistentValues = objectHolder.getValue();
                        this.version = objectHolder.getVersion();
                    } else {
                        MappedRecord source = objectHolder.getValue();
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
                    DataObject_1 composite = this.dataObjectManager.getObjectById(objectHolder.getResourceIdentifier());
                    setInaccessibilityReason(
                        new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ILLEGAL_STATE,
                            "Shared object proxy had to be replaced by composite object proxy",
                            ExceptionHelper.newObjectIdParameter("shared", this),
                            new BasicException.Parameter("composite", objectHolder.getResourceIdentifier())

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
    ) {
        if (makeNonTransactional)
            try {
                this.getUnitOfWork().remove(this);
            } catch (Exception ignored) {
                // Just ignore it
            }
        this.clear();
        this.version = null;
        this.persistentValues = null;
        this.created = false;
        this.identity = null;
    }

    /**
     * Ask the persistence framework for the object's content
     * 
     * @param fetchPlan
     * @param features
     * @param refresh
     *            tells whether the (remote) object shall be
     *            refreshed before answering the query
     * @param throwNotFoundException
     */
    @SuppressWarnings("unchecked")
    DataObject_1 unconditionalLoad(
        FetchPlan fetchPlan,
        Set<String> features,
        boolean refresh,
        boolean throwNotFoundException
    )
        throws ServiceException {
        try {
            Query_2Facade query = Query_2Facade.forObjectId(this);
            if (fetchPlan != null) {
                query.setGroups(fetchPlan.getGroups());
            }
            if (features != null) {
                query.setFeatures(features);
            }
            query.setRefresh(refresh);
            query.setSize(Integer.valueOf(1));
            IndexedRecord reply = (IndexedRecord) this.dataObjectManager.getInteraction().execute(
                this.dataObjectManager.getInteractionSpecs().GET,
                query.getDelegate()
            );
            if (reply == null || reply.isEmpty()) {
                this.setNotFound(jdoGetObjectId());
            } else {
                this.setFound();
                return postLoad((ObjectRecord) reply.get(0));
            }
        } catch (ResourceException exception) {
            this.setInaccessibilityReason(new ServiceException(exception));
        } catch (ServiceException exception) {
            setInaccessibilityReason(exception);
        }
        if (throwNotFoundException || this.notFound == null) {
            throw getInaccessibilityReason(); // inaccessible
        } else {
            return null; // not found
        }
    }

    /**
     * Set the inaccessibility reason
     * 
     * @param inaccessabilityReason
     * 
     * @return the inaccessibility reason
     */
    private void setInaccessibilityReason(
        ServiceException inaccessabilityReason
    ) {
        this.inaccessabilityReason = inaccessabilityReason;
        this.notFound = (inaccessabilityReason.getExceptionCode() == BasicException.Code.NOT_FOUND) ? ANONYMOUS_XRI : null;
        makeInaccessable();
    }

    private void setNotFound(
        Path xri
    ) {
        this.notFound = xri == null ? ANONYMOUS_XRI : xri.toXRI();
        this.inaccessabilityReason = null; // lazily initialised
        makeInaccessable();
    }

    private void setFound() {
        this.notFound = null;
        this.inaccessabilityReason = null;
    }

    /**
     * @param inaccessabilityReason
     */
    private void makeInaccessable() {
        if (this.dataObjectManager == null) {
            this.invalidate(
                false // makeNonTransactional
            );
        } else if (this.notFound == null) {
            this.invalidate(
                true // makeNonTransactional
            );
        } else
            try {
                this.dataObjectManager.invalidate(
                    this.identity,
                    true // makeNonTransactional
                );
            } catch (ServiceException exception) {
                exception.log();
            }
    }

    /**
     * Evict the data object
     */
    public void evictUnconditionally() {
        clear();
        this.version = null;
        this.persistentValues = null;
    }

    /**
     * Refresh the data object
     * 
     * @throws ServiceException
     */
    void refreshUnconditionally()
        throws ServiceException {
        this.evictUnconditionally();
        Container_1 container = this.getContainer(true);
        if (container != null) {
            container.openmdxjdoEvict(false, true);
        }
        this.unconditionalLoad(
            this.dataObjectManager.getFetchPlan(),
            null, // features
            true, // refresh
            true // throwNotFoundException
        );
        evictReadLock();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#getContainer(boolean)
     */
    public Container_1 getContainer(
        final boolean lazily
    ) {
        if (this.container == null && this.jdoIsPersistent()) {
            final Path identity = this.jdoGetObjectId();
            final int size = identity.size() - 2;
            if (size > 0) {
                final Path parentId = identity.getPrefix(size);
                if (!lazily || this.dataObjectManager.containsKey(parentId)) {
                    final DataObject_1_0 parent = this.dataObjectManager.getObjectById(parentId, false);
                    if (!parent.objIsInaccessible())
                        try {
                            this.container = (Container_1) parent.objGetContainer(
                                identity.getSegment(size).toClassicRepresentation()
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
    ) {
        this.container = container;
    }

    /**
     * Determines whether the object is <code>TRANSIENT</code> or <code>PERSISTENT_NEW</code>
     * 
     * @return <code>true</code> if the object is <code>TRANSIENT</code> or <code>PERSISTENT_NEW</code>
     */
    protected boolean isTransientOrNew() {
        return !jdoIsPersistent() || jdoIsNew();
    }

    /**
     * Retrieve this object's persistent aspects
     * 
     * @return the persistent aspects
     * 
     * @throws ServiceException
     */
    @Override
    public Container_1_0 getAspects()
        throws ServiceException {
        Container_1 container = this.getContainer(false);
        if (container != null) {
            getAspects(container);
        }
        return this.aspects;
    }

    Container_1_0 getAspects(Container_1 container) {
        if (this.aspects == null || container != this.aspects.container()) {
            this.aspects = container.subMap(
                new Filter(
                    new IsInCondition(
                        Quantifier.THERE_EXISTS,
                        SystemAttributes.CORE,
                        true, // IS_IN,
                        this.jdoIsPersistent() ? this.jdoGetObjectId() : this.jdoGetTransactionalObjectId()
                    )
                )
            );
            if (this.aspects instanceof Selection_1) {
                final Selection_1 selection = (Selection_1) this.aspects;
                if (isTransientOrNew()) {
                    selection.markAsEmpty();
                }
            }

        }
        return this.aspects;
    }

    Container_1_0 aspects() {
        return this.aspects;
    }

    boolean isQualified() {
        return this.jdoIsPersistent() || this.qualifier != null;
    }

    String getQualifier() {
        return this.jdoIsPersistent() ? this.jdoGetObjectId().getLastSegment().toClassicRepresentation() : this.qualifier;
    }

    /**
     * The transient qualifier is used when an object is added without
     * qualifier to a transient container
     * 
     * @return the object's transient qualifier
     */
    String getPlaceHolder() {
        return ":" + this.transientObjectId;
    }

    /**
     * Retrieve the unit of work
     * 
     * @return the layer specific unit of work
     * 
     * @throws JDOUserException
     *             in case of an invalid object
     */
    public final UnitOfWork_1 getUnitOfWork() {
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
        return this.dataObjectManager.currentUnitOfWork();
    }

    /**
     * Retrieve the unit of work
     * 
     * @param featureName
     *            name of the feature to be marked dirty
     * 
     * @return the unit of work or {@code null} for non-transactional access
     * 
     * @throws ServiceException
     */
    final UnitOfWork_1 getUnitOfWorkIfTransactional(
        String featureName
    )
        throws ServiceException {
        final UnitOfWork_1 unitOfWork = this.getUnitOfWork();
        if (this.jdoIsPersistent()) {
            this.addTo(unitOfWork);
        } else if (!unitOfWork.isMember(this)) {
            return null;
        }
        TransactionalState_1 state = unitOfWork.getState(this, false);
        state.setPrepared(false);
        state.dirtyFeatures(false).add(featureName);
        return unitOfWork;
    }

    /**
     * Retrieve the unit of work
     * 
     * @param featureName
     *            name of the feature to be marked dirty
     * 
     * @return the unit of work or {@code null} for non-transactional access
     * 
     * @throws ServiceException
     */
    final UnitOfWork_1 getUnitOfWorkIfTransactional()
        throws ServiceException {
        final UnitOfWork_1 unitOfWork = this.getUnitOfWork();
        return unitOfWork.isMember(this) ? unitOfWork : null;
    }

    /**
     * After this call the object observes unit of work boundaries.
     * <p>
     * This method is idempotent.
     *
     * @exception ServiceException
     *                ILLEGAL_STATE
     *                if no unit of work is in progress
     * @exception ServiceException
     *                if the object can't be added to the unit of work for
     *                another reason.
     */
    void objMakeTransactional()
        throws ServiceException {
        this.addTo(getUnitOfWork());
    }

    /**
     * An object is touched either explicitly or when a non query operation is invoked
     * 
     * @throws ServiceException
     *             in case of failure
     */
    @Override
    public void touch()
        throws ServiceException {
        objMakeTransactional();
        this.getUnitOfWork().getState(this, false).touch();
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
    )
        throws ServiceException {
        if (unitOfWork.add(this)) {
            if (this.transientValues == null) {
                this.objRetrieve(
                    false, // reload
                    this.dataObjectManager.getFetchPlan(),
                    null, // features
                    false, // beforeImage
                    true // throwNotFoundException
                );
            } else {
                unitOfWork.getState(this, false).setValues(this.transientValues, this.jdoIsNew());
                this.transientValues = null;
            }
            if (unitOfWork.isReadLockRequired() &&
                this.jdoIsPersistent() && !this.jdoIsNew() &&
                isModifiable() && !isRemovable()) {
                this.lock = LockAssertions.newReadLockAssertion(unitOfWork.getTransactionTime());
            }
        }
    }

    /**
     * After this call the object ignores unit of work boundaries.
     * <p>
     * This method is idempotent.
     *
     * @exception ServiceException
     *                ILLEGAL_STATE
     *                if the object is dirty.
     * @exception ServiceException
     *                if the object can't be removed from its unit of work for
     *                another reason
     */
    void objMakeNontransactional()
        throws ServiceException {
        if (this.jdoIsDirty()) {
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
     * @param exclude
     *            the features not to be cloned
     *
     * @return the clone
     */
    @Override
    public DataObject_1_0 openmdxjdoClone(
        String... exclude
    ) {
        try {
            return new DataObject_1(
                this,
                false, // beforeImage
                exclude
            );
        } catch (Exception e) {
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
     * @param there
     *            the object's new container.
     * @param criteria
     *            The criteria is used to move the object to the container or
     *            <code>null</null>, in which case it is up to the
     *            implementation to define the criteria.
     *
     * &#64;exception ServiceException  ILLEGAL_STATE
     *            if the object is persistent.
     * &#64;exception ServiceException BAD_PARAMETER
     *            if <code>there</code> is not an instance of <code>DelegatingContainer</code>.
     * @exception ServiceException
     *                if the move operation fails.
     */
    @Override
    public void objMove(
        final Container_1_0 there,
        final String criteria
    )
        throws ServiceException {
        //
        // Validate the arguments
        //
        if (there == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The container is null"
            );
        }
        final Container_1_0 newContainer = there.container();
        if (this.jdoIsPersistent()) {
            final Container_1 oldContainer = this.getContainer(true);
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "Attempt to move a persistent object",
                ExceptionHelper.newObjectIdParameter("id", this),
                new BasicException.Parameter(
                    "old", ReducedJDOHelper.getTransactionalObjectId(oldContainer), ReducedJDOHelper.getObjectId(oldContainer)
                ),
                new BasicException.Parameter(
                    "new", ReducedJDOHelper.getTransactionalObjectId(newContainer), ReducedJDOHelper.getObjectId(newContainer)
                )
            );
        }
        this.container = (Container_1) newContainer;
        //
        // Set the container
        //
        if (this.container.jdoIsPersistent()) {
            String qualifier = criteria;
            for (PlugIn_1_0 plugIn : this.dataObjectManager.getPlugIns()) {
                qualifier = plugIn.getQualifier(this, qualifier);
            }
            if (qualifier == null) {
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
            if (ClassicSegments.isPlaceholder(qualifier)) {
                if (isProxy()) {
                    this.objMakeTransactional();
                    TransactionalState_1 state = this.getState(false);
                    state.setLifeCycleEventPending(true);
                    this.identity = identity;
                    this.created = true;
                    this.dataObjectManager.currentUnitOfWork().flush(false);
                    flushed = true;
                    qualifier = identity.getLastSegment().toClassicRepresentation();
                } else {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        "The qualifier must be neither null nor a place holder unless the container is a proxy",
                        new BasicException.Parameter("container", container.jdoGetObjectId()),
                        new BasicException.Parameter("qualifier", qualifier),
                        new BasicException.Parameter("proxy", Boolean.FALSE)
                    );
                }
            } else if (this.dataObjectManager.containsKey(identity)) {
                DataObject_1_0 collision = this.dataObjectManager.getObjectById(identity, false);
                if (collision != this) {
                    try {
                        collision.objGetClass();
                    } catch (Exception exception) {
                        // The candidate could be evicted now
                    }
                    if (this.dataObjectManager.containsKey(identity)) {
                        this.getState(false).setLifeCycleEventPending(false);
                        this.container = null;
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.DUPLICATE,
                            "There is already an object with the same qualifier in the container",
                            new BasicException.Parameter(BasicException.Parameter.XRI, identity)
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
            for (Flushable flushable : this.flushableValues.values()) {
                if (flushable instanceof Container_1) {
                    this.makePersistent(
                        (Container_1) flushable,
                        false // aspect
                    );
                    this.makePersistent(
                        (Container_1) flushable,
                        true // aspect
                    );
                } else if (flushable instanceof ManagedAspect) {
                    ManagedAspect managedAspect = (ManagedAspect) flushable;
                    managedAspect.evict();
                    if (this.created) {
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
            if (criteria == null) {
                this.container.addToCache(
                    this.getPlaceHolder(),
                    this
                );
            } else {
                this.container.addToCache(
                    this.qualifier = criteria,
                    this
                );
                for (Flushable flushable : this.flushableValues.values()) {
                    if (flushable instanceof ManagedAspect) {
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
    private String getCacheKey() {
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
     * @param identity
     *            the object id
     * @param flushed
     *            <code>true</code> if the object is alread flushed
     * @throws ServiceException
     */
    public void makePersistent(
        Path identity,
        boolean flushed
    )
        throws ServiceException {
        if (!flushed) {
            TransactionalState_1 state = this.getState(false);
            state.setLifeCycleEventPending(true);
            this.created = true;
            this.identity = identity;
        }
        if (this.transientObjectId == null) {
            this.dataObjectManager.putUnlessPresent(identity, this);
        } else {
            this.dataObjectManager.move(this.transientObjectId, identity);
        }
        if (!flushed) {
            this.objMakeTransactional();
        }
        if (!this.untouchable) {
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
    )
        throws ServiceException {
        Map<String, DataObject_1_0> children = new LinkedHashMap<String, DataObject_1_0>();
        for (Map.Entry<String, DataObject_1_0> child : descendants.entrySet()) {
            final DataObject_1_0 value = child.getValue();
            if (!value.jdoIsPersistent()) {
                final String key = child.getKey();
                if (aspect == ClassicSegments.getCoreComponentFromAspectQualifierPlaceholder(key).isPresent()) {
                    children.put(
                        key,
                        value
                    );
                }
            }
        }
        for (Map.Entry<String, DataObject_1_0> child : children.entrySet()) {
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
    ModelElement_1_0 getClassifier()
        throws ServiceException {
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
     * @exception ServiceException
     *                NOT_SUPPORTED
     *                If the object refuses to be removed.
     * @exception ServiceException
     *                if the object can't be removed
     */
    void objRemove(
        boolean updateCache
    )
        throws ServiceException {
        objRemove();
    }

    /**
     * Removes an object.
     * <p>
     * Neither <code>getValue()</code> nor <code>setValue()</code>
     * calls are allowed after an <code>remove()</code> invocation and
     * <code>isDeleted()</code> will return <code>true</code> unless the
     * object has been transient.
     * 
     * @exception ServiceException
     *                NOT_SUPPORTED
     *                If the object refuses to be removed.
     * @exception ServiceException
     *                if the object can't be removed
     */
    void objRemove()
        throws ServiceException {
        Model_1_0 model = getModel();
        //
        // Cascade Removal To Children
        //
        if (!this.jdoIsPersistent()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "Attempt to remove a transient object"
            );
        }
        if (this.jdoIsNew()) {
            for (Flushable flushable : this.flushableValues.values()) {
                if (flushable instanceof Container_1) {
                    for (DataObject_1_0 child : ((Container_1) flushable).values()) {
                        //
                        // Be aware that an aspect could have been removed by its core for example
                        //
                        if (child instanceof DataObject_1) {
                            ((DataObject_1) child).objRemove();
                        }
                    }
                }
            }
        } else {
            CascadeDelete: for (PlugIn_1_0 plugIn : this.dataObjectManager.getPlugIns()) {
                if (plugIn.requiresCallbackOnCascadedDelete(this)) {
                    for (Map.Entry<String, ModelElement_1_0> e : getClassifier().objGetMap("allFeature").entrySet()) {
                        ModelElement_1_0 featureDef = e.getValue();
                        if (model.isReferenceType(featureDef)) {
                            if (AggregationKind.COMPOSITE.equals(model.getElement(featureDef.getReferencedEnd()).getAggregation())) {
                                this.dataObjectManager.deletePersistentAll(
                                    this.objGetContainer(e.getKey()).values()
                                );
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
        if (model.isInstanceof(this, "org:openmdx:base:AspectCapable") &&
            !model.isInstanceof(this, "org:openmdx:base:Aspect")) {
            this.getAspects().clear();
        }
        Container_1 container = this.getContainer(true);
        if (container != null) {
            container.removeFromChache(this.identity.getLastSegment().toClassicRepresentation());
        }
    }

    /**
     * Prepare the object for flushing
     * 
     * @throws ServiceException
     */
    void prepare()
        throws ServiceException {
        if (!this.untouchable && this.jdoIsDirty() && !this.jdoIsDeleted()) {
            this.dataObjectManager.fireInstanceCallback(this, InstanceLifecycleEvent.STORE, false);
        }
        this.getState(false).setPrepared(true);
    }

    /**
     * Propagate new instances if necessary
     * 
     * @exception ServiceException
     *                if the object can't be flushed
     * @throws ResourceException
     */
    void propagate(
        Interaction interaction
    )
        throws ServiceException,
        ResourceException {
        TransactionalState_1 state = this.getState(false);
        if (state.isLifeCycleEventPending()) {
            ObjectRecord input;
            if (!state.isFlushed()) {
                final String base = this.identity.getLastSegment().toClassicRepresentation();
                if (ClassicSegments.isPlaceholder(base)) {
                    input = Records.getRecordFactory().createMappedRecord(ObjectRecord.class);
                    input.setTransientObjectId(this.jdoGetTransactionalObjectId());
                    if (this.transactionalValuesRecordName != null) {
                        input.setValue(
                            Records.getRecordFactory().createMappedRecord(this.transactionalValuesRecordName)
                        );
                    }
                    interaction.execute(
                        this.dataObjectManager.getInteractionSpecs().CREATE,
                        input
                    );
                    state.setFlushed(true);
                }
            }
            input = Records.getRecordFactory().createMappedRecord(ObjectRecord.class);
            input.setResourceIdentifier(this.identity);
            if (this.transactionalValuesRecordName != null) {
                input.setValue(
                    Records.getRecordFactory().createMappedRecord(this.transactionalValuesRecordName)
                );
            }
            if (state.isFlushed()) {
                input.setTransientObjectId(this.transientObjectId);
                Record reply = interaction.execute(
                    this.dataObjectManager.getInteractionSpecs().MOVE,
                    input
                );
                final ObjectRecord persistent = (ObjectRecord) ((IndexedRecord) reply).get(0);
                this.identity = persistent.getResourceIdentifier();
            } else {
                interaction.execute(
                    InteractionSpecs.getRestInteractionSpecs(false).CREATE,
                    input
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
     * @exception ServiceException
     *                if the object can't be flushed
     */
    void flush(
        Interaction interaction,
        boolean beforeCompletion
    )
        throws ServiceException {
        TransactionalState_1 state = this.getState(false);
        if (beforeCompletion && !this.untouchable && jdoIsPersistent() && !isProxy()) {
            validate(state);
        }
        flushStructuralFeatures(interaction, state);
        flushBehaviouralFeatures(interaction, state);
        state.setDirtyFeaturesFlushed();
        if (!beforeCompletion) {
            evictReadLock();
            evictWriteLock();
        }
    }

    /**
     * Validate the object before it is flushed to the data store
     * 
     * @throws ServiceException
     */
    private void validate(
        TransactionalState_1 state
    )
        throws ServiceException {
        if (jdoIsDeleted())
            return;
        Collection<String> missing = null;
        ModelElement_1_0 classifier = getClassifier();
        if (jdoIsNew()) {
            Map<String, ModelElement_1_0> attributes = classifier.getModel().getAttributeDefs(
                classifier,
                false, // sub-types
                true // includeDerived
            );
            for (Map.Entry<String, ModelElement_1_0> attribute : attributes.entrySet()) {
                if (cardinalityIsInvalid(classifier, attribute.getValue())) {
                    if (missing == null) {
                        missing = new ArrayList<String>();
                    }
                    missing.add(attribute.getKey());
                }
            }
        } else if (jdoIsDirty()) {
            Map<String, ModelElement_1_0> attributes = getAttributes();
            for (String feature : state.dirtyFeatures(true)) {
                ModelElement_1_0 attribute = attributes.get(feature);
                if (cardinalityIsInvalid(classifier, attribute)) {
                    if (missing == null) {
                        missing = new ArrayList<String>();
                    }
                    missing.add(feature);
                }
            }
        }
        if (missing != null) {
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
     * Tells whether the feature's cardinality is invalid
     * 
     * @param classDef
     * @param featureDef
     *
     * @return <code>true</code> if a mandatory feature is missing
     *
     * @throws ServiceException
     */
    private boolean cardinalityIsInvalid(
        ModelElement_1_0 classDef,
        ModelElement_1_0 featureDef
    )
        throws ServiceException {
        if (isMandatory(featureDef) && !dataObjectManager.isExemptFromValidation(this, featureDef) && isPersistent(featureDef)) {
            String featureName = featureDef.getName();
            if (!ModelHelper.isFeatureHeldByCore(classDef, featureName)) {
                return this.objGetValue(featureName) == null;
            }
        }
        return false;
    }

    /**
     * Initialize a newly created object record
     * 
     * @param input
     * @param values
     * 
     * @return the initialize facade
     * @throws ResourceException
     */
    private ObjectRecord newInput(
        boolean transactionalId,
        MappedRecord values
    )
        throws ResourceException {
        ObjectRecord input = Records.getRecordFactory().createMappedRecord(ObjectRecord.class);
        if (transactionalId) {
            input.setTransientObjectId(this.jdoGetTransactionalObjectId());
        } else {
            input.setResourceIdentifier(this.jdoGetObjectId());
        }
        input.setVersion(this.jdoGetVersion());
        input.setLock(this.lock);
        if (values != null) {
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
    )
        throws ServiceException {
        try {
            if (!jdoIsPersistent() && state.isLifeCycleEventPending()) {
                Map<String, Object> transactionalValues = state.values(false);
                if (this.persistentValues == null) {
                    this.persistentValues = newRecord(this.transactionalValuesRecordName);
                }
                for (Map.Entry<String, Object> e : transactionalValues.entrySet()) {
                    String feature = e.getKey();
                    Object source = e.getValue();
                    Flushable flushable = this.flushableValues.get(feature);
                    if (flushable != null) {
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
                final ObjectRecord input = newInput(
                    true,
                    this.persistentValues
                );
                interaction.execute(
                    this.dataObjectManager.getInteractionSpecs().CREATE,
                    input
                );
                state.setLifeCycleEventPending(false);
                state.setFlushed(true);
            } else if (this.jdoIsDeleted()) {
                if (state.isLifeCycleEventPending()) {
                    if (!this.jdoIsNew() || state.isFlushed()) {
                        String objectClass = DataObject_1.getRecordName(this, false);
                        final ObjectRecord input = newInput(
                            false,
                            objectClass == null ? null : Records.getRecordFactory().createMappedRecord(objectClass)
                        );
                        interaction.execute(
                            this.dataObjectManager.getInteractionSpecs().DELETE,
                            input
                        );
                        state.setFlushed(false);
                    }
                    state.setLifeCycleEventPending(false);
                }
            } else if (this.jdoIsNew() && state.isLifeCycleEventPending()) {
                Map<String, Object> transactionalValues = state.values(false);
                if (this.persistentValues == null) {
                    this.persistentValues = newRecord(this.transactionalValuesRecordName);
                }
                for (Map.Entry<String, Object> e : transactionalValues.entrySet()) {
                    String feature = e.getKey();
                    Object source = e.getValue();
                    Flushable flushable = this.flushableValues.get(feature);
                    if (flushable == null) {
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
                final ObjectRecord input;
                if (this.isProxy()) {
                    if (ClassicSegments.isPlaceholder(this.identity.getLastSegment().toClassicRepresentation())) {
                        input = newInput(
                            true,
                            this.persistentValues
                        );
                        interaction.execute(
                            this.dataObjectManager.getInteractionSpecs().CREATE,
                            input
                        );
                        input.setResourceIdentifier(this.identity);
                        input.setTransientObjectId(this.transientObjectId);
                        Record reply = interaction.execute(
                            this.dataObjectManager.getInteractionSpecs().MOVE,
                            input
                        );
                        final ObjectRecord persistent = (ObjectRecord) ((IndexedRecord) reply).get(0);
                        this.identity = persistent.getResourceIdentifier();
                    } else {
                        input = newInput(
                            false,
                            this.persistentValues
                        );
                        interaction.execute(
                            this.dataObjectManager.getInteractionSpecs().UPDATE,
                            input
                        );
                    }
                    state.setFlushed(true);
                } else {
                    input = newInput(
                        false,
                        this.persistentValues
                    );
                    if (state.isFlushed()) {
                        interaction.execute(
                            this.dataObjectManager.getInteractionSpecs().UPDATE,
                            Records.getRecordFactory().singletonMappedRecord(
                                Multiplicity.MAP.code(),
                                null, // recordShortDescription
                                this.transientObjectId,
                                input
                            )
                        );
                    } else {
                        interaction.execute(
                            this.dataObjectManager.getInteractionSpecs().CREATE,
                            input
                        );
                    }
                }
                this.transientOnRollback = true;
                state.setLifeCycleEventPending(false);
            } else if ((this.jdoIsDirty() || (this.jdoIsPersistent() && this.version != null)) &&
                this.jdoGetObjectId().size() > 4 // exclude Authorities and Providers
            ) {
                MappedRecord beforeImage = this.persistentValues;
                this.persistentValues = newRecord(this.objGetClass());
                Map<String, Object> transactionalValues = state.values(false);
                for (String feature : state.dirtyFeatures(true)) {
                    Flushable flushable = this.flushableValues.get(feature);
                    if (flushable != null) {
                        try {
                            flushable.flush();
                        } catch (IOException exception) {
                            throw new ServiceException(exception);
                        }
                    } else if (transactionalValues.containsKey(feature)) {
                        Object source = transactionalValues.get(feature);
                        if (source instanceof Flushable) {
                            try {
                                ((Flushable) source).flush();
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
                final ObjectRecord input = newInput(
                    false,
                    this.persistentValues
                );
                this.persistentValues = beforeImage;
                interaction.execute(
                    this.dataObjectManager.getInteractionSpecs().UPDATE,
                    input
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
        TransactionalState_1 state
    )
        throws ServiceException {
        Queue<Operation> operations = state.operations(true);
        for (Operation operation = operations.poll(); operation != null; operation = operations.poll()) {
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
    )
        throws ServiceException {
        if (this.beforeImage == null)
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Before image should have been set"
            );
        Multiplicity multiplicity = ModelHelper.getMultiplicity(feature);
        String featureName = feature.getName();
        switch (multiplicity) {
            case SINGLE_VALUE:
            case OPTIONAL:
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
                    ReducedJDOHelper.getAnyObjectId(this)
                );
                return true;

            default:
                SysLog.log(
                    Level.WARNING,
                    "Unsupported Multiplicity {0}, treat the feature {1} in the object {2} as modified",
                    multiplicity,
                    featureName,
                    ReducedJDOHelper.getAnyObjectId(this)
                );
                return true;
        }
    }

    /**
     * Tests whether some non-derived features have been modified
     * and removes idempotent modifications
     * 
     * @param beforeImage
     *            the object's before-image
     * 
     * @return <code>true</code> if some non-derived features have been modified
     * 
     * @throws ServiceException
     */
    public boolean thereRemainDirtyFeaturesAfterRemovingTheUnmodifiedOnes(
        DataObject_1_0 beforeImage
    )
        throws ServiceException {
        TransactionalState_1 state = this.getState(false);
        Map<String, ModelElement_1_0> attributes = getAttributes();
        boolean dirty = state.isTouched();
        Set<String> dirtyFeatures = state.dirtyFeatures(true);
        for (Iterator<String> i = dirtyFeatures.iterator(); i.hasNext();) {
            ModelElement_1_0 attribute = attributes.get(i.next());
            if (!this.isFeatureModified(attribute)) {
                i.remove();
            } else if (!ModelHelper.isDerived(attribute)) {
                dirty = true;
            }
        }
        if (!dirty) {
            detectConcurrentModification(beforeImage);
        }
        return dirty;
    }

    /**
     * Retrieve the object's structural features
     * 
     * @return the object's structural features
     * 
     * @throws ServiceException
     */
    private Map<String, ModelElement_1_0> getAttributes()
        throws ServiceException {
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
     * @param attribute
     *            the attribute to be tested
     * 
     * @return <code>true</code> if the attribute is persistent
     * 
     * @throws ServiceException
     */
    private static boolean isPersistent(
        ModelElement_1_0 attribute
    )
        throws ServiceException {
        return Persistency.getInstance().isPersistentAttribute(attribute);
    }

    /**
     * Tells whether an attribute is streamed
     * 
     * @param attribute
     *            the attribute to be tested
     * 
     * @return <code>true</code> if the attribute is streamed
     * 
     * @throws ServiceException
     */
    private static boolean isStreamed(
        ModelElement_1_0 attribute
    )
        throws ServiceException {
        return isStreamedValue(attribute);
    }

    /**
     * Tells whether an attribute is mandatory
     * 
     * @param attribute
     *            the attribute to be tested
     * 
     * @return <code>true</code> if the attribute is mandatory
     * 
     * @throws ServiceException
     */
    private static boolean isMandatory(
        ModelElement_1_0 attribute
    )
        throws ServiceException {
        return ModelHelper.getMultiplicity(attribute) == Multiplicity.SINGLE_VALUE;
    }

    /**
     * Provide the before image
     * 
     * @return the before image
     * 
     * @throws ServiceException
     */
    public DataObject_1 getBeforeImage()
        throws ServiceException {
        if (this.jdoIsNew())
            return null;
        if (this.beforeImage == null || this.beforeImage.objIsInaccessible()) {
            this.beforeImage = new DataObject_1(
                this,
                true
            );
        }
        return this.beforeImage;
    }

    public void makePersistentCleanWhenUnmodified()
        throws ServiceException {
        thereRemainDirtyFeaturesAfterRemovingTheUnmodifiedOnes(getBeforeImage());
        evictBeforeImage(); // free memory

    }

    /**
     * Check the locks before releasing the object from the unit of work
     * 
     * @param beforeImage
     *            the object's before image
     * 
     * @throws ServiceException
     */
    private void detectConcurrentModification(
        DataObject_1_0 beforeImage
    )
        throws ServiceException {
        String writeLockAssertion = getWriteLockAssertion();
        if (writeLockAssertion != null) {
            assertWriteLock(beforeImage, writeLockAssertion);
        }
        if (LockAssertions.isReadLockAssertion(this.lock)) {
            assertReadLock(beforeImage, LockAssertions.getTransactionTime(this.lock));
        }
    }

    /**
     * Assert that the object has not been updated concurrently through another persistence manager during this unit of work
     * 
     * @param beforeImage
     *            the object's before image
     * @param lockValue
     *            the read lock value
     * 
     * @throws ServiceException
     */
    private void assertReadLock(
        DataObject_1_0 beforeImage,
        Date lockValue
    )
        throws ServiceException {
        Date currentValue = (Date) beforeImage.objGetValue(SystemAttributes.MODIFIED_AT);
        if (currentValue != null) {
            if (currentValue.after(lockValue)) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                    "Object has been updated by another persistence manager during this unit of work",
                    new BasicException.Parameter(BasicException.Parameter.XRI, ReducedJDOHelper.getAnyObjectId(this)),
                    new BasicException.Parameter("lockAssertion", this.lock),
                    new BasicException.Parameter(SystemAttributes.MODIFIED_AT, DateTimeFormat.EXTENDED_UTC_FORMAT.format(currentValue))
                );
            }
        }
    }

    /**
     * Assert that the object has not been updated concurrently through another persistence manager since it has been cached
     * 
     * @param beforeImage
     *            the object's before image
     * @param lockAssertion
     *            the write lock assertion
     * 
     * @throws ServiceException
     */
    private void assertWriteLock(
        DataObject_1_0 beforeImage,
        String lockAssertion
    )
        throws ServiceException {
        Matcher lockMatcher = WRITE_LOCK_PATTERN.matcher(lockAssertion);
        if (lockMatcher.matches())
            try {
                String lockFeature = lockMatcher.group(1);
                Date currentValue = (Date) beforeImage.objGetValue(lockFeature);
                if (currentValue != null) {
                    Date lockValue = DateTimeFormat.EXTENDED_UTC_FORMAT.parse(lockMatcher.group(2) + "Z");
                    if (!lockValue.equals(currentValue)) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                            "Object has been updated by another persistence manager since it has been cached",
                            new BasicException.Parameter(BasicException.Parameter.XRI, ReducedJDOHelper.getAnyObjectId(this)),
                            new BasicException.Parameter("lockAssertion", lockAssertion),
                            new BasicException.Parameter(lockFeature, DateTimeFormat.EXTENDED_UTC_FORMAT.format(currentValue))
                        );
                    }
                }
            } catch (ParseException exception) {
                SysLog.warning("Unable to validate lock assertion for object " + ReducedJDOHelper.getAnyObjectId(this), lockAssertion);
            }
    }

    private String getWriteLockAssertion()
        throws ServiceException {
        if (this.version instanceof byte[]) {
            try {
                return new String((byte[]) this.version, "UTF-8");
            } catch (UnsupportedEncodingException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "UTF-8 should be supported"
                );
            }
        } else {
            return null;
        }
    }

    void afterCompletion(
        Status status
    )
        throws ServiceException {
        switch (status) {
            case STATUS_COMMITTED:
                if (this.jdoIsDeleted()) {
                    if (this.jdoIsPersistent()) {
                        this.dataObjectManager.invalidate(
                            this.identity,
                            false // makeNonTransactional
                        );
                    }
                    this.deleted = false;
                    this.identity = null;
                }
                break;
            case STATUS_ROLLEDBACK:
                this.getState(false).setLifeCycleEventPending(false);
                if (this.jdoIsDeleted()) {
                    this.deleted = false;
                }
                if (this.transientOnRollback || this.jdoIsNew()) {
                    if (this.dataObjectManager != null) {
                        this.dataObjectManager.makeTransient(this);
                    }
                    if (this.container != null) {
                        this.container.removeFromChache(this.identity.getLastSegment().toClassicRepresentation());
                        this.container = null;
                        this.aspects = null;
                    }
                    this.identity = null;
                }
                break;
            default:
                break;
        }
        Container_1 container = this.getContainer(true);
        if (container != null) {
            container.openmdxjdoEvict(false, true);
        }
        this.transientOnRollback = false;
        this.created = false;
        this.beforeImage = null;
    }

    /**
     * Reset the read lock
     */
    private void evictReadLock() {
        this.lock = null;
    }

    /**
     * Reset the write lock
     */
    private void evictWriteLock() {
        this.version = null;
    }

    /**
     * Evict the data object
     */
    void evict() {
        boolean clear;
        if (this.jdoIsDirty()) {
            clear = false; // to not to lose changes
        } else if (this.jdoIsPersistent()) {
            try {
                this.dataObjectManager.fireInstanceCallback(this, InstanceLifecycleEvent.CLEAR, true);
            } catch (ServiceException exception) {
                throw new JDOFatalInternalException(
                    "Lenient instance callback exception should have been logged and not propagated",
                    exception.getCause()
                );
            }
            clear = true;
        } else if (this.isProxy()) {
            clear = true;
        } else {
            clear = false;
        }
        if (clear) {
            this.clear();
            for (Map.Entry<String, Flushable> entry : this.flushableValues.entrySet()) {
                Flushable value = entry.getValue();
                if (value instanceof ManagedAspect) {
                    ((ManagedAspect) value).evict();
                } else if (value instanceof PersistenceCapableCollection) {
                    ((PersistenceCapableCollection) value).openmdxjdoEvict(
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
        if (this.beforeImage != null && !this.beforeImage.jdoIsPersistent()) {
            this.beforeImage.evictUnconditionally();
            this.beforeImage = null;
        }
    }

    private void clear() {
        try {
            TransactionalState_1 state = getState(true);
            if (state != null) {
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
     *         "remote" counterpart
     */
    final boolean isOutOfSync() {
        TransactionalState_1 state = this.getState(true);
        return state != null && state.isOutOfSync();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objThreadSafetyRequired()
     */
    @Override
    public boolean objThreadSafetyRequired() {
        return this.dataObjectManager.isThreadSafetyRequired();
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
    @Override
    public boolean jdoIsDirty() {
        if (this.jdoIsNew() || this.jdoIsDeleted()) {
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
    @Override
    public boolean jdoIsPersistent() {
        return this.identity != null;
    }

    /**
     * Tells whether the object is contained in a container
     * 
     * @return <code>true</code> if the object is either persistent or has
     *         already been moved to a container
     */
    @Override
    public boolean objIsContained() {
        return this.jdoIsPersistent() || this.container != null;
    }

    /**
     * Tests whether this object has been newly made persistent. Instances
     * that have been made persistent in the current unit of work return true.
     * <p>
     * Transient instances return false.
     *
     * @return true if this instance was made persistent in the current unit
     *         of work.
     */
    @Override
    public boolean jdoIsNew() {
        return this.created;
    }

    /**
     * Tests whether this object is hollow
     * 
     * @return <code>true</code> if the object is hollow
     */
    protected boolean objIsHollow() {
        return this.persistentValues == null;
    }

    /**
     * Tests whether this object is a proxy
     * 
     * @return <code>true</code> if this object is a proxy
     */
    public boolean isProxy() {
        return this.dataObjectManager.isProxy();
    }

    /**
     * Tests whether this object is a virtual object
     * 
     * @return <code>true</code> if this object is a virtual object
     */
    private boolean isVirtual() {
        return ObjectRecords.isVirtualObjectVersion(this.version);
    }

    /**
     * Tests whether this object becomes transient on rollback.
     *
     * @return true if this instance becomes transient on rollback
     */
    public boolean isTransientOnRollback() {
        return jdoIsNew();
    }

    /**
     * Tests whether this object has been deleted. Instances that have been
     * deleted in the current unit of work return true.
     * Transient instances return false.
     *
     * @return true if this instance was deleted in the current unit of work.
     */
    @Override
    public boolean jdoIsDeleted() {
        return this.deleted;
    }

    /**
     * Tests whether this object belongs to the current unit of work.
     *
     * @return true if this instance belongs to the current unit of work.
     */
    @Override
    public boolean jdoIsTransactional() {
        try {
            return this.getUnitOfWork().isMember(this);
        } catch (Exception e) {
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
     * @return true if this instance is inaccessible
     */
    @Override
    public boolean objIsInaccessible() {
        return this.notFound != null || this.inaccessabilityReason != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#objDoesNotExist()
     */
    @Override
    public boolean objDoesNotExist() {
        if (this.jdoIsPersistent() && objIsHollow())
            try {
                this.objRetrieve(
                    false, // reload
                    this.dataObjectManager.getFetchPlan(),
                    null, // features
                    false, // beforeImage
                    false // throwNotFoundException
                );
            } catch (ServiceException exception) {
                return exception.getExceptionCode() == BasicException.Code.NOT_FOUND;
            }
        return this.notFound != null;
    }

    /**
     * Retrieve the reason for the objects inaccessibility
     *
     * @return Returns the inaccessibility reason
     */
    @Override
    public ServiceException getInaccessibilityReason() {
        if (this.notFound != null && this.inaccessabilityReason == null) {
            this.inaccessabilityReason = new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND,
                "No object found with the given id",
                new BasicException.Parameter("object", this.jdoGetTransactionalObjectId(), this.notFound)
            );
        }
        return this.inaccessabilityReason;
    }

    /**
     * Ensure that the object is read enabled
     * 
     * @param reload
     * @param fetchPlan
     * @param features
     * @param beforeImage
     *            load the complete before image
     * @param throwNotFoundException
     *            if <code>false</code> return <code>null</code>
     *            instead of throwing an exception if the object does not exist
     * 
     * @return the retrieved object
     * 
     * @exception ServiceException
     *                <ul>
     *                <li>ILLEGAL_STATE if the object is deleted
     *                <li>NOT_FOUND if the object does not exist and throwNotFoundException is <code>true</code>
     *                </ul>
     */
    @SuppressWarnings("unchecked")
    DataObject_1 objRetrieve(
        boolean reload,
        FetchPlan fetchPlan,
        Set<String> features,
        boolean beforeImage,
        boolean throwNotFoundException
    )
        throws ServiceException {
        if (objIsInaccessible()) {
            if (throwNotFoundException || notFound == null) {
                throw getInaccessibilityReason();
            } else {
                return null;
            }
        } else if (reload || this.objIsHollow()) {
            if (isProxy() && !isVirtual()) {
                this.dataObjectManager.currentUnitOfWork().synchronize();
                return unconditionalLoad(
                    fetchPlan == null ? PROXY_CLONE_FETCH_PLAN : fetchPlan,
                    features,
                    false, // refresh
                    throwNotFoundException
                );
            } else if (jdoIsPersistent() && !jdoIsNew()) {
                return unconditionalLoad(
                    fetchPlan,
                    features,
                    false, // refresh
                    throwNotFoundException
                );
            } else {
                return this;
            }
        } else if (fetchPlan == null) {
            Set<String> fetched = beforeImage ? (objIsHollow() ? Collections.emptySet() : persistentValues.keySet())
                : objDefaultFetchGroup();
            Set<String> missing = null;
            for (Map.Entry<String, ModelElement_1_0> attribute : getAttributes().entrySet()) {
                String attributeName = attribute.getKey();
                if (!fetched.contains(attributeName) &&
                    isPersistent(attribute.getValue()) &&
                    !isStreamed(attribute.getValue())) {
                    if (missing == null) {
                        missing = new HashSet<String>();
                    }
                    missing.add(attributeName);
                }
            }
            if (missing != null || (fetchPlan != null && !fetchPlan.getGroups().isEmpty())) {
                try {
                    MappedRecord persistentValues = newRecord(
                        this.persistentValues.getRecordName()
                    );
                    persistentValues.putAll(this.persistentValues);
                    List<OrderSpecifier> orders = new ArrayList<>();
                    for (String feature : missing) {
                        orders.add(
                            new OrderSpecifier(feature, SortOrder.UNSORTED)
                        );
                    }
                    Query_2Facade input = Query_2Facade.forObjectId(this);
                    if(fetchPlan != null) {
                        input.setGroups(fetchPlan.getGroups());
                    }
                    input.setQueryFilter(
                        new Filter(
                            null, // condition
                            orders,
                            null // extension
                        )
                    );
                    IndexedRecord indexedRecord = (IndexedRecord) this.dataObjectManager.getInteraction().execute(
                        this.dataObjectManager.getInteractionSpecs().GET,
                        input.getDelegate()
                    );
                    if (indexedRecord.isEmpty())
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_FOUND,
                            "Could not fetch missing attributes",
                            ExceptionHelper.newObjectIdParameter("id", this),
                            new BasicException.Parameter("feature", missing)
                        );
                    ObjectRecord output = (ObjectRecord) indexedRecord.get(0);
                    persistentValues.putAll(
                        output.getValue()
                    );
                    this.persistentValues = persistentValues;
                } catch (ResourceException exception) {
                    throw new ServiceException(exception);
                }
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
     * @param name
     *            the attribute's name
     * @return <code>true</code> if the given attribute has to be loaded
     * 
     * @throws ServiceException
     */
    private boolean attributeMustBeLoaded(
        String name
    )
        throws ServiceException {
        return this.attributeIsRetrievedAndMustBeLoaded(name) && !this.persistentValues.containsKey(name);
    }

    /**
     * Test whether the given attribute has to be loaded
     * 
     * @param name
     *            the attribute's name
     * 
     * @return <code>true</code> if the given attribute has to be loaded
     * 
     * @throws ServiceException
     */
    private boolean attributeIsRetrievedAndMustBeLoaded(
        String name
    )
        throws ServiceException {
        if (this.jdoIsPersistent() && !this.jdoIsNew()) {
            this.objRetrieve(
                false, // reload
                this.dataObjectManager.getFetchPlan(),
                Collections.singleton(name),
                false,
                true // throwNotFoundException
            );
            return true;
        } else if (this.isProxy() && Boolean.TRUE.equals(this.getFeature(name).isDerived())) {
            this.objMakeTransactional();
            TransactionalState_1 state = this.getState(false);
            if (!state.isFlushed()) {
                state.setLifeCycleEventPending(true);
            }
            this.objRetrieve(
                true, // reload
                this.dataObjectManager.getFetchPlan(),
                Collections.singleton(name),
                false,
                true // throwNotFoundException
            );
            return true;
        } else {
            return false;
        }
    }

    /**
     * Ensure that the attribute is fetched.
     *
     * @param name
     *            attribute name
     * @param stream
     *            defines whether the value is a stream or not
     * @param clear
     *            tells whether a collection shall be cleared
     * 
     * @exception ServiceException
     *                ILLEGAL_STATE
     *                if the object is deleted
     */
    final Object getPersistentAttribute(
        String name,
        boolean stream,
        Multiplicity multiplicity,
        boolean clear
    )
        throws ServiceException {
        final Object rawValue = retrievePersistentValue(name, stream, clear);
        final Object normalizedValue = normalizePersistentValue(rawValue, multiplicity, clear);
        cachePersistentValue(name, stream, rawValue, normalizedValue);
        return normalizedValue;
    }

    @SuppressWarnings("unchecked")
    private void cachePersistentValue(
        String name,
        boolean stream,
        final Object rawValue,
        final Object normalizedValue
    ) {
        if (this.persistentValues != null) {
            if (stream) {
                this.persistentValues.remove(name);
            } else if (rawValue != normalizedValue) {
                this.persistentValues.put(
                    name,
                    normalizedValue
                );
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object normalizePersistentValue(
        final Object rawValue,
        Multiplicity multiplicity,
        boolean clear
    ) {
        Object normalizedValue = rawValue;
        switch (multiplicity) {
            case SINGLE_VALUE:
            case OPTIONAL: {
                if (rawValue instanceof List) {
                    List<?> source = (List<?>) rawValue;
                    normalizedValue = source.isEmpty() ? null : source.get(0);
                }
            }
                break;
            case SET:
            case LIST: {
                if (rawValue instanceof List<?>) {
                    if (clear) {
                        ((Collection<?>) rawValue).clear();
                    }
                } else {
                    List<Object> target = new ArrayList<Object>();
                    if (!clear) {
                        if (rawValue instanceof Collection<?>) {
                            target.addAll((Collection<?>) rawValue);
                        } else if (rawValue instanceof Map<?, ?>) {
                            SortedMap<Integer, ? extends Object> sortedValue = rawValue instanceof SortedMap<?, ?>
                                ? (SortedMap<Integer, ?>) rawValue
                                : new TreeMap<Integer, Object>((Map<Integer, ?>) rawValue);
                            target.addAll(
                                SortedMaps.asSparseArray(sortedValue).asList()
                            );
                        } else if (rawValue != null) {
                            target.add(rawValue);
                        }
                    }
                    normalizedValue = target;
                }
            }
                break;
            case SPARSEARRAY: {
                if (rawValue instanceof SparseArray<?>) {
                    if (clear) {
                        ((SparseArray<?>) rawValue).clear();
                    }
                } else {
                    SparseArray<Object> target = new TreeSparseArray<Object>();
                    if (!clear) {
                        if (rawValue instanceof Map<?, ?>) {
                            target.putAll((Map<Integer, ?>) rawValue);
                        } else if (rawValue instanceof Collection<?>) {
                            int i = 0;
                            for (Object value : (Collection<?>) rawValue) {
                                target.put(Integer.valueOf(i++), value);
                            }
                        } else if (rawValue != null) {
                            target.put(Integer.valueOf(0), rawValue);
                        }
                    }
                    normalizedValue = target;
                }
            }
                break;
            case MAP:
            case STREAM:
                // TODO not handled yet
                break;
        }
        return normalizedValue;
    }

    private Object retrievePersistentValue(
        String name,
        boolean stream,
        boolean clear
    )
        throws ServiceException {
        if (!clear && attributeMustBeLoaded(name)) {
            return loadPersistentValue(name, stream);
        } else if (this.persistentValues == null) {
            return null;
        } else {
            return this.persistentValues.get(name);
        }
    }

    @SuppressWarnings("unchecked")
    private Object loadPersistentValue(
        String name,
        boolean stream
    )
        throws ServiceException {
        final MappedRecord persistentValues = newRecord(this.persistentValues.getRecordName());
        persistentValues.putAll(this.persistentValues);
        final Filter attributeSpecifier = new Filter(
            null, // condition
            Collections.singletonList(
                new OrderSpecifier(name, SortOrder.UNSORTED)
            ),
            null // extension
        );
        final Object persistentValue;
        try {
            final Query_2Facade input = Query_2Facade.forObjectId(this);
            input.setQueryFilter(attributeSpecifier);
            final IndexedRecord indexedRecord = (IndexedRecord) this.dataObjectManager.getInteraction().execute(
                this.dataObjectManager.getInteractionSpecs().GET,
                input.getDelegate()
            );
            if (indexedRecord.isEmpty()) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "Could not fetch attribute",
                    ExceptionHelper.newObjectIdParameter("id", this),
                    new BasicException.Parameter("feature", name)
                );
            }
            final ObjectRecord output = (ObjectRecord) indexedRecord.get(0);
            persistentValues.putAll(
                output.getValue()
            );
            persistentValue = stream ? persistentValues.remove(name) : persistentValues.get(name);
            this.persistentValues = persistentValues;
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
        }
        return persistentValue;
    }

    /**
     * Ensure that the argument is single valued
     *
     * @param argument
     *            the argument to be checked
     *
     * @exception ServiceException
     *                BAD_PARAMETER
     *                if the argument is multi-valued
     */
    protected void assertSingleValued(
        Object argument
    )
        throws ServiceException {
        if (argument instanceof Collection<?>) {
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
     * @param feature
     *            the attribute's name
     * @param to
     *            the object.
     *
     * @exception ServiceException
     *                ILLEGAL_STATE
     *                if the object is write protected.
     * @exception ServiceException
     *                BAD_PARAMETER
     *                if the feature is multi-valued
     * @exception ServiceException
     *                NOT_SUPPORTED
     *                if the object has no such feature
     * @exception ServiceException
     *                if the object is not accessible
     */
    @Override
    public void objSetValue(
        String feature,
        Object to
    )
        throws ServiceException {
        this.assertSingleValued(to);
        UnitOfWork_1 unitOfWork = this.getUnitOfWorkIfTransactional(feature);
        (unitOfWork == null ? this.transientValues : unitOfWork.getState(this, false).values(false)).put(
            feature,
            to
        );
        if (to != null && this.isAspectHasCore(feature)) {
            for (PlugIn_1_0 plugIn : this.dataObjectManager.getPlugIns()) {
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
    )
        throws ServiceException {
        String type = featureDef.getModel().getDereferencedType(featureDef.getType()).getQualifiedName();
        Object persistentValue = this.getPersistentAttribute(
            featureName,
            true, // stream 
            Multiplicity.SINGLE_VALUE,
            false // clear
        );
        if (PrimitiveTypes.BINARY.equals(type)) {
            InputStream value;
            Long length;
            if (persistentValue == null) {
                return null;
            } else if (persistentValue instanceof InputStream) {
                //
                // Compatibility Mode
                //
                value = (InputStream) persistentValue;
                length = null;
            } else
                try {
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
                protected InputStream newContent()
                    throws IOException {
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
                    if (persistentValue instanceof InputStream) {
                        //
                        // Compatibility Mode
                        //
                        return (InputStream) persistentValue;
                    } else {
                        //
                        // Native Mode
                        //
                        return ((BinaryLargeObject) persistentValue).getContent();
                    }
                }

            };
        } else if (PrimitiveTypes.STRING.equals(type)) {
            Reader value;
            Long length;
            if (persistentValue == null) {
                return null;
            } else if (persistentValue instanceof Reader) {
                //
                // Compatibility Mode
                //
                value = (Reader) persistentValue;
                length = null;
            } else
                try {
                    //
                    // Native Mode
                    //
                    CharacterLargeObject object = (CharacterLargeObject) persistentValue;
                    value = object.getContent();
                    length = object.getLength();
                } catch (IOException exception) {
                    throw new ServiceException(exception);
                }
            return new CharacterLargeObjects.StreamLargeObject(value, length) {

                private static final long serialVersionUID = -6402214529785948658L;

                @Override
                protected Reader newContent()
                    throws IOException {
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
                    if (persistentValue instanceof Reader) {
                        //
                        // Compatibility Mode
                        //
                        return (Reader) persistentValue;
                    } else {
                        //
                        // Native Mode
                        //
                        return ((CharacterLargeObject) persistentValue).getContent();
                    }
                }

            };
        } else
            throw new ServiceException(
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
     * @param feature
     *            the feature's name
     *
     * @return the object representing the feature;
     *         or null if the feature's value hasn't been set yet.
     *
     * @exception ServiceException
     *                NOT_SUPPORTED
     *                if the object has no such feature
     * @exception ServiceException
     *                if the object is not accessible
     */
    @Override
    public Object objGetValue(
        final String feature
    )
        throws ServiceException {
        if (this.transientValues == null) {
            UnitOfWork_1 unitOfWork = this.getUnitOfWork();
            Map<String, Object> transactionalValues;
            if (this.jdoIsTransactional()) {
                transactionalValues = unitOfWork.getState(DataObject_1.this, false).values(false);
                Object transactionalValue = transactionalValues.get(feature);
                if (transactionalValue != null || transactionalValues.containsKey(feature)) {
                    return transactionalValue;
                }
            } else {
                transactionalValues = null;
            }
            ModelElement_1_0 featureDef = this.getFeature(feature);
            Object transactionalValue;
            if (isStreamedValue(featureDef)) {
                transactionalValue = this.getLargeObjectValue(feature, featureDef);
            } else {
                Object nonTransactionalValue = this.getPersistentAttribute(
                    feature,
                    false,
                    Multiplicity.SINGLE_VALUE,
                    false
                );
                transactionalValue = nonTransactionalValue == null ? null : featureDef == null ? nonTransactionalValue : // an embedded object's class
                    this.getMarshaller(featureDef).marshal(nonTransactionalValue);
            }
            if (transactionalValues != null) {
                transactionalValues.put(feature, transactionalValue);
            }
            return transactionalValue;
        } else if ((this.objIsHollow() && !this.isProxy()) ||
            this.transientValues.containsKey(feature)) {
            return this.transientValues.get(feature);
        } else {
            ModelElement_1_0 featureDef = this.getFeature(feature);
            Object transientValue;
            if (isStreamedValue(featureDef)) {
                transientValue = this.getLargeObjectValue(feature, featureDef);
            } else {
                Object clonedValue = this.getPersistentAttribute(
                    feature,
                    false, // stream
                    Multiplicity.SINGLE_VALUE,
                    false // clear
                );
                transientValue = clonedValue == null ? null : featureDef == null ? clonedValue : // an embedded object's class
                    this.getMarshaller(featureDef).marshal(clonedValue);
            }
            (this.jdoIsTransactional() ? this.getState(false).values(false) : this.transientValues).put(
                feature,
                transientValue
            );
            return transientValue;
        }

    }

    /**
     * Null-safe streamable test
     */
    private static boolean isStreamedValue(
        ModelElement_1_0 featureDef
    )
        throws ServiceException {
        return featureDef != null && ModelHelper.getMultiplicity(featureDef).isStreamValued();
    }

    /**
     * Cast the value of a given feature
     * 
     * @param feature
     * @param type
     * 
     * @return the value
     * @throws ServiceException
     *             if the value is not of the given type
     */
    private <T> T getFlushable(
        String feature,
        Class<T> type
    )
        throws ServiceException {
        Object value = this.flushableValues.get(feature);
        if (value == null || type.isInstance(value)) {
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
     * Assert that multi-value requests are model conform
     * 
     * @param feature
     * @param map
     */
    private void assertMultivalued(
        String feature,
        Multiplicity requested
    )
        throws ServiceException {
        Multiplicity multiplicity = ModelHelper.getMultiplicity(getAttributes().get(feature));
        if (multiplicity != requested) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "The feature's cache contains already a value of another type",
                ExceptionHelper.newObjectIdParameter("object", this),
                new BasicException.Parameter("feature", feature),
                new BasicException.Parameter("multiplicity", multiplicity),
                new BasicException.Parameter("requested", requested)
            ).log();
        }
    }

    /**
     * Get a List attribute.
     * <p>
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param feature
     *            The feature's name.
     *
     * @return a collection which may be empty but never null.
     *
     * @exception ServiceException
     *                ILLEGAL_STATE
     *                if the object is deleted
     * @exception ServiceException
     *                NOT_SUPPORTED
     *                if the object has no such feature
     * @exception ClassCastException
     *                if the feature's value is not a list
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Object> objGetList(
        String feature
    )
        throws ServiceException {
        List<Object> flushable = getFlushable(feature, List.class);
        if (flushable == null) {
            assertMultivalued(feature, Multiplicity.LIST);
            return (List<Object>) Maps.putUnlessPresent(
                this.flushableValues,
                feature,
                new ManagedList(
                    feature,
                    this.getMarshaller(feature)
                )
            );
        } else {
            return flushable;
        }
    }

    /**
     * Get a Set attribute.
     * <p>
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param feature
     *            List<Object> flushable = getFlushable(feature, List.class);
     * 
     *            The feature's name.
     *
     * @return a collection which may be empty but never null.
     *
     * @exception ServiceException
     *                ILLEGAL_STATE
     *                if the object is deleted
     * @exception ServiceException
     *                NOT_SUPPORTED
     *                if the object has no such feature
     * @exception ClassCastException
     *                if the feature's value is not a set
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<Object> objGetSet(
        String feature
    )
        throws ServiceException {
        Set<Object> flushable = getFlushable(feature, Set.class);
        if (flushable == null) {
            assertMultivalued(feature, Multiplicity.SET);
            return (Set<Object>) Maps.putUnlessPresent(
                this.flushableValues,
                feature,
                new ManagedSet(
                    feature,
                    this.getMarshaller(feature)
                )
            );
        } else {
            return flushable;
        }
    }

    /**
     * Get a SparseArray attribute.
     * <p>
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param feature
     *            The feature's name.
     *
     * @return a collection which may be empty but never null.
     *
     * @exception ServiceException
     *                ILLEGAL_STATE
     *                if the object is deleted
     * @exception ClassCastException
     *                if the feature's value is not a sparse array
     * @exception ServiceException
     *                NOT_SUPPORTED
     *                if the object has no such feature
     */
    @SuppressWarnings("unchecked")
    @Override
    public SortedMap<Integer, Object> objGetSparseArray(
        String feature
    )
        throws ServiceException {
        SortedMap<Integer, Object> flushable = getFlushable(feature, SortedMap.class);
        if (flushable == null) {
            assertMultivalued(feature, Multiplicity.SPARSEARRAY);
            return (SortedMap<Integer, Object>) Maps.putUnlessPresent(
                this.flushableValues,
                feature,
                new ManagedSortedMap(
                    feature,
                    this.getMarshaller(feature)
                )
            );
        } else {
            return flushable;
        }
    }

    /**
     * Get a Map attribute.
     * <p>
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param feature
     *            The feature's name.
     *
     * @return a map which may be empty but never null.
     *
     * @exception ServiceException
     *                ILLEGAL_STATE
     *                if the object is deleted
     * @exception ServiceException
     *                BAD_MEMBER_NAME
     *                if the object has no such feature
     * @exception ClassCastException
     *                if the feature's value is not a set
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> objGetMap(
        String feature
    )
        throws ServiceException {
        Map<String, Object> flushable = getFlushable(feature, Map.class);
        if (flushable == null) {
            assertMultivalued(feature, Multiplicity.MAP);
            return (Map<String, Object>) Maps.putUnlessPresent(
                this.flushableValues,
                feature,
                new ManagedMap(
                    feature,
                    this.getMarshaller(feature)
                )
            );
        } else {
            return flushable;
        }
    }

    /**
     * Get a reference feature.
     * <p>
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param feature
     *            The feature's name.
     *
     * @return a collection which may be empty but never null.
     *
     * @exception ServiceException
     *                ILLEGAL_STATE
     *                if the object is deleted
     * @exception ClassCastException
     *                if the feature is not a reference
     * @exception ServiceException
     *                NOT_SUPPORTED
     *                if the object has no such feature
     */
    @Override
    public Container_1_0 objGetContainer(
        String feature
    )
        throws ServiceException {
        Container_1_0 flushable = getFlushable(feature, Container_1_0.class);
        return flushable == null ? (Container_1_0) Maps.putUnlessPresent(
            this.flushableValues,
            feature,
            new Container_1(this, feature)
        ) : flushable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.accessor.cci.DataObject_1_0#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record,
     * javax.resource.cci.Record)
     */
    @Override
    public boolean execute(
        InteractionSpec ispec,
        Record input,
        Record output
    )
        throws ResourceException {
        if (ispec instanceof MethodInvocationSpec) {
            try {
                MethodInvocationSpec methodInvocationSpec = (MethodInvocationSpec) ispec;
                Operation entry = new Operation(
                    methodInvocationSpec.getFunctionName(),
                    (MappedRecord) input,
                    (MappedRecord) output
                );
                boolean query = methodInvocationSpec.getInteractionVerb() == InteractionSpec.SYNC_SEND_RECEIVE;
                if (query) {
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

    Object noContent(
        Object source
    ) {
        if (source instanceof DataObject_1) {
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

    @Override
    public String toString() {
        if (this.objIsInaccessible()) {
            return AbstractDataObject_1.toString(this, this.notFound == null ? null : "No object found for '" + this.notFound + "'");
        } else {
            try {
                Map<Object, Object> content = new HashMap<Object, Object>();
                TransactionalState_1 state = this.getState(true);
                String description;
                if (state == null) {
                    description = null;
                } else {
                    for (Map.Entry<String, Object> e : state.values(false).entrySet()) {
                        Object v = e.getValue();
                        if (v instanceof Collection) {
                            if (v instanceof List) {
                                List<Object> t = new ArrayList<Object>();
                                for (Iterator<?> j = ((List<?>) v).iterator(); j.hasNext();) {
                                    t.add(
                                        this.noContent(j.next())
                                    );
                                }
                            } else if (v instanceof Set) {
                                Set<Object> t = new HashSet<Object>();
                                for (Iterator<?> j = ((Set<?>) v).iterator(); j.hasNext();) {
                                    t.add(
                                        this.noContent(j.next())
                                    );
                                }
                            } // else ignore
                        } else if (v instanceof SortedMap) {
                            SortedMap<Object, Object> t = new TreeMap<Object, Object>();
                            for (Iterator<?> j = ((SortedMap<?, ?>) v).entrySet().iterator(); j.hasNext();) {
                                Map.Entry<?, ?> k = (Map.Entry<?, ?>) j.next();
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
    )
        throws java.io.IOException {
        try {
            TransactionalState_1 state = this.getState(true);
            stream.defaultWriteObject();
            if (state == null) {
                stream.writeInt(0);
            } else {
                Set<String> features = state.dirtyFeatures(true);
                Map<String, Object> source = state.values(false);
                stream.writeInt(features.size());
                for (String feature : features) {
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
    private void readObject(
        java.io.ObjectInputStream stream
    )
        throws java.io.IOException,
        ClassNotFoundException {
        stream.defaultReadObject();
        this.flushableValues = Maps.newMap(objThreadSafetyRequired());
        int count = stream.readInt();
        if (count > 0) {
            TransactionalState_1 state;
            try {
                state = this.getState(false);
            } catch (JDOException exception) {
                throw (IOException) new IOException().initCause(exception);
            }
            Set<String> features = state.dirtyFeatures(false);
            Map<String, Object> target = state.values(false);
            for (int i = 0; i < count; i++) {
                String feature = (String) stream.readObject();
                features.add(feature);
                target.put(feature, stream.readObject());
            }
        }
        this.dataObjectManager.putUnlessPresent(this.identity, this);
    }

    @SuppressWarnings("cast")
    private final ModelElement_1_0 getAttribute(
        String featured,
        String feature
    )
        throws ServiceException {
        Map<String, ModelElement_1_0> features = (Map<String, ModelElement_1_0>) getModel().getElement(
            featured
        ).objGetMap(
            "attribute"
        );
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
    )
        throws ServiceException {
        Marshaller marshaller = feature == null ? null
            : DataObject_1.DATA_TYPE_MARSHALLER.get(
                feature.getModel().getDereferencedType(feature.getType()).getQualifiedName()
            );
        return marshaller == null ? this.dataObjectManager : marshaller;
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
    )
        throws ServiceException {
        int i = feature.lastIndexOf(':');
        if (i < 0) {
            return this.getAttribute(
                objGetClass(),
                feature
            );
        } else {
            this.objRetrieve(
                false, // reload
                this.dataObjectManager.getFetchPlan(),
                Collections.singleton(feature),
                false,
                true // throwNotFoundException
            );
            Object raw = this.persistentValues.get(
                feature.substring(0, ++i) + SystemAttributes.OBJECT_CLASS
            );
            String embeddedClass = raw instanceof List<?> ? (String) ((List<?>) raw).get(0) : (String) raw;
            String embeddedFeature = feature.substring(i);
            return SystemAttributes.OBJECT_CLASS.equals(embeddedFeature) ? null
                : this.getAttribute(
                    embeddedClass,
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
    )
        throws ServiceException {
        return this.getMarshaller(this.getFeature(feature));
    }

    /**
     * Retrieve a specific aspect
     * 
     * @param aspectType
     *            the aspect type such as
     * 
     * @return the specific aspect
     * 
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, DataObject_1_0> getAspect(
        String aspectType
    ) {
        Map<String, DataObject_1_0> flushable = (Map<String, DataObject_1_0>) this.flushableValues.get(aspectType);
        return flushable != null ? flushable
            : (Map<String, DataObject_1_0>) Maps.putUnlessPresent(
                this.flushableValues,
                aspectType,
                new ManagedAspect(this, aspectType)
            );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getFactory()
     */
    @Override
    public DataObjectManager_1 jdoGetPersistenceManager() {
        return this.dataObjectManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyFields(java.lang.Object, int[])
     */
    @Override
    public void jdoCopyFields(
        Object other,
        int[] fieldNumbers
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsFromObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldConsumer,
     * java.lang.Object)
     */
    @Override
    public void jdoCopyKeyFieldsFromObjectId(
        ObjectIdFieldConsumer fm,
        Object oid
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(java.lang.Object)
     */
    @Override
    public void jdoCopyKeyFieldsToObjectId(Object oid) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldSupplier,
     * java.lang.Object)
     */
    @Override
    public void jdoCopyKeyFieldsToObjectId(
        ObjectIdFieldSupplier fm,
        Object oid
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoGetVersion()
     */
    @Override
    public byte[] jdoGetVersion() {
        return this.version;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDetached()
     */
    @Override
    public boolean jdoIsDetached() {
        return this.detached;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoMakeDirty(java.lang.String)
     */
    @Override
    public void jdoMakeDirty(String fieldName) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager)
     */
    @Override
    public PersistenceCapable jdoNewInstance(StateManager sm) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager, java.lang.Object)
     */
    @Override
    public PersistenceCapable jdoNewInstance(
        StateManager sm,
        Object oid
    ) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance()
     */
    @Override
    public Object jdoNewObjectIdInstance() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance(java.lang.Object)
     */
    @Override
    public Object jdoNewObjectIdInstance(Object o) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideField(int)
     */
    @Override
    public void jdoProvideField(int fieldNumber) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideFields(int[])
     */
    @Override
    public void jdoProvideFields(int[] fieldNumbers) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceField(int)
     */
    @Override
    public void jdoReplaceField(int fieldNumber) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFields(int[])
     */
    @Override
    public void jdoReplaceFields(int[] fieldNumbers) {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFlags()
     */
    @Override
    public void jdoReplaceFlags() {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceStateManager(javax.jdo.spi.StateManager)
     */
    @Override
    public void jdoReplaceStateManager(StateManager sm)
        throws SecurityException {
        throw new UnsupportedOperationException("Operation not supported by dataprovider connection");
    }

    /**
     * Create a record
     * 
     * @param recordName
     *            the MOF class name
     * 
     * @return a new record
     * 
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    static <T extends Record> T newRecord(
        String recordName
    )
        throws ServiceException {
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
        if (value == null) {
            throw new JDOUserException(
                "Null values can't be inserted into such a JMI feature collection",
                new NullPointerException(target.getSimpleName() + " does not accept null values"),
                this
            );
        }
    }

    //--------------------------------------------------------------------------
    // Class ManagedList
    //--------------------------------------------------------------------------

    /**
     * The managed list delegates
     * <ul>
     * <li>to a unit of work local copy if the object is transactional
     * <li>to the persistent values if the object is non-transactional
     * </ul>
     */
    final class ManagedList
        extends AbstractList<Object>
        implements Flushable {

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
        )
            throws ServiceException {
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
        ) {
            try {
                final UnitOfWork_1 unitOfWork = makeDirty ? getUnitOfWorkIfTransactional(this.feature) : getUnitOfWorkIfTransactional();
                if (unitOfWork == null) {
                    if (DataObject_1.this.transientValues != null) {
                        List<Object> transientValue = (List<Object>) DataObject_1.this.transientValues.get(this.feature);
                        if (makeDirty && transientValue == null) {
                            transientValues.put(
                                this.feature,
                                transientValue = new ArrayList<Object>()
                            );
                            return transientValue;
                        }
                        if (transientValue != null) {
                            if (clear) {
                                transientValue.clear();
                            }
                            return transientValue;
                        }
                    }
                    if (clear) {
                        this.nonTransactional.clear();
                    }
                    return this.nonTransactional;
                } else {
                    Map<String, Object> transactionalValues = unitOfWork.getState(DataObject_1.this, false).values(false);
                    List<Object> transactionalValue = (List<Object>) transactionalValues.get(this.feature);
                    if (transactionalValue == null) {
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
        public int size() {
            return this.getDelegate(false, false).size();
        }

        /*
         * (non-Javadoc)
         * 
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
        ) {
            assertNonNullValue(element, List.class);
            return this.getDelegate(true, false).set(index, element);
        }

        @Override
        public void add(
            int index,
            Object element
        ) {
            assertNonNullValue(element, List.class);
            this.getDelegate(true, false).add(index, element);
        }

        @Override
        public Object remove(
            int index
        ) {
            return this.getDelegate(true, false).remove(index);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractList#clear()
         */
        @Override
        public void clear() {
            this.getDelegate(true, true);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.Flushable#flush()
         */
        @Override
        public void flush()
            throws IOException {
            List<Object> source = this.getDelegate(false, false);
            if (source != this.nonTransactional) {
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
            )
                throws ServiceException {
                super(
                    marshaller,
                    null // delegate provided by getDelegate() method
                );
            }

            /**
             * Implements <code>Serializable</code>
             */
            private static final long serialVersionUID = -5790864813300543661L;

            /*
             * (non-Javadoc)
             * 
             * @see org.openmdx.base.collection.MarshallingList#getDelegate()
             */
            @Override
            protected List<?> getDelegate() {
                try {
                    return (List<?>) DataObject_1.this.getPersistentAttribute(
                        ManagedList.this.feature,
                        false,
                        Multiplicity.LIST,
                        false
                    );
                } catch (ServiceException e) {
                    throw new RuntimeServiceException(e);
                }
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.openmdx.base.collection.MarshallingList#clear()
             */
            @Override
            public void clear() {
                try {
                    DataObject_1.this.getPersistentAttribute(
                        ManagedList.this.feature,
                        false,
                        Multiplicity.LIST,
                        true
                    );
                } catch (ServiceException e) {
                    throw new RuntimeServiceException(e);
                }
            }

        }

    }

    //------------------------------------------------------------------------
    // Class ManagedSet
    //------------------------------------------------------------------------

    /**
     * The managed set delegates
     * <ul>
     * <li>to a unit of work local copy if the object is transactional
     * <li>to the persistent values if the object is non-transactional
     * </ul>
     */
    final class ManagedSet
        extends AbstractSet<Object>
        implements Flushable {

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
        )
            throws ServiceException {
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
        ) {
            try {
                final UnitOfWork_1 unitOfWork = makeDirty ? getUnitOfWorkIfTransactional(this.feature) : getUnitOfWorkIfTransactional();
                if (unitOfWork == null) {
                    if (DataObject_1.this.transientValues != null) {
                        Set<Object> transientValue = (Set<Object>) DataObject_1.this.transientValues.get(this.feature);
                        if (makeDirty && transientValue == null) {
                            transientValues.put(
                                this.feature,
                                transientValue = new HashSet<Object>()
                            );
                            return transientValue;
                        }
                        if (transientValue != null) {
                            if (clear) {
                                transientValue.clear();
                            }
                            return transientValue;
                        }
                    }
                    if (clear) {
                        this.nonTransactional.clear();
                    }
                    return this.nonTransactional;
                } else {
                    Map<String, Object> transactionalValues = unitOfWork.getState(DataObject_1.this, false).values(false);
                    Set<Object> transactionalValue = (Set<Object>) transactionalValues.get(this.feature);
                    if (transactionalValue == null) {
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

        /*
         * (non-Javadoc)
         * 
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
        public void clear() {
            this.getDelegate(true, true);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.getDelegate(false, false).size();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return this.getDelegate(false, false).isEmpty();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractCollection#add(java.lang.Object)
         */
        @Override
        public boolean add(Object o) {
            assertNonNullValue(o, Set.class);
            return this.getDelegate(true, false).add(o);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.Flushable#flush()
         */
        @Override
        public void flush()
            throws IOException {
            Set<Object> source = this.getDelegate(false, false);
            if (source != this.nonTransactional) {
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
            )
                throws ServiceException {
                super(
                    marshaller,
                    null // delegate provided by getDelegate() method
                );
            }

            /**
             * Implements <code>Serializable</code>
             */
            private static final long serialVersionUID = -5790864813300543661L;

            /*
             * (non-Javadoc)
             * 
             * @see org.openmdx.base.collection.MarshallingCollection#getDelegate()
             */
            @Override
            protected Collection<?> getDelegate() {
                try {
                    return (Collection<?>) DataObject_1.this.getPersistentAttribute(
                        ManagedSet.this.feature,
                        false,
                        Multiplicity.LIST,
                        false
                    );
                } catch (ServiceException e) {
                    throw new RuntimeServiceException(e);
                }
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.openmdx.base.collection.MarshallingCollection#clear()
             */
            @Override
            public void clear() {
                try {
                    DataObject_1.this.getPersistentAttribute(
                        ManagedSet.this.feature,
                        false,
                        Multiplicity.LIST,
                        true
                    );
                } catch (ServiceException e) {
                    throw new RuntimeServiceException(e);
                }
            }

        }

        /**
         * Set Iterator
         */
        class SetIterator implements Iterator<Object> {

            SetIterator(
                Iterator<?> delegate,
                boolean transactional
            ) {
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

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#hasNext()
             */
            @Override
            public boolean hasNext() {
                return this.delegate.hasNext();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#next()
             */
            @Override
            public Object next() {
                return this.current = this.delegate.next();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#remove()
             */
            @Override
            public void remove() {
                if (this.transactional) {
                    this.delegate.remove();
                } else if (this.current == null) {
                    throw new IllegalStateException(
                        "There is no current element to be removed"
                    );
                } else {
                    ManagedSet.this.getDelegate(true, false).remove(this.current);
                    this.current = null;
                }
            }

        }

    }

    //--------------------------------------------------------------------------
    // Class ManagedMap
    //--------------------------------------------------------------------------

    /**
     * The managed map delegates
     * <ul>
     * <li>to a unit of work local copy if the object is transactional
     * <li>to the persistent values if the object is non-transactional
     * </ul>
     */
    final class ManagedMap
        implements Map<String, Object>, Flushable {

        /**
         * Constructor
         *
         * @param feature
         * @param marshaller
         * 
         * @throws ServiceException
         */
        ManagedMap(
            final String feature,
            Marshaller marshaller
        )
            throws ServiceException {
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
        private final Map<String, Object> nonTransactional;

        private final Set<Map.Entry<String, Object>> entries = new AbstractSet<Map.Entry<String, Object>>() {

            @Override
            public Iterator<Map.Entry<String, Object>> iterator() {
                return new EntryIterator();
            }

            @Override
            public int size() {
                return getDelegate(false, false).size();
            }

        };

        @SuppressWarnings("unchecked")
        protected Map<String, Object> getDelegate(
            boolean makeDirty,
            boolean clear
        ) {
            try {
                final UnitOfWork_1 unitOfWork = makeDirty ? getUnitOfWorkIfTransactional(this.feature) : getUnitOfWorkIfTransactional();
                if (unitOfWork == null) {
                    if (DataObject_1.this.transientValues != null) {
                        Map<String, Object> transientValue = (Map<String, Object>) transientValues.get(this.feature);
                        if (makeDirty && transientValue == null) {
                            DataObject_1.this.transientValues.put(
                                this.feature,
                                transientValue = new HashMap<String, Object>()
                            );
                            return transientValue;
                        }
                        if (transientValue != null) {
                            if (clear) {
                                transientValue.clear();
                            }
                            return transientValue;
                        }
                    }
                    if (clear) {
                        this.nonTransactional.clear();
                    }
                    return this.nonTransactional;
                } else {
                    Map<String, Object> transactionalValues = unitOfWork.getState(DataObject_1.this, false).values(false);
                    Map<String, Object> transactionalValue = (Map<String, Object>) transactionalValues.get(this.feature);
                    if (transactionalValue == null) {
                        transactionalValues.put(
                            this.feature,
                            transactionalValue = clear ? new HashMap<String, Object>() : new HashMap<String, Object>(this.nonTransactional)
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

        /*
         * (non-Javadoc)
         * 
         * @see java.io.Flushable#flush()
         */
        @Override
        public void flush()
            throws IOException {
            Map<String, Object> source = ManagedMap.this.getDelegate(false, false);
            if (source != this.nonTransactional) {
                this.nonTransactional.clear();
                this.nonTransactional.putAll(source);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#entrySet()
         */
        @Override
        public Set<java.util.Map.Entry<String, Object>> entrySet() {
            return this.entries;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#clear()
         */
        @Override
        public void clear() {
            ManagedMap.this.getDelegate(true, true);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#containsKey(java.lang.Object)
         */
        @Override
        public boolean containsKey(Object key) {
            return ManagedMap.this.getDelegate(false, false).containsKey(key);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#containsValue(java.lang.Object)
         */
        public boolean containsValue(Object value) {
            return ManagedMap.this.getDelegate(false, false).containsValue(value);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#get(java.lang.Object)
         */
        @Override
        public Object get(Object key) {
            return ManagedMap.this.getDelegate(false, false).get(key);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return ManagedMap.this.getDelegate(false, false).isEmpty();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#keySet()
         */
        @Override
        public Set<String> keySet() {
            return ManagedMap.this.getDelegate(false, false).keySet(); // TODO make it modifiable
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
         */
        @Override
        public Object put(
            String key,
            Object value
        ) {
            return ManagedMap.this.getDelegate(true, false).put(key, value);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#putAll(java.util.Map)
         */
        @Override
        public void putAll(Map<? extends String, ? extends Object> t) {
            ManagedMap.this.getDelegate(true, false).putAll(t);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#remove(java.lang.Object)
         */
        @Override
        public Object remove(Object key) {
            return ManagedMap.this.getDelegate(true, false).remove(key);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#size()
         */
        @Override
        public int size() {
            return ManagedMap.this.getDelegate(false, false).size();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#values()
         */
        @Override
        public Collection<Object> values() {
            return ManagedMap.this.getDelegate(false, false).values();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            return ManagedMap.this.getDelegate(false, false).equals(obj);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return ManagedMap.this.getDelegate(false, false).hashCode();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return ManagedMap.this.getDelegate(false, false).toString();
        }

        /**
         * Entry Iterator
         */
        class EntryIterator implements Iterator<Map.Entry<String, Object>> {

            private Map<String, Object> readOnly = ManagedMap.this.getDelegate(false, false);

            private Iterator<Map.Entry<String, Object>> delegate = readOnly.entrySet().iterator();

            protected Map.Entry<String, Object> current = null;

            protected void makeDirty() {
                if (this.readOnly != null) {
                    Map<String, Object> forUpdate = ManagedMap.this.getDelegate(true, false);
                    Map<String, Object> readOnly = this.readOnly;
                    this.readOnly = null;
                    if (forUpdate != readOnly) {
                        this.delegate = forUpdate.entrySet().iterator();
                        while (this.delegate.hasNext()) {
                            Map.Entry<String, Object> candidate = this.delegate.next();
                            if (this.current.getKey().equals(candidate.getKey())) {
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

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#hasNext()
             */
            @Override
            public boolean hasNext() {
                return this.delegate.hasNext();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#next()
             */
            @Override
            public Map.Entry<String, Object> next() {
                this.current = this.delegate.next();
                return new Map.Entry<String, Object>() {

                    public String getKey() {
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

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#remove()
             */
            @Override
            public void remove() {
                EntryIterator.this.makeDirty();
                this.delegate.remove();
            }

        }

        /**
         * Persistent Values Accessor
         */
        class NonTransactional extends MarshallingMap<String, Object> {

            /**
             * Constructor
             * 
             * @param marshaller
             *
             * @throws ServiceException
             */
            NonTransactional(
                Marshaller marshaller
            )
                throws ServiceException {
                super(
                    marshaller,
                    null // delegate provided by getDelegate() method
                );
            }

            /**
             * Implements <code>Serializable</code>
             */
            private static final long serialVersionUID = 4281239775781185351L;

            /*
             * (non-Javadoc)
             * 
             * @see org.openmdx.base.collection.MarshallingMap#getDelegate()
             */
            @SuppressWarnings("unchecked")
            @Override
            protected Map<String, Object> getDelegate() {
                try {
                    return (Map<String, Object>) DataObject_1.this.getPersistentAttribute(
                        ManagedMap.this.feature,
                        false,
                        Multiplicity.MAP,
                        false
                    );
                } catch (ServiceException e) {
                    throw new RuntimeServiceException(e);
                }
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.openmdx.base.collection.MarshallingList#clear()
             */
            @Override
            public void clear() {
                try {
                    DataObject_1.this.getPersistentAttribute(
                        ManagedMap.this.feature,
                        false,
                        Multiplicity.MAP,
                        true
                    );
                } catch (ServiceException e) {
                    throw new RuntimeServiceException(e);
                }
            }

        }

    }

    //------------------------------------------------------------------------
    // Class ManagedSortedMap
    //------------------------------------------------------------------------

    /**
     * The managed sparse array delegates
     * <ul>
     * <li>to a unit of work local copy if the object is transactional
     * <li>to the persistent values if the object is non-transactional
     * </ul>
     */
    final class ManagedSortedMap
        implements SortedMap<Integer, Object>, Flushable {

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
        )
            throws ServiceException {
            this.feature = feature;
            this.nonTransactional = new MarshallingSortedMap(
                marshaller,
                new PopulationMap<Object>() {

                    /*
                     * (non-Javadoc)
                     * 
                     * @see org.openmdx.compatibility.base.collection.PopulationMap#getDelegate()
                     */
                    @SuppressWarnings("unchecked")
                    @Override
                    protected SparseArray<Object> getDelegate() {
                        try {
                            return (SparseArray<Object>) DataObject_1.this.getPersistentAttribute(
                                ManagedSortedMap.this.feature,
                                false,
                                Multiplicity.SPARSEARRAY,
                                false
                            );
                        } catch (ServiceException e) {
                            throw new RuntimeServiceException(e);
                        }
                    }

                    /*
                     * (non-Javadoc)
                     * 
                     * @see java.util.AbstractMap#clear()
                     */
                    @Override
                    public void clear() {
                        try {
                            DataObject_1.this.getPersistentAttribute(
                                ManagedSortedMap.this.feature,
                                false,
                                Multiplicity.SPARSEARRAY,
                                true
                            );
                        } catch (ServiceException e) {
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
        private final SortedMap<Integer, Object> nonTransactional;

        private final Set<Map.Entry<Integer, Object>> entries = new AbstractSet<Map.Entry<Integer, Object>>() {

            @Override
            public Iterator<Map.Entry<Integer, Object>> iterator() {
                return new EntryIterator();
            }

            @Override
            public int size() {
                return getDelegate(false, false).size();
            }

        };

        @SuppressWarnings("unchecked")
        protected SortedMap<Integer, Object> getDelegate(
            boolean makeDirty,
            boolean clear
        ) {
            try {
                final UnitOfWork_1 unitOfWork = makeDirty ? getUnitOfWorkIfTransactional(this.feature) : getUnitOfWorkIfTransactional();
                if (unitOfWork == null) {
                    if (DataObject_1.this.transientValues != null) {
                        SortedMap<Integer, Object> transientValue = (SortedMap<Integer, Object>) transientValues.get(this.feature);
                        if (makeDirty && transientValue == null) {
                            DataObject_1.this.transientValues.put(
                                this.feature,
                                transientValue = new TreeMap<Integer, Object>()
                            );
                            return transientValue;
                        }
                        if (transientValue != null) {
                            if (clear) {
                                transientValue.clear();
                            }
                            return transientValue;
                        }
                    }
                    if (clear) {
                        this.nonTransactional.clear();
                    }
                    return this.nonTransactional;
                } else {
                    Map<String, Object> transactionalValues = unitOfWork.getState(DataObject_1.this, false).values(false);
                    SortedMap<Integer, Object> transactionalValue = (SortedMap<Integer, Object>) transactionalValues.get(this.feature);
                    if (transactionalValue == null) {
                        transactionalValues.put(
                            this.feature,
                            transactionalValue = clear ? new TreeMap<Integer, Object>()
                                : new TreeMap<Integer, Object>(this.nonTransactional)
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

        /*
         * (non-Javadoc)
         * 
         * @see java.io.Flushable#flush()
         */
        @Override
        public void flush()
            throws IOException {
            SortedMap<Integer, Object> source = ManagedSortedMap.this.getDelegate(false, false);
            if (source != this.nonTransactional) {
                this.nonTransactional.clear();
                this.nonTransactional.putAll(source);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#entrySet()
         */
        @Override
        public Set<java.util.Map.Entry<Integer, Object>> entrySet() {
            return this.entries;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.SortedMap#comparator()
         */
        @Override
        public Comparator<? super Integer> comparator() {
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.SortedMap#firstKey()
         */
        @Override
        public Integer firstKey() {
            return ManagedSortedMap.this.getDelegate(false, false).firstKey();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.SortedMap#headMap(java.lang.Object)
         */
        @Override
        public SortedMap<Integer, Object> headMap(Integer toKey) {
            return new SubMap(null, toKey);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.SortedMap#lastKey()
         */
        @Override
        public Integer lastKey() {
            return ManagedSortedMap.this.getDelegate(false, false).lastKey();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.SortedMap#subMap(java.lang.Object, java.lang.Object)
         */
        @Override
        public SortedMap<Integer, Object> subMap(
            Integer fromKey,
            Integer toKey
        ) {
            return new SubMap(fromKey, toKey);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.SortedMap#tailMap(java.lang.Object)
         */
        @Override
        public SortedMap<Integer, Object> tailMap(Integer fromKey) {
            return new SubMap(fromKey, null);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#clear()
         */
        @Override
        public void clear() {
            ManagedSortedMap.this.getDelegate(true, true);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#containsKey(java.lang.Object)
         */
        @Override
        public boolean containsKey(Object key) {
            return ManagedSortedMap.this.getDelegate(false, false).containsKey(key);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#containsValue(java.lang.Object)
         */
        @Override
        public boolean containsValue(Object value) {
            return ManagedSortedMap.this.getDelegate(false, false).containsValue(value);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#get(java.lang.Object)
         */
        @Override
        public Object get(Object key) {
            return ManagedSortedMap.this.getDelegate(false, false).get(key);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return ManagedSortedMap.this.getDelegate(false, false).isEmpty();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#keySet()
         */
        @Override
        public Set<Integer> keySet() {
            return ManagedSortedMap.this.getDelegate(false, false).keySet(); // TOD make it modifiable
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
         */
        @Override
        public Object put(
            Integer key,
            Object value
        ) {
            return ManagedSortedMap.this.getDelegate(true, false).put(key, value);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#putAll(java.util.Map)
         */
        @Override
        public void putAll(Map<? extends Integer, ? extends Object> t) {
            ManagedSortedMap.this.getDelegate(true, false).putAll(t);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#remove(java.lang.Object)
         */
        @Override
        public Object remove(Object key) {
            return ManagedSortedMap.this.getDelegate(true, false).remove(key);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#size()
         */
        @Override
        public int size() {
            return ManagedSortedMap.this.getDelegate(false, false).size();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.AbstractMap#values()
         */
        @Override
        public Collection<Object> values() {
            return ManagedSortedMap.this.getDelegate(false, false).values();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            return ManagedSortedMap.this.getDelegate(false, false).equals(obj);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return ManagedSortedMap.this.getDelegate(false, false).hashCode();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return ManagedSortedMap.this.getDelegate(false, false).toString();
        }

        /**
         * Head, tail and sub-mapes
         */
        class SubMap
            extends AbstractMap<Integer, Object>
            implements SortedMap<Integer, Object> {

            /**
             * Constructor
             *
             * @param from
             * @param to
             */
            SubMap(
                Integer from,
                Integer to
            ) {
                this.from = from;
                this.to = to;
            }

            /**
             * The from-key, or <code>null</code> in case of a head-map
             */
            private final Integer from;

            /**
             * anotherPersistenceManager.currentTransaction().begin();
             * for(Map.Entry<Integer,String> e : anotherInfo.entrySet()) {
             * e.setValue(e.getKey().toString());
             * }
             * anotherPersistenceManager.currentTransaction().commit();
             * 
             * The to-key, or <code>null</code> in case of a tail-map
             */
            private final Integer to;

            /**
             * Assert that the key is inside the sub-map's range
             * 
             * @param key
             *            the key to be tested
             */
            private void validateKey(
                int key
            ) {
                if ((this.from != null && this.from.intValue() > key) ||
                    (this.to != null && this.to.intValue() < key)) {
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
            protected SortedMap<Integer, Object> getSubMap() {
                SortedMap<Integer, Object> map = ManagedSortedMap.this.getDelegate(false, false);
                if (from == null) {
                    if (to == null) {
                        return map;
                    } else {
                        return map.headMap(to);
                    }
                } else {
                    if (to == null) {
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

            /*
             * (non-Javadoc)
             * 
             * @see java.util.AbstractMap#entrySet()
             */
            @Override
            public Set<java.util.Map.Entry<Integer, Object>> entrySet() {
                return this.getSubMap().entrySet();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.SortedMap#comparator()
             */
            @Override
            public Comparator<? super Integer> comparator() {
                return null;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.SortedMap#firstKey()
             */
            @Override
            public Integer firstKey() {
                return this.getSubMap().firstKey();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.SortedMap#headMap(java.lang.Object)
             */
            @Override
            public SortedMap<Integer, Object> headMap(Integer toKey) {
                this.validateKey(toKey.intValue());
                return new SubMap(this.from, toKey);
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.SortedMap#lastKey()
             */
            @Override
            public Integer lastKey() {
                return this.getSubMap().lastKey();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.SortedMap#subMap(java.lang.Object, java.lang.Object)
             */
            @Override
            public SortedMap<Integer, Object> subMap(
                Integer fromKey,
                Integer toKey
            ) {
                this.validateKey(fromKey.intValue());
                this.validateKey(toKey.intValue());
                return new SubMap(fromKey, toKey);
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.SortedMap#tailMap(java.lang.Object)
             */
            @Override
            public SortedMap<Integer, Object> tailMap(Integer fromKey) {
                this.validateKey(fromKey.intValue());
                return new SubMap(fromKey, null);
            }

        }

        /**
         * Entry Iterator
         */
        class EntryIterator implements Iterator<Map.Entry<Integer, Object>> {

            private SortedMap<Integer, Object> readOnly = ManagedSortedMap.this.getDelegate(false, false);

            private Iterator<Map.Entry<Integer, Object>> delegate = readOnly.entrySet().iterator();

            protected Map.Entry<Integer, Object> current = null;

            protected void makeDirty() {
                if (this.readOnly != null) {
                    SortedMap<Integer, Object> forUpdate = ManagedSortedMap.this.getDelegate(true, false);
                    SortedMap<Integer, Object> readOnly = this.readOnly;
                    this.readOnly = null;
                    if (forUpdate != readOnly) {
                        this.delegate = forUpdate.entrySet().iterator();
                        while (this.delegate.hasNext()) {
                            Map.Entry<Integer, Object> candidate = this.delegate.next();
                            if (this.current.getKey().equals(candidate.getKey())) {
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

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#hasNext()
             */
            @Override
            public boolean hasNext() {
                return this.delegate.hasNext();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#next()
             */
            @Override
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

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#remove()
             */
            @Override
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
        ) {
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
        @SuppressWarnings({ "unchecked", "resource" })
        void invoke(
            Interaction interaction
        )
            throws ServiceException {
            try {
                MessageRecord input = DataObject_1.newRecord(MessageRecord.NAME);
                input.setResourceIdentifier(identity.getDescendant(this.operation, UUIDs.newUUID().toString()));
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
        // DATA_TYPE_MARSHALLER initalization
        //
        DATA_TYPE_MARSHALLER.put(DATE, DateMarshaller.NORMALIZE);
        DATA_TYPE_MARSHALLER.put(DATETIME, DateTimeMarshaller.NORMALIZE);
        DATA_TYPE_MARSHALLER.put(DURATION, DurationMarshaller.NORMALIZE);
        DATA_TYPE_MARSHALLER.put(SHORT, ShortMarshaller.NORMALIZE);
        DATA_TYPE_MARSHALLER.put(INTEGER, IntegerMarshaller.NORMALIZE);
        DATA_TYPE_MARSHALLER.put(LONG, LongMarshaller.NORMALIZE);
        //
        // lifecycleListenerClass initalization
        //
        LIFECYCLE_LISTENER_CLASS[InstanceLifecycleEvent.CREATE] = CreateLifecycleListener.class;
        LIFECYCLE_LISTENER_CLASS[InstanceLifecycleEvent.LOAD] = LoadLifecycleListener.class;
        LIFECYCLE_LISTENER_CLASS[InstanceLifecycleEvent.STORE] = StoreLifecycleListener.class;
        LIFECYCLE_LISTENER_CLASS[InstanceLifecycleEvent.CLEAR] = ClearLifecycleListener.class;
        LIFECYCLE_LISTENER_CLASS[InstanceLifecycleEvent.DELETE] = DeleteLifecycleListener.class;
        LIFECYCLE_LISTENER_CLASS[InstanceLifecycleEvent.DIRTY] = DirtyLifecycleListener.class;
        LIFECYCLE_LISTENER_CLASS[InstanceLifecycleEvent.DETACH] = DetachLifecycleListener.class;
        LIFECYCLE_LISTENER_CLASS[InstanceLifecycleEvent.ATTACH] = AttachLifecycleListener.class;
        //
        // clone fetch plan
        //
        PROXY_CLONE_FETCH_PLAN.setGroup(FetchPlan.ALL);
    }

    
}
