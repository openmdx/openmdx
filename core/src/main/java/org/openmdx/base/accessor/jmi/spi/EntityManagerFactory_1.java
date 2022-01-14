/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Entity Manager Factory
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
package org.openmdx.base.accessor.jmi.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.jdo.Constants;
import javax.jdo.JDODataStoreException;
import javax.jdo.JDOFatalDataStoreException;

import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.accessor.view.ViewManagerFactory_1;
import org.openmdx.base.aop1.PlugIn_1;
import org.openmdx.base.aop1.PlugIn_1_0;
import org.openmdx.base.caching.datastore.CacheAdapter;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.persistence.cci.ConfigurableProperty;
import org.openmdx.base.persistence.spi.AbstractPersistenceManagerFactory;
import org.openmdx.kernel.configuration.cci.Configuration;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.BasicException.Parameter;
import org.openmdx.kernel.jdo.JDODataStoreCache;
import org.openmdx.kernel.jdo.JDOPersistenceManager;
import org.openmdx.kernel.jdo.JDOPersistenceManagerFactory;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.kernel.loading.BeanFactory;
import org.openmdx.kernel.loading.Factory;
import org.w3c.cci2.SparseArray;

/**
 * Entity Manager Factory
 * <p>
 * The <code>PersistenceManagerFactory</code> it delegates to can be specified
 * in the following ways:
 * <ol>
 * <li>as <code>org.openmdx.jdo.PersistenceManagerFactory</code> property if
 * the client has set up the persistence manager factory on its own
 * <li>as <code>org.openmdx.jdo.PersistenceManagerFactoryName</code> property for
 * an embedded persistence manager
 * <li>as <code>connection-url</code> attribute for an extra-VM connection
 * <li>as <code>connection-factory-name</code> and
 * <code>connection-factory2-name</code> attributes for JNDI registered
 * objects, such as
 * <ul>
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
     * @param overrides the configuration properties
     * @param configuration the configuration properties
     * @param defaults for missing configuration and override properties
     */
    protected EntityManagerFactory_1(
        Map<?, ?> overrides,
        Map<?, ?> configuration,
        Map<?, ?> defaults
    ) {
        super(overrides, configuration, defaults);
        this.overrides = overrides;
        //
        // Persistence Manager Factory Configuration
        //
        JDOPersistenceManagerFactory dataManagerFactory = (JDOPersistenceManagerFactory) super.getConnectionFactory();
        if (dataManagerFactory == null) {
            String dataManagerFactoryName = super.getConnectionFactoryName();
            if (dataManagerFactoryName != null)
                try {
                    if (dataManagerFactoryName.startsWith("jdo:")) {
                        dataManagerFactory = ReducedJDOHelper.getPersistenceManagerFactory(
                            this.overrides,
                            dataManagerFactoryName.substring(4)
                        );
                    } else {
                        dataManagerFactory = (JDOPersistenceManagerFactory) getConnectionFactoryByName(
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
        if (dataManagerFactory == null) {
            throw BasicException.initHolder(
                hasConnectionURL() ? new JDODataStoreException(
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
                            super.getConnectionURL()
                        )
                    )
                )
                    : new JDODataStoreException(
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
        }
        //
        // Plug-In Configurations
        //
        try {
            Configuration viewManagerConfiguration = getConfiguration("org.openmdx.jdo.ViewManager");
            List<PlugIn_1_0> viewManagerPlugIns = new ArrayList<>();
            for (ListIterator<String> i = viewManagerConfiguration.getSparseArray(
                "plugIn",
                String.class
            ).populationIterator(); i.hasNext();) {
                Configuration viewPlugInConfiguration = getConfiguration(i.next());
                Factory<PlugIn_1_0> viewPlugInFactory = BeanFactory.newInstance(
                    PlugIn_1_0.class,
                    viewPlugInConfiguration
                );
                viewManagerPlugIns.add(viewPlugInFactory.instantiate());
            }
            JDOPersistenceManagerFactory layerManagerFactory = viewManagerPlugIns.isEmpty() ? new ViewManagerFactory_1(
                dataManagerFactory,
                new PlugIn_1()
            )
                : new ViewManagerFactory_1(
                    dataManagerFactory,
                    viewManagerPlugIns.toArray(new PlugIn_1_0[viewManagerPlugIns.size()])
                );
            Configuration entityManagerConfiguration = getConfiguration("org.openmdx.jdo.EntityManager");
            for (ListIterator<String> i = entityManagerConfiguration.getSparseArray(
                "userObject",
                String.class
            ).populationIterator(); i.hasNext();) {
                createUserObject(
                    i.next(),
                    this.userObjects
                );
            }
            SparseArray<String> plugIns = entityManagerConfiguration.getSparseArray(
                "plugIn",
                String.class
            );
            if (!plugIns.isEmpty()) {
                layerManagerFactory = new LayerManagerFactory_2(
                    layerManagerFactory,
                    getPlugInConfiguration(
                        null,
                        this.userObjects
                    )
                );
                // User-configured layers
                for (ListIterator<String> i = plugIns.populationIterator(); i.hasNext();) {
                    layerManagerFactory = new LayerManagerFactory_2(
                        layerManagerFactory,
                        getPlugInConfiguration(
                            i.next(),
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

    private boolean hasConnectionURL() {
        return super.getConnectionURL() != null;
    }

    /**
     * The shared user objects
     */
    private final Map<String, Object> userObjects = new HashMap<>();

    /**
     * The top level layer manager factory
     */
    private final JDOPersistenceManagerFactory delegate;

    /**
     * The entity manager factory's mapping
     */
    private final Mapping_1_0 mapping;

    /**
     * The lazily create data store cache
     */
    private JDODataStoreCache dataStoreCache;

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -3043695082264242663L;

    /**
     * The default configuration
     */
    protected static final Map<String, Object> DEFAULT_CONFIGURATION = createDefaultConfiguration(
        Collections.singletonMap(
            ConfigurableProperty.TransactionType.qualifiedName(),
            Constants.RESOURCE_LOCAL
        )
    );

    // Configuration overrides propagated to persistence manager
    private final Map<?, ?> overrides;

    /**
     * The method is used by JDOHelper to construct an instance of
     * <code>PersistenceManagerFactory</code> based on user-specified
     * properties.
     * 
     * @param props
     * 
     * @return a new <code>PersistenceManagerFactory</code>
     */
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory(
        Map props
    ) {
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
    public static JDOPersistenceManagerFactory getPersistenceManagerFactory(
        Map overrides,
        Map props
    ) {
        return new EntityManagerFactory_1(overrides, props, DEFAULT_CONFIGURATION);
    }

    /**
     * Create and initialize an entity manager
     * 
     * @param delegate
     *            the entity manager's delegate
     * 
     * @return an new entity manager
     */
    private PersistenceManager_1_0 newEntityManager(
        JDOPersistenceManager delegate
    ) {
        return new RefRootPackage_1(
            this, // persistenceManagerFactory
            delegate,
            this.mapping,
            this.userObjects
        ).refPersistenceManager();
    }

    /*
     * (non-Javadoc)
     * 
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

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.persistence.spi.AbstractPersistenceManagerFactory#newPersistenceManager()
     */
    @Override
    protected PersistenceManager_1_0 newPersistenceManager() {
        return newEntityManager(
            this.delegate.getPersistenceManager()
        );
    }

    /**
     * Create a user object
     * 
     * @param configurationProvider
     * @param userObjects
     * @param section
     * 
     * @throws ServiceException
     */
    private void createUserObject(
        String section,
        Map<String, Object> userObjects
    ) throws ServiceException {
        try {
            Factory<?> userObjectFactory = BeanFactory.newInstance(
                getConfiguration(section)
            );
            final boolean shared = !userObjectFactory.getInstanceClass().isInterface();
            final String userObjectKey = section.substring(section.lastIndexOf('.') + 1);
            userObjects.put(
                userObjectKey,
                shared ? userObjectFactory.instantiate() : userObjectFactory
            );
        } catch (Exception exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                "User object initialization failure",
                new Parameter("section", section)
            );
        }
    }

    
    /**
     * Prepare a plug-in's configuration according to the configuration
     * provider's entries.
     * 
     * @param configurationProvider
     * @param section
     *            the plug-in's section name
     * @param sharedUserObjects
     * 
     * @return the plug-in configuration
     * 
     * @throws ServiceException
     */
    private Configuration getPlugInConfiguration(
        String section,
        Map<String, Object> sharedUserObjects
    ) throws ServiceException {
        Configuration plugInConfiguration = getConfiguration(section);
        //
        // User Objects
        //
        Map<String, Object> userObjects = plugInConfiguration.getMutableMap(
            "userObjects",
            Object.class
        );
        for (ListIterator<String> j = plugInConfiguration.getSparseArray(
            "userObject",
            String.class
        ).populationIterator(); j.hasNext();) {
            createUserObject(
                j.next(),
                userObjects
            );
        }
        //
        // Model Mapping
        // 
        Map<String, String> implementationMap = plugInConfiguration.getMutableMap(
            "implementationMap",
            String.class
        );
        SparseArray<String> modelPackages = plugInConfiguration.getSparseArray(
            "modelPackage",
            String.class
        );
        for (ListIterator<String> j = plugInConfiguration.getSparseArray(
            "packageImpl",
            String.class
        ).populationIterator(); j.hasNext();) {
            implementationMap.put(
                modelPackages.get(Integer.valueOf(j.nextIndex())),
                j.next()
            );
        }
        return plugInConfiguration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.kernel.jdo.JDOPersistenceManagerFactory#getDataStoreCache()
     */
    @Override
    public JDODataStoreCache getDataStoreCache() {
        if (this.dataStoreCache == null) {
            this.dataStoreCache = new Jmi1DataStoreCache(
                (CacheAdapter) delegate.getDataStoreCache(),
                this.mapping
            );
        }
        return this.dataStoreCache;
    }

}
