<%@  page contentType= "text/html;charset=UTF-8" language="java" pageEncoding= "UTF-8" %><%
/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: Default.jsp
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
%><%@ page session="true" import="
org.openmdx.base.naming.*,
org.openmdx.portal.servlet.*,
org.openmdx.portal.servlet.control.*,
org.openmdx.portal.servlet.view.*
" %>
<%
	ApplicationContext app = (ApplicationContext)session.getValue(WebKeys.APPLICATION_KEY);
	ViewsCache viewsCache = (ViewsCache)session.getValue(WebKeys.VIEW_CACHE_KEY_SHOW);
	ShowObjectView view = (ShowObjectView)viewsCache.getView(request.getParameter(Action.PARAMETER_REQUEST_ID));
	Texts_1_0 texts = app.getTexts();
	ShowInspectorControl inspectorControl = view.getShowInspectorControl();
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

		// Root menu
		MenuControl rootPanel = (MenuControl)view.createControl(
			"rootPanel",
			MenuControl.class
		);
		rootPanel.addControl(
			view.createControl("rootmenu", RootMenuControl.class)
		);
		rootPanel.setMenuClass("navv");
		rootPanel.setLayout(MenuControl.LAYOUT_VERTICAL);

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

		// Operations Menu
		MenuControl menuOps = (MenuControl)view.createControl(
			"menuOps",
			MenuControl.class
		);
		menuOps.setLayout(MenuControl.LAYOUT_HORIZONTAL);
		menuOps.setMenuClass("nav");
		menuOps.setHasPrintOption(true);
		menuOps.addControl(inspectorControl.getOperationPaneControl());
		menuOps.addControl(inspectorControl.getWizardControl());

		// Search
		Control search = view.createControl(
			"search",
			ScriptControl.class
		);

		// Errors
		Control errors = view.createControl(null, ShowErrorsControl.class);

		// Attributes
		Control attributes = inspectorControl.getAttributePaneControl();

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
	<link href="_style/colors.css" rel="stylesheet" type="text/css">
	<link href="_style/calendar-small.css" rel="stylesheet" type="text/css">
	<script type="text/javascript" src="javascript/portal-all.js"></script>
	<!--[if lt IE 7]><script type="text/javascript" src="javascript/iehover-fix.js"></script><![endif]-->
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
	<link rel="stylesheet" type="text/css" href="_style/n2default.css" >
	<link rel="stylesheet" type="text/css" href="javascript/yui/build/assets/skins/sam/container.css" >
	<link rel="stylesheet" type="text/css" href="javascript/wiky/wiky.css" >
	<link rel="stylesheet" type="text/css" href="javascript/wiky/wiky.lang.css" >
	<link rel="stylesheet" type="text/css" href="javascript/wiky/wiky.math.css" >
	<link rel='shortcut icon' href='images/favicon.ico' />
<%
	prolog.paint(p, PagePrologControl.FRAME_POST_PROLOG, false);
	p.flush();
%>
<script language="javascript" type="text/javascript">
		var rootMenu = null;

		function toggleRootMenu(e){
			try{if(e.ctrlKey && e.altKey){rootMenu.moveTo(e.clientX+1, e.clientY);if(rootMenu.cfg.config.visible.value){rootMenu.hide();}else{rootMenu.show();}YAHOO.util.Event.preventDefault(e);}}catch(e){}
		};

		YAHOO.util.Event.onDOMReady(function(){
			rootMenu = new YAHOO.widget.Panel("rootPanel",{context:['rootMenuAnchor','tl','tr'], close:true, visible:false,constraintoviewport:true});
			rootMenu.cfg.queueProperty(
				"keylisteners",
				new YAHOO.util.KeyListener(document, { keys:27 }, {fn:rootMenu.hide, scope:rootMenu, correctScope:true })
			);
			rootMenu.cfg.queueProperty(
				"keylisteners",
				new YAHOO.util.KeyListener(document, { alt:true, keys:88 }, {fn:rootMenu.hide, scope:rootMenu, correctScope:true })
			);
			kl = new YAHOO.util.KeyListener(document, { alt:true, keys:88 }, {fn:rootMenu.show, scope:rootMenu, correctScope:true });
			kl.enable();
			YAHOO.util.Event.addListener(document, "click", toggleRootMenu);
			rootMenu.render();
		});
</script>
</head>
<body class="yui-skin-sam" onload="initPage();">
<iframe class="popUpFrame" id="DivShim" src="blank.html" scrolling="no" frameborder="0" style="position:absolute; top:0px; left:0px; display:none;"></iframe>
<%
		EditObjectControl.paintEditPopups(p);
		p.flush();
%>
<div id="container">
	<div id="wrap">
		<div id="<%= NavigationControl.getHeaderId(p) %>">
			<div id="hider">
<%
				rootPanel.paint(p, false);
				p.flush();
%>
			</div> <!-- hider -->
<%
			north.paint(p, false);
			p.flush();
%>
			<div id="topnavi">
<%
				search.paint(p, false);
				p.flush();
