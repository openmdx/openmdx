/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: TransientObjectView 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.attribute.Attribute;

/**
 * TransientObjectView
 *
 */
public class TransientObjectView extends View implements Serializable {

	/**
     * Constructor.
     * 
     * @param object
     * @param app
     * @param lookupObject
     * @param pm
     */
    public TransientObjectView(
        Object object,
        ApplicationContext app,
        RefObject_1_0 lookupObject,
        PersistenceManager pm
    ) {
        super(
        	null, // no control
            UUIDs.newUUID().toString(),
            null,
            marshalObject(object, pm),
            null, // resourcePathPrefix
            null, // navigationTarget
            null, // isReadOnly
            app
        );
        this.lookupObject = lookupObject;
    }

    /**
     * Convert attribute values instance of of Path to corresponding object.
     * 
     * @param object
     * @param pm
     * @return
     */
    protected static Object marshalObject(
    	Object object,
    	PersistenceManager pm
    ) {
    	if(object instanceof Map) {
    		@SuppressWarnings("unchecked")
            Map<String,Object> source = (Map<String,Object>)object;
    		Map<String,Object> target = new HashMap<String,Object>();
    		for(String feature: source.keySet()) {
    			Object value = source.get(feature);
    			target.put(
    				feature,
    				marshalValue(value, pm)
    			);
    		} 
    		return target;
    	} else {
    		return object;
    	}
    }
    
    /**
     * Marshal value.
     * 
     * @param value
     * @param pm
     * @return
     */
    protected static Object marshalValue(
    	Object value,
    	PersistenceManager pm
    ) {
    	if(value instanceof Path) {
    		return pm.getObjectById((Path)value);
    	} else if(value instanceof Collection) {
    		List<Object> values = new ArrayList<Object>();
    		for(Object v: (Collection<?>)value) {
    			values.add(
    				marshalValue(v, pm)
    			);
    		}
    		return values;
    	} else {
    		return value;
    	}
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.View#getType()
     */
    @Override
    public String getType(
    ) {
        return VIEW_USER_DEFINED;
    }

    /**
     * Get loookup object.
     * 
     * @return
     */
    public RefObject_1_0 getLookupObject(
    ) {
        return this.lookupObject;
    }
    
    /**
     * Get attributes as field map.
     * 
     * @param attributes
     * @return
     */
    public Map<String,Attribute> getAsFieldMap(
        Attribute[] attributes
    ) {
        Map<String,Attribute> fieldMap = new HashMap<String,Attribute>();
        for(
            int i = 0; i < attributes.length; 
            i++
        ) {
            fieldMap.put(
                attributes[i].getValue().getName(),
                attributes[i]
            );
        }
        return fieldMap;
    }
    
    /**
     * Get parameters as paramter map.
     * 
     * @param allParameters
     * @param requiredParameters
     * @return
     */
    public Map<String,?> getAsParameterMap(
        Map<String,?> allParameters,
        String[] requiredParameters
    ) {
        Map<String,Object> parameterMap = new HashMap<String,Object>();
        for(
            int i = 0; i < requiredParameters.length; 
            i++
        ) {
            if(allParameters.get(requiredParameters[i]) != null) {
                parameterMap.put(
                    requiredParameters[i],
                    allParameters.get(requiredParameters[i])
                );
            }
        }
        return parameterMap;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.View#findField(java.lang.String, java.lang.String)
     */
    @Override
    public org.openmdx.ui1.jmi1.ValuedField findField(
        String forClass,
        String featureName
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.Inspector inspector = this.app.getInspector(forClass);
        for(Object pane: inspector.getMember()) {
            if (pane instanceof org.openmdx.ui1.jmi1.AttributePane) {
                org.openmdx.ui1.jmi1.AttributePane paneAttr = (org.openmdx.ui1.jmi1.AttributePane)pane;
                List<org.openmdx.ui1.jmi1.Tab> tabs = paneAttr.getMember();
                for(org.openmdx.ui1.jmi1.Tab tab: tabs) {
                	List<org.openmdx.ui1.jmi1.FieldGroup> fieldGroups = tab.getMember();
                    for(org.openmdx.ui1.jmi1.FieldGroup fieldGroup: fieldGroups) {
                    	List<org.openmdx.ui1.jmi1.ValuedField> fields = fieldGroup.getMember();
                        for(org.openmdx.ui1.jmi1.ValuedField field: fields) {
                            if(field.getFeatureName().equals(featureName)) {
                                return field;
                            }
                        }
                    }
                }
            }          
        }   
        return null;
    }    

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.View#getFieldLabel(java.lang.String, java.lang.String, short)
     */
    @Override
    public String getFieldLabel(
        String forClass,
        String featureName,
        short locale
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.LabelledField field = this.findField(
            forClass, 
            featureName
        );
        return field == null ? 
        	null : 
        		locale < field.getLabel().size() ? 
        			field.getLabel().get(locale) : 
        				field.getLabel().get(0);
    }
        
    /**
     * Get short label for feature.
     * 
     * @param forClass
     * @param featureName
     * @param locale
     * @return
     * @throws ServiceException
     */
    public String getShortLabel(
        String forClass,
        String featureName,
        short locale
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.LabelledField field = this.findField(
            forClass, 
            featureName
        );
        return field == null ? 
        	null :
        		field.getShortLabel().isEmpty() ?
        			this.getFieldLabel(forClass, featureName, locale) :
		        		locale < field.getShortLabel().size() ? 
		        			field.getShortLabel().get(locale) : 
		        				field.getShortLabel().get(0);
    }

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.view.Canvas#refresh(boolean)
	 */
	@Override
	public void refresh(
		boolean refreshData
	) throws ServiceException {
	}

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.view.Component#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Component> List<T> getChildren(
		Class<T> type
	) {
		return Collections.emptyList();
	}

    /* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.component.Component#paint(org.openmdx.portal.servlet.ViewPort, java.lang.String, boolean)
	 */
	@Override
	public void paint(
		ViewPort p, 
		String frame, 
		boolean forEditing
	) throws ServiceException {
		this.control.paint(p, frame, forEditing);
	}

    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
	private static final long serialVersionUID = -8595763565176473920L;

	private final RefObject_1_0 lookupObject;

}
