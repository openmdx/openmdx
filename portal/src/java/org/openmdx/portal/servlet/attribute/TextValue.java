/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: TextValue.java,v 1.14 2009/09/25 12:02:38 wfro Exp $
 * Description: TextValue 
 * Revision:    $Revision: 1.14 $
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
package org.openmdx.portal.servlet.attribute;

import java.io.Serializable;

import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.ViewPort;

public class TextValue
    extends AttributeValue
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public static AttributeValue createTextValue(
        Object object,
        FieldDef fieldDef,
        boolean isPassword,
        int maxLength,
        ApplicationContext application    
    ) {
        // Return user defined attribute value class or TextValue as default
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
            : new TextValue(
                object,
                fieldDef,
                isPassword,
                maxLength,
                application
            );
    }
    
    //-------------------------------------------------------------------------
    protected TextValue(
        Object object,
        FieldDef fieldDef,
        boolean isPassword,
        int maxLength,
        ApplicationContext application    
    ) {
        super(
            object, 
            fieldDef,
            application
        );
        this.isPassword = isPassword;
        this.maxLength = maxLength;
    }

    //-------------------------------------------------------------------------
    public boolean isPassword(
    ) {
        return this.isPassword;
    }

    //-------------------------------------------------------------------------
    public Object getDefaultValue(
    ) {      
        return this.fieldDef.defaultValue;
    }
  
    //-------------------------------------------------------------------------
    public int getMaxLength(
    ) {      
        return this.maxLength;
    }
  
    //-------------------------------------------------------------------------
    /**
     * Prepares a single stringified Value to append.
     */
    protected String getStringifiedValueInternal(
        ViewPort p, 
        Object v,
        boolean multiLine,
        boolean forEditing,
        boolean shortFormat
    ) {            
        return this.application.getHtmlEncoder().encode(v.toString().trim(), forEditing);
    }
  
    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3258688819002619449L;

    private final boolean isPassword;
    private final int maxLength;
  
}

//--- End of File -----------------------------------------------------------
