/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UnicodeTransformation.java,v 1.7 2009/05/26 13:10:13 wfro Exp $
 * Description: openMDX Character Conversions: Unicode Transformation 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/26 13:10:13 $
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
package org.openmdx.base.text.conversion;

import org.openmdx.base.text.conversion.cci.UnicodeTransformer_1_0;



/**
 * openMDX Character Conversions
 * Unicode Transformation
 */
public class UnicodeTransformation  {

    /**
     * Avoid instantiation
     */ 
    private UnicodeTransformation(
    ){
        super();
    }

    /**
     * Convert a String to a byte array.
     * 
     * @param source UTF-16 source
     * @return UTF-8 encoded result
     */
    public static byte[] toByteArray(
        String source
    ){
        return transformer.utf8Array(source);
    }

    /**
     * Convert a char array to a byte array.
     * 
     * @param source UTF-16 source
     * @param offset
     * @param length
     * @return UTF-8 encoded result
     */
    public static byte[] toByteArray(
        char[] source,
        int  offset,
        int length
    ){
        return transformer.utf8Array(
            new String(source, offset, length)
        );
    }

    /**
     * Convert byte array to String
     * 
     * @param source UTF-8 encoded source
     * @param offset
     * @param length
     * @return UTF-16 encoded result
     */
    public static String toString(
        byte[] source,
        int offset,
        int length
    ){
        return transformer.utf16String(source, offset, length);
    }

    /**
     * Convert byte array to a char array
     * 
     * @param source UTF-8 encoded source
     * @param offset
     * @param length
     * @return UTF-16 encoded result
     */
    public static char[] toCharArray(
        byte[] source,
        int offset,
        int length
    ){
        return transformer.utf16Array(source, offset, length);
    }

    /**
     * Get the currently used transformer instance.
     * @return an appropriate Transformer_1_0 instance
     */
    public static UnicodeTransformer_1_0 getTransformer(
    ){
        return transformer;
    }
    
    /**
     * There is a single UnicodeTransformer_1_0 class since openMDX 1.12;
     */
    private final static UnicodeTransformer_1_0 transformer = new StringUnicodeTransformer_1();
        
}
