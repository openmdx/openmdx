/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: JDO Data Store Cache
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
package org.openmdx.base.caching.datastore;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.integration.CacheLoader;

import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.kernel.jdo.JDODataStoreCache;

/**
 * JDO Data Store Cache
 * 
 * @since openMDX 2.17
 */
public interface CacheAdapter extends JDODataStoreCache {

    /**
     * Offer an {@link ObjectRecord} for caching. 
     * This allows the {@link CacheAdapter} to mediate the cache usage.
     * 
     * @param objectRecord an object record which may be cached at the 
     * {@link CacheAdapter}'s discretion
     * 
     * @since openMDX 2.17
     */
    void offer(ObjectRecord objectRecord);
    
    /**
     * Determines if the {@link Cache} contains an entry for the specified key.
     * <p>
     * If the cache is configured read-through the associated {@link CacheLoader}
     * is not called. Only the cache is checked.
     * </p>
     * @param xri key whose presence in this cache is to be tested.
     * @return <tt>true</tt> if this cache contains a mapping for the specified key
     * @throws NullPointerException  if key is null
     * @throws IllegalStateException if the cache is {@link #isClosed()}
     * @throws RuntimeException      it there is a problem checking the mapping
     * 
     * @since openMDX 2.17
     */
    boolean containsKey(Path xri);
    
    /**
     * Gets an entry from the cache.
     * <p>
     * If the cache is configured to use read-through, and get would return null
     * because the entry is missing from the cache, the Cache's {@link CacheLoader}
     * is called in an attempt to load the entry.
     *
     * @param xri the key whose associated value is to be returned
     * @return the element, or null, if it does not exist.
     * @throws IllegalStateException if the cache is {@link #isClosed()}
     * @throws NullPointerException  if the key is null
     * @throws RuntimeException      if there is a problem fetching the value
     * 
     * @since openMDX 2.17
     */
    ObjectRecord get(Path xri);

    /**
     * Gets a collection of entries from the {@link Cache}, returning them as
     * {@link Map} of the values associated with the set of keys requested.
     * <p>
     * If the cache is configured read-through, and a get for a key would
     * return null because an entry is missing from the cache, the Cache's
     * {@link CacheLoader} is called in an attempt to load the entry. If an
     * entry cannot be loaded for a given key, the key will not be present in
     * the returned Map.
     *
     * @param xris The keys whose associated values are to be returned.
     * @return A map of entries that were found for the given keys. Keys not found
     *         in the cache are not in the returned map.
     * @throws NullPointerException  if keys is null or if keys contains a null
     * @throws IllegalStateException if the cache is {@link #isClosed()}
     * @throws CacheException        if there is a problem fetching the values
     * @throws ClassCastException    if the implementation is configured to perform
     *                               runtime-type-checking, and the key or value
     *                               types are incompatible with those that have been
     *                               configured for the {@link Cache}
     * 
     * @since openMDX 2.17
     */
    Map<Path, ObjectRecord> getAll(Set<Path> xris);

    /** 
     * Evict the parameter instances from the second-level cache.
     * @param filter matching objects are evicted
     * 
     * @see #evictAll(boolean, Class)
     * 
     * @since openMDX 2.17
     */
    void evictAll (Predicate<ObjectRecord> filter);
    
    /**
     * Use the predicate based method at JCA level
     * 
     * @deprecated use {@link #evictAll(Predicate)}
     */
    @SuppressWarnings("rawtypes")
    @Override
    @Deprecated
    default void evictAll (Class pcClass, boolean subclasses) {
        throw new UnsupportedOperationException(
            "jmi1/cci2 based methods shall not be invoked at JCA level"
        );
    }

    /**
     * Use the predicate based method at JCA level
     * 
     * @deprecated use {@link #evictAll(Predicate)}
     */
    @SuppressWarnings("rawtypes")
    @Override
    @Deprecated
    default void evictAll (boolean subclasses, Class pcClass) {
        throw new UnsupportedOperationException(
            "jmi1/cci2 based methods shall not be invoked at JCA level"
        );
    }
    
    /** 
     * Pin instances in the second-level cache.
     * <p>
     * <em>
     * Note:<br>
     * A {@code CacheAdapter} keeps a reference to the given filter.
     * {@code filter.equals(Object)) is used to unpin it later on.
     * </em>
     * @param filter matching objects are evicted
     * 
     * @see #pinAll(boolean, Class)
     * @see #unpinAll(Predicate)
     * 
     * @since openMDX 2.17
     */
    void pinAll (Predicate<ObjectRecord> filter);

    /**
     * Use the predicate based method at JCA level
     * 
     * @deprecated use {@link #pinAll(Predicate)}
     */
    @SuppressWarnings("rawtypes")
    @Override
    @Deprecated
    default void pinAll(
        Class pcClass,
        boolean subclasses
    ) {
        throw new UnsupportedOperationException(
            "jmi1/cci2 based methods shall not be invoked at JCA level"
        );
    }

    /**
     * Use the predicate based method at JCA level
     * 
     * @deprecated use {@link #pinAll(Predicate)}
     */
    @SuppressWarnings("rawtypes")
    @Override
    @Deprecated
    default void pinAll(
        boolean subclasses,
        Class pcClass
    ) {
        throw new UnsupportedOperationException(
            "jmi1/cci2 based methods shall not be invoked at JCA level"
        );
    }
    
    /** 
     * Unpin instances from the second-level cache.
     * <p>
     * <em>
     * Note:<br>
     * {@code filter.equals(Object)) is used for unpinning.
     * </em>
     * @param filter matching objects are evicted
     * 
     * @see #unpinAll(boolean, Class)
     * @see #pinAll(Predicate)
     * 
     * @since openMDX 2.17
     */
    void unpinAll(Predicate<ObjectRecord> filter);

    /**
     * Use the predicate based method at JCA level
     * 
     * @deprecated use {@link #unpinAll(Predicate)}
     */
    @SuppressWarnings("rawtypes")
    @Override
    @Deprecated
    default void unpinAll(Class pcClass, boolean subclasses) {
        throw new UnsupportedOperationException(
            "jmi1/cci2 based methods shall not be invoked at JCA level"
        );
    }
    
    /**
     * Use the predicate based method at JCA level
     * 
     * @deprecated use {@link #unpinAll(Predicate)}
     */
    @SuppressWarnings("rawtypes")
    @Override
    @Deprecated
    default void unpinAll(boolean subclasses, Class pcClass) {
        throw new UnsupportedOperationException(
            "jmi1/cci2 based methods shall not be invoked at JCA level"
        );
    }

}
