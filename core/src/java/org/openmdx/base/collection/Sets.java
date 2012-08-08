/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Sets.java,v 1.10 2011/04/12 12:20:43 hburger Exp $
 * Description: Sets 
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/04/12 12:20:43 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.base.collection;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openmdx.base.query.Selector;

/**
 * Sets
 */
public class Sets {

    /**
     * Constructor 
     */
    private Sets() {
        // Avoid instantiation
    }
    
    /**
     * Create a concurrent hash set
     * 
     * @param <T>
     * 
     * @return a newly created concurrent hash set
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<T> newConcurrentHashSet(){
        return MapBackedSet.decorate(
            new ConcurrentHashMap<T,Object>(),
            Maps.NULL
        );
    }

    /**
     * Decorate a collection as set
     * 
     * @param collection the collection backing up the set
     * 
     * @return the collection itself if it is a set or a set decorator
     */
    public static <T> Set<T> asSet(
        final Collection<T> collection
    ){
        return
            collection == null || collection instanceof Set<?> ? (Set<T>)collection : 
            new CollectionSet<T>(collection);
    }
    
    /**
     * Decorate a map as set
     * 
     * @param map the map backing up the set
     * 
     * @param map
     * 
     * @return the decorator
     */
    public static <T> Set<T> asSet(
        Map<T,? super String> map
    ){
        return map == null ? null : new MapSet<T>(map);
    }
        
    /**
     * Decorate a collection as set
     * 
     * @param collection the collection backing up the set
     * 
     * @return the collection itself if it is a set or a set decorator
     */
    public static <T> Set<T> asSet(
        final T[] collection
    ){
        return collection == null ? null : new ArraySet<T>(collection);
    }
        

    /**
     * Return a sub-set of a collection decorated as set
     * 
     * @param collection the collection backing up the super-set
     * 
     * @return the filtered set
     */
    public static <T> Set<T> subSet(
        final Collection<T> collection,
        final Selector selector
    ){
        return new SubSet<T>(collection, selector);
    }
    

    //------------------------------------------------------------------------
    // Class CollectionSet
    //------------------------------------------------------------------------
    
    /**
     * Wrap a collection as set
     */
    static class MapSet<T> extends AbstractSet<T> {
        
        /**
         * Constructor 
         *
         * @param map
         */
        MapSet(
            Map<T,? super String> map
        ){
            this.map = map;
        }
            
        private final Map<T,? super String> map;
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<T> iterator() {
            return this.map.keySet().iterator();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.map.size();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#add(java.lang.Object)
         */
        @Override
        public boolean add(T e) {
            return this.map.put(e, "") == null;
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#clear()
         */
        @Override
        public void clear() {
            this.map.clear();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#contains(java.lang.Object)
         */
        @Override
        public boolean contains(Object o) {
            return this.map.containsKey(o);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return this.map.isEmpty();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#remove(java.lang.Object)
         */
        @Override
        public boolean remove(Object o) {
            return this.map.remove(o) != null;
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class CollectionSet
    //------------------------------------------------------------------------
    
    /**
     * Wrap a collection as set
     */
    static class CollectionSet<T> extends AbstractSet<T> {

        /**
         * Constructor 
         *
         * @param collection
         */
        CollectionSet(
            Collection<T> collection
        ){
            this.collection = collection;
        }
        
        private final Collection<T> collection;
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<T> iterator() {
            return this.collection.iterator();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.collection.size();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return this.collection.isEmpty();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#add(java.lang.Object)
         */
        @Override
        public boolean add(T o) {
            return !this.collection.contains(o) && this.collection.add(o);  
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class CollectionSet
    //------------------------------------------------------------------------
    
    /**
     * Wrap a collection as set
     */
    static class ArraySet<T> extends AbstractSet<T> {

        /**
         * Constructor 
         *
         * @param collection
         */
        ArraySet(
            T[] collection
        ){
            this.collection = collection;
        }
        
        protected final T[] collection;
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<T> iterator() {
            return new ArrayIterator();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.collection.length;
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return this.collection.length == 0;
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#add(java.lang.Object)
         */
        @Override
        public boolean add(T o) {
            throw new UnsupportedOperationException(
                "One can't add an element to an array based set"
            );
        }
        
        /**
         * Array Iterator
         */
        class ArrayIterator implements Iterator<T> {
            
            /**
             * 
             */
            private int nextIndex = 0;

            /* (non-Javadoc)
             * @see java.util.Iterator#hasNext()
             */
            public boolean hasNext(
            ) {
                return this.nextIndex < collection.length;
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#next()
             */
            public T next() {
                return collection[this.nextIndex++];
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#remove()
             */
            public void remove() {
                throw new UnsupportedOperationException(
                    "One can't remove an element from an array based set"
                );
            }
            
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class SubSet
    //------------------------------------------------------------------------
    
    /**
     * Wrap a collection as a filtered set
     */
    static class SubSet<T> extends AbstractSet<T> {

        /**
         * Constructor 
         *
         * @param collection the collection backing up the super-set
         * @param selector the sub-set's selector
         */
        SubSet(
            Collection<T> collection,
            Selector selector
        ){
            this.superSet = collection;
            this.selector = selector;
        }
        
        final Collection<T> superSet;

        final Selector selector;
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<T> iterator() {
            return new SubSetIterator();
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            int s = 0;
            for(T t : this.superSet){
                if(this.selector.accept(t)) {
                    s++;
                }
            }
            return s;
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#add(java.lang.Object)
         */
        @Override
        public boolean add(Object o) {
            throw new UnsupportedOperationException(
                "This set can't be modified directly"
            );
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return this.superSet.isEmpty() || !iterator().hasNext();
        }

        /**
         * Sub-Set Iterator
         */
        class SubSetIterator implements Iterator<T> {
            
            /**
             * The super-set iterator
             */
            private Iterator<T> delegate = superSet.iterator();
            
            /**
             * The pre-fetched object
             */
            private T prefetched = null;
            
            /* (non-Javadoc)
             * @see java.util.Iterator#hasNext()
             */
            public boolean hasNext() {
                while(this.prefetched == null && this.delegate.hasNext()) {
                    T candidate = this.delegate.next();
                    if(SubSet.this.selector.accept(candidate)){
                        this.prefetched = candidate;
                    }
                }
                return this.prefetched != null;
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#next()
             */
            public T next() {
                if(hasNext()) {
                    T current = this.prefetched;
                    this.prefetched = null;
                    return current;
                } 
                throw new NoSuchElementException();
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#remove()
             */
            public void remove() {
                throw new UnsupportedOperationException(
                    "This set can't be modified directly"
                );
            }
            
        }
        
    }


}
