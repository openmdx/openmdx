/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: HttpConsoleRenderer.java,v 1.2 2008/03/31 11:41:04 wfro Exp $
 * Description: HTTP Console Page Renderer
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/31 11:41:04 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.application.servlet.log.render;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.openmdx.compatibility.kernel.log.Config;
import org.openmdx.compatibility.kernel.log.LogEntity;
import org.openmdx.compatibility.kernel.log.LogEntityReader;
import org.openmdx.compatibility.kernel.log.LogEvent;
import org.openmdx.compatibility.kernel.log.LogEventFilter;
import org.openmdx.compatibility.kernel.log.LogLevel;
import org.openmdx.compatibility.kernel.log.LogUtil;
import org.openmdx.compatibility.kernel.log.RemoteSinkLog;
import org.openmdx.compatibility.kernel.log.SysLog;

/**
 * Provides the HTML page renderers for the HTTP Console
 */
@SuppressWarnings("unchecked")
public class HttpConsoleRenderer {

	public static void renderMainPage(
	    PrintWriter writer
	) {
        writer.println("<p>The Log Administration Console allows you to</p>");
        writer.println("<ul>");
        writer.println("<li>Configure the logging framework.</li>");
        writer.println("<li>View and download log files.</li>");
        writer.println("<li>Monitor the log output.</li>");
        writer.println("</ul>");
        writer.println("<p>&nbsp;</p>");
        writer.println("<p>&nbsp;</p>");
        writer.println("<p>&nbsp;</p>");     
	}

	public static void renderLoginPage(
        PrintWriter writer,
		String      contextPath,
		String      servletPath,
		boolean     loginRejected)
	{
        writer.println("<p>&nbsp;</p>");
        writer.println("<p>&nbsp;</p>");

		if (loginRejected) {
            writer.println("<p>Login rejected. Please try again</p>");
		}else{
            writer.println("<p>Please login</p>");
		}

        writer.println("<p>&nbsp;</p>");
        writer.println("<form method=post action=\"" + contextPath + servletPath + "\">");
        writer.println("  <table align=left border=0 cellspacing=0 cellpadding=4>");
        writer.println("    <tbody>");
        writer.println("      <tr>");
        writer.println("        <td>user id</td>");
        writer.println("        <td><input type=text name=userid size=12><br></td>");
        writer.println("      </tr>");
        writer.println("      <tr>");
        writer.println("        <td>password</td>");
        writer.println("        <td><input type=password name=passwd size=12><br></td>");
        writer.println("      </tr>");
        writer.println("    </tbody>");
        writer.println("  </table>");
        writer.println("  <p><br><br><br><br><br></p>");
        writer.println("  <input type=submit value=\"Login\">");
        writer.println("  <p>&nbsp;</p>");
        writer.println("</form>");
	}

	public static void renderConfigPage(
        PrintWriter  writer,
		String       contextPath,
		String       servletPath)
	{
   		ArrayList     list = new ArrayList(SysLog.getConfigManager().list());


		// open HTML table
        writer.println("<table border=0 cellspacing=1 cellpadding=3>");
        writer.println("  <tbody>");
        writer.println("    <tr>");
        writer.println("      <td class=v12bg align=left   valign=bottom width=120>App ID</td>");
        writer.println("      <td class=v12bg align=left   valign=bottom width=120>Log Name</td>");
        writer.println("      <td class=v12bg align=center valign=bottom width=100>Level</td>");
        writer.println("      <td class=v12bg align=center valign=bottom width=100>Performance</td>");
        writer.println("      <td class=v12bg align=center valign=bottom width=100>Statistics</td>");
        writer.println("      <td class=v12bg align=center valign=bottom></td>");
        writer.println("    </tr>");

        if (list.isEmpty()) {
            writer.println("    <tr>");
            writer.println("      <td class=spalte1 align=left valign=bottom colspan=6>No loggers available</td>");
            writer.println("    <tr>");
        }
        else {
            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                Config c = (Config)iterator.next();
                if (c.isActiv()) {
                    // Access the Config object and remove the reference to it
                    // immediately
                    String  appName  = c.getApplicationId();
                    String  logName  = c.getLogName();
                    int     logLevel = c.getLogLevel();
                    String  perfLog  = c.isPerformanceLog() ? "checked" : "";
                    String  statLog  = c.isStatisticsLog()  ? "checked" : "";



                    writer.println("<tr>");
                    writer.println("  <form method=post action=\"" + contextPath + servletPath + "/config-update\">");
                    writer.println("    <td class=spalte1 align=left valign=center>" + appName  + "</td>");
                    writer.println("    <td class=spalte1 align=left valign=center>" + logName  + "</td>");

                    // build log level option list
                    writer.println("    <td class=spalte1 align=center valign=center>");
                    writer.println("      <select name=loglevel size=1>");
                    for(int ii=LogLevel.LOG_LEVEL_MIN; ii<=LogLevel.LOG_LEVEL_MAX; ii++) {
                        writer.println("        <option" + (ii==logLevel ? " selected" : "")+ ">" + LogUtil.logLevelToStringLong(ii));
                    }
                    writer.println("    </td>");

                    writer.println("    <td class=spalte1 align=center valign=center>");
                    writer.println("      <input type=checkbox " + perfLog + " name=perf value=perf>");
                    writer.println("    </td>");
                    writer.println("    <td class=spalte1 align=center valign=center>");
                    writer.println("      <input type=checkbox " + statLog + " name=stat value=stat>");
                    writer.println("    </td>");
                    writer.println("    <td class=spalte1 align=center valign=center>");
                    writer.println("      <input type=hidden name=applog  value=\"" + appName + "\">");
                    writer.println("      <input type=hidden name=logname value=\"" + logName + "\">");
                    writer.println("      <input type=submit name=Update value=Update>");
                    writer.println("    </td>");
                    writer.println("  </form>");
                    writer.println("</tr>");


                    // free references to config object immediately
                    appName = null;
                    logName = null;
                    c = null;
                }
            }
        }

		// close HTML table
        writer.println("  </tbody>");
        writer.println("</table>");

