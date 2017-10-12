/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Container_1 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2017, OMEX AG, Switzerland
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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.util.AbstractSequentialList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import javax.jdo.FetchPlan;
import javax.jdo.JDOUserException;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.rest.spi.AbstractFilter;
import org.openmdx.base.accessor.rest.spi.ObjectFilter;
import org.openmdx.base.aop0.PlugIn_1_0;
import org.openmdx.base.collection.Maps;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.PersistenceCapableCollection;
import org.openmdx.base.persistence.spi.StandardFetchPlan;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.rest.cci.ConditionRecord;
import org.openmdx.base.rest.cci.FeatureOrderRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryExtensionRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.ImmutableDatatype;
import org.w3c.spi.DatatypeFactories;
import org.w3c.spi.ImmutableDatatypeFactory;

/**
 * Container
 */
abstract class AbstractContainer_1 extends AbstractConsumerAwareCollection {

    /**
     * Constructor for a persistent or transient container
     */
    protected AbstractContainer_1(
    ){
        super();
    }

    /**
     * 
     */
    private transient List<DataObject_1_0> persistent;
    
    /**
     * 
     */
    private transient Excluded excluded;
    
    /**
     * 
     */
    private transient Included included;
    
    /**
     * 
     */
    private transient BatchingList stored;
    
    /**
     * 
     */
    private transient Set<Map.Entry<String,DataObject_1_0>> entries;
    
    /**
     * 
     */
    private transient Set<String> keys;
    
    /**
     * 
     */
    private transient Collection<DataObject_1_0> values;
    
    /**
     * 
     */
    protected static final int BATCH_SIZE_GREEDY = Integer.MAX_VALUE;

    /**
     * The initial slice cache size
     */
    protected static final int INITIAL_SLICE_CACHE_SIZE = 8;

    /**
     * Do not build a new cache if the container is already retrieved
     */
    static final Map<String, DataObject_1_0> NO_CACHE = Collections.emptyMap();
    
    /**
     * The conditions in case of a sub-map
     * 
     * @return the conditions
     */
    protected abstract List<ConditionRecord> getConditions();
    
    /**
     * The extension in case of a sub-map
     * 
     * @return the extension
     */
    protected abstract List<QueryExtensionRecord> getExtensions();
    
    /**
     * Tells whether the cache must be ignored
     * 
     * @return <code>true</code> if the cache must be ignored.
     */
    protected abstract boolean isIgnoreCache();
    
    /**
     * Retrieve the filter
     * 
     * @return the filter
     */
    protected abstract DataObjectFilter getFilter();

    /**
     * No need to provide caches for new objects
     */
    protected static final EnumSet<ObjectState> EMPTY_CACHE_CANDIDATES = EnumSet.of(
        ObjectState.HOLLOW_PERSISTENT_NONTRANSACTIONAL,
        ObjectState.PERSISTENT_CLEAN, 
        ObjectState.PERSISTENT_DIRTY
    );
    
    protected static final QueryFilterRecord PLAIN = new Filter();

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoGetPersistenceManager()
     */
    @Override
    public abstract DataObjectManager_1 openmdxjdoGetDataObjectManager();

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#container()
     */
    @Override
    public abstract Container_1 container();

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#isRetrieved()
     */
    @Override
    public abstract boolean isRetrieved();

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        this.entrySet().clear();
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<Entry<String, DataObject_1_0>> entrySet() {
        if(this.entries == null) {
            this.entries = new EntrySet();
        }
        return this.entries;
    }

    protected boolean isExtent(){
        return false;
    }

    protected boolean isPlainExtent(){
        return isExtent();
    }
    
