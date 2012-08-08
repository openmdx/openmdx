/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SQLExpression.java,v 1.13 2008/09/10 08:55:29 hburger Exp $
 * Description: openMDX SQL LIKE Pattern implementation
 * Revision:    $Revision: 1.13 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:29 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.base.text.pattern;

import org.openmdx.base.exception.BadParameterException;
import org.openmdx.base.text.pattern.cci.Matcher_1_0;
import org.openmdx.base.text.pattern.cci.Pattern_1_0;
import org.openmdx.kernel.exception.BasicException;

/**
 * SQL Like pattern implementation
 */
public class SQLExpression {

    /**
     * 
     */
    public static final char ESCAPE = '\\';

    /**
     * 
     */
    public static final char CHARACTER_PLACEHOLDER = '_';

    /**
     * 
     */
    public static final char STRING_PLACEHOLDER = '%';

    /**
     * Constructor
     */
    private SQLExpression(
    ){
        super();
    }

    /**
     * Compile a regular expression
     * @param regularExpression
     * 
     * @exception   IllegalArgumentException
     *              if the regular expression can't be compiled
     * @exception   UnsupportedOperationException
     *              if instance acquisition fails
     */    
    public static Pattern_1_0 compile(
        String _sqlPattern
    ){
        String sqlPattern = _sqlPattern;
        try {
            StringBuilder regexpPattern = new StringBuilder();
            if(
                    SQLExpression.ACCEPT_GLOBAL_REGULAR_EXPRESSION_FLAGS &&
                    sqlPattern.startsWith("(?")
            ){
                int i = sqlPattern.indexOf(')') + 1;
                if(i > 0) {
                    regexpPattern.append(
                        sqlPattern.substring(0, i)
                    );
                    sqlPattern = sqlPattern.substring(i);
                }
            }
            String terminator = "$";
            for(
                    int i = 0;
                    i < sqlPattern.length();
                    i++
            ){
                char candidate = sqlPattern.charAt(i);
                if(i == 0){
                    if(
                            MISSING_DELIMITER_ALLOWS_ANY_MATCH && 
                            candidate == STRING_PLACEHOLDER
                    ) continue;
                    regexpPattern.append(
                        '^'
                    );
                }
                if(candidate == ESCAPE){
                    char escaped = sqlPattern.charAt(++i);
                    if(
                            escaped != STRING_PLACEHOLDER && 
                            escaped != CHARACTER_PLACEHOLDER
                    ) regexpPattern.append(
                        candidate
                    );
                    regexpPattern.append(
                        escaped
                    );
                } else if (candidate == STRING_PLACEHOLDER) {
                    if(
                            MISSING_DELIMITER_ALLOWS_ANY_MATCH && 
                            i + 1 == sqlPattern.length()
                    ){
                        terminator = "";
                    } else {
                        regexpPattern.append(
                            ".*?"
                        );
                    }
                } else if (candidate == CHARACTER_PLACEHOLDER) {
                    regexpPattern.append(
                        '.'
                    );
                } else if (
                        (candidate >= '/' && candidate <= '>') ||
                        (candidate >= '@' && candidate <= 'Z') ||
                        (candidate >= '`' && candidate <= 'z') ||
                        candidate > '~'    
                ) {
                    regexpPattern.append(
                        candidate
                    );
                } else {
                    regexpPattern.append(
                        "\\u"
                    ).append(
                        Integer.toHexString(1 + Character.MAX_VALUE + candidate).substring(1)
                    );
                }
            }
            return new Pattern_1(
                RegularExpression.compile(
                    regexpPattern.append(
                        terminator
                    ).toString()
                ),
                sqlPattern
            );
        } catch (UnsupportedOperationException exception) {
            throw exception;
        } catch (BadParameterException exception) {
            throw new BadParameterException(
                exception,
                "Could not parse the SQL pattern",
                new BasicException.Parameter("sqlPattern", sqlPattern)
            );
        } catch (NullPointerException exception) {
            throw new BadParameterException(
                exception,
                "SQL pattern must not be null",
                new BasicException.Parameter("sqlPattern", sqlPattern)
            );
        }
    }

    /**
     * May be set to true if "XAB" matches "AB$".
     */
    final static private boolean MISSING_DELIMITER_ALLOWS_ANY_MATCH = false;

    /**
     * May be set to true if global regular expression flags are accepted.
     */
    final static private boolean ACCEPT_GLOBAL_REGULAR_EXPRESSION_FLAGS = true;

    /**
     * SQL Pattern_1
     */
    static class Pattern_1 implements Pattern_1_0 {

        /**
         * 
         */
        private static final long serialVersionUID = 3977021755711893808L;

        /**
         * 
         */
        private final Pattern_1_0 pattern;

        /**
         * 
         */
        private final String expression;

        /**
         * Constructor
         * 
         * 
         */
        public Pattern_1(
            Pattern_1_0 pattern,
            String expression
        ){
            this.pattern = pattern;
            this.expression = expression;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object that) {
            return 
            this == that || 
            that instanceof Pattern_1 && this.pattern().equals(((Pattern_1)that).pattern());
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return pattern.hashCode();
        }

        /**
         * @param input
         * @return
         */
        public Matcher_1_0 matcher(String input) {
            return pattern.matcher(input);
        }

        /**
         * @param input
         * @return
         */
        public boolean matches(String input) {
            return pattern.matches(input);
        }

        /**
         * @return
         */
        public String pattern() {
            return this.expression;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return getClass().getName() + ": " + pattern();
        }

    }

}
