/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Layer_1.java,v 1.16 2008/02/29 15:22:03 hburger Exp $
 * Description: User Profile Service
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/29 15:22:03 $
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
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequestContexts;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * A transparent implementation of Layer_1_0, i.e. all calls are delegated.
 */
public class Layer_1
    extends AbstractLayer_1
    implements Layer_1_2
{

    /**
     * Get the layer's id
     */
    protected final short getId(
    ){
        return this.id;
    }

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

    /**
     * The layer this layer delegates to
     *
     * @return  the layer to delegate to
     */
    protected final Layer_1_0 getDelegation(
    ){
        return this.delegation;
    }

    protected boolean isLenient(
        DataproviderRequest request
    ){
        return Boolean.TRUE.equals(request.context(DataproviderRequestContexts.LENIENT).get(0));
    }
    
    /**
     * The layer this layer delegates to
     *
     * @return  the layer to delegate to
     */
    protected final Layer_1_0 getDelegation(
        DataproviderRequest request
    ){
         return isLenient(request) ? getLenientDelegation() : getDelegation();
    }
    
    /**
     * Says whether this layer is the terminal layer or not
     *
     * @return  true if there is no layer to delegate to
     */
    protected final boolean terminal(
    ){
        return this.delegation == null;
    }


    //------------------------------------------------------------------------
    // Implements Layer_1_2
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1_2#epilog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest, org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply)
     */
    public void epilog(
        ServiceHeader header,
        UnitOfWorkRequest request,
        UnitOfWorkReply reply
    ) {
        if(this.delegation instanceof Layer_1_2) {
            ((Layer_1_2)this.delegation).epilog(header, request, reply);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1_2#prolog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest)
     */
    public void prolog(
        ServiceHeader header, 
        UnitOfWorkRequest request
    ) {
        if(this.delegation instanceof Layer_1_2) {
            ((Layer_1_2)this.delegation).prolog(header, request);
        }
    }

    
    //------------------------------------------------------------------------
    // Implements Layer_1_1
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1_1#getLenientProcessor()
     */
    public Layer_1_0 getLenientProcessor() {
        return this.bypassedByLenientRequests ? getLenientDelegation() : this;
    }

    /**
     * Retrieve a delegate processing lenient requests
     * 
     * @return a delegate processing lenient requests
     */
    private Layer_1_0 getLenientDelegation() {
        return this.delegation instanceof Layer_1_1 ? 
            ((Layer_1_1)this.delegation).getLenientProcessor() :
            this.delegation;
    }
    
    
    //------------------------------------------------------------------------
    // Implements Layer_1_0
    //------------------------------------------------------------------------

    /**
     * This layer's specific configuration specifiers.
     * <p>
     * Usage:
     * <pre>
     *   Map specification = super.configurationSpecification();
     *   specification.put(
     *     "specificOption1", new ConfigurationSpecifier([...])
     *   );
     *   specification.put(
     *     "specificOption2", new ConfigurationSpecifier([...])
     *   );
     *   [...]
     *   return specification;
     * </pre>
     *
     * @return  a map with id/ConfigurationSpecifier entries
     */
    public Map<String,ConfigurationSpecifier> configurationSpecification(){
        return new HashMap<String,ConfigurationSpecifier>();
    }


    /**
     * Activates a dataprovider layer
     * 
     * @param   id              the dataprovider layer's id
     * @param   configuration   the dataprovider'a configuration
     *  <dl>
     *      <dt>namespaceId</dt>            <dd>String</dd>
     *      <dt>exposedPath</dt>            <dd>Path[]</dd>
     *  </dl>
     * @param       delegation
     *              the layer to delegate to;
     *              or null if "persistenceLayer".equals(id)
     *
     * @exception   Exception
     *              unexpected exceptions
     * @exception   ServiceException
     *              expected exceptions
     */
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws Exception, ServiceException{
        super.activate(id, configuration, delegation);
        this.statistics = (LayerStatistics_1_0)configuration.values(
            SharedConfigurationEntries.LAYER_STATISTICS
        ).get(
            id
        );
        this.configuration = configuration;
        this.delegation = delegation;
        this.id = id;
        this.bypassedByLenientRequests = configuration.isOn(
            SharedConfigurationEntries.BYPASSED_BY_LENIENT_REQUESTS
        );
        SysLog.detail(
            "Activating " + DataproviderLayers.toString(id) + " layer " + getClass().getName(),
             configuration
        );
    }

    /**
     * Deactivates a dataprovider layer
     * <p>
     * Subclasses overriding this method have to apply the following pattern:
     * <pre>
     *  public void deactivate(
     *  ) throws Exception, ServiceException {
     *      // local deactivation code
     *      super.deactivate();
     *  }
     * </pre>       
     *
     * @exception   Exception
     *              unexpected exceptions
     * @exception   ServiceException
     *              expected exceptions
     */
    public void deactivate(
    ) throws Exception, ServiceException{
        super.deactivate();
        SysLog.info(
            DataproviderLayers.toString(getId()) + " layer deactivated"
        );
        this.configuration = null;
        this.delegation = null;
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
        Layer_1_0 delegation = getDelegation(request);
        return delegation == null ? null : delegation.get(header,request);
    }

    /**
     * Request to start publishing the data stream specified by the requests's
     * path 
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
        Layer_1_0 delegation = getDelegation(request);
        return delegation == null ? null : delegation.startPublishing(
            header,
            request
        );
    }

    /**
     * Get the objects specified by the references and filter properties
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
    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        Layer_1_0 delegation = getDelegation(request);
        return delegation == null ? null : delegation.find(header,request);
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
        Layer_1_0 delegation = getDelegation(request);
        return delegation == null ? null : delegation.create(header,request);
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
        Layer_1_0 delegation = getDelegation(request);
        return delegation == null ? null : delegation.modify(header,request);
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
        Layer_1_0 delegation = getDelegation(request);
        return delegation == null ? null : delegation.replace(header,request);
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
        Layer_1_0 delegation = getDelegation(request);
        return delegation == null ? null : delegation.set(header,request);
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
        Layer_1_0 delegation = getDelegation(request);
        return delegation == null ? null : delegation.remove(header,request);
    }

    /**
     * Operation request
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
    public DataproviderReply operation(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        Layer_1_0 delegation = getDelegation(request);
        return delegation == null ? null : delegation.operation(header,request);
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
        Layer_1_0 delegation = requests.length == 0 ? null : getDelegation(requests[0]);
        if (delegation != null) {
            delegation.prolog(header,requests);
        }
    }

    /**
     * This method allows the dataprovider layers postprocessing of a 
     * collection of requests as a whole after the actual processing of the 
     * individual requests has been done.
     *
     * @param       header
     *              the requests' service header
     * @param       requests
     *              the request list
     * @param       replys
     *              the reply list
     *
     * @exception   ServiceException
     *              on failure
     */
    public void epilog(
        ServiceHeader header,
        DataproviderRequest[] requests,
        DataproviderReply[] replies
    ) throws ServiceException {
        if(requests.length != replies.length) {
            RuntimeServiceException assertionFailure = new RuntimeServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    null,
                    "The numbers of requests and replies do not match"
            );
            SysLog.error(
                assertionFailure.getMessage(),
                assertionFailure.getCause()
            );
            throw assertionFailure;
        }
        Layer_1_0 delegation = requests.length == 0 ? null : getDelegation(requests[0]);
        if (delegation != null) {
            delegation.epilog(header,requests,replies);
        }
    }


    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0
    //------------------------------------------------------------------------

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
        UnitOfWorkReply[] replies = new UnitOfWorkReply[
            workingUnits.length
        ];
        for(
            int index = 0;
            index < workingUnits.length;
            index++
        ) {
            replies[index] = process(
                header,
                workingUnits[index]
            );
        }
        return replies;
    }

    /**
     * Process one working units
     *
     * @param   header          the service header
     * @param   workingUnits    a collection of working units
     *
     * @return  a collection of working unit replies
     */
    protected UnitOfWorkReply process(
        ServiceHeader header,
        UnitOfWorkRequest unitOfWorkRequest
    ){
        if(unitOfWorkRequest.isTransactionalUnit()) {
            RuntimeServiceException assertionFailure = new RuntimeServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    null,
                    "A layer can't handle transactions"
            );
            SysLog.error(
                assertionFailure.getMessage(),
                assertionFailure.getCause()
            );
            throw assertionFailure;
        }
        DataproviderRequest[] requests = unitOfWorkRequest.getRequests();
        DataproviderReply[] replies = new DataproviderReply[requests.length];
        try {
            int lenient = 0;
            if(this.bypassedByLenientRequests) {
                for(
                    int i = 0;
                    i < requests.length;
                    i++
                ){
                    if(isLenient(requests[i])) {
                        lenient++;
                    }
                }
            }
            if(lenient == 0) {
                if(this.id == DataproviderLayers.INTERCEPTION) {
                    prolog(
                        header,
                        unitOfWorkRequest
                    );
                }
                prolog(header,requests);
                for (
                    int index = 0;
                    index < requests.length;
                    index++
                ) {
                    DataproviderRequest request = requests[index];
                    SysLog.trace(
                        DataproviderLayers.toString(this.id),
                        request
                    );
                    switch (request.operation()) {
                        case DataproviderOperations.OBJECT_OPERATION:
                            replies[index] = operation(header,request);
                        break;
                        case DataproviderOperations.OBJECT_CREATION:
                            replies[index] = create(header,request);
                        break;
                        case DataproviderOperations.OBJECT_MODIFICATION:
                            replies[index] = modify(header,request);
                        break;
                        case DataproviderOperations.OBJECT_REPLACEMENT:
                            replies[index] = replace(header,request);
                        break;
                        case DataproviderOperations.OBJECT_REMOVAL:
                            replies[index] = remove(header,request);
                        break;
                        case DataproviderOperations.OBJECT_SETTING:
                            replies[index] = set(header,request);
                        break;
                        case DataproviderOperations.ITERATION_START:
                        case DataproviderOperations.ITERATION_CONTINUATION:
                            replies[index] = find(header,request);
                        break;
                        case DataproviderOperations.OBJECT_MONITORING:
                            replies[index] = startPublishing(header,request);
                        break;
                        case DataproviderOperations.OBJECT_RETRIEVAL:
                            replies[index] = get(header,request);
                        break;
                    }
                }
                epilog(
                  header,
                  requests,
                  replies
                );
                if (this.statistics != null) this.statistics.update(
                  requests,
                  replies
                );
                UnitOfWorkReply unitOfWorkReply = new UnitOfWorkReply(replies); 
                if(this.id == DataproviderLayers.INTERCEPTION) {
                    epilog(
                        header,
                        unitOfWorkRequest,
                        unitOfWorkReply
                    );
                }
                return unitOfWorkReply;
            } else if(lenient == requests.length) {
                Layer_1_0 delegation = getLenientDelegation();
                if(delegation == null) {
                    RuntimeServiceException configurationFailure = new RuntimeServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION,
                        null,
                        "No layer processes lenient requests"
                    );
                    SysLog.error(
                        configurationFailure.getMessage(),
                        configurationFailure.getCause()
                    );
                    throw configurationFailure;
                } else if(
                    this.id == DataproviderLayers.INTERCEPTION &&
                    delegation instanceof Layer_1_2
                ){
                    ((Layer_1_2)delegation).prolog(
                        header,
                        unitOfWorkRequest
                    );
                    UnitOfWorkReply unitOfWorkReply = delegation.process(
                        header, 
                        new UnitOfWorkRequest[]{unitOfWorkRequest}
                    )[0];
                    ((Layer_1_2)delegation).epilog(
                        header,
                        unitOfWorkRequest,
                        unitOfWorkReply
                    );
                    return unitOfWorkReply;
                } else {
                    return delegation.process(
                        header, 
                        new UnitOfWorkRequest[]{unitOfWorkRequest}
                    )[0];
                }
            } else throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("requests", requests.length),
                    new BasicException.Parameter("lenient requests", lenient)
                },
                "In a unit of work either all requests or none must be lenient"
            );
        } catch (ServiceException exception) {
            if (this.statistics != null) this.statistics.update(
              requests,
              replies
            );
            return new UnitOfWorkReply(exception);
        }
    }


    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------

    /**
     * This flag defines whether this layer is bypassed by lenient requests.
     * 
     * @see SharedConfigurationEntries#BYPASSED_BY_LENIENT_REQUESTS
     * @see DataproviderRequestContexts#LENIENT
     */
    private boolean bypassedByLenientRequests;

    /**
     * The layer's id
     */
    private short id;

    /**
     *
     */
    private Layer_1_0 delegation;

    /**
     *
     */
    private Configuration configuration;

    /**
     * statistics object
     */
    private LayerStatistics_1_0 statistics;

}
