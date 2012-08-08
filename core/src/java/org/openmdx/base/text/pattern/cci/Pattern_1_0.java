/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Pattern_1_0.java,v 1.5 2004/04/02 16:59:01 wfro Exp $
 * Description: openMDX Pattern interface
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/04/02 16:59:01 $
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
package org.openmdx.base.text.pattern.cci;

import java.io.Serializable;

/**
 * Pattern interface
 * <p>
 * Instances of this class are immutable and are safe for use by 
 * multiple concurrent threads. Instances of Matcher_1_0 class are 
 * not safe for such use. 
 */
public interface Pattern_1_0 
    extends Serializable
{

    /**
     * Attempts to match the given input against the pattern
     * <p>
     * An invocation of this convenience method of the form
     * <pre>
     *      matches(input)
     * </pre>
     * behaves in exactly the same way as the expression
     * <pre>
     *      matcher(input).matches()
     * </pre>
     *  
     * @param input
     */
    boolean matches (
        String input
    );

    /**
     * Creates a matcher that will match the given input against this 
     * pattern.
     * 
     * @param   input
     *          The character sequence to be matched
     * 
     * @return  A new matcher for this pattern
     */
    Matcher_1_0 matcher(
        String input
    ); 

    /**
     * Returns the expression from which this pattern was compiled. 
     *  
     * @return The source of this pattern
     */
    public String pattern();
        
}
