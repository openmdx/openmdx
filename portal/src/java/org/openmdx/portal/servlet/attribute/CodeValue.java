/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: CodeValue
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.control.EditInspectorControl;

/**
 * CodeValue
 *
 */
public class CodeValue extends AttributeValue implements Serializable {
  
    /**
     * Create code value.
     * 
     * @param object
     * @param fieldDef
     * @param application
     * @param containerName
     * @return
     */
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
    
    /**
     * Constructor.
     *
     * @param object
     * @param fieldDef
     * @param application
     * @param containerName
     */
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

    /**
     * Get long text for given code and current locale.
     * 
     * @param codeAsKey
     * @param includeAll
     * @return
     */
    public Map<?,?> getLongText(
        boolean codeAsKey,
        boolean includeAll
    ) {
        return this.app.getCodes().getLongText(
            this.containerName,
            this.app.getCurrentLocaleAsIndex(),
            codeAsKey,
            includeAll
        );
    }

    /**
     * Get short text for given code and current locale.
     * 
     * @param codeAsKey
     * @param includeAll
     * @return
     */
    public Map<?,?> getShortText(
        boolean codeAsKey,
        boolean includeAll
    ) {
        return this.app.getCodes().getShortText(
            this.containerName,
            this.app.getCurrentLocaleAsIndex(),
            codeAsKey,
            includeAll
        );
    }

    /**
     * Return code-level defined background color if defined. Otherwise return
     * field-level color.
     * 
     */
    public String getBackColor(
    ) {
        String backColor = null;
        Object value = super.getValue(false);      
        if(value instanceof Short) {
            backColor = (String)this.app.getCodes().getBackColors(
                this.containerName,
                true
            ).get(value);          
        }
        return backColor == null
            ? super.getBackColor()
            : backColor;
    }
    
    /**
     * Return code-level defined color if defined. Otherwise return
     * field-level color.
     * 
     */
    public String getColor(
    ) {
        String color = null;
        Object value = super.getValue(false);      
        if(value instanceof Short) {
            color = (String)this.app.getCodes().getColors(
                this.containerName,
                true
            ).get(value);          
        }
        return color == null
            ? super.getColor()
            : color;
    }
    
    /**
     * Return code-level defined icon key if defined. Otherwise return
     * field-level icon key.
     * 
     */
    public String getIconKey(
    ) {
        String iconKey = null;
        Object value = super.getValue(false);      
        if(value instanceof Short) {
            iconKey = (String)this.app.getCodes().getIconKeys(
                this.containerName,
                true
            ).get(value);          
        }
        return iconKey == null
            ? super.getIconKey()
            : iconKey;
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
        return v.toString();
    }

    /**
     * Get stringified code value either in short or long format. Encode if required.
     * 
     * @param shortFormat
     * @param encode
     * @return
     */
    public Object getValue(
    	boolean shortFormat,
    	boolean encode
    ) {
    	Object value = super.getValue(shortFormat);
    	if(value == null) {
    		return null;
    	} else if(value instanceof Collection) {
    		List<String> values = new ArrayList<String>();
    		for(Iterator<?> i = ((Collection<?>)value).iterator(); i.hasNext(); ) {
    			Object code = i.next();
    			if(code instanceof Number) {
    				Short codeAsShort = new Short(((Number)code).shortValue());
    				String text = shortFormat
    					? (String)this.getShortText(true, true).get(codeAsShort)
    					: (String)this.getLongText(true, true).get(codeAsShort);
					values.add(
						encode
							? this.app.getHtmlEncoder().encode(text == null ? code.toString() : text, false)
							: text == null ? code.toString() : text
					);
    			} else {
    				values.add(
    					encode 
    						? this.app.getHtmlEncoder().encode(code.toString(), false)
    						: code.toString()
    				);
    			}
    		}
    		return values;
    	} else {
    		if(value instanceof Number) {
    			Short codeAsShort = new Short(((Number)value).shortValue());
    			String text = shortFormat
    				? (String)this.getShortText(true, true).get(codeAsShort)
    				: (String)this.getLongText(true, true).get(codeAsShort);
				return encode
					? this.app.getHtmlEncoder().encode(text == null ? value.toString() : text, false)
					: text == null ? value.toString() : text;
    		} else {    			
    			return encode
    				? this.app.getHtmlEncoder().encode(value.toString(), false)
    				: value.toString();
    		}
    	}
    }

