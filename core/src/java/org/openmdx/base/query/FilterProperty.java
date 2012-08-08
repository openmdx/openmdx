/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: FilterProperty.java,v 1.4 2009/06/18 18:24:40 hburger Exp $
 * Description: Filter Property
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/18 18:24:40 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.base.query;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.resource.ResourceException;

import org.openmdx.base.resource.Records;


/**
 * A Filter Property consists of<ol>
 * <li>a quantor
 * <li>the name of the attribute
 * <li>an operator
 * <li>an array of values (its minimal and maximal length is defined by the operator
 * </ol> 
 */
public final class FilterProperty implements Serializable  {

    /**
     * Do NOT use! Required for Externalizable.
     */
    public FilterProperty(
    ) {        
    }

    /**
     * Creates a new FilterProperty
     *
     * @param     quantor
     *            defines which quantor has to be applied
     * @param     name
     *            defines to which attribute to filter has to be applied
     * @param     operator
     *            defines which operator has to be applied
     * @param     values
     *            Defines the "right" operands of the operator   
     */
    public FilterProperty(
        short quantor,
        String name,
        short operator,
        Object... values
    ) {
        this.quantor = quantor;
        this.name = name.intern();
        this.operator = operator;
        this.values = values;
    }

    /**
     * Creates a new FilterProperty
     *
     * @param     quantor
     *            defines which quantor has to be applied
     * @param     name
     *            defines to which attribute to filter has to be applied
     * @param     operator
     *            defines which operator has to be applied
     */
    public FilterProperty(
        short quantor,
        String name,
        short operator
    ) {
        this.quantor = quantor;
        this.name = name.intern();
        this.operator = operator;
        this.values = NO_VALUES;
    }

    /**
     * The quantor
     * 
     * @see org.openmdx.base.query.Quantors
     */
    public final short quantor(){
        return this.quantor;
    }

    /**
     *
     */
    public final String name(){
        return this.name;
    }

    /**
     * The operator
     *
     * @see org.openmdx.base.query.FilterOperators
     */
    public final short operator(
    ){
        return this.operator;
    }

    /**
     * 
     */
    public Object[] getValues(){
        return this.values;
    }

    /**
     * @param index
     */
    public Object getValue(
        int index
    ){
        return this.values[index];
    }

    /**
     * 
     */
    public List<Object> values(){
        return Arrays.asList(this.values);
    }

    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /**
     * Returns a string representation of the object. In general, the toString
     * method returns a string that "textually represents" this object. The
     * result should be a concise but informative representation that is easy
     * for a person to read. It is recommended that all subclasses override
     * this method. 
     *
     * @return the filter property's string representation
     */
    public String toString (
    ){
        try {
            return Records.getRecordFactory().asMappedRecord(
                getClass().getName(), 
                Quantors.toString(quantor()) + ' ' + name() + ' ' + 
                FilterOperators.toString(operator()) + ' ' + values(),
                TO_STRING_FIELDS,
                new Object[]{
                    Quantors.toString(quantor()),
                    name(), 
                    FilterOperators.toString(operator()), 
                    values()
                }
            ).toString();
        } catch (ResourceException exception) {
            return super.toString();
        }
    }


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object) {
        if(!(object instanceof FilterProperty)) return false;
        FilterProperty that = (FilterProperty)object; 
        return this.quantor == that.quantor &&
        this.name.equals(that.name) &&
        this.operator == that.operator &&
        valuesAreEqual(this.values, that.values); 
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.name.hashCode();
    }

    /**
     * Compare the arrays as list or set, depending on the filter operator
     * 
     * @param left
     * @param right
     * 
     * @return true if the values are equal
     */
    private boolean valuesAreEqual(
        Object[] left,
        Object[] right
    ){
        switch (this.operator){
            case FilterOperators.IS_IN: 
            case FilterOperators.IS_LIKE: 
            case FilterOperators.SOUNDS_LIKE: 
            case FilterOperators.IS_NOT_IN: 
            case FilterOperators.IS_UNLIKE: 
            case FilterOperators.SOUNDS_UNLIKE: 
                List<Object> l = Arrays.asList(left);
                List<Object> r = Arrays.asList(right);
                return r.containsAll(l) && l.containsAll(r);
            default:
                return Arrays.equals(left, right);
        }
    }

    //------------------------------------------------------------------------
    // Members
    //------------------------------------------------------------------------
    private static final long serialVersionUID = 3976738060186890550L;

    private String name;
    private short operator;
    private short quantor;
    private Object[] values;

    private static final String[] TO_STRING_FIELDS = {
        "quantor",
        "name",
        "operator",
        "values"
    };

    private static final Object[] NO_VALUES = {};

}
