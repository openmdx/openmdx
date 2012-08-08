<%@page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%><%
/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: QuickAccessDashlet.jsp,v 1.16 2009/10/27 15:14:47 wfro Exp $
 * Description: QuickAccessDashlet.jsp
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/10/27 15:14:47 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 *
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
%>
<%@ page session="true" import="
java.util.*,
java.util.zip.*,
java.io.*,
java.text.*,
java.math.*,
java.net.*,
org.openmdx.base.naming.*,
org.openmdx.portal.servlet.*,
org.openmdx.portal.servlet.view.*,
org.openmdx.portal.servlet.texts.*,
org.openmdx.portal.servlet.control.*,
org.openmdx.kernel.log.*
" %>
<%
	final String COMMAND_ADD_MENU_ENTRY = "AddMenuEntry";
	final String COMMAND_DELETE_MENU_ENTRY = "DeleteMenuEntry";
	final String COMMAND_SELECT_EDIT_MODE = "SelectEditMode";
	
	ApplicationContext app = (ApplicationContext)session.getValue(WebKeys.APPLICATION_KEY);
	Texts_1_0 texts = app.getTexts();
	ViewsCache viewsCache = (ViewsCache)session.getValue(WebKeys.VIEW_CACHE_KEY_SHOW);
	String parameters = request.getParameter(WebKeys.REQUEST_PARAMETER);
  	if(app != null && parameters != null) {
  		
  		// Get parameters
		final String xri = Action.getParameter(parameters, Action.PARAMETER_OBJECTXRI);
		final String requestId = request.getParameter(Action.PARAMETER_REQUEST_ID);
		String dashletId = Action.getParameter(parameters, Action.PARAMETER_ID);
		boolean isSharedDashlet = false;
		if(dashletId != null && dashletId.startsWith(DashboardControl.SHARED_DASHLET_MARKER)) {
			isSharedDashlet = true;
		}
		
		final String FIELD_NAME_TARGET_XRI = dashletId + ".TargetXri";
		final String FIELD_NAME_FUNCTION = dashletId + ".Function";
		final String FIELD_NAME_FUNCTION_SELECT = dashletId + ".FunctionSelect";
		final String FIELD_NAME_COMMAND = dashletId + ".Command";		
		
		final String command = request.getParameter(FIELD_NAME_COMMAND);
		String selectedTargetXri = request.getParameter(FIELD_NAME_TARGET_XRI);
		String selectedTargetXriTitle = request.getParameter(FIELD_NAME_TARGET_XRI + ".Title");
		String selectedFunction = request.getParameter(FIELD_NAME_FUNCTION_SELECT);
		
		// Handle request
		if(xri != null && requestId != null && dashletId != null && viewsCache.getView(requestId) != null) {
			ShowObjectView view = (ShowObjectView)viewsCache.getView(requestId);
			String editModeScriptlet = "$('" + dashletId + ".AddMenuEntryFieldSet').style.display='block';$('" + dashletId + ".EditMode').style.display='none';";
		    String submitFormScriptlet = "var form = document.forms['" + dashletId + "Form'];var params = Form.serialize(form);new Ajax.Updater('" + dashletId + "Content', './wizards/Dashboard/QuickAccessDashlet.jsp', {asynchronous:true, evalScripts: true, parameters: params, onComplete: function(){}});return false;";
	        boolean isEditMode = COMMAND_SELECT_EDIT_MODE.equals(command) || (selectedTargetXri != null && selectedTargetXri.length() > 0);
%>			
			<div id="<%= dashletId %>Content" style="padding:5px;">
			<form id="<%= dashletId %>Form" name="<%= dashletId %>Form" />
				<input id="<%= WebKeys.REQUEST_ID %>" name="<%= Action.PARAMETER_REQUEST_ID %>" type="hidden" value="<%= requestId %>" />
				<input id="<%= WebKeys.REQUEST_PARAMETER %>" name="<%= WebKeys.REQUEST_PARAMETER %>" type="hidden" value="<%= parameters %>" />
				<input id="<%= FIELD_NAME_COMMAND %>" name="<%= FIELD_NAME_COMMAND %>" type="hidden" value=""/>
           		<input id="<%= FIELD_NAME_FUNCTION %>" name="<%= FIELD_NAME_FUNCTION %>" type="hidden" value="" />
				<table width="100%">
					<tr>
						<th />
						<th width="5%" />
					</tr>
					<tr>
						<td>					
<%			
							// Menu
							Set<String> menuEntries = new TreeSet<String>();
							Properties settings = null;
							if(isSharedDashlet) {
								settings = app.getUserSettings(
									app.getUserHomeIdentity().getParent().getDescendant(
										app.getRoleMapper().getAdminPrincipal(app.getCurrentSegment())
									)
								);	
							}
							else {
								settings = app.getSettings();
							}
							String propertyNamePrefix = isSharedDashlet ? dashletId.substring(1) : dashletId;
							for(int i = 0; i < 100; i++) {
								if(settings.getProperty(propertyNamePrefix + "." + i) != null) {
									menuEntries.add(settings.getProperty(propertyNamePrefix + "." + i));
								}
							}
							// ADD_MENU_ENTRY
							if(COMMAND_ADD_MENU_ENTRY.equals(command) && selectedTargetXri != null && selectedFunction != null) {
								int freeIndex = -1;
								for(int i = 0; i < 100; i++) {
									if(settings.getProperty(propertyNamePrefix + "." + i) == null) {
										freeIndex = i;
										break;
									}
								}
								if(freeIndex >= 0) {
									String function = selectedTargetXri + " > " + selectedFunction; 
									settings.setProperty(
										propertyNamePrefix + "." + freeIndex,
										function
									);
									menuEntries.add(function);
								}
								selectedTargetXri = null;
								selectedTargetXriTitle = null;
							}
							// DELETE_MENU_ENTRY
							else if(COMMAND_DELETE_MENU_ENTRY.equals(command) && selectedTargetXri != null) {
								String function = new Path(selectedTargetXri).toXRI() + " > " + request.getParameter(FIELD_NAME_FUNCTION);
								if(function != null && function.length() > 0) {
									for(int i = 0; i < 100; i++) {
										if(function.equals(settings.getProperty(propertyNamePrefix + "." + i))) {
											settings.remove(propertyNamePrefix + "." + i);
											break;
										}
									}									
								}
								menuEntries.remove(function);
								selectedTargetXri = null;
								selectedTargetXriTitle = null;
							}
							Path previousTargetXri = null;
%>
							<table>
<%							
								for(String menuEntry: menuEntries) {
									Path targetXri = new Path(menuEntry.substring(0, menuEntry.indexOf(" > ")));
									String function = menuEntry.substring(menuEntry.indexOf(" > ") + 3);
									ShowObjectView targetView = new ShowObjectView(
										view.getId(),
										(String)null,
										targetXri,
										app,
										new HashMap(),
										(String)null,
										Collections.emptyMap()
									);
									if(function.startsWith("Operation.")) {
										String operationName = function.substring(10);
										List<OperationPaneControl> operationPaneControls = new ArrayList<OperationPaneControl>();
										// Operations
										for(OperationPaneControl paneControl: targetView.getShowInspectorControl().getOperationPaneControl()) {
											for(OperationTabControl tabControl: paneControl.getOperationTabControl()) {
												if(tabControl.getQualifiedOperationName().equals(operationName)) {
													String tabId = Integer.toString(tabControl.getTabId());
													Action action = null;
													if(tabControl instanceof WizardTabControl) {
														WizardTabControl wizardTabControl = (WizardTabControl)tabControl;
														if(wizardTabControl.isInplace()) {
															String script = "$('UserDialogWait').className='loading udwait';new Ajax.Updater('UserDialog', '." + tabControl.getName() + "?requestId=REQUEST_ID&xri=" + targetXri + "', {evalScripts: true});";
															action = new Action(
																Action.EVENT_MACRO,
																new Action.Parameter[]{
																    new Action.Parameter(Action.PARAMETER_OBJECTXRI, targetXri.toXri()),
																    new Action.Parameter(Action.PARAMETER_NAME, org.openmdx.base.text.conversion.Base64.encode(script.getBytes("UTF-8"))),
																    new Action.Parameter(Action.PARAMETER_TYPE, Integer.toString(Action.MACRO_TYPE_JAVASCRIPT))                    
																},
																tabControl.getToolTip(),
																tabControl.getToolTip(),
																true
															);
														}
														else {
															String script = "window.location.href=$('op" + tabId + "Trigger').href;";
															action = new Action(
																Action.EVENT_MACRO,
																new Action.Parameter[]{
																    new Action.Parameter(Action.PARAMETER_OBJECTXRI, targetXri.toXri()),
																    new Action.Parameter(Action.PARAMETER_NAME, org.openmdx.base.text.conversion.Base64.encode(script.getBytes("UTF-8"))),
																    new Action.Parameter(Action.PARAMETER_TYPE, Integer.toString(Action.MACRO_TYPE_JAVASCRIPT))                    
																},
																tabControl.getToolTip(),
																tabControl.getToolTip(),
																true
															);
														}
													}
													else {
														String script = "eval($('op" + tabId + "Trigger').getAttribute('onclick'));";
														action = new Action(
															Action.EVENT_MACRO,
															new Action.Parameter[]{
															    new Action.Parameter(Action.PARAMETER_OBJECTXRI, targetXri.toXri()),
															    new Action.Parameter(Action.PARAMETER_NAME, org.openmdx.base.text.conversion.Base64.encode(script.getBytes("UTF-8"))),
															    new Action.Parameter(Action.PARAMETER_TYPE, Integer.toString(Action.MACRO_TYPE_JAVASCRIPT))                    
															},
															tabControl.getName(),
															tabControl.getToolTip(),
															true
														);
													}
%>						
													<tr>
<%
														if(targetXri.equals(previousTargetXri)) {
%>
															<td />
															<td />															
<%															
														}
														else {
%>
															<td><img src="images/<%= targetView.getObjectReference().getIconKey() %>" />&nbsp;&nbsp;</td>																	
								                    		<td><i><%= targetView.getObjectReference().getTitle() %></i></td>
<%
														}
%>								                    		                    	
								                    	<td>&nbsp;&nbsp;&gt;&nbsp;&nbsp;</td>                    	
									            		<td><a href="#" onmouseover="javascript:this.href=<%= view.getEvalHRef(action) %>;onmouseover=function(){};"><%= action.getTitle() %></a></td>
<%
														if(isEditMode) {
%>									            		
									            			<td><a onclick="javascript:$('<%= FIELD_NAME_COMMAND %>').value='<%= COMMAND_DELETE_MENU_ENTRY %>';$('<%= FIELD_NAME_TARGET_XRI %>').value='<%= targetXri %>';$('<%= FIELD_NAME_FUNCTION %>').value='<%= function %>';<%= submitFormScriptlet %>"><img src="./images/collapse.gif" /></a></td>
<%														}
														else {
%>
															<td />
<%																														
														}
%>									            			
									            	</tr>                    	
<%
													previousTargetXri = targetXri;
												}
											}
										}
										// Wizards
										for(WizardTabControl tabControl: targetView.getShowInspectorControl().getWizardControl().getWizardTabControls()) {
											String tabId = Integer.toString(tabControl.getTabId());
											if(tabControl.getQualifiedOperationName().equals(operationName)) {
												Action action = null;
												if(tabControl.isInplace()) {
													String script = "$('UserDialogWait').className='loading udwait';new Ajax.Updater('UserDialog', '." + tabControl.getName() + "?requestId=REQUEST_ID&xri=" + targetXri + "', {evalScripts: true});";
													action = new Action(
														Action.EVENT_MACRO,
														new Action.Parameter[]{
														    new Action.Parameter(Action.PARAMETER_OBJECTXRI, targetXri.toXri()),
														    new Action.Parameter(Action.PARAMETER_NAME, org.openmdx.base.text.conversion.Base64.encode(script.getBytes("UTF-8"))),
														    new Action.Parameter(Action.PARAMETER_TYPE, Integer.toString(Action.MACRO_TYPE_JAVASCRIPT))                    
														},
														tabControl.getToolTip(),
														tabControl.getToolTip(),
														true
													);
												}
												else {
													String script = "window.location.href=$('op" + tabId + "Trigger').href;";
													action = new Action(
														Action.EVENT_MACRO,
														new Action.Parameter[]{
														    new Action.Parameter(Action.PARAMETER_OBJECTXRI, targetXri.toXri()),
														    new Action.Parameter(Action.PARAMETER_NAME, org.openmdx.base.text.conversion.Base64.encode(script.getBytes("UTF-8"))),
														    new Action.Parameter(Action.PARAMETER_TYPE, Integer.toString(Action.MACRO_TYPE_JAVASCRIPT))                    
														},
														tabControl.getToolTip(),
														tabControl.getToolTip(),
														true
													);
												}
%>						
												<tr>
<%
													if(targetXri.equals(previousTargetXri)) {
%>
														<td />
														<td />															
<%															
													}
													else {
%>
														<td><img src="images/<%= targetView.getObjectReference().getIconKey() %>" />&nbsp;&nbsp;</td>																	
							                    		<td><i><%= targetView.getObjectReference().getTitle() %></i></td>
<%
													}
%>								                    		                    	
							                    	<td>&nbsp;&nbsp;&gt;&nbsp;&nbsp;</td>                    	
								            		<td><a href="#" onmouseover="javascript:this.href=<%= view.getEvalHRef(action) %>;onmouseover=function(){};"><%= action.getTitle() %></a></td>
<%
													if(isEditMode) {
%>									            		
								            			<td><a onclick="javascript:$('<%= FIELD_NAME_COMMAND %>').value='<%= COMMAND_DELETE_MENU_ENTRY %>';$('<%= FIELD_NAME_TARGET_XRI %>').value='<%= targetXri %>';$('<%= FIELD_NAME_FUNCTION %>').value='<%= function %>';<%= submitFormScriptlet %>"><img src="./images/collapse.gif" /></a></td>
<%
													}
													else {
%>
														<td />
<%																														
													}
%>									            			
								            	</tr>                    	
<%
												previousTargetXri = targetXri;
											}
										}
									}
									else if(function.startsWith("Reference.")) {
										String referenceName = function.substring(10);
										for(ReferencePane pane: targetView.getReferencePane()) {
											for(Action action: pane.getReferencePaneControl().getSelectReferenceAction()) {
												if(referenceName.equals(action.getParameter(Action.PARAMETER_REFERENCE_NAME))) {
													List<Action.Parameter> actionParams = new ArrayList<Action.Parameter>(Arrays.asList(action.getParameters()));
													actionParams.add(
														new Action.Parameter(Action.PARAMETER_OBJECTXRI, targetXri.toXri())
													);
													Action selectObjectAndReferenceAction = new Action(
														Action.EVENT_SELECT_OBJECT,
														actionParams.toArray(new Action.Parameter[actionParams.size()]),
														action.getTitle(),
														action.getIconKey(),
														action.isEnabled()
													);
%>													
													<tr>		
<%
														if(targetXri.equals(previousTargetXri)) {
%>
															<td />
															<td />															
<%															
														}
														else {
%>
															<td><img src="images/<%= targetView.getObjectReference().getIconKey() %>" />&nbsp;&nbsp;</td>																	
									                    	<td><i><%= targetView.getObjectReference().getTitle() %></i></td>
<%
														}
%>									                    	                    	
								                    	<td>&nbsp;&nbsp;&gt;&nbsp;&nbsp;</td>                    	
								                    	<td><a href="#" onmouseover="javascript:this.href=<%= view.getEvalHRef(selectObjectAndReferenceAction) %>;onmouseover=function(){};"><%= texts.getSearchText() + " " + (action.getTitle().startsWith(WebKeys.TAB_GROUPING_CHARACTER) ? action.getTitle().substring(1) : action.getTitle()) %></a></td>
<%
														if(isEditMode) {
%>								                    	
									            			<td><a onclick="javascript:$('<%= FIELD_NAME_COMMAND %>').value='<%= COMMAND_DELETE_MENU_ENTRY %>';$('<%= FIELD_NAME_TARGET_XRI %>').value='<%= targetXri %>';$('<%= FIELD_NAME_FUNCTION %>').value='<%= function %>';<%= submitFormScriptlet %>"><img src="./images/collapse.gif" /></a></td>
<%
														}
														else {
%>
															<td />
<%																														
														}
%>									            			
								                    </tr>               	
<%
													previousTargetXri = targetXri;
												}						
											}
										}
									}
								}
%>
							</table>
						</td>
						<td valign="bottom" align="right">
<%						
				            if(!isEditMode && !isSharedDashlet) {
%>	            	
								<div id="<%= dashletId %>.EditMode">
									<a onclick="javascript:$('<%= FIELD_NAME_COMMAND %>').value='<%= COMMAND_SELECT_EDIT_MODE %>';<%= submitFormScriptlet %>"><img src="./images/<%= WebKeys.ICON_EDIT %>" style="border:0;align:absmiddle;" /></a>
								</div>
<%
	            			}
%>						
						</td>
					</tr>
					<tr>
						<td colspan="2">
<%				
							// Edit area
							List<String> options = new ArrayList<String>();
							if(selectedTargetXri != null && selectedTargetXri.length() > 0) {
								ShowObjectView targetView = new ShowObjectView(
									view.getId(),
									(String)null,
									new Path(selectedTargetXri),
									app,
									new HashMap(),
									(String)null,
									Collections.emptyMap()
								);
								// Operations
								for(OperationPaneControl paneControl: targetView.getShowInspectorControl().getOperationPaneControl()) {
									for(OperationTabControl tabControl: paneControl.getOperationTabControl()) {		
										String tabId = Integer.toString(tabControl.getTabId());
										options.add(
											"<option value=\"Operation." + tabControl.getQualifiedOperationName() + "\">" + paneControl.getToolTip() + " &gt; " + tabControl.getToolTip() + "</option>"
										);
									}
								}
								// Wizards
								for(WizardTabControl tabControl: targetView.getShowInspectorControl().getWizardControl().getWizardTabControls()) {
									String tabId = Integer.toString(tabControl.getTabId());
									options.add(
										"<option value=\"Operation." + tabControl.getQualifiedOperationName() + "\">Wizards &gt; " + tabControl.getToolTip() + "</option>"
									);
								}
								// Grids
								for(ReferencePane pane: targetView.getReferencePane()) {
									for(Action action: pane.getReferencePaneControl().getSelectReferenceAction()) {
										options.add(
											"<option value=\"Reference." + action.getParameter(Action.PARAMETER_REFERENCE_NAME) + "\">" + texts.getSearchText() + " " + (action.getTitle().startsWith(WebKeys.TAB_GROUPING_CHARACTER) ? action.getTitle().substring(1) : action.getTitle()) + "</option>"
										);
									}
								}
							}					
				            String lookupId = org.openmdx.kernel.id.UUIDs.getGenerator().next().toString();	            
				            Action findObjectAction = view.getFindObjectAction(
				                "org:openmdx:base:ContextCapable", 
				                lookupId
				            );
%>					
							<fieldset id="<%= dashletId %>.AddMenuEntryFieldSet" style="display:<%= isEditMode ? "block" : "none" %>">
								<table width="100%">
									<tr>
										<td>					 				
						                	<input id="<%= FIELD_NAME_TARGET_XRI %>.Title" name="<%= FIELD_NAME_TARGET_XRI %>.Title" type="text" class="valueL mandatory" readonly value="<%= selectedTargetXriTitle == null ? "" : selectedTargetXriTitle %>" />
						                    <input id="<%= FIELD_NAME_TARGET_XRI %>" name="<%= FIELD_NAME_TARGET_XRI %>" type="hidden" class="valueLLocked" value="<%= selectedTargetXri == null ? "" : selectedTargetXri %>" onchange="javascript:<%= submitFormScriptlet %>" />
						                </td>
						                <td class="addon">
				                    		<img class="popUpButton" border="0" alt="Click to open ObjectFinder" src="images/<%= findObjectAction.getIconKey() %>" onclick="javascript:OF.findObject(<%= view.getEvalHRef(findObjectAction) %>, $('<%= FIELD_NAME_TARGET_XRI %>.Title'), $('<%= FIELD_NAME_TARGET_XRI %>'), '<%= lookupId %>');" />
				                    	</td>
				                    	<td>
<%
											if(!options.isEmpty()) {
%>	                    
					                    		<select id="<%= FIELD_NAME_FUNCTION_SELECT %>" name="<%= FIELD_NAME_FUNCTION_SELECT %>" class="valueL">
<%
													for(String option: options) {
%>
														<%= option %>
<%
													}
%>
												</select>
<%
											}
%>					
										</td>
										<td align="right">
<%
											if(selectedTargetXri != null) {
%>								
												&nbsp;&nbsp;&nbsp;<a class="abutton" onclick="javascript:$('<%= FIELD_NAME_COMMAND %>').value='<%= COMMAND_ADD_MENU_ENTRY %>';<%= submitFormScriptlet %>">&nbsp;&nbsp;+&nbsp;&nbsp;</a>
<%
											}
%>									
											&nbsp;&nbsp;&nbsp;<a class="abutton" onclick="javascript:$('<%= FIELD_NAME_TARGET_XRI %>').value='';$('<%= FIELD_NAME_TARGET_XRI %>.Title').value='';<%= submitFormScriptlet %>"><%= texts.getCancelTitle() %></a>
										</td>
									</tr>		
								</table>	                                        			
							</fieldset>
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
		    <i>QuickAccessDashlet invoked with missing or invalid parameters:</i>
		    <ul>
			    <li><b>RequestId:</b> <%= requestId %></li>
			    <li><b>XRI:</b> <%= xri %></li>
			    <li><b>Dashlet-Id:</b> <%= dashletId %></li>
			</ul>
<%
		}
  	}
%>
