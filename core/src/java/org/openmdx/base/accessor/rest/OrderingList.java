/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: OrderingList 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.kernel.jdo.ReducedJDOHelper;

/**
 * Ordering List
 */
abstract class OrderingList extends FilteringList {

    /**
     * Constructor 
     *
     * @param comparator
     */
    OrderingList(
        Comparator<DataObject_1_0> comparator
    ){
        this.comparator = comparator;
    }

    private final Comparator<DataObject_1_0> comparator;

    @Override
    public ListIterator<DataObject_1_0> listIterator(
        int index
    ) {
        if(this.comparator == null) {
            return super.listIterator(index);
        } else {
            List<DataObject_1_0> selection = new ArrayList<DataObject_1_0>();
            for(
                ListIterator<DataObject_1_0> i = super.listIterator(0);
                i.hasNext();
            ){
                selection.add(i.next());
            }
            Collections.sort(selection, this.comparator);
            return new OrderingListIterator(
                selection.listIterator(index)
            );
        }
    }

    Comparator<DataObject_1_0> getComparator(){
        return this.comparator;
    }
    
    /**
     * Ordering List Iterator
     */
    private class OrderingListIterator implements ListIterator<DataObject_1_0> {

        /**
         * Constructor 
         *
         * @param delegate
         */
        OrderingListIterator(
            ListIterator<DataObject_1_0> delegate
        ){
            this.delegate = delegate;
        }

        /**
         * 
         */
        private final ListIterator<DataObject_1_0> delegate;
        
        /**
         * 
         */
        private DataObject_1_0 current = null;

        @Override
        public void add(DataObject_1_0 e) {
            throw new UnsupportedOperationException("Query results are unmodifiable");
        }

        @Override
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        @Override
        public boolean hasPrevious() {
            return this.delegate.hasPrevious();
        }

        @Override
        public DataObject_1_0 next() {
            return this.current = this.delegate.next();
        }

        @Override
        public int nextIndex() {
            return this.delegate.nextIndex();
        }

        @Override
        public DataObject_1_0 previous() {
            return this.current = this.delegate.previous();
        }

        @Override
        public int previousIndex() {
            return this.delegate.previousIndex();
        }

        @Override
        public void remove() {
            this.delegate.remove();
            ReducedJDOHelper.getPersistenceManager(this.current).deletePersistent(this.current);
            this.current = null;
        }

        @Override
        public void set(DataObject_1_0 e) {
            throw new UnsupportedOperationException("Query results are unmodifiable");
        }

    }

}