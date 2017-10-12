/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Filter Property
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.application.dataprovider.cci;

import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_BOOLEAN_PARAM;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_CLASS;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_CLAUSE;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_DATETIME_PARAM;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_DATE_PARAM;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_DECIMAL_PARAM;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_INTEGER_PARAM;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_STRING_PARAM;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.query.AnyTypeCondition;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.cci.ConditionRecord;
import org.openmdx.base.rest.cci.QueryExtensionRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.kernel.id.UUIDs;


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
     * Convert the filter properties to conditions
     * 
     * @param filterProperties
     * 
     * @return the corresponding conditions
     */
    public static List<Condition> toCondition(
        FilterProperty[] filterProperties
    ){
        if(filterProperties == null || filterProperties.length == 0) {
            return Collections.emptyList();
        } else {
            Condition[] conditions = new Condition[filterProperties.length];
            int i = 0;
            for(FilterProperty filterProperty : filterProperties) {
                conditions[i++] = new AnyTypeCondition(
                    Quantifier.valueOf(filterProperty.quantor()),
                    filterProperty.name(),
                    ConditionType.valueOf(filterProperty.operator()),
                    filterProperty.getValues()
                );
            }
            return Arrays.asList(conditions);
        }
    }

    /**
     * Convert a filter's conditions to filter properties
     * 
     * @param filter
     * 
     * @return the corresponding filter properties
     */
    public static List<FilterProperty> getFilterProperties(
        QueryFilterRecord filter
    ){
        if(filter == null) {
            return Collections.emptyList();
        } else {
            List<FilterProperty> filterProperties = new ArrayList<FilterProperty>();
            for(ConditionRecord condition : filter.getCondition()) {
                if(condition != null) {
                    filterProperties.add(
                        new FilterProperty(
                            Quantifier.codeOf(condition.getQuantifier()),
                            condition.getFeature(),
                            ConditionType.codeOf(condition.getType()),
                            condition.getValue()
                        )
                    );
                }
            }
            List<QueryExtensionRecord> extensions = filter.getExtension();
            if(extensions != null) {
            	for(QueryExtensionRecord extension: extensions) {
                    String namespace = SystemAttributes.CONTEXT_PREFIX + UUIDs.newUUID() + ':';
                    short piggyBackQuantifier = Quantifier.codeOf(null);
                    short piggyBackOperator = ConditionType.codeOf(null);
                    filterProperties.add(
                        new FilterProperty(
                            piggyBackQuantifier,
                            namespace + SystemAttributes.OBJECT_CLASS,
                            piggyBackOperator,
                            QUERY_EXTENSION_CLASS
                        )
                    );
                    filterProperties.add(
                        new FilterProperty(
                            piggyBackQuantifier,
                            namespace + QUERY_EXTENSION_CLAUSE,
                            piggyBackOperator,
                            extension.getClause()
                        )
                    );
                    filterProperties.add(
                        new FilterProperty(
                            piggyBackQuantifier,
                            namespace + QUERY_EXTENSION_BOOLEAN_PARAM,
                            piggyBackOperator,
                            extension.getBooleanParam().toArray()
                        )
                    );
                    filterProperties.add(
                        new FilterProperty(
                            piggyBackQuantifier,
                            namespace + QUERY_EXTENSION_DATE_PARAM,
                            piggyBackOperator,
                            extension.getDateParam().toArray()
                        )
                    );
                    filterProperties.add(
                        new FilterProperty(
                            piggyBackQuantifier,
                            namespace + QUERY_EXTENSION_DATETIME_PARAM,
                            piggyBackOperator,
                            extension.getDateTimeParam().toArray()
                        )
                    );
                    filterProperties.add(
                        new FilterProperty(
                            piggyBackQuantifier,
                            namespace + QUERY_EXTENSION_DECIMAL_PARAM,
                            piggyBackOperator,
                            extension.getDecimalParam().toArray()
                        )
                    );
                    filterProperties.add(
                        new FilterProperty(
                            piggyBackQuantifier,
                            namespace + QUERY_EXTENSION_INTEGER_PARAM,
                            piggyBackOperator,
                            extension.getIntegerParam().toArray()
                        )
                    );
                    filterProperties.add(
                        new FilterProperty(
                            piggyBackQuantifier,
                            namespace + QUERY_EXTENSION_STRING_PARAM,
                            piggyBackOperator,
                            extension.getStringParam().toArray()
                        )
                    );
            	}
            }
            return filterProperties;
        }
    }
    
    /**
     * The quantor
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
    @Override
    public String toString (
    ){
        Quantifier quantifier = Quantifier.valueOf(this.quantor());
        ConditionType conditionType = ConditionType.valueOf(this.operator());
        return Records.getRecordFactory().asMappedRecord(
		    this.getClass().getName(), 
		    (quantifier == null ? "PIGGY_BACK" : quantifier.name()) + " " + this.name() + " " + conditionType + " " + this.values(),
		    TO_STRING_FIELDS,
		    new Object[]{
		        quantifier,
		        this.name(), 
		        conditionType, 
		        this.values()
		    }
		).toString();
    }


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object) {
        if(!(object instanceof FilterProperty)) return false;
        FilterProperty that = (FilterProperty)object; 
        return this.quantor == that.quantor &&
        this.name.equals(that.name) &&
        this.operator == that.operator &&
        this.valuesAreEqual(this.values, that.values); 
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
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
        ConditionType type = ConditionType.valueOf(this.operator);
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
