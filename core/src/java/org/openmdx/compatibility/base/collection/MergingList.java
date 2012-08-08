/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: MergingList.java,v 1.2 2008/03/21 18:45:24 hburger Exp $
 * Description: SPICE Collections: Merging List
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:45:24 $
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
import java.util.Comparator;
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
public class MergingList extends AbstractSequentialList {

    /**
     * The method accept must be overridden if selector is null.
     * 
     * @param list
     * @param selector
     */
    public MergingList(
        List x,
        List y,
        Comparator comparator
    ){
        this.lists = new List[]{x,y};
        this.comparator = comparator;
    }

    /* (non-Javadoc)
     * @see java.util.List#listIterator(int)
     */
    public ListIterator listIterator(int index) {
        return new MergingListIterator(index);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#size()
     */
    public int size() {
        int total = 0;
        for(
            int i = 0;
            i < this.lists.length;
            i++
        ){
            int s = this.lists[i].size();
            if(s == Integer.MAX_VALUE) return Integer.MAX_VALUE;
            total += s;
        }
        return total;
    }

	/* (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
	public boolean isEmpty() {
        for(
            int i = 0;
            i < this.lists.length;
            i++
        ) if(
        	! this.lists[i].isEmpty()
		) return false;
        return true;
	}
	
    /**
     * 
     */
    protected List[] lists;

    /**
     * 
     */
    protected Comparator comparator;

    
    //------------------------------------------------------------------------
    // Class MergingListIterator
    //------------------------------------------------------------------------
    
    /**
     * 
     */
    class MergingListIterator implements ListIterator {
    
        /**
         * Filtering Iterator
         */
        public MergingListIterator(
            int index
        ){
            if(lists[0].isEmpty()){
                this.iterators[0] = lists[0].listIterator();
                this.iterators[1] = lists[1].listIterator(index);
                this.nextIndex = this.iterators[1].nextIndex();
                this.previousIndex = this.iterators[1].previousIndex();
            } else if (lists[1].isEmpty()) {
                this.iterators[0] = lists[0].listIterator(index);
                this.iterators[1] = lists[1].listIterator();
                this.nextIndex = this.iterators[0].nextIndex();
                this.previousIndex = this.iterators[0].previousIndex();
            } else {
                this.iterators[0] = lists[0].listIterator();
                this.iterators[1] = lists[1].listIterator();
                for(
                    int i=0;
                    i < index;
                    i++
                ) next();
            }
        }
    
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return this.prefetched[0] || this.prefetched[1] || 
                this.iterators[0].hasNext() || this.iterators[1].hasNext();
        }   
    
        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Object next() {
            setDirection(true);
            for(
                int i = 0;
                i < lists.length;
                i++
            ) if(
                !this.prefetched[i] && (this.prefetched[i] = this.iterators[i].hasNext())
            ) this.values[i] = this.iterators[i].next();
            if(this.prefetched[0]){
                if(this.prefetched[1]){
                    if(comparator.compare(this.values[0], this.values[1]) <= 0){
                        this.current = 0;
                    } else {
                        this.current = 1;
                    }
                } else {
                    this.current = 0;
                }
            } else if (this.prefetched[1]){
                this.current = 1;
            } else {
                this.current = -1;
                throw new NoSuchElementException("End of list reached");
            } 
            this.previousIndex = this.nextIndex++;
            this.prefetched[this.current] = false;
            return this.values[this.current];
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
            setDirection(false);
            for(
                int i = 0;
                i < lists.length;
                i++
            ) if(
                !this.prefetched[i] && (this.prefetched[i] = this.iterators[i].hasPrevious())
            ) this.values[i] = this.iterators[i].previous();
            if(this.prefetched[0]){
                if(this.prefetched[1]){
                    if(comparator.compare(this.values[0], this.values[1]) > 0){
                        this.current = 0;
                    } else {
                        this.current = 1;
                    }
                } else {
                    this.current = 0;
                }
            } else if (this.prefetched[1]){
                this.current = 1;
            } else {
                this.current = -1;
                throw new NoSuchElementException("Beginning of list reached");
            } 
            this.nextIndex = this.previousIndex--;
            this.prefetched[this.current] = false;
            return this.values[this.current];
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
        public void remove(
        ) {
            getCurrent().remove();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#set(java.lang.Object)
         */
        public void set(Object object) {
            getCurrent().set(object);
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#add(java.lang.Object)
         */
        public void add(Object object) {
            throw new UnsupportedOperationException();
        }

        /**
         * Validate and return current
         */
        private ListIterator getCurrent (
        ){
            if(this.current == -1) throw new IllegalStateException(
                "Iterator has no current element"
            );
            return this.iterators[this.current];
        }

        /**
         * 
         */
        private void setDirection(
            boolean ascending
        ){
            if(this.ascending != ascending) { // change direction       
                for(
                    int i = 0;
                    i < lists.length;
                    i++
                ) if (this.prefetched[i]) {
                    if(ascending){
                        this.iterators[i].next();
                    } else {
                        this.iterators[i].previous();
                    }
                }
                this.ascending = ascending;
            }
        }
        
        /**
         * Delegate
         */
        protected final ListIterator[] iterators = new ListIterator[lists.length];
    
        /**
         * Index of the element to be returned by next()
         */
        private int nextIndex = 0;  

        /**
         * Index of the element to be returned by previous()
         */
        private int previousIndex = -1; 

        /**
         * Value
         */
        private Object[] values = new Object[lists.length];

        /**
         * Direction
         */
        private boolean ascending;
        
        /**
         * Has read ahead
         */
        private boolean[] prefetched = new boolean[lists.length];
        
        /**
         * Direction
         */
        private int current = -1;
        
    }

}
