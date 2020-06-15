/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Standard Plug-In 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2012, OMEX AG, Switzerland
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

import javax.resource.cci.InteractionSpec;

import org.openmdx.base.accessor.rest.DataObject_1;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;

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

    /**
     * Tells whether the Removable interceptor shall be applied
     * 
     * @param type
     * 
     * @return <code>true</code> if the Removable interceptor shall be applied
     * 
     * @throws ServiceException
     */
    protected boolean isRemovable(
        ObjectView_1_0 view,
        Interceptor_1 next,
        ModelElement_1_0 type
    ) throws ServiceException{
        return type.getModel().isSubtypeOf(type, "org:openmdx:base:Removable");
    }

    /**
     * Tells whether the Segment interceptor shall be applied
     * 
     * @param view
     * @param next
     * @param type
     * 
     * @return <code>true</code> if the Segment interceptor shall be applied
     * 
     * @throws ServiceException
     */
    protected boolean isSegment(
        ObjectView_1_0 view,
        Interceptor_1 next,
        ModelElement_1_0 type
    ) throws ServiceException{
        return type.getModel().isSubtypeOf(type, "org:openmdx:base:Segment");
    }
    
    /**
     * Tells whether ExtentCapable interceptor shall be applied
     * 
     * @param type
     * 
     * @return <code>true</code> if the ExtentCapable interceptor shall be applied
     * 
     * @throws ServiceException
     */
    protected boolean isExtentCapable(
        ObjectView_1_0 view,
        Interceptor_1 next,
        ModelElement_1_0 type
    ) throws ServiceException{
        return type.getModel().isSubtypeOf(type, "org:openmdx:base:ExtentCapable");
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.aop1.PlugIn#getInterceptor(org.openmdx.base.accessor.view.Interceptor_1)
     */
    public Interceptor_1 getInterceptor(
        ObjectView_1_0 view,
        Interceptor_1 next
    ) throws ServiceException {
        Interceptor_1 interceptor = next;
        ModelElement_1_0 type = view.getModel().getElement(view.objGetDelegate().objGetClass());
        //
        // Removable Capability
        //
        if(isRemovable(view, next, type)) {
            interceptor = new Removable_1(
                view,
                interceptor
            );
        }
        //
        // Extent Capability
        //
        if(isSegment(view, next, type)) {
            interceptor = new Segment_1(
                view,
                interceptor
            );
        } else if (isExtentCapable(view, next, type)) {
            interceptor = new ExtentCapable_1(
                view,
                interceptor
            );
        }
        return interceptor;
    }

	/* (non-Javadoc)
	 * @see org.openmdx.base.aop1.PlugIn_1_0#propagatedEagerly(org.openmdx.base.accessor.rest.DataObject_1, java.lang.String, java.lang.Object)
	 */
    @Override
	public boolean propagatedEagerly(
		DataObject_1 object, 
		String feature,
		Object value
	) throws ServiceException {
		return false;
	}

    /* (non-Javadoc)
     * @see org.openmdx.base.aop1.PlugIn_1_0#resolveObjectClass(java.lang.String, javax.resource.cci.InteractionSpec)
     */
    @Override
    public String resolveObjectClass(
        String objectClass,
        InteractionSpec interactionSpec
    ) throws ServiceException {
        return objectClass;
    }

}
