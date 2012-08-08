/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: FilteringList.java,v 1.2 2009/05/24 21:40:28 wfro Exp $
 * Description: SPICE Collections: Chaining Lists
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/24 21:40:28 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.accessor.rest;

import java.util.AbstractSequentialList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.exception.ServiceException;

/**
 * Filtering List
 */
abstract class FilteringList extends AbstractSequentialList<DataObject_1_0> {

    /**
     * The subclass must implement the Selector interface.
     * 
     * @param list
     * 
     * exception    ClassCastException if the subclass is not an instance
     *              of Selector
     */
    protected FilteringList(
    ){
    }
    
    /**
     * Retrieve list.
     *
     * @return Returns the list.
     */
    protected abstract List<DataObject_1_0> getSource();

	/**
     * Interceptable remove
     * 
     * @param object
     * @param iterator
     */
    protected void removeInternal(
        Object object,
        ListIterator<DataObject_1_0> iterator
    ) throws ServiceException {
        iterator.remove();
    }

    /* (non-Javadoc)
     * @see java.util.List#listIterator(int)
     */
    public ListIterator<DataObject_1_0> listIterator(int index) {
        return new FilteringListIterator(index);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#size()
     */
    public int size() {
        int size = 0;
        for(Object candidate : getSource()) {
            if(accept(candidate)) {
                size++;
            }
        }
        return size;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#isEmpty()
     */
    @Override
    public boolean isEmpty() {
    	List<?> source = getSource();
    	if(!source.isEmpty()) {
            for(Object candidate : source) {
                if(accept(candidate)) {
                    return false;
                }
            }
    	}
        return true;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object object) {
        return accept(object) && getSource().contains(object);
    }

    /**
     * Tells whether the object is acceptable as a member of the collection
     * 
     * @param candidate the candidate to be tested
     * 
     * @return <code>true</code> if the object is acceptable as a member of the collection
     */
    abstract protected boolean accept(
    	Object candidate
    );
    
    /**
     * Tells whether a filter should be applied to the candidates or not
     * 
     * @return <code>true</code> if no filter should be applied to the candidates 
     */
    protected boolean acceptAll (
    ){
        return false;   
    }


    //------------------------------------------------------------------------
    // Class FilteringListIterator
    //------------------------------------------------------------------------
    
    /**
     * 
     */
    class FilteringListIterator implements ListIterator<DataObject_1_0> {
    
        /**
         * Filtering Iterator
         */
        public FilteringListIterator(
            int index
        ){
        	List<DataObject_1_0> source = getSource();
            if(acceptAll()){
                this.iterator = source.listIterator(index);
                this.nextIndex = this.iterator.nextIndex();
                this.previousIndex = this.iterator.previousIndex();
            } else {
                this.iterator = source.listIterator();
                while (index > nextIndex()) next();
            }
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            while(!this.readAheadReady && this.iterator.hasNext()) {
                this.readAheadElement = this.iterator.next(); 
                this.readAheadCount++;
                this.readAheadReady = accept(this.readAheadElement);
            }
            return this.readAheadReady;
        }
    
        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public DataObject_1_0 next() {
            if(!hasNext()) {
            	throw new NoSuchElementException(
	                "End of list reached"
	            );
            }
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
        public DataObject_1_0 previous(
        ){
            if(!hasPrevious()) throw new NoSuchElementException(
                "Begin of list reached"
            );
            reposition();
            DataObject_1_0 candidate = this.iterator.previous();
            while (!FilteringList.this.accept(candidate)) {
                candidate = this.iterator.previous();
            }
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
            if(this.current == null) {
            	throw new IllegalStateException(
	                "Iterator has no current element"
	            );
            }
            reposition();
            try {
                removeInternal(
                    this.current,
                    this.iterator
                );
            } catch (ServiceException exception) {
                throw new UnsupportedOperationException(
                    exception
                );
            }
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#set(T)
         */
        public void set(DataObject_1_0 object) {
            check(object);
            reposition();
            this.iterator.set(object);
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#add(T)
         */
        public void add(DataObject_1_0 object) {
            check(object);
            reposition();
            this.iterator.add(object);          
            this.nextIndex++;
            this.previousIndex++;
        }

        private DataObject_1_0 readAheadFlush(
        ){
        	DataObject_1_0 object = this.readAheadElement;
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
            if(!FilteringList.this.accept(object)) {
            	throw new IllegalArgumentException(
	                "Member rejected by filter"
	            );
            }
        }

        //-------------------------------------------------------------------
        // Members
        //-------------------------------------------------------------------        
        protected final ListIterator<DataObject_1_0> iterator;  
        
        /**
         * Index of the element to be returned by next()
         */
        private int nextIndex = 0;  

        /**
         * Index of the element to be returned by previuos()
         */
        private int previousIndex = -1; 

        DataObject_1_0 readAheadElement = null;        
        DataObject_1_0 current = null;        
        int readAheadCount = 0;        
        boolean readAheadReady = false;
        
    }

}
