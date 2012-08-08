/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: RootMenuControl.java,v 1.31 2011/07/07 22:35:42 wfro Exp $
 * Description: RootMenuControl
 * Revision:    $Revision: 1.31 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/07/07 22:35:42 $
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

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.UserSettings;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.action.SelectAndNewObjectAction;
import org.openmdx.portal.servlet.action.SelectObjectAction;
import org.openmdx.portal.servlet.texts.Texts_1_0;
import org.openmdx.portal.servlet.view.ObjectView;

public class RootMenuControl
    extends Control
    implements Serializable {

	//-------------------------------------------------------------------------
    public RootMenuControl(
        String id,
        String locale,
        int localeAsIndex
    ) {
        super(
            id,
            locale,
            localeAsIndex
        );
    }
    
    //-------------------------------------------------------------------------
    public static void paintQuickAccessors(
        ViewPort p
    ) throws ServiceException {
        ObjectView view = (ObjectView)p.getView();        
        String spacer = p.getViewPortType() == ViewPort.Type.MOBILE ? "" : "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
        if(view.getQuickAccessActions().length > 0) {
            for(int i = 0; i < view.getQuickAccessActions().length; i++) {
                Action action = view.getQuickAccessActions()[i];
                if(p.getViewPortType() != ViewPort.Type.MOBILE || action.getParameter(Action.PARAMETER_NAME).length() == 0) {
                	p.write("  <li><a href=\"#\" onmouseover=\"javascript:this.href=", p.getEvalHRef(action), ";onmouseover=function(){};\" title=\"", action.getToolTip(), "\">", spacer, (action.getIconKey() == null ? "" : p.getImg("src=\"", p.getResourcePath("images/"), action.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"\"")), " " ,action.getTitle(), "</a></li>");
                }
            }
        }        
        if(p.getViewPortType() != ViewPort.Type.MOBILE) {
	        ApplicationContext app = p.getApplicationContext();
	        Texts_1_0 texts = app.getTexts();
	        Action showHeaderAction = view.getSetPanelStateAction("Header", 0);
	        Action hideHeaderAction = view.getSetPanelStateAction("Header", 1);
	        Action logoffAction = view.getLogoffAction();
	        Action saveSettingsAction = view.getSaveSettingsAction();
	        String showHeaderTitle = texts.getShowHeaderTitle();
	        String hideHeaderTitle = texts.getHideHeaderTitle();
	        p.write("  <li><a href=\"#\" onclick=\"javascript:new Ajax.Request(", p.getEvalHRef(hideHeaderAction), ", {asynchronous:true});try{$('logoTable').id='logoTableNH';$('content').id='contentNH';}catch(e){};\" title=\"", hideHeaderTitle, "\">", spacer, p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_HEADER_HIDE, "\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"\""), "&nbsp;", hideHeaderTitle, "</a></li>");             
	        p.write("  <li><a href=\"#\" onclick=\"javascript:new Ajax.Request(", p.getEvalHRef(showHeaderAction), ", {asynchronous:true});try{$('logoTableNH').id='logoTable';$('contentNH').id='content';}catch(e){};\" title=\"", showHeaderTitle, "\">", spacer, p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_HEADER_SHOW, "\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"\""), "&nbsp;", showHeaderTitle, "</a></li>"); 
	        p.write("  <li><a href=\"#\" onclick=\"javascript:new Ajax.Request(", p.getEvalHRef(saveSettingsAction), ", {asynchronous:true});\" title=\"", saveSettingsAction.getTitle(), "\">", spacer, p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_SAVE_SELECTED, "\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"\""), "&nbsp;", saveSettingsAction.getTitle(), "</a></li>");             
	        p.write("  <li><a href=\"#\" onclick=\"javascript:window.location.href=", p.getEvalHRef(logoffAction), ";\" title=\"", logoffAction.getTitle(), "\">", spacer, p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_LOGOFF_SELECTED, "\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"\""), "&nbsp;", logoffAction.getTitle(), "</a></li>");
        }
    }
    
    //-------------------------------------------------------------------------
    public static void paintTopNavigation(
        ViewPort p
    ) throws ServiceException {
        ObjectView view = (ObjectView)p.getView();
        ApplicationContext app = view.getApplicationContext();
        Path selectedObjectIdentity = view.getObjectReference().getObject().refGetPath();
        Action[] selectRootObjectAction = view.getSelectRootObjectActions();
        int currentPerspective = app.getCurrentPerspective();
        int lastItemLevel = 0;
        int currentItemLevel = 0;        
        int i = 0;
        int indexItemLevel0 = 0;
        int nItemsLevel0 = 0;
        int topNavigationShowMax = 6;
        boolean topNavigationShowSublevel = false; 
        try {
            topNavigationShowMax = Integer.valueOf(app.getSettings().getProperty(UserSettings.TOP_NAVIGATION_SHOW_MAX, "7")).intValue();
            // Never show sub levels. Use Dashlets instead
            // topNavigationShowSublevel = Boolean.valueOf(app.getSettings().getProperty(UserSettings.TOP_NAVIGATION_SHOW_SUBLEVEL, "true")).booleanValue();
        }
        catch(Exception e) {}
        String stateItemLevel0 = "1";                
        while(i < selectRootObjectAction.length) {
            Action action = selectRootObjectAction[i];
            currentItemLevel = 0;
            if((action.getEvent() == SelectObjectAction.EVENT_ID) && (action.getParameter(Action.PARAMETER_REFERENCE).length() > 0)) {
              currentItemLevel = 1;
            }
            else if(action.getEvent() == SelectAndNewObjectAction.EVENT_ID) {
              currentItemLevel = 2;
            }
            // Get state of root object
            if(currentItemLevel == 0) {
                stateItemLevel0 = app.getSettings().getProperty(
                	UserSettings.ROOT_OBJECT_STATE + (currentPerspective == 0 ? "" : "[" + Integer.toString(currentPerspective) + "]") + "." + indexItemLevel0 + ".State", 
                	"1"
                );
                if("1".equals(stateItemLevel0)) nItemsLevel0++;
                if(nItemsLevel0 > topNavigationShowMax) break;
                indexItemLevel0++;
            }
            // Only show menu entry if state is "1"
            if(action.isEnabled() && "1".equals(stateItemLevel0)) {
                // open levels
                int j = 0;
                while(j < currentItemLevel - lastItemLevel) {
                    if(p.getViewPortType() != ViewPort.Type.MOBILE) {      
                    	if(topNavigationShowSublevel) {
                    		p.write("  <ul>");
                    	}
                    	else {
                    		p.write("  <ul style=\"display:none\">");                    		
                    	}
                    }
                    j++;
                }
                // close levels
                j = 0;
                while(j < lastItemLevel - currentItemLevel) {
                    if(p.getViewPortType() != ViewPort.Type.MOBILE) {                	
	                    p.write("  </ul>");
	                    p.write("</li>");
                    }
                    j++;
                }
                if(currentItemLevel == 1) {
                    if(p.getViewPortType() != ViewPort.Type.MOBILE) {
                    	p.write("    <li><a href=\"#\" onmouseover=\"javascript:this.href=", p.getEvalHRef(action), ";onmouseover=function(){};\">", action.getTitle(), "</a></li>");
                    }
                }
                else {
                    String selectedTag = "";
                    Path currentObjectIdentity = new Path(action.getParameter(Action.PARAMETER_OBJECTXRI));
                    if(selectedObjectIdentity.startsWith(currentObjectIdentity)) {
                        selectedTag = "class=\"selected\"";
                    }
                    if(p.getViewPortType() == ViewPort.Type.MOBILE) {
                    	p.write("<li ", selectedTag, "><a href=\"#\" onmouseover=\"javascript:this.href=", p.getEvalHRef(action), ";onmouseover=function(){};\">", p.getImg("src=\"", p.getResourcePathPrefix(), "images/", action.getIconKey(), "\""), "&nbsp;&nbsp;&nbsp;<span>", action.getTitle(), "</span></a>");                    	
                    }
                    else {
                    	p.write("<li ", selectedTag, "><a href=\"#\" onmouseover=\"javascript:this.href=", p.getEvalHRef(action), ";onmouseover=function(){};\"><span>", p.getImg("src=\"", p.getResourcePathPrefix(), "images/", action.getIconKey(), "\""), "&nbsp;&nbsp;", action.getTitle(), "</span></a>");
                    }
                }
            }
            lastItemLevel = currentItemLevel;
            i++;
        }
        // Close levels
        int j = 0;
        while(j < lastItemLevel) {
            if(p.getViewPortType() != ViewPort.Type.MOBILE) {        	
	            p.write("  </ul>");
	            p.write("</li>");
            }
            j++;
        } 
        if(i < selectRootObjectAction.length) {
            if(p.getViewPortType() != ViewPort.Type.MOBILE) {        	
	            p.write("<li><a href=\"#\" onclick=\"javascript:return false;\"><span>", p.getImg("src=\"", p.getResourcePathPrefix(), "images/spacer.gif\" width=\"0\" height=\"15\" border=\"0\""), "\u00BB</span></a>");
	            p.write("  <ul>");
            }
            while(i < selectRootObjectAction.length) {
                Action action = selectRootObjectAction[i];
                if((action.getEvent() == SelectObjectAction.EVENT_ID) && (action.getParameter(Action.PARAMETER_REFERENCE).length() == 0)) {                
                    stateItemLevel0 = app.getSettings().getProperty(
                    	UserSettings.ROOT_OBJECT_STATE + (currentPerspective == 0 ? "" : "[" + Integer.toString(currentPerspective) + "]") + "." + indexItemLevel0 + ".State", 
                    	"1"
                    );
                    indexItemLevel0++;
                    if(action.isEnabled() && "1".equals(stateItemLevel0)) {    
                    	if(p.getViewPortType() == ViewPort.Type.MOBILE) {
                    		p.write("    <li><a href=\"#\" onmouseover=\"javascript:this.href=", p.getEvalHRef(action), ";onmouseover=function(){};\">", p.getImg("src=\"", p.getResourcePathPrefix(), "images/", action.getIconKey(), "\""), "&nbsp;&nbsp;&nbsp;<span>", action.getTitle(), "</span></a></li>");                    		
                    	}
                    	else {
                    		p.write("    <li><a href=\"#\" onmouseover=\"javascript:this.href=", p.getEvalHRef(action), ";onmouseover=function(){};\">", p.getImg("src=\"", p.getResourcePathPrefix(), "images/", action.getIconKey(), "\""), "&nbsp;&nbsp;", action.getTitle(), "</a></li>");
                    	}
                    }
                }
                i++;
            }
            if(p.getViewPortType() != ViewPort.Type.MOBILE) {            
	            p.write("  </ul>");
	            p.write("</li>");
            }
        }
    }
         
    //-------------------------------------------------------------------------
    public static void paintMenuFlyIn(
    	ViewPort p
    ) throws ServiceException {
    	p.write("<div id=\"menuFlyIn\">");
    	p.write("  <ul id=\"nav\" class=\"nav\" onmouseover=\"sfinit(this);\" >");
    	p.write("    <li><a href=\"#\" onclick=\"javascript:return false;\"><img id=\"rootMenuAnchor\" src=\"./images/flyin.gif\" border=\"0\"/></a>");
    	p.write("      <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
    	RootMenuControl.paintQuickAccessors(p);
    	p.write("      </ul>");
    	p.write("    </li>");
    	p.write("  </ul>");
    	p.write("</div> <!-- menuFlyIn -->");    	
    }
    
}
