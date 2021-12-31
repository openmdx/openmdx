/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Cache Test 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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
package test.cache;

import java.util.Date;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;

/**
 * Cache Test
 * 
 * This test is to be ignored at the moment
 */
public class CacheTest {
    
    /**
     * Use the configured cache factory
     * 
     * @param arguments
     */
    public static void main(
        String... arguments
    ){
        try {
            final CachingProvider cachingProvider = Caching.getCachingProvider();
            final CacheManager cacheManager = cachingProvider.getCacheManager();
            final Configuration<String, Date> configuration = new Configuration<String, Date>() {

                /**
				 * Implements {@code Serializable}
				 */
				private static final long serialVersionUID = -8761562372515462710L;

				@Override
                public Class<String> getKeyType(
                ) {
                    return String.class;
                }

                @Override
                public Class<Date> getValueType(
                ) {
                    return Date.class;
                }

                @Override
                public boolean isStoreByValue(
                ) {
                    return false;
                }
            };
            Date now = new Date();
            Cache<String,Date> cache1 = cacheManager.createCache("testCache", configuration);
            cache1.put("created", now);
            now = new Date(now.getTime() + 1000);
            Cache<String,Date> cache2 = cacheManager.createCache("testCache", configuration);
            cache2.put("created", now);
            System.out.println(cache1.getClass().getName() + '@' + System.identityHashCode(cache1) + " created at " + cache1.get("created"));
            System.out.println(cache2.getClass().getName() + '@' + System.identityHashCode(cache2) + " created at " + cache2.get("created"));
        } catch (CacheException exception) {
            exception.printStackTrace();
        }
        
    }

}
