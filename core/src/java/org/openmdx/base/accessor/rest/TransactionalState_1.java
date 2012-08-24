/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Transactional State Implementation 1 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2011, OMEX AG, Switzerland
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

import java.util.AbstractQueue;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.rest.DataObject_1.Operation;

/**
 * Transactional State Implementation 1
 */
public final class TransactionalState_1 {

    /**
     * @serial
     */
    private Set<String> dirtyFeatures = null;

    /**
     * Dirty features have been flushed
     */
    private boolean dirtyFeaturesFlushed = false;

    /**
     * A non-query operation has been invoked
     */
    private boolean touched = false;
    
    /**
     * @serial
     */
    private boolean prepared = false;

    /**
     * @serial
     */
    private boolean lifeCycleEventPending = false;

    /**
     * @serial
     */
    private boolean flushed = false;
    
    /**
     *
     */
    private Map<String,Object> values = null;

    /**
     * Transactional Aspects
     */
    private Map<String,DataObject_1_0> transactionalAspects = null;

    /**
     * Aspect Specific Contexts
     */
    private Map<Class<?>,Object> contexts = null;
    
    /**
     * Operation Queue
     */
    private Queue<Operation> operationQueue = null;
    
    /**
     * An efficient way to return an empty operation queue.
     */
    private static final Queue<Operation> NO_OPERATIONS = new EmptyQueue<Operation>();

    /**
     * Marks the object as touched
     */
    final void touch(){
        this.touched = true;
    }
    
    /**
     * Retrieve touched.
     *
     * @return Returns the touched.
     */
    final boolean isTouched() {
        return this.touched;
    }

    /**
     * Tells whether the data object is out of sync with its "remote" counterpart
     * 
     * @return <code>true</code> if the data object is out of sync with its 
     * "remote" counterpart
     */
    final boolean isOutOfSync(
    ){
        return this.lifeCycleEventPending || (
            this.dirtyFeatures != null && !this.dirtyFeatures.isEmpty()
        ) || (
        	this.operationQueue != null && !this.operationQueue.isEmpty() 	
        );
    }

    /**
     * Tells whether there is a life cycle event pending or a feature dirty
     * 
     * @return <code>false</code> unless a life cycle event is pending 
     * or a feature is dirty
     */
    final boolean isDirty(
    ){
        return this.dirtyFeaturesFlushed || this.touched || isOutOfSync();
    }
    
    /**
     * Retrieve lifeCycleEventPending.
     *
     * @return Returns the lifeCycleEventPending.
     */
    final boolean isLifeCycleEventPending() {
        return this.lifeCycleEventPending;
    }
    
    /**
     * Set Life-Cycle-Event pending.
     * @param lifeCycleEventPending TODO
     */
    final void setLifeCycleEventPending(
        boolean lifeCycleEventPending
    ) {
        this.lifeCycleEventPending = lifeCycleEventPending;
    }

    final boolean isFlushed(){
        return this.flushed;
    }

    final void setFlushed(
        boolean flushed
    ){
        this.flushed = flushed;
    }
    
    final void setDirtyFeaturesFlushed(
    ){
        if(this.dirtyFeatures == null) {
            this.dirtyFeaturesFlushed = false;
        } else {
            this.dirtyFeaturesFlushed = !this.dirtyFeatures.isEmpty();
            this.dirtyFeatures.clear();
        }
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
     * 
     * @param readOnly 
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
     * Convert the transient values to transactional ones
     * 
     * @param values
     */
    final void setValues(
        Map<String,Object> values
    ){
        this.values = values;
    }
    
    /**
     * The dirty features
     * 
     * @param readOnly 
     * 
     * @return the set of dirty features
     */
    public final Set<String> dirtyFeatures(
        boolean readOnly
    ){
        if(this.dirtyFeatures != null) {
        	return this.dirtyFeatures ;
        } else if (readOnly) {
        	return Collections.<String>emptySet();
        } else {
        	return this.dirtyFeatures = new HashSet<String>();
        }
    }

    /**
     * Retrieve the operations to be executed during commit
     * 
     * @param readOnly 
     * 
     * @return the operation queue, never<code>null</code>
     */
    final Queue<Operation> operations(
        boolean readOnly
    ){
        if(this.operationQueue == null) {
            if(readOnly) {
                return NO_OPERATIONS;
            } else {
                this.operationQueue = new LinkedList<Operation>();
            } 
        }
        return this.operationQueue;
    }

    /**
     * Retrieve the aspects to to be made persistent together with this object
     * 
     * @param readOnly 
     * 
     * @return the aspect map, never <code>null</code>
     */
    final Map<String,DataObject_1_0> transactionalAspects(
        boolean readOnly
    ){
        if(this.transactionalAspects == null) {
            if(readOnly) {
                return Collections.emptyMap();
            }
            this.transactionalAspects = new LinkedHashMap<String,DataObject_1_0>();
        }
        return this.transactionalAspects;
    }

    /**
     * Retrieve an aspect specific context
     * 
     * @param key 
     * @return an aspect specific context
     */
    final Object getContext(
        Class<?> key
    ){
        return this.contexts == null ? null : this.contexts.get(key);
    }
    
    /**
     * Set or clear an aspect specific context
     * 
     * @param key 
     * @param context
     */
    final void setContext(
        Class<?> key,
        Object  context
    ){
        if(this.contexts == null) {
            this.contexts = new IdentityHashMap<Class<?>,Object>();
        }
        this.contexts.put(key, context);
    }

    /**
     * Set or clear an aspect specific context
     * 
     * @param key 
     * @param context
     */
    final void removeContext(
        Class<?> key
    ){
        if(this.contexts != null) {
            this.contexts.remove(key);
        } 
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
        this.dirtyFeaturesFlushed = false;
        this.touched = false;
        if(this.contexts != null) {
            this.contexts.clear();
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
