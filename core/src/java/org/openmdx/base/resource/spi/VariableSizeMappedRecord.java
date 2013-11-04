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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.resource.cci.MappedRecord;

import org.openmdx.kernel.collection.InternalizedKeys;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.w3c.cci2.ImmutableDatatype;

/**
 * Java Connector Architecture:
 * A variable-size MappedRecord implementation. Only keys of type string and Integers in the range [-128..127] are supported.
 */
@SuppressWarnings({"rawtypes","unchecked"})
class VariableSizeMappedRecord 
    extends AbstractMap
    implements MappedRecord, MultiLineStringRepresentation
{

    /**
     * Constructor
     */
    protected VariableSizeMappedRecord(){
        // for de-serialization
    }
        
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
        this.values = new IdentityHashMap();
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
    private transient Map values;
    
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
     */
    private void writeObject(
        ObjectOutputStream out
    ) throws IOException {
        out.defaultWriteObject();
        int size = this.values.size();
        out.writeInt(size);
        for(Map.Entry<?, ?> entry : (Set<Map.Entry<?, ?>>)this.values.entrySet()) {
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
       this.values = new IdentityHashMap(size);
       for(
           int i = 0;
           i < size;
           i++
       ){
           this.values.put(
               InternalizedKeys.internalize(in.readObject()),
               in.readObject()
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
        return this.values.put(
            InternalizedKeys.internalize(key), 
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
        return this.values.entrySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    @Override
    public void clear(
    ) {
        this.values.clear();        
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(
        Object key
    ) {
        return this.values.containsKey(
            InternalizedKeys.internalize(key)
        );
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(
        Object value
    ) {
        return this.containsValue(value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public Object get(
        Object key
    ) {
        return this.values.get(
            InternalizedKeys.internalize(key)
        );
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
        return this.values.remove(
            InternalizedKeys.internalize(key)
        );
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

}
