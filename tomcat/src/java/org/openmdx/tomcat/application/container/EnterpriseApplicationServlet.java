/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: EnterpriseApplicationServlet.java,v 1.3 2008/02/11 14:44:03 wfro Exp $
 * Description: Enterprise Application Servlet
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/11 14:44:03 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.tomcat.application.container;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.manager.Constants;
import org.apache.catalina.manager.ManagerServlet;
import org.apache.catalina.util.RequestUtil;
import org.apache.tomcat.util.http.fileupload.DiskFileUpload;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.openmdx.kernel.application.container.lightweight.LightweightContainer;
import org.openmdx.tomcat.application.container.ExtendedEngine.SuffixFilter;

/**
 * Enterprise Application Servlet
 */
public class EnterpriseApplicationServlet extends ManagerServlet {

	/**
	 * Constructor
	 */
	public EnterpriseApplicationServlet() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.apache.catalina.manager.ManagerServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		super.init();
    	Container engine = host.getParent();
    	if(engine instanceof ExtendedEngine) {
    		this.engine = (ExtendedEngine) engine;
    	}
	}
	
	/* (non-Javadoc)
	 * @see org.apache.catalina.manager.ManagerServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(
		HttpServletRequest request, 
		HttpServletResponse response
	) throws IOException, ServletException {
        String message;
    	if(this.engine == null) {
        	Container engine = host.getParent();
        	message = "FAIL - " + (engine == null ?
        		"Missing engine" :
        		"Unsupported engine class '" + engine.getClass().getName() + "'"
        	);
    	} else {
            String command = request.getPathInfo();
            if (command == null || "/".equals(command) || "/list".equals(command)) {
            	message = null;
            } else if ("/status".equals(command)) {
            	message = "Lightweight container " + (LightweightContainer.hasInstance() ?
            		 "running as " + LightweightContainer.getInstance() :
            		 "inactive"
            	);
            } else if ("/deploy/war".equals(command)){
                message = deployWebApplication(
            		request.getParameter("descriptor"),
            		request.getParameter("uri")
                );
            } else if ("/undeploy/war".equals(command)){
                message = undeployWebApplication(
            		request.getParameter("uri")
                );
            } else if ("/undeploy/rar".equals(command)){
                message = undeploy(
            		ExtendedEngine.RAR_FILTER, 
            		request.getParameter("uri")
                );
            } else if ("/undeploy/ear".equals(command)){
                message = undeploy(
            		ExtendedEngine.EAR_FILTER, 
            		request.getParameter("uri")
                );
            } else {
                message = sm.getString(
                	"managerServlet.unknownCommand", 
                	command
                );
            }
    	}
    	list(request, response, message);
	}

	@SuppressWarnings("unchecked")
	private static List<FileItem> parseRequest(
		DiskFileUpload upload,
		HttpServletRequest request
	) throws org.apache.tomcat.util.http.fileupload.FileUploadException{
		return upload.parseRequest(request);
	}

    /**
     * Process a POST request for the specified resource.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    public void doPost(
    	HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException, ServletException {
    	if(this.engine != null && "/upload".equals(request.getPathInfo())) {
    		upload(request, response);
    	} else {
            doGet(request,response);
        }
    }
    
    /**
     * Process an upload request for the specified resource.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet-specified error occurs
     */
    private void upload(
    	HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException, ServletException {
        //
        // Prepare our output writer to generate the response message
        //
        response.setContentType("text/html; charset=" + Constants.CHARSET);
        String message = "";
        //
        // Create a new file upload handler
        //
        DiskFileUpload upload = new DiskFileUpload();
        //
        // Get the tempdir
        //
        File tempdir = (File) getServletContext().getAttribute
            ("javax.servlet.context.tempdir");
        // Set upload parameters
        upload.setSizeMax(-1);
        upload.setRepositoryPath(tempdir.getCanonicalPath());
        String archive = null;
        FileItem archiveUpload = null;
        try {
            //
            // Process the uploaded fields
            //
            for(FileItem item : parseRequest(upload,request)) {
                if (!item.isFormField()) {
                    if (item.getFieldName().equals("deployArchive") &&
                        archiveUpload == null) {
                        archiveUpload = item;
                    } else {
                        item.delete();
                    }
                }
            }
            while (true) {
                if (archiveUpload == null) {
                    message = sm.getString
                        ("htmlManagerServlet.deployUploadNoFile");
                    break;
                }
                archive = archiveUpload.getName();
                if (
                	!ExtendedEngine.RAR_FILTER.accept(null, archive) &&
                	!ExtendedEngine.EAR_FILTER.accept(null, archive)
                ){
                    message = "FAIL - '" + archive + "' is neither a RAR nor an EAR";
                    break;
                }
                //
                // Get the filename if uploaded name includes a path
                //
                if (archive.lastIndexOf('\\') >= 0) {
                    archive = archive.substring(archive.lastIndexOf('\\') + 1);
                }
                if (archive.lastIndexOf('/') >= 0) {
                    archive = archive.substring(archive.lastIndexOf('/') + 1);
                }
                //
                // Identify the appBase of the owning Host of this Context (if any)
                //
                File newArchive = new File(this.engine.getStagingDir(), archive);
                if (newArchive.exists() && !newArchive.delete()) {
                    message = "FAIL - Archive file '" + archive + "' could not be replaced in the staging directory";
                    break;
                }
                File oldArchive = new File(this.engine.getDeploymentDirectory(), archive);
                if(oldArchive.isDirectory()) {
                    message = "FAIL - Expanded enterprise application '" + archive + "' must be manually deleted first";
                    break;
                }
                if(oldArchive.exists()) {
                    archiveUpload.write(newArchive);
                    message = "PENDING - Archive '" + archive + "' will be started the next time Tomcat is restarted";
                } else {
                    archiveUpload.write(oldArchive);
                    message = this.engine.deploy(LightweightContainer.getInstance(), oldArchive) ?
                        "OK - Archive '" + archive + "' has been deployed and started" :
                        "FAIL - Archive '" + archive + "' has been deployed but not started";
                }
                break;
            }
        } catch(Exception e) {
            message = sm.getString("htmlManagerServlet.deployUploadFail", e.getMessage());
            log(message, e);
        } finally {
            if (archiveUpload != null) {
                archiveUpload.delete();
            }
            archiveUpload = null;
        }
        list(request, response, message);
    }

    /**
     * Render a HTML list of the currently active enterprise applications
     * in our engine.
     *
     * @param request The request
     * @param response The response
     * @param message a message to display
     */
    private void list(
    	HttpServletRequest request,
    	HttpServletResponse response,
        String message
    ) throws IOException {
        if (debug >= 1) log(
        	"list: Listing enterprise applications for engine '" + this.engine.getName() + "'"
        );
        String contextPath = request.getContextPath();
        PrintWriter writer = response.getWriter();
        //
        // HTML Header Section
        //
        writer.print(Constants.HTML_HEADER_SECTION);
        //
        // Body Header Section
        //
        writer.print(
        	MessageFormat.format(
        		BODY_HEADER_SECTION, 
        		request.getContextPath(),
        		"Tomcat/Lightweight Container Enterprise Application Manager"
        	)
        );
        //
        // Message Section
        //
        writer.print(
        	MessageFormat.format(
        		Constants.MESSAGE_SECTION, 
        		sm.getString("htmlManagerServlet.messageLabel"),
        		message == null || message.length() == 0 ? "OK" : RequestUtil.filter(message)  
        	)
        );
        //
        // Manager Section
        //
        writer.print(
        	MessageFormat.format(
        		Constants.MANAGER_SECTION, 
        		sm.getString("htmlManagerServlet.manager"),
        		response.encodeURL(contextPath + "/enterprise-applications/list"),
        		"Lightweight Container Deployment Units",
        		response.encodeURL("/"),
        		"Home Page",
        		response.encodeURL(contextPath + "/html/list"),
        		"Manage Web Applications",
        		response.encodeURL(contextPath + "/enterprise-applications/status"),
        		"Lightweight Container Status"
        	)
        );
        //
        // Deployment Units
        //
        writer.print(
        	MessageFormat.format(
        		LIST_HEADER_SECTION,
        		"Lightweight Container Deployment Units"
        	)
        );
        writer.print(
        	MessageFormat.format(
    			DEPLOYMENT_UNIT_HEADER_SECTION,
        		"Resource Adapter",
        		sm.getString("htmlManagerServlet.appsAvailable"),
        		sm.getString("htmlManagerServlet.appsTasks")
        	)
        );
        boolean highlighted = true;
        for(File file : engine.getResourceAdapterFiles(false)) {
        	highlighted = !highlighted;
        	String uri = ExtendedEngine.RAR_FILTER.getName(
        		file
        	);
            writer.print(
            	MessageFormat.format(
            		DEPLOYMENT_UNIT_SECTION,
            		uri,
            		"&nbsp;", // running
            		response.encodeURL(
            			contextPath + "/enterprise-applications/undeploy/rar" +
            			"?uri=" + uri
            		),
            		"Undeploy upon re-start",
            		highlighted ? "#C3F3C3" : "#FFFFFF"
            	)
            );
        }
        writer.print(
        	MessageFormat.format(
        		DEPLOYMENT_UNIT_HEADER_SECTION,
        		"Enterprise Application",
        		sm.getString("htmlManagerServlet.appsAvailable"),
        		sm.getString("htmlManagerServlet.appsTasks")
        	)
        );
        highlighted = true;
        for(File file : engine.getEnterpriseApplicationFiles(false)) {
        	highlighted = !highlighted;
        	String uri = ExtendedEngine.EAR_FILTER.getName(file);
            writer.print(
            	MessageFormat.format(
            		DEPLOYMENT_UNIT_SECTION,
            		uri,
            		Boolean.valueOf(engine.isRunning(uri)),
            		response.encodeURL(
            			contextPath + "/enterprise-applications/undeploy/ear" +
            			"?uri=" + uri
            		),
            		"Undeploy upon re-start",
            		highlighted ? "#C3F3C3" : "#FFFFFF"
            	)
            );
        }
        //
        // Deploy Section
        // 
        writer.print(
        	MessageFormat.format(
        		DEPLOY_SECTION, 
        		sm.getString("htmlManagerServlet.deployTitle"),
        		sm.getString("htmlManagerServlet.deployServer"),
        		"Enterprise Application",
        		sm.getString("htmlManagerServlet.appsPath"),
        		sm.getString("htmlManagerServlet.appsAvailable"),
        		sm.getString("htmlManagerServlet.appsTasks")
        	)
        );
        //
        // Web Applications Row Section
        //
        highlighted = true;
        for(File enterpriseApplication : this.engine.getStagingDir().listFiles(ExtendedEngine.DIRECTORY_FILTER)) {
        	for(File contextFile : enterpriseApplication.listFiles(ExtendedEngine.XML_FILTER)) {
                highlighted = !highlighted;
            	String webApplication = ExtendedEngine.XML_FILTER.getPath(contextFile);
                writer.print(
	            	MessageFormat.format(
	            		WEB_APPLICATION_SECTION,
	            		enterpriseApplication.getName(),
	            		webApplication,
	            		isRunning(webApplication),
	            		response.encodeURL(
	            			contextPath + "/enterprise-applications/deploy/war" +
	            			"?descriptor=" + contextFile.getAbsolutePath() + 
	            			"&uri=" + webApplication
	            		),
	            		"Deploy",
	            		response.encodeURL(
	            			contextPath + "/enterprise-applications/undeploy/war" +
	            			"?uri=" + webApplication
	            		),
	            		"Undeploy",
	            		highlighted ? "#C3F3C3" : "#FFFFFF"
	            	)
	            );
        	}
        }
        //
        // Upload Section
        //
        writer.print(
        	MessageFormat.format(
        		UPLOAD_SECTION, 
        		"RAR or EAR to deploy",
        		response.encodeURL(
        			request.getContextPath() + "/enterprise-applications/upload"
        		),
        		"Select archive to upload",
        		sm.getString("htmlManagerServlet.deployButton")
        	)
        );
        //
        // HTML Tail Section
        //
        writer.print(HTML_TAIL_SECTION);
        //        
        // Finish up the response
        //
        writer.flush();
        writer.close();
    }

    private boolean isRunning(
    	String uri
    ){
    	Context context = (Context) host.findChild(uri);
    	return context != null && context.getAvailable();
    }
    
    /**
     * Deploy a web application for the specified path using the specified
     * context configuration file.
     *
     * @param config URL of the context configuration file to be deployed
     * @param path Context path of the application to be deployed
     * 
     * @return message String
     */
    private String deployWebApplication(String config, String path) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        super.deploy(printWriter, config, path, null, false);
        return stringWriter.toString();
    }

    /**
     * Undeploy the web application for the specified path.
     *
     * @param config URL of the context configuration file to be deployed
     * @param path Context path of the application to be deployed
     * 
     * @return message String
     */
    private String undeployWebApplication(String path) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        super.undeploy(printWriter, path);
        return stringWriter.toString();
    }
    
    /**
     * Undeploy 
     *  
     * @param kind the kind of deployment unit
     * @param name the deployment unit's name
     * 
     * the enterprise application's name
     *
     * @return message String
     */
    protected String undeploy(
    	SuffixFilter kind, 
    	String name
    ) {
    	File oldArchive = kind.getFile(this.engine.getDeploymentDir(), name);
    	File newArchive = kind.getFile(this.engine.getStagingDir(), name);
    	try {
	    	return oldArchive.isDirectory() ?
	    		"FAIL: Expanded deployment unit '" + name + "' must be udenployed manually" :
	    	newArchive.exists() && !newArchive.delete() ?
	    		"FAIL: Deployment unit '" + name + "' can't be cleared from the staging directory" :
	    	newArchive.createNewFile() ?
	    		"PENDING: Deployment unit '" + name + "' will be undeployed upon restart" :
	    		"FAIL: Undeploy marker file for '" + name + "' could not be created";
    	} catch (IOException exception) {
    		return "FAIL: Undeploy marker file for '" + name + "' could not be created: " + exception.getMessage(); 
    	}
    }
    
    /**
     * 
     */
    protected ExtendedEngine engine;
    
	/**
	 * Implements <code>Serializable</code>
	 */
	private static final long serialVersionUID = -5097769576716258478L;

    public static final String BODY_HEADER_SECTION =
        "<title>{0}</title>\n" +
        "</head>\n" +
        "\n" +
        "<body bgcolor=\"#FFFFFF\">\n" +
        "\n" +
        "<table cellspacing=\"4\" width=\"100%\" border=\"0\">\n" +
        " <tr>\n" +
        "  <td colspan=\"2\">\n" +
        "   <a href=\"http://www.openmdx.org/\">\n" +
        "    <img border=\"0\" alt=\"openMDX\" align=\"left\"\n" +
        "         src=\"{0}/images/mdx.gif\">\n" +
        "   </a>\n" +
        "   <a href=\"http://tomcat.apache.org/\">\n" +
        "    <img border=\"0\" alt=\"The Tomcat Container\"\n" +
        "         align=\"right\" src=\"{0}/images/tomcat.gif\">\n" +
        "   </a>\n" +
        "  </td>\n" +
        " </tr>\n" +
        "</table>\n" +
        "<hr size=\"1\" noshade=\"noshade\">\n" +
        "<table cellspacing=\"4\" width=\"100%\" border=\"0\">\n" +
        " <tr>\n" +
        "  <td class=\"page-title\" bordercolor=\"#000000\" " +
        "align=\"left\" nowrap>\n" +
        "   <font size=\"+2\">{1}</font>\n" +
        "  </td>\n" +
        " </tr>\n" +
        "</table>\n" +
        "<br>\n" +
        "\n";
	
    public static final String HTML_TAIL_SECTION =
        "<hr size=\"1\" noshade=\"noshade\">\n" +
        "<center><font size=\"-1\" color=\"#525D76\">\n" +
        " <em>Copyright &copy; 2008, <a href=\"http://www.omex.ch/\">OMEX AG</a></em>" +
        "</font></center>\n" +
        "\n" +
        "</body>\n" +
        "</html>";

    private static final String LIST_HEADER_SECTION =
        "<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\">\n" +
        "<tr>\n" +
        " <td colspan=\"3\" class=\"title\">{0}</td>\n" +
        "</tr>\n";

    private static final String DEPLOYMENT_UNIT_HEADER_SECTION =
        "<tr>\n" +
        " <td class=\"header-left\"><small>{0}</small></td>\n" +
        " <td class=\"header-center\"><small>{1}</small></td>\n" +
        " <td class=\"header-left\"><small>{2}</small></td>\n" +
        "</tr>\n";

    private static final String WEB_APPLICATION_SECTION =
        "<tr>\n" +
        " <td class=\"row-left\" bgcolor=\"{7}\"><small>{0}</small></td>\n" +
        " <td class=\"row-left\" bgcolor=\"{7}\"><small><a href=\"{1}\">{1}</a></small></td>\n" +
        " <td class=\"row-center\" bgcolor=\"{7}\"><small>{2}</small></td>\n" +
        " <td class=\"row-left\" bgcolor=\"{7}\"><small><a href=\"{3}\">{4}</a>&nbsp;<a href=\"{5}\">{6}</a></small></td>\n" +
        "</tr>\n";

    private static final String DEPLOYMENT_UNIT_SECTION =
        "<tr>\n" +
        " <td class=\"row-left\" bgcolor=\"{4}\"><small>{0}</small></td>\n" +
        " <td class=\"row-center\" bgcolor=\"{4}\"><small>{1}</small></td>\n" +
        " <td class=\"row-left\" bgcolor=\"{4}\"><small><a href={2}>{3}</a></small></td>\n" +
        "</tr>\n";
        
    private static final String DEPLOY_SECTION =
        "</table>\n" +
        "<br>\n" +
        "<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\">\n" +
        "<tr>\n" +
        " <td colspan=\"4\" class=\"title\">{0}</td>\n" +
        "</tr>\n" +
        "<tr>\n" +
        " <td colspan=\"4\" class=\"header-center\"><small>{1}</small></td>\n" +
        "</tr>\n" +
        "<tr>\n" +
        " <td class=\"header-left\"><small>{2}</small></td>\n" +
        " <td class=\"header-left\"><small>{3}</small></td>\n" +
        " <td class=\"header-center\"><small>{4}</small></td>\n" +
        " <td class=\"header-left\"><small>{5}</small></td>\n" +
        "</tr>\n";

    private static final String UPLOAD_SECTION =
        "<tr>\n" +
        " <td colspan=\"4\" class=\"header-center\"><small>{0}</small></td>\n" +
        "</tr>\n" +
        "<tr>\n" +
        " <td colspan=\"4\">\n" +
        "<form action=\"{1}\" method=\"post\" " +
        "enctype=\"multipart/form-data\">\n" +
        "<table cellspacing=\"0\" cellpadding=\"3\">\n" +
        "<tr>\n" +
        " <td class=\"row-right\">\n" +
        "  <small>{2}</small>\n" +
        " </td>\n" +
        " <td class=\"row-left\">\n" +
        "  <input type=\"file\" name=\"deployArchive\" size=\"40\">\n" +
        " </td>\n" +
        "</tr>\n" +
        "<tr>\n" +
        " <td class=\"row-right\">\n" +
        "  &nbsp;\n" +
        " </td>\n" +
        " <td class=\"row-left\">\n" +
        "  <input type=\"submit\" value=\"{3}\">\n" +
        " </td>\n" +
        "</tr>\n" +
        "</table>\n" +
        "</form>\n" +
        "</table>\n" +
        "<br>\n" +
        "</table>\n";

}
