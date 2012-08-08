/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SystemAttributes_1.java,v 1.6 2006/07/24 09:03:38 hburger Exp $
 * Description: Handle the BasicObjects' Attributes
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/07/24 09:03:38 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.compatibility.base.dataprovider.layer.model;

import java.util.Date;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.spi.BeforeImageCachingLayer_1;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;

/**
 * Handle the BasicObjects' Attributes.
 */
public class SystemAttributes_1
    extends BeforeImageCachingLayer_1 
{

    /**
     * Defines the maximum length for createdBy's and modifiedBy's principal 
     * chains. 
     */
    private int principalLimit;
    
    
    //--------------------------------------------------------------------------
    // Implements Layer_1_0
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.BeforeImageCachingLayer_1#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
    public void activate(short id, Configuration configuration, Layer_1_0 delegation) throws Exception, ServiceException {
        super.activate(id, configuration, delegation);
        this.principalLimit = configuration.containsEntry(LayerConfigurationEntries.PRINCIPAL_LIMIT) ? 
            ((Integer) configuration.values(LayerConfigurationEntries.PRINCIPAL_LIMIT).get(0)).intValue() : 
            Integer.MAX_VALUE;
    }

    /**
     * Set the corresponding system attributes for write operations. 
     *
     * @param         header
     *                the requests' service header
     * @param     requests
     *                the request list
     *
     * @exception ServiceException
     *                on failure
     */
    public void prolog(
        ServiceHeader header,
        DataproviderRequest[] requests
    ) throws ServiceException {
        String at = header.getRequestedAt();
        if(at == null) at = DateFormat.getInstance().format(new Date());
        List by = header.getPrincipalChain();
        for(
            int index = 0;
            index < requests.length;
            index++
        ){
            DataproviderRequest request = requests[index];
            switch(request.operation()) {
                case DataproviderOperations.OBJECT_CREATION:
                    // exclude Authority, Provider, Segment
                    if(isInstanceOfBasicObject(request.object())) {
                      request.object().clearValues(
                          SystemAttributes.CREATED_BY
                      ).addAll(
                          limit(by)
                      );
                      request.object().clearValues(
                          SystemAttributes.CREATED_AT
                      ).add(at);
                    }
                    // no break here!         
                case DataproviderOperations.OBJECT_MODIFICATION:
                case DataproviderOperations.OBJECT_REPLACEMENT:
                case DataproviderOperations.OBJECT_SETTING: 
                    // exclude Authority, Provider, Segment
                    if(isInstanceOfBasicObject(request.object())) {
                      request.object().clearValues(
                          SystemAttributes.MODIFIED_BY
                      ).addAll(
                          limit(by)
                      );
                      request.object().clearValues(
                          SystemAttributes.MODIFIED_AT
                      ).add(at);
                    }
                    break;
            }
        }
        super.prolog(header, requests);
    }

    /**
     * Truncates the principal chain if it is larger than principalLimit.
     * 
     * @param principals the original principal chain
     * 
     * @return the - maybe truncated - principal chain
     */
    private List limit(
        List principals
    ){
        return this.principalLimit < principals.size() ?
            principals.subList(0, this.principalLimit) :
            principals;
    }
    
    /**
     * Tells whether an object is an instance of BasicObject
     * <p>
     * This model-unaware implementation can be overridden by a sub-class.
     * 
     * @param object
     * 
     * @return true if the object is an instance of BasicObject
     * 
     * @exception ServiceException if the model element is not found
     */
    protected boolean isInstanceOfBasicObject(
        DataproviderObject_1_0 object
    ) throws ServiceException {
        return object.path().size() > 5;
    }

}
