/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Set View
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
package org.openmdx.state2.aop1;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.exception.ServiceException;

/**
 * List View
 */
final class SetView<O,E> extends CollectionView<O,Set<E>,E> implements Set<E> {

    /**
     * Constructor 
     *
     * @param involvedStates
     * @param feature
     */
    private SetView (
        InvolvedMembers<O,Set<E>> members
    ){
        super(members);
    }

    /**
     * Set View Factory Method
     * 
     * @param involved
     * @param feature
     * 
     * @return e new Set view
     */
    static  SetView<DataObject_1_0,Object> newObjectSet(
        final Involved<DataObject_1_0> involved,
        final String feature
    ){
        return new SetView<DataObject_1_0,Object>(
            new InvolvedMembers<DataObject_1_0,Set<Object>>(
                involved,
                feature
            ) {
    
                @Override
                protected Set<Object> getMember(
                    DataObject_1_0 state
                ) throws ServiceException {
                    return state.objGetSet(feature);
                }
                
            }
        );
    }

    /**
     * Set View Factory Method
     * 
     * @param involved
     * @param feature
     * 
     * @return e new key set view
     */
    static  SetView<SortedMap<Integer,Object>,Integer> newKeySet(
        final Involved<SortedMap<Integer,Object>> involved,
        final String feature
    ){
        return new SetView<SortedMap<Integer,Object>,Integer>(
            new InvolvedMembers<SortedMap<Integer,Object>,Set<Integer>>(
                    involved,
                    feature
                ) {
        
                    @Override
                    protected Set<Integer> getMember(
                        SortedMap<Integer,Object> state
                    ) throws ServiceException {
                        return state.keySet();
                    }
                    
                }
          );
    }
    
    /* (non-Javadoc)
     * @see java.util.List#iterator()
     */
    public Iterator<E> iterator() {
        return new ViewIterator();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Set<?>) {
            Set<?> that = (Set<?>) obj;
            if(this.size() == that.size()) {
                for(Object member : this) {
                    if(!that.contains(member)) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = 0;
        for(Object member : this) {
            hash += member == null ? 0 : member.hashCode();
        }
        return hash;
    }


    //------------------------------------------------------------------------
    // Class ViewIterator
    //------------------------------------------------------------------------
    
    /**
     * View Iterator
     */
    class ViewIterator implements Iterator<E> {

        /**
         * Constructor 
         *
         * @param index
         */
        ViewIterator(
        ){
            Set<E> values = null;
            for(Set<E> delegate : members.getInvolved(AccessMode.FOR_QUERY)) {
                if(values == null) {
                    values = delegate;
                } else if(!values.equals(delegate)) {
                    throw new IllegalStateException(
                        "The underlying states have different values"
                    );
                }
            }
            if(values == null) {
                values = Collections.emptySet();
            } else {
                values = new HashSet<E>(values);
            }
            this.delegate = values.iterator();
        }

        /**
         * The delegate
         */
        private final Iterator<E> delegate;

        /**
         * The delegate
         */
        private E current = null;
        
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public E next() {
            return this.current = this.delegate.next();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            if(current == null) {
                throw new IllegalStateException("No current element");
            }
            for(Collection<E> delegate : members.getInvolved(AccessMode.FOR_UPDATE)) {
                delegate.remove(this.current);
            }
            this.current = null;
        }
        
    }

}