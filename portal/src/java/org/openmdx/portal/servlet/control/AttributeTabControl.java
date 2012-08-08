/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: AttributeTabControl.java,v 1.13 2009/09/25 12:02:38 wfro Exp $
 * Description: TabControl
 * Revision:    $Revision: 1.13 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/25 12:02:38 $
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

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.view.ObjectView;

public class AttributeTabControl
  extends TabControl
  implements Serializable {
  
  //-------------------------------------------------------------------------
  public AttributeTabControl(
      String id,
      String locale,
      int localeAsIndex,
      ControlFactory controlFactory,
      org.openmdx.ui1.jmi1.Tab tab,
      int tabIndex
  ) {
    super(
        id,
        locale,
        localeAsIndex,
        controlFactory,
        tab,
        0,
        tabIndex
    );
  }
  
    //-------------------------------------------------------------------------
    @Override
    public void paint(
        ViewPort p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {
        RefObject_1_0 refObj = p.getView() instanceof ObjectView ?
            ((ObjectView)p.getView()).getRefObject() :
            null;
        for(
            int i = 0; 
            i < this.getFieldGroupControl().length; 
            i++
        ) {
            FieldGroupControl fieldGroup = this.getFieldGroupControl()[i];
            if(
                p.getApplicationContext().getPortalExtension().isEnabled(fieldGroup, refObj, p.getApplicationContext())) {
                fieldGroup.paint(
                    p,
                    frame,
                    forEditing
                );
            }
        }
    }
    
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3257567312814158642L;
}

//--- End of File -----------------------------------------------------------
