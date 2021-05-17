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
import java.util.Collections;
import java.util.List;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.portal.servlet.PortalExtension_1_0;
import org.openmdx.portal.servlet.component.EditObjectView;
import org.openmdx.portal.servlet.component.ObjectView;
import org.openmdx.portal.servlet.component.UiOperationPane;
import org.openmdx.portal.servlet.wizards.WizardDefinition;
import org.openmdx.portal.servlet.wizards.WizardDefinitionFactory;

/**
 * OperationPaneControl
 *
 */
public class UiOperationPaneControl extends OperationPaneControl implements Serializable {
  
    /**
     * Constructor 
     *
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param paneDef
     * @param wizardFactory
     * @param valueFactory
     * @param paneIndex
     * @param forClass
     */
    public UiOperationPaneControl(
        String id,
        String locale,
        int localeAsIndex,
        PortalExtension_1_0.ControlFactory controlFactory,
        org.openmdx.ui1.jmi1.OperationPane paneDef,
        WizardDefinitionFactory wizardFactory,    
        int paneIndex,
        String forClass
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            controlFactory,
            paneIndex,
            forClass
        );
        int tabIndex = 0;
        this.paneDef = paneDef;
        // Operations
        List<UiOperationTabControl> children = new ArrayList<UiOperationTabControl>();
        List<org.openmdx.ui1.jmi1.OperationTab> tabs = paneDef.getMember();
        for(org.openmdx.ui1.jmi1.OperationTab tab: tabs) {
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
                children.add(
                    this.newWizardTabControl(
                        this.uuidAsString(),
                        locale,
                        localeAsIndex,
                        tab,
                        controlFactory,
                        (WizardDefinition)wizardDefinitions[0],
                        paneIndex,
                        tabIndex++               
                    )
                );                
            } else {
                children.add(
                    this.newOperationTabControl(
                        this.uuidAsString(),
                        locale,
                        localeAsIndex,
                        controlFactory,
                        tab,
                        paneIndex,
                        tabIndex++
                    )
                );
            }
        }   
        this.operationTabControls = children;        
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.control.OperationPaneControl#newComponent(org.openmdx.portal.servlet.component.ObjectView)
     */
    @Override
    public UiOperationPane newComponent(
    	ObjectView view
    ) {
    	return new UiOperationPane(
    		this,
            view
        );
    }

    /**
     * Create new instance of WizardTabControl. Override for
     * custom-specific implementation.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param tabDef
     * @param controlFactory
     * @param wizardDef
     * @param paneIndex
     * @param tabIndex
     * @return
     */
    protected UiWizardTabControl newWizardTabControl(
        String id,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.OperationTab tabDef,
        PortalExtension_1_0.ControlFactory controlFactory,
        WizardDefinition wizardDef,
        int paneIndex,
        int tabIndex	    		
    ) {
    	return new UiWizardTabControl(
    		id,
    		locale,
    		localeAsIndex,
    		tabDef,
    		controlFactory,
    		wizardDef,
    		paneIndex,
    		tabIndex
    	);
    }
        
    /**
     * Create new instance of OperationTabControl. Override for
     * custom-specific implementation.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param tabDef
     * @param paneIndex
     * @param tabIndex
     * @return
     */
    protected UiOperationTabControl newOperationTabControl(
		String id,
		String locale,
		int localeAsIndex,
		PortalExtension_1_0.ControlFactory controlFactory,
		org.openmdx.ui1.jmi1.OperationTab tabDef,
		int paneIndex,
		int tabIndex	    		
    ) {
    	return new UiOperationTabControl(
    		id,
    		locale,
    		localeAsIndex,
    		controlFactory,
    		tabDef,
    		paneIndex,
    		tabIndex
    	);
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
	 * @see org.openmdx.portal.servlet.control.Control#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Control> List<T> getChildren(
		Class<T> type
	) {
		if(UiOperationTabControl.class.isAssignableFrom(type)) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)this.operationTabControls;
			return children;			
		} else {
			return Collections.emptyList();
		}
	}

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.control.PaneControl#getToolTip()
	 */
	@Override
	public String getToolTip(
	) {
        org.openmdx.ui1.jmi1.Pane pane = this.paneDef;
        return this.localeAsIndex < pane.getToolTip().size()
            ? pane.getToolTip().get(this.localeAsIndex)
            : !pane.getToolTip().isEmpty() ? pane.getToolTip().get(0) : "N/A";
	}

    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258126938563294520L;
 
    public static final String FRAME_PARAMETERS = "Parameters";
    public static final String FRAME_RESULTS = "Results";

    private final org.openmdx.ui1.jmi1.OperationPane paneDef;    
    private List<UiOperationTabControl> operationTabControls;

}

//--- End of File -----------------------------------------------------------
