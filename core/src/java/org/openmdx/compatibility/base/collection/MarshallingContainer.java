/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: MarshallingContainer.java,v 1.16 2008/02/18 14:11:33 hburger Exp $
 * Description: Marshalling Container
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/18 14:11:33 $
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
import java.util.Iterator;
import java.util.List;
import org.openmdx.base.collection.FetchSize;
import org.openmdx.base.collection.MarshallingSequentialList;
import org.openmdx.base.exception.BadParameterException;
import org.openmdx.compatibility.base.marshalling.CollectionMarshallerAdapter;
import org.openmdx.compatibility.base.marshalling.Marshaller;

/**
 * A Marshalling Container
 */
public class MarshallingContainer<E>
    extends AbstractCollection<E>
    implements Container<E>, Serializable, FetchSize
{

    /**
     * 
     */
    private static final long serialVersionUID = 3616445717720610871L;


    /**
     * Constructor
     *
     * @param   the marshaller to be applied to the elements, filter and order
     *          objects.
     * @param   The delegate contains unmarshalled elements
     */
    @SuppressWarnings("unchecked")
    public MarshallingContainer(
        org.openmdx.base.object.spi.Marshaller marshaller,
        Container<?> container
    ) {
        this.marshaller = marshaller;
        this.container = (Container<Object>) container;
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
    public boolean add(
        E element
    ){
        return this.container.add(
            unmarshal(element)
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


    /**
     * Select an object matching the filter.
     * <p>
     * The acceptable filter object classes must be specified by the 
     * Container implementation.
     *
     * @param     filter
     *            The filter to be applied to objects of this container
     *
     * @return    the object matching the filter;
     *            or null if no object matches the filter.
     * 
     * @exception ClassCastException
     *            if the class of the specified filter prevents it from
     *            being applied to this container.
     * @exception IllegalArgumentException
     *            if some aspect of this filter prevents it from being
     *            applied to this container. 
     * @exception InvalidCardinalityException
     *            if more than one object matches the filter. 
     */

    @SuppressWarnings("unchecked")
    public E get(
        Object filter
    ){
        return (E) this.marshaller.marshal(
            this.container.get(unmarshal(filter))
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


    //------------------------------------------------------------------------
    // Implements Collection
    //------------------------------------------------------------------------

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
    // Implements Container
    //------------------------------------------------------------------------

    /**
     * Selects objects matching the filter.
     * <p>
     * The semantics of the container returned by this method become
     * undefined if the backing container (i.e., this list) is structurally
     * modified in any way other than via the returned container. (Structural
     * modifications are those that change the size of this list, or otherwise
     * perturb it in such a fashion that iterations in progress may yield
     * incorrect results.) 
     * <p>
     * This method returns a Collection as opposed to a Set because it 
     * behaves as set in respect to object id equality, not element equality.
     * <p>
     * The acceptable filter object classes must be specified by the 
     * Container implementation.
     *
     * @param     filter
     *            The filter to be applied to objects of this container
     *
     * @return    A subset of this container containing the objects
     *            matching the filter.
     * 
     * @exception ClassCastException
     *            if the class of the specified filter prevents it from
     *            being applied to this container.
     * @exception IllegalArgumentException
     *            if some aspect of this filter prevents it from being
     *            applied to this container. 
     */
    public Container<E> subSet(
        Object filter
    ){
        return new MarshallingContainer<E>(
            this.marshaller,
            this.container.subSet(unmarshal(filter))
        );
    }

    /**
     * Returns a list based on the underlaying container.
     * <p>
     * The acceptable order object classes must be specified by the container 
     * implementation.
     *
     * @param     order
     *            The order to be applied to objects of this container;
     *            or null for the container's default order.
     *
     * @return    A list containing the objects of this container
     *            sorted according to the given order.
     * 
     * @exception ClassCastException
     *            if the class of the specified order prevents it from
     *            being applied to this container.
     * @exception IllegalArgumentException
     *            if some aspect of this order prevents it from being
     *            applied to this container. 
     */
    public List<E> toList(
        Object order
    ){
        return new MarshallingSequentialList<E>(
            this.marshaller,
            this.container.toList(unmarshal(order))
        );
    }

    /**
     * Unmarshals an argument
     *
     * @param     argument
     *            The argument to be unmarshalled
     *
     * @return    the unmarshalled argument
     * 
     * @exception BadParameterException
     *            if some aspect of argument prevents it from being
     *            unmarshalled 
     */
    private Object unmarshal(
        Object argument
    ){
        return this.marshaller.unmarshal(argument);
    }


    /**
     *
     */

    protected Container<Object> container;

    /**
     *
     */
    protected org.openmdx.base.object.spi.Marshaller marshaller;

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
        private final Iterator<Object> iterator = MarshallingContainer.this.container.iterator();

    }

}
