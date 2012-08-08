/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: Attribute.java,v 1.17 2008/12/08 16:33:52 wfro Exp $
 * Description: Attribute 
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/08 16:33:52 $
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
import java.util.List;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.HtmlPage;

public final class Attribute
  implements Serializable {
  
    //-------------------------------------------------------------------------
    public Attribute(
        int locale,
        org.openmdx.ui1.jmi1.ValuedField field,
        AttributeValue attributeValue
    ) {
        this(
            locale,
            field,
            field.getSpanRow(),
            attributeValue
        );
    }
    
    //-------------------------------------------------------------------------
    public Attribute(
        int locale,
        org.openmdx.ui1.jmi1.ValuedField field,
        int spanRow,
        AttributeValue attributeValue
    ) {
        this(
            locale < field.getLabel().size() ? 
                field.getLabel().get(locale) : 
                field.getLabel().get(0),
            locale < field.getToolTip().size() ? 
                field.getToolTip().get(locale) : 
                field.getToolTip().size() == 0 ? "" : field.getToolTip().get(0),
            spanRow,
            field.getEventHandler(),
            attributeValue     
        );
    }
    
    //-------------------------------------------------------------------------
    protected Attribute(
        String label,
        String toolTip,
        int spanRow,
        List eventHandler,
        AttributeValue value
    ) {
      this.isEmpty = false;
      this.label = label;
      this.toolTip = toolTip;
      this.spanRow = spanRow;
      this.eventHandler = eventHandler;
      this.value = value;
    }

    //-------------------------------------------------------------------------
    public Attribute(
    ) {
        this.isEmpty = true;
        this.label = null;
        this.toolTip = null;
        this.spanRow = 0;
        this.value = null;
    }
  
    //-------------------------------------------------------------------------
    /**
     * An empty attribute should be skipped by the
     * rendering. An empty attribute acts as placeholder
     * in case an attribute was defined with spanRow > 1 
     * in the same column of a field group.
     */
    public boolean isEmpty(
    ) {
        return this.isEmpty;
    }
  
    //-------------------------------------------------------------------------
    public String getLabel(
    ) {
        return this.label;
    }
  
    //-------------------------------------------------------------------------
    public String getToolTip(
    ) {
        return this.toolTip;
    }
  
    //-------------------------------------------------------------------------
    public AttributeValue getValue(
    ) {
        return this.value;
    }

    //-------------------------------------------------------------------------
    public String getName(
    ) {
        return this.value.getName();
    }
    
    //-------------------------------------------------------------------------
    public int getSpanRow(
    ) {
        return this.spanRow;
    }
  
    //-------------------------------------------------------------------------
    public List getEventHandler(
    ) {
        return this.eventHandler;
    }
  
    //-------------------------------------------------------------------------
    public String toString(
    ) {
        return this.label + "=" + this.value.toString();
    }

    //-------------------------------------------------------------------------
    public String getStringifiedValue(
        HtmlPage p,
        boolean forEditing,
        boolean shortFormat
    ) {
        return this.getValue().getStringifiedValue(
            p,
            this.getSpanRow() > 1,
            forEditing,
            shortFormat
        );
    }
  
    //-------------------------------------------------------------------------
    /**
     * Paints the attribute to page.
     * 
     * @param p target page
     * @param nCols column span
     * @param gapModifier gap modifier before attribute-specific code is generated. null if forEditing==true 
     * @param styleModifier style tag for generated element. null if forEditing==true
     * @param widthModifier width tag for generated element. null if forEditing==true
     * @param rowSpanModifier row span modifier
     * @param stringifiedValue stringified value of field
     * 
     * @throws ServiceException
     */
    public void paintForShow(
        HtmlPage p,
        int nCols,
        String gapModifier,
        String styleModifier,
        String widthModifier,
        String rowSpanModifier,
        String stringifiedValue
    ) throws ServiceException {
        this.paintForShow(
            p, 
            null, 
            nCols, 
            gapModifier, 
            styleModifier, 
            widthModifier, 
            rowSpanModifier, 
            stringifiedValue
        );
    }
    
    //-------------------------------------------------------------------------
    /**
     * Paints the attribute to page.
     * 
     * @param p target page
     * @param label attribute label. null for default label, i.e. this.getValue().getLabel()
     * @param nCols column span
     * @param gapModifier gap modifier before attribute-specific code is generated. null if forEditing==true 
     * @param styleModifier style tag for generated element. null if forEditing==true
     * @param widthModifier width tag for generated element. null if forEditing==true
     * @param rowSpanModifier row span modifier
     * @param stringifiedValue stringified value of field
     * 
     * @throws ServiceException
     */
    public void paintForShow(
        HtmlPage p,
        String label,
        int nCols,
        String gapModifier,
        String styleModifier,
        String widthModifier,
        String rowSpanModifier,
        String stringifiedValue
    ) throws ServiceException { 
        this.value.paint(
            this, 
            p, 
            null,
            label,
            null,
            nCols, 
            -1, 
            gapModifier, 
            styleModifier, 
            widthModifier, 
            rowSpanModifier, 
            null, 
            null,
            null, 
            stringifiedValue,
            false
        );
    }
  
    //-------------------------------------------------------------------------
    /**
     * Paints the attribute to page. Use default field id and label.
     * 
     * @param p target page
     * @param lookupObject optinal base object where object lookup starts from. null if forEditing==false
     * @param nCols column span
     * @param tabIndex tab index of generated input field.
     * @param rowSpanModifier row span modifier
     * @param readonlyModifier readonly modifier.
     * @param disabledModifier disabled modifier.
     * @param lockedModifier modifier to lock field.
     * @param stringifiedValue stringified value of field
     * 
     * @throws ServiceException
     */
    public void paintForEdit(
        HtmlPage p,
        RefObject_1_0 lookupObject,
        int nCols,
        int tabIndex,
        String rowSpanModifier,
        String readonlyModifier,
        String disabledModifier,
        String lockedModifier,
        String stringifiedValue
    ) throws ServiceException {
        this.paintForEdit(
            p, 
            null, 
            null, 
            lookupObject, 
            nCols, 
            tabIndex, 
            rowSpanModifier, 
            readonlyModifier, 
            disabledModifier, 
            lockedModifier, 
            stringifiedValue
        );
    }
    
    //-------------------------------------------------------------------------
    /**
     * Paints the attribute to page.
     * 
     * @param p target page
     * @param id optional input field id. null for default id
     * @param label attribute label. null for default label, i.e. this.getValue().getLabel()
     * @param lookupObject optinal base object where object lookup starts from. null if forEditing==false
     * @param nCols column span
     * @param tabIndex tab index of generated input field.
     * @param rowSpanModifier row span modifier
     * @param readonlyModifier readonly modifier.
     * @param disabledModifier disabled modifier.
     * @param lockedModifier modifier to lock field.
     * @param stringifiedValue stringified value of field
     * 
     * @throws ServiceException
     */
    public void paintForEdit(
        HtmlPage p,
        String id,
        String label,
        RefObject_1_0 lookupObject,
        int nCols,
        int tabIndex,
        String rowSpanModifier,
        String readonlyModifier,
        String disabledModifier,
        String lockedModifier,
        String stringifiedValue
    ) throws ServiceException { 
        this.value.paint(
            this, 
            p, 
            id,
            label,
            lookupObject,
            nCols, 
            tabIndex, 
            null, 
            null, 
            null, 
            rowSpanModifier, 
            readonlyModifier, 
            disabledModifier,
            lockedModifier, 
            stringifiedValue, 
            true
        );
    }
  
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3617860776384542256L;
  
    private boolean isEmpty;
    private String label = null;
    private String toolTip = null;
    private int spanRow;
    private List eventHandler;
    private AttributeValue value = null;
}
