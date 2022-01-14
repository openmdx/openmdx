<%@  page contentType= "text/html;charset=UTF-8" language="java" pageEncoding= "UTF-8" %><%
/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: show-Default.jsp
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
	<link rel="stylesheet" href="js/bootstrap/css/bootstrap.min.css">
	<link rel="stylesheet" href="_style/ssf.css" >
	<link rel="stylesheet" href="_style/n2default.css" >
	<link rel="stylesheet" href="_style/colors.css">
	<link rel="stylesheet" href="_style/calendar-small.css">
	<link rel="stylesheet" href="js/wiky/wiky.css" >
	<link rel="stylesheet" href="js/wiky/wiky.lang.css" >
	<link rel="stylesheet" href="js/wiky/wiky.math.css" >
	<link rel="stylesheet" href="js/yui/build/assets/skins/sam/container.css" >
	<link rel='shortcut icon' href='images/favicon.ico' >
	<!-- Libraries -->
	<script type="text/javascript" src="javax.faces.resource/jsf.js.xhtml?ln=javax.faces&amp;stage=Development"></script>	
    <script src="js/prototype.js"></script>
    <script src="js/jquery/jquery.min.js"></script>
	<script>
	  $.noConflict();
	</script>
	<script src="js/popper/js/popper.min.js"></script>	
	<script src="js/bootstrap/js/bootstrap.min.js"></script>
	<script src="js/portal-all.js"></script>
	<script src="js/calendar/lang/calendar-<%= app.getCurrentLocaleAsString() %>.js"></script>
	<!--[if lt IE 7]><script type="text/javascript" src="js/iehover-fix.js"></script><![endif]-->
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
<div class="<%= CssClass.navbar + " " + CssClass.navbar_expand_md + " " + CssClass.navbar_dark + " " + CssClass.fixed_top + " " + CssClass.bg_dark %>">
	<button class="<%= CssClass.navbar_toggler %>" type="button" data-toggle="collapse" data-target="#bs-top-navigation">
    	<span class="navbar-toggler-icon"></span>
	</button>
	<div id="bs-top-navigation" class="<%= CssClass.collapse + " " + CssClass.navbar_collapse %>">
		<ul class="<%= CssClass.navbar_nav + " " + CssClass.mr_auto %>">
<%
			RootMenuControl.paintTopNavigation(p, null);
			p.flush();
%>
		</ul>
<%
		search.paint(p, false);
		p.flush();
%>
		<ul class="<%= CssClass.navbar_nav %>">
			<li class="<%= CssClass.nav_item %>">
			    <div class="<%= CssClass.dropdown %>">
			        <button type="button" class="<%= CssClass.btn %> <%= CssClass.btn_sm %>" data-toggle="<%= CssClass.dropdown %>" onclick="javascript:this.parentNode.hide=function(){};">
			            <span class="<%= CssClass.nav_link %>" style="background-color:inherit;"><%= app.getLoginPrincipal() %></span>
			        </button>
			        <div class="<%= CssClass.dropdown_menu %> <%= CssClass.dropdown_menu_right %>">
			            <% SessionInfoControl.paintLogoffButton(p, false, CssClass.dropdown_item.toString()); p.flush(); %>
			            <% SessionInfoControl.paintSaveSettingsButton(p, false, CssClass.dropdown_item.toString()); p.flush(); %>
			        </div>
			    </div>
			</li>
    	</ul>
	</div>
</div>
<div id="container">
	<div id="wrap">
		<div id="<%= NavigationControl.getHeaderId(p) %>" class="<%= CssClass.d_print_none %>">
			<div id="hider">
			    <!-- root panel -->
				<div id="rootPanel">"
					<div class="<%= CssClass.hd %>"><%= app.getTexts().getExploreText() %></div>
					<div class="bd">
						<ul id="<%=CssClass.ssf_navv %>" class="<%=CssClass.ssf_navv %>" onmouseover="sfinit(this);">
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
			<ul id="nav" class="<%= CssClass.nav + " " + CssClass.nav_pills %>" style="width:100%">
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
					<div class="<%= CssClass.d_xs_none %> <%= CssClass.col_sm_3 %> <%= CssClass.col_lg_2 %> <%= CssClass.d_print_none %>">
<%
						workspaceDashboard.paint(p, false);
						p.flush();
%>		
					</div> <!--  paneLeft -->
<%
				}
%>
				<div class="<%= hideWorkspaceDashboard ? CssClass.col_12.toString() : CssClass.col_sm_9 + " " + CssClass.col_lg_10 %>">
<%
					errors.paint(p, false);
					// No dashboards in lookup mode
					if(view.getLookupType() == null) {
						dashboard.paint(p, false);
					}
					p.flush();
%>
					<iframe name="UserDialogResponse" id="UserDialogResponse" style="display:none;" onload="javascript:var t=this.contentDocument.body.innerHTML;if(t){$('UserDialog').innerHTML=t;};"></iframe>
					<div id="OperationDialogPlaceHolder" style="display:none;"></div>
					<div id="UserDialog"><div id="UserDialogWait" class="<%= CssClass.d_none %>" /></div></div>
					<div id="UserDialogPlaceHolder" style="display:none;"></div>
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
	  Control errors = view.createControl(null, ShowErrorsControl.class);
	  errors.paint(p, false);
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
