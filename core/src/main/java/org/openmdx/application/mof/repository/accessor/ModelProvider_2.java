/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Model Provider 
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
package org.openmdx.application.mof.repository.accessor;

import java.util.Collections;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.cci.Interaction;

import org.openmdx.base.dataprovider.kernel.Dataprovider_2;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.spi.AbstractRestPort;
import org.openmdx.kernel.loading.Resources;

public class ModelProvider_2 extends AbstractRestPort {

	/**
	 * Constructor_
	 */
	private ModelProvider_2(
		Port<RestConnection> delegate
	){
		setDelegate(delegate);
	}

	private static final String MODEL_DATAPROVIDER_CONFIGURATION_URI = Resources.toResourceXRI(
		"org/openmdx/application/mof/repository/accessor/model-dataprovider.properties"
	);
	private static final String META_MODEL_DATAPROVIDER_CONFIGURATION_URI = Resources.toResourceXRI(
		"org/openmdx/application/mof/repository/accessor/meta-model-dataprovider.properties"
	);
	
	private static Port<RestConnection> newDelegate(
		boolean metaModel
	){
		return new Dataprovider_2(
			metaModel ? META_MODEL_DATAPROVIDER_CONFIGURATION_URI : MODEL_DATAPROVIDER_CONFIGURATION_URI
		);
	}

	public static Port<RestConnection> newModelExternalizationDataprovider(
		String openmdxjdoMetadataDirectory
	){
		return new Dataprovider_2(
			META_MODEL_DATAPROVIDER_CONFIGURATION_URI,
			Collections.<String,Map<String,?>>singletonMap(
				"APPLICATION",
				Collections.singletonMap(
					"openmdxjdoMetadataDirectory", 
					openmdxjdoMetadataDirectory
				)
			)	
		);
	}
	
    /**
     * Constructs the in-memory provider for a model repository..
     * 
     * @return a new in-memory provider
     */
    static Port<RestConnection> newInstance(
    	boolean metaModel
    ){
    	return new ModelProvider_2(
    		newDelegate(metaModel)
    	);
    }
 
	@Override
	public Interaction getInteraction(
		RestConnection connection
	) throws ResourceException {
		return newDelegateInteraction(connection);
	}
    
}