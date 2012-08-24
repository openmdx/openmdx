/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: SessionInfoControl
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.attribute.DateValue;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.view.View;

public class SessionInfoControl
    extends Control
    implements Serializable {

    //-------------------------------------------------------------------------
    public SessionInfoControl(
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
    
    //---------------------------------------------------------------------------------
    public static void paintLoginPrincipal(
        ViewPort p
    ) {
        try {
            ApplicationContext app = p.getApplicationContext();
            p.write("<span>", app.getLoginPrincipal(), "</span>");
        }
        catch(Exception e) {
            new ServiceException(e).log();
        }
    }
    
    //---------------------------------------------------------------------------------
    public static void paintLocalesMenu(
        ViewPort p,
        boolean forEditing
    ) {
        try {
            ApplicationContext app = p.getApplicationContext();
            View view = p.getView();        
            if(forEditing) {
                p.write("<span>", app.getCurrentLocaleAsString(), "</span>");            
            }
            else {
                p.write("<ul id=\"nav\" class=\"nav\" onmouseover=\"sfinit(this);\" >");
                p.write("  <li><a href=\"#\" onclick=\"javascript:return false;\">", app.getCurrentLocaleAsString(), "&nbsp;<img src=\"", p.getResourcePath("images/"), WebKeys.ICON_PANEL_DOWN, "\" alt=\"\" /></a>");
                p.write("    <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
                Action[] selectLocaleAction = ((ShowObjectView)view).getSelectLocaleAction();
                for(int i = 0; i < selectLocaleAction.length; i++) {
                    Action action = selectLocaleAction[i];
                    p.write("      <li><a href=\"#\" onclick=\"javascript:window.location.href=", p.getEvalHRef(action), ";\"><span style=\"font-family:courier;\">", action.getParameter(Action.PARAMETER_LOCALE), " - </span>", action.getTitle(), "</a></li>");
                }
                p.write("    </ul>");
                p.write("  </li>");
                p.write("</ul>");
            }
        }
        catch(Exception e) {
            new ServiceException(e).log();
        }
    }
    
    //---------------------------------------------------------------------------------
    public static void paintLogoffButton(
        ViewPort p,
        boolean forEditing,
        String buttonClass
    ) {
        try {
            View view = p.getView();
            ApplicationContext app = p.getApplicationContext();            
            Action logoffAction = view.getLogoffAction();        
            if(!forEditing) {
                p.write("<a class=\"", buttonClass, "\" href=\"#\" onclick=\"javascript:", p.getButtonEffectPulsate(), "window.location.href=", p.getEvalHRef(logoffAction), ";\">", logoffAction.getTitle(), "&nbsp;", app.getLoginPrincipal(), "</a>");            
            }
        }
        catch(Exception e) {
            new ServiceException(e).log();
        }
    }
    
    //---------------------------------------------------------------------------------
    public static void paintRolesMenu(
        ViewPort p,
        boolean forEditing
    ) {
        try {
            ApplicationContext app = p.getApplicationContext();
            HtmlEncoder_1_0 htmlEncoder = app.getHtmlEncoder();
            View view = p.getView();        
            Action[] setRoleAction = view.getSetRoleActions();
            String currentRoleTitle = app.getPortalExtension().getTitle(
            	p.getView().getObject(),
            	new Action(Action.EVENT_SET_ROLE, null, app.getCurrentUserRole(), true),
            	app.getCurrentUserRole(),
            	app
            );
            if(p.getViewPortType() == ViewPort.Type.MOBILE) {
            	String groupId = "selectRole";
                p.write("    <ul id=\"t", groupId, "\" selected=\"true\" style=\"position:relative;top:auto;min-height:0px;\" onclick=\"javascript:var e=document.getElementById('p", groupId, "');if(e.style.display=='block'){e.style.display='none';}else{e.style.display='block';};\">");
                p.write("      <li class=\"group\" style=\"height:40px;\"><div style=\"padding-top:10px;\">", p.getImg("src=\"", p.getResourcePathPrefix(), "images/", WebKeys.ICON_ROLE, "\""), "&nbsp;&nbsp;&nbsp;", htmlEncoder.encode(currentRoleTitle, false), "</div></li>");
                p.write("    </ul>");
                p.write("    <ul id=\"p", groupId, "\" selected=\"true\" style=\"display:none;position:relative;top:auto;min-height:0px;\">");
                for(int i = 0; i < setRoleAction.length; i++) {
                    Action action = setRoleAction[i];
                    p.write("      <li><a href=\"#\" onclick=\"javascript:window.location.href=", p.getEvalHRef(action), ";\">", p.getImg("src=\"", p.getResourcePathPrefix(), "images/", action.getIconKey(), "\""), "&nbsp;&nbsp;&nbsp;", action.getTitle(), "</a></li>");
                }
                p.write("    </ul>");
            }
            else if(forEditing || (setRoleAction.length < 2)) {            	
                p.write("<span>", app.getCurrentUserRole(), "</span>");            
            }
            else {
                p.write("<ul id=\"nav\" class=\"nav\" onmouseover=\"sfinit(this);\" >");
                p.write("  <li><a href=\"#\" onclick=\"javascript:return false;\">", htmlEncoder.encode(currentRoleTitle, false), "&nbsp;<img src=\"", p.getResourcePath("images/"), WebKeys.ICON_PANEL_DOWN, "\" alt=\"\" /></a>");
                p.write("    <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
                for(int i = 0; i < setRoleAction.length; i++) {
                    Action action = setRoleAction[i];
                    p.write("      <li><a href=\"#\" onclick=\"javascript:window.location.href=", p.getEvalHRef(action), ";\">", action.getTitle(), "</a></li>");
                }
                p.write("    </ul>");
                p.write("  </li>");
                p.write("</ul>");
            }
        }
        catch(Exception e) {
            new ServiceException(e).log();
        }        
    }
    
    //---------------------------------------------------------------------------------
    public static void paintCurrentDateTime(
        ViewPort p,
        String separator
    ) {
        try {
            ApplicationContext app = p.getApplicationContext();
            SimpleDateFormat dateTimeFormat = DateValue.getLocalizedDateTimeFormatter(
                null, 
                true, 
                app
            );
            String formattedDateTime = dateTimeFormat.format(new Date());
            p.write(
                formattedDateTime.replace(" ", separator), 
                separator, 
                dateTimeFormat.getTimeZone().getID()
            );
        }
        catch(Exception e) {
            new ServiceException(e).log();
        }
    }
    
    //---------------------------------------------------------------------------------
    public static void paintSaveSettingsButton(
        ViewPort p,
        boolean forEditing,
        String buttonClass
    ) {
        try {
            View view = p.getView();        
            Action saveSettingsAction = view.getSaveSettingsAction();
            if(!forEditing) {
                p.write("<a class=\"", buttonClass, "\" href=\"#\" onclick=\"javascript:", p.getButtonEffectPulsate(), ";new Ajax.Request(", p.getEvalHRef(saveSettingsAction), ", {asynchronous:true});\">", saveSettingsAction.getTitle(), "</a>");            
            }
        }
        catch(Exception e) {
            new ServiceException(e).log();
        }        
    }
    
    //---------------------------------------------------------------------------------
    // Members
    //---------------------------------------------------------------------------------
    private static final long serialVersionUID = -5199330235931891428L;
    
}
