/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Collection View
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2012, OMEX AG, Switzerland
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

import java.lang.reflect.Array;
import java.util.Collection;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.BasicException.Parameter;
import org.openmdx.kernel.exception.Throwables;


/**
 * List View
 */
abstract class CollectionView<O,C extends Collection<E>,E> implements Collection<E> {

    /**
     * Constructor 
     *
     * @param members
     */
    protected CollectionView (
        InvolvedMembers<O,C> members
    ){
        this.members = members;
    }
    
    /**
     * The collection's delegates
     */
    protected final InvolvedMembers<O,C> members;
    
    /**
     * Provide the id Parameter for exception enhancement
     * 
     * @return the id parameter
     * 
     * @see org.openmdx.state2.aop1.InvolvedMembers#getIdParameter()
     */
    protected Parameter getIdParameter() {
        return this.members.getIdParameter();
    }

    /* (non-Javadoc)
     * @see java.util.List#add(java.lang.Object)
     */
    public boolean add(E o) {
        boolean modified = false;
        for(C delegate : members.getInvolved(AccessMode.FOR_UPDATE)) {
            modified |= delegate.add(o);
        }
        return modified;
    }

    /* (non-Javadoc)
     * @see java.util.List#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends E> c) {
        boolean modified = false;
        for(C delegate : members.getInvolved(AccessMode.FOR_UPDATE)) {
            modified |= delegate.addAll(c);
        }
        return modified;
    }

    /* (non-Javadoc)
     * @see java.util.List#clear()
     */
    public void clear() {
        for(C delegate : members.getInvolved(AccessMode.FOR_UPDATE)) {
            delegate.clear();
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        UniqueValue<Boolean> reply = new UniqueValue<Boolean>();
        try{
            for(C delegate : members.getInvolved(AccessMode.FOR_QUERY)) {
                reply.set(Boolean.valueOf(delegate.contains(o)));
            }
            return reply.get();
        } catch (ServiceException exception) {
            throw Throwables.initCause(
                new IllegalStateException(
                    "The underlying states do not allow for the determination of the given property"
                ),
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                getIdParameter()
            );
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> c) {
        try {
            UniqueValue<Boolean> reply = new UniqueValue<Boolean>();
            for(C delegate : members.getInvolved(AccessMode.FOR_QUERY)) {
                reply.set(Boolean.valueOf(delegate.containsAll(c)));
            }
            return reply.get();
        } catch (ServiceException exception) {
            throw Throwables.initCause(
                new IllegalStateException(
                    "The underlying states do not allow for the unique determination of the given property"
                ),
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                getIdParameter()
            );
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#isEmpty()
     */
    public boolean isEmpty() {
        try {
            UniqueValue<Boolean> reply = new UniqueValue<Boolean>();
            for(C delegate : members.getInvolved(AccessMode.FOR_QUERY)) {
                reply.set(Boolean.valueOf(delegate.isEmpty()));
            }
            return reply.get();
        } catch (ServiceException exception) {
            throw Throwables.initCause(
                new IllegalStateException(
                    "The underlying states do not allow for the unique determination of the given property"
                ),
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                getIdParameter()
            );
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        boolean modified = false;
        for(C delegate : members.getInvolved(AccessMode.FOR_UPDATE)) {
            modified |= delegate.remove(o);
        }
        return modified;
    }

    /* (non-Javadoc)
     * @see java.util.List#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for(C delegate : members.getInvolved(AccessMode.FOR_UPDATE)) {
            modified |= delegate.removeAll(c);
        }
        return modified;
    }

    /* (non-Javadoc)
     * @see java.util.List#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        for(C delegate : members.getInvolved(AccessMode.FOR_UPDATE)) {
            modified |= delegate.retainAll(c);
        }
        return modified;
    }

    /* (non-Javadoc)
     * @see java.util.List#size()
     */
    public int size() {
        try {
            UniqueValue<Integer> reply = new UniqueValue<Integer>();
            for(C delegate : members.getInvolved(AccessMode.FOR_QUERY)) {
                reply.set(Integer.valueOf(delegate.size()));
            }
            return reply.get().intValue();
        } catch (ServiceException exception) {
            throw Throwables.initCause(
                new IllegalStateException(
                    "The underlying states do not allow for the unique determination of the given property"
                ),
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                getIdParameter()
            );
        }
    }

    /* (non-Javadoc)
     * @see java.util.List#toArray()
     */
    public Object[] toArray() {
        Object[] reply = new Object[size()];
        int i = 0;
        for(Object member : this) {
            reply[i++] = member;
        }
        return reply;
    }

    /* (non-Javadoc)
     * @see java.util.List#toArray(T[])
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        int length = size();
        Class<? extends T> componentType = (Class<? extends T>) a.getClass().getComponentType();
        T[] reply = length > a.length ? (T[])Array.newInstance(componentType, length) : a;
        int i = 0;
        for(Object member : this) {
            reply[i++] = componentType.cast(member);
        }
        return reply;
    }
        
}