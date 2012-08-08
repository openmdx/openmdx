/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: EntityManagerFactory_2Bean.java,v 1.16 2009/06/08 17:11:16 hburger Exp $
 * Description: Entity Manager Factory EJB 
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/08 17:11:16 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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

package org.openmdx.application.persistence.ejb;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionFactory;
import javax.resource.spi.LocalTransactionException;
import javax.transaction.Synchronization;

import org.openmdx.application.cci.ConfigurationSpecifier;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.application.dataprovider.kernel.ManagerFactoryConfigurationEntries;
import org.openmdx.application.ejb.spi.SessionBean_1;
import org.openmdx.application.rest.ejb.Connection_2Factory;
import org.openmdx.application.rest.ejb.LateBindingConnectionFactory;
import org.openmdx.application.rest.spi.FinalClassPlugIn_2;
import org.openmdx.application.rest.spi.PlugIn_2Factory;
import org.openmdx.application.rest.spi.Router_2;
import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.accessor.rest.DataObjectManagerFactory_1;
import org.openmdx.base.accessor.view.ViewManager_1;
import org.openmdx.base.beans.BeanFactory;
import org.openmdx.base.beans.Factory;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.EntityManagerFactory;
import org.openmdx.base.persistence.cci.UserObjects;
import org.openmdx.base.persistence.spi.AspectObjectAcessor;
import org.openmdx.base.resource.spi.TransactionManager;
import org.openmdx.base.rest.spi.RestConnection;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.BasicException.Parameter;
import org.openmdx.kernel.persistence.spi.DelegatingPersistenceManagerFactory;

/**
 * Entity Manager Factory EJB 
 */
