/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: AttributeValue
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
package org.openmdx.portal.servlet.attribute;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jdo.JDOHelper;

import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.text.conversion.HtmlEncoder;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.Autocompleter_1_0;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.DataBinding;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;

public abstract class AttributeValue implements Serializable {

	/**
     * Create attribute value.
     * 
     * @param valueClassName
     * @param object
     * @param fieldDef
     * @param app
     * @return
     */
    public static AttributeValue createAttributeValue(
        String valueClassName,
        Object object,
        FieldDef fieldDef,
        ApplicationContext app
    ) {
        Class<?> valueClass = (Class<?>)AttributeValue.cachedValueClasses.get(valueClassName);
        if(valueClass == null) {
            try {
                valueClass = Classes.getApplicationClass(valueClassName);
            } 
            catch(ClassNotFoundException e) {
            	SysLog.warning("binary value class not found", valueClassName);
            }
            AttributeValue.cachedValueClasses.put(
                valueClassName,
                valueClass
            );
        }
        if(valueClass != null) {
            try {
                Constructor<?> valueConstructor = valueClass.getConstructor(
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
                        app
                    }
                );
            } catch(NoSuchMethodException e) {
            	SysLog.error("constructor for value class not found", valueClassName + "(Object object, FieldDef fieldDef, ApplicationContext application)");
            } catch(InvocationTargetException e) {
            	SysLog.error("InvocationTargetException: can not create attribute value", e.getTargetException());
            } catch(InstantiationException e) {
            	SysLog.error("InstantiationException: can not create attribute value", e);
            } catch(IllegalAccessException e) {
            	SysLog.error("IllegalAccessException: can not create attribute value", e);
            }
        }        
        return null;
    }

    /**
     * Constructor 
     *
     * @param object
     * @param fieldDef
     * @param application
     */
    public AttributeValue(
        Object object,
        FieldDef fieldDef,
        ApplicationContext application
    ) {
        this.object = object;
        this.fieldDef = fieldDef;
        this.app = application;
    }

    /**
     * Refresh attribute value, i.e. if value is cached the current value must
     * be retrieved from object.
     */
    public void refresh(
    ) {    
    }

    /**
     * Get field attribute's field definition.
     * 
     * @return
     */
    public FieldDef getFieldDef(
    ) {
        return this.fieldDef;
    }

    /**
     * Return true if multiplicity is single valued, i.e. Multiplicity.SINGLE_VALUE, 
     * Multiplicity.OPTIONAL, Multiplicity.STREAM.
     * 
     * @return
     */
    public boolean isSingleValued(
    ) {
        String multiplicity = this.fieldDef.multiplicity;
        return 
        	Multiplicity.SINGLE_VALUE.code().equals(multiplicity) ||
        	Multiplicity.OPTIONAL.code().equals(multiplicity) ||
        	Multiplicity.STREAM.code().equals(multiplicity);
    }

    /**
     * Return true if multiplicity is Multiplicity.OPTIONAL (0..1)
     * 
     * @return
     */
    public boolean isOptionalValued(
    ) {
        String multiplicity = this.fieldDef.multiplicity;
        return Multiplicity.OPTIONAL.code().equals(multiplicity);
    }

    /**
     * Return true if attribute is mandatory.
     * 
     * @return
     */
    public boolean isMandatory(
    ) {
        return this.fieldDef.isMandatory;
    }
    
    /**
     * Get attribute multiplicity.
     * 
     * @return
     */
    public String getMultiplicity(
    ) {
        return this.fieldDef.multiplicity;
    }

    /**
     * Get object.
     * 
     * @return
     */
    public Object getObject(
    ) {
        return this.object;
    }

    /**
     * Get qualified feature name.
     * 
     * @return
     */
    public String getName(
    ) {
        return this.fieldDef.qualifiedFeatureName;
    }

    /**
     * Return true if attribute is changeable.
     * 
     * @return
     */
    public boolean isChangeable(
    ) {
        return this.fieldDef.isChangeable;
    }

    /**
     * Test whether user has permission for given action for attribute on object.
     * 
     * @param action
     * @return
     */
    public boolean hasPermission(
    	String action
    ) {
    	return this.object instanceof RefObject_1_0  ? 
    		this.app.getPortalExtension().hasPermission(
	    		this.getName(),
	    		(RefObject_1_0)this.object,
	    		this.app,
	    		action
	    	) :
	    		action == null || action.isEmpty() || action.startsWith(WebKeys.GRANT_PREFIX);
    }

    /**
     * Get raw value.
     * 
     * @return
     */
    public Object getRawValue(
    ) {
    	return this.getValue(
    		this.fieldDef.qualifiedFeatureName,
    		true
    	);
    }
 
    /**
     * Get feature value.
     * 
     * @param feature
     * @param shortFormat
     * @return
     */
    protected Object getValue(
        String feature,
        boolean shortFormat
    ) {
        if(this.object == null) {
            return null;
        }
        else if(this.object instanceof RefObject_1_0) {
            try {
                RefObject_1_0 refObj = (RefObject_1_0)this.object;
                Object value;
                value = this.fieldDef.dataBinding.getValue(
                    refObj,
                    feature,
                    this.app
                );
                Object defaultValue = this.getDefaultValue();
                if(
                    !JDOHelper.isPersistent(refObj) &&
                    (defaultValue != null) && 
                    ((value == null) || 
                        ((value instanceof Number) && ((Number)value).intValue() == 0) ||
                        ((value instanceof Boolean) && !((Boolean)value).booleanValue())
                    )
                ) {
                    return defaultValue;
                } else {
                    return value;
                }
            } catch(JmiServiceException e) {
                if(
                    (e.getExceptionCode() == BasicException.Code.NOT_FOUND) ||
                    (e.getExceptionCode() == BasicException.Code.AUTHORIZATION_FAILURE)
                ) {
                    return e.getCause();
                } else {
                	SysLog.detail("can not get feature " + feature + " of object ", ((RefObject_1_0)this.object).refMofId() + ". Reason see log");
                	SysLog.detail(e.getMessage(), e.getCause());
                    return null;
                }
            } catch(Exception e) {
            	SysLog.detail("can not get feature " + feature + " of object ", ((RefObject_1_0)this.object).refMofId() + ". Reason see log");
                ServiceException e0 = new ServiceException(e);
                SysLog.detail(e0.getMessage(), e0.getCause());
                return null;
            }
        } else {
            Object value = ((Map<?,?>)this.object).get(feature);
            return value == null ? this.getDefaultValue() : value;
        }
    }

    /**
     * Get attribute value in short format.
     * 
     * @param shortFormat
     * @return
     */
    public Object getValue(
        boolean shortFormat
    ) {
        return this.getValue(
            this.fieldDef.qualifiedFeatureName,
            shortFormat
        );
    }

    /**
     * Get attribute value as collection.
     * 
     * @param shortFormat
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Collection<?> getValues(
        boolean shortFormat
    ) {
        Collection<Object> values = null;
        Object value = this.getValue(shortFormat);
        if(value instanceof Collection) {
            values = (Collection<Object>)value;
        }
        else {
            values = new ArrayList<>();
            if((value != null) && (value.toString().trim().length() > 0)) {
                values.add(value);
            }
        }
        return values;
    }

    /**
     * Get label.
     * 
     * @param attribute
     * @return
     */
    protected String getLabel(
        Attribute attribute
    ) {
        String label = attribute.getLabel();
        label += label.length() == 0 ? "" : ":";
        return label;
    }

    /**
     * Get default value. Must be implemented by subclasses.
     * 
     * @return
     */
    public abstract Object getDefaultValue();

    /**
     * Get feature value as string.
     * 
     * @param feature
     * @param shortFormat
     * @return
     */
    public String getString(
        String feature,
        boolean shortFormat
    ) {
        Object value = this.getValue(
            feature,
            shortFormat
        );
        if(value instanceof Collection) {
            if(((Collection<?>)value).size() > 0) {
                Object v = ((Collection<?>)value).iterator().next();
                return v == null ? null : v.toString();
            } else {
                return null;
            }
        }
        else {
            return value == null ? null : value.toString();
        }
    }

    /** 
     * Returns stringified attribute value.
     *  
     * @param p
     * @param multiLine
     * @param forEditing
     * @param shortFormat
     * @return
     */
    public String getStringifiedValue(
        ViewPort p,
        boolean multiLine,
        boolean forEditing,
        boolean shortFormat
    ) {    
        Object value = this.getValue(shortFormat);
        StringBuilder stringifiedValue = new StringBuilder();
        if(value instanceof Collection) {
            for(
                Iterator<?> i = ((Collection<?>)value).iterator(); 
                i.hasNext(); 
            ) {
                Object v = null;
                try {
                    v = i.next();
                } catch(Exception e) {
                    ServiceException e0 = new ServiceException(e);
                    SysLog.detail(e0.getMessage(), e0.getCause());
                }
                boolean hasDivTag = false;
                if(!this.isSingleValued()) {
                    if(forEditing) {                	  
                        if(stringifiedValue.length() > 0) {
                            stringifiedValue.append("\n");
                        }
                    } else if(multiLine)  {
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
                	String internalValue = this.getStringifiedValueInternal(
                        p, 
                        v, 
                        multiLine, 
                        forEditing, 
                        shortFormat
                    );
                    stringifiedValue.append(internalValue);
                    if(hasDivTag) {
                    	// Empty div tag renders to zero height. Put a &nbsp;
                    	if(internalValue.length() == 0) {
                    		stringifiedValue.append("&nbsp;");
                    	}
                        stringifiedValue.append("</div>");
                    }                
                }
            }
        } else {
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

    /**
     * Prepares a single stringified Value to append.
     * 
     * @param p
     * @param v
     * @param multiLine
     * @param forEditing
     * @param shortFormat
     * @return
     */
    protected String getStringifiedValueInternal(
        ViewPort p, 
        Object v,
        boolean multiLine,
        boolean forEditing,
        boolean shortFormat
    ) {
        return v.toString().trim();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(
    ) {
        Object value = this.getValue(false);
        return value == null
            ? null
            : value.toString();
    }

    /**
     * Returns the background color of the field/value as W3C CSS color, 
     * null if not defined.
     * 
     * @return
     */
    public String getBackColor(
    ) {
        return this.fieldDef.backColor;
    }

    /**
     * Returns the color of the field/value as W3C CSS color, null if not
     * defined.
     * 
     * @return
     */
    public String getColor(
    ) {
        return this.fieldDef.color;
    }

    /**
     * Get cssClass for field group.
     * 
     * @return
     */
    public String getCssClassFieldGroup(
    ) {
    	return this.fieldDef.getCssClassFieldGroup();
    }
    
    /**
     * Get cssClass for object container.
     * 
     * @return
     */
    public String getCssClassObjectContainer(
    ) {
    	return this.fieldDef.getCssClassObjectContainer();    	
    }

    /**
     * Returns icon key.
     * 
     * @return
     */
    public String getIconKey(
    ) {
        return this.fieldDef.iconKey;
    }

    /**
     * Get configured data binding.
     * 
     * @return
     */
    public DataBinding getDataBinding(
    ) {
        return fieldDef.dataBinding;
    }

    /**
     * Get auto completer.
     * 
     * @param target
     * @return
     */
    public Autocompleter_1_0 getAutocompleter(
        RefObject_1_0 target
    ) {
        return target == null ? null : 
        	this.app.getPortalExtension().getAutocompleter(
                this.app,
                target,
                this.fieldDef.qualifiedFeatureName,
                null // restrictToType
            );
    }

    /**
     * Get upper bound for given multiplicity.
     * 
     * @param defaultMultiplicity
     * @return
     */
    protected String getUpperBound(
        String defaultMultiplicity
    ) {
        String upperBound = "10";
        String multiplicity = this.getMultiplicity();
        if(multiplicity.indexOf("..") < 0) {
        	try {
        		Integer.parseInt(multiplicity);
        		multiplicity = "1.." + multiplicity;
        	} catch(Exception e) {
        		multiplicity = defaultMultiplicity;
        	}
        }
    	try {
    		String upper = multiplicity.substring(multiplicity.indexOf("..") + 2);
    		Integer.parseInt(upper);
    		upperBound = upper;
    	} catch(Exception e) {}        		
        return upperBound;        
    }

    /**
     * Get label.
     * 
     * @param attribute
     * @param p
     * @param label
     * @return
     */
    protected String getLabel(
    	Attribute attribute,
    	ViewPort p,
    	String label
    ) {
        if(label == null) {
            label = attribute.getLabel();
            if(label == null) {
            	label = attribute.getName();
            }
           	label += label.trim().length() == 0 ? "" : ":";
        }
        return label;
    }
    
    /**
     * Get title.
     * 
     * @param attribute
     * @param label
     * @return
     */
    protected String getTitle(
    	Attribute attribute,
    	String label
    ) {
    	String title = attribute.getToolTip();
    	if(title == null) return null;
    	return label.startsWith(title) ? null : title;
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
     * @param readonlyModifier readonly modifier. Set if field is read-only, otherwise "".
     * @param lockedModifier modifier to lock field. null if forEditing==false
     * @param stringifiedValue stringified value of field
     * @param forEditing field is paint in edit mode if true
     * 
     * @throws ServiceException
     */
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
        if(forEditing) {
            // Only generate id if required
            String feature = this.getName();
            id = (id == null) || (id.length() == 0) ? 
            	feature + "[" + Integer.toString(tabIndex) + "]" : 
            	id;            
            p.write("<td class=\"", CssClass.fieldLabel.toString(), "\" title=\"", (title == null ? "" : htmlEncoder.encode(title, false)), "\"><span class=\"", CssClass.nw.toString(), "\">", htmlEncoder.encode(label, false), "</span></td>");
            // Edit single-value
            if(this.isSingleValued()) {
                p.write("<td ", rowSpanModifier, ">");
                if(attribute.getSpanRow() > 1) {
                    if(readonlyModifier.isEmpty()) {
                        if(attribute.getSpanRow() > 4) {
                        	p.write("  <table style=\"width:100%;\"><tr>");
                            p.write("    <td><div onclick=\"javascript:loadHTMLedit('", id, "', '", p.getResourcePathPrefix(), "');\"", p.getOnMouseOver("javascript: this.style.backgroundColor='#FF9900';this.style.cursor='pointer';"), p.getOnMouseOut("javascript: this.style.backgroundColor='';"), " >", p.getImg("src=\"", p.getResourcePath("images/html"), p.getImgType(), "\" border=\"0\" alt=\"html\" title=\"\""), "</div></td>");
                            p.write("    <td style=\"width:100%;\"><div onclick=\"javascript:loadWIKYedit('", id, "','", p.getResourcePathPrefix(), "');\"", p.getOnMouseOver("javascript: this.style.backgroundColor='#FF9900';this.style.cursor='pointer';"), p.getOnMouseOut("javascript: this.style.backgroundColor='';"), " >", p.getImg("src=\"", p.getResourcePath("images/wiki"), p.getImgType(), "\" border=\"0\" alt=\"wiki\" title=\"\""), "</div></td>");                             
                            p.write("    <td><div onclick=\"javascript:$('", id, "').value=Wiky.toWiki($('", id, "').value);\"", p.getOnMouseOver("javascript: this.style.backgroundColor='#FF9900';this.style.cursor='pointer';"), p.getOnMouseOut("javascript: this.style.backgroundColor='';"), " >", p.getImg("src=\"", p.getResourcePath("images/htmltowiki"), p.getImgType(), "\" border=\"0\" alt=\"html &gt; wiki\" title=\"\""), "</div></td>");
                            p.write("  </tr></table>");
                        }
                        String classModifier = this.isMandatory() 
                        	? CssClass.mandatory.toString() 
                        	: "";
                        p.write("  <textarea id=\"", id, "\" name=\"", id, "\" class=\"", classModifier, "\" rows=\"", Integer.toString(attribute.getSpanRow()), "\" cols=\"30\" style=\"width:100%;\" class=\"string\" ", readonlyModifier, " tabindex=\"", Integer.toString(tabIndex), "\">", stringifiedValue, "</textarea>");
                    } else {
                     	// In case of read-only render as input field. However, without id and name attributes
                        p.write("  <textarea rows=\"", Integer.toString(attribute.getSpanRow()), "\" cols=\"30\" class=\"", CssClass.multiStringLocked.toString(), "\" ", readonlyModifier, " tabindex=\"", Integer.toString(tabIndex), "\">", stringifiedValue, "</textarea>");
                    }
                } else {
                    Autocompleter_1_0 autocompleter = readonlyModifier.isEmpty() ? 
                    	this.getAutocompleter(lookupObject) :
                    		null;
                    // Predefined, selectable values only allowed for single-valued attributes with spanRow == 1
                    // Show drop-down instead of input field
                    if(autocompleter != null) {
                        autocompleter.paint(
                            p, 
                            id,
                            tabIndex,
                            feature,
                            this,
                            false,
                            null,
                            null,
                            "class=\"" + CssClass.valueL + " " + CssClass.valueAC + "\"",
                            null, // imgTag
                            null // onChangeValueScript
                        );
                    } else {
                        String inputType = this instanceof TextValue ? 
                            ((TextValue)this).isPassword() ? "password" : "text" : 
                            "text";
                        int maxLength = this instanceof TextValue ? 
                            ((TextValue)this).getMaxLength() : 
                            Integer.MAX_VALUE;
                        String maxLengthModifier = (maxLength == Integer.MAX_VALUE) ? 
                            "" : 
                            "maxlength=\"" + maxLength + "\"";
                        String classModifier = this.isMandatory() 
                        	? CssClass.mandatory + " " + CssClass.valueL
                        	: CssClass.valueL.toString();
                        if(readonlyModifier.isEmpty()) {
                        	p.write("  <input id=\"", id, "\" name=\"", id, "\" type=\"", inputType, "\" class=\"", classModifier, lockedModifier, "\" ", readonlyModifier, " tabindex=\"" + tabIndex, "\" ", maxLengthModifier, " value=\"", stringifiedValue, "\"");
                            p.writeEventHandlers("    ", attribute.getEventHandler());
                        } else {
                        	// In case of read-only render as input field. However, without id and name attributes                        	
                        	p.write("  <input type=\"", inputType, "\" class=\"", classModifier, lockedModifier, "\" ", readonlyModifier, " tabindex=\"" + tabIndex, "\" ", maxLengthModifier, " value=\"", stringifiedValue, "\"");                        	
                        }
                        p.writeEventHandlers("    ", attribute.getEventHandler());
                        p.write("  >");
                    }
                }
                p.write("</td>");
                p.write("<td class=\"", CssClass.addon.toString(), "\" ", rowSpanModifier, "></td>");
            } else {
                // Edit multi-value
                int maxLength = this instanceof TextValue ? 
                    Math.min(Short.MAX_VALUE, ((TextValue)this).getMaxLength()) : 
                    Short.MAX_VALUE;
                p.write("<td ", rowSpanModifier, ">");
                if(readonlyModifier.isEmpty()) {
                	p.write("  <textarea id=\"", id, "\" name=\"", id, "\" rows=\"", Integer.toString(attribute.getSpanRow()), "\" cols=\"20\"", readonlyModifier, " tabindex=\"", Integer.toString(tabIndex), "\" onfocus=\"javascript:checkTextareaLimits(this,", this.getUpperBound("1..10"), ", ", Integer.toString(maxLength), ");\">", stringifiedValue, "</textarea>");
                } else {
                	p.write("  <textarea rows=\"", Integer.toString(attribute.getSpanRow()), "\" cols=\"20\"", readonlyModifier, " tabindex=\"", Integer.toString(tabIndex), "\" onfocus=\"javascript:checkTextareaLimits(this,", this.getUpperBound("1..10"), ", ", Integer.toString(maxLength), ");\">", stringifiedValue, "</textarea>");                	
                }
                p.write("</td>");
                p.write("<td class=\"", CssClass.addon.toString(), "\" ", rowSpanModifier, ">");
                p.write("</td>");
            }
        } else {
            // Show
        	String cssClass = this.app.getPortalExtension().getDefaultCssClassFieldGroup(this, this.app);
        	if(this.getCssClassFieldGroup() != null) {
        		cssClass = this.getCssClassFieldGroup() + " " + cssClass;
        	}
            if(stringifiedValue.isEmpty()) {
                styleModifier += "\"";
                p.write(gapModifier);
            	p.write("<td class=\"", CssClass.fieldLabel.toString(), "\" title=\"", (title == null ? "" : htmlEncoder.encode(title, false)), "\"><span class=\"", CssClass.nw.toString(), "\">",  htmlEncoder.encode(label, false), "</span></td>");
                p.write("<td ", rowSpanModifier, " class=\"", cssClass, "\" ",  widthModifier, " ", styleModifier, ">&nbsp;</td>");
            } else {
                if(this.isSingleValued()) {
                    p.write(gapModifier);
                   	p.write("<td class=\"", CssClass.fieldLabel.toString(), "\" title=\"", (title == null ? "" : htmlEncoder.encode(title, false)), "\"><span class=\"", CssClass.nw.toString(), "\">", htmlEncoder.encode(label, false), "</span></td>");
                    String feature = this.getName();                    	
                    id = (id == null) || id.isEmpty() ? 
                        feature + "[" + Integer.toString(tabIndex) + "]" : 
                        id;
                    boolean containsWiki = HtmlEncoder.containsWiki(stringifiedValue);
                    boolean containsHtml = HtmlEncoder.containsHtml(stringifiedValue);
                    if(attribute.getSpanRow() > 1) {
                        if(nCols == 1) {
                            styleModifier += "\"";
                       		p.write("<td ", rowSpanModifier, " class=\"", cssClass, "\" ", widthModifier, " ", (styleModifier.length() == 0 ? "" : styleModifier), ">");
                            if(containsWiki) {
	                            p.write("<div id=\"", id, "Value\" style='display:none'>");
	                            this.app.getPortalExtension().renderTextValue(
	                            	p,
	                            	this,
	                            	stringifiedValue, 
	                            	true
	                            );
	                            p.write("</div>");
                            }
                       		p.write("<div class=\"", CssClass.fieldSpannedFull.toString(), "\" id=\"", id, "\" ", (styleModifier.length() == 0 ? "" : styleModifier), ">");
                            if(containsWiki) {
                            	p.write("<script language=\"javascript\" type=\"text/javascript\">try{var w=Wiky.toHtml($('", id, "Value').innerHTML);if(w.startsWith('<p>')){w=w.substring(3);};if(w.endsWith('</p>')){w=w.substring(0,w.length-4);};w=w.strip();$('", id, "').update(w);}catch(e){$('", id, "').update($('", id, "Value').innerHTML);};</script>");                            	
                            } else {
	                            this.app.getPortalExtension().renderTextValue(
	                            	p,
	                            	this,
	                            	containsHtml ? stringifiedValue : stringifiedValue.replaceAll("\n", "<br />"), 
	                            	false
	                            );
                            }
                            p.write("</div>");
                        } else {
                            styleModifier += "height:" + (1.2+(attribute.getSpanRow()-1)*1.5) + "em;\"";
                       		p.write("<td ", rowSpanModifier, " class=\"", cssClass, "\" ", widthModifier, " ",  styleModifier, ">");
                            if(containsWiki) {
	                            p.write("<div id=\"", id, "Value\" style='display:none'>");
	                            this.app.getPortalExtension().renderTextValue(
	                            	p,
	                            	this,
	                            	stringifiedValue, 
	                            	true
	                            );
	                            p.write("</div>");
                            }
                       		p.write("<div class=\"", CssClass.fieldSpanned.toString(), "\" id=\"", id, "\" ", styleModifier, ">");
                            if(containsWiki) {
                            	p.write("<script language=\"javascript\" type=\"text/javascript\">try{var w=Wiky.toHtml($('", id, "Value').innerHTML);if(w.startsWith('<p>')){w=w.substring(3);};if(w.endsWith('</p>')){w=w.substring(0,w.length-4);};w=w.strip();$('", id, "').update(w);}catch(e){$('", id, "').update($('", id, "Value').innerHTML);};</script>");
                            } else {
	                            this.app.getPortalExtension().renderTextValue(
	                            	p,
	                            	this,
	                            	containsHtml ? stringifiedValue : stringifiedValue.replaceAll("\n", "<br />"), 
	                            	false
	                            );                	
                            }
                            p.write("</div>");
                        }
                    } else {
                    	// spanRow is 1
                        styleModifier += "\"";
                        CharSequence iconTag = this.getIconKey() == null 
                        	? "" 
                        	: "" + p.getImg("src=\"", p.getResourcePath("images/"), this.getIconKey(), "\" align=\"middle\" border=\"0\" alt=\"\"") + p.getImg("src=\"", p.getResourcePath("images/spacer"), p.getImgType(), "\" width=\"5\" height=\"0\" align=\"middle\" border=\"0\" alt=\"\"");
                       	p.write("<td ",  rowSpanModifier, " class=\"", cssClass, "\" ", widthModifier, " ", styleModifier, ">");
                        if(attribute.getValue() instanceof TextValue) {
                        	if(containsWiki) {
		                        p.write("<div id=\"", id, "Value\" style='display:none'>");
		                        this.app.getPortalExtension().renderTextValue(
		                        	p,
		                        	this,
		                        	stringifiedValue, 
		                        	true
		                        );
		                        p.write("</div>");
                        	}
                       		p.write("<div class=\"", CssClass.field.toString(), "\" id=\"", id, "\">", iconTag);
	                        if(containsWiki) {
	                        	p.write("<script language=\"javascript\" type=\"text/javascript\">try{var w=Wiky.toHtml($('", id, "Value').innerHTML);if(w.startsWith('<p>')){w=w.substring(3);};if(w.endsWith('</p>')){w=w.substring(0,w.length-4);};w=w.strip();$('", id, "').update(w);}catch(e){$('", id, "').update($('", id, "Value').innerHTML);};</script>");
	                        } else {
	                            this.app.getPortalExtension().renderTextValue(
	                            	p,
	                            	this,
	                            	stringifiedValue, 
	                            	false
	                            );                            		                        	
	                        }
	                        p.write("</div>");
                        } else {
                       		p.write("<div class=\"", CssClass.field.toString(), "\" id=\"", id, "\">", iconTag);
	                        this.app.getPortalExtension().renderTextValue(
	                        	p,
	                        	this,
	                        	containsHtml ? stringifiedValue : stringifiedValue.replaceAll("\n", "<br />"), 
	                        	false
	                        );
	                        p.write("</div>");                        	
                        }
                    }
                   	p.write("</td>");
                } else {
                    styleModifier += "height:" + (1.2+(attribute.getSpanRow()-1)*1.5) + "em;\"";
                    p.debug("<!-- multi-valued AttributeValue -->");
                    p.write(gapModifier);
                    p.write("<td class=\"", CssClass.fieldLabel.toString(), "\" title=\"", (title == null ? "" : htmlEncoder.encode(title, false)), "\"><span class=\"", CssClass.nw.toString(), "\">", label, "</span></td>");
                    p.write("<td class=\"", cssClass, "\" ",  rowSpanModifier, " ",  widthModifier, " ",  styleModifier, ">");
                    p.write("  <div class=\"", CssClass.valueMulti.toString(), "\" ",  styleModifier, "> ");
                    this.app.getPortalExtension().renderTextValue(
                    	p,
                    	this,
                    	stringifiedValue, 
                    	false
                    );
                	p.write("  </div>");
                	p.write("</td>");
                }
            }
        }
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
	private static final long serialVersionUID = -8515784264965959005L;

	public static final CssClass DEFAULT_CSS_CLASS = CssClass.valueL;
    private static Map<String,Class<?>> cachedValueClasses = new HashMap<String,Class<?>>();

    protected final FieldDef fieldDef;
    protected Object object = null;
    protected ApplicationContext app = null;

}

//--- End of File -----------------------------------------------------------
