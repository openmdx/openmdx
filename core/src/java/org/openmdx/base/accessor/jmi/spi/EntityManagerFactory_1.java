/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Entity Manager Factory
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2013, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.spi;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;

import javax.jdo.Constants;
import javax.jdo.JDODataStoreException;
import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.datastore.DataStoreCache;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.spi.PropertiesConfigurationProvider;
import org.openmdx.base.accessor.rest.spi.ConnectionCacheProvider_2_0;
import org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0;
import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.accessor.view.ViewManagerFactory_1;
import org.openmdx.base.aop1.PlugIn_1;
import org.openmdx.base.aop1.PlugIn_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.ConfigurableProperty;
import org.openmdx.base.persistence.spi.AbstractPersistenceManagerFactory;
import org.openmdx.kernel.configuration.PropertiesProvider;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.BasicException.Parameter;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.kernel.loading.BeanFactory;
import org.openmdx.kernel.loading.Factory;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.SparseArray;


/**
 * Entity Manager Factory
 * <p>
 * The <code>PersistenceManagerFactory</code> it delegates to can be specified
 * in the following ways:<ol>
 * <li>as <code>org.openmdx.jdo.PersistenceManagerFactory</code> property if
 * the client has set up the persistence manager factory on its own
 * <li>as <code>org.openmdx.jdo.PersistenceManagerFactoryName</code> property for 
 * an embedded persistence manager
 * <li>as <code>connection-url</code> attribute for an extra-VM connection 
 * <li>as <code>connection-factory-name</code> and 
 * <code>connection-factory2-name</code> attributes for JNDI registered 
 * objects, such as<ul>
 * <li>JCA <code>ConnectionFactory</code> for a RESTful extra-VM connection  
 * <li><code>EJBHome</code> or <code>EJBLocalHome</code> for RESTful 
 * intra-VM connections
 * </ul>
 * </ol>
 */
