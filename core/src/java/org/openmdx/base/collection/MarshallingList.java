/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: MarshallingList.java,v 1.10 2008/02/18 14:11:33 hburger Exp $
 * Description: SPICE Collections: Merging List
 * Revision:    $Revision: 1.10 $
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
import java.util.AbstractList;
import java.util.List;

import org.openmdx.base.exception.BadParameterException;
import org.openmdx.compatibility.base.marshalling.CollectionMarshallerAdapter;
import org.openmdx.compatibility.base.marshalling.Marshaller;

/**
 * A Marshalling List
 */
public class MarshallingList<E>
    extends AbstractList<E> 
    implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 3761694498107044406L;

    /**
     * Constructor
     * 
     * @param marshaller
     * @param list
     */    
    @SuppressWarnings("unchecked")
    public MarshallingList(
        org.openmdx.base.object.spi.Marshaller marshaller,
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
    public MarshallingList(
        Marshaller marshaller,
        List<?> list 
    ) {
        this(new CollectionMarshallerAdapter(marshaller), list);
    }

    /**
     * 
     */    
    public void add(
        int index, 
        E element
    ) {
        this.list.add(
            index,
            unmarshal(element)
        ); 
    }
  
    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(E element) {
        return this.list.add(
            unmarshal(element)
        );
    }

    /* (non-Javadoc)
     * @see java.util.Collection#clear()
     */
    public void clear() {
        this.list.clear();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object candidate) {
        return this.list.contains(
            this.marshaller.unmarshal(candidate)
        );
    }

    /**
     * 
     */  
    @SuppressWarnings("unchecked")
    public E get(
        int index
    ) {
        return (E) this.marshaller.marshal(this.list.get(index));
    }

    /* (non-Javadoc)
     * @see java.util.List#indexOf(java.lang.Object)
     */
    public int indexOf(Object candidate) {
        return this.list.indexOf(
            this.marshaller.unmarshal(candidate)
        );
    }

    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object candidate) {
        return this.list.lastIndexOf(
            this.marshaller.unmarshal(candidate)
        );
    }

    /**
     * 
     */  
    @SuppressWarnings("unchecked")
    public E remove(
        int index
    ) {
        return (E) this.marshaller.marshal(
            this.list.remove(index)
        );
    } 

    /* (non-Javadoc)
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object candidate) {
        return this.list.remove(
            this.marshaller.unmarshal(candidate)
        );
    }

    /**
     * 
     */    
    @SuppressWarnings("unchecked")
    public E set(
        int index, 
        E element
    ) {
        return (E) this.marshaller.marshal(
            this.list.set(
                index, 
                unmarshal(element)
            )
        );
    }

    /**
     * 
     */
    public int size(
    ) {
        return this.list.size();
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
    protected List<Object> list;

    /**
     * 
     */
    protected org.openmdx.base.object.spi.Marshaller marshaller;

}
