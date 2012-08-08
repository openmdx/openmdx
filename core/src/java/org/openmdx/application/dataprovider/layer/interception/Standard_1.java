/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Standard_1.java,v 1.3 2009/06/04 14:47:06 hburger Exp $
 * Description: Standard Transport Layer Plug-In
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/04 14:47:06 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.application.dataprovider.layer.interception;

import javax.resource.cci.MappedRecord;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderOperations;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;

/**
 * The standard implementation of the interception layer's plug-in.
 */
public class Standard_1
    extends SystemAttributes_1 
{

    //--------------------------------------------------------------------------
    // Implements Layer_1_0
    //--------------------------------------------------------------------------

    /**
     * Activates a dataprovider layer
     * 
     * @param   id              the dataprovider layer's id
     * @param   configuration   the dataprovider'a configuration
     *  <dl>
     *      <dt>namespaceId</dt>            <dd>String</dd>
     *      <dt>exposedPath</dt>            <dd>Path[]</dd>
     *      <dt>propagateSet</dt>           <dd>Boolean</dd>
     *  </dl>
     * @param       delegation
     *              the layer to delegate to;
     *              or null if "persistenceLayer".equals(id)
     *
     * @exception   ServiceException
     *              expected exceptions
     * @exception   Exception
     *              unexpected exceptions
     *
     */
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws ServiceException {
        super.activate(id,configuration,delegation);
        this.interceptSet = ! configuration.isOn(LayerConfigurationEntries.PROPAGATE_SET);
    }

    /**
     * Set the corresponding system attributes for write operations. 
     *
     * @param       header
     *              the requests' service header
     * @param       requests
     *              the request list
     *
     * @exception   ServiceException
     *              on failure
     */
    public void prolog(
        ServiceHeader header,
        DataproviderRequest[] requests
    ) throws ServiceException {
        super.prolog(header, requests);
        if(this.interceptSet)for(
            int index = 0;
            index < requests.length;
            index++
        ){
            DataproviderRequest request = requests[index];
            MappedRecord afterImage = request.object();
            try {
                ObjectHolder_2Facade afterImageFacade = ObjectHolder_2Facade.newInstance(afterImage);
                if(request.operation()== DataproviderOperations.OBJECT_SETTING) {
                    try {
                        ObjectHolder_2Facade beforeImageFacade = ObjectHolder_2Facade.newInstance(
                            this.getBeforeImage(header, request)
                        );
                        afterImageFacade.setVersion(
                            beforeImageFacade.getVersion()
                        );
                    } 
                    catch (ServiceException exception) {
                        afterImageFacade.clearAttributeValues(SystemAttributes.CREATED_BY).add(
                            afterImageFacade.attributeValues(SystemAttributes.MODIFIED_BY)
                        );
                        afterImageFacade.clearAttributeValues(SystemAttributes.CREATED_AT).add(
                            afterImageFacade.attributeValues(SystemAttributes.MODIFIED_AT)
                        );
                    }
                }
            }
            catch(Exception e) {
                throw new ServiceException(e);
            }
        }
    }

    /**
     * Creates an object or modifies all its changeable attributes if it
     * already exists.
     *
     * @param       header
     *              request header
     * @param       request
     *              the request
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    public DataproviderReply set(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return this.interceptSet ? (
            hasBeforeImage(request) ? 
                replace(header,request) :
                create(header,request)
            ) :
            super.set(header,request);
    }


    //--------------------------------------------------------------------------
    // Instance members
    //--------------------------------------------------------------------------

    /**
     * false => intercept set operations
     * true => propagate set operations
     */
    private boolean interceptSet = false;
        
}
