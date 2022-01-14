/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: View 
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
package org.openmdx.portal.servlet.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.jdo.PersistenceManager;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.QuickAccessor;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.action.LogoffAction;
import org.openmdx.portal.servlet.action.SaveSettingsAction;
import org.openmdx.portal.servlet.action.SetPanelStateAction;
import org.openmdx.portal.servlet.control.Control;
import org.openmdx.portal.servlet.control.InspectorControl;

/**
 * View
 *
 */
public abstract class View extends Component implements Serializable {

	/**
     * Creates view.
     * 
     * @param id id of the view
     * @param containerElementId id of the container element if the view is 
     *        embedded in another view. null if the view is a not embedded. The
     *        containerElementId is used by controls to create unique tag ids. 
     * @param object object to be rendered
     * @param navigationTarget navigation anchors are generated with specified target.
     * @param isReadOnly if specified overrides changeable property of controls.
     * @param app application context
     */
    public View(
    	InspectorControl control,
        String id,
        String containerElementId,
        Object object,
        String resourcePathPrefix,
        String navigationTarget,
        Boolean isReadOnly,
        ApplicationContext app
    ) {
    	super(
    		control,
    		null
    	);
    	this.view = this;
        this.id = id;
        this.containerElementId = containerElementId;
        this.object = object;
        this.resourcePathPrefix = resourcePathPrefix;
        this.navigationTarget = navigationTarget;
        this.isReadOnly = isReadOnly;
        this.app = app;
    }

    /**
     * Get view type.
     * 
     * @return
     */
    public abstract String getType();
  
    /**
     * Create a unique request id for this view.
     * 
     */
    public void createRequestId(
    ) {
        this.requestId = UUIDConversion.toUID(UUIDs.newUUID());
    }
  
    /**
     * Get unique request id.
     * 
     * @return
     */
    public String getRequestId(
    ) {
        return this.requestId;
    }
  
    /**
     * Get application context.
     * 
     * @return
     */
    public ApplicationContext getApplicationContext(
    ) {
        return this.app;
    }
  
    /**
     * Get logoff action.
     * 
     * @return
     */
    public Action getLogoffAction(
    ) {
        return new Action(
            LogoffAction.EVENT_ID,
            null,
            this.app.getTexts().getLogoffText(),
            true
        );
    }

    /**
     * Get save settings action.
     * 
     * @return
     */
    public Action getSaveSettingsAction(
    ) {
        return new Action(
            SaveSettingsAction.EVENT_ID,
            null,
            this.app.getTexts().getSaveSettingsText(),
            true
        );
    }

    /**
     * Get resource path prefix.
     * 
     * @return
     */
    public String getResourcePathPrefix(
    ) {
    	return this.resourcePathPrefix;
    }
    
    /**
     * The navigation target is rendered for anchor tags generated for object navigation.
     *   
     * @return navigation target. Values are "_none", "_blank", "_parent", "_self", "_top". 
	 */
	public String getNavigationTarget(
	) {
	    return this.navigationTarget;
	}

	/**
	 * If specified, overrides changeable property of controls.
	 * 
	 * @return true if view is read-only. If null, changeable property of control is applied.
	 */
	public Boolean isReadOnly(
	) {
		return this.isReadOnly;
	}
	
    /**
     * Get set role actions.
     * 
     * @return
     */
    public Action[] getSetRoleActions(
    ) {
    	List<String> userRoles = this.app.getUserRoles();
    	Map<String,Action> selectRoleActions = new TreeMap<String,Action>();
    	for(String roleName: userRoles) {
    		Action template = new Action(
    			Action.EVENT_SET_ROLE,
    			new Action.Parameter[]{
    				new Action.Parameter(Action.PARAMETER_NAME, roleName)
    			},
    			roleName,
        		this.app.getTexts().getSelectUserRoleText(),
    			WebKeys.ICON_ROLE,
    			true
    		);    		
    		selectRoleActions.put(
    			template.getTitle(),
    			new Action(
    				template.getEvent(),
    				template.getParameters(),
    				this.app.getPortalExtension().getTitle(this.getObject(), template, roleName, this.app),
    				template.getToolTip(),
    				template.getIconKey(),
    				template.isEnabled()    				
    			)
    		);
    	}
    	return selectRoleActions.values().toArray(new Action[selectRoleActions.size()]);
    }

    /**
     * Get quick access actions.
     * 
     * @return
     */
    public Action[] getQuickAccessActions(
    ) {
        QuickAccessor[] quickAccessors = this.getApplicationContext().getQuickAccessors();
        Action[] actions = new Action[quickAccessors.length];
        for(int i = 0; i < quickAccessors.length; i++) {
            actions[i] = quickAccessors[i].getAction(this.getObject());
        }
        return actions;
    }
  
    /**
     * Get macros.
     * 
     * @return
     */
    public Object[] getMacro(
    ) {
        return this.macro;
    }
    
