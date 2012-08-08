/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: MarshallingSequentialList.java,v 1.18 2008/03/06 19:03:26 hburger Exp $
 * Description: SPICE Collections: Merging List
 * Revision:    $Revision: 1.18 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/06 19:03:26 $
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

import java.io.OutputStream;
import java.io.Serializable;
import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.exception.StackedException;
import org.openmdx.compatibility.base.marshalling.CollectionMarshallerAdapter;
import org.openmdx.compatibility.base.marshalling.Marshaller;
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
        org.openmdx.base.persistence.spi.Marshaller marshaller,
        List<?> list 
    ) {
        this.marshaller = marshaller;
        this.list = (List<Object>) list;
    }
  
    /**
     * Constructor
     * 
     * @param marshaller
     * @param list
     */    
    public MarshallingSequentialList(
        Marshaller marshaller,
        List<?> list 
    ) {
        this(new CollectionMarshallerAdapter(marshaller), list);
    }
  
    /* (non-Javadoc)
     * @see java.util.List#add(int, java.lang.Object)
     */
    public void add(int index, E element) {
        getDelegate().add(index, this.marshaller.unmarshal(element));
    }

    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(E element) {
        return getDelegate().add(this.marshaller.unmarshal(element));
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
    public boolean contains(Object element) {
        return getDelegate().contains(this.marshaller.unmarshal(element));
    }

    /* (non-Javadoc)
     * @see java.util.List#get(int)
     */
    @SuppressWarnings("unchecked")
    public E get(int index) {
        return (E) this.marshaller.marshal(getDelegate().get(index));
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
    public int indexOf(Object arg0) {
        return getDelegate().indexOf(this.marshaller.unmarshal(arg0));
    }

    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty() {
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
    public int lastIndexOf(Object arg0) {
        return getDelegate().lastIndexOf(this.marshaller.unmarshal(arg0));
    }

    public ListIterator<E> listIterator(
    ) {
        return new MarshallingIterator(
            getDelegate().listIterator()
        );
    }
           
    public ListIterator<E> listIterator(
        int index
    ) {
        return new MarshallingIterator(
            getDelegate().listIterator(index)
        );
    }

    /* (non-Javadoc)
     * @see java.util.List#remove(int)
     */
    @SuppressWarnings("unchecked")
    public E remove(int index) {
        return (E) this.marshaller.marshal(
            getDelegate().remove(index)
        );
    }

    /* (non-Javadoc)
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object element) {
        return getDelegate().remove(this.marshaller.unmarshal(element));
    }

    /* (non-Javadoc)
     * @see java.util.List#set(int, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public E set(int index, E arg1) {
        return (E) this.marshaller.marshal(
        	getDelegate().set(index, this.marshaller.unmarshal(arg1))
        );
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
                StackedException.DEFAULT_DOMAIN, 
                StackedException.NOT_SUPPORTED,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("class",list.getClass().getName())
                },
                "List to delegate to is not reconstructable"
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
    protected org.openmdx.base.persistence.spi.Marshaller marshaller;


    //------------------------------------------------------------------------
    // Class MarshallingIterator
    //------------------------------------------------------------------------

    protected class MarshallingIterator
        implements ListIterator<E> 
    {

        @SuppressWarnings("unchecked")
        MarshallingIterator(
            ListIterator<?> iterator
        ) {
            this.iterator = (ListIterator<Object>) iterator;
        }
        
        public void add(
            Object o
        ) {
            this.iterator.add(marshaller.unmarshal(o));
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
            return (E) marshaller.marshal(this.iterator.next());
        }

        public int nextIndex(
        ) {
            return this.iterator.nextIndex();
        }
        
        @SuppressWarnings("unchecked")
        public E previous(
        ) {
            return (E) marshaller.marshal(this.iterator.previous());
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
            this.iterator.set(marshaller.unmarshal(o));
        }
    
        private final ListIterator<Object> iterator;

    }

}
