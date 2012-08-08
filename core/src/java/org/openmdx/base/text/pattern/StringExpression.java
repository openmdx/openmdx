/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: StringExpression.java,v 1.7 2007/10/10 16:05:54 hburger Exp $
 * Description: openMDX String Pattern implementation
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:05:54 $
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
package org.openmdx.base.text.pattern;

import org.openmdx.base.text.pattern.cci.Matcher_1_0;
import org.openmdx.base.text.pattern.cci.Pattern_1_0;

import org.openmdx.kernel.text.StringBuilders;

/**
 * openMDX String pattern implementation
 */
public class StringExpression {

    /**
     * Avoid instantiation
     */
    private StringExpression(
    ){
        super();
    }

    /**
     * Compile a string expression
     * 
     * @return a compiled pattern
     * 
     * @exception   IllegalArgumentException
     *              if the regular expression can't be compiled
     * @exception   UnsupportedOperationException
     *              if instance acquisition fails
     */
    public static Pattern_1_0 compile(
        String pattern
    ){
        return new Pattern_1 (pattern);
    }

    /**
     *
     */
    private static final class Pattern_1 implements Pattern_1_0 {

        /**
         * 
         */
        private static final long serialVersionUID = 3618136745229564212L;

        /**
         * Constructor
         * 
         * @param pattern
         */
        Pattern_1(
            String pattern
        ){
            this.pattern = pattern;
        }

        /**
         * 
         */
        private final String pattern;

        /* (non-Javadoc)
         * @see org.openmdx.base.text.pattern.cci.Pattern_1_0#matches(java.lang.String)
         */
        public boolean matches(String input) {
            return this.pattern.equals(input);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.text.pattern.cci.Pattern_1_0#matcher(java.lang.String)
         */
        public Matcher_1_0 matcher(String input) {
            return new Matcher_1(this.pattern, input);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.text.pattern.cci.Pattern_1_0#pattern()
         */
        public String pattern() {
            return this.pattern;
        }

    }

    /**
     * 
     */
    private static final class Matcher_1 implements Matcher_1_0 {

        /**
         * Constructor
         * 
         * @param input
         */
        Matcher_1(
            String pattern,
            String input
        ){
            this.pattern = pattern;
            reset(input);
        }

        /**
         * 
         */
        private final String pattern;

        /**
         * 
         */
        private String input;

        /**
         * 
         */
        private transient int match;

        /* (non-Javadoc)
         * @see org.openmdx.base.text.pattern.cci.Matcher_1_0#matches()
         */
        public boolean matches() {
            return this.pattern.equals(this.input);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.text.pattern.cci.Matcher_1_0#reset(java.lang.String)
         */
        public Matcher_1_0 reset(String input) {
            this.input = input;
            this.match = -1;
            return this;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.text.pattern.cci.Matcher_1_0#replaceAll(java.lang.String)
         */
        public String replaceAll(String replacement) {
            CharSequence target = StringBuilders.newStringBuilder();
            int position = 0;
            while(find(position)){
                StringBuilders.asStringBuilder(target).append(
                    this.input.substring(position, start())
                ).append(
                    replacement
                );
                position = end();
            }
            return StringBuilders.asStringBuilder(target).append(
                this.input.substring(position)
            ).toString();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.text.pattern.cci.Matcher_1_0#replaceFirst(java.lang.String)
         */
        public String replaceFirst(String replacement) {
            return find(0) ?
                this.input.substring(0, start()) + replacement + this.input.substring(end()) :
                this.input;
        }

        /* (non-Javadoc)
         * @see org.openmdx.uses.javax.util.regex.MatchResult#start()
         */
        public int start() {
            assertMatch();
            return this.match;
        }

        /* (non-Javadoc)
         * @see org.openmdx.uses.javax.util.regex.MatchResult#start(int)
         */
        public int start(int group) {
            assertGroup(group);
            return 0;
        }

        /* (non-Javadoc)
         * @see org.openmdx.uses.javax.util.regex.MatchResult#end()
         */
        public int end() {
            assertMatch();
            return this.match + this.pattern.length();
        }

        /* (non-Javadoc)
         * @see org.openmdx.uses.javax.util.regex.MatchResult#end(int)
         */
        public int end(int group) {
            assertGroup(group);
            return this.input.length();
        }

        /* (non-Javadoc)
         * @see org.openmdx.uses.javax.util.regex.MatchResult#group()
         */
        public String group() {
            assertMatch();
            return this.pattern;
        }

        /* (non-Javadoc)
         * @see org.openmdx.uses.javax.util.regex.MatchResult#group(int)
         */
        public String group(int group) {
            return group == 0 ? this.input : null;
        }

        /* (non-Javadoc)
         * @see org.openmdx.uses.javax.util.regex.MatchResult#groupCount()
         */
        public int groupCount() {
            return 0;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.text.pattern.cci.Matcher_1_0#find()
         */
        public boolean find() {
            return this.match < 0 ? find(0) : find(end());
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.text.pattern.cci.Matcher_1_0#find(int)
         */
        public boolean find(
            int start
        ) {
            assertPosition(start);
            this.match = this.input.indexOf(this.pattern, start);
            return this.match >= 0;
        }

        /**
         * Assert group index validity
         * 
         * @param group
         * 
         * @exception IndexOutOfBoundsException
         * if there is no capturing group in the pattern with the given index
         */
        private void assertGroup(
            int group
        ){
            if(group > 0) throw new IndexOutOfBoundsException(
                "There is no capturing group in the pattern with index '" + group + "'"
            );

        }

        /**
         * Assert position validity
         * 
         * @param position
         * 
         * @exception IndexOutOfBoundsException
         * If position is less than zero or if position is greater than 
         * the length of the input sequence.
         */
        private void assertPosition(
            int position
        ){
            if(position < 0 || position > this.input.length()) throw new IndexOutOfBoundsException(
                "Position '" + position + "' is not in the range [0.." + this.input.length() + "]"
            );

        }

        /**
         * Assert match
         * 
         * @param group
         * 
         * @exception IllegalStateException
         * if no match has yet been attempted, or if the previous match operation failed
         */
        private void assertMatch(
        ){
            if(this.match < 0) throw new IllegalStateException(
                "No match has yet been attempted, or if the previous match operation failed"
            );

        }

    }

}
