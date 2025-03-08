/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Cache Test 
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
package test.cache;


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
            final Configuration<String, #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif> configuration = new Configuration<>() {

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
                public Class<#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif> getValueType(
                ) {
                    return #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif.class;
                }

                @Override
                public boolean isStoreByValue(
                ) {
                    return false;
                }
            };
            #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif now = #if CLASSIC_CHRONO_TYPES new java.util.Date() #else java.time.Instant.now() #endif;
            Cache<String,#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif> cache1 = cacheManager.createCache("testCache", configuration);
            cache1.put("created", now);
            #if CLASSIC_CHRONO_TYPES now = new java.util.Date(now.getTime() + 1000) #else now = now.plusMillis(1000) #endif;
//            now = new Date(now.getTime() + 1000);
            Cache<String,#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif> cache2 = cacheManager.createCache("testCache", configuration);
            cache2.put("created", now);
            System.out.println(cache1.getClass().getName() + '@' + System.identityHashCode(cache1) + " created at " + cache1.get("created"));
            System.out.println(cache2.getClass().getName() + '@' + System.identityHashCode(cache2) + " created at " + cache2.get("created"));
        } catch (CacheException exception) {
            exception.printStackTrace();
        }
        
    }

}
