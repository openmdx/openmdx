/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractObject.java,v 1.4 2009/03/03 15:21:42 hburger Exp $
 * Description: Object Relational Mapping 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/03 15:21:42 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2007, OMEX AG, Switzerland
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
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;
import javax.jdo.Query;

import org.oasisopen.jdo2.Identifiable;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.SortedMaps;
import org.w3c.cci2.SparseArray;

/**
 * Object Relational Mapping
 */
@SuppressWarnings("serial")
public abstract class AbstractObject
    extends Identifiable
    implements Serializable 
{

    /**
     * Retrieve the slices.
     * <p>
     * This method must be overridden by the first subclass with multivalued attributes.
     * 
     * @return Returns the openmdxjdo$arrays.
     */
    protected <E extends Slice> SortedMap<Integer,E> openmdxjdoGetSlices(
    ){
    	return null;
    }

    /**
     * Create a new map instance
     * 
     * @return a map instance
     */
    protected <T> Map<String,T> openmdxjdoNewMap(
    ){
        return new HashMap<String,T>();
    }
    
    /**
     * Create a new array slice instance
     * <p>
     * This method must be overridden by all subclasses with multivalued attributes.
     * 
     * @return a new array slice instance
     */
    protected Slice openmdxjdoNewSlice(
        int index
    ){
        return null;
    }
    
    /**
     * Create a set view for a given field
     * @param field the collection's field id
     * 
     * @return the requested <code>Set</code>
     * 
     * @exception java.lang.IllegalArgumentException
     *            in case of an invalid field id
     */
    protected <E> Set<E> openmdxjdoGetSet(int field) {
        return new SlicedSet<E>(field);
    }

    /**
     * Create a list view for a given field
     * @param field the <code>List</code>'s field id
     * 
     * @return the requested <code>List</code>
     * 
     * @exception java.lang.IllegalArgumentException
     *            in case of an invalid field id
     */
    protected <E> List<E> openmdxjdoGetList(int field) {
        return new SlicedList<E>(field);
    }

    /**
     * Replace the values of a given Collection.
     * 
     * @param <E>
     * @param collection the Collection to be modified
     * @param array the value to be added
     */
    @SuppressWarnings("unchecked")
    protected final <E> void openmdxjdoSetCollection(
        Collection<E> collection,
        Object array
    ) {
        collection.clear();
        for(
            int index = 0, length = Array.getLength(array);
            index < length;
            index++
        ){
            collection.add(
                (E)Array.get(array, index)
            );
        }
    }

    /**
     * Replace the values of a given SparseArray.
     * 
     * @param <E>
     * @param array the SparseArray to be modified
     * @param value the value(s) to be added
     */
    protected final <E> void openmdxjdoSetArray(
        SparseArray<E> array,
        Map<Integer, ? extends E> value
    ) {
        array.clear();
        array.putAll(value);
    }
        
    /**
     * Create a sparse array view for a given field
     * @param field the collection's field id
     * 
     * @return the requested sparse array
     * 
     * @exception java.lang.IllegalArgumentException
     *            in case of an invalid field id
     */
    protected final <E> SparseArray<E> openmdxjdoGetSparseArray(
        int field
    ) {
        return SortedMaps.asSparseArray(
            new SlicedMap<E>(field)
        );
    }

    /**
     * Retrieve a multivalued attribute's cardinality
     * 
     * @param field the attributes's field id
     * 
     * @return the cardinality of the corresponding attribute
     * 
     * @exception java.lang.IllegalArgumentException
     *            in case of an invalid field id
     */
    protected int openmdxjdoGetSize(int field) {
        throw new IllegalArgumentException(
            "There is no multivalued attribute with index " + field
        );
    }

    /**
     * Set a multivalued attribute's cardinality
     * 
     * @param field the attributes's field id
     * @param size the multivalued attribute's cardinality
     * 
     * @exception java.lang.IllegalArgumentException
     *            in case of an invalid field id
     */
    protected void openmdxjdoSetSize(int field, int size) {
        throw new IllegalArgumentException(
            "There is no multivalued attribute with index " + field
        );
    }

    /**
     * Retrieve a value of a multivalued attribute
     * 
     * @param field
     * @param index
     * 
     * @return the requested value 
     * 
     * @throws NullPointerException if the calling class does not support 
     * multivalued attributes, i.e. if <code>openmdxjdoGetSlices()</code> 
     * has not been overridden properly.
     */
    final Object openmdxjdoGetValue(int field, int index) {
        Slice slice = openmdxjdoGetSlices().get(index);
        return slice == null ? null : slice.getValue(field);
    }

    /**
     * Set a value of a multivalued attribute
     * 
     * @param field
     * @param index
     * @param value
     * 
     * @return the previous value
     * 
     * @throws NullPointerException if the calling class does not support 
     * multivalued attributes, i.e. if <code>openmdxjdoGetSlices()</code> or 
     * <code>openmdxjdoNewSlice()</code> has not been overridden properly.
     */
    final Object openmdxjdoSetValue(
		int field, 
		int index, 
		Object value
   ) {
        SortedMap<Integer, Slice> slices = openmdxjdoGetSlices(); 
        Slice slice = slices.get(index);
        if(value != null && slice == null) {
            slices.put(
                index, 
                slice = openmdxjdoNewSlice(index)
            );
        }
        if(slice == null) {
            return null;
        } else {
            Object old = slice.getValue(field);
            slice.setValue(field, value);
            return old;
        }
    }

    /**
     * Navigate through a containment reference
     * 
     * @param objectClass
     * @param queryName
     * @param mixinParent
     * @param parent
     * 
     * @return the set of children
     */
    protected static final <T> Collection<T> openmdxjdoGetObjectsByParent(
        Class<T> objectClass, String queryName,
        boolean mixinParent,  Object parent
    ){
        return new ResultSet<T>(
            JDOHelper.getPersistenceManager(parent).newNamedQuery(objectClass, queryName),
            mixinParent ? openmdxjdoGetObjectId(parent) : parent
        );
    }

    /**
     * Validate elements to be added
     * 
     * @param element
     * 
     * @exception NullPointerException if the element is 
     * <code>null</code>
     */
    protected static final void openmdxjdoValidateElement(Object element){
        if(element == null) throw new NullPointerException(
            "Elements may not be null"
        );
    }
    
    /**
     * Convert a binary large object to a byte array
     * 
     * @param largeObject
     * 
     * @return the large object's content
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
     * Convert a character large object to a character array
     * 
     * @param largeObject
     * 
     * @return the large object's content
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
    
    /**
     * Creates a class name
     * 
     * @param components the class' components
     * 
     * @return an unmodifiable list containing the components
     */
    protected static final List<String> openmdxjdoClassName(
        String... components
    ){
        return Collections.unmodifiableList(Arrays.asList(components));
    }
    
    /**
     * Slice base class
     */
    @SuppressWarnings("serial")
    public static class Slice
        implements Serializable
    {

        /**
         * Constructor 
         */
        protected Slice(
        ) {
        }

        /**
         * Retrieve the number of fields in a given class and their superclasses.
         * 
         * @return the number of fields in a given class and their superclasses
         */
        protected static int openmdxjdoFieldCount(){
            return 0;
        }
        
        /**
         * Retrieve a slice field value 
         * 
         * @param field the field's id
         * 
         * @return the slice field value
         * 
         * @exception java.lang.IllegalArgumentException
         *            in case of an invalid field id
         */
        protected Object getValue(
            int field
        ){
            throw new IllegalArgumentException(
                "There is no field with index " + field
            );
        }

        /**
         * Set a slice field
         * 
         * @param field the slice field's id
         * @param value the new value
         * 
         * @exception java.lang.IllegalArgumentException
         *            in case of an invalid field id
         */
        protected void setValue(
            int field, 
            Object value
        ){
            throw new IllegalArgumentException(
                "There is no field with index " + field
            );
        }

        /**
         * Abstract Slice ObjectId Class
         */
        public abstract static class AbstractObjectId
            implements Serializable 
        {

            /**
             * Constructor 
             */
            protected AbstractObjectId(
            ){
            }

            protected static int openmdxjdoIndex(
                String sliceIdentity
            ){
                int separator = sliceIdentity.lastIndexOf(FRAGEMENT_DELIMITER);
                return Integer.parseInt(
                    sliceIdentity.substring(separator + 1)
                );
            }

            protected static String openmdxjdoIdentity(
                Class<?> sliceClass,
                String sliceIdentity
            ){
//              int separator = sliceIdentity.lastIndexOf(FRAGEMENT_DELIMITER);
                throw new UnsupportedOperationException("TODO: JDO-->JPA identity mapping");
//                return new StringIdentity(
//                    sliceClass,
//                    sliceIdentity.substring(0, separator)
//                );
            }
            
            protected abstract int openmdxjdoIndex();

            protected abstract String openmdxjdoIdentity();
            
            /* (non-Javadoc)
             * @see java.lang.Object#toString()
             */
            @Override
            public final String toString() {
                return new StringBuilder(
                    openmdxjdoIdentity().toString()
                ).append(
                    FRAGEMENT_DELIMITER
                ).append(
                    openmdxjdoIndex()
                ).toString();
            }

            /* (non-Javadoc)
             * @see java.lang.Object#hashCode()
             */
            @Override
            public final int hashCode() {
                return openmdxjdoIdentity().hashCode() ^ openmdxjdoIndex();
            }

            /* (non-Javadoc)
             * @see java.lang.Object#equals(java.lang.Object)
             */
            @Override
            public final boolean equals(
                Object other
            ){
                if(other.getClass() == this.getClass()) {
                    AbstractObjectId that = (AbstractObjectId)other;
                    return 
                        this.openmdxjdoIndex() == that.openmdxjdoIndex() && 
                        this.openmdxjdoIdentity().equals(that.openmdxjdoIdentity());
                } else {
                    return false;
                }
            }
            
            /**
             * Separates the main object's JDO object id from the index index
             */
            private static final char FRAGEMENT_DELIMITER = '#';
            
        }
        
    }
    
    /**
     * EmbeddedList
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
     
        /**
         * The number of embedded fields
         */
        private final int capacity;
        
    }
    
    /**
     * EmbeddedSet
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

    /**
     * SlicedList
     */
    private class SlicedSet<E> extends AbstractSet<E> {
    
        /**
         * Constructor 
         * @param field
         */
        SlicedSet(
            final int field
        ) {
            this.field = field;
        }
    
        /**
         * The <code>Set</code>'s field id
         */
        final int field;
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<E> iterator() {
            
            return new Iterator<E>() {
    
                int next = 0;
                int current = -1;
                int size = AbstractObject.this.openmdxjdoGetSize(field);
                
                public final boolean hasNext() {
                    return next < size;
                }
    
                @SuppressWarnings("unchecked")
                public final E next() {
                    E element = (E) AbstractObject.this.openmdxjdoGetValue(field, current = next++);
                    if(element == null) throw new NoSuchElementException();
                    return element;
                }
    
                public final void remove() {
                    if(current < 0) throw new IllegalStateException();
                    Object o = null;
                    for(
                        int i = size - 1;
                        i >= current;
                        i--
                    ) {
                        o = AbstractObject.this.openmdxjdoSetValue(field, i, o);
                    }
                    next = current;
                    current = -1;
                    AbstractObject.this.openmdxjdoSetSize(field, --size);
                }
                
            };
            
        }
    
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#add(java.lang.Object)
         */
        @Override
        public final boolean add(E o) {
            AbstractObject.openmdxjdoValidateElement(o);
            boolean add = contains(o);
            int size = AbstractObject.this.openmdxjdoGetSize(field);
            if(add) {
                AbstractObject.this.openmdxjdoSetValue(field, size, o);
            }
            AbstractObject.this.openmdxjdoSetSize(field, ++size);
            return add;
        }
    
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#clear()
         */
        @Override
        public final void clear() {
            for(
                int i = AbstractObject.this.openmdxjdoGetSize(this.field);
                i >= 0;
                i--
            ){
                AbstractObject.this.openmdxjdoSetValue(this.field, i, null);
            }
            AbstractObject.this.openmdxjdoSetSize(this.field, 0);
        }
    
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public final int size() {
            return AbstractObject.this.openmdxjdoGetSize(this.field);
        }
        
    }

    /**
     * SlicedList
     */
    private class SlicedList<E> extends AbstractList<E> {
    
        /**
         * Constructor 
         * @param field
         */
        SlicedList(
            final int field
        ) {
            this.field = field;
        }
    
        /**
         * The <code>List</code>'s field id
         */
        private final int field;
        
        /* (non-Javadoc)
         * @see java.util.AbstractList#get(int)
         */
        @SuppressWarnings("unchecked")
        @Override
        public final E get(int index) {
            return (E) AbstractObject.this.openmdxjdoGetValue(this.field, index);
        }
    
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public final int size() {
            return AbstractObject.this.openmdxjdoGetSize(this.field);
        }
    
        /* (non-Javadoc)
         * @see java.util.AbstractList#add(int, java.lang.Object)
         */
        @Override
        public final void add(int index, E element) {
            AbstractObject.openmdxjdoValidateElement(element);
            int size = validateIndexAndReturnSize (index, true); 
            Object current = element;
            for(
                int i = index;
                current != null;
                i++
            ) {
                current = AbstractObject.this.openmdxjdoSetValue(this.field, i++, current);
            }
            AbstractObject.this.openmdxjdoSetSize(this.field, size + 1);
        }
    
        /* (non-Javadoc)
         * @see java.util.AbstractList#remove(int)
         */
        @SuppressWarnings("unchecked")
        @Override
        public final E remove(int index) {
            int size = validateIndexAndReturnSize (index, false);
            Object element = null;
            for(
                int i = size - 1;
                i >= index;
                i--
            ) {
                element = AbstractObject.this.openmdxjdoSetValue(this.field, i, element);
            }
            AbstractObject.this.openmdxjdoSetSize(this.field, size - 1);
            return (E) element;
        }
    
        /* (non-Javadoc)
         * @see java.util.AbstractList#set(int, java.lang.Object)
         */
        @SuppressWarnings("unchecked")
        @Override
        public final E set(int index, E element) {
            AbstractObject.openmdxjdoValidateElement(element);
            validateIndexAndReturnSize (index, false);
            return (E) AbstractObject.this.openmdxjdoSetValue(this.field, index, element);
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractList#removeRange(int, int)
         */
        @Override
        protected final void removeRange(int fromIndex, int toIndex) {
            int count = toIndex - fromIndex;
            int oldSize = AbstractObject.this.openmdxjdoGetSize(field);
            int newSize = oldSize - count;
            for(
               int i = fromIndex;
               i < oldSize;
               i++
            ){
                AbstractObject.this.openmdxjdoSetValue(
                    this.field,
                    i,
                    i < newSize ? AbstractObject.this.openmdxjdoGetValue(this.field, i + count) : null
                );
            }
            AbstractObject.this.openmdxjdoSetSize(this.field, newSize);
        }
        
        /**
         * Validate the index 
         * 
         * @param index
         * @param sizeIsAcceptable
         * 
         * @throws IndexOutOfBoundsException if the index is less than 0
         * or greater than maximum.
         */
        private final int validateIndexAndReturnSize(
            final int index,
            final boolean sizeIsAcceptable
        ){
            int size = AbstractObject.this.openmdxjdoGetSize(field);
            int maximum = sizeIsAcceptable ? size : size - 1;
            if (index  < 0 || index > maximum) throw new IndexOutOfBoundsException(
                "Index: "+index+", Size: "+size
            );
            return size;
        }
    
    }

    /**
     * Sliced Map
     */
    private class SlicedMap<E> 
        extends AbstractMap<Integer,E> 
        implements SortedMap<Integer,E> 
    {
    
        /**
         * Constructor 
         * @param field
         */
        SlicedMap(
            final int field
        ) {
            this(
                AbstractObject.this.openmdxjdoGetSlices(), 
                field, 
                false
            );
        }
    
        /**
         * Constructor 
         * @param delegate
         * @param field
         * @param view
         */
        private SlicedMap(
            final SortedMap<Integer, Slice> delegate,
            final int field,
            final boolean view
        ) {
            this.delegate = delegate;
            this.field = field;
            this.view = view;
        }
        
        /**
         * The <code>Map</code>'s field id
         */
        final int field;
    
        /**
         * The slices backing this map.
         */
        final SortedMap<Integer, Slice> delegate;
        
        /**
         * <code>view</code> is <code>true</code> for headMaps,
         * tailMaps and subMaps.
         */
        private final boolean view;
        
        /* (non-Javadoc)
         * @see java.util.AbstractMap#size()
         */
        @Override
        public final int size() {
            if(this.view) {
                int size = 0;
                for(Slice m : this.delegate.values()) {
                    if(m.getValue(this.field) != null) {
                        size++;
                    }
                }
                return size;
            } else {
                return AbstractObject.this.openmdxjdoGetSize(this.field);
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.AbstractMap#entrySet()
         */
        @Override
        public Set<java.util.Map.Entry<Integer, E>> entrySet() {
            
            return new AbstractSet<java.util.Map.Entry<Integer, E>>() {
    
                
                @Override
                public Iterator<Map.Entry<Integer, E>> iterator() {
                    return new Iterator<Map.Entry<Integer, E>>() {
    
                        int size = SlicedMap.this.size();
                        int step = 0;
                        Map.Entry<Integer, Slice> current = null;
                        
                        Iterator<Map.Entry<Integer, Slice>> delegate = SlicedMap.this.delegate.entrySet().iterator();
                        
                        public final boolean hasNext() {
                            return this.step < this.size;
                        }
    
                        private final Slice nextSlice() {
                            Slice slice;
                            do {
                                slice = (this.current = this.delegate.next()).getValue();
                            } while (slice.getValue(field) == null);
                            step++;
                            return slice;
                        }
                        
                        @SuppressWarnings("unchecked")
                        public final Map.Entry<Integer, E> next() {
                            final Slice slice = nextSlice();
                            return new Map.Entry<Integer, E>() {
    
                                public final Integer getKey() {
                                    return current.getKey();
                                }
    
                                public final E getValue() {
                                    return (E) slice.getValue(field);
                                }
    
                                public final E setValue(E value) {
                                    AbstractObject.openmdxjdoValidateElement(value);
                                    Object old = getValue();
                                    slice.setValue(field, value);
                                    return (E) old;
                                }
                                
                            };
                        }
    
                        public final void remove() {
                            if(current == null) throw new IllegalStateException();
                            current.getValue().setValue(field, null);
                            current = null;
                        }
                        
                    };
                }
    
                @Override
                public final int size() {
                    return SlicedMap.this.size();
                }                    
                
            };
        }
    
        /* (non-Javadoc)
         * @see java.util.SortedMap#comparator()
         */
        public final Comparator<? super Integer> comparator() {
            return this.delegate.comparator();
        }
    
        /* (non-Javadoc)
         * @see java.util.SortedMap#firstKey()
         */
        public final Integer firstKey() {
            for(Map.Entry<Integer, Slice> entry : this.delegate.entrySet()) {
                if(entry.getValue().getValue(field) != null) {
                    return entry.getKey();
                }
            }
            throw new NoSuchElementException();
        }
    
        /* (non-Javadoc)
         * @see java.util.SortedMap#headMap(java.lang.Object)
         */
        public final SortedMap<Integer, E> headMap(Integer toKey) {
            return new SlicedMap<E>(
                this.delegate.headMap(toKey),
                this.field,
                true
            );
        }
    
        /* (non-Javadoc)
         * @see java.util.SortedMap#lastKey()
         */
        public final Integer lastKey() {
            Integer key = null;
            for(Map.Entry<Integer, Slice> entry : this.delegate.entrySet()) {
                if(entry.getValue().getValue(field) != null) {
                    key = entry.getKey();
                }
            }
            if(key == null) throw new NoSuchElementException();
            return key;
        }
    
        /* (non-Javadoc)
         * @see java.util.SortedMap#subMap(java.lang.Object, java.lang.Object)
         */
        public final SortedMap<Integer, E> subMap(Integer fromKey, Integer toKey) {
            return new SlicedMap<E>(
                this.delegate.subMap(fromKey, toKey),
                this.field,
                true
            );
        }
    
        /* (non-Javadoc)
         * @see java.util.SortedMap#tailMap(java.lang.Object)
         */
        public final SortedMap<Integer, E> tailMap(Integer fromKey) {
            return new SlicedMap<E>(
                this.delegate.tailMap(fromKey),
                this.field,
                true
            );
        }
    
        /* (non-Javadoc)
         * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
         */
        @SuppressWarnings("unchecked")
        @Override
        public final E put(Integer key, E value) {
            AbstractObject.openmdxjdoValidateElement(value); // a SparseArray invokes remove(Integer) for null values
            E oldValue = (E) AbstractObject.this.openmdxjdoSetValue(field, key, value);
            if(view && oldValue == null) {
                AbstractObject.this.openmdxjdoSetSize(
                    this.field,
                    AbstractObject.this.openmdxjdoGetSize(this.field) + 1
                );
            }
            return oldValue;
        }
    
        /* (non-Javadoc)
         * @see java.util.AbstractMap#remove(java.lang.Object)
         */
        @SuppressWarnings("unchecked")
        @Override
        public final E remove(Object key) {
            if(key instanceof Integer) {
                E oldValue = (E) AbstractObject.this.openmdxjdoSetValue(field, (Integer)key, null);
                if(view && oldValue != null) {
                    AbstractObject.this.openmdxjdoSetSize(
                        this.field,
                        AbstractObject.this.openmdxjdoGetSize(this.field) - 1
                    );
                }
                return oldValue;
            } else {
                return null;
            }
        }
                
    }
    
    /**
     * Wrap a collection as set
     */
    private static class ResultSet<E> 
        extends AbstractCollection<E> 
        implements Closeable
    {

        /**
         * Constructor 
         *
         * @param query
         * @param parent
         */
        ResultSet(
            Query query,
            Object parent
        ){
            this.query = query;
            this.parent = parent;
        }

        /**
         * 
         */
        private Query query;

        /**
         * 
         */
        private Object parent;

        /**
         * 
         */
        private Collection<E> delegate = null;

        /**
         * 
         * @return the query result list
         */
        @SuppressWarnings("unchecked")
        private final Collection<E> getDelegate(
        ){
            if(this.delegate == null) {
                if(this.query == null) throw new IllegalStateException(
                    "This result set has already been closed"
                );
                this.delegate = (Collection<E>) this.query.execute(this.parent);
            } 
            return this.delegate;
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<E> iterator() {
            return this.getDelegate().iterator();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.getDelegate().size();
        }

        /* (non-Javadoc)
         * @see java.io.Closeable#close()
         */
        public void close(
        ) throws IOException {
            if(this.delegate != null) {
                this.query.close(this.delegate);
                this.delegate = null;
            }
            this.query = null;
            this.parent = null;
        }        
        
    }
    
}