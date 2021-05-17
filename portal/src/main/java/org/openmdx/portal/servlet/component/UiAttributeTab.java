/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Description: AttributeTab
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
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
import java.util.Collections;
import java.util.List;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.control.UiAttributeTabControl;
import org.openmdx.portal.servlet.control.UiFieldGroupControl;

/**
 * AttributeTab
 *
 */
public class UiAttributeTab extends Component implements Serializable {
    
    /**
     * Constructor 
     *
     * @param control
     * @param view
     * @param attributePane
     * @param object
     */
    public UiAttributeTab(
        UiAttributeTabControl control,
        ObjectView view,
        Object object
    ) {
        super(
            control,
            view
        );
        List<UiFieldGroup> fieldGroups = new ArrayList<UiFieldGroup>();
	    for(UiFieldGroupControl fieldGroupControl: control.getChildren(UiFieldGroupControl.class)) {
	    	fieldGroups.add(
	    		fieldGroupControl.newComponent(
	                view,
	                object
	            )
	        );
	    }
        this.fieldGroups = fieldGroups;
    }

    /**
     * Get name for this tab.
     * 
     * @return
     */
    public String getName(
    ) {
    	return ((UiAttributeTabControl)this.control).getName();
    }

    /**
     * Test permission for this tab.
     * 
     * @param view
     * @param app
     * @param action
     * @return
     */
    protected boolean hasPermission(
    	RefObject_1_0 object,
    	ApplicationContext app,
    	String action
    ) {
    	return app.getPortalExtension().hasPermission(
			this.control, 
			object, 
			app,
			action
		);
    }

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.view.Component#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Component> List<T> getChildren(
		Class<T> type
	) {
		if(type == UiFieldGroup.class) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)this.fieldGroups;
			return children;
		} else {
			return Collections.emptyList();
		}
	}

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.ControlState#refresh(boolean)
     */
    @Override
    public void refresh(
        boolean refreshData
    ) throws ServiceException {
        for(UiFieldGroup fieldGroup: this.getChildren(UiFieldGroup.class)) {
            fieldGroup.refresh(refreshData);
        }
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
        RefObject_1_0 refObj = p.getView() instanceof ObjectView ?
            ((ObjectView)p.getView()).getObject() :
            null;
        ApplicationContext app = p.getApplicationContext();
        for(UiFieldGroup fieldGroup: this.getChildren(UiFieldGroup.class)) {
            boolean isRevokeShow = fieldGroup.hasPermission(
            	refObj, 
            	app, 
            	WebKeys.PERMISSION_REVOKE_SHOW
            );
            if(!isRevokeShow) {
                fieldGroup.paint(
                    p,
                    frame,
                    forEditing
                );
            }
        }
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------    
    private static final long serialVersionUID = -7375080538351577732L;

    protected final List<UiFieldGroup> fieldGroups;

}
