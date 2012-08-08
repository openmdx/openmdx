/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: PlugIn_1.java,v 1.7 2011/09/09 17:35:19 hburger Exp $
 * Description: StandardPlugIn 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/09/09 17:35:19 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.state2.aop1;

import javax.resource.cci.InteractionSpec;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.aop1.PlugIn_1_0;
import org.openmdx.base.aop1.Segment_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.state2.cci.DateStateContext;
import org.openmdx.state2.cci.DateTimeStateContext;
import org.openmdx.state2.spi.Configuration;

/**
 * Standard Plug-In
 */
public class PlugIn_1 implements PlugIn_1_0 {

    /* (non-Javadoc)
     * @see org.openmdx.base.aop1.PlugIn#getInterceptor(org.openmdx.base.accessor.view.Interceptor_1)
     */
    public Interceptor_1 getInterceptor(
        ObjectView_1_0 view,
        Interceptor_1 next
    ) throws ServiceException {
        Interceptor_1 interceptor = next;
        InteractionSpec interactionSpec = view.getInteractionSpec();
        Model_1_0 model = view.getModel();
        ModelElement_1_0 dataObjectType = model.getElement(view.objGetDelegate().objGetClass());
        boolean stateCapable = model.isSubtypeOf(dataObjectType, "org:openmdx:state2:StateCapable");
        Path objectId = view.jdoGetObjectId();
        Boolean validTimeUnique;
        if(objectId != null) {
            validTimeUnique = Boolean.valueOf(SharedObjects.getPlugInObject(view.jdoGetPersistenceManager(), Configuration.class).isValidTimeUnique(objectId));
        } else if (stateCapable) {
            validTimeUnique = (Boolean) interceptor.objGetValue("validTimeUnique");
        } else {
            validTimeUnique = Boolean.valueOf(SharedObjects.getPlugInObject(view.jdoGetPersistenceManager(), Configuration.class).isTheChildrensValidTimeUnique(dataObjectType));
        }
        if(!Boolean.TRUE.equals(validTimeUnique)){
	        interceptor = new org.openmdx.state2.aop1.Object_1(
	            view,
	            interceptor
	        );
            if(model.isSubtypeOf(dataObjectType, "org:openmdx:base:Segment")) {
                interceptor = new Segment_1(
                    view,
                    interceptor
                ){
    
                    @Override
                    protected Container_1_0 newExtent(
                        ObjectView_1_0 parent,
                        Container_1_0 container
                    ) throws ServiceException {
                        return new Extent_1(parent, container);
                    }
                   
                };
            }
        }
        if(stateCapable) {
            if(Boolean.TRUE.equals(validTimeUnique) || interactionSpec == null) {
                //
                // State Capability
                //
                interceptor = new org.openmdx.state2.aop1.StateCapable_1(
                    view,
                    interceptor, 
                    validTimeUnique
                );
            } else if(interactionSpec instanceof DateStateContext) {
                //
                // DateState View
                //
                interceptor = new org.openmdx.state2.aop1.DateState_1(
                    view,
                    interceptor
                );
            } else if(interactionSpec instanceof DateTimeStateContext) {
                //
                // DateTimeState View
                //
                interceptor = new org.openmdx.state2.aop1.DateTimeState_1(
                    view,
                    interceptor
                );
            }
        }
        return interceptor;
    }

}
