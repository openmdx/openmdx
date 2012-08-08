/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RegularExpression.java,v 1.10 2007/10/10 16:05:54 hburger Exp $
 * Description: openMDX SQL LIKE Pattern implementation
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:05:54 $
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

import org.openmdx.base.text.pattern.cci.Pattern_1_0;
import org.openmdx.base.text.pattern.jre.since1_4.RegularExpressionPatternFactory_1;
import org.openmdx.base.text.pattern.spi.PatternFactory_1_0;

/**
 * SQL Like pattern implementation
 */
public class RegularExpression {

    /**
     * Avoid instantiation
     */
    private RegularExpression(
    ){
        super();
    }

    /**
     * Compile a regular expression
     * 
     * @return a compiled pattern
     * 
     * @exception   IllegalArgumentException
     *              if the regular expression can't be compiled
     * @exception   UnsupportedOperationException
     *              if instance acquisition fails
     */
    public static Pattern_1_0 compile(
        String regularExpression
    ){ 
        return patternFactory.createPattern(regularExpression);
    }
    
    /**
     * There is a single PatternFactory_1_0 class since openMDX 1.12.
     */
    private static PatternFactory_1_0 patternFactory = new RegularExpressionPatternFactory_1();
    
}
