/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SimpleTypePredicate_1.java,v 1.9 2009/06/09 12:45:17 hburger Exp $
 * Description: Any Type Predicate implementation
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 12:45:17 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.openmdx.base.accessor.jmi.cci.RefFilter_1_0;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.query.FilterOperators;
import org.w3c.cci2.AnyTypePredicate;

/**
 * Any Type Predicate implementation
 */
class SimpleTypePredicate_1
    extends AbstractPredicate_1
    implements AnyTypePredicate
{

    /**
     * Constructor 
     * 
     * @param delegate 
     * @param featureDef 
     * @param quantor
     */
    SimpleTypePredicate_1 (
        RefFilter_1_0 delegate,
        ModelElement_1_0 featureDef,
        short quantor
    ){
        super(delegate, featureDef);
        this.quantor = quantor;
    }

    /**
     * 
     */
    protected final short quantor;

    /**
     * Adding value to the delegate filter
     * 
     * @param operator
     * @param operand
     */
    protected void refAddValue(
        short operator,
        Collection<?> operand
    ){
        refAddValue(
            this.quantor,
            operator,
            operand
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#equalTo(V)
     */
    public void equalTo(
        Object operand
    ) {
        elementOf(
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#elementOf(V...)
     */
    public void elementOf(
        Object... operand
    ) {
        elementOf(
            Arrays.asList(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#elementOf(Collection<V>)
     */
    public void elementOf(
        Collection<?> operand
    ) {
        refAddValue(
            FilterOperators.IS_IN,
            operand
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#notEqual(V)
     */
    public void notEqualTo(
        Object operand
    ) {
        notAnElementOf(
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#notAnElementOf(V...)
     */
    public void notAnElementOf(
        Object... operand
    ) {
        notAnElementOf(
            Arrays.asList(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#notAnElementOf(Collection<V>)
     */
    public void notAnElementOf(
        Collection<?> operand
    ) {
        refAddValue(
            FilterOperators.IS_NOT_IN,
            operand
        );
    }

}
