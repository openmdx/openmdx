/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Structures.java,v 1.8 2008/02/28 16:20:02 hburger Exp $
 * Description: Arrays Extension 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/28 16:20:02 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.w3c.spi2;

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
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
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.WeakHashMap;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;
import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;

import org.oasisopen.spi2.DataObjectIdBuilder;
import org.oasisopen.spi2.ObjectId;
import org.oasisopen.spi2.ObjectIdBuilder;
import org.openmdx.base.collection.TreeSparseArray;
import org.openmdx.base.resource.spi.OrderedRecordFactory;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.java.Identifier;
import org.w3c.cci2.SortedMaps;
import org.w3c.cci2.SparseArray;

/**
 * Structures
 */
public class Structures {

    /**
     * Constructor
     */
    protected Structures() {
        // Avoid instantiation
    }

    /**
     * Create a structure proxy instance
     * 
     * @param structureClass
     *            the structure's interface
     * @param values
     *            the structure's values
     * 
     * @return an initialized structure proxy instance
     * 
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    private static <S> S create(
        Class<S> structureClass, 
        MetaData metaData,
        Object... values
    ) {
        return (S) Classes.newProxyInstance(
            new ProxyHandler(metaData, values), 
            structureClass, PersistenceAware.class
        );
    }
    
    /**
     * Create a structure proxy instance
     * 
     * @param structureClass
     *            the structure's interface
     * @param values
     *            the structure's values
     * 
     * @return an initialized structure proxy instance
     * 
     * @throws IllegalArgumentException
     */
    public static <S> S create(
        Class<S> structureClass, 
        Object... values
    ) {
        MetaData metaData = MetaData.getInstance(structureClass);
        return create(
            structureClass,
            metaData,
            values
        );
    }

    /**
     * Create a structure proxy instance
     * 
     * @param structureClass
     *            the structure's interface
     * @param members
     *            the structure's members
     * 
     * @return an initialized structure proxy instance
     * 
     * @throws IllegalArgumentException
     */
    public static <S> S create(
        Class<S> structureClass,
        Structures.Member<?>... members
    ) {
        MetaData metaData = MetaData.getInstance(structureClass);
        return create(
            structureClass,
            metaData,
            metaData.toValues(members)
        );
    }

    /**
     * Create a structure proxy instance
     * 
     * @param structureClass
     *            the structure's interface
     * @param members
     *            the structure's members
     * 
     * @return an initialized structure proxy instance
     * 
     * @throws IllegalArgumentException
     */
    public static <S> S create(
        Class<S> structureClass,
        List<Structures.Member<?>> members
    ) {
        MetaData metaData = MetaData.getInstance(structureClass);
        return create(
            structureClass,
            metaData,
            metaData.toValues(members)
        );
    }
    
    /**
     * Create a structure from a map 
     * 
     * @param persistenceManager the persistence manager is optional
     * if the record does not contain references to persistence capable
     * objects.
     * 
     * @param record
     * 
     * @return the corresponding structure proxy
     * 
     * @throws IllegalArgumentException
     */
    public static <S> S create(
        PersistenceManager persistenceManager,
        Class<S> structureClass,
        Map<?,?> content
    ){
        MetaData metaData = MetaData.getInstance(structureClass);
        return create(
            structureClass,
            metaData,
            metaData.toValues(persistenceManager, content)
        );
    }
    
    /**
     * Create a structure from a map 
     * 
     * @param persistenceManager the persistence manager is optional
     * if the record does not contain references to persistence capable
     * objects.
     * 
     * @param record
     * 
     * @return the corresponding structure proxy
     * 
     * @throws IllegalArgumentException
     */
    public static Object create(
        PersistenceManager persistenceManager,
        List<String> structureClass,
        Map<?,?> content
    ){
        try {
            return create(
                persistenceManager,
                Classes.getApplicationClass(cciClassName(structureClass)),
                content
            );
        } catch (ClassNotFoundException exception) {
            throw newIllegalArgumentException(
                "structure",
                structureClass,
                exception
            );
        }
    }
    
    /**
     * Create a structure from a JCA record 
     * 
     * @param persistenceManager the persistence manager is optional
     * if the record does not contain references to persistence capable
     * objects.
     * 
     * @param record
     * 
     * @return the corresponding structure proxy
     * 
     * @throws IllegalArgumentException
     */
    public static Object create(
        PersistenceManager persistenceManager,
        MappedRecord record
    ){
        return create(
            persistenceManager,
            Arrays.asList(record.getRecordName().split("::")),
            record
        );
    }
    
