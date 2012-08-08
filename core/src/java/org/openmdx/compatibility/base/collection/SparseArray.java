/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: SparseArray.java,v 1.1 2008/02/18 13:34:06 hburger Exp $
 * Description: SPICE SparseArray interface
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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

/**
 * Sparsely Populated Array interface
 */
public interface SparseArray<E>
    extends Collection<E>
{

    /**
     * Returns the number of (non-null) elements in this sparse array. If this
     * sparse array contains more than Integer.MAX_VALUE elements or if its
     * size can't be determined yet it returns Integer.MAX_VALUE.
     *
     * @return  the number of elements in this sparse array.
     */
    int size();
     
    /**
     * Returns an iterator over the elements in this sparse array (in proper
     * sequence).
     *
     * @return  an iterator over the elements in this sparse array (in proper
     *          sequence).
     */
    Iterator<E> iterator();

    /**
     * A list backed up by the sparse array. Its size() is the sparse array's
     * lastIndex() + 1 and the sparse array's un-populated positions are
     * represented as null values.
     *  
     * @return      a list representing the sparse array
     */
    List<E> asList();

    /**
     * Return the index of the first populated element in the sparse array or 
     * -1 if the sparse array is empty.
     *
     * @return      the index of the first populated element in the sparse
     *              array.
     */
    int start(); 

    /**
     * Return the index where add() would insert an element.
     *
     * @return      the index of the last populated element in the sparse 
     *              array incremented by one unless the sparse array is empty;
     *              if it is empty <code>fromIndex</code> is returned for
     *              subarrays, 0 otherwise.
     */
    int end(); 

    /**
     * Returns the element at the specified position in this sparse array.
     *
     * @param   index
     *          index of element to return.
     *
     * @return  the element at the specified position in this sparse array.
     *
     * @exception   IndexOutOfBoundsException
     *              if the index is out of range (index < 0).
     */
    E get(
        int index
    );

    /**
     * Replaces the element at the specified position in this sparse array
     * with the specified element (optional operation).
     * <p>
     * <code>set(i,null)</code> is equivalent to <code>remove(i)</code>.
     *
     * @param   index
     *          index of element to replace.
     * @param   element
     *          element to be stored at the specified position.
     *
     * @return  the element previously at the specified position.
     *
     * @exception   UnsupportedOperationException
     *              if the set method is not supported by this sparse array.
     * @exception   ClassCastException - if the class of the specified element
     *              prevents it from being added to this sparse array.
     * @exception   IllegalArgumentException
     *              if some aspect of the specified element prevents it from
     *              being added to this sparse array.
     * @exception   IndexOutOfBoundsException
     *              if the index is out of range (index < 0).
     */
    E set(
        int index,
        E element
    );

    /**
     * Copies all of the entries from the specified sparse array to this 
     * sparse array (optional operation). These settings will replace any 
     * setings that this sparse array had for any of the indices currently in
     * the specified sparse array.
     *
     * @param   t
     *          Entries to be stored in this sparse array.
     *
     * @exception   UnsupportedOperationException
     *              if the setAll method is not supported by this sparse
     *              array.
     * @exception   ClassCastException
     *              if the class of a value in the specified sparse array
     *              prevents it from being stored in this sparse array.
     * @exception   IllegalArgumentException
     *              some aspect of a value in the specified sparse array
     *              prevents it from being stored in this sparse array.
     */
    void setAll(
        SparseArray<? extends E> t
    );

    /**
     * Appends the specified element to the end of this sparse array (optional
     * operation). 
     * <p>
     * Adding a <code>null</code> value to a sparse array does nothing.
     * <p>
     * Sparse arrays that support this operation may place limitations on what
     * elements may be added to this sparse array. In particular, some sparse 
     * arrays will impose restrictions on the type of elements that may be
     * added. Sparse array classes should clearly specify in their
     * documentation any restrictions on what elements may be added.
     * 
     * @param   o
     *          element to be appended to this sparse array.
     *
     * @return  true if o != null; false otherwise.
     *
     * @exception   UnsupportedOperationException
     *              if the add method is not supported by this sparse array.
     * @exception   ClassCastException
     *              if the class of the specified element prevents it from
     *              being added to this sparse array.
     * @exception   IllegalArgumentException
     *              if some aspect of this element prevents it from being
     *              added to this collection.
     */
    boolean add(    
        E o
    );

    /**
     * Removes the element at the specified position in this sparse array
     * (optional operation). Returns the element that was removed from the
     * sparse array.
     * <p>
     * <code>remove(i)</code> is equivalent to <code>set(i,null)</code>.
     *
     * @param   index
     *          the index of the element to removed.
     * 
     * @return  the element previously at the specified position;
     *          or null the specified position was not occupied before.
     *
     * @exception   UnsupportedOperationException
     *              if the remove method is not supported by this sparse
     *              array.
     * @exception   IndexOutOfBoundsException
     *              if the index is out of range (index < 0).
     */
    E remove(
        int index
    );

    /**
     * Returns the index in this sparse array of the first occurrence of the
     * specified element, or -1 if this sparse array does not contain this
     * element. More formally, returns the lowest index i such that 
     * <code>(o==null ? get(i)==null : o.equals(get(i)))</code>, or -1 if
     * there is no such index.
     *
     * @param   o
     *          element to search for.
     *
     * @return  the index in this sparse array of the first occurrence of the
     *          specified element, or -1 if this sparse array does not contain
     *          this element.
     */
    int indexOf(
        Object o
    );

    /**
     * Returns an iterator for the populated elements in this sparse array (in
     * proper sequence). The first index is start() and the last end() - 1
     * respectively. The indices are not contiguous.
     *
     * @return  an iterator over the populated elements in the sparse array
     *
     * @see #populationIterator(int)
     * @see #start()
     * @see #end()
     */
    PopulationIterator<E> populationIterator(
    );

    /**
     * Returns an iterator for the populated elements in this sparse array (in
     * proper sequence). The first index is greater or equal fromIndex and the
     * last end() - 1 respectively. The indices are not contiguous.
     *
     * @return  an iterator over the populated elements in the sparse array
     *
     * @see #populationIterator()
     * @see #end()
     */
    PopulationIterator<E> populationIterator(
        int fromIndex
    );

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
    SortedMap<Integer, E> populationMap(
    );

    /**
     * Returns a view of the portion of this sparse array between the
     * specified fromIndex, inclusive, and toIndex, exclusive. (If fromIndex
     * and toIndex are equal, the returned sparse array is empty.) The
     * returned sparse array is backed by this sparse array, so changes in the
     * returned sparse array are reflected in this sparse array, and
     * vice-versa. The returned sparse array supports all of the optional
     * sparse array operations supported by this sparse array.
     * <p>
     * This method eliminates the need for explicit range operations (of the
     * sort that commonly exist for arrays). Any operation that expects a
     * sparse array can be used as a range operation by passing a subArray
     * view instead of a whole sparse array. For example, the following idiom
     * removes a range of elements from a sparse array: 
     * <p>
     * <code>list.subArray(from, to).clear();</code>
     * <p>
     * Similar idioms may be constructed for indexOf and lastIndexOf, and all
     * of the algorithms in the Collections class can be applied to a subArray.
     * <p>
     * The semantics of the sparse array returned by this method become
     * undefined if the backing sparse array (i.e., this sparse array) is
     * structurally modified in any way other than via the returned sparse
     * array. (Structural modifications are those that change the size of this 
     * sparse array, or otherwise perturb it in such a fashion that iterations
     * in progress may yield incorrect results.)
     *
     * @param   fromIndex
     *          low endpoint (inclusive) of the subArray.
     * @param   toIndex
     *          high endpoint (exclusive) of the subArray.
     *
     * @return  a view of the specified range within this sparse array.
     *
     * @exception   IndexOutOfBoundsException
     *              for an illegal endpoint index value (fromIndex < 0 ||
     *              fromIndex > toIndex).
     */ 
    SparseArray<E> subArray(
        int fromIndex,
        int toIndex
    );

}
