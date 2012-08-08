/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Delegation_1.java,v 1.8 2008/09/10 08:55:25 hburger Exp $
 * Description: Delegation_1 plugin
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:25 $
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
package org.openmdx.compatibility.base.dataprovider.layer.persistence.delegation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * A layer which maps Layer_1_0 to Dataprovider_1_0 requests.
 */
@SuppressWarnings("unchecked")
public class Delegation_1
implements Layer_1_0 {

    public void activate(
        short id,
        Dataprovider_1_0 delegation
    ) throws Exception {
        this.dataprovider = delegation;
        this.dataproviders = null;
        SysLog.trace("Use fixed dataprovider connection",delegation);
    }


    //--------------------------------------------------------------------------
    // Implements Layer_1_0
    //--------------------------------------------------------------------------

    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws Exception {
        if (delegation == null) {
            if (configuration.containsEntry("channel")) {
                Number channel = (Number)configuration.values("channel").get(0);
                activate(
                    id,
                    (Dataprovider_1_0)configuration.values(
                        SharedConfigurationEntries.DATAPROVIDER_CONNECTION
                    ).get(channel.intValue())
                );
                SysLog.trace("Use dataprovider channel",channel);
            } else {
                this.dataproviders = configuration.values(
                    SharedConfigurationEntries.DATAPROVIDER_CONNECTION
                );
                SysLog.trace(
                    "Use a set of dataprovider connections",
                    this.dataproviders
                );
            }
        } else {
            activate(
                id,
                delegation
            );
        }   
    }

    public void deactivate(
    ) throws Exception {
        //
    }

    public Map configurationSpecification() {
        return new HashMap();
    }

    /**
     * Get the object specified by the requests's path 
     *
     * @param       header
     *              request header
     * @param       request
     *          the request
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return process(header,request);
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
    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return process(header,request);
    }

    /**
     * Request to start publishing the data stream specified by the requests's path 
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
    public DataproviderReply startPublishing(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return process(header,request);
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
        return process(header,request);
    }

    /**
     * Modifies some of an object's attributes leaving the others unchanged.
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
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return process(header,request);
    }

    /**
     * Modifies all changeable attributes of an object.
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
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return process(header,request);
    }

    /**
     * Creates an object or modifies all its changeable attributes if it
     * already exists.
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
    public DataproviderReply set(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return process(header,request);
    }

    /**
     * Removes an object including its descendents
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
    public DataproviderReply remove(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return process(header,request);
    }

    /**
     * This method allows the dataprovider layers to verify the integrity of a 
     * set of requests as a whole before the actual processing of the 
     * individual requests starts.
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
        // Delegation_1 is a terminal layer
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
        // Delegation_1 is a terminal layer
    }

    protected DataproviderReply process(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        UnitOfWorkReply workingUnit = process(
            header,
            new UnitOfWorkRequest(
                false,
                new DataproviderRequest[]{request}
            )
        );
        if(workingUnit.failure()) throw new ServiceException(
            workingUnit.getStatus(),
            workingUnit.getStatus().getExceptionDomain(),
            workingUnit.getStatus().getExceptionCode(),
            workingUnit.getStatus().getMessage() // Parameters available in next stack element only
        );
        return workingUnit.getReplies()[0];
    }

    protected UnitOfWorkReply process(
        ServiceHeader header,
        UnitOfWorkRequest request
    ){
        try {
            Dataprovider_1_0 target = this.dataprovider;
            if (target == null) {
                Number channel = (Number)request.context(
                    SharedConfigurationEntries.DATAPROVIDER_CONNECTION
                ).get(
                    0
                );
                SysLog.trace("Channel",channel);
                try {
                    target = (Dataprovider_1_0)this.dataproviders.get(
                        channel.intValue()
                    );
                } catch (NullPointerException exception) {
                    throw new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "Channel missing",
                        new BasicException.Parameter("dataproviders",this.dataproviders)
                    );      
                }
                if (target == null) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "Not existing channel referenced",
                    new BasicException.Parameter("dataproviders",this.dataproviders),
                    new BasicException.Parameter("channel",channel)
                );      
            } else {
                SysLog.trace("Channel","fixed");
            }
            return target.process(
                header,
                new UnitOfWorkRequest[]{request}
            )[0];
        } catch (ServiceException exception) {
            return new UnitOfWorkReply(exception.log());
        }
    }

    //--------------------------------------------------------------------------
    // Implements Operation_1_0
    //--------------------------------------------------------------------------

    /**
     * Execute an operation
     *
     * @param       header
     *              the request header
     * @param       request
     *              the request
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              in case of failure
     */
    public DataproviderReply operation(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return process(header,request);
    }


    //--------------------------------------------------------------------------
    // Implements Dataprovider_1_0
    //--------------------------------------------------------------------------

    /**
     * Process a set of working units
     *
     * @param header          the service header
     * @param workingUnits    a collection of working units
     *
     * @return    a collection of working unit replies
     */
    public UnitOfWorkReply[] process(
        ServiceHeader header,
        UnitOfWorkRequest[] workingUnits
    ){
        try {
            if (this.dataprovider == null) {
                UnitOfWorkReply[] replies = new UnitOfWorkReply[workingUnits.length];
                for (
                        int index = 0;
                        index < workingUnits.length;
                        index++
                ) replies[index] = process(header, workingUnits[index]);
                SysLog.trace("Replies",Arrays.asList(replies));
                return replies;
            } else {
                return this.dataprovider.process(
                    header,
                    workingUnits
                );
            }
        } catch (RuntimeException exception) {
            throw new RuntimeServiceException(exception).log();
        }
    }


    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------

    /**
     *
     */
    private Dataprovider_1_0 dataprovider;

    /**
     *
     */
    private SparseList dataproviders;

}
