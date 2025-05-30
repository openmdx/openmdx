<%@ page contentType= "text/html;charset=UTF-8" language= "java" pageEncoding= "UTF-8" %><%
/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: Login.jsp
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
		System.out.println(SystemClock.getInstance().now() + ": Login: requestURL=" + requestURL + "; isRequestedSessionIdFromCookie=" + request.isRequestedSessionIdFromCookie() + "; servletPath=" + request.getServletPath() + "; remoteUser=" + request.getRemoteUser());

		// Locale
		if(request.getParameter(org.openmdx.portal.servlet.WebKeys.LOCALE_KEY) != null) {
			request.getSession().setAttribute(
				org.openmdx.portal.servlet.WebKeys.LOCALE_KEY,
				request.getParameter(org.openmdx.portal.servlet.WebKeys.LOCALE_KEY)
			);
		} else {
			ApplicationContext app = (ApplicationContext)session.getAttribute(WebKeys.APPLICATION_KEY);
			request.getSession().setAttribute(
				org.openmdx.portal.servlet.WebKeys.LOCALE_KEY,
				app == null ?
					(request.getHeader("accept-language") == null || request.getHeader("accept-language").length()<5) ?
						null :
						request.getHeader("accept-language").substring(0,2) + "_" + request.getHeader("accept-language").substring(3) :
					app.getCurrentLocaleAsString()
			);
		}
		String localeStr = (String)session.getAttribute(org.openmdx.portal.servlet.WebKeys.LOCALE_KEY);
		if(localeStr != null && localeStr.length() > 5) {
			localeStr = localeStr.substring(0, 5);
		}
		String defaultLocale = "en_US";
		Map<String,String> activeLocales = new LinkedHashMap<String,String>();
%><%@ include file="login-locales.jsp" %><%
		if((localeStr == null) || !activeLocales.containsKey(localeStr)) {
			localeStr = defaultLocale;
		}
		request.getSession().setAttribute(org.openmdx.portal.servlet.WebKeys.LOCALE_KEY, localeStr);
		// Timezone
		if(request.getParameter(org.openmdx.portal.servlet.WebKeys.TIMEZONE_KEY) != null) {
			request.getSession().setAttribute(
				org.openmdx.portal.servlet.WebKeys.TIMEZONE_KEY,
				request.getParameter(org.openmdx.portal.servlet.WebKeys.TIMEZONE_KEY)
			);
		}
		// Initial Scale
		if(request.getParameter(org.openmdx.portal.servlet.WebKeys.INITIAL_SCALE_KEY) != null) {
			try {
				request.getSession().setAttribute(
					org.openmdx.portal.servlet.WebKeys.INITIAL_SCALE_KEY,
					new java.math.BigDecimal(request.getParameter(org.openmdx.portal.servlet.WebKeys.INITIAL_SCALE_KEY))
				);
			} catch(Exception ignore) {
				boolean stop = true;
			}
		}
		localeStr = (String)session.getAttribute(org.openmdx.portal.servlet.WebKeys.LOCALE_KEY);
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
		String timezone = (String)session.getAttribute(org.openmdx.portal.servlet.WebKeys.TIMEZONE_KEY);
		java.math.BigDecimal initialScale = (java.math.BigDecimal)session.getAttribute(org.openmdx.portal.servlet.WebKeys.INITIAL_SCALE_KEY);
		boolean loginFailed = "true".equals((String)request.getSession().getAttribute("loginFailed"));
		request.getSession().setAttribute("loginFailed", "false");
		// Set default timezone
		if(timezone == null) {
			timezone = TimeZone.getDefault().getID();
			request.getSession().setAttribute(org.openmdx.portal.servlet.WebKeys.TIMEZONE_KEY, timezone);
		}
		// Set default initialScale
		if(initialScale == null) {
			initialScale = java.math.BigDecimal.ONE;
		}
		// servletUrl
		String queryString = request.getQueryString();
		if(queryString != null) {
			queryString = queryString.replace("getPath=true", "getPath=false");
		}
	    String servletUrl = request.getContextPath() + "/" + WebKeys.SERVLET_NAME  + (queryString == null ? "" : "?" + queryString);
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html dir="<%= texts.get("dir") %>">
<head>
	<title>Login</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta http-equiv="Expires" content="0">
	<meta name="viewport" content="width=320; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;">
	<meta name="apple-touch-fullscreen" content="YES" />

	<!-- Styles -->
	<link rel="stylesheet" href="js/bootstrap/css/bootstrap.min.css">
	<link rel="stylesheet" href="<%= request.getContextPath() %>/_style/ssf.css" >
	<link rel="stylesheet" href="<%= request.getContextPath() %>/_style/n2default.css" >
	<link rel="stylesheet" href="<%= request.getContextPath() %>/_style/colors.css">
	<link rel="stylesheet" href="<%= request.getContextPath() %>/_style/calendar-small.css">
	<link rel='shortcut icon' href='<%= request.getContextPath() %>/images/favicon.ico' />

	<!-- Libraries -->
	<script language="javascript" type="text/javascript" src="<%= request.getContextPath() %>/js/prototype.js"></script>

