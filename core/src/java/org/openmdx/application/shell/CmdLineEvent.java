/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CmdLineEvent.java,v 1.1 2009/01/13 23:51:08 wfro Exp $
 * Description: Command line handler events
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 23:51:08 $
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
package org.openmdx.application.shell;


import java.util.EventObject;



/**
 * Implements a command line event that application may handle
 */
public class CmdLineEvent extends EventObject {

	/**
     * 
     */
    private static final long serialVersionUID = 3546362816501461299L;

    /** 
	 * Event type: CMDLINE_BAD_ARGS
	 * 
	 * <p>The command line parsing failed to due wrong/missing command line 
	 * parameters. The event info holds a formatted usage text.
	 */
	public static final int CMDLINE_BAD_ARGS =1;

	/**
	 * Event type: CMDLINE_HELP_REQUEST 
	 * 
	 * <p>The caller requested a help text. The event info holds a formatted
     * help text.
	 */
	public static final int CMDLINE_HELP_REQUEST = 2;

	/** 
	 * Event type: CMDLINE_VERSION_REQUEST
	 * 
	 * <p>The caller requested the application's version. The event info holds 
	 * the version info.
	 */
	public static final int CMDLINE_VERSION_REQUEST = 3;

	/** 
	 * Event type: CMDLINE_TRACE
	 * 
	 * <p>A command line parser trace message if the command line parser trace 
	 * is activated. The event info holds a trace message.
	 */
	public static final int CMDLINE_TRACE = 4;


	/**
	 * Creates a new command line event
	 * 
	 * @param source
	 * @param type
	 * @param text
	 */
	public CmdLineEvent(
			Object source, 
			int type, 
			String info)
	{
		super(source);
		
		this.type = type;
		this.info = info;
	}


	/**
	 * Returns a String representation of this CmdLineEvent
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "";
	}


	/**
	 * Returns the associated info.
	 * 
	 * @return String 
	 */
	String getInfo()
	{
		return this.info;
	}


	/**
	 * Returns the event type
	 * 
	 * @return int
	 */
	int getType()
	{
		return this.type;
	}



	private int type;
	private String info;
}


