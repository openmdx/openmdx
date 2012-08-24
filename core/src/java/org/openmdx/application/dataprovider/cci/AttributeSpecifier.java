/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: openMDX Compatibility Mode: Attribute specifier
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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.resource.Records;
import org.openmdx.kernel.exception.BasicException;


/**
 * Specifies an attribute to be retrieved
 */
public final class AttributeSpecifier
implements Serializable
{

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3834306220118193458L;

    /**
     * Constructor
     * 
     * @param name
     * @param position
     * @param size
     * @param direction
     * @param order
     * 
     * @exception   IllegalArgumentException
     *              if any of the arguments has an illegal value
     */
    public AttributeSpecifier(
        String name,
        int position,
        int size,
        short direction,
        short order
    ){
        Set<String> invalid = new HashSet<String>();

        if(
                name == null
        ) invalid.add("position");
        this.name = name;

        if(
                position < 0
        ) invalid.add("position");
        this.position = position;

        if(
                size < 0
        ) invalid.add("size");
        this.size = size;

        if(
                direction != SortOrder.ASCENDING.code() &&
                direction != SortOrder.DESCENDING.code()
        ) invalid.add("direction");
        this.direction = direction;

        if(
                order != SortOrder.UNSORTED.code() &&
                order != SortOrder.ASCENDING.code() &&
                order != SortOrder.DESCENDING.code()
        ) invalid.add("order");
        this.order = order;

        if (! invalid.isEmpty()) { 
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "Illegal value for arguments " + invalid,
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION,
                        new BasicException.Parameter("name",name),
                        new BasicException.Parameter("position",position),
                        new BasicException.Parameter("size",size),
                        new BasicException.Parameter("direction",direction),
                        new BasicException.Parameter("order",order)
                   )
               )
            );
        }
    }

    /**
     * Retrieve all attribute values
     */
    public AttributeSpecifier(
        String name
    ) {
        this(name,0,Integer.MAX_VALUE,SortOrder.ASCENDING.code(),SortOrder.UNSORTED.code());
    }

    /**
     * Retrieve the specified attribute values
     *
     * @param       name
     *              the attribute's name
     * @param       position
     *              start position of the values to be retrieved
     * @param       size
     *              the maximum size of the values to be retrieved
     * @param       direction
     *              the direction of the retrieval
     *
     * @see SortOrder
     */
    public AttributeSpecifier(
        String name,
        int position,
        int size,
        short direction
    ) {
        this(name,position,size,direction,SortOrder.UNSORTED.code());
    }

    /**
     * Retrieve the next specified attribute according to order.
     * Used for sorting purposes only.
     *
     * @param       name
     *              the attribute's name
     * @param       position
     *              start position of the values to be retrieved
     * @param       order
     *              defines whether this attribute is relevant for the order;
     *              it is applicable to find requests only
     *
     * @see SortOrder
     */
    public AttributeSpecifier(
        String name,
        int position,
        short order
    ) {
        this(name,position,1,SortOrder.ASCENDING.code(),order);
    }

    /**
     * The attribute's name
     */
    final private String name;

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
     * Start position of the values to be retrieved
     */ 
    final private int position;

    private static final String[] TO_STRING_FIELDS = {
        "name",
        "position",
        "size",
        "direction",
        "order"
    };

    /**
     * Get the start position of the values to be retrieved
     *
     * @return      the start position of the values to be retrieved.
     */
    public int position() {
        return this.position;
    }

    /**
     * The maximum size of the values to be retrieved
     */
    final private int size;

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
            return null;
        } else {
            OrderSpecifier[] orderSpecifiers = new OrderSpecifier[attributeSpecifiers.length];
            int i = 0;
            for(AttributeSpecifier attributeSpecifier: attributeSpecifiers) {
                if(attributeSpecifier.position() == 0) {
                    orderSpecifiers[i++] = new OrderSpecifier(
                        attributeSpecifier.name(),
                        SortOrder.valueOf(attributeSpecifier.order())
                    );
                } else {
                    throw new IllegalArgumentException(
                        "Ordering is no longer supported for multivalued attributes"
                    );
                }
            }
            return Arrays.asList(orderSpecifiers);
        }
        
    }
                     
                           
    /**
     * Return the maximum size of the values to be retrieved
     */
    public int size() {
        return this.size;
    }   

    /**
     * The direction of value retrieval
     *
     * @see SortOrder
     */ 
    final private short direction;

    /**
     * Get the direction of value retrieval
     *
     * @return      the direction of value retrieval
     *
     * @see SortOrder
     */ 
    public short direction(){
        return this.direction;
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
		    name + '[' + position + ']',
		    TO_STRING_FIELDS, 
		    new Object[]{
		        name,
		        Integer.valueOf(position), 
		        Integer.valueOf(size), 
		        SortOrder.valueOf(direction), 
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
        this.position == that.position &&
        this.size == that.size &&
        this.direction == that.direction &&
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
