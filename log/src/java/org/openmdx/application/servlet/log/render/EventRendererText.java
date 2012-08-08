/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: EventRendererText.java,v 1.1 2008/03/21 18:21:49 hburger Exp $
 * Description: Text event renderer
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:21:49 $
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
import java.util.Iterator;

import org.openmdx.compatibility.kernel.log.LogEvent;


/**
 * The log event renderer interface
 */
public class EventRendererText implements EventRenderer
{
	/**
	 * Renders log events as plain text
	 *
     * @param writer        A writer
     * @param ctx           A console context
     * @param events        The events to render
     * @param formatter     A date formatter
	 */
	public void render(
            PrintWriter        writer,
            ConsoleContext     ctx,
            ArrayList<LogEvent>          events,
			SimpleDateFormat   dateFormatter)
		throws IOException
	{
		Iterator<LogEvent> iterator = events.iterator();
		while(iterator.hasNext()) {
			// A LogFormatter object is not threadsafe!
			synchronized(ctx.textFormatter) {
                 writer.println(ctx.textFormatter.format((LogEvent)iterator.next()));
			}
			
	        writer.println("<br>");
		}

		if (events.size() == 0)  writer.println("[end of entity]<br>");
	}
	
	/**
	 * Returns the preferred HTML table witdh
	 *
	 * @return  the table witdh  "100%", "1200", ...
	 */
	public String getPreferredTableWidth()
	{
		return "1400";
	}

}
