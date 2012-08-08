/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: OffsetArrayList.java,v 1.2 2009/01/11 21:19:32 wfro Exp $
 * Description: Sparsely Populated List Implementation
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/11 21:19:32 $
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
package org.openmdx.base.collection;

import java.io.Serializable;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Sparsely Populated List Implementation
 * <p>
 * The OffsetArrayList can be populated in any order.
 */
public class OffsetArrayList<E>
    implements SparseList<E>, Cloneable, Serializable
{

    //------------------------------------------------------------------------
    // Classes
    //------------------------------------------------------------------------

    /**
     * 
     */
    private static final long serialVersionUID = 3977857388844168754L;

    /**
     *
     */
    static class NonContiguousIterator<E>
        implements ListIterator<E>
    {


        /**
         * Index into values
         */
        protected int nextIndex = 0;

        /**
         * Index into values
         */
        protected int previousIndex = - 1;

        /**
         * Index into values
         */
        protected int currentIndex = - 1;

        /**
         * 
         */
        protected final List<E> values; 

        /**
         * 
         */
        protected final int offset; 

        /**
         * 
         */
        protected NonContiguousIterator(
            int offset,
            List<E> values
        ) {
            super();
            this.values = values;
            this.offset = offset;
        }

        /**
         * Returns true if this list iterator has more elements when
         * traversing the list in the forward direction. (In other words,
         * returns true if next would return an element rather than throwing
         * an exception.)
         *
         * @return      true if the list iterator has more elements when
         *              traversing the list in the forward direction.
         */
        public boolean hasNext(
            )
        {
            return this.nextIndex < values.size();
        }

        /**
         * Returns the next element in the list. This method may be called
         * repeatedly to iterate through the list, or intermixed with calls to
         * previous to go back and forth. (Note that alternating calls to next
         * and previous will return the same element repeatedly.)
         *
         * @return      the next element in the list.
         *
         * @exception   NoSuchElementException
         *              if the iteration has no next element.
         */
        public E next(
            )
        {
            this.currentIndex = this.previousIndex = this.nextIndex;
            if (
                ++this.nextIndex < values.size()
                ) while (
                      values.get(this.nextIndex) == null
                      ) this.nextIndex++;
            return values.get(this.currentIndex);
        }

        /**
         * Returns true if this list iterator has more elements when
         * traversing the list in the reverse direction. (In other words,
         * returns true if previous would return an element rather than
         * throwing an exception.)
         *
         * @return      true if the list iterator has more elements when
         *              traversing the list in the reverse direction.
         */
        public boolean hasPrevious(
            )
        {
            return this.previousIndex >= 0;
        }

        /**
         * Returns the previous element in the list. This method may be called
         * repeatedly to iterate through the list backwards, or intermixed
         * with calls to next to go back and forth. (Note that alternating
         * calls to next and previous will return the same element
         * repeatedly.)
         *
         * @return      the previous element in the list.
         *
         * @exception   NoSuchElementException
         *              if the iteration has no previous element.
         */
        public E previous(
            )
        {
            this.currentIndex = this.nextIndex = previousIndex;
            if (
                --this.previousIndex >= 0
                ) while (
                      values.get(this.previousIndex) == null
                      ) this.previousIndex--;
            return values.get(this.currentIndex);
        }

        /**
         * Returns the index of the element that would be returned by a
         * subsequent call to next. (Returns list size if the list iterator is
         * at the end of the list.)
         *
         * @return      the index of the element that would be returned by a
         *              subsequent call to next, or list size if list iterator
         *              is at end of list.
         */
        public int nextIndex(
            )
        {
            return this.nextIndex + offset;
        }

        /**
         * Returns the index of the element that would be returned by a
         * subsequent call to previous. (Returns -1 if the list iterator is at
         * the beginning of the list.)
         *
         * @return      the index of the element that would be returned by a
         *              subsequent call to previous, or -1 if list iterator is
         *              at beginning of list.
         */
        public int previousIndex(
            )
        {
            return this.previousIndex + offset;
        }

        /**
         * Removes from the list the last element that was returned by next or
         * previous (optional operation). This call can only be made once per
         * call to next or previous. It can be made only if ListIterator.add
         * has not been called after the last call to next or previous.
         *
         * @exception   UnsupportedOperationException
         *              if the remove operation is not supported by this list
         *              iterator.
         * @exception   IllegalStateException
         *              neither next nor previous have been called, or remove
         *              or add have been called after the last call to
         *              next or previous.
         */
        public void remove(
            )
        {
            throw new UnsupportedOperationException(); 
        }

        /**
         * Replaces the last element returned by next or previous with the
         * specified element (optional operation). This call can be made only
         * if neither ListIterator.remove nor ListIterator.add have been
         * called after the last call to next or previous.
         *
         * @param       o
         *              the element with which to replace the last element
         *              returned by next or previous.
         *
         * @exception   UnsupportedOperationException
         *              if the set operation is not supported by this list
         *              iterator.
         * @exception   ClassCastException
         *              if the class of the specified element prevents it from
         *              being added to this list.
         * @exception   IllegalArgumentException
         *              if some aspect of the specified element prevents it
         *              from being added to this list.
         * @exception   IllegalStateException
         *              if neither next nor previous have been called, or
         *              remove or add have been called after the last call to
         *              next or previous.
         */
        public void set(
            E o
            )
        {
            values.set(this.currentIndex,o);
        }

        /**
         * Inserts the specified element into the list (optional operation).
         * The element is inserted immediately before the next element that
         * would be returned by next, if any, and after the next element that
         * would be returned by previous, if any. (If the list contains no
         * elements, the new element becomes the sole element on the list.)
         * The new element is inserted before the implicit cursor: a
         * subsequent call to next would be unaffected, and a subsequent call
         * to previous would return the new element. (This call increases by
         * one the value that would be returned by a call to nextIndex or
         * previousIndex.)
         *
         * @param       o
         *              the element to insert.
         *
         * @exception   UnsupportedOperationException
         *              if the add method is not supported by this list
         *              iterator.
         * @exception   ClassCastException
         *              if the class of the specified element prevents it from
         *              being added to this Set.
         * @exception   IllegalArgumentException
         *              if some aspect of this element prevents it from being
         *              added to this Collection.
         */
        public void add(
            E o
            )
        {
            throw new UnsupportedOperationException(); 
        }

    }

    /**
     *
     */
    static class ContiguousIterator<E>
        extends NonContiguousIterator<E>
    {

        /**
         * Constructor
         *
         * @param       _index
         *              index of first element to be returned from the list
         *              iterator (by a call to the next method).
         *
         * @exception   IndexOutOfBoundsException
         *              if the index is out of range (index < 0 || index >
         *              size()).
         */
        ContiguousIterator(
            int offset,
            List<E> values,
            int _index
        ) {
            super(offset,values);
            int index = _index;
            cursor = index;
            if (index < 0) throw new IndexOutOfBoundsException(
                               "Index out of bounds: " + index
                               );
            try 
            {
                while(index-- > 0) super.next();
            } 
            catch (NoSuchElementException exception) 
            {
                throw new IndexOutOfBoundsException(
                    "Index out of bounds: " + index
                    );
            }
        }

        /**
         *
         */
        int cursor;

        /**
         * Returns the index of the element that would be returned by a
         * subsequent call to next. (Returns list size if the list iterator is
         * at the end of the list.)
         *
         * @return      the index of the element that would be returned by a
         *              subsequent call to next, or list size if list iterator
         *              is at end of list.
         */
        public int nextIndex(
            )
        {
            return cursor;
        }

        /**
         * Returns the next element in the list. This method may be called
         * repeatedly to iterate through the list, or intermixed with calls to
         * previous to go back and forth. (Note that alternating calls to next
         * and previous will return the same element repeatedly.)
         *
         * @return      the next element in the list.
         *
         * @exception   NoSuchElementException
         *              if the iteration has no next element.
         */
        public E next(
            )
        {
            cursor++;
            return super.next();
        }

        /**
         * Returns the index of the element that would be returned by a
         * subsequent call to next. (Returns list size if the list iterator is
         * at the end of the list.)
         *
         * @return      the index of the element that would be returned by a
         *              subsequent call to next, or list size if list iterator
         *              is at end of list.
         */
        public int previousIndex(
            )
        {
            return cursor - 1;
        }

        /**
         * Returns the previous element in the list. This method may be called
         * repeatedly to iterate through the list backwards, or intermixed
         * with calls to next to go back and forth. (Note that alternating
         * calls to next and previous will return the same element
         * repeatedly.)
         *
         * @return      the previous element in the list.
         *
         * @exception   NoSuchElementException
         *              if the iteration has no previous element.
         */
        public E previous(
            )
        {
            cursor--;
            return super.previous();
        }

    }
    /**
     *
     */
    static class PopulationList<E>
        extends AbstractSequentialList<E>
    {

        
        /**
         * 
         */
        protected final int offset;
        
        /**
         * 
         */
        protected final List<E> values;

        /**
         * 
         */
        public PopulationList(
            int offset,
            List<E> values
        ) {
            super();
            this.offset = offset;
            this.values = values;
        }

        /**
         * Returns the number of elements in this list. If this list contains
         * more than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
         *
         * @return      the number of elements in this list.
         */
        public int size(
            )
        {
            // Population size could be cached for improved efficiency
            int count = 0;
            for(
                int pointer = 0;
                pointer < values.size();
                pointer++
                ) if(values.get(pointer) != null) count++;
            return count;
        }

        /**
         * Returns a list iterator of the elements in this list (in proper
         * sequence), starting at the specified position in this list. The
         * specified index indicates the first element that would be returned
         * by an initial call to the next method. An initial call to the
         * previous method would return the element with the specified index
         * minus one.
         *
         * @param       index
         *              index of first element to be returned from the list
         *              iterator (by a call to the next method).
         *
         * @return      a list iterator of the elements in this list (in
         *              proper sequence), starting at the specified position
         *              in this list.
         *
         * @exception   IndexOutOfBoundsException
         *              if the index is out of range (index < 0 || index >
         *              size()).
         */
        public ListIterator<E> listIterator(
            int index
        ){
            return new ContiguousIterator<E>(
                this.offset,
                this.values,
                index
            );
        }

        /**
         * NOTE: Must be implemented for J#
         */
        public ListIterator<E> listIterator(
        ) {
          return listIterator(0);
        }

    }

    /**
     * Creates a <code>OffsetArrayList</code> object.
     */
    private OffsetArrayList(
        int offset,
        List<E> values
    ){
        this.values = values;
        this.offset = offset;
    }


    /**
     * Creates a <code>OffsetArrayList</code> object.
     */
    public OffsetArrayList(
    ){
        this(0, new ArrayList<E>());
    }

    /**
     * Creates a <code>OffsetArrayList</code> object with initial capacity n.
     */
    public OffsetArrayList(
        int n
    ){
        this(0, new ArrayList<E>(n));
    }

    /**
     * Creates a <code>OffsetArrayList</code> object.
     *
     * @param  collection  A non-null collection of property values
     */
    public OffsetArrayList(
        Collection<? extends E>  collection
    ){
        if(collection instanceof SparseList) {
            SparseList<? extends E> list = (SparseList<? extends E>) collection;
            this.values = new ArrayList<E>(
                list.subList(list.firstIndex(),list.size())
            );
            this.offset = list.firstIndex();
        } else {
            this.values = new ArrayList<E>(collection);
            this.offset = 0;
            trim(null);
        }
    }


    //------------------------------------------------------------------------
    // Extends AbstractList
    //------------------------------------------------------------------------

    /**
     * Get the pointer
     *
     * @return      a pointer into the values list
     *
     * @exception   IndexOutOfBoundsException
     *              if the specified index is out of range (index < 0).
     */
    private final int getPointer(
        int index
    ){
        if (index < 0) throw new ArrayIndexOutOfBoundsException(index);
        return index - offset;
    }

    /**
     * Remove leading and trailing null elements from values.
     */
    private final E trim(
        E result
    ){
        if (! this.values.isEmpty()) for (
            int index = this.values.size() - 1;
            this.values.get(index) == null;
            index--
        ) this.values.remove(index);
        while (! this.values.isEmpty() && this.values.get(0) == null) {
            offset++;
            this.values.remove(0);
        }
        return result;
    }

    /**
     * Returns the number of elements in this list. If this list contains more
     * than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
     *
     * @return      the number of elements in this list.
     */
    public int size(
    ){
        return this.values.size() + offset;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param       index
     *              index of element to return.
     *
     * @return      the element at the specified position in this list;
     *              null if the element has not been set yet.
     *
     * @exception   IndexOutOfBoundsException
     *              if the given index is out of range (index < 0).
     */
     public E get(
        int index
     ){
        final int pointer = getPointer(index);
        return pointer >= 0 && pointer < values.size() ?
            values.get(pointer) :
            null;
     }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element (optional operation).
     *
     * @param       index
     *              index of element to replace.
     * @param       element
     *              element to be stored at the specified position.
     *
     * @return      the element previously at the specified position.
     *
     * @exception   UnsupportedOperationException
     *              if the set method is not supported by this List.
     * @exception   ClassCastException
     *              if the class of the specified element prevents it from
     *              being added to this list.
     * @exception   IllegalArgumentException
     *              if some aspect of the specified element prevents it from
     *              being added to this list.
     * @exception   IndexOutOfBoundsException
     *              if the specified index is out of range (index < 0).
     */
     public E set(
        int index,
        E element
     ){
        int pointer = getPointer(index);
        if(pointer >= 0 && pointer < this.values.size()) {
            return trim(this.values.set(pointer,element));
        } else if (element == null) {
            return null;
        }
        if(this.values.isEmpty()) {
            offset = index;
            pointer = 0;
        } else if (pointer < 0) {
            offset = index;
            while (++pointer < 0) this.values.add(0,null);
        } else {
            while (pointer > this.values.size()) this.values.add(null);
        }
        this.values.add(pointer,element);
        return null;
     }

    /**
     * Inserts the specified element at the specified position in this list
     * (optional operation). Shifts the element currently at that position
     * (if any) and any subsequent elements to the right (adds one to their
     * indices).
     *
     * @param       index
     *              index at which the specified element is to be inserted.
     * @param       element
     *              element to be inserted.
     *
     * @exception   UnsupportedOperationException
     *              if the add method is not supported by this list.
     * @exception   ClassCastException
     *              if the class of the specified element prevents it from
     *              being added to this list.
     * @exception   IllegalArgumentException
     *              if some aspect of the specified element prevents it from
     *              being added to this list.
     * @xception    IndexOutOfBoundsException
     *              index is out of range (index < 0 || index > size()).
     */
     public void add(
        int index,
        E element
     ){
        if (index <= offset) {
            offset++;
            set(index, element);
        } else if (index >= size()) {
            set(index, element);
        } else {
            this.values.add(index - offset, element);
        }
     }

    /**
     * Removes the element at the specified position in this list (optional
     * operation). Shifts any subsequent elements to the left (subtracts one
     * from their indices). Returns the element that was removed from the
     * list.
     *
     * @param       index
     *              the index of the element to remove.
     *
     * @return      the element previously at the specified position.
     *
     *
     * @exception   UnsupportedOperationException
     *              if the remove method is not supported by this list.
     * @exception   IndexOutOfBoundsException
     *              if the specified index is out of range (index < 0).
     */
    public E remove(
        int index
    ){
        final int pointer = getPointer(index);
        if(pointer >= 0 && pointer < this.values.size()) {
            return trim(this.values.remove(pointer));
        } else {
            if(pointer < 0) offset--;
            return trim(null);
        }
    }

    /**
     * Removes all of the elements from this collection (optional operation).
     * The collection will be empty after this call returns (unless it throws an exception).
     */
    public void clear() {
      this.values.clear();
      offset = 0;
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the specified
     * collection's iterator (optional operation). The behavior of this
     * operation is unspecified if the specified collection is modified while
     * the operation is in progress. (Note that this will occur if the
     * specified collection is this list, and it's nonempty.)
     *
     * @param       c
     *              collection whose elements are to be added to this list.
     *
     * @return      true if this list changed as a result of the call.
     *
     * @exception   UnsupportedOperationException
     *              f the addAll method is not supported by this list.
     * @exception   ClassCastException
     *              if the class of an element in the specified collection
     *              prevents it from being added to this list.
     * @exception   IllegalArgumentException
     *              if some aspect of an element in the specified collection
     *              prevents it from being added to this list.
     */
    public boolean addAll(
        Collection<? extends E> c
    ){
        return addAll(size(), c);
    }

    /**
     * Inserts all of the elements in the specified collection into this list
     * at the specified position (optional operation). Shifts the element
     * currently at that position (if any) and any subsequent elements to the
     * right (increases their indices). The new elements will appear in this
     * list in the order that they are returned by the specified collection's
     * iterator. The behavior of this operation is unspecified if the
     * specified collection is modified while the operation is in progress.
     * (Note that this will occur if the specified collection is this list,
     * and it's nonempty.)
     *
     *
     * @param       _index
     *              index at which to insert first element from the specified
     *              collection.
     * @param       c
     *              collection whose elements are to be added to this list.
     *
     * @return      true if this list changed as a result of the call.
     *
     * @exception   UnsupportedOperationException
     *              f the addAll method is not supported by this list.
     * @exception   ClassCastException
     *              if the class of an element in the specified collection
     *              prevents it from being added to this list.
     * @exception   IllegalArgumentException
     *              if some aspect of an element in the specified collection
     *              prevents it from being added to this list.
     * @exception   IndexOutOfBoundsException
     *              if the index is out of range (index < 0).
     */
    public boolean addAll(
        int _index,
        Collection<? extends E> c
    ){
        int index = _index;
        boolean modified = false;
        if (c instanceof SparseList) {
            for (
                ListIterator<? extends E> iterator = ((SparseList<? extends E>)c).populationIterator();
                iterator.hasNext();
                modified = true
            ) add(index + iterator.nextIndex(), iterator.next());
        } else {
            for (
                Iterator<? extends E> iterator = c.iterator();
                iterator.hasNext();
                modified = true, index++
            ) add(index, iterator.next());
        }
        return modified;
    }

    //------------------------------------------------------------------------
    // Implements SparseList
    //------------------------------------------------------------------------

    /**
     * Returns a list iterator of the populated elements in this list (in
     * proper sequence). The indices start firstIndex(), end at lastIndex()
     * and are not contiguous.
     *
     * @return  an iterator over the populated elements in the list
     */
    public ListIterator<E> populationIterator(
    ){
        return new NonContiguousIterator<E>(this.offset, this.values);
    }

    /**
     * An unmodifiable list containing all the populated elements. Its indices
     * are contiguous and start with 0.
     *
     * @return      a list containing the populated elements only
     */
    public List<E> population(
    ){
        return new PopulationList<E>(this.offset, this.values);
    }

    /**
     * Return the index of the first populated element in the list.
     *
     * @return      the index of the first populated element in the list.
     */
    public int firstIndex(
    ){
        return offset;
    }

    /**
     * Return the index of the last populated element in the list.
     *
     * @return      the index of the last populated element in the list.
     */
    public int lastIndex(
    ){
        return size() - 1;
    }


    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------

    /**
     * Generates a new copy of this dataprovider object.
     * Subsequent changes to the path or the attributes of this
     * object will not affect the new copy, and vice versa.
     *
     * @return    a clone of this instance.
     */
    public Object clone(
    ){
        return new OffsetArrayList<E>(this);
    }


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param   object  the reference object with which to compare.
     *
     * @return  true if this object is the same as the object argument;
     *          false otherwise.
     */
    public boolean equals (Object object)
    {
        if (this == object) return true;
        if (! (object instanceof SparseList)) return false;
        final SparseList<?> that = (SparseList<?>)object;
        return
            this.firstIndex() == that.firstIndex() &&
            this.lastIndex() == that.lastIndex() &&
            this.subList(firstIndex(),size()).equals(
                that.subList(firstIndex(),size())
            );
    }

    /**
     * Returns a hash code value for the object. This method is supported for
     * the benefit of hashtables such as those provided by
     * java.util.Hashtable.
     *
     * @return       a hash code value for this object.
     */
    public int hashCode(
    ){
        return values.hashCode();
    }


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /**
     * Returns a string representation of the object. In general, the toString
     * method returns a string that "textually represents" this object. The
     * result should be a concise but informative representation that is easy
     * for a person to read. It is recommended that all subclasses override
     * this method.
     *
     * @return the dataprovider object's string representation
     */
    public String toString (
    ){
        final StringBuilder result = new StringBuilder("[");
        String prefix = "";
        for(
            ListIterator<E> iterator = populationIterator ();
            iterator.hasNext();
            prefix = ", "
        ) result.append(
            prefix
        ).append(
            iterator.nextIndex()
        ).append(
            ':'
        ).append(
            iterator.next()
        );
        return result.append(
            ']'
        ).toString();
    }


    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------

    /**
     * The property values' offset.
     */
    protected transient int offset;

    /**
     * The property's values.
     */
    protected transient List<E> values = null;


    //------------------------------------------------------------------------
    // Implements Externalizable
    //------------------------------------------------------------------------

    private void writeObject(
        java.io.ObjectOutputStream stream
    ) throws java.io.IOException {
        stream.writeObject(
            this.values.toArray(new Object[this.values.size()])
        );
    }

    @SuppressWarnings("unchecked")
    private void readObject(
      java.io.ObjectInputStream stream
    ) throws java.io.IOException, ClassNotFoundException {
      this.values = new ArrayList<E>(Arrays.asList((E[])stream.readObject()));
    }

    //------------------------------------------------------------------------
    // Extends AbstractList
    //------------------------------------------------------------------------

    /**
     * Appends the specified element to the end of this List (optional
     * operation). <p>
     *
     * This implementation calls <tt>add(size(), o)</tt>.<p>
     *
     * Note that this implementation throws an
     * <tt>UnsupportedOperationException</tt> unless <tt>add(int, Object)</tt>
     * is overridden.
     *
     * @param o element to be appended to this list.
     * 
     * @return <tt>true</tt> (as per the general contract of
     * <tt>Collection.add</tt>).
     * 
     * @throws UnsupportedOperationException if the <tt>add</tt> method is not
     *        supported by this Set.
     * 
     * @throws ClassCastException if the class of the specified element
     *        prevents it from being added to this set.
     * 
     * @throws IllegalArgumentException some aspect of this element prevents
     *            it from being added to this collection.
     */
    public boolean add(E o) {
    add(size(), o);
    return true;
    }


    // Search Operations

    /**
     * Returns the index in this list of the first occurence of the specified
     * element, or -1 if the list does not contain this element.  More
     * formally, returns the lowest index <tt>i</tt> such that <tt>(o==null ?
     * get(i)==null : o.equals(get(i)))</tt>, or -1 if there is no such
     * index.<p>
     *
     * This implementation first gets a list iterator (with
     * <tt>listIterator()</tt>).  Then, it iterates over the list until the
     * specified element is found or the end of the list is reached.
     *
     * @param o element to search for.
     * 
     * @return the index in this List of the first occurence of the specified
     *         element, or -1 if the List does not contain this element.
     */
    public int indexOf(Object o) {
    ListIterator<E> e = listIterator();
    if (o==null) {
        while (e.hasNext())
        if (e.next()==null)
            return e.previousIndex();
    } else {
        while (e.hasNext())
        if (o.equals(e.next()))
            return e.previousIndex();
    }
    return -1;
    }

    /**
     * Returns the index in this list of the last occurence of the specified
     * element, or -1 if the list does not contain this element.  More
     * formally, returns the highest index <tt>i</tt> such that <tt>(o==null ?
     * get(i)==null : o.equals(get(i)))</tt>, or -1 if there is no such
     * index.<p>
     *
     * This implementation first gets a list iterator that points to the end
     * of the list (with listIterator(size())).  Then, it iterates backwards
     * over the list until the specified element is found, or the beginning of
     * the list is reached.
     *
     * @param o element to search for.
     * 
     * @return the index in this list of the last occurence of the specified
     *         element, or -1 if the list does not contain this element.
     */
    public int lastIndexOf(Object o) {
    ListIterator<E> e = listIterator(size());
    if (o==null) {
        while (e.hasPrevious())
        if (e.previous()==null)
            return e.nextIndex();
    } else {
        while (e.hasPrevious())
        if (o.equals(e.previous()))
            return e.nextIndex();
    }
    return -1;
    }


    // Iterators

    /**
     * Returns an iterator over the elements in this list in proper
     * sequence. <p>
     *
     * This implementation returns a straightforward implementation of the
     * iterator interface, relying on the backing list's <tt>size()</tt>,
     * <tt>get(int)</tt>, and <tt>remove(int)</tt> methods.<p>
     *
     * Note that the iterator returned by this method will throw an
     * <tt>UnsupportedOperationException</tt> in response to its
     * <tt>remove</tt> method unless the list's <tt>remove(int)</tt> method is
     * overridden.<p>
     *
     * This implementation can be made to throw runtime exceptions in the face
     * of concurrent modification, as described in the specification for the
     * (protected) <tt>modCount</tt> field.
     *
     * @return an iterator over the elements in this list in proper sequence.
     * 
     */
    public Iterator<E> iterator() {
    return new Itr();
    }

    /**
     * Returns an iterator of the elements in this list (in proper sequence).
     * This implementation returns <tt>listIterator(0)</tt>.
     * 
     * @return an iterator of the elements in this list (in proper sequence).
     * 
     * @see #listIterator(int)
     */
    public ListIterator<E> listIterator() {
    return listIterator(0);
    }

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence), starting at the specified position in the list.  The
     * specified index indicates the first element that would be returned by
     * an initial call to the <tt>next</tt> method.  An initial call to
     * the <tt>previous</tt> method would return the element with the
     * specified index minus one.<p>
     *
     * This implementation returns a straightforward implementation of the
     * <tt>ListIterator</tt> interface that extends the implementation of the
     * <tt>Iterator</tt> interface returned by the <tt>iterator()</tt> method.
     * The <tt>ListIterator</tt> implementation relies on the backing list's
     * <tt>get(int)</tt>, <tt>set(int, Object)</tt>, <tt>add(int, Object)</tt>
     * and <tt>remove(int)</tt> methods.<p>
     *
     * Note that the list iterator returned by this implementation will throw
     * an <tt>UnsupportedOperationException</tt> in response to its
     * <tt>remove</tt>, <tt>set</tt> and <tt>add</tt> methods unless the
     * list's <tt>remove(int)</tt>, <tt>set(int, Object)</tt>, and
     * <tt>add(int, Object)</tt> methods are overridden.<p>
     *
     * This implementation can be made to throw runtime exceptions in the
     * face of concurrent modification, as described in the specification for
     * the (protected) <tt>modCount</tt> field.
     *
     * @param index index of the first element to be returned from the list
     *          iterator (by a call to the <tt>next</tt> method).
     * 
     * @return a list iterator of the elements in this list (in proper
     *         sequence), starting at the specified position in the list.
     * 
     * @throws IndexOutOfBoundsException if the specified index is out of
     *        range (<tt>index &lt; 0 || index &gt; size()</tt>).
     * 
     */
    public ListIterator<E> listIterator(final int index) {
    if (index<0 || index>size())
      throw new IndexOutOfBoundsException("Index: "+index);

    return new ListItr(index);
    }

    class Itr implements Iterator<E> {
    /**
     * Index of element to be returned by subsequent call to next.
     */
    int cursor = 0;

    /**
     * Index of element returned by most recent call to next or
     * previous.  Reset to -1 if this element is deleted by a call
     * to remove.
     */
    int lastRet = -1;

    /**
     * The modCount value that the iterator believes that the backing
     * List should have.  If this expectation is violated, the iterator
     * has detected concurrent modification.
     */
    int expectedModCount = modCount;

    public boolean hasNext() {
        return cursor != size();
    }

    public E next() {
        try {
        E next = get(cursor);
        checkForComodification();
        lastRet = cursor++;
        return next;
        } catch(IndexOutOfBoundsException e) {
        checkForComodification();
        throw new NoSuchElementException();
        }
    }

    public void remove() {
        if (lastRet == -1)
        throw new IllegalStateException();
            checkForComodification();

        try {
        OffsetArrayList.this.remove(lastRet);
        if (lastRet < cursor)
            cursor--;
        lastRet = -1;
        expectedModCount = modCount;
        } catch(IndexOutOfBoundsException e) {
        throw new ConcurrentModificationException();
        }
    }

    final void checkForComodification() {
        if (modCount != expectedModCount)
        throw new ConcurrentModificationException();
    }
    }

    private class ListItr extends Itr implements ListIterator<E> {
        
        ListItr(final int index) {
            cursor = index;
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }
    
        public E previous() {
            try {
            E previous = get(--cursor);
            checkForComodification();
            lastRet = cursor;
            return previous;
            } catch(IndexOutOfBoundsException e) {
            checkForComodification();
            throw new NoSuchElementException();
            }
        }
    
        public int nextIndex() {
            return cursor;
        }
    
        public int previousIndex() {
            return cursor-1;
        }

        public void set(E o) {
            if (lastRet == -1)
            throw new IllegalStateException();
                checkForComodification();
    
            try {
            OffsetArrayList.this.set(lastRet, o);
            expectedModCount = modCount;
            } catch(IndexOutOfBoundsException e) {
            throw new ConcurrentModificationException();
            }
        }

        public void add(E o) {
                checkForComodification();
    
            try {
            OffsetArrayList.this.add(cursor++, o);
            lastRet = -1;
            expectedModCount = modCount;
            } catch(IndexOutOfBoundsException e) {
            throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Returns a view of the portion of this list between <tt>fromIndex</tt>,
     * inclusive, and <tt>toIndex</tt>, exclusive.  (If <tt>fromIndex</tt> and
     * <tt>toIndex</tt> are equal, the returned list is empty.)  The returned
     * list is backed by this list, so changes in the returned list are
     * reflected in this list, and vice-versa.  The returned list supports all
     * of the optional list operations supported by this list.<p>
     *
     * This method eliminates the need for explicit range operations (of the
     * sort that commonly exist for arrays).  Any operation that expects a
     * list can be used as a range operation by operating on a subList view
     * instead of a whole list.  For example, the following idiom removes a
     * range of elements from a list:
     * <pre>
     *     list.subList(from, to).clear();
     * </pre>
     * Similar idioms may be constructed for <tt>indexOf</tt> and
     * <tt>lastIndexOf</tt>, and all of the algorithms in the
     * <tt>Collections</tt> class can be applied to a subList.<p>
     * 
     * The semantics of the list returned by this method become undefined if
     * the backing list (i.e., this list) is <i>structurally modified</i> in
     * any way other than via the returned list.  (Structural modifications are
     * those that change the size of the list, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results.)<p>
     *
     * This implementation returns a list that subclasses
     * <tt>AbstractList</tt>.  The subclass stores, in private fields, the
     * offset of the subList within the backing list, the size of the subList
     * (which can change over its lifetime), and the expected
     * <tt>modCount</tt> value of the backing list.<p>
     *
     * The subclass's <tt>set(int, Object)</tt>, <tt>get(int)</tt>,
     * <tt>add(int, Object)</tt>, <tt>remove(int)</tt>, <tt>addAll(int,
     * Collection)</tt> and <tt>removeRange(int, int)</tt> methods all
     * delegate to the corresponding methods on the backing abstract list,
     * after bounds-checking the index and adjusting for the offset.  The
     * <tt>addAll(Collection c)</tt> method merely returns <tt>addAll(size,
     * c)</tt>.<p>
     *
     * The <tt>listIterator(int)</tt> method returns a "wrapper object" over a
     * list iterator on the backing list, which is created with the
     * corresponding method on the backing list.  The <tt>iterator</tt> method
     * merely returns <tt>listIterator()</tt>, and the <tt>size</tt> method
     * merely returns the subclass's <tt>size</tt> field.<p>
     *
     * All methods first check to see if the actual <tt>modCount</tt> of the
     * backing list is equal to its expected value, and throw a
     * <tt>ConcurrentModificationException</tt> if it is not.
     *
     * @param fromIndex low endpoint (inclusive) of the subList.
     * @param toIndex high endpoint (exclusive) of the subList.
     * @return a view of the specified range within this list.
     * @throws IndexOutOfBoundsException endpoint index value out of range
     *         <tt>(fromIndex &lt; 0 || toIndex &gt; size)</tt>
     * @throws IllegalArgumentException endpoint indices out of order
     * <tt>(fromIndex &gt; toIndex)</tt> */
    public List<E> subList(int fromIndex, int toIndex) {
        return toIndex <= this.offset ?
            Collections.nCopies(toIndex - fromIndex, (E)null) :
        fromIndex < this.offset ?
            new OffsetArrayList<E>(this.offset - fromIndex, this.values) :
            this.values.subList(fromIndex - this.offset, toIndex - this.offset);
    }

    // Comparison and hashing

    /**
     * Removes from this list all of the elements whose index is between
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive.
     * Shifts any succeeding elements to the left (reduces their index).  This
     * call shortens the ArrayList by <tt>(toIndex - fromIndex)</tt>
     * elements.  (If <tt>toIndex==fromIndex</tt>, this operation has no
     * effect.)<p>
     *
     * This method is called by the <tt>clear</tt> operation on this list
     * and its subLists.  Overriding this method to take advantage of
     * the internals of the list implementation can <i>substantially</i>
     * improve the performance of the <tt>clear</tt> operation on this list
     * and its subLists.<p>
     *
     * This implementation gets a list iterator positioned before
     * <tt>fromIndex</tt>, and repeatedly calls <tt>ListIterator.next</tt>
     * followed by <tt>ListIterator.remove</tt> until the entire range has
     * been removed.  <b>Note: if <tt>ListIterator.remove</tt> requires linear
     * time, this implementation requires quadratic time.</b>
     *
     * @param fromIndex index of first element to be removed.
     * @param toIndex index after last element to be removed.
     */
    protected void removeRange(int fromIndex, int toIndex) {
        ListIterator<E> it = listIterator(fromIndex);
        for (int i=0, n=toIndex-fromIndex; i<n; i++) {
            it.next();
            it.remove();
        }
    }

    /**
     * The number of times this list has been <i>structurally modified</i>.
     * Structural modifications are those that change the size of the
     * list, or otherwise perturb it in such a fashion that iterations in
     * progress may yield incorrect results.<p>
     *
     * This field is used by the iterator and list iterator implementation
     * returned by the <tt>iterator</tt> and <tt>listIterator</tt> methods.
     * If the value of this field changes unexpectedly, the iterator (or list
     * iterator) will throw a <tt>ConcurrentModificationException</tt> in
     * response to the <tt>next</tt>, <tt>remove</tt>, <tt>previous</tt>,
     * <tt>set</tt> or <tt>add</tt> operations.  This provides
     * <i>fail-fast</i> behavior, rather than non-deterministic behavior in
     * the face of concurrent modification during iteration.<p>
     *
     * <b>Use of this field by subclasses is optional.</b> If a subclass
     * wishes to provide fail-fast iterators (and list iterators), then it
     * merely has to increment this field in its <tt>add(int, Object)</tt> and
     * <tt>remove(int)</tt> methods (and any other methods that it overrides
     * that result in structural modifications to the list).  A single call to
     * <tt>add(int, Object)</tt> or <tt>remove(int)</tt> must add no more than
     * one to this field, or the iterators (and list iterators) will throw
     * bogus <tt>ConcurrentModificationExceptions</tt>.  If an implementation
     * does not wish to provide fail-fast iterators, this field may be
     * ignored.
     */
    protected transient int modCount = 0;
    
    
    //------------------------------------------------------------------------
    // Extends AbstractCollection
    //------------------------------------------------------------------------

    /**
     * Returns <tt>true</tt> if this collection contains no elements.<p>
     *
     * This implementation returns <tt>size() == 0</tt>.
     *
     * @return <tt>true</tt> if this collection contains no elements.
     */
    public boolean isEmpty() {
    return size() == 0;
    }

    /**
     * Returns <tt>true</tt> if this collection contains the specified
     * element.  More formally, returns <tt>true</tt> if and only if this
     * collection contains at least one element <tt>e</tt> such that
     * <tt>(o==null ? e==null : o.equals(e))</tt>.<p>
     *
     * This implementation iterates over the elements in the collection,
     * checking each element in turn for equality with the specified element.
     *
     * @param o object to be checked for containment in this collection.
     * @return <tt>true</tt> if this collection contains the specified element.
     */
    public boolean contains(Object o) {
    Iterator<E> e = iterator();
    if (o==null) {
        while (e.hasNext())
        if (e.next()==null)
            return true;
    } else {
        while (e.hasNext())
        if (o.equals(e.next()))
            return true;
    }
    return false;
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
    Object[] result = new Object[size()];
    Iterator<E> e = iterator();
    for (int i=0; e.hasNext(); i++)
        result[i] = e.next();
    return result;
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
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] _a) {
        T[] a = _a;
        int size = size();
        if (a.length < size) a = (T[])java.lang.reflect.Array.newInstance(
            a.getClass().getComponentType(), 
            size
        );
        Iterator<E> it=iterator();
        for (int i=0; i<size; i++)
            a[i] = (T)it.next();
        if (a.length > size)
            a[size] = null;
        return a;
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
     * This implementation iterates over the collection looking for the
     * specified element.  If it finds the element, it removes the element
     * from the collection using the iterator's remove method.<p>
     *
     * Note that this implementation throws an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by this
     * collection's iterator method does not implement the <tt>remove</tt>
     * method.
     *
     * @param o element to be removed from this collection, if present.
     * @return <tt>true</tt> if the collection contained the specified
     *         element.
     * 
     * @throws UnsupportedOperationException if the <tt>remove</tt> method is
     *        not supported by this collection.
     */
    public boolean remove(Object o) {
    Iterator<?> e = iterator();
    if (o==null) {
        while (e.hasNext()) {
        if (e.next()==null) {
            e.remove();
            return true;
        }
        }
    } else {
        while (e.hasNext()) {
        if (o.equals(e.next())) {
            e.remove();
            return true;
        }
        }
    }
    return false;
    }


    // Bulk Operations

    /**
     * Returns <tt>true</tt> if this collection contains all of the elements
     * in the specified collection. <p>
     *
     * This implementation iterates over the specified collection, checking
     * each element returned by the iterator in turn to see if it's
     * contained in this collection.  If all elements are so contained
     * <tt>true</tt> is returned, otherwise <tt>false</tt>.
     *
     * @param c collection to be checked for containment in this collection.
     * @return <tt>true</tt> if this collection contains all of the elements
     *         in the specified collection.
     * 
     * @see #contains(Object)
     */
    public boolean containsAll(Collection<?> c) {
    Iterator<?> e = c.iterator();
    while (e.hasNext())
        if(!contains(e.next()))
        return false;

    return true;
    }

    /**
     * Removes from this collection all of its elements that are contained in
     * the specified collection (optional operation). <p>
     *
     * This implementation iterates over this collection, checking each
     * element returned by the iterator in turn to see if it's contained
     * in the specified collection.  If it's so contained, it's removed from
     * this collection with the iterator's <tt>remove</tt> method.<p>
     *
     * Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by the
     * <tt>iterator</tt> method does not implement the <tt>remove</tt> method.
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
    boolean modified = false;
    Iterator<?> e = iterator();
    while (e.hasNext()) {
        if(c.contains(e.next())) {
        e.remove();
        modified = true;
        }
    }
    return modified;
    }

    /**
     * Retains only the elements in this collection that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this collection all of its elements that are not contained in the
     * specified collection. <p>
     *
     * This implementation iterates over this collection, checking each
     * element returned by the iterator in turn to see if it's contained
     * in the specified collection.  If it's not so contained, it's removed
     * from this collection with the iterator's <tt>remove</tt> method.<p>
     *
     * Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by the
     * <tt>iterator</tt> method does not implement the remove method.
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
    boolean modified = false;
    Iterator<?> e = iterator();
    while (e.hasNext()) {
        if(!c.contains(e.next())) {
        e.remove();
        modified = true;
        }
    }
    return modified;
    }

}
