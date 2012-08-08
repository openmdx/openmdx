/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractSparseArray.java,v 1.1 2008/02/18 13:34:05 hburger Exp $
 * Description: SPICE Abstract SparseArray Implementation
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/18 13:34:05 $
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

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;

/**
 * This class provides a skeletal implementation of the SparseArray interface
 * to minimize the effort required to implement this interface backed by a
 * data store.
 * <p>
 * The programmer needs only to extend this class and provide an
 * implementation for the populationMap() method.
 * <p>
 * The programmer should generally provide a void (no argument) and collection
 * constructor, as per the recommendation in the Collection interface
 * specification.
 * 
 * The documentation for each non-abstract methods in this class describes its
 * implementation in detail. Each of these methods may be overridden if the
 * collection being implemented admits a more efficient implementation. 
 */
public abstract class AbstractSparseArray<E>
    implements SparseArray<E>
{

    
    /**
     * Constructs an empty sparse array.
     */
    protected AbstractSparseArray(
    ){
        super();
    }

    /**
     * The lower bound is relevant for the end(), add(Object element) and
     * asList() methods.
     *
     * @return  this sparse array's lower bound
     *
     * @see end()
     * @see asList()
     * @see add(Object)
     */
    protected int lowerBound(){
        return 0;
    }

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
    abstract public SortedMap<Integer,E> populationMap(
    );


    //------------------------------------------------------------------------
    // Implements SparseArray
    //------------------------------------------------------------------------
    
    /**
     * Returns the number of (non-null) elements in this sparse array. If this
     * sparse array contains more than Integer.MAX_VALUE elements or if its
     * size can't be determined yet it returns Integer.MAX_VALUE.
     * <p>
     * This implementaion returns the population map's size. 
     *
     * @return  the number of elements in this sparse array.
     */
    public int size(
    ){
        return populationMap().size();
    }
     
    /**
     * Returns an iterator over the elements in this sparse array (in proper
     * sequence).
     * <p>
     * This implementaion returns an iterator over the population map's
     * values. 
     *
     * @return  an iterator over the elements in this sparse array (in proper
     *          sequence).
     */
    public Iterator<E> iterator(
    ){
        return populationMap().values().iterator();
    }

    /**
     * A list backed up by the sparse array. Its size() is the sparse array's
     * lastIndex() + 1 and the sparse array's un-populated positions are
     * represented as null values.
     *  
     * @return      a list representing the sparse array
     */
    public List<E> asList(
    ){
        return new ValueList();
    }

    /**
     * Return the index of the first populated element in the sparse array or 
     * -1 if the sparse array is empty.
     * <p>
     * This implementation returns the population map's first key unless it
     * is empty.
     *
     * @return      the index of the first populated element in the sparse
     *              array.
     */
    public int start(
    ){
        return populationMap().isEmpty() ? 
            -1 : 
            populationMap().firstKey().intValue();
    }

    /**
     * Return the index where add() would insert an element.
     * <p>
     * This implementation returns the population map's last key unless it
     * is empty.
     *
     * @return      the index of the last populated element in the sparse 
     *              array incremented by one unless the sparse array is empty;
     *              if it is empty <code>fromIndex</code> is returned for
     *              subarrays, 0 otherwise.
     */
    public int end(){
        return populationMap().isEmpty() ? 
            lowerBound() : 
            populationMap().lastKey().intValue() + 1;
    }

    /**
     * Returns the element at the specified position in this sparse array.
     * <p>
     * This implementation returns the corresponding element from the
     * population map.
     *
     * @param   index
     *          index of element to return.
     *
     * @return  the element at the specified position in this sparse array.
     *
     * @exception   IndexOutOfBoundsException
     *              if the index is out of range (index < 0).
     */
    public E get(
        int index
    ){
        return populationMap().get(new Integer(index));
    }

    /**
     * Replaces the element at the specified position in this sparse array
     * with the specified element (optional operation).
     * <p>
     * <code>set(i,null)</code> is equivalent to <code>remove(i)</code>.
     * <p>
     * This implementation puts the corresponding element into the
     * population map unless it is null.
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
    public E set(
        int index,
        E element
    ){
        return element == null ?
            remove(index) :
            populationMap().put(new Integer(index), element);
    }

    /**
     * Copies all of the entries from the specified sparse array to this 
     * sparse array (optional operation). These settings will replace any 
     * setings that this sparse array had for any of the indices currently in
     * the specified map.
     * <p>
     * This implementation calls the set(int index, Object element) method
     * for each entry.
     *
     * @param   t
     *          Entries to be stored in this map.
     *
     * @exception   UnsupportedOperationException
     *              if the sttAll method is not supported by this sparse
     *              array.
     * @exception   ClassCastException
     *              if the class of a value in the specified sparse array
     *              prevents it from being stored in this sparse array.
     * @exception   IllegalArgumentException
     *              some aspect of a key or value in the specified map
     *              prevents it from being stored in this map.
     * @exception   NullPointerException
     *              this map does not permit null keys or values, and the
     *              specified key or value is null.
     */
    public void setAll(
        SparseArray<? extends E> t
    ){
        for (
            PopulationIterator<? extends E> i = t.populationIterator();
            i.hasNext();
        ) set(i.nextIndex(), i.next());
    }

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
     * <p>
     * This implementation puts the element at position end() into the sparse
     * array unless it is null.
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
    public boolean add(
        E o
    ){
        boolean modified = o != null;
        if(modified) set(end(), o);
        return modified;
    }

    /**
     * Removes the element at the specified position in this sparse array
     * (optional operation). Returns the element that was removed from the
     * sparse array.
     * <p>
     * <code>remove(i)</code> is equivalent to <code>set(i,null)</code>.
     * <p>
     * This implementation removes the element from the population.
     *
     * @param   index
     *          the index of the element to removed.
     * 
     * @return  the element previously at the specified position.
     *
     * @exception   UnsupportedOperationException
     *              if the remove method is not supported by this sparse
     *              array.
     * @exception   IndexOutOfBoundsException
     *              if the index is out of range (index < 0).
     */
    public E remove(int index){
        return populationMap().remove(new Integer(index));
    }

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
    public int indexOf(
        Object o
    ){
        for(Map.Entry<Integer,E> e : populationMap().entrySet()) {
            Object v = e.getValue();
            if(
                o == null ? v == null : o.equals(v)
            ) return e.getKey().intValue();
        }
        return -1;
    }
    
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
    public PopulationIterator<E> populationIterator(
    ){
        return new PopulationIteratorImpl();
    }

    /**
     * Returns an iterator for the populated elements in this sparse array (in
     * proper sequence). The first index is greater or equal fromIndex and the
     * last end() - 1 respectively. The indices are not contiguous.
     * <p>
     * This implementation iterates over the tail array starting at fromIndex.
     *
     * @return  an iterator over the populated elements in the sparse array
     *
     * @see #populationIterator()
     * @see #end()
     */
    public PopulationIterator<E> populationIterator(
        int fromIndex
    ){
        return new SubArray(
            populationMap(),
            fromIndex
        ).populationIterator();
    }

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
     * The semantics of this list returned by this method become undefined if
     * the backing list (i.e., this list) is structurally modified in any way
     * other than via the returned list. (Structural modifications are those
     * that change the size of this list, or otherwise perturb it in such a
     * fashion that iterations in progress may yield incorrect results.)
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
    public SparseArray<E> subArray(
        int fromIndex,
        int toIndex
    ){
        return new SubArray(
            populationMap(),
            fromIndex,
            toIndex
        );
    }


    //------------------------------------------------------------------------
    // Implements Collection
    //------------------------------------------------------------------------
    
    // Query Operations

    /**
     * Returns <tt>true</tt> if this collection contains no elements.<p>
     * 
     * This implementation checks whether the population map is empty.
     *
     * @return <tt>true</tt> if this collection contains no elements.
     */
    public boolean isEmpty() {
        return populationMap().isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this collection contains the specified
     * element.  More formally, returns <tt>true</tt> if and only if this
     * collection contains at least one element <tt>e</tt> such that
     * <tt>(o==null ? e==null : o.equals(e))</tt>.<p>
     * 
     * This implementation checks whether the object is among the population
     * map's values.
     *
     * @param o object to be checked for containment in this collection.
     * @return <tt>true</tt> if this collection contains the specified element.
     */
    public boolean contains(Object o) {
        return populationMap().values().contains(o);
    }

    /**
     * Returns an array containing all of the elements in this collection.  If
     * the collection makes any guarantees as to what order its elements are
     * returned by its iterator, this method must return the elements in the
     * same order.  The returned array will be "safe" in that no references to
     * it are maintained by the collection.  (In other words, this method must
     * allocate a new array even if the collection is backed by an Array).
     * The caller is thus free to modify the returned array.<p>
     *
     * This implementation allocates the array to be returned, and iterates
     * over the elements in the collection, storing each object reference in
     * the next consecutive element of the array, starting with element 0.
     *
     * @return an array containing all of the elements in this collection.
     */
    public Object[] toArray() {
        return populationMap().values().toArray();
    }

    /**
     * Returns an array with a runtime type is that of the specified array and
     * that contains all of the elements in this collection.  If the
     * collection fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this collection.<p>
     *
     * If the collection fits in the specified array with room to spare (i.e.,
     * the array has more elements than the collection), the element in the
     * array immediately following the end of the collection is set to
     * <tt>null</tt>.  This is useful in determining the length of the
     * collection <i>only</i> if the caller knows that the collection does
     * not contain any <tt>null</tt> elements.)<p>
     *
     * If this collection makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements in
     * the same order. <p>
     *
     * This implementation checks if the array is large enough to contain the
     * collection; if not, it allocates a new array of the correct size and
     * type (using reflection).  Then, it iterates over the collection,
     * storing each object reference in the next consecutive element of the
     * array, starting with element 0.  If the array is larger than the
     * collection, a <tt>null</tt> is stored in the first location after the
     * end of the collection.
     *
     * @param  a the array into which the elements of the collection are to
     *         be stored, if it is big enough; otherwise, a new array of the
     *         same runtime type is allocated for this purpose.
     * @return an array containing the elements of the collection.
     * 
     * @throws NullPointerException if the specified array is <tt>null</tt>.
     * 
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in this
     *         collection.
     */
    public <T> T[] toArray(T a[]) {
        return populationMap().values().toArray(a);
    }

    // Modification Operations

    /**
     * Removes a single instance of the specified element from this
     * collection, if it is present (optional operation).  More formally,
     * removes an element <tt>e</tt> such that <tt>(o==null ? e==null :
     * o.equals(e))</tt>, if the collection contains one or more such
     * elements.  Returns <tt>true</tt> if the collection contained the
     * specified element (or equivalently, if the collection changed as a
     * result of the call).<p>
     *
     * This implementation removes the object from the population map's
     * values.<p>
     *
     * @param o element to be removed from this collection, if present.
     * @return <tt>true</tt> if the collection contained the specified
     *         element.
     * 
     * @throws UnsupportedOperationException if the <tt>remove</tt> method is
     *        not supported by this collection.
     */
    public boolean remove(Object o) {
        return populationMap().values().remove(o);
    }

    // Bulk Operations

    /**
     * Returns <tt>true</tt> if this collection contains all of the elements
     * in the specified collection. <p>
     *
     * This implementation checks whether all the collection's elements are
     * among population map's values.<p>
     *
     * @param c collection to be checked for containment in this collection.
     * @return <tt>true</tt> if this collection contains all of the elements
     *         in the specified collection.
     * 
     * @see #contains(Object)
     */
    public boolean containsAll(Collection<?> c) {
        return populationMap().values().containsAll(c);
    }

    /**
     * Adds all of the elements in the specified collection to this collection
     * (optional operation).  The behavior of this operation is undefined if
     * the specified collection is modified while the operation is in
     * progress.  (This implies that the behavior of this call is undefined if
     * the specified collection is this collection, and this collection is
     * nonempty.) <p>
     *
     * This implementation iterates over the specified collection, and adds
     * each object returned by the iterator to this collection, in turn.<p>
     *
     * Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> unless <tt>add</tt> is
     * overridden.
     *
     * @param c collection whose elements are to be added to this collection.
     * @return <tt>true</tt> if this collection changed as a result of the
     * call.
     * @throws UnsupportedOperationException if the <tt>addAll</tt> method is
     *        not supported by this collection.
     * 
     * @see #add(Object)
     */
    public boolean addAll(Collection<? extends E> c) {
        boolean modified = false;
        for(E e : c) {
            modified |= add(e);
        }
        return modified;
    }

    /**
     * Removes from this collection all of its elements that are contained in
     * the specified collection (optional operation). <p>
     *
     * This implementation removes all elements in the specified collection
     * from the population map's values.<p>
     *
     * @param c elements to be removed from this collection.
     * @return <tt>true</tt> if this collection changed as a result of the
     * call.
     * 
     * @throws    UnsupportedOperationException removeAll is not supported
     *        by this collection.
     * 
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean removeAll(Collection<?> c) {
        return populationMap().values().removeAll(c);
    }

    /**
     * Retains only the elements in this collection that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this collection all of its elements that are not contained in the
     * specified collection. <p>
     *
     * This implementation removes all elements not contained in the specified
     * collection from the population map's values.<p>
     *
     * @param c elements to be retained in this collection.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call.
     * 
     * @throws UnsupportedOperationException if the <tt>retainAll</tt> method
     *        is not supported by this collection.
     * 
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean retainAll(Collection<?> c) {
        return populationMap().values().retainAll(c);
    }

    /**
     * Removes all of the elements from this collection (optional operation).
     * The collection will be empty after this call returns (unless it throws
     * an exception).<p>
     *
     * This implementation clears the population map.<p>
     *
     * Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by this
     * collection's <tt>iterator</tt> method does not implement the
     * <tt>remove</tt> method.
     *
     * @throws UnsupportedOperationException if the <tt>remove</tt> method is
     *        not supported by this collection.
     */
    public void clear() {
        populationMap().clear();
    }


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /**
     * Returns a string representation of this collection.  The string
     * representation consists of a list of the collection's elements in the
     * order they are returned by its iterator, enclosed in square brackets
     * (<tt>"[]"</tt>).  Adjacent elements are separated by the characters
     * <tt>", "</tt> (comma and space).  Elements are converted to strings as
     * by <tt>String.valueOf(Object)</tt>.<p>
     *
     * This implementation creates an empty string buffer, appends a left
     * square bracket, and iterates over the collection appending the string
     * representation of each element in turn.  After appending each element
     * except the last, the string <tt>", "</tt> is appended.  Finally a right
     * bracket is appended.  A string is obtained from the string buffer, and
     * returned.
     * 
     * @return a string representation of this collection.
     */
    public String toString() {
        StringBuilder buf = new StringBuilder("[");
        String prefix = "";
        for(
            PopulationIterator<E> i = populationIterator();
            i.hasNext();
            prefix = ", "
        ){
            int k = i.nextIndex();
            buf.append(
                prefix
            ).append(
                k
            ).append(
                ':'
            ).append(
                i.next()
            );
        }
        return buf.append(
            ']'
        ).toString();
    }
    
    
    //------------------------------------------------------------------------
    // Instance members
    //------------------------------------------------------------------------

    /**
     *
     */
    private class SubArray extends AbstractSparseArray<E> {

        /**
         * Sub array
         */     
        SubArray(
            SortedMap<Integer,E> parent,
            int fromIndex,
            int toIndex
        ){
            this.map = parent.subMap(
                new Integer(fromIndex), 
                new Integer(toIndex)
            );
            this.lowerBound = fromIndex;
        }

        /**
         * Tail array
         */     
        SubArray(
            SortedMap<Integer,E> parent,
            int fromIndex
        ){
            this.map = parent.tailMap(
                new Integer(fromIndex)
            );
            this.lowerBound = fromIndex;
        }

        /**
         * The sub map
         */
        private final SortedMap<Integer,E> map;

        /** 
         * The lower bound
         */     
        private final int lowerBound;

        /**
         * The lower bound is relevant for the end(), add(Object element) and
         * asList() methods.
         *
         * @return  this sparse array's lower bound
         *
         * @see end()
         * @see asList()
         * @see add(Object)
         */
        protected int lowerBound(){
            return this.lowerBound;
        }
         
        /**
         * Returns a map view of this sparse array's populated elements.
         *
         * @return  a map view of this sparse array's populated elements
         */
        public SortedMap<Integer,E> populationMap(
        ){
            return this.map;
        }

    }
    
    /**
     * The asList method returns a list backed up by the sparse array. Its
     * size() is the sparse array's lastIndex() + 1 and the sparse array's
     * un-populated positions are represented as null values.
     */ 
    private class ValueList extends AbstractList<E> {
        
        /**
         * Returns the sparse array's end.
         *
         * @return  the number of elements in this list.
         */
        public int size(
        ){
            return end() - lowerBound();
        }
     
        /**
         * Returns the element at the specified position in this list.
         *
         * @param   index
         *          index of element to return.
         *
         * @return  the element at the specified position in this list.
         *
         * @exception   IndexOutOfBoundsException
         *              if the index is out of range (<tt>index &lt; 0 || 
         *              index &gt; size()</tt>).
         */
        public E get(int index) {
            if(
                index < 0 || index > size()
            ) throw new IndexOutOfBoundsException(
                "Can not retrieve an object from position " + index +
                " in a list with " + size() + " elements" 
            );
            return AbstractSparseArray.this.get(
                index + lowerBound()
            );
        }

        /**
         * Replaces the element at the specified position in this list with
         * the specified element.
         *
         * @param   index
         *          index of element to return.
         * @param   element
         *          element to be stored at the specified position.
         *
         * @return  the element previously at the specified position.
         *
         * @exception   IndexOutOfBoundsException
         *              if the index is out of range (<tt>index &lt; 0 || 
         *              index &gt; size()</tt>).
         */
        public E set(  
            int index,
            E element
        ){
            if(
                index < 0 || index > size()
            ) throw new IndexOutOfBoundsException(
                "Can not store an object at position " + index +
                " in a list with " + size() + " elements" 
            );
            return AbstractSparseArray.this.set(
                index,
                element
            );
        }  
                
    }

    /**
     * An iterator for the populated elements in this sparse array (in
     * proper sequence). The first index is start() and the last end() - 1
     * respectively. The indices are not contiguous.
     */
    private class PopulationIteratorImpl implements PopulationIterator<E> {

        /**
         * Iterate internally over the data entries. 
         */
        private final Iterator<Map.Entry<Integer,E>> entries = populationMap().entrySet().iterator();
                
        /**
         * Prefetch is necessary for nextIndex
         */     
        private Map.Entry<Integer,E> nextEntry = null;
        
        /**
         * Current entry is necessary for set
         */     
        private Map.Entry<Integer,E> currentEntry = null;

        /**
         * Returns true if this population iterator has more elements when
         * traversing the sparse array in the forward direction. (In other 
         * words, returns true if next would return an element rather than
         * throwing an exception.)
         *
         * @return  true if the population iterator has more elements when 
         *          traversing the sparse array in the forward direction.
         */
        public boolean hasNext(
        ){
            return nextEntry != null || this.entries.hasNext();
        }
    
        /**
         * Returns the next element in the sparse array. This method may be
         * called repeatedly to iterate through the sparse array.
         *
         * @return  the next element in the sparse array.
         *
         * @exception   NoSuchElementException
         *              if the iteration has no next element.
         */
        public E next(
        ){
            currentEntry = this.nextEntry == null ?
                this.entries.next() : 
                this.nextEntry;
            this.nextEntry = null;
            return currentEntry.getValue();
        }
    
        /**
         * Returns the index of the element that would be returned by a 
         * subsequent call to next. (Returns sparse array end if the
         * population iterator is at the end of the sparse array.)
         *
         * @return  the index of the element that would be returned by a
         *          subsequent call to next, or sparse array end if population
         *          iterator is at end of the sparse array.
         */
        public int nextIndex(
        ){
            if(nextEntry == null){
                if (! this.entries.hasNext()) return end();
                this.nextEntry = this.entries.next();
                this.currentEntry = null;
            }
            return nextEntry.getKey().intValue(); 
        }
    
        /**
         * Removes from the sparse array the last element that was returned by
         * next or previous (optional operation). 
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
        public void remove(
        ){
            if(this.nextEntry != null) throw new IllegalStateException(
                "remove() can't be called after nextIndex()"
            );
            if(this.currentEntry == null) throw new IllegalStateException(
                "remove() can be called after next() only"
            );
            this.entries.remove();
        }   
        
        /**
         * Replaces the last element returned by next or previous with the
         * specified element (optional operation). 
         *
         * @param   o
         *          the element with which to replace the last element returned by
         *          next or previous.
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
        public void set(E o){
            if(this.nextEntry != null) throw new IllegalStateException(
                "set(Object) can't be called after nextIndex()"
            );
            if(this.currentEntry == null) throw new IllegalStateException(
                "set(Object) can be called after next() only"
            );
            this.currentEntry.setValue(o);
        }
        
    }   

}
