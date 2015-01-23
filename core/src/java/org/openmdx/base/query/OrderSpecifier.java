/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Order Specifier
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2012, OMEX AG, Switzerland
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

import org.openmdx.base.rest.spi.FeatureOrderRecord;

/**
 * Order Specifier
 */
public class OrderSpecifier extends FeatureOrderRecord {

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
		super(feature, order);
	}
	
    /**
     * Constructor 
     */
    public OrderSpecifier(
    ){
        this(null, SortOrder.DESCENDING);        
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
     * Constructor for Clone
     * 
     * @param that the template
     */
    private OrderSpecifier(
    	OrderSpecifier that
    ){
    	super(that);
    }

    /**
     * Implements <code>Serializable</code>
     */
	private static final long serialVersionUID = -8465157113272809016L;

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public OrderSpecifier clone(
    ){
        return new OrderSpecifier(this);
    }

    /**
     * Set sortOrder.
     * 
     * @param sortOrder The sortOrder to set.
     */
    public void setSortOrder(
        SortOrder sortOrder
    ) {
        super.setSortOrder(sortOrder);
    }

    public void setFeature(
        String feature
    ) {
        super.setFeature(feature);
    }

    public short getOrder() {
    	final SortOrder sortOrder = getSortOrder();
        return (sortOrder == null ? SortOrder.UNSORTED : sortOrder).code();
    }

    public void setOrder(
        short order
    ) {
    	super.setSortOrder(SortOrder.valueOf(order));
    }
    
    //-------------------------------------------------------------------------
    // Implements Record
    //-------------------------------------------------------------------------
    
    @Override
    public String getRecordShortDescription() {
    	return null;
    }

}
