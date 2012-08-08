/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PlaceHolder_1.java,v 1.6 2008/02/29 15:23:26 hburger Exp $
 * Description: Standard Transport Layer Plug-In
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/29 15:23:26 $
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
package org.openmdx.compatibility.base.dataprovider.layer.interception;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.spi.BeforeImageCachingLayer_1;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.naming.Path;

/**
 * The standard implementation of the Transport layer's plug-in.
 */
public class PlaceHolder_1
    extends BeforeImageCachingLayer_1 
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
     *      <dt>propagatePlaceholder</dt>   <dd>Boolean</dd>
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
        this.placeholders = configuration.isOn("propagatePlaceholder") ?
            null :
            new ArrayList<Path>();
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
    @SuppressWarnings("unchecked")
    public void prolog(
        ServiceHeader header,
        DataproviderRequest[] requests
    ) throws ServiceException {
        if(replacePlaceholders()){ 
            this.placeholders.clear();
            for(
                int r = 0;
                r < requests.length;
                r++
            ) rememberPlaceholder(requests[r].path());
            for(
                int r = 0;
                r < requests.length;
                r++
            ) for (
                Iterator a = requests[r].object().attributeNames().iterator();
                a.hasNext();
            ) for(
                ListIterator v = requests[r].object().values((String)a.next()).populationIterator();
                v.hasNext();
            ){
                int i = this.placeholders.indexOf(v.next());
                if(i >= 0) v.set(this.placeholders.get(i));
            }
        }
        super.prolog(header, requests);
    }

    /**
     * Create a new object
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
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        if(
            replacePlaceholders() &&
            request.path().size() % 2 == 1 &&
            request.path().getLastComponent().isPlaceHolder()
        ){
            Path placeholder = new Path(request.path());
            request.path().remove(request.path().size()-1);
            DataproviderReply reply = super.create(header,request);
            Path actual = reply.getObject().path();
            for (
                Iterator<Path> i = this.placeholders.iterator();
                i.hasNext();
            ){
                Path candidate = i.next();
                if(candidate.startsWith(placeholder)){
                    candidate.setTo(actual.getDescendant(candidate.getSuffix(placeholder.size())));
                    if(!isPlaceholder(candidate)) i.remove();
                }
            }
            return reply;
        } else {    
            return super.create(header,request);
        }
    }
    
    /**
     * Remember the path if it's a placeholder
     * 
     * @param path
     */
    private void rememberPlaceholder(
        Path path
    ){
        if(isPlaceholder(path)) this.placeholders.add(path);
    }

    /**
     * Test whether a path is a placeholder
     * 
     * @param path
     */
    private boolean isPlaceholder(
        Path path
    ){
        for(
            int i = 0;
            i < path.size();
            i++
        ) if (
            path.getComponent(i).isPlaceHolder()
        ) return true;
        return false;
    }

    /**
     * 
     */
    private boolean replacePlaceholders(
    ){
        return this.placeholders != null;     
    }

    
    //--------------------------------------------------------------------------
    // Instance members
    //--------------------------------------------------------------------------

    /**
     * To allow patching
     */
    private List<Path> placeholders;

}
