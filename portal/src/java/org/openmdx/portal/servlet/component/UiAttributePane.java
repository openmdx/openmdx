/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Description: CompositeGrid
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
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
package org.openmdx.portal.servlet.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.control.AttributePaneControl;
import org.openmdx.portal.servlet.control.UiAttributeTabControl;

/**
 * AttributePane
 *
 */
public class UiAttributePane extends AttributePane implements Serializable {
    
    /**
     * Constructor.
     * 
     * @param control
     * @param view
     * @param object
     */
    public UiAttributePane(
        AttributePaneControl control,
        ObjectView view,
        Object object
    ) {
        super(
            control,
            view,
            object
        );
    	List<UiAttributeTab> attributeTabs = new ArrayList<UiAttributeTab>();
	    for(UiAttributeTabControl tabControl: control.getChildren(UiAttributeTabControl.class)) {
	    	attributeTabs.add(
	        	tabControl.newComponent(
	                view,
	                object
	            )
	        );
	    }
        this.attributeTabs = attributeTabs;
    }

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.view.Component#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Component> List<T> getChildren(
		Class<T> type
	) {
		if(type == UiAttributeTab.class) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)this.attributeTabs;
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
        for(UiAttributeTab tab: this.attributeTabs) {
            tab.refresh(refreshData);
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
    	ApplicationContext app = p.getApplicationContext();
    	HtmlEncoder_1_0 htmlEncoder = app.getHtmlEncoder();
    	ObjectView view = (ObjectView)p.getView();
    	List<UiAttributeTab> attributeTabs = this.getChildren(UiAttributeTab.class);
    	int nActiveTab = 0;
    	for(UiAttributeTab tab: attributeTabs) {
    		if(!tab.getChildren(UiFieldGroup.class).isEmpty()) {
    			nActiveTab++;
    		}
    	}
    	p.setProperty(
			ViewPort.PROPERTY_N_ACTIVE_TAB,
			new Integer(nActiveTab)
		);        
    	// Select all tabs. Show only if tab size > 0
    	if(nActiveTab > 0) {   
			p.write("<div id=\"inspector\">");
			p.write("  <ul class=\"", CssClass.nav.toString(), " ", CssClass.nav_tabs.toString(), " ", CssClass.nav_condensed.toString(), "\" style=\"z-index:201;\">");
			int index = 0;
			String inspPanelIdPrefix = view.getContainerElementId() == null 
				? "inspPanel"
				: "inspPanel" + view.getContainerElementId() + "-";
			for(UiAttributeTab tab: attributeTabs) {
				boolean isRevokeShow = tab.hasPermission(
					view.getObject(), 
					app, 
					WebKeys.PERMISSION_REVOKE_SHOW
				);
				if(!isRevokeShow) {
					String inspPanelId = inspPanelIdPrefix + Integer.toString(index);                            
					p.write("    <li class=\"", CssClass.nav_item.toString(), "\" onclick=\"javascript:activateTab(this, '#", inspPanelId, "');\"><a class=\"", CssClass.nav_link.toString(), " ", (index == 0 ? CssClass.active.toString() : ""), "\" href=\"#!\">", htmlEncoder.encode(tab.getName(), false), "</a></li>");
					index++;
				}
			}
			p.write("    <li class=\"", CssClass.nav_item.toString(), "\" onclick=\"javascript:activateTabs(this, '#", inspPanelIdPrefix, "')\"><a class=\"", CssClass.nav_link.toString(), "\" href=\"#!\">*</a></li>");
			p.write("  </ul>");
			p.write("  <div id=\"inspContent\" class=\"", CssClass.inspContent.toString(), " ", CssClass.tab_content.toString(), "\" style=\"display:block;z-index:200;\">");
			index = 0;
			for(UiAttributeTab tab: attributeTabs) {
				boolean isRevokeShow = tab.hasPermission(
					view.getObject(), 
					app, 
					WebKeys.PERMISSION_REVOKE_SHOW
				);
				if(!isRevokeShow) {
					String inspPanelId = inspPanelIdPrefix + Integer.toString(index);                                            
					p.write("    <div id=\"", inspPanelId, "\" class=\"", CssClass.tab_pane.toString(), (index == 0 ? " " + CssClass.active : ""), "\">");
					tab.paint(
						p,
						frame,
						forEditing
					);
					p.write("    </div>");
					index++;
				}
			}
			p.write("  </div>");
			p.write("</div>");
			p.write("<div style=\"padding:1px;\"></div>");
			if(
				(view.getContainerElementId() != null) && 
				!(view instanceof EditObjectView)
			) {
				p.write("<div class=\"", CssClass.gridSpacerBottom.toString(), "\"></div>");                        
			}
    	}
    	SysLog.detail("< paint");
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = -447974858493592293L;

    protected final List<UiAttributeTab> attributeTabs;

}
