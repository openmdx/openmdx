/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: ShowObjectView 
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
package org.openmdx.portal.servlet.action;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.component.ObjectView;
import org.openmdx.portal.servlet.component.ShowObjectView;

public class MacroAction extends BoundAction {

	public final static int EVENT_ID = 43;

	@Override
    public ActionPerformResult perform(
        ObjectView view,
        HttpServletRequest request,
        HttpServletResponse response,        
        String parameter,
        HttpSession session,
        Map<String,String[]> requestParameters,
        ViewsCache editViewsCache,
        ViewsCache showViewsCache      
    ) throws IOException, ServletException {
		ShowObjectView currentView = (ShowObjectView)view;
    	ObjectView nextView = currentView;
        ViewPort.Type nextViewPortType = null;
        ApplicationContext app = currentView.getApplicationContext();
        // parameter is of format
        // [name=name][;type=type]
        String objectXri = Action.getParameter(parameter, Action.PARAMETER_OBJECTXRI);
        Path objectIdentity = new Path(objectXri);
        try {
            String actionName = Action.getParameter(parameter, Action.PARAMETER_NAME);
            if(actionName != null) {
                actionName = new String(Base64.decode(actionName));
            }
            String actionType = Action.getParameter(parameter, Action.PARAMETER_TYPE);
            nextView = new ShowObjectView(
                currentView.getId(),
                currentView.getContainerElementId(),
                (RefObject_1_0)app.getNewPmData().getObjectById(objectIdentity),
                app,
                currentView.createHistoryAppendCurrent(),
                null, // nextPrevActions
                currentView.getLookupType(),
                null, // do not propagate resourcePathPrefix
                null, // do not propagate navigationTarget
                null // do not propagate isReadOnly
            );
            nextView.setMacro(
                new Object[]{
                    actionType == null ? new Short((short)0) : new Short(actionType),
                    actionName,
                    Collections.EMPTY_LIST
                }
            );
        } catch (Exception e) {
            ServiceException e0 = new ServiceException(e);
            SysLog.warning(e0.getMessage(), e0.getCause());
            app.addErrorMessage(
                app.getTexts().getErrorTextCannotSelectObject(), new String[] { objectXri, e.getMessage() }
            );
            nextView = currentView;
        }
        return new ActionPerformResult(
            nextView,
            nextViewPortType
        );
    }
        
}
