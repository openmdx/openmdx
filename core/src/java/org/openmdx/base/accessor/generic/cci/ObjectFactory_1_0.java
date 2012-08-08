/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ObjectFactory_1_0.java,v 1.12 2008/09/11 10:42:06 hburger Exp $
 * Description: openMDX Object Layer: Object Factory Interface
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/11 10:42:06 $
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

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.transaction.UnitOfWork_1_0;
import org.openmdx.compatibility.base.marshalling.Marshaller;

/**
 * openMDX Object Layer: Object Factory Interface.
 * <p>
 * The object factory returns the same object for a given object id as long
 * as it is not garbage collected.
 */
public interface ObjectFactory_1_0 
extends StructureFactory_1_0, Marshaller
{

    //------------------------------------------------------------------------
    // Life cycle management
    //------------------------------------------------------------------------

    /**
     * Close the object factory.
     * <p>
     * After the close method completes, all methods on the
     * <code>ObjectFactory_1_0</code> instance except <code>isClosed()</code>
     * throw an <code>ILLEGAL_STATE ServiceException</code>.
     */
    void close(
    ) throws ServiceException;

    /**
     * Tells whether the object factory has been closed.
     * 
     * @return <code>true</code> if the object factory has been closed
     */
    boolean isClosed();


    //------------------------------------------------------------------------
    // Unit Of Work management
    //------------------------------------------------------------------------

    /**
     * Return the unit of work associated with the current object factory.
     *
     * @return  the unit of work
     * 
     * @exception ServiceException ILLEGAL_STATE if the object factory is 
     * closed
     */
    UnitOfWork_1_0 getUnitOfWork(
    ) throws ServiceException;


    //------------------------------------------------------------------------
    // Object management
    //------------------------------------------------------------------------

    /**
     * Get an object from the object factory.
     * <p>
     * If an object with the given access path is already in the cache it is
     * returned, otherwise a new object is returned.
     *
     * @param       accessPath
     *              Access path of object to be retrieved.
     *
     * @return      A persistent object
     *
     * @exception   ServiceException    ILLEGAL_STATE
     *              if the object factory is closed
     */
    Object_1_0 getObject(
        Object accessPath
    ) throws ServiceException;

    /**
     * Create an object
     *
     * @param       objectClass
     *              The model class of the object to be created
     *
     * @return      a new object instance
     *
     * @exception   ServiceException    ILLEGAL_STATE
     *              if the object factory is closed
     * @exception   ServiceException    BAD_PARAMETER
     *              if the objectClass is an Aspect sub-class             
     */
    Object_1_0 createObject(
        String objectClass
    ) throws ServiceException;

    /**
     * This method is deprecated and will throw a NOT_SUPPORTED exception
     * 
     * @deprecated
     * 
     * @exception   ServiceException    NOT_SUPPORTED
     */
    Object_1_0 createObject(
      String objectClass,
      Object_1_0 initialValues
    ) throws ServiceException;

}
