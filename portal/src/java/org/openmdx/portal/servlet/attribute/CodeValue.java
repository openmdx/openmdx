/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: CodeValue.java,v 1.32 2007/08/12 23:02:26 wfro Exp $
 * Description: CodeValue
 * Revision:    $Revision: 1.32 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/08/12 23:02:26 $
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
package org.openmdx.portal.servlet.attribute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.text.StringBuilders;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.control.EditObjectControl;

public class CodeValue
    extends AttributeValue
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public static AttributeValue createCodeValue(
        Object object,
        FieldDef fieldDef,
        ApplicationContext application,
        String containerName
    ) {
        // Return user defined attribute value class or CodeValue as default
        String valueClassName = (String)application.getMimeTypeImpls().get(fieldDef.mimeType);
        AttributeValue attributeValue = valueClassName == null
            ? null
            : AttributeValue.createAttributeValue(
                valueClassName,
                object,
                fieldDef,
                application
              );
        return attributeValue != null
            ? attributeValue
            : new CodeValue(
                object,
                fieldDef,
                application,
                containerName
            );
    }
    
    //-------------------------------------------------------------------------
    protected CodeValue(
        Object object,
        FieldDef fieldDef,
        ApplicationContext application,
        String containerName
    ) {
        super(
            object,
            fieldDef,
            application
        );
        this.containerName = containerName;
        this.defaultValue = fieldDef.defaultValue == null
            ? null
            : new Short(fieldDef.defaultValue);
    }

    //-------------------------------------------------------------------------
    public SortedMap getLongText(
        boolean codeAsKey,
        boolean includeAll
    ) {
        return this.application.getCodes().getLongText(
            this.containerName,
            this.application.getCurrentLocaleAsIndex(),
            codeAsKey,
            includeAll
        );
    }

    //-------------------------------------------------------------------------
    public SortedMap getShortText(
        boolean codeAsKey,
        boolean includeAll
    ) {
        return this.application.getCodes().getShortText(
            this.containerName,
            this.application.getCurrentLocaleAsIndex(),
            codeAsKey,
            includeAll
        );
    }

    //-------------------------------------------------------------------------
    /**
     * Return code-level defined background color if defined. Otherwise return
     * field-level color.
     */
    public String getBackColor(
    ) {
        String backColor = null;
        Object value = super.getValue(false);      
        if(value instanceof Short) {
            backColor = (String)this.application.getCodes().getBackColors(
                this.containerName,
                true
            ).get(value);          
        }
        return backColor == null
            ? super.getBackColor()
            : backColor;
    }
    
    //-------------------------------------------------------------------------
    /**
     * Return code-level defined color if defined. Otherwise return
     * field-level color.
     */
    public String getColor(
    ) {
        String color = null;
        Object value = super.getValue(false);      
        if(value instanceof Short) {
            color = (String)this.application.getCodes().getColors(
                this.containerName,
                true
            ).get(value);          
        }
        return color == null
            ? super.getColor()
            : color;
    }
    
    //-------------------------------------------------------------------------
    /**
     * Return code-level defined icon key if defined. Otherwise return
     * field-level icon key.
     */
    public String getIconKey(
    ) {
        String iconKey = null;
        Object value = super.getValue(false);      
        if(value instanceof Short) {
            iconKey = (String)this.application.getCodes().getIconKeys(
                this.containerName,
                true
            ).get(value);          
        }
        return iconKey == null
            ? super.getIconKey()
            : iconKey;
    }
    
    //-------------------------------------------------------------------------
    protected String getStringifiedValueInternal(
        HtmlPage p, 
        Object v,
        boolean multiLine,
        boolean forEditing,
        boolean shortFormat
    ) {
        return v.toString();
    }

    //-------------------------------------------------------------------------
    public Object getValue(
        boolean shortFormat
    ) {
        Object value = super.getValue(shortFormat);
        if(value == null) {
            return null;
        }
        else if(value instanceof Collection) {
          List values = new ArrayList();
          for(Iterator i = ((Collection)value).iterator(); i.hasNext(); ) {
            Object code = i.next();
            if(code instanceof Number) {
                Short codeAsShort = new Short(((Number)code).shortValue());
                String text = shortFormat
                    ? (String)this.getShortText(true, true).get(codeAsShort)
                    : (String)this.getLongText(true, true).get(codeAsShort);
                values.add(
                    this.application.getHtmlEncoder().encode(
                        text == null 
                            ? code.toString() 
                            : text,
                        false
                    )
                );
            }
            else {
                values.add(
                    this.application.getHtmlEncoder().encode(
                        code.toString(),
                        false
                    )
                );
            }
          }
          return values;
        }
        else {
            if(value instanceof Number) {
                Short codeAsShort = new Short(((Number)value).shortValue());
                String text = shortFormat
                    ? (String)this.getShortText(true, true).get(codeAsShort)
                    : (String)this.getLongText(true, true).get(codeAsShort);
                return 
                    this.application.getHtmlEncoder().encode(
                        text == null 
                            ? value.toString() 
                            : text,
                        false
                    );
            }
            else {
                return this.application.getHtmlEncoder().encode(
                    value.toString(),
                    false
                );
            }
        }
    }

    //-------------------------------------------------------------------------
    public Object getDefaultValue(
    ) {
        if(this.defaultValue == null) {
            return null;
        }
        String text = (String)this.getLongText(true, true).get(this.defaultValue);
        return text == null ? this.defaultValue.toString() : text;
    }
  
    //-------------------------------------------------------------------------
    public void paint(
        Attribute attribute,
        HtmlPage p,
        String id,
        String label,
        RefObject_1_0 lookupObject,
        int nCols,
        int tabIndex,
        String gapModifier,
        String styleModifier,
        String widthModifier,
        String rowSpanModifier,
        String readonlyModifier,
        String disabledModifier,
        String lockedModifier,
        String stringifiedValue,
        boolean forEditing
    ) throws ServiceException {
        HtmlEncoder_1_0 htmlEncoder = p.getApplicationContext().getHtmlEncoder();                        
        if(label == null) {
            label = attribute.getLabel();
            label += label.length() == 0 ? "" : ":";        
        }
        if(forEditing) {
            String feature = this.getName();
            id = (id == null) || (id.length() == 0)            
                ? feature + "[" + Integer.toString(tabIndex) + "]"
                : id;            
            p.write("<td class=\"label\"><span class=\"nw\">", htmlEncoder.encode(label, false), "</span></td>");            
            SortedMap longTextsT = this.getLongText(false, false);
            CharSequence longTextsAsJsArray = StringBuilders.newStringBuilder();
            for(Iterator options = longTextsT.keySet().iterator(); options.hasNext(); ) {
            	(longTextsAsJsArray.length() > 0 ?
            		StringBuilders.asStringBuilder(longTextsAsJsArray).append(",") :
        			StringBuilders.asStringBuilder(longTextsAsJsArray)
		        ).append(
		        	"'"
		        ).append(
		        	options.next()
		        ).append(
		        	"'"
		        );
            }    
            if(this.isSingleValued()) {               
                Number featureValue = (Number)super.getValue(false);                      
                p.write("<td ", rowSpanModifier, ">");
                if(this.isChangeable() && this.isEnabled()) {
                    p.write("    <select id=\"", id, "\" name=\"", id, "\" class=\"valueL", lockedModifier, "\" ", readonlyModifier, " tabindex=\"", Integer.toString(tabIndex), "\">");
                    for(Iterator options = longTextsT.entrySet().iterator(); options.hasNext(); ) {
                      Map.Entry option = (Map.Entry)options.next();
                      short optionValue = ((Number)option.getValue()).shortValue();
                      String selectedModifier = (featureValue != null) && (optionValue == featureValue.shortValue()) 
                          ? "selected" 
                          : "";
                      p.write("        <option ", selectedModifier, " value=\"" + option.getKey(), "\">" + option.getKey());
                    }
                    p.write("    </select>");
                }
                else {
                    p.write("    <input id=\"", id, "\" name=\"", id, "\" type=\"text\" class=\"valueL", lockedModifier, "\" ", readonlyModifier, " tabindex=\"", Integer.toString(tabIndex), "\" value=\"", stringifiedValue, "\">");
                }
                p.write("</td>");
                p.write("<td class=\"addon\" ", rowSpanModifier, "></td>");
            }
            else {
                p.write("<td ", rowSpanModifier, ">");
                p.write("  <textarea id=\"", id, "\" name=\"", id, "\" class=\"multiStringLocked\" rows=\"", Integer.toString(attribute.getSpanRow()), "\" cols=\"20\" readonly tabindex=\"", Integer.toString(tabIndex), "\">", stringifiedValue, "</textarea>");
                p.write("</td>");
                p.write("<td class=\"addon\" ", rowSpanModifier, ">");
                if(this.isChangeable()) {
                    p.write("    ", p.getImg("class=\"popUpButton\" id=\"", id, ".popup\" border=\"0\" alt=\"Click to edit\" src=\"", p.getResourcePath("images/edit"), p.getImgType(), "\"", p.getOnClick("multiValuedHigh=", this.getUpperBound("10"), "; return editcodes_showPopup(this.id, '", EditObjectControl.POPUP_EDIT_CODES, "', $('", id, "'), new Array(", longTextsAsJsArray.toString(), "));")));
                }
                p.write("</td>");
            }
        }
        else {
            if(stringifiedValue.length() == 0) {
                super.paint(
                    attribute,
                    p,
                    id,
                    label,
                    lookupObject,
                    nCols,
                    tabIndex,
                    gapModifier,
                    styleModifier,
                    widthModifier,
                    rowSpanModifier,
                    readonlyModifier,
                    disabledModifier,
                    lockedModifier,
                    stringifiedValue,
                    forEditing
                );
            }
            else {      
                if(this.isSingleValued()) {
                    styleModifier = "";
                }
                else {
                    styleModifier += "height:" + (1.2+(attribute.getSpanRow()-1)*1.35) + "em;\"";
                }
                p.debug("<!-- multi-valued CodeValue -->");
                p.write(gapModifier);
                p.write("<td class=\"label\"><span class=\"nw\">",  htmlEncoder.encode(label, false), "</span></td>");
                p.write("<td class=\"valueL\" ", rowSpanModifier, " ", widthModifier, " ", styleModifier, ">");
                if(!this.isSingleValued()) {
                    p.write("  <div class=\"valueMulti\" ", styleModifier, "> ");
                }
                Object v = super.getValue(false);
                Collection values = new ArrayList();
                if(v instanceof Collection) {
                    values.addAll((Collection)v);
                }
                else {
                    values.add(v);
                }
                for(Iterator i = values.iterator(); i.hasNext(); ) {
                    Short codeValue = new Short(((Number)i.next()).shortValue());
                    String text = 
                        (String)this.getLongText(true, true).get(codeValue);
                    String color = (String)this.application.getCodes().getColors(
                        this.containerName,
                        true
                    ).get(codeValue);          
                    String backColor = (String)this.application.getCodes().getBackColors(
                        this.containerName,
                        true
                    ).get(codeValue);
                    String iconKey = (String)this.application.getCodes().getIconKeys(
                        this.containerName,
                        true
                    ).get(codeValue);
                    if((color != null) && (backColor != null)) {
                        p.write("<div style=\"color:", color, ";background-color:", backColor, ";\">");
                    }
                    else {
                        p.write("<div>");
                    }
                    if(iconKey != null) {
                        p.write("<img src=\"", p.getResourcePath("images/"), iconKey, "\" align=\"bottom\" border=\"0\" alt=\"\" />");
                        p.write("<img src=\"", p.getResourcePath("images/"), "spacer.gif\" width=\"5\" height=\"0\" align=\"bottom\" border=\"0\" alt=\"\" />");
                    }
                    if(text != null) {
                        this.application.getPortalExtension().renderTextValue(p, htmlEncoder.encode(text, false));
                    }
                    p.write("</div>");
                }
                if(!this.isSingleValued()) {
                    p.write("  </div>"); // valueMulti
                }
                p.write("</td>");
            }
        }
    }
    
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258135734622761273L;

    private String containerName = null;
    private Short defaultValue = null;
  
}

//--- End of File -----------------------------------------------------------
