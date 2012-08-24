/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: FieldGroupControl
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
package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jdo.JDOHelper;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.UiContext;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.attribute.AttributeValue;
import org.openmdx.portal.servlet.attribute.BinaryValue;
import org.openmdx.portal.servlet.attribute.TextValue;
import org.openmdx.portal.servlet.view.EditObjectView;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.TransientObjectView;
import org.openmdx.portal.servlet.view.View;
import org.openmdx.portal.servlet.view.ViewMode;

public class FieldGroupControl
    extends Control
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public FieldGroupControl(
        String id,
        String locale,
        int localeAsIndex,
        org.openmdx.ui1.jmi1.FieldGroup fieldGroup
    ) {
        super(
            id,
            locale,
            localeAsIndex
        );
        this.labels = fieldGroup.getLabel();
        this.fields = FieldGroupControl.getFields(fieldGroup);
        this.columnBreakAtElements = fieldGroup.getColumnBreakAtElement();
    }
  
    //-------------------------------------------------------------------------
    public FieldGroupControl(
        String id,
        String locale,
        int localeAsIndex,
        UiContext uiContext,        
        org.openmdx.ui1.jmi1.FormFieldGroupDefinition formFieldGroup
    ) {
        super(
            id,
            locale,
            localeAsIndex
        );
        this.labels = formFieldGroup.getLabel();
        this.columnBreakAtElements = formFieldGroup.getColumnBreakAtElement();
        this.fields = FieldGroupControl.getFields(
            uiContext, 
            formFieldGroup
        );
    }
  
    //-------------------------------------------------------------------------
    public static class Field {
    
        public Field(
            org.openmdx.ui1.jmi1.ValuedField field,
            Integer spanRow,
            Integer skipRow
        ) {
            this.field = field;
            this.spanRow = spanRow;
            this.skipRow = skipRow;
        }
    
        public int getSpanRow(
        ) {
            return this.spanRow == null ?
                this.field.getSpanRow() :
                this.spanRow;
        }
        
        public int getSkipRow(
        ) {
            return this.skipRow == null ?
                this.field.getSkipRow() :
                this.skipRow;
        }
        
        public org.openmdx.ui1.jmi1.ValuedField getField(
        ) {
            return this.field;
        }
        
        private final org.openmdx.ui1.jmi1.ValuedField field;
        private final Integer spanRow;
        private final Integer skipRow;
    }
    
    //-------------------------------------------------------------------------
    protected static org.openmdx.ui1.jmi1.ValuedField findField(
        UiContext uiContext,        
        String forClass,
        String featureName
    ) {
        org.openmdx.ui1.jmi1.Inspector inspector = uiContext.getInspector(
            forClass, 
            UiContext.MAIN_PERSPECTIVE
        );
        for(Iterator i = inspector.getMember().iterator(); i.hasNext(); ) {
            Object pane = i.next();
            if(pane instanceof org.openmdx.ui1.jmi1.AttributePane) {
                org.openmdx.ui1.jmi1.AttributePane paneAttr = (org.openmdx.ui1.jmi1.AttributePane)pane;
                for(Iterator j = paneAttr.getMember().iterator(); j.hasNext(); ) {
                    org.openmdx.ui1.jmi1.Tab tab = (org.openmdx.ui1.jmi1.Tab)j.next();
                    for(Iterator k = tab.getMember().iterator(); k.hasNext(); ) {
                        org.openmdx.ui1.jmi1.FieldGroup fieldGroup = (org.openmdx.ui1.jmi1.FieldGroup)k.next();
                        for(Iterator l = fieldGroup.getMember().iterator(); l.hasNext(); ) {
                            org.openmdx.ui1.jmi1.ValuedField field = (org.openmdx.ui1.jmi1.ValuedField)l.next();
                            if(field.getFeatureName().equals(featureName)) {
                                return field;
                            }
                        }
                    }
                }
            }          
            else if (pane instanceof org.openmdx.ui1.jmi1.OperationPane) {
            	org.openmdx.ui1.jmi1.OperationPane paneOp = (org.openmdx.ui1.jmi1.OperationPane)pane;
                for(Iterator j = paneOp.getMember().iterator(); j.hasNext(); ) {
                    org.openmdx.ui1.jmi1.Tab tab = (org.openmdx.ui1.jmi1.Tab)j.next();
                    for(Iterator k = tab.getMember().iterator(); k.hasNext(); ) {
                        org.openmdx.ui1.jmi1.FieldGroup fieldGroup = (org.openmdx.ui1.jmi1.FieldGroup)k.next();
                        for(Iterator l = fieldGroup.getMember().iterator(); l.hasNext(); ) {
                            org.openmdx.ui1.jmi1.ValuedField field = (org.openmdx.ui1.jmi1.ValuedField)l.next();
                            if(field.getFeatureName().equals(featureName)) {
                                return field;
                            }
                        }
                    }
                }
            }          
        }   
        return null;
    }    
    
    //-------------------------------------------------------------------------
    protected static List<FieldGroupControl.Field> getFields(
        UiContext uiContext,        
        org.openmdx.ui1.jmi1.FormFieldGroupDefinition formFieldGroup
    ) {        
        List<FieldGroupControl.Field> fields = new ArrayList<FieldGroupControl.Field>();
        org.openmdx.ui1.cci2.FormFieldDefinitionQuery query = 
            (org.openmdx.ui1.cci2.FormFieldDefinitionQuery)JDOHelper.getPersistenceManager(formFieldGroup).newQuery(org.openmdx.ui1.jmi1.FormFieldDefinition.class);
        query.orderByOrder().ascending();
        List<org.openmdx.ui1.jmi1.FormFieldDefinition> fieldDefinitions = formFieldGroup.getFormFieldDefinition(query);
        for(org.openmdx.ui1.jmi1.FormFieldDefinition fieldDefinition: fieldDefinitions) {
            org.openmdx.ui1.jmi1.ValuedField field = FieldGroupControl.findField(
                uiContext,
                fieldDefinition.getForClass(), 
                fieldDefinition.getFeatureName()
            );
            if(field != null) {
                fields.add(
                    new Field(
                        field,
                        fieldDefinition.getSpanRow(),
                        fieldDefinition.getSkipRow()
                    )
                );
            }
        }
        return fields;
    }
    
    //-------------------------------------------------------------------------
    protected static List<FieldGroupControl.Field> getFields(
        org.openmdx.ui1.jmi1.FieldGroup fieldGroup
    ) {        
        List<FieldGroupControl.Field> fields = new ArrayList<FieldGroupControl.Field>();
        List<org.openmdx.ui1.jmi1.ValuedField> members = fieldGroup.getMember();
        for(org.openmdx.ui1.jmi1.ValuedField member: members) {
            fields.add(
                new Field(
                    member,
                    null,
                    null
                )
            );
        }
        return fields;
    }
    
    //-------------------------------------------------------------------------
    public String getName(
    ) {
        return this.localeAsIndex < this.labels.size() ? 
            this.labels.get(this.localeAsIndex) : 
            this.labels.get(0);
    }
  
    //-------------------------------------------------------------------------
    public Attribute[][] getAttribute(
        Object object,
        ApplicationContext application
    ) {    
        List<List<Attribute>> columns = new ArrayList<List<Attribute>>();
        List<Attribute> attributes = null;
        columns.add(
            attributes = new ArrayList<Attribute>()
        );
        int col = 0;
        int maxRow = 0;
        int count = 0;
        Attribute[][] a = null;
        try {
            while(count < this.fields.size()) {
                if(
                	(col < this.columnBreakAtElements.size()) && 
                	(count == this.columnBreakAtElements.get(col))
                ) {
                    columns.add(
                        attributes = new ArrayList<Attribute>()
                    );
                    col++;
                }
                Field field = this.fields.get(count);
                int locale = application.getCurrentLocaleAsIndex();
                AttributeValue attributeValue = application.createAttributeValue(
                    field.getField(), 
                    object
                );
                // skip rows before
                for(int i = 0; i < field.getSkipRow(); i++) {
                    attributes.add(null); // add an empty cell              
                }
                attributes.add(
                    new Attribute(
                        locale, 
                        field.getField(), 
                        field.getSpanRow(),
                        attributeValue
                    )
                );
                // skip rows after
                for(int i = 1; i < field.getSpanRow(); i++) {
                    attributes.add(new Attribute()); // add an empty placeholder
                }
                maxRow = java.lang.Math.max(maxRow, attributes.size());
                count++;
            }
            a = new Attribute[columns.size()][maxRow];
            for(int u = 0; u < columns.size(); u++) {
                List row = (List)columns.get(u);
                for(int v = 0; v < row.size(); v++) {
                    a[u][v] = (Attribute)row.get(v);
                }
            }
        }
        catch(Exception e) {
            new ServiceException(e).log();
        }
        return a;
    }
    
    //-------------------------------------------------------------------------
    @Override
    public void paint(
        ViewPort p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {
        ApplicationContext app = p.getApplicationContext();
        HtmlEncoder_1_0 htmlEncoder = app.getHtmlEncoder();
        View view = p.getView();
        ViewMode viewMode = view instanceof EditObjectView ? 
            ((EditObjectView)view).getMode() : 
            ViewMode.STANDARD;
        Attribute[][] attributes = this.getAttribute(
            view.getObject(), 
            app
        );
        int nCols = attributes.length;
        int nRows = nCols > 0 ? attributes[0].length : 0;
        if((nCols > 0) && (nRows > 0)) {
            int fieldGroupId = p.getProperty(ViewPort.PROPERTY_FIELD_GROUP_ID) != null ? 
                ((Integer)p.getProperty(ViewPort.PROPERTY_FIELD_GROUP_ID)).intValue() : 
                1000;
        	if(p.getViewPortType() == ViewPort.Type.MOBILE) {
        		String fieldGroupName = this.getName();
        		if(fieldGroupName != null && fieldGroupName.length() > 0) {
        			p.write("      <h2>", htmlEncoder.encode(this.getName(), false), "</h2>");
        	    }
            	p.write("      <fieldset>");    
                for(int u = 0; u < nCols; u++) {                  
                	for(int v = 0; v < nRows; v++) {
	                  Attribute attribute = attributes[u][v];
	                  int tabIndex = fieldGroupId + 100*(u+1) + v;               
	                  if(attribute == null || attribute.isEmpty()) {
	                	  // skip
	                  }
	                  else {
	                    AttributeValue valueHolder = attribute.getValue();
	                    String stringifiedValue = attribute.getStringifiedValue(
	                       p, 
	                       forEditing, 
	                       false
	                    );
	                    boolean isRevokeShow = valueHolder.hasPermission(WebKeys.PERMISSION_REVOKE_SHOW);
	                    boolean isRevokeEdit = valueHolder.hasPermission(WebKeys.PERMISSION_REVOKE_EDIT);
	                    stringifiedValue = isRevokeShow ? 
	                    	WebKeys.LOCKED_VALUE :
	                    		valueHolder instanceof TextValue ? 
	                    			((TextValue)valueHolder).isPassword() ? 
	                    				"*****" : 
	                    					stringifiedValue : 
	                    						stringifiedValue;	                    
	                    boolean isModifiable = 
	                        !isRevokeShow &&
	                        !isRevokeEdit &&
	                        valueHolder.isChangeable() && 
	                        (viewMode != ViewMode.EMBEDDED || !(valueHolder instanceof BinaryValue));
	                    String readonlyModifier = isModifiable ? "" : "readonly";
	                    String lockedModifier = isModifiable ? "" : "Locked";
	                    StringBuilder styleModifier = new StringBuilder("style=\"");
	                    p.write("      <div class=\"row\">");	                    
	                    valueHolder.paint(
	                        attribute,
	                        p,
	                        null, // default id
	                        null, // default label
	                        view instanceof TransientObjectView ? 
	                            ((TransientObjectView)view).getLookupObject() :
	                            view instanceof ObjectView ? 
	                                ((ObjectView)view).getLookupObject() :
	                                null,
	                        nCols,
	                        tabIndex,
	                        "",
	                        styleModifier.toString(),
	                        "",
	                        "",
	                        readonlyModifier,
	                        lockedModifier,
	                        stringifiedValue,
	                        forEditing
	                    );
	                    p.write("      </div>");
	                  }
	                }
	            }
            	p.write("      </fieldset>");
        	}
        	else {
	            p.write("<div class=\"fieldGroupName\">", htmlEncoder.encode(this.getName(), false), "</div>");
	            p.write("<table class=\"fieldGroup\">");
	            for(int v = 0; v < nRows; v++) {
	                p.write("<tr>");
	                for(int u = 0; u < nCols; u++) {                  
	                  Attribute attribute = attributes[u][v];
	                  int tabIndex = fieldGroupId + 100*(u+1) + v;
	                  StringBuilder rowSpanModifier = new StringBuilder();
	                  if((attribute != null) && (attribute.getSpanRow() > 1)) {
	                      rowSpanModifier.append(
	                      	"rowspan=\""
	                      ).append(
	                    	attribute.getSpanRow()
	                      ).append(
	                        "\""
	                      );
	                  }
	                  StringBuilder gapModifier = new StringBuilder();
	                  if(u > 0) {
	                      gapModifier.append(
	                    	  "<td class=\"gap\" "
	                      ).append(
	                    	  rowSpanModifier
	                      ).append(
	                    	  "></td>"
	                      );
	                  }
	                  StringBuilder widthModifier = new StringBuilder();
	                  if(v == 0) {
	                	  widthModifier.append(
	                		"width=\""
	                	  ).append(
	                		100/nCols
	                	  ).append(
	                		"%\""
	                	  ); 
	                  }
	                  if(attribute == null) {
	                    if(!forEditing) {
	                        p.write(gapModifier.toString());
	                    }
	                    p.write("<td class=\"label\"></td>");
	                    if(forEditing) {
	                        p.write("<td class=\"valueEmpty\">&nbsp;</td>");
	                        p.write("<td class=\"addon\"></td>");
	                    }
	                    else {
	                        p.write("<td class=\"valueEmpty\" ", widthModifier.toString(), ">&nbsp;</td>");                        
	                    }
	                  }
	                  else if(attribute.isEmpty()) {
	                    p.write("<td class=\"label\"></td>");
	                  }
	                  else {
	                    AttributeValue valueHolder = attribute.getValue();
	                    String stringifiedValue = attribute.getStringifiedValue(
	                       p, 
	                       forEditing, 
	                       false
	                    );
	                    boolean isRevokeShow = valueHolder.hasPermission(WebKeys.PERMISSION_REVOKE_SHOW);
	                    boolean isRevokeEdit = valueHolder.hasPermission(WebKeys.PERMISSION_REVOKE_EDIT);
	                    stringifiedValue = isRevokeShow ? 
	                    	WebKeys.LOCKED_VALUE :
	                    		valueHolder instanceof TextValue ? 
	                    			((TextValue)valueHolder).isPassword() ? 
	                    				"*****" : 
	                    					stringifiedValue : 
	                    						stringifiedValue;	                    
	                    // styles
	                    String color = valueHolder.getColor();
	                    String backColor = valueHolder.getBackColor();
	                    // A field is modifiable if it is enabled and changeable. BinaryValues are
	                    // not modifiable if the view is embedded.
	                    boolean isModifiable = 
	                        !isRevokeShow &&
	                        !isRevokeEdit &&
	                        valueHolder.isChangeable() && 
	                        (viewMode != ViewMode.EMBEDDED || !(valueHolder instanceof BinaryValue));
	                    String readonlyModifier = isModifiable ? "" : "readonly";
	                    String lockedModifier = isModifiable ? "" : "Locked";
	                    StringBuilder styleModifier = new StringBuilder("style=\"");
	                    if(color != null) {
	                        styleModifier.append(
	                        	"color:"
	                        ).append(
	                        	color
	                        ).append(
	                        	";"
	                        );
	                    }
	                    if(backColor != null) {
	                        styleModifier.append(
	                        	"background-color:"
	                        ).append(
	                        	backColor
	                        ).append(
	                        	";"
	                        );
	                    }
	                    valueHolder.paint(
	                        attribute,
	                        p,
	                        null, // default id
	                        null, // default label
	                        view instanceof TransientObjectView ? 
	                            ((TransientObjectView)view).getLookupObject() :
	                            view instanceof ObjectView ? 
	                                ((ObjectView)view).getLookupObject() :
	                                null,
	                        nCols,
	                        tabIndex,
	                        gapModifier.toString(),
	                        styleModifier.toString(),
	                        widthModifier.toString(),
	                        rowSpanModifier.toString(),
	                        readonlyModifier,
	                        lockedModifier,
	                        stringifiedValue,
	                        forEditing
	                    );
	                  }
	                }
	                p.write("</tr>");
	            }
	            p.write("</table>");
	            p.setProperty(
	                ViewPort.PROPERTY_FIELD_GROUP_ID,
	                new Integer(fieldGroupId + 1000)
	            );
        	}
        }
    }
  
    //-------------------------------------------------------------------------
    public List<FieldGroupControl.Field> getFields(
    ) {
        return this.fields;
    }
    
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3257845493685302835L;
  
    protected final List<String> labels;
    protected final List<FieldGroupControl.Field> fields;    
    protected final List<Integer> columnBreakAtElements;
    
}

//--- End of File -----------------------------------------------------------
