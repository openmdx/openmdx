/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DataproviderObjectRecord.java,v 1.7 2008/03/19 17:06:44 hburger Exp $
 * Description: Dataprovider Adapter: DataproviderObject Wrapper
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/19 17:06:44 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.transport.adapter;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.resource.cci.MappedRecord;

import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.kernel.text.format.IndentingFormatter;

/**
 * @author hburger
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
@SuppressWarnings("unchecked")
public class DataproviderObjectRecord implements MappedRecord {

    /**
     * 
     */
    private static final long serialVersionUID = 3257567312931731768L;


    /**
     * @param arg0
     */
    public DataproviderObjectRecord(DataproviderObject_1_0 source) {
        this.dataproviderObject = source;
        this.recordName = (String) source.values(SystemAttributes.OBJECT_CLASS).get(0);
        this.keys = source.attributeNames();
        this.keys.remove(SystemAttributes.OBJECT_CLASS);
        byte[] digest = source.getDigest();
        if(digest != null){
            source.clearValues(
                SystemAttributes.OBJECT_LOCK_PREFIX + SystemAttributes.OBJECT_CLASS
            ).set(0, SystemAttributes.OPTIMISTIC_LOCK_CLASS);
            source.clearValues(
                SystemAttributes.OBJECT_LOCK_PREFIX + SystemAttributes.OBJECT_DIGEST
            ).set(0, digest);
        }
    }


    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordName()
     */
    public String getRecordName() {
        return this.recordName;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#setRecordName(java.lang.String)
     */
    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#setRecordShortDescription(java.lang.String)
     */
    public void setRecordShortDescription(String recordShortDescription) {
        this.recordShortDescription = recordShortDescription;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordShortDescription()
     */
    public String getRecordShortDescription() {
        return this.recordShortDescription;
    }


    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size(
    ){
        return this.keys.size();
    }


    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return this.keys.isEmpty();
    }


    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return this.keys.contains(key);
    }


    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        return values().contains(value);
    }


    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        return key instanceof String ?
            this.dataproviderObject.getValues((String)key) :
            null;
    }


    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value) {
        this.values = null;
        if(value instanceof Collection){
            this.dataproviderObject.clearValues((String) key).addAll((Collection) value);
        } else {
            this.dataproviderObject.clearValues((String) key).add(value);
        }
        return null;
    }


    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key) {
        this.values = null;
        Object value = key instanceof String ?
            this.dataproviderObject.getValues((String) key) :
            null;
        this.keys.remove(key);
        return value;
    }


    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map map) {
        for(
            Iterator i = map.entrySet().iterator();
            i.hasNext();
        ){
            Map.Entry e = (Entry) i.next();
            put(e.getKey(),e.getValue());
        }
    }


    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear() {
        this.values = null;
        this.keys.clear();
    }


    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
        return this.keys;
    }


    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public Collection values() {
        if(this.values == null){
            this.values = new ArrayList();
            for(
                Iterator i=this.keys.iterator();
                i.hasNext();
            ) this.values.add(this.dataproviderObject.values((String) i.next()));
        }
        return this.values;
    }


    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
        if(this.entries == null) this.entries = new EntrySet();
        return this.entries;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException {
        return new DataproviderObjectRecord(this.dataproviderObject);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return IndentingFormatter.toString(this);
    }


    //------------------------------------------------------------------------
    // Instance Members
    //------------------------------------------------------------------------

    /**
     * 
     */
    private String recordName = null;

    /**
     * 
     */
    private String recordShortDescription = null;
    
    /**
     * 
     */
    protected final DataproviderObject_1_0 dataproviderObject;

    /**
     * 
     */
    protected Set keys = null;

    /**
     * 
     */
    private transient Collection values = null;

    /**
     * 
     */
    private transient Set entries = null;


    //------------------------------------------------------------------------
    // Class EntrySet
    //------------------------------------------------------------------------

    class EntrySet extends AbstractSet {

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        public Iterator iterator() {
            return new EntryIterator(keys.iterator());
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        public int size() {
            return keys.size();
        }   
        
    }


    //------------------------------------------------------------------------
    // Class EntryIterator
    //------------------------------------------------------------------------

    class EntryIterator implements Iterator {

        EntryIterator(
            Iterator keyIterator
        ){
            this.keyIterator = keyIterator;
        }
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return this.keyIterator.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Object next() {
            return new Entry((String)this.keyIterator.next());
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            this.keyIterator.remove();
        }   
        
        private final Iterator keyIterator;
        
    }


    //------------------------------------------------------------------------
    // Class Entry
    //------------------------------------------------------------------------

    class Entry implements Map.Entry {  
        
        Entry(
            String key
        ){
            this.key = key;
        }

        /* (non-Javadoc)
         * @see java.util.Map.Entry#getKey()
         */
        public Object getKey() {
            return this.key;
        }

        /* (non-Javadoc)
         * @see java.util.Map.Entry#getValue()
         */
        public Object getValue() {
            return dataproviderObject.values(key);
        }

        /* (non-Javadoc)
         * @see java.util.Map.Entry#setValue(java.lang.Object)
         */
        public Object setValue(Object value) {
            return put(this.key, value);
        }
                
        private String key;

    }
    
}
