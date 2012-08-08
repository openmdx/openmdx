/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractListIterator.java,v 1.2 2008/09/02 09:50:28 hburger Exp $
 * Description: SPICE Collections: Abstract List Iterator
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/02 09:50:28 $
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
package org.openmdx.compatibility.base.collection;

import java.util.ListIterator;

/**
 * Abstract List Iterator
 */
public abstract class AbstractListIterator<E> implements ListIterator<E> {

    /**
     * 
     */
    protected AbstractListIterator(
    ){
        super();
    }

    protected abstract ListIterator<E> getDelegate(
    );
    
    /* (non-Javadoc)
     * @see java.util.ListIterator#nextIndex()
     */
    public int nextIndex() {
        return getDelegate().nextIndex();
    }

    /* (non-Javadoc)
     * @see java.util.ListIterator#previousIndex()
     */
    public int previousIndex() {
        return getDelegate().previousIndex();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        getDelegate().remove();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        return getDelegate().hasNext();
    }

    /* (non-Javadoc)
     * @see java.util.ListIterator#hasPrevious()
     */
    public boolean hasPrevious() {
        return getDelegate().hasPrevious();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public E next() {
        this.currentIndex = getDelegate().nextIndex();
        return current = getDelegate().next();
    }

    /* (non-Javadoc)
     * @see java.util.ListIterator#previous()
     */
    public E previous() {
        this.currentIndex = getDelegate().previousIndex();
        return current = getDelegate().previous();
    }

    /* (non-Javadoc)
     * @see java.util.ListIterator#add(java.lang.Object)
     */
    public void add(E arg0) {
        getDelegate().add(arg0);
    }

    /* (non-Javadoc)
     * @see java.util.ListIterator#set(java.lang.Object)
     */
    public void set(E arg0) {
        getDelegate().set(arg0);
    }

    /**
     * 
     */
    protected E current = null;

    /**
     * 
     */
    protected int currentIndex;
    
}