        writer.println("<p>&nbsp;</p>");
        writer.println("<p>&nbsp;</p>");
        writer.println("<p>&nbsp;</p>");
	}


    public static void renderLogViewPage(
		PrintWriter    writer,
        ConsoleContext consoleCtx,
		String         contextPath,
		String         servletPath)
	{
   		ArrayList     list = new ArrayList(SysLog.getConfigManager().list());
		consoleCtx.reset();


		// open HTML table
        writer.println("<table border=0 cellspacing=1 cellpadding=3>");
        writer.println("  <tbody>");
        writer.println("    <tr>");
        writer.println("      <td class=v12bg align=left  valign=bottom width=120>App ID</td>");
        writer.println("      <td class=v12bg align=left  valign=bottom width=120>Log Name</td>");
        writer.println("      <td class=v12bg align=left  valign=bottom width=200>Log Entity</td>");
        writer.println("      <td class=v12bg align=right valign=bottom width=60>Size</td>");
        writer.println("    </tr>");


        if (list.isEmpty()) {
            writer.println("    <tr>");
            writer.println("      <td class=spalte1 align=left valign=bottom colspan=6>No loggers available</td>");
            writer.println("    <tr>");
        }
        else {
            Iterator iterator = list.iterator();
      		while (iterator.hasNext()) {
      			Config c = (Config)iterator.next();
      			if (c.isActiv()) {
                    // Access the Config object and remove the reference to it
                    // immediately
                    String    appName  = c.getApplicationId();
                    String    logName  = c.getLogName();
                    List      entities = c.getReadableEntities();
    
    				// reverse sort
    				Collections.sort(entities, Collections.reverseOrder());
    
    
                    if (entities != null) {
    	                for(int ii=0; ii<entities.size(); ii++) {
    						LogEntity entity = (LogEntity)entities.get(ii);
    
    				        writer.println("<tr>");
    				        writer.println("  <td class=spalte1 align=left valign=center>" + (ii==0 ? appName : "")  + "</td>");
    				        writer.println("  <td class=spalte1 align=left valign=center>" + (ii==0 ? logName : "")  + "</td>");
    				        writer.println("  <td class=spalte1links align=left valign=center> ");
                			writer.println("    <p>" + HttpConsoleRenderer.makeHRef(
                										contextPath + servletPath + "/logview/view",
    		            							    entity.getName(),
    		            							 	appName,
    		            							 	logName,
    		            							 	entity.getName(),
    		            							 	entity.getMechanism(),
    		            							 	"next") +
                			                 "</p>");
                	        writer.println("  </td>");
    				        writer.println("  <td class=spalte1 align=right  valign=center>" + HttpConsoleRenderer.getFormattedEntitySize(c, entity)  + "</td>");
    				        writer.println("</tr>");
    	                }
      				}
                    else {
    			        writer.println("<tr>");
    			        writer.println("  <td class=spalte1 align=left valign=center>" + appName  + "</td>");
    			        writer.println("  <td class=spalte1 align=left valign=center>" + logName  + "</td>");
    			        writer.println("  <td class=spalte1></td>");
    			        writer.println("  <td class=spalte1></td>");
    			        writer.println("</tr>");
      				}
    
    
    				// free references to config object immediately
    				entities = null;
    				appName  = null;
    				logName  = null;
    				c = null;
      			}
      		}
        }

		// close HTML table
        writer.println("  </tbody>");
        writer.println("</table>");

        writer.println("<p>&nbsp;</p>");
        writer.println("<p>&nbsp;</p>");
        writer.println("<p>&nbsp;</p>");
	}


	public static void renderLogRemovePage(
        PrintWriter     writer,
		ConsoleContext  consoleCtx,
		String          contextPath,
		String          servletPath)
	{
   		ArrayList     list = new ArrayList(SysLog.getConfigManager().list());
        int		  displayedEntities = 0;


		consoleCtx.reset();

		// open HTML table
        writer.println("<table border=0 cellspacing=1 cellpadding=3>");
        writer.println("  <tbody>");
        writer.println("    <tr>");
        writer.println("      <td class=v12bg align=left   valign=bottom width=120>App ID</td>");
        writer.println("      <td class=v12bg align=left   valign=bottom width=120>Log Name</td>");
        writer.println("      <td class=v12bg align=left   valign=bottom width=200>Log Entity</td>");
        writer.println("      <td class=v12bg align=right  valign=bottom width=60>Size</td>");
        writer.println("      <td class=v12bg align=center valign=bottom></td>");
        writer.println("    </tr>");

        if (list.isEmpty()) {
            writer.println("    <tr>");
            writer.println("      <td class=spalte1 align=left valign=bottom colspan=5>No loggers available</td>");
            writer.println("    <tr>");
        }
        else {
            Iterator iterator = list.iterator();
      		while (iterator.hasNext()) {
    			Config c = (Config)iterator.next();
    			if (c.isActiv()) {
                    // Access the Config object and remove the reference to it
                    // immediately
                    String    appName  = c.getApplicationId();
                    String    logName  = c.getLogName();
                    List      entities = c.getRemoveableEntities();
    
    				// sort
    				Collections.sort(entities);
    
                    if (entities != null) {
    	                for(int ii=0; ii<entities.size(); ii++) {
    	                	displayedEntities++;
    						LogEntity entity = (LogEntity)entities.get(ii);
    
    				        writer.println("<tr>");
    				        writer.println("  <form method=post action=\"" + contextPath + servletPath + "/logremove/remove\">");
    				        writer.println("    <td class=spalte1 align=left valign=center>" + (ii==0 ? appName : "") + "</td>");
    				        writer.println("    <td class=spalte1 align=left valign=center>" + (ii==0 ? logName : "") + "</td>");
    				        writer.println("    <td class=spalte1 align=left valign=center>" + entity.getName() + "</td>");
    				        writer.println("    <td class=spalte1 align=right  valign=center>" + HttpConsoleRenderer.getFormattedEntitySize(c, entity)  + "</td>");
    		        		writer.println("    <td class=spalte1 align=center valign=center>");
    					    writer.println("      <input type=hidden name=appname    value=\"" + appName + "\">");
    					    writer.println("      <input type=hidden name=logname    value=\"" + logName + "\">");
    					    writer.println("      <input type=hidden name=entitymech value=\"" + entity.getMechanism() + "\">");
    					    writer.println("      <input type=hidden name=entityname value=\"" + entity.getName() + "\">");
    			    	    writer.println("      <input type=submit value=Remove>");
    				        writer.println("    </td>");
    				        writer.println("  </form>");
    				        writer.println("</tr>");
    	                }
      				}
                    else {
    			        writer.println("<tr>");
    			        writer.println("  <td class=spalte1 align=left valign=center>" + appName  + "</td>");
    			        writer.println("  <td class=spalte1 align=left valign=center>" + logName  + "</td>");
    			        writer.println("  <td class=spalte1></td>");
    			        writer.println("  <td class=spalte1></td>");
    			        writer.println("  <td class=spalte1></td>");
    			        writer.println("</tr>");
      				}
    
    
    				// free references to config object immediately
    				entities = null;
    				appName  = null;
    				logName  = null;
    				c = null;
      			}
      		}
            if (displayedEntities == 0) {
                writer.println("    <tr>");
                writer.println("      <td class=spalte1 align=left valign=bottom colspan=5>No entities currently available for removal</td>");
                writer.println("    <tr>");
            }
        }
        
		// close HTML table
        writer.println("  </tbody>");
        writer.println("</table>");

        if (list.isEmpty()) {
            writer.println("<p>&nbsp;</p>");
            writer.println("<p>&nbsp;</p>");
            writer.println("<p>&nbsp;</p>");
        }
        else {
            for(int jj=0; jj<3-displayedEntities; jj++) {
                writer.println("<p>&nbsp;</p>");
            }
        }
	}


	public static void renderLogDownloadPage(
        PrintWriter    writer,
		ConsoleContext consoleCtx,
		String         contextPath,
		String         servletPath)
	{
   		ArrayList     list = new ArrayList(SysLog.getConfigManager().list());


		consoleCtx.reset();

		// open HTML table
        writer.println("<table border=0 cellspacing=1 cellpadding=3>");
        writer.println("  <tbody>");
        writer.println("    <tr>");
        writer.println("      <td class=v12bg align=left  valign=bottom width=120>App ID</td>");
        writer.println("      <td class=v12bg align=left  valign=bottom width=120>Log Name</td>");
        writer.println("      <td class=v12bg align=left  valign=bottom width=200>Log Entity</td>");
        writer.println("      <td class=v12bg align=right valign=bottom width=60>Size</td>");
        writer.println("    </tr>");

        if (list.isEmpty()) {
            writer.println("    <tr>");
            writer.println("      <td class=spalte1 align=left valign=bottom colspan=5>No loggers available</td>");
            writer.println("    <tr>");
        }
        else {
            Iterator iterator = list.iterator();
      		while (iterator.hasNext()) {
    			Config c = (Config)iterator.next();
    			if (c.isActiv()) {
                    // Access the Config object and remove the reference to it
                    // immediately
                    String    appName  = c.getApplicationId();
                    String    logName  = c.getLogName();
                    List      entities = c.getReadableEntities();
    
    				// reverse sort
    				Collections.sort(entities, Collections.reverseOrder());
    
                    if (entities != null) {
    	                for(int ii=0; ii<entities.size(); ii++) {
    						LogEntity entity = (LogEntity)entities.get(ii);
    
    				        writer.println("<tr>");
    				        writer.println("  <td class=spalte1 align=left valign=center>" + (ii==0 ? appName : "")  + "</td>");
    				        writer.println("  <td class=spalte1 align=left valign=center>" + (ii==0 ? logName : "")  + "</td>");
    				        writer.println("  <td class=spalte1links align=left valign=center> ");
                			writer.println("    <p>" + HttpConsoleRenderer.makeHRef(
                										contextPath + servletPath + "/logdownload/download/" + entity.getName(),
    		            							    entity.getName(),
    		            							 	appName,
    		            							 	logName,
    		            							 	entity.getName(),
    		            							 	entity.getMechanism(),
    		            							 	null) + "</p>");
                	        writer.println("  </td>");
    				        writer.println("  <td class=spalte1 align=right valign=center>" + HttpConsoleRenderer.getFormattedEntitySize(c, entity)  + "</td>");
    				        writer.println("</tr>");
    	                }
      				}
                    else {
    			        writer.println("<tr>");
    			        writer.println("  <td class=spalte1 align=left valign=center>" + appName  + "</td>");
    			        writer.println("  <td class=spalte1 align=left valign=center>" + logName  + "</td>");
    			        writer.println("  <td class=spalte1></td>");
    			        writer.println("  <td class=spalte1></td>");
    			        writer.println("</tr>");
      				}
    
    
    				// free references to config object immediately
    				entities = null;
    				appName  = null;
    				logName  = null;
    				c = null;
      			}
      		}
        }

		// close HTML table
        writer.println("  </tbody>");
        writer.println("</table>");

        writer.println("<p>&nbsp;</p>");
        writer.println("<p>&nbsp;</p>");
        writer.println("<p>&nbsp;</p>");
	}


	public static void renderLogMonitorPage(
        PrintWriter    writer,
		ConsoleContext consoleCtx,
		String         contextPath,
		String         servletPath)
	{
   		ArrayList          list = new ArrayList(SysLog.getConfigManager().list());
   		long               position;
   		String             monitorColor = HttpConsoleRenderer.COLOR_MONITOR_STATE_GOOD;
        MonitoredLogEntity monitoredEntity;
        LogEvent           event;
        LogEventFilter     eventFilter = new LogEventFilter(
        											LogLevel.LOG_LEVEL_WARNING,
        											false, false, false);


		consoleCtx.reset();

		// open HTML table
        writer.println("<table border=0 cellspacing=1 cellpadding=3>");
        writer.println("  <tbody>");
        writer.println("    <tr>");
        writer.println("      <td class=v12bg align=left  valign=bottom width=120>App ID</td>");
        writer.println("      <td class=v12bg align=left  valign=bottom width=120>Log Name</td>");
        writer.println("      <td class=v12bg align=left  valign=bottom width=60>State</td>");
        writer.println("      <td class=v12bg align=left  valign=bottom width=200>Log Entity</td>");
        writer.println("      <td class=v12bg align=right valign=bottom width=60>Size</td>");
        writer.println("      <td class=v12bg align=center valign=bottom></td>");
        writer.println("    </tr>");

        if (list.isEmpty()) {
            writer.println("    <tr>");
            writer.println("      <td class=spalte1 align=left valign=bottom colspan=6>No loggers available</td>");
            writer.println("    <tr>");
        }
        else {
            Iterator iterator = list.iterator();
      		while (iterator.hasNext()) {
    			Config c = (Config)iterator.next();
    			if (c.isActiv()) {
                    // Access the Config object and remove the reference to it
                    // immediately
                    String    appName  = c.getApplicationId();
                    String    logName  = c.getLogName();
                    List<LogEntity>      entities = c.getActiveEntities();
    
    				// sort
    				Collections.sort(entities);
    
                    if (entities != null) {
    	                for(int jj=0; jj<entities.size(); jj++) {
    						LogEntity entity = (LogEntity)entities.get(jj);
    
    						try {
    							// read next 100 events
    							ArrayList        events = new ArrayList();
    							LogEntityReader  reader = c.getReader(entity);
    
    							if (reader != null) {
    								monitoredEntity = (MonitoredLogEntity)consoleCtx.monitoredLogEntities.get(entity);
    								if (monitoredEntity == null) {
    									monitoredEntity = new MonitoredLogEntity(entity, reader.size());
    									consoleCtx.monitoredLogEntities.put(entity, monitoredEntity);
    								}
    
    
    								position = reader.readLogEvents(monitoredEntity.getPosition(), 200, 0, eventFilter, events);
    								monitoredEntity.setPosition(position); // remember position
    
    								for(int ii=0; ii<events.size(); ii++) {
    									event = (LogEvent)events.get(ii);
    									switch(event.getLoggingLevel()) {
    										case LogLevel.LOG_LEVEL_CRITICAL_ERROR:
    											monitoredEntity.setState(MonitoredLogEntity.ENITY_MONITOR_STATE_CRITICAL);
    											break;
    										case LogLevel.LOG_LEVEL_ERROR:
    											if (monitoredEntity.getState() < MonitoredLogEntity.ENITY_MONITOR_STATE_ERROR) {
    												monitoredEntity.setState(MonitoredLogEntity.ENITY_MONITOR_STATE_ERROR);
    											}
    											break;
    										case LogLevel.LOG_LEVEL_WARNING:
    											if (monitoredEntity.getState() == MonitoredLogEntity.ENITY_MONITOR_STATE_GOOD) {
    												monitoredEntity.setState(MonitoredLogEntity.ENITY_MONITOR_STATE_WARNING);
    											}
    											break;
    									}
    								}
    
    								switch(monitoredEntity.getState()) {
    									case MonitoredLogEntity.ENITY_MONITOR_STATE_GOOD:
    										monitorColor = HttpConsoleRenderer.COLOR_MONITOR_STATE_GOOD;
    										break;
    									case MonitoredLogEntity.ENITY_MONITOR_STATE_WARNING:
    										monitorColor = HttpConsoleRenderer.COLOR_MONITOR_STATE_WARNING;
    										break;
    									case MonitoredLogEntity.ENITY_MONITOR_STATE_ERROR:
    										monitorColor = HttpConsoleRenderer.COLOR_MONITOR_STATE_ERROR;
    										break;
    									case MonitoredLogEntity.ENITY_MONITOR_STATE_CRITICAL:
    										monitorColor = HttpConsoleRenderer.COLOR_MONITOR_STATE_CRITICAL;
    										break;
    									default:
    										break;
    								}
    
    							}
    						}
                            catch(IOException ex) {
                                // ignore
                            }
    
    
    				        writer.println("<tr>");
    					    writer.println("  <form method=post action=\"" + contextPath + servletPath + "/logmonitor/monitor\">");
    				        writer.println("    <td class=spalte1 align=left valign=center>" + appName + "</td>");
    				        writer.println("    <td class=spalte1 align=left valign=center>" + logName	 + "</td>");
    				        writer.println("    <td class=spalte1 align=left valign=center>");
    				        writer.println("    <table border=1 cellspacing=1 cellpadding=2 width=100% bgcolor="+monitorColor+"><body><tr><td></td></tr></body></table>");
    				        writer.println("    </td>");
    				        writer.println("    <td class=spalte1links align=left valign=center> ");
    	        			writer.println("      <p>" + HttpConsoleRenderer.makeHRef(
    	        										contextPath + servletPath + "/logview/view",
    		            							    entity.getName(),
    		            							 	appName,
    		            							 	logName,
    		            							 	entity.getName(),
    		            							 	entity.getMechanism(),
    		            							 	"next") + "</p>");
    	        	        writer.println("    </td>");
    				        writer.println("    <td class=spalte1 align=right valign=center>" + HttpConsoleRenderer.getFormattedEntitySize(c, entity)  + "</td>");
    				        writer.println("    <td class=spalte1 align=center valign=center>");
    					    writer.println("      <input type=hidden name=appname    value=\"" + appName + "\">");
    					    writer.println("      <input type=hidden name=logname    value=\"" + logName + "\">");
    					    writer.println("      <input type=hidden name=entitymech value=\"" + entity.getMechanism() + "\">");
    					    writer.println("      <input type=hidden name=entityname value=\"" + entity.getName() + "\">");
    				        writer.println("      <input type=submit value=Reset></td>");
    					    writer.println("    </td>");
    					    writer.println("  </form>");
    				        writer.println("</tr>");
    	                }
      				}
                    else {
    			        writer.println("<tr>");
    			        writer.println("  <td class=spalte1 align=left valign=center>" + appName  + "</td>");
    			        writer.println("  <td class=spalte1 align=left valign=center>" + logName  + "</td>");
    			        writer.println("  <td class=spalte1></td>");
    			        writer.println("  <td class=spalte1></td>");
    			        writer.println("  <td class=spalte1></td>");
    			        writer.println("  <td class=spalte1></td>");
    			        writer.println("</tr>");
      				}
    
    
    				// free references to config object immediately
    				entities = null;
    				appName  = null;
    				logName  = null;
    				c = null;
      			}
      		}
        }
        
		// close HTML table
        writer.println("  </tbody>");
        writer.println("</table>");

        writer.println("<p>&nbsp;</p>");

        writer.println("<form method=post action=\"" + contextPath + servletPath + "/logmonitor/monitor/config-update\">");
        writer.println("<p>");
        writer.println("   <input type=hidden name=refresh value=\"" + ConsoleContext.monitorRefreshRate + "\">");
        writer.println("   <input type=submit value=CheckNow>");
        writer.println("</p>");
        writer.println("</form>");

        writer.println("<p>&nbsp;</p>");

        writer.println("<table border=0 cellspacing=1 cellpadding=3>");
        writer.println("  <tbody>");
        writer.println("    <tr>");
        writer.println("      <form method=post action=\"" + contextPath + servletPath + "/logmonitor/monitor/config-update\">");
        writer.println("      <td valign=middle>Refresh rate:&nbsp;&nbsp;</td>");
        writer.println("      <td valign=middle><input type=text name=refresh value="+ConsoleContext.monitorRefreshRate+" size=4></td>");
        writer.println("      <td align=left valign=middle width=60>sec</td>");
        writer.println("      <td valign=middle><input type=submit value=Update></td>");
        writer.println("      </form>");
        writer.println("    </tr>");
        writer.println("  </tbody>");
        writer.println("</table>");
	}


	public static void handleLogViewAction(
            PrintWriter      writer,
			ConsoleContext   consoleCtx,
			String           contextPath,
			String           servletPath,
			SimpleDateFormat dateFormatter,
			String		     appName,
			String  	     logName,
			String  	     entityName,
			String  	     entityMech,
			String           mode)
	{
		long       newPosition = 0;
   		ArrayList  list = new ArrayList(SysLog.getConfigManager().list());


		try {
			if (appName    == null) throw new Exception("Missing appname");
			if (logName    == null) throw new Exception("Missing logname");
			if (entityMech == null) throw new Exception("Missing entity mechanism");
			if (entityName == null) throw new Exception("Missing entity name");
			if (mode       == null) throw new Exception("Missing mode");

			if (consoleCtx.logEntity == null) {
				// first time visit
				consoleCtx.logEntity = new LogEntity(entityName, entityMech);
				consoleCtx.logReader = null;
			}
            else {
				// Session verification
				if (! (consoleCtx.logEntity.getName().equals(entityName) &&
				       consoleCtx.logEntity.getMechanism().equals(entityMech))) {
					 /// log("Session mismatch (wrong entity) => correcting");
					 consoleCtx.reset();
					 consoleCtx.logEntity = new LogEntity(entityName, entityMech);
				}
			}

			// If we dont't have a log reader yet -> create one
			if (consoleCtx.logReader == null) {
                Iterator iterator = list.iterator();
		  		while (iterator.hasNext()) {
					Config c = (Config)iterator.next();
					if (c.isActiv()) {
		                // Access the Config object and remove the reference to it
		                // immediately
		                if (c.getApplicationId().equals(appName) && c.getLogName().equals(logName)) {
							consoleCtx.logReader = c.getReader(consoleCtx.logEntity);
			   	            c = null;  // free reference immediately
							break;
		                }
		   	            c = null;  // free reference immediately
		  			}
		  		}

				if (consoleCtx.logReader == null) {
					throw new Exception("Cannot create a log entity reader");
				}
			}


			long entitySize = consoleCtx.logReader.size();

			try {
				if (mode.equals("next")) { // "next"
					// consoleCtx.logReaderPos may be negative (offset from EOF,
					// set by handleLogViewerConfigUpdate
					if (consoleCtx.logReaderPos < 0) {
						consoleCtx.logReaderPos += entitySize;
						if (consoleCtx.logReaderPos < 0) {
							consoleCtx.logReaderPos = 0;
						}
					}
					newPosition = consoleCtx.logReaderPos;
				}
                else if (mode.equals("curr")) {  // "curr"
					Long pos = (Long)consoleCtx.entityPositions.removeLast();
					newPosition = pos.longValue();
				}
                else { // "prev"
					consoleCtx.entityPositions.removeLast();
					Long pos = (Long)consoleCtx.entityPositions.removeLast();
					newPosition = pos.longValue();
				}
			}
            catch(NoSuchElementException ex) { newPosition = 0; }

			try {
			  	Long pos = (Long)consoleCtx.entityPositions.getLast();
			  	if (pos.longValue() != newPosition) {
				  	consoleCtx.entityPositions.add(new Long(newPosition)); // remember position
				}
			}
            catch(NoSuchElementException ex) {
			  	consoleCtx.entityPositions.add(new Long(newPosition)); // remember position
			}

			String heading = "Entity: " + entityName + "     " +
			                 "size=" + (entitySize + 1023) / 1024 + "KB   ";

            // Get the next chunk of log events
            ArrayList events = new ArrayList();
            if (consoleCtx.logReader != null) {
                consoleCtx.logReaderPos = consoleCtx.logReader.readLogEvents(
                                            newPosition,
                                            consoleCtx.viewPageSize,
                                            2000,
                                            consoleCtx.filter,
                                            events);
            }
            
	        // Progress bar
			HtmlProgressBar progressBar = new HtmlProgressBar(
											newPosition,
											consoleCtx.logReaderPos - newPosition,
											entitySize, 150);

			// 'Header'
			writer.println("<p>" + heading + "</p>");

			writer.println(progressBar.render());
		    writer.println("<p>&nbsp;</p>");

			// Navigation Links top [viewconfig | prev | next]
			writer.println("<p>");
			writer.println(HttpConsoleRenderer.makeHRef(contextPath + servletPath + "/logview/view/config","viewconfig",appName,logName,entityName,entityMech,null));
			writer.println("&nbsp;&nbsp;");
			writer.println(HttpConsoleRenderer.makeHRef(contextPath + servletPath + "/logview/view","prev",appName,logName,entityName,entityMech,"prev"));
			writer.println("&nbsp;&nbsp;");
			writer.println(HttpConsoleRenderer.makeHRef(contextPath + servletPath + "/logview/view","next",appName,logName,entityName,entityMech,"next"));
			writer.println("</p>");

		    // Event table
            if (consoleCtx.logReader != null) {
                consoleCtx.eventRenderer.render(writer, consoleCtx, events, dateFormatter);
            }
            else {
                SysLog.warning("Event renderer: Did not get a LogEntityReader");
            }
            writer.println("<br>");

	        // Navigation links bottom
	       	writer.println("<p>");
			writer.println(HttpConsoleRenderer.makeHRef(contextPath + servletPath + "/logview/view","prev",appName,logName,entityName,entityMech,"prev"));
	    	writer.println("&nbsp;&nbsp;");
			writer.println(HttpConsoleRenderer.makeHRef(contextPath + servletPath + "/logview/view","next",appName,logName,entityName,entityMech,"next"));
	       	writer.println("</p>");
		}
        catch(Exception ex) {
			/// log("Cannot show log file page", ex);
	       	writer.println("<p><b>An error occurred while reading from the log entity '" + entityName + "'</b></p>");
		    writer.println("<p>&nbsp;</p>");
		    writer.println("<p>Possible reasons are:</p>");
			writer.println("<ul>");
			writer.println("  <li>The entity was deleted by someone else</li>");
			writer.println("  <li>The entity was rolled over by the logger</li>");
			writer.println("  <li>An IO error occurred while reading from the entity</li>");
			writer.println("  </li>");
			writer.println("</ul>");
		    writer.println("<p>&nbsp;</p>");
			writer.println("<p><a href=\"" + contextPath + servletPath + "/logview\" class=links>proceed</a></p>");
		}
	}


	public static boolean handleLogDownloadAction(
			String		  appName,
			String  	  logName,
			String  	  entityName,
			String  	  entityMech,
			OutputStream  outStream)
		throws IOException
	{


		Config config = HttpConsoleRenderer.lookupConfig(appName, logName);
		if (config != null) {
        	LogEntity entity = new LogEntity(entityName, entityMech);
        	LogEntityReader reader = config.getReader(entity);

        	byte[]  buffer = new byte[1024 * 128];
        	long    pos   = 0;
        	long    bytesRead;

        	while(true) {
				bytesRead = reader.readBinary(pos, buffer);

				if (bytesRead > 0) {
		            // send the file as binary data body
		            outStream.write(buffer, 0, (int)bytesRead);
		            outStream.flush();
				}
                else {
					break;
				}

				pos += bytesRead;
        	}
		}

        return (config != null);
	}


	public static boolean handleLogRemoveAction(
		String	appName,
		String  logName,
		String  entityName,
		String  entityMech)
	{
		Config config = HttpConsoleRenderer.lookupConfig(appName, logName);
		if (config != null) {
           	config.removeEntity(new LogEntity(entityName, entityMech));
		}

        return (config != null);
	}


	public static boolean handleLogMonitorAction(
		ConsoleContext consoleCtx,
		String	       appName,
		String         logName,
		String         entityName,
		String         entityMech)
	{
		MonitoredLogEntity monitoredEntity;
		LogEntity          entity = new LogEntity(entityName, entityMech);

		monitoredEntity = (MonitoredLogEntity)consoleCtx.monitoredLogEntities.get(entity);
		if (monitoredEntity != null) {
			monitoredEntity.setState(MonitoredLogEntity.ENITY_MONITOR_STATE_GOOD);
		}

		return true;
	}

	public static boolean handleLogMonitorUpdateAction(
		ConsoleContext consoleCtx,
		String	       refresh)
	{
		try {
			ConsoleContext.monitorRefreshRate = Integer.parseInt(refresh);
			if (ConsoleContext.monitorRefreshRate < 5) {
				ConsoleContext.monitorRefreshRate = 5;
			}
		}
        catch(NumberFormatException ex) {
            // ignore
        }
        
		return true;
	}

	public static boolean handleConfigUpdateAction(
		String appName,
		String logName,
		String logLevel,
		String perfLog,
		String statLog)
	{
		Config config = HttpConsoleRenderer.lookupConfig(appName, logName);
		if (config != null) {
        	config.setLogLevel(LogUtil.logLevelFromString(logLevel));
        	config.enablePerformanceLog(perfLog != null);
        	config.enableStatisticsLog(statLog != null);
		}

        return (config != null);
	}

	public static void handleLogRemoteAction(
		String	appid,
		String	cfgname,
		String	logname,
		String  timestamp,
		String  loglevel,
		String  logsource,
		String  hostname,
		String  processid,
		String  threadname,
		String  classname,
		String  methodname,
		String  line,
		String  summary,
		String  detail)

	{
		Date   date = null;
		int    lineNr;
		int    level;


		// parse timestamp (format = "yyyyMMdd'T'HHmmss.SSS'Z'")
		if (timestamp != null) {
			date = remoteLogDateFormat.parse(timestamp, new ParsePosition(0));
		}
		if (date == null) date = new Date();  // anyway we need a date

		// parse line number
		try {
			lineNr = Integer.parseInt(line);
		}
        catch(NumberFormatException ex) {
			lineNr = 0;
		}

		// parse log level
		level = LogUtil.logLevelFromString(loglevel);

	    LogEvent event = new LogEvent(
								logname,
								date,
								level,
								cfgname,
								appid,
								logsource,
								hostname,
								processid,
								threadname,
								classname,
								methodname,
								lineNr,
					    		summary,
								detail);

		RemoteSinkLog.log(event);

	}

	public static void renderLogViewConfigPage(
        PrintWriter    writer,
		ConsoleContext consoleCtx,
		String         contextPath,
		String         servletPath,
		String	       appName,
		String         logName,
		String         entityName,
		String         entityMech)
	{
		if (consoleCtx.filter == null) consoleCtx.filter = new LogEventFilter();

        writer.println("<form method=post ACTION=\"" + contextPath + servletPath + "/logview/view/config-update?");
		writer.println(   HttpConsoleRenderer.makeQueryString(appName,logName,entityName,entityMech, null));
        writer.println("  \">");

        writer.println("  <table border=0 cellspacing=0 cellpadding=4>");
        writer.println("    <tbody>");
        writer.println("      <tr>");
        writer.println("         <td height=10 colspan=3></td>");
        writer.println("      </tr>");
        writer.println("      <tr>");
        writer.println("        <td valign=middle width=120>Viewer page size:</td>");
        writer.println("        <td valign=middle><input type=text name=pagesize value="+consoleCtx.viewPageSize+" size=6></td>");
        writer.println("        <td valign=middle>Log events</td>");
        writer.println("      </tr>");
        writer.println("      <tr>");
        writer.println("        <td valign=middle>Start from:</td>");
        writer.println("        <td valign=middle><input type=text name=startpos value=0 size=6></td>");
        writer.println("        <td valign=middle>KB&nbsp;&nbsp;(&lt;0 for offset from EOF)</td>");
        writer.println("      </tr>");
        writer.println("    </tbody>");
        writer.println("  </table>");

        writer.println("  <table border=0 cellspacing=0 cellpadding=4>");
        writer.println("    <tbody>");
        writer.println("      <tr>");
        writer.println("         <td height=15 colspan=2></td>");
        writer.println("      </tr>");
        writer.println("      <tr>");
        writer.println("        <td valign=middle>Renderer:</td>");
        writer.println("        <td valign=middle>");
        writer.println("          <input type=radio " + (consoleCtx.eventRenderer instanceof  EventRendererTable ? "checked" : "") + " name=renderer value=table> Table ");
        writer.println("          <input type=radio " + (consoleCtx.eventRenderer instanceof  EventRendererText  ? "checked" : "") + " name=renderer value=text> Text");
        writer.println("        </td>");
        writer.println("      </tr>");
        writer.println("      <tr>");
        writer.println("         <td height=15 colspan=2></td>");
        writer.println("      </tr>");
        writer.println("      <tr>");
        writer.println("        <td valign=middle width=120>Log Event Filter:</td>");
        writer.println("        <td valign=middle>");
        writer.println("          <select name=loglevel size=1>");
        for(int ii=LogLevel.LOG_LEVEL_MIN; ii<=LogLevel.LOG_LEVEL_MAX; ii++) {
        	writer.println("            <option" + (ii==consoleCtx.filter.getLoggingLevel() ? " selected" : "") + ">" + LogUtil.logLevelToStringLong(ii));
       	}
        writer.println("        </td>");
        writer.println("      </tr>");
        writer.println("      <tr>");
        writer.println("        <td valign=middle></td>");
        writer.println("        <td valign=middle>");
        writer.println("          <input type=checkbox " + (consoleCtx.filter.getPerformance() ? "checked" : "") + " name=perf value=perf> Performance");
        writer.println("        </td>");
        writer.println("      </tr>");
        writer.println("      <tr>");
        writer.println("        <td valign=middle></td>");
        writer.println("        <td valign=middle>");
        writer.println("          <input type=checkbox " + (consoleCtx.filter.getStatistics() ? "checked" : "") + " name=stat value=stat> Statistics");
        writer.println("        </td>");
        writer.println("      </tr>");
        writer.println("      <tr>");
        writer.println("        <td valign=middle></td>");
        writer.println("        <td valign=middle>");
        writer.println("          <input type=checkbox " + (consoleCtx.filter.getNotification() ? "checked" : "") + " name=notf value=notf> Notification");
        writer.println("        </td>");
        writer.println("      </tr>");
        writer.println("      <tr>");
        writer.println("         <td colspan=2 height=100 valign=bottom><input type=submit value=\"Update Configuration\"></td>");
        writer.println("      </tr>");
        writer.println("      <tr>");
        writer.println("         <td colspan=2 height=40 valign=bottom>");
        writer.println(             HttpConsoleRenderer.makeHRef(contextPath + servletPath + "/logview/view", "back to log viewer",appName,logName,entityName,entityMech,"curr"));
        writer.println("         </td>");
        writer.println("      </tr>");
        writer.println("    </tbody>");
        writer.println("  </table>");
        writer.println("</form>");
	}



	public static void handleLogViewConfigAction(
		ConsoleContext consoleCtx,
		String         contextPath,
		String         servletPath,
		String	       appName,
		String         logName,
		String         entityName,
		String         entityMech,
		String         pageSize,
		String         startPos,
		String         renderer,
		String         logLevel,
		String         performance,
		String         statistics,
		String         notifications)
	{
		if (consoleCtx.filter == null) consoleCtx.filter = new LogEventFilter();


		// Read HTML FORM input parameter "pagesize"
		if (pageSize != null) {
			consoleCtx.viewPageSize = (int)HttpConsoleRenderer.parseLong(pageSize, 200);
			if (consoleCtx.viewPageSize < 1) consoleCtx.viewPageSize = 1;
		}

		// Read HTML FORM input parameter "startpos"
		if (startPos != null) {
			consoleCtx.logReaderPos = HttpConsoleRenderer.parseLong(startPos, 0) * 1024;
		}

		// Read HTML FORM input parameter "renderer"
		if (renderer != null) {
			if (renderer.equals("table")) consoleCtx.eventRenderer = new EventRendererTable();
			if (renderer.equals("text" )) consoleCtx.eventRenderer = new EventRendererText();
		}


		//Handler HTML FORM filter input parameters
		if (logLevel != null) {
			consoleCtx.filter.setLoggingLevel(LogUtil.logLevelFromString(logLevel));
		}
        else {
			consoleCtx.filter.setLoggingLevel(LogLevel.LOG_LEVEL_MAX);
		}
		consoleCtx.filter.setPerformance(performance != null);
		consoleCtx.filter.setStatistics(statistics != null);
		consoleCtx.filter.setNotification(notifications != null);
	}

	public static HtmlPageTemplate createHtmlPageTemplate(
				String  contextPath,
				String  servletPath,
				String  htmlTitle)
	{
		HtmlPageTemplate  pageTemplate = new HtmlPageTemplate(contextPath,htmlTitle);

		// Standard page: title image
		pageTemplate.setTitleImage("images/logconsole/logo.gif");

        // Standard page: links 'menu'
        pageTemplate.addMenuLink();
        pageTemplate.addMenuLink(contextPath + servletPath                 , "Welcome");
        pageTemplate.addMenuLink();
        pageTemplate.addMenuLink(contextPath + servletPath + "/config"     , "Configuration");
        pageTemplate.addMenuLink(contextPath + servletPath + "/logview"    , "View");
        pageTemplate.addMenuLink(contextPath + servletPath + "/logremove"  , "Remove");
        pageTemplate.addMenuLink(contextPath + servletPath + "/logdownload", "Download");
        pageTemplate.addMenuLink(contextPath + servletPath + "/logmonitor" , "Monitor");

        return pageTemplate;
	}

	/**
	 * Returns a formatted entity size in the format "146B", "100.8KB", "3.6MB"
	 *
	 * @param config  A logger config
	 * @param entity  An entity
	 * @return A formatted size
	 */
	private static String getFormattedEntitySize(
		Config    config,
		LogEntity entity)
	{

        LogEntityReader reader = config.getReader(entity);
        String  fileSize;
        try {
        	long size = reader.size();
        	if (size < 1024) {
        		fileSize = size + "B";
        	}
            else if (size < 1024 * 1024) {
        		fileSize = size / 1024 + "." + ((size % 1024) * 10) / 1024 + "KB";
        	}
            else {
        		long mb = 1024 * 1024;
        		fileSize = size / mb + "." + ((size % mb) * 10) / mb + "MB";
        	}
        }
        catch(IOException ex) {
        	fileSize = "";
        }

        return fileSize;
	}



	/**
	 * Create a HLink reference
	 *
	 * @param path			The HLink URL path element
	 * @param refName		The HLink ref name
	 * @param appName		The HLink URL query string app name element
	 * @param logName		The HLink URL query string log name element
	 * @param entityName	The HLink URL query string entity name element
	 * @param entityMech	The HLink URL query string entity mech element
	 * @param mode		    The HLink URL query string mode element (may be null)
	 * @return The HLink reference
	 */
	public static String makeHRef(
		String  path,
		String  refName,
		String	appName,
		String  logName,
		String  entityName,
		String  entityMech,
		String  mode)
	{
		return "<a href=\"" + path + "?" +
				 makeQueryString(appName,logName,entityName,entityMech,mode) +
        		"\"class=links>" + refName + "</a>";
	}



	/**
	 * Create a URL query string
	 *
	 * @param appName		The query string app name element
	 * @param logName		The query string log name element
	 * @param entityName	The query string entity name element
	 * @param entityMech	The query string entity mech element
	 * @param mode		    The query string mode element (may be null)
	 * @return The query string
	 */
	public static String makeQueryString(
		String	appName,
		String  logName,
		String  entityName,
		String  entityMech,
		String  mode
    ) {
		try {
            StringBuilder sb = new StringBuilder(
                "appname="
            ).append(
                URLEncoder.encode(appName, ENCODING)
            ).append(
                "&logname="
            ).append(
                URLEncoder.encode(logName, ENCODING)
            ).append(
                "&entityname="
            ).append(
                URLEncoder.encode(entityName, ENCODING)
            ).append("&entitymech=").append(URLEncoder.encode(entityMech, ENCODING));
            if (mode != null) {
                sb.append(
                    "&mode="
                ).append(
                    mode
                );
            }
            return sb.toString();
        } catch (UnsupportedEncodingException exception) {
            throw new RuntimeException(exception);
        }
	}



	/**
	 * Looksup for a log Config object given by app name and log name
	 *
	 * @param cfgName		The config name
	 * @param logName		The log name
	 * @return The associated Config object or null if not found
	 */
	public static Config lookupConfig(
		String cfgName,
		String logName)
	{
   		ArrayList  list = new ArrayList(SysLog.getConfigManager().list());
		Config c = null;

  		Iterator iterator = list.iterator();
  		while (iterator.hasNext()) {
			c = (Config)iterator.next();
			if (c.isActiv()) {
                if (c.getApplicationId().equals(cfgName) &&  c.getLogName().equals(logName)) {
                	return c;
                }
  			}
  		}

  		return c;
	}



	/**
	 * Parses a long value from a String and returns a default value if
	 * the parsing fails, or the passed String is null
	 *
	 * @param value         The value to be parsed
	 * @param defaultValue  A default value
	 * @return The parsed value
	 */
	private static long parseLong(String value, long defaultValue)
	{
		if (value == null) return defaultValue;

		try {
			return Long.parseLong(value);
		}
        catch(NumberFormatException ex) {
			return defaultValue;
		}
	}


	// Color definitions
	private static final String COLOR_GREEN     = "#00C000";
	private static final String COLOR_YELLOW    = "#FFFF00";
	private static final String COLOR_LIGHT_RED = "#FFA0A0";
	private static final String COLOR_RED       = "#FF0000";


	// Monitor colors
	private static final String COLOR_MONITOR_STATE_GOOD     = HttpConsoleRenderer.COLOR_GREEN;
	private static final String COLOR_MONITOR_STATE_WARNING  = HttpConsoleRenderer.COLOR_YELLOW;
	private static final String COLOR_MONITOR_STATE_ERROR    = HttpConsoleRenderer.COLOR_LIGHT_RED;
	private static final String COLOR_MONITOR_STATE_CRITICAL = HttpConsoleRenderer.COLOR_RED;


	/** the timestamp format used by the remote log */
	private static SimpleDateFormat remoteLogDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSS'Z'");

    /**
     * The encoding scheme to be used
     */
    protected final static String ENCODING = "UTF-8";
    
}

