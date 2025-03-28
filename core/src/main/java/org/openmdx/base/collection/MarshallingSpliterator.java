/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: MarshallingSpliterator 
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

import java.util.Spliterator;
import java.util.function.Consumer;

import org.openmdx.base.marshalling.Marshaller;

/**
 * MarshallingSpliterator
 *
 */
public class MarshallingSpliterator<S,T> implements Spliterator<T> {
    
    /**
     * Constructor 
     */
    public MarshallingSpliterator(
        Class<T> targetClass,
        Spliterator<S> delegate,
        Marshaller marshaller
    ) {
        this.delegate = delegate;
        this.marshaller = marshaller;
        this.targetClass = targetClass;
    }
    
    /**
     * The marshaller
     */
    private final Marshaller marshaller;
    
    /**
     * The delegate spliterator
     */
    private final Spliterator<S> delegate;

    /**
     * The target class
     */
    private final Class<T> targetClass;
    
    /* (non-Javadoc)
     * @see java.util.Spliterator#tryAdvance(java.util.function.Consumer)
     */
    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        return this.delegate.tryAdvance(newConsumer(action));
    }

    /* (non-Javadoc)
     * @see java.util.Spliterator#forEachRemaining(java.util.function.Consumer)
     */
    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        this.delegate.forEachRemaining(newConsumer(action));
    }

    /* (non-Javadoc)
     * @see java.util.Spliterator#trySplit()
     */
    @Override
    public Spliterator<T> trySplit() {
        return null; // Splitting is not yet supported
    }

    /* (non-Javadoc)
     * @see java.util.Spliterator#estimateSize()
     */
    @Override
    public long estimateSize() {
        return this.delegate.estimateSize();
    }

    /* (non-Javadoc)
     * @see java.util.Spliterator#characteristics()
     */
    @Override
    public int characteristics() {
        return this.delegate.characteristics();
    }

    private Consumer<? super S> newConsumer(
        final Consumer<? super T> delegate
    ){
        return new MarshallingConsumer<S,T>(this.targetClass, delegate, this.marshaller);
    }
    
}
