/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1.java,v 1.19 2012/01/05 23:20:20 hburger Exp $
 * Description: The dataprovider kernel
 * Revision:    $Revision: 1.19 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2012/01/05 23:20:20 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2012, OMEX AG, Switzerland
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.openmdx.application.cci.ConfigurationProvider_1_0;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.BeanFactory;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.log.SysLog;
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
            null
        );
        String namespace = this.kernelConfiguration.getFirstValue(
            SharedConfigurationEntries.NAMESPACE_ID
        );
        try {
            SysLog.log(
                Level.FINEST,
                "Creating kernel for namespace \"{0}\": {1}",
                namespace,
                kernelConfiguration
            );
            //
            // NAMESPACE_ID
            //
            dataproviderConfiguration.values(
                SharedConfigurationEntries.NAMESPACE_ID
            ).put(0, namespace);
            //
            // DATAPROVIDER_CONNECTION
            //
            dataproviderConfiguration.values(
                SharedConfigurationEntries.DATAPROVIDER_CONNECTION_FACTORY
            ); // Like that even an initially empty entry is sharable
            //
            // Delegate
            //
            String[] plugIns = kernelConfiguration.getValues("plugIn");
            this.delegate = plugIns.length == 0 ? 
                createDelegateFromLegacyConfiguration(dataproviderConfiguration, configurationProvider) :
                createDelegate(dataproviderConfiguration, configurationProvider, plugIns);
            SysLog.log(
                Level.FINER,
                "Kernel for namespace \"{0}\" created: {}",
                namespace, 
                kernelConfiguration
            );
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

    static private final String[] KERNEL_CONFIGURATION_SECTION = {
        "KERNEL"
    };

    static private final String[] LEGACY_LAYER_NAMES = {
        "PERSISTENCE",
        "MODEL",
        "APPLICATION",
        "TYPE",
        "INTERCEPTION"
    };
    
    /**
     * Initialized when the kernel is ready to act as dataprovider connection.
     */
    private final Layer_1 delegate;

    private final Configuration kernelConfiguration;
    
    /**
     * The 5-layer legacy configuration
     * 
     * @param dataproviderConfiguration
     * @param configurationProvider
     * 
     * @return the delegate
     * 
     * @throws ServiceException 
     */
    private Layer_1 createDelegateFromLegacyConfiguration(
        Configuration dataproviderConfiguration,
        ConfigurationProvider_1_0 configurationProvider
    ) throws ServiceException{
        Layer_1 layer = null;
        //
        // Load
        //
        Map<String,Layer_1> layers = new HashMap<String, Layer_1>();
        for(String layerName : LEGACY_LAYER_NAMES) {
            String className = kernelConfiguration.getFirstValue(layerName);
            try {
                layers.put(
                    layerName, 
                    Classes.<Layer_1>getApplicationClass(className).newInstance()
                );
            } catch (Exception exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ACTIVATION_FAILURE,
                    "Could not create " + layerName + " plugin " + className,
                    new BasicException.Parameter("layer", layerName),
                    new BasicException.Parameter("class", className)
                ).log();
            }
        }
        //
        // Configure
        //
        Map<String,Configuration> layerConfigurations = new HashMap<String, Configuration>();
        for(String layerName : LEGACY_LAYER_NAMES) {
            try {
                Configuration layerConfiguration = configurationProvider.getConfiguration(
                    new String[]{layerName},
                    null
                );
                Map<String,SparseArray<?>> source = dataproviderConfiguration.entries();
                Map<String,SparseArray<?>> target = layerConfiguration.entries();
                for(Map.Entry<String,SparseArray<?>> entry : source.entrySet()) {
                    if(!target.containsKey(entry.getKey())) {
                        target.put(entry.getKey(), entry.getValue());
                    }
                }
                layerConfigurations.put(layerName, layerConfiguration);
            } catch (Exception exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION,
                    "Could not configure " + layerName + " plugin",
                    new BasicException.Parameter("layer", layerName)
                ).log();
            }
        }
        //
        // Activate
        //
        short layerId =  0;
        for(String layerName : LEGACY_LAYER_NAMES) {
            Layer_1 current = layers.get(layerName);
            try {
                current.activate(
                    layerId++,
                    layerConfigurations.get(layerName),
                    layer
                );
                layer = current;
            } catch (Exception exception) {
                String className = current.getClass().getName();
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ACTIVATION_FAILURE,
                    "Could not create " + layerName + " plugin " + className,
                    new BasicException.Parameter("layer", layerName),
                    new BasicException.Parameter("class", className)
                ).log();
            }
        }
        return layer;
    }

    /**
     * The standard plug-ins are Java Beans
     * 
     * @param dataproviderConfiguration
     * @param configurationProvider
     * @param plugIns
     * 
     * @return the delegate
     * @throws ServiceException 
     */
    private Layer_1 createDelegate(
        Configuration dataproviderConfiguration,
        ConfigurationProvider_1_0 configurationProvider,
        String[] plugIns
    ) throws ServiceException{
        Layer_1 layer = null;
        //
        // Load
        //
        Map<String,Layer_1> layers  = new HashMap<String, Layer_1>();
        for(String plugIn : plugIns) {
            layers.put(
                plugIn,
                new BeanFactory<Layer_1>(
                    "class",
                    configurationProvider.getConfiguration(
                        new String[]{plugIn},
                        null
                    ).entries()
                ).instantiate()
            );
        }
        //
        // Activate
        //
        short layerId = 0;
        for(String layerName : plugIns) {
            Layer_1 current = layers.get(layerName);
            current.activate(
                layerId++,
                this.kernelConfiguration,
                layer
            );
            layer = current;
        }
        return layer;
    }
    
    /**
     * Retrieve the delegation stack
     * 
     * @return the entry point for the delegation stack
     */
    public Layer_1 getDelegate(
    ) {
        return this.delegate;
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
        SysLog.log(
            Level.FINEST,
            "{0} requested by {1}",
            requests, header.getPrincipalChain()
        );
        return getDelegate().process(
            header, 
            requests,
            replies
        );
    }

}
