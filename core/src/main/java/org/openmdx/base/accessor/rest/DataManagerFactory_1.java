/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Data Object Manager Factory
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
package org.openmdx.base.accessor.rest;

import static org.openmdx.application.dataprovider.cci.SharedConfigurationEntries.DATABASE_CONNECTION_FACTORY_NAME;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import javax.jdo.Constants;
import javax.jdo.JDODataStoreException;
import javax.jdo.JDOFatalDataStoreException;

import #if JAVA_8 javax.resource.ResourceException #else jakarta.resource.ResourceException #endif;

import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.rest.spi.Switch_2;
import org.openmdx.base.aop0.PlugIn_1_0;
import org.openmdx.base.aop0.UpdateAvoidance_1;
import org.openmdx.base.caching.datastore.CacheAdapter;
import org.openmdx.base.caching.datastore.NoSecondLevelCache;
import org.openmdx.base.caching.port.CachingPort;
import org.openmdx.base.caching.port.StandardCachingPort;
import org.openmdx.base.caching.virtualobjects.StandardVirtualObjects;
import org.openmdx.base.caching.virtualobjects.VirtualObjectProvider;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.AbstractPersistenceManagerFactory;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.base.rest.spi.RestConnectionFactory;
import org.openmdx.base.transaction.TransactionAttributeType;
import org.openmdx.kernel.configuration.cci.Configuration;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.JDOPersistenceManagerFactory;
import org.openmdx.kernel.loading.BeanFactory;
import org.w3c.cci2.SparseArray;

/**
 * Data Object Manager Factory
 */
