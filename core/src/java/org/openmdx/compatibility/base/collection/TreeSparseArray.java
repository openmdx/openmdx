/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TreeSparseArray.java,v 1.1 2008/02/18 13:34:06 hburger Exp $
 * Description: Implementation of SparseArray
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/18 13:34:06 $
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.compatibility.base.collection;

import java.io.Serializable;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * TreeSparseArray
 */
public class TreeSparseArray<E>
    extends AbstractSparseArray<E>
    implements Cloneable, Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 3688511012718719797L;


    /**
     * Constructs an empty sparse array.
     */
    public TreeSparseArray(
    ){
        super();
    }

    /**
     * Constructs a new sparse array containing the same entries as the given 
     * sparse array.
     *
     * @param   collection
     *          sparse array whose entries are to be placed into this sparse
     *          array
     */
    public TreeSparseArray(
        SparseArray<? extends E> collection
    ){
        setAll(collection);
    }

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param   collection
     *          a collection whose entries are to be placed into this sparse
     *          array
     */
    @SuppressWarnings("unchecked")
    public TreeSparseArray(
        Collection<? extends E> collection
    ){
        if(collection instanceof SparseArray){
            setAll((SparseArray<E>)collection);
        } else {
            addAll(collection);
        }
    }


    //------------------------------------------------------------------------
    // Implements SparseArray
    //------------------------------------------------------------------------
    
    /**
     * Returns a map view of this sparse array's populated elements. The map's
     * iterator will return the entries in ascending order. The map is backed
     * by the sparse array, so changes to the sparse array are reflected in
     * the map, and vice-versa. If the sparse array is modified while an
     * iteration over the map is in progress, the results of the iteration are
     * undefined. The map supports element removal, which removes the
     * corresponding entry from the sparse array, via the Iterator.remove, 
     * Map.remove, removeAll retainAll, and clear operations. It does not
     * support the add or addAll operations.
     *
     * @return  a map view of this sparse array's populated elements
     */
    public SortedMap<Integer,E> populationMap(
    ){
        return this.data;
    }


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /**
     * Returns a shallow copy of this TreeSparseArray instance. (The keys and
     * values themselves are not cloned.)
     *
     * @return  a shallow copy of this Map.
     */
    public Object clone(
    ){
        return new TreeSparseArray<E>(this);
    }


    //------------------------------------------------------------------------
    // Instance members
    //------------------------------------------------------------------------

    /**
     * The sparse array data is stored in a sorted map.
     */
    private final SortedMap<Integer,E> data = new TreeMap<Integer,E>();

}
