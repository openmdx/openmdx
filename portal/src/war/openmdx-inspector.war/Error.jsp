<%@ page contentType= "text/html;charset=UTF-8" language= "java" pageEncoding= "UTF-8" %><%
/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: Error.jsp
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
 */
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html;CHARSET=utf-8">
  <meta http-equiv="Expires" content="0">
  <title>Error Page</title>
</head>
<%@ page import="
java.util.*,
java.net.*,
java.io.*,
javax.servlet.*
" %>
<body>
<%
	request.setCharacterEncoding("UTF-8");
	// Get locale
	String locale = request.getParameter(org.openmdx.portal.servlet.WebKeys.LOCALE_KEY);
	if(locale == null) {
		try {
			locale = (String)request.getSession().getAttribute(org.openmdx.portal.servlet.WebKeys.LOCALE_KEY);
		} catch(Exception e) {}
	}
	String timezone = request.getParameter(org.openmdx.portal.servlet.WebKeys.TIMEZONE_KEY);
	if(timezone == null) {
		try {
			timezone = (String)request.getSession().getAttribute(org.openmdx.portal.servlet.WebKeys.TIMEZONE_KEY);
		} catch(Exception e) {}
	}
	Object initialScale = request.getParameter(org.openmdx.portal.servlet.WebKeys.INITIAL_SCALE_KEY);
	if(initialScale == null) {
		try {
			initialScale = request.getSession().getAttribute(org.openmdx.portal.servlet.WebKeys.INITIAL_SCALE_KEY);
		} catch(Exception e) {}
	}
	String loginFailed = request.getParameter("loginFailed");	
	System.out.println(#if CLASSIC_CHRONO_TYPES new java.util.Date() #else java.time.Instant.now() #endif + ": Error: login failed; locale=" + locale + "; timezone=" + timezone + "; requestURL=" + request.getRequestURL());
	// Invalidate sesion
	try {
		request.getSession().invalidate();
	} catch(Exception e) {}	
	// Forward to Login
	String cookieMissingHint = request.isRequestedSessionIdFromCookie() ? 
		"" : 
		"&cookieError=true";
	try {
		request.getSession().setAttribute("loginFailed", loginFailed == null ? "true" : loginFailed);
		request.getSession().setAttribute(org.openmdx.portal.servlet.WebKeys.LOCALE_KEY, locale);
		request.getSession().setAttribute(org.openmdx.portal.servlet.WebKeys.TIMEZONE_KEY, timezone);
		request.getSession().setAttribute(
			org.openmdx.portal.servlet.WebKeys.INITIAL_SCALE_KEY, 
			initialScale == null ? null : new java.math.BigDecimal(initialScale.toString())
		);
		request.getSession().setAttribute("processingLogin", "true");		
	} 
	catch(Exception e) {}
	String event = request.getParameter("event");
	String parameter = request.getParameter("parameter");
	response.sendRedirect(
		request.getContextPath() + "/ObjectInspectorServlet?" + org.openmdx.portal.servlet.WebKeys.LOCALE_KEY + "=" + locale +
		(timezone == null ? "" : "&" + org.openmdx.portal.servlet.WebKeys.TIMEZONE_KEY + "=" + URLEncoder.encode(timezone)) + 
		(initialScale == null ? "" : "&" + org.openmdx.portal.servlet.WebKeys.INITIAL_SCALE_KEY + "=" + initialScale.toString()) + 
		(event == null ? "" : "&event=" + URLEncoder.encode(event)) +
		(parameter == null ? "" : "&parameter=" + URLEncoder.encode(parameter)) +
		cookieMissingHint
	);
%>
</body>
</html>
