/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: ContainerControl
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
import java.util.Iterator;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.ViewPort;

public class PanelControl
    extends ContainerControl
    implements Serializable {

  //-------------------------------------------------------------------------
  public PanelControl(
        String id,
        String locale,
        int localeAsIndex
    ) {
        super(
            id,
            locale,
            localeAsIndex
        );
        this.layout = LAYOUT_NONE;
  }

    //-------------------------------------------------------------------------
    @Override
    public void paint(
        ViewPort p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {

        // Contained controls
        if(this.layout != LAYOUT_NONE) {
            p.write("<table id=\"", this.id, "\" ", this.tableStyle, ">");
        }
        if(this.layout == LAYOUT_HORIZONTAL) {
            p.write("<tr>");
        }
        int ii = 0;
        for(
            Iterator i = this.controls.iterator();
            i.hasNext();
            ii++
        ) {
            Control control = (Control)i.next();
            if(this.layout == LAYOUT_VERTICAL) {
                p.write("<tr>");
            }
            if(this.layout != LAYOUT_NONE) {
                p.write("<td>");
            }
            control.paint(
                p,
                (String)this.frames.get(ii),
                forEditing
            );
            if(this.layout != LAYOUT_NONE) {
                p.write("</td>");
            }
            if(this.layout == LAYOUT_VERTICAL) {
                p.write("</tr>");
            }
        }
        if(this.layout == LAYOUT_HORIZONTAL) {
            p.write("</tr>");
        }
        if(this.layout != LAYOUT_NONE) {
            p.write("</table>");
        }
    }

    //-------------------------------------------------------------------------
    public void setLayout(
        int layout
    ) {
        this.layout = layout;
    }
    
    //-------------------------------------------------------------------------
    public void setTableStyle(
        String tableStyle
    ) {
        this.tableStyle = tableStyle;
    }
    
    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = -1231427781842708999L;

    public static final int LAYOUT_NONE = 0;
    public static final int LAYOUT_HORIZONTAL = 1;
    public static final int LAYOUT_VERTICAL = 2;
    
    public final static int PANEL_STATE_SHOW = 0;
    public final static int PANEL_STATE_HIDE = 1;
    
    private int layout;
    private String tableStyle = "";

}
