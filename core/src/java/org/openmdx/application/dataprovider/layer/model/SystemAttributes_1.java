/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: SystemAttributes_1.java,v 1.2 2009/06/01 16:02:59 wfro Exp $
 * Description: Handle the BasicObjects' Attributes
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/01 16:02:59 $
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
package org.openmdx.application.dataprovider.layer.model;

import java.util.Date;
import java.util.List;

import javax.resource.cci.MappedRecord;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderOperations;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.spi.BeforeImageCachingLayer_1;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;
import org.openmdx.base.text.format.DateFormat;

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
    public void activate(short id, Configuration configuration, Layer_1_0 delegation) throws ServiceException {
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
        if(at == null) {
            at = DateFormat.getInstance().format(new Date());
        }
        List<String> by = header.getPrincipalChain();
        for(DataproviderRequest request : requests){            
            switch(request.operation()) {
                case DataproviderOperations.OBJECT_CREATION:
                    // exclude Authority, Provider, Segment
                    if(this.isInstanceOfBasicObject(request.object())) {
                        try {
                            ObjectHolder_2Facade facade = ObjectHolder_2Facade.newInstance(request.object());
                            facade.clearAttributeValues(SystemAttributes.CREATED_BY).addAll(
                                limit(by)
                            );
                            facade.clearAttributeValues(SystemAttributes.CREATED_AT).add(
                                at
                            );
                        }
                        catch(Exception e) {
                            throw new ServiceException(e);
                        }
                    }
                    // no break here!         
                case DataproviderOperations.OBJECT_MODIFICATION:
                case DataproviderOperations.OBJECT_REPLACEMENT:
                case DataproviderOperations.OBJECT_SETTING: 
                    // exclude Authority, Provider, Segment
                    if(this.isInstanceOfBasicObject(request.object())) {
                        try {
                            ObjectHolder_2Facade facade = ObjectHolder_2Facade.newInstance(request.object());                        
                            facade.clearAttributeValues(SystemAttributes.MODIFIED_BY).addAll(
                                limit(by)
                            );
                            facade.clearAttributeValues(SystemAttributes.MODIFIED_AT).add(
                                at
                            );
                        }
                        catch(Exception e) {
                            throw new ServiceException(e);
                        }
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
    private List<String> limit(
        List<String> principals
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
        MappedRecord object
    ) throws ServiceException {
        return ObjectHolder_2Facade.getPath(object).size() > 5;
    }

}
