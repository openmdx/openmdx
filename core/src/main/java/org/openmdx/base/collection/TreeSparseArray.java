/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: TreeSparseArray 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2013, OMEX AG, Switzerland
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

import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;

/**
 * TreeSparseArray
 * 
 * @deprecated use {@link org.openmdx.kernel.collection.TreeSparseArray}
 */
@Deprecated
public class TreeSparseArray<E> extends org.openmdx.kernel.collection.TreeSparseArray<E> {

    /**
     * Constructor
     *  
     * @deprecated use {@link org.openmdx.kernel.collection.TreeSparseArray}
     */
    @Deprecated
    public TreeSparseArray() {
        super();
    }

    /**
     * Constructor 
     *
     * @param that another sparse array
     *  
     * @deprecated use {@link org.openmdx.kernel.collection.TreeSparseArray}
     */
    @Deprecated
    public TreeSparseArray(
        SortedMap<Integer,? extends E> that
    ){
        super(that);
    }

    /**
     * Constructor 
     *
     * @param that another sparse array
     *  
     * @deprecated use {@link org.openmdx.kernel.collection.TreeSparseArray}
     */
    @Deprecated
    public TreeSparseArray(
        Map<Integer,? extends E> that
    ){
        super(that);
    }

    /**
     * Constructor 
     *
     * @param list the source
     *  
     * @deprecated use {@link org.openmdx.kernel.collection.TreeSparseArray}
     */
    @Deprecated
    public TreeSparseArray(
        Collection<? extends E> list
    ){
        super(list);
    }

    /**
     * Implememts <code>Serializable</code>
     */
    private static final long serialVersionUID = 3594203027758389259L;

    
    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------

    /**
     * Clone this sparse array
     * 
     * @return the clone
     * 
     * @deprecated use {@link org.openmdx.kernel.collection.TreeSparseArray#clone()}
     */
    @Override
    @Deprecated
    public TreeSparseArray<E> clone(
    ){
        return new TreeSparseArray<>(this);
    }    
    
}
