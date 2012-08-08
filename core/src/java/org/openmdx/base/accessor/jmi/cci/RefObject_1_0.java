/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RefObject_1_0.java,v 1.9 2008/02/08 16:51:25 hburger Exp $
 * Description: RefObject_1_0 interface
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/08 16:51:25 $
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
package org.openmdx.base.accessor.jmi.cci;

import java.util.EventListener;
import java.util.Set;

import javax.jmi.reflect.RefObject;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.naming.Path;

/**
 * This interface extends the javax.jmi.reflect.RefObject interface 
 * by additional helpers and openMDX-specific methods. These
 * methods must not be used by 100% JMI-compliant applications.
 */
public interface RefObject_1_0
  extends RefObject {

  //-------------------------------------------------------------------------
  // Object_1_0 like accessors
  // Most of this methods are delegated to refDelegate() but offer JMI-like
  // signatures.
  //-------------------------------------------------------------------------
  /**
   * Return the object which RefObject_1_0 delegates to. The returned object
   * is managed by a basic accessor.
   * 
   * @return Object_1_0 delegate object.
   */
  public Object_1_0 refDelegate(
  );

  /**
   * Returns the object's access path.
   *
   * @return  the object's access path;
   *           or null for transient objects
   */
  public Path refGetPath(
  );

  /**
   * Puts an object in read-only mode.
   * <p>
   * This method is idempotent.
   *
   * @exception  JmiServiceException  ILLEGAL_STATE
   *              if the object is dirty
   */
  public void refWriteProtect(
  );

  /**
   * After this call the object observes unit of work boundaries.
   * <p>
   * This method is idempotent.
   *
   * @exception  JmiServiceException ILLEGAL_STATE
   *              if the object is locked 
   * @exception  JmiServiceException 
   *              if the object can't be added to the unit of work for
   *              another reason.
   */
  public void refAddToUnitOfWork(
  );
   
  /**
   * After this call the object ignores unit of work boundaries.
   * <p>
   * This method is idempotent.
   *
   * @exception  JmiServiceException ILLEGAL_STATE 
   *              if the object is dirty.
   * @exception  JmiServiceException if the object can't be removed from 
   *              its unit of work for another reason 
   */
  public void refRemoveFromUnitOfWork(
  );

  /**
   * Tells whether this object is in read-only mode.
   *
   * @return  true if the object is write protected, 
   *           false if the object is write enabled.
   */
  public boolean refIsWriteProtected(
  ); 

  /**
   * Tests whether this object is dirty. Instances that have been modified,
   * deleted, or newly made persistent in the current unit of work return
   * true.
   * <p>
   * Transient instances return false. 
   * 
   * @return true if this instance has been modified in the current unit
   *          of work.
   */ 
  public boolean refIsDirty(
  );

  /**
   * Tests whether this object is persistent. Instances that represent
   * persistent objects in the data store return true. 
   * 
   * @return true if this instance is persistent.
   */
  public boolean refIsPersistent(
  );

  /**
   * Tests whether this object has been newly made persistent. Instances
   * that have been made persistent in the current unit of work return true. 
   * <p>
   * Transient instances return false. 
   *
   * @return  true if this instance was made persistent in the current unit
   *           of work. 
   */
  public boolean refIsNew(
  );

  /**
   * Tests whether this object has been deleted. Instances that have been
   * deleted in the current unit of work return true. 
   * Transient instances return false. 
   *
   * @return  true if this instance was deleted in the current unit of work.
   */
  public boolean refIsDeleted(
  );

  /**
   * Refresh the state of the instance from its provider.
   *
   * @exception  JmiServiceException 
   *              if the object can't be synchronized
   */
  public void refRefresh(
  );

  /**
   * Flush the state of the instance to its provider. 
   *
   * @exception  JmiServiceException 
   *              if the object can't be flushed.
   */
  public void refFlush(
  );

  /**
   * Refresh the state of the instance from its provider asynchrounously.
   *
   * @exception  JmiServiceException 
   *              if the object can't be synchronized asynchronously
   */
  public void refRefreshAsynchronously(
  );

  /**
   * Returns the refDelegate().objDefaultFetchGroup() plus the set of all
   * non-derived attributes of the object.
   *
   */
  public Set<String> refDefaultFetchGroup(
  );

  /**
   * Returns application-defined context.
   * 
   * @return Object application-defined context.
   */
  public Object refContext(
  );

  /**
   * Initializes the object based on the source object. The source object
   * must be of the same class or a subtype of the target.
   * 
   * @param existing existing object.
   * @throws JmiServiceException thrown if object can not be initialized. 
   */
  public void refInitialize(
    RefObject source
  );

  /**
   * Initializes the object as follows:
   * <ul>
   *   <li>collections are cleared.</li>
   *   <li>primitive required attributes are set to default values: 
   *       string = ""; number = 0; date = min date. They are set to null if setRequiredToNull==true.</li>
   *   <li>primitive optional attributes are set to null if initializeOptional == true
   *       and are left untouched if initializeOptional == false.</li>
   *   <li>required references can not be initialized and an exception is thrown.</li>
   * </ul>
   * 
   * @param setRequiredToNull if true, required attributes are set to null. Otherwise
   *         they are initialized with a default value.
   * @param setOptionalToNull if true, optional features are set to true, otherwise
   *         they are left untouched.
   * @throws JmiServiceException thrown if object can not be initialized. 
   */
  public void refInitialize(
    boolean setRequiredToNull,
    boolean setOptionalToNull
  );

  /**
   * Returns the value of feature identified by qualifier. marshal defines whether
   * marshalling is applied to the returned object. If false a RefObject which is in 
   * fact of type 'org:openmdx:test:app1:Partner' is returned as type RefObject 
   * instead of org.openmdx.test.app1.Partner. This method method can improve 
   * performance in cases where no marshalled objects are required.
   *   
   * @param feature feature to be retrieved.
   * @param qualifier identifies object to be retrieved.
   * @param marshal if true the returned value is marshalled.
   * @throws JmiServiceException
   */
  public Object refGetValue(
    RefObject feature,
    Object qualifier,
    boolean marshal
  );

  /**
   * Returns the value of feature. Instead of returning the value as return
   * value it is streamed to value. The value must either be a binary or 
   * character output stream.
   *   
   * @param feature feature to be retrieved.
   * @param value binary or character output stream.
   * @param position stream is returned starting from position.
   * @return length of the stream.
   * @throws JmiServiceException
   */
  public long refGetValue(
    String feature,
    Object value,
    long position
  );

  /**
   * Sets the value of feature. The value must be a binary or character
   * input stream of the specified length. The parameter length results 
   * in better performance for stream handling.
   * 
   * @param feature feature to be retrieved.
   * @param newValue binary or character input stream.
   * @param length length of the stream.
   * @throws JmiServiceException
   */
  public void refSetValue(
    String feature,
    Object newValue,
    long length
  );

  /**
   * Adds the value to the feature with the specified qualifier.
   *
   * @param featureName feature to add value.
   * @param qualifier qualifier of value to add.
   * @param value value to add.
   */
  public void refAddValue(
    String featureName,
    Object qualifier,
    Object value
  );

  /**
   * Removes qualified value from feature.
   * 
   * @param featureName qualified value is removed from feature.
   * @param qualifier value qualifier.
   */
  public void refRemoveValue(
    String featureName,
    Object qualifier
  );

  /**
   * Removes value from feature.
   * 
   * @param featureName value is removed from feature.
   * @param value value to be removed.
   */
  public void refRemoveValue(
    String featureName,
    RefObject value
  );

  //-------------------------------------------------------------------------
  // Event Handling
  //-------------------------------------------------------------------------

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
   * @exception ServiceException BAD_MEMBER_NAME
   *              if the object has no such feature or if a non-null
   *              feature name is specified for an instance level event
   * @exception ServiceException NOT_SUPPORTED
   *              if the listener's class is not supported
   * @exception   ServiceException TOO_MANY_EVENT_LISTENERS
   *              if an attempt is made to register more than one 
   *              listener for a unicast event.
   * @exception ServiceException BAD_PARAMETER
   *              If the listener is null 
   */
  void refAddEventListener(
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
   * @exception ServiceException BAD_MEMBER_NAME
   *              if the object has no such feature or if a non-null
   *              feature name is specified for an instance level event
   * @exception ServiceException NOT_SUPPORTED
   *              if the listener's class is not supported
   * @exception ServiceException BAD_PARAMETER
   *              If the listener is null 
   */
  void refRemoveEventListener(
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
   * @exception ServiceException BAD_MEMBER_NAME
   *              if the object has no such feature or if a non-null
   *              feature name is specified for an instance level event
   * @exception ServiceException BAD_PARAMETER
   *              If the listener's type is not a subtype of EventListener 
   * @exception ServiceException NOT_SUPPORTED
   *              if the listener type is not supported
   */
  EventListener[] refGetEventListeners(
      String feature,
      Class<? extends EventListener> listenerType
  ) throws ServiceException;

}

//--- End of File -----------------------------------------------------------
