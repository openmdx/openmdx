/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: CompactSparseList.java,v 1.1 2009/01/05 13:47:16 wfro Exp $
 * Description: Property
 * Revision:    $Revision: 1.1 $
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

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * A compact sparse list is in non-delegate mode as long as its
 * size is less than or equal 1 and gets into delegate mode when
 * growing.
 */
public class CompactSparseList<E>
    implements SparseList<E>, Cloneable, Serializable
{

    /**
     * Creates a <code>CompactSparseList</code> object.
     */
    public CompactSparseList(
    ){
        this.value = null;
        this.delegate = null;
    }

    /**
     * Creates <code>CompactSparseList</code> object with the initial value.
     */
    public CompactSparseList(
        E value
    ) {
        this.value = value;
        this.delegate = null;
    }
    
    /**
     * Creates a <code>CompactSparseList</code> object with initial capacity n.
     */
    public CompactSparseList(
        int n
    ){
        this.value = null;
        this.delegate = n <= 1 ? null : new OffsetArrayList<E>(n);
    }

    /**
     * Creates a <code>OffsetArrayList</code> object.
     *
     * @param  values  A non-null collection of property values
     */
    public CompactSparseList(
        Collection<? extends E>  collection
    ){
        int n = collection.size();
        if(n > 1) {
            this.value = null;
            this.delegate = new OffsetArrayList<E>(collection);            
        } else {
            this.value = n == 0 ? null : collection.iterator().next();
            this.delegate = null;            
        }
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -8876321469812398929L;

    /**
     * Delegate values
     */
    private SparseList<E> delegate;
    
    /**
     * Non-Delegate value
     */
    private E value;
    
    //------------------------------------------------------------------------
    // Implements SparseList
    //------------------------------------------------------------------------
    
    @SuppressWarnings("unchecked")
    private List<E> nonDelegate(){
        return this.value == null ? 
            (List<E>)Collections.EMPTY_LIST : 
            Collections.singletonList(this.value);        
    }
    
    private synchronized SparseList<E> getDelegate(){
        if(this.delegate == null) {
            this.delegate = new OffsetArrayList<E>();
            if(this.value != null) {
                this.delegate.add(this.value);
                this.value = null;
            }
        }
        return this.delegate;
    }
    
    private List<E> getList(){
        return this.delegate == null ? nonDelegate() : this.delegate;
    }
    
    /* (non-Javadoc)
     * @see java.util.List#add(int, java.lang.Object)
     */
    public void add(int index, E element) {
        if(this.delegate == null) {
            if(index == 0 && this.value == null) {
                this.value = element;
            } else {
                getDelegate().add(index, element);
            }
        } else {
            this.delegate.add(index, element);
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#add(java.lang.Object)
     */
    public boolean add(E o) {
        if(o == null) {
            return false;
        } else if(this.delegate == null) {
            if(this.value == null) {
                this.value = o;
                return true;
            } else {
                return getDelegate().add(o);
            }
        } else {
            return this.delegate.add(o);
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends E> c) {
        if(c.isEmpty()) {
            return false;
        } else if(this.delegate == null) {
            if(this.value != null || c.size() > 1) {
                return getDelegate().addAll(c);
            } else {
                return (this.value = c.iterator().next()) != null;
            }
        } else {
            return this.delegate.addAll(c);
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#addAll(int, java.util.Collection)
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        if(c.isEmpty()) {
            return false;
        } else if(this.delegate == null) {
            if(index > 0 || this.value != null || c.size() > 1) {
                return getDelegate().addAll(index, c);
            } else {
                return (this.value = c.iterator().next()) != null;
            }
        } else {
            return this.delegate.addAll(index, c);
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#clear()
     */
    public void clear() {
        this.delegate = null;
        this.value = null;
    }

    /* (non-Javadoc)
     * @see java.util.List#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        if(this.delegate == null) {
            return this.value != null && this.value.equals(o);
        } else {
            return this.delegate.contains(o);
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> c) {
        if(this.delegate == null) {
            for(
                Iterator<?> i = c.iterator();
                i.hasNext();
            ) if(
                !contains(i.next())
            ) return false;
            return true;
        } else {
            return this.delegate.containsAll(c);
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if(this.delegate == null) {
            if(o instanceof SparseList) {
                SparseList<?> l = (SparseList<?>)o;
                if(this.value == null) {
                    return l.isEmpty();
                } else if(l.size() == 1) {
                    return this.value.equals(l.get(0));
                }
                else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return this.delegate.equals(o);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.collection.SparseList#firstIndex()
     */
    public int firstIndex() {
        if(this.delegate == null) {
            return 0;
        } else {
            return this.delegate.firstIndex();
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#get(int)
     */
    public E get(int index) {
        if(this.delegate == null) {
            return index == 0 ? this.value : null;
        } else {
            return this.delegate.get(index);
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#hashCode()
     */
    public int hashCode() {
        switch(size()) {
            case 0: return 0;
            case 1: return get(0).hashCode();
            default: return this.delegate.hashCode(); 
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#indexOf(java.lang.Object)
     */
    public int indexOf(Object o) {
        if(this.delegate == null) {
            if(this.value == null) {
                return o == null ? 0 : -1;
            } else {
                return this.value.equals(o) ? 0 : -1;
            }
        } else {
            return this.delegate.indexOf(o);
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#isEmpty()
     */
    public boolean isEmpty() {
        if(this.delegate == null) {
            return this.value == null;
        } else {
            return this.delegate.isEmpty();
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#iterator()
     */
    public Iterator<E> iterator() {
        if(this.delegate == null) {
            return new NonDelegateIterator();
        } else {
            return this.delegate.iterator();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.collection.SparseList#lastIndex()
     */
    public int lastIndex() {
        if(this.delegate == null) {
            return this.value == null ? -1 : 0;
        } else {
            return this.delegate.lastIndex();
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object o) {
        if(this.delegate == null) {
            if(this.value == null) {
                return o == null ? 0 : -1;
            } else {
                return this.value.equals(o) ? 0 : -1;
            }
        } else {
            return this.delegate.lastIndexOf(o);
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#listIterator()
     */
    public ListIterator<E> listIterator() {
        if(this.delegate == null) {
            return new NonDelegateIterator();
        } else {
            return this.delegate.listIterator();
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#listIterator(int)
     */
    public ListIterator<E> listIterator(int index) {
        if(this.delegate == null) {
            return new NonDelegateIterator(index);
        } else {
            return this.delegate.listIterator(index);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.collection.SparseList#population()
     */
    public List<E> population() {
        if(this.delegate == null) {
            return nonDelegate();
        } else {
            return this.delegate.population();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.collection.SparseList#populationIterator()
     */
    public ListIterator<E> populationIterator() {
        if(this.delegate == null) {
            return new NonDelegateIterator();
        } else {
            return this.delegate.populationIterator();
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#remove(int)
     */
    public E remove(int index) {
        if(this.delegate == null) {
            if(index < 0) throw new IndexOutOfBoundsException(
                "Index " + index + " is less than 0"
            );
            if(index < 0 || index > size()) throw new IndexOutOfBoundsException(
                "Index " + index + " is greater than size " + size()
            );
            E value = this.value;
            this.value = null;
            return value;
        } else {
            return this.delegate.remove(index);
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        if(this.delegate == null) {
            boolean modify = contains(o);
            if(modify) this.value = null;
            return modify;
        } else {
            return this.delegate.remove(o);
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection<?> c) {
        if(this.delegate == null) {
            if(this.value == null) {
                return false;
            } else {
                for(
                    Iterator<?> i = c.iterator();
                    i.hasNext();
                ) {
                    if(remove(i.next())) return true;  
                }
                return false;
            }
        } else {
            return this.delegate.removeAll(c);
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> c) {
        if(this.delegate == null) {
            if(this.value == null){
                return false;
            } else {
                boolean modify = !c.contains(this.value);
                if(modify) this.value = null;
                return modify;
            }
        } else {
            return this.delegate.retainAll(c);
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#set(int, java.lang.Object)
     */
    public E set(int index, E element) {
        if(this.delegate == null) {
            if(index == 0) {
                E value = this.value;
                this.value = element;
                return value;
                
            } else {
                return getDelegate().set(index, element);
            }
        } else {
            return this.delegate.set(index, element);
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#size()
     */
    public int size() {
        if(this.delegate == null) {
            return this.value == null ? 0 : 1;
        } else {
            return this.delegate.size();
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#subList(int, int)
     */
    public List<E> subList(int fromIndex, int toIndex) {
        if(this.delegate == null) {
            if(this.value == null || fromIndex > 0) {
                return Collections.nCopies(toIndex - fromIndex, null);
            } else if (toIndex == 1){
                return nonDelegate();
            } else {
                return getDelegate().subList(fromIndex, toIndex);
            }
        } else {
            return this.delegate.subList(fromIndex, toIndex);
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#toArray()
     */
    public Object[] toArray() {
        if(this.delegate == null) {
            return this.value == null ? EMPTY_ARRAY : new Object[]{this.value};
        } else {
            return this.delegate.toArray();
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#toArray(java.lang.Object[])
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] _a) {
        T[] a = _a;
        if(this.delegate == null) {
            if(this.value != null) {
                if(a.length == 0) a = (T[]) Array.newInstance(
                    a.getClass().getComponentType(),
                    1
                );
                a[0] = (T) this.value;
            }
            return a;
        } else {
            return this.delegate.toArray(a);
        }
    }
        
    //------------------------------------------------------------------------
    // Members
    //------------------------------------------------------------------------
    private static final Object[] EMPTY_ARRAY = new Object[0];
    
    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    protected Object clone(
    ) {
        return new CompactSparseList<E>(this);
    }

    
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getList().toString();
    }

    
    //------------------------------------------------------------------------
    // Class NonDelegateIterator
    //------------------------------------------------------------------------
    
    //------------------------------------------------------------------------
    // Class NonDelegateIterator
    //------------------------------------------------------------------------
    
    class NonDelegateIterator implements ListIterator<E> {

        /**
         * 
         */
        private int nextIndex;

        /**
         * 
         */
        private int currentIndex;
        
        /**
         * 
         */
        private int previousIndex;
        
        /**
         * Constructor
         *  
         * @param index
         */
        NonDelegateIterator(
            int index
        ){
            this.nextIndex = index;
            this.currentIndex = -2;
            this.previousIndex = index -1;
        }

        /**
         * Constructor
         */
        NonDelegateIterator(
        ){
            this(0);
        }
        
        /* (non-Javadoc)
         * @see java.util.ListIterator#nextIndex()
         */
        public int nextIndex() {
            return this.nextIndex;
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#previousIndex()
         */
        public int previousIndex() {
            return this.previousIndex;
        }

        private void invalidateCursor(){
            if(this.currentIndex != 0) throw new IllegalStateException();
            this.currentIndex = -2;
        }

        private E moveCursor(){
            this.currentIndex = 0;
            this.previousIndex = -1;
            this.nextIndex = 1;
            return CompactSparseList.this.get(0);
        }
        
        /* (non-Javadoc)
         * @see java.util.ListIterator#remove()
         */
        public void remove() {
            invalidateCursor();
            CompactSparseList.this.set(0, null);
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#hasNext()
         */
        public boolean hasNext() {
            return nextIndex() < CompactSparseList.this.size();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#hasPrevious()
         */
        public boolean hasPrevious() {
            return previousIndex() >= 0;
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#next()
         */
        public E next() {
            if(!hasNext()) throw new NoSuchElementException();
            return moveCursor();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#previous()
         */
        public E previous() {
            if(!hasPrevious()) throw new NoSuchElementException();
            return moveCursor();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#add(java.lang.Object)
         */
        public void add(Object o) {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#set(java.lang.Object)
         */
        public void set(E o) {
            invalidateCursor();
            CompactSparseList.this.set(0, o);
        }
        
    }
    
}
