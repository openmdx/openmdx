/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RegularExpressionPattern_1.java,v 1.2 2006/01/09 13:15:20 hburger Exp $
 * Description: openMDX Regular Expression Pattern
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/01/09 13:15:20 $
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

import java.util.regex.Pattern;

import org.openmdx.base.text.pattern.cci.Matcher_1_0;
import org.openmdx.base.text.pattern.cci.Pattern_1_0;

/**
 * Pattern implementation 
 */
final class RegularExpressionPattern_1 
    implements Pattern_1_0
{

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 1340205266230718256L;

    /**
     * 
     */
    private final Pattern compiledExpression;

    /**
     * 
     */
    private final String rawExpression;

    /**
     * Compiles a regular expression
     * 
     * @param pattern
     *
     * @exception IllegalArgumentException
     *            if the pattern can't be compiled
     */    
    RegularExpressionPattern_1(
        String pattern
    ) {
        this.rawExpression = pattern;
        this.compiledExpression = Pattern.compile(pattern);
    }


    //------------------------------------------------------------------------    
    // Implements Pattern_1_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.text.common.Pattern_1_0#match(java.lang.String)
     */
    public boolean matches(String source) {
        return matcher(source).matches();
    }

    /* (non-Javadoc)
     * @see org.openmdx.text.common.Pattern_1_0#pattern()
     */
    public String pattern() {
        return this.rawExpression;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.pattern.cci.Pattern_1_0#matcher(java.lang.String)
     */
    public Matcher_1_0 matcher(String input) {
        return new RegularExpressionMatcher_1(this.compiledExpression, input);
    }


    //------------------------------------------------------------------------    
    // Extends Object
    //------------------------------------------------------------------------
            
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getClass().getName() + ": " + pattern();
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object that) {
        return 
            this == that || 
            that instanceof RegularExpressionPattern_1 && this.pattern().equals(((RegularExpressionPattern_1)that).pattern());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return pattern().hashCode();
    }

}
