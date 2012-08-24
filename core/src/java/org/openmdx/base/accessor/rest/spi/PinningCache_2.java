/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Pinning Port 
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
import java.util.Set;
import java.util.logging.Level;

import javax.cache.Cache;
import javax.resource.ResourceException;

import org.openmdx.base.collection.Sets;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.kernel.log.SysLog;

/**
 * Pinning Port 
 */
public class PinningCache_2 extends BasicCache_2 {
    
    /**
     * Constructor 
     */
    public PinningCache_2() {
        super();
    }

    /**
     * To override CacheManager.getCacheFactory()
     */
    String pinningCacheFactoy;
    
    /**
     * The cache name is <code>null</code> unless it shall be shared
     */
    String pinningCacheName;
    
    /**
     * The cache configuration URL
     */
    URL pinningCacheConfiguration;

    /**
     * No individual entries by default
     */
    private Path[] pinId = {};

    /**
     * No individual entries by default
     */
    private String[] pinAllClass = {};

    /**
     * No individual entries by default
     */
    private Path[] pinAllPattern = {};

    /**
     * No individual entries by default
     */
    private String[] pinAllType = {};

    /**
     * Objects pinned by their object id
     */
    private Set<Path> pinnedById;
    
    /**
     * Objects pinned by their matching object id
     */
    private Set<Path> pinnedByPattern;
    
    /**
     * Objects pinned by their class
     */
    private Set<String> pinnedByClass;
    
    /**
     * Objects pinned by their class or subclass
     */
    private Set<String> pinnedByType;
    
    /**
     * <code>false</code> if all <code>pinnedBy</code> sets are empty
     */
    private boolean active = false;
    

    
    //------------------------------------------------------------------------
    // Is JavaBean
    //------------------------------------------------------------------------
    
    /**
     * Retrieve pinningCacheFactoy.
     *
     * @return Returns the pinningCacheFactoy.
     */
    public String getPinningCacheFactoy() {
        return this.pinningCacheFactoy;
    }
    
    /**
     * Set pinningCacheFactoy.
     * 
     * @param pinningCacheFactoy The pinningCacheFactoy to set.
     */
    public void setPinningCacheFactoy(String pinningCacheFactoy) {
        this.pinningCacheFactoy = "".equals(pinningCacheFactoy) ? null : pinningCacheFactoy;
    }

    /**
     * Retrieve cacheConfiguration.
     *
     * @return Returns the cacheConfiguration.
     */
    public String getAutomaticCacheConfiguration() {
        return this.pinningCacheConfiguration.toString();
    }
    
    /**
     * Set cacheConfiguration.
     * 
     * @param cacheConfiguration The cacheConfiguration to set.
     */
    public void setAutomaticCacheConfiguration(String cacheConfiguration) {
        this.pinningCacheConfiguration = toURL(cacheConfiguration);
    }

    /**
     * Retrieve pinId.
     *
     * @return Returns the pinId.
     */
    public String[] getPinId(
    ) {
        String[] pinId = new String[this.pinId.length];
        for(
            int i = 0;
            i < pinId.length;
            i++
        ){
            pinId[i] = this.pinId[i].toXRI();
        }
        return pinId;
    }

    /**
     * Retrieve pinId.
     *
     * @param index the array index
     * 
     * @return Returns the pinId.
     */
    public String getPinId(
        int index
    ) {
        return this.pinId[index].toXRI();
    }
    
    /**
     * Set pinId.
     * 
     * @param pinId The pinId to set.
     */
    public void setPinId(
        String[] pinId
    ) {
        this.pinId = new Path[pinId.length];
        for(
            int i = 0;
            i < pinId.length;
            i++
        ){
            this.pinId[i] = new Path(pinId[i]);
        }
    }

    /**
     * Set pinId.
     * 
     * @param index the array index
     * @param pinId The pinId to set.
     */
    public void setPinId(
        int index, 
        String pinId
    ) {
        this.pinId[index] = new Path(pinId);
    }
    
    /**
     * Retrieve pinAllClass.
     *
     * @return Returns the pinAllClass.
     */
    public String[] getPinAllClass() {
        return this.pinAllClass;
    }

    /**
     * Retrieve pinAllClass.
     *
     * @param index the array index
     * 
     * @return Returns the pinAllClass.
     */
    public String getPinAllClass(
        int index
    ) {
        return this.pinAllClass[index];
    }
    
