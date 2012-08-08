/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Runtime_1.java,v 1.11 2008/02/29 15:23:04 hburger Exp $
 * Description: Standard Transport Layer Plug-In
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/29 15:23:04 $
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
package org.openmdx.compatibility.base.dataprovider.layer.interception;

import java.util.Date;
import java.util.Iterator;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.application.spi.AbstractApplicationContext_1;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.naming.Path;

/**
 * The standard implementation of the Transport layer's plug-in.
 */
public class Runtime_1
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
    ) throws Exception {
        super.activate(id,configuration,delegation);
        this.activatedAt = DateFormat.getInstance().format(new Date());
        this.domainName = AbstractApplicationContext_1.hasInstance() ?
                AbstractApplicationContext_1.getInstance().getDomainName() :
                "-";
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
        for( // Once upon a time Strict_1 couldn't handle more than one model at once
            int index = 0;
            index < requests.length;
            index++
        ) if(requests[index].path().startsWith(RUNTIME_AUTHORITY)) return;
        super.prolog(header,requests);
    }

    /**
     * This method allows the dataprovider layers postprocessing of a 
     * collection of requests as a whole after the actual processing of the 
     * individual requests has been done.
     *
     * @param       header
     *              the requests' service header
     * @param       requests
     *              the request collection
     * @param       replies
     *              the reply collection
     *
     * @exception   ServiceException
     *              must not be thrown unless the request collection is to be 
     *              treated as an atomic processing unit
     */
    public void epilog(
        ServiceHeader header,
        DataproviderRequest[] requests,
        DataproviderReply[] replies
    ) throws ServiceException {
        for( // Once upon a time Strict_1 couldn't handle more than one model at once
            int index = 0;
            index < requests.length;
            index++
        ) if(requests[index].path().startsWith(RUNTIME_AUTHORITY)) return;
        super.epilog(header, requests, replies);
    }

    /**
     * Get the objects specified by the references and filter properties.
     *
     * @param       request
     *              the request
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    @SuppressWarnings("unchecked")
    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        if(request.path().startsWith(RUNTIME_AUTHORITY)) {
            switch(request.path().size()) {
                case 6: {// Domain
                    DataproviderObject result = new DataproviderObject(
                        request.path().getChild(this.domainName)
                    );
                    result.values(SystemAttributes.OBJECT_CLASS).add(
                        "org:openmdx:compatibility:runtime1:Domain"
                    );
                    result.values(SystemAttributes.CREATED_AT).add(
                        this.activatedAt
                    );
                    result.values(SystemAttributes.MODIFIED_AT).add(
                        this.activatedAt
                    );
                    return new DataproviderReply(result);
                }
                case 8: {// Namespace
                    DataproviderObject result = new DataproviderObject(
                        request.path().getChild(
                            (String)getConfiguration().values(
                                LayerConfigurationEntries.NAMESPACE_ID
                            ).get(0)
                        )
                    );
                    result.values(SystemAttributes.OBJECT_CLASS).add(
                        "org:openmdx:compatibility:runtime1:Namespace"
                    );
                    result.values(SystemAttributes.CREATED_AT).add(
                        this.activatedAt
                    );
                    result.values(SystemAttributes.MODIFIED_AT).add(
                        this.activatedAt
                    );
                    SparseList exposedPaths = result.values("exposedPath");
                    for(
                        Iterator i = getConfiguration().values(
                            LayerConfigurationEntries.EXPOSED_PATH
                        ).populationIterator();
                        i.hasNext();
                    ) if(LayerConfigurationEntries.EXPOSED_PATH_IS_MODELLED_AS_URI) {
                        exposedPaths.add(((Path)i.next()).toUri());
                    } else {
                        exposedPaths.add(i.next());
                    }
                    return new DataproviderReply(result);
                }
                case 10: //... Instance
                default : return new DataproviderReply();
            }
        } else {
            return super.find(header, request);
        }
    }

    /**
     * Special handling for org:openmdx:compatibility:runtime1 requests
     */
    final static private Path RUNTIME_AUTHORITY = new Path("xri:@openmdx:org.openmdx.compatibility.runtime1");

    /**
     *
     */
    String activatedAt;

    /**
     * 
     */
    private String domainName;
    
}
