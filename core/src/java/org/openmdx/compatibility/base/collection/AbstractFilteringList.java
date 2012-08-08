/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractFilteringList.java,v 1.2 2008/03/21 18:45:23 hburger Exp $
 * Description: SPICE Collections: Chaining Lists
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:45:23 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.collection;

import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * @author hburger
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
@SuppressWarnings("unchecked")
abstract public class AbstractFilteringList extends AbstractSequentialList {

    protected AbstractFilteringList(
        List list
    ){
        this.list = list; 
    }
    
    /**
     * Tests whether candidate is a member of the collection.
     * 
     * @parame  candidate
     * 
     * @return true if the candidate is a member of the collection
     */
    protected abstract boolean accept(
        Object candidate
    );
    
    /**
     * Interceptable remove
     * 
     * @param object
     * @param iterator
     */
    protected void removeInternal(
        Object object,
        ListIterator iterator
    ){
        iterator.remove();
    }

    /* (non-Javadoc)
     * @see java.util.List#listIterator(int)
     */
    public ListIterator listIterator(int index) {
        return new FilteringListIterator(index);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#size()
     */
    public int size() {
        int size = 0;
        for (
            Iterator i = this.list.iterator();
            i.hasNext();
        ) if (accept(i.next())) size++;
        return size;
    }

    /**
     * 
     */
    protected final List list;

    
    //------------------------------------------------------------------------
    // Class FilteringListIterator
    //------------------------------------------------------------------------
    
    /**
     * 
     */
    class FilteringListIterator implements ListIterator {
    
        /**
         * Filtering Iterator
         */
        public FilteringListIterator(
            int index
        ) {
            this.iterator = list.listIterator();
            while (index > nextIndex()) next();
        }
    
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            while((!this.readAheadReady) && this.iterator.hasNext()) {
                this.readAheadElement = this.iterator.next(); 
                this.readAheadCount++;
                this.readAheadReady = accept(this.readAheadElement);
            }
            return this.readAheadReady;
        }
    
        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Object next() {
            if(!hasNext()) throw new NoSuchElementException(
                "End of list reached"
            );
            this.previousIndex = this.nextIndex++;
            return this.current = readAheadFlush();
        }
    
        /* (non-Javadoc)
         * @see java.util.ListIterator#hasPrevious()
         */
        public boolean hasPrevious() {
            return this.previousIndex >= 0;
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#previous()
         */
        public Object previous(
        ){
            if(!hasPrevious()) throw new NoSuchElementException(
                "Begin of list reached"
            );
            reposition();
            Object candidate = this.iterator.previous();
            while (!accept(candidate)) candidate = this.iterator.previous();
            this.nextIndex = this.previousIndex--;
            this.readAheadCount = 0;
            return this.current = candidate;
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#nextIndex()
         */
        public int nextIndex(
        ) {
            return this.nextIndex;
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#previousIndex()
         */
        public int previousIndex() {
            return this.previousIndex;
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#remove()
         */
        public void remove() {
            if(this.current == null) throw new IllegalStateException(
                "Iterator has no current element"
            );
            reposition();
            removeInternal(
                this.current,
                this.iterator
            );
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#set(java.lang.Object)
         */
        public void set(Object object) {
            check(object);
            reposition();
            this.iterator.set(object);
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#add(java.lang.Object)
         */
        public void add(Object object) {
            check(object);
            reposition();
            this.iterator.add(object);          
            this.nextIndex++;
            this.previousIndex++;
        }

        private Object readAheadFlush(
        ){
            Object object = this.readAheadElement;
            this.readAheadCount = 0;
            this.readAheadElement = null;
            this.readAheadReady = false;
            return object;
        }
        
        /**
         *
         */
        private void reposition(
        ){
            if(this.nextIndex == 0) throw new IllegalStateException(
                "No element fetched yet"
            );
            while (readAheadCount-- > 0) this.iterator.previous();
            readAheadFlush();
        }

        /**
         * 
         * @param object
         * 
         * @exception   IllegalArgumentException
         *              if the object is not acceptable by the filter
         */
        private void check (
            Object object
        ){
            if(!accept(object)) throw new IllegalArgumentException(
                "Member rejected by filter"
            );
        }

        /**
         * Delegate
         */
        protected final ListIterator iterator;  
    
        /**
         * Index of the element to be returned by next()
         */
        private int nextIndex = 0;  

        /**
         * Index of the element to be returned by previuos()
         */
        private int previousIndex = -1; 

        /**
         * 
         */
        Object readAheadElement = null;
        
        /**
         * 
         */
        Object current = null;
        
        /**
         * 
         */
        int readAheadCount = 0;
        
        /**
         * 
         */
        boolean readAheadReady = false;
        
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object that) {
            return this == that;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            // TODO Auto-generated method stub
            return super.hashCode();
        }

    }

}
