/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Marshalling Map
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
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

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.spi.PersistenceCapable;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.ExceptionListenerMarshaller;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.marshalling.ReluctantUnmarshalling;
import org.openmdx.kernel.exception.BasicException;

/**
 * A Marshalling Map
 * <p>
 * The marshaller is applied to the delegate's values, but not to its keys.
 */
public class MarshallingMap<K,V>
  extends AbstractMap<K,V>
  implements Serializable 
{

    /**
     * Constructor 
     *
     * @param map
     * @param unmarshalling
     */
    protected MarshallingMap(
        Map<K,V> map, 
        Unmarshalling unmarshalling 
    ){
        this.unmarshalling = unmarshalling;
        this.map = map;
        this.marshaller = (Marshaller) this;
    }
    
    /**
     * Constructor
     * 
     * @param marshaller
     * @param map
     * @param unmarshalling 
     */
    public MarshallingMap(
        Marshaller marshaller,
        Map<K,V> map, 
        Unmarshalling unmarshalling 
    ) {
        this.marshaller = marshaller;
        this.map = map;
        this.unmarshalling = unmarshalling;
    }
  
    /**
     * Constructor
     * 
     * @param marshaller
     * @param map
     */
    public MarshallingMap(
        Marshaller marshaller,
        Map<K,V> map 
    ) {
        this(
            new ExceptionListenerMarshaller(marshaller), 
            map, 
            marshaller instanceof ReluctantUnmarshalling ? Unmarshalling.RELUCTANT : Unmarshalling.EAGER
        );
    }
    
        
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3834309540194956341L;

    /**
     * 
     */
    protected final Unmarshalling unmarshalling;    

    /**
     * 
     */    
    private final Map<K,V> map;

    /**
     * 
     */
    protected final Marshaller marshaller;
    
    /**
     * This method may be overridden by a sub-class for dynamic delegation
     * 
     * @return the delegate
     */
    protected Map<K,V> getDelegate(){
        return this.map;
    }
    
    
    //------------------------------------------------------------------------
    // Implements Map
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        this.getDelegate().clear();
    }
      
    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return this.getDelegate().containsKey(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(
        Object value
    ) {
        try {
            switch(this.unmarshalling) {
                case RELUCTANT:
                    return super.containsValue(value);
                case EAGER: default: 
                    return this.getDelegate().containsValue(
                        this.marshaller.unmarshal(value)
                    );
            }
        } catch(ServiceException e) {
            return false;
        }        
    }

    /**
     * 
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet(
    ) {
        try {
            return new MarshallingSet<Map.Entry<K, V>>(
                new MapEntryMarshaller<K,V>(this.marshaller),
                this.getDelegate().entrySet(),
                this.unmarshalling
            );
        } catch (NullPointerException exception) {
            return null; 
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public V get(
        Object key
    ) {
        Object value = getDelegate().get(key); 
        if(value instanceof PersistenceCapable && JDOHelper.getPersistenceManager(value) == null) {
            return null;
        } else if(this.marshaller instanceof ExceptionListenerMarshaller) {
            ExceptionListenerMarshaller marshaller = (ExceptionListenerMarshaller) this.marshaller;
            try {
                return (V) marshaller.getDelegate().marshal(value);
            } catch (ServiceException exception) {
                if(exception.getExceptionCode() != BasicException.Code.NOT_FOUND) {
                    marshaller.exceptionThrown(exception);
                }
                return null;
            }
        } else {
            try {
                return (V) this.marshaller.marshal(value);
            } catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }        
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return getDelegate().isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<K> keySet() {
        return map.keySet();
    }
  
    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public V put(
        K key, 
        V value
    ) {
        try {
            return (V) this.marshaller.marshal(
                getDelegate().put(key, (V)this.marshaller.unmarshal(value))
            );
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public V remove(
        Object key
    ) {
        try {
            return (V) this.marshaller.marshal(
                getDelegate().remove(key)
            );
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return getDelegate().size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    @Override
    public Collection<V> values() {
        return new MarshallingCollection<V>(
            this.marshaller,
            getDelegate().values(), 
            this.unmarshalling
        );
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#toString()
     */
    @Override
    public String toString() {
        return getDelegate().toString();
    }

    
    //------------------------------------------------------------------------
    // Class MapEntryMarshaller
    //------------------------------------------------------------------------

    /**
     * Map Entry Marshaller
     */
    static class MapEntryMarshaller<K,V>
        implements Marshaller 
     {

        /**
         * Constructor
         * @param marshaller
         */      
        public MapEntryMarshaller(
            Marshaller marshaller
        ) {
            this.marshaller = marshaller;
        }
    
        /**
         * 
         */
        public Object marshal(
            Object source
        ) {
            return source instanceof Map.Entry<?,?> ? new MarshallingMapEntry<K,V>(
                this.marshaller,
                source
            ) : source;
        }
    
        /**
         * 
         */
        @SuppressWarnings("unchecked")
        public Object unmarshal(
            Object source
        ) {
            return source instanceof MarshallingMapEntry ?
                ((MarshallingMapEntry<K,V>)source).getEntry() :
                source;
        }
            
        /**
         * 
         */
        private final Marshaller marshaller;
  
    }


    //------------------------------------------------------------------------
    // Class MarshallingMapEntry
    //------------------------------------------------------------------------

    /**
     * Marshalling Map Entry
     */
    static class MarshallingMapEntry<K,V> 
        implements Map.Entry<K,V> 
    {
    
        /**
         * Constructor
         * 
         * @param marshaller
         * @param entry
         */
        @SuppressWarnings("unchecked")
        public MarshallingMapEntry(
            Marshaller marshaller,
            Object entry
        ) {
            this.marshaller = marshaller;
            this.entry = (Entry<K, Object>) entry;
        }

        /**
         * 
         * @return
         */
        public Map.Entry<K,?> getEntry(
        ) {
            return this.entry;
        }
    
        /**
         * 
         */
        public K getKey(
        ) {
            return this.entry.getKey();      
        }

        /**
         * 
         */    
        @SuppressWarnings("unchecked")
        public V getValue(
        ) {
            try {
                return (V) this.marshaller.marshal(
                    this.entry.getValue()
                );
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }            
        }

        /**
         * 
         */    
        public V setValue(
            V value
        ) {
            try {
                V oldValue = this.getValue();
                this.entry.setValue(
                    this.marshaller.unmarshal(value)
                );
                return oldValue;
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }            
        }
        
        /**
         * 
         */
        private final Map.Entry<K,Object> entry;

        /**
         * 
         */    
        private final Marshaller marshaller;

    }

}
