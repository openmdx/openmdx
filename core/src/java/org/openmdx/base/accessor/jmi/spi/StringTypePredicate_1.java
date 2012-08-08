/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: StringTypePredicate_1.java,v 1.9 2009/06/09 12:45:17 hburger Exp $
 * Description: String Type Predicate implementation
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 12:45:17 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2007, OMEX AG, Switzerland
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.openmdx.base.accessor.jmi.cci.RefFilter_1_0;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.query.FilterOperators;
import org.w3c.cci2.StringTypePredicate;

/**
 * String Type Predicate implementation
 */
class StringTypePredicate_1

    extends ComparableTypePredicate_1<String>



    implements StringTypePredicate
{

    /**
     * Constructor 
     * 
     * @param delegate 
     * @param featureDef 
     * @param quantor
     */
    StringTypePredicate_1 (
        RefFilter_1_0 delegate,
        ModelElement_1_0 featureDef,
        short quantor
    ){
        super(delegate, featureDef, quantor);
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.StringTypePredicate#like(java.lang.String)
     */
    public void like(

        String operand



    ) {
        like(
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.StringTypePredicate#like(java.lang.String...)
     */
    public void like(

        String... operand



    ) {
        like(
            Arrays.asList(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.StringTypePredicate#like(Collection<String>)
     */
    public void like(

        Collection<String> operand



    ) {
        refAddValue(
            super.quantor,
            FilterOperators.IS_LIKE,
            operand
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.StringTypePredicate#unlike(java.lang.String)
     */
    public void unlike(

        String operand



    ) {
        unlike(
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.StringTypePredicate#unlike(java.lang.String...)
     */
    public void unlike(

        String... operand



    ) {
        unlike(
            Arrays.asList(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.StringTypePredicate#unlike(Collection<String>)
     */
    public void unlike(
        Collection<String> operand
    ) {
        refAddValue(
            super.quantor,
            FilterOperators.IS_UNLIKE,
            operand
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.StringTypePredicate#like(int,java.lang.String)
     */
    public void like(
        int flags,
        String operand
    ) {
        like(
                flags,
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.StringTypePredicate#like(java.lang.String...)
     */
    public void like(
        int flags,
        String... operand
    ) {
        like(
            flags,
            Arrays.asList(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.StringTypePredicate#like(Collection<String>)
     */
    public void like(
        int flags,
        Collection<String> operand
    ) {
        if(flags == 0) {
            refAddValue(
                super.quantor,
                FilterOperators.IS_LIKE,
                operand
            );
        } 
        else if(flags == SOUNDS) {
            refAddValue(
                super.quantor,
                FilterOperators.SOUNDS_LIKE,
                operand
            );
        } 
        else throw new IllegalArgumentException(
            "No other flag than SOUNDS is supported in compatibility mode"
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.StringTypePredicate#unlike(java.lang.String)
     */
    public void unlike(
        int flags,
        String operand
     ) {
        unlike(
            flags,
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.ci2.StringTypePredicate#unlike(java.lang.String...)
     */
    public void unlike(
        int flags,
        String... operand
    ) {
        unlike(
            flags,
            Arrays.asList(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.StringTypePredicate#unlike(Collection<String>)
     */
    public void unlike(
        int flags,
        Collection<String> operand
    ) {
        if(flags == 0) {
            refAddValue(
                super.quantor,
                FilterOperators.IS_UNLIKE,
                operand
            );
        } 
        else if(flags == SOUNDS) {
            refAddValue(
                super.quantor,
                FilterOperators.SOUNDS_UNLIKE,
                operand
            );
        } 
        else throw new IllegalArgumentException(
            "No other flag than SOUNDS is supported in compatibility mode"
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#endsWith(java.lang.Object)
     */
    public void endsWith(
        String operand
    ) {
        endsWith(
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#endsWith(V[])
     */
    public void endsWith(
        String... operand
    ) {
        endsWith(
            Arrays.asList(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#endsWith(java.util.Collection)
     */
    public void endsWith(
        Collection<String> operand
    ) {
        refAddValue(
            super.quantor,
            FilterOperators.IS_LIKE,
            jdoWildcard(operand, true)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#startsWith(java.lang.Object)
     */
    public void startsWith(
        String operand
    ) {
        startsWith(
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#startsWith(V[])
     */
    public void startsWith(
        String... operand
    ) {
        startsWith(
            Arrays.asList(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#startsWith(java.util.Collection)
     */
    public void startsWith(
        Collection<String> operand
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
        String operand
    ) {
        endsNotWith(
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#endsNotWith(V[])
     */
    public void endsNotWith(
        String... operand
    ) {
        endsNotWith(
            Arrays.asList(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#endsNotWith(java.util.Collection)
     */
    public void endsNotWith(
        Collection<String> operand
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
        String operand
    ) {
        startsNotWith(
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#startsNotWith(V[])
     */
    public void startsNotWith(
        String... operand
    ) {
        startsNotWith(
            Arrays.asList(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.MatchableTypePredicate#startsNotWith(java.util.Collection)
     */
    public void startsNotWith(
        Collection<String> operand
    ) {
        refAddValue(
            super.quantor,
            FilterOperators.IS_UNLIKE,
            StringTypePredicate_1.jdoWildcard(operand, false)
        );
    }

    /**
     * Add a wildcard prefix or suffix to the given operands
     * 
     * @param prefix tells whether a prefix or suffix has to be added
     * @param source the original operands
     * 
     * @return the modified operands
     */
    @SuppressWarnings("unchecked")
    static Collection jdoWildcard (
        Collection source,
        boolean prefix
    ){
        if (source == null) {
            return Collections.EMPTY_SET;
        } 
        else {
            Collection target = new ArrayList();
            for(
                Iterator i = source.iterator();
                i.hasNext();
            ){
                target.add(
                    prefix ? ".*" + i.next() : i.next().toString() + ".*"
                );
            }
            return target;
        }
    }

}
