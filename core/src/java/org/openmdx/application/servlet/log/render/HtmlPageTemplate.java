/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: HtmlPageTemplate.java,v 1.4 2007/10/10 17:16:04 hburger Exp $
 * Description: Webserver Test Main
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 17:16:04 $
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
package org.openmdx.application.servlet.log.render;


import java.io.PrintWriter;
import java.util.ArrayList;


/**
 * A  HTML page template
 */
public class HtmlPageTemplate
{
	static private class HtmlLink
	{
		HtmlLink(String uri, String name) { this.uri = uri; this.name = name; }

		public String getUri() { return this.uri; }
		public String getName() { return this.name; }

		private String uri;
		private String name;
	}


	public HtmlPageTemplate(String root)
	{
		this(root, "openMDX");
	}

	public HtmlPageTemplate(String contextPath, String htmlTitle)
	{
		if (contextPath.endsWith("/")) {
			this.contextPath = contextPath.substring(0, contextPath.length()-1);
		}
        else {
			this.contextPath = contextPath;
		}

		this.htmlTitle = htmlTitle;
	}


	public void setHeading(String heading)
	{
		this.heading = heading == null ? new String() : heading;
	}


	public void addMenuLink(String uri, String name)
	{
		if (uri == null) {
			htmlMenuLinks.add(null);
		}
        else {
			htmlMenuLinks.add(new HtmlLink(uri, name));
		}
	}

	public void addMenuLink()
	{
		addMenuLink(null, null);
	}


	public void setTitleImage(String uri)
	{
		this.titleImgUri = uri;
	}

	public void enableCaching(boolean state)
	{
		this.noCaching = state;
	}


    public void renderHead(PrintWriter  writer)
    {
        writer.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        writer.println("<html>");
        writer.println("<head>");
        writer.println("  <title>" + this.htmlTitle + "</title>");
        writer.println("");
        writer.println("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
        if (this.noCaching) {
	        writer.println("  <meta http-equiv=\"expire\" content=\"0\">");
	        writer.println("  <meta http-equiv=\"Pragma\" content=\"no-cache\">");
	        writer.println("  <meta http-equiv=\"Cache-Control\" content=\"no-cache\">");
	    }
        writer.println("");
        writer.println("  <link rel=\"stylesheet\" href=\""+this.contextPath+"/_style/logconsole.css\" type=\"text/css\">");
        writer.println("</head>");
        writer.println("");
        writer.println("<body text=\"#000000\" bgcolor=\"#ffffff\" background=\""+this.contextPath+"/images/logconsole/background.gif\">");
        writer.println("<div align=\"center\">");
        writer.println("");
        writer.println("<table width=100% border=0 cellspacing=0 cellpadding=0>");
        writer.println("  <tbody>");
        writer.println("    <tr>");
        writer.println("      <td valign=\"center\" align=\"left\" bgcolor=\"#FFFFFF\">");
        writer.println("          <p class=\"header_logo\"><img src=\"" + this.contextPath + "/" + this.titleImgUri + "\" ></p>");
        writer.println("      </td>");
        writer.println("      <td valign=\"center\" align=\"left\" bgcolor=\"#000066\">");
        writer.println("          <p class=\"header_title\">" + this.htmlTitle + "</p>");
        writer.println("      </td>");
        writer.println("    </tr>");
        writer.println("    <tr>");
        writer.println("      <td width=\"142\" valign=\"top\" bgcolor=\"#e8e8e8\">");
        writer.println("        <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"4\">");
        writer.println("          <!-- BEGIN MENU -->");
        writer.println("          <tbody>");

		// Links 'menu'
        if (!this.htmlMenuLinks.isEmpty()) {
        	HtmlLink  link;

        	for(int ii=0; ii<this.htmlMenuLinks.size(); ii++) {
        		link = (HtmlLink)this.htmlMenuLinks.get(ii);
        		if (link == null) {
                	writer.println("<tr><td><p>&nbsp;</p></td></tr>");
        		}
                else {
		            writer.println("<tr><td><div align=right> <a href=\"" + link.getUri() + "\" class=sub_menu>" + link.getName() + "</a> </div></td></tr>");
        		}
        	}
        }

        writer.println("        </tbody>");
        writer.println("      </table>");
        writer.println("    </td>");
        writer.println("");
        writer.println("    <!-- BEGIN CONTENT -->");
        writer.println("    <td>");
        writer.println("      <table width=100% border=0 cellspacing=0 cellpadding=6 bgcolor=#ffffff>");
        writer.println("        <tbody>");
        writer.println("          <tr><td height=18></td></tr>");
        writer.println("          <tr>");
        writer.println("            <td>");
        writer.println("              <p class=\"main_title\">" + this.heading + "</p>");
    }

    public void renderTail(PrintWriter  writer)
    {
        writer.println("            </td>");
        writer.println("          </tr>");
        writer.println("        </tbody>");
        writer.println("      </table>");
        writer.println("    </td>");
        writer.println("  </tr>");
        writer.println("</tbody>");
        writer.println("</table>");
        writer.println("");

        writer.println("");
        writer.println("</div>");
        writer.println("<br>");
        writer.println("</body>");
        writer.println("</html>");
    }


	private ArrayList     htmlMenuLinks  = new ArrayList();
	private String        titleImgUri    = new String();
	private String        heading        = new String();
	private String        contextPath;
	private String        htmlTitle;
    private boolean       noCaching      = true;
}
