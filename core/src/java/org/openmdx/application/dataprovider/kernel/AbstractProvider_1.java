/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractProvider_1.java,v 1.1 2009/01/05 13:44:55 wfro Exp $
 * Description: EntityProvider_2 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/05 13:44:55 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
import java.util.Map;

import org.openmdx.application.cci.ConfigurationProvider_1_0;
import org.openmdx.application.cci.ConfigurationSpecifier;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderLayers;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.base.exception.ServiceException;

/**
 * EntityProvider_2
 */
class AbstractProvider_1 {

    /**
     * Constructor 
     *
     * @param configurationProvider
     * @throws ServiceException 
     */
    AbstractProvider_1(
        ConfigurationProvider_1_0 configurationProvider
    ) throws ServiceException{
        this.kernelConfiguration = configurationProvider.getConfiguration(
            KERNEL_CONFIGURATION_SECTION,
            kernelConfigurationSpecification
        );
    }

    /**
     * 
     */
    protected final Configuration kernelConfiguration;
    
    /**
    *
    */
   static private final String[] KERNEL_CONFIGURATION_SECTION = {"KERNEL"};

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
