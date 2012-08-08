/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: AbstractMappedRecord.java,v 1.7 2011/11/26 01:34:57 hburger Exp $
 * Description: Abstract Mapped Record 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/11/26 01:34:57 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;

import org.openmdx.base.collection.Sets;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;


/**
 * Abstract Mapped Record
 */
@SuppressWarnings("rawtypes")
abstract class AbstractMappedRecord implements MultiLineStringRepresentation, MappedRecord {

    /**
     * Constructor 
     */
    protected AbstractMappedRecord(
        String[] keys
    ){
        this.keys = keys;
    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -4987442812135881974L;

    /**
     * The keys
     */
    protected final String[] keys;
    
    /* (non-Javadoc)
     * @see java.util.AbstractMap#clone()
     */
    @Override
    public abstract Object clone(
    );

    /**
     * The record's short description is treated as comment
     */
    private transient String recordShortDescription;    

    /**
     * The cached key set
     */
    private transient Set keySet;
    
    /**
     * The cached entry set
     */
    private transient Set entrySet;    
    
    /**
     * Retrieve a value by index
     * 
     * @param index the index
     * @return the value
     */
    protected abstract Object get(
        int index
    );    

    /**
     * Set a value by index 
     * 
     * @param index the index
     * @param value the new value
     * 
     * @return the old value
     */
    protected abstract void put(
        int index,
        Object value
    );
    
    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
//  @Override
    public final Object get(Object key) {
        return get(Arrays.binarySearch(this.keys, key));
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
//  @Override
    public final Object put(
        Object key, 
        Object value
    ) {
        int index = Arrays.binarySearch(this.keys, key);
        if(index >= 0) {
            Object old = get(index);
            put(index, value);
            return old;
        }
        throw new IllegalArgumentException(
            "Unsupported key",
            BasicException.newEmbeddedExceptionStack(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                new BasicException.Parameter("supported", (Object[])this.keys),
                new BasicException.Parameter("key", key),
                new BasicException.Parameter("value", value)
            )
        );
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
//  @Override
    final public Set entrySet() {
        return this.entrySet == null ? this.entrySet = new AbstractSet(){

            @Override
            public Iterator iterator() {
                return new Iterator(){

                    private int i = 0;
                    
                //  @Override
                    public boolean hasNext() {
                        return i < size();
                    }

                //  @Override
                    public Object next() {
                        final String key = keys[i++]; 
                        return new Map.Entry() {

                        //  @Override
                            public Object getKey() {
                                return key;
                            }

                        //  @Override
                            public Object getValue() {
                                return get(key);
                            }

                        //  @Override
                            public Object setValue(Object value) {
                                return put(key, value);
                            }
                        };
                    }

                //  @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                    
                };
            }

            @Override
            public int size() {
                return keys.length;
            } 
            
        } : this.entrySet;
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
//  @Override
    final public Set keySet() {
        return this.keySet == null ? this.keySet = Sets.asSet(
            Arrays.asList(this.keys)
        ) : this.keySet;
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
//  @Override
    final public void clear() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
//  @Override
    final public boolean containsKey(Object key) {
        for(String candidate : this.keys) {
            if(candidate.equals(key)) return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
//  @Override
    final public boolean containsValue(Object value) {
        for(String key : this.keys) {
            Object candidate = get(key);
            if(value == null ? null == candidate : value.equals(candidate)) return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
//  @Override
    final public boolean isEmpty() {
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
//  @Override
    final public void putAll(Map m) {
        for(Object e : m.entrySet()) {
            Map.Entry<?, ?> entry = (Entry<?, ?>) e;
            put(entry.getKey(), entry.getValue());
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
//  @Override
    final public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
//  @Override
    final public int size() {
        return this.keys.length;
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
//  @Override
    final public Collection values() {
        return new AbstractCollection(){

            @Override
            public Iterator iterator() {
                return new Iterator(){

                    private int i = 0;
                    
                //  @Override
                    public boolean hasNext() {
                        return i < keys.length;
                    }

                //  @Override
                    public Object next() {
                        return get(keys[i++]);
                    }

                //  @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                    
                };
            }

            @Override
            public int size() {
                return keys.length;
            }
          
        };
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordShortDescription()
     */
//  @Override
    final public String getRecordShortDescription() {
        return this.recordShortDescription;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#setRecordName(java.lang.String)
     */
//  @Override
    final public void setRecordName(String recordName) {
        if(!recordName.equals(getRecordName())) throw BasicException.initHolder(
            new IllegalArgumentException(
                "Unmodifiable Record Name",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("fixed", getRecordName()),
                    new BasicException.Parameter("requested", recordName)
                )
            )
        );
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#setRecordShortDescription(java.lang.String)
     */
//  @Override
    final public void setRecordShortDescription(String description) {
        this.recordShortDescription = description;
    }

    /**
     * Wrap the array into an <code>IndexedRecord</code>
     * 
     * @param set
     * 
     * @return a set delegating to the given array
     */
    protected static IndexedRecord asSet(
        Object[] set
    ){
        return set == null ? null : Records.getRecordFactory().asIndexedRecord("set", null, set);
    }
    
    /**
     * Convert an <code>IndexedRecord</code> to a <code>String</code> array
     * 
     * @param value the <code>IndexedRecord</code>
     * 
     * @return an array with the value's components
     */
    protected static String[] toArray(
        Object value
    ){
        return 
            value == null ? null :
            value instanceof String ? new String[]{(String)value} :
            ((Collection<?>)value).toArray(new String[((Collection<?>)value).size()]); 
    }
    
    /**
     * Convert the value to a <code>Path</code> if necessary
     * 
     * @param value
     * 
     * @return the value as <code>Path</code>
     */
    protected static Path toPath(
        Object value
    ){
        return 
            value == null ? null :
            value instanceof Path ? (Path)value :
            new Path((String)value);
    }
    
    /**
     * Convert the value to a <code>Long</code> if necessary
     * 
     * @param value
     * 
     * @return the value as <code>Long</code>
     */
    protected static Long toLong(
        Object value
    ){
        return 
            value == null ? null :
            value instanceof Long ? (Long)value :
            Long.valueOf((String) value);
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
