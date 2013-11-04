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
import java.util.TreeMap;

import org.w3c.cci2.SortedMaps.AsSparseArray;

/**
 * TreeSparseArray
 */
public class TreeSparseArray<E>
    extends AsSparseArray<E>
    implements Cloneable
{

    /**
     * Constructor 
     *
     * @param delegate
     */
    public TreeSparseArray() {
        super(
            new TreeMap<Integer,E>()
        );
    }

    /**
     * Constructor 
     *
     * @param sparseArray
     */
    public TreeSparseArray(
        SortedMap<Integer,? extends E> sparseArray
    ){
        super(
            new TreeMap<Integer,E>(sparseArray)
        );
    }

    /**
     * Constructor 
     *
     * @param sparseArray
     */
    public TreeSparseArray(
        Map<Integer,? extends E> sparseArray
    ){
        super(
            new TreeMap<Integer,E>(sparseArray)
        );
    }

    /**
     * Constructor 
     *
     * @param sparseArray
     */
    public TreeSparseArray(
        Collection<? extends E> list
    ){
        this();
        int i = 0;
        for(E e : list){
            put(Integer.valueOf(i++), e);
        }
    }

    /**
     * Implememts <code>Serializable</code>
     */
    private static final long serialVersionUID = -4052326371211325827L;

    
    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.util.AbstractMap#clone()
     */
    @Override
    protected Object clone(
    ){
        return new TreeSparseArray<E>(this);
    }    
    
}