	/**
	 * Determine whether the members are administered by the proxified container
	 * 
	 * @return <code>true</code> if query rely on the proxified container only
	 */
	protected boolean isProxy() {
		return this.openmdxjdoGetDataObjectManager().isProxy() && this.jdoIsPersistent();
	}

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        final Map<String, DataObject_1_0> cache = container().getCache();
        if(cache == null) { 
            return this.getPersistent().isEmpty(); 
        } else if (cache.isEmpty()) {
            return true;
        } else if (AbstractContainer_1.this.getFilter() == null) {
            return false;
        } else {
            for(DataObject_1_0 candidate : cache.values()) {
                if(this.containsValue(candidate)){
                    return false;
                }
            }
            return true;
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<String> keySet() {
        if(this.keys == null) {
            this.keys = new KeySet(this);
        }
        return this.keys;
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends String, ? extends DataObject_1_0> m) {
        for(Map.Entry<? extends String, ? extends DataObject_1_0> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public DataObject_1_0 remove(Object key) {
        DataObject_1_0 value = get(key);
        if(value != null) {
            if(value.jdoIsPersistent()) {
                this.openmdxjdoGetDataObjectManager().deletePersistent(value);
            } else {
                container().getCache().remove(key);
            }
        }
        return value;
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        final Map<String, DataObject_1_0> cache = container().getCache();
        if(cache == null) { 
            return this.getPersistent().size(); 
        } else if (cache.isEmpty()) {
            return 0;
        } else if (this.getFilter() == null) {
            return cache.size();
        } else {
            int count = 0;
            for(DataObject_1_0 candidate : cache.values()) {
                if(this.containsValue(candidate)){
                    count++;
                }
            }
            return count;
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    @Override
    public Collection<DataObject_1_0> values() {
        if(this.values == null) {
            this.values = new UnorderedValues(this);
        }
        return this.values;
    }
    
    @Override
    public List<DataObject_1_0> values(
        FetchPlan fetchPlan, 
        FeatureOrderRecord... criteria
    ) {
        return new OrderedValues(
            DataObjectComparator.getInstance(criteria), 
            fetchPlan == null ? StandardFetchPlan.newInstance(null) : fetchPlan
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#subMap(org.openmdx.base.query.Filter)
     */
    @Override
    public Container_1_0 subMap(QueryFilterRecord filter) {
        DataObjectFilter objectFilter = DataObjectFilter.getInstance(this.getFilter(), filter, isExtent());
		return objectFilter == null ? this : new Selection_1(this, objectFilter);
    }

    /**
     * Evict the collection's cached slices
     */
    protected void evictStored(
    ){
        if (this.stored != null) {
            this.stored.evict();
        }
    }
    
    /**
     * Evict the collection's members
     */
    protected void evictMembers(
    ){
        for(
            Object candidate : (Set<?>) this.openmdxjdoGetDataObjectManager().getManagedObjects(
                EnumSet.of(ObjectState.PERSISTENT_CLEAN, ObjectState.PERSISTENT_DIRTY)
            )
        ) {
            if(this.containsValue(candidate)) {
                ((DataObject_1)candidate).evict();
            }
        }
    }

    /**
     * Determines whether an object belongs to the container or extent
     * 
     * @param candidate
     * 
     * @return <code>true</code> if the object belongs to the container or extent
     */
    protected boolean isInContainerOrExtent(
        Object candidate
    ){
        return container().containsObject(candidate);
    }

    /**
     * 
     * @return
     */
    protected Excluded getExcluded(){
        if(this.excluded == null) {
            this.excluded = new Excluded();
        }
        return this.excluded;
    }

    /**
     * 
     * @return
     */
    protected Included getIncluded(){
        if(this.included == null) {
            this.included = new Included(null);
        }
        return this.included;
    }

    /**
     * Retrieve the stored objects
     * 
     * @param comparator
     * @param fetchGroups
     * 
     * @return the stored objects
     */
    BatchingList getStored(
        DataObjectComparator comparator, 
        Set<String> fetchGroups
    ){
        final FeatureOrderRecord[] order = comparator == null ? null : comparator.getDelegate();
        final List<ConditionRecord> conditions = getConditions();
        final List<QueryExtensionRecord> extensions = getExtensions();
        
        final QueryFilterRecord query;
        final QueryFilterRecord key;
        if(
            (conditions == null || conditions.isEmpty()) && 
            (order == null || order.length == 0) &&
            (extensions == null || extensions.isEmpty()) 
        ) {
            query = null;
            key = PLAIN;
        } else {
            key = query = new Filter(
                conditions, 
                order == null ? null : Arrays.asList(order), 
                extensions
            );
        }
        final BatchingList stored = container().queries.get(key);
        return stored == null ? Maps.putUnlessPresent(
            container().queries,
            key, 
            new BatchingList(
                container().getQueryType(), 
                query, 
                fetchGroups
            )
        ) : stored;
    }
    
    /**
     * 
     * @return
     */
    protected BatchingList getStored(){
        if(this.stored == null) {
            this.stored = this.getStored(
                null, 
                null
            );
        }
        return this.stored;
    }

    /**
     * 
     * @return
     */
    protected boolean hasStored(){
        return this.stored == null;
    }
    
    /**
     * Proxy access requires the persistent managers to be synchronized
     */
    void synchronize(){
        if(isProxy()) try {
            if(openmdxjdoGetDataObjectManager().currentUnitOfWork().synchronize() && this.persistent != null) {
                ((BatchingList)this.persistent).evict();
            }
        } catch (ServiceException exception) {
            throw new JDOUserException(
                "Unable to synchronize proxy",
                exception
            );
        }
    }
    
    /**
     * 
     * @return
     */
    protected List<DataObject_1_0> getPersistent(){
        synchronize();
		if(this.persistent == null) {
		    this.persistent = this.isProxy() ? this.getStored() : new ChainingList(
                this.getIncluded(),
                this.getStored(),
                this.getExcluded()
            );
    	}
        return this.persistent;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoRefresh()
     */
    @Override
    public void openmdxjdoRefresh(
    ) {
        PersistenceManager persistenceManager = this.openmdxjdoGetDataObjectManager();
        for(DataObject_1 candidate : this.openmdxjdoGetDataObjectManager().currentUnitOfWork().getMembers()) {
            if(this.containsValue(candidate)) {
                persistenceManager.refresh(candidate);
            }
        }
    }

    /**
     * Creates a data object cache 
     * 
     * @param owner the owner determines whether thread safety is required
     * 
     * @return a newly created cache
     */
    protected static Map<String, DataObject_1_0> newObjectCache(
        DataObject_1_0 owner
    ) {
        return owner.objThreadSafetyRequired() ? new ConcurrentHashMap<String,DataObject_1_0>() : new HashMap<String,DataObject_1_0>();
    }
    
    /**
     * Create and populate cache for persistent objects
     * 
     * @param caches
     * @param object
     */
    static void addToCache(
        Map<Container_1_0,Map<String, DataObject_1_0>> caches,
        DataObject_1_0 object
    ){
        Container_1_0 container = object.getContainer(false);
        if(container != null) {
            Map<String, DataObject_1_0> cache = caches.get(container);
            if(cache == null) {
                caches.put(
                    container,
                    cache = container.isRetrieved() || !container.jdoIsPersistent() ? NO_CACHE : newObjectCache(object)
                );
            }
            if(cache != NO_CACHE) {
                if(object.jdoIsPersistent()) {
                    //
                    // That's what we expect
                    //
                    cache.put(
                        object.jdoGetObjectId().getLastSegment().toClassicRepresentation(),
                        object
                    );
                } else {
                    //
                    // Try to recover from unexpected situation
                    //
                    SysLog.log(
                        Level.WARNING,
                        "Trying to add transient object {0} to persistent container {1}, discarding cache!",
                        object.jdoGetTransactionalObjectId(),
                        container.jdoGetObjectId()
                    );
                    caches.put(container, NO_CACHE);
                }
            }
        }
    }
    
    static void addEmptyCache(
        Map<Container_1_0,Map<String, DataObject_1_0>> caches,
        DataObject_1_0 parent,
        String feature
    ) throws ServiceException {
        final Container_1_0 container = parent.objGetContainer(feature);
        if(!container.isRetrieved()) {
            final Map<String, DataObject_1_0> cache = caches.get(container);
            if(cache == null) {
                caches.put(
                    container,
                    newObjectCache(parent)
                );
            }
        }
    }
    
    /**
     * The values may be filtered and/or ordered
     */
    private class OrderedValues 
        extends AbstractSequentialList<DataObject_1_0>
        implements PersistenceCapableCollection
    {

        /**
         * Constructor 
         * 
         * @param selector
         * @param entrySet
         * @param comparator
         * @param fetchGroups 
         */
        OrderedValues(
            DataObjectComparator comparator, 
            FetchPlan fetchPlan
        ){
            this.comparator = comparator;
            this.fetchPlan = fetchPlan;
        }

        private final DataObjectComparator comparator;
        protected final FetchPlan fetchPlan;
        
        private BatchingCollection persistent;
        private BatchingList stored;
        
        @SuppressWarnings("unchecked")
        private BatchingList getStored(){
            if(this.stored == null) {
                this.stored = AbstractContainer_1.this.getStored(
                    this.comparator, 
                    this.fetchPlan.getGroups()
                );
            } 
            return this.stored;
        }
        
        private BatchingCollection getPersistent(){
            synchronize();
            if(this.persistent == null) {
                this.persistent = this.comparator == null ? new ChainingList(
                    new Included(this.comparator),
                    getStored(),
                    AbstractContainer_1.this.getExcluded()
                ) : new MergingList(
                    new Included(this.comparator),
                    getStored(),
                    AbstractContainer_1.this.getExcluded()
                );
            }
            return this.persistent;
        }

        @Override
        public ListIterator<DataObject_1_0> listIterator(int index) {
            final Map<String, DataObject_1_0> cache = container().getCache();
            if(cache != null && !AbstractContainer_1.this.isIgnoreCache()) {
                List<DataObject_1_0> values = new ArrayList<DataObject_1_0>();
                for(DataObject_1_0 candidate : cache.values()) {
                    if(AbstractContainer_1.this.containsValue(candidate)) {
                        values.add(candidate);
                    }
                }
                if(this.comparator != null) {
                    Collections.sort(values, this.comparator);
                }
                return new ValueIterator(
                    values.listIterator(index),
                    true
                );    
            } else if (AbstractContainer_1.this.isRetrieved()) {
                List<DataObject_1_0> values = new ArrayList<DataObject_1_0>(
                    AbstractContainer_1.this.values()
                );
                if(this.comparator != null) {
                    Collections.sort(values, this.comparator);
                }
                return new ValueIterator(
                    values.listIterator(index),
                    true
                );    
            } else {
                List<DataObject_1_0> persistent = getPersistent();
                return new ValueIterator(
                    persistent instanceof BatchingCollection ? 
                        ((BatchingCollection)persistent).listIterator(index, this.fetchPlan) : 
                        persistent.listIterator(index),
                    false
                );
            }
        }

        @Override
        public int size() {
            final Map<String, DataObject_1_0> cache = container().getCache();
            if(cache == null || AbstractContainer_1.this.isIgnoreCache()) {
                return this.getPersistent().size(fetchPlan);
            } else {
                int count = 0;
                for(DataObject_1_0 candidate : cache.values()) {
                    if(AbstractContainer_1.this.containsValue(candidate)) {
                        count++;
                    }
                }
                return count;    
            }
        }

        @Override
        public boolean isEmpty() {
            final Map<String, DataObject_1_0> cache = container().getCache();
            return cache == null ? this.getPersistent().isEmpty() : (cache.isEmpty() || super.isEmpty());
        } 

        @Override
        public void clear() {
            for (
                Iterator<DataObject_1_0> i = this.listIterator();
                i.hasNext();
            ){
                i.next();
                i.remove();
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoEvict(boolean)
         */
        @Override
        public void openmdxjdoEvict(
            boolean allMembers, boolean allSubSets
         ) {
            AbstractContainer_1.this.openmdxjdoEvict(allMembers, allSubSets);
            if(this.stored != null) {
                this.stored.evict();
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoGetContainerId()
         */
        @Override
        public Path jdoGetObjectId() {
            return (Path) AbstractContainer_1.this.jdoGetObjectId();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoGetPersistenceManager()
         */
        @Override
        public PersistenceManager openmdxjdoGetDataObjectManager() {
            return AbstractContainer_1.this.openmdxjdoGetDataObjectManager();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetPersistenceManager()
         */
        @Override
        public PersistenceManager jdoGetPersistenceManager(){
        	return AbstractContainer_1.this.jdoGetPersistenceManager();
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoGetTransientContainerId()
         */
        @Override
        public TransientContainerId jdoGetTransactionalObjectId() {
            return (TransientContainerId) AbstractContainer_1.this.jdoGetTransactionalObjectId();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoIsPersistent()
         */
        @Override
        public boolean jdoIsPersistent() {
            return AbstractContainer_1.this.jdoIsPersistent();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoRefresh()
         */
        @Override
        public void openmdxjdoRefresh() {
            if(this.stored != null) {
                this.stored.evict();
            }
            AbstractContainer_1.this.openmdxjdoRefresh();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoRetrieve(javax.jdo.FetchPlan)
         */
        @Override
        public void openmdxjdoRetrieve(FetchPlan fetchPlan) {
            AbstractContainer_1.this.openmdxjdoRetrieve(fetchPlan);
        }

        /**
         * Break the List contract to avoid round-trips
         */
        @Override
        public boolean equals(
            Object that
        ) {
            return this == that;
        }

        /**
         * Break the List contract to avoid round-trips
         */
        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        /**
         * Break the List contract to avoid round-trips
         */
        @Override
        public String toString(
        ){
            return this.getClass().getName() + "@" + System.identityHashCode(this);
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoReplaceStateManager(javax.jdo.spi.StateManager)
         */
        @Override
        public void jdoReplaceStateManager(
            StateManager sm
        ) throws SecurityException {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoProvideField(int)
         */
        @Override
        public void jdoProvideField(int fieldNumber) {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoProvideFields(int[])
         */
        @Override
        public void jdoProvideFields(int[] fieldNumbers) {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoReplaceField(int)
         */
        @Override
        public void jdoReplaceField(int fieldNumber) {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFields(int[])
         */
        @Override
        public void jdoReplaceFields(int[] fieldNumbers) {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFlags()
         */
        @Override
        public void jdoReplaceFlags() {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoCopyFields(java.lang.Object, int[])
         */
        @Override
        public void jdoCopyFields(Object other, int[] fieldNumbers) {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoMakeDirty(java.lang.String)
         */
        @Override
        public void jdoMakeDirty(String fieldName) {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoGetVersion()
         */
        @Override
        public Object jdoGetVersion() {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoIsDirty()
         */
        @Override
        public boolean jdoIsDirty() {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoIsTransactional()
         */
        @Override
        public boolean jdoIsTransactional() {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoIsNew()
         */
        @Override
        public boolean jdoIsNew() {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoIsDeleted()
         */
        @Override
        public boolean jdoIsDeleted() {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoIsDetached()
         */
        @Override
        public boolean jdoIsDetached() {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager)
         */
        @Override
        public PersistenceCapable jdoNewInstance(StateManager sm) {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager, java.lang.Object)
         */
        @Override
        public PersistenceCapable jdoNewInstance(StateManager sm, Object oid) {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance()
         */
        @Override
        public Object jdoNewObjectIdInstance() {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance(java.lang.Object)
         */
        @Override
        public Object jdoNewObjectIdInstance(Object o) {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(java.lang.Object)
         */
        @Override
        public void jdoCopyKeyFieldsToObjectId(Object oid) {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldSupplier, java.lang.Object)
         */
        @Override
        public void jdoCopyKeyFieldsToObjectId(ObjectIdFieldSupplier fm, Object oid) {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        /* (non-Javadoc)
         * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsFromObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldConsumer, java.lang.Object)
         */
        @Override
        public void jdoCopyKeyFieldsFromObjectId(
            ObjectIdFieldConsumer fm,
            Object oid
        ) {
            throw new UnsupportedOperationException("Not supported by persistence capable collections");
        }

        
        //--------------------------------------------------------------------
        // Class ValueIterator
        //--------------------------------------------------------------------
        
        /**
         * The Value Iterator handles the remove method
         */
        private class ValueIterator implements ListIterator<DataObject_1_0> {

            /**
             * Constructor 
             *
             * @param delegate
             * @param updateDelegate
             */
            ValueIterator(
                ListIterator<DataObject_1_0> delegate,
                boolean updateDelegate
            ){
                this.delegate = delegate;
                this.updateDelegate = updateDelegate;
            }

            private final boolean updateDelegate;

            private final ListIterator<DataObject_1_0> delegate;

            private DataObject_1_0 current = null;


            @Override
            public void add(DataObject_1_0 o) {


                throw new UnsupportedOperationException("Query result is unmodifiable");
            }

            @Override
            public boolean hasNext() {
                return this.delegate.hasNext();
            }

            @Override
            public boolean hasPrevious() {
                return this.delegate.hasPrevious();
            }

            @Override
            public DataObject_1_0 next() {
                return this.current = this.delegate.next();
            }

            @Override
            public int nextIndex() {
                return this.delegate.nextIndex();
            }

            @Override
            public DataObject_1_0 previous() {
                return this.current = this.delegate.previous();
            }

            @Override
            public int previousIndex() {
                return this.delegate.previousIndex();
            }

            @Override
            public void remove() {
                if(this.updateDelegate) {
                    this.delegate.remove();
                }
                try {
                    ((DataObject_1)this.current).objRemove(true);
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                }
            }

            @Override
            public void set(DataObject_1_0 o) {
                throw new UnsupportedOperationException("Query result is unmodifiable");
            }

        }

    }
    
    
    //------------------------------------------------------------------------
    // Interface Batching Iterable
    //------------------------------------------------------------------------
        
    /**
     * Batching Iterable
     */
    static interface BatchingCollection extends List<DataObject_1_0> {

        /**
         * Create a list iterator using the given fetch plan
         * 
         * @param index
         * @param fetchPlan
         * 
         * @return a list iterator 
         */
        ListIterator<DataObject_1_0> listIterator(
            int index,
            FetchPlan fetchPlan
        );
        
        /**
         * Determine the collection's size
         * 
         * @param fetchPlan
         * 
         * @return the collection's size 
         */
        int size(
            FetchPlan fetchPlan
        );
        
    }

    
    //------------------------------------------------------------------------
    // Class UnorderedValues
    //------------------------------------------------------------------------
    
    /**
     * Unordered Values
     */
    private static class UnorderedValues implements Collection<DataObject_1_0> {

        /**
         * Constructor 
         */
        UnorderedValues(
        	Container_1_0 delegate
        ) {
        	this.delegate = delegate;
        }

        /**
         * The delegate
         */
        private final Container_1_0 delegate;

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        public Iterator<DataObject_1_0> iterator() {
            return new ValueIterator(
                this.delegate.entrySet().iterator()
            );
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        public int size() {
            return this.delegate.size();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#contains(java.lang.Object)
         */
        public boolean contains(Object o) {
            return this.delegate.containsValue(o);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        public boolean isEmpty() {
            return this.delegate.isEmpty();
        }

        /* (non-Javadoc)
		 * @see java.util.Collection#toArray()
		 */
		public Object[] toArray() {
			return toArray(
				new Object[size()]
			);
		}

		/* (non-Javadoc)
		 * @see java.util.Collection#toArray(T[])
		 */
		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] a) {
			Object[] values;
			int s = size();
			if(a.length < s) {
				values = (Object[]) Array.newInstance(a.getClass().getComponentType(), s);
			} else {
				values = a;
			}
			int i = 0;
			for(Map.Entry<String, DataObject_1_0> entry : this.delegate.entrySet()) {
				values[i++] = entry.getValue();
			}
			while(i < s) {
				values[i++] = null;
			}
			return (T[]) values;
		}

		/* (non-Javadoc)
		 * @see java.util.Collection#add(java.lang.Object)
		 */
		public boolean add(DataObject_1_0 o) {
	        throw new UnsupportedOperationException("Not yet implemented");
		}

		/* (non-Javadoc)
		 * @see java.util.Collection#remove(java.lang.Object)
		 */
		public boolean remove(Object o) {
			if(this.contains(o)) {
	            if(ReducedJDOHelper.isPersistent(o)) {
	            	this.delegate.openmdxjdoGetDataObjectManager().deletePersistent(o);
	                return true;
	            } else {
	            	for(
	            		Iterator<Map.Entry<String, DataObject_1_0>> i = this.delegate.entrySet().iterator();
	            		i.hasNext();
	            	){
	            		if(o == i.next()) {
	            			i.remove();
	            			return true;
	            		}
	            	}
	            	return false;
	            }
			} else {
				return false;
			}
		}

		/* (non-Javadoc)
		 * @see java.util.Collection#containsAll(java.util.Collection)
		 */
		public boolean containsAll(Collection<?> c) {
			for(Object value : c) {
				if(!this.delegate.containsValue(value)) return false;
			}
			return true;
		}

		/* (non-Javadoc)
		 * @see java.util.Collection#addAll(java.util.Collection)
		 */
		public boolean addAll(Collection<? extends DataObject_1_0> c) {
	        throw new UnsupportedOperationException("Not yet implemented");
		}

		/* (non-Javadoc)
		 * @see java.util.Collection#removeAll(java.util.Collection)
		 */
		public boolean removeAll(Collection<?> c) {
			boolean modified = false;
			for(Object o : c) {
				modified |= remove(o);
			}
			return modified;
		}

		/* (non-Javadoc)
		 * @see java.util.Collection#retainAll(java.util.Collection)
		 */
		public boolean retainAll(Collection<?> c) {
			boolean modified = false;
			for(
				Iterator<DataObject_1_0> i = this.iterator(); 
				i.hasNext();
			) {
				if(!c.contains(i.next())) {
					i.remove();
					modified = true;
				}
			}
			return modified;
		}

		/* (non-Javadoc)
		 * @see java.util.Collection#clear()
		 */
		public void clear() {
			this.delegate.clear();
		}

		/**
         * Break the List contract to avoid round-trips
         */
        @Override
        public boolean equals(
            Object that
        ) {
            return this == that;
        }

        /**
         * Break the List contract to avoid round-trips
         */
        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        /**
         * Break the List contract to avoid round-trips
         */
        @Override
        public String toString(
        ){
            return this.getClass().getSimpleName() + " of " + this.delegate;
        }

        
	    //--------------------------------------------------------------------
	    // Class ValueIterator
	    //--------------------------------------------------------------------
	    
	    /**
	     * Value Iterator
	     */
	    private static class ValueIterator implements Iterator<DataObject_1_0> {
	        
	        /**
	         * Constructor 
	         *
	         * @param delegate
	         */
	        ValueIterator(
	            Iterator<Map.Entry<String, DataObject_1_0>> delegate
	        ){
	            this.delegate = delegate;
	        }
	        
	        /**
	         * An entry set iterator
	         */
	        private final Iterator<Map.Entry<String, DataObject_1_0>> delegate;
	
	        /* (non-Javadoc)
	         * @see java.util.Iterator#hasNext()
	         */
	        @Override
	        public boolean hasNext() {
	            return this.delegate.hasNext();
	        }
	
	        /* (non-Javadoc)
	         * @see java.util.Iterator#next()
	         */
	        @Override
	        public DataObject_1_0 next() {
	            return this.delegate.next().getValue();
	        }
	
	        /* (non-Javadoc)
	         * @see java.util.Iterator#remove()
	         */
	        @Override
	        public void remove() {
	            this.delegate.remove();
	        }
	        
	    }

    }    

    
    //------------------------------------------------------------------------
    // Class KeySet
    //-----------------------------------------------------------------------

    /**
     * Key Set
     */
    private static class KeySet extends AbstractSet<String> {

        /**
         * Constructor 
         * 
         * @param delegate 
         */
        KeySet(
        	Container_1_0 delegate
        ) {
            this.delegate = delegate;
        }

        /**
         * The delegate
         */
        private final Container_1_0 delegate;
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<String> iterator() {
            return new KeyIterator(
                this.delegate.entrySet().iterator()
            );
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.delegate.size();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#contains(java.lang.Object)
         */
        @Override
        public boolean contains(Object o) {
            return this.delegate.containsKey(o);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return this.delegate.isEmpty();
        }

        /**
         * Break the List contract to avoid round-trips
         */
        @Override
        public boolean equals(
            Object that
        ) {
            return this == that;
        }

        /**
         * Break the List contract to avoid round-trips
         */
        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        /**
         * Break the List contract to avoid round-trips
         */
        @Override
        public String toString(
        ){
            return this.getClass().getSimpleName() + " of " + this.delegate;
        }

        
        //--------------------------------------------------------------------
        // Class KeyIterator
        //--------------------------------------------------------------------
        
        /**
         * Key Iterator
         */
        private static class KeyIterator implements Iterator<String> {
            
            /**
             * Constructor 
             *
             * @param delegate
             */
            KeyIterator(
                Iterator<Map.Entry<String, DataObject_1_0>> delegate
            ){
                this.delegate = delegate;
            }
            
            /**
             * An entry set iterator
             */
            private final Iterator<Map.Entry<String, DataObject_1_0>> delegate;

            /* (non-Javadoc)
             * @see java.util.Iterator#hasNext()
             */
            @Override
            public boolean hasNext() {
                return this.delegate.hasNext();
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#next()
             */
            @Override
            public String next() {
                return this.delegate.next().getKey();
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#remove()
             */
            @Override
            public void remove() {
                this.delegate.remove();
            }
            
        }
        
    }
    
    
    //------------------------------------------------------------------------
    // Class EntrySet
    //-----------------------------------------------------------------------

    /**
     * Entry Set
     */
    private class EntrySet implements Set<Map.Entry<String, DataObject_1_0>> {

        /**
         * Constructor 
         */
        EntrySet(
        ){
        	super();
        }

        @Override
        public Iterator<java.util.Map.Entry<String, DataObject_1_0>> iterator(
        ) {
            final Map<String, DataObject_1_0> cache = container().getCache();
            return cache == null ? new DatastoreIterator(
                AbstractContainer_1.this.getPersistent().listIterator(0)
            ) : new CacheIterator(
                cache.entrySet().iterator()
            );
        }

        @Override
        public int size() {
            return AbstractContainer_1.this.size();
        }

        @Override
        public boolean isEmpty() {
            return AbstractContainer_1.this.isEmpty();
        }

        @Override
        public boolean add(java.util.Map.Entry<String, DataObject_1_0> o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(
            Collection<? extends java.util.Map.Entry<String, DataObject_1_0>> c
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            for(
                Iterator<Map.Entry<String, DataObject_1_0>> i = this.iterator();
                i.hasNext();
            ){
                i.next();
                i.remove();
            }
        }

        @Override
        public boolean contains(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException();
        }

        
        //--------------------------------------------------------------------
        // Class CacheIterator
        //--------------------------------------------------------------------

        /**
         * Entry Iterator
         */
        private class CacheIterator implements Iterator<Map.Entry<String, DataObject_1_0>> {

            CacheIterator(
                Iterator<Map.Entry<String, DataObject_1_0>> delegate
            ){
                this.delegate = delegate;
            }

            private final Iterator<Map.Entry<String, DataObject_1_0>> delegate;

            Map.Entry<String, DataObject_1_0> preFetched;

            Map.Entry<String, DataObject_1_0> current;

            private boolean isAspect(
            	DataObject_1_0 value
            ) throws ServiceException{
            	if(value instanceof DataObject_1) {
            		DataObject_1 object = (DataObject_1) value;
            		for(PlugIn_1_0 plugIn : openmdxjdoGetDataObjectManager().getPlugIns()) {
            			Boolean aspect = plugIn.isAspect(object);
            			if(aspect != null) {
            				return aspect.booleanValue();
            			}
            		}
            	}
            	return false;
            }
            
            @Override
            public boolean hasNext() {
                while(this.preFetched == null && this.delegate.hasNext()) try {
                    Map.Entry<String, DataObject_1_0> candidate = this.delegate.next();
                    DataObject_1_0 value = candidate.getValue();
                    if(
                    	AbstractContainer_1.this.containsValue(value) && 
                    	(!isAspect(value) || value.objGetValue("core") != null)
                    ){
                        this.preFetched = candidate;
                    }
                } catch (ServiceException exception) {
                    SysLog.trace("Acceptance test failure", exception);
                }
                return this.preFetched != null;
            }

            @Override
            public java.util.Map.Entry<String, DataObject_1_0> next() {
                if(this.hasNext()) {
                    this.current = this.preFetched;
                    this.preFetched = null;
                    return this.current;
                } else {
                    throw new NoSuchElementException();
                }
            }

            @Override
            public void remove() {
                if(this.current == null) {
                    throw new IllegalStateException("No current element");
                }
                try {
                    DataObject_1 object = (DataObject_1)this.current.getValue();
                    if(object.jdoIsPersistent()) {
                        object.objRemove(false);
                    } else {
                        container().getCache().values().remove(object);
                    }
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                } finally {
                    this.delegate.remove();
                }
            }

        }

        
        //--------------------------------------------------------------------
        // Class DatastoreIterator
        //--------------------------------------------------------------------

        /**
         * Selection Iterator
         */
        private class DatastoreIterator implements Iterator<Map.Entry<String, DataObject_1_0>> {

            /**
             * Constructor 
             *
             * @param delegate
             */
            DatastoreIterator(
                Iterator<DataObject_1_0> delegate
            ){
                this.delegate = delegate;
            }

            /**
             * The batching list iterator
             */
            private final Iterator<DataObject_1_0> delegate;

            /**
             * The current data object
             */
            DataObject_1_0 current;

            @Override
            public boolean hasNext() {
                return this.delegate.hasNext();
            }

            @Override
            public Entry<String, DataObject_1_0> next() {
                this.current = this.delegate.next();
                return new Map.Entry<String, DataObject_1_0>(){

                    @Override
                    public String getKey() {
                        return DatastoreIterator.this.current.jdoGetObjectId().getLastSegment().toClassicRepresentation();
                    }

                    @Override
                    public DataObject_1_0 getValue() {
                        return DatastoreIterator.this.current;
                    }

                    @Override
                    public DataObject_1_0 setValue(DataObject_1_0 value) {
                        throw new UnsupportedOperationException("An object can't be replaced");
                    }

                };
            }

            @Override
            public void remove() {
                if(this.current == null) {
                    throw new IllegalStateException("No current element");
                } else {
                    AbstractContainer_1.this.openmdxjdoGetDataObjectManager().deletePersistent(this.current);
                }
            }

        }

    }

    
    //------------------------------------------------------------------------
    // Class BatchingList
    //-----------------------------------------------------------------------

    /**
     * Batching List
     */
    class BatchingList 
        extends AbstractSequentialList<DataObject_1_0>
        implements BatchingCollection
    {

        /**
         * Constructor 
         *
         * @param queryType
         * @param query
         * @param fetchGroups
         */
        BatchingList(
            String queryType,
            QueryFilterRecord query,
            Set<String> fetchGroups
        ) {
            this.queryType = queryType;
            this.query = query;
            this.sliceCache = null; 
            this.fetchGroups = fetchGroups;    
        }

        private final String queryType;        
        private final QueryFilterRecord query;        
        private final Set<String> fetchGroups;
        private Reference<DataObjectSlice>[] sliceCache;
        private Integer total = null;
        private List<DataObject_1_0> selectionCache;

        
        Integer getTotal(
        ) {
            return this.total; 
        }
        
        void setTotal(
            int total
        ) {
            this.total = Integer.valueOf(total);
            if(total == 0) {
                this.selectionCache = new ArrayList<DataObject_1_0>();
            }
        }

        /**
         * Populate extent caches
         * 
         * @param fetchPlan
         * @throws ServiceException 
         */
        private void populateCaches(
            FetchPlan fetchPlan
        ) throws ServiceException {
            final Map<Container_1_0,Map<String, DataObject_1_0>> caches = new IdentityHashMap<Container_1_0, Map<String, DataObject_1_0>>();
            buildCachesForNonEmptyContainers(caches, fetchPlan);
            buildCachesForEmptyContainers(caches);
            deployCaches(caches);            
        }

        /**
         * Deploy Caches
         * 
         * @param caches
         */
        private void deployCaches(
            Map<Container_1_0, Map<String, DataObject_1_0>> caches
        ) {
            for(Map.Entry<Container_1_0,Map<String, DataObject_1_0>> entry : caches.entrySet()) {
                final Map<String, DataObject_1_0> cache = entry.getValue();
                if(cache != NO_CACHE){
                    Container_1_0 container = entry.getKey();
                    if(!container.isRetrieved() && container instanceof Container_1){
                        ((Container_1)container).amendAndDeployCache(cache);
                    }
                }
            }
        }

        /**
         * Build caches for non-empty container
         * 
         * @param caches
         * @param fetchPlan
         */
        private void buildCachesForNonEmptyContainers(
            Map<Container_1_0, Map<String, DataObject_1_0>> caches,
            FetchPlan fetchPlan
        ) {
            for(
                Iterator<DataObject_1_0> i = this.listIterator(0, fetchPlan);
                i.hasNext();
            ){
                DataObject_1_0 object = i.next();
                if(!object.jdoIsDeleted()) {
                    addToCache(caches, object);
                }                    
            }
        }

        /**
         * Build caches for non-empty container
         * 
         * @param fetchPlan
         * @param caches
         * @throws ServiceException 
         */
        private void buildCachesForEmptyContainers(
            Map<Container_1_0, Map<String, DataObject_1_0>> caches
        ) throws ServiceException {
            Path memberPattern = getFilter().getIdentityPattern();
            Path parentPattern = memberPattern.getPrefix(memberPattern.size() - 2);
            String feature = memberPattern.getSegment(parentPattern.size()).toClassicRepresentation();
            Model_1_0 model = Model_1Factory.getModel();
            for(Object c : AbstractContainer_1.this.openmdxjdoGetDataObjectManager().getManagedObjects(EMPTY_CACHE_CANDIDATES)){
                DataObject_1_0 candidate = (DataObject_1_0) c;
                if(candidate.jdoGetObjectId().isLike(parentPattern)) {
                    ModelElement_1_0 classifierDef = model.getElement(candidate.objGetClass());
                    ModelElement_1_0 referenceDef = model.getFeatureDef(classifierDef, feature, false);
                    if(referenceDef != null && referenceDef.isReferenceType()) {
                        addEmptyCache(caches, candidate, feature);
                    }
                }
            }
        }
       
        /**
         * Populate non-extent cache
         * 
         * @param fetchPlan
         */
        private void populateCache(
            FetchPlan fetchPlan
        ) {
            List<DataObject_1_0> allCache = new ArrayList<DataObject_1_0>();
            for(
                Iterator<DataObject_1_0> i = this.listIterator(0, fetchPlan);
                i.hasNext();
            ){
                allCache.add(i.next());
            }
            this.selectionCache = allCache;
            this.evictSliceCache();
        }
        
        /**
         * Retrieve the collection
         * 
         * @param fetchPlan 
         * @throws ServiceException  
         */
        synchronized void retrieveAll(
            FetchPlan fetchPlan
        ) throws ServiceException {
            if(!this.isRetrieved()) {
                if(isPlainExtent()) {
                    populateCaches(fetchPlan);
                } else {
                    populateCache(fetchPlan);
                }
            }
        }

        boolean isRetrieved(
        ){
            return this.selectionCache != null;
        }
        
        synchronized public void evict(
        ){
            this.total = null;
            this.selectionCache = null;
            this.evictSliceCache();
        }
        
        /**
         * Retrieve a slice from the cache
         * 
         * @param index
         * 
         * @return the requested slice; or <code>null</code> if none is found
         */
        DataObjectSlice getSlice(
            int index
        ){
            Reference<DataObjectSlice>[] snapshot = this.sliceCache;
            if(snapshot != null) {
                for(Reference<DataObjectSlice> reference : snapshot) {
                    if(reference != null) {
                        DataObjectSlice slice = reference.get();
                        if(
                            slice != null &&
                            slice.offset <= index && 
                            index < slice.offset + slice.size()
                        ) {
                            return slice;
                        }
                    }
                }
            }
            return null;
        }

        private void evictSliceCache(){
            if(this.sliceCache != null) {
                Arrays.fill(this.sliceCache, null);
            }
        }
        
        /**
         * Add a slice to the cache
         * 
         * @param slice
         */
        @SuppressWarnings("unchecked")
        synchronized void addSlice(
            DataObjectSlice slice
        ){
            Reference<DataObjectSlice> reference = new SoftReference<DataObjectSlice>(slice);
            if(this.sliceCache == null) {
                this.sliceCache = new Reference[AbstractContainer_1.INITIAL_SLICE_CACHE_SIZE];
                this.sliceCache[0] = reference;
            } else {
                Reference<DataObjectSlice>[] oldCache = this.sliceCache;
                int oldLength = oldCache.length;
                for(
                    int i = 0;
                    i < oldLength;
                    i++
                ) {
                    Reference<DataObjectSlice> entry = oldCache[i];
                    if(entry == null || entry.get() == null) {
                        oldCache[i] = reference;
                        return;
                    }
                }
                this.sliceCache = new Reference[oldLength * 2];
                System.arraycopy(oldCache, 0, this.sliceCache, 0, oldLength);
                this.sliceCache[oldLength] = reference;
            }
        }

        @Override
        public ListIterator<DataObject_1_0> listIterator(
            int index
        ) {
            return this.isRetrieved() ? 
                this.selectionCache.listIterator(index) : 
                this.listIterator(index, null); // no fetch plan specified
        }

        @Override
        public
        ListIterator<DataObject_1_0> listIterator(
            int index,
            FetchPlan fetchPlan
        ){
           return new BatchingIterator(index, fetchPlan);  
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.AbstractContainer_1.BatchingCollection#size(javax.jdo.FetchPlan)
         */
        @Override
        public int size(FetchPlan fetchPlan) {
            Integer total = this.getTotal();
            if(total == null) {
                ListIterator<?> i = this.listIterator(0, fetchPlan);
                int count = 0; 
                while(i.hasNext()){
                    total = this.getTotal();
                    if(total != null) {
                        return total.intValue();
                    }
                    count++;
                    i.next();
                }
                return count;
            } else {
                return total.intValue();
            }
        }

        @Override
        public int size(
        ) {
            return size(null);
        }
        
        boolean isSmallerThanCacheThreshold(
        ){
            Integer total = this.getTotal();
            return 
                total != null && 
                total.intValue() < AbstractContainer_1.this.openmdxjdoGetDataObjectManager().getCacheThreshold();
        }

        @Override
        public boolean isEmpty(
        ) {
            Integer total = this.getTotal();
            return total != null ? total.intValue() == 0 : !this.listIterator(0).hasNext();
        }

        protected Query_2Facade newQuery(
        ) throws ResourceException{
            Query_2Facade query = Query_2Facade.newInstance(AbstractContainer_1.this.container().jdoGetObjectId());
            query.setQueryType(this.queryType);
            query.setQueryFilter(this.query);
            query.setGroups(this.fetchGroups);
            return query;
        }
        
        /**
         * Break the List contract to avoid round-trips
         */
        @Override
        public boolean equals(
            Object that
        ) {
            return this == that;
        }

        /**
         * Break the List contract to avoid round-trips
         */
        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        /**
         * Break the List contract to avoid round-trips
         */
        @Override
        public String toString(
        ){
            return this.getClass().getSimpleName() + " of " + AbstractContainer_1.this;
        }

        
        //--------------------------------------------------------------------
        // Class BatchingIterator
        //--------------------------------------------------------------------
        
        /**
         * Iterator
         */
        private class BatchingIterator implements ListIterator<DataObject_1_0> {

            /**
             * Constructor 
             * 
             * @param fetchPlan
             * @param index
             */
            @SuppressWarnings("unchecked")
            BatchingIterator(
                int index,
                FetchPlan fetchPlan 
            ) {
                this.nextIndex = index;
                this.previousIndex = index - 1;
                if(fetchPlan == null) {
                    this.batchSize = AbstractContainer_1.this.openmdxjdoGetDataObjectManager().getOptimalFetchSize();
                    this.fetchGroupNames = null;
                } else {
                    int fetchSize = fetchPlan.getFetchSize();
                    if(fetchSize == FetchPlan.FETCH_SIZE_OPTIMAL) {
                        this.batchSize = AbstractContainer_1.this.openmdxjdoGetDataObjectManager().getOptimalFetchSize();
                    } else if (fetchSize == FetchPlan.FETCH_SIZE_GREEDY) {
                        this.batchSize = AbstractContainer_1.BATCH_SIZE_GREEDY;
                    } else {
                        this.batchSize = fetchSize;
                    }
                    this.fetchGroupNames = fetchPlan.getGroups();
                 }
            }

            private final int batchSize;  
            private int previousIndex;
            private int nextIndex;
            private int highWaterMark = 0;
            private final Set<String> fetchGroupNames;
            private DataObjectSlice slice;

            /**
             * Retrieve the given element
             * 
             * @param index
             * @param ascending 
             * 
             * @return the given element
             */
            private DataObject_1_0 get(
                int index, 
                boolean ascending
            ){
                if(
                    this.slice == null || 
                    index < this.slice.offset || 
                    index >= this.slice.offset + this.slice.size()
                ) {
                    try {
                        if(!this.load(true, index)) {
                            throw Throwables.initCause(
                                new NoSuchElementException("Element " + index + " exceeds the size"),
                                null, // exception 
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.GENERIC,
                                new BasicException.Parameter("total", BatchingList.this.getTotal()),
                                new BasicException.Parameter("index", index)
                            );
                        }
                    }  catch (ResourceException exception) {
                        throw Throwables.initCause(
                            new NoSuchElementException("Could not retrieve element " + index),
                            exception, 
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.GENERIC,
                            new BasicException.Parameter("total", BatchingList.this.getTotal()),
                            new BasicException.Parameter("index", index)
                        );
                    }
                }
                return this.slice.get(index - this.slice.offset);
            }

            @Override
            public void add(
                DataObject_1_0 o
            ) {
                throw new UnsupportedOperationException("A list proxy is unmodifiable");
            }

            private boolean load(
                boolean ascending, 
                int index
            ) throws ResourceException {
                Integer total = BatchingList.this.getTotal();
                if(total == null || index < total.intValue()) {
                    this.slice = BatchingList.this.getSlice(index);
                    if(this.slice == null) {
                        Query_2Facade facade = BatchingList.this.newQuery();
                        facade.setPosition(Long.valueOf(ascending ? index : -(index+1)));
                        facade.setSize(Integer.valueOf(this.batchSize));
                        if(this.fetchGroupNames != null) {
                            facade.setGroups(this.fetchGroupNames);
                        }
                        IndexedRecord cache = (IndexedRecord) AbstractContainer_1.this.openmdxjdoGetDataObjectManager().getInteraction().execute(
                            AbstractContainer_1.this.openmdxjdoGetDataObjectManager().getInteractionSpecs().GET,
                            facade.getDelegate()
                        );
                        this.slice = new DataObjectSlice(ascending ? index : (index-cache.size()+1));
                        Long resultTotal = null;
                        Integer highWaterMark = null;
                        if(cache instanceof ResultRecord) {
                            ResultRecord result = (ResultRecord) cache;
                            resultTotal = result.getTotal();
                            if(resultTotal == null) {
                                Boolean hasMore = result.getHasMore();
                                if(Boolean.TRUE.equals(hasMore)) {
                                    highWaterMark = Integer.valueOf(index + cache.size() + 1);
                                } else if (
                                    Boolean.FALSE.equals(hasMore) &&
                                    (index == 0 || !cache.isEmpty()) 
                                ) {
                                    resultTotal = Long.valueOf(index + cache.size());
                                }
                            }
                        } else if(index == 0 && cache.isEmpty()) {
                            resultTotal = Long.valueOf(0);
                        }
                        if(resultTotal != null) {
                            BatchingList.this.setTotal(resultTotal.intValue());
                            this.highWaterMark = resultTotal.intValue();
                        } else if(
                            BatchingList.this.getTotal() == null &&
                            highWaterMark != null &&
                            this.highWaterMark < highWaterMark.intValue()
                        ) {
                            this.highWaterMark = highWaterMark.intValue();
                        }
                        for(Object o : cache) {
                            this.slice.add(AbstractContainer_1.this.openmdxjdoGetDataObjectManager().receive((ObjectRecord) o));
                        }
                        boolean loaded = !this.slice.isEmpty(); 
                        if(loaded && !AbstractContainer_1.this.isIgnoreCache()) {
                            BatchingList.this.addSlice(this.slice);
                        }
                        return loaded;
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }                
            }

            @Override
            public boolean hasNext(
            ) {
                try {
                    Integer total = BatchingList.this.getTotal(); 
                    return  total != null ? (
                        this.nextIndex < total.intValue() 
                    ) : (
                        this.nextIndex < this.highWaterMark || this.load(true, this.nextIndex)
                    );
                } catch (ResourceException exception) {
                    throw new RuntimeServiceException(exception);
                }
            }

            @Override
            public boolean hasPrevious(
            ) {
                return this.previousIndex >= 0;
            }

            @Override
            public DataObject_1_0 next(
            ) {
                if(this.hasNext()) {
                    return this.get(
                        this.previousIndex = this.nextIndex++, 
                        true
                    );
                } 
                throw new NoSuchElementException(
                    "nextIndex() >= size(): " + this.nextIndex
                );
            }

            @Override
            public int nextIndex(
            ) {
                return this.nextIndex;
            }

            @Override
            public DataObject_1_0 previous(
            ) {
                if(this.hasPrevious()) {
                    return this.get(
                        this.nextIndex = this.previousIndex--, 
                        false
                    );
                } 
                else throw new NoSuchElementException(
                    "previousIndex() < 0: " + this.previousIndex
                );
            }

            @Override
            public int previousIndex(
            ) {
                return this.previousIndex;
            }

            @Override
            public void remove(
            ) {
                throw new UnsupportedOperationException("A list proxy is unmodifiable");
            }

            @Override
            public void set(
                DataObject_1_0 o
            ) {
                throw new UnsupportedOperationException("A list proxy is unmodifiable");
            }

        }    

    }

    
    //------------------------------------------------------------------------
    // Class DataObjectSlice
    //------------------------------------------------------------------------

    /**
     * The slices are used as keys in the cache. That's why they should not
     * follow the List's standard contract for equality and hashCode!
     */
    @SuppressWarnings("serial")
    private static class DataObjectSlice extends ArrayList<DataObject_1_0> {

        /**
         * Constructor 
         *
         * @param offset
         */
        DataObjectSlice(
            int offset
        ){
            this.offset = offset;
        }

        /**
         * The slice's offset
         */
        final int offset;

        @Override
        public boolean equals(Object that) {
            return this == that;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

    }


    //------------------------------------------------------------------------
    // Class Members
    //------------------------------------------------------------------------

    /**
     * Members
     */
    private abstract class Members extends OrderingList {

        /**
         * Constructor 
         *
         * @param comparator
         */
        Members(
            Comparator<DataObject_1_0> comparator
        ) {
            super(comparator);
        }

        /**
         * Tells whether the collection refers to the whole container or a sub-set
         */
        private final boolean plain = AbstractContainer_1.this.getFilter() == null; 
        
        /**
         * Tells whether the collection refers to the whole container or a sub-set
         *
         * @return <code>true</code> if the collection refers to the whole container
         */
        protected final boolean isPlain(){
            return this.plain; 
        }
        
        @Override
        protected List<? extends DataObject_1_0> getSource(
        ){
            return AbstractContainer_1.this.openmdxjdoGetDataObjectManager().currentUnitOfWork().getMembers();
        }

        /**
         * Break the List contract to avoid round-trips
         */
        @Override
        public String toString(
        ){
            return this.getClass().getSimpleName() + " of " + AbstractContainer_1.this;
        }

    }


    //------------------------------------------------------------------------
    // Class Excluded
    //------------------------------------------------------------------------

    /**
     * Excluded Members
     */
    final class Excluded extends Members {

        /**
         * Constructor 
         */
        Excluded(
        ){
            super(null);
        }

        /**
         * Select the objects to be handled by their state
         * 
         * @param candidate
         * 
         * @return <code>true</code> if the object might be included according to its state
         */
        protected final boolean handles(
            Object candidate
        ){
            return this.isPlain() ? ReducedJDOHelper.isDeleted(candidate) : ReducedJDOHelper.isDirty(candidate);
        }

        @Override
        protected final boolean accept(
            Object candidate
        ) {
            return handles(candidate) && !ReducedJDOHelper.isNew(candidate) && isInContainerOrExtent(candidate);
        }

    }


    //------------------------------------------------------------------------
    // Class Included
    //------------------------------------------------------------------------

    /**
     * Included Members
     */
    final class Included extends Members {

        /**
         * Constructor 
         *
         * @param comparator
         */
        Included(
            Comparator<DataObject_1_0> comparator
        ){
            super(comparator);
        }

        /**
         * Select the objects to be handled by their state
         * 
         * @param candidate
         * 
         * @return <code>true</code> if the object might be included according to its state
         */
        protected final boolean handles(
            Object candidate
        ){
            return this.isPlain() ? ReducedJDOHelper.isNew(candidate) : ReducedJDOHelper.isDirty(candidate);
        }

        @Override
        protected final boolean accept(
            Object candidate
        ) {
            return handles(candidate) && AbstractContainer_1.this.containsValue(candidate);
        }

		/* (non-Javadoc)
		 * @see org.openmdx.base.accessor.rest.AbstractContainer_1.Members#getSource()
		 */
		@Override
		protected List<? extends DataObject_1_0> getSource() {
		    return isProxy() ? Collections.<DataObject_1_0>emptyList() : super.getSource();
		}
        
    }


    //------------------------------------------------------------------------
    // Class CleanIterator
    //------------------------------------------------------------------------

    /**
     * Clean Iterator
     */
    private static class CleanIterator implements ListIterator<DataObject_1_0> {
        
        /**
         * Constructor 
         *
         * @param excluded
         * @param delegate
         */
        CleanIterator(
            Excluded excluded, 
            ListIterator<DataObject_1_0> delegate,
            int index
        ) {
            super();
            this.excluded = excluded;
            this.delegate = delegate;
            for(int i = index;i > 0;i--){
                this.next();
            }
        }
        
        private final Excluded excluded;
        private final ListIterator<DataObject_1_0> delegate;
        private int nextIndex = 0;
        private int previousIndex = -1;
        private DataObject_1_0 prefetched = null;
        
        /**
         * @param o
         * @see java.util.ListIterator#add(java.lang.Object)
         */
        @Override
        public void add(DataObject_1_0 o) {
            throw new UnsupportedOperationException();
        }
        
        /**
         * @return
         * @see java.util.ListIterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            while(this.prefetched == null && this.delegate.hasNext()) {
                DataObject_1_0 candidate = this.delegate.next();
                if(!this.excluded.handles(candidate)) {
                    this.prefetched = candidate;
                }
            }
            return this.prefetched != null;
        }
        
        /**
         * @return
         * @see java.util.ListIterator#hasPrevious()
         */
        @Override
        public boolean hasPrevious() {
            return this.previousIndex >= 0;
        }
        
        /**
         * @return
         * @see java.util.ListIterator#next()
         */
        @Override
        public DataObject_1_0 next() {
            if(this.hasNext()) {
                this.previousIndex = this.nextIndex++;
                DataObject_1_0 next = this.prefetched;
                this.prefetched = null;
                return next;
            } else {
                throw new NoSuchElementException();
            }
        }
        
        /**
         * @return
         * @see java.util.ListIterator#nextIndex()
         */
        @Override
        public int nextIndex() {
            return this.nextIndex;
        }
        
        /**
         * @return
         * @see java.util.ListIterator#previous()
         */
        @Override
        public DataObject_1_0 previous() {
            if(this.hasPrevious()) {
                DataObject_1_0 candidate = this.delegate.previous();
                while(this.excluded.handles(candidate)){
                    candidate = this.delegate.previous();
                }
                this.nextIndex = this.previousIndex--;
                return candidate;
            } else {
                throw new NoSuchElementException();
            }
        }
        
        /**
         * @return
         * @see java.util.ListIterator#previousIndex()
         */
        @Override
        public int previousIndex() {
            return this.previousIndex;
        }
        
        /**
         * 
         * @see java.util.ListIterator#remove()
         */
        @Override
        public void remove() {
            this.delegate.remove();
        }
        
        /**
         * @param o
         * @see java.util.ListIterator#set(java.lang.Object)
         */
        @Override
        public void set(DataObject_1_0 o) {
            throw new UnsupportedOperationException();
        }
                
    }
    
    //------------------------------------------------------------------------
    // Class JoiningList
    //------------------------------------------------------------------------

    /**
     * Joining  List
     * <p>
     * <em>Note the difference between a <code>JoiningList</code> and a <code>java.util.List</code>:<br>
     * A <code>ChainingList</code>'s <code>size()</code> method uses <code>Integer.MAX_VALUE</code> 
     * to tell, that its size has not yet been calculated.<br>
     * That's why such a <code>Collection</code> has to be wrapped into a <code>CountingCollection</code>
     * before being returned by the API.</em>
     */
    static abstract class JoiningList 
        extends AbstractSequentialList<DataObject_1_0> 
    {

        /**
         * Constructor 
         *
         * @param included
         * @param stored
         * @param excluded
         */
        protected JoiningList(
            Included included,
            BatchingList stored,
            Excluded excluded
        ){
            this.comparator = included.getComparator();
            this.included = included;
            this.stored = stored;
            this.excluded = excluded;
        }

        protected final Comparator<DataObject_1_0> comparator;
        protected final Included included;
        protected final Excluded excluded;
        protected final BatchingList stored;

        protected Excluded getExcluded(
        ) {
            return this.excluded;
        }
        
        protected BatchingList getStored(
        ) {
           return this.stored;
        }
         
        public int size(
            FetchPlan fetchPlan 
        ){
            int storedSize = this.stored.size(fetchPlan);
            if(storedSize != Integer.MAX_VALUE) {
                if(storedSize == 0 && this.included.isEmpty()) {
                    return 0;
                } else if(this.excluded.isPlain()) {
                    return storedSize + this.included.size() - this.excluded.size();
                } else if(this.included.isEmpty() && this.excluded.isEmpty()) {
                    return storedSize;
                }
            }
            int total = this.included.size();
            for(DataObject_1_0 candidate : this.stored) {
                if(!this.excluded.handles(candidate)) {
                    total++;
                }
            }
            return total;
        }
        
        @Override
        public int size(
        ) {
            return size(null);
        }

        @Override
        public boolean isEmpty() {
            if(this.included.isEmpty()) {
                if(this.stored.isEmpty()) {
                    return true;
                } else if (this.excluded.isPlain()) {
                    if(this.excluded.isEmpty()) {
                        return false;
                    } else {
                        Integer total = this.stored.getTotal();
                        if(total != null && total.intValue() == this.excluded.size()){
                            return true;
                        } else {
                            return !this.stored.listIterator(this.excluded.size()).hasNext();
                        }
                    }
                } else {
                    Integer total = this.stored.getTotal();
                    if(total != null && total.intValue() > this.excluded.size()) {
                        return false;
                    } else {
                        for(DataObject_1_0 candidate : this.stored) {
                            if(!this.excluded.handles(candidate)) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            } else {
                return false;
            }
        }
        
        /**
         * Break the List contract to avoid round-trips
         */
        @Override
        public boolean equals(
            Object that
        ) {
            return this == that;
        }

        /**
         * Break the List contract to avoid round-trips
         */
        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        /**
         * Break the List contract to avoid round-trips
         */
        @Override
        public String toString(
        ){
            return this.getClass().getSimpleName() + "@" + System.identityHashCode(this);
        }

    }    

    
    //------------------------------------------------------------------------
    // Class ChainingList
    //------------------------------------------------------------------------

    /**
     * Chaining List
     */
    static final class ChainingList extends JoiningList implements BatchingCollection {

        /**
         * Constructor 
         *
         * @param dirty
         * @param stored
         * @param excluded
         */
        ChainingList(
            Included dirty,
            BatchingList stored,
            Excluded excluded
        ){
            super(dirty, stored, excluded); 
        }

        @Override
        public ListIterator<DataObject_1_0> listIterator(
            int index
        ) {
            return listIterator(index, null);
        }

        @Override
        public ListIterator<DataObject_1_0> listIterator(
            int index,
            FetchPlan fetchPlan
        ) {
            return new ChainingIterator(index, fetchPlan);
        }
        
        /**
         * Chaining Iterator
         */
        private class ChainingIterator implements ListIterator<DataObject_1_0>{

            /**
             * Constructor 
             *
             * @param index
             * @param fetchPlan
             */
            ChainingIterator (
                int index, 
                FetchPlan fetchPlan
            ){
                this.fetchPlan = fetchPlan;
                this.nextIndex = index;
                this.previousIndex = index - 1;
                this.dirtySize = ChainingList.this.included.size();
            }

            private int previousIndex;
            private int nextIndex;
            private int dirtySize;

            private ListIterator<DataObject_1_0> dirtyIterator = null;
            private ListIterator<DataObject_1_0> cleanIterator = null;
            private ListIterator<DataObject_1_0> currentIterator = null;
            private final FetchPlan fetchPlan;

            private ListIterator<DataObject_1_0> getIterator(
                int index
            ){
                if(index < this.dirtySize) {
                    if(this.dirtyIterator == null) {
                        this.dirtyIterator = included.listIterator(this.nextIndex);  
                    }
                    return this.dirtyIterator;
                } else {
                    if(this.cleanIterator == null){
                        this.cleanIterator = ChainingList.this.excluded.isEmpty() ? stored.listIterator(
                            this.nextIndex - this.dirtySize,
                            this.fetchPlan
                        ) : new CleanIterator(
                            ChainingList.this.excluded, 
                            ChainingList.this.stored.listIterator(
                                0, 
                                this.fetchPlan
                            ), 
                            this.nextIndex - this.dirtySize
                        );
                    }
                    return this.cleanIterator;
                }
            }

            @Override
            public boolean hasNext() {
                return this.getIterator(this.nextIndex).hasNext();
            }

            @Override
            public DataObject_1_0 next(
            ) {
                DataObject_1_0 current = (this.currentIterator = this.getIterator(this.nextIndex)).next();
                this.previousIndex = this.nextIndex++;
                return current;
            }

            @Override
            public boolean hasPrevious() {
                return this.getIterator(this.previousIndex).hasPrevious();
            }

            @Override
            public DataObject_1_0 previous() {
                DataObject_1_0 current = (this.currentIterator = this.getIterator(this.previousIndex)).previous();
                this.nextIndex = this.previousIndex--;
                return current;
            }

            @Override
            public int nextIndex() {
                return this.nextIndex;
            }

            @Override
            public int previousIndex() {
                return this.previousIndex;
            }

            @Override
            public void remove() {
                this.currentIterator.remove();
                if(this.currentIterator == this.dirtyIterator) {
                    this.dirtySize--;
                }
            }

            @Override
            public void set(DataObject_1_0 object) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void add(DataObject_1_0 object) {
                throw new UnsupportedOperationException();
            }

        }

    }    

    
    //------------------------------------------------------------------------
    // Class FilteringList
    //------------------------------------------------------------------------

    /**
     * Filtering List
     */
    private static abstract class FilteringList extends AbstractSequentialList<DataObject_1_0> {

        /**
         * Constructor 
         */
        FilteringList() {
            super();
        }

        /**
         * Retrieve list.
         *
         * @return Returns the list.
         */
        protected abstract List<? extends DataObject_1_0> getSource();

        @Override
        public ListIterator<DataObject_1_0> listIterator(int index) {
            return new FilteringListIterator(
                this.getSource(),
                index
            );
        }

        @Override
        public int size() {
            int size = 0;
            for(Object candidate : this.getSource()) {
                if(this.accept(candidate)) {
                    size++;
                }
            }
            return size;
        }

        @Override
        public boolean isEmpty() {
            Collection<?> source = this.getSource();
            if(!source.isEmpty()) {
                for(Object candidate : source) {
                    if(this.accept(candidate)) {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public boolean contains(Object object) {
            return this.accept(object) && this.getSource().contains(object);
        }

        /**
         * Tells whether the object is acceptable as a member of the collection
         * 
         * @param candidate the candidate to be tested
         * 
         * @return <code>true</code> if the object is acceptable as a member of the collection
         */
        protected boolean accept(
            Object candidate
        ){
            if(this.acceptAll()) {
                return true;
            } else {
                throw new UnsupportedOperationException(
                    "accept() must be overridden unless acceptAll() is true"
                );
            }
        }

        /**
         * Tells whether a filter should be applied to the candidates or not
         * 
         * @return <code>true</code> if no filter should be applied to the candidates 
         */
        protected boolean acceptAll (
        ){
            return false;   
        }

        /**
         * Break the List contract to avoid round-trips
         */
        @Override
        public boolean equals(
            Object that
        ) {
            return this == that;
        }

        /**
         * Break the List contract to avoid round-trips
         */
        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        /**
         * Filtering List Iterator
         */
        private class FilteringListIterator implements ListIterator<DataObject_1_0> {

            /**
             * Constructor 
             *
             * @param source
             * @param index
             */
            FilteringListIterator(
                List<? extends DataObject_1_0> source,
                    int index
            ){
                if(FilteringList.this.acceptAll()){
                    this.iterator = source.listIterator(index);
                    this.nextIndex = this.iterator.nextIndex();
                    this.previousIndex = this.iterator.previousIndex();
                } else {
                    this.iterator = source.listIterator();
                    this.nextIndex = 0;
                    this.previousIndex = -1;
                    while (index > this.nextIndex) {
                        this.next();
                    }
                }
            }

            private final ListIterator<? extends DataObject_1_0> iterator;  
            private int nextIndex;  
            private int previousIndex; 
            private DataObject_1_0 readAheadElement = null;        
            private DataObject_1_0 current = null;        
            private int readAheadCount = 0;        
            private boolean readAheadReady = false;

            @Override
            public boolean hasNext() {
                while(!this.readAheadReady && this.iterator.hasNext()) {
                    this.readAheadElement = this.iterator.next(); 
                    this.readAheadCount++;
                    this.readAheadReady = FilteringList.this.accept(this.readAheadElement);
                }
                return this.readAheadReady;
            }

            @Override
            public DataObject_1_0 next() {
                if(!this.hasNext()) {
                    throw new NoSuchElementException(
                        "End of list reached"
                    );
                }
                this.previousIndex = this.nextIndex++;
                return this.current = this.readAheadFlush();
            }

            @Override
            public boolean hasPrevious() {
                return this.previousIndex >= 0;
            }

            @Override
            public DataObject_1_0 previous(
            ){
                if(!this.hasPrevious()) throw new NoSuchElementException(
                    "Begin of list reached"
                );
                this.reposition();
                DataObject_1_0 candidate = this.iterator.previous();
                while (!FilteringList.this.accept(candidate)) {
                    candidate = this.iterator.previous();
                }
                this.nextIndex = this.previousIndex--;
                this.readAheadCount = 0;
                return this.current = candidate;
            }

            @Override
            public int nextIndex(
            ) {
                return this.nextIndex;
            }

            @Override
            public int previousIndex() {
                return this.previousIndex;
            }

            @Override
            public void remove() {
                if(this.current == null) {
                    throw new IllegalStateException(
                        "Iterator has no current element"
                    );
                }
                this.reposition();
                if(this.current.jdoIsPersistent()) {
                    this.current.jdoGetPersistenceManager().deletePersistent(this.current);
                } else {
                    this.iterator.remove();
                }
            }

            @Override
            public void set(DataObject_1_0 object) {
                throw new UnsupportedOperationException("Query result is unmodifiable");
            }

            @Override
            public void add(DataObject_1_0 object) {
                throw new UnsupportedOperationException("Query result is unmodifiable");
            }

            private DataObject_1_0 readAheadFlush(
            ){
                DataObject_1_0 object = this.readAheadElement;
                this.readAheadCount = 0;
                this.readAheadElement = null;
                this.readAheadReady = false;
                return object;
            }

            /**
             *
             */
            private void reposition(
            ){
                if(this.nextIndex == 0) throw new IllegalStateException(
                    "No element fetched yet"
                );
                while (readAheadCount-- > 0) {
                    this.iterator.previous();
                }
                this.readAheadFlush();
            }

        }

    }

    
    //------------------------------------------------------------------------
    // Class OrderingList
    //------------------------------------------------------------------------

    /**
     * Ordering List
     */
    private static abstract class OrderingList extends FilteringList {

        /**
         * Constructor 
         *
         * @param comparator
         */
        OrderingList(
            Comparator<DataObject_1_0> comparator
        ){
            this.comparator = comparator;
        }

        private final Comparator<DataObject_1_0> comparator;

        @Override
        public ListIterator<DataObject_1_0> listIterator(
            int index
        ) {
            if(this.comparator == null) {
                return super.listIterator(index);
            } else {
                List<DataObject_1_0> selection = new ArrayList<DataObject_1_0>();
                for(
                    ListIterator<DataObject_1_0> i = super.listIterator(0);
                    i.hasNext();
                ){
                    selection.add(i.next());
                }
                Collections.sort(selection, this.comparator);
                return new OrderingListIterator(
                    selection.listIterator(index)
                );
            }
        }

        Comparator<DataObject_1_0> getComparator(){
            return this.comparator;
        }
        
        /**
         * Ordering List Iterator
         */
        private class OrderingListIterator implements ListIterator<DataObject_1_0> {

            /**
             * Constructor 
             *
             * @param delegate
             */
            OrderingListIterator(
                ListIterator<DataObject_1_0> delegate
            ){
                this.delegate = delegate;
            }

            /**
             * 
             */
            private final ListIterator<DataObject_1_0> delegate;
            
            /**
             * 
             */
            private DataObject_1_0 current = null;

            @Override
            public void add(DataObject_1_0 e) {
                throw new UnsupportedOperationException("Query results are unmodifiable");
            }

            @Override
            public boolean hasNext() {
                return this.delegate.hasNext();
            }

            @Override
            public boolean hasPrevious() {
                return this.delegate.hasPrevious();
            }

            @Override
            public DataObject_1_0 next() {
                return this.current = this.delegate.next();
            }

            @Override
            public int nextIndex() {
                return this.delegate.nextIndex();
            }

            @Override
            public DataObject_1_0 previous() {
                return this.current = this.delegate.previous();
            }

            @Override
            public int previousIndex() {
                return this.delegate.previousIndex();
            }

            @Override
            public void remove() {
                this.delegate.remove();
                ReducedJDOHelper.getPersistenceManager(this.current).deletePersistent(this.current);
                this.current = null;
            }

            @Override
            public void set(DataObject_1_0 e) {
                throw new UnsupportedOperationException("Query results are unmodifiable");
            }

        }

    }

    
    //------------------------------------------------------------------------
    // Class MergingList
    //------------------------------------------------------------------------

    /**
     * Merging List
     */
    private static final class MergingList extends JoiningList implements BatchingCollection {

        /**
         * Constructor 
         *
         * @param dirty
         * @param stored
         * @param excluded 
         */
        protected MergingList(
            Included dirty,
            BatchingList stored,
            Excluded excluded
        ){
            super(dirty,stored,excluded);
        }

        @Override
        public ListIterator<DataObject_1_0> listIterator(
            int index
        ) {
            return listIterator(index, null);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.AbstractContainer_1.BatchingIterable#listIterator(int, javax.jdo.FetchPlan)
         */
        @Override
        public ListIterator<DataObject_1_0> listIterator(
            int index,
            FetchPlan fetchPlan
        ) {
            return new MergingIterator(index, fetchPlan);
        }

        /**
         * Merging Iterator
         */
        private final class MergingIterator implements ListIterator<DataObject_1_0> {

            /**
             * Constructor 
             *
             * @param index
             * @param fetchPlan the (optional) fetch plan
             */
            MergingIterator (
                int index, 
                FetchPlan fetchPlan
            ) {
                this.nextIndex = index;
                this.previousIndex = index - 1;
                this.dirtyIterator = MergingList.this.included.listIterator();
                // Iterate up to the requested index if there are dirty or excluded elements
                if(
                    this.dirtyIterator.hasNext() || 
                    !MergingList.this.getExcluded().isEmpty()
                ) {
                    this.cleanIterator = MergingList.this.getExcluded().isEmpty() ? MergingList.this.getStored().listIterator(
                        0,
                        fetchPlan
                    ) : new CleanIterator(
                        MergingList.this.getExcluded(),
                        MergingList.this.getStored().listIterator(0, fetchPlan),
                        0
                    );
                    for(int i = index; i > 0; i--) {
                        try {
                            this.next();
                        } 
                        catch (NoSuchElementException exception) {
                            throw Throwables.initCause(
                                new IndexOutOfBoundsException(
                                    "The given index is greater or equal to the collection's size"
                                ),
                                exception,
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_PARAMETER,
                                new BasicException.Parameter("index", index),
                                new BasicException.Parameter("size", index - i)
                            );
                        }
                    }
                }
                // Use list iterator in case there are no dirty elements
                else {
                    this.cleanIterator = MergingList.this.getStored().listIterator(index, fetchPlan); 
                }
            }
            
            private int previousIndex;
            private int nextIndex;

            private final ListIterator<DataObject_1_0> dirtyIterator;
            private final ListIterator<DataObject_1_0> cleanIterator;
            
            private DataObject_1_0 nextDirty = null;
            private DataObject_1_0 nextClean = null;
            private DataObject_1_0 previousDirty = null;
            private DataObject_1_0 previousClean = null;
            private boolean useDirty;            

            @Override
            public boolean hasNext(
            ) {
                return 
                    this.nextDirty != null ||
                    this.nextClean != null ||
                    this.dirtyIterator.hasNext() ||
                    this.cleanIterator.hasNext();
            }

            @Override
            public DataObject_1_0 next(
            ) {
                if(this.nextDirty == null && this.dirtyIterator.hasNext()) {
                    this.nextDirty = this.dirtyIterator.next();
                }
                if(this.nextClean == null && this.cleanIterator.hasNext()) {
                    this.nextClean = this.cleanIterator.next();
                }
                if(this.nextDirty == null && this.nextClean == null) {
                    throw new NoSuchElementException("End of clean and dirty lists reached");
                }
                this.useDirty =
                    this.nextDirty == null ? false :
                    this.nextClean == null ? true :
                    MergingList.this.comparator.compare(this.nextDirty, this.nextClean) <= 0;
                this.previousIndex = this.nextIndex++;
                DataObject_1_0 current;
                if(this.useDirty) {
                    current = this.nextDirty;
                    this.nextDirty = null;
                } else {
                    current = this.nextClean;
                    this.nextClean = null;
                }
                return current;
            }

            @Override
            public boolean hasPrevious() {
                return this.previousIndex > 0;
            }

            @Override
            public DataObject_1_0 previous(
            ) {
                if(this.previousDirty == null && this.dirtyIterator.hasPrevious()) {
                    this.previousDirty = this.dirtyIterator.previous();
                }
                if(this.previousClean == null && this.cleanIterator.hasPrevious()) {
                    this.previousClean = this.cleanIterator.previous();
                }
                if(this.previousDirty == null && this.previousClean == null) {
                    throw new NoSuchElementException("Beginning of clean and dirty lists reached");
                }
                this.useDirty =
                    this.previousDirty == null ? false :
                    this.previousClean == null ? true :
                    MergingList.this.comparator.compare(this.previousDirty, this.previousClean) > 0;
                this.nextIndex = this.previousIndex--;
                DataObject_1_0 current;
                if(this.useDirty) {
                    current = this.previousDirty;
                    this.previousDirty = null;
                } 
                else {
                    current = this.previousClean;
                    this.previousClean = null;
                }
                return current;
            }

            @Override
            public int nextIndex(
            ) {
                return this.nextIndex;
            }

            @Override
            public int previousIndex(
            ) {
                return this.previousIndex;
            }

            @Override
            public void remove(
            ) {
                (this.useDirty ? this.dirtyIterator : this.cleanIterator).remove();
            }

            @Override
            public void set(
                DataObject_1_0 object
            ) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void add(
                DataObject_1_0 object
            ) {
                throw new UnsupportedOperationException();
            }

        }

    }    

    //------------------------------------------------------------------------
    // Class ObjectComparator
    //------------------------------------------------------------------------

    /**
     * Object Comparator
     */
    static final class DataObjectComparator implements Comparator<DataObject_1_0> {

        /**
         * Constructor 
         *
         * @param order
         */
        private DataObjectComparator(
            FeatureOrderRecord[] order
        ){
            this.order = order;
        }

        /**
         * 
         */
        private final FeatureOrderRecord[] order;

        /**
         * Retrieve an ObjectComparator instance
         * 
         * @param order
         * 
         * @return an ObjectComparator instance; or <code>null</code> if
         * the order is <code>null</code> or has length <code>0</code>
         */
        static DataObjectComparator getInstance(
            FeatureOrderRecord[] order
        ){
            return order == null || order.length == 0 ? null : new DataObjectComparator(order);
        }
        
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static int compareValues(
            Object left, 
            Object right
        ) {
            if(left == null) {
                return right == null ? 0 : -1;
            } else if (right == null) {
                return +1;
            } else if(left instanceof XMLGregorianCalendar) {
                if(left instanceof ImmutableDatatype<?> != right instanceof ImmutableDatatype<?>){
                    ImmutableDatatypeFactory datatypeFactory = DatatypeFactories.immutableDatatypeFactory();
                    return datatypeFactory.toDate((XMLGregorianCalendar) left).compare(datatypeFactory.toDate((XMLGregorianCalendar) right));
                } else {
                    return ((XMLGregorianCalendar)left).compare((XMLGregorianCalendar) right);
                }
            } else {
                return ((Comparable)left).compareTo(right);
            }
        }
        
        @Override
        public int compare(
            DataObject_1_0 ox, 
            DataObject_1_0 oy
        ) {
            if(this.order == null){
                if(ox.jdoIsPersistent()) {
                    if(oy.jdoIsPersistent()) {
                        Path ix = (Path) ReducedJDOHelper.getObjectId(ox); 
                        Path iy = (Path) ReducedJDOHelper.getObjectId(oy); 
                        return ix.compareTo(iy);
                    } else {
                        return +1;
                    }
                } else {
                    if(oy.jdoIsPersistent()) {
                        return -1;
                    } else {
                        UUID ix = (UUID) ReducedJDOHelper.getTransactionalObjectId(ox);
                        UUID iy = (UUID) ReducedJDOHelper.getTransactionalObjectId(oy);
                        return ix.compareTo(iy);
                    }
                }
            } else {
                for(FeatureOrderRecord s : this.order){
                    if(s.getSortOrder() != SortOrder.UNSORTED) try {
                        Object vx = ox.objGetValue(s.getFeature());
                        Object vy = oy.objGetValue(s.getFeature());
                        int c = DataObjectComparator.compareValues(vx,vy);
                        if (c != 0) {
                            return s.getSortOrder() == SortOrder.ASCENDING ? c : -c;
                        }
                    } catch (ServiceException excpetion) {
                        // exclude field from comparison
                    }
                }
                return 0;
            }
        }

        @Override
        public boolean equals(Object that) {
            return 
                that instanceof DataObjectComparator &&
                Arrays.equals(this.order, ((DataObjectComparator) that).order);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.order);
        }

        public FeatureOrderRecord[] getDelegate(
        ){
            return this.order;
        }
        
    }

    //------------------------------------------------------------------------
    // Class DataObjectFilter
    //------------------------------------------------------------------------

    static class DataObjectFilter extends ObjectFilter {

		protected DataObjectFilter(
			DataObjectFilter superFilter,
			QueryFilterRecord filter, 
			boolean extentQuery
		) {
			super(superFilter, filter, extentQuery);
		}

		/**
		 * Implements <code>Serializable</code>
		 */
		private static final long serialVersionUID = 7352255651343841409L;
		
		/* (non-Javadoc)
		 * @see org.openmdx.base.accessor.rest.ObjectFilter#newFilter(org.openmdx.base.rest.cci.QueryFilterRecord)
		 */
		@Override
		protected AbstractFilter newFilter(QueryFilterRecord delegate) {
			return new DataObjectFilter(null, delegate, this.extentQuery);
		}

		/* (non-Javadoc)
		 * @see org.openmdx.base.accessor.rest.ObjectFilter#equal(java.lang.Object, java.lang.Object)
		 */
		@Override
		protected boolean equal(Object candidate, Object value) {
	        if(candidate instanceof DataObject_1_0) {
	        	if(value instanceof UUID) {
	        		return value.equals(ReducedJDOHelper.getTransactionalObjectId(candidate));
	        	} else if(value instanceof Path) {
	        		Path oid = (Path)value;
	        		if(oid.isTransactionalObjectId()) {
	        			return oid.toTransactionalObjectId().equals(ReducedJDOHelper.getTransactionalObjectId(candidate));
	        		} else {
	        			return oid.equals(ReducedJDOHelper.getObjectId(candidate));
	        		}
	        	} else {
	        		return false;
	        	}
	        } else {
				return super.equal(candidate, value);
	        }
		}
    	
	    protected ModelElement_1_0 getClassifier(
    		Object object
        ){
    		try {
				return ((DataObject_1)object).getClassifier();
			} catch (ServiceException e) {
				throw new RuntimeServiceException(e);
			}
        }
        
    	protected boolean isEmpty(
    		Object object,
    		final String featureName, 
    		QueryFilterRecord filter
    	) throws ServiceException {
    		return ((DataObject_1)object).objGetContainer(featureName).subMap(filter).isEmpty();
    	}

        @Override
        protected Iterator<?> getValuesIterator(
            Object candidate, 
            ConditionRecord condition
        ){
        	try {
	            String attribute = condition.getFeature();
	            DataObject_1 object = (DataObject_1)candidate;
	            String objectClass = object.objGetClass();
	            if(SystemAttributes.OBJECT_CLASS.equals(attribute)){
	                return Collections.singleton(objectClass).iterator();
	            } else {    
	                ModelElement_1_0 classifier = getClassifier(candidate);
	                if(SystemAttributes.OBJECT_INSTANCE_OF.equals(attribute)){
	                    return newInstanceOfIterator(classifier);
	                } else if("core".equals(attribute) && isCoreInstance(classifier)){
	                	return Collections.emptySet().iterator();
	                } else {
	                    ModelElement_1_0 featureDef = classifier.getModel().getFeatureDef(classifier, attribute, false);
	                    switch(ModelHelper.getMultiplicity(featureDef)) {
	                        case LIST:
	                            return object.objGetList(attribute).iterator();
	                        case SET:
	                            return object.objGetSet(attribute).iterator();
	                        case SPARSEARRAY:
	                            return object.objGetSparseArray(attribute).values().iterator();
	                        default:
	                            Object value = object.objGetValue(attribute);
	                            return (
	                                value == null ? Collections.emptySet() : Collections.singleton(value)
	                            ).iterator();
	                    }
	                }
	            }
        	} catch (ServiceException exception) {
        		throw new RuntimeServiceException(exception);
        	}
        }
        
	    /**
	     * Object filter factory
	     * 
	     * @param superFilter
	     * @param subFilter
	     * @param extentQuery 
	     * 
	     * @return a new object filter
	     */
	    static DataObjectFilter getInstance (
	        DataObjectFilter superFilter,
	        QueryFilterRecord subFilter, 
	        boolean extentQuery
	    ){
	        if(subFilter == null) {
	            return null;
	        }
	        List<ConditionRecord> conditions = subFilter.getCondition();
	        List<QueryExtensionRecord> extensions = subFilter.getExtension();
	        if(
	            (conditions == null || conditions.isEmpty()) && // TODO detect idempotent conditions
	            (extensions == null || extensions.isEmpty())
	        ){
	            return null;
	        }
	        return new DataObjectFilter(
	            superFilter,
	            subFilter,
	            extentQuery
	        );
	    }


    }
    
}
