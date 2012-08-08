/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ServiceHeader.java,v 1.11 2008/09/09 12:06:39 hburger Exp $
 * Description: Service Header
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/09 12:06:39 $
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
package org.openmdx.compatibility.base.dataprovider.cci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.resource.ResourceException;

import org.openmdx.base.resource.Records;
import org.openmdx.kernel.text.MultiLineStringRepresentation;

/**
 * This class represents a service header
 */
public final class ServiceHeader
    implements MultiLineStringRepresentation, Serializable, Cloneable
{

    /**
     * 
     */
    private static final long serialVersionUID = 3546359530901812277L;

    /**
     * Constructor
     *
     * @param       principal
     *              The request is processed on behalf of principal.
     * @param       correlationId
     * @param       trace
     *              Define, whether the request should be traced
     *              over all the tiers.
     * @param       qualityOfService
     *              The requested quality of service
     */ 
    public ServiceHeader(
        String principal,
        String correlationId,
        boolean traceRequest,
        QualityOfService qualityOfService
    ){
        this(
            principal, 
            correlationId, 
            traceRequest, 
            qualityOfService, 
            null, // requestedAt
            null // requestedFor
        );
    }

    /**
     * Constructor
     *
     * @param       principal
     *              The request is processed on behalf of principal.
     * @param       correlationId
     * @param       trace
     *              Define, whether the request should be traced
     *              over all the tiers.
     * @param       qualityOfService
     *              The requested quality of service
     * @param       requestedAt
     *              Defines the time point for queries of stateful objects
     * @param       requestedFor
     *              Defines the time point for the data of stateful objects
     */ 
    public ServiceHeader(
        String principal,
        String correlationId,
        boolean traceRequest,
        QualityOfService qualityOfService,
        String requestedAt,
        String requestedFor
    ){
        this(
            principal == null ? 
                NO_PRINCIPALS : 
                Collections.singletonList(principal), 
            correlationId,
            traceRequest,
            qualityOfService,
            requestedAt, 
            requestedFor
        );
    }

    /**
     * Constructor for marshallers.
     *
     * @param       principalChain
     *              The request is processed on behalf of principalChain.
     * @param       correlationId
     * @param       trace
     *              Define, whether the request should be traced
     *              over all the tiers.
     * @param       qualityOfService
     *              The requested quality of service
     * @param       requestedAt
     *              Defines the time point for queries of stateful objects
     * @param       requestedFor
     *              Defines the time point for the data of stateful objects
     */ 
    public ServiceHeader(
        String[] principalChain,
        String correlationId,
        boolean traceRequest,
        QualityOfService qualityOfService,
        String requestedAt,
        String requestedFor
    ){
        this(
            principalChain == null ? 
                NO_PRINCIPALS :
                Arrays.asList(principalChain),
            correlationId,
            traceRequest,
            qualityOfService,
            requestedAt, 
            requestedFor
        );
    }

    /**
     * Constructor for marshallers.
     *
     * @param       principalChain
     *              The request is processed on behalf of principalChain.
     * @param       correlationId
     * @param       trace
     *              Define, whether the request should be traced
     *              over all the tiers.
     * @param       qualityOfService
     *              The requested quality of service
     * @param       requestedAt
     *              Defines the time point for queries of stateful objects
     * @param       requestedFor
     *              Defines the time point for the data of stateful objects
     */ 
    private ServiceHeader(
        List<String> principalChain,
        String correlationId,
        boolean traceRequest,
        QualityOfService qualityOfService,
        String requestedAt,
        String requestedFor
    ){
        this.principalChain = new ArrayList<String>(principalChain);
        this.correlationId = correlationId;
        this.traceRequest = traceRequest;
        this.qualityOfService = qualityOfService;
        this.requestedAt = requestedAt;
        this.requestedFor = requestedFor;
    }
    
    /**
     * Deprecated onstructor for marshallers.
     *
     * @deprecated    Marshallers must handle requestedAt and requestedFor
     */ 
    public ServiceHeader(
        String[] principalChain,
        String correlationId,
        boolean traceRequest,
        QualityOfService qualityOfService
    ){
        this(
            principalChain,
            correlationId,
            traceRequest,
            qualityOfService,
            null, // requestedAt 
            null // requestedFor
        );
    }

    /**
     * Default constructor
     */
    public ServiceHeader(
    ){
        this(
            NO_PRINCIPALS, // principalChain
            null, // correlationId
            false, // traceRequest
            new QualityOfService(),
            null, // requestedAt
            null // requestedFor
        );
    }

    /**
     * The request is processed on behalf of principalChain.
     */
    private final List<String> principalChain;

    /**
     * 
     */
    private final static List<String> NO_PRINCIPALS = Collections.emptyList();
    
    /**
     * For toString
     */
    private final static String[] TO_STRING_CONTENT = {
        "principalChain",
        "correlationId",
        "requestedAt",
        "requestedFor"
    };

    /**
     *
     */
    public List<String> getPrincipalChain(){
        return Collections.unmodifiableList(this.principalChain);
    }
    
    /**
     * Replaces the principal list by one containing the given principal
     */
    public void setPrincipal(
        String principal
    ){
        this.principalChain.clear();
        addPrincipal(principal);
    }
        
    /**
     * Adds a principal to the principal chain
     */
    public void addPrincipal(
        String principal
    ){
        if(principal != null) this.principalChain.add(principal);
    }

    /**
     * The correlation id
     */
    private final String correlationId;

    /**
     * Get the correlation id
     * 
     * @return the correlation id
     */
    public String getCorrelationId(){
        return this.correlationId;
    }
    /**
     * The session id in case of a session based request; "" otherwise.
     * @return the correlation id
     * @deprecated use org.openmdx.compatibility.base.dataprovider.cci.ServcieHeader#getCorrelationId() instead
     */
    public String getSessionId(){
        return getCorrelationId();
    }
    
    /**
     * Define, whether the request should be traced over all the tiers.
     */
    private final boolean traceRequest;
    public boolean traceRequest(){
        return this.traceRequest;
    }

    /**
     *
     */
    private final QualityOfService qualityOfService;
    public QualityOfService getQualityOfService(){
        return this.qualityOfService;
    }

    private final String requestedAt;
    public String getRequestedAt(
    ){
        return this.requestedAt;
    }

    private final String requestedFor;
    public String getRequestedFor(
    ){
        return this.requestedFor;
    }

    
    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone(
    ) throws CloneNotSupportedException {        
        return new ServiceHeader(
             this.principalChain,
             this.correlationId,
             this.traceRequest,
             this.qualityOfService,
             this.requestedAt,
             this.requestedFor
        );
    }


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(
    ){
        try {
            return Records.getRecordFactory().asMappedRecord(
                ServiceHeader.class.getName(),
                null, // short description
                TO_STRING_CONTENT,
                new Object[]{
                    this.principalChain,
                    this.correlationId,
                    this.requestedAt,
                    this.requestedFor
                }
            ).toString();
        } catch (ResourceException exception) {
            return super.toString();
        }
    }

}
