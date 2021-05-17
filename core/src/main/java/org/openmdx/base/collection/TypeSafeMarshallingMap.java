/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Type-Safe Marshalling Map
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2016, OMEX AG, Switzerland
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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openmdx.base.marshalling.TypeSafeMarshaller;

/**
 * A Marshalling Map
 * <p>
 * The marshaller is applied to the delegate's values, but not to its keys.
 */
public class TypeSafeMarshallingMap<K,U,M>
  extends AbstractMap<K,M>
{

    /**
     * Constructor
     * 
     * @param marshaller the marshaller to be used
     * @param delegate the map with unmarshalled values
     */
    public TypeSafeMarshallingMap(
        TypeSafeMarshaller<U,M> marshaller,
        Map<K,U> delegate 
    ) {
        this.marshaller = marshaller;
        this.delegate = delegate;
    }

    /**
     * The delegate map
     */
    private final Map<K,U> delegate;

    /**
     * The value marshaller
     */
    private final TypeSafeMarshaller<U,M> marshaller;
    
    
    //------------------------------------------------------------------------
    // Implements Map
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        this.delegate.clear();
    }
      
    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return this.delegate.containsKey(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(
        Object value
    ) {
        M marshalled = this.marshaller.asMarshalledValue(value);
        return 
            marshalled != null &&
            this.delegate.containsValue(this.marshaller.unmarshal(marshalled));
    }

    /**
     * 
     */
    @Override
    public Set<Map.Entry<K, M>> entrySet(
    ) {
        return new TypeSafeMarshallingEntrySet<K,U,M>(marshaller, delegate.entrySet());
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public M get(
        Object key
    ) {
        return this.marshaller.marshal(this.delegate.get(key));
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }
  
    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public M put(
        K key, 
        M value
    ) {
        return this.marshaller.marshal(
            this.delegate.put(key, this.marshaller.unmarshal(value))
        );
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public M remove(
        Object key
    ) {
        return this.marshaller.marshal(
           this.delegate.remove(key)
        );
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return this.delegate.size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    @Override
    public Collection<M> values() {
        return new TypeSafeMarshallingCollection<U,M>(
            this.marshaller,
            this.delegate.values()
        );
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TypeSafeMarshallingMap [delegate=" + this.delegate + "]";
    }

    
    //------------------------------------------------------------------------
    // Class TypeSafeMarshallingEntrySet
    //------------------------------------------------------------------------

    /**
     * Type-Safe Marshalling Entry Set
     */
    static class TypeSafeMarshallingEntrySet<K,U,M> 
        extends AbstractSet<Map.Entry<K,M>>
    {
    
        TypeSafeMarshallingEntrySet(
            TypeSafeMarshaller<U,M> marshaller,
            Set<Map.Entry<K,U>> delegate
        ) {
            this.marshaller = marshaller;
            this.delegate = delegate;
        }
        
        private final TypeSafeMarshaller<U,M> marshaller;
        private final Set<Map.Entry<K,U>> delegate;
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<java.util.Map.Entry<K, M>> iterator() {
            return new TypeSafeMarshallingEntryIterator<K,U,M>(marshaller, delegate.iterator());
        }
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.delegate.size();
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "TypeSafeMarshallingEntrySet [delegate=" + this.delegate + "]";
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class TypeSafeMarshallingEntryIterator
    //------------------------------------------------------------------------
    
    /**
     * Type-Safe Marshalling Entry Iterator
     */
    static class TypeSafeMarshallingEntryIterator<K,U,M>
        implements Iterator<Map.Entry<K,M>> 
    {

        TypeSafeMarshallingEntryIterator(
            TypeSafeMarshaller<U,M> marshaller,
            Iterator<Map.Entry<K, U>> delegate
        ) {
            this.marshaller = marshaller;
            this.delegate = delegate;
        }

        /**
         * The delegate iterator
         */
        private final Iterator<Map.Entry<K, U>> delegate;

        /**
         * The marshaller to be used
         */    
        private final TypeSafeMarshaller<U,M> marshaller;
        
        public boolean hasNext(
        ) {
            return this.delegate.hasNext();
        }
        
        public Map.Entry<K,M> next(
        ) {
            return new TypeSafeMarshallingEntry<K,U,M>(marshaller, this.delegate.next());
        }

        public void remove(
        ) {
            this.delegate.remove();
        }

    }

    //------------------------------------------------------------------------
    // Class TypeSafeEntry
    //------------------------------------------------------------------------
    
    /**
     * Type-Safe Entry
     */
    static class TypeSafeMarshallingEntry<K,U,M> implements Map.Entry<K,M> {

        TypeSafeMarshallingEntry(
            TypeSafeMarshaller<U, M> marshaller,
            java.util.Map.Entry<K, U> delegate
        ) {
            this.delegate = delegate;
            this.marshaller = marshaller;
        }

        /**
         * The delegate entry
         */
        private final Map.Entry<K, U> delegate;

        /**
         * The marshaller to be used
         */    
        private final TypeSafeMarshaller<U,M> marshaller;
        
        
        @Override
        public K getKey() {
            return delegate.getKey();
        }

        @Override
        public M getValue() {
            return marshaller.marshal(delegate.getValue());
        }

        @Override
        public M setValue(M value) {
            return this.marshaller.marshal(
                this.delegate.setValue(this.marshaller.unmarshal(value))
            );
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "TypeSafeMarshallingEntry [delegate=" + this.delegate + "]";
        }        
        
    }

}
