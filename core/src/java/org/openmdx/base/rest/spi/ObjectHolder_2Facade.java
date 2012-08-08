/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ObjectHolder_2Facade.java,v 1.7 2009/06/14 00:27:17 wfro Exp $
 * Description: Object Facade
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/14 00:27:17 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.base.collection.OffsetArrayList;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.kernel.exception.BasicException;

/**
 * Object Facade
 */
public class ObjectHolder_2Facade {

    /**
     * Constructor 
     *
     * @param delegate
     */
    private ObjectHolder_2Facade(
        MappedRecord delegate
    ) throws ResourceException {
        if(!isDelegate(delegate)) throw BasicException.initHolder(
            new ResourceException(
                "The delegate has the wrong type",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("expected", "org:openmdx:kernel:ObjectHolder"),
                    new BasicException.Parameter("actual", delegate.getRecordName())
                )
            )
        );
        this.delegate = delegate;
    }

    /**
     * Constructor 
     *
     * @param objectClass the fully qualified MOF id of the object's class
     */
    private ObjectHolder_2Facade(
    ) throws ResourceException {
        this.delegate = Records.getRecordFactory().asMappedRecord(
            "org:openmdx:kernel:ObjectHolder",
            null,
            MEMBERS,
            new Object[MEMBERS.length]
        );
    }
    
    /**
     * The data object members
     */
    private static final String[] MEMBERS = {
        "path",
        "version",
        "value"
    };    
    
    /**
     * The query record
     */
    private final MappedRecord delegate;
    
    /**
     * Cache path value
     */
    private Path path = null;
    
    /**
     * Create a facade for the given record
     * 
     * @param record
     * 
     * @return the object facade
     * 
     * @throws ResourceException
     */
    public static ObjectHolder_2Facade newInstance(
        MappedRecord record
    ) throws ResourceException {
        return record == null ?
            null :
            new ObjectHolder_2Facade(record);
    }
    
    /**
     * Create a facade for the given object class
     * 
     * @param objectClass the object class
     * 
     * @return the object facade
     * 
     * @throws ResourceException
     */
    public static ObjectHolder_2Facade newInstance(
    ) throws ResourceException {
        return new ObjectHolder_2Facade();
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
    public static ObjectHolder_2Facade newInstance(
        Path objectId
    ) throws ResourceException {
        return newInstance(
            objectId,
            "org:openmdx:base:Void"
        );
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
    public static ObjectHolder_2Facade newInstance(
        Path objectId,
        String objectClass
    ) throws ResourceException {
        ObjectHolder_2Facade object = newInstance();
        MappedRecord value = Records.getRecordFactory().createMappedRecord(objectClass);
        object.setPath(objectId);
        object.setValue(value);
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
        return "org:openmdx:kernel:ObjectHolder".equals(record.getRecordName());
    }
    
    /**
     * Retrieve the delegate
     * 
     * @return the delegate
     */
    public MappedRecord getDelegate(){
        return this.delegate;
    }
    
    /**
     * Retrieve objectId.
     *
     * @return Returns the objectId.
     */
    public final Path getPath(
    ) {
        if(this.path == null) {
            this.path = new Path((String)delegate.get("path"));
        }
        return this.path;       
    }
    
    public static Path getPath(
        MappedRecord delegate
    ) {
        return new Path((String)delegate.get("path"));
    }
    
    public String getObjectClass(
    ) {
        return getObjectClass(this.delegate);
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
    @SuppressWarnings("unchecked")
    public final void setPath(Path path) {
        this.delegate.put("path", path.toXRI());
    }
    
    /**
     * Retrieve the value
     *
     * @return Returns the value.
     */
    public final MappedRecord getValue(
    ) {
        return getValue(this.delegate);
    }

    public static MappedRecord getValue(
        MappedRecord delegate
    ) {
        return (MappedRecord)delegate.get("value");
    }
    
    @SuppressWarnings("unchecked")
    public SparseList<Object> getAttributeValues(
        String attributeName
    ) throws ServiceException {
        MappedRecord value = this.getValue();
        Object v = value.get(attributeName);
        if(v == null) {
            return null;
        }
        else {
            return new SparseListFacade(
                value,
                attributeName
            );
        }
    }
    
    @SuppressWarnings("unchecked")
    public SparseList<Object> attributeValues(
        String attributeName
    ) throws ServiceException {
        SparseList value = this.getAttributeValues(attributeName);
        if(value == null) {
            this.getValue().put(
                attributeName,
                null
            );
            value = new SparseListFacade(
                this.getValue(),
                attributeName
            );     
        }
        return value;        
    }

    @SuppressWarnings("unchecked")
    public Object attributeValue(
        String attributeName
    ) throws ServiceException {
        MappedRecord value = this.getValue();
        Object v = value.get(attributeName);
        if(v == null) {
            return null;
        }
        else if(v instanceof SparseList) {
            return ((SparseList<Object>)v).get(0);
        }
        else {
            return v;
        }        
    }
    
    @SuppressWarnings("unchecked")
    public SparseList<Object> clearAttributeValues(
        String attributeName
    ) throws ServiceException {        
        SparseList value = this.attributeValues(attributeName);
        value.clear();
        return value;
    }

    /**
     * Set the value.
     * 
     * @param value The value to set.
     */
    @SuppressWarnings("unchecked")
    public final void setValue(
        MappedRecord value
    ) {
        this.delegate.put("value", value);
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
        return delegate.get("version");
    }
    
    /**
     * Set version.
     * 
     * @param version The version to set.
     */
    @SuppressWarnings("unchecked")
    public final void setVersion(Object version) {
//        Object value = version; // type check
        this.delegate.put(
            "version", 
            version 
        );
    }

    @SuppressWarnings("unchecked")
    public static MappedRecord cloneObject(
        MappedRecord object
    ) throws ResourceException, ServiceException {
        ObjectHolder_2Facade facade = ObjectHolder_2Facade.newInstance(object);
        ObjectHolder_2Facade copy = ObjectHolder_2Facade.newInstance(
            facade.getPath(),
            facade.getObjectClass()
        );
        copy.setVersion(facade.getVersion());
        for(String key: (Set<String>)facade.getValue().keySet()) {
            copy.attributeValues(key).addAll(
                facade.attributeValues(key)
            );
        }
        return copy.getDelegate();
    }
    
    //-----------------------------------------------------------------------
    // SparseListFacade
    //-----------------------------------------------------------------------
    private static class SparseListFacade<E>
        implements SparseList<E>, Cloneable, Serializable
    {
    
        /**
         * Creates <code>DelegatingSparseList</code>. The value is managed in record at key.
         */
        public SparseListFacade(
            MappedRecord record,
            Object key
        ) {
            this.record = record;
            this.key = key;
        }
        
        private static final long serialVersionUID = 9044898033126787944L;
        private MappedRecord record;    
        private Object key;
        
        //------------------------------------------------------------------------
        // Implements SparseList
        //------------------------------------------------------------------------
        
        @SuppressWarnings("unchecked")
        private List<E> nonDelegate(
        ){
            E value = (E)this.record.get(this.key);
            return value == null ? 
                (List<E>)Collections.EMPTY_LIST : 
                Collections.singletonList(value);        
        }
        
        @SuppressWarnings("unchecked")
        private synchronized SparseList<E> attributeValues(
        ){
            Object value = this.record.get(this.key);
            if(value instanceof SparseList) {
                return (SparseList<E>)value;
            }
            else {
                SparseList<E> values = new OffsetArrayList<E>();
                this.record.put(
                    this.key, 
                    values
                );
                if(value != null) {
                    values.add((E)value);
                }
                return values;
            }
        }
        
        @SuppressWarnings("unchecked")
        private List<E> getList(
        ) {
            Object values = this.record.get(this.key);
            return values instanceof SparseList ? (SparseList<E>)values : nonDelegate();
        }
        
        /* (non-Javadoc)
         * @see java.util.List#add(int, java.lang.Object)
         */
        @SuppressWarnings("unchecked")
        public void add(
            int index, 
            E element
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                ((SparseList)values).add(index, element);            
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
        @SuppressWarnings("unchecked")
        public boolean add(
            E o
        ) {
            Object values = this.record.get(this.key);
            if(o == null) {
                return false;
            } 
            else if(values instanceof SparseList) {
                return ((SparseList)values).add(o);
            }
            else {
                if(values == null) {
                    this.record.put(
                        this.key, 
                        o
                    );                
                    return true;
                } 
                else {
                    return this.attributeValues().add(o);
                }
            } 
        }
    
        /* (non-Javadoc)
         * @see java.util.List#addAll(java.util.Collection)
         */
        @SuppressWarnings("unchecked")
        public boolean addAll(
            Collection<? extends E> c
        ) {
            Object values = this.record.get(this.key);
            if(c.isEmpty()) {
                return false;
            } 
            else if(values instanceof SparseList) {
                return ((SparseList)values).addAll(c);
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
        @SuppressWarnings("unchecked")
        public boolean addAll(
            int index, 
            Collection<? extends E> c
        ) {
            Object values = this.record.get(this.key);
            if(c.isEmpty()) {
                return false;
            } 
            else if(values instanceof SparseList) {
                return ((SparseList)values).addAll(index, c);
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
            this.record.remove(this.key);
        }
    
        /* (non-Javadoc)
         * @see java.util.List#contains(java.lang.Object)
         */
        @SuppressWarnings("unchecked")
        public boolean contains(
            Object o
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).contains(o);            
            }
            else {
                return values != null && values.equals(o);
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#containsAll(java.util.Collection)
         */
        @SuppressWarnings("unchecked")
        public boolean containsAll(
            Collection<?> c
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).containsAll(c);            
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
        @SuppressWarnings("unchecked")
        public boolean equals(
            Object o
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).equals(o);            
            }
            else {
                if(o instanceof SparseList<?>) {
                    SparseList<?> l = (SparseList<?>)o;
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
         * @see org.openmdx.compatibility.base.collection.SparseList#firstIndex()
         */
        @SuppressWarnings("unchecked")
        public int firstIndex(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).firstIndex();            
            }
            else {
                return 0;
            } 
        }
    
        /* (non-Javadoc)
         * @see java.util.List#get(int)
         */
        @SuppressWarnings("unchecked")
        public E get(
            int index
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList<E>)values).get(index);           
            }
            else {
                return index == 0 ? (E)values : null;
            } 
        }
    
        /* (non-Javadoc)
         * @see java.util.List#hashCode()
         */
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
        @SuppressWarnings("unchecked")
        public int indexOf(
            Object o
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).indexOf(o);            
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
        @SuppressWarnings("unchecked")
        public boolean isEmpty(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).isEmpty();            
            }
            else {
                return values == null;
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#iterator()
         */
        @SuppressWarnings("unchecked")
        public Iterator<E> iterator(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).iterator();            
            }
            else {
                return new NonDelegateIterator();
            }
        }
    
        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.collection.SparseList#lastIndex()
         */
        @SuppressWarnings("unchecked")
        public int lastIndex(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).lastIndex();            
            }
            else {
                return values == null ? -1 : 0;
            } 
        }
    
        /* (non-Javadoc)
         * @see java.util.List#lastIndexOf(java.lang.Object)
         */
        @SuppressWarnings("unchecked")
        public int lastIndexOf(
            Object o
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).lastIndexOf(o);            
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
        @SuppressWarnings("unchecked")
        public ListIterator<E> listIterator(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).listIterator();            
            }
            else {
                return new NonDelegateIterator();
            } 
        }
    
        /* (non-Javadoc)
         * @see java.util.List#listIterator(int)
         */
        @SuppressWarnings("unchecked")
        public ListIterator<E> listIterator(
            int index
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).listIterator(index);            
            }
            else {
                return new NonDelegateIterator(index);
            }
        }
    
        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.collection.SparseList#population()
         */
        @SuppressWarnings("unchecked")
        public List<E> population(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).population();            
            }
            else {
                return this.nonDelegate();
            }
        }
    
        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.collection.SparseList#populationIterator()
         */
        @SuppressWarnings("unchecked")
        public ListIterator<E> populationIterator(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).populationIterator();            
            }
            else {
                return new NonDelegateIterator();
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#remove(int)
         */
        @SuppressWarnings("unchecked")
        public E remove(
            int index
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList<E>)values).remove(index);            
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
        @SuppressWarnings("unchecked")
        public boolean remove(
            Object o
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).remove(o);            
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
        @SuppressWarnings("unchecked")
        public boolean removeAll(
            Collection<?> c
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).removeAll(c);            
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
        @SuppressWarnings("unchecked")
        public boolean retainAll(
            Collection<?> c
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).retainAll(c);            
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
        @SuppressWarnings("unchecked")
        public E set(
            int index, 
            E element
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList<E>)values).set(index, element);            
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
        @SuppressWarnings("unchecked")
        public int size(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).size();            
            }
            else {
                return values == null ? 0 : 1;
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#subList(int, int)
         */
        @SuppressWarnings("unchecked")
        public List<E> subList(
            int fromIndex, 
            int toIndex
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).subList(fromIndex, toIndex);            
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
        @SuppressWarnings("unchecked")
        public Object[] toArray(
        ) {
            Object values = this.record.get(this.key);
            if(values instanceof SparseList) {
                return ((SparseList)values).toArray();            
            }
            else {
                return values == null ? EMPTY_ARRAY : new Object[]{values};
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.List#toArray(java.lang.Object[])
         */
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(
            T[] _a
        ) {
            Object values = this.record.get(this.key);
            T[] a = _a;
            if(values instanceof SparseList) {
                return ((SparseList<E>)values).toArray(a);            
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
                return SparseListFacade.this.get(0);
            }
            
            /* (non-Javadoc)
             * @see java.util.ListIterator#remove()
             */
            public void remove() {
                invalidateCursor();
                SparseListFacade.this.set(0, null);
            }
    
            /* (non-Javadoc)
             * @see java.util.ListIterator#hasNext()
             */
            public boolean hasNext() {
                return nextIndex() < SparseListFacade.this.size();
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
                SparseListFacade.this.set(0, o);
            }
            
        }
                    
    }
    
}
