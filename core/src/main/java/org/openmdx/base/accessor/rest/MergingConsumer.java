/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Merging Consumer 
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
import java.util.Iterator;

import java.util.function.Consumer; 

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.rest.AbstractContainer_1.Included;

class MergingConsumer implements Consumer<DataObject_1_0> {

    MergingConsumer(
        Consumer<DataObject_1_0> delegate,
        Included included
    ) {
        this.delegate = delegate;
        this.included = included.iterator();
        this.comparator = included.getComparator();
    }

    private final Consumer<DataObject_1_0> delegate;
    private final Iterator<DataObject_1_0> included;
    private final Comparator<DataObject_1_0> comparator;
    private DataObject_1_0 nextDirty;

    @Override
    public void accept(DataObject_1_0 nextClean) {
        while(useDirty(nextClean)) {
            this.delegate.accept(nextDirty);
            this.nextDirty = null;
        }
        this.delegate.accept(nextClean);
    }
    
    private boolean useDirty(DataObject_1_0 nextClean){
        if(this.nextDirty == null) {
            if(this.included.hasNext()) {
                this.nextDirty = this.included.next();
            } else {
                return false;
            }
        }
        return this.comparator.compare(this.nextDirty, nextClean) <= 0;
    }
    
    void processRemaining(){
        if(this.nextDirty != null) {
            delegate.accept(nextDirty);
        }
        while(this.included.hasNext()) {
            delegate.accept(this.included.next());
        }
    }
    
}
