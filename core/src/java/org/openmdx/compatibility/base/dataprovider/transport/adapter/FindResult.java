/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: FindResult.java,v 1.8 2008/03/07 03:25:09 hburger Exp $
 * Description: Dataprovider Adapter: Find Result
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/07 03:25:09 $
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
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.compatibility.base.dataprovider.transport.adapter;

import java.io.InputStream;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObjectMarshaller_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.IterationProcessor;
import org.openmdx.compatibility.base.dataprovider.cci.MarshallingRequestedList;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Manager_1_0;
import org.openmdx.compatibility.base.naming.Path;

/**
 * Dataprovider Adapter
 * Find Result
 */
class FindResult extends MarshallingRequestedList {

    /**
     * 
     */
    private static final long serialVersionUID = 3546920264634873912L;

    /**
     * Constructor
     * 
     * @param iterationProcessor
     * @param marshaller
     */
    FindResult(
        IterationProcessor iterationProcessor, 
        Path referenceFilter,
        DataproviderObjectMarshaller_1_0 marshaller,
        Manager_1_0 manager
    ) {
        super(iterationProcessor, referenceFilter, marshaller);
        this.manager = manager;
    }

    //------------------------------------------------------------------------
    // Implements Reconstructable
    //------------------------------------------------------------------------

    /**
     * Constructor
     */ 
    FindResult(
        IterationProcessor iterationProcessor,
        Path referenceFilter,
        DataproviderObjectMarshaller_1_0 marshaller,
        Manager_1_0 manager,
        InputStream stream
    ) throws ServiceException {
        super(iterationProcessor,referenceFilter,marshaller,stream);
        this.manager = manager;
    }


    //------------------------------------------------------------------------
    // Extends RequestedList
    //------------------------------------------------------------------------

    /**
     * Called if the work unit has been processed successfully
     */
    protected void onReplyInterceptor(
        DataproviderReply reply
    ){
        DataproviderObject_1_0[] fetched = reply.getObjects();
        if(
            this.manager != null
        ) for(
            int i = 0;
            i < fetched.length;
            i++ 
        ) try {
            DataproviderObject_1_0 source = fetched[i];
            if(source.path().isLike(EXTENT_PATTERN)) source.path().setTo(
                new Path(source.path().getBase())
            );
            manager.fetched(
                source.path(), 
                Marshaller.toMappedRecord(source)
            );
        } catch (ServiceException exception) {
            exception.log();
        }
        super.onReplyInterceptor(reply);
    }
    
        
    //------------------------------------------------------------------------
    // Instance members
    //------------------------------------------------------------------------

    /**
     * 
     */
    final protected Manager_1_0 manager;

    /**
     * 
     */
    static final Path EXTENT_PATTERN = new Path(
        "xri:@openmdx:*/provider/:*/segment/:*/extent/:*"
    );
    
}
