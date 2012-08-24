/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: ObjectInspectorServlet 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2012, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */

/**
 * This is a generic servlet which allows to browse and edit MOF-compliant
 * repositories.
 * <p>
 * The openMDX distribution is delivered with a sample configuration.
 * <p>
 * The following init-params are supported
 * <p>
 * <ul>
 *   <li>applicationName: title of application.</li>
 *   <li>initPrincipal: principal during servlet init for loading codes and data. Default is root.</li>
 *   <li>codeSegment: xri of segment containing codes. This is required if code-to-text
 *       translation must be supported.</li>
 *   <li>evaluator: class implementing the interface org.openmdx.base.application.generic.servlet.Evaluation_1_0.</li>
 *   <li>httpEncoder: class implementing the interface org.openmdx.portal.servlet.HttpEncoder_1_0.</li>
 *   <li>realm: xri of realm containing the security principals.</li>
 *   <li>rootObject[i]: xri of segment containing application data to browse. Must by provided by ejb/data.</li>
 *   <li>uiSegment: xri of segment used to store ui configuration. Must be provided by ejb/data.</li>
 *   <li>locale[i]: names of supported locale, e.g. en_US, de_CH. A corresponding Texts_<locale> class must exist.</li>
 *   <li>requestSizeThreshold: The max size in bytes to be stored in memory.</li>
 *   <li>requestSizeMax: The maximum allowed upload size, in bytes.</li>
 *   <li>uiRefreshRate: rate in ms for refreshing ui configuration (0 for no refresh).</li>
 *   <li>favoritesReference: if configured, defines the container path where the favorite objects are stored.</li>
 *   <li>userHome: path to user home object. If specified the user settings are retrieved/stored in feature 'settings' of the user home object.</li>
 *   <li>mimeType[i]: mime type name of user-defined mime type.
 *   <li>mimeTypeClass[i]: name of class which implements mimeType[i]. The class must be a subclass of org.openmdx.portal.servlet.attribute.BinaryValue.
 *   <li>exceptionDomain: name of exception domain which is mapped to user defined exception texts.
 *   <li>filterCriteriaField: field name which is used to store current filter value when invoking operations.
 *   <li>retrieveByPathPattern: list of path patterns. Objects with access path matching the path patterns are retrieved
 *       by their access path instead of their refMofId, i.e. their identity.
 *   <li>ejb/data: ejb-ref of an EJB managing the application data.</li>
 * </ul>
 * <p>
 * The servlet may be ported to struts or another html framework in the future.
 */
package org.openmdx.portal.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.action.AbstractAction;
import org.openmdx.portal.servlet.action.ActionPerformResult;
import org.openmdx.portal.servlet.action.BoundAction;
import org.openmdx.portal.servlet.action.FindObjectAction;
import org.openmdx.portal.servlet.action.FindObjectsAction;
import org.openmdx.portal.servlet.action.ReloadAction;
import org.openmdx.portal.servlet.action.UnboundAction;
import org.openmdx.portal.servlet.control.ControlFactory;
import org.openmdx.portal.servlet.loader.CodesLoader;
import org.openmdx.portal.servlet.loader.DataLoader;
import org.openmdx.portal.servlet.loader.FilterLoader;
import org.openmdx.portal.servlet.loader.LayoutLoader;
import org.openmdx.portal.servlet.loader.ReportsLoader;
import org.openmdx.portal.servlet.loader.TextsLoader;
import org.openmdx.portal.servlet.loader.UiLoader;
import org.openmdx.portal.servlet.loader.WizardsLoader;
import org.openmdx.portal.servlet.reports.ReportDefinitionFactory;
import org.openmdx.portal.servlet.view.EditObjectView;
import org.openmdx.portal.servlet.view.LayoutFactory;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.wizards.WizardDefinitionFactory;
import org.openmdx.uses.org.apache.commons.fileupload.DiskFileUpload;
import org.openmdx.uses.org.apache.commons.fileupload.FileItem;
import org.openmdx.uses.org.apache.commons.fileupload.FileUpload;
import org.openmdx.uses.org.apache.commons.fileupload.FileUploadException;

/**
 * This is a generic servlet which allows to browse and edit MOF-compliant
 * repositories.
 * <p>
 * The openMDX distribution is delivered with a sample configuration.
 * <p>
 * The following init-params are supported
 * <p>
 * <ul>
 *   <li>applicationName: title of application.</li>
 *   <li>initPrincipal: principal during servlet init for loading codes and data. Default is root.</li>
 *   <li>codeSegment: xri of segment containing codes. This is required if code-to-text
 *       translation must be supported.</li>
 *   <li>portalExtension: class implementing the interface org.openmdx.portal.servlet.PortalExtension_1_0.</li>
 *   <li>httpEncoder: class implementing the interface org.openmdx.portal.servlet.HttpEncoder_1_0.</li>
 *   <li>realm: xri of realm containing the security principals.</li>
 *   <li>rootObject[i]: xri of segment containing application data to browse. Must by provided by ejb/data.</li>
 *   <li>uiSegment: xri of segment used to store ui configuration. Must be provided by ejb/ui.</li>
 *   <li>locale[i]: names of supported locale, e.g. en_US, de_CH. A corresponding Texts_<locale> class must exist.</li>
 *   <li>requestSizeThreshold: The max size in bytes to be stored in memory.</li>
 *   <li>requestSizeMax: The maximum allowed upload size, in bytes.</li>
 *   <li>uiRefreshRate: rate in ms for refreshing ui configuration (0 for no refresh).</li>
 *   <li>favoritesReference: if configured, defines the container path where the favorite objects are stored.</li>
 *   <li>userHome: path to user home object. If specified the user settings are retrieved/stored in feature 'settings' of the user home object.</li>
 *   <li>mimeType[i]: mime type name of user-defined mime type.</li>
 *   <li>mimeTypeClass[i]: name of class which implements mimeType[i]. The class must be a subclass of org.openmdx.portal.servlet.attribute.BinaryValue.</li>
 *   <li>exceptionDomain: name of exception domain which is mapped to user defined exception texts.</li>
 *   <li>filterCriteriaField: field name which is used to store current filter value when invoking operations.</li>
 *   <li>filterValuePattern: filter value patterns (0: prefix 1, 1: prefix 2, 2: suffix). Filter values entered by
 *       the user are completed as <prefix 1><prefix 2><user filter value><suffix>. This way user entered filter values
 *       can be automatically completed. The default patterns are prefix1=(?i), prefix2=%, suffix=%.</li>
 *   <li>retrieveByPathPattern: list of path patterns. Objects with access path matching the path patterns are retrieved
 *       by their access path instead of their refMofId, i.e. their identity.</li>
 *   <li>viewsTimeout: timeout for unused views in minutes. Default is 5.</li>
 *   <li>mobileUserAgents: semi-colon or comma separated list of mobile user agents. The GUI will be launched in 
 *       mobile mode if the user-agent header contains (indexOf() >= 0) one of the configured mobileUserAgent strings. 
 * </ul>
 * <p>
 */

