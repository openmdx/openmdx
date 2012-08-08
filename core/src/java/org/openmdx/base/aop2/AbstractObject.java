/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractObject.java,v 1.7 2009/05/23 15:08:56 wfro Exp $
 * Description: Abstract Object
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/23 15:08:56 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2009, OMEX AG, Switzerland
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
package org.openmdx.base.aop2;

import java.util.Collections;

import javax.jdo.JDOFatalInternalException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.ClearCallback;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.LoadCallback;
import javax.jdo.listener.StoreCallback;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;

import org.openmdx.base.jmi1.BasePackage;
import org.openmdx.base.jmi1.Void;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.spi.AspectObjectAcessor;
import org.openmdx.jdo.listener.ConstructCallback;

/**
 * Abstract Object
 */
public abstract class AbstractObject<S extends RefObject, N, C> {

    /**
     * Constructor 
     *
     * @param same
     * @param next
     */
    protected AbstractObject(
        S same,
        N next
    ) {
        this.same = same;
        this.next = next;
    }

    /**
     * Delegates to the same plug-in's proxy instance
     */
    private final S same;

    /**
     * Delegates to the next plug-in's proxy instance
     */
    private final N next;

    
    //------------------------------------------------------------------------
    // Objects
    //------------------------------------------------------------------------
    
    /**
     * Retrieve same.
     *
     * @return Returns the same.
     */
    protected final S sameObject() {
        return this.same;
    }

    /**
     * Retrieve next.
     *
     * @return Returns the next.
     */
    protected final N nextObject() {
        return this.next;
    }

    
    //------------------------------------------------------------------------
    // Packages
    //------------------------------------------------------------------------
    
    @SuppressWarnings("unchecked")
    protected final <T extends RefPackage> T samePackage(
    ){
        return (T) this.same.refImmediatePackage();
    }

    
    //------------------------------------------------------------------------
    // Managers
    //------------------------------------------------------------------------
    
    /**
     * Retrieve the same layer's persistence manager
     * 
     * @return the same layer's persistence manager
     */
    protected final PersistenceManager sameManager(){
        return JDOHelper.getPersistenceManager(this.same);
    }

    /**
     * Retrieve the same layer's persistence manager
     * 
     * @return the same layer's persistence manager
     */
    protected final PersistenceManager nextManager(){
        return JDOHelper.getPersistenceManager(this.next);
    }

    
    //------------------------------------------------------------------------
    // Context Objects
    //------------------------------------------------------------------------
    
    /**
     * Retrieve the current object id
     * 
     * @return the current object id
     */
    private final Object contextId(){
        return PersistenceHelper.getCurrentObjectId(sameObject());
    }
    
    /**
     * Acquire the <code>AspectObjectAcessor</code> instance
     * 
     * @return the <code>AspectObjectAcessor</code> instance
     */
    private AspectObjectAcessor aspectObjectAccessor(){
        AspectObjectAcessor contexts = (AspectObjectAcessor) sameManager().getUserObject(AspectObjectAcessor.class);
        if(contexts == null) throw new JDOFatalInternalException(
            AspectObjectAcessor.class.getSimpleName() + " acquisition failure"
        );
        return contexts;
    }
    
    /**
     * Retrieve the context belonging to this aspect, creating a new one if necessary.
     * 
     * @return the context
     */
    @SuppressWarnings("unchecked")
    protected C thisContext(
    ){
        AspectObjectAcessor contexts = aspectObjectAccessor();
        Object id = contextId();
        Class<?> aspect = getClass();
        Object context = contexts.get(id, aspect);
        if(context ==  null) contexts.put(
            id,
            aspect,
            context = newContext()
        );
        return (C) context;
    }
    
    /**
     * Must be overridden by a sub-class in order to use an aspect specific context
     * 
     * @return a new context
     */
    protected C newContext(){
        return null; 
    }
    
    /**
     * Removes the context if it is already set
     */
    protected void evictContext(
    ){
        aspectObjectAccessor().remove(
            contextId(), // id
            getClass() // aspect
        );
    }
    
    
    //------------------------------------------------------------------------
    // Void Factory
    //------------------------------------------------------------------------
    
    /**
     * Void factory
     * 
     * @return a new Void instance
     */
    protected final Void newVoid(){
        return ((BasePackage)this.same.refOutermostPackage().refPackage("org:openmdx:base")).createVoid();
    }

    
    //------------------------------------------------------------------------
    // Callback Delegation
    //------------------------------------------------------------------------
    
