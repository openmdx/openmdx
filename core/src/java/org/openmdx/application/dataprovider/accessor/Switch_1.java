/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Switch_1.java,v 1.1 2009/01/09 01:08:28 wfro Exp $
 * Description: Dataprovider Adapter: Switch
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/09 01:08:28 $
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
package org.openmdx.application.dataprovider.accessor;

import java.io.Serializable;

import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.application.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

/**
 * Dataprovider Adapter: Switch
 */
public class Switch_1 implements Serializable, Dataprovider_1_0 {

	/**
     * 
     */
    private static final long serialVersionUID = 3257286924614776119L;


    /**
	 * Standard constructor
	 * 
	 * @param forward
	 * @param scopes
	 * @param fallback
	 * @throws ServiceException
	 */
    public Switch_1(
        Dataprovider_1_0[] forward,
        Path[] scopes,
        Dataprovider_1_0 fallback
    ) throws ServiceException {
        this.scopes = scopes;
        this.mappings = forward;
        this.fallback = fallback;
    }
    
    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0
    //------------------------------------------------------------------------

    /**
     * Process a set of working units
     *
     * @param   header          the service header
     * @param   unitsOfWork a collection of working units
     *
     * @return  a collection of working unit replies
     */
    public UnitOfWorkReply[] process(
        ServiceHeader header,
        UnitOfWorkRequest... unitsOfWork
    ){
        UnitOfWorkReply[] replies = new UnitOfWorkReply[unitsOfWork.length];
        for(
            int i=0;
            i < unitsOfWork.length;
            i++
        ) replies[i] = process(header, unitsOfWork[i]);
        return replies;
    }
    
    /**
     * Process a single unit of work
     *
     * @param   header          the service header
     * @param   unitOfWork      a working unit
     *
     * @return  a collection of working unit replies
     */
    private UnitOfWorkReply process(
        ServiceHeader header,
        UnitOfWorkRequest unitOfWork
    ) {
        try {
            DataproviderRequest[] requests = unitOfWork.getRequests();
            UnitOfWorkReply reply = new UnitOfWorkReply(
                new DataproviderReply[requests.length]
            );
            if(requests.length == 0) return reply;
            Dataprovider_1_0 currentMapping = getDataprovider(requests[0]);
            int currentPosition = 0;
            for(
                int nextPosition = 1;
                nextPosition < requests.length;
                nextPosition++
            ){
                Dataprovider_1_0 nextMapping = getDataprovider(requests[nextPosition]);
                if(nextMapping != currentMapping){
                    reply = process(
                        header,
                        unitOfWork,
                        reply,
                        currentPosition,
                        nextPosition - currentPosition,
                        currentMapping
                    );
                    if(reply.failure()) return reply;
                    currentMapping = nextMapping;
                    currentPosition = nextPosition;
                }
            }                                               
            return process(
                header,
                unitOfWork,
                reply,
                currentPosition,
                requests.length - currentPosition,
                currentMapping
            );
        } catch (ServiceException exception) {
            return new UnitOfWorkReply(exception);
        }
    }
    
    /**
     *
     */
    private static UnitOfWorkReply process(
        ServiceHeader header,
        UnitOfWorkRequest unitOfWorkRequest,
        UnitOfWorkReply unitOfWorkReply,
        int position,
        int length,
        Dataprovider_1_0 dataprovider
    ){
        DataproviderRequest[] requests = new DataproviderRequest[length];
        System.arraycopy(
            unitOfWorkRequest.getRequests(), position,
            requests, 0,
            length
        );
        UnitOfWorkRequest subunitOfWorkRequest = new UnitOfWorkRequest(
            unitOfWorkRequest.isTransactionalUnit(),
            requests
        );
        UnitOfWorkReply subunitOfWorkReply = dataprovider.process(
            header,
            subunitOfWorkRequest
        )[0];
        if (subunitOfWorkReply.failure()) return subunitOfWorkReply;
        System.arraycopy(
            subunitOfWorkReply.getReplies(), 0,
            unitOfWorkReply.getReplies(), position,
            length
        );
        return unitOfWorkReply;
    }

    /**
     * 
     * @param request
     * @throws ServiceException
     */
    private Dataprovider_1_0 getDataprovider(
        DataproviderRequest request
    ) throws ServiceException {
        Path path = request.path();
        for (
            int i = 0;
            i < this.scopes.length;
            i++
        ) if (path.startsWith(this.scopes[i])) return this.mappings[i];
        return this.fallback;
    }

    
    //------------------------------------------------------------------------
    // Instance Members
    //------------------------------------------------------------------------

    /**
     * 
     */
    Path[] scopes;
    
    /**
     * 
     */
    Dataprovider_1_0[] mappings;
    
    /**
     * 
     */
    final Dataprovider_1_0 fallback;
        

    //------------------------------------------------------------------------
    // Class Members
    //------------------------------------------------------------------------
    
}
