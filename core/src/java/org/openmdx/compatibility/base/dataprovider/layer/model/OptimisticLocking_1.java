/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: OptimisticLocking_1.java,v 1.18 2009/01/12 02:41:46 hburger Exp $
 * Description: Optimistic Locking Plug-In
 * Revision:    $Revision: 1.18 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/12 02:41:46 $
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
package org.openmdx.compatibility.base.dataprovider.layer.model;

import static org.openmdx.compatibility.base.dataprovider.layer.type.Strict_1.STATE1_CAPABLE_CLASS;

import java.math.BigInteger;
import java.util.Arrays;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.UnicodeTransformation;
import org.openmdx.kernel.exception.BasicException;

/**
 * Optimistic Locking Plug-In
 */
public class OptimisticLocking_1 extends SystemAttributes_1 {

    /**
     * Tells whether the plug-in is active of inactive.
     */
    private boolean optimisticLocking = false;

    /**
     * Tells whether even unmodified objects are checked.
     */
    private boolean always = false;
    
    /**
     * Calculates and sets an object's digest
     * 
     * @throws ServiceException 
     */
    private void propagateDigest(
        DataproviderObject_1_0 object
    ) throws ServiceException{
        if(
            object.getDigest() != null || // Do not override digest
            !isInstanceOfBasicObject(object) // relies on BasicObject's modifiedAt feature
        ) {
            return;
        }
        String objectClassName = this.getObjectClassName(object);
        if(STATE1_CAPABLE_CLASS.equals(objectClassName)) {
            SparseList<?> objectVersion = object.getValues(SystemAttributes.VERSION);
            if(objectVersion != null) {
                Number integerVersion = (Number) objectVersion.get(0);
                if(integerVersion != null) {
                    BigInteger bigintegerVersion = BigInteger.valueOf(integerVersion.longValue());
                    object.setDigest(
                        bigintegerVersion.toByteArray()
                    );
                }
            }
        } else {
            SparseList<?> modifiedAt = object.getValues(SystemAttributes.MODIFIED_AT);
            if(modifiedAt == null) return;
            Object source = modifiedAt.get(0);
            if(source != null) object.setDigest(
                UnicodeTransformation.toByteArray(source.toString())
            );
        }
    }

    /**
     * Propagate the digest
     * 
     * @param replies
     * @throws ServiceException
     */
    protected void propagateDigest(
        DataproviderReply[] replies        
    ) throws ServiceException {
        if (this.optimisticLocking){
            for(DataproviderReply reply : replies) {
                for(DataproviderObject object : reply.getObjects()) {
                    propagateDigest(object);
                }
            }
        }
    }

    /**
     * Verify the digest of an object to be modified
     * 
     * @param header
     * @param request
     * 
     * @throws ServiceException CONCURRENT_ACCESS_FAILURE
     * in case of a digest mismatch.
     */
    protected void verifyDigest(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        if(this.optimisticLocking){
            DataproviderObject afterImage = request.object();
            if(this.always || isModified(afterImage)){
                DataproviderObject_1_0 beforeImage = getBeforeImage(header, request);
                propagateDigest(beforeImage);
                if (
                    !Arrays.equals(beforeImage.getDigest(),afterImage.getDigest())
                ) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                    "Digest mismatch",
                    new BasicException.Parameter(
                        "path",
                        request.path()
                    ),
                    new BasicException.Parameter(
                        "beforeImageDigest",
                        beforeImage.getDigest()
                    ),
                    new BasicException.Parameter(
                        "afterImageDigest",
                        afterImage.getDigest()
                    )
                );
            }
        }
    }

    protected boolean isModified(
        DataproviderObject afterImage
    ){
        for(String attribute : afterImage.attributeNames()) {
            if(
                !SystemAttributes.OBJECT_CLASS.equals(attribute) &&
                !SystemAttributes.MODIFIED_AT.equals(attribute) &&
                !SystemAttributes.MODIFIED_BY.equals(attribute)
            ){
                return true;
            }
        }
        return false;
    }
    
    //------------------------------------------------------------------------
    // Implements Layer_1_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws ServiceException {
        super.activate(id, configuration, delegation);
        this.always = configuration.isOn(LayerConfigurationEntries.OPTIMISTIC_LOCKING);
        this.optimisticLocking = this.always || "whenModified".equalsIgnoreCase(
            configuration.getFirstValue(LayerConfigurationEntries.OPTIMISTIC_LOCKING)
        );
    }

    /**
     * Modifies some of an object's attributes leaving the others unchanged.
     *
     * @param   request     the request, an in out parameter
     *
     * @exception   ServiceException
     *              Any ServiceException raised by the persistence layer;
     * @exception   ServiceException EX_OPTIMISTIC_LOCKING
     *              if the object's digest verification failed
     * @exception   ServiceException EX_BAD_PARAM
     *              if the new class is not an instance of the existing one
     */
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        //
        // Verify Digest
        //
        verifyDigest(header,request);
        // Process request
        return super.modify(header,request);
    }

    /**
     * Modifies some or all of an object's attributes leaving the others 
     * unchanged.
     *
     * @param   request     the request, an in out parameter
     *
     * @exception   ServiceException
     *              Any ServiceException raised by the persistence layer;
     * @exception   ServiceException EX_OPTIMISTIC_LOCKING
     *              if the object's digest verification failed
     * @exception   ServiceException EX_BAD_PARAM
     *              if the new class is not an instance of the existing one
     */
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        // 
        // Verify Digest
        //
        verifyDigest(header,request);
        //
        // Process request
        //
        return super.replace(header,request);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0#epilog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest[], org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply[])
     */
    public void epilog(
        ServiceHeader header,
        DataproviderRequest[] requests,
        DataproviderReply[] replies
    ) throws ServiceException {
        super.epilog(header, requests, replies);
        //
        // Propagate Digest
        //
        propagateDigest(replies);
    }

    protected String getObjectClassName(
        DataproviderObject_1_0 object
    ){
        if (object != null) {
            //
            // OBJECT_CLASS may be null in delete operations
            //
            SparseList<Object> objectClassAttribute = object.getValues(SystemAttributes.OBJECT_CLASS);
            if (objectClassAttribute != null) return (String)objectClassAttribute.get(0);
        }
        return null;
    }

}
