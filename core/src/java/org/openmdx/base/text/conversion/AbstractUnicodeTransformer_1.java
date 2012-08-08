/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractUnicodeTransformer_1.java,v 1.1 2009/05/26 13:10:13 wfro Exp $
 * Description: SPICE Character Conversions: Abstract Unicode Transformation 
 * Revision:    $Revision: 1.1 $
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
package org.openmdx.base.text.conversion;

import org.openmdx.base.text.conversion.cci.UnicodeTransformer_1_0;
import org.openmdx.kernel.exception.BasicException;


/**
 * @author hburger
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class AbstractUnicodeTransformer_1
implements UnicodeTransformer_1_0
{

    /**
     * Constructor
     */
    protected AbstractUnicodeTransformer_1(
    ){
        super();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.conversion.cci.UnicodeTransformer_1_0#utf8Array(int[], int, int)
     */
    //------------------------------------------------------------------------
    // Implements Transformer_1_0
    //------------------------------------------------------------------------

    public byte[] utf8Array(int[] source, int offset, int length) {
        int sourceLimit = offset + length;
        // determine how many bytes are needed for the complete conversion
        int targetLength = 0;
        for (int i=0; i<sourceLimit; i++) {
            if (source[i] < 0x80) {
                targetLength++;
            } else if (source[i] < 0x0800) {
                targetLength += 2;
            } else if (source[i] < 0x10000) {
                targetLength += 3;
            } else {
                targetLength += 4;
            }
        }
        // allocate a byte[] of the necessary size
        byte[] target = new byte[targetLength];
        // do the conversion from character code points to utf-8
        for(int i=0, bytes = 0; i<sourceLimit; i++) {
            int b = source[i];
            if(b < 0x80) {
                target[bytes++] = (byte)b;
            } else if (b < 0x0800) {
                target[bytes++] = (byte)(b >> 6 | 0xC0);
                target[bytes++] = (byte)(b & 0x3F | 0x80);
            } else if (b < 0x10000) {
                target[bytes++] = (byte)(b >> 12 | 0xE0);
                target[bytes++] = (byte)(b >> 6 & 0x3F | 0x80);
                target[bytes++] = (byte)(b & 0x3F | 0x80);
            } else {
                target[bytes++] = (byte)(b >> 18 | 0xF0);
                target[bytes++] = (byte)(b >> 12 & 0x3F | 0x80);
                target[bytes++] = (byte)(b >> 6 & 0x3F | 0x80);
                target[bytes++] = (byte)(b & 0x3F | 0x80);
            }
        }
        return target;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.conversion.cci.UnicodeTransformer_1_0#utf16Array(int[], int, int)
     */
    public char[] utf16Array(int[] source, int offset, int length) {
        int sourceLimit = offset + length;
        int targetLimit = 0;
        for(
                int sourceIndex = offset;
                sourceIndex < sourceLimit;
                sourceIndex++
        ) targetLimit += source[sourceIndex] > 0x10000 ? 2 : 1;
        char[] target = new char[targetLimit];
        for(
                int sourceIndex = offset, targetIndex = 0;
                sourceIndex < sourceLimit;
                sourceIndex++
        ){
            int unicode = source[sourceIndex];
            if(unicode > 0x10000){
                target[targetIndex++] = (char)((unicode - 0x10000) / 0x400 + 0xD800);
                target[targetIndex++] = (char)((unicode - 0x10000) % 0x400 + 0xDC00);
            } else {
                target[targetIndex++]=(char)unicode;
            }
        }
        return target;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.conversion.cci.UnicodeTransformer_1_0#utf32Array(byte[], int, int)
     */
    public int[] utf32Array(byte[] source, int offset, int length) {
        try {
            int sourceLimit = offset + length;
            int[] target = new int[length];
            int targetLimit = 0;
            for (
                    int sourceIndex=offset; 
                    sourceIndex < sourceLimit;
            ){ 
                int value = 0xFF & source[sourceIndex++];
                if(value >= 0xF0){ // 4 byte encoding
                    value &= 0x7;
                    value <<= 6;
                    value |= 0x3F & getContinuation(source,sourceIndex++);
                    value <<= 6;
                    value |= 0x3F & getContinuation(source,sourceIndex++);
                    value <<= 6;
                    value |= 0x3F & getContinuation(source,sourceIndex++);
                } else if (value >= 0xE0) { // 3 byte encoding
                    value &= 0xF;
                    value <<= 6;
                    value |= 0x3F & getContinuation(source,sourceIndex++);
                    value <<= 6;
                    value |= 0x3F & getContinuation(source,sourceIndex++);
                } else if (value >= 0xC0) { // 2 byte encoding
                    value &= 0x1F;
                    value <<= 6;
                    value |= 0x3F & getContinuation(source,sourceIndex++);
                } else if (value >= 0x80) throw BasicException.newStandAloneExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "Unsolicited trail byte",
                    new BasicException.Parameter("source",source),
                    new BasicException.Parameter("index",sourceIndex),
                    new BasicException.Parameter("value",value)
                );
                target[targetLimit++] = value;
            }
            return trim(target, targetLimit);
        } catch (BasicException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    private int getContinuation(
        byte[] source,
        int index
    ) throws BasicException {
        if(index >= source.length) throw BasicException.newStandAloneExceptionStack(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "Missing trail byte",
            new BasicException.Parameter("source",source),
            new BasicException.Parameter("index",index)
        ); 
        int unit = source[index];
        if(unit < 0x80 || unit >= 0xC0) throw BasicException.newStandAloneExceptionStack(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "Missing trail byte",
            new BasicException.Parameter("source",source),
            new BasicException.Parameter("index",index),
            new BasicException.Parameter("value",unit),
            new BasicException.Parameter("minimum", 0x80),
            new BasicException.Parameter("maximum", 0xDF)
        ); 
        return unit;
    }

    /**
     * 
     */
    private int[] trim(
        int[] source,
        int length
    ){
        if(source.length == length) return source;
        int[] target = new int[length];
        System.arraycopy (source, 0, target, 0, length);
        return target;      
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.conversion.cci.UnicodeTransformer_1_0#utf32Array(char[], int, int)
     */
    public int[] utf32Array(char[] source, int offset, int length) {
        try {
            int sourceLimit = offset + length;
            int[] target = new int[length];
            int targetLimit = 0;
            for (
                    int sourceIndex = offset; 
                    sourceIndex < sourceLimit;
            ){ 
                int value = source[sourceIndex++];
                if(value >= 0xD800 && value <= 0xDBFF){
                    value = 0x10000 + (value - 0xD800) * 0x400; // high-surrogate
                    value += getContinuation(source, sourceIndex++) - 0xDC00; // low-surrogate
                } else if (value >= 0xDC00 && value <= 0xDFFF) throw BasicException.newStandAloneExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "Low-surrogate not preceeded by high-surrogate",
                    new BasicException.Parameter("source",source),
                    new BasicException.Parameter("index",sourceIndex),
                    new BasicException.Parameter("value",value)
                );
                target[targetLimit++] = value;
            }
            return trim(target, targetLimit);
        } catch (BasicException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    private int getContinuation(
        char[] source,
        int index
    ) throws BasicException {
        if(index >= source.length) throw BasicException.newStandAloneExceptionStack(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "Missing low-surrogate",
            new BasicException.Parameter("source",source),
            new BasicException.Parameter("index",index)
        ); 
        int unit = source[index];
        if(unit < 0xDC00 || unit > 0xDFFF) throw BasicException.newStandAloneExceptionStack(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "Missing low surrogate",
            new BasicException.Parameter("source",source),
            new BasicException.Parameter("index",index),
            new BasicException.Parameter("value",unit),
            new BasicException.Parameter("minimum", 0xDC00),
            new BasicException.Parameter("maximum", 0xDFFF)
        ); 
        return unit;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.text.conversion.cci.UnicodeTransformer_1_0#utf32Array(java.lang.String)
     */
    public int[] utf32Array(String source) {
        char[] utf16 = source.toCharArray();
        return utf32Array(utf16, 0, utf16.length);
    }

}
