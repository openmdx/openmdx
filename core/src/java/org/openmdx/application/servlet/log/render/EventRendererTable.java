/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: EventRendererTable.java,v 1.1 2004/06/01 20:02:54 hburger Exp $
 * Description: Table event renderer
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/06/01 20:02:54 $
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


import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.openmdx.kernel.log.LogEvent;
import org.openmdx.kernel.log.LogUtil;


/**
 * The log event renderer interface
 */
public class EventRendererTable implements EventRenderer
{
	/**
	 * Renders log events as a HTML
	 *
     * @param writer        A writer
     * @param ctx           A console context
     * @param events        The events to render
     * @param formatter     A date formatter
	 */
	public void render(
            PrintWriter       writer,
            ConsoleContext    ctx,
            ArrayList         events,
			SimpleDateFormat  dateFormatter)
		throws IOException
	{
        LogEvent       event;
        Date  		   timestamp;
        String         evtTimestamp;
        String         evtLogLevel;
        String		   evtLineNr;
		ArrayList 	   evtDetail;


		// open HTML table and add the header
        writer.println("<table border=0 cellspacing=1 cellpadding=3>");
        writer.println("  <tbody>");
        writer.println("    <tr>");
        writer.println("      <td class=v12bg valign=bottom width=120>Timestamp</td>");
        writer.println("      <td class=v12bg valign=bottom width=60>Level</td>");
        writer.println("      <td class=v12bg valign=bottom width=120>Log Name</td>");
        writer.println("      <td class=v12bg valign=bottom width=120>Thread ID</td>");
        writer.println("      <td class=v12bg valign=bottom>Log Source</td>");
        writer.println("    </tr>");
        writer.println("    <tr>");
        writer.println("      <td class=v12bg valign=bottom></td>");
        writer.println("      <td class=v12bg valign=bottom colspan=4>Class Name</td>");
        writer.println("    </tr>");
        writer.println("    <tr>");
        writer.println("      <td class=v12bg valign=bottom></td>");
        writer.println("      <td class=v12bg valign=bottom colspan=3>Method Name</td>");
        writer.println("      <td class=v12bg valign=bottom>Line nr</td>");
        writer.println("    </tr>");
        writer.println("    <tr>");
        writer.println("      <td class=v12bg valign=bottom></td>");
        writer.println("      <td class=v12bg valign=bottom colspan=4>Summary</td>");
        writer.println("    </tr>");
        writer.println("    <tr>");
        writer.println("      <td class=v12bg valign=bottom></td>");
        writer.println("      <td class=v12bg valign=bottom colspan=4>(Detail)</td>");
        writer.println("    </tr>");



		Iterator iterator = events.iterator();
		while(iterator.hasNext()) {
			event = (LogEvent)iterator.next();

			timestamp = event.getTime();

			evtTimestamp = (timestamp == null) ? "?" : dateFormatter.format(event.getTime());
			evtLogLevel  = LogUtil.logLevelToStringLong(event.getLoggingLevel());
			evtDetail    = event.getLogStringDetailAsList();
			evtLineNr    = (event.getLineNr() > 0) ? String.valueOf(event.getLineNr()) : "";

	        writer.println("<tr>");
	        writer.println("  <td height=10 colspan=5></td>");
	        writer.println("</tr>");

	        writer.println("<tr>");
	        writer.println("  <td class=spalte2 align=left valign=top >" + evtTimestamp + "</td>");
	        writer.println("  <td class=spalte2 align=left valign=top >" + evtLogLevel + "</td>");
	        writer.println("  <td class=spalte2 align=left valign=top >" + event.getLogName() + "</td>");
	        writer.println("  <td class=spalte2 align=left valign=top >" + event.getThreadname() + "</td>");
	        writer.println("  <td class=spalte2 align=left valign=top >" + HtmlUtilities.filter(event.getLogSource()) + "</td>");
	        writer.println("</tr>");

	        writer.println("<tr>");
	        writer.println("  <td class=spalte1 align=left valign=top ></td>");
	        writer.println("  <td class=spalte1 align=left valign=top colspan=4>" + event.getClassName() + "</td>");
	        writer.println("</tr>");

	        writer.println("<tr>");
	        writer.println("  <td class=spalte1 align=left valign=top ></td>");
	        writer.println("  <td class=spalte1 align=left valign=top colspan=3>" + event.getMethodName() + "</td>");
	        writer.println("  <td class=spalte1 align=left valign=top>" + evtLineNr + "</td>");
	        writer.println("</tr>");

	        writer.println("<tr>");
	        writer.println("  <td class=spalte1 align=left valign=top ></td>");
	        writer.println("  <td class=spalte1 align=left valign=top colspan=4>" + HtmlUtilities.filter(event.getLogStringSummary()) + "</td>");
	        writer.println("</tr>");

			if ((evtDetail != null) && (evtDetail.size() > 0)) {
		        writer.println("<tr>");
	        	writer.println("  <td class=spalte1 align=left valign=top ></td>");
			    writer.println("  <td class=spalte1 align=left valign=top colspan=4>");
		        for(int ii=0; ii<evtDetail.size(); ii++) {
		        	if (ii > 0) writer.println("<br>");
			        writer.println(HtmlUtilities.filter((String)evtDetail.get(ii)));
		        }
			    writer.println("</td>");
		        writer.println("</tr>");
			}
		}

		if (events.size() == 0) {
	        writer.println("<tr>");
	        writer.println("  <td height=10 colspan=5></td>");
	        writer.println("</tr>");

	        writer.println("<tr>");
	        writer.println("  <td class=spalte2 align=left valign=top></td>");
	        writer.println("  <td class=spalte2 align=left valign=top colspan=4>[end of entity]</td>");
	        writer.println("</tr>");
		}

		// close HTML table
        writer.println("  </tbody>");
        writer.println("</table>");
	}
	
	/**
	 * Returns the preferred HTML table witdh
	 *
	 * @return  the table witdh  "100%", "1200", ...
	 */
	public String getPreferredTableWidth()
	{
		return "100%";
	}

}

