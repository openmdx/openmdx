/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ReportTabControl.java,v 1.8 2008/11/12 10:36:53 wfro Exp $
 * Description: WizardTabControl
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/12 10:36:53 $
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
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.attribute.DateValue;
import org.openmdx.portal.servlet.reports.ReportDefinition;
import org.openmdx.portal.servlet.texts.Texts_1_0;

public class ReportTabControl
    extends OperationTabControl
    implements Serializable {

    //-----------------------------------------------------------------------
    public ReportTabControl(
      String id,
      String locale,
      int localeAsIndex,
      ControlFactory controlFactory,
      ReportDefinition definition,
      int paneIndex,
      int tabIndex
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            controlFactory,
            null,
            paneIndex,
            tabIndex
        );
        this.reportDefinition = definition;
    }

    //-----------------------------------------------------------------------
    public String getOperationName(
    ) {
        return this.reportDefinition.getLabel();
    }

    //-----------------------------------------------------------------------
    public String getToolTip(
    ) {
        return this.reportDefinition.getToolTip();
    }

    //-----------------------------------------------------------------------
    public String getName(
    ) {
        return this.reportDefinition.getName();
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void paint(
        HtmlPage p, 
        String frame, 
        boolean forEditing
    ) throws ServiceException {
        ApplicationContext application = p.getApplicationContext();
        Texts_1_0 texts = application.getTexts();
        
        // Report menu entries
        if(frame == null) {
            String operationId = Integer.toString(100*(this.getPaneIndex() + 1) + this.getTabIndex());     
            p.write("    <li><a href=\"#\"", " id=\"op", operationId, "Trigger\" onclick=\"javascript:$('op", operationId, "Dialog').style.display='block';if(!op", operationId, "Dialog){op", operationId, "Dialog = new YAHOO.widget.Panel('op", operationId, "Dialog', {zindex:20000, fixedcenter:true, close:true, visible:false, constraintoviewport:true, modal:true}); op", operationId, "Dialog.cfg.queueProperty('keylisteners', new YAHOO.util.KeyListener(document, {keys:27}, {fn:op", operationId, "Dialog.hide, scope:op", operationId, "Dialog, correctScope:true})); op", operationId, "Dialog.render();} op", operationId, "Dialog.show();\">", this.getToolTip(), "...</a></li>");
        }        
        // Report input fields
        else if(FRAME_PARAMETERS.equals(frame)) {
            int operationIndex = 100*(this.getPaneIndex() + 1) + this.getTabIndex();        
            String formId = "op" + operationIndex + "Form";
            p.write("<script language=\"javascript\" type=\"text/javascript\">");
            p.write("  var op", Integer.toString(operationIndex), "Dialog = null;");
            p.write("</script>");            
            p.write("<div id=\"op", Integer.toString(operationIndex), "Dialog\" class=\"opDialog\">");
            p.write("  <div class=\"hd\">", this.getToolTip(), "</div> <!-- name of operation -->");
            p.write("  <div class=\"bd\">");                        
            p.write("    <form id=\"", formId, "\" name=\"", formId, "\" action=\"", this.reportDefinition.getAction(), "\" enctype=\"multipart/form-data\" accept-charset=\"utf-8\" method=\"get\" target=\"_blank\" >");
            p.write("      <input type=\"hidden\" name=\"__report\" value=\"", this.getName(), "\">");
            ReportDefinition.Parameter[] parameters = this.reportDefinition.getParameter();
            p.write("      <div class=\"opFieldGroupName\">Report parameters</div>");
            p.write("      <table class=\"opFieldGroup\">");
            int tabIndex = 1;
            for(
                int v = 0; 
                v < parameters.length; 
                v++
            ) {
                p.write("      <tr>");
                ReportDefinition.Parameter parameter = parameters[v];
                String label = parameter.getLabel();
                label += label.length() == 0 ? "" : ":";
                String stringifiedValue = parameter.getDefaultValue();
                String field = parameter.getName();                
                p.write("          <td class=\"label\"><span class=\"nw\">", label, "</span></td>");
                if("dateTime".equals(parameter.getDataType())) {
                    int calId = operationIndex*100 + tabIndex;
                    String calendarFormat = DateValue.getCalendarFormat(
                        DateValue.getLocalizedDateTimeFormatter(null, true, application)
                    );
                    p.write("          <td>");
                    p.write("          </td>");
                    p.write("          <td class=\"addon\">");
                    p.write("            <a>", p.getImg("class=\"popUpButton\" id=\"cal_trigger", Integer.toString(calId), "\" border=\"0\" alt=\"Click to open Calendar\" src=\"", p.getResourcePath("images/"), "cal", p.getImgType(), "\""), "</a>");
                    p.write("            <script language=\"javascript\" type=\"text/javascript\">");
                    p.write("              Calendar.setup({");
                    p.write("                inputField   : \"cal_field", Integer.toString(calId), "\",");
                    p.write("                ifFormat     : \"", calendarFormat, "\",");
                    p.write("                timeFormat   : \"24\",");
                    p.write("                button       : \"cal_trigger", Integer.toString(calId), "\",");
                    p.write("                align        : \"Tr\",");
                    p.write("                singleClick  : true,");
                    p.write("                showsTime    : true");
                    p.write("              });");
                    p.write("            </script>");
                    p.write("          </td>");
                }
                else {
                    p.write("          <td>");
                    p.write("            <input type=\"text\" class=\"valueL\" tabindex=\"", Integer.toString(tabIndex), "\" name=\"", field, "\" value=\"", stringifiedValue, "\">");                    
                    p.write("          </td>");
                    p.write("          <td class=\"addon\"></td>");
                }
                p.write("        </tr>");
                tabIndex++;
            }
            if(this.reportDefinition.askForReportFormat()) {
                p.write("        <tr>");
                p.write("          <td class=\"label\"><span class=\"nw\">Report type:</span></td>");
                p.write("          <td>");
                p.write("            <select class=\"valueL\" name=\"__format\" tabindex=\"", Integer.toString(tabIndex), "\">");
                p.write("              <option  value=\"html\">HTML");
                p.write("              <option  value=\"pdf\">PDF");
                p.write("              <option  value=\"xml\">XML");
                p.write("            </select>");
                p.write("          </td>");
                p.write("          <td class=\"addon\"></td>");
                p.write("        </tr>");
                tabIndex++;
            }
            p.write("      </table>");
            p.write("      <div style=\"text-align:right;padding-top:8px;\">");
            p.write("        <input type=\"submit\" value=\"", texts.getOkTitle(), "\" onclick=\"javascript:op", Integer.toString(operationIndex), "Dialog.hide();\" />");
            p.write("        <input type=\"button\" value=\"", texts.getCancelTitle(), "\" onclick=\"javascript:op", Integer.toString(operationIndex), "Dialog.hide();\" />");
            p.write("      </div>");                                    
            p.write("    </form>");   
            p.write("  </div>");
            p.write("</div>");
        }
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private final ReportDefinition reportDefinition;
    
}
