<%@ page contentType= "text/html;charset=UTF-8" language="java" pageEncoding= "UTF-8" %><%
/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: edit-Default.jsp,v 1.37 2008/06/01 16:40:52 wfro Exp $
 * Description: edit-Default.jsp
 * Revision:    $Revision: 1.37 $
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
java.util.*,
java.text.*,
java.math.*,
org.openmdx.portal.servlet.*,
org.openmdx.portal.servlet.view.*,
org.openmdx.portal.servlet.control.*,
org.openmdx.compatibility.base.naming.*,
org.openmdx.portal.servlet.texts.*
" %><%
	ApplicationContext app = (ApplicationContext)session.getValue(WebKeys.APPLICATION_KEY);
	ViewsCache viewsCache = (ViewsCache)session.getValue(WebKeys.VIEW_CACHE_KEY_EDIT);
	EditObjectView view = (EditObjectView)viewsCache.getView(request.getParameter(Action.PARAMETER_REQUEST_ID));
	PaintScope paintScope = PaintScope.valueOf(request.getParameter(Action.PARAMETER_SCOPE));
	Texts_1_0 texts = app.getTexts();
	EditInspectorControl inspectorControl = view.getEditInspectorControl();
	HtmlPage p = HtmlPageFactory.openPage(
		view,
		request,
		out
	);

	// Prolog
	Control prolog = view.createControl(
		"prolog",
		PagePrologControl.class
	);

	// Epilog
	Control epilog = view.createControl(
		"epilog",
		PageEpilogControl.class
	);

	// Session info
	Control north = view.createControl(
		"north",
		SessionInfoControl.class
	);

	// Errors
	Control errors = view.createControl(
  		"errors",
  		ShowErrorsControl.class
  	);

	// Title
	Control title = view.createControl(
  		"title",
  		ObjectTitleControl.class
  	);

	// Attributes
	Control attributes = inspectorControl.getAttributePaneControl();

	// STANDARD
	if(view.getMode() == ViewMode.STANDARD) {
		EditObjectControl edit = (EditObjectControl)view.createControl(
			"editObject",
			EditObjectControl.class
		);
		edit.addControl(errors);
		edit.addControl(title);
		edit.addControl(attributes);
%>
<!--[if IE]><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"><![endif]-->
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html dir="<%= texts.getDir() %>">
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
    history.forward(); // prevent going back to this page by breaking history
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
<body class="ytheme-gray" onload="initPage();">
<iframe class="popUpFrame" id="DivShim" src="blank.html" scrolling="no" frameborder="0" style="position:absolute; top:0px; left:0px; display:none;"></iframe>
<%@ include file="../../../../edit-header.html" %>
<div id="header">&nbsp;
<%
	north.paint(p, true);
	p.flush();
%>&nbsp;
</div>
<%
	edit.paint(p, true);
	epilog.paint(p, true);
	p.close(false);
%>
<%@ include file="../../../../edit-note.html" %>
<%@ include file="../../../../edit-footer.html" %>
</body>
</html>
<%
	}
	// EMBEDDED
	else if(view.getMode() == ViewMode.EMBEDDED) {
		EditObjectControl edit = (EditObjectControl)view.createControl(
			"editObject",
			EditObjectControl.class
		);
		if(!app.getErrorMessages().isEmpty()) {
			edit.addControl(errors);
		}
		edit.addControl(attributes);
		edit.paint(p, true);
		p.flush();
		p.close(false);
%>
		<script language="javascript" type="text/javascript">
			//alert('postLoad edit');
		</script>
<%
	}
%>
