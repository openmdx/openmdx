/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: RequestCollection class
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.cci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;


/**
 * An object request
 */
public final class DataproviderRequestProcessor
    implements Serializable, Cloneable
{

    //------------------------------------------------------------------------
    public DataproviderRequestProcessor(   
        ServiceHeader serviceHeader,
        Dataprovider_1_0 dataprovider
    ){
        try {
            if (serviceHeader == null) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "A RequestCollection's serviceHeader must not be null"
            );
            this.serviceHeader = serviceHeader;
            if (dataprovider == null) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "A RequestCollection's dataprovider must not be null"
            );
            this.dataprovider = dataprovider;
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    //------------------------------------------------------------------------
    private boolean isPreferringNotFoundException(){
        return this.serviceHeader.isPreferringNotFoundException();
    }
    
    //------------------------------------------------------------------------
    /**
     * The requests embedded by beginUnitOfWork() and endUnitOfWork() are 
     * processed together.
     *
     * @exception   ServiceException    ILLEGAL_STATE
     *              if the collection is already in working unit mode
     */
    public void beginBatch(
    ) throws ServiceException {
        if(this.isBatching) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            "Request collection is already in batching mode"
        );
        this.isBatching = true;
    }

    //------------------------------------------------------------------------
    /**
     * The requests embedded by beginUnitOfWork() and endUnitOfWork() are 
     * processed together.
     *
     * @exception   ServiceException    ILLEGAL_STATE
     *              if the collection is not in working unit mode
     * @exception   RuntimeException    
     *              in case of system failure
     */
    public void endBatch(
    ) throws ServiceException {
        if(!this.isBatching) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            "Request collection is not in batching mode"
        );
        List<DataproviderRequest> requests = new ArrayList<DataproviderRequest>(
            this.workingUnitRequests
        );
        this.workingUnitRequests.clear();
        List<DataproviderReplyListener> listeners = new ArrayList<DataproviderReplyListener>(
            this.workingUnitListeners
        );
        this.workingUnitListeners.clear();  
        this.dispatch(
            requests,
            listeners
        );
        this.isBatching = false;
    }

    //------------------------------------------------------------------------
    /**
     * Forget the current batch.
     */
    public void forgetBatch(){
        this.workingUnitRequests.clear();
        this.workingUnitListeners.clear(); 
        this.isBatching = false;
    }

    //------------------------------------------------------------------------
    /**
     * Adds a get request retrieving the typical attributes.
     *
     * @param       path
     *              the object's path
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public ObjectRecord addGetRequest(
        Path path
    ) throws ServiceException {
        return this.addGetRequest(
            path,
            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
            null
        );
    }

    //------------------------------------------------------------------------
    /** 
     * Adds a get request retrieving all attributes specified by either 
     * the <code>attributeSelector</code> or the
     * <code>attributeSpecifier</code>.
     *
     * @param       path
     *              the object's path
     * @param       attributeSelector
     *              A (class dependent) predfined set of attributes to be
     *              returned
     * @param       attributeSpecifier
     *              An array specifying additional attributes to be returned;
     *              this argument may be <code>null</code>.
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public ObjectRecord addGetRequest(
        Path path,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier
    ) throws ServiceException {
        RequestedObject target = new RequestedObject();
        this.addGetRequest(
            path,
            attributeSelector,
            attributeSpecifier,
            target
        );
        if(target.getException() != null) {
            throw target.getException();
        }
        return target;
    }

    //------------------------------------------------------------------------
    /** 
     * Adds a get request retrieving all attributes specified by either 
     * the <code>attributeSelector</code> or the
     * <code>attributeSpecifier</code>.
     *
     * @param       path
     *              the object's path
     * @param       attributeSelector
     *              A (class dependent) predefined set of attributes to be
     *              returned
     * @param       attributeSpecifier
     *              An array specifying additional attributes to be returned;
     *              this argument may be <code>null</code>.
     * @param       listener
     *              The dataprovider reply listener
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public void addGetRequest(
        Path path,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier,
        DataproviderReplyListener listener
    ) throws ServiceException {
        this.dispatch(
            new DataproviderRequest(
                Facades.newQuery(path, isPreferringNotFoundException()).getDelegate(),
                DataproviderOperations.OBJECT_RETRIEVAL,
                attributeSelector,
                attributeSpecifier
            ),
            listener
        );
    }

    //------------------------------------------------------------------------
    /**
     * Adds a create request retrieving the typical attributes.
     *
     * @param       object
     *              thr object to be created
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public MappedRecord addCreateRequest(
        ObjectRecord object
    ) throws ServiceException {
        return this.addCreateRequest(
            object,
            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
            null
        );
    }

    //------------------------------------------------------------------------
    /** 
     * Adds a create request retrieving all attributes specified by either 
     * the <code>attributeSelector</code> or the
     * <code>attributeSpecifier</code>.
     *
     * @param       object
     *              the object to be created
     * @param       attributeSelector
     *              A (class dependent) predefined set of attributes to be
     *              returned
     * @param       attributeSpecifier
     *              An array specifying additional attributes to be returned;
     *              this argument may be <code>null</code>.
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public MappedRecord addCreateRequest(
        ObjectRecord object,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier
    ) throws ServiceException {
        RequestedObject target = new RequestedObject();
        addCreateRequest(
            object,
            attributeSelector,
            attributeSpecifier,
            target
        );
        if(target.getException() != null) {
            throw target.getException();
        }
        return target;
    }

    //------------------------------------------------------------------------
    /** 
     * Adds a create request retrieving all attributes specified by either 
     * the <code>attributeSelector</code> or the
     * <code>attributeSpecifier</code>.
     *
     * @param       object
     *              the object to be created
     * @param       attributeSelector
     *              A (class dependent) predefined set of attributes to be
     *              returned
     * @param       attributeSpecifier
     *              An array specifying additional attributes to be returned;
     *              this argument may be <code>null</code>.
     * @param       listener
     *              The dataprovider reply listener
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public void addCreateRequest(
        ObjectRecord object,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier,
        DataproviderReplyListener listener
    ) throws ServiceException {
        this.dispatch(
            new DataproviderRequest(
                object,
                DataproviderOperations.OBJECT_CREATION,
                attributeSelector,
                attributeSpecifier
            ),
            listener
        );
    }

    //------------------------------------------------------------------------
    /**
     * Adds a replace request retrieving the typical attributes.
     *
     * @param       object
     *              the object to be modified
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public MappedRecord addReplaceRequest(
        ObjectRecord object
    ) throws ServiceException {
        return this.addReplaceRequest(
            object,
            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
            null
        );
    }

    //------------------------------------------------------------------------
    /** 
     * Adds a replace request retrieving all attributes specified by either 
     * the <code>attributeSelector</code> or the
     * <code>attributeSpecifier</code>.
     *
     * @param       object
     *              the object to be modified
     * @param       attributeSelector
     *              A (class dependent) predefined set of attributes to be
     *              returned
     * @param       attributeSpecifier
     *              An array specifying additional attributes to be returned;
     *              this argument may be <code>null</code>.
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public MappedRecord addReplaceRequest(
        ObjectRecord object,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier
    ) throws ServiceException {
        final RequestedObject target = new RequestedObject();
        this.addReplaceRequest(
            object,
            attributeSelector,
            attributeSpecifier,
            target
        );
        if(target.getException() != null) {
            throw target.getException();
        }        
        return target;
    }

    //------------------------------------------------------------------------
    /** 
     * Adds a replace request retrieving all attributes specified by either 
     * the <code>attributeSelector</code> or the
     * <code>attributeSpecifier</code>.
     *
     * @param       object
     *              the object to be modified
     * @param       attributeSelector
     *              A (class dependent) predefined set of attributes to be
     *              returned
     * @param       attributeSpecifier
     *              An array specifying additional attributes to be returned;
     *              this argument may be <code>null</code>.
     * @param       listener
     *              The dataprovider reply listener
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public void addReplaceRequest(
        ObjectRecord object,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier,
        DataproviderReplyListener listener
    ) throws ServiceException {
        this.dispatch(
            new DataproviderRequest(
                object,
                DataproviderOperations.OBJECT_REPLACEMENT,
                attributeSelector,
                attributeSpecifier
            ),
            listener
        );
    }

    //------------------------------------------------------------------------
    /**
     * Adds a remove request retrieving no attributes.
     *
     * @param       path
     *              the object's path
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public MappedRecord addRemoveRequest(
        Path path
    ) throws ServiceException {
        return this.addRemoveRequest(
            path,
            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
            null
        );
    }

    //------------------------------------------------------------------------
    /** 
     * Adds a remove request retrieving all attributes specified by either 
     * the <code>attributeSelector</code> or the
     * <code>attributeSpecifier</code>.
     *
     * @param       path
     *              the object's path
     * @param       attributeSelector
     *              A (class dependent) predfined set of attributes to be
     *              returned
     * @param       attributeSpecifier
     *              An array specifying additional attributes to be returned;
     *              this argument may be <code>null</code>.
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public MappedRecord addRemoveRequest(
        Path path,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier
    ) throws ServiceException {
        final RequestedObject target = new RequestedObject();
        addRemoveRequest(
            path,
            attributeSelector,
            attributeSpecifier,
            target
        );
        if(target.getException() != null) {
            throw target.getException();
        }        
        return target;
    }

    //------------------------------------------------------------------------
    /** 
     * Adds a remove request retrieving all attributes specified by either 
     * the <code>attributeSelector</code> or the
     * <code>attributeSpecifier</code>.
     *
     * @param       path
     *              the object's path
     * @param       attributeSelector
     *              A (class dependent) predefined set of attributes to be
     *              returned
     * @param       attributeSpecifier
     *              An array specifying additional attributes to be returned;
     *              this argument may be <code>null</code>.
     * @param       listener
     *              The dataprovider reply listener
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public void addRemoveRequest(
        Path path,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier,
        DataproviderReplyListener listener
    ) throws ServiceException {
        this.dispatch(
            new DataproviderRequest(
                Facades.newQuery(path, isPreferringNotFoundException()).getDelegate(),
                DataproviderOperations.OBJECT_REMOVAL,
                attributeSelector,
                attributeSpecifier
            ),
            listener
        );
    }

    //------------------------------------------------------------------------
    /**
     * Adds a find request selecting all objects where the
     * <code>referenceFilter</code> as well as the
     * <code>attributeFilter</code> evaluate to true.
     * No attributes are included.
     *
     * @param       referenceFilter
     *              an object may be included into the result sets only if it
     *              is accessible through the path passed as
     *              <code>referenceFilter</code>
     * @param       attributeFilter
     *              an object may be included into the result sets only if all
     *              the filter properties evaluate to true if applied to it; 
     *              this argument may be <code>null</code>.
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    @SuppressWarnings("rawtypes")
    public List addFindRequest(
        Path referenceFilter,
        FilterProperty[] attributeFilter
    ) throws ServiceException {
        return this.addFindRequest(
            referenceFilter,
            attributeFilter,
            AttributeSelectors.NO_ATTRIBUTES,
            0,
            Integer.MAX_VALUE,
            SortOrder.ASCENDING.code()
        );
    }

    //------------------------------------------------------------------------
    /**
     * Adds a find request selecting all objects where the
     * <code>referenceFilter</code> as well as the
     * <code>attributeFilter</code> evaluate to true.
     *
     * @param       referenceFilter
     *              an object may be included into the result sets only if it
     *              is accessible through the path passed as
     *              <code>referenceFilter</code>
     * @param       attributeFilter
     *              an object may be included into the result sets only if all
     *              the filter properties evaluate to true if applied to it; 
     *              this argument may be <code>null</code>.
     * @param       attributeSelector
     *              A (class dependent) predefined set of attributes to be
     *              returned
     * @param       position
     *              Index of the first or last object to be retrieved
     *              depending on the direction
     * @param       size
     *              Maximal number of objects to be returned at once
     * @param       direction   
     *              either ASCENDING or DESCENDING
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    @SuppressWarnings("rawtypes")
    public List addFindRequest(
        Path referenceFilter,
        FilterProperty[] attributeFilter,
        short attributeSelector,
        int position,
        int size,
        short direction
    ) throws ServiceException {
        return this.addFindRequest(
            referenceFilter,
            attributeFilter,
            attributeSelector,
            null, // atributeSpecifier
            position,
            size,
            direction
        );
    }

    //------------------------------------------------------------------------
    /**
     * Adds a find request selecting all objects where the
     * <code>referenceFilter</code> as well as the
     * <code>attributeFilter</code> evaluate to true.
     *
     * @param       referenceFilter
     *              an object may be included into the result sets only if it
     *              is accessible through the path passed as
     *              <code>referenceFilter</code>
     * @param       attributeFilter
     *              an object may be included into the result sets only if all
     *              the filter properties evaluate to true if applied to it; 
     *              this argument may be <code>null</code>.
     * @param       attributeSelector
     *              A (class dependent) predefined set of attributes to be
     *              returned
     * @param       attributeSpecifiers
     *              An array of attribute specifiers
     * @param       position
     *              Index of the first or last object to be retrieved
     *              depending on the direction
     * @param       size
     *              Maximal number of objects to be returned at once
     * @param       direction   
     *              either ASCENDING or DESCENDING
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    @SuppressWarnings("rawtypes")
    public List addFindRequest(
        Path referenceFilter,
        FilterProperty[] attributeFilter,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifiers,
        int position,
        int size,
        short direction
    ) throws ServiceException {
        RequestedList target = new RequestedList(
            clone(),
            referenceFilter,
            attributeFilter,
            attributeSelector,
            attributeSpecifiers
        );
        this.addFindRequest(
            referenceFilter,
            attributeFilter,
            attributeSelector,
            attributeSpecifiers,
            position,
            size,
            direction,
            target
        );
        return target;
    }

    //------------------------------------------------------------------------
    /**
     * Adds a find request selecting all objects where the
     * <code>referenceFilter</code> as well as the
     * <code>attributeFilter</code> evaluate to true.
     *
     * @param       referenceFilter
     *              an object may be included into the result sets only if it
     *              is accessible through the path passed as
     *              <code>referenceFilter</code>
     * @param       attributeFilter
     *              an object may be included into the result sets only if all
     *              the filter properties evaluate to true if applied to it; 
     *              this argument may be <code>null</code>.
     * @param       attributeSelector
     *              A (class dependent) predefined set of attributes to be
     *              returned
     * @param       attributeSpecifier
     *              An array of attribute specifiers
     * @param       position
     *              Index of the first or last object to be retrieved
     *              depending on the direction
     * @param       size
     *              Maximal number of objects to be returned at once
     * @param       direction   
     *              either ASCENDING or DESCENDING
     * @param       listener
     *              The dataprovider reply listener            object.setObjectId(path);

     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public void addFindRequest(
        Path referenceFilter,
        FilterProperty[] attributeFilter,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier,
        int position,
        int size,
        short direction,
        DataproviderReplyListener listener
    ) throws ServiceException {
        this.dispatch(
            new DataproviderRequest(
                Facades.newQuery(referenceFilter, isPreferringNotFoundException()).getDelegate(),
                DataproviderOperations.ITERATION_START,
                attributeFilter,
                position,
                size,
                direction,
                attributeSelector,
                attributeSpecifier
            ),
            listener
        );
    }

    //------------------------------------------------------------------------
    /**
     * Ad an operation request
     *
     * @param       request
     *              the request object
     *
     * @return      the reply
     *
     * @exception   ServiceException BAD_PARAMETER
     *              if the path refers to an object, not an operation
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public MessageRecord addOperationRequest(
        MessageRecord request
    ) throws ServiceException {
        final RequestedMessage reply = new RequestedMessage();
        addOperationRequest(
            request, reply
        );
        return reply;
    }

    //------------------------------------------------------------------------
    /**
     * Ad an operation request
     *
     * @param       request
     *              the request object
     * @param       listener
     *              The dataprovider reply listener
     *
     * @exception   ServiceException BAD_PARAMETER
     *              if the path refers to an object, not an operation
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public void addOperationRequest(
        MessageRecord request,
        DataproviderReplyListener listener
    ) throws ServiceException {
        try {
            if(request.getMessageId() == null) {
            	request.setResourceIdentifier(request.getResourceIdentifier().getChild(uuidAsString()));
            }
            this.dispatch(
                new DataproviderRequest(
                    request,
                    DataproviderOperations.OBJECT_OPERATION,
                    AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                    null
                ),
                listener
            );
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    //------------------------------------------------------------------------
    protected void dispatch(
        DataproviderRequest request,
        DataproviderReplyListener listener
    ) throws ServiceException {
        if (this.isBatching){
            this.workingUnitRequests.add(request);
            this.workingUnitListeners.add(listener);
        } 
        else {
            this.dispatch(
                Arrays.asList(request),
                Arrays.asList(listener)
            );
        }
    }

    //------------------------------------------------------------------------
    protected List<DataproviderReply> dispatch(
        List<DataproviderRequest> requests,
        List<DataproviderReplyListener> listeners
    ){
        List<DataproviderReply> replies = new ArrayList<DataproviderReply>();
        ServiceException status = this.dataprovider.process(
            this.serviceHeader,
            requests,
            replies
        );
        if(status != null) {
            for (
                int requestIndex = 0;
                requestIndex < listeners.size();
                requestIndex++
            ) {
                listeners.get(requestIndex).onException(
                    status
                );
            }
        } 
        else {
            for (
                int requestIndex = 0;
                requestIndex < listeners.size();
                requestIndex++
            ) {
                listeners.get(requestIndex).onReply(
                    replies.get(requestIndex)
                );
            }
        }
        return replies;     
    }

    //------------------------------------------------------------------------
    @Override
    public DataproviderRequestProcessor clone(  
    ){
        return new DataproviderRequestProcessor(
            this.serviceHeader,
            this.dataprovider
        );
    }

    //------------------------------------------------------------------------
    protected final String uuidAsString(
    ){
        return UUIDs.newUUID().toString();
    }

    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------
    private static final long serialVersionUID = 9050309167977617777L;

    final protected Dataprovider_1_0 dataprovider;
    protected boolean isBatching = false;
    final protected List<DataproviderRequest> workingUnitRequests = new ArrayList<DataproviderRequest>();
    final protected List<DataproviderReplyListener> workingUnitListeners = new ArrayList<DataproviderReplyListener>();
    final protected ServiceHeader serviceHeader;

}
