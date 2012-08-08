/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractComparator.java,v 1.4 2007/11/07 18:17:46 hburger Exp $
 * Description: Abstract Filter Class
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/11/07 18:17:46 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.spi;

import java.util.Comparator;
import java.util.Iterator;

import javax.resource.ResourceException;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.resource.Records;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.Orders;


/**
 * @author hburger
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class AbstractComparator implements Comparator {

    /**
     * 
     */
    protected AbstractComparator(
        AttributeSpecifier[] order
    ) {
        this.order = order;
    }

    /**
     * 
     * @param attribute
     * @return an iterator for the values 
     * 
     * @exception   ClassCastException
     *              If the filter is not applicable to the candidate
     */
    protected abstract Iterator getValues(
        Object candidate,
        String attribute
    );
    
    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object left, Object right) {
        specifications: for(
            int i = 0;
            i < this.order.length;
            i++
        ){
            AttributeSpecifier specifier = this.order[i];
            boolean ln, rn;
            Object lv, rv;
            int greaterThanResult = 0; 
            switch (specifier.order()){
                case Orders.ANY : 
                    continue specifications;
                case Orders.ASCENDING : 
                    greaterThanResult = +1;
                    break;
                case Orders.DESCENDING : 
                    greaterThanResult = -1;
                    break;
            }
            for(
                Iterator 
                    li = getValues(left, specifier.name()),
                    ri = getValues(right, specifier.name());
                (ln = li.hasNext()) | (rn = ri.hasNext());
            ){
                if(!ln) return -greaterThanResult;
                if(!rn) return +greaterThanResult;
                if((lv = li.next()) != null | (rv = ri.next()) != null) {
                    if(lv == null) return -greaterThanResult;
                    if(rv == null) return +greaterThanResult;
                    int c = ((Comparable)lv).compareTo(rv);
                    if(c < 0){
                        return -greaterThanResult;
                    } else if (c > 0) {
                        return +greaterThanResult; 
                    }
                }
            }
        }
        return 0;
    }

    /**
     * 
     */
    protected AttributeSpecifier[] order;

    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        try {
            return Records.getRecordFactory().asIndexedRecord(
                getClass().getName(), 
                null, 
                this.order
            ).toString();
        } catch (ResourceException e) {
            throw new RuntimeServiceException(e);
        }
    }

}