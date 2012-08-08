/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_2Connection.java,v 1.9 2009/06/09 12:45:19 hburger Exp $
 * Description: Dataprovider_2Connection 
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 12:45:19 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionMetaData;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.application.Version;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderOperations;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.DataproviderRequestContexts;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.QualityOfService;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.application.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.AttributeSpecifier;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Directions;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.RestFunction;
import org.openmdx.base.resource.cci.ResultRecord;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.base.rest.spi.RestConnection;
import org.openmdx.base.text.conversion.JavaBeans;
import org.openmdx.kernel.exception.BasicException;

/**
 * Dataprovider_2Connection
 *
 */
public class Dataprovider_2Connection implements RestConnection {

    //-------------------------------------------------------------------------
    public Dataprovider_2Connection(
        Dataprovider_1_0 dataprovider
    ) {
        this.dataprovider = dataprovider;
    }
    
    //-------------------------------------------------------------------------
    protected Model_1_0 getModel(
    ){
        return model == null ? 
            model = Model_1Factory.getModel() : 
            model;
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestConnection#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record)
     */
    @SuppressWarnings("unchecked")
    public Record execute(
        InteractionSpec ispec, 
        Record input
    ) throws ResourceException {
        if(ispec instanceof RestInteractionSpec){
            RestInteractionSpec interactionSpec = (RestInteractionSpec) ispec;
            //
            // Prepare Service Header
            //
            ServiceHeader serviceHeader = new ServiceHeader(
                interactionSpec.getPrincipalChain(),
                null, // correlationId
                false, // traceRequest
                QualityOfService.STANDARD,
                null, // requestedAt
                null // requestedFor
            );
            //
            // Prepare Request
            //
            List<DataproviderRequest> requests = new ArrayList<DataproviderRequest>();
            RestFunction function = interactionSpec.getFunction();
            boolean iteration = false;
            if(input instanceof MappedRecord) {
                try {
                    MappedRecord record = (MappedRecord) input;
                    switch(function) {
                        case GET: {
                            Query_2Facade facade = Query_2Facade.newInstance(record);
                            Path xri = facade.getPath(); 
                            ObjectHolder_2Facade requestCandidates = ObjectHolder_2Facade.newInstance(xri);
                            if(xri.size() % 2 == 0) {
                                Number number = facade.getPosition();
                                int requestPosition = number == null ? 0 : number.intValue(); 
                                number = facade.getSize();
                                int requestSize = number == null ? Integer.MAX_VALUE : number.intValue();
                                Object recordFilter = facade.getQuery();
                                FilterProperty[] requestFilter = null;
                                AttributeSpecifier[] requestOrdering  = null;
                                if(recordFilter != null) {
                                    Filter filter = (Filter)JavaBeans.fromXML(
                                        ((CharSequence)recordFilter)
                                    );
                                    if(filter != null) {
                                        //
                                        // Filter
                                        //
                                        List<FilterProperty> filterProperties = new ArrayList<FilterProperty>();
                                        for(Condition condition : filter.getCondition()) {
                                            filterProperties.add(
                                                new FilterProperty(
                                                    condition.getQuantor(),
                                                    condition.getFeature(),
                                                    FilterOperators.fromString(condition.getName()),
                                                    condition.getValue()
                                                )
                                            );
                                        }
                                        requestFilter = filterProperties.toArray(
                                            new FilterProperty[filterProperties.size()]
                                        );
                                        //
                                        // Ordering
                                        //
                                        List<AttributeSpecifier> attributeSpecifiers = new ArrayList<AttributeSpecifier>();
                                        for(OrderSpecifier orderSpecifier : filter.getOrderSpecifier()) {
                                            attributeSpecifiers.add(
                                                new AttributeSpecifier(
                                                    orderSpecifier.getFeature(),
                                                    0, // position
                                                    orderSpecifier.getOrder()
                                                )
                                            );
                                        }
                                        requestOrdering = attributeSpecifiers.toArray(
                                            new AttributeSpecifier[attributeSpecifiers.size()]
                                        );
                                    }
                                }
                                iteration = true;
                                requests.add(
                                    new DataproviderRequest(
                                        requestCandidates.getDelegate(),
                                        DataproviderOperations.ITERATION_START,
                                        requestFilter,
                                        requestPosition,
                                        requestSize,
                                        Directions.ASCENDING,
                                        AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                                        requestOrdering 
                                    )
                                );
                            } 
                            else {
                                Object recordFilter = facade.getQuery();
                                List<AttributeSpecifier> attributeSpecifiers = new ArrayList<AttributeSpecifier>();
                                if(recordFilter != null) {
                                    Filter filter = (Filter)JavaBeans.fromXML(
                                        ((CharSequence)recordFilter)
                                    );
                                    for(OrderSpecifier orderSpecifier : filter.getOrderSpecifier()) {
                                        attributeSpecifiers.add(
                                            new AttributeSpecifier(
                                                orderSpecifier.getFeature()
                                            )
                                        );
                                    }
                                }
                                requests.add(
                                    new DataproviderRequest(
                                        requestCandidates.getDelegate(),
                                        DataproviderOperations.OBJECT_RETRIEVAL,
                                        AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                                        attributeSpecifiers.toArray(new AttributeSpecifier[attributeSpecifiers.size()])
                                    )
                                );
                            }
                        } break;
                        case DELETE: {
                            if(Query_2Facade.isDelegate(record)) {
                                Query_2Facade facade = Query_2Facade.newInstance(record);
                                Path candidates = facade.getPath();
                                if(candidates.size() % 2 == 0) {
                                    // TODO support delete by query
                                    throw new NotSupportedException(
                                        "Delete by query not yet supported"
                                    ); 
                                } 
                                else {
                                    requests.add(
                                        new DataproviderRequest(
                                            record,
                                            DataproviderOperations.OBJECT_REMOVAL,
                                            AttributeSelectors.NO_ATTRIBUTES,
                                            null // attributeSpecifier
                                        )
                                    );
                                }
                            } 
                            else {
                                requests.add(
                                    new DataproviderRequest(
                                        record,
                                        DataproviderOperations.OBJECT_REMOVAL,
                                        AttributeSelectors.NO_ATTRIBUTES,
                                        null // attributeSpecifier
                                    )
                                );
                            }
                        } break;
                        case POST: {
                            Set<Map.Entry<?,?>> entries = record.entrySet();
                            for(Map.Entry<?,?> entry : entries) {
                                MappedRecord object = (MappedRecord) entry.getValue();
                                ObjectHolder_2Facade request = ObjectHolder_2Facade.newInstance(new Path(entry.getKey().toString()));
                                request.setValue(object);
                                request.setVersion(null);
                                requests.add(
                                    new DataproviderRequest(
                                        request.getDelegate(),
                                        getModel().isStructureType(object.getRecordName()) ? 
                                            DataproviderOperations.OBJECT_OPERATION : 
                                            DataproviderOperations.OBJECT_CREATION,
                                        AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                                        null // attributeSpecifier
                                    )
                                );
                            }
                        } break;
                        case PUT: {
                            requests.add(
                                new DataproviderRequest(
                                    record,
                                    DataproviderOperations.OBJECT_REPLACEMENT,
                                    AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                                    null // attributeSpecifier
                                )
                            );
                        } break;
                    }
                } catch (ServiceException exception) {
                    throw ResourceExceptions.initHolder(
                        new ResourceException(
                            "Could not convert input record to request",
                            BasicException.newEmbeddedExceptionStack(
                                exception,
                                BasicException.Code.DEFAULT_DOMAIN, 
                                BasicException.Code.TRANSFORMATION_FAILURE 
                            )
                        )
                    );
                } catch (RuntimeException exception) {
                    throw ResourceExceptions.initHolder(
                        new ResourceException(
                            "Could not convert input record to request",
                            BasicException.newEmbeddedExceptionStack(
                                exception,
                                BasicException.Code.DEFAULT_DOMAIN, 
                                BasicException.Code.TRANSFORMATION_FAILURE 
                            )
                        )
                    );
                }
            } else if (input instanceof IndexedRecord) {
                IndexedRecord record = (IndexedRecord) input;
                for(Object resourceIdentifier : record){
                    if(resourceIdentifier instanceof String) {
                        Path xri = new Path((String)resourceIdentifier);
                        ObjectHolder_2Facade request = ObjectHolder_2Facade.newInstance(xri);
                        switch(function) {
                            case GET:         
                                try {
                                    requests.add(
                                        new DataproviderRequest(
                                            request.getDelegate(),
                                            DataproviderOperations.OBJECT_RETRIEVAL,
                                            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                                            null // attributeSpecifier
                                        )
                                    );
                                }
                                catch(ServiceException e) {
                                    throw new ResourceException(e);
                                }
                                break;
                            case DELETE:
                                try {
                                    requests.add(
                                        new DataproviderRequest(
                                            request.getDelegate(),
                                            DataproviderOperations.OBJECT_REMOVAL,
                                            AttributeSelectors.NO_ATTRIBUTES,
                                            null // attributeSpecifier
                                        )
                                    );
                                }
                                catch(ServiceException e) {
                                    throw new ResourceException(e);
                                }
                                break;
                            default: 
                                throw ResourceExceptions.initHolder(
                                    new ResourceException(
                                        "Unexpected function for indexed record",
                                        BasicException.newEmbeddedExceptionStack(
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.BAD_PARAMETER,
                                            new BasicException.Parameter("supported", RestFunction.GET, RestFunction.DELETE),
                                            new BasicException.Parameter("actual", function)
                                        )
                                    )
                                );
                        }
                    } 
                    else {
                        throw ResourceExceptions.initHolder(
                            new ResourceException(
                                "Unexpected function name",
                                BasicException.newEmbeddedExceptionStack(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.BAD_PARAMETER,
                                    new BasicException.Parameter("supported", String.class.getName()),
                                    new BasicException.Parameter("actual", function)
                                )
                            )                        
                        );
                    }
                }                    
            } else throw ResourceExceptions.initHolder(
                new ResourceException(
                    "Unexpected input record",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("supported", MappedRecord.class.getName(), IndexedRecord.class.getName()),
                        new BasicException.Parameter("actual", input == null ? null : input.getClass().getName())
                    )
                )
            );
            //
            // Process Request
            //
            for(DataproviderRequest request : requests) {
                request.context(DataproviderRequestContexts.LENIENT).set(0, Boolean.TRUE);
            }
            UnitOfWorkRequest request = new UnitOfWorkRequest(
                false, // new-transaction
                requests.toArray(new DataproviderRequest[requests.size()])
            );
            UnitOfWorkReply reply = this.dataprovider.process(
                serviceHeader, 
                request
            )[0];
            //
            // Prepare Reply Record
            //
            if(reply.failure()){
                ServiceException cause = reply.getStatus();
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        cause.getCause().getDescription(),
                        BasicException.newEmbeddedExceptionStack(cause)
                    )
                );
            } else {
                IndexedRecord replyRecord = Records.getRecordFactory().createIndexedRecord(Multiplicities.LIST);
                for(DataproviderReply r : reply.getReplies()) {
                    if(iteration && replyRecord instanceof ResultRecord) {
                        ResultRecord resultRecord = (ResultRecord) replyRecord;
                        Map<?,?> contexts = r.contexts();
                        if(contexts.containsKey(DataproviderReplyContexts.TOTAL)) {
                            Number total = (Number) r.context(DataproviderReplyContexts.TOTAL).get(0);
                            if(total != null) {
                                resultRecord.setTotal(total.longValue());
                            }
                        }
                        if(contexts.containsKey(DataproviderReplyContexts.HAS_MORE)) {
                            Boolean hasMore = (Boolean) r.context(DataproviderReplyContexts.HAS_MORE).get(0);
                            if(hasMore != null) {
                                resultRecord.setHasMore(hasMore.booleanValue());
                            }
                        }
                    }
                    for (MappedRecord o : r.getObjects()) {
                        replyRecord.add(o);
                    }
                }
                return replyRecord;
            }
        } else {
            throw ResourceExceptions.initHolder(
                new ResourceException(
                    "Unexpected interaction spec",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("supported", RestInteractionSpec.class.getName()),
                        new BasicException.Parameter("actual", ispec == null ? null : ispec.getClass().getName())
                    )
                )
            );
        }
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestConnection#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
     */
    public boolean execute(
        InteractionSpec ispec, 
        Record input, 
        Record output
    ) throws ResourceException {
        throw new ResourceException(
            "Execute with output record not yet implemented",
            BasicException.newEmbeddedExceptionStack(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_IMPLEMENTED
            )
        ); // TODO
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestConnection#getMetaData()
     */
    public ConnectionMetaData getMetaData(
    ) throws ResourceException {
        return metaData;
    }

    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------    
    private final static ConnectionMetaData metaData = 
        new ConnectionMetaData(){
            /**
             * It's an openMDX Dataprovider connection
             */
            public String getEISProductName(
            ) throws ResourceException {
                return "openMDX/Dataprovider";
            }
            /**
             * with the given openMDX version
             */
            public String getEISProductVersion(
            ) throws ResourceException {
                return Version.getSpecificationVersion();
            }
            /**
             * Propagate the factory's security information
             */
            public String getUserName(
            ) throws ResourceException {
                return null;
            }        
        };        
    private static Model_1_0 model = null;
    
    private final Dataprovider_1_0 dataprovider;
}
