/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: DataBinding
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2012, OMEX AG, Switzerland
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

import javax.jmi.reflect.RefObject;

/**
 * Base class for data binding implementations.
 *
 */
public abstract class DataBinding {

    /**
     * Get value of specified feature.
     * @param object
     * @param qualifiedFeatureName
     * @param app
     * @return
     */
    public abstract Object getValue(
        RefObject object,
        String qualifiedFeatureName,
        ApplicationContext app
    );

    /**
     * Set value for specified feature.
     * @param object
     * @param qualifiedFeatureName
     * @param newValue
     * @param app
     */
    public abstract void setValue(
        RefObject object,
        String qualifiedFeatureName,
        Object newValue,
        ApplicationContext app
    );
    
    /**
     * Same as getValue() with app == null.
     * @param object
     * @param qualifiedFeatureName
     * @return
     */
    public Object getValue(
        RefObject object,
        String qualifiedFeatureName
    ) {
    	return this.getValue(
    		object, 
    		qualifiedFeatureName, 
    		null // app
    	);
    }

    /**
     * Set as setValue() with app == null.
     * @param object
     * @param qualifiedFeatureName
     * @param newValue
     */
    public void setValue(
        RefObject object,
        String qualifiedFeatureName,
        Object newValue
    ) {
    	this.setValue(
    		object, 
    		qualifiedFeatureName, 
    		newValue, 
    		null // app
    	);
    }

    /**
     * Extract attribute name from qualified feature name.
     * @param qualifiedFeatureName
     * @return
     */
    public String getAttributeName(
       String qualifiedFeatureName
    ) {
        return qualifiedFeatureName.substring(
            qualifiedFeatureName.lastIndexOf("!") + 1
        );        
    }
    
    /**
     * Extract reference names from qualified feature name.
     * @param qualifiedFeatureName
     * @return
     */
    public String[] getReferenceNames(
       String qualifiedFeatureName
    ) {        
        String qualifiedReferenceName = qualifiedFeatureName.substring(
            0, 
            qualifiedFeatureName.indexOf("!")
        );
        String referenceNames = qualifiedReferenceName.indexOf("*") > 0 ?
            qualifiedReferenceName.substring(qualifiedReferenceName.lastIndexOf(":") + 1, qualifiedReferenceName.indexOf("*")) :
            qualifiedReferenceName.substring(qualifiedReferenceName.lastIndexOf(":") + 1);
        return referenceNames.startsWith("(") && referenceNames.endsWith(")") ?
            referenceNames.substring(1, referenceNames.length()-1).split(";") :
            referenceNames.split(";");
    }
        
    /**
     * Return object referenced by referenceNames.
     * @param object
     * @param referenceNames
     * @return
     */
    public RefObject getReferencedObject(
        RefObject object,
        String[] referenceNames
    ) {
        RefObject referencedObject = object;
        for(int i = 0; i < referenceNames.length-1; i++) {
            referencedObject = (RefObject)referencedObject.refGetValue(referenceNames[i]);
            if(referencedObject == null) break;
        }
        return referencedObject;
    }
        
}
