/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Container_1 
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.accessor.rest;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;

import javax.jdo.FetchPlan;
import javax.jdo.JDOUserException;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.collection.Maps;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.PersistenceCapableCollection;
import org.openmdx.base.persistence.spi.StandardFetchPlan;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.query.Filter;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.cci.ConditionRecord;
import org.openmdx.base.rest.cci.ConsumerRecord;
import org.openmdx.base.rest.cci.FeatureOrderRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryExtensionRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.kernel.log.SysLog;

/**
 * Container
 */
abstract class AbstractContainer_1 extends AbstractConsumerAwareCollection {

    /**
     * Constructor for a persistent or transient container
     */
    protected AbstractContainer_1() {
        super();
    }

    /**
     * 
     */
    private transient ProcessingList persistent;

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
    private transient Set<Map.Entry<String, DataObject_1_0>> entries;

    /**
     * 
     */
    private transient Set<String> keys;

    /**
     * 
     */
    private transient ProcessingCollection values;

    /**
     * The initial slice cache size
     */
    protected static final int INITIAL_SLICE_CACHE_SIZE = 8;

    /**
     * This is required for the actual proxy implementation.
     */
    protected static final int BATCH_SIZE_GREEDY = Integer.MAX_VALUE;

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
     * Tells whether there is no filter to be considered
     * 
     * @return {@code true} if no filter is to be taken into consideration
     */
    protected final boolean isPlain(){
        return getFilter() == null;
    }
    
    /**
     * No need to provide caches for new objects
     */
    protected static final EnumSet<ObjectState> EMPTY_CACHE_CANDIDATES = EnumSet.of(
        ObjectState.HOLLOW_PERSISTENT_NONTRANSACTIONAL,
        ObjectState.PERSISTENT_CLEAN,
        ObjectState.PERSISTENT_DIRTY
    );

    protected static final QueryFilterRecord PLAIN = new Filter();

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoGetPersistenceManager()
     */
    @Override
    public abstract DataObjectManager_1 openmdxjdoGetDataObjectManager();

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.accessor.cci.Container_1_0#container()
     */
    @Override
    public abstract Container_1 container();

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.accessor.cci.Container_1_0#isRetrieved()
     */
    @Override
    public abstract boolean isRetrieved();

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        this.entrySet().clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<Entry<String, DataObject_1_0>> entrySet() {
        if (this.entries == null) {
            this.entries = new EntrySet();
        }
        return this.entries;
    }

    protected boolean isExtent() {
        return false;
    }

