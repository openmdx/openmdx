/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Registry interface 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2010, OMEX AG, Switzerland
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

import java.util.Set;

/**
* Registry interface
*/
public interface Registry<K,V> { 

    /**
     * Registers an object unless an object is already registered for the given key.
     *
     * @param   key
     *          the key
     * @param   value
     *          the value
     *
     * @return  either the old value if it was present or the new one if it was absent 
     * 
     * @throws IllegalStateException if the registry is already closed
     */
    V putUnlessPresent(
        K key,
        V value
    );

    /**
     * Registers an object 
     *
     * @param   key
     *          the key
     * @param   value
     *          the value
     *
     * @return  <code>null</code> unless there is already another object registered with the given key
     * 
     * @throws IllegalStateException if the registry is already closed
     */
    V put(
        K key,
        V value
    );
    
    /**
     * Retrieve an object from the cache
     * 
     * @param   key
     *          the key
     * 
     * @return the cached object, or <code>null</code>
     * 
     * @throws IllegalStateException if the registry is already closed
     */
    V get(
        K key
    );

    /**
     * Remove an object from the cache
     * 
     * @param   key
     *          the key
     * 
     * @return the cached object, or <code>null</code>
     * 
     * @throws IllegalStateException if the registry is already closed
     */
    V remove(
        K key
    );
    
    /**
     * Retrieve the objects managed by the cache
     * 
     * @return the set of objects managed by the cache
     * 
     * @throws IllegalStateException if the registry is already closed
     */
    Set<V> values(
    );

    /**
     * Clears the cache
     * 
     * @throws IllegalStateException if the registry is already closed
     */
    void clear();
    
    /**
     * Closes the registry.
     * If the registry is already closed then invoking this method has no effect. 
     */
    void close();

}
