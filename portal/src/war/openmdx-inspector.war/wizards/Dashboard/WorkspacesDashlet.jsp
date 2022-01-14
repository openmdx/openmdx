<%@  page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %><%
/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: WorkspacesDashlet.jsp
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
org.openmdx.portal.servlet.component.*,
org.openmdx.portal.servlet.control.*,
org.openmdx.portal.servlet.wizards.*,
org.openmdx.base.naming.*
" %><%

	final int MAX_WORKSPACES = 10;
	final String PROPERTY_WORKSPACES = "Workspaces";
	final String PROPERTY_WORKSPACE = "Workspace";
	final String PROPERTY_WORKSPACE_LABEL = "label";
	
	final String COMMAND_APPLY = "Apply";
	final String COMMAND_CANCEL = "Cancel";
	final String COMMAND_EDIT = "Edit";
	final String COMMAND_SELECT = "Select";
	
	request.setCharacterEncoding("UTF-8");
	String servletPath = "." + request.getServletPath();
	String servletPathPrefix = servletPath.substring(0, servletPath.lastIndexOf("/") + 1);

	ApplicationContext app = (ApplicationContext)session.getValue(WebKeys.APPLICATION_KEY);
	Texts_1_0 texts = app.getTexts();
	ViewsCache viewsCache = (ViewsCache)session.getValue(WebKeys.VIEW_CACHE_KEY_SHOW);
	String parameters = request.getParameter(WebKeys.REQUEST_PARAMETER);
  	if(app != null && parameters != null) {
  		
  		// Get parameters
		final String xri = Action.getParameter(parameters, Action.PARAMETER_OBJECTXRI);
		final String requestId = request.getParameter(Action.PARAMETER_REQUEST_ID);
		String dashletId = Action.getParameter(parameters, Action.PARAMETER_ID);
		
		final String FIELD_NAME_COMMAND = dashletId + ".Command";
		final String FIELD_NAME_PARAM = dashletId + ".Param";
		final String FIELD_WORKSPACE_NAME = dashletId + ".Workspace.Name";
		
		// Handle request
		if(xri != null && requestId != null && dashletId != null && viewsCache.getView(requestId) != null) {

		    String submitFormScriptlet = "var form = document.forms['" + dashletId + "Form'];var params = Form.serialize(form);jQuery.ajax({type: 'get', url: './wizards/Dashboard/WorkspacesDashlet.jsp', dataType: 'html', data: params, success: function(data){$('" + dashletId + "Content').innerHTML=data;eval(jQuery(data).find('script').text());eval(jQuery(data).filter('script').text());}});return false;";			

		    String command = request.getParameter(FIELD_NAME_COMMAND);
			if(command == null) command = "";						
			boolean actionApply = COMMAND_APPLY.equals(command);
			boolean actionCancel = COMMAND_CANCEL.equals(command);
			boolean actionSelect = COMMAND_SELECT.equals(command);
			boolean actionEdit = COMMAND_EDIT.equals(command);
			
			Path userHomeAdminIdentity = app.getUserHomeIdentity() == null 
				? null 
				: app.getUserHomeIdentityAsPath().getParent().getDescendant(
					app.getPortalExtension().getAdminPrincipal(app.getCurrentSegment())
				  );
			boolean isAdmin = 
				app.getUserHomeIdentity() == null ||
				app.getUserHomeIdentityAsPath().equals(userHomeAdminIdentity);
		
			Properties settings = app.getSettings();
			if(actionSelect) {
				String workspaceId = request.getParameter(FIELD_NAME_PARAM);
				if(workspaceId != null && !workspaceId.isEmpty()) {
					app.setCurrentWorkspace(workspaceId);
					javax.jdo.PersistenceManager pm = app.getNewPmData();
					RefObject_1_0 obj = (RefObject_1_0)pm.getObjectById(new Path(xri));					
					Action nextAction = new ObjectReference(obj, app).getSelectObjectAction();
					response.sendRedirect(
						request.getContextPath() + "/" + nextAction.getEncodedHRef()
					);
					return;
				}
			}
			else if(actionApply) {
				String workspaceIdsAsString = "";
				for(int i = 0; i < MAX_WORKSPACES; i++) {
					String workspaceId = "W" + i;
					String workspaceName = request.getParameter(workspaceId + "." + FIELD_WORKSPACE_NAME);
					if(i == 0 && (workspaceName == null || workspaceName.isEmpty())) {
						workspaceName = "Default";
					}
					
					if(workspaceName == null || workspaceName.isEmpty()) {
						settings.remove(PROPERTY_WORKSPACE + "." + workspaceId + "." + PROPERTY_WORKSPACE_LABEL);
					} else {
						settings.setProperty(
							PROPERTY_WORKSPACE + "." + workspaceId + "." + PROPERTY_WORKSPACE_LABEL,
							workspaceName
						);
						workspaceIdsAsString += (workspaceIdsAsString.isEmpty() ? "" : ";") + workspaceId;
					}
				}
				settings.setProperty(
					PROPERTY_WORKSPACES,
					workspaceIdsAsString
				);
			}
%>
			<div id="<%= dashletId %>Content" style=>
			<form id="<%= dashletId %>Form" name="<%= dashletId %>Form">
				<input type="hidden" name="<%= Action.PARAMETER_REQUEST_ID %>" value="<%= requestId %>" />
				<input id="<%= WebKeys.REQUEST_PARAMETER %>" name="<%= WebKeys.REQUEST_PARAMETER %>" type="hidden" value="<%= parameters %>" />
				<input type="hidden" name="<%= Action.PARAMETER_OBJECTXRI %>" value="<%= xri %>" />
				<input type="hidden" id="<%= FIELD_NAME_COMMAND %>" name="<%= FIELD_NAME_COMMAND %>" value="" />
				<input type="hidden" id="<%= FIELD_NAME_PARAM %>" name="<%= FIELD_NAME_PARAM %>" value="" />
				<table class="<%= CssClass.tableLayout %>">
					<tr>
						<td class="<%= CssClass.cellObject %>">
							<div id="panel<%= dashletId %>" style="display:block;background-color:inherit;">
<%
								if(isAdmin && actionEdit) {
									for(int i = 0; i < MAX_WORKSPACES; i++) {
										String workspaceId = "W" + i;
										String workspaceName = settings.getProperty(
											PROPERTY_WORKSPACE + "." + workspaceId + "." + PROPERTY_WORKSPACE_LABEL
										);
										if(workspaceName == null) {
											workspaceName = "";
										}
										if(i == 0 && workspaceName.isEmpty()) {
											workspaceName = "Default";
										}
%>				
										<div>
											<%= i %>:<input type="text" name="<%= workspaceId + "." + FIELD_WORKSPACE_NAME %>" value="<%= workspaceName %>" />
										</div>
<%										
									}
%>									
									<div>
										<input type="submit" class="<%= CssClass.btn.toString() + " " + CssClass.btn_light.toString() %>" name="<%= COMMAND_APPLY %>" tabindex="9010" value="<%= texts.getSaveTitle() %>" onclick="javascript:$('<%= FIELD_NAME_COMMAND %>').value='<%= COMMAND_APPLY %>';<%= submitFormScriptlet %>;" />
										<input type="submit" class="<%= CssClass.btn.toString() + " " + CssClass.btn_light.toString() %>" name="<%= COMMAND_CANCEL %>" tabindex="9020" value="<%= texts.getCancelTitle() %>" onclick="javascript:$('<%= FIELD_NAME_COMMAND %>').value='<%= COMMAND_CANCEL %>';<%= submitFormScriptlet %>;" />
									</div>
<%
								}
								else {
									Properties adminSettings = isAdmin ? settings : app.getUserSettings(userHomeAdminIdentity);
									String workspaceIdsAsString = adminSettings.getProperty(
										PROPERTY_WORKSPACES
									);
									List<String> workspaceIds = new ArrayList<String>(
										workspaceIdsAsString == null ? 
											Collections.<String>emptyList() :
												Arrays.asList(workspaceIdsAsString.split(";"))
									);
									if(!workspaceIds.contains("W0")) {
										workspaceIds.add(0, "W0");
									}
									for(int i = 0; i < workspaceIds.size(); i++) {
										String workspaceId = workspaceIds.get(i);
										String workspaceName = adminSettings.getProperty(
											PROPERTY_WORKSPACE + "." + workspaceId + "." + PROPERTY_WORKSPACE_LABEL
										);
										if(workspaceName == null) {
											workspaceName = "";
										}
										if(i == 0 && workspaceName.isEmpty()) {
											workspaceName = "Default";
										}
%>
							            <div><a <%= workspaceId.equals(app.getCurrentWorkspace()) ? "class=\"" + CssClass.hilite + "\"" : "" %> onclick="javascript:$('<%= FIELD_NAME_COMMAND %>').value='<%= COMMAND_SELECT %>';$('<%= FIELD_NAME_PARAM %>').value='<%= workspaceId %>';<%= submitFormScriptlet %>"><%= workspaceName %></a></div>
<%
									}
								}
%>
							</div>
<%
				            if(isAdmin && !actionEdit) {
%>	            	
								<div id="<%= dashletId %>.EditMode">
									<a onclick="javascript:$('<%= FIELD_NAME_COMMAND %>').value='<%= COMMAND_EDIT %>';<%= submitFormScriptlet %>"><img src="./images/<%= WebKeys.ICON_EDIT %>" style="border:0;align:absmiddle;" /></a>
								</div>
<%
	            			}
%>				            
						</td>
					</tr>
				</table>
			</form>
			</div>
<%				
		}
		else {
%>
			<p>
		    <i>WorkspacesDashlet invoked with missing or invalid parameters:</i>
		    <ul>
			    <li><b>RequestId:</b> <%= requestId %></li>
			    <li><b>XRI:</b> <%= xri %></li>
			    <li><b>Dashlet-Id:</b> <%= dashletId %></li>
			</ul>
<%
		}
  	}
%>