    /**
     * Return a mapped record view of the given structure
     * 
     * @param structure
     * 
     * @return a mapped record view of the given structure
     * 
     * @exception ClassCastException unless isStructureInstance(structure) evaluates to <code>true</code>
     */
    public static MappedRecord toRecord(
        Object structure
    ){
        return ((PersistenceAware)structure).openmdxjdoRecord();
    }
    
    /**
     * Tests whether an object is a structure instance
     * 
     * @param object
     *            the object to be tested
     * 
     * @return <code>true</code> if the object is a structure instance
     */
    public static boolean isStructureInstance(Object object) {
        return object != null && Proxy.isProxyClass(object.getClass())
            && Proxy.getInvocationHandler(object) instanceof ProxyHandler;
    }

    private static IllegalArgumentException newIllegalArgumentException(
        String type,
        List<String> qualifiedClassName,
        ClassNotFoundException cause
    ){
        StringBuilder message = new StringBuilder(
            "Can't find interface for "
        ).append(
            type
        );
        String separator = " ";
        for(String element : qualifiedClassName) {
            message.append(separator).append(element);
            separator = "::";
        }
        return new IllegalArgumentException(
            message.toString(),
            cause
        );
    }
    
    /**
     * Convert a model class name to the corresponding cci class name
     * 
     * @param modelClass a model class
     * 
     * @return the corresponding cci class name
     */
    private static String cciClassName (
        List<String> modelClass
    ){
        StringBuilder javaClass = new StringBuilder();
        int iLimit = modelClass.size() - 1;
        for(
            int i = 0;
            i < iLimit;
            i++
        ){
            javaClass.append(
                Identifier.PACKAGE_NAME.toIdentifier(modelClass.get(i))
            ).append(
                '.'
            );
        }
        return javaClass.append(
            Names.CCI2_PACKAGE_SUFFIX
        ).append(
            '.'
        ).append(
            Identifier.CLASS_PROXY_NAME.toIdentifier(modelClass.get(iLimit))
        ).toString(
        );
    }
    
    
    //------------------------------------------------------------------------
    // Class ProxyHandler
    //------------------------------------------------------------------------

    /**
     * ProxyHandler
     */
    private static class ProxyHandler
        implements InvocationHandler, Serializable
    {

        /**
         * Constructor
         * 
         * @param nameClass
         * @param values
         */
        ProxyHandler(MetaData metaData, Object... values) {
            this.metaData = metaData;
            this.values = toNestedArrays(values);
        }

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = 8952038983241186303L;

        private final Object[] values;

        private transient Iterable<?>[] collections;

        private transient Class<?> structureClass;

        private transient MetaData metaData;

        private transient int hashCode = 0;

        private transient String string = null;
        
        private transient MappedRecord record = null;

        /**
         * Collection modification exception
         */
        private static final String UNMODIFIABLE = "Structure members are unmodifiable";

        /**
         * 
         */
        private static final Object[] EMPTY_SPARSE_ARRAY = new Object[] {
            new int[] {}, new Object[] {}
        };

        /**
         * 
         */
        private static final Object[] EMPTY_COLLECTION = new Object[] {
        };

        /**
         * 
         */
        private static final int[] NO_INDICES = new int[] {
        };
        
        
        /*
         * (non-Javadoc)
         * 
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
         *      java.lang.reflect.Method, java.lang.Object[])
         */
        @SuppressWarnings("unchecked")
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
            Class<?> declaringClass = method.getDeclaringClass();
            String methodName = method.getName();
            if (this.structureClass == null) {
                this.structureClass = proxy.getClass().getInterfaces()[0];
            }
            if (this.metaData == null) {
                this.metaData = MetaData.getInstance(structureClass);
            }
            if (Object.class == declaringClass) {
                if ("equals".equals(methodName)) {
                    if (this == args[0]) {
                        return true;
                    } else if (!getClass().isInstance(args[0])) {
                        return false;
                    } else {
                        ProxyHandler that = (ProxyHandler) Proxy
                            .getInvocationHandler(args[0]);
                        return Arrays.deepEquals(this.values, that.values);
                    }
                } else if ("toString".equals(methodName)) {
                    if (this.string == null) {
                        this.string = toRecord().toString();
                    }
                    return this.string;
                } else if ("hashCode".equals(methodName)) {
                    if (this.hashCode == 0) {
                        this.hashCode = Arrays.deepHashCode(this.values);
                    }
                    return this.hashCode;
                }
            } else if (PersistenceAware.class == declaringClass) {
                if("openmdxjdoSetPersistenceManager".equals(methodName)) {
                    return null;
                } else if ("openmdxjdoValues".equals(methodName)) {
                    Object[] values = new Object[this.metaData.names.length];
                    for(Enum<?> member : this.metaData.names) {
                        values[member.ordinal()] = get(member);
                    }
                    return values;
                } else if ("openmdxjdoRecord".equals(methodName)) {
                    return toRecord();
                }
            } else {
                Enum<?> member = this.metaData.accessors.get(method);
                if (member != null) return get(member);
            }
            throw new UnsupportedOperationException(declaringClass.getName()
                + '.' + methodName + " is not supported by "
                + ProxyHandler.class.getName());
        }

