/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ApplicationContextConfiguration.java,v 1.1 2010/03/07 01:55:46 wfro Exp $
 * Description: PortalSessionConfiguration 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/07 01:55:46 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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

package org.openmdx.portal.servlet;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManagerFactory;

import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.portal.servlet.control.ControlFactory;
import org.openmdx.portal.servlet.view.LayoutFactory;

/**
 * PortalSessionConfiguration
 *
 */
public class ApplicationContextConfiguration {

	public String getApplicationName() {
		return this.applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getLocale() {
		return this.locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getTimezone() {
		return this.timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public ControlFactory getControlFactory() {
		return this.controlFactory;
	}

	public void setControlFactory(ControlFactory controlFactory) {
		this.controlFactory = controlFactory;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getLoginPrincipal() {
		return this.loginPrincipal;
	}

	public void setLoginPrincipal(String loginPrincipal) {
		this.loginPrincipal = loginPrincipal;
	}

	public Path getLoginRealmIdentity() {
		return this.loginRealmIdentity;
	}

	public void setLoginRealmIdentity(Path loginRealmIdentity) {
		this.loginRealmIdentity = loginRealmIdentity;
	}

	public List getRetrieveByPathPatterns() {
		return this.retrieveByPathPatterns;
	}

	public void setRetrieveByPathPatterns(List retrieveByPathPatterns) {
		this.retrieveByPathPatterns = retrieveByPathPatterns;
	}

	public String getUserHomeIdentity() {
		return this.userHomeIdentity;
	}

	public void setUserHomeIdentity(String userHomeIdentity) {
		this.userHomeIdentity = userHomeIdentity;
	}

	public String[] getRootObjectIdentities() {
		return this.rootObjectIdentities;
	}

	public void setRootObjectIdentities(String[] rootObjectIdentities) {
		this.rootObjectIdentities = rootObjectIdentities;
	}

	public PortalExtension_1_0 getPortalExtension() {
		return this.portalExtension;
	}

	public void setPortalExtension(PortalExtension_1_0 portalExtension) {
		this.portalExtension = portalExtension;
	}

	public HtmlEncoder_1_0 getHttpEncoder() {
		return this.httpEncoder;
	}

	public void setHttpEncoder(HtmlEncoder_1_0 httpEncoder) {
		this.httpEncoder = httpEncoder;
	}

	public Map getFilters() {
		return this.filters;
	}

	public void setFilters(Map filters) {
		this.filters = filters;
	}

	public Codes getCodes() {
		return this.codes;
	}

	public void setCodes(Codes codes) {
		this.codes = codes;
	}

	public LayoutFactory getLayoutFactory() {
		return this.layoutFactory;
	}

	public void setLayoutFactory(LayoutFactory layoutFactory) {
		this.layoutFactory = layoutFactory;
	}

	public File getTempDirectory() {
		return this.tempDirectory;
	}

	public void setTempDirectory(File tempDirectory) {
		this.tempDirectory = tempDirectory;
	}

	public String getTempFilePrefix() {
		return this.tempFilePrefix;
	}

	public void setTempFilePrefix(String tempFilePrefix) {
		this.tempFilePrefix = tempFilePrefix;
	}

	public String getQuickAccessorsReference() {
		return this.quickAccessorsReference;
	}

	public void setQuickAccessorsReference(String quickAccessorsReference) {
		this.quickAccessorsReference = quickAccessorsReference;
	}

	public Map getMimeTypeImpls() {
		return this.mimeTypeImpls;
	}

	public void setMimeTypeImpls(Map mimeTypeImpls) {
		this.mimeTypeImpls = mimeTypeImpls;
	}

	public String getExceptionDomain() {
		return this.exceptionDomain;
	}

	public void setExceptionDomain(String exceptionDomain) {
		this.exceptionDomain = exceptionDomain;
	}

	public String getFilterCriteriaField() {
		return this.filterCriteriaField;
	}

	public void setFilterCriteriaField(String filterCriteriaField) {
		this.filterCriteriaField = filterCriteriaField;
	}

	public String[] getFilterValuePatterns() {
		return this.filterValuePatterns;
	}

	public void setFilterValuePatterns(String[] filterValuePatterns) {
		this.filterValuePatterns = filterValuePatterns;
	}

	public PersistenceManagerFactory getPmfData() {
		return this.pmfData;
	}

	public void setPmfData(PersistenceManagerFactory pmfData) {
		this.pmfData = pmfData;
	}

	public RoleMapper_1_0 getRoleMapper() {
		return this.roleMapper;
	}

	public void setRoleMapper(RoleMapper_1_0 roleMapper) {
		this.roleMapper = roleMapper;
	}

	public Model_1_0 getModel() {
		return this.model;
	}

	public void setModel(Model_1_0 model) {
		this.model = model;
	}

	public ViewPort.Type getViewPortType() {
		return this.viewPortType;
	}

	public void setViewPortType(ViewPort.Type viewPortType) {
		this.viewPortType = viewPortType;
	}

	public String getUserRole() {
		return this.userRole;
	}    

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}
        
	private String applicationName;
    private String locale;
    private String timezone;
    private ControlFactory controlFactory;
    private String sessionId;
    private ViewPort.Type viewPortType;
	private String loginPrincipal;
    private String userRole;
	private Path loginRealmIdentity;
    private List retrieveByPathPatterns;
    private String userHomeIdentity;
    private String[] rootObjectIdentities;
    private PortalExtension_1_0 portalExtension;
    private HtmlEncoder_1_0 httpEncoder;
    private Map filters;
    private Codes codes;
    private LayoutFactory layoutFactory;
    private File tempDirectory;
    private String tempFilePrefix;
    private String quickAccessorsReference;
    private Map mimeTypeImpls;
    private String exceptionDomain;
    private String filterCriteriaField;
    private String[] filterValuePatterns;
    private PersistenceManagerFactory pmfData;
    private RoleMapper_1_0 roleMapper;
    private Model_1_0 model;
	
}
