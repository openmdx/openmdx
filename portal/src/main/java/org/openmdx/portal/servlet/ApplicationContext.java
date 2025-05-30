/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: ApplicationContext
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
import java.util.logging.Level;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.UserObjects;
import org.openmdx.base.persistence.spi.PersistenceManagers;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.action.SelectObjectAction;
import org.openmdx.portal.servlet.component.LayoutFactory;
import org.openmdx.portal.servlet.wizards.WizardDefinitionFactory;
import org.w3c.time.SystemClock;

/**
 * ApplicationContext holds the state of the current (user) session.
 *
 */
public final class ApplicationContext implements Serializable {
  
	/**
	 * Constructor 
	 *
	 */
	public ApplicationContext(
	) {	
	}

    /**
     * Init application context.
     * @throws ServiceException
     */
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
        if(this.getSettings().getProperty(UserSettings.LOCALE_NAME.getName()) != null) {
            this.setCurrentLocale(
                this.getSettings().getProperty(UserSettings.LOCALE_NAME.getName())
            );
        } else {
            this.setCurrentLocale(
                this.getLocale() == null ? "en_US" : this.getLocale()
            );
        }
        // Init TimeZone
        if(this.getSettings().getProperty(UserSettings.TIMEZONE_NAME.getName()) != null) {
            this.setCurrentTimeZone(
                this.getSettings().getProperty(UserSettings.TIMEZONE_NAME.getName())
            );
        } else {
            this.setCurrentTimeZone(
                this.getTimezone() == null ? TimeZone.getDefault().getID() : this.getTimezone()
            );
        }
        // Init Perspective
        if(this.getSettings().getProperty(UserSettings.PERSPECTIVE_ID.getName()) != null) {
            this.setCurrentPerspective(
                Integer.valueOf(this.getSettings().getProperty(UserSettings.PERSPECTIVE_ID.getName()))
            );
        } else {
            this.setCurrentPerspective(0);
        }
        // Init Workspace
        if(this.getSettings().getProperty(UserSettings.WORKSPACE_ID.getName()) != null) {
            this.setCurrentWorkspace(
                this.getSettings().getProperty(UserSettings.WORKSPACE_ID.getName())
            );
        } else {
            this.setCurrentWorkspace(DEFAULT_WORKSPACE);
        }    
        this.loadQuickAccessors();
        this.loadRootObjects();
    }

    /**
     * Map order list to string.
     * @param order
     * @return
     */
    public static String getOrderAsString(
        List<Integer> order
    ) {
        if(order == null) return "0";
        String orderAsString = "";
        for(int i = 0; i < order.size(); i++) {
            if(i > 0) orderAsString += ":";
            orderAsString += "" + order.get(i);
        }
        return orderAsString;
    }

    /**
     * Map identity to path. Map place holders to :*
     * @param pattern
     * @return
     */
    private Path mapIdentityAsPattern(
        String pattern
    ) {
        return this.mapIdentity(
            pattern,
            true
        );
    }

    /**
     * Map identity to path.
     * @param pattern
     * @param asPattern
     * @return
     */
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
    
    /**
     * Map identity to path and replace place holders.
     * @param pattern
     * @param principalName
     * @param asPattern
     * @return
     */
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

    /**
     * Load root objects.
     */
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
  
    /**
     * Load user home.
     */
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
  
    /**
     * Load quick accessor.
     */
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
                Iterator<Path> i = quickAccessorsReferences.iterator(); 
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
                        Iterator<?> j = ((Collection<?>)parent.refGetValue(quickAccessorsReferenceIdentity.getLastSegment().toClassicRepresentation())).iterator();
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
                            } catch(Exception e) {}
                            try {
                            	name = (String)quickAccessor.refGetValue("name");
                            } catch(Exception e) {}
                            try {
                            	description = (String)quickAccessor.refGetValue("description");
                            } catch(Exception e) {}
                            try {
                            	iconKey = (String)quickAccessor.refGetValue("iconKey");
                            } catch(Exception e) {}
                            try {
                            	actionType = (Number)quickAccessor.refGetValue("actionType");                            
                            } catch(Exception e) {}
                            try {
                            	actionName = (String)quickAccessor.refGetValue("actionName");
                            } 
                            catch(Exception e) {}
                            try {
                            	@SuppressWarnings("unchecked")
								List<String> params = (List<String>)quickAccessor.refGetValue("actionParam");
                            	actionParams = params;
                            } catch(Exception ignore) {
                    			SysLog.trace("Exception ignored", ignore);
                            }
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

    /**
     * Get quick accessors.
     * @return
     */
    public QuickAccessor[] getQuickAccessors(
    ) {
        return this.quickAccessors;
    }

    /**
     * Save settings.
     * @param logoff
     */
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

    /**
     * Store settings.
     */
    private void storeSettings(
    ) {
        if(this.userHome != null) {
            PersistenceManager pm = this.getNewPmData();
            try {                  
                try(ByteArrayOutputStream bs = new ByteArrayOutputStream()){
                    if(this.currentSettings == null) {
                    	this.currentSettings = new Properties();
                    }
                    this.currentSettings.store(
                        bs,
                        "Settings of user " + this.userHome.refMofId() 
                    );
                    RefObject_1_0 userHome = (RefObject_1_0)pm.getObjectById(this.userHome.refGetPath());
                    pm.currentTransaction().begin();
                    userHome.refSetValue(
                        "settings",
                        bs.toString("UTF-8")
                    );
                }
                pm.currentTransaction().commit();
            }
            catch(Exception e) {
            	SysLog.warning("Unable to store user settings for " + this.userHome.refMofId() + ". Ignoring.");
                Throwables.log(e);
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
  
    /**
     * Load settings.
     */
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

    /**
     * Get root objects.
     * @return
     */
    public RefObject_1_0[] getRootObject(
    ) {
        return this.rootObjects;
    }

    /**
     * Get actions for selecting the root objects (SelectObjectAction.EVENT_ID)
     * @return
     */
    public Action[] getRootObjectActions(
    ) {
        if(this.rootObjectActions.get(this.currentPerspective) == null) {
            RefObject_1_0[] rootObject = this.getRootObject();
            List<Action> actions = new ArrayList<Action>();
            Set<String> labels = new HashSet<String>();
            Set<String> refMofIds = new HashSet<String>();
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
                    } catch(Exception ignore) {
            			SysLog.trace("Exception ignored", ignore);
                    }
                    boolean isEnabled = label != null;
                    String iconKey = WebKeys.ICON_MISSING;
                    try {
                        iconKey =  this.getIconKey(rootObjectClass);
                    } catch(ServiceException e) {
                    	SysLog.detail("can not get icon key", rootObjectClass);
                    	SysLog.detail(e.getMessage(), e.getCause());
                    }
                    // EVENT_SELECT_OBJECT action for root object
                    actions.add(
                        new Action(
                            SelectObjectAction.EVENT_ID,
                            new Action.Parameter[]{
                                new Action.Parameter(Action.PARAMETER_OBJECTXRI, rootObject[i].refGetPath().toXRI())
                            },
                            // append qualifier in case there are root object labels with same label
                            labels.contains(label) ? 
                                label + " (" + new Path(rootObject[i].refMofId()).getSegment(4).toClassicRepresentation() + ")" : 
                                label,
                            iconKey,
                            isEnabled
                        )
                    );
                    labels.add(label);
                    refMofIds.add(rootObject[i].refMofId());
                }
            }
            this.rootObjectActions.put(
                this.currentPerspective,
                (Action[])actions.toArray(new Action[actions.size()])
            );
        }
        return this.rootObjectActions.get(this.currentPerspective);
    }
    
    /**
     * Get current ui context.
     * 
     * @return
     */
    public UiContext getUiContext(
    ) {
        return this.uiContext;
    }

    /**
     * Set the uiContext.
     * 
     * @param uiContext
     */
    public void setUiContext(
    	UiContext uiContext
    ) {
    	this.uiContext = uiContext;
    }

    /**
     * Get HTML encoder.
     * @return
     */
    public HtmlEncoder_1_0 getHtmlEncoder(
    ) {
        return this.getHttpEncoder();
    }

    /**
     * Get inspector for given class.
     * @param forClass
     * @return
     * @throws ServiceException
     */
    public org.openmdx.ui1.jmi1.Inspector getInspector(
        String forClass
    ) throws ServiceException {
        return this.getUiContext().getInspector(
            forClass,
            this.currentPerspective
        );
    }

    /**
     * Get inspector for given class.
     * @param forClass
     * @return
     * @throws ServiceException
     */
    public org.openmdx.ui1.jmi1.AssertableInspector getAssertableInspector(
        String forClass
    ) throws ServiceException {
        return this.getUiContext().getAssertableInspector(
            forClass,
            this.currentPerspective
        );
    }

    /**
     * Get label for given class and current locale.
     * @param forClass
     * @return
     * @throws ServiceException
     */
    public String getLabel(
        String forClass
    ) throws ServiceException {
        return this.getUiContext().getLabel(
            forClass,
            this.getCurrentLocaleAsIndex(),
            this.currentPerspective            
        );
    }

    /**
     * Get icon key for given class.
     * @param forClass
     * @return
     * @throws ServiceException
     */
    public String getIconKey(
        String forClass
    ) throws ServiceException {
        return this.getUiContext().getIconKey(
            forClass,
            this.currentPerspective
        );
    }
  
    /**
     * Get back color for given class.
     * @param forClass
     * @return
     * @throws ServiceException
     */
    public String getBackColor(
        String forClass
    ) throws ServiceException {
        return this.getUiContext().getBackColor(
            forClass,
            this.currentPerspective
        );
    }
  
    /**
     * Get color for given class.
     * @param forClass
     * @return
     * @throws ServiceException
     */
    public String getColor(
        String forClass
    ) throws ServiceException {
        return this.getUiContext().getColor(
            forClass,
            this.currentPerspective
        );
    }
  
    /**
     * Get filters for given reference.
     * @param forReference
     * @return
     */
    public Filters getFilters(
        String forReference
    ) {
        return (Filters)this.getFilters().get(forReference);
    }
  
    /**
     * Get layout id for given class.
     * @param forClass
     * @param forEditing
     * @return
     */
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
  
    /**
     * Get text resources.
     * @return
     */
    public Texts_1_0 getTexts(
    ) {
        return this.getTextsFactory().getTextsBundle(this.currentLocaleAsString);
    }

	/**
	 * @param textsFactory the textsFactory to set
	 */
	public void setTextsFactory(
		Texts textsFactory
	) {
		this.textsFactory = textsFactory;
	}

	/**
	 * @return the textsFactory
	 */
	public Texts getTextsFactory(
	) {
		return this.textsFactory;
	}

    /**
     * Get time zone configured in user settings.
     * @return
     */
    public String getCurrentTimeZone(
    ) {
        return this.currentTimeZone;
    }
  
    /**
     * Set current time zone and store in user settings.
     * @param id
     */
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
            UserSettings.TIMEZONE_NAME.getName(),
            this.currentTimeZone
        );
        // actions are locale-specific. Invalidate
        this.rootObjectActions.clear();
    }
  
    /**
     * Set locale and store in user settings.
     * @param locale
     */
    public synchronized void setCurrentLocale(
        String locale
    ) {
        List<String> locales = Arrays.asList(this.getTextsFactory().getLocale());
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
            UserSettings.LOCALE_NAME.getName(),
            this.currentLocaleAsString
        );
        // actions are locale-specific. Invalidate
        this.rootObjectActions.clear();
    }
  
    /**
     * Get curren locale.
     * @return
     */
    public short getCurrentLocaleAsIndex(
    ) {
        return this.getTexts().getLocaleIndex();
    }

    /**
     * Get current locale.
     * @return
     */
    public String getCurrentLocaleAsString(
    ) {
        return this.currentLocaleAsString;
    }
  
    /**
     * Get current locale.
     * @return
     */
    public Locale getCurrentLocale(
    ) {
        String locale = this.getCurrentLocaleAsString();
        return new Locale(
            locale.substring(0, 2), 
            locale.substring(locale.indexOf("_") + 1)
        );      
    }

    /**
     * Get current user role.
     * @return
     */
    public String getCurrentUserRole(
    ) {
        return this.currentUserRole;
    }
  
    /**
     * Get available user roles.
     * @return
     */
    public List<String> getUserRoles(
    ) {
        return this.currentUserRoles;
    }

    /**
     * Get current view port type.
     * @return
     */
    public ViewPort.Type getCurrentViewPortType(
    ) {
    	return this.currentViewPortType;
    }

    /**
     * Set current view port type.
     * @param viewPortType
     */
    public void setCurrentViewPortType(
    	ViewPort.Type viewPortType
    ) {
    	this.currentViewPortType = viewPortType;
    }
    
    /**
     * Add error message to list of error messages.
     * @param message
     * @param parameters
     */
    public void addErrorMessage(
        String message,
        String[] parameters
    ) {
        String preparedMessage = "";
        int i = 0;
        while(i < message.length()) {
            if((i <= message.length()-4) && "${".equals(message.substring(i,i+2))) {
                short index = Short.parseShort(message.substring(i + 2, i + 3));
                try {
                    preparedMessage += parameters[index];
                } 
                catch(Exception ignore) {
        			SysLog.trace("Exception ignored", ignore);
                }
                i += 4;
            }
            else {
                preparedMessage += message.charAt(i);
                i++;
            }
        }
        this.errorMessages.add(preparedMessage);
    }
  
    /**
     * Get list of current error messages.
     * @return
     */
    public List<String> getErrorMessages(
    ) {
        return this.errorMessages;
    }
  
    /**
     * Get temp file name.
     * @param name
     * @param extension
     * @return
     */
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
  
    /**
     * Get new persistence manager for given user role.
     * @param pmf
     * @param userRole
     * @return
     * @throws ServiceException
     */
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

    /**
     * Evicts the data persistence manager and rolls back active transactions. 
     * The reset is typically invoked after an operation invocation with isQuery=false.
     *
     * @throws ServiceException
     */
    public void resetPmData(
    ) throws ServiceException {
        this.pmData.evictAll();
        try {
            this.pmData.currentTransaction().rollback();
        } catch(Exception ignore) {
			SysLog.trace("Exception ignored", ignore);
        }
        this.pmDataReloadedAt = SystemClock.getInstance().now();
    }

    /**
     * Create the data persistence manager. This data persistence manager is used
     * to retrieve objects for the attributes and grid panes. This create is invoked
     * after a logon or role change. As a consequence all views must be destroyed.
     * @return
     * @throws ServiceException
     */
    public PersistenceManager createPmData(
    ) throws ServiceException {
        this.pmData = this.createPersistenceManager(
            this.getPmfData(),
            this.currentUserRole
        );
        this.pmDataReloadedAt = SystemClock.getInstance().now();
        return this.pmData;
    }

    /**
     * Get persistence manager for accessing ui repository and common ui objects.
     * @return
     * @throws ServiceException
     */
    public PersistenceManager createPmControl(
    ) throws ServiceException {
        return this.createPmControl(true);
    }

    /**
     * Refresh the control package. This package is used to retrieve control
     * objects, i.e. favorites, user settings, root objects. This refresh is 
     * typically initiated explicitly by the user.
     * @param loadObjects if true control objects such quick accessors, user
     *        home only refresh the controlPkg. Do not load
     *        any objects such as favorites, user settings, root objects.
     * @param loadControlObjects
     * @return
     * @throws ServiceException
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

    /**
     * Close application context and all associated resources.
     * 
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
  
    /**
     * Get current persistence manager for accessing user data.
     * @return
     */
    private PersistenceManager getPmData(
    ) {
        return this.pmData;
    }

    /**
     * Create new persistence manager for accessing user data.
     * @return
     */
    public PersistenceManager getNewPmData(
    ) {
    	return this.pmData.getPersistenceManagerFactory().getPersistenceManager(
    		UserObjects.getPrincipalChain(this.pmData).toString(),
    		null
    	);
    }
    
    /**
     * Get time when pm data was last reloaded.
     * @return
     */
    public #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif getPmDataReloadedAt(
    ) {
        return this.pmDataReloadedAt;
    }

    /**
     * Get persistence managet for accessing portal control objects.
     * @return
     */
    public PersistenceManager getPmControl(
    ) {
        return this.pmControl;
    }

    /**
     * Get current user settings.
     * @return
     */
    public Properties getSettings(
    ) {
        return this.currentSettings;
    }

    /**
     * Get user settings of given user.
     * @param userHomeIdentity
     * @return
     */
    public Properties getUserSettings(
    	Path userHomeIdentity
    ) {
        Properties settings = new Properties();
		try {
			RefObject_1_0 userHome = (RefObject_1_0)this.getPmData().getObjectById(userHomeIdentity);
	        if(userHome.refGetValue("settings") != null) {
	            settings.load(
	                new ByteArrayInputStream(
	                    ((String)userHome.refGetValue("settings")).getBytes("UTF-8")
	                )
	            );
	        }
		} catch(Exception e) {}
		return settings;
    }

    /**
     * Get identity under which the given object can be retrieved.
     * @param object
     * @return
     */
    public Path getObjectRetrievalIdentity(
        RefObject_1_0 object
    ) {
        Path retrievalIdentity = object.refGetPath();
        Path path = object.refGetPath();
        for(
            Iterator<Path> i = this.getRetrieveByPathPatterns().iterator();
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
  
    /**
     * Set state for given panel.
     * @param panelName
     * @param panelState
     */
    public void setPanelState(
        String panelName,
        int panelState
    ) {
        this.currentSettings.setProperty(
            "Panel." + panelName + ".State",
            "" + panelState          
        );
    }
  
    /**
     * Get state of given panel.
     * @param panelName
     * @return
     */
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

    /**
     * The like condition patterns define prefix and suffix patterns which are
     * added to filter values entered by the user. E.g. used in grid for completing
     * search value. 
     * @return filter value patterns. 0: pattern prefix 1, 1: pattern prefix 2, 2: suffix
     * @return
     */
    public String[] getFilterValuePattern(
    ) {
        return this.getFilterValuePatterns();
    }

    /**
     * Locale-specific parsing of specified number.
     * @param numberAsString
     * @return
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
            } catch(Exception e) {
            	SysLog.log(Level.FINE, "Error when parsing number {0}", numberAsString, e);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Get identity of current user's home object.
     * @return
     */
    public Path getUserHomeIdentityAsPath(
    ) {
        return this.getUserHomeIdentity() == null ?
        	null : 
        		this.mapIdentity(this.getUserHomeIdentity(), false);
    }

    /**
     * Get currently active segment name.
     * @return
     */
    public String getCurrentSegment(
    ) {
        return this.currentSegment;
    }

    /**
     * Get current perspective.
     * @return
     */
    public int getCurrentPerspective(
    ) {
        return this.currentPerspective;
    }
    
    /**
     * Set current perspective.
     * @param perspective
     */
    public synchronized void setCurrentPerspective(
        int perspective
    ) {
        this.currentPerspective = perspective;
        // Save in settings
        this.getSettings().setProperty(
            UserSettings.PERSPECTIVE_ID.getName(),
            Integer.toString(this.currentPerspective)
        );        
    }
    
    /**
     * Get current workspace.
     * @return
     */
    public String getCurrentWorkspace(
    ) {
        return this.currentWorkspace;
    }
    
    /**
     * Set current workspace.
     * @param workspace
     */
    public void setCurrentWorkspace(
        String workspace
    ) {
        this.currentWorkspace = workspace;
        // Save in settings
        this.getSettings().setProperty(
            UserSettings.WORKSPACE_ID.getName(),
            this.currentWorkspace
        );        
    }
    
    /**
     * Get ui element definition for given element id.
     * 
     * @param id
     * @return
     * @throws ServiceException
     */
    public org.openmdx.ui1.jmi1.Element getUiElement(
        String id
    ) throws ServiceException {
        return this.getUiContext().getUiSegment(this.currentPerspective).getElement(
            id
        );
    }
    
    /**
     * Get ui element definition for given element id.
     * 
     * @param qualifiedName
     * @return
     * @throws ServiceException
     */
    public org.openmdx.ui1.jmi1.ElementDefinition getUiElementDefinition(
        String qualifiedName
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.ElementDefinition uiElementDefinition = 
            this.getUiContext().getUiSegment(this.currentPerspective).getElementDefinition(qualifiedName);
        if(uiElementDefinition == null) {
            uiElementDefinition = this.getUiContext().getUiSegment(UiContext.MAIN_PERSPECTIVE).getElementDefinition(
                qualifiedName
            );
        }
        return uiElementDefinition;
    }
    
    /**
     * Get form definition for given form.
     * 
     * @param name
     * @return
     * @throws ServiceException
     */
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
    
    /**
     * Get definition for given feature.
     * 
     * @param qualifiedFeatureName
     * @return
     * @throws ServiceException
     */
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

    /**
     * Complete given filter value with configured wildcard patterns.
     * 
     * @param filterValue
     * @return
     */
    public String getWildcardFilterValue(
        String filterValue
    ) {
    	// Escape regexp chars
    	filterValue = filterValue.replace("(", "\\(");
    	filterValue = filterValue.replace(")", "\\)");
    	filterValue = filterValue.replace("[", "\\[");
    	filterValue = filterValue.replace("]", "\\]");
    	filterValue = filterValue.replace("*", "\\*");
    	filterValue = filterValue.replace(".", "\\.");
    	// filterValuePattern handling
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

	/**
	 * Get application name.
	 * @return
	 */
	public String getApplicationName(
	) {
		return this.applicationName;
	}

	/**
	 * Set application name.
	 * @param applicationName
	 */
	public void setApplicationName(
		String applicationName
	) {
		this.applicationName = applicationName;
	}

	/**
	 * Get current locale.
	 * @return
	 */
	public String getLocale(
	) {
		return this.locale;
	}

	/**
	 * Set current locale.
	 * @param locale
	 */
	public void setLocale(
		String locale
	) {
		this.locale = locale;
	}

	/**
	 * Get current time zone.
	 * @return
	 */
	public String getTimezone(
	) {
		return this.timezone;
	}

	/**
	 * Set current time zone.
	 * @param timezone
	 */
	public void setTimezone(
		String timezone
	) {
		this.timezone = timezone;
	}

	/**
	 * @return the initialScale
	 */
	public BigDecimal getInitialScale(
	) {
		return initialScale;
	}

	/**
	 * @param initialScale the initialScale to set
	 */
	public void setInitialScale(
		BigDecimal initialScale
	) {
		if(initialScale != null) {
			if(initialScale.floatValue() >= 0.4 && initialScale.floatValue() <= 3.0) {
				this.initialScale = initialScale;
			} else {
				this.initialScale = BigDecimal.ONE;
			}
		} else {
			this.initialScale = BigDecimal.ONE;
		}
	}

	/**
	 * Get session id.
	 * @return
	 */
	public String getSessionId(
	) {
		return this.sessionId;
	}

	/**
	 * Set session id.
	 * @param sessionId
	 */
	public void setSessionId(
		String sessionId
	) {
		this.sessionId = sessionId;
	}

	/**
	 * Get login principal.
	 * @return
	 */
	public String getLoginPrincipal(
	) {
		return this.loginPrincipal;
	}

	/**
	 * Set login principal.
	 * @param loginPrincipal
	 */
	public void setLoginPrincipal(
		String loginPrincipal
	) {
		this.loginPrincipal = loginPrincipal;
	}

	/**
	 * Get identity of login realm.
	 * @return
	 */
	public Path getLoginRealmIdentity(
	) {
		return this.loginRealmIdentity;
	}

	/**
	 * Set identity of login realm.
	 * @param loginRealmIdentity
	 */
	public void setLoginRealmIdentity(
		Path loginRealmIdentity
	) {
		this.loginRealmIdentity = loginRealmIdentity;
	}

	/**
	 * Get configured retrieveByPath patterns.
	 * @return
	 */
	public List<Path> getRetrieveByPathPatterns(
	) {
		return this.retrieveByPathPatterns;
	}

	/**
	 * Set retrieveByPath patterns.
	 * @param retrieveByPathPatterns
	 */
	public void setRetrieveByPathPatterns(
		List<Path> retrieveByPathPatterns
	) {
		this.retrieveByPathPatterns = retrieveByPathPatterns;
	}

	/**
	 * Get user home identity.
	 * @return
	 */
	public String getUserHomeIdentity() {
		return this.userHomeIdentity;
	}

	/**
	 * Set user home identity.
	 * @param userHomeIdentity
	 */
	public void setUserHomeIdentity(
		String userHomeIdentity
	) {
		this.userHomeIdentity = userHomeIdentity;
	}

	/**
	 * Get root object identities.
	 * @return
	 */
	public String[] getRootObjectIdentities(
	) {
		return this.rootObjectIdentities;
	}

	/**
	 * Set root object identities.
	 * @param rootObjectIdentities
	 */
	public void setRootObjectIdentities(
		String[] rootObjectIdentities
	) {
		this.rootObjectIdentities = rootObjectIdentities;
	}

	/**
	 * Get portal extension.
	 * @return
	 */
	public PortalExtension_1_0 getPortalExtension(
	) {
		return this.portalExtension;
	}

	/**
	 * Set portal extension.
	 * @param portalExtension
	 */
	public void setPortalExtension(
		PortalExtension_1_0 portalExtension
	) {
		this.portalExtension = portalExtension;
	}

	/**
	 * Get http encoder.
	 * @return
	 */
	public HtmlEncoder_1_0 getHttpEncoder(
	) {
		return this.httpEncoder;
	}

	/**
	 * Set http encoder.
	 * @param httpEncoder
	 */
	public void setHttpEncoder(
		HtmlEncoder_1_0 httpEncoder
	) {
		this.httpEncoder = httpEncoder;
	}

	/**
	 * Get filters.
	 * @return
	 */
	public Map<String,Filters> getFilters() {
		return this.filters;
	}

	/**
	 * Set filters.
	 * @param filters
	 */
	public void setFilters(
		Map<String,Filters> filters
	) {
		this.filters = filters;
	}

	/**
	 * Get codes.
	 * @return
	 */
	public Codes getCodes(
	) {
		return this.codes;
	}

	/**
	 * Get codes.
	 * @param codes
	 */
	public void setCodes(
		Codes codes
	) {
		this.codes = codes;
	}

	/**
	 * Get layout factory.
	 * @return
	 */
	public LayoutFactory getLayoutFactory(
	) {
		return this.layoutFactory;
	}

	/**
	 * Set layout factory.
	 * @param layoutFactory
	 */
	public void setLayoutFactory(
		LayoutFactory layoutFactory
	) {
		this.layoutFactory = layoutFactory;
	}

	/**
	 * Get temp file directory.
	 * @return
	 */
	public File getTempDirectory(
	) {
		return this.tempDirectory;
	}

	public void setTempDirectory(
		File tempDirectory
	) {
		this.tempDirectory = tempDirectory;
	}

	/**
	 * Get temp file prefix.
	 * @return
	 */
	public String getTempFilePrefix(
	) {
		return this.tempFilePrefix;
	}

	/**
	 * Set temp file prefix.
	 * @param tempFilePrefix
	 */
	public void setTempFilePrefix(
		String tempFilePrefix
	) {
		this.tempFilePrefix = tempFilePrefix;
	}

	/**
	 * Get reference path for quick accessors.
	 * @return
	 */
	public String getQuickAccessorsReference(
	) {
		return this.quickAccessorsReference;
	}

	/**
	 * Set reference path for quick accessors.
	 * @param quickAccessorsReference
	 */
	public void setQuickAccessorsReference(
		String quickAccessorsReference
	) {
		this.quickAccessorsReference = quickAccessorsReference;
	}

	/**
	 * Get impls for mime types.
	 * @return
	 */
	public Map<String,String> getMimeTypeImpls(
	) {
		return this.mimeTypeImpls;
	}

	/**
	 * Set impls for mime types.
	 * @param mimeTypeImpls
	 */
	public void setMimeTypeImpls(
		Map<String,String> mimeTypeImpls
	) {
		this.mimeTypeImpls = mimeTypeImpls;
	}

	/**
	 * Get exception domain.
	 * @return
	 */
	public String getExceptionDomain(
	) {
		return this.exceptionDomain;
	}

	/**
	 * Set exception domain.
	 * @param exceptionDomain
	 */
	public void setExceptionDomain(
		String exceptionDomain
	) {
		this.exceptionDomain = exceptionDomain;
	}

	/**
	 * Get filter criteria field.
	 * @return
	 */
	public String getFilterCriteriaField(
	) {
		return this.filterCriteriaField;
	}

	/**
	 * Set filter criteria field.
	 * @param filterCriteriaField
	 */
	public void setFilterCriteriaField(
		String filterCriteriaField
	) {
		this.filterCriteriaField = filterCriteriaField;
	}

	/**
	 * Get filter value patterns.
	 * @return
	 */
	public String[] getFilterValuePatterns(
	) {
		return this.filterValuePatterns;
	}

	/**
	 * set filter value patterns.
	 * @param filterValuePatterns
	 */
	public void setFilterValuePatterns(
		String[] filterValuePatterns
	) {
		this.filterValuePatterns = filterValuePatterns;
	}

	/**
	 * Get persistence manager factory for user data persistence managers.
	 * @return
	 */
	public PersistenceManagerFactory getPmfData(
	) {
		return this.pmfData;
	}

	/**
	 * Set persistence manager factory for user data persistence managers.
	 * @param pmfData
	 */
	public void setPmfData(
		PersistenceManagerFactory pmfData
	) {
		this.pmfData = pmfData;
	}

	/**
	 * Get model.
	 * @return
	 */
	public Model_1_0 getModel(
	) {
		return this.model;
	}

	/**
	 * Set model.
	 * @param model
	 */
	public void setModel(
		Model_1_0 model
	) {
		this.model = model;
	}

	/**
	 * Get view port type.
	 * @return
	 */
	public ViewPort.Type getViewPortType(
	) {
		return this.viewPortType;
	}

	/**
	 * Set view port type.
	 * @param viewPortType
	 */
	public void setViewPortType(
		ViewPort.Type viewPortType
	) {
		this.viewPortType = viewPortType;
	}

	/**
	 * Get user role.
	 * @return
	 */
	public String getUserRole(
	) {
		return this.userRole;
	}    

	/**
	 * Set user role.
	 * @param userRole
	 */
	public void setUserRole(
		String userRole
	) {
		this.userRole = userRole;
	}

	/**
	 * @return the wizardDefinitionFactory
	 */
	public WizardDefinitionFactory getWizardDefinitionFactory(
	) {
		return wizardDefinitionFactory;
	}

	/**
	 * @param wizardDefinitionFactory the wizardDefinitionFactory to set
	 */
	public void setWizardDefinitionFactory(
		WizardDefinitionFactory wizardDefinitionFactory
	) {
		this.wizardDefinitionFactory = wizardDefinitionFactory;
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
	protected BigDecimal initialScale = BigDecimal.ONE;
	protected String sessionId;
	protected ViewPort.Type viewPortType;
	protected String loginPrincipal;
	protected String userRole;
	protected Path loginRealmIdentity;
	protected List<Path> retrieveByPathPatterns;
	protected String userHomeIdentity;
	protected String[] rootObjectIdentities;
	protected PortalExtension_1_0 portalExtension;
	protected HtmlEncoder_1_0 httpEncoder;
	protected Map<String,Filters> filters;
	protected Texts textsFactory;
	protected UiContext uiContext;
	protected WizardDefinitionFactory wizardDefinitionFactory;
	protected Codes codes;
	protected LayoutFactory layoutFactory;
	protected File tempDirectory;
	protected String tempFilePrefix;
	protected String quickAccessorsReference;
	protected Map<String,String> mimeTypeImpls;
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
	protected #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif pmDataReloadedAt;
	protected PersistenceManager pmControl; // package managing control objects
	protected String currentLocaleAsString = null;
	protected String currentTimeZone = null;
	protected String currentSegment = "Standard";
	protected int currentPerspective = 0;
	protected String currentWorkspace = DEFAULT_WORKSPACE;
	protected ViewPort.Type currentViewPortType = null;
  
}

//--- End of File -----------------------------------------------------------