        /**
         * Retrieve a given field 
         * 
         * @param field
         * 
         * @return
         */
        private Object get(
            Enum<?> field
        ){
            int slot = field.ordinal();
            Object v = this.values[slot];
            Class<?> t = this.metaData.memberTypes[slot];
            if (Iterable.class.isAssignableFrom(t)) {
                if (this.collections == null) {
                    this.collections = new Iterable<?>[this.values.length];
                } else if (this.collections[slot] != null) { 
                    return this.collections[slot]; 
                }
                if (List.class == t) {
                    return this.collections[slot] = v == null ? 
                        Collections.EMPTY_LIST : 
                        new UnmodifiableList((Object[]) v);
                } else if (Set.class == t) {
                    return this.collections[slot] = v == null ? 
                        Collections.EMPTY_SET : 
                       new UnmodifiableSet((Object[]) v);
                } else if (SparseArray.class == t) {
                    Object[] iv = v == null ? 
                        EMPTY_SPARSE_ARRAY : 
                        (Object[]) v;
                    return this.collections[slot] = SortedMaps.asSparseArray(
                        new UnmodifiableSortedMap(
                            (int[]) iv[0],
                            (Object[]) iv[1]
                        )
                    );
                } else throw new IllegalArgumentException(
                    "Members of type '" + t.getName() + "' are not yet supported by " + Structures.class.getName()
                );
            } else {
                return v;
            }
        }
        
        @SuppressWarnings("unchecked")
        Object[] toNestedArrays(
            Object[] source
        ) {
            if (source.length != this.metaData.names.length) throw new IllegalArgumentException(
                "Structure " + this.metaData.type + " has " + this.metaData.names.length + 
                " members, but " + source.length + " have been provided"
            );
            Object[] target = source;
            for (
                int slot = 0; 
                slot < source.length; 
                slot++
            ) {
                Object value = source[slot];
                if(value == null) {
                    target[slot] = null;
                } else {
                    Class<?> memberType = this.metaData.memberTypes[slot];
                    if(List.class == memberType || Set.class == memberType) {
                        if (source == target) {
                            target = new Object[source.length];
                            System.arraycopy(source, 0, target, 0, slot);
                        } 
                        Collection<?> c;
                        if(value instanceof Collection) {
                            c = (Collection<?>) value;
                        } else if (value.getClass().isArray()) {
                            c = ArraysExtension.asList(value);
                        } else throw new IllegalArgumentException(
                            "Mmember '" + this.metaData.names[slot] + 
                            "' requires either a collection or array value"
                        );
                        int s = c.size();
                        if(s == 0) {
                            target[slot] = null;
                        } else {
                            Object[] vs = new Object[s];
                            int j = 0;
                            for(Object e : c) {
                                vs[j++] = toElement(slot, e);
                            }
                            target[slot] = vs;
                        }
                    } else if (SparseArray.class == memberType) {
                        if (source == target) {
                            target = new Object[source.length];
                            System.arraycopy(source, 0, target, 0, slot);
                        }
                        SparseArray<?> a;
                        if (value instanceof SparseArray) {
                            a = (SparseArray<?>) value;
                        } else if (value instanceof Map) {
                            a = toSparseArray((Map<?,?>)value);
                        } else throw new IllegalArgumentException(
                            "Mmember '" + this.metaData.names[slot] + 
                            "' requires a map value with numeric keys"
                        );
                        int s = a.size();
                        if (s == 0) {
                            target[slot] = null;
                        } else {
                            int[] is = new int[s];
                            Object[] vs = new Object[s];
                            int j = 0;
                            for (
                                ListIterator<?> p = a.populationIterator(); 
                                p.hasNext(); 
                                j++
                            ) {
                                is[j] = p.nextIndex();
                                vs[j] = toElement(slot, p.next());
                            }
                            target[slot] = new Object[] {
                                is, vs
                            };
                        }
                    } else if(memberType.isInstance(value)) {
                        target[slot] = value;
                    } else throw new IllegalArgumentException(
                        "Value of class '" + value.getClass().getName() + 
                        "' can't be assigned to member '" + this.metaData.names[slot] + "'" +
                        " expecting a value of type '" + memberType.getName() + "'"
                    );
                }
            }
            return target;
        }
        
