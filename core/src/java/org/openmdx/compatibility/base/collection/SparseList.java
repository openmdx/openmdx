/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: SparseList.java,v 1.7 2008/02/08 16:52:21 hburger Exp $
 * Description: Sparsely Populated List Interface
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/08 16:52:21 $
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

import java.util.List;
import java.util.ListIterator;

/**
 * Sparsely Populated List Interface
 */
public interface SparseList<E> 
    extends List<E>
{

    /**
     * Returns a list iterator of the populated elements in this list (in
     * proper sequence). The indices start at firstIndex(), end at lastIndex()
     * and are not contiguous.
     *
     * @return  an iterator over the populated elements in the list
     *
     * @see org.openmdx.compatibility.base.collection.SparseList#firstIndex()
     * @see org.openmdx.compatibility.base.collection.SparseList#lastIndex()
     */
    ListIterator<E> populationIterator();

    /**
     * An unmodifiable list containing all the populated elements. Its indices
     * are contiguous and start with 0.
     *
     * @return      a list containing the populated elements only
     */
    List<E> population();

    /**
     * Return the index of the first populated element in the list.
     *
     * @return      the index of the first populated element in the list.
     */
    int firstIndex(); 

    /**
     * Return the index of the last populated element in the list.
     *
     * @return      the index of the last populated element in the list.
     */
    int lastIndex(); 

}