/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Legacy Plug-In
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2013, OMEX AG, Switzerland
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
import org.openmdx.base.accessor.rest.DataObject_1;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.state2.spi.LegacyPlugInHelper;

/**
 * Add valid-time-unique support
 */
public class LegacyPlugIn_1 extends PlugIn_1 {

	/**
	 * Build the interceptor
	 * 
	 * @param view
	 * @param next
	 * @param type
	 * @param validTimeUnique
	 * @return the amended interceptor
	 * @throws ServiceException
	 */
	@Override
    protected Interceptor_1 getInterceptor(
        ObjectView_1_0 view,
        Interceptor_1 next,
        ModelElement_1_0 type,
        boolean validTimeUnique
    ) throws ServiceException {
    	return validTimeUnique ? newStateCapableInterceptor(
        	view, 
        	next, 
        	type, 
        	validTimeUnique
    	) : super.getInterceptor(
        	view, 
        	next, 
        	type, 
        	validTimeUnique
        );
    }        
	
	
	/**
	 * Build the interceptor
	 * 
	 * @param view
	 * @param interceptor
	 * @param validTimeUnique
	 * @return the amended interceptor
	 * @throws ServiceException
	 */
	@Override
    protected Interceptor_1 getInterceptor(
        ObjectView_1_0 view,
        Interceptor_1 next,
        ModelElement_1_0 type
    ) throws ServiceException {
    	return getInterceptor(
    		view, 
    		next, 
    		type,
    		LegacyPlugInHelper.isValidTimeUnique(view, next, type)
    	);
    }
	
	/* (non-Javadoc)
	 * @see org.openmdx.state2.aop1.PlugIn_1#newStateCapableInterceptor(org.openmdx.base.accessor.view.ObjectView_1_0, org.openmdx.base.accessor.view.Interceptor_1, boolean)
	 */
	@Override
	protected StateCapable_1 newStateCapableInterceptor(
		ObjectView_1_0 view,
		Interceptor_1 interceptor, 
		ModelElement_1_0 type, 
		boolean validTimeUnique
	) throws ServiceException {
		return LegacyPlugInHelper.isLegacy(view, type) ? new LegacyStateCapable_1(
            view,
            interceptor,
            validTimeUnique
        ) : super.newStateCapableInterceptor(
        	view, 
        	interceptor, 
        	type, 
        	validTimeUnique
        );
	}

	/* (non-Javadoc)
	 * @see org.openmdx.state2.aop1.PlugIn_1#newStateCapableContainer(org.openmdx.base.accessor.view.ObjectView_1_0, org.openmdx.base.accessor.cci.Container_1_0, java.lang.String)
	 */
	@Override
	protected Container_1_0 newStateCapableContainer(
		ObjectView_1_0 parent,
		Container_1_0 container,
		String type
	) throws ServiceException {
    	return new LegacyStateCapableContainer_1(
    		parent,
    		container,
    		type
    	);
	}

	/* (non-Javadoc)
	 * @see org.openmdx.state2.aop1.PlugIn_1#newDateStateInterceptor(org.openmdx.base.accessor.view.ObjectView_1_0, org.openmdx.base.accessor.view.Interceptor_1, org.openmdx.base.mof.cci.ModelElement_1_0, boolean)
	 */
	@Override
	protected DateState_1 newDateStateInterceptor(
		ObjectView_1_0 view,
		Interceptor_1 interceptor, 
		ModelElement_1_0 type,
		boolean validTimeUnique
	) throws ServiceException {
		return LegacyPlugInHelper.isLegacy(view, type) ? new LegacyDateState_1(
			view,	
			interceptor,
			validTimeUnique
		) : super.newDateStateInterceptor(
			view, 
			interceptor, 
			type, 
			validTimeUnique
		);
	}


	/* (non-Javadoc)
	 * @see org.openmdx.base.aop0.PlugIn_1_0#propagatedEagerly(org.openmdx.base.accessor.rest.DataObject_1, java.lang.String, java.lang.Object)
	 */
    @Override
	public boolean propagatedEagerly(
		DataObject_1 object, 
		String feature,
		Object value
	) throws ServiceException {
		if(
			"validTimeUnique".equals(feature) &&
			Boolean.TRUE.equals(value) &&
			object.getModel().isInstanceof(object, "org:openmdx:state2:Legacy")
		){
			object.objSetValue(feature, value);
			return true;
		} else {
			return super.propagatedEagerly(object, feature, value);
		}
	}


    /* (non-Javadoc)
     * @see org.openmdx.state2.aop1.PlugIn_1#resolveObjectClass(java.lang.String, javax.resource.cci.InteractionSpec)
     */
    @Override
    public String resolveObjectClass(
        String objectClass,
        InteractionSpec interactionSpec
    ) throws ServiceException {
        if(Model_1Factory.getModel().isSubtypeOf(objectClass, "org:openmdx:state2:Legacy")) {
            return objectClass;
        } else {
            return super.resolveObjectClass(objectClass, interactionSpec);
        }
    }

}
