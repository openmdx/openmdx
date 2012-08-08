/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Featured_1_0.java,v 1.1 2008/05/27 16:52:29 hburger Exp $
 * Description: Featured Interface 1.0
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/27 16:52:29 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.generic.cci;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.ServiceException;

/**
 * Featured Interface 1.0
 */
public interface Featured_1_0 {

    //------------------------------------------------------------------------
    // Values
    //------------------------------------------------------------------------

    /**
     * Set an attribute's value.
     * <p>
     * This method returns a <code>BAD_PARAMETER</code> exception unless the 
     * feature is single valued or a stream. 
     *
     * @param       feature
     *              the attribute's name
     * @param       to
     *              the object.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is write protected 
     * @exception   ServiceException BAD_PARAMETER
     *              if the feature is multi-valued
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     * @exception   ServiceException 
     *              if the object is not accessible
     */
    void objSetValue(
        String feature,
        Object to
    ) throws ServiceException;

    /**
     * Get a single-valued attribute.
     * <p>
     * This method returns a <code>BAD_PARAMETER</code> exception unless the 
     * feature is single valued or a stream. 
     *
     * @param       feature
     *              the feature's name
     *
     * @return      the object representing the feature;
     *              or null if the feature's value hasn't been set yet.
     *
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     * @exception   ServiceException BAD_PARAMETER
     *              if the feature is multi-valued
     * @exception   ServiceException 
     *              if the object is not accessible
     */
    Object objGetValue(
        String feature
    ) throws ServiceException;

    /**
     * Get a List attribute.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     * @exception   ClassCastException
     *              if the feature's value is not a list
     */
    List<Object> objGetList(
        String feature
    ) throws ServiceException;
    
    /**
     * Get a Set attribute.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     * @exception   ClassCastException
     *              if the feature's value is not a set
     */
    Set<Object> objGetSet(
        String feature
    ) throws ServiceException;

    /**
     * Get a SparseArray attribute.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature's value is not a sparse array
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     */
    SortedMap<Integer,Object> objGetSparseArray(
        String feature
    ) throws ServiceException;
    
    /**
     * Get a large object feature
     * <p> 
     * This method returns a new LargeObject.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a large object which may be empty but never is null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature's value is not a large object
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     */
    LargeObject_1_0 objGetLargeObject(
        String feature
    ) throws ServiceException;

    /**
     * Get a reference feature.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature is not a reference
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature
     */
    FilterableMap<String, Object_1_0> objGetContainer(
        String feature
    ) throws ServiceException;


    //------------------------------------------------------------------------
    // Operations
    //------------------------------------------------------------------------

    /**
     * Invokes an operation synchronously.
     * <p>
     * Only query operations can be invoked synchronously unless the unit of
     * work is non-optimistic or committing. Such queries use the object states
     * at the beginning of the unit of work!
     *
     * @param       operation
     *              The operation name
     * @param       arguments
     *              The operation's arguments
     *
     * @return      the operation's return values
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if a non-query operation is called in an inappropriate
     *              state of the unit of work.
     * @exception   ServiceException NOT_SUPPORTED
     *              if synchronous calls are not supported by the basic accessor
     *              or if the requested operation is not supported by object
     *              instance.
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the requested operation is not a feature of the object.
     * @exception   ServiceException 
     *              if a checked exception is thrown by the implementation or
     *              the invocation fails for another reason.
     */
    Structure_1_0 objInvokeOperation(
        String operation,
        Structure_1_0 arguments
    ) throws ServiceException;

    /**
     * Invokes an operation asynchronously.
     * <p>
     * Such asynchronous operations will be invoked at the very end of an 
     * optimistic unit of work, i.e. after all modifications at object and
     * attribute level.
     *
     * @param       operation
     *              The operation name
     * @param       arguments
     *              The operation's arguments
     *
     * @return      a structure with the result's values if the accessor is
     *              going to populate it after the unit of work has committed
     *              or null if the operation's return value(s) will never be
     *              available to the accessor.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if no unit of work is in progress
     * @exception   ServiceException NOT_SUPPORTED
     *              if synchronous calls are not supported by the basic
     *              accessor.
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the requested operation is not a feature of the object.
     * @exception   ServiceException 
     *              if the invocation fails for another reason
     */
    Structure_1_0 objInvokeOperationInUnitOfWork(
        String operation,
        Structure_1_0 arguments
    ) throws ServiceException;

}

