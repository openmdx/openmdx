<%@ page contentType= "text/html;charset=UTF-8" language= "java" pageEncoding= "UTF-8" %><%
/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Logoff.jsp,v 1.7 2007/11/15 18:53:36 wfro Exp $
 * Description: LoginFailed.jsp
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/11/15 18:53:36 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 */
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html;CHARSET=utf-8">
  <title>Login Failed</title>
</head>

<%@ page import="
java.util.*,
java.net.*,
java.io.*,
javax.servlet.*
"%>
<%
request.setCharacterEncoding("UTF-8");
%>

<body>
<%
  if(request.getSession().getAttribute("ObjectInspectorServlet.ApplicationContext") != null) {
      System.out.println(new Date() + ": Logoff: removing application context");
      request.getSession().removeAttribute("ObjectInspectorServlet.ApplicationContext");
  }

  System.out.println(new Date() + ": Logoff: requestURL=" + request.getRequestURL());
  String locale = request.getParameter("locale");
  if(locale == null) {
      locale = (String)request.getSession().getAttribute("locale");  
  }
  String timezone = request.getParameter("timezone");
  if(timezone == null) {
      timezone = (String)request.getSession().getAttribute("timezone");  
  }
  System.out.println(new Date() + ": Logoff: invalidate session. locale=" + locale + "; timezone=" + timezone);  
  session.invalidate();
  // NO session management beyond this point.
  // Otherwise WebSphere 5 fails
  response.sendRedirect("Login?locale=" + locale + (timezone == null ? "" : "&timezone=" + URLEncoder.encode(timezone)));  
%>
</body>
</html>
