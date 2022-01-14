/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Description: OperationTab
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
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
import java.util.Collections;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.control.UiOperationParamControl;
import org.openmdx.portal.servlet.control.UiOperationTabControl;
import org.openmdx.ui1.layer.application.Ui_1;

/**
 * OperationTab
 *
 */
public class UiOperationTab extends Component implements Serializable {
    
    /**
     * Constructor.
     * 
     * @param control
     * @param view
     * @param object
     */
    public UiOperationTab(
        UiOperationTabControl control,
        ObjectView view,
        Object object
    ) {
        super(
            control,
            view
        );
        List<UiOperationParam> operationParams = new ArrayList<UiOperationParam>();
		for(UiOperationParamControl operationParamControl: control.getChildren(UiOperationParamControl.class)) {
			operationParams.add(
				operationParamControl.newComponent(
	                view,
	                object
	            )
            );
        }
        this.operationParams = operationParams;
    }

    /**
     * Get control casted to OperationTabControl.
     * 
     * @return
     */
    protected UiOperationTabControl getOperationTabControl(
    ) {
    	return (UiOperationTabControl)this.control;
    }

    /**
     * Get operation name.
     * 
     * @return
     */
    public String getOperationName(
    ) {
        return this.getOperationTabControl().getOperationName();
    }

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.view.Component#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Component> List<T> getChildren(
		Class<T> type
	) {
		if(type == UiOperationParam.class) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)this.operationParams;
			return children;
		} else {
			return Collections.emptyList();
		}
	}

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.Canvas#refresh(boolean)
     */
    @Override
    public void refresh(
        boolean refreshData
    ) throws ServiceException {
    	for(UiOperationParam operationParam: this.getChildren(UiOperationParam.class)) {
    		operationParam.refresh(refreshData);
        }
    }

    /**
     * Get name.
     * 
     * @return
     */
    public String getName(
    ) {
    	return this.getOperationTabControl().getName();
    }

    /**
     * Get tool tip.
     * 
     * @return
     */
    public String getToolTip(
    ) {
    	return this.getOperationTabControl().getToolTip();
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
    	return this.getOperationTabControl().getInvokeOperationAction(view, app);
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
    	return this.getOperationTabControl().confirmExecution(app);
    }

    /**
     * Return true if result of this operation is displayed inline.
     * 
     * @return
     */
    public boolean displayOperationResult(
    ) {
    	return this.getOperationTabControl().displayOperationResult();
    }
    
    /* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.component.Component#paint(org.openmdx.portal.servlet.ViewPort, java.lang.String, boolean)
	 */
    @Override
    public void paint(
        ViewPort p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {
        ApplicationContext app = p.getApplicationContext();
        ShowObjectView view = (ShowObjectView)p.getView();
        UiOperationTabControl control = this.getOperationTabControl();
        // Operation menues
        if(frame == null) {
            String operationId = Integer.toString(control.getTabId());
            List<UiOperationParam> operationParams = this.getChildren(UiOperationParam.class);
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
        	boolean isRevokeObjectEdit = control.hasPermission(
                view.getObjectReference().getObject(),
                app,
                WebKeys.PERMISSION_REVOKE_EDIT
            );
            if(
            	!isRevokeEdit && 
            	!isRevokeShow && 
            	!(Ui_1.EDIT_OBJECT_OPERATION_NAME.equals(control.getOperationName()) && isRevokeObjectEdit)
            ) {
                // No input parameters so do not get operation dialog
                if(operationParams.isEmpty() && !control.confirmExecution(app)) {
                    Action invokeOperationAction = control.getInvokeOperationAction(
                        view,
                        app
                    );
                    if(Ui_1.EDIT_OBJECT_OPERATION_NAME.equals(control.getOperationName())) {
                    	if(invokeOperationAction.isEnabled()) {
                    		p.write("    <a href=\"#\" class=\"" + CssClass.dropdown_item.toString() + "\" onclick=\"javascript:jQuery.ajax({type: 'get', url: ", p.getEvalHRef(invokeOperationAction), ", dataType: 'html', success: function(data){$('aPanel').innerHTML=data;evalScripts(data);}});return false;\" id=\"opTab", operationId, "\" >", control.getName(), "</a>");
                    	} else {
                    		p.write("    <a href=\"#\" class=\"" + CssClass.disabled.toString() + " " + CssClass.dropdown_item.toString() + "\" id=\"opTab", operationId, "\" >", control.getName(), "</a>");
                    	}
                    } else {
                    	if(invokeOperationAction.isEnabled()) {
                    		p.write("    <a href=\"#\" class=\"" + CssClass.dropdown_item.toString() + "\" onmouseover=\"javascript:this.href=", p.getEvalHRef(invokeOperationAction), ";onmouseover=function(){};\" id=\"opTab", operationId, "\" >", control.getName(), "</a>");
                    	} else {
                    		p.write("    <a href=\"#\" class=\"" + CssClass.disabled.toString() + " " + CssClass.dropdown_item.toString() + "\" id=\"opTab", operationId, "\" >", control.getName(), "</a>");                    		
                    	}
                    }
                } else {
                    // Standard operation with input parameters. Retrieve operation dialog with async request.
                    Action getOperationDialogAction = control.getGetOperationDialogAction(
                        view,
                        app
                    );
                    if(getOperationDialogAction.isEnabled()) {
                    	p.write("    <a href=\"#\" class=\"" + CssClass.dropdown_item.toString() + "\" onclick=\"javascript:jQuery.ajax({type: 'get', url: ", p.getEvalHRef(getOperationDialogAction), ", dataType: 'html', success: function(data){$('OperationDialog').innerHTML=data;evalScripts(data);}});return false;\" id=\"op", operationId, "\" >", control.getName(), "...</a>");
                    } else {
                    	p.write("    <a href=\"#\" class=\"" + CssClass.disabled.toString() + " " + CssClass.dropdown_item.toString() + "\" id=\"op", operationId, "\">", control.getName(), "...</a>");                    	
                    }
                }
            } else if(!isRevokeShow) {
                // Operation can not be invoked. Do not generate onclick or href actions
                // no input parameters
                if(operationParams.isEmpty()) {
                    p.write("    <a href=\"#\" class=\"", CssClass.disabled.toString() + " " + CssClass.dropdown_item.toString(), "\" id=\"opTab", operationId, "\"><span>", control.getName(), "</span></a>");
                } else {
                    // standard operation with input parameters
                    p.write("    <a href=\"#\" class=\"", CssClass.disabled.toString() + " " + CssClass.dropdown_item.toString(),"\" id=\"opTab", operationId, "\" ><span>", control.getName(), "...</span></a>");
                }
            } else {
                // Operation is hidden
            	// no op
            }
        }
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------    
    private static final long serialVersionUID = 4397901261495693L;
    
    protected final List<UiOperationParam> operationParams;

}
