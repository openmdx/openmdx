<%@ page contentType= "text/html;charset=UTF-8" language= "java" pageEncoding= "UTF-8" %><%
/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: Login.jsp,v 1.71 2010/11/17 13:11:07 wfro Exp $
 * Description: Login.jsp
 * Revision:    $Revision: 1.71 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/11/17 13:11:07 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 *
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
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
<%@ page session="true" import="
java.util.*,
java.net.*,
java.util.Enumeration,
java.io.PrintWriter,
org.openmdx.portal.servlet.*
"%>
<%
	// Handle getPath request. Return servlet path and query string as plain text.
	// This function can be used by Ajax clients to test whether they are redirected
	// to the login page or the session is already authenticated
	if(request.getParameter("getPath") != null && "true".equals(request.getParameter("getPath"))) {
%>
		<%= request.getServletPath() + "?" + request.getQueryString() %>
<%
	}
	// Login form
	else {
		request.setCharacterEncoding("UTF-8");
		String requestURL = request.getRequestURL().toString();
		System.out.println(new Date() + ": Login: requestURL=" + requestURL + "; isRequestedSessionIdFromCookie=" + request.isRequestedSessionIdFromCookie() + "; servletPath=" + request.getServletPath() + "; remoteUser=" + request.getRemoteUser());

		// Locale
		if(request.getParameter("locale") != null) {
			request.getSession().setAttribute(
				"locale",
				request.getParameter("locale")
			);
		}
		else {
			ApplicationContext app = (ApplicationContext)session.getAttribute(WebKeys.APPLICATION_KEY);
			request.getSession().setAttribute(
				"locale",
				app == null ?
					(request.getHeader("accept-language") == null || request.getHeader("accept-language").length()<5) ?
						null :
						request.getHeader("accept-language").substring(0,2) + "_" + request.getHeader("accept-language").substring(3) :
					app.getCurrentLocaleAsString()
			);
		}		
		String localeStr = (String)session.getAttribute("locale");
		if(localeStr != null && localeStr.length() > 5) {
			localeStr = localeStr.substring(0, 5);
		}
		String defaultLocale = "en_US";
		List activeLocales = new ArrayList();
%><%@ include file="login-locales.jsp" %><%
		if(!activeLocales.contains(defaultLocale)) {
			activeLocales.add(defaultLocale);
		}
		if((localeStr == null) || !activeLocales.contains(localeStr)) {
			localeStr = defaultLocale;
		}
		request.getSession().setAttribute("locale", localeStr);
		
		// Timezone		
		if(request.getParameter("timezone") != null) {
			request.getSession().setAttribute(
				"timezone",
				request.getParameter("timezone")
			);
		}
		localeStr = (String)session.getAttribute("locale");
		String timezone = (String)session.getAttribute("timezone");
		boolean loginFailed = "true".equals((String)request.getSession().getAttribute("loginFailed"));
		request.getSession().setAttribute("loginFailed", "false");
		// Set default timezone
		if(timezone == null) {
			timezone = TimeZone.getDefault().getID();
			request.getSession().setAttribute("timezone", timezone);
		}
		// servletUrl
		String queryString = request.getQueryString();
		if(queryString != null) {
			queryString = queryString.replace("getPath=true", "getPath=false");
		}
	    String servletUrl = request.getContextPath() + "/" + WebKeys.SERVLET_NAME  + (queryString == null ? "" : "?" + queryString);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html dir="<%= dir.get(localeStr) %>" style="background:white;">
<head>
	<title>Login</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta http-equiv="Expires" content="0">
	<meta name="viewport" content="width=320; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;">
	<meta name="apple-touch-fullscreen" content="YES" />
	<link href="<%=request.getContextPath()%>/_style/colors.css" rel="stylesheet" type="text/css">
	<script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/javascript/prototype.js"></script>
	<!--[if lt IE 7]><script type="text/javascript" src="<%=request.getContextPath()%>/javascript/iehover-fix.js"></script><![endif]-->
	<link href="<%=request.getContextPath()%>/_style/ssf.css" rel="stylesheet" type="text/css">
	<link href="<%=request.getContextPath()%>/_style/n2default.css" rel="stylesheet" type="text/css">
	<link href="<%=request.getContextPath()%>/javascript/yui/build/assets/skins/sam/container.css" rel="stylesheet" type="text/css">
	<link rel='shortcut icon' href='<%=request.getContextPath()%>/images/favicon.ico' />

 	<script language="javascript" type="text/javascript">

 		function checkRedirect() {
		    var isRedirected = false;
 			var isEmbedded = <%= !loginFailed && (queryString == null || queryString.indexOf("loginFailed") == -1) ? "true" : "false" %>;
 			// Do not embed the login page in another page
 			if(isEmbedded) {
				window.location.href = window.location.href + '?loginFailed=false';
				isRedirected = true;
 			}
 			else {
				var servletUrl = '<%= servletUrl %>';
				new Ajax.Request(
					'<%=request.getContextPath()%>/jsp/GetPath.jsp?getPath=true',
		 			{
		 			    method:'get',
		 			    asynchronous:false,
		 			    onSuccess: function(transport){
							var responseText = transport == null || transport.responseText == null ?
								"no response text" :
									transport.responseText;
							if(responseText.indexOf("/Login.jsp") == -1) {
								window.location.href = servletUrl;
								isRedirected = true;
							}
		 			    },
		 			    onFailure: function(){
		 	 			}
					}
				);
				// The previous Ajax request destroys the redirect URL. Fix it.
				new Ajax.Request(
					servletUrl,
		 			{
		 			    method:'get',
		 			    asynchronous:false,
		 			    onSuccess: function(transport){
		 			    },
		 			    onFailure: function(){
		 	 			}
					}
				);
 			}
			return isRedirected;
 		}
 	</script>
</head>
<body class="yui-skin-sam" style="border:0px solid white;" onLoad="javascript:document.forms.formLogin.j_username.focus();">
<script language="javascript" type="text/javascript">
 	checkRedirect();
</script>
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
  <div id="login" style="position:relative;text-align:center;margin-left:auto;margin-right:auto;padding-top:15em;">
  <%@ include file="login-header.html" %>
  <form name="formLogin" method="POST" action="j_security_check" accept-charset="UTF-8">
    <table style="text-align:left;border-collapse:collapse;margin-left:auto;margin-right:auto;width:550px;border:solid 1px #DDDDDD;">
      <tr>
        <td colspan="2" width="100%" style="vertical-align: middle;padding:8px;white-space:nowrap;">
          <span style="font-size:14pt;font-weight:bold;"><%= textsLogin.get(localeStr) %></span>
        </td>
        <td style="vertical-align: middle; padding-right:5px;white-space:nowrap;">
          <ul dir="ltr" id="nav" class="nav" style="width:220px;" onmouseover="sfinit(this);">
            <li id="flyout" style="border-top: solid 1px #DDDDDD;border-bottom: solid 1px #DDDDDD;"><a href="#"><img src="<%=request.getContextPath()%>/images/panel_down.gif" alt="" style="border:none 0px white;float:right;top:-20px;" /><%= localeStr %> - <%= textsLocale.get(localeStr) %>&nbsp;</a>
              <ul onclick="this.style.left='-999em';" onmouseout="this.style.left='';">
<%
                for (int i = 0; i < activeLocales.size(); i++) {
%>
					<li><a href="#" onclick="javascript:window.location.href='Login.jsp?locale=<%= activeLocales.get(i).toString() %>&timezone=<%= URLEncoder.encode(timezone) %>';"><span style="font-family:courier;"><%= activeLocales.get(i).toString() %>&nbsp;&nbsp;</span><%= textsLocale.get(activeLocales.get(i)).toString() %></a></li>
<%
                }
%>
              </ul>
            </li>
          </ul>
        </td>
      </tr>
      <tr>
        <td colspan="3">
          <noscript>
            <div class="panelJSWarning" style="display: block;">
              <a href="helpJsCookie_<%= localeStr %>.html" target="_blank"><img class="popUpButton" src="<%=request.getContextPath()%>/images/help.gif" width="16" height="16" border="0" alt="" /></a> <%= textsJavaScript.get(localeStr) %>
            </div>
          </noscript>
          <div id="cookieWarningBlock" class="panelCookieWarning" style="display: none;">
            <a href="helpJsCookie_<%= localeStr %>.html" target="_blank"><img class="popUpButton" src="<%=request.getContextPath()%>/images/help.gif" width="16" height="16" border="0" onclick="javascript:void(window.open('helpJsCookie_<%= localeStr %>.html', 'Help', 'fullscreen=no,toolbar=no,status=no,menubar=no,scrollbars=yes,resizable=yes,directories=no,location=no,width=400'));" alt="" /></a> <%= textsSessionCookie.get(localeStr) %>
          </div>
        </td>
      </tr>
      <tr>
        <td colspan="3"><div style="height:30px;"></div></td>
      </tr>
      <tr>
        <td style="vertical-align: middle;padding-left:8px;padding-right:5px;" nowrap>
          <%= textsUsername.get(localeStr) %>:
	      </td>
	      <td style="vertical-align: middle;padding-right:8px;">
	        <input type="text" name="j_username" title="<%= textsUsername.get(localeStr) %>">
	      </td>
	      <td>
	      </td>
      </tr>
      <tr>
        <td colspan="3" style="height:2px;"></td>
      </tr>
      <tr>
        <td style="vertical-align: middle;padding-left:8px;padding-right:5px;" nowrap>
	        <%= textsPassword.get(localeStr) %>:
	      </td>
	      <td style="vertical-align: middle;padding-right:8px;">
	        <input type="password" name="j_password" title="<%= textsPassword.get(localeStr) %>">
	      </td>
	      <td nowrap style="vertical-align: middle;padding-right:8px;">
	        <span style="vertical-align: bottom;"><input class="submit" type="submit" name="button" value="<%= textsLogin.get(localeStr) %>" onclick="if($('loginFailedHint')){$('loginFailedHint').style.display='none'};if(checkRedirect()){return false;}else{$('flyout').style.display='none';$('wait').style.visibility='visible';this.disabled=true;this.form.submit();};" >&nbsp;<img id="wait" src="<%=request.getContextPath()%>/images/wait.gif" alt="" title="" style="visibility:hidden;" /></span>
	      </td>
      </tr>
      <tr>
        <td colspan="3" style="height:30px;"></td>
      </tr>
<%
      if(loginFailed) {
%>
	      <tr id="loginFailedHint">
	        <td colspan="3" class="cellErrorRight" style="padding:5px;">
	          &nbsp;<b><%= textsLoginFailed.get(localeStr) %></b>
	        </td>
	      </tr>
<%
      }
%>
      <tr>
        <td colspan="3">
          <%@ include file="login-note.html" %>
        </td>
      </tr>
    </table>
  </form>
  </div>
  <div style="height:100px;"></div>
  <%@ include file="login-footer.html" %>
  <script language="javascript" type="text/javascript">
	if(<%= !request.isRequestedSessionIdFromCookie() && (request.getParameter("cookieError") != null) %>) {
      if($('cookieWarningBlock')) {
        $('cookieWarningBlock').style.display = 'block';
      }
    }
  </script>
</body>
</html>
<%
	}
%>
