/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractObject.java,v 1.3 2008/04/25 15:16:56 hburger Exp $
 * Description: Abstract Object
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/25 15:16:56 $
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
package org.openmdx.base.aop2.standard;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;

import org.openmdx.base.jmi1.BasePackage;
import org.openmdx.base.jmi1.Void;

/**
 * Abstract Object
 */
public abstract class AbstractObject {

    /**
     * Constructor 
     *
     * @param same
     * @param next
     */
    public AbstractObject(
        RefObject same,
        Object next
    ) {
        this.same = same;
        this.next = next;
    }

    /**
     * Delegates to the same plug-in's proxy instance
     */
    private final RefObject same;

    /**
     * Delegates to the next plug-in's proxy instance
     */
    private final Object next;

    /**
     * Retrieve same.
     *
     * @return Returns the same.
     */
    @SuppressWarnings("unchecked")
    protected final <T extends RefObject> T sameObject() {
        return (T) this.same;
    }

    /**
     * Retrieve next.
     *
     * @return Returns the next.
     */
    @SuppressWarnings("unchecked")
    protected final <T> T nextObject() {
        return (T) this.next;
    }

    @SuppressWarnings("unchecked")
    protected final <T extends RefPackage> T samePackage(
    ){
        return (T) this.same.refImmediatePackage();
    }

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
    
    /**
     * Void factory
     * 
     * @return a new Void instance
     */
    protected final Void newVoid(){
        return ((BasePackage)this.same.refOutermostPackage().refPackage("org:openmdx:base")).createVoid();
    }

}
