/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ApplicationContext.java,v 1.66 2008/09/18 14:30:26 wfro Exp $
 * Description: ApplicationContext
 * Revision:    $Revision: 1.66 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/18 14:30:26 $
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
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */
package org.openmdx.portal.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jmi.reflect.RefObject;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.portal.servlet.texts.TextsFactory;
import org.openmdx.portal.servlet.texts.Texts_1_0;
import org.openmdx.portal.servlet.view.LayoutFactory;
import org.openmdx.security.realm1.jmi1.Principal;

public final class ApplicationContext
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public ApplicationContext(
        String applicationName,
        String locale,
        String timezone,
        UiContext uiContext,
        String sessionId,
        String loginPrincipal,
        String userRole,
        Path loginRealmIdentity,
        List retrieveByPathPatterns,
        String userHomeIdentity,
        String[] rootObjectIdentities,
        PortalExtension_1_0 portalExtension,
        HtmlEncoder_1_0 httpEncoder,
        Map filters,
        Codes codes,
        TextsFactory textsFactory,
        LayoutFactory layoutFactory,
        File tempDirectory,
        String tempFilePrefix,
        String quickAccessorsReference,
        Map mimeTypeImpls,
        String exceptionDomain,
        String filterCriteriaField,
        String[] filterValuePatterns,
        PersistenceManagerFactory pmfData,
        RoleMapper_1_0 roleMapper,
        Model_1_0 model
    ) throws ServiceException {
        this.applicationName = applicationName;
        this.uiContext = uiContext;
        this.loginPrincipal = loginPrincipal;
        this.retrieveByPathPatterns = retrieveByPathPatterns;
        this.userHomeIdentity = userHomeIdentity;
        this.rootObjectIdentities = rootObjectIdentities;
        this.portalExtension = portalExtension;
        this.htmlEncoder = httpEncoder;
        this.filters = filters;
        this.codes = codes;
        this.textsFactory = textsFactory;
        this.layoutFactory = layoutFactory;
        this.tempDirectory = tempDirectory;
        this.tempFilePrefix = tempFilePrefix;
        this.quickAccessorsReference = quickAccessorsReference;
        this.mimeTypeImpls = mimeTypeImpls;
        this.exceptionDomain = exceptionDomain;
        this.filterCriteriaField = filterCriteriaField;
        this.filterValuePattern = filterValuePatterns;
        this.pmfData = pmfData;
        this.errorMessages = new ArrayList<String>();
        this.userRoles = new ArrayList<String>();
        this.userRoles.add(this.loginPrincipal);
        this.roleMapper = roleMapper;
        this.model = model;
        if(this.loginPrincipal == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.AUTHORIZATION_FAILURE, 
                null,
                "Login principal is null. Can not create application context"
            );          
        }
        this.currentUserRole = this.loginPrincipal;
        try {
            this.createPmData();
        }
        catch(ServiceException e) {
            throw new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.AUTHORIZATION_FAILURE, 
                null,
                "Can not initialize connection to dataproviders"
            );
        }
        // Get user roles if security is enabled
        if(loginRealmIdentity != null) {
            org.openmdx.security.realm1.jmi1.Realm loginRealm = null;
            try {
                loginRealm = (org.openmdx.security.realm1.jmi1.Realm)this.pmData.getObjectById(loginRealmIdentity);
            }
            catch(Exception e) {
                throw new ServiceException(
                    e,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INITIALIZATION_FAILURE, 
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("realm", loginRealmIdentity)
                    },
                    "Unable to login. Can not access login realm"
                );
            }
            if(roleMapper.checkPrincipal(loginRealm, this.loginPrincipal, this.pmData) == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.AUTHORIZATION_FAILURE, 
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("realm", loginRealmIdentity),                          
                        new BasicException.Parameter("principal", this.loginPrincipal)
                    },
                    "Unable to login. Principal is not registered or disabled"
                );
            }
            try {
                this.userRoles = roleMapper.getUserInRoles(
                    loginRealm,
                    this.loginPrincipal,
                    this.pmData
                );
            }
            catch(Exception e) {
                throw new ServiceException(
                    e,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.PROCESSING_FAILURE, 
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("realm", loginRealmIdentity),                          
                        new BasicException.Parameter("principal", this.loginPrincipal)
                    },
                    "Unable to login. Can not get roles of principal"
                );
            }
            // Check whether userRole exists and is a qualified name
            if(
                this.userRoles.contains(userRole) &&
                (userRole.indexOf("@") >= 0)
            ) {
                this.currentUserRole = userRole;
                this.currentSegment = userRole.substring(userRole.indexOf("@") + 1);
            }
            else if(!this.userRoles.isEmpty()) {
                this.currentUserRole = (String)this.userRoles.iterator().next();
                this.currentSegment = this.currentUserRole.substring(this.currentUserRole.indexOf("@") + 1);
            }
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.AUTHORIZATION_FAILURE, 
                    new BasicException.Parameter[]{
                        new BasicException.Parameter("realm", loginRealmIdentity),                          
                        new BasicException.Parameter("principal", this.loginPrincipal)
                    },
                    "Unable to login. Principal does not have any assigned roles"
                );
            }
        }
        try {
            this.createPmControl(false);
            this.resetPmData();
        }
        catch(ServiceException e) {
            throw new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.PROCESSING_FAILURE, 
                new BasicException.Parameter[]{
                    new BasicException.Parameter("realm", loginRealmIdentity),                          
                    new BasicException.Parameter("principal", this.loginPrincipal)
                },
                "Can not refresh application context"
            );
        }      
        // Update login date for current principal
        if(
            (loginRealmIdentity != null) &&          
            (this.currentUserRole != null) &&
            (this.currentSegment != null)
        ) {
            try {
                Principal principal = (Principal)this.pmData.getObjectById(
                    loginRealmIdentity.getParent().getDescendant(
                        new String[]{this.currentSegment, "principal", this.currentUserRole.substring(0, this.currentUserRole.indexOf("@"))}
                    )
                );              
                // Don't care if feature 'lastLoginAt does not exist on principal
                this.pmData.currentTransaction().begin();
                principal.refSetValue("lastLoginAt", new Date());
                this.pmData.currentTransaction().commit();
            } 
            catch(Exception e) {
                try {
                    this.pmData.currentTransaction().rollback();
                } catch(Exception e0) {}
                new ServiceException(e).log();
            }
        }      
        this.loadUserHome();
        this.loadSettings();
        // Init locale
        if(this.getSettings().getProperty(PROPERTY_LOCALE_NAME) != null) {
            this.setCurrentLocale(
                this.getSettings().getProperty(PROPERTY_LOCALE_NAME)
            );
        }
        else {
            this.setCurrentLocale(
                locale == null ? "en_US" : locale
            );
        }
        // Init TimeZone
        if(this.getSettings().getProperty(PROPERTY_TIMEZONE_NAME) != null) {
            this.setCurrentTimeZone(
                this.getSettings().getProperty(PROPERTY_TIMEZONE_NAME)
            );
        }
        else {
            this.setCurrentTimeZone(
                timezone == null ? TimeZone.getDefault().getID() : timezone
            );
        }
        this.loadQuickAccessors();
        this.loadRootObjects();
    }

    //-------------------------------------------------------------------------
    public static String getOrderAsString(
        List order
    ) {
        if(order == null) return "0";
        String orderAsString = "";
        for(int i = 0; i < order.size(); i++) {
            if(i > 0) orderAsString += ":";
            orderAsString += "" + order.get(i);
        }
        return orderAsString;
    }

    //-------------------------------------------------------------------------
    private Path mapIdentity(
        String pattern
    ) {
        String strippedUserRole = this.currentUserRole.indexOf("@") >= 0 
        ? this.currentUserRole.substring(0, this.currentUserRole.indexOf("@")) 
            : this.currentUserRole;
        return this.mapIdentity(
            pattern,
            strippedUserRole,
            false
        );
    }

    //-------------------------------------------------------------------------
    private Path mapIdentityAsPattern(
        String pattern
    ) {
        return this.mapIdentity(
            pattern,
            null,
            true
        );
    }

    //-------------------------------------------------------------------------
    private Path mapIdentity(
        String pattern,
        String strippedUserRole,
        boolean asPattern
    ) {
        String mapped = "";
        int i = 0;
        while(i < pattern.length()) {
            if(pattern.substring(i).startsWith("${USER}")) {
                if(asPattern) {
                    mapped += ":*";
                }
                else {
                    mapped += strippedUserRole;
                }
                i += 7;
            }
            else if(pattern.substring(i).startsWith("${SEGMENT}")) {
                if(asPattern) {
                    mapped += ":*";
                }
                else {
                    mapped += this.currentSegment;
                }
                i += 10;            
            }
            else {
                mapped += pattern.charAt(i);
                i++;
            }
        }
        return new Path(mapped);
    }

    //-------------------------------------------------------------------------
    private void loadRootObjects(
    ) {
        // Retrieve root objects
        List<Object> rootObjects = new ArrayList<Object>();
        for(
                int i = 0; 
                i < this.rootObjectIdentities.length; 
                i++
        ) {
            Path mappedIdentity = this.mapIdentity(this.rootObjectIdentities[i]);
            try {
                rootObjects.add(
                    pmControl.getObjectById(mappedIdentity)
                );
            }
            catch(Exception e) {
                AppLog.info("Can not get root object", mappedIdentity);
                AppLog.info(e.getMessage(), e.getCause());              
            }
        }
        this.rootObjects = (RefObject_1_0[])rootObjects.toArray(new RefObject_1_0[rootObjects.size()]);      
        this.rootObjectActions = null;
    }
  
    //-------------------------------------------------------------------------
    private void loadUserHome(
    ) {
        this.userHome = null;      
        try {
            if(this.userHomeIdentity != null) {
                Path mappedIdentity = this.mapIdentity(this.userHomeIdentity);
                this.userHome = (RefObject_1_0)pmControl.getObjectById(mappedIdentity);
            }
        }
        catch(Exception e) {
            AppLog.warning("can not get user home object. Skipping");
        }      
    }
  
    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void loadQuickAccessors(
    ) {
        Map<String,QuickAccessor> quickAccessors = new TreeMap<String,QuickAccessor>();    
        if(this.quickAccessorsReference != null) {
            List<Path> quickAccessorsReferences = new ArrayList<Path>();
            // Quick accessors of segment admin
            quickAccessorsReferences.add(
                this.mapIdentity(
                    this.quickAccessorsReference,
                    this.roleMapper.getAdminPrincipal(this.currentSegment),
                    false
                )
            );
            // Quick accessors of current user
            Path quickAccessorsReference = this.mapIdentity(
                this.quickAccessorsReference
            );
            if(!quickAccessorsReferences.contains(quickAccessorsReference)) {
                quickAccessorsReferences.add(quickAccessorsReference);
            }
            int ii = 0;
            for(
                Iterator i = quickAccessorsReferences.iterator(); 
                i.hasNext();
                ii++
            ) {
                quickAccessorsReference = (Path)i.next();
                RefObject_1_0 parent = null;
                try {
                    parent = (RefObject_1_0)this.pmData.getObjectById(
                      quickAccessorsReference.getParent()
                    );
                    if(parent != null) {
                      for(
                        Iterator j = ((Collection)parent.refGetValue(quickAccessorsReference.getBase())).iterator();
                        j.hasNext();
                      ) {
                        try {
                            RefObject_1_0 quickAccessor = (RefObject_1_0)j.next();
                            RefObject_1_0 target = null;
                            String name = null;
                            String description = null;
                            String iconKey = null;
                            Number actionType = null;
                            String actionName = null;
                            List<String> actionParams = null;
                            try {target = (RefObject_1_0)quickAccessor.refGetValue("reference");} catch(Exception e) {}
                            try {name = (String)quickAccessor.refGetValue("name");} catch(Exception e) {}
                            try {description = (String)quickAccessor.refGetValue("description");} catch(Exception e) {}
                            try {iconKey = (String)quickAccessor.refGetValue("iconKey");} catch(Exception e) {}
                            try {actionType = (Number)quickAccessor.refGetValue("actionType");} catch(Exception e) {}
                            try {actionName = (String)quickAccessor.refGetValue("actionName");} catch(Exception e) {}
                            try {actionParams = (List<String>) quickAccessor.refGetValue("actionParam");} catch(Exception e) {}
                            if(name == null) {
                                name = new ObjectReference(target, this).getTitle();
                            }           
                            if(target != null) {
                                int matchingRootObjectIdentity = -1;
                                for(int k = 0; k < this.rootObjectIdentities.length; k++) {
                                    Path rootObjectPattern = this.mapIdentityAsPattern(this.rootObjectIdentities[k]);
                                    if(target.refGetPath().isLike(rootObjectPattern)) {
                                        matchingRootObjectIdentity = k;
                                        break;
                                    }                                    
                                }
                                quickAccessors.put(
                                    ii + ":" + name + ":" + quickAccessor.refMofId(), // Order accessors by (parent, name, accessor)
                                    new QuickAccessor(
                                        matchingRootObjectIdentity >= 0 
                                            ? this.mapIdentity(this.rootObjectIdentities[matchingRootObjectIdentity]) 
                                            : target.refGetPath(),
                                        name,
                                        description == null ? name : description,
                                        iconKey,
                                        actionType,
                                        actionName,
                                        actionParams
                                    )
                                );
                            }
                        }
                        catch(Exception e) {
                          ServiceException e0 = new ServiceException(e);
                          AppLog.info("Can not get quick accessors", e.getMessage());
                          AppLog.detail(e0.getMessage(), e0.getCause());
                        }
                      }
                  }
                }
                catch(Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    AppLog.warning("Can not get quick accessor container", e.getMessage());
                    AppLog.detail(e0.getMessage(), e0.getCause());     
                }
            }
        }
        this.quickAccessors = (QuickAccessor[])quickAccessors.values().toArray(
            new QuickAccessor[quickAccessors.size()]
        );      
    }

    //-------------------------------------------------------------------------
    public QuickAccessor[] getQuickAccessors(
    ) {
        return this.quickAccessors;
    }

    //-------------------------------------------------------------------------
    public void saveSettings(
        boolean logoff
    ) {
        if(this.userHome != null) {
            boolean saveSettings = true;
            try {
                this.userHome.refRefresh();
                if(logoff) {
                    Boolean storeSettingsOnLogoff = (Boolean)this.userHome.refGetValue("storeSettingsOnLogoff");
                    saveSettings = (storeSettingsOnLogoff == null) || storeSettingsOnLogoff.booleanValue();
                }
            }
            catch(Exception e) {}
            if(saveSettings) {
                this.storeSettings();
            }
        }
    }

    //-------------------------------------------------------------------------
    private void storeSettings(
    ) {
        if(this.userHome != null) {
            try {                  
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                this.settings.store(
                    bs,
                    "settings of user " + this.userHome.refMofId() 
                );
                bs.close();

                this.pmControl.currentTransaction().begin();
                this.userHome.refSetValue(
                    "settings",
                    bs.toString("UTF-8")
                );
                this.pmControl.currentTransaction().commit();
            }
            catch(Exception e) {
                AppLog.warning("Can not store user settings. Skipping", e.getMessage());    
                try {
                    this.pmControl.currentTransaction().rollback();
                } catch(Exception e0) {}
            }
        }
    }
  
    //-------------------------------------------------------------------------
    private void loadSettings(
    ) {
        this.settings = new Properties();
        if(this.userHome != null) {
            try {
                this.settings = new Properties();
                if(this.userHome.refGetValue("settings") != null) {
                    this.settings.load(
                        new ByteArrayInputStream(
                            ((String)this.userHome.refGetValue("settings")).getBytes("UTF-8")
                        )
                    );
                }
            }
            catch(Exception e) {
                AppLog.warning("can not get settings from user home. Init with empty settings", e);              
            }
        }
    }

    //-------------------------------------------------------------------------
    public String getApplicationName(
    ) {
        return this.applicationName;
    }

    //-------------------------------------------------------------------------
    public RefObject_1_0[] getRootObject(
    ) {
        return this.rootObjects;
    }

    //-------------------------------------------------------------------------
    public Action[] getRootObjectActions(
    ) {
        if(this.rootObjectActions == null) {
            RefObject[] rootObject = this.getRootObject();
            List<Action> actions = new ArrayList<Action>();
            Set<String> labels = new HashSet<String>();
            Set<String> refMofIds = new HashSet<String>();
            // For each configured root object add the following actions:
            // * EVENT_SELECT_OBJECT action for root object
            // * EVENT_SELECT_OBJECT action for all references (tabs) of root object
            // * EVENT_SELECT_AND_NEW_OBJECT action for all referenced, changeable, non-abstract objects
            for(
                int i = 0; 
                i < rootObject.length; 
                i++
            ) {
                if(!refMofIds.contains(rootObject[i].refMofId())) {
                    String rootObjectClass = rootObject[i].refClass().refMofId();
                    String label = this.getLabel(rootObjectClass);
                    String iconKey = WebKeys.ICON_MISSING;
                    try {
                        iconKey =  this.getIconKey(rootObjectClass);
                    }
                    catch(ServiceException e) {
                        AppLog.detail("can not get icon key", rootObjectClass);
                        AppLog.detail(e.getMessage(), e.getCause());
                    }
                    // EVENT_SELECT_OBJECT action for root object
                    actions.add(
                        new Action(
                            Action.EVENT_SELECT_OBJECT,
                            new Action.Parameter[]{
                                new Action.Parameter(Action.PARAMETER_OBJECTXRI, rootObject[i].refMofId())
                            },
                            // append qualifier in case there are root object labels with same label
                            labels.contains(label) ? 
                                label + " (" + new Path(rootObject[i].refMofId()).get(4) + ")" : 
                                label,
                            iconKey,
                            true
                        )
                    );
                    labels.add(label);
                    refMofIds.add(rootObject[i].refMofId());
                    // Add EVENT_SELECT_OBJECT for all references and EVENT_SELECT_AND_NEW_OBJECT 
                    // actions for all changeable, composite objects
                    try {
                        org.openmdx.ui1.jmi1.Inspector inspector = this.getInspector(rootObjectClass);
                        int paneIndex = 0;
                        for(
                            Iterator j = inspector.getMember().iterator();
                            j.hasNext();
                        ) {
                            Object paneRef = j.next();
                            if(paneRef instanceof org.openmdx.ui1.jmi1.ReferencePane) {
                                int referenceIndex = 0;
                                for(
                                    Iterator k = ((org.openmdx.ui1.jmi1.ReferencePane)paneRef).getMember().iterator();
                                    k.hasNext();
                                    referenceIndex++
                                ) {
                                    org.openmdx.ui1.jmi1.Tab tab = (org.openmdx.ui1.jmi1.Tab )k.next();
                                    String title = 
                                        this.getCurrentLocaleAsIndex() < tab.getTitle().size() ? 
                                            tab.getTitle().get(this.getCurrentLocaleAsIndex()) : 
                                            tab.getTitle().get(0);
                                        if(title.startsWith("\u00BB")) title = title.substring(1);
                                        if(title.length() > 1) {
                                            // EVENT_SELECT_OBJECT for reference jj
                                            actions.add(
                                                new Action(
                                                    Action.EVENT_SELECT_OBJECT,
                                                    new Action.Parameter[]{
                                                        new Action.Parameter(Action.PARAMETER_PANE, "" + paneIndex), 
                                                        new Action.Parameter(Action.PARAMETER_REFERENCE, "" + referenceIndex),
                                                        new Action.Parameter(Action.PARAMETER_OBJECTXRI, rootObject[i].refMofId())
                                                    },
                                                    title,
                                                    tab.getIconKey(),
                                                    true
                                                )
                                            );
                                        }
                                }
                                paneIndex++;
                            }
                        }
                    }
                    catch(ServiceException e) {
                        AppLog.detail("can not get inspector", rootObjectClass);
                        AppLog.detail(e.getMessage(), e.getCause());
                    }
                }
            }
            this.rootObjectActions = (Action[])actions.toArray(new Action[actions.size()]);
        }
        return this.rootObjectActions;
    }
    
    //-------------------------------------------------------------------------
    public UiContext getUiContext(
    ) {
        return this.uiContext;
    }

    //-------------------------------------------------------------------------
    public PortalExtension_1_0 getPortalExtension(
    ) {
        return this.portalExtension;
    }

    //-------------------------------------------------------------------------
    public HtmlEncoder_1_0 getHtmlEncoder(
    ) {
        return this.htmlEncoder;
    }

    //-------------------------------------------------------------------------
    public org.openmdx.ui1.jmi1.Inspector getInspector(
        String forClass
    ) throws ServiceException {
        return this.uiContext.getInspector(forClass);
    }

    //-------------------------------------------------------------------------
    public org.openmdx.ui1.jmi1.AssertableInspector getAssertableInspector(
        String forClass
    ) throws ServiceException {
        return this.uiContext.getAssertableInspector(forClass);
    }

    //-------------------------------------------------------------------------
    public String getLabel(
        String forClass
    ) {
        return this.uiContext.getLabel(
            forClass,
            this.getCurrentLocaleAsIndex()
        );
    }

    //-------------------------------------------------------------------------
    public String getIconKey(
        String forClass
    ) throws ServiceException {
        return this.uiContext.getIconKey(
            forClass
        );
    }
  
    //-------------------------------------------------------------------------
    public String getBackColor(
        String forClass
    ) throws ServiceException {
        return this.uiContext.getBackColor(
            forClass
        );
    }
  
    //-------------------------------------------------------------------------
    public String getColor(
        String forClass
    ) throws ServiceException {
        return this.uiContext.getColor(
            forClass
        );
    }
  
    //-------------------------------------------------------------------------
    public Filters getFilters(
        String forReference
    ) {
        return (Filters)this.filters.get(forReference);
    }
  
    //-------------------------------------------------------------------------
    public Codes getCodes(
    ) {
        return this.codes;
    }

    //-------------------------------------------------------------------------
    public String getLayout(
        String forClass,
        boolean forEditing
    ) {      
        return this.layoutFactory.getLayout(
            this.currentLocaleAsString,
            forClass,
            forEditing
        );
    }
  
    //-------------------------------------------------------------------------
    public Texts_1_0 getTexts(
    ) {
        return this.textsFactory.getTexts(this.currentLocaleAsString);
    }

    //-------------------------------------------------------------------------
    public TextsFactory getTextsFactory(
    ) {
        return this.textsFactory;
    }

    //-------------------------------------------------------------------------
    public String getCurrentTimeZone(
    ) {
        return this.currentTimeZone;
    }
  
    //-------------------------------------------------------------------------
    public synchronized void setCurrentTimeZone(
        String id
    ) {
        String[] availableIDs = TimeZone.getAvailableIDs();
        if(Arrays.asList(availableIDs).contains(id) || id.startsWith("GMT")) {
            this.currentTimeZone = id;
        }
        // Save in settings
        this.getSettings().setProperty(
            PROPERTY_TIMEZONE_NAME,
            this.currentTimeZone
        );
        // actions are locale-specific. Invalidate
        this.rootObjectActions = null;
    }
  
    //-------------------------------------------------------------------------
    public synchronized void setCurrentLocale(
        String locale
    ) {
        List locales = Arrays.asList(this.textsFactory.getLocale());
        if(locales.contains(locale)) {
            this.currentLocaleAsString = locale;
        }
        // find locale with matching language
        else {
            String language = locale.substring(0, 2);
            for(int i = 0; i < locales.size(); i++) {
                if(
                        (locales.get(i) != null) && 
                        ((String)locales.get(i)).startsWith(language)
                ) {
                    this.currentLocaleAsString = (String)locales.get(i);
                    return;
                }
            }
            // fall back to locale[0]
            this.currentLocaleAsString = (String)locales.get(0);
        }
        // Save in settings
        this.getSettings().setProperty(
            PROPERTY_LOCALE_NAME,
            this.currentLocaleAsString
        );
        // actions are locale-specific. Invalidate
        this.rootObjectActions = null;
    }
  
    //-------------------------------------------------------------------------
    public short getCurrentLocaleAsIndex(
    ) {
        return this.getTexts().getLocaleIndex();
    }

    //-------------------------------------------------------------------------
    public String getCurrentLocaleAsString(
    ) {
        return this.currentLocaleAsString;
    }
  
    //-------------------------------------------------------------------------
    public Locale getCurrentLocale(
    ) {
        String locale = this.getCurrentLocaleAsString();
        return new Locale(
            locale.substring(0, 2), 
            locale.substring(locale.indexOf("_") + 1)
        );      
    }

    //-------------------------------------------------------------------------
    public String getLoginPrincipalId(
    ) {
        return this.loginPrincipal;
    }

    //-------------------------------------------------------------------------
    public String getCurrentUserRole(
    ) {
        return this.currentUserRole;
    }
  
    //-------------------------------------------------------------------------
    public List getUserRoles(
    ) {
        return this.userRoles;
    }

    //-------------------------------------------------------------------------
    public void addErrorMessage(
        String message,
        String[] parameters
    ) {
        String preparedMessage = "";
        int i = 0;
        while(i < message.length()) {
            if((i <= message.length()-4) && "${".equals(message.substring(i,i+2))) {
                short index = new Short(message.substring(i+2, i+3)).shortValue();
                try {
                    preparedMessage += parameters[index];
                } catch(Exception e) {}
                i += 4;
            }
            else {
                preparedMessage += message.charAt(i);
                i++;
            }
        }
        this.errorMessages.add(preparedMessage);
    }
  
    //-------------------------------------------------------------------------
    public List getErrorMessages(
    ) {
        return this.errorMessages;
    }
  
    //-------------------------------------------------------------------------
    public File getTempDirectory(
    ) {
        return this.tempDirectory;
    }
  
    //-------------------------------------------------------------------------
    public String getTempFileName(
        String name,
        String extension
    ) {
        // eliminate special chars
        String fileName = "";
        for(int i = 0; i < name.length(); i++) {
            if(name.charAt(i) != ':') {
                fileName += name.charAt(i);
            }
        }
        return this.tempDirectory.getPath() + File.separator + this.tempFilePrefix + "-" + fileName + extension;
    }
  
    //-------------------------------------------------------------------------
    public String getExceptionDomain(
    ) {
        return this.exceptionDomain;
    }

    //-------------------------------------------------------------------------
    public String getFilterCriteriaField(
    ) {
        return this.filterCriteriaField;
    }
  
    //-------------------------------------------------------------------------
    private PersistenceManager createPersistenceManager(
        PersistenceManagerFactory pmf,
        String userRole
    ) throws ServiceException {
        List<String> principalChain = new ArrayList<String>();
        userRole = userRole == null || !this.userRoles.contains(userRole) ? this.currentUserRole : userRole;
        // newRole has format <role>@<segment>
        // Remove segment name from qualified role name
        String strippedRole = userRole.indexOf("@") >= 0 ? userRole.substring(0, userRole.indexOf("@")) : userRole;
        principalChain.add(strippedRole);
        if(!this.loginPrincipal.equals(strippedRole)) {
            principalChain.add(this.loginPrincipal);
        }
        return pmf.getPersistenceManager(
            principalChain.toString(), 
            null
        );
    }
  
    //-------------------------------------------------------------------------
    /**
     * Evicts the data persistence manager and rolls back active transactions. 
     * The reset is typically invoked after an operation invocation with isQuery=false.
     */
    public void resetPmData(
    ) throws ServiceException {
        this.pmData.evictAll();
        try {
            this.pmData.currentTransaction().rollback();
        } catch(Exception e) {}
        this.pmDataReloadedAt = new Date();
    }

    //-------------------------------------------------------------------------
    /**
     * Create the data persistence manager. This data persistence manager is used
     * to retrieve objects for the attributes and grid panes. This create is invoked
     * after a logon or role change. As a consequence all views must be destroyed.
     */
    public PersistenceManager createPmData(
    ) throws ServiceException {
        this.pmData = this.createPersistenceManager(
            this.pmfData,
            this.currentUserRole
        );
        this.pmDataReloadedAt = new Date();
        return this.pmData;
    }

    //-------------------------------------------------------------------------
    public PersistenceManager createPmControl(
    ) throws ServiceException {
        return this.createPmControl(true);
    }

    //-------------------------------------------------------------------------
    /**
     * Refresh the control package. This package is used to retrieve control
     * objects, i.e. favorites, user settings, root objects. This refresh is 
     * typically initiated explicitly by the user.
     * @param loadObjects if true control objects such quick accessors, user
     *        home only refresh the controlPkg. Do not load
     *        any objects such as favorites, user settings, root objects.
     */
    private PersistenceManager createPmControl(
        boolean loadControlObjects
    ) throws ServiceException {
        this.pmControl = this.createPersistenceManager(
            this.pmfData,
            this.currentUserRole
        );
        if(loadControlObjects) {
            this.loadQuickAccessors();
            this.loadUserHome();
            this.loadRootObjects();
        }
        else {
            this.quickAccessors = null;
            this.userHome = null;
            this.rootObjects = null;
        }
        return this.pmControl;
    }

    //-------------------------------------------------------------------------
    /**
     * Close application context and all associated resources.
     */
    public void close(
    ) {
        if(this.pmData != null) {
            try {
                this.pmData.close();
            } catch(Exception e) {}
        }
        this.pmData = null;
        if(this.pmControl != null) {
            try {
                this.pmControl.close();
            } catch(Exception e) {}
        }
        this.pmControl = null;      
    }
  
    //-------------------------------------------------------------------------
    public PersistenceManager getPmData(
    ) {
        return this.pmData;
    }

    //-------------------------------------------------------------------------
    public Date getPmDataReloadedAt(
    ) {
        return this.pmDataReloadedAt;
    }

    //-------------------------------------------------------------------------
    public PersistenceManager getPmControl(
    ) {
        return this.pmControl;
    }

    //-------------------------------------------------------------------------
    public Map getMimeTypeImpls(
    ) {
        return this.mimeTypeImpls;
    }

    //-------------------------------------------------------------------------
    public Properties getSettings(
    ) {
        return this.settings;
    }
  
    //-------------------------------------------------------------------------
    public Path getObjectRetrievalIdentity(
        RefObject_1_0 object
    ) {
        Path retrievalIdentity = object.refGetPath();
        Path path = object.refGetPath();
        for(
                Iterator i = this.retrieveByPathPatterns.iterator();
                i.hasNext();
        ) {
            Path pattern = (Path)i.next();
            if(path.isLike(pattern)) {
                retrievalIdentity = path;
                break;
            }
        }
        return retrievalIdentity;
    }
  
    //-------------------------------------------------------------------------
    public void setPanelState(
        String panelName,
        int panelState
    ) {
        this.settings.setProperty(
            "Panel." + panelName + ".State",
            "" + panelState          
        );
    }
  
    //-------------------------------------------------------------------------
    public int getPanelState(
        String panelName
    ) {
        String panelState = this.settings.getProperty(
            "Panel." + panelName + ".State"
        );
        return panelState == null ? 
            0 : 
            Integer.parseInt(panelState);
    }

    //-------------------------------------------------------------------------
    public RoleMapper_1_0 getRoleMapper(
    ) {
        return this.roleMapper;
    }

    //-------------------------------------------------------------------------
    public Model_1_0 getModel(
    ) {
        return this.model;
    }
  
    //-------------------------------------------------------------------------
    /**
     * The like condition patterns define prefix and suffix patterns which are
     * added to filter values entered by the user. E.g. used in grid for completing
     * search value. 
     * @return filter value patterns. 0: pattern prefix 1, 1: pattern prefix 2, 2: suffix
     */
    public String[] getFilterValuePattern(
    ) {
        return this.filterValuePattern;
    }

    //-------------------------------------------------------------------------
    /**
     * Locale-specific parsing of specified number.
     */
    public BigDecimal parseNumber(
        String numberAsString
    ) {
        // get locale-specific DecimalFormat
        Locale userLocale = null;
        Locale[] locales = DecimalFormat.getAvailableLocales();
        for(int j = 0; j < locales.length; j++) {
            if(this.getCurrentLocaleAsString().equals(locales[j].toString())) {
                userLocale = locales[j];
                break;
            }
        }
        DecimalFormat parser = userLocale == null ? 
            (DecimalFormat)DecimalFormat.getInstance() : 
            (DecimalFormat)DecimalFormat.getInstance(userLocale);

        // DecimalFormat is not used to parse the number because
        // it can not parse BigDecimal --> loss of precision
        numberAsString = numberAsString.replace(parser.getDecimalFormatSymbols().getGroupingSeparator(), ' ');
        numberAsString = numberAsString.replace(parser.getDecimalFormatSymbols().getDecimalSeparator(), '.');
        // eliminate blanks
        int pos = 0;
        while((pos = numberAsString.indexOf(' ')) >= 0) {
            numberAsString = numberAsString.substring(0, pos) + numberAsString.substring(pos + 1);
        }
        if(numberAsString.length() > 0) {
            try {
                return new BigDecimal(numberAsString);
            }
            catch(Exception e) {
                ServiceException e0 = new ServiceException(e);
                AppLog.warning(e0.getMessage(), e0.getCause());
                return null;
            }
        }
        else {
            return null;
        }
    }
  
    //-------------------------------------------------------------------------
    public Path getUserHomeIdentity(
    ) {
        return this.mapIdentity(
            this.userHomeIdentity
        );
    }
    
    //-------------------------------------------------------------------------
    public String getCurrentSegment(
    ) {
        return this.currentSegment;
    }
    
    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = -8690003081285454886L;

    private static final String PROPERTY_LOCALE_NAME = "Locale.Name";
    private static final String PROPERTY_TIMEZONE_NAME = "TimeZone.Name";
  
    private final String loginPrincipal;
    private final UiContext uiContext;
    private final String applicationName;
    private final PortalExtension_1_0 portalExtension;
    private final HtmlEncoder_1_0 htmlEncoder;
    private final List retrieveByPathPatterns;
    private final String userHomeIdentity;
    private RefObject_1_0 userHome = null;
    private RefObject_1_0[] rootObjects = null;
    private Action[] rootObjectActions = null;
    private final String[] rootObjectIdentities;
    private final Map filters;
    private final Codes codes;
    private final TextsFactory textsFactory;
    private final LayoutFactory layoutFactory;
    private final List<String> errorMessages;
    private QuickAccessor[] quickAccessors = null;
    private final File tempDirectory;
    private final String tempFilePrefix;
    private final String quickAccessorsReference;
    private final String exceptionDomain;
    private final String filterCriteriaField;
    private final Map mimeTypeImpls;
    private Properties settings = new Properties();
    private List<String> userRoles;
    private String currentUserRole;
    private final RoleMapper_1_0 roleMapper;
    private final String[] filterValuePattern;
    private PersistenceManagerFactory pmfData;
    private PersistenceManager pmData; // package managing data objects
    private Date pmDataReloadedAt;
    private PersistenceManager pmControl; // package managing control objects
    private final Model_1_0 model;
    
    private String currentLocaleAsString = null;
    private String currentTimeZone = null;
    private String currentSegment = "Standard";
  
}

//--- End of File -----------------------------------------------------------
