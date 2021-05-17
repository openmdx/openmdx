/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Jmi1ClassPredicate 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2020, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.spi;

import java.util.function.Predicate;

import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.rest.cci.ObjectRecord;

/**
 * Jmi1ClassPredicate
 *
 * @since openMDX 2.17
 */
class Jmi1ClassPredicate implements Predicate<ObjectRecord> {

    /**
     * Constructor 
     *
     * @param modelClassName the selection
     */
    protected Jmi1ClassPredicate(String modelClassName) {
        this.modelClassName = modelClassName;
    }

    /**
     * The selection
     */
    private final String modelClassName;
    
    /* (non-Javadoc)
     * @see java.util.function.Predicate#test(java.lang.Object)
     */
    @Override
    public boolean test(ObjectRecord t) {
        return test(t.getValue().getRecordName());
    }

    private boolean test(String candidateClassName) {
        return getModelClassName().equals(candidateClassName);
    }
    
    static Predicate<ObjectRecord> newInstance(
        boolean subclasses, 
        String modelClassName
    ) {
        return subclasses ? new Jmi1ClassOrSubClassPredicate(
            modelClassName,
            Model_1Factory.getModel()
        ) : new Jmi1ClassPredicate(
            modelClassName
        );
    }
    

    protected String getModelClassName() {
        return this.modelClassName;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 31*getClass().hashCode() + getModelClassName().hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        return
            that != null &&
            this.getClass() == that.getClass() &&
            this.getModelClassName().equals(((Jmi1ClassPredicate)that).getModelClassName()); 
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getModelClassName();
    }
    
}