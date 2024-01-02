/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Type-Safe Marshalling Collection
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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import org.openmdx.base.marshalling.TypeSafeMarshaller;

/**
 * Type-Safe Marshalling Collection
 */
public class TypeSafeMarshallingCollection<U,M>
  extends AbstractCollection<M> 
{
    
    /**
     * Constructor
     * 
     * @param marshaller
     * @param collection
     */
    public TypeSafeMarshallingCollection(
        TypeSafeMarshaller<U,M> marshaller,
        Collection<U> collection
    ) {
        this.marshaller = marshaller;
        this.delegate = collection;
    }

    /**
     * The delegate collection
     */
    private final Collection<U> delegate;

    /**
     * The marshaller to be used
     */    
    private final TypeSafeMarshaller<U,M> marshaller;

    /* (non-Javadoc)
     * @see java.util.Collection#clear()
     */
    @Override
    public void clear() {
        this.delegate.clear();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    @Override
    public boolean add(
        M element
    ) {
        return this.delegate.add(this.marshaller.unmarshal(element));
    }

    /* (non-Javadoc)
     * @see java.util.Collection#contains(java.lang.Object)
     */
    @Override
    public boolean contains(
        Object element
    ) {
        final Optional<U> unmarshalled = marshaller.asUnmarshalledValue(element);
        return unmarshalled.isPresent() && this.delegate.contains(unmarshalled.get());
    }

    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }
    
    @Override
    public Iterator<M> iterator(
    ) {
        return new TypeSafeMarshallingIterator<U,M>(
            this.marshaller,
            this.delegate.iterator()
        );
    }

    /* (non-Javadoc)
     * @see java.util.Collection#remove(java.lang.Object)
     */
    @Override
    public boolean remove(
        Object element
    ) {
        final Optional<U> unmarshalled = marshaller.asUnmarshalledValue(element);
        return unmarshalled.isPresent() && this.delegate.remove(unmarshalled.get());
    }

    @Override
    public int size(
    ) {
        return this.delegate.size();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TypeSafeMarshallingCollection [delegate=" + this.delegate + "]";
    }

    
    //------------------------------------------------------------------------
    // Class TypeSafeMarshallingIterator
    //------------------------------------------------------------------------
    

    /**
     * Type-Safe Marshalling Iterator
     */
    static class TypeSafeMarshallingIterator<U,M>
        implements Iterator<M> 
    {

        TypeSafeMarshallingIterator(
            TypeSafeMarshaller<U,M> marshaller,
            Iterator<U> delegate
        ) {
            this.marshaller = marshaller;
            this.delegate = delegate;
        }

        /**
         * The delegate iteratir
         */
        private final Iterator<U> delegate;

        /**
         * The marshaller to be used
         */    
        private final TypeSafeMarshaller<U,M> marshaller;
        
        public boolean hasNext(
        ) {
            return this.delegate.hasNext();
        }
        
        public M next(
        ) {
            return marshaller.marshal(
                delegate.next()
            );
        }

        public void remove(
        ) {
            this.delegate.remove();
        }

    }

}
