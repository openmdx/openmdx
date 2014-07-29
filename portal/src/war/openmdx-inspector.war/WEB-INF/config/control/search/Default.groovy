// Search
p.write("<form class=\"navbar-form navbar-right\" role=\"search\" name=\"SearchInput\" id=\"SearchInput\" action=\"./wizards/Search.jsp?" + org.openmdx.portal.servlet.Action.PARAMETER_REQUEST_ID + "=" + p.getView().getRequestId() + "\">");
p.write("  <div class=\"form-group\">");
p.write("    <input type=\"hidden\" name=\"" + org.openmdx.portal.servlet.Action.PARAMETER_REQUEST_ID + "\" value=\"" + p.getView().getRequestId() + "\"/>");
p.write("    <input type=\"text\" name=\"searchExpression\" id=\"searchExpression\" class=\"smallFont\" onmouseover=\"javascript:this.focus();\"/>");
p.write("  </div>");
p.write("  <button type=\"submit\" class=\"btn btn-sm\" style=\"background-color:transparent;\"><span class=\"glyphicon glyphicon-search\" style=\"color:grey\"></span></button>");
p.write("</form>");
