/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: UserDefinedView.java,v 1.13 2008/11/10 15:16:30 wfro Exp $
 * Description: UserDefinedView 
 * Revision:    $Revision: 1.13 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/10 15:16:30 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.view;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.attribute.AttributeValue;

public class UserDefinedView
    extends View
    implements Serializable {

    //-------------------------------------------------------------------------
    public UserDefinedView(
        Object object,
        ApplicationContext application,
        View parentView
    ) {
        super(
            UUIDs.getGenerator().next().toString(),
            null,
            object,
            application
        );
        this.attributes = new HashMap<String,Attribute>();
        this.requestId = parentView.getRequestId();
    }

    //-------------------------------------------------------------------------
    public String getType(
    ) {
        return VIEW_USER_DEFINED;
    }

    //-------------------------------------------------------------------------
    private Attribute addAttribute(
        String id,
        org.openmdx.ui1.jmi1.ValuedField customizedField,
        Object object
    ) throws ServiceException {
        AttributeValue attributeValue = this.application.createAttributeValue(
            customizedField,
            object
        );
        Attribute attribute = new Attribute(
            this.application.getCurrentLocaleAsIndex(), 
            customizedField, 
            attributeValue
        );
        this.attributes.put(
            id,
            attribute
        );
        return attribute;
    }    
        
    //-------------------------------------------------------------------------
    public org.openmdx.ui1.jmi1.ValuedField findField(
        String forClass,
        String featureName
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.Inspector inspector = this.application.getInspector(forClass);
        for(Iterator i = inspector.getMember().iterator(); i.hasNext(); ) {
            Object pane = i.next();
            if (pane instanceof org.openmdx.ui1.jmi1.AttributePane) {
                org.openmdx.ui1.jmi1.AttributePane paneAttr = (org.openmdx.ui1.jmi1.AttributePane)pane;
                for(Iterator j = paneAttr.getMember().iterator(); j.hasNext(); ) {
                    org.openmdx.ui1.jmi1.Tab tab = (org.openmdx.ui1.jmi1.Tab)j.next();
                    for(Iterator k = tab.getMember().iterator(); k.hasNext(); ) {
                        org.openmdx.ui1.jmi1.FieldGroup fieldGroup = (org.openmdx.ui1.jmi1.FieldGroup)k.next();
                        for(Iterator l = fieldGroup.getMember().iterator(); l.hasNext(); ) {
                            org.openmdx.ui1.jmi1.ValuedField field = (org.openmdx.ui1.jmi1.ValuedField)l.next();
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

    //-------------------------------------------------------------------------
    public String getFieldLabel(
        String forClass,
        String featureName,
        short locale
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.LabelledField field = this.findField(
            forClass, 
            featureName
        );
        return field == null
            ? null
            : locale < field.getLabel().size()
                ? field.getLabel().get(locale)
                : field.getLabel().get(0);
    }
    
    //-------------------------------------------------------------------------
    public Attribute addAttribute(
        String id,
        String uiElementId,
        Object object
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.ValuedField customizedField = 
            (org.openmdx.ui1.jmi1.ValuedField)this.application.getUiElement(
                uiElementId
            );
        return this.addAttribute(
            id, 
            customizedField, 
            object
        );
    }    
        
    //-------------------------------------------------------------------------
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
    
    //-------------------------------------------------------------------------
    public Attribute getAttribute(
        String id
    ) {
        return (Attribute)this.attributes.get(id);
    }
    
    //-------------------------------------------------------------------------
    public Map getAsFieldMap(
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
    
    //-------------------------------------------------------------------------
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
    
    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = -8074193422830498288L;
    
    protected final Map<String,Attribute> attributes;

}
