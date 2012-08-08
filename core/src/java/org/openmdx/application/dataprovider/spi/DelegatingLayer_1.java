/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DelegatingLayer_1.java,v 1.2 2009/02/17 10:06:22 hburger Exp $
 * Description: Delegating Layer 1
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/17 10:06:22 $
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
package org.openmdx.application.dataprovider.spi;

import org.openmdx.application.dataprovider.cci.DataproviderLayers;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.application.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * Delegating Layer 1
 */
public class DelegatingLayer_1 extends Layer_1 {

    /**
     * Constructor 
     */
    protected DelegatingLayer_1() {
        // Instantiation requires subclassing
    }

    
    //------------------------------------------------------------------------
    // Extends Layer_1
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#process(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest)
     */
    @Override
    protected UnitOfWorkReply process(
        ServiceHeader header,
        UnitOfWorkRequest unitOfWorkRequest
    ){      
        UnitOfWorkReply unitOfWorkReply;
        DataproviderRequest[] dataproviderRequests = unitOfWorkRequest.getRequests();
        DataproviderReply[] dataproviderReplies = null;
        try {
            int cursor = 0, pending = 0;
            for(DataproviderRequest dataproviderRequest : dataproviderRequests) {
                DataproviderReply dataproviderReply = process(header, dataproviderRequest);
                if(dataproviderReply == null) {
                    pending++;
                } else {
                    if(dataproviderReplies == null) {
                        dataproviderReplies = new DataproviderReply[dataproviderRequests.length];
                    }
                    dataproviderReplies[cursor] = dataproviderReply;
                }
                cursor++;
            }
            if(dataproviderReplies == null){
                unitOfWorkReply = getDelegation().process(
                    header, 
                    unitOfWorkRequest
                )[0];
            } else if (pending == 0) {
                unitOfWorkReply = new UnitOfWorkReply(dataproviderReplies);
            } else {
                DataproviderRequest[] pendingRequests = new DataproviderRequest[pending];
                for(
                    int pendingCursor = 0, originalCursor = 0;
                    originalCursor < dataproviderReplies.length;
                    originalCursor++
                ){
                    if(dataproviderReplies[originalCursor] == null) {
                        pendingRequests[pendingCursor++] = dataproviderRequests[originalCursor];
                    }
                }
                unitOfWorkReply = getDelegation().process(
                    header, 
                    new UnitOfWorkRequest(
                        unitOfWorkRequest.isTransactionalUnit(),
                        pendingRequests
                    )
                )[0];
                if(!unitOfWorkReply.failure()) {
                    DataproviderReply[] pendingReplies = unitOfWorkReply.getReplies();
                    for(
                        int pendingCursor = 0, originalCursor = 0;
                        originalCursor < dataproviderReplies.length;
                        originalCursor++
                    ){
                        if(dataproviderReplies[originalCursor] == null) {
                            dataproviderReplies[originalCursor] = pendingReplies[pendingCursor++];
                        }
                    }
                    unitOfWorkReply = new UnitOfWorkReply(dataproviderReplies);
                }
            }
        } catch (ServiceException exception) {
            unitOfWorkReply = new UnitOfWorkReply(exception);
        }
        return unitOfWorkReply;
    }

    
    
    //------------------------------------------------------------------------
    // Methods to be overridden by as subclass
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#create(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#find(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#get(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#modify(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#operation(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply operation(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return null;
    }
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#remove(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply remove(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#replace(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#set(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply set(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#startPublishing(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply startPublishing(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return null;
    }

    
    //------------------------------------------------------------------------
    // Methods not to be invoked
    //------------------------------------------------------------------------
    
    /**
     * Create a <code>NOT_SUPPORTED</code> ServiceException
     * 
     * @return a newly created <code>NOT_SUPPORTED</code> ServiceException
     */
    private ServiceException newNotSupportedException(
    ){
        return new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "Only Datatprovider_1_0 processing methods are supported",
            new BasicException.Parameter("layer-class", getClass()),
            new BasicException.Parameter("layer-id", DataproviderLayers.toString(getId()))
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#epilog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest[], org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply[])
     */
    @Override
    public final void epilog(
        ServiceHeader header,
        DataproviderRequest[] requests,
        DataproviderReply[] replies
    ) throws ServiceException {
        throw newNotSupportedException();
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#prolog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest[])
     */
    @Override
    public final void prolog(
        ServiceHeader header, 
        DataproviderRequest[] requests
    ) throws ServiceException {
        throw newNotSupportedException();
    }


    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#epilog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest, org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply)
     */
    @Override
    public final void epilog(
        ServiceHeader header,
        UnitOfWorkRequest request,
        UnitOfWorkReply reply
    ) throws ServiceException {
        throw newNotSupportedException();
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#prolog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest)
     */
    @Override
    public final void prolog(
        ServiceHeader header, 
        UnitOfWorkRequest request
    ) throws ServiceException {
        throw newNotSupportedException();
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#process(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest[], org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply[])
     */
    @Override
    public final void process(
        ServiceHeader header,
        DataproviderRequest[] requests,
        DataproviderReply[] replies
    ) throws ServiceException {
        throw newNotSupportedException();
    }
    
}