        /**
         * 
         */
        private final static SparseArray<?> toSparseArray(
            Map<?,?> source
        ){
            SparseArray<Object> target = new TreeSparseArray<Object>();
            for(Map.Entry<?,?> e : source.entrySet()) {
                Object index = e.getKey();
                if(index instanceof Number) {
                    target.put(
                        index instanceof Integer ? 
                            (Integer)index : 
                            Integer.valueOf(((Number)index).intValue()), 
                        e.getValue()
                    );
                } else throw new IllegalArgumentException(
                    "A sparse array's keys must be numbers: " + index.getClass().getName()
                );
            }
            return target;
        }
        
        private Object toElement(
            int slot,
            Object element
        ){
            Class<?> expected = this.metaData.elementTypes[slot]; 
            if(expected.isInstance(element)) {
                return element;
            } else throw new IllegalArgumentException(
                "Elements of multi-valued member '" + this.metaData.names[slot] + "' must " + ( 
                   element == null ? "not be null" : "be instances of " + 
                   expected.getName() + ": " +  element.getClass().getName()
                )
            );
        }

        MappedRecord toRecord() {
            if(this.record == null) try {
                Object[] values = new Object[this.metaData.names.length];
                for(Enum<?> field : this.metaData.names) {
                    int slot = field.ordinal();
                    Object v = this.values[slot];
                    Class<?> t = this.metaData.memberTypes[slot];
                    if (Iterable.class.isAssignableFrom(t)) {
                        Object[] i = (Object[]) v;
                        if (SparseArray.class == t) {
                            if(v == null) {
                                values[slot] = OrderedRecordFactory.getInstance().asMappedRecord(
                                    MetaData.toType(t),
                                    null, // recordShortDescription 
                                    NO_INDICES,
                                    EMPTY_COLLECTION
                                );
                            } else {
                                values[slot] = OrderedRecordFactory.getInstance().asMappedRecord(
                                    MetaData.toType(t),
                                    null, // recordShortDescription 
                                    i[0], // keys
                                    toRecordValues((Object[])i[1]) // values
                                );
                            }
                        } else {
                            if(v == null) {
                                values[slot] = OrderedRecordFactory.getInstance().asIndexedRecord(
                                    MetaData.toType(t),
                                    null, // recordShortDescription 
                                    EMPTY_COLLECTION
                                );
                            } else {
                                values[slot] = OrderedRecordFactory.getInstance().asIndexedRecord(
                                    MetaData.toType(t),
                                    null, // recordShortDescription 
                                    toRecordValues(i)
                                );
                            }
                        }
                    } else {
                        values[slot] = v;
                    }
                    this.record = OrderedRecordFactory.getInstance().asMappedRecord(
                        this.metaData.type, 
                        null, // recordShortDescription, 
                        this.metaData.keys, 
                        values
                    );
                }
            } catch (ResourceException exception) {
                throw new RuntimeException(
                    "Can not represent the structure '" + this.metaData.type + "' as mapped record",
                    exception
               );
            }
            return this.record;
        }
        
        private Object[] toRecordValues(
            Object[] structureValues
        ){
            Object[] recordValues = new Object[structureValues.length];
            int slot = 0;
            for(Object v : structureValues) {
                if(v instanceof PersistenceCapable) {
                    if(JDOHelper.isPersistent(v)) {
                        recordValues[slot++] = JDOHelper.getObjectId(v);
                    } else throw new IllegalStateException(
                        "Transient object's can't be used as value of a structure member"
                    );
                } else if (v instanceof PersistenceAware){
                    recordValues[slot++] = Structures.toRecord(v);
                } else {
                    recordValues[slot++] = v;
                }
            }
            return recordValues;
        }
        
        /**
         * Unmodifiable Set
         */
        private class UnmodifiableSet
            extends AbstractSet<Object>
            implements Serializable
        {
        
            /**
             * Constructor
             * 
             * @param values
             */
            UnmodifiableSet(Object[] values) {
                this.values = values;
            }
        
            /**
             * Implements <code>Serializable</code>
             */
            private static final long serialVersionUID = -824731466833965774L;
        
            /**
             * The members
             */
            private final Object[] values;
        
            /*
             * (non-Javadoc)
             * 
             * @see java.util.AbstractCollection#iterator()
             */
            @Override
            public Iterator<Object> iterator() {
        
                return new Iterator<Object>() {
        
                    private int i = 0;
        
                    public boolean hasNext() {
                        return i < size();
                    }
        
                    @SuppressWarnings("unchecked")
                    public Object next() {
                        if (hasNext()) {
                            return UnmodifiableSet.this.values[i++];
                        } else {
                            throw new NoSuchElementException();
                        }
                    }
        
                    public void remove() {
                        throw new UnsupportedOperationException(UNMODIFIABLE);
                    }
        
                };
        
            }
        
            /*
             * (non-Javadoc)
             * 
             * @see java.util.AbstractCollection#size()
             */
            @Override
            public final int size() {
                return UnmodifiableSet.this.values.length;
            }
        
        }


