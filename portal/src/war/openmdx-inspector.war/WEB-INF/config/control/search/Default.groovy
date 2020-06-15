// Search
p.write("<form class=\"form-inline\" role=\"search\" name=\"SearchInput\" id=\"SearchInput\" action=\"./wizards/Search.jsp?" + org.openmdx.portal.servlet.Action.PARAMETER_REQUEST_ID + "=" + p.getView().getRequestId() + "\">");
p.write("  <div class=\"form-group\">");
p.write("    <input type=\"hidden\" name=\"" + org.openmdx.portal.servlet.Action.PARAMETER_REQUEST_ID + "\" value=\"" + p.getView().getRequestId() + "\"/>");
p.write("    <input type=\"text\" name=\"searchExpression\" id=\"searchExpression\" class=\"form-control\" onmouseover=\"javascript:this.focus();\"/>");
p.write("  </div>");
p.write("  <button type=\"submit\" class=\"btn btn-sm\" style=\"background-color:transparent;\"><img src=\"./images/search_panel.gif\" alt=\"v\" border=\"0\" align=\"bottom\"/></button>");
p.write("</form>");
