/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: AttributeValue.java,v 1.62 2008/09/10 09:31:01 wfro Exp $
 * Description: AttributeValue
 * Revision:    $Revision: 1.62 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 09:31:01 $
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
package org.openmdx.portal.servlet.attribute;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.Autocompleter_1_0;
import org.openmdx.portal.servlet.DataBinding_1_0;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.control.EditObjectControl;

public abstract class AttributeValue
implements Serializable {

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public static AttributeValue createAttributeValue(
        String valueClassName,
        Object object,
        FieldDef fieldDef,
        ApplicationContext application
    ) {
        Class valueClass = (Class)AttributeValue.cachedValueClasses.get(valueClassName);
        if(valueClass == null) {
            try {
                valueClass = Classes.getApplicationClass(valueClassName);
            } 
            catch(ClassNotFoundException e) {
                AppLog.warning("binary value class not found", valueClassName);
            }
            AttributeValue.cachedValueClasses.put(
                valueClassName,
                valueClass
            );
        }
        if(valueClass != null) {
            try {
                Constructor valueConstructor = valueClass.getConstructor(
                    new Class[]{
                        Object.class, 
                        FieldDef.class, 
                        ApplicationContext.class
                    }
                );
                return (BinaryValue)valueConstructor.newInstance(
                    new Object[]{
                        object,
                        fieldDef,
                        application
                    }
                );
            }
            catch(NoSuchMethodException e) {
                AppLog.error("constructor for value class not found", valueClassName + "(Object object, FieldDef fieldDef, ApplicationContext application)");
            }
            catch(InvocationTargetException e) {
                AppLog.error("InvocationTargetException: can not create attribute value", e.getTargetException());
            }
            catch(InstantiationException e) {
                AppLog.error("InstantiationException: can not create attribute value", e);
            }
            catch(IllegalAccessException e) {
                AppLog.error("IllegalAccessException: can not create attribute value", e);
            }
        }        
        return null;
    }

    //-------------------------------------------------------------------------
    public AttributeValue(
        Object object,
        FieldDef fieldDef,
        ApplicationContext application
    ) {
        this.object = object;
        this.fieldDef = fieldDef;
        this.application = application;
    }

    //-------------------------------------------------------------------------
    /**
     * refresh attribute value, i.e. if value is cached the current value must
     * be retieved from object.
     */
    public void refresh(
    ) {    
    }

    //-------------------------------------------------------------------------
    public FieldDef getFieldDef(
    ) {
        return this.fieldDef;
    }

    //-------------------------------------------------------------------------
    public boolean isSingleValued(
    ) {
        String multiplicity = this.fieldDef.multiplicity;
        return 
        Multiplicities.SINGLE_VALUE.equals(multiplicity) ||
        Multiplicities.OPTIONAL_VALUE.equals(multiplicity) ||
        Multiplicities.STREAM.equals(multiplicity);
    }

    //-------------------------------------------------------------------------
    public boolean isOptionalValued(
    ) {
        String multiplicity = this.fieldDef.multiplicity;
        return 
        Multiplicities.OPTIONAL_VALUE.equals(multiplicity);
    }

    //-------------------------------------------------------------------------
    public String getMultiplicity(
    ) {
        return this.fieldDef.multiplicity;
    }

    //-------------------------------------------------------------------------
    public Object getObject(
    ) {
        return this.object;
    }

    //-------------------------------------------------------------------------
    public String getName(
    ) {
        return this.fieldDef.qualifiedFeatureName;
    }

    //-------------------------------------------------------------------------
    public boolean isChangeable(
    ) {
        return this.fieldDef.isChangeable;
    }

    //-------------------------------------------------------------------------
    public boolean isEnabled(
    ) {
        return this.object instanceof RefObject_1_0 
        ? this.application.getPortalExtension().isEnabled(
            this.getName(),
            (RefObject_1_0)this.object,
            this.application
        )
        : true;
    }

    //-------------------------------------------------------------------------
    protected Object getValue(
        String feature,
        boolean shortFormat
    ) {
        if(this.object == null) {
            return null;
        }
        else if(this.object instanceof RefObject_1_0) {
            try {
                Object value = this.fieldDef.dataBinding.getValue(
                    (RefObject_1_0)this.object,
                    feature
                );
                return value == null
                ? this.getDefaultValue()
                    : value;
            }
            catch(JmiServiceException e) {
                if(
                    (e.getExceptionCode() == BasicException.Code.NOT_FOUND) ||
                    (e.getExceptionCode() == BasicException.Code.AUTHORIZATION_FAILURE)
                ) {
                    return e.getExceptionStack();
                }
                else {
                    AppLog.info("can not get feature " + feature + " of object ", ((RefObject_1_0)this.object).refMofId() + ". Reason see log");
                    AppLog.info(e.getMessage(), e.getCause());
                    return null;
                }
            }
            catch(Exception e) {
                AppLog.info("can not get feature " + feature + " of object ", ((RefObject_1_0)this.object).refMofId() + ". Reason see log");
                ServiceException e0 = new ServiceException(e);
                AppLog.info(e0.getMessage(), e0.getCause());
                return null;
            }
        }
        else {
            Object value = ((Map)this.object).get(feature);
            return value == null
                ? this.getDefaultValue()
                : value;
        }
    }

    //-------------------------------------------------------------------------
    public Object getValue(
        boolean shortFormat
    ) {
        return this.getValue(
            this.fieldDef.qualifiedFeatureName,
            shortFormat
        );
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    protected Collection getValues(
        boolean shortFormat
    ) {
        Collection values = null;
        Object value = this.getValue(shortFormat);
        if(value instanceof Collection) {
            values = (Collection)value;
        }
        else {
            values = new ArrayList();
            if((value != null) && (value.toString().trim().length() > 0)) {
                values.add(value);
            }
        }
        return values;
    }

    //-------------------------------------------------------------------------
    protected String getLabel(
        Attribute attribute
    ) {
        String label = attribute.getLabel();
        label += label.length() == 0 ? "" : ":";
        return label;
    }

    //-------------------------------------------------------------------------
    public abstract Object getDefaultValue();

    //-------------------------------------------------------------------------
    public String getString(
        String feature,
        boolean shortFormat
    ) {
        Object value = this.getValue(
            feature,
            shortFormat
        );
        if(value instanceof Collection) {
            if(((Collection)value).size() > 0) {
                Object v = ((Collection)value).iterator().next();
                return v == null
                ? null
                    : v.toString();
            }
            else {
                return null;
            }
        }
        else {
            return value == null
            ? null
                : value.toString();
        }
    }

    //-------------------------------------------------------------------------
    /** 
     * Returns stringified attribute value. 
     */
    public String getStringifiedValue(
        HtmlPage p,
        boolean multiLine,
        boolean forEditing,
        boolean shortFormat
    ) {    
        Object value = this.getValue(shortFormat);
        StringBuilder stringifiedValue = new StringBuilder();
        if(value instanceof Collection) {
            for(
                Iterator i = ((Collection)value).iterator(); 
                i.hasNext(); 
            ) {
                Object v = null;
                try {
                    v = i.next();
                }
                catch(Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    SysLog.warning("Error when getting element from collection (more info at detail level)", e0.getMessage());
                    SysLog.detail(e0.getMessage(), e0.getCause());
                }
                boolean hasDivTag = false;
                if(!this.isSingleValued()) {
                    if(forEditing) {                	  
                        if(stringifiedValue.length() > 0) {
                            stringifiedValue.append("\n");
                        }
                    }
                    else if(multiLine)  {
                        stringifiedValue.append("<div>");
                        hasDivTag = true;
                    }
                    // multi-valued, non-spanned
                    else {
                        if(stringifiedValue.length() > 0) {
                            stringifiedValue.append("; ");
                        }
                    }              
                }
                if(v != null) {
                    stringifiedValue.append(
                        this.getStringifiedValueInternal(
                            p, 
                            v, 
                            multiLine, 
                            forEditing, 
                            shortFormat
                        )
                    ); 
                    if(hasDivTag) {
                        stringifiedValue.append("</div>");
                    }                
                }
            }
        }
        else {
            if(value != null) {
                stringifiedValue = new StringBuilder(
                    this.getStringifiedValueInternal(
                        p, 
                        value, 
                        multiLine, 
                        forEditing, 
                        shortFormat
                    )
                );
            }
        }    
        return stringifiedValue.toString();
    }

    //-------------------------------------------------------------------------
    /**
     * Prepares a single stringified Value to append.
     */
    protected String getStringifiedValueInternal(
        HtmlPage p, 
        Object v,
        boolean multiLine,
        boolean forEditing,
        boolean shortFormat
    ) {
        return v.toString().trim();
    }

    //-------------------------------------------------------------------------
    public String toString(
    ) {
        Object value = this.getValue(false);
        return value == null
            ? null
            : value.toString();
    }

    //-------------------------------------------------------------------------
    /**
     * Returns the background color of the field/value as W3C CSS color, 
     * null if not defined.
     */
    public String getBackColor(
    ) {
        return this.fieldDef.backColor;
    }

    //-------------------------------------------------------------------------
    /**
     * Returns the color of the field/value as W3C CSS color, null if not
     * defined.
     */
    public String getColor(
    ) {
        return this.fieldDef.color;
    }

    //-------------------------------------------------------------------------
    /**
     * Returns icon key.
     */
    public String getIconKey(
    ) {
        return this.fieldDef.iconKey;
    }

    //-------------------------------------------------------------------------
    public DataBinding_1_0 getDataBinding(
    ) {
        return fieldDef.dataBinding;
    }

    //-------------------------------------------------------------------------
    public Autocompleter_1_0 getAutocompleter(
        RefObject_1_0 target
    ) {
        return target == null
        ? null
            : this.application.getPortalExtension().getAutocompleter(
                this.application,
                target,
                this.fieldDef.qualifiedFeatureName
            );
    }

    //-------------------------------------------------------------------------
    protected String getUpperBound(
        String defaultValue
    ) {
        String upperBound = defaultValue;
        // Set upperBound=multiplicity if it is an integer
        if(
                (this.getMultiplicity().length() > 0) &&
                Character.isDigit(this.getMultiplicity().charAt(0))
        ) {
            try {
                Integer.parseInt(this.getMultiplicity());
                upperBound = this.getMultiplicity();
            } catch(Exception e) {}                    
        }
        return upperBound;        
    }

    /**
     * Paints the attribute to p.
     * 
     * @param attribute attribute to paint
     * @param p target page
     * @param id optional id for input fields. null if forEditing==false
     * @param label field label. If null attribute.getLabel() is used as default
     * @param lookupObject base where object lookup starts from. null if forEditing==false
     * @param nCols column span
     * @param tabIndex tab index of generated input field. -1 if forEditing==false
     * @param gapModifier gap modifier before attribute-specific code is generated. null if forEditing==true 
     * @param styleModifier style tag for generated element. null if forEditing==true
     * @param widthModifier width tag for generated element. null if forEditing==true
     * @param rowSpanModifier row span modifier
     * @param readonlyModifier readonly modifier. null if forEditing==false
     * @param disabledModifier disabled modifier. null if forEditing==false
     * @param lockedModifier modifier to lock field. null if forEditing==false
     * @param stringifiedValue stringified value of field
     * @param forEditing field is paint in edit mode if true
     * 
     * @throws ServiceException
     */
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
            // Only generate id if required
            String feature = this.getName();
            id = (id == null) || (id.length() == 0)            
            ? feature + "[" + Integer.toString(tabIndex) + "]"
                : id;            
            p.write("<td class=\"label\"><span class=\"nw\">", htmlEncoder.encode(label, false), "</span></td>");            
            if(this.isSingleValued()) {
                p.write("<td ", rowSpanModifier, ">");
                if(attribute.getSpanRow() > 1) {
                    if(this.isChangeable() && this.isEnabled()) {
                        // Multiline textarea always requires an id for launching HTML editor
                        if(attribute.getSpanRow() > 4) {
                            p.write("  <div ", p.getOnClick("javascript:loadHTMLedit('", id, "');"), p.getOnMouseOver("javascript: this.style.backgroundColor='#FF9900';this.style.cursor='pointer';"), p.getOnMouseOut("javascript: this.style.backgroundColor='';"), " >", p.getImg("src=\"", p.getResourcePath("images/html"), p.getImgType(), "\" border=\"0\" alt=\"o\" title=\"\""), "</div>");
                        }
                        p.write("  <textarea id=\"", id, "\" name=\"", id, "\" rows=\"", Integer.toString(attribute.getSpanRow()), "\" cols=\"30\" style=\"width:100%;\" class=\"string\" ", readonlyModifier, " tabindex=\"", Integer.toString(tabIndex), "\">", stringifiedValue, "</textarea>");
                    }
                    else {
                        p.write("  <textarea id=\"", id, "\" name=\"", id, "\" rows=\"", Integer.toString(attribute.getSpanRow()), "\" cols=\"30\" class=\"multiStringLocked\" ", readonlyModifier, " tabindex=\"", Integer.toString(tabIndex), "\">", stringifiedValue, "</textarea>");
                    }
                }
                else {
                    Autocompleter_1_0 autocompleter = this.getAutocompleter(
                        lookupObject
                    );
                    // Predefined, selectable values only allowed for single-valued attributes with spanRow == 1
                    // Show drop-down instead of input field
                    if(autocompleter != null) {
                        autocompleter.paint(
                            p, 
                            id,
                            tabIndex,
                            feature,
                            null,
                            false,
                            null,
                            null,
                            "class=\"valueL valueAC\"",
                            null
                        );
                    }
                    else {
                        String inputType = this instanceof TextValue
                        ? ((TextValue)this).isPassword() ? "password" : "text"
                            : "text";
                        int maxLength = this instanceof TextValue
                        ? ((TextValue)this).getMaxLength()
                            : Integer.MAX_VALUE;
                        String maxLengthModifier = (maxLength == Integer.MAX_VALUE)
                        ? ""
                            : "maxlength=\"" + maxLength + "\"";
                        p.write("  <input id=\"", id, "\" name=\"", id, "\" type=\"", inputType, "\" class=\"valueL", lockedModifier, "\" ", readonlyModifier, " tabindex=\"" + tabIndex, "\" ", maxLengthModifier, " value=\"", stringifiedValue, "\"");
                        p.writeEventHandlers("    ", attribute.getEventHandler());
                        p.write("  >");
                    }
                }
                p.write("</td>");
                p.write("<td class=\"addon\" ", rowSpanModifier, "></td>");
            }
            else {
                p.write("<td ", rowSpanModifier, ">");
                p.write("  <textarea id=\"", id, "\" name=\"", id, "\" class=\"multiStringLocked\" rows=\"", Integer.toString(attribute.getSpanRow()), "\" cols=\"20\" readonly tabindex=\"" + tabIndex, "\">", stringifiedValue, "</textarea>");
                p.write("</td>");
                p.write("<td class=\"addon\" ", rowSpanModifier, ">");
                if(this.isChangeable() && this.isEnabled()) {
                    int maxLength = this instanceof TextValue
                    ? ((TextValue)this).getMaxLength()
                        : Integer.MAX_VALUE;
                    p.write("    ", p.getImg("class=\"popUpButton\" id=\"", id, ".popup\" border=\"0\" alt=\"Click to edit\" src=\"", p.getResourcePath("images/edit"), p.getImgType(), "\"" + p.getOnClick("javascript: editstrings_maxLength=" + maxLength, "; multiValuedHigh=", this.getUpperBound("10"), "; popup_", EditObjectControl.EDIT_STRINGS, " = ", EditObjectControl.EDIT_STRINGS, "_showPopup(event, this.id, popup_", EditObjectControl.EDIT_STRINGS, ", 'popup_", EditObjectControl.EDIT_STRINGS, "', $('", id, "'), new Array());")));
                }
                p.write("</td>");
            }
        }
        else {
            if(stringifiedValue.length() == 0) {
                styleModifier += "\"";
                p.write(gapModifier);
                p.write("<td class=\"label\"><span class=\"nw\">",  htmlEncoder.encode(label, false), "</span></td>");
                p.write("<td ", rowSpanModifier, " class=\"valueL\" ",  widthModifier, " ", styleModifier, ">&nbsp;</td>");
            }
            else {                                
                if(this.isSingleValued()) {
                    p.debug("<!-- single-valued AttributeValue -->");
                    p.write(gapModifier);
                    p.write("<td class=\"label\"><span class=\"nw\">", htmlEncoder.encode(label, false), "</span></td>");
                    if(attribute.getSpanRow() > 1) {
                        if(nCols == 1) {
                            styleModifier += "\"";
                            p.write("<td ", rowSpanModifier, " class=\"valueL\" ", widthModifier, " ", (styleModifier.length() == 0 ? "" : styleModifier), ">");
                            p.write("<div class=\"fieldSpannedFull\" ", (styleModifier.length() == 0 ? "" : styleModifier), ">");
                            this.application.getPortalExtension().renderTextValue(p, stringifiedValue);
                            p.write("</div>");
                        }
                        else {
                            styleModifier += "height:" + (1.2+(attribute.getSpanRow()-1)*1.5) + "em;\"";
                            p.write("<td ", rowSpanModifier, " class=\"valueL\" ", widthModifier, " ",  styleModifier, ">");
                            p.write("<div class=\"fieldSpanned\" ", styleModifier, ">");
                            this.application.getPortalExtension().renderTextValue(p, stringifiedValue);
                            p.write("</div>");
                        }
                    }
                    else {
                        styleModifier += "\"";
                        CharSequence iconTag = this.getIconKey() == null ? 
                            "" : 
                            "" + p.getImg("src=\"", p.getResourcePath("images/"), this.getIconKey(), "\" align=\"middle\" border=\"0\" alt=\"\"") + p.getImg("src=\"", p.getResourcePath("images/spacer"), p.getImgType(), "\" width=\"5\" height=\"0\" align=\"middle\" border=\"0\" alt=\"\"");                                                                      
                        p.write("<td ",  rowSpanModifier, " class=\"valueL\" ", widthModifier, " ", styleModifier, ">");
                        p.write("<div class=\"field\">", iconTag);
                        this.application.getPortalExtension().renderTextValue(p, stringifiedValue);
                        p.write("</div>");
                    }
                    p.write("</td>");
                }
                else {
                    styleModifier += "height:" + (1.2+(attribute.getSpanRow()-1)*1.5) + "em;\"";
                    p.debug("<!-- multi-valued AttributeValue -->");
                    p.write(gapModifier);
                    p.write("<td class=\"label\"><span class=\"nw\">",  htmlEncoder.encode(label, false), "</span></td>");
                    p.write("<td class=\"valueL\" ",  rowSpanModifier, " ",  widthModifier, " ",  styleModifier, ">");
                    p.write("  <div class=\"valueMulti\" ",  styleModifier, "> ");
                    this.application.getPortalExtension().renderTextValue(p, stringifiedValue);
                    p.write("  </div>");
                    p.write("</td>");
                }
            }
        }
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static Map cachedValueClasses = new HashMap();

    protected final FieldDef fieldDef;
    protected Object object = null;
    protected ApplicationContext application = null;

}

//--- End of File -----------------------------------------------------------