        /**
         * Unmodifiable List
         */
        private class UnmodifiableList
            extends AbstractList<Object>
            implements Serializable
        {
        
            /**
             * 
             */
            private static final long serialVersionUID = -3087018618868418163L;
        
            /**
             * Constructor
             * 
             * @param values
             */
            UnmodifiableList(Object[] values) {
                this.values = values;
            }
        
            /**
             * The members
             */
            private final Object[] values;
        
            /*
             * (non-Javadoc)
             * 
             * @see java.util.AbstractCollection#size()
             */
            @Override
            public final int size() {
                return UnmodifiableList.this.values.length;
            }
        
            /* (non-Javadoc)
             * @see java.util.AbstractList#get(int)
             */
            @Override
            public Object get(int index) {
                return this.values[index];
            }
        
        }

        
        /**
         * Unmodifiable Sorted Map
         */
        private class UnmodifiableSortedMap
            extends AbstractMap<Integer, Object>
            implements SortedMap<Integer, Object>, Serializable
        {
        
            /**
             * Constructor
             * 
             * @param indices
             * @param values
             */
            UnmodifiableSortedMap(
                int[] indices, 
                Object[] values
            ) {
                this(indices, values, 0, indices.length);
            }
        
            /**
             * Constructor
             * 
             * @param indices
             * @param values
             * @param begin
             * @param end
             */
            private UnmodifiableSortedMap(
                int[] indices,
                Object[] values,
                int begin,
                int end) {
                this.indices = indices;
                this.values = values;
                this.begin = begin;
                this.end = end;
            }
        
            private final int[] indices;
        
            private final Object[] values;
        
            private final int begin;
        
            private final int end;
        
            private transient Set<Map.Entry<Integer, Object>> entries;
        
            /**
             * Implements <code>Serializable</code>
             */
            private static final long serialVersionUID = 7457080255902185521L;
        
            /*
             * (non-Javadoc)
             * 
             * @see java.util.AbstractMap#entrySet()
             */
            @Override
            public Set<Map.Entry<Integer, Object>> entrySet() {
                return this.entries == null ? this.entries = new EntrySet()
                    : this.entries;
            }
        
            /*
             * (non-Javadoc)
             * 
             * @see java.util.SortedMap#comparator()
             */
            public Comparator<? super Integer> comparator() {
                return null;
            }
        
            /*
             * (non-Javadoc)
             * 
             * @see java.util.SortedMap#firstKey()
             */
            public Integer firstKey() {
                return this.indices[this.begin];
            }
        
            /*
             * (non-Javadoc)
             * 
             * @see java.util.SortedMap#headMap(java.lang.Object)
             */
            public SortedMap<Integer, Object> headMap(Integer toKey) {
                int end = this.begin;
                while (end < this.end && this.indices[end] < toKey) {
                    end++;
                }
                return new UnmodifiableSortedMap(
                    this.indices,
                    this.values,
                    this.begin,
                    end);
            }
        
            /*
             * (non-Javadoc)
             * 
             * @see java.util.SortedMap#lastKey()
             */
            public Integer lastKey() {
                return this.indices[this.end - 1];
            }
        
            /*
             * (non-Javadoc)
             * 
             * @see java.util.SortedMap#subMap(java.lang.Object, java.lang.Object)
             */
            public SortedMap<Integer, Object> subMap(Integer fromKey, Integer toKey) {
                int begin = this.begin;
                while (begin < this.end && this.indices[begin] < fromKey) {
                    begin++;
                }
                int end = this.begin;
                while (end < this.end && this.indices[end] < toKey) {
                    end++;
                }
                return new UnmodifiableSortedMap(
                    this.indices,
                    this.values,
                    begin,
                    end);
            }
        
            /*
             * (non-Javadoc)
             * 
             * @see java.util.SortedMap#tailMap(java.lang.Object)
             */
            public SortedMap<Integer, Object> tailMap(Integer fromKey) {
                int begin = this.begin;
                while (begin < this.end && this.indices[begin] < fromKey) {
                    begin++;
                }
                return new UnmodifiableSortedMap(
                    this.indices,
                    this.values,
                    begin,
                    this.end);
            }
        
            /**
             * Entry Set
             */
            class EntrySet
                extends AbstractSet<Map.Entry<Integer, Object>>
            {
        
                /*
                 * (non-Javadoc)
                 * 
                 * @see java.util.AbstractCollection#iterator()
                 */
                @Override
                public Iterator<Map.Entry<Integer, Object>> iterator() {
        
                    return new Iterator<Map.Entry<Integer, Object>>() {
        
                        private int i = UnmodifiableSortedMap.this.begin;
        
                        public boolean hasNext() {
                            return i < UnmodifiableSortedMap.this.end;
                        }
        
                        public Map.Entry<Integer, Object> next() {
        
                            return new Map.Entry<Integer, Object>() {
        
                                final int position = i++;
        
                                public Integer getKey() {
                                    return UnmodifiableSortedMap.this.indices[this.position];
                                }
        
                                @SuppressWarnings("unchecked")
                                public Object getValue() {
                                    return UnmodifiableSortedMap.this.values[this.position];
                                }
        
                                public Object setValue(Object value) {
                                    throw new UnsupportedOperationException(
                                        UNMODIFIABLE);
                                }
        
                            };
        
                        }
        
                        public void remove() {
                            throw new UnsupportedOperationException(UNMODIFIABLE);
                        }
        
                    };
        
                }
        
                /*
                 * (non-Javadoc)
                 * 
                 * @see java.util.AbstractCollection#size()
                 */
                @Override
                public int size() {
                    return UnmodifiableSortedMap.this.end
                        - UnmodifiableSortedMap.this.begin;
                }
        
            }
        
        }
        
    }

    
    // ------------------------------------------------------------------------
    // Class Member
    // ------------------------------------------------------------------------

    /**
     * Member
     */
    public final static class Member<T extends Enum<T>> {

        /**
         * Constructor
         * 
         * @param name
         * @param value
         */
        public Member(T name, Object value) {
            this.name = name;
            this.value = value;
        }

        /**
         * The member's name
         */
        private final T name;

        /**
         * The member's value, which may be <code>null</code> in case of
         * optional members.
         */
        private final Object value;

        /**
         * Retrieve the member's name
         * 
         * @return the name
         */
        public final T getName() {
            return name;
        }

        /**
         * Retrieve the member's value
         * 
         * @return the value
         */
        public final Object getValue() {
            return value;
        }

    }

    
    // ------------------------------------------------------------------------
    // Class MetaData
    // ------------------------------------------------------------------------

    private static class MetaData {

        MetaData(
            Class<?> structureClass
        ) {
            this.type = toType(structureClass);
            this.nameClass = getInnerClass(structureClass, MEMBER);
            if (!this.nameClass.isEnum()) throw new IllegalArgumentException(
                "The member class '" + MEMBER + "' of '" + structureClass.getName() + "' is not an enumeration class"
            );
            this.names = (Enum<?>[]) nameClass.getEnumConstants();
            this.accessors = new HashMap<Method, Enum<?>>(names.length);
            this.memberTypes = new Class<?>[names.length];
            this.elementTypes = new Class<?>[names.length];
            this.keys = new String[names.length];
            Method[] methods = structureClass.getDeclaredMethods();
            Names: for (Enum<?> name : names) {
                int slot = name.ordinal();
                String memberName = this.keys[slot] = name.toString();
                String suffix = Character.toUpperCase(memberName.charAt(0)) + memberName.substring(1);
                for (Method method : methods) {
                    if (method.getParameterTypes().length == 0) {
                        String candidate = method.getName();
                        Class<?> returnType = method.getReturnType();
                        if (
                            candidate.equals("get" + suffix) || (
                                (boolean.class == returnType || Boolean.class == returnType) && 
                                (candidate.equals(memberName) || candidate.equals("is" + suffix))
                            )
                        ) {
                            this.accessors.put(method, name);
                            this.memberTypes[slot] = returnType;
                            if(Iterable.class.isAssignableFrom(returnType)) {
                                Type elementType = method.getGenericReturnType();
                                if(elementType instanceof ParameterizedType) {
                                    Type[] e = ((ParameterizedType)elementType).getActualTypeArguments();
                                    if(e != null && e.length == 1) {
                                        if(e[0] instanceof Class<?>) {
                                            this.elementTypes[slot] = (Class<?>) e[0];
                                        } else if (e[0] instanceof TypeVariable){
                                            TypeVariable<?> t = (TypeVariable<?>)e[0];
                                            this.elementTypes[slot] = (Class<?>)t.getBounds()[0];
                                        } else if (e[0] instanceof GenericArrayType){
                                            GenericArrayType t = (GenericArrayType)e[0];
                                            if(byte.class == t.getGenericComponentType()) {
                                                this.elementTypes[slot] = byte[].class;
                                            } else throw new IllegalArgumentException(
                                                "Element type '" + e[0] + "' not yet supported by " + Structures.class.getName() 
                                            );
                                        } else throw new IllegalArgumentException(
                                            "Expecting exactly one type parameter for iterable member '" + name + "'"
                                        );
                                    } else throw new IllegalArgumentException(
                                        "Expecting exactly one type parameter for iterable member '" + name + "'"
                                    );
                                } else throw new IllegalArgumentException(
                                    "Missing type parameter for iterable member '" + name + "'"
                                );
                            }
                            continue Names;
                        }
                    }
                }
            }
        }

        private final Class<?> nameClass;

        final Enum<?>[] names;
        
        final String[] keys;

        private final Map<Method, Enum<?>> accessors;

        final Class<?>[] memberTypes;

        final Class<?>[] elementTypes;
        
        final String type;

        private static final String MEMBER = "Member";

        private static final String STEREOTYPE_LIST = "\u00ablist\u00bb";
        
        private static final String STEREOTYPE_SET = "\u00abset\u00bb";
        
        private static final String STEREOTYPE_SPARSEARRAY = "\u00absparsearray\u00bb";                    

        private static final Map<Class<?>, MetaData> cache = Collections.synchronizedMap(
            new WeakHashMap<Class<?>, MetaData>()
        );

        Object[] toValues(Structures.Member<?>... members) {
            Object[] values = new Object[this.names.length];
            for (Structures.Member<?> member : members) {
                Enum<?> name = member.getName();
                Object value = member.getValue();
                if (!this.nameClass.isInstance(name)) throw new IllegalArgumentException(
                    "Member '" + member + "' is not declared in " + this.nameClass.getName()
                );
                values[name.ordinal()] = value;
            }
            return values;
        }

        Object[] toValues(List<Structures.Member<?>> members) {
            Object[] values = new Object[this.names.length];
            for (Structures.Member<?> member : members) {
                values[member.getName().ordinal()] = member.getValue();
            }
            return values;
        }

        Object[] toValues(
            PersistenceManager persistenceManager,
            Map<?,?> content
        ) {
            Object[] values = new Object[this.names.length];
            for(Map.Entry<?, ?> e : content.entrySet()) {
                int slot = toName(e.getKey()).ordinal();
                Class<?> memberType = this.memberTypes[slot];
                Object value = e.getValue();
                if(value == null) {
                    // leave null in values[slot]
                } else if(List.class == memberType || Set.class == memberType) {
                    if(value instanceof Collection) {
                        values[slot] = toValue(
                            persistenceManager,
                            this.elementTypes[slot],
                            (Collection<?>)value
                        );
                    } else if (value instanceof Object[]) {
                        values[slot] = toValues(
                            persistenceManager,
                            this.elementTypes[slot],
                            (Object[])value
                        );
                    } else throw new IllegalArgumentException(
                        "Multi-valued member '" + e.getKey() + 
                        "' requires a collection or array value: " +
                        value.getClass().getName()
                    );
                } else if (SparseArray.class == memberType) {
                    if(value instanceof Map) {
                        values[slot] = toValue(
                            persistenceManager,
                            this.elementTypes[slot],
                            (Map<?,?>)value
                        );
                    } else throw new IllegalArgumentException(
                        "Sparse array member '" + e.getKey() + 
                        "' requires a map value: " +
                        value.getClass().getName()
                    );
                } else {
                    values[slot] = toValue(
                        persistenceManager,
                        memberType,
                        value
                    );
                }
            }
            return values;
        }
        
        private Object toValue(
            PersistenceManager persistenceManager,
            Class<?> type,
            Object value
        ) {
            if (value == null) {
                return null;
            } else if (value instanceof Map) {
                return create(
                    persistenceManager,
                    type,
                    (Map<?,?>)value
                );
            } else if (type.isInterface() && value instanceof String) {
                String oid = (String) value;
                if(persistenceManager == null) throw new NullPointerException(
                    "A persistence manager is required to create values of type " + type.getName() +
                    ": " + oid
                );
                ObjectIdBuilder objectIdBuilder = (ObjectIdBuilder) persistenceManager.getUserObject(
                    ObjectIdBuilder.class.getName()
                );
                if(objectIdBuilder == null) objectIdBuilder = DataObjectIdBuilder.getInstance();
                ObjectId objectId = objectIdBuilder.toObjectId(oid); 
                List<String> targetClass = objectId.getTargetClass();
                if(targetClass == null) throw new NullPointerException(
                    "The object id's target class can't be determined by the current object id builder: " + oid
                );
                try {
                    return persistenceManager.getObjectById(
                        Classes.getApplicationClass(cciClassName(targetClass)),
                        oid
                    );
                } catch (ClassNotFoundException exception) {
                    throw newIllegalArgumentException(
                        "persistence capable class",
                        targetClass,
                        exception
                    );
                }
            } else {
                return value;
            }
        }
        
        private Object[] toValue(
            PersistenceManager persistenceManager,
            Class<?> elementType,
            Collection<?> source
        ) {
            Object[] target = new Object[source.size()];
            int i = 0;
            for(Object value : source) {
                target[i++] = toValue(persistenceManager, elementType, value);
            }
            return target;
        }

        private Object[] toValues(
            PersistenceManager persistenceManager,
            Class<?> elementType,
            Object[] source
        ) {
            Object[] target = source;
            for(
               int i = 0;
               i < source.length;
               i++
            ){
                Object s = source[i];
                Object t =  toValue(persistenceManager, elementType, s);
                if(t != s) {
                    if(source == target) {
                        target = new Object[source.length];
                        System.arraycopy(source, 0, target, 0, i);
                    }
                    target[i] = t;
                }
            }
            return target;
        }
        
        private Map<?,?> toValue(
            PersistenceManager persistenceManager,
            Class<?> elementType,
            Map<?,?> source
        ) {
            Object[] sourceValues = source.values().toArray();
            Object[] targetValues = toValues(persistenceManager, elementType, sourceValues);
            return sourceValues == targetValues ? source : ArraysExtension.asMap(
                source.keySet().toArray(),
                targetValues
            );
        }
        
        /**
         * Retrieve the member name for a given key
         * 
         * @param name
         * 
         * @return the member name for a given key
         */
        private Enum<?> toName(Object key) {
            for(Enum<?> name : this.names) {
                if(name.toString().equals(key)) return name;
            }
            throw new IllegalArgumentException(
                "No member '" + key + "' is declared in " + this.nameClass.getName()
            );
        }
        
        /**
         * Retrieve the meta data instance
         * 
         * @param structureClass
         * 
         * @return the structure's meta data
         * 
         * @throws IllegalArgumentException if structureClass is not an
         * interface enumerating its members.
         */
        static MetaData getInstance(Class<?> structureClass) {
            MetaData metaData = MetaData.cache.get(structureClass);
            if (metaData == null) { 
                if (!structureClass.isInterface()) throw new IllegalArgumentException(
                    "Class " + structureClass.getName() + " is not an interface"
                );
                MetaData.cache.put(
                    structureClass, 
                    metaData = new MetaData(structureClass)
                );
            }
            return metaData;
        }

        private static Class<?> getInnerClass(
            Class<?> outerClass,
            String name
        ){
            for (Class<?> innerClass : outerClass.getDeclaredClasses()) {
                if (name.equals(innerClass.getSimpleName())) {
                    return innerClass;
                }
            }
            throw new IllegalArgumentException(
                "Missing member class '" + name + "' in '" + outerClass.getName() + "'"
            );
        }
        
        /**
         * Retrieve the corresponding MOF id
         * 
         * @param javaClass
         * 
         * @return the class' MOF id
         */
        static String toType(
            Class<?> javaClass
        ){
            if(Iterable.class.isAssignableFrom(javaClass)) {
                return 
                    List.class == javaClass ? STEREOTYPE_LIST :
                    Set.class == javaClass ? STEREOTYPE_SET :
                    SparseArray.class == javaClass ? STEREOTYPE_SPARSEARRAY :
                    "java:" + javaClass.getName();
            } else {
                String[] qualifiedName = javaClass.getName().split("\\.");
                StringBuilder type = new StringBuilder();
                for (int i = 0, iLimit = qualifiedName.length - 2; i < iLimit; i++) {
                    type.append(qualifiedName[i]).append("::");
                }
                return type.append(
                    qualifiedName[qualifiedName.length - 1]
                ).toString();
            }
        }

    }

    
    // ------------------------------------------------------------------------
    // Class UnmodifiableSet
    // ------------------------------------------------------------------------

    /**
     * Persistence Aware
     */
    public static interface PersistenceAware {

        /**
         * Retrieve the structure's values
         * 
         * @return the structure's values in the members' order
         */
        Object[] openmdxjdoValues(
        );

        /**
         * Retrieve the structure's JCA record representation
         * 
         * @return JCA record representation of the structure
         */
        MappedRecord openmdxjdoRecord(
        );
        
    }

}
