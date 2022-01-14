/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Entity Manager Proxy Factory
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
package org.openmdx.base.rest.connector;

import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import javax.jdo.Constants;
import javax.jdo.JDODataStoreException;
import javax.jdo.JDOFatalDataStoreException;
import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
import javax.resource.spi.ResourceAllocationException;

import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.rest.DataObjectManager_1;
import org.openmdx.base.accessor.rest.spi.Switch_2;
import org.openmdx.base.aop0.PlugIn_1_0;
import org.openmdx.base.caching.datastore.CacheAdapter;
import org.openmdx.base.caching.datastore.NoSecondLevelCache;
import org.openmdx.base.caching.port.StandardCachingPort;
import org.openmdx.base.caching.virtualobjects.StandardVirtualObjects;
import org.openmdx.base.caching.virtualobjects.VirtualObjectProvider;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.ConfigurableProperty;
import org.openmdx.base.persistence.spi.AbstractPersistenceManagerFactory;
import org.openmdx.base.resource.cci.ConnectionFactory;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.base.rest.spi.ConnectionAdapter;
import org.openmdx.base.rest.spi.RestConnectionFactory;
import org.openmdx.base.transaction.TransactionAttributeType;
import org.openmdx.kernel.configuration.Configurations;
import org.openmdx.kernel.configuration.cci.Configuration;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.EmptyJDODataStoreCache;
import org.openmdx.kernel.jdo.JDODataStoreCache;
import org.openmdx.kernel.jdo.JDOPersistenceManagerFactory;
import org.openmdx.kernel.loading.BeanFactory;
import org.openmdx.kernel.loading.Factory;
import org.w3c.cci2.SparseArray;

/**
 * Entity Manager Proxy Factory
 */
public class EntityManagerProxyFactory_2 extends AbstractPersistenceManagerFactory<DataObjectManager_1_0> {

