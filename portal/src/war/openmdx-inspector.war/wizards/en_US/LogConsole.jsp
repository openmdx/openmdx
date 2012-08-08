<%@ page contentType= "text/html;charset=UTF-8" language="java" pageEncoding= "UTF-8" %>
<%  request.setCharacterEncoding("UTF-8"); %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>

<head>
  <title>openCRX Log Console</title>
  <meta name="label" content="openCRX Log Console">
  <meta name="toolTip" content="openCRX Log Console">
  <meta name="targetType" content="_blank">
  <meta name="forClass" content="org:opencrx:kernel:admin1:Segment"> 
  <meta name="order" content="110">
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8">

</head>

<body>
  forwarding to the openCRX Log Console...
</body>

</html>
<%
  response.sendRedirect("../../LogConsoleServlet/");
%>
