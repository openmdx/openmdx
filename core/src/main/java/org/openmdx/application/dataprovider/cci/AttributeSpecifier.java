/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: openMDX Compatibility Mode: Attribute specifier
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
package org.openmdx.application.dataprovider.cci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.resource.Records;
import org.openmdx.base.rest.cci.FeatureOrderRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.kernel.exception.BasicException;


/**
 * Specifies an attribute to be retrieved
 */
public final class AttributeSpecifier implements Serializable {

    /**
     * Retrieve all attribute values
     * 
     * @param       name
     *              the attribute's name
     */
    public AttributeSpecifier(
        String name
    ) {
        this(name,SortOrder.UNSORTED.code());
    }

    /**
     * Constructor
     * 
     * @param       name
     *              the attribute's name
     * @param       order
     *              defines whether this attribute is relevant for the order;
     *              it is applicable to find requests only
     * @exception   IllegalArgumentException
     *              if any of the arguments has an illegal value
     */
    public AttributeSpecifier(
        String name,
        short order
    ){
        this(name, null, order);
    }

    /**
     * Constructor
     * 
     * @param       name
     *              the attribute's name
     * @param       pointer
     *              an XPath or JSON pointer
     * @param       order
     *              defines whether this attribute is relevant for the order;
     *              it is applicable to find requests only
     * @exception   IllegalArgumentException
     *              if any of the arguments has an illegal value
     */
    public AttributeSpecifier(
        String name,
        String pointer,
        short order
    ){
        Set<String> invalid = new HashSet<String>();
        if(
            name == null
        ) {
            invalid.add("name");
        }
        this.name = name;
        if(
            order != SortOrder.UNSORTED.code() &&
            order != SortOrder.ASCENDING.code() &&
            order != SortOrder.DESCENDING.code()
        ) {
            invalid.add("order");
        }
        this.order = order;
        if(
            pointer != null && 
            !pointer.startsWith("/")
        ) {
            invalid.add("pointer");
        }
        this.pointer = pointer;
        if (!invalid.isEmpty()) { 
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "Illegal value for arguments " + invalid,
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION,
                        new BasicException.Parameter("name",name),
                        new BasicException.Parameter("pointer",pointer),
                        new BasicException.Parameter("order",order)
                   )
               )
            );
        }
    }

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = -2014082079642825679L;
    
    /**
     * The attribute's name
     */
    final private String name;

    /**
     * The associated XPath or JSON pointer
     */
    final private String pointer;
    
    private static final String[] TO_STRING_FIELDS = {
        "name",
        "pointer",
        "order"
    };

    /**
     * Get the attribute's name
     *
     * @return the attribute's name
     */
    public String name(
    ){
        return this.name;
    }

    /**
     * Get the feature's pointer
     *
     * @return the feature's pointer
     */
    public String pointer(
    ){
        return this.pointer;
    }
    
    /**
     * Convert attribute specifiers to order specifiers
     * 
     * @param attributeSpecifiers
     * 
     * @return the corresponding order specifiers
     */
    public static List<OrderSpecifier> toOrderSpecifier(
        AttributeSpecifier[] attributeSpecifiers
    ){
        if(attributeSpecifiers == null || attributeSpecifiers.length == 0) {
            return Collections.emptyList();
        } else {
            OrderSpecifier[] orderSpecifiers = new OrderSpecifier[attributeSpecifiers.length];
            int i = 0;
            for(AttributeSpecifier attributeSpecifier: attributeSpecifiers) {
                orderSpecifiers[i++] = new OrderSpecifier(
                    attributeSpecifier.name(),
                    SortOrder.valueOf(attributeSpecifier.order())
                );
            }
            return Arrays.asList(orderSpecifiers);
        }
    }
                     
    /**
     * The sort order
     *
     * @see SortOrder
     */
    final private short order;

    /**
     * Get the sort order
     *
     * @return      the sort order
     *
     * @see SortOrder
     */
    public short order(){
        return this.order;
    }

    public static List<AttributeSpecifier> getAttributeSpecifiers(
        QueryFilterRecord filter
    ) {
        List<AttributeSpecifier> attributeSpecifiers = new ArrayList<AttributeSpecifier>();
        if(filter != null && filter.getOrderSpecifier() != null) {
            for(FeatureOrderRecord orderSpecifier: filter.getOrderSpecifier()) {
                final SortOrder sortOrder = orderSpecifier.getSortOrder();
                attributeSpecifiers.add(
                    new AttributeSpecifier(
                        orderSpecifier.featureName(),
                        orderSpecifier.featurePointer(),
                        (sortOrder == null ? SortOrder.UNSORTED : sortOrder).code()
                    )
                );
            }
        }
        return attributeSpecifiers;
    }

    public static Map<String,AttributeSpecifier> getAttributeSpecifierAsMap(
        QueryFilterRecord filter
    ) throws ServiceException {
        Map<String,AttributeSpecifier> attributeSpecifierAsMap = new HashMap<String,AttributeSpecifier>();
        List<AttributeSpecifier> attributeSpecifiers = getAttributeSpecifiers(filter);
        for(AttributeSpecifier attributeSpecifier: attributeSpecifiers) {
            attributeSpecifierAsMap.put(
                attributeSpecifier.name(),
                attributeSpecifier
            );
        }
        return attributeSpecifierAsMap;
    }

    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Records.getRecordFactory().asMappedRecord(
		    getClass().getName(), 
		    pointer == null ? name : name + pointer,
		    TO_STRING_FIELDS, 
		    new Object[]{
		        name,
		        pointer, 
		        SortOrder.valueOf(order)
		    }
		).toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object) {
        if(!(object instanceof AttributeSpecifier)) return false;
        AttributeSpecifier that = (AttributeSpecifier)object;
        return 
        this.name.equals(that.name) &&
        (this.pointer == null ? that.pointer == null : this.pointer.equals(that.pointer)) &&
        this.order == that.order;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
