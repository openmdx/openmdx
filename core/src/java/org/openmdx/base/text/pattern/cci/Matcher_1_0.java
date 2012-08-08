/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Matcher_1_0.java,v 1.7 2008/01/08 16:16:31 hburger Exp $
 * Description: openMDX Pattern interface
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/08 16:16:31 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.base.text.pattern.cci;


import java.util.regex.MatchResult;




/**
 * Pattern interface
 * <p>
 * Instances of Matcher_1_0 are not safe for use by multiple concurrent 
 * threads.
 */
public interface Matcher_1_0
    extends MatchResult
{

    /**
     * Attempts to match the entire region against the pattern.
     * <p>
     * If the match succeeds then more information can be obtained via 
     * the start, end, and group methods.
     *   
     * @return  <code>true</code> if, and only if, the entire region 
     *          sequence matches this matcher's pattern
     */
    boolean matches(
    );

    /**
     * Resets this matcher with a new input sequence.
     * <p>
     * Resetting a matcher discards all of its explicit state
     * information and sets its append position to zero. The matcher's 
     * region is set to the default region, which is its entire 
     * character sequence.
     * 
     * @param   input
     *          The new input character sequence
     * 
     * @return  This matcher 
     */
    Matcher_1_0 reset(
        String input
    );

    /**
     * Attempts to find the next subsequence of the input sequence that 
     * matches the pattern.
     * <p>
     * This method starts at the beginning of the input sequence or, if a 
     * previous invocation of the method was successful and the matcher has 
     * not since been reset, at the first character not matched by the 
     * previous match.
     * <p>
     * If the match succeeds then more information can be obtained via the 
     * start, end, and group methods.
     *  
     * @return true if, and only if, a subsequence of the input sequence 
     * matches this matcher's pattern
     */
    public boolean find(
    );

    /**
     * Resets this matcher and then attempts to find the next subsequence of 
     * the input sequence that matches the pattern, starting at the specified 
     * index. 
     * <p>
     * If the match succeeds then more information can be obtained via the 
     * start, end, and group methods, and subsequent invocations of the find() 
     * method will start at the first character not matched by this match. 
     * 
     * @param start
     * 
     * @return true if, and only if, a subsequence of the input sequence 
     * starting at the given index matches this matcher's pattern
     * 
     * @exception IndexOutOfBoundsException
     * If start is less than zero or if start is greater than the length of 
     * the input sequence. 
     */
    public boolean find(
        int start
    );

    /**
     * Replaces every subsequence of the input sequence that matches the
     * pattern with the given replacement string.
     * <p>
     * This method first resets this matcher. It then scans the input sequence 
     * looking for matches of the pattern. Characters that are not part of any 
     * match are appended directly to the result string; each match is 
     * replaced in the result by the replacement string. The replacement 
     * string may contain references to captured subsequences as in the 
     * appendReplacement method. 
     * <p>
     * Invoking this method changes this matcher's state. If the matcher is to be 
     * used in further matching operations then it should first be reset.
     *  
     * @param replacement The replacement string 
     * 
     * @return The string constructed by replacing each matching subsequence by 
     * the replacement string, substituting captured subsequences as needed
     */
    public String replaceAll(
        String replacement
    );

    /**
     * Replaces the first subsequence of the input sequence that matches the 
     * pattern with the given replacement string.  
     * <p>
     * This method first resets this matcher. It then scans the input sequence 
     * looking for a match of the pattern. Characters that are not part of the 
     * match are appended directly to the result string; the match is replaced 
     * in the result by the replacement string. The replacement string may 
     * contain references to captured subsequences as in the appendReplacement 
     * method.
     * Invoking this method changes this matcher's state. If the matcher is to 
     * be used in further matching operations then it should first be reset. 
     * 
     * @param replacement The replacement string 
     * 
     * @return The string constructed by replacing the first matching 
     * subsequence by the replacement string, substituting captured 
     * subsequences as needed
     */
    public String replaceFirst(
        String replacement
    );

}
