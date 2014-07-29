/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: UiAttributePaneControl
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

import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.PortalExtension_1_0;
import org.openmdx.portal.servlet.component.ObjectView;
import org.openmdx.portal.servlet.component.UiAttributePane;

/**
 * UiAttributePaneControl
 *
 */
public class UiAttributePaneControl extends AttributePaneControl implements Serializable {

    /**
     * Constructor.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param paneDef
     * @param paneIndex
     */
    public UiAttributePaneControl(
        String id,
        String locale,
        int localeAsIndex,
        PortalExtension_1_0.ControlFactory controlFactory,
        org.openmdx.ui1.jmi1.AttributePane paneDef,
        int paneIndex
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            paneIndex
        );
        SysLog.detail("Preparing attribute tabs");
        List<UiAttributeTabControl> attributeTabControls = new ArrayList<UiAttributeTabControl>();
        for(int i = 0; i < paneDef.getMember().size(); i++) {
        	SysLog.detail("Preparing attribute tab", new Integer(i));
            org.openmdx.ui1.jmi1.Tab tab = (org.openmdx.ui1.jmi1.Tab) paneDef.getMember().get(i);
            attributeTabControls.add(
                this.newUiAttributeTabControl(
                    tab.refGetPath().getBase(), 
                    locale, 
                    localeAsIndex,
                    controlFactory,
                    tab, 
                    i
                )
            );
        }
        this.attributeTabControls = attributeTabControls;        
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.control.AttributePaneControl#newComponent(org.openmdx.portal.servlet.component.ObjectView, java.lang.Object)
     */
    @Override
    public UiAttributePane newComponent(
    	ObjectView view,
    	Object object
    ) {
		return new UiAttributePane(
			this,
            view,
            object
        );    	
    }

    /**
     * Create new instance of AttributeTabControl. Override for
     * custom-specific implementation.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param tabDef
     * @param tabIndex
     * @return
     */
    protected UiAttributeTabControl newUiAttributeTabControl(
		String id,
		String locale,
		int localeAsIndex,
		PortalExtension_1_0.ControlFactory controlFactory,
		org.openmdx.ui1.jmi1.Tab tabDef,
		int tabIndex	    		
    ) {
    	return new UiAttributeTabControl(
    		id,
    		locale,
    		localeAsIndex,
    		controlFactory,
    		tabDef,
    		tabIndex
    	);
    }
  
	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.control.Control#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Control> List<T> getChildren(
		Class<T> type
	) {
		if(UiAttributeTabControl.class.isAssignableFrom(type)) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)this.attributeTabControls;
			return children;
		} else {
			return Collections.emptyList();
		}		
	}
	
	//---------------------------------------------------------------------------------
    // Members
    //---------------------------------------------------------------------------------
    private static final long serialVersionUID = -2499549677358494504L;

    protected final List<UiAttributeTabControl> attributeTabControls;
    
}
