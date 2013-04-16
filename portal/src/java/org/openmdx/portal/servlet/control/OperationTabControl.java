/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: OperationTabControl
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
package org.openmdx.portal.servlet.control;

import java.io.Serializable;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.action.GetOperationDialogAction;
import org.openmdx.portal.servlet.action.InvokeOperationAction;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.view.ViewMode;
import org.openmdx.ui1.layer.application.Ui_1;

/**
 * OperationTabControl
 *
 */
public class OperationTabControl extends TabControl implements Serializable {
  
    /**
     * Constructor 
     *
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param tab
     * @param paneIndex
     * @param tabIndex
     */
    public OperationTabControl(
        String id,
        String locale,
        int localeAsIndex,
        ControlFactory controlFactory,
        org.openmdx.ui1.jmi1.OperationTab tab,
        int paneIndex,
        int tabIndex
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            controlFactory,
            tab,
            paneIndex,
            tabIndex
        );
        this.isQuery = tab == null ? false : Boolean.TRUE.equals(tab.isQuery());
        this.operationName = tab == null ? null :((org.openmdx.ui1.jmi1.OperationTab)tab).getOperationName();
    }

    /**
     * Get operation name.
     * 
     * @return
     */
    public String getOperationName(
    ) {
        return this.operationName;
    }

    /**
     * Get qualified operation name.
     * 
     * @return
     */
    public String getQualifiedOperationName(
    ) {
    	return this.operationName;
    }
    
    /**
     * Get operation icon key.
     * 
     * @return
     */
    public String getIconKey(
    ) {
        return this.iconKey.substring(
            this.iconKey.lastIndexOf(":") + 1
        ) + WebKeys.ICON_TYPE;
    }

    /**
     * Get operation tooltip.
     * 
     * @return
     */
    public String getToolTip(
    ) {
        return this.localeAsIndex < this.toolTips.size() ? 
            this.toolTips.get(this.localeAsIndex) : 
            this.toolTips.get(0);
    }
  
    /**
     * Return true if operation configuration is required.
     * 
     * @param app
     * @return
     */
    public boolean confirmExecution(
        ApplicationContext app
    ) {
        return 
            Ui_1.DELETE_OBJECT_OPERATION_NAME.equals(this.getOperationName()) ||
            (Ui_1.RELOAD_OBJECT_OPERATION_NAME.equals(this.getOperationName()) && app.getPortalExtension().isRootPrincipal(app.getCurrentUserRole()));
    }
    
    /**
     * Return true if result of this operation is displayed inline.
     * 
     * @return
     */
    public boolean displayOperationResult(
    ) {
    	if(
    		// Do not display inline for operations with empty result
    		(!this.isQuery && (this.getFieldGroupControl().length == 0 || (this.getFieldGroupControl().length == 2 && this.getFieldGroupControl()[1].getFields().isEmpty()))) ||
    		Ui_1.DELETE_OBJECT_OPERATION_NAME.equals(this.getOperationName()) ||
    		Ui_1.RELOAD_OBJECT_OPERATION_NAME.equals(this.getOperationName()) ||
    		Ui_1.NAVIGATE_TO_PARENT_OPERATION_NAME.equals(this.getOperationName())
    	) {
    		return false;
    	} else {
    		return true;
    	}
    }

    /**
     * Get invoke operation action.
     * 
     * @param view
     * @param app
     * @return
     * @throws ServiceException
     */
    public Action getInvokeOperationAction(
        ShowObjectView view,
        ApplicationContext app
    ) throws ServiceException {
        // built-in operations
        if(Ui_1.EDIT_OBJECT_OPERATION_NAME.equals(this.getOperationName())) {
            return view.getObjectReference().getEditObjectAction(ViewMode.EMBEDDED);
        }
        else if(Ui_1.DELETE_OBJECT_OPERATION_NAME.equals(this.getOperationName())) { 
            return view.getObjectReference().getDeleteObjectAction();
        }
        else if(Ui_1.RELOAD_OBJECT_OPERATION_NAME.equals(this.getOperationName())) {
            return view.getObjectReference().getReloadAction();
        }
        else if(Ui_1.NAVIGATE_TO_PARENT_OPERATION_NAME.equals(this.getOperationName())) {
            return view.getObjectReference().getSelectParentAction();
        }
        else {
            return new Action(
                InvokeOperationAction.EVENT_ID,
                new Action.Parameter[]{
                    new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(this.getPaneIndex())),
                    new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getObjectReference().getXRI()),                
                    new Action.Parameter(Action.PARAMETER_TAB, Integer.toString(this.getTabIndex()))
                },
                "OK",
                true
            );
        }
    }

    /**
     * Get operation dialog action.
     * 
     * @param view
     * @param app
     * @return
     * @throws ServiceException
     */
    public Action getGetOperationDialogAction(
        ShowObjectView view,
        ApplicationContext app
    ) throws ServiceException {
        return new Action(
            GetOperationDialogAction.EVENT_ID,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getObjectReference().getXRI()),
                new Action.Parameter(Action.PARAMETER_ID, Integer.toString(this.getTabId()))
            },
            "OK",
            true
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.control.Control#paint(org.openmdx.portal.servlet.ViewPort, java.lang.String, boolean)
     */
    @Override
    public void paint(
        ViewPort p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {
        ApplicationContext app = p.getApplicationContext();
        ShowObjectView view = (ShowObjectView)p.getView();
        // Operation menues
        if(frame == null) {
            String operationId = Integer.toString(this.getTabId());
        	boolean isRevokeShow = app.getPortalExtension().hasPermission(
                this.getOperationName(),
                view.getObjectReference().getObject(),
                app,
                WebKeys.PERMISSION_REVOKE_SHOW
            );
        	boolean isRevokeEdit = app.getPortalExtension().hasPermission(
                this.getOperationName(),
                view.getObjectReference().getObject(),
                app,
                WebKeys.PERMISSION_REVOKE_EDIT
            );
            if(!isRevokeEdit && !isRevokeShow) {
                // No input parameters so do not get operation dialog
                if((this.getFieldGroupControl().length == 0) && !this.confirmExecution(app)) {
                    Action invokeOperationAction = this.getInvokeOperationAction(
                        view,
                        app
                    );
                    if(Ui_1.EDIT_OBJECT_OPERATION_NAME.equals(this.getOperationName())) {
                        p.write("    <li><a href=\"javascript:void(0)\" onclick=\"javascript:new Ajax.Updater('aPanel', ", p.getEvalHRef(invokeOperationAction),", {asynchronous:true, evalScripts: true, onComplete: function(){}});return false;\" id=\"opTab", operationId, "\" >", this.getName(), "</a></li>");                        
                    } else {
                        p.write("    <li><a href=\"javascript:void(0)\" onmouseover=\"javascript:this.href=", p.getEvalHRef(invokeOperationAction), ";onmouseover=function(){};\" id=\"opTab", operationId, "\" >", this.getName(), "</a></li>");
                    }
                }
                // Standard operation with input parameters. Retrieve operation dialog with async request.
                else {                                         
                    Action getOperationDialogAction = this.getGetOperationDialogAction(
                        view,
                        app
                    );
                    p.write("    <li><a href=\"javascript:void(0)\" onclick=\"javascript:new Ajax.Updater('OperationDialog', ", p.getEvalHRef(getOperationDialogAction),", {asynchronous:true, evalScripts: true, onComplete: function(){}});return false;\" id=\"op", operationId, "\" >", this.getName(), "...</a></li>");                        
                }
            }
            // Operation can not be invoked. Do not generate onclick or href actions
            else if(!isRevokeShow) {
                // no input parameters
                if(this.getFieldGroupControl().length == 0) {
                    p.write("    <li><a href=\"javascript:void(0)\" id=\"opTab", operationId, "\" ><span>", this.getName(), "</span></a></li>");
                }
                // standard operation with input parameters
                else {
                    p.write("    <li><a href=\"javascript:void(0)\" id=\"opTab", operationId, "\" ><span>", this.getName(), "...</span></a></li>");
                }
            }
            // Operation is hidden
            else {
            	// no op
            }
        }
    }
  
	/**
	 * Retrieve isQuery.
	 *
	 * @return Returns the isQuery.
	 */
	public boolean isQuery(
	) {
		return this.isQuery;
	}

	//-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3904961962566955576L;

    public static final String FRAME_RESULTS = "Results";

    protected final String operationName;
    protected final boolean isQuery;
	
}

//--- End of File -----------------------------------------------------------
