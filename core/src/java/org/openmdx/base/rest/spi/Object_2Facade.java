/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Object Facade
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2013, OMEX AG, Switzerland
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
package org.openmdx.base.rest.spi;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractList;
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
import java.util.UUID;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.base.collection.TreeSparseArray;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.SparseArray;

/**
 * Object Facade
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class Object_2Facade {

    /**
     * Constructor 
     *
     * @param delegate
     */
    private Object_2Facade(
        MappedRecord delegate
    ) throws ResourceException {
        if(!isDelegate(delegate)) {
            throw BasicException.initHolder(
                new ResourceException(
                    "The delegate has the wrong type",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("expected", ObjectRecord.NAME),
                        new BasicException.Parameter("actual", delegate.getRecordName())
                    )
                )
            );
        }
        this.delegate = (ObjectRecord) delegate;
    }

    /**
     * Constructor 
     */
    private Object_2Facade(
    ) throws ResourceException {
        this.delegate = (ObjectRecord) Records.getRecordFactory().createMappedRecord(ObjectRecord.NAME);
    }
    
    /**
     * The query record
     */
    private final ObjectRecord delegate;
    
    /**
     * Create a facade for the given record
     * 
     * @param record
     * 
     * @return the object facade
     * 
     * @throws ResourceException
     */
    public static Object_2Facade newInstance(
        MappedRecord record
    ) throws ResourceException {
        return record == null ? null : new Object_2Facade(record);
    }
    
    /**
     * Create a facade with the given object identifier
     * 
     * @param objectId the object identifier
     * 
     * @return the object facade
     * 
     * @throws ResourceException
     */
    public static Object_2Facade newInstance(
        Path objectId
    ) throws ResourceException {
    	Object_2Facade facade = new Object_2Facade();
    	facade.setPath(objectId);
        return facade;
    }

    /**
     * Create a facade with the given object identifier
     * 
     * @param transactionalObjectId the object identifier
     * 
     * @return the object facade
     * 
     * @throws ResourceException
     */
    public static Object_2Facade newInstance(
        UUID transactionalObjectId
    ) throws ResourceException {
        return newInstance(new Path(transactionalObjectId));
    }
    
    /**
     * Create a facade for the given object identifier and object class
     * 
     * @param objectClass the object class
     * 
     * @return the object facade
     * 
     * @throws ResourceException
     */
    public static Object_2Facade newInstance(
        Path objectId,
        String objectClass
    ) throws ResourceException {
        Object_2Facade object = newInstance(objectId);
        if(objectClass != null){
        	object.setValue(Records.getRecordFactory().createMappedRecord(objectClass));
        }
        return object;
    }
    
    /**
     * Test whether the given record is an object facade delegate
     * 
     * @param record the record to be tested
     * 
     * @return <code>true</code> if the given record is an object facade delegate
     */
    public static boolean isDelegate(
        Record record
    ){
        return ObjectRecord.NAME.equals(record.getRecordName());
    }
    
    /**
     * Retrieve the delegate
     * 
     * @return the delegate
     */
    public ObjectRecord getDelegate(){
        return this.delegate;
    }
    
    /**
     * Retrieve objectId.
     *
     * @return Returns the objectId.
     */
    public final Path getPath(
    ) {
        return delegate.getPath();
    }
    
    public static Path getPath(
        MappedRecord delegate
    ) {
        return delegate instanceof ObjectRecord ? 
            ((ObjectRecord)delegate).getPath() :
            (Path)delegate.get("path");
    }
    
    public String getObjectClass(
    ) {
        return this.delegate.getValue().getRecordName();
    }
    
    public static String getObjectClass(
        MappedRecord delegate
    ) {
        return getValue(delegate).getRecordName();
    }
    
    /**
     * Set objectId.
     * 
     * @param path The objectId to set.
     */
    public final void setPath(Path path) {
        this.delegate.setPath(path);
    }
    
    /**
     * Retrieve the value
     *
     * @return Returns the value.
     */
    public final MappedRecord getValue(
    ) {
        return this.delegate.getValue();
    }

    public static MappedRecord getValue(
        MappedRecord delegate
    ) {
        return delegate instanceof ObjectRecord ? 
            ((ObjectRecord)delegate).getValue() :
            (MappedRecord)delegate.get("value");
    }
    
    public Object getAttributeValues(
        String attributeName
    ) throws ServiceException {
        MappedRecord value = this.getValue();
        Object v = value.get(attributeName);
        return 
            v == null ? null :
            v instanceof SparseArray ? new SparseArrayFacade(value, attributeName) :
            v instanceof Map ? v :     
            new ListFacade(value,attributeName);
    }
    
    public Object attributeValues(
        String attributeName
    ) throws ServiceException {
        return this.attributeValues(
            attributeName,
            Multiplicity.LIST
        );
    }

    public int getSizeOfAttributeValuesAsList(
        String attributeName
    ) throws ServiceException{
        return attributeValuesAsList(attributeName).size();
    }
    
    public void clearAttributeValuesAsList(
        String attributeName
    ) throws ServiceException{
        List<Object> target = attributeValuesAsList(attributeName);
        target.clear();
    }

    public Object getSingletonFromAttributeValuesAsList(
        String attributeName
    ) throws ServiceException{
        return attributeValuesAsList(attributeName).get(0);
        
    }
    
    public void replaceAttributeValuesAsListBySingleton(
        String attributeName, 
        Object value
    ) throws ServiceException{
        List<Object> target = attributeValuesAsList(attributeName);
        target.clear();
        target.add(value);
    }

    public void replaceAttributeValuesAsList(
        String attributeName, 
        Collection<?> values
    ) throws ServiceException{
        List<Object> target = attributeValuesAsList(attributeName);
        target.clear();
        target.addAll(values);
    }

    public boolean attributeValuesAsListContains(
        String attributeName, 
        Object value
    ) throws ServiceException{
        return attributeValuesAsList(attributeName).contains(value);
    }
    
    public void addToAttributeValuesAsList(
        String attributeName, 
        Object value
    ) throws ServiceException{
        List<Object> target = attributeValuesAsList(attributeName);
        target.add(value);
    }

    public void addAllToAttributeValuesAsList(
        String attributeName, 
        Collection<?> values
    ) throws ServiceException{
        List<Object> target = attributeValuesAsList(attributeName);
        target.addAll(values);
    }

    public Object getAttributeValueFromList(
        String attributeName, 
        int index
    ) throws ServiceException{
        return attributeValuesAsList(attributeName).get(index);
    }

    public List<Object> getAttributeValuesAsReadOnlyList(
        String attributeName
    ) throws ServiceException {
        return Collections.unmodifiableList(attributeValuesAsList(attributeName));
    }

    public List<Object> getAttributeValuesAsGuardedList(
        String attributeName
    ) throws ServiceException {
        String objectClass = getObjectClass();
        List<Object> values = attributeValuesAsList(attributeName);
        if(objectClass.startsWith("org:omg:model1:") || objectClass.startsWith("org:openmdx:system:")) {
            return values;
        } else {
            return new GuardedList(objectClass, attributeName, values);
        }
    }
    
    public List<Object> attributeValuesAsList(
        String attributeName
    ) throws ServiceException {
        return (List<Object>)this.attributeValues(
            attributeName,
            Multiplicity.LIST
        );
    }

    public Map<String,Object> attributeValuesAsMap(
        String attributeName
    ) throws ServiceException {
        return (Map<String,Object>)this.attributeValues(
            attributeName,
            Multiplicity.MAP
        );
    }
    
    public Object attributeValues(
        String attributeName,
        Multiplicity multiplicity
    ) throws ServiceException {
        Object value = this.getAttributeValues(attributeName);
        if(value == null) {
            MappedRecord record = this.getValue(); 
            if (Multiplicity.MAP == multiplicity){
                value = record.get(attributeName);
                if(value == null) try {
                    record.put(
                        attributeName,
                        value = Records.getRecordFactory().createMappedRecord(Multiplicity.MAP.toString())
                    );
                } catch (ResourceException exception) {
                    throw new ServiceException(exception);
                }
            } else {
                record.put(
                    attributeName,
                    null
                );
                value = Multiplicity.SPARSEARRAY == multiplicity ? new SparseArrayFacade(
                    record,
                    attributeName
                ) : new ListFacade(
                    record,
                    attributeName
                );
            }
        }
        return value;        
    }

    public Object attributeValue(
        String attributeName
    ) throws ServiceException {
        MappedRecord value = this.getValue();
        Object v = value.get(attributeName);
        if(v == null) {
            return null;
        }
        else if(v instanceof List) {
            return ((List<Object>)v).get(0);
        }
        else if(v instanceof SparseArray) {
            return ((SparseArray<Object>)v).get(Integer.valueOf(0));
        }
        else {
            return v;
        }        
    }
    
    /**
     * Set the value.
     * 
     * @param value The value to set.
     */
    public final void setValue(
        MappedRecord value
    ) {
        this.delegate.setValue(value);
    }

    /**
     * Retrieve the read lock.
     *
     * @return Returns the read lock.
     */
    public final Object getLock(
    ) {
        return getLock(this.delegate);
    }
    
    /**
     * Retrieve the read lock.
     *
     * @return Returns the read lock.
     */
    public static Object getLock(
        MappedRecord delegate
    ) {
        return delegate instanceof ObjectRecord ? 
            ((ObjectRecord)delegate).getLock() :
            delegate.get("lock");
    }
    
    /**
     * Set the read lock.
     * 
     * @param version The lock to set.
     */
    public final void setLock(Object lock) {
        this.delegate.setLock(lock);
    }
    
    /**
     * Retrieve version.
     *
     * @return Returns the version.
     */
    public final Object getVersion(
    ) {
        return getVersion(this.delegate);
    }

    public static Object getVersion(
        MappedRecord delegate
    ) {
        return delegate instanceof ObjectRecord ? 
            ((ObjectRecord)delegate).getVersion() :
            delegate.get("version");
    }
    
    /**
     * Set version.
     * 
     * @param version The version to set.
     */
    public final void setVersion(Object version) {
        this.delegate.setVersion(version);
    }

    private List assertMultiplicity(
        MappedRecord object,
        String featureName,
        List values
    ) throws ServiceException {
        final int size = values.size();
        if(size > 1) {
            String type = object.getRecordName();
            if(!type.startsWith("org:omg:model1:")) {
                if(isSingleValued(type, featureName)) {
                    Object singleton = null;
                    for(Object value : values){
                        if(value == null) {
                            // Ignore
                        } else if(singleton == null) {
                            singleton = value;
                        } else if (!singleton.equals(value)) {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE,
                                "UNRECOVERABLE MULTIPLICITY FAILURE: Conflicting values for optional or single valued field",
                                new BasicException.Parameter("xri", this.getPath()),
                                new BasicException.Parameter("type", type),
                                new BasicException.Parameter("featureName", featureName),
                                new BasicException.Parameter("size", size),
                                new BasicException.Parameter("values", values),
                                new BasicException.Parameter("object", object)
                            ).log();
                        }
                    }
                    new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "RECOVERABLE MULTIPLICITY FAILURE: Recoverable value for optional or single valued field",
                        new BasicException.Parameter("xri", this.getPath()),
                        new BasicException.Parameter("type", type),
                        new BasicException.Parameter("featureName", featureName),
                        new BasicException.Parameter("size", size),
                        new BasicException.Parameter("values", values),
                        new BasicException.Parameter("value", singleton),
                        new BasicException.Parameter("object", object)
                    ).log();
                    if(singleton == null) {
                        return Collections.emptyList();
                    } else {
                        return Collections.singletonList(singleton);
                    }
                }
            }
        }
        return values;
    }
    
    /**
     * Clone the wrapped object 
     * 
     * @return a facade of the clone
     * @throws ResourceException 
     * 
     * @throws ServiceException
     */
    public Object_2Facade cloneObject(
    ) throws ServiceException{
        Object_2Facade copy;
		try {
			copy = newInstance(
			    this.getPath(),
			    this.getObjectClass()
			);
		} catch (ResourceException exception) {
			throw new ServiceException(exception);
		}
        copy.setVersion(this.getVersion());
        MappedRecord value = this.getValue();
        for(String key: (Set<String>)value.keySet()) {
            Object source = this.attributeValues(key);
            if(source instanceof SparseArray) {
                ((SparseArray)copy.attributeValues(
                    key, 
                    Multiplicity.SPARSEARRAY
                )).putAll(
                    (SparseArray)source
                );
            } else if (source instanceof List){
                ((List)copy.attributeValues(
                    key,
                    Multiplicity.LIST
                )).addAll(
                    assertMultiplicity(value, key, (List)source)
                );                
            } else if (source instanceof Map<?,?>){
                ((Map)copy.attributeValues(
                    key,
                    Multiplicity.MAP
                )).putAll(
                    (Map)source
                );                                
            } else {
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "Unexpected value type to be cloned",
                    new BasicException.Parameter("xri", this.getPath()),
                    new BasicException.Parameter("key", key),
                    new BasicException.Parameter("type", source == null ? null : source.getClass().getName())
                ).log();
            }
        }
        return copy;
    }
    
    /**
     * Clone an object 
     * 
     * @param object
     * @return a clone of the object
     * 
     * @throws ServiceException
     */
    public static MappedRecord cloneObject(
        MappedRecord object
    ) throws ServiceException {
		try {
			return newInstance(object).cloneObject().getDelegate();
		} catch (ResourceException e) {
			throw new ServiceException(e);
		}
    }
    
    /**
     * @param type the fully qualified MOF class name
     * @param featureName the unqualified feature name
     * 
     * @throws ServiceException
     */
    protected static boolean isSingleValued(
        String type,
        String featureName
    ){
        Model_1_0 model = Model_1Factory.getModel();
        try {
            ModelElement_1_0 classifierDef = model.getElement(type);
            if(classifierDef != null) {
                ModelElement_1_0 featureDef = model.getFeatureDef(classifierDef, featureName, false);
                if(featureDef != null) {
                    return ModelHelper.getMultiplicity(featureDef).isSingleValued();
                } else {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND,
                        "Invalid feature",
                        new BasicException.Parameter("type", type),
                        new BasicException.Parameter("feature", featureName)
                    );
                }
            } else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "Class not found",
                    new BasicException.Parameter("class", type)
                );
            }
        } catch (ServiceException exception) {
            exception.log();
            return false;
        }
    }
    
    
    //-----------------------------------------------------------------------
    // List Facade    
    //-----------------------------------------------------------------------
    
    private static class ListFacade<E>
        implements List<E>, Cloneable, Serializable
    {
    
        /**
         * Creates <code>DelegatingSparseList</code>. The value is managed in record at key.
         */
        public ListFacade(
            MappedRecord record,
            Object key
        ) {
            this.record = record;
            this.key = key;
        }
        
        private static final long serialVersionUID = 9044898033126787944L;
        private final MappedRecord record;    
        private final Object key;
        
        private List<E> nonDelegate(
        ){
            E value = (E)this.record.get(this.key);
            return value == null ? 
                (List<E>)Collections.EMPTY_LIST : 
                Collections.singletonList(value);        
        }
        
        private synchronized List<E> attributeValues(
        ){
            Object value = this.record.get(this.key);
            if(value instanceof List) {
                return (List<E>)value;
            } else {                
                List<E> values = new ArrayList<E>();
                if(value != null) {
                    values.add((E)value);
                }
                this.record.put(
                    this.key, 
                    values
                );
                return values;
            }
        }
        
        private List<E> getList(
        ) {
            Object values = this.record.get(this.key);
            return values instanceof List ? (List<E>)values : nonDelegate();
        }
        
        /* (non-Javadoc)
         * @see java.util.List#add(int, java.lang.Object)
         */
        public void add(
            int index, 
            E element
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                ((List)values).add(index, element);            
            }
            else {
                if(index == 0 && values == null) {
                    this.record.put(
                        this.key, 
                        element
                    );
                } 
                else {
                    this.attributeValues().add(index, element);
                }
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#add(java.lang.Object)
         */
        public boolean add(
            E element
        ) {
            Object values = this.record.get(this.key);
            if(element == null) {
                return false;
            } 
            else if(values instanceof List) {
                return ((List)values).add(element);
            }
            else {
                if(values == null) {
                    this.record.put(
                        this.key, 
                        element
                    );                
                    return true;
                } 
                else {
                    return this.attributeValues().add(element);
                }
            } 
        }
    
        /* (non-Javadoc)
         * @see java.util.List#addAll(java.util.Collection)
         */
        public boolean addAll(
            Collection<? extends E> c
        ) {
            Object values = this.record.get(this.key);
            if(c.isEmpty()) {
                return false;
            } 
            else if(values instanceof List) {
                return ((List)values).addAll(c);
            }
            else {
                if(values != null || c.size() > 1) {
                    return this.attributeValues().addAll(c);
                } 
                else {
                    Object value = c.iterator().next();
                    this.record.put(
                        this.key, 
                        value
                    );                
                    return value != null;
                }
            } 
        }
    
        /* (non-Javadoc)
         * @see java.util.List#addAll(int, java.util.Collection)
         */
        public boolean addAll(
            int index, 
            Collection<? extends E> c
        ) {
            Object values = this.record.get(this.key);
            if(c.isEmpty()) {
                return false;
            } 
            else if(values instanceof List) {
                return ((List)values).addAll(index, c);
            }
            else {
                if(index > 0 || values != null || c.size() > 1) {
                    return this.attributeValues().addAll(index, c);
                } 
                else {
                    Object value = c.iterator().next();
                    this.record.put(
                        this.key, 
                        value
                    );                                
                    return value != null;
                }
            } 
        }
    
        /* (non-Javadoc)
         * @see java.util.List#clear()
         */
        public void clear(
        ) {
            this.attributeValues().clear();
        }
    
        /* (non-Javadoc)
         * @see java.util.List#contains(java.lang.Object)
         */
        public boolean contains(
            Object o
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                return ((List)values).contains(o);            
            }
            else {
                return values != null && values.equals(o);
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#containsAll(java.util.Collection)
         */
        public boolean containsAll(
            Collection<?> c
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                return ((List)values).containsAll(c);            
            }
            else {
                for(
                    Iterator<?> i = c.iterator();
                    i.hasNext();
                ) {
                    if(!this.contains(i.next())) {
                        return false;
                    }
                }
                return true;
            } 
        }
    
        /* (non-Javadoc)
         * @see java.util.List#equals(java.lang.Object)
         */
        @Override
        public boolean equals(
            Object o
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                return ((List)values).equals(o);            
            }
            else {
                if(o instanceof List<?>) {
                    List<?> l = (List<?>)o;
                    if(values == null) {
                        return l.isEmpty();
                    } 
                    else if(l.size() == 1) {
                        return values.equals(l.get(0));
                    }
                    else {
                        return false;
                    }
                } 
                else {
                    return false;
                }
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#get(int)
         */
        public E get(
            int index
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                return ((List<E>)values).get(index);           
            }
            else {
                return index == 0 ? (E)values : null;
            } 
        }
    
        /* (non-Javadoc)
         * @see java.util.List#hashCode()
         */
        @Override
        public int hashCode(
        ) {
            switch(this.size()) {
                case 0: return 0;
                default: return this.record.get(this.key).hashCode();
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#indexOf(java.lang.Object)
         */
        public int indexOf(
            Object o
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                return ((List)values).indexOf(o);            
            }
            else {
                if(values == null) {
                    return o == null ? 0 : -1;
                } 
                else {
                    return values.equals(o) ? 0 : -1;
                }
            } 
        }
    
        /* (non-Javadoc)
         * @see java.util.List#isEmpty()
         */
        public boolean isEmpty(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                return ((List)values).isEmpty();            
            }
            else {
                return values == null;
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#iterator()
         */
        public Iterator<E> iterator(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                return ((List)values).iterator();            
            }
            else {
                return new NonDelegateIterator();
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#lastIndexOf(java.lang.Object)
         */
        public int lastIndexOf(
            Object o
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                return ((List)values).lastIndexOf(o);            
            }
            else {
                if(values == null) {
                    return o == null ? 0 : -1;
                } 
                else {
                    return values.equals(o) ? 0 : -1;
                }
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#listIterator()
         */
        public ListIterator<E> listIterator(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                return ((List)values).listIterator();            
            }
            else {
                return new NonDelegateIterator();
            } 
        }
    
        /* (non-Javadoc)
         * @see java.util.List#listIterator(int)
         */
        public ListIterator<E> listIterator(
            int index
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                return ((List)values).listIterator(index);            
            }
            else {
                return new NonDelegateIterator(index);
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#remove(int)
         */
        public E remove(
            int index
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                return ((List<E>)values).remove(index);            
            }
            else {
                if(index < 0) throw new IndexOutOfBoundsException(
                    "Index " + index + " is less than 0"
                );
                if(index < 0 || index > size()) throw new IndexOutOfBoundsException(
                    "Index " + index + " is greater than size " + size()
                );
                E value = (E)values;
                this.record.remove(this.key);
                return value;
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#remove(java.lang.Object)
         */
        public boolean remove(
            Object o
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                return ((List)values).remove(o);            
            }
            else {
                boolean modify = this.contains(o);
                if(modify) {
                    this.record.remove(this.key);
                }
                return modify;
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#removeAll(java.util.Collection)
         */
        public boolean removeAll(
            Collection<?> c
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                return ((List)values).removeAll(c);            
            }
            else {
                if(values == null) {
                    return false;
                } 
                else {
                    for(
                        Iterator<?> i = c.iterator();
                        i.hasNext();
                    ) {
                        if(this.remove(i.next())) return true;  
                    }
                    return false;
                }
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#retainAll(java.util.Collection)
         */
        public boolean retainAll(
            Collection<?> c
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                return ((List)values).retainAll(c);            
            }
            else {
                if(values == null){
                    return false;
                } 
                else {
                    boolean modify = !c.contains(values);
                    if(modify) {
                        this.record.remove(this.key);
                    }
                    return modify;
                }
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#set(int, java.lang.Object)
         */
        public E set(
            int index, 
            E element
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                return ((List<E>)values).set(index, element);            
            }
            else {
                if(index == 0) {
                    E value = (E)values;
                    this.record.put(
                        this.key,
                        element
                    );
                    return value;
                    
                } 
                else {
                    return this.attributeValues().set(index, element);
                }
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#size()
         */
        public int size(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                return ((List)values).size();            
            }
            else {
                return values == null ? 0 : 1;
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#subList(int, int)
         */
        public List<E> subList(
            int fromIndex, 
            int toIndex
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                return ((List)values).subList(fromIndex, toIndex);            
            }
            else {
                if(values == null || fromIndex > 0) {
                    return Collections.nCopies(toIndex - fromIndex, null);
                } 
                else if (toIndex == 1){
                    return this.nonDelegate();
                } 
                else {
                    return this.attributeValues().subList(fromIndex, toIndex);
                }
            } 
        }
    
        /* (non-Javadoc)
         * @see java.util.List#toArray()
         */
        public Object[] toArray(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof List) {
                return ((List)values).toArray();            
            }
            else {
                return values == null ? EMPTY_ARRAY : new Object[]{values};
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#toArray(java.lang.Object[])
         */
        public <T> T[] toArray(
            T[] _a
        ) {
            Object values = this.record.get(this.key);
            T[] a = _a;
            if(values instanceof List) {
                return ((List<E>)values).toArray(a);            
            }
            else {
                if(values != null) {
                    if(a.length == 0) {
                        a = (T[]) Array.newInstance(
                            a.getClass().getComponentType(),
                            1
                        );
                    }
                    a[0] = (T)values;
                }
                return a;
            }
        }
            
        //------------------------------------------------------------------------
        // Members
        //------------------------------------------------------------------------
        private static final Object[] EMPTY_ARRAY = new Object[0];
        
        //------------------------------------------------------------------------
        // Implements Cloneable
        //------------------------------------------------------------------------
        
        /* (non-Javadoc)
         * @see java.lang.Object#clone()
         */
        @Override
        protected Object clone(
        ) {
            throw new UnsupportedOperationException("clone not supported for delegating sparse lists");
        }
        
        //------------------------------------------------------------------------
        // Extends Object
        //------------------------------------------------------------------------
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return getList().toString();
        }
    
        
        //------------------------------------------------------------------------
        // Class NonDelegateIterator
        //------------------------------------------------------------------------    
        class NonDelegateIterator implements ListIterator<E> {
    
            private int nextIndex;    
            private int currentIndex;
            private int previousIndex;
            
            /**
             * Constructor
             *  
             * @param index
             */
            NonDelegateIterator(
                int index
            ){
                this.nextIndex = index;
                this.currentIndex = -2;
                this.previousIndex = index -1;
            }
    
            NonDelegateIterator(
            ){
                this(0);
            }
            
            /* (non-Javadoc)
             * @see java.util.ListIterator#nextIndex()
             */
            public int nextIndex() {
                return this.nextIndex;
            }
    
            /* (non-Javadoc)
             * @see java.util.ListIterator#previousIndex()
             */
            public int previousIndex() {
                return this.previousIndex;
            }
    
            private void invalidateCursor(){
                if(this.currentIndex != 0) throw new IllegalStateException();
                this.currentIndex = -2;
            }
    
            private E moveCursor(){
                this.currentIndex = 0;
                this.previousIndex = -1;
                this.nextIndex = 1;
                return ListFacade.this.get(0);
            }
            
            /* (non-Javadoc)
             * @see java.util.ListIterator#remove()
             */
            public void remove() {
                invalidateCursor();
                ListFacade.this.set(0, null);
            }
    
            /* (non-Javadoc)
             * @see java.util.ListIterator#hasNext()
             */
            public boolean hasNext() {
                return nextIndex() < ListFacade.this.size();
            }
    
            /* (non-Javadoc)
             * @see java.util.ListIterator#hasPrevious()
             */
            public boolean hasPrevious() {
                return previousIndex() >= 0;
            }
    
            /* (non-Javadoc)
             * @see java.util.ListIterator#next()
             */
            public E next() {
                if(!hasNext()) throw new NoSuchElementException();
                return moveCursor();
            }
    
            /* (non-Javadoc)
             * @see java.util.ListIterator#previous()
             */
            public E previous() {
                if(!hasPrevious()) throw new NoSuchElementException();
                return moveCursor();
            }
    
            /* (non-Javadoc)
             * @see java.util.ListIterator#add(java.lang.Object)
             */
            public void add(Object o) {
                throw new UnsupportedOperationException();
            }
    
            /* (non-Javadoc)
             * @see java.util.ListIterator#set(java.lang.Object)
             */
            public void set(E o) {
                invalidateCursor();
                ListFacade.this.set(0, o);
            }
            
        }
                    
    }

    //-----------------------------------------------------------------------
    // Sparse Array Facade
    //-----------------------------------------------------------------------
    
    private static class SparseArrayFacade<E>
        implements SparseArray<E>, Cloneable, Serializable
    {

        /**
         * Creates <code>SparseArrayFacade</code>. 
         * The value is managed in record at key.
         *
         * @param record
         * @param key
         */
        public SparseArrayFacade(
            MappedRecord record,
            Object key
        ) {
            this.record = record;
            this.key = key;
        }
        
        private static final long serialVersionUID = 5241681200495515635L;
        private final MappedRecord record;    
        private final Object key;

        private synchronized SparseArray<E> attributeValues(
        ){
            Object value = this.record.get(this.key);
            if(value instanceof SparseArray) {
                return (SparseArray<E>)value;
            } else {
                SparseArray<E> values = new TreeSparseArray<E>();
                values.put(Integer.valueOf(0), (E)value);
                this.record.put(
                    this.key, 
                    values
                );
                return values;
            }
        }
                
        /* (non-Javadoc)
         * @see java.util.List#addAll(java.util.Collection)
         */
        public void putAll(
            Map<? extends Integer, ? extends E> m
        ) {
            Object values = this.record.get(this.key);
            if(m.isEmpty()) {
                return;
            } 
            else if(values instanceof SparseArray) {
                ((SparseArray)values).putAll(m);
            }
            else {
                this.attributeValues().putAll(m);
            } 
        }
    
        /* (non-Javadoc)
         * @see java.util.List#clear()
         */
        public void clear(
        ) {
            this.attributeValues().clear();
        }
    
        /* (non-Javadoc)
         * @see java.util.List#equals(java.lang.Object)
         */
        @Override
        public boolean equals(
            Object o
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray) {
                return ((SparseArray)values).equals(o);            
            }
            else {
                if(o instanceof SparseArray<?>) {
                    SparseArray<?> l = (SparseArray<?>)o;
                    if(values == null) {
                        return l.isEmpty();
                    } 
                    else if(l.size() == 1) {
                        return values.equals(l.get(Integer.valueOf(0)));
                    }
                    else {
                        return false;
                    }
                } 
                else {
                    return false;
                }
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#get(int)
         */
        public E get(
            Object key
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray) {
                return ((SparseArray<E>)values).get(key);           
            }
            else {
                return this.attributeValues().get(key);
            } 
        }
    
        /* (non-Javadoc)
         * @see java.util.List#hashCode()
         */
        @Override
        public int hashCode(
        ) {
            switch(this.size()) {
                case 0: return 0;
                default: return this.record.get(this.key).hashCode();
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#isEmpty()
         */
        public boolean isEmpty(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray) {
                return ((SparseArray)values).isEmpty();            
            }
            else {
                return this.attributeValues().isEmpty();
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#iterator()
         */
        public Iterator<E> iterator(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray) {
                return ((SparseArray)values).iterator();            
            }
            else {
                return this.attributeValues().iterator();
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#lastIndexOf(java.lang.Object)
         */
        public Integer lastKey(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray) {
                return (Integer)((SparseArray)values).lastKey();            
            }
            else {
                return this.attributeValues().lastKey();
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#lastIndexOf(java.lang.Object)
         */
        public Integer firstKey(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray) {
                return (Integer)((SparseArray)values).firstKey();            
            }
            else {
                return this.attributeValues().firstKey();
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#listIterator()
         */
        public ListIterator<E> populationIterator(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray) {
                return ((SparseArray)values).populationIterator();            
            }
            else {
                return this.attributeValues().populationIterator();
            } 
        }
    
        /* (non-Javadoc)
         * @see java.util.List#remove(int)
         */
        public E remove(
            Object key
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray) {
                return ((SparseArray<E>)values).remove(key);            
            }
            else {
                return this.attributeValues().remove(key);
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#set(int, java.lang.Object)
         */
        public E put(
            Integer key, 
            E element
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray) {
                return ((SparseArray<E>)values).put(key, element);            
            }
            else {
                return this.attributeValues().put(key, element);
            }
        }

        /* (non-Javadoc)
         * @see java.util.List#size()
         */
        public int size(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray) {
                return ((SparseArray)values).size();            
            }
            else {
                return this.attributeValues().size();
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#subList(int, int)
         */
        public SparseArray<E> subMap(
            Integer fromKey, 
            Integer toKey
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray) {
                return ((SparseArray)values).subMap(fromKey, toKey);            
            }
            else {
                return this.attributeValues().subMap(
                    fromKey, 
                    toKey
                );
            }
        }
    
        /* (non-Javadoc)
         * @see org.w3c.cci2.SparseArray#asList()
         */
        public List<E> asList(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray<?>) {
                return ((SparseArray<E>)values).asList();            
            }
            else {
                return this.attributeValues().asList();
            }
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.SparseArray#headMap(java.lang.Integer)
         */
        public SparseArray<E> headMap(
            Integer toKey
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray<?>) {
                return ((SparseArray<E>)values).headMap(toKey);            
            }
            else {
                return this.attributeValues().headMap(toKey);
            }
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.SparseArray#tailMap(java.lang.Integer)
         */
        public SparseArray<E> tailMap(
            Integer fromKey
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray<?>) {
                return ((SparseArray<E>)values).tailMap(fromKey);            
            }
            else {
                return this.attributeValues().tailMap(fromKey);
            }
        }

        /* (non-Javadoc)
         * @see java.util.SortedMap#comparator()
         */
        public Comparator<? super Integer> comparator(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray<?>) {
                return ((SparseArray<E>)values).comparator();            
            }
            else {
                return this.attributeValues().comparator();
            }
        }

        /* (non-Javadoc)
         * @see java.util.Map#containsKey(java.lang.Object)
         */
        public boolean containsKey(
            Object key
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray<?>) {
                return ((SparseArray<E>)values).containsKey(key);            
            }
            else {
                return this.attributeValues().containsKey(key);
            }
        }

        /* (non-Javadoc)
         * @see java.util.Map#containsValue(java.lang.Object)
         */
        public boolean containsValue(
            Object value
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray<?>) {
                return ((SparseArray<E>)values).containsValue(value);            
            }
            else {
                return this.attributeValues().containsValue(value);
            }
        }

        /* (non-Javadoc)
         * @see java.util.Map#entrySet()
         */
        public Set<java.util.Map.Entry<Integer, E>> entrySet(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray<?>) {
                return ((SparseArray<E>)values).entrySet();            
            }
            else {
                return this.attributeValues().entrySet();
            }
        }

        /* (non-Javadoc)
         * @see java.util.Map#keySet()
         */
        public Set<Integer> keySet(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray<?>) {
                return ((SparseArray<E>)values).keySet();            
            }
            else {
                return this.attributeValues().keySet();
            }
        }

        /* (non-Javadoc)
         * @see java.util.Map#values()
         */
        public Collection<E> values(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseArray<?>) {
                return ((SparseArray<E>)values).values();            
            }
            else {
                return this.attributeValues().values();
            }
        }

        //------------------------------------------------------------------------
        // Implements Cloneable
        //------------------------------------------------------------------------
        
        /* (non-Javadoc)
         * @see java.lang.Object#clone()
         */
        @Override
        protected Object clone(
        ) {
            throw new UnsupportedOperationException("clone not supported for delegating sparse arrays");
        }
        
        //------------------------------------------------------------------------
        // Extends Object
        //------------------------------------------------------------------------
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString(
        ) {
            return this.attributeValues().toString();
        }
        
    }

    //------------------------------------------------------------------------
    // Class GuardedList
    //------------------------------------------------------------------------

    /**
     * GuardedList asserts that only multivalued features may have more than one element
     */
    class GuardedList extends AbstractList<Object> {

        GuardedList(String objectClass, String featureName, List<Object> valuesAsList) {
            this.objectClass = objectClass;
            this.featureName = featureName;
            this.delegate = valuesAsList;
        }
        
        private final List<Object> delegate;
        private final String objectClass;
        private final String featureName;
        private Boolean singlevalued;
        
        /* (non-Javadoc)
         * @see java.util.AbstractList#get(int)
         */
        @Override
        public Object get(int index) {
            return this.delegate.get(index);
        }
       
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.delegate.size();
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractList#set(int, java.lang.Object)
         */
        @Override
        public Object set(int index, Object element) {
            return this.delegate.set(index, element);
        }
        
        private boolean isSinglevalued(){
            if(this.singlevalued == null) {
                this.singlevalued = Boolean.valueOf(isSingleValued(getObjectClass(), this.featureName));
            }
            return this.singlevalued.booleanValue();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractList#add(int, java.lang.Object)
         */
        @Override
        public void add(int index, Object element) {
            if(this.delegate.isEmpty() || !this.isSinglevalued()) {
                this.delegate.add(index, element);
            } else {
                throw new RuntimeServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "The maximal cardinality of 1 would be exceeded",
                    new BasicException.Parameter("objectClass", this.objectClass),
                    new BasicException.Parameter("featureName", this.featureName),
                    new BasicException.Parameter("delegate", this.delegate),
                    new BasicException.Parameter("index", index),
                    new BasicException.Parameter("value", element)
                ).log();
            }
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractList#remove(int)
         */
        @Override
        public Object remove(int index) {
            return this.delegate.remove(index);
        }

        
    }

}
