/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: FilteringList 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.openmdx.base.accessor.cci.DataObject_1_0;

/**
 * Filtering List
 */
abstract class FilteringList extends AbstractSequentialList<DataObject_1_0> {

    /**
     * Constructor 
     */
    FilteringList() {
        super();
    }

    /**
     * Retrieve list.
     *
     * @return Returns the list.
     */
    protected abstract Collection<? extends DataObject_1_0> getSource();

    @Override
    public ListIterator<DataObject_1_0> listIterator(int index) {
        return new FilteringListIterator(
            this.getSource(),
            index
        );
    }

    @Override
    public int size() {
        int size = 0;
        final Collection<? extends DataObject_1_0> source = this.getSource();
        if(!source.isEmpty()) {
            for(Object candidate : source) {
                if(this.accept(candidate)) {
                    size++;
                }
            }
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        final Collection<? extends DataObject_1_0> source = this.getSource();
        if(!source.isEmpty()) {
            for(Object candidate : source) {
                if(this.accept(candidate)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean contains(Object object) {
        return this.accept(object) && this.getSource().contains(object);
    }

    /**
     * Tells whether the object is acceptable as a member of the collection
     * 
     * @param candidate the candidate to be tested
     * 
     * @return <code>true</code> if the object is acceptable as a member of the collection
     */
    protected boolean accept(
        Object candidate
    ){
        if(this.acceptAll()) {
            return true;
        } else {
            throw new UnsupportedOperationException(
                "accept() must be overridden unless acceptAll() is true"
            );
        }
    }

    /**
     * Tells whether a filter should be applied to the candidates or not
     * 
     * @return <code>true</code> if no filter should be applied to the candidates 
     */
    protected boolean acceptAll (
    ){
        return false;   
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
     * Filtering List Iterator
     */
    private class FilteringListIterator implements ListIterator<DataObject_1_0> {

        /**
         * Constructor 
         *
         * @param candidates
         * @param index
         */
        FilteringListIterator(
            Collection<? extends DataObject_1_0> candidates,
            int index
        ){
            this.candidates = candidates;
            final List<DataObject_1_0> elements;
            if(candidates.isEmpty()) {
                elements = Collections.emptyList();
            } else if(FilteringList.this.acceptAll()){
                elements = new ArrayList<DataObject_1_0>(candidates);
            } else {
                elements = new ArrayList<DataObject_1_0>(candidates.size());
                for(DataObject_1_0 candidate : candidates) {
                    if(FilteringList.this.accept(candidate)) {
                        elements.add(candidate);
                    }
                }
            }
            this.delegate = elements.listIterator(index);
        }

        private final Collection<? extends DataObject_1_0> candidates;
        private final ListIterator<DataObject_1_0> delegate;  
        private DataObject_1_0 current;

        @Override
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        @Override
        public DataObject_1_0 next() {
            return this.current = this.delegate.next();
        }

        @Override
        public boolean hasPrevious() {
            return this.delegate.hasPrevious();
        }

        @Override
        public DataObject_1_0 previous(
        ){
            return this.current = this.delegate.previous();
        }

        @Override
        public int nextIndex(
        ) {
            return this.delegate.nextIndex();
        }

        @Override
        public int previousIndex() {
            return this.delegate.previousIndex();
        }

        @Override
        public void remove() {
            this.delegate.remove(); // throws IllegalStateException if necessary
            if(this.current.jdoIsPersistent()) {
                this.current.jdoGetPersistenceManager().deletePersistent(this.current);
            } else {
                this.candidates.remove(current);
            }
            this.current = null;
        }

        @Override
        public void set(DataObject_1_0 object) {
            throw new UnsupportedOperationException("Query result can't be changed");
        }

        @Override
        public void add(DataObject_1_0 object) {
            throw new UnsupportedOperationException("Query result can't be extended");
        }

    }

}