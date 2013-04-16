<%@page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%><%
/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: WorkspacesDashlet.jsp
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 *
 * Copyright (c) 2004-2013, OMEX AG, Switzerland
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
org.openmdx.base.exception.*,
org.openmdx.portal.servlet.*,
org.openmdx.portal.servlet.view.*,
org.openmdx.portal.servlet.control.*,
org.openmdx.portal.servlet.action.*,
org.openmdx.kernel.log.*
" %>
<%
	/*
	The StaticQuickAccessDashlet shows a menu with a list of shortcuts. The shortcuts are configured
	in a properties file located in ./wizards/StaticQuickAccessDashlet/<dashlet-label>.properties
	Below is a sample properties file. The entries are displayed in the order of the property keys.

	# SampleMenu.properties
    e0000=<div style="font-size:120%;font-weight:bold;">Customers</div>
	e0001=xri://@openmdx*org.opencrx.kernel.activity1/provider/CRX/segment/Standard/activityCreator&queryType=org:opencrx:kernel:activity1:ActivityCreator&query=name().equalTo("Sample~Customers"); > Operation.org:opencrx:kernel:activity1:ActivityCreator:newActivity
	e0002=xri://@openmdx*org.opencrx.kernel.activity1/provider/CRX/segment/Standard/activityTracker&queryType=org:opencrx:kernel:activity1:ActivityTracker&query=name().equalTo("Sample~Customers"); > Reference.filteredActivity
	e0003=<div style="font-size:120%;font-weight:bold;">Compliance</div>
	e0004=xri://@openmdx*org.opencrx.kernel.activity1/provider/CRX/segment/Standard/activityCreator&queryType=org:opencrx:kernel:activity1:ActivityCreator&query=name().equalTo("Sample~Compliance"); > Operation.org:opencrx:kernel:activity1:ActivityCreator:newActivity
	e0005=xri://@openmdx*org.opencrx.kernel.activity1/provider/CRX/segment/Standard/activityTracker&queryType=org:opencrx:kernel:activity1:ActivityTracker&query=name().equalTo("Sample~Compliance"); > Reference.filteredActivity
	e0006=<div style="font-size:120%;font-weight:bold;">Generic</div>
	e0007=xri://@openmdx*org.opencrx.kernel.activity1/provider/CRX/segment/Standard/activityCreator&queryType=org:opencrx:kernel:activity1:ActivityCreator&query=name().equalTo("Sample~Generic"); > Operation.org:opencrx:kernel:activity1:ActivityCreator:newActivity
	e0008=xri://@openmdx*org.opencrx.kernel.activity1/provider/CRX/segment/Standard/activityTracker&queryType=org:opencrx:kernel:activity1:ActivityTracker&query=name().equalTo("Sample~Generic"); > Reference.filteredActivity
	e0009=<div style="font-size:120%;font-weight:bold;">Communications</div>
	e0010=xri://@openmdx*org.opencrx.kernel.activity1/provider/CRX/segment/Standard/activityCreator&queryType=org:opencrx:kernel:activity1:ActivityCreator&query=name().equalTo("Sample~Communications"); > Operation.org:opencrx:kernel:activity1:ActivityCreator:newActivity
	e0011=xri://@openmdx*org.opencrx.kernel.activity1/provider/CRX/segment/Standard/activityTracker&queryType=org:opencrx:kernel:activity1:ActivityTracker&query=name().equalTo("Sample~Communications"); > Reference.filteredActivity
	e0012=<div style="font-size:120%;font-weight:bold;">Analysis</div>
	e0013=xri://@openmdx*org.opencrx.kernel.activity1/provider/CRX/segment/Standard/activityCreator&queryType=org:opencrx:kernel:activity1:ActivityCreator&query=name().equalTo("Sample~Analysis"); > Operation.org:opencrx:kernel:activity1:ActivityCreator:newActivity
	e0014=xri://@openmdx*org.opencrx.kernel.activity1/provider/CRX/segment/Standard/activityTracker&queryType=org:opencrx:kernel:activity1:ActivityTracker&query=name().equalTo("Sample~Analysis"); > Reference.filteredActivity
	e0021=<div style="font-size:120%;font-weight:bold;">Underwriting</div>
	e0022=xri://@openmdx*org.opencrx.kernel.activity1/provider/CRX/segment/Standard/activityCreator&queryType=org:opencrx:kernel:activity1:ActivityCreator&query=name().equalTo("Sample~Underwriting"); > Operation.org:opencrx:kernel:activity1:ActivityCreator:newActivity
	e0023=xri://@openmdx*org.opencrx.kernel.activity1/provider/CRX/segment/Standard/activityTracker&queryType=org:opencrx:kernel:activity1:ActivityTracker&query=name().equalTo("Sample~Underwriting"); > Reference.filteredActivity	
 */
	ApplicationContext app = (ApplicationContext)session.getValue(WebKeys.APPLICATION_KEY);
	Texts_1_0 texts = app.getTexts();
	ViewsCache viewsCache = (ViewsCache)session.getValue(WebKeys.VIEW_CACHE_KEY_SHOW);
	String parameters = request.getParameter(WebKeys.REQUEST_PARAMETER);
  	if(app != null && parameters != null) {
  		
  		javax.jdo.PersistenceManager pm = app.getNewPmData();
  		
  		// Get parameters
		final String xri = Action.getParameter(parameters, Action.PARAMETER_OBJECTXRI);
		final String requestId = request.getParameter(Action.PARAMETER_REQUEST_ID);
		String dashletId = Action.getParameter(parameters, Action.PARAMETER_ID);
		boolean isSharedDashlet = false;
		if(dashletId != null && dashletId.startsWith(DashboardControl.SHARED_DASHLET_MARKER)) {
			isSharedDashlet = true;
		}
		
		// Menu entries from properties file
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
		Properties menuEntryProperties = new Properties();
		try {
			menuEntryProperties.load(
				new InputStreamReader(
					this.getServletContext().getResourceAsStream(
						"./wizards/StaticQuickAccessDashlet/" + ((String)settings.get((isSharedDashlet ? dashletId.substring(1) : dashletId) + ".label")).replace("*", "_") + ".properties"
					),
					"UTF-8"
				)				
			);
		} catch(Exception e) {
			new ServiceException(e).log();
		}
		Map<Object,Object> menuEntries = new TreeMap<Object,Object>();
		menuEntries.putAll(menuEntryProperties);
		
		// Handle request
		if(xri != null && requestId != null && dashletId != null && viewsCache.getView(requestId) != null) {
			ShowObjectView view = (ShowObjectView)viewsCache.getView(requestId);
%>
			<div id="<%= dashletId %>Content" style="padding:5px;min-width:150px;">
				<form id="<%= dashletId %>Form" name="<%= dashletId %>Form" />
					<input id="<%= WebKeys.REQUEST_ID %>" name="<%= Action.PARAMETER_REQUEST_ID %>" type="hidden" value="<%= requestId %>" />
					<input id="<%= WebKeys.REQUEST_PARAMETER %>" name="<%= WebKeys.REQUEST_PARAMETER %>" type="hidden" value="<%= parameters %>" />
					<div>
						<table>
<%							
							final String SPACER = "</td></tr><tr><td><img src=\"images/spacer.gif\" height=\"1px;\" /></td></tr>";
							String spacer = "";
							for(Object entry: menuEntries.values()) {
								String menuEntry = (String)entry;
								try {
									int pos = menuEntry.indexOf(" > ");
									if(pos > 0) {
										String queryString = menuEntry.substring(0, pos);
										if(!queryString.startsWith("?path=")) {
											queryString = "?path=" + queryString;
										}
										javax.jdo.Query objectQuery = pm.newQuery(
											org.openmdx.base.persistence.cci.Queries.QUERY_LANGUAGE,
											queryString
										);
										List<javax.jmi.reflect.RefObject> objects = (List<javax.jmi.reflect.RefObject>)objectQuery.execute();
										if(!objects.isEmpty()) {
											Path targetXri = new Path(objects.iterator().next().refMofId());
											String function = menuEntry.substring(pos + 3);
											ShowObjectView targetView = null;
											try {
												targetView = new ShowObjectView(
													view.getId(),
													(String)null,
													targetXri,
													app,
													new HashMap(),
													(String)null, // lookupType
													(String)null, // resourcePathPrefix
													(String)null, // navigationTarget
													(Boolean)null // isReadOnly
												);
											} catch(Exception ignore) {}
											if(targetView != null) {
												if(function.startsWith("Operation.")) {
													String operationName = function.substring(10);
													List<OperationPaneControl> operationPaneControls = new ArrayList<OperationPaneControl>();
													// Operations
													for(OperationPaneControl paneControl: targetView.getShowInspectorControl().getOperationPaneControl()) {
														for(OperationTabControl tabControl: paneControl.getOperationTabControl()) {
															if(tabControl.getQualifiedOperationName().equals(operationName)) {
																String tabId = Integer.toString(tabControl.getTabId());
																Action action = null;
																String target = "";
																if(tabControl instanceof WizardTabControl) {
																	WizardTabControl wizardTabControl = (WizardTabControl)tabControl;
																	if(wizardTabControl.isInplace()) {
																		String script = "$('UserDialogWait').className='loading udwait';new Ajax.Updater('UserDialog', '." + tabControl.getName() + "?requestId=REQUEST_ID&xri=" + targetXri + "', {evalScripts: true});";
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
%>
																<div>								                    		                    	
												          			&nbsp;&nbsp;&raquo;&nbsp;<a <%= target %> href="#" onmouseover="javascript:this.href=<%= view.getEvalHRef(action) %>;onmouseover=function(){};" style="font-size:90%;"><%= action.getTitle().replace(" ", "&nbsp;") %></a>
												          		</div>
<%
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
%>								                    
															<div>	                    	
<%
																String target = "_blank".equals(tabControl.getWizardDefinition().getTargetType()) ? "target=\"_blank\"" : "target=\"_self\"";
%>
											          			&nbsp;&nbsp;&raquo;&nbsp;<a href="#" <%= target %> onmouseover="javascript:this.href=<%= view.getEvalHRef(action) %>;onmouseover=function(){};" style="font-size:90%;"><%= action.getTitle().replace(" ", "&nbsp;") %></a>
											          		</div>
<%
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
																	SelectObjectAction.EVENT_ID,
																	actionParams.toArray(new Action.Parameter[actionParams.size()]),
																	action.getTitle(),
																	action.getIconKey(),
																	action.isEnabled()
																);
	%>
																<div>									                    	                    	
												                		&nbsp;&nbsp;&raquo;&nbsp;<a href="#" onmouseover="javascript:this.href=<%= view.getEvalHRef(selectObjectAndReferenceAction) %>;onmouseover=function(){};" style="font-size:90%;"><%= (action.getTitle().startsWith(WebKeys.TAB_GROUPING_CHARACTER) ? action.getTitle().substring(1).replace(" ", "&nbsp;") : action.getTitle().replace(" ", "&nbsp;")) %></a>
												                	</div>
	<%
															}						
														}
													}
												}
											}
										}
									}
									// Separator
									else {
%>
					                  	<%= spacer %>
										<tr>
											<td>
												<%= menuEntry %>
<%
											spacer = SPACER;
									}
								} catch (Exception e) {
									ServiceException e0 = new ServiceException(e);
									if(e0.getExceptionCode() != BasicException.Code.AUTHORIZATION_FAILURE) {
										e0.log();
									}
								}
							}
%>
							<%= spacer.isEmpty() ? "" : "</td></tr>" %>
						</table>
					</div>
				</form>
			</div>
<%				
		}
		else {
%>
			<p>
		    <i>StaticQuickAccessDashlet invoked with missing or invalid parameters:</i>
		    <ul>
			    <li><b>RequestId:</b> <%= requestId %></li>
			    <li><b>XRI:</b> <%= xri %></li>
			    <li><b>Dashlet-Id:</b> <%= dashletId %></li>
			</ul>
<%
		}
  	}
%>
