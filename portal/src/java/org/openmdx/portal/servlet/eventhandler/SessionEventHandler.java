/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: SessionEventHandler.java,v 1.17 2009/09/25 12:02:38 wfro Exp $
 * Description: SessionEventHandler 
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/25 12:02:38 $
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
package org.openmdx.portal.servlet.eventhandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.ViewPortFactory;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.ShowObjectView;

public class SessionEventHandler {

    //-----------------------------------------------------------------------
    public static HandleEventResult handleEvent(
        int event,
        ObjectView view,
        HttpServletRequest request,
        HttpServletResponse response,
        ApplicationContext application,
        String parameter,
        Map parameterMap
    ) throws IOException, ServletException {
        if(view instanceof ShowObjectView) {
            
            switch(event) {
            
                case Action.EVENT_SET_PANEL_STATE:
                    try {
                        ViewPort p = ViewPortFactory.openPage(view, request, EventHandlerHelper.getWriter(request, response));
                        String panelName = Action.getParameter(parameter, Action.PARAMETER_NAME);
                        int panelState = Integer.parseInt(Action.getParameter(parameter, Action.PARAMETER_STATE));
                        application.setPanelState(panelName, panelState);
                        p.close(true);                     
                    }
                    catch (Exception e) {
                        ServiceException e0 = new ServiceException(e);
                        SysLog.warning(e0.getMessage(), e0.getCause());
                    }
                    break;
        
                case Action.EVENT_SAVE_SETTINGS:
                    try {
                        ViewPort p = ViewPortFactory.openPage(view, request, EventHandlerHelper.getWriter(request, response));
                        application.saveSettings(false);
                        p.close(true);
                    }
                    catch (Exception e) {
                        ServiceException e0 = new ServiceException(e);
                        SysLog.warning(e0.getMessage(), e0.getCause());
                    }
                    break;
                    
                case Action.EVENT_LOGOFF:             
                    application.saveSettings(true);
                    application.close();
                    request.getSession().setAttribute(
                        WebKeys.LOCALE_KEY, 
                        application.getCurrentLocaleAsString()
                    );
                    request.getSession().setAttribute(
                        WebKeys.TIMEZONE_KEY, 
                        application.getCurrentTimeZone()
                    );
                    ServletContext sc = request.getSession().getServletContext();
                    RequestDispatcher rd = sc.getRequestDispatcher(
                        "/Logoff.jsp?locale=" + URLEncoder.encode(application.getCurrentLocaleAsString(), "UTF-8") + "&timezone=" + URLEncoder.encode(application.getCurrentTimeZone(), "UTF-8")
                    );
                    rd.forward(
                        request, 
                        response
                    );      
                    SysLog.detail("logoff");
                    break;                    
            }
        }
        return new HandleEventResult(
            HandleEventResult.StatusCode.DONE
        );
    }

    //-------------------------------------------------------------------------
    public static boolean acceptsEvent(
        int event
    ) {
        return
            (event == Action.EVENT_SET_PANEL_STATE) ||
            (event == Action.EVENT_SAVE_SETTINGS) ||
            (event == Action.EVENT_LOGOFF);
    }

}
