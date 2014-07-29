/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Filter
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
package org.openmdx.base.query;

import static org.openmdx.application.dataprovider.layer.persistence.jdbc.Database_1_Attributes.QUERY_EXTENSION_BOOLEAN_PARAM;
import static org.openmdx.application.dataprovider.layer.persistence.jdbc.Database_1_Attributes.QUERY_EXTENSION_CLASS;
import static org.openmdx.application.dataprovider.layer.persistence.jdbc.Database_1_Attributes.QUERY_EXTENSION_CLAUSE;
import static org.openmdx.application.dataprovider.layer.persistence.jdbc.Database_1_Attributes.QUERY_EXTENSION_DATETIME_PARAM;
import static org.openmdx.application.dataprovider.layer.persistence.jdbc.Database_1_Attributes.QUERY_EXTENSION_DATE_PARAM;
import static org.openmdx.application.dataprovider.layer.persistence.jdbc.Database_1_Attributes.QUERY_EXTENSION_DECIMAL_PARAM;
import static org.openmdx.application.dataprovider.layer.persistence.jdbc.Database_1_Attributes.QUERY_EXTENSION_INTEGER_PARAM;
import static org.openmdx.application.dataprovider.layer.persistence.jdbc.Database_1_Attributes.QUERY_EXTENSION_STRING_PARAM;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.persistence.spi.QueryExtension;
import org.openmdx.base.resource.Records;
import org.w3c.cci2.AnyTypePredicate;

/**
 * A filter allows to retrieve subsets of filterable maps and to sort
 * its content. A filter consists of a set of (ANDed) filter conditions 
 * and a (ordered) list of order clauses.
 * 
 * The Filter class is bean-compliant. Hence, it can be externalized
 * with the XMLDecoder.
 * 
 * @see org.openmdx.base.text.conversion.JavaBeans
 */