public class EntityManagerFactory_2Bean
    extends SessionBean_1
    implements TransactionManager, EntityManagerFactory 
{

    /**
     * The plug-in specific configuration specifiers.
     */
    private final static Map<String,ConfigurationSpecifier> entityManagerConfigurationSpecification = Collections.singletonMap(
        ManagerFactoryConfigurationEntries.PLUG_IN,
        new ConfigurationSpecifier (
            "The plug-ins",
            true, 
            0, 
            100
        )
    );

    /**
     * The persistence manager specific configuration specifiers.
     */
    private final static Map<String,ConfigurationSpecifier> persistenceManagerConfigurationSpecification = Collections.singletonMap(
        ManagerFactoryConfigurationEntries.REST_PROVIDER,
        new ConfigurationSpecifier (
            "The REST provdiders' JNDI names",
            true, 
            0, 
            100
        )
    );
    
    /**
     * The manager specific configuration specifiers.
     */
    private final static  Map<String,ConfigurationSpecifier> plugInConfigurationSpecification = ArraysExtension.asMap(
        new String[]{
            SharedConfigurationEntries.MODEL_PACKAGE,
            SharedConfigurationEntries.PACKAGE_IMPL,
            ManagerFactoryConfigurationEntries.USER_OBJECT_NAME
        },
        new ConfigurationSpecifier[]{
            new ConfigurationSpecifier (
                "Optional model packages specified as full qualified class names.",
                true, 
                0, 
                100
            ),
            new ConfigurationSpecifier (
                "Java packages containing implementation classes specified as " +
                "Fully qualified java package name names.",
                true, 
                0, 
                100
            ),
            new ConfigurationSpecifier (
                "Names of the plug-in scoped user objects",
                false, 
                0, 
                100
            )
        }
    );

    /**
     * 
     */
    private List<Configuration> plugInConfigurations;    

    /**
     * Required to route the requests
     */
    private Map<Path,ConnectionFactory> destinations;

    /**
     * Implements <code>Serializable</cpde>
     */
    private static final long serialVersionUID = -4785739824343154491L;

    private static class EntityManagerFactory_2  
        extends DelegatingPersistenceManagerFactory {
    
        @SuppressWarnings("unchecked")
        private EntityManagerFactory_2 (
            PersistenceManager persistenceManager,        
            Configuration plugInConfiguration 
        ){
            this(
                persistenceManager.getPersistenceManagerFactory(),
                (Map<String,String>)plugInConfiguration.values("implementationMap").get(0), 
                (Map<String,Object>)plugInConfiguration.values("userObjects").get(0)
            );
        }
    
        private EntityManagerFactory_2 (
            PersistenceManagerFactory delegate,
            Map<String,String> implementationMap, 
            Map<String,Object> userObjects
        ){
            this.delegate = delegate;
            this.implementationMap = implementationMap;
            this.userObjects = userObjects;
        }
    
        /* (non-Javadoc)
         * @see org.openmdx.kernel.persistence.spi.DelegatingPersistenceManagerFactory#delegate()
         */
        @Override
        protected PersistenceManagerFactory delegate() {
            return this.delegate;
        }
    
        /* (non-Javadoc)
         * @see org.openmdx.kernel.persistence.spi.DelegatingPersistenceManagerFactory#getPersistenceManager()
         */
        @Override
        public PersistenceManager getPersistenceManager() {
            return newPersistenceManager(
                super.getPersistenceManager()
            );
        }
    
        /* (non-Javadoc)
         * @see org.openmdx.kernel.persistence.spi.DelegatingPersistenceManagerFactory#getPersistenceManager(java.lang.String, java.lang.String)
         */
        @Override
        public PersistenceManager getPersistenceManager(
            String userid,
            String password
        ) {
            return newPersistenceManager(
                super.getPersistenceManager(userid, password)
            );
        }
    
        protected PersistenceManager newPersistenceManager (
            PersistenceManager delegate
        ){
            return new RefRootPackage_1(
              this, // persistenceManagerFactory
              delegate,
              this.implementationMap,
              this.userObjects,
              UserObjects.getPrincipalChain(delegate)
          ).refPersistenceManager();
        }
    
        public static PersistenceManager newEntityManager(
            DataObjectManager_1_0 persistenceManager, 
            List<Configuration> plugInConfigurations,
            List<String> principalChain,
            boolean accessor
        ){
            // aspectObjectAccessor and callbackRegistry are shared user objects
            Object aspectObjectAccessor = persistenceManager.getUserObject(AspectObjectAcessor.class);
            PersistenceManager layerManager = new ViewManager_1(persistenceManager);
            for(int i = 0; i < plugInConfigurations.size(); i++) {
                layerManager =  new EntityManagerFactory_2(
                    layerManager,
                    plugInConfigurations.get(i) 
                ).newPersistenceManager(
                    layerManager
                );
                if(principalChain != null) {
                    layerManager.putUserObject(
                        Principal[].class,
                        principalChain
                    );
                }
                if(aspectObjectAccessor != null) {
                    layerManager.putUserObject(
                        AspectObjectAcessor.class,
                        aspectObjectAccessor
                    );
                }
            }
            return layerManager;
        }

        //-------------------------------------------------------------------
        // Members
        //-------------------------------------------------------------------        
        private static final long serialVersionUID = -3412830449142819058L;
    
        private final Map<String,String> implementationMap;
        private final Map<String,Object> userObjects;
        private final PersistenceManagerFactory delegate;            
                
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
    protected Configuration getPlugInConfiguration(
        String[] section,
        Map<String,Object> sharedUserObjects
    ) throws ServiceException{
        String[] subSection = new String[section.length + 1];
        System.arraycopy(section, 0, subSection, 0, section.length);
        //
        // User Objects
        //
        Map<String,Object> userObjects = new HashMap<String,Object>(sharedUserObjects);
        Configuration plugInConfiguration = getConfiguration(
            section,
            plugInConfigurationSpecification
        );
        plugInConfiguration.values(
            "userObjects"
        ).set(
            0,
            userObjects
        );
        for(
            ListIterator<?> j = plugInConfiguration.values(
                ManagerFactoryConfigurationEntries.USER_OBJECT_NAME
            ).populationIterator();
            j.hasNext();
        ){
            subSection[section.length] = (String) j.next();
            getUserObject(
                subSection,
                userObjects
            );
        }
        //
        // Model Mapping
        // 
        Map<String,String> implementationMap = new HashMap<String,String>();
        plugInConfiguration.values(
            "implementationMap"
        ).set(
            0,
            implementationMap
        );
        SparseList<String> modelPackages = plugInConfiguration.values(
            SharedConfigurationEntries.MODEL_PACKAGE
        );
        for(
            ListIterator<?> j = plugInConfiguration.values(
                SharedConfigurationEntries.PACKAGE_IMPL
            ).populationIterator();
            j.hasNext();
        ){
            implementationMap.put(
                modelPackages.get(j.nextIndex()),
                j.next().toString()
            );
        }
        return plugInConfiguration;
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
    private void getUserObject(
        String[] section,
        Map<String,Object> userObjects
    ) throws ServiceException {
        Configuration userObjectConfiguration = getConfiguration(
            section
        );  
        try {
            Factory<?> userObjectFactory = new BeanFactory<Object>(
                ManagerFactoryConfigurationEntries.USER_OBJECT_CLASS,
                userObjectConfiguration.entries()
            );
            Object userObject = userObjectFactory.instantiate();
            userObjects.put(
                section[section.length - 1],
                userObject
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

    /* (non-Javadoc)
     * @see org.openmdx.application.j2ee.SessionBean_1#activate()
     */
    @Override
    public void activate(
    ) throws Exception {
        super.activate();
        this.plugInConfigurations = new ArrayList<Configuration>();
        //
        // Persistence Manager Configuration
        // 
        Configuration persistenceManagerConfiguration = getConfiguration(
            new String[] {
                ManagerFactoryConfigurationEntries.PERSISTENCE_MANAGER
            },
            persistenceManagerConfigurationSpecification 
        );
        SparseList<?> restProviders = persistenceManagerConfiguration.values(
            ManagerFactoryConfigurationEntries.REST_PROVIDER
        );
        SparseList<?> restPlugIns = persistenceManagerConfiguration.values(
            ManagerFactoryConfigurationEntries.REST_PLUG_IN
        );
        this.destinations = new HashMap<Path,ConnectionFactory>();
        ConnectionFactory finalClassPlugIn = Connection_2Factory.newInstance(
            new FinalClassPlugIn_2()
        );
        for(Path virtualPathPattern : FinalClassPlugIn_2.PATTERN) {
            this.destinations.put(
                virtualPathPattern,
                finalClassPlugIn
            );
       }
        Map<String,ConnectionFactory> destinations = new HashMap<String,ConnectionFactory>();
        for(
            ListIterator<?> i = persistenceManagerConfiguration.values(
                ManagerFactoryConfigurationEntries.PATTERN
            ).populationIterator();
            i.hasNext();
        ){
            int index = i.nextIndex();
            ConnectionFactory destination;
            String restProvider = (String) restProviders.get(index);
            String restPlugIn = (String) restPlugIns.get(index);
            if (restPlugIn != null) {
                destination = destinations.get(restPlugIn);
                if(destination == null)  try {
                    Configuration plugInConfiguration = getConfiguration(
                        ManagerFactoryConfigurationEntries.PERSISTENCE_MANAGER,
                        restPlugIn
                    );
                    ConnectionFactory nextDestination;
                    if(restProvider == null) {
                        nextDestination = null;
                    } else {
                        nextDestination = destinations.get(restProvider);
                        if(nextDestination == null) {
                            destinations.put(
                                restProvider,
                                nextDestination = LateBindingConnectionFactory.newInstance(
                                    SessionBean_1.BEAN_ENVIRONMENT + '/' + restProvider
                                )
                            );
                        }
                    }
                    destinations.put(
                        restPlugIn,
                        destination = PlugIn_2Factory.newInstance(
                            new BeanFactory<RestConnection>(
                                ManagerFactoryConfigurationEntries.PLUG_IN_CLASS,
                                plugInConfiguration.entries()
                            ),
                            nextDestination
                        )
                    );
                } catch (Exception exception) {
                    throw new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION,
                        "Invalid REST PlugIn configuration",
                        new BasicException.Parameter("restPlugIn", restPlugIn)
                    );
                }
            } else if(restProvider != null) {
                //
                // Provider Mode
                //
                destination = destinations.get(restProvider);
                if(destination == null) {
                    destinations.put(
                        restProvider,
                        destination = LateBindingConnectionFactory.newInstance(
                            SessionBean_1.BEAN_ENVIRONMENT + '/' + restProvider
                        )
                    );
                }
            } else {
                //
                // Comaptibility Mode
                //
                destination = LateBindingConnectionFactory.newInstance(
                    SessionBean_1.BEAN_ENVIRONMENT + '/' +  SharedConfigurationEntries.DATAPROVIDER_CONNECTION + "[" + index + "]"
                );
            }
            this.destinations.put(
                new Path(i.next().toString()),
                destination
            );
        }        
        //
        // Entity Manager Configuration
        // 
        Configuration entityManagerConfiguration = getConfiguration(
            new String[] {
                ManagerFactoryConfigurationEntries.ENTITY_MANAGER
            },
            entityManagerConfigurationSpecification 
        );
        Map<String,Object> userObjects = new HashMap<String,Object>();
        for(
            ListIterator<?> i = entityManagerConfiguration.values(
                ManagerFactoryConfigurationEntries.USER_OBJECT_NAME
            ).populationIterator();
            i.hasNext();
        ) {
            this.getUserObject(
                new String[] {
                    ManagerFactoryConfigurationEntries.ENTITY_MANAGER,
                    (String) i.next()
                },
                userObjects
            );
        }
        // Layer with empty configuration at bottom-level wraps RefObject_1
        this.plugInConfigurations.add(
            this.getPlugInConfiguration(
                new String[] {
                    ManagerFactoryConfigurationEntries.ENTITY_MANAGER,
                    ""
                },
                userObjects
            )
        );
        // User-configured layers
        for(
            ListIterator<?> i = entityManagerConfiguration.values(
                ManagerFactoryConfigurationEntries.PLUG_IN
            ).populationIterator();
            i.hasNext();
        ){
            this.plugInConfigurations.add(
                this.getPlugInConfiguration(
                    new String[] {
                        ManagerFactoryConfigurationEntries.ENTITY_MANAGER,
                        (String) i.next()
                    },
                    userObjects
                )
            );
        }
        // Layer with empty configuration at top-level asserts that
        // JMI invocation handlers work properly for reflective and
        // typed method invocations
        this.plugInConfigurations.add(
            this.getPlugInConfiguration(
                new String[] {
                    ManagerFactoryConfigurationEntries.ENTITY_MANAGER,
                    ""
                },
                userObjects
            )
        );
    }

    //------------------------------------------------------------------------    
    // Implements OptimisticTransaction
    //------------------------------------------------------------------------    
    
    /* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.OptimisticTransaction#commit(javax.transaction.Synchronization)
     */
    public void commit(
        Synchronization synchronization
    ) throws LocalTransactionException {
        SessionContext sessionContext = super.getSessionContext();
        if(sessionContext.getRollbackOnly()) throw BasicException.initHolder(
            new LocalTransactionException(
                "Unit of work was marked for rollback only",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ROLLBACK
                )
            )
        );  
        try {
            synchronization.beforeCompletion();
        } catch (RuntimeException exception) {
            sessionContext.setRollbackOnly();
            throw BasicException.initHolder(
                new LocalTransactionException(
                    "Unit of work set to rollback-only during commit",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ROLLBACK
                    )
                )
            );
        }
    }

    //------------------------------------------------------------------------    
    // Implements EntityManagerFactory
    //------------------------------------------------------------------------    
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.EntityManagerFactory#getEntityManager(java.util.List)
     */
    public PersistenceManager getEntityManager(
        List<String> principalChain
    ) throws ResourceException {
        SessionContext sessionContext = getSessionContext();    
        DataObjectManager_1_0 persistenceManager = new DataObjectManagerFactory_1(
            principalChain, 
            Router_2.newInstance(
                sessionContext.getCallerPrincipal().getName(),
                this.destinations
            ),
            (TransactionManager)sessionContext.getEJBLocalObject()
        ).getPersistenceManager();
        return EntityManagerFactory_2.newEntityManager(
            persistenceManager,
            this.plugInConfigurations, 
            principalChain,
            true
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.cci.EntityManagerFactory#getEntityManager()
     */
    public PersistenceManager getEntityManager(
    ) throws ResourceException {
        return getEntityManager(
            Collections.singletonList(
                getSessionContext().getCallerPrincipal().getName()
            )
        );
    }

}
