// Search
p.write("<div id=\"searchButton\" onclick=\"javascript:\$('SearchInput').submit();\">&nbsp;</div>");
p.write("<div id=\"searchBox\">");
p.write("  <form name=\"SearchInput\" id=\"SearchInput\" action=\"./wizards/Search.jsp?" + org.openmdx.portal.servlet.Action.PARAMETER_REQUEST_ID + "=" + p.getView().getRequestId() + "\">");
p.write("    <input type=\"hidden\" name=\"" + org.openmdx.portal.servlet.Action.PARAMETER_REQUEST_ID + "\" value=\"" + p.getView().getRequestId() + "\"/>");
p.write("    <input type=\"text\" name=\"searchExpression\" id=\"searchExpression\" class=\"smallFont\" onmouseover=\"javascript:this.focus();\"/>");
p.write("  </form>");
p.write("  <script language=\"javascript\" type=\"text/javascript\">function setFocusSearch(){try{document.forms.SearchInput.searchExpression.focus();}catch(e){}}; YAHOO.util.Event.addListener(window, \"load\", setFocusSearch);</script>");
p.write("</div>");
