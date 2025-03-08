/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Object Record Comparator
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
package org.openmdx.base.dataprovider.layer.persistence.none;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import #if CLASSIC_CHRONO_TYPES javax.xml.datatype #else java.time #endif.Duration;

import org.openmdx.base.naming.Path;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.rest.cci.FeatureOrderRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.w3c.cci2.ImmutableDatatype;
import org.w3c.spi.DatatypeFactories;
import org.w3c.spi.ImmutableDatatypeFactory;
import org.w3c.spi2.Datatypes;

/**
 * Object Record Comparator
 */
class ObjectRecordComparator implements Comparator<ObjectRecord> {

    /**
     * Constructor 
     *
     * @param order
     */
    private ObjectRecordComparator(
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
    static ObjectRecordComparator getInstance(
        List<FeatureOrderRecord> order
    ){
        return order == null || order.isEmpty() ? null : new ObjectRecordComparator(
        	order.toArray(new FeatureOrderRecord[order.size()])
        );
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
        } else if(Datatypes.DATE_CLASS.isInstance(left)) {
            if(left instanceof ImmutableDatatype<?> != right instanceof ImmutableDatatype<?>){
                ImmutableDatatypeFactory datatypeFactory = DatatypeFactories.immutableDatatypeFactory();
                return datatypeFactory.toDate((#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) left).compare(datatypeFactory.toDate((#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) right));
            } else {
                return ((#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif)left).compare((#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif) right);
            }
        } else {
            return ((Comparable)left).compareTo(right);
        }
    }
    
    @Override
    public int compare(
        ObjectRecord ox, 
        ObjectRecord oy
    ) {
        if(this.order == null){
            if(ox.getResourceIdentifier() != null) {
                if(oy.getResourceIdentifier() != null) {
                    Path ix = ox.getResourceIdentifier() ; 
                    Path iy = oy.getResourceIdentifier(); 
                    return ix.compareTo(iy);
                } else {
                    return +1;
                }
            } else {
                if(oy.getResourceIdentifier() != null) {
                    return -1;
                } else {
                    UUID ix = ox.getTransientObjectId();
                    UUID iy = oy.getTransientObjectId();
                    return ix.compareTo(iy);
                }
            }
        } else {
            for(FeatureOrderRecord s : this.order){
                if(s.getSortOrder() != SortOrder.UNSORTED) {
					Object vx = getValue(ox, s);
                    Object vy = getValue(oy, s);
                    int c = ObjectRecordComparator.compareValues(vx,vy);
                    if (c != 0) {
                        return s.getSortOrder() == SortOrder.ASCENDING ? c : -c;
                    }
				}
            }
            return 0;
        }
    }

    /**
     * Retrieve a feature's value
     * 
     * @param object
     * @param featureOrder
     * @return the corresponding value
     */
    private Object getValue(
        ObjectRecord object, FeatureOrderRecord featureOrder
    ) {
        return object.getValue().get(featureOrder.getFeature());
    }

    @Override
    public boolean equals(Object that) {
        return 
            that instanceof ObjectRecordComparator &&
            Arrays.equals(this.order, ((ObjectRecordComparator) that).order);
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
