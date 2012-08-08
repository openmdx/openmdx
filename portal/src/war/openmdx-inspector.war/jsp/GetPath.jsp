<%
	// Return servlet path with query string for Ajax clients
	if(request.getParameter("getPath") != null && "true".equals(request.getParameter("getPath"))) {
%>
		<%= request.getServletPath() + "?" + request.getQueryString() %>
<%
	}
	// Redirect to portal servlet 
	else {
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>GetPath</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta http-equiv="Expires" content="0">
 	<script language="javascript" type="text/javascript">
	    var servletUrl = '../<%= org.openmdx.portal.servlet.WebKeys.SERVLET_NAME %>' + '<%= request.getQueryString() == null ? "" : "?" + request.getQueryString() %>';
		window.location.href = servletUrl;						
	</script>	
</head>
<body>
</body>
</html>
<%
	}
%>
