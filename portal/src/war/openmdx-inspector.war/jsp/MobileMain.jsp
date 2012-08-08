<%@  page contentType= "text/html;charset=UTF-8" language="java" pageEncoding= "UTF-8" %><%
/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: MobileMain.jsp,v 1.5 2009/10/16 21:46:27 wfro Exp $
 * Description: ShowObject.jsp
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/10/16 21:46:27 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2006, OMEX AG, Switzerland
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
org.openmdx.portal.servlet.view.*,
org.openmdx.portal.servlet.texts.*" %>
<%
	ApplicationContext app = (ApplicationContext)session.getValue(WebKeys.APPLICATION_KEY);
	ViewsCache viewsCache = (ViewsCache)session.getValue(WebKeys.VIEW_CACHE_KEY_SHOW);
	String requestId = request.getParameter(Action.PARAMETER_REQUEST_ID);
	if(app == null || viewsCache == null || viewsCache.getView(requestId) == null) {
		response.sendRedirect(
			request.getContextPath() + "/" + WebKeys.SERVLET_NAME
		);
		return;		
	}
	ShowObjectView view = (ShowObjectView)viewsCache.getView(requestId);
	Texts_1_0 texts = app.getTexts();
	ShowInspectorControl inspectorControl = view.getShowInspectorControl();
	ViewPort p = ViewPortFactory.openPage(
		view,
		request,
		out
	);
	String pathPrefix= "..";
	p.setResourcePathPrefix(pathPrefix + "/");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="content-type" content="text/html; charset=UTF-8">
  <title><%= app.getApplicationName() %></title>
  <meta name="viewport" content="width=320; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;">
  <meta name="apple-touch-fullscreen" content="YES" /> 
  <style type="text/css" media="screen">@import "<%= pathPrefix %>/_style/mobile/iui.css";</style>
  <style type="text/css" media="screen">@import "<%= pathPrefix %>/_style/mobile/opencrx-iui.css";</style>
  <script type="text/javascript" src="<%= pathPrefix %>/javascript/portal-all.js"></script>
  <script type="application/x-javascript">      
  	addEventListener(
		"load", 
		function() {
	  		setTimeout(updateLayout, 0);
		}, 
		false
    );  
    function updateLayout(
    ) {
		window.scrollTo(0, 1);
    }      
  </script>
</head>
<body orient="landscape">
    <div class="toolbar" style="font-size:17px;font-weight:bold;color:#dddddd;">
      <div style="width:90%">
      	<a href="<%=pathPrefix %>/jsp/MobileMain.jsp?<%= Action.PARAMETER_REQUEST_ID %>=<%= requestId %>"><%= app.getApplicationName() %></a>
<%
		NavigationControl.paintToggleViewPort(
		  p,
		  false
		);
		p.flush();
%>
      </div>
    </div>
<%    
	SessionInfoControl.paintRolesMenu(p, false);
	p.flush();
%>
    <ul id="menu" title="Menu" selected="true" style="position:relative;top:auto;">
<%    
		RootMenuControl.paintTopNavigation(p);
		RootMenuControl.paintQuickAccessors(p);
		p.flush();
%>
   </ul>
</body>
</html>
