/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: XML Importer
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2011, OMEX AG, Switzerland
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
package org.openmdx.application.xml.spi;

import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderRequestProcessor;
import org.openmdx.application.dataprovider.cci.RequestedObject;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;

/**
 * Dataprovider Target
 */
public class Dataprovider_1Target implements ImportTarget {

    /**
     * Constructor
     * 
     * @param target
     */
    public Dataprovider_1Target(
        DataproviderRequestProcessor target
    ) {
        this.target = target;
        this.target2 = target.clone();
    }

    /**
     * The delegate
     */
    private final DataproviderRequestProcessor target;
    private final DataproviderRequestProcessor target2; // for retrieval

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.application.xml.ImportTarget#importObject(org.openmdx
     * .application.xml.ImportTarget.Mode, javax.resource.cci.MappedRecord)
     */
    @Override
    public void importObject(
        ImportMode mode, 
        ObjectRecord objectHolder
    ) throws ServiceException {
        switch (mode) {
            case UPDATE:
                Facades.asObject(objectHolder).setVersion(
                    Object_2Facade.getVersion(
                        this.target.addGetRequest(
                            Object_2Facade.getPath(objectHolder)
                        )
                    )
                );
                this.target.addReplaceRequest(
                    objectHolder,
                    AttributeSelectors.NO_ATTRIBUTES,
                    null // attributeSpecifier
                );
                break;
            case SET:
                RequestedObject result = new RequestedObject();
                this.target2.addGetRequest(
                    Object_2Facade.getPath(objectHolder),
                    AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                    null,
                    result
                );
                if(result.getException() != null && result.getException().getExceptionCode() != BasicException.Code.NOT_FOUND) {
                    throw result.getException();
                }
                if(result.getObject(false) == null) {
                    this.target.addCreateRequest(
                        objectHolder,
                        AttributeSelectors.NO_ATTRIBUTES,
                        null // attributeSpecifier
                    );
                } else {
                    this.target.addReplaceRequest(
                        objectHolder,
                        AttributeSelectors.NO_ATTRIBUTES,
                        null // attributeSpecifier
                    );
                }
                break;
            case CREATE:
                this.target.addCreateRequest(
                    objectHolder,
                    AttributeSelectors.NO_ATTRIBUTES,
                    null // attributeSpecifier
                );
                break;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ImportTarget#importEpilog()
     */
    @Override
    public void importEpilog(
        boolean successful
    ) throws ServiceException {
        if(successful) {
            this.target.endBatch();
        } else {
            this.target.forgetBatch();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ImportTarget#importProlog()
     */
    @Override
    public void importProlog(
    ) throws ServiceException {
        this.target.beginBatch();
    }

}