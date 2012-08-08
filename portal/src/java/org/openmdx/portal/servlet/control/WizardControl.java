/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: WizardControl.java,v 1.28 2009/10/21 17:16:10 wfro Exp $
 * Description: WizardControl
 * Revision:    $Revision: 1.28 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/10/21 17:16:10 $
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

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.wizards.WizardDefinition;

//-----------------------------------------------------------------------------
public class WizardControl
    extends Control
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public WizardControl(
        String id,
        String locale,
        int localeAsIndex,
        ControlFactory controlFactory,
        WizardDefinition[] wizardDefinitions
    ) {
        super(
            id,
            locale,
            localeAsIndex
        );
        this.wizardTabs = new WizardTabControl[wizardDefinitions.length];
        for(int i = 0; i < wizardDefinitions.length; i++) {
            this.wizardTabs[i] = controlFactory.createWizardTabControl(
                null,
                locale,
                localeAsIndex,
                null,
                wizardDefinitions[i],
                WIZARD_PANE_INDEX,
                i                
            );
        }
    }
  
    //-----------------------------------------------------------------------
    public Action getInvokeWizardAction(
        String identity
    ) {
        Action action = 
            new Action(
                Action.EVENT_INVOKE_WIZARD, 
                new Action.Parameter[]{
                    new Action.Parameter(Action.PARAMETER_OBJECTXRI, identity)
                },
                "",
                true
            );
        return action;
    }
    
    //-------------------------------------------------------------------------
    @Override
    public void paint(
        ViewPort p,
        String frame,
        boolean forEditing
    ) throws ServiceException {
    	SysLog.detail("> paint");

        // Wizard menu entries
        if(frame == null) {
            if(this.wizardTabs.length > 0) {            
                p.write("<li><a href=\"#\">Wizards&nbsp;&nbsp;&nbsp;</a>");
                p.write("  <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
                for(
                    int i = 0; 
                    i < this.wizardTabs.length; 
                    i++
                ) {
                    this.wizardTabs[i].paint(
                        p,
                        frame,
                        forEditing
                   );
                }
                p.write("  </ul>");
                p.write("</li>");
            }
        }                   
        SysLog.detail("< paint");        
    }

    //-------------------------------------------------------------------------
    public WizardTabControl[] getWizardTabControls(
    ) {
    	return this.wizardTabs;
    }
    
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = -7785589508766566304L;
    
    private static final int WIZARD_PANE_INDEX = 3000;
    
    private final WizardTabControl[] wizardTabs;
}

//--- End of File -----------------------------------------------------------
