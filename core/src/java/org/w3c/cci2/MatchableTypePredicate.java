/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: MatchableTypePredicate.java,v 1.1 2008/01/08 16:16:33 hburger Exp $
 * Description: Matchable Type Predicate
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/08 16:16:33 $
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
package org.w3c.cci2;

import java.util.Collection;

/**
 * MatchableTypePredicate
 * <p>
 * The matching rules depend on the element type
 */

public interface MatchableTypePredicate<V>



    extends AnyTypePredicate
{

    /**
     * Matches if the attribute's value matches the operand.
     * 
     * @param operand the operand the attribute's value is compared to 
     */
    void like(

        V operand



    );

    /**
     * Matches if the attribute's value matches one of the operands.
     * 
     * @param operand the operand the attribute's value is compared to 
     */
    void like(

        V... operand



    );

    /**
     * Matches if the attribute's value matches one of the operands.
     * 
     * @param operand the operand the attribute's value is compared to 
     */
    void like(

        Collection<V> operand



    );

    /**
     * Matches if the attribute's value does not match the operand.
     * 
     * @param operand the operand the attribute's value is compared to 
     */
    void unlike(

        V operand



    );

    /**
     * Matches if the attribute's value does not match any of the operands.
     * 
     * @param operand the operand the attribute's value is compared to 
     */
    void unlike(

        V... operand



    );

    /**
     * Matches if the attribute's value does not match any of the operands.
     * 
     * @param operand the operand the attribute's value is compared to 
     */
    void unlike(

        Collection<V> operand



    );

    /**
     * Matches if the attribute's value starts with the operand.
     * 
     * @param operand the operand the attribute's value is compared to 
     */
    void startsWith(

        V operand



    );

    /**
     * Matches if the attribute's value starts with one of the operands.
     * 
     * @param operand the operand the attribute's value is compared to 
     */
    void startsWith(

        V... operand



    );

    /**
     * Matches if the attribute's value starts with one of the operands.
     * 
     * @param operand the operand the attribute's value is compared to 
     */
    void startsWith(

        Collection<V> operand



    );

    /**
     * Matches if the attribute's value ends with the operand.
     * 
     * @param operand the operand the attribute's value is compared to 
     */
    void endsWith(

        V operand



    );

    /**
     * Matches if the attribute's value ends with any of the operands.
     * 
     * @param operand the operand the attribute's value is compared to 
     */
    void endsWith(

        V... operand



    );

    /**
     * Matches if the attribute's value ends with any of the operands.
     * 
     * @param operand the operand the attribute's value is compared to 
     */
    void endsWith(

        Collection<V> operand



    );

    /**
     * Matches if the attribute's value starts not with the operand.
     * 
     * @param operand the operand the attribute's value is compared to 
     */
    void startsNotWith(

        V operand



    );

    /**
     * Matches if the attribute's value starts not with one of the operands.
     * 
     * @param operand the operand the attribute's value is compared to 
     */
    void startsNotWith(

        V... operand



    );

    /**
     * Matches if the attribute's value starts not with one of the operands.
     * 
     * @param operand the operand the attribute's value is compared to 
     */
    void startsNotWith(

        Collection<V> operand



    );

    /**
     * Matches if the attribute's value ends not with the operand.
     * 
     * @param operand the operand the attribute's value is compared to 
     */
    void endsNotWith(

        V operand



    );

    /**
     * Matches if the attribute's value ends not with any of the operands.
     * 
     * @param operand the operand the attribute's value is compared to 
     */
    void endsNotWith(

        V... operand



    );

    /**
     * Matches if the attribute's value ends not with any of the operands.
     * 
     * @param operand the operand the attribute's value is compared to 
     */
    void endsNotWith(

        Collection<V> operand



    );

}
