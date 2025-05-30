/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: SessionInfoControl
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
import java.util.Collections;
import java.util.List;

import org.openmdx.kernel.exception.Throwables;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.attribute.DateValue;
import org.openmdx.portal.servlet.component.ShowObjectView;
import org.openmdx.portal.servlet.component.View;
import org.w3c.time.SystemClock;

/**
 * SessionInfoControl
 *
 */
public class SessionInfoControl extends Control implements Serializable {

    /**
     * Constructor.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     */
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
    
    /**
     * Paint login principal.
     * 
     * @param p
     */
    public static void paintLoginPrincipal(
        ViewPort p
    ) {
        try {
            ApplicationContext app = p.getApplicationContext();
            p.write("<span>", app.getLoginPrincipal(), "</span>");
        } catch(Exception e) {
            Throwables.log(e);
        }
    }
    
    /**
     * Paint locales menu.
     * 
     * @param p
     * @param forEditing
     */
    public static void paintLocalesMenu(
        ViewPort p,
        boolean forEditing
    ) {
        try {
            ApplicationContext app = p.getApplicationContext();
            View view = p.getView();        
            if(forEditing) {
                p.write("<span>", app.getCurrentLocaleAsString(), "</span>");            
            } else {
                p.write("<ul class=\"", CssClass.nav.toString(), " ", CssClass.nav_pills.toString(), "\">");
                p.write("  <div class=\"", CssClass.dropdown.toString(), "\">");
                p.write("    <button type=\"button\" class=\"", CssClass.btn.toString(), " " , CssClass.btn_sm.toString(), " " , CssClass.dropdown_toggle.toString(), "\" data-toggle=\"", CssClass.dropdown.toString(), "\" onclick=\"javascript:this.parentNode.hide=function(){};\">", app.getCurrentLocaleAsString(), "  <b class=\"caret\"></b></button>");
                p.write("    <div class=\"", CssClass.dropdown_menu.toString(), "\" style=\"z-index:1010;\">");
                Action[] selectLocaleAction = ((ShowObjectView)view).getSelectLocaleAction();
                for(int i = 0; i < selectLocaleAction.length; i++) {
                    Action action = selectLocaleAction[i];
                    p.write("      <a href=\"#\" class=\"" + CssClass.dropdown_item.toString(), "\" onclick=\"javascript:window.location.href=", p.getEvalHRef(action), ";\">", action.getParameter(Action.PARAMETER_LOCALE), " - ", action.getTitle(), "</a>");
                }
                p.write("    </div>");
                p.write("  </div>");
                p.write("</ul>");
            }
        } catch(Exception e) {
            Throwables.log(e);
        }
    }
    
    /**
     * Paint logoff button.
     * 
     * @param p
     * @param forEditing
     * @param buttonClass
     * @param buttonStyle
     */
    public static void paintLogoffButton(
        ViewPort p,
        boolean forEditing,
        String buttonClass,
        String buttonStyle
    ) {
        try {
            View view = p.getView();
            ApplicationContext app = p.getApplicationContext();            
            Action logoffAction = view.getLogoffAction();        
            if(!forEditing) {
                p.write("<a class=\"", buttonClass, "\" style=\"", buttonStyle, "\" href=\"#\" onclick=\"javascript:window.location.href=", p.getEvalHRef(logoffAction), ";\">", logoffAction.getTitle(), "&nbsp;", app.getLoginPrincipal(), "</a>");
            }
        } catch(Exception e) {
            Throwables.log(e);
        }
    }
    
    /**
     * Paint logoff button.
     * 
     * @param p
     * @param forEditing
     * @param buttonClass
     */
    public static void paintLogoffButton(
        ViewPort p,
        boolean forEditing,
        String buttonClass
    ) {
    	paintLogoffButton(
    		p,
    		forEditing,
    		buttonClass,
    		"border:none;"
    	);
    }

    /**
     * Paint roles menu.
     * 
     * @param p
     * @param forEditing
     */
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
            if(forEditing) {            	
                p.write("<span>", app.getCurrentUserRole(), "</span>");            
            } else {
            	boolean hasRoles = setRoleAction.length > 1;
                p.write("<ul class=\"", CssClass.nav.toString(), " ", CssClass.nav_pills.toString(), "\">");
                p.write("  <div class=\"", CssClass.dropdown.toString(), (hasRoles ? "" : " " + CssClass.disabled), "\">");
                p.write("    <button type=\"button\" class=\"", CssClass.btn.toString(), " ", CssClass.btn_sm.toString(), " ", CssClass.dropdown_toggle.toString(), "\" data-toggle=\"", CssClass.dropdown.toString(), "\" onclick=\"javascript:this.parentNode.hide=function(){};\">", htmlEncoder.encode(currentRoleTitle, false), " <b class=\"caret\"></b></button>");
                if(hasRoles) {
                	p.write("    <div class=\"", CssClass.dropdown_menu.toString(), "\" style=\"z-index:1010;\">");
	                for(int i = 0; i < setRoleAction.length; i++) {
	                    Action action = setRoleAction[i];
	                    p.write("      <a href=\"#\" class=\"", CssClass.dropdown_item.toString(), "\" onclick=\"javascript:window.location.href=", p.getEvalHRef(action), ";\">", action.getTitle(), "</a>");
	                }
	                p.write("    </div>");
                }
                p.write("  </div>");
                p.write("</ul>");
            }
        } catch(Exception e) {
            Throwables.log(e);
        }        
    }

    /**
     * Paint current date-time.
     * 
     * @param p
     * @param separator
     */
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
            String formattedDateTime = dateTimeFormat.format(SystemClock.getInstance().now());
            p.write(
                formattedDateTime.replace(" ", separator), 
                separator, 
                dateTimeFormat.getTimeZone().getID()
            );
        } catch(Exception e) {
            Throwables.log(e);
        }
    }
    
    /**
     * Paint save settings button.
     * 
     * @param p
     * @param forEditing
     * @param buttonClass
     * @param buttonStyle
     */
    public static void paintSaveSettingsButton(
        ViewPort p,
        boolean forEditing,
        String buttonClass,
        String buttonStyle
    ) {
        try {
            View view = p.getView();        
            Action saveSettingsAction = view.getSaveSettingsAction();
            if(!forEditing) {
                p.write("<a class=\"", buttonClass, "\" style=\"", buttonStyle, "\" href=\"#\" onclick=\"javascript:new Ajax.Request(", p.getEvalHRef(saveSettingsAction), ", {asynchronous:true});\">", saveSettingsAction.getTitle(), "</a>");
            }
        } catch(Exception e) {
            Throwables.log(e);
        }
    }

    /**
     * Paint save settings button.
     * 
     * @param p
     * @param forEditing
     * @param buttonClass
     */
    public static void paintSaveSettingsButton(
        ViewPort p,
        boolean forEditing,
        String buttonClass
    ) {
    	paintSaveSettingsButton(
    		p,
    		forEditing,
    		buttonClass,
    		"border:none;"
    	);
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

    //---------------------------------------------------------------------------------
    // Members
    //---------------------------------------------------------------------------------
    private static final long serialVersionUID = -5199330235931891428L;
    
}
