/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Container_1.java,v 1.45 2010/03/31 14:36:38 hburger Exp $
 * Description: Container_1 
 * Revision:    $Revision: 1.45 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/31 14:36:38 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2010, OMEX AG, Switzerland
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

import static org.openmdx.base.naming.SpecialResourceIdentifiers.EXTENT_REFERENCES;

import java.io.Flushable;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.Container;
import org.openmdx.base.persistence.spi.QuerySpec;
import org.openmdx.base.persistence.spi.StandardFetchPlan;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.query.AttributeSpecifier;
import org.openmdx.base.query.Directions;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.ModelAwareFilter;
import org.openmdx.base.query.Orders;
import org.openmdx.base.query.Selector;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.base.text.conversion.JavaBeans;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.w3c.cci2.ImmutableDatatype;
import org.w3c.cci2.SparseArray;
import org.w3c.spi.DatatypeFactories;
import org.w3c.spi.ImmutableDatatypeFactory;

/**
 * Container
 */
class Container_1
    extends AbstractMap<String, DataObject_1_0>
    implements Flushable, Evictable, Container_1_0, Container 
{

    /**
     * Constructor for a persistent or transient container
     *
     * @param owner
     * @param feature
     */
    Container_1(
        DataObject_1 owner,
        String feature
    ){
        this.owner = owner;
        this.transientContainerId = new TransientContainerId(
            owner.jdoGetTransactionalObjectId(),
            feature
        );
        this.ignoreCache = !isComposite();
        this.validate = getManager().isProxy() ? null : Boolean.FALSE;
        this.cache = this.ignoreCache || isStored() ? null : new ConcurrentHashMap<String,DataObject_1_0>();
        this.stored = new ConcurrentHashMap<String,BatchingList>();
        this.entrySet = isExtent() ? null : new EntrySet(null);
    }

    /**
     * The container id is lazily populated
     */
    private Path containerId;

    /**
     * 
     */
    private final TransientContainerId transientContainerId;
    
    /**
     * The owner might be replaced by an aspect's core object
     */
    private final DataObject_1 owner;

    /**
     * <code>true</code> if cache must be ignored.
     */
    private final boolean ignoreCache;
    
    /**
     * Tells whether all objects must be validated
     */
    private Boolean validate;
    
    /**
     * This collection<ul>
     * <li>does not include <em>PERSISTENT-DELETED</em> or <em>PERSISTENT-NEW-DELETED</em> instances
     * <li>does include <em>TRANSIENT</code> instances
     * </ul>
     */
    private ConcurrentMap<String, DataObject_1_0> cache = null;

    /**
     * The container's query type
     */
    private String queryType;

    /**
     * Query to list mapping
     */
    private ConcurrentMap<String,BatchingList> stored;
    
    /**
     * 
     */
    private final EntrySet entrySet;

    /**
     * 
     */
    protected static final int BATCH_SIZE_GREEDY = Integer.MAX_VALUE;

    /**
     * The initial slice cache size
     */
    protected static final int INITIAL_SLICE_CACHE_SIZE = 8;

    /**
     * Determine whether the container is composite or not
     * 
     * @return <code>false</code> unless the container is composite
     */
    private boolean isComposite(
   ){
        try {
            Model_1_0 model = getManager().getModel();
            ModelElement_1_0 classDef = model.getElement(this.owner.objGetClass());
            ModelElement_1_0 reference = model.getFeatureDef(classDef, this.transientContainerId.getFeature(), true);
            ModelElement_1_0 referencedEnd = model.getElement(reference.objGetValue("referencedEnd"));
            return AggregationKind.COMPOSITE.equals(referencedEnd.objGetValue("aggregation"));
        } catch(Exception e) {
            return false;
        }
    }
        
    /**
     * Tells whether all objects must be validated
     * 
     * @return <code>true</code> if all objects must be validated
     */
    private boolean mustValidate(
    ){
        if(this.validate == null) {
            Path xri = this.openmdxjdoGetContainerId();
            if(xri != null) try {
                this.validate = Boolean.valueOf(getManager().getModel().containsSharedAssociation(xri));
            } catch (Exception exception) {
                this.validate = Boolean.TRUE;
            }
        }
        return Boolean.TRUE.equals(this.validate);
    } 
    
    /**
     * Add an object to the cache
     * @param key
     * @param value
     */
    void addToCache(
        String key,
        DataObject_1_0 value
    ){
        if(isRetrieved()) {
            this.cache.put(key, value);
        }
    }

    /**
     * Add an object to the cache
     * @param key
     * @param value
     */
    void removeFromChache(
        String key
    ){
        if(isRetrieved()) {
            this.cache.remove(key);
        }
    }

    public boolean openmdxjdoIsPersistent(){
        return this.containerId != null || this.owner.jdoIsPersistent();
    }

    public Path openmdxjdoGetContainerId() {
        if(this.containerId == null && this.owner.jdoIsPersistent()) {
            this.containerId = this.owner.jdoGetObjectId().getChild(this.transientContainerId.getFeature());
        }
        return this.containerId;
    }

    public TransientContainerId openmdxjdoGetTransientContainerId() {
        return this.transientContainerId;
    }


    @Override
    public boolean isEmpty() {
        return entrySet().isEmpty();
    }

    @Override
    public DataObject_1_0 get(
        Object key
    ) {
        if(key instanceof String) {
            String qualifier = (String) key;
            if(isRetrieved()) {
                //
                // Retrieve the object from the cache
                //
                return this.cache.get(qualifier);
            } else try {
                //
                // Test whether lookup for this object has already failed in the same unit of work.
                //
                UnitOfWork_1 unitOfWork = getManager().currentTransaction();
                if(unitOfWork.isActive()) {
                    TransactionalState_1 state = unitOfWork.getState(this.owner, true);
                    if(state != null) {
                        Set<?> notFound = (Set<?>) state.values(true).get(this.transientContainerId.getFeature());
                        if(notFound != null && notFound.contains(qualifier)) {
                            return null;
                        }
                    }
                }
            } catch (JDOException ignore) {
                // Ignore exceptions
            }
            return getManager().getObjectById(
                openmdxjdoGetContainerId().getChild(qualifier), 
                mustValidate()
            );
        } else {
            return null; 
        }
    }

    @Override
    public boolean containsKey(Object key) {
        if(isRetrieved()) {
            return this.cache.containsKey(key);
        } else if (key instanceof String){
            // TODO replace by find
            DataObject_1_0 candidate = getManager().getObjectById(openmdxjdoGetContainerId().getChild((String)key), true);
            return candidate != null && !candidate.jdoIsDeleted();
        } else {
            return false;
        }
    }

    @Override
    public boolean containsValue(Object value) {
        if(value instanceof DataObject_1_0) {
            DataObject_1_0 object = (DataObject_1_0) value;
            return
            object.jdoGetPersistenceManager() == getManager() &&
            !object.jdoIsDeleted() &&
            object.jdoGetObjectId().size() - 1 == openmdxjdoGetContainerId().size() &&
            object.jdoGetObjectId().startsWith(openmdxjdoGetContainerId());
        } else {
            return false;
        }
    }

    @Override
    public DataObject_1_0 put(
        String key, 
        DataObject_1_0 value
    ) {
        try {
            value.objMove(this, key);
        } catch (ServiceException exception) {
            throw new JDOUserException(
                "Cannot add object to container",
                exception
            );
        }
        return null;
    }

    @Override
    public DataObject_1_0 remove(Object key) {
        if(key instanceof String) {
            String qualifier = (String) key;
            DataObject_1_0 value = get(qualifier);
            if(value == null) {
                return null;
            } else {
                if(value.jdoIsPersistent()) {
                    getManager().deletePersistent(value);
                    return value;
                } else {
                    return this.cache.remove(qualifier);
                }
            }
        } else {
            return null;
        }
    }

    private void unconditionalRetrieveAll(
        FetchPlan fetchPlan
    ){
        ConcurrentMap<String,DataObject_1_0> cache = new ConcurrentHashMap<String,DataObject_1_0>();
        for(DataObject_1_0 object : getStored(null,null, null)){
            if(!object.jdoIsDeleted()) {
                cache.put(
                    object.jdoGetObjectId().getBase(),
                    object
                );
            }                    
        }
        for(DataObject_1_0 object : getIncluded(null,null)) {
            cache.put(
                object.jdoGetObjectId().getBase(),
                object
            );
        }
        this.cache = cache;
    }
    
    public void retrieveAll(
        FetchPlan fetchPlan
    ) {
        if(!isRetrieved() && isStored()) {
            unconditionalRetrieveAll(fetchPlan);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#refreshAll()
     */
    public void refreshAll() {
        if(isStored()) {
            for(DataObject_1_0 value : values()) {
                DataObject_1 dataObject = (DataObject_1) value;
                if(dataObject.jdoIsPersistent() && !dataObject.jdoIsNew()) {
                    dataObject.unconditionalEvict();
                }
            }
            evict();
            unconditionalRetrieveAll(getManager().getFetchPlan());
        }
    }

    public Container_1_0 container() {
        return this;
    }

    public Container_1_0 subMap(Object filter) {
        ObjectFilter selector = newObjectFilter(null, filter);
        return selector == null ? this : new SubMap(this, selector);
    }

    public List<DataObject_1_0> values(Object criteria) {
        if(criteria instanceof QuerySpec) {
            return new Values(
                this.entrySet, 
                new ObjectComparator(((QuerySpec)criteria).getAttributeSpecifiers()), 
                ((QuerySpec)criteria).getFetchGroups()
            );
        } else if(criteria == null || criteria instanceof AttributeSpecifier[]) {
            return new Values(
                this.entrySet, 
                new ObjectComparator((AttributeSpecifier[])criteria), 
                StandardFetchPlan.DEFAULT_GROUPS
            );
        } else {
            throw BasicException.initHolder(
                new JDOFatalUserException(
                    "Invalid order criteria",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("expected", QuerySpec.class.getName(), AttributeSpecifier[].class.getName()),
                        new BasicException.Parameter("actual", criteria.getClass().getName())
                    )
                )
            );
        }
    }

    @Override
    public Set<Entry<String, DataObject_1_0>> entrySet() {
        if(this.entrySet == null) {
            throw BasicException.initHolder(
                new JDOFatalUserException(
                    "Operation not supported for extent",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED
                    )
                )
            );
        } else {
            return this.entrySet;
        }
    }

    public void evict(
    ){
        if(isStored()) {
            this.cache = null;
        } 
        if(!this.stored.isEmpty()) {
            for(BatchingList stored : this.stored.values()) {
                stored.evict();
            }
        }
    }

    /**
     * Retrieve the query type
     * 
     * @return the query type
     */
    private String getQueryType(){
        if(this.queryType == null) try {
            this.queryType = (String) getManager().getModel().getTypes(openmdxjdoGetContainerId())[2].objGetValue("qualifiedName");
        } catch (ServiceException exception) {
            exception.log();
        }
        return this.queryType;
    }

    /**
     * Retrieve the managed objects
     * 
     * @return the managed objects
     */
    BatchingList getStored(
        ObjectFilter selector,
        ObjectComparator comparator
    ){
        return getStored(selector, comparator, null);
    }

    /**
     * Retrieve the stored objects
     * 
     * @param selector
     * @param comparator
     * @param fetchGroups
     * 
     * @return the stored objects
     */
    BatchingList getStored(
        ObjectFilter selector,
        ObjectComparator comparator, 
        Set<String> fetchGroups
    ){
        final FilterProperty[] filter = selector == null ? null : selector.getDelegate();
        final AttributeSpecifier[] order = comparator == null ? null : comparator.getDelegate();
        String query;
        String key;
        if((filter == null || filter.length == 0) && (order == null || order.length == 0)) {
            query = null;
            key = "";
        } else try {
            key = query =  JavaBeans.toXML(new Filter(filter, order));
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
        BatchingList stored = this.stored.get(key);
        if(stored == null) {
            BatchingList concurrent = this.stored.putIfAbsent(
                key,
                stored = new BatchingList(getQueryType(), query, fetchGroups)
            );
            if(concurrent != null) {
                stored = concurrent;
            }
        }
        return stored;
    }

    @Override
    public boolean equals(Object that) {
        if(this == that) {
            return true;
        } else if(that instanceof Container_1) {
            return 
            openmdxjdoIsPersistent() && 
            this.openmdxjdoGetContainerId().equals(((Container)that).openmdxjdoGetContainerId());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        Path containerId = openmdxjdoGetContainerId();
        return (
            containerId == null ? this.transientContainerId.toPath() : containerId 
        ).toXRI();
    }

    ConcurrentMap<String, DataObject_1_0> getCache(){
        if(this.cache == null && !this.ignoreCache()) {
            BatchingList stored = this.stored.get("");
            if(stored != null && stored.isSmallerThanCacheThreshold()){
                retrieveAll(null); // TODO consider fetch plan
            }
        }
        return this.cache;
    }

    boolean isExtent(){
        return openmdxjdoIsPersistent() && openmdxjdoGetContainerId().isLike(EXTENT_REFERENCES);
    }

    public boolean isRetrieved(){
        return this.cache != null;
    }

    boolean isStored(){
        return this.owner.jdoIsPersistent() && !this.owner.jdoIsNew();
    }

    boolean ignoreCache(
    ) {
        return this.ignoreCache;
    }
    
    public void flush(
    ) throws IOException {
        // Nothing to do in this implementation
    }

    protected final DataObjectManager_1 getManager(){
        return this.owner.jdoGetPersistenceManager();
    }

    /**
     * Retrieve an include collection
     * 
     * @param selector
     * 
     * @return a new exclude collection
     */
    List<DataObject_1_0> getIncluded(
        Selector selector,
        Comparator<DataObject_1_0> comparator
    ){
        return new Included(selector, comparator);
    }


    //------------------------------------------------------------------------
    // Class Values
    //-----------------------------------------------------------------------

    /**
     * The values may be filtered and/or ordered
     */
    class Values extends AbstractSequentialList<DataObject_1_0>{

        /**
         * Constructor 
         *
         * @param entrySet
         * @param comparator
         * @param fetchGroups 
         */
        Values(
            EntrySet entrySet,
            ObjectComparator comparator, 
            Set<String> fetchGroups
        ){
            this.entrySet = entrySet;
            this.comparator = comparator;
            this.fetchGroups = fetchGroups;
        }

        private final EntrySet entrySet;
        private final ObjectComparator comparator;
        private List<DataObject_1_0> persistent;
        protected final Set<String> fetchGroups;
        
        private List<DataObject_1_0> getPersistent(){
            if(this.persistent == null) {
                final List<DataObject_1_0> dirty = new Included(this.entrySet.selector,this.comparator);
                final BatchingList stored = getStored(this.entrySet.selector, this.comparator, this.fetchGroups);
                final Excluded excluded = entrySet.getExcluded();
                this.persistent = this.comparator == null ? new ChainingList(
                    dirty,
                    stored,
                    excluded
                ) : new MergingList(
                    dirty,
                    stored,
                    excluded, 
                    this.comparator
                );
            }
            return this.persistent;
        }

        @Override
        public ListIterator<DataObject_1_0> listIterator(int index) {
            final ConcurrentMap<String, DataObject_1_0> cache = Container_1.this.getCache();
            boolean ignoreCache = 
                (cache == null) || 
                (this.entrySet.getSelector() != null && this.entrySet.getSelector().ignoreCache());
            if(!ignoreCache) {
                List<DataObject_1_0> values = new ArrayList<DataObject_1_0>();
                for(DataObject_1_0 candidate : cache.values()) {
                    if(this.entrySet.accept(candidate)) {
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
            } else if (this.entrySet.isRetrieved()) {
                List<DataObject_1_0> values = new ArrayList<DataObject_1_0>();
                for(Map.Entry<String, DataObject_1_0> entry : this.entrySet) {
                    values.add(entry.getValue());
                }
                if(this.comparator != null) {
                    Collections.sort(values, this.comparator);
                }
                return new ValueIterator(
                    values.listIterator(index),
                    true
                );    
            } else {
                return new ValueIterator(
                    getPersistent().listIterator(index),
                    false
                );
            }
        }

        @Override
        public int size() {
            final ConcurrentMap<String, DataObject_1_0> cache = Container_1.this.getCache();
            boolean ignoreCache = 
                (cache == null) || 
                (this.entrySet.getSelector() != null && this.entrySet.getSelector().ignoreCache());
            if(ignoreCache) {
                return getPersistent().size();
            } 
            else {
                int count = 0;
                for(DataObject_1_0 candidate : cache.values()) {
                    if(this.entrySet.accept(candidate)) {
                        count++;
                    }
                }
                return count;    
            }
        }

        @Override
        public boolean isEmpty() {
            final ConcurrentMap<String, DataObject_1_0> cache = Container_1.this.getCache();
            return cache == null ? getPersistent().isEmpty() : (cache.isEmpty() || super.isEmpty());
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

        /**
         * The Value Iterator handles the remove method
         */
        class ValueIterator implements ListIterator<DataObject_1_0> {


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


            public void add(DataObject_1_0 o) {
                throw new UnsupportedOperationException("Query result is unmodifiable");
            }

            public boolean hasNext() {
                return this.delegate.hasNext();
            }

            public boolean hasPrevious() {
                return this.delegate.hasPrevious();
            }

            public DataObject_1_0 next() {
                return this.current = this.delegate.next();
            }

            public int nextIndex() {
                return this.delegate.nextIndex();
            }

            public DataObject_1_0 previous() {
                return this.current = this.delegate.previous();
            }

            public int previousIndex() {
                return this.delegate.previousIndex();
            }

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

            public void set(DataObject_1_0 o) {
                throw new UnsupportedOperationException("Query result is unmodifiable");
            }

        };

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
         *
         * @param selector
         */
        EntrySet(
            ObjectFilter selector
        ){
            this.selector = selector;
        }

        final ObjectFilter selector;

        private List<DataObject_1_0> persistent;
        private Excluded excluded;
        private List<DataObject_1_0> included;
        private BatchingList stored;

        public ObjectFilter getSelector(
        ) {
            return this.selector;
        }
                
        /**
         * Tells whether the given selection has been cached
         * 
         * @return <code>true</code> if the given selection has been cached
         */
        boolean isRetrieved(){
            return this.stored != null && this.stored.isRetrieved();
        }
        
        void retrieveAll(
            FetchPlan fetchPlan
        ){
            getStored().retrieveAll(fetchPlan);
        }
        
        Excluded getExcluded(){
            if(this.excluded == null) {
                this.excluded = new Excluded(this.selector);
            }
            return this.excluded;
        }

        private List<DataObject_1_0> getIncluded(){
            if(this.included == null) {
                this.included = new Included(this.selector,null);
            }
            return this.included;
        }

        BatchingList getStored(){
            if(this.stored == null) {
                this.stored = Container_1.this.getStored(selector, null, null);
            }
            return this.stored;
        }

        List<DataObject_1_0> getPersistent(){
            if(this.persistent == null) {
                this.persistent = new ChainingList(
                    getIncluded(),
                    getStored(),
                    getExcluded()
                );
            }
            return this.persistent;
        }

        public Iterator<java.util.Map.Entry<String, DataObject_1_0>> iterator(
        ) {
            ConcurrentMap<String, DataObject_1_0> cache = Container_1.this.getCache();
            return cache == null ? new SelectionIterator(
                getPersistent().listIterator(0)
            ) : new EntryIterator(
                cache.entrySet().iterator()
            );
        }

        public int size() {
            ConcurrentMap<String, DataObject_1_0> cache = Container_1.this.getCache();
            if(cache == null) { 
                return getPersistent().size(); 
            } else if (cache.isEmpty()) {
                return 0;
            } else if (this.selector == null) {
                return cache.size();
            } else {
                int count = 0;
                for(
                    Iterator<Map.Entry<String, DataObject_1_0>> i = new EntryIterator(cache.entrySet().iterator());
                    i.hasNext();
                ){
                    count++;
                    i.next();
                }
                return count;
            }
        }

        public boolean isEmpty() {
            ConcurrentMap<String, DataObject_1_0> cache = Container_1.this.getCache();
            if(cache == null) { 
                return getPersistent().isEmpty(); 
            } else if (cache.isEmpty()) {
                return true;
            } else if (this.selector == null) {
                return false;
            } else {
                return !new EntryIterator(cache.entrySet().iterator()).hasNext();
            }
        }

        public boolean add(java.util.Map.Entry<String, DataObject_1_0> o) {
            throw new UnsupportedOperationException("Query result is unmodifiable");
        }

        public boolean addAll(
            Collection<? extends java.util.Map.Entry<String, DataObject_1_0>> c
        ) {
            throw new UnsupportedOperationException("Query result is unmodifiable");
        }

        public void clear() {
            for(
                Iterator<Map.Entry<String, DataObject_1_0>> i = iterator();
                i.hasNext();
            ){
                i.next();
                i.remove();
            }
        }

        public boolean contains(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Query result is unmodifiable");
        }

        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Query result is unmodifiable");
        }

        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        public <T> T[] toArray(T[] a) {
            throw new UnsupportedOperationException();
        }

        final boolean accept(Object candidate) {
            return this.selector == null || this.selector.accept(candidate);
        }

        /**
         * Entry Iterator
         */
        private class EntryIterator implements Iterator<Map.Entry<String, DataObject_1_0>> {

            EntryIterator(
                Iterator<Map.Entry<String, DataObject_1_0>> delegate
            ){
                this.delegate = delegate;
            }

            private final Iterator<Map.Entry<String, DataObject_1_0>> delegate;

            Map.Entry<String, DataObject_1_0> preFetched;

            Map.Entry<String, DataObject_1_0> current;

            public boolean hasNext() {
                while(this.preFetched == null && this.delegate.hasNext()) {
                    Map.Entry<String, DataObject_1_0> candidate = this.delegate.next();
                    if(accept(candidate.getValue())) {
                        this.preFetched = candidate;
                    }
                }
                return this.preFetched != null;
            }

            public java.util.Map.Entry<String, DataObject_1_0> next() {
                if(this.hasNext()) {
                    this.current = this.preFetched;
                    this.preFetched = null;
                    return this.current;
                } else {
                    throw new NoSuchElementException();
                }
            }

            public void remove() {
                if(this.current == null) {
                    throw new IllegalStateException("No current element");
                }
                try {
                    ((DataObject_1)this.current.getValue()).objRemove(false);
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                } finally {
                    this.delegate.remove();
                }
            }

        }

    }


    //------------------------------------------------------------------------
    // Class CachedIterator
    //-----------------------------------------------------------------------

    /**
     * Cached Iterator
     */
    class CachedIterator implements Iterator<Map.Entry<String, DataObject_1_0>> {

        CachedIterator(
            Iterator<Map.Entry<String, DataObject_1_0>> delegate
        ){
            this.delegate = delegate;
        }

        /**
         * The batching list iterator
         */
        private final Iterator<Map.Entry<String, DataObject_1_0>> delegate;

        /**
         * The current data object
         */
        Map.Entry<String, DataObject_1_0> current;

        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        public Entry<String, DataObject_1_0> next() {
            return this.current = this.delegate.next();
        }

        public void remove() {
            DataObject_1_0 value = this.current.getValue(); 
            if(value.jdoIsPersistent()) {
                getManager().deletePersistent(value);
            } else {
                this.delegate.remove();
            }
        }

    }


    //------------------------------------------------------------------------
    // Class SelectionIterator
    //-----------------------------------------------------------------------

    /**
     * Selection Iterator
     */
    class SelectionIterator implements Iterator<Map.Entry<String, DataObject_1_0>> {

        /**
         * Constructor 
         *
         * @param delegate
         */
        SelectionIterator(
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

        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        public Entry<String, DataObject_1_0> next() {
            this.current = this.delegate.next();
            return new Map.Entry<String, DataObject_1_0>(){

                public String getKey() {
                    return SelectionIterator.this.current.jdoGetObjectId().getBase();
                }

                public DataObject_1_0 getValue() {
                    return SelectionIterator.this.current;
                }

                public DataObject_1_0 setValue(DataObject_1_0 value) {
                    throw new UnsupportedOperationException("Query result is unmodifiable");
                }

            };
        }

        public void remove() {
            if(this.current == null) {
                throw new IllegalStateException("No current element");
            } else {
                getManager().deletePersistent(this.current);
            }
        }

    }


    //------------------------------------------------------------------------
    // Class BatchingList
    //-----------------------------------------------------------------------

    /**
     * Batching List
     */
    private class BatchingList 
        extends AbstractSequentialList<DataObject_1_0>
        implements Evictable
    {

        /**
         * 
         * Constructor 
         *
         * @param queryType
         * @param query
         * @param fetchGroups
         */
        BatchingList(
            String queryType,
            String query,
            Set<String> fetchGroups
        ) {
            this.queryType = queryType;
            this.query = query;
            this.sliceCache = null; 
            this.fetchGroups = fetchGroups;    
        }

        // ------------------------------------------------------------------
        // Members
        // ------------------------------------------------------------------
        private final String queryType;        
        private final String query;        
        private final Set<String> fetchGroups;
        private Reference<DataObjectSlice>[] sliceCache;
        private Integer total = null;
        private List<DataObject_1_0> selectionCache;

        // ------------------------------------------------------------------
        Integer getTotal(
        ) {
            return Container_1.this.ignoreCache() ?
                null :
                this.total; 
        }
        
        // ------------------------------------------------------------------
        void setTotal(
            int total
        ) {
            if(!Container_1.this.ignoreCache()) {
                this.total = total;
            }
        }
        
        // ------------------------------------------------------------------
        /**
         * TODO take fetch plan into consideration
         */
        synchronized void retrieveAll(
            FetchPlan fetchPlan
        ){
            if(!isRetrieved()) {
                List<DataObject_1_0> allCache = new ArrayList<DataObject_1_0>();
                for(
                    Iterator<DataObject_1_0> i = listIterator(0, null);
                    i.hasNext();
                ){
                    allCache.add(i.next());
                }
                this.selectionCache = allCache;
                evictSliceCache();
            }
        }
        
        // ------------------------------------------------------------------
        boolean isRetrieved(
        ){
            return this.selectionCache != null;
        }
        
        // ------------------------------------------------------------------
        synchronized public void evict(
        ){
            this.total = null;
            this.selectionCache = null;
            evictSliceCache();
        }
        
        // ------------------------------------------------------------------
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

        // ------------------------------------------------------------------
        private void evictSliceCache(){
            if(this.sliceCache != null) {
                Arrays.fill(this.sliceCache, null);
            }
        }
        
        // ------------------------------------------------------------------
        /**
         * Add a slice to the cache
         * 
         * @param slice
         */
        @SuppressWarnings("unchecked")
        synchronized void addSlice(
            DataObjectSlice slice
        ){
            Reference reference = new SoftReference<DataObjectSlice>(slice);
            if(this.sliceCache == null) {
                this.sliceCache = new Reference[INITIAL_SLICE_CACHE_SIZE];
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

        // ------------------------------------------------------------------
        @Override
        public ListIterator<DataObject_1_0> listIterator(
            int index
        ) {
            return isRetrieved() ? 
                this.selectionCache.listIterator(index) : 
                    listIterator(index, null);
        }

        // ------------------------------------------------------------------
        ListIterator<DataObject_1_0> listIterator(
            int index,
            FetchPlan fetchPlan
        ){
           return new BatchingIterator(index, fetchPlan);  
        }
        
        // ------------------------------------------------------------------
        @Override
        public int size(
        ) {
            if(this.getTotal() == null) {
                ListIterator<?> i = listIterator(0);
                int total = 0; 
                while(i.hasNext()){
                    if(this.getTotal() != null) {
                        return this.getTotal().intValue();
                    }
                    total++;
                    i.next();
                }
                return total;
            }
            else {
                return this.getTotal();
            }
        }
        
        // ------------------------------------------------------------------
        boolean isSmallerThanCacheThreshold(
        ){
            return 
                (this.getTotal() != null) && 
                (this.getTotal().intValue() <  getManager().getCacheThreshold());
        }

        // ------------------------------------------------------------------
        @Override
        public boolean isEmpty(
        ) {
            return this.getTotal() != null ?
                this.getTotal().intValue() == 0 :
                    !this.listIterator(0).hasNext();
        }

        // ------------------------------------------------------------------
        protected Query_2Facade newQuery(
        ) throws ResourceException{
            Query_2Facade query = Query_2Facade.newInstance();
            query.setPath(openmdxjdoGetContainerId());
            query.setQueryType(this.queryType);
            query.setQuery(this.query);
            query.setGroups(this.fetchGroups);
            return query;
        }
        
        // ------------------------------------------------------------------
        /**
         * Iterator
         */
        class BatchingIterator implements ListIterator<DataObject_1_0> {

            /**
             * Constructor 
             * 
             * @param fetchPlan
             * @param index
             */
            BatchingIterator(
                int index,
                FetchPlan fetchPlan 
            ) {
                this.nextIndex = index;
                this.previousIndex = index - 1;
                if(fetchPlan == null) {
                    this.batchSize = getManager().getOptimalFetchSize();
                } else {
                    int fetchSize = fetchPlan.getFetchSize();
                    if(fetchSize == FetchPlan.FETCH_SIZE_OPTIMAL) {
                        this.batchSize = getManager().getOptimalFetchSize();
                    } else if (fetchSize == FetchPlan.FETCH_SIZE_GREEDY) {
                        this.batchSize = BATCH_SIZE_GREEDY;
                    } else {
                        this.batchSize = fetchSize;
                    }
                 }
            }

            private final int batchSize;  
            private int previousIndex;
            private int nextIndex;
            private int highWaterMark = 0;
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

            public void add(
                DataObject_1_0 o
            ) {
                throw new UnsupportedOperationException("A list proxy is unmodifiable");
            }

            private boolean load(
                boolean ascending, 
                int index
            ) throws ResourceException {
                if(
                    (BatchingList.this.getTotal() == null) || 
                    (index < BatchingList.this.getTotal())
                ) {
                    this.slice = getSlice(index);
                    if(this.slice == null) {
                        Query_2Facade facade = newQuery();
                        facade.setPosition(ascending ? index : -(index+1));
                        facade.setSize(this.batchSize);
                        IndexedRecord cache = (IndexedRecord) getManager().getInteraction().execute(
                            getManager().getInteractionSpecs().GET,
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
                                } 
                                else if (
                                    Boolean.FALSE.equals(hasMore) &&
                                    (index == 0 || !cache.isEmpty()) 
                                ) {
                                    resultTotal = Long.valueOf(index + cache.size());
                                }
                            }
                        } 
                        else if(index == 0 && cache.isEmpty()) {
                            resultTotal = Long.valueOf(0);
                        }
                        if(resultTotal != null) {
                            BatchingList.this.setTotal(resultTotal.intValue());
                            this.highWaterMark = resultTotal.intValue();
                        } 
                        else if(
                            BatchingList.this.getTotal() == null &&
                            highWaterMark != null &&
                            this.highWaterMark < highWaterMark.intValue()
                        ) {
                            this.highWaterMark = highWaterMark.intValue();
                        }
                        for(Object o : cache) {
                            this.slice.add(getManager().receive((MappedRecord) o));
                        }
                        boolean loaded = !this.slice.isEmpty(); 
                        if(loaded && !Container_1.this.ignoreCache()) {
                            addSlice(this.slice);
                        }
                        return loaded;
                    } 
                    else {
                        return true;
                    }
                } 
                else {
                    return false;
                }                
            }

            public boolean hasNext(
            ) {
                try {
                    return BatchingList.this.getTotal() != null ?
                        this.nextIndex < BatchingList.this.getTotal() :                     
                        (this.nextIndex < this.highWaterMark) || this.load(true, this.nextIndex);
                } catch (ResourceException exception) {
                    throw new RuntimeServiceException(exception);
                }
            }

            public boolean hasPrevious(
            ) {
                return this.previousIndex >= 0;
            }

            public DataObject_1_0 next(
            ) {
                if(hasNext()) {
                    return this.get(
                        this.previousIndex = this.nextIndex++, 
                        true
                    );
                } 
                else throw new NoSuchElementException(
                    "nextIndex() >= size(): " + this.nextIndex
                );
            }

            public int nextIndex(
            ) {
                return this.nextIndex;
            }

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

            public int previousIndex(
            ) {
                return this.previousIndex;
            }

            public void remove(
            ) {
                throw new UnsupportedOperationException("A list proxy is unmodifiable");
            }

            public void set(
                DataObject_1_0 o
            ) {
                throw new UnsupportedOperationException("A list proxy is unmodifiable");
            }

        }    

        // ------------------------------------------------------------------
    }

    //------------------------------------------------------------------------
    // Class DataObjectSlice
    //------------------------------------------------------------------------

    /**
     * The slices are used as keys in the cache. That's why they should not
     * follow the List's standard contract for equality and hashCode!
     */
    @SuppressWarnings("serial")
    static class DataObjectSlice extends ArrayList<DataObject_1_0> {

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
    // Class FilteringList
    //------------------------------------------------------------------------

    /**
     * Filtering List
     */
    abstract class FilteringList extends AbstractSequentialList<DataObject_1_0> {

        /**
         * Retrieve list.
         *
         * @return Returns the list.
         */
        protected abstract List<? extends DataObject_1_0> getSource();

        public ListIterator<DataObject_1_0> listIterator(int index) {
            return new FilteringListIterator(
                getSource(),
                index
            );
        }

        public int size() {
            int size = 0;
            for(Object candidate : getSource()) {
                if(accept(candidate)) {
                    size++;
                }
            }
            return size;
        }

        @Override
        public boolean isEmpty() {
            Collection<?> source = getSource();
            if(!source.isEmpty()) {
                for(Object candidate : source) {
                    if(accept(candidate)) {
                        return false;
                    }
                }
            }
            return true;
        }

        public boolean contains(Object object) {
            return accept(object) && getSource().contains(object);
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
            if(acceptAll()) {
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
                if(acceptAll()){
                    this.iterator = source.listIterator(index);
                    this.nextIndex = this.iterator.nextIndex();
                    this.previousIndex = this.iterator.previousIndex();
                } else {
                    this.iterator = source.listIterator();
                    this.nextIndex = 0;
                    this.previousIndex = -1;
                    while (index > this.nextIndex) {
                        next();
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

            public boolean hasNext() {
                while(!this.readAheadReady && this.iterator.hasNext()) {
                    this.readAheadElement = this.iterator.next(); 
                    this.readAheadCount++;
                    this.readAheadReady = accept(this.readAheadElement);
                }
                return this.readAheadReady;
            }

            public DataObject_1_0 next() {
                if(!hasNext()) {
                    throw new NoSuchElementException(
                        "End of list reached"
                    );
                }
                this.previousIndex = this.nextIndex++;
                return this.current = readAheadFlush();
            }

            public boolean hasPrevious() {
                return this.previousIndex >= 0;
            }

            public DataObject_1_0 previous(
            ){
                if(!hasPrevious()) throw new NoSuchElementException(
                    "Begin of list reached"
                );
                reposition();
                DataObject_1_0 candidate = this.iterator.previous();
                while (!FilteringList.this.accept(candidate)) {
                    candidate = this.iterator.previous();
                }
                this.nextIndex = this.previousIndex--;
                this.readAheadCount = 0;
                return this.current = candidate;
            }

            public int nextIndex(
            ) {
                return this.nextIndex;
            }

            public int previousIndex() {
                return this.previousIndex;
            }

            public void remove() {
                if(this.current == null) {
                    throw new IllegalStateException(
                        "Iterator has no current element"
                    );
                }
                reposition();
                if(this.current.jdoIsPersistent()) {
                    getManager().deletePersistent(this.current);
                } else {
                    this.iterator.remove();
                }
            }

            public void set(DataObject_1_0 object) {
                throw new UnsupportedOperationException("Query result is unmodifiable");
            }

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
                while (readAheadCount-- > 0) this.iterator.previous();
                readAheadFlush();
            }

        }

    }


    //------------------------------------------------------------------------
    // Class OrderingList
    //------------------------------------------------------------------------

    /**
     * Ordering List
     */
    private abstract class OrderingList extends FilteringList {

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

            private final ListIterator<DataObject_1_0> delegate;
            private DataObject_1_0 current = null;

            public void add(DataObject_1_0 e) {
                throw new UnsupportedOperationException("Query results are unmodifiable");
            }

            public boolean hasNext() {
                return this.delegate.hasNext();
            }

            public boolean hasPrevious() {
                return this.delegate.hasPrevious();
            }

            public DataObject_1_0 next() {
                return this.current = this.delegate.next();
            }

            public int nextIndex() {
                return this.delegate.nextIndex();
            }

            public DataObject_1_0 previous() {
                return this.current = this.delegate.previous();
            }

            public int previousIndex() {
                return this.delegate.previousIndex();
            }

            public void remove() {
                this.delegate.remove();
                JDOHelper.getPersistenceManager(this.current).deletePersistent(this.current);
                this.current = null;
            }

            public void set(DataObject_1_0 e) {
                throw new UnsupportedOperationException("Query results are unmodifiable");
            }

        }

    }


    //------------------------------------------------------------------------
    // Class Members
    //------------------------------------------------------------------------

    /**
     * Members
     */
    class Members extends OrderingList {

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

        @Override
        final protected List<? extends DataObject_1_0> getSource(
        ){
            return getManager().currentTransaction().getMembers();
        }

        @Override
        final protected boolean accept(Object candidate) {
            return candidate instanceof DataObject_1 && accept((DataObject_1_0)candidate);
        }

        /**
         * Test whether a given candidate is member of the collection
         * 
         * @param candidate
         * 
         * @return <code>true</code> if the candidate is member of the collection
         */
        protected boolean accept(
            DataObject_1_0 candidate
        ){
            Path objectId = candidate.jdoGetObjectId();
            if(objectId == null) {
                return false;
            } else {
                Path containerId = openmdxjdoGetContainerId();
                return 
                    objectId.size() == containerId.size() + 1 &&
                    objectId.startsWith(containerId);
            }
        }

    }


    //------------------------------------------------------------------------
    // Class Excluded
    //------------------------------------------------------------------------

    /**
     * Excluded Members
     */
    private final class Excluded extends Members {

        /**
         * Constructor 
         *
         * @param selector
         */
        Excluded(
            Selector selector
        ){
            super(null);
            this.plain = selector == null;
        }

        /**
         * Tells whether there is an attribute filter or not
         */
        private final boolean plain;

        boolean isPlain(){
            return this.plain;
        }
        
        boolean handles(
            DataObject_1_0 candidate
        ) {
            return this.plain ? candidate.jdoIsDeleted() : candidate.jdoIsDirty();
        }
        
        @Override
        protected boolean accept(
            DataObject_1_0 candidate
        ) {
            return !candidate.jdoIsNew() && handles(candidate) && super.accept(candidate);
        }

    }


    //------------------------------------------------------------------------
    // Class Included
    //------------------------------------------------------------------------

    /**
     * Included Members
     */
    private final class Included extends Members {

        /**
         * Constructor 
         *
         * @param selector
         * @param comparator
         */
        Included(
            Selector selector,
            Comparator<DataObject_1_0> comparator
        ){
            super(comparator);
            this.selector = selector;
        }

        private final Selector selector;

        @Override
        protected boolean accept(
            DataObject_1_0 candidate
        ) {
            return !candidate.jdoIsDeleted() && super.accept(candidate) &&  (
                this.selector == null ? candidate.jdoIsNew() : (candidate.jdoIsDirty() && this.selector.accept(candidate))  
            );
        }

    }


    //------------------------------------------------------------------------
    // Class MergingList
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
    private abstract class JoiningList 
        extends AbstractSequentialList<DataObject_1_0> 
    {

        /**
         * Constructor 
         *
         * @param dirty
         * @param stored
         * @param excluded
         * @param comparator
         */
        protected JoiningList(
            List<DataObject_1_0> dirty,
            BatchingList stored,
            Excluded excluded,
            ObjectComparator comparator
        ){
            this.comparator = comparator;
            this.dirty = dirty;
            this.stored = stored;
            this.excluded = excluded;
        }

        protected final ObjectComparator comparator;
        protected final List<DataObject_1_0> dirty;
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
        ) {
            int storedSize = this.stored.size();
            if(storedSize != Integer.MAX_VALUE) {
                if(storedSize == 0 && this.dirty.isEmpty()) {
                    return 0;
                }
                if(this.excluded.isPlain()) {
                    return storedSize + this.dirty.size() - this.excluded.size();
                }
            }
            int total = this.dirty.size();
            for(DataObject_1_0 candidate : this.stored) {
                if(!this.excluded.handles(candidate)) {
                    total++;
                }
            }
            return total;
        }

        public boolean isEmpty() {
            if(this.dirty.isEmpty()) {
                if(this.stored.isEmpty()) {
                    return true;
                } else if (this.excluded.isPlain()) {
                    return this.stored.size() == this.excluded.size();
                } else {
                    for(DataObject_1_0 candidate : this.stored) {
                        if(!this.excluded.handles(candidate)) {
                            return false;
                        }
                    }
                    return true;
                }
            } else {
                return false;
            }
        }

        public String toString(
        ){
            Path containerId = openmdxjdoGetContainerId();
            return getClass().getName() + (
                containerId == null ? "@" + System.identityHashCode(this) :  ": " + containerId.toXRI()
            );
        }

    }    

    //------------------------------------------------------------------------
    // Class ChainingList
    //------------------------------------------------------------------------

    /**
     * Chaining List
     */
    private final class ChainingList extends JoiningList {

        /**
         * Constructor 
         *
         * @param dirty
         * @param stored
         * @param excluded
         */
        ChainingList(
            List<DataObject_1_0> dirty,
            BatchingList stored,
            Excluded excluded
        ){
            super(dirty, stored, excluded, null); 
        }

        public ListIterator<DataObject_1_0> listIterator(
            int index
        ) {
            return new ChainingIterator(index);
        }

        /**
         * Chaining Iterator
         */
        private class ChainingIterator implements ListIterator<DataObject_1_0>{

            /**
             * Constructor 
             *
             * @param index
             */
            ChainingIterator (
                int index
            ){
                this.nextIndex = index;
                this.previousIndex = index - 1;
                this.dirtySize = dirty.size();
            }

            private int previousIndex;
            private int nextIndex;
            private int dirtySize;

            private ListIterator<DataObject_1_0> dirtyIterator = null;
            private ListIterator<DataObject_1_0> cleanIterator = null;
            private ListIterator<DataObject_1_0> currentIterator = null;

            private ListIterator<DataObject_1_0> getPreviousIterator(){
                if(this.previousIndex < dirty.size()) {
                    if(this.dirtyIterator == null) {
                        this.dirtyIterator = dirty.listIterator(this.nextIndex);  
                    }
                    return this.dirtyIterator;
                } else {
                    if(this.cleanIterator == null){
                        this.cleanIterator = excluded.isEmpty() ? stored.listIterator(
                            this.nextIndex - dirtySize
                        ) : new CleanIterator(
                            excluded, 
                            stored.listIterator(0), 
                            this.nextIndex - dirtySize
                        );
                    }
                    return this.cleanIterator;
                }
            }

            private ListIterator<DataObject_1_0> getNextIterator(){
                if(this.nextIndex < dirtySize) {
                    return this.dirtyIterator == null ? this.dirtyIterator = dirty.listIterator(this.nextIndex) : this.dirtyIterator;
                } else {
                    return this.cleanIterator == null ? this.cleanIterator = stored.listIterator(this.nextIndex - dirtySize) : this.cleanIterator;
                }
            }

            public boolean hasNext() {
                return getNextIterator().hasNext();
            }

            public DataObject_1_0 next(
            ) {
                DataObject_1_0 current = (this.currentIterator = getNextIterator()).next();
                this.previousIndex = this.nextIndex++;
                return current;
            }

            public boolean hasPrevious() {
                return getPreviousIterator().hasPrevious();
            }

            public DataObject_1_0 previous() {
                DataObject_1_0 current = (this.currentIterator = getPreviousIterator()).previous();
                this.nextIndex = this.previousIndex--;
                return current;
            }

            public int nextIndex() {
                return this.nextIndex;
            }

            public int previousIndex() {
                return this.previousIndex;
            }

            public void remove() {
                this.currentIterator.remove();
                if(this.currentIterator == this.dirtyIterator) {
                    this.dirtySize--;
                }
            }

            public void set(DataObject_1_0 object) {
                throw new UnsupportedOperationException();
            }

            public void add(DataObject_1_0 object) {
                throw new UnsupportedOperationException();
            }

        }

    }    


    //------------------------------------------------------------------------
    // Class MergingList
    //------------------------------------------------------------------------

    /**
     * Merging List
     */
    private final class MergingList extends JoiningList {

        /**
         * Constructor 
         *
         * @param dirty
         * @param stored
         * @param excluded 
         * @param comparator
         */
        MergingList(
            List<DataObject_1_0> dirty,
            BatchingList stored,
            Excluded excluded,
            ObjectComparator comparator
        ){
            super(dirty,stored,excluded,comparator);
        }

        public ListIterator<DataObject_1_0> listIterator(
            int index
        ) {
            return new MergingIterator(index);
        }

        /**
         * Merging Iterator
         */
        private final class MergingIterator implements ListIterator<DataObject_1_0> {

            /**
             * Constructor 
             *
             * @param index
             */
            MergingIterator (
                int index
            ) {
                this.nextIndex = index;
                this.previousIndex = index - 1;
                this.dirtyIterator = MergingList.this.dirty.listIterator();
                // Iterate up to the requested index if there are dirty or excluded elements
                if(
                    this.dirtyIterator.hasNext() || 
                    !MergingList.this.getExcluded().isEmpty()
                ) {
                    this.cleanIterator = MergingList.this.getExcluded().isEmpty() ? 
                        MergingList.this.getStored().listIterator(0) : 
                            new CleanIterator(
                                MergingList.this.getExcluded(),
                                MergingList.this.getStored().listIterator(0),
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
                    this.cleanIterator = MergingList.this.getStored().listIterator(index); 
                }
            }

            public boolean hasNext(
            ) {
                return 
                    this.nextDirty != null ||
                    this.nextClean != null ||
                    this.dirtyIterator.hasNext() ||
                    this.cleanIterator.hasNext();
            }

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
                    comparator.compare(this.nextDirty, this.nextClean) <= 0;
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

            public boolean hasPrevious() {
                return this.previousIndex > 0;
            }

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
                    this.previousDirty == null ? 
                        false :
                            this.previousClean == null ? 
                                true :
                                    comparator.compare(this.previousDirty, this.previousClean) > 0;
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

            public int nextIndex(
            ) {
                return this.nextIndex;
            }

            public int previousIndex(
            ) {
                return this.previousIndex;
            }

            public void remove(
            ) {
                (this.useDirty ? this.dirtyIterator : this.cleanIterator).remove();
            }

            public void set(
                DataObject_1_0 object
            ) {
                throw new UnsupportedOperationException();
            }

            public void add(
                DataObject_1_0 object
            ) {
                throw new UnsupportedOperationException();
            }

            //---------------------------------------------------------------
            // Members
            //---------------------------------------------------------------            
            private int previousIndex;
            private int nextIndex;

            private final ListIterator<DataObject_1_0> dirtyIterator;
            private final ListIterator<DataObject_1_0> cleanIterator;
            
            private DataObject_1_0 nextDirty = null;
            private DataObject_1_0 nextClean = null;
            private DataObject_1_0 previousDirty = null;
            private DataObject_1_0 previousClean = null;
            private boolean useDirty;
            
        }

    }    


    //------------------------------------------------------------------------
    // Class SubMap
    //------------------------------------------------------------------------

    /**
     * Sub-Map
     */
    private final class SubMap
        extends AbstractMap<String, DataObject_1_0>
        implements Container_1_0 
    {

        /**   
         * Standard Constructor 
         *
         * @param superMap
         * @param selector
         */
        SubMap(
            Container_1_0 superMap,
            ObjectFilter selector
        ){
            this.superMap = superMap;
            this.entrySet = new EntrySet(selector);
        }

        private final Container_1_0 superMap;
        private final EntrySet entrySet;

        public Container_1_0 subMap(Object filter) {
            ObjectFilter selector = newObjectFilter(
                this.entrySet.selector,
                filter
            );
            return selector == null ? this : new SubMap(this, selector);
        }

        public boolean containsKey(Object key) {
            return get(key) != null;
        }

        @Override
        public DataObject_1_0 get(Object key) {
            DataObject_1_0 candidate = this.superMap.get(key);
            return candidate != null && this.entrySet.accept(candidate) ? candidate : null; 
        }

        public boolean containsValue(Object value) {
            return 
                this.superMap.containsValue(value) &&
                this.entrySet.accept(value);
        }

        public void retrieveAll(
            FetchPlan fetchPlan
        ) {
            if(!this.superMap.isRetrieved()) {
                this.entrySet.retrieveAll(fetchPlan);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.cci.Container_1_0#refreshAll()
         */
        public void refreshAll() {
            ((Container_1)container()).evict();
            for(DataObject_1_0 value : values()) {
                getManager().refresh(value);
            }
        }

        public Container_1_0 container() {
            return this.superMap.container();
        }

        @Override
        public Set<java.util.Map.Entry<String, DataObject_1_0>> entrySet() {
            return this.entrySet;
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return entrySet().isEmpty();
        }

        @Override
        public DataObject_1_0 put(String key, DataObject_1_0 value) {
            if(this.entrySet.accept(value)) {
                return this.superMap.put(key, value);
            } else {
                throw new IllegalArgumentException("The given value is not a member of this sub-map");
            }
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " of " + this.superMap;
        }

        public List<DataObject_1_0> values(Object criteria) {
            return criteria instanceof QuerySpec ? new Values(
                this.entrySet,
                new ObjectComparator(((QuerySpec)criteria).getAttributeSpecifiers()), 
                ((QuerySpec)criteria).getFetchGroups()
            ) : new Values(
                this.entrySet,
                new ObjectComparator((AttributeSpecifier[])criteria), 
                StandardFetchPlan.DEFAULT_GROUPS
            );
        }

        public boolean isRetrieved() {
            return superMap.isRetrieved() || this.entrySet.isRetrieved();
        }

    }

    /**
     * Object filter factory
     * @param superFilter
     * @param subFilter
     * 
     * @return a new object filter
     */
    protected ObjectFilter newObjectFilter (
        ObjectFilter superFilter,
        Object subFilter
    ){
        if(subFilter instanceof FilterProperty[] ){
            FilterProperty[] properties = (FilterProperty[]) subFilter;
            //
            // TODO remove entries covered by superFilter from the filter properties
            //
            return properties.length == 0 ? null :  new ObjectFilter(
                superFilter,
                properties
            );
        } else if(subFilter == null) {
            return null;
        } else {
            throw BasicException.initHolder(
                new JDOFatalUserException(
                    "Invalid filter class",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("expected", FilterProperty[].class.getName()),
                        new BasicException.Parameter("actual", subFilter.getClass().getName())
                    )
                )
            );
        }
    }

    /**
     * Object Filter
     */
    private final class ObjectFilter extends ModelAwareFilter {

        /**
         * Constructor for a sub-class aware filter
         * @param filter
         * 
         * @exception   IllegalArgumentException
         *              in case of an invalid filter property set
         * @exception   NullPointerException
         *              if the filter is <code>null</code>
         */
        ObjectFilter(
            ObjectFilter superFilter,
            FilterProperty[] filter
        ){
            super(filter);
            this.superFilter = superFilter;
            boolean ignoreCache = false;
            boolean evaluateSuperFilterFirst = true;
            if(filter != null) {
                for(int i = 0; i < filter.length; i++) {
                    String name = filter[i].name(); 
                    if(name.startsWith(SystemAttributes.CONTEXT_PREFIX)) {
                        ignoreCache = true;
                        break;
                    } else if (
                        SystemAttributes.OBJECT_CLASS.equals(name) ||
                        SystemAttributes.OBJECT_INSTANCE_OF.equals(name)
                    ) {
                        evaluateSuperFilterFirst = false;
                    }
                }
            }
            this.ignoreCache = ignoreCache;
            this.evaluateSuperFilterFirst = evaluateSuperFilterFirst;
        }

        private static final long serialVersionUID = 875014812028655977L;
        private final ObjectFilter superFilter;
        private transient FilterProperty[] delegate;
        private final boolean ignoreCache;
        private final boolean evaluateSuperFilterFirst;
        
        private List<FilterProperty> buildDelegate(
        ){
            List<FilterProperty> delegate = 
                this.superFilter == null ? new ArrayList<FilterProperty>() :
                this.superFilter.buildDelegate();
            for(FilterProperty property : super.getDelegate()) {
                delegate.add(property);
            }
            return delegate;
        }

        public boolean ignoreCache(
        ) {
            return this.ignoreCache;
        }
        
        /**
         * Tells whether a filer property has to be updated because it contains 
         * a transient object id
         * 
         * @param property the filter property to be tested
         * 
         * @return <code>true</code> if the property contains a transient object id
         */
        private boolean containsTransientObjectId(
            FilterProperty property
        ){
            for (Object value : property.values()) {
                if(value instanceof UUID) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Replace transient object id's by appropriate paths
         * 
         * @param source the filter property
         * 
         * @return a property without transient object ids
         */
        private FilterProperty replaceTransientObjectIds(
            FilterProperty property
        ){
            List<?> source = property.values();
            Object[] target = new Object[source.size()];
            int i = 0;
            for(Object value : source) {
                if(value instanceof UUID) {
                    Object object = getManager().getObjectById(value);
                    target[i++] = JDOHelper.isPersistent(object) ? JDOHelper.getObjectId(object) : new Path((UUID)value);
                } else {
                    target[i++] = value;
                }
            }
            return new FilterProperty(
                property.quantor(),
                property.name(),
                property.operator(),
                target
            );
        }

        @Override
        public FilterProperty[] getDelegate(
        ) {
            if(this.delegate == null) {
                List<FilterProperty> delegate = buildDelegate();
                this.delegate = new FilterProperty[delegate.size()];
                int i = 0;
                for(FilterProperty property : delegate) {
                    this.delegate[i++] = containsTransientObjectId(property) ? replaceTransientObjectIds(property) : property;
                }
            }
            return this.delegate;
        }

        @Override
        protected Iterator<?> getValuesIterator(
            Object candidate, 
            String attribute
        ) throws ServiceException {
            DataObject_1 object = (DataObject_1)candidate;
            return 
                SystemAttributes.OBJECT_CLASS.equals(attribute) ? Collections.singleton(object.objGetClass()).iterator() :
                SystemAttributes.OBJECT_INSTANCE_OF.equals(attribute) ? newInstanceOfIterator(object.objGetClass()) :
                object.objGetIterable(attribute).iterator();
        }

        @Override
        public boolean accept(Object candidate) {
            if(this.superFilter == null) {
                return !JDOHelper.isDeleted(candidate) && super.accept(candidate); 
            } else if (this.evaluateSuperFilterFirst) {
                return this.superFilter.accept(candidate) && super.accept(candidate);
            } else {
                return super.accept(candidate) && this.superFilter.accept(candidate);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.query.AbstractFilter#equal(java.lang.Object, java.lang.Object)
         */
        @Override
        protected boolean equal(
            Object candidate, 
            Object value
        ) {
            if(candidate == value) {
                return true;
            } else if(candidate instanceof DataObject_1_0) {
                if(value instanceof UUID) {
                    return value.equals(JDOHelper.getTransactionalObjectId(candidate));
                } else if(value instanceof Path) {
                    Path oid = (Path)value;
                    if(oid.isTransientObjectId()) {
                        return oid.toUUID().equals(JDOHelper.getTransactionalObjectId(candidate));
                    } else {
                        return oid.equals(JDOHelper.getObjectId(candidate));
                    }
                } else {
                    return false;
                }
            } else {
                return super.equal(candidate, value);
            }
        }
        
    }


    //------------------------------------------------------------------------
    // Class ObjectComparator
    //------------------------------------------------------------------------

    /**
     * Object Comparator
     */
    private static final class ObjectComparator implements Comparator<DataObject_1_0> {

        /**
         * For de-serialization
         */
        @SuppressWarnings("unused")
        protected ObjectComparator(
        ){
            super();
        }

        /**
         * Constructor 
         *
         * @param order
         */
        public ObjectComparator(
            AttributeSpecifier[] order
        ){
            this.order = order;
        }

        private AttributeSpecifier[] order;

        private Object getValue(
            DataObject_1_0 object,
            AttributeSpecifier specifier
        ){
            try {
                Object value = SystemAttributes.OBJECT_CLASS.equals(specifier.name()) ?
                    object.objGetClass() :
                        object.objGetValue(specifier.name());
                    int position = specifier.position();
                    //... Sort on multivalued attributes is deprecated!
                    if(value instanceof List<?>){
                        List<?> collection = (List<?>)value;
                        return collection.size() > position ? collection.get(position) : null;
                    } else if (value instanceof SparseArray<?>) {
                        SparseArray<?> collection = (SparseArray<?>)value;
                        return collection.get(position);
                    } else if (value instanceof SortedMap<?,?>) {
                        SortedMap<?,?> collection = (SortedMap<?,?>)value;
                        return collection.get(Integer.valueOf(position));
                    } else if (value instanceof Collection<?>) {
                        Collection<?> collection = (Collection<?>)value;
                        if(position >= collection.size()) return null;
                        Iterator<?> i = collection.iterator();
                        while (
                            position-- > 0
                        ) i.next();
                        return i.next();
                    } else {
                        return value;
                    }
            } catch (ServiceException e) {
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        private static int compareValues(Object left, Object right) {
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
        public int compare(DataObject_1_0 ox, DataObject_1_0 oy) {
            if(this.order == null){
                if(ox.jdoIsPersistent()) {
                    if(oy.jdoIsPersistent()) {
                        Path ix = (Path) JDOHelper.getObjectId(ox); 
                        Path iy = (Path) JDOHelper.getObjectId(oy); 
                        return ix.compareTo(iy);
                    } else {
                        return +1;
                    }
                } else {
                    if(oy.jdoIsPersistent()) {
                        return -1;
                    } else {
                        UUID ix = (UUID) JDOHelper.getTransactionalObjectId(ox);
                        UUID iy = (UUID) JDOHelper.getTransactionalObjectId(oy);
                        return ix.compareTo(iy);
                    }
                }
            } else {
                for(
                    int i = 0;
                    i < this.order.length;
                    i++
                ){
                    AttributeSpecifier s = this.order[i];
                    if(s.order() == Orders.ANY) continue;
                    Object vx = getValue(ox,s);
                    Object vy = getValue(oy,s);
                    int c = compareValues(vx,vy);
                    if (c != 0) {
                        return s.order() == Directions.ASCENDING ? c : -c;
                    }
                }
                return 0;
            }
        }

        @Override
        public boolean equals(Object that) {
            return 
                that != null && 
                that.getClass() == this.getClass() &&
                Arrays.equals(this.order, ((ObjectComparator)that).order);
        }

        @Override
        public int hashCode() {
            return Arrays.asList(this.order).hashCode();
        }

        public AttributeSpecifier[] getDelegate(
        ){
            return this.order;
        }

    }

    
    //------------------------------------------------------------------------
    // Class
    //------------------------------------------------------------------------

    /**
     * Clean Iterator
     */
    class CleanIterator implements ListIterator<DataObject_1_0> {
        
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
                next();
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
        public void add(DataObject_1_0 o) {
            throw new UnsupportedOperationException();
        }
        
        /**
         * @return
         * @see java.util.ListIterator#hasNext()
         */
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
        public boolean hasPrevious() {
            return this.previousIndex >= 0;
        }
        
        /**
         * @return
         * @see java.util.ListIterator#next()
         */
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
        public int nextIndex() {
            return this.nextIndex;
        }
        
        /**
         * @return
         * @see java.util.ListIterator#previous()
         */
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
        public int previousIndex() {
            return this.previousIndex;
        }
        
        /**
         * 
         * @see java.util.ListIterator#remove()
         */
        public void remove() {
            this.delegate.remove();
        }
        
        /**
         * @param o
         * @see java.util.ListIterator#set(java.lang.Object)
         */
        public void set(DataObject_1_0 o) {
            throw new UnsupportedOperationException();
        }
                
    }
    
}
