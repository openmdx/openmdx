/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: ReferencedObjectDataBinding 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.databinding;

import java.util.Collection;

import javax.jmi.reflect.RefObject;

import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.DataBinding;

/**
 * Allows to set/get features of referenced objects.
 * 
 */
public class ReferencedObjectDataBinding extends DataBinding {

    /**
     * Constructor 
     *
     */
    public ReferencedObjectDataBinding(
    ) {
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.DataBinding#getValue(javax.jmi.reflect.RefObject, java.lang.String, org.openmdx.portal.servlet.ApplicationContext)
     */
    @Override
    public Object getValue(
        RefObject object, 
        String qualifiedFeatureName,
        ApplicationContext app
    ) {
        String[] referenceNames = this.getReferenceNames(qualifiedFeatureName);
        RefObject referenced = this.getReferencedObject(
            object, 
            referenceNames
        );
        if(referenced != null) {
            String attributeName = this.getAttributeName(qualifiedFeatureName);            
            return referenced.refGetValue(attributeName);
        }
        else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.DataBinding#setValue(javax.jmi.reflect.RefObject, java.lang.String, java.lang.Object, org.openmdx.portal.servlet.ApplicationContext)
     */
    @Override
    public void setValue(
        RefObject object, 
        String qualifiedFeatureName, 
        Object newValue,
        ApplicationContext app
    ) {
        String[] referenceNames = this.getReferenceNames(qualifiedFeatureName);
        RefObject referenced = this.getReferencedObject(
            object, 
            referenceNames
        );
        if(referenced != null) {
            String attributeName = this.getAttributeName(qualifiedFeatureName);      
            Object oldValue = referenced.refGetValue(attributeName);
            if(oldValue instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<Object> values = (Collection<Object>)oldValue;
                values.clear();
                if(newValue instanceof Collection) {
                    values.addAll((Collection<?>)newValue);
                } else {
                    values.add(newValue);
                }
            } else {
                referenced.refSetValue(
                    attributeName, 
                    newValue
                );
            }
        }
    }

}
