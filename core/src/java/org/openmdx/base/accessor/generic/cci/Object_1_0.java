/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Object_1_0.java,v 1.5 2008/02/08 16:50:58 hburger Exp $
 * Description: SPICE Basic Accessor Object interface
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/08 16:50:58 $
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
package org.openmdx.base.accessor.generic.cci;

import java.util.EventListener;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.naming.Path;

/**
 * The Object_1_0 interface.
 */
public interface Object_1_0 {

    /**
     * Returns the object's access path.
     *
     * @return  the object's access path;
     *          or null for transient objects
     */
    Path objGetPath(
    ) throws ServiceException;

    /**
     * Returns the object's resource identifier
     *
     * @return  the object's access path;
     *          or null for transient or new objects
     */
    Object objGetResourceIdentifier(
    );

    /**
     * Returns a new set containing the names of the features in the default
     * fetch group.
     * <p>
     * The returned set is a copy of the original set, i.e. interceptors are
     * free to modify it before passing it on.
     *
     * @return  the names of the features in the default fetch group
     *
     * @exception   ServiceException  
     *              if the information is unavailable
     */
    Set<String> objDefaultFetchGroup(
    ) throws ServiceException;

    /**
     * Returns the object's model class.
     *
     * @return  the object's model class
     *
     * @exception   ServiceException  
     *              if the information is unavailable
     */
    String objGetClass(
    ) throws ServiceException;


    //------------------------------------------------------------------------
    // State Management
    //------------------------------------------------------------------------

    /**
     * After this call the object observes unit of work boundaries.
     * <p>
     * This method is idempotent.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is locked 
     * @exception   ServiceException 
     *              if the object can't be added to the unit of work for
     *        another reason.
     */
    void objAddToUnitOfWork(
    ) throws ServiceException;
     
    /**
     * After this call the object ignores unit of work boundaries.
     * <p>
     * This method is idempotent.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is dirty.
     * @exception   ServiceException 
     *              if the object can't be removed from its unit of work for
     *        another reason 
     */
    void objRemoveFromUnitOfWork(
    ) throws ServiceException;


    //------------------------------------------------------------------------
    // State Queries
    //------------------------------------------------------------------------

    /**
     * Tests whether this object is dirty. Instances that have been modified,
     * deleted, or newly made persistent in the current unit of work return
     * true.
     * <p>
     * Transient instances return false. 
     * 
     * @return true if this instance has been modified in the current unit
     *         of work.
     */ 
    boolean objIsDirty(
  ) throws ServiceException;

    /**
     * Tests whether this object is persistent. Instances that represent
     * persistent objects in the data store return true. 
     * 
     * @return true if this instance is persistent.
     */
    boolean objIsPersistent(
    ) throws ServiceException;

    /**
     * Tests whether this object has been newly made persistent. Instances
     * that have been made persistent in the current unit of work return true. 
     * <p>
     * Transient instances return false. 
     *
     * @return  true if this instance was made persistent in the current unit
     *          of work. 
     */
    boolean objIsNew(
    ) throws ServiceException;

    /**
     * Tests whether this object has been deleted. Instances that have been
     * deleted in the current unit of work return true. 
     * Transient instances return false. 
     *
     * @return  true if this instance was deleted in the current unit of work.
     */
    boolean objIsDeleted(
    ) throws ServiceException;

    /**
     * Tests whether this object belongs to the current unit of work.
     *
     * @return  true if this instance belongs to the current unit of work.
     */
    boolean objIsInUnitOfWork(
    ) throws ServiceException;


    //------------------------------------------------------------------------
    // Synchronization
    //------------------------------------------------------------------------

    /**
     * Refresh the state of the instance from its provider.
     *
     * @exception   ServiceException 
     *              if the object can't be synchronized
     */
    void objRefresh(
    ) throws ServiceException;

    /**
     * Flush the state of the instance to its provider.
     * 
     * @return      true if all attributes could be flushed,
     *              false if some attributes contained placeholders
     *
     * @exception   ServiceException NOT_SUPPORTED
     *              if the unit of work is optimistic
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is not persistent
     * @exception   ServiceException 
     *              if the object can't be synchronized
     */
    boolean objFlush(
    ) throws ServiceException;

    /**
     * Mark an object as volatile, i.e POST_RELOAD InstanceCallbackEvents
     * may be fired. 
     *
     * @exception   ServiceException 
     *              if the object can't be made volatile.
     */
    void objMakeVolatile(
    ) throws ServiceException;


    //------------------------------------------------------------------------
    // Life Cycle Operations
    //------------------------------------------------------------------------

