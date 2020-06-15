/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: GridEventHandler 
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
package org.openmdx.portal.servlet.action;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmdx.kernel.exception.Throwables;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.Texts_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.ViewPortFactory;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.component.ObjectView;
import org.openmdx.portal.servlet.component.OperationPane;
import org.openmdx.portal.servlet.component.ShowObjectView;
import org.openmdx.portal.servlet.component.UiOperationParam;
import org.openmdx.portal.servlet.component.UiOperationTab;

/**
 * GetOperationDialogAction
 *
 */
public class UiGetOperationDialogAction extends BoundAction {

    public static final int EVENT_ID = 52;
    public static final int OPERATION_DIALOG_ZINDEX = 5000;

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.action.BoundAction#perform(org.openmdx.portal.servlet.view.ObjectView, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String, javax.servlet.http.HttpSession, java.util.Map, org.openmdx.portal.servlet.ViewsCache, org.openmdx.portal.servlet.ViewsCache)
	 */
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
    ) throws IOException {
        if(view instanceof ShowObjectView) {
            ShowObjectView currentView = (ShowObjectView)view;
            ViewPort p = ViewPortFactory.openPage(
                view,
                request,
                this.getWriter(request, response)
            );
            ApplicationContext app = currentView.getApplicationContext();
            Texts_1_0 texts = app.getTexts();
            try {
                String operationId = Action.getParameter(parameter, Action.PARAMETER_ID);
                int operationIndex = Integer.valueOf(operationId);
                int paneIndex = (operationIndex / 100) - 1;
                int tabIndex = operationIndex % 100;
                OperationPane operationPane = currentView.getChildren(OperationPane.class).get(paneIndex);
                List<UiOperationTab> operationTabs = operationPane.getChildren(UiOperationTab.class);
                if(tabIndex < operationTabs.size()) {
                	UiOperationTab operationTab = operationTabs.get(tabIndex);
	                Action invokeOperationAction = operationTab.getInvokeOperationAction(currentView, app);
	                String formId = "op" + operationId + "Form";
	                // Operations with no arguments
	                if(operationTab.getChildren(UiOperationParam.class).isEmpty()) {            
	                    // Confirmation prompt
	                    if(operationTab.confirmExecution(app)) {
	                        p.write("<div id=\"op", operationId, "Dialog\">");
	                        p.write("  <div class=\"", CssClass.OperationDialogTitle.toString(), "\">", texts.getAssertExecutionText(), "</div> <!-- name of operation -->");
	                        p.write("  <div class=\"", CssClass.bd.toString(), "\">");                        
	                        p.write("    <form id=\"", formId, "\" name=\"", formId, "\" enctype=\"multipart/form-data\" accept-charset=\"utf-8\"", (operationTab.displayOperationResult() ? " target=\"OperationDialogResponse\"" : ""), " method=\"post\">");
	                        p.write("      <input type=\"hidden\" name=\"requestId.submit\" value=\"", view.getRequestId(), "\" />");
	                        p.write("      <input type=\"hidden\" name=\"event.submit\" value=\"", Integer.toString(invokeOperationAction.getEvent()), "\" />");
	                        p.write("      <input type=\"hidden\" name=\"parameter\" value=\"", invokeOperationAction.getParameter(), "\" />");
	                        p.write("      <div id=\"opWaitArea", operationId, "\" style=\"width:50px;height:24px;\" class=\"", CssClass.wait.toString(), "\">&nbsp;</div>");
	                        p.write("      <div>&nbsp;</div>");
	                        p.write("      <div id=\"opSubmitArea", operationId, "\" style=\"display:none;\">");
	                        p.write("        <input type=\"submit\" class=\"", CssClass.btn.toString(), " ", CssClass.btn_light.toString(), "\" value=\"", texts.getOkTitle(), "\" onclick=\"javascript:$('opWaitArea", operationId, "').style.display='block';$('opSubmitArea", operationId, "').style.display='none';\" />");
	                        p.write("        <input type=\"button\" class=\"", CssClass.btn.toString(), " ", CssClass.btn_light.toString(), "\" value=\"", texts.getCancelTitle(), "\" onclick=\"javascript:$('OperationDialogHolder').style.display='none';$('OperationDialog').innerHTML='';\" />");
	                        p.write("      </div>");
	                        p.write("    </form>");
		                    p.write("    <script language=\"javascript\" type=\"text/javascript\">");
	                        p.write("      $('opWaitArea", operationId, "').style.display='none';");
	                        p.write("      $('opSubmitArea", operationId, "').style.display='block';");
	                        p.write("      $('OperationDialogHolder').style.zIndex=", Integer.toString(OPERATION_DIALOG_ZINDEX), ";");
	                        p.write("      $('OperationDialogHolder').style.display='block';");
	                        p.write("    </script>");
	                        p.write("  </div>");
	                        p.write("</div>");
		                	p.write("<br />");	                        
	                    }
	                } else {
	                    // Standard operations
	                    p.write("<div id=\"op", operationId, "Dialog\">");
	                    p.write("  <div class=\"", CssClass.OperationDialogTitle.toString(), "\">", operationTab.getToolTip(), "</div> <!-- name of operation -->");
	                    p.write("  <div class=\"", CssClass.bd.toString(), "\">");                        
	                    p.write("    <form id=\"", formId, "\" name=\"", formId, "\" action=\"\" enctype=\"multipart/form-data\" accept-charset=\"utf-8\"", (operationTab.displayOperationResult() ? " target=\"OperationDialogResponse\"" : ""), " method=\"post\">");
	                    // only show field group for input fields
	                    for(int k = 0; k < 1; k++) {
	                        UiOperationParam operationParam = operationTab.getChildren(UiOperationParam.class).get(k);
	                        operationParam.paintOperationParams(
	                        	p,
	                        	formId,
	                        	operationIndex
	                        );
	                    }
	                    p.write("      <input type=\"hidden\" name=\"requestId.submit\" value=\"", view.getRequestId(), "\" />");
	                    p.write("      <input type=\"hidden\" name=\"event.submit\" value=\"", Integer.toString(invokeOperationAction.getEvent()), "\" />");
	                    p.write("      <input type=\"hidden\" name=\"parameter.submit\" value=\"", invokeOperationAction.getParameter(), "\" />");
	                    p.write("      <div id=\"opWaitArea", operationId, "\" style=\"width:50px;height:24px;\" class=\"", CssClass.wait.toString(), "\">&nbsp;</div>");
	                    p.write("      <div>&nbsp;</div>");
	                    p.write("      <div id=\"opSubmitArea", operationId, "\" style=\"display:none;\">");
	                    p.write("        <input type=\"submit\" class=\"", CssClass.btn.toString(), " ", CssClass.btn_light.toString(), "\" value=\"", texts.getOkTitle(), "\" onclick=\"javascript:$('opWaitArea", operationId, "').style.display='block';$('opSubmitArea", operationId, "').style.display='none';\" />");
	                    p.write("        <input type=\"button\" class=\"", CssClass.btn.toString(), " ", CssClass.btn_light.toString(), "\" value=\"", texts.getCancelTitle(), "\" onclick=\"javascript:$('OperationDialogHolder').style.display='none';$('OperationDialog').innerHTML='';\" />");
	                    p.write("      </div>");
	                    p.write("    </form>");
	                    p.write("    <script language=\"javascript\" type=\"text/javascript\">");
	                    p.write("      $('opWaitArea", operationId, "').style.display='none';");
	                    p.write("      $('opSubmitArea", operationId, "').style.display='block';");
	                    p.write("      $('OperationDialogHolder').style.zIndex=", Integer.toString(OPERATION_DIALOG_ZINDEX), ";");
	                    p.write("      $('OperationDialogHolder').style.display='block';");
	                    p.write("    </script>");
	                    p.write("  </div>");
	                    p.write("</div>");
	                	p.write("<br />");                     
	                }
                }
            } catch (Exception e) {
                Throwables.log(e);
            }
            try {
                p.close(true);
            } catch (Exception e) {
                Throwables.log(e);
            }
        }
        return new ActionPerformResult(
            ActionPerformResult.StatusCode.DONE
        );
    }

}
