/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1.java,v 1.15 2008/02/29 18:08:54 hburger Exp $
 * Description: The dataprovider kernel
 * Revision:    $Revision: 1.15 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/29 18:08:54 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.kernel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.generic.view.Manager_1;
import org.openmdx.base.accessor.jmi.spi.PersistenceManagerFactory_1;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.object.spi.PersistenceManagerFactory_2_0;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.PersistenceManagerFactory_1_0;
import org.openmdx.compatibility.base.application.cci.ConfigurationProvider_1_0;
import org.openmdx.compatibility.base.application.cci.ConfigurationSpecifier;
import org.openmdx.compatibility.base.application.cci.Configuration_1_0;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.application.spi.ConfigurationProviderAdapter_1;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderLayers;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.spi.LayerStatistics_1_0;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.dataprovider.transport.adapter.Provider_1;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_2Connection;
import org.openmdx.compatibility.base.dataprovider.transport.delegation.Connection_1;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_5;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.accessor.basic.spi.Model_1;

/**
 * This class is the dataprovider builder.
 */
public class Dataprovider_1 
    implements Dataprovider_1_2Connection
{

    /**
     * Constructor
     *
     * @param       dataproviderConfiguration
     *              The configuration shared by all layers
     * @param       configurationProvider
     *              The configurationProvider provides the configurations for
     *              the kernel and the individual layers.
     * @param       self 
     *              A self reference
     *
     * @exception   ServiceException
     *              If the instantiation of the dataprovider fails
     *
     * @deprecated  Configuration_1_0 superseeded by ConfigurationProvider_1_0
     */
    public Dataprovider_1(
        Configuration dataproviderConfiguration,
        Configuration_1_0 configurationProvider, 
        Dataprovider_1_0 self
    ) throws ServiceException {
        this(
            dataproviderConfiguration, 
            new ConfigurationProviderAdapter_1(configurationProvider), 
            self
        );
    }

    /**
     * Constructor
     * 
     * @param       dataproviderConfiguration
     *              The configuration shared by all layers
     * @param       configurationProvider
     *              The configurationProvider provides the configurations for
     *              the kernel and the individual layers.
     * @param       self 
     *              A self reference
     *
     * @exception   ServiceException
     *              If the instantiation of the dataprovider fails
     */
    public Dataprovider_1(
        Configuration dataproviderConfiguration,
        ConfigurationProvider_1_0 configurationProvider, 
        Dataprovider_1_0 self
    ) throws ServiceException {

        Configuration kernelConfiguration;
        String namespace;
        
        // Get kernel configuration
        try {
            kernelConfiguration = configurationProvider.getConfiguration(
                new String[]{KERNEL_CONFIGURATION_SECTION},
                this.configurationSpecification()
            );
            namespace = kernelConfiguration.getFirstValue(
                SharedConfigurationEntries.NAMESPACE_ID
            );
        } catch (Exception exception) {
            throw new ServiceException (
                exception, 
            BasicException.Code.DEFAULT_DOMAIN, 
            BasicException.Code.ACTIVATION_FAILURE, 
                new BasicException.Parameter [] {
                    new BasicException.Parameter("section", KERNEL_CONFIGURATION_SECTION)
                },
                "Error retrieving dataprovider kernel options"
            );
        }
        
        try {
            SysLog.trace (
                "Creating kernel for namespace \"" + namespace + '"',
                kernelConfiguration
            );
        
            // Prepare layer statistics objects
            LayerStatistics_1_0[] layerStatistics = namespaceStatistics.get(namespace);
            if(layerStatistics == null) synchronized(namespaceStatistics){
                layerStatistics = namespaceStatistics.get(namespace);
                if(layerStatistics == null) {
                    layerStatistics = new LayerStatistics_1_0[LAYERS];
                    for(
                        short layer = DataproviderLayers.PERSISTENCE;
                        layer <= DataproviderLayers.INTERCEPTION;
                        layer++
                    ) layerStatistics[layer] = new LayerStatistics_1();
                    namespaceStatistics.put(namespace, layerStatistics);
                }
            }
    
            // Get dataprovider configuration

            // LAYER_STATISTICS
            dataproviderConfiguration.values(
              SharedConfigurationEntries.LAYER_STATISTICS
            ).addAll(
                Arrays.asList(layerStatistics)
            );

            // NAMESPACE_ID
            dataproviderConfiguration.values(
                SharedConfigurationEntries.NAMESPACE_ID
            ).add(namespace);

            // EXPOSED_PATH
            SparseList<Path> exposedPaths = dataproviderConfiguration.values(
              SharedConfigurationEntries.EXPOSED_PATH
            );
            for(
                ListIterator<?> iterator = kernelConfiguration.values(
                    SharedConfigurationEntries.EXPOSED_PATH
                ).populationIterator();
                iterator.hasNext();
            ) exposedPaths.set(
                iterator.nextIndex(),
                toPath(iterator.next())
            );
    
            // DELEGATION_PATH
            SparseList<Path> delegationPaths = dataproviderConfiguration.values(
              SharedConfigurationEntries.DELEGATION_PATH
            );
            for(
                ListIterator<?> iterator = kernelConfiguration.values(
                    SharedConfigurationEntries.DELEGATION_PATH
                ).populationIterator();
                iterator.hasNext();
            ) delegationPaths.set(
                iterator.nextIndex(),
                toPath(iterator.next())
            );
    
            // MODEL
            dataproviderConfiguration.values(
              SharedConfigurationEntries.MODEL_PACKAGE
            ).addAll(
              kernelConfiguration.values(SharedConfigurationEntries.MODEL_PACKAGE)
            );
            dataproviderConfiguration.values(
              SharedConfigurationEntries.PACKAGE_IMPL
            ).addAll(
              kernelConfiguration.values(SharedConfigurationEntries.PACKAGE_IMPL)
            );
            this.model = new Model_1();
            dataproviderConfiguration.values(
              SharedConfigurationEntries.MODEL
            ).add(model);
            model.addModels(
              dataproviderConfiguration.values(SharedConfigurationEntries.MODEL_PACKAGE)
            );

            // DATAPROVIDER_CONNECTION
            dataproviderConfiguration.values(
                SharedConfigurationEntries.DATAPROVIDER_CONNECTION
            ); // Like that even an initially empty entry is sharable
            
            // SELF
            this.self = self;
            if(self != null) dataproviderConfiguration.values(
                SharedConfigurationEntries.THIS_DATAPROVIDER
            ).add(
                self
            );
            
            // Load, configure and activate plug-ins
            Configuration[] layerConfigurations = new Configuration[LAYERS];
            for(
                short step = LOAD_PLUGIN;
                step <= ACTIVATE_PLUGIN;
                step++
            ) for(
                short layer = DataproviderLayers.PERSISTENCE;
                layer <= DataproviderLayers.INTERCEPTION;
                layer++
            ) try {
                switch (step) {
                    case LOAD_PLUGIN:
                        String className = kernelConfiguration.getFirstValue(
                            DataproviderLayers.toString(layer)
                        );
                        layers[layer]=(Layer_1_0)Classes.getApplicationClass(
                          className
                        ).newInstance();
                    break;
                    case CONFIGURE_PLUGIN:
                        // Load configuration
                        Configuration layerConfiguration =
                            configurationProvider.getConfiguration(
                                new String[]{DataproviderLayers.toString(layer)},
                                layers[layer].configurationSpecification()
                            );

                        // Propagate dataprovider configuration entries
                        Map<String,SparseList<?>> source = dataproviderConfiguration.entries();
                        Map<String,SparseList<?>> target = layerConfiguration.entries();
                        Object name;
                        for(
                            Iterator<?> names = source.keySet().iterator();
                            names.hasNext();
                        ) if (
                            ! target.containsKey(name = names.next())
                        ) target.put(name.toString(), source.get(name));

                        layerConfigurations[layer] = layerConfiguration;
                    break;
                    case ACTIVATE_PLUGIN:
                        layers[layer].activate(
                            layer,
                            layerConfigurations[layer],
                            layer == 0 ? null : layers[layer-1]
                        );
                    break;
                }
            } catch (Exception exception) {
                
                String id = DataproviderLayers.toString(layer);
                String className = kernelConfiguration.getFirstValue(id);
                throw new ServiceException(
                    exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("layer",id),
                        new BasicException.Parameter("class",className),
                        new BasicException.Parameter("step",step)
                    },
                    "Could not activate " + id + " layer plugin " + className
                ).log();
            }

            SysLog.detail(
                "Kernel for namespace \"" + namespace + "\" created",
                 kernelConfiguration
            );

            this.delegation = layers[DataproviderLayers.INTERCEPTION];

        } catch (Exception exception) {
            throw new ServiceException(
                exception,
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ACTIVATION_FAILURE,
                kernelConfiguration.toExceptionParameters(),
                "Creation of kernel for namespace \"" + namespace + "\" failed"
            ).log();
        }
    }


    /**
     * The kernel's specific configuration specifiers.
     *
     * @return    a map with id/ConfigurationSpecifier entries
     */
    private Map<String,ConfigurationSpecifier> configurationSpecification(
    ){
        Map<String,ConfigurationSpecifier> specification = 
            new HashMap<String,ConfigurationSpecifier>();
        for(
            int layer = DataproviderLayers.PERSISTENCE;
            layer <= DataproviderLayers.INTERCEPTION;
            layer++
        ) specification.put(
            DataproviderLayers.toString(layer),
            new ConfigurationSpecifier (
                DataproviderLayers.toString(layer) + " layer plug-in class",
                true, 1, 1
            )
        );
        specification.put(
            SharedConfigurationEntries.NAMESPACE_ID,
              new ConfigurationSpecifier (
                "The namespace ID. Where <value> is a simple string",
                true, 1, 1
            )
        );
        specification.put(
            SharedConfigurationEntries.EXPOSED_PATH,
            new ConfigurationSpecifier (
                "A requests is not accepted " +
                    " unless its path starts with one of the exposed ones",
                true, 1, 100
            )
        );
        specification.put(
            SharedConfigurationEntries.DELEGATION_PATH,
            new ConfigurationSpecifier (
                "Requests to be delegated use the dataprovider with the same " +
					"index as its matching delegation path.",
                false, 1, 100
            )
        );
		specification.put(
            SharedConfigurationEntries.MODEL_PACKAGE,
            new ConfigurationSpecifier (
                "Optional model packages specified as full qualified class names.",
                true, 0, 100
              )
          );
        return specification;
    }

    /**
     * The toPath method accepts any input class
     */
    protected final static Path toPath(
        Object path
    ){
        return
            path == null ? 
                null : 
            path instanceof Path ? 
                (Path)path : 
                new Path(path.toString());
    }

    
    //------------------------------------------------------------------------
    // Implements PersistenceManagerFactory_2_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManagerFactory_2_0#getPersistenceManager()
     */
    public PersistenceManager getPersistenceManager(
    ) throws ServiceException{
        return this.delegation instanceof PersistenceManagerFactory_2_0 ?
            ((PersistenceManagerFactory_2_0)this.delegation).getPersistenceManager() :
            getPersistenceManager(new ServiceHeader());
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManagerFactory_2_0#getPersistenceManager(java.lang.String, java.lang.String)
     */
    public PersistenceManager getPersistenceManager(
        String userid,
        String password
    ) throws ServiceException {
        return this.delegation instanceof PersistenceManagerFactory_2_0 ?
            ((PersistenceManagerFactory_2_0)this.delegation).getPersistenceManager(userid, password) :
            getPersistenceManager(PersistenceManagerFactory_1.newServiceHeader(userid));
    }

    
    //------------------------------------------------------------------------
    // Implements PersistenceManagerFactory_1_0
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.PersistenceManagerFactory_1_0#getPersistenceManager(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader)
     */
    public PersistenceManager getPersistenceManager(
        ServiceHeader serviceHeader
    ) throws ServiceException {
        if(this.delegation instanceof PersistenceManagerFactory_1_0) {
            return ((PersistenceManagerFactory_1_0)this.delegation).getPersistenceManager(serviceHeader);
        } else {
            Connection_1_5 connection = new Connection_1(
                new Provider_1(
                    new RequestCollection(
                        serviceHeader,
                        this.self
                    ),
                    false // transactionPolicyIsNew
                ),
                true // containerManagedUnitOfWork
            ); 
            connection.setModel(this.model);
            return new RefRootPackage_1(
                new Manager_1(connection),
                false // throwNotFoundIfNull
            ).refPersistenceManager();
        }
    }

    
    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0
    //------------------------------------------------------------------------

    /**
     * Process a set of working units
     *
     * @param    header            the service header
     * @param    workingUnits    a collection of working units
     *
     * @return    a collection of working unit replies
     */
    public UnitOfWorkReply[] process(
        ServiceHeader header,
        UnitOfWorkRequest[] workingUnits
    ){
        if (SysLog.isTraceOn()) SysLog.trace(
            "Requested by " + header.getPrincipalChain(),
            Arrays.asList(workingUnits)
        );
        return delegation.process(header, workingUnits);
    }


    //------------------------------------------------------------------------
    // Implements Dataprivider_1_1Connection
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection#close()
     */
    public void close() {
        this.delegation = null;
        for(
          short layer = DataproviderLayers.INTERCEPTION;
          layer >= DataproviderLayers.PERSISTENCE;
          layer--
        ) if (layers[layer] != null) try {
          layers[layer].deactivate();
        } catch (Exception exception) {
          String id = DataproviderLayers.toString(layer);
          String className = layers[layer].getClass().getName();
          new ServiceException(
            exception,
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.DEACTIVATION_FAILURE,
            new BasicException.Parameter[]{
              new BasicException.Parameter("layer", id),
              new BasicException.Parameter("class", className)
            },
            "Could not deactivate " + id + " layer plugin " + className
          ).log();
        }
    }

    
    //------------------------------------------------------------------------
    // Implements LifeCycleObject_1_0
    //------------------------------------------------------------------------

    /**
     * Deactivates this kernel
     */
    public void remove(
    ){
        close();
    }
    

    //------------------------------------------------------------------------
    // Instance Members
    //------------------------------------------------------------------------

    /**
     * The model repository is propagated to the layer configuration as well.
     */
    private final Model_1_3 model;
    /**
     * Initialized when the kernel is ready to act as dataprovider connection.
     */
    private Layer_1_0 delegation = null;
    
    /**
     * The layer instances
     */
    final protected Layer_1_0[] layers = new Layer_1_0[LAYERS];

    /**
     * EJB self reference
     */
    private final Dataprovider_1_0 self;

    
    //------------------------------------------------------------------------
    // Class Members
    //------------------------------------------------------------------------

    /**
     *
     */
    static private final String KERNEL_CONFIGURATION_SECTION = "KERNEL";

    /**
     * Number of layers
     */
    final static int LAYERS = DataproviderLayers.INTERCEPTION + 1;
    
    /**
     * Plugin activation step
     */
    final private static short LOAD_PLUGIN         = 0;
    
    /**
     * Plugin activation step
     */
    final private static short CONFIGURE_PLUGIN    = 1;

    /**
     * Plugin activation step
     */
    final private static short ACTIVATE_PLUGIN    = 2;

    /**
     * Contains one layer statistic array per namespace.
     */
    final static Map<String,LayerStatistics_1_0[]> namespaceStatistics = new HashMap<String,LayerStatistics_1_0[]>();

}
