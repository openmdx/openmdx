/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Name:        $Id: OperationTab.java,v 1.9 2007/08/09 17:14:41 wfro Exp $
 * Description: CompositeGrid
 * Revision:    $Revision: 1.9 $
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * Date:        $Date: 2007/08/09 17:14:41 $
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
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.portal.servlet.view;

import java.io.Serializable;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.control.OperationTabControl;
import org.openmdx.ui1.layer.application.Ui_1;

public class OperationTab 
    extends ControlState
    implements Serializable {
    
    //-----------------------------------------------------------------------    
    public OperationTab(
        OperationTabControl control,
        ObjectView view,
        OperationPane operationPane,
        Object object
    ) {
        super(
            control,
            view
        );
        this.operationPane = operationPane;
        this.fieldGroup = new FieldGroup[control.getFieldGroupControl().length];
        for(int i = 0; i < this.fieldGroup.length; i++) {
            this.fieldGroup[i] = new FieldGroup(
                control.getFieldGroupControl()[i],
                view,
                object
            );
        }
    }

    //-----------------------------------------------------------------------    
    public OperationTabControl getOperationTabControl(
    ) {
        return (OperationTabControl)this.control;
    }
    
    //-----------------------------------------------------------------------    
    public FieldGroup[] getFieldGroup(
    ) {
        return this.fieldGroup;
    }
    
    //-------------------------------------------------------------------------
    public void refresh(
        boolean refreshData
    ) throws ServiceException {
        for(int i = 0; i < this.fieldGroup.length; i++) {
            this.fieldGroup[i].refresh(refreshData);
        }
    }  
  
    //-----------------------------------------------------------------------    
    private static final long serialVersionUID = 4397901261495693L;
    
    protected final OperationPane operationPane;
    protected final FieldGroup[] fieldGroup;
    
}