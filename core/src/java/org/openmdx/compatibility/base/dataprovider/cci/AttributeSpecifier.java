/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AttributeSpecifier.java,v 1.15 2008/06/28 00:21:28 hburger Exp $
 * Description: openMDX Compatibility Mode: Attribute specifier
 * Revision:    $Revision: 1.15 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/28 00:21:28 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.compatibility.base.dataprovider.cci;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.resource.ResourceException;

import org.openmdx.base.exception.BadParameterException;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.resource.Records;
import org.openmdx.kernel.exception.BasicException;


/**
 * Specifies an attribute to be retrieved
 */
@SuppressWarnings("unchecked")
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
     * @exception   BadParameterException
     *              if any of the arguments ahs an illegal value
     */
    AttributeSpecifier(
        String name,
        int position,
        int size,
        short direction,
        short order
    ){
        Set invalid = new HashSet();

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
            direction != Directions.ASCENDING &&
            direction != Directions.DESCENDING
        ) invalid.add("direction");
        this.direction = direction;

        if(
            order != Orders.ANY &&
            order != Directions.ASCENDING &&
            order != Directions.DESCENDING
        ) invalid.add("order");
        this.order = order;

        if (! invalid.isEmpty()) throw new BadParameterException(
            new BasicException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("name",name),
                    new BasicException.Parameter("position",position),
                    new BasicException.Parameter("size",size),
                    new BasicException.Parameter("direction",direction),
                    new BasicException.Parameter("order",order)
                },
                "Illegal value for arguments " + invalid
            )
        );
    }

    /**
     * Retrieve all attribute values
     */
    public AttributeSpecifier(
        String name
    ) {
        this(name,0,Integer.MAX_VALUE,Directions.ASCENDING,Orders.ANY);
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
     * @see org.openmdx.compatibility.base.dataprovider.cci.Directions
     */
    public AttributeSpecifier(
        String name,
        int position,
        int size,
        short direction
    ) {
        this(name,position,size,direction,Orders.ANY);
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
     * @see org.openmdx.compatibility.base.dataprovider.cci.Orders
     */
    public AttributeSpecifier(
        String name,
        int position,
        short order
    ) {
        this(name,position,1,Directions.ASCENDING,order);
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
     * Return the maximum size of the values to be retrieved
     */
    public int size() {
        return this.size;
    }   

    /**
     * The direction of value retrieval
     *
     * @see org.openmdx.compatibility.base.dataprovider.cci.Directions
     */ 
    final private short direction;

    /**
     * Get the direction of value retrieval
     *
     * @return      the direction of value retrieval
     *
     * @see org.openmdx.compatibility.base.dataprovider.cci.Directions
     */ 
    public short direction(){
        return this.direction;
    }
    
    /**
     * The sort order
     *
     * @see org.openmdx.compatibility.base.dataprovider.cci.Orders
     */
    final private short order;
    
    /**
     * Get the sort order
     *
     * @return      the sort order
     *
     * @see org.openmdx.compatibility.base.dataprovider.cci.Orders
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
    public String toString() {
        try {
            return Records.getRecordFactory().asMappedRecord(
                getClass().getName(), 
                name + '[' + position + ']',
                new String[]{
                    "name",
                    "position",
                    "size",
                    "direction",
                    "order"
                }, 
                new Object[]{
                    name,
                    new Integer(position), 
                    new Integer(size), 
                    Directions.toString(direction), 
                    Orders.toString(order)
                }
            ).toString();
        } catch (ResourceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
    public int hashCode() {
        return name.hashCode();
    }

}
