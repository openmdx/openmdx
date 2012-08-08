/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: OperationTabControl.java,v 1.21 2007/12/13 18:58:09 wfro Exp $
 * Description: OperationTabControl
 * Revision:    $Revision: 1.21 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/13 18:58:09 $
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
import org.openmdx.portal.servlet.attribute.CodeValue;
import org.openmdx.portal.servlet.attribute.DateValue;
import org.openmdx.portal.servlet.attribute.ObjectReferenceValue;
import org.openmdx.portal.servlet.attribute.TextValue;
import org.openmdx.portal.servlet.texts.Texts_1_0;
import org.openmdx.portal.servlet.view.FieldGroup;
import org.openmdx.portal.servlet.view.OperationPane;
import org.openmdx.portal.servlet.view.OperationTab;
import org.openmdx.portal.servlet.view.ShowObjectView;
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
    
  }
  
  //-------------------------------------------------------------------------
  public String getOperationName(
  ) {
    return ((org.openmdx.ui1.jmi1.OperationTab)this.tab).getOperationName();
  }
  
  //-------------------------------------------------------------------------
  public String getIconKey(
  ) {
    return this.tab.getIconKey().substring(
      this.tab.getIconKey().lastIndexOf(":") + 1
    ) + WebKeys.ICON_TYPE;
  }

  //-------------------------------------------------------------------------
  public String getToolTip(
  ) {
    return this.localeAsIndex < this.tab.getToolTip().size()
      ? this.tab.getToolTip().get(this.localeAsIndex)
      : this.tab.getToolTip().get(0);
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
    ) {
        // build-in operations
        if(Ui_1.EDIT_OBJECT_OPERATION_NAME.equals(this.getOperationName())) {
            return view.getObjectReference().getEditObjectAction();
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
    private void writeOperationParameterDialog(
        HtmlPage p,
        int operationIndex,
        String okTitle,
        String cancelTitle
    ) throws ServiceException {
        
        p.write("  <script type=\"text/javascript\">");
        p.write("      var Operation", Integer.toString(operationIndex), " = function(){");
        p.write("      var dialog;");
        p.write("");
        p.write("      return {");
        p.write("");
        p.write("        submitHandler : function(btn, evtObject) {");
        p.write("             document.op", Integer.toString(operationIndex), "Form.submit();");
        p.write("             this.hide();");
        p.write("        },");
        p.write("");
        p.write("        showDialog : function(){");
        p.write("            if(!dialog){");
        p.write("                dialog = new YAHOO.ext.BasicDialog(\"op", Integer.toString(operationIndex), "Dialog\", {");
        p.write("                        modal:true,");
        p.write("                        autoTabs:false,");
        p.write("                        autoScroll:false,");
        p.write("                        resizeable:false,");
        p.write("                        width:\"60%\",");
        p.write("                        shim:true,");
        p.write("                        shadow:true,");
        p.write("                        minWidth:250,");
        p.write("                        minHeight:50,");
        p.write("                        proxyDrag: false");
        p.write("                });");
        p.write("                dialog.addKeyListener(27, dialog.hide, dialog);");
        p.write("                dialog.addButton('", okTitle, "', this.submitHandler, dialog);");
        p.write("                dialog.addButton('", cancelTitle, "', dialog.hide, dialog);");
        p.write("            }");
        p.write("            var elt = getEl('op", Integer.toString(operationIndex), "Form');");
        p.write("            dialog.resizeTo(elt.getWidth(), elt.getHeight()+100);");        
        p.write("            dialog.show();");
        p.write("        }");
        p.write("      };");
        p.write("    }();");
        p.write("");
        p.write("  </script>");
    }
    
    //-------------------------------------------------------------------------
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
                // no input parameters so don't generate dialog
                if((this.getFieldGroupControl().length == 0) && !this.confirmExecution(application)) {
                    p.write("    <li><a href=\"#\"", p.getOnClick("javascript:this.href=", p.getEvalHRef(invokeOperationAction), ";"), " id=\"opTab", operationId, "\" >", this.getName(), "</a></li>");
                }
                // standard operation with input parameters
                else {
                    p.write("    <li><a href=\"#\"", " id=\"op", operationId, "Trigger\"", p.getOnClick("javascript:Operation", operationId, ".showDialog();try{document.forms.op", operationId, "Form.elements[0].focus();}catch(e){};"), " >", this.getName(), "...</a></li>");
                }
            }
            // operation is disabled. Do not generate onlick or href actions
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

            if(invokeOperationAction.isEnabled()) {

                String formId = "op" + operationIndex + "Form";

                // Operations with no arguments
                if(this.getFieldGroupControl().length == 0) {            
                    // Confirmation prompt
                    if(this.confirmExecution(application)) {
                        p.write("<div id=\"op", Integer.toString(operationIndex), "Dialog\" style=\"visibility:hidden;position:absolute;top:0px;\">");
                        this.writeOperationParameterDialog(
                            p, 
                            operationIndex,
                            texts.getYesText(),
                            texts.getNoText()
                        );
                        p.write("  <div class=\"ydlg-hd\">", texts.getAssertExecutionText(), "</div>");
                        p.write("  <div class=\"ydlg-bd\">");     
                        p.write("    <form id=\"", formId, "\" name=\"", formId, "\" action=\"\" enctype=\"multipart/form-data\" accept-charset=\"utf-8\" method=\"post\" >");
                        p.write("      <input type=\"hidden\" name=\"requestId.submit\" value=\"", view.getRequestId(), "\" />");  
                        p.write("      <input type=\"hidden\" name=\"event.submit\" value=\"", Integer.toString(invokeOperationAction.getEvent()), "\" />");
                        p.write("      <input type=\"hidden\" name=\"parameter\" value=\"", invokeOperationAction.getParameter(), "\" />");
                        p.write("    </form>");
                        p.write("  </div>");
                        p.write("</div>");
                    }
                }            
                // Standard operations
                else {
                    p.write("<div id=\"op", Integer.toString(operationIndex), "Dialog\" style=\"visibility:hidden;position:absolute;top:0px;\">");
                    this.writeOperationParameterDialog(
                        p, 
                        operationIndex,
                        texts.getOkTitle(),
                        texts.getCancelTitle()
                    );
                    p.write("  <div class=\"ydlg-hd\">", this.getToolTip(), "</div>");
                    p.write("  <div class=\"ydlg-bd\">");     
                    p.write("<form id=\"", formId, "\" name=\"", formId, "\" action=\"\" enctype=\"multipart/form-data\" accept-charset=\"utf-8\" method=\"post\" >");
                    // only show field group for input fields
                    for(int k = 0; k < 1; k++) {
                        FieldGroup fieldGroup = operationTab.getFieldGroup()[k];
                        Attribute[][] attributes = fieldGroup.getAttribute();
                        if((attributes != null) && (attributes.length > 0) && (attributes[0].length > 0)) {
                            p.write("<div class=\"opFieldGroupName\">", fieldGroup.getFieldGroupControl().getName(), "</div>");
                            // Firefox Caret Bug Workaround: <div style=...
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
                                                AppLog.warning(e0.getMessage(), e0.getCause(), 1);
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
                                                        true
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
                                                    } catch (Exception e) {}
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
                                                        p.write("  ", p.getImg("class=\"popUpButton\" border=\"0\" alt=\"Click to open ObjectFinder\" src=\"", p.getResourcePath("images/"), findObjectAction.getIconKey(), "\"", p.getOnClick("javascript:OF.findObject(" + p.getEvalHRef(findObjectAction), ", document.forms['", formId, "'].elements['", field, "[" + tabIndex, "]", ".Title'], document.forms['", formId, "'].elements['", field, "[", Integer.toString(tabIndex), "]", "'], '", lookupId, "');")));
                                                    }
                                                    p.write("</td>");
                                                }
                                                else if(valueHolder instanceof BinaryValue) {
                                                    p.write("<td>");
                                                    p.write("  <input type=\"file\" class=\"valueL\" name=\"", field, "[" + tabIndex, "]", "\" tabindex=\"" + tabIndex, "\">");
                                                    p.write("</td>");
                                                    p.write("<td class=\"addon\"></td>");
                                                }
                                                else {
                                                    p.write("<td ", rowSpanModifier, ">");
                                                    if(attribute.getSpanRow() > 1) {
                                                        int htmlId = operationIndex*100 + tabIndex;
                                                        if(attribute.getSpanRow() > 4) {
                                                            p.write("<div", p.getOnClick("javascript:loadHTMLedit('T", Integer.toString(htmlId), "');"), p.getOnMouseOver("javascript:this.style.backgroundColor='#FF9900';this.style.cursor='pointer';"), p.getOnMouseOut("javascript: this.style.backgroundColor='';"), " >", p.getImg("src=\"", p.getResourcePath("images/"), "html", p.getImgType(), "\" border=\"0\" alt=\"o\" title=\"\""), "</div>");
                                                        }
                                                        p.write("<textarea id=\"T", Integer.toString(htmlId), "\" rows=\"", Integer.toString(attribute.getSpanRow()), "\" cols=\"20\" class=\"string\" name=\"", field, "[", Integer.toString(tabIndex), "]", "\" tabindex=\"", Integer.toString(tabIndex), "\" style=\"width:100%;\" >", stringifiedValue, "</textarea>");
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
                    p.write("    <input type=\"hidden\" name=\"requestId.submit\" value=\"", view.getRequestId(), "\" />");  
                    p.write("    <input type=\"hidden\" name=\"event.submit\" value=\"", Integer.toString(invokeOperationAction.getEvent()), "\" />");
                    p.write("    <input type=\"hidden\" name=\"parameter\" value=\"", invokeOperationAction.getParameter(), "\" />");
                    p.write("  </form>");
                    p.write("</div>");
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

}

//--- End of File -----------------------------------------------------------
