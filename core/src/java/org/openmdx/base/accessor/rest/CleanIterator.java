/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: $
 * Description: CleanIterator 
 * Revision:    $Revision: $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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

import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.rest.AbstractContainer_1.Excluded;

/**
 * Clean Iterator
 */
class CleanIterator implements ListIterator<DataObject_1_0> {
    
    /**
     * Constructor 
     *
     * @param excluded
     * @param delegate
     */
    CleanIterator(
        Excluded excluded, 
        ListIterator<DataObject_1_0> delegate,
        int index
    ) {
        super();
        this.excluded = excluded;
        this.delegate = delegate;
        for(int i = index;i > 0;i--){
            this.next();
        }
    }
    
    private final Excluded excluded;
    private final ListIterator<DataObject_1_0> delegate;
    private int nextIndex = 0;
    private int previousIndex = -1;
    private DataObject_1_0 prefetched = null;
    
    /**
     * @param o
     * @see java.util.ListIterator#add(java.lang.Object)
     */
    @Override
    public void add(DataObject_1_0 o) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * @return
     * @see java.util.ListIterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        while(this.prefetched == null && this.delegate.hasNext()) {
            DataObject_1_0 candidate = this.delegate.next();
            if(!this.excluded.handles(candidate)) {
                this.prefetched = candidate;
            }
        }
        return this.prefetched != null;
    }
    
    /**
     * @return
     * @see java.util.ListIterator#hasPrevious()
     */
    @Override
    public boolean hasPrevious() {
        return this.previousIndex >= 0;
    }
    
    /**
     * @return
     * @see java.util.ListIterator#next()
     */
    @Override
    public DataObject_1_0 next() {
        if(this.hasNext()) {
            this.previousIndex = this.nextIndex++;
            DataObject_1_0 next = this.prefetched;
            this.prefetched = null;
            return next;
        } else {
            throw new NoSuchElementException();
        }
    }
    
    /**
     * @return
     * @see java.util.ListIterator#nextIndex()
     */
    @Override
    public int nextIndex() {
        return this.nextIndex;
    }
    
    /**
     * @return
     * @see java.util.ListIterator#previous()
     */
    @Override
    public DataObject_1_0 previous() {
        if(this.hasPrevious()) {
            DataObject_1_0 candidate = this.delegate.previous();
            while(this.excluded.handles(candidate)){
                candidate = this.delegate.previous();
            }
            this.nextIndex = this.previousIndex--;
            return candidate;
        } else {
            throw new NoSuchElementException();
        }
    }
    
    /**
     * @return
     * @see java.util.ListIterator#previousIndex()
     */
    @Override
    public int previousIndex() {
        return this.previousIndex;
    }
    
    /**
     * 
     * @see java.util.ListIterator#remove()
     */
    @Override
    public void remove() {
        this.delegate.remove();
    }
    
    /**
     * @param o
     * @see java.util.ListIterator#set(java.lang.Object)
     */
    @Override
    public void set(DataObject_1_0 o) {
        throw new UnsupportedOperationException();
    }
            
}