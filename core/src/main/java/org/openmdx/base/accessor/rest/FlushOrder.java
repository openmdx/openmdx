/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Flush Order 
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

import java.util.Comparator;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.naming.Path;


/**
 * Flush Order
 */
public class FlushOrder implements Comparator<DataObject_1_0> {

    /**
     * Constructor 
     */
    protected FlushOrder(){
        super();
    }

    /**
     * A  a {@code FlushOrder} instance
     */
    private static final Comparator<DataObject_1_0> instance = new FlushOrder();
    
    /**
     * Retrieve a {@code FlushOrder} instance
     * 
     * @return a {@code FlushOrder} instance
     */
    public static Comparator<DataObject_1_0> getInstance(){
        return FlushOrder.instance;
    }
    
    @Override
    public int compare(
        DataObject_1_0 left, 
        DataObject_1_0 right
    ) {
        Path leftId = left.jdoGetObjectId();
        Path rightId = right.jdoGetObjectId();
        //
        // Deleted
        //
        boolean leftIsDeleted = left.jdoIsDeleted();
        boolean rightIsDeleted = right.jdoIsDeleted();
        if(leftIsDeleted | rightIsDeleted) {
            if(leftIsDeleted & rightIsDeleted){
                return -leftId.compareTo(rightId);
            } else {
                return leftIsDeleted ? +1 : -1; 
            }
        }
        //
        // New
        //
        boolean leftIsNew = left.jdoIsNew();
        boolean rightIsNew = right.jdoIsNew();
        if(leftIsNew | rightIsNew) {
            if(leftIsNew & rightIsNew) {
                return +leftId.compareTo(rightId);
            } else {
                return leftIsNew ? +1 : -1;
            }
        }
        //
        // Dirty
        //
        boolean leftIsDirty = left.jdoIsDirty();
        boolean rightIsDirty = right.jdoIsDirty();
        if(leftIsDirty | rightIsDirty) {
            if(leftIsDirty & rightIsDirty) {
                return +leftId.compareTo(rightId);
            } else {
                return leftIsDirty ? +1 : -1;
            }
        }
        //
        // Persistent
        //
        boolean leftIsPersistent = left.jdoIsPersistent();
        boolean rightIsPersistent = right.jdoIsPersistent();
        if(leftIsPersistent | rightIsPersistent) {
            if(leftIsPersistent & rightIsPersistent){
                return +leftId.compareTo(rightId);
            } else {
                return leftIsPersistent ? +1 : -1;
            }
        }
        //
        // Transient
        //
        return left.jdoGetTransactionalObjectId().compareTo(right.jdoGetTransactionalObjectId());
    }
    
}
