/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: FieldGroupControl.java,v 1.37 2008/08/12 16:38:06 wfro Exp $
 * Description: FieldGroupControl
 * Revision:    $Revision: 1.37 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/08/12 16:38:06 $
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
import java.util.List;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.attribute.Attribute;
import org.openmdx.portal.servlet.attribute.AttributeValue;
import org.openmdx.portal.servlet.attribute.BinaryValue;
import org.openmdx.portal.servlet.attribute.TextValue;
import org.openmdx.portal.servlet.view.EditObjectView;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.ViewMode;
import org.openmdx.ui1.jmi1.FieldGroup;

public class FieldGroupControl
    extends Control
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public FieldGroupControl(
        String id,
        String locale,
        int localeAsIndex,
        ControlFactory controlFactory,
        org.openmdx.ui1.jmi1.FieldGroup fieldGroup,
        TabControl tabControl,
        int fieldGroupIndex
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            controlFactory
        );
        this.tabControl = tabControl;
        this.fieldGroup = fieldGroup;    
        this.fieldGroupIndex = fieldGroupIndex;
    }
  
    //-------------------------------------------------------------------------
    public FieldGroup getFieldGroup(
    ) {
        return this.fieldGroup;
    }
    
    //-------------------------------------------------------------------------
    public String getName(
    ) {
        return this.localeAsIndex < this.fieldGroup.getLabel().size()
          ? this.fieldGroup.getLabel().get(this.localeAsIndex)
          : this.fieldGroup.getLabel().get(0);
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
    AppLog.trace("refreshing field group", fieldGroup);
    Attribute[][] a = null;
    try {
        while(count < this.fieldGroup.getMember().size()) {
          if(count == this.fieldGroup.getColumnBreakAtElement().get(col)) {
            columns.add(
              attributes = new ArrayList<Attribute>()
            );
            col++;
          }
          org.openmdx.ui1.jmi1.ValuedField field = (org.openmdx.ui1.jmi1.ValuedField)this.fieldGroup.getMember().get(count);
          int locale = application.getCurrentLocaleAsIndex();
          AttributeValue attributeValue =
            this.controlFactory.getAttributeValueFactory().getAttributeValue(
              field, 
              object,
              application
          );
          // skip rows before
          for(int i = 0; i < field.getSkipRow(); i++) {
            attributes.add(null); // add an empty cell              
          }
          attributes.add(
              new Attribute(
                  locale, 
                  field, 
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
        HtmlPage p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {
        HtmlEncoder_1_0 htmlEncoder = p.getApplicationContext().getHtmlEncoder();
        ObjectView view = (ObjectView)p.getView();
        ViewMode viewMode = view instanceof EditObjectView ? ((EditObjectView)view).getMode() : ViewMode.STANDARD;
        Attribute[][] attributes = view.getAttributePane().getAttributeTab()[this.tabControl.getTabIndex()].getFieldGroup()[this.fieldGroupIndex].getAttribute();
        int nCols = attributes.length;
        int nRows = nCols > 0 ? attributes[0].length : 0;
        if((nCols > 0) && (nRows > 0)) {
            int fieldGroupId = p.getProperty(HtmlPage.PROPERTY_FIELD_GROUP_ID) != null
                ? ((Integer)p.getProperty(HtmlPage.PROPERTY_FIELD_GROUP_ID)).intValue()
                : 1000;
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
                    p.write("<td class=\"valueEmpty\" ",  widthModifier.toString(), ">&nbsp;</td>");
                    if(forEditing) {
                        p.write("<td class=\"addon\"></td>");
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
                    stringifiedValue = valueHolder instanceof TextValue
                        ? ((TextValue)valueHolder).isPassword() ? "*****" : stringifiedValue
                        : stringifiedValue;

                    // styles
                    String color = valueHolder.getColor();
                    String backColor = valueHolder.getBackColor();
                    // A field is modifiable if it is enabled and changeable. BinaryValues are
                    // not modifiable if the view is embedded.
                    boolean isModifiable = 
                        valueHolder.isEnabled() && 
                        valueHolder.isChangeable() && 
                        (viewMode != ViewMode.EMBEDDED || !(valueHolder instanceof BinaryValue));
                    String readonlyModifier = isModifiable ? "" : "readonly";
                    String disabledModifier = isModifiable ? "" : "disabled";                    
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
                        view.getLookupObject(),
                        nCols,
                        tabIndex,
                        gapModifier.toString(),
                        styleModifier.toString(),
                        widthModifier.toString(),
                        rowSpanModifier.toString(),
                        readonlyModifier,
                        disabledModifier,
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
                HtmlPage.PROPERTY_FIELD_GROUP_ID,
                new Integer(fieldGroupId + 1000)
            );            
        }
    }
  
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3257845493685302835L;
  
    private final TabControl tabControl;
    private final org.openmdx.ui1.jmi1.FieldGroup fieldGroup;
    private final int fieldGroupIndex;
    
}

//--- End of File -----------------------------------------------------------
