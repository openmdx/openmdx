/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: $
 * Description: KeySet 
 * Revision:    $Revision: $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2018, OMEX AG, Switzerland
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

package org.openmdx.base.accessor.rest;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;

/**
 * Key Set
 */
class KeySet extends AbstractSet<String> {

    /**
     * Constructor 
     * 
     * @param delegate 
     */
    KeySet(
    	Container_1_0 delegate
    ) {
        this.delegate = delegate;
    }

    /**
     * The delegate
     */
    private final Container_1_0 delegate;
    
    /* (non-Javadoc)
     * @see java.util.AbstractCollection#iterator()
     */
    @Override
    public Iterator<String> iterator() {
        return new KeyIterator(
            this.delegate.entrySet().iterator()
        );
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
        return this.delegate.size();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object o) {
        return this.delegate.containsKey(o);
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    /**
     * Break the List contract to avoid round-trips
     */
    @Override
    public boolean equals(
        Object that
    ) {
        return this == that;
    }

    /**
     * Break the List contract to avoid round-trips
     */
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    /**
     * Break the List contract to avoid round-trips
     */
    @Override
    public String toString(
    ){
        return this.getClass().getSimpleName() + " of " + this.delegate;
    }

    
    //--------------------------------------------------------------------
    // Class KeyIterator
    //--------------------------------------------------------------------
    
    /**
     * Key Iterator
     */
    private static class KeyIterator implements Iterator<String> {
        
        /**
         * Constructor 
         *
         * @param delegate
         */
        KeyIterator(
            Iterator<Map.Entry<String, DataObject_1_0>> delegate
        ){
            this.delegate = delegate;
        }
        
        /**
         * An entry set iterator
         */
        private final Iterator<Map.Entry<String, DataObject_1_0>> delegate;

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        @Override
        public String next() {
            return this.delegate.next().getKey();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            this.delegate.remove();
        }
        
    }
    
}