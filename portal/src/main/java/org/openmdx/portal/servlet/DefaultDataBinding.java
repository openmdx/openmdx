/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: DefaultDataBinding 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
public class DefaultDataBinding extends DataBinding {

    @SuppressWarnings("unchecked")
    static protected Collection<Object> valueAsCollection(
    	Object value
    ) {
    	return (Collection<Object>)value;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.DataBinding_1_0#getValue(org.openmdx.base.accessor.jmi.cci.RefObject_1_0, java.lang.String)
     */
    @Override
    public Object getValue(
        RefObject object, 
        String qualifiedFeatureName,
        ApplicationContext app
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
    @Override
    public void setValue(
        RefObject object, 
        String featureName, 
        Object newValue,
        ApplicationContext app
    ) {
    	// Qualified feature name
    	if(featureName.indexOf(":") > 0) {
    		featureName = featureName.substring(featureName.lastIndexOf(":") + 1);
    	}
        if(newValue instanceof Collection) {
            Collection<Object> newValues = valueAsCollection(newValue);
            Collection<Object> values = valueAsCollection(
            	object.refGetValue(
            		featureName
                )
            );
            values.clear();
            values.addAll(newValues);
        }
        else {
            object.refSetValue(
                featureName,
                newValue
            );
        }
    }
    
}
