/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: DefaultDataBinding.java,v 1.3 2008/04/13 11:39:31 wfro Exp $
 * Description: DefaultDataBinding 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/13 11:39:31 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */

package org.openmdx.portal.servlet;

import java.util.Collection;

import javax.jmi.reflect.RefObject;

/**
 * DefaultDataBinding
 *
 */
public class DefaultDataBinding 
    implements DataBinding_1_0 {

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.DataBinding_1_0#getValue(org.openmdx.base.accessor.jmi.cci.RefObject_1_0, java.lang.String)
     */
    public Object getValue(
        RefObject object, 
        String qualifiedFeatureName
    ) {
        try {
            Object value = object.refGetValue(
                qualifiedFeatureName
            );
            return value;
        }
        // Return null in case of a NullPointer
        catch(NullPointerException e) {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.DataBinding_1_0#setValue(org.openmdx.base.accessor.jmi.cci.RefObject_1_0, java.lang.String, java.lang.Object)
     */
    public void setValue(
        RefObject object, 
        String qualifiedFeatureName, 
        Object newValue
    ) {
        if(newValue instanceof Collection) {
            Collection newValues = (Collection)newValue;
            Collection values = (Collection)object.refGetValue(
                qualifiedFeatureName
            );
            values.clear();
            values.addAll(newValues);
        }
        else {
            object.refSetValue(
                qualifiedFeatureName,
                newValue
            );
        }
    }
    
}
