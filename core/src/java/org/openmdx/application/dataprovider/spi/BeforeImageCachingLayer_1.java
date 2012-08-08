/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: BeforeImageCachingLayer_1.java,v 1.3 2009/06/01 15:39:40 wfro Exp $
 * Description: BeforeImageCachingLayer_1
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/01 15:39:40 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.application.dataprovider.spi;

import javax.resource.cci.MappedRecord;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderOperations;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.DataproviderRequestContexts;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;


/**
 * BeforeImageCachingLayer_1
 */
public class BeforeImageCachingLayer_1
    extends Layer_1 
{

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
    public void activate(
        short id, 
        Configuration configuration, 
        Layer_1_0 delegation
    ) throws ServiceException {
        super.activate(id, configuration, delegation);
        this.beforeImageContextName = DataproviderRequestContexts.BEFORE_IMAGE + getId();
    }

    /**
     * Retrieve an object's before image
     * 
     * @param header
     * @param request
     * 
     * @return an object's before image
     * 
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    protected MappedRecord getBeforeImage(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        SparseList beforeImageContext = request.context(this.beforeImageContextName);
        MappedRecord beforeImage = (MappedRecord)beforeImageContext.get(0);
        if(beforeImage == null) {
            try {
                beforeImageContext.set(
                    0,
                    beforeImage = this.getDelegation().get(
                        header,
                        new DataproviderRequest(
                            ObjectHolder_2Facade.newInstance(request.path()).getDelegate(),
                            DataproviderOperations.OBJECT_RETRIEVAL,
                            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                            null
                        )
                    ).getObject()
                );
            }
            catch(Exception e) {
                throw new ServiceException(e);
            }
        }
        return beforeImage;
    }

    /**
     * Tests whether a before image is attachd to a given request
     * 
     * @param request
     * 
     * @return true if a before image has been attached to the given request
     */
    protected boolean hasBeforeImage(
        DataproviderRequest request
    ){
        return !request.context(this.beforeImageContextName).isEmpty();
    }
    
    /**
     * The layer specific before image context name
     */
    private String beforeImageContextName;
    
}
