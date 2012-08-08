/*
 * ====================================================================
 * Project:     OMEX/Core, http://www.omex.ch/
 * Name:        $Id: NativeDataOutputStream.java,v 1.10 2008/01/07 13:52:49 wfro Exp $
 * Description: NativeDataOutputStream 
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/07 13:52:49 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.io;

import java.io.IOException;
import java.io.OutputStream;

import org.openmdx.kernel.natives.Native;

/**
 * NativeDataOutputStream
 */
public class NativeDataOutputStream implements DataOutput {
    
    //-----------------------------------------------------------------------
    public NativeDataOutputStream(
        OutputStream out
    ) {
        this.out = out;
        this.count = 0;
        this.internalizedStringsSize = 0;
    }
    
    //-----------------------------------------------------------------------
    private short getInternalizedStringIndex(
        String s
    ) {
        short index = -1;
        for(short i = 0; i < this.internalizedStringsSize; i++) {
            if(s == this.internalizedStrings[i]) {
                index = i;
                break;
            }
        }
        if(index == -1) {
            if(this.internalizedStringsSize >= this.internalizedStrings.length) {
                String[] newInternalizedStrings = new String[this.internalizedStrings.length << 1];
                System.arraycopy(this.internalizedStrings, 0, newInternalizedStrings, 0, this.internalizedStrings.length);
                this.internalizedStrings = newInternalizedStrings;
            }
            this.internalizedStrings[this.internalizedStringsSize++] = s;
            index = (short)-this.internalizedStringsSize;
        }
        return index;
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.io.DataOutput#writeInternalizedString(java.lang.String)
     */
    public void writeInternalizedString(
        String value
    ) throws IOException {
        short index = this.getInternalizedStringIndex(value);
        this.writeShort(index);
        if(index < 0) {
            this.writeString(value);
        }
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.io.DataOutput#writeInternalizedStrings(java.lang.String[])
     */
    public void writeInternalizedStrings(
        String[] values
    ) throws IOException {
        int length = values.length;
        this.writeShort(length);
        String[] mappedValues = new String[length];
        for(int i = 0; i < length; i++) {
            short index = this.getInternalizedStringIndex(values[i]);
            this.writeShort(index);
            mappedValues[i] = index >= 0 ? null : values[i];
        }
        this.writeStrings(mappedValues);
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.io.DataOutput#writeNumber(java.lang.Number)
     */
    public void writeNumber(
        Number value
    ) throws IOException {
        Numbers.writeNumber(this, value);
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.io.DataOutput#writeNumbers(java.lang.Number[])
     */
    public void writeNumbers(
        Number[] value
    ) throws IOException {
        int length = value.length; 
        this.writeShort(length);
        for(
            int i = 0;
            i < length;
            i++
        ){
            this.writeNumber(value[i]);
        }
    }

    //-----------------------------------------------------------------------
    private void assertBuffer(
        int newLen
    ) {
        if(newLen > this.cb.length) {
            char[] newcb = new char[newLen << 1];
            System.arraycopy(this.cb, 0, newcb, 0, this.count);
            this.cb = newcb;            
        }        
    }

    //-----------------------------------------------------------------------
    public void flush(
    ) throws IOException {
        this.flush(true);
    }
    
    //-----------------------------------------------------------------------
    private void flush(
        boolean force
    ) throws IOException {
        if(force || this.count > LIMIT) {
            int nBytes = 2 * this.count;
            if(this.bb == null || nBytes > this.bb.length) {
                this.bb = new byte[nBytes];
            }
            // Convert cb to bb
            Native.convertToByteSequence(
                this.cb, 
                this.bb,
                nBytes
            );
            this.out.write(nBytes >>> 24 & 0xFF);
            this.out.write(nBytes >>> 16 & 0xFF);
            this.out.write(nBytes >>> 8 & 0xFF);
            this.out.write(nBytes);            
            this.out.write(
                this.bb, 
                0, 
                nBytes 
            ); 
            this.count = 0;
        }
    }
    
    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.io.DataOutput#writeString(java.lang.String)
     */
    public void writeString(
        String value
    ) throws IOException {
        if(value == null) {
            this.writeLong(-1);
        }
        else {
            int len = value.length();
            int newLen = this.count + len + 2;
            this.assertBuffer(newLen);
            this.writeLong(len);
            value.getChars(
                0, 
                len, 
                this.cb, 
                this.count
            );
            this.count += len;
            this.flush(false);
        }
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.io.DataOutput#writeStrings(java.lang.String[])
     */
    public void writeStrings(
        String[] values
    ) throws IOException {
        this.writeShort(values.length);
        for(int i = 0; i < values.length; i++) {
            this.writeString(values[i]);
        }
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataOutput#write(int)
     */
    public void write(
        int value
    ) throws IOException {
        this.writeByte(value);
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataOutput#write(byte[])
     */
    public void write(
        byte[] value
    ) throws IOException {
        this.flush(true);
        this.out.write(value);
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataOutput#write(byte[], int, int)
     */
    public void write(
        byte[] value, 
        int off, 
        int len
    ) throws IOException {
        this.flush(true);
        this.out.write(value, off, len);
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataOutput#writeBoolean(boolean)
     */
    public void writeBoolean(
        boolean value
    ) throws IOException {
        this.assertBuffer(this.count + 1);
        this.cb[this.count++] = value ? (char)1 : (char)0;
        this.flush(false);
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataOutput#writeByte(int)
     */
    public void writeByte(
        int value
    ) throws IOException {
        this.flush(true);
        this.out.write(value);
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataOutput#writeBytes(java.lang.String)
     */
    public void writeBytes(
        String value
    ) throws IOException {
        throw new UnsupportedOperationException();        
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataOutput#writeChar(int)
     */
    public void writeChar(
        int value
    ) throws IOException {
        this.assertBuffer(this.count + 1);
        this.cb[this.count++] = (char)value;
        this.flush(false);
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataOutput#writeChars(java.lang.String)
     */
    public void writeChars(
        String value
    ) throws IOException {
        int newLen = this.count + value.length();
        this.assertBuffer(newLen);
        value.getChars(
            0, 
            value.length(), 
            this.cb, 
            this.count
        );
        this.flush(false);
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataOutput#writeDouble(double)
     */
    public void writeDouble(
        double value
    ) throws IOException {
        this.writeLong(Double.doubleToLongBits(value));
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataOutput#writeFloat(float)
     */
    public void writeFloat(
        float v
    ) throws IOException {
        this.writeInt(Float.floatToIntBits(v));        
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataOutput#writeInt(int)
     */
    public void writeInt(
        int value
    ) throws IOException {
        this.assertBuffer(this.count + 2);
        char c;
        c = (char)((value >>> 16) & 0xFFFF);
        this.cb[this.count++] = c;
        c = (char)((value >>>  0) & 0xFFFF);
        this.cb[this.count++] = c;
        this.flush(false);
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataOutput#writeLong(long)
     */
    public void writeLong(
        long value
    ) throws IOException {
        this.assertBuffer(this.count + 4);
        char c;
        c = (char)((value >>> 48) & 0xFFFF);
        this.cb[this.count++] = c;
        c = (char)((value >>> 32) & 0xFFFF);
        this.cb[this.count++] = c;
        c = (char)((value >>> 16) & 0xFFFF);
        this.cb[this.count++] = c;
        c = (char)((value >>> 0) & 0xFFFF);
        this.cb[this.count++] = c;
        this.flush(false);
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataOutput#writeShort(int)
     */
    public void writeShort(
        int value
    ) throws IOException {
        this.assertBuffer(this.count + 1);
        this.cb[this.count++] = (char)value;
        this.flush(false);
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataOutput#writeUTF(java.lang.String)
     */
    public void writeUTF(
        String value
    ) throws IOException {
        this.writeString(value);
    }
        
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final int LIMIT = 16384;
    
    private final OutputStream out;
    private int count;
    private char[] cb = new char[256];
    private byte[] bb = new byte[256];
    private String[] internalizedStrings = new String[16];
    private short internalizedStringsSize;
    
}