public class DataManagerFactory_1
    extends AbstractPersistenceManagerFactory<DataObjectManager_1_0> {

    /**
     * Constructor
     *
     * @param overrides
     *            the configuration properties
     * @param configuration
     *            the configuration properties
     * @param defaults
     *            for missing configuration and override properties
     * @exception JDOFatalDataStoreException
     *                in case of failure
     */
    protected DataManagerFactory_1(
        Map<?, ?> overrides,
        Map<?, ?> configuration,
        Map<?, ?> defaults
    ) {
        super(overrides, configuration, defaults);
        try {
            if (getConnectionFactory() != null) {
                this.cacheAdapter = new EmptyCacheAdapter();
                this.optimalFetchSize = Optional.empty();
                this.cacheThreshold = Optional.empty();
                this.plugIns = DEFAULT_PLUG_INS;
            } else {
                final Configuration persistenceManagerConfiguration = getConfiguration(
                    "org.openmdx.jdo.DataManager"
                );
                this.optimalFetchSize = persistenceManagerConfiguration.getOptionalValue(
                    "optimalFetchSize",
                    Integer.class
                );
                this.cacheThreshold = persistenceManagerConfiguration.getOptionalValue(
                    "cacheThreshold",
                    Integer.class
                );
                this.plugIns = createPlugIns(persistenceManagerConfiguration);
                this.cacheAdapter = createCacheAdapter();
                configureConnectionFactories(persistenceManagerConfiguration);
            }
        } catch (Exception exception) {
            throw BasicException.initHolder(
                new JDOFatalDataStoreException(
                    "Data object manager factory configuration retrieval failure",
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
     * The default configuration
     */
    protected static final Map<String, Object> DEFAULT_CONFIGURATION = createDefaultConfiguration(Collections.emptyMap());

    /**
     * 
     */
    private static final PlugIn_1_0[] DEFAULT_PLUG_INS = {
        new UpdateAvoidance_1()
    };

    /**
     * Implements {@code Serializabel}
     */
    private static final long serialVersionUID = -8694584589690397280L;

    /**
     * The optimal fetch size
     */
    private final Optional<Integer> optimalFetchSize;

    /**
     * Collections smaller than this value are cached before being evaluated
     */
    private final Optional<Integer> cacheThreshold;

    /**
     * 
     */
    private final PlugIn_1_0[] plugIns;

    /**
     * The Data Store Cache Adapter
     */
    private final CacheAdapter cacheAdapter;

    private void configureConnectionFactories(
        final Configuration persistenceManagerConfiguration
    )
        throws ResourceException,
        ServiceException {
        final Map<Path, Port<RestConnection>> destinations = new LinkedHashMap<Path, Port<RestConnection>>();
        createConnectionFactories(createPort(destinations));
        configurePlugIns(persistenceManagerConfiguration, destinations);
    }

    private Port<RestConnection> createPort(final Map<Path, Port<RestConnection>> destinations)
        throws ResourceException,
        ServiceException {
        final Port<RestConnection> port = new Switch_2(
            createCachingPort(
                createVirtualObjectProvider(),
                this.cacheAdapter
            ),
            destinations
        );
        return port;
    }

    private void configurePlugIns(
        final Configuration persistenceManagerConfiguration,
        final Map<Path, Port<RestConnection>> destinations
    ) {
        final Map<String, Port<RestConnection>> raw = new LinkedHashMap<String, Port<RestConnection>>();
        final SparseArray<String> restPlugIns = persistenceManagerConfiguration.getSparseArray(
            "restPlugIn",
            String.class
        );
        for (ListIterator<String> i = persistenceManagerConfiguration.getSparseArray(
            "xriPattern",
            String.class
        ).populationIterator(); i.hasNext();) {
            final int index = i.nextIndex();
            final String pattern = i.next();
            final String restPlugIn = restPlugIns.get(Integer.valueOf(index));
            Port<RestConnection> destination = raw.get(restPlugIn);
            if (destination == null) {
                destination = createDestination(
                    getConfiguration(
                        createPlugInConfigurationDefaults(),
                        restPlugIn,
                        Collections.emptyMap()
                    )
                );
                raw.put(
                    restPlugIn,
                    destination
                );
            }
            destinations.put(
                new Path(pattern),
                destination
            );
        }
        DataManagerPreferencesPort.discloseConfiguration(
            destinations,
            raw
        );
    }

    private void createConnectionFactories(final Port<RestConnection> port) {
        final boolean supportsLocalTransaction = Constants.RESOURCE_LOCAL.equals(super.getTransactionType());
        setConnectionFactory(
            new RestConnectionFactory(
                port,
                supportsLocalTransaction,
                TransactionAttributeType.MANDATORY
            )
        );
        setConnectionFactory2(
            new RestConnectionFactory(
                port,
                supportsLocalTransaction,
                TransactionAttributeType.REQUIRES_NEW
            )
        );
    }

    /**
     * Provide the plug-in configuration default entries
     * 
     * @return the plug-in configuration default entries
     */
    private Map<String, ?> createPlugInConfigurationDefaults() {
        final String connectionFactoryName = super.getConnectionFactoryName();
        return connectionFactoryName == null ? Collections.emptyMap()
            : Collections.singletonMap(
                DATABASE_CONNECTION_FACTORY_NAME,
                connectionFactoryName
            );
    }

    @SuppressWarnings("unchecked")
    private Port<RestConnection> createDestination(Configuration plugInConfiguration) {
        return BeanFactory.newInstance(
            Port.class,
            plugInConfiguration
        ).instantiate();
    }

    /**
     * To create the plug-ins eagerly
     * 
     * @param properties
     * @param persistenceManagerConfiguration
     * 
     * @return the plug-ins
     * 
     * @throws ServiceException
     *             in case of failure
     */
    private PlugIn_1_0[] createPlugIns(
        Configuration persistenceManagerConfiguration
    )
        throws ServiceException {
        final SparseArray<String> plugInConfiguration = persistenceManagerConfiguration.getSparseArray(
            "plugIn",
            String.class
        );
        if (plugInConfiguration.isEmpty()) {
            return DEFAULT_PLUG_INS;
        }
        final PlugIn_1_0[] plugInStack = new PlugIn_1_0[plugInConfiguration.size()];
        for (int i = 0; i < plugInStack.length; i++) {
            final String section = plugInConfiguration.get(Integer.valueOf(i));
            plugInStack[i] = BeanFactory.newInstance(
                PlugIn_1_0.class,
                getConfiguration(section)
            ).instantiate();
        }
        return plugInStack;
    }

    /**
     * To create the virtual object provider eagerly
     * 
     * @return the newly created virtual object provider
     * 
     * @throws ServiceException
     *             in case of configuration failure
     */
    private VirtualObjectProvider createVirtualObjectProvider()
        throws ServiceException {
        return BeanFactory.newInstance(
            VirtualObjectProvider.class,
            getConfiguration("org.openmdx.base.caching.virtualobjects.VirtualObjectProvider"),
            StandardVirtualObjects.class
        ).instantiate();
    }

    /**
     * To create the caching port eagerly
     * 
     * @return the newly created caching port
     * 
     * @throws ServiceException
     *             in case of configuration failure
     */
    private CachingPort createCachingPort(
        VirtualObjectProvider virtualObjectProvider,
        CacheAdapter cacheAdapter
    )
        throws ServiceException {
        final CachingPort cachingPort = BeanFactory.newInstance(
            CachingPort.class,
            getConfiguration("org.openmdx.base.caching.port.CachingPort"),
            StandardCachingPort.class
        ).instantiate();
        cachingPort.setVirtualObjectProvider(virtualObjectProvider);
        cachingPort.setCacheAdapter(cacheAdapter);
        return cachingPort;
    }

    /**
     * To create the cache adapter eagerly
     * 
     * @return the newly created cache adapter
     * 
     * @throws ServiceException
     *             in case of configuration failure
     */
    private CacheAdapter createCacheAdapter()
        throws ServiceException {
        return BeanFactory.newInstance(
            CacheAdapter.class,
            getConfiguration("org.openmdx.base.caching.datastore.CacheAdapter"),
            NoSecondLevelCache.class
        ).instantiate();
    }

    /**
     * The method is used by JDOHelper to construct an instance of
     * {@code PersistenceManagerFactory} based on user-specified
     * properties.
     * 
     * @param props
     * 
     * @return a new {@code PersistenceManagerFactory}
     */
    @SuppressWarnings("rawtypes")
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory(
        Map props
    ) {
        return getPersistenceManagerFactory(
            Collections.EMPTY_MAP,
            props
        );
    }

    /**
     * The method is used by JDOHelper to construct an instance of
     * {@code PersistenceManagerFactory} based on user-specified
     * properties.
     * 
     * @param overrides
     * @param props
     * 
     * @return a new {@code PersistenceManagerFactory}
     */
    @SuppressWarnings({ "rawtypes" })
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory(
        Map overrides,
        Map props
    ) {
        return new DataManagerFactory_1(overrides, props, DEFAULT_CONFIGURATION);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1#newPersistenceManager(java.lang.String, java.lang.String)
     */
    @Override
    protected DataObjectManager_1_0 newPersistenceManager(
        String userid,
        String password
    ) {
        try {
            return new DataObjectManager_1(
                this,
                false,
                this.plugIns,
                this.optimalFetchSize,
                this.cacheThreshold,
                getIsolateThreads(),
                new RestConnectionSpec(
                    userid,
                    password
                )
            );
        } catch (ResourceException exception) {
            throw BasicException.initHolder(
                new JDODataStoreException(
                    "The dataobject manager is unable to establish REST connection",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.MEDIA_ACCESS_FAILURE
                    )
                )
            );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1#newPersistenceManager(java.lang.String, java.lang.String)
     */
    @Override
    protected DataObjectManager_1_0 newPersistenceManager() {
        return newPersistenceManager(
            System.getProperty("user.name"),
            null
        );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.kernel.jdo.JDOPersistenceManagerFactory#getDataStoreCache()
     */
    @Override
    public CacheAdapter getDataStoreCache() {
        return this.cacheAdapter;
    }

}