    /**
     * Set pinAllClass.
     * 
     * @param pinAllClass The pinClass to set.
     */
    public void setPinAllClass(String[] pinAllClass) {
        this.pinAllClass = pinAllClass;
    }

    /**
     * Set pinClass.
     * 
     * @param index the array index
     * @param pinClass The pinClass to set.
     */
    public void setPinAllClass(
        int index, 
        String pinClass
    ) {
        this.pinAllClass[index] = pinClass;
    }
    
    /**
     * Retrieve pinAllPattern.
     *
     * @return Returns the pinAllPattern.
     */
    public String[] getPinAllPattern(
    ) {
        String[] pinAllPattern = new String[this.pinAllPattern.length];
        for(
            int i = 0;
            i < pinAllPattern.length;
            i++
        ){
            pinAllPattern[i] = this.pinAllPattern[i].toXRI();
        }
        return pinAllPattern;
    }

    /**
     * Retrieve pinAllPattern.
     *
     * @param index the array index
     * 
     * @return Returns the pinAllPattern.
     */
    public String getPinAllPattern(
        int index
    ) {
        return this.pinAllPattern[index].toXRI();
    }
    
    /**
     * Set pinAllPattern.
     * 
     * @param pinPattern The pinAllPattern to set.
     */
    public void setPinAllPattern(
        String[] pinAllPattern
    ) {
        this.pinAllPattern = new Path[pinAllPattern.length];
        for(
            int i = 0;
            i < pinAllPattern.length;
            i++
        ){
            this.pinAllPattern[i] = new Path(pinAllPattern[i]);
        }
    }

    /**
     * Set pinPattern.
     * 
     * @param index the array index
     * @param pinPattern The pinPattern to set.
     */
    public void setPinAllPattern(
        int index, 
        String pinAllPattern
    ) {
        this.pinAllPattern[index] = new Path(pinAllPattern);
    }
    
    /**
     * Retrieve pinAllType.
     *
     * @return Returns the pinAllType.
     */
    public String[] getPinAllType() {
        return this.pinAllType;
    }

    /**
     * Retrieve pinAllType.
     *
     * @param index the array index
     * 
     * @return Returns the pinAllType.
     */
    public String getPinAllType(
        int index
    ) {
        return this.pinAllType[index];
    }
    
    /**
     * Set pinAllType.
     * 
     * @param pinAllType The pinAllType to set.
     */
    public void setPinAllType(String[] pinAllType) {
        this.pinAllType = pinAllType;
    }    

    /**
     * Set pinAllType.
     * 
     * @param index the array index
     * @param pinType The pinAllType to set.
     */
    public void setPinAllType(
        int index, 
        String pinAllType
    ) {
        this.pinAllType[index] = pinAllType;
    }

    
    //------------------------------------------------------------------------
    // Implements Port
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.Caching_2#newCache(java.lang.String)
     */
    @Override
    protected BasicConnectionCache newCache(
        String id
    ) throws ResourceException {
        return new PinningCache(id);
    }

