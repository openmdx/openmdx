/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: View.java,v 1.52 2008/06/26 00:39:15 wfro Exp $
 * Description: View 
 * Revision:    $Revision: 1.52 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/26 00:39:15 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2006, OMEX AG, Switzerland
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
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.portal.servlet.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefStruct;

import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.QuickAccessor;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.control.Control;
import org.openmdx.portal.servlet.control.ControlFactory;

//---------------------------------------------------------------------------
public abstract class View
    implements Serializable {
  
    //-------------------------------------------------------------------------
    /**
     * Creates view.
     * 
     * @param id id of the view
     * @param containerElementId id of the container element if the view is 
     *        embedded in another view. null if the view is a not embedded. The
     *        containerElementId is used by controls to create unique tag ids. 
     * @param object object to be rendered
     * @param application application context
     * @param controlFactory factory used to created new controls
     */
    public View(
        String id,
        String containerElementId,
        Object object,
        ApplicationContext application,
        ControlFactory controlFactory
    ) {
        this.id = id;
        this.containerElementId = containerElementId;
        this.object = object;
        this.application = application;
        // View must only work with this persistence manager and must not
        // get the persistence manager from application.getPmData.
        this.pm = application.getPmData(); 
        this.controlFactory = controlFactory;
        this.setGuiModeActions = new Action[]{
            new Action(
                Action.EVENT_SELECT_GUI_MODE, 
                new Action.Parameter[]{ 
                   new Action.Parameter(Action.PARAMETER_NAME, WebKeys.SETTING_GUI_MODE_BASIC) 
                }, 
                "0", 
                true
            ),                              
            new Action(
                Action.EVENT_SELECT_GUI_MODE, 
                new Action.Parameter[]{ 
                   new Action.Parameter(Action.PARAMETER_NAME, WebKeys.SETTING_GUI_MODE_STANDARD) 
                }, 
                "1", 
                true
            )      
        };
    }

    //-------------------------------------------------------------------------
    public abstract String getType();
  
    //-------------------------------------------------------------------------
    public void createRequestId(
    ) {
        this.requestId = this.uuidGenerator.next().toString();
    }
  
    //-------------------------------------------------------------------------
    public String getRequestId(
    ) {
        return this.requestId;
    }
  
    //-------------------------------------------------------------------------
    public ApplicationContext getApplicationContext(
    ) {
        return this.application;
    }
  
    //-------------------------------------------------------------------------
    public Action getLogoffAction(
    ) {
        return new Action(
            Action.EVENT_LOGOFF,
            null,
            this.application.getTexts().getLogoffText(),
            true
        );
    }

    //-------------------------------------------------------------------------
    public Action getSaveSettingsAction(
    ) {
        return new Action(
            Action.EVENT_SAVE_SETTINGS,
            null,
            this.application.getTexts().getSaveSettingsText(),
            true
        );
    }

    //-------------------------------------------------------------------------
    public Action[] getSetRoleActions(
    ) {
        List userRoles = this.application.getUserRoles();
        Action[] selectRoleActions = new Action[userRoles.size()];
        int ii = 0;
        for(
            Iterator i = userRoles.iterator(); 
            i.hasNext(); 
            ii++
        ) {
          String roleName = (String)i.next();
          selectRoleActions[ii] = new Action(
              Action.EVENT_SET_ROLE,
              new Action.Parameter[]{
                  new Action.Parameter(Action.PARAMETER_NAME, roleName)
              },
              this.application.getTexts().getSelectUserRoleText(),
              true
          );          
        }
        return selectRoleActions;
    }
  
    //-------------------------------------------------------------------------
    public Action[] getQuickAccessActions(
    ) {
        QuickAccessor[] quickAccessors = this.getApplicationContext().getQuickAccessors();
        Action[] actions = new Action[quickAccessors.length];
        for(int i = 0; i < quickAccessors.length; i++) {
            actions[i] = quickAccessors[i].getAction();
        }
        return actions;
    }
  
    //-------------------------------------------------------------------------
    public Object[] getMacro(
    ) {
        return this.macro;
    }
    
    //-------------------------------------------------------------------------
    public void setMacro(
        Object[] newValue
    ) {
        this.macro = newValue;
    }
    
    //-------------------------------------------------------------------------
    public Action[] getSelectRootObjectActions(
    ) {
        return this.getApplicationContext().getRootObjectActions();
    }
    
    //-------------------------------------------------------------------------  
    public Action getSetPanelStateAction(
        String panelName,
        int panelState
    ) {
        return new Action(
            Action.EVENT_SET_PANEL_STATE,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_NAME, panelName),
                new Action.Parameter(Action.PARAMETER_STATE, "" + panelState)
            },
            "N/A",
            true
        );      
    }
  
    //-------------------------------------------------------------------------  
    public Action[] getSetGuiModeActions(
    ) {
        return this.setGuiModeActions;
    }
  
    //-------------------------------------------------------------------------  
    public int getPanelState(
        String panelName
    ) {
        return this.getApplicationContext().getPanelState(panelName);
    }
  
    //-------------------------------------------------------------------------  
    /**
     * Each view has an id and a childId. Normally all views of the same
     * 'layer' have the same id, i.e. a view which creates a 'next' view
     * inherits the id. getChild(), createNewChild() allows the create 
     * new view layers.
     * <p>
     * A JSP wants to create a ShowObject dialog in a new window. This
     * can be done by creating a new child id and then issuing an event
     * (e.g. EVENT_FIND_OBJECT) which creates a view with the new child id.
     */
    public String getId(
    ) {
        return this.id;
    }
  
    //-------------------------------------------------------------------------  
    /**
     * @return id of the container element if the view is embedded, null
     * otherwise
     */
    public String getContainerElementId(
    ) {
        return this.containerElementId;
    }
    
    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void structToMap(
        RefStruct from,
        Map to,
        ModelElement_1_0 structDef
    ) throws JmiServiceException {
        for(Iterator i = ((Map)structDef.values("field").get(0)).values().iterator(); i.hasNext(); ) {
          ModelElement_1_0 field = (ModelElement_1_0)i.next();
          String fieldName = (String)field.values("qualifiedName").get(0);
          try {
            to.put(
              fieldName,
              from.refGetValue(fieldName)
            );
          }
          catch(Exception e) {
            to.put(fieldName, null);
          }
        }
    }

    //-------------------------------------------------------------------------
    public void handleCanNotCommitException(
        BasicException e
    ) {
        BasicException e0 = e.getCause(this.application.getExceptionDomain());
        if(e0 == null) {
            this.application.addErrorMessage(
              application.getTexts().getErrorTextCanNotCreateOrEditObject(),
              new String[]{e.getMessage()}
            );
        }
        else if(application.getTexts().getUserDefinedText(e0.getExceptionCode() + "") != null) {
            List<String> parameters = new ArrayList<String>();
            int i = 0;
            while(e0.getParameter("param" + i) != null) {
                parameters.add(e0.getParameter("param" + i));
                i++;
            }
            this.application.addErrorMessage(
                application.getTexts().getUserDefinedText(e0.getExceptionCode() + ""),
                (String[])parameters.toArray(new String[parameters.size()])
	        );             
        }
        else {
            this.application.addErrorMessage(
                application.getTexts().getErrorTextCanNotCreateOrEditObject(),
                new String[]{e.getMessage()}
	        );              
        }
    }
  
    //-------------------------------------------------------------------------
    /**
     * Refresh view. refreshData ==> do refresh data. Do not refresh 
     * rendering information.
     */
    public void refresh(
        boolean refreshData
    ) throws ServiceException {
    }
  
    //-------------------------------------------------------------------------
    public Control createControl(
        String id,
        Class controlClass
    ) throws ServiceException {
        return this.controlFactory.createControl(
            id,
            this.application.getCurrentLocaleAsString(),
            this.application.getCurrentLocaleAsIndex(),
            controlClass
        );
    }
  
    //-------------------------------------------------------------------------
    public Control createControl(
        String id,
        Class controlClass,
        Object[] parameter
    ) throws ServiceException {
        return this.controlFactory.createControl(
            id,
            this.application.getCurrentLocaleAsString(),
            this.application.getCurrentLocaleAsIndex(),
            controlClass,
            parameter
       );
    } 
  
    //-------------------------------------------------------------------------
    public ControlFactory getControlFactory(
    ) {
        return this.controlFactory;
    }
    
    //-------------------------------------------------------------------------
    public PersistenceManager getPersistenceManager(
    ) {
        return this.pm;
    }
    
    //-------------------------------------------------------------------------
    public Object getObject(
    ) {
        return this.object;
    }
    
    //-------------------------------------------------------------------------
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
    
    //-------------------------------------------------------------------------
    public String getEvalHRef(
        Action action
    ) {
        return this.getEvalHRef(
            action, 
            REQUEST_ID_FORMAT_UID
        );
    }
    
    //-------------------------------------------------------------------------
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
    
    //-------------------------------------------------------------------------
    public String getEvalHRef(
        Action action,
        short requestIdFormat
    ) {
        return action.getEvalHRef(
            this.getRequestId(requestIdFormat)
        );
    }
        
    //-------------------------------------------------------------------------
    public String getEncodedHRef(
        Action action
    ) {
        return this.getEncodedHRef(
            action, 
            REQUEST_ID_FORMAT_UID
        );
    }
    
    //-------------------------------------------------------------------------
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
    
    //-------------------------------------------------------------------------
    public String getEncodedHRef(
        Action action,
        short requestIdFormat
    ) {
        return action.getEncodedHRef(
            this.getRequestId(requestIdFormat)
        );
    }
        
    //-------------------------------------------------------------------------
    public Action getFindObjectAction(
        String feature,
        String id
    ) {
        return Action.getFindObjectAction(
            feature, 
            id
        );
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    public static final String VIEW_EDIT_OBJECT = "EditObject";
    public static final String VIEW_SHOW_OBJECT = "ShowObject";
    public static final String VIEW_USER_DEFINED = "UserDefined";
    public static final String REQUEST_ID_TEMPLATE = "REQUEST_ID";
    
    public static final short REQUEST_ID_FORMAT_NONE = 0;
    public static final short REQUEST_ID_FORMAT_UID = 1;
    public static final short REQUEST_ID_FORMAT_TEMPLATE = 2;
    
    protected final ApplicationContext application;
    protected final ControlFactory controlFactory;
    protected final String id;
    protected final String containerElementId;
    protected Object object;
    protected PersistenceManager pm;
  
    protected String requestId = null;
    protected Action[] setGuiModeActions = null;
    protected Object[] macro = null;
    
    private transient UUIDGenerator uuidGenerator = UUIDs.getGenerator();

}

//--- End of File -----------------------------------------------------------
