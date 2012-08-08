/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: PathPattern.java,v 1.2 2005/02/21 13:10:12 hburger Exp $
 * Description: PathPattern_1
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/02/21 13:10:12 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.compatibility.base.query;

import org.openmdx.base.text.pattern.cci.Matcher_1_0;
import org.openmdx.base.text.pattern.cci.Pattern_1_0;
import org.openmdx.compatibility.base.naming.Path;


/**
 * PathPattern_1
 */
class PathPattern implements Pattern_1_0 {

    /**
     * 
     */
    private static final long serialVersionUID = 3256441391432086579L;
    /**
     * 
     */
    private final Path pathPattern;
    
    /**
     * Constructor
     * 
     * 
     */
    PathPattern(
         Path pathPattern
    ) {
        super();
        this.pathPattern = pathPattern;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.pattern.cci.Pattern_1_0#matches(java.lang.String)
     */
    public boolean matches(String input) {
        try {
            return matches(new Path(input));
        } catch (Exception exception){
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.pattern.cci.Pattern_1_0#matches(java.lang.String)
     */
    public boolean matches(Path input) {
        return input.isLike(this.pathPattern);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.pattern.cci.Pattern_1_0#matcher(java.lang.String)
     */
    public Matcher_1_0 matcher(String input) {
        throw new UnsupportedOperationException("Matcher not supported by PathPattern");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.pattern.cci.Pattern_1_0#pattern()
     */
    public String pattern() {
        return this.pathPattern.toUri();
    }

}