/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: WizardTabControl
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.PortalExtension_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.component.ObjectView;
import org.openmdx.portal.servlet.component.ShowObjectView;
import org.openmdx.portal.servlet.component.UiWizardTab;
import org.openmdx.portal.servlet.wizards.WizardDefinition;

/**
 * WizardTabControl
 *
 */
public class UiWizardTabControl extends UiOperationTabControl implements Serializable {

	/**
     * Constructor.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param tabDef
     * @param controlFactory
     * @param definition
     * @param paneIndex
     * @param tabIndex
     */
    public UiWizardTabControl(
        String id,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.OperationTab tabDef,
        PortalExtension_1_0.ControlFactory controlFactory,
        WizardDefinition definition,
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
        this.wizardDefinition = definition;
    }

    
    /* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.control.OperationTabControl#newComponent(org.openmdx.portal.servlet.component.ObjectView, java.lang.Object)
	 */
	@Override
	public UiWizardTab newComponent(
		ObjectView view, 
		Object object
	) {
		return new UiWizardTab(
			this,
			view,
			object
		);
	}

	/* (non-Javadoc)
     * @see org.openmdx.portal.servlet.control.TabControl#getName()
     */
    @Override
    public String getName(
    ) {
        return (this.labels != null) && (this.wizardDefinition.getLabel() == null) 
        	? super.getName() 
        	: this.wizardDefinition.getLabel();
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.control.OperationTabControl#getOperationName()
     */
    @Override
    public String getOperationName(
    ) {        
        return this.wizardDefinition.getName();
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.control.OperationTabControl#getQualifiedOperationName()
     */
    @Override
    public String getQualifiedOperationName(
    ) {
    	String qualifiedOperationName = super.getQualifiedOperationName();
    	return qualifiedOperationName == null ?
    		this.wizardDefinition.getName() :
    			qualifiedOperationName;
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.control.OperationTabControl#getToolTip()
     */
    @Override
    public String getToolTip(
    ) {
        return (this.labels != null) && (this.wizardDefinition.getToolTip() == null) ? 
            super.getToolTip() : 
            this.wizardDefinition.getToolTip();            
    }

    /**
     * Return true for in-place rendering.
     * 
     * @return
     */
    public boolean isInplace(
    ) {
        String targetType = this.wizardDefinition.getTargetType();
    	return "_inplace".equals(targetType);
    }
    
    /**
     * Get wizard definition.
     * 
     * @return
     */
    public WizardDefinition getWizardDefinition(
    ) {
    	return this.wizardDefinition;
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
        ShowObjectView view = (ShowObjectView)p.getView();      
        ApplicationContext app = view.getApplicationContext();
        if(frame == null) {
            Integer tabId = this.getTabId();        
            String objectXri = view.getObjectReference().getObject().refMofId();     
            String encodedObjectXri = objectXri;
            try {
                encodedObjectXri = URLEncoder.encode(objectXri, "UTF-8");
            } catch(UnsupportedEncodingException e) {}
            boolean isRevokeShow = app.getPortalExtension().hasPermission(
            	this.getQualifiedOperationName(), 
            	view.getObject(),
            	app,
            	WebKeys.PERMISSION_REVOKE_SHOW
            ); 
            boolean isRevokeEdit = app.getPortalExtension().hasPermission(
            	this.getQualifiedOperationName(), 
            	view.getObject(),
            	app,
            	WebKeys.PERMISSION_REVOKE_EDIT
            ); 
            if(!isRevokeShow && !isRevokeEdit) {
                if(this.wizardDefinition.getOpenParameter() != null && !this.wizardDefinition.getOpenParameter().isEmpty()) {
                    p.write("    <a href=\"#\" class=\"" + CssClass.dropdown_item.toString() + "\" onclick=\"javascript:window.open('.", this.getOperationName(), "?", Action.PARAMETER_OBJECTXRI, "=", encodedObjectXri, "&", Action.PARAMETER_REQUEST_ID, "=", view.getRequestId(), "', '", this.getOperationName(), "', '", this.wizardDefinition.getOpenParameter(), "');\" id=\"op", Integer.toString(tabId), "\">", this.getName(), "...</a>");                    
                } else {
                    String parameters = null;
                    String operationName = super.getOperationName();
                    if((operationName != null) && operationName.indexOf("?") > 0) {
                        parameters = operationName.substring(operationName.indexOf("?") + 1);
                    }                    
                    if(this.isInplace()) {
                        p.write("    <a href=\"#\" class=\"" + CssClass.dropdown_item.toString() + "\" onclick=\"javascript:jQuery.ajax({type: 'get', url: '.", this.getOperationName(), "?", Action.PARAMETER_OBJECTXRI, "=", encodedObjectXri, "&", Action.PARAMETER_REQUEST_ID, "=", view.getRequestId(), (parameters == null ? "" : "&" + parameters), "', dataType: 'html', success: function(data){$('UserDialog').innerHTML=data;evalScripts(data);}});\" id=\"op", Integer.toString(tabId), "\">", this.getName(), "...</a>");
                    } else {
                        p.write("    <a href=\".", this.getOperationName(), "?", Action.PARAMETER_OBJECTXRI, "=", encodedObjectXri, "&", Action.PARAMETER_REQUEST_ID, "=", view.getRequestId(), (parameters == null ? "" : "&" + parameters), "\" class=\"" + CssClass.dropdown_item.toString() + "\" target=\"", this.wizardDefinition.getTargetType(), "\" id=\"op", Integer.toString(tabId), "\">", this.getName(), "...</a>");
                    }
                }
            } else if(!isRevokeShow) {
                p.write("    <a href=\"#\" class=\"" + CssClass.dropdown_item.toString() + " " + CssClass.dropdown_item.toString() + "\" id=\"op", Integer.toString(tabId), "\"><span>", this.getName(), "</span></a>");
            }
        }
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
	private static final long serialVersionUID = -1790885812663078493L;

    private final WizardDefinition wizardDefinition;

}
