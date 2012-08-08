/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ResourceIdentifierTypePredicate_1.java,v 1.8 2009/06/09 12:45:17 hburger Exp $
 * Description: Resource Identifier Type Predicate implementation
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 12:45:17 $
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
import java.util.Collection;
import java.util.Collections;

import org.openmdx.base.accessor.jmi.cci.RefFilter_1_0;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.query.FilterOperators;
import org.w3c.cci2.MatchableTypePredicate;

/**
 * Resource Identifier Type Predicate implementation
 */
class ResourceIdentifierTypePredicate_1<V extends Comparable<?>>
    extends ComparableTypePredicate_1<V>
    implements MatchableTypePredicate<V>
{

    /**
     * Constructor 
     * 
     * @param delegate 
     * @param featureDef
     * @param quantor
     */
    ResourceIdentifierTypePredicate_1 (
        RefFilter_1_0 delegate,
        ModelElement_1_0 featureDef,
        short quantor
    ){
        super(delegate, featureDef, quantor);
    }

    /* (non-Javadoc)
     * @see org.w3c.query.ResourceIdentifierTypePredicate#like(V)
     */
    public void like(
        V operand
    ) {
        like(
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.query.ResourceIdentifierTypePredicate#like(V...)
     */
    public void like(
        V... operand
    ) {
        like(
            Arrays.asList(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.query.ResourceIdentifierTypePredicate#like(Collection<V>)
     */
    public void like(
        Collection<V> operand
    ) {
        refAddValue(
            super.quantor,
            FilterOperators.IS_LIKE,
            operand
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.query.ResourceIdentifierTypePredicate#unlike(V)
     */
    public void unlike(
        V operand
    ) {
        unlike(
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.query.ResourceIdentifierTypePredicate#unlike(V...)
     */
    public void unlike(
        V... operand
    ) {
        unlike(
            Arrays.asList(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.query.ResourceIdentifierTypePredicate#unlike(Collection<V>)
     */
    public void unlike(
        Collection<V> operand
    ) {
        refAddValue(
            super.quantor,
            FilterOperators.IS_UNLIKE,
            operand
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#endsWith(java.lang.Object)
     */
    public void endsWith(
        V operand
    ) {
        endsWith(
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#endsWith(V[])
     */
    public void endsWith(
        V... operand
    ) {
        endsWith(
            Arrays.asList(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#endsWith(java.util.Collection)
     */
    public void endsWith(
        Collection<V> operand
    ) {
        refAddValue(
            super.quantor,
            FilterOperators.IS_LIKE,
            StringTypePredicate_1.jdoWildcard(operand, true)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#startsWith(java.lang.Object)
     */
    public void startsWith(
        V operand
    ) {
        startsWith(
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#startsWith(V[])
     */
    public void startsWith(
        V... operand
    ) {
        startsWith(
            Arrays.asList(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#startsWith(java.util.Collection)
     */
    public void startsWith(
        Collection<V> operand
    ) {
        refAddValue(
            super.quantor,
            FilterOperators.IS_LIKE,
            StringTypePredicate_1.jdoWildcard(operand, false)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#endsNotWith(java.lang.Object)
     */
    public void endsNotWith(
        V operand
    ) {
        endsNotWith(
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#endsNotWith(V[])
     */
    public void endsNotWith(
        V... operand
    ) {
        endsNotWith(
            Arrays.asList(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#endsNotWith(java.util.Collection)
     */
    public void endsNotWith(
        Collection<V> operand
    ) {
        refAddValue(
            super.quantor,
            FilterOperators.IS_UNLIKE,
            StringTypePredicate_1.jdoWildcard(operand, true)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#startsNotWith(java.lang.Object)
     */
    public void startsNotWith(
        V operand
    ) {
        startsNotWith(
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#startsNotWith(V[])
     */
    public void startsNotWith(
        V... operand
    ) {
        startsNotWith(
            Arrays.asList(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#startsNotWith(java.util.Collection)
     */
    public void startsNotWith(
        Collection<V> operand
    ) {
        refAddValue(
            super.quantor,
            FilterOperators.IS_UNLIKE,
            StringTypePredicate_1.jdoWildcard(operand, false)
        );
    }

}
