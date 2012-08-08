<%@  page contentType= "text/html;charset=UTF-8" language="java" pageEncoding= "UTF-8" %><%
/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: show-Default.jsp,v 1.44 2008/06/01 16:40:52 wfro Exp $
 * Description: Default.jsp
 * Revision:    $Revision: 1.44 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/01 16:40:52 $
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
 * This product includes software developed by Mihai Bazon
 * (http://dynarch.com/mishoo/calendar.epl) published with an LGPL
 * license.
 */
%><%@ page session="true" import="
org.openmdx.compatibility.base.naming.*,
org.openmdx.portal.servlet.*,
org.openmdx.portal.servlet.control.*,
org.openmdx.portal.servlet.view.*,
org.openmdx.portal.servlet.texts.*
" %>
<%
	ApplicationContext app = (ApplicationContext)session.getValue(WebKeys.APPLICATION_KEY);
	ViewsCache viewsCache = (ViewsCache)session.getValue(WebKeys.VIEW_CACHE_KEY_SHOW);
	ShowObjectView view = (ShowObjectView)viewsCache.getView(request.getParameter(Action.PARAMETER_REQUEST_ID));
	PaintScope paintScope = PaintScope.valueOf(request.getParameter(Action.PARAMETER_SCOPE));
	Texts_1_0 texts = app.getTexts();
	ShowInspectorControl inspectorControl = view.getShowInspectorControl();
	String guiMode = app.getCurrentGuiMode();
	HtmlPage p = HtmlPageFactory.openPage(
		view,
		request,
		out
	);

	// PaintScope.FULL
	if(paintScope == PaintScope.FULL) {
		// Set header
		response.setHeader(
			"Cache-Control",
			"max-age=" + Integer.MAX_VALUE
		);
		response.setHeader(
			"Pragma",
			""
		);
	
		// Prolog
		Control prolog = view.createControl(
			"PROLOG",
			PagePrologControl.class
		);
	
		// Epilog
		Control epilog = view.createControl(
			"EPILOG",
			PageEpilogControl.class
		);
	
		// Operation parameters
		PanelControl operationParams = (PanelControl)view.createControl(
			"PARAMS",
			PanelControl.class
		);
		operationParams.setLayout(PanelControl.LAYOUT_NONE);
		operationParams.addControl(inspectorControl.getOperationPaneControl(), OperationPaneControl.FRAME_PARAMETERS);
		operationParams.addControl(inspectorControl.getReportControl(), OperationPaneControl.FRAME_PARAMETERS);
	
		// Dialogs
		PanelControl dialogs = (PanelControl)view.createControl(
			"dialogs",
			PanelControl.class
		);
		dialogs.addControl(operationParams);
	
		// North
		Control north = view.createControl(
			"north", 
			SessionInfoControl.class
		);
	
		// West
		MenuControl menuRoot = (MenuControl)view.createControl(
			"menuRoot",
			MenuControl.class
		);
		menuRoot.addControl(
			view.createControl("rootmenu", RootMenuControl.class)
		);
		menuRoot.setMenuClass("navv");
		menuRoot.setLayout(MenuControl.LAYOUT_VERTICAL);
		PanelControl west = (PanelControl)view.createControl(
			"layoutWest",
			PanelControl.class
		);
		west.setLayout(PanelControl.LAYOUT_VERTICAL);
		west.setTableStyle("cellspacing=\"0\" cellpadding=\"0\"");
		west.addControl(
			new Control[]{
				view.createControl("userwest", ScriptControl.class),
				menuRoot,
				view.createControl("nav-calendar", CalendarControl.class)
			}
		);
	
		// Title
		ObjectTitleControl title = (ObjectTitleControl)view.createControl(
			"TITLE",
			ObjectTitleControl.class
		);
		title.setShowPerformance(false);
		// Menu
		MenuControl menu = (MenuControl)view.createControl(
			"opMenu",
			MenuControl.class
		);
		menu.setLayout(MenuControl.LAYOUT_HORIZONTAL);
		menu.setMenuClass("nav");
		menu.setHasPrintOption(true);
		menu.addControl(inspectorControl.getOperationPaneControl());
		menu.addControl(inspectorControl.getWizardControl());
		menu.addControl(inspectorControl.getReportControl());
	
		// Operation results
		PanelControl operationResults = (PanelControl)view.createControl(
			"PARAMS",
			PanelControl.class
		);
		operationResults.setLayout(PanelControl.LAYOUT_NONE);
		operationResults.addControl(inspectorControl.getOperationPaneControl(), OperationPaneControl.FRAME_RESULTS);
	
		// Errors
		Control errors =	view.createControl(null, ShowErrorsControl.class);
	
		// Attributes
		Control attributes = inspectorControl.getAttributePaneControl();
	
		// References
		PanelControl references = (PanelControl)view.createControl(
			"REFERENCES",
			PanelControl.class
		);
		references.setLayout(PanelControl.LAYOUT_NONE);
		ReferencePaneControl[] referencePaneControls = inspectorControl.getReferencePaneControl();
		for(int i = 0; i < referencePaneControls.length; i++) {
			referencePaneControls[i].setIsMultiDeleteEnabled(true);
		}
		references.addControl(
			referencePaneControls,
			ReferencePaneControl.FRAME_VIEW
		);
	
		// Center
		PanelControl center = (PanelControl)view.createControl(
			"CENTER",
			PanelControl.class
		);
		center.setLayout(PanelControl.LAYOUT_VERTICAL);
		center.setTableStyle("class=\"wide\"");
		center.addControl(title);
		center.addControl(menu);
	
		boolean noLayoutManager =
		guiMode.equals(WebKeys.SETTING_GUI_MODE_BASIC);

%>
<!--[if IE]><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"><![endif]-->
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
	if(noLayoutManager) {
%>
		<html dir="<%= texts.getDir() %>">
<%
	}
	else {
%>
		<html dir="<%= texts.getDir() %>" style="overflow:hidden;">
<%
	}
%>
<head>
  <title><%= app.getApplicationName() + " - " + view.getObjectReference().getTitle() + (view.getObjectReference().getTitle().length() == 0 ? "" : " - ") + view.getObjectReference().getLabel() %></title>
<%
	prolog.paint(p, PagePrologControl.FRAME_PRE_PROLOG, false);
	p.flush();
%>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<link href="_style/colors.css" rel="stylesheet" type="text/css">
	<link href="_style/calendar-small.css" rel="stylesheet" type="text/css">
	<!--[if lt IE 7]><script type="text/javascript" src="javascript/iehover-fix.js"></script><![endif]-->
	<script type="text/javascript" src="javascript/portal-all.js"></script>
	<script type="text/javascript" src="javascript/calendar/lang/calendar-<%= app.getCurrentLocaleAsString() %>.js"></script>
	<script language="javascript" type="text/javascript">
	  var OF = null;
	  try {
		OF = self.opener.OF;
	  }
	  catch(e) {
		OF = null;
	  }
	  if(!OF) {
		OF = new ObjectFinder();
	  }
	</script>
	<link rel="stylesheet" type="text/css" href="_style/ssf.css" >
	<link rel="stylesheet" type="text/css" href="javascript/yui-ext/resources/css/yui-ext.css" >
	<link rel="stylesheet" type="text/css" href="javascript/yui-ext/resources/css/ytheme-gray.css" >
	<link rel="stylesheet" type="text/css" href="_style/n2default.css" >
	<link rel='shortcut icon' href='images/favicon.ico' />
<%
	prolog.paint(p, PagePrologControl.FRAME_POST_PROLOG, false);
	p.flush();
%>
</head>
<%
	if(noLayoutManager) {
%>
		<body class="ytheme-gray" onload="initPage();">
<%
	}
	else {
%>
		<body class="ytheme-gray" onload="initPage();" style="overflow:hidden;">
<%
	}
%>
<iframe class="popUpFrame" id="DivShim" src="blank.html" scrolling="no" frameborder="0" style="position:absolute; top:0px; left:0px; display:none;"></iframe>
<%
	dialogs.paint(p, false);
	p.flush();
%>
<%
	if(noLayoutManager) {
%>
<div id ="container">
<table id="simpleLayout">
<tr id="slHeaderRow">
<td id="slHeader" colspan="2">
	<div id="header">
<%
	}
	else {
%>
<div id ="container">
  <div id="header" class="ylayout-inactive-content">
<%
	}
	north.paint(p, false);
	p.flush();
	if(noLayoutManager) {
%>
	</div>
</td>
</tr>
<tr id="slContentRow">
<td id="slNavigation">
	<div id="navigation" >
<%
	}
	else {
%>
  </div>
	<div id="navigation" class="ylayout-inactive-content">
<%
	}
    west.paint(p, false);
    p.flush();
%>
       <%@ include file="../../../../show-note.html" %>
<%
	if(noLayoutManager) {
%>
	</div>
</td>
<td id="slContent">
	<div id="content">
<%
	}
	else {
%>
	</div>
  <div id="content" class="ylayout-inactive-content">
<%
	}
%>
		<div id="ie7bugfix" style="position:relative;top:0;left:0;height:auto;">
<%@ include file="../../../../show-header.html" %>
<%
			errors.paint(p, false);
			center.paint(p, false);
			operationResults.paint(p, false);
			p.flush();
%>
			<div id="aPanel">
<%
			attributes.paint(p, false);
			p.flush();
%>
			</div>
<%
			references.paint(p, false);
			p.flush();
%>
<%@ include file="../../../../show-footer.html" %>
		</div>
<%
	if(noLayoutManager) {
%>
	</div>
</td>
</tr>
</table
<%
	}
	else {
%>
	</div>
<%
	}
%>
</div>

<%
	epilog.paint(p, false);
	p.close(false);
%>
</body>
</html>
<%
	}
	// PaintScope.ATTRIBUTE_PANE
	else if(paintScope == PaintScope.ATTRIBUTE_PANE) {
		view.getAttributePane().getAttributePaneControl().paint(
			p, 
			false
		);
		p.flush();
		p.close(false);
%>
	<script language="javascript" type="text/javascript">
		//alert('postLoad show');
	</script>
<%
	}
%>
