/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Plugin_1.java,v 1.43 2008/09/10 08:55:21 hburger Exp $
 * Description: Plugin_1
 * Revision:    $Revision: 1.43 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:21 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.transport.dispatching;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.openmdx.base.accessor.generic.cci.LargeObject_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.collection.FetchSize;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.collection.Reconstructable;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

//-----------------------------------------------------------------------------
/**
 * openMDX 1 compatibility plugin which dispatches Layer_1_0 requests to 
 * ObjectFactory_1_0 object requests.
 */
abstract public class Plugin_1
extends OperationAwarePlugin_1 
{

    /**
     * This value means that potentially expensive counting has been avoided.
     */
    protected final static Integer UNKNOWN_TOTAL = new Integer(Integer.MAX_VALUE);


    //---------------------------------------------------------------------------
    // Layer_1_0
    //---------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    public void prolog(
        ServiceHeader header,
        DataproviderRequest[] requests
    ) throws ServiceException {  
        super.prolog(
            header, 
            requests
        );
        for(
                int i = 0;
                i < requests.length;
                i++
        ) {
            DataproviderRequest request = requests[i];
            switch(request.operation()) {
                case DataproviderOperations.OBJECT_CREATION :
                    this.createObject(request);
                    break;
                case DataproviderOperations.OBJECT_SETTING : 
                    if(this.retrieveObject(request,false) == null) {
                        this.createObject(request);
                    }
                    break;
                case DataproviderOperations.OBJECT_REPLACEMENT : 
                case DataproviderOperations.OBJECT_MODIFICATION : 
                    this.retrieveObject(request, true);
                    break;
            }
        }
    }

    //---------------------------------------------------------------------------
    // Layer_1
    //---------------------------------------------------------------------------
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        if(this.retrievalSize > 1) {
            List<Path> batch = new ArrayList<Path>();
            batch.add(request.path());
            List<DataproviderObject> result = new ArrayList<DataproviderObject>();
            result.add(
                DataproviderObjectMarshaller.toDataproviderObject(
                    request.path(),        
                    this.retrieveObject(request.path()),
                    request,
                    this.model,
                    batch, 
                    this.retrievalSize
                )        
            );           
            for(
                    int i = 1;
                    i < this.retrievalSize && i < batch.size();
                    i++
            ) {
                try {
                    Path path = batch.get(i);
                    result.add(
                        DataproviderObjectMarshaller.toDataproviderObject(
                            path,        
                            this.retrieveObject(path),
                            Collections.EMPTY_SET,
                            this.model,
                            batch, 
                            this.retrievalSize
                        )        
                    );   
                    SysLog.trace("Referenced object retrieved", path);
                } 
                catch (Exception exception) {
                    new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.GENERIC,
                        "Retrieval of referenced object failed",
                        new BasicException.Parameter("requested", request.path()),
                        new BasicException.Parameter("referenced", batch.get(i))
                    ).log(); 
                }
            }
            return new DataproviderReply(result);
        } 
        else {
            return new DataproviderReply(
                DataproviderObjectMarshaller.toDataproviderObject(
                    request.path(),        
                    this.retrieveObject(request.path()),
                    request,
                    this.model,
                    null, 
                    this.retrievalSize
                )
            );
        }
    }

    //---------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        List paths = null;
        FilterableMap container = this.getContainer(
            this.retrieveObject(
                request.path().getParent()
            ),
            request.path().getBase()
        );
        switch(request.operation()) {
            case DataproviderOperations.ITERATION_START:
                paths = container.subMap(
                    request.attributeFilter()
                ).values(
                    request.attributeSpecifier()
                ); 
                break;
            case DataproviderOperations.ITERATION_CONTINUATION:
                paths = container.values(
                    new ByteArrayInputStream(
                        (byte[])request.context(DataproviderReplyContexts.ITERATOR).get(0)
                    )
                );        
                break;
            default:
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "Unexpected operation",
                    new BasicException.Parameter(
                        "operation",
                        DataproviderOperations.toString(request.operation())
                    )
                );
        }

        // complete and return as DataproviderObject
        List result = new ArrayList();
        int replySize = paths instanceof FetchSize 
        ?  ((FetchSize)paths).getFetchSize() 
            : FetchSize.DEFAULT_FETCH_SIZE; 
        if(replySize <= 0) replySize = this.batchSize;
        if(replySize > request.size()) replySize = request.size();
        int replyPosition = request.position();
        if(request.direction() == Directions.DESCENDING) {
            if(replySize > replyPosition) replySize = replyPosition + 1;
            replyPosition = replyPosition + 1 - replySize;
        }
        int ii = 0;
        ListIterator iterator = null;
        for(
                iterator = paths.listIterator(replyPosition);
                iterator.hasNext() && ii < replySize;
                ii++
        ) {
            Object element = iterator.next();
            Path path;
            Object_1_0 source;
            if(element instanceof Object_1_0) {
                source = (Object_1_0)element;
                path = source.objGetPath();
            } 
            else {
                path = (Path)element;
                source = retrieveObject(path);
            }
            result.add(
                DataproviderObjectMarshaller.toDataproviderObject(
                    path,
                    source,
                    request,
                    this.model
                )
            );
        }
        DataproviderReply reply = new DataproviderReply(result);
        /**
         * Try to set context. If collection does not support the features
         * do not set the context
         */
        // HAS_MORE
        boolean hasMore = iterator.hasNext();
        try {
            reply.context(DataproviderReplyContexts.HAS_MORE).set(
                0,
                Boolean.valueOf(hasMore)
            );
        } 
        catch(Exception e) {
            new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC,
                "Reply context population failure",
                new BasicException.Parameter []{
                    new BasicException.Parameter("path", request.path()),
                    new BasicException.Parameter("context", DataproviderReplyContexts.HAS_MORE)                    
                }
            ).log();
        }
        // TOTAL
        try {
            reply.context(DataproviderReplyContexts.TOTAL).set(
                0,
                hasMore ? UNKNOWN_TOTAL : Integer.valueOf(replyPosition + result.size())     
            );
        } 
        catch(Exception e) {
            new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC,
                "Reply context population failure",
                new BasicException.Parameter []{
                    new BasicException.Parameter("path", request.path()),
                    new BasicException.Parameter("context", DataproviderReplyContexts.TOTAL)                    
                }
            ).log();
        }
        /**
         * ITERATOR. collection must be serializable in case an iteration
         * is required. If not throw an exception.
         */
        if(hasMore) {
            if(paths instanceof Reconstructable) {
                try {
                    ByteArrayOutputStream stream=new ByteArrayOutputStream();
                    ((Reconstructable)paths).write(stream);
                    reply.context(DataproviderReplyContexts.ITERATOR).add(
                        stream.toByteArray()
                    );
                } 
                catch (ServiceException exception){
                    throw new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.TOO_LARGE_RESULT_SET,
                        "A list exceeding the given limit " +
                        "could not be prepared for reconstruction",
                        new BasicException.Parameter("size", paths.size()),
                        new BasicException.Parameter("limit", String.valueOf(replySize))
                    );
                }
            } 
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.TOO_LARGE_RESULT_SET,
                    "A non-reconstructable list's size exceeds the given limit",
                    new BasicException.Parameter("size", paths.size()),
                    new BasicException.Parameter("limit",String.valueOf(replySize)),
                    new BasicException.Parameter("class",paths.getClass().getName())
                );
            }
        }
        // ATTRIBUTE_SELECTOR
        reply.context(DataproviderReplyContexts.ATTRIBUTE_SELECTOR).set(
            0,
            new Short(request.attributeSelector())
        );
        return reply;
    }

    //---------------------------------------------------------------------------
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest    request
    ) throws ServiceException {
        Path identity = request.path();
        Object_1_0 target = (Object_1_0)this.objectCache.get(identity);
        if(target == null) target = createObject(request);
        Path parentPath = identity.getPrefix( identity.size() - 2);
        Object_1_0 parent = (Object_1_0)this.objectCache.get(parentPath);
        if(parent==null) parent = this.retrieveObject(parentPath);
        DataproviderObjectMarshaller.toObject(    
            null,
            request.object(),
            target,
            this.objectCache,
            this.objectFactory,
            this.model, true
        );
        target.objMove(
            parent.objGetContainer(
                identity.get(parentPath.size())
            ),
            identity.getBase()
        );
        identity.setTo(target.objGetPath());
        return new DataproviderReply(
            request.object()
        );
    }

    //---------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest    request
    ) throws ServiceException {
        Object_1_0 target = (Object_1_0)this.objectCache.get(request.path());
        if(target == null) target = this.retrieveObject(request, true);
        DataproviderObjectMarshaller.toObject(
            null,
            request.object(),
            target,
            this.objectCache,
            this.objectFactory,
            this.model, false
        );
        if(request.attributeSelector() == AttributeSelectors.NO_ATTRIBUTES) {
            DataproviderObject reply = new DataproviderObject(
                request.path()
            );
            reply.values(SystemAttributes.OBJECT_CLASS).add(
                target.objGetClass()
            );
            return new DataproviderReply(reply);          
        }
        else {
            return this.get(
                header,
                request
            );
        }
    }

    //---------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest    request
    ) throws ServiceException {
        Object_1_0 target = (Object_1_0)this.objectCache.get(request.path());
        if(target == null) target = this.retrieveObject(request, true);
        DataproviderObjectMarshaller.toObject(
            null,
            request.object(),
            target,
            this.objectCache,
            this.objectFactory,
            this.model, true
        );        
        if(request.attributeSelector() == AttributeSelectors.NO_ATTRIBUTES) {
            DataproviderObject reply = new DataproviderObject(
                request.path()
            );
            reply.values(SystemAttributes.OBJECT_CLASS).add(
                target.objGetClass()
            );
            return new DataproviderReply(reply);        
        }
        else {
            return new DataproviderReply(
                DataproviderObjectMarshaller.toDataproviderObject(
                    request.path(),
                    target,
                    request,
                    this.model
                )
            );
        }
    }

    //---------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public DataproviderReply set(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        Boolean objectIsNew = (Boolean)this.objectIsNew.get(request.path());
        if(
                (objectIsNew != null && objectIsNew.booleanValue()) || 
                this.retrieveObject(request, false) == null
        ){
            DataproviderObject_1_0 object = request.object();
            // exclude Authority, Provider, Segment
            if(object.path().size() > 5) {
                object.clearValues(SystemAttributes.CREATED_BY).addAll(
                    object.values(SystemAttributes.MODIFIED_BY)
                );
                object.clearValues(SystemAttributes.CREATED_AT).addAll(
                    object.values(SystemAttributes.MODIFIED_AT)
                );
            }
            return this.create(header,request); 
        } 
        else {
            return this.replace(header, request);
        }
    }

    //---------------------------------------------------------------------------
    public DataproviderReply remove(
        ServiceHeader header,
        DataproviderRequest    request
    ) throws ServiceException {
        DataproviderReply reply = this.get(
            header,
            request
        );
        Object_1_0 target = this.retrieveObject(
            request.path()
        );
        target.objRemove();
        return reply;
    }

    //---------------------------------------------------------------------------  
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.StreamOperationAwareLayer_1#getStreamOperation(org.openmdx.compatibility.base.naming.Path, java.lang.String, java.io.OutputStream, long)
     */
    @Override
    protected DataproviderObject getStreamOperation(
        ServiceHeader header,
        Path objectPath,
        String feature,
        OutputStream value, 
        long position, 
        Path replyPath
    ) throws ServiceException {
        LargeObject_1_0 object = this.retrieveObject(objectPath).objGetLargeObject(feature); 
        object.getBinaryStream(value, position);
        return this.createResponse(
            replyPath, 
            object.length()
        );
    }

    //---------------------------------------------------------------------------  
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.StreamOperationAwareLayer_1#getStreamOperation(org.openmdx.compatibility.base.naming.Path, java.lang.String, java.io.Writer, long)
     */
    @Override
    protected DataproviderObject getStreamOperation(
        ServiceHeader header,
        Path objectPath,
        String feature,
        Writer value, 
        long position, 
        Path replyPath
    ) throws ServiceException {
        LargeObject_1_0 object = this.retrieveObject(objectPath).objGetLargeObject(feature); 
        object.getCharacterStream(value, position);
        return this.createResponse(
            replyPath, 
            object.length()
        );
    }

}

//--- End of File -----------------------------------------------------------
