/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PopulationIterator.java,v 1.4 2009/01/05 13:47:16 wfro Exp $
 * Description: PopulationIterator interface
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/05 13:47:16 $
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
package org.openmdx.base.collection;

import java.util.Iterator;

/**
 * Allows to iterate over a sparse array's population
 */
public interface PopulationIterator<E>
    extends Iterator<E>
{

    /**
     * Returns true if this population iterator has more elements when
     * traversing the sparse array in the forward direction. (In other words,
     * returns true if next would return an element rather than throwing an
     * exception.)
     *
     * @return  true if the population iterator has more elements when
     *          traversing the sparse array in the forward direction.
     */
    boolean hasNext();

    /**
     * Returns the next element in the sparse array. This method may be called
     * repeatedly to iterate through the sparse array.
     *
     * @return  the next element in the sparse array.
     *
     * @exception   NoSuchElementException
     *              if the iteration has no next element.
     */
    E next();

    /**
     * Returns the index of the element that would be returned by a subsequent
     * call to next. (Returns sparse array end if the population iterator is
     * at the end of the sparse array.)
     *
     * @return  the index of the element that would be returned by a
     *          subsequent call to next, or sparse array end if population
     *          iterator is at end of the sparse array.
     */
    int nextIndex();

    /**
     * Removes from the sparse array the last element that was returned by
     * next (optional operation).
     * <p>
     * <code>remove()</code> is equivalent to <code>set(null);</code>.
     *
     * @exception   UnsupportedOperationException
     *              if the remove operation is not supported by this
     *              population iterator.
     * @exception   IllegalStateException
     *              if either nextIndex has been called or next has not been
     *              called.
     */
    public void remove();

    /**
     * Replaces the last element returned by next or previous with the
     * specified element (optional operation).
     *
     * @param   o
     *          the element with which to replace the last element returned by
     *          next.
     *
     * @exception   UnsupportedOperationException
     *              if the set operation is not supported by this population
     *              iterator.
     * @exception   ClassCastException
     *              if the class of the specified element prevents it from
     *              being added to this sparse array.
     * @exception   IllegalArgumentException
     *              if some aspect of the specified element prevents it from
     *              being added to this sparse array.
     * @exception   IllegalStateException
     *              if either nextIndex has been called or next has not been
     *              called.
     */
    void set(E o);

}
