/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: String Type Predicate
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.w3c.cci2;

import java.util.Collection;

/**
 * String Type Predicate
 *
 * The like and unlike predicates are formed according to the following rulse:
 * <li>The wildcard sequence &ldquo;<code>.*</code>&rdquo; stands for any 
 *     number of characters
 * <li>The wildcard character &ldquo;<code>.</code>&rdquo; stands for a 
 *     single character
 * </ul>
 */
public interface StringTypePredicate
    extends ComparableTypePredicate<String>, MatchableTypePredicate<String>
{

    /**
     * Enables SoundEx processing of the pattern.
     * <p>
     * When this flag is specified then the input string that specifies the 
     * pattern is converted into a SoundEx code.
     * <p>
     * No other flag must be specified in combination with SOUNDS 
     * <p>
     * There is no embedded flag character for enabling SOUNDS processing.
     */
    int SOUNDS = 010000000000;

    /**
     * To define the SoundEx to be used.
     * <p>
     * <code>"org.openmdx.query.SoundEx"</code> is used as the first argument
     * of JDO <code>Query</code>'s <code>addExtension()</code> method, while
     * the second one is the name of the SoundEx to be used.
     * 
     * @see javax.jdo.Query#addExtension(String, Object)
     */
    String SOUNDEX = "org.openmdx.query.SoundEx";

    /**
     * Matches if the attribute's value matches the operand.
     * 
     * @param flags the flags to be applied, e.g.<ul>
     * <li>java.util.regex.Pattern.CASE_INSENSITIVE
     * <li>org.w3c.cci2.StringTypePredicate.SOUNDS
     * </ul>
     * @param operand the operand the attribute's value is compared to 
     * according to the following rules:<ul>
     * <li>The wildcard sequence &ldquo;<code>.*</code>&rdquo; stands for any 
     *     number of characters
     * <li>The wildcard character &ldquo;<code>.</code>&rdquo; stands for a 
     *     single character
     * </ul>
     * @see java.util.regex.Pattern#CASE_INSENSITIVE
     * @see StringTypePredicate#SOUNDS
     */
    void like(
        int flags,
        String operand
    );

    /**
     * Matches if the attribute's value matches one of the operands.
     * 
     * @param flags the flags to be applied, e.g.<ul>
     * <li>java.util.regex.Pattern.CASE_INSENSITIVE
     * <li>org.w3c.cci2.StringTypePredicate.SOUNDS
     * </ul>
     * @param operand the operand the attribute's value is compared to 
     * according to the following rules:<ul>
     * <li>The wildcard sequence &ldquo;<code>.*</code>&rdquo; stands for any 
     *     number of characters
     * <li>The wildcard character &ldquo;<code>.</code>&rdquo; stands for a 
     *     single character
     * </ul>
     * @see java.util.regex.Pattern#CASE_INSENSITIVE
     * @see StringTypePredicate#SOUNDS
     */
    void like(
        int flags,
        String... operand
    );

    /**
     * Matches if the attribute's value matches one of the operands.
     * 
     * @param flags the flags to be applied, e.g.<ul>
     * <li>java.util.regex.Pattern.CASE_INSENSITIVE
     * <li>org.w3c.cci2.StringTypePredicate.SOUNDS
     * </ul>
     * @param operand the operand the attribute's value is compared to 
     * according to the following rules:<ul>
     * <li>The wildcard sequence &ldquo;<code>.*</code>&rdquo; stands for any 
     *     number of characters
     * <li>The wildcard character &ldquo;<code>.</code>&rdquo; stands for a 
     *     single character
     * </ul>
     * @see java.util.regex.Pattern#CASE_INSENSITIVE
     * @see StringTypePredicate#SOUNDS
     */
    void like(
        int flags,
        Collection<String> operand
    );

    /**
     * Matches if the attribute's value does not match the operand.
     * 
     * @param flags the flags to be applied, e.g.<ul>
     * <li>java.util.regex.Pattern.CASE_INSENSITIVE
     * <li>org.w3c.cci2.StringTypePredicate.SOUNDS
     * </ul>
     * @param operand the operand the attribute's value is compared to 
     * according to the following rules:<ul>
     * <li>The wildcard sequence &ldquo;<code>.*</code>&rdquo; stands for any 
     *     number of characters
     * <li>The wildcard character &ldquo;<code>.</code>&rdquo; stands for a 
     *     single character
     * </ul>
     * @see java.util.regex.Pattern#CASE_INSENSITIVE
     * @see StringTypePredicate#SOUNDS
     */
    void unlike(
        int flags,
        String operand
    );

    /**
     * Matches if the attribute's value does not match any of the operands.
     * 
     * @param flags the flags to be applied, e.g.<ul>
     * <li>java.util.regex.Pattern.CASE_INSENSITIVE
     * <li>org.w3c.cci2.StringTypePredicate.SOUNDS
     * </ul>
     * @param operand the operand the attribute's value is compared to 
     * according to the following rules:<ul>
     * <li>The wildcard sequence &ldquo;<code>.*</code>&rdquo; stands for any 
     *     number of characters
     * <li>The wildcard character &ldquo;<code>.</code>&rdquo; stands for a 
     *     single character
     * </ul>
     * @see java.util.regex.Pattern#CASE_INSENSITIVE
     * @see StringTypePredicate#SOUNDS
     */
    void unlike(
        int flags,
        String... operand
    );

    /**
     * Matches if the attribute's value does not match any of the operands.
     * 
     * @param flags the flags to be applied, e.g.<ul>
     * <li>java.util.regex.Pattern.CASE_INSENSITIVE
     * <li>org.w3c.cci2.StringTypePredicate.SOUNDS
     * </ul>
     * @param operand the operand the attribute's value is compared to 
     * according to the following rules:<ul>
     * <li>The wildcard sequence &ldquo;<code>.*</code>&rdquo; stands for any 
     *     number of characters
     * <li>The wildcard character &ldquo;<code>.</code>&rdquo; stands for a 
     *     single character
     * </ul>
     * @see java.util.regex.Pattern#CASE_INSENSITIVE
     * @see StringTypePredicate#SOUNDS
     */
    void unlike(
        int flags,
        Collection<String> operand
    );

}
