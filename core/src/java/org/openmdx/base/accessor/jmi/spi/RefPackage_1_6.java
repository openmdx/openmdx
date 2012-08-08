/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefPackage_1_6.java,v 1.3 2008/12/15 03:15:33 hburger Exp $
 * Description: RefPackage_1_5 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:33 $
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
package org.openmdx.base.accessor.jmi.spi;


import java.util.Set;

import javax.jmi.reflect.RefObject;

import org.openmdx.base.accessor.jmi.cci.RefPackage_1_5;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.marshalling.CachingMarshaller_1_0;
import org.openmdx.compatibility.base.naming.Path;

/**
 * RefPackage 1.6
 */
public interface RefPackage_1_6 extends RefPackage_1_5, CachingMarshaller_1_0 {

    /**
     * Retrieve a class' mix-in interfaces
     * 
     * @param qualifiedClassName
     * @return the class' mix-in interfaces
     */
    Set<Class<?>> getMixedInInterfaces(
        String qualifiedClassName
    ) throws ServiceException;
    
    /**
     * Get name of a class implementing the given interface
     *  
     * @param qualifiedClassName
     * @param declaringClass
     * 
     * @return the name a class implementing the given interface; or 
     * <code>null</code> if neither the implementation for the most nor for 
     * the least derived class implements it 
     */
    String getClassImplementingInterface (
        String qualifiedClassName, 
        Class<?> declaringClass
    ) throws ServiceException;
    
    /**
     * Tells whether the refPackage belongs to an accessor
     * 
     * @return <code>true</code> if the refPackage belongs to an accessor
     */
    boolean isAccessor();
    
    /**
     * Get object with the given object id. This operation is equivalent to
     * <pre>
     *   Object_1_0 object = refPackage.refObject(objectId.toResourceIdentifier());
     * </pre>
     * 
     * @param refMofId unique id of RefObject.
     * 
     * @return RefObject
     */
    RefObject refObject(
      Path objectId
    );
    
}