public class ObjectInspectorServlet 
  extends HttpServlet {
  
    //-----------------------------------------------------------------------
    private PersistenceManagerFactory getPersistenceManagerFactory(
    ) throws NamingException, ServiceException {
        return JDOHelper.getPersistenceManagerFactory("EntityManagerFactory");
    }
      
    //-------------------------------------------------------------------------
    public void init(
        ServletConfig conf
    ) throws ServletException  {
        super.init(conf);
        ServletContext context = this.getServletContext();

        // initialize model repository
        try {
            this.model = Model_1Factory.getModel();
        }
        catch(Exception e) {
            System.out.println("can not initialize model repository " + e.getMessage());
            System.out.println(new ServiceException(e).getCause());
        }
        // Persistence manager factories
        try {
            this.pmfMetaData = this.getPersistenceManagerFactory();
            this.pmfData = this.getPersistenceManagerFactory();
        }
        catch(Exception e) {
            throw new ServletException("can not get persistence manager factory", e);
        }

        // Info
        String messagePrefix = new Date() + "  ";
        System.out.println();
        System.out.println();
        System.out.println(messagePrefix + "Starting web application \"" + conf.getServletContext().getContextPath() + "\"");
        System.out.println(messagePrefix + "Driven by openMDX/Portal. Revision: $Revision: 1.130 $");
        System.out.println(messagePrefix + "For more information see http://www.openmdx.org");
        System.out.println(messagePrefix + "Loading... (see log for more information)");
        
        // Get locales. Non-configured locales are stored as null.
        // For non-configured locales texts, ui and code entries fall back to locale[0]
        int maxLocale = 1;
        for(int i = 0; i < 1000; i++) {
            if(this.getInitParameter("locale[" + i + "]") != null) {
                maxLocale = i+1;
            }
        }
        this.locales = new String[maxLocale];
        for(int i = 0; i < maxLocale; i++) {
            this.locales[i] = this.getInitParameter("locale[" + i + "]");
        }
        SysLog.info("configured locale " + locales);

        // exception domain
        this.exceptionDomain = null;
        if(this.getInitParameter("exceptionDomain") != null) {
            this.exceptionDomain = this.getInitParameter("exceptionDomain");
        }

        // filterCriteriaField 
        this.filterCriteriaField = null;
        if(this.getInitParameter("filterCriteriaField") != null) {
            this.filterCriteriaField = this.getInitParameter("filterCriteriaField");
        }

        // filterValuePatterns
        if(this.getInitParameter("filterValuePattern[0]") != null) {
            this.filterValuePattern[0] = this.getInitParameter("filterValuePattern[0]");
        }
        if(this.getInitParameter("filterValuePattern[1]") != null) {
            this.filterValuePattern[1] = this.getInitParameter("filterValuePattern[1]");
        }
        if(this.getInitParameter("filterValuePattern[2]") != null) {
            this.filterValuePattern[2] = this.getInitParameter("filterValuePattern[2]");
        }      
        // Realm
        if(this.getInitParameter("realm") != null) {
            try {
                this.realmIdentity = new Path(this.getInitParameter("realm"));
            } 
            catch(Exception e) {}
            SysLog.info("realm", this.realmIdentity);
        }

        // retrieve by path patterns
        this.retrieveByPathPatterns = new ArrayList<Path>();
        for(int i = 0; i < 100; i++) {
            if(this.getInitParameter("retrieveByPathPattern[" + i + "]") != null) {
                this.retrieveByPathPatterns.add(
                    new Path(this.getInitParameter("retrieveByPathPattern[" + i + "]"))
                );
            }
            else {
                break;
            }
        }      
        // Ui config
        this.uiProviderPath = new Path(this.getInitParameter("uiSegment")).getPrefix(3);
        // Get portal extension
        this.portalExtension = new DefaultPortalExtension();
        try {
            if(this.getInitParameter("evaluator") != null) {
                this.portalExtension = Classes.<PortalExtension_1_0>getApplicationClass(this.getInitParameter("evaluator")).newInstance();
            }
            else if(this.getInitParameter("portalExtension") != null) {
                this.portalExtension = Classes.<PortalExtension_1_0>getApplicationClass(this.getInitParameter("portalExtension")).newInstance();
            }
        }
        catch(Exception e) {
        	this.log("loading PortalExtension failed", e);
        }
        // Reload UI
        this.reloadUi();
        // Layouts
        try {
            this.layoutLoader = new LayoutLoader(
                this.getServletContext(),
                this.portalExtension
            );
            this.layoutFactory = this.layoutLoader.loadLayouts(
                this.locales,
                this.model
            );
        }
        catch(ServiceException e) {
            this.log("loading layouts failed");
        }      
        // Get reports
        try {
            this.reportsLoader = new ReportsLoader(
                this.getServletContext(),
                this.portalExtension
            );
            this.reportFactory = this.reportsLoader.loadReportDefinitions(
                context,
                this.locales,
                model
            );
        }
        catch(ServiceException e) {
            this.log("loading reports failed", e);
        }      
        // Get wizards
        try {
            this.wizardsLoader = new WizardsLoader(
                this.getServletContext(),
                this.portalExtension
            );
            this.wizardFactory = this.wizardsLoader.loadWizardDefinitions(
                context,
                this.locales,
                this.model
            );
        }
        catch(ServiceException e) {
            this.log("loading wizards failed", e);
        }
        // Codes
        this.codes = null;
        try {
            this.codeSegmentIdentity = new Path(this.getInitParameter("codeSegment"));
            PersistenceManager pm = this.createPersistenceManagerData(
                Arrays.asList(
                	this.portalExtension.getAdminPrincipal(new Path(this.codeSegmentIdentity).get(4))
                )
            );
            RefObject_1_0 codeSegment = this.codeSegmentIdentity == null ? null : 
            	(RefObject_1_0)pm.getObjectById(this.codeSegmentIdentity);
            if(codeSegment != null) {
                this.codes = new Codes(codeSegment);
            }
        } catch(Exception e) {
        	SysLog.warning("Unable to initialize codes", e.getMessage());
        }
        // Get texts
        try {
        	this.texts = new Texts(
        		this.locales,
        		TextsLoader.getDefaultTextsBundles(this.locales, this.getServletContext()),
        		this.codes
        	);
        } catch(ServiceException e) {
            this.log("Loading texts failed", e);
        }
        // Control factory
        this.controlFactory = new ControlFactory(
            this.uiContext,
            this.texts,
            this.wizardFactory,
            this.reportFactory
        );      
        // User home
        this.userHomeIdentity = null;
        if(this.getInitParameter(WebKeys.CONFIG_USER_HOME) != null) {
            this.userHomeIdentity = this.getInitParameter(WebKeys.CONFIG_USER_HOME);
        }      
        // Root objects
        try {
            this.loadRootObjects();
        }
        catch(ServiceException e) {
        	this.log("loading roots failed", e);
        }      
        // Load bootstrap data
        try {
            new DataLoader(
                this.getServletContext(), 
                this.portalExtension,
                this.pmfMetaData              
            ).loadData(
                "bootstrap"
            );
        }
        catch(ServiceException e) {
        	this.log("bootstrap data import failed", e);
        }
        // Filter config
        try {
            this.filterLoader = new FilterLoader(
                this.getServletContext(),
                this.portalExtension,
                this.model
            );
            this.filterLoader.loadFilters(
                this.uiContext,
                this.filters
            );
        }
        catch(ServiceException e) {
        	this.log("loading filters failed");
        }
        // Application name
        this.applicationName = this.getInitParameter("applicationName");
        // Request size
        if(this.getInitParameter("requestSizeThreshold") != null) {
            try {
                this.requestSizeThreshold = new Integer(this.getInitParameter("requestSizeThreshold")).intValue();
            } 
            catch(Exception e) {}
        }
        if(this.getInitParameter("requestSizeMax") != null) {
            try {
                this.requestSizeMax = new Integer(this.getInitParameter("requestSizeMax")).intValue();
            } 
            catch(Exception e) {}
        }
        SysLog.info("requestSizeThreshold", new Integer(this.requestSizeThreshold));
        SysLog.info("requestSizeMax", new Integer(this.requestSizeMax));
        // Ui refresh rate
        if(this.getInitParameter("uiRefreshRate") != null) {
            try {
                this.uiRefreshRate = new Integer(this.getInitParameter("uiRefreshRate")).intValue();
            } 
            catch(Exception e) {}
        }
        // Views timeout
        if(this.getInitParameter("viewsTimeout") != null) {
            try {
                this.viewsTimeoutMinutes = new Integer(this.getInitParameter("viewsTimeout")).intValue();
            } 
            catch(Exception e) {}
        }        
        SysLog.info("uiRefreshRate", new Integer(this.uiRefreshRate));
        // httpEncoder
        this.htmlEncoder = new DefaultHtmlEncoder();
        try {
            if(this.getInitParameter(WebKeys.CONFIG_HTML_ENCODER) != null) {
                this.htmlEncoder = Classes.<HtmlEncoder_1_0>getApplicationClass(this.getInitParameter(WebKeys.CONFIG_HTML_ENCODER)).newInstance();
            }
        }
        catch(Exception e) {
        	this.log("loading " + WebKeys.CONFIG_HTML_ENCODER + " failed", e);
        }
        // favoritesReference
        if(this.getInitParameter("favoritesReference") != null) {
            try {
                this.favoritesReference = this.getInitParameter("favoritesReference");
            } 
            catch(Exception e) {}
        }
        SysLog.info("favoritesReference", this.favoritesReference);
        // Mime type mapping
        int i = 0;
        this.mimeTypeImpls = new HashMap<String,String>();
        while(this.getInitParameter("mimeType[" + i + "]") != null) {
            this.mimeTypeImpls.put(
                this.getInitParameter("mimeType[" + i + "]"),
                this.getInitParameter("mimeTypeClass[" + i + "]")              
            );
            i++;
        }
        // Mobile user agents
        this.mobileUserAgents = new ArrayList<String>();
        if(this.getInitParameter("mobileUserAgents") != null) {
        	StringTokenizer tokens = new StringTokenizer(this.getInitParameter("mobileUserAgents"), ";,", false);
        	while(tokens.hasMoreTokens()) {
        		this.mobileUserAgents.add(
        			tokens.nextToken()
        		);
        	}
        }
    }
  
    //-------------------------------------------------------------------------
    protected PersistenceManager createPersistenceManagerData(
        List<String> principalChain
    ) throws ServiceException {
        return this.pmfData.getPersistenceManager(
            principalChain.toString(), 
            null
        );
    }

    //-------------------------------------------------------------------------
    protected void loadRootObjects(
    ) throws ServiceException {
        List<String> rootObjectIdentities = new ArrayList<String>();
        int i = 0;
        while(this.getInitParameter("rootObject[" + i + "]") != null) {
            rootObjectIdentities.add(
            	this.getInitParameter("rootObject[" + i + "]")
            );
            i++;
        }
        this.rootObjectIdentities = rootObjectIdentities.toArray(new String[rootObjectIdentities.size()]);
    }

    //-------------------------------------------------------------------------
    protected String getParameter(
        Map parameterMap,
        String name
    ) {
        Object[] values = (Object[])parameterMap.get(name);
        return values == null ? null : (values.length > 0 ? (String)values[0] : null);
    }

    //-------------------------------------------------------------------------
    /**
     * Override for custom-specific application context implementation.
     */
    protected ApplicationContext newApplicationContext(
    ) {
    	return new ApplicationContext();
    }
    
    //-------------------------------------------------------------------------
    /**
     * Override for custom-specific application context implementation.
     */
    protected ApplicationContext createApplicationContext(
        HttpSession session,
        HttpServletRequest request,
        String userRole
    ) throws ServiceException {
    	SysLog.detail("Creating new context", "user=" + request.getRemoteUser());
		String userAgent = request.getHeader("user-agent");
        boolean isMobileUserAgent = false;
        if(userAgent != null) {
	        for(String mobileUserAgent: this.mobileUserAgents) {        
	        	if(userAgent.indexOf(mobileUserAgent) >= 0) {
	        		isMobileUserAgent = true;
	        		break;
	        	}
	        }
        }
        ApplicationContext app = this.newApplicationContext();
        app.setApplicationName(this.applicationName);
        app.setLocale((String)session.getAttribute(WebKeys.LOCALE_KEY));
        app.setTimezone( (String)session.getAttribute(WebKeys.TIMEZONE_KEY));
        app.setControlFactory(this.controlFactory);
        app.setSessionId(session.getId());
        app.setViewPortType(isMobileUserAgent ? ViewPort.Type.MOBILE : ViewPort.Type.STANDARD);
        app.setLoginPrincipal(request.getUserPrincipal() == null ? null : request.getUserPrincipal().getName());
        app.setUserRole(userRole);
        app.setLoginRealmIdentity(this.realmIdentity);
        app.setRetrieveByPathPatterns(this.retrieveByPathPatterns);
        app.setUserHomeIdentity(this.userHomeIdentity);
        app.setRootObjectIdentities(this.rootObjectIdentities);
        app.setPortalExtension(this.portalExtension);
        app.setHttpEncoder(this.htmlEncoder);
        app.setFilters(this.filters);
        app.setCodes(this.codes);
        app.setLayoutFactory(this.layoutFactory);
        app.setTempDirectory((File)this.getServletContext().getAttribute("javax.servlet.context.tempdir"));
        app.setTempFilePrefix(request.getSession().getId() + "-");
        app.setQuickAccessorsReference(this.favoritesReference);
        app.setMimeTypeImpls(this.mimeTypeImpls);
        app.setExceptionDomain(this.exceptionDomain);
        app.setFilterCriteriaField(this.filterCriteriaField);
        app.setFilterValuePatterns(this.filterValuePattern);
        app.setPmfData(this.pmfData);
        app.setModel(this.model);
        return app;
    }
  
    //-------------------------------------------------------------------------
    synchronized protected void reloadUi(
    ) {
        try {
            this.uiLoader = new UiLoader(
                this.getServletContext(),
                this.portalExtension,
                this.model,
                this.uiProviderPath
            );
            List<Path> uiSegmentPaths = this.uiLoader.load(
                this.locales
            );
            if(this.uiContext == null) {
                this.uiContext = new UiContext(
                    uiSegmentPaths,
                    this.uiLoader.getRepository()
                );
            }
            else {
                this.uiContext.reset(              
                    uiSegmentPaths,
                    this.uiLoader.getRepository()
                );
            }
            if(this.filterLoader != null) {
                this.filterLoader.loadFilters(
                    this.uiContext,
                    this.filters
                );    
            }    
            if(this.controlFactory != null) {
                this.controlFactory.reset();
            }
            this.uiRefreshedAt = System.currentTimeMillis();
        }
        catch(Exception e) {
        	new ServiceException(e).log();
        }
    }

    //-------------------------------------------------------------------------
    protected PrintWriter getWriter(
        HttpServletRequest request,
        HttpServletResponse response        
    ) throws IOException {
        OutputStream os = response.getOutputStream(); 
        response.setContentType("text/html");
        // Do not cache replies
        response.addDateHeader("Expires", -1);
        return new PrintWriter(os);
    }
    
    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void handleRequest(
        HttpServletRequest req, 
        HttpServletResponse res
    ) throws ServletException, IOException {
  
        HttpServletRequestWrapper request = new HttpServletRequestWrapper(req);
        HttpServletResponseWrapper response = new HttpServletResponseWrapper(res);
      
        // PERFORMANCE
        long t0 = System.currentTimeMillis();
        SysLog.detail("receive request");
    
        // ObjectInspectorServlet supports UTF-8 encoding only
        request.setCharacterEncoding("UTF-8");
        
        // Check Session
        HttpSession session = request.getSession(false);
        if(
            (session == null) ||
            (request.getUserPrincipal() == null)
        ) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            ServletContext sc = this.getServletContext();
            RequestDispatcher rd = sc.getRequestDispatcher(
                WebKeys.ERROR_PAGE
            ); 
            rd.forward(request, response);     
            return;
        }
        // init views cache for new sessions
        if(session.getAttribute(WebKeys.VIEW_CACHE_KEY_SHOW) == null) {
            session.setAttribute(
                WebKeys.VIEW_CACHE_KEY_SHOW,
                new ViewsCache(this.viewsTimeoutMinutes)
            );
        }
        if(session.getAttribute(WebKeys.VIEW_CACHE_KEY_EDIT) == null) {
            session.setAttribute(
                WebKeys.VIEW_CACHE_KEY_EDIT,
                new ViewsCache(this.viewsTimeoutMinutes)
            );
        }        
        // Dump header
        if(SysLog.isTraceOn()) {
        	SysLog.trace("HEADER");
            for(Enumeration i = request.getHeaderNames(); i.hasMoreElements(); ) {
                String name = (String)i.nextElement();
                for(Enumeration j = request.getHeaders(name); j.hasMoreElements(); ) {
                	SysLog.trace("header", name + "=" + j.nextElement());
                }
            }        
            // Dump parameter map
            SysLog.trace("PARAMETER");
            for(Iterator i = request.getParameterMap().keySet().iterator(); i.hasNext(); ) {
                Object key = i.next();
                SysLog.trace("parameter", key + "=" + Arrays.asList((Object[])request.getParameterMap().get(key)));
            }
        }
        // Refresh ui config if required
        if(
            ((this.uiRefreshRate > 0) || (this.uiRefreshedAt == 0)) &&
            (System.currentTimeMillis() > this.uiRefreshedAt + this.uiRefreshRate)
        ) {
            this.reloadUi();
        }        
        // Application
        ApplicationContext app = (ApplicationContext)session.getAttribute(WebKeys.APPLICATION_KEY);
        if(app == null) {
            try {
            	synchronized(session) {
            		app = (ApplicationContext)session.getAttribute(WebKeys.APPLICATION_KEY);
            		if(app == null) {
            			app = this.createApplicationContext(session, request, null);
            			app.init();
		                session.setAttribute(
		                    WebKeys.APPLICATION_KEY,
		                    app
		                );
            		}
            	}
            }
            catch(ServiceException e) {
                // Log exception and send user to logoff page
                if(e.getExceptionCode() == BasicException.Code.AUTHORIZATION_FAILURE) {
                	SysLog.warning(e.getMessage(), e.getCause());
                }
                else {
                	SysLog.error(e.getMessage(), e.getCause());                
                }
                // Can not get application context. Send to logoff page
                String[] locales = (String[])request.getParameterMap().get(WebKeys.REQUEST_PARAMETER_LOCALE);
                ServletContext sc = request.getSession().getServletContext();            
                RequestDispatcher rd = sc.getRequestDispatcher(
                    "/Logoff.jsp" + ((locales != null) && (locales.length > 0) ? "?locale=" + locales[0] : "")
                );
                rd.forward(
                    request, 
                    response
                );
                return;
            }
        }
        Map<String,String[]> parameterMap = request.getParameterMap();
        // Set locale if passed as parameter
        if(parameterMap.get(WebKeys.REQUEST_PARAMETER_LOCALE) != null) {
            String[] locales = (String[])parameterMap.get(WebKeys.REQUEST_PARAMETER_LOCALE);
            if(locales.length > 0) {
                app.setCurrentLocale(locales[0]);
            }
        }
          
        // Do file upload if required. Store received files into
        // temporary folder. handleRequest processes the stored files
        if(FileUpload.isMultipartContent(request)) {
            parameterMap = new HashMap();
            SysLog.detail("multi part content");
            DiskFileUpload upload = new DiskFileUpload();
            upload.setHeaderEncoding("UTF-8");
            try {
                List items = upload.parseRequest(
                    request,
                    this.requestSizeThreshold,
                    this.requestSizeMax,
                    app.getTempDirectory().getPath()
                );
                SysLog.detail("request parsed");
                for(Iterator i = items.iterator(); i.hasNext(); ) {
                    FileItem item = (FileItem)i.next();
                    if(item.isFormField()) {
                        parameterMap.put(
                          item.getFieldName(),
                          new String[]{item.getString("UTF-8")}
                        );
                    }
                    else {
                        // Reset binary
                        if("#NULL".equals(item.getName())) {
                            parameterMap.put(
                                item.getFieldName(),
                                new String[]{item.getName()}
                            );              
                        }
                        // Add to parameter map if file received
                        else if(item.getSize() > 0) {
                            parameterMap.put(
                                item.getFieldName(),
                                new String[]{item.getName()}
                            );
                            String location = app.getTempFileName(item.getFieldName(), "");
                            // Bytes
                            File outFile = new File(location);                          
                            try {
                                item.write(outFile);
                            }
                            catch(Exception e) {
                                new ServiceException(e).log();
                            }                        
                            // MimeType
                            try {
                                PrintWriter pw = new PrintWriter(
                                  new FileOutputStream(location + ".INFO")
                                );
                                pw.println(item.getContentType());
                                int sep = item.getName().lastIndexOf("/");
                                if(sep < 0) {
                                  sep = item.getName().lastIndexOf("\\");
                                }
                                pw.println(item.getName().substring(sep + 1));
                                pw.close();
                            }
                            catch(Exception e) {
                                new ServiceException(e).log();
                            }
                        }
                    }
                }
            }
            catch(FileUploadException e) {
                ServiceException e0 = new ServiceException(e);
                SysLog.detail(e.getMessage(), e0);
                SysLog.warning("Can not upload file", Arrays.asList(e.getMessage(), app.getCurrentUserRole()));
            }
        }    
        // requestId. The form field has priority over referrer
        String requestId = 
          this.getParameter(parameterMap, WebKeys.REQUEST_ID + ".submit") == null ? 
              this.getParameter(parameterMap, WebKeys.REQUEST_ID) == null ? 
                  null :                       
                  this.getParameter(parameterMap, WebKeys.REQUEST_ID) : 
              this.getParameter(parameterMap, WebKeys.REQUEST_ID + ".submit");
        SysLog.detail(WebKeys.REQUEST_ID, requestId);    
        // event. The form field has priority over referer
        short event = Action.EVENT_NONE;
        try {
            event = 
                this.getParameter(parameterMap, WebKeys.REQUEST_EVENT + ".submit") == null ? 
                    this.getParameter(parameterMap, WebKeys.REQUEST_EVENT) == null ? 
                        Action.EVENT_NONE : 
                        Short.parseShort(this.getParameter(parameterMap, WebKeys.REQUEST_EVENT)) : 
                    Short.parseShort(this.getParameter(parameterMap, WebKeys.REQUEST_EVENT + ".submit"));
        }
        catch(Exception e) {}
        SysLog.detail("event", event);    
        // Get name of pressed button (if any)
        String buttonName = "";
        for(Iterator i = parameterMap.keySet().iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            if(name.endsWith(".x")) {
                buttonName = name.substring(0, name.lastIndexOf(".x"));
                break;
            }
        }  
        // Get parameter (either parameter or name of pressed button)
        String parameter = null;
        if(this.getParameter(parameterMap, WebKeys.REQUEST_PARAMETER) != null) {
            parameter = this.getParameter(parameterMap, WebKeys.REQUEST_PARAMETER);
        }
        else if(this.getParameter(parameterMap, WebKeys.REQUEST_PARAMETER_ENC) != null) {
            parameter = 
                URLDecoder.decode(
                    this.getParameter(parameterMap, WebKeys.REQUEST_PARAMETER_ENC), 
                    "UTF-8"
                );
        }
        else if(this.getParameter(parameterMap, WebKeys.REQUEST_PARAMETER_LIST) != null) {
            // parameter must be parsed and decoded by specific event handler
            parameter = this.getParameter(parameterMap, WebKeys.REQUEST_PARAMETER_LIST);
        }
        else {
            parameter = buttonName;
        }
        SysLog.detail(WebKeys.REQUEST_PARAMETER, parameter);        
        // Views
        Long viewsCachedSince = (Long)session.getAttribute(WebKeys.VIEW_CACHE_CACHED_SINCE);
        ViewsCache showViewsCache = (ViewsCache)session.getAttribute(WebKeys.VIEW_CACHE_KEY_SHOW);
        ViewsCache editViewsCache = (ViewsCache)session.getAttribute(WebKeys.VIEW_CACHE_KEY_EDIT);
        if(
            (viewsCachedSince == null) ||
            (viewsCachedSince.longValue() < this.uiRefreshedAt)
        ) {
            showViewsCache.clearViews(
                session,
                this.uiRefreshedAt
            );
        }
        // Touch cache for requested view. This updates lastAccessedAt and
        // asserts that removeDirtyViews() does not remove the requested view 
        // even it is old
        showViewsCache.getView(requestId);
        editViewsCache.getView(requestId);
        showViewsCache.removeDirtyViews();        
        SysLog.trace("Getting view", requestId);
        ObjectView view = requestId == null ? 
            null : 
	            showViewsCache.getView(requestId) == null ? 
	                editViewsCache.getView(requestId) : 
	                	showViewsCache.getView(requestId);	     
        // Check for user role change. EVENT_SET_ROLE explicitly
        // sets the new role. A selected object can implicitly 
        // trigger a role change if the segment does not match the
        // current segment. An implicit role change is only possible
        // if no requestId is supplied
        String requestedObjectXri = Action.getParameter(parameter, Action.PARAMETER_OBJECTXRI);
        Path requestedObjectIdentity = requestedObjectXri.length() > 0 ? 
            new Path(requestedObjectXri) : 
            null;
        String newRole = event == Action.EVENT_SET_ROLE ? 
            Action.getParameter(parameter, Action.PARAMETER_NAME) : 
            	(requestId != null) || (requestedObjectIdentity == null) || (requestedObjectIdentity.size() < 5) ? 
            		app.getCurrentUserRole() : 
            			app.getPortalExtension().getNewUserRole(app, requestedObjectIdentity);
        // A new application context must be created in case of a role change. 
        if(
            (event == Action.EVENT_SET_ROLE) ||            
            !newRole.equals(app.getCurrentUserRole())
        ) {
            // A role change always resets all current and cached views
            view = null;
            showViewsCache.clearViews(
                session, 
                this.uiRefreshedAt
            );
            try {
            	app = this.createApplicationContext(
                    session, 
                    request, 
                    newRole
                );
            	app.init();
                session.setAttribute(
                    WebKeys.APPLICATION_KEY,
                    app
                );
                app.createPmControl();
                app.createPmData();
            }
            catch(Exception e) {
                ServiceException e0 = new ServiceException(e);
                SysLog.warning("Unable to switch to requested role", Arrays.asList(requestedObjectIdentity.get(4), e.getMessage()));
                SysLog.warning(e0.getMessage(), e0.getCause());
            }
        }
        if(
            (view == null) &&
            (event !=  FindObjectsAction.EVENT_ID)
        ) {
          SysLog.detail("no view or view with empty object, creating default");
          try {
              if(requestedObjectIdentity != null) {
                  try {
                      view = new ShowObjectView(
                          UUIDs.newUUID().toString(),
                          null,
                          requestedObjectIdentity,
                          app,
                          new LinkedHashMap<Path,Action>(),
                          null, // lookupType
                          null, // resourcePathPrefix
                          null, // navigationTarget
                          null // isReadOnly
                      );
                  }
                  catch(Exception e) {
                      ServiceException e0 = new ServiceException(e);
                      SysLog.warning("can not get object", e.getMessage());
                      SysLog.detail(e0.getMessage(), e0.getCause());               
                  }
              }
              if(view == null) {
                  // Try to retrieve home XRI from quick accessors. Locate quick
                  // accessor with name ending with *
                  QuickAccessor[] quickAccessors = app.getQuickAccessors();
                  Path homeObjectIdentity = null;
                  for(int i = 0; i < quickAccessors.length; i++) {
                      if(quickAccessors[i].getName().endsWith("*")) {
                          homeObjectIdentity = quickAccessors[i].getTargetIdentity();
                          break;
                      }
                  }
                  // If no quick accessor is found fall back to root object 0
                  if(homeObjectIdentity == null) {
                      homeObjectIdentity = app.getRootObject()[0].refGetPath();
                  }              
                  view = new ShowObjectView(
                      UUIDs.newUUID().toString(),
                      null,
                      homeObjectIdentity,
                      app,
                      new LinkedHashMap<Path,Action>(),
                      null, // lookupType
                      null, // resourcePathPrefix
                      null, // navigationTarget
                      null // isReadOnly
                  );
              }
              // FIND_OBJECT events are handled even if the view is newly created
              // Otherwise send back a window.location.href. This reloads the page based on this view
              if(
                  (view != null) && 
                  (event != FindObjectAction.EVENT_ID)
              ) {
                  view.createRequestId();
                  showViewsCache.addView(
                      view.getRequestId(),
                      view
                  );
                  Action action = view.getObjectReference().getSelectObjectAction();
                  ViewPort p = ViewPortFactory.openPage(
                      view,
                      request,
                      this.getWriter(request, response)
                  );
                  p.write("<script language=\"javascript\" type=\"text/javascript\">");
                  String requestURL = request.getRequestURL().toString();
                  int pos = requestURL.indexOf(WebKeys.SERVLET_NAME);
                  if(pos > 0) {
                      p.write("  window.location.href='", requestURL.substring(0, pos), p.getEncodedHRef(action), "';");
                  }
                  else {
                      p.write("  window.location.href='", p.getEncodedHRef(action), "';");                      
                  }
                  p.write("</script>");
                  p.close(true);
                  return;
              }
          }
          catch(Exception e) {
              ServiceException e0 = new ServiceException(e);
              SysLog.warning("can not create ShowObjectView", e.getMessage());
              SysLog.warning(e0.getMessage(), e0.getCause());
              session.invalidate();
              response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
              ServletContext sc = this.getServletContext();
              RequestDispatcher rd = sc.getRequestDispatcher(
                  WebKeys.ERROR_PAGE
              ); 
              rd.forward(request, response);     
              return;
          }
        }    
        // Performance
        long t1 = System.currentTimeMillis();
        SysLog.detail("time (ms) to parse parameters and refresh config", (t1-t0));
        t0 = t1;        
        // EVENT_RELOAD. Refresh application context.
        if(event == ReloadAction.EVENT_ID) {
            try {            
                app.createPmControl();
                app.resetPmData();
                // Reload codes and data in case the root principal issues a reload
                if(this.portalExtension.isRootPrincipal(app.getCurrentUserRole())) {
                    // Codes
                    try {
                        new CodesLoader(
                            this.getServletContext(), 
                            this.portalExtension,
                            this.pmfMetaData              
                        ).loadCodes(
                            this.locales
                        );
                        Codes.refresh(app.getPmControl());
                    } catch(ServiceException e) {
                    	this.log("Loading Codes failed", e);
                    }
                    // Texts
                    try {
                        new TextsLoader(
                        	this.codeSegmentIdentity,
                            this.getServletContext(), 
                            this.portalExtension,
                            this.pmfMetaData
                        ).loadTexts(
                            this.locales
                        );
                    } catch(ServiceException e) {
                    	this.log("Loading Texts failed", e);
                    }
                    // Data
                    try {
                        new DataLoader(
                            this.getServletContext(),
                            this.portalExtension,
                            this.pmfMetaData
                        ).loadData(
                            "data"
                        );
                    } catch(ServiceException e) {
                    	this.log("data import failed", e);
                    }
                    // Ui
                    this.reloadUi();
                    showViewsCache.clearViews(
                        session,
                        this.uiRefreshedAt
                    );
                    editViewsCache.clearViews(
                        session,
                        this.uiRefreshedAt
                    );                        
                }
            }
            catch(ServiceException e) {
                throw new ServletException("Can not refresh application", e);
            } 
        }
        // EVENT_DOWNLOAD
        if(
            (event == Action.EVENT_DOWNLOAD_FROM_LOCATION) ||
            (event == Action.EVENT_DOWNLOAD_FROM_FEATURE)
        ) {
            String name = Action.getParameter(parameter, Action.PARAMETER_NAME);
            String mimeType = Action.getParameter(parameter, Action.PARAMETER_MIME_TYPE);
            SysLog.trace("name", name);
            SysLog.trace("mimeType", mimeType);
            response.setContentType(mimeType);
            response.setHeader("Content-disposition", "attachment;filename=" + name);            
            OutputStream os = response.getOutputStream();
            // EVENT_DOWNLOAD_FROM_LOCATION
            if(event == Action.EVENT_DOWNLOAD_FROM_LOCATION) {
                String location = Action.getParameter(parameter, Action.PARAMETER_LOCATION);
                SysLog.trace("location", location);
                InputStream is = new FileInputStream(
                    app.getTempFileName(location, "")
                );
                if(is != null) {
                    int b = 0;
                    try {
                        int length = 0;
                        while((b = is.read()) >= 0) {
                            os.write(b);
                            length++;
                        }
                        is.close();                  
                        response.setContentLength(length);
                    } 
                    catch(Exception e) {
                        ServiceException e0 = new ServiceException(e);
                        SysLog.warning("can not write stream");
                        SysLog.warning(e0.getMessage(), e0.getCause());
                    }
                }
            }
            // EVENT_DOWNLOAD_FROM_FEATURE
            else {
                try {
                    Path objectIdentity = new Path(Action.getParameter(parameter, Action.PARAMETER_OBJECTXRI));
                    PersistenceManager pm = app.getNewPmData();
                    RefObject_1_0 refObj = (RefObject_1_0)pm.getObjectById(objectIdentity);
                    String feature = Action.getParameter(parameter, Action.PARAMETER_FEATURE);
                    ModelElement_1_0 featureDef = app.getModel().getElement(feature);
                    if(Multiplicity.STREAM.toString().equals(featureDef.objGetValue("multiplicity"))) {
                        long length = refObj.refGetValue(feature, os, 0);
                        response.setContentLength(new Long(length).intValue());       
                    }
                    else {
                        byte[] bytes = (byte[])refObj.refGetValue(feature);
                        if(bytes != null) {
                            for(int i = 0; i < bytes.length; i++) {
                                os.write(bytes[i]);
                            }
                            response.setContentLength(bytes.length);                    
                        }
                    }
                    pm.close();
                }
                catch(Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    SysLog.warning("can not write stream");
                    SysLog.warning(e0.getMessage(), e0.getCause());
                }
            }
            os.close();
            // PERFORMANCE
            t1 = System.currentTimeMillis();
            SysLog.detail("time (ms) to handle event", (t1-t0));
            t0 = t1;       
        }    
        // Dispatch event to action
        else {
        	AbstractAction action = this.portalExtension.getActionFactory().getAction(event);
        	if(action instanceof UnboundAction) {
        		UnboundAction unboundAction = (UnboundAction)action;
        		unboundAction.perform(
        			request, 
        			response, 
        			app, 
        			parameter, 
        			parameterMap
        		);
                t1 = System.currentTimeMillis();
                SysLog.detail("time (ms) to handle find object event", (t1-t0));
                t0 = t1;                   
        	} else if(action instanceof BoundAction) {
        		ActionPerformResult result = null;        		
        		try {
	        		if(view != null) {
	                    view.getApplicationContext().getErrorMessages().clear();        			
		        		BoundAction boundAction = (BoundAction)action;        		
		        		result = boundAction.perform(
		        			view, 
		        			request, 
		        			response, 
		        			parameter, 
		        			session, 
		        			parameterMap, 
		        			editViewsCache, 
		        			showViewsCache
		        		);
		                t1 = System.currentTimeMillis();
		                SysLog.detail("time (ms) to handle find object event", (t1-t0));
		                t0 = t1;                   
	        		}
        		}
                catch(Exception e) {
                	SysLog.warning("handleEvent throws exception", e.getMessage());
                    new ServiceException(e).log();
                }    
                // PERFORMANCE
                t1 = System.currentTimeMillis();
                SysLog.detail("time (ms) to handle event", (t1-t0));
                t0 = t1;      
                if(
                    (result == null) ||
                    (result.getStatusCode() == ActionPerformResult.StatusCode.FORWARD)
                ) {
                    ObjectView nextView = result == null ? 
                        null : 
                        result.getView();
                    // No nextView. go back to default view
                    if(nextView == null) {
                    	SysLog.detail("no nextView. Creating default");
                        try {
                            nextView = new ShowObjectView(
                                UUIDs.newUUID().toString(),
                                null,
                                app.getRootObject()[0].refGetPath(),
                                app,
                                new LinkedHashMap<Path,Action>(),
                                null, // lookupTyp0e
                                null, // resourcePathPrefix
                                null, // navigationTarget
                                null // isReadOnly
                            );
                        }
                        catch(Exception e) {
                        	SysLog.warning("Can not get default view", e.getMessage());
                            new ServiceException(e).log();
                        }
                    }              
                    // Set next view
                    session.setAttribute(
                        WebKeys.CURRENT_VIEW_KEY,
                        nextView
                    );              
                    // Add next view to set of views
                    nextView.createRequestId();
                    if(nextView instanceof ShowObjectView) {
                        showViewsCache.addView(
                            nextView.getRequestId(),
                            nextView
                        );
                    }
                    else if(nextView instanceof EditObjectView){
                        editViewsCache.addView(
                            nextView.getRequestId(),
                            nextView
                        );
                    }            
                    String autostartUrl = app.getPortalExtension().getAutostartUrl(app);
                    // Redirect to autostart URL
                    if(autostartUrl != null) {
                    	URL url = new URL("http://localhost/" + request.getContextPath() + autostartUrl);
                    	boolean hasQuery = url.getQuery() != null;
                    	boolean hasXri = autostartUrl.indexOf(Action.PARAMETER_OBJECTXRI) >= 0;
                    	response.sendRedirect(
                        	request.getContextPath() + 
                        	autostartUrl + 
                        	((hasQuery ? "&" : "?") + Action.PARAMETER_REQUEST_ID + "=" + nextView.getRequestId()) +
                        	(hasXri ? "" : "&" + Action.PARAMETER_OBJECTXRI + "=" + nextView.getRefObject().refMofId())
                    	);
                    }
                    // Forward to page renderer JSP
                    else {
	                    RequestDispatcher rd = this.getServletContext().getRequestDispatcher(
	                        "/jsp/" + nextView.getType() + ".jsp" + 
	                        "?" + Action.PARAMETER_REQUEST_ID + "=" + nextView.getRequestId() +
	                        (result.getViewPortType() == null ? "" : ("&" + Action.PARAMETER_VIEW_PORT + "=" + result.getViewPortType().toString()))
	                    );
	                    SysLog.detail("forward reply");
	                    try {
	                        rd.forward(
	                        	request, 
	                        	response
	                        );
	                    } catch(Exception e) {
	                    	SysLog.warning("Unable forward request", e.getMessage());
	                    }
                    }
                    SysLog.detail("done");
                }
        	}
        }
    }
  
    //-------------------------------------------------------------------------
    public void doGet(
        HttpServletRequest req, 
        HttpServletResponse res
    ) throws ServletException, IOException {
        this.handleRequest(req, res);
    }

    //-------------------------------------------------------------------------
    public void doPost(
        HttpServletRequest req, 
        HttpServletResponse res
    ) throws ServletException, IOException {
        this.handleRequest(req, res);
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3257008756679522610L;

    private String applicationName;
    protected long uiRefreshedAt = 0L;
    private Path uiProviderPath = null;
    private UiContext uiContext = null;
    private PersistenceManagerFactory pmfMetaData = null;
    private PersistenceManagerFactory pmfData = null;
    private Path realmIdentity;
    private String userHomeIdentity;
    private String[] rootObjectIdentities;
    private PortalExtension_1_0 portalExtension = null;
    private HtmlEncoder_1_0 htmlEncoder = null;
    private Model_1_0 model = null;
    private List<Path> retrieveByPathPatterns = null;
    private Map filters = new HashMap();
    private Path codeSegmentIdentity = null;
    private Codes codes = null;
    private Texts texts = null;
    private LayoutFactory layoutFactory = null;
    private ReportDefinitionFactory reportFactory = null;
    private WizardDefinitionFactory wizardFactory = null;
    private ControlFactory controlFactory = null;
    // In-memory threshold for multi-part forms 
    // Content for fields larger than threshold is written to disk
    private int requestSizeThreshold = 200;  
    private int requestSizeMax = 4000000;
    private int uiRefreshRate = 0;
    private int viewsTimeoutMinutes = 5;
    private String favoritesReference = null;
    private String[] locales;
    private Map<String,String> mimeTypeImpls = null;
    private List<String> mobileUserAgents = null;
    private String exceptionDomain = null;
    private String filterCriteriaField = null;
    private String[] filterValuePattern = new String[]{"(?i)",  ".*", ".*"};
    private UiLoader uiLoader;
    private FilterLoader filterLoader;
    private LayoutLoader layoutLoader;
    private ReportsLoader reportsLoader;
    private WizardsLoader wizardsLoader;

}

//--- End of File -----------------------------------------------------------
