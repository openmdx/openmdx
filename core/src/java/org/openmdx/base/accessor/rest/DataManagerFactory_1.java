/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Data Object Manager Factory
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
package org.openmdx.base.accessor.rest;

import static org.openmdx.application.dataprovider.cci.SharedConfigurationEntries.DATABASE_CONNECTION_FACTORY;
import static org.openmdx.application.dataprovider.cci.SharedConfigurationEntries.DATAPROVIDER_CONNECTION_FACTORY;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;

import javax.jdo.Constants;
import javax.jdo.JDODataStoreException;
import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.PersistenceManagerFactory;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.spi.PropertiesConfigurationProvider;
import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.rest.spi.BasicCache_2;
import org.openmdx.base.accessor.rest.spi.ConnectionCacheProvider_2_0;
import org.openmdx.base.accessor.rest.spi.DataStoreCache_2_0;
import org.openmdx.base.accessor.rest.spi.PinningCache_2;
import org.openmdx.base.accessor.rest.spi.Switch_2;
import org.openmdx.base.aop0.PlugIn_1_0;
import org.openmdx.base.aop0.UpdateAvoidance_1;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.ConfigurableProperty;
import org.openmdx.base.persistence.spi.AbstractPersistenceManagerFactory;
import org.openmdx.base.persistence.spi.PersistenceManagers;
import org.openmdx.base.resource.cci.ConnectionFactory;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.base.rest.spi.ConnectionFactoryAdapter;
import org.openmdx.base.transaction.TransactionAttributeType;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.BeanFactory;
import org.openmdx.kernel.loading.Resources;
import org.w3c.cci2.SparseArray;


/**
 * Data Object Manager Factory
 */
