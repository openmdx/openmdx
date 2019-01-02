/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: $
 * Description: JoiningList 
 * Revision:    $Revision: $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2018, OMEX AG, Switzerland
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

package org.openmdx.base.accessor.rest;

import java.util.AbstractSequentialList;
import java.util.Comparator;

import javax.jdo.FetchPlan;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.rest.AbstractContainer_1.BatchingList;
import org.openmdx.base.accessor.rest.AbstractContainer_1.Excluded;
import org.openmdx.base.accessor.rest.AbstractContainer_1.Included;

/**
 * Joining  List
 * <p>
 * <em>Note the difference between a <code>JoiningList</code> and a <code>java.util.List</code>:<br>
 * A <code>ChainingList</code>'s <code>size()</code> method uses <code>Integer.MAX_VALUE</code> 
 * to tell, that its size has not yet been calculated.<br>
 * That's why such a <code>Collection</code> has to be wrapped into a <code>CountingCollection</code>
 * before being returned by the API.</em>
 */
abstract class JoiningList 
    extends AbstractSequentialList<DataObject_1_0> 
    implements ProcessingList
{

    /**
     * Constructor 
     *
     * @param included
     * @param stored
     * @param excluded
     */
    protected JoiningList(
        Included included,
        BatchingList stored,
        Excluded excluded
    ){
        this.comparator = included.getComparator();
        this.included = included;
        this.stored = stored;
        this.excluded = excluded;
    }

    protected final Comparator<DataObject_1_0> comparator;
    protected final Included included;
    protected final Excluded excluded;
    protected final BatchingList stored;

    protected Excluded getExcluded(
    ) {
        return this.excluded;
    }
    
    protected BatchingList getStored(
    ) {
       return this.stored;
    }
     
    public int size(
        FetchPlan fetchPlan 
    ){
        int storedSize = this.stored.size(fetchPlan);
        if(storedSize != Integer.MAX_VALUE) {
            if(storedSize == 0 && this.included.isEmpty()) {
                return 0;
            } else if(this.excluded.isPlain()) {
                return storedSize + this.included.size() - this.excluded.size();
            } else if(this.included.isEmpty() && this.excluded.isEmpty()) {
                return storedSize;
            }
        }
        int total = this.included.size();
        for(DataObject_1_0 candidate : this.stored) {
            if(!this.excluded.handles(candidate)) {
                total++;
            }
        }
        return total;
    }
    
    @Override
    public int size(
    ) {
        return size(null);
    }

    @Override
    public boolean isEmpty() {
        if(this.included.isEmpty()) {
            if(this.stored.isEmpty()) {
                return true;
            } else if (this.excluded.isPlain()) {
                if(this.excluded.isEmpty()) {
                    return false;
                } else {
                    Integer total = this.stored.getTotal();
                    if(total != null && total.intValue() == this.excluded.size()){
                        return true;
                    } else {
                        return !this.stored.listIterator(this.excluded.size()).hasNext();
                    }
                }
            } else {
                Integer total = this.stored.getTotal();
                if(total != null && total.intValue() > this.excluded.size()) {
                    return false;
                } else {
                    for(DataObject_1_0 candidate : this.stored) {
                        if(!this.excluded.handles(candidate)) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        } else {
            return false;
        }
    }
    
    /**
     * Break the List contract to avoid round-trips
     */
    @Override
    public boolean equals(
        Object that
    ) {
        return this == that;
    }

    /**
     * Break the List contract to avoid round-trips
     */
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    /**
     * Break the List contract to avoid round-trips
     */
    @Override
    public String toString(
    ){
        return this.getClass().getSimpleName() + "@" + System.identityHashCode(this);
    }

}