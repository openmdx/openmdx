/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: TabControl
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.UiContext;
import org.openmdx.portal.servlet.attribute.Attribute;

/**
 * FormControl
 *
 */
public class FormControl extends UiTabControl implements Serializable {
  
	/**
     * Constructor.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param uiContext
     * @param formDef
     */
    public FormControl(
        String id,
        String locale,
        int localeAsIndex,
        UiContext uiContext,
        org.openmdx.ui1.jmi1.FormDefinition formDef
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            formDef,
            FormControl.getFieldGroupControls(
                id,
                locale,
                localeAsIndex,
                uiContext,
                formDef
            )
        );
    }

    /**
     * Get field group controls.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param uiContext
     * @param formDefinition
     * @return
     */
    protected static List<UiFieldGroupControl> getFieldGroupControls(
        String id,
        String locale,
        int localeAsIndex,
        UiContext uiContext,
        org.openmdx.ui1.jmi1.FormDefinition formDefinition
    ) {
        List<UiFieldGroupControl> formFieldGroupControls = new ArrayList<UiFieldGroupControl>();
        org.openmdx.ui1.cci2.FormFieldGroupDefinitionQuery query = 
            (org.openmdx.ui1.cci2.FormFieldGroupDefinitionQuery)JDOHelper.getPersistenceManager(formDefinition).newQuery(org.openmdx.ui1.jmi1.FormFieldGroupDefinition.class);
        query.orderByOrder().ascending();
        List<org.openmdx.ui1.jmi1.FormFieldGroupDefinition> formFieldGroupDefinitions = formDefinition.getFormFieldGroupDefinition(query);
        int index = 0;
        for(org.openmdx.ui1.jmi1.FormFieldGroupDefinition formFieldGroupDefinition: formFieldGroupDefinitions) {
            formFieldGroupControls.add(
                new UiFieldGroupControl(
                    id + "[" + index + "]",
                    locale,
                    localeAsIndex,
                    uiContext,
                    formFieldGroupDefinition
                )
            );
            index++;
        }
        return formFieldGroupControls;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.control.Control#paint(org.openmdx.portal.servlet.ViewPort, java.lang.String, boolean)
     */
    @Override
    public void paint(
        ViewPort p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {
        for(Control fieldGroup: this.getChildren(UiFieldGroupControl.class)) {
            fieldGroup.paint(
                p,
                frame,
                forEditing
            );
        }
    }

    /**
     * Map parameter map to object.
     * 
     * @param parameterMap
     * @param object
     * @param app
     * @param pm
     */
    public void updateObject(
        Map<String,String[]> parameterMap,
        Object object,
        ApplicationContext app,
        PersistenceManager pm
    ) {
        for(UiFieldGroupControl fieldGroupControl: this.getChildren(UiFieldGroupControl.class)) {
            Map<String,Attribute> attributesAsMap = new HashMap<String,Attribute>();
            Attribute[][] attributes = fieldGroupControl.getAttribute(object, app);
            for(Attribute[] column: attributes) {
                for(Attribute attribute: column) {
                    if((attribute != null) && !attribute.isEmpty()) {
                        attributesAsMap.put(
                            attribute.getValue().getName(),
                            attribute
                        );
                    }
                }
            }
            app.getPortalExtension().updateObject(
                object, 
                parameterMap, 
                attributesAsMap, 
                app
            );
        }
    }
        
    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
	private static final long serialVersionUID = 3404842425846192954L;

}

//--- End of File -----------------------------------------------------------
