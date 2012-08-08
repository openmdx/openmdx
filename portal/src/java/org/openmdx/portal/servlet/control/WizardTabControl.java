/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: WizardTabControl.java,v 1.11 2009/02/11 12:57:20 wfro Exp $
 * Description: WizardTabControl
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/11 12:57:20 $
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
package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.wizards.WizardDefinition;

public class WizardTabControl
  extends OperationTabControl
  implements Serializable {

    //-----------------------------------------------------------------------
    public WizardTabControl(
        String id,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.OperationTab tab,
        ControlFactory controlFactory,
        WizardDefinition definition,
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
        this.wizardDefinition = definition;
    }

    //-----------------------------------------------------------------------
    public String getName(
    ) {
        return this.wizardDefinition.getName();
    }

    //-----------------------------------------------------------------------
    public String getOperationName(
    ) {        
        return (this.labels != null) && (this.wizardDefinition.getLabel() == null) ? 
            super.getName() : 
            this.wizardDefinition.getLabel();
    }

    //-----------------------------------------------------------------------
    public String getToolTip(
    ) {
        return (this.labels != null) && (this.wizardDefinition.getToolTip() == null) ? 
            super.getToolTip() : 
            this.wizardDefinition.getToolTip();            
    }

    //-----------------------------------------------------------------------
    @Override
    public void paint(
        HtmlPage p, 
        String frame, 
        boolean forEditing
    ) throws ServiceException {
        ShowObjectView view = (ShowObjectView)p.getView();      
        ApplicationContext app = view.getApplicationContext();
        if(frame == null) {
            int operationIndex = 100*(this.getPaneIndex() + 1) + this.getTabIndex();        
            String objectXri = view.getObjectReference().getObject().refMofId();     
            String encodedObjectXri = objectXri;
            try {
                encodedObjectXri = URLEncoder.encode(objectXri, "UTF-8");
            }
            catch(UnsupportedEncodingException e) {}
            if(app.getPortalExtension().isEnabled(this.getName(), view.getRefObject(), app)) {
                if(this.wizardDefinition.getOpenParameter().length() > 0) {
                    p.write("    <li><a href=\"#\" onclick=\"javascript:window.open('.", this.getName(), "?", Action.PARAMETER_OBJECTXRI, "=", encodedObjectXri, "&", Action.PARAMETER_REQUEST_ID, "=", view.getRequestId(), "', '", this.getOperationName(), "', '", this.wizardDefinition.getOpenParameter(), "');\" id=\"opTab", Integer.toString(operationIndex), "\">", this.getOperationName(), "...</a></li>");                    
                }
                else {
                    String targetType = this.wizardDefinition.getTargetType();
                    String parameters = null;
                    String operationName = super.getOperationName();
                    if((operationName != null) && operationName.indexOf("?") > 0) {
                        parameters = operationName.substring(operationName.indexOf("?") + 1);
                    }                    
                    if("_inplace".equals(targetType)) {
                        p.write("    <li><a href=\"#\" onclick=\"javascript:new Ajax.Updater('UserDialog', '.", this.getName(), "?", Action.PARAMETER_OBJECTXRI, "=", encodedObjectXri, "&", Action.PARAMETER_REQUEST_ID, "=", view.getRequestId(), (parameters == null ? "" : "&" + parameters), "', {evalScripts: true});\" id=\"opTab", Integer.toString(operationIndex), "\">", this.getOperationName(), "...</a></li>");
                    }
                    else {
                        p.write("    <li><a href=\".", this.getName(), "?", Action.PARAMETER_OBJECTXRI, "=", encodedObjectXri, "&", Action.PARAMETER_REQUEST_ID, "=", view.getRequestId(), (parameters == null ? "" : "&" + parameters), "\" target=\"", this.wizardDefinition.getTargetType(), "\" id=\"opTab", Integer.toString(operationIndex), "\">", this.getOperationName(), "...</a></li>");                        
                    }
                }
            }
            else {
                p.write("    <li><a href=\"#\" id=\"opTab", Integer.toString(operationIndex), "\"><span>", this.getName(), "</span></a></li>");                
            }
        }
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private final WizardDefinition wizardDefinition;

}
