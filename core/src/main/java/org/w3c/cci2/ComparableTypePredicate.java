/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Comparable Type Predicate
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
package org.w3c.cci2;

/**
 * Comparable Type Predicate
 */
public interface ComparableTypePredicate<V extends Comparable<?>>
    extends AnyTypePredicate
{

    /**
     * Matches if the attribute's value is equal or greater than the lower 
     * bound and less than or equal the upper bound.
     * 
     * @param lowerBound
     * @param upperBound
     */
    void between(
        V lowerBound,
        V upperBound
    );

    /**
     * Matches if the attribute's value is less than the lower or greater than
     * the upper bound.
     * 
     * @param lowerBound
     * @param upperBound
     */
    void outside(
        V lowerBound,
        V upperBound
    );

    /**
     * Matches if the attribute's value is less than the oeprand.
     * 
     * @param operand
     */
    void lessThan(
        V operand
    );

    /**
     * Matches if the attribute's value is less than or equal the oeprand.
     * 
     * @param operand
     */
    void lessThanOrEqualTo(
        V operand
    );

    /**
     * Matches if the attribute's value is greater than or equal to the operand.
     * 
     * @param operand
     */
    void greaterThanOrEqualTo(
        V operand
    );

    /**
     * Matches if the attribute's value is greater than the operand.
     * 
     * @param operand
     */
    void greaterThan(
        V operand
    );

}
