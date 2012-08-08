/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Name:        $Id: AttributePane.java,v 1.7 2008/08/12 16:38:07 wfro Exp $
 * Description: CompositeGrid
 * Revision:    $Revision: 1.7 $
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * Date:        $Date: 2008/08/12 16:38:07 $
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
package org.openmdx.portal.servlet.view;

import java.io.Serializable;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.control.AttributePaneControl;

public class AttributePane 
    extends ControlState
    implements Serializable {
    
    //-----------------------------------------------------------------------
    public AttributePane(
        AttributePaneControl control,
        ObjectView view,
        Object object
    ) {
        super(
            control,
            view
        );
        this.attributeTab = new AttributeTab[control.getAttributeTabControl().length];
        for(int i = 0; i < this.attributeTab.length; i++) {
            this.attributeTab[i] = new AttributeTab(
                control.getAttributeTabControl()[i],
                view,
                this,
                object
            );
        }
    }
    
    //-----------------------------------------------------------------------
    public AttributeTab[] getAttributeTab(
    ) {
        return this.attributeTab;
    }
    
    //-----------------------------------------------------------------------
    public AttributePaneControl getAttributePaneControl(
    ) {
        return (AttributePaneControl)this.control;
    }
    
    //-------------------------------------------------------------------------
    public void refresh(
        boolean refreshData
    ) throws ServiceException {
        for(int i = 0; i < this.attributeTab.length; i++) {
            this.attributeTab[i].refresh(refreshData);
        }
    }
    
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = -447974858493592293L;

    protected final AttributeTab[] attributeTab;

}
