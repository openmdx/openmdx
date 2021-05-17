/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Null Masking Map 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.base.collection;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * NullMaskingMap
 *
 */
@SuppressWarnings("rawtypes")
class NullMaskingMap extends AbstractMapDecorator {

    /**
     * Constructor only used in deserialization, do not use otherwise.
     */
    protected NullMaskingMap() {
        super();
    }

    /**
     * Constructor 
     *
     * @param map
     */
    protected NullMaskingMap(
        Map delegate,
        Object nullValue
    ) {
        super(delegate);
        this.nullValue = nullValue;
    }

    /**
     * The null value object
     */
    Object nullValue;
    
    /**
     * The values
     */
    private transient Collection values;
    
    /**
     * The entry set
     */
    private transient Set entries;

    /**
     * Decorate a given map
     * 
     * @param delegate
     * @param nullValue
     * 
     * @return a newly created decorated map
     */
    static Map decorate(
        Map delegate,
        Object nullValue
    ){
        return new NullMaskingMap(delegate, nullValue);
    }

    @Override
    public boolean containsValue(Object value) {
        return super.containsValue(
            mask(value)
        );
    }

    @Override
    public Set entrySet() {
        if(this.entries == null) {
            this.entries = new Entries(
                super.entrySet()
            );
        }
        return this.entries;
    }

    @Override
    public Object get(Object key) {
        return unmask(
            super.get(key)
        );
    }

    @Override
    public Object put(Object key, Object value) {
        return super.put(
            key, 
            mask(value)
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public void putAll(Map mapToCopy) {
        for (Map.Entry<?,?> e : (Set<Map.Entry<?,?>>) super.entrySet()) {
            super.put(
                e.getKey(), 
                mask(e.getValue())
            );
        }
    }

    @Override
    public Collection values() {
        if(this.values == null) {
            this.values = new Values(super.values());
        }
        return this.values;
    }

    /**
     * Replace <code>null<code> by <code>nullValue</code>
     * 
     * @param value
     * 
     * @return the masked value
     */
    protected Object mask (
        Object value
    ){
        return value == null ? this.nullValue : value;
    }
    
    /**
     * Replace <code>null<nullValue> by <code>null</code>
     * 
     * @param value
     * 
     * @return the unmasked value
     */
    protected Object unmask (
        Object value
    ){
        return value == this.nullValue ? null : value;
    }
    
        
    //------------------------------------------------------------------------
    // Class MaskedValues
    //------------------------------------------------------------------------
    
    /**
     * Masked Values
     */
    class Values extends AbstractCollection {

        /**
         * Constructor 
         *
         * @param delegate
         * @param nullValue
         */
        Values(
            Collection delegate
        ){
            this.delegate = delegate;
        }

        /**
         * Non-masked values
         */
        protected final Collection delegate;

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator iterator() {
            return new Iterator() {

                private final Iterator delegate = Values.this.delegate.iterator();
                
                public boolean hasNext() {
                    return this.delegate.hasNext();
                }

                public Object next() {
                    return unmask(this.delegate.next());
                }

                public void remove() {
                    this.delegate.remove();
                }
                
            };
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.delegate.size();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return this.delegate.isEmpty();
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class MaskedEntries
    //------------------------------------------------------------------------

    /**
     * Masked Entries
     */
    class Entries extends AbstractSet {

        /**
         * Constructor 
         *
         * @param delegate
         */
        Entries(
            Set delegate
        ){
            this.delegate = delegate;
        }
        
        /**
         * 
         */
        protected final Set delegate;
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator iterator() {
            return new Iterator() {

                protected final Iterator delegate = Entries.this.delegate.iterator();
                
                public boolean hasNext() {
                    return this.delegate.hasNext();
                }

                public Object next() {
                    return new Map.Entry(){

                        private final Map.Entry entry = (Entry) delegate.next(); 
                        
                        public Object getKey() {
                            return entry.getKey();
                        }

                        public Object getValue() {
                            return unmask(entry.getValue());
                        }

                        @SuppressWarnings("unchecked")
                        public Object setValue(Object value) {
                            return unmask(
                                entry.setValue(
                                    mask(value)
                                )
                            ); 
                        }
                        
                    };
                }

                public void remove() {
                    this.delegate.remove();
                }
                
            };
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.delegate.size();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return this.delegate.isEmpty();
        }
        
    }

}