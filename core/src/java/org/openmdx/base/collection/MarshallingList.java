/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: MarshallingList.java,v 1.14 2008/10/02 17:32:13 hburger Exp $
 * Description: MarshallingList
 * Revision:    $Revision: 1.14 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/10/02 17:32:13 $
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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.openmdx.base.persistence.spi.Marshaller;
import org.openmdx.compatibility.base.marshalling.CollectionMarshallerAdapter;
import org.openmdx.compatibility.base.marshalling.ReluctantUnmarshalling;

/**
 * A Marshalling List
 */
@SuppressWarnings("unchecked")
public class MarshallingList<E> extends AbstractList<E> implements Serializable {

    /**
     * Constructor
     * 
     * @param marshaller
     * @param list
     * @param unmarshalling
     */    
    public MarshallingList(
        Marshaller marshaller,
        List list, 
        Unmarshalling unmarshalling 
    ) {
        this.marshaller = marshaller;
        this.delegate = list;
        this.unmarshalling = unmarshalling;
    }

    /**
     * Constructor
     * 
     * @param marshaller
     * @param list
     */    
    public MarshallingList(
        Marshaller marshaller,
        List list 
    ) {
        this(marshaller, list, Unmarshalling.EAGER);
    }

    /**
     * Constructor
     * 
     * @param marshaller
     * @param list
     */    
    public MarshallingList(
        org.openmdx.compatibility.base.marshalling.Marshaller marshaller,
        List list 
    ) {
        this(
            new CollectionMarshallerAdapter(marshaller), 
            list, 
            marshaller instanceof ReluctantUnmarshalling ? Unmarshalling.RELUCTANT : Unmarshalling.EAGER
        );
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3761694498107044406L;
    
    /**
     * 
     */
    private List delegate;
    
    /**
     * 
     */
    protected final Marshaller marshaller;
    
    /**
     * 
     */
    protected final Unmarshalling unmarshalling;
    
    /**
     * Retrieve the delegate
     * <p>
     * This method may be overridden by a sub-class for dynamic delegation.
     * 
     * @return the delegate
     */
    protected List getDelegate(){
        return this.delegate;
    }

    /**
     * Replace the delegate for static delegation
     * 
     * @param the new delegate
     */
    protected void setDelegate(
        List delegate
    ){
        this.delegate = delegate;
    }
    
    
    //------------------------------------------------------------------------
    // Implements List
    //------------------------------------------------------------------------

    public void add(
        int index, 
        E element
    ) {
        getDelegate().add(
            index,
            marshaller.unmarshal(element)
        ); 
    }
  
    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(E element) {
        return getDelegate().add(
            marshaller.unmarshal(element)
        );
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
    public boolean contains(Object candidate) {
        return getDelegate().contains(
            marshaller.unmarshal(candidate)
        );
    }

    public E get(
        int index
    ) {
        return (E) marshaller.marshal(
            getDelegate().get(index)
        );
    }

    /* (non-Javadoc)
     * @see java.util.List#indexOf(java.lang.Object)
     */
    public int indexOf(Object candidate) {
        switch(this.unmarshalling) {
            case RELUCTANT:
                return super.indexOf(candidate);
            case EAGER:
            default:    
                return getDelegate().indexOf(
                    marshaller.unmarshal(candidate)
                );
        }
    }

    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty() {
        return getDelegate().isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object candidate) {
        switch(this.unmarshalling) {
            case RELUCTANT:
                return super.lastIndexOf(candidate);
            case EAGER:
            default:    
                return getDelegate().lastIndexOf(
                    marshaller.unmarshal(candidate)
                );
        }
    }

    public E remove(
        int index
    ) {
        return (E) marshaller.marshal(
            getDelegate().remove(index)
        );
    } 

    /* (non-Javadoc)
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(
        Object candidate
    ) {
        switch(this.unmarshalling) {
            case RELUCTANT:
                return super.remove(candidate);
            case EAGER:
            default:    
                return getDelegate().remove(
                    marshaller.unmarshal(candidate)
                );
        }
    }

    public E set(
        int index, 
        E element
    ) {
        return (E) marshaller.marshal(
            getDelegate().set(
                index, 
                marshaller.unmarshal(element)
            )
        );
    }

    public int size(
    ) {
        return getDelegate().size();
    }

    public Iterator<E> iterator(
    ) {
        return new MarshallingListIterator(getDelegate().listIterator());
    }

    public ListIterator<E> listIterator(
    ) {
        return new MarshallingListIterator(getDelegate().listIterator());
    }

    public ListIterator<E> listIterator(
        int index
    ) {
        return new MarshallingListIterator(getDelegate().listIterator(index));
    }
 
    
    //------------------------------------------------------------------------
    // Class MarshallingListIterator
    //-----------------------------------------------------------------------
    
    /**
     * 
     */
    class MarshallingListIterator implements ListIterator<E> {
        
        /**
         * Constructor 
         *
         * @param delegate
         */
        MarshallingListIterator(
            ListIterator<?> delegate
        ) {
            this.delegate = delegate;
        }

        /**
         * 
         */
        private final ListIterator delegate;

        public void add(
            E element
        ) {
            this.delegate.add(
                marshaller.unmarshal(element)
            );
        }

        public boolean hasNext(
        ) {
            return this.delegate.hasNext();
        }

        public boolean hasPrevious(
        ) {
            return this.delegate.hasPrevious();
        }

        public E next(
        ) {
            return (E)marshaller.marshal(
                this.delegate.next()
            );
        }

        public int nextIndex(
        ) {
            return this.delegate.nextIndex();
        }

        public E previous(
        ) {
            return (E)marshaller.marshal(
                this.delegate.previous()
            );
        }

        public int previousIndex(
        ) {
            return this.delegate.previousIndex();
        }

        public void remove(
        ) {
            this.delegate.remove();            
        }

        public void set(
            E element
        ) {
            this.delegate.set(
                marshaller.unmarshal(element)
            );
        }
       
    }
        
}
