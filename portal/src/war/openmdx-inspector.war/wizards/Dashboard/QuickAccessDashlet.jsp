<%@page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%><%
/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: QuickAccessDashlet.jsp
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 *
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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
org.openmdx.kernel.exception.*,
org.openmdx.base.accessor.jmi.cci.*,
org.openmdx.kernel.id.*,
org.openmdx.base.exception.*,
org.openmdx.portal.servlet.*,
org.openmdx.portal.servlet.component.*,
org.openmdx.portal.servlet.control.*,
org.openmdx.portal.servlet.action.*,
org.openmdx.base.text.conversion.*,
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
		    String submitFormScriptlet = "var form = document.forms['" + dashletId + "Form'];var params = Form.serialize(form);jQuery.ajax({type: 'get', url: './wizards/Dashboard/QuickAccessDashlet.jsp', dataType: 'html', data: params, success: function(data){$('" + dashletId + "Content').innerHTML=data;eval(jQuery(data).find('script').text());eval(jQuery(data).filter('script').text());}});return false;";
	        boolean isEditMode = COMMAND_SELECT_EDIT_MODE.equals(command) || (selectedTargetXri != null && selectedTargetXri.length() > 0);
%>			
			<div id="<%= dashletId %>Content">
			<form id="<%= dashletId %>Form" name="<%= dashletId %>Form" />
				<input id="<%= WebKeys.REQUEST_ID %>" name="<%= Action.PARAMETER_REQUEST_ID %>" type="hidden" value="<%= requestId %>" />
				<input id="<%= WebKeys.REQUEST_PARAMETER %>" name="<%= WebKeys.REQUEST_PARAMETER %>" type="hidden" value="<%= parameters %>" />
				<input id="<%= FIELD_NAME_COMMAND %>" name="<%= FIELD_NAME_COMMAND %>" type="hidden" value=""/>
           		<input id="<%= FIELD_NAME_FUNCTION %>" name="<%= FIELD_NAME_FUNCTION %>" type="hidden" value="" />
						<div>					
