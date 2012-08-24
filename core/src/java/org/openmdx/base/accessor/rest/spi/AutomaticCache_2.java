/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Caching Port 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2010, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.rest.spi;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

import javax.cache.Cache;
import javax.resource.ResourceException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.kernel.log.SysLog;

/**
 * Standard Cache
 */
public class AutomaticCache_2 extends PinningCache_2 {
    
    /**
     * Constructor 
     */
    public AutomaticCache_2() {
        super();
    }

    /**
     * To override CacheManager.getCacheFactory()
     */
    String automaticCacheFactoy;
    
    /**
     * The cache name is <code>null</code> unless it shall be shared
     */
    String automaticCacheName;
    
    /**
     * The cache configuration URL
     */
    URL automaticCacheConfiguration;
    

    //------------------------------------------------------------------------
    // Is JavaBean
    //------------------------------------------------------------------------
            
    /**
     * Retrieve automaticCacheFactoy.
     *
     * @return Returns the automaticCacheFactoy.
     */
    public String getAutomaticCacheFactoy() {
        return this.automaticCacheFactoy;
    }

    /**
     * Set automaticCacheFactoy.
     * 
     * @param automaticCacheFactoy The automaticCacheFactoy to set.
     */
    public void setAutomaticCacheFactoy(String automaticCacheFactoy) {
        this.automaticCacheFactoy = "".equals(automaticCacheFactoy) ? null : automaticCacheFactoy;
    }

    /**
     * Retrieve automaticCacheName.
     *
     * @return Returns the automaticCacheName.
     */
    public String getAutomaticCacheName() {
        return this.automaticCacheName;
    }

    /**
     * Set automaticCacheName.
     * 
     * @param automaticCacheName The automaticCacheName to set.
     */
    public void setAutomaticCacheName(String automaticCacheName) {
        this.automaticCacheName = "".equals(automaticCacheName) ? null : automaticCacheName;
    }

    /**
     * Retrieve cacheConfiguration.
     *
     * @return Returns the cacheConfiguration.
     */
    @Override
    public String getAutomaticCacheConfiguration() {
        return this.automaticCacheConfiguration.toString();
    }
    
    /**
     * Set cacheConfiguration.
     * 
     * @param cacheConfiguration The cacheConfiguration to set.
     */
    @Override
    public void setAutomaticCacheConfiguration(String cacheConfiguration) {
        this.automaticCacheConfiguration = toURL(cacheConfiguration);
    }

    
    //------------------------------------------------------------------------
    // Implements Port
    //------------------------------------------------------------------------
        
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.Caching_2#newCache(java.lang.String)
     */
    @Override
    protected BasicConnectionCache newCache(
        String userName
    ) throws ResourceException {
        return new AutomaticCache(userName);
    }
    
    
    //------------------------------------------------------------------------
    // Class BasicConnectionCache
    //------------------------------------------------------------------------
    
    /**
     * Basic Connection Cache
     */
    protected class AutomaticCache extends PinningCache {

        /**
         * Constructor 
         * 
         * @throws ServiceException 
         */
        protected AutomaticCache(
            String userName
        ) throws ResourceException {
            super(userName);
        }

        /**
         * Evictable and non-evictable objects which have been sown.
         */
        private Map<Path,ObjectRecord> cache;
        
        @Override
        public ObjectRecord peek(
            Path oid
        ) throws ServiceException { 
            if(this.cache != null){
                ObjectRecord object = this.cache.get(oid);
                if(object != null) {
                    SysLog.log(Level.FINEST, "Got {0} from PINNING cache", oid);
                    return object;
                }
            }
            return super.peek(oid);
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.Cache_2_0#evict(org.openmdx.base.naming.Path)
         */
        @Override
        void evict(
            Path oid
        ) throws ServiceException {
            if(this.cache != null){
                this.cache.remove(oid);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.Cache_2_0#evictAll()
         */
        @Override
        void evictAll(
        ) throws ServiceException {
            if(this.cache != null){
                this.cache.clear();
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.Cache_2_0#evictAll(org.openmdx.base.naming.Path)
         */
        @Override
        void evictAll(
            Path oidPattern
        ) throws ServiceException { 
            if(this.cache != null){
                this.evictAll(this.cache.values(), oidPattern, false, null);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.Cache_2_0#evictAll(java.util.Collection)
         */
        @Override
        void evictAll(
            Collection<Path> oids
        ) throws ServiceException { 
            if(this.cache != null){
                this.cache.keySet().removeAll(oids);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.Cache_2_0#evictAll(boolean, java.lang.String)
         */
        @Override
        void evictAll(
            boolean subclasses, 
            String pcClass
        ) throws ServiceException { 
            if(this.cache != null){
                this.evictAll(this.cache.values(), null, subclasses, pcClass);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.Cache_2_0#isLCached(org.openmdx.base.naming.Path)
         */
        @Override
        public boolean isAvailable(
            Mode mode, 
            Path xri
        ) throws ServiceException {
            return (
                (mode == null || mode == Mode.AUTOMATIC) &&
                this.cache != null && 
                this.cache.containsKey(xri)
            ) || super.isAvailable(mode, xri);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.Cache_2_0#put(org.openmdx.base.naming.Path, javax.resource.cci.MappedRecord)
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean put(
            Mode mode, 
            ObjectRecord object
        ) throws ServiceException {
            if(super.put(mode, object)){
                return true;
            }
            if(mode == null || mode == Mode.AUTOMATIC) try {
                if(this.cache == null) synchronized(this) {
                    if(this.cache == null){
                        this.cache = getCache(
                            Mode.AUTOMATIC, 
                            super.userName, 
                            AutomaticCache_2.this.automaticCacheName, 
                            AutomaticCache_2.this.automaticCacheFactoy, 
                            AutomaticCache_2.this.automaticCacheConfiguration
                        );
                    }
                }
                BasicCache_2.put((Cache)this.cache, object);
                return true;
            } catch (ResourceException exception) {
                throw new ServiceException(exception);
            }
            return false;
        }
    }
    
}
