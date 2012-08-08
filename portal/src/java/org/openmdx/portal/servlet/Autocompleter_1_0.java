/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: Autocompleter_1_0.java,v 1.15 2009/09/25 12:02:38 wfro Exp $
 * Description: Autocompleter_1_0
 * Revision:    $Revision: 1.15 $
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * Date:        $Date: 2009/09/25 12:02:38 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of CRIXP Corp. nor the names of the contributors
 * to openCRX may be used to endorse or promote products derived
 * from this software without specific prior written permission
 * 
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
 * This product includes software developed by contributors to
 * openMDX (http://www.openmdx.org/)
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */
package org.openmdx.portal.servlet;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.attribute.AttributeValue;

public interface Autocompleter_1_0 {
    
    /**
     * Generates the html code for the autocompleter.
     * @param p painting area
     * @param id optional id for input field. If none is specified an id is automatically generated.        
     * @param tabIndex field index
     * @param fieldName name of autocompletion field
     * @param currentValue current value of autocompletion field
     * @param numericCompare if a numeric compare should be applied to current value
     *        and the autocompleter value list
     * @param tdTag td tag if the autcompleter is embedded in a table. null if no 
     *        td tag must be generated
     * @param imgTag optional image tag is placed right after the autocomplete input field
     */
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
    ) throws ServiceException;

    /**
     * Returns true if selectable values are fixed
     */
    public boolean hasFixedSelectableValues(
    );
        
}
