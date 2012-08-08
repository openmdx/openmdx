/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: XRISegments.java,v 1.2 2007/07/06 17:12:24 hburger Exp $
 * Description: XRISubSegments 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/07/06 17:12:24 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package org.oasisopen.spi2;

import org.openmdx.base.text.conversion.UnicodeTransformation;
import org.openxri.XRIParseException;
import org.openxri.XRISubSegment;

/**
 * XRI Sub-Segments
 */
public class XRISegments {

    /**
     * Avoid instantiation
     */
    protected XRISegments() {
    }

    /**
     * Append a subSegment value
     * 
     * @param segment the object the sub-segment will be appended to
     * @param optionalDelimiterRequired defines whether '*' sub-segment delimiters are required
     * @param persistent defines whther the sub-segment starts with '!' or '*'
     * @param crossReference defines whther the value has to be embedded into parenthesis
     * @param rawSubSegmentValue the raw sub-segment value
     */
    public static void append(
        StringBuilder segment,
        boolean optionalDelimiterRequired, 
        boolean persistent, 
        boolean crossReference, 
        Object rawSubSegmentValue
    ){
        StringBuilder buffer = new StringBuilder(
            persistent ? "!" : "*"
        );
        if(crossReference) {
            buffer.append('(');
        }
        buffer.append(rawSubSegmentValue);
        if(crossReference) {
            buffer.append(')');
        }
        String candidate = buffer.toString();
        if(escape(candidate)) {
            segment.append(
                candidate.charAt(0)
            ).append(
                CES_PREFIX
            );
            byte[] utf8 = UnicodeTransformation.toByteArray(
                candidate.substring(
                    crossReference ? 2 : 1, 
                    candidate.length() - (crossReference ? 1 : 0)
                )
            );
            for(
                int i = 0;
                i < utf8.length;
                i++
            ){
                byte octet = utf8[i];
                if(
                    (octet >= 'A' && octet <= 'Z') ||
                    (octet >= 'a' && octet <= 'z') ||
                    (octet >= '0' && octet <= '9') ||
                    (octet == '-' || octet == '.' || octet == '_' || octet == '~')
                ) {
                    segment.append((char)octet);
                } else {
                    int digits = 0xFF & octet;
                    segment.append(
                        '%'
                    ).append(
                        characterForDigit(digits / 0x10)
                    ).append(
                        characterForDigit(digits % 0x10)
                    );
                }
            }
            segment.append(CES_SUFFIX);
        } else {
            segment.append(
                persistent | optionalDelimiterRequired ? candidate : candidate.substring(1)
            );
        }
    }
    
    /**
     * Checks, whether a raw sub-segment value has to be escaped or not
     * 
     * @param rawSubSegmentValue
     * 
     * @return <code>true</code> if the raw value has to be escaped
     */
    private static final boolean escape(
        String rawSubSegmentValue
    ){
        if(isCaseExactString(rawSubSegmentValue)) {
            return true;
        } else try {
            new XRISubSegment(rawSubSegmentValue, true);
            return false;
        } catch (XRIParseException exception) {
            return true;
        }
    }
    
    /**
     * Retieves a sub-segment's raw value
     * 
     * @param subSegment the XRI sub-segment
     * @param crossReference tells whether a cross-reference is expected
     * 
     * @return a sub-segment's raw value
     */
    public static String toString(
        XRISubSegment subSegment,
        boolean crossReference
    ){
        String rawOrEscapedValue = subSegment.toString(true).substring(1);
        if(isCaseExactString(rawOrEscapedValue)) {
            byte[] utf8 = new byte[rawOrEscapedValue.length()];
            int j = 0;
            for(
                int i = CES_PREFIX.length(), iLimit = rawOrEscapedValue.length() - CES_SUFFIX.length();
                i < iLimit;
                i++
            ){
                char c = rawOrEscapedValue.charAt(i);
                if(c == '%') {
                    utf8[j++] = (byte) (
                        0x10 * digitForCharacter(rawOrEscapedValue.charAt(++i)) +
                        digitForCharacter(rawOrEscapedValue.charAt(++i))
                    );
                } else {
                    utf8[j++] = (byte) c;
                }
            }
            return UnicodeTransformation.toString(utf8, 0, j);   
        } else if (crossReference) {
            if(rawOrEscapedValue.startsWith("(") && rawOrEscapedValue.endsWith(")")) {
                return rawOrEscapedValue.substring(1, rawOrEscapedValue.length() - 1);
            } else throw new IllegalArgumentException(
                "SubSegment does not contain a cross reference: \"" + subSegment + '"'
            );
        } else {
            return rawOrEscapedValue;
        }
    }

    /**
     * Tells whether a value matches the case exact string meta data production.
     * <p>
     * This implementation works for valid sub-segments only.
     * 
     * @param value the string value to be tested
     * 
     * @return <code>true</code> if the value matches the case exact string production
     */
    public static boolean isCaseExactString(
        String value
    ){
        return 
            value.startsWith(CES_PREFIX) && 
            value.endsWith(CES_SUFFIX);
    }

    /**
     * Create an uppercase hexadecimal digit.
     * 
     * @param digit the digit to be converted.
     * 
     * @return the corresponding character
     */
    private final static char characterForDigit(
        int digit
    ){
        return (char) (
            digit < 10 ? '0' + digit : 'A' - 10 + digit
        );
    }

    /**
     * Parses a hexadecimal digtt
     *  
     * @param character a hexadecinal digit
     * 
     * @return the corresponding digit
     * 
     * @throws NumberFormatException if the character is not a valid hxadecimal digit
     */
    private final static int digitForCharacter(
        char character
    ){
        int digit = 
            character >= '0' && character <= '9' ? character - '0' :
            character >= 'A' && character <= 'Z' ? character - 'A' + 10 :
            character >= 'a' && character <= 'z' ? character - 'a' + 10 :
            -1;
        if(digit < 0) throw new NumberFormatException(
            "'" + character + "' is not a valid hexadecimal digit"
        );
        return digit;
    }

    /**
     * Case exact string meta data prefix
     */
    private static final String CES_PREFIX = "($t*ces*";
    
    /**
     * Case exact string meta data suffix
     */
    private static final String CES_SUFFIX = ")";

}
