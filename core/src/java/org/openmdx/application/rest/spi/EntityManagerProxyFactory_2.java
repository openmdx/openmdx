/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: EntityManagerProxyFactory_2.java,v 1.10 2010/12/22 00:13:20 hburger Exp $
 * Description: Entity Manager Proxy Factory
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/12/22 00:13:20 $
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
package org.openmdx.application.rest.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.Constants;
import javax.jdo.JDODataStoreException;
import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.Interaction;
import javax.resource.spi.ResourceAllocationException;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.spi.PropertiesConfigurationProvider;
import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.rest.DataObjectManager_1;
import org.openmdx.base.accessor.rest.spi.BasicCache_2;
import org.openmdx.base.accessor.rest.spi.Switch_2;
import org.openmdx.base.aop0.PlugIn_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.ConfigurableProperty;
import org.openmdx.base.persistence.spi.AbstractPersistenceManagerFactory;
import org.openmdx.base.persistence.spi.PersistenceManagers;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.base.rest.spi.ConnectionAdapter;
import org.openmdx.base.transaction.TransactionAttributeType;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.BeanFactory;
import org.openmdx.kernel.loading.Factory;

/**
 * Entity Manager Proxy Factory
 */
public class EntityManagerProxyFactory_2 extends AbstractPersistenceManagerFactory<DataObjectManager_1_0> {

    /**
     * Constructor 
     *
     * @param configuration
     */
    protected EntityManagerProxyFactory_2(
        Map<?,?> configuration
    ){
        super(configuration);
        //
        // Data Manager Properties
        // 
        try {
            Configuration dataManagerConfiguration = PropertiesConfigurationProvider.getConfiguration(
                PropertiesConfigurationProvider.toProperties(configuration),
                "org", "openmdx", "jdo", "DataManager"
            );
            this.optimalFetchSize = (Integer) dataManagerConfiguration.values(
                "optimalFetchSize"
            ).get(0);
            this.cacheThreshold = (Integer) dataManagerConfiguration.values(
                "cacheThreshold"
            ).get(0);
        } catch (ServiceException exception) {
            throw BasicException.initHolder(
                new JDOFatalDataStoreException(
                    "Data object manager factory set up failure",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION
                    )
                )
            );
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
                            Map<String,Object> connectionDriverProperties = new HashMap<String,Object>();
                            //
                            // Standard Properties
                            //
                            connectionDriverProperties.put(
                                "ConnectionURL",
                                connectionURL
                            );
                            String userName = super.getConnectionUserName();
                            if(userName != null) {
                                connectionDriverProperties.put(
                                    "UserName",
                                    userName
                                );
                            }
                            String password = super.getConnectionPassword();
                            if(password != null) {
                                connectionDriverProperties.put(
                                    "Password",
                                    password
                                );
                            }
                            //
                            // Specific Properties
                            //
                            try {
                                Configuration connectionDriverConfiguration = PropertiesConfigurationProvider.getConfiguration(
                                    PropertiesConfigurationProvider.toProperties(configuration),
                                    "org", "openmdx", "jdo", "ConnectionDriver"
                                );
                                connectionDriverProperties.putAll(connectionDriverConfiguration.entries());
                            } catch (ServiceException exception) {
                                throw BasicException.initHolder(
                                    new JDOFatalDataStoreException(
                                        "Data object manager factory set up failure",
                                        BasicException.newEmbeddedExceptionStack(
                                            exception,
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.INVALID_CONFIGURATION
                                        )
                                    )
                                );
                            }
                            connectionFactory = BeanFactory.newInstance(
                                connectionDriverName,
                                connectionDriverProperties
                            );
                        }
                    }
                } else {
                    try {
                        connectionFactory = new InitialContext().lookup(connectionFactoryName);
                    } catch (NamingException exception) {
                        throw new ServiceException(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.INVALID_CONFIGURATION,
                            "Connection factory lookup failure",
                            new BasicException.Parameter(
                                Constants.PROPERTY_CONNECTION_FACTORY_NAME, 
                                connectionFactoryName
                            )
                        );
                    }
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
     * Catch all proxied objects
     */
    private static final Path PROXY_PATTERN = new Path("%");
    
    /**
     * Create a new Port
     * 
     * @param connectionFactory
     * 
     * @return a new <code>Port</code> for the given connection factory
     * 
     * @throws ServiceException
     */
    protected Port newPort(
        final Object connectionFactory
    ) throws ServiceException{
        if(connectionFactory instanceof ConnectionFactory) {
            return new Port(){
                
                /* (non-Javadoc)
                 * @see org.openmdx.base.resource.spi.Port#getInteraction(javax.resource.cci.Connection)
                 */
                public Interaction getInteraction(
                    Connection connection
                ) throws ResourceException {
                    if(connection instanceof ConnectionAdapter) {
                        return ((ConnectionFactory)connectionFactory).getConnection(
                            ((ConnectionAdapter)connection).getConnectionSpec()
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
            if(port instanceof Port) {
                return (Port) port;
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
     * The default configuration
     */
    protected static final Map<String, Object> DEFAULT_CONFIGURATION = new HashMap<String, Object>(
        AbstractPersistenceManagerFactory.DEFAULT_CONFIGURATION
    );

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 7461507288357096266L;

    /**
     * The optimal fetch size
     */
    private final Integer optimalFetchSize;

    /**
     * Collections smaller than this value are cached before being evaluated
     */
    private final Integer cacheThreshold;
    
    /**
     * The destinations
     */
    private final Map<Path,Port> destinations;

    /**
     * The standard plug-ins
     */
    private static final PlugIn_1_0[] STANDARD_PLUG_INS = {
        new ProxyPlugIn_1()
    };
    
    /**
     * The method is used by JDOHelper to construct an instance of 
     * <code>PersistenceManagerFactory</code> based on user-specified 
     * properties.
     * 
     * @param props
     * 
     * @return a new <code>PersistenceManagerFactory</code>
     */
    @SuppressWarnings("unchecked")
    public static PersistenceManagerFactory getPersistenceManagerFactory (
        Map props
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
    @SuppressWarnings("unchecked")
    public static PersistenceManagerFactory getPersistenceManagerFactory (
        Map overrides, 
        Map props
    ){
        Map<Object,Object> configuration = new HashMap<Object,Object>(DEFAULT_CONFIGURATION);
        configuration.putAll(props);
        configuration.putAll(overrides);
        return new EntityManagerProxyFactory_2(configuration);
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
            RestConnectionSpec connectionSpec = new RestConnectionSpec(userid, password);
            return new DataObjectManager_1(
                this,
                true, // proxy
                userid == null ? null : PersistenceManagers.toPrincipalChain(userid),
                ConnectionAdapter.newInstance(
                    null, // connectionFactory
                    connectionSpec,     
                    TransactionAttributeType.SUPPORTS, 
                    new Switch_2(
                        new BasicCache_2(), 
                        this.destinations
                    )
                ), 
                null, // connection2
                STANDARD_PLUG_INS, 
                this.optimalFetchSize, 
                this.cacheThreshold, 
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

    static {
        EntityManagerProxyFactory_2.DEFAULT_CONFIGURATION.put(
            ConfigurableProperty.TransactionType.qualifiedName(),
            Constants.RESOURCE_LOCAL
        );    
    }

}
