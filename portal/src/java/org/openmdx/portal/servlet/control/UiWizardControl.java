/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: WizardControl
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
package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.PortalExtension_1_0;
import org.openmdx.portal.servlet.Texts_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.wizards.WizardDefinition;

/**
 * WizardControl
 *
 */
public class UiWizardControl extends WizardControl implements Serializable {
  
    /**
     * Constructor.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param wizardDefinitions
     */
    public UiWizardControl(
        String id,
        String locale,
        int localeAsIndex,
        PortalExtension_1_0.ControlFactory controlFactory,
        WizardDefinition[] wizardDefinitions
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            controlFactory,
            wizardDefinitions
        );
        List<UiWizardTabControl> children = new ArrayList<UiWizardTabControl>();
        for(int i = 0; i < wizardDefinitions.length; i++) {
        	children.add(
        		this.newUiWizardTabControl(
	                null,
	                locale,
	                localeAsIndex,
	                controlFactory,
	                wizardDefinitions[i],
	                WIZARD_PANE_INDEX,
	                i
	            )
            );
        }
        this.wizardTabControls = children;
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
    protected UiWizardTabControl newUiWizardTabControl(
        String id,
        String locale,
        int localeAsIndex,
        PortalExtension_1_0.ControlFactory controlFactory,
        WizardDefinition wizardDef,
        int paneIndex,
        int tabIndex	    		
    ) {
    	return new UiWizardTabControl(
    		id,
    		locale,
    		localeAsIndex,
    		null,
    		controlFactory,
    		wizardDef,
    		paneIndex,
    		tabIndex
    	);
    }

    /**
     * Get action for invoking wizard.
     * 
     * @param objectXRI
     * @return
     */
    public Action getInvokeWizardAction(
        String objectXRI
    ) {
        Action action = 
            new Action(
                Action.EVENT_INVOKE_WIZARD, 
                new Action.Parameter[]{
                    new Action.Parameter(Action.PARAMETER_OBJECTXRI, objectXRI)
                },
                "",
                true
            );
        return action;
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
    	Texts_1_0 texts = p.getApplicationContext().getTexts();
        // Wizard menu entries
        if(frame == null) {
        	String id = this.getId();
        	List<UiWizardTabControl> children = this.getChildren(UiWizardTabControl.class);          
            p.write("<li id=\"", id, "-dropdown\" class=\"", CssClass.dropdown.toString(), "\"><a href=\"#\" class=\"", CssClass.dropdownToggle.toString(), "\" data-toggle=\"dropdown\" onclick=\"javascript:this.parentNode.hide=function(){};\">", texts.getWizardsMenuTitle(), "</a>");
            p.write("  <ul id=\"", id, "-menu\" class=\"", CssClass.dropdownMenu.toString(), "\" style=\"z-index:1010;\">");
            for(UiWizardTabControl tab: children) {
                tab.paint(
                    p,
                    frame,
                    forEditing
               );
            }
            p.write("  </ul>");
            p.write("</li>");
            p.write("<script language=\"javascript\" type=\"text/javascript\">");
            p.write("  if($('", id, "-menu').innerHTML.trim()==''){$('", id, "-dropdown').style.display='none'};");
            p.write("</script>");            
        }
        SysLog.detail("< paint");        
    }

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.control.Control#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Control> List<T> getChildren(
		Class<T> type
	) {
		if(UiWizardTabControl.class.isAssignableFrom(type)) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)this.wizardTabControls;
			return children;
		} else {
			return Collections.emptyList();
		}	
	}
    
    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = -7785589508766566304L;
    private static final int WIZARD_PANE_INDEX = 3000;
    
    private final List<UiWizardTabControl> wizardTabControls;
}

//--- End of File -----------------------------------------------------------
