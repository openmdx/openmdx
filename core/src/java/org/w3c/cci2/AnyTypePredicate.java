/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Any Type Predicate
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2010, OMEX AG, Switzerland
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
 * Any Type Predicate
 */
public interface AnyTypePredicate {

    /**
     * &lsaquo;attribute value&rsaquo; = &lsaquo;operand&rsaquo;
     * <p>
     * Matches if the attribute's value is equal to the operand.
     * 
     * @param operand the operand the attribute value is compared to
     */
    void equalTo(
        Object operand
    );

    /**
     * &lsaquo;attribute value&rsaquo; &isin; &lsaquo;operands&rsaquo;
     * <p>
     * Matches if the attribute's value is equal to one the operands.
     * 
     * @param operands the operands the attribute value is compared to
     */
    void elementOf(
        Object... operands
    );

    /**
     * &lsaquo;attribute value&rsaquo; &isin; &lsaquo;operands&rsaquo;
     * <p>
     * Matches if the attribute's value is equal to one the operands.
     * 
     * @param operands the operand the attribute value is compared to
     */
    void elementOf(
        Collection<?> operands
    );

    /**
     * &lsaquo;attribute value&rsaquo; &ne; &lsaquo;operand&rsaquo;
     * <p>
     * Matches if the attribute's value is not equal to the operand.
     * 
     * @param operand the operand the attribute value is compared to
     */
    void notEqualTo(
        Object operand
    );

    /**
     * &lsaquo;attribute value&rsaquo; &notin; &lsaquo;operands&rsaquo;
     * <p>
     * Matches if the attribute's value is not equal to any the operands.
     * 
     * @param operands the operand the attribute value is compared to
     */
    void notAnElementOf(
        Object... operands
    );

    /**
     * &lsaquo;attribute value&rsaquo; &notin; &lsaquo;operands&rsaquo;
     * <p>
     * Matches if the attribute's value is not equal to any the operands.
     * 
     * @param operands the operand the attribute value is compared to
     */
    void notAnElementOf(
        Collection<?> operands
    );

    /**
     * This method is deprecated in order to avoid its erroneous use in lieu 
     * of equalTo().
     * 
     * @deprecated to avoid erroneous use 
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     * @see AnyTypePredicate#equalTo(Object);
     */
    @Deprecated
    boolean equals(Object obj);

}
