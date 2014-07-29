/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: RootMenuControl
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
import java.util.Collections;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.Texts_1_0;
import org.openmdx.portal.servlet.UserSettings;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.action.SelectObjectAction;
import org.openmdx.portal.servlet.component.ObjectView;

/**
 * RootMenuControl
 *
 */
public class RootMenuControl extends Control implements Serializable {

	/**
     * Constructor 
     *
     * @param id
     * @param locale
     * @param localeAsIndex
     */
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
    
    /**
     * Pain quick accessors.
     * 
     * @param p
     * @throws ServiceException
     */
    public static void paintQuickAccessors(
        ViewPort p
    ) throws ServiceException {
        ObjectView view = (ObjectView)p.getView();        
        String spacer = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
        if(view.getQuickAccessActions().length > 0) {
            for(int i = 0; i < view.getQuickAccessActions().length; i++) {
                Action action = view.getQuickAccessActions()[i];
            	p.write("  <li><a href=\"#\" onmouseover=\"javascript:this.href=", p.getEvalHRef(action), ";onmouseover=function(){};\" title=\"", action.getToolTip(), "\">", spacer, (action.getIconKey() == null ? "" : p.getImg("src=\"", p.getResourcePath("images/"), action.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"\"")), " " ,action.getTitle(), "</a></li>");
            }
        }
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
    
    /**
     * Paint top navigation.
     * 
     * @param p
     * @throws ServiceException
     */
    public static void paintTopNavigation(
        ViewPort p,
        Integer topNavigationShowMax
    ) throws ServiceException {
        ObjectView view = (ObjectView)p.getView();
        ApplicationContext app = view.getApplicationContext();
        Path selectedObjectIdentity = view.getObjectReference().getObject().refGetPath();
        Action[] selectRootObjectAction = view.getSelectRootObjectActions();
        int currentPerspective = app.getCurrentPerspective();
        if(topNavigationShowMax == null) {
	        topNavigationShowMax = 6;
	        try {
	            topNavigationShowMax = Integer.valueOf(app.getSettings().getProperty(UserSettings.TOP_NAVIGATION_SHOW_MAX.getName(), "7")).intValue();
	        } catch(Exception ignore) {}
        }
        String state = "1";                
        int i = 0;
        int itemIndex = 0;
        int count = 0;
        while(i < selectRootObjectAction.length) {
            Action action = selectRootObjectAction[i];
            state = app.getSettings().getProperty(
            	UserSettings.ROOT_OBJECT_STATE.getName() + (currentPerspective == 0 ? "" : "[" + Integer.toString(currentPerspective) + "]") + "." + itemIndex + ".State", 
            	"1"
            );
            if("1".equals(state)) {
            	count++;
            }
            if(count > topNavigationShowMax) { 
            	break;
            }
            itemIndex++;
            // Only show menu entry if state is "1"
            if(action.isEnabled() && "1".equals(state)) {
                String selectedTag = "";
                Path currentObjectIdentity = new Path(action.getParameter(Action.PARAMETER_OBJECTXRI));
                if(selectedObjectIdentity.startsWith(currentObjectIdentity)) {
                    selectedTag = "class=\"" + CssClass.active + "\"";
                }
               	p.write("<li ", selectedTag, "><a href=\"#\" onmouseover=\"javascript:this.href=", p.getEvalHRef(action), ";onmouseover=function(){};\"><span>", action.getTitle(), "</span></a>");
            }
            i++;
        }
        if(i < selectRootObjectAction.length) {
            p.write("<li class=\"", CssClass.dropdown.toString(), "\"><a href=\"#\" class=\"", CssClass.dropdownToggle.toString(), "\" style=\"margin-top:2px;\" data-toggle=\"dropdown\" onclick=\"javascript:this.parentNode.hide=function(){};\"><button class=\"", CssClass.navbarToggle.toString(), "\" style=\"display:block;margin:0px;padding:0px;\"><span class=\"sr-only\">Show menu</span><span class=\"icon-bar\"></span><span class=\"icon-bar\"></span><span class=\"icon-bar\"></span></button></a>");
            p.write("  <ul class=\"", CssClass.dropdownMenu.toString(), "\" role=\"menu\">");
            while(i < selectRootObjectAction.length) {
                Action action = selectRootObjectAction[i];
                if((action.getEvent() == SelectObjectAction.EVENT_ID) && (action.getParameter(Action.PARAMETER_REFERENCE).length() == 0)) {                
                    state = app.getSettings().getProperty(
                    	UserSettings.ROOT_OBJECT_STATE.getName() + (currentPerspective == 0 ? "" : "[" + Integer.toString(currentPerspective) + "]") + "." + itemIndex + ".State", 
                    	"1"
                    );
                    itemIndex++;
                    if(action.isEnabled() && "1".equals(state)) {    
                		p.write("    <li><a href=\"#\" onmouseover=\"javascript:this.href=", p.getEvalHRef(action), ";onmouseover=function(){};\">", action.getTitle(), "</a></li>");
                    }
                }
                i++;
            }
            p.write("  </ul>");
            p.write("</li>");
        }
    }

    /**
     * Paint fly-in menu.
     * 
     * @param p
     * @throws ServiceException
     */
    public static void paintMenuFlyIn(
    	ViewPort p
    ) throws ServiceException {
    	p.write("<div id=\"menuFlyIn\">");
    	p.write("  <ul id=\"", CssClass.ssfNav.toString(), "\" class=\"", CssClass.ssfNav.toString(), "\" onmouseover=\"sfinit(this);\" >");
    	p.write("    <li><a href=\"#\" onclick=\"javascript:return false;\"><img id=\"rootMenuAnchor\" src=\"./images/flyin.gif\" border=\"0\"/></a>");
    	p.write("      <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
    	RootMenuControl.paintQuickAccessors(p);
    	p.write("      </ul>");
    	p.write("    </li>");
    	p.write("  </ul>");
    	p.write("</div> <!-- menuFlyIn -->");    	
    }

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.control.Control#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Control> List<T> getChildren(
		Class<T> type
	) {
		return Collections.emptyList();
	}

	//-----------------------------------------------------------------------
	// Members
	//-----------------------------------------------------------------------
	private static final long serialVersionUID = 421816311561508866L;

}
