/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: VirtualObjects_1.java,v 1.4 2008/10/28 17:23:37 hburger Exp $
 * Description: Hard-Wired Objects Layer
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/10/28 17:23:37 $
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
package org.openmdx.test.app1.aop2.persistence;

import java.util.Date;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hard-wired Objects Layer
 * <p>
 * This layer implementation shall be replaced by an aspect oriented
 * persistence plug-in in future.
 */
public class VirtualObjects_1
extends HardWiredObjects_1 
{

    /**
     * 
     */
    private static final String PRODUCT_GROUP_TYPE_NAME = "org:openmdx:test:app1:ProductGroup";

    /**
     * 
     */
    private static final Path PRODUCT_GROUP_PATTERN = new Path(
        new String[]{
            "org:openmdx:test:app1",
            "provider",
            ":*",
            "segment",
            ":*",
            "productGroup",
            ":*"
        }
    );

    /**
     * 
     */
    private static final String PRODUCT_TYPE_NAME = "org:openmdx:test:app1:Product";

    /**
     * 
     */
    private static final Path PRODUCT_PATTERN = PRODUCT_GROUP_PATTERN.getDescendant(
        new String[]{
            "product",
            ":*"
        }
    );

    /**
     * The virtual objects' creation date
     */
    private String createdAt;

    /**
     * The virtual objects' modification date
     */
    private String modifiedAt;

    /**
     * 
     */
    private final Logger logger = LoggerFactory.getLogger(VirtualObjects_1.class);


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
    ) throws Exception, ServiceException {
        super.activate(
            id, 
            configuration, 
            delegation
        );
        //
        // Basic object property of the virtual objects
        //
        this.createdAt = org.openmdx.base.text.format.DateFormat.getInstance().format(new Date());
        this.modifiedAt = this.createdAt;
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
    private DataproviderObject newObject(
        Path objectId,
        String objectClass
    ) throws ServiceException {
        DataproviderObject object = new DataproviderObject(new Path("Standard"));
        object.values("description").add(objectId.get(objectId.size() - 2) + " '" + objectId.getBase() + "'");
        object.values(SystemAttributes.OBJECT_CLASS).add(objectClass);
        object.values(SystemAttributes.CREATED_AT).add(this.createdAt);
        object.values(SystemAttributes.MODIFIED_AT).add(this.modifiedAt);
        return object;
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#get(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        Path objectId = request.path();
        this.logger.trace("Get request for {}", objectId);
        if(objectId.isLike(PRODUCT_GROUP_PATTERN)) {
            //
            // virtual ProductGroup instances
            //
            return new DataproviderReply(
                newObject(
                    objectId,
                    PRODUCT_GROUP_TYPE_NAME
                )
            );
        } else if(objectId.isLike(PRODUCT_PATTERN)) {
            //
            // virtual Product instances
            //
            return new DataproviderReply(
                newObject(
                    objectId,
                    PRODUCT_TYPE_NAME
                )
            );
        } else {
            //
            // non-virtual objects
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
        Path objectId = request.path();
        this.logger.trace("Create request for {}", objectId);
        if(
                objectId.isLike(PRODUCT_GROUP_PATTERN) ||
                objectId.isLike(PRODUCT_PATTERN)
        ) {
            //
            // virtual Product|ProductGroup
            //
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "Virtual objects can't be created",
                new BasicException.Parameter("objectId", objectId.toXri())
            );
        } else {
            //
            // non-virtual objects
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
        Path objectId = request.path();
        this.logger.trace("Remove request for {}", objectId);
        if(
                objectId.isLike(PRODUCT_GROUP_PATTERN) ||
                objectId.isLike(PRODUCT_PATTERN)
        ) {
            //
            // virtual Product|ProductGroup
            //
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "Virtual objects can't be removed",
                new BasicException.Parameter[]{
                    new BasicException.Parameter("objectId", objectId.toXri())
                }
            );
        } else {
            //
            // non-virtual objects
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
        Path objectId = request.path();
        this.logger.trace("Replace request for {}", objectId);
        if(
                objectId.isLike(PRODUCT_GROUP_PATTERN) ||
                objectId.isLike(PRODUCT_PATTERN)
        ) {
            //
            // virtual Product|ProductGroup
            //
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "Virtual objects can't be replaced",
                new BasicException.Parameter("objectId", objectId.toXri())
            );
        } else {
            //
            // non-virtual objects
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
        Path objectId = request.path();
        this.logger.trace("Find request for {}", objectId);
        if(
                objectId.isLike(PRODUCT_GROUP_PATTERN) ||
                objectId.isLike(PRODUCT_PATTERN)
        ) {
            //
            // virtual Product|ProductGroup
            //
            DataproviderReply reply = new DataproviderReply();
            reply.context(DataproviderReplyContexts.HAS_MORE).set(0, Boolean.FALSE);
            return reply;
        } else {
            //
            // non hard-wired objects
            //
            return super.find(header, request);
        }
    }

}
