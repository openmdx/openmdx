/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: FormControl.java,v 1.7 2010/03/28 00:49:52 wfro Exp $
 * Description: TabControl
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/28 00:49:52 $
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

public class FormControl
    extends TabControl
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public FormControl(
        String id,
        String locale,
        int localeAsIndex,
        UiContext uiContext,
        org.openmdx.ui1.jmi1.FormDefinition formDefinition
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            formDefinition,
            FormControl.getFieldGroupControls(
                id,
                locale,
                localeAsIndex,
                uiContext,
                formDefinition
            )
        );
    }

    //-------------------------------------------------------------------------
    protected static List<FieldGroupControl> getFieldGroupControls(
        String id,
        String locale,
        int localeAsIndex,
        UiContext uiContext,
        org.openmdx.ui1.jmi1.FormDefinition formDefinition
    ) {
        List<FieldGroupControl> formFieldGroupControls = new ArrayList<FieldGroupControl>();
        org.openmdx.ui1.cci2.FormFieldGroupDefinitionQuery query = 
            (org.openmdx.ui1.cci2.FormFieldGroupDefinitionQuery)JDOHelper.getPersistenceManager(formDefinition).newQuery(org.openmdx.ui1.jmi1.FormFieldGroupDefinition.class);
        query.orderByOrder().ascending();
        List<org.openmdx.ui1.jmi1.FormFieldGroupDefinition> formFieldGroupDefinitions = formDefinition.getFormFieldGroupDefinition(query);
        int index = 0;
        for(org.openmdx.ui1.jmi1.FormFieldGroupDefinition formFieldGroupDefinition: formFieldGroupDefinitions) {
            formFieldGroupControls.add(
                new FieldGroupControl(
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
    
    //-------------------------------------------------------------------------
    @Override
    public void paint(
        ViewPort p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {
        for(
            int i = 0; 
            i < this.getFieldGroupControl().length; 
            i++
        ) {
            FieldGroupControl fieldGroup = this.getFieldGroupControl()[i];
            fieldGroup.paint(
                p,
                frame,
                forEditing
            );
        }
    }
    
    //-------------------------------------------------------------------------
    public void updateObject(
        Map<String,Object[]> parameterMap,
        Object object,
        ApplicationContext application,
        PersistenceManager pm
    ) {
        for(FieldGroupControl fieldGroupControl: this.fieldGroupControls) {
            Map<String,Attribute> attributesAsMap = new HashMap<String,Attribute>();
            Attribute[][] attributes = fieldGroupControl.getAttribute(object, application);
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
            application.getPortalExtension().updateObject(
                object, 
                parameterMap, 
                attributesAsMap, 
                application
            );
        }
    }
        
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
}

//--- End of File -----------------------------------------------------------
