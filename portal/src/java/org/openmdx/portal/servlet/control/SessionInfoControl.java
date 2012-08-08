/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: SessionInfoControl.java,v 1.33 2007/08/10 12:41:52 wfro Exp $
 * Description: SessionInfoControl
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/08/10 12:41:52 $
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
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.portal.servlet.control;

import java.io.Serializable;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.view.View;

public class SessionInfoControl
    extends Control
    implements Serializable {

    //-------------------------------------------------------------------------
    public SessionInfoControl(
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
    
    //---------------------------------------------------------------------------------
    public void paint(
        HtmlPage p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {
        AppLog.detail("> paint");
       
        View view = p.getView();
        ApplicationContext app = view.getApplicationContext();
        Action saveSettingsAction = view.getSaveSettingsAction();
        Action logoffAction = view.getLogoffAction();
        
        p.write("<table id=\"headerlayout\" style=\"position:relative;\">");
        p.write("  <tr id=\"headRow\">");
        p.write("    <td id=\"head\" colspan=\"2\">");
        p.write("      <table id=\"info\">");
        p.write("        <tr>");
        p.write("          <td id=\"headerCellLeft\"><img id=\"logoLeft\" src=\"", p.getResourcePath("images/"), "logoLeft.gif\" alt=\"", app.getApplicationName(), "\" title=\"\" /></td>");
        p.write("          <td id=\"headerCellSpacerLeft\"></td>");
        p.write("          <td id=\"headerCellMiddle\">");
        p.write("            <table id=\"headerMiddleLayout\">");
        p.write("              <tr>");
        p.write("                <td><span>", app.getLoginPrincipalId(), "</span></td>");
        // Locales
        if(forEditing) {
            p.write("                <td><span>", app.getCurrentLocaleAsString(), "</span></td>");            
        }
        else {
            p.write("                <td>");
            p.write("                  <ul id=\"nav\" class=\"nav\" onmouseover=\"sfinit(this);\" >");
            p.write("                    <li><a href=\"#\" onclick=\"javascript:return false;\">", app.getCurrentLocaleAsString(), "&nbsp;<img src=\"", p.getResourcePath("images/"), WebKeys.ICON_PANEL_DOWN, "\" alt=\"\" /></a>");
            p.write("                      <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
            Action[] selectLocaleAction = ((ShowObjectView)view).getSelectLocaleAction();
            for(int i = 0; i < selectLocaleAction.length; i++) {
                Action action = selectLocaleAction[i];
                p.write("                        <li><a href=\"#\" onclick=\"javascript:window.location.href=", p.getEvalHRef(action), ";\">", action.getParameter(Action.PARAMETER_LOCALE), " - ", action.getTitle(), "</a></li>");
            }
            p.write("                      </ul>");
            p.write("                    </li>");
            p.write("                  </ul>");
            p.write("                </td>");
        }
        if(!forEditing) {
            p.write("                <td><a class=\"abutton\" href=\"#\"", p.getOnClick("javascript:", p.getButtonEffectPulsate(), "window.location.href=", p.getEvalHRef(logoffAction), ";"), ">", logoffAction.getTitle(), "</a></td>");            
        }
        p.write("              </tr>");
        p.write("              <tr>");            
        // Roles
        Action[] setRoleAction = view.getSetRoleActions();
        if(forEditing || (setRoleAction.length < 2)) {
            p.write("                <td><span>", app.getCurrentUserRole(), "</span></td>");            
        }
        else {
            p.write("                <td>");
            p.write("                  <ul id=\"nav\" class=\"nav\" onmouseover=\"sfinit(this);\" >");
            p.write("                    <li><a href=\"#\" onclick=\"javascript:return false;\">", app.getCurrentUserRole(), "&nbsp;<img src=\"", p.getResourcePath("images/"), WebKeys.ICON_PANEL_DOWN, "\" alt=\"\" /></a>");
            p.write("                      <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
            for(int i = 0; i < setRoleAction.length; i++) {
                Action action = setRoleAction[i];
                p.write("                          <li><a href=\"#\" onclick=\"javascript:window.location.href=", p.getEvalHRef(action), ";\">", action.getParameter(Action.PARAMETER_NAME), "</a></li>");
            }
            p.write("                      </ul>");
            p.write("                    </li>");
            p.write("                  </ul>");
            p.write("                </td>");
        }
        // GUI modes
        Action[] setGuiModeAction = view.getSetGuiModeActions();
        String currentGuiMode = "0";
        for(int i = 0; i < setGuiModeAction.length; i++) {
            if(setGuiModeAction[i].getParameter(Action.PARAMETER_NAME).equals(app.getCurrentGuiMode())) {
                currentGuiMode = setGuiModeAction[i].getTitle();
                break;
            }
        }
        if(forEditing) {
            p.write("                <td><span>", p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_UI_MODE, currentGuiMode, p.getImgType(), "\" alt=\"\""), "&nbsp;</span></td>");            
        }
        else {
            p.write("                <td>");
            p.write("                  <ul id=\"nav\" class=\"nav\" onmouseover=\"sfinit(this);\" >");
            p.write("                    <li><a href=\"#\" onclick=\"javascript:return false;\">", p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_UI_MODE, currentGuiMode, p.getImgType(), "\" alt=\"\""), "&nbsp;", p.getImg("src=\"images/", WebKeys.ICON_PANEL_DOWN, "\" alt=\"\""), "</a>");
            p.write("                      <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
            for(int i = 0; i < setGuiModeAction.length; i++) {
                Action action = setGuiModeAction[i];
                p.write("                        <li><a href=\"#\" onclick=\"javascript:window.location.href=", p.getEvalHRef(action), ";\">", p.getImg("src=\"", p.getResourcePath("images/"), WebKeys.ICON_UI_MODE, action.getTitle(), p.getImgType(), "\" alt=\"\""), "</a></li>");
            }
            p.write("                      </ul>");
            p.write("                    </li>");        
            p.write("                  </ul>");
            p.write("                </td>");
        }
        if(!forEditing) {
            p.write("                <td><a class=\"abutton\" href=\"#\"", p.getOnClick("javascript:", p.getButtonEffectPulsate(), ";new Ajax.Request(", p.getEvalHRef(saveSettingsAction), ", {asynchronous:true});"), ">", saveSettingsAction.getTitle(), "</a></td>");            
        }
        p.write("              </tr>");
        p.write("            </table>");
        p.write("          </td>");
        p.write("          <td id=\"headerCellRight\"><img id=\"logoRight\" src=\"", p.getResourcePath("images/"), "logoRight.gif\" alt=\"\" title=\"\" /></td>");
        p.write("        </tr>");
        p.write("      </table>");
        p.write("    </td>");
        p.write("  </tr>");
        p.write("</table>");
                
        AppLog.detail("< paint");
    }

    //---------------------------------------------------------------------------------
    // Members
    //---------------------------------------------------------------------------------
    private static final long serialVersionUID = -5199330235931891428L;
    
}
