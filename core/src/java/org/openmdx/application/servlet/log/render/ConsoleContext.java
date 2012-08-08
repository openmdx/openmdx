/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ConsoleContext.java,v 1.2 2007/10/10 16:05:49 hburger Exp $
 * Description: Console context
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:05:49 $
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


import java.util.Hashtable;
import java.util.LinkedList;

import org.openmdx.kernel.log.LogEntity;
import org.openmdx.kernel.log.LogEntityReader;
import org.openmdx.kernel.log.LogEventFilter;
import org.openmdx.kernel.log.LogFormatter;
import org.openmdx.kernel.log.LogFormatterFactory;




/**
 * The Console context used in session.
 */
public class ConsoleContext
{
	public ConsoleContext(
	){
	    super();
	}
	
	/** 
	 * Reset the context
	 */
	public void reset()
    {
		this.entityPositions = new LinkedList();
		this.logReader       = null;
		this.logReaderPos    = 0;
		this.viewPageSize    = 200;
		this.logEntity       = null;

		// do not reset the fields
		//    - this.renderer
		//    - this.filter
		//    - this.textFormatter
		// for the user's convenience this fields survive.
    }

	/** A list containing the previously visted log 'pages'. */
	public LinkedList entityPositions = null;

	/** The currently used sink entity reader */
	public LogEntityReader  logReader = null;

	/** The current log reader position */
	public long  logReaderPos = 0;

	/** The currently used page size for viewing */
	public int viewPageSize = 200;

	/** The currently used entity (needed for state verification) */
	public LogEntity  logEntity = null;

	/** The log event filter */
	public LogEventFilter  filter = new LogEventFilter();

	/** A log formatter */
	public LogFormatter textFormatter = LogFormatterFactory.createHtmlFormatter(null);
	
	/** A log event renderer */
	public EventRenderer eventRenderer = new EventRendererTable();
	
	/** Monitor refresh rate in seconds */
	public static int  monitorRefreshRate = 10;
	
	/** Monitored log entities.
	 *  Key is a LogEntity, value is a MonitoredLogEntity
	 */
	public Hashtable monitoredLogEntities = new Hashtable();


	public static final int RENDERER_TEXT  = 0;
	public static final int RENDERER_TABLE = 1;
}

