/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: PlugIn_1.java,v 1.4 2012/01/07 01:37:44 hburger Exp $
 * Description: StandardPlugIn 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2012/01/07 01:37:44 $
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
package org.openmdx.base.aop1;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.state2.spi.Configuration;

/**
 * Standard Plug-In
 */
public class PlugIn_1 implements PlugIn_1_0 {

    /**
     * Constructor 
     */
    public PlugIn_1() {
        super();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop1.PlugIn#getInterceptor(org.openmdx.base.accessor.view.Interceptor_1)
     */
    public Interceptor_1 getInterceptor(
        ObjectView_1_0 view,
        Interceptor_1 next
    ) throws ServiceException {
        Interceptor_1 interceptor = next;
        Model_1_0 model = view.getModel();
        ModelElement_1_0 dataObjectType = model.getElement(view.objGetDelegate().objGetClass());
        if(model.isSubtypeOf(dataObjectType, "org:openmdx:base:Removable")) {
            Path objectId = view.jdoGetObjectId();
            boolean validTimeUnique = objectId == null ? 
                model.isSubtypeOf(dataObjectType, "org:openmdx:state2:StateCapable") && Boolean.TRUE.equals(interceptor.objGetValue("validTimeUnique")) :
                SharedObjects.getPlugInObject(view.jdoGetPersistenceManager(), Configuration.class).isValidTimeUnique(objectId); 
            if(!validTimeUnique) {
                //
                // Removable Capability
                //
                interceptor = new org.openmdx.base.aop1.Removable_1(
                    view,
                    interceptor
                );
            }
        }
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
        } else if (model.isSubtypeOf(dataObjectType, "org:openmdx:base:ExtentCapable")) {
            //
            // Extent Capability
            //
            interceptor = new org.openmdx.base.aop1.ExtentCapable_1(
                view,
                interceptor
            );
        }
        return interceptor;
    }

}
