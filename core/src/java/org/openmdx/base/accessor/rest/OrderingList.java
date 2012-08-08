/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: OrderingList.java,v 1.2 2009/05/24 21:40:28 wfro Exp $
 * Description: Ordering List
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/24 21:40:28 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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

import javax.jdo.JDOHelper;

import org.openmdx.base.accessor.cci.DataObject_1_0;


/**
 * Ordering List
 */
abstract class OrderingList extends FilteringList {

    /**
     * The method accept must be overridden if selector is null.
     * 
     * @param list
     * @param selector
     */
    public OrderingList(
        Comparator<DataObject_1_0> comparator
    ){
        this.comparator = comparator;
    }

    /**
     * 
     */
    private final Comparator<DataObject_1_0> comparator;
        
	/* (non-Javadoc)
     * @see java.util.AbstractSequentialList#listIterator(int)
     */
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

    
    //------------------------------------------------------------------------
    // Class SortedListIterator
    //------------------------------------------------------------------------

    /**
     * Sorted List Iterator
     */
    static class OrderingListIterator implements ListIterator<DataObject_1_0> {
        
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
        
        private static final String UNMODIFIABLE = "Query results are unmodifiable";
        /**
         * The value iterator
         */
        private final ListIterator<DataObject_1_0> delegate;

        /**
         * The iterator's current element
         */
        private DataObject_1_0 current = null;
        
        /**
         * @param e
         * @see java.util.ListIterator#add(java.lang.Object)
         */
        public void add(DataObject_1_0 e) {
            throw new UnsupportedOperationException(UNMODIFIABLE);
        }

        /**
         * @return
         * @see java.util.ListIterator#hasNext()
         */
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        /**
         * @return
         * @see java.util.ListIterator#hasPrevious()
         */
        public boolean hasPrevious() {
            return this.delegate.hasPrevious();
        }

        /**
         * @return
         * @see java.util.ListIterator#next()
         */
        public DataObject_1_0 next() {
            return this.current = this.delegate.next();
        }

        /**
         * @return
         * @see java.util.ListIterator#nextIndex()
         */
        public int nextIndex() {
            return this.delegate.nextIndex();
        }

        /**
         * @return
         * @see java.util.ListIterator#previous()
         */
        public DataObject_1_0 previous() {
            return this.current = this.delegate.previous();
        }

        /**
         * @return
         * @see java.util.ListIterator#previousIndex()
         */
        public int previousIndex() {
            return this.delegate.previousIndex();
        }

        /**
         * 
         * @see java.util.ListIterator#remove()
         */
        public void remove() {
            this.delegate.remove();
            JDOHelper.getPersistenceManager(this.current).deletePersistent(this.current);
            this.current = null;
        }

        /**
         * @param e
         * @see java.util.ListIterator#set(java.lang.Object)
         */
        public void set(DataObject_1_0 e) {
            throw new UnsupportedOperationException(UNMODIFIABLE);
        }

    }
    
}
