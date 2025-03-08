<%@ page contentType= "text/html;charset=UTF-8" language= "java" pageEncoding= "UTF-8" %><%
/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: LoginFailed.jsp
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
  <title>Logoff</title>
	<link href="<%=request.getContextPath()%>/_style/n2default.css" rel="stylesheet" type="text/css">
</head>

<%@ page import="
java.util.*,
java.net.*,
java.io.*,
javax.servlet.*,
org.openmdx.portal.servlet.*
"%>
<%
request.setCharacterEncoding("UTF-8");
%>
<body style="border:0px solid white;">
<%
	String localeStr = null;
	try {
			localeStr = (String)session.getAttribute("locale");
	} catch (Exception e) {}
	if(localeStr != null && localeStr.length() > 5) {
		localeStr = localeStr.substring(0, 5);
	}
	// Load locale-specific texts (overload texts.properties with custom-specific texts)
	java.util.Properties texts = new java.util.Properties();
	String textsDir = "/WEB-INF/config/texts/" + localeStr + "/";
	texts.load(
		new java.io.InputStreamReader(
			request.getServletContext().getResourceAsStream(textsDir + "texts.properties"),
			"UTF-8"
		)
	);
	for(String textsPath: new TreeSet<String>(request.getServletContext().getResourcePaths(textsDir))) {
		if(!textsPath.endsWith("/texts.properties")) {
			texts.load(
				new java.io.InputStreamReader(
					request.getServletContext().getResourceAsStream(textsPath),
					"UTF-8"
				)
			);
		}
	}	
	String defaultLocale = "en_US";
	Map<String,String> activeLocales = new LinkedHashMap<String,String>();
	boolean wasAuthenticated = false;

	if(request.getSession().getAttribute("ObjectInspectorServlet.ApplicationContext") != null) {
		wasAuthenticated = true;
		System.out.println(#if CLASSIC_CHRONO_TYPES new java.util.Date() #else java.time.Instant.now() #endif + ": Logoff: removing application context");
		request.getSession().removeAttribute("ObjectInspectorServlet.ApplicationContext");
	}
	if(request.getSession().getAttribute("processingLogin") != null) {
		request.getSession().setAttribute("processingLogin", "false");
	}
	System.out.println(#if CLASSIC_CHRONO_TYPES new java.util.Date() #else java.time.Instant.now() #endif + ": Logoff: requestURL=" + request.getRequestURL());
	String locale = request.getParameter(org.openmdx.portal.servlet.WebKeys.LOCALE_KEY);
	if(locale == null) {
		locale = (String)request.getSession().getAttribute(org.openmdx.portal.servlet.WebKeys.LOCALE_KEY);  
	}
	String timezone = request.getParameter(org.openmdx.portal.servlet.WebKeys.TIMEZONE_KEY);
	if(timezone == null) {
		timezone = (String)request.getSession().getAttribute(org.openmdx.portal.servlet.WebKeys.TIMEZONE_KEY);  
	}
	Object initialScale = request.getParameter(org.openmdx.portal.servlet.WebKeys.INITIAL_SCALE_KEY);
	if(initialScale == null) {
		initialScale = request.getSession().getAttribute(org.openmdx.portal.servlet.WebKeys.INITIAL_SCALE_KEY);
	}
	System.out.println(#if CLASSIC_CHRONO_TYPES new java.util.Date() #else java.time.Instant.now() #endif + ": Logoff: invalidate session. locale=" + locale + "; timezone=" + timezone);
	session.invalidate();
  	if(wasAuthenticated) {
		// NO session management beyond this point.
		// Otherwise WebSphere 5 fails
		response.sendRedirect(
			"Login.jsp?" +
			org.openmdx.portal.servlet.WebKeys.LOCALE_KEY + "=" + locale + 
			(timezone == null ? "" : "&" + org.openmdx.portal.servlet.WebKeys.TIMEZONE_KEY + "=" + URLEncoder.encode(timezone)) +
			(initialScale == null ? "" : "&" + org.openmdx.portal.servlet.WebKeys.INITIAL_SCALE_KEY + "=" + initialScale.toString()) +
			("&loginFailed=false")	
		);
	}
%>
<%@ include file="login-locales.jsp" %>
	<div id="header" style="height:90px;">
    <div id="logoTable" style="padding-left:10px;">
      <table dir="ltr" id="headerlayout" style="position:relative;">
        <tr id="headRow">
          <td id="head" colspan="2">
            <table id="info">
              <tr>
                <td id="headerCellLeft"><img id="logoLeft" style="cursor:default;" src="<%=request.getContextPath()%>/images/logoLeft.gif" alt="openCRX - limitless relationship management" title="openCRX - limitless relationship management" /></td>
                <td id="headerCellMiddle"></td>
                <td id="headerCellRight"><img id="logoRight" src="<%=request.getContextPath()%>/images/logoRight.gif" alt="" title="" /></td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </div>
  </div>
  <div id="login" style="position:relative;text-align:center;margin-left:auto;margin-right:auto;padding-top:10em;">
  <%@ include file="login-header.html" %>
</div>

	&nbsp;&nbsp;<input class="<%= CssClass.submit %>" type="submit" name="button" value="<%= texts.get("LoginText") == null ? "Login" :  texts.get("LoginText") %>" onclick="javascript:window.location.href='Login.jsp';" >

</body>
</html>