    /* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.attribute.AttributeValue#getValue(boolean)
	 */
    @Override
    public Object getValue(
    	boolean shortFormat
    ) {
    	return this.getValue(
    		shortFormat, 
    		false // encode
    	);
    }

	/* (non-Javadoc)
     * @see org.openmdx.portal.servlet.attribute.AttributeValue#getDefaultValue()
     */
    @Override
    public Object getDefaultValue(
    ) {
        return this.defaultValue;
    }
  
    /**
     * Paint code value drop-down for given code texts.
     * 
     * @param p
     * @param id
     * @param tabIndex
     * @param lockedModifier
     * @param styleModifier
     * @param selectedCodeValue
     * @param filterByCodeGroup
     * @param htmlEncoder
     * @param longTextsT
     * @throws ServiceException
     */
    protected void paintCodeValues(
    	ViewPort p,
    	String id,
    	int tabIndex,
    	String lockedModifier,
    	String styleModifier,
    	String disabledModifier,
    	Number selectedCodeValue,
    	String filterByCodeGroup,
    	HtmlEncoder_1_0 htmlEncoder,
    	Map<?,?> longTextsT
    ) throws ServiceException {
		p.write("    <select id=\"", id, "\" name=\"", id, "\" class=\"", CssClass.valueL.toString(), "", lockedModifier, "\"", " ", (disabledModifier == null ? "" : disabledModifier), " ", (styleModifier == null ? "" : styleModifier), " tabindex=\"", Integer.toString(tabIndex), "\">");
		for(Map.Entry<?,?> option: longTextsT.entrySet()) {
			short codeValue = ((Number)option.getValue()).shortValue();
			String selectedModifier = (selectedCodeValue != null) && (codeValue == selectedCodeValue.shortValue()) 
				? "selected" 
				: "";
			String codeText = (String)option.getKey();
			String codeGroup = "";
			if(codeText.indexOf("|") > 0) {
				int pos = codeText.indexOf("|");
				codeGroup = codeText.substring(0, pos);
				codeText = codeText.substring(pos + 1);
			}
			if(codeGroup.equals(filterByCodeGroup)) {
				codeText = htmlEncoder.encode(codeText, false);
				p.write("      <option ", selectedModifier, " value=\"" + codeValue, "\">" + codeText);					
			}
		}
		p.write("    </select>");
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
    	// Edit
    	if(forEditing) {
    		String feature = this.getName();
    		id = (id == null) || (id.length() == 0)
    			? feature + "[" + Integer.toString(tabIndex) + "]"
    			: id;
    		p.write("<td class=\"", CssClass.fieldLabel.toString(), "\" title=\"", (title == null ? "" : htmlEncoder.encode(title, false)), "\"><span class=\"", CssClass.nw.toString(), "\">", htmlEncoder.encode(label, false), "</span></td>");
            Map<?,?> longTextsT = this.getLongText(false, false);
    		if(this.isSingleValued()) {
    			Object value = super.getValue(false);
    			Number codeValue = null;
    			if(value instanceof Collection) {
    				Collection<?> values = (Collection<?>)value;
    				codeValue = values.isEmpty() ? null : (Number)values.iterator().next();
    			} else {
    				codeValue = (Number)value;
    			}
    			p.write("<td ", rowSpanModifier, ">");
    			if(readonlyModifier.isEmpty()) {
    				// Collect groups for cascading code fields
    				List<String> codeGroups = new ArrayList<String>();
    				String selectedCodeGroup = "";
    				boolean hasEmptyCodeGroup = false;
    				for(Map.Entry<?,?> option: longTextsT.entrySet()) {
    					short optionValue = ((Number)option.getValue()).shortValue();
    					String optionText = (String)option.getKey();
    					if(optionText.indexOf("|") > 0) {
    						String codeGroup = optionText.substring(0, optionText.indexOf("|"));
    						if(!codeGroups.contains(codeGroup)) {
    							codeGroups.add(codeGroup);
    						}
    						if(codeValue != null && optionValue == codeValue.shortValue()) {
    							selectedCodeGroup = codeGroup;
    						}
    					} else {
    						hasEmptyCodeGroup = true;
    					}
    				}
    				// Non-cascading code drop downs
    				if(codeGroups.isEmpty()) {
    					this.paintCodeValues(
    						p, 
    						id, 
    						tabIndex, 
    						lockedModifier,
    						null, // styleModifier
    						null, // disabledModifier
    						codeValue,
    						"", // codeGroup
    						htmlEncoder, 
    						longTextsT
    					);
    				} else {
    					// Cascading code drop-downs
    					if(hasEmptyCodeGroup && !codeGroups.contains("")) {
    						codeGroups.add(0, "");
    					}
    					// Group drop-down
	    				p.write("    <select id=\"G-", id, "\" class=\"", CssClass.valueL.toString(), "", lockedModifier, "\"", " tabindex=\"", Integer.toString(tabIndex), "\" onchange=\"javascript:for(i=0;i<", Integer.toString(codeGroups.size()), ";i++){eId='", id, "' + i;$(eId).style.display='none';$(eId).disabled=true;};eId='", id, "' + this.selectedIndex;$(eId).style.display='block';$(eId).removeAttribute('disabled');\">");
	    				int groupId = 0;
	    				for(String codeGroup: codeGroups) {
	    					String selectedModifier = codeGroup.equals(selectedCodeGroup) ? "selected" : "";
	    					p.write("      <option ", selectedModifier, " value=\"" + groupId, "\">" + codeGroup);
	    					groupId++;
	    				}
	    				p.write("    </select>");
	    				// For each group individual drop-down
	    				groupId = 0;
	    				for(String codeGroup: codeGroups) {
	    					this.paintCodeValues(
	    						p,
	    						id + groupId,
	    						tabIndex,
	    						lockedModifier,
	    						codeGroup.equals(selectedCodeGroup) ? "" : "disabled",
	    						codeGroup.equals(selectedCodeGroup) ? "style=\"display:block;\"" : "style=\"display:none;\"",
	    						codeValue,
	    						codeGroup,
	    						htmlEncoder,
	    						longTextsT
	    					);
	    					groupId++;
	    				}
    				}
    			} else {
    				// In case of read-only render as input field. However, without id and name attributes                        	
    				p.write("    <input type=\"text\" class=\"", CssClass.valueL.toString(), "", lockedModifier, "\" ", readonlyModifier, " tabindex=\"", Integer.toString(tabIndex), "\" value=\"", stringifiedValue, "\">");
    			}
    			p.write("</td>");
    			p.write("<td class=\"", CssClass.addon.toString(), "\" ", rowSpanModifier, "></td>");
    		} else {
        		StringBuilder longTextsAsJsArray = new StringBuilder();
        		Set<?> optionTexts = longTextsT.keySet();
        		for(Object optionText: optionTexts) {
        			(longTextsAsJsArray.length() > 0 
        				? longTextsAsJsArray.append(",") 
        				: longTextsAsJsArray
        			).append(
        				"'"
        			).append(
        				((String)optionText).replaceAll("'", "\\\\'")
        			).append(
        				"'"
        			);
        		}
    			p.write("<td ", rowSpanModifier, ">");
    			if(readonlyModifier.isEmpty()) {
    				p.write("  <textarea id=\"", id, "\" name=\"", id, "\" class=\"", CssClass.multiStringLocked.toString(), "\" rows=\"", Integer.toString(attribute.getSpanRow()), "\" cols=\"20\" readonly tabindex=\"", Integer.toString(tabIndex), "\">", stringifiedValue, "</textarea>");
    			} else {
    				// In case of read-only render as input field. However, without id and name attributes                        	
    				p.write("  <textarea class=\"", CssClass.multiStringLocked.toString(), "\" rows=\"", Integer.toString(attribute.getSpanRow()), "\" cols=\"20\" readonly tabindex=\"", Integer.toString(tabIndex), "\">", stringifiedValue, "</textarea>");                	
    			}
    			p.write("</td>");
    			p.write("<td class=\"", CssClass.addon.toString(), "\" ", rowSpanModifier, ">");
    			if(readonlyModifier.isEmpty()) {
    				p.write("<a role=\"button\" data-toggle=\"modal\" href=\"#popup_", EditInspectorControl.EDIT_CODES, "\" onclick=\"javascript:multiValuedHigh=", this.getUpperBound("1..10"), "; ", EditInspectorControl.EDIT_CODES, "_showPopup(event, this.id, popup_", EditInspectorControl.EDIT_CODES, ", 'popup_", EditInspectorControl.EDIT_CODES, "', $('", id, "'), new Array(", longTextsAsJsArray.toString(), "));\">");
    				p.write("    ", p.getImg("class=\"", CssClass.popUpButton.toString(), "\" id=\"", id, ".popup\" border=\"0\" alt=\"Click to edit\" src=\"", p.getResourcePath("images/edit"), p.getImgType(), "\" "));
    				p.write("</a>");
    			}
    			p.write("</td>");
    		}
    	} else {
    		// Show
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
    			if(this.isSingleValued()) {
    				styleModifier = "";
    			} else {
    				styleModifier += "height:" + (1.2+(attribute.getSpanRow()-1)*1.35) + "em;\"";
    			}
				p.write(gapModifier);
				p.write("<td class=\"", CssClass.fieldLabel.toString(), "\" title=\"", (title == null ? "" : htmlEncoder.encode(title, false)), "\"><span class=\"", CssClass.nw.toString(), "\">", htmlEncoder.encode(label, false), "</span></td>");
				p.write("<td class=\"", cssClass, "\" ", rowSpanModifier, " ", widthModifier, " ", styleModifier, ">");
				if(!this.isSingleValued()) {
					p.write("  <div class=\"", CssClass.valueMulti.toString(), "\" ", styleModifier, ">");
				}
    			Object v = super.getValue(false);
    			Collection<Object> values = new ArrayList<Object>();
    			if(v instanceof Collection) {
    				values.addAll((Collection<?>)v);
    			} else {
    				values.add(v);
    			}
    			for(Iterator<?> i = values.iterator(); i.hasNext(); ) {
    				Short codeValue = new Short(((Number)i.next()).shortValue());
    				String codeText = (String)this.getLongText(true, true).get(codeValue);
    				String color = (String)this.app.getCodes().getColors(
    					this.containerName,
    					true
    				).get(codeValue);          
    				String backColor = (String)this.app.getCodes().getBackColors(
    					this.containerName,
    					true
    				).get(codeValue);
    				String iconKey = (String)this.app.getCodes().getIconKeys(
    					this.containerName,
    					true
    				).get(codeValue);
    				if((color != null) && (backColor != null)) {
    					p.write("<div style=\"color:", color, ";background-color:", backColor, ";\">");
    				} else {
    					p.write("<div>");
    				}
    				if(iconKey != null) {
    					p.write("<img src=\"", p.getResourcePath("images/"), iconKey, "\" align=\"bottom\" border=\"0\" alt=\"\" />");
    					p.write("<img src=\"", p.getResourcePath("images/"), "spacer.gif\" width=\"5\" height=\"0\" align=\"bottom\" border=\"0\" alt=\"\" />");
    				}
    				if(codeText != null) {
    					this.app.getPortalExtension().renderTextValue(
    						p, 
    						this,
    						htmlEncoder.encode(codeText, false), 
    						false
    					);
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
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258135734622761273L;

    private String containerName = null;
    private Short defaultValue = null;
  
}

//--- End of File -----------------------------------------------------------
