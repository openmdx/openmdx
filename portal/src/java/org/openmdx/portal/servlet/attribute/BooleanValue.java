/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: BooleanValue
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2013, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.attribute;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.control.EditInspectorControl;

/**
 * BooleanValue
 *
 */
public class BooleanValue
    extends AttributeValue
    implements Serializable {
  
    /**
     * Create boolean value.
     * 
     * @param object
     * @param fieldDef
     * @param application
     * @return
     */
    public static AttributeValue createBooleanValue(
        Object object,
        FieldDef fieldDef,
        ApplicationContext application
    ) {
        // Return user defined attribute value class or BooleanValue as default
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
            : new BooleanValue(
                object,
                fieldDef,
                application
              );        
    }
    
    /**
     * Constructor. 
     *
     * @param object
     * @param fieldDef
     * @param application
     */
    protected BooleanValue(
        Object object,
        FieldDef fieldDef,
        ApplicationContext application
    ) {
        super(
            object, 
            fieldDef,
            application
        );
        this.defaultValue = fieldDef.defaultValue == null 
            ? null 
            : new Boolean(fieldDef.defaultValue);
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.attribute.AttributeValue#getStringifiedValueInternal(org.openmdx.portal.servlet.ViewPort, java.lang.Object, boolean, boolean, boolean)
     */
    @Override
    protected String getStringifiedValueInternal(
        ViewPort p, 
        Object v,
        boolean multiLine,
        boolean forEditing,
        boolean shortFormat
    ) {
        if(forEditing) {
            return super.getStringifiedValueInternal(
                p, 
                v, 
                multiLine, 
                forEditing,
                shortFormat
            );          
        } else {
            return ((Boolean)v).booleanValue() 
                ? app.getTexts().getTrueText() 
                : app.getTexts().getFalseText();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.attribute.AttributeValue#getDefaultValue()
     */
    @Override
    public Object getDefaultValue(
    ) {
        return this.defaultValue;
    }
  
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.attribute.AttributeValue#paint(org.openmdx.portal.servlet.attribute.Attribute, org.openmdx.portal.servlet.ViewPort, java.lang.String, java.lang.String, org.openmdx.base.accessor.jmi.cci.RefObject_1_0, int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public void paint(
        Attribute attribute,
        ViewPort p,
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
        String lockedModifier,
        String stringifiedValue,
        boolean forEditing
    ) throws ServiceException { 
        HtmlEncoder_1_0 htmlEncoder = p.getApplicationContext().getHtmlEncoder();     
        label = this.getLabel(attribute, p, label);
        String title = this.getTitle(attribute, label);
        String boolImages = "";
        Collection values = this.getValues(false);
        for(Iterator e = values.iterator(); e.hasNext(); ) {
            if(Boolean.TRUE.equals(e.next())) {
            	boolImages += p.getImg("src=\"", p.getResourcePath("images/checked"), p.getImgType(), "\" alt=\"checked\"");
            } else {
            	boolImages += p.getImg("src=\"", p.getResourcePath("images/notchecked"), p.getImgType(), "\" alt=\"not checked\"");
            }
        }
        if(forEditing) {
            String feature = this.getName();
            id = (id == null) || (id.length() == 0)            
                ? feature + "[" + Integer.toString(tabIndex) + "]"
                : id;
            p.write("<td class=\"", CssClass.fieldLabel.toString(), "\" title=\"", (title == null ? "" : htmlEncoder.encode(title, false)), "\"><span class=\"", CssClass.nw.toString(), "\">", htmlEncoder.encode(label, false), "</span></td>");            
            if(this.isSingleValued()) {                
                // if checked sends (true,false). If not checked sends (false). The hidden field
                // guarantees that always a value is sent.
                String checkedModifier = "true".equals(stringifiedValue) ? "checked" : "";
                p.write("<td ", rowSpanModifier, ">");
                if(readonlyModifier.isEmpty()) {
	                p.write("  <input id=\"", id, ".false\" name=\"", id, ".false\" type=\"hidden\" class=\"", CssClass.valueL.toString(), "", lockedModifier, "\" value=\"false\">");
	                p.write("  <input id=\"", id, ".true\" name=\"", id, ".true\" type=\"checkbox\" ", checkedModifier, " ", (readonlyModifier.isEmpty() ? "" : "disabled"), " tabindex=\"" + tabIndex, "\" value=\"true\"");
	                p.writeEventHandlers("    ", attribute.getEventHandler());
	                p.write("  >");
                } else {
                	p.write(boolImages);
                }
                p.write("</td>");
                p.write("<td class=\"", CssClass.addon.toString(), "\" ", rowSpanModifier, "></td>");
            } else {
                p.write("<td ", rowSpanModifier, ">");
                if(readonlyModifier.isEmpty()) {
                	p.write("  <textarea id=\"", id, "\" name=\"", id, "\" class=\"", CssClass.multiStringLocked.toString(), "\" rows=\"", Integer.toString(attribute.getSpanRow()), "\" cols=\"20\" readonly tabindex=\"" + tabIndex, "\">", stringifiedValue, "</textarea>");
                } else {
                	p.write(boolImages);                	
                }
                p.write("</td>");
                p.write("<td class=\"", CssClass.addon.toString(), "\" ", rowSpanModifier, ">");
                if(this.isChangeable()) {
    				p.write("<a role=\"button\" data-toggle=\"modal\" href=\"#popup_", EditInspectorControl.EDIT_BOOLEANS, "\" onmouseover=\"javascript:multiValuedHigh=", this.getUpperBound("1..10"), "; ", EditInspectorControl.EDIT_BOOLEANS, "_showPopup(event, this.id, popup_", EditInspectorControl.EDIT_BOOLEANS, ", 'popup_", EditInspectorControl.EDIT_BOOLEANS, "', $('", id, "'), new Array());\">");
    				p.write("    ", p.getImg("class=\"", CssClass.popUpButton.toString(), "\" id=\"", id, ".popup\" border=\"0\" alt=\"Click to edit\" src=\"", p.getResourcePath("images/edit"), p.getImgType(), "\" "));
    				p.write("</a>");                	
                }
                p.write("</td>");
            }
        } else {
        	String cssClass = this.app.getPortalExtension().getDefaultCssClassFieldGroup(this, this.app);
        	if(this.getCssClassFieldGroup() != null) {
        		cssClass = this.getCssClassFieldGroup() + " " + cssClass;
        	}        	
            if(stringifiedValue.isEmpty() || WebKeys.LOCKED_VALUE.equals(stringifiedValue)) {
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
                    lockedModifier,
                    stringifiedValue,
                    forEditing
                );
            } else {
                styleModifier += "\"";
                p.debug("<!-- BooleanValue -->");
                p.write(gapModifier);
                p.write("<td class=\"", CssClass.fieldLabel.toString(), "\" title=\"", (title == null ? "" : htmlEncoder.encode(title, false)), "\"><span class=\"", CssClass.nw.toString(), "\">", htmlEncoder.encode(label, false), "</span></td>");
                p.write("<td ",  rowSpanModifier, " class=\"", cssClass, "\" ",  widthModifier, " ",  styleModifier, "><div class=\"", CssClass.field.toString(), "\" title=\"", stringifiedValue, "\">",  boolImages, "</div></td>");
            }
        }
    }

    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3761124925162927922L;

    private Boolean defaultValue = null;
  
}

//--- End of File -----------------------------------------------------------
