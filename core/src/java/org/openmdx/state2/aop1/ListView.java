/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ListView.java,v 1.2 2009/01/10 12:12:12 wfro Exp $
 * Description: List View
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/10 12:12:12 $
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
package org.openmdx.state2.aop1;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.exception.ServiceException;

/**
 * List View
 */
final class ListView 
    extends CollectionView<DataObject_1_0,List<Object>,Object> 
    implements List<Object> 
{

    /**
     * Constructor 
     *
     * @param involvedMembers
     */
    private ListView (
        InvolvedMembers<DataObject_1_0,List<Object>> involvedMembers
    ){
        super(involvedMembers);
    }

    /**
     * List View Factory Method
     * 
     * @param involvedStates
     * @param feature
     * 
     * @return a new list view
     */
    static List<Object> newObjectList(
        final Involved<DataObject_1_0> involvedStates,
        final String feature
    ){
        return new ListView(
            new InvolvedMembers<DataObject_1_0,List<Object>>(
                involvedStates,
                feature
            ) {

                @Override
                protected List<Object> getMember(
                    DataObject_1_0 state
                ) throws ServiceException {
                    return state.objGetList(feature);
                }
                
            }
        );
        
    }
    
    /* (non-Javadoc)
     * @see java.util.List#iterator()
     */
    public Iterator<Object> iterator() {
        return listIterator();
    }

    /* (non-Javadoc)
     * @see java.util.List#add(int, java.lang.Object)
     */
    public void add(int index, Object element) {
        for(List<Object> delegate : members.getInvolved(AccessMode.FOR_UPDATE)) {
            if(index > delegate.size()) {
                throw new IndexOutOfBoundsException(
                    "The size of the list in one of the underlying states is to small"
                );
            }
        }
        for(List<Object> delegate : members.getInvolved(AccessMode.FOR_UPDATE)) {
            delegate.add(index, element);
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#addAll(int, java.util.Collection)
     */
    public boolean addAll(int index, Collection<? extends Object> c) {
        for(List<Object> delegate : members.getInvolved(AccessMode.FOR_UPDATE)) {
            if(index > delegate.size()) {
                throw new IndexOutOfBoundsException(
                    "The size of the list in one of the underlyaing states is to small"
                );
            }
        }
        boolean modified = false;
        for(List<Object> delegate : members.getInvolved(AccessMode.FOR_UPDATE)) {
            modified |= delegate.addAll(index, c);
        }
        return modified;
    }

    /* (non-Javadoc)
     * @see java.util.List#get(int)
     */
    public Object get(int index) {
        UniqueValue<Object> reply = new UniqueValue<Object>();
        for(List<Object> delegate : members.getInvolved(AccessMode.FOR_QUERY)) {
            reply.set(delegate.get(index));
        }
        return reply.get();
    }

    /* (non-Javadoc)
     * @see java.util.List#indexOf(java.lang.Object)
     */
    public int indexOf(Object o) {
        UniqueValue<Integer> reply = new UniqueValue<Integer>();
        for(List<Object> delegate : this.members.getInvolved(AccessMode.FOR_QUERY)) {
            reply.set(Integer.valueOf(delegate.indexOf(o)));
        }
        return reply.get().intValue();
    }

    /* (non-Javadoc)
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object o) {
        UniqueValue<Integer> reply = new UniqueValue<Integer>();
        for(List<Object> delegate : this.members.getInvolved(AccessMode.FOR_QUERY)) {
            reply.set(Integer.valueOf(delegate.lastIndexOf(o)));
        }
        return reply.get().intValue();
    }

    /* (non-Javadoc)
     * @see java.util.List#listIterator()
     */
    public ListIterator<Object> listIterator() {
        return listIterator(0);
    }

    /* (non-Javadoc)
     * @see java.util.List#listIterator(int)
     */
    public ListIterator<Object> listIterator(int index) {
        return new ViewIterator(index);
    }

    /* (non-Javadoc)
     * @see java.util.List#remove(int)
     */
    public Object remove(int index) {
        UniqueValue<Object> reply = new UniqueValue<Object>();
        for(List<Object> delegate : members.getInvolved(AccessMode.FOR_UPDATE)) {
            reply.set(delegate.get(index));
        }
        for(List<Object> delegate : members.getInvolved(AccessMode.FOR_UPDATE)) {
            delegate.remove(index);
        }
        return reply.get();
    }

    /* (non-Javadoc)
     * @see java.util.List#set(int, java.lang.Object)
     */
    public Object set(int index, Object element) {
        UniqueValue<Object> reply = new UniqueValue<Object>();
        for(List<Object> delegate : members.getInvolved(AccessMode.FOR_UPDATE)) {
            reply.set(delegate.get(index));
        }
        for(List<Object> delegate : members.getInvolved(AccessMode.FOR_UPDATE)) {
            delegate.set(index, element);
        }
        return reply.get();
    }

    /* (non-Javadoc)
     * @see java.util.List#subList(int, int)
     */
    public List<Object> subList(
        final int fromIndex, 
        final int toIndex
    ) {
        return new ListView(
            new InvolvedMembers<DataObject_1_0,List<Object>>(
                members.involvedStates,
                members.feature
            ) {

                @Override
                protected List<Object> getMember(
                    DataObject_1_0 state
                ) throws ServiceException {
                    return state.objGetList(feature).subList(fromIndex, toIndex);
                }
                
            }
            
        );
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof List) {
            List<?> that = (List<?>) obj;
            int size = this.size();
            if(size == that.size()) {
                for(
                    int i = 0;
                    i < size;
                    i++
                ){
                    Object thisMember = this.get(i);
                    Object thatMember = that.get(i);
                    if(thisMember == null ? thatMember != null : !thisMember.equals(thatMember)) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = 1;
        for(Object member : this) {
            hash *= 31;
            hash += member == null ? 0 : member.hashCode();
        }
        return hash;  
    }

    
    //------------------------------------------------------------------------
    // Class ViewIterator
    //------------------------------------------------------------------------
    

    /**
     * View Iterator
     */
    class ViewIterator implements ListIterator<Object> {

        /**
         * Constructor 
         *
         * @param index
         */
        ViewIterator(
            int index
        ){
            this.nextIndex = index;
            this.previousIndex = index - 1;
            this.currentIndex = - 1;
        }

        /**
         * 
         */
        private int nextIndex;
        
        /**
         * 
         */
        private int previousIndex;

        /**
         * 
         */
        private int currentIndex;
        
        /* (non-Javadoc)
         * @see java.util.ListIterator#add(java.lang.Object)
         */
        public void add(Object o) {
            if(this.currentIndex < 0) {
                throw new IllegalStateException("No current element");
            }
            for(List<Object> delegate : members.getInvolved(AccessMode.FOR_UPDATE)) {
                delegate.add(this.nextIndex, o);
            }
            this.previousIndex++;
            this.nextIndex++;
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#hasNext()
         */
        public boolean hasNext() {
            UniqueValue<Boolean> reply = new UniqueValue<Boolean>();
            for(List<Object> delegate : members.getInvolved(AccessMode.FOR_QUERY)) {
                reply.set(Boolean.valueOf(nextIndex < delegate.size()));
            }
            return reply.get().booleanValue();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#hasPrevious()
         */
        public boolean hasPrevious() {
            return this.previousIndex >= 0;
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#next()
         */
        public Object next() {
            UniqueValue<Object> reply = new UniqueValue<Object>();
            this.currentIndex = this.nextIndex;
            for(List<Object> delegate : members.getInvolved(AccessMode.FOR_QUERY)) {
                reply.set(delegate.get(this.currentIndex));
            }
            this.previousIndex = this.nextIndex++;
            return reply.get();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#nextIndex()
         */
        public int nextIndex() {
            return this.nextIndex;

        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#previous()
         */
        public Object previous() {
            UniqueValue<Object> reply = new UniqueValue<Object>();
            this.currentIndex = this.previousIndex;
            for(List<Object> delegate : members.getInvolved(AccessMode.FOR_QUERY)) {
                reply.set(delegate.get(this.currentIndex));
            }
            this.nextIndex = this.previousIndex--;
            return reply.get();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#previousIndex()
         */
        public int previousIndex() {
            return this.previousIndex;
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#remove()
         */
        public void remove() {
            if(this.currentIndex < 0) {
                throw new IllegalStateException("No current element");
            }
            for(List<Object> delegate : members.getInvolved(AccessMode.FOR_UPDATE)) {
                delegate.remove(this.currentIndex);
            }
            this.currentIndex = -1;
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#set(java.lang.Object)
         */
        public void set(Object o) {
            if(this.currentIndex < 0) {
                throw new IllegalStateException("No current element");
            }
            for(List<Object> delegate : members.getInvolved(AccessMode.FOR_UPDATE)) {
                delegate.set(this.currentIndex, o);
            }
        }
        
    }

}