/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Model_1Provider 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland
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
package org.openmdx.application.mof.repository.accessor;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderRequestProcessor;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.loading.Classes;

public class ModelProvider extends Layer_1 {

    /**
     * Constructor 
     *
     * @param plugInChain
     * 
     * @throws Exception
     */
    private ModelProvider(            
        Layer_1 plugInChain
    ) throws Exception {
        this.activate(
            (short)5, 
            new Configuration(), 
            plugInChain
        );
    }

    /**
     * Constructs the in-memory provider for a model repository..
     * 
     * @return a new in-memory provider
     */
    static Dataprovider_1_0 newInstance(
    ) throws ServiceException {
        try {
            Configuration configuration = new Configuration();
            configuration.values(
                SharedConfigurationEntries.NAMESPACE_ID
            ).put(Integer.valueOf(0), "model1");
            Configuration persistenceConfiguration = new Configuration(configuration);
            persistenceConfiguration.values(
                org.openmdx.application.dataprovider.layer.persistence.none.LayerConfigurationEntries.CLONE_REPLY
            ).put(Integer.valueOf(0), Boolean.FALSE);
            Layer_1 persistencePlugin = Classes.<Layer_1>getApplicationClass(
                org.openmdx.application.dataprovider.layer.persistence.none.InMemory_1.class.getName()
            ).newInstance();
            persistencePlugin.activate((short)0, persistenceConfiguration, null);
            Layer_1 modelPlugin = Classes.<Layer_1>getApplicationClass(
                org.openmdx.application.mof.repository.layer.model.Model_1.class.getName()
            ).newInstance();
            modelPlugin.activate((short)1, configuration, persistencePlugin);
            Layer_1 applicationPlugin = Classes.<Layer_1>getApplicationClass(
                org.openmdx.application.mof.repository.layer.application.Model_1.class.getName()
            ).newInstance();
            applicationPlugin.activate((short)2, configuration, modelPlugin);
            Layer_1 typePlugin = Classes.<Layer_1>getApplicationClass(
                org.openmdx.application.mof.repository.layer.type.Model_1.class.getName()
            ).newInstance();
            typePlugin.activate((short)3, configuration, applicationPlugin);
            Layer_1 interceptionPlugin = Classes.<Layer_1>getApplicationClass(
                org.openmdx.application.dataprovider.layer.interception.Standard_1.class.getName()
            ).newInstance();
            interceptionPlugin.activate((short)4, configuration, typePlugin);
            return new ModelProvider(interceptionPlugin);
        } catch(ServiceException e) {
            throw e;
        } catch(Exception e) {
            throw new RuntimeServiceException(e);
        }
    }
 
    static DataproviderRequestProcessor createChannel(
        Dataprovider_1_0 modelProvider
    ){
        return new DataproviderRequestProcessor(
            new ServiceHeader(null, null, false),
            modelProvider
        );
        
    }
    
}