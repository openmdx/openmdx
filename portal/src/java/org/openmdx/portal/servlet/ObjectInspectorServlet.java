/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ObjectInspectorServlet.java,v 1.86 2009/03/08 18:03:20 wfro Exp $
 * Description: ObjectInspectorServlet 
 * Revision:    $Revision: 1.86 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/08 18:03:20 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
 *   <li>roleMapper: class implementing the interface org.openmdx.portal.servlet.RoleMapper_1_0.</li>
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
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import org.openmdx.application.dataprovider.transport.ejb.cci.Jmi1AccessorFactory_2;
import org.openmdx.application.log.AppLog;
import org.openmdx.application.mof.cci.Multiplicities;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_3;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.persistence.cci.ConfigurableProperty;
import org.openmdx.portal.servlet.control.ControlFactory;
import org.openmdx.portal.servlet.eventhandler.EditObjectEventHandler;
import org.openmdx.portal.servlet.eventhandler.EventHandlerHelper;
import org.openmdx.portal.servlet.eventhandler.FindObjectsEventHandler;
import org.openmdx.portal.servlet.eventhandler.GridEventHandler;
import org.openmdx.portal.servlet.eventhandler.HandleEventResult;
import org.openmdx.portal.servlet.eventhandler.LookupObjectEventHandler;
import org.openmdx.portal.servlet.eventhandler.SessionEventHandler;
import org.openmdx.portal.servlet.eventhandler.ShowObjectEventHandler;
import org.openmdx.portal.servlet.loader.CodesLoader;
import org.openmdx.portal.servlet.loader.DataLoader;
import org.openmdx.portal.servlet.loader.FilterLoader;
import org.openmdx.portal.servlet.loader.LayoutLoader;
import org.openmdx.portal.servlet.loader.ReportsLoader;
import org.openmdx.portal.servlet.loader.TextsLoader;
import org.openmdx.portal.servlet.loader.UiLoader;
import org.openmdx.portal.servlet.loader.WizardsLoader;
import org.openmdx.portal.servlet.reports.ReportDefinitionFactory;
import org.openmdx.portal.servlet.texts.TextsFactory;
import org.openmdx.portal.servlet.view.EditObjectView;
import org.openmdx.portal.servlet.view.LayoutFactory;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.wizards.WizardDefinitionFactory;
import org.openmdx.uses.org.apache.commons.collections.MapUtils;
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
 *   <li>evaluator: class implementing the interface org.openmdx.portal.servlet.PortalExtension_1_0.</li>
 *   <li>httpEncoder: class implementing the interface org.openmdx.portal.servlet.HttpEncoder_1_0.</li>
 *   <li>roleMapper: class implementing the interface org.openmdx.portal.servlet.RoleMapper_1_0.</li>
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
 *   <li>viewsCacheSize: views are cached up to the configured cache size per session. Increasing the cache size improves
 *       performance but also the memory usage per session. Default is 5.</li>
 *   <li>ejb/data: ejb-ref of an EJB managing the application data.</li>
 * </ul>
 * <p>
 */

