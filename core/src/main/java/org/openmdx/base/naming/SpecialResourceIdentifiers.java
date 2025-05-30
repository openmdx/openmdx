/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Special Resource Identifiers 
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
package org.openmdx.base.naming;

import java.util.regex.Pattern;


/**
 * Special Resource Identifiers
 */
public class SpecialResourceIdentifiers {

    /**
     * Avoid instantiation
     */
    private SpecialResourceIdentifiers(
    ){
        super();
    }
    
    /**
     * The extent reference pattern
     */
    public static final Path EXTENT_REFERENCES = new Path("xri://@openmdx*($..)/provider/($..)/segment/($..)/extent");    

    /**
     * The extent object pattern
     */
    public static final Path EXTENT_OBJECTS = new Path("xri://@openmdx*($..)/provider/($..)/segment/($..)/extent/($..)");    

    /**
     * An escaped dot
     */
    private final static Pattern ESCAPED_DOT = Pattern.compile("\\.", Pattern.LITERAL);
    
    /**
     * Unescape a JDO-escaped xri
     * 
     * @param xri a JDO-escaped XRI
     * 
     * @return the unescaped XRI
     */
    public static String unescapeResourceIdentifier(
        String xri
    ){
        return xri.indexOf('\\') < 0 ? xri : ESCAPED_DOT.matcher(xri).replaceAll(".");
    }
    
}
