/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: MarshallingSequentialList.java,v 1.25 2009/02/09 15:29:44 hburger Exp $
 * Description: SPICE Collections: Merging List
 * Revision:    $Revision: 1.25 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/09 15:29:44 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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

import java.io.OutputStream;
import java.io.Serializable;
import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.ExceptionListenerMarshaller;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;

/**
 * A Marshalling Sequential List
 */
public class MarshallingSequentialList<E>
    extends AbstractSequentialList<E>
    implements Reconstructable, Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 3257852069179110709L;


    /**
     * Deserializer
     */
    protected MarshallingSequentialList(
    ){    
        super();
    }

    /**
     * Constructor
     * 
     * @param marshaller
     * @param list
     */    
    @SuppressWarnings("unchecked")
    public MarshallingSequentialList(
        Marshaller marshaller,
        List<?> list 
    ) {
        this.marshaller = new ExceptionListenerMarshaller(marshaller);
        this.list = (List<Object>) list;
    }

    /**
     * Make the marshaller dynamically selectable
     * 
     * @return the marshalle prvided upon construction
     */
    protected Marshaller getMarshaller(){
        return this.marshaller;
    }
    
    /* (non-Javadoc)
     * @see java.util.List#add(int, java.lang.Object)
     */
    public void add(
        int index, 
        E element
    ) {
        try {
            getDelegate().add(index, getMarshaller().unmarshal(element));
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }
    }

    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(
        E element
    ) {
        try {
            return getDelegate().add(getMarshaller().unmarshal(element));
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    /* (non-Javadoc)
     * @see java.util.Collection#clear()
     */
    public void clear() {
        getDelegate().clear();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(
        Object element
    ) {
        try {
            return getDelegate().contains(getMarshaller().unmarshal(element));
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    /* (non-Javadoc)
     * @see java.util.List#get(int)
     */
    @SuppressWarnings("unchecked")
    public E get(
        int index
    ) {
        try {
            return (E) getMarshaller().marshal(getDelegate().get(index));
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    /**
     * 
     * @return
     */
    protected List<Object> getDelegate(
    ){
        return this.list;  
    }

    /* (non-Javadoc)
     * @see java.util.List#indexOf(java.lang.Object)
     */
    public int indexOf(
        Object arg0
    ) {
        try {
            return getDelegate().indexOf(getMarshaller().unmarshal(arg0));
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty(
    ) {
        return getDelegate().isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#iterator()
     */
    public Iterator<E> iterator() {
        return listIterator();
    }

    /* (non-Javadoc)
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(
        Object arg0
    ) {
        try {
            return getDelegate().lastIndexOf(getMarshaller().unmarshal(arg0));
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    public ListIterator<E> listIterator(
    ) {
        return new MarshallingIterator<E>(
            getMarshaller(),
            getDelegate().listIterator()
        );
    }

    public ListIterator<E> listIterator(
        int index
    ) {
        return new MarshallingIterator<E>(
            getMarshaller(),
            getDelegate().listIterator(index)
        );
    }

    /* (non-Javadoc)
     * @see java.util.List#remove(int)
     */
    @SuppressWarnings("unchecked")
    public E remove(
        int index
    ) {
        try {
            return (E) getMarshaller().marshal(
                getDelegate().remove(index)
            );
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    /* (non-Javadoc)
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(
        Object element
    ) {
        try {
            return getDelegate().remove(getMarshaller().unmarshal(element));
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    /* (non-Javadoc)
     * @see java.util.List#set(int, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public E set(
        int index, 
        E arg1
    ) {
        try {
            return (E) getMarshaller().marshal(
                getDelegate().set(index, getMarshaller().unmarshal(arg1))
            );
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }


    //------------------------------------------------------------------------
    // Extends AbstractSequentialList 
    //------------------------------------------------------------------------

    public int size(
    ) {
        return getDelegate().size();
    }


    //------------------------------------------------------------------------
    // Implements Reconstructable
    //------------------------------------------------------------------------

    /**
     * Write part of a reconstructable object's state to an OutputStream
     * (optional operation).
     *
     * @param   stream
     *          OutputStream that holds part of a reconstructable object's 
     *          state.
     *
     * @exception   ServiceException
     *              if partial state streaming fails
     * @exception   ServiceException NOT_SUPPORTED
     *              if the instance is not reconstructable
     */
    public void write(
        OutputStream stream
    ) throws ServiceException {
        List<Object> list = getDelegate();
        if(list instanceof Reconstructable){
            ((Reconstructable)list).write(stream);
        } else {    
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.NOT_SUPPORTED,
                "List to delegate to is not reconstructable",
                new BasicException.Parameter("class",list.getClass().getName())
            );
        }
    }

    /**
     * 
     */
    protected transient List<Object> list;


    //------------------------------------------------------------------------
    // Instance members
    //------------------------------------------------------------------------

    /**
     * @serial
     */
    protected Marshaller marshaller;


    //------------------------------------------------------------------------
    // Class MarshallingIterator
    //------------------------------------------------------------------------

    protected static class MarshallingIterator<E>
        implements ListIterator<E> 
    {

        @SuppressWarnings("unchecked")
        MarshallingIterator(
            Marshaller marshaller,
            ListIterator<?> iterator
        ) {
            this.iterator = (ListIterator<Object>) iterator;
            this.marshaller = marshaller;
        }

        private final ListIterator<Object> iterator;

        private final Marshaller marshaller;
        
        public void add(
            Object o
        ) {
            try {
                this.iterator.add(this.marshaller.unmarshal(o));
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }            
        }

        public boolean hasNext(
        ) {
            return this.iterator.hasNext();
        }

        public boolean hasPrevious(
        ) {
            return this.iterator.hasPrevious();
        }

        @SuppressWarnings("unchecked")
        public E next(
        ) {
            try {
                return (E) this.marshaller.marshal(this.iterator.next());
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }            
        }

        public int nextIndex(
        ) {
            return this.iterator.nextIndex();
        }

        @SuppressWarnings("unchecked")
        public E previous(
        ) {
            try {
                return (E) this.marshaller.marshal(this.iterator.previous());
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }
        }

        public int previousIndex(
        ) {
            return this.iterator.previousIndex();
        }

        public void remove(
        ) {
            this.iterator.remove();
        }

        public void set(
            Object o
        ) {
            try {
                this.iterator.set(this.marshaller.unmarshal(o));
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }            
        }

    }

}
