/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Standard Plug-In 
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
import org.openmdx.base.aop1.PlugIn_1_0;
import org.openmdx.base.aop1.Segment_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.state2.cci.DateStateContext;
import org.openmdx.state2.cci.DateTimeStateContext;
import org.openmdx.state2.cci.StateContext;
import org.openmdx.state2.spi.TechnicalAttributes;

/**
 * Standard Plug-In
 */
public class PlugIn_1 implements PlugIn_1_0 {

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
    protected Interceptor_1 getInterceptor(
        ObjectView_1_0 view,
        Interceptor_1 next,
        ModelElement_1_0 type,
        boolean validTimeUnique
    ) throws ServiceException {
        return buildObjectInterceptor(
        	view, 
        	buildContainerInterceptor(
            	view, 
            	next, 
            	type, 
            	validTimeUnique
            ), 
        	type, 
        	validTimeUnique
        );
    }        

	/**
	 * Build the container interceptor
	 * 
	 * @param view
	 * @param next
	 * @param type
	 * @param validTimeUnique
	 * @return the container interceptor
	 * @throws ServiceException
	 */
    protected Interceptor_1 buildContainerInterceptor(
        ObjectView_1_0 view,
        Interceptor_1 next,
        ModelElement_1_0 type,
        boolean validTimeUnique
    ) throws ServiceException {
        Interceptor_1 interceptor = new org.openmdx.state2.aop1.Object_1(
            view,
            next
        ){

            @Override
            protected Container_1_0 newStateCapableContainer(
                ObjectView_1_0 parent,
                Container_1_0 container,
                String type
            ) throws ServiceException {
                return PlugIn_1.this.newStateCapableContainer(parent, container, type);
            }
            
        };
        return view.getModel().isSubtypeOf(type, "org:openmdx:base:Segment") ? new Segment_1(
            view,
            interceptor
        ){

                @Override
                protected Container_1_0 newExtent(
                    ObjectView_1_0 parent,
                    Container_1_0 container
                ) throws ServiceException {
                    return new Extent_1(parent, container){

						/**
						 * Implements <code>Serializable</code>
						 */
						private static final long serialVersionUID = -6873613222542014345L;

						@Override
						Container_1_0 newStateCapableContainer(
							ObjectView_1_0 parent, 
							Container_1_0 container,
							String type
						) throws ServiceException {
							return PlugIn_1.this.newStateCapableContainer(parent, container, type);
						}
                    	
                    };
                }
               
        } : interceptor;
    }        

    /**
     * Create a <code>StateCapable</code> container
     * 
     * @param parent
     * @param container
     * @param type
     * @return a new <code>StateCapable</code> container
     * 
     * @throws ServiceException
     */
    protected Container_1_0 newStateCapableContainer(
	    ObjectView_1_0 parent,
	    Container_1_0 container, 
	    String type 
    ) throws ServiceException{
    	return new StateCapableContainer_1(
    		parent,
    		container,
    		type
    	);
    }
    
	/**
	 * Build the object interceptor
	 * 
	 * @param view
	 * @param interceptor
	 * @param type
	 * @param validTimeUnique
	 * @return the object interceptor
	 * @throws ServiceException
	 */
    protected Interceptor_1 buildObjectInterceptor(
        ObjectView_1_0 view,
        Interceptor_1 interceptor,
        ModelElement_1_0 type,
        boolean validTimeUnique
    ) throws ServiceException {
    	if(view.getModel().isSubtypeOf(type, "org:openmdx:state2:StateCapable")) {
    		InteractionSpec context = view.getInteractionSpec();
			return context == null ? newStateCapableInterceptor(
				view, 
				interceptor, 
				type, 
				validTimeUnique
			) : context instanceof DateStateContext ? newDateStateInterceptor(
				view, 
				interceptor, 
				type, 
				validTimeUnique
			) : context instanceof DateTimeStateContext ? newDateTimeStateInterceptor(
				view, 
				interceptor, 
				type, 
				validTimeUnique
			) : interceptor;
    	} else {
			return  interceptor;
    	}
    }
    
