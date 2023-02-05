/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Population Map 
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

package org.openmdx.base.collection;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

import org.w3c.cci2.SparseArray;

/**
 * Population Map
 */
public class PopulationMap<V>
    extends AbstractMap<Integer,V>
    implements SortedMap<Integer,V>
{

    /**
     * Constructor 
     */
    public PopulationMap() {
        this.delegate = null;
    }

    /**
     * Constructor 
     *
     * @param delegate
     */
    public PopulationMap(
        SparseArray<V> delegate
    ) {
        this.delegate = delegate;
    }

    /**
     * The delegate unless getDelegate() is overridden by a sub-class.
     */
    private SparseArray<V> delegate;

    /**
     * The entry set
     */
    private final Set<Map.Entry<Integer, V>> entries = new EntrySet();
    
    /**
     * The delegate
     * 
     * @return
     */
    protected SparseArray<V> getDelegate(){
        return this.delegate;
    }
    
    protected void setDelegate(
        SparseArray<V> delegate
    ){
        this.delegate = delegate;
    }
    
    /* (non-Javadoc)
     * @see java.util.AbstractMap#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<Integer, V>> entrySet() {
        return this.entries;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return entrySet().isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public V put(Integer key, V value) {
        return getDelegate().put(key, value);
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
        return Integer.valueOf(0);
    }

    /* (non-Javadoc)
     * @see java.util.SortedMap#headMap(java.lang.Object)
     */
    public SortedMap<Integer, V> headMap(Integer toKey) {
        return new SubMap(null, toKey);
    }

    /* (non-Javadoc)
     * @see java.util.SortedMap#lastKey()
     */
    public Integer lastKey() {
        return Integer.valueOf((getDelegate().size() - 1));
    }

    /* (non-Javadoc)
     * @see java.util.SortedMap#subMap(java.lang.Object, java.lang.Object)
     */
    public SortedMap<Integer, V> subMap(Integer fromKey, Integer toKey) {
        return new SubMap(fromKey, toKey);
    }

    /* (non-Javadoc)
     * @see java.util.SortedMap#tailMap(java.lang.Object)
     */
    public SortedMap<Integer, V> tailMap(Integer fromKey) {
        return new SubMap(fromKey, null);
    }

    
    //------------------------------------------------------------------------
    // Class EntrySet
    //------------------------------------------------------------------------

    /**
     * 
     */
    class EntrySet extends AbstractSet<Map.Entry<Integer,V>> {

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<java.util.Map.Entry<Integer, V>> iterator() {
            return new EntryIterator();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return getDelegate().size();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#clear()
         */
        @Override
        public void clear() {
            getDelegate().clear();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return getDelegate().isEmpty();
        }
       
    }

    
    //------------------------------------------------------------------------
    // Class EntryIterator
    //------------------------------------------------------------------------

    /**
     * EntryIterator
     */
    class EntryIterator implements Iterator<Map.Entry<Integer,V>> {

        /**
         * Constructor 
         */
        EntryIterator(
        ){
            this.to = null;
        }

        /**
         * Constructor 
         */
        EntryIterator(
            Integer from,
            Integer to
        ){
            if(from != null) {
                while(this.delegate.hasNext() && this.delegate.nextIndex() < from.intValue()) {
                    this.delegate.next();
                }
            }
            this.to = to;
        }

        /**
         * 
         */
        private final Integer to;
        
        /**
         * 
         */
        final ListIterator<V> delegate = getDelegate().populationIterator();
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return this.delegate.hasNext() && (
                to == null || to.intValue() > this.delegate.nextIndex()
            );
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public java.util.Map.Entry<Integer, V> next() {
            final int key = this.delegate.nextIndex();
            if(to != null && key >= to.intValue()) {
                throw new NoSuchElementException();
            }
            final V value = this.delegate.next();
            return new Map.Entry<Integer, V>(){

                public Integer getKey() {
                    return Integer.valueOf(key);
                }

                public V getValue() {
                    return value;
                }

                public V setValue(V value) {
                    EntryIterator.this.delegate.set(value);
                    return value;
                }
                
            };
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            this.delegate.remove();
        }

    }

    
    //------------------------------------------------------------------------
    // Class SubMap
    //------------------------------------------------------------------------
    
    /**
     * 
     */
    class SubMap
        extends AbstractMap<Integer,V>
        implements SortedMap<Integer,V>
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
            this.from = from != null && from.intValue() != 0 ? from : null;
            this.to = to;
        }

        /**
         * 
         */
        final Integer from;
        
        /**
         * 
         */
        final Integer to;

        /**
         * The entry set
         */
        private final Set<Map.Entry<Integer, V>> entries = new SubSet();

        /* (non-Javadoc)
         * @see java.util.AbstractMap#entrySet()
         */
        @Override
        public Set<java.util.Map.Entry<Integer, V>> entrySet() {
            return this.entries;
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return entrySet().isEmpty();
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
            for(
                ListIterator<?> i = getDelegate().populationIterator();
                i.hasNext();
                i.next()
            ){
                int n = i.nextIndex();
                if(inRange(n)) {
                    return Integer.valueOf(n);
                }
            }
            throw new NoSuchElementException(
                "The head-, sub- or tail-map is empty"
            );    
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#headMap(java.lang.Object)
         */
        public SortedMap<Integer, V> headMap(Integer toKey) {
            assertRange(toKey);
            return PopulationMap.this.headMap(toKey);
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#lastKey()
         */
        public Integer lastKey() {
            int l = -1;
            for(
                ListIterator<?> i = getDelegate().populationIterator();
                i.hasNext();
                i.next()
            ){
                int n = i.nextIndex();
                if(inRange(n)) {
                    l = n;
                } else {
                    break; 
                }
            }
            if(l < 0) {
                throw new NoSuchElementException(
                    "The head-, sub- or tail-map is empty"
                );    
            } else {
                return Integer.valueOf(l);
            }
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#subMap(java.lang.Object, java.lang.Object)
         */
        public SortedMap<Integer, V> subMap(Integer fromKey, Integer toKey) {
            assertRange(fromKey);
            assertRange(toKey);
            return PopulationMap.this.subMap(fromKey, toKey);
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#tailMap(java.lang.Object)
         */
        public SortedMap<Integer, V> tailMap(Integer fromKey) {
            assertRange(fromKey);
            return PopulationMap.this.tailMap(fromKey);
        }

        /**
         * Tests whether the key is in the given sub-range
         * 
         * @param key
         * 
         * @return {@code true} if the key is in the given sub-range
         */
        boolean inRange(
            int key
        ){
            return (
                this.from == null || this.from.intValue() <= key
            ) && (
                this.to == null || this.to.intValue() > key
            );
        }

        /**
         * Validate a given key
         * 
         * @param key
         * 
         * @throws NullPointerException if the key is {@code null}
         * @throws IllegalArgumentException if the key is outside the given range
         */
        private void assertRange(
            Integer key
        ){
            if(!inRange(key.intValue())) {
                throw new IllegalArgumentException(
                    "The given key is outside the head-, sub- or tail-map's range"
                );
            }
        }

        //--------------------------------------------------------------------
        // Class SubSet
        //--------------------------------------------------------------------
        
        /**
         * Sub-Set
         */
        class SubSet extends AbstractSet<Map.Entry<Integer,V>> {

            /* (non-Javadoc)
             * @see java.util.AbstractCollection#iterator()
             */
            @Override
            public Iterator<java.util.Map.Entry<Integer, V>> iterator() {
                return new EntryIterator(from, to);
            }

            /* (non-Javadoc)
             * @see java.util.AbstractCollection#size()
             */
            @Override
            public int size() {
                int s = 0;
                ListIterator<?> i = getDelegate().populationIterator();
                if(from != null) {
                    while(i.hasNext() && i.nextIndex() < from.intValue()) {
                        i.next();
                    }
                }
                for(;i.hasNext();i.next()) {
                    if(to != null && i.nextIndex() >= to.intValue()){
                        return s;
                    }
                    s++;
                }
                return s;
            }

            /* (non-Javadoc)
             * @see java.util.AbstractCollection#isEmpty()
             */
            @Override
            public boolean isEmpty() {
                ListIterator<?> i = getDelegate().populationIterator();
                if(from != null) {
                    while(i.hasNext() && i.nextIndex() < from.intValue()) {
                        i.next();
                    }
                }
                return i.hasNext() && i.nextIndex() < to.intValue();
            }
            
        }

    }

}
