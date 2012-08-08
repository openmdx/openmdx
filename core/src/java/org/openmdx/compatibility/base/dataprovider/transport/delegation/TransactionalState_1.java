/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TransactionalState_1.java,v 1.4 2008/09/06 09:43:03 hburger Exp $
 * Description: Transactional State Implementation 1 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/06 09:43:03 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.transport.delegation;

import java.util.AbstractQueue;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.openmdx.compatibility.base.dataprovider.transport.delegation.Object_1.Operation;

/**
 * Transactional State Implementation 1
 */
final class TransactionalState_1 {

    /**
     * @serial
     */
    private Set<String> dirtyFeatures = null;

    /**
     * @serial
     */
    private boolean prepared = false;

    /**
     * @serial
     */
    private boolean lifeCycleEventPending = false;

    /**
     *
     */
    private Map<String,Object> values = null;

    /**
     * Operation Queue
     */
    private Queue<Operation> operationQueue = null;

    
    static final Queue<Operation> EMPTY_QUEUE = new EmptyQueue<Operation>();
    
    /**
     * Retrieve lifeCycleEventPending.
     *
     * @return Returns the lifeCycleEventPending.
     */
    final boolean isLifeCycleEventPending() {
        return this.lifeCycleEventPending;
    }

    /**
     * Set lifeCycleEventPending.
     * 
     * @param lifeCycleEventPending The lifeCycleEventPending to set.
     */
    final void setLifeCycleEventPending(boolean lifeCycleEventPending) {
        this.lifeCycleEventPending = lifeCycleEventPending;
    }

    /**
     * Retrieve prepared.
     *
     * @return Returns the prepared.
     */
    final boolean isPrepared() {
        return this.prepared;
    }

    /**
     * Set prepared.
     * 
     * @param prepared The prepared to set.
     */
    final void setPrepared(boolean prepared) {
        this.prepared = prepared;
    }

    /**
     * The unit of work local value store
     * @param readOnly TODO
     * 
     * @return the value cache
     */
    final Map<String,Object> values(
        boolean readOnly
    ){
        if(this.values == null) {
            if(readOnly) {
                return Collections.emptyMap();
            } else {
                this.values = new HashMap<String,Object>();
            }
        }
        return this.values;
    }

    /**
     * The dirty features
     * @param readOnly TODO
     * 
     * @return the set of dirty features
     */
    final Set<String> dirtyFeatures(
        boolean readOnly
    ){
        if(this.dirtyFeatures == null) {
            if(readOnly) {
                return Collections.emptySet();
            } else {
                this.dirtyFeatures = new HashSet<String>();
            }
        }
        return this.dirtyFeatures;
    }

    /**
     * Retrieve the operations to be executed during commit
     * @param readOnly TODO
     * 
     * @return the operation queue
     */
    final Queue<Operation> operations(
        boolean readOnly
    ){
        if(this.operationQueue == null) {
            if(readOnly) {
                return EMPTY_QUEUE;
            } else {
                this.operationQueue = new LinkedList<Operation>();
            } 
        }
        return this.operationQueue;
    }

    /**
     * Clear
     */
    final void clear(){
        if(this.values != null){
            this.values.clear();
        }
        if(this.dirtyFeatures != null) {
            this.dirtyFeatures.clear();
        }        
    }
    
    //------------------------------------------------------------------------
    // Class EmptyQueue
    //------------------------------------------------------------------------

    /**
     * Empty Queue
     */
    static final class EmptyQueue<E> extends AbstractQueue<E> {

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @SuppressWarnings("unchecked")
        @Override
        public Iterator<E> iterator() {
            return Collections.EMPTY_LIST.iterator();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return 0;
        }

        /* (non-Javadoc)
         * @see java.util.Queue#offer(java.lang.Object)
         */
        public boolean offer(E o) {
            return false;
        }

        /* (non-Javadoc)
         * @see java.util.Queue#peek()
         */
        public E peek() {
            return null;
        }

        /* (non-Javadoc)
         * @see java.util.Queue#poll()
         */
        public E poll() {
            return null;
        }
        
    }
    
    
}