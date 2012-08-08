/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ClassMapping_1_0.java,v 1.1 2010/04/16 18:24:20 hburger Exp $
 * Description: Mapping_1_0 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/16 18:24:20 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.RefObject;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.exception.ServiceException;
import org.w3c.jpa3.AbstractObject;


/**
 * JMI 1 Mapping 1.0
 */
interface ClassMapping_1_0 {

    /**
     * Tells whether the interface declaring a method is a mixed-in interface
     * 
     * @param declaringClass
     * 
     * @return <code> true</code> the interface is a mixed-in interface
     * @throws ServiceException
     */
    boolean isMixedInInterfaces(
        Class<?> declaringClass
    );
    
    /**
     * Retrieve the aspect implementation classes
     * 
     * @return the aspect implementation classes
     */
    AspectImplementationDescriptor[] getAspectImplementationDescriptors(
    );

    /**
     * Retrieve an invocation descriptor
     * 
     * @param method
     * @return the invocation descriptor; or <code>null/code>
     */
    InvocationDescriptor getInvocationDescriptor(
        Method method
    );
    
    /**
     * Retrieve the instance interface
     * 
     * @return the instance interface
     */
    Class<? extends RefObject> getInstanceInterface();

    /**
     * Retrieve the JPA class
     * 
     * @return the JPA class
     * 
     * @throws ServiceException
     */
    Class<? extends AbstractObject> getInstanceClass(
    ) throws ServiceException;
        
    /**
     * Create a class proxy
     * 
     * @param refPackage
     * 
     * @return a new instance proxy
     * 
     * @throws ServiceException
     */
    Jmi1Class_1_0 newClass(
        Jmi1Package_1_0 refPackage
    ) throws ServiceException;

    /**
     * Create an instance proxy
     * 
     * @param refClass
     * @param delegate
     * 
     * @return a new insatnce proxy
     * 
     * @throws ServiceException
     */
    RefObject_1_0 newInstance(
        Jmi1Class_1_0 refClass,
        PersistenceCapable delegate
    );

    /**
     * Create a query proxy
     * 
     * @param invocationHandler
     * 
     * @return a new query proxy
     * 
     * @throws ServiceException
     */
    RefQuery_1_0 newQuery(
        InvocationHandler invocationHandler
    );

}
