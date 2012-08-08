/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: HardWiredObjects_1.java,v 1.6 2009/05/27 23:14:18 wfro Exp $
 * Description: Hard-Wired Objects Layer
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/27 23:14:18 $
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
package org.openmdx.test.app1.layer.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.layer.application.Standard_1;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;

/**
 * Hard-wired Objects Layer
 * <p>
 * This layer implementation shall be replaced by an aspect oriented
 * persistence plug-in in future.
 */
public class HardWiredObjects_1
    extends Standard_1 
{

    /**
     * 
     */
    private static final String ADDRESS_FORMAT_TYPE_NAME = "org:openmdx:test:app1:AddressFormat";

    /**
     * 
     */
    private static final String NAME_FORMAT_TYPE_NAME = "org:openmdx:test:app1:NameFormat";

    /**
     * 
     */
    private Map<String,DataproviderObject> nameFormats;

    /**
     * 
     */
    private Map<String,DataproviderObject> addressFormats;

    /**
     * 
     */
    private final Logger logger = Logger.getLogger(HardWiredObjects_1.class.getName());


    //------------------------------------------------------------------------
    // Extends Layer_1
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
    @Override
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws ServiceException {
        super.activate(
            id, 
            configuration, 
            delegation
        );
        //
        // Creation time of the hard-wired objects
        //
        String timestamp = org.openmdx.base.text.format.DateFormat.getInstance().format(new Date());
        //
        // hard-wired NameFormat
        //
        this.nameFormats = new HashMap<String,DataproviderObject>();
        DataproviderObject nameFormatStandard = new DataproviderObject(new Path("Standard"));
        nameFormatStandard.values("description").add("default name format");
        nameFormatStandard.values(SystemAttributes.OBJECT_CLASS).add(NAME_FORMAT_TYPE_NAME);
        nameFormatStandard.values(SystemAttributes.CREATED_AT).add(timestamp);
        nameFormatStandard.values(SystemAttributes.MODIFIED_AT).add(timestamp);
        this.nameFormats.put(
            "Standard",
            nameFormatStandard
        );
        //
        // hard-wired AddressFormat
        //
        this.addressFormats = new HashMap<String,DataproviderObject>();
        DataproviderObject addressFormatStandard = new DataproviderObject(new Path("Standard"));
        addressFormatStandard.values("description").add("default address format");
        addressFormatStandard.values(SystemAttributes.OBJECT_CLASS).add(ADDRESS_FORMAT_TYPE_NAME);
        addressFormatStandard.values(SystemAttributes.CREATED_AT).add(timestamp);
        addressFormatStandard.values(SystemAttributes.MODIFIED_AT).add(timestamp);
        this.addressFormats.put(
            "Standard",
            addressFormatStandard
        );

    }     

    /**
     * Retrieve a copy of a format object with the given identity
     * 
     * @param objectId
     * @param formats
     * 
     * @return the copy
     * 
     * @throws ServiceException
     */
    private DataproviderObject getFormat(
        Path objectId,
        Map<String,DataproviderObject> formats
    ) throws ServiceException {
        DataproviderObject original = formats.get(objectId.getBase());
        if(original == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND, 
                "Format not found",
                new BasicException.Parameter("path", objectId)
            );        
        }
        DataproviderObject reply = new DataproviderObject(original);
        reply.path().setTo(objectId);
        return reply;
    }

    /**
     * Retrieve the reference name
     * 
     * @param request
     * 
     * @return the internalized reference name
     */
    private String getReferenceName(
        DataproviderRequest request
    ) {
        Path path = request.path(); 
        int size = path.size();
        return path.get(
            size - 1 - size % 2
        ).intern();
    }


    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#get(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.logger.log(Level.FINEST,"Get request for {0}", request.path());
        String referenceName = getReferenceName(request);
        if("nameFormat" == referenceName) {
            //
            // hard-wired nameFormat
            //
            return new DataproviderReply(
                getFormat(
                    request.path(),
                    this.nameFormats
                )
            );
        } else if("addressFormat" == referenceName) {
            //
            // hard-wired addressFormat
            //
            return new DataproviderReply(
                getFormat(
                    request.path(),
                    this.addressFormats
                )
            );
        } else {
            //
            // non hard-wired objects
            //
            return super.get(header, request);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.layer.application.ProvidingUid_1#create(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.logger.log(Level.FINEST,"Create request for {0}", request.path());
        String referenceName = getReferenceName(request);
        if("nameFormat" == referenceName || "addressFormat" == referenceName) {
            //
            // hard-wired nameFormat|addressFormat
            //
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "update not allowed on references with constraint isFrozen",
                new BasicException.Parameter("reference", referenceName)
            );
        } else {
            //
            // non hard-wired objects
            //
            return super.create(header, request);
        }
    }


    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#remove(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply remove(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        String referenceName = getReferenceName(request);
        if("nameFormat" == referenceName || "addressFormat" == referenceName) {
            //
            // hard-wired nameFormat|addressFormat
            //
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED, 
                "update not allowed on references with constraint isFrozen",
                new BasicException.Parameter("reference", referenceName)
            );
        } else {
            //
            // non hard-wired objects
            //
            return super.remove(header, request);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#replace(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        String referenceName = getReferenceName(request);
        if("nameFormat" == referenceName || "addressFormat" == referenceName) {
            //
            // hard-wired nameFormat|addressFormat
            //
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "update not allowed on references with constraint isFrozen",
                new BasicException.Parameter("reference", referenceName)
            );
        } else {
            //
            // non hard-wired objects
            //
            return super.replace(header, request);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#find(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        String referenceName = getReferenceName(request);
        if("nameFormat" == referenceName) {
            //
            // hard-wired NameFormat
            //
            List<DataproviderObject_1_0> formats = new ArrayList<DataproviderObject_1_0>();
            for(String id : this.nameFormats.keySet()) {
                formats.add(
                    getFormat(
                        request.path().getChild(id),
                        this.nameFormats
                    )
                );
            }
            return getSlice(request, formats);
        } else if("addressFormat" == referenceName) {
            //
            // hard-wired AddressFormat
            //
            List<DataproviderObject_1_0> formats = new ArrayList<DataproviderObject_1_0>();
            for(String id : this.addressFormats.keySet()) {
                formats.add(
                    getFormat(
                        request.path().getChild(id),
                        this.addressFormats
                    )
                );
            }
            return getSlice(request, formats);
        } else {
            //
            // non hard-wired objects
            //
            return super.find(header, request);
        }
    }

    private DataproviderReply getSlice(
        DataproviderRequest request,
        List<DataproviderObject_1_0> values
    ){
        boolean hasMore;
        if(request.position() >= values.size()) {
            values = Collections.emptyList();
            hasMore = false;
        } else {
            int fromPosition = request.position();
            long toPosition = fromPosition;
            toPosition += request.size();
            if(toPosition >= values.size()) {
                toPosition = values.size();
                hasMore = false;
            } else {
                hasMore = true;
            }
            if(fromPosition > 0 || hasMore) {
                values = values.subList(fromPosition, (int)toPosition);
            }
        }
        DataproviderReply reply = new DataproviderReply(values);
        reply.context(DataproviderReplyContexts.HAS_MORE).set(0, Boolean.valueOf(hasMore));
        return reply;
    }

}