@SuppressWarnings("rawtypes")
public class EntityManagerFactory_1
    extends AbstractPersistenceManagerFactory<PersistenceManager_1_0> {

    /**
     * Constructor 
     *
     * @param configuration
     */
    protected EntityManagerFactory_1(
        Map<?,?> overrides,
        Map<?,?> configuration
    ) {
        super(configuration);
        this.overrides = overrides;
        //
        // Persistence Manager Factory Configuration
        //
        PersistenceManagerFactory dataManagerFactory = (PersistenceManagerFactory) super.getConnectionFactory();
        if(dataManagerFactory == null) {
            String dataManagerFactoryName = super.getConnectionFactoryName();
            if(dataManagerFactoryName != null) try {
                if(dataManagerFactoryName.startsWith("jdo:")) {
                    dataManagerFactory = ReducedJDOHelper.getPersistenceManagerFactory(
                        this.overrides,
                        dataManagerFactoryName.substring(4)
                    );
                } else {
                    dataManagerFactory = (PersistenceManagerFactory) getConnectionFactoryByName(
                        dataManagerFactoryName
                    );
                }
            } catch (Exception exception) {
                throw BasicException.initHolder(
                    new JDODataStoreException(
                        "The entity manager factory could not acquire its persistence manager factory",
                        BasicException.newEmbeddedExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.MEDIA_ACCESS_FAILURE,
                            new BasicException.Parameter(
                                ConfigurableProperty.Name.qualifiedName(), 
                                getName()
                            ),
                            new BasicException.Parameter(
                                ConfigurableProperty.ConnectionFactoryName.qualifiedName(), 
                                dataManagerFactoryName
                            )
                        )
                    )
                );
            }
        }
        if(dataManagerFactory == null) {
            String url = super.getConnectionURL();
            if(url != null) {
                throw BasicException.initHolder(
                    new JDODataStoreException(
                        "NOT YET IMPLEMENTED: The entity manager factory could not acquire its persistence manager factory",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_IMPLEMENTED,
                            new BasicException.Parameter(
                                ConfigurableProperty.Name.qualifiedName(), 
                                getName()
                            ),
                            new BasicException.Parameter(
                                ConfigurableProperty.ConnectionURL.qualifiedName(), 
                                url
                            )
                        )
                    )
                );
            }
        }
        if(dataManagerFactory == null) {
            throw BasicException.initHolder(
                new JDODataStoreException(
                    "There was no data manager factory configured for this entity manager factory",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION
                    ),
                    new BasicException.Parameter(
                        ConfigurableProperty.Name.qualifiedName(), 
                        getName()
                    )
                )
            );
        } else {
            this.dataStoreCache = dataManagerFactory instanceof ConnectionCacheProvider_2_0 ?
                ((ConnectionCacheProvider_2_0)dataManagerFactory).getConnectionCache() :
                null;
        }
        //
        // Plug-In Configurations
        //
        try {
            Properties properties = PropertiesProvider.toProperties(configuration);
            Configuration viewManagerConfiguration = PropertiesConfigurationProvider.getConfiguration(
                properties,
                "org", "openmdx", "jdo", "ViewManager"
            );
            List<PlugIn_1_0> viewManagerPlugIns = new ArrayList<>();
            for(
                ListIterator<?> i = viewManagerConfiguration.values(
                    "plugIn"
                ).populationIterator();
                i.hasNext();
            ) {
                Configuration viewPlugInConfiguration = PropertiesConfigurationProvider.getConfiguration(
                    properties,
                    toSection(i.next())
                );  
                Factory<PlugIn_1_0> viewPlugInFactory = BeanFactory.newInstance(
                	PlugIn_1_0.class,
                    viewPlugInConfiguration
                );
                viewManagerPlugIns.add(viewPlugInFactory.instantiate());
            }
            PersistenceManagerFactory layerManagerFactory = viewManagerPlugIns.isEmpty() ? new ViewManagerFactory_1(
                dataManagerFactory,
                new PlugIn_1()
            ) : new ViewManagerFactory_1(
                dataManagerFactory,
                viewManagerPlugIns.toArray(new PlugIn_1_0[viewManagerPlugIns.size()])
            );
            Configuration entityManagerConfiguration = PropertiesConfigurationProvider.getConfiguration(
                properties,
                "org", "openmdx", "jdo", "EntityManager"
            );
            for(
                ListIterator<?> i = entityManagerConfiguration.values(
                    "userObject"
                ).populationIterator();
                i.hasNext();
            ) {
                getUserObject(
                    properties,
                    toSection(i.next()),
                    this.userObjects
                );
            }
            SparseArray<?> plugIns = entityManagerConfiguration.values(
                "plugIn"
            );
            if(!plugIns.isEmpty()) {
                layerManagerFactory = new LayerManagerFactory_2(
                    layerManagerFactory,
                    getPlugInConfiguration(
                        properties,
                        null,
                        this.userObjects
                    )
                );
                // User-configured layers
                for(
                    ListIterator<?> i = plugIns.populationIterator();
                    i.hasNext();
                ){
                    layerManagerFactory = new LayerManagerFactory_2(
                        layerManagerFactory,
                        getPlugInConfiguration(
                            properties,
                            toSection(i.next()),
                            this.userObjects
                        )
                    );
                }
            }
            this.mapping = LayerManagerFactory_2.newPlugInMapping(
                this.delegate = layerManagerFactory, 
                null
            );
        } catch (Exception exception) {
            throw BasicException.initHolder(
                new JDOFatalDataStoreException(
                    "JMI object manager factory configuration retrieval failure",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION
                    )
                )
            );
        }
    }

    /**
     * Provide a configuration entry's section
     * 
     * @param name the configuration entry
     * 
     * @return the configuration entry's section
     */
    private static String[] toSection(
        Object name
    ){
        return ((String)name).split("\\.");
    }

    /**
     * The shared user objects
     */
    private final Map<String,Object> userObjects = new HashMap<>();
    
    /**
     * The top level layer manager factory
     */
    private final PersistenceManagerFactory delegate;
    
    /**
     * The entity manager factory's mapping
     */
    private final Mapping_1_0 mapping;
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -3043695082264242663L;

    /**
     * The default configuration
     */
    protected static final Map<String, Object> DEFAULT_CONFIGURATION = new HashMap<>(
        AbstractPersistenceManagerFactory.DEFAULT_CONFIGURATION
    );

    // Configuration overrides propagated to persistence manager
    private final Map<?,?> overrides;

    /**
     * 
     */
    private final DataStoreCache_2_0 dataStoreCache;
    
    static {
        EntityManagerFactory_1.DEFAULT_CONFIGURATION.put(
            ConfigurableProperty.TransactionType.qualifiedName(),
            Constants.RESOURCE_LOCAL
        );
    }
    
    /**
     * The method is used by JDOHelper to construct an instance of 
     * <code>PersistenceManagerFactory</code> based on user-specified 
     * properties.
     * 
     * @param props
     * 
     * @return a new <code>PersistenceManagerFactory</code>
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory (
        Map props
    ){
        return getPersistenceManagerFactory(
            Collections.EMPTY_MAP,
            props
        );
    }

    /**
     * Retrieve an entity manager factory specified by the given properties
     *
     * @param overrides
     * @param props
     * 
     * @return a new entity manager factory
     */
    @SuppressWarnings("unchecked")
    public static PersistenceManagerFactory getPersistenceManagerFactory (
        Map overrides, 
        Map props
    ){
        Map<Object,Object> configuration = new HashMap<>(DEFAULT_CONFIGURATION);
        configuration.putAll(props);
        try {
            String entityManagerName = (String)props.get(ConfigurableProperty.Name.qualifiedName());
            if(entityManagerName != null) {
                for(URL resource : Resources.getMetaInfResources(entityManagerName + ".properties")) {
                    Properties properties = new Properties();
                    properties.load(resource.openStream());
                    configuration.putAll(properties);
                }
            }
        } catch(Exception exception) {
            SysLog.warning("Unable to retrieve the entity manager configuration", exception);
        }
        configuration.putAll(overrides);
        return new EntityManagerFactory_1(overrides, configuration);
    }

    /**
     * Create and initialize an entity manager
     * 
     * @param delegate the entity manager's delegate
     * 
     * @return an new entity manager
     */
    private PersistenceManager_1_0 newEntityManager(
        PersistenceManager delegate
    ){
        return new RefRootPackage_1(
          this, // persistenceManagerFactory
          delegate,
          this.mapping,
          this.userObjects
        ).refPersistenceManager(
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.AbstractPersistenceManagerFactory#newPersistenceManager(java.lang.String, java.lang.String)
     */
    @Override
    protected PersistenceManager_1_0 newPersistenceManager(
        String userid,
        String password
    ) {
        return newEntityManager(
            this.delegate.getPersistenceManager(
                userid,
                password
            )
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.AbstractPersistenceManagerFactory#newPersistenceManager()
     */
    @Override
    protected PersistenceManager_1_0 newPersistenceManager(
    ) {
        return newEntityManager(
            this.delegate.getPersistenceManager()
        );
    }

    /**
     * Retrieve a single user object
     * 
     * @param configurationProvider
     * @param userObjects
     * @param section
     * 
     * @throws ServiceException
     */
    private static void getUserObject(
        Properties properties,
        String[] section,
        Map<String,Object> userObjects
    ) throws ServiceException {
        try {
            Configuration userObjectConfiguration = PropertiesConfigurationProvider.getConfiguration(
                properties,
                section
            );  
            Factory<?> userObjectFactory = BeanFactory.newInstance(
                userObjectConfiguration
            );
            final boolean shared = !userObjectFactory.getInstanceClass().isInterface();
            userObjects.put(
                section[section.length - 1],
                shared ? userObjectFactory.instantiate() : userObjectFactory
            );
        } catch (Exception exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                "User object initialization failure",
                new Parameter("section",(Object[])section)
            );
        } 
     }

    /**
     * Prepare a plug-in's configuration according to the configuration
     * provider's entries.
     * 
     * @param configurationProvider
     * @param section the plug-in's section name
     * @param sharedUserObjects
     * 
     * @return the plug-in configuration
     * 
     * @throws ServiceException 
     */
    private static Configuration getPlugInConfiguration(
        Properties properties,
        String[] section,
        Map<String,Object> sharedUserObjects
    ) throws ServiceException{
        Configuration plugInConfiguration = PropertiesConfigurationProvider.getConfiguration(
            properties,
            section
        );
        //
        // User Objects
        //
        Map<String,Object> userObjects = new HashMap<>(sharedUserObjects);
        plugInConfiguration.values(
            "userObjects"
        ).put(
            Integer.valueOf(0),
            userObjects
        );
        for(
            ListIterator<?> j = plugInConfiguration.values(
                "userObject"
            ).populationIterator();
            j.hasNext();
        ){
            getUserObject(
                properties,
                toSection(j.next()),
                userObjects
            );
        }
        //
        // Model Mapping
        // 
        Map<String,String> implementationMap = new HashMap<>();
        plugInConfiguration.values(
            "implementationMap"
        ).put(
            Integer.valueOf(0),
            implementationMap
        );
        SparseArray<String> modelPackages = plugInConfiguration.values(
            "modelPackage"
        );
        for(
            ListIterator<?> j = plugInConfiguration.values(
                "packageImpl"
            ).populationIterator();
            j.hasNext();
        ){
            implementationMap.put(
                modelPackages.get(Integer.valueOf(j.nextIndex())),
                j.next().toString()
            );
        }
        return plugInConfiguration;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.AbstractPersistenceManagerFactory#newDataStoreCache()
     */
    @Override
    protected DataStoreCache newDataStoreCache() {
        return this.dataStoreCache == null ? new DataStoreCache.EmptyDataStoreCache(
        ) : new CacheAdapter(
            this.dataStoreCache, 
            this.mapping
        );
    }

    

    //------------------------------------------------------------------------
    // Class CacheAdapter
    //------------------------------------------------------------------------

    /**
     * Cache Adapter
     */
    class CacheAdapter implements DataStoreCache {

        /**
         * Constructor 
         *
         * @param delegate
         */
        CacheAdapter(
            DataStoreCache_2_0 delegate,
            Mapping_1_0 mapping
        ) {
            this.delegate = delegate;
            this.mapping = mapping;
        }

        /**
         * The datastore cache REST adapter 
         */
        private final DataStoreCache_2_0 delegate;
        
        /**
         * 
         */
        private final Mapping_1_0 mapping;
        
        /* (non-Javadoc)
         * @see javax.jdo.datastore.DataStoreCache#evict(java.lang.Object)
         */
    //  @Override
        public void evict(Object oid) {
            if(oid instanceof Path) try {
                this.delegate.evict((Path) oid);
            } catch (ServiceException exception) {
                throw new JDOUserException("DataStoreCache access failure", exception);
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.datastore.DataStoreCache#evictAll()
         */
    //  @Override
        public void evictAll(
        ) {
            try {
                this.delegate.evictAll();
            } catch (ServiceException exception) {
                throw new JDOUserException("DataStoreCache access failure", exception);
            }
       }

        /* (non-Javadoc)
         * @see javax.jdo.datastore.DataStoreCache#evictAll(java.lang.Object[])
         */
    //  @Override
        public void evictAll(Object... oids) {
            for(Object oid : oids){
                evict(oid);
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.datastore.DataStoreCache#evictAll(java.util.Collection)
         */
        @SuppressWarnings("unchecked")
    //  @Override
        public void evictAll(Collection oids) {
            try {
                this.delegate.evictAll(oids);
            } catch (ServiceException exception) {
                throw new JDOUserException("DataStoreCache access failure", exception);
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.datastore.DataStoreCache#evictAll(java.lang.Class, boolean)
         */
        public void evictAll(Class pcClass, boolean subclasses) {
            evictAll(subclasses, pcClass);
        }

        /* (non-Javadoc)
         * @see javax.jdo.datastore.DataStoreCache#evictAll(boolean, java.lang.Class)
         */
        public void evictAll(boolean subclasses, Class pcClass) {
            try {
                this.delegate.evictAll(subclasses, this.mapping.getModelClassName(pcClass));
            } catch (ServiceException exception) {
                throw new JDOUserException("DataStoreCache access failure", exception);
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.datastore.DataStoreCache#pin(java.lang.Object)
         */
    //  @Override
        public void pin(Object oid) {
            if(oid instanceof Path) try {
                this.delegate.pin((Path) oid);
            } catch (ServiceException exception) {
                throw new JDOUserException("DataStoreCache access failure", exception);
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.datastore.DataStoreCache#pinAll(java.util.Collection)
         */
        @SuppressWarnings("unchecked")
    //  @Override
        public void pinAll(Collection oids) {
            try {
                this.delegate.pinAll(oids);
            } catch (ServiceException exception) {
                throw new JDOUserException("DataStoreCache access failure", exception);
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.datastore.DataStoreCache#pinAll(java.lang.Object[])
         */
    //  @Override
        public void pinAll(Object... oids) {
            for(Object oid : oids){
                pin(oid);
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.datastore.DataStoreCache#pinAll(java.lang.Class, boolean)
         */
        public void pinAll(Class pcClass, boolean subclasses) {
            pinAll(subclasses, pcClass);
        }

        /* (non-Javadoc)
         * @see javax.jdo.datastore.DataStoreCache#pinAll(boolean, java.lang.Class)
         */
        public void pinAll(boolean subclasses, Class pcClass) {
            try {
                this.delegate.pinAll(subclasses, this.mapping.getModelClassName(pcClass));
            } catch (ServiceException exception) {
                throw new JDOUserException("DataStoreCache access failure", exception);
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.datastore.DataStoreCache#unpin(java.lang.Object)
         */
    //  @Override
        public void unpin(Object oid) {
            if(oid instanceof Path) try {
                this.delegate.unpin((Path) oid);
            } catch (ServiceException exception) {
                throw new JDOUserException("DataStoreCache access failure", exception);
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.datastore.DataStoreCache#unpinAll(java.util.Collection)
         */
        @SuppressWarnings("unchecked")
    //  @Override
        public void unpinAll(Collection oids) {
            try {
                this.delegate.unpinAll(oids);
            } catch (ServiceException exception) {
                throw new JDOUserException("DataStoreCache access failure", exception);
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.datastore.DataStoreCache#unpinAll(java.lang.Object[])
         */
    //  @Override
        public void unpinAll(Object... oids) {
            for(Object oid : oids){
                unpin(oid);
            }
        }

        /* (non-Javadoc)
         * @see javax.jdo.datastore.DataStoreCache#unpinAll(java.lang.Class, boolean)
         */
        public void unpinAll(Class pcClass, boolean subclasses) {
            unpinAll(subclasses, pcClass);
        }

        /* (non-Javadoc)
         * @see javax.jdo.datastore.DataStoreCache#unpinAll(boolean, java.lang.Class)
         */
        public void unpinAll(boolean subclasses, Class pcClass) {
            try {
                this.delegate.unpinAll(subclasses, this.mapping.getModelClassName(pcClass));
            } catch (ServiceException exception) {
                throw new JDOUserException("DataStoreCache access failure", exception);
            }
        }
        
    }

}
