/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PersistentContainer_1.java,v 1.13 2009/06/09 16:21:23 hburger Exp $
 * Description: A persistent container implementation
 * Revision:    $Revision: 1.13 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 16:21:23 $
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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.AbstractSequentialList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.WeakHashMap;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.collection.MarshallingSequentialList;
import org.openmdx.base.collection.Reconstructable;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.AbstractFilter;
import org.openmdx.base.query.AttributeSpecifier;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Selector;
import org.openmdx.base.resource.cci.ResultRecord;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.base.text.conversion.JavaBeans;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;

/**
 * A persistent container implementation
 */
@SuppressWarnings("unchecked")
class PersistentContainer_1
    extends AbstractContainer<DataObject_1_0>
    implements Evictable 
{

    /**
     * Constructor
     * 
     * @param referenceFilter
     * @param attributeFilter
     * @param manager
     * @param provider
     * @param provided
     * @param sequenceProvider
     * @param reference 
     */
    private PersistentContainer_1(
        Path referenceFilter,
        Selector attributeFilter,
        DataObjectManager_1 manager,
        EvictablePersistentObjects provided,
        DataObject_1 sequenceProvider, 
        Set<String> keys
    ){
        super(manager.getModel(), attributeFilter);
        if(manager == null) throw new IllegalArgumentException(
            "A persistent container's manager must not be null"
        );      
        this.referenceFilter = referenceFilter;
        this.manager = manager;
        this.sequenceProvider = sequenceProvider;
        this.keys = keys == null ? new KeySet(this) : keys;
        this.provided = provided == null ? new EvictablePersistentObjects() : provided;
    }

    /**
     * Constructor 
     * 
     * @param referenceFilter
     * @param manager
     * @param provider
     * @param sequenceProvider
     * @param providerManagedObjects
     */
    PersistentContainer_1(
        Path referenceFilter,
        DataObjectManager_1 manager,
        DataObject_1 sequenceProvider
    ){
        this(
            referenceFilter, 
            null, // attributeFilter
            manager, 
            null, // new EvictablePersistentObjects(featureName),
            sequenceProvider, 
            null // keys
        );
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3978147629899526454L;

    /**
     * The attribute filter free container
     */
    private final Set<String> keys;

    /**
     * @serial
     */
    protected Path referenceFilter; 

    /**
     * @serial
     */
    protected DataObjectManager_1 manager;

    /**
     * Reconstructable collection
     */
    protected transient List collection; 

    /**
     * Reconstructable collection
     */
    protected transient List include; 

    /**
     * Reconstructable collection
     */
    protected transient Exclude exclude; 
    
    /**
     * @serial
     */
    protected final EvictablePersistentObjects provided;

    /**
     * The collection's parent
     */
    protected final DataObject_1 sequenceProvider;

    /**
     * 
     */
    protected static final int LOCAL_EVALUATION_LIMIT = 1000;

    /**
     * The key set consists of the object ids' base values. 
     * 
     * @return the key set
     */
    Set<String> keySet(){
        return this.keys;
    }

    /**
     *
     */
    Path getReferenceFilter(
    ){
        return this.referenceFilter;
    }

    /**
     *
     */
    DataObjectManager_1 getManager(
    ){
        return this.manager;
    }
    
    //--------------------------------------------------------------------------
    // Extends AbstractContainer
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.Container#retrieveAll(boolean)
     */
    public void retrieveAll(
        boolean useFetchPlan
    ) {
        this.provided.retrieveAll(useFetchPlan); // TODO support retrieveAll on an arbitrary super-set
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.delegation.AbstractContainer#initialQualifier()
     */
    protected long initialQualifier(
    ) throws ServiceException{
        return this.sequenceProvider.getSequence(
            this.referenceFilter.getBase()
        );
    }

    //------------------------------------------------------------------------
    // Implements Collection
    //------------------------------------------------------------------------

    /**
     * Returns the number of elements in this collection. If the collection 
     * contains more than Integer.MAX_VALUE elements or the number of elements
     * is unknown, returns Integer.MAX_VALUE.
     *
     * @eturn   the number of elements in this collection
     */
    @Override
    public int size(
    ) {
        return getDelegate().size();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return getDelegate().isEmpty();
    }

    /**
     * Returns an iterator over the elements contained in this collection.
     * 
     * @return    an iterator over the elements contained in this collection.
     */
    public Iterator iterator(
    ){
        return getDelegate().iterator();
    }

    /**
     * Returns true if this collection contains the specified element. 
     *
     * @param   element
     *          element whose presence in this collection is to be tested.
     *
     * @return  true if this collection contains the specified element
     */
    public boolean contains(
        Object element
    ){
        if(element instanceof DataObject_1) {
            DataObject_1 object = (DataObject_1)element;
            return 
            object.persistenceManager == this.manager &&
            object.jdoIsPersistent() && !object.jdoIsDeleted() &&
            object.jdoGetObjectId().getParent().equals(this.referenceFilter) &&
            (getSelector() == null || getSelector().accept(object));
        } else {
            return false;
        }
    }

    /**
     * Removes a single instance of the specified element from this
     * container, if it is present
     *
     * @param   element
     *          element to be removed from this container, if present.
     *
     * @return  true if this collection changed as a result of the call.
     *
     * @exception   UnsupportedOperationException
     *              remove is not supported by this collection.
     */
    public boolean remove(
        Object element
    ){
        try {
            boolean modify=contains(element);
            if(modify)((DataObject_1)element).objRemove();
            return modify;
        } catch (ServiceException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    /**
     * Selects objects matching the filter.
     * <p>
     * The semantics of the collection returned by this method become
     * undefined if the backing collection (i.e., this list) is structurally
     * modified in any way other than via the returned collection. (Structural
     * modifications are those that change the size of this list, or otherwise
     * perturb it in such a fashion that iterations in progress may yield
     * incorrect results.) 
     * <p>
     * This method returns a Collection as opposed to a Set because it 
     * behaves as set in respect to object id equality, not element equality.
     * <p>
     * The acceptable filter object classes must be specified by the 
     * Container implementation.
     *
     * @param     filter
     *            The filter to be applied to objects of this container
     *
     * @return    A subset of this container containing the objects
     *            matching the filter.
     * 
     * @exception ClassCastException
     *            if the class of the specified filter prevents it from
     *            being applied to this container.
     * @exception IllegalArgumentException
     *            if some aspect of this filter prevents it from being
     *            applied to this container. 
     */
    @SuppressWarnings("deprecation")
    public org.openmdx.base.collection.Container subSet(
        Object filter
    ){
        Selector selector = super.combineWith(filter);
        return selector == null ? this : new PersistentContainer_1(
            this.referenceFilter,
            selector,
            this.manager,
            this.provided,
            this.sequenceProvider, 
            this.keys
        );
    }

    /**
     * Select an object matching the filter.
     * <p>
     * The acceptable filter object classes must be specified by the 
     * Container implementation.
     *
     * @param     filter
     *            The filter to be applied to objects of this container
     *
     * @return    the object matching the filter;
     *            or null if no object matches the filter.
     * 
     * @exception ClassCastException
     *            if the class of the specified filter prevents it from
     *            being applied to this container.
     * @exception IllegalArgumentException
     *            if some aspect of this filter prevents it from being
     *            applied to this container or if more than one object 
     *            matches the filter. 
     */
    public DataObject_1_0 get(
        Object filter
    ){
        try {
            DataObject_1_0 candidate = (DataObject_1_0)this.manager.getObjectById(
                this.referenceFilter.getChild((String)filter)
            );
            return getSelector() == null || getSelector().accept(candidate) ? candidate : null; 
        } catch (Exception source) {
            BasicException target = BasicException.toExceptionStack(source);
            if(target.getExceptionCode() == BasicException.Code.NOT_FOUND) {
                return null;
            } else {
                throw new IllegalArgumentException(target);
            }
        }
    }

    /**
     * Applies given criteria to the elements of the container and returns the
     * result as list.
     * <p>
     * The acceptable criteria classes must be specified by the container 
     * implementation.
     *
     * @param     criteria
     *            The criteria to be applied to objects of this container;
     *            or <code>null</code> for all the container's elements in
     *            their default order.
     *
     * @return    a list based on the container's elements and the given
     *            criteria.
     * 
     * @exception ClassCastException
     *            if the class of the specified criteria prevents them from
     *            being applied to this container's elements.
     * @exception IllegalArgumentException
     *            if some aspect of the criteria prevents them from being
     *            applied to this container's elements. 
     */
    public List toList(
        Object criteria
    ) {
        if(criteria == null) {
            return getDelegate();
        } else if (criteria instanceof AttributeSpecifier[]){       
            //
            // Construct
            //
            AttributeSpecifier[] attributeSpecifier = (AttributeSpecifier[])criteria;
            if(attributeSpecifier.length == 0) {
                return getDelegate();
            } else try {
                ObjectComparator_1 comparator = new ObjectComparator_1(attributeSpecifier);
                ProviderCollection providerCollection = new ProviderCollection(comparator);
                return providerCollection.isExtent() ? new CountingCollection (
                    providerCollection
                ) : new CountingCollection(
                    new Include(getSelector(), comparator),
                    providerCollection
                );
            } catch (ServiceException exception) {
                throw new RuntimeServiceException (exception);
            }
        } else {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "Invalid criteria",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("actual", criteria.getClass().getName()),
                        new BasicException.Parameter("acceptable", AttributeSpecifier[].class.getName())
                    )
                )
            );
        }
    }

    private boolean isNew() {
        return this.sequenceProvider.jdoIsNew();
    }

    public List getDelegate(
    ){
        if(this.collection == null) try {
            if(this.include == null) {
                this.include = new Include(getSelector(), null);
            }
            if(isNew()) { 
                return this.include;
            } else {
                this.collection = new CountingCollection (
                    new List[]{
                        this.include,
                        new ProviderCollection()
                    }
                );
            }
        } catch (ServiceException exception) {
            throw new IllegalArgumentException(exception);
        }
        return this.collection;
    }

    protected final Exclude getExclude(
    ){
        if(this.exclude == null) {
            this.exclude = new Exclude(getSelector());
        }
        return this.exclude;
    }
    
    
    //------------------------------------------------------------------------
    // Implements RefBaseObject
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refMofId()
     */
    public Object getContainerId(
    ) {
        return this.referenceFilter;
    }

    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#toString()
     */
    public String toString(
    ){
        StringBuilder text = new StringBuilder(
            getClass().getName()
        );
        try {
            text.append(
                ": ("
            ).append(
                this.referenceFilter 
            ).append(
                ", "
            ).append(
                getSelector() == null ? 0 : ((AbstractFilter)getSelector()).size()
            ).append(
                " filter properties)"
            ).toString();
        } catch (Exception exception) {
            text.append(
                "// "
            ).append(
                exception
            );
        }
        return text.toString();
    }


    //------------------------------------------------------------------------
    // Implements Evictable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     */
    public void evict() {
        this.provided.evict();      
        if(this.collection instanceof Evictable) {
            ((Evictable)this.collection).evict();
        }
    }

    
    //------------------------------------------------------------------------
    // Class Members
    //------------------------------------------------------------------------

    abstract class Members extends OrderingList {
                
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

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.accessor.FilteringList#getDelegate()
         */
        @Override
        final protected List getSource(
        ){
            try {
                return PersistentContainer_1.this.manager.getUnitOfWork().getMembers();
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.accessor.FilteringList#accept(java.lang.Object)
         */
        @Override
        final protected boolean accept(Object candidate) {
            return candidate instanceof DataObject_1 && accept((DataObject_1)candidate);
        }

        /**
         * Test whether a given candidate is member of the collection
         * 
         * @param candidate
         * 
         * @return <code>true</code> if the candidate is member of the collection
         */
        protected boolean accept(
            DataObject_1 candidate
        ){
            Path objectId = candidate.jdoGetObjectId();
            return 
                objectId != null &&
                objectId.size() == PersistentContainer_1.this.referenceFilter.size() + 1 &&
                objectId.startsWith(PersistentContainer_1.this.referenceFilter); 
        }
        
    }
    
    //------------------------------------------------------------------------
    // Class Include
    //------------------------------------------------------------------------

    /**
     * Include
     */
    final class Include extends Members {

        Include(
            Selector selector,
            Comparator<DataObject_1_0> comparator
        ){
            super(comparator);
            this.selector = selector;
        }
        
        private final Selector selector;
        
        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.accessor.FilteringList#acceptAll()
         */
        @Override
        protected final boolean acceptAll() {
            return this.selector == null;
        }

        /* (non-Javadoc)
         * @see Members#accept(DataObject_1)
         */
        protected boolean accept(DataObject_1 candidate) {
            if(
                !candidate.jdoIsDeleted() &&
                super.accept(candidate)
            ) {
                if(acceptAll()) {
                    return candidate.jdoIsNew();
                } else if(candidate.jdoIsNew() || candidate.jdoIsDirty()) { 
                    return this.selector.accept(candidate);
                }
            }
            return false;
        }

    }

    //------------------------------------------------------------------------
    // Class Exclude
    //------------------------------------------------------------------------

    /**
     * Exclude
     */
    final class Exclude extends Members {

        /**
         * Constructor 
         *
         * @param selector
         */
        Exclude(
            Selector selector
        ){
            super(null);
            this.plain = selector == null;
        }
        
        /**
         * Tells whether there is an attribute filter or not
         */
        private final boolean plain;

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.accessor.FilteringList#acceptAll()
         */
        @Override
        protected final boolean acceptAll() {
            return this.plain;
        }

        
        /* (non-Javadoc)
         * @see Members#accept(DataObject_1)
         */
        protected boolean accept(DataObject_1 candidate) {
            return 
                !candidate.jdoIsNew() &&
                (plain ? candidate.jdoIsDeleted() : candidate.jdoIsDirty()) &&
                super.accept(candidate);
        }

    }
    
    //------------------------------------------------------------------------
    // Class AbstractListIterator
    //------------------------------------------------------------------------

    /**
     * Abstract List Iterator
     */
    abstract static class AbstractListIterator<E> implements ListIterator<E> {

        /**
         * 
         */
        protected AbstractListIterator(
        ){
            super();
        }

        protected abstract ListIterator<E> getDelegate(
        );

        /* (non-Javadoc)
         * @see java.util.ListIterator#nextIndex()
         */
        public int nextIndex() {
            return getDelegate().nextIndex();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#previousIndex()
         */
        public int previousIndex() {
            return getDelegate().previousIndex();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            getDelegate().remove();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return getDelegate().hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#hasPrevious()
         */
        public boolean hasPrevious() {
            return getDelegate().hasPrevious();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public E next() {
            this.currentIndex = getDelegate().nextIndex();
            return current = getDelegate().next();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#previous()
         */
        public E previous() {
            this.currentIndex = getDelegate().previousIndex();
            return current = getDelegate().previous();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#add(java.lang.Object)
         */
        public void add(E arg0) {
            getDelegate().add(arg0);
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#set(java.lang.Object)
         */
        public void set(E arg0) {
            getDelegate().set(arg0);
        }

        /**
         * 
         */
        protected E current = null;

        /**
         * 
         */
        protected int currentIndex;

    }
    
    //------------------------------------------------------------------------
    // Class FilteringIterator
    //------------------------------------------------------------------------

    /**
     * Filtering Iterator
     */
    static class FilteringIterator extends ObjectIterator {

        /**
         * Constructor 
         *
         * @param iterator
         * @param offset
         * @param exclude
         */
        public FilteringIterator(
            ListIterator iterator,
            int offset,
            Selector exclude
        ) {
            super(iterator);
            this.exclude = exclude;
            while(this.nextIndex < offset) {
                next();
            }
        }

        /**
         * 
         */
        private int nextIndex = 0;
        
        /**
         * 
         */
        private int previousIndex = -1;

        /**
         * 
         */
        private int mark = -1;
        
        /**
         * 
         */
        private final Selector exclude;

        private Object prefetched = null;
        
        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.accessor.PersistentContainer_1.AbstractListIterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            if(this.prefetched == null) {
                this.mark = super.previousIndex();
                while(this.prefetched == null && super.hasNext()) {
                    Object candidate = super.next();
                    if(!this.exclude.accept(candidate)) {
                        this.prefetched = candidate;
                    }
                }
            }
            return this.prefetched != null;
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.accessor.PersistentContainer_1.AbstractListIterator#hasPrevious()
         */
        @Override
        public boolean hasPrevious() {
            return this.previousIndex >= 0;
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.accessor.PersistentContainer_1.AbstractListIterator#next()
         */
        @Override
        public Object next() {
            if(hasNext()) {
                Object next = this.prefetched;
                this.prefetched = null;
                this.previousIndex = this.nextIndex++;
                return next;
            }
            throw new NoSuchElementException(
                "There are no more elements in the collection"
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.accessor.PersistentContainer_1.AbstractListIterator#nextIndex()
         */
        @Override
        public int nextIndex() {
            return this.nextIndex;
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.accessor.PersistentContainer_1.AbstractListIterator#previous()
         */
        @Override
        public Object previous() {
            if(this.prefetched != null) {
                while(super.previousIndex() > this.mark) {
                    super.previous();
                }
            }
            Object candidate;
            do {
                candidate = super.previous();
            } while (this.exclude.accept(candidate));
            this.nextIndex = this.previousIndex--;
            return candidate;
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.accessor.PersistentContainer_1.AbstractListIterator#previousIndex()
         */
        @Override
        public int previousIndex() {
            return this.previousIndex;
        }
        
    }
    
    //--------------------------------------------------------------------------
    // Class ObjectIterator
    //--------------------------------------------------------------------------

    /**
     * Object Iterator
     */
    static class ObjectIterator extends AbstractListIterator {

        /**
         * 
         */
        ObjectIterator(
            ListIterator iterator
        ){
            this.iterator = iterator;
        }

        /* (non-Javadoc)
         */
        protected ListIterator getDelegate() {
            return this.iterator;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            if(current == null) throw new IllegalStateException(
                "the is no current object"
            );
            try {
                ((DataObject_1)super.current).objRemove();
            } catch (ServiceException exception) {
                throw new UnsupportedOperationException(exception.getMessage());
            }
        }

        /**
         * 
         */ 
        private final ListIterator iterator;

    }
    
    //--------------------------------------------------------------------------
    // Class ChainingList
    //--------------------------------------------------------------------------

    /**
     * Chaining List
     */
    static class ChainingList<E> extends AbstractSequentialList<E> implements Evictable {

        /**
         * Constructor 
         *
         * @param lists
         */
        ChainingList(
            List<E>[] lists
        ){
            this.lists = lists;
        }

        /**
         * 
         */
        protected List<E>[] lists;

        /* (non-Javadoc)
         * @see java.util.List#listIterator(int)
         */
        public ListIterator<E> listIterator(
            int index
        ) {
            return new ChainingIterator(index);
        }

        /* (non-Javadoc)
         * @see java.util.Collection#size()
         */
        public int size(
        ) {
            int size = 0;
            for(List<E> list : this.lists) {
                int segmentSize = list.size();
                if(segmentSize == Integer.MAX_VALUE) {
                    return Integer.MAX_VALUE;
                }
                size += segmentSize;
            }
            return size;
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            for(List<E> list : this.lists) {
                if(!list.isEmpty()){
                    return false;
                }
            } 
            return true;
        }

        /* (non-Javadoc)
         * @see java.util.AbstractList#clear()
         */
        @Override
        public void clear() {
            for(List<E> list : this.lists) {
                if(!list.isEmpty()){
                    list.clear();
                }
            } 
        }

        /**
         * 
         */
        public List<E>[] getDelegate(
        ){
            return this.lists;
        }

        //------------------------------------------------------------------------
        // Implements Evictable
        //------------------------------------------------------------------------

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.transport.delegation.Evictable#evict()
         */
        public void evict() {
            for(List<E> delegate : this.lists) {
                if(delegate instanceof Evictable) {
                    ((Evictable)delegate).evict();
                }
            }
        }


        //------------------------------------------------------------------------
        // Extends Object
        //------------------------------------------------------------------------

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#toString()
         */
        public String toString(
        ){
            StringBuilder text = new StringBuilder(
                getClass().getName()
            );
            try {
                text.append(
                    ": (delegating to "
                ).append(
                    this.lists.length 
                ).append(
                    "lists)"
                ).toString();
            } catch (Exception exception) {
                text.append(
                    "// "
                ).append(
                    exception.getMessage()
                );
            }
            return text.toString();
        }

        //--------------------------------------------------------------------------
        // Class ChainingIterator
        //--------------------------------------------------------------------------

        /**
         * Chaining Iterator
         */
        class ChainingIterator implements ListIterator<E>{

            /**
             * Constructor 
             *
             * @param nextIndex
             */
            ChainingIterator (
                int nextIndex
            ){
                this.limits[0] = 0;
                int[] sizes = new int[lists.length -1];
                for(
                        int i = 1, total = 0;
                        i < lists.length;
                        i++
                ) this.limits[i] = total += sizes[i-1] = lists[i-1].size();
                this.limits[lists.length] = Integer.MAX_VALUE;
                for(
                        int i = 0;
                        i < lists.length;
                        i++
                ) {
                    if(nextIndex < this.limits[i]) {
                        this.iterators[i] = lists[i].listIterator(0);
                    } else if (nextIndex >= this.limits[i+1]){
                        this.iterators[i] = lists[i].listIterator(this.limits[i+1] - this.limits[i]);
                    } else {
                        this.iterators[i] = lists[i].listIterator(nextIndex - this.limits[i]);
                        this.currentIterator = i;
                    }
                }
            }

            /**
             * 
             */
            private int[] limits = new int[lists.length+1];

            /**
             * 
             */
            private ListIterator<E>[] iterators = new ListIterator[lists.length];

            /**
             * 
             */
            int currentIterator = 0;

            /* (non-Javadoc)
             * @see java.util.ListIterator#hasNext()
             */
            public boolean hasNext(
            ) {
                for(
                        int i = this.currentIterator;
                        i < this.iterators.length;
                        i++
                ) if (
                        this.iterators[i].hasNext()
                ) return true;
                return false;
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#next()
             */
            public E next(
            ) {
                while (!this.iterators[this.currentIterator].hasNext()) {
                    if(this.currentIterator >= this.iterators.length - 1) {
                        throw new NoSuchElementException("End of last list reached");
                    }
                    this.currentIterator++;
                }
                return this.iterators[this.currentIterator].next();
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#hasPrevious()
             */
            public boolean hasPrevious() {
                for(
                        int i = this.currentIterator;
                        i >= 0;
                        i--
                ) if (
                        this.iterators[i].hasPrevious()
                ) return true;
                return false;
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#previous()
             */
            public E previous() {
                while (!this.iterators[this.currentIterator].hasPrevious()) {
                    if(
                            this.currentIterator <= 0
                    ) throw new NoSuchElementException(
                        "Begin of first list reached"
                    );
                    this.currentIterator--;
                }
                return this.iterators[this.currentIterator].previous();
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#nextIndex()
             */
            public int nextIndex() {
                return 
                this.limits[this.currentIterator] + 
                this.iterators[this.currentIterator].nextIndex();
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#previousIndex()
             */
            public int previousIndex() {
                return 
                this.limits[this.currentIterator] + 
                this.iterators[this.currentIterator].previousIndex();
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#remove()
             */
            public void remove() {
                this.iterators[this.currentIterator].remove();
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#set(java.lang.Object)
             */
            public void set(E object) {
                this.iterators[this.currentIterator].set(object);
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#add(java.lang.Object)
             */
            public void add(E object) {
                this.iterators[this.currentIterator].add(object);
            }

        }

    }
    
    //------------------------------------------------------------------------
    // Class CountingCollection
    //------------------------------------------------------------------------

    /**
     * Counting Collection
     */
    static class CountingCollection 
        extends ChainingList 
        implements Serializable
    {

        /**
         * @param lists
         */
        CountingCollection(List... lists) {
            super(lists);
        }

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = 3257562923424559925L;

        /* (non-Javadoc)
         * @see java.util.Collection#size()
         */
        public int size() {
            int collectionSize = super.size();
            if(collectionSize != Integer.MAX_VALUE) {
                return collectionSize;
            }
            int count = 0;
            for(
                Iterator i = listIterator(0); 
                i.hasNext();
                i.next()
            ) {
                if(++count % LOCAL_EVALUATION_LIMIT == 0) {
                    collectionSize = super.size();
                    if(collectionSize != Integer.MAX_VALUE) {
                        return collectionSize;
                    }
                }
            }
            return count;
        }

        /* (non-Javadoc)
         * @see java.util.List#listIterator(int)
         */
        public ListIterator listIterator(int index) {
            return new ObjectIterator(super.listIterator(index));
        }

    }

    //------------------------------------------------------------------------
    // Class CountingList
    //------------------------------------------------------------------------

    /**
     * Counting List
     */
    class CachingList 
        extends MarshallingSequentialList 
        implements Reconstructable, Marshaller, Serializable, Evictable
    {

        /**
         * Constructor 
         *
         * @param order
         */
        CachingList(
            boolean extent,
            boolean plain,
            ObjectComparator_1 order
        ) {
            this.comparator = order;
            this.extent = extent;
            this.plain = plain;
            this.ordering = order != null;
        }

        /**
         * Tells whether it is an extent or not
         * 
         * @return <code>true</code> in case of extent
         */
        final boolean isExtent(){
            return this.extent;          
        }
        
        private final boolean extent;
        
        private final boolean plain;

        private final boolean ordering;
        
        private transient List cached = null;

        private final ObjectComparator_1 comparator;
        
        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = -4316483048099731080L;

        boolean useCache(){
            return 
                !this.extent && 
                (this.ordering || !this.plain) && 
                (PersistentContainer_1.this.provided.isCached() || PersistentContainer_1.this.provided.size() < LOCAL_EVALUATION_LIMIT);
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.collection.MarshallingSequentialList#getMarshaller()
         */
        @Override
        protected Marshaller getMarshaller() {
            return useCache() ? this : PersistentContainer_1.this.manager;
        }

        /* (non-Javadoc)
         */
        public void write(OutputStream stream) throws ServiceException {
            boolean propagate = super.list instanceof Reconstructable; 
            try {
                ObjectOutputStream buffer=new ObjectOutputStream(stream);
                buffer.writeBoolean(propagate);
                if(propagate){
                    buffer.flush();
                    ((Reconstructable)super.list).write(stream);
                } else {
                    buffer.flush();
                }
            } catch (IOException e) {
                throw new ServiceException(e);
            }
        }

        /* (non-Javadoc)
         */
        public Object marshal(Object source) throws ServiceException {
            return source;
        }

        /* (non-Javadoc)
         */
        public Object unmarshal(Object source) throws ServiceException {
            return source;
        }

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.transport.delegation.Evictable#evict()
         */
        public void evict() {
            this.cached = null;
            super.list = null;
        }
        
        protected String getQueryType(){
            if(this.plain) {
                try {
                    return (String) getModel().getTypes(PersistentContainer_1.this.referenceFilter)[2].objGetValue("qualifiedName");
                } catch (ServiceException exception) {
                    exception.log();
                    return null;
                }
            } else {
                String instanceOf = null;
                for(FilterProperty property : ((AbstractFilter)getSelector()).getDelegate()) {
                    Object[] values = property.getValues();
                    if(
                        property.operator() == FilterOperators.IS_IN &&
                        values != null && values.length == 1
                    ) { 
                        if(SystemAttributes.OBJECT_CLASS.equals(property.name())) {
                            return (String) values[0];
                        } else if (SystemAttributes.OBJECT_INSTANCE_OF.equals(property.name())) {
                            instanceOf = (String) values[0];
                        }
                    }
                }
                return instanceOf;
            }
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.collection.MarshallingSequentialList#getDelegate()
         */
        @Override
        protected List getDelegate() {
            if(useCache()) {
                if(this.cached == null) {
                    this.cached = this.ordering ? new OrderingList(
                        this.comparator
                    ){
                        
                        /* (non-Javadoc)
                         * @see org.openmdx.application.dataprovider.accessor.FilteringList#acceptAll()
                         */
                        @Override
                        protected boolean acceptAll() {
                            return getSelector() == null;
                        }

                        @Override
                        protected boolean accept(Object candidate) {
                            return getSelector().accept(candidate);
                        }

                        @Override
                        protected List<DataObject_1_0> getSource() {
                            return PersistentContainer_1.this.provided;
                        }
                        
                    } : new FilteringList(){

                        @Override
                        protected final boolean accept(Object candidate) {
                            return getSelector().accept(candidate);
                        }

                        @Override
                        protected final List getSource() {
                            return PersistentContainer_1.this.provided;
                        }
                            
                    };
                }
                return this.cached;
            } else { 
                if(super.list == null) {
                    try {
                        this.list = this.plain && !this.ordering ? PersistentContainer_1.this.provided : new BatchingList(
                            PersistentContainer_1.this.manager,
                            PersistentContainer_1.this.referenceFilter,
                            getQueryType(),
                            JavaBeans.toXML(
                                new Filter(
                                    this.plain ? null : ((AbstractFilter)getSelector()).getDelegate(),
                                    this.ordering ? this.comparator.getDelegate() : null   
                                )
                            )
                        );
                    } 
                    catch (ServiceException e) {
                        throw new RuntimeServiceException(e);
                    }
                }
                return super.list;
            }
        }
    }
    
    //------------------------------------------------------------------------
    // Class ProviderCollection
    //------------------------------------------------------------------------

    /**
     * Provider Collection
     */
    final class ProviderCollection 
        extends FilteringList 
        implements Selector, Evictable
    {

        /**
         * Constructor 
         *
         * @param order
         * 
         * @throws ServiceException
         */
        ProviderCollection(
        ) throws ServiceException {
            this(
                null // order
            );
        }

        /**
         * Constructor 
         *
         * @param order
         * 
         * @throws ServiceException
         */
        ProviderCollection(
            ObjectComparator_1 order
        ) throws ServiceException {
            this(
                PersistentContainer_1.this.referenceFilter.isLike(EXTENT_REFERENCES),
                getSelector() == null,
                order
            );
        }

        /**
         * Constructor 
         *
         * @param extent
         * @param order
         * @throws ServiceException
         */
        private ProviderCollection(
            boolean extent,
            boolean plain,
            ObjectComparator_1 order
        ) throws ServiceException {
            this.source = new CachingList(
                extent,
                plain,
                order
            );
            this.extent = extent;
            this.plain = plain;
        }
        
        private final CachingList source;
        
        private final boolean extent;

        private final boolean plain;
        
        final boolean isExtent(){
            return this.extent;
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.query.Selector#accept()
         */
        public final boolean accept(Object candidate) {
            return this.extent || !getExclude().accept(candidate);
        }

        /* (non-Javadoc)
         */
        @Override
        protected final boolean acceptAll (
        ){
            return this.extent || getExclude().isEmpty();
        }

        /* (non-Javadoc)
         */
        @Override
        protected final void removeInternal(
            Object object, 
            ListIterator iterator
        ) throws ServiceException {
            if(object instanceof DataObject_1){
                ((DataObject_1)object).objRemove();
            } else {
                super.removeInternal(object, iterator);
            }
        }

        /* (non-Javadoc)
         * @see java.util.Collection#size()
         */
        @Override
        public final int size() {
            if(this.plain && !this.extent) {
                int providerCount = PersistentContainer_1.this.provided.size();
                if(providerCount != Integer.MAX_VALUE) {
                    return providerCount - getExclude().size();
                }
            } else if (getExclude().isEmpty() && !this.source.useCache()) {
                return this.source.size();
            }
            return Integer.MAX_VALUE;
        }

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.transport.delegation.Evictable#evict()
         */
        public final void evict() {
            ((Evictable)getSource()).evict();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractList#removeRange(int, int)
         */
        @Override
        protected final void removeRange(int fromIndex, int toIndex) {
            if(toIndex < Integer.MAX_VALUE) {
                super.removeRange(fromIndex, toIndex);
            } else {
                for(
                    ListIterator<?> i = listIterator(fromIndex);
                    i.hasNext();
                ){
                    i.next();
                    i.remove();
                }
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.accessor.FilteringList#getSource()
         */
        @Override
        protected final List getSource() {
            return this.source;
        }

    }

    //------------------------------------------------------------------------
    // Class KeySet
    //------------------------------------------------------------------------

    /**
     * Key Set
     */
    static final class KeySet extends AbstractSet<String> {

        /**
         * Constructor 
         *
         * @param delegate
         */
        KeySet(
            Collection<DataObject_1_0> delegate
        ){
            this.delegate = delegate;            
        }

        /**
         * 
         */
        private final Collection<DataObject_1_0> delegate;        

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<String> iterator() {
            return new KeyIterator(
                this.delegate.iterator()
            );
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return delegate.size();
        }

    }

    //------------------------------------------------------------------------
    // Class KeyIterator
    //------------------------------------------------------------------------

    /**
     * Key Iterator
     */
    final static class KeyIterator implements Iterator<String> {

        /**
         * Constructor 
         *
         * @param delegate
         */
        KeyIterator(
            Iterator<DataObject_1_0> delegate
        ){
            this.delegate = delegate;
        }

        private final Iterator<DataObject_1_0> delegate;

        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        public String next() {
            return this.delegate.next().jdoGetObjectId().getBase();
        }

        public void remove() {
            this.delegate.remove();
        }

    }
    
    //------------------------------------------------------------------------
    // Class KeyIterator
    //------------------------------------------------------------------------

    /**
     * Merging List
     */
    static class MergingList extends AbstractSequentialList {

        /**
         * Constructor
         *
         * @param x
         * @param y
         * @param comparator
         */
        MergingList(
            List x,
            List y,
            Comparator comparator
        ){
            this.lists = new List[]{x,y};
            this.comparator = comparator;
        }

        /* (non-Javadoc)
         * @see java.util.List#listIterator(int)
         */
        public ListIterator listIterator(int index) {
            return new MergingListIterator(index);
        }

        /* (non-Javadoc)
         * @see java.util.Collection#size()
         */
        public int size() {
            int total = 0;
            for(
                    int i = 0;
                    i < this.lists.length;
                    i++
            ){
                int s = this.lists[i].size();
                if(s == Integer.MAX_VALUE) return Integer.MAX_VALUE;
                total += s;
            }
            return total;
        }

        /* (non-Javadoc)
         * @see java.util.Collection#isEmpty()
         */
        public boolean isEmpty() {
            for(List list : this.lists) {
                if(!list.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        /**
         * 
         */
        protected List[] lists;

        /**
         * 
         */
        protected Comparator comparator;


        //------------------------------------------------------------------------
        // Class MergingListIterator
        //------------------------------------------------------------------------

        /**
         * 
         */
        class MergingListIterator implements ListIterator {

            /**
             * Filtering Iterator
             */
            public MergingListIterator(
                int index
            ){
                if(lists[0].isEmpty()){
                    this.iterators[0] = lists[0].listIterator();
                    this.iterators[1] = lists[1].listIterator(index);
                    this.nextIndex = this.iterators[1].nextIndex();
                    this.previousIndex = this.iterators[1].previousIndex();
                } else if (lists[1].isEmpty()) {
                    this.iterators[0] = lists[0].listIterator(index);
                    this.iterators[1] = lists[1].listIterator();
                    this.nextIndex = this.iterators[0].nextIndex();
                    this.previousIndex = this.iterators[0].previousIndex();
                } else {
                    this.iterators[0] = lists[0].listIterator();
                    this.iterators[1] = lists[1].listIterator();
                    for(
                            int i=0;
                            i < index;
                            i++
                    ) next();
                }
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#hasNext()
             */
            public boolean hasNext() {
                return this.prefetched[0] || this.prefetched[1] || 
                this.iterators[0].hasNext() || this.iterators[1].hasNext();
            }   

            /* (non-Javadoc)
             * @see java.util.Iterator#next()
             */
            public Object next() {
                setDirection(true);
                for(
                        int i = 0;
                        i < lists.length;
                        i++
                ) if(
                        !this.prefetched[i] && (this.prefetched[i] = this.iterators[i].hasNext())
                ) this.values[i] = this.iterators[i].next();
                if(this.prefetched[0]){
                    if(this.prefetched[1]){
                        if(comparator.compare(this.values[0], this.values[1]) <= 0){
                            this.current = 0;
                        } else {
                            this.current = 1;
                        }
                    } else {
                        this.current = 0;
                    }
                } else if (this.prefetched[1]){
                    this.current = 1;
                } else {
                    this.current = -1;
                    throw new NoSuchElementException("End of list reached");
                } 
                this.previousIndex = this.nextIndex++;
                this.prefetched[this.current] = false;
                return this.values[this.current];
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#hasPrevious()
             */
            public boolean hasPrevious() {
                return this.previousIndex >= 0;
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#previous()
             */
            public Object previous(
            ){
                setDirection(false);
                for(
                        int i = 0;
                        i < lists.length;
                        i++
                ) if(
                        !this.prefetched[i] && (this.prefetched[i] = this.iterators[i].hasPrevious())
                ) this.values[i] = this.iterators[i].previous();
                if(this.prefetched[0]){
                    if(this.prefetched[1]){
                        if(comparator.compare(this.values[0], this.values[1]) > 0){
                            this.current = 0;
                        } else {
                            this.current = 1;
                        }
                    } else {
                        this.current = 0;
                    }
                } else if (this.prefetched[1]){
                    this.current = 1;
                } else {
                    this.current = -1;
                    throw new NoSuchElementException("Beginning of list reached");
                } 
                this.nextIndex = this.previousIndex--;
                this.prefetched[this.current] = false;
                return this.values[this.current];
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#nextIndex()
             */
            public int nextIndex(
            ) {
                return this.nextIndex;
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#previousIndex()
             */
            public int previousIndex() {
                return this.previousIndex;
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#remove()
             */
            public void remove(
            ) {
                getCurrent().remove();
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#set(java.lang.Object)
             */
            public void set(Object object) {
                getCurrent().set(object);
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#add(java.lang.Object)
             */
            public void add(Object object) {
                throw new UnsupportedOperationException();
            }

            /**
             * Validate and return current
             */
            private ListIterator getCurrent (
            ){
                if(this.current == -1) throw new IllegalStateException(
                    "Iterator has no current element"
                );
                return this.iterators[this.current];
            }

            /**
             * 
             */
            private void setDirection(
                boolean ascending
            ){
                if(this.ascending != ascending) { // change direction       
                    for(
                            int i = 0;
                            i < lists.length;
                            i++
                    ) if (this.prefetched[i]) {
                        if(ascending){
                            this.iterators[i].next();
                        } else {
                            this.iterators[i].previous();
                        }
                    }
                    this.ascending = ascending;
                }
            }

            /**
             * Delegate
             */
            protected final ListIterator[] iterators = new ListIterator[lists.length];

            /**
             * Index of the element to be returned by next()
             */
            private int nextIndex = 0;  

            /**
             * Index of the element to be returned by previous()
             */
            private int previousIndex = -1; 

            /**
             * Value
             */
            private Object[] values = new Object[lists.length];

            /**
             * Direction
             */
            private boolean ascending;

            /**
             * Has read ahead
             */
            private boolean[] prefetched = new boolean[lists.length];

            /**
             * Direction
             */
            private int current = -1;

        }

    }

    //------------------------------------------------------------------------
    // Class EvictablePersistentObjects
    //------------------------------------------------------------------------

    /**
     * Evictable Persistent Objects
     */
    private class EvictablePersistentObjects
        extends MarshallingSequentialList<DataObject_1_0>
        implements Evictable, Serializable
    {

        /**
         * Constructor 
         *
         * @param feature
         */
        EvictablePersistentObjects(
        ){
            super(PersistentContainer_1.this.sequenceProvider.persistenceManager, null);
        }

        /**
        * Implements <code>Serializable</code>
        */
       private static final long serialVersionUID = 3257285812084159024L;

        /* (non-Javadoc)
         */
        protected List<Object> getDelegate() {
            if(super.list == null) {
                if(PersistentContainer_1.this.sequenceProvider.jdoIsNew()){
                    super.list = Collections.emptyList();
                } else {
                    if(PersistentContainer_1.this.sequenceProvider.persistentValues != null) {
                        super.list = (List)PersistentContainer_1.this.sequenceProvider.persistentValues.get(
                            PersistentContainer_1.this.referenceFilter.getBase()
                        );
                    }
                    if(super.list == null) {
                        try {
                            super.list = new BatchingList(
                                PersistentContainer_1.this.manager,
                                PersistentContainer_1.this.referenceFilter,
                                (String) getModel().getTypes(PersistentContainer_1.this.referenceFilter)[2].objGetValue("qualifiedName"),
                                null
                            );
                        } 
                        catch (ServiceException exception) {
                            throw new RuntimeServiceException(exception);
                        }
                    }
                }
            }
            return super.list;
        }

        boolean isCached(){
            return super.list != null && !(super.list instanceof BatchingList);
        }
        
        void retrieveAll(
            boolean useFetchPlan
        ){
            if(!isCached()) {
                super.list = new ArrayList<Object>(super.list); // TODO take useFetchPlan into account
            }
        }
        
        /* (non-Javadoc)
         */
        public void evict() {
            super.list = null;
        }

        /* (non-Javadoc)
         * @see java.util.Collection#size()
         */
        public int size() {
            return super.list == null ? Integer.MAX_VALUE : super.size();
        }

    }

    //-----------------------------------------------------------------------
    private static class BatchingList extends AbstractSequentialList<Object> {

        /**
         * Constructor 
         *
         * @param manager
         * @param resourceIdentifier
         * @param queryType
         * @param query
         */
        BatchingList(
            DataObjectManager_1 manager,
            Path resourceIdentifier,
            String queryType,
            String query
        ) {
            this.manager = manager;
            this.resourceIdentifier = resourceIdentifier;
            this.queryType = queryType;
            this.query = query;
        }

        final DataObjectManager_1 manager;        
        private final Path resourceIdentifier;        
        private final String queryType;        
        private final String query;        
        Integer total = null;
        int highWaterMark = 0;
        private Map<DataObjectSlice,Integer> cache = new WeakHashMap<DataObjectSlice,Integer>();        
        
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
            synchronized(this.cache) {
                for(DataObjectSlice slice : cache.keySet()) {
                    if(slice.offset <= index && index < slice.offset + slice.size()) {
                        return slice;
                    }
                }
            }
            return null;
        }

        /**
         * Add a slice to the cache
         * 
         * @param slice
         */
        void addSlice(
            DataObjectSlice slice
        ){
            synchronized(this.cache) {
                this.cache.put(slice, null);
            }
        }
        
        /**
         * Create a query
         * 
         * @return a new Query object
         */
        Query_2Facade newQuery(
        ){
            try {
                Query_2Facade facade = Query_2Facade.newInstance();
                facade.setPath(this.resourceIdentifier);
                facade.setQueryType(this.queryType);
                facade.setQuery(this.query);
                return facade;
            } catch (ResourceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractSequentialList#listIterator(int)
         */
        @Override
        public ListIterator listIterator(int index) {
            return new BatchingIterator(index);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size(
        ) {
            if(this.total == null) {
                int total = this.highWaterMark;
                ListIterator<?> i;
                try {
                    i = listIterator(this.highWaterMark);
                } 
                catch (IndexOutOfBoundsException exeption) {
                    i = listIterator(this.highWaterMark = 0);
                }
                while(i.hasNext()){
                    if(this.total != null) {
                        return this.total.intValue();
                    }
                    total = i.nextIndex();
                    i.next();
                }
                this.total = Integer.valueOf(total);
            }
            return this.total.intValue();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty(
        ) {
            return this.total == null ?  
                this.highWaterMark == 0 && !listIterator(0).hasNext() :
                this.total.intValue() == 0;
        }

        //------------------------------------------------------------------------
        // Class BatchingIterator
        //------------------------------------------------------------------------
        
        /**
         * Iterator
         */
        class BatchingIterator implements ListIterator<DataObject_1_0> {

            /**
             * Constructor 
             *
             * @param index
             */
            BatchingIterator(
                int index
            ) {
                this.nextIndex = index;
                this.previousIndex = index - 1;
            }

            private final int BATCH_SIZE = 50;            
            private int previousIndex;
            private int nextIndex;
            private DataObjectSlice slice = null;
                        
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
                        this.load(
                            ascending ? index :
                            index > BATCH_SIZE ? index - BATCH_SIZE : 
                            0
                        );
                    } 
                    catch (ResourceException exception) {
                        throw Throwables.initCause(
                            new NoSuchElementException("Could not retrieve element " + index),
                            exception, 
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.GENERIC,
                            new BasicException.Parameter("total", total),
                            new BasicException.Parameter("index", index)
                        );
                    }
                }
                return this.slice.get(index - this.slice.offset);
            }
            
            
            /* (non-Javadoc)
             * @see java.util.ListIterator#add(java.lang.Object)
             */
            public void add(DataObject_1_0 o) {
                throw new UnsupportedOperationException("A list proxy is unmodifiable");
            }

            private boolean load(
                int index
            ) throws ResourceException {
                if(
                    (BatchingList.this.total == null) || 
                    (index < BatchingList.this.total)
                ) {
                    this.slice = getSlice(index);
                    if(this.slice == null) {
                        Query_2Facade query = newQuery();
                        query.setPosition(index);
                        query.setSize(BATCH_SIZE);
                        IndexedRecord cache = (IndexedRecord) manager.getInteraction2().execute(
                            manager.getInteractionSpecs().GET,
                            query.getDelegate()
                        );
                        this.slice = new DataObjectSlice(index);
                        Long total = null;
                        Integer highWaterMark = null;
                        if(cache instanceof ResultRecord) {
                            ResultRecord result = (ResultRecord) cache;
                            total = result.getTotal();
                            if(total == null) {
                                Boolean hasMore = result.getHasMore();
                                if(Boolean.TRUE.equals(hasMore)) {
                                    highWaterMark = Integer.valueOf(index + cache.size() + 1);
                                } 
                                else if (
                                    Boolean.FALSE.equals(hasMore) &&
                                    (index == 0 || !cache.isEmpty()) 
                                ) {
                                    total = Long.valueOf(index + cache.size());
                                }
                            }
                        } 
                        else if(index == 0 && cache.isEmpty()) {
                            total = Long.valueOf(0);
                        }
                        if(total != null) {
                            BatchingList.this.total = Integer.valueOf(BatchingList.this.highWaterMark = total.intValue());
                        } 
                        else if(
                            BatchingList.this.total == null &&
                            highWaterMark != null &&
                            BatchingList.this.highWaterMark < highWaterMark.intValue()
                        ) {
                            BatchingList.this.highWaterMark = highWaterMark.intValue();
                        }
                        for(Object o : cache) {
                            this.slice.add(manager.receive((MappedRecord) o));
                        }
                        boolean loaded = !this.slice.isEmpty(); 
                        if(loaded) {
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
            
            /* (non-Javadoc)
             * @see java.util.ListIterator#hasNext()
             */
            public boolean hasNext(
            ) {
                try {
                    return this.nextIndex < highWaterMark || load(this.nextIndex);
                } 
                catch (ResourceException exception) {
                    return false;
                }
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#hasPrevious()
             */
            public boolean hasPrevious(
            ) {
                return this.previousIndex >= 0;
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#next()
             */
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

            /* (non-Javadoc)
             * @see java.util.ListIterator#nextIndex()
             */
            public int nextIndex(
            ) {
                return this.nextIndex;
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#previous()
             */
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

            /* (non-Javadoc)
             * @see java.util.ListIterator#previousIndex()
             */
            public int previousIndex(
            ) {
                return this.previousIndex;
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#remove()
             */
            public void remove(
            ) {
                throw new UnsupportedOperationException("A list proxy is unmodifiable");
            }

            /* (non-Javadoc)
             * @see java.util.ListIterator#set(java.lang.Object)
             */
            public void set(
                DataObject_1_0 o
            ) {
                throw new UnsupportedOperationException("A list proxy is unmodifiable");
            }
            
        }
        
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
            
            /* (non-Javadoc)
             * @see java.util.AbstractList#equals(java.lang.Object)
             */
            @Override
            public boolean equals(Object that) {
                return this == that;
            }

            /* (non-Javadoc)
             * @see java.util.AbstractList#hashCode()
             */
            @Override
            public int hashCode() {
                return System.identityHashCode(this);
            }
            
        }

    }

}
