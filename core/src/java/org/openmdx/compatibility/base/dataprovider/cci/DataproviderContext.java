/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DataproviderContext.java,v 1.9 2008/02/29 17:58:29 hburger Exp $
 * Description: Dataprovider Context
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/29 17:58:29 $
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
package org.openmdx.compatibility.base.dataprovider.cci;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.resource.Records;
import org.openmdx.compatibility.base.collection.OffsetArrayList;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.kernel.text.format.IndentingFormatter;


/**
 * Request/reply contexts
 */
@SuppressWarnings("unchecked")
public abstract class DataproviderContext
    extends AbstractMap
    implements MappedRecord  
{

    private static final long serialVersionUID = 3688508813594473521L;
    
    /**
     * 
     */
    private MappedRecord contexts;

    /**
     * Constructor
     */
    protected DataproviderContext(        
    ){
        this.contexts = newContexts();
    }

    /**
     * Constructor
     */
    protected DataproviderContext(
        DataproviderContext that
    ){
      this.contexts = that != null ? that.contexts : newContexts(); 
    }

    /**
     * @return a new contexts object
     */
    private static final MappedRecord newContexts(){
        try {
            return Records.getRecordFactory().createMappedRecord(
                DataproviderContext.class.getName()
            );
        } catch (ResourceException e) {
            throw new RuntimeServiceException(e);
        }
    }
    
    /**
     * Returns the modifiable context value list.
     * This method never returns null.
     */
    public final SparseList<Object> context(
        String name
    ){
        OffsetArrayList values = (OffsetArrayList)this.contexts.get(name);
        if (values == null) {
            values = new OffsetArrayList();
            this.contexts.put(name, values);
        }
        return values;
    }
    
    /**
     * A map representation of the contexts.
     */
    public final Map contexts(
    ){
        return this.contexts;
    }


    //------------------------------------------------------------------------
    // Implements Record
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordName()
     */
    public String getRecordName(
    ){
        return getClass().getName();
    }
    
    /* (non-Javadoc)
     * @see javax.resource.cci.Record#setRecordName(java.lang.String)
     */
    public void setRecordName(String recordName) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#setRecordShortDescription(java.lang.String)
     */
    public void setRecordShortDescription(String recordShortDescription) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordShortDescription()
     */
    public String getRecordShortDescription() {
        return null;
    }


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /**
     * Returns a string representation of the object. In general, the
     * toString method returns a string that "textually represents" this
     * object. The result should be a concise but informative representation
     * that is easy for a person to read. It is recommended that all
     * subclasses override this method.
     *
     * @return      a string representation of the object.
     */
    public String toString(
    ){
        return IndentingFormatter.toString(this);
    } 

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


    //------------------------------------------------------------------------
    // Extends AbstractMap
    //------------------------------------------------------------------------

    private transient Set entrySet;

    /**
     * 
     */
    protected abstract Collection keys();
    
    /**
     * To be overridden by subclasses
     *  
     * (non-Javadoc)
     * @see java.util.Map#get(Object)
     */  
    public Object get(Object key) {
        return ("contexts".equals(key)) ? this.contexts : null; 
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#entrySet()
     */
    public Set entrySet() {
        if(this.entrySet == null) this.entrySet = new EntrySet(keys());
        return this.entrySet;
    }


    //------------------------------------------------------------------------
    // Class EntrySet
    //------------------------------------------------------------------------

    class EntrySet extends AbstractSet {
        
        private final Collection keys;

        EntrySet(
            Collection keys
        ){
            this.keys = keys;
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        public Iterator iterator() {
            return new EntryIterator(this.keys.iterator());
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        public int size() {
            return this.keys.size();
        }
                
    }

    //------------------------------------------------------------------------
    // Class EntryIterator
    //------------------------------------------------------------------------

    class EntryIterator implements Iterator {

        private final Iterator keyIterator;

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
            return new Entry(this.keyIterator.next());
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }    

    }


    //------------------------------------------------------------------------
    // Class Entry
    //------------------------------------------------------------------------

    class Entry implements Map.Entry {

        private Object value;

        private Object key;

        Entry(
            Object key
        ){
            this.key = key;
            this.value = get(key);
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
            return this.value;
        }

        /* (non-Javadoc)
         * @see java.util.Map.Entry#setValue(java.lang.Object)
         */
        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }

    }
    
}
