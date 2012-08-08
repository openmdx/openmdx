/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: OrderSpecifier.java,v 1.15 2011/11/26 01:34:59 hburger Exp $
 * Description: Order Specifier
 * Revision:    $Revision: 1.15 $
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

import org.openmdx.base.resource.Records;

/**
 * Order Specifier
 */
public class OrderSpecifier
    implements Serializable 
{

    /**
     * Constructor 
     */
    public OrderSpecifier(
    ){
        this(null, (short)-1);        
    }

    /**
     * Constructor 
     *
     * @param feature
     * @param order
     */
    public OrderSpecifier(
        String feature,
        SortOrder order
    ) {
        this.feature = feature;
        this.order = order;
    }

    /**
     * Constructor 
     *
     * @param feature
     * @param order
     * 
     * @deprecated
     */
    @Deprecated
    public OrderSpecifier(
        String feature,
        short order
    ) {
        this(
            feature,
            SortOrder.valueOf(order)
        );
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 6129959553711798810L;

    /**
     * 
     */
    private String feature;
    
    /**
     * 
     */
    private SortOrder order;
    
    /**
     * 
     */
    private static final String[] TO_STRING_FIELDS = {
        "feature",
        "order"
    };
    
    /**
     * Retrieve sortOrder.
     *
     * @return Returns the sortOrder.
     */
    public SortOrder getSortOrder() {
        return this.order;
    }
    
    /**
     * Set sortOrder.
     * 
     * @param sortOrder The sortOrder to set.
     */
    public void setSortOrder(
        SortOrder sortOrder
    ) {
        this.order = sortOrder;
    }

    public String getFeature() {
        return this.feature;
    }

    public void setFeature(
        String feature
    ) {
        this.feature = feature;
    }

    public short getOrder() {
        return this.order == null ? 0 : this.order.code();
    }

    public void setOrder(
        short order
    ) {
        this.order = SortOrder.valueOf(order);
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof OrderSpecifier) {
            OrderSpecifier that = (OrderSpecifier) obj;
            return 
                this.order == that.order &&
                areEqual(this.feature, that.feature);
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
        if(this.feature != null) {
            code += feature.hashCode();
        }
        if(this.order != null) {
            code ^= this.order.code();
        }
        return code;
    }

    /**
     * Compare to values
     * 
     * @param left
     * @param right
     * 
     * @return <code>true</code> if the values are either equal or both <code>null</code>
     */
    private static boolean areEqual(
        String left,
        String right
    ){
        return left == null ? right == null : left.equals(right);
    }
       
    @Override
    public String toString(
    ) {
        StringBuilder description = new StringBuilder(
		).append(
		    this.order == null ? ' ' : order.symbol()
		).append(
		    this.feature
		);
		return Records.getRecordFactory().asMappedRecord(
		    getClass().getName(), 
		    description.toString(),
		    OrderSpecifier.TO_STRING_FIELDS,
		    new Object[]{
		        feature,
		        this.order
		    }
		).toString();
    }

}
