/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: FinalClassPlugIn_2.java,v 1.2 2009/06/01 15:42:15 wfro Exp $
 * Description: Final Class Plug-In 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/01 15:42:15 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.application.rest.spi;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;

import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;
import org.openmdx.base.rest.spi.RestPlugIn;
import org.openmdx.kernel.exception.BasicException;


/**
 * Final Class Plug-In
 */
public class FinalClassPlugIn_2 implements RestPlugIn {

    /**
     * 
     */
    public static final Path[] PATTERN = {
        new Path("xri://@openmdx*($..)"),
        new Path("xri://@openmdx*($..)/provider/($..)"),
        new Path("xri://@openmdx*org.openmdx.compatibility.state1/provider/-/segment/-")
    };

    /**
     * 
     */
    private static final String[] TYPE = {
        "org:openmdx:base:Authority",
        "org:openmdx:base:Provider",
        "org:openmdx:compatibility:state1:Segment"
    };

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestConnection#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
     */
    public Record execute(
        InteractionSpec ispec, 
        Record input
    ) throws ResourceException {
        RestInteractionSpec interactionSpec = (RestInteractionSpec) ispec;
        switch(interactionSpec.getFunction()) {
            case GET: 
                if(input instanceof IndexedRecord) {
                    IndexedRecord record = (IndexedRecord) input;
                    if(record.size() == 1) {
                        Path xri = new Path(record.get(0).toString());
                        for(
                            int i = 0;
                            i < PATTERN.length;
                            i++
                        ){
                            if(xri.isLike(PATTERN[i])){
                                ObjectHolder_2Facade facade = ObjectHolder_2Facade.newInstance();
                                facade.setPath(xri);
                                facade.setValue(Records.getRecordFactory().createMappedRecord(TYPE[i]));
                                return Records.getRecordFactory().singletonIndexedRecord(
                                    Multiplicities.LIST,
                                    null,
                                    facade.getDelegate()
                                );
                            }
                        }
                    } // else TODO
                }
                throw new ResourceException(
                    "The virtual connection does not support the given request",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_IMPLEMENTED,
                        new BasicException.Parameter("input", input)
                    )
                );
            default: throw new ResourceException(
                "The virtual connection supports GET only",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_IMPLEMENTED
                )
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestConnection#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
     */
    public boolean execute(
        InteractionSpec ispec,
        Record input,
        Record output
    ) throws ResourceException {
        throw new ResourceException(
            "Execute with output record not yet implemented",
            BasicException.newEmbeddedExceptionStack(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_IMPLEMENTED
            )
        ); // TODO 
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestConnection#getMetaData()
     */
    public ConnectionMetaData getMetaData(
    ) throws ResourceException {
        return null; // no need to provide meta data for a plug-in
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestPlugIn#setNext(javax.resource.cci.Connection)
     */
    public void setNext(Connection next) {
        // This plug-in has no provider of its own
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestPlugIn#setSame(javax.resource.cci.Connection)
     */
    public void setSame(Connection same) {
        // This plug-in has no need to delegate to the same level
    }

    
}
