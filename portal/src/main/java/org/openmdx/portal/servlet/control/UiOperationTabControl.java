/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: OperationTabControl
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
package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.util.List;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.PortalExtension_1_0;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.action.UiGetOperationDialogAction;
import org.openmdx.portal.servlet.action.InvokeOperationAction;
import org.openmdx.portal.servlet.component.ObjectView;
import org.openmdx.portal.servlet.component.UiOperationTab;
import org.openmdx.portal.servlet.component.ShowObjectView;
import org.openmdx.portal.servlet.component.ViewMode;
import org.openmdx.ui1.layer.application.Ui_1;

/**
 * OperationTabControl
 *
 */
public class UiOperationTabControl extends UiTabControl implements Serializable {
  
    /**
     * Constructor 
     *
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param tabDef
     * @param paneIndex
     * @param tabIndex
     */
	public UiOperationTabControl(
		String id,
		String locale,
		int localeAsIndex,
		PortalExtension_1_0.ControlFactory controlFactory,
		org.openmdx.ui1.jmi1.OperationTab tabDef,
		int paneIndex,
		int tabIndex
	) {
		super(
			id,
			locale,
			localeAsIndex,
			controlFactory,
			tabDef,
			paneIndex,
			tabIndex
		);
		this.isQuery = tabDef == null ? false : Boolean.TRUE.equals(tabDef.isQuery());
		this.operationName = tabDef == null ? null :((org.openmdx.ui1.jmi1.OperationTab)tabDef).getOperationName();
		// Replace field group controls by operation param controls
		this.fieldGroupControls.clear();
		if(tabDef != null) {
			for(org.openmdx.ui1.jmi1.FieldGroup fieldGroup: tabDef.<org.openmdx.ui1.jmi1.FieldGroup>getMember()) {
				this.fieldGroupControls.add(
					this.newOperationParamControl(
						fieldGroup.refGetPath().getLastSegment().toClassicRepresentation(),
						locale,
						localeAsIndex,
						fieldGroup
					)
				);
			}
		}
	}

    /**
     * Get new component.
     * 
     * @param view
     * @param object
     * @return
     */
    public UiOperationTab newComponent(
    	ObjectView view,
    	Object object
    ) {
    	return new UiOperationTab(
        	this,
            view,
            object
		);    	
    }
    
    /**
     * Create new instance of OperationParamControl. Override
     * for custom-specific implementation.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param fieldGroupDef
     * @return
     */
    protected UiOperationParamControl newOperationParamControl(
        String id,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.FieldGroup fieldGroupDef	    		
    ) {
    	return new UiOperationParamControl(
    		id,
    		locale,
    		localeAsIndex,
    		fieldGroupDef
    	);
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
    	List<UiOperationParamControl> children = this.getChildren(UiOperationParamControl.class);
    	if(
    		// Do not display inline for operations with empty result
    		(!this.isQuery && (children.isEmpty() || (children.size() == 2 && children.get(1).getFields().isEmpty()))) ||
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
        } else if(Ui_1.DELETE_OBJECT_OPERATION_NAME.equals(this.getOperationName())) { 
            return view.getObjectReference().getDeleteObjectAction();
        } else if(Ui_1.RELOAD_OBJECT_OPERATION_NAME.equals(this.getOperationName())) {
            return view.getObjectReference().getReloadAction();
        } else if(Ui_1.NAVIGATE_TO_PARENT_OPERATION_NAME.equals(this.getOperationName())) {
            return view.getObjectReference().getSelectParentAction();
        } else {
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
    	Action invokeOperationAction = this.getInvokeOperationAction(view, app);
        return new Action(
            UiGetOperationDialogAction.EVENT_ID,
            new Action.Parameter[]{
                new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getObjectReference().getXRI()),
                new Action.Parameter(Action.PARAMETER_ID, Integer.toString(this.getTabId()))
            },
            invokeOperationAction.getTitle(),
            invokeOperationAction.isEnabled()
        );
    }

    /**
     * Test permission for operation and action.
     * 
     * @param object
     * @param app
     * @param action
     * @return
     */
    public boolean hasPermission(
    	RefObject_1_0 object,
    	ApplicationContext app,
    	String action
    ) {
    	return app.getPortalExtension().hasPermission(
            this.operationName,
            object,
            app,
            action
        );    	
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