	/**
	 * Build a StateCapable interceptor
	 * 
	 * @param view
	 * @param interceptor
	 * @param type 
	 * @param validTimeUnique 
	 * 
	 * @return a new StateCapable interceptor
	 * 
	 * @throws ServiceException
	 */
	protected DateTimeState_1 newDateTimeStateInterceptor(
		ObjectView_1_0 view,
		Interceptor_1 interceptor, 
		ModelElement_1_0 type, 
		boolean validTimeUnique
	) throws ServiceException {
		return new org.openmdx.state2.aop1.DateTimeState_1(
			view,
			interceptor
		);
	}

	/**
	 * Build a DateState interceptor
	 * 
	 * @param view
	 * @param interceptor
	 * @param type 
	 * @param validTimeUnique 
	 * 
	 * @return a new DateState interceptor
	 * 
	 * @throws ServiceException
	 */
	protected DateState_1 newDateStateInterceptor(
		ObjectView_1_0 view,
		Interceptor_1 interceptor, 
		ModelElement_1_0 type, 
		boolean validTimeUnique
	) throws ServiceException {
		return new org.openmdx.state2.aop1.DateState_1(
			view,
			interceptor
		);
	}

	/**
	 * Build a DateTimeState interceptor
	 * 
	 * @param view
	 * @param interceptor
	 * @param type 
	 * @param validTimeUnique 
	 * 
	 * @return a new DateTimeState interceptor
	 * 
	 * @throws ServiceException
	 */
	protected StateCapable_1 newStateCapableInterceptor(
		ObjectView_1_0 view,
		Interceptor_1 interceptor, 
		ModelElement_1_0 type, 
		boolean validTimeUnique
	) throws ServiceException {
		return new org.openmdx.state2.aop1.StateCapable_1(
            view,
            interceptor
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
    protected Interceptor_1 getInterceptor(
        ObjectView_1_0 view,
        Interceptor_1 next,
        ModelElement_1_0 type
    ) throws ServiceException {
    	return getInterceptor(
    		view, 
    		next, 
    		type,
    		false
    	);
    }
	
	/* (non-Javadoc)
     * @see org.openmdx.base.aop1.PlugIn#getInterceptor(org.openmdx.base.accessor.view.Interceptor_1)
     */
    public final Interceptor_1 getInterceptor(
        ObjectView_1_0 view,
        Interceptor_1 next
    ) throws ServiceException {
    	return getInterceptor(
    		view, 
    		next, 
    		view.getModel().getElement(view.objGetDelegate().objGetClass())
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
		    TechnicalAttributes.TRANSACTION_TIME_UNIQUE.equals(feature) &&
			Boolean.TRUE.equals(value) &&
			object.getModel().isInstanceof(object, "org:openmdx:state2:StateCapable")
		){
			object.objSetValue(feature, value);
			return true;
		} else {
			return false;
		}
	}

    /* (non-Javadoc)
     * @see org.openmdx.base.aop1.PlugIn_1_0#resolveObjectClass(java.lang.String, javax.resource.cci.InteractionSpec)
     */
    @Override
    public String resolveObjectClass(
        String objectClass,
        InteractionSpec interactionSpec
    ) throws ServiceException {
        Model_1_0 model = Model_1Factory.getModel();
        
        if(model.isSubtypeOf(objectClass, "org:openmdx:state2:StateCapable")){
            boolean stateRequired = interactionSpec instanceof StateContext;
            boolean stateRequested = model.isSubtypeOf(objectClass, "org:openmdx:state2:BasicState");
            if(stateRequired != stateRequested) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "There is a mismatch between the kind of class required " +
                "by the  context and the kind of class to be created",
                new BasicException.Parameter("interactionSpec", interactionSpec),
                new BasicException.Parameter("stateRequired", stateRequired),
                new BasicException.Parameter("modelClass", objectClass),
                new BasicException.Parameter("stateRequested", stateRequested)
            );
        }
        return objectClass;
    }

}
