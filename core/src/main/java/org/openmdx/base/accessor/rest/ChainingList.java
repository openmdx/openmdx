/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Chaining List 
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

import java.util.ListIterator;

import java.util.function.Consumer; 

import javax.jdo.FetchPlan;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.rest.AbstractContainer_1.BatchingList;
import org.openmdx.base.accessor.rest.AbstractContainer_1.Excluded;
import org.openmdx.base.accessor.rest.AbstractContainer_1.Included;

/**
 * Chaining List
 */
final class ChainingList extends JoiningList implements BatchingCollection {

    /**
     * Constructor 
     *
     * @param dirty
     * @param stored
     * @param excluded
     */
    ChainingList(
        Included dirty,
        BatchingList stored,
        Excluded excluded
    ){
        super(dirty, stored, excluded); 
    }

    @Override
    public ListIterator<DataObject_1_0> listIterator(
        int index
    ) {
        return listIterator(index, null);
    }

    @Override
    public ListIterator<DataObject_1_0> listIterator(
        int index,
        FetchPlan fetchPlan
    ) {
        return new ChainingIterator(index, fetchPlan);
    }
    
    @Override
    public void processAll(
        Consumer<DataObject_1_0> consumer
    ) {
        for(DataObject_1_0 element : this.included) {
            consumer.accept(element);
        }
        stored.processAll(new FilteringConsumer(consumer, getExcluded()));
    }
    
    /**
     * Chaining Iterator
     */
    private class ChainingIterator implements ListIterator<DataObject_1_0>{

        /**
         * Constructor 
         *
         * @param index
         * @param fetchPlan
         */
        ChainingIterator (
            int index, 
            FetchPlan fetchPlan
        ){
            this.fetchPlan = fetchPlan;
            this.nextIndex = index;
            this.previousIndex = index - 1;
            this.dirtySize = ChainingList.this.included.size();
        }

        private int previousIndex;
        private int nextIndex;
        private int dirtySize;

        private ListIterator<DataObject_1_0> dirtyIterator = null;
        private ListIterator<DataObject_1_0> cleanIterator = null;
        private ListIterator<DataObject_1_0> currentIterator = null;
        private final FetchPlan fetchPlan;

        private ListIterator<DataObject_1_0> getIterator(
            int index
        ){
            if(index < this.dirtySize) {
                if(this.dirtyIterator == null) {
                    this.dirtyIterator = included.listIterator(this.nextIndex);  
                }
                return this.dirtyIterator;
            } else {
                if(this.cleanIterator == null){
                    this.cleanIterator = ChainingList.this.excluded.isEmpty() ? stored.listIterator(
                        this.nextIndex - this.dirtySize,
                        this.fetchPlan
                    ) : new CleanIterator(
                        ChainingList.this.excluded, 
                        ChainingList.this.stored.listIterator(
                            0, 
                            this.fetchPlan
                        ), 
                        this.nextIndex - this.dirtySize
                    );
                }
                return this.cleanIterator;
            }
        }

        @Override
        public boolean hasNext() {
            return this.getIterator(this.nextIndex).hasNext();
        }

        @Override
        public DataObject_1_0 next(
        ) {
            DataObject_1_0 current = (this.currentIterator = this.getIterator(this.nextIndex)).next();
            this.previousIndex = this.nextIndex++;
            return current;
        }

        @Override
        public boolean hasPrevious() {
            return this.getIterator(this.previousIndex).hasPrevious();
        }

        @Override
        public DataObject_1_0 previous() {
            DataObject_1_0 current = (this.currentIterator = this.getIterator(this.previousIndex)).previous();
            this.nextIndex = this.previousIndex--;
            return current;
        }

        @Override
        public int nextIndex() {
            return this.nextIndex;
        }

        @Override
        public int previousIndex() {
            return this.previousIndex;
        }

        @Override
        public void remove() {
            this.currentIterator.remove();
            if(this.currentIterator == this.dirtyIterator) {
                this.dirtySize--;
            }
        }

        @Override
        public void set(DataObject_1_0 object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(DataObject_1_0 object) {
            throw new UnsupportedOperationException();
        }

    }

}
