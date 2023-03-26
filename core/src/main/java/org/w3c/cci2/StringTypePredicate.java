/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: StringTypePredicate
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

import java.util.Collection;

/**
 * String Type Predicate
 *
 * The like and unlike predicates are formed according to the following rulse:
 * <li>The wildcard sequence &ldquo;{@code .*}&rdquo; stands for any 
 *     number of characters
 * <li>The wildcard character &ldquo;{@code .}&rdquo; stands for a 
 *     single character
 * </ul>
 * 
 * <pre> 
 * 100000000000000 0000000000000000 SOUNDS
 *   1000000000000 0000000000000000 POSIX_EXPRESSION
 *    100000000000 0000000000000000 ACCENT_INSENSITIVE
 *              10 0000000000000000 JSON_QUERY
 *               1 0000000000000000 X_QUERY
 *                        100000000 UNICODE_CHARACTER_CLASS
 *                         10000000 CANON_EQ
 *                          1000000 UNICODE_CASE
 *                           100000 DOTALL
 *                            10000 LITERAL
 *                             1000 MULTILINE
 *                              100 COMMENTS
 *                               10 CASE_INSENSITIVE
 *                                1 UNIX_LINES
 * </pre> 
 */
public interface StringTypePredicate
    extends ComparableTypePredicate<String>, MatchableTypePredicate<String>
{

    /**
     * Enables (database specific) JSON query processing of the pattern.
     * <p>
     * No other flag must be specified in combination with JSON_QUERY 
     * <p> 
     * JSON query expression matching can also be enabled via the 
     * embedded flag expression&nbsp;<tt>(?j)</tt>.
     */
    int JSON_QUERY = 0400000;
    
    /**
     * Enables (database specific) XQuery query processing of the pattern.
     * <p>
     * No other flag must be specified in combination with X_QUERY 
     * <p> 
     * XQuery query expression matching can also be enabled via the 
     * embedded flag expression&nbsp;<tt>(?X)</tt>.
     */
    int X_QUERY = 0200000;
    
    /*
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
     * Enables POSIX regular expression matching.
     *
     * <p> POSIX regular expression matching can also be enabled via the 
     * embedded flag expression&nbsp;<tt>(?P)</tt>.
     *
     * <p> Specifying this flag may impose a performance penalty.  </p>
     */
    int POSIX_EXPRESSION = 02000000000;
    
    /**
     * Enables accent-insensitive matching.
     *
     * <p> Case-insensitive matching can also be enabled via the embedded flag
     * expression&nbsp;<tt>(?I)</tt>.
     *
     * <p> Specifying this flag may impose a performance penalty.  </p>
     */
    int ACCENT_INSENSITIVE = 01000000000;
    
    /**
     * To define the SoundEx to be used.
     * <p>
     * {@code "org.openmdx.query.SoundEx"} is used as the first argument
     * of JDO {@code Query}'s {@code addExtension()} method, while
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
     * @param operand the attribute's value is compared to the operand  
     * according to the following rules, unless either the JSON_QUERY
     * or X_QUERY flag is specified:<ul>
     * <li>The wildcard sequence &ldquo;{@code .*}&rdquo; stands for any 
     *     number of characters
     * <li>The wildcard character &ldquo;{@code .}&rdquo; stands for a 
     *     single character
     * </ul>
     * @see java.util.regex.Pattern#CASE_INSENSITIVE
     * @see java.util.regex.Pattern#UNICODE_CASE
     * @see #SOUNDS
     * @see #X_QUERY
     * @see #JSON_QUERY
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
     * @param operand the attribute's value is compared to the operand  
     * according to the following rules:<ul>
     * <li>The wildcard sequence &ldquo;{@code .*}&rdquo; stands for any 
     *     number of characters
     * <li>The wildcard character &ldquo;{@code .}&rdquo; stands for a 
     *     single character
     * </ul>
     * @see java.util.regex.Pattern#CASE_INSENSITIVE
     * @see java.util.regex.Pattern#UNICODE_CASE
     * @see #SOUNDS
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
     * @param operand the attribute's value is compared to the operand  
     * according to the following rules:<ul>
     * <li>The wildcard sequence &ldquo;{@code .*}&rdquo; stands for any 
     *     number of characters
     * <li>The wildcard character &ldquo;{@code .}&rdquo; stands for a 
     *     single character
     * </ul>
     * @see java.util.regex.Pattern#CASE_INSENSITIVE
     * @see java.util.regex.Pattern#UNICODE_CASE
     * @see #SOUNDS
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
     * @param operand the attribute's value is compared to the operand  
     * according to the following rules, unless either the JSON_QUERY
     * or X_QUERY flag is specified:<ul>
     * <li>The wildcard sequence &ldquo;{@code .*}&rdquo; stands for any 
     *     number of characters
     * <li>The wildcard character &ldquo;{@code .}&rdquo; stands for a 
     *     single character
     * </ul>
     * @see java.util.regex.Pattern#CASE_INSENSITIVE
     * @see java.util.regex.Pattern#UNICODE_CASE
     * @see #SOUNDS
     * @see #X_QUERY
     * @see #JSON_QUERY
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
     * @param operand the attribute's value is compared to the operand  
     * according to the following rules:<ul>
     * <li>The wildcard sequence &ldquo;{@code .*}&rdquo; stands for any 
     *     number of characters
     * <li>The wildcard character &ldquo;{@code .}&rdquo; stands for a 
     *     single character
     * </ul>
     * @see java.util.regex.Pattern#CASE_INSENSITIVE
     * @see java.util.regex.Pattern#UNICODE_CASE
     * @see #SOUNDS
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
     * @param operand the attribute's value is compared to the operand  
     * according to the following rules:<ul>
     * <li>The wildcard sequence &ldquo;{@code .*}&rdquo; stands for any 
     *     number of characters
     * <li>The wildcard character &ldquo;{@code .}&rdquo; stands for a 
     *     single character
     * </ul>
     * @see java.util.regex.Pattern#CASE_INSENSITIVE
     * @see java.util.regex.Pattern#UNICODE_CASE
     * @see #SOUNDS
     */
    void unlike(
        int flags,
        Collection<String> operand
    );

}
