/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: OperationPaneControl.java,v 1.82 2009/09/25 12:02:37 wfro Exp $
 * Description: UiBasedOperationPaneControl class
 * Revision:    $Revision: 1.82 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/25 12:02:37 $
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
package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.ObjectReference;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.attribute.AttributeValue;
import org.openmdx.portal.servlet.attribute.AttributeValueFactory;
import org.openmdx.portal.servlet.attribute.TextValue;
import org.openmdx.portal.servlet.reports.ReportDefinition;
import org.openmdx.portal.servlet.reports.ReportDefinitionFactory;
import org.openmdx.portal.servlet.texts.Texts_1_0;
import org.openmdx.portal.servlet.view.EditObjectView;
import org.openmdx.portal.servlet.view.FieldGroup;
import org.openmdx.portal.servlet.view.ObjectCreationResult;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.OperationTab;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.wizards.WizardDefinition;
import org.openmdx.portal.servlet.wizards.WizardDefinitionFactory;

//-----------------------------------------------------------------------------
public class OperationPaneControl
    extends PaneControl
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public OperationPaneControl(
        String id,
        String locale,
        int localeAsIndex,
        ControlFactory controlFactory,
        org.openmdx.ui1.jmi1.OperationPane pane,
        WizardDefinitionFactory wizardFactory,
        ReportDefinitionFactory reportFactory,        
        AttributeValueFactory valueFactory,
        int paneIndex,
        String forClass
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            pane,
            paneIndex
        );
        int tabIndex = 0;
        // Operations
        List<OperationTabControl> operationTabs = new ArrayList<OperationTabControl>();
        for(
            int i = 0; 
            i < pane.getMember().size(); 
            i++
        ) {
            org.openmdx.ui1.jmi1.OperationTab tab = (org.openmdx.ui1.jmi1.OperationTab)pane.getMember().get(i);
            // Lookup a wizard which implements the operation
            WizardDefinition[] wizardDefinitions = wizardFactory.findWizardDefinitions(
                forClass, 
                locale, 
                tab.getOperationName().indexOf("?") > 0 ? 
                    tab.getOperationName().substring(0, tab.getOperationName().indexOf("?")) :
                    tab.getOperationName()
            );
            // Wizard implements operation
            if(wizardDefinitions.length > 0) {
                operationTabs.add(
                    controlFactory.createWizardTabControl(
                        null,
                        locale,
                        localeAsIndex,
                        tab,
                        (WizardDefinition)wizardDefinitions[0],
                        paneIndex,
                        tabIndex++               
                    )
                );                
            }
            else {
                // Lookup a report which implements the operation
                ReportDefinition[] reportDefinitions = reportFactory.findReportDefinitions(
                    forClass,
                    locale,
                    tab.getOperationName()
                );
                // Report implements operation
                if(reportDefinitions.length > 0) {
                    operationTabs.add(
                        controlFactory.createReportTabControl(
                            null,
                            locale,
                            localeAsIndex,
                            (ReportDefinition)reportDefinitions[0],
                            paneIndex,
                            tabIndex++               
                        )
                    );                    
                }
                // Provider implements operation
                else {                    
                    operationTabs.add(
                        controlFactory.createOperationTabControl(
                            null,
                            locale,
                            localeAsIndex,
                            tab,
                            paneIndex,
                            tabIndex++
                        )
                    );
                }
            }
        }   
        this.operationTabControl = (OperationTabControl[])operationTabs.toArray(new OperationTabControl[operationTabs.size()]);        
    }

    //-------------------------------------------------------------------------
    public RefObject_1_0 getAutocompleteTarget(
        ObjectView view
    ) {
        return view instanceof EditObjectView
            ? ((EditObjectView)view).isEditMode()
                ? view.getObjectReference().getObject()
                : ((EditObjectView)view).getParent()
            : view.getObjectReference().getObject();
    }
    
    //-------------------------------------------------------------------------
    @Override
    public void paint(
        ViewPort p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {
    	SysLog.detail("> paint");

        ShowObjectView view = (ShowObjectView)p.getView();
        ApplicationContext app = view.getApplicationContext();
        Texts_1_0 texts = app.getTexts();

        // Operation menues
        if(frame == null) {
            p.write("<li><a href=\"#\" onclick=\"javascript:return false;\">", this.getToolTip(), "&nbsp;&nbsp;&nbsp;</a>");
            p.write("  <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
            for(int j = 0; j < this.getOperationTabControl().length; j++) {
                this.getOperationTabControl()[j].paint(
                    p, 
                    frame,
                    forEditing
                );
            }
            p.write("  </ul>");
            p.write("</li>");
        }
        // Operation parameters
        else if(FRAME_PARAMETERS.equals(frame)) {
            for(int j = 0; j < this.getOperationTabControl().length; j++) {
                this.getOperationTabControl()[j].paint(
                    p, 
                    frame,
                    forEditing
                );
            }            
        }
        // Operation results
        else if(FRAME_RESULTS.equals(frame)) {
            // Operation result (if any) and result has fields
            OperationTab operationTabResult = view.resetOperationTabResult();
            if(operationTabResult != null) {
                FieldGroup fieldGroup = operationTabResult.getFieldGroup()[1];
                Attribute[][] attributes = fieldGroup.getAttribute();
                int nCols = attributes.length;
                int nRows = nCols > 0 ? attributes[0].length : 0;
                if((nCols > 0) && (nRows > 0)) {
                    p.write("<div class=\"panelResult\" style=\"display: block;\">");
                    p.write("  <table class=\"opFieldGroup\">");
                    for(int v = 0; v < nRows; v++) {
                        p.write("<tr>");
                        for(int u = 0; u < nCols; u++) {
                            Attribute attribute = attributes[u][v];
                            if(attribute == null) continue;
                            if(attribute.isEmpty()) {
                                p.write("<td class=\"label\"></td>");
                            }                                
                            else {
                                String label = attribute.getLabel();
                                AttributeValue valueHolder = attribute.getValue();
                                Object value = valueHolder.getValue(false);
                                String stringifiedValue = attribute.getStringifiedValue(
                                    p, 
                                    false, 
                                    false
                                );
                                stringifiedValue = valueHolder instanceof TextValue
                                ? ((TextValue)valueHolder).isPassword() ? "*****" : stringifiedValue
                                    : stringifiedValue;
                                String widthModifier = "";                                    
                                String readonlyModifier = valueHolder.isChangeable() ? "" : "readonly";
                                String disabledModifier = valueHolder.isChangeable() ? "" : "disabled";                        
                                String lockedModifier = valueHolder.isChangeable() ? "" : "Locked";
                                String styleModifier = "style=\"";
                                // ObjectReference                          
                                if(value instanceof ObjectReference) {
                                    Action selectAction = ((ObjectReference)value).getSelectObjectAction();
                                    Action selectAndEditAction = ((ObjectReference)value).getSelectAndEditObjectAction();
                                    p.write("<td class=\"label\"><span class=\"nw\">", label, ":</span></td>");
                                    p.write("<td class=\"valueL\" ", widthModifier, "><div class=\"field\" title=\"", selectAction.getToolTip(), "\"><a href=\"\" onmouseover=\"javascript:this.href=", p.getEvalHRef(selectAction), ";onmouseover=function(){};\">", selectAction.getTitle(), "</a> [<a href=\"\" onclick=\"javascript:this.href=", p.getEvalHRef(selectAndEditAction), ";\">", texts.getEditTitle(), "</a>]</div></td>");
                                }
                                // other types
                                else {
                                    valueHolder.paint(
                                        attribute,
                                        p,
                                        null, // default id
                                        null, // default label
                                        view.getLookupObject(),
                                        nCols,
                                        0,
                                        "",
                                        styleModifier,
                                        widthModifier,
                                        "",
                                        readonlyModifier,
                                        disabledModifier,
                                        lockedModifier,
                                        stringifiedValue,
                                        forEditing
                                    );
                                }
                            }
                        }
                        p.write("</tr>");
                    }
                    p.write("  </table>");
                    p.write("</div>");
                }
            }

            // show reference of newly created object as operation result
            ObjectCreationResult objectCreationResult = view.resetObjectCreationResult();
            if(objectCreationResult != null) {
                Action selectAction = objectCreationResult.getSelectObjectAction();
                Action editAction = objectCreationResult.getEditObjectAction();        
                p.write("<div class=\"panelResult\" style=\"display: block;\">");
                p.write("  <table class=\"opFieldGroup\">");
                p.write("    <td class=\"label\"><span class=\"nw\">", selectAction.getTitle(), ":</span></td>");
                p.write("    <td class=\"valueL\"><div class=\"field\" title=\"", selectAction.getToolTip(), "\"><a href=\"\" onmouseover=\"javascript:this.href=", p.getEvalHRef(selectAction), ";onmouseover=function(){};\">", selectAction.getToolTip(), "</a> [<a href=\"\" onclick=\"javascript:this.href=", p.getEvalHRef(editAction), ";\">", texts.getEditTitle(), "</a>]</div></td>");
                p.write("  </table>");
                p.write("</div>");
            }
        }

        SysLog.detail("< paint");

    }

    //-------------------------------------------------------------------------
    public OperationTabControl[] getOperationTabControl(
    ) {
        return this.operationTabControl;
    }
    
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258126938563294520L;
 
    public static final String FRAME_PARAMETERS = "Parameters";
    public static final String FRAME_RESULTS = "Results";
    
    private OperationTabControl[] operationTabControl;
    
}

//--- End of File -----------------------------------------------------------
