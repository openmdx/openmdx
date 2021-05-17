/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: JoiningListDataBinding 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland
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
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */
package org.openmdx.portal.servlet.databinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jmi.reflect.RefObject;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.query.SortOrder;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.DataBinding;

/**
 * JoiningListDataBinding
 *
 */
public class JoiningListDataBinding extends DataBinding {

	/**
	 * Constructor.
	 * 
	 * @param params
	 */
	public JoiningListDataBinding(
		String params
	) {
		String[] features = null;
		String orderBy = null;
		if(params != null) {
			String[] pList = params.split(";");
			for(int i = 0; i < pList.length; i++) {
				String p = pList[i];
				if(p.startsWith("features=[")) {
					features = p.substring(10, p.length()-1).split(",");
				} else if(p.startsWith("orderBy=[")) {
					orderBy = p.substring(9, p.length()-1);
				}
			}
		}
		this.features = features;
		this.orderBy = orderBy;
	}

	/* (non-Javadoc)
     * @see org.openmdx.portal.servlet.DataBinding#getValue(javax.jmi.reflect.RefObject, java.lang.String, org.openmdx.portal.servlet.ApplicationContext)
     */
    @Override
    public Object getValue(
    	RefObject object, 
    	String qualifiedFeatureName, 
    	ApplicationContext app
    ) {
    	Model_1_0 model = Model_1Factory.getModel();
    	List<RefObject_1_0> objects = new ArrayList<RefObject_1_0>();
    	if(this.features != null) {
    		for(String feature: this.features) {
    			try {
    				ModelElement_1_0 classDef = model.getElement(object.refClass().refMofId());
    				if(model.getFeatureDef(classDef, feature, false) != null) {
    	                @SuppressWarnings({
    	                    "unchecked", "rawtypes"
    	                })
    	                RefContainer<RefObject_1_0> container = (RefContainer)object.refGetValue(feature);
    	                org.openmdx.base.query.Filter query = new org.openmdx.base.query.Filter();
    	                if(this.orderBy != null) {
    	            		String[] orderByArgs = this.orderBy.split("\\.");
    	                	query.setOrderSpecifier(
    	                		Arrays.asList(
    	                			orderByArgs.length == 2 
    	                				? new OrderSpecifier(orderByArgs[0], SortOrder.valueOf(orderByArgs[1]))
    	                				: new OrderSpecifier(this.orderBy, SortOrder.ASCENDING)
    	                		)
    	                	);
    	                }
    	                objects.addAll(container.refGetAll(query));    					
    				}
    			} catch(Exception ignore) {}
    		}
    	}
    	return objects;
    }

	/* (non-Javadoc)
     * @see org.openmdx.portal.servlet.DataBinding#setValue(javax.jmi.reflect.RefObject, java.lang.String, java.lang.Object, org.openmdx.portal.servlet.ApplicationContext)
     */
    @Override
    public void setValue(
    	RefObject object, 
    	String qualifiedFeatureName, 
    	Object newValue, 
    	ApplicationContext app
    ) {	    
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private final String[] features;
    private final String orderBy;
}
