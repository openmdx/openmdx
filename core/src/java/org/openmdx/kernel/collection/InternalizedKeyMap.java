/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Internalized Key Map 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland
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
package org.openmdx.kernel.collection;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Set;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.uses.java.util.IdentityHashMap;

/**
 * The keys are restricted to <code>String</code>, <code>Short</code>, 
 * <code>Integer</code> and <code>Long</code> values.
 */
public class InternalizedKeyMap<K,V> extends AbstractMap<K, V> {

    /**
     * Constructor 
     */
    public InternalizedKeyMap(){
        delegate = new IdentityHashMap<K, V>();
    }

    /**
     * Constructs a new, empty map with the specified expected maximum size.
     * Putting more than the expected number of key-value mappings into
     * the map may cause the internal data structure to grow, which may be
     * somewhat time-consuming.
     *
     * @param expectedMaxSize the expected maximum size of the map
     * @throws IllegalArgumentException if <tt>expectedMaxSize</tt> is negative
     */
    public InternalizedKeyMap(
        int expectedMaxSize
    ){
        delegate = new IdentityHashMap<K, V>(expectedMaxSize);
    }

    /**
     * Tells whether the internalization shall be trusted or validated
     */
    private final static boolean TRUST_INTERNALIZATION = Boolean.getBoolean("org.openmdx.kernel.collection.InternalizedKeyMap.TrustInternalization");
    		
    /**
     * The delegate map
     */
    private final IdentityHashMap<K,V> delegate;
    
    /* (non-Javadoc)
     * @see java.util.AbstractMap#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return this.delegate.entrySet();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return 
            InternalizedKeys.isInternalizable(key) &&
            this.delegate.containsKey(InternalizedKeys.internalize(key));
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#get(java.lang.Object)
     */
    @Override 
    public V get(Object key) {
        return InternalizedKeys.isInternalizable(key) ?
            this.delegate.get(InternalizedKeys.internalize(key)) :
            null;
    }

    /**
     * Works around internalization problems
     * 
     * @param newKey
     * 
     * @return the (maybe recovered) key
     */
    private K validatedKey(
        K newKey
    ){
        if(newKey == null){
        	return newKey;
        } else {
        	K oldKey = this.delegate.findEqualKey(newKey);
        	if(oldKey == null) {
        		return newKey;
        	} else {
        		if (oldKey != newKey) {
	                SysLog.error(
	                    "Internalization returns different objects over time",
	                    BasicException.newStandAloneExceptionStack(
	                        BasicException.Code.DEFAULT_DOMAIN,
	                        BasicException.Code.ASSERTION_FAILURE,
	                        "Internalization returns different objects over time",
	                        new BasicException.Parameter("value", oldKey, newKey),
	                        new BasicException.Parameter("class", oldKey.getClass().getName(), newKey.getClass().getName()),
	                        new BasicException.Parameter("hashCodes", Integer.valueOf(System.identityHashCode(oldKey)), Integer.valueOf(System.identityHashCode(newKey)))
	                    )
	                );
	        	}
	        	return oldKey;
        	}
        }
    }
    
    /* (non-Javadoc)
     * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public V put(K key, V value) {
        K internalizedKey = InternalizedKeys.internalize(key);
        return this.delegate.put(
        	TRUST_INTERNALIZATION ? internalizedKey : validatedKey(internalizedKey), 
            value
        );
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#remove(java.lang.Object)
     */
    @Override
    public V remove(Object key) {
        return InternalizedKeys.isInternalizable(key) ?
            this.delegate.remove(InternalizedKeys.internalize(key)) :
            null;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        return this.delegate.containsValue(value);
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#keySet()
     */
    @Override
    public Set<K> keySet() {
        return this.delegate.keySet();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#values()
     */
    @Override
    public Collection<V> values() {
        return this.delegate.values();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#size()
     */
    @Override
    public int size() {
        return this.delegate.size();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#clear()
     */
    @Override
    public void clear() {
        this.delegate.clear();
    }
    
}
