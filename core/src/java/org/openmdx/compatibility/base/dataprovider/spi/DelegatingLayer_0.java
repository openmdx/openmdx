/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DelegatingLayer_0.java,v 1.11 2008/09/10 08:55:25 hburger Exp $
 * Description: DelegatingLayer_0 class
 * Revision:    $Revision: 1.11 $
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
package org.openmdx.compatibility.base.dataprovider.spi;

import java.util.HashMap;
import java.util.Map;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.cci.ConfigurationSpecifier;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderLayers;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

public class DelegatingLayer_0
implements Layer_1_0
{

    /**
     * To be supplied by the dataprovider
     *
     * @return  the (modifiable) configuration
     *  <dl>
     *      <dt>namespaceId</dt>            <dd>String</dd>
     *      <dt>exposedPath</dt>            <dd>Path[]</dd>
     *  </dl>
     */
    protected final Configuration getConfiguration(
    ){
        return this.configuration;
    }

    //------------------------------------------------------------------------
    // The methods to be reviewed>>>
    //------------------------------------------------------------------------

    /**
     * This method allows the dataprovider layers to set up the working units'
     * contexts.
     *
     * @param       header
     *              the working units' service header
     * @param       contexts
     *              the working units' contexts
     */
    protected void contextInitialization(
        ServiceHeader header, 
        Object[] contexts
    ){
        //
    }

    /**
     * This method allows the dataprovider layers to verify the integrity of a 
     * collection of requests as a whole before the actual processing of the 
     * individual requests starts.
     *
     * @param       header
     *              the unit of work's service header
     * @param       unitOfWork
     *              the request list
     *
     * @exception   ServiceException
     *              on failure
     */
    protected void prolog(
        ServiceHeader header, Object context,
        UnitOfWorkRequest unitOfWork
    ) throws ServiceException {
        DataproviderRequest[] requests = unitOfWork.getRequests();
        for (
                int index = 0;
                index < requests.length;
                index++
        ) prolog(
            header, 
            context,
            requests[index]
        );
    }

    /**
     * This method allows the dataprovider layer to verify the integrity of a 
     * single request before its actual processing.
     *
     * @param       header
     *              the request's service header
     * @param       request
     *              the request
     *
     * @exception   ServiceException
     *              on failure
     */
    protected void prolog(
        ServiceHeader header, Object context,
        DataproviderRequest request
    ) throws ServiceException {
        if (this.id == DataproviderLayers.PERSISTENCE) new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE,
            "DelegatingLayer_0 used as PERSISTENCE layer!",
            new BasicException.Parameter("class", getClass().getName()),
            new BasicException.Parameter(
                "namespace",
                (Object[])getConfiguration().getValues(
                    SharedConfigurationEntries.NAMESPACE_ID
                )
            )
        ).log();
        SysLog.trace(
            DataproviderLayers.toString(this.id), 
            request
        );
        switch (request.operation()) {
            case DataproviderOperations.OBJECT_OPERATION:
                operationProlog(
                    header, context,
                    request
                );
                break;
            case DataproviderOperations.OBJECT_CREATION:
                createProlog(
                    header, context,
                    request
                );
                break;
            case DataproviderOperations.OBJECT_SETTING:
                setProlog(
                    header, context,
                    request
                );
                break;
            case DataproviderOperations.OBJECT_MODIFICATION:
            case DataproviderOperations.OBJECT_REPLACEMENT:
                updateProlog(
                    header, context,
                    request
                );
                break;
            case DataproviderOperations.OBJECT_REMOVAL:
                removeProlog(
                    header, context,
                    request
                );
                break;
            case DataproviderOperations.ITERATION_START:
            case DataproviderOperations.ITERATION_CONTINUATION:
                findProlog(
                    header, context,
                    request
                );
                break;
            case DataproviderOperations.OBJECT_RETRIEVAL:
                getProlog(
                    header, context,
                    request
                );
                break;
        }
    }

    /**
     * This method allows the dataprovider layer to verify the integrity of a 
     * single request before its actual processing.
     *
     * @param       header
     *              the request's service header
     * @param       request
     *              the request
     *
     * @exception   ServiceException
     *              on failure
     */
    protected void operationProlog(
        ServiceHeader header, Object context,
        DataproviderRequest request
    ) throws ServiceException {
        //
    }

    /**
     * This method allows the dataprovider layer to verify the integrity of a 
     * single request before its actual processing.
     *
     * @param       header
     *              the request's service header
     * @param       request
     *              the request
     *
     * @exception   ServiceException
     *              on failure
     */
    protected void getProlog(
        ServiceHeader header, Object context,
        DataproviderRequest request
    ) throws ServiceException {
        //
    }

    /**
     * This method allows the dataprovider layer to verify the integrity of a 
     * single request before its actual processing.
     *
     * @param       header
     *              the request's service header
     * @param       request
     *              the request
     *
     * @exception   ServiceException
     *              on failure
     */
    protected void createProlog(
        ServiceHeader header, Object context,
        DataproviderRequest request
    ) throws ServiceException {
        //
    }

    /**
     * This method allows the dataprovider layer to verify the integrity of a 
     * single request before its actual processing.
     *
     * @param       header
     *              the request's service header
     * @param       request
     *              the request
     *
     * @exception   ServiceException
     *              on failure
     */
    protected void setProlog(
        ServiceHeader header, Object context,
        DataproviderRequest request
    ) throws ServiceException {
        //
    }

    /**
     * This method allows the dataprovider layer to verify the integrity of a 
     * single request before its actual processing.
     *
     * @param       header
     *              the request's service header
     * @param       request
     *              the request
     *
     * @exception   ServiceException
     *              on failure
     */
    protected void updateProlog(
        ServiceHeader header, Object context,
        DataproviderRequest request
    ) throws ServiceException {
        //
    }

    /**
     * This method allows the dataprovider layer to verify the integrity of a 
     * single request before its actual processing.
     *
     * @param       header
     *              the request's service header
     * @param       request
     *              the request
     *
     * @exception   ServiceException
     *              on failure
     */
    protected void removeProlog(
        ServiceHeader header, Object context,
        DataproviderRequest request
    ) throws ServiceException {
        //
    }

    /**
     * This method allows the dataprovider layer to verify the integrity of a 
     * single request before its actual processing.
     *
     * @param       header
     *              the request's service header
     * @param       request
     *              the request
     *
     * @exception   ServiceException
     *              on failure
     */
    protected void findProlog(
        ServiceHeader header, Object context,
        DataproviderRequest request
    ) throws ServiceException {
        //
    }

    /**
     * This method allows the dataprovider layers postprocessing of a 
     * collection of requests as a whole after the actual processing of the 
     * individual requests has been done.
     *
     * @param       header
     *              the unit of work's service header
     * @param       unitOfWork
     *              the request collection
     * @param       replies
     *              the reply collection
     *
     * @exception   ServiceException
     *              must not be thrown unless the request collection is to be 
     *              treated as an atomic processing unit
     */
    protected void epilog(
        ServiceHeader header, 
        Object context,
        UnitOfWorkRequest unitOfWork,
        UnitOfWorkReply reply
    ) throws ServiceException {
        if (reply.failure()) return;
        DataproviderRequest[] requests = unitOfWork.getRequests();
        DataproviderReply[] replies = reply.getReplies();
        for (
                int index = 0;
                index < requests.length;
                index++
        ) epilog(
            header, 
            context,
            requests[index],
            replies[index]
        );
    }

    /**
     * This method allows the dataprovider layer to verify the integrity of a 
     * single reply after the actual processing of the request.
     *
     * @param       header
     *              the request's service header
     * @param       request
     *              the request 
     * @param       reply
     *              the reply
     *
     * @exception   ServiceException
     *              must not be thrown unless the request collection is to be 
     *              treated as an atomic processing unit
     */
    protected void epilog(
        ServiceHeader header, Object context,
        DataproviderRequest request,
        DataproviderReply reply
    ) throws ServiceException {
        switch (request.operation()) {
            case DataproviderOperations.OBJECT_OPERATION:
                operationEpilog(
                    header, context,
                    request,
                    reply
                );
                break;
            case DataproviderOperations.OBJECT_CREATION:
                createEpilog(
                    header, context,
                    request,
                    reply
                );
                break;
            case DataproviderOperations.OBJECT_SETTING:
                setEpilog(
                    header, context,
                    request,
                    reply
                );
                break;
            case DataproviderOperations.OBJECT_MODIFICATION:
            case DataproviderOperations.OBJECT_REPLACEMENT:
                updateEpilog(
                    header, context,
                    request,
                    reply
                );
                break;
            case DataproviderOperations.OBJECT_REMOVAL:
                removeEpilog(
                    header, context,
                    request,
                    reply
                );
                break;
            case DataproviderOperations.ITERATION_START:
            case DataproviderOperations.ITERATION_CONTINUATION:
                findEpilog(
                    header, context,
                    request,
                    reply
                );
                break;
            case DataproviderOperations.OBJECT_RETRIEVAL:
                getEpilog(
                    header, context,
                    request,
                    reply
                );
                break;
        }
    }

    /**
     * This method allows the dataprovider layer to verify the integrity of a 
     * single reply after the actual processing of the request.
     *
     * @param       header
     *              the request's service header
     * @param       request
     *              the request
     * @param       reply
     *              the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    protected void operationEpilog(
        ServiceHeader header, Object context,
        DataproviderRequest request,
        DataproviderReply reply
    ) throws ServiceException {
        //
    }

    /**
     * This method allows the dataprovider layer to verify the integrity of a 
     * single reply after the actual processing of the request.
     *
     * @param       header
     *              the request's service header
     * @param       request
     *              the request
     * @param       reply
     *              the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    protected void getEpilog(
        ServiceHeader header, Object context,
        DataproviderRequest request,
        DataproviderReply reply
    ) throws ServiceException {
        //
    }

    /**
     * This method allows the dataprovider layer to verify the integrity of a 
     * single reply after the actual processing of the request.
     *
     * @param       header
     *              the request's service header
     * @param       request
     *              the request
     * @param       reply
     *              the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    protected void createEpilog(
        ServiceHeader header, Object context,
        DataproviderRequest request,
        DataproviderReply reply
    ) throws ServiceException {
        //
    }

    /**
     * This method allows the dataprovider layer to verify the integrity of a 
     * single reply after the actual processing of the request.
     *
     * @param       header
     *              the request's service header
     * @param       request
     *              the request
     * @param       reply
     *              the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    protected void setEpilog(
        ServiceHeader header, Object context,
        DataproviderRequest request,
        DataproviderReply reply
    ) throws ServiceException {
        //
    }

    /**
     * This method allows the dataprovider layer to verify the integrity of a 
     * single reply after the actual processing of the request.
     *
     * @param       header
     *              the request's service header
     * @param       request
     *              the request
     * @param       reply
     *              the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    protected void updateEpilog(
        ServiceHeader header, Object context,
        DataproviderRequest request,
        DataproviderReply reply
    ) throws ServiceException {
        //
    }

    /**
     * This method allows the dataprovider layer to verify the integrity of a 
     * single reply after the actual processing of the request.
     *
     * @param       header
     *              the request's service header
     * @param       request
     *              the request
     * @param       reply
     *              the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    protected void removeEpilog(
        ServiceHeader header, Object context,
        DataproviderRequest request,
        DataproviderReply reply
    ) throws ServiceException {
        //
    }

    /**
     * This method allows the dataprovider layer to verify the integrity of a 
     * single reply after the actual processing of the request.
     *
     * @param       header
     *              the request's service header
     * @param       request
     *              the request
     * @param       reply
     *              the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    protected void findEpilog(
        ServiceHeader header, Object context,
        DataproviderRequest request,
        DataproviderReply reply
    ) throws ServiceException {
        //
    }


    //------------------------------------------------------------------------
    // Implements Layer_1_0
    //------------------------------------------------------------------------

    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws Exception {
//      this.statistics = (LayerStatistics_1_0)configuration.values(
//      SharedConfigurationEntries.LAYER_STATISTICS
//      ).get(
//      id
//      );
        this.configuration = configuration;
        this.delegation = delegation;
        this.id = id;
        SysLog.detail(
            "Activating " + DataproviderLayers.toString(id) + " layer ",
            configuration
        );
    }

    public void deactivate(
    ) throws Exception {
        //
    }

    public Map<String,ConfigurationSpecifier> configurationSpecification() {
        return new HashMap<String,ConfigurationSpecifier>();
    }

    /**
     * Get the object specified by the requests's path 
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
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        throw new UnsupportedOperationException(DATAPROVIDER_ONLY);
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
        throw new UnsupportedOperationException(DATAPROVIDER_ONLY);
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
        throw new UnsupportedOperationException(DATAPROVIDER_ONLY);
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
        throw new UnsupportedOperationException(DATAPROVIDER_ONLY);
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
        throw new UnsupportedOperationException(DATAPROVIDER_ONLY);
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
        throw new UnsupportedOperationException(DATAPROVIDER_ONLY);
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
        throw new UnsupportedOperationException(DATAPROVIDER_ONLY);
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
        throw new UnsupportedOperationException(DATAPROVIDER_ONLY);
    }

    /**
     * This method allows the dataprovider layers to verify the integrity of a 
     * collection of requests as a whole before the actual processing of the 
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
        throw new UnsupportedOperationException(DATAPROVIDER_ONLY);
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
        throw new UnsupportedOperationException(DATAPROVIDER_ONLY);
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
        throw new UnsupportedOperationException(DATAPROVIDER_ONLY);
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
            ServiceException[] stati = new ServiceException[workingUnits.length];
            Object[] contexts = new Object[workingUnits.length];
            contextInitialization(
                header,
                contexts
            );
            for (
                    int index = 0;
                    index < workingUnits.length;
                    index++
            ) try {
                prolog(
                    header, 
                    contexts[index],
                    workingUnits[index]
                );
            } catch (ServiceException exception) {
                stati[index] = exception;
                workingUnits[index] = NULL_WORKING_UNIT;
            }
            UnitOfWorkReply[] result = delegation.process(
                header, 
                workingUnits
            );
            for (
                    int index = 0;
                    index < workingUnits.length;
                    index++
            ) if (stati[index] == null) try {
                epilog(
                    header, 
                    contexts[index],
                    workingUnits[index],
                    result[index]
                );
//              if (this.statistics != null) this.statistics.update(
//              workingUnits[index].getRequests(),
//              result[index].getReplies()
//              );
            } catch (ServiceException exception) {
                result[index] = new UnitOfWorkReply(exception);
                //      if (this.statistics != null) this.statistics.update(
                //          requests,
                //          null
                //      );
            } else {
                result[index] = new UnitOfWorkReply(stati[index]);
                //      if (this.statistics != null) this.statistics.update(
                //          requests,
                //          null
                //      );
            }
            return result;
        } catch (RuntimeException exception) {
            throw new RuntimeServiceException(exception).log();
        }
    }


    //------------------------------------------------------------------------
    // Constants
    //------------------------------------------------------------------------

    final static private String DATAPROVIDER_ONLY = 
        DelegatingLayer_0.class.getName()
        + " supports Datatprovider_1_0 processing methods only";


    final static private UnitOfWorkRequest NULL_WORKING_UNIT = 
        new UnitOfWorkRequest(false,new DataproviderRequest[]{});


    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------

    /**
     * The layer's id
     */
    private short id;

    /**
     *
     */
    private Dataprovider_1_0 delegation;

    /**
     *
     */
    private Configuration configuration;

//  /**
//  * statistics object
//  */
//  private LayerStatistics_1_0 statistics;

}
