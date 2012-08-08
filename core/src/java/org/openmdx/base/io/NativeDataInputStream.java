/*
 * ====================================================================
 * Project:     OMEX/Core, http://www.omex.ch/
 * Name:        $Id: NativeDataInputStream.java,v 1.9 2008/01/07 13:52:49 wfro Exp $
 * Description: NioDataInputStream 
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/07 13:52:49 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
import java.io.InputStream;

import org.openmdx.kernel.natives.Native;

/**
 * NativeDataInputStream
 */
public class NativeDataInputStream implements DataInput
{
    
    //-----------------------------------------------------------------------
    public NativeDataInputStream(
        InputStream in
    ) {
        this.in = in;
        this.pos = 0;
        this.count = 0;
    }
    
    //-----------------------------------------------------------------------
    private void assertBuffer(
    ) throws IOException {
        if(this.pos >= this.count) {
            int b1 = this.in.read() << 24;
            int b2 = this.in.read() << 16;
            int b3 = this.in.read() << 8;
            int b4 = this.in.read();
            int nBytes = b1 | b2 | b3 | b4;
            if(this.bb == null || this.bb.length < nBytes) {
                this.bb = new byte[nBytes];
            }
            this.in.read(this.bb, 0, nBytes);
            int nChars = nBytes >>> 1;
            if(this.cb == null || this.cb.length < nChars) {
                this.cb = new char[nChars];
            }
            Native.convertToCharSequence(
                this.bb,
                this.cb,
                nChars
            );
            this.pos = 0;
            this.count = nChars;
        }
    }
    
    //-----------------------------------------------------------------------
    public String readString(
    ) throws IOException {
        int len = (int)this.readLong();
        if(len < 0) {
            return null;
        } 
        else {
            this.assertBuffer();
            String s = new String(
                this.cb, 
                this.pos, 
                len
            );
            this.pos += len;
            return s;
        }
    }
    
    //-----------------------------------------------------------------------
    public String[] readStrings(
    ) throws IOException {
        int length = this.readShort();
        String[] value = new String[length];
        for(
            int i = 0;
            i < length;
            i++
        ){
            value[i] = this.readString();
        }
        return value;
    }
    
    //-----------------------------------------------------------------------
    private void addInternalizedString(
        short index,
        String s
    ) {
        index = (short)-(index + 1);
        if(index >= this.internalizedStrings.length) {
            String[] newInternalizedStrings = new String[this.internalizedStrings.length << 1];
            System.arraycopy(this.internalizedStrings, 0, newInternalizedStrings, 0, this.internalizedStrings.length);
            this.internalizedStrings = newInternalizedStrings;            
        }
        this.internalizedStrings[index] = s;
    }
    
    //-----------------------------------------------------------------------
    public String readInternalizedString(
    ) throws IOException {
        short index = this.readShort();
        if(index < 0) {
            String s = null;
            this.addInternalizedString(
                index,
                s = this.readString().intern()
            );
            return s;
        }
        else {
            return this.internalizedStrings[index];
        }
    }
    
    //-----------------------------------------------------------------------
    public String[] readInternalizedStrings(
    ) throws IOException {
        int count = this.readShort();
        short[] indexes = new short[count];
        for(int i = 0; i < indexes.length; i++) {
            indexes[i] = this.readShort();
        }
        String[] values = this.readStrings();
        for(int i = 0; i < indexes.length; i++) {
            short index = indexes[i];
            if(index < 0) {
                this.addInternalizedString(
                    index,
                    values[i] = values[i].intern()
                );
            }
            else {
                values[i] = this.internalizedStrings[index];
            }
        }
        return values;
    }

    //-----------------------------------------------------------------------
    public Number readNumber(
    ) throws IOException {
        this.assertBuffer();
        return Numbers.readNumber(this);
    }
    
    //-----------------------------------------------------------------------
    public Number[] readNumbers(
    ) throws IOException {
        int length = this.readShort();
        Number[] value = new Number[length];
        for(
            int i = 0;
            i < length;
            i++
        ){
            value[i] = readNumber();
        }
        return value;
    }
    
    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataInput#readBoolean()
     */
    public boolean readBoolean(
    ) throws IOException {
        short b = this.readShort();
        return b != 0;
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataInput#readByte()
     */
    public byte readByte(
    ) throws IOException {
        return (byte)this.in.read();
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataInput#readChar()
     */
    public char readChar(
    ) throws IOException {
        this.assertBuffer();
        return this.cb[this.pos++];
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataInput#readDouble()
     */
    public double readDouble(
    ) throws IOException {
        return Double.longBitsToDouble(this.readLong());
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataInput#readFloat()
     */
    public float readFloat(
    ) throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataInput#readFully(byte[])
     */
    public void readFully(
        byte[] b
    ) throws IOException {
        this.in.read(b);
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataInput#readFully(byte[], int, int)
     */
    public void readFully(
        byte[] b, 
        int off, 
        int len
    ) throws IOException {
        this.in.read(b, off, len);
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataInput#readInt()
     */
    public int readInt(
    ) throws IOException {
        this.assertBuffer();
        return 
            (this.cb[this.pos++] << 16) | 
            this.cb[this.pos++];
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataInput#readLine()
     */
    public String readLine(
    ) throws IOException {
        throw new UnsupportedOperationException();
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataInput#readLong()
     */
    public long readLong(
    ) throws IOException {
        this.assertBuffer();
        return 
            (this.cb[this.pos++] << 48) | 
            (this.cb[this.pos++] << 32) | 
            (this.cb[this.pos++] << 16) | 
            this.cb[this.pos++];
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataInput#readShort()
     */
    public short readShort(
    ) throws IOException {
        this.assertBuffer();
        return (short)this.cb[this.pos++];
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataInput#readUTF()
     */
    public String readUTF(
    ) throws IOException {
        return this.readString();
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataInput#readUnsignedByte()
     */
    public int readUnsignedByte(
    ) throws IOException {
        throw new UnsupportedOperationException();
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataInput#readUnsignedShort()
     */
    public int readUnsignedShort(
    ) throws IOException {
        throw new UnsupportedOperationException();
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.io.DataInput#skipBytes(int)
     */
    public int skipBytes(
        int n
    ) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private InputStream in;
    private char[] cb;
    private byte[] bb;
    private int pos;
    private int count;
    private String[] internalizedStrings = new String[16];
    
}
