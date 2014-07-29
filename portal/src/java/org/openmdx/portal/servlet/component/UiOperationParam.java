/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Description: OperationFieldGroup
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.component;

import java.io.Serializable;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.Autocompleter_1_0;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.ObjectReference;
import org.openmdx.portal.servlet.Texts_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.attribute.AttributeValue;
import org.openmdx.portal.servlet.attribute.BinaryValue;
import org.openmdx.portal.servlet.attribute.BooleanValue;
import org.openmdx.portal.servlet.attribute.CodeValue;
import org.openmdx.portal.servlet.attribute.DateValue;
import org.openmdx.portal.servlet.attribute.ObjectReferenceValue;
import org.openmdx.portal.servlet.attribute.TextValue;
import org.openmdx.portal.servlet.control.UiFieldGroupControl;
import org.openmdx.portal.servlet.control.UiOperationParamControl;

/**
 * OperationFieldGroup
 *
 */
public class UiOperationParam extends UiFieldGroup implements Serializable {
    
    /**
     * Constructor.
     * 
     * @param control
     * @param view
     * @param object
     */
    public UiOperationParam(
        UiFieldGroupControl control,
        ObjectView view,
        Object object
    ) {
        super(
            control,
            view,
            object
        );
    }
   
    /**
     * Paint operation dialog.
     * 
     * @param p
     * @param formId
     * @param operationIndex
     * @throws ServiceException
     */
    public void paintOperationParams(
    	ViewPort p,
    	String formId,
    	int operationIndex
    ) throws ServiceException {
    	ApplicationContext app = p.getApplicationContext();
    	ObjectView view = this.getView();
    	UiOperationParamControl control = (UiOperationParamControl)this.control;
        Attribute[][] attributes = control.getAttribute(
        	this.getObject(), 
            app
        );
        if((attributes != null) && (attributes.length > 0) && (attributes[0].length > 0)) {
            p.write("<div class=\"", CssClass.fieldGroupName.toString(), "\">", this.getName(), "</div>");
            p.write("<div style=\"overflow:auto;\">");
            p.write("<table class=\"", CssClass.fieldGroup.toString(), "\">");
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
                        p.write("<td class=\"", CssClass.fieldLabel.toString(), "\"></td>");
                        p.write("<td class=\"", CssClass.valueEmpty.toString(), "\">&nbsp;</td>");
                    } else if(attribute.isEmpty()) {
                        p.write("<td class=\"", CssClass.fieldLabel.toString(), "\"></td>");
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
                        p.write("<td class=\"", CssClass.fieldLabel.toString(), "\"><span class=\"", CssClass.nw.toString(), "\">", label, "</span></td>");
                        // single-valued
                        if(valueHolder.isSingleValued()) {
                            if(valueHolder instanceof CodeValue) {
                                Map<Short,String> longTextsT = app.getCodes().getLongTextByCode(
                                    fieldName, 
                                    app.getCurrentLocaleAsIndex(),
                                    false
                                );
                                p.write("<td>");
                                p.write("  <select class=\"", CssClass.valueL.toString(), "\" name=\"", fieldId, "\" tabindex=\"", Integer.toString(index), "\">");
                                for(Map.Entry<Short,String> option: longTextsT.entrySet()) {
                                    String selectedModifier = option.getKey().equals(valueHolder.getValue(false)) ? "selected" : "";
                                    Short codeValue = (Short)option.getKey();
                                    String codeText = (String)option.getValue();
                                    p.write("<option ", selectedModifier, " value=\"", Short.toString(codeValue), "\">", codeText);
                                }
                                p.write("  </select>");
                                p.write("</td>");
                                p.write("<td class=\"", CssClass.addon.toString(), "\"></td>");
                            } else if(valueHolder instanceof DateValue) {
                                DateValue dateValue = (DateValue)valueHolder;
                                p.write("<td ", rowSpanModifier, ">");
                                p.write("<input type=\"text\" class=\"", CssClass.valueR.toString(), "\" id=\"cal_field", Integer.toString(fieldIndex), "\" name=\"", fieldId, "\" tabindex=\"", Integer.toString(index), "\" value=\"", stringifiedValue, "\">");
                                p.write("</td>");
                                p.write("<td class=\"", CssClass.addon.toString(), "\" ", rowSpanModifier, ">");
                                if(dateValue.isDate()) {                                          
                                    String calendarFormat = DateValue.getCalendarFormat(dateValue.getLocalizedDateFormatter(true));                                          
                                    p.write(p.getImg("class=\"", CssClass.popUpButton.toString(), "\" id=\"cal_trigger" + fieldIndex + "\" border=\"0\" alt=\"Click to open Calendar\" src=\"", p.getResourcePath("images/"), "cal", p.getImgType(), "\""));
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
                                    p.write(p.getImg("class=\"", CssClass.popUpButton.toString(), "\" id=\"cal_trigger" + fieldIndex + "\" border=\"0\" alt=\"Click to open Calendar\" src=\"", p.getResourcePath("images/"), "cal", p.getImgType(), "\""));
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
                                    p.write("  <input type=\"text\" class=\"", CssClass.valueL.toString(), "\" name=\"", fieldId, ".Title\" tabindex=\"", Integer.toString(index), "\" value=\"", (objectReference == null ? "" : objectReference.getTitle()), "\">");
                                    p.write("  <input type=\"hidden\" class=\"", CssClass.valueLLocked.toString(), "\" name=\"", fieldId, "\" value=\"", (objectReference == null ? "" : objectReference.getXRI()), "\">");
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
                                        "class=\"" + CssClass.autocompleterInput + "\"",
                                        "class=\"" + CssClass.valueL + " " + CssClass.valueAC + "\"",
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
                                p.write("<td class=\"", CssClass.addon.toString(), "\">");
                                if(
                                    (autocompleter == null) || 
                                    !autocompleter.hasFixedSelectableValues()
                                ) {
                                    p.write("  ", p.getImg("class=\"", CssClass.popUpButton.toString(), "\" border=\"0\" alt=\"Click to open ObjectFinder\" src=\"", p.getResourcePath("images/"), findObjectAction.getIconKey(), "\" onclick=\"javascript:OF.findObject(", p.getEvalHRef(findObjectAction), ", document.forms['", formId, "'].elements['", fieldId, ".Title'], document.forms['", formId, "'].elements['", fieldId, "'], '", lookupId, "');\""));
                                }
                                p.write("</td>");
                            } else if(valueHolder instanceof BinaryValue) {
                                p.write("<td>");
                                p.write("  <input type=\"file\" class=\"", CssClass.valueL.toString(), "\" name=\"", fieldId, "\" tabindex=\"", Integer.toString(index), "\">");
                                p.write("</td>");
                                p.write("<td class=\"", CssClass.addon.toString(), "\"></td>");
                            } else if(valueHolder instanceof BooleanValue) {
                                // if checked sends (true,false). If not checked sends (false). The hidden field
                                // guarantees that always a value is sent.
                                String checkedModifier = "true".equals(stringifiedValue) ? "checked" : "";
                                p.write("<td ", rowSpanModifier, ">");
                                p.write("  <input name=\"", fieldId, ".false\" type=\"hidden\" class=\"", CssClass.valueL.toString(), "", "\" value=\"false\">");
                                p.write("  <input name=\"", fieldId, ".true\" type=\"checkbox\" ", checkedModifier, " tabindex=\"", Integer.toString(index), "\" value=\"true\">");
                                p.write("</td>");
                                p.write("<td class=\"", CssClass.addon.toString(), "\" ", rowSpanModifier, "></td>");                                                	
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
                                            "class=\"" + CssClass.autocompleterInput + "\"",
                                            "class=\"" + CssClass.valueL + " " + CssClass.valueAC + "\"",
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
                                        p.write("<input type=\"", inputType, "\" class=\"", CssClass.valueL.toString(), "\" tabindex=\"", Integer.toString(index), "\" name=\"", fieldId, "\" ", maxLengthModifier, " value=\"", stringifiedValue, "\">");
                                    }
                                }
                                p.write("</td>");
                                p.write("<td class=\"", CssClass.addon.toString(), "\"></td>");
                            }
                        } else {
                            // multi-valued
                            p.write("<td ", rowSpanModifier, ">");
                            p.write("  <textarea class=\"", CssClass.multiString.toString(), "\" name=\"", fieldId, "\" rows=\"" + attribute.getSpanRow(), "\" cols=\"20\" tabindex=\"", Integer.toString(index), "\">", stringifiedValue, "</textarea>");
                            p.write("</td>");
                            p.write("<td class=\"", CssClass.addon.toString(), "\"></td>");
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
    
    /**
     * Paint operation result.
     * 
     * @param p
     * @throws ServiceException
     */
    public void paintOperationResult(
    	ViewPort p
    ) throws ServiceException {
    	ApplicationContext app = p.getApplicationContext();
    	Texts_1_0 texts = app.getTexts();
    	UiOperationParamControl control = (UiOperationParamControl)this.control;
    	ObjectView view = this.getView();
        Attribute[][] attributes = control.getAttribute(
            this.getObject(), 
            app
        );
        int nCols = attributes.length;
        int nRows = nCols > 0 ? attributes[0].length : 0;
        if((nCols > 0) && (nRows > 0)) {
            p.write("<div class=\"", CssClass.panelResult.toString(), " ", CssClass.alert.toString(), " ", CssClass.alertSuccess.toString(), "\" style=\"display: block;\">");
            p.write("  <table class=\"", CssClass.fieldGroup.toString(), "\">");
            for(int v = 0; v < nRows; v++) {
                p.write("<tr>");
                for(int u = 0; u < nCols; u++) {
                    Attribute attribute = attributes[u][v];
                    if(attribute == null) continue;
                    if(attribute.isEmpty()) {
                        p.write("<td class=\"", CssClass.fieldLabel.toString(), "\"></td>");
                    } else {
                        String label = attribute.getLabel();
                        AttributeValue valueHolder = attribute.getValue();
                        Object value = valueHolder.getValue(false);
                        String stringifiedValue = attribute.getStringifiedValue(
                            p, 
                            false, 
                            false
                        );
                        stringifiedValue = valueHolder instanceof TextValue
                        	? ((TextValue)valueHolder).isPassword() 
                        		? "*****" 
                        		: stringifiedValue
                            : stringifiedValue;
                        String widthModifier = "";                                    
                        String readonlyModifier = valueHolder.isChangeable() ? "" : "readonly";
                        String lockedModifier = valueHolder.isChangeable() ? "" : "Locked";
                        String styleModifier = "style=\"";
                        // ObjectReference
                        if(value instanceof ObjectReference) {
                            Action selectAction = ((ObjectReference)value).getSelectObjectAction();
                            Action selectAndEditAction = ((ObjectReference)value).getSelectAndEditObjectAction();
                            p.write("<td class=\"", CssClass.fieldLabel.toString(), "\"><span class=\"", CssClass.nw.toString(), "\">", label, ":</span></td>");
                            if(stringifiedValue == null || stringifiedValue.isEmpty()) {
                            	p.write("<td class=\"", CssClass.valueL.toString(), "\" ", widthModifier, "><div class=\"", CssClass.field.toString(), "\" title=\"", selectAction.getToolTip(), "\"></div></td>");                            	
                            } else {
                            	p.write("<td class=\"", CssClass.valueL.toString(), "\" ", widthModifier, "><div class=\"", CssClass.field.toString(), "\" title=\"", selectAction.getToolTip(), "\"><a href=\"\" onmouseover=\"javascript:this.href=", p.getEvalHRef(selectAction), ";onmouseover=function(){};\">", selectAction.getTitle(), "</a> [<a href=\"\" onclick=\"javascript:this.href=", p.getEvalHRef(selectAndEditAction), ";\">", texts.getEditTitle(), "</a>]</div></td>");
                            }
                        } else {
                            // other types
                            valueHolder.paint(
                                attribute,
                                p,
                                null, // default id
                                null, // default label
                                view.getLookupObject(),
                                nCols,
                                0,
                                "",
                                styleModifier,
                                widthModifier,
                                "",
                                readonlyModifier,
                                lockedModifier,
                                stringifiedValue,
                                false // forEditing
                            );
                        }
                    }
                }
                p.write("</tr>");
            }
            p.write("  </table>");
            p.write("</div>");
        } else {
        	p.write("<div />");
        }    	
    }

	//-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
	private static final long serialVersionUID = 7567829727618951823L;

}