public class Filter
    implements Serializable, AnyTypePredicate, Cloneable {

    /**
     * Constructor 
     *
     * @param that
     */
    private Filter(
        Filter that
    ){
        this.conditions = new ArrayList<Condition>(that.conditions.size());
        for(Condition condition : that.conditions) {
            this.conditions.add(condition.clone());
        }
        this.orderSpecifiers = new ArrayList<OrderSpecifier>(that.orderSpecifiers.size());
        for(OrderSpecifier orderSpecifier : this.orderSpecifiers) {
            this.orderSpecifiers.add(orderSpecifier.clone());
        }
        this.extensions = new ArrayList<Extension>(that.extensions.size());
        for(Extension extension : that.extensions) {
            this.extensions.add(extension.clone());
        }
    }

    /**
     * Constructs a filter with the specified conditions and
     * order specifiers.
     */
    public Filter(
        List<Condition> conditions,
        List<OrderSpecifier> orderSpecifiers,
        List<Extension> extensions
    ) {
        this.conditions = conditions == null ? new ArrayList<Condition>() : new ArrayList<Condition>(conditions);
        this.orderSpecifiers = orderSpecifiers == null ? new ArrayList<OrderSpecifier>() : new ArrayList<OrderSpecifier>(orderSpecifiers);
        this.extensions = extensions == null ? new ArrayList<Extension>() :  new ArrayList<Extension>(extensions); 
    }

    /**
     * Constructs an empty filter
     */
    public Filter(
    ) {
        this(null, null, null);
    }

    /**
     * Constructs a filter with the specified conditions and
     * order specifiers.
     */
    public Filter(
        Condition[] conditions,
        OrderSpecifier[] orderSpecifiers
    ) {
        this(
            conditions == null ? null : Arrays.asList(conditions),
            orderSpecifiers == null ? null : Arrays.asList(orderSpecifiers),
            null
        );
    }
    
    /**
     * Constructs a filter with the specified conditions
     * @param conditions
     */
    public Filter(
        Condition... conditions
    ) {
        this(
            conditions == null ? null : Arrays.asList(conditions),
            null, // order specifiers
            null // extension
        );
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3257285842266371888L;
    
    /**
     * The conditions
     */
    private final List<Condition> conditions;
    
    /**
     * The order
     */
    private final List<OrderSpecifier> orderSpecifiers;
    
    /**
     * The query extension
     */
    private List<Extension> extensions;
    
    /**
     * The fields for the toString() method
     */
    private static final String[] TO_STRING_FIELDS = {
        "condition",
        "orderSpecifier",
        "extension"
    };
    
    /**
     * Retrieve the conditions.
     * 
     * @return the write-through list of filter conditions
     */
    public List<Condition> getCondition(
    ) {
        return this.conditions;
    }

    /**
     * Retrieve the first value
     * 
     * @param condition
     * 
     * @return
     */
    private static String getFirstValue(
        Condition condition
    ){
        Object[] values = condition.getValue();
        return values == null || values.length == 0 || !(values[0] instanceof String) ? null : (String)values[0];
    }

    /**
     * Retrieve a type safe value list
     * 
     * @param valueClass
     * @param condition
     * 
     * @return the typed value list
     */
    private static <E> List<E> getValues(
        Class<E> valueClass,
        Condition condition
    ){
        List<E> target = new ArrayList<E>();
        Object[] source = condition.getValue();
        if(source != null){
            for(Object value : source) {
                target.add(valueClass.cast(value));
            }
        }
        return target;
    }
    
    /**
     * Replace the conditions
     * 
     * @param conditions
     */
    public void setCondition(
        List<Condition> conditions
    ) {
        this.conditions.clear();
        this.conditions.addAll(conditions);
    }
        
    /**
     * Required to decode legacy XML data
     * 
     * @deprecated use {@link Filter#getCondition()}.set(int,Condition)
     */
    @Deprecated
    public void setCondition(
        int index,
        Condition condition
    ) {
        this.conditions.set(
            index,
            condition
        );
    }

    /**
     * Required to decode legacy XML data
     * 
     * @deprecated use {@link Filter#setCondition(List)}
     */
    @Deprecated
    public void setCondition(
        Condition[] conditions
    ) {
        this.conditions.clear();
        if(conditions != null) {
            for(Condition candidateCondition : conditions) {
                if(candidateCondition.getType() == null) {
                    String piggyBackFeature = candidateCondition.getFeature();
                    if(
                        piggyBackFeature != null &&
                        piggyBackFeature.startsWith(SystemAttributes.CONTEXT_PREFIX) &&
                        piggyBackFeature.endsWith(SystemAttributes.OBJECT_CLASS) &&
                        QUERY_EXTENSION_CLASS.equals(Filter.getFirstValue(candidateCondition))
                    ){
                        String piggyBackNamespace = piggyBackFeature.substring(
                            0, 
                            piggyBackFeature.length() - SystemAttributes.OBJECT_CLASS.length()
                        );
                        QueryExtension extension = new QueryExtension();
                        for(Condition condition : conditions) {
                            String feature = condition.getFeature();
                            if(feature.startsWith(piggyBackNamespace)) {
                                piggyBackFeature = feature.substring(piggyBackNamespace.length());
                                if(QUERY_EXTENSION_CLAUSE.equals(piggyBackFeature)) {
                                    extension.setClause(Filter.getFirstValue(condition));
                                } else if (QUERY_EXTENSION_BOOLEAN_PARAM.equals(piggyBackFeature)) {
                                    extension.setBooleanParam(
                                        Filter.getValues(Boolean.class, condition)
                                    );
                                } else if (QUERY_EXTENSION_DATE_PARAM.equals(piggyBackFeature)) {
                                    extension.setDateParam(
                                        Filter.getValues(XMLGregorianCalendar.class, condition)
                                    );
                                } else if (QUERY_EXTENSION_DATETIME_PARAM.equals(piggyBackFeature)) {
                                    extension.setDateTimeParam(
                                        Filter.getValues(Date.class, condition)
                                    );
                                } else if (QUERY_EXTENSION_DECIMAL_PARAM.equals(piggyBackFeature)) {
                                    extension.setDecimalParam(
                                        Filter.getValues(BigDecimal.class, condition)
                                    );
                                } else if (QUERY_EXTENSION_INTEGER_PARAM.equals(piggyBackFeature)) {
                                    extension.setIntegerParam(
                                        Filter.getValues(Integer.class, condition)
                                    );
                                } else if (QUERY_EXTENSION_STRING_PARAM.equals(piggyBackFeature)) {
                                    extension.setStringParam(
                                        Filter.getValues(String.class, condition)
                                    );
                                }
                            } else {
                                this.conditions.add(condition);
                            }
                        }
                        this.getExtension().add(extension);
                        return;
                    }
                }
            }
            this.conditions.addAll(
                Arrays.asList(conditions)
            );
        }
    }

    /**
     * Retrieve the order specifiers.
     * 
     * @return the write-through list of order specifiers
     */
    public List<OrderSpecifier> getOrderSpecifier(){
        return this.orderSpecifiers;
    }
    
    /**
     * Replace the order specifiers
     * 
     * @param order specifiers
     */
    public void setOrderSpecifier(
        List<OrderSpecifier> orderSpecifiers
    ) {
        this.orderSpecifiers.clear();
        this.orderSpecifiers.addAll(orderSpecifiers);
    }
    
    /**
     * Returns the list of order specifiers.
     * 
     * @deprecated use {@link Filter#getCondition()}.toArray(new Condition[])
     */
    @Deprecated
    public OrderSpecifier[] getOrderSpecifierAsArray(
    ) {
        return this.orderSpecifiers.toArray(
            new OrderSpecifier[this.orderSpecifiers.size()]
        );
    }

    /**
     * Required to decode array based XML data
     * 
     * @deprecated use {@link Filter#getOrderSpecifier()}.set(int,order specifiers)
     */
    @Deprecated
    public void setOrderSpecifier(
        int index,
        OrderSpecifier orderSpecifier
    ) {
        this.orderSpecifiers.set(
            index,
            orderSpecifier
        );
    }

    /**
     * Required to decode array based XML data
     * 
     * @deprecated use {@link Filter#setOrderSpecifier(List)}
     */
    @Deprecated
    public void setOrderSpecifier(
        OrderSpecifier[] orderSpecifiers
    ) {
        this.orderSpecifiers.clear();
        if(orderSpecifiers != null) {
            this.orderSpecifiers.addAll(
                Arrays.asList(orderSpecifiers)
            );
        }
    }

    /**
     * Retrieve extension.
     *
     * @return Returns the extension.
     */
    public List<Extension> getExtension(
    ) {
        return this.extensions;
    }

    /**
     * Set extensions.
     * 
     * @param extension The extension to set.
     */
    public void setExtension(
        Extension[] extensions
    ) {
        this.extensions.clear();
        this.extensions.addAll(
            Arrays.asList(extensions)
        );        
    }

    /**
     * Set extension at index.
     * 
     * @param index
     * @param extension
     */
    public void setExtension(
        int index, 
        Extension extension
    ) {
        this.extensions.set(
            index, 
            extension
        );
    }

    /**
     * Set extensions.
     * 
     * @param extension
     */
    public void setExtension(
        List<Extension> extension
    ) {
        this.extensions.clear();
        this.extensions.addAll(extension);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(
    ) {
        return Records.getRecordFactory().asMappedRecord(
		    this.getClass().getName(),
		    null,
		    Filter.TO_STRING_FIELDS, 
		    new Object[]{
		        this.conditions,
		        this.orderSpecifiers,		        
		        this.extensions
		    }
		).toString();
    }

    //-------------------------------------------------------------------------
    // Implements Cloneable
    //-------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Filter clone(
    ){
        return new Filter(this);
    }

    
    //-------------------------------------------------------------------------
    // Implements AnyTypePredicate
    //-------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#elementOf(java.lang.Object[])
     */
    public void elementOf(Object... operands) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#elementOf(java.util.Collection)
     */
    public void elementOf(Collection<?> operands) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#equalTo(java.lang.Object)
     */
    public void equalTo(Object operand) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#notAnElementOf(java.lang.Object[])
     */
    public void notAnElementOf(Object... operands) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#notAnElementOf(java.util.Collection)
     */
    public void notAnElementOf(Collection<?> operands) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#notEqualTo(java.lang.Object)
     */
    public void notEqualTo(Object operand) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Filter) {
            Filter that = (Filter) obj;
            return 
                Filter.areEquivalent(this.conditions, that.conditions) && 
                Filter.areEquivalent(this.orderSpecifiers, that.orderSpecifiers) &&
                Filter.areEquivalent(this.extensions, that.extensions); 
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int code = 0;
        if(this.conditions != null) {
            code += conditions.hashCode();
        }
        if(this.orderSpecifiers != null) {
            code += this.orderSpecifiers.hashCode();
        }
        return code;
    }

    /**
     * Compares two value lists treating <code>null</code> as empty lists
     * 
     * @param left
     * @param right
     * 
     * @return if the two lists are similar
     */
    private static boolean areEquivalent(
        List<?> left,
        List<?> right
    ){
        return left == null || left.isEmpty() ? right == null || right.isEmpty() : left.equals(right);
    }

}
