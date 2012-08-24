/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: JPA AbstractObject 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2009, OMEX AG, Switzerland
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
package org.w3c.jpa3;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

import javax.jdo.JDOUserException;

import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.SparseArray;

/**
 * Object Relational Mapping
 */
public abstract class AbstractObject implements Serializable {

    /**
     * Constructor 
     */
    protected AbstractObject(
    ){
        super();
    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -1472728633796611343L;

    /**
     * The object's state
     */
    transient ObjectState openmdxjdoState = ObjectState.TRANSIENT;

    /**
     * @serial Opaque value to support optimistic locking
     */
    transient Object openmdxjdoVersion;
    
    /**
     * Retrieve the <code>openmdxjdoIdentity</code> field value
     * 
     * @return the <code>openmdxjdoIdentity</code> field value
     */
    protected abstract String getOpenmdxjdoIdentity();

    /**
     * Replace the <code>openmdxjdoIdentity</code> field value
     * 
     * @param identity the <code>openmdxjdoIdentity</code> field value
     */
    protected abstract void setOpenmdxjdoIdentity(
        String identity
    );
    
    /**
     * Mark the object as dirty
     */
    public void openmdxjdoMakeDirty(
    ){
        if(this.openmdxjdoState == ObjectState.DETACHED_CLEAN) {
            this.openmdxjdoState = ObjectState.DETACHED_DIRTY;
        }
    }
    
    /**
     * Replace a collection's content
     * 
     * @param target
     * @param source
     */
    @SuppressWarnings("unchecked")
    protected final static <E> void openmdxjdoSetCollection(
        Collection<E> target,
        Object source
    ) {
        target.clear();
        if(source != null) {
            for(
                int index = 0, length = Array.getLength(source);
                index < length;
                index++
            ){
                target.add(
                    (E)Array.get(source, index)
                );
            }
        }
    }

    /**
     * Replace a sparse arrays's content
     * 
     * @param target
     * @param source
     */
    protected final static <E> void openmdxjdoSetArray(
        SparseArray<E> target,
        Map<Integer, ? extends E> source
    ) {
        target.clear();
        if(source != null) {
            target.putAll(source);
        }
    }
        
    /**
     * Converts a binary large object into a byte array
     * 
     * @param largeObject
     * 
     * @return the large object's byte[] representation
     */
    protected static final byte[] openmdxjdoToArray(
        BinaryLargeObject largeObject
    ){
        try {
            InputStream source = largeObject.getContent();
            Long length = largeObject.getLength();
            ByteArrayOutputStream target = length == null ? 
                new ByteArrayOutputStream() : 
                new ByteArrayOutputStream((int) length.longValue());
            for(
                int value = source.read();
                value >= 0;
                value = source.read()
            ) {
                target.write(value);
            }
            return target.toByteArray();
        } catch (IOException exception) {
            throw new JDOUserException(
                "Could not retrieve the large object's content",
                exception
            );
        }
    }
    
    /**
     * Converts a character large object into a character array
     * 
     * @param largeObject
     * 
     * @return the large object's char[] representation
     */
    protected static final char[] openmdxjdoToArray(
        CharacterLargeObject largeObject
    ){
        try {
            Reader source = largeObject.getContent();
            Long length = largeObject.getLength();
            CharArrayWriter target = length == null ?
                new CharArrayWriter() :
                new CharArrayWriter((int) length.longValue());
            for(
                int value = source.read();
                value >= 0;
                value = source.read()
            ) {
                target.write(value);
            }
            return target.toCharArray();
        } catch (IOException exception) {
            throw new JDOUserException(
                "Could not retrieve the large object's content",
                exception
            );
        }
    }

    
    //------------------------------------------------------------------------
    // Implements <code>Serializable</code>
    //------------------------------------------------------------------------
    
    /**
     * Serialize transient field
     * 
     * @param out serialization stream
     * 
     * @throws IOException
     */
    private void writeObject(
        ObjectOutputStream out
    ) throws IOException {
        out.writeObject(this.openmdxjdoState); 
        out.writeObject(this.openmdxjdoVersion); 
    }

    /**
     * De-serialize transient field
     * 
     * @param in de-serialization stream
     * 
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(
        java.io.ObjectInputStream in
    ) throws IOException, ClassNotFoundException {
        this.openmdxjdoState = (ObjectState) in.readObject();
        this.openmdxjdoVersion = in.readObject();
    }


    //------------------------------------------------------------------------
    // Class SlicedSet
    //------------------------------------------------------------------------
    
    /**
     * Sliced Set
     */
    protected abstract static class SlicedSet<E,S> extends AbstractSet<E> {

        /**
         * Constructor 
         *
         * @param slices
         */
        protected SlicedSet(
           SortedMap<Integer,S> slices 
        ) {
            this.slices = slices;
        }

        /**
         * The slice holder
         */
        private final SortedMap<Integer,S> slices;
        

        /**
         * Create a slice
         * 
         * @param index the index of the slice
         * 
         * @return a new slice
         */
        protected abstract S newSlice(
            int index
        );
        
        /**
         * Retrieve the value at the given index
         * 
         * @param slice the source
         * 
         * @return the requested value
         */
        protected abstract E getValue(
            S slice
        );
        
        /**
         * Propagate a value to the given slice
         * 
         * @param slice the target
         * @param value the value
         */
        protected abstract void setValue(
            S slice,
            E value
        );    
        
        /* (non-Javadoc)
         * @see java.util.List#size()
         */
        @Override
        public abstract int size();

        /**
         * Propagate the size
         * 
         * @param size
         */
        protected abstract void setSize(
            int size
        );

        /**
         * Retrieve the requested slice
         * 
         * @param index
         * 
         * @return a (maybe newly created) slice
         */
        S getSlice(
            int index
        ){
            Integer key = Integer.valueOf(index);
            S slice = this.slices.get(key);
            if(slice == null) {
                this.slices.put(
                    key,
                    slice = newSlice(index)
                );
            }
            return slice;
        }
        
        /**
         * Assert that the value is not <code>null</code>
         * 
         * @param value the value to be tested
         * 
         * @throws <code>nullPointerException</code> if the value is <code>null</code>
         */
        protected static void validateValue(
            Object value
        ){
            if(value == null) {
                throw new NullPointerException("The value must not be null");
            }
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<E> iterator() {
            
            return new Iterator<E>() {
    
                int next = 0;
                int current = -1;

            //  @Override
                public final boolean hasNext() {
                    return this.next < size();
                }
    
            //  @Override
                public final E next() {
                    if(this.next >= size()) {
                        throw new NoSuchElementException();
                    } else {
                        return getValue(getSlice(current = next++));
                    }
                }
    
            //  @Override
                public final void remove() {
                    if(this.current < 0) {
                        throw new IllegalStateException();
                    } else {
                        int size = size() - 1;
                        E e = null;
                        for(
                            int i = size;
                            i >= this.current;
                            i--
                        ) {
                            S s = getSlice(i);
                            E o = getValue(s);
                            setValue(s, e);
                            e = o;
                        }
                        this.next = this.current;
                        this.current = -1;
                        setSize(size);
                    }
                }
                
            };
            
        }
    
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#add(java.lang.Object)
         */
        @Override
        public final boolean add(E o) {
            validateValue(o);
            boolean add = !contains(o);
            int size = size();
            if(add) {
                setValue(getSlice(size), o);
                setSize(++size);
            }
            return add;
        }
    
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#clear()
         */
        @Override
        public final void clear() {
            for(
                int i = size() - 1;
                i >= 0;
                i--
            ){
                setValue(getSlice(i), null);
            }
            setSize(0);
        }
    
    }
    
    
    //------------------------------------------------------------------------
    // Class SlicedList
    //------------------------------------------------------------------------
    
    /**
     * Sliced List
     */
    protected abstract static class SlicedList<E,S> extends AbstractList<E> {

        /**
         * Constructor 
         *
         * @param slices
         */
        protected SlicedList(
           SortedMap<Integer,S> slices 
        ) {
            this.slices = slices;
        }

        /**
         * The slice holder
         */
        private final SortedMap<Integer,S> slices;
        
        /**
         * Create a slice
         * 
         * @param index the index of the slice
         * 
         * @return a new slice
         */
        protected abstract S newSlice(
            int index
        );
                
        /**
         * Retrieve the value at the given index
         * 
         * @param slice the source
         * 
         * @return the requested value
         */
        protected abstract E getValue(
            S slice
        );
        
        /**
         * Propagate a value to the given slice
         * 
         * @param slice the target
         * @param value the value
         */
        protected abstract void setValue(
            S slice,
            E value
        );    
        
        /* (non-Javadoc)
         * @see java.util.List#size()
         */
        @Override
        public abstract int size();

        /**
         * Propagate the size
         * 
         * @param size
         */
        protected abstract void setSize(
            int size
        );

        /**
         * Retrieve the requested slice
         * 
         * @param index
         * 
         * @return a (maybe newly created) slice
         */
        private S getSlice(
            int index
        ){
            Integer key = Integer.valueOf(index);
            S slice = this.slices.get(key);
            if(slice == null) {
                this.slices.put(
                    key,
                    slice = newSlice(index)
                );
            }
            return slice;
        }
        
        /**
         * Assert that the value is not <code>null</code>
         * 
         * @param value the value to be tested
         * 
         * @throws <code>nullPointerException</code> if the value is <code>null</code>
         */
        protected static void validateValue(
            Object value
        ){
            if(value == null) {
                throw new NullPointerException("The value must not be null");
            }
        }

        /**
         * Validate the given index
         * 
         * @param index
         * @param size
         * @param extendable <code>true</code> if size is a valid index value
         */
        protected void validateIndex(
            int index,
            int size,
            boolean extendable
        ){
            if(extendable ? index > size : index >= size) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
            }
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractList#get(int)
         */
        @Override
        public final E get(int index) {
            int size = size();
            validateIndex(index, size, false);
            return getValue(getSlice(index));
        }
    
        /* (non-Javadoc)
         * @see java.util.AbstractList#add(int, java.lang.Object)
         */
        @Override
        public final void add(int index, E element) {
            int size = size();
            validateValue(element);
            validateIndex(index, size, true);
            E e = element;
            for(
                int i = index;
                i <= size;
                i++
            ) {
                S s = getSlice(i);
                E o = getValue(s);
                setValue(s, e);
                e = o;
            }
            setSize(size + 1);
        }
    
        /* (non-Javadoc)
         * @see java.util.AbstractList#set(int, java.lang.Object)
         */
        @Override
        public final E set(int index, E element) {
            int size = size();
            validateValue(element);
            validateIndex(index, size, false);
            S s = getSlice(index);
            E o = getValue(s);
            setValue(s, element);
            setSize(size); // make dirty
            return o;
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractList#removeRange(int, int)
         */
        @Override
        protected final void removeRange(int fromIndex, int toIndex) {
            if(fromIndex < toIndex){
                int oldSize = size();
                validateIndex(fromIndex, oldSize, false);
                validateIndex(toIndex, oldSize, true);
                int count = toIndex - fromIndex;
                int newSize = oldSize - count;
                for(
                   int i = fromIndex;
                   i < oldSize;
                   i++
                ){
                    setValue(
                        getSlice(i),
                        i < newSize ? getValue(getSlice(i + count)) : null
                    );
                }
                setSize(newSize);
            }
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class SlicedMap
    //------------------------------------------------------------------------

    /**
     * Sliced Map
     */
    protected abstract static class SlicedMap<E,S> implements SortedMap<Integer,E> {

        /**
         * Constructor 
         *
         * @param slices
         */
        protected SlicedMap(
           SortedMap<Integer,S> slices 
        ) {
            this.slices = slices;
        }

        /**
         * The slice holder
         */
        protected final SortedMap<Integer,S> slices;
        
        /**
         * Create a slice
         * 
         * @param index the index of the slice
         * 
         * @return a new slice
         */
        protected abstract S newSlice(
            int index
        );
                
        /**
         * Retrieve the value at the given index
         * 
         * @param slice the source
         * 
         * @return the requested value
         */
        protected abstract E getValue(
            S slice
        );
        
        /**
         * Propagate a value to the given slice
         * 
         * @param slice the target
         * @param value the value
         */
        protected abstract void setValue(
            S slice,
            E value
        );    
        
        /* (non-Javadoc)
         * @see java.util.List#size()
         */
    //  @Override
        public abstract int size();

        /**
         * Propagate the size
         * 
         * @param size
         */
        protected abstract void setSize(
            int size
        );

        /**
         * Decrease the size
         */
        protected void decrease(){
            setSize(size() - 1);
        }

        /**
         * Increase the size
         */
        protected void increase(){
            setSize(size() - 1);
        }

        /**
         * Make dirty
         */
        protected void touch(){
            setSize(size());
        }
        
       /* (non-Javadoc)
         * @see java.util.SortedMap#comparator()
         */
    //  @Override
        public Comparator<? super Integer> comparator() {
            return null;
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#entrySet()
         */
    //  @Override
        public Set<java.util.Map.Entry<Integer, E>> entrySet() {
            return new AbstractSet<Entry<Integer,E>>() {

                @Override
                public Iterator<Map.Entry<Integer, E>> iterator() {
                    return new Iterator<Entry<Integer,E>>() {

                        private final Iterator<Map.Entry<Integer, S>> delegate = SlicedMap.this.slices.entrySet().iterator();
                        Map.Entry<Integer, S> current = null;
                        private Map.Entry<Integer, S> prefetched = null;
                        
                    //  @Override
                        public boolean hasNext() {
                            while(this.prefetched == null && this.delegate.hasNext()) {
                                Map.Entry<Integer, S> candidate = this.delegate.next();
                                if(getValue(candidate.getValue()) != null) {
                                    this.prefetched = candidate;
                                }
                            }
                            return this.prefetched != null;
                        }
                        
                    //  @Override
                        public Map.Entry<Integer, E> next() {
                            if(hasNext()) {
                                this.current = this.prefetched;
                                this.prefetched = null;
                                return new Map.Entry<Integer, E>(){

                                //  @Override
                                    public Integer getKey() {
                                        return current.getKey();
                                    }

                                //  @Override
                                    public E getValue() {
                                        return SlicedMap.this.getValue(current.getValue());
                                    }

                                //  @Override
                                    public E setValue(E value) {
                                        if(value == null) {
                                            remove();
                                            return null;
                                        } else if(current == null) {
                                            throw new IllegalStateException("No current element");
                                        } else {
                                            E old = getValue(); 
                                            SlicedMap.this.setValue(current.getValue(), value);
                                            current = null;
                                            return old;
                                        }
                                    }
                                    
                                };
                            } else {
                                throw new NoSuchElementException();
                            }
                        }

                    //  @Override
                        public void remove() {
                            if(this.current == null) {
                                throw new IllegalStateException("No current element");
                            } else {
                                SlicedMap.this.setValue(this.current.getValue(), null);
                                decrease();
                                this.current = null;
                            }
                        }
                        
                    };
                }

                @Override
                public int size() {
                    return SlicedMap.this.size();
                }
                
            };
          
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#firstKey()
         */
    //  @Override
        public Integer firstKey() {
            Iterator<Map.Entry<Integer, E>> entries = entrySet().iterator();
            if(entries.hasNext()) {
                return entries.next().getKey();
            } else {
                throw new NoSuchElementException("The map is empty");
            }
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#headMap(java.lang.Object)
         */
    //  @Override
        public SortedMap<Integer, E> headMap(Integer toKey) {
            final SlicedMap<E,S> delegate = this;
            
            return new SlicedMap<E,S>(
                delegate.slices.headMap(toKey)
            ){
                @Override
                protected E getValue(S slice) {
                    return delegate.getValue(slice);
                }

                @Override
                protected S newSlice(int index) {
                    return delegate.newSlice(index);
                }

                @Override
                protected void setSize(int size) {
                    throw new UnsupportedOperationException("The view is unmodifiable at the moement");
                }

                @Override
                protected void setValue(S slice, E value) {
                    throw new UnsupportedOperationException("The view is unmodifiable at the moement");
                }

                @Override
                public int size() {
                    //
                    // a view should not store its size
                    //
                    int count = 0;
                    for(@SuppressWarnings("unused") Map.Entry<Integer,E> e: entrySet()) {
                        count++;
                    }
                    return count;
                }
                
            };
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#keySet()
         */
    //  @Override
        public Set<Integer> keySet() {
            final Set<Map.Entry<Integer, E>> entries = entrySet();
            return new AbstractSet<Integer>(){

                @Override
                public Iterator<Integer> iterator() {
                    final Iterator<Map.Entry<Integer, E>> delegate = entries.iterator();
                    return new Iterator<Integer>(){

                    //  @Override
                        public boolean hasNext() {
                            return delegate.hasNext();
                        }

                    //  @Override
                        public Integer next() {
                            return delegate.next().getKey();
                        }

                    //  @Override
                        public void remove() {
                            delegate.remove();
                        }
                        
                    };
                }

                @Override
                public int size() {
                    return entries.size();
                }
                
            };
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#lastKey()
         */
    //  @Override
        public Integer lastKey() {
            Iterator<Map.Entry<Integer, E>> entries = entrySet().iterator();
            Map.Entry<Integer, E> cursor = null;
            while(entries.hasNext()) {
                cursor = entries.next();
            }
            if(cursor == null) {
                throw new NoSuchElementException("The map is empty");
            } else {
                return cursor.getKey();
            }
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#subMap(java.lang.Object, java.lang.Object)
         */
    //  @Override
        public SortedMap<Integer, E> subMap(Integer fromKey, Integer toKey) {
            final SlicedMap<E,S> delegate = this;
            
            return new SlicedMap<E,S>(
                delegate.slices.subMap(fromKey, toKey)
            ){

                @Override
                protected E getValue(S slice) {
                    return delegate.getValue(slice);
                }

                @Override
                protected S newSlice(int index) {
                    return delegate.newSlice(index);
                }

                @Override
                protected void setSize(int size) {
                    throw new UnsupportedOperationException("The view is unmodifiable at the moement");
                }

                @Override
                protected void setValue(S slice, E value) {
                    throw new UnsupportedOperationException("The view is unmodifiable at the moement");
                }

                @Override
                public int size() {
                    //
                    // a view should not store its size
                    //
                    int count = 0;
                    for(@SuppressWarnings("unused") Map.Entry<Integer,E> e: entrySet()) {
                        count++;
                    }
                    return count;
                }
                
            };
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#tailMap(java.lang.Object)
         */
    //  @Override
        public SortedMap<Integer, E> tailMap(Integer fromKey) {
            final SlicedMap<E,S> delegate = this;
            
            return new SlicedMap<E,S>(
                delegate.slices.tailMap(fromKey)
            ){

                @Override
                protected E getValue(S slice) {
                    return delegate.getValue(slice);
                }

                @Override
                protected S newSlice(int index) {
                    return delegate.newSlice(index);
                }

                @Override
                protected void setSize(int size) {
                    throw new UnsupportedOperationException("The view is unmodifiable at the moement");
                }

                @Override
                protected void setValue(S slice, E value) {
                    throw new UnsupportedOperationException("The view is unmodifiable at the moement");
                }

                @Override
                public int size() {
                    //
                    // a view should not store its size
                    //
                    int count = 0;
                    for(@SuppressWarnings("unused") Map.Entry<Integer,E> e: entrySet()) {
                        count++;
                    }
                    return count;
                }
                
            };
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#values()
         */
    //  @Override
        public Collection<E> values() {
            final Set<Map.Entry<Integer, E>> entries = entrySet();
            return new AbstractCollection<E>(){

                @Override
                public Iterator<E> iterator() {
                    final Iterator<Map.Entry<Integer, E>> delegate = entries.iterator();
                    return new Iterator<E>(){

                    //  @Override
                        public boolean hasNext() {
                            return delegate.hasNext();
                        }

                    //  @Override
                        public E next() {
                            return delegate.next().getValue();
                        }

                    //  @Override
                        public void remove() {
                            delegate.remove();
                        }
                        
                    };
                }

                @Override
                public int size() {
                    return entries.size();
                }
                
            };
        }

        /* (non-Javadoc)
         * @see java.util.Map#clear()
         */
    //  @Override
        public void clear() {
            for(Map.Entry<Integer,S> e : this.slices.entrySet()) {
                setValue(e.getValue(), null);
            }
            setSize(0);
        }

        /* (non-Javadoc)
         * @see java.util.Map#containsKey(java.lang.Object)
         */
    //  @Override
        public boolean containsKey(Object key) {
            return get(key) != null;
        }

        /* (non-Javadoc)
         * @see java.util.Map#containsValue(java.lang.Object)
         */
    //  @Override
        public boolean containsValue(Object value) {
            if(value != null) {
                for(Map.Entry<Integer,S> e : this.slices.entrySet()) {
                    if(value.equals(getValue(e.getValue()))) {
                        return true;
                    }
                }
            }
            return false;
        }

        /* (non-Javadoc)
         * @see java.util.Map#get(java.lang.Object)
         */
    //  @Override
        public E get(Object key) {
            S slice = this.slices.get(key);
            return slice == null ? null : getValue(slice);
        }

        /* (non-Javadoc)
         * @see java.util.Map#isEmpty()
         */
    //  @Override
        public boolean isEmpty() {
            return size() == 0;
        }

        /* (non-Javadoc)
         * @see java.util.Map#put(java.lang.Object, java.lang.Object)
         */
    //  @Override
        public E put(Integer key, E value) {
            if(value == null) {
                return remove(key);
            } else {
                S slice = this.slices.get(key);
                if(slice == null) {
                    this.slices.put(
                        key,
                        slice = newSlice(key.intValue())
                    );
                    setValue(slice, value);
                    increase();
                    return null;
                } else {
                    E old = getValue(slice);
                    setValue(slice, value);
                    if(old == null) {
                        increase();
                    } else {
                        touch();
                    }
                    return old;
                }
            }
        }

        /* (non-Javadoc)
         * @see java.util.Map#putAll(java.util.Map)
         */
    //  @Override
        public void putAll(Map<? extends Integer, ? extends E> m) {
            for(Map.Entry<? extends Integer, ? extends E> e : m.entrySet()) {
                put(e.getKey(), e.getValue());
            }
        }

        /* (non-Javadoc)
         * @see java.util.Map#remove(java.lang.Object)
         */
    //  @Override
        public E remove(Object key) {
            S slice = this.slices.get(key);
            if(slice == null) {
                return null;
            } else {
                E e = getValue(slice);
                if(e != null) {
                    setValue(slice, null);
                    decrease();
                }
                return e;
            }
        }       
        
    }
    
    
    //------------------------------------------------------------------------
    // Class EmbeddedList
    //------------------------------------------------------------------------

    /**
     * Embedded List
     */
    abstract protected static class EmbeddedList<E> extends AbstractList<E> {
        
        /**
         * 
         * Constructor 
         *
         * @param capacity
         */
        protected EmbeddedList(
            int capacity
        ){
            this.capacity = capacity;
        }

        /**
         * Embedded field accessor
         * 
         * @param index
         * 
         * @return the value of the requested field
         */
        abstract protected E openmdxjdoGet(int index);

        /**
         * Embedded field modifier
         * 
         * @param index
         * @param element the new value of the requested field
         * 
         * @return the former value of the requested field
         */
        abstract protected void openmdxjdoSet(int index, E element);
        
        /* (non-Javadoc)
         * @see java.util.AbstractList#get(int)
         */
        @Override
        public final E get(int index) {
            return openmdxjdoGet(index);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractList#set(int, java.lang.Object)
         */
        @Override
        public final E set(int index, E element) {
            E formerValue = openmdxjdoGet(index);
            openmdxjdoSet(index, element);
            return formerValue;
        }

        /* (non-Javadoc)
         * @see java.util.AbstractList#add(int, java.lang.Object)
         */
        @Override
        public final void add(int index, E element) {
            E e = element; 
            for(
                int i = index;
                element != null;
                i++
            ) {
                e = set(i, e);                
            }
        }

        /* (non-Javadoc)
         * @see java.util.AbstractList#remove(int)
         */
        @Override
        public final E remove(int index) {
            E element = null;
            for(
                int i = size() - 1;
                i >= index;
                i--
            ) {
                element = set(i, element);                
            }
            return element;
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public final int size() {
            for(
               int i = 0;
               i < capacity;
               i++
            ) {
                if(get(i) == null) return i;
            }
            return capacity;
        }
     
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return capacity == 0 || get(0) == null;
        }

        /**
         * The number of embedded fields
         */
        private final int capacity;
        
    }
    
    
    //------------------------------------------------------------------------
    // Class EmbeddedSet
    //------------------------------------------------------------------------

    /**
     * Embedded Set
     */
    abstract protected static class EmbeddedSet<E> extends AbstractSet<E> {
        
        /**
         * Constructor 
         *
         * @param capacity
         */
        protected EmbeddedSet(
            int capacity
        ){
            this.delegate = new Delegate(capacity);
        }

        /**
         * Embedded field accessor
         * 
         * @param index
         * 
         * @return the value of the requested field
         */
        abstract protected E openmdxjdoGet(int index);

        /**
         * Embedded field modifier
         * 
         * @param index
         * @param element the new value of the requested field
         * 
         * @return the former value of the requested field
         */
        abstract protected void openmdxjdoSet(int index, E element);
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#add(java.lang.Object)
         */
        @Override
        public final boolean add(E e) {
            return 
                !this.delegate.contains(e) &&
                this.delegate.add(e);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public final Iterator<E> iterator(
        ) {
            return this.delegate.iterator();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public final int size() {
            return this.delegate.size();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return this.delegate.isEmpty();
        }

        /**
         * Delegate
         */
        private class Delegate extends EmbeddedList<E> {

            /**
             * Constructor 
             *
             * @param capacity
             */
            Delegate(int capacity) {
                super(capacity);
            }

            /* (non-Javadoc)
             * @see org.w3c.jdo2.AbstractObject.EmbeddedList#get(int)
             */
            @Override
            protected final E openmdxjdoGet(int index) {
                return EmbeddedSet.this.openmdxjdoGet(index);
            }

            /* (non-Javadoc)
             * @see org.w3c.jdo2.AbstractObject.EmbeddedList#set(int, java.lang.Object)
             */
            @Override
            protected final void openmdxjdoSet(int index, E element) {
                EmbeddedSet.this.openmdxjdoSet(index, element);
            }

        }
        
        /**
         * An embedded list
         */
        private final List<E> delegate;

    }

    
    //------------------------------------------------------------------------
    // Class AbstractStateInterrogation
    //------------------------------------------------------------------------
    
    /**
     * Replicate to avoid javax.jdo dependency. OpenJPA requires ObjectState
     * to be public.
     */
    public static enum ObjectState {
        TRANSIENT,
        DETACHED_CLEAN,
        DETACHED_DIRTY
    }

    
    //------------------------------------------------------------------------
    // Class AbstractStateInterrogation
    //------------------------------------------------------------------------

    /**
     * Abstract State Interrogation
     */
    public static class AbstractStateAccessor {
 
        /**
         * Constructor 
         */
        protected AbstractStateAccessor(){
            super();
        }

        /**
         * This method retrieves the JPA identity, a <code>String</code>
         *  
         * @param pc
         * 
         * @return the JPA identity if pc is an instance of<code>AbstractObject</code>
         */
        public String getObjectId(Object pc) {
            return pc instanceof AbstractObject ? ((AbstractObject)pc).getOpenmdxjdoIdentity() : null;
        }

        /**
         * Retrieve the object's version
         * 
         * @param pc
         * 
         * @return the version if pc is an instance of<code>AbstractObject</code>
         * </ul>
         */
        public Object getVersion(Object pc) {
            return pc instanceof AbstractObject ? ((AbstractObject)pc).openmdxjdoVersion : null; 
        }

        /**
         * A transient or detached object is never deleted
         * 
         * @param pc
         * 
         * @return<ul>
         * <li><code>null</code> if pc is not an instance of<code>AbstractObject</code>
         * <li>FALSE</code> if pc is an instance of<code>AbstractObject</code>
         * </ul>
         */
        public Boolean isDeleted(Object pc) {
            return pc instanceof AbstractObject ? Boolean.FALSE : null; 
        }

        /**
         * Tells whether the object is detached
         * 
         * @param pc
         * 
         * @return<ul>
         * <li><code>null</code> if pc is not an instance of<code>AbstractObject</code>
         * <li><code>FALSE</code> if pc is transient
         * <li><code>TRUE</code> if pc is detached-clean or detached-dirty
         * </ul>
         */
        public Boolean isDetached(Object pc) {
            if(pc instanceof AbstractObject) {
                ObjectState state = ((AbstractObject)pc).openmdxjdoState; 
                return Boolean.valueOf(state == ObjectState.DETACHED_CLEAN || state == ObjectState.DETACHED_DIRTY);
            } else {
                return null;
            }
        }

        /**
         * Tells whether the object is dirty
         * 
         * @param pc
         * 
         * @return<ul>
         * <li><code>null</code> if pc is not an instance of<code>AbstractObject</code>
         * <li><code>FALSE</code> if pc is transient or detached
         * <li><code>TRUE</code> if pc is detached-dirty
         * </ul>
         */
        public Boolean isDirty(Object pc) {
            return pc instanceof AbstractObject ? Boolean.valueOf(((AbstractObject)pc).openmdxjdoState == ObjectState.DETACHED_DIRTY): null; 
        }

        /**
         * A transient or detached object is never new
         * 
         * @param pc
         * 
         * @return<ul>
         * <li><code>null</code> if pc is not an instance of<code>AbstractObject</code>
         * <li>FALSE</code> if pc is an instance of<code>AbstractObject</code>
         * </ul>
         */
        public Boolean isNew(Object pc) {
            return pc instanceof AbstractObject ? Boolean.FALSE : null; 
        }

        /**
         * A transient or detached object is never persistent
         * 
         * @param pc
         * 
         * @return<ul>
         * <li><code>null</code> if pc is not an instance of<code>AbstractObject</code>
         * <li>FALSE</code> if pc is an instance of<code>AbstractObject</code>
         * </ul>
         */
        public Boolean isPersistent(Object pc) {
            return pc instanceof AbstractObject ? Boolean.FALSE : null; 
        }

        /**
         * A transient or detached object is never transactional
         * 
         * @param pc
         * 
         * @return<ul>
         * <li><code>null</code> if pc is not an instance of<code>AbstractObject</code>
         * <li>FALSE</code> if pc is an instance of<code>AbstractObject</code>
         * </ul>
         */
        public Boolean isTransactional(Object pc) {
            return pc instanceof AbstractObject ? Boolean.FALSE : null; 
        }

        /**
         * Mark the instance as dirty
         * 
         * @param pc
         * @param fieldName
         * 
         * @return <code>true</code> if pc is an instance of <code>AbstractObject</code> 
         */
        public boolean makeDirty(Object pc, String fieldName) {
            if(pc instanceof AbstractObject) {
                ((AbstractObject)pc).openmdxjdoMakeDirty();
                return true;
            } else {
                return false;
            }
        }
        
        /**
         * Set a detached or transient object's identity
         *  
         * @param pc
         * @param openmdxjdoIdentity
         * 
         * @throw ClassCastException if pc is not an instance of <code>AbstractObject</code>
         */
        protected static void setObjectId(
            Object pc,
            String openmdxjdoIdentity
        ){
            AbstractObject target = (AbstractObject) pc;
            target.setOpenmdxjdoIdentity(openmdxjdoIdentity);
        }

        /**
         * Set a detached object's state and version
         *  
         * @param pc
         * @param openmdxjdoVersion
         * 
         * @throw ClassCastException if pc is not an instance of <code>AbstractObject</code>
         */
        protected static void setVersion(
            Object pc,
            Object openmdxjdoVersion
        ){
            AbstractObject target = (AbstractObject) pc;
            target.openmdxjdoState = ObjectState.DETACHED_CLEAN;
            target.openmdxjdoVersion = openmdxjdoVersion;
        }

    }

    static {
        try {
            //
            // Delegate to other package to avoid direct jdo.bundle dependency
            //
            Class.forName("org.w3c.spi.StateAccessor");
        } catch (Exception ignore) {
            // Accept missing JDO support
        }

    }

}