    /**
     * Delegate <code>ClearCallback</code> method to the mix-in super-classes 
     * of the same layer and to the next layer.
     */
    protected void jdoPreClear(
    ) {
        //
        // Mix-In Classes Of Same Layer
        //
        jdoPreClear(
            (Class<? super N>[])null // any order
        );
        //
        // Next Layer
        //
        if(this.next instanceof ClearCallback) {
            ((ClearCallback)this.next).jdoPreClear();
        }
    }

    /**
     * Delegate <code>StoreCallback</code> method to the mix-in super-classes 
     * of the same layer and to the next layer.
     */
    protected void jdoPreStore(
    ) {
        //
        // Mix-In Classes Of Same Layer
        //
        jdoPreStore(
            (Class<? super N>[])null // any order
        );
    }

    /**
     * Delegate <code>LoadCallback</code> method to the mix-in super-classes 
     * of the same layer and to the next layer.
     */
    protected void jdoPostLoad(
    ) {
        //
        // Mix-In Classes Of Same Layer
        //
        jdoPostLoad(
            (Class<? super N>[])null // any order
        );
    }

    /**
     * Delegate <code>DeleteCallback</code> method to the mix-in super-classes 
     * of the same layer and to the next layer.
     */
    protected void jdoPreDelete(
    ) {
        //
        // Mix-In Classes Of Same Layer
        //
        jdoPreDelete(
            (Class<? super N>[])null // any order
        );
    }

    /**
     * Delegate <code>ConstructCallback</code> method to the mix-in super-classes 
     * of the same layer and to the next layer.
     */
    protected void openmdxjdoPostConstruct(
    ) {
        //
        // Mix-In Classes Of Same Layer
        //
        openmdxjdoPostConstruct(
            (Class<? super N>[])null // any order
        );
    }

    
    //------------------------------------------------------------------------
    // Callback Delegation To Mix-In Classes Of The Same Layer
    //------------------------------------------------------------------------
    
    /**
     * Delegate <code>ClearCallback</code> method to the mix-in super-classes  
     * of the same layer.
     * 
     * @param selection the selection and order of mix-in super-classes to dispatch to
     */
    protected void jdoPreClear(
        Class<? super N>... selection
    ) {
        for(ClearCallback target : mixedInTargets(ClearCallback.class, selection)) {
            target.jdoPreClear();
        }
    }

    /**
     * Delegate <code>StoreCallback</code> method to the mix-in super-classes  
     * of the same layer.
     * 
     * @param selection the selection and order of mix-in super-classes to dispatch to
     */
    protected void jdoPreStore(
        Class<? super N>... selection
    ) {
        for(StoreCallback c : mixedInTargets(StoreCallback.class, selection)) {
            c.jdoPreStore();
        }
    }

    /**
     * Delegate <code>LoadCallback</code> method to the mix-in super-classes  
     * of the same layer.
     * 
     * @param selection the selection and order of mix-in super-classes to dispatch to
     */
    protected void jdoPostLoad(
        Class<? super N>... selection
    ) {
        for(LoadCallback c : mixedInTargets(LoadCallback.class, selection)) {
            c.jdoPostLoad();
        }
    }

    /**
     * Delegate <code>DeleteCallback</code> method to the mix-in super-classes  
     * of the same layer.
     * 
     * @param selection the selection and order of mix-in super-classes to dispatch to
     */
    protected void jdoPreDelete(
        Class<? super N>... selection
    ) {
        for(DeleteCallback c : mixedInTargets(DeleteCallback.class, selection)) {
            c.jdoPreDelete();
        }
    }

    /**
     * Delegate <code>ConstructCallback</code> method to the mix-in super-classes  
     * of the same layer.
     * 
     * @param selection the selection and order of mix-in super-classes to dispatch to
     */
    protected void openmdxjdoPostConstruct(
        Class<? super N>... selection
    ) {
        for(ConstructCallback c : mixedInTargets(ConstructCallback.class, selection)) {
            c.openmdxjdoPostConstruct();
        }
    }

    /**
     * Provide the targets for mixed-in interfaces 
     * 
     * @param targetClass the target interface
     * @param selection the selection and order of mix-in super-classes to dispatch to
     * 
     * @return the targets for mixed-in interfaces 
     */
    protected final <I> Iterable<I> mixedInTargets(
        Class<I> targetClass,
        Class<? super N>... selection
    ){
        return Collections.emptyList(); // TODO provide targets
    }

}
