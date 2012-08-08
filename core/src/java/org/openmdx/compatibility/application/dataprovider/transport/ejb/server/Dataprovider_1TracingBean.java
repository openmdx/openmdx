/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1TracingBean.java,v 1.17 2005/02/21 11:59:46 hburger Exp $
 * Description: A Dataprovider Service
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/02/21 11:59:46 $
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
package org.openmdx.compatibility.application.dataprovider.transport.ejb.server;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.transport.rmi.RMIMapper;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.text.format.IndentingFormatter;

/**
 * The dataprovider server tracing on detail level
 */
public class Dataprovider_1TracingBean
  extends Dataprovider_1Bean
{

    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0
    //------------------------------------------------------------------------

    /**
     * 
     */
    private static final long serialVersionUID = 3762256322745480246L;

    /**
     * Process a set of working units
     *
     * @param   header          the service header
     * @param   workingUnits    a collection of working units
     *
     * @return  a collection of working unit replies
     */
    public UnitOfWorkReply[] process(
        ServiceHeader header,
        UnitOfWorkRequest[] workingUnits
    ){      
        try {
            AppLog.detail(
            	this.serverId, 
            	new IndentingFormatter(
            		ArraysExtension.asMap(
            			new String[]{
            			    "correlationId",
            			    "principalChain",
            			    "requestedAt",
            			    "requestedFor",
            			    "unitsOfWork"
            			},
            			new Object[]{
            			    header.getCorrelationId(),
            			    header.getPrincipalChain(),
            			    header.getRequestedAt(),
            			    header.getRequestedFor(),
            			    workingUnits
            			}
            		)
            	)
            );
            UnitOfWorkReply[] replies = RMIMapper.marshal(
                this.kernel.process(
                    header,
                    RMIMapper.unmarshal(workingUnits)
                )
            );
            AppLog.detail(
            	this.serverId, 
            	new IndentingFormatter(
            		ArraysExtension.asMap(
            			new String[]{
            			    "correlationId",
            			    "replies"
            			},
            			new Object[]{
            			    header.getCorrelationId(),
            			    replies
            			}
            		)
            	)
            );
            return replies;
        } catch (RuntimeException exception) {
            new RuntimeServiceException(exception).log();
            throw exception;    
        }
    }

}  
