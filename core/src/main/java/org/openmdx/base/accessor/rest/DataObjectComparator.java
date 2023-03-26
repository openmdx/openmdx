/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: DataObjectComparator 
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

package org.openmdx.base.accessor.rest;

import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.rest.cci.FeatureOrderRecord;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.w3c.cci2.ImmutableDatatype;
import org.w3c.spi.DatatypeFactories;
import org.w3c.spi.ImmutableDatatypeFactory;

/**
 * Object Comparator
 */
final class DataObjectComparator implements Comparator<DataObject_1_0> {

    /**
     * Constructor 
     *
     * @param order
     */
    private DataObjectComparator(
        FeatureOrderRecord[] order
    ){
        this.order = order;
    }

    /**
     * 
     */
    private final FeatureOrderRecord[] order;

    /**
     * Retrieve an ObjectComparator instance
     * 
     * @param order
     * 
     * @return an ObjectComparator instance; or {@code null} if
     * the order is {@code null} or has length {@code 0}
     */
    static DataObjectComparator getInstance(
        FeatureOrderRecord[] order
    ){
        return order == null || order.length == 0 ? null : new DataObjectComparator(order);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static int compareValues(
        Object left, 
        Object right
    ) {
        if(left == null) {
            return right == null ? 0 : -1;
        } else if (right == null) {
            return +1;
        } else if(left instanceof XMLGregorianCalendar) {
            if(left instanceof ImmutableDatatype<?> != right instanceof ImmutableDatatype<?>){
                ImmutableDatatypeFactory datatypeFactory = DatatypeFactories.immutableDatatypeFactory();
                return datatypeFactory.toDate((XMLGregorianCalendar) left).compare(datatypeFactory.toDate((XMLGregorianCalendar) right));
            } else {
                return ((XMLGregorianCalendar)left).compare((XMLGregorianCalendar) right);
            }
        } else {
            return ((Comparable)left).compareTo(right);
        }
    }
    
    @Override
    public int compare(
        DataObject_1_0 ox, 
        DataObject_1_0 oy
    ) {
        if(this.order == null){
            if(ox.jdoIsPersistent()) {
                if(oy.jdoIsPersistent()) {
                    Path ix = (Path) ReducedJDOHelper.getObjectId(ox); 
                    Path iy = (Path) ReducedJDOHelper.getObjectId(oy); 
                    return ix.compareTo(iy);
                } else {
                    return +1;
                }
            } else {
                if(oy.jdoIsPersistent()) {
                    return -1;
                } else {
                    UUID ix = (UUID) ReducedJDOHelper.getTransactionalObjectId(ox);
                    UUID iy = (UUID) ReducedJDOHelper.getTransactionalObjectId(oy);
                    return ix.compareTo(iy);
                }
            }
        } else {
            for(FeatureOrderRecord s : this.order){
                if(s.getSortOrder() != SortOrder.UNSORTED) try {
                    Object vx = ox.objGetValue(s.getFeature());
                    Object vy = oy.objGetValue(s.getFeature());
                    int c = DataObjectComparator.compareValues(vx,vy);
                    if (c != 0) {
                        return s.getSortOrder() == SortOrder.ASCENDING ? c : -c;
                    }
                } catch (ServiceException excpetion) {
                    // exclude field from comparison
                }
            }
            return 0;
        }
    }

    @Override
    public boolean equals(Object that) {
        return 
            that instanceof DataObjectComparator &&
            Arrays.equals(this.order, ((DataObjectComparator) that).order);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.order);
    }

    public FeatureOrderRecord[] getDelegate(
    ){
        return this.order;
    }
    
}