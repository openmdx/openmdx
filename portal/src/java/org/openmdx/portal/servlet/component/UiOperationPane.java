/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Description: UiOperationPane
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
import java.util.HashMap;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.control.OperationPaneControl;
import org.openmdx.portal.servlet.control.UiOperationTabControl;

/**
 * UiOperationPane
 *
 */
public class UiOperationPane extends OperationPane implements Serializable {
    
	/**
     * Constructor.
     * 
     * @param control
     * @param view
     */
    public UiOperationPane(
        OperationPaneControl control,
        ObjectView view
    ) {
        super(
            control,
            view
        );
        List<UiOperationTab> operationTabs = new ArrayList<UiOperationTab>();
		for(UiOperationTabControl operationTabControl: control.getChildren(UiOperationTabControl.class)) {
			operationTabs.add(
	            operationTabControl.newComponent(
	                view,
	                new HashMap<String,Object>()
				)
            );
		}
        this.operationTabs = operationTabs;        
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.component.OperationPane#getToolTip()
     */
    @Override
    public String getToolTip(
    ) {
    	OperationPaneControl control = (OperationPaneControl)this.control;
    	return control.getToolTip();
    }
    
	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.view.Component#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Component> List<T> getChildren(
		Class<T> type
	) {
		if(type == UiOperationTab.class) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)this.operationTabs;
			return children;
		} else {
			return Collections.emptyList();
		}
	}

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.Canvas#refresh(boolean)
     */
    @Override
    public void refresh(
        boolean refreshData
    ) throws ServiceException {
    	for(UiOperationTab operationTab: this.getChildren(UiOperationTab.class)) {
    		operationTab.refresh(refreshData);
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
    	SysLog.detail("> paint");
        if(frame == null) {
            String toolTip = this.getToolTip();
            p.write("<li class=\"", CssClass.dropdown.toString(), "\"><a href=\"#\" class=\"", CssClass.dropdownToggle.toString(), "\" data-toggle=\"dropdown\" onclick=\"javascript:this.parentNode.hide=function(){};\">", toolTip, "</a>");
            p.write("  <ul class=\"", CssClass.dropdownMenu.toString(), "\" role=\"menu\" style=\"z-index:1010;\">");
            for(UiOperationTab tab: this.getChildren(UiOperationTab.class)) {
                tab.paint(
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

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
	private static final long serialVersionUID = -2254622323295094874L;

    protected final List<UiOperationTab> operationTabs;

}
