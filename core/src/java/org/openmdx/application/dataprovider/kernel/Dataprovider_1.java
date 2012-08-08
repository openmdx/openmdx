/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1.java,v 1.4 2009/02/10 16:08:52 hburger Exp $
 * Description: The dataprovider kernel
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/10 16:08:52 $
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
package org.openmdx.application.dataprovider.kernel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.openmdx.application.cci.ConfigurationProvider_1_0;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderLayers;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.application.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.application.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.application.dataprovider.spi.LayerStatistics_1_0;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.application.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_6;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the dataprovider builder.
 */
public class Dataprovider_1 
    extends EntityManagerFactory_2
    implements Dataprovider_1_1Connection
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
     * @exception   ServiceException
     *              If the instantiation of the dataprovider fails
     */
    public Dataprovider_1(
        Configuration dataproviderConfiguration,
        ConfigurationProvider_1_0 configurationProvider, 
        Dataprovider_1_0 self
    ) throws ServiceException {
        super(configurationProvider);

        String namespace = super.kernelConfiguration.getFirstValue(
            SharedConfigurationEntries.NAMESPACE_ID
        );
        super.kernelConfiguration.getFirstValue(
            SharedConfigurationEntries.PERSISTENCE_MANAGER_BINDING
        );

        try {
            this.logger.trace (
                "Creating kernel for namespace \"{}\"",
                namespace,
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
            this.model = Model_1Factory.getModel();
            dataproviderConfiguration.values(SharedConfigurationEntries.MODEL).set(0, this.model);
            List<String> modelPackages = new ArrayList<String>();
            for(Object modelPackage : dataproviderConfiguration.values(SharedConfigurationEntries.MODEL_PACKAGE).population()) {
                modelPackages.add((String) modelPackage);
            }
            this.model.addModels(modelPackages);

            // DATAPROVIDER_CONNECTION
            dataproviderConfiguration.values(
                SharedConfigurationEntries.DATAPROVIDER_CONNECTION
            ); // Like that even an initially empty entry is sharable

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
                    "Could not activate " + id + " layer plugin " + className,
                    new BasicException.Parameter("layer",id),
                    new BasicException.Parameter("class",className),
                    new BasicException.Parameter("step",step)
                ).log();
            }
            logger.debug(
                "Kernel for namespace \"{}\" created: {}",
                namespace,
                kernelConfiguration
            );

            this.delegation = layers[DataproviderLayers.INTERCEPTION];

        } catch (Exception exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                "Creation of kernel for namespace \"" + namespace + "\" failed",
                kernelConfiguration.toExceptionParameters()
            ).log();
        }
    }


    //------------------------------------------------------------------------
    // Instance Members
    //------------------------------------------------------------------------

    /**
     * Initialized when the kernel is ready to act as dataprovider connection.
     */
    private Layer_1_0 delegation = null;

    /**
     * The layer instances
     */
    final protected Layer_1_0[] layers = new Layer_1_0[LAYERS];

    /**
     * The logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(Dataprovider_1.class);

    /**
     * The model repository
     */
    private Model_1_6 model;
    
    
    //------------------------------------------------------------------------
    // Class Members
    //------------------------------------------------------------------------

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
    final static Map<String,LayerStatistics_1_0[]> namespaceStatistics = 
        new HashMap<String,LayerStatistics_1_0[]>();

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

    /**
     * Retrieve the model repository
     * 
     * @return the model repository
     */
    protected Model_1_6 getModel(){
        return this.model;
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
        UnitOfWorkRequest... workingUnits
    ){
        this.logger.trace(
            "{} requested by {}",
            Arrays.asList(workingUnits),
            header.getPrincipalChain()
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
                "Could not deactivate " + id + " layer plugin " + className,
                new BasicException.Parameter("layer", id),
                new BasicException.Parameter("class", className)
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

}
