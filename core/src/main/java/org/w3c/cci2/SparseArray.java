/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: SparseArray 
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

import java.util.List;
import java.util.ListIterator;
import java.util.SortedMap;

/**
 * SparseArray
 * <p>
 * Setting a value to {@code null} is equivalent to removing the entry.
 */
public interface SparseArray<E> extends SortedMap<Integer, E>, Iterable<E> {

    /**
     * A list backed up by the sparse array:<ul>
     * <li>its size() is the sparse array's {@code lastKey() + 1}
     * <li>the sparse array's un-populated positions are represented as {@code null</null> values
     * <li>{@code get(int)} and set(int,E) operations are allowed for any index >= 0
     * <li>{@code add(E)} is allowed
     * </ul>
     *  
     * @return a {@code List} representing this {@code SparseArray}
     */
    List<E> asList();

    /**
     * This iterator skips all null values.
     * 
     * @return an iterator for the non-null elements
     */
    ListIterator<E> populationIterator();
    
    /**
     * Returns a view of the portion of this sparse array whose keys range from
     * {@code fromKey}, inclusive, to {@code toKey}, exclusive.  (If
     * {@code fromKey} and {@code toKey} are equal, the returned sparse array
     * is empty.)  The returned sparse array is backed by this sparse array, so
     * changes in the returned sparse array are reflected in this sparse array,
     * and vice-versa.  The returned Map supports all optional map operations
     * that this sparse array supports.<p>
     *
     * The map returned by this method will throw an
     * {@code IllegalArgumentException} if the user attempts to insert a key
     * outside the specified range.<p>
     *
     * Note: this method always returns a <i>half-open range</i> (which
     * includes its low endpoint but not its high endpoint).  If you need a
     * <i>closed range</i> (which includes both endpoints), and the key type
     * allows for calculation of the successor a given key, merely request the
     * subrange from {@code lowEndpoint} to {@code successor(highEndpoint)}.
     * Sparse arrays are map whose keys are integers.
     * The following idiom obtains a view containing all of the key-value
     * mappings in {@code m} whose keys are between {@code low} and
     * {@code high}, inclusive:
     * 
     *      <pre>    Map sub = m.subMap(low, high + 1);</pre>
     * 
     * A similarly technique can be used to generate an <i>open range</i>
     * (which contains neither endpoint).  The following idiom obtains a
     * view containing  all of the key-value mappings in {@code m} whose keys
     * are between {@code low} and {@code high}, exclusive:
     * 
     *      <pre>    Map sub = m.subMap(low + 1, high);</pre>
     *
     * @param fromKey low endpoint (inclusive) of the subMap.
     * @param toKey high endpoint (exclusive) of the subMap.
     *
     * @return a view of the specified range within this sparse array.
     * 
     * @throws IllegalArgumentException if {@code fromKey} is greater than
     *         {@code toKey}; or if this map is itself a subMap, headMap,
     *         or tailMap, and {@code fromKey} or {@code toKey} are not
     *         within the specified range of the subMap, headMap, or tailMap.
     * @throws NullPointerException if {@code fromKey} or {@code toKey} is
     *         {@code null}.
     */
    SparseArray<E> subMap(Integer fromKey, Integer toKey);

    /**
     * Returns a view of the portion of this sparse array whose keys are
     * strictly less than toKey.  The returned sparse array is backed by this
     * sparse array, so changes in the returned sparse array are reflected in this
     * sparse array, and vice-versa.  The returned map supports all optional map
     * operations that this sparse array supports.<p>
     *
     * The map returned by this method will throw an IllegalArgumentException
     * if the user attempts to insert a key outside the specified range.<p>
     *
     * Note: this method always returns a view that does not contain its
     * (high) endpoint.  If you need a view that does contain this endpoint,
     * and the key type allows for calculation of the successor a given
     * key, merely request a headMap bounded by successor(highEndpoint).
     * Sparse arrays are map whose keys are integers.
     * The following idiom obtains a view containing all of the
     * key-value mappings in {@code m} whose keys are less than or equal to
     * {@code high}:
     * 
     *      <pre>    Map head = m.headMap(high + 1);</pre>
     *
     * @param toKey high endpoint (exclusive) of the subMap.
     *
     * @return a view of the specified initial range of this sparse array.
     *
     * @throws IllegalArgumentException if this map is itself a subMap,
     *         headMap, or tailMap, and {@code toKey} is not within the
     *         specified range of the subMap, headMap, or tailMap.
     * @throws NullPointerException if {@code toKey} is {@code null}.
     */
    SparseArray<E> headMap(Integer toKey);

    /**
     * Returns a view of the portion of this sparse array whose keys are greater
     * than or equal to {@code fromKey}.  The returned sparse array is backed
     * by this sparse array, so changes in the returned sparse array are reflected
     * in this sparse array, and vice-versa.  The returned map supports all
     * optional map operations that this sparse array supports.<p>
     *
     * The map returned by this method will throw an
     * {@code IllegalArgumentException} if the user attempts to insert a key
     * outside the specified range.<p>
     *
     * Note: this method always returns a view that contains its (low)
     * endpoint.  If you need a view that does not contain this endpoint, and
     * the element type allows for calculation of the successor a given value,
     * merely request a tailMap bounded by {@code successor(lowEndpoint)}.
     * Sparse arrays are map whose keys are integers.
     * The following idiom obtains a view containing all of the
     * key-value mappings in {@code m} whose keys are strictly greater than
     * {@code low}:
     * 
     *      <pre>    Map tail = m.tailMap(low + 1);</pre>
     *
     * @param fromKey low endpoint (inclusive) of the tailMap.
     *
     * @return a view of the specified final range of this sparse array.
     *
     * @throws IllegalArgumentException if this map is itself a subMap,
     *         headMap, or tailMap, and {@code fromKey} is not within the
     *         specified range of the subMap, headMap, or tailMap.
     * @throws NullPointerException if {@code fromKey} is {@code null}.
     */
    SparseArray<E> tailMap(Integer fromKey);

}
