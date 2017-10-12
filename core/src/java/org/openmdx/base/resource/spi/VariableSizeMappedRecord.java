/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: JCA: variable-size MappedRecord implementation
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license  as listed below.
 * 
 * Copyright (c) 2004-2013, OMEX AG, Switzerland
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
package org.openmdx.base.resource.spi;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.resource.cci.Freezable;
import org.openmdx.kernel.collection.InternalizedKeyMap;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.w3c.cci2.ImmutableDatatype;

/**
 * Java Connector Architecture:
 * A variable-size MappedRecord implementation. 
 * <p>
 * The key values must be of instances of the following types:<ul> 
 * <li><code>java.lang.String<code>
 * <li><code>java.lang.Short<code>
 * <li><code>java.lang.Integer<code>
 * <li><code>java.lang.Long<code>
 * </ul>
 */
@SuppressWarnings({"rawtypes","unchecked"})
class VariableSizeMappedRecord 
    extends AbstractMap
    implements MappedRecord, MultiLineStringRepresentation, Freezable
{

    /**
     * Creates a <code>MappedRecord</code> with the specified name.
     *
     * @param     recordName
     *            The name of the record acts as a pointer to the meta 
     *            information (stored in the metadata repository) for a specific
     *            record type. 
     */
    VariableSizeMappedRecord(
        String recordName
    ){
        this.recordName = recordName;
        this.recordShortDescription = null;
        this.values = new InternalizedKeyMap();
    }

    /**
     * @serial
     */
    private String recordName;    

    /**
     * @serial
     */
    private String recordShortDescription;

    /**
     * The values are serialized explicitly
     */
    private transient Map<Object,Object> values;
    
    /**
     * Implements <code>Freezable</code>
     */
    private boolean immutable = false;
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 7135299628146306393L;

    
    //------------------------------------------------------------------------
    // Implements Serializable
    //------------------------------------------------------------------------
    
    /**
     * Serialize
     * 
     * @param out
     * @throws IOException
            // TODO Auto-generated method stub
            return null
     */
    private void writeObject(
        ObjectOutputStream out
    ) throws IOException {
        out.defaultWriteObject();
        int size = this.values.size();
        out.writeInt(size);
        for(Map.Entry<?, ?> entry : this.values.entrySet()) {
            out.writeObject(entry.getKey());
            out.writeObject(entry.getValue());
        }
    }
    
    /**
     * De-serialize
     * 
     * @param in
     * 
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(
        java.io.ObjectInputStream in
    ) throws IOException, ClassNotFoundException {
       in.defaultReadObject();
       int size = in.readInt();
       this.values = new InternalizedKeyMap(size);
       for(
           int i = 0;
           i < size;
           i++
       ){
           this.values.put(
               in.readObject(),
               in.readObject()
           );
       }
    }

    
    //--------------------------------------------------------------------------
    // Implements Freezable
    //--------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.Freezable#makeImmutable()
     */
    @Override
    public synchronized void makeImmutable() {
        if(!this.immutable) {
            for(Map.Entry<?,Object> e : this.values.entrySet()){
                Object original = e.getValue();
                Object immutable = Isolation.toImmutable(original);
                if(original != immutable) {
                    e.setValue(immutable);
                }
            }
            this.immutable = true;
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.Freezable#isImmutable()
     */
    @Override
    public boolean isImmutable() {
        return this.immutable;
    }
    
    /**
     * Asserts that the object is mutable
     * 
     * @throws IllegalStateException if the record is immutable
     */
    protected void assertMutability(){
        if(this.immutable) {
            throw new IllegalStateException(
                "This record is frozen",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE,
                    new BasicException.Parameter("class", getClass().getName()),
                    new BasicException.Parameter("name", getRecordName()),
                    new BasicException.Parameter("immutable", Boolean.TRUE)
                )
            );
        }
    }

    
    //--------------------------------------------------------------------------
    // Extends AbstractMap
    //--------------------------------------------------------------------------

    /**
     * Associates the specified value with the specified key in this map. 
     * If the map previously contained a mapping for this key, the old value
     * is replaced.
     *
     * @param     key
     *            key with which the specified value is to be associated.
     * @param     value
     *            value to be associated with the specified key.
     *
     * @return    previous value associated with specified key, or null if there
     *            was no mapping for key. (A null return can also indicate that
     *            the map previously associated null with the specified key, if
     *            the implementation supports null values.)
     *
     * @exception ClassCastException
     *            if the class of the specified key or value prevents it from
     *            being stored in this map.
     * @exception IllegalArgumentException unless the key is one of<ul>
     * <li>a <code>java.lang.String</code> instance
     * <li>a <code>java.lang.Instance</code> instance in the range 
     * <code>-128</code> to <code>127</code>
     * </ul>
     * @exception NullPointerException 
     *            this map does not permit null keys or values, and the
     *            specified key or value is <code>null</code>
     */
    @Override
    public Object put(
        Object key,
        Object value
    ){
        assertMutability();
        return this.values.put(
            key, 
            value
         );
    }

    /**
     * Returns a set view of the mappings contained in this map.
     * Each element in this set is a Map.Entry. 
     * 
     * @return  a set view of the mappings contained in this map.
     */
    @Override
    public Set entrySet(
    ){
        return new EntrySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    @Override
    public void clear(
    ) {
        assertMutability();
        this.values.clear();        
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(
        Object key
    ) {
        return this.values.containsKey(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(
        Object value
    ) {
        return this.values.containsValue(value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public Object get(
        Object key
    ) {
        return this.values.get(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty(
    ) {
        return this.values.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public Object remove(
        Object key
    ) {
        assertMutability();
        return this.values.remove(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    @Override
    public int size(
    ) {
        return this.values.size();
    }

    
    //--------------------------------------------------------------------------
    // Implements Record
    //--------------------------------------------------------------------------

    /**
     * Gets the name of the Record. 
     *
     * @return  String representing name of the Record
     */
    public final String getRecordName(
    ){
        return this.recordName;
    }

    /**
     * Sets the name of the Record. 
     *
     * @param name
     *        Name of the Record
     */
    public final void setRecordName(
        String name
    ){
        this.recordName = name;
    }

    /**
     * Gets a short description string for the Record.
     * This property is used primarily by application development tools. 
     *
     * @return   String representing a short description of the Record
     */
    public final String getRecordShortDescription(
    ){
        return this.recordShortDescription;
    }

    /**
     * Sets a short description string for the Record.
     * This property is used primarily by application development tools. 
     *
     * @param description
     *        Description of the Record
     */
    public final void setRecordShortDescription(
        String description
    ){
        this.recordShortDescription = description;
    }

    /**
     * Check whether this instance has the same content as another Map.
     * <p>
     * The Record's name and short description are ignored.
     *
     * @return  true if two instances are equal
     */
    @Override
    public boolean equals(
        Object other
    ){
        if (other == this) return true;
        if (!(other instanceof Map)) return false;
        Map that = (Map) other;
        if (that.size() != size()) return false;
        for(
            Iterator i = entrySet().iterator();
            i.hasNext();
        ) {
            Entry e = (Entry) i.next();
            Object key = e.getKey();
            Object thisValue = e.getValue();
            Object thatValue = that.get(key);
			if (thisValue == null) {
                if (!(thatValue==null && that.containsKey(key))) return false;
            } else if (thisValue instanceof List<?> && thatValue instanceof List<?>){
                List<?> thisList = (List<?>) thisValue;
                List<?> thatList = (List<?>) thatValue;
                int thisSize = thisList.size();
                int thatSize = thatList.size();
                if(thisSize == thatSize) {
                    for(int j = 0; j < thisSize; j++) {
                        if(!areEqual(thisList.get(j), thatList.get(j))) return false;
                    }
                } else {
                    return false;
                }
            } else {
                if(!areEqual(thisValue, thatValue)) return false;
            }
        }
        return true;
    }

    private static boolean areEqual(Object thisValue, Object thatValue){
        return thatValue instanceof ImmutableDatatype<?> ? thatValue.equals(thisValue) : thisValue.equals(thatValue);
    }
    
    /**
     * Returns the hash code for the Record instance. 
     *
     * @return hash code
     */
    @Override
    public int hashCode(
    ){
        return this.values.hashCode();
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return  a copy of this MappedRecord instance
     */
    @Override
    public VariableSizeMappedRecord clone(
    ){
        VariableSizeMappedRecord that = new VariableSizeMappedRecord(this.recordName);
        that.recordShortDescription = this.recordShortDescription;
        that.putAll(this.values);
        return that;
    }

    /**
     * Returns a multi-line string representation of this MappedRecord.
     * <p>
     * The string representation consists of the record name, follwed by the
     * optional short description enclosed in parenthesis (" (...)"), followed 
     * by a colon and the mappings enclosed in braces (": {...}"). Each
     * key-value mapping is rendered as the key followed by an equals sign ("=")
     * followed by the associated value written on a separate line and indented
     * while embedded lines are indented as well.
     *
     * @return   a multi-line String representation of this Record.
     */
    @Override
    public String toString(
    ){
        return IndentingFormatter.toString(this);
    }

    /**
     * Entry Set
     */
    @SuppressWarnings("synthetic-access")
    class EntrySet extends AbstractSet {
        
        /* (non-Javadoc)Kbje
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator iterator() {
            return new EntryIterator(values.entrySet().iterator());
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return values.size();
        }
        
    }

    /**
     * Entry Iterator
     */
    class EntryIterator implements Iterator {
        
        /**
         * Constructor 
         */
        EntryIterator(
            Iterator<Map.Entry<Object, Object>> delegate
        ) {
            this.delegate = delegate;
        }
        
        private final Iterator<Map.Entry<Object, Object>> delegate;

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        @Override
        public Object next() {
            return new RecordEntry(delegate.next());
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            assertMutability();
            delegate.remove();
        }
        
    }

    class RecordEntry implements Map.Entry {

        RecordEntry (
            Map.Entry<Object, Object> delegate
        ){
            this.delegate = delegate;
        }
        
        private final Entry<Object, Object> delegate;

        /* (non-Javadoc)
         * @see java.util.Map.Entry#getKey()
         */
        @Override
        public Object getKey() {
            return delegate.getKey();
        }

        /* (non-Javadoc)
         * @see java.util.Map.Entry#getValue()
         */
        @Override
        public Object getValue() {
            return delegate.getValue();
        }

        /* (non-Javadoc)
         * @see java.util.Map.Entry#setValue(java.lang.Object)
         */
        @Override
        public Object setValue(Object value) {
            assertMutability();
            return delegate.setValue(value);
        }
        
    }
    
}