    /**
     * Set macros.
     * 
     * @param newValue
     */
    public void setMacro(
        Object[] newValue
    ) {
        this.macro = newValue;
    }
    
    /**
     * Get select root object actions.
     * 
     * @return
     */
    public Action[] getSelectRootObjectActions(
    ) {
        return this.getApplicationContext().getRootObjectActions();
    }
    
    /**
     * Get set panel state action.
     * 
     * @param panelName
     * @param panelState
     * @return
     */
    public Action getSetPanelStateAction(
        String panelName,
        int panelState
    ) {
        return new Action(
            SetPanelStateAction.EVENT_ID,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_NAME, panelName),
                new Action.Parameter(Action.PARAMETER_STATE, "" + panelState)
            },
            "N/A",
            true
        );      
    }
  
    /**
     * Get state for given panel.
     * 
     * @param panelName
     * @return
     */
    public int getPanelState(
        String panelName
    ) {
        return this.getApplicationContext().getPanelState(panelName);
    }
  
    /**
     * Each view has an id and a childId. Normally all views of the same
     * 'layer' have the same id, i.e. a view which creates a 'next' view
     * inherits the id. getChild(), createNewChild() allows the create 
     * new view layers.
     * <p>
     * A JSP wants to create a ShowObject dialog in a new window. This
     * can be done by creating a new child id and then issuing an event
     * (e.g. EVENT_FIND_OBJECT) which creates a view with the new child id.
     * 
     * @return
     */
    public String getId(
    ) {
        return this.id;
    }
  
    /**
     * Get id of container element.
     * 
     * @return id of the container element if the view is embedded, null
     * otherwise
     */
    public String getContainerElementId(
    ) {
        return this.containerElementId;
    }
    
    /**
     * Handle can not commit exception.
     * 
     * @param e
     */
    public void handleCanNotCommitException(
        BasicException e
    ) {
        BasicException e0 = e.getCause(this.app.getExceptionDomain());
        if(e0 == null) {
            this.app.addErrorMessage(
                this.app.getTexts().getErrorTextCanNotCreateOrEditObject(),
                new String[]{e.getMessage()}
            );
        } else if(app.getTexts().getUserDefinedText(e0.getExceptionCode() + "") != null) {
            List<String> parameters = new ArrayList<String>();
            int i = 0;
            while(e0.getParameter("param" + i) != null) {
                parameters.add(e0.getParameter("param" + i));
                i++;
            }
            this.app.addErrorMessage(
                this.app.getTexts().getUserDefinedText(e0.getExceptionCode() + ""),
                (String[])parameters.toArray(new String[parameters.size()])
	        );             
        } else {
            this.app.addErrorMessage(
                this.app.getTexts().getErrorTextCanNotCreateOrEditObject(),
                new String[]{e.getMessage()}
	        );              
        }
    }
  
    /**
     * Refresh view.
     * 
     * @param refreshData
     * @param closePm
     * @return
     * @throws ServiceException
     */
    public PersistenceManager refresh(
        boolean refreshData,
        boolean closePm
    ) throws ServiceException {
    	return null;
    }
  
    /**
     * Create control.
     * 
     * @param id
     * @param controlClass
     * @param parameter
     * @return
     * @throws ServiceException
     */
    public Control createControl(
        String id,
        Class<?> controlClass,
        Object... parameter
    ) throws ServiceException {
    	ApplicationContext app = this.app;    	
        return this.app.getPortalExtension().getControlFactory().createControl(
            id,
            app.getCurrentLocaleAsString(),
            app.getCurrentLocaleAsIndex(),            
            controlClass,
            parameter
       );
    }

    /**
     * Get view object.
     * 
     * @return
     */
    public Object getObject(
    ) {
        return this.object;
    }
    
    /**
     * Get request id in given format.
     * 
     * @param requestIdFormat
     * @return
     */
    protected String getRequestId(
        short requestIdFormat
    ) {
        String requestId = null;
        switch(requestIdFormat) {
            case REQUEST_ID_FORMAT_NONE:
                requestId = null;
                break;
            case REQUEST_ID_FORMAT_UID:
                requestId = this.getRequestId() != null
                    ? this.getRequestId()
                    : null;
                break;
            case REQUEST_ID_FORMAT_TEMPLATE:
                requestId = View.REQUEST_ID_TEMPLATE;
                break;
        }
        return requestId;
    }
    
    /**
     * Get href for given action.
     * 
     * @param action
     * @return
     */
    public String getEvalHRef(
        Action action
    ) {
        return this.getEvalHRef(
            action, 
            REQUEST_ID_FORMAT_UID
        );
    }
    
    /**
     * Get href for given action.
     * 
     * @param action
     * @param includeRequestId
     * @return
     */
    public String getEvalHRef(
        Action action,
        boolean includeRequestId
    ) {
        return this.getEvalHRef(
            action, 
            includeRequestId
                ? REQUEST_ID_FORMAT_UID
                : REQUEST_ID_FORMAT_NONE
        );
    }
    
    /**
     * Get href for given action.
     * 
     * @param action
     * @param requestIdFormat
     * @return
     */
    public String getEvalHRef(
        Action action,
        short requestIdFormat
    ) {
        return action.getEvalHRef(
            this.getRequestId(requestIdFormat)
        );
    }
        
    /**
     * Get href for given action.
     * 
     * @param action
     * @return
     */
    public String getEncodedHRef(
        Action action
    ) {
        return this.getEncodedHRef(
            action, 
            REQUEST_ID_FORMAT_UID
        );
    }
    
    /**
     * Get href for given action.
     * 
     * @param action
     * @param includeRequestId
     * @return
     */
    public String getEncodedHRef(
        Action action,
        boolean includeRequestId
    ) {
       return this.getEncodedHRef(
           action,
           includeRequestId
               ? REQUEST_ID_FORMAT_UID
               : REQUEST_ID_FORMAT_NONE
       );
    }
    
    /**
     * Get href for given action.
     * 
     * @param action
     * @param requestIdFormat
     * @return
     */
    public String getEncodedHRef(
        Action action,
        short requestIdFormat
    ) {
        return action.getEncodedHRef(
            this.getRequestId(requestIdFormat)
        );
    }
        
    /**
     * Get find object action.
     * 
     * @param feature
     * @param id
     * @return
     */
    public Action getFindObjectAction(
        String feature,
        String id
    ) {
        return Action.getFindObjectAction(
            feature, 
            id
        );
    }

    /**
     * Find UI field for given feature.
     * 
     * @param forClass
     * @param featureName
     * @return
     * @throws ServiceException
     */
    public org.openmdx.ui1.jmi1.ValuedField findField(
        String forClass,
        String featureName
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.Inspector inspector = this.app.getInspector(forClass);
        for(Object pane: inspector.getMember()) {
            if (pane instanceof org.openmdx.ui1.jmi1.AttributePane) {
                org.openmdx.ui1.jmi1.AttributePane paneAttr = (org.openmdx.ui1.jmi1.AttributePane)pane;
                List<org.openmdx.ui1.jmi1.Tab> tabs = paneAttr.getMember();
                for(org.openmdx.ui1.jmi1.Tab tab: tabs) {
                	List<org.openmdx.ui1.jmi1.FieldGroup> fieldGroups = tab.getMember();
                    for(org.openmdx.ui1.jmi1.FieldGroup fieldGroup: fieldGroups) {
                    	List<org.openmdx.ui1.jmi1.ValuedField> fields = fieldGroup.getMember();
                        for(org.openmdx.ui1.jmi1.ValuedField field: fields) {
                            if(field.getFeatureName().equals(featureName)) {
                                return field;
                            }
                        }
                    }
                }
            }          
        }   
        return null;
    }    

    /**
     * Get label for given feature.
     * 
     * @param forClass
     * @param featureName
     * @param locale
     * @return
     * @throws ServiceException
     */
    public String getFieldLabel(
        String forClass,
        String featureName,
        short locale
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.LabelledField field = this.findField(
            forClass, 
            featureName
        );
        return field == null ? 
        	null : 
    		locale < field.getLabel().size() ? 
    			field.getLabel().get(locale) : 
    			field.getLabel().get(0);
    }
    
    /**
     * Get short label for given feature.
     * 
     * @param forClass
     * @param featureName
     * @param locale
     * @return
     * @throws ServiceException
     */
    public String getFieldShortLabel(
        String forClass,
        String featureName,
        short locale
    ) throws ServiceException {
        org.openmdx.ui1.jmi1.LabelledField field = this.findField(
            forClass, 
            featureName
        );
        return field == null ? 
        	null :
        		field.getShortLabel().isEmpty() ?
        			this.getFieldLabel(forClass, featureName, locale) :
        				locale < field.getShortLabel().size() ? 
        					field.getShortLabel().get(locale) : 
        						field.getShortLabel().get(0);
    }
        
    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
	private static final long serialVersionUID = 4407613329999766870L;

	public static final String VIEW_EDIT_OBJECT = "EditObject";
    public static final String VIEW_SHOW_OBJECT = "ShowObject";
    public static final String VIEW_USER_DEFINED = "UserDefined";
    public static final String REQUEST_ID_TEMPLATE = "REQUEST_ID";
    public static final String CURRENT_OBJECT_XRI_TEMPLATE = "CURRENT_OBJECT_XRI";
    
    public static final short REQUEST_ID_FORMAT_NONE = 0;
    public static final short REQUEST_ID_FORMAT_UID = 1;
    public static final short REQUEST_ID_FORMAT_TEMPLATE = 2;
    
    protected final ApplicationContext app;
    protected final String id;
    protected final String containerElementId;
    protected Object object;
    protected String resourcePathPrefix = null;
    protected String navigationTarget = null;
    protected Boolean isReadOnly = null;
    protected String requestId = null;
    protected Object[] macro = null;
    
}

//--- End of File -----------------------------------------------------------
