/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ObjectTitleControl.java,v 1.28 2008/05/01 21:43:55 wfro Exp $
 * Description: ObjectTitleControl
 * Revision:    $Revision: 1.28 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/01 21:43:55 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 20042007, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of CRIXP Corp. nor the names of the contributors
 * to openCRX may be used to endorse or promote products derived
 * from this software without specific prior written permission
 * 
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
 * This product includes software developed by contributors to
 * openMDX (http://www.openmdx.org/)
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.portal.servlet.control;

import java.io.Serializable;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.texts.Texts_1_0;
import org.openmdx.portal.servlet.view.ObjectView;

public class ObjectTitleControl
    extends Control
    implements Serializable {

    //-------------------------------------------------------------------------
    public ObjectTitleControl(
        String id,
        String locale,
        int localeAsIndex,
        ControlFactory controlFactory
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            controlFactory
        );
    }
    
    //---------------------------------------------------------------------------------
    @Override
    public void paint(
        HtmlPage p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {
        AppLog.detail("> paint");

        ObjectView view = (ObjectView)p.getView();
        ApplicationContext app = view.getApplicationContext();
        HtmlEncoder_1_0 htmlEncoder = app.getHtmlEncoder();
        String guiLook = app.getCurrentGuiMode();
        Texts_1_0 texts = app.getTexts();
        boolean noLayoutManager = 
            guiLook.equals(WebKeys.SETTING_GUI_MODE_BASIC);
            
        p.write("<table class=\"wide\">");
        if(noLayoutManager) {
            p.write("  <tr><td id=\"inspBreadcrum\" colspan=\"3\"></td></tr>");
        }
        p.write("  <tr>");
        if(!forEditing) {
            if((view.getLookupType() != null) && view.getObjectReference().isInstanceof(view.getLookupType())) {
                p.write("    <td id=\"inspSelector\">");
                p.write("      <input type=\"checkbox\" name=\"objselect\" value=\"obj\"", p.getOnClick("OF.selectAndClose('", view.getObjectReference().refMofId(), "', '", htmlEncoder.encode(view.getObjectReference().getTitleEscapeQuote(), false), "', '", view.getId(), "', window);"), " />");
                p.write("    </td>");
            }
            p.write("    <td id=\"inspIcon\">");
            p.write("      <a href=\"\"", p.getOnMouseOver("javascript:this.href=", p.getEvalHRef(view.getObjectReference().getReloadAction()), ";") + " title=\"", texts.getReloadText(), "\">", p.getImg("src=\"", p.getResourcePath("images/"), view.getObjectReference().getIconKey(), "\" border=\"0\" align=\"bottom\" alt=\"o\" title=\"\"") + "</a>");
            p.write("    </td>");
        }
        p.write("    <td id=\"inspTitle\">");
        if(view.getObjectReference().getObject() instanceof org.openmdx.base.jmi1.Segment) {
            p.write("      ", view.getObjectReference().getTitle());            
        }
        else {
            p.write("      ", view.getObjectReference().getTitle(), " - ", view.getObjectReference().getLabel());            
        }
        if(this.showPerformance) {
            p.write("      <input type=\"submit\" onclick=\"javascript:alert('execution times d1='+(t1-t0)+'; d2='+(t2-t1)+'; d3='+(t3-t2)+'; total='+(t3-t0));return;\" value=\"Execution times\">");
        }
        p.write("    </td>");    
        if(!forEditing) {
            Action backAction = view.getBackAction();
            if(backAction != null) {
                p.write("    <td id=\"inspClose\"><a href=\"\"", p.getOnClick("javascript:this.href=", p.getEvalHRef(backAction), ";") + " title=\"", backAction.getTitle(), "\">&nbsp;X&nbsp;</a></td>");
            }
        }
        p.write("  </tr>");
        p.write("</table>");
                
        AppLog.detail("< paint");
    }
    
    //---------------------------------------------------------------------------------
    public void setShowPerformance(
        boolean newValue
    ) {
        this.showPerformance = newValue;
    }
    
    //---------------------------------------------------------------------------------
    // Members
    //---------------------------------------------------------------------------------
    private static final long serialVersionUID = 4455336391780863135L;
    
    private boolean showPerformance = false;

}
