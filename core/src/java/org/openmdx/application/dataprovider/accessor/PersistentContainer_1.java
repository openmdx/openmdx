/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PersistentContainer_1.java,v 1.17 2009/03/03 17:23:08 hburger Exp $
 * Description: A persistent container implementation
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/03 17:23:08 $
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
package org.openmdx.application.dataprovider.accessor;

import static org.openmdx.base.naming.SpecialResourceIdentifiers.EXTENT_REFERENCES;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
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
import java.util.NoSuchElementException;
import java.util.Set;

import org.openmdx.application.dataprovider.accessor.UnitOfWork_1.Exclude;
import org.openmdx.application.dataprovider.cci.AttributeSpecifier;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.collection.MarshallingSequentialList;
import org.openmdx.base.collection.Reconstructable;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.AbstractFilter;
import org.openmdx.base.query.Selector;
import org.openmdx.kernel.exception.BasicException;

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
        Connection_1 manager,
        Channel provider,
        List provided,
        DataObject_1 sequenceProvider, 
        Set<String> keys
    ){
        super(manager.getModel(), attributeFilter);
        if(provider == null) throw new IllegalArgumentException(
            "A persistent container's provider must not be null"
        );
        if(manager == null) throw new IllegalArgumentException(
            "A persistent container's manager must not be null"
        );      
        this.referenceFilter = referenceFilter;
        this.manager = manager;
        this.provider = provider;
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
     * @param providerManagedObjects
     * @param sequenceProvider
     */
    PersistentContainer_1(
        Path referenceFilter,
        Connection_1 manager,
        Channel provider,
        String featureName, 
        DataObject_1 sequenceProvider
    ){
        this(referenceFilter, manager, provider, sequenceProvider);
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
        Connection_1 manager,
        Channel provider,
        DataObject_1 sequenceProvider
    ){
        this(
            referenceFilter, 
            null, // attributeFilter
            manager, 
            provider,
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
    protected Connection_1 manager;

    /**
     * @serial
     */
    protected Channel provider;

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
    protected final List provided;

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
    Channel getProvider(
    ){
        return this.provider;
    }

    /**
     *
     */
    Connection_1 getManager(
    ){
        return this.manager;
    }


    //--------------------------------------------------------------------------
    // Extends AbstractContainer
    //--------------------------------------------------------------------------

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
            object.connection == this.manager &&
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
            this.provider,
            this.provided,
            this.sequenceProvider, 
            this.keys
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.delegation.AbstractContainer#superSet(java.lang.Object)
     */
    @Override
    protected AbstractContainer<DataObject_1_0> superSet() {
        return new PersistentContainer_1(
            this.referenceFilter,
            null,
            this.manager,
            this.provider,
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
        } else if(criteria instanceof InputStream){
            try {
                //
                // Reconstruct
                //
                InputStream iterator = (InputStream)criteria;
                ObjectInputStream buffer=new ObjectInputStream(iterator);
                Selector attributeFilter = (Selector)buffer.readObject();
                ObjectComparator_1 comparator = (ObjectComparator_1)buffer.readObject();
                return buffer.readBoolean() ? new ReconstructableList(
                    attributeFilter,
                    new Include(attributeFilter, comparator),
                    new MarshallingSequentialList(
                        this.manager,
                        this.provider.reconstruct(
                            this.referenceFilter,
                            this.manager,
                            iterator
                        )
                    ),
                    comparator,
                    PersistentContainer_1.this.manager.getUnitOfWork().exclude(
                        PersistentContainer_1.this.referenceFilter,
                        attributeFilter
                    )
                ) :  new PersistentContainer_1(
                    this.referenceFilter,
                    attributeFilter,
                    this.manager,
                    this.provider,
                    this.provided,
                    this.sequenceProvider, 
                    this.keys
                ).toList(
                    comparator.getDelegate()
                );
            } catch (Exception exception) {
                throw new IllegalArgumentException (exception);
            }
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
                        new BasicException.Parameter("acceptable", InputStream.class.getName(), AttributeSpecifier[].class.getName())
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
            if(this.include == null) try {
                this.include = (
                    this.manager.getUnitOfWork()
                ).include(
                    this.referenceFilter,
                    getSelector()
                );
            } catch (ServiceException serviceException) {
                throw new IllegalStateException(serviceException);
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
        if(this.exclude == null) try {
            this.exclude = this.manager.getUnitOfWork().exclude(
                PersistentContainer_1.this.referenceFilter,
                getSelector()
            );
        } catch (ServiceException serviceException) {
            throw new IllegalStateException(serviceException);
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
                this.referenceFilter.toXri() 
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
        if(this.provided instanceof Evictable) {
            ((Evictable)this.provided).evict();      
        }
        if(this.collection instanceof Evictable) {
            ((Evictable)this.collection).evict();
        }
    }


    //------------------------------------------------------------------------
    // Class Include
    //------------------------------------------------------------------------

    /**
     * Include
     */
    class Include extends AbstractSequentialList<Object> {

        /**
         * Constructor 
         *
         * @param attributeFilter
         */
        Include(
            Selector attributeFilter,
            Comparator comparator
        ){
            this.attributeFilter = attributeFilter;
            this.comparator = comparator;
        }

        /**
         * 
         */
        private final Selector attributeFilter;

        /**
         * 
         */
        private final Comparator comparator;

        /**
         * 
         * @return
         */
        private List<?> getSelection(){
            try {
                return PersistentContainer_1.this.manager.getUnitOfWork().include(
                    PersistentContainer_1.this.referenceFilter,
                    this.attributeFilter
                );
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        /* (non-Javadoc)
         * @see java.util.AbstractSequentialList#listIterator(int)
         */
        @Override
        public ListIterator<Object> listIterator(int index) {
            List<Object> list = new ArrayList<Object>(getSelection());
            Collections.sort(list, this.comparator);
            return list.listIterator(index);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return getSelection().size();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return getSelection().isEmpty();
        }

    }


    //------------------------------------------------------------------------
    // Class ReconstructableList
    //------------------------------------------------------------------------

    /**
     * Reconstructable List
     */
    static class ReconstructableList 
        extends MergingList 
        implements Reconstructable
    {

        /**
         * @param x
         * @param y
         * @param comparator
         */
        ReconstructableList(
            Selector filter,
            List x, 
            List y,
            Comparator comparator,
            Collection<?> exclude 
        ) {
            super(x, y, comparator);
            this.filter = filter;
            this.exclude = exclude;
        }

        /**
         * 
         */     
        private final Selector filter;

        /**
         * 
         */
        private final Collection<?> exclude;

        /* (non-Javadoc)
         */
        public void write(OutputStream stream) throws ServiceException {
            boolean propagate = !lists[1].isEmpty() && lists[1] instanceof Reconstructable;
            try {
                ObjectOutputStream buffer=new ObjectOutputStream(stream);
                buffer.writeObject(filter);
                buffer.writeObject(comparator);
                buffer.writeBoolean(propagate);
                buffer.flush();
                if(propagate)((Reconstructable)lists[1]).write(stream);
            } catch (IOException e) {
                throw new ServiceException(e);
            }
        }

        /* (non-Javadoc)
         * @see java.util.List#listIterator(int)
         */
        public ListIterator<?> listIterator(
            int index
        ) {
            return new FilteringIterator(
                super.listIterator(0), 
                index, 
                (Selector)exclude
            );
        }

        /* (non-Javadoc)
         * @see java.util.Collection#size()
         */
        public int size() {
            if(this.exclude.isEmpty()) {
                int collectionSize = super.size();
                if(collectionSize != Integer.MAX_VALUE) {
                    return collectionSize;
                }
                int iterationCount = 0;
                for (
                        Iterator i = this.iterator();
                        i.hasNext();
                ){
                    i.next();
                    if(iterationCount++ % LOCAL_EVALUATION_LIMIT == 0) {
                        collectionSize = super.size();
                        if(collectionSize != Integer.MAX_VALUE) {
                            return collectionSize;
                        }
                    }
                }
                return iterationCount;
            } else {
                int collectionSize = 0;
                for(
                    Iterator<?> i = listIterator(0);
                    i.hasNext();
                ){
                    i.next();
                    collectionSize++;
                }
                return collectionSize;
            }
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
                !this.extent && (this.ordering || !this.plain) && 
                PersistentContainer_1.this.provided.size() < LOCAL_EVALUATION_LIMIT;
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
        
        /* (non-Javadoc)
         * @see org.openmdx.base.collection.MarshallingSequentialList#getDelegate()
         */
        @Override
        protected List getDelegate() {
            if(useCache()) {
                if(this.cached == null) {
                    this.cached = this.ordering ? new OrderingList(
                        PersistentContainer_1.this.provided,
                        getSelector(),
                        this.comparator
                    ) : new FilteringList(
                        PersistentContainer_1.this.provided,
                        getSelector()
                    );
                }
                return this.cached;
            } else {
                if(super.list == null) try {
                    super.list = this.plain && !this.ordering ? PersistentContainer_1.this.provided : provider.find(
                        PersistentContainer_1.this.referenceFilter,
                        this.plain ? null : ((AbstractFilter)getSelector()).getDelegate(),
                        this.ordering ? this.comparator.getDelegate() : null, // attributeSpecifier
                        PersistentContainer_1.this.manager
                    );
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
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
    class ProviderCollection 
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
            super(
                new CachingList(
                    extent,
                    plain,
                    order
                )
            );
            this.extent = extent;
            this.plain = plain;
        }
        
        private final boolean extent;

        private final boolean plain;
        
        boolean isExtent(){
            return this.extent;
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.query.Selector#accept()
         */
        public boolean accept(Object candidate) {
            return this.extent || !getExclude().accept(candidate);
        }

        /* (non-Javadoc)
         */
        @Override
        protected boolean acceptAll (
        ){
            return this.extent || getExclude().isEmpty();
        }

        /* (non-Javadoc)
         */
        @Override
        protected void removeInternal(
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
        public int size() {
            if(this.plain && !this.extent) {
                int providerCount = PersistentContainer_1.this.provided.size();
                if(providerCount != Integer.MAX_VALUE) {
                    return providerCount - getExclude().size();
                }
            }
            return Integer.MAX_VALUE;
        }

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.dataprovider.transport.delegation.Evictable#evict()
         */
        public void evict() {
            ((Evictable)super.list).evict();
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
            return ((Path)this.delegate.next().jdoGetObjectId()).getBase();
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
         * The method accept must be overridden if selector is null.
         * 
         * @param list
         * @param selector
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
        extends MarshallingSequentialList<Object>
        implements Evictable, Serializable
    {

        /**
         *
         */
        private static final long serialVersionUID = 3257285812084159024L;

        /**
         * Constructor 
         *
         * @param feature
         */
        EvictablePersistentObjects(
        ){
            super(PersistentContainer_1.this.sequenceProvider.connection, null);
        }

        /* (non-Javadoc)
         */
        protected List<Object> getDelegate() {
            if(super.list == null) {
                try {
                    if(PersistentContainer_1.this.sequenceProvider.jdoIsNew()){
                        super.list = Collections.emptyList();
                    } else {
                        if(PersistentContainer_1.this.sequenceProvider.persistentValues != null) {
                            super.list = (List)PersistentContainer_1.this.sequenceProvider.persistentValues.get(
                                PersistentContainer_1.this.referenceFilter.getBase()
                            );
                        }
                        if(super.list == null) {
                            super.list = PersistentContainer_1.this.sequenceProvider.channel.find(
                                PersistentContainer_1.this.referenceFilter,
                                null,
                                null,
                                PersistentContainer_1.this.sequenceProvider.connection
                            );
                        }
                    }
                } catch (ServiceException e){
                    throw new RuntimeServiceException(e);
                }
            }
            return super.list;
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

}
