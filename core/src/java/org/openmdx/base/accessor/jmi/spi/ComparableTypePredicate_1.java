/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ComparableTypePredicate_1.java,v 1.5 2009/01/13 02:10:33 wfro Exp $
 * Description: Comparable Type Predicate implementation
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 02:10:33 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2008, OMEX AG, Switzerland
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
import java.util.Collections;

import org.openmdx.base.accessor.jmi.cci.RefFilter_1_1;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.query.FilterOperators;
import org.w3c.cci2.ComparableTypePredicate;

/**
 * Comparable Type Predicate implementation
 */

class ComparableTypePredicate_1<V extends Comparable<?>>
    extends SimpleTypePredicate_1
    implements ComparableTypePredicate<V>
{

    /**
     * Constructor 
     * 
     * @param delegate 
     * @param featureDef 
     * @param quantor
     */
    ComparableTypePredicate_1 (
        RefFilter_1_1 delegate,
        ModelElement_1_0 featureDef,
        short quantor
    ){
        super(delegate, featureDef, quantor);
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.ComparableTypePredicate#between(V, V)
     */
    public void between(
        V lowerBound,
        V upperBound
    ) {
        refAddValue(
            this.quantor,
            FilterOperators.IS_BETWEEN,
            Arrays.asList(
                new Comparable[]{lowerBound, upperBound}
            )
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.ComparableTypePredicate#outside(V, V)
     */
    public void outside(
        V lowerBound,
        V upperBound
    ) {
        refAddValue(
            this.quantor,
            FilterOperators.IS_OUTSIDE,
            Arrays.asList(
                new Comparable[]{lowerBound, upperBound}
            )
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.ComparableTypePredicate#lessThan(V)
     */
    public void lessThan(
        V operand
    ) {
        refAddValue(
            this.quantor,
            FilterOperators.IS_LESS,
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.ComparableTypePredicate#lessThanOrEqual(V)
     */
    public void lessThanOrEqualTo(
        V operand
    ) {
        refAddValue(
            this.quantor,
            FilterOperators.IS_LESS_OR_EQUAL,
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.ComparableTypePredicate#greaterThanOrEqual(V)
     */
    public void greaterThanOrEqualTo(
        V operand
    ) {
        refAddValue(
            this.quantor,
            FilterOperators.IS_GREATER_OR_EQUAL,
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.ComparableTypePredicate#greaterThan(V)
     */
    public void greaterThan(
        V operand
    ) {
        refAddValue(
            this.quantor,
            FilterOperators.IS_GREATER,
            Collections.singleton(operand)
        );
    }

}
