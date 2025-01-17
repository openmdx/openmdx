/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Structures 
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
package org.w3c.spi2;

import java.io.Serializable;
import java.lang.reflect.Array;
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
import java.util.ArrayList;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.RefStruct;
#if JAVA_8
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
#else
import jakarta.resource.cci.IndexedRecord;
import jakarta.resource.cci.MappedRecord;
import jakarta.resource.cci.Record;
#endif

import org.omg.mof.spi.Identifier;
import org.omg.mof.spi.Names;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.collection.Maps;
import org.openmdx.base.resource.Records;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.collection.TreeSparseArray;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.kernel.loading.Classes;
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
     * @param persistenceManager 
     * @param structureClass the structure's interface
     * @param values the structure's values
     * 
     * @return an initialized structure proxy instance
     * 
     * @throws IllegalArgumentException
     */
    private static <S> S create(
        PersistenceManager persistenceManager, 
        Class<S> structureClass,
        MetaData metaData, Object... values
    ) {
        return Classes.<S>newProxyInstance(
            new ProxyHandler(persistenceManager, metaData, values), 
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
            null, // persistenceManager
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
            null, // persistenceManager
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
        List<? extends Structures.Member<?>> members
    ) {
        MetaData metaData = MetaData.getInstance(structureClass);
        return create(
            null, // persistenceManager
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
     * @param structureClass
     * @param content
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
            persistenceManager,
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
     * @param structureClass
     * @param content
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
     * Create a structure from a Java Bean
     * 
     * @param persistenceManager the persistence manager is optional
     * if the record does not contain references to persistence capable
     * objects.
     * @param structureClass
     * @param javaBean
     * 
     * @return the corresponding structure proxy
     * 
     * @throws IllegalArgumentException
     */
    public static <S> S fromJavaBean(
        PersistenceManager persistenceManager,
        Class<S> structureClass,
        Object javaBean
    ){
        if(javaBean == null) {
            return null;
        } else {
            MetaData metaData = MetaData.getInstance(structureClass);
            return create(
                persistenceManager,
                structureClass,
                metaData, 
                metaData.fromJavaBean(persistenceManager, javaBean)
            );
        }
    }
    
    /**
     * Save a record into a Java Bean
     * 
     * @param source the record to be saved
     * @param target the target bean
     * 
     * @exception IllegalArgumentException if the record can't be saved into the bean
     * @exception NullPointerException if either the source or the target is {@code null}
     */
    public static <T> T toJavaBean(
        MappedRecord source,
        T target
    ){
        try {
            return MetaData.getInstance(source).toJavaBean(source, target);
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                "Unable to save the record type " + source.getRecordName() + " into the java bean type " + target.getClass().getName(),
                exception
            );
        }
    }
 
    /**
     * Save a structure into a Java Bean
     * 
     * @param source the structure to be saved
     * @param target the target bean
     * 
     * @exception IllegalArgumentException if the record can't be saved into the bean
     * @exception NullPointerException if either the source or the target is {@code null}
     */
    public static <T> T toJavaBean(
        Object source,
        T target
    ){
        return toJavaBean(
            source instanceof MappedRecord ? (MappedRecord)source : toRecord(source, true, true),
            target
        );
    }

    
    /**
     * Retrieve the record type
     * 
     * @param record
     * 
     * @return the record name components
     */
    static List<String> refTypeName(
        Record record
    ){
        String name = record.getRecordName();
        return Arrays.asList(name.split(name.indexOf("::") > 0 ? "::" : ":"));
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
            refTypeName(record),
            record
        );
    }
    
    /**
     * Return a mapped record view of the given structure
     * 
     * @param structure
     * @param mapNullValues if true, fields with value==null are added
     *        to the mapped record. They are not added if mapNullValues 
     *        is false.
     * 
     * @return a mapped record view of the given structure
     * 
     * @exception ClassCastException unless isStructureInstance(structure) evaluates to {@code true}
     */
    public static MappedRecord toRecord(
        Object structure,
        boolean mapNullValues
    ){
        return toRecord(structure, mapNullValues, true);
    }

    /**
     * Return a mapped record view of the given structure
     * 
     * @param structure
     * @param mapNullValues if true, fields with value==null are added to the mapped record. 
     *                      They are not added if mapNullValues is false.
     * @param mofCompliant when {@code true{@code  use {@code "::"} as separator, {@code ":"} otherwise       
     * 
     * @return a mapped record view of the given structure
     * 
     * @exception ClassCastException unless isStructureInstance(structure) evaluates to {@code true}
     */
    public static MappedRecord toRecord(
        Object structure,
        boolean mapNullValues,
        boolean mofCompliant
    ){
        return ((PersistenceAware)structure).openmdxjdoRecord(mapNullValues, mofCompliant);
    }
    
    /**
     * Tests whether an object is a structure instance
     * 
     * @param object
     *            the object to be tested
     * 
     * @return {@code true} if the object is a structure instance
     */
    public static boolean isStructureInstance(Object object) {
        return object != null && Proxy.isProxyClass(object.getClass())
            && Proxy.getInvocationHandler(object) instanceof ProxyHandler;
    }

    static IllegalArgumentException newIllegalArgumentException(
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
    static String cciClassName (
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
         * @param persistenceManager the (optional) PersistenceManager
         * @param values
         * @param nameClass
         */
        ProxyHandler(
            PersistenceManager persistenceManager, 
            MetaData metaData, 
            Object... values
        ) {
            this.metaData = metaData;
            this.values = toNestedArrays(values);
        }

        /**
         * Implements {@code Serializable}
         */
        private static final long serialVersionUID = 8952038983241186303L;

        private final Object[] values;

        private transient PersistenceManager persistenceManager;
        
        private transient Iterable<?>[] collections;

//        private transient Iterable<?>[] collections;
        
        private transient Class<?> structureClass;

        private transient MetaData metaData;

        private transient int hashCode = 0;

        private transient String string = null;
        
        /**
         * Record names are MOF compliant qualified names using "::" as separator
         */
        private transient MappedRecord record = null;

        /**
         * Record names are internal qualified names using ":" as separator
         */
        private transient MappedRecord delegate = null;
        
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
        private static final Integer[] NO_INDICES = new Integer[] {
        };
        
        
        /*
         * (non-Javadoc)
         * 
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
         *      java.lang.reflect.Method, java.lang.Object[])
         */
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
                        return Boolean.TRUE;
                    } else if (!getClass().isInstance(args[0])) {
                        return Boolean.FALSE;
                    } else {
                        ProxyHandler that = (ProxyHandler) Proxy.getInvocationHandler(args[0]);
                        return Boolean.valueOf(Arrays.deepEquals(this.values, that.values));
                    }
                } else if ("toString".equals(methodName)) {
                    if (this.string == null) {
                        this.string = toRecord(true, true).toString();
                    }
                    return this.string;
                } else if ("hashCode".equals(methodName)) {
                    if (this.hashCode == 0) {
                        this.hashCode = Arrays.deepHashCode(this.values);
                    }
                    return Integer.valueOf(this.hashCode);
                }
            } else if (PersistenceAware.class == declaringClass) {
                if ("openmdxjdoValues".equals(methodName)) {
                    Object[] values = new Object[this.metaData.names.length];
                    for(Enum<?> member : this.metaData.names) {
                        values[member.ordinal()] = get(member);
                    }
                    return values;
                } else if ("openmdxjdoRecord".equals(methodName)) {
                    if(this.record == null) {
                        this.record =  toRecord(Boolean.TRUE.equals(args[0]), Boolean.TRUE.equals(args[1]));
                    }
                    return this.record;
                }
            } else if(RefStruct_1_0.class.equals(declaringClass)) {
                if("refDelegate".equals(methodName)) {
                    if(this.delegate == null) {
                        this.delegate = toRecord(true, false); 
                    }
                    return this.delegate;
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
            } else if (RefStruct.class.isAssignableFrom(t) && v instanceof Map<?,?>) {
                return Structures.create(
                    this.persistenceManager,
                    t,
                    (Map<?,?>)v
                );
            } else {
                return v;
            }
        }
        
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
                    if(RefStruct_1_0.class.isAssignableFrom(memberType)) {
                        target[slot] = ((RefStruct_1_0)value).refDelegate();
                    } else if(List.class == memberType || Set.class == memberType) {
                        if (source == target) {
                            target = new Object[source.length];
                            System.arraycopy(source, 0, target, 0, slot);
                        } 
                        Collection<?> c;
                        if(value instanceof Collection<?>) {
                            c = (Collection<?>) value;
                        } else if (value.getClass().isArray()) {
                            c = ArraysExtension.asList(value);
                        } else throw new IllegalArgumentException(
                            "Member '" + this.metaData.names[slot] + 
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
                        if (value instanceof SparseArray<?>) {
                            a = (SparseArray<?>) value;
                        } else if (value instanceof Map<?,?>) {
                            a = toSparseArray((Map<?,?>)value);
                        } else throw new IllegalArgumentException(
                            "Member '" + this.metaData.names[slot] + 
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
                    } else if(Classes.toObjectClass(memberType).isInstance(value)) {
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

        MappedRecord toRecord(
            boolean mapNullValues, 
            boolean mofCompliant
        ) {
            List<Object> values = new ArrayList<Object>();
			List<String> keys = new ArrayList<String>();
			for(Enum<?> field : this.metaData.names) {
			    int slot = field.ordinal();
			    Object v = this.values[slot];
			    Object value = null;
			    Class<?> t = this.metaData.memberTypes[slot];
			    if (Iterable.class.isAssignableFrom(t)) {
			        Object[] i = (Object[]) v;
			        if (SparseArray.class == t) {
			            if(v == null) {
			                value = Records.getRecordFactory().asMappedRecord(
			                    MetaData.toType(t, mofCompliant),
			                    null, // recordShortDescription 
			                    NO_INDICES,
			                    EMPTY_COLLECTION
			                );
			            } else {
			                value = Records.getRecordFactory().asMappedRecord(
			                    MetaData.toType(t, mofCompliant),
			                    null, // recordShortDescription 
			                    (Object[])i[0], // keys
			                    toRecordValues(
			                        (Object[])i[1], 
			                        mapNullValues, mofCompliant
			                    ) // values
			                );
			            }
			        } else {
			            if(v == null) {
			                value = Records.getRecordFactory().asIndexedRecord(
			                    MetaData.toType(t, mofCompliant),
			                    null, // recordShortDescription 
			                    EMPTY_COLLECTION
			                );
			            } else {
			                value = Records.getRecordFactory().asIndexedRecord(
			                    MetaData.toType(t, mofCompliant),
			                    null, // recordShortDescription 
			                    toRecordValues(
			                        i, 
			                        mapNullValues, mofCompliant
			                    )
			                );
			            }
			        }
			    } else {
			        value = v instanceof PersistenceAware ? Structures.toRecord(v, mapNullValues, mofCompliant) : v;
			    }
			    if(mapNullValues || (value != null)) {
			        keys.add(this.metaData.keys[slot]);
			        values.add(value);                        
			    }
			}
			return Records.getRecordFactory().asMappedRecord(
			    mofCompliant ? this.metaData.type : Names.toQualifiedName(this.metaData.type), 
			    null, // recordShortDescription, 
			    keys.toArray(new String[keys.size()]),
			    toRecordValues(values)
			);
        }
        
        private static Object[] toRecordValues(
            List<?> source
        ){
            Object[] target = new Object[source.size()];
            for(int i = 0; i < target.length; i++) {
                target[i] = ReducedJDOHelper.replaceObjectById(source.get(i));
            }
            return target;
        }
        
        private Object[] toRecordValues(
            Object[] structureValues,
            boolean mapNullValues, 
            boolean mofCompliant
        ){
            Object[] recordValues = new Object[structureValues.length];
            int slot = 0;
            for(Object v : structureValues) {
                if(v instanceof PersistenceCapable) {
                    if(ReducedJDOHelper.isPersistent(v)) {
                        recordValues[slot++] = ReducedJDOHelper.getObjectId(v);
                    } else throw new IllegalStateException(
                        "Transient object's can't be used as value of a structure member"
                    );
                } else if (v instanceof PersistenceAware){
                    recordValues[slot++] = Structures.toRecord(v, mapNullValues, mofCompliant);
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
             * Implements {@code Serializable}
             */
            private static final long serialVersionUID = -824731466833965774L;
        
            /**
             * The members
             */
            final Object[] values;
        
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
        
            final int[] indices;
        
            final Object[] values;
        
            final int begin;
        
            final int end;
        
            private transient Set<Map.Entry<Integer, Object>> entries;
        
            /**
             * Implements {@code Serializable}
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
                return Integer.valueOf(this.indices[this.begin]);
            }
        
            /*
             * (non-Javadoc)
             * 
             * @see java.util.SortedMap#headMap(java.lang.Object)
             */
            public SortedMap<Integer, Object> headMap(Integer toKey) {
                int end = this.begin;
                while (end < this.end && this.indices[end] < toKey.intValue()) {
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
                return Integer.valueOf(this.indices[this.end - 1]);
            }
        
            /*
             * (non-Javadoc)
             * 
             * @see java.util.SortedMap#subMap(java.lang.Object, java.lang.Object)
             */
            public SortedMap<Integer, Object> subMap(Integer fromKey, Integer toKey) {
                int begin = this.begin;
                while (begin < this.end && this.indices[begin] < fromKey.intValue()) {
                    begin++;
                }
                int end = this.begin;
                while (end < this.end && this.indices[end] < toKey.intValue()) {
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
                while (begin < this.end && this.indices[begin] < fromKey.intValue()) {
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
        
                        int i = UnmodifiableSortedMap.this.begin;
        
                        public boolean hasNext() {
                            return i < UnmodifiableSortedMap.this.end;
                        }
        
                        public Map.Entry<Integer, Object> next() {
        
                            return new Map.Entry<Integer, Object>() {
        
                                final int position = i++;
        
                                public Integer getKey() {
                                    return Integer.valueOf(UnmodifiableSortedMap.this.indices[this.position]);
                                }
        
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
         * The member's value, which may be {@code null} in case of
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

        @Override
        public String toString(
        ) {
            return this.name.toString();
        }

    }

    
    // ------------------------------------------------------------------------
    // Class MetaData
    // ------------------------------------------------------------------------

    private static class MetaData {

        MetaData(
            Class<?> structureClass
        ) {
            this.type = toType(structureClass, true);
            this.nameClass = getInnerClass(structureClass, MEMBER);
            if (!this.nameClass.isEnum()) throw new IllegalArgumentException(
                "The member class '" + MEMBER + "' of '" + structureClass.getName() + "' is not an enumeration class"
            );
            this.names = (Enum<?>[]) nameClass.getEnumConstants();
            this.accessors = new HashMap<Method, Enum<?>>(names.length);
            this.beanGetter = new HashMap<Enum<?>, String>(names.length);
            this.beanSetter = new HashMap<Enum<?>, String>(names.length);
            this.memberTypes = new Class<?>[names.length];
            this.elementTypes = new Class<?>[names.length];
            this.keys = new String[names.length];
            List<Method> methods = Classes.getOrderedMethods(structureClass);
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
                            this.beanGetter.put(name, candidate);
                            this.beanSetter.put(name, "set" + suffix);
                            this.memberTypes[slot] = returnType;
                            if(Iterable.class.isAssignableFrom(returnType)) {
                                Type elementType = method.getGenericReturnType();
                                if(elementType instanceof ParameterizedType) {
                                    Type[] e = ((ParameterizedType)elementType).getActualTypeArguments();
                                    if(e != null && e.length == 1) {
                                        if(e[0] instanceof Class<?>) {
                                            this.elementTypes[slot] = (Class<?>) e[0];
                                        } else if (e[0] instanceof TypeVariable<?>){
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

        final Map<Method, Enum<?>> accessors;

        final Map<Enum<?>, String> beanGetter;

        final Map<Enum<?>, String> beanSetter;
        
        final Class<?>[] memberTypes;

        final Class<?>[] elementTypes;
        
        final String type;

        private static final String MEMBER = "Member";

        private static final String STEREOTYPE_LIST = "\u00ablist\u00bb";
        
        private static final String STEREOTYPE_SET = "\u00abset\u00bb";
        
        private static final String STEREOTYPE_SPARSEARRAY = "\u00absparsearray\u00bb";                    

        private static final ConcurrentMap<Class<?>, MetaData> forStructureClass = new ConcurrentHashMap<Class<?>, Structures.MetaData>();

        private static final ConcurrentMap<List<String>, MetaData> forTypeName = new ConcurrentHashMap<List<String>, Structures.MetaData>();
        
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

        Object[] toValues(List<? extends Structures.Member<?>> members) {
            Object[] values = new Object[this.names.length];
            for (Structures.Member<?> member : members) {
                values[member.getName().ordinal()] = member.getValue();
            }
            return values;
        }

        private boolean isStructureType(
            Class<?> candidate
        ){
            if(candidate != null) {
                for(Class<?> innerClass : candidate.getClasses()) {
                    if(MEMBER.equals(innerClass.getSimpleName())) {
                        return true;
                    }
                }
                for(Class<?> superClass : candidate.getInterfaces()) {
                    for(Class<?> innerClass : superClass.getClasses()) {
                        if(MEMBER.equals(innerClass.getSimpleName())) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private final boolean isMultivalueType(
            Class<?> type
        ){
            return type.isArray() && type.getComponentType() != Byte.TYPE;
        }
        
        /**
         * Save a mapped record into a Java Bean
         * 
         * @param structure
         * @param javaBean the Java bean to be updated
         * 
         * @throws Exception 
         */
        @SuppressWarnings("unchecked")
        <T> T toJavaBean(
            MappedRecord structure,
            T javaBean
        ) throws Exception{
            Members: for(Enum<?> member : this.names) {
                String name = member.name();
                if(structure.containsKey(name)) {
                    Object source = structure.get(name);
                    String methodName = this.beanSetter.get(member);
                    Object target;
                    Methods: for(Method method : javaBean.getClass().getMethods()) {
                        if(methodName.equals(method.getName())){
                            Class<?>[] arguments = method.getParameterTypes(); 
                            if(arguments.length == 1) try {
                                Class<?> type = arguments[0];
                                if(isMultivalueType(type)) {
                                    if(source == null) {
                                        target = Array.newInstance(type, 0); 
                                    } else {
                                        List<?> values;
                                        if(source instanceof IndexedRecord) {
                                            values = (IndexedRecord)source;
                                        } else if (source instanceof MappedRecord) {
                                            values = new TreeSparseArray<Object>((Map<Integer,?>)source).asList();
                                        } else {
                                            continue Methods;
                                        }
                                        Class<?> componentType = type.getComponentType();
                                        target = Array.newInstance(componentType, values.size());
                                        int i = 0;
                                        for(Object value : values){
                                            Array.set(target, i++, toJavaBeanValue(value, componentType));
                                        }
                                    } 
                                } else {
                                    target = toJavaBeanValue(source, type);
                                }
                                method.invoke(javaBean, target);
                                continue Members;
                            } catch (Exception ignore) {
                                continue Methods;
                            }
                        }
                    }
                    throw new NoSuchMethodException(
                        "No matching " + methodName + " method found to save member " + member + ": " + source
                    );
                }
            }
            return javaBean;
        }
         
        /**
         * Provide the Java bean value and assert its type
         * 
         * @param value
         * @param type
         * 
         * @return the Java bean value
         * @throws Exception
         */
        Object toJavaBeanValue(
            Object value,
            Class<?> type
        ) throws Exception {
            return getObjectType(type).cast(
                value instanceof MappedRecord ? Structures.toJavaBean((MappedRecord)value, type.newInstance()) : value
            );
        }

        /**
         * Retrieve the type of its object equivalent
         * <p>
         * Converts neither {@code byte} nor {@code character}!
         * 
         * @param type or its object equivalent
         * 
         * @return the reflectively assignable component type
         */
        Class<?> getObjectType(
            Class<?> type
        ){
            return
                Boolean.TYPE == type ? Boolean.class :
                Short.TYPE ==  type ? Short.class :
                Integer.TYPE == type ? Integer.class :
                Long.TYPE == type ? Long.class :
                type;
        }
        
        Object[] fromJavaBean(
            PersistenceManager persistenceManager,
            Object javaBean
        ){
            if(javaBean == null) {
                return null;
            } else {
                Object[] values = new Object[this.names.length];
                for(Enum<?> name : this.names) {
                    int slot = name.ordinal();
                    try {
                        Object value = javaBean.getClass().getMethod(this.beanGetter.get(name)).invoke(javaBean);
                        //
                        // Handle Nested Java Beans
                        //
                        if(isStructureType(this.memberTypes[slot])) {
                            values[slot] = Structures.fromJavaBean(persistenceManager, this.memberTypes[slot], value);
                        } else if (isStructureType(this.elementTypes[slot])){
                            Object[] source = (Object[]) value;
                            Object[] target = new Object[source.length];
                            for(int i = 0; i < source.length; i++) {
                                target[i] = Structures.fromJavaBean(persistenceManager, this.elementTypes[slot], source[i]);
                            }
                            values[slot] = target;
                        } else {
                            values[slot] = value;
                        }
                        //
                        // Handle Sparse Arrays
                        //
                        if(this.memberTypes[slot] == SparseArray.class) {
                            Object[] source = (Object[]) values[slot];
                            SparseArray<Object> target = new TreeSparseArray<Object>();
                            for(int i = 0; i < source.length; i++) {
                                if(source[i] != null) {
                                    target.put(Integer.valueOf(i), source[i]);
                                }
                            }
                            values[slot] = target;
                        }
                    } catch (Exception exception) {
                        throw new IllegalArgumentException(
                            "Unable to retrieve field '" + name.name() + "' from " + (javaBean == null ? "null" : javaBean.getClass().getName()) + " Java bean",
                            exception
                        );
                    }
                }
                return values;
            }
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
                    if(value instanceof Collection<?>) {
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
                    if(value instanceof Map<?,?>) {
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
            } else if (value instanceof Map<?,?>) {
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
                return persistenceManager.getObjectById(type, value);
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
         * @param featureName
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
            MetaData metaData = MetaData.forStructureClass.get(structureClass);
            if (metaData == null) { 
                if (!structureClass.isInterface()) throw new IllegalArgumentException(
                    "Class " + structureClass.getName() + " is not an interface"
                );
                metaData = Maps.putUnlessPresent(
                    MetaData.forStructureClass,
                    structureClass, 
                    new MetaData(structureClass)
                );
            }
            return metaData;
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
        static MetaData getInstance(Record structure) {
            List<String> typeName = refTypeName(structure);
            MetaData metaData = MetaData.forTypeName.get(typeName);
            if (metaData == null) try { 
                metaData = Maps.putUnlessPresent(
                    MetaData.forTypeName,
                    typeName, 
                    MetaData.getInstance(Classes.getApplicationClass(cciClassName(typeName)))
                );
            } catch (ClassNotFoundException exception) {
                throw newIllegalArgumentException(
                    "structure",
                    typeName,
                    exception
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
            for(Class<?> superClass : outerClass.getInterfaces()) {
                for (Class<?> innerClass : superClass.getDeclaredClasses()) {
                    if (name.equals(innerClass.getSimpleName())) {
                        return innerClass;
                    }
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
         * @param mofCompliant "::" is used as separator if {@code true} 
         * 
         * @return the class' MOF id
         */
        static String toType(
            Class<?> javaClass, 
            boolean mofCompliant
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
                    type.append(qualifiedName[i]).append(mofCompliant ? "::" : ":");
                }
                return type.append(
                    qualifiedName[qualifiedName.length - 1]
                ).toString();
            }
        }

    }

    
    // ------------------------------------------------------------------------
    // Class PersistenceAware
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
         * @param mapNullValues if true, fields with value==null are added
         *        to the mapped record. They are not added if mapNullValues 
         *        is false.
         * @param mofCompliant when {@code true{@code  use {@code "::"} as separator, {@code ":"} otherwise       
         * 
         * @return JCA record representation of the structure
         */
        MappedRecord openmdxjdoRecord(
            boolean mapNullValues,
            boolean mofCompliant
        );
        
    }

}
