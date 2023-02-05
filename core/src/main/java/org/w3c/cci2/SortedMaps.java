/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Sorted Maps 
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
package org.w3c.cci2;

import java.io.Serializable;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Sorted Maps
 */
public class SortedMaps {

    /**
     * Constructor
     */
    private SortedMaps() {
        // Avoid instantiation
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final SparseArray EMPTY_SPARSE_ARRAY = unmodifiableSparseArray(new TreeMap());

    /**
     * Returns a {@code SparseArray} backed by the specified sorted map.
     * Changes to the returned sparse array "write through" to the sorted map.
     * Setting a value to {@code null} is propagated as
     * {@code remove} operation. The returned sparse array is
     * {@code Serializable}.
     * 
     * @param s
     *            the sorted map by which the sparse array will be backed.
     * 
     * @return a sparse array view of the specified sorted map.
     */
    public static <E> SparseArray<E> asSparseArray(
        SortedMap<Integer, E> s
    ) {
        return new AsSparseArray<E>(s);
    }

    /**
     * Returns an empty sparse array
     * 
     * @return an an empty sparse array
     */
    @SuppressWarnings("unchecked")
    public static <E> SparseArray<E> emptySparseArray() {
        return EMPTY_SPARSE_ARRAY;
    }

    /**
     * Returns a SparseArray containing a single element
     * 
     * @param element
     *            the single element
     * 
     * @return an unmodifiable SparseArray containing the single element
     */
    public static <E> SparseArray<E> singletonSparseArray(E element) {
        return unmodifiableSparseArray(
            new TreeMap<Integer, E>(
                Collections.singletonMap(Integer.valueOf(0), element)
            )
        );
    }

    /**
     * Returns a {@code SparseArray} backed by the specified sorted map.
     * The returned sparse array is {@code Serializable}.
     * 
     * @param s
     *            the sorted map by which the sparse array will be backed.
     * 
     * @return an unmodifiable sparse array view of the specified sorted map.
     */
    public static <E> SparseArray<E> unmodifiableSparseArray(
        SortedMap<Integer, E> s
    ) {
        return asSparseArray(
            Collections.unmodifiableSortedMap(s)
        );
    }

    
    //------------------------------------------------------------------------
    // Class AsSparseArray
    //------------------------------------------------------------------------

    /**
     * {@code TreeMap} based SparseArray implementation.
     */
    static class AsSparseArray<E>
        extends AbstractSparseArray<E>
        implements Serializable {

        /**
         * Constructor
         *
         * @param delegate
         */
        AsSparseArray(
            SortedMap<Integer, E> delegate
        ) {
            this.delegate = delegate;
        }

        /**
         * Implements {@code Serializable}
         */
        private static final long serialVersionUID = -162457543630599238L;

        /**
         * The sorted map by which the sparse array will is backed
         */
        private final SortedMap<Integer, E> delegate;

        /*
         * (non-Javadoc)
         * 
         * @see org.w3c.cci2.AbstractSparseArray#delegate()
         */
        @Override
        protected SortedMap<Integer, E> delegate() {
            return this.delegate;
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.AbstractSparseArray#newInstance(int, java.util.SortedMap)
         */
        @Override
        protected SparseArray<E> subArray(
            SortedMap<Integer, E> delegate
        ) {
            return new AsSparseArray<>(delegate);
        }

    }

}
