/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ChainingList.java,v 1.1 2008/11/04 10:00:10 hburger Exp $
 * Description: SPICE Collections: Chaining Lists
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/04 10:00:10 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.transport.delegation;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.openmdx.kernel.log.SysLog;

/**
 * Chaining List
 */
public class ChainingList<E> 
    extends AbstractSequentialList<E> 
    implements Evictable
{

    /**
     * 
     */
    public ChainingList(
        List<E>[] lists
    ){
        this.lists = lists;
    }

    /* (non-Javadoc)
     * @see java.util.List#listIterator(int)
     */
    public ListIterator<E> listIterator(
        int index
    ) {
        return new ChainingIterator(index);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#size()
     */
    public int size(
    ) {
        List<Integer> segmentSizes = SysLog.isTraceOn() ? new ArrayList<Integer>() : null;
        int size = 0;
        for(
            int i = this.lists.length - 1;
            i >= 0;
            i--
        ){
            int segmentSize = this.lists[i].size();
            if(segmentSizes != null)segmentSizes.add(new Integer(segmentSize));
            if(segmentSize == Integer.MAX_VALUE){
                size = Integer.MAX_VALUE;
                break;
            }
            size += segmentSize;
        } 
        SysLog.trace("Segment sizes", segmentSizes);
        return size;
    }

    /**
     * 
     */
    protected List<E>[] lists;

    /**
     * 
     */
    public List<E>[] getDelegate(
    ){
        return this.lists;
    }

    
    //------------------------------------------------------------------------
    // Implements Evictable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.delegation.Evictable#evict()
     */
    public void evict() {
        for(List<E> delegate : this.lists) {
            if(delegate instanceof Evictable) {
                ((Evictable)delegate).evict();
            }
        }
    }
    
    
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.util.AbstractCollection#toString()
     */
    public String toString(
    ){
        StringBuilder text = new StringBuilder(
            getClass().getName()
        );
        try {
            text.append(
                ": (delegating to "
            ).append(
                this.lists.length 
            ).append(
                "lists)"
            ).toString();
        } catch (Exception exception) {
            text.append(
                "// "
            ).append(
                exception.getMessage()
            );
        }
        return text.toString();
    }
    
    
    //--------------------------------------------------------------------------
    // Class ChainingIterator
    //--------------------------------------------------------------------------

    /**
     * 
     * @author hburger
     *
     * To change the template for this generated type comment go to
     * Window>Preferences>Java>Code Generation>Code and Comments
     */
    @SuppressWarnings("unchecked")
    class ChainingIterator implements ListIterator<E>{

        ChainingIterator (
            int nextIndex
        ){
            this.limits[0] = 0;
            int[] sizes = new int[lists.length -1];
            for(
                int i = 1, total = 0;
                i < lists.length;
                i++
            ) this.limits[i] = total += sizes[i-1] = lists[i-1].size();
            this.limits[lists.length] = Integer.MAX_VALUE;
            for(
                int i = 0;
                i < lists.length;
                i++
            ) {
                this.iterators[i] = 
                    this.limits[i+1] < nextIndex ? lists[i].listIterator(sizes[i]) :
                    this.limits[i] > nextIndex ? lists[i].listIterator(0) :
                    lists[currentIterator = i].listIterator(nextIndex - limits[i]);
            }
        }
        
        /* (non-Javadoc)
         * @see java.util.ListIterator#hasNext()
         */
        public boolean hasNext(
        ) {
            for(
                int i = this.currentIterator;
                i < this.iterators.length;
                i++
            ) if (
                this.iterators[i].hasNext()
            ) return true;
            return false;
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#next()
         */
        public E next(
        ) {
            while (!this.iterators[this.currentIterator].hasNext()) {
                if(this.currentIterator >= this.iterators.length - 1) {
                    throw new NoSuchElementException("End of last list reached");
                }
                this.currentIterator++;
            }
            return this.iterators[this.currentIterator].next();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#hasPrevious()
         */
        public boolean hasPrevious() {
            for(
                int i = this.currentIterator;
                i >= 0;
                i--
            ) if (
                this.iterators[i].hasPrevious()
            ) return true;
            return false;
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#previous()
         */
        public E previous() {
            while (!this.iterators[this.currentIterator].hasPrevious()) {
                if(
                    this.currentIterator <= 0
                ) throw new NoSuchElementException(
                    "Begin of first list reached"
                );
                this.currentIterator--;
            }
            return this.iterators[this.currentIterator].previous();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#nextIndex()
         */
        public int nextIndex() {
            return 
                this.limits[this.currentIterator] + 
                this.iterators[this.currentIterator].nextIndex();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#previousIndex()
         */
        public int previousIndex() {
            return 
                this.limits[this.currentIterator] + 
                this.iterators[this.currentIterator].previousIndex();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#remove()
         */
        public void remove() {
            this.iterators[this.currentIterator].remove();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#set(java.lang.Object)
         */
        public void set(E object) {
            this.iterators[this.currentIterator].set(object);
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#add(java.lang.Object)
         */
        public void add(E object) {
            this.iterators[this.currentIterator].add(object);
        }

        /**
         * 
         */
        private int[] limits = new int[lists.length+1];
                
        /**
         * 
         */
        private ListIterator<E>[] iterators = new ListIterator[lists.length];

        /**
         * 
         */
        int currentIterator = - 1;
        
    }

}
