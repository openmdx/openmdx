/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Lists.java,v 1.4 2008/11/27 16:46:56 hburger Exp $
 * Description: Lists 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/27 16:46:56 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Lists
 */
public class Lists {
    
    /**
     * Constructor 
     */
    private Lists(){
        // Avoid instantiation
    }

    /**
     * Create a sub-list
     * 
     * @param source the given list
     * @param fromIndex low endpoint (inclusive) of the subList.
     * @param toIndex high endpoint (exclusive) of the subList.
     * 
     * @return a sub-list
     */
    public static <E> List<E> subList(
        List<E> source,
        Number fromIndex,
        Number toIndex
    ){
        int lowerBound = fromIndex == null ? 0 : fromIndex.intValue();
        return toIndex == null ? (
            lowerBound == 0 ? source : new TailList<E>(source, lowerBound)
        ) : source.subList(lowerBound, toIndex.intValue());
    }
    
    /**
     * List array factory
     * 
     * @param length the number of lists
     * 
     * @return a newly created typed list array
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T>[] newListArray(
        int length
    ){
        return new List[length];
    }

    
    //------------------------------------------------------------------------
    // Class TailList
    //------------------------------------------------------------------------
    
    /**
     * Tail List
     * <P>
     * No method except size() and toArray() invoke the list's size metho
     */
    static final class TailList<E> implements List<E> {

        /**
         * Constructor 
         * @param list
         * @param offset
         */
        TailList(
            List<E> list, 
            int offset
        ) {
            this.list = list;
            this.offset = offset;
        }

        /**
         * The sub-list's begin-index
         */
        private final int offset;
        
        /**
         * The whole list
         */
        private final List<E> list;

        /**
         * @param o
         * @return
         * @see java.util.List#add(java.lang.Object)
         */
        public boolean add(E o) {
            return this.list.add(o);
        }

        /**
         * @param index
         * @param element
         * @see java.util.List#add(int, java.lang.Object)
         */
        public void add(int index, E element) {
            this.list.add(
                this.offset + index, 
                element
             );
        }

        /**
         * @param c
         * @return
         * @see java.util.List#addAll(java.util.Collection)
         */
        public boolean addAll(Collection<? extends E> c) {
            return this.list.addAll(c);
        }

        /**
         * @param index
         * @param c
         * @return
         * @see java.util.List#addAll(int, java.util.Collection)
         */
        public boolean addAll(int index, Collection<? extends E> c) {
            return this.list.addAll(
                this.offset + index, 
                c
            );
        }

        /**
         * 
         * @see java.util.List#clear()
         */
        public void clear() {
            for(
                Iterator<E> i = this.list.listIterator(offset);
                i.hasNext();
            ){
                i.remove();
            }
        }

