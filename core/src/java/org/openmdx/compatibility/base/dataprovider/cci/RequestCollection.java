/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RequestCollection.java,v 1.27 2008/10/14 16:05:33 hburger Exp $
 * Description: RequestCollection class
 * Revision:    $Revision: 1.27 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/10/14 16:05:33 $
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
import java.util.Date;
import java.util.List;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;
import org.openmdx.kernel.log.SysLog;


/**
 * An object request
 */
public final class RequestCollection
    implements Serializable, Cloneable, IterationProcessor, DataproviderProcessor_1_0
{

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 9050309167977617777L;

    /**
     * Constructor
     */
    public RequestCollection(   
        ServiceHeader serviceHeader,
        Dataprovider_1_0 dataprovider
    ){
        this(serviceHeader, dataprovider, false);
    }

    /**
     * Constructor
     */
    public RequestCollection(   
        ServiceHeader serviceHeader,
        Dataprovider_1_0 dataprovider,
        boolean lenient
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
            this.lenient = lenient;
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    /**
     * Creates a new RequestCollection
     * 
     * @param header
     * 
     * @return a new request collection with the same provider and the specified header.
     **/
    public RequestCollection createRequestCollection(
        ServiceHeader header
    ) {
        return new RequestCollection(
            header,
            this.dataprovider,
            this.lenient
        );
    }

    /**
     * Creates a new RequestCollection
     * 
     * @param header
     * 
     * @return a new request collection with the same provider and the specified header.
     **/
    public RequestCollection createRequestCollection(
        String requestedFor,
        String requestedAt
    ) {
        return new RequestCollection(
            new ServiceHeader(
                this.serviceHeader.getPrincipalChain().toArray(
                    new String[this.serviceHeader.getPrincipalChain().size()]
                ),
                this.serviceHeader.getCorrelationId(),
                this.serviceHeader.traceRequest(),
                this.serviceHeader.getQualityOfService(),
                requestedAt,
                requestedFor
            ),
            this.dataprovider,
            this.lenient
        );
    }


    //------------------------------------------------------------------------
    // Implements DataproviderProcessor_1_0
    //------------------------------------------------------------------------

    /**
     * Clear the request and reply list.
     *
     * @return  the old reply list
     */
    public void clear(
    ){
        this.batchRequests.clear();
        this.batchListeners.clear();
        this.workingUnitRequests.clear();
        this.workingUnitListeners.clear();
        this.inBatch = false;
        this.inUnitOfWork = false;
        this.transactionalUnit = false;
    }

    /**
     * Calling beginBatch() postpones request processing until endBatch() is
     * called.
     *
     * @exception   ServiceException    ILLEGAL_STATE
     *              if the collection is already in batch or working unit mode
     */
    public void beginBatch(
    ) throws ServiceException {
        if(this.inBatch) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            "Request collection is already in batch mode"
        );
        this.inBatch = true;
    }

    /**
     * Calling endBatch() starts processing of all requests added since
     * beginBatch().
     *
     * @return      the working unit replies
     *
     * @exception   ServiceException    ILLEGAL_STATE
     *              if the collection is not in batch mode
     * @exception   RuntimeException    
     *              in case of system failure
     */
    public UnitOfWorkReply[] endBatch(
    ) throws ServiceException {
        if(! this.inBatch) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            "Request collection is not in batch mode"
        );
        UnitOfWorkRequest[] requests = batchRequests.toArray(
            new UnitOfWorkRequest[batchRequests.size()]
        );
        batchRequests.clear();
        DataproviderReplyListener[][] listeners = batchListeners.toArray(
            new DataproviderReplyListener[batchListeners.size()][]
        );
        batchListeners.clear(); 
        this.inBatch = false;
        return dispatch(
            requests,
            listeners
        );
    }

    /**
     * The requests embedded by beginUnitOfWork() and endUnitOfWork() are 
     * processed together.
     *
     * @param       transactionalUnit
     *              Defines whether the working unit is a transactional unit;
     *              false means that it is either a part of a bigger
     *              transactional unit or a non-transactional unit
     *
     * @exception   ServiceException    ILLEGAL_STATE
     *              if the collection is already in working unit mode
     */
    public void beginUnitOfWork(
        boolean transactionalUnit
    ) throws ServiceException {
        if(this.inUnitOfWork) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            "Request collection is already in working unit mode"
        );
        this.inUnitOfWork = true;
        this.transactionalUnit = transactionalUnit;
    }

    /**
     * The requests embedded by beginUnitOfWork() and endUnitOfWork() are 
     * processed together.
     *
     * @exception   ServiceException    ILLEGAL_STATE
     *              if the collection is not in working unit mode
     * @exception   RuntimeException    
     *              in case of system failure
     */
    public void endUnitOfWork(
    ) throws ServiceException {
        if(! this.inUnitOfWork) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            "Request collection is not in working unit mode"
        );
        DataproviderRequest[] requests = this.workingUnitRequests.toArray(
            new DataproviderRequest[this.workingUnitRequests.size()]
        );
        this.workingUnitRequests.clear();
        DataproviderReplyListener[] listeners = this.workingUnitListeners.toArray(
            new DataproviderReplyListener[this.workingUnitListeners.size()]
        );
        this.workingUnitListeners.clear();  
        dispatch(
            new UnitOfWorkRequest(
                this.transactionalUnit,
                requests
            ),
            listeners
        );
        this.inUnitOfWork = false;
        this.transactionalUnit = false;
    }


    //------------------------------------------------------------------------
    // Requests
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
    public DataproviderObject_1_0 addGetRequest(
        Path path
    ) throws ServiceException {
        return addGetRequest(
            path,
            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
            null
        );
    }

    /** 
     * Adds a get request retrieveing all attributes specified by either 
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
    public DataproviderObject_1_0 addGetRequest(
        Path path,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier
    ) throws ServiceException {
        RequestedObject target = new RequestedObject();
        addGetRequest(
            path,
            attributeSelector,
            attributeSpecifier,
            target
        );
        return target;
    }

    /** 
     * Adds a get request retrieveing all attributes specified by either 
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
        dispatch(
            new DataproviderRequest(
                new DataproviderObject(path),
                DataproviderOperations.OBJECT_RETRIEVAL,
                attributeSelector,
                attributeSpecifier
            ),
            listener
        );
    }


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
    public DataproviderObject_1_0 addCreateRequest(
        DataproviderObject object
    ) throws ServiceException {
        return addCreateRequest(
            object,
            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
            null
        );
    }

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
    public DataproviderObject_1_0 addCreateRequest(
        DataproviderObject object,
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
        return target;
    }

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
        DataproviderObject object,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier,
        DataproviderReplyListener listener
    ) throws ServiceException {
        dispatch(
            new DataproviderRequest(
                object,
                DataproviderOperations.OBJECT_CREATION,
                attributeSelector,
                attributeSpecifier
            ),
            listener
        );
    }


    /**
     * Adds a modify request retrieving the typical attributes.
     *
     * @param       object
     *              the object to be modified
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public DataproviderObject_1_0 addModifyRequest(
        DataproviderObject object
    ) throws ServiceException {
        return addModifyRequest(
            object,
            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
            null
        );
    }

    /** 
     * Adds a modify request retrieving all attributes specified by either 
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
    public DataproviderObject_1_0 addModifyRequest(
        DataproviderObject object,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier
    ) throws ServiceException {
        final RequestedObject target = new RequestedObject();
        addModifyRequest(
            object,
            attributeSelector,
            attributeSpecifier,
            target
        );
        return target;
    }

    /** 
     * Adds a modify request retrieving all attributes specified by either 
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
    public void addModifyRequest(
        DataproviderObject object,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier,
        DataproviderReplyListener listener
    ) throws ServiceException {
        dispatch(
            new DataproviderRequest(
                object,
                DataproviderOperations.OBJECT_MODIFICATION,
                attributeSelector,
                attributeSpecifier
            ),
            listener
        );
    }


    /**
     * Adds a replace request retrieving the typical attributes.
     *
     * @param       object
     *              thr object to be modified
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public DataproviderObject_1_0 addReplaceRequest(
        DataproviderObject object
    ) throws ServiceException {
        return addReplaceRequest(
            object,
            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
            null
        );
    }

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
    public DataproviderObject_1_0 addReplaceRequest(
        DataproviderObject object,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier
    ) throws ServiceException {
        final RequestedObject target = new RequestedObject();
        addReplaceRequest(
            object,
            attributeSelector,
            attributeSpecifier,
            target
        );
        return target;
    }

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
        DataproviderObject object,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier,
        DataproviderReplyListener listener
    ) throws ServiceException {
        dispatch(
            new DataproviderRequest(
                object,
                DataproviderOperations.OBJECT_REPLACEMENT,
                attributeSelector,
                attributeSpecifier
            ),
            listener
        );
    }


    /**
     * Adds a set request retrieving the typical attributes.
     *
     * @param       object
     *              the object to be created or replaced
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public DataproviderObject_1_0 addSetRequest(
        DataproviderObject object
    ) throws ServiceException {
        return addSetRequest(
            object,
            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
            null
        );
    }

    /** 
     * Adds a set request retrieveing all attributes specified by either 
     * the <code>attributeSelector</code> or the
     * <code>attributeSpecifier</code>.
     *
     * @param       object
     *              the object to be modified
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
    public DataproviderObject_1_0 addSetRequest(
        DataproviderObject object,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier
    ) throws ServiceException {
        final RequestedObject target = new RequestedObject();
        addSetRequest(
            object,
            attributeSelector,
            attributeSpecifier,
            target
        );
        return target;
    }

    /** 
     * Adds a set request retrieving all attributes specified by either 
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
    public void addSetRequest(
        DataproviderObject object,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier,
        DataproviderReplyListener listener
    ) throws ServiceException {
        dispatch(
            new DataproviderRequest(
                object,
                DataproviderOperations.OBJECT_SETTING,
                attributeSelector,
                attributeSpecifier
            ),
            listener
        );
    }


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
    public DataproviderObject_1_0 addRemoveRequest(
        Path path
    ) throws ServiceException {
        return addRemoveRequest(
            path,
            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
            null
        );
    }

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
    public DataproviderObject_1_0 addRemoveRequest(
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
        return target;
    }

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
        dispatch(
            new DataproviderRequest(
                new DataproviderObject(path),
                DataproviderOperations.OBJECT_REMOVAL,
                attributeSelector,
                attributeSpecifier
            ),
            listener
        );
    }


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
    @SuppressWarnings("unchecked")
    public List addFindRequest(
        Path referenceFilter,
        FilterProperty[] attributeFilter
    ) throws ServiceException {
        return addFindRequest(
            referenceFilter,
            attributeFilter,
            AttributeSelectors.NO_ATTRIBUTES,
            0,
            Integer.MAX_VALUE,
            Directions.ASCENDING
        );
    }

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
     *
     * @see org.openmdx.compatibility.base.dataprovider.cci.Directions
     */
    @SuppressWarnings("unchecked")
    public List addFindRequest(
        Path referenceFilter,
        FilterProperty[] attributeFilter,
        short attributeSelector,
        int position,
        int size,
        short direction
    ) throws ServiceException {
        return addFindRequest(
            referenceFilter,
            attributeFilter,
            attributeSelector,
            null, // atributeSpecifier
            position,
            size,
            direction
        );
    }

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
     *
     * @see org.openmdx.compatibility.base.dataprovider.cci.Directions
     */
    @SuppressWarnings("unchecked")
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
            (IterationProcessor)clone(),
            referenceFilter
        );
        addFindRequest(
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
     * @param       listener
     *              The dataprovider reply listener
     *
     * @obsolete
     *
     * @exception   ServiceException
     *              if no valid request can be added
     *
     * @see org.openmdx.compatibility.base.dataprovider.cci.Directions
     */
    public void addFindRequest(
        Path referenceFilter,
        FilterProperty[] attributeFilter,
        short attributeSelector,
        int position,
        int size,
        short direction,
        DataproviderReplyListener listener
    ) throws ServiceException {
        addFindRequest(
            referenceFilter,
            attributeFilter,
            attributeSelector,
            null, // atributeSpecifier
            position,
            size,
            direction,
            listener
        );
    }

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
     *              The dataprovider reply listener
     *
     * @exception   ServiceException
     *              if no valid request can be added
     *
     * @see org.openmdx.compatibility.base.dataprovider.cci.Directions
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
        dispatch(
            new DataproviderRequest(
                new DataproviderObject(referenceFilter),
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


    /**
     * Add a publish request
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public DataproviderObject_1_0 addPublishingRequest(
        String target,
        Path path,
        short scope,        
        short operation,
        long expiration
    ) throws ServiceException {
        final RequestedObject reply = new RequestedObject();
        addPublishingRequest(
            target,
            path,
            scope,
            operation,
            expiration,
            reply
        );
        return reply;
    }

    /**
     * Add a publish request
     *
     * @param       listener
     *              The dataprovider reply listener
     *
     * @exception   ServiceException
     *              if no valid request can be added
     */
    public void addPublishingRequest(
        String target,
        Path path,
        short scope,        
        short operation,
        long expiration,
        DataproviderReplyListener listener
    ) throws ServiceException {
        final DataproviderObject request = new DataproviderObject(path);
        request.values("scope").add(new Short(scope));
        request.values("operation").add(new Short(operation));
        request.values("expiration").add(
            DateFormat.getInstance().format(new Date(expiration))
        );
        dispatch(
            new DataproviderRequest(
                request,
                DataproviderOperations.OBJECT_MONITORING,
                AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                null
            ),
            listener
        );
    }


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
    public DataproviderObject_1_0 addOperationRequest(
        DataproviderObject request
    ) throws ServiceException {
        if(
                request.path().size() % 2 != 0
        ) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN, 
            BasicException.Code.BAD_PARAMETER,
            "The request's path must end in the requested operation's name",
            new BasicException.Parameter("path",request.path())
        );  

        // Append request id
        request.path().add(uuidAsString());

        final RequestedObject reply = new RequestedObject();
        dispatch(
            new DataproviderRequest(
                request,
                DataproviderOperations.OBJECT_OPERATION,
                AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                null
            ),
            reply
        );
        return reply;
    }

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
        DataproviderObject request,
        DataproviderReplyListener listener
    ) throws ServiceException {
        if(
                request.path().size() % 2 != 0
        ) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN, 
            BasicException.Code.BAD_PARAMETER,
            "The request's path must end in the requested operation's name",
            new BasicException.Parameter("path",request.path())
        );  

        // Append request id
        request.path().add(uuidAsString());

        dispatch(
            new DataproviderRequest(
                request,
                DataproviderOperations.OBJECT_OPERATION,
                AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                null
            ),
            listener
        );
    }


    //------------------------------------------------------------------------
    // Implements IterationProcessor
    //------------------------------------------------------------------------

    /**
     * Add an iteration request
     * 
     * @param referenceFilter
     * @param iterator
     * @param attributeSelector
     * @param position
     * @param size
     * @param direction
     * @param listener
     * 
     * @exception   ServiceException
     *              if no valid request can be added
     *
     * @see org.openmdx.compatibility.base.dataprovider.cci.Directions
     * 
     * @deprecated Use {@link #addIterationRequest(Path,byte[],short,AttributeSpecifier[],int,int,short,DataproviderReplyListener)} instead
     */
    public void addIterationRequest(
        Path referenceFilter,
        byte[] iterator,
        short attributeSelector,
        int position,
        int size,
        short direction,
        DataproviderReplyListener listener
    ) throws ServiceException {
        addIterationRequest(
            referenceFilter,
            iterator,
            attributeSelector,
            null, // attributeSpecifiers
            position,
            size,
            direction,
            listener);
    }

    /**
     * Add an iteration request
     * 
     * @param referenceFilter
     * @param iterator
     * @param attributeSelector
     * @param attributeSpecifiers
     * @param position
     * @param size
     * @param direction
     * @param listener
     * 
     * @exception   ServiceException
     *              if no valid request can be added
     *
     * @see org.openmdx.compatibility.base.dataprovider.cci.Directions
     */
    public void addIterationRequest(
        Path referenceFilter,
        byte[] iterator,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifiers,
        int position,
        int size,
        short direction, DataproviderReplyListener listener
    ) throws ServiceException {
        if(referenceFilter == null) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN, 
            BasicException.Code.BAD_PARAMETER,
            "Reference filter must not be null"
        );
        DataproviderRequest request = new DataproviderRequest (
            new DataproviderObject(referenceFilter),
            DataproviderOperations.ITERATION_CONTINUATION,
            null,
            position,
            size,
            direction,
            attributeSelector,
            attributeSpecifiers
        );
        request.context(DataproviderReplyContexts.ITERATOR).add(iterator);
        dispatch(
            request,
            listener
        );
    }


    //------------------------------------------------------------------------
    // Dispatching
    //------------------------------------------------------------------------

    /**
     *
     */
    protected void dispatch(
        DataproviderRequest request,
        DataproviderReplyListener listener
    ) throws ServiceException {
        if(this.lenient) {
            request.context(DataproviderRequestContexts.LENIENT).set(0, Boolean.TRUE);
        }
        if (this.inUnitOfWork){
            this.workingUnitRequests.add(request);
            this.workingUnitListeners.add(listener);
        } else {
            dispatch(
                new UnitOfWorkRequest(
                    false,
                    new DataproviderRequest[]{request}
                ),
                new DataproviderReplyListener[]{listener}
            );
        }
    }

    /**
     *
     */
    protected void dispatch(
        UnitOfWorkRequest request,
        DataproviderReplyListener[] listeners
    ) throws ServiceException {
        if(request.getRequests().length != listeners.length) {
            RuntimeServiceException assertionFailure = new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.ASSERTION_FAILURE,
                "The number of requests and listeners does not match"
            );
            SysLog.error(
                assertionFailure.getMessage(), 
                assertionFailure.getCause()
            );
            throw assertionFailure;
        }
        if (this.inBatch){
            this.batchRequests.add(request);
            this.batchListeners.add(listeners);
        } else {
            UnitOfWorkReply reply = dispatch(
                new UnitOfWorkRequest[]{request},
                new DataproviderReplyListener[][]{listeners}
            )[0];
            if (reply.failure()) throw reply.getStatus();
        }
    }

    protected UnitOfWorkReply[] dispatch(
        UnitOfWorkRequest[] requests,
        DataproviderReplyListener[][] listeners
    ){
        if(requests.length != listeners.length) {
            RuntimeServiceException assertionFailure = new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.ASSERTION_FAILURE,
                "The number of working unit requests and listeners does not match"
            );
            SysLog.error(
                assertionFailure.getMessage(), 
                assertionFailure.getCause()
            );
            throw assertionFailure;
        }
        UnitOfWorkReply[] replies = this.dataprovider.process(
            this.serviceHeader,
            requests
        );
        if(requests.length != listeners.length) {
            RuntimeServiceException assertionFailure = new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.ASSERTION_FAILURE,
                "The number of requests and replies does not match"
            );
            SysLog.error(
                assertionFailure.getMessage(), 
                assertionFailure.getCause()
            );
            throw assertionFailure;
        }
        for (
                int workingUnitIndex = 0;
                workingUnitIndex < requests.length;
                workingUnitIndex++
        ) try {
            UnitOfWorkReply source = replies[workingUnitIndex];
            DataproviderReplyListener[] target = listeners[workingUnitIndex];
            if (source.failure()) {
                for (
                        int requestIndex = 0;
                        requestIndex < target.length;
                        requestIndex++
                ) target[requestIndex].onException(
                    source.getStatus()
                );
            } else {
                for (
                        int requestIndex = 0;
                        requestIndex < target.length;
                        requestIndex++
                ) target[requestIndex].onReply(
                    source.getReplies()[requestIndex]
                );
            }
        } catch (Exception exception) {
            new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "A DataproviderReplyListener raised an exception",
                new BasicException.Parameter(
                    "workingUnit", 
                    workingUnitIndex
                )
            ).log();
        }
        return replies;     
    }

    /**
     * Retrieve the service headers's principal chain
     * 
     * @return the service headers's principal chain
     */
    public List<String> getPrincipalChain(){
        return this.serviceHeader.getPrincipalChain();
    }

    
    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------

    /**
     * Generates a new copy of this dataprovider object.
     * Subsequent changes to the path or the attributes of this
     * object will not affect the new copy, and vice versa.
     *
     * @return    a clone of this instance.
     */
    public Object clone(  
    ){
        return new RequestCollection(
            this.serviceHeader,
            this.dataprovider,
            this.lenient
        );
    }


    /**
     * 
     * @return
     */
    protected final String uuidAsString(
    ){
        if(this.uuidGenerator == null) this.uuidGenerator = UUIDs.getGenerator();
        return this.uuidGenerator.next().toString();
    }

    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------

    /**
     * Tells whether the <code>UnitOfWorkRequest</code>s <code>"lenient"</code>
     * should be set to <code>Boolean.TRUE</code>.
     */
    final protected boolean lenient;

    /**
     *
     */
    final protected Dataprovider_1_0 dataprovider;

    /**
     *
     */
    protected boolean inBatch = false;

    /**
     *
     */
    protected boolean inUnitOfWork = false;

    /**
     *
     */
    protected boolean transactionalUnit = false;

    /**
     *
     */
    final protected List<UnitOfWorkRequest> batchRequests = new ArrayList<UnitOfWorkRequest>();

    /**
     *
     */
    final protected List<DataproviderReplyListener[]> batchListeners = new ArrayList<DataproviderReplyListener[]>();

    /**
     *
     */
    final protected List<DataproviderRequest> workingUnitRequests = new ArrayList<DataproviderRequest>();

    /**
     *
     */
    final protected List<DataproviderReplyListener> workingUnitListeners = new ArrayList<DataproviderReplyListener>();

    /**
     *
     */
    final protected ServiceHeader serviceHeader;

    /**
     * 
     */
    private transient UUIDGenerator uuidGenerator = null;

}
