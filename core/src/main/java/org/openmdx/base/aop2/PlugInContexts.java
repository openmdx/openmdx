/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Plug-In Contexts
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2011, OMEX AG, Switzerland
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
package org.openmdx.base.aop2;

import org.openmdx.base.accessor.jmi.spi.Jmi1ObjectInvocationHandler;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;


/**
 * Plug-In Contexts
 */
public class PlugInContexts {

    /**
     * Constructor 
     */
    private PlugInContexts() {
        // Avoid instantiation
    }

    /**
     * Retrieve a plug-in's context object
     * 
     * @param object the object
     * @param plugInClass the plug-in's class
     * @return the requested context object, or <code>null</code> if the plugInClass is not applicable to the object
     * @throws RuntimeServiceException  
     * 
     * @exception ServiceException
     */
    public static <C> C getPlugInContext(
        Object object,
        Class<? extends AbstractObject<?, ?, ? extends C>> plugInClass
    ){
        try {
            return Jmi1ObjectInvocationHandler.getAspectImplementationInstance(object, plugInClass).thisContext();
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }        
    
    /**
     * Retrieve a plug-in's context object
     * 
     * @param object the object
     * @param plugInClass the plug-in's class
     * @return the requested context object, or <code>null</code> if the plugInClass is not applicable to the object
     * @throws RuntimeServiceException  
     * 
     * @exception ServiceException
     */
    @SuppressWarnings("unchecked")
    public static <C> C uncheckedGetPlugInContext(
        Object object,
        Class<?> plugInClass
    ){
        try {
            return ((AbstractObject<?, ?, C>)Jmi1ObjectInvocationHandler.getAspectImplementationInstance(object, plugInClass)).thisContext();
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }        

}