</head>
<body style="border:0px solid white;">
  <script language="javascript" type="text/javascript">

	// Check for redirect
	var isEmbedded = <%= !loginFailed && (queryString == null || queryString.indexOf("loginFailed") == -1) ? "true" : "false" %>;
	// Do not embed the login page in another page
	if(isEmbedded) {
		window.location.href = window.location.href + '?loginFailed=false';
	} else {
		var servletUrl = '<%= servletUrl %>';
		new Ajax.Request(
			'<%=request.getContextPath()%>/jsp/GetPath.jsp?getPath=true',
			{
				method:'get',
				asynchronous:false,
				onSuccess: function(transport){
					var responseText = transport == null || transport.responseText == null ? "no response text" : transport.responseText;
					if(responseText.indexOf("/Login.jsp") == -1) {
						window.location.href = servletUrl;
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
  </script>
  <div id="header">
    <div id="logoTable">
      <table dir="ltr" id="headerlayout" style="position:relative;">
        <tr id="headRow">
          <td id="head" colspan="2">
            <table id="info">
              <tr>
                <td id="headerCellLeft"><img id="logoLeft" style="cursor:default;" src="<%=request.getContextPath()%>/images/logoLeft.gif" alt="openCRX - limitless relationship management" title="openCRX - limitless relationship management" /></td>
                <td id="headerCellMiddle" style="background-image:url('./images/logoMiddle.gif');background-repeat:repeat-x;width:100%;">
                  <table>
                    <tr>
                      <td style="width:50px;" />
                      <td style="vertical-align: middle; padding-right:5px;white-space:nowrap;">
                        <ul dir="ltr" id="<%=CssClass.ssf_nav %>" class="<%=CssClass.ssf_nav %>" style="width:220px;" onmouseover="sfinit(this);">
                          <li id="flyout"><a href="#"><img src="<%=request.getContextPath()%>/images/panel_down.gif" alt="" style="border:none 0px white;float:right;top:-20px;" /><%= localeStr %> - <%= texts.get("LocaleTitle") %>&nbsp;</a>
                            <ul onclick="this.style.left='-999em';" onmouseout="this.style.left='';">
<%
                              for(String locale: activeLocales.keySet()) {
%>
                                <li><a href="#" onclick="javascript:window.location.href='Login.jsp?<%= org.openmdx.portal.servlet.WebKeys.LOCALE_KEY %>=<%= locale %>&<%= org.openmdx.portal.servlet.WebKeys.TIMEZONE_KEY %>=<%= URLEncoder.encode(timezone) %>&<%= org.openmdx.portal.servlet.WebKeys.INITIAL_SCALE_KEY %>=<%= initialScale %>';"><span style="font-family:courier;"><%= locale %>&nbsp;&nbsp;</span><%= activeLocales.get(locale) %></a></li>
<%
                              }
%>
                            </ul>
                          </li>
                        </ul>
                      </td>
                    </tr>
                  </table>
                </td>
                <td id="headerCellRight"><img id="logoRight" src="<%=request.getContextPath()%>/images/logoRight.gif" alt="" title="" /></td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </div>
  </div>
  <div class="container">
    <form role="form" class="form-signin" style="max-width:400px;margin:0 auto;" method="POST" action="j_security_check" accept-charset="UTF-8">
      <h2 class="form-signin-heading"><%= texts.get("LoginText") %></h2>
      <input type="text" name="j_username" autofocus="" placeholder="<%= texts.get("UsernameText") %>" class="form-control" />
      <input type="password" name="j_password" placeholder="<%= texts.get("PasswordText") %>" class="form-control" />
      <br />
      <button type="submit" class="btn btn-lg btn-primary btn-block"><%= texts.get("LoginText") %></button>
      <br />
      <%@ include file="login-note.html" %>      
<%
      if(loginFailed) {
%>
        <br />
        <div class="alert alert-danger text-center">
          <%= texts.get("LoginFailedText") %>
        </div>
<%
      }
%>
    </form>
  </div>
  <div style="height:100px;"></div>
  <%@ include file="login-footer.html" %>
</body>
</html>
<%
}
%>
