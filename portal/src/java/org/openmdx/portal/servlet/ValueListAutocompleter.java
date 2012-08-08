/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ValueListAutocompleter.java,v 1.17 2009/09/25 12:02:38 wfro Exp $
 * Description: ListAutocompleteControl 
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/25 12:02:38 $
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
package org.openmdx.portal.servlet;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.attribute.AttributeValue;

public class ValueListAutocompleter
    implements Autocompleter_1_0, Serializable {
  
    //-------------------------------------------------------------------------
    public ValueListAutocompleter(
        List options
    ) {
        this.options = options;
    }
    
    //-----------------------------------------------------------------------
    public void paint(
        ViewPort p,
        String id,
        int tabIndex,
        String fieldName,
        AttributeValue currentValue,
        boolean numericCompare,
        CharSequence tdTag,
        CharSequence inputFieldDivClass,
        CharSequence inputFieldClass,
        CharSequence imgTag
    ) throws ServiceException {
        
    	SysLog.detail("> paint");        
        
        ApplicationContext app = p.getApplicationContext();
        HtmlEncoder_1_0 htmlEncoder = app.getHtmlEncoder();
        id = (id == null) || (id.length() == 0)
            ? fieldName + "[" + tabIndex + "]"
            : id;
        p.write("<select id=\"", id, "\" ", (inputFieldClass == null ? "" : inputFieldClass), " name=\"", id, "\" tabindex=\"", Integer.toString(tabIndex), "\">");
        int ii = 0;
        for(Iterator i = this.options.iterator(); i.hasNext(); ii++) {
            Object option = i.next();
            String selectedModifier = "";
            if(currentValue != null) {
                if(numericCompare) {
                    BigDecimal d1 = app.parseNumber((String)option);
                    if(d1 == null) {
                    	SysLog.warning("Option for numeric field is not a number", Arrays.asList(new Object[]{fieldName, option, this.options}));
                    }
                    BigDecimal d2 = app.parseNumber((String)currentValue.getValue(false));
                    if(d2 == null) {
                    	SysLog.warning("Numeric attribute value can not be parsed as number", Arrays.asList(new Object[]{fieldName, option}));                        
                    }
                    selectedModifier = (d1 != null) && (d2 != null)  
                        ? d1.compareTo(d2) == 0 ? "selected" : ""
                        : option.equals(currentValue.getValue(false)) ? "selected" : "";                                                        
                }
                else {
                    selectedModifier = option.equals(currentValue.getValue(false)) ? "selected" : "";                    
                }
            }
            if(option instanceof ObjectReference) {
                ObjectReference r = (ObjectReference)option;
                p.write("  <option ", selectedModifier, " value=\"", r.refMofId(), "\">", r.getTitle());
            }
            else {
                String optionEncoded = htmlEncoder.encode("" + option, false);
                p.write("  <option ", selectedModifier, " value=\"", optionEncoded, "\">", optionEncoded);                
            }
        }
        p.write("</select>");
    }

    //-----------------------------------------------------------------------
    public boolean hasFixedSelectableValues(
    ) {
        return true;
    }
        
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = -1138020420475572050L;
    
    private final List options;
    
}
