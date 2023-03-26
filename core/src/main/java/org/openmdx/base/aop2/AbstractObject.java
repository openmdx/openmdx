/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Abstract Object
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
package org.openmdx.base.aop2;

import java.util.UUID;

import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;

import org.openmdx.base.jmi1.BasePackage;
import org.openmdx.base.jmi1.Void;
import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.kernel.jdo.ReducedJDOHelper;

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

    /**
     * Marshal a next layer object
     * 
     * @param next a next layer object
     * 
     * @return the corresponding same layer object
     */
    @SuppressWarnings("unchecked")
	protected final <T extends RefObject> T toSame(
    	RefObject next 
    ){
    	return next == null ? null : (T) sameManager().getObjectById(ReducedJDOHelper.getTransactionalObjectId(next));
    }

    /**
     * Unmarshal a same layer object
     * 
     * @param same a same layer object
     * 
     * @return the corresponding next layer object
     */
    @SuppressWarnings("unchecked")
	protected final <T extends RefObject> T toNext(
    	RefObject same 
    ){
    	return same == null ? null : (T) nextManager().getObjectById(ReducedJDOHelper.getTransactionalObjectId(same));
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
        return ReducedJDOHelper.getPersistenceManager(this.same);
    }

    /**
     * Retrieve the same layer's persistence manager
     * 
     * @return the same layer's persistence manager
     */
    protected final PersistenceManager nextManager(){
        return ReducedJDOHelper.getPersistenceManager(this.next);
    }

    
    //------------------------------------------------------------------------
    // Context Objects
    //------------------------------------------------------------------------
    
    /**
     * Retrieve the context belonging to this aspect, creating a new one if necessary.
     * 
     * @return the context
     */
    @SuppressWarnings("unchecked")
    protected C thisContext(
    ){
        SharedObjects.Aspects contexts = SharedObjects.aspectObjects(sameManager());
        UUID id = (UUID) ReducedJDOHelper.getTransactionalObjectId(sameObject());
        Class<?> aspect = getClass();
        Object context = contexts.get(id, aspect);
        if(context ==  null) {
            contexts.put(
                id,
                aspect,
                context = newContext()
            );
        }
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
        SharedObjects.aspectObjects(sameManager()).remove(
            (UUID) ReducedJDOHelper.getTransactionalObjectId(sameObject()),
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
    // Callback Termination
    //------------------------------------------------------------------------
    
    /**
     * A sub-class implementing {@code ClearCallback} should invoke 
     * {@code super.jdoPreClear()}.
     */
    protected void jdoPreClear(
    ){
    	// terminate callback
    }

    /**
     * A sub-class implementing {@code StoreCallback} should invoke 
     * {@code super.jdoPreStore()}.
     */
    protected void jdoPreStore(
    ){
    	// terminate callback
    }

    /**
     * A sub-class implementing {@code LoadCallback} should invoke 
     * {@code super.jdoPostLoad()}.
     */
    protected void jdoPostLoad(
    ) {
    	// terminate callback
    }

    /**
     * A sub-class implementing {@code DeleteCallback} should invoke 
     * {@code super.jdoPreDelete()}.
     */
    protected void jdoPreDelete(
    ) {
    	// terminate callback
    }

    /**
     * A sub-class implementing {@code ConstructCallback} should invoke 
     * {@code super.openmdxjdoPostConstruct()}.
     */
    protected void openmdxjdoPostConstruct(
    ) {
    	// terminate callback
    }
    
}
