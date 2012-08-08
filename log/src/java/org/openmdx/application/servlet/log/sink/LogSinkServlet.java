/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LogSinkServlet.java,v 1.1 2008/03/21 18:21:52 hburger Exp $
 * Description: Log sink servlet
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:21:52 $
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
package org.openmdx.application.servlet.log.sink;


import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmdx.application.log.AppLog;
import org.openmdx.application.servlet.log.render.HttpConsoleRenderer;



/**
 * The log sink servlet
 */
public class LogSinkServlet extends HttpServlet {


	/**
     * 
     */
    private static final long serialVersionUID = 3256728359789474608L;


    /**
	 * The log console handler
	 */
    public LogSinkServlet(
    ){
        super();
    }


	/**
	 * The handler initialisation
	 */
	public void init()
	{
		AppLog.info("LogSinkServlet initialized");
	}


	/**
	 * Handles the HTTP GET request
	 *
	 * @param request  A HTTP request
	 * @param response A HTTP response
	 */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
    	HttpConsoleRenderer.handleLogRemoteAction(
					        		request.getParameter("appid"),
					        		request.getParameter("cfgname"),
					        		request.getParameter("logname"),
					        		request.getParameter("timestamp"),
					        		request.getParameter("loglevel"),
					        		request.getParameter("logsource"),
					        		request.getParameter("hostname"),
					        		request.getParameter("processid"),
					        		request.getParameter("threadid"),
					        		request.getParameter("class"),
					        		request.getParameter("method"),
					        		request.getParameter("line"),
					        		request.getParameter("summary"),
					        		request.getParameter("detail"));

        response.setContentType("text/html");
        response.setContentLength(0);
    }


	/**
	 * Handles the HTTP POST request
	 *
	 * @param request  A HTTP request
	 * @param response A HTTP response
	 */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        doGet(request, response); // delegate it to the HTTP GET handler
    }


//	/** the timestamp format used by the remote log */
//	private static SimpleDateFormat remoteLogDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSS'Z'");
}