<%			
							// Menu
							Set<String> menuEntries = new TreeSet<String>();
							Properties settings = null;
							if(isSharedDashlet) {
								settings = app.getUserSettings(
									app.getUserHomeIdentityAsPath().getParent().getDescendant(
										app.getPortalExtension().getAdminPrincipal(app.getCurrentSegment())
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
							<div class="<%= CssClass.sidebarNav %>">
							<ul class="<%= CssClass.nav + " " + CssClass.navList %>">
<%							
								for(String menuEntry: menuEntries) {
									try {
										Path targetXri = new Path(menuEntry.substring(0, menuEntry.indexOf(" > ")));
										String function = menuEntry.substring(menuEntry.indexOf(" > ") + 3);
										ShowObjectView targetView = new ShowObjectView(
											view.getId(),
											(String)null,
											(RefObject_1_0)app.getNewPmData().getObjectById(targetXri),
											app,
											new HashMap(),
											null, // nextPrevActions
											(String)null, // lookupType
											(String)null, // resourcePathPrefix
											(String)null, // navigationTarget
											(Boolean)null // isReadOnly
										);
										if(function.startsWith("Operation.")) {
											String operationName = function.substring(10);
											List<OperationPaneControl> operationPaneControls = new ArrayList<OperationPaneControl>();
											// Operations
											for(OperationPaneControl paneControl: targetView.getControl().getChildren(OperationPaneControl.class)) {
												for(UiOperationTabControl tabControl: paneControl.getChildren(UiOperationTabControl.class)) {
													if(tabControl.getQualifiedOperationName().equals(operationName)) {
														String tabId = Integer.toString(tabControl.getTabId());
														Action action = null;
														String target = "";
														if(tabControl instanceof UiWizardTabControl) {
															UiWizardTabControl wizardTabControl = (UiWizardTabControl)tabControl;
															if(wizardTabControl.isInplace()) {
																String script = "$('UserDialogWait').className='loading udwait';jQuery.ajax({type: 'get', url: '." + tabControl.getOperationName() + "?requestId=REQUEST_ID&xri=" + targetXri + "', dataType: 'html', success: function(data){$('UserDialog').innerHTML=data;eval(jQuery(data).find('script').text());eval(jQuery(data).filter('script').text());}});";
																action = new Action(
																	MacroAction.EVENT_ID,
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
																String script = "window.location.href=$('op" + tabId + "').href;";
																action = new Action(
																	MacroAction.EVENT_ID,
																	new Action.Parameter[]{
																		  new Action.Parameter(Action.PARAMETER_OBJECTXRI, targetXri.toXri()),
																		  new Action.Parameter(Action.PARAMETER_NAME, org.openmdx.base.text.conversion.Base64.encode(script.getBytes("UTF-8"))),
																		  new Action.Parameter(Action.PARAMETER_TYPE, Integer.toString(Action.MACRO_TYPE_JAVASCRIPT))                    
																	},
																	tabControl.getToolTip(),
																	tabControl.getToolTip(),
																	true
																);
																target = "_blank".equals(wizardTabControl.getWizardDefinition().getTargetType()) ? "target=\"_blank\"" : "target=\"_self\"";
															}
														}
														else {
															String script = "$('op" + tabId + "').onclick();";
															action = new Action(
																MacroAction.EVENT_ID,
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
														if(!targetXri.equals(previousTargetXri)) {
															List<Action.Parameter> actionParamsT = new ArrayList<Action.Parameter>();
															actionParamsT.add(
																new Action.Parameter(Action.PARAMETER_OBJECTXRI, targetXri.toXri())
															);
															Action selectObjectAndReferenceActionT = new Action(
																SelectObjectAction.EVENT_ID,
																actionParamsT.toArray(new Action.Parameter[actionParamsT.size()]),
																null, //action.getTitle(),
																null, //action.getIconKey(),
																true  //action.isEnabled()
															);
															String targetHref = selectObjectAndReferenceActionT.getEncodedHRef();
															String toolTip = targetView.getObjectReference().getTitle();
															if (toolTip != null) {
																toolTip = toolTip.replace("\"", "'");
															} else {
															  toolTip = "--";
															}
%>
															<li class="<%= CssClass.navHeader %>"><a href='<%= targetHref %>' style="padding:2px;"><b><%= targetView.getObjectReference().getTitle() %></b></a></li>
<%
														}
%>
														<li <%= !isEditMode && function.compareTo("Operation.org:openmdx:base:BasicObject:reload") == 0 ? "style='display:none;'" : "" %>>
															<span>
<%
															if(isEditMode) {
%>									            		
																<a style="padding:2px;" onclick="javascript:$('<%= FIELD_NAME_COMMAND %>').value='<%= COMMAND_DELETE_MENU_ENTRY %>';$('<%= FIELD_NAME_TARGET_XRI %>').value='<%= targetXri %>';$('<%= FIELD_NAME_FUNCTION %>').value='<%= function %>';<%= submitFormScriptlet %>"><img src="./images/collapse.gif" /></a>
<%															
															}
%>									            			
															<a <%= target %> href="#" style="padding:2px;font-size:90%;" onmouseover="javascript:this.href=<%= view.getEvalHRef(action) %>;onmouseover=function(){};"><%= action.getTitle() %></a>
															</span>
														</li>
<%
														previousTargetXri = targetXri;
													}
												}
											}
											// Wizards
											for(UiWizardTabControl tabControl: targetView.getControl().getChildren(WizardControl.class).get(0).getChildren(UiWizardTabControl.class)) {
												String tabId = Integer.toString(tabControl.getTabId());
												if(tabControl.getQualifiedOperationName().equals(operationName)) {
													Action action = null;
													if(tabControl.isInplace()) {
														String script = "$('UserDialogWait').className='loading udwait';jQuery.ajax({type: 'get', url: '." + tabControl.getOperationName() + "?requestId=REQUEST_ID&xri=" + targetXri + "', dataType: 'html', success: function(data){$('UserDialog').innerHTML=data;eval(jQuery(data).find('script').text());eval(jQuery(data).filter('script').text());}});";
														action = new Action(
															MacroAction.EVENT_ID,
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
														String script = "window.location.href=$('op" + tabId + "').href;";
														action = new Action(
															MacroAction.EVENT_ID,
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
													if(!targetXri.equals(previousTargetXri)) {
															List<Action.Parameter> actionParamsT = new ArrayList<Action.Parameter>();
															actionParamsT.add(
																new Action.Parameter(Action.PARAMETER_OBJECTXRI, targetXri.toXri())
															);
															Action selectObjectAndReferenceActionT = new Action(
																SelectObjectAction.EVENT_ID,
																actionParamsT.toArray(new Action.Parameter[actionParamsT.size()]),
																null, //action.getTitle(),
																null, //action.getIconKey(),
																true  //action.isEnabled()
															);
															String targetHref = selectObjectAndReferenceActionT.getEncodedHRef();
															String toolTip = targetView.getObjectReference().getTitle();
															if (toolTip != null) {
																toolTip = toolTip.replace("\"", "'");
															} else {
															  toolTip = "--";
															}
%>
														<li class="<%= CssClass.navHeader %>"><a href='<%= targetHref%>' style="padding:2px;"><b><%= targetView.getObjectReference().getTitle() %></b></a></li>
<%
													}
%>								                    
													<li>
														<span>
<%
														if(isEditMode) {
%>									            		
									          				<a style="padding:2px;" onclick="javascript:$('<%= FIELD_NAME_COMMAND %>').value='<%= COMMAND_DELETE_MENU_ENTRY %>';$('<%= FIELD_NAME_TARGET_XRI %>').value='<%= targetXri %>';$('<%= FIELD_NAME_FUNCTION %>').value='<%= function %>';<%= submitFormScriptlet %>"><img src="./images/collapse.gif" /></a>
<%
														}
														String target = "_blank".equals(tabControl.getWizardDefinition().getTargetType()) ? "target=\"_blank\"" : "target=\"_self\"";
%>
									          			<a href="#" <%= target %> style="padding:2px;font-size:90%;" onmouseover="javascript:this.href=<%= view.getEvalHRef(action) %>;onmouseover=function(){};"><%= action.getTitle() %></a>
														</span>
									          		</li>
<%
													previousTargetXri = targetXri;
												}
											}
										}
										else if(function.startsWith("Reference.")) {
											String referenceName = function.substring(10);
											for(ReferencePane pane: targetView.getChildren(ReferencePane.class)) {
												for(Action action: pane.getSelectReferenceAction()) {
													if(referenceName.equals(action.getParameter(Action.PARAMETER_REFERENCE_NAME))) {
														List<Action.Parameter> actionParams = new ArrayList<Action.Parameter>(Arrays.asList(action.getParameters()));
														actionParams.add(
															new Action.Parameter(Action.PARAMETER_OBJECTXRI, targetXri.toXri())
														);
														Action selectObjectAndReferenceAction = new Action(
															SelectObjectAction.EVENT_ID,
															actionParams.toArray(new Action.Parameter[actionParams.size()]),
															action.getTitle(),
															action.getIconKey(),
															action.isEnabled()
														);
														if(!targetXri.equals(previousTargetXri)) {
															List<Action.Parameter> actionParamsT = new ArrayList<Action.Parameter>();
															actionParamsT.add(
																new Action.Parameter(Action.PARAMETER_OBJECTXRI, targetXri.toXri())
															);
															Action selectObjectAndReferenceActionT = new Action(
																SelectObjectAction.EVENT_ID,
																actionParamsT.toArray(new Action.Parameter[actionParamsT.size()]),
																null, //action.getTitle(),
																null, //action.getIconKey(),
																true  //action.isEnabled()
															);
															String targetHref = selectObjectAndReferenceActionT.getEncodedHRef();
															String toolTip = targetView.getObjectReference().getTitle();
															if (toolTip != null) {
																toolTip = toolTip.replace("\"", "'");
															} else {
															  toolTip = "--";
															}
%>
															<li class="<%= CssClass.navHeader %>"><a href='<%= targetHref %>' style="padding:2px;"><b><%= targetView.getObjectReference().getTitle() %></b></a></li>
<%
														}
%>
														<li>
															<span>
<%
															if(isEditMode) {
%>								                    	
										          				<a style="padding:2px;" onclick="javascript:$('<%= FIELD_NAME_COMMAND %>').value='<%= COMMAND_DELETE_MENU_ENTRY %>';$('<%= FIELD_NAME_TARGET_XRI %>').value='<%= targetXri %>';$('<%= FIELD_NAME_FUNCTION %>').value='<%= function %>';<%= submitFormScriptlet %>"><img src="./images/collapse.gif" /></a>
<%
															}
%>									            			
									                  		<a href="#" style="padding:2px;font-size:90%;" onmouseover="javascript:this.href=<%= view.getEvalHRef(selectObjectAndReferenceAction) %>;onmouseover=function(){};"><%= (action.getTitle().startsWith(WebKeys.TAB_GROUPING_CHARACTER) ? action.getTitle().substring(1) : action.getTitle()) %></a>
															</span>
									                  	</li>
<%
														previousTargetXri = targetXri;
													}
												}
											}
										}
									} catch (Exception e) {
										ServiceException e0 = new ServiceException(e);
										if(e0.getExceptionCode() != BasicException.Code.AUTHORIZATION_FAILURE) {
											e0.log();
										}
									}
								}
%>
							</ul>
							</div>
						</div>
						<div>
<%						
				            if(!isEditMode && !isSharedDashlet) {
%>	            	
								<div id="<%= dashletId %>.EditMode">
									<a onclick="javascript:$('<%= FIELD_NAME_COMMAND %>').value='<%= COMMAND_SELECT_EDIT_MODE %>';<%= submitFormScriptlet %>"><img src="./images/<%= WebKeys.ICON_EDIT %>" style="border:0;align:absmiddle;" /></a>
								</div>
<%
	            			}
%>						
						</div>
						<div>
<%				
							// Edit area
							List<String> options = new ArrayList<String>();
							if(selectedTargetXri != null && selectedTargetXri.length() > 0) {
								ShowObjectView targetView = new ShowObjectView(
									view.getId(),
									(String)null,
									(RefObject_1_0)app.getNewPmData().getObjectById(new Path(selectedTargetXri)),
									app,
									new HashMap(),
									null, // nextPrevActions
									(String)null, // lookupType
									(String)null, // resourcePathPrefix
									(String)null, // navigationTarget
									(Boolean)null // isReadOnly
								);
								// Operations
								for(OperationPaneControl paneControl: targetView.getControl().getChildren(OperationPaneControl.class)) {
									for(UiOperationTabControl tabControl: paneControl.getChildren(UiOperationTabControl.class)) {		
										String tabId = Integer.toString(tabControl.getTabId());
										options.add(
											"<option value=\"Operation." + tabControl.getQualifiedOperationName() + "\">" + paneControl.getToolTip() + " &gt; " + tabControl.getToolTip() + "</option>"
										);
									}
								}
								// Wizards
								for(UiWizardTabControl tabControl: targetView.getControl().getChildren(WizardControl.class).get(0).getChildren(UiWizardTabControl.class)) {
									String tabId = Integer.toString(tabControl.getTabId());
									options.add(
										"<option value=\"Operation." + tabControl.getQualifiedOperationName() + "\">Wizards &gt; " + tabControl.getToolTip() + "</option>"
									);
								}
								// Grids
								for(ReferencePane pane: targetView.getChildren(ReferencePane.class)) {
									for(Action action: pane.getSelectReferenceAction()) {
										options.add(
											"<option value=\"Reference." + action.getParameter(Action.PARAMETER_REFERENCE_NAME) + "\">" + texts.getSearchText() + " " + (action.getTitle().startsWith(WebKeys.TAB_GROUPING_CHARACTER) ? action.getTitle().substring(1) : action.getTitle()) + "</option>"
										);
									}
								}
							}
				            String lookupId = UUIDConversion.toUID(UUIDs.newUUID());	            
				            Action findObjectAction = view.getFindObjectAction(
				                "org:openmdx:base:ContextCapable", 
				                lookupId
				            );
%>					
							<fieldset id="<%= dashletId %>.AddMenuEntryFieldSet" style="display:<%= isEditMode ? "block" : "none" %>">
								<table width="100%">
									<tr>
						                <td>
				                    		<img class="<%= CssClass.popUpButton %>" border="0" alt="Click to open ObjectFinder" src="images/<%= findObjectAction.getIconKey() %>" onclick="javascript:OF.findObject(<%= view.getEvalHRef(findObjectAction) %>, $('<%= FIELD_NAME_TARGET_XRI %>.Title'), $('<%= FIELD_NAME_TARGET_XRI %>'), '<%= lookupId %>');" />
				                    	</td>
										<td>					 				
						                	<input id="<%= FIELD_NAME_TARGET_XRI %>.Title" name="<%= FIELD_NAME_TARGET_XRI %>.Title" type="text" class="valueL mandatory" style="font-size:90%;" readonly value="<%= selectedTargetXriTitle == null ? "" : selectedTargetXriTitle %>" />
						                    <input id="<%= FIELD_NAME_TARGET_XRI %>" name="<%= FIELD_NAME_TARGET_XRI %>" type="hidden" class="valueLLocked" value="<%= selectedTargetXri == null ? "" : selectedTargetXri %>" onchange="javascript:<%= submitFormScriptlet %>" />
						                </td>
				                    </tr>
				                    <tr>
				                    	<td colspan="2">
<%
											if(!options.isEmpty()) {
%>	                    
					                    		<select id="<%= FIELD_NAME_FUNCTION_SELECT %>" name="<%= FIELD_NAME_FUNCTION_SELECT %>" class="valueL" style="font-size:90%;">
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
									</tr>
									<tr>
										<td colspan="2">
<%
											if(selectedTargetXri != null) {
%>								
												<a class="<%= CssClass.btn.toString() + " " + CssClass.btnDefault.toString() %>" style="font-size:90%;" onclick="javascript:$('<%= FIELD_NAME_COMMAND %>').value='<%= COMMAND_ADD_MENU_ENTRY %>';<%= submitFormScriptlet %>">&nbsp;&nbsp;+&nbsp;&nbsp;</a>&nbsp;&nbsp;
<%
											}
%>									
											<a class="<%= CssClass.btn.toString() + " " + CssClass.btnDefault.toString() %>" style="font-size:90%;" onclick="javascript:$('<%= FIELD_NAME_TARGET_XRI %>').value='';$('<%= FIELD_NAME_TARGET_XRI %>.Title').value='';<%= submitFormScriptlet %>"><%= texts.getCancelTitle() %></a>
										</td>
									</tr>
								</table>
							</fieldset>
						</div>
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
