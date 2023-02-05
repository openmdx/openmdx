/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Filter
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_BOOLEAN_PARAM;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_CLASS;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_CLAUSE;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_DATETIME_PARAM;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_DATE_PARAM;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_DECIMAL_PARAM;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_INTEGER_PARAM;
import static org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_1_Attributes.QUERY_EXTENSION_STRING_PARAM;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.persistence.spi.QueryExtension;
import org.openmdx.base.rest.cci.ConditionRecord;
import org.openmdx.base.rest.cci.FeatureOrderRecord;
import org.openmdx.base.rest.cci.QueryExtensionRecord;
import org.openmdx.base.rest.spi.QueryFilterRecord;

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
public class Filter extends QueryFilterRecord {

	/**
	 * Constructs an empty filter
	 */
	public Filter(
	) {
		super();
	}
	
    /**
     * Constructs a filter with the specified conditions and
     * order specifiers.
     */
    public Filter(
        List<? extends ConditionRecord> conditions,
        List<? extends FeatureOrderRecord> orderSpecifiers,
        List<? extends QueryExtensionRecord> extensions
    ) {
    	super(conditions, orderSpecifiers, extensions);
    }

    /**
     * Constructs a filter with the specified conditions and
     * order specifiers.
     */
    public Filter(
        Condition[] conditions,
        OrderSpecifier[] orderSpecifiers
    ) {
    	super(Arrays.asList(conditions), Arrays.asList(orderSpecifiers), null);
    }
    
    /**
     * Constructs a filter with the specified conditions
     * 
     * @param conditions
     */
    public Filter(
        Condition... conditions
    ) {
    	super(Arrays.asList(conditions), null, null);
    }

    /**
     * Constructor for clones 
     *
     * @param that
     */
    private Filter(
		Filter that
	){
    	super(that);
    }
    
    /**
     * Implements {@code Serializable}
     */
	private static final long serialVersionUID = -2577782503585083499L;
    
    /**
     * Retrieve the first value
     * 
     * @param condition
     * 
     * @return
     */
    private static String getFirstValue(
        ConditionRecord condition
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
        ConditionRecord condition
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
    	replaceValues(getCondition(), conditions);
    }
        
    /**
     * Required to decode legacy XML data
     * 
     */
    public void setCondition(
        int index,
        ConditionRecord condition
    ) {
    	super.getCondition().set(
            index,
            condition
        );
    }

    public ConditionRecord getCondition(
        int index
    ) {
        return super.getCondition().get(index);
    }
    
    /**
     * Required to decode legacy XML data
     * 
     */
    public void setCondition(
        ConditionRecord[] conditions
    ) {
    	final List<ConditionRecord> target = getCondition();
    	target.clear();
        if(conditions != null) {
            for(ConditionRecord candidateCondition : conditions) {
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
                        for(ConditionRecord condition : conditions) {
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
                            	target.add(condition);
                            }
                        }
                        this.getExtension().add(extension);
                        return;
                    }
                }
            }
            target.addAll(
                Arrays.asList(conditions)
            );
        }
    }

    /**
     * Replace the order specifiers
     * 
     * @param order specifiers
     */
    public void setOrderSpecifier(
        List<OrderSpecifier> orderSpecifiers
    ) {
    	replaceValues(getOrderSpecifier(), orderSpecifiers);
    }
    
    /**
     * Returns the list of order specifiers.
     * 
     * @deprecated use {@link Filter#getCondition()}.toArray(new Condition[])
     */
    @Deprecated
    public FeatureOrderRecord[] getOrderSpecifierAsArray(
    ) {
    	final List<FeatureOrderRecord> orderSpecifier = super.getOrderSpecifier();
    	return orderSpecifier.toArray(
			new FeatureOrderRecord[orderSpecifier.size()]
        );
    }

    public FeatureOrderRecord getOrderSpecifier(
        int index
    ) {
        return getOrderSpecifier().get(index);
    }
    
    /**
     * Required to decode array based XML data
     * 
     */
    public void setOrderSpecifier(
        int index,
        FeatureOrderRecord orderSpecifier
    ) {
        super.getOrderSpecifier().set(
            index,
            orderSpecifier
        );
    }

    /**
     * Required to decode array based XML data
     * 
     */
    public void setOrderSpecifier(
        FeatureOrderRecord[] orderSpecifiers
    ) {
    	replaceValues(getOrderSpecifier(), orderSpecifiers);
    }

    /**
     * Set extensions.
     * 
     * @param extension The extension to set.
     */
    public void setExtension(
        QueryExtensionRecord[] extensions
    ) {
    	replaceValues(getExtension(), extensions);
    }

    public QueryExtensionRecord getExtension(
        int index
    ) {
        return super.getExtension().get(index);
    }
    
    /**
     * Set extension at index.
     * 
     * @param index
     * @param extension
     */
	public void setExtension(
        int index, 
        QueryExtensionRecord extension
    ) {
        super.getExtension().set(
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
        List<QueryExtensionRecord> extension
    ) {
    	replaceValues(getExtension(), extension);
    }


    //-------------------------------------------------------------------------
    // Implements Record
    //-------------------------------------------------------------------------
    
    @Override
    public String getRecordShortDescription() {
    	return null;
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


}
