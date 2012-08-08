/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Explorer_1.java,v 1.11 2009/01/05 13:48:24 wfro Exp $
 * Description: Standard Explorer Plug-In
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/05 13:48:24 $
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
package org.openmdx.compatibility.runtime1.layer.application;


import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.application.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.base.exception.ServiceException;

/**
 * Standard Explorer Plug-In
 */
public class Explorer_1 extends AbstractExplorer_1 {

    /**
     * Process a single unit of work
     *
     * @param   header          the service header
     * @param   unitOfWork      a working unit
     *
     * @return  a collection of working unit replies
     */
    protected UnitOfWorkReply process(
        ServiceHeader header,
        UnitOfWorkRequest unitOfWork
    ) {
        try {
            DataproviderRequest[] requests = unitOfWork.getRequests();
            UnitOfWorkReply reply = new UnitOfWorkReply(
                new DataproviderReply[requests.length]
            );
            if(requests.length == 0) return reply;
            Integer currentMapping = getMapping(requests[0]);
            int currentPosition = 0;
            for(
                int nextPosition = 1;
                nextPosition < requests.length;
                nextPosition++
            ){
                Integer nextMapping = getMapping(requests[nextPosition]);
                if(! nextMapping.equals(currentMapping)){
                    reply = process(
                        header,
                        unitOfWork,
                        reply,
                        getPositions(currentPosition, nextPosition - currentPosition),
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
                getPositions(currentPosition, requests.length - currentPosition),
                currentMapping
            );
        } catch (ServiceException exception) {
            return new UnitOfWorkReply(exception);
        }
    }

    /**
     * Create a position array
     * 
     * @param from
     * @param length
     * 
     * @return the corresponding position array
     */
    protected int[] getPositions(
        int from, 
        int length
    ){
        int[] positions = new int[length];
        for(
            int i = 0;
            i < length;
            i++
        ) positions[i] = from + i;
        return positions;
    }

}