    /**
     * Create and initialize a set lazily
     * 
     * @param entries
     * 
     * @return the initialized set
     */
    static <E> Set<E> newSet(
        E[] entries
    ){
        Set<E> set = Sets.newConcurrentHashSet();
        if(entries != null) {
            for(E entry : entries) {
                set.add(entry);
            }
        }
        return set;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.VirtualObjectPort_2#initialize()
     */
    @Override
    protected void initialize(
    ) throws ServiceException {
        super.initialize();
        SysLog.detail("pinAllType", this.pinAllType);
        this.pinnedByType = newSet(this.pinAllType);
        SysLog.detail("pinAllClass", this.pinAllClass);
        this.pinnedByClass = newSet(this.pinAllClass);
        SysLog.detail("pinnedById", this.pinnedById);
        this.pinnedById = newSet(this.pinId);
        SysLog.detail("pinAllPattern", this.pinAllPattern);
        this.pinnedByPattern = newSet(this.pinAllPattern);
        this.active = 
            !this.pinnedByType.isEmpty() ||
            !this.pinnedByClass.isEmpty() ||
            !this.pinnedById.isEmpty() ||
            !this.pinnedByPattern.isEmpty();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#pin(org.openmdx.base.naming.Path)
     */
    @Override
    public void pin(
        Path xri
    ) throws ServiceException {
        assertInitialization();
        if(!isPinned(xri,null)){
            SysLog.detail("pin", xri);
            this.pinnedById.add(xri);
            this.active = true;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#pinAll(java.util.Collection)
     */
    @Override
    public void pinAll(
        Collection<Path> xris
    ) throws ServiceException {
        assertInitialization();
        SysLog.detail("pinAll", xris);
        this.pinnedById.addAll(xris);
        this.active = true;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#pinAll(org.openmdx.base.naming.Path)
     */
    @Override
    public void pinAll(
        Path xriPattern
    ) throws ServiceException {
        assertInitialization();
        SysLog.detail("pinAll", xriPattern);
        this.pinnedByPattern.add(xriPattern);
        this.active = true;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#pinAll(boolean, java.lang.String)
     */
    @Override
    public void pinAll(
        boolean subclasses, 
        String pcClass
    ) throws ServiceException {
        assertInitialization();
        SysLog.detail(subclasses ? "pinAllByType" : "pinAllByClass", pcClass);
        (subclasses ? this.pinnedByType : this.pinnedByClass).add(pcClass);
        this.active = true;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#unpin(org.openmdx.base.naming.Path)
     */
    @Override
    public void unpin(
        Path xri
    ) throws ServiceException {
        assertInitialization();
        SysLog.detail("unpin", xri);
        this.pinnedById.remove(xri);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#unpinAll(java.util.Collection)
     */
    @Override
    public void unpinAll(
        Collection<Path> xris
    ) throws ServiceException {
        assertInitialization();
        SysLog.detail("unpinAll", xris);
        this.pinnedById.removeAll(xris);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#unpinAll(org.openmdx.base.naming.Path)
     */
    @Override
    public void unpinAll(
        Path xriPattern
    ) throws ServiceException {
        assertInitialization();
        SysLog.detail("unpinAll", xriPattern);
        this.pinnedByPattern.add(xriPattern);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#unpinAll(boolean, java.lang.String)
     */
    @Override
    public void unpinAll(
        boolean subclasses, 
        String pcClass
    ) throws ServiceException {
        assertInitialization();
        SysLog.detail(subclasses ? "unpinAllByType" : "unpinAllByClass", pcClass);
        (subclasses ? this.pinnedByType : this.pinnedByClass).add(pcClass);
    }

    /**
     * Tells whether the given object is pinned
     * 
     * @param xri
     * @param pcClass
     * 
     * @return
     */
    boolean isPinned(
        Path xri,
        String pcClass
    ){
        if(this.active){
            if(xri != null){
                if(this.pinnedById.contains(xri)){
                    return true;
                }
                for(Path pattern : this.pinnedByPattern) {
                    if(xri.isLike(pattern)) {
                        return true;
                    }
                }
            }
            if(pcClass != null){
                if(this.pinnedByClass.contains(pcClass)){
                    return true;
                }
                for(String type : this.pinnedByType) {
                    if(isSubtypeOf(pcClass,type)) {
                        return true;
                    }
                }            
            }
        }
        return false;
    }
    
    //------------------------------------------------------------------------
    // Class BasicConnectionCache
    //------------------------------------------------------------------------
    
    /**
     * Basic Connection Cache
     */
    protected class PinningCache extends BasicConnectionCache {

        /**
         * Constructor 
         * 
         * @throws ServiceException 
         */
        protected PinningCache(
            String id
        ) throws ResourceException {
            super(id);
        }

        /**
         * Evictable and non-evictable objects which have been sown.
         */
        private volatile Map<Path,ObjectRecord> cache;        

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
            if(this.cache != null) {
                this.cache.remove(oid);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.Cache_2_0#evictAll()
         */
        @Override
        void evictAll(
        ) throws ServiceException {
            if(this.cache != null) {
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
            if(this.cache != null) {
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
            if(this.cache != null) {
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
            if(this.cache != null) {
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
                (mode == null || mode == Mode.PINNING) &&
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
            if(mode == null || mode == Mode.PINNING) {
                boolean pinned = isPinned(object.getPath(), object.getValue().getRecordName());
                if(pinned) try {
                    if(this.cache == null) synchronized(this) {
                        if(this.cache == null){
                            this.cache = getCache(
                                Mode.PINNING, 
                                super.userName, 
                                PinningCache_2.this.pinningCacheName, 
                                PinningCache_2.this.pinningCacheFactoy, 
                                PinningCache_2.this.pinningCacheConfiguration
                            );
                        }
                        
                    }
                    BasicCache_2.put((Cache)this.cache, object);
                } catch (ResourceException exception) {
                    throw new ServiceException(exception);
                }
                return pinned;
            } else {
                return super.put(mode, object);
            }
        }

    }
    
}
