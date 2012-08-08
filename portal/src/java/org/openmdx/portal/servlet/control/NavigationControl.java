/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: NavigationControl.java,v 1.9 2008/08/25 11:12:12 wfro Exp $
 * Description: NavigationControl 
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/08/25 11:12:12 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.texts.Texts_1_0;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.view.View;

public class NavigationControl 
    extends Control
    implements Serializable {

    //-------------------------------------------------------------------------
    public NavigationControl(
        String id,
        String locale,
        int localeAsIndex,
        ControlFactory controlFactory
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            controlFactory
        );
    }
        
    //-------------------------------------------------------------------------    
    public static void paintClose(
        HtmlPage p,
        boolean forEditing
    ) {
        try {
            View view = p.getView();
            if(!forEditing) {
                Action backAction = ((ShowObjectView)view).getBackAction();
                if(backAction != null) {
                    p.write("<div id=\"closeButton\" ", p.getOnClick("javascript:window.location.href=", p.getEvalHRef(backAction), ";") + ">&nbsp;</div>");
                }
            }
        }
        catch(Exception e) {
            new ServiceException(e).log();
        }
    }
        
    //-------------------------------------------------------------------------    
    public static void paintHeaderHider(
        HtmlPage p,
        boolean forEditing
    ) {
        try {
            View view = p.getView();
            if(!forEditing) {
                int panelState = p.getApplicationContext().getPanelState("Header");
                String id = panelState == 0 ?
                    "headerShown" :
                    "headerHidden";
                Action showHeaderAction = view.getSetPanelStateAction("Header", 0);
                Action hideHeaderAction = view.getSetPanelStateAction("Header", 1);
                p.write("<div id=\"", id, "\" onClick=\"javascript:if(this.id=='headerHidden'){new Ajax.Request(", p.getEvalHRef(showHeaderAction), ", {asynchronous:true});this.id='headerShown';$('logoTableNH').id='logoTable';$('contentNH').id='content';}else{new Ajax.Request(", p.getEvalHRef(hideHeaderAction), ", {asynchronous:true});this.id='headerHidden';$('logoTable').id='logoTableNH';$('content').id='contentNH';};\">&nbsp;</div>");
            }
        }
        catch(Exception e) {
            new ServiceException(e).log();
        }
    }
        
    //-------------------------------------------------------------------------    
    public static void paintPrint(
        HtmlPage p,
        boolean forEditing
    ) {
        try {
            p.write("<div id=\"printButton\" onClick=\"javascript:yuiPrint();\">&nbsp;</div>");
        }
        catch(Exception e) {
            new ServiceException(e).log();
        }
    }
    
    //-------------------------------------------------------------------------    
    public static void paintBreadcrum(
        HtmlPage p,
        boolean forEditing
    ) {
        try {
            if(!forEditing) {
                Texts_1_0 texts = p.getApplicationContext().getTexts();
                HtmlEncoder_1_0 htmlEncoder = p.getApplicationContext().getHtmlEncoder();
                ShowObjectView view = (ShowObjectView)p.getView();
                if((view.getLookupType() != null) && view.getObjectReference().isInstanceof(view.getLookupType())) {
                    p.write("<input type=\"checkbox\" name=\"objselect\" value=\"obj\"", p.getOnClick("OF.selectAndClose('", view.getObjectReference().refMofId(), "', '", htmlEncoder.encode(view.getObjectReference().getTitleEscapeQuote(), false), "', '", view.getId(), "', window);"), " />");
                }
                p.write("<div id=\"reloadIcon\" ", p.getOnClick("javascript:window.location.href=", p.getEvalHRef(view.getObjectReference().getReloadAction()), ";"), " title=\"", texts.getReloadText(), "\">", p.getImg("src=\"", p.getResourcePath("images/"), view.getObjectReference().getIconKey(), "\" border=\"0\" align=\"absbottom\" alt=\"o\" title=\"\""), "</div>");
                Action[] selectParentActions = view.getSelectParentAction();
                for(
                    int i = 0; 
                    i < selectParentActions.length; 
                    i++
                ) {
                    Action selectParentAction = selectParentActions[i];
                    String style = null;
                    String separator = null;
                    String title = null;
                    if(selectParentAction != null) {
                        if(i == selectParentActions.length - 1) {
                            String strippedTitle = selectParentAction.getTitle();
                            strippedTitle = strippedTitle.replaceAll("<br />", "\u00b6");
                            if((i > 1) && (strippedTitle.length() > 45)) {
                                strippedTitle = strippedTitle.substring(0, 45) + "...";
                            }
                            title = view.getObjectReference().getObject() instanceof org.openmdx.base.jmi1.Segment ?
                                strippedTitle :
                                strippedTitle + " - " + view.getObjectReference().getLabel();
                            style = "class=\"current\"";
                            separator = "";
                        }
                        else {
                            title = selectParentAction.getTitle();
                            style = "";
                            separator = " &gt;";
                        }
                        p.write("<a ", style, " href=\"#\" onclick=\"javascript:this.href='./' + ", p.getEvalHRef(selectParentAction), ";\">", title, "</a>", separator);
                    }
                }
            }            
        }
        catch(Exception e) {
            new ServiceException(e).log();
        }        
    }
    
}
