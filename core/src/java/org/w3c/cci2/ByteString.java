/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ByteString.java,v 1.4 2010/06/07 08:47:27 hburger Exp $
 * Description: ByteString 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/07 08:47:27 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2009, OMEX AG, Switzerland
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
package org.w3c.cci2;

import java.util.Arrays;

/**
 * Byte String
 */
public final class ByteString implements Final {

    /**
     * Constructor 
     *
     * @param buffer a reference to the buffer is kept by 
     * <code>ByteString</code>.
     * <p><em>
     * Note:<br>
     * The buffer must not be modified by a cooperative
     * program after it has been used as <code>ByteString</code>
     * constructor argument.
     * </em>
     */
    public ByteString(
        byte[] buffer
    ){
        this(buffer, 0, buffer.length);
    }

    /**
     * Constructor 
     *
     * @param buffer a reference to the buffer is kept by 
     * <code>ByteString</code>.
     * <p><em>
     * Note:<br>
     * The buffer must not be modified by a cooperative
     * program after it has been used as <code>ByteString</code>
     * constructor argument.
     * </em>
     * @param offset the buffer's content before <code>offset</code>
     * is ignored
     * @param length the buffer's content after <code>offset + length</code>
     * is ignored
     */
    public ByteString(
        byte[] buffer,
        int offset,
        int length
    ){
        this.buffer = buffer;
        this.offset = offset;
        this.length = length;
    }

    /**
     * Retrieve the buffer.
     * <p><em>
     * Note:<br>
     * You must never modify this buffer!
     * </em> 
     * @return Returns the bytes.
     */
    public final byte[] buffer() {
        return this.buffer;
    }
    
    /**
     * Retrieve offset.
     *
     * @return Returns the offset.
     */
    public final int offset() {
        return this.offset;
    }

    /* (non-Javadoc)
     * @see java.lang.CharSequence#length()
     */
    public int length() {
        return this.length;
    }

    /* (non-Javadoc)
     * @see java.lang.CharSequence#subSequence(int, int)
     */
    public ByteString subSequence(int start, int end) {
        int length = end - start;
        if(start < 0 || end < 0 || length < 0 || end > length()) {
            throw new IndexOutOfBoundsException(
                "[" + start + "," + end + "] doos not fit into [0," + this.length + "]" 
            );
        }
        return new ByteString(
            this.buffer,
            this.offset + start,
            length
        );
    }

    /**
     * Creates a newly allocated byte array. 
     * Its size is the length of this byte string. 
     *
     * @return  the contents of this byte string, as a byte array.
     * @see     org.w3c.cci2.ByteString#length()
     */
    public byte[] toArray() {
        byte[] buffer = new byte[this.length];
        System.arraycopy(this.buffer, this.offset, buffer, 0, this.length);
        return buffer;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if(this.string == null) {
            this.string = String.valueOf(
                toHexadecimalString(this.buffer, this.offset, this.length * 2)
            ); 
        }
        return this.string;
    }

    /**
     * "Normalize" the array
     * 
     * @return the original array or a copy of a sub-sequence
     */
    private byte[] asArray(){
        if(this.array == null) {
            this.array = this.offset == 0 && this.length == this.buffer.length ? this.buffer : this.toArray();
        } 
        return this.array;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ByteString) {
            ByteString that = (ByteString) obj;
            return Arrays.equals(this.asArray(), that.asArray());
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(asArray());
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
        
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = 3826766941878437515L;

    private final static char[] HEXADECIMAL_DIGITS = new char[]{
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

    /**
     * @serial the buffer
     */
    private final byte[] buffer;

    /**
     * @serial the offset
     */
    private final int offset;
    
    /**
     * @serial the length
     */
    private final int length;

    /**
     * This byte string's <code>String</code> representation.
     */
    private transient String string;
    
    /**
     * This byte string's array representation.
     */
    private transient byte[] array;
    
}
