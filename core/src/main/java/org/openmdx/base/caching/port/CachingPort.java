/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Caching Port 
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

package org.openmdx.base.caching.port;

import javax.cache.Cache;
import javax.cache.integration.CacheLoader;

import org.openmdx.base.caching.datastore.CacheAdapter;
import org.openmdx.base.caching.virtualobjects.VirtualObjectProvider;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.RestConnection;

/**
 * Caching Port
 * 
 * @since openMDX 2.17
 */
public interface CachingPort extends Port<RestConnection> {

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
     * To connect the port with its virtual object provider
     * 
     * @param virtualObjectProvider the virtual object provider
     * 
     * @since openMDX 2.17
     */
    void setVirtualObjectProvider(
      VirtualObjectProvider virtualObjectProvider
    );

    /**
     * To connect the port with its cache adapter
     * 
     * @param cacheAdapter the cache adapter
     * 
     * @since openMDX 2.17
     */
    void setCacheAdapter(
      CacheAdapter cacheAdapter
    );

}