    /**
     * Constructor
     *
     * @param overrides the configuration properties
     * @param configuration the configuration properties
     * @param defaults for missing configuration and override properties
     */
    protected EntityManagerProxyFactory_2(
        Map<?, ?> overrides,
        Map<?, ?> configuration,
        Map<?, ?> defaults
    ) {
        super(overrides, configuration, defaults);
        final Configuration dataManagerConfiguration = getConfiguration(
            "org.openmdx.jdo.DataManager"
        );
        this.optimalFetchSize = dataManagerConfiguration.getOptionalValue(
            "optimalFetchSize",
            Integer.class
        );
        this.cacheThreshold = dataManagerConfiguration.getOptionalValue(
            "cacheThreshold",
            Integer.class
        );
        SparseArray<String> plugIns = dataManagerConfiguration.getSparseArray(
            "plugIn",
            String.class
        );
        if(plugIns.isEmpty()) {
            this.plugIns = DEFAULT_PLUG_INS;
        } else {
            this.plugIns = new PlugIn_1_0[plugIns.size()];
            final ListIterator<String> p = plugIns.populationIterator();
            for(
                int i = 0;
                i < this.plugIns.length;
                i++
            ){
                this.plugIns[i] = BeanFactory.newInstance(
                	PlugIn_1_0.class,
                    getConfiguration(p.next())
                ).instantiate();
            }
        }
        //
        // Connection Factory
        // 
        try {
            Object connectionFactory = super.getConnectionFactory();
            if(connectionFactory == null) {
                String connectionFactoryName = super.getConnectionFactoryName();
                if(connectionFactoryName == null) { 
                    String connectionURL = super.getConnectionURL();
                    if(connectionURL == null) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.INVALID_CONFIGURATION,
                            "Neither connection factory nor connection factory name nor connection URL have been specified",
                            new BasicException.Parameter(
                                "expected",
                                ConfigurableProperty.ConnectionFactory.qualifiedName(), 
                                ConfigurableProperty.ConnectionFactoryName.qualifiedName(),
                                ConfigurableProperty.ConnectionURL.qualifiedName()
                             )
                        );
                    } else {
                        connectionFactory = getConnectionFactoryByURL(configuration, connectionURL);
                    }
                } else {
                    connectionFactory = getConnectionFactoryByName(connectionFactoryName);
                }
            }
            this.destinations = Collections.singletonMap(
                PROXY_PATTERN,
                newPort(connectionFactory)
            );
        } catch (ServiceException exception) {
            throw BasicException.initHolder(
                new JDOFatalDataStoreException(
                    "Data manager proxy factory set up failure",
                    BasicException.newEmbeddedExceptionStack(exception)
                )
            );
        }
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 6853221646431074355L;

    /**
     * The aop0 plug-ins
     */
    private final PlugIn_1_0[] plugIns;

    /**
     * The optimal fetch size
     */
    private final Optional<Integer> optimalFetchSize;

    /**
     * Collections smaller than this value are cached before being evaluated
     */
    private final Optional<Integer> cacheThreshold;
    
    /**
     * The destinations
     */
    private final Map<Path,Port<RestConnection>> destinations;

    /**
     * The Data Store Cache
     */
    private final JDODataStoreCache dataStoreCache = createDataStoreCache();
    
    /**
     * The standard plug-ins
     */
    private static final PlugIn_1_0[] DEFAULT_PLUG_INS = {
        new ProxyPlugIn_1()
    };
    
    /**
     * Catch all proxied objects
     */
    private static final Path PROXY_PATTERN = new Path("xri://@openmdx*($...)");

    /**
     * The default configuration
     */
    protected static final Map<String, Object> DEFAULT_CONFIGURATION = createDefaultConfiguration(
        Collections.singletonMap(
            ConfigurableProperty.TransactionType.qualifiedName(),
            Constants.RESOURCE_LOCAL
        )
    );

    /**
     * The connection driver configuration section
     */
    private static final String CONNECTION_DRIVER_CONFIGURATION_ENTRY = "org.openmdx.jdo.ConnectionDriver";
    
    /**
     * Acquire the connection factory by its URL and driver name
     * 
     * @param configuration
     * @param connectionURL
     * 
     * @return the connection factory
     * 
     * @throws ServiceException
     */
    protected Object getConnectionFactoryByURL(
        Map<?,?> configuration,
        String connectionURL
    ) throws ServiceException{
        String connectionDriverName = super.getConnectionDriverName();
        if(connectionDriverName == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Together with a connection URL you have to specify a connection driver",
                new BasicException.Parameter(
                    "connection-url-property", 
                    ConfigurableProperty.ConnectionURL.qualifiedName()
                ),
                new BasicException.Parameter(
                    "required-connection-driver-properties", 
                    ConfigurableProperty.ConnectionDriverName.qualifiedName()
                ),
                new BasicException.Parameter(
                    "optional-connection-driver-properties", 
                    ConfigurableProperty.ConnectionUserName.qualifiedName(), 
                    ConfigurableProperty.ConnectionPassword.qualifiedName()
                ),
                new BasicException.Parameter(
                    "connection-driver-interface", 
                    Port.class.getName()
                )
            );
        } else {
            final Map<String, String> overrides = new HashMap<>();
            overrides.put(
                CONNECTION_DRIVER_CONFIGURATION_ENTRY + ".class",
                connectionDriverName
            );
            //
            // Standard Properties
            //
            overrides.put(
                CONNECTION_DRIVER_CONFIGURATION_ENTRY + ".ConnectionURL",
                connectionURL
            );
            String userName = super.getConnectionUserName();
            if(userName != null) {
                overrides.put(
                    CONNECTION_DRIVER_CONFIGURATION_ENTRY + ".UserName",
                    userName
                );
            }
            String password = super.getConnectionPassword();
            if(password != null) {
                overrides.put(
                    CONNECTION_DRIVER_CONFIGURATION_ENTRY + ".Password",
                    password
                );
            }
            final Configuration connectionDriverConfiguration = Configurations.getConnectionDriverConfiguration(
                overrides, 
                configuration, 
                CONNECTION_DRIVER_CONFIGURATION_ENTRY
            );
            return BeanFactory.newInstance(
                connectionDriverConfiguration
            );
        }
    }
    
    /**
     * Create a new Port
     * 
     * @param connectionFactory
     * 
     * @return a new <code>Port</code> for the given connection factory
     * 
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
	protected Port<RestConnection> newPort(
        final Object connectionFactory
    ) throws ServiceException{
        if(connectionFactory instanceof ConnectionFactory) {
            return new Port<RestConnection>(){
                
                /* (non-Javadoc)
                 * @see org.openmdx.base.resource.spi.Port#getInteraction(javax.resource.cci.Connection)
                 */
                public Interaction getInteraction(
                    RestConnection connection
                ) throws ResourceException {
                    if(connection instanceof ConnectionAdapter) {
                        return ((ConnectionFactory)connectionFactory).getConnection(
                            ((ConnectionAdapter)connection).getMetaData().getConnectionSpec()
                        ).createInteraction(
                        );
                    }
                    throw ResourceExceptions.initHolder(
                        new ResourceAllocationException(
                            "Connection can't be established",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_PARAMETER,
                                new BasicException.Parameter("expected", ConnectionAdapter.class.getName()),
                                new BasicException.Parameter("actual", connection == null ? null : connection.getClass().getName())
                            )
                        )
                    );
                }
                
            };
        } else if(connectionFactory instanceof Factory<?>) {
            Factory<?> portFactory = (Factory<?>)connectionFactory; 
            Object port = portFactory.instantiate();
            if(port instanceof Port<?>) {
                return (Port<RestConnection>) port;
            } else {
                throw new ServiceException(
                    new ResourceAllocationException(
                        "Inapporopriate connection driver, can't create port",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter(
                                "expected", 
                                Factory.class.getName() + "<" + Port.class.getName() + ">"
                            ),
                            new BasicException.Parameter(
                                "actual", 
                                Factory.class.getName() + "<" + (port == null ? null : port.getClass().getName()) + ">"
                            )
                        )
                    )
                );
            }
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                "Inapporopriate connection factory, can't create port",
                new BasicException.Parameter(
                    "actual", 
                    connectionFactory == null ? null : connectionFactory.getClass().getName()
                )
            );
        }
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
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory (
        Map<?,?> props
    ){
        return getPersistenceManagerFactory(
            Collections.EMPTY_MAP,
            props
        );
    }

    /**
     * The method is used by JDOHelper to construct an instance of 
     * <code>PersistenceManagerFactory</code> based on user-specified 
     * properties.
     * 
     * @param overrides
     * @param props
     * 
     * @return a new <code>PersistenceManagerFactory</code>
     */
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory (
        Map<?,?> overrides, 
        Map<?,?> props
    ){
        return new EntityManagerProxyFactory_2(overrides, props, DEFAULT_CONFIGURATION);
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.jdo.JDOPersistenceManagerFactory#getDataStoreCache()
     */
    @Override
    public JDODataStoreCache getDataStoreCache() {
        return dataStoreCache;
    }

    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1#newPersistenceManager(java.lang.String, java.lang.String)
     */
    @Override
    protected DataObjectManager_1_0 newPersistenceManager(
        String userid,
        String password
    ) {
        try {
            final RestConnectionSpec connectionSpec = new RestConnectionSpec(userid, password);
            return new DataObjectManager_1(
                this,
                true, // proxy
                this.plugIns,
                this.optimalFetchSize, 
                this.cacheThreshold, 
                getIsolateThreads(), 
                connectionSpec  
            );
        } catch (ResourceException exception) {
            throw BasicException.initHolder(
                new JDODataStoreException(
                    "The data object manager proxy factory is unable to establish connection(s)",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.MEDIA_ACCESS_FAILURE
                    )
                )
            );
        }
    }

    protected RestConnectionFactory createConnectionFactory(
    ) throws ResourceException {
        return new RestConnectionFactory(
        	createPort(),
        	true, // supportsLocalTransactionDemarcation
        	TransactionAttributeType.SUPPORTS
        );
    }

    protected Switch_2 createPort(
    ) throws ResourceException {
        return new Switch_2(
            createCachingPort(), 
            this.destinations
        );
    }

    protected StandardCachingPort createCachingPort() {
        final StandardCachingPort cachingPort = new StandardCachingPort();
        cachingPort.setVirtualObjectProvider(createVirtualObjectProvider());
        cachingPort.setCacheAdapter(createCacheAdapter());
        return cachingPort;
    }

    protected CacheAdapter createCacheAdapter() {
        return new NoSecondLevelCache();
    }

    protected VirtualObjectProvider createVirtualObjectProvider() {
        return new StandardVirtualObjects();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1#newPersistenceManager(java.lang.String, java.lang.String)
     */
    @Override
    protected DataObjectManager_1_0 newPersistenceManager(
    ) {
        return newPersistenceManager(
            System.getProperty("user.name"), 
            null
        );
    }

    protected JDODataStoreCache createDataStoreCache() {
        return new EmptyJDODataStoreCache();
    }

}
