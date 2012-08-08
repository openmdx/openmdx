/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: HexadecimalFormatter.java,v 1.2 2007/10/10 17:16:08 hburger Exp $
 * Description: Hexadecmial Formatter
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 17:16:08 $
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
package org.openmdx.kernel.text.format;

import java.util.Arrays;

/**
 * Hexadecmial Formatter
 */
public class HexadecimalFormatter {

    /**
     * Constructor
     * 
     * @param value to be formatted
     * @param digits number of digits, maybe leading to leading 0s or Fs
     * 
     * @exception IllegalArgumentException
     *            if digits is negative
     */
    public HexadecimalFormatter(
        long value,
        int digits
    ){
        if(digits < 0) throw new IllegalArgumentException("Digits must not be negative: " + digits);
        this.fixedSizeValue = value;
        this.digits = digits;
        this.variableSizeValue = null; // Will be ignored
    }

    /**
     * Constructor
     * <p>
     * The toString() method formats the value according to its size,
     * adding leading digits if necessary.
     * 
     * @param value to be formatted
     */
    public HexadecimalFormatter(
        long value
    ){
        this(value, 16);
    }
    
    /**
     * Constructor
     * <p>
     * The toString() method formats the value according to its size,
     * adding leading digits if necessary.
     * 
     * @param value to be formatted
     */
    public HexadecimalFormatter(
        int value
    ){
        this(value, 8);
    }

    /**
     * Constructor
     * <p>
     * The toString() method formats the value according to its size,
     * adding leading digits if necessary.
     * 
     * @param value to be formatted
     */
    public HexadecimalFormatter(
        short value
    ){
        this(value, 4);
    }

    /**
     * Constructor
     * <p>
     * The toString() method formats the value according to its size,
     * adding leading digits if necessary.
     * 
     * @param value to be formatted
     */
    public HexadecimalFormatter(
        byte value
    ){
        this(value, 2);
    }
    
    /**
     * Constructor
     * <p>
     * The toString() method formats the value according to its size.
     * 
     * @param value to be formatted
     * @param offset the offset in the buffer of the first byte to format.
     * @param bytes number of bytes, maybe leading to trailing blanks
     */
    public HexadecimalFormatter(
        byte[] value,
        int offset,
        int bytes
    ){
        if(bytes < 0) throw new IllegalArgumentException("Bytes must not be negative: " + bytes);
        this.fixedSizeValue = offset; 
        this.variableSizeValue = value;
        this.digits = value == null ? -1 : bytes * 2;
    }

    /**
     * Constructor
     * <p>
     * The toString() method formats the value according to its size.
     * 
     * @param value to be formatted
     */
    public HexadecimalFormatter(
        byte[] value
    ){
        this(value, 0, value == null ? 0 : value.length);
    }
    
    /**
     * 
     */
    private final long fixedSizeValue;

    /**
     * 
     */
    private final byte[] variableSizeValue;

    /**
     * A negative digits value represents a null value.
     */
    private final int digits;
    
    /**
     * 
     */
    private final static char[] HEXADECIMAL_DIGITS = new char[]{
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };
    
    /**
     * Converts a long value to a hexadciemal String.
     *
     * @param value a long to format
     * @param digits the number of digits
     * 
     * @return the hex formatted value with leading zeroes
     */
    private static char[] toHexadecimalString(         
        long _value, 
        int digits
    ){
        long value = _value;
        char[] buffer = new char[digits];
        for(
            int i = digits;
            i > 0;
            value >>= 4  // next digit
        ) buffer[--i] = HEXADECIMAL_DIGITS[(int)(value & 0xf)];
        return buffer;
    }

    /**
     * Convert the bytes value to a hexadecimal string
     * 
     * @param value to be formatted
     * @param offset the offset in the buffer of the first byte to format.
     * @param digits number of digits, maybe leading to trailing 0s
     * 
     * @return the bytes value as hexadecimal string
     */
    private static char[] toHexadecimalString(         
        byte[] value,
        int offset,
        int digits
    ){
        char[] buffer = new char[digits];
        int available = 2 * (value.length - offset);
        int cursor = 0;
        int end = digits;
        if(available < digits) end = available;
        if(offset < 0) cursor = -2 * offset;
        if (cursor != 0 || end != digits) Arrays.fill(buffer, ' ');
        while(cursor < end) {
            byte b = value[offset + cursor / 2];
            if(cursor % 2 == 0) b >>=4;
            buffer[cursor++] = HEXADECIMAL_DIGITS[b & 0xf];
        }
        return buffer;
    }
    
    
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.digits < 0 ?
            "null" :
            new String(
                this.variableSizeValue == null ? 
                    toHexadecimalString(this.fixedSizeValue, this.digits) :
                    toHexadecimalString(this.variableSizeValue, (int) this.fixedSizeValue, this.digits)
            );
    }

}