    protected boolean isPlainExtent() {
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

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        final Map<String, DataObject_1_0> cache = container().getCache();
        if (cache == null) {
            return this.getPersistent().isEmpty();
        } else if (cache.isEmpty()) {
            return true;
        } else if (AbstractContainer_1.this.isPlain()) {
            return false;
        } else {
            for (DataObject_1_0 candidate : cache.values()) {
                if (this.containsValue(candidate)) {
                    return false;
                }
            }
            return true;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<String> keySet() {
        if (this.keys == null) {
            this.keys = new KeySet(this);
        }
        return this.keys;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(
        Map<? extends String, ? extends DataObject_1_0> m
    ) {
        for (Map.Entry<? extends String, ? extends DataObject_1_0> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public DataObject_1_0 remove(
        Object key
    ) {
        DataObject_1_0 value = get(key);
        if (value != null) {
            if (value.jdoIsPersistent()) {
                this.openmdxjdoGetDataObjectManager().deletePersistent(value);
            } else {
                container().getCache().remove(key);
            }
        }
        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        final Map<String, DataObject_1_0> cache = container().getCache();
        if (cache == null) {
            return this.getPersistent().size();
        } else if (cache.isEmpty()) {
            return 0;
        } else if (this.isPlain()) {
            return cache.size();
        } else {
            int count = 0;
            for (DataObject_1_0 candidate : cache.values()) {
                if (this.containsValue(candidate)) {
                    count++;
                }
            }
            return count;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Map#values()
     */
    @Override
    public ProcessingCollection values() {
        if (this.values == null) {
            this.values = new UnorderedValues(this);
        }
        return this.values;
    }

    @Override
    public ProcessingList values(
        FetchPlan fetchPlan,
        FeatureOrderRecord... criteria
    ) {
        return new OrderedValues(
            DataObjectComparator.getInstance(criteria),
            fetchPlan == null ? StandardFetchPlan.newInstance(null) : fetchPlan
        );
    }

    @Override
    protected Processor processor(
        FetchPlan fetchPlan,
        FeatureOrderRecord[] criteria
    ) {
        if (criteria == null || criteria.length == 0) {
            final Map<String, DataObject_1_0> cache = container().getCache();
            if (cache == null) {
                return AbstractContainer_1.this.getPersistent();
            } else {
                return values();
            }
        } else {
            return values(fetchPlan, criteria);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.accessor.cci.Container_1_0#subMap(org.openmdx.base.query.Filter)
     */
    @Override
    public Container_1_0 subMap(
        QueryFilterRecord filter
    ) {
        DataObjectFilter objectFilter = DataObjectFilter.getInstance(this.getFilter(), filter, isExtent());
        return objectFilter == null ? this : new Selection_1(this, objectFilter);
    }

    /**
     * Evict the collection's cached slices
     */
    protected void evictStored() {
        if (this.stored != null) {
            this.stored.evict();
        }
    }

    /**
     * Evict the collection's members
     */
    protected void evictMembers() {
        for (Object candidate : (Set<?>) this.openmdxjdoGetDataObjectManager().getManagedObjects(
            EnumSet.of(ObjectState.PERSISTENT_CLEAN, ObjectState.PERSISTENT_DIRTY)
        )) {
            if (this.containsValue(candidate)) {
                ((DataObject_1) candidate).evict();
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
    ) {
        return container().containsObject(candidate);
    }

    /**
     * 
     * @return
     */
    protected Excluded getExcluded() {
        if (this.excluded == null) {
            this.excluded = new Excluded();
        }
        return this.excluded;
    }

    /**
     * 
     * @return
     */
    protected Included getIncluded() {
        if (this.included == null) {
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
    ) {
        final FeatureOrderRecord[] order = comparator == null ? null : comparator.getDelegate();
        final List<ConditionRecord> conditions = getConditions();
        final List<QueryExtensionRecord> extensions = getExtensions();

        final QueryFilterRecord query;
        final QueryFilterRecord key;
        if ((conditions == null || conditions.isEmpty()) &&
            (order == null || order.length == 0) &&
            (extensions == null || extensions.isEmpty())) {
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

    protected BatchingList getStored() {
        if (this.stored == null) {
            this.stored = this.getStored(
                null,
                null
            );
        }
        return this.stored;
    }

    protected boolean hasStored() {
        return this.stored == null;
    }

    /**
     * Proxy access requires the persistent managers to be synchronized
     */
    void synchronize() {
        if (isProxy()) {
            try {
                if (openmdxjdoGetDataObjectManager().currentUnitOfWork().synchronize() && this.persistent != null) {
                    ((BatchingList) this.persistent).evict();
                }
            } catch (ServiceException exception) {
                throw new JDOUserException(
                    "Unable to synchronize proxy",
                    exception
                );
            }
        }
    }

    protected ProcessingList getPersistent() {
        synchronize();
        if (this.persistent == null) {
            this.persistent = this.isProxy() ? this.getStored() : new ChainingList(
                this.getIncluded(),
                this.getStored(),
                this.getExcluded()
            );
        }
        return this.persistent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoRefresh()
     */
    @Override
    public void openmdxjdoRefresh() {
        openmdxjdoGetDataObjectManager().currentUnitOfWork().refreshMembers(this);
    }

    /**
     * Creates a data object cache
     * 
     * @param owner
     *            the owner determines whether thread safety is required
     * 
     * @return a newly created cache
     */
    protected static Map<String, DataObject_1_0> newObjectCache(
        DataObject_1_0 owner
    ) {
        return Maps.newMap(owner.objThreadSafetyRequired());
    }

    /**
     * Create and populate cache for persistent objects
     * 
     * @param caches
     * @param object
     */
    static void addToCache(
        Map<Container_1_0, Map<String, DataObject_1_0>> caches,
        DataObject_1_0 object
    ) {
        Container_1_0 container = object.getContainer(false);
        if (container != null) {
            Map<String, DataObject_1_0> cache = caches.get(container);
            if (cache == null) {
                caches.put(
                    container,
                    cache = container.isRetrieved() || !container.jdoIsPersistent() ? NO_CACHE : newObjectCache(object)
                );
            }
            if (cache != NO_CACHE) {
                if (object.jdoIsPersistent()) {
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
        Map<Container_1_0, Map<String, DataObject_1_0>> caches,
        DataObject_1_0 parent,
        String feature
    )
        throws ServiceException {
        final Container_1_0 container = parent.objGetContainer(feature);
        if (!container.isRetrieved()) {
            final Map<String, DataObject_1_0> cache = caches.get(container);
            if (cache == null) {
                caches.put(
                    container,
                    newObjectCache(parent)
                );
            }
        }
    }

    protected static String toFetchGroupName(
        Set<String> fetchGroups
    ) {
        if (fetchGroups == null) {
            return null;
        }
        switch (fetchGroups.size()) {
            case 0:
                return null;
            case 1:
                return fetchGroups.iterator().next();
            default:
                throw new RuntimeServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "At most one fetch group may be specified",
                    new BasicException.Parameter("groups", fetchGroups)
                );
        }
    }

    //------------------------------------------------------------------------
    // Class OrderedValues
    //-----------------------------------------------------------------------

    /**
     * The values may be filtered and/or ordered
     */
    private class OrderedValues
        extends AbstractProcessingPersistenceCapableSequentialList
        implements PersistenceCapableCollection {

        /**
         * Constructor
         */
        OrderedValues(
            DataObjectComparator comparator,
            FetchPlan fetchPlan
        ) {
            this.comparator = comparator;
            this.fetchPlan = fetchPlan;
        }

        private final DataObjectComparator comparator;
        protected final FetchPlan fetchPlan;

        private BatchingCollection persistent;
        private BatchingList stored;

        @SuppressWarnings("unchecked")
        private BatchingList getStored() {
            if (this.stored == null) {
                this.stored = AbstractContainer_1.this.getStored(
                    this.comparator,
                    this.fetchPlan.getGroups()
                );
            }
            return this.stored;
        }

        private BatchingCollection getPersistent() {
            synchronize();
            if (this.persistent == null) {
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
        protected ProcessingList processor() {
            final Map<String, DataObject_1_0> cache = container().getCache();
            if (useCache(cache)) {
                return getCachedValues(cache);
            } else if (AbstractContainer_1.this.isRetrieved()) {
                return getRetrievedValues();
            } else {
                return getPersistent();
            }
        }

        @Override
        public ListIterator<DataObject_1_0> listIterator(
            int index
        ) {
            final Map<String, DataObject_1_0> cache = container().getCache();
            if (useCache(cache)) {
                return new ValueIterator(
                    getCachedValues(cache).listIterator(index),
                    true
                );
            } else if (AbstractContainer_1.this.isRetrieved()) {
                return new ValueIterator(
                    getRetrievedValues().listIterator(index),
                    true
                );
            } else {
                List<DataObject_1_0> persistent = getPersistent();
                return new ValueIterator(
                    persistent instanceof BatchingCollection ? ((BatchingCollection) persistent).listIterator(index, this.fetchPlan)
                        : persistent.listIterator(index),
                    false
                );
            }
        }

        private ProcessingList getRetrievedValues() {
            final ProcessingList values = new ProcessingArrayList(
                AbstractContainer_1.this.values()
            );
            if (this.comparator != null) {
                Collections.sort(values, this.comparator);
            }
            return values;
        }

        private ProcessingList getCachedValues(
            final Map<String, DataObject_1_0> cache
        ) {
            final ProcessingList values = new ProcessingArrayList();
            for (DataObject_1_0 candidate : cache.values()) {
                if (AbstractContainer_1.this.containsValue(candidate)) {
                    values.add(candidate);
                }
            }
            if (this.comparator != null) {
                Collections.sort(values, this.comparator);
            }
            return values;
        }

        private boolean useCache(
            final Map<String, DataObject_1_0> cache
        ) {
            return cache != null && !AbstractContainer_1.this.isIgnoreCache();
        }

        @Override
        public int size() {
            final Map<String, DataObject_1_0> cache = container().getCache();
            if (useCache(cache)) {
                int count = 0;
                for (DataObject_1_0 candidate : cache.values()) {
                    if (AbstractContainer_1.this.containsValue(candidate)) {
                        count++;
                    }
                }
                return count;
            } else {
                return this.getPersistent().size(fetchPlan);
            }
        }

        @Override
        public boolean isEmpty() {
            final Map<String, DataObject_1_0> cache = container().getCache();
            return cache == null ? this.getPersistent().isEmpty() : (cache.isEmpty() || super.isEmpty());
        }

        @Override
        public void clear() {
            for (Iterator<DataObject_1_0> i = this.listIterator(); i.hasNext();) {
                i.next();
                i.remove();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoEvict(boolean)
         */
        @Override
        public void openmdxjdoEvict(
            boolean allMembers, boolean allSubSets
        ) {
            AbstractContainer_1.this.openmdxjdoEvict(allMembers, allSubSets);
            if (this.stored != null) {
                this.stored.evict();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoGetContainerId()
         */
        @Override
        public Path jdoGetObjectId() {
            return (Path) AbstractContainer_1.this.jdoGetObjectId();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoGetPersistenceManager()
         */
        @Override
        public PersistenceManager openmdxjdoGetDataObjectManager() {
            return AbstractContainer_1.this.openmdxjdoGetDataObjectManager();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetPersistenceManager()
         */
        @Override
        public PersistenceManager jdoGetPersistenceManager() {
            return AbstractContainer_1.this.jdoGetPersistenceManager();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoGetTransientContainerId()
         */
        @Override
        public TransientContainerId jdoGetTransactionalObjectId() {
            return (TransientContainerId) AbstractContainer_1.this.jdoGetTransactionalObjectId();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoIsPersistent()
         */
        @Override
        public boolean jdoIsPersistent() {
            return AbstractContainer_1.this.jdoIsPersistent();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoRefresh()
         */
        @Override
        public void openmdxjdoRefresh() {
            if (this.stored != null) {
                this.stored.evict();
            }
            AbstractContainer_1.this.openmdxjdoRefresh();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoRetrieve(javax.jdo.FetchPlan)
         */
        @Override
        public void openmdxjdoRetrieve(
            FetchPlan fetchPlan
        ) {
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
        public String toString() {
            return this.getClass().getName() + "@" + System.identityHashCode(this);
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
            ) {
                this.delegate = delegate;
                this.updateDelegate = updateDelegate;
            }

            private final boolean updateDelegate;

            private final ListIterator<DataObject_1_0> delegate;

            private DataObject_1_0 current = null;

            @Override
            public void add(
                DataObject_1_0 o
            ) {
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
                if (this.updateDelegate) {
                    this.delegate.remove();
                }
                openmdxjdoGetDataObjectManager().deletePersistent(this.current);
            }

            @Override
            public void set(
                DataObject_1_0 o
            ) {
                throw new UnsupportedOperationException("Query result is unmodifiable");
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
        EntrySet() {
            super();
        }

        @Override
        public Iterator<java.util.Map.Entry<String, DataObject_1_0>> iterator() {
            final Map<String, DataObject_1_0> cache = container().getCache();
            return cache == null ? 
                new DatastoreIterator(AbstractContainer_1.this.getPersistent().listIterator(0)) : 
                new CacheIterator(cache);
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
        public boolean add(
            java.util.Map.Entry<String, DataObject_1_0> o
        ) {
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
            for (Iterator<Map.Entry<String, DataObject_1_0>> i = this.iterator(); i.hasNext();) {
                i.next();
                i.remove();
            }
        }

        @Override
        public boolean contains(
            Object o
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(
            Collection<?> c
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(
            Object o
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(
            Collection<?> c
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(
            Collection<?> c
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T[] toArray(
            T[] a
        ) {
            throw new UnsupportedOperationException();
        }

        //--------------------------------------------------------------------
        // Class CacheIterator
        //--------------------------------------------------------------------

        /**
         * Entry Iterator
         */
        private class CacheIterator implements Iterator<Map.Entry<String, DataObject_1_0>> {

            /**
             * Take a snapshot of the keys
             *
             * @param cache
             */
            CacheIterator(
                Map<String, DataObject_1_0> cache
            ) {
                this.cache = cache;
                if(AbstractContainer_1.this.isPlain()) {
                    keys = new ArrayList<String>(cache.keySet());
                } else {
                    keys = new ArrayList<String>();
                    for(Map.Entry<String, DataObject_1_0> entry : cache.entrySet()) {
                        if (AbstractContainer_1.this.containsValue(entry.getValue())) {
                            keys.add(entry.getKey());
                        }
                    }
                }
            }

            private final Map<String, DataObject_1_0> cache;
            private final List<String> keys;
            private int index = 0;
            private DataObject_1_0 current = null;

            @Override
            public boolean hasNext() {
                return index < keys.size();
            }

            @Override
            public java.util.Map.Entry<String, DataObject_1_0> next() {
                if (this.hasNext()) {
                    final String key = keys.get(index++);
                    final DataObject_1_0 value = current = this.cache.get(key);
                    return new Map.Entry<String, DataObject_1_0>() {

                        @Override
                        public String getKey() {
                            return key;
                        }

                        @Override
                        public DataObject_1_0 getValue() {
                            return value;
                        }

                        @Override
                        public DataObject_1_0 setValue(
                            DataObject_1_0 value
                        ) {
                            throw new UnsupportedOperationException("Object can't be replaced");
                        }
                    };
                } else {
                    throw new NoSuchElementException();
                }
            }
            
            @Override
            public void remove() {
                if (this.current == null) {
                    throw new IllegalStateException("No current element");
                }
                if (current.jdoIsPersistent()) {
                    openmdxjdoGetDataObjectManager().deletePersistent(current);
                } else {
                    container().getCache().values().remove(current);
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
            ) {
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
                return new Map.Entry<String, DataObject_1_0>() {

                    @Override
                    public String getKey() {
                        return DatastoreIterator.this.current.jdoGetObjectId().getLastSegment().toClassicRepresentation();
                    }

                    @Override
                    public DataObject_1_0 getValue() {
                        return DatastoreIterator.this.current;
                    }

                    @Override
                    public DataObject_1_0 setValue(
                        DataObject_1_0 value
                    ) {
                        throw new UnsupportedOperationException("An object can't be replaced");
                    }

                };
            }

            @Override
            public void remove() {
                if (this.current == null) {
                    throw new IllegalStateException("No current element");
                } else {
                    openmdxjdoGetDataObjectManager().deletePersistent(this.current);
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
        extends AbstractProcessingSequentialList
        implements BatchingCollection {

        /**
         * Constructor
         */
        BatchingList(
            String queryType,
            QueryFilterRecord queryFilter,
            Set<String> fetchGroups
        ) {
            this.queryType = queryType;
            this.queryFilter = queryFilter;
            this.sliceCache = null;
            this.fetchGroups = fetchGroups;
        }

        private final String queryType;
        private final QueryFilterRecord queryFilter;
        private final Set<String> fetchGroups;
        private Reference<DataObjectSlice>[] sliceCache;
        private Integer total = null;
        private List<DataObject_1_0> selectionCache;

        Integer getTotal() {
            return this.total;
        }

        void setTotal(
            int total
        ) {
            this.total = Integer.valueOf(total);
            if (total == 0) {
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
        )
            throws ServiceException {
            final Map<Container_1_0, Map<String, DataObject_1_0>> caches = new IdentityHashMap<Container_1_0, Map<String, DataObject_1_0>>();
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
            for (Map.Entry<Container_1_0, Map<String, DataObject_1_0>> entry : caches.entrySet()) {
                final Map<String, DataObject_1_0> cache = entry.getValue();
                if (cache != NO_CACHE) {
                    Container_1_0 container = entry.getKey();
                    if (!container.isRetrieved() && container instanceof Container_1) {
                        ((Container_1) container).amendAndDeployCache(cache);
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
            for (Iterator<DataObject_1_0> i = this.listIterator(0, fetchPlan); i.hasNext();) {
                DataObject_1_0 object = i.next();
                if (!object.jdoIsDeleted()) {
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
        )
            throws ServiceException {
            Path memberPattern = getFilter().getIdentityPattern();
            Path parentPattern = memberPattern.getPrefix(memberPattern.size() - 2);
            String feature = memberPattern.getSegment(parentPattern.size()).toClassicRepresentation();
            Model_1_0 model = Model_1Factory.getModel();
            for (Object c : openmdxjdoGetDataObjectManager().getManagedObjects(EMPTY_CACHE_CANDIDATES)) {
                DataObject_1_0 candidate = (DataObject_1_0) c;
                if (candidate.jdoGetObjectId().isLike(parentPattern)) {
                    ModelElement_1_0 classifierDef = model.getElement(candidate.objGetClass());
                    ModelElement_1_0 referenceDef = model.getFeatureDef(classifierDef, feature, false);
                    if (referenceDef != null && referenceDef.isReferenceType()) {
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
            Collector allCache = new Collector();
            this.processAll(allCache, fetchPlan);
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
        )
            throws ServiceException {
            if (!this.isRetrieved()) {
                if (isPlainExtent()) {
                    populateCaches(fetchPlan);
                } else {
                    populateCache(fetchPlan);
                }
            }
        }

        boolean isRetrieved() {
            return this.selectionCache != null;
        }

        synchronized public void evict() {
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
        ) {
            Reference<DataObjectSlice>[] snapshot = this.sliceCache;
            if (snapshot != null) {
                for (Reference<DataObjectSlice> reference : snapshot) {
                    if (reference != null) {
                        DataObjectSlice slice = reference.get();
                        if (slice != null &&
                            slice.offset <= index &&
                            index < slice.offset + slice.size()) {
                            return slice;
                        }
                    }
                }
            }
            return null;
        }

        private void evictSliceCache() {
            if (this.sliceCache != null) {
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
        ) {
            Reference<DataObjectSlice> reference = new SoftReference<DataObjectSlice>(slice);
            if (this.sliceCache == null) {
                this.sliceCache = new Reference[AbstractContainer_1.INITIAL_SLICE_CACHE_SIZE];
                this.sliceCache[0] = reference;
            } else {
                Reference<DataObjectSlice>[] oldCache = this.sliceCache;
                int oldLength = oldCache.length;
                for (int i = 0; i < oldLength; i++) {
                    Reference<DataObjectSlice> entry = oldCache[i];
                    if (entry == null || entry.get() == null) {
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
            return this.isRetrieved() ? this.selectionCache.listIterator(index) : this.listIterator(index, null); // no fetch plan specified
        }

        @Override
        public ListIterator<DataObject_1_0> listIterator(
            int index,
            FetchPlan fetchPlan
        ) {
            if(fetchPlan != null && fetchPlan.getFetchSize() == FetchPlan.FETCH_SIZE_GREEDY) {
                openmdxjdoRetrieve(fetchPlan);
                if(this.isRetrieved()) { // which should be true after openmdxjdoRetrieve()
                    return this.selectionCache.listIterator(index);
                }
            }
            return new BatchingIterator(index, fetchPlan);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.accessor.rest.AbstractContainer_1.BatchingCollection#size(javax.jdo.FetchPlan)
         */
        @Override
        public int size(
            FetchPlan fetchPlan
        ) {
            Integer total = this.getTotal();
            if (total == null) {
                ListIterator<?> i = this.listIterator(0, fetchPlan);
                int count = 0;
                while (i.hasNext()) {
                    total = this.getTotal();
                    if (total != null) {
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
        public int size() {
            return size(null);
        }

        boolean isSmallerThanCacheThreshold() {
            Integer total = this.getTotal();
            return total != null &&
                total.intValue() < openmdxjdoGetDataObjectManager().getCacheThreshold();
        }

        @Override
        public boolean isEmpty() {
            Integer total = this.getTotal();
            return total != null ? total.intValue() == 0 : !this.listIterator(0).hasNext();
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
        public String toString() {
            return this.getClass().getSimpleName() + " of " + AbstractContainer_1.this;
        }

        @Override
        protected DataObjectManager_1 openmdxjdoGetDataObjectManager() {
            return AbstractContainer_1.this.openmdxjdoGetDataObjectManager();
        }

        @Override
        protected void processAll(
            ConsumerRecord consumer,
            FetchPlan fetchPlan
        ) {
            try {
                final DataObjectManager_1 dataObjectManager = openmdxjdoGetDataObjectManager();
                final QueryRecord query = Records.getRecordFactory().createMappedRecord(QueryRecord.class);
                query.setResourceIdentifier(AbstractContainer_1.this.container().jdoGetObjectId());
                query.setQueryType(this.queryType);
                query.setQueryFilter(this.queryFilter);
                query.setFetchGroupName(getFetchGroupName(fetchPlan));
                dataObjectManager.getInteraction().execute(
                    dataObjectManager.getInteractionSpecs().GET,
                    query,
                    consumer
                );
            } catch (ResourceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        private String getFetchGroupName(FetchPlan fetchPlan) {
            return toFetchGroupName(getFetchGroups(fetchPlan));
        }

        @SuppressWarnings("unchecked")
        private Set<String> getFetchGroups(FetchPlan fetchPlan) {
            return fetchPlan == null ? this.fetchGroups : fetchPlan.getGroups();
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
                // actual
                if (fetchPlan == null) {
                    this.batchSize = toBatchSize(FetchPlan.FETCH_SIZE_OPTIMAL);
                    this.fetchGroupNames = null;
                } else {
                    this.batchSize = toBatchSize(fetchPlan.getFetchSize());
                    this.fetchGroupNames = fetchPlan.getGroups();
                }
            }

            private final int batchSize;
            private int previousIndex;
            private int nextIndex;
            private int highWaterMark = 0;
            private final Set<String> fetchGroupNames;
            private DataObjectSlice slice;

            private int toBatchSize(int fetchSize){
                if(isProxy()) {
                    if(fetchSize == FetchPlan.FETCH_SIZE_OPTIMAL) {
                        return AbstractContainer_1.this.openmdxjdoGetDataObjectManager().getOptimalFetchSize();
                    } else if (fetchSize == FetchPlan.FETCH_SIZE_GREEDY) {
                        return AbstractContainer_1.BATCH_SIZE_GREEDY;
                    }                
                }
                return fetchSize;
            }
            
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
            ) {
                if (this.slice == null ||
                    index < this.slice.offset ||
                    index >= this.slice.offset + this.slice.size()) {
                    try {
                        if (!this.load(true, index)) {
                            throw Throwables.initCause(
                                new NoSuchElementException("Element " + index + " exceeds the size"),
                                null, // exception 
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.GENERIC,
                                new BasicException.Parameter("total", BatchingList.this.getTotal()),
                                new BasicException.Parameter("index", index)
                            );
                        }
                    } catch (ResourceException exception) {
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
            )
                throws ResourceException {
                Integer total = BatchingList.this.getTotal();
                if (total == null || index < total.intValue()) {
                    this.slice = BatchingList.this.getSlice(index);
                    if (this.slice == null) {
                        QueryRecord query = Records.getRecordFactory().createMappedRecord(QueryRecord.class);
                        query.setResourceIdentifier(AbstractContainer_1.this.container().jdoGetObjectId());
                        query.setQueryType(BatchingList.this.queryType);
                        query.setQueryFilter(BatchingList.this.queryFilter);
                        query.setFetchGroupName(
                            toFetchGroupName(
                                this.fetchGroupNames == null ? BatchingList.this.fetchGroups : this.fetchGroupNames
                            )
                        );
                        query.setSize(Long.valueOf(this.batchSize));
                        query.setPosition(Long.valueOf(ascending ? index : -(index + 1)));
                        final DataObjectManager_1 dataObjectManager = openmdxjdoGetDataObjectManager();
                        final IndexedRecord cache = (IndexedRecord) dataObjectManager.getInteraction().execute(
                            dataObjectManager.getInteractionSpecs().GET,
                            query
                        );
                        this.slice = new DataObjectSlice(ascending ? index : (index - cache.size() + 1));
                        Long resultTotal = null;
                        Integer highWaterMark = null;
                        if (cache instanceof ResultRecord) {
                            ResultRecord result = (ResultRecord) cache;
                            resultTotal = result.getTotal();
                            if (resultTotal == null) {
                                Boolean hasMore = result.getHasMore();
                                if (Boolean.TRUE.equals(hasMore)) {
                                    highWaterMark = Integer.valueOf(index + cache.size() + 1);
                                } else if (Boolean.FALSE.equals(hasMore) &&
                                    (index == 0 || !cache.isEmpty())) {
                                    resultTotal = Long.valueOf(index + cache.size());
                                }
                            }
                        } else if (index == 0 && cache.isEmpty()) {
                            resultTotal = Long.valueOf(0);
                        }
                        if (resultTotal != null) {
                            BatchingList.this.setTotal(resultTotal.intValue());
                            this.highWaterMark = resultTotal.intValue();
                        } else if (BatchingList.this.getTotal() == null &&
                            highWaterMark != null &&
                            this.highWaterMark < highWaterMark.intValue()) {
                            this.highWaterMark = highWaterMark.intValue();
                        }
                        for (Object o : cache) {
                            this.slice.add(dataObjectManager.receive((ObjectRecord) o));
                        }
                        boolean loaded = !this.slice.isEmpty();
                        if (loaded && !AbstractContainer_1.this.isIgnoreCache()) {
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
            public boolean hasNext() {
                Integer total = BatchingList.this.getTotal();
                if (total == null) {
                    try {
                        return this.nextIndex < this.highWaterMark || this.load(true, this.nextIndex);
                    } catch (ResourceException exception) {
                        throw new RuntimeServiceException(exception);
                    }
                } else {
                    return this.nextIndex < total.intValue();
                }
            }

            @Override
            public boolean hasPrevious() {
                return this.previousIndex >= 0;
            }

            @Override
            public DataObject_1_0 next() {
                if (this.hasNext()) {
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
            public int nextIndex() {
                return this.nextIndex;
            }

            @Override
            public DataObject_1_0 previous() {
                if (this.hasPrevious()) {
                    return this.get(
                        this.nextIndex = this.previousIndex--,
                        false
                    );
                } else
                    throw new NoSuchElementException(
                        "previousIndex() < 0: " + this.previousIndex
                    );
            }

            @Override
            public int previousIndex() {
                return this.previousIndex;
            }

            @Override
            public void remove() {
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
    // Class Members
    //------------------------------------------------------------------------

    /**
     * Members
     */
    abstract class Members extends OrderingList {

        /**
         * Constructor
         */
        Members(
            Comparator<DataObject_1_0> comparator
        ) {
            super(comparator);
        }

        /**
         * Tells whether the collection refers to the whole container or a sub-set
         */
        private final boolean plain = AbstractContainer_1.this.isPlain();

        /**
         * Tells whether the collection refers to the whole container or a sub-set
         *
         * @return <code>true</code> if the collection refers to the whole container
         */
        protected final boolean isPlain() {
            return this.plain;
        }

        @Override
        protected Collection<? extends DataObject_1_0> getSource() {
            return openmdxjdoGetDataObjectManager().currentUnitOfWork().getMembers();
        }

        /**
         * Break the List contract to avoid round-trips
         */
        @Override
        public String toString() {
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
        Excluded() {
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
        ) {
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
         */
        Included(
            Comparator<DataObject_1_0> comparator
        ) {
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
        ) {
            return this.isPlain() ? ReducedJDOHelper.isNew(candidate) : ReducedJDOHelper.isDirty(candidate);
        }

        @Override
        protected final boolean accept(
            Object candidate
        ) {
            return handles(candidate) && AbstractContainer_1.this.containsValue(candidate);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.accessor.rest.AbstractContainer_1.Members#getSource()
         */
        @Override
        protected Collection<? extends DataObject_1_0> getSource() {
            return isProxy() ? Collections.<DataObject_1_0>emptyList() : super.getSource();
        }

    }

}
