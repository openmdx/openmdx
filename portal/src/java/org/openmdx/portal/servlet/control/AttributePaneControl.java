/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: AttributePaneControl.java,v 1.30 2008/08/12 16:38:06 wfro Exp $
 * Description: TabControl
 * Revision:    $AttributePaneRenderer: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/08/12 16:38:06 $
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

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.view.EditObjectView;
import org.openmdx.portal.servlet.view.ObjectView;

public class AttributePaneControl
    extends PaneControl
    implements Serializable {

    //-------------------------------------------------------------------------
    public AttributePaneControl(
        String id,
        String locale,
        int localeAsIndex,
        ControlFactory controlFactory,
        org.openmdx.ui1.jmi1.AttributePane pane,
        int paneIndex
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            controlFactory,
            pane,
            paneIndex
        );
        AppLog.detail("Preparing attribute tabs");
        List<AttributeTabControl> attributeTab = new ArrayList<AttributeTabControl>();
        for (int i = 0; i < pane.getMember().size(); i++) {
            AppLog.detail("Preparing attribute tab", new Integer(i));
            org.openmdx.ui1.jmi1.Tab tab = (org.openmdx.ui1.jmi1.Tab) pane.getMember().get(i);
            attributeTab.add(
                controlFactory.createAttributeTabControl(
                    null, 
                    locale, 
                    localeAsIndex,
                    tab, 
                    i
                )
            );
        }
        this.attributeTabControl = (AttributeTabControl[]) attributeTab.toArray(new AttributeTabControl[attributeTab.size()]);        
    }
    
    //---------------------------------------------------------------------------------
    @Override
    public void paint(
        HtmlPage p,
        String frame,
        boolean forEditing
    ) throws ServiceException {
        AppLog.detail("> paint");
        
        ApplicationContext app = p.getApplicationContext();
        HtmlEncoder_1_0 htmlEncoder = app.getHtmlEncoder();
        ObjectView view = (ObjectView)p.getView();        
        
        int nActiveTab = 0;
        for(int i = 0; i < this.attributeTabControl.length; i++) {
          AttributeTabControl tab = this.attributeTabControl[i];
          if(tab.getFieldGroupControl().length > 0) {
            nActiveTab++;
          }
        }
        p.setProperty(
            HtmlPage.PROPERTY_N_ACTIVE_TAB,
            new Integer(nActiveTab)
        );
        
        // Select all tabs. Show only if tab size > 0
        if(nActiveTab > 0) {       
            p.write("<div id=\"inspector\">");
            // Generate divs for each tab in case of simple look. Tabs are generated
            // in PageEpilogControl in other looks
            p.write("  <div class=\"inspTabPanel\" style=\"z-index:201;\">");
            for(int i = 0; i < this.attributeTabControl.length; i++) {
                AttributeTabControl tab = this.getAttributeTabControl()[i];                            
                String inspPanelId = view.getContainerElementId() == null
                    ? Integer.toString(i)
                    : view.getContainerElementId() + "-" + Integer.toString(i);                            
                p.write("    <a href=\"#\" class=\"", (i == 0 ? "selected" : "hidden"), "\" ", p.getOnClick("javascript:inspTabSelect(this, 'inspPanel", inspPanelId, "');return false;"), ">", htmlEncoder.encode(tab.getName(), false), "</a>");
            }
            p.write("    <a href=\"#\" onclick=\"javascript:inspTabSelect(this, '');return false;\">*</a>");
            p.write("  </div>");
            p.write("  <div id=\"inspContent\" class=\"inspContent\" style=\"z-index:200;\">");
            for(int i = 0; i < this.attributeTabControl.length; i++) {
                AttributeTabControl tab = this.attributeTabControl[i];
                String inspPanelId = view.getContainerElementId() == null
                    ? Integer.toString(i)
                    : view.getContainerElementId() + "-" + Integer.toString(i);                                            
                p.write("    <div id=\"inspPanel", inspPanelId, "\" class=\"", (i == 0 ? "selected" : "hidden"), "\" style=\"padding-top:10px;\">");
                tab.paint(
                    p,
                    frame,
                    forEditing
                );
                p.write("    </div>");    
            }
            p.write("  </div>");
            p.write("</div>");
            p.write("<div style=\"padding:1px;\"></div>");
            if(view.getContainerElementId() != null && !(view instanceof EditObjectView)) {
                p.write("<div class=\"gridSpacerBottom\"></div>");                        
            }                                                
        }
        AppLog.detail("< paint");
    }
    
    // -------------------------------------------------------------------------
    public AttributeTabControl[] getAttributeTabControl(
    ) {
        return this.attributeTabControl;
    }
    
    //---------------------------------------------------------------------------------
    // Members
    //---------------------------------------------------------------------------------
    private static final long serialVersionUID = -2499549677358494504L;
    
    private final AttributeTabControl[] attributeTabControl;
    
}
