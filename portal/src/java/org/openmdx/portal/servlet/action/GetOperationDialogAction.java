/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: GridEventHandler 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.action;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.Autocompleter_1_0;
import org.openmdx.portal.servlet.ObjectReference;
import org.openmdx.portal.servlet.Texts_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.ViewPortFactory;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.attribute.AttributeValue;
import org.openmdx.portal.servlet.attribute.BinaryValue;
import org.openmdx.portal.servlet.attribute.BooleanValue;
import org.openmdx.portal.servlet.attribute.CodeValue;
import org.openmdx.portal.servlet.attribute.DateValue;
import org.openmdx.portal.servlet.attribute.ObjectReferenceValue;
import org.openmdx.portal.servlet.attribute.TextValue;
import org.openmdx.portal.servlet.control.OperationTabControl;
import org.openmdx.portal.servlet.view.FieldGroup;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.OperationPane;
import org.openmdx.portal.servlet.view.OperationTab;
import org.openmdx.portal.servlet.view.ShowObjectView;

/**
 * GetOperationDialogAction
 *
 */
public class GetOperationDialogAction extends BoundAction {

    public static final int EVENT_ID = 52;
    public static final int OPERATION_DIALOG_ZINDEX = 5000;

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.action.BoundAction#perform(org.openmdx.portal.servlet.view.ObjectView, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String, javax.servlet.http.HttpSession, java.util.Map, org.openmdx.portal.servlet.ViewsCache, org.openmdx.portal.servlet.ViewsCache)
	 */
    @Override
    public ActionPerformResult perform(
        ObjectView view,
        HttpServletRequest request,
        HttpServletResponse response,
        String parameter,
        HttpSession session,
        Map<String,String[]> requestParameters,
        ViewsCache editViewsCache,
        ViewsCache showViewsCache
    ) throws IOException {
        if(view instanceof ShowObjectView) {
            ShowObjectView currentView = (ShowObjectView)view;
            ViewPort p = ViewPortFactory.openPage(
                view,
                request,
                this.getWriter(request, response)
            );
            ApplicationContext app = currentView.getApplicationContext();
            Texts_1_0 texts = app.getTexts();
            try {
                String operationId = Action.getParameter(parameter, Action.PARAMETER_ID);
                int operationIndex = Integer.valueOf(operationId);
                int paneIndex = (operationIndex / 100) - 1;
                int tabIndex = operationIndex % 100;
                OperationPane operationPane = currentView.getOperationPane()[paneIndex];
                OperationTab operationTab = operationPane.getOperationTab()[tabIndex];
                OperationTabControl operationTabControl = operationTab.getOperationTabControl();
                Action invokeOperationAction = operationTabControl.getInvokeOperationAction(currentView, app);
                String formId = "op" + operationId + "Form";
                // Operations with no arguments
                if(operationTabControl.getFieldGroupControl().length == 0) {            
                    // Confirmation prompt
                    if(operationTabControl.confirmExecution(app)) {
                        p.write("<div id=\"op", operationId, "Dialog\">");
                        p.write("  <div class=\"OperationDialogTitle\">", texts.getAssertExecutionText(), "</div> <!-- name of operation -->");
                        p.write("  <div class=\"bd\">");                        
                        p.write("    <form id=\"", formId, "\" name=\"", formId, "\" action=\"\" enctype=\"multipart/form-data\" accept-charset=\"utf-8\"", (operationTabControl.displayOperationResult() ? " target=\"OperationDialogResponse\"" : ""), " method=\"post\" >");
                        p.write("      <input type=\"hidden\" name=\"requestId.submit\" value=\"", view.getRequestId(), "\" />");
                        p.write("      <input type=\"hidden\" name=\"event.submit\" value=\"", Integer.toString(invokeOperationAction.getEvent()), "\" />");
                        p.write("      <input type=\"hidden\" name=\"parameter\" value=\"", invokeOperationAction.getParameter(), "\" />");
                        p.write("      <div id=\"opWaitArea", operationId, "\" style=\"width:50px;height:24px;\" class=\"wait\">&nbsp;</div>");
                        p.write("      <div>&nbsp;</div>");
                        p.write("      <div id=\"opSubmitArea", operationId, "\" style=\"display:none;\">");
                        p.write("        <input type=\"submit\" value=\"", texts.getOkTitle(), "\" onclick=\"javascript:$('opWaitArea", operationId, "').style.display='block';$('opSubmitArea", operationId, "').style.display='none';\" />");
                        p.write("        <input type=\"button\" value=\"", texts.getCancelTitle(), "\" onclick=\"javascript:$('OperationDialogHolder').style.display='none';$('OperationDialog').innerHTML='';\" />");
                        p.write("      </div>");
                        p.write("    </form>");
                        p.write("    <script language=\"javascript\" type=\"text/javascript\">");
                        p.write("      $('opWaitArea", operationId, "').style.display='none';");
                        p.write("      $('opSubmitArea", operationId, "').style.display='block';");
                        p.write("      $('OperationDialogHolder').style.zIndex=", Integer.toString(OPERATION_DIALOG_ZINDEX), ";");
                        p.write("      $('OperationDialogHolder').style.display='block';");
                    	p.write("      new Draggable('OperationDialogHolder', {revert:false,scroll:window,zindex:", Integer.toString(OPERATION_DIALOG_ZINDEX), ",");
                    	p.write("	     onStart: function() {");
                    	p.write("		   $('OperationDialogHolder').className='dragged';");
                    	p.write("	     },");
                    	p.write("      });");               
                        p.write("    </script>");
                        p.write("  </div>");
                        p.write("</div>");
                    }
                } else {
                    // Standard operations
                    p.write("<div id=\"op", operationId, "Dialog\">");
                    p.write("  <div class=\"OperationDialogTitle\">", operationTabControl.getToolTip(), "</div> <!-- name of operation -->");
                    p.write("  <div class=\"bd\">");                        
                    p.write("    <form id=\"", formId, "\" name=\"", formId, "\" action=\"\" enctype=\"multipart/form-data\" accept-charset=\"utf-8\"", (operationTabControl.displayOperationResult() ? " target=\"OperationDialogResponse\"" : ""), " method=\"post\" >");
                    // only show field group for input fields
                    for(int k = 0; k < 1; k++) {
                        FieldGroup fieldGroup = operationTab.getFieldGroup()[k];
                        Attribute[][] attributes = fieldGroup.getAttribute();
                        if((attributes != null) && (attributes.length > 0) && (attributes[0].length > 0)) {
                            p.write("<div class=\"fieldGroupName\">", fieldGroup.getFieldGroupControl().getName(), "</div>");
                            p.write("<div style=\"overflow:auto;\">");
                            p.write("<table class=\"fieldGroup\">");
                            int nCols = attributes.length;
                            int nRows = nCols > 0 ? attributes[0].length : 0;
                            int index = 1;
                            int fieldIndex = operationIndex*100 + index;       
                            for(int v = 0; v < nRows; v++) {
                                p.write("<tr>");
                                for(int u = 0; u < nCols; u++) {
                                    Attribute attribute = attributes[u][v];
                                    String rowSpanModifier = attribute == null ? 
                                    	"" : 
                                    	attribute.getSpanRow() > 1 ? "rowspan=\"" + attribute.getSpanRow() + "\"" : "";
                                    if(attribute == null) {
                                        p.write("<td class=\"label\"></td>");
                                        p.write("<td class=\"valueEmpty\">&nbsp;</td>");
                                    } else if(attribute.isEmpty()) {
                                        p.write("<td class=\"label\"></td>");
                                    } else {
                                        String label = attribute.getLabel();
                                        label += label.length() == 0 ? "" : ":";
                                        AttributeValue valueHolder = attribute.getValue();
                                        String fieldName = valueHolder.getName();                       
                                        String fieldId = fieldName + "[" + fieldIndex + "]";
                                        String stringifiedValue = "#ERR";
                                        try {
                                            stringifiedValue = attribute.getStringifiedValue(
                                                p, 
                                                true, 
                                                false
                                            );
                                        } catch (Exception e) {
                                            ServiceException e0 = new ServiceException(e);
                                            SysLog.warning(e0.getMessage(), e0.getCause());
                                        }
                                        p.write("<td class=\"label\"><span class=\"nw\">", label, "</span></td>");
                                        // single-valued
                                        if(valueHolder.isSingleValued()) {
                                            if(valueHolder instanceof CodeValue) {
                                                @SuppressWarnings("unchecked")
                                                Map<Object,Object> longTextsT = app.getCodes().getLongText(
                                                    fieldName, 
                                                    app.getCurrentLocaleAsIndex(), 
                                                    true, // codeAsKey 
                                                    false
                                                );
                                                p.write("<td>");
                                                p.write("  <select class=\"valueL\" name=\"", fieldId, "\" tabindex=\"", Integer.toString(index), "\">");
                                                for(Map.Entry<Object,Object> option: longTextsT.entrySet()) {
                                                    String selectedModifier = option.getKey().equals(valueHolder.getValue(false)) ? "selected" : "";
                                                    Short codeValue = (Short)option.getKey();
                                                    String codeText = (String)option.getValue();
                                                    p.write("<option ", selectedModifier, " value=\"", Short.toString(codeValue), "\">", codeText);
                                                }
                                                p.write("  </select>");
                                                p.write("</td>");
                                                p.write("<td class=\"addon\"></td>");
                                            } else if(valueHolder instanceof DateValue) {
                                                DateValue dateValue = (DateValue)valueHolder;
                                                p.write("<td ", rowSpanModifier, ">");
                                                p.write("<input type=\"text\" class=\"valueR\" id=\"cal_field", Integer.toString(fieldIndex), "\" name=\"", fieldId, "\" tabindex=\"", Integer.toString(index), "\" value=\"", stringifiedValue, "\">");
                                                p.write("</td>");
                                                p.write("<td class=\"addon\" ", rowSpanModifier, ">");
                                                if(dateValue.isDate()) {                                          
                                                    String calendarFormat = DateValue.getCalendarFormat(dateValue.getLocalizedDateFormatter(true));                                          
                                                    p.write(p.getImg("class=\"popUpButton\" id=\"cal_trigger" + fieldIndex + "\" border=\"0\" alt=\"Click to open Calendar\" src=\"", p.getResourcePath("images/"), "cal", p.getImgType(), "\""));
                                                    p.write("<script language=\"javascript\" type=\"text/javascript\">");
                                                    p.write("  Calendar.setup({");
                                                    p.write("    inputField   : \"cal_field" + fieldIndex, "\",");
                                                    p.write("    ifFormat     : \"" + calendarFormat + "\",");
                                                    p.write("    timeFormat   : \"24\",");
                                                    p.write("    button       : \"cal_trigger", Integer.toString(fieldIndex), "\",");
                                                    p.write("    align        : \"Tl\",");
                                                    p.write("    singleClick  : true,");
                                                    p.write("    showsTime    : false");
                                                    p.write("  });");
                                                    p.write("</script>");
                                                } else {
                                                    String calendarFormat = DateValue.getCalendarFormat(dateValue.getLocalizedDateTimeFormatter(true));                                          
                                                    p.write(p.getImg("class=\"popUpButton\" id=\"cal_trigger" + fieldIndex + "\" border=\"0\" alt=\"Click to open Calendar\" src=\"", p.getResourcePath("images/"), "cal", p.getImgType(), "\""));
                                                    p.write("<script language=\"javascript\" type=\"text/javascript\">");
                                                    p.write("  Calendar.setup({");
                                                    p.write("    inputField   : \"cal_field", Integer.toString(fieldIndex), "\",");
                                                    p.write("    ifFormat     : \"" + calendarFormat + "\",");
                                                    p.write("    timeFormat   : \"24\",");
                                                    p.write("    button       : \"cal_trigger", Integer.toString(fieldIndex), "\",");
                                                    p.write("    align        : \"Tl\",");
                                                    p.write("    singleClick  : true,");
                                                    p.write("    showsTime    : true");
                                                    p.write(" });");
                                                    p.write("</script>");
                                                }
                                                p.write("</td>");
                                            } else if(valueHolder instanceof ObjectReferenceValue) {
                                                ObjectReference objectReference = null;
                                                try {
                                                    objectReference = (ObjectReference)valueHolder.getValue(false);
                                                } catch (Exception e) {}
                                                Autocompleter_1_0 autocompleter = ((ObjectReferenceValue)valueHolder).getAutocompleter(
                                                    view.getLookupObject()
                                                );
                                                // Find object
                                                p.write("<td>");
                                                if(autocompleter == null) {
                                                    p.write("  <input type=\"text\" class=\"valueL\" name=\"", fieldId, ".Title\" tabindex=\"", Integer.toString(index), "\" value=\"", (objectReference == null ? "" : objectReference.getTitle()), "\">");
                                                    p.write("  <input type=\"hidden\" class=\"valueLLocked\" name=\"", fieldId, "\" value=\"", (objectReference == null ? "" : objectReference.getXRI()), "\">");
                                                } else {
                                                    // Show drop-down with selectable lookup values
                                                    autocompleter.paint(
                                                        p,
                                                        fieldId,
                                                        index,
                                                        fieldName,
                                                        valueHolder,
                                                        false,
                                                        null,
                                                        "class=\"autocompleterInput\"",
                                                        "class=\"valueL valueAC\"",
                                                        null, // imgTag
                                                        null // onChangeValueScript
                                                    );                                          
                                                }
                                                p.write("</td>");
                                                String lookupId = org.openmdx.kernel.id.UUIDs.newUUID().toString();
                                                Action findObjectAction = view.getFindObjectAction(
                                                    fieldName, 
                                                    lookupId
                                                );
                                                p.write("<td class=\"addon\">");
                                                if(
                                                    (autocompleter == null) || 
                                                    !autocompleter.hasFixedSelectableValues()
                                                ) {
                                                    p.write("  ", p.getImg("class=\"popUpButton\" border=\"0\" alt=\"Click to open ObjectFinder\" src=\"", p.getResourcePath("images/"), findObjectAction.getIconKey(), "\" onclick=\"javascript:OF.findObject(", p.getEvalHRef(findObjectAction), ", document.forms['", formId, "'].elements['", fieldId, ".Title'], document.forms['", formId, "'].elements['", fieldId, "'], '", lookupId, "');\""));
                                                }
                                                p.write("</td>");
                                            } else if(valueHolder instanceof BinaryValue) {
                                                p.write("<td>");
                                                p.write("  <input type=\"file\" class=\"valueL\" name=\"", fieldId, "\" tabindex=\"", Integer.toString(index), "\">");
                                                p.write("</td>");
                                                p.write("<td class=\"addon\"></td>");
                                            } else if(valueHolder instanceof BooleanValue) {
                                                // if checked sends (true,false). If not checked sends (false). The hidden field
                                                // guarantees that always a value is sent.
                                                String checkedModifier = "true".equals(stringifiedValue) ? "checked" : "";
                                                p.write("<td ", rowSpanModifier, ">");
                                                p.write("  <input name=\"", fieldId, ".false\" type=\"hidden\" class=\"valueL", "\" value=\"false\">");
                                                p.write("  <input name=\"", fieldId, ".true\" type=\"checkbox\" ", checkedModifier, " tabindex=\"", Integer.toString(index), "\" value=\"true\">");
                                                p.write("</td>");
                                                p.write("<td class=\"addon\" ", rowSpanModifier, "></td>");                                                	
                                            } else {
                                                p.write("<td ", rowSpanModifier, ">");
                                                if(attribute.getSpanRow() > 1) {
                                                	String id = "T" + Integer.toString(fieldIndex);
                                                    if(attribute.getSpanRow() > 4) {                                                    	
                                                    	p.write("  <table><tr>");
                                                        p.write("    <td><div onclick=\"javascript:loadHTMLedit('", id, "', '", p.getResourcePathPrefix(), "');\"", p.getOnMouseOver("javascript: this.style.backgroundColor='#FF9900';this.style.cursor='pointer';"), p.getOnMouseOut("javascript: this.style.backgroundColor='';"), " >", p.getImg("src=\"", p.getResourcePath("images/html"), p.getImgType(), "\" border=\"0\" alt=\"html\" title=\"\""), "</div></td>");
                                                        p.write("    <td style=\"width:100%;\"><div onclick=\"javascript:loadWIKYedit('", id, "','./');\"", p.getOnMouseOver("javascript: this.style.backgroundColor='#FF9900';this.style.cursor='pointer';"), p.getOnMouseOut("javascript: this.style.backgroundColor='';"), " >", p.getImg("src=\"", p.getResourcePath("images/wiki"), p.getImgType(), "\" border=\"0\" alt=\"wiki\" title=\"\""), "</div></td>");                             
                                                        p.write("    <td><div onclick=\"javascript:$('", id, "').value=Wiky.toWiki($('", id, "').value);\"", p.getOnMouseOver("javascript: this.style.backgroundColor='#FF9900';this.style.cursor='pointer';"), p.getOnMouseOut("javascript: this.style.backgroundColor='';"), " >", p.getImg("src=\"", p.getResourcePath("images/htmltowiki"), p.getImgType(), "\" border=\"0\" alt=\"html &gt; wiki\" title=\"\""), "</div></td>");
                                                        p.write("  </tr></table>");
                                                    }
                                                    p.write("<textarea id=\"", id, "\" rows=\"", Integer.toString(attribute.getSpanRow()), "\" cols=\"20\" class=\"string\" name=\"", fieldId, "\" tabindex=\"", Integer.toString(index), "\" style=\"width:100%;\" >", stringifiedValue, "</textarea>");
                                                } else {
                                                    Autocompleter_1_0 autocompleter = app.getPortalExtension().getAutocompleter(
                                                    	app, 
                                                        view.getLookupObject(), 
                                                        fieldName,
                                                        null // restrictToType
                                                    );
                                                    // Predefined, selectable values only allowed for single-valued attributes with spanRow == 1
                                                    // Show drop-down instead of input field
                                                    if(autocompleter != null) {
                                                        autocompleter.paint(
                                                            p,
                                                            null,
                                                            fieldIndex,
                                                            fieldName,
                                                            valueHolder,
                                                            false,
                                                            null,
                                                            "class=\"autocompleterInput\"",
                                                            "class=\"valueL valueAC\"",
                                                            null, // imgTag
                                                            null // onChangeValueScript
                                                        );
                                                    } else {                                                        
                                                        String inputType = valueHolder instanceof TextValue ? 
                                                        	((TextValue)valueHolder).isPassword() ? "password" : "text" : 
                                                        	"text";                                     
                                                        int maxLength = valueHolder instanceof TextValue ? 
                                                        	((TextValue)valueHolder).getMaxLength() : 
                                                        	Integer.MAX_VALUE;
                                                        String maxLengthModifier = (maxLength == Integer.MAX_VALUE) ? 
                                                        	"" : 
                                                        	"maxlength=\"" + maxLength + "\"";
                                                        p.write("<input type=\"", inputType, "\" class=\"valueL\" tabindex=\"", Integer.toString(index), "\" name=\"", fieldId, "\" ", maxLengthModifier, " value=\"", stringifiedValue, "\">");
                                                    }
                                                }
                                                p.write("</td>");
                                                p.write("<td class=\"addon\"></td>");
                                            }
                                        } else {
                                            // multi-valued
                                            p.write("<td ", rowSpanModifier, ">");
                                            p.write("  <textarea class=\"multiString\" name=\"", fieldId, "\" rows=\"" + attribute.getSpanRow(), "\" cols=\"20\" tabindex=\"", Integer.toString(index), "\">", stringifiedValue, "</textarea>");
                                            p.write("</td>");
                                            p.write("<td class=\"addon\"></td>");
                                        }
                                        index++;
                                        fieldIndex++;
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
                    p.write("      <input type=\"hidden\" name=\"parameter.submit\" value=\"", invokeOperationAction.getParameter(), "\" />");
                    p.write("      <div id=\"opWaitArea", operationId, "\" style=\"width:50px;height:24px;\" class=\"wait\">&nbsp;</div>");
                    p.write("      <div>&nbsp;</div>");
                    p.write("      <div id=\"opSubmitArea", operationId, "\" style=\"display:none;\">");
                    p.write("        <input type=\"submit\" value=\"", texts.getOkTitle(), "\" onclick=\"javascript:$('opWaitArea", operationId, "').style.display='block';$('opSubmitArea", operationId, "').style.display='none';\" />");
                    p.write("        <input type=\"button\" value=\"", texts.getCancelTitle(), "\" onclick=\"javascript:$('OperationDialogHolder').style.display='none';$('OperationDialog').innerHTML='';\" />");
                    p.write("      </div>");
                    p.write("    </form>");
                    p.write("    <script language=\"javascript\" type=\"text/javascript\">");
                    p.write("      $('opWaitArea", operationId, "').style.display='none';");
                    p.write("      $('opSubmitArea", operationId, "').style.display='block';");
                    p.write("      $('OperationDialogHolder').style.zIndex=", Integer.toString(OPERATION_DIALOG_ZINDEX), ";");
                    p.write("      $('OperationDialogHolder').style.display='block';");
                	p.write("      new Draggable('OperationDialogHolder', {revert:false,scroll:window,zindex:", Integer.toString(OPERATION_DIALOG_ZINDEX), ",");
                	p.write("	     onStart: function() {");
                	p.write("		   $('OperationDialogHolder').className='dragged';");
                	p.write("	     },");
                	p.write("      });");               
                    p.write("    </script>");
                    p.write("  </div>");
                    p.write("</div>");
                	p.write("<br />");                            
                }
            } catch (Exception e) {
                ServiceException e0 = new ServiceException(e);
                SysLog.warning(e0.getMessage(), e0.getCause());
            }
            try {
                p.close(true);
            } catch (Exception e) {
                ServiceException e0 = new ServiceException(e);
                SysLog.warning(e0.getMessage(), e0.getCause());
            }
        }
        return new ActionPerformResult(
            ActionPerformResult.StatusCode.DONE
        );
    }

}
