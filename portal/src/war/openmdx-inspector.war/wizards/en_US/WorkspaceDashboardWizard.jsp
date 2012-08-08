<%@  page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %><%
/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: WorkspaceDashboardWizard.jsp,v 1.2 2011/03/18 17:45:26 wfro Exp $
 * Description: WorkspaceDashboardWizard.jsp
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/03/18 17:45:26 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 *
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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
 * This product includes software developed by Mihai Bazon
 * (http://dynarch.com/mishoo/calendar.epl) published with an LGPL
 * license.
 */
%><%@ page session="true" import="
java.util.*,
java.io.*,
java.text.*,
org.openmdx.application.cci.*,
org.openmdx.base.text.conversion.*,
org.openmdx.base.accessor.cci.*,
org.openmdx.kernel.id.cci.*,
org.openmdx.kernel.id.*,
org.openmdx.base.accessor.jmi.cci.*,
org.openmdx.portal.servlet.*,
org.openmdx.portal.servlet.attribute.*,
org.openmdx.portal.servlet.view.*,
org.openmdx.portal.servlet.texts.*,
org.openmdx.portal.servlet.control.*,
org.openmdx.portal.servlet.reports.*,
org.openmdx.portal.servlet.wizards.*,
org.openmdx.base.naming.*
" %><%
	final String FORM_NAME = "WorkspaceDashboardForm";
	final String WIZARD_NAME = "WorkspaceDashboardWizard.jsp";

	final String COMMAND_APPLY = "Apply";
	final String COMMAND_CANCEL = "Cancel";
	final String COMMAND_ADD = "Add";
	final String COMMAND_DELETE = "Delete";
	
	request.setCharacterEncoding("UTF-8");
	String servletPath = "." + request.getServletPath();
	String servletPathPrefix = servletPath.substring(0, servletPath.lastIndexOf("/") + 1);

	ApplicationContext app = (ApplicationContext)session.getValue(WebKeys.APPLICATION_KEY);
	ViewsCache viewsCache = (ViewsCache)session.getValue(WebKeys.VIEW_CACHE_KEY_SHOW);
	String requestId =  request.getParameter(Action.PARAMETER_REQUEST_ID);
	String objectXri = request.getParameter(Action.PARAMETER_OBJECTXRI);
	if(objectXri == null || app == null || viewsCache.getView(requestId) == null) {
		response.sendRedirect(
			request.getContextPath() + "/" + WebKeys.SERVLET_NAME
		);
		return;
	}
	javax.jdo.PersistenceManager pm = app.getNewPmData();
	RefObject_1_0 obj = (RefObject_1_0)pm.getObjectById(new Path(objectXri));
	Texts_1_0 texts = app.getTexts();
	Codes codes = app.getCodes();

	// Get Parameters
	String command = request.getParameter("Command");
	if(command == null) command = "";						
	boolean actionApply = COMMAND_APPLY.equals(command);
	boolean actionCancel = COMMAND_CANCEL.equals(command);
	boolean actionAdd = COMMAND_ADD.equals(command);
	boolean actionDelete = command != null && command.startsWith(COMMAND_DELETE + ".");
	
	if(actionCancel) {
	  session.setAttribute(WIZARD_NAME, null);
		Action nextAction = new ObjectReference(obj, app).getSelectObjectAction();
		response.sendRedirect(
			request.getContextPath() + "/" + nextAction.getEncodedHRef()
		);
		return;
	}
	else if(actionAdd) {
		String dashboardId = null;
		List<String> dashletIds = new ArrayList<String>();
		Enumeration<String> parameterNames = request.getParameterNames();
		while(parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			if(parameterName.endsWith("." + WorkspaceDashboardControl.DASHLET_PROPERTY_ID)) {
				String dashletId = parameterName.substring(0, parameterName.lastIndexOf("."));
				dashletIds.add(dashletId);
				if(dashboardId == null) {
					dashboardId = dashletId.substring(0, dashletId.lastIndexOf(".")); 
				}
			}
		}
		if(dashboardId == null) {
			String workspaceName = app.getCurrentWorkspace();
			dashboardId = WorkspaceDashboardControl.class.getSimpleName() + "." + workspaceName;	
		}
		if(dashboardId != null) {
			Properties settings = app.getSettings();
			String ids = settings.getProperty(
				dashboardId + "." + WorkspaceDashboardControl.DASHBOARD_PROPERTY_DASHLETS
			);
			Set<String> idsAsSet = ids == null ?
				new HashSet<String>() :
					new HashSet<String>(Arrays.asList(ids.split(";")));
			int freeIndex = -1;
			for(int i = 0; i < 100; i++) {
				if(!idsAsSet.contains("D" + i)) {
					freeIndex = i;
					break;
				}
			}
			String id = "D" + freeIndex;
			String name = request.getParameter("SelectedDashlet");
			String dashletId = dashboardId + "." + id;
			settings.setProperty(
				dashletId + "." + WorkspaceDashboardControl.DASHLET_PROPERTY_ID,
				id
			);
			settings.setProperty(
				dashletId + "." + WorkspaceDashboardControl.DASHLET_PROPERTY_NAME,
				name
			);
			settings.setProperty(
				dashletId + "." + WorkspaceDashboardControl.DASHLET_PROPERTY_LABEL,
				name
			);
			settings.setProperty(
				dashletId + "." + WorkspaceDashboardControl.DASHLET_PROPERTY_WIDTH,
				"1"
			);
			settings.setProperty(
				dashletId + "." + WorkspaceDashboardControl.DASHLET_PROPERTY_ORDERX,
				"0"
			);
			settings.setProperty(
				dashletId + "." + WorkspaceDashboardControl.DASHLET_PROPERTY_ORDERY,
				"9"
			);				
			settings.setProperty(
				dashboardId + "." + WorkspaceDashboardControl.DASHBOARD_PROPERTY_DASHLETS,
				(ids == null ? "" : ids + ";") + id
			);
		}
	}
	else if(actionApply) {
		// Get Dashlet-Ids
		String dashboardId = null;
		List<String> dashletIds = new ArrayList<String>();
		Enumeration<String> parameterNames = request.getParameterNames();
		while(parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			if(parameterName.endsWith("." + WorkspaceDashboardControl.DASHLET_PROPERTY_ID)) {
				String dashletId = parameterName.substring(0, parameterName.lastIndexOf("."));
				dashletIds.add(dashletId);
				if(dashboardId == null) {
					dashboardId = dashletId.substring(0, dashletId.lastIndexOf(".")); 
				}
			}
		}
		// Save dashlet properties
		if(dashboardId != null) {
			Properties settings = app.getSettings();
			String dashlets = "";
			for(String dashletId: dashletIds) {
				String id = request.getParameter(dashletId + "." + WorkspaceDashboardControl.DASHLET_PROPERTY_ID);
				String name = request.getParameter(dashletId + "." + WorkspaceDashboardControl.DASHLET_PROPERTY_NAME);
				String label = request.getParameter(dashletId + "." + WorkspaceDashboardControl.DASHLET_PROPERTY_LABEL);
				if(label == null) {
					label = name;
				}
				String width = request.getParameter(dashletId + "." + WorkspaceDashboardControl.DASHLET_PROPERTY_WIDTH);
				if(width == null) {
					width = "1";
				}
				String orderX = request.getParameter(dashletId + "." + WorkspaceDashboardControl.DASHLET_PROPERTY_ORDERX);
				if(orderX == null) {
					orderX = "9999";
				}
				String orderY = request.getParameter(dashletId + "." + WorkspaceDashboardControl.DASHLET_PROPERTY_ORDERY);
				if(orderY == null) {
					orderY = "9999";
				}
				if(id != null && name != null) {
					if(dashlets.length() > 0) dashlets += ";";
					dashlets += id;
					settings.setProperty(
						dashletId + "." + WorkspaceDashboardControl.DASHLET_PROPERTY_ID,
						id
					);
					settings.setProperty(
						dashletId + "." + WorkspaceDashboardControl.DASHLET_PROPERTY_NAME,
						name
					);
					settings.setProperty(
						dashletId + "." + WorkspaceDashboardControl.DASHLET_PROPERTY_LABEL,
						label
					);
					settings.setProperty(
						dashletId + "." + WorkspaceDashboardControl.DASHLET_PROPERTY_WIDTH,
						width
					);
					settings.setProperty(
						dashletId + "." + WorkspaceDashboardControl.DASHLET_PROPERTY_ORDERX,
						orderX
					);
					settings.setProperty(
						dashletId + "." + WorkspaceDashboardControl.DASHLET_PROPERTY_ORDERY,
						orderY
					);					
				}
			}		
			settings.setProperty(
				dashboardId + "." + WorkspaceDashboardControl.DASHBOARD_PROPERTY_DASHLETS,
				dashlets
			);
		}
	}
	else if(actionDelete) {
		String dashletId = command.substring(COMMAND_DELETE.length() + 1);
		Properties settings = app.getSettings();
		for(Iterator i = settings.keySet().iterator(); i.hasNext(); ) {
			String propertyName = (String)i.next();
			if(propertyName.startsWith(dashletId)) {
				i.remove();
			}
		}
		String dashboardId = dashletId.substring(0, dashletId.lastIndexOf("."));
		String ids = settings.getProperty(
			dashboardId + "." + WorkspaceDashboardControl.DASHBOARD_PROPERTY_DASHLETS
		);
		if(ids != null) {
			Set<String> idsAsSet = new HashSet<String>(Arrays.asList(ids.split(";")));
			idsAsSet.remove(
				dashletId.substring(dashletId.lastIndexOf(".") + 1)
			);
			if(idsAsSet.isEmpty()) {
				settings.remove(
					dashboardId + "." + WorkspaceDashboardControl.DASHBOARD_PROPERTY_DASHLETS
				);				
			}
			else {
				ids = "";
				for(String id: idsAsSet) {
					if(ids.length() > 0) ids += ";";
					ids += id;
				}
				settings.setProperty(
					dashboardId + "." + WorkspaceDashboardControl.DASHBOARD_PROPERTY_DASHLETS,
					ids
				);
			}
		}
	}
%>
<!--
	<meta name="label" content="Manage Workspace Dashboard">
	<meta name="toolTip" content="Manage Workspace Dashboard">
	<meta name="targetType" content="_inplace">
	<meta name="forClass" content="org:openmdx:base:ContextCapable">
	<meta name="order" content="8889">
-->	
<%
	String providerName = obj.refGetPath().get(2);
	String segmentName = obj.refGetPath().get(4);
	View view = viewsCache.getView(requestId);
	ViewPort p = ViewPortFactory.openPage(
		view,
		request,
		out
	);
%>
<form id="<%= FORM_NAME %>" name="<%= FORM_NAME %>" accept-charset="UTF-8" method="POST" action="<%= servletPath %>">
	<input type="hidden" name="<%= Action.PARAMETER_REQUEST_ID %>" value="<%= requestId %>" />
	<input type="hidden" name="<%= Action.PARAMETER_OBJECTXRI %>" value="<%= objectXri %>" />
	<input type="hidden" id="Command" name="Command" value="" />									
	<table cellspacing="8" class="tableLayout">
		<tr>
			<td class="cellObject">
				<div class="panel" id="panel<%= FORM_NAME %>" style="display:block">
<%
					Control dashboard = view.createControl(
						"WorkspaceDashboardEdit",
						WorkspaceDashboardControl.class
					);
					dashboard.paint(
						p,
						true
					);
					p.flush();
%>
				</div>
				<select name="SelectedDashlet">
<%
					Set<String> paths = this.getServletContext().getResourcePaths("/wizards/Dashboard/");
					for(String path: paths) {
						String dashlet = path.substring(path.lastIndexOf("/") + 1);
						if(dashlet.endsWith(".jsp")) {
							dashlet = dashlet.substring(0, dashlet.indexOf(".jsp"));
						}
%>
						<option value="<%= dashlet %>"><%= dashlet %></option>
<%						
					}
%>				
				</select>
				<input type="submit" class="abutton" name="<%= COMMAND_ADD %>" tabindex="9020" value="+" onclick="javascript:$('Command').value=this.name;" />
				&nbsp;&nbsp;&nbsp;&nbsp;
				<input type="submit" class="abutton" name="<%= COMMAND_APPLY %>" id="<%= COMMAND_APPLY %>" tabindex="9000" value="<%= COMMAND_APPLY %>" onclick="javascript:$('Command').value=this.name;" />
				<input type="submit" class="abutton" name="<%= COMMAND_CANCEL %>" id="<%= COMMAND_CANCEL %> %>" tabindex="9010" value="<%= app.getTexts().getCancelTitle() %>" onclick="javascript:$('Command').value=this.name;" />
			</td>
		</tr>
	</table>
</form>
<script language="javascript" type="text/javascript">
	Event.observe('<%= FORM_NAME %>', 'submit', function(event) {
		$('<%= FORM_NAME %>').request({
			onFailure: function() { },
			onSuccess: function(t) {
				$('UserDialog').update(t.responseText);
			}
		});
		Event.stop(event);
	});		
</script>
<%
if(pm != null) {
	pm.close();
}
p.close(false);
%>
