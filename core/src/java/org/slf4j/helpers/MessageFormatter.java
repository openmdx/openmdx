/* 
 * Copyright (c) 2004-2007 QOS.ch
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * ______________________________________________________________________
 *
 * Copyright (c) 2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * ====================================================================
 * Project:     openMEX, http://www.openmdx.org/
 * Name:        $Id: MessageFormatter.java,v 1.5 2008/09/09 10:05:04 hburger Exp $
 * Description: Intending Formatter
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/09 10:05:04 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
 *    the documentation and/or other materials provided with the
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
package org.slf4j.helpers;

import java.util.Arrays;

/**
 * Formats messages according to very simple substitution rules. Substitutions
 * can be made 1, 2 or more arguments.
 * <p>
 * For example,
 * <pre>MessageFormatter.format(&quot;Hi {}.&quot;, &quot;there&quot;);</pre>
 * will return the string "Hi there.".
 * <p>
 * The {} pair is called the <em>formatting anchor</em>. It serves to
 * designate the location where arguments need to be substituted within the
 * message pattern.
 * <p>
 * In the rare case where you need to place the '{' or '}' in the message
 * pattern itself but do not want them to be interpreted as a formatting
 * anchors, you can espace the '{' character with '\', that is the backslash
 * character. Only the '{' character should be escaped. There is no need to
 * escape the '}' character. For example, 
 * <pre>MessageFormatter.format(&quot;Set \\{1,2,3} is not equal to {}.&quot;, &quot;1,2&quot;);</pre>
 * will return the string "Set {1,2,3} is not equal to 1,2.". 
 * 
 * <p>
 * The escaping behaviour just described can be overridden by 
 * escaping the escape character '\'. Calling
 * <pre>MessageFormatter.format(&quot;File name is C:\\\\{}.&quot;, &quot;file.zip&quot;);</pre>
 * will return the string "File name is C:\file.zip".
 * 
 * <p>
 * See {@link #format(String, Object)}, {@link #format(String, Object, Object)}
 * and {@link #arrayFormat(String, Object[])} methods for more details.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * ______________________________________________________________________
 * 
 * openMDX changes include:
 * o Java 5 support
 * o Array format support
 * o StringBuilder replacing StringBuffer
 */
public class MessageFormatter {
    static final char DELIM_START = '{';
    static final char DELIM_STOP = '}';
    private static final char ESCAPE_CHAR = '\\';

    /**
     * Performs single argument substitution for the 'messagePattern' passed as
     * parameter.
     * <p>
     * For example,
     * 
     * <pre>
     * MessageFormatter.format(&quot;Hi {}.&quot;, &quot;there&quot;);
     * </pre>
     * 
     * will return the string "Hi there.".
     * <p>
     * 
     * @param messagePattern
     *          The message pattern which will be parsed and formatted
     * @param argument
     *          The argument to be substituted in place of the formatting anchor
     * @return The formatted message
     */
    public static String format(String messagePattern, Object arg) {
        return arrayFormat(messagePattern, new Object[] { arg });
    }

    /**
     * 
     * Performs a two argument substitution for the 'messagePattern' passed as
     * parameter.
     * <p>
     * For example,
     * 
     * <pre>
     * MessageFormatter.format(&quot;Hi {}. My name is {}.&quot;, &quot;Alice&quot;, &quot;Bob&quot;);
     * </pre>
     * 
     * will return the string "Hi Alice. My name is Bob.".
     * 
     * @param messagePattern
     *          The message pattern which will be parsed and formatted
     * @param arg1
     *          The argument to be substituted in place of the first formatting
     *          anchor
     * @param arg2
     *          The argument to be substituted in place of the second formatting
     *          anchor
     * @return The formatted message
     */
    public static String format(String messagePattern, Object arg1, Object arg2) {
        return arrayFormat(messagePattern, new Object[] { arg1, arg2 });
    }

    /**
     * Same principle as the {@link #format(String, Object)} and
     * {@link #format(String, Object, Object)} methods except that any number of
     * arguments can be passed in an array.
     * 
     * @param pattern
     *          The message pattern which will be parsed and formatted
     * @param arguments
     *          An array of arguments to be substituted in place of formatting
     *          anchors
     * @return The formatted message
     */
    public static String arrayFormat(String pattern, Object... arguments) {
        if (pattern == null) {
            return null;
        }
        int i = 0;
        int len = pattern.length();
        int j = pattern.indexOf(DELIM_START);

        StringBuilder target = new StringBuilder(pattern.length() + 50);

        for (int L = 0; L < arguments.length; L++) {

            j = pattern.indexOf(DELIM_START, i);

            if (j == -1 || (j + 1 == len)) {
                // no more variables
                if (i == 0) { // this is a simple string
                    return pattern;
                } else { // add the tail string which contains no variables and return
                    // the result.
                    target.append(pattern.substring(i, pattern.length()));
                    return target.toString();
                }
            } else {
                char delimStop = pattern.charAt(j + 1);

                if (isEscapedDelimeter(pattern, j)) {
                    if(!isDoubleEscaped(pattern, j)) {
                        L--; // DELIM_START was escaped, thus should not be incremented
                        target.append(pattern.substring(i, j - 1));
                        target.append(DELIM_START);
                        i = j + 1;
                    } else {
                        // The escape character preceding the delemiter start is
                        // itself escaped: "abc x:\\{}"
                        // we have to consume one backward slash
                        target.append(pattern.substring(i, j-1));
                        target.append(arguments[L]);
                        i = j + 2;
                    }
                } else if ((delimStop != DELIM_STOP)) {
                    // invalid DELIM_START/DELIM_STOP pair
                    target.append(pattern.substring(i, pattern.length()));
                    return target.toString();
                } else {
                    // normal case
                    target.append(pattern.substring(i, j));
                    target.append(arguments[L]);
                    i = j + 2;
                }
            }
        }
        // append the characters following the second {} pair.
        return target.append(pattern.substring(i)).toString();
    }

    /**
     * Array aware append operation
     * 
     * @param target
     * @param element
     */
    public static void appendTo(
        StringBuilder target,
        Object element
    ){
        if(element != null) {
            Class<?> eClass = element.getClass();
            if(eClass.isArray()) {
                target.append(
                    eClass == byte[].class ? Arrays.toString((byte[])element) :
                    eClass == short[].class ? Arrays.toString((short[])element) :
                    eClass == int[].class ? Arrays.toString((int[])element) :
                    eClass == long[].class ? Arrays.toString((long[])element) :
                    eClass == char[].class ? Arrays.toString((char[])element) :
                    eClass == float[].class ? Arrays.toString((float[])element) :
                    eClass == double[].class ? Arrays.toString((double[])element) :
                    eClass == boolean[].class ? Arrays.toString((boolean[])element) :
                    Arrays.deepToString((Object[]) element)
                );
                return;
            }
        }
        target.append(element);
    }

    static boolean isEscapedDelimeter(String messagePattern,
        int delimeterStartIndex) {

        if (delimeterStartIndex == 0) {
            return false;
        }
        char potentialEscape = messagePattern.charAt(delimeterStartIndex - 1);
        if (potentialEscape == ESCAPE_CHAR) {
            return true;
        } else {
            return false;
        }
    }

    static boolean isDoubleEscaped(String messagePattern, int delimeterStartIndex) {
        if (delimeterStartIndex >= 2
                && messagePattern.charAt(delimeterStartIndex - 2) == ESCAPE_CHAR) {
            return true;
        } else {
            return false;
        }
    }
}
