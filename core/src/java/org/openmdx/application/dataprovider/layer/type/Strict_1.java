/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Strict TYPE Plug-In
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license  as listed below.
 * 
 * Copyright (c) 2008-2013, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.layer.type;

import java.util.Collection;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.cci.Persistency;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;


/**
 * Strict TYPE Plug-In
 */
public class Strict_1 extends Standard_1 {

    @SuppressWarnings("unchecked")
    private void validateMultiplicity(
        Object_2Facade objectFacade,
        ModelElement_1_0 classDef, 
        boolean create
    ) throws ServiceException{
        if(classDef != null) {
            final MappedRecord valueMap = objectFacade.getValue();
            final Persistency persistency = Persistency.getInstance();
            for(Map.Entry<String,ModelElement_1_0> feature : ((Map<String,ModelElement_1_0>)classDef.objGetMap("allFeature")).entrySet()) {
                final ModelElement_1_0 featureDef = feature.getValue();
                if(
                    persistency.isPersistentAttribute(featureDef) &&
                    !ModelHelper.isDerived(featureDef)
                ){
                    final String featureName = feature.getKey();
                    if(!ModelHelper.isFeatureHeldByCore(classDef, featureName)){
                        if(create || valueMap.containsKey(featureName)) {
                            final Multiplicity multiplicity = ModelHelper.getMultiplicity(featureDef);
                            if(multiplicity.isSingleValued()) {
                                final Object value = valueMap.get(featureName);
                                if(value == null) {
                                    if(multiplicity == Multiplicity.SINGLE_VALUE) {
                                        Model_1Factory.getModel(true);
                                        throw new ServiceException(
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.BAD_PARAMETER,
                                            "UNRECOVERABLE MULTIPLICITY FAILURE: Missing mandatory value",
                                            new BasicException.Parameter("xri", objectFacade.getPath()),
                                            new BasicException.Parameter("modelClass", objectFacade.getObjectClass()),
                                            new BasicException.Parameter("featureName", featureName),
                                            new BasicException.Parameter("value", value)
                                        ).log();
                                    }
                                } else if (value instanceof Collection) {
                                    final Collection<?> collection = (Collection<?>)value;
                                    Object singleton = null;
                                    int index = 0;
                                    for(Object element : collection) {
                                        if(index++ == 0) {
                                            singleton = element;
                                        } else if (element != null) {
                                            if(singleton == null) {
                                                singleton = element;
                                            } else if (!singleton.equals(element)) {
                                                Model_1Factory.getModel(true);
                                                throw new ServiceException(
                                                    BasicException.Code.DEFAULT_DOMAIN,
                                                    BasicException.Code.BAD_PARAMETER,
                                                    "UNRECOVERABLE MULTIPLICITY FAILURE: Too many values for a single valued attribute",
                                                    new BasicException.Parameter("xri", objectFacade.getPath()),
                                                    new BasicException.Parameter("modelClass", objectFacade.getObjectClass()),
                                                    new BasicException.Parameter("featureName", featureName),
                                                    new BasicException.Parameter("value", collection)
                                                ).log();
                                            }
                                        }
                                    }
                                    if(singleton == null && multiplicity == Multiplicity.SINGLE_VALUE) {
                                        Model_1Factory.getModel(true);
                                        throw new ServiceException(
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.BAD_PARAMETER,
                                            "UNRECOVERABLE MULTIPLICITY FAILURE: Missing mandatory value",
                                            new BasicException.Parameter("xri", objectFacade.getPath()),
                                            new BasicException.Parameter("modelClass", objectFacade.getObjectClass()),
                                            new BasicException.Parameter("featureName", featureName),
                                            new BasicException.Parameter("value", collection)
                                        ).log();
                                    }
                                    if(index > 1) {
                                        Model_1Factory.getModel(true);
                                        new ServiceException(
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.BAD_PARAMETER,
                                            "RECOVERABLE MULTIPLICITY FAILURE: Too many values for a single valued feature",
                                            new BasicException.Parameter("xri", objectFacade.getPath()),
                                            new BasicException.Parameter("modelClass", objectFacade.getObjectClass()),
                                            new BasicException.Parameter("featureName", featureName),
                                            new BasicException.Parameter("value", collection)
                                        ).log();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.type.Standard_1#prepareAndValidateCreateRequest(org.openmdx.base.rest.spi.Object_2Facade, org.openmdx.base.mof.cci.ModelElement_1_0)
     */
    @Override
    protected void prepareAndValidateCreateRequest(
        Object_2Facade objectFacade,
        ModelElement_1_0 classDef
    ) throws ServiceException {
      
        
        validateMultiplicity(objectFacade, classDef, true);
        super.prepareAndValidateCreateRequest(objectFacade, classDef);
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.type.Standard_1#prepareAndValidatePutRequest(org.openmdx.base.rest.spi.Object_2Facade, org.openmdx.base.mof.cci.ModelElement_1_0)
     */
    @Override
    protected void prepareAndValidatePutRequest(
        Object_2Facade objectFacade,
        ModelElement_1_0 classDef
    ) throws ServiceException {
        validateMultiplicity(objectFacade, classDef, false);
        super.prepareAndValidatePutRequest(objectFacade, classDef);
    }

    @Override
    public Interaction getInteraction(
        RestConnection connection
    ) throws ResourceException {
        return new LayerInteraction(connection);
    }

    //------------------------------------------------------------------------
    // Class LayerInteraction
    //------------------------------------------------------------------------

    public class LayerInteraction extends Standard_1.LayerInteraction {
        
        public LayerInteraction(
            RestConnection connection
        ) throws ResourceException {
            super(connection);
        }
    }
    
}
