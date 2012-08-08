/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ObjectFactory_1_1.java,v 1.3 2006/05/12 20:08:52 hburger Exp $
 * Description: SPICE Object Layer: Object Factory Interface
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/05/12 20:08:52 $
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
package org.openmdx.base.accessor.generic.cci;

import org.openmdx.base.exception.ServiceException;

/**
 * SPICE Object Layer: Object Factory Interface.
 * <p>
 * The object factory returns the same object for a given object id as long
 * as it is not garbage collected.
 * 
 * @since 1.3
 */
public interface ObjectFactory_1_1 
    extends ObjectFactory_1_0
{

    /**
     * This method creates a new object with the initial values.
     * <p>
     * This method method and its org::openmdx::compatibility::role1 model
     * are deprecated in favour of the org::openmdx::base::RoleCapable class
     * and the $link{ObjectFactory_1_1#createObject(java.lang.String,
     * java.lang.String,Object_1_0) createObject(String,String,Object_1_0)}
     * method.
     * 
     * @see org.openmdx.compatibility.role1.cci.Role
     */
    Object_1_0 createObject(
      String objectClass,
      Object_1_0 initialValues
    ) throws ServiceException;

    /**
     * This method creates a new role of a RoleCapable object.
     * 
     * @see org.openmdx.base.cci.RoleCapable
     */
    Object_1_0 createObject(
      String roleClass,
      String roleId,
      Object_1_0 roleCapable
    ) throws ServiceException;

}
