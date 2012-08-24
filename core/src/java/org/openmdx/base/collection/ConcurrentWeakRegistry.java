/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Concurrent Weak Registry
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.base.collection;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Concurrent Weak Registry
 */
public class ConcurrentWeakRegistry<K,V> implements Registry<K, V> {

    /**
     * The concurrent map holding the cache's references
     */
    private ConcurrentMap<K,WeakReference<V>> delegate = new ConcurrentHashMap<K,WeakReference<V>>();

    /**
     * The value reference queue
     */
    private ReferenceQueue<V> queue = new ReferenceQueue<V>();
    
    /**
     * The value collection
     */
    private final Set<V> values = new AbstractSet<V>() {

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<V> iterator(
        ) {
            return new ValueIterator<V>(
                getDelegate().values().iterator()
            );
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return getDelegate().size();
        }
        
    };

    /**
     * Retrieve the delegate and evict its stale entries if required
     * 
     * @return the thread-safe delegate
     */
    protected ConcurrentMap<K,WeakReference<V>> getDelegate(
    ){
        boolean hasStaleEntries = false;
        while(this.queue.poll() != null) {
            hasStaleEntries = true;
        }
        if(hasStaleEntries) {
            for(
                Iterator<? extends Reference<V>> i = this.delegate.values().iterator();
                i.hasNext();
            ){
                Reference<V> r = i.next();
                if(r.get() == null) {
                    i.remove();
                }
            }
        }
        return this.delegate;
    }
    

    //------------------------------------------------------------------------
    // Implements Registry
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.Cache#clear()
     */
//  @Override
    public void clear() {
        //
        // No need to evict stale entries before clearance
        //
        this.delegate.clear();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.collection.Registry#close()
     */
//  @Override
    public void close() {
        clear();
        this.delegate = null;
        this.queue = null;
    }


    /* (non-Javadoc)
     * 
     * @see org.openmdx.base.collection.Cache#get(java.lang.Object)
     */
//  @Override
    public V get(K key) {
        WeakReference<V> v = getDelegate().get(key); 
        return v == null ? null : v.get();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.Cache#remove(java.lang.Object)
     */
//  @Override
    public V remove(K key) {
        WeakReference<V> v = getDelegate().remove(key); 
        return v == null ? null : v.get();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.Cache#putIfAbsent(java.lang.Object, java.lang.Object)
     */
//  @Override
    public V putUnlessPresent(K key, V value) {
        WeakReference<V> v = new WeakReference<V>(value, this.queue);
        ConcurrentMap<K,WeakReference<V>> delegate = getDelegate();
        WeakReference<V> c = delegate.putIfAbsent(key, v);
        if(c == null) {
            return value;
        } else {
            V concurrent = c.get();
            if(concurrent == null) {
                delegate.put(key, v);
                return value;
            } else {
                return concurrent;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.Cache#put(java.lang.Object, java.lang.Object)
     */
//  @Override
    public V put(K key, V value) {
        WeakReference<V> v = new WeakReference<V>(value, this.queue);
        WeakReference<V> c = getDelegate().put(key, v);
        return c == null ? null : c.get();
    }


    /**
     * Retrieve the objects managed by the cache
     * *
     * @return a collection with the objects managed by the cache
     */
//  @Override
    public Set<V> values(
    ){
        return this.values;
    }
    
    
    //------------------------------------------------------------------------
    // Class ValueIterator
    //------------------------------------------------------------------------
    
    /**
     * Value Iterator
     */
    static class ValueIterator<V> implements Iterator<V> {
        
        /**
         * Constructor 
         *
         * @param delegate
         */
        public ValueIterator(
            Iterator<WeakReference<V>> delegate
        ){
            this.delegate = delegate;
        }

        /**
         * The reference iterator
         */
        private final Iterator<WeakReference<V>> delegate;
        
        /**
         * The prefetched value
         */
        private V next = null;
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
    //  @Override
        public boolean hasNext() {
            while(this.next == null && this.delegate.hasNext()) {
                this.next = this.delegate.next().get();
            }
            return this.next != null;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
    //  @Override
        public V next() {
            if(hasNext()) {
                V next = this.next;
                this.next = null;
                return next;
            } else {
                throw new NoSuchElementException();
            }
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
    //  @Override
        public void remove() {
            this.delegate.remove();
        }
        
    }        

}