/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Condition.java,v 1.17 2011/11/26 01:34:59 hburger Exp $
 * Description: Condition
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/11/26 01:34:59 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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
import java.util.Collections;
import java.util.List;

import org.openmdx.base.resource.Records;

/**
 * Abstract Condition
 */
public abstract class Condition implements Serializable, Cloneable {

    /**
     * Constructor 
     */
    protected Condition(
    ) {
        this(null, null, (Object[])null);
    }

    /**
     * Constructor 
     *
     * @param quantifier
     * @param feature
     * @param values
     */
    protected Condition(
        Quantifier quantifier,
        String feature,
        Object... values
    ) {
        this.quantifier = quantifier;
        this.feature = feature;
        this.values = values;
    }

    /**
     * The quantifier, i.e. &#x2200; or &#x2203;  
     */
    private Quantifier quantifier;
    
    /**
     * The unqualified feature name
     */
    private String feature;
    
    /**
     * The condition specific values
     */
    private Object[] values;
    
    private static final String[] TO_STRING_FIELDS = {
        "quantifier",
        "feature",
        "type",
        "values"
    };
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -3115618018740431736L;

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone(
    ) throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Retrieve the quantifier's <code>enum</code> representation
     * 
     * @return the quantifier's <code>enum</code> representation
     */
    public Quantifier getQuantifier() {
        return this.quantifier;
    }
  
    /**
     * Set the quantifier's <code>enum</code> representation
     * 
     * @param the quantifier's <code>enum</code> representation
     */
    public void setQuantifier(
        Quantifier quantifier
    ) {
        this.quantifier = quantifier;
    }
    
    /**
     * Required to decode legacy XML data
     * 
     * @deprecated use {@link Condition#setQuantifier(Quantifier)}
     */
    @Deprecated
    public void setQuantor(
        short quantor
    ) {
        this.quantifier = Quantifier.valueOf(quantor);
    }

    /**
     * Retrieve the feature name
     * 
     * @return the unqualified feature name
     */
    public String getFeature() {
        return this.feature;
    }

    /**
     * Set the feature name
     * 
     * @param the unqualified feature name
     */
    public void setFeature(
        String feature
    ) {
        this.feature = feature;
    }

    /**
     * Retrieve the values (with a condition type specific semantic and cardinality)
     * 
     * @return the values
     */
    public Object[] getValue(
    ) {
        return this.values;
    }

    /**
     * Replace the values (with a condition type specific semantic and cardinality)
     * 
     * @param the values
     */
    public void setValue(
        Object... values
    ) {
        this.values = values;
    }

    /**
     * Retrieve one of the values (with a condition type specific semantic)
     * 
     * @param index
     * 
     * @return the values
     */
    public Object getValue(
        int index
    ) {
        return this.values[index];
    }

    /**
     * Replace a single value (with a condition type specific semantic)
     * 
     * @param index the index
     * @param value the value
     */
    public void setValue(
        int index,
        Object value
    ) {
        this.values[index] = value;
    }

    /**
     * Retrieve the condition type's string representation
     * 
     * @return the condition type's name
     */
    public String getName(
    ){
        ConditionType type = this.getType();
        return type == null ? "PIGGY_BACK" : type.name();
    }

    /**
     * Retrieve the condition type's character representation
     * 
     * @return the condition type's symbol
     */
    public char getSymbol(
    ){
        ConditionType type = this.getType();
        return type == null ? '\uFFFC' : type.symbol();
    }

    /**
     * Retrieve the condition type's character representation
     * 
     * @return the condition type's symbol
     */
    public abstract ConditionType getType();

    private String getTypeName(
        List<?> values
    ){
        ConditionType type = this.getType();
        if(type == null) {
            return null;
        } else if (!values.isEmpty() && values.get(0) instanceof Filter){
            switch(type){
                case IS_IN: return "WHERE";
                case IS_NOT_IN: return "WHERE_NOT";
            }
        }
        return this.getType().name();
    }
    
    private String getDescription(
        List<?> values
    ){
        ConditionType type = this.getType();
        if(type == null) {
            return null;
        } else {
            StringBuilder description = new StringBuilder();
            if(this.quantifier != null) {
                description.append(
                    this.quantifier.symbol()
                ).append(
                    ' '
                ).append(
                    this.feature
                ).append(
                    " | "
                ).append(
                    this.feature
                ).append(
                    ' '
                );
            }
            return description.append(
                type.symbol()
            ).append(
                ' '
            ).append(
                values
            ).toString();
        }
    }
    
    /**
     * Retrieve the condition's string representation, e.g.<code> 
     * &#x2026;(&#x2200; color | color &#x2208; ["RED", "GREEN"])&#x2026;</code> 
     * 
     * @return the condition's string representation
     */
    @Override
    public String toString (
    ) {
        List<?> values = this.values == null ? Collections.emptyList() : Arrays.asList(this.values);
		return Records.getRecordFactory().asMappedRecord(
		    this.getClass().getName(), 
		    getDescription(values),
		    Condition.TO_STRING_FIELDS, 
		    new Object[]{
		        this.quantifier,
		        this.feature, 
		        getTypeName(values), 
		        values
		    }
		).toString();
    }
  
    /**
     * Compare ths object to another one
     * 
     * @param obj the other object
     * 
     * @return <code>true</code> if the other object represents the same condition
     */
    @Override
    public boolean equals(
        Object obj
    ) {
        if(obj instanceof Condition) {
            Condition that = (Condition)obj;
            return 
                this.quantifier == that.quantifier &&
                this.feature.equals(that.feature) &&
                this.getType() == that.getType() &&
                this.areEqual(this.values, that.values);
        } else {
            return false;
        }
    }

    /**
     * Compare the value arrays, treating empty and <code>null</code> as equal
     * 
     * @param left
     * @param right
     * 
     * @return if the value arrays are equal
     */
    protected boolean areEqual(
        Object[] left,
        Object[] right
    ){
        if(left == null || left.length == 0) {
            return right == null || right.length == 0; 
        } else if (right == null){
            return false;
        } else {
            ConditionType type = this.getType();
            if(
                type == ConditionType.IS_IN ||
                type == ConditionType.IS_NOT_IN ||
                type == ConditionType.IS_LIKE ||
                type == ConditionType.IS_UNLIKE ||
                type == ConditionType.SOUNDS_LIKE ||
                type == ConditionType.SOUNDS_UNLIKE 
           ){
               List<Object> l = Arrays.asList(left);
               List<Object> r = Arrays.asList(right);
               return r.containsAll(l) && l.containsAll(r);
            } else {
               return Arrays.equals(left, right);
           }
        }
    }
    
}
