/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: MarshallingList
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.ExceptionListenerMarshaller;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.marshalling.ReluctantUnmarshalling;

/**
 * A Marshalling List
 */
@SuppressWarnings({"unchecked","rawtypes"})
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
        this(
            new ExceptionListenerMarshaller(marshaller), 
            list, 
            marshaller instanceof ReluctantUnmarshalling ? Unmarshalling.RELUCTANT : Unmarshalling.EAGER
        );
    }

    /**
     * Implements {@code Serializable}
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

    @Override
    public void add(
        int index, 
        E element
    ) {
        try {
            getDelegate().add(
                index,
                marshaller.unmarshal(element)
            );
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }            
    }
  
    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    @Override
    public boolean add(
        E element
    ) {
        try {
            return getDelegate().add(
                marshaller.unmarshal(element)
            );
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    /* (non-Javadoc)
     * @see java.util.Collection#clear()
     */
    @Override
    public void clear() {
        getDelegate().clear();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#contains(java.lang.Object)
     */
    @Override
    public boolean contains(
        Object candidate
    ) {
        try {
            return getDelegate().contains(
                marshaller.unmarshal(candidate)
            );
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    @Override
    public E get(
        int index
    ) {
        try {
            return (E) marshaller.marshal(
                getDelegate().get(index)
            );
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    /* (non-Javadoc)
     * @see java.util.List#indexOf(java.lang.Object)
     */
    @Override
    public int indexOf(
        Object candidate
    ) {
        try {
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
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return getDelegate().isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    @Override
    public int lastIndexOf(
        Object candidate
    ) {
        try {
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
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    @Override
    public E remove(
        int index
    ) {
        try {
            return (E) marshaller.marshal(
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
    @Override
    public boolean remove(
        Object candidate
    ) {
        try {
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
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    @Override
    public E set(
        int index, 
        E element
    ) {
        try {
            return (E) marshaller.marshal(
                getDelegate().set(
                    index, 
                    marshaller.unmarshal(element)
                )
            );
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    @Override
    public int size(
    ) {
        return getDelegate().size();
    }

    @Override
    public Iterator<E> iterator(
    ) {
        return new MarshallingListIterator(getDelegate().listIterator());
    }

    @Override
    public ListIterator<E> listIterator(
    ) {
        return new MarshallingListIterator(getDelegate().listIterator());
    }

    @Override
    public ListIterator<E> listIterator(
        int index
    ) {
        return new MarshallingListIterator(getDelegate().listIterator(index));
    }
 
    /* (non-Javadoc)
     * @see java.util.AbstractCollection#toString()
     */
    @Override
    public String toString() {
        return getDelegate().toString();
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
            try {
                this.delegate.add(
                    marshaller.unmarshal(element)
                );
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }            
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
            
            try {
                return (E)marshaller.marshal(
                    this.delegate.next()
                );
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }            
        }

        public int nextIndex(
        ) {
            return this.delegate.nextIndex();
        }

        public E previous(
        ) {
            try {
                return (E)marshaller.marshal(
                    this.delegate.previous()
                );
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }            
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
            try {
                this.delegate.set(
                    marshaller.unmarshal(element)
                );
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }            
        }
       
    }
        
}
