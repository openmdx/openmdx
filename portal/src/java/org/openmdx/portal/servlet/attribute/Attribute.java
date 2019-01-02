/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: Attribute 
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
import java.util.List;

import org.openmdx.portal.servlet.ViewPort;

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
        List<String> eventHandler,
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
    public List<String> getEventHandler(
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
        ViewPort p,
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
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3617860776384542256L;
  
    private boolean isEmpty;
    private String label = null;
    private String toolTip = null;
    private int spanRow;
    private List<String> eventHandler;
    private AttributeValue value = null;
}
