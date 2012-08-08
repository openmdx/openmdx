/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: StreamOperationAwareLayer_1.java,v 1.6 2007/12/25 14:39:00 wfro Exp $
 * Description: Stream Operation Aware Layer_1_0 Implementation
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/25 14:39:00 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.spi;

import java.io.OutputStream;
import java.io.Writer;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;


/**
 * Stream operation aware Layer_1_0 implementation.
 * <p>
 * This class dipatches operation requests to<ul>
 * <li>getStreamOperation
 * <li>otherOperation
 * </ul>
 */
public abstract class StreamOperationAwareLayer_1 extends Layer_1 {

    /**
     * Retrieves a configuration value
     * 
     * @param source
     * @param key
     * @param defaultValue
     * 
     * @return the configuration value or its default
     */
    protected String getConfigurationValue(
        String key,
        String defaultValue
    ){
        Configuration configuration = getConfiguration();
        return configuration.containsEntry(key) && !configuration.values(key).isEmpty() ?
            ((String)configuration.values(key).get(0)) :
            defaultValue;
    }

    /**
     * Retrieves a configuration value
     * 
     * @param source
     * @param key
     * @param defaultValue
     * 
     * @return the configuration value or its default
     */
    protected int getConfigurationValue(
        String key,
        int defaultValue
    ){
        Configuration configuration = getConfiguration();
        return configuration.containsEntry(key) && !configuration.values(key).isEmpty() ?
            ((Number)configuration.values(key).get(0)).intValue() :
            defaultValue;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Operation_1_0#operation(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public final DataproviderReply operation(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        DataproviderObject response = null;
        String operationName = request.path().get(
            request.path().size() - 2
        );
        Path replyPath = request.path().getDescendant(
            new String[]{"reply", super.uidAsString()}
        );
        boolean binaryStream = 
            SystemOperations.GET_BINARY_STREAM.equals(operationName) &&
            SystemOperations.GET_BINARY_STREAM_ARGUMENTS.equals(getRecordType(request));
        boolean characterStream =
            SystemOperations.GET_CHARACTER_STREAM.equals(operationName) &&
            SystemOperations.GET_CHARACTER_STREAM_ARGUMENTS.equals(getRecordType(request));
        if(binaryStream | characterStream){
            //
            // Handle stream retrieval operations
            // 
            SparseList featureAttribute = request.object().getValues(SystemOperations.GET_STREAM_FEATURE);
            SparseList positionAttribute = request.object().getValues(SystemOperations.GET_STREAM_POSITION);
            SparseList valueAttribute = request.object().getValues(SystemOperations.GET_STREAM_VALUE);
            try {
                long position = ((Long)positionAttribute.get(0)).longValue();
                Object stream = valueAttribute.get(0);
                Path objectPath = request.path().getPrefix(request.path().size() - 2);
                String feature = (String) featureAttribute.get(0);
                if(binaryStream) response = getStreamOperation(
                    header, 
                    objectPath, 
                    feature, 
                    (OutputStream)stream, 
                    position, replyPath
                );
                if (characterStream) response = getStreamOperation(
                    header, 
                    objectPath, 
                    feature, 
                    (Writer)stream, 
                    position, replyPath
                 );
            } catch (RuntimeException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("path", request.path()),
                        new BasicException.Parameter(SystemOperations.GET_STREAM_FEATURE, featureAttribute),
                        new BasicException.Parameter(SystemOperations.GET_STREAM_POSITION, positionAttribute),
                    },
                    "Invalid stream operation request"
                );
            }
        } else {
            response = otherOperation(header, request, operationName, replyPath); 
        }
        return response == null ?
            super.operation(header, request) :
            new DataproviderReply(response);         
    }
    
    /**
     * This method is overridden by a subclass if ith supports other operations.
     * 
     * @param header
     * @param request
     * @param operation
     * @param replyPath
     * 
     * @return the reply object; or null if the request should be delegated
     * 
     * @throws ServiceException
     */
    protected DataproviderObject otherOperation(
        ServiceHeader header,
        DataproviderRequest request,
        String operation, 
        Path replyPath
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            new BasicException.Parameter[]{
                new BasicException.Parameter("path", request.path()),
                new BasicException.Parameter("operation", operation),
            },
            "Operation not supported"
        );
    }

    /**
     * This method is usually overridden by a subclass.
     * 
     * @param header 
     * @param objectPath
     * @param feature
     * @param value
     * @param position
     * @param replyPath 
     * 
     * @return the reply object; or null if the request should be delegated
     * 
     * @throws ServiceException
     */
    protected DataproviderObject getStreamOperation(
        ServiceHeader header,
        Path objectPath,
        String feature,
        Writer value, 
        long position, 
        Path replyPath
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            new BasicException.Parameter[]{
                new BasicException.Parameter("path", objectPath),
                new BasicException.Parameter(SystemOperations.GET_STREAM_FEATURE, feature),
                new BasicException.Parameter(SystemOperations.GET_STREAM_POSITION, position),
            },
            "Character stream retrieval not supported"
        );
    }

    /**
     * This method is usually overridden by a subclass.
     * 
     * @param header 
     * @param objectPath
     * @param feature
     * @param value
     * @param position
     * @param replyPath 
     * 
     * @return the reply object; or null if the request should be delegated
     * 
     * @throws ServiceException
     */
    protected DataproviderObject getStreamOperation(
        ServiceHeader header,
        Path objectPath,
        String feature,
        OutputStream value, 
        long position, 
        Path replyPath
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            new BasicException.Parameter[]{
                new BasicException.Parameter("path", objectPath),
                new BasicException.Parameter(SystemOperations.GET_STREAM_FEATURE, feature),
                new BasicException.Parameter(SystemOperations.GET_STREAM_POSITION, position),
            },
            "Binary stream retrieval not supported"
        );
    }

    /**
     * Retrieve an operation request's record type
     * 
     * @param request the operation request
     * 
     * @return the opration request's record type
     */
    private String getRecordType(
        DataproviderRequest request
    ){
        SparseList recordTypeAttribute = request.object().getValues(SystemAttributes.OBJECT_CLASS);
        return (String) recordTypeAttribute.get(0);
    }

    /**
     * Create a get streamm response
     * 
     * @param replyPath
     * @param objectSize
     * 
     * @return a get stream response's dataprovider object
     */
    protected DataproviderObject createResponse(
        Path replyPath,
        long objectSize
    ){
        DataproviderObject result = new DataproviderObject(replyPath);
  	    result.values(SystemAttributes.OBJECT_CLASS).add(SystemOperations.GET_STREAM_RESULT);
        result.values(SystemOperations.GET_STREAM_LENGTH).add(new Long(objectSize));
        return result;
    }

}

