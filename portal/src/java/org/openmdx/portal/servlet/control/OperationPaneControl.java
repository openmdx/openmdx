/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: UiBasedOperationPaneControl class
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.attribute.AttributeValueFactory;
import org.openmdx.portal.servlet.view.EditObjectView;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.wizards.WizardDefinition;
import org.openmdx.portal.servlet.wizards.WizardDefinitionFactory;

/**
 * OperationPaneControl
 *
 */
public class OperationPaneControl extends PaneControl implements Serializable {
  
    /**
     * Constructor 
     *
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param pane
     * @param wizardFactory
     * @param valueFactory
     * @param paneIndex
     * @param forClass
     */
    public OperationPaneControl(
        String id,
        String locale,
        int localeAsIndex,
        ControlFactory controlFactory,
        org.openmdx.ui1.jmi1.OperationPane pane,
        WizardDefinitionFactory wizardFactory,    
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
            } else {
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
        this.operationTabControl = (OperationTabControl[])operationTabs.toArray(new OperationTabControl[operationTabs.size()]);        
    }

    /**
     * Get view's autocomplete target.
     * 
     * @param view
     * @return
     */
    public RefObject_1_0 getAutocompleteTarget(
        ObjectView view
    ) {
        return view instanceof EditObjectView
            ? ((EditObjectView)view).isEditMode()
                ? view.getObjectReference().getObject()
                : ((EditObjectView)view).getParent()
            : view.getObjectReference().getObject();
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
    	SysLog.detail("> paint");
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
        SysLog.detail("< paint");
    }

    /**
     * Get operation tag controls.
     * 
     * @return
     */
    public OperationTabControl[] getOperationTabControl(
    ) {
        return this.operationTabControl;
    }
    
    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258126938563294520L;
 
    public static final String FRAME_PARAMETERS = "Parameters";
    public static final String FRAME_RESULTS = "Results";
    
    private OperationTabControl[] operationTabControl;
    
}

//--- End of File -----------------------------------------------------------
