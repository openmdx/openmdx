/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Aspect Oriented View Manager Plug-In
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.base.aop1;

import javax.resource.cci.InteractionSpec;

import org.openmdx.base.accessor.rest.DataObject_1;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.ServiceException;

/**
 * Aspect Oriented View Plug-In
 */
public interface PlugIn_1_0 {

    /**
     * A plug-in may either return its own interceptor or just return the
     * given one.
     * 
     * @param view the view delegating to the interceptors
     * @param next the next interceptor
     * 
     * @return the given or a new interceptor
     * 
     * @exception ServiceExcetion if the required plug-in can't be determined 
     * or provided
     */
    Interceptor_1 getInterceptor(
        ObjectView_1_0 view,
        Interceptor_1 next
    ) throws ServiceException;
    
    /**
     * Propgate the value before the view's initialization if necessary
     * 
     * @param object
     * @param feature
     * @param value 
     * 
     * @return {@code true} if the value has been propagated eagerly
     */
    boolean propagatedEagerly(
        DataObject_1 object, 
        String feature, 
        Object value
	) throws ServiceException;

    /**
     * Resolve an object class 
     *  
     * @param       objectClass
     *              The model class of the object to be created
     * @param       interactionSpec
     *              the interaction specification   
     *              
     * @return the object class or a replacement, if necessary
     *  
     * @throws ServiceException if the object class is not acceptable for the 
     * given interaction specification
     */
    String resolveObjectClass(
        String objectClass, 
        InteractionSpec interactionSpec
    ) throws ServiceException;
    
}