public class DataManagerFactory_1 
    extends AbstractPersistenceManagerFactory<DataObjectManager_1_0> 
    implements ConnectionCacheProvider_2_0
{

    /**
     * Constructor 
     *
     * @param configuration
     */
    protected DataManagerFactory_1(
        final Map<?,?> configuration
    ){
        super(configuration);
        try {
            Properties properties = PropertiesConfigurationProvider.toProperties(configuration);
            Configuration persistenceManagerConfiguration = PropertiesConfigurationProvider.getConfiguration(
                properties,
                "org", "openmdx", "jdo", "DataManager"
            );
            this.optimalFetchSize = (Integer) persistenceManagerConfiguration.values(
                "optimalFetchSize"
            ).get(Integer.valueOf(0));
            this.cacheThreshold = (Integer) persistenceManagerConfiguration.values(
                "cacheThreshold"
            ).get(Integer.valueOf(0));
            Object connectionFactory = super.getConnectionFactory();
            // TODO Android
            if(connectionFactory instanceof ConnectionFactoryAdapter) {
            	this.connectionFactory = (ConnectionFactory) connectionFactory;
            	this.connectionFactory2 = (ConnectionFactory) super.getConnectionFactory2();
            	this.cachePlugIn = null; // TODO
            	this.plugIns = DEFAULT_PLUG_INS;
            } else {
                SparseArray<?> plugIns = persistenceManagerConfiguration.values(
                    "plugIn"
                );
                if(plugIns.isEmpty()) {
                    this.plugIns = DEFAULT_PLUG_INS;
                } else {
                    this.plugIns = new PlugIn_1_0[plugIns.size()];
                    for(
                        int i = 0;
                        i < this.plugIns.length;
                        i++
                    ){
                        this.plugIns[i] = new BeanFactory<PlugIn_1_0>(
                            "class",
                            PropertiesConfigurationProvider.getConfiguration(
                                properties,
                                toSection(plugIns.get(Integer.valueOf(i)))
                            ).entries()
                        ).instantiate();
                    }
                }
                String cachePlugIn = persistenceManagerConfiguration.getFirstValue("cachePlugIn");
                this.cachePlugIn = cachePlugIn == null ? new PinningCache_2(
                ) : new BeanFactory<BasicCache_2>(
                    "class",
                    PropertiesConfigurationProvider.getConfiguration(
                        properties,
                        cachePlugIn.split("\\.")
                    ).entries()
                ).instantiate();
                String connectionFactoryName = super.getConnectionFactoryName();
                Map<Path,Port> destinations = new LinkedHashMap<Path,Port>();
                Port port = new Switch_2(
                    this.cachePlugIn, 
                    destinations
                );
                boolean supportsLocalTransaction = Constants.RESOURCE_LOCAL.equals(super.getTransactionType()); 
                this.connectionFactory = new ConnectionFactoryAdapter(
                    port,
                    supportsLocalTransaction,
                    TransactionAttributeType.MANDATORY
                );
                this.connectionFactory2 = new ConnectionFactoryAdapter(
                    port,
                    supportsLocalTransaction,
                    TransactionAttributeType.REQUIRES_NEW
                );
                Map<String,Port> raw = new LinkedHashMap<String,Port>();
                SparseArray<?> restPlugIns = persistenceManagerConfiguration.values(
                    "restPlugIn"
                );
                for(
                    ListIterator<?> i = persistenceManagerConfiguration.values(
                        "xriPattern"
                    ).populationIterator();
                    i.hasNext();
                ){
                    int index = i.nextIndex();
                    String pattern = i.next().toString();
                    String restPlugIn = (String) restPlugIns.get(Integer.valueOf(index));
                    Port destination = raw.get(restPlugIn);
                    if(destination == null){
                        Configuration plugInConfiguration = PropertiesConfigurationProvider.getConfiguration(
                            properties,
                            restPlugIn.split("\\.")
                        );
                        plugInConfiguration.values(
                            DATAPROVIDER_CONNECTION_FACTORY
                        ).put(
                            Integer.valueOf(0), 
                            this.connectionFactory
                        );
                        if(connectionFactoryName != null) {
                            SparseArray<Object> datasources = plugInConfiguration.values(
                            	DATABASE_CONNECTION_FACTORY
                            );
                            if(datasources.isEmpty()) {
                                datasources.put(Integer.valueOf(0),connectionFactoryName);
                            }
                        }
                        destination = new BeanFactory<Port>(
                            "class",
                            plugInConfiguration.entries()
                        ).instantiate();
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
    protected static final Map<String, Object> DEFAULT_CONFIGURATION = new HashMap<String, Object>(
        AbstractPersistenceManagerFactory.DEFAULT_CONFIGURATION
    );

    /**
     * 
     */
    private static final PlugIn_1_0[] DEFAULT_PLUG_INS = {
        new UpdateAvoidance_1()
    };
    
    /**
     * Implements <code>Serializabel</code>
     */
    private static final long serialVersionUID = -8694584589690397280L;

    /**
     * The optimal fetch size
     */
    private final Integer optimalFetchSize;

    /**
     * Collections smaller than this value are cached before being evaluated
     */
    private final Integer cacheThreshold;

    /**
     * 
     */
    private final BasicCache_2 cachePlugIn;
    
    /**
     * 
     */
    private final PlugIn_1_0[] plugIns;
    
    /**
     * Standard REST Connection Factory
     */
    private final ConnectionFactory connectionFactory;

    /**
     * Standard REST Connection Factory
     */
    private final ConnectionFactory connectionFactory2;
    
    /**
     * The method is used by JDOHelper to construct an instance of 
     * <code>PersistenceManagerFactory</code> based on user-specified 
     * properties.
     * 
     * @param props
     * 
     * @return a new <code>PersistenceManagerFactory</code>
     */
    @SuppressWarnings("rawtypes")
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static PersistenceManagerFactory getPersistenceManagerFactory (
        Map overrides, 
        Map props
    ){
        Map<Object,Object> configuration = new HashMap<Object,Object>(DEFAULT_CONFIGURATION);
        configuration.putAll(props);
        try {
            String dataObjectManagerName = (String)props.get(ConfigurableProperty.Name.qualifiedName());
            if(dataObjectManagerName != null) {
                for(URL resource : Resources.getMetaInfResources(dataObjectManagerName + ".properties")) {
                    Properties properties = new Properties();
                    properties.load(resource.openStream());
                    configuration.putAll(properties);
                }
            }
        } catch(
        	Exception e
        ) {
        	// ignore configuration exceptions
        }        
        configuration.putAll(overrides);
        return new DataManagerFactory_1(configuration);
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
     * Retrieve the connection for non-transactional read operations
     * 
     * @param connectionSpec
     * 
     * @return a connection for non-transactional read operations or <code>null</code>
     * 
     * @throws ResourceException
     */
    private Connection getConnection2(
    	RestConnectionSpec connectionSpec
    ) throws ResourceException{
    	if(
    		this.connectionFactory2 != null && (
    			getOptimistic() || (
    				!getContainerManaged() && (
    					this.getNontransactionalRead() || this.getNontransactionalWrite()
    				)
    			)
    		)
    	) {
    		return this.connectionFactory2.getConnection(connectionSpec);
    	} else {
    		return null;
    	}
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
            RestConnectionSpec connectionSpec = new RestConnectionSpec(
                userid,
                password
            );
            return new DataObjectManager_1(
                this,
                false,
                userid == null ? null : PersistenceManagers.toPrincipalChain(userid),
                this.connectionFactory.getConnection(connectionSpec),
                getConnection2(connectionSpec),
                this.plugIns, 
                this.optimalFetchSize, 
                this.cacheThreshold, 
                connectionSpec  
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

    /**
     * Retrieve the data store cache
     * 
     * @param dataManagerFactory the data manager factory
     * 
     * @return the data store cache
     */
    public DataStoreCache_2_0 getConnectionCache(
    ){
        return this.cachePlugIn;
    }
    
}