        /**
         * @param o
         * @return
         * @see java.util.List#contains(java.lang.Object)
         */
        public boolean contains(Object o) {
            for(
                ListIterator<E> tailIterator = listIterator();
                tailIterator.hasNext();
            ){
                E e = tailIterator.next();
                if(o == null ? e == null : o.equals(e)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * @param c
         * @return
         * @see java.util.List#containsAll(java.util.Collection)
         */
        public boolean containsAll(Collection<?> c) {
            List<Object> t = new ArrayList<Object>(c);
            for(
                ListIterator<E> tailIterator = listIterator();
                !t.isEmpty() && tailIterator.hasNext();
            ){
                t.remove(tailIterator.next());
            }
            return t.isEmpty();
        }

        /**
         * @param o
         * @return
         * @see java.util.List#equals(java.lang.Object)
         */
        public boolean equals(Object o) {
            if(o instanceof List) {
                ListIterator<?> thatIterator = ((List<?>)o).listIterator();
                for(
                    ListIterator<E> tailIterator = listIterator(); 
                    tailIterator.hasNext();
                ){
                    if(thatIterator.hasNext()) {
                        E thisElement = tailIterator.next();
                        Object thatElement = thatIterator.next();
                        if(thisElement == null ? thatElement != null : !thisElement.equals(thatElement)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
                return !thatIterator.hasNext();
            } else {
                return false;
            }
        }

        /**
         * @param index
         * @return
         * @see java.util.List#get(int)
         */
        public E get(int index) {
            return this.list.get(this.offset + index);
        }

        /**
         * @return
         * @see java.util.List#hashCode()
         */
        public int hashCode() {
            int hashCode = 1;
            for(
                ListIterator<E> tailIterator = listIterator();
                tailIterator.hasNext();
            ){
                E e = tailIterator.next();
                hashCode = 31*hashCode + (e==null ? 0 : e.hashCode());
            }
            return hashCode;
        }

        /**
         * @param o
         * @return
         * @see java.util.List#indexOf(java.lang.Object)
         */
        public int indexOf(Object o) {
            for(
                ListIterator<E> tailIterator = listIterator();
                tailIterator.hasNext();
            ){
                int i = tailIterator.nextIndex();
                Object e = tailIterator.next();
                if(o == null ? e == null : o.equals(e)) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * @return
         * @see java.util.List#isEmpty()
         */
        public boolean isEmpty() {
            return !listIterator().hasNext();
        }

        /**
         * @return
         * @see java.util.List#iterator()
         */
        public Iterator<E> iterator() {
            return listIterator();
        }

        /**
         * @param o
         * @return
         * @see java.util.List#lastIndexOf(java.lang.Object)
         */
        public int lastIndexOf(Object o) {
            int index = -1;
            for(
                ListIterator<E> tailIterator = listIterator();
                tailIterator.hasNext();
            ){
                int i = tailIterator.nextIndex();
                Object e = tailIterator.next();
                if(o == null ? e == null : o.equals(e)) {
                    index = i;
                }
            }
            return index;
        }

        /**
         * @return
         * @see java.util.List#listIterator()
         */
        public ListIterator<E> listIterator() {
            return this.list.listIterator(
                this.offset
            );
        }

        /**
         * @param index
         * @return
         * @see java.util.List#listIterator(int)
         */
        public ListIterator<E> listIterator(int index) {
            return this.list.listIterator(
                this.offset + index
            );
        }

        /**
         * @param index
         * @return
         * @see java.util.List#remove(int)
         */
        public E remove(int index) {
            return this.list.remove(
                this.offset + index
            );
        }

        /**
         * @param o
         * @return
         * @see java.util.List#remove(java.lang.Object)
         */
        public boolean remove(Object o) {
            for(
                ListIterator<E> tailIterator = listIterator();
                tailIterator.hasNext();
            ){
                Object e = tailIterator.next();
                if(o == null ? e == null : o.equals(e)) {
                    tailIterator.remove();
                    return true;
                }
            }
            return false;
        }

        /**
         * @param c
         * @return
         * @see java.util.List#removeAll(java.util.Collection)
         */
        public boolean removeAll(Collection<?> c) {
            boolean modified = false;
            for(
                ListIterator<E> tailIterator = listIterator();
                tailIterator.hasNext();
            ){
                if(c.contains(tailIterator.next())){
                    modified = true;
                    tailIterator.remove();
                }
            }
            return modified;
        }

        /**
         * @param c
         * @return
         * @see java.util.List#retainAll(java.util.Collection)
         */
        public boolean retainAll(Collection<?> c) {
            boolean modified = false;
            for(
                ListIterator<E> tailIterator = listIterator();
                tailIterator.hasNext();
            ){
                if(!c.contains(tailIterator.next())){
                    modified = true;
                    tailIterator.remove();
                }
            }
            return modified;
        }

        /**
         * @param index
         * @param element
         * @return
         * @see java.util.List#set(int, java.lang.Object)
         */
        public E set(int index, E element) {
            return this.list.set(
                this.offset + index, 
                element
            );
        }

        /**
         * @return
         * @see java.util.List#size()
         */
        public int size() {
            return this.list.size() - this.offset;
        }

        /**
         * @param fromIndex
         * @param toIndex
         * @return
         * @see java.util.List#subList(int, int)
         */
        public List<E> subList(int fromIndex, int toIndex) {
            return this.list.subList(
                this.offset + fromIndex, 
                this.offset + toIndex
            );
        }

        /**
         * @return
         * @see java.util.List#toArray()
         */
        public Object[] toArray() {
            return this.list.subList(
                this.offset, this.list.size()
            ).toArray();
        }

        /**
         * @param <T>
         * @param a
         * @return
         * @see java.util.List#toArray(T[])
         */
        public <T> T[] toArray(T[] a) {
            return this.list.subList(
                this.offset, 
                this.list.size()
            ).toArray(a);
        }

    }    

}
