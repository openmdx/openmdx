/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: BASE64 encode/decode
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 * BASE64 encode/decode
 */
public class Base64 {
    
    /**
     * Avoid instantiation
     */
    private Base64(
    ) {
        super();
    }
    
    private static final char[] S_BASE64CHAR = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
        'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
        'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
        'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', '+', '/'
    };
    private static final char S_BASE64PAD = '=';
    private static final byte[] S_DECODETABLE = new byte[128];
    static {
        for (int i = 0;  i < S_DECODETABLE.length;  i ++)
            S_DECODETABLE[i] = Byte.MAX_VALUE;  // 127
        for (int i = 0;  i < S_BASE64CHAR.length;  i ++) // 0 to 63
            S_DECODETABLE[S_BASE64CHAR[i]] = (byte)i;
    }

    private static int decode0(char[] ibuf, byte[] obuf, int _wp) {
        int wp = _wp;
        int outlen = 3;
        if (ibuf[3] == S_BASE64PAD)  outlen = 2;
        if (ibuf[2] == S_BASE64PAD)  outlen = 1;
        int b0 = S_DECODETABLE[ibuf[0]];
        int b1 = S_DECODETABLE[ibuf[1]];
        int b2 = S_DECODETABLE[ibuf[2]];
        int b3 = S_DECODETABLE[ibuf[3]];
        switch (outlen) {
          case 1:
            obuf[wp] = (byte)(b0 << 2 & 0xfc | b1 >> 4 & 0x3);
            return 1;
          case 2:
            obuf[wp++] = (byte)(b0 << 2 & 0xfc | b1 >> 4 & 0x3);
            obuf[wp] = (byte)(b1 << 4 & 0xf0 | b2 >> 2 & 0xf);
            return 2;
          case 3:
            obuf[wp++] = (byte)(b0 << 2 & 0xfc | b1 >> 4 & 0x3);
            obuf[wp++] = (byte)(b1 << 4 & 0xf0 | b2 >> 2 & 0xf);
            obuf[wp] = (byte)(b2 << 6 & 0xc0 | b3 & 0x3f);
            return 3;
          default:
            throw new RuntimeException("Internal Errror");
        }
    }

    /**
     *
     */
    public static byte[] decode(char[] data, int off, int len) {
        char[] ibuf = new char[4];
        int ibufcount = 0;
        byte[] obuf = new byte[len/4*3+3];
        int obufcount = 0;
        for (int i = off;  i < off+len;  i ++) {
            char ch = data[i];
            if (ch == S_BASE64PAD
                || ch < S_DECODETABLE.length && S_DECODETABLE[ch] != Byte.MAX_VALUE) {
                ibuf[ibufcount++] = ch;
                if (ibufcount == ibuf.length) {
                    ibufcount = 0;
                    obufcount += decode0(ibuf, obuf, obufcount);
                }
            }
        }
        if (obufcount == obuf.length)
            return obuf;
        byte[] ret = new byte[obufcount];
        System.arraycopy(obuf, 0, ret, 0, obufcount);
        return ret;
    }

    /**
     *
     */
    public static byte[] decode(String data) {
        char[] ibuf = new char[4];
        int ibufcount = 0;
        byte[] obuf = new byte[data.length()/4*3+3];
        int obufcount = 0;
        for (int i = 0;  i < data.length();  i ++) {
            char ch = data.charAt(i);
            if (ch == S_BASE64PAD
                || ch < S_DECODETABLE.length && S_DECODETABLE[ch] != Byte.MAX_VALUE) {
                ibuf[ibufcount++] = ch;
                if (ibufcount == ibuf.length) {
                    ibufcount = 0;
                    obufcount += decode0(ibuf, obuf, obufcount);
                }
            }
        }
        if (obufcount == obuf.length)
            return obuf;
        byte[] ret = new byte[obufcount];
        System.arraycopy(obuf, 0, ret, 0, obufcount);
        return ret;
    }

    /**
     *
     */
    public static void decode(char[] data, int off, int len, OutputStream ostream) throws IOException {
        char[] ibuf = new char[4];
        int ibufcount = 0;
        byte[] obuf = new byte[3];
        for (int i = off;  i < off+len;  i ++) {
            char ch = data[i];
            if (ch == S_BASE64PAD
                || ch < S_DECODETABLE.length && S_DECODETABLE[ch] != Byte.MAX_VALUE) {
                ibuf[ibufcount++] = ch;
                if (ibufcount == ibuf.length) {
                    ibufcount = 0;
                    int obufcount = decode0(ibuf, obuf, 0);
                    ostream.write(obuf, 0, obufcount);
                }
            }
        }
    }

    /**
     *
     */
    public static void decode(String data, OutputStream ostream) throws IOException {
        char[] ibuf = new char[4];
        int ibufcount = 0;
        byte[] obuf = new byte[3];
        for (int i = 0;  i < data.length();  i ++) {
            char ch = data.charAt(i);
            if (ch == S_BASE64PAD
                || ch < S_DECODETABLE.length && S_DECODETABLE[ch] != Byte.MAX_VALUE) {
                ibuf[ibufcount++] = ch;
                if (ibufcount == ibuf.length) {
                    ibufcount = 0;
                    int obufcount = decode0(ibuf, obuf, 0);
                    ostream.write(obuf, 0, obufcount);
                }
            }
        }
    }

    /**
     * Returns base64 representation of specified byte array.
     */
    public static String encode(byte[] data) {
        return encode(data, 0, data.length);
    }

    /**
     * Returns base64 representation of specified byte array.
     */
    public static String encode(byte[] data, int off, int len) {
        if (len <= 0)  return "";
        char[] out = new char[len/3*4+4];
        int rindex = off;
        int windex = 0;
        int rest = len-off;
        while (rest >= 3) {
            int i = ((data[rindex]&0xff)<<16)
                +((data[rindex+1]&0xff)<<8)
                +(data[rindex+2]&0xff);
            out[windex++] = S_BASE64CHAR[i>>18];
            out[windex++] = S_BASE64CHAR[(i>>12)&0x3f];
            out[windex++] = S_BASE64CHAR[(i>>6)&0x3f];
            out[windex++] = S_BASE64CHAR[i&0x3f];
            rindex += 3;
            rest -= 3;
        }
        if (rest == 1) {
            int i = data[rindex]&0xff;
            out[windex++] = S_BASE64CHAR[i>>2];
            out[windex++] = S_BASE64CHAR[(i<<4)&0x3f];
            out[windex++] = S_BASE64PAD;
            out[windex++] = S_BASE64PAD;
        } else if (rest == 2) {
            int i = ((data[rindex]&0xff)<<8)+(data[rindex+1]&0xff);
            out[windex++] = S_BASE64CHAR[i>>10];
            out[windex++] = S_BASE64CHAR[(i>>4)&0x3f];
            out[windex++] = S_BASE64CHAR[(i<<2)&0x3f];
            out[windex++] = S_BASE64PAD;
        }
        return new String(out, 0, windex);
    }

    /**
     * Outputs base64 representation of the specified byte array to a byte stream.
     */
    public static void encode(byte[] data, int off, int len, OutputStream ostream) throws IOException {
        if (len <= 0)  return;
        byte[] out = new byte[4];
        int rindex = off;
        int rest = len-off;
        while (rest >= 3) {
            int i = ((data[rindex]&0xff)<<16)
                +((data[rindex+1]&0xff)<<8)
                +(data[rindex+2]&0xff);
            out[0] = (byte)S_BASE64CHAR[i>>18];
            out[1] = (byte)S_BASE64CHAR[(i>>12)&0x3f];
            out[2] = (byte)S_BASE64CHAR[(i>>6)&0x3f];
            out[3] = (byte)S_BASE64CHAR[i&0x3f];
            ostream.write(out, 0, 4);
            rindex += 3;
            rest -= 3;
        }
        if (rest == 1) {
            int i = data[rindex]&0xff;
            out[0] = (byte)S_BASE64CHAR[i>>2];
            out[1] = (byte)S_BASE64CHAR[(i<<4)&0x3f];
            out[2] = (byte)S_BASE64PAD;
            out[3] = (byte)S_BASE64PAD;
            ostream.write(out, 0, 4);
        } else if (rest == 2) {
            int i = ((data[rindex]&0xff)<<8)+(data[rindex+1]&0xff);
            out[0] = (byte)S_BASE64CHAR[i>>10];
            out[1] = (byte)S_BASE64CHAR[(i>>4)&0x3f];
            out[2] = (byte)S_BASE64CHAR[(i<<2)&0x3f];
            out[3] = (byte)S_BASE64PAD;
            ostream.write(out, 0, 4);
        }
    }

    /**
     * Outputs base64 representation of the specified byte array to a character stream.
     */
    private static void encode(
        byte[] data, 
        int off, 
        int len, 
        Writer writer,
        char[] tmp
    ) throws IOException {
        if (len <= 0)  return;
        int rindex = off;
        int rest = len-off;
        int output = 0;
        while (rest >= 3) {
            int i = ((data[rindex]&0xff)<<16)
                +((data[rindex+1]&0xff)<<8)
                +(data[rindex+2]&0xff);
            tmp[0] = S_BASE64CHAR[i>>18];
            tmp[1] = S_BASE64CHAR[(i>>12)&0x3f];
            tmp[2] = S_BASE64CHAR[(i>>6)&0x3f];
            tmp[3] = S_BASE64CHAR[i&0x3f];
            writer.write(tmp, 0, 4);
            rindex += 3;
            rest -= 3;
            output += 4;
            if (output % 76 == 0)
                writer.write('\n');
        }
        if (rest == 1) {
            int i = data[rindex]&0xff;
            tmp[0] = S_BASE64CHAR[i>>2];
            tmp[1] = S_BASE64CHAR[(i<<4)&0x3f];
            tmp[2] = S_BASE64PAD;
            tmp[3] = S_BASE64PAD;
            writer.write(tmp, 0, 4);
        } else if (rest == 2) {
            int i = ((data[rindex]&0xff)<<8)+(data[rindex+1]&0xff);
            tmp[0] = S_BASE64CHAR[i>>10];
            tmp[1] = S_BASE64CHAR[(i>>4)&0x3f];
            tmp[2] = S_BASE64CHAR[(i<<2)&0x3f];
            tmp[3] = S_BASE64PAD;
            writer.write(tmp, 0, 4);
        }
    }
    
    /**
     * Outputs base64 representation of the specified byte array to a character stream.
     */
    public static void encode(
        byte[] data, 
        int off, 
        int len, 
        Writer writer
    ) throws IOException {
        char[] tmp = new char[4];
        encode(data, off, len, writer, tmp);
    }
    
    /**
     * Outputs base64 representation of the specified byte array to a character stream.
     */
    public static void encode(
        InputStream data, 
        Writer writer
    ) throws IOException {
        char[] tmp = new char[4];
        byte[] bytes = new byte[3];
        int len = 0;
        while((len = data.read(bytes, 0, 3)) != -1) {
            encode(bytes, 0, len, writer, tmp);            
        }
    }
    
}

