/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: OrderingList.java,v 1.4 2009/02/24 18:55:52 hburger Exp $
 * Description: Ordering List
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/24 18:55:52 $
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
package org.openmdx.application.dataprovider.accessor;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import javax.jdo.JDOHelper;

import org.openmdx.base.query.Selector;


/**
 * Ordering List
 */
@SuppressWarnings("unchecked")
class OrderingList extends AbstractSequentialList<Object> {

    /**
     * The method accept must be overridden if selector is null.
     * 
     * @param list
     * @param selector
     */
    public OrderingList(
        List list,
        Selector selector,
        Comparator comparator
    ){
        this.source = list;
        this.selector = selector;
        this.comparator = comparator;
    }

    /**
     * 
     */
    protected final List source;

    /**
     * 
     */
    protected final Selector selector;

    /**
     * 
     */
    protected final Comparator comparator;
    
    /* (non-Javadoc)
     * @see java.util.AbstractSequentialList#listIterator(int)
     */
    @Override
    public ListIterator listIterator(
        int index
    ) {
        List<Object> selection = new ArrayList<Object>();
        if(this.selector == null) {
            selection.addAll(this.source);
        } else {
            for(Object candidate : this.source) {
                if(this.selector.accept(candidate)) {
                    selection.add(candidate);
                }
            }
        }
        Collections.sort(selection, this.comparator);
        return new OrderingListIterator(
            selection.listIterator(index)
        );
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
        if(this.selector == null) {
            return this.source.size();
        } else {
           int count = 0;
           for(Object candidate : this.source) {
               if(this.selector.accept(candidate)) {
                   count++;
               }
           }
           return count;
        }
    }
    
    
    //------------------------------------------------------------------------
    // Class SortedListIterator
    //------------------------------------------------------------------------

    /**
     * Sorted List Iterator
     */
    static class OrderingListIterator implements ListIterator {
        
        /**
         * Constructor 
         *
         * @param delegate
         */
        OrderingListIterator(
            ListIterator delegate
        ){
            this.delegate = delegate;
        }
        
        private static final String UNMODIFIABLE = "Query results are unmodifiable";
        /**
         * The value iterator
         */
        private final ListIterator delegate;

        /**
         * The iterator's current element
         */
        private Object current = null;
        
        /**
         * @param e
         * @see java.util.ListIterator#add(java.lang.Object)
         */
        public void add(Object e) {
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
        public Object next() {
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
        public Object previous() {
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
        public void set(Object e) {
            throw new UnsupportedOperationException(UNMODIFIABLE);
        }
        
    }
    
}
