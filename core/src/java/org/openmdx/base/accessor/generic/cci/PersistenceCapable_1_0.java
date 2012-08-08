/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PersistenceCapable_1_0.java,v 1.2 2008/09/10 08:55:28 hburger Exp $
 * Description:The Persistence Capable Interface 1.0
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:28 $
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

import java.util.Set;

import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.naming.Path;

/**
 * The Persistence Capable Interface 1.0
 */
public interface PersistenceCapable_1_0 extends Featured_1_0 {

    /**
     * Returns the object's access path.
     *
     * @return  the object's access path;
     *          or null for transient objects
     */
    Path objGetPath(
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


    //------------------------------------------------------------------------
    // Life Cycle Operations
    //------------------------------------------------------------------------

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

}

