/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Simple Adapter for a JSR 107 compliant cache
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2020, OMEX AG, Switzerland
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
package org.openmdx.base.caching.datastore;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;

/**
 * Sample Adapter for a JSR&nbsp;107 compliant cache.
 * <p>
 * <em>
 * Note:
 * This is just an example.<ul>
 * <li>It is usually unwise to cache all offered objects 
 * independent of their change frequency.
 * <li>Pinning is not supported by this sample adapter.
 * </em>
 * 
 * @since openMDX 2.17
 */
public class SampleCacheAdapter implements CacheAdapter {

    /**
     * The Java Bean property {@code uri}
     */
    private URI uri;
    
    /**
     * The Java Bean property {@code cachName}
     */
    private String cacheName;

    /**
     * The lazily retrieved cache
     */
    private Cache<Path, ObjectRecord> cache;
    
    /**
     * Retrieve uri.
     *
     * @return Returns the uri.
     */
    public String getUri() {
        return this.uri == null ? null : this.uri.toString();
    }

    /**
     * Set uri.
     * 
     * @param uri The uri to set.
     * 
     * @throws  IllegalArgumentException
     *          If the given string violates RFC&nbsp;2396
     */
    public void setUri(String uri) {
        this.uri = uri == null ? null : URI.create(uri);
    }

    /**
     * Retrieve cacheName.
     *
     * @return Returns the cacheName.
     */
    public String getCacheName() {
        return this.cacheName;
    }

    /**
     * Set cacheName.
     * 
     * @param cacheName The cacheName to set.
     */
    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    protected Cache<Path, ObjectRecord> getCache() {
        if(cache == null) {
            cache = getManager().getCache(cacheName, Path.class, ObjectRecord.class);
        }
        return cache;
    }

    protected CacheManager getManager() {
        return getProvider().getCacheManager(uri, getClass().getClassLoader());
    }

    protected CachingProvider getProvider() {
        return Caching.getCachingProvider(getClass().getClassLoader());
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.caching.datastore.CacheAdapter#containsKey(org.openmdx.base.naming.Path)
     */
    @Override
    public boolean containsKey(Path key) {
        return getCache().containsKey(key);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.caching.datastore.CacheAdapter#offer(org.openmdx.base.rest.cci.ObjectRecord)
     */
    @Override
    public void offer(ObjectRecord value) {
        getCache().put(value.getResourceIdentifier(), value);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.caching.datastore.CacheAdapter#get(org.openmdx.base.naming.Path)
     */
    @Override
    public ObjectRecord get(Path key) {
        return getCache().get(key);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.caching.datastore.CacheAdapter#getAll(java.util.Set)
     */
    @Override
    public Map<Path, ObjectRecord> getAll(Set<Path> keys) {
        return getCache().getAll(keys);
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#evict(java.lang.Object)
     */
    @Override
    public void evict(Object oid) {
        getCache().remove((Path)oid);
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#evictAll()
     */
    @Override
    public void evictAll() {
        getCache().removeAll();
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#evictAll(java.lang.Object[])
     */
    @Override
    public void evictAll(Object... oids) {
        evictAll(Arrays.asList(oids));
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#evictAll(java.util.Collection)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void evictAll(Collection oids) {
        getCache().removeAll(
            new HashSet<Path>(oids)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.caching.datastore.CacheAdapter#evictAll(java.util.function.Predicate)
     */
    @Override
    public void evictAll(Predicate<ObjectRecord> filter) {
        for(
            Iterator<Cache.Entry<Path,ObjectRecord>> i = getCache().iterator();
            i.hasNext();
        ) {
            final Cache.Entry<Path, ObjectRecord> entry = i.next();
            if(entry != null && filter.test(entry.getValue())) {
                i.remove();
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#pin(java.lang.Object)
     */
    @Override
    public void pin(Object oid) {
        // Pinning not supported by the sample cache adapter
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#pinAll(java.util.Collection)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void pinAll(Collection oids) {
        // Pinning not supported by the sample cache adapter
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#pinAll(java.lang.Object[])
     */
    @Override
    public void pinAll(Object... oids) {
        // Pinning not supported by the sample cache adapter
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.caching.datastore.CacheAdapter#pinAll(java.util.function.Predicate)
     */
    @Override
    public void pinAll(Predicate<ObjectRecord> filter) {
        // Pinning not supported by the sample cache adapter
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#unpin(java.lang.Object)
     */
    @Override
    public void unpin(Object oid) {
        // Pinning not supported by the sample cache adapter
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#unpinAll(java.util.Collection)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void unpinAll(Collection oids) {
        // Pinning not supported by the sample cache adapter
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.DataStoreCache#unpinAll(java.lang.Object[])
     */
    @Override
    public void unpinAll(Object... oids) {
        // Pinning not supported by the sample cache adapter
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.caching.datastore.CacheAdapter#unpinAll(java.util.function.Predicate)
     */
    @Override
    public void unpinAll(Predicate<ObjectRecord> filter) {
        // Pinning not supported by thobjobje sample cache adapter
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.jdo.JDODataStoreCache#unwrap(java.lang.Class)
     */
    @Override
    public <T> T unwrap(Class<T> clazz) {
        if(clazz.isInstance(this.cache)) {
            return clazz.cast(this.cache);
        }
        return this.cache.unwrap(clazz);
    }
    
}
