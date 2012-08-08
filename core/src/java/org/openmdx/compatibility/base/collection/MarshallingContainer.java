/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: MarshallingContainer.java,v 1.18 2008/04/21 16:58:25 hburger Exp $
 * Description: Marshalling Container
 * Revision:    $Revision: 1.18 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/21 16:58:25 $
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

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.collection.FetchSize;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.compatibility.base.marshalling.CollectionMarshallerAdapter;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.w3c.cci2.Container;

/**
 * A Marshalling Container
 */
public class MarshallingContainer<E>
    extends AbstractCollection<E>
    implements RefContainer, Serializable, FetchSize
{

    /**
     * Constructor
     *
     * @param   the marshaller to be applied to the elements, filter and order
     *          objects.
     * @param   The delegate contains unmarshalled elements
     */
    @SuppressWarnings("unchecked")
    public MarshallingContainer(
        org.openmdx.base.persistence.spi.Marshaller marshaller,
        Collection<?> container
    ) {
        this.marshaller = marshaller;
        this.container = container;
    }

    /**
     * Constructor
     *
     * @param   the marshaller to be applied to the elements, filter and order
     *          objects.
     * @param   The delegate contains unmarshalled elements
     */
    public MarshallingContainer(
        Marshaller marshaller,
        Container<?> container
    ) {
        this(new CollectionMarshallerAdapter(marshaller), container);
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3616445717720610871L;

    
    //------------------------------------------------------------------------
    // Implements Collection
    //------------------------------------------------------------------------

    /**
     * Add an object to the collection.
     *
     * @param     element
     *            the object to be added
     *
     * @exception ClassCastException
     *            if the class of the specified criteria or element prevents it
     *            from being added to this container.
     * @exception IllegalArgumentException
     *            if some aspect of the specified criteria or element prevents 
     *            it from being added to this container.
     */
    @SuppressWarnings("unchecked")
    public boolean add(
        E element
    ){
        return this.container.add(
            this.marshaller.unmarshal(element)
        );
    }

    /* (non-Javadoc)
     * @see java.util.Collection#clear()
     */
    public void clear() {
        this.container.clear();
    }

    /**
     * Returns true if this collection contains the specified element. 
     *
     * @param   element
     *          element whose presence in this collection is to be tested.
     *
     * @return  true if this collection contains the specified element
     * 
     * @exception   ClassCastException
     *              if the type of the specified element is incompatible with 
     *              this collection (optional). 
     * @exception   NullPointerException
     *              if the specified element is null and this collection does 
     *              not support null elements (optional).
     */
    public boolean contains(
        Object element
    ){
        return this.container.contains(
            this.marshaller.unmarshal(element)
        );
    }

    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty() {
        return this.container.isEmpty();
    }

    /**
     * Returns an iterator over the elements contained in this container.
     * 
     * @return    an iterator over the elements contained in this container.
     */
    public Iterator<E> iterator(
    ){
        return new MarshallingIterator();
    }

    /**
     * Container's can't support remove operations. That's why an
     * UnsupportedOperationException has to be thrown.
     *
     * @param   element
     *          element to be removed from this collection, if present.
     *
     * @return  true if this collection changed as a result of the call.
     *
     * @exception   ClassCastException
     *              if the type of the specified element is incompatible 
     *              with this collection (optional). 
     *              NullPointerException
     *              if the specified element is null and this collection
     *              does not support null elements (optional). 
     * @exception   UnsupportedOperationException
     *              remove is not supported by this collection.
     */
    public boolean remove(
        Object element
    ){
        return this.container.remove(
            this.marshaller.unmarshal(element)
        );
    }


    /**
     * Returns the number of elements in this container. If the container 
     * contains more than Integer.MAX_VALUE elements or the number of elements
     * is unknown, returns Integer.MAX_VALUE.
     *
     * @eturn   the number of elements in this container
     */
    public int size(
    ) {
        return this.container.size();
    }


    //------------------------------------------------------------------------
    // Implements FetchSize
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     */
    public int getFetchSize(
    ) {
        if(
            this.container instanceof FetchSize
        ) this.fetchSize = ((FetchSize)this.container).getFetchSize();
        return this.fetchSize;
    }

    /* (non-Javadoc)
     */
    public void setFetchSize(
        int fetchSize
    ){
        this.fetchSize = fetchSize;
        if(
            this.container instanceof FetchSize
        ) ((FetchSize)this.container).setFetchSize(fetchSize);
    }

    /**
     * The proposed fetch size
     */
    private int fetchSize = DEFAULT_FETCH_SIZE;


    //------------------------------------------------------------------------
    // Implements RefContainer
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refAdd(java.lang.Object[])
     */
    public void refAdd(Object... arguments) {
        Object[] marshalled = new Object[arguments.length];
        int o = arguments.length - 1;
        System.arraycopy(arguments, 0, marshalled, 0, o);
        marshalled[o] = this.marshaller.unmarshal(arguments[o]);
    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refGet(java.lang.Object[])
     */
    public Object refGet(Object... arguments) {
        return this.marshaller.marshal(
            ((RefContainer)this.container).refGet(arguments)
        );
    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refGetAll(java.lang.Object)
     */
    public List<?> refGetAll(Object query) {
        return new MarshallingList<E>(
            this.marshaller,
            ((RefContainer)this.container).refGetAll(
                this.marshaller.unmarshal(query)
            )
        );
    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refRemove(java.lang.Object[])
     */
    public void refRemove(Object... arguments) {
        ((RefContainer)this.container).refRemove(arguments);
    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refRemoveAll(java.lang.Object)
     */
    public void refRemoveAll(Object query) {
        ((RefContainer)this.container).refRemoveAll(
            this.marshaller.unmarshal(query)
        );
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    protected Collection container;

    /**
     *
     */
    protected org.openmdx.base.persistence.spi.Marshaller marshaller;

    
    //------------------------------------------------------------------------
    // Class MarshallingIterator
    //------------------------------------------------------------------------
    
    /**
     * Marshalling Iterator
     */
    protected class MarshallingIterator implements Iterator<E> {

        /**
         *
         */
        public boolean hasNext(
        ){
            return this.iterator.hasNext();
        }

        /**
         * Returns the next element in the iteration. 
         * 
         * @returns     the next element in the iteration. 
         * 
         * @exception   NoSuchElementException
         *              iteration has no more elements.
         */
        @SuppressWarnings("unchecked")
        public E next(
        ){
            return (E) MarshallingContainer.this.marshaller.marshal(
                this.iterator.next()
            );
        }

        /**
         *
         */
        public void remove(
        ) {
            this.iterator.remove();
        }

        /**
         *
         */
        private final Iterator<?> iterator = MarshallingContainer.this.container.iterator();

    }

}