    /**
     * The copy operation makes a copy of the object. The copy is located in the
     * scope of the container passed as the first parameter and includes the
     * object's default fetch set.
     *
     * @return    an object initialized from the existing object.
     * 
     * @param     there
     *            the new object's container or <code>null</code>, in which case
     *            the object will not belong to any container until it is moved
     *            to a container.
     * @param     criteria
     *            The criteria is used to add the object to the container or 
     *            <code>null</null>, in which case it is up to the
     *            implementation to define the criteria.
     *
     * @exception ServiceException
     *            if the copy operation fails.
     */
    Object_1_0 objCopy(
        FilterableMap<String, Object_1_0> there,
        String criteria
    ) throws ServiceException;
    
    /**
     * The move operation moves the object to the scope of the container passed
     * as the first parameter. The object remains valid after move has
     * successfully executed.
     *
     * @param     there
     *            the object's new container.
     * @param     criteria
     *            The criteria is used to move the object to the container or 
     *            <code>null</null>, in which case it is up to the
     *            implementation to define the criteria.
     *
     * @exception ServiceException  ILLEGAL_STATE
     *            if the object is persistent.
     * @exception ServiceException BAD_PARAMETER
     *            if <code>there</code> is <code>null</code>.
     * @exception ServiceException  
     *            if the move operation fails.
     */
    void objMove(
        FilterableMap<String, Object_1_0> there,
        String criteria
    ) throws ServiceException;
    
    /**
     * Removes an object. 
     * <p>
     * Neither <code>getValue()</code> nor <code>setValue()</code>
     * calls are allowed after an <code>remove()</code> invocation and
     * <code>isDeleted()</code> will return <code>true</code> unless the
     * object has been transient.
     *
     * @exception   ServiceException NOT_SUPPORTED
     *              If the object refuses to be removed.
     * @exception   ServiceException 
     *              if the object can't be removed
     */
    void objRemove(
    ) throws ServiceException;


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


    //------------------------------------------------------------------------
    // Event Handling
    //------------------------------------------------------------------------

    /**
     * Add an event listener.
     * 
     * @param feature
     *        restrict the listener to this feature;
     *        or null if the listener is interested in all features
     * @param listener
     *        the event listener to be added
     * <p>
     * It is implementation dependent whether the feature name is verified or 
     * not.
     * 
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature or if a non-null
     *              feature name is specified for an instance level event
     * @exception   ServiceException NOT_SUPPORTED
     *              if the listener's class is not supported
     * @exception   ServiceException TOO_MANY_EVENT_LISTENERS
     *              if an attempt is made to register more than one 
     *              listener for a unicast event.
     * @exception   ServiceException BAD_PARAMETER
     *              If the listener is null 
     */
    void objAddEventListener(
        String feature,
        EventListener listener
    ) throws ServiceException;

    /**
     * Remove an event listener.
     * <p>
     * It is implementation dependent whether feature name and listener
     * class are verified. 
     * 
     * @param feature
     *        the name of the feature that was listened on,
     *        or null if the listener is interested in all features
     * @param listener
     *        the event listener to be removed
     * 
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature or if a non-null
     *              feature name is specified for an instance level event
     * @exception   ServiceException NOT_SUPPORTED
     *              if the listener's class is not supported
     * @exception   ServiceException BAD_PARAMETER
     *              If the listener is null 
     */
    void objRemoveEventListener(
        String feature,
        EventListener listener
    ) throws ServiceException;

    /**
     * Get event listeners.
     * <p>
     * The <code>feature</code> argument is ignored for listeners registered 
     * with a <code>null</code> feature argument.
     * <p>
     * It is implementation dependent whether feature name and listener
     * type are verified. 
     * 
     * @param feature
     *        the name of the feature that was listened on,
     *        or null for listeners interested in all features
     * @param listenerType
     *        the type of the event listeners to be returned
     * 
     * @return an array of listenerType containing the matching event
     *         listeners
     * 
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the object has no such feature or if a non-null
     *              feature name is specified for an instance level event
     * @exception   ServiceException BAD_PARAMETER
     *              If the listener's type is not a subtype of EventListener 
     * @exception   ServiceException NOT_SUPPORTED
     *              if the listener type is not supported
     */
    EventListener[] objGetEventListeners(
        String feature,
        Class<? extends EventListener> listenerType
    ) throws ServiceException;

    /**
     * Register a synchronization object for upward delegation.
     *
     * @param   synchronization
     *          The synchronization object to be registered
     *
     * @exception ServiceException TOO_MANY_EVENT_LISTENERS
     *            if an attempt is made to register more than one 
     *            synchronization object.
     * 
     * @deprecated  use addEventListener(String,EventListener) instead
     */
    void objRegisterSynchronization(
    org.openmdx.compatibility.base.accessor.object.cci.InstanceCallbacks_1_0 synchronization
    ) throws ServiceException;
    
}

