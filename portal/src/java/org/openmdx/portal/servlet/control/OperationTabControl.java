/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: OperationTabControl.java,v 1.40 2009/04/30 11:48:40 wfro Exp $
 * Description: OperationTabControl
 * Revision:    $Revision: 1.40 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/04/30 11:48:40 $
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
import java.util.Map;
import java.util.SortedMap;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.Autocompleter_1_0;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.ObjectReference;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.attribute.AttributeValue;
import org.openmdx.portal.servlet.attribute.BinaryValue;
import org.openmdx.portal.servlet.attribute.BooleanValue;
import org.openmdx.portal.servlet.attribute.CodeValue;
import org.openmdx.portal.servlet.attribute.DateValue;
import org.openmdx.portal.servlet.attribute.ObjectReferenceValue;
import org.openmdx.portal.servlet.attribute.TextValue;
import org.openmdx.portal.servlet.texts.Texts_1_0;
import org.openmdx.portal.servlet.view.FieldGroup;
import org.openmdx.portal.servlet.view.OperationPane;
import org.openmdx.portal.servlet.view.OperationTab;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.view.ViewMode;
import org.openmdx.ui1.layer.application.Ui_1;

public class OperationTabControl
    extends TabControl
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public OperationTabControl(
        String id,
        String locale,
        int localeAsIndex,
        ControlFactory controlFactory,
        org.openmdx.ui1.jmi1.OperationTab tab,
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
        this.operationName = tab == null ? null :((org.openmdx.ui1.jmi1.OperationTab)tab).getOperationName();
    }
  
    //-------------------------------------------------------------------------
    public String getOperationName(
    ) {
        return this.operationName;
    }

    //-------------------------------------------------------------------------
    public String getIconKey(
    ) {
        return this.iconKey.substring(
            this.iconKey.lastIndexOf(":") + 1
        ) + WebKeys.ICON_TYPE;
    }

    //-------------------------------------------------------------------------
    public String getToolTip(
    ) {
        return this.localeAsIndex < this.toolTips.size() ? 
            this.toolTips.get(this.localeAsIndex) : 
            this.toolTips.get(0);
    }
  
    //-------------------------------------------------------------------------
    public boolean confirmExecution(
        ApplicationContext application
    ) {
        return 
            Ui_1.DELETE_OBJECT_OPERATION_NAME.equals(this.getOperationName()) ||
            (Ui_1.RELOAD_OBJECT_OPERATION_NAME.equals(this.getOperationName()) && application.getRoleMapper().isRootPrincipal(application.getCurrentUserRole()));
    }
    
    //-------------------------------------------------------------------------
    public Action getInvokeOperationAction(
        ShowObjectView view,
        ApplicationContext application
    ) throws ServiceException {
        // built-in operations
        if(Ui_1.EDIT_OBJECT_OPERATION_NAME.equals(this.getOperationName())) {
            return view.getObjectReference().getEditObjectAction(ViewMode.EMBEDDED);
        }
        else if(Ui_1.DELETE_OBJECT_OPERATION_NAME.equals(this.getOperationName())) { 
            return view.getObjectReference().getDeleteObjectAction();
        }
        else if(Ui_1.RELOAD_OBJECT_OPERATION_NAME.equals(this.getOperationName())) {
            return view.getObjectReference().getReloadAction();
        }
        else if(Ui_1.NAVIGATE_TO_PARENT_OPERATION_NAME.equals(this.getOperationName())) {
            return view.getObjectReference().getSelectParentAction();
        }
        else {       
            return new Action(
                Action.EVENT_INVOKE_OPERATION,
                new Action.Parameter[]{
                    new Action.Parameter(Action.PARAMETER_PANE, Integer.toString(this.getPaneIndex())),
                    new Action.Parameter(Action.PARAMETER_OBJECTXRI, view.getObjectReference().refMofId()),                
                    new Action.Parameter(Action.PARAMETER_TAB, Integer.toString(this.getTabIndex()))
                },
                "OK",
                application.getPortalExtension().isEnabled(
                    this.getOperationName(),
                    view.getObjectReference().getObject(),
                    application
                )
            );
        }
    }
      
    //-------------------------------------------------------------------------
    @Override
    public void paint(
        HtmlPage p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {
        ApplicationContext application = p.getApplicationContext();
        ShowObjectView view = (ShowObjectView)p.getView();
        OperationPane operationPane = view.getOperationPane()[this.getPaneIndex()];
        OperationTab operationTab = operationPane.getOperationTab()[this.getTabIndex()];
        Texts_1_0 texts = application.getTexts();

        // Operation menues
        if(frame == null) {
            int operationIndex = 100*(this.getPaneIndex() + 1) + this.getTabIndex();
            String operationId = Integer.toString(operationIndex);
            Action invokeOperationAction = this.getInvokeOperationAction(
                view,
                application
            );
            if(invokeOperationAction.isEnabled()) {
                // No input parameters so don't generate dialog
                if((this.getFieldGroupControl().length == 0) && !this.confirmExecution(application)) {
                    if(Ui_1.EDIT_OBJECT_OPERATION_NAME.equals(this.getOperationName())) {
                        p.write("    <li><a href=\"#\" onclick=\"javascript:new Ajax.Updater('aPanel', ", p.getEvalHRef(invokeOperationAction),", {asynchronous:true, evalScripts: true, onComplete: function(){}});return false;\" id=\"opTab", operationId, "\" >", this.getName(), "</a></li>");                        
                    }
                    else {
                        p.write("    <li><a href=\"#\" onmouseover=\"javascript:this.href=", p.getEvalHRef(invokeOperationAction), ";onmouseover=function(){};\" id=\"opTab", operationId, "\" >", this.getName(), "</a></li>");
                    }
                }
                // Standard operation with input parameters
                else {                                         
                    p.write("    <li><a href=\"#\"", " id=\"op", operationId, "Trigger\" onclick=\"javascript:$('op", operationId, "Dialog').style.display='block';if(!op", operationId, "Dialog){op", operationId, "Dialog = new YAHOO.widget.Panel('op", operationId, "Dialog', {zindex:20000, fixedcenter:true, close:true, visible:false, constraintoviewport:true, modal:true}); op", operationId, "Dialog.cfg.queueProperty('keylisteners', new YAHOO.util.KeyListener(document, {keys:27}, {fn:op", operationId, "Dialog.hide, scope:op", operationId, "Dialog, correctScope:true})); op", operationId, "Dialog.render();} op", operationId, "Dialog.show();\">", this.getName(), "...</a></li>");
                }
            }
            // operation is disabled. Do not generate onclick or href actions
            else {
                // no input parameters
                if(this.getFieldGroupControl().length == 0) {
                    p.write("    <li><a href=\"#\" id=\"opTab", operationId, "\" ><span>", this.getName(), "</span></a></li>");
                }
                // standard operation with input parameters
                else {
                    p.write("    <li><a href=\"#\" id=\"opTab", operationId, "\" ><span>", this.getName(), "...</span></a></li>");
                }
            }
        }
        // Operation parameters
        else if(FRAME_PARAMETERS.equals(frame)) {
            Action invokeOperationAction = this.getInvokeOperationAction(
                view, 
                application
            );
            int operationIndex = 100*(this.getPaneIndex() + 1) + this.getTabIndex();
            String operationId = Integer.toString(operationIndex);
            if(invokeOperationAction.isEnabled()) {
                String formId = "op" + operationId + "Form";
                // Operations with no arguments
                if(this.getFieldGroupControl().length == 0) {            
                    // Confirmation prompt
                    if(this.confirmExecution(application)) {
                        p.write("<script language=\"javascript\" type=\"text/javascript\">");
                        p.write("  var op", operationId, "Dialog = null;");
                        p.write("</script>");     
                        p.write("<div id=\"op", operationId, "Dialog\" class=\"opDialog\">");
                        p.write("  <div class=\"hd\">", texts.getAssertExecutionText(), "</div> <!-- name of operation -->");
                        p.write("  <div class=\"bd\">");                        
                        p.write("    <form id=\"", formId, "\" name=\"", formId, "\" action=\"\" enctype=\"multipart/form-data\" accept-charset=\"utf-8\" method=\"post\" >");
                        p.write("      <input type=\"hidden\" name=\"requestId.submit\" value=\"", view.getRequestId(), "\" />");  
                        p.write("      <input type=\"hidden\" name=\"event.submit\" value=\"", Integer.toString(invokeOperationAction.getEvent()), "\" />");
                        p.write("      <input type=\"hidden\" name=\"parameter\" value=\"", invokeOperationAction.getParameter(), "\" />");
                        p.write("      <div id=\"op", operationId, "FormButtons\" style=\"text-align:right;padding-top:8px;\">");
                        p.write("        <input type=\"submit\" value=\"", texts.getOkTitle(), "\" onmouseup=\"javascript:$('op", operationId, "FormWait').style.display='block';$('op", operationId, "FormButtons').style.visibility='hidden';\" />");
                        p.write("        <input type=\"button\" value=\"", texts.getCancelTitle(), "\" onclick=\"javascript:op", operationId, "Dialog.hide();\" />");
                        p.write("      </div>");
                        p.write("      <div id=\"op", operationId, "FormWait\" style=\"text-align:right;display:none;\">");
                        p.write("        <img src=\"images/wait.gif\" alt=\"\" />");
                        p.write("      </div>");                    
                        p.write("    </form>");
                        p.write("  </div>");
                        p.write("</div>");
                    }
                }            
                // Standard operations
                else {
                    p.write("<script language=\"javascript\" type=\"text/javascript\">");
                    p.write("  var op", operationId, "Dialog = null;");
                    p.write("</script>");     
                    p.write("<div id=\"op", operationId, "Dialog\" class=\"opDialog\">");
                    p.write("  <div class=\"hd\">", this.getToolTip(), "</div> <!-- name of operation -->");
                    p.write("  <div class=\"bd\">");                        
                    p.write("    <form id=\"", formId, "\" name=\"", formId, "\" action=\"\" enctype=\"multipart/form-data\" accept-charset=\"utf-8\" method=\"post\" >");
                    // only show field group for input fields
                    for(int k = 0; k < 1; k++) {
                        FieldGroup fieldGroup = operationTab.getFieldGroup()[k];
                        Attribute[][] attributes = fieldGroup.getAttribute();
                        if((attributes != null) && (attributes.length > 0) && (attributes[0].length > 0)) {
                            p.write("<div class=\"opFieldGroupName\">", fieldGroup.getFieldGroupControl().getName(), "</div>");
                            p.write("<div style=\"overflow:auto;\">");
                            p.write("<table class=\"opFieldGroup\">");
                            int nCols = attributes.length;
                            int nRows = nCols > 0 ? attributes[0].length : 0;
                            int tabIndex = 1;
                            for(int v = 0; v < nRows; v++) {
                                p.write("<tr>");
                                for(int u = 0; u < nCols; u++) {
                                    Attribute attribute = attributes[u][v];
                                    String rowSpanModifier = attribute == null
                                        ? ""
                                        : attribute.getSpanRow() > 1 ? "rowspan=\"" + attribute.getSpanRow() + "\"" : "";
                                        if(attribute == null) {
                                            p.write("<td class=\"label\"></td>");
                                            p.write("<td class=\"valueEmpty\">&nbsp;</td>");
                                        }
                                        else if(attribute.isEmpty()) {
                                            p.write("<td class=\"label\"></td>");
                                        }
                                        else {
                                            String label = attribute.getLabel();
                                            label += label.length() == 0 ? "" : ":";
                                            AttributeValue valueHolder = attribute.getValue();
                                            String stringifiedValue = "#ERR";
                                            try {
                                                stringifiedValue = attribute.getStringifiedValue(
                                                    p, 
                                                    true, 
                                                    false
                                                );
                                            } 
                                            catch (Exception e) {
                                                ServiceException e0 = new ServiceException(e);
                                                AppLog.warning(e0.getMessage(), e0.getCause());
                                            }
                                            String field = valueHolder.getName();                       
                                            p.write("<td class=\"label\"><span class=\"nw\">", label, "</span></td>");
                                            // single-valued
                                            if(valueHolder.isSingleValued()) {
                                                if(valueHolder instanceof CodeValue) {
                                                    SortedMap longTextsT = application.getCodes().getLongText(
                                                        field, 
                                                        application.getCurrentLocaleAsIndex(), 
                                                        false, 
                                                        false
                                                    );
                                                    p.write("<td>");
                                                    p.write("  <select class=\"valueL\" name=\"", field, "[", Integer.toString(tabIndex), "]", "\" tabindex=\"", Integer.toString(tabIndex), "\">");
                                                    for(Iterator options = longTextsT.entrySet().iterator(); options.hasNext(); ) {
                                                        Map.Entry option = (Map.Entry)options.next();
                                                        String selectedModifier = option.getKey().equals(valueHolder.getValue(false)) ? "selected" : "";
                                                        p.write("<option ", selectedModifier, " value=\"", option.getKey().toString(), "\">", option.getKey().toString());
                                                    }
                                                    p.write("  </select>");
                                                    p.write("</td>");
                                                    p.write("<td class=\"addon\"></td>");
                                                }
                                                else if(valueHolder instanceof DateValue) {
                                                    DateValue dateValue = (DateValue)valueHolder;
                                                    int calId = operationIndex*100 + tabIndex;
                                                    p.write("<td ", rowSpanModifier, ">");
                                                    p.write("<input type=\"text\" class=\"valueR\" id=\"cal_field" + calId, "\" name=\"", field, "[" + tabIndex, "]", "\" tabindex=\"" + tabIndex, "\" value=\"", stringifiedValue, "\">");
                                                    p.write("</td>");
                                                    p.write("<td class=\"addon\" ", rowSpanModifier, ">");
                                                    if(dateValue.isDate()) {                                          
                                                        String calendarFormat = DateValue.getCalendarFormat(dateValue.getLocalizedDateFormatter(true));                                          
                                                        p.write(p.getImg("class=\"popUpButton\" id=\"cal_trigger" + calId + "\" border=\"0\" alt=\"Click to open Calendar\" src=\"", p.getResourcePath("images/"), "cal", p.getImgType(), "\""));
                                                        p.write("<script language=\"javascript\" type=\"text/javascript\">");
                                                        p.write("  Calendar.setup({");
                                                        p.write("    inputField   : \"cal_field" + calId, "\",");
                                                        p.write("    ifFormat     : \"" + calendarFormat + "\",");
                                                        p.write("    timeFormat   : \"24\",");
                                                        p.write("    button       : \"cal_trigger" + calId, "\",");
                                                        p.write("    align        : \"Tl\",");
                                                        p.write("    singleClick  : true,");
                                                        p.write("    showsTime    : false");
                                                        p.write("  });");
                                                        p.write("</script>");
                                                    }
                                                    else {
                                                        String calendarFormat = DateValue.getCalendarFormat(dateValue.getLocalizedDateTimeFormatter(true));                                          
                                                        p.write(p.getImg("class=\"popUpButton\" id=\"cal_trigger" + calId + "\" border=\"0\" alt=\"Click to open Calendar\" src=\"", p.getResourcePath("images/"), "cal", p.getImgType(), "\""));
                                                        p.write("<script language=\"javascript\" type=\"text/javascript\">");
                                                        p.write("  Calendar.setup({");
                                                        p.write("    inputField   : \"cal_field" + calId, "\",");
                                                        p.write("    ifFormat     : \"" + calendarFormat + "\",");
                                                        p.write("    timeFormat   : \"24\",");
                                                        p.write("    button       : \"cal_trigger" + calId, "\",");
                                                        p.write("    align        : \"Tl\",");
                                                        p.write("    singleClick  : true,");
                                                        p.write("    showsTime    : true");
                                                        p.write(" });");
                                                        p.write("</script>");
                                                    }
                                                    p.write("</td>");
                                                }
                                                else if(valueHolder instanceof ObjectReferenceValue) {
                                                    ObjectReference objectReference = null;
                                                    try {
                                                        objectReference = (ObjectReference)valueHolder.getValue(false);
                                                    } 
                                                    catch (Exception e) {}
                                                    Autocompleter_1_0 autocompleter = ((ObjectReferenceValue)valueHolder).getAutocompleter(
                                                        view.getLookupObject()
                                                    );
                                                    // Find object
                                                    p.write("<td>");
                                                    if(autocompleter == null) {
                                                        p.write("  <input type=\"text\" class=\"valueL\" name=\"", field, "[" + tabIndex, "].Title\" tabindex=\"" + tabIndex, "\" value=\"", (objectReference == null ? "" : objectReference.getTitle()), "\">");
                                                        p.write("  <input type=\"hidden\" class=\"valueLLocked\" name=\"", field, "[" + tabIndex, "]", "\" value=\"", (objectReference == null ? "" : objectReference.refMofId()), "\">");
                                                    }
                                                    // Show drop-down with selectable lookup values
                                                    else {
                                                        autocompleter.paint(
                                                            p,
                                                            null,
                                                            tabIndex,
                                                            field,
                                                            valueHolder,
                                                            false,
                                                            null,
                                                            "class=\"autocompleterInput\"",
                                                            "class=\"valueL valueAC\"",
                                                            null
                                                        );                                          
                                                    }
                                                    p.write("</td>");
                                                    String lookupId = org.openmdx.kernel.id.UUIDs.getGenerator().next().toString();
                                                    Action findObjectAction = view.getFindObjectAction(
                                                        field, 
                                                        lookupId
                                                    );
                                                    p.write("<td class=\"addon\">");
                                                    if(
                                                        (autocompleter == null) || 
                                                        !autocompleter.hasFixedSelectableValues()
                                                    ) {
                                                        p.write("  ", p.getImg("class=\"popUpButton\" border=\"0\" alt=\"Click to open ObjectFinder\" src=\"", p.getResourcePath("images/"), findObjectAction.getIconKey(), "\" onclick=\"javascript:OF.findObject(", p.getEvalHRef(findObjectAction), ", document.forms['", formId, "'].elements['", field, "[" + tabIndex, "]", ".Title'], document.forms['", formId, "'].elements['", field, "[", Integer.toString(tabIndex), "]", "'], '", lookupId, "');\""));
                                                    }
                                                    p.write("</td>");
                                                }
                                                else if(valueHolder instanceof BinaryValue) {
                                                    p.write("<td>");
                                                    p.write("  <input type=\"file\" class=\"valueL\" name=\"", field, "[" + tabIndex, "]", "\" tabindex=\"" + tabIndex, "\">");
                                                    p.write("</td>");
                                                    p.write("<td class=\"addon\"></td>");
                                                }
                                                else if(valueHolder instanceof BooleanValue) {
                                                    // if checked sends (true,false). If not checked sends (false). The hidden field
                                                    // guarantees that always a value is sent.
                                                    String checkedModifier = "true".equals(stringifiedValue) ? "checked" : "";
                                                    p.write("<td ", rowSpanModifier, ">");
                                                    p.write("  <input name=\"", field, "[" + tabIndex, "].false\" type=\"hidden\" class=\"valueL", "\" value=\"false\">");
                                                    p.write("  <input name=\"", field, "[" + tabIndex, "].true\" type=\"checkbox\" ", checkedModifier, " tabindex=\"" + tabIndex, "\" value=\"true\">");
                                                    p.write("</td>");
                                                    p.write("<td class=\"addon\" ", rowSpanModifier, "></td>");                                                	
                                                }
                                                else {
                                                    p.write("<td ", rowSpanModifier, ">");
                                                    if(attribute.getSpanRow() > 1) {
                                                        int htmlId = operationIndex*100 + tabIndex;
                                                        if(attribute.getSpanRow() > 4) {
                                                            p.write("<div onclick=\"javascript:loadHTMLedit('T", Integer.toString(htmlId), "');\"", p.getOnMouseOver("javascript:this.style.backgroundColor='#FF9900';this.style.cursor='pointer';"), p.getOnMouseOut("javascript: this.style.backgroundColor='';"), " >", p.getImg("src=\"", p.getResourcePath("images/"), "html", p.getImgType(), "\" border=\"0\" alt=\"o\" title=\"\""), "</div>");
                                                        }
                                                        p.write("<textarea id=\"T", Integer.toString(htmlId), "\" rows=\"", Integer.toString(attribute.getSpanRow()), "\" cols=\"20\" class=\"string\" name=\"", field, "[", Integer.toString(tabIndex), "]", "\" tabindex=\"", Integer.toString(tabIndex), "\" style=\"width:100%;\" >", stringifiedValue, "</textarea>");
                                                    }
                                                    else {
                                                        Autocompleter_1_0 autocompleter = application.getPortalExtension().getAutocompleter(
                                                            application, 
                                                            view.getLookupObject(), 
                                                            field
                                                        );
                                                        // Predefined, selectable values only allowed for single-valued attributes with spanRow == 1
                                                        // Show drop-down instead of input field
                                                        if(autocompleter != null) {
                                                            autocompleter.paint(
                                                                p,
                                                                null,
                                                                tabIndex,
                                                                field,
                                                                valueHolder,
                                                                false,
                                                                null,
                                                                "class=\"autocompleterInput\"",
                                                                "class=\"valueL valueAC\"",
                                                                null
                                                            );
                                                        }
                                                        else {                                                        
                                                            String inputType = valueHolder instanceof TextValue
                                                            ? ((TextValue)valueHolder).isPassword() ? "password" : "text"
                                                                : "text";                                     
                                                            int maxLength = valueHolder instanceof TextValue
                                                            ? ((TextValue)valueHolder).getMaxLength()
                                                                : Integer.MAX_VALUE;
                                                            String maxLengthModifier = (maxLength == Integer.MAX_VALUE)
                                                            ? ""
                                                                : "maxlength=\"" + maxLength + "\"";
                                                            p.write("<input type=\"", inputType, "\" class=\"valueL\" tabindex=\"" + tabIndex, "\" name=\"", field, "[" + tabIndex, "]", "\" ", maxLengthModifier, " value=\"", stringifiedValue, "\">");
                                                        }
                                                    }
                                                    p.write("</td>");
                                                    p.write("<td class=\"addon\"></td>");
                                                }
                                            }                              
                                            // multi-valued
                                            else {
                                                p.write("<td ", rowSpanModifier, ">");
                                                p.write("  <textarea class=\"multiString\" name=\"", field, "[" + tabIndex, "]", "\" rows=\"" + attribute.getSpanRow(), "\" cols=\"20\" tabindex=\"" + tabIndex, "\">", stringifiedValue, "</textarea>");
                                                p.write("</td>");
                                                p.write("<td class=\"addon\"></td>");
                                            }

                                            tabIndex++;
                                        }
                                }
                                p.write("</tr>");
                            }
                            p.write("</table>");
                            p.write("</div>");
                        }
                    }
                    p.write("      <input type=\"hidden\" name=\"requestId.submit\" value=\"", view.getRequestId(), "\" />");  
                    p.write("      <input type=\"hidden\" name=\"event.submit\" value=\"", Integer.toString(invokeOperationAction.getEvent()), "\" />");
                    p.write("      <input type=\"hidden\" name=\"parameter\" value=\"", invokeOperationAction.getParameter(), "\" />");
                    p.write("      <div id=\"op", operationId, "FormButtons\" style=\"text-align:right;padding-top:8px;\">");
                    p.write("        <input type=\"submit\" value=\"", texts.getOkTitle(), "\" onmouseup=\"javascript:$('op", operationId, "FormWait').style.display='block';$('op", operationId, "FormButtons').style.visibility='hidden';\" />");
                    p.write("        <input type=\"button\" value=\"", texts.getCancelTitle(), "\" onclick=\"javascript:op", operationId, "Dialog.hide();\" />");
                    p.write("      </div>");
                    p.write("      <div id=\"op", operationId, "FormWait\" style=\"text-align:right;display:none;\">");
                    p.write("        <img src=\"images/wait.gif\" alt=\"\" />");
                    p.write("      </div>");                    
                    p.write("    </form>");
                    p.write("  </div>");
                    p.write("</div>");
                }
            }
        }
    }
  
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3904961962566955576L;

    public static final String FRAME_PARAMETERS = "Parameters";
    public static final String FRAME_RESULTS = "Results";

    protected final String operationName;
}

//--- End of File -----------------------------------------------------------