%>
				<ul id="navigation" class="navigation" onmouseover="sfinit(this);">
<%
					RootMenuControl.paintTopNavigation(p);
					p.flush();
%>
				</ul>
			</div> <!-- topnavi -->
<%
			RootMenuControl.paintMenuFlyIn(p);
			navigation.paint(p, false);
			menuOps.paint(p, false);
			p.flush();
%>
			<div id="OperationDialogHolder">
				<div id="OperationDialogEmbedder" onClick="javascript:try{var ud=$('UserDialog');var od=$('OperationDialog');ud.parentNode.insertBefore(od,ud);$('OperationDialogHolder').innerHTML='';od.className='dragged';window.scrollBy(0,-999999);}catch(e){};">
					<img src='images/show_content.gif' alt='' />
				</div>
				<div id="OperationDialog"></div>
			</div>
			<iframe name="OperationDialogResponse" id="OperationDialogResponse" onload="javascript:var t=this.contentDocument.body.innerHTML;if(t){$('OperationDialog').innerHTML=t;};"></iframe>
		</div> <!-- header -->
		<div id="content-wrap">
<%
			boolean hideWorkspaceDashboard = Boolean.valueOf(app.getSettings().getProperty(UserSettings.HIDE_WORKSPACE_DASHBOARD.getName()));
%>		
			<div id="<%= NavigationControl.getContentHeaderId(p) %>">
<%@ include file="../../../../show-header.html" %>
<%
				if(!hideWorkspaceDashboard) {
%>
					<div id="paneLeft">
<%
						workspaceDashboard.paint(p, false);
						p.flush();
%>		
					</div> <!--  paneLeft -->
<%
				}
				errors.paint(p, false);
				// No dashboards in lookup mode
				if(view.getLookupType() == null) {
					dashboard.paint(p, false);
				}
				p.flush();
%>
				<iframe name="UserDialogResponse" id="UserDialogResponse" style="display:none;" onload="javascript:var t=this.contentDocument.body.innerHTML;if(t){$('UserDialog').innerHTML=t;};"></iframe>
				<div id="UserDialog"><div id="UserDialogWait" class="hidden" /></div></div>
				<script language="javascript" type="text/javascript">try {if($('header')){$('OperationDialogEmbedder').click();}}catch(e){};</script>
				<div id="aPanel">
<%
					attributes.paint(p, false);
					p.flush();
%>
				</div>
<%
				// Reference panes
				ReferencePaneControl[] referencePaneControls = inspectorControl.getReferencePaneControl();
				for(int i = 0; i < referencePaneControls.length; i++) {
					%><%@ include file="../Set-MultiDelete-include.jsp" %><%
					referencePaneControls[i].paint(
						p,
						ReferencePaneControl.FRAME_VIEW,
						false // forEditing
					);
				}
				p.flush();
%>
<%@ include file="../../../../show-footer.html" %>
			</div> <!-- content -->
		</div> <!-- content-wrap -->
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

	// ViewPort.Type.MOBILE
	else if(p.getViewPortType() == ViewPort.Type.MOBILE) {

		// Set header
		response.setHeader(
			"Cache-Control",
			"max-age=" + Integer.MAX_VALUE
		);
		response.setHeader(
			"Pragma",
			""
		);
		// Attributes
		Control attributes = inspectorControl.getAttributePaneControl();

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="content-type" content="text/html; charset=UTF-8">
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
  <meta name="viewport" content="width=320; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;">
  <meta name="apple-touch-fullscreen" content="YES" /> 
  <style type="text/css" media="screen">@import "./_style/ssf.css";</style>
  <style type="text/css" media="screen">@import "./_style/mobile/iui.css";</style>
  <style type="text/css" media="screen">@import "./_style/mobile/opencrx-iui.css";</style>
  <script type="text/javascript" src="javascript/portal-all.js"></script>
  <script type="application/x-javascript">      
    addEventListener(
      "load", 
      function() {
        setTimeout(updateLayout, 0);
      }, 
      false
    );  
    function updateLayout() {
      window.scrollTo(0, 1);
    } 
  </script>
</head>
<body orient="landscape">
    <div class="toolbar" style="font-size:17px;font-weight:bold;color:#dddddd;">
      <div style="width:90%">
<%
        NavigationControl.paintBreadcrum(
          p,
          false
        );
        NavigationControl.paintToggleViewPort(
          p,
          false
        );
        p.flush();
%>
      </div>
    </div>
<%
    attributes.paint(p, false);
    p.flush();
    ReferencePaneControl[] referencePaneControls = inspectorControl.getReferencePaneControl();
    for(int i = 0; i < referencePaneControls.length; i++) {
      referencePaneControls[i].paint(
        p,
        ReferencePaneControl.FRAME_VIEW,
        false // forEditing
      );
    }
    p.flush();
%>
</body>
</html>
<%
	}
	
	// ViewPort.Type.EMBEDDED
	else if(p.getViewPortType() == ViewPort.Type.EMBEDDED) {
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
