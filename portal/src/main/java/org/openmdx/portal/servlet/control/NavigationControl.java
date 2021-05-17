/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: NavigationControl 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2008-2014, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */

package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.openmdx.kernel.exception.Throwables;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.Texts_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.component.ShowObjectView;
import org.openmdx.portal.servlet.component.View;

/**
 * NavigationControl
 *
 */
public class NavigationControl extends Control implements Serializable {

	/**
     * Constructor 
     *
     * @param id
     * @param locale
     * @param localeAsIndex
     */
    public NavigationControl(
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
     * Paint close button.
     * 
     * @param p
     * @param forEditing
     */
    public static void paintClose(
        ViewPort p,
        boolean forEditing
    ) {
        try {
            View view = p.getView();
            if(!forEditing) {
                Action action = ((ShowObjectView)view).getBackAction();
                if(action != null) {
                    p.write("<div id=\"closeButton\" onclick=\"javascript:window.location.href=", p.getEvalHRef(action), ";\">&nbsp;</div>");
                }
            }
        } catch(Exception e) {
            Throwables.log(e);
        }
    }

    /**
     * Paint next button.
     * 
     * @param p
     * @param forEditing
     */
    public static void paintNext(
        ViewPort p,
        boolean forEditing
    ) {
        try {
            View view = p.getView();
            if(!forEditing) {
                Action action = ((ShowObjectView)view).getNextAction();
                if(action != null) {
                    p.write("<div id=\"nextButton\" onclick=\"javascript:window.location.href=", p.getEvalHRef(action), ";\">&nbsp;</div>");
                }
            }
        } catch(Exception e) {
            Throwables.log(e);
        }
    }

    /**
     * Paint prev button.
     * 
     * @param p
     * @param forEditing
     */
    public static void paintPrev(
        ViewPort p,
        boolean forEditing
    ) {
        try {
            View view = p.getView();
            if(!forEditing) {
                Action action = ((ShowObjectView)view).getPrevAction();
                if(action != null) {
                    p.write("<div id=\"prevButton\" onclick=\"javascript:window.location.href=", p.getEvalHRef(action), ";\">&nbsp;</div>");
                }
            }
        } catch(Exception e) {
            Throwables.log(e);
        }
    }

    /**
     * Get header id.
     * 
     * @param p
     * @return
     */
    public static String getHeaderId(
    	ViewPort p
    ) {
    	return "header";
    }
    
    /**
     * Get id of content header.
     * 
     * @param p
     * @return
     */
    public static String getContentClass(
    	ViewPort p
    ) {
    	ApplicationContext app = p.getApplicationContext();
    	return getContentClass(
	    	app.getPanelState("Header") == 0
	    );
    }

    /**
     * Paint header hider button.
     * 
     * @param p
     * @param forEditing
     */
    public static void paintHeaderHider(
        ViewPort p,
        boolean forEditing
    ) {
        try {
            View view = p.getView();
            if(!forEditing) {
            	ApplicationContext app = p.getApplicationContext();
                int panelState = app.getPanelState("Header");
                Action showHeaderAction = view.getSetPanelStateAction("Header", 0);
                Action hideHeaderAction = view.getSetPanelStateAction("Header", 1);
                String showHeaderScript = "try{$('content').className='" + getContentClass(true) + "';}catch(e){};";
                String hideHeaderScript = "try{$('content').className='" + getContentClass(false) + "';}catch(e){};";
                p.write("<div id=\"", (panelState == 0 ? "headerShown" : "headerHidden"), "\" onClick=\"javascript:if(this.id=='headerHidden'){new Ajax.Request(", p.getEvalHRef(showHeaderAction), ", {asynchronous:true});this.id='headerShown';$('panelLogo').className='logoTable';", showHeaderScript, "}else{new Ajax.Request(", p.getEvalHRef(hideHeaderAction), ", {asynchronous:true});this.id='headerHidden';$('panelLogo').className='logoTableNH';", hideHeaderScript, "};\">&nbsp;</div>");
            }
        } catch(Exception e) {
            Throwables.log(e);
        }
    }

    /**
     * Get id of content header.
     * 
     * @param p
     * @return
     */
    public static String getContentClass(
    	boolean headerStateYes
    ) {
    	return
    		"container-fluid " + 
    		"content" + 
    		(headerStateYes ? "HeaderYes" : "HeaderNo");
    }

    /**
     * Paint print button.
     * 
     * @param p
     * @param forEditing
     */
    public static void paintPrint(
        ViewPort p,
        boolean forEditing
    ) {
        try {
            p.write("<div id=\"printButton\" onClick=\"javascript:window.print();\">&nbsp;</div>");
        } catch(Exception e) {
            Throwables.log(e);
        }
    }

    /**
     * Paint perspective selector.
     * 
     * @param p
     * @param forEditing
     */
    public static void paintSelectPerspectives(
        ViewPort p,
        boolean forEditing
    ) {
        try {
            View view = p.getView();
            if(view instanceof ShowObjectView) {
                ShowObjectView showView = (ShowObjectView)view;
                ApplicationContext app = p.getApplicationContext();
                Action[] selectPerspectiveActions = showView.getSelectPerspectiveAction();
                if(selectPerspectiveActions.length > 1) {
                    p.write("<div id=\"perspectiveSelector\">");
                    p.write("  <div class=\"", CssClass.dropdown.toString(), "\">");
                    p.write("    <button class=\"", CssClass.btn.toString(), " ", CssClass.btn_sm.toString(), "\" type=\"button\" data-toggle=\"dropdown\" style=\"white-space:nowrap;\" onclick=\"javascript:this.parentNode.hide=function(){};\">");
                    p.write("      <img src=\"./images/perspective_", Integer.toString(app.getCurrentPerspective()), WebKeys.ICON_TYPE, "\" title=\"", selectPerspectiveActions[app.getCurrentPerspective()].getTitle(), "\"/>");
                    p.write("    </button>");                    
                    p.write("    <div class=\"", CssClass.dropdown_menu.toString(), "\">");
                    for(int i = 0; i < selectPerspectiveActions.length; i++) {
                        Action action = selectPerspectiveActions[i];
                        if(action.isEnabled()) {
                            p.write("      <div class=\"", CssClass.nav_item.toString(), "\">");
                            p.write("        <button class=\"", CssClass.dropdown_item.toString(), " ", CssClass.btn.toString(), " ", CssClass.btn_sm.toString(), " ", CssClass.nav_link.toString(), "\" onclick=\"javascript:window.location.href=", p.getEvalHRef(action), ";\">");
                            p.write("          <img src=\"./images/perspective_", Integer.toString(i), WebKeys.ICON_TYPE, "\" title=\"", action.getTitle(), "\" />&nbsp;", action.getTitle());
                            p.write("        </button>");
                            p.write("      </div>");
                        }
                    }
                    p.write("    </div>");
                    p.write("  </div>");
                    p.write("</div>");
                }
            }
        } catch(Exception e) {
            Throwables.log(e);
        }
    }

    /**
     * Paint navigation breadcrumbs.
     * 
     * @param p
     * @param forEditing
     */
    public static void paintBreadcrumb(
        ViewPort p,
        boolean forEditing
    ) {
        try {
            if(!forEditing) {
            	ApplicationContext app = p.getApplicationContext();
                Texts_1_0 texts = app.getTexts();
                HtmlEncoder_1_0 htmlEncoder = p.getApplicationContext().getHtmlEncoder();
                ShowObjectView view = (ShowObjectView)p.getView();
                if((view.getLookupType() != null) && view.getObjectReference().isInstanceof(view.getLookupType())) {
                    p.write("<span class=\"", CssClass.lookupSelector.toString(), "\"><input type=\"checkbox\" name=\"objselect\" value=\"obj\" onclick=\"OF.selectAndClose('", view.getObjectReference().getXRI(), "', '", htmlEncoder.encode(view.getObjectReference().getTitleAsJavascriptArg(), false), "', '", view.getId(), "', window);\" /></span>");
                }
                p.write("<div id=\"reloadIcon\" onclick=\"javascript:window.location.href=", p.getEvalHRef(view.getObjectReference().getReloadAction()), ";\" title=\"", texts.getReloadText(), "\">", p.getImg("src=\"", p.getResourcePath("images/"), view.getObjectReference().getIconKey(), "\" border=\"0\" align=\"absbottom\" alt=\"o\" title=\"\""), "</div>");                	
                Action[] selectParentActions = view.getSelectParentAction();
                p.write("<ol class=\"", CssClass.breadcrumb.toString(), "\" style=\"margin-bottom:0px;\">");
                for(
                    int i = 0; 
                    i < selectParentActions.length; 
                    i++
                ) {
                    Action selectParentAction = selectParentActions[i];
                    String title = null;
                    if(selectParentAction != null) {
                        if(i == selectParentActions.length - 1) {
                            String strippedTitle = selectParentAction.getTitle();
                            strippedTitle = strippedTitle.replaceAll("<br />", "\u00b6");
                            if((i > 1) && (strippedTitle.length() > 45)) {
                                strippedTitle = strippedTitle.substring(0, 45) + "...";
                            }
                            title = view.getObjectReference().getObject() instanceof org.openmdx.base.jmi1.Segment 
                            	? strippedTitle 
                            	: strippedTitle + " - " + view.getObjectReference().getLabel();
                            p.write("<li class=\"", CssClass.breadcrumb_item.toString(), " ", CssClass.active.toString(), "\">");
                        } else {
                        	title = selectParentAction.getTitle();
                            p.write("<li class=\"", CssClass.breadcrumb_item.toString(), "\">");
                        }
                        p.write("<a href=\"#\" onmouseover=\"javascript:this.href='./' + ", p.getEvalHRef(selectParentAction), ";onmouseover=function(){};\">", htmlEncoder.encode(title, false), "</a>");
                        p.write("</li>");
                    }
                }
                p.write("</ol>");
            }
        } catch(Exception e) {
            Throwables.log(e);
        }       
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
	private static final long serialVersionUID = 5154342686199609946L;

}
