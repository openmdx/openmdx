/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: Autocompleter_1_0
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
 * * Neither the name of the openMDX team nor the names of the contributors
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
     * Generates the html code for the auto-completer.
     * 
     * @param p painting area
     * @param id optional id for input field. If none is specified an id is automatically generated.        
     * @param tabIndex field index
     * @param fieldName name of auto-completion field
     * @param currentValue current value of auto-completion field
     * @param numericCompare if a numeric compare should be applied to current value
     *        and the auto-completer value list
     * @param tdTag td tag if the auto-completer is embedded in a table. null if no 
     *        td tag must be generated
     * @param imgTag optional image tag is placed right after the auto-complete input field
     * @param onChangeValueScript
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
        CharSequence imgTag,
        CharSequence onChangeValueScript
    ) throws ServiceException;

    /**
     * Returns true if selectable values are fixed
     */
    public boolean hasFixedSelectableValues(
    );
        
}
