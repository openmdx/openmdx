/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: URI Transformation 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */

package org.openmdx.base.text.conversion;


/**
 * URI Transformation
 */
public class URITransformation {

    /**
     * Constructor 
     */
    private URITransformation() {
        // Avoid instantiation
    }
    
    /**
     * Convert a percent encoded ASCII string to an UTF-16 string
     * 
     * @param source
     * 
     * @return the decoded value
     */
    public static String decode(
        String source
    ){
        if(source.indexOf('%') < 0) {
            return source;
        } else {
            byte[] utf8 = new byte[source.length()];
            int j = 0;
            for(
                int i = 0, iLimit = source.length();
                i < iLimit;
                i++
            ){
                char c = source.charAt(i);
                if(c == '%') {
                    utf8[j++] = (byte) (
                        0x10 * URITransformation.valueOfHexadecimalDigit(source.charAt(++i)) +
                        URITransformation.valueOfHexadecimalDigit(source.charAt(++i))
                    );
                } else {
                    utf8[j++] = (byte) c;
                }
            }
            return UnicodeTransformation.toString(utf8, 0, j);   
        }
    }
    
    /**
     * Escape percent signs
     * 
     * @param source the URI to be encoded
     * 
     * @return the encoded URI
     */
    public static String encode(
        String source 
    ){
        return source.replaceAll("%", "%25");
    }
    
    
    /**
     * Retrieve the value of an (upper or lower case) hexadecimal digit
     * 
     * @param digit a hexadecimal digit
     * 
     * @return the hexadecimal digit's value
     */
    private final static int valueOfHexadecimalDigit(
        char digit
    ){
        return 
            digit >= '0' && digit <= '9' ? digit - '0' :
            digit >= 'A' && digit <= 'Z' ? digit - 'A' + 10 :
            digit >= 'a' && digit <= 'z' ? digit - 'a' + 10 :
            -1; // invalid hexadecimal digit
    }

}