public class ObjectInspectorServlet 
  extends HttpServlet {
  
    //-----------------------------------------------------------------------
    private PersistenceManagerFactory getPersistenceManagerFactory(
    ) throws NamingException, ServiceException {
        Map<String,String> properties = new HashMap<String,String>();
        properties.put(
            ConfigurableProperty.ConnectionFactoryName.qualifiedName(), 
            "java:comp/env/ejb/EntityManagerFactory"
        );
        properties.put(
            ConfigurableProperty.PersistenceManagerFactoryClass.qualifiedName(), 
            Jmi1AccessorFactory_2.class.getName()
        );
        return JDOHelper.getPersistenceManagerFactory(properties);
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

        // Get locales. Non-configured locales are stored as null.
        // For non-configured locales texts, ui and code entries fall back to locale[0]
        int maxLocale = 1;
        for(int i = 0; i < 1000; i++) {
            if(this.getInitParameter("locale[" + i + "]") != null) {
                maxLocale = i+1;
            }
        }
        this.locale = new String[maxLocale];
        for(int i = 0; i < maxLocale; i++) {
            this.locale[i] = this.getInitParameter("locale[" + i + "]");
        }
        AppLog.info("configured locale " + locale);

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
            AppLog.info("realm", this.realmIdentity);
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
        // Role mapper
        this.roleMapper = new DefaultRoleMapper();
        try {
            if(this.getInitParameter(WebKeys.CONFIG_ROLE_MAPPER) != null) {
                this.roleMapper = (RoleMapper_1_0)Classes.getApplicationClass(this.getInitParameter(WebKeys.CONFIG_ROLE_MAPPER)).newInstance();
            }
        }
        catch(Exception e) {
            this.log("loading " + WebKeys.CONFIG_ROLE_MAPPER + " failed", e);
        }      
        // Ui config
        this.uiProviderPath = new Path(this.getInitParameter("uiSegment")).getPrefix(3);
        this.reloadUi();
        // Get texts
        try {
            this.textsLoader = new TextsLoader(
                this.getServletContext(),
                this.roleMapper
            );
            this.textsFactory = this.textsLoader.loadTexts(
                this.locale
            );
        }
        catch(ServiceException e) {
            this.log("loading texts failed", e);
        }      
        // Layouts
        try {
            this.layoutLoader = new LayoutLoader(
                this.getServletContext(),
                this.roleMapper
            );
            this.layoutFactory = this.layoutLoader.loadLayouts(
                this.locale,
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
                this.roleMapper
            );
            this.reportFactory = this.reportsLoader.loadReportDefinitions(
                context,
                this.locale,
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
                this.roleMapper
            );
            this.wizardFactory = this.wizardsLoader.loadWizardDefinitions(
                context,
                this.locale,
                model
            );
        }
        catch(ServiceException e) {
            this.log("loading wizards failed", e);
        }
        // Control factory
        this.controlFactory = new ControlFactory(
            this.uiContext,
            this.textsFactory,
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
                this.roleMapper,
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
                this.roleMapper,
                model
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
        AppLog.info("requestSizeThreshold", new Integer(this.requestSizeThreshold));
        AppLog.info("requestSizeMax", new Integer(this.requestSizeMax));
        // Ui refresh rate
        if(this.getInitParameter("uiRefreshRate") != null) {
            try {
                this.uiRefreshRate = new Integer(this.getInitParameter("uiRefreshRate")).intValue();
            } 
            catch(Exception e) {}
        }
        AppLog.info("uiRefreshRate", new Integer(this.uiRefreshRate));
        // Portal extension
        this.evaluator = new DefaultPortalExtension();
        try {
            if(this.getInitParameter("evaluator") != null) {
                this.evaluator = (PortalExtension_1_0)Classes.getApplicationClass(this.getInitParameter("evaluator")).newInstance();
            }
        }
        catch(Exception e) {
        	this.log("loading evaluator failed", e);
        }
        // httpEncoder
        this.htmlEncoder = new DefaultHtmlEncoder();
        try {
            if(this.getInitParameter(WebKeys.CONFIG_HTML_ENCODER) != null) {
                this.htmlEncoder = (HtmlEncoder_1_0)Classes.getApplicationClass(this.getInitParameter(WebKeys.CONFIG_HTML_ENCODER)).newInstance();
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
        AppLog.info("favoritesReference", this.favoritesReference);
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
        // Codes
        this.codes = null;
        try {
            Path codeSegmentIdentity = new Path(this.getInitParameter("codeSegment"));
            PersistenceManager pm = this.createPersistenceManagerData(
                Arrays.asList(
                    new String[]{
                        this.roleMapper.getAdminPrincipal(new Path(codeSegmentIdentity).get(4))
                    }
                )
            );
            RefObject_1_0 codeSegment = codeSegmentIdentity == null
                ? null
                : (RefObject_1_0)pm.getObjectById(codeSegmentIdentity);
            if(codeSegment != null) {
                this.codes = new Codes(codeSegment);
            }
        }
        catch(Exception e) {
            AppLog.warning("can not initialize codes", e.getMessage());
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
    protected ApplicationContext createApplicationContext(
        HttpSession session,
        HttpServletRequest request,
        String userRole
    ) throws ServiceException {
        AppLog.detail("Creating new context", "user=" + request.getRemoteUser());
        return new ApplicationContext(
            this.applicationName,
            (String)session.getAttribute(WebKeys.LOCALE_KEY),
            (String)session.getAttribute(WebKeys.TIMEZONE_KEY),
            this.controlFactory,
            session.getId(),
            request.getUserPrincipal() == null ? null : request.getUserPrincipal().getName(),
            userRole,
            this.realmIdentity,
            this.retrieveByPathPatterns,
            this.userHomeIdentity,
            this.rootObjectIdentities,
            this.evaluator,
            this.htmlEncoder,
            this.filters,
            this.codes,
            this.layoutFactory,
            (File)this.getServletContext().getAttribute("javax.servlet.context.tempdir"),
            request.getSession().getId() + "-",
            this.favoritesReference,
            this.mimeTypeImpls,
            this.exceptionDomain,
            this.filterCriteriaField,
            this.filterValuePattern,
            this.pmfData,
            this.roleMapper,
            this.model
        );
    }
  
    //-------------------------------------------------------------------------
    synchronized protected void reloadUi(
    ) {
        try {
            this.uiLoader = new UiLoader(
                this.getServletContext(),
                this.roleMapper,
                this.model,
                this.uiProviderPath
            );
            List<Path> uiSegmentPaths = this.uiLoader.load(
                this.locale
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
        catch(ServiceException e) {}
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
        AppLog.detail("receive request");
    
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
                new ViewsCache(
                    this.getInitParameter("viewsCacheSize") == null ? 
                        null : 
                        new Integer(this.getInitParameter("viewsCacheSize")
                    )
                )
            );
        }
        if(session.getAttribute(WebKeys.VIEW_CACHE_KEY_EDIT) == null) {
            session.setAttribute(
                WebKeys.VIEW_CACHE_KEY_EDIT,
                new ViewsCache(
                    this.getInitParameter("viewsCacheSize") == null ? 
                        null : 
                        new Integer(this.getInitParameter("viewsCacheSize")
                    )
                )
            );
        }        
        // Dump header
        if(AppLog.isTraceOn()) {
            AppLog.trace("HEADER");
            for(Enumeration i = request.getHeaderNames(); i.hasMoreElements(); ) {
                String name = (String)i.nextElement();
                for(Enumeration j = request.getHeaders(name); j.hasMoreElements(); ) {
                    AppLog.trace("header", name + "=" + j.nextElement());
                }
            }        
            // Dump parameter map
            AppLog.trace("PARAMETER");
            for(Iterator i = request.getParameterMap().keySet().iterator(); i.hasNext(); ) {
                Object key = i.next();
                AppLog.trace("parameter", key + "=" + Arrays.asList((Object[])request.getParameterMap().get(key)));
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
        ApplicationContext application = (ApplicationContext)session.getAttribute(WebKeys.APPLICATION_KEY);
        if(application == null) {
            try {
                session.setAttribute(
                    WebKeys.APPLICATION_KEY,
                    application = this.createApplicationContext(session, request, null)
                );
            }
            catch(ServiceException e) {
                // Log exception and send user to logoff page
                if(e.getExceptionCode() == BasicException.Code.AUTHORIZATION_FAILURE) {
                    AppLog.warning(e.getMessage(), e.getCause());
                }
                else {
                    AppLog.error(e.getMessage(), e.getCause());                
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
        Map parameterMap = request.getParameterMap();
        // Set locale if passed as parameter
        if(parameterMap.get(WebKeys.REQUEST_PARAMETER_LOCALE) != null) {
            String[] locales = (String[])parameterMap.get(WebKeys.REQUEST_PARAMETER_LOCALE);
            if(locales.length > 0) {
                application.setCurrentLocale(locales[0]);
            }
        }
          
        // Do file upload if required. Store received files into
        // temporary folder. handleRequest processes the stored files
        if(FileUpload.isMultipartContent(request)) {
            parameterMap = new HashMap();
            AppLog.detail("multi part content");
            DiskFileUpload upload = new DiskFileUpload();
            upload.setHeaderEncoding("UTF-8");
            try {
                List items = upload.parseRequest(
                    request,
                    this.requestSizeThreshold,
                    this.requestSizeMax,
                    application.getTempDirectory().getPath()
                );
                AppLog.detail("request parsed");
                for(Iterator i = items.iterator(); i.hasNext(); ) {
                    FileItem item = (FileItem)i.next();
                    if(item.isFormField()) {
                        AppLog.trace("form field=" + item.getFieldName());
                        parameterMap.put(
                          item.getFieldName(),
                          new String[]{item.getString("UTF-8")}
                        );
                    }
                    else {
                        AppLog.trace("file.fieldName", item.getFieldName());
                        AppLog.trace("file.contentType", item.getContentType());
                        AppLog.trace("file.isInMemory", item.isInMemory());
                        AppLog.trace("file.sizeInBytes", item.getSize());
                        AppLog.trace("file.name", item.getName());                        
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
                            String location = application.getTempFileName(item.getFieldName(), "");
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
                SysLog.warning("Can not upload file", Arrays.asList(e.getMessage(), application.getCurrentUserRole()));
            }
        }    
        // requestId. The form field has priority over referrer
        String requestId = 
          this.getParameter(parameterMap, WebKeys.REQUEST_ID + ".submit") == null ? 
              this.getParameter(parameterMap, WebKeys.REQUEST_ID) == null ? 
                  null :                       
                  this.getParameter(parameterMap, WebKeys.REQUEST_ID) : 
              this.getParameter(parameterMap, WebKeys.REQUEST_ID + ".submit");
        AppLog.detail(WebKeys.REQUEST_ID, requestId);    
        // event. The form field has priority over referer
        int event = Action.EVENT_NONE;
        try {
            event = 
                this.getParameter(parameterMap, WebKeys.REQUEST_EVENT + ".submit") == null ? 
                    this.getParameter(parameterMap, WebKeys.REQUEST_EVENT) == null ? 
                        Action.EVENT_NONE : 
                        Integer.parseInt(this.getParameter(parameterMap, WebKeys.REQUEST_EVENT)) : 
                    Integer.parseInt(this.getParameter(parameterMap, WebKeys.REQUEST_EVENT + ".submit"));
        }
        catch(Exception e) {}
        AppLog.detail("event", event);    
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
        AppLog.detail(WebKeys.REQUEST_PARAMETER, parameter);        
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
        showViewsCache.removeDirtyViews();
        // As default action either try to select object defined by target or by parameter
        AppLog.trace("getting view for requestId", requestId);
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
                application.getCurrentUserRole() : 
                application.getCurrentUserRole().substring(0, application.getCurrentUserRole().indexOf("@") + 1) + requestedObjectIdentity.get(4);
        // A new application context must be created in case of a role change. 
        if(
            (event == Action.EVENT_SET_ROLE) ||            
            !newRole.equals(application.getCurrentUserRole())
        ) {
            // A role change always resets all current and cached views
            view = null;
            showViewsCache.clearViews(
                session, 
                this.uiRefreshedAt
            );
            try {
                session.setAttribute(
                    WebKeys.APPLICATION_KEY,
                    application = this.createApplicationContext(
                        session, 
                        request, 
                        newRole
                    )
                );
                application.createPmControl();
                application.createPmData();
            }
            catch(Exception e) {
                ServiceException e0 = new ServiceException(e);
                AppLog.warning("Unable to switch to requested role", Arrays.asList(requestedObjectIdentity.get(4), e.getMessage()));
                AppLog.warning(e0.getMessage(), e0.getCause());
            }
        }
        if(
            (view == null) &&
            (event !=  Action.EVENT_FIND_OBJECTS)
        ) {
          AppLog.detail("no view or view with empty object, creating default");
          try {
              if(requestedObjectIdentity != null) {
                  try {
                      view = new ShowObjectView(
                          UUIDs.getGenerator().next().toString(),
                          null,
                          requestedObjectIdentity,
                          application,
                          MapUtils.orderedMap(new HashMap()),
                          null,
                          null
                      );
                  } 
                  catch(Exception e) {
                      ServiceException e0 = new ServiceException(e);
                      AppLog.warning("can not get object", e.getMessage());
                      AppLog.detail(e0.getMessage(), e0.getCause());               
                  }
              }
              if(view == null) {
                  // Try to retrieve home XRI from quick accessors. Locate quick
                  // accessor with name ending with *
                  QuickAccessor[] quickAccessors = application.getQuickAccessors();
                  Path homeObjectIdentity = null;
                  for(int i = 0; i < quickAccessors.length; i++) {
                      if(quickAccessors[i].getName().endsWith("*")) {
                          homeObjectIdentity = quickAccessors[i].getTargetIdentity();
                          break;
                      }
                  }
                  // If no quick accessor is found fall back to root object 0
                  if(homeObjectIdentity == null) {
                      homeObjectIdentity = application.getRootObject()[0].refGetPath();
                  }              
                  view = new ShowObjectView(
                      UUIDs.getGenerator().next().toString(),
                      null,
                      homeObjectIdentity,
                      application,
                      MapUtils.orderedMap(new HashMap()),
                      null,
                      null
                  );
              }
              // FIND_OBJECT events are handled even if the view is newly created
              // Otherwise send back a window.location.href. This reloads the page based on this view
              if(
                  (view != null) && 
                  (event != Action.EVENT_FIND_OBJECT)
              ) {
                  view.createRequestId();
                  showViewsCache.addView(
                      view.getRequestId(),
                      view
                  );
                  Action action = view.getObjectReference().getSelectObjectAction();
                  HtmlPage p = HtmlPageFactory.openPage(
                      view,
                      request,
                      EventHandlerHelper.getWriter(request, response)
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
              AppLog.warning("can not create ShowObjectView", e.getMessage());
              AppLog.warning(e0.getMessage(), e0.getCause());
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
        AppLog.detail("time (ms) to parse parameters and refresh config", (t1-t0));
        t0 = t1;        
        // EVENT_RELOAD. Refresh application context.
        if(event == Action.EVENT_RELOAD) {
            try {            
                application.createPmControl();
                application.resetPmData();
                // Reload codes and data in case the init principal
                // issues a reload
                if(this.roleMapper.isRootPrincipal(application.getCurrentUserRole())) {
                    // Codes
                    try {
                        new CodesLoader(
                            this.getServletContext(), 
                            this.roleMapper,
                            this.pmfMetaData              
                        ).loadCodes(
                            this.locale
                        );
                        this.codes.refresh();
                    }
                    catch(ServiceException e) {
                    	this.log("code import failed", e);
                    }
                    // Data
                    try {
                        new DataLoader(
                            this.getServletContext(),
                            this.roleMapper,
                            this.pmfMetaData
                        ).loadData(
                            "data"
                        );
                    }
                    catch(ServiceException e) {
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
                EventHandlerHelper.notifyObjectModified(
                    showViewsCache
                );                
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
            AppLog.trace("name", name);
            AppLog.trace("mimeType", mimeType);
            response.setContentType(mimeType);
            response.setHeader("Content-disposition", "attachment;filename=" + name);            
            OutputStream os = response.getOutputStream();
            // EVENT_DOWNLOAD_FROM_LOCATION
            if(event == Action.EVENT_DOWNLOAD_FROM_LOCATION) {
                String location = Action.getParameter(parameter, Action.PARAMETER_LOCATION);
                AppLog.trace("location", location);
                InputStream is = new FileInputStream(
                    application.getTempFileName(location, "")
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
                        AppLog.warning("can not write stream");
                        AppLog.warning(e0.getMessage(), e0.getCause());
                    }
                }
            }
            // EVENT_DOWNLOAD_FROM_FEATURE
            else {
                try {
                    Path objectIdentity = new Path(Action.getParameter(parameter, Action.PARAMETER_OBJECTXRI));
                    RefObject_1_0 refObj = (RefObject_1_0)application.getPmData().getObjectById(objectIdentity);
                    String feature = Action.getParameter(parameter, Action.PARAMETER_FEATURE);
                    ModelElement_1_0 featureDef = application.getModel().getElement(feature);
                    if(Multiplicities.STREAM.equals(featureDef.objGetValue("multiplicity"))) {
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
                }
                catch(Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    AppLog.warning("can not write stream");
                    AppLog.warning(e0.getMessage(), e0.getCause());
                }
            }
            os.close();
            // PERFORMANCE
            t1 = System.currentTimeMillis();
            AppLog.detail("time (ms) to handle event", (t1-t0));
            t0 = t1;       
        }    
        // FindObjectsEventHandler
        else if(FindObjectsEventHandler.acceptsEvent(event)) {
            FindObjectsEventHandler.handleRequest(
                request,
                response,
                application,
                parameter,
                (String[])parameterMap.get(WebKeys.REQUEST_PARAMETER_FILTER_VALUES)
            );
            t1 = System.currentTimeMillis();
            AppLog.detail("time (ms) to handle find object event", (t1-t0));
            t0 = t1;                   
        }    
        // GridEventHandler
        else if(GridEventHandler.acceptsEvent(event)) {
            GridEventHandler.handleEvent(
                event,
                view,
                request,
                response,
                application,
                parameter,
                parameterMap,
                showViewsCache
            );
            t1 = System.currentTimeMillis();
            AppLog.detail("time (ms) to handle grid event", (t1-t0));
            t0 = t1;                   
        }
        // SessionEventHandler
        else if(SessionEventHandler.acceptsEvent(event)) {
            SessionEventHandler.handleEvent(
                event,
                view,
                request,
                response,
                application,
                parameter,
                parameterMap
            );
            t1 = System.currentTimeMillis();
            AppLog.detail("time (ms) to handle session event", (t1-t0));
            t0 = t1;                       
        }                
        // Dispatch event to view    
        else {                    
            // handle action and return view for new target
            AppLog.detail("parameterMap", parameterMap);
            HandleEventResult result = null;
            try {
                if(view != null) {
                    view.getApplicationContext().getErrorMessages().clear();
                    if(LookupObjectEventHandler.acceptsEvent(event)) {
                        result = LookupObjectEventHandler.handleEvent(
                            event,
                            view,
                            request,
                            response,
                            application,
                            parameter,
                            parameterMap
                        );                  
                    }
                    else if(ShowObjectEventHandler.acceptsEvent(event)) {
                        result = ShowObjectEventHandler.handleEvent(
                            event,
                            (ShowObjectView)view,
                            parameter,
                            session,
                            parameterMap,
                            showViewsCache
                        );
                    }
                    else if(EditObjectEventHandler.acceptsEvent(event)) {
                        result = EditObjectEventHandler.handleEvent(
                            event,
                            (EditObjectView)view,
                            request,
                            response,
                            parameter,
                            session,
                            parameterMap,
                            editViewsCache,
                            showViewsCache
                        );                  
                    }
                }
            }
            catch(Exception e) {
                AppLog.warning("handleEvent throws exception", e.getMessage());
                new ServiceException(e).log();
            }    
            // PERFORMANCE
            t1 = System.currentTimeMillis();
            AppLog.detail("time (ms) to handle event", (t1-t0));
            t0 = t1;      
            if(
                (result == null) ||
                (result.getStatusCode() == HandleEventResult.StatusCode.FORWARD)
            ) {
                ObjectView nextView = result == null ? 
                    null : 
                    result.getView();
                // No nextView. go back to default view
                if(nextView == null) {
                    AppLog.detail("no nextView. Creating default");
                    try {
                        nextView = new ShowObjectView(
                            UUIDs.getGenerator().next().toString(),
                            null,
                            application.getRootObject()[0].refGetPath(),
                            application,
                            MapUtils.orderedMap(new HashMap()),
                            null,
                            null
                        );
                    }
                    catch(Exception e) {
                        AppLog.warning("Can not get default view", e.getMessage());
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
                // reply
                ServletContext sc = this.getServletContext();
                RequestDispatcher rd = sc.getRequestDispatcher(
                    "/jsp/" + nextView.getType() + ".jsp" + 
                    "?" + Action.PARAMETER_REQUEST_ID + "=" + nextView.getRequestId() +
                    "&" + Action.PARAMETER_SCOPE + "=" + result.getPaintScope().toString()
                ); 
                AppLog.detail("forward reply");
                try {
                    rd.forward(request, response);
                }
                catch(Exception e) {
                    AppLog.warning("Unable forward request", e.getMessage());
                }
                AppLog.detail("done");
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
    private PortalExtension_1_0 evaluator = null;
    private HtmlEncoder_1_0 htmlEncoder = null;
    private RoleMapper_1_0 roleMapper = null;
    private Model_1_3 model = null;
    private List<Path> retrieveByPathPatterns = null;
    private Map filters = new HashMap();
    private Codes codes = null;
    private TextsFactory textsFactory = null;
    private LayoutFactory layoutFactory = null;
    private ReportDefinitionFactory reportFactory = null;
    private WizardDefinitionFactory wizardFactory = null;
    private ControlFactory controlFactory = null;
    private int requestSizeThreshold = 100000;
    private int requestSizeMax = 4000000;
    private int uiRefreshRate = 0;
    private String favoritesReference = null;
    private String[] locale;
    private Map<String,String> mimeTypeImpls = null;
    private String exceptionDomain = null;
    private String filterCriteriaField = null;
    private String[] filterValuePattern = new String[]{"(?i)",  ".*", ".*"};
    private UiLoader uiLoader;
    private FilterLoader filterLoader;
    private TextsLoader textsLoader;
    private LayoutLoader layoutLoader;
    private ReportsLoader reportsLoader;
    private WizardsLoader wizardsLoader;
  
}

//--- End of File -----------------------------------------------------------
