/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: RootMenuControl.java,v 1.4 2008/05/01 21:43:57 wfro Exp $
 * Description: ReferencePaneRenderer
 * Revision:    $AttributePaneRenderer: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/01 21:43:57 $
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

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.texts.Texts_1_0;
import org.openmdx.portal.servlet.view.ObjectView;

public class RootMenuControl
    extends Control
    implements Serializable {

    //-------------------------------------------------------------------------
    public RootMenuControl(
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
    public static void paintQuickAccessors(
        HtmlPage p
    ) throws ServiceException {
        ObjectView view = (ObjectView)p.getView();        
        if(view.getQuickAccessActions().length > 0) {
            for(int i = 0; i < view.getQuickAccessActions().length; i++) {
                Action action = view.getQuickAccessActions()[i];
                p.write("  <li><a href=\"#\"", p.getOnClick("javascript:this.href=", p.getEvalHRef(action), ";") + " title=\"", action.getToolTip(), "\">", (action.getIconKey() == null ? "" : p.getImg("src=\"", p.getResourcePath("images/"), action.getIconKey() + "\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"\"")), " " , action.getTitle(), "</a></li>");
            }
            p.write("  <li>&nbsp;</li>");
        }        
    }
    
    //-------------------------------------------------------------------------
    public static void paintRootObjects(
        HtmlPage p
    ) throws ServiceException {
        ObjectView view = (ObjectView)p.getView();
        ApplicationContext application = view.getApplicationContext();
        Action[] selectRootObjectAction = view.getSelectRootObjectActions();
        int lastItemLevel = 0;
        int currentItemLevel = 0;
        int i = 0;
        int nItemLevel0 = 0;
        String stateItemLevel0 = "1";
        while(i < selectRootObjectAction.length) {
            Action action = selectRootObjectAction[i];
            currentItemLevel = 0;
            if((action.getEvent() == Action.EVENT_SELECT_OBJECT) && (action.getParameter(Action.PARAMETER_REFERENCE).length() > 0)) {
              currentItemLevel = 1;
            }
            else if(action.getEvent() == Action.EVENT_SELECT_AND_NEW_OBJECT) {
              currentItemLevel = 2;
            }
            // Get state of root object
            if(currentItemLevel == 0) {
                stateItemLevel0 = application.getSettings().getProperty("RootObject." + nItemLevel0 + ".State", "1");
                nItemLevel0++;
            }
            // Only show menu entry if state is "1"
            if("1".equals(stateItemLevel0)) {
                // open levels
                int j = 0;
                while(j < currentItemLevel - lastItemLevel) {
                  p.write("  <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
                  j++;
                }
                // close levels
                j = 0;
                while(j < lastItemLevel - currentItemLevel) {
                    p.write("  </ul>");
                    p.write("</li>");
                    j++;
                }
                if(currentItemLevel == 1) {
                    p.write("    <li><a href=\"#\"", p.getOnClick("javascript:this.href=", p.getEvalHRef(action), ";") + ">&nbsp;&nbsp;", action.getTitle(), "</a></li>");
                }
                else {
                    p.write("<li><a href=\"#\"", p.getOnClick("javascript:this.href=", p.getEvalHRef(action), ";") + ">", p.getImg("src=\"", p.getResourcePath("images/"), action.getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"\""), " " , action.getTitle(), "</a>");
                }
            }
            lastItemLevel = currentItemLevel;
            i++;
        }
        // close levels
        i = 0;
        while(i < currentItemLevel) {
            p.write("  </ul>");
            p.write("</li>");
            i++;
        }        
    }
   
    //-------------------------------------------------------------------------
    public static void paintHistory(
        HtmlPage p
    ) throws ServiceException {        
        ObjectView view = (ObjectView)p.getView();      
        ApplicationContext app = view.getApplicationContext();        
        Texts_1_0 texts = app.getTexts();        
        String state = app.getSettings().getProperty("History.State", "1");
        if("1".equals(state)) {
            if(view.getHistoryAction().length > 0) {
                p.write("<li><a href=\"#\">", p.getImg("src=\"", p.getResourcePath("images/"), "access_history", p.getImgType(), "\" border=\"0\" align=\"bottom\" alt=\">\" title=\"\""), " " , texts.getHistoryText(), "</a>");
                p.write("  <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
                int i = 0;
                while(i < view.getHistoryAction().length) {
                    Action action = view.getHistoryAction()[i];
                    p.write("    <li><a href=\"#\"", p.getOnClick("javascript:this.href=", p.getEvalHRef(action), ";") + " title=\"", action.getTitle(), "\">", p.getImg("src=\"", p.getResourcePath("images/"), action.getIconKey() + "\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"\""), " " , action.getTitle(), "</a></li>");
                    i++;
                }
                p.write("  </ul>");
                p.write("</li>");
            }
        }
    }
    
    //---------------------------------------------------------------------------------
    // Members
    //---------------------------------------------------------------------------------
               
}
