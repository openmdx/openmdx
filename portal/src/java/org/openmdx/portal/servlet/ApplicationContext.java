/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ApplicationContext.java,v 1.123 2011/11/28 13:31:41 wfro Exp $
 * Description: ApplicationContext
 * Revision:    $Revision: 1.123 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/11/28 13:31:41 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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
import java.util.HashMap;
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

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.UserObjects;
import org.openmdx.base.persistence.spi.PersistenceManagers;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.action.SelectObjectAction;
import org.openmdx.portal.servlet.attribute.AttributeValue;
import org.openmdx.portal.servlet.control.Control;
import org.openmdx.portal.servlet.control.ControlFactory;
import org.openmdx.portal.servlet.control.EditInspectorControl;
import org.openmdx.portal.servlet.control.ShowInspectorControl;
import org.openmdx.portal.servlet.texts.TextsFactory;
import org.openmdx.portal.servlet.texts.Texts_1_0;
import org.openmdx.portal.servlet.view.LayoutFactory;

public final class ApplicationContext
    implements Serializable {
  
    //-------------------------------------------------------------------------
	public ApplicationContext(
	) {	
	}
	
    //-------------------------------------------------------------------------
    public void init(
    ) throws ServiceException {
        this.currentViewPortType = this.getViewPortType();
        this.currentUserRoles = new ArrayList<String>();
        String loginPrincipal = this.getLoginPrincipal();
        if(loginPrincipal == null || loginPrincipal.isEmpty()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.AUTHORIZATION_FAILURE, 
                "Login principal is null or empty. Can not create application context"
            );                  	
        }
        String userRole = this.getUserRole();
        this.currentUserRoles.add(loginPrincipal);
        int posSegmentSeparator; 
        if((posSegmentSeparator = loginPrincipal.indexOf("\\")) > 0) {
        	String segmentName = loginPrincipal.substring(0, posSegmentSeparator);
        	loginPrincipal = loginPrincipal.substring(posSegmentSeparator + 1);
        	if(userRole == null) {
        		userRole = loginPrincipal + "@" + segmentName;
        	}
        }
        this.currentLoginPrincipal = loginPrincipal;
        this.currentUserRole = userRole == null ?
            this.currentLoginPrincipal :
            userRole;
        PersistenceManager pm = null;
        try {
	        pm = this.createPersistenceManager(
	            this.getPmfData(),
	            this.currentUserRole
	        );
        }
        catch(ServiceException e) {
            throw new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE, 
                "Can not initialize persistence manager"
            );
        }
        // Get user roles if security is enabled
        Path loginRealmIdentity = this.getLoginRealmIdentity();
        if(loginRealmIdentity != null) {
            if(!this.portalExtension.checkPrincipal(loginRealmIdentity, loginPrincipal, pm)) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.AUTHORIZATION_FAILURE, 
                    "Unable to login. Principal is not registered or disabled",
                    new BasicException.Parameter("realm", loginRealmIdentity),                          
                    new BasicException.Parameter("principal", loginPrincipal)
                );
            }
            try {
                this.currentUserRoles = this.portalExtension.getUserRoles(
                	loginRealmIdentity,
                    loginPrincipal,
                    pm
                );
            }
            catch(Exception e) {
                throw new ServiceException(
                    e,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.PROCESSING_FAILURE, 
                    "Unable to login. Can not get roles of principal",
                    new BasicException.Parameter("realm", loginRealmIdentity),                          
                    new BasicException.Parameter("principal", loginPrincipal)
                );
            }
            // Check whether userRole exists and is a qualified name
            if(
                this.currentUserRoles.contains(userRole) &&
                (userRole.indexOf("@") >= 0)
            ) {
                this.currentUserRole = userRole;
                this.currentSegment = userRole.substring(userRole.indexOf("@") + 1);
            }
            else if(!this.currentUserRoles.isEmpty()) {
                this.currentUserRole = (String)this.currentUserRoles.iterator().next();
                this.currentSegment = this.currentUserRole.substring(this.currentUserRole.indexOf("@") + 1);
            }
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.AUTHORIZATION_FAILURE, 
                    "Unable to login. Principal does not have any assigned roles",
                    new BasicException.Parameter("realm", loginRealmIdentity),                          
                    new BasicException.Parameter("principal", loginPrincipal)
                );
            }
        }
        pm.close();
        try {
            this.createPmControl(false);
            this.createPmData();
        }
        catch(ServiceException e) {
            throw new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.PROCESSING_FAILURE, 
                "Can not refresh application context",
                new BasicException.Parameter("realm", loginRealmIdentity),                          
                new BasicException.Parameter("principal", loginPrincipal)
            );
        }      
        // Update login date for current principal
        if(
            (loginRealmIdentity != null) &&          
            (this.currentUserRole != null) &&
            (this.currentSegment != null)
        ) {
        	this.portalExtension.setLastLoginAt(
        		loginRealmIdentity, 
        		this.currentSegment, 
        		this.currentUserRole.substring(0, this.currentUserRole.indexOf("@")), 
        		this.pmData
        	);
        }      
        this.loadUserHome();
        this.loadSettings();
        // Init locale
        if(this.getSettings().getProperty(UserSettings.LOCALE_NAME) != null) {
            this.setCurrentLocale(
                this.getSettings().getProperty(UserSettings.LOCALE_NAME)
            );
        }
        else {
            this.setCurrentLocale(
                this.getLocale() == null ? "en_US" : this.getLocale()
            );
        }
        // Init TimeZone
        if(this.getSettings().getProperty(UserSettings.TIMEZONE_NAME) != null) {
            this.setCurrentTimeZone(
                this.getSettings().getProperty(UserSettings.TIMEZONE_NAME)
            );
        }
        else {
            this.setCurrentTimeZone(
                this.getTimezone() == null ? TimeZone.getDefault().getID() : this.getTimezone()
            );
        }
        // Init Perspective
        if(this.getSettings().getProperty(UserSettings.PERSPECTIVE_ID) != null) {
            this.setCurrentPerspective(
                Integer.valueOf(this.getSettings().getProperty(UserSettings.PERSPECTIVE_ID))
            );
        }
        else {
            this.setCurrentPerspective(0);
        }
        // Init Workspace
        if(this.getSettings().getProperty(UserSettings.WORKSPACE_ID) != null) {
            this.setCurrentWorkspace(
                this.getSettings().getProperty(UserSettings.WORKSPACE_ID)
            );
        }
        else {
            this.setCurrentWorkspace(DEFAULT_WORKSPACE);
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
    private Path mapIdentityAsPattern(
        String pattern
    ) {
        return this.mapIdentity(
            pattern,
            true
        );
    }

    //-------------------------------------------------------------------------
    private Path mapIdentity(
    	String pattern,
    	boolean asPattern
    ) {
    	String principalName = this.currentUserRole.indexOf("@") >= 0 ? 
    		this.currentUserRole.substring(0, this.currentUserRole.indexOf("@")) : 
    			this.currentUserRole;
    	List<String> principalChain = PersistenceManagers.toPrincipalChain(principalName);
    	return this.mapIdentity(
    		pattern, 
    		principalChain.get(0), 
    		asPattern
    	);
    }
    
    //-------------------------------------------------------------------------
    private Path mapIdentity(
        String pattern,
        String principalName,
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
                    mapped += principalName;
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
        String[] rootObjectIdentities = this.getRootObjectIdentities();
        for(
            int i = 0; 
            i < rootObjectIdentities.length; 
            i++
        ) {
            Path mappedIdentity = this.mapIdentity(rootObjectIdentities[i], false);
            try {
                rootObjects.add(
                    pmData.getObjectById(mappedIdentity)
                );
            }
            catch(Exception e) {
                SysLog.info("Can not get root object", "object.xri=" + mappedIdentity + "; message=" + e.getMessage());
                SysLog.detail(e.getMessage(), e.getCause());              
            }
        }
        this.rootObjects = (RefObject_1_0[])rootObjects.toArray(new RefObject_1_0[rootObjects.size()]);      
        this.rootObjectActions.clear();
    }
  
    //-------------------------------------------------------------------------
    private void loadUserHome(
    ) {
        this.userHome = null;      
        try {
        	String userHomeIdentity = this.getUserHomeIdentity();
            if(userHomeIdentity != null) {
                Path mappedIdentity = this.mapIdentity(userHomeIdentity, false);
                this.userHome = (RefObject_1_0)pmData.getObjectById(mappedIdentity);
            }
        }
        catch(Exception e) {
        	SysLog.warning("can not get user home object. Skipping");
        }      
    }
  
    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private void loadQuickAccessors(
    ) {
        Map<String,QuickAccessor> quickAccessors = new TreeMap<String,QuickAccessor>();    
        String quickAccessorsReference = this.getQuickAccessorsReference();
        if(quickAccessorsReference != null) {
            List<Path> quickAccessorsReferences = new ArrayList<Path>();
            // Quick accessors of segment admin
            quickAccessorsReferences.add(
                this.mapIdentity(
                    quickAccessorsReference,
                    this.portalExtension.getAdminPrincipal(this.currentSegment),
                    false
                )
            );
            // Quick accessors of current user
            Path quickAccessorsReferenceIdentity = this.mapIdentity(quickAccessorsReference, false);
            if(!quickAccessorsReferences.contains(quickAccessorsReferenceIdentity)) {
                quickAccessorsReferences.add(quickAccessorsReferenceIdentity);
            }
            int ii = 0;
            for(
                Iterator i = quickAccessorsReferences.iterator(); 
                i.hasNext();
                ii++
            ) {
            	quickAccessorsReferenceIdentity = (Path)i.next();
                RefObject_1_0 parent = null;
                try {
                    parent = (RefObject_1_0)this.pmData.getObjectById(
                    	quickAccessorsReferenceIdentity.getParent()
                    );
                    if(parent != null) {
                      for(
                        Iterator j = ((Collection)parent.refGetValue(quickAccessorsReferenceIdentity.getBase())).iterator();
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
                            try {
                            	target = (RefObject_1_0)quickAccessor.refGetValue("reference");
                            } 
                            catch(Exception e) {}
                            try {
                            	name = (String)quickAccessor.refGetValue("name");
                            } 
                            catch(Exception e) {}
                            try {
                            	description = (String)quickAccessor.refGetValue("description");
                            } 
                            catch(Exception e) {}
                            try {
                            	iconKey = (String)quickAccessor.refGetValue("iconKey");
                            } 
                            catch(Exception e) {}
                            try {
                            	actionType = (Number)quickAccessor.refGetValue("actionType");                            
                            } 
                            catch(Exception e) {}
                            try {
                            	actionName = (String)quickAccessor.refGetValue("actionName");
                            } 
                            catch(Exception e) {}
                            try {
                            	actionParams = (List<String>) quickAccessor.refGetValue("actionParam");
                            } 
                            catch(Exception e) {}
                            if(name == null) {
                                name = new ObjectReference(target, this).getTitle();
                            }           
                            int matchingRootObjectIdentity = -1;
                            String[] rootObjectIdentities = this.getRootObjectIdentities();
                            if(target != null) {
                                for(int k = 0; k < rootObjectIdentities.length; k++) {
                                    Path rootObjectPattern = this.mapIdentityAsPattern(rootObjectIdentities[k]);
                                    if(target.refGetPath().isLike(rootObjectPattern)) {
                                        matchingRootObjectIdentity = k;
                                        break;
                                    }                                    
                                }
                            }
                            quickAccessors.put(
                                ii + ":" + name + ":" + quickAccessor.refMofId(), // Order accessors by (parent, name, accessor)
                                new QuickAccessor(
                                    matchingRootObjectIdentity >= 0 ? 
                                    	this.mapIdentity(rootObjectIdentities[matchingRootObjectIdentity], false) : 
                                    	target == null ? null : target.refGetPath(),
                                    name,
                                    description == null ? name : description,
                                    iconKey,
                                    actionType,
                                    actionName,
                                    actionParams
                                )
                            );
                        }
                        catch(Exception e) {
                          ServiceException e0 = new ServiceException(e);
                          SysLog.info("Can not get quick accessors", e.getMessage());
                          SysLog.detail(e0.getMessage(), e0.getCause());
                        }
                      }
                  }
                }
                catch(Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    SysLog.warning("Can not get quick accessor container", e.getMessage());
                    SysLog.detail(e0.getMessage(), e0.getCause());     
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
            PersistenceManager pm = this.getNewPmData();
            try {                  
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                if(this.currentSettings == null) {
                	this.currentSettings = new Properties();
                }
                this.currentSettings.store(
                    bs,
                    "Settings of user " + this.userHome.refMofId() 
                );
                bs.close();                
                RefObject_1_0 userHome = (RefObject_1_0)pm.getObjectById(this.userHome.refGetPath());
                pm.currentTransaction().begin();
                userHome.refSetValue(
                    "settings",
                    bs.toString("UTF-8")
                );
                pm.currentTransaction().commit();
            }
            catch(Exception e) {
            	SysLog.warning("Unable to store user settings for " + this.userHome.refMofId() + ". Ignoring.");
            	new ServiceException(e).log();
                try {
                    pm.currentTransaction().rollback();
                } 
                catch(Exception e0) {}
            }
            finally {
            	if(pm != null) {
            		pm.close();
            	}
            }
        }
    }
  
    //-------------------------------------------------------------------------
    private void loadSettings(
    ) {
        this.currentSettings = new Properties();
        if(this.userHome != null) {
            try {
                this.currentSettings = new Properties();
                if(this.userHome.refGetValue("settings") != null) {
                    this.currentSettings.load(
                        new ByteArrayInputStream(
                            ((String)this.userHome.refGetValue("settings")).getBytes("UTF-8")
                        )
                    );
                }
            }
            catch(Exception e) {
            	SysLog.warning("can not get settings from user home. Init with empty settings", e);              
            }
        }
    }

    //-------------------------------------------------------------------------
    public RefObject_1_0[] getRootObject(
    ) {
        return this.rootObjects;
    }

    //-------------------------------------------------------------------------
    public Action[] getRootObjectActions(
    ) {
        if(this.rootObjectActions.get(this.currentPerspective) == null) {
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
                    String label = "-";
                    try {
                        label = this.getLabel(rootObjectClass);
                    } 
                    catch(Exception e) {}
                    boolean isEnabled = label != null;
                    String iconKey = WebKeys.ICON_MISSING;
                    try {
                        iconKey =  this.getIconKey(rootObjectClass);
                    }
                    catch(ServiceException e) {
                    	SysLog.detail("can not get icon key", rootObjectClass);
                    	SysLog.detail(e.getMessage(), e.getCause());
                    }
                    // EVENT_SELECT_OBJECT action for root object
                    actions.add(
                        new Action(
                            SelectObjectAction.EVENT_ID,
                            new Action.Parameter[]{
                                new Action.Parameter(Action.PARAMETER_OBJECTXRI, rootObject[i].refMofId())
                            },
                            // append qualifier in case there are root object labels with same label
                            labels.contains(label) ? 
                                label + " (" + new Path(rootObject[i].refMofId()).get(4) + ")" : 
                                label,
                            iconKey,
                            isEnabled
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
                                            	!tab.getTitle().isEmpty() ? 
                                            		tab.getTitle().get(0) : 
                                            			"NA";
                                    if(title != null) {
                                        if(title.startsWith("\u00BB")) title = title.substring(1);
                                        if(title.length() > 1) {
                                            // EVENT_SELECT_OBJECT for reference jj
                                            actions.add(
                                                new Action(
                                                    SelectObjectAction.EVENT_ID,
                                                    new Action.Parameter[]{
                                                        new Action.Parameter(Action.PARAMETER_PANE, "" + paneIndex), 
                                                        new Action.Parameter(Action.PARAMETER_REFERENCE, "" + referenceIndex),
                                                        new Action.Parameter(Action.PARAMETER_OBJECTXRI, rootObject[i].refMofId())
                                                    },
                                                    title,
                                                    tab.getIconKey(),
                                                    isEnabled
                                                )
                                            );
                                        }
                                    }
                                }
                                paneIndex++;
                            }
                        }
                    }
                    catch(ServiceException e) {
                    	SysLog.detail("can not get inspector", rootObjectClass);
                    	SysLog.detail(e.getMessage(), e.getCause());
                    }
                }
            }
            this.rootObjectActions.put(
                this.currentPerspective,
                (Action[])actions.toArray(new Action[actions.size()])
            );
        }
        return this.rootObjectActions.get(this.currentPerspective);
    }
    
    //-------------------------------------------------------------------------
    public UiContext getUiContext(
    ) {
        return this.getControlFactory().getUiContext();
    }

    //-------------------------------------------------------------------------
    public HtmlEncoder_1_0 getHtmlEncoder(
    ) {
        return this.getHttpEncoder();
    }

    //-------------------------------------------------------------------------
    public org.openmdx.ui1.jmi1.Inspector getInspector(
        String forClass
    ) throws ServiceException {
        return this.getUiContext().getInspector(
            forClass,
            this.currentPerspective
        );
    }

    //-------------------------------------------------------------------------
    public org.openmdx.ui1.jmi1.AssertableInspector getAssertableInspector(
        String forClass
    ) throws ServiceException {
        return this.getUiContext().getAssertableInspector(
            forClass,
            this.currentPerspective
        );
    }

    //-------------------------------------------------------------------------
    public String getLabel(
        String forClass
    ) throws ServiceException {
        return this.getUiContext().getLabel(
            forClass,
            this.getCurrentLocaleAsIndex(),
            this.currentPerspective            
        );
    }

    //-------------------------------------------------------------------------
    public String getIconKey(
        String forClass
    ) throws ServiceException {
        return this.getUiContext().getIconKey(
            forClass,
            this.currentPerspective
        );
    }
  
    //-------------------------------------------------------------------------
    public String getBackColor(
        String forClass
    ) throws ServiceException {
        return this.getUiContext().getBackColor(
            forClass,
            this.currentPerspective
        );
    }
  
    //-------------------------------------------------------------------------
    public String getColor(
        String forClass
    ) throws ServiceException {
        return this.getUiContext().getColor(
            forClass,
            this.currentPerspective
        );
    }
  
    //-------------------------------------------------------------------------
    public Filters getFilters(
        String forReference
    ) {
        return (Filters)this.getFilters().get(forReference);
    }
  
    //-------------------------------------------------------------------------
    public String getLayout(
        String forClass,
        boolean forEditing
    ) {      
        return this.getLayoutFactory().getLayout(
            this.currentLocaleAsString,
            forClass,
            forEditing
        );
    }
  
    //-------------------------------------------------------------------------
    public Texts_1_0 getTexts(
    ) {
        return this.getTextsFactory().getTexts(this.currentLocaleAsString);
    }

    //-------------------------------------------------------------------------
    public TextsFactory getTextsFactory(
    ) {
        return this.getControlFactory().getTextsFactory();
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
        else if(this.currentTimeZone == null) {
        	this.currentTimeZone = "UTC";
        }
        // Save in settings
        this.getSettings().setProperty(
            UserSettings.TIMEZONE_NAME,
            this.currentTimeZone
        );
        // actions are locale-specific. Invalidate
        this.rootObjectActions.clear();
    }
  
    //-------------------------------------------------------------------------
    public synchronized void setCurrentLocale(
        String locale
    ) {
        List locales = Arrays.asList(this.getTextsFactory().getLocale());
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
            UserSettings.LOCALE_NAME,
            this.currentLocaleAsString
        );
        // actions are locale-specific. Invalidate
        this.rootObjectActions.clear();
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
    public String getCurrentUserRole(
    ) {
        return this.currentUserRole;
    }
  
    //-------------------------------------------------------------------------
    public List<String> getUserRoles(
    ) {
        return this.currentUserRoles;
    }

    //-------------------------------------------------------------------------
    public ViewPort.Type getCurrentViewPortType(
    ) {
    	return this.currentViewPortType;
    }

    //-------------------------------------------------------------------------
    public void setCurrentViewPortType(
    	ViewPort.Type viewPortType
    ) {
    	this.currentViewPortType = viewPortType;
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
                } 
                catch(Exception e) {}
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
        return 
        	this.getTempDirectory().getPath() + 
        	File.separator + this.getTempFilePrefix() + "-" + fileName + extension;
    }
  
    //-------------------------------------------------------------------------
    private PersistenceManager createPersistenceManager(
        PersistenceManagerFactory pmf,
        String userRole
    ) throws ServiceException {
        // Role has format <principal name>@<segment>
        userRole = userRole == null || !this.currentUserRoles.contains(userRole) ? this.currentUserRole : userRole;
        String principalName = userRole.indexOf("@") >= 0 ? userRole.substring(0, userRole.indexOf("@")) : userRole;
        List<String> principalChain = new ArrayList<String>(PersistenceManagers.toPrincipalChain(principalName));
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
        } 
        catch(Exception e) {}
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
            this.getPmfData(),
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
            this.getPmfData(),
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
            } 
            catch(Exception e) {}
        }
        this.pmData = null;
        if(this.pmControl != null) {
            try {
                this.pmControl.close();
            } 
            catch(Exception e) {}
        }
        this.pmControl = null;      
    }
  
    //-------------------------------------------------------------------------
    private PersistenceManager getPmData(
    ) {
        return this.pmData;
    }

    //-------------------------------------------------------------------------
    public PersistenceManager getNewPmData(
    ) {
    	return this.pmData.getPersistenceManagerFactory().getPersistenceManager(
    		UserObjects.getPrincipalChain(this.pmData).toString(),
    		null
    	);
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
    public Properties getSettings(
    ) {
        return this.currentSettings;
    }

    //-------------------------------------------------------------------------
    public Properties getUserSettings(
    	Path userHomeIdentity
    ) {
        Properties settings = new Properties();
		try {
			RefObject_1_0 userHomeAdmin = (RefObject_1_0)this.getPmData().getObjectById(userHomeIdentity);
	        if(userHomeAdmin.refGetValue("settings") != null) {
	            settings.load(
	                new ByteArrayInputStream(
	                    ((String)userHomeAdmin.refGetValue("settings")).getBytes("UTF-8")
	                )
	            );
	        }
		} catch(Exception e) {}
		return settings;
    }
    
    //-------------------------------------------------------------------------
    public Path getObjectRetrievalIdentity(
        RefObject_1_0 object
    ) {
        Path retrievalIdentity = object.refGetPath();
        Path path = object.refGetPath();
        for(
            Iterator i = this.getRetrieveByPathPatterns().iterator();
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
        this.currentSettings.setProperty(
            "Panel." + panelName + ".State",
            "" + panelState          
        );
    }
  
    //-------------------------------------------------------------------------
    public int getPanelState(
        String panelName
    ) {
        String panelState = this.currentSettings.getProperty(
            "Panel." + panelName + ".State"
        );
        return panelState == null ? 
            0 : 
            Integer.parseInt(panelState);
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
        return this.getFilterValuePatterns();
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
                SysLog.warning(e0.getMessage(), e0.getCause());
                return null;
            }
        }
        else {
            return null;
        }
    }
  
    //-------------------------------------------------------------------------
    public Path getUserHomeIdentityAsPath(
    ) {
        return this.getUserHomeIdentity() == null ?
        	null : 
        		this.mapIdentity(this.getUserHomeIdentity(), false);
    }

    //-------------------------------------------------------------------------
    public String getCurrentSegment(
    ) {
        return this.currentSegment;
    }

    //-------------------------------------------------------------------------
    public int getCurrentPerspective(
    ) {
        return this.currentPerspective;
    }
    
    //-------------------------------------------------------------------------
    public synchronized void setCurrentPerspective(
        int perspective
    ) {
        this.currentPerspective = perspective;
        // Save in settings
        this.getSettings().setProperty(
            UserSettings.PERSPECTIVE_ID,
            Integer.toString(this.currentPerspective)
        );        
    }
    
    //-------------------------------------------------------------------------
    public String getCurrentWorkspace(
    ) {
        return this.currentWorkspace;
    }
    
    //-------------------------------------------------------------------------
    public void setCurrentWorkspace(
        String workspace
    ) {
        this.currentWorkspace = workspace;
        // Save in settings
        this.getSettings().setProperty(
            UserSettings.WORKSPACE_ID,
            this.currentWorkspace
        );        
    }
    
    //-------------------------------------------------------------------------
    public Control createControl(
        String id,
        Class<?> controlClass,
        Object... parameter
    ) throws ServiceException {
        return this.getControlFactory().createControl(
            id,
            this.getCurrentLocaleAsString(),
            this.getCurrentLocaleAsIndex(),
            controlClass
        );
    }
    
    //-------------------------------------------------------------------------
    public ShowInspectorControl createShowInspectorControl(
        String id,
        String forClass
    ) throws ServiceException {
        return this.getControlFactory().createShowInspectorControl(
            id,
            this.getCurrentPerspective(),
            this.getCurrentLocaleAsString(),
            this.getCurrentLocaleAsIndex(),
            this.getInspector(forClass),
            forClass
        );
    }
    
    //-------------------------------------------------------------------------
    public EditInspectorControl createEditInspectorControl(
        String id,
        String forClass
    ) throws ServiceException {
        return this.getControlFactory().createEditInspectorControl(
            id,
            this.getCurrentPerspective(),
            this.getCurrentLocaleAsString(),
            this.getCurrentLocaleAsIndex(),
            this.getInspector(forClass),
            forClass
        );
    }

    //-------------------------------------------------------------------------
    public AttributeValue createAttributeValue(
        org.openmdx.ui1.jmi1.ValuedField customizedField,
        Object object        
    ) throws ServiceException {
        return this.getControlFactory().getAttributeValueFactory().getAttributeValue(
            customizedField,
            object,
            this
        );
    }

    //-------------------------------------------------------------------------
    public org.openmdx.ui1.jmi1.Element getUiElement(
        String id
    ) throws ServiceException {
        return this.getUiContext().getUiSegment(this.currentPerspective).getElement(
            id
        );
    }
    
    //-------------------------------------------------------------------------
    public org.openmdx.ui1.jmi1.ElementDefinition getUiElementDefinition(
        String qualifiedName
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.ElementDefinition uiElementDefinition = 
            this.getUiContext().getUiSegment(this.currentPerspective).getElementDefinition(
                qualifiedName
            );
        if(uiElementDefinition == null) {
            uiElementDefinition = this.getUiContext().getUiSegment(UiContext.MAIN_PERSPECTIVE).getElementDefinition(
                qualifiedName
            );
        }
        return uiElementDefinition;
    }
    
    //-------------------------------------------------------------------------
    public org.openmdx.ui1.jmi1.FormDefinition getUiFormDefinition(
        String name
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.FormDefinition uiFormDefinition = 
            this.getUiContext().getUiSegment(this.currentPerspective).getFormDefinition(
                name
            );
        if(uiFormDefinition == null) {
            uiFormDefinition = this.getUiContext().getUiSegment(UiContext.MAIN_PERSPECTIVE).getFormDefinition(
                name
            );
        }
        return uiFormDefinition;
    }
    
    //-------------------------------------------------------------------------
    public org.openmdx.ui1.jmi1.FeatureDefinition getFeatureDefinition(
        String qualifiedFeatureName
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.FeatureDefinition featureDefinition = 
            this.getUiContext().getUiSegment(this.currentPerspective).getFeatureDefinition(
                qualifiedFeatureName
            );
        if(featureDefinition == null) {
            featureDefinition = this.getUiContext().getUiSegment(UiContext.MAIN_PERSPECTIVE).getFeatureDefinition(
                qualifiedFeatureName
            );            
        }
        return featureDefinition;
    }

    //-------------------------------------------------------------------------
    public String getWildcardFilterValue(
        String filterValue
    ) {
        if(!filterValue.endsWith(this.getFilterValuePattern()[2])) {
            filterValue += this.getFilterValuePattern()[2];
        }
        if(
            !filterValue.startsWith(this.getFilterValuePattern()[0]) ||
            (this.getFilterValuePattern()[0].length() == 0)
        ) {
            if(!filterValue.startsWith(this.getFilterValuePattern()[1])) {
                filterValue = this.getFilterValuePattern()[1] + filterValue;
            }
            filterValue = this.getFilterValuePattern()[0] + filterValue;
        }  
        return filterValue.replace("%", ".*");
    }

    //-------------------------------------------------------------------------
    // Configuration
    //-------------------------------------------------------------------------
	public String getApplicationName(
	) {
		return this.applicationName;
	}

	public void setApplicationName(
		String applicationName
	) {
		this.applicationName = applicationName;
	}

	public String getLocale(
	) {
		return this.locale;
	}

	public void setLocale(
		String locale
	) {
		this.locale = locale;
	}

	public String getTimezone(
	) {
		return this.timezone;
	}

	public void setTimezone(
		String timezone
	) {
		this.timezone = timezone;
	}

	public ControlFactory getControlFactory(
	) {
		return this.controlFactory;
	}

	public void setControlFactory(
		ControlFactory controlFactory
	) {
		this.controlFactory = controlFactory;
	}

	public String getSessionId(
	) {
		return this.sessionId;
	}

	public void setSessionId(
		String sessionId
	) {
		this.sessionId = sessionId;
	}

	public String getLoginPrincipal(
	) {
		return this.loginPrincipal;
	}

	public void setLoginPrincipal(
		String loginPrincipal
	) {
		this.loginPrincipal = loginPrincipal;
	}

	public Path getLoginRealmIdentity(
	) {
		return this.loginRealmIdentity;
	}

	public void setLoginRealmIdentity(
		Path loginRealmIdentity
	) {
		this.loginRealmIdentity = loginRealmIdentity;
	}

	public List getRetrieveByPathPatterns(
	) {
		return this.retrieveByPathPatterns;
	}

	public void setRetrieveByPathPatterns(
		List retrieveByPathPatterns
	) {
		this.retrieveByPathPatterns = retrieveByPathPatterns;
	}

	public String getUserHomeIdentity() {
		return this.userHomeIdentity;
	}

	public void setUserHomeIdentity(
		String userHomeIdentity
	) {
		this.userHomeIdentity = userHomeIdentity;
	}

	public String[] getRootObjectIdentities(
	) {
		return this.rootObjectIdentities;
	}

	public void setRootObjectIdentities(
		String[] rootObjectIdentities
	) {
		this.rootObjectIdentities = rootObjectIdentities;
	}

	public PortalExtension_1_0 getPortalExtension(
	) {
		return this.portalExtension;
	}

	public void setPortalExtension(
		PortalExtension_1_0 portalExtension
	) {
		this.portalExtension = portalExtension;
	}

	public HtmlEncoder_1_0 getHttpEncoder(
	) {
		return this.httpEncoder;
	}

	public void setHttpEncoder(
		HtmlEncoder_1_0 httpEncoder
	) {
		this.httpEncoder = httpEncoder;
	}

	public Map getFilters() {
		return this.filters;
	}

	public void setFilters(
		Map filters
	) {
		this.filters = filters;
	}

	public Codes getCodes(
	) {
		return this.codes;
	}

	public void setCodes(
		Codes codes
	) {
		this.codes = codes;
	}

	public LayoutFactory getLayoutFactory(
	) {
		return this.layoutFactory;
	}

	public void setLayoutFactory(
		LayoutFactory layoutFactory
	) {
		this.layoutFactory = layoutFactory;
	}

	public File getTempDirectory(
	) {
		return this.tempDirectory;
	}

	public void setTempDirectory(
		File tempDirectory
	) {
		this.tempDirectory = tempDirectory;
	}

	public String getTempFilePrefix(
	) {
		return this.tempFilePrefix;
	}

	public void setTempFilePrefix(
		String tempFilePrefix
	) {
		this.tempFilePrefix = tempFilePrefix;
	}

	public String getQuickAccessorsReference(
	) {
		return this.quickAccessorsReference;
	}

	public void setQuickAccessorsReference(
		String quickAccessorsReference
	) {
		this.quickAccessorsReference = quickAccessorsReference;
	}

	public Map getMimeTypeImpls(
	) {
		return this.mimeTypeImpls;
	}

	public void setMimeTypeImpls(
		Map mimeTypeImpls
	) {
		this.mimeTypeImpls = mimeTypeImpls;
	}

	public String getExceptionDomain(
	) {
		return this.exceptionDomain;
	}

	public void setExceptionDomain(
		String exceptionDomain
	) {
		this.exceptionDomain = exceptionDomain;
	}

	public String getFilterCriteriaField(
	) {
		return this.filterCriteriaField;
	}

	public void setFilterCriteriaField(
		String filterCriteriaField
	) {
		this.filterCriteriaField = filterCriteriaField;
	}

	public String[] getFilterValuePatterns(
	) {
		return this.filterValuePatterns;
	}

	public void setFilterValuePatterns(
		String[] filterValuePatterns
	) {
		this.filterValuePatterns = filterValuePatterns;
	}

	public PersistenceManagerFactory getPmfData(
	) {
		return this.pmfData;
	}

	public void setPmfData(
		PersistenceManagerFactory pmfData
	) {
		this.pmfData = pmfData;
	}

	public Model_1_0 getModel(
	) {
		return this.model;
	}

	public void setModel(
		Model_1_0 model
	) {
		this.model = model;
	}

	public ViewPort.Type getViewPortType(
	) {
		return this.viewPortType;
	}

	public void setViewPortType(
		ViewPort.Type viewPortType
	) {
		this.viewPortType = viewPortType;
	}

	public String getUserRole(
	) {
		return this.userRole;
	}    

	public void setUserRole(
		String userRole
	) {
		this.userRole = userRole;
	}

    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = -8690003081285454886L;
    
    public static final String DEFAULT_WORKSPACE = "W0";
    
    // Config settings
	protected String applicationName;
	protected String locale;
	protected String timezone;
	protected ControlFactory controlFactory;
	protected String sessionId;
	protected ViewPort.Type viewPortType;
	protected String loginPrincipal;
	protected String userRole;
	protected Path loginRealmIdentity;
	protected List retrieveByPathPatterns;
	protected String userHomeIdentity;
	protected String[] rootObjectIdentities;
	protected PortalExtension_1_0 portalExtension;
	protected HtmlEncoder_1_0 httpEncoder;
	protected Map filters;
	protected Codes codes;
	protected LayoutFactory layoutFactory;
	protected File tempDirectory;
	protected String tempFilePrefix;
	protected String quickAccessorsReference;
	protected Map mimeTypeImpls;
	protected String exceptionDomain;
	protected String filterCriteriaField;
	protected String[] filterValuePatterns;
	protected PersistenceManagerFactory pmfData;
	protected Model_1_0 model;

	// Current settings
	protected RefObject_1_0 userHome = null;
	protected RefObject_1_0[] rootObjects = null;
	protected final Map<Integer,Action[]> rootObjectActions = new HashMap<Integer,Action[]>();
	protected final List<String> errorMessages = new ArrayList<String>(); 
	protected QuickAccessor[] quickAccessors = null;
	protected Properties currentSettings = new Properties();
	protected List<String> currentUserRoles;
	protected String currentLoginPrincipal;
	protected String currentUserRole;
	protected PersistenceManager pmData; // package managing data objects
	protected Date pmDataReloadedAt;
	protected PersistenceManager pmControl; // package managing control objects
	protected String currentLocaleAsString = null;
	protected String currentTimeZone = null;
	protected String currentSegment = "Standard";
	protected int currentPerspective = 0;
	protected String currentWorkspace = DEFAULT_WORKSPACE;
	protected ViewPort.Type currentViewPortType = null;
  
}

//--- End of File -----------------------------------------------------------
