/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: BasicCache_2.java,v 1.7 2010/06/02 16:14:39 hburger Exp $
 * Description: Virtual Object Port
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/02 16:14:39 $
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;

import org.openmdx.base.accessor.rest.spi.ManagedConnectionCache_2_0.Mode;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.log.SysLog;

/**
 * Virtual Object Port 
 */
public class BasicCache_2 implements Port, DataStoreCache_2_0  {
    
    /**
     * Constructor 
     */
    public BasicCache_2() {
        for(Mode mode : Mode.values()) {
            loggers[mode.ordinal()] = new CacheLogger(mode.toString(), Level.FINEST); 
        }
    }

    /**
     * Time to live in seconds, defaults to an hour
     */
    int timeToLive = 3600;

    /**
     * 
     */
    final CacheLogger[] loggers = new CacheLogger[3];
    
    /**
     * Catch Authorities and Providers by default
     */
    private Path[] sowAllPattern = {
        new Path("xri://@openmdx*($..)"),
        new Path("xri://@openmdx*($..)/provider/($..)")
    };

    /**
     * The <code>sowAllClass</code> index must match the <code>sowAllPattern</code> index.
     * 
     * @see #sowAllPattern
     */
    private String[] sowAllClass = {
        "org:openmdx:base:Authority",
        "org:openmdx:base:Provider"
    };

    /**
     * Keeps track of the caches
     */
    private volatile ConcurrentMap<String,BasicConnectionCache> caches = null;

