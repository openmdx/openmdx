/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ObjectComparator_1.java,v 1.9 2008/06/28 00:21:44 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/28 00:21:44 $
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
package org.openmdx.base.accessor.generic.spi;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.collection.SparseArray;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.Orders;

/**
 * @author hburger
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
@SuppressWarnings("unchecked")
public final class ObjectComparator_1 implements Serializable, Comparator {

    /**
     * 
     */
    private static final long serialVersionUID = 3546926878817857586L;

    /**
     * For de-serialization
     */
    protected ObjectComparator_1(
    ){
        super();
    }
    
    /**
     * 
     */
    public ObjectComparator_1(
        AttributeSpecifier[] order
    ){
        this.order = order;
    }

    private Object getValue(
        Object_1_0 object,
        AttributeSpecifier specifier
    ){
        try {
            Object value = object.objGetValue(specifier.name());
            int position = specifier.position();
            //... Sort on multivalued attributes is deprecated!
            if(value instanceof List){
                List collection = (List)value;
                return collection.size() > position ? collection.get(position) : null;
            } else if (value instanceof SparseArray) {
                SparseArray collection = (SparseArray)value;
                return collection.get(position);
            } else if (value instanceof SortedMap) {
                SortedMap collection = (SortedMap)value;
                return collection.get(new Integer(position));
            } else if (value instanceof Collection) {
                Collection collection = (Collection)value;
                if(position >= collection.size()) return null;
                Iterator i = collection.iterator();
                while (
                    position-- > 0
                ) i.next();
                return i.next();
            } else {
                return value;
            }
        } catch (ServiceException e) {
            return null;
        }
    }
    
    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object x, Object y) {
        Object_1_0 ox = (Object_1_0)x;
        Object_1_0 oy = (Object_1_0)y;
        if(this.order == null){
            Object ix = ox.objGetResourceIdentifier(); 
            Object iy = oy.objGetResourceIdentifier(); 
            return ix == null ? 
                (iy == null ? 0 : 1) : 
                (iy == null ? -1 : ((Comparable)ix).compareTo(iy));
        } else {
            for(
                int i = 0;
                i < this.order.length;
                i++
            ){
                AttributeSpecifier s = this.order[i];
                if(s.order() == Orders.ANY) continue;
                Object vx = getValue(ox,s);
                Object vy = getValue(oy,s);
                if(vx != null || vy != null){
                    if(vx == null) return -1;
                    if(vy == null) return +1;
                    int c = ((Comparable)vx).compareTo(vy);
                    if (c != 0) return s.order() == Directions.ASCENDING ? c : -c;
                }
            }
            return 0;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object that) {
        return that == null || that.getClass() != getClass() ?
            false :
            Arrays.equals(this.order, ((ObjectComparator_1)that).order);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return Arrays.asList(this.order).hashCode();
    }

    /**
     * 
     */
    private AttributeSpecifier[] order;
    
    public AttributeSpecifier[] getDelegate(
    ){
        return this.order;
    }

}
