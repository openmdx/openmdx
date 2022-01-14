/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Merging List 
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
import java.util.NoSuchElementException;

import java.util.function.Consumer; 

import javax.jdo.FetchPlan;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.rest.AbstractContainer_1.BatchingList;
import org.openmdx.base.accessor.rest.AbstractContainer_1.Excluded;
import org.openmdx.base.accessor.rest.AbstractContainer_1.Included;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;

/**
 * Merging List
 */
final class MergingList extends JoiningList implements BatchingCollection {

    /**
     * Constructor 
     *
     * @param dirty
     * @param stored
     * @param excluded 
     */
    protected MergingList(
        Included dirty,
        BatchingList stored,
        Excluded excluded
    ){
        super(dirty,stored,excluded);
    }

    @Override
    public ListIterator<DataObject_1_0> listIterator(
        int index
    ) {
        return listIterator(index, null);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.AbstractContainer_1.BatchingIterable#listIterator(int, javax.jdo.FetchPlan)
     */
    @Override
    public ListIterator<DataObject_1_0> listIterator(
        int index,
        FetchPlan fetchPlan
    ) {
        return new MergingIterator(index, fetchPlan);
    }

    @Override
    public void processAll(
        Consumer<DataObject_1_0> consumer
    ) {
        final MergingConsumer mergingConsumer = new MergingConsumer(consumer, this.included);
        this.stored.processAll(new FilteringConsumer(mergingConsumer, getExcluded()));
        mergingConsumer.processRemaining();
    }
    
    /**
     * Merging Iterator
     */
    private final class MergingIterator implements ListIterator<DataObject_1_0> {

        /**
         * Constructor 
         *
         * @param index
         * @param fetchPlan the (optional) fetch plan
         */
        MergingIterator (
            int index, 
            FetchPlan fetchPlan
        ) {
            this.nextIndex = index;
            this.previousIndex = index - 1;
            this.dirtyIterator = MergingList.this.included.listIterator();
            // Iterate up to the requested index if there are dirty or excluded elements
            if(
                this.dirtyIterator.hasNext() || 
                !MergingList.this.getExcluded().isEmpty()
            ) {
                this.cleanIterator = MergingList.this.getExcluded().isEmpty() ? MergingList.this.getStored().listIterator(
                    0,
                    fetchPlan
                ) : new CleanIterator(
                    MergingList.this.getExcluded(),
                    MergingList.this.getStored().listIterator(0, fetchPlan),
                    0
                );
                for(int i = index; i > 0; i--) {
                    try {
                        this.next();
                    } 
                    catch (NoSuchElementException exception) {
                        throw Throwables.initCause(
                            new IndexOutOfBoundsException(
                                "The given index is greater or equal to the collection's size"
                            ),
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter("index", index),
                            new BasicException.Parameter("size", index - i)
                        );
                    }
                }
            }
            // Use list iterator in case there are no dirty elements
            else {
                this.cleanIterator = MergingList.this.getStored().listIterator(index, fetchPlan); 
            }
        }
        
        private int previousIndex;
        private int nextIndex;

        private final ListIterator<DataObject_1_0> dirtyIterator;
        private final ListIterator<DataObject_1_0> cleanIterator;
        
        private DataObject_1_0 nextDirty = null;
        private DataObject_1_0 nextClean = null;
        private DataObject_1_0 previousDirty = null;
        private DataObject_1_0 previousClean = null;
        private boolean useDirty;            

        @Override
        public boolean hasNext(
        ) {
            return 
                this.nextDirty != null ||
                this.nextClean != null ||
                this.dirtyIterator.hasNext() ||
                this.cleanIterator.hasNext();
        }

        @Override
        public DataObject_1_0 next(
        ) {
            if(this.nextDirty == null && this.dirtyIterator.hasNext()) {
                this.nextDirty = this.dirtyIterator.next();
            }
            if(this.nextClean == null && this.cleanIterator.hasNext()) {
                this.nextClean = this.cleanIterator.next();
            }
            if(this.nextDirty == null && this.nextClean == null) {
                throw new NoSuchElementException("End of clean and dirty lists reached");
            }
            this.useDirty =
                this.nextDirty == null ? false :
                this.nextClean == null ? true :
                MergingList.this.comparator.compare(this.nextDirty, this.nextClean) <= 0;
            this.previousIndex = this.nextIndex++;
            DataObject_1_0 current;
            if(this.useDirty) {
                current = this.nextDirty;
                this.nextDirty = null;
            } else {
                current = this.nextClean;
                this.nextClean = null;
            }
            return current;
        }

        @Override
        public boolean hasPrevious() {
            return this.previousIndex > 0;
        }

        @Override
        public DataObject_1_0 previous(
        ) {
            if(this.previousDirty == null && this.dirtyIterator.hasPrevious()) {
                this.previousDirty = this.dirtyIterator.previous();
            }
            if(this.previousClean == null && this.cleanIterator.hasPrevious()) {
                this.previousClean = this.cleanIterator.previous();
            }
            if(this.previousDirty == null && this.previousClean == null) {
                throw new NoSuchElementException("Beginning of clean and dirty lists reached");
            }
            this.useDirty =
                this.previousDirty == null ? false :
                this.previousClean == null ? true :
                MergingList.this.comparator.compare(this.previousDirty, this.previousClean) > 0;
            this.nextIndex = this.previousIndex--;
            DataObject_1_0 current;
            if(this.useDirty) {
                current = this.previousDirty;
                this.previousDirty = null;
            } 
            else {
                current = this.previousClean;
                this.previousClean = null;
            }
            return current;
        }

        @Override
        public int nextIndex(
        ) {
            return this.nextIndex;
        }

        @Override
        public int previousIndex(
        ) {
            return this.previousIndex;
        }

        @Override
        public void remove(
        ) {
            (this.useDirty ? this.dirtyIterator : this.cleanIterator).remove();
        }

        @Override
        public void set(
            DataObject_1_0 object
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(
            DataObject_1_0 object
        ) {
            throw new UnsupportedOperationException();
        }

    }

}