    /**
     * Evictable and non-evictable 
     */
    Map<Path,String> sownByPattern;
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#evict(org.openmdx.base.naming.Path)
     */
//  @Override
    public void evict(
        Path xri
    ) throws ServiceException {
        for(
            Iterator<BasicConnectionCache> i = this.caches.values().iterator();
            i.hasNext();
        ){
            BasicConnectionCache cache = i.next();
            if(cache.isExpired()) {
                cache.evictAll();
                i.remove();
            } else {
                cache.evict(xri);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#evictAll()
     */
//  @Override
    public void evictAll(
    ) throws ServiceException {
        for(
            Iterator<BasicConnectionCache> i = this.caches.values().iterator();
            i.hasNext();
        ){
            BasicConnectionCache cache = i.next();
            cache.evictAll();
            if(cache.isExpired()) {
                i.remove();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#evictAll(org.openmdx.base.naming.Path)
     */
//  @Override
    public void evictAll(
        Path xriPattern
    ) throws ServiceException {
        for(
            Iterator<BasicConnectionCache> i = this.caches.values().iterator();
            i.hasNext();
        ){
            BasicConnectionCache cache = i.next();
            if(cache.isExpired()) {
                cache.evictAll();
                i.remove();
            } else {
                cache.evictAll(xriPattern);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#evictAll(java.util.Collection)
     */
//  @Override
    public void evictAll(
        Collection<Path> xris
    ) throws ServiceException {
        for(
            Iterator<BasicConnectionCache> i = this.caches.values().iterator();
            i.hasNext();
        ){
            BasicConnectionCache cache = i.next();
            if(cache.isExpired()) {
                cache.evictAll();
                i.remove();
            } else {
                cache.evictAll(xris);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#evictAll(boolean, java.lang.String)
     */
//  @Override
    public void evictAll(
        boolean subclasses, 
        String pcClass
    ) throws ServiceException {
        for(
            Iterator<BasicConnectionCache> i = this.caches.values().iterator();
            i.hasNext();
        ){
            BasicConnectionCache cache = i.next();
            if(cache.isExpired()) {
                cache.evictAll();
                i.remove();
            } else {
                cache.evictAll(subclasses, pcClass);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#pin(org.openmdx.base.naming.Path)
     */
//  @Override
    public void pin(
        Path xri
    ) throws ServiceException {
        // nothing to to
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#pinAll(java.util.Collection)
     */
//  @Override
    public void pinAll(
        Collection<Path> xris
    ) throws ServiceException {
        // nothing to to
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#pinAll(org.openmdx.base.naming.Path)
     */
//  @Override
    public void pinAll(
        Path xriPattern
    ) throws ServiceException {
        // nothing to to
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#pinAll(boolean, java.lang.String)
     */
//  @Override
    public void pinAll(
        boolean subclasses, 
        String pcClass
    ) throws ServiceException {
        // nothing to to
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#unpin(org.openmdx.base.naming.Path)
     */
//  @Override
    public void unpin(
        Path xri
    ) throws ServiceException {
        // nothing to to
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#unpinAll(java.util.Collection)
     */
//  @Override
    public void unpinAll(
        Collection<Path> xris
    ) throws ServiceException {
        // nothing to to
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#unpinAll(org.openmdx.base.naming.Path)
     */
//  @Override
    public void unpinAll(
        Path xriPattern
    ) throws ServiceException {
        // nothing to to
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0#unpinAll(boolean, java.lang.String)
     */
//  @Override
    public void unpinAll(
        boolean subclasses, 
        String pcClass
    ) throws ServiceException {
        // nothing to to
    }

    
    //------------------------------------------------------------------------
    // Is JavaBean
    //------------------------------------------------------------------------
    
    /**
     * Retrieve sowAllPattern.
     *
     * @return Returns the sowAllPattern.
     */
    public String[] getSowAllPattern(
    ) {
        String[] sowAllPattern = new String[this.sowAllPattern.length];
        for(
            int i = 0;
            i < sowAllPattern.length;
            i++
        ){
            sowAllPattern[i] = this.sowAllPattern[i].toXRI();
        }
        return sowAllPattern;
    }
    
    /**
     * Retrieve timeToLive.
     *
     * @return Returns the timeToLive.
     */
    public int getTimeToLive() {
        return this.timeToLive;
    }
    
    /**
     * Set timeToLive.
     * 
     * @param timeToLive The timeToLive to set.
     */
    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    /**
     * Retrieve sowAllPattern.
     *
     * @param index the array index
     * 
     * @return Returns the sowAllPattern.
     */
    public String getSowAllPattern(
        int index
    ) {
        return this.sowAllPattern[index].toXRI();
    }
    
    /**
     * Set sowAllPattern.
     * 
     * @param sowAllPattern The sowAllPattern to set.
     */
    public void setSowAllPattern(
        String[] sowAllPattern
    ) {
        this.sowAllPattern = new Path[sowAllPattern.length];
        for(
            int i = 0;
            i < sowAllPattern.length;
            i++
        ){
            this.sowAllPattern[i] = new Path(sowAllPattern[i]);
        }
    }

    /**
     * Set sowAllPattern.
     * 
     * @param index the array index
     * @param sowAllPattern The sowAllPattern to set.
     */
    public void setSowAllPattern(
        int index, 
        String sowAllPattern
    ) {
        this.sowAllPattern[index] = new Path(sowAllPattern);
    }
    
    /**
     * Retrieve sowAllClass.
     *
     * @return Returns the sowAllClass.
     */
    public String[] getSowAllClass() {
        return this.sowAllClass;
    }

    /**
     * Retrieve sowAllClass.
     *
     * @param index the array index
     * 
     * @return Returns the sowAllClass.
     */
    public String getSowAllClass(
        int index
    ) {
        return this.sowAllClass[index];
    }
    
    /**
     * Set sowAllClass.
     * 
     * @param sowAllClass The sowAllClass to set.
     */
    public void setSowAllClass(
        String[] sowAllClass
    ) {
        this.sowAllClass = sowAllClass;
    }

    /**
     * Set sowAllClass.
     * 
     * @param index the array index
     * @param sowAllPattern The sowAllClass to set.
     */
    public void setSowAllClass(
        int index, 
        String sowAllClass
    ) {
        this.sowAllClass[index] = sowAllClass;
    }

    
    //------------------------------------------------------------------------
    // Implements Port
    //------------------------------------------------------------------------

    /**
     * Retrieve the cache factory
     * 
     * @param factoryClass <code>null</code> or the fully qualified cache factory class name
     * 
     * @return the cache factory
     */
    private CacheFactory getCacheFactory(
        String factoryClass
    ) throws CacheException{
        if(factoryClass == null) {
            return CacheManager.getInstance().getCacheFactory(); 
        } else try {
            return Classes.newApplicationInstance(CacheFactory.class, factoryClass);
        } catch (Exception exception) {
            throw new CacheException(
                "Cache factory acquisition failure",
                exception
            );
        } 
    }
    
    /**
     * Create a cache
     * 
     * @param mode 
     * @param factoryClass <code>null</code> or the fully qualified cache factory class name
     * @param configuration the configuration property file URL
     * 
     * @return a new cache
     * @throws ResourceException 
     */
    private Cache newCache(
        Mode mode,
        String factoryClass, 
        URL configuration
    ) throws CacheException, ResourceException{
        Cache cache = this.getCacheFactory(
            factoryClass
        ).createCache(
            BasicCache_2.getConfiguration(configuration)
        );
        cache.addListener(loggers[mode.ordinal()]);
        return cache;
    }
    
    
    /**
     * Retrieve a cache instance
     *
     * @param mode 
     * @param userName 
     * @param cacheName 
     * @param cacheFactory 
     * @param configuration
     */
    protected Cache getCache(
        Mode mode,
        String userName, 
        String cacheName, 
        String cacheFactory, URL configuration
    ) throws ResourceException{
        try {
            if(cacheName == null){
                return this.newCache(mode, cacheFactory, configuration);
            } else {
                String id = userName + '@' + cacheName;
                CacheManager cacheManager = CacheManager.getInstance(); 
                Cache cache = cacheManager.getCache(id);
                if(cache == null) synchronized(cacheManager){
                    cache = cacheManager.getCache(id);
                    if(cache == null) {
                        cacheManager.registerCache(
                            id, 
                            cache  = this.newCache(mode, cacheFactory, configuration)
                        );
                    }
                }
                return cache;
            }
        } catch (CacheException exception) {
            throw new ResourceException(
                "Cache acquisition failure",
                exception
            );
        }
    }
    
    /**
     * Convert the configuration URI to an URL
     * 
     * @param uri the configuration URI
     * 
     * @return the configuration URL
     */
    protected static URL toURL(
        String uri
    ){
        try {
            return "".equals(uri) ? null : new URL(uri);
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException(
                "Invalid configuration URL",
                exception
            );
        }
    }
    
    /**
     * Retrieve a configuration
     * 
     * @param url the configuration URL
     * 
     * @return the configuration
     */
    protected static Properties getConfiguration(
        URL url
    ) throws ResourceException {
        Properties cacheConfiguration = new Properties();
        if(url != null) try {
            cacheConfiguration.load(url.openStream());
        } catch (IOException exception) {
            throw ResourceExceptions.initHolder(
                new ResourceException(
                    "Cache configuration retrieval failure",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION,
                        new BasicException.Parameter("configuration", url)
                    )
                )
            );
        }
        return cacheConfiguration;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.spi.Caching_2#newCache(java.lang.String)
     */
    protected BasicConnectionCache newCache(
        String userName
    ) throws ResourceException {
        return new BasicConnectionCache(userName);
    }

    /**
     * Lazy initialization is required for all Java-Bean properties being available
     * @throws ServiceException 
     * @throws ResourceException 
     */
    protected void initialize(
    ) throws ServiceException{
        int keyLength = this.sowAllPattern == null ? 0 : this.sowAllPattern.length;
        int valueLength = this.sowAllClass == null ? 0 : this.sowAllClass.length;
        if(valueLength == keyLength) {
            SysLog.detail("sowAllPattern", this.sowAllPattern);
            SysLog.detail("sowAllClass", this.sowAllClass);
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Keys and values do not match",
                new BasicException.Parameter("sowAllPattern", (Object[])this.sowAllPattern),
                new BasicException.Parameter("sowAllClass", (Object[])this.sowAllClass)
            );
        }
        this.sownByPattern = new ConcurrentHashMap<Path, String>();
        for(
            int i = 0;
            i < this.sowAllPattern.length;
            i++
        ){
            this.sownByPattern.put(
                this.sowAllPattern[i], 
                this.sowAllClass[i]
            );
        }
        this.caches = new ConcurrentHashMap<String, BasicConnectionCache>();
    }

    protected void assertInitialization(
    ) throws ServiceException{
        if(this.caches == null) {
            synchronized(this) {
                if(this.caches == null){
                    this.initialize();
                }
            }
        }
    }
    
    /**
     * Retrieve the connection specific cache
     * 
     * @param username
     * 
     * @return a (maybe newly created) cache
     * 
     * @throws ResourceException in case of failure 
     */
    private BasicConnectionCache getCache(
        Connection connection
    ) throws ResourceException{
        if(this.caches == null) {
            synchronized(this) {
                if(this.caches == null) try {
                    this.initialize();
                } catch (ServiceException exception) {
                    BasicException cause = exception.getCause();
                    throw new ResourceException(
                        cause.getDescription(),
                        cause
                    );
                }
            }
        }
        String userName = connection.getMetaData().getUserName();
        BasicConnectionCache cache = this.caches.get(userName);
        if(cache == null) {
            BasicConnectionCache concurrent = this.caches.putIfAbsent(
                userName,
                cache = this.newCache(userName)
            );
            if(concurrent == null) {
                return cache;
            } else {
                concurrent.touch();
                return concurrent;
            }
        } else {
            cache.touch();
            return cache;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.Port#getInteraction(javax.resource.cci.Connection)
     */
//  @Override
    public CachingInteraction getInteraction(
        Connection connection
    ) throws ResourceException {
        return new CachingInteraction(
            connection,
            this.getCache(connection)
        );
    }
    
    /**
     * Tests whether a given type is a subtype of another type
     * 
     * @param type
     * @param ofType
     * 
     * @return <code>true</code> if <code>type</code> is a subtype of <code>ofType</code>
     */
    protected static final boolean isSubtypeOf(
        String type,
        String ofType
    ){
        try {
            return Model_1Factory.getModel().isSubtypeOf(type, ofType);
        } catch (ServiceException ignored) {
            return false;
        }
    }
    
    @SuppressWarnings("unchecked")
    protected static void put(
        Cache cache,
        ObjectRecord newObject
    ) throws ResourceException {
        Path xri = newObject.getPath();
        ObjectRecord oldObject = (ObjectRecord) cache.peek(xri);
        if(oldObject != null){
            Object oldVersion = oldObject.getVersion();
            Object newVersion = newObject.getVersion();
            if(oldVersion == null ? newVersion == null : oldVersion.equals(newVersion)) {
                MappedRecord oldValues = oldObject.getValue();
                MappedRecord newValues = newObject.getValue();
                MappedRecord values = Records.getRecordFactory().createMappedRecord(oldValues.getRecordName());
                values.putAll(oldValues);
                values.putAll(newValues);
                ObjectRecord object = (ObjectRecord) Records.getRecordFactory().createMappedRecord(ObjectRecord.NAME);
                object.setPath(xri);
                object.setVersion(oldVersion);
                object.setValue(values);
                cache.put(xri, object);
            }
        } else {
            cache.put(xri, newObject);
        }
    }

    
    //------------------------------------------------------------------------
    // Class CachingInteraction
    //------------------------------------------------------------------------
    
    /**
     * Caching Interaction
     */
    protected class CachingInteraction 
        extends AbstractRestInteraction 
        implements CacheAccessor_2_0 
    {

        /**
         * Constructor 
         *
         * @param connection
         * @param cache
         */
        protected CachingInteraction(
            Connection connection,
            ManagedConnectionCache_2_0 cache
        ) {
            super(connection);
            this.cache = cache;
        }

        /**
         * The cache
         */
        protected final ManagedConnectionCache_2_0 cache;

        /**
         * Retrieve the managed connection cache
         * 
         * @return the managed connection cache
         */
    //  @Override
        public ManagedConnectionCache_2_0 getManagedConnectionCache(){
            return this.cache;
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.CacheAccessor_2_0#getDataStoreCache()
         */
    //  @Override
        public DataStoreCache_2_0 getDataStoreCache(
        ) throws ServiceException {
            return BasicCache_2.this;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#get(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean get(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            MappedRecord reply = this.cache.peek(input.getPath());
            if(reply == null) {
                return false;
            } else {
                output.add(reply);
                return true;
            }
        }
        
    }
    

    //------------------------------------------------------------------------
    // Class BasicConnectionCache
    //------------------------------------------------------------------------
    
    /**
     * Basic Connection Cache
     */
    protected class BasicConnectionCache implements ManagedConnectionCache_2_0 {

        /**
         * Constructor 
         * 
         * @throws ServiceException 
         */
        protected BasicConnectionCache(
            String userName
        ) throws ResourceException {
            this.userName = userName;
            this.touch();
        }

        /**
         * The user name
         */
        protected final String userName;
        
        /**
         * The caches expiration time
         */
        protected long expiration;
        
        /**
         * Evictable and non-evictable objects which have been sown.
         */
        private volatile Map<Path,ObjectRecord> sownById = null;
        
        /**
         * 
         */
        void touch(
        ){
            this.expiration = BasicCache_2.this.timeToLive > 0 ?
                System.currentTimeMillis() + BasicCache_2.this.timeToLive * 1000L :
                Long.MAX_VALUE;
        }
        
        /**
         * Tells whether the cache is expired
         * 
         * @return <code>true</code> if the cache is expired
         */
        boolean isExpired(){
            return System.currentTimeMillis() > this.expiration;
        }
        
    //  @Override
        public ObjectRecord peek(
            Path oid
        ) throws ServiceException { 
            if(this.sownById != null) {
                ObjectRecord object = this.sownById.get(oid); 
                if(object != null) {
                    SysLog.log(Level.FINEST, "Got {0} from BASIC cache", oid);
                    return object;
                }
            }            
            try {
                for(Map.Entry<Path,String> entry : BasicCache_2.this.sownByPattern.entrySet()) {
                    if(oid.isLike(entry.getKey())) {
                        return Object_2Facade.newInstance(oid, entry.getValue()).getDelegate();
                    }
                }
            } catch (ResourceException exception) {
                throw new ServiceException(exception);
            }
            return null;
        }
        
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.Cache_2_0#evict(org.openmdx.base.naming.Path)
         */
        void evict(
            Path oid
        ) throws ServiceException { 
            if(this.sownById != null) {
                this.sownById.remove(oid);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.Cache_2_0#evictAll()
         */
        void evictAll(
        ) throws ServiceException {
            if(this.sownById != null) {
                this.sownById.clear();
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.Cache_2_0#evictAll(org.openmdx.base.naming.Path)
         */
        void evictAll(
            Path oidPattern
        ) throws ServiceException { 
            if(this.sownById != null) {
                this.evictAll(this.sownById.values(), oidPattern, false, null);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.Cache_2_0#evictAll(java.util.Collection)
         */
        void evictAll(
            Collection<Path> oids
        ) throws ServiceException { 
            if(this.sownById != null) {
                this.sownById.keySet().removeAll(oids);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.Cache_2_0#evictAll(boolean, java.lang.String)
         */
        void evictAll(
            boolean subclasses, 
            String pcClass
        ) throws ServiceException { 
            if(this.sownById != null) {
                this.evictAll(this.sownById.values(), null, subclasses, pcClass);
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.Cache_2_0#isLCached(org.openmdx.base.naming.Path)
         */
    //  @Override
        public boolean isAvailable(
            Mode mode, 
            Path xri
        ) throws ServiceException {
            if(mode == null || mode == Mode.BASIC) {
                if(this.sownById != null && this.sownById.containsKey(xri)) {
                    return true;
                }
                for(Path pattern : BasicCache_2.this.sownByPattern.keySet()) {
                    if(xri.isLike(pattern)) {
                        return true;
                    }
                }
                return false;
            }
            return false;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.Cache_2_0#put(org.openmdx.base.naming.Path, javax.resource.cci.MappedRecord)
         */
    //  @Override
        public boolean put(
            Mode mode, 
            ObjectRecord object
        ) throws ServiceException {
            if(mode == Mode.BASIC) {
                if(this.sownById == null) synchronized(this) {
                    if(this.sownById == null) {
                        this.sownById = new ConcurrentHashMap<Path, ObjectRecord>();
                    }
                }
                Path xri = object.getPath();
                loggers[0].onPut(xri);
                this.sownById.put(xri, object);
                return true;
            } else {
                return false;
            }
        }

        /**
         * Evict matching objects
         * 
         * @param cache
         * @param oidPattern
         * @param subclasses
         * @param pcClass
         */
        protected void evictAll(
            Collection<ObjectRecord> cache,
            Path oidPattern,
            boolean subclasses, 
            String pcClass
        ){
            for(
                Iterator<ObjectRecord> i = cache.iterator();
                i.hasNext();
            ){
                ObjectRecord candidate = i.next();  
                boolean evict = true;
                if(oidPattern != null) {
                    evict &= candidate.getPath().isLike(oidPattern);
                }
                if(pcClass != null){
                    String candidateClass = candidate.getRecordName();
                    evict &= subclasses ? BasicCache_2.isSubtypeOf(candidateClass, pcClass) : candidateClass.equals(pcClass);
                }
                if(evict) {
                    loggers[0].onEvict(candidate.getPath());
                    i.remove();
                }
            }
        }
        
    }

}
