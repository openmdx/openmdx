/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: State_1_Attributes.java,v 1.1 2009/05/26 14:31:21 wfro Exp $
 * Description: Generated constants for State_1_Attributes
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/26 14:31:21 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
 */
package org.openmdx.application.dataprovider.layer.model;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;

/**
 * Attributes used by State_1.
 */
public class State_1_Attributes {

    protected State_1_Attributes() {
        // Avoid instantiation
    }


    /**
     * Attribute name.
     */
    static public final String STATED_OBJECT = "statedObject";

    /**
     * Attribute name.
     */
    static public final String KEEPING_INVALIDATED_STATES = "keepingInvalidatedStates";

    /**
     * 
     */
    public static final String CREATED_AT_ALIAS = SystemAttributes.MODIFIED_AT;

    /**
     * 
     */
    public static final String REMOVED_AT_ALIAS = "object_invalidatedAt";

    /**
     * 
     */
    public static final String REMOVED_BY_ALIAS = SystemAttributes.MODIFIED_BY;


    /**
     * Search for stated object filter property
     * 
     * @param attributeFilter the attribute filter to be searched for
     * 
     * @return the index of the stated object filter property, or <code>-1</code>
     * if it is not found
     */
    public static int indexOfStatedObject(
        FilterProperty[] attributeFilter
    ){
        for(
                int i = 0;
                i < attributeFilter.length;
                i++
        ){
            FilterProperty filter = attributeFilter[i];
            if (
                    Quantors.THERE_EXISTS == filter.quantor() &&
                    State_1_Attributes.STATED_OBJECT.equals(filter.name()) &&
                    filter.operator() == (
                            filter.values().isEmpty() ? FilterOperators.IS_NOT_IN : FilterOperators.IS_IN  
                    )
            ) {
                return i;
            }                
        }
        return -1;
    }

}
