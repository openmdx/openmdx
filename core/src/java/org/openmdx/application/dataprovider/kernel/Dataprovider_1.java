/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1.java,v 1.16 2010/04/16 09:46:58 hburger Exp $
 * Description: The dataprovider kernel
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/16 09:46:58 $
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openmdx.application.cci.ConfigurationProvider_1_0;
import org.openmdx.application.cci.ConfigurationSpecifier;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderLayers;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.log.LoggerFactory;
import org.w3c.cci2.SparseArray;

public class Dataprovider_1 implements Dataprovider_1_0 {

    /**
     * Constructor
     * 
     * @param       dataproviderConfiguration
     *              The configuration shared by all layers
     * @param       configurationProvider
     *              The configurationProvider provides the configurations for
     *              the kernel and the individual layers.
     * @exception   ServiceException
     *              If the instantiation of the dataprovider fails
     */
    public Dataprovider_1(
        Configuration dataproviderConfiguration,
        ConfigurationProvider_1_0 configurationProvider
    ) throws ServiceException {
        this.kernelConfiguration = configurationProvider.getConfiguration(
            KERNEL_CONFIGURATION_SECTION,
            kernelConfigurationSpecification
        );
        String namespace = this.kernelConfiguration.getFirstValue(
            SharedConfigurationEntries.NAMESPACE_ID
        );
        this.kernelConfiguration.getFirstValue(
            SharedConfigurationEntries.PERSISTENCE_MANAGER_BINDING
        );

        try {
            this.logger.log(
                Level.FINEST,
                "Creating kernel for namespace \"{0}\": {1}",
                new Object[]{
                    namespace,
                    kernelConfiguration
                }
            );

            // NAMESPACE_ID
            dataproviderConfiguration.values(
                SharedConfigurationEntries.NAMESPACE_ID
            ).put(0, namespace);

            // EXPOSED_PATH
            SparseArray<Path> exposedPaths = dataproviderConfiguration.values(
                SharedConfigurationEntries.EXPOSED_PATH
            );
            for(
                ListIterator<?> iterator = kernelConfiguration.values(SharedConfigurationEntries.EXPOSED_PATH).populationIterator();
                iterator.hasNext();
            ) {
                exposedPaths.put(
                    iterator.nextIndex(),
                    toPath(iterator.next())
                );
            }

            // DELEGATION_PATH
            SparseArray<Path> delegationPaths = dataproviderConfiguration.values(
                SharedConfigurationEntries.DELEGATION_PATH
            );
            for(
                    ListIterator<?> iterator = kernelConfiguration.values(
                        SharedConfigurationEntries.DELEGATION_PATH
                    ).populationIterator();
                    iterator.hasNext();
            ) delegationPaths.put(
                iterator.nextIndex(),
                toPath(iterator.next())
            );

            // MODEL
            dataproviderConfiguration.values(
                SharedConfigurationEntries.MODEL_PACKAGE
            ).putAll(
                kernelConfiguration.values(SharedConfigurationEntries.MODEL_PACKAGE)
            );
            dataproviderConfiguration.values(
                SharedConfigurationEntries.PACKAGE_IMPL
            ).putAll(
                kernelConfiguration.values(SharedConfigurationEntries.PACKAGE_IMPL)
            );
            List<String> modelPackages = new ArrayList<String>();
            for(Object modelPackage : dataproviderConfiguration.values(SharedConfigurationEntries.MODEL_PACKAGE).values()) {
                modelPackages.add((String) modelPackage);
            }
            if(!modelPackages.isEmpty()) {
                Model_1Factory.getModel().addModels(modelPackages);
            }

            // DATAPROVIDER_CONNECTION
            dataproviderConfiguration.values(
                SharedConfigurationEntries.DATAPROVIDER_CONNECTION_FACTORY
            ); // Like that even an initially empty entry is sharable

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
                        layers[layer]=Classes.<Layer_1>getApplicationClass(
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
                        Map<String,SparseArray<?>> source = dataproviderConfiguration.entries();
                        Map<String,SparseArray<?>> target = layerConfiguration.entries();
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
            this.logger.log(
                Level.FINER,
                "Kernel for namespace \"{0}\" created: {}",
                new Object[]{namespace,kernelConfiguration}
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
    public Layer_1 getDelegate(
    ) {
        return this.delegation;
    }
    
    //------------------------------------------------------------------------
    // Instance Members
    //------------------------------------------------------------------------

    /**
     * Initialized when the kernel is ready to act as dataprovider connection.
     */
    private Layer_1 delegation = null;

    /**
     * The layer instances
     */
    final protected Layer_1[] layers = new Layer_1[LAYERS];

    /**
     * The logger instance
     */
    private final Logger logger = LoggerFactory.getLogger();

    
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
     * Process a set of working units
     *
     * @param    header            the service header
     * @param    workingUnits    a collection of working units
     *
     * @return    a collection of working unit replies
     */
    public ServiceException process(
        ServiceHeader header,
        List<DataproviderRequest> requests,
        List<DataproviderReply> replies
    ){
        this.logger.log(
            Level.FINEST,
            "{0} requested by {1}",
            new Object[]{requests, header.getPrincipalChain()}
        );
        return delegation.process(
            header, 
            requests,
            replies
        );
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    static private final String[] KERNEL_CONFIGURATION_SECTION = {"KERNEL"};

    protected final Configuration kernelConfiguration;
    
    /**
     * The kernel's specific configuration specifiers.
     */
    private final static  Map<String,ConfigurationSpecifier> kernelConfigurationSpecification = 
        new HashMap<String,ConfigurationSpecifier>();
    
    static {
        for(
            int layer = DataproviderLayers.PERSISTENCE;
            layer <= DataproviderLayers.INTERCEPTION;
            layer++
        ) kernelConfigurationSpecification.put(
            DataproviderLayers.toString(layer),
            new ConfigurationSpecifier (
                DataproviderLayers.toString(layer) + " layer plug-in class",
                true, 1, 1
            )
        );
        kernelConfigurationSpecification.put(
            SharedConfigurationEntries.NAMESPACE_ID,
              new ConfigurationSpecifier (
                "The namespace ID. Where <value> is a simple string",
                true, 1, 1
            )
        );
        kernelConfigurationSpecification.put(
            SharedConfigurationEntries.EXPOSED_PATH,
            new ConfigurationSpecifier (
                "A requests is not accepted " +
                    " unless its path starts with one of the exposed ones",
                true, 1, 100
            )
        );
        kernelConfigurationSpecification.put(
            SharedConfigurationEntries.DELEGATION_PATH,
            new ConfigurationSpecifier (
                "Requests to be delegated use the dataprovider with the same " +
                    "index as its matching delegation path.",
                false, 1, 100
            )
        );
        kernelConfigurationSpecification.put(
            SharedConfigurationEntries.MODEL_PACKAGE,
            new ConfigurationSpecifier (
                "Optional model packages specified as full qualified class names.",
                true, 0, 100
              )
          );
        kernelConfigurationSpecification.put(
            SharedConfigurationEntries.PERSISTENCE_MANAGER_BINDING,
            new ConfigurationSpecifier (
                "The persistence manager bindig, e.g. cci2, jmi1,...",
                false, 0, 1
              )
          );
    }
        
}
