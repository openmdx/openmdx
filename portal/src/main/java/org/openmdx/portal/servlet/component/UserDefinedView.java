/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: UserDefinedView 
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.attribute.AttributeValue;

/**
 * UserDefinedView
 *
 */
public class UserDefinedView extends View implements Serializable {

    /**
     * Constructor.
     * 
     * @param object
     * @param app
     * @param parentView
     */
    public UserDefinedView(
        Object object,
        ApplicationContext app,
        View parentView
    ) {
        super(
        	null, // no control
            UUIDs.newUUID().toString(),
            null,
            object,
            null, // resourcePathPrefix
            null, // navigationTarget
            null, // isReadOnly
            app
        );
        this.attributes = new HashMap<String,Attribute>();
        this.requestId = parentView.getRequestId();
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
     * Add attribute to view.
     * 
     * @param id
     * @param customizedField
     * @param object
     * @return
     * @throws ServiceException
     */
    private Attribute addAttribute(
        String id,
        org.openmdx.ui1.jmi1.ValuedField customizedField,
        Object object
    ) throws ServiceException {
        AttributeValue attributeValue = this.app.getPortalExtension().getControlFactory().createAttributeValue(
            customizedField,
            object,
            this.app
        );
        Attribute attribute = new Attribute(
            this.app.getCurrentLocaleAsIndex(), 
            customizedField, 
            attributeValue
        );
        this.attributes.put(
            id,
            attribute
        );
        return attribute;
    }    
        
    /**
     * Add attribute to view.
     * 
     * @param id
     * @param uiElementId
     * @param object
     * @return
     * @throws ServiceException
     */
    public Attribute addAttribute(
        String id,
        String uiElementId,
        Object object
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.ValuedField customizedField = 
            (org.openmdx.ui1.jmi1.ValuedField)this.app.getUiElement(
                uiElementId
            );
        return this.addAttribute(
            id, 
            customizedField, 
            object
        );
    }    
        
    /**
     * Add attribute to view.
     * 
     * @param id
     * @param forClass
     * @param featureName
     * @param object
     * @return
     * @throws ServiceException
     */
    public Attribute addAttribute(
        String id,
        String forClass,
        String featureName,
        Object object
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.ValuedField customizedField = this.findField(
            forClass, 
            featureName
        );
        if(customizedField == null) return null;
        return this.addAttribute(
            id,
            customizedField,
            object
        );
    }
    
    /**
     * Get attribute.
     * 
     * @param id
     * @return
     */
    public Attribute getAttribute(
        String id
    ) {
        return (Attribute)this.attributes.get(id);
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
     * Get parameters as parameter map.
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
    private static final long serialVersionUID = -8074193422830498288L;
    
    protected final Map<String,Attribute> attributes;

}
