/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LogConsoleServlet.java,v 1.3 2008/03/31 09:02:06 wfro Exp $
 * Description: Log console servlet
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/31 09:02:06 $
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
package org.openmdx.application.servlet.log.console;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmdx.application.servlet.log.render.ConsoleContext;
import org.openmdx.application.servlet.log.render.HtmlPageTemplate;
import org.openmdx.application.servlet.log.render.HttpConsoleRenderer;

/**
 * The log console servlet
 */
public class LogConsoleServlet extends HttpServlet {


    /**
     * 
     */
    private static final long serialVersionUID = 3258133539878089525L;

    /**
     * The log console handler
     */
    public LogConsoleServlet(
    ){
        super();
    }


    /**
     * The handler initialisation
     */
    public void init()
    {
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
        HttpSession   session  = request.getSession(true);
    
    	if (this.pageTemplate == null) {
    		// Standard page template
    		this.pageTemplate = HttpConsoleRenderer.createHtmlPageTemplate(
    								request.getContextPath(),
    								request.getServletPath(),
                                    LogConsoleServlet.CONSOLE_TITLE);
    	}
        
    	// Create a console context if we don't have one
    	ConsoleContext consoleCtx = (ConsoleContext)session.getAttribute(WebKeys.CONSOLE_CONTEXT);
    	if (consoleCtx == null) {
    		session.setAttribute(WebKeys.CONSOLE_CONTEXT, new ConsoleContext());
    	}
    
    	dispatch(request, response);
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


    /**
     * Handles the log console. Actually does the dispatching to the specific
     * page handlers and renderers. These handlers and renderers are completely
     * unaware of any HTTP container.
     *
     * @param request  A HTTP request
     * @param response A HTTP response
     */
    private void dispatch(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        String  pathInfo = request.getPathInfo();


        if (pathInfo == null) {
            processWelcomePage(request, response);
        }
        else if (pathInfo.equals("/config")) {
            processConfig(request, response);
        }
        else if (pathInfo.equals("/config-update")) {
            processConfigUpdate(request, response);
        }
        else if (pathInfo.equals("/logview")) {
            processLogViewSelection(request, response);
        }
        else if (pathInfo.equals("/logremove")) {
            processLogEntityRemoveSelection(request, response);
        }
        else if (pathInfo.equals("/logdownload")) {
            processLogEntityDownloadSelection(request, response);
        }
        else if (pathInfo.equals("/logmonitor")) {
            processLogMonitor(request, response);
        }
        else if (pathInfo.equals("/logremove/remove")) {
            processLogEntityRemove(request, response);
        }
        else if (pathInfo.startsWith("/logdownload/download")) {
            processLogEntityDownload(request, response);
        }
        else if (pathInfo.equals("/logmonitor/monitor")) {
            processLogMonitorAction(request, response);
        }
        else if (pathInfo.equals("/logview/view")) {
            processLogViewAction(request, response);
        }
        else if (pathInfo.equals("/logmonitor/monitor/config-update")) {
            processLogMonitorConfigUpdateAction(request, response);
        }
        else if (pathInfo.equals("/logview/view/config")) {
            processLogViewConfiguration(request, response);
        }
        else if (pathInfo.equals("/logview/view/config-update")) {
            processLogViewConfigurationUpdate(request, response);
        }
        else {
            processWelcomePage(request, response);
    	}
	}


    /**
     * Process the welcome page.
     */
    private void processWelcomePage(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        pageTemplate.setHeading("Welcome");
        
        pageTemplate.renderHead(response.getWriter());
        HttpConsoleRenderer.renderMainPage(response.getWriter());
        pageTemplate.renderTail(response.getWriter());

        response.setContentType("text/html");
        response.getWriter().flush();
    }
  
    /**
     * Process the configuration. Show the configuration page.
     */
    private void processConfig(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        pageTemplate.setHeading("Log Configuration");
        
        pageTemplate.renderHead(response.getWriter());
        HttpConsoleRenderer.renderConfigPage(response.getWriter(), request.getContextPath(), request.getServletPath());
        pageTemplate.renderTail(response.getWriter());

        response.setContentType("text/html");
        response.getWriter().flush();
    }
    
    /**
     * Process the configuration update action. Returns to the config page.
     */
    private void processConfigUpdate(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        boolean ok = HttpConsoleRenderer.handleConfigUpdateAction(
                        request.getParameter("applog"),
                        request.getParameter("logname"),
                        request.getParameter("loglevel"),
                        request.getParameter("perf"),
                        request.getParameter("stat"));

        if (ok) {
            log("Updating log configuration. " +
                " AppName="      + request.getParameter("applog")  +
                " ,LogName="     + request.getParameter("logname")  +
                " ,LogLevel="    + request.getParameter("loglevel") +
                " ,Performance=" + (request.getParameter("perf") == null ? "off" : "on") +
                " ,Statistics="  + (request.getParameter("stat") == null ? "off" : "on"));
        }
        else {
            log("Updating log configuration failed. The logger " +
                " AppName="  + request.getParameter("applog") +
                " ,LogName=" + request.getParameter("logname") +
                " is not active anymore");
        }

        response.sendRedirect(request.getContextPath() + request.getServletPath() + "/config");
    }
 
    /**
     * Process the log view selection.
     */
    private void processLogViewSelection(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        HttpSession     session    = request.getSession(false);
        ConsoleContext  consoleCtx = (ConsoleContext)session.getAttribute(WebKeys.CONSOLE_CONTEXT);

        pageTemplate.setHeading("Log Entity View Selection");

        pageTemplate.renderHead(response.getWriter());
        HttpConsoleRenderer.renderLogViewPage(response.getWriter(), consoleCtx, request.getContextPath(), request.getServletPath());
        pageTemplate.renderTail(response.getWriter());

        response.setContentType("text/html");
        response.getWriter().flush();
    }

    /**
     * Process the log entity remove selection.
     */
    private void processLogEntityRemoveSelection(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        HttpSession     session    = request.getSession(false);
        ConsoleContext  consoleCtx = (ConsoleContext)session.getAttribute(WebKeys.CONSOLE_CONTEXT);

        pageTemplate.setHeading("Log Entity Removal Selection");

        pageTemplate.renderHead(response.getWriter());
        HttpConsoleRenderer.renderLogRemovePage(response.getWriter(), consoleCtx, request.getContextPath(), request.getServletPath());
        pageTemplate.renderTail(response.getWriter());

        response.setContentType("text/html");
        response.getWriter().flush();
    }

    /**
     * Process the log entity download selection.
     */
    private void processLogEntityDownloadSelection(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        HttpSession     session    = request.getSession(false);
        ConsoleContext  consoleCtx = (ConsoleContext)session.getAttribute(WebKeys.CONSOLE_CONTEXT);

        pageTemplate.setHeading("Log Entity Download Selection");

        pageTemplate.renderHead(response.getWriter());
        HttpConsoleRenderer.renderLogDownloadPage(response.getWriter(), consoleCtx, request.getContextPath(), request.getServletPath());
        pageTemplate.renderTail(response.getWriter());

        response.setContentType("text/html");
        response.getWriter().flush();
    }

    /**
     * Process the log monitor.
     */
    private void processLogMonitor(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        HttpSession     session    = request.getSession(false);
        ConsoleContext  consoleCtx = (ConsoleContext)session.getAttribute(WebKeys.CONSOLE_CONTEXT);

        pageTemplate.setHeading("Log Entity Monitor");

        pageTemplate.renderHead(response.getWriter());
        HttpConsoleRenderer.renderLogMonitorPage(response.getWriter(), consoleCtx, request.getContextPath(), request.getServletPath());
        pageTemplate.renderTail(response.getWriter());

        response.setIntHeader("Refresh", ConsoleContext.monitorRefreshRate);
        response.setContentType("text/html");
        response.getWriter().flush();
    }

    /**
     * Process the log entity remove.
     */
    private void processLogEntityRemove(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
//      HttpSession     session    = request.getSession(false);
//      ConsoleContext  consoleCtx = (ConsoleContext)session.getAttribute(WebKeys.CONSOLE_CONTEXT);

        boolean ok = HttpConsoleRenderer.handleLogRemoveAction(
                        request.getParameter("appname"),
						request.getParameter("logname"),
						request.getParameter("entityname"),
						request.getParameter("entitymech"));

        if (ok) {
            log("Removing log entity. " +
                " AppName="    + request.getParameter("appname") +
                " ,LogName="   + request.getParameter("logname") +
                " ,Entity="    + request.getParameter("entityname") +
                " ,Mechanism=" + request.getParameter("entitymech"));
        }
        else {
                log("Removing log entity failed. The logger " +
                    " AppName=" + request.getParameter("appname") +
                " ,LogName=" + request.getParameter("logname") +
                " is not active anymore");
        }

        response.sendRedirect(request.getContextPath() + request.getServletPath() + "/logremove");
    }

    /**
     * Process the log entity download.
     */
    private void processLogEntityDownload(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
//      HttpSession     session    = request.getSession(false);
//      ConsoleContext  consoleCtx = (ConsoleContext)session.getAttribute(WebKeys.CONSOLE_CONTEXT);

        // HTTP response
        response.setContentType("application/octet-stream");

        pageTemplate.setHeading("Log Entity Download");

        log("Downloading entity:" + request.getParameter("entityname"));
        try {
            boolean ok = HttpConsoleRenderer.handleLogDownloadAction(
                                request.getParameter("appname"),
                                request.getParameter("logname"),
                                request.getParameter("entityname"),
                                request.getParameter("entitymech"),
                                response.getOutputStream());
            if(!ok) {
                log("Download log entity failed. The logger " +
                    " AppName=" + request.getParameter("appname") +
                    " ,LogName=" + request.getParameter("logname") +
                    " is not active anymore");
            }
        }
        catch(IOException ex) {
            log("Download error for entity " + request.getParameter("entityname") + ": " + ex);
        }
    }

    /**
     * Process the log monitor action.
     */
    private void processLogMonitorAction(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        HttpSession     session    = request.getSession(false);
        ConsoleContext  consoleCtx = (ConsoleContext)session.getAttribute(WebKeys.CONSOLE_CONTEXT);

        HttpConsoleRenderer.handleLogMonitorAction(
            consoleCtx,
            request.getParameter("appname"),
            request.getParameter("logname"),
            request.getParameter("entityname"),
            request.getParameter("entitymech"));

        response.sendRedirect(request.getContextPath() + request.getServletPath() + "/logmonitor");
    }

    /**
     * Process the log view action.
     */
    private void processLogViewAction(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        HttpSession     session    = request.getSession(false);
        ConsoleContext  consoleCtx = (ConsoleContext)session.getAttribute(WebKeys.CONSOLE_CONTEXT);

        PrintWriter  writer  = null;
        StringWriter swriter = null;
        boolean sendGZIP     = acceptsGzipEncoding(request);
        boolean directGZIP   = false; // set to false if your servlet container has 
                                      // problems with print writer streams on top
                                      // of GZIP streams.
        
        if (sendGZIP) {
            if (directGZIP) {
                writer = new PrintWriter(new GZIPOutputStream(response.getOutputStream()));
            }
            else {
                swriter = new StringWriter();
                writer  = new PrintWriter(swriter);
            }
        }
        else {
            writer = response.getWriter();
        }
        
        pageTemplate.setHeading("Log Entity View");
        
        pageTemplate.renderHead(writer);
        HttpConsoleRenderer.handleLogViewAction(
                writer,
                consoleCtx,
                request.getContextPath(),
                request.getServletPath(),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"),
                request.getParameter("appname"),
                request.getParameter("logname"),
                request.getParameter("entityname"),
                request.getParameter("entitymech"),
                request.getParameter("mode"));
        pageTemplate.renderTail(writer);

        if (sendGZIP) {
            response.setHeader("Content-Encoding", "gzip");
            response.setContentType("text/html");
            
            GZIPOutputStream  zos = null;
            try {                    
                if (directGZIP) {
                    writer.flush();
                    writer.close();
                    writer = null;
                }
                else {
                    writer.flush();
                    writer.close();
                    writer = null;
                    
                    byte[]  htmlBuf = swriter.getBuffer().toString().getBytes();
                    ByteArrayOutputStream  bos = new ByteArrayOutputStream(16 * 1024);
                    zos = new GZIPOutputStream(bos);
                 
                    zos.write(htmlBuf);
                    zos.flush();   
                    zos.close();
                    zos = null;
                    
                    byte[] gzipHtmlBuf = bos.toByteArray();

                    response.getOutputStream().write(gzipHtmlBuf);
                    response.getOutputStream().flush();

                    log("Sending 'gzip' encoded page. Compressed " +
                            htmlBuf.length + " bytes to " + gzipHtmlBuf.length);

                    response.setContentLength(gzipHtmlBuf.length);
                }
            }
            finally {
                if (zos != null) zos.close();
                if (writer != null) writer.close();
            }
        }
        else {
            response.setContentType("text/html");
            writer.flush();
        }
    }

    /**
     * Process the log monitor configuration update action.
     */
    private void processLogMonitorConfigUpdateAction(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        HttpSession     session    = request.getSession(false);
        ConsoleContext  consoleCtx = (ConsoleContext)session.getAttribute(WebKeys.CONSOLE_CONTEXT);

        HttpConsoleRenderer.handleLogMonitorUpdateAction(
            consoleCtx, 
            request.getParameter("refresh"));
    
        response.sendRedirect(request.getContextPath() + request.getServletPath() + "/logmonitor");
    }

    /**
     * Process the log view configuration.
     */
    private void processLogViewConfiguration(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        HttpSession     session    = request.getSession(false);
        ConsoleContext  consoleCtx = (ConsoleContext)session.getAttribute(WebKeys.CONSOLE_CONTEXT);

        pageTemplate.setHeading("Log View Configuration");

        pageTemplate.renderHead(response.getWriter());
        HttpConsoleRenderer.renderLogViewConfigPage(
            response.getWriter(),
            consoleCtx,
            request.getContextPath(),
            request.getServletPath(),
            request.getParameter("appname"),
            request.getParameter("logname"),
            request.getParameter("entityname"),
            request.getParameter("entitymech"));
        pageTemplate.renderTail(response.getWriter());

        response.setContentType("text/html");
        response.getWriter().flush();
    }
    
    /**
     * Process the log monitor configuration update.
     */
    private void processLogViewConfigurationUpdate(HttpServletRequest request, HttpServletResponse response)
        throws IOException
    {
        HttpSession     session    = request.getSession(false);
        ConsoleContext  consoleCtx = (ConsoleContext)session.getAttribute(WebKeys.CONSOLE_CONTEXT);

        log(
            "Log Configuration Update: " +
            "  pageSize = " + consoleCtx.viewPageSize + " events," +
            "  startPos = " + (consoleCtx.logReaderPos/1024) + " KB," +
            "  renderer = " + request.getParameter("renderer"));

        HttpConsoleRenderer.handleLogViewConfigAction(
            consoleCtx,
            request.getContextPath(),
            request.getServletPath(),
            request.getParameter("appname"),
            request.getParameter("logname"),
            request.getParameter("entityname"),
            request.getParameter("entitymech"),
            request.getParameter("pagesize"),
            request.getParameter("startpos"),
            request.getParameter("renderer"),
            request.getParameter("loglevel"),
            request.getParameter("perf"),
            request.getParameter("stat"),
            request.getParameter("notf")
        );

        StringBuilder url = new StringBuilder(
            request.getContextPath()
        ).append(
            request.getServletPath()
        ).append(
            "/logview/view?"
        ).append(
            HttpConsoleRenderer.makeQueryString(
                request.getParameter("appname"),
                request.getParameter("logname"),
                request.getParameter("entityname"),
                request.getParameter("entitymech"),
                "next"
            )
        );
        response.sendRedirect(url.toString());
     }




    
	/**
	 * Send a HTML page GZIP encoded
	 *
	 * @param response  A response object
	 * @param htmlPage  A HTML page
	 */
	private boolean acceptsGzipEncoding(HttpServletRequest request)
	{
    	String encodings = request.getHeader("Accept-Encoding");
		return ((encodings != null) && (encodings.toLowerCase().indexOf("gzip") != -1));
    }



    private final static String CONSOLE_TITLE = "Log Administration Console";

    private HtmlPageTemplate  pageTemplate  = null;
}
