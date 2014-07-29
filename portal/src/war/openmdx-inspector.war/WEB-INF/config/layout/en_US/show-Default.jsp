<%@  page contentType= "text/html;charset=UTF-8" language="java" pageEncoding= "UTF-8" %><%
/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: show-Default.jsp
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
%><%@ page session="true" import="
org.openmdx.base.naming.*,
org.openmdx.portal.servlet.*,
org.openmdx.portal.servlet.control.*,
org.openmdx.portal.servlet.component.*
" %>
<%
	ApplicationContext app = (ApplicationContext)session.getValue(WebKeys.APPLICATION_KEY);
	ViewsCache viewsCache = (ViewsCache)session.getValue(WebKeys.VIEW_CACHE_KEY_SHOW);
	ShowObjectView view = (ShowObjectView)viewsCache.getView(request.getParameter(Action.PARAMETER_REQUEST_ID));
	Texts_1_0 texts = app.getTexts();
	ShowInspectorControl inspectorControl = (ShowInspectorControl)view.getControl();
	ViewPort p = ViewPortFactory.openPage(
		view,
		request,
		out
	);

	// ViewPort.Type.STANDARD
	if(p.getViewPortType() == ViewPort.Type.STANDARD) {

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

		// North
		Control north = view.createControl(
			"north",
			SessionInfoControl.class
		);

		// West
		PanelControl west = (PanelControl)view.createControl(
			"layoutWest",
			PanelControl.class
		);
		west.setLayout(PanelControl.LAYOUT_VERTICAL);
		west.setTableStyle("cellspacing=\"0\" cellpadding=\"0\"");

		// Navigation
		Control navigation = view.createControl(
			"navigation",
			NavigationControl.class
		);

		// Search
		Control search = view.createControl(
			"search",
			ScriptControl.class
		);

		// Errors
		Control errors = view.createControl(null, ShowErrorsControl.class);

		// Dashboard
		Control dashboard = view.createControl(
			"Dashboard",
			DashboardControl.class
		);
		// WorkspaceDashboard
		Control workspaceDashboard = view.createControl(
			"WorkspaceDashboard",
			WorkspaceDashboardControl.class
		);		
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html dir="<%= texts.getDir() %>">
<head>
<%
	String title = view.getObjectReference().getTitle();
	if(title == null) {
	    title = "#ERR";
	}
	else {
	    while(title.startsWith("<") && title.indexOf("/>") > 0) {
	        title = title.substring(title.indexOf("/>") + 2);
	    }
	}
%>
  <title><%= app.getApplicationName() + " - " + title + (title.length() == 0 ? "" : " - ") + view.getObjectReference().getLabel() %></title>
<%
	prolog.paint(p, PagePrologControl.FRAME_PRE_PROLOG, false);
	p.flush();
%>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=<%= app.getInitialScale() %>, maximum-scale=1.0">
	
	<!-- Styles -->
	<link rel="stylesheet" href="javascript/bootstrap/css/bootstrap.min.css">
	<link rel="stylesheet" href="_style/ssf.css" >
	<link rel="stylesheet" href="_style/n2default.css" >
	<link rel="stylesheet" href="_style/colors.css">
	<link rel="stylesheet" href="_style/calendar-small.css">
	<link rel="stylesheet" href="javascript/wiky/wiky.css" >
	<link rel="stylesheet" href="javascript/wiky/wiky.lang.css" >
	<link rel="stylesheet" href="javascript/wiky/wiky.math.css" >
	<link rel="stylesheet" href="javascript/yui/build/assets/skins/sam/container.css" >
	<link rel='shortcut icon' href='images/favicon.ico' >

	<!-- Libraries -->
    <script src="javascript/prototype.js"></script>
    <script src="javascript/jquery/jquery.min.js"></script>
	<script>
	  $.noConflict();
	</script>
	<script src="javascript/bootstrap/js/bootstrap.min.js"></script>
	<script src="javascript/portal-all.js"></script>
	<script src="javascript/calendar/lang/calendar-<%= app.getCurrentLocaleAsString() %>.js"></script>
	<!--[if lt IE 7]><script type="text/javascript" src="javascript/iehover-fix.js"></script><![endif]-->
	<script language="javascript" type="text/javascript">
		var OF = null;
		try {
			OF = self.opener.OF;
		} catch(e) {
			OF = null;
		}
		if(!OF) {
			OF = new ObjectFinder();
		}	  
	</script>
<%
	prolog.paint(p, PagePrologControl.FRAME_POST_PROLOG, false);
	p.flush();
%>
</head>
<body style="padding-top:50px;" onload="initPage();">
<iframe class="<%= CssClass.popUpFrame %>" id="DivShim" src="blank.html" scrolling="no" frameborder="0" style="position:absolute; top:0px; left:0px; display:none;"></iframe>
<%
		EditInspectorControl.paintEditPopups(p);
		p.flush();
%>
<div class="<%= CssClass.navbar + " " + CssClass.navbarInverse + " " + CssClass.navbarFixedTop %>" role="navigation">
	<div class="<%= CssClass.containerFluid %>" style="padding-left:0px;">
		<div class="<%= CssClass.navbarHeader %>">
			<button type="button" class="<%= CssClass.navbarToggle %>" data-toggle="collapse" data-target="#bs-top-navigation">
				<span class="sr-only">Toggle navigation</span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			</button>
		</div>
		<div class="<%= CssClass.collapse + " " + CssClass.navbarCollapse %>" id="bs-top-navigation">
			<ul class="<%= CssClass.nav + " " + CssClass.navbarNav + " " + CssClass.visibleSm + " " + CssClass.visibleMd %>">
<%
				RootMenuControl.paintTopNavigation(p, 1);
				p.flush();
%>
			</ul>
			<ul class="<%= CssClass.nav + " " + CssClass.navbarNav + " " + CssClass.hiddenSm + " " + CssClass.hiddenMd %>">
<%
				RootMenuControl.paintTopNavigation(p, null);
				p.flush();
%>
			</ul>
			<ul class="<%= CssClass.nav + " " + CssClass.navbarNav + " " + CssClass.navbarRight %>">
				<li class="<%= CssClass.dropdown %>">
					<a href="#" class="<%= CssClass.dropdownToggle %>" data-toggle="dropdown" onclick="javascript:this.parentNode.hide=function(){};"><span><%= app.getLoginPrincipal() %> <b class="<%= CssClass.caret %>"></b></span></a>
					<ul class="<%= CssClass.dropdownMenu %>" role="menu">
						<li><% SessionInfoControl.paintLogoffButton(p, false, ""); p.flush(); %></li>
						<li><% SessionInfoControl.paintSaveSettingsButton(p, false, ""); p.flush(); %></li>
					</ul>
				</li>
			</ul>
<%
			search.paint(p, false);
			p.flush();
%>
		</div>
	</div>
</div>
<div id="container">
	<div id="wrap">
		<div id="<%= NavigationControl.getHeaderId(p) %>" class="<%= CssClass.hiddenPrint %>">
			<div id="hider">
			    <!-- root panel -->
				<div id="rootPanel">"
					<div class="<%= CssClass.hd %>"><%= app.getTexts().getExploreText() %></div>
					<div class="bd">
						<ul id="<%=CssClass.ssfNavv %>" class="<%=CssClass.ssfNavv %>" onmouseover="sfinit(this);">
<%
							RootMenuControl.paintQuickAccessors(p);
							p.flush();
%>
							<li>&nbsp;</li>
						</ul>
					</div>
				</div>
			</div> <!-- hider -->
<%
			north.paint(p, false);
			p.flush();
			navigation.paint(p, false);
			p.flush();
%>
			<ul id="nav" class="<%= CssClass.nav + " " + CssClass.navPills %>">
<%
				for(OperationPane operationPane: view.getChildren(OperationPane.class)) {
					operationPane.paint(p, null, false);
				}
				for(WizardControl wizardControl: inspectorControl.getChildren(WizardControl.class)) {
					wizardControl.paint(p, false);
				}
				p.flush();
%>
			</ul>
			<div id="OperationDialogHolder">
				<div id="OperationDialogEmbedder" onClick="javascript:try{var ud=$('UserDialog');var od=$('OperationDialog');ud.parentNode.insertBefore(od,ud);$('OperationDialogHolder').innerHTML='';window.scrollBy(0,-999999);}catch(e){};">
					<img src='images/show_content.gif' alt='' />
				</div>
				<div id="OperationDialog"></div>
			</div>
			<iframe name="OperationDialogResponse" id="OperationDialogResponse" onload="javascript:var t=this.contentDocument.body.innerHTML;if(t){$('OperationDialog').innerHTML=t;};"></iframe>
		</div> <!-- header -->
		<div id="content" class="<%= NavigationControl.getContentClass(p) %>">
<%
			boolean hideWorkspaceDashboard = Boolean.valueOf(app.getSettings().getProperty(UserSettings.HIDE_WORKSPACE_DASHBOARD.getName()));
%>
			<div class="<%= CssClass.row %>">
<%@ include file="../../../../show-header.html" %>
<%
				if(!hideWorkspaceDashboard) {
%>
					<div class="<%= CssClass.hiddenXs %> <%= CssClass.colSm3 %> <%= CssClass.colLg2 %> <%= CssClass.hiddenPrint %>">
<%
						workspaceDashboard.paint(p, false);
						p.flush();
%>		
					</div> <!--  paneLeft -->
<%
				}
%>
				<div class="<%= hideWorkspaceDashboard ? CssClass.colXs12.toString() : CssClass.colXs12 + " " + CssClass.colSm9 + " " + CssClass.colLg10 %>">
<%
					errors.paint(p, false);
					// No dashboards in lookup mode
					if(view.getLookupType() == null) {
						dashboard.paint(p, false);
					}
					p.flush();
%>
					<iframe name="UserDialogResponse" id="UserDialogResponse" style="display:none;" onload="javascript:var t=this.contentDocument.body.innerHTML;if(t){$('UserDialog').innerHTML=t;};"></iframe>
					<div id="UserDialog"><div id="UserDialogWait" class="<%= CssClass.hidden %>" /></div></div>
					<script language="javascript" type="text/javascript">try {if($('header')){$('OperationDialogEmbedder').click();}}catch(e){};</script>
					<div id="aPanel">
<%
						for(AttributePane attributePane: view.getChildren(AttributePane.class)) {
							attributePane.paint(p, null, false);
						}
						p.flush();
%>
					</div>
<%
					// Reference panes
					for(ReferencePane referencePane: view.getChildren(ReferencePane.class)) {
						%><%@ include file="../Set-MultiDelete-include.jsp" %><%
						referencePane.paint(
							p,
							ReferencePaneControl.FRAME_VIEW,
							false // forEditing
						);
					}
					p.flush();
%>
				</div>
			</div>
			<div class="row">
<%@ include file="../../../../show-footer.html" %>
			</div> <!-- row -->
		</div> <!-- container -->
<%@ include file="../../../../show-footer-noscroll.html" %>
	</div> <!-- wrap -->
</div> <!-- container -->

<%
	epilog.paint(p, false);
	p.close(false);
%>
</body>
</html>
<%
	}
	
	// ViewPort.Type.EMBEDDED
	else if(p.getViewPortType() == ViewPort.Type.EMBEDDED) {
	  for(AttributePane attributePane: view.getChildren(AttributePane.class)) {
		  attributePane.paint(
			  p,
			  null,
			  false
		  );
	  }
	  p.flush();
	  p.close(false);
%>
	  <script language="javascript" type="text/javascript">
		//alert('postLoad show');
	  </script>
<%
	}
%>
