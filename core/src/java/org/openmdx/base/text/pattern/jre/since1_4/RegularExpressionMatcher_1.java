/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RegularExpressionMatcher_1.java,v 1.2 2004/12/16 16:55:08 hburger Exp $
 * Description: openMDX Regular Expression Matcher
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/12/16 16:55:08 $
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
package org.openmdx.base.text.pattern.jre.since1_4;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmdx.base.text.pattern.cci.Matcher_1_0;

/**
 * A Regular Expression Matcher 
 */
class RegularExpressionMatcher_1 implements Matcher_1_0 {
 
    /**
     * 
     */
    private final Matcher matcher;

    /**
     * 
     */
    public RegularExpressionMatcher_1(
        Pattern pattern,
        String input
    ) {
        this.matcher = pattern.matcher(input);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.pattern.cci.Matcher_1_0#matches()
     */
    public boolean matches() {
        return this.matcher.matches();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.pattern.cci.Matcher_1_0#reset(java.lang.String)
     */
    public Matcher_1_0 reset(
        String input
    ) {
        this.matcher.reset(input);
        return this;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.text.jre.before1_5.MatchResult#start()
     */
    public int start(
    ){
        return this.matcher.start();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.text.jre.before1_5.MatchResult#start(int)
     */
    public int start(
       int group
    ) {
        return this.matcher.start(group);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.text.jre.before1_5.MatchResult#end()
     */
    public int end(
    ) {
        return this.matcher.end();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.text.jre.before1_5.MatchResult#end(int)
     */
    public int end(
        int group
    ) {
        return this.matcher.end(group);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.text.jre.before1_5.MatchResult#group()
     */
    public String group(
    ) {
        return this.matcher.group();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.text.jre.before1_5.MatchResult#group(int)
     */
    public String group(
        int group
    ) {
        return this.matcher.group(group);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.text.jre.before1_5.MatchResult#groupCount()
     */
    public int groupCount(
    ) {
        return this.matcher.groupCount();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.pattern.cci.Matcher_1_0#find()
     */
    public boolean find() {
        return this.matcher.find();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.pattern.cci.Matcher_1_0#find(int)
     */
    public boolean find(int start) {
        return this.matcher.find(start);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.pattern.cci.Matcher_1_0#replaceAll(java.lang.String)
     */
    public String replaceAll(String replacement) {
        return this.matcher.replaceAll(replacement);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.pattern.cci.Matcher_1_0#replaceFirst(java.lang.String)
     */
    public String replaceFirst(String replacement) {
        return this.matcher.replaceFirst(replacement);
    }
    
}